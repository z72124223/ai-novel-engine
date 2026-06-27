package com.localnovel.engine

import com.localnovel.engine.model.StoryboardPacketAssetCandidateDraft
import com.localnovel.engine.model.StoryboardPacketDraft
import com.localnovel.engine.model.StoryboardPacketDraftValidationEvidence
import com.localnovel.engine.model.StoryboardPacketDraftValidationReport
import com.localnovel.engine.model.StoryboardPacketFrameDraft
import com.localnovel.engine.model.StoryboardPacketReviewPacket
import com.localnovel.engine.service.StoryboardPacketReviewService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoryboardPacketReviewTest {
    @Test
    fun passDraftAndValidationReportBuildReviewReadyPacket() {
        val packet = StoryboardPacketReviewService().create(
            storyboardPacketDraft = storyboardPacketDraft(),
            storyboardPacketDraftReport = validationReport(),
            reviewId = "storyboard-review-001",
            storyboardPacketDraftFile = "storyboard_packet_draft.json",
            storyboardPacketDraftReportFile = "storyboard_packet_draft_report.json",
        )

        assertEquals("PASS", packet.verdict)
        assertEquals("REVIEW_READY", packet.reviewStatus)
        assertEquals("storyboard-review-001", packet.reviewId)
        assertEquals("storyboard_packet_human_review", packet.nextAllowedDownstreamStep)
        assertEquals(1, packet.frameCount)
        assertEquals(listOf("frame-001"), packet.frameIds)
        assertEquals(2, packet.protectedContinuityAnchorsSummary.protectedContinuityAnchorCount)
        assertEquals(1, packet.assetCandidateRequirements.size)
        assertTrue(packet.remainingResidualRisks.isNotEmpty())
        assertTrue(packet.reviewChecklist.isNotEmpty())

        val encoded = JsonSupport.json.encodeToString(packet).lowercase()
        assertFalse(encoded.contains("approved"))
        assertFalse(encoded.contains("locked"))
        assertFalse(encoded.contains("final"))
        assertFalse(encoded.contains("canon"))
    }

    @Test
    fun nonPassValidationReportFailsReviewPacket() {
        val packet = StoryboardPacketReviewService().create(
            storyboardPacketDraft = storyboardPacketDraft(),
            storyboardPacketDraftReport = validationReport(verdict = "FAIL", nextAllowedDownstreamStep = ""),
            storyboardPacketDraftFile = "storyboard_packet_draft.json",
            storyboardPacketDraftReportFile = "storyboard_packet_draft_report.json",
        )

        assertEquals("FAIL", packet.verdict)
        assertEquals("FAIL", packet.reviewStatus)
        assertEquals("", packet.nextAllowedDownstreamStep)
        assertTrue(packet.findings.any { it.ruleId == "DRAFT_VALIDATION_REPORT_NOT_PASS" })
    }

    @Test
    fun wrongValidationReportNextStepFailsReviewPacket() {
        val packet = StoryboardPacketReviewService().create(
            storyboardPacketDraft = storyboardPacketDraft(),
            storyboardPacketDraftReport = validationReport(nextAllowedDownstreamStep = "asset_candidate_preflight"),
            storyboardPacketDraftFile = "storyboard_packet_draft.json",
            storyboardPacketDraftReportFile = "storyboard_packet_draft_report.json",
        )

        assertEquals("FAIL", packet.verdict)
        assertTrue(packet.findings.any { it.ruleId == "DRAFT_REPORT_NEXT_STEP_NOT_REVIEW" })
    }

    @Test
    fun sourceBindingMissingOrMismatchFailsReviewPacket() {
        val missing = StoryboardPacketReviewService().create(
            storyboardPacketDraft = storyboardPacketDraft(packetId = ""),
            storyboardPacketDraftReport = validationReport(),
            storyboardPacketDraftFile = "storyboard_packet_draft.json",
            storyboardPacketDraftReportFile = "storyboard_packet_draft_report.json",
        )
        val mismatch = StoryboardPacketReviewService().create(
            storyboardPacketDraft = storyboardPacketDraft(scriptPacketDraftContentSha256 = "b".repeat(64)),
            storyboardPacketDraftReport = validationReport(),
            storyboardPacketDraftFile = "storyboard_packet_draft.json",
            storyboardPacketDraftReportFile = "storyboard_packet_draft_report.json",
        )

        assertEquals("FAIL", missing.verdict)
        assertTrue(missing.findings.any { it.ruleId == "STORYBOARD_PACKET_DRAFT_ID_MISSING" })
        assertEquals("FAIL", mismatch.verdict)
        assertTrue(mismatch.findings.any { it.ruleId == "SCRIPT_PACKET_DRAFT_HASH_MISMATCH" })
    }

    @Test
    fun blankValidationReportDraftFileFailsReviewPacket() {
        val packet = StoryboardPacketReviewService().create(
            storyboardPacketDraft = storyboardPacketDraft(),
            storyboardPacketDraftReport = validationReport(storyboardPacketDraftFile = ""),
            storyboardPacketDraftFile = "storyboard_packet_draft.json",
            storyboardPacketDraftReportFile = "storyboard_packet_draft_report.json",
        )

        assertEquals("FAIL", packet.verdict)
        assertTrue(packet.findings.any { it.ruleId == "DRAFT_REPORT_DRAFT_FILE_MISSING" })
    }

    @Test
    fun mismatchedValidationReportDraftFileFailsReviewPacket() {
        val packet = StoryboardPacketReviewService().create(
            storyboardPacketDraft = storyboardPacketDraft(),
            storyboardPacketDraftReport = validationReport(storyboardPacketDraftFile = "other.json"),
            storyboardPacketDraftFile = "storyboard_packet_draft.json",
            storyboardPacketDraftReportFile = "storyboard_packet_draft_report.json",
        )

        assertEquals("FAIL", packet.verdict)
        assertTrue(packet.findings.any { it.ruleId == "REVIEW_INPUT_DRAFT_FILE_MISMATCH" })
    }

    @Test
    fun forbiddenDownstreamCompletionClaimFailsReviewPacket() {
        val packet = StoryboardPacketReviewService().create(
            storyboardPacketDraft = storyboardPacketDraft(reviewNotes = "image generated and production locked"),
            storyboardPacketDraftReport = validationReport(),
            storyboardPacketDraftFile = "storyboard_packet_draft.json",
            storyboardPacketDraftReportFile = "storyboard_packet_draft_report.json",
        )

        assertEquals("FAIL", packet.verdict)
        assertTrue(packet.evidence.forbiddenClaimCount >= 2)
        assertTrue(packet.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
    }

    @Test
    fun shortOutputApiOrApprovalValuesFailReviewPacket() {
        val packet = StoryboardPacketReviewService().create(
            storyboardPacketDraft = storyboardPacketDraft(
                frames = listOf(
                    storyboardFrame(
                        imageOutputPath = "frame-001",
                        videoOutputPath = "shot-001",
                        generationApi = "manual-provider-job",
                        assetApprovalStatus = "approved",
                    ),
                ),
                assetCandidates = listOf(
                    StoryboardPacketAssetCandidateDraft(
                        candidateId = "asset-door-mark",
                        requirement = "candidate only: visible mark reference",
                        approvalStatus = "approved",
                    ),
                ),
            ),
            storyboardPacketDraftReport = validationReport(),
            storyboardPacketDraftFile = "storyboard_packet_draft.json",
            storyboardPacketDraftReportFile = "storyboard_packet_draft_report.json",
        )

        assertEquals("FAIL", packet.verdict)
        assertTrue(packet.findings.any { it.ruleId == "IMAGE_OUTPUT_PATH_FORBIDDEN" })
        assertTrue(packet.findings.any { it.ruleId == "VIDEO_OUTPUT_PATH_FORBIDDEN" })
        assertTrue(packet.findings.any { it.ruleId == "GENERATION_API_FORBIDDEN" })
        assertTrue(packet.findings.any { it.ruleId == "ASSET_APPROVAL_STATUS_FORBIDDEN" })
    }

    @Test
    fun cliWritesStoryboardPacketReviewWithoutDatabase() {
        val tempDir = Files.createTempDirectory("storyboard-packet-review")
        val draftPath = tempDir.resolve("storyboard_packet_draft.json")
        val reportPath = tempDir.resolve("storyboard_packet_draft_report.json")
        val out = tempDir.resolve("storyboard_packet_review.json")
        writeJson(draftPath, storyboardPacketDraft())
        writeJson(reportPath, validationReport(storyboardPacketDraftFile = draftPath.toString()))

        val exit = Cli().run(
            listOf(
                "create-storyboard-packet-review",
                "--storyboard-packet-draft",
                draftPath.toString(),
                "--storyboard-packet-draft-report",
                reportPath.toString(),
                "--review",
                "storyboard-review-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val packet = readJson<StoryboardPacketReviewPacket>(out)
        assertEquals("PASS", packet.verdict)
        assertEquals("REVIEW_READY", packet.reviewStatus)
        assertEquals("storyboard-review-cli-001", packet.reviewId)
        assertEquals("storyboard_packet_human_review", packet.nextAllowedDownstreamStep)
        assertEquals(draftPath.toString(), packet.storyboardPacketDraftFile)
        assertEquals(reportPath.toString(), packet.storyboardPacketDraftReportFile)
    }

    private fun storyboardPacketDraft(
        packetId: String = "storyboard-packet-draft-001",
        storyboardPlanningBriefId: String = "storyboard-brief-001",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        scriptPacketDraftFile: String = "script_packet_draft.json",
        scriptPacketDraftContentSha256: String = "a".repeat(64),
        protectedContinuityAnchors: List<String> = protectedAnchors,
        frames: List<StoryboardPacketFrameDraft> = listOf(storyboardFrame()),
        assetCandidates: List<StoryboardPacketAssetCandidateDraft> = listOf(
            StoryboardPacketAssetCandidateDraft(
                candidateId = "asset-door-mark",
                requirement = "candidate only: visible mark reference",
                sourceFrameIds = listOf("frame-001"),
            ),
        ),
        reviewNotes: String = "Text-only storyboard packet draft for review handoff.",
    ): StoryboardPacketDraft =
        StoryboardPacketDraft(
            packetId = packetId,
            storyboardPlanningBriefId = storyboardPlanningBriefId,
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = scriptPacketDraftContentSha256,
            sourceStoryboardPlanningBriefFile = "storyboard_planning_brief.json",
            protectedContinuityAnchors = protectedContinuityAnchors,
            frames = frames,
            assetCandidates = assetCandidates,
            reviewNotes = reviewNotes,
        )

    private fun storyboardFrame(
        frameId: String = "frame-001",
        imageOutputPath: String = "",
        videoOutputPath: String = "",
        generationApi: String = "",
        assetApprovalStatus: String = "",
    ): StoryboardPacketFrameDraft =
        StoryboardPacketFrameDraft(
            frameId = frameId,
            sceneReference = "checkpoint-gate",
            beatReference = "beat-001",
            visualIntent = "Show Mira arriving after the paid toll while the guard checks the visible mark.",
            cameraIntent = "Medium shot clarifying gate distance and visible mark.",
            mustShow = listOf("Mira has already paid the door toll before reaching the checkpoint."),
            doNotShow = listOf("Do not reveal author-only culprit evidence."),
            continuityAnchors = protectedAnchors,
            assetCandidateRefs = listOf("asset-door-mark"),
            reviewNotes = "Text-only frame for handoff.",
            imageOutputPath = imageOutputPath,
            videoOutputPath = videoOutputPath,
            generationApi = generationApi,
            assetApprovalStatus = assetApprovalStatus,
        )

    private fun validationReport(
        verdict: String = "PASS",
        storyboardPacketDraftFile: String = "storyboard_packet_draft.json",
        storyboardPacketDraftId: String = "storyboard-packet-draft-001",
        storyboardPlanningBriefId: String = "storyboard-brief-001",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        scriptPacketDraftFile: String = "script_packet_draft.json",
        scriptPacketDraftContentSha256: String = "a".repeat(64),
        nextAllowedDownstreamStep: String = "storyboard_packet_review",
    ): StoryboardPacketDraftValidationReport =
        StoryboardPacketDraftValidationReport(
            verdict = verdict,
            storyboardPlanningBriefFile = "storyboard_planning_brief.json",
            storyboardPacketDraftFile = storyboardPacketDraftFile,
            storyboardPlanningBriefId = storyboardPlanningBriefId,
            storyboardPacketDraftId = storyboardPacketDraftId,
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = scriptPacketDraftContentSha256,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
            evidence = StoryboardPacketDraftValidationEvidence(
                frameCount = 1,
                protectedContinuityAnchorCount = 2,
                protectedContinuityAnchorReferencedCount = 2,
            ),
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }

    private companion object {
        val protectedAnchors = listOf(
            "Mira has already paid the door toll before reaching the checkpoint.",
            "The guard requires a visible mark before answering questions.",
        )
    }
}
