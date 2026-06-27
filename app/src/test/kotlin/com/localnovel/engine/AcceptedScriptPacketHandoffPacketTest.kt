package com.localnovel.engine

import com.localnovel.engine.model.AcceptedScriptPacketHandoffPacket
import com.localnovel.engine.model.NovelizationAcceptedDraftPacket
import com.localnovel.engine.model.NovelizationAcceptedDraftSourceReports
import com.localnovel.engine.model.NovelizationScriptPacketPreflightReport
import com.localnovel.engine.model.ScriptPacketDraft
import com.localnovel.engine.model.ScriptPacketDraftStructureBridgeReport
import com.localnovel.engine.service.AcceptedScriptPacketHandoffPacketService
import com.localnovel.engine.service.ScriptPacketDraftStructureBridgeService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AcceptedScriptPacketHandoffPacketTest {
    @Test
    fun passingStructureBridgeBuildsAcceptedScriptPacketHandoff() {
        val draft = boundScriptPacketDraft()
        val draftFile = "script_packet_draft.json"
        val bridge = bridgeReport(draft, draftFile)

        val packet = AcceptedScriptPacketHandoffPacketService().build(
            scriptPacketDraftText = JsonSupport.json.encodeToString(draft),
            scriptPacketDraft = draft,
            structureBridgeReport = bridge,
            scriptPacketDraftFile = draftFile,
            structureBridgeReportFile = "structure_bridge_report.json",
        )

        assertEquals("PASS", packet.verdict)
        assertEquals("accepted-script-packet-script-packet-draft-sample-001", packet.packetId)
        assertTrue(packet.scriptPacketDraftContentSha256.matches(Regex("[0-9a-f]{64}")))
        assertEquals("structure_bridge_report.json", packet.sourceReports.structureBridgeReportFile)
        assertEquals("PASS", packet.sourceReports.structureBridgeVerdict)
        assertEquals("PASS", packet.sourceReports.sourceBindingVerdict)
        assertEquals("PASS", packet.sourceReports.structureVerdict)
        assertEquals("script_packet_preflight.json", packet.sourceReports.scriptPacketPreflightReportFile)
        assertEquals("accepted_draft_packet.json", packet.sourceReports.acceptedDraftPacketFile)
        assertEquals(2, packet.protectedContinuityAnchorsSummary.protectedContinuityAnchorCount)
        assertTrue(packet.remainingResidualRisks.isNotEmpty())
        assertEquals("storyboard_planning_preflight", packet.nextAllowedDownstreamStep)
    }

    @Test
    fun failedBridgeBlocksAcceptedScriptPacketHandoff() {
        val draft = boundScriptPacketDraft()
        val draftFile = "script_packet_draft.json"
        val bridge = bridgeReport(draft, draftFile).copy(
            verdict = "FAIL",
            downstreamAllowed = false,
            blockedReason = "source-binding verdict is FAIL",
        )

        val packet = AcceptedScriptPacketHandoffPacketService().build(
            scriptPacketDraftText = JsonSupport.json.encodeToString(draft),
            scriptPacketDraft = draft,
            structureBridgeReport = bridge,
            scriptPacketDraftFile = draftFile,
            structureBridgeReportFile = "structure_bridge_report.json",
        )

        assertEquals("FAIL", packet.verdict)
        assertEquals("", packet.nextAllowedDownstreamStep)
        assertTrue(packet.findings.any { it.ruleId == "STRUCTURE_BRIDGE_NOT_PASS" })
        assertTrue(packet.findings.any { it.ruleId == "STRUCTURE_BRIDGE_DOWNSTREAM_BLOCKED" })
    }

    @Test
    fun downstreamAllowedFalseBlocksAcceptedScriptPacketHandoff() {
        val draft = boundScriptPacketDraft()
        val draftFile = "script_packet_draft.json"
        val bridge = bridgeReport(draft, draftFile).copy(
            downstreamAllowed = false,
            blockedReason = "manual review blocked downstream",
        )

        val packet = AcceptedScriptPacketHandoffPacketService().build(
            scriptPacketDraftText = JsonSupport.json.encodeToString(draft),
            scriptPacketDraft = draft,
            structureBridgeReport = bridge,
            scriptPacketDraftFile = draftFile,
            structureBridgeReportFile = "structure_bridge_report.json",
        )

        assertEquals("FAIL", packet.verdict)
        assertEquals("", packet.nextAllowedDownstreamStep)
        assertTrue(packet.findings.any { it.ruleId == "STRUCTURE_BRIDGE_DOWNSTREAM_BLOCKED" })
    }

    @Test
    fun forbiddenDownstreamCompletionClaimsBlockPacket() {
        val cleanDraft = boundScriptPacketDraft()
        val draft = cleanDraft.copy(
            reviewSummary = cleanDraft.reviewSummary.copy(
                verdict = "unreviewed",
                reviewerNotes = "Storyboard generated, approved asset list ready, and final video ready.",
            ),
        )
        val draftFile = "script_packet_draft.json"
        val bridge = bridgeReport(cleanDraft, draftFile)

        val packet = AcceptedScriptPacketHandoffPacketService().build(
            scriptPacketDraftText = JsonSupport.json.encodeToString(draft),
            scriptPacketDraft = draft,
            structureBridgeReport = bridge,
            scriptPacketDraftFile = draftFile,
            structureBridgeReportFile = "structure_bridge_report.json",
        )

        assertEquals("FAIL", packet.verdict)
        assertEquals("", packet.nextAllowedDownstreamStep)
        assertTrue(packet.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
        assertTrue(packet.evidence.forbiddenClaimCount > 0)
    }

    @Test
    fun forbiddenClaimsInPreviouslyUnscannedFieldsBlockPacket() {
        val cleanDraft = boundScriptPacketDraft()
        val segment = cleanDraft.segments.first()
        val draft = cleanDraft.copy(
            segments = listOf(
                segment.copy(
                    visualSetup = segment.visualSetup.copy(
                        mustShow = segment.visualSetup.mustShow + "image generated",
                    ),
                    knowledgePass = segment.knowledgePass.copy(
                        publicFacts = segment.knowledgePass.publicFacts + "final video ready",
                    ),
                    interactionChoices = segment.interactionChoices.mapIndexed { index, choice ->
                        if (index == 0) {
                            choice.copy(allowedBecause = "setting pack canon says this is already approved")
                        } else {
                            choice
                        }
                    },
                ),
            ) + cleanDraft.segments.drop(1),
            storyboardNotes = cleanDraft.storyboardNotes.copy(
                frameCandidates = cleanDraft.storyboardNotes.frameCandidates.mapIndexed { index, frame ->
                    if (index == 0) {
                        frame.copy(doNotShow = frame.doNotShow + "storyboard generated")
                    } else {
                        frame
                    }
                },
            ),
        )
        val draftFile = "script_packet_draft.json"
        val bridge = bridgeReport(cleanDraft, draftFile)

        val packet = AcceptedScriptPacketHandoffPacketService().build(
            scriptPacketDraftText = JsonSupport.json.encodeToString(draft),
            scriptPacketDraft = draft,
            structureBridgeReport = bridge,
            scriptPacketDraftFile = draftFile,
            structureBridgeReportFile = "structure_bridge_report.json",
        )

        assertEquals("FAIL", packet.verdict)
        assertEquals("", packet.nextAllowedDownstreamStep)
        assertTrue(packet.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
        assertTrue(packet.findings.any { it.path.contains("visualSetup.mustShow") })
        assertTrue(packet.findings.any { it.path.contains("knowledgePass.publicFacts") })
        assertTrue(packet.findings.any { it.path.contains("interactionChoices[0].allowedBecause") })
        assertTrue(packet.findings.any { it.path.contains("frameCandidates[0].doNotShow") })
    }

    @Test
    fun assetApprovalWordingBlocksPacket() {
        val cleanDraft = boundScriptPacketDraft()
        val draft = cleanDraft.copy(
            assetNeeds = cleanDraft.assetNeeds.mapIndexed { index, asset ->
                if (index == 0) {
                    asset.copy(candidateReason = "asset approval complete; assets approved for final handoff.")
                } else {
                    asset
                }
            },
        )
        val draftFile = "script_packet_draft.json"
        val bridge = bridgeReport(cleanDraft, draftFile)

        val packet = AcceptedScriptPacketHandoffPacketService().build(
            scriptPacketDraftText = JsonSupport.json.encodeToString(draft),
            scriptPacketDraft = draft,
            structureBridgeReport = bridge,
            scriptPacketDraftFile = draftFile,
            structureBridgeReportFile = "structure_bridge_report.json",
        )

        assertEquals("FAIL", packet.verdict)
        assertEquals("", packet.nextAllowedDownstreamStep)
        assertTrue(packet.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
        assertTrue(packet.findings.any { it.path.contains("assetNeeds[0].candidateReason") })
        assertTrue(packet.findings.any { it.evidence.any { evidence -> evidence.contains("asset approval complete") } })
    }

    @Test
    fun cliWritesAcceptedScriptPacketHandoffWithoutDatabase() {
        val tempDir = Files.createTempDirectory("accepted-script-packet-handoff")
        val draftPath = tempDir.resolve("script_packet_draft.json")
        val bridgePath = tempDir.resolve("structure_bridge_report.json")
        val out = tempDir.resolve("accepted_script_packet_handoff.json")
        val draft = boundScriptPacketDraft()
        writeJson(draftPath, draft)
        writeJson(bridgePath, bridgeReport(draft, draftPath.toString()))

        val exit = Cli().run(
            listOf(
                "create-accepted-script-packet-handoff-packet",
                "--script-packet-draft",
                draftPath.toString(),
                "--structure-bridge-report",
                bridgePath.toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val packet = readJson<AcceptedScriptPacketHandoffPacket>(out)
        assertEquals("PASS", packet.verdict)
        assertEquals("storyboard_planning_preflight", packet.nextAllowedDownstreamStep)
        assertTrue(packet.scriptPacketDraftContentSha256.matches(Regex("[0-9a-f]{64}")))
        assertEquals(draftPath.toString(), packet.scriptPacketDraftFile)
    }

    private fun bridgeReport(
        draft: ScriptPacketDraft,
        draftFile: String,
    ): ScriptPacketDraftStructureBridgeReport =
        ScriptPacketDraftStructureBridgeService().validate(
            scriptPacketDraft = draft,
            scriptPacketPreflight = scriptPacketPreflight(),
            acceptedDraftPacket = acceptedPacket(),
            scriptPacketDraftFile = draftFile,
            scriptPacketPreflightFile = "script_packet_preflight.json",
            acceptedDraftPacketFile = "accepted_draft_packet.json",
        )

    private fun boundScriptPacketDraft(): ScriptPacketDraft {
        val packet = readJson<ScriptPacketDraft>(samplePath("script_packet_draft_sample.json"))
        val accepted = acceptedPacket()
        return packet.copy(
            source = packet.source.copy(
                sourceId = accepted.packetId,
                acceptedDraftPacketId = accepted.packetId,
                acceptedDraftContentSha256 = accepted.revisedDraftContentSha256,
                acceptedDraftFile = accepted.revisedDraftFile,
            ),
            storyboardNotes = packet.storyboardNotes.copy(
                continuityLocks = accepted.protectedContinuityAnchors,
            ),
        )
    }

    private fun scriptPacketPreflight(): NovelizationScriptPacketPreflightReport =
        NovelizationScriptPacketPreflightReport(
            verdict = "PASS",
            preflightId = "script-packet-preflight-001",
            acceptedDraftPacketId = "accepted-draft-packet-001",
            nextAllowedDownstreamStep = "script_packet_draft",
        )

    private fun acceptedPacket(): NovelizationAcceptedDraftPacket =
        NovelizationAcceptedDraftPacket(
            verdict = "PASS",
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
            protectedContinuityAnchors = listOf(
                "Mira already paid the door toll before reaching the checkpoint.",
                "The guard requires a visible mark before answering questions.",
            ),
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
