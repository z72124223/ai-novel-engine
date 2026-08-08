package com.localnovel.engine

import com.localnovel.engine.model.NovelizationAcceptedDraftHandledFix
import com.localnovel.engine.model.NovelizationAcceptedDraftPacket
import com.localnovel.engine.model.NovelizationAcceptedDraftSourceReports
import com.localnovel.engine.model.NovelizationScriptPacketPreflightReport
import com.localnovel.engine.service.NovelizationScriptPacketPreflightService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NovelizationScriptPacketPreflightTest {
    @Test
    fun completeAcceptedDraftPacketPassesPreflight() {
        val report = NovelizationScriptPacketPreflightService().validate(
            acceptedDraftPacket = acceptedPacket(),
            preflightId = "script-packet-preflight-001",
            acceptedDraftPacketFile = "reports/accepted_draft_packet.json",
        )

        assertEquals("PASS", report.verdict)
        assertEquals("script-packet-preflight-001", report.preflightId)
        assertEquals("accepted-draft-packet-001", report.acceptedDraftPacketId)
        assertEquals("script_packet_draft", report.nextAllowedDownstreamStep)
        assertTrue(report.evidence.revisedDraftContentHashPresent)
        assertEquals(3, report.evidence.sourceReportFileCount)
        assertEquals(2, report.evidence.sourceReportVerdictCount)
        assertEquals(2, report.evidence.protectedContinuityAnchorCount)
    }

    @Test
    fun nonPassAcceptedDraftPacketBlocksPreflight() {
        val report = NovelizationScriptPacketPreflightService().validate(
            acceptedDraftPacket = acceptedPacket(verdict = "FAIL"),
            preflightId = "script-packet-preflight-001",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "ACCEPTED_DRAFT_PACKET_NOT_PASS" })
    }

    @Test
    fun missingHashWrongStepOrMissingAnchorsFailsPreflight() {
        val report = NovelizationScriptPacketPreflightService().validate(
            acceptedDraftPacket = acceptedPacket(
                revisedDraftContentSha256 = "",
                nextAllowedDownstreamStep = "storyboard_draft",
                protectedContinuityAnchors = emptyList(),
            ),
            preflightId = "script-packet-preflight-001",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "REVISED_DRAFT_CONTENT_HASH_INVALID" })
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_STEP_NOT_ALLOWED" })
        assertTrue(report.findings.any { it.ruleId == "PROTECTED_CONTINUITY_ANCHORS_EMPTY" })
    }

    @Test
    fun cliWritesScriptPacketPreflightReportWithoutDatabase() {
        val tempDir = Files.createTempDirectory("script-packet-preflight")
        val packetPath = tempDir.resolve("accepted_draft_packet.json")
        val out = tempDir.resolve("script_packet_preflight_report.json")
        writeJson(packetPath, acceptedPacket())

        val exit = Cli().run(
            listOf(
                "validate-novelization-script-packet-preflight",
                "--accepted-draft-packet",
                packetPath.toString(),
                "--preflight",
                "script-packet-preflight-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<NovelizationScriptPacketPreflightReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("script-packet-preflight-cli-001", report.preflightId)
        assertEquals("accepted-draft-packet-001", report.acceptedDraftPacketId)
    }

    private fun acceptedPacket(
        verdict: String = "PASS",
        revisedDraftContentSha256: String = "a".repeat(64),
        nextAllowedDownstreamStep: String = "script_packet_draft",
        protectedContinuityAnchors: List<String> = listOf(
            "Mira already paid the door toll before reaching the checkpoint.",
            "The guard requires a visible mark before answering questions.",
        ),
    ): NovelizationAcceptedDraftPacket =
        NovelizationAcceptedDraftPacket(
            verdict = verdict,
            packetId = "accepted-draft-packet-001",
            revisedDraftFile = "drafts/revised.md",
            revisedDraftContentSha256 = revisedDraftContentSha256,
            sourceReports = NovelizationAcceptedDraftSourceReports(
                revisionBriefReportFile = "reports/revision_brief.json",
                revisionBriefVerdict = "PASS",
                revisedDraftIntakeReportFile = "reports/revised_draft_intake.json",
                revisedDraftIntakeVerdict = "PASS",
                continuityAnchorsFile = "reports/continuity_anchors.json",
                continuityAnchorFileId = "continuity-anchors-sample-001",
            ),
            requiredFixesHandledSummary = listOf(
                NovelizationAcceptedDraftHandledFix(
                    itemId = "revision-item-001",
                    fixType = "banned_phrase_removal",
                    sourceRuleId = "BANNED_CALIBRATION_STRUCTURE_CALIBRATION_EN_NOT_BUT",
                    severity = "CRITICAL",
                ),
            ),
            protectedContinuityAnchors = protectedContinuityAnchors,
            doNotChange = listOf("Do not reset the toll payment as unpaid."),
            remainingResidualRisks = listOf("Downstream script/storyboard/video gates have not yet validated transformation readiness."),
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
