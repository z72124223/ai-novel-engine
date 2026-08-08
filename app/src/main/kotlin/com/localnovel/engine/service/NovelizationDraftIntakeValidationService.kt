package com.localnovel.engine.service

import com.localnovel.engine.model.NovelizationDraftIntakeEvidence
import com.localnovel.engine.model.NovelizationDraftIntakeFinding
import com.localnovel.engine.model.NovelizationDraftIntakeReport
import com.localnovel.engine.model.NovelizationWriterBriefValidationReport

class NovelizationDraftIntakeValidationService {
    private val knownSourceTypes = setOf("codex", "gpt", "quarterfull", "manual", "mixed", "other")
    private val knownVerdicts = setOf("PASS", "PASS_WITH_WARNINGS", "FAIL")

    fun validate(
        draftText: String,
        writerBriefReport: NovelizationWriterBriefValidationReport,
        draftFile: String = "",
        writerBriefReportFile: String = "",
        sourceType: String = "",
        sourceLabel: String = "",
    ): NovelizationDraftIntakeReport {
        val findings = mutableListOf<NovelizationDraftIntakeFinding>()
        val normalizedSource = sourceType.trim().lowercase()
        val writerBriefVerdict = writerBriefReport.verdict.trim().uppercase()

        when {
            normalizedSource.isBlank() -> findings.add(
                finding(
                    ruleId = "SOURCE_TYPE_MISSING",
                    category = "source",
                    severity = "MAJOR",
                    path = "$.sourceType",
                    message = "Draft intake must identify where the draft came from.",
                    suggestedFix = "Use one of codex, gpt, quarterfull, manual, mixed, or other.",
                ),
            )
            normalizedSource !in knownSourceTypes -> findings.add(
                finding(
                    ruleId = "SOURCE_TYPE_UNKNOWN",
                    category = "source",
                    severity = "WARNING",
                    path = "$.sourceType",
                    message = "Draft source type is not one of the known intake categories.",
                    evidence = listOf(sourceType),
                    suggestedFix = "Prefer codex, gpt, quarterfull, manual, mixed, or other.",
                ),
            )
        }

        if (sourceLabel.isBlank()) {
            findings.add(
                finding(
                    ruleId = "SOURCE_LABEL_MISSING",
                    category = "source",
                    severity = "WARNING",
                    path = "$.sourceLabel",
                    message = "Draft intake should carry a source label for later review.",
                    suggestedFix = "Add a short label such as QuarterFull item id, chat title, or manual draft version.",
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
                    message = "Draft intake cannot continue with an empty draft.",
                    suggestedFix = "Provide the draft text file before running intake validation.",
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
                    suggestedFix = "Repair broken glyphs before continuity or prose quality review.",
                ),
            )
        }

        when {
            writerBriefVerdict.isBlank() -> findings.add(
                finding(
                    ruleId = "WRITER_BRIEF_VERDICT_MISSING",
                    category = "writer_brief",
                    severity = "MAJOR",
                    path = "$.writerBriefReport.verdict",
                    message = "Writer brief validation report has no verdict.",
                    suggestedFix = "Run validate-novelization-writer-brief before draft intake.",
                ),
            )
            writerBriefVerdict !in knownVerdicts -> findings.add(
                finding(
                    ruleId = "WRITER_BRIEF_VERDICT_UNKNOWN",
                    category = "writer_brief",
                    severity = "MAJOR",
                    path = "$.writerBriefReport.verdict",
                    message = "Writer brief validation report has an unknown verdict.",
                    evidence = listOf(writerBriefReport.verdict),
                    suggestedFix = "Use a writer brief report with verdict PASS, PASS_WITH_WARNINGS, or FAIL.",
                ),
            )
            writerBriefVerdict == "FAIL" -> findings.add(
                finding(
                    ruleId = "WRITER_BRIEF_FAILED",
                    category = "writer_brief",
                    severity = "CRITICAL",
                    path = "$.writerBriefReport",
                    message = "Draft intake cannot pass because the linked writer brief failed.",
                    evidence = listOf("briefId=${writerBriefReport.briefId}", "findings=${writerBriefReport.findingCount}"),
                    suggestedFix = "Fix the writer brief before accepting any draft into review.",
                ),
            )
        }

        val verdict = when {
            findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
            findings.any { it.severity == "WARNING" } -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }

        return NovelizationDraftIntakeReport(
            verdict = verdict,
            draftFile = draftFile,
            writerBriefReportFile = writerBriefReportFile,
            sourceType = sourceType,
            sourceLabel = sourceLabel,
            findingCount = findings.size,
            findings = findings,
            evidence = NovelizationDraftIntakeEvidence(
                writerBriefVerdict = writerBriefReport.verdict,
                draftCharacters = draftText.length,
                draftLines = draftText.lineSequence().count(),
                replacementCharacterCount = replacementCharacterCount,
            ),
        )
    }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): NovelizationDraftIntakeFinding =
        NovelizationDraftIntakeFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            path = path,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
        )
}
