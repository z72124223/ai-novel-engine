package com.localnovel.engine

import com.localnovel.engine.model.AcceptedScriptPacketContinuityAnchorsSummary
import com.localnovel.engine.model.ReviewableStoryboardPacket
import com.localnovel.engine.model.StoryboardPacketAssetCandidateDraft
import com.localnovel.engine.model.StoryboardPacketDraft
import com.localnovel.engine.model.StoryboardPacketFrameDraft
import com.localnovel.engine.model.StoryboardPacketHumanReviewPreflightEvidence
import com.localnovel.engine.model.StoryboardPacketHumanReviewPreflightReport
import com.localnovel.engine.model.StoryboardPacketReviewAssetRequirement
import com.localnovel.engine.model.StoryboardPacketReviewChecklistItem
import com.localnovel.engine.model.StoryboardPacketReviewEvidence
import com.localnovel.engine.model.StoryboardPacketReviewPacket
import com.localnovel.engine.model.StoryboardPacketReviewSourceReports
import com.localnovel.engine.model.StoryboardStoryMovementAuditReport
import com.localnovel.engine.service.ReviewableStoryboardPacketService
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

class ReviewableStoryboardPacketTest {
    @Test
    fun happyPathAuditedStoryboardChainCreatesReviewablePacket() {
        val bundle = fixtureBundle()
        val packet = createPacket(bundle)

        assertEquals("PASS", packet.verdict, packet.findings.joinToString { it.ruleId })
        assertEquals("reviewable-storyboard-packet-001", packet.packetId)
        assertEquals("reviewable_storyboard_packet_candidate", packet.packetStatus)
        assertEquals("reviewable_storyboard_packet_ready", packet.nextAllowedDownstreamStep)
        assertEquals("reviewable_storyboard_packet_packaging_only", packet.downstreamBoundary)
        assertEquals("accepted-script-packet-001", packet.acceptedScriptPacketId)
        assertEquals("script_packet_draft.json", packet.scriptPacketDraftFile)
        assertEquals("a".repeat(64), packet.scriptPacketDraftContentSha256)
        assertEquals(2, packet.frameSummaries.size)
        assertTrue(packet.movementEvidenceSummary.movementEvidenceCount >= 2)
        assertTrue(packet.movementEvidenceSummary.openingPressureEvidenceCount >= 1)
        assertTrue(packet.movementEvidenceSummary.actionEvidenceCount >= 1)
        assertTrue(packet.movementEvidenceSummary.consequenceOrChoiceEvidenceCount >= 1)
        assertEquals(protectedAnchors.size, packet.protectedContinuityAnchorsSummary.protectedContinuityAnchorCount)
        assertEquals(1, packet.remainingResidualRisks.size)
        assertEquals(5, packet.reviewChecklist.size)
        assertFalse(packet.nextAllowedDownstreamStep.contains("provider", ignoreCase = true))
        assertFalse(packet.nextAllowedDownstreamStep.contains("production", ignoreCase = true))
        assertFalse(packet.nextAllowedDownstreamStep.contains("canon", ignoreCase = true))
        assertFalse(packet.nextAllowedDownstreamStep.contains("approval", ignoreCase = true))
    }

