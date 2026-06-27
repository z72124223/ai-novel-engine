package com.localnovel.engine

import com.localnovel.engine.model.AcceptedScriptPacketContinuityAnchorsSummary
import com.localnovel.engine.model.AcceptedScriptPacketHandoffPacket
import com.localnovel.engine.model.AcceptedScriptPacketSourceReports
import com.localnovel.engine.model.StoryboardPacketAssetCandidateDraft
import com.localnovel.engine.model.StoryboardPacketDraft
import com.localnovel.engine.model.StoryboardPacketFrameDraft
import com.localnovel.engine.model.StoryboardPacketHumanReviewPreflightReport
import com.localnovel.engine.model.StoryboardPacketReviewPacket
import com.localnovel.engine.service.StoryboardPacketDraftValidationService
import com.localnovel.engine.service.StoryboardPacketHumanReviewPreflightService
import com.localnovel.engine.service.StoryboardPacketReviewService
import com.localnovel.engine.service.StoryboardPlanningBriefService
import com.localnovel.engine.service.StoryboardPlanningPreflightService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PipelineHappyPathFixtureTest {
    @Test
    fun acceptedScriptPacketBoundaryReachesHumanReviewableStoryboardSkeleton() {
        val tempDir = Files.createTempDirectory("pipeline-happy-path-fixture")
        val acceptedScriptPacketPath = tempDir.resolve("accepted_script_packet.json")
        val planningPreflightPath = tempDir.resolve("storyboard_planning_preflight.json")
        val planningBriefPath = tempDir.resolve("storyboard_planning_brief.json")
        val storyboardDraftPath = tempDir.resolve("storyboard_packet_draft.json")
        val storyboardDraftValidationPath = tempDir.resolve("storyboard_packet_draft_validation.json")
        val storyboardReviewPath = tempDir.resolve("storyboard_packet_review.json")
        val humanReviewPreflightPath = tempDir.resolve("storyboard_packet_human_review_preflight.json")

        val acceptedScriptPacket = acceptedScriptPacket()
        writeJson(acceptedScriptPacketPath, acceptedScriptPacket)

        val planningPreflight = StoryboardPlanningPreflightService().validate(
            acceptedScriptPacket = acceptedScriptPacket,
            preflightId = "storyboard-preflight-fixture-001",
            acceptedScriptPacketFile = acceptedScriptPacketPath.toString(),
        )
        writeJson(planningPreflightPath, planningPreflight)
        assertEquals("PASS", planningPreflight.verdict, planningPreflight.findings.joinToString { it.ruleId })
        assertEquals("storyboard_planning_brief", planningPreflight.nextAllowedDownstreamStep)

        val planningBrief = StoryboardPlanningBriefService().build(
            acceptedScriptPacket = acceptedScriptPacket,
            storyboardPlanningPreflight = planningPreflight,
            briefId = "storyboard-brief-fixture-001",
            acceptedScriptPacketFile = acceptedScriptPacketPath.toString(),
            storyboardPlanningPreflightFile = planningPreflightPath.toString(),
        )
        writeJson(planningBriefPath, planningBrief)
        assertEquals("PASS", planningBrief.verdict, planningBrief.findings.joinToString { it.ruleId })
        assertEquals("storyboard_packet_draft", planningBrief.nextAllowedDownstreamStep)

        val storyboardDraft = storyboardPacketDraft(
            storyboardPlanningBriefId = planningBrief.briefId,
            acceptedScriptPacketId = planningBrief.acceptedScriptPacketId,
            scriptPacketDraftFile = planningBrief.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = planningBrief.scriptPacketDraftContentSha256,
            sourceStoryboardPlanningBriefFile = planningBriefPath.toString(),
            protectedContinuityAnchors = planningBrief.protectedContinuityAnchorsSummary.protectedContinuityAnchors,
        )
        writeJson(storyboardDraftPath, storyboardDraft)

        val draftValidation = StoryboardPacketDraftValidationService().validate(
            storyboardPlanningBrief = planningBrief,
            storyboardPacketDraft = storyboardDraft,
            storyboardPlanningBriefFile = planningBriefPath.toString(),
            storyboardPacketDraftFile = storyboardDraftPath.toString(),
        )
        writeJson(storyboardDraftValidationPath, draftValidation)
        assertEquals("PASS", draftValidation.verdict, draftValidation.findings.joinToString { it.ruleId })
        assertEquals("storyboard_packet_review", draftValidation.nextAllowedDownstreamStep)

        val storyboardReview = StoryboardPacketReviewService().create(
            storyboardPacketDraft = storyboardDraft,
            storyboardPacketDraftReport = draftValidation,
            reviewId = "storyboard-review-fixture-001",
            storyboardPacketDraftFile = storyboardDraftPath.toString(),
            storyboardPacketDraftReportFile = storyboardDraftValidationPath.toString(),
        )
        writeJson(storyboardReviewPath, storyboardReview)
        assertReviewableStoryboardPacketSkeleton(storyboardReview, acceptedScriptPacket)

        val humanReviewPreflight = StoryboardPacketHumanReviewPreflightService().validate(
            storyboardPacketReview = storyboardReview,
            preflightId = "storyboard-human-review-preflight-fixture-001",
            storyboardPacketReviewFile = storyboardReviewPath.toString(),
        )
        writeJson(humanReviewPreflightPath, humanReviewPreflight)
        assertHumanReviewReadyEndpoint(humanReviewPreflight, storyboardReview, acceptedScriptPacket)

        val reloadedEndpoint = readJson<StoryboardPacketHumanReviewPreflightReport>(humanReviewPreflightPath)
        assertEquals("PASS", reloadedEndpoint.verdict)
        assertEquals("storyboard_packet_human_review_ready", reloadedEndpoint.nextAllowedDownstreamStep)
    }

    private fun assertReviewableStoryboardPacketSkeleton(
        review: StoryboardPacketReviewPacket,
        acceptedScriptPacket: AcceptedScriptPacketHandoffPacket,
    ) {
        assertEquals("PASS", review.verdict)
        assertEquals("REVIEW_READY", review.reviewStatus)
        assertEquals("storyboard_packet_human_review", review.nextAllowedDownstreamStep)
        assertEquals(acceptedScriptPacket.packetId, review.acceptedScriptPacketId)
        assertEquals(acceptedScriptPacket.scriptPacketDraftFile, review.scriptPacketDraftFile)
        assertEquals(acceptedScriptPacket.scriptPacketDraftContentSha256, review.scriptPacketDraftContentSha256)
        assertEquals(protectedAnchors, review.protectedContinuityAnchorsSummary.protectedContinuityAnchors)
        assertEquals(2, review.frameCount)
        assertEquals(listOf("frame-001", "frame-002"), review.frameIds)
        assertTrue(review.reviewChecklist.any { it.category == "downstream_boundary" })
        assertTrue(review.assetCandidateRequirements.all { it.requirement.contains("candidate", ignoreCase = true) })
        assertSafeBoundaryText(
            listOf(
                review.reviewStatus,
                review.nextAllowedDownstreamStep,
                review.assetCandidateRequirements.joinToString(" ") { it.requirement },
                review.remainingResidualRisks.joinToString(" "),
            ),
        )
    }

    private fun assertHumanReviewReadyEndpoint(
        endpoint: StoryboardPacketHumanReviewPreflightReport,
        review: StoryboardPacketReviewPacket,
        acceptedScriptPacket: AcceptedScriptPacketHandoffPacket,
    ) {
        assertEquals("PASS", endpoint.verdict, endpoint.findings.joinToString { it.ruleId })
        assertEquals("storyboard_packet_human_review_ready", endpoint.nextAllowedDownstreamStep)
        assertEquals(review.reviewId, endpoint.storyboardPacketReviewId)
        assertEquals(review.storyboardPacketDraftFile, endpoint.storyboardPacketDraftFile)
        assertEquals(review.storyboardPacketDraftReportFile, endpoint.storyboardPacketDraftReportFile)
        assertEquals(acceptedScriptPacket.packetId, endpoint.acceptedScriptPacketId)
        assertEquals(acceptedScriptPacket.scriptPacketDraftFile, endpoint.scriptPacketDraftFile)
        assertEquals(acceptedScriptPacket.scriptPacketDraftContentSha256, endpoint.scriptPacketDraftContentSha256)
        assertEquals("REVIEW_READY", endpoint.reviewStatus)
        assertEquals(2, endpoint.evidence.frameCount)
        assertEquals(2, endpoint.evidence.protectedContinuityAnchorCount)
        assertEquals(0, endpoint.evidence.forbiddenClaimCount)
        assertFalse(endpoint.nextAllowedDownstreamStep.contains("provider", ignoreCase = true))
        assertFalse(endpoint.nextAllowedDownstreamStep.contains("registry", ignoreCase = true))
        assertFalse(endpoint.nextAllowedDownstreamStep.contains("production", ignoreCase = true))
        assertFalse(endpoint.nextAllowedDownstreamStep.contains("image_generation", ignoreCase = true))
    }

    private fun acceptedScriptPacket(): AcceptedScriptPacketHandoffPacket =
        AcceptedScriptPacketHandoffPacket(
            verdict = "PASS",
            packetId = "accepted-script-packet-fixture-001",
            scriptPacketDraftFile = "script_packet_draft_fixture.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            sourceReports = AcceptedScriptPacketSourceReports(
                structureBridgeReportFile = "script_structure_bridge_report.json",
                structureBridgeVerdict = "PASS",
                sourceBindingVerdict = "PASS",
                structureVerdict = "PASS",
                scriptPacketPreflightReportFile = "script_packet_preflight.json",
                acceptedDraftPacketFile = "accepted_draft_packet.json",
            ),
            protectedContinuityAnchorsSummary = AcceptedScriptPacketContinuityAnchorsSummary(
                protectedContinuityAnchorCount = protectedAnchors.size,
                protectedContinuityAnchors = protectedAnchors,
            ),
            remainingResidualRisks = listOf(
                "Storyboard is text-only and waits for human review before downstream planning.",
            ),
            nextAllowedDownstreamStep = "storyboard_planning_preflight",
        )

    private fun storyboardPacketDraft(
        storyboardPlanningBriefId: String,
        acceptedScriptPacketId: String,
        scriptPacketDraftFile: String,
        scriptPacketDraftContentSha256: String,
        sourceStoryboardPlanningBriefFile: String,
        protectedContinuityAnchors: List<String>,
    ): StoryboardPacketDraft =
        StoryboardPacketDraft(
            packetId = "storyboard-packet-draft-fixture-001",
            storyboardPlanningBriefId = storyboardPlanningBriefId,
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = scriptPacketDraftContentSha256,
            sourceStoryboardPlanningBriefFile = sourceStoryboardPlanningBriefFile,
            protectedContinuityAnchors = protectedContinuityAnchors,
            frames = listOf(
                storyboardFrame(
                    frameId = "frame-001",
                    beatReference = "beat-arrival",
                    visualIntent = "Show Mira arriving after the paid door toll while the guard checks the visible mark.",
                    cameraIntent = "Medium shot that keeps the gate, toll token, and visible mark in the same frame.",
                    mustShow = listOf(protectedAnchors[0], "The gate guard is still evaluating whether to answer."),
                ),
                storyboardFrame(
                    frameId = "frame-002",
                    beatReference = "beat-check",
                    visualIntent = "Show the guard requiring the visible mark before giving any route information.",
                    cameraIntent = "Close shot on the visible mark and the guard's restrained response.",
                    mustShow = listOf(protectedAnchors[1], "Mira keeps the toll token visible but does not receive secret knowledge."),
                ),
            ),
            assetCandidates = listOf(
                StoryboardPacketAssetCandidateDraft(
                    candidateId = "asset-visible-mark",
                    requirement = "candidate only reference for the visible checkpoint mark",
                    sourceFrameIds = listOf("frame-001", "frame-002"),
                    approvalStatus = "candidate_only",
                    reviewNotes = "Needed only as a candidate reference for later human review.",
                ),
            ),
            reviewNotes = "Text-only storyboard skeleton for human review readiness fixture.",
        )

    private fun storyboardFrame(
        frameId: String,
        beatReference: String,
        visualIntent: String,
        cameraIntent: String,
        mustShow: List<String>,
    ): StoryboardPacketFrameDraft =
        StoryboardPacketFrameDraft(
            frameId = frameId,
            sceneReference = "checkpoint-gate",
            beatReference = beatReference,
            visualIntent = visualIntent,
            cameraIntent = cameraIntent,
            mustShow = mustShow,
            doNotShow = listOf("Do not show author-only culprit evidence.", "Keep this frame text-only for review."),
            continuityAnchors = protectedAnchors,
            assetCandidateRefs = listOf("asset-visible-mark"),
            reviewNotes = "Text-only frame entry for human review.",
        )

    private fun assertSafeBoundaryText(values: List<String>) {
        val combined = values.joinToString("\n")
        val forbidden = listOf(
            "approved_reference",
            "registered_reference",
            "asset approval",
            "production lock",
            "production_locked",
            "canon",
            "image generated",
            "video generated",
            "provider job",
            "runtime scene created",
        )
        forbidden.forEach { term ->
            assertFalse(combined.contains(term, ignoreCase = true), "Forbidden boundary term leaked: $term")
        }
    }

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
