package com.localnovel.engine.service

import com.localnovel.engine.model.NovelizationDraftIntakeReport
import com.localnovel.engine.model.NovelizationProseQualityAuditReport
import com.localnovel.engine.model.NovelizationProseQualityFinding
import com.localnovel.engine.model.NovelizationRevisionBriefEvidence
import com.localnovel.engine.model.NovelizationRevisionBriefFinding
import com.localnovel.engine.model.NovelizationRevisionBriefReport
import com.localnovel.engine.model.NovelizationRevisionContinuityAnchors
import com.localnovel.engine.model.NovelizationRevisionItem

class NovelizationRevisionBriefService {
    private val knownVerdicts = setOf("PASS", "PASS_WITH_WARNINGS", "FAIL")
    private val mandatoryChecklist = listOf(
        "ai_taste_revision",
        "dialogue_revision",
        "character_voice_revision",
        "scene_pressure_reinforcement",
        "banned_phrase_removal",
        "continuity_anchor_protection",
    )

    fun build(
        draftText: String,
        draftIntakeReport: NovelizationDraftIntakeReport,
        proseQualityReport: NovelizationProseQualityAuditReport,
        continuityAnchors: NovelizationRevisionContinuityAnchors,
        briefId: String = "",
        draftFile: String = "",
        draftIntakeReportFile: String = "",
        proseQualityAuditReportFile: String = "",
        continuityAnchorsFile: String = "",
    ): NovelizationRevisionBriefReport {
        val findings = mutableListOf<NovelizationRevisionBriefFinding>()
        val intakeVerdict = draftIntakeReport.verdict.trim().uppercase()
        val proseVerdict = proseQualityReport.verdict.trim().uppercase()

        validateUpstreamVerdict(
            verdict = intakeVerdict,
            originalVerdict = draftIntakeReport.verdict,
            path = "$.draftIntakeReport.verdict",
            source = "draft intake",
            failRuleId = "DRAFT_INTAKE_FAILED",
            unknownRuleId = "DRAFT_INTAKE_VERDICT_UNKNOWN",
            missingRuleId = "DRAFT_INTAKE_VERDICT_MISSING",
            findings = findings,
        )
        validateUpstreamVerdict(
            verdict = proseVerdict,
            originalVerdict = proseQualityReport.verdict,
            path = "$.proseQualityAuditReport.verdict",
            source = "prose quality audit",
            failRuleId = "",
            unknownRuleId = "PROSE_QUALITY_VERDICT_UNKNOWN",
            missingRuleId = "PROSE_QUALITY_VERDICT_MISSING",
            findings = findings,
            failIsBlocking = false,
        )

        if (briefId.isBlank()) {
            findings.add(
                finding(
                    ruleId = "BRIEF_ID_MISSING",
                    category = "structure",
                    severity = "WARNING",
                    path = "$.briefId",
                    message = "Revision brief should carry a stable briefId for review handoff.",
                    suggestedFix = "Pass --brief or provide a stable brief id.",
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
                    message = "Cannot build a revision brief for an empty draft.",
                    suggestedFix = "Provide the same non-empty draft used by draft intake and prose quality audit.",
                ),
            )
        }
        if (continuityAnchors.anchors.none { it.isNotBlank() }) {
            findings.add(
                finding(
                    ruleId = "CONTINUITY_ANCHORS_EMPTY",
                    category = "continuity",
                    severity = "CRITICAL",
                    path = "$.continuityAnchors.anchors",
                    message = "Revision brief must list continuity anchors that revision must not break.",
                    suggestedFix = "Provide a continuity anchors JSON file with at least one anchor.",
                ),
            )
        }

        val requiredFixes = proseQualityReport.findings
            .filter { it.severity == "CRITICAL" || it.severity == "MAJOR" || it.severity == "WARNING" }
            .mapIndexed { index, proseFinding -> proseFinding.toRevisionItem(index + 1) }

        val verdict = when {
            findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
            findings.any { it.severity == "WARNING" } -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }

        return NovelizationRevisionBriefReport(
            verdict = verdict,
            briefId = briefId,
            draftFile = draftFile,
            draftIntakeReportFile = draftIntakeReportFile,
            proseQualityAuditReportFile = proseQualityAuditReportFile,
            continuityAnchorsFile = continuityAnchorsFile,
            revisionRequired = requiredFixes.isNotEmpty() || proseVerdict == "FAIL" || proseVerdict == "PASS_WITH_WARNINGS",
            mandatoryChecklist = mandatoryChecklist,
            requiredFixes = requiredFixes,
            continuityAnchors = continuityAnchors.anchors.filter { it.isNotBlank() },
            doNotChange = continuityAnchors.doNotChange.filter { it.isNotBlank() },
            findingCount = findings.size,
            findings = findings,
            evidence = NovelizationRevisionBriefEvidence(
                draftIntakeVerdict = draftIntakeReport.verdict,
                proseQualityVerdict = proseQualityReport.verdict,
                draftCharacters = draftText.length,
                proseQualityFindingCount = proseQualityReport.findingCount,
                requiredFixCount = requiredFixes.size,
                continuityAnchorCount = continuityAnchors.anchors.count { it.isNotBlank() },
                doNotChangeCount = continuityAnchors.doNotChange.count { it.isNotBlank() },
            ),
        )
    }

