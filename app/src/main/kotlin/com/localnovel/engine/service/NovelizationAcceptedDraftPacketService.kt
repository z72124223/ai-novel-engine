package com.localnovel.engine.service

import com.localnovel.engine.model.NovelizationAcceptedDraftHandledFix
import com.localnovel.engine.model.NovelizationAcceptedDraftPacket
import com.localnovel.engine.model.NovelizationAcceptedDraftPacketEvidence
import com.localnovel.engine.model.NovelizationAcceptedDraftPacketFinding
import com.localnovel.engine.model.NovelizationAcceptedDraftSourceReports
import com.localnovel.engine.model.NovelizationRevisedDraftIntakeReport
import com.localnovel.engine.model.NovelizationRevisionBriefReport
import com.localnovel.engine.model.NovelizationRevisionContinuityAnchors
import java.security.MessageDigest

class NovelizationAcceptedDraftPacketService {
    private val knownVerdicts = setOf("PASS", "PASS_WITH_WARNINGS", "FAIL")
    private val nextAllowedDownstreamStep = "script_packet_draft"

    fun build(
        revisedDraftText: String,
        revisedDraftIntakeReport: NovelizationRevisedDraftIntakeReport,
        revisionBriefReport: NovelizationRevisionBriefReport,
        continuityAnchors: NovelizationRevisionContinuityAnchors,
        packetId: String = "",
        revisedDraftFile: String = "",
        revisedDraftIntakeReportFile: String = "",
        revisionBriefReportFile: String = "",
        continuityAnchorsFile: String = "",
    ): NovelizationAcceptedDraftPacket {
        val findings = mutableListOf<NovelizationAcceptedDraftPacketFinding>()
        val intakeVerdict = revisedDraftIntakeReport.verdict.trim().uppercase()
        val revisionBriefVerdict = revisionBriefReport.verdict.trim().uppercase()

        validateRequiredString(
            value = packetId,
            path = "$.packetId",
            ruleId = "PACKET_ID_MISSING",
            message = "Accepted draft packet should carry a stable packetId.",
            suggestedFix = "Pass --packet with a stable packet id.",
            severity = "WARNING",
            findings = findings,
        )
        validateRequiredString(
            value = revisedDraftText,
            path = "$.revisedDraft",
            ruleId = "REVISED_DRAFT_EMPTY",
            message = "Accepted draft packet cannot be built from an empty revised draft.",
            suggestedFix = "Provide the same revised draft that passed revised draft intake.",
            severity = "CRITICAL",
            findings = findings,
        )
        validateVerdict(
            verdict = intakeVerdict,
            originalVerdict = revisedDraftIntakeReport.verdict,
            source = "revised draft intake",
            path = "$.revisedDraftIntakeReport.verdict",
            failRuleId = "REVISED_DRAFT_INTAKE_NOT_PASS",
            unknownRuleId = "REVISED_DRAFT_INTAKE_VERDICT_UNKNOWN",
            missingRuleId = "REVISED_DRAFT_INTAKE_VERDICT_MISSING",
            findings = findings,
            passOnly = true,
        )
        validateVerdict(
            verdict = revisionBriefVerdict,
            originalVerdict = revisionBriefReport.verdict,
            source = "revision brief",
            path = "$.revisionBriefReport.verdict",
            failRuleId = "REVISION_BRIEF_FAILED",
            unknownRuleId = "REVISION_BRIEF_VERDICT_UNKNOWN",
            missingRuleId = "REVISION_BRIEF_VERDICT_MISSING",
            findings = findings,
            passOnly = false,
        )

        val protectedAnchors = protectedContinuityAnchors(revisionBriefReport, continuityAnchors)
        if (protectedAnchors.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "PROTECTED_CONTINUITY_ANCHORS_EMPTY",
                    category = "continuity",
                    severity = "CRITICAL",
                    path = "$.protectedContinuityAnchors",
                    message = "Accepted draft packet must carry protected continuity anchors for downstream conversion.",
                    suggestedFix = "Provide a continuity anchors file or revision brief report with at least one anchor.",
                ),
            )
        }

        if (continuityAnchors.anchors.any { it.isNotBlank() } &&
            revisionBriefReport.continuityAnchors.any { it.isNotBlank() } &&
            continuityAnchors.anchors.filter { it.isNotBlank() }.toSet() !=
            revisionBriefReport.continuityAnchors.filter { it.isNotBlank() }.toSet()
        ) {
            findings.add(
                finding(
                    ruleId = "CONTINUITY_ANCHOR_SOURCE_MISMATCH",
                    category = "continuity",
                    severity = "WARNING",
                    path = "$.sourceReports.continuityAnchorsFile",
                    message = "Continuity anchors file and revision brief report do not contain the same anchor set.",
                    suggestedFix = "Review both sources before downstream conversion.",
                ),
            )
        }

        val residualRisks = residualRisks(revisionBriefReport, revisedDraftIntakeReport)
        val verdict = when {
            findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
            findings.any { it.severity == "WARNING" } -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }

        return NovelizationAcceptedDraftPacket(
            verdict = verdict,
            packetId = packetId,
            revisedDraftFile = revisedDraftFile,
            revisedDraftContentSha256 = sha256(revisedDraftText),
            sourceReports = NovelizationAcceptedDraftSourceReports(
                revisionBriefReportFile = revisionBriefReportFile,
                revisionBriefVerdict = revisionBriefReport.verdict,
                revisedDraftIntakeReportFile = revisedDraftIntakeReportFile,
                revisedDraftIntakeVerdict = revisedDraftIntakeReport.verdict,
                continuityAnchorsFile = continuityAnchorsFile,
                continuityAnchorFileId = continuityAnchors.anchorFileId,
            ),
            requiredFixesHandledSummary = revisionBriefReport.requiredFixes
                .filter { it.itemId.isNotBlank() }
                .map {
                    NovelizationAcceptedDraftHandledFix(
                        itemId = it.itemId,
                        fixType = it.fixType,
                        sourceRuleId = it.sourceRuleId,
                        severity = it.severity,
                    )
                },
            protectedContinuityAnchors = protectedAnchors,
            doNotChange = (revisionBriefReport.doNotChange + continuityAnchors.doNotChange)
                .filter { it.isNotBlank() }
                .distinct(),
            remainingResidualRisks = residualRisks,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            findingCount = findings.size,
            findings = findings,
            evidence = NovelizationAcceptedDraftPacketEvidence(
                revisedDraftCharacters = revisedDraftText.length,
                requiredFixCount = revisionBriefReport.requiredFixes.count { it.itemId.isNotBlank() },
                protectedContinuityAnchorCount = protectedAnchors.size,
                doNotChangeCount = (revisionBriefReport.doNotChange + continuityAnchors.doNotChange)
                    .count { it.isNotBlank() },
                revisedDraftIntakeFindingCount = revisedDraftIntakeReport.findingCount,
                revisionBriefFindingCount = revisionBriefReport.findingCount,
                residualRiskCount = residualRisks.size,
            ),
        )
    }

    private fun validateVerdict(
        verdict: String,
        originalVerdict: String,
        source: String,
        path: String,
        failRuleId: String,
        unknownRuleId: String,
        missingRuleId: String,
        findings: MutableList<NovelizationAcceptedDraftPacketFinding>,
        passOnly: Boolean,
    ) {
        when {
            verdict.isBlank() -> findings.add(
                finding(
                    ruleId = missingRuleId,
                    category = "upstream",
                    severity = "MAJOR",
                    path = path,
                    message = "Linked $source report has no verdict.",
                    suggestedFix = "Regenerate the $source report before building an accepted draft packet.",
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
            passOnly && verdict != "PASS" -> findings.add(
                finding(
                    ruleId = failRuleId,
                    category = "upstream",
                    severity = "CRITICAL",
                    path = path,
                    message = "Accepted draft packet can PASS only after revised draft intake PASS.",
                    evidence = listOf(originalVerdict),
                    suggestedFix = "Fix and rerun revised draft intake before creating the handoff packet.",
                ),
            )
            !passOnly && verdict == "FAIL" -> findings.add(
                finding(
                    ruleId = failRuleId,
                    category = "upstream",
                    severity = "CRITICAL",
                    path = path,
                    message = "Accepted draft packet cannot pass because linked $source failed.",
                    suggestedFix = "Fix the upstream $source before building the handoff packet.",
                ),
            )
        }
    }

    private fun validateRequiredString(
        value: String,
        path: String,
        ruleId: String,
        message: String,
        suggestedFix: String,
        severity: String,
        findings: MutableList<NovelizationAcceptedDraftPacketFinding>,
    ) {
        if (value.isBlank()) {
            findings.add(
                finding(
                    ruleId = ruleId,
                    category = "structure",
                    severity = severity,
                    path = path,
                    message = message,
                    suggestedFix = suggestedFix,
                ),
            )
        }
    }

    private fun protectedContinuityAnchors(
        revisionBriefReport: NovelizationRevisionBriefReport,
        continuityAnchors: NovelizationRevisionContinuityAnchors,
    ): List<String> =
        (revisionBriefReport.continuityAnchors + continuityAnchors.anchors)
            .filter { it.isNotBlank() }
            .distinct()

    private fun residualRisks(
        revisionBriefReport: NovelizationRevisionBriefReport,
        revisedDraftIntakeReport: NovelizationRevisedDraftIntakeReport,
    ): List<String> {
        val risks = mutableListOf(
            "Downstream script/storyboard/video gates have not yet validated transformation readiness.",
        )
        revisionBriefReport.findings
            .filter { it.severity == "WARNING" }
            .mapTo(risks) { "Revision brief warning remains review-relevant: ${it.ruleId}" }
        revisedDraftIntakeReport.findings
            .filter { it.severity == "WARNING" }
            .mapTo(risks) { "Revised draft intake warning remains review-relevant: ${it.ruleId}" }
        if (revisionBriefReport.doNotChange.any { it.isNotBlank() }) {
            risks.add("Protected do-not-change hints must be preserved in downstream adaptation.")
        }
        return risks.distinct()
    }

    private fun sha256(text: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): NovelizationAcceptedDraftPacketFinding =
        NovelizationAcceptedDraftPacketFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            path = path,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
        )
}
