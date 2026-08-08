package com.localnovel.engine

import com.localnovel.engine.model.ImageCandidateBrief
import com.localnovel.engine.model.ImageCandidateBriefChecklistItem
import com.localnovel.engine.model.ImageCandidatePromptPreparation
import com.localnovel.engine.model.ImageCandidateRequirement
import com.localnovel.engine.service.ImageCandidatePromptPreparationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageCandidatePromptPreparationTest {
    @Test
    fun passBriefCreatesPromptPreparation() {
        val preparation = ImageCandidatePromptPreparationService().create(
            imageCandidateBrief = brief(),
            preparationId = "image-candidate-prompt-prep-001",
            imageCandidateBriefFile = "image_candidate_brief.json",
        )

        assertEquals("PASS", preparation.verdict)
        assertEquals("image-candidate-prompt-prep-001", preparation.preparationId)
        assertEquals("image_candidate_prompt_validation", preparation.nextAllowedDownstreamStep)
        assertEquals(1, preparation.promptItems.size)
        assertTrue(preparation.promptItems.first().positivePromptDraft.isNotBlank())
        assertTrue(preparation.promptItems.first().negativePromptDraft.isNotBlank())
        assertTrue(preparation.promptItems.first().providerName.isBlank())
        assertTrue(preparation.promptItems.first().generationApi.isBlank())
        assertTrue(preparation.promptItems.first().providerJobId.isBlank())
        assertTrue(preparation.promptItems.first().imageOutputPath.isBlank())
    }

    @Test
    fun nonPassBriefOrWrongNextStepFailsPreparation() {
        val nonPass = ImageCandidatePromptPreparationService().create(
            imageCandidateBrief = brief(verdict = "FAIL"),
            imageCandidateBriefFile = "image_candidate_brief.json",
        )
        val wrongNext = ImageCandidatePromptPreparationService().create(
            imageCandidateBrief = brief(nextAllowedDownstreamStep = "image_candidate_prompt_validation"),
            imageCandidateBriefFile = "image_candidate_brief.json",
        )

        assertEquals("FAIL", nonPass.verdict)
        assertTrue(nonPass.findings.any { it.ruleId == "IMAGE_CANDIDATE_BRIEF_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertTrue(wrongNext.findings.any { it.ruleId == "IMAGE_CANDIDATE_BRIEF_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun missingSourceOrRequirementFailsPreparation() {
        val missingSource = ImageCandidatePromptPreparationService().create(
            imageCandidateBrief = brief(decisionId = ""),
            imageCandidateBriefFile = "image_candidate_brief.json",
        )
        val missingRequirement = ImageCandidatePromptPreparationService().create(
            imageCandidateBrief = brief(imageCandidateRequirements = emptyList()),
            imageCandidateBriefFile = "image_candidate_brief.json",
        )

        assertEquals("FAIL", missingSource.verdict)
        assertTrue(missingSource.findings.any { it.ruleId == "DECISION_ID_MISSING" })
        assertEquals("FAIL", missingRequirement.verdict)
        assertTrue(missingRequirement.findings.any { it.ruleId == "IMAGE_CANDIDATE_REQUIREMENTS_MISSING" })
    }

    @Test
    fun providerOutputAndForbiddenClaimsFailPreparation() {
        val preparation = ImageCandidatePromptPreparationService().create(
            imageCandidateBrief = brief(
                imageCandidateRequirements = listOf(
                    imageRequirement(
                        status = "approved_reference",
                        requirement = "prompt submitted; image generated; canon applied",
                        imageOutputPath = "frame-001",
                        generationApi = "manual-provider-job",
                        providerJobId = "job-001",
                    ),
                ),
            ),
            imageCandidateBriefFile = "image_candidate_brief.json",
        )

        assertEquals("FAIL", preparation.verdict)
        assertTrue(preparation.findings.any { it.ruleId == "IMAGE_CANDIDATE_STATUS_NOT_ALLOWED" })
        assertTrue(preparation.findings.any { it.ruleId == "IMAGE_CANDIDATE_STATUS_FORBIDDEN" })
        assertTrue(preparation.findings.any { it.ruleId == "IMAGE_OUTPUT_PATH_FORBIDDEN" })
        assertTrue(preparation.findings.any { it.ruleId == "GENERATION_API_FORBIDDEN" })
        assertTrue(preparation.findings.any { it.ruleId == "PROVIDER_JOB_ID_FORBIDDEN" })
        assertTrue(preparation.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
    }

    @Test
    fun forbiddenAssetTypeStatusUpgradeFailsPreparation() {
        val approvedReference = ImageCandidatePromptPreparationService().create(
            imageCandidateBrief = brief(
                imageCandidateRequirements = listOf(imageRequirement(assetType = "approved_reference")),
            ),
            imageCandidateBriefFile = "image_candidate_brief.json",
        )
        val productionLocked = ImageCandidatePromptPreparationService().create(
            imageCandidateBrief = brief(
                imageCandidateRequirements = listOf(imageRequirement(assetType = "production_locked")),
            ),
            imageCandidateBriefFile = "image_candidate_brief.json",
        )

        assertEquals("FAIL", approvedReference.verdict)
        assertTrue(approvedReference.findings.any { it.ruleId == "IMAGE_CANDIDATE_STATUS_FORBIDDEN" })
        assertEquals("FAIL", productionLocked.verdict)
        assertTrue(productionLocked.findings.any { it.ruleId == "IMAGE_CANDIDATE_STATUS_FORBIDDEN" })
    }

    @Test
    fun cliWritesPromptPreparationWithoutDatabase() {
        val tempDir = Files.createTempDirectory("image-candidate-prompt-preparation")
        val briefPath = tempDir.resolve("image_candidate_brief.json")
        val out = tempDir.resolve("image_candidate_prompt_preparation.json")
        writeJson(briefPath, brief())

        val exit = Cli().run(
            listOf(
                "create-image-candidate-prompt-preparation",
                "--image-candidate-brief",
                briefPath.toString(),
                "--preparation",
                "image-candidate-prompt-prep-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val preparation = readJson<ImageCandidatePromptPreparation>(out)
        assertEquals("PASS", preparation.verdict)
        assertEquals("image_candidate_prompt_validation", preparation.nextAllowedDownstreamStep)
    }

    private fun brief(
        verdict: String = "PASS",
        briefId: String = "image-candidate-brief-001",
        decisionId: String = "asset-candidate-human-decision-001",
        imageCandidateRequirements: List<ImageCandidateRequirement> = listOf(imageRequirement()),
        nextAllowedDownstreamStep: String = "image_candidate_prompt_preparation",
    ): ImageCandidateBrief =
        ImageCandidateBrief(
            verdict = verdict,
            briefId = briefId,
            imageCandidatePreflightFile = "image_candidate_preflight.json",
            imageCandidatePreflightId = "image-candidate-preflight-001",
            assetCandidateHumanReviewDecisionIntakeFile = "asset_candidate_human_review_decision_intake.json",
            decisionId = decisionId,
            assetCandidateReviewPreflightFile = "asset_candidate_review_preflight.json",
            assetCandidateReviewPreflightId = "asset-candidate-review-preflight-001",
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
            imageCandidateRequirements = imageCandidateRequirements,
            preparationChecklist = listOf(
                ImageCandidateBriefChecklistItem("candidate-only", "candidate_boundary", "Every image candidate remains candidate-only."),
                ImageCandidateBriefChecklistItem("no-generation", "downstream_boundary", "This brief cannot be used as an image generation request."),
            ),
            downstreamBoundary = "image_candidate_brief_only",
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun imageRequirement(
        assetType: String = "prop",
        status: String = "image_candidate",
        requirement: String = "candidate only: prepare prompt-safe door mark reference exploration",
        imageOutputPath: String = "",
        generationApi: String = "",
        providerJobId: String = "",
    ): ImageCandidateRequirement =
        ImageCandidateRequirement(
            requirementId = "img-req-door-mark",
            candidateId = "asset-door-mark-candidate",
            assetType = assetType,
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
