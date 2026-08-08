package com.localnovel.engine.service

import com.localnovel.engine.model.NovelizationAcceptedDraftPacket
import com.localnovel.engine.model.NovelizationScriptPacketPreflightReport
import com.localnovel.engine.model.ScriptPacketDraft
import com.localnovel.engine.model.ScriptPacketDraftStructureBridgeEvidence
import com.localnovel.engine.model.ScriptPacketDraftStructureBridgeFinding
import com.localnovel.engine.model.ScriptPacketDraftStructureBridgeReport

class ScriptPacketDraftStructureBridgeService(
    private val sourceBindingValidationService: ScriptPacketDraftSourceBindingValidationService =
        ScriptPacketDraftSourceBindingValidationService(),
    private val draftValidationService: ScriptPacketDraftValidationService =
        ScriptPacketDraftValidationService(),
) {
    fun validate(
        scriptPacketDraft: ScriptPacketDraft,
        scriptPacketPreflight: NovelizationScriptPacketPreflightReport,
        acceptedDraftPacket: NovelizationAcceptedDraftPacket,
        scriptPacketDraftFile: String = "",
        scriptPacketPreflightFile: String = "",
        acceptedDraftPacketFile: String = "",
    ): ScriptPacketDraftStructureBridgeReport {
        val sourceBindingReport = sourceBindingValidationService.validate(
            scriptPacketDraft = scriptPacketDraft,
            scriptPacketPreflight = scriptPacketPreflight,
            acceptedDraftPacket = acceptedDraftPacket,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketPreflightFile = scriptPacketPreflightFile,
            acceptedDraftPacketFile = acceptedDraftPacketFile,
        )
        val structureReport = draftValidationService.validate(
            packet = scriptPacketDraft,
            packetFile = scriptPacketDraftFile,
        )

        val sourceBindingPass = sourceBindingReport.verdict.normalizedVerdict() == "PASS"
        val structureVerdict = structureReport.verdict.normalizedVerdict()
        val structureAcceptable = structureVerdict in setOf("PASS", "PASS_WITH_WARNINGS")
        val findings = mutableListOf<ScriptPacketDraftStructureBridgeFinding>()

        if (!sourceBindingPass) {
            findings.add(
                finding(
                    ruleId = "SOURCE_BINDING_NOT_PASS",
                    category = "source_binding",
                    severity = "CRITICAL",
                    path = "$.sourceBindingVerdict",
                    message = "Script packet structure bridge requires source-binding verdict PASS.",
                    evidence = listOf(sourceBindingReport.verdict),
                    suggestedFix = "Fix source-binding findings before running the combined bridge handoff.",
                ),
            )
        }
        if (!structureAcceptable) {
            findings.add(
                finding(
                    ruleId = "SCRIPT_PACKET_STRUCTURE_NOT_ACCEPTABLE",
                    category = "structure",
                    severity = "CRITICAL",
                    path = "$.structureVerdict",
                    message = "Script packet structure bridge requires structure validation PASS or PASS_WITH_WARNINGS.",
                    evidence = listOf(structureReport.verdict),
                    suggestedFix = "Fix structure validation findings before downstream script packet handoff.",
                ),
            )
        }

        val downstreamAllowed = sourceBindingPass && structureAcceptable
        val blockedReason = blockedReason(sourceBindingPass, sourceBindingReport.verdict, structureAcceptable, structureReport.verdict)
        val verdict = when {
            !downstreamAllowed -> "FAIL"
            structureVerdict == "PASS_WITH_WARNINGS" -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }
        val downstreamAllowedReason = if (downstreamAllowed) {
            "source binding PASS and structure validation ${structureReport.verdict}"
        } else {
            ""
        }

        return ScriptPacketDraftStructureBridgeReport(
            verdict = verdict,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketPreflightFile = scriptPacketPreflightFile,
            acceptedDraftPacketFile = acceptedDraftPacketFile,
            scriptPacketId = scriptPacketDraft.packetId,
            sourceBindingVerdict = sourceBindingReport.verdict,
            structureVerdict = structureReport.verdict,
            downstreamAllowed = downstreamAllowed,
            blockedReason = blockedReason,
            sourceBindingFindingCount = sourceBindingReport.findingCount,
            structureFindingCount = structureReport.findingCount,
            findingCount = findings.size,
            sourceBindingFindings = sourceBindingReport.findings,
            structureFindings = structureReport.findings,
            findings = findings,
            evidence = ScriptPacketDraftStructureBridgeEvidence(
                sourceBindingPass = sourceBindingPass,
                structureAcceptable = structureAcceptable,
                structureWarningAllowed = structureVerdict == "PASS_WITH_WARNINGS",
                downstreamAllowedReason = downstreamAllowedReason,
                blockedReason = blockedReason,
            ),
        )
    }

    private fun blockedReason(
        sourceBindingPass: Boolean,
        sourceBindingVerdict: String,
        structureAcceptable: Boolean,
        structureVerdict: String,
    ): String {
        val reasons = mutableListOf<String>()
        if (!sourceBindingPass) {
            reasons.add("source-binding verdict is $sourceBindingVerdict")
        }
        if (!structureAcceptable) {
            reasons.add("structure verdict is $structureVerdict")
        }
        return reasons.joinToString("; ")
    }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): ScriptPacketDraftStructureBridgeFinding =
        ScriptPacketDraftStructureBridgeFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            path = path,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
        )

    private fun String.normalizedVerdict(): String =
        trim().uppercase()
}
