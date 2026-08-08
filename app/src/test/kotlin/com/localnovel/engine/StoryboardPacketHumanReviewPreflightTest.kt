package com.localnovel.engine

import com.localnovel.engine.model.AcceptedScriptPacketContinuityAnchorsSummary
import com.localnovel.engine.model.StoryboardPacketHumanReviewPreflightReport
import com.localnovel.engine.model.StoryboardPacketReviewAssetRequirement
import com.localnovel.engine.model.StoryboardPacketReviewChecklistItem
import com.localnovel.engine.model.StoryboardPacketReviewPacket
import com.localnovel.engine.model.StoryboardPacketReviewSourceReports
import com.localnovel.engine.service.StoryboardPacketHumanReviewPreflightService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StoryboardPacketHumanReviewPreflightTest {
    @Test
    fun reviewReadyPacketPassesHumanReviewPreflight() {
        val report = StoryboardPacketHumanReviewPreflightService().validate(
            storyboardPacketReview = reviewPacket(),
            preflightId = "human-review-preflight-001",
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("PASS", report.verdict)
        assertEquals("human-review-preflight-001", report.preflightId)
        assertEquals("storyboard-review-001", report.storyboardPacketReviewId)
        assertEquals("REVIEW_READY", report.reviewStatus)
        assertEquals("storyboard_packet_human_review_ready", report.nextAllowedDownstreamStep)
        assertEquals(1, report.evidence.frameCount)
        assertEquals(1, report.evidence.frameIdCount)
        assertEquals(5, report.evidence.reviewChecklistCount)
        assertEquals(2, report.evidence.protectedContinuityAnchorCount)
        assertTrue(report.evidence.residualRiskCount > 0)
    }

    @Test
    fun failedOrNonReadyReviewPacketFailsPreflight() {
        val report = StoryboardPacketHumanReviewPreflightService().validate(
            storyboardPacketReview = reviewPacket(verdict = "FAIL", reviewStatus = "FAIL", nextAllowedDownstreamStep = ""),
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "STORYBOARD_PACKET_REVIEW_NOT_PASS" })
        assertTrue(report.findings.any { it.ruleId == "STORYBOARD_PACKET_REVIEW_STATUS_NOT_READY" })
    }

    @Test
    fun wrongReviewPacketNextStepFailsPreflight() {
        val report = StoryboardPacketHumanReviewPreflightService().validate(
            storyboardPacketReview = reviewPacket(nextAllowedDownstreamStep = "asset_candidate_preflight"),
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "STORYBOARD_PACKET_REVIEW_NEXT_STEP_NOT_HUMAN_REVIEW" })
    }

    @Test
    fun missingSourceBindingFieldsFailPreflight() {
        val report = StoryboardPacketHumanReviewPreflightService().validate(
            storyboardPacketReview = reviewPacket(
                reviewId = "",
                storyboardPacketDraftReportFile = "",
                sourceReports = sourceReports(reportFile = ""),
            ),
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "REVIEW_ID_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "STORYBOARD_PACKET_DRAFT_REPORT_FILE_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "SOURCE_DRAFT_VALIDATION_REPORT_FILE_MISSING" })
    }

    @Test
    fun sourceValidationReportFailVerdictFailsPreflight() {
        val report = StoryboardPacketHumanReviewPreflightService().validate(
            storyboardPacketReview = reviewPacket(
                sourceReports = sourceReports(verdict = "FAIL"),
            ),
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "SOURCE_DRAFT_VALIDATION_REPORT_NOT_PASS" })
    }

    @Test
    fun sourceValidationReportUnknownVerdictFailsPreflight() {
        val report = StoryboardPacketHumanReviewPreflightService().validate(
            storyboardPacketReview = reviewPacket(
                sourceReports = sourceReports(verdict = "MAYBE"),
            ),
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "SOURCE_DRAFT_VALIDATION_REPORT_VERDICT_UNKNOWN" })
    }

    @Test
    fun sourceValidationNextStepMissingOrWrongFailsPreflight() {
        val missing = StoryboardPacketHumanReviewPreflightService().validate(
            storyboardPacketReview = reviewPacket(
                sourceReports = sourceReports(nextStep = ""),
            ),
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )
        val mismatch = StoryboardPacketHumanReviewPreflightService().validate(
            storyboardPacketReview = reviewPacket(
                sourceReports = sourceReports(nextStep = "asset_candidate_preflight"),
            ),
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("FAIL", missing.verdict)
        assertTrue(missing.findings.any { it.ruleId == "SOURCE_DRAFT_VALIDATION_NEXT_STEP_MISSING" })
        assertEquals("FAIL", mismatch.verdict)
        assertTrue(mismatch.findings.any { it.ruleId == "SOURCE_DRAFT_VALIDATION_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun missingChecklistCategoryFailsPreflight() {
        val report = StoryboardPacketHumanReviewPreflightService().validate(
            storyboardPacketReview = reviewPacket(
                reviewChecklist = reviewChecklist().filterNot { it.category == "assets" },
            ),
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "REVIEW_CHECKLIST_REQUIRED_CATEGORY_MISSING" })
    }

    @Test
    fun blankOrDuplicateFrameIdsFailPreflight() {
        val blank = StoryboardPacketHumanReviewPreflightService().validate(
            storyboardPacketReview = reviewPacket(frameIds = listOf("")),
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )
        val duplicate = StoryboardPacketHumanReviewPreflightService().validate(
            storyboardPacketReview = reviewPacket(frameCount = 2, frameIds = listOf("frame-001", "frame-001")),
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("FAIL", blank.verdict)
        assertTrue(blank.findings.any { it.ruleId == "FRAME_IDS_MISSING" })
        assertEquals("FAIL", duplicate.verdict)
        assertTrue(duplicate.findings.any { it.ruleId == "FRAME_IDS_DUPLICATE" })
    }

    @Test
    fun forbiddenApprovalCanonOrGeneratedMediaClaimFailsPreflight() {
        val report = StoryboardPacketHumanReviewPreflightService().validate(
            storyboardPacketReview = reviewPacket(
                remainingResidualRisks = listOf("image generated; canon applied; user approved"),
            ),
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.evidence.forbiddenClaimCount >= 3)
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
    }

    @Test
    fun cliWritesHumanReviewPreflightWithoutDatabase() {
        val tempDir = Files.createTempDirectory("storyboard-human-review-preflight")
        val reviewPath = tempDir.resolve("storyboard_packet_review.json")
        val out = tempDir.resolve("storyboard_human_review_preflight.json")
        writeJson(reviewPath, reviewPacket())

        val exit = Cli().run(
            listOf(
                "validate-storyboard-packet-human-review-preflight",
                "--storyboard-packet-review",
                reviewPath.toString(),
                "--preflight",
                "human-review-preflight-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<StoryboardPacketHumanReviewPreflightReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("human-review-preflight-cli-001", report.preflightId)
        assertEquals("storyboard_packet_human_review_ready", report.nextAllowedDownstreamStep)
        assertEquals(reviewPath.toString(), report.storyboardPacketReviewFile)
    }

    private fun reviewPacket(
        verdict: String = "PASS",
        reviewId: String = "storyboard-review-001",
        reviewStatus: String = "REVIEW_READY",
        storyboardPacketDraftFile: String = "storyboard_packet_draft.json",
        storyboardPacketDraftReportFile: String = "storyboard_packet_draft_report.json",
        storyboardPacketDraftId: String = "storyboard-packet-draft-001",
        storyboardPlanningBriefId: String = "storyboard-brief-001",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        scriptPacketDraftFile: String = "script_packet_draft.json",
        scriptPacketDraftContentSha256: String = "a".repeat(64),
        sourceReports: StoryboardPacketReviewSourceReports = sourceReports(),
        frameCount: Int = 1,
        frameIds: List<String> = listOf("frame-001"),
        protectedContinuityAnchorsSummary: AcceptedScriptPacketContinuityAnchorsSummary = continuitySummary(),
        assetCandidateRequirements: List<StoryboardPacketReviewAssetRequirement> = assetRequirements(),
        remainingResidualRisks: List<String> = listOf(
            "Human review has not completed this storyboard packet.",
            "No image or video generation has run.",
        ),
        reviewChecklist: List<StoryboardPacketReviewChecklistItem> = reviewChecklist(),
        nextAllowedDownstreamStep: String = "storyboard_packet_human_review",
    ): StoryboardPacketReviewPacket =
        StoryboardPacketReviewPacket(
            verdict = verdict,
            reviewId = reviewId,
            reviewStatus = reviewStatus,
            storyboardPacketDraftFile = storyboardPacketDraftFile,
            storyboardPacketDraftReportFile = storyboardPacketDraftReportFile,
            storyboardPacketDraftId = storyboardPacketDraftId,
            storyboardPlanningBriefId = storyboardPlanningBriefId,
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = scriptPacketDraftContentSha256,
            sourceReports = sourceReports,
            frameCount = frameCount,
            frameIds = frameIds,
            protectedContinuityAnchorsSummary = protectedContinuityAnchorsSummary,
            assetCandidateRequirements = assetCandidateRequirements,
            remainingResidualRisks = remainingResidualRisks,
            reviewChecklist = reviewChecklist,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun sourceReports(
        reportFile: String = "storyboard_packet_draft_report.json",
        verdict: String = "PASS",
        nextStep: String = "storyboard_packet_review",
    ): StoryboardPacketReviewSourceReports =
        StoryboardPacketReviewSourceReports(
            storyboardPacketDraftValidationReportFile = reportFile,
            storyboardPacketDraftValidationVerdict = verdict,
            storyboardPacketDraftValidationNextStep = nextStep,
        )

    private fun continuitySummary(): AcceptedScriptPacketContinuityAnchorsSummary =
        AcceptedScriptPacketContinuityAnchorsSummary(
            protectedContinuityAnchorCount = 2,
            protectedContinuityAnchors = listOf(
                "Mira has already paid the door toll before reaching the checkpoint.",
                "The guard requires a visible mark before answering questions.",
            ),
        )

    private fun assetRequirements(): List<StoryboardPacketReviewAssetRequirement> =
        listOf(
            StoryboardPacketReviewAssetRequirement(
                candidateId = "asset-door-mark",
                requirement = "candidate only: visible mark reference",
                sourceFrameIds = listOf("frame-001"),
            ),
        )

    private fun reviewChecklist(): List<StoryboardPacketReviewChecklistItem> =
        listOf(
            StoryboardPacketReviewChecklistItem("review-scene-lock", "scene_lock", "Check scene lock."),
            StoryboardPacketReviewChecklistItem("review-continuity", "continuity", "Check continuity anchors."),
            StoryboardPacketReviewChecklistItem("review-secrecy", "knowledge_boundary", "Check secrecy boundary."),
            StoryboardPacketReviewChecklistItem("review-assets", "assets", "Check asset candidates."),
            StoryboardPacketReviewChecklistItem("review-downstream", "downstream_boundary", "Check downstream boundary."),
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
