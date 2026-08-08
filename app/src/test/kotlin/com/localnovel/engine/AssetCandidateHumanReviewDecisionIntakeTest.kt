package com.localnovel.engine

import com.localnovel.engine.model.AssetCandidateHumanReviewDecision
import com.localnovel.engine.model.AssetCandidateHumanReviewDecisionBlockingIssue
import com.localnovel.engine.model.AssetCandidateHumanReviewDecisionChecklistItem
import com.localnovel.engine.model.AssetCandidateHumanReviewDecisionIntakeReport
import com.localnovel.engine.model.AssetCandidateReviewPreflightReport
import com.localnovel.engine.model.ImageCandidateRequirement
import com.localnovel.engine.service.AssetCandidateHumanReviewDecisionIntakeService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssetCandidateHumanReviewDecisionIntakeTest {
    @Test
    fun passPreflightAndHumanDecisionAllowsImageCandidatePreflight() {
        val report = AssetCandidateHumanReviewDecisionIntakeService().validate(
            assetCandidateReviewPreflight = preflight(),
            assetCandidateHumanReviewDecision = decision(),
            assetCandidateReviewPreflightFile = "asset_candidate_review_preflight.json",
            assetCandidateHumanReviewDecisionFile = "asset_candidate_human_review_decision.json",
        )

        assertEquals("PASS", report.verdict)
        assertEquals(true, report.decisionAllowedDownstream)
        assertEquals("image_candidate_preflight", report.nextAllowedDownstreamStep)
        assertEquals(1, report.imageCandidateRequirements.size)
    }

    @Test
    fun upstreamNonPassOrWrongNextStepFailsDecisionIntake() {
        val nonPass = AssetCandidateHumanReviewDecisionIntakeService().validate(
            assetCandidateReviewPreflight = preflight(verdict = "FAIL"),
            assetCandidateHumanReviewDecision = decision(),
            assetCandidateReviewPreflightFile = "asset_candidate_review_preflight.json",
            assetCandidateHumanReviewDecisionFile = "asset_candidate_human_review_decision.json",
        )
        val wrongNext = AssetCandidateHumanReviewDecisionIntakeService().validate(
            assetCandidateReviewPreflight = preflight(nextAllowedDownstreamStep = "image_candidate_preflight"),
            assetCandidateHumanReviewDecision = decision(),
            assetCandidateReviewPreflightFile = "asset_candidate_review_preflight.json",
            assetCandidateHumanReviewDecisionFile = "asset_candidate_human_review_decision.json",
        )

        assertEquals("FAIL", nonPass.verdict)
        assertTrue(nonPass.findings.any { it.ruleId == "ASSET_CANDIDATE_REVIEW_PREFLIGHT_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertTrue(wrongNext.findings.any { it.ruleId == "ASSET_CANDIDATE_REVIEW_PREFLIGHT_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun autoSourceUnknownStatusAndMismatchFailDecisionIntake() {
        val report = AssetCandidateHumanReviewDecisionIntakeService().validate(
            assetCandidateReviewPreflight = preflight(),
            assetCandidateHumanReviewDecision = decision(
                decisionSource = "auto",
                decisionStatus = "APPROVED",
                acceptedScriptPacketId = "other-packet",
            ),
            assetCandidateReviewPreflightFile = "asset_candidate_review_preflight.json",
            assetCandidateHumanReviewDecisionFile = "asset_candidate_human_review_decision.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals(false, report.decisionAllowedDownstream)
        assertTrue(report.findings.any { it.ruleId == "DECISION_SOURCE_NOT_HUMAN" })
        assertTrue(report.findings.any { it.ruleId == "DECISION_STATUS_UNKNOWN" })
        assertTrue(report.findings.any { it.ruleId == "DECISION_ACCEPTED_SCRIPT_PACKET_ID_MISMATCH" })
    }

    @Test
    fun requestChangesAndRejectDoNotAllowDownstream() {
        val requestChanges = AssetCandidateHumanReviewDecisionIntakeService().validate(
            assetCandidateReviewPreflight = preflight(),
            assetCandidateHumanReviewDecision = decision(decisionStatus = "REQUEST_CHANGES", imageCandidateRequirements = emptyList()),
            assetCandidateReviewPreflightFile = "asset_candidate_review_preflight.json",
            assetCandidateHumanReviewDecisionFile = "asset_candidate_human_review_decision.json",
        )
        val reject = AssetCandidateHumanReviewDecisionIntakeService().validate(
            assetCandidateReviewPreflight = preflight(),
            assetCandidateHumanReviewDecision = decision(decisionStatus = "REJECT", imageCandidateRequirements = emptyList()),
            assetCandidateReviewPreflightFile = "asset_candidate_review_preflight.json",
            assetCandidateHumanReviewDecisionFile = "asset_candidate_human_review_decision.json",
        )

        assertEquals("PASS", requestChanges.verdict)
        assertEquals(false, requestChanges.decisionAllowedDownstream)
        assertEquals("", requestChanges.nextAllowedDownstreamStep)
        assertEquals("PASS", reject.verdict)
        assertEquals(false, reject.decisionAllowedDownstream)
        assertEquals("", reject.nextAllowedDownstreamStep)
    }

    @Test
    fun allowDownstreamWithIncompleteChecklistOrBlockingIssueFails() {
        val shortChecklist = AssetCandidateHumanReviewDecisionIntakeService().validate(
            assetCandidateReviewPreflight = preflight(),
            assetCandidateHumanReviewDecision = decision(checklistItems = listOf(checklistItem("downstream_boundary"))),
            assetCandidateReviewPreflightFile = "asset_candidate_review_preflight.json",
            assetCandidateHumanReviewDecisionFile = "asset_candidate_human_review_decision.json",
        )
        val blocking = AssetCandidateHumanReviewDecisionIntakeService().validate(
            assetCandidateReviewPreflight = preflight(),
            assetCandidateHumanReviewDecision = decision(
                blockingIssues = listOf(
                    AssetCandidateHumanReviewDecisionBlockingIssue("issue-1", "BLOCKING", "OPEN", "Needs visual requirement repair."),
                ),
            ),
            assetCandidateReviewPreflightFile = "asset_candidate_review_preflight.json",
            assetCandidateHumanReviewDecisionFile = "asset_candidate_human_review_decision.json",
        )

        assertEquals("FAIL", shortChecklist.verdict)
        assertTrue(shortChecklist.findings.any { it.ruleId == "HUMAN_CHECKLIST_REQUIRED_CATEGORY_MISSING" })
        assertEquals("FAIL", blocking.verdict)
        assertTrue(blocking.findings.any { it.ruleId == "UNRESOLVED_BLOCKING_ISSUE_PRESENT" })
    }

    @Test
    fun forbiddenClaimsStatusesAndOutputFieldsFailDecisionIntake() {
        val report = AssetCandidateHumanReviewDecisionIntakeService().validate(
            assetCandidateReviewPreflight = preflight(),
            assetCandidateHumanReviewDecision = decision(
                imageCandidateRequirements = listOf(
                    imageRequirement(
                        status = "approved_reference",
                        requirement = "image generated; canon applied",
                        imageOutputPath = "frame-001",
                        generationApi = "manual-provider-job",
                        providerJobId = "job-001",
                    ),
                ),
            ),
            assetCandidateReviewPreflightFile = "asset_candidate_review_preflight.json",
            assetCandidateHumanReviewDecisionFile = "asset_candidate_human_review_decision.json",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "IMAGE_CANDIDATE_STATUS_NOT_ALLOWED" })
        assertTrue(report.findings.any { it.ruleId == "IMAGE_CANDIDATE_STATUS_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "IMAGE_OUTPUT_PATH_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "GENERATION_API_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "PROVIDER_JOB_ID_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
    }

    @Test
    fun cliWritesDecisionIntakeWithoutDatabase() {
        val tempDir = Files.createTempDirectory("asset-candidate-human-review-decision")
        val preflightPath = tempDir.resolve("asset_candidate_review_preflight.json")
        val decisionPath = tempDir.resolve("asset_candidate_human_review_decision.json")
        val out = tempDir.resolve("asset_candidate_human_review_decision_intake.json")
        writeJson(preflightPath, preflight())
        writeJson(decisionPath, decision(assetCandidateReviewPreflightFile = preflightPath.toString()))

        val exit = Cli().run(
            listOf(
                "validate-asset-candidate-human-review-decision",
                "--asset-candidate-review-preflight",
                preflightPath.toString(),
                "--asset-candidate-human-review-decision",
                decisionPath.toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<AssetCandidateHumanReviewDecisionIntakeReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("image_candidate_preflight", report.nextAllowedDownstreamStep)
    }

    private fun preflight(
        verdict: String = "PASS",
        preflightId: String = "asset-candidate-review-preflight-001",
        nextAllowedDownstreamStep: String = "asset_candidate_human_review_ready",
    ): AssetCandidateReviewPreflightReport =
        AssetCandidateReviewPreflightReport(
            verdict = verdict,
            preflightId = preflightId,
            assetCandidateDraftValidationReportFile = "asset_candidate_draft_validation.json",
            assetCandidateDraftFile = "asset_candidate_draft.json",
            assetCandidateDraftId = "asset-candidate-draft-001",
            assetCandidateBriefFile = "asset_candidate_brief.json",
            assetCandidateBriefId = "asset-candidate-brief-001",
            assetCandidatePreflightFile = "asset_candidate_preflight.json",
            assetCandidatePreflightId = "asset-candidate-preflight-001",
            humanReviewDecisionIntakeFile = "human_review_decision_intake.json",
            humanReviewDecisionId = "human-review-decision-001",
            storyboardPacketReviewFile = "storyboard_packet_review.json",
            storyboardPacketReviewId = "storyboard-review-001",
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            candidateItemCount = 1,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun decision(
        decisionSource: String = "human",
        decisionStatus: String = "ALLOW_IMAGE_CANDIDATE_PREFLIGHT",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        checklistItems: List<AssetCandidateHumanReviewDecisionChecklistItem> = checklist(),
        blockingIssues: List<AssetCandidateHumanReviewDecisionBlockingIssue> = emptyList(),
        imageCandidateRequirements: List<ImageCandidateRequirement> = listOf(imageRequirement()),
        assetCandidateReviewPreflightFile: String = "asset_candidate_review_preflight.json",
    ): AssetCandidateHumanReviewDecision =
        AssetCandidateHumanReviewDecision(
            decisionId = "asset-candidate-human-decision-001",
            preflightId = "asset-candidate-review-preflight-001",
            assetCandidateReviewPreflightFile = assetCandidateReviewPreflightFile,
            assetCandidateDraftValidationReportFile = "asset_candidate_draft_validation.json",
            assetCandidateDraftFile = "asset_candidate_draft.json",
            assetCandidateDraftId = "asset-candidate-draft-001",
            assetCandidateBriefFile = "asset_candidate_brief.json",
            assetCandidateBriefId = "asset-candidate-brief-001",
            assetCandidatePreflightFile = "asset_candidate_preflight.json",
            assetCandidatePreflightId = "asset-candidate-preflight-001",
            humanReviewDecisionIntakeFile = "human_review_decision_intake.json",
            humanReviewDecisionId = "human-review-decision-001",
            storyboardPacketReviewFile = "storyboard_packet_review.json",
            storyboardPacketReviewId = "storyboard-review-001",
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            decisionSource = decisionSource,
            reviewer = "01",
            reviewerRole = "01_control",
            decisionAt = "2026-06-25T00:00:00Z",
            decisionStatus = decisionStatus,
            checklistItems = checklistItems,
            blockingIssues = blockingIssues,
            imageCandidateRequirements = imageCandidateRequirements,
        )

    private fun checklist(): List<AssetCandidateHumanReviewDecisionChecklistItem> =
        listOf(
            checklistItem("candidate_identity"),
            checklistItem("asset_type"),
            checklistItem("visual_requirements"),
            checklistItem("continuity_scene_fit"),
            checklistItem("downstream_boundary"),
            checklistItem("no_approval_no_lock"),
        )

    private fun checklistItem(category: String): AssetCandidateHumanReviewDecisionChecklistItem =
        AssetCandidateHumanReviewDecisionChecklistItem("check-$category", category, "PASS")

    private fun imageRequirement(
        status: String = "image_candidate",
        requirement: String = "candidate only: prepare prompt-safe door mark reference exploration",
        imageOutputPath: String = "",
        generationApi: String = "",
        providerJobId: String = "",
    ): ImageCandidateRequirement =
        ImageCandidateRequirement(
            requirementId = "img-req-door-mark",
            candidateId = "asset-door-mark-candidate",
            assetType = "prop",
            sourceRequirementId = "asset-door-mark-candidate",
            status = status,
            requirement = requirement,
            referenceRequirements = listOf("front view candidate reference needed"),
            doNotShow = listOf("no production-state wording"),
            promptSafetyNotes = listOf("preparation only; no media request or status upgrade"),
            imageOutputPath = imageOutputPath,
            generationApi = generationApi,
            providerJobId = providerJobId,
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
