package com.localnovel.engine.service

import com.localnovel.engine.model.NovelizationDraftIntakeReport
import com.localnovel.engine.model.NovelizationProseQualityAuditReport
import com.localnovel.engine.model.NovelizationProseQualityEvidence
import com.localnovel.engine.model.NovelizationProseQualityFinding

class NovelizationProseQualityAuditService {
    private val knownVerdicts = setOf("PASS", "PASS_WITH_WARNINGS", "FAIL")
    private val sentenceBoundaryRegex = Regex("""[.!?。！？]+""")
    private val hardBannedStructures = listOf(
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
        BannedStructure("CALIBRATION_EN_NOT_ONLY", Regex("""(?i)\bnot\s+.+?\s*,?\s+only\s+.+""")),
    )
    private val genericAbstractions = listOf(
        "ancient secrets",
        "destiny",
        "fate",
        "profound",
        "indescribable",
        "unspeakable",
        "inner turmoil",
        "meaning of",
        "mysterious force",
        "truth of the world",
        "unknown power",
        "命運",
        "宿命",
        "真相",
        "世界的真相",
        "不可名狀",
        "無法言喻",
        "古老秘密",
        "古老的秘密",
        "神秘力量",
        "未知力量",
        "深刻意義",
        "內心掙扎",
    )
    private val expositionDialogueMarkers = listOf(
        "as you know",
        "please explain",
        "whole system",
        "ancient secrets",
        "i feel a profound",
        "truth of the world",
        "fate and survival",
        "meaning of",
        "lore",
        "請解釋",
        "解釋一下",
        "整個系統",
        "這個系統",
        "古老秘密",
        "古老的秘密",
        "世界的真相",
        "命運",
        "宿命",
        "不可名狀",
        "神秘力量",
        "設定",
        "背景",
        "真正的意義",
    )
    private val pressureMarkers = listOf(
        "blocked",
        "cannot",
        "cost",
        "debt",
        "door",
        "guard",
        "line",
        "mark",
        "must",
        "need",
        "paid",
        "pressure",
        "refuse",
        "risk",
        "toll",
        "wait",
        "門",
        "門口",
        "門檻",
        "關卡",
        "守衛",
        "守門",
        "不能",
        "不准",
        "代價",
        "成本",
        "債",
        "債務",
        "標記",
        "印記",
        "通行",
        "排隊",
        "隊伍",
        "必須",
        "需要",
        "付過",
        "付錢",
        "拒絕",
        "風險",
        "危險",
        "等待",
        "阻擋",
    )

