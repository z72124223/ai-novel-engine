package com.localnovel.engine.service

import com.localnovel.engine.model.ChapterContinuityAnchorFile
import com.localnovel.engine.model.ChapterContinuityAuditEvidence
import com.localnovel.engine.model.ChapterContinuityAuditReport
import com.localnovel.engine.model.ChapterContinuityCheck
import com.localnovel.engine.model.ChapterContinuityFinding

class ChapterContinuityAuditService {
    fun audit(
        previousText: String,
        draftText: String,
        anchors: ChapterContinuityAnchorFile,
        previousFile: String = "",
        draftFile: String = "",
        anchorFile: String = "",
    ): ChapterContinuityAuditReport {
        val findings = mutableListOf<ChapterContinuityFinding>()

        if (previousText.isBlank()) {
            findings.add(
                finding(
                    ruleId = "PREVIOUS_TEXT_EMPTY",
                    category = "input",
                    severity = "CRITICAL",
                    message = "Previous accepted chapter text is empty.",
                    suggestedFix = "Provide the accepted previous chapter before auditing the next draft.",
                ),
            )
        }
        if (draftText.isBlank()) {
            findings.add(
                finding(
                    ruleId = "DRAFT_TEXT_EMPTY",
                    category = "input",
                    severity = "CRITICAL",
                    message = "Draft chapter text is empty.",
                    suggestedFix = "Provide a draft chapter before continuity review.",
                ),
            )
        }
        if (
            anchors.mustAppearInPrevious.isEmpty() &&
            anchors.mustAppearInDraft.isEmpty() &&
            anchors.mustNotAppearInDraft.isEmpty()
        ) {
            findings.add(
                finding(
                    ruleId = "ANCHOR_CHECKS_EMPTY",
                    category = "anchors",
                    severity = "MAJOR",
                    message = "Continuity anchor file contains no checks.",
                    suggestedFix = "Add explicit previous-chapter anchors and draft regression checks before accepting generated prose.",
                ),
            )
        }

        anchors.mustAppearInPrevious.forEach { check ->
            validateConfiguredCheck(check, "mustAppearInPrevious", findings)
            if (check.text.isNotBlank() && !previousText.contains(check.text)) {
                findings.add(
                    finding(
                        ruleId = "ANCHOR_MISSING_FROM_PREVIOUS",
                        category = "anchors",
                        severity = check.normalizedSeverity(),
                        checkId = check.checkId,
                        message = "A declared previous-chapter anchor was not found in the previous accepted chapter.",
                        evidence = listOf(check.text),
                        suggestedFix = check.suggestedFix.ifBlank {
                            "Fix the anchor text or point the audit at the correct accepted previous chapter."
                        },
                    ),
                )
            }
        }

        anchors.mustAppearInDraft.forEach { check ->
            validateConfiguredCheck(check, "mustAppearInDraft", findings)
            if (check.text.isNotBlank() && !draftText.contains(check.text)) {
                findings.add(
                    finding(
                        ruleId = "REQUIRED_DRAFT_ANCHOR_MISSING",
                        category = "continuity",
                        severity = check.normalizedSeverity(),
                        checkId = check.checkId,
                        message = "The draft does not contain a required continuity or progression anchor.",
                        evidence = listOf(check.text),
                        suggestedFix = check.suggestedFix.ifBlank {
                            "Revise the draft so it acknowledges or advances this required continuity anchor."
                        },
                    ),
                )
            }
        }

        anchors.mustNotAppearInDraft.forEach { check ->
            validateConfiguredCheck(check, "mustNotAppearInDraft", findings)
            if (check.text.isNotBlank() && draftText.contains(check.text)) {
                findings.add(
                    finding(
                        ruleId = "DRAFT_CONTAINS_FORBIDDEN_REGRESSION",
                        category = "continuity",
                        severity = check.normalizedSeverity(),
                        checkId = check.checkId,
                        message = "The draft contains a forbidden regression phrase or repeated completed beat.",
                        evidence = listOf(check.text),
                        suggestedFix = check.suggestedFix.ifBlank {
                            "Remove the repeated setup beat or rewrite it as a memory/callback that advances the current chapter."
                        },
                    ),
                )
            }
        }

        val replacementCharacterCount = draftText.count { it == '\uFFFD' }
        if (replacementCharacterCount > 0) {
            findings.add(
                finding(
                    ruleId = "DRAFT_CONTAINS_REPLACEMENT_CHARACTER",
                    category = "text_integrity",
                    severity = "MAJOR",
                    message = "The draft contains Unicode replacement characters, which usually means broken source text or encoding loss.",
                    evidence = listOf("replacementCharacters=$replacementCharacterCount"),
                    suggestedFix = "Repair the broken glyphs from the source before treating the draft as reviewable prose.",
                ),
            )
        }

        val verdict = when {
            findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
            findings.any { it.severity == "WARNING" } -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }

        return ChapterContinuityAuditReport(
            verdict = verdict,
            previousFile = previousFile,
            draftFile = draftFile,
            anchorFile = anchorFile,
            auditId = anchors.auditId,
            findingCount = findings.size,
            findings = findings,
            evidence = ChapterContinuityAuditEvidence(
                previousCharacters = previousText.length,
                draftCharacters = draftText.length,
                mustAppearInPreviousCount = anchors.mustAppearInPrevious.size,
                mustAppearInDraftCount = anchors.mustAppearInDraft.size,
                mustNotAppearInDraftCount = anchors.mustNotAppearInDraft.size,
                replacementCharacterCount = replacementCharacterCount,
            ),
        )
    }

    private fun validateConfiguredCheck(
        check: ChapterContinuityCheck,
        section: String,
        findings: MutableList<ChapterContinuityFinding>,
    ) {
        if (check.checkId.isBlank()) {
            findings.add(
                finding(
                    ruleId = "CHECK_ID_MISSING",
                    category = "anchors",
                    severity = "WARNING",
                    message = "A continuity check is missing checkId.",
                    suggestedFix = "Give every check a stable checkId for review reports.",
                ),
            )
        }
        if (check.text.isBlank()) {
            findings.add(
                finding(
                    ruleId = "CHECK_TEXT_MISSING",
                    category = "anchors",
                    severity = "MAJOR",
                    checkId = check.checkId,
                    message = "A continuity check has blank text.",
                    evidence = listOf(section),
                    suggestedFix = "Fill the exact text that the audit should search for.",
                ),
            )
        }
    }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        message: String,
        checkId: String = "",
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): ChapterContinuityFinding =
        ChapterContinuityFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            checkId = checkId,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
        )

    private fun ChapterContinuityCheck.normalizedSeverity(): String =
        when (severity.trim().uppercase()) {
            "CRITICAL", "MAJOR", "WARNING", "INFO" -> severity.trim().uppercase()
            else -> "MAJOR"
        }
}