    private fun validateUpstreamVerdict(
        verdict: String,
        originalVerdict: String,
        path: String,
        source: String,
        failRuleId: String,
        unknownRuleId: String,
        missingRuleId: String,
        findings: MutableList<NovelizationRevisionBriefFinding>,
        failIsBlocking: Boolean = true,
    ) {
        when {
            verdict.isBlank() -> findings.add(
                finding(
                    ruleId = missingRuleId,
                    category = "upstream",
                    severity = "MAJOR",
                    path = path,
                    message = "Linked $source report has no verdict.",
                    suggestedFix = "Regenerate the $source report before building the revision brief.",
                ),
            )
            verdict !in knownVerdicts -> findings.add(
                finding(
                    ruleId = unknownRuleId,
                    category = "upstream",
                    severity = "MAJOR",
                    path = path,
                    message = "Linked $source report has an unknown verdict.",
                    evidence = listOf(originalVerdict),
                    suggestedFix = "Use a report with verdict PASS, PASS_WITH_WARNINGS, or FAIL.",
                ),
            )
            failIsBlocking && verdict == "FAIL" -> findings.add(
                finding(
                    ruleId = failRuleId,
                    category = "upstream",
                    severity = "CRITICAL",
                    path = path,
                    message = "Revision brief cannot pass because linked $source failed.",
                    suggestedFix = "Fix upstream $source before building a revision brief.",
                ),
            )
        }
    }

    private fun NovelizationProseQualityFinding.toRevisionItem(index: Int): NovelizationRevisionItem =
        NovelizationRevisionItem(
            itemId = "revision-item-${index.toString().padStart(3, '0')}",
            fixType = fixTypeFor(this),
            sourceRuleId = ruleId,
            category = category,
            severity = severity,
            instruction = instructionFor(this),
            evidence = evidence,
            suggestedFix = suggestedFix,
        )

    private fun fixTypeFor(finding: NovelizationProseQualityFinding): String =
        when {
            finding.ruleId.startsWith("BANNED_CALIBRATION_STRUCTURE") -> "banned_phrase_removal"
            finding.ruleId == "DIALOGUE_EXPOSITION_DUMP" -> "dialogue_revision"
            finding.ruleId == "SCENE_PRESSURE_MISSING" -> "scene_pressure_reinforcement"
            finding.ruleId == "MECHANICAL_SENTENCE_RHYTHM" -> "rhythm_revision"
            finding.category == "anti_ai_prose" -> "ai_taste_revision"
            finding.category == "dialogue" -> "dialogue_revision"
            finding.category == "text_integrity" -> "text_integrity_repair"
            finding.category == "intake" -> "upstream_intake_review"
            else -> "prose_quality_revision"
        }

    private fun instructionFor(finding: NovelizationProseQualityFinding): String =
        when (fixTypeFor(finding)) {
            "banned_phrase_removal" -> "Remove the banned calibration phrasing and replace it with concrete action, pressure, cost, refusal, or visible evidence."
            "dialogue_revision" -> "Revise dialogue so each line follows speaker pressure, knowledge boundary, and subtext instead of explaining lore or theme."
            "scene_pressure_reinforcement" -> "Add visible scene pressure through cost, refusal, risk, procedure, obstacle, time pressure, or consequence."
            "rhythm_revision" -> "Vary sentence rhythm without adding ornamental filler; use action, sensory cue, compression, or consequence."
            "ai_taste_revision" -> "Replace generic AI-like abstraction with specific visible behavior, concrete stakes, object detail, or character choice."
            "text_integrity_repair" -> "Repair broken text before any literary revision."
            "upstream_intake_review" -> "Resolve upstream intake warnings before revising prose."
            else -> "Revise this passage according to the prose quality finding without changing continuity anchors."
        }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): NovelizationRevisionBriefFinding =
        NovelizationRevisionBriefFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            path = path,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
        )
}
