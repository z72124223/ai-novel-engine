package com.localnovel.engine.service

import com.localnovel.engine.model.AcceptedScriptPacketHandoffPacket
import com.localnovel.engine.model.StoryboardPlanningPreflightEvidence
import com.localnovel.engine.model.StoryboardPlanningPreflightFinding
import com.localnovel.engine.model.StoryboardPlanningPreflightReport

class StoryboardPlanningPreflightService {
    private val requiredInputStep = "storyboard_planning_preflight"
    private val nextAllowedDownstreamStep = "storyboard_planning_brief"
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
    private val knownVerdicts = setOf("PASS", "PASS_WITH_WARNINGS", "FAIL")

    fun validate(
        acceptedScriptPacket: AcceptedScriptPacketHandoffPacket,
        preflightId: String = "",
        acceptedScriptPacketFile: String = "",
    ): StoryboardPlanningPreflightReport {
        val findings = mutableListOf<StoryboardPlanningPreflightFinding>()
        val packetVerdict = acceptedScriptPacket.verdict.normalizedVerdict()
        val structureBridgeVerdict = acceptedScriptPacket.sourceReports.structureBridgeVerdict.normalizedVerdict()
        val sourceBindingVerdict = acceptedScriptPacket.sourceReports.sourceBindingVerdict.normalizedVerdict()
        val structureVerdict = acceptedScriptPacket.sourceReports.structureVerdict.normalizedVerdict()

        requirePassVerdict(
            verdict = packetVerdict,
            originalVerdict = acceptedScriptPacket.verdict,
            path = "$.acceptedScriptPacket.verdict",
            source = "accepted script packet handoff packet",
            missingRuleId = "ACCEPTED_SCRIPT_PACKET_VERDICT_MISSING",
            unknownRuleId = "ACCEPTED_SCRIPT_PACKET_VERDICT_UNKNOWN",
            failRuleId = "ACCEPTED_SCRIPT_PACKET_NOT_PASS",
            findings = findings,
        )

        if (acceptedScriptPacket.nextAllowedDownstreamStep != requiredInputStep) {
            findings.add(
                finding(
                    ruleId = "NEXT_DOWNSTREAM_STEP_NOT_STORYBOARD_PREFLIGHT",
                    category = "downstream",
                    severity = "CRITICAL",
                    path = "$.acceptedScriptPacket.nextAllowedDownstreamStep",
                    message = "Storyboard planning preflight requires accepted script packet nextAllowedDownstreamStep=storyboard_planning_preflight.",
                    evidence = listOf(acceptedScriptPacket.nextAllowedDownstreamStep),
                    suggestedFix = "Use the accepted script packet handoff packet produced for storyboard planning preflight.",
                ),
            )
        }

        if (!sha256Regex.matches(acceptedScriptPacket.scriptPacketDraftContentSha256)) {
            findings.add(
                finding(
                    ruleId = "SCRIPT_PACKET_DRAFT_HASH_INVALID",
                    category = "source_linkage",
                    severity = "CRITICAL",
                    path = "$.acceptedScriptPacket.scriptPacketDraftContentSha256",
                    message = "Accepted script packet must carry a 64-character lowercase SHA-256 script packet draft content hash.",
                    evidence = listOf(acceptedScriptPacket.scriptPacketDraftContentSha256),
                    suggestedFix = "Recreate the accepted script packet handoff from the script packet draft JSON file.",
                ),
            )
        }

        requirePassVerdict(
            verdict = structureBridgeVerdict,
            originalVerdict = acceptedScriptPacket.sourceReports.structureBridgeVerdict,
            path = "$.acceptedScriptPacket.sourceReports.structureBridgeVerdict",
            source = "structure bridge",
            missingRuleId = "STRUCTURE_BRIDGE_VERDICT_MISSING",
            unknownRuleId = "STRUCTURE_BRIDGE_VERDICT_UNKNOWN",
            failRuleId = "STRUCTURE_BRIDGE_NOT_PASS",
            findings = findings,
        )
        requirePassVerdict(
            verdict = sourceBindingVerdict,
            originalVerdict = acceptedScriptPacket.sourceReports.sourceBindingVerdict,
            path = "$.acceptedScriptPacket.sourceReports.sourceBindingVerdict",
            source = "source-binding",
            missingRuleId = "SOURCE_BINDING_VERDICT_MISSING",
            unknownRuleId = "SOURCE_BINDING_VERDICT_UNKNOWN",
            failRuleId = "SOURCE_BINDING_NOT_PASS",
            findings = findings,
        )
        requireStructureVerdict(structureVerdict, acceptedScriptPacket.sourceReports.structureVerdict, findings)
        validateSourceReportFiles(acceptedScriptPacket, findings)
        validateContinuityAnchors(acceptedScriptPacket, findings)
        validateResidualRisks(acceptedScriptPacket, findings)

        val verdict = when {
            findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
            findings.any { it.severity == "WARNING" } -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }
        val blockedReason = if (verdict == "FAIL") {
            findings
                .filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }
                .joinToString("; ") { it.ruleId }
        } else {
            ""
        }

