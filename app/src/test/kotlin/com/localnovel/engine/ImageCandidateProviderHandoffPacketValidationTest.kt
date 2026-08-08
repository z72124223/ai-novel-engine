package com.localnovel.engine

import com.localnovel.engine.model.ImageCandidatePromptItem
import com.localnovel.engine.model.ImageCandidateProviderHandoffPacket
import com.localnovel.engine.model.ImageCandidateProviderHandoffPacketValidationReport
import com.localnovel.engine.service.ImageCandidateProviderHandoffPacketValidationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageCandidateProviderHandoffPacketValidationTest {
    @Test
    fun passPacketAllowsManualExternalActionReady() {
        val report = ImageCandidateProviderHandoffPacketValidationService().validate(
            providerHandoffPacket = packet(),
            providerHandoffPacketFile = "provider_handoff_packet.json",
        )

        assertEquals("PASS", report.verdict)
        assertEquals("image_candidate_manual_external_action_ready", report.nextAllowedDownstreamStep)
        assertEquals(1, report.promptItems.size)
    }

    @Test
    fun nonPassPacketOrWrongNextStepFailsValidation() {
        val nonPass = ImageCandidateProviderHandoffPacketValidationService().validate(
            providerHandoffPacket = packet(verdict = "FAIL"),
            providerHandoffPacketFile = "provider_handoff_packet.json",
        )
        val wrongNext = ImageCandidateProviderHandoffPacketValidationService().validate(
            providerHandoffPacket = packet(nextAllowedDownstreamStep = "image_candidate_manual_external_action_ready"),
            providerHandoffPacketFile = "provider_handoff_packet.json",
        )

        assertEquals("FAIL", nonPass.verdict)
        assertTrue(nonPass.findings.any { it.ruleId == "PROVIDER_HANDOFF_PACKET_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertTrue(wrongNext.findings.any { it.ruleId == "PROVIDER_HANDOFF_PACKET_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun missingSourceOrPromptItemsFailsValidation() {
        val missingSource = ImageCandidateProviderHandoffPacketValidationService().validate(
            providerHandoffPacket = packet(decisionId = ""),
            providerHandoffPacketFile = "provider_handoff_packet.json",
        )
        val missingPrompts = ImageCandidateProviderHandoffPacketValidationService().validate(
            providerHandoffPacket = packet(promptItems = emptyList()),
            providerHandoffPacketFile = "provider_handoff_packet.json",
        )

        assertEquals("FAIL", missingSource.verdict)
        assertTrue(missingSource.findings.any { it.ruleId == "DECISION_ID_MISSING" })
        assertEquals("FAIL", missingPrompts.verdict)
        assertTrue(missingPrompts.findings.any { it.ruleId == "PROMPT_ITEMS_MISSING" })
    }

    @Test
    fun blankAcceptedScriptPacketIdOrScriptPacketDraftFileFailsValidation() {
        val missingAcceptedScript = ImageCandidateProviderHandoffPacketValidationService().validate(
            providerHandoffPacket = packet(acceptedScriptPacketId = ""),
            providerHandoffPacketFile = "provider_handoff_packet.json",
        )
        val missingScriptDraftFile = ImageCandidateProviderHandoffPacketValidationService().validate(
            providerHandoffPacket = packet(scriptPacketDraftFile = ""),
            providerHandoffPacketFile = "provider_handoff_packet.json",
        )

        assertEquals("FAIL", missingAcceptedScript.verdict)
        assertTrue(missingAcceptedScript.findings.any { it.ruleId == "ACCEPTED_SCRIPT_PACKET_ID_MISSING" })
        assertEquals("FAIL", missingScriptDraftFile.verdict)
        assertTrue(missingScriptDraftFile.findings.any { it.ruleId == "SCRIPT_PACKET_DRAFT_FILE_MISSING" })
    }

    @Test
    fun providerJobSubmittedOutputGeneratedAndForbiddenClaimsFailValidation() {
        val report = ImageCandidateProviderHandoffPacketValidationService().validate(
            providerHandoffPacket = packet(
                promptItems = listOf(promptItem(positivePromptDraft = "image generated; asset approved; canon applied")),
                providerApiEndpoint = "https://provider.example/api",
                providerJobId = "job-001",
                submittedAt = "2026-06-25T00:00:00Z",
                outputPath = "x.png",
                generatedStatus = "image generated",
            ),
            providerHandoffPacketFile = "provider_handoff_packet.json",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "PROVIDER_API_ENDPOINT_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "PROVIDER_JOB_ID_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "SUBMITTED_AT_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "OUTPUT_PATH_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "GENERATED_STATUS_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "PROMPT_STATUS_UPGRADE_FORBIDDEN" })
    }

    @Test
    fun cliWritesPacketValidationWithoutDatabase() {
        val tempDir = Files.createTempDirectory("image-provider-handoff-packet-validation")
        val packetPath = tempDir.resolve("provider_handoff_packet.json")
        val out = tempDir.resolve("provider_handoff_packet_validation.json")
        writeJson(packetPath, packet())

        val exit = Cli().run(
            listOf(
                "validate-image-candidate-provider-handoff-packet",
                "--provider-handoff-packet",
                packetPath.toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<ImageCandidateProviderHandoffPacketValidationReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("image_candidate_manual_external_action_ready", report.nextAllowedDownstreamStep)
    }

    private fun packet(
        verdict: String = "PASS",
        decisionId: String = "provider-handoff-decision-001",
        promptItems: List<ImageCandidatePromptItem> = listOf(promptItem()),
        nextAllowedDownstreamStep: String = "image_candidate_provider_handoff_packet_validation",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        scriptPacketDraftFile: String = "script_packet_draft.json",
        providerApiEndpoint: String = "",
        providerJobId: String = "",
        submittedAt: String = "",
        outputPath: String = "",
        generatedStatus: String = "",
    ): ImageCandidateProviderHandoffPacket =
        ImageCandidateProviderHandoffPacket(
            verdict = verdict,
            packetId = "provider-handoff-packet-001",
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionIntakeFile = "provider_handoff_decision_intake.json",
            decisionId = decisionId,
            preflightId = "provider-handoff-preflight-001",
            imageCandidatePromptValidationReportFile = "image_candidate_prompt_validation.json",
            imageCandidatePromptPreparationFile = "image_candidate_prompt_preparation.json",
            preparationId = "image-candidate-prompt-prep-001",
            imageCandidateBriefFile = "image_candidate_brief.json",
            imageCandidateBriefId = "image-candidate-brief-001",
            imageCandidatePreflightFile = "image_candidate_preflight.json",
            imageCandidatePreflightId = "image-candidate-preflight-001",
            assetCandidateHumanReviewDecisionIntakeFile = "asset_candidate_human_review_decision_intake.json",
            assetDecisionId = "asset-candidate-human-decision-001",
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = "a".repeat(64),
            promptItems = promptItems,
            manualInstructions = listOf(
                "Manual copy only: inspect the prompt item before using any external tool.",
                "Keep negative prompt and do-not-show boundary attached to each prompt item.",
            ),
            providerApiEndpoint = providerApiEndpoint,
            providerJobId = providerJobId,
            submittedAt = submittedAt,
            outputPath = outputPath,
            generatedStatus = generatedStatus,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun promptItem(
        positivePromptDraft: String = "candidate-only prop visual requirement: door mark reference; front view candidate reference needed",
        negativePromptDraft: String = "no production-state wording",
        doNotShow: List<String> = listOf("no production-state wording"),
        promptSafetyNotes: List<String> = listOf("public visual boundary text only; keep secret material out of prompts"),
    ): ImageCandidatePromptItem =
        ImageCandidatePromptItem(
            promptItemId = "img-req-door-mark-prompt",
            sourceRequirementId = "img-req-door-mark",
            sourceCandidateId = "asset-door-mark-candidate",
            assetType = "prop",
            positivePromptDraft = positivePromptDraft,
            negativePromptDraft = negativePromptDraft,
            doNotShow = doNotShow,
            promptSafetyNotes = promptSafetyNotes,
            publicVisualRequirements = listOf("front view candidate reference needed"),
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
