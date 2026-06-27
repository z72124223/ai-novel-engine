package com.localnovel.engine

import com.localnovel.engine.model.ImageCandidateExternalResultItem
import com.localnovel.engine.model.ImageCandidateReferencePacket
import com.localnovel.engine.model.ImageCandidateReferencePacketValidationReport
import com.localnovel.engine.service.ImageCandidateReferencePacketValidationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageCandidateReferencePacketValidationTest {
    @Test
    fun passCandidateReferencePacketAllowsHumanAcceptanceReady() {
        val report = ImageCandidateReferencePacketValidationService().validate(
            referencePacket = packet(),
            referencePacketFile = "candidate_reference_packet.json",
        )

        assertEquals("PASS", report.verdict)
        assertEquals("candidate_reference_human_acceptance_ready", report.nextAllowedDownstreamStep)
        assertEquals(1, report.imageResults.size)
    }

    @Test
    fun nonPassPacketOrWrongNextStepFailsValidation() {
        val nonPass = ImageCandidateReferencePacketValidationService().validate(
            referencePacket = packet(verdict = "FAIL"),
            referencePacketFile = "candidate_reference_packet.json",
        )
        val wrongNext = ImageCandidateReferencePacketValidationService().validate(
            referencePacket = packet(nextAllowedDownstreamStep = "candidate_reference_human_acceptance_ready"),
            referencePacketFile = "candidate_reference_packet.json",
        )

        assertEquals("FAIL", nonPass.verdict)
        assertTrue(nonPass.findings.any { it.ruleId == "CANDIDATE_REFERENCE_PACKET_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertTrue(wrongNext.findings.any { it.ruleId == "CANDIDATE_REFERENCE_PACKET_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun missingSourceFieldsOrImageResultsFailValidation() {
        val missingSource = ImageCandidateReferencePacketValidationService().validate(
            referencePacket = packet(acceptedScriptPacketId = ""),
            referencePacketFile = "candidate_reference_packet.json",
        )
        val missingResults = ImageCandidateReferencePacketValidationService().validate(
            referencePacket = packet(imageResults = emptyList()),
            referencePacketFile = "candidate_reference_packet.json",
        )

        assertEquals("FAIL", missingSource.verdict)
        assertTrue(missingSource.findings.any { it.ruleId == "ACCEPTED_SCRIPT_PACKET_ID_MISSING" })
        assertEquals("FAIL", missingResults.verdict)
        assertTrue(missingResults.findings.any { it.ruleId == "IMAGE_RESULTS_MISSING" })
    }

    @Test
    fun approvedReferenceAndProductionLockedStatusesFailValidation() {
        val approved = ImageCandidateReferencePacketValidationService().validate(
            referencePacket = packet(packetStatus = "approved_reference"),
            referencePacketFile = "candidate_reference_packet.json",
        )
        val locked = ImageCandidateReferencePacketValidationService().validate(
            referencePacket = packet(packetStatus = "production_locked"),
            referencePacketFile = "candidate_reference_packet.json",
        )

        assertEquals("FAIL", approved.verdict)
        assertTrue(approved.findings.any { it.ruleId == "REFERENCE_PACKET_STATUS_NOT_ALLOWED" })
        assertTrue(approved.findings.any { it.ruleId == "CANDIDATE_REFERENCE_STATUS_FORBIDDEN" })
        assertEquals("FAIL", locked.verdict)
        assertTrue(locked.findings.any { it.ruleId == "REFERENCE_PACKET_STATUS_NOT_ALLOWED" })
        assertTrue(locked.findings.any { it.ruleId == "CANDIDATE_REFERENCE_STATUS_FORBIDDEN" })
    }

    @Test
    fun remoteMetadataProviderFieldsAndApprovalClaimsFailValidation() {
        val report = ImageCandidateReferencePacketValidationService().validate(
            referencePacket = packet(
                imageResults = listOf(
                    resultItem(
                        localImagePath = "https://provider.example/image.png",
                        metadataOnlyReference = "manual metadata from https://provider.example/image.png",
                        providerJobId = "job-001",
                        apiResponse = "api response",
                        remoteUrl = "https://provider.example/image.png",
                        generatedStatus = "image generated",
                        notes = "asset approved; canon applied",
                    ),
                ),
                reviewNotes = listOf("runtime scene created"),
            ),
            referencePacketFile = "candidate_reference_packet.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "REMOTE_IMAGE_PATH_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "REMOTE_METADATA_REFERENCE_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "PROVIDER_JOB_ID_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "API_RESPONSE_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "REMOTE_URL_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "GENERATED_STATUS_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "CANDIDATE_REFERENCE_STATUS_FORBIDDEN" })
    }

    @Test
    fun cliWritesCandidateReferencePacketValidationWithoutDatabase() {
        val tempDir = Files.createTempDirectory("image-candidate-reference-packet-validation")
        val packetPath = tempDir.resolve("candidate_reference_packet.json")
        val out = tempDir.resolve("candidate_reference_packet_validation.json")
        writeJson(packetPath, packet())

        val exit = Cli().run(
            listOf(
                "validate-image-candidate-reference-packet",
                "--candidate-reference-packet",
                packetPath.toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<ImageCandidateReferencePacketValidationReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("candidate_reference_human_acceptance_ready", report.nextAllowedDownstreamStep)
    }

    private fun packet(
        verdict: String = "PASS",
        packetStatus: String = "reference_candidate",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        nextAllowedDownstreamStep: String = "candidate_reference_packet_validation",
        imageResults: List<ImageCandidateExternalResultItem> = listOf(resultItem()),
        reviewNotes: List<String> = listOf("candidate packet remains ready for later human acceptance workflow"),
    ): ImageCandidateReferencePacket =
        ImageCandidateReferencePacket(
            verdict = verdict,
            packetId = "candidate-reference-packet-001",
            packetStatus = packetStatus,
            resultReviewPreflightFile = "result_review_preflight.json",
            resultReviewDecisionIntakeFile = "decision_intake.json",
            resultReviewPreflightId = "result-review-preflight-001",
            decisionId = "result-human-decision-001",
            externalResultIntakeFile = "external_result_intake.json",
            resultId = "external-result-001",
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            providerHandoffPacketId = "provider-handoff-packet-001",
            providerHandoffPreflightId = "provider-handoff-preflight-001",
            providerHandoffDecisionId = "provider-handoff-decision-001",
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            imageResults = imageResults,
            reviewChecklistSummary = listOf("visual_drift_checked", "source_binding_checked", "do_not_show_checked"),
            reviewNotes = reviewNotes,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun resultItem(
        localImagePath: String = "outputs/candidates/door-mark-001.png",
        metadataOnlyReference: String = "",
        providerJobId: String = "",
        apiResponse: String = "",
        remoteUrl: String = "",
        generatedStatus: String = "",
        notes: String = "candidate only",
    ): ImageCandidateExternalResultItem =
        ImageCandidateExternalResultItem(
            resultItemId = "image-result-001",
            promptItemId = "img-req-door-mark-prompt",
            sourceCandidateId = "asset-door-mark-candidate",
            assetType = "prop",
            candidateStatus = "external_result_candidate",
            localImagePath = localImagePath,
            metadataOnlyReference = metadataOnlyReference,
            doNotShowChecklist = listOf("no hidden truth shown"),
            driftReviewChecklist = listOf("matches prompt item public visual requirement"),
            residualRisks = listOf("needs later human acceptance before any registry work"),
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
