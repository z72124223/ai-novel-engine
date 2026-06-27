package com.localnovel.engine.service

import com.localnovel.engine.model.NovelizationAcceptedDraftPacket
import com.localnovel.engine.model.NovelizationScriptPacketPreflightEvidence
import com.localnovel.engine.model.NovelizationScriptPacketPreflightFinding
import com.localnovel.engine.model.NovelizationScriptPacketPreflightReport

class NovelizationScriptPacketPreflightService {
    private val requiredDownstreamStep = "script_packet_draft"
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
    private val knownVerdicts = setOf("PASS", "PASS_WITH_WARNINGS", "FAIL")

    fun validate(
        acceptedDraftPacket: NovelizationAcceptedDraftPacket,
        preflightId: String = "",
        acceptedDraftPacketFile: String = "",
    ): NovelizationScriptPacketPreflightReport {
        val findings = mutableListOf<NovelizationScriptPacketPreflightFinding>()

        if (preflightId.isBlank()) {
            findings.add(
                finding(
                    ruleId = "PREFLIGHT_ID_MISSING",
                    category = "structure",
                    severity = "WARNING",
                    path = "$.preflightId",
                    message = "Script packet preflight should carry a stable preflightId.",
                    suggestedFix = "Pass --preflight with a stable preflight id.",
                ),
            )
        }
        if (acceptedDraftPacket.packetId.isBlank()) {
            findings.add(
                finding(
                    ruleId = "ACCEPTED_DRAFT_PACKET_ID_MISSING",
                    category = "packet",
                    severity = "MAJOR",
                    path = "$.acceptedDraftPacket.packetId",
                    message = "Accepted draft packet id is missing.",
                    suggestedFix = "Regenerate the accepted draft packet with a packet id.",
                ),
            )
        }
        if (acceptedDraftPacket.verdict.trim().uppercase() != "PASS") {
            findings.add(
                finding(
                    ruleId = "ACCEPTED_DRAFT_PACKET_NOT_PASS",
                    category = "packet",
                    severity = "CRITICAL",
                    path = "$.acceptedDraftPacket.verdict",
                    message = "Script packet preflight can pass only when accepted draft packet verdict is PASS.",
                    evidence = listOf(acceptedDraftPacket.verdict),
                    suggestedFix = "Fix upstream accepted draft packet before script packet preflight.",
                ),
            )
        }
        if (acceptedDraftPacket.nextAllowedDownstreamStep != requiredDownstreamStep) {
            findings.add(
                finding(
                    ruleId = "DOWNSTREAM_STEP_NOT_ALLOWED",
                    category = "downstream",
                    severity = "CRITICAL",
                    path = "$.acceptedDraftPacket.nextAllowedDownstreamStep",
                    message = "Accepted draft packet does not allow script packet draft as the next downstream step.",
                    evidence = listOf(acceptedDraftPacket.nextAllowedDownstreamStep),
                    suggestedFix = "Regenerate the accepted draft packet after revised draft intake PASS.",
                ),
            )
        }
        if (!sha256Regex.matches(acceptedDraftPacket.revisedDraftContentSha256)) {
            findings.add(
                finding(
                    ruleId = "REVISED_DRAFT_CONTENT_HASH_INVALID",
                    category = "hash",
                    severity = "MAJOR",
                    path = "$.acceptedDraftPacket.revisedDraftContentSha256",
                    message = "Accepted draft packet must carry a 64-character lowercase SHA-256 revised draft content hash.",
                    suggestedFix = "Regenerate the accepted draft packet from the revised draft file.",
                ),
            )
        }

        validateSourceReports(acceptedDraftPacket, findings)
        if (acceptedDraftPacket.protectedContinuityAnchors.none { it.isNotBlank() }) {
            findings.add(
                finding(
                    ruleId = "PROTECTED_CONTINUITY_ANCHORS_EMPTY",
                    category = "continuity",
                    severity = "CRITICAL",
                    path = "$.acceptedDraftPacket.protectedContinuityAnchors",
                    message = "Script packet preflight requires protected continuity anchors.",
                    suggestedFix = "Regenerate the accepted draft packet with protected continuity anchors.",
                ),
            )
        }

        val verdict = when {
            findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
            findings.any { it.severity == "WARNING" } -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }

        return NovelizationScriptPacketPreflightReport(
            verdict = verdict,
            preflightId = preflightId,
            acceptedDraftPacketFile = acceptedDraftPacketFile,
            acceptedDraftPacketId = acceptedDraftPacket.packetId,
            nextAllowedDownstreamStep = acceptedDraftPacket.nextAllowedDownstreamStep,
            findingCount = findings.size,
            findings = findings,
            evidence = NovelizationScriptPacketPreflightEvidence(
                packetVerdict = acceptedDraftPacket.verdict,
                revisedDraftContentHashPresent = sha256Regex.matches(acceptedDraftPacket.revisedDraftContentSha256),
                sourceReportFileCount = sourceReportFileCount(acceptedDraftPacket),
                sourceReportVerdictCount = sourceReportVerdictCount(acceptedDraftPacket),
                protectedContinuityAnchorCount = acceptedDraftPacket.protectedContinuityAnchors.count { it.isNotBlank() },
                requiredFixHandledCount = acceptedDraftPacket.requiredFixesHandledSummary.count { it.itemId.isNotBlank() },
                residualRiskCount = acceptedDraftPacket.remainingResidualRisks.count { it.isNotBlank() },
            ),
        )
    }

