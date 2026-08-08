package com.localnovel.engine

import com.localnovel.engine.model.ImageCandidateExternalResultIntakeReport
import com.localnovel.engine.model.ImageCandidateExternalResultItem
import com.localnovel.engine.model.ImageCandidateResultReviewPreflightReport
import com.localnovel.engine.service.ImageCandidateResultReviewPreflightService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageCandidateResultReviewPreflightTest {
    @Test
    fun passExternalResultIntakeAllowsHumanReviewReady() {
        val report = ImageCandidateResultReviewPreflightService().validate(
            externalResultIntake = intakeReport(),
            externalResultIntakeFile = "external_result_intake.json",
        )

        assertEquals("PASS", report.verdict)
        assertEquals("image_candidate_result_human_review_ready", report.nextAllowedDownstreamStep)
        assertEquals(1, report.imageResults.size)
    }

    @Test
    fun nonPassIntakeOrWrongNextStepFailsPreflight() {
        val nonPass = ImageCandidateResultReviewPreflightService().validate(
            externalResultIntake = intakeReport(verdict = "FAIL"),
            externalResultIntakeFile = "external_result_intake.json",
        )
        val wrongNext = ImageCandidateResultReviewPreflightService().validate(
            externalResultIntake = intakeReport(nextAllowedDownstreamStep = "image_candidate_result_human_review_ready"),
            externalResultIntakeFile = "external_result_intake.json",
        )

        assertEquals("FAIL", nonPass.verdict)
        assertTrue(nonPass.findings.any { it.ruleId == "EXTERNAL_RESULT_INTAKE_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertTrue(wrongNext.findings.any { it.ruleId == "EXTERNAL_RESULT_INTAKE_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun missingSourceOrReviewChecklistFailsPreflight() {
        val missingSource = ImageCandidateResultReviewPreflightService().validate(
            externalResultIntake = intakeReport(acceptedScriptPacketId = ""),
            externalResultIntakeFile = "external_result_intake.json",
        )
        val missingChecklist = ImageCandidateResultReviewPreflightService().validate(
            externalResultIntake = intakeReport(imageResults = listOf(resultItem(doNotShowChecklist = emptyList()))),
            externalResultIntakeFile = "external_result_intake.json",
        )

        assertEquals("FAIL", missingSource.verdict)
        assertTrue(missingSource.findings.any { it.ruleId == "ACCEPTED_SCRIPT_PACKET_ID_MISSING" })
        assertEquals("FAIL", missingChecklist.verdict)
        assertTrue(missingChecklist.findings.any { it.ruleId == "DO_NOT_SHOW_CHECKLIST_MISSING" })
    }

    @Test
    fun remoteProviderFieldsAndApprovalClaimsFailPreflight() {
        val report = ImageCandidateResultReviewPreflightService().validate(
            externalResultIntake = intakeReport(
                imageResults = listOf(
                    resultItem(
                        localImagePath = "https://provider.example/image.png",
                        providerJobId = "job-001",
                        apiResponse = "api response",
                        remoteUrl = "https://provider.example/image.png",
                        generatedStatus = "image generated",
                        notes = "asset approved; production locked; canon applied",
                    ),
                ),
            ),
            externalResultIntakeFile = "external_result_intake.json",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "REMOTE_IMAGE_PATH_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "PROVIDER_JOB_ID_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "API_RESPONSE_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "REMOTE_URL_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "GENERATED_STATUS_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "IMAGE_RESULT_STATUS_FORBIDDEN" })
    }

    @Test
    fun remoteMetadataOnlyReferenceFailsPreflightAndDoesNotReleaseDownstream() {
        val report = ImageCandidateResultReviewPreflightService().validate(
            externalResultIntake = intakeReport(
                imageResults = listOf(
                    resultItem(
                        localImagePath = "",
                        metadataOnlyReference = "https://provider.example/image.png",
                    ),
                ),
            ),
            externalResultIntakeFile = "external_result_intake.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "REMOTE_METADATA_REFERENCE_FORBIDDEN" })
    }

    @Test
    fun embeddedRemoteMetadataOnlyReferenceFailsPreflightAndDoesNotReleaseDownstream() {
        val embeddedUrl = ImageCandidateResultReviewPreflightService().validate(
            externalResultIntake = intakeReport(
                imageResults = listOf(
                    resultItem(
                        localImagePath = "",
                        metadataOnlyReference = "manual metadata from https://provider.example/image.png",
                    ),
                ),
            ),
            externalResultIntakeFile = "external_result_intake.json",
        )
        val embeddedProviderUri = ImageCandidateResultReviewPreflightService().validate(
            externalResultIntake = intakeReport(
                imageResults = listOf(
                    resultItem(
                        localImagePath = "",
                        metadataOnlyReference = "manual metadata from provider://job-001",
                    ),
                ),
            ),
            externalResultIntakeFile = "external_result_intake.json",
        )

        assertEquals("FAIL", embeddedUrl.verdict)
        assertEquals("", embeddedUrl.nextAllowedDownstreamStep)
        assertTrue(embeddedUrl.findings.any { it.ruleId == "REMOTE_METADATA_REFERENCE_FORBIDDEN" })
        assertEquals("FAIL", embeddedProviderUri.verdict)
        assertEquals("", embeddedProviderUri.nextAllowedDownstreamStep)
        assertTrue(embeddedProviderUri.findings.any { it.ruleId == "REMOTE_METADATA_REFERENCE_FORBIDDEN" })
    }

    @Test
    fun cliWritesResultReviewPreflightWithoutDatabase() {
        val tempDir = Files.createTempDirectory("image-result-review-preflight")
        val intakePath = tempDir.resolve("external_result_intake.json")
        val out = tempDir.resolve("result_review_preflight.json")
        writeJson(intakePath, intakeReport())

        val exit = Cli().run(
            listOf(
                "validate-image-candidate-result-review-preflight",
                "--external-result-intake",
                intakePath.toString(),
                "--preflight",
                "result-review-preflight-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<ImageCandidateResultReviewPreflightReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("result-review-preflight-001", report.preflightId)
        assertEquals("image_candidate_result_human_review_ready", report.nextAllowedDownstreamStep)
    }

    private fun intakeReport(
        verdict: String = "PASS",
        nextAllowedDownstreamStep: String = "image_candidate_result_review_preflight",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        imageResults: List<ImageCandidateExternalResultItem> = listOf(resultItem()),
    ): ImageCandidateExternalResultIntakeReport =
        ImageCandidateExternalResultIntakeReport(
            verdict = verdict,
            resultId = "external-result-001",
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            externalResultFile = "external_result.json",
            packetId = "provider-handoff-packet-001",
            preflightId = "provider-handoff-preflight-001",
            decisionId = "provider-handoff-decision-001",
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            resultStatus = "external_result_candidate",
            imageResults = imageResults,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun resultItem(
        localImagePath: String = "outputs/candidates/door-mark-001.png",
        metadataOnlyReference: String = "",
        doNotShowChecklist: List<String> = listOf("no hidden truth shown"),
        driftReviewChecklist: List<String> = listOf("matches prompt item public visual requirement"),
        residualRisks: List<String> = listOf("needs human visual review before any candidate reference packet"),
        notes: String = "candidate only",
        providerJobId: String = "",
        apiResponse: String = "",
        remoteUrl: String = "",
        generatedStatus: String = "",
    ): ImageCandidateExternalResultItem =
        ImageCandidateExternalResultItem(
            resultItemId = "image-result-001",
            promptItemId = "img-req-door-mark-prompt",
            sourceCandidateId = "asset-door-mark-candidate",
            assetType = "prop",
            candidateStatus = "external_result_candidate",
            localImagePath = localImagePath,
            metadataOnlyReference = metadataOnlyReference,
            doNotShowChecklist = doNotShowChecklist,
            driftReviewChecklist = driftReviewChecklist,
            residualRisks = residualRisks,
            notes = notes,
            providerJobId = providerJobId,
            apiResponse = apiResponse,
            remoteUrl = remoteUrl,
            generatedStatus = generatedStatus,
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
