package com.localnovel.engine

import com.localnovel.engine.model.AssetCandidateDraftItem
import com.localnovel.engine.model.AssetCandidateDraftValidationReport
import com.localnovel.engine.model.AssetCandidateReviewPreflightReport
import com.localnovel.engine.service.AssetCandidateReviewPreflightService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssetCandidateReviewPreflightTest {
    @Test
    fun passValidationReportAllowsHumanReviewReadyPreflight() {
        val report = AssetCandidateReviewPreflightService().validate(
            assetCandidateDraftValidation = validationReport(),
            preflightId = "asset-candidate-review-preflight-001",
            assetCandidateDraftValidationReportFile = "asset_candidate_draft_validation.json",
        )

        assertEquals("PASS", report.verdict)
        assertEquals("asset-candidate-review-preflight-001", report.preflightId)
        assertEquals("asset_candidate_human_review_ready", report.nextAllowedDownstreamStep)
        assertEquals(1, report.candidateItemCount)
    }

    @Test
    fun nonPassValidationReportFailsPreflight() {
        val report = AssetCandidateReviewPreflightService().validate(
            assetCandidateDraftValidation = validationReport(verdict = "FAIL"),
            assetCandidateDraftValidationReportFile = "asset_candidate_draft_validation.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "ASSET_CANDIDATE_DRAFT_VALIDATION_NOT_PASS" })
    }

    @Test
    fun wrongValidationNextStepFailsPreflight() {
        val report = AssetCandidateReviewPreflightService().validate(
            assetCandidateDraftValidation = validationReport(nextAllowedDownstreamStep = "asset_candidate_human_review_ready"),
            assetCandidateDraftValidationReportFile = "asset_candidate_draft_validation.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "ASSET_CANDIDATE_DRAFT_VALIDATION_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun missingSourceBindingFieldFailsPreflight() {
        val report = AssetCandidateReviewPreflightService().validate(
            assetCandidateDraftValidation = validationReport(assetCandidateDraftId = ""),
            assetCandidateDraftValidationReportFile = "asset_candidate_draft_validation.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "ASSET_CANDIDATE_DRAFT_ID_MISSING" })
    }

    @Test
    fun missingCandidateItemsFailsPreflight() {
        val report = AssetCandidateReviewPreflightService().validate(
            assetCandidateDraftValidation = validationReport(candidateItems = emptyList()),
            assetCandidateDraftValidationReportFile = "asset_candidate_draft_validation.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals(0, report.candidateItemCount)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "ASSET_CANDIDATE_ITEMS_MISSING" })
    }

    @Test
    fun forbiddenCandidateStatusFailsPreflight() {
        val report = AssetCandidateReviewPreflightService().validate(
            assetCandidateDraftValidation = validationReport(
                candidateItems = listOf(candidateItem(candidateStatus = "review_ready")),
            ),
            assetCandidateDraftValidationReportFile = "asset_candidate_draft_validation.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "ASSET_CANDIDATE_STATUS_NOT_ALLOWED" })
        assertTrue(report.findings.any { it.ruleId == "ASSET_CANDIDATE_STATUS_FORBIDDEN" })
    }

    @Test
    fun forbiddenApprovalCanonRuntimeOrGeneratedMediaClaimFailsPreflight() {
        val report = AssetCandidateReviewPreflightService().validate(
            assetCandidateDraftValidation = validationReport(
                candidateItems = listOf(
                    candidateItem(requirement = "candidate only: video generated; assets approved; runtime scene created"),
                ),
            ),
            assetCandidateDraftValidationReportFile = "asset_candidate_draft_validation.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
    }

    @Test
    fun cliWritesAssetCandidateReviewPreflightWithoutDatabase() {
        val tempDir = Files.createTempDirectory("asset-candidate-review-preflight")
        val validationPath = tempDir.resolve("asset_candidate_draft_validation.json")
        val out = tempDir.resolve("asset_candidate_review_preflight.json")
        writeJson(validationPath, validationReport())

        val exit = Cli().run(
            listOf(
                "validate-asset-candidate-review-preflight",
                "--asset-candidate-draft-validation",
                validationPath.toString(),
                "--preflight",
                "asset-candidate-review-preflight-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<AssetCandidateReviewPreflightReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("asset-candidate-review-preflight-cli-001", report.preflightId)
        assertEquals("asset_candidate_human_review_ready", report.nextAllowedDownstreamStep)
    }

    private fun validationReport(
        verdict: String = "PASS",
        assetCandidateDraftFile: String = "asset_candidate_draft.json",
        assetCandidateDraftId: String = "asset-candidate-draft-001",
        assetCandidateBriefFile: String = "asset_candidate_brief.json",
        assetCandidateBriefId: String = "asset-candidate-brief-001",
        assetCandidatePreflightFile: String = "asset_candidate_preflight.json",
        assetCandidatePreflightId: String = "asset-candidate-preflight-001",
        humanReviewDecisionIntakeFile: String = "human_review_decision_intake.json",
        humanReviewDecisionId: String = "human-review-decision-001",
        storyboardPacketReviewFile: String = "storyboard_packet_review.json",
        storyboardPacketReviewId: String = "storyboard-review-001",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        scriptPacketDraftFile: String = "script_packet_draft.json",
        scriptPacketDraftContentSha256: String = "a".repeat(64),
        candidateItems: List<AssetCandidateDraftItem> = listOf(candidateItem()),
        nextAllowedDownstreamStep: String = "asset_candidate_review_preflight",
    ): AssetCandidateDraftValidationReport =
        AssetCandidateDraftValidationReport(
            verdict = verdict,
            assetCandidateDraftFile = assetCandidateDraftFile,
            assetCandidateDraftId = assetCandidateDraftId,
            assetCandidateBriefFile = assetCandidateBriefFile,
            assetCandidateBriefId = assetCandidateBriefId,
            assetCandidatePreflightFile = assetCandidatePreflightFile,
            assetCandidatePreflightId = assetCandidatePreflightId,
            humanReviewDecisionIntakeFile = humanReviewDecisionIntakeFile,
            humanReviewDecisionId = humanReviewDecisionId,
            storyboardPacketReviewFile = storyboardPacketReviewFile,
            storyboardPacketReviewId = storyboardPacketReviewId,
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = scriptPacketDraftContentSha256,
            candidateItems = candidateItems,
            downstreamBoundary = "asset_candidate_draft_validation_only",
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun candidateItem(
        candidateId: String = "asset-door-mark-candidate",
        assetType: String = "prop",
        sourceRequirementId: String = "asset-door-mark-candidate",
        candidateStatus: String = "draft_candidate",
        requirement: String = "candidate only: visible door mark prop reference needed for frame consistency",
        publicDescription: String = "Candidate-only visible door mark prop reference.",
        visualReferenceRequirement: String = "Candidate-only prop reference sheet needed.",
    ): AssetCandidateDraftItem =
        AssetCandidateDraftItem(
            candidateId = candidateId,
            assetType = assetType,
            sourceRequirementId = sourceRequirementId,
            candidateStatus = candidateStatus,
            requirement = requirement,
            publicDescription = publicDescription,
            visualReferenceRequirement = visualReferenceRequirement,
            sourceFrameIds = listOf("frame-001"),
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