    fun audit(
        draftText: String,
        draftIntakeReport: NovelizationDraftIntakeReport,
        draftFile: String = "",
        draftIntakeReportFile: String = "",
    ): NovelizationProseQualityAuditReport {
        val findings = mutableListOf<NovelizationProseQualityFinding>()
        val normalizedIntakeVerdict = draftIntakeReport.verdict.trim().uppercase()
        val normalizedDraft = draftText.lowercase()
        val dialogueLines = extractDialogueLines(draftText)

        when {
            normalizedIntakeVerdict.isBlank() -> findings.add(
                finding(
                    ruleId = "DRAFT_INTAKE_VERDICT_MISSING",
                    category = "intake",
                    severity = "MAJOR",
                    path = "$.draftIntakeReport.verdict",
                    message = "Draft intake report has no verdict.",
                    suggestedFix = "Run validate-novelization-draft-intake before prose quality audit.",
                ),
            )
            normalizedIntakeVerdict !in knownVerdicts -> findings.add(
                finding(
                    ruleId = "DRAFT_INTAKE_VERDICT_UNKNOWN",
                    category = "intake",
                    severity = "MAJOR",
                    path = "$.draftIntakeReport.verdict",
                    message = "Draft intake report has an unknown verdict.",
                    evidence = listOf(draftIntakeReport.verdict),
                    suggestedFix = "Use a draft intake report with verdict PASS, PASS_WITH_WARNINGS, or FAIL.",
                ),
            )
            normalizedIntakeVerdict == "FAIL" -> findings.add(
                finding(
                    ruleId = "DRAFT_INTAKE_FAILED",
                    category = "intake",
                    severity = "CRITICAL",
                    path = "$.draftIntakeReport",
                    message = "Prose quality audit cannot pass because draft intake failed.",
                    evidence = listOf("findings=${draftIntakeReport.findingCount}"),
                    suggestedFix = "Fix draft intake before running prose quality review.",
                ),
            )
            normalizedIntakeVerdict == "PASS_WITH_WARNINGS" -> findings.add(
                finding(
                    ruleId = "DRAFT_INTAKE_WARNINGS_PRESENT",
                    category = "intake",
                    severity = "WARNING",
                    path = "$.draftIntakeReport",
                    message = "Draft intake passed with warnings; prose review may be working from imperfect inputs.",
                    evidence = listOf("findings=${draftIntakeReport.findingCount}"),
                    suggestedFix = "Review upstream intake warnings before accepting this draft.",
                ),
            )
        }

        if (draftText.isBlank()) {
            findings.add(
                finding(
                    ruleId = "DRAFT_TEXT_EMPTY",
                    category = "draft",
                    severity = "CRITICAL",
                    path = "$.draft",
                    message = "Cannot audit empty draft text.",
                    suggestedFix = "Provide a non-empty draft file.",
                ),
            )
        }

        val replacementCharacterCount = draftText.count { it == '\uFFFD' }
        if (replacementCharacterCount > 0) {
            findings.add(
                finding(
                    ruleId = "DRAFT_CONTAINS_REPLACEMENT_CHARACTER",
                    category = "text_integrity",
                    severity = "MAJOR",
                    path = "$.draft",
                    message = "Draft contains Unicode replacement characters, indicating broken source text or encoding loss.",
                    evidence = listOf("replacementCharacters=$replacementCharacterCount"),
                    suggestedFix = "Repair broken glyphs before prose quality review.",
                ),
            )
        }

        val bannedHits = scanBannedCalibration(draftText, findings)
        val abstractionHits = genericAbstractions.filter { normalizedDraft.contains(it) }
        if (abstractionHits.size >= 2) {
            findings.add(
                finding(
                    ruleId = "GENERIC_ABSTRACTION_CLUSTER",
                    category = "anti_ai_prose",
                    severity = "MAJOR",
                    path = "$.draft",
                    message = "Draft clusters generic abstract language often associated with AI-like prose.",
                    evidence = abstractionHits.distinct().take(8),
                    suggestedFix = "Replace abstract labels with visible action, cost, procedure, sensory detail, or character pressure.",
                ),
            )
        }

        if (draftText.isNotBlank() && pressureMarkers.none { normalizedDraft.contains(it) }) {
            findings.add(
                finding(
                    ruleId = "SCENE_PRESSURE_MISSING",
                    category = "scene_pressure",
                    severity = "MAJOR",
                    path = "$.draft",
                    message = "Draft does not expose concrete scene pressure through cost, refusal, risk, procedure, or obstacle cues.",
                    suggestedFix = "Add visible pressure that forces characters to choose, refuse, pay, wait, risk, or act.",
                ),
            )
        }

        if (dialogueLines.isEmpty() && draftText.isNotBlank()) {
            findings.add(
                finding(
                    ruleId = "DIALOGUE_LINES_EMPTY",
                    category = "dialogue",
                    severity = "WARNING",
                    path = "$.draft",
                    message = "Draft has no detected dialogue lines for voice or subtext review.",
                    suggestedFix = "If this scene should contain speech, add dialogue shaped by character pressure and knowledge boundaries.",
                ),
            )
        }
        val expositionHits = dialogueLines.filter { line ->
            val lower = line.lowercase()
            expositionDialogueMarkers.any { lower.contains(it) }
        }
        if (expositionHits.isNotEmpty()) {
            findings.add(
                finding(
                    ruleId = "DIALOGUE_EXPOSITION_DUMP",
                    category = "dialogue",
                    severity = "MAJOR",
                    path = "$.draft.dialogue",
                    message = "Dialogue line appears to explain lore, theme, or system information instead of speaking from pressure.",
                    evidence = expositionHits.map { it.trimForEvidence() }.take(5),
                    suggestedFix = "Turn exposition into a concrete demand, refusal, question, procedure, cost, or omission.",
                ),
            )
        }

        val repeatedSentenceStart = repeatedSentenceStart(draftText)
        if (repeatedSentenceStart != null) {
            findings.add(
                finding(
                    ruleId = "MECHANICAL_SENTENCE_RHYTHM",
                    category = "rhythm",
                    severity = "WARNING",
                    path = "$.draft",
                    message = "Draft repeats the same sentence opening enough times to feel mechanically patterned.",
                    evidence = listOf(repeatedSentenceStart),
                    suggestedFix = "Vary sentence openings with action, sensory cue, dialogue, consequence, or compression.",
                ),
            )
        }

        val verdict = when {
            findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
            findings.any { it.severity == "WARNING" } -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }

        return NovelizationProseQualityAuditReport(
            verdict = verdict,
            draftFile = draftFile,
            draftIntakeReportFile = draftIntakeReportFile,
            findingCount = findings.size,
            findings = findings,
            evidence = NovelizationProseQualityEvidence(
                draftIntakeVerdict = draftIntakeReport.verdict,
                draftCharacters = draftText.length,
                draftLines = draftText.lineSequence().count(),
                dialogueLineCount = dialogueLines.size,
                sentenceCount = sentenceCount(draftText),
                bannedCalibrationHitCount = bannedHits,
                genericAbstractionHitCount = abstractionHits.size,
                expositionDialogueHitCount = expositionHits.size,
                replacementCharacterCount = replacementCharacterCount,
            ),
        )
    }

