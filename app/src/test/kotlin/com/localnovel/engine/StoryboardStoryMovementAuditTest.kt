package com.localnovel.engine

import com.localnovel.engine.model.AcceptedScriptPacketContinuityAnchorsSummary
import com.localnovel.engine.model.StoryboardPacketAssetCandidateDraft
import com.localnovel.engine.model.StoryboardPacketFrameDraft
import com.localnovel.engine.model.StoryboardPacketDraft
import com.localnovel.engine.model.StoryboardPacketHumanReviewPreflightEvidence
import com.localnovel.engine.model.StoryboardPacketHumanReviewPreflightReport
import com.localnovel.engine.model.StoryboardPacketReviewAssetRequirement
import com.localnovel.engine.model.StoryboardPacketReviewChecklistItem
import com.localnovel.engine.model.StoryboardPacketReviewEvidence
import com.localnovel.engine.model.StoryboardPacketReviewPacket
import com.localnovel.engine.model.StoryboardPacketReviewSourceReports
import com.localnovel.engine.model.StoryboardStoryMovementAuditReport
import com.localnovel.engine.service.StoryboardStoryMovementAuditService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoryboardStoryMovementAuditTest {
    @Test
    fun happyPathHumanReviewReadyStoryboardPassesStoryMovementAudit() {
        val bundle = fixtureBundle()
        val report = audit(bundle)

        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("story_movement_audited", report.nextAllowedDownstreamStep)
        assertEquals("storyboard_story_movement_audit_only", report.downstreamBoundary)
        assertEquals(2, report.frameCount)
        assertEquals(2, report.evidence.uniqueFrameIdCount)
        assertTrue(report.evidence.movementEvidenceCount >= 2)
        assertTrue(report.evidence.openingPressureEvidenceCount >= 1)
        assertTrue(report.evidence.actionEvidenceCount >= 1)
        assertTrue(report.evidence.consequenceOrChoiceEvidenceCount >= 1)
        assertEquals(protectedAnchors.size, report.evidence.coveredContinuityAnchorCount)
        assertEquals(0, report.evidence.forbiddenClaimCount)
        assertFalse(report.nextAllowedDownstreamStep.contains("provider", ignoreCase = true))
        assertFalse(report.nextAllowedDownstreamStep.contains("production", ignoreCase = true))
        assertFalse(report.nextAllowedDownstreamStep.contains("canon", ignoreCase = true))
    }

    @Test
    fun nonPassOrWrongHumanReviewPreflightFailsAudit() {
        val bundle = fixtureBundle()
        val nonPass = audit(bundle.copy(preflight = bundle.preflight.copy(verdict = "FAIL", nextAllowedDownstreamStep = "")))
        val wrongNext = audit(bundle.copy(preflight = bundle.preflight.copy(nextAllowedDownstreamStep = "asset_candidate_preflight")))

        assertEquals("FAIL", nonPass.verdict)
        assertEquals("", nonPass.nextAllowedDownstreamStep)
        assertTrue(nonPass.findings.any { it.ruleId == "HUMAN_REVIEW_PREFLIGHT_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertEquals("", wrongNext.nextAllowedDownstreamStep)
        assertTrue(wrongNext.findings.any { it.ruleId == "HUMAN_REVIEW_PREFLIGHT_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun sourceBindingMismatchFailsAudit() {
        val bundle = fixtureBundle()
        val report = audit(bundle.copy(review = bundle.review.copy(acceptedScriptPacketId = "accepted-script-packet-forged")))

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "ACCEPTED_SCRIPT_PACKET_ID_MISMATCH" })
    }

    @Test
    fun emptyMissingOrDuplicateFrameIdsFailAudit() {
        val bundle = fixtureBundle()
        val empty = audit(bundle.copy(draft = bundle.draft.copy(frames = emptyList()), review = bundle.review.copy(frameIds = emptyList(), frameCount = 0)))
        val missing = audit(bundle.copy(draft = bundle.draft.copy(frames = listOf(frame(frameId = ""))), review = bundle.review.copy(frameIds = listOf(""))))
        val duplicate = audit(
            bundle.copy(
                draft = bundle.draft.copy(frames = listOf(frame(frameId = "frame-001"), frame(frameId = "frame-001"))),
                review = bundle.review.copy(frameIds = listOf("frame-001", "frame-001")),
            ),
        )

        assertEquals("FAIL", empty.verdict)
        assertTrue(empty.findings.any { it.ruleId == "STORYBOARD_FRAMES_MISSING" })
        assertEquals("FAIL", missing.verdict)
        assertTrue(missing.findings.any { it.ruleId == "FRAME_ID_MISSING" })
        assertEquals("FAIL", duplicate.verdict)
        assertTrue(duplicate.findings.any { it.ruleId == "FRAME_ID_DUPLICATE" })
    }

    @Test
    fun staticOnlyFramesWithoutActionChoiceOrConsequenceFailAudit() {
        val staticAnchors = listOf("Blue ribbon remains sealed.")
        val staticFrame = StoryboardPacketFrameDraft(
            frameId = "frame-001",
            sceneReference = "quiet-room",
            beatReference = "beat-pose",
            visualIntent = "A quiet room under soft light.",
            cameraIntent = "Wide view.",
            mustShow = emptyList(),
            doNotShow = listOf("No hidden culprit reveal."),
            continuityAnchors = emptyList(),
            reviewNotes = "Static composition only.",
        )
        val bundle = fixtureBundle(
            anchors = staticAnchors,
            draft = draft(anchors = emptyList(), frames = listOf(staticFrame)),
            review = review(anchors = staticAnchors, frameIds = listOf("frame-001"), frameCount = 1),
            preflight = preflight(frameCount = 1),
        )
        val report = audit(bundle)

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "FRAME_MOVEMENT_EVIDENCE_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "ACTION_BEAT_EVIDENCE_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "CONSEQUENCE_OR_CHOICE_EVIDENCE_MISSING" })
    }

    @Test
    fun missingContinuityAnchorEvidenceFailsAudit() {
        val bundle = fixtureBundle()
        val strippedFrames = listOf(
            frame(
                frameId = "frame-001",
                beatReference = "beat-arrival",
                visualIntent = "Show Arai arriving after checkpoint pressure while a clerk checks a token.",
                cameraIntent = "Medium view of the blocked entrance.",
                mustShow = listOf("The clerk blocks the route before an answer."),
            ).copy(continuityAnchors = emptyList()),
            frame(
                frameId = "frame-002",
                beatReference = "beat-check",
                visualIntent = "Show the clerk requiring a stamped token before giving route information.",
                cameraIntent = "Close view on the stamped token.",
                mustShow = listOf("Arai waits for the route choice."),
            ).copy(continuityAnchors = emptyList()),
        )
        val report = audit(bundle.copy(draft = bundle.draft.copy(protectedContinuityAnchors = emptyList(), frames = strippedFrames)))

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "PROTECTED_CONTINUITY_ANCHOR_NOT_COVERED" })
    }

    @Test
    fun forbiddenDownstreamCompletionClaimFailsAudit() {
        val bundle = fixtureBundle()
        val report = audit(bundle.copy(draft = bundle.draft.copy(reviewNotes = "image generated by provider job")))

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.evidence.forbiddenClaimCount >= 2)
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
    }

    @Test
    fun shortOutputProviderOrApprovalFieldsFailAudit() {
        val bundle = fixtureBundle()

        val imageOutput = audit(bundle.withFirstFrame { it.copy(imageOutputPath = "frame-001.png") })
        val videoOutput = audit(bundle.withFirstFrame { it.copy(videoOutputPath = "shot-001") })
        val generationApi = audit(bundle.withFirstFrame { it.copy(generationApi = "manual-provider") })
        val approvalStatus = audit(bundle.withFirstFrame { it.copy(assetApprovalStatus = "approved") })

        assertEquals("FAIL", imageOutput.verdict)
        assertEquals("", imageOutput.nextAllowedDownstreamStep)
        assertTrue(imageOutput.findings.any { it.ruleId == "IMAGE_OUTPUT_PATH_FORBIDDEN" })
        assertEquals("FAIL", videoOutput.verdict)
        assertEquals("", videoOutput.nextAllowedDownstreamStep)
        assertTrue(videoOutput.findings.any { it.ruleId == "VIDEO_OUTPUT_PATH_FORBIDDEN" })
        assertEquals("FAIL", generationApi.verdict)
        assertEquals("", generationApi.nextAllowedDownstreamStep)
        assertTrue(generationApi.findings.any { it.ruleId == "GENERATION_API_FORBIDDEN" })
        assertEquals("FAIL", approvalStatus.verdict)
        assertEquals("", approvalStatus.nextAllowedDownstreamStep)
        assertTrue(approvalStatus.findings.any { it.ruleId == "ASSET_APPROVAL_STATUS_FORBIDDEN" })
    }

    @Test
    fun cliWritesStoryMovementAuditReportWithoutDatabase() {
        val tempDir = Files.createTempDirectory("storyboard-story-movement-audit")
        val draftPath = tempDir.resolve("storyboard_packet_draft.json")
        val reviewPath = tempDir.resolve("storyboard_packet_review.json")
        val preflightPath = tempDir.resolve("storyboard_human_review_preflight.json")
        val out = tempDir.resolve("storyboard_story_movement_audit.json")
        val bundle = fixtureBundle(
            draftFile = draftPath.toString(),
            reviewFile = reviewPath.toString(),
            preflightFile = preflightPath.toString(),
        )
        writeJson(draftPath, bundle.draft)
        writeJson(reviewPath, bundle.review)
        writeJson(preflightPath, bundle.preflight)

        val exit = Cli().run(
            listOf(
                "audit-storyboard-story-movement",
                "--storyboard-packet-draft",
                draftPath.toString(),
                "--storyboard-packet-review",
                reviewPath.toString(),
                "--storyboard-human-review-preflight",
                preflightPath.toString(),
                "--audit",
                "story-movement-audit-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<StoryboardStoryMovementAuditReport>(out)
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("story-movement-audit-cli-001", report.auditId)
        assertEquals("story_movement_audited", report.nextAllowedDownstreamStep)
    }

    private fun audit(bundle: FixtureBundle): StoryboardStoryMovementAuditReport =
        StoryboardStoryMovementAuditService().audit(
            storyboardPacketDraft = bundle.draft,
            storyboardPacketReview = bundle.review,
            storyboardHumanReviewPreflight = bundle.preflight,
            storyboardPacketDraftFile = bundle.draftFile,
            storyboardPacketReviewFile = bundle.reviewFile,
            storyboardHumanReviewPreflightFile = bundle.preflightFile,
            auditId = "story-movement-audit-001",
        )

    private fun FixtureBundle.withFirstFrame(
        transform: (StoryboardPacketFrameDraft) -> StoryboardPacketFrameDraft,
    ): FixtureBundle =
        copy(draft = draft.copy(frames = listOf(transform(draft.frames.first())) + draft.frames.drop(1)))

    private fun fixtureBundle(
        anchors: List<String> = protectedAnchors,
        draftFile: String = "storyboard_packet_draft.json",
        reviewFile: String = "storyboard_packet_review.json",
        preflightFile: String = "storyboard_human_review_preflight.json",
        draft: StoryboardPacketDraft = draft(anchors = anchors),
        review: StoryboardPacketReviewPacket = review(anchors = anchors),
        preflight: StoryboardPacketHumanReviewPreflightReport = preflight(),
    ): FixtureBundle =
        FixtureBundle(
            draft = draft,
            review = review.copy(storyboardPacketDraftFile = draftFile),
            preflight = preflight.copy(
                storyboardPacketReviewFile = reviewFile,
                storyboardPacketDraftFile = draftFile,
            ),
            draftFile = draftFile,
            reviewFile = reviewFile,
            preflightFile = preflightFile,
        )

    private fun draft(
        anchors: List<String> = protectedAnchors,
        frames: List<StoryboardPacketFrameDraft> = listOf(
            frame(
                frameId = "frame-001",
                beatReference = "beat-arrival",
                visualIntent = "Show Mira arriving after the paid door toll while the guard checks the visible mark.",
                cameraIntent = "Medium shot that keeps the gate, toll token, and visible mark in the same frame.",
                mustShow = listOf(protectedAnchors[0], "The gate guard is still evaluating whether to answer."),
            ),
            frame(
                frameId = "frame-002",
                beatReference = "beat-check",
                visualIntent = "Show the guard requiring the visible mark before giving any route information.",
                cameraIntent = "Close shot on the visible mark and the guard's restrained response.",
                mustShow = listOf(protectedAnchors[1], "Mira keeps the toll token visible but does not receive hidden knowledge."),
            ),
        ),
    ): StoryboardPacketDraft =
        StoryboardPacketDraft(
            packetId = "storyboard-packet-draft-001",
            storyboardPlanningBriefId = "storyboard-planning-brief-001",
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            sourceStoryboardPlanningBriefFile = "storyboard_planning_brief.json",
            protectedContinuityAnchors = anchors,
            frames = frames,
            assetCandidates = listOf(
                StoryboardPacketAssetCandidateDraft(
                    candidateId = "asset-visible-mark",
                    requirement = "candidate only visual reference for the checkpoint mark",
                    sourceFrameIds = listOf("frame-001", "frame-002"),
                    approvalStatus = "candidate_only",
                    reviewNotes = "Candidate reference only for later human review.",
                ),
            ),
            reviewNotes = "Text-only storyboard skeleton for human review readiness.",
        )

    private fun frame(
        frameId: String = "frame-001",
        beatReference: String = "beat-arrival",
        visualIntent: String = "Show Mira arriving after the paid toll while the guard checks the visible mark.",
        cameraIntent: String = "Medium shot clarifying gate distance and visible mark.",
        mustShow: List<String> = listOf(protectedAnchors[0]),
    ): StoryboardPacketFrameDraft =
        StoryboardPacketFrameDraft(
            frameId = frameId,
            sceneReference = "checkpoint-gate",
            beatReference = beatReference,
            visualIntent = visualIntent,
            cameraIntent = cameraIntent,
            mustShow = mustShow,
            doNotShow = listOf("Do not reveal hidden culprit evidence."),
            continuityAnchors = protectedAnchors,
            assetCandidateRefs = listOf("asset-visible-mark"),
            reviewNotes = "Text-only frame entry for human review.",
        )

    private fun review(
        anchors: List<String> = protectedAnchors,
        frameIds: List<String> = listOf("frame-001", "frame-002"),
        frameCount: Int = frameIds.size,
    ): StoryboardPacketReviewPacket =
        StoryboardPacketReviewPacket(
            verdict = "PASS",
            reviewId = "storyboard-review-001",
            reviewStatus = "REVIEW_READY",
            storyboardPacketDraftFile = "storyboard_packet_draft.json",
            storyboardPacketDraftReportFile = "storyboard_packet_draft_report.json",
            storyboardPacketDraftId = "storyboard-packet-draft-001",
            storyboardPlanningBriefId = "storyboard-planning-brief-001",
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            sourceReports = StoryboardPacketReviewSourceReports(
                storyboardPacketDraftValidationReportFile = "storyboard_packet_draft_report.json",
                storyboardPacketDraftValidationVerdict = "PASS",
                storyboardPacketDraftValidationNextStep = "storyboard_packet_review",
            ),
            frameCount = frameCount,
            frameIds = frameIds,
            protectedContinuityAnchorsSummary = AcceptedScriptPacketContinuityAnchorsSummary(
                protectedContinuityAnchorCount = anchors.size,
                protectedContinuityAnchors = anchors,
            ),
            assetCandidateRequirements = listOf(
                StoryboardPacketReviewAssetRequirement(
                    candidateId = "asset-visible-mark",
                    requirement = "candidate-only visual reference for checkpoint mark",
                    sourceFrameIds = frameIds,
                ),
            ),
            remainingResidualRisks = listOf("Text-only packet still waits for human review."),
            reviewChecklist = listOf(
                checklist("scene_lock"),
                checklist("continuity"),
                checklist("knowledge_boundary"),
                checklist("asset_candidates"),
                checklist("downstream_boundary"),
            ),
            nextAllowedDownstreamStep = "storyboard_packet_human_review",
            evidence = StoryboardPacketReviewEvidence(
                draftValidationVerdict = "PASS",
                draftValidationNextAllowedDownstreamStep = "storyboard_packet_review",
                frameCount = frameCount,
                protectedContinuityAnchorCount = anchors.size,
                reviewChecklistCount = 5,
            ),
        )

    private fun preflight(frameCount: Int = 2): StoryboardPacketHumanReviewPreflightReport =
        StoryboardPacketHumanReviewPreflightReport(
            verdict = "PASS",
            preflightId = "storyboard-human-review-preflight-001",
            storyboardPacketReviewFile = "storyboard_packet_review.json",
            storyboardPacketReviewId = "storyboard-review-001",
            storyboardPacketDraftFile = "storyboard_packet_draft.json",
            storyboardPacketDraftReportFile = "storyboard_packet_draft_report.json",
            storyboardPacketDraftId = "storyboard-packet-draft-001",
            storyboardPlanningBriefId = "storyboard-planning-brief-001",
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            reviewStatus = "REVIEW_READY",
            nextAllowedDownstreamStep = "storyboard_packet_human_review_ready",
            evidence = StoryboardPacketHumanReviewPreflightEvidence(
                reviewPacketVerdict = "PASS",
                reviewStatus = "REVIEW_READY",
                reviewNextAllowedDownstreamStep = "storyboard_packet_human_review",
                sourceFieldCount = 12,
                frameCount = frameCount,
                frameIdCount = frameCount,
                reviewChecklistCount = 5,
                protectedContinuityAnchorCount = protectedAnchors.size,
            ),
        )

    private fun checklist(category: String): StoryboardPacketReviewChecklistItem =
        StoryboardPacketReviewChecklistItem(
            itemId = "check-$category",
            category = category,
            requiredCheck = "Confirm $category before human review.",
            status = "required_for_human_review",
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }

    private data class FixtureBundle(
        val draft: StoryboardPacketDraft,
        val review: StoryboardPacketReviewPacket,
        val preflight: StoryboardPacketHumanReviewPreflightReport,
        val draftFile: String,
        val reviewFile: String,
        val preflightFile: String,
    )

    private companion object {
        val protectedAnchors = listOf(
            "Mira has already paid the door toll before reaching the checkpoint.",
            "The guard requires a visible mark before answering questions.",
        )
    }
}
