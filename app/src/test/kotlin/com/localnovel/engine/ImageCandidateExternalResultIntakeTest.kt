package com.localnovel.engine

import com.localnovel.engine.model.ImageCandidateExternalResult
import com.localnovel.engine.model.ImageCandidateExternalResultIntakeReport
import com.localnovel.engine.model.ImageCandidateExternalResultItem
import com.localnovel.engine.model.ImageCandidateProviderHandoffPacketValidationReport
import com.localnovel.engine.model.ImageCandidatePromptItem
import com.localnovel.engine.service.ImageCandidateExternalResultIntakeService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageCandidateExternalResultIntakeTest {
    @Test
    fun passValidationAndHumanExternalResultAllowsResultReviewPreflight() {
        val report = ImageCandidateExternalResultIntakeService().validate(
            providerHandoffPacketValidation = validationReport(),
            externalResult = externalResult(),
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            externalResultFile = "external_result.json",
        )

        assertEquals("PASS", report.verdict)
        assertEquals("image_candidate_result_review_preflight", report.nextAllowedDownstreamStep)
        assertEquals(1, report.imageResults.size)
        assertEquals(1, report.evidence.localImageReferenceCount)
    }

    @Test
    fun upstreamNonPassOrWrongNextStepFailsIntake() {
        val nonPass = ImageCandidateExternalResultIntakeService().validate(
            providerHandoffPacketValidation = validationReport(verdict = "FAIL"),
            externalResult = externalResult(),
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            externalResultFile = "external_result.json",
        )
        val wrongNext = ImageCandidateExternalResultIntakeService().validate(
            providerHandoffPacketValidation = validationReport(nextAllowedDownstreamStep = "image_candidate_result_review_preflight"),
            externalResult = externalResult(),
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            externalResultFile = "external_result.json",
        )

        assertEquals("FAIL", nonPass.verdict)
        assertTrue(nonPass.findings.any { it.ruleId == "PROVIDER_HANDOFF_PACKET_VALIDATION_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertTrue(wrongNext.findings.any { it.ruleId == "PROVIDER_HANDOFF_PACKET_VALIDATION_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun sourceMismatchOrUnknownPromptFailsIntake() {
        val mismatch = ImageCandidateExternalResultIntakeService().validate(
            providerHandoffPacketValidation = validationReport(),
            externalResult = externalResult(packetId = "other-packet"),
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            externalResultFile = "external_result.json",
        )
        val unknownPrompt = ImageCandidateExternalResultIntakeService().validate(
            providerHandoffPacketValidation = validationReport(),
            externalResult = externalResult(imageResults = listOf(resultItem(promptItemId = "missing-prompt"))),
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            externalResultFile = "external_result.json",
        )

        assertEquals("FAIL", mismatch.verdict)
        assertTrue(mismatch.findings.any { it.ruleId == "PACKET_ID_MISMATCH" })
        assertEquals("FAIL", unknownPrompt.verdict)
        assertTrue(unknownPrompt.findings.any { it.ruleId == "PROMPT_ITEM_ID_NOT_FOUND" })
    }

    @Test
    fun autoSourceRemoteProviderApiAndApprovalClaimsFailIntake() {
        val report = ImageCandidateExternalResultIntakeService().validate(
            providerHandoffPacketValidation = validationReport(),
            externalResult = externalResult(
                resultSource = "auto",
                notes = "asset approved; canon applied",
                providerJobId = "job-001",
                apiResponse = "{\"provider\":\"x\"}",
                remoteUrl = "https://provider.example/result",
                imageResults = listOf(
                    resultItem(
                        localImagePath = "https://provider.example/image.png",
                        providerJobId = "job-002",
                        apiResponse = "api response",
                        remoteUrl = "https://provider.example/image.png",
                        generatedStatus = "image generated",
                    ),
                ),
            ),
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            externalResultFile = "external_result.json",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "RESULT_SOURCE_NOT_HUMAN" })
        assertTrue(report.findings.any { it.ruleId == "PROVIDER_JOB_ID_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "API_RESPONSE_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "REMOTE_URL_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "REMOTE_IMAGE_PATH_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "GENERATED_STATUS_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "IMAGE_RESULT_STATUS_FORBIDDEN" })
    }

    @Test
    fun remoteMetadataOnlyReferenceFailsIntakeAndDoesNotReleaseDownstream() {
        val report = ImageCandidateExternalResultIntakeService().validate(
            providerHandoffPacketValidation = validationReport(),
            externalResult = externalResult(
                imageResults = listOf(
                    resultItem(
                        localImagePath = "",
                        metadataOnlyReference = "https://provider.example/image.png",
                    ),
                ),
            ),
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            externalResultFile = "external_result.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "REMOTE_METADATA_REFERENCE_FORBIDDEN" })
    }

    @Test
    fun embeddedRemoteMetadataOnlyReferenceFailsIntakeAndDoesNotReleaseDownstream() {
        val embeddedUrl = ImageCandidateExternalResultIntakeService().validate(
            providerHandoffPacketValidation = validationReport(),
            externalResult = externalResult(
                imageResults = listOf(
                    resultItem(
                        localImagePath = "",
                        metadataOnlyReference = "manual metadata from https://provider.example/image.png",
                    ),
                ),
            ),
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            externalResultFile = "external_result.json",
        )
        val embeddedProviderUri = ImageCandidateExternalResultIntakeService().validate(
            providerHandoffPacketValidation = validationReport(),
            externalResult = externalResult(
                imageResults = listOf(
                    resultItem(
                        localImagePath = "",
                        metadataOnlyReference = "manual metadata from provider://job-001",
                    ),
                ),
            ),
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            externalResultFile = "external_result.json",
        )

        assertEquals("FAIL", embeddedUrl.verdict)
        assertEquals("", embeddedUrl.nextAllowedDownstreamStep)
        assertTrue(embeddedUrl.findings.any { it.ruleId == "REMOTE_METADATA_REFERENCE_FORBIDDEN" })
        assertEquals("FAIL", embeddedProviderUri.verdict)
        assertEquals("", embeddedProviderUri.nextAllowedDownstreamStep)
        assertTrue(embeddedProviderUri.findings.any { it.ruleId == "REMOTE_METADATA_REFERENCE_FORBIDDEN" })
    }

    @Test
    fun cliWritesExternalResultIntakeWithoutDatabase() {
        val tempDir = Files.createTempDirectory("image-external-result-intake")
        val validationPath = tempDir.resolve("provider_handoff_packet_validation.json")
        val externalResultPath = tempDir.resolve("external_result.json")
        val out = tempDir.resolve("external_result_intake.json")
        writeJson(validationPath, validationReport())
        writeJson(externalResultPath, externalResult(providerHandoffPacketValidationReportFile = validationPath.toString()))

        val exit = Cli().run(
            listOf(
                "validate-image-candidate-external-result-intake",
                "--provider-handoff-packet-validation",
                validationPath.toString(),
                "--external-result",
                externalResultPath.toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<ImageCandidateExternalResultIntakeReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("image_candidate_result_review_preflight", report.nextAllowedDownstreamStep)
    }

    private fun validationReport(
        verdict: String = "PASS",
        nextAllowedDownstreamStep: String = "image_candidate_manual_external_action_ready",
    ): ImageCandidateProviderHandoffPacketValidationReport =
        ImageCandidateProviderHandoffPacketValidationReport(
            verdict = verdict,
            providerHandoffPacketFile = "provider_handoff_packet.json",
            packetId = "provider-handoff-packet-001",
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionIntakeFile = "provider_handoff_decision_intake.json",
            decisionId = "provider-handoff-decision-001",
            preflightId = "provider-handoff-preflight-001",
            imageCandidatePromptValidationReportFile = "image_candidate_prompt_validation.json",
            imageCandidatePromptPreparationFile = "image_candidate_prompt_preparation.json",
            preparationId = "image-candidate-prompt-prep-001",
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            promptItems = listOf(promptItem()),
            manualInstructions = listOf("Manual external action only; do not record generated output as approved."),
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun externalResult(
        providerHandoffPacketValidationReportFile: String = "provider_handoff_packet_validation.json",
        packetId: String = "provider-handoff-packet-001",
        resultSource: String = "human",
        notes: String = "candidate result returned for review only",
        providerJobId: String = "",
        apiResponse: String = "",
        remoteUrl: String = "",
        imageResults: List<ImageCandidateExternalResultItem> = listOf(resultItem()),
    ): ImageCandidateExternalResult =
        ImageCandidateExternalResult(
            resultId = "external-result-001",
            providerHandoffPacketValidationReportFile = providerHandoffPacketValidationReportFile,
            packetId = packetId,
            preflightId = "provider-handoff-preflight-001",
            decisionId = "provider-handoff-decision-001",
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            resultSource = resultSource,
            recorder = "human-reviewer",
            recorderRole = "01_control",
            resultAt = "2026-06-25T00:00:00Z",
            resultStatus = "external_result_candidate",
            imageResults = imageResults,
            notes = notes,
            providerJobId = providerJobId,
            apiResponse = apiResponse,
            remoteUrl = remoteUrl,
        )

    private fun resultItem(
        promptItemId: String = "img-req-door-mark-prompt",
        localImagePath: String = "outputs/candidates/door-mark-001.png",
        metadataOnlyReference: String = "",
        providerJobId: String = "",
        apiResponse: String = "",
        remoteUrl: String = "",
        generatedStatus: String = "",
    ): ImageCandidateExternalResultItem =
        ImageCandidateExternalResultItem(
            resultItemId = "image-result-001",
            promptItemId = promptItemId,
            sourceCandidateId = "asset-door-mark-candidate",
            assetType = "prop",
            candidateStatus = "external_result_candidate",
            localImagePath = localImagePath,
            metadataOnlyReference = metadataOnlyReference,
            doNotShowChecklist = listOf("no hidden truth shown"),
            driftReviewChecklist = listOf("matches prompt item public visual requirement"),
            residualRisks = listOf("needs human visual review before any candidate reference packet"),
            notes = "candidate only",
            providerJobId = providerJobId,
            apiResponse = apiResponse,
            remoteUrl = remoteUrl,
            generatedStatus = generatedStatus,
        )

    private fun promptItem(): ImageCandidatePromptItem =
        ImageCandidatePromptItem(
            promptItemId = "img-req-door-mark-prompt",
            sourceRequirementId = "img-req-door-mark",
            sourceCandidateId = "asset-door-mark-candidate",
            assetType = "prop",
            positivePromptDraft = "candidate-only prop visual requirement: door mark reference",
            negativePromptDraft = "no production-state wording",
            doNotShow = listOf("no hidden truth shown"),
            promptSafetyNotes = listOf("public visual boundary text only"),
            publicVisualRequirements = listOf("front view candidate reference needed"),
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