    private fun scanBannedCalibration(
        draftText: String,
        findings: MutableList<NovelizationProseQualityFinding>,
    ): Int {
        var hits = 0
        val lines = draftText.lines()
        lines.forEachIndexed { index, line ->
            hardBannedStructures.forEach { banned ->
                if (banned.regex.containsMatchIn(line)) {
                    hits += 1
                    findings.add(
                        finding(
                            ruleId = "BANNED_CALIBRATION_STRUCTURE_${banned.id}",
                            category = "anti_ai_prose",
                            severity = "CRITICAL",
                            path = "$.draft.lines[$index]",
                            message = "Draft contains a hard-banned calibration structure.",
                            evidence = listOf(line.trimForEvidence()),
                            suggestedFix = "Replace the slogan with concrete action, pressure, cost, refusal, procedure, or visible evidence.",
                        ),
                    )
                }
            }
        }
        return hits
    }

    private fun extractDialogueLines(draftText: String): List<String> =
        draftText.lines().filter { line ->
            line.contains('"') ||
                line.contains('\u201C') ||
                line.contains('\u201D') ||
                line.contains('\u300C') ||
                line.contains('\u300D')
        }

    private fun repeatedSentenceStart(draftText: String): String? {
        val starts = draftText
            .split(sentenceBoundaryRegex)
            .mapNotNull { sentence -> sentenceStartKey(sentence) }
        if (starts.size < 4) return null
        val repeated = starts.groupingBy { it }.eachCount().filterValues { it >= 4 }
        return repeated.maxByOrNull { it.value }?.let { "${it.key}=${it.value}" }
    }

    private fun sentenceCount(draftText: String): Int =
        draftText.split(sentenceBoundaryRegex).count { it.isNotBlank() }

    private fun sentenceStartKey(sentence: String): String? {
        val trimmed = sentence.trim().trimStart { !it.isLetterOrDigit() && !it.isCjk() }
        if (trimmed.isBlank()) return null
        val first = trimmed.first()
        return if (first.isCjk()) {
            first.toString()
        } else {
            trimmed
                .split(Regex("""\s+"""))
                .firstOrNull()
                ?.lowercase()
                ?.trim { !it.isLetterOrDigit() }
                ?.takeIf { it.length >= 2 }
        }
    }

    private fun Char.isCjk(): Boolean =
        this in '\u4E00'..'\u9FFF' ||
            this in '\u3400'..'\u4DBF' ||
            this in '\uF900'..'\uFAFF'

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): NovelizationProseQualityFinding =
        NovelizationProseQualityFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            path = path,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
        )

    private fun String.trimForEvidence(maxLength: Int = 160): String {
        val normalized = trim().replace(Regex("""\s+"""), " ")
        return if (normalized.length <= maxLength) normalized else normalized.take(maxLength - 3) + "..."
    }

    private data class BannedStructure(
        val id: String,
        val regex: Regex,
    )
}
