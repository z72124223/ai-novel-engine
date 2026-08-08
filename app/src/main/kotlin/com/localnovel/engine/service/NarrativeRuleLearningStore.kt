package com.localnovel.engine.service

import com.localnovel.engine.model.LearnedNarrativeRule
import com.localnovel.engine.model.LearnedNarrativeRulePack
import com.localnovel.engine.model.NarrativeQualityFinding
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant

object NarrativeRuleAutoPromoter {
    private val skipEvidencePrefixes = listOf(
        "missing=",
        "denseClauseCount=",
        "matchedKeywords=",
        "requiredMatches=",
        "consecutiveAtmosphere=",
        "beatId=",
    )

    fun promote(
        projectId: String,
        dbPath: Path,
        findings: List<NarrativeQualityFinding>,
    ): NarrativeRuleLearningStore.PromotionResult {
        val promotable = findings.flatMap { finding -> patternsFromFinding(finding).map { finding to it } }
        if (promotable.isEmpty()) {
            return NarrativeRuleLearningStore.PromotionResult(emptyList(), findings.size)
        }
        return NarrativeRuleLearningStore.promotePatterns(
            projectId = projectId,
            dbPath = dbPath,
            entries = promotable.map { (finding, pattern) ->
                NarrativeRuleLearningStore.PromotionEntry(
                    pattern = pattern,
                    category = finding.category.ifBlank { "logic" },
                    severity = finding.severity,
                    message = "Learned block: narrative contains '$pattern' (${finding.ruleId}).",
                    suggestedFix = finding.suggestedFix.ifBlank { "Remove or replace '$pattern'." },
                    sourceRuleId = finding.ruleId,
                    sourceEvidence = pattern,
                )
            },
        )
    }

    private fun patternsFromFinding(finding: NarrativeQualityFinding): List<String> =
        finding.evidence
            .mapNotNull { evidence -> extractPromotablePattern(evidence) }
            .filter { it.length in 2..24 }
            .distinct()

    private fun extractPromotablePattern(evidence: String): String? {
        val trimmed = evidence.trim()
        if (trimmed.isBlank()) return null
        if (skipEvidencePrefixes.any { trimmed.startsWith(it) }) return null
        val quoteMatch = Regex("""quote=(.+)""").find(trimmed)?.groupValues?.getOrNull(1)?.trim()
        if (!quoteMatch.isNullOrBlank()) return quoteMatch.take(24)
        if (trimmed.startsWith("「") && trimmed.endsWith("」")) {
            return trimmed.removePrefix("「").removeSuffix("」").take(24)
        }
        if (trimmed.any { it in 'a'..'z' || it in 'A'..'Z' }) return null
        return trimmed.take(24)
    }
}

class NarrativeRuleLearningStore {
    data class PromotionEntry(
        val pattern: String,
        val category: String,
        val severity: String,
        val message: String,
        val suggestedFix: String,
        val sourceRuleId: String,
        val sourceEvidence: String,
    )

    data class PromotionResult(
        val promotedRules: List<LearnedNarrativeRule>,
        val skippedCount: Int,
    )

    companion object {
        fun storePath(dbPath: Path, projectId: String): Path =
            dbPath.parent.resolve("narrative_learned_rules").resolve("$projectId.json")

        fun load(projectId: String, dbPath: Path): LearnedNarrativeRulePack {
            val path = storePath(dbPath, projectId)
            if (!Files.exists(path)) {
                return LearnedNarrativeRulePack(projectId = projectId)
            }
            return JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
        }

        fun save(pack: LearnedNarrativeRulePack, dbPath: Path) {
            val path = storePath(dbPath, pack.projectId)
            Files.createDirectories(path.parent)
            val updated = pack.copy(updatedAt = Instant.now().toString())
            Files.writeString(path, JsonSupport.json.encodeToString(updated))
        }

        fun promotePatterns(
            projectId: String,
            dbPath: Path,
            entries: List<PromotionEntry>,
        ): PromotionResult {
            val pack = load(projectId, dbPath)
            val now = Instant.now().toString()
            val existingByPattern = pack.rules.associateBy { normalizePattern(it.pattern) }.toMutableMap()
            val promoted = mutableListOf<LearnedNarrativeRule>()
            var skipped = 0

            entries.forEach { entry ->
                val normalized = normalizePattern(entry.pattern)
                if (normalized.isBlank()) {
                    skipped += 1
                    return@forEach
                }
                val existing = existingByPattern[normalized]
                if (existing != null) {
                    val bumped = existing.copy(hitCount = existing.hitCount + 1)
                    existingByPattern[normalized] = bumped
                    skipped += 1
                    return@forEach
                }
                val rule = LearnedNarrativeRule(
                    ruleId = "LEARNED-${slug(entry.sourceRuleId)}-${normalized.take(12)}",
                    category = entry.category,
                    severity = entry.severity,
                    patternType = "narrative_contains",
                    pattern = entry.pattern,
                    message = entry.message,
                    suggestedFix = entry.suggestedFix,
                    sourceRuleId = entry.sourceRuleId,
                    sourceEvidence = entry.sourceEvidence,
                    promotedAt = now,
                )
                existingByPattern[normalized] = rule
                promoted += rule
            }

            if (promoted.isNotEmpty() || skipped > 0) {
                save(
                    pack.copy(
                        projectId = projectId,
                        rules = existingByPattern.values.sortedBy { it.ruleId },
                    ),
                    dbPath,
                )
            }
            return PromotionResult(promotedRules = promoted, skippedCount = skipped)
        }

        private fun normalizePattern(pattern: String): String =
            pattern.trim()
                .replace(Regex("\\s+"), "")
                .replace(Regex("[，。！？；：、,.!?;:「」\"'…—\\-]"), "")

        private fun slug(value: String): String =
            value.lowercase()
                .replace(Regex("[^a-z0-9]+"), "-")
                .trim('-')
                .ifBlank { "finding" }
    }
}

object LearnedNarrativeRuleEvaluator {
    fun evaluate(
        narrative: String,
        rules: List<LearnedNarrativeRule>,
    ): List<NarrativeQualityFinding> =
        rules.mapNotNull { rule ->
            when (rule.patternType) {
                "narrative_contains" -> {
                    if (rule.pattern.isNotBlank() && narrative.contains(rule.pattern)) {
                        NarrativeQualityFinding(
                            ruleId = rule.ruleId,
                            category = rule.category.ifBlank { "learned_rule" },
                            severity = rule.severity,
                            message = rule.message,
                            evidence = listOf(rule.pattern),
                            suggestedFix = rule.suggestedFix.ifBlank { "Remove learned blocked pattern." },
                        )
                    } else {
                        null
                    }
                }
                else -> null
            }
        }
}