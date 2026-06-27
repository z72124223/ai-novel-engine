package com.localnovel.engine.service

import com.localnovel.engine.model.NovelizationRevisedDraftIntakeEvidence
import com.localnovel.engine.model.NovelizationRevisedDraftIntakeFinding
import com.localnovel.engine.model.NovelizationRevisedDraftIntakeReport
import com.localnovel.engine.model.NovelizationRevisionBriefReport
import com.localnovel.engine.model.NovelizationRevisionHandlingDeclaration
import kotlin.math.ceil

class NovelizationRevisedDraftIntakeValidationService {
    private val knownVerdicts = setOf("PASS", "PASS_WITH_WARNINGS", "FAIL")
    private val acceptableHandlingStatuses = setOf("HANDLED", "NOT_APPLICABLE")
    private val bannedStructures = listOf(
        BannedStructure(
            "CALIBRATION_ZH_THIS_NOT_THIS_IS_COMPACT",
            Regex("\u9019\u4e0d\u662f\\S+\\s*[\uFF0C, ]\\s*\u9019\u662f\\S+"),
        ),
        BannedStructure(
            "CALIBRATION_ZH_THIS_NOT_THIS_IS_SPACED",
            Regex("\u9019\u4e0d\u662f\\s+.+?[\uFF0C, ]\\s*\u9019\u662f\\s+.+"),
        ),
        BannedStructure(
            "CALIBRATION_ZH_NOT_BUT_COMPACT",
            Regex("\u4e0d\u662f\\S+\\s*[\uFF0C, ]?\\s*\u800c\u662f\\S+"),
        ),
        BannedStructure(
            "CALIBRATION_ZH_NOT_BUT_SPACED",
            Regex("\u4e0d\u662f\\s+.+?[\uFF0C, ]?\\s*\u800c\u662f\\s+.+"),
        ),
        BannedStructure("CALIBRATION_EN_NOT_BUT", Regex("""(?i)\bnot\s+.+?\s*,?\s+but\s+.+""")),
        BannedStructure("CALIBRATION_EN_NOT_ONLY", Regex("""(?i)\bnot\s+only\b.+?\bbut(?:\s+also)?\b.+""")),
    )

    fun validate(
        originalDraftText: String,
        revisedDraftText: String,
        revisionBriefReport: NovelizationRevisionBriefReport,
        declaredHandling: NovelizationRevisionHandlingDeclaration = NovelizationRevisionHandlingDeclaration(),
        originalDraftFile: String = "",
        revisedDraftFile: String = "",
        revisionBriefReportFile: String = "",
        declaredHandlingFile: String = "",
    ): NovelizationRevisedDraftIntakeReport {
        val findings = mutableListOf<NovelizationRevisedDraftIntakeFinding>()
        val normalizedOriginal = normalizeForComparison(originalDraftText)
        val normalizedRevised = normalizeForComparison(revisedDraftText)
        val unchangedAfterNormalization = normalizedOriginal.isNotEmpty() && normalizedOriginal == normalizedRevised
        val revisionBriefVerdict = revisionBriefReport.verdict.trim().uppercase()

        validateRevisionBriefVerdict(revisionBriefReport, revisionBriefVerdict, findings)
        validateDraftShape(
            originalDraftText = originalDraftText,
            revisedDraftText = revisedDraftText,
            unchangedAfterNormalization = unchangedAfterNormalization,
            findings = findings,
        )
        validateDeclaredHandling(
            revisionBriefReport = revisionBriefReport,
            declaredHandling = declaredHandling,
            declaredHandlingFile = declaredHandlingFile,
            findings = findings,
        )
        val bannedResidualCount = validateBannedResiduals(revisedDraftText, findings)
        val preservedAnchorCount = validateContinuityAnchors(
            originalDraftText = originalDraftText,
            revisedDraftText = revisedDraftText,
            revisionBriefReport = revisionBriefReport,
            findings = findings,
        )

        val verdict = when {
            findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
            findings.any { it.severity == "WARNING" } -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }

        return NovelizationRevisedDraftIntakeReport(
            verdict = verdict,
            originalDraftFile = originalDraftFile,
            revisedDraftFile = revisedDraftFile,
            revisionBriefReportFile = revisionBriefReportFile,
            declaredHandlingFile = declaredHandlingFile,
            revisionBriefId = revisionBriefReport.briefId,
            findingCount = findings.size,
            findings = findings,
            evidence = NovelizationRevisedDraftIntakeEvidence(
                revisionBriefVerdict = revisionBriefReport.verdict,
                revisionRequired = revisionBriefReport.revisionRequired,
                originalCharacters = originalDraftText.length,
                revisedCharacters = revisedDraftText.length,
                unchangedAfterNormalization = unchangedAfterNormalization,
                mandatoryChecklistCount = revisionBriefReport.mandatoryChecklist.count { it.isNotBlank() },
                requiredFixCount = revisionBriefReport.requiredFixes.count { it.itemId.isNotBlank() },
                declaredChecklistCount = declaredHandling.handledChecklist.count { it.checklistItem.isNotBlank() },
                declaredFixCount = declaredHandling.handledFixes.count { it.itemId.isNotBlank() },
                bannedResidualCount = bannedResidualCount,
                continuityAnchorCount = revisionBriefReport.continuityAnchors.count { it.isNotBlank() },
                continuityAnchorPreservedCount = preservedAnchorCount,
            ),
        )
    }

    private fun validateRevisionBriefVerdict(
        revisionBriefReport: NovelizationRevisionBriefReport,
        revisionBriefVerdict: String,
        findings: MutableList<NovelizationRevisedDraftIntakeFinding>,
    ) {
        when {
            revisionBriefVerdict.isBlank() -> findings.add(
                finding(
                    ruleId = "REVISION_BRIEF_VERDICT_MISSING",
                    category = "upstream",
                    severity = "MAJOR",
                    path = "$.revisionBriefReport.verdict",
                    message = "Linked revision brief report has no verdict.",
                    suggestedFix = "Regenerate the revision brief report before validating a revised draft.",
                ),
            )
            revisionBriefVerdict !in knownVerdicts -> findings.add(
                finding(
                    ruleId = "REVISION_BRIEF_VERDICT_UNKNOWN",
                    category = "upstream",
                    severity = "MAJOR",
                    path = "$.revisionBriefReport.verdict",
                    message = "Linked revision brief report has an unknown verdict.",
                    evidence = listOf(revisionBriefReport.verdict),
                    suggestedFix = "Use a report with verdict PASS, PASS_WITH_WARNINGS, or FAIL.",
                ),
            )
            revisionBriefVerdict == "FAIL" -> findings.add(
                finding(
                    ruleId = "REVISION_BRIEF_FAILED",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.revisionBriefReport.verdict",
                    message = "Revised draft intake cannot pass because linked revision brief failed.",
                    suggestedFix = "Fix the revision brief gate before submitting a revised draft.",
                ),
            )
        }
    }

    private fun validateDraftShape(
        originalDraftText: String,
        revisedDraftText: String,
        unchangedAfterNormalization: Boolean,
        findings: MutableList<NovelizationRevisedDraftIntakeFinding>,
    ) {
        if (originalDraftText.isBlank()) {
            findings.add(
                finding(
                    ruleId = "ORIGINAL_DRAFT_EMPTY",
                    category = "draft",
                    severity = "MAJOR",
                    path = "$.originalDraft",
                    message = "Original draft is empty, so the revised draft cannot be compared safely.",
                    suggestedFix = "Provide the original draft that produced the revision brief.",
                ),
            )
        }
        if (revisedDraftText.isBlank()) {
            findings.add(
                finding(
                    ruleId = "REVISED_DRAFT_EMPTY",
                    category = "draft",
                    severity = "CRITICAL",
                    path = "$.revisedDraft",
                    message = "Revised draft must not be empty.",
                    suggestedFix = "Provide the revised draft text before running revised draft intake.",
                ),
            )
        }
        if (unchangedAfterNormalization) {
            findings.add(
                finding(
                    ruleId = "REVISED_DRAFT_UNCHANGED",
                    category = "draft",
                    severity = "CRITICAL",
                    path = "$.revisedDraft",
                    message = "Revised draft is the same as the original draft after normalization.",
                    suggestedFix = "Submit an actual revision, not the unchanged original draft.",
                ),
            )
        }
    }

