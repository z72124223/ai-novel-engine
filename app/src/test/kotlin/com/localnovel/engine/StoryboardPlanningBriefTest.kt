package com.localnovel.engine

import com.localnovel.engine.model.AcceptedScriptPacketContinuityAnchorsSummary
import com.localnovel.engine.model.AcceptedScriptPacketHandoffPacket
import com.localnovel.engine.model.AcceptedScriptPacketSourceReports
import com.localnovel.engine.model.StoryboardPlanningBrief
import com.localnovel.engine.model.StoryboardPlanningPreflightReport
import com.localnovel.engine.service.StoryboardPlanningBriefService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StoryboardPlanningBriefTest {
    @Test
    fun passAcceptedPacketAndPreflightBuildsBrief() {
        val brief = StoryboardPlanningBriefService().build(
            acceptedScriptPacket = acceptedScriptPacket(),
            storyboardPlanningPreflight = storyboardPlanningPreflight(),
            briefId = "storyboard-planning-brief-001",
            acceptedScriptPacketFile = "accepted_script_packet.json",
            storyboardPlanningPreflightFile = "storyboard_planning_preflight.json",
        )

        assertEquals("PASS", brief.verdict)
        assertEquals("storyboard-planning-brief-001", brief.briefId)
        assertEquals("accepted-script-packet-001", brief.acceptedScriptPacketId)
        assertEquals("script_packet_draft.json", brief.scriptPacketDraftFile)
        assertTrue(brief.scriptPacketDraftContentSha256.matches(Regex("[0-9a-f]{64}")))
        assertEquals("storyboard_packet_draft", brief.nextAllowedDownstreamStep)
        assertEquals(2, brief.protectedContinuityAnchorsSummary.protectedContinuityAnchorCount)
        assertTrue(brief.remainingResidualRisks.isNotEmpty())
        assertTrue(brief.sceneBeatPlanningSummary.notes.isNotEmpty())
        assertTrue(brief.framePlanningConstraints.downstreamBoundary.isNotBlank())
        assertTrue(brief.mustShowBoundary.isNotEmpty())
        assertTrue(brief.mustNotShowBoundary.isNotEmpty())
        assertTrue(brief.assetNeedsCarriedForward.policy.isNotBlank())
        assertTrue(brief.reviewChecklist.isNotEmpty())
    }

    @Test
    fun nonPassAcceptedPacketFailsBrief() {
        val brief = StoryboardPlanningBriefService().build(
            acceptedScriptPacket = acceptedScriptPacket(verdict = "FAIL"),
            storyboardPlanningPreflight = storyboardPlanningPreflight(),
        )

        assertEquals("FAIL", brief.verdict)
        assertEquals("", brief.nextAllowedDownstreamStep)
        assertTrue(brief.findings.any { it.ruleId == "ACCEPTED_SCRIPT_PACKET_NOT_PASS" })
    }

    @Test
    fun nonPassPreflightOrWrongNextStepFailsBrief() {
        val brief = StoryboardPlanningBriefService().build(
            acceptedScriptPacket = acceptedScriptPacket(),
            storyboardPlanningPreflight = storyboardPlanningPreflight(
                verdict = "FAIL",
                nextAllowedDownstreamStep = "storyboard_packet_draft",
            ),
        )

        assertEquals("FAIL", brief.verdict)
        assertEquals("", brief.nextAllowedDownstreamStep)
        assertTrue(brief.findings.any { it.ruleId == "STORYBOARD_PLANNING_PREFLIGHT_NOT_PASS" })
        assertTrue(brief.findings.any { it.ruleId == "PREFLIGHT_NEXT_STEP_NOT_PLANNING_BRIEF" })
    }

    @Test
    fun missingPreflightAcceptedScriptPacketIdFailsBrief() {
        val brief = StoryboardPlanningBriefService().build(
            acceptedScriptPacket = acceptedScriptPacket(),
            storyboardPlanningPreflight = storyboardPlanningPreflight(acceptedScriptPacketId = ""),
        )

        assertEquals("FAIL", brief.verdict)
        assertEquals("", brief.nextAllowedDownstreamStep)
        assertTrue(brief.findings.any { it.ruleId == "PREFLIGHT_ACCEPTED_SCRIPT_PACKET_ID_MISSING" })
    }

    @Test
    fun missingAcceptedScriptPacketIdFailsBrief() {
        val brief = StoryboardPlanningBriefService().build(
            acceptedScriptPacket = acceptedScriptPacket(packetId = ""),
            storyboardPlanningPreflight = storyboardPlanningPreflight(),
        )

        assertEquals("FAIL", brief.verdict)
        assertEquals("", brief.nextAllowedDownstreamStep)
        assertTrue(brief.findings.any { it.ruleId == "ACCEPTED_SCRIPT_PACKET_ID_MISSING" })
    }

    @Test
    fun mismatchedAcceptedScriptPacketIdsFailBrief() {
        val brief = StoryboardPlanningBriefService().build(
            acceptedScriptPacket = acceptedScriptPacket(packetId = "accepted-script-packet-001"),
            storyboardPlanningPreflight = storyboardPlanningPreflight(acceptedScriptPacketId = "accepted-script-packet-002"),
        )

        assertEquals("FAIL", brief.verdict)
        assertEquals("", brief.nextAllowedDownstreamStep)
        assertTrue(brief.findings.any { it.ruleId == "ACCEPTED_SCRIPT_PACKET_ID_MISMATCH" })
    }

    @Test
    fun missingSourceHashAnchorsOrRisksFailsBrief() {
        val brief = StoryboardPlanningBriefService().build(
            acceptedScriptPacket = acceptedScriptPacket(
                scriptPacketDraftFile = "",
                scriptPacketDraftContentSha256 = "",
                sourceReports = AcceptedScriptPacketSourceReports(),
                protectedContinuityAnchorsSummary = AcceptedScriptPacketContinuityAnchorsSummary(),
                remainingResidualRisks = emptyList(),
            ),
            storyboardPlanningPreflight = storyboardPlanningPreflight(),
        )

        assertEquals("FAIL", brief.verdict)
        assertEquals("", brief.nextAllowedDownstreamStep)
        assertTrue(brief.findings.any { it.ruleId == "SCRIPT_PACKET_DRAFT_HASH_INVALID" })
        assertTrue(brief.findings.any { it.ruleId == "SCRIPT_PACKET_DRAFT_FILE_MISSING" })
        assertTrue(brief.findings.any { it.ruleId == "STRUCTURE_BRIDGE_REPORT_FILE_MISSING" })
        assertTrue(brief.findings.any { it.ruleId == "SCRIPT_PACKET_PREFLIGHT_REPORT_FILE_MISSING" })
        assertTrue(brief.findings.any { it.ruleId == "ACCEPTED_DRAFT_PACKET_FILE_MISSING" })
        assertTrue(brief.findings.any { it.ruleId == "STRUCTURE_BRIDGE_VERDICT_MISSING" })
        assertTrue(brief.findings.any { it.ruleId == "SOURCE_BINDING_VERDICT_MISSING" })
        assertTrue(brief.findings.any { it.ruleId == "STRUCTURE_VERDICT_MISSING" })
        assertTrue(brief.findings.any { it.ruleId == "PROTECTED_CONTINUITY_ANCHORS_MISSING" })
        assertTrue(brief.findings.any { it.ruleId == "RESIDUAL_RISKS_MISSING" })
    }

    @Test
    fun forbiddenCompletionClaimInCarriedTextFailsBrief() {
        val brief = StoryboardPlanningBriefService().build(
            acceptedScriptPacket = acceptedScriptPacket(
                remainingResidualRisks = listOf("final video complete; assets approved"),
            ),
            storyboardPlanningPreflight = storyboardPlanningPreflight(),
        )

        assertEquals("FAIL", brief.verdict)
        assertEquals("", brief.nextAllowedDownstreamStep)
        assertTrue(brief.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
        assertTrue(brief.evidence.forbiddenClaimCount > 0)
    }

    @Test
    fun cliWritesStoryboardPlanningBriefWithoutDatabase() {
        val tempDir = Files.createTempDirectory("storyboard-planning-brief")
        val acceptedPacketPath = tempDir.resolve("accepted_script_packet.json")
        val preflightPath = tempDir.resolve("storyboard_planning_preflight.json")
        val out = tempDir.resolve("storyboard_planning_brief.json")
        writeJson(acceptedPacketPath, acceptedScriptPacket())
        writeJson(
            preflightPath,
            storyboardPlanningPreflight(acceptedScriptPacketFile = acceptedPacketPath.toString()),
        )

        val exit = Cli().run(
            listOf(
                "create-storyboard-planning-brief",
                "--accepted-script-packet",
                acceptedPacketPath.toString(),
                "--storyboard-planning-preflight",
                preflightPath.toString(),
                "--brief",
                "storyboard-planning-brief-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val brief = readJson<StoryboardPlanningBrief>(out)
        assertEquals("PASS", brief.verdict)
        assertEquals("storyboard-planning-brief-cli-001", brief.briefId)
        assertEquals("storyboard_packet_draft", brief.nextAllowedDownstreamStep)
        assertEquals(acceptedPacketPath.toString(), brief.acceptedScriptPacketFile)
        assertEquals(preflightPath.toString(), brief.storyboardPlanningPreflightFile)
    }

    private fun acceptedScriptPacket(
        verdict: String = "PASS",
        packetId: String = "accepted-script-packet-001",
        scriptPacketDraftFile: String = "script_packet_draft.json",
        scriptPacketDraftContentSha256: String = "a".repeat(64),
        sourceReports: AcceptedScriptPacketSourceReports = sourceReports(),
        protectedContinuityAnchorsSummary: AcceptedScriptPacketContinuityAnchorsSummary = continuitySummary(),
        remainingResidualRisks: List<String> = listOf("Storyboard packet draft has not yet validated frame continuity."),
    ): AcceptedScriptPacketHandoffPacket =
        AcceptedScriptPacketHandoffPacket(
            verdict = verdict,
            packetId = packetId,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = scriptPacketDraftContentSha256,
            sourceReports = sourceReports,
            protectedContinuityAnchorsSummary = protectedContinuityAnchorsSummary,
            remainingResidualRisks = remainingResidualRisks,
            nextAllowedDownstreamStep = "storyboard_planning_preflight",
        )

    private fun storyboardPlanningPreflight(
        verdict: String = "PASS",
        nextAllowedDownstreamStep: String = "storyboard_planning_brief",
        acceptedScriptPacketFile: String = "accepted_script_packet.json",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
    ): StoryboardPlanningPreflightReport =
        StoryboardPlanningPreflightReport(
            verdict = verdict,
            preflightId = "storyboard-planning-preflight-001",
            acceptedScriptPacketFile = acceptedScriptPacketFile,
            acceptedScriptPacketId = acceptedScriptPacketId,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun sourceReports(): AcceptedScriptPacketSourceReports =
        AcceptedScriptPacketSourceReports(
            structureBridgeReportFile = "structure_bridge_report.json",
            structureBridgeVerdict = "PASS",
            sourceBindingVerdict = "PASS",
            structureVerdict = "PASS",
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
