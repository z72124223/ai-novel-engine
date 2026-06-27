package com.localnovel.engine

import com.localnovel.engine.model.ImageCandidatePromptItem
import com.localnovel.engine.model.ImageCandidatePromptPreparation
import com.localnovel.engine.model.ImageCandidatePromptValidationReport
import com.localnovel.engine.service.ImageCandidatePromptValidationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageCandidatePromptValidationTest {
    @Test
    fun passPreparationCreatesPromptValidationReport() {
        val report = ImageCandidatePromptValidationService().validate(
            promptPreparation = preparation(),
            promptPreparationFile = "image_candidate_prompt_preparation.json",
        )

        assertEquals("PASS", report.verdict)
        assertEquals("image_candidate_provider_handoff_preflight", report.nextAllowedDownstreamStep)
        assertEquals(1, report.promptItems.size)
    }

    @Test
    fun nonPassPreparationOrWrongNextStepFailsValidation() {
        val nonPass = ImageCandidatePromptValidationService().validate(
            promptPreparation = preparation(verdict = "FAIL"),
            promptPreparationFile = "image_candidate_prompt_preparation.json",
        )
        val wrongNext = ImageCandidatePromptValidationService().validate(
            promptPreparation = preparation(nextAllowedDownstreamStep = "image_candidate_provider_handoff_preflight"),
            promptPreparationFile = "image_candidate_prompt_preparation.json",
        )

        assertEquals("FAIL", nonPass.verdict)
        assertTrue(nonPass.findings.any { it.ruleId == "PROMPT_PREPARATION_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertTrue(wrongNext.findings.any { it.ruleId == "PROMPT_PREPARATION_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun missingSourceOrPromptItemFieldsFailsValidation() {
        val missingSource = ImageCandidatePromptValidationService().validate(
            promptPreparation = preparation(decisionId = ""),
            promptPreparationFile = "image_candidate_prompt_preparation.json",
        )
        val missingPromptItem = ImageCandidatePromptValidationService().validate(
            promptPreparation = preparation(promptItems = listOf(promptItem(positivePromptDraft = "", doNotShow = emptyList(), negativePromptDraft = "", promptSafetyNotes = emptyList()))),
            promptPreparationFile = "image_candidate_prompt_preparation.json",
        )

        assertEquals("FAIL", missingSource.verdict)
        assertTrue(missingSource.findings.any { it.ruleId == "DECISION_ID_MISSING" })
        assertEquals("FAIL", missingPromptItem.verdict)
        assertTrue(missingPromptItem.findings.any { it.ruleId == "POSITIVE_PROMPT_DRAFT_MISSING" })
        assertTrue(missingPromptItem.findings.any { it.ruleId == "NEGATIVE_PROMPT_OR_DO_NOT_SHOW_MISSING" })
        assertTrue(missingPromptItem.findings.any { it.ruleId == "PROMPT_SAFETY_NOTES_MISSING" })
    }

    @Test
    fun authorOnlyProviderOutputAndForbiddenClaimsFailValidation() {
        val report = ImageCandidatePromptValidationService().validate(
            promptPreparation = preparation(
                promptItems = listOf(
                    promptItem(
                        positivePromptDraft = "author_only hidden truth secret cause; image generated; asset approved",
                        providerName = "provider-x",
                        generationApi = "manual-provider-job",
                        providerJobId = "job-001",
                        imageOutputPath = "frame-001",
                    ),
                ),
            ),
            promptPreparationFile = "image_candidate_prompt_preparation.json",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "AUTHOR_ONLY_PROMPT_TEXT_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "PROMPT_STATUS_UPGRADE_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "PROVIDER_NAME_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "GENERATION_API_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "PROVIDER_JOB_ID_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "IMAGE_OUTPUT_PATH_FORBIDDEN" })
    }

    @Test
    fun forbiddenAssetTypeStatusUpgradeOrAuthorOnlyFailsValidation() {
        val statusUpgrade = ImageCandidatePromptValidationService().validate(
            promptPreparation = preparation(promptItems = listOf(promptItem(assetType = "approved_reference"))),
            promptPreparationFile = "image_candidate_prompt_preparation.json",
        )
        val authorOnly = ImageCandidatePromptValidationService().validate(
            promptPreparation = preparation(promptItems = listOf(promptItem(assetType = "author_only"))),
            promptPreparationFile = "image_candidate_prompt_preparation.json",
        )

        assertEquals("FAIL", statusUpgrade.verdict)
        assertTrue(statusUpgrade.findings.any { it.ruleId == "PROMPT_STATUS_UPGRADE_FORBIDDEN" })
        assertEquals("FAIL", authorOnly.verdict)
        assertTrue(authorOnly.findings.any { it.ruleId == "AUTHOR_ONLY_PROMPT_TEXT_FORBIDDEN" })
    }

    @Test
    fun cliWritesPromptValidationReportWithoutDatabase() {
        val tempDir = Files.createTempDirectory("image-candidate-prompt-validation")
        val preparationPath = tempDir.resolve("image_candidate_prompt_preparation.json")
        val out = tempDir.resolve("image_candidate_prompt_validation.json")
        writeJson(preparationPath, preparation())

        val exit = Cli().run(
            listOf(
                "validate-image-candidate-prompt-preparation",
                "--image-candidate-prompt-preparation",
                preparationPath.toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<ImageCandidatePromptValidationReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("image_candidate_provider_handoff_preflight", report.nextAllowedDownstreamStep)
    }

    private fun preparation(
        verdict: String = "PASS",
        preparationId: String = "image-candidate-prompt-prep-001",
        decisionId: String = "asset-candidate-human-decision-001",
        promptItems: List<ImageCandidatePromptItem> = listOf(promptItem()),
        nextAllowedDownstreamStep: String = "image_candidate_prompt_validation",
    ): ImageCandidatePromptPreparation =
        ImageCandidatePromptPreparation(
            verdict = verdict,
            preparationId = preparationId,
            imageCandidateBriefFile = "image_candidate_brief.json",
            imageCandidateBriefId = "image-candidate-brief-001",
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
            promptItems = promptItems,
            downstreamBoundary = "image_candidate_prompt_preparation_only",
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun promptItem(
        assetType: String = "prop",
        positivePromptDraft: String = "candidate-only prop visual requirement: door mark reference; front view candidate reference needed",
        negativePromptDraft: String = "no production-state wording",
        doNotShow: List<String> = listOf("no production-state wording"),
        promptSafetyNotes: List<String> = listOf("public visual boundary text only; keep secret material out of prompts"),
        providerName: String = "",
        generationApi: String = "",
        providerJobId: String = "",
        imageOutputPath: String = "",
    ): ImageCandidatePromptItem =
        ImageCandidatePromptItem(
            promptItemId = "img-req-door-mark-prompt",
            sourceRequirementId = "img-req-door-mark",
            sourceCandidateId = "asset-door-mark-candidate",
            assetType = assetType,
            positivePromptDraft = positivePromptDraft,
            negativePromptDraft = negativePromptDraft,
            doNotShow = doNotShow,
            promptSafetyNotes = promptSafetyNotes,
            publicVisualRequirements = listOf("front view candidate reference needed"),
            providerName = providerName,
            generationApi = generationApi,
            providerJobId = providerJobId,
            imageOutputPath = imageOutputPath,
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