    private fun validateSourceReports(
        acceptedDraftPacket: NovelizationAcceptedDraftPacket,
        findings: MutableList<NovelizationScriptPacketPreflightFinding>,
    ) {
        val reports = acceptedDraftPacket.sourceReports
        val requiredFiles = listOf(
            "$.sourceReports.revisionBriefReportFile" to reports.revisionBriefReportFile,
            "$.sourceReports.revisedDraftIntakeReportFile" to reports.revisedDraftIntakeReportFile,
            "$.sourceReports.continuityAnchorsFile" to reports.continuityAnchorsFile,
        )
        requiredFiles.forEach { (path, value) ->
            if (value.isBlank()) {
                findings.add(
                    finding(
                        ruleId = "SOURCE_REPORT_FILE_MISSING",
                        category = "source_reports",
                        severity = "MAJOR",
                        path = path,
                        message = "Accepted draft packet is missing a source report file path.",
                        suggestedFix = "Regenerate the accepted draft packet with source report file paths.",
                    ),
                )
            }
        }

        validateSourceVerdict(
            verdict = reports.revisionBriefVerdict,
            path = "$.sourceReports.revisionBriefVerdict",
            expectedPassOnly = false,
            findings = findings,
        )
        validateSourceVerdict(
            verdict = reports.revisedDraftIntakeVerdict,
            path = "$.sourceReports.revisedDraftIntakeVerdict",
            expectedPassOnly = true,
            findings = findings,
        )
    }

    private fun validateSourceVerdict(
        verdict: String,
        path: String,
        expectedPassOnly: Boolean,
        findings: MutableList<NovelizationScriptPacketPreflightFinding>,
    ) {
        val normalized = verdict.trim().uppercase()
        when {
            normalized.isBlank() -> findings.add(
                finding(
                    ruleId = "SOURCE_REPORT_VERDICT_MISSING",
                    category = "source_reports",
                    severity = "MAJOR",
                    path = path,
                    message = "Accepted draft packet is missing a source report verdict.",
                    suggestedFix = "Regenerate the accepted draft packet with source report verdicts.",
                ),
            )
            normalized !in knownVerdicts -> findings.add(
                finding(
                    ruleId = "SOURCE_REPORT_VERDICT_UNKNOWN",
                    category = "source_reports",
                    severity = "MAJOR",
                    path = path,
                    message = "Accepted draft packet has an unknown source report verdict.",
                    evidence = listOf(verdict),
                    suggestedFix = "Use source report verdict PASS, PASS_WITH_WARNINGS, or FAIL.",
                ),
            )
            expectedPassOnly && normalized != "PASS" -> findings.add(
                finding(
                    ruleId = "REVISED_DRAFT_INTAKE_SOURCE_NOT_PASS",
                    category = "source_reports",
                    severity = "CRITICAL",
                    path = path,
                    message = "Revised draft intake source verdict must be PASS before script packet preflight.",
                    evidence = listOf(verdict),
                    suggestedFix = "Rerun revised draft intake and accepted draft packet creation after PASS.",
                ),
            )
            !expectedPassOnly && normalized == "FAIL" -> findings.add(
                finding(
                    ruleId = "REVISION_BRIEF_SOURCE_FAILED",
                    category = "source_reports",
                    severity = "CRITICAL",
                    path = path,
                    message = "Revision brief source verdict cannot be FAIL before script packet preflight.",
                    evidence = listOf(verdict),
                    suggestedFix = "Fix the revision brief before downstream script packet preflight.",
                ),
            )
        }
    }

    private fun sourceReportFileCount(acceptedDraftPacket: NovelizationAcceptedDraftPacket): Int =
        listOf(
            acceptedDraftPacket.sourceReports.revisionBriefReportFile,
            acceptedDraftPacket.sourceReports.revisedDraftIntakeReportFile,
            acceptedDraftPacket.sourceReports.continuityAnchorsFile,
        ).count { it.isNotBlank() }

    private fun sourceReportVerdictCount(acceptedDraftPacket: NovelizationAcceptedDraftPacket): Int =
        listOf(
            acceptedDraftPacket.sourceReports.revisionBriefVerdict,
            acceptedDraftPacket.sourceReports.revisedDraftIntakeVerdict,
        ).count { it.isNotBlank() }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): NovelizationScriptPacketPreflightFinding =
        NovelizationScriptPacketPreflightFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            path = path,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
        )
}