    @Test
    fun nonPassOrWrongAuditNextStepFailsPacket() {
        val bundle = fixtureBundle()
        val nonPass = createPacket(bundle.copy(audit = bundle.audit.copy(verdict = "FAIL", nextAllowedDownstreamStep = "")))
        val wrongNext = createPacket(bundle.copy(audit = bundle.audit.copy(nextAllowedDownstreamStep = "storyboard_packet_human_review_ready")))

        assertEquals("FAIL", nonPass.verdict)
        assertEquals("", nonPass.nextAllowedDownstreamStep)
        assertTrue(nonPass.frameSummaries.isEmpty())
        assertTrue(nonPass.findings.any { it.ruleId == "STORY_MOVEMENT_AUDIT_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertEquals("", wrongNext.nextAllowedDownstreamStep)
        assertTrue(wrongNext.findings.any { it.ruleId == "STORY_MOVEMENT_AUDIT_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun nonPassOrWrongHumanReviewPreflightNextStepFailsPacket() {
        val bundle = fixtureBundle()
        val nonPass = createPacket(bundle.copy(preflight = bundle.preflight.copy(verdict = "FAIL", nextAllowedDownstreamStep = "")))
        val wrongNext = createPacket(bundle.copy(preflight = bundle.preflight.copy(nextAllowedDownstreamStep = "asset_candidate_preflight")))

        assertEquals("FAIL", nonPass.verdict)
        assertEquals("", nonPass.nextAllowedDownstreamStep)
        assertTrue(nonPass.findings.any { it.ruleId == "HUMAN_REVIEW_PREFLIGHT_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertEquals("", wrongNext.nextAllowedDownstreamStep)
        assertTrue(wrongNext.findings.any { it.ruleId == "HUMAN_REVIEW_PREFLIGHT_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun sourceBindingMissingOrMismatchFailsPacket() {
        val bundle = fixtureBundle()
        val missing = createPacket(bundle.copy(draft = bundle.draft.copy(acceptedScriptPacketId = "")))
        val mismatch = createPacket(bundle.copy(review = bundle.review.copy(acceptedScriptPacketId = "accepted-script-packet-forged")))
        val hashMismatch = createPacket(bundle.copy(audit = bundle.audit.copy(scriptPacketDraftContentSha256 = "b".repeat(64))))

        assertEquals("FAIL", missing.verdict)
        assertEquals("", missing.nextAllowedDownstreamStep)
        assertTrue(missing.findings.any { it.ruleId == "ACCEPTED_SCRIPT_PACKET_ID_MISSING" })
        assertEquals("FAIL", mismatch.verdict)
        assertEquals("", mismatch.nextAllowedDownstreamStep)
        assertTrue(mismatch.findings.any { it.ruleId == "ACCEPTED_SCRIPT_PACKET_ID_MISMATCH" })
        assertEquals("FAIL", hashMismatch.verdict)
        assertEquals("", hashMismatch.nextAllowedDownstreamStep)
        assertTrue(hashMismatch.findings.any { it.ruleId == "AUDIT_SCRIPT_PACKET_DRAFT_HASH_MISMATCH" })
    }

    @Test
    fun frameIdSetMismatchOrDuplicateFrameIdsFailPacket() {
        val bundle = fixtureBundle()
        val setMismatch = createPacket(bundle.copy(review = bundle.review.copy(frameIds = listOf("frame-001", "frame-forged"))))
        val duplicateFrames = listOf(frame(frameId = "frame-001"), frame(frameId = "frame-001"))
        val duplicate = createPacket(
            bundle.copy(
                draft = bundle.draft.copy(frames = duplicateFrames),
                review = bundle.review.copy(frameIds = listOf("frame-001", "frame-001")),
            ),
        )

        assertEquals("FAIL", setMismatch.verdict)
        assertEquals("", setMismatch.nextAllowedDownstreamStep)
        assertTrue(setMismatch.findings.any { it.ruleId == "FRAME_ID_SET_MISMATCH" })
        assertEquals("FAIL", duplicate.verdict)
        assertEquals("", duplicate.nextAllowedDownstreamStep)
        assertTrue(duplicate.findings.any { it.ruleId == "FRAME_ID_DUPLICATE" })
    }

    @Test
    fun missingMovementEvidenceSummaryFailsPacket() {
        val bundle = fixtureBundle()
        val report = createPacket(
            bundle.copy(
                audit = bundle.audit.copy(
                    movementEvidenceCount = 0,
                    evidence = bundle.audit.evidence.copy(
                        movementEvidenceCount = 0,
                        openingPressureEvidenceCount = 0,
                        actionEvidenceCount = 0,
                        consequenceOrChoiceEvidenceCount = 0,
                    ),
                ),
            ),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "MOVEMENT_EVIDENCE_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "OPENING_PRESSURE_EVIDENCE_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "ACTION_EVIDENCE_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "CONSEQUENCE_OR_CHOICE_EVIDENCE_MISSING" })
    }

    @Test
    fun missingContinuityAnchorsSummaryFailsPacket() {
        val bundle = fixtureBundle()
        val report = createPacket(
            bundle.copy(
                review = bundle.review.copy(
                    protectedContinuityAnchorsSummary = AcceptedScriptPacketContinuityAnchorsSummary(),
                ),
            ),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "PROTECTED_CONTINUITY_ANCHORS_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "CONTINUITY_ANCHOR_COVERAGE_MISMATCH" })
    }

    @Test
    fun shortOutputProviderOrApprovalFieldsInDraftFailPacket() {
        val bundle = fixtureBundle()

        val imageOutput = createPacket(bundle.withFirstFrame { it.copy(imageOutputPath = "frame-001.png") })
        val videoOutput = createPacket(bundle.withFirstFrame { it.copy(videoOutputPath = "shot-001") })
        val generationApi = createPacket(bundle.withFirstFrame { it.copy(generationApi = "manual-provider") })
        val approvalStatus = createPacket(bundle.withFirstFrame { it.copy(assetApprovalStatus = "approved") })

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
    fun forbiddenDownstreamClaimInTextSurfaceFailsPacket() {
        val bundle = fixtureBundle()
        val report = createPacket(bundle.copy(draft = bundle.draft.copy(reviewNotes = "image generated by provider job")))

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.evidence.forbiddenClaimCount >= 2)
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
    }

    @Test
    fun cliWritesReviewableStoryboardPacketWithoutDatabase() {
        val tempDir = Files.createTempDirectory("reviewable-storyboard-packet")
        val draftPath = tempDir.resolve("storyboard_packet_draft.json")
        val reviewPath = tempDir.resolve("storyboard_packet_review.json")
        val preflightPath = tempDir.resolve("storyboard_human_review_preflight.json")
        val auditPath = tempDir.resolve("storyboard_story_movement_audit.json")
        val out = tempDir.resolve("reviewable_storyboard_packet.json")
        val bundle = fixtureBundle(
            draftFile = draftPath.toString(),
            reviewFile = reviewPath.toString(),
            preflightFile = preflightPath.toString(),
            auditFile = auditPath.toString(),
        )
        writeJson(draftPath, bundle.draft)
        writeJson(reviewPath, bundle.review)
        writeJson(preflightPath, bundle.preflight)
        writeJson(auditPath, bundle.audit)

        val exit = Cli().run(
            listOf(
                "create-reviewable-storyboard-packet",
                "--storyboard-packet-draft",
                draftPath.toString(),
                "--storyboard-packet-review",
                reviewPath.toString(),
                "--storyboard-human-review-preflight",
                preflightPath.toString(),
                "--storyboard-story-movement-audit",
                auditPath.toString(),
                "--packet",
                "reviewable-storyboard-packet-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val packet = readJson<ReviewableStoryboardPacket>(out)
        assertEquals("PASS", packet.verdict, packet.findings.joinToString { it.ruleId })
        assertEquals("reviewable-storyboard-packet-cli-001", packet.packetId)
        assertEquals("reviewable_storyboard_packet_ready", packet.nextAllowedDownstreamStep)
    }

    private fun createPacket(bundle: FixtureBundle): ReviewableStoryboardPacket =
        ReviewableStoryboardPacketService().create(
            storyboardPacketDraft = bundle.draft,
            storyboardPacketReview = bundle.review,
            storyboardHumanReviewPreflight = bundle.preflight,
            storyboardStoryMovementAudit = bundle.audit,
            storyboardPacketDraftFile = bundle.draftFile,
            storyboardPacketReviewFile = bundle.reviewFile,
            storyboardHumanReviewPreflightFile = bundle.preflightFile,
            storyboardStoryMovementAuditFile = bundle.auditFile,
            packetId = "reviewable-storyboard-packet-001",
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
        auditFile: String = "storyboard_story_movement_audit.json",
        draft: StoryboardPacketDraft = draft(anchors = anchors),
        review: StoryboardPacketReviewPacket = review(anchors = anchors),
        preflight: StoryboardPacketHumanReviewPreflightReport = preflight(),
        audit: StoryboardStoryMovementAuditReport? = null,
    ): FixtureBundle {
        val resolvedReview = review.copy(storyboardPacketDraftFile = draftFile)
        val resolvedPreflight = preflight.copy(
            storyboardPacketReviewFile = reviewFile,
            storyboardPacketDraftFile = draftFile,
        )
        val resolvedAudit = audit ?: StoryboardStoryMovementAuditService().audit(
            storyboardPacketDraft = draft,
            storyboardPacketReview = resolvedReview,
            storyboardHumanReviewPreflight = resolvedPreflight,
            storyboardPacketDraftFile = draftFile,
            storyboardPacketReviewFile = reviewFile,
            storyboardHumanReviewPreflightFile = preflightFile,
            auditId = "story-movement-audit-001",
        )
        return FixtureBundle(
            draft = draft,
            review = resolvedReview,
            preflight = resolvedPreflight,
            audit = resolvedAudit,
            draftFile = draftFile,
            reviewFile = reviewFile,
            preflightFile = preflightFile,
            auditFile = auditFile,
        )
    }

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
        val audit: StoryboardStoryMovementAuditReport,
        val draftFile: String,
        val reviewFile: String,
        val preflightFile: String,
        val auditFile: String,
    )

    private companion object {
        val protectedAnchors = listOf(
            "Mira has already paid the door toll before reaching the checkpoint.",
            "The guard requires a visible mark before answering questions.",
        )
    }
}
