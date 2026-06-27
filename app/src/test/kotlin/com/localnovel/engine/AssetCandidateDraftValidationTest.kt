package com.localnovel.engine

import com.localnovel.engine.model.AssetCandidateDraft
import com.localnovel.engine.model.AssetCandidateDraftItem
import com.localnovel.engine.model.AssetCandidateDraftValidationReport
import com.localnovel.engine.service.AssetCandidateDraftValidationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssetCandidateDraftValidationTest {
    @Test
    fun passDraftCreatesValidationReport() {
        val report = AssetCandidateDraftValidationService().validate(
            assetCandidateDraft = draft(),
            assetCandidateDraftFile = "asset_candidate_draft.json",
        )

        assertEquals("PASS", report.verdict)
        assertEquals("asset-candidate-draft-001", report.assetCandidateDraftId)
        assertEquals("asset_candidate_review_preflight", report.nextAllowedDownstreamStep)
        assertEquals(1, report.candidateItems.size)
    }

    @Test
    fun nonPassDraftFailsValidation() {
        val report = AssetCandidateDraftValidationService().validate(
            assetCandidateDraft = draft(verdict = "FAIL"),
            assetCandidateDraftFile = "asset_candidate_draft.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "ASSET_CANDIDATE_DRAFT_NOT_PASS" })
    }

    @Test
    fun wrongDraftNextStepFailsValidation() {
        val report = AssetCandidateDraftValidationService().validate(
            assetCandidateDraft = draft(nextAllowedDownstreamStep = "asset_candidate_human_review_ready"),
            assetCandidateDraftFile = "asset_candidate_draft.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "ASSET_CANDIDATE_DRAFT_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun missingSourceBindingFieldFailsValidation() {
        val report = AssetCandidateDraftValidationService().validate(
            assetCandidateDraft = draft(acceptedScriptPacketId = ""),
            assetCandidateDraftFile = "asset_candidate_draft.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "ACCEPTED_SCRIPT_PACKET_ID_MISSING" })
    }

    @Test
    fun incompleteCandidateItemFailsValidation() {
        val report = AssetCandidateDraftValidationService().validate(
            assetCandidateDraft = draft(
                candidateItems = listOf(
                    candidateItem(candidateId = "", assetType = "", sourceRequirementId = "", requirement = ""),
                ),
            ),
            assetCandidateDraftFile = "asset_candidate_draft.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "ASSET_CANDIDATE_ID_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "ASSET_CANDIDATE_TYPE_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "SOURCE_REQUIREMENT_ID_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "ASSET_CANDIDATE_REQUIREMENT_MISSING" })
    }

    @Test
    fun forbiddenCandidateStatusFailsValidation() {
        val report = AssetCandidateDraftValidationService().validate(
            assetCandidateDraft = draft(
                candidateItems = listOf(candidateItem(candidateStatus = "approved")),
            ),
            assetCandidateDraftFile = "asset_candidate_draft.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "ASSET_CANDIDATE_STATUS_NOT_ALLOWED" })
        assertTrue(report.findings.any { it.ruleId == "ASSET_CANDIDATE_STATUS_FORBIDDEN" })
    }

    @Test
    fun forbiddenApprovalCanonRuntimeOrGeneratedMediaClaimFailsValidation() {
        val report = AssetCandidateDraftValidationService().validate(
            assetCandidateDraft = draft(
                candidateItems = listOf(
                    candidateItem(requirement = "candidate only: image generated; asset approval complete; canon applied"),
                ),
            ),
            assetCandidateDraftFile = "asset_candidate_draft.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
    }

    @Test
    fun cliWritesAssetCandidateDraftValidationWithoutDatabase() {
        val tempDir = Files.createTempDirectory("asset-candidate-draft-validation")
        val draftPath = tempDir.resolve("asset_candidate_draft.json")
        val out = tempDir.resolve("asset_candidate_draft_validation.json")
        writeJson(draftPath, draft())

        val exit = Cli().run(
            listOf(
                "validate-asset-candidate-draft",
                "--asset-candidate-draft",
                draftPath.toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<AssetCandidateDraftValidationReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("asset_candidate_review_preflight", report.nextAllowedDownstreamStep)
    }

    private fun draft(
        verdict: String = "PASS",
        draftId: String = "asset-candidate-draft-001",
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
    ): AssetCandidateDraft =
        AssetCandidateDraft(
            verdict = verdict,
            draftId = draftId,
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
            downstreamBoundary = "asset_candidate_draft_only",
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
