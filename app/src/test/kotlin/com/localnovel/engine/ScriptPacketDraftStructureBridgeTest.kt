package com.localnovel.engine

import com.localnovel.engine.model.NovelizationAcceptedDraftPacket
import com.localnovel.engine.model.NovelizationAcceptedDraftSourceReports
import com.localnovel.engine.model.NovelizationScriptPacketPreflightReport
import com.localnovel.engine.model.ScriptPacketDraft
import com.localnovel.engine.model.ScriptPacketDraftStructureBridgeReport
import com.localnovel.engine.service.ScriptPacketDraftStructureBridgeService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScriptPacketDraftStructureBridgeTest {
    @Test
    fun sourceBindingAndStructurePassAllowsDownstream() {
        val report = ScriptPacketDraftStructureBridgeService().validate(
            scriptPacketDraft = boundScriptPacketDraft(),
            scriptPacketPreflight = scriptPacketPreflight(),
            acceptedDraftPacket = acceptedPacket(),
        )

        assertEquals("PASS", report.verdict)
        assertEquals("PASS", report.sourceBindingVerdict)
        assertEquals("PASS", report.structureVerdict)
        assertTrue(report.downstreamAllowed)
        assertEquals("", report.blockedReason)
        assertEquals(0, report.findingCount)
    }

    @Test
    fun sourceBindingFailureBlocksBridgeEvenWhenStructurePasses() {
        val report = ScriptPacketDraftStructureBridgeService().validate(
            scriptPacketDraft = boundScriptPacketDraft(includeBinding = false),
            scriptPacketPreflight = scriptPacketPreflight(),
            acceptedDraftPacket = acceptedPacket(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("FAIL", report.sourceBindingVerdict)
        assertEquals("PASS", report.structureVerdict)
        assertTrue(!report.downstreamAllowed)
        assertTrue(report.findings.any { it.ruleId == "SOURCE_BINDING_NOT_PASS" })
        assertTrue(report.sourceBindingFindings.any { it.ruleId == "ACCEPTED_DRAFT_SOURCE_BINDING_MISSING" })
    }

    @Test
    fun structureFailureBlocksBridgeEvenWhenSourceBindingPasses() {
        val report = ScriptPacketDraftStructureBridgeService().validate(
            scriptPacketDraft = boundScriptPacketDraft(breakStructure = true),
            scriptPacketPreflight = scriptPacketPreflight(),
            acceptedDraftPacket = acceptedPacket(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("PASS", report.sourceBindingVerdict)
        assertEquals("FAIL", report.structureVerdict)
        assertTrue(!report.downstreamAllowed)
        assertTrue(report.findings.any { it.ruleId == "SCRIPT_PACKET_STRUCTURE_NOT_ACCEPTABLE" })
        assertTrue(report.structureFindings.any { it.ruleId == "BEAT_VISIBLE_ACTION_MISSING" })
    }

    @Test
    fun structureWarningsAreAllowedButPreserved() {
        val report = ScriptPacketDraftStructureBridgeService().validate(
            scriptPacketDraft = boundScriptPacketDraft(includeFrameCandidates = false),
            scriptPacketPreflight = scriptPacketPreflight(),
            acceptedDraftPacket = acceptedPacket(),
        )

        assertEquals("PASS_WITH_WARNINGS", report.verdict)
        assertEquals("PASS", report.sourceBindingVerdict)
        assertEquals("PASS_WITH_WARNINGS", report.structureVerdict)
        assertTrue(report.downstreamAllowed)
        assertTrue(report.evidence.structureWarningAllowed)
        assertTrue(report.structureFindings.any { it.ruleId == "STORYBOARD_FRAME_CANDIDATES_EMPTY" })
    }

    @Test
    fun cliWritesStructureBridgeReportWithoutDatabase() {
        val tempDir = Files.createTempDirectory("script-packet-structure-bridge")
        val draftPath = tempDir.resolve("script_packet_draft.json")
        val preflightPath = tempDir.resolve("script_packet_preflight.json")
        val acceptedPacketPath = tempDir.resolve("accepted_draft_packet.json")
        val out = tempDir.resolve("structure_bridge_report.json")
        writeJson(draftPath, boundScriptPacketDraft())
        writeJson(preflightPath, scriptPacketPreflight())
        writeJson(acceptedPacketPath, acceptedPacket())

        val exit = Cli().run(
            listOf(
                "validate-script-packet-structure-bridge",
                "--script-packet-draft",
                draftPath.toString(),
                "--script-packet-preflight",
                preflightPath.toString(),
                "--accepted-draft-packet",
                acceptedPacketPath.toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<ScriptPacketDraftStructureBridgeReport>(out)
        assertEquals("PASS", report.verdict)
        assertTrue(report.downstreamAllowed)
        assertEquals("script-packet-draft-sample-001", report.scriptPacketId)
    }

    private fun boundScriptPacketDraft(
        includeBinding: Boolean = true,
        includeAnchors: Boolean = true,
        includeFrameCandidates: Boolean = true,
        breakStructure: Boolean = false,
    ): ScriptPacketDraft {
        val packet = readJson<ScriptPacketDraft>(samplePath("script_packet_draft_sample.json"))
        val accepted = acceptedPacket()
        val bound = packet.copy(
            source = packet.source.copy(
                sourceId = if (includeBinding) accepted.packetId else "neutral-sample-chapter-001",
                acceptedDraftPacketId = if (includeBinding) accepted.packetId else "",
                acceptedDraftContentSha256 = if (includeBinding) accepted.revisedDraftContentSha256 else "",
                acceptedDraftFile = if (includeBinding) accepted.revisedDraftFile else "",
            ),
            storyboardNotes = packet.storyboardNotes.copy(
                continuityLocks = if (includeAnchors) accepted.protectedContinuityAnchors else emptyList(),
                frameCandidates = if (includeFrameCandidates) {
                    packet.storyboardNotes.frameCandidates
                } else {
                    emptyList()
                },
            ),
        )
        if (!breakStructure) return bound

        return bound.copy(
            segments = bound.segments.mapIndexed { segmentIndex, segment ->
                if (segmentIndex != 0) return@mapIndexed segment
                segment.copy(
                    beatList = segment.beatList.mapIndexed { beatIndex, beat ->
                        if (beatIndex == 0) beat.copy(visibleAction = "") else beat
                    },
                )
            },
        )
    }

    private fun scriptPacketPreflight(verdict: String = "PASS"): NovelizationScriptPacketPreflightReport =
        NovelizationScriptPacketPreflightReport(
            verdict = verdict,
            preflightId = "script-packet-preflight-001",
            acceptedDraftPacketId = "accepted-draft-packet-001",
            nextAllowedDownstreamStep = "script_packet_draft",
        )

    private fun acceptedPacket(
        verdict: String = "PASS",
        protectedContinuityAnchors: List<String> = listOf(
            "Mira already paid the door toll before reaching the checkpoint.",
            "The guard requires a visible mark before answering questions.",
        ),
    ): NovelizationAcceptedDraftPacket =
        NovelizationAcceptedDraftPacket(
            verdict = verdict,
            packetId = "accepted-draft-packet-001",
            revisedDraftFile = "drafts/revised.md",
            revisedDraftContentSha256 = "a".repeat(64),
            sourceReports = NovelizationAcceptedDraftSourceReports(
                revisionBriefReportFile = "reports/revision_brief.json",
                revisionBriefVerdict = "PASS",
                revisedDraftIntakeReportFile = "reports/revised_draft_intake.json",
                revisedDraftIntakeVerdict = "PASS",
                continuityAnchorsFile = "reports/continuity_anchors.json",
                continuityAnchorFileId = "continuity-anchors-sample-001",
            ),
            protectedContinuityAnchors = protectedContinuityAnchors,
            nextAllowedDownstreamStep = "script_packet_draft",
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }

    private fun samplePath(fileName: String): Path {
        val candidates = listOf(
            Path.of("samples", fileName),
            Path.of("..", "samples", fileName),
        )
        return candidates.firstOrNull { Files.exists(it) } ?: error("Cannot find sample file: $fileName")
    }
}
