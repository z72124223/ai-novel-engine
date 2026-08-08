package com.localnovel.engine

import com.localnovel.engine.model.ImageCandidatePromptItem
import com.localnovel.engine.model.ImageCandidatePromptValidationReport
import com.localnovel.engine.model.ImageCandidateProviderHandoffPreflightReport
import com.localnovel.engine.service.ImageCandidateProviderHandoffPreflightService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageCandidateProviderHandoffPreflightTest {
    @Test
    fun passPromptValidationAllowsManualProviderHandoffReady() {
        val preflight = ImageCandidateProviderHandoffPreflightService().validate(
            promptValidationReport = validationReport(),
            preflightId = "image-candidate-provider-handoff-preflight-001",
            promptValidationReportFile = "image_candidate_prompt_validation.json",
        )

        assertEquals("PASS", preflight.verdict)
        assertEquals("image-candidate-provider-handoff-preflight-001", preflight.preflightId)
        assertEquals("image_candidate_manual_provider_handoff_ready", preflight.nextAllowedDownstreamStep)
        assertEquals(1, preflight.promptItems.size)
    }

    @Test
    fun nonPassValidationOrWrongNextStepFailsPreflight() {
        val nonPass = ImageCandidateProviderHandoffPreflightService().validate(
            promptValidationReport = validationReport(verdict = "FAIL"),
            promptValidationReportFile = "image_candidate_prompt_validation.json",
        )
        val wrongNext = ImageCandidateProviderHandoffPreflightService().validate(
            promptValidationReport = validationReport(nextAllowedDownstreamStep = "image_candidate_manual_provider_handoff_ready"),
            promptValidationReportFile = "image_candidate_prompt_validation.json",
        )

        assertEquals("FAIL", nonPass.verdict)
        assertTrue(nonPass.findings.any { it.ruleId == "PROMPT_VALIDATION_REPORT_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertTrue(wrongNext.findings.any { it.ruleId == "PROMPT_VALIDATION_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun missingSourceOrPromptItemsFailsPreflight() {
        val missingSource = ImageCandidateProviderHandoffPreflightService().validate(
            promptValidationReport = validationReport(decisionId = ""),
            promptValidationReportFile = "image_candidate_prompt_validation.json",
        )
        val missingPromptItems = ImageCandidateProviderHandoffPreflightService().validate(
            promptValidationReport = validationReport(promptItems = emptyList()),
            promptValidationReportFile = "image_candidate_prompt_validation.json",
        )

        assertEquals("FAIL", missingSource.verdict)
        assertTrue(missingSource.findings.any { it.ruleId == "DECISION_ID_MISSING" })
        assertEquals("FAIL", missingPromptItems.verdict)
        assertTrue(missingPromptItems.findings.any { it.ruleId == "PROMPT_ITEMS_MISSING" })
    }

    @Test
    fun authorOnlyProviderOutputAndForbiddenClaimsFailPreflight() {
        val preflight = ImageCandidateProviderHandoffPreflightService().validate(
            promptValidationReport = validationReport(
                promptItems = listOf(
                    promptItem(
                        positivePromptDraft = "future reveal hidden motive; prompt submitted; assets approved",
                        providerName = "provider-x",
                        generationApi = "manual-provider-job",
                        providerJobId = "job-001",
                        imageOutputPath = "frame-001",
                    ),
                ),
            ),
            promptValidationReportFile = "image_candidate_prompt_validation.json",
        )

        assertEquals("FAIL", preflight.verdict)
        assertTrue(preflight.findings.any { it.ruleId == "AUTHOR_ONLY_PROMPT_TEXT_FORBIDDEN" })
        assertTrue(preflight.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
        assertTrue(preflight.findings.any { it.ruleId == "PROMPT_STATUS_UPGRADE_FORBIDDEN" })
        assertTrue(preflight.findings.any { it.ruleId == "PROVIDER_NAME_FORBIDDEN" })
        assertTrue(preflight.findings.any { it.ruleId == "GENERATION_API_FORBIDDEN" })
        assertTrue(preflight.findings.any { it.ruleId == "PROVIDER_JOB_ID_FORBIDDEN" })
        assertTrue(preflight.findings.any { it.ruleId == "IMAGE_OUTPUT_PATH_FORBIDDEN" })
    }

    @Test
    fun forbiddenAssetTypeStatusUpgradeOrAuthorOnlyFailsPreflight() {
        val statusUpgrade = ImageCandidateProviderHandoffPreflightService().validate(
            promptValidationReport = validationReport(promptItems = listOf(promptItem(assetType = "production_locked"))),
            promptValidationReportFile = "image_candidate_prompt_validation.json",
        )
        val hiddenTruth = ImageCandidateProviderHandoffPreflightService().validate(
            promptValidationReport = validationReport(promptItems = listOf(promptItem(assetType = "hidden truth"))),
            promptValidationReportFile = "image_candidate_prompt_validation.json",
        )

        assertEquals("FAIL", statusUpgrade.verdict)
        assertTrue(statusUpgrade.findings.any { it.ruleId == "PROMPT_STATUS_UPGRADE_FORBIDDEN" })
        assertEquals("FAIL", hiddenTruth.verdict)
        assertTrue(hiddenTruth.findings.any { it.ruleId == "AUTHOR_ONLY_PROMPT_TEXT_FORBIDDEN" })
    }

    @Test
    fun cliWritesProviderHandoffPreflightWithoutDatabase() {
        val tempDir = Files.createTempDirectory("image-candidate-provider-handoff-preflight")
        val validationPath = tempDir.resolve("image_candidate_prompt_validation.json")
        val out = tempDir.resolve("image_candidate_provider_handoff_preflight.json")
        writeJson(validationPath, validationReport())

        val exit = Cli().run(
            listOf(
                "validate-image-candidate-provider-handoff-preflight",
                "--image-candidate-prompt-validation",
                validationPath.toString(),
                "--preflight",
                "image-candidate-provider-handoff-preflight-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val preflight = readJson<ImageCandidateProviderHandoffPreflightReport>(out)
        assertEquals("PASS", preflight.verdict)
        assertEquals("image_candidate_manual_provider_handoff_ready", preflight.nextAllowedDownstreamStep)
    }

    private fun validationReport(
        verdict: String = "PASS",
        preparationId: String = "image-candidate-prompt-prep-001",
        decisionId: String = "asset-candidate-human-decision-001",
        promptItems: List<ImageCandidatePromptItem> = listOf(promptItem()),
        nextAllowedDownstreamStep: String = "image_candidate_provider_handoff_preflight",
    ): ImageCandidatePromptValidationReport =
        ImageCandidatePromptValidationReport(
            verdict = verdict,
            imageCandidatePromptPreparationFile = "image_candidate_prompt_preparation.json",
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
            downstreamBoundary = "image_candidate_prompt_validation_only",
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
