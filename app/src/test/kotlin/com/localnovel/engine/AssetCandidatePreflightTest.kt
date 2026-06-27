package com.localnovel.engine

import com.localnovel.engine.model.AssetCandidatePreflightReport
import com.localnovel.engine.model.StoryboardPacketHumanReviewDecisionIntakeReport
import com.localnovel.engine.model.StoryboardPacketReviewAssetRequirement
import com.localnovel.engine.model.StoryboardPacketReviewPacket
import com.localnovel.engine.model.StoryboardPacketReviewSourceReports
import com.localnovel.engine.service.AssetCandidatePreflightService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssetCandidatePreflightTest {
    @Test
    fun passDecisionIntakeAndReviewPacketAllowsAssetCandidateBrief() {
        val report = AssetCandidatePreflightService().validate(
            humanReviewDecisionIntake = decisionIntake(),
            storyboardPacketReview = reviewPacket(),
            preflightId = "asset-candidate-preflight-001",
            humanReviewDecisionIntakeFile = "human_review_decision_intake.json",
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("PASS", report.verdict)
        assertEquals("asset-candidate-preflight-001", report.preflightId)
        assertEquals("human-review-decision-001", report.decisionId)
        assertEquals("storyboard-review-001", report.storyboardPacketReviewId)
        assertEquals("asset_candidate_brief", report.nextAllowedDownstreamStep)
        assertEquals(1, report.assetCandidateRequirements.size)
    }

    @Test
    fun nonPassDecisionIntakeFailsPreflight() {
        val report = AssetCandidatePreflightService().validate(
            humanReviewDecisionIntake = decisionIntake(verdict = "FAIL"),
            storyboardPacketReview = reviewPacket(),
            humanReviewDecisionIntakeFile = "human_review_decision_intake.json",
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "HUMAN_REVIEW_DECISION_INTAKE_NOT_PASS" })
    }

    @Test
    fun decisionNotAllowedDownstreamFailsPreflight() {
        val report = AssetCandidatePreflightService().validate(
            humanReviewDecisionIntake = decisionIntake(decisionAllowedDownstream = false),
            storyboardPacketReview = reviewPacket(),
            humanReviewDecisionIntakeFile = "human_review_decision_intake.json",
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "DECISION_NOT_ALLOWED_DOWNSTREAM" })
    }

    @Test
    fun wrongDecisionStatusOrNextStepFailsPreflight() {
        val wrongStatus = AssetCandidatePreflightService().validate(
            humanReviewDecisionIntake = decisionIntake(decisionStatus = "REQUEST_CHANGES"),
            storyboardPacketReview = reviewPacket(),
            humanReviewDecisionIntakeFile = "human_review_decision_intake.json",
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )
        val wrongNextStep = AssetCandidatePreflightService().validate(
            humanReviewDecisionIntake = decisionIntake(nextAllowedDownstreamStep = "asset_candidate_draft"),
            storyboardPacketReview = reviewPacket(),
            humanReviewDecisionIntakeFile = "human_review_decision_intake.json",
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("FAIL", wrongStatus.verdict)
        assertTrue(wrongStatus.findings.any { it.ruleId == "DECISION_STATUS_NOT_ASSET_CANDIDATE_PREFLIGHT" })
        assertEquals("FAIL", wrongNextStep.verdict)
        assertTrue(wrongNextStep.findings.any { it.ruleId == "DECISION_NEXT_STEP_NOT_ASSET_CANDIDATE_PREFLIGHT" })
    }

    @Test
    fun sourceBindingMissingOrMismatchFailsPreflight() {
        val missing = AssetCandidatePreflightService().validate(
            humanReviewDecisionIntake = decisionIntake(storyboardPacketReviewFile = ""),
            storyboardPacketReview = reviewPacket(),
            humanReviewDecisionIntakeFile = "human_review_decision_intake.json",
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )
        val mismatch = AssetCandidatePreflightService().validate(
            humanReviewDecisionIntake = decisionIntake(acceptedScriptPacketId = "accepted-script-packet-other"),
            storyboardPacketReview = reviewPacket(),
            humanReviewDecisionIntakeFile = "human_review_decision_intake.json",
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("FAIL", missing.verdict)
        assertTrue(missing.findings.any { it.ruleId == "DECISION_INTAKE_REVIEW_FILE_MISSING" })
        assertEquals("FAIL", mismatch.verdict)
        assertTrue(mismatch.findings.any { it.ruleId == "ACCEPTED_SCRIPT_PACKET_ID_MISMATCH" })
    }

    @Test
    fun directReviewPacketFailVerdictFailsEvenWhenSourceFieldsMatch() {
        val report = AssetCandidatePreflightService().validate(
            humanReviewDecisionIntake = decisionIntake(),
            storyboardPacketReview = reviewPacket(verdict = "FAIL"),
            humanReviewDecisionIntakeFile = "human_review_decision_intake.json",
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertEquals(emptyList(), report.assetCandidateRequirements)
        assertTrue(report.findings.any { it.ruleId == "STORYBOARD_PACKET_REVIEW_NOT_PASS" })
    }

    @Test
    fun directReviewPacketNonReadyStatusFailsEvenWhenSourceFieldsMatch() {
        val report = AssetCandidatePreflightService().validate(
            humanReviewDecisionIntake = decisionIntake(),
            storyboardPacketReview = reviewPacket(reviewStatus = "FAIL"),
            humanReviewDecisionIntakeFile = "human_review_decision_intake.json",
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertEquals(emptyList(), report.assetCandidateRequirements)
        assertTrue(report.findings.any { it.ruleId == "STORYBOARD_PACKET_REVIEW_NOT_READY" })
    }

    @Test
    fun directReviewPacketWrongNextStepFailsEvenWhenSourceFieldsMatch() {
        val report = AssetCandidatePreflightService().validate(
            humanReviewDecisionIntake = decisionIntake(),
            storyboardPacketReview = reviewPacket(nextAllowedDownstreamStep = "asset_candidate_preflight"),
            humanReviewDecisionIntakeFile = "human_review_decision_intake.json",
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertEquals(emptyList(), report.assetCandidateRequirements)
        assertTrue(report.findings.any { it.ruleId == "STORYBOARD_PACKET_REVIEW_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun missingAssetCandidateRequirementsFailsPreflight() {
        val report = AssetCandidatePreflightService().validate(
            humanReviewDecisionIntake = decisionIntake(),
            storyboardPacketReview = reviewPacket(assetCandidateRequirements = emptyList()),
            humanReviewDecisionIntakeFile = "human_review_decision_intake.json",
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "ASSET_CANDIDATE_REQUIREMENTS_MISSING" })
    }

    @Test
    fun forbiddenApprovalLockCanonOrGeneratedMediaClaimFailsPreflight() {
        val report = AssetCandidatePreflightService().validate(
            humanReviewDecisionIntake = decisionIntake(),
            storyboardPacketReview = reviewPacket(
                assetCandidateRequirements = listOf(
                    StoryboardPacketReviewAssetRequirement(
                        candidateId = "asset-approved-door",
                        requirement = "asset approved; canon applied; image generated",
                        sourceFrameIds = listOf("frame-001"),
                    ),
                ),
            ),
            humanReviewDecisionIntakeFile = "human_review_decision_intake.json",
            storyboardPacketReviewFile = "storyboard_packet_review.json",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.evidence.forbiddenClaimCount >= 2)
        assertTrue(report.evidence.forbiddenStatusCount >= 1)
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "ASSET_CANDIDATE_STATUS_FORBIDDEN" })
    }

    @Test
    fun cliWritesAssetCandidatePreflightWithoutDatabase() {
        val tempDir = Files.createTempDirectory("asset-candidate-preflight")
        val intakePath = tempDir.resolve("human_review_decision_intake.json")
        val reviewPath = tempDir.resolve("storyboard_packet_review.json")
        val out = tempDir.resolve("asset_candidate_preflight.json")
        writeJson(intakePath, decisionIntake(storyboardPacketReviewFile = reviewPath.toString()))
        writeJson(reviewPath, reviewPacket())

        val exit = Cli().run(
            listOf(
                "validate-asset-candidate-preflight",
                "--human-review-decision-intake",
                intakePath.toString(),
                "--storyboard-packet-review",
                reviewPath.toString(),
                "--preflight",
                "asset-candidate-preflight-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<AssetCandidatePreflightReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("asset-candidate-preflight-cli-001", report.preflightId)
        assertEquals("asset_candidate_brief", report.nextAllowedDownstreamStep)
    }

    private fun decisionIntake(
        verdict: String = "PASS",
        decisionId: String = "human-review-decision-001",
        preflightId: String = "human-review-preflight-001",
        humanReviewPreflightFile: String = "human_review_preflight.json",
        humanReviewDecisionFile: String = "human_review_decision.json",
        storyboardPacketReviewFile: String = "storyboard_packet_review.json",
        storyboardPacketReviewId: String = "storyboard-review-001",
        storyboardPacketDraftFile: String = "storyboard_packet_draft.json",
        storyboardPacketDraftReportFile: String = "storyboard_packet_draft_report.json",
        storyboardPacketDraftId: String = "storyboard-packet-draft-001",
        storyboardPlanningBriefId: String = "storyboard-brief-001",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        scriptPacketDraftFile: String = "script_packet_draft.json",
        scriptPacketDraftContentSha256: String = "a".repeat(64),
        decisionStatus: String = "ALLOW_ASSET_CANDIDATE_PREFLIGHT",
        decisionAllowedDownstream: Boolean = true,
        nextAllowedDownstreamStep: String = "asset_candidate_preflight",
    ): StoryboardPacketHumanReviewDecisionIntakeReport =
        StoryboardPacketHumanReviewDecisionIntakeReport(
            verdict = verdict,
            decisionId = decisionId,
            preflightId = preflightId,
            humanReviewPreflightFile = humanReviewPreflightFile,
            humanReviewDecisionFile = humanReviewDecisionFile,
            storyboardPacketReviewFile = storyboardPacketReviewFile,
            storyboardPacketReviewId = storyboardPacketReviewId,
            storyboardPacketDraftFile = storyboardPacketDraftFile,
            storyboardPacketDraftReportFile = storyboardPacketDraftReportFile,
            storyboardPacketDraftId = storyboardPacketDraftId,
            storyboardPlanningBriefId = storyboardPlanningBriefId,
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = scriptPacketDraftContentSha256,
            decisionStatus = decisionStatus,
            decisionAllowedDownstream = decisionAllowedDownstream,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

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
        assetCandidateRequirements: List<StoryboardPacketReviewAssetRequirement> = assetRequirements(),
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
            sourceReports = StoryboardPacketReviewSourceReports(
                storyboardPacketDraftValidationReportFile = storyboardPacketDraftReportFile,
                storyboardPacketDraftValidationVerdict = "PASS",
                storyboardPacketDraftValidationNextStep = "storyboard_packet_review",
            ),
            frameCount = 1,
            frameIds = listOf("frame-001"),
            assetCandidateRequirements = assetCandidateRequirements,
            remainingResidualRisks = listOf("Asset work is still candidate-only and not approved."),
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun assetRequirements(): List<StoryboardPacketReviewAssetRequirement> =
        listOf(
            StoryboardPacketReviewAssetRequirement(
                candidateId = "asset-door-mark-candidate",
                requirement = "candidate only: visible door mark reference needed for frame consistency",
                sourceFrameIds = listOf("frame-001"),
            ),
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