    private fun validateDeclaredHandling(
        revisionBriefReport: NovelizationRevisionBriefReport,
        declaredHandling: NovelizationRevisionHandlingDeclaration,
        declaredHandlingFile: String,
        findings: MutableList<NovelizationRevisedDraftIntakeFinding>,
    ) {
        if (declaredHandlingFile.isBlank() && hasRequiredHandling(revisionBriefReport) && declaredHandling.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "DECLARED_HANDLING_FILE_MISSING",
                    category = "declared_handling",
                    severity = "MAJOR",
                    path = "$.declaredHandling",
                    message = "Revision brief requires handling declarations, but no declared handling file was provided.",
                    suggestedFix = "Provide --declared-handling with checklist and required fix handling declarations.",
                ),
            )
        }
        if (declaredHandling.revisionBriefId.isNotBlank() &&
            revisionBriefReport.briefId.isNotBlank() &&
            declaredHandling.revisionBriefId != revisionBriefReport.briefId
        ) {
            findings.add(
                finding(
                    ruleId = "DECLARED_HANDLING_BRIEF_ID_MISMATCH",
                    category = "declared_handling",
                    severity = "MAJOR",
                    path = "$.declaredHandling.revisionBriefId",
                    message = "Declared handling points at a different revision brief id.",
                    evidence = listOf(declaredHandling.revisionBriefId, revisionBriefReport.briefId),
                    suggestedFix = "Regenerate the declared handling file for the linked revision brief report.",
                ),
            )
        }

        val declaredChecklist = declaredHandling.handledChecklist
            .filter { it.checklistItem.isNotBlank() }
            .associateBy { it.checklistItem.trim() }
        revisionBriefReport.mandatoryChecklist.filter { it.isNotBlank() }.forEachIndexed { index, checklistItem ->
            val declaration = declaredChecklist[checklistItem.trim()]
            when {
                declaration == null -> findings.add(
                    finding(
                        ruleId = "MANDATORY_CHECKLIST_HANDLING_MISSING",
                        category = "declared_handling",
                        severity = "MAJOR",
                        path = "$.declaredHandling.handledChecklist[$index]",
                        message = "Mandatory revision checklist item has no declared handling.",
                        evidence = listOf(checklistItem),
                        suggestedFix = "Declare how this mandatory checklist item was handled.",
                    ),
                )
                declaration.status.trim().uppercase() !in acceptableHandlingStatuses -> findings.add(
                    finding(
                        ruleId = "MANDATORY_CHECKLIST_HANDLING_STATUS_INVALID",
                        category = "declared_handling",
                        severity = "MAJOR",
                        path = "$.declaredHandling.handledChecklist[$index].status",
                        message = "Mandatory revision checklist item has an invalid handling status.",
                        evidence = listOf(checklistItem, declaration.status),
                        suggestedFix = "Use status HANDLED or NOT_APPLICABLE.",
                    ),
                )
            }
        }

        val declaredFixes = declaredHandling.handledFixes
            .filter { it.itemId.isNotBlank() }
            .associateBy { it.itemId.trim() }
        revisionBriefReport.requiredFixes.filter { it.itemId.isNotBlank() }.forEachIndexed { index, requiredFix ->
            val declaration = declaredFixes[requiredFix.itemId.trim()]
            when {
                declaration == null -> findings.add(
                    finding(
                        ruleId = "REQUIRED_FIX_HANDLING_MISSING",
                        category = "declared_handling",
                        severity = "MAJOR",
                        path = "$.declaredHandling.handledFixes[$index]",
                        message = "Required revision fix has no declared handling.",
                        evidence = listOf(requiredFix.itemId, requiredFix.fixType),
                        suggestedFix = "Declare how this required fix was handled.",
                    ),
                )
                declaration.status.trim().uppercase() !in acceptableHandlingStatuses -> findings.add(
                    finding(
                        ruleId = "REQUIRED_FIX_HANDLING_STATUS_INVALID",
                        category = "declared_handling",
                        severity = "MAJOR",
                        path = "$.declaredHandling.handledFixes[$index].status",
                        message = "Required revision fix has an invalid handling status.",
                        evidence = listOf(requiredFix.itemId, declaration.status),
                        suggestedFix = "Use status HANDLED or NOT_APPLICABLE.",
                    ),
                )
            }
        }
    }

    private fun validateBannedResiduals(
        revisedDraftText: String,
        findings: MutableList<NovelizationRevisedDraftIntakeFinding>,
    ): Int {
        var count = 0
        bannedStructures.forEach { banned ->
            val match = banned.pattern.find(revisedDraftText)
            if (match != null) {
                count += 1
                findings.add(
                    finding(
                        ruleId = "BANNED_PHRASE_RESIDUAL_${banned.id}",
                        category = "banned_phrase",
                        severity = "MAJOR",
                        path = "$.revisedDraft",
                        message = "Revised draft still contains a banned calibration phrase.",
                        evidence = listOf(match.value.take(160)),
                        suggestedFix = "Remove the banned phrasing and replace it with concrete action, pressure, cost, or visible evidence.",
                    ),
                )
            }
        }
        return count
    }

    private fun validateContinuityAnchors(
        originalDraftText: String,
        revisedDraftText: String,
        revisionBriefReport: NovelizationRevisionBriefReport,
        findings: MutableList<NovelizationRevisedDraftIntakeFinding>,
    ): Int {
        var preservedCount = 0
        revisionBriefReport.continuityAnchors.filter { it.isNotBlank() }.forEachIndexed { index, anchor ->
            val check = anchorPreservation(anchor, revisedDraftText)
            val damage = continuityDamage(
                anchor = anchor,
                revisedDraftText = revisedDraftText,
                doNotChangeHints = revisionBriefReport.doNotChange,
            )
            if (damage != null) {
                val evidence = mutableListOf("anchor=$anchor", damage.evidence)
                if (check.matchedTerms.isNotEmpty()) {
                    evidence.add("matchedTerms=${check.matchedTerms.joinToString(", ")}")
                }
                findings.add(
                    finding(
                        ruleId = "CONTINUITY_ANCHOR_DAMAGED",
                        category = "continuity",
                        severity = "MAJOR",
                        path = "$.revisionBriefReport.continuityAnchors[$index]",
                        message = "Continuity anchor appears to be contradicted by a polarity or do-not-change violation.",
                        evidence = evidence,
                        suggestedFix = "Restore the anchor meaning and remove the contradictory revision.",
                    ),
                )
            } else if (check.preserved) {
                preservedCount += 1
            } else {
                val originalCheck = anchorPreservation(anchor, originalDraftText)
                val evidence = mutableListOf("anchor=$anchor")
                if (check.matchedTerms.isNotEmpty()) {
                    evidence.add("matchedTerms=${check.matchedTerms.joinToString(", ")}")
                }
                if (originalCheck.preserved) {
                    evidence.add("anchorAppearedPreservedInOriginal=true")
                }
                findings.add(
                    finding(
                        ruleId = "CONTINUITY_ANCHOR_NOT_PRESERVED",
                        category = "continuity",
                        severity = "MAJOR",
                        path = "$.revisionBriefReport.continuityAnchors[$index]",
                        message = "Continuity anchor is not sufficiently represented in the revised draft.",
                        evidence = evidence,
                        suggestedFix = "Restore the anchor facts in the revised draft without changing their meaning.",
                    ),
                )
            }
        }
        return preservedCount
    }

    private fun hasRequiredHandling(revisionBriefReport: NovelizationRevisionBriefReport): Boolean =
        revisionBriefReport.mandatoryChecklist.any { it.isNotBlank() } ||
            revisionBriefReport.requiredFixes.any { it.itemId.isNotBlank() }

    private fun NovelizationRevisionHandlingDeclaration.isEmpty(): Boolean =
        revisionBriefId.isBlank() &&
            handledChecklist.none { it.checklistItem.isNotBlank() } &&
            handledFixes.none { it.itemId.isNotBlank() }

    private fun anchorPreservation(anchor: String, text: String): AnchorPreservation {
        val normalizedAnchor = normalizeForSearch(anchor)
        val normalizedText = normalizeForSearch(text)
        if (normalizedAnchor.isNotBlank() && normalizedText.contains(normalizedAnchor)) {
            return AnchorPreservation(preserved = true, terms = listOf(normalizedAnchor), matchedTerms = listOf(normalizedAnchor))
        }

        val terms = significantTerms(anchor)
        if (terms.isEmpty()) {
            return AnchorPreservation(preserved = true, terms = emptyList(), matchedTerms = emptyList())
        }
        val matched = terms.filter { normalizedText.contains(it) }
        val requiredMatches = if (terms.size <= 2) {
            terms.size
        } else {
            ceil(terms.size * 0.55).toInt().coerceAtLeast(2)
        }
        return AnchorPreservation(
            preserved = matched.size >= requiredMatches,
            terms = terms,
            matchedTerms = matched,
        )
    }

    private fun continuityDamage(
        anchor: String,
        revisedDraftText: String,
        doNotChangeHints: List<String>,
    ): ContinuityDamage? {
        val normalizedAnchor = normalizeForDamage(anchor)
        val normalizedRevised = normalizeForDamage(revisedDraftText)
        violatedDoNotChangeHint(normalizedRevised, doNotChangeHints)?.let { return it }

        val anchorHasPaidMeaning = paymentPositiveTerms.any { normalizedAnchor.contains(it) }
        if (!anchorHasPaidMeaning) {
            return null
        }
        splitSentences(revisedDraftText).forEach { sentence ->
            val normalizedSentence = normalizeForDamage(sentence)
            val hasPaymentContext = paymentContextTerms.any { normalizedSentence.contains(it) }
            if (hasPaymentContext && paymentNegativePatterns.any { it.containsMatchIn(normalizedSentence) }) {
                return ContinuityDamage("payment polarity reversed: ${sentence.trim().take(160)}")
            }
        }
        return null
    }

    private fun violatedDoNotChangeHint(
        normalizedRevised: String,
        doNotChangeHints: List<String>,
    ): ContinuityDamage? {
        doNotChangeHints.filter { it.isNotBlank() }.forEach { hint ->
            val normalizedHint = normalizeForDamage(hint)
            val forbiddenPhrase = normalizedHint
                .removePrefix("do not ")
                .removePrefix("dont ")
                .removePrefix("never ")
                .trim()
            if (forbiddenPhrase.length >= 12 && normalizedRevised.contains(forbiddenPhrase)) {
                return ContinuityDamage("doNotChange hint violated: $hint")
            }
            val hintWarnsUnpaid = normalizedHint.contains("unpaid") || normalizedHint.contains("not paid")
            if (hintWarnsUnpaid && paymentNegativePatterns.any { it.containsMatchIn(normalizedRevised) }) {
                return ContinuityDamage("doNotChange unpaid warning violated: $hint")
            }
            val hintWarnsReset = normalizedHint.contains("reset") && paymentContextTerms.any { normalizedHint.contains(it) }
            val revisedResetsPayment = Regex("""\breset\b.{0,50}\b(toll|paid|pay|payment|unpaid)\b""")
                .containsMatchIn(normalizedRevised)
            if (hintWarnsReset && revisedResetsPayment) {
                return ContinuityDamage("doNotChange reset warning violated: $hint")
            }
        }
        return null
    }

    private fun significantTerms(text: String): List<String> {
        val normalized = normalizeForSearch(text)
        val latinTerms = Regex("""[a-z0-9][a-z0-9'-]{2,}""")
            .findAll(normalized)
            .map { it.value }
            .filterNot { it in stopWords }
        val cjkTerms = Regex("""[\u4E00-\u9FFF]{2,}""")
            .findAll(text)
            .flatMap { match ->
                match.value.windowed(size = 2, step = 1).asSequence()
            }
        return (latinTerms + cjkTerms)
            .distinct()
            .take(24)
            .toList()
    }

    private fun normalizeForComparison(text: String): String =
        text.removePrefix("\uFEFF")
            .trim()
            .lowercase()
            .replace(Regex("""\s+"""), "")

    private fun normalizeForSearch(text: String): String =
        text.removePrefix("\uFEFF")
            .lowercase()
            .replace(Regex("""\s+"""), " ")
            .trim()

    private fun normalizeForDamage(text: String): String =
        text.removePrefix("\uFEFF")
            .lowercase()
            .replace(Regex("""[^\p{L}\p{N}\s]+"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

    private fun splitSentences(text: String): List<String> =
        text.split(Regex("""[.!?。！？]+"""))
            .map { it.trim() }
            .filter { it.isNotBlank() }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): NovelizationRevisedDraftIntakeFinding =
        NovelizationRevisedDraftIntakeFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            path = path,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
        )

    private data class BannedStructure(
        val id: String,
        val pattern: Regex,
    )

    private data class AnchorPreservation(
        val preserved: Boolean,
        val terms: List<String>,
        val matchedTerms: List<String>,
    )

    private data class ContinuityDamage(
        val evidence: String,
    )

    private companion object {
        val stopWords = setOf(
            "the",
            "and",
            "but",
            "for",
            "from",
            "that",
            "this",
            "with",
            "before",
            "after",
            "already",
        )
        val paymentPositiveTerms = listOf(
            "paid",
            "payment",
            "付款",
            "付過",
            "已付",
            "已付款",
            "繳過",
            "已繳",
        )
        val paymentContextTerms = listOf(
            "paid",
            "pay",
            "payment",
            "toll",
            "door toll",
            "unpaid",
            "付款",
            "付過",
            "已付",
            "已付款",
            "繳過",
            "已繳",
            "未付",
            "未付款",
            "沒付",
            "沒繳",
            "通行費",
            "門費",
        )
        val paymentNegativePatterns = listOf(
            Regex("""\b(not|never)\s+(?:yet\s+)?paid\b"""),
            Regex("""\bhad\s+not\s+paid\b"""),
            Regex("""\bwithout\s+paying\b"""),
            Regex("""\bunpaid\b"""),
            Regex("""\breset\b.{0,50}\b(toll|paid|pay|payment|unpaid)\b"""),
            Regex("""(未付款|未付|沒付款|沒付|未繳|沒繳|尚未付款|尚未付|未支付)"""),
            Regex("""(重置|歸零).{0,20}(付款|付費|通行費|門費|繳費)"""),
        )
    }
}
