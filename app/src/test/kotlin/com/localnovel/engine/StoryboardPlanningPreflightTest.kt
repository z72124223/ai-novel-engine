package com.localnovel.engine

import com.localnovel.engine.model.AcceptedScriptPacketContinuityAnchorsSummary
import com.localnovel.engine.model.AcceptedScriptPacketHandoffPacket
import com.localnovel.engine.model.AcceptedScriptPacketSourceReports
import com.localnovel.engine.model.StoryboardPlanningPreflightReport
import com.localnovel.engine.service.StoryboardPlanningPreflightService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StoryboardPlanningPreflightTest {
    @Test
    fun passAcceptedScriptPacketAllowsStoryboardPlanningBrief() {
        val report = StoryboardPlanningPreflightService().validate(
            acceptedScriptPacket = acceptedScriptPacket(),
            preflightId = "storyboard-planning-preflight-001",
            acceptedScriptPacketFile = "accepted_script_packet.json",
        )

        assertEquals("PASS", report.verdict)
        assertEquals("storyboard-planning-preflight-001", report.preflightId)
        assertEquals("accepted-script-packet-001", report.acceptedScriptPacketId)
        assertEquals("accepted_script_packet.json", report.acceptedScriptPacketFile)
        assertEquals("storyboard_planning_brief", report.nextAllowedDownstreamStep)
        assertEquals("", report.blockedReason)
        assertEquals(2, report.evidence.protectedContinuityAnchorCount)
        assertEquals(1, report.evidence.residualRiskCount)
        assertTrue(report.evidence.scriptPacketDraftHashPresent)
    }

    @Test
    fun nonPassPacketOrWrongDownstreamStepBlocksPreflight() {
        val report = StoryboardPlanningPreflightService().validate(
            acceptedScriptPacket = acceptedScriptPacket(
                verdict = "FAIL",
                nextAllowedDownstreamStep = "storyboard_packet_draft",
            ),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.blockedReason.contains("ACCEPTED_SCRIPT_PACKET_NOT_PASS"))
        assertTrue(report.blockedReason.contains("NEXT_DOWNSTREAM_STEP_NOT_STORYBOARD_PREFLIGHT"))
        assertTrue(report.findings.any { it.ruleId == "ACCEPTED_SCRIPT_PACKET_NOT_PASS" })
        assertTrue(report.findings.any { it.ruleId == "NEXT_DOWNSTREAM_STEP_NOT_STORYBOARD_PREFLIGHT" })
    }

    @Test
    fun missingHashSourcesAnchorsOrResidualRisksFailsPreflight() {
        val report = StoryboardPlanningPreflightService().validate(
            acceptedScriptPacket = acceptedScriptPacket(
                scriptPacketDraftContentSha256 = "",
                sourceReports = AcceptedScriptPacketSourceReports(),
                protectedContinuityAnchorsSummary = AcceptedScriptPacketContinuityAnchorsSummary(),
                remainingResidualRisks = emptyList(),
            ),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "SCRIPT_PACKET_DRAFT_HASH_INVALID" })
        assertTrue(report.findings.any { it.ruleId == "STRUCTURE_BRIDGE_VERDICT_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "SOURCE_BINDING_VERDICT_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "STRUCTURE_VERDICT_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "STRUCTURE_BRIDGE_REPORT_FILE_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "SCRIPT_PACKET_PREFLIGHT_REPORT_FILE_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "ACCEPTED_DRAFT_PACKET_FILE_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "PROTECTED_CONTINUITY_ANCHORS_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "RESIDUAL_RISKS_MISSING" })
    }

    @Test
    fun structureFailureBlocksPreflight() {
        val report = StoryboardPlanningPreflightService().validate(
            acceptedScriptPacket = acceptedScriptPacket(
                sourceReports = sourceReports(structureVerdict = "FAIL"),
            ),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "STRUCTURE_VERDICT_FAILED" })
    }

    @Test
    fun cliWritesStoryboardPlanningPreflightReportWithoutDatabase() {
        val tempDir = Files.createTempDirectory("storyboard-planning-preflight")
        val packetPath = tempDir.resolve("accepted_script_packet.json")
        val out = tempDir.resolve("storyboard_planning_preflight.json")
        writeJson(packetPath, acceptedScriptPacket())

        val exit = Cli().run(
            listOf(
                "validate-storyboard-planning-preflight",
                "--accepted-script-packet",
                packetPath.toString(),
                "--preflight",
                "storyboard-planning-preflight-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<StoryboardPlanningPreflightReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("storyboard-planning-preflight-cli-001", report.preflightId)
        assertEquals("storyboard_planning_brief", report.nextAllowedDownstreamStep)
        assertEquals(packetPath.toString(), report.acceptedScriptPacketFile)
    }

    private fun acceptedScriptPacket(
        verdict: String = "PASS",
        nextAllowedDownstreamStep: String = "storyboard_planning_preflight",
        scriptPacketDraftContentSha256: String = "a".repeat(64),
        sourceReports: AcceptedScriptPacketSourceReports = sourceReports(),
        protectedContinuityAnchorsSummary: AcceptedScriptPacketContinuityAnchorsSummary = continuitySummary(),
        remainingResidualRisks: List<String> = listOf("Storyboard planning brief has not yet validated frame continuity."),
    ): AcceptedScriptPacketHandoffPacket =
        AcceptedScriptPacketHandoffPacket(
            verdict = verdict,
            packetId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = scriptPacketDraftContentSha256,
            sourceReports = sourceReports,
            protectedContinuityAnchorsSummary = protectedContinuityAnchorsSummary,
            remainingResidualRisks = remainingResidualRisks,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun sourceReports(
        structureBridgeVerdict: String = "PASS",
        sourceBindingVerdict: String = "PASS",
        structureVerdict: String = "PASS",
    ): AcceptedScriptPacketSourceReports =
        AcceptedScriptPacketSourceReports(
            structureBridgeReportFile = "structure_bridge_report.json",
            structureBridgeVerdict = structureBridgeVerdict,
            sourceBindingVerdict = sourceBindingVerdict,
            structureVerdict = structureVerdict,
            scriptPacketPreflightReportFile = "script_packet_preflight.json",
            acceptedDraftPacketFile = "accepted_draft_packet.json",
        )

    private fun continuitySummary(): AcceptedScriptPacketContinuityAnchorsSummary =
        AcceptedScriptPacketContinuityAnchorsSummary(
            protectedContinuityAnchorCount = 2,
            protectedContinuityAnchors = listOf(
                "Mira already paid the door toll before reaching the checkpoint.",
                "The guard requires a visible mark before answering questions.",
            ),
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
