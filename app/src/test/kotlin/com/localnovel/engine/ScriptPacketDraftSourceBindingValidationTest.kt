package com.localnovel.engine

import com.localnovel.engine.model.NovelizationAcceptedDraftPacket
import com.localnovel.engine.model.NovelizationAcceptedDraftSourceReports
import com.localnovel.engine.model.NovelizationScriptPacketPreflightReport
import com.localnovel.engine.model.ScriptPacketDraft
import com.localnovel.engine.model.ScriptPacketDraftSourceBindingReport
import com.localnovel.engine.service.ScriptPacketDraftSourceBindingValidationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScriptPacketDraftSourceBindingValidationTest {
    @Test
    fun boundDraftPassesSourceBindingGate() {
        val report = ScriptPacketDraftSourceBindingValidationService().validate(
            scriptPacketDraft = boundScriptPacketDraft(),
            scriptPacketPreflight = scriptPacketPreflight(),
            acceptedDraftPacket = acceptedPacket(),
        )

        assertEquals("PASS", report.verdict)
        assertEquals("script-packet-draft-sample-001", report.scriptPacketId)
        assertEquals("accepted-draft-packet-001", report.acceptedDraftPacketId)
        assertTrue(report.evidence.bindingMatchedBy.contains("acceptedDraftPacketId"))
        assertTrue(report.evidence.bindingMatchedBy.contains("revisedDraftContentSha256"))
        assertTrue(report.evidence.bindingMatchedBy.contains("revisedDraftFile"))
        assertEquals(2, report.evidence.referencedContinuityAnchorCount)
    }

    @Test
    fun failedUpstreamReportsBlockSourceBindingGate() {
        val report = ScriptPacketDraftSourceBindingValidationService().validate(
            scriptPacketDraft = boundScriptPacketDraft(),
            scriptPacketPreflight = scriptPacketPreflight(verdict = "FAIL"),
            acceptedDraftPacket = acceptedPacket(verdict = "FAIL"),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "SCRIPT_PACKET_PREFLIGHT_NOT_PASS" })
        assertTrue(report.findings.any { it.ruleId == "ACCEPTED_DRAFT_PACKET_NOT_PASS" })
    }

    @Test
    fun missingAcceptedDraftBindingFailsGate() {
        val report = ScriptPacketDraftSourceBindingValidationService().validate(
            scriptPacketDraft = boundScriptPacketDraft(includeBinding = false),
            scriptPacketPreflight = scriptPacketPreflight(),
            acceptedDraftPacket = acceptedPacket(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "ACCEPTED_DRAFT_SOURCE_BINDING_MISSING" })
    }

    @Test
    fun missingProtectedContinuityAnchorReferenceFailsGate() {
        val draft = boundScriptPacketDraft(includeAnchors = false)

        val report = ScriptPacketDraftSourceBindingValidationService().validate(
            scriptPacketDraft = draft,
            scriptPacketPreflight = scriptPacketPreflight(),
            acceptedDraftPacket = acceptedPacket(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "PROTECTED_CONTINUITY_ANCHOR_NOT_REFERENCED" })
    }

    @Test
    fun missingPureChineseProtectedContinuityAnchorFailsGate() {
        val report = ScriptPacketDraftSourceBindingValidationService().validate(
            scriptPacketDraft = boundScriptPacketDraft(includeAnchors = false),
            scriptPacketPreflight = scriptPacketPreflight(),
            acceptedDraftPacket = acceptedPacket(
                protectedContinuityAnchors = listOf("米拉已經付過門費才抵達關卡"),
            ),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "PROTECTED_CONTINUITY_ANCHOR_NOT_REFERENCED" })
    }

    @Test
    fun referencedPureChineseProtectedContinuityAnchorPassesGate() {
        val protectedAnchors = listOf("米拉已經付過門費才抵達關卡")
        val draft = boundScriptPacketDraft(includeAnchors = false).let { packet ->
            packet.copy(
                storyboardNotes = packet.storyboardNotes.copy(
                    continuityLocks = listOf("劇本包保留：米拉已經付過門費才抵達關卡。"),
                ),
            )
        }

        val report = ScriptPacketDraftSourceBindingValidationService().validate(
            scriptPacketDraft = draft,
            scriptPacketPreflight = scriptPacketPreflight(),
            acceptedDraftPacket = acceptedPacket(protectedContinuityAnchors = protectedAnchors),
        )

        assertEquals("PASS", report.verdict)
        assertEquals(1, report.evidence.referencedContinuityAnchorCount)
    }

    @Test
    fun downstreamApprovalClaimsFailGate() {
        val draft = boundScriptPacketDraft().copy(
            reviewSummary = boundScriptPacketDraft().reviewSummary.copy(
                verdict = "final",
                reviewerNotes = "This script packet is canon and ready as final video.",
            ),
        )

        val report = ScriptPacketDraftSourceBindingValidationService().validate(
            scriptPacketDraft = draft,
            scriptPacketPreflight = scriptPacketPreflight(),
            acceptedDraftPacket = acceptedPacket(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_APPROVAL_CLAIM_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "SCRIPT_PACKET_DRAFT_REVIEW_CLAIMS_APPROVAL" })
    }

    @Test
    fun cliWritesSourceBindingReportWithoutDatabase() {
        val tempDir = Files.createTempDirectory("script-packet-source-binding")
        val draftPath = tempDir.resolve("script_packet_draft.json")
        val preflightPath = tempDir.resolve("script_packet_preflight.json")
        val acceptedPacketPath = tempDir.resolve("accepted_draft_packet.json")
        val out = tempDir.resolve("source_binding_report.json")
        writeJson(draftPath, boundScriptPacketDraft())
        writeJson(preflightPath, scriptPacketPreflight())
        writeJson(acceptedPacketPath, acceptedPacket())

        val exit = Cli().run(
            listOf(
                "validate-script-packet-source-binding",
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
        val report = readJson<ScriptPacketDraftSourceBindingReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("script-packet-draft-sample-001", report.scriptPacketId)
        assertEquals("accepted-draft-packet-001", report.acceptedDraftPacketId)
    }

    private fun boundScriptPacketDraft(
        includeBinding: Boolean = true,
        includeAnchors: Boolean = true,
    ): ScriptPacketDraft {
        val packet = readJson<ScriptPacketDraft>(samplePath("script_packet_draft_sample.json"))
        val accepted = acceptedPacket()
        return packet.copy(
            source = packet.source.copy(
                sourceId = if (includeBinding) accepted.packetId else "neutral-sample-chapter-001",
                acceptedDraftPacketId = if (includeBinding) accepted.packetId else "",
                acceptedDraftContentSha256 = if (includeBinding) accepted.revisedDraftContentSha256 else "",
                acceptedDraftFile = if (includeBinding) accepted.revisedDraftFile else "",
            ),
            storyboardNotes = packet.storyboardNotes.copy(
                continuityLocks = if (includeAnchors) accepted.protectedContinuityAnchors else emptyList(),
            ),
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