        return StoryboardPlanningPreflightReport(
            verdict = verdict,
            preflightId = preflightId,
            acceptedScriptPacketFile = acceptedScriptPacketFile,
            acceptedScriptPacketId = acceptedScriptPacket.packetId,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = StoryboardPlanningPreflightEvidence(
                packetVerdict = acceptedScriptPacket.verdict,
                structureBridgeVerdict = acceptedScriptPacket.sourceReports.structureBridgeVerdict,
                sourceBindingVerdict = acceptedScriptPacket.sourceReports.sourceBindingVerdict,
                structureVerdict = acceptedScriptPacket.sourceReports.structureVerdict,
                scriptPacketDraftHashPresent = sha256Regex.matches(acceptedScriptPacket.scriptPacketDraftContentSha256),
                sourceReportFileCount = sourceReportFileCount(acceptedScriptPacket),
                sourceReportVerdictCount = sourceReportVerdictCount(acceptedScriptPacket),
                protectedContinuityAnchorCount = acceptedScriptPacket.protectedContinuityAnchorsSummary.protectedContinuityAnchorCount,
                residualRiskCount = acceptedScriptPacket.remainingResidualRisks.count { it.isNotBlank() },
            ),
        )
    }

    private fun requirePassVerdict(
        verdict: String,
        originalVerdict: String,
        path: String,
        source: String,
        missingRuleId: String,
        unknownRuleId: String,
        failRuleId: String,
        findings: MutableList<StoryboardPlanningPreflightFinding>,
    ) {
        when {
            verdict.isBlank() -> findings.add(
                finding(
                    ruleId = missingRuleId,
                    category = "upstream",
                    severity = "MAJOR",
                    path = path,
                    message = "Linked $source report has no verdict.",
                    suggestedFix = "Regenerate the upstream $source report before storyboard planning preflight.",
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
            verdict != "PASS" -> findings.add(
                finding(
                    ruleId = failRuleId,
                    category = "upstream",
                    severity = "CRITICAL",
                    path = path,
                    message = "Storyboard planning preflight requires linked $source PASS.",
                    evidence = listOf(originalVerdict),
                    suggestedFix = "Fix and rerun the upstream $source gate before storyboard planning.",
                ),
            )
        }
    }

    private fun requireStructureVerdict(
        verdict: String,
        originalVerdict: String,
        findings: MutableList<StoryboardPlanningPreflightFinding>,
    ) {
        when {
            verdict.isBlank() -> findings.add(
                finding(
                    ruleId = "STRUCTURE_VERDICT_MISSING",
                    category = "upstream",
                    severity = "MAJOR",
                    path = "$.acceptedScriptPacket.sourceReports.structureVerdict",
                    message = "Accepted script packet source reports must include structure verdict.",
                    suggestedFix = "Recreate the accepted script packet handoff from a structure bridge report.",
                ),
            )
            verdict !in knownVerdicts -> findings.add(
                finding(
                    ruleId = "STRUCTURE_VERDICT_UNKNOWN",
                    category = "upstream",
                    severity = "MAJOR",
                    path = "$.acceptedScriptPacket.sourceReports.structureVerdict",
                    message = "Structure verdict is unknown.",
                    evidence = listOf(originalVerdict),
                    suggestedFix = "Use a structure verdict PASS, PASS_WITH_WARNINGS, or FAIL.",
                ),
            )
            verdict == "FAIL" -> findings.add(
                finding(
                    ruleId = "STRUCTURE_VERDICT_FAILED",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.acceptedScriptPacket.sourceReports.structureVerdict",
                    message = "Storyboard planning preflight cannot proceed when structure validation failed.",
                    evidence = listOf(originalVerdict),
                    suggestedFix = "Fix script packet structure before storyboard planning.",
                ),
            )
        }
    }

    private fun validateSourceReportFiles(
        acceptedScriptPacket: AcceptedScriptPacketHandoffPacket,
        findings: MutableList<StoryboardPlanningPreflightFinding>,
    ) {
        val reports = acceptedScriptPacket.sourceReports
        requireNonBlank(
            reports.structureBridgeReportFile,
            "$.acceptedScriptPacket.sourceReports.structureBridgeReportFile",
            "STRUCTURE_BRIDGE_REPORT_FILE_MISSING",
            "Accepted script packet must retain the structure bridge report file path.",
            "Recreate the accepted script packet handoff packet with the structure bridge report path.",
            findings,
        )
        requireNonBlank(
            reports.scriptPacketPreflightReportFile,
            "$.acceptedScriptPacket.sourceReports.scriptPacketPreflightReportFile",
            "SCRIPT_PACKET_PREFLIGHT_REPORT_FILE_MISSING",
            "Accepted script packet must retain the script packet preflight report file path.",
            "Recreate the accepted script packet handoff packet from a bridge report with source links.",
            findings,
        )
        requireNonBlank(
            reports.acceptedDraftPacketFile,
            "$.acceptedScriptPacket.sourceReports.acceptedDraftPacketFile",
            "ACCEPTED_DRAFT_PACKET_FILE_MISSING",
            "Accepted script packet must retain the accepted draft packet file path.",
            "Recreate the accepted script packet handoff packet from a bridge report with source links.",
            findings,
        )
    }

    private fun validateContinuityAnchors(
        acceptedScriptPacket: AcceptedScriptPacketHandoffPacket,
        findings: MutableList<StoryboardPlanningPreflightFinding>,
    ) {
        val summary = acceptedScriptPacket.protectedContinuityAnchorsSummary
        val nonBlankAnchors = summary.protectedContinuityAnchors.filter { it.isNotBlank() }
        if (summary.protectedContinuityAnchorCount <= 0 || nonBlankAnchors.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "PROTECTED_CONTINUITY_ANCHORS_MISSING",
                    category = "continuity",
                    severity = "CRITICAL",
                    path = "$.acceptedScriptPacket.protectedContinuityAnchorsSummary",
                    message = "Storyboard planning preflight requires protected continuity anchors summary.",
                    suggestedFix = "Recreate the accepted script packet handoff packet with protected continuity anchors.",
                ),
            )
        }
        if (summary.protectedContinuityAnchorCount != nonBlankAnchors.size) {
            findings.add(
                finding(
                    ruleId = "PROTECTED_CONTINUITY_ANCHOR_COUNT_MISMATCH",
                    category = "continuity",
                    severity = "MAJOR",
                    path = "$.acceptedScriptPacket.protectedContinuityAnchorsSummary.protectedContinuityAnchorCount",
                    message = "Protected continuity anchor count does not match the nonblank protected anchor list.",
                    evidence = listOf(summary.protectedContinuityAnchorCount.toString(), nonBlankAnchors.size.toString()),
                    suggestedFix = "Recreate the accepted script packet handoff packet from the script packet draft.",
                ),
            )
        }
    }

    private fun validateResidualRisks(
        acceptedScriptPacket: AcceptedScriptPacketHandoffPacket,
        findings: MutableList<StoryboardPlanningPreflightFinding>,
    ) {
        if (acceptedScriptPacket.remainingResidualRisks.none { it.isNotBlank() }) {
            findings.add(
                finding(
                    ruleId = "RESIDUAL_RISKS_MISSING",
                    category = "risk",
                    severity = "MAJOR",
                    path = "$.acceptedScriptPacket.remainingResidualRisks",
                    message = "Storyboard planning preflight requires remaining residual risks to be listed.",
                    suggestedFix = "Recreate the accepted script packet handoff packet with downstream residual risks preserved.",
                ),
            )
        }
    }

    private fun requireNonBlank(
        value: String,
        path: String,
        ruleId: String,
        message: String,
        suggestedFix: String,
        findings: MutableList<StoryboardPlanningPreflightFinding>,
    ) {
        if (value.isBlank()) {
            findings.add(
                finding(
                    ruleId = ruleId,
                    category = "source_linkage",
                    severity = "MAJOR",
                    path = path,
                    message = message,
                    suggestedFix = suggestedFix,
                ),
            )
        }
    }

    private fun sourceReportFileCount(packet: AcceptedScriptPacketHandoffPacket): Int =
        listOf(
            packet.sourceReports.structureBridgeReportFile,
            packet.sourceReports.scriptPacketPreflightReportFile,
            packet.sourceReports.acceptedDraftPacketFile,
        ).count { it.isNotBlank() }

    private fun sourceReportVerdictCount(packet: AcceptedScriptPacketHandoffPacket): Int =
        listOf(
            packet.sourceReports.structureBridgeVerdict,
            packet.sourceReports.sourceBindingVerdict,
            packet.sourceReports.structureVerdict,
        ).count { it.isNotBlank() }

    private fun String.normalizedVerdict(): String =
        trim().uppercase()

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): StoryboardPlanningPreflightFinding =
        StoryboardPlanningPreflightFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            path = path,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
        )
}
