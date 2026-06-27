package com.localnovel.engine

import com.localnovel.engine.model.CandidateReferenceAcceptancePacket
import com.localnovel.engine.model.CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport
import com.localnovel.engine.model.CandidateReferenceRegistryPacketValidationReport
import com.localnovel.engine.model.ImageCandidateExternalResultItem
import com.localnovel.engine.service.CandidateReferenceAcceptancePacketService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CandidateReferenceAcceptancePacketTest {
    @Test
    fun passDecisionIntakeCreatesCandidateOnlyAcceptancePacket() {
        val packet = CandidateReferenceAcceptancePacketService().create(
            registryPacketValidation = validationReport(),
            decisionIntake = decisionIntake(),
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionIntakeFile = "registry_human_acceptance_intake.json",
        )

        assertEquals("PASS", packet.verdict)
        assertEquals("candidate_reference_acceptance_candidate", packet.packetStatus)
        assertEquals("candidate_reference_acceptance_packet_validation", packet.nextAllowedDownstreamStep)
        assertEquals(1, packet.imageResults.size)
        assertTrue(packet.boundaryChecklist.all { "approved" !in it && "registered_reference" !in it })
    }

    @Test
    fun upstreamFailDecisionNotAllowedOrWrongNextStepFailsAcceptancePacket() {
        val upstreamFail = CandidateReferenceAcceptancePacketService().create(
            registryPacketValidation = validationReport(verdict = "FAIL"),
            decisionIntake = decisionIntake(),
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionIntakeFile = "registry_human_acceptance_intake.json",
        )
        val notAllowed = CandidateReferenceAcceptancePacketService().create(
            registryPacketValidation = validationReport(),
            decisionIntake = decisionIntake(decisionAllowedDownstream = false, nextAllowedDownstreamStep = ""),
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionIntakeFile = "registry_human_acceptance_intake.json",
        )
        val wrongNext = CandidateReferenceAcceptancePacketService().create(
            registryPacketValidation = validationReport(),
            decisionIntake = decisionIntake(nextAllowedDownstreamStep = "candidate_reference_acceptance_packet_validation"),
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionIntakeFile = "registry_human_acceptance_intake.json",
        )

        assertEquals("FAIL", upstreamFail.verdict)
        assertTrue(upstreamFail.findings.any { it.ruleId == "REGISTRY_PACKET_VALIDATION_NOT_PASS" })
        assertEquals("FAIL", notAllowed.verdict)
        assertTrue(notAllowed.findings.any { it.ruleId == "DECISION_NOT_DOWNSTREAM_ALLOWED" })
        assertEquals("FAIL", wrongNext.verdict)
        assertTrue(wrongNext.findings.any { it.ruleId == "DECISION_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun sourceMismatchAndForgedImageResultIdentityFailAcceptancePacket() {
        val sourceMismatch = CandidateReferenceAcceptancePacketService().create(
            registryPacketValidation = validationReport(),
            decisionIntake = decisionIntake(acceptedScriptPacketId = "forged-accepted-script-packet"),
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionIntakeFile = "registry_human_acceptance_intake.json",
        )
        val forgedImageResult = CandidateReferenceAcceptancePacketService().create(
            registryPacketValidation = validationReport(),
            decisionIntake = decisionIntake(imageResults = listOf(resultItem(resultItemId = "forged-result", localImagePath = "outputs/candidates/forged.png"))),
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionIntakeFile = "registry_human_acceptance_intake.json",
        )

        assertEquals("FAIL", sourceMismatch.verdict)
        assertTrue(sourceMismatch.findings.any { it.ruleId == "ACCEPTED_SCRIPT_PACKET_ID_MISMATCH" })
        assertEquals("FAIL", forgedImageResult.verdict)
        assertTrue(forgedImageResult.findings.any { it.ruleId == "IMAGE_RESULT_SET_MISMATCH" || it.ruleId == "IMAGE_RESULT_NOT_IN_VALIDATION_REPORT" })
        assertEquals("", forgedImageResult.nextAllowedDownstreamStep)
    }

    @Test
    fun forgedAssetTypeStatusUpgradeFailsAcceptancePacket() {
        val packet = CandidateReferenceAcceptancePacketService().create(
            registryPacketValidation = validationReport(),
            decisionIntake = decisionIntake(imageResults = listOf(resultItem(assetType = "production_locked"))),
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionIntakeFile = "registry_human_acceptance_intake.json",
        )

        assertEquals("FAIL", packet.verdict)
        assertEquals("", packet.nextAllowedDownstreamStep)
        assertTrue(packet.findings.any { it.ruleId == "IMAGE_RESULT_SOURCE_MISMATCH" || it.ruleId == "CANDIDATE_REFERENCE_STATUS_FORBIDDEN" })
    }

    @Test
    fun forbiddenRemoteProviderApprovalRegisteredAndGeneratedClaimsFailAcceptancePacket() {
        val packet = CandidateReferenceAcceptancePacketService().create(
            registryPacketValidation = validationReport(),
            decisionIntake = decisionIntake(
                imageResults = listOf(
                    resultItem(
                        localImagePath = "https://provider.example/image.png",
                        metadataOnlyReference = "manual metadata from provider://job-001",
                        providerJobId = "job-001",
                        apiResponse = "api response",
                        remoteUrl = "https://provider.example/image.png",
                        generatedStatus = "image generated",
                        candidateStatus = "production_locked",
                        notes = "registered reference canon applied",
                    ),
                ),
            ),
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionIntakeFile = "registry_human_acceptance_intake.json",
        )

        assertEquals("FAIL", packet.verdict)
        assertEquals("", packet.nextAllowedDownstreamStep)
        assertTrue(packet.findings.any { it.ruleId == "REMOTE_IMAGE_PATH_FORBIDDEN" })
        assertTrue(packet.findings.any { it.ruleId == "REMOTE_METADATA_REFERENCE_FORBIDDEN" })
        assertTrue(packet.findings.any { it.ruleId == "PROVIDER_JOB_ID_FORBIDDEN" })
        assertTrue(packet.findings.any { it.ruleId == "API_RESPONSE_FORBIDDEN" })
        assertTrue(packet.findings.any { it.ruleId == "REMOTE_URL_FORBIDDEN" })
        assertTrue(packet.findings.any { it.ruleId == "GENERATED_STATUS_FORBIDDEN" })
        assertTrue(packet.findings.any { it.ruleId == "CANDIDATE_REFERENCE_STATUS_FORBIDDEN" })
        assertTrue(packet.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
    }

    @Test
    fun cliWritesAcceptancePacketWithoutDatabase() {
        val tempDir = Files.createTempDirectory("candidate-reference-acceptance-packet")
        val validationPath = tempDir.resolve("registry_packet_validation.json")
        val intakePath = tempDir.resolve("registry_human_acceptance_intake.json")
        val out = tempDir.resolve("acceptance_packet.json")
        writeJson(validationPath, validationReport())
        writeJson(intakePath, decisionIntake(registryPacketValidationReportFile = validationPath.toString()))

        val exit = Cli().run(
            listOf(
                "create-candidate-reference-acceptance-packet",
                "--registry-packet-validation",
                validationPath.toString(),
                "--human-acceptance-decision-intake",
                intakePath.toString(),
                "--packet",
                "acceptance-packet-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val packet = readJson<CandidateReferenceAcceptancePacket>(out)
        assertEquals("PASS", packet.verdict)
        assertEquals("acceptance-packet-001", packet.packetId)
        assertEquals("candidate_reference_acceptance_packet_validation", packet.nextAllowedDownstreamStep)
    }

    private fun validationReport(
        verdict: String = "PASS",
        nextAllowedDownstreamStep: String = "candidate_reference_registry_human_acceptance_ready",
        imageResults: List<ImageCandidateExternalResultItem> = listOf(resultItem()),
    ): CandidateReferenceRegistryPacketValidationReport =
        CandidateReferenceRegistryPacketValidationReport(
            verdict = verdict,
            registryPacketFile = "registry_packet.json",
            packetId = "registry-packet-001",
            packetStatus = "candidate_reference_candidate",
            registryPreflightFile = "registry_preflight.json",
            preflightId = "registry-preflight-001",
            humanAcceptanceDecisionIntakeFile = "prior_human_acceptance_intake.json",
            decisionId = "prior-human-acceptance-decision-001",
            candidateReferencePacketValidationReportFile = "candidate_reference_validation.json",
            referencePacketFile = "candidate_reference_packet.json",
            referencePacketId = "candidate-reference-packet-001",
            resultReviewPreflightId = "result-review-preflight-001",
            resultReviewDecisionIntakeFile = "result_decision_intake.json",
            resultReviewDecisionId = "result-human-decision-001",
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            providerHandoffPacketId = "provider-handoff-packet-001",
            providerHandoffPreflightId = "provider-handoff-preflight-001",
            providerHandoffDecisionId = "provider-handoff-decision-001",
            externalResultIntakeFile = "external_result_intake.json",
            resultId = "external-result-001",
            imageResults = imageResults,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun decisionIntake(
        verdict: String = "PASS",
        decisionAllowedDownstream: Boolean = true,
        decisionStatus: String = "ALLOW_CANDIDATE_REFERENCE_ACCEPTANCE_PACKET",
        nextAllowedDownstreamStep: String = "candidate_reference_acceptance_packet",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        imageResults: List<ImageCandidateExternalResultItem> = listOf(resultItem()),
        registryPacketValidationReportFile: String = "registry_packet_validation.json",
    ): CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport =
        CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport(
            verdict = verdict,
            decisionId = "registry-human-acceptance-decision-001",
            registryPacketValidationReportFile = registryPacketValidationReportFile,
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
            registryPacketFile = "registry_packet.json",
            registryPacketId = "registry-packet-001",
            registryPreflightFile = "registry_preflight.json",
            registryPreflightId = "registry-preflight-001",
            humanAcceptanceDecisionIntakeFile = "prior_human_acceptance_intake.json",
            priorHumanAcceptanceDecisionId = "prior-human-acceptance-decision-001",
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            referencePacketFile = "candidate_reference_packet.json",
            referencePacketId = "candidate-reference-packet-001",
            resultReviewPreflightId = "result-review-preflight-001",
            resultReviewDecisionIntakeFile = "result_decision_intake.json",
            resultReviewDecisionId = "result-human-decision-001",
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            providerHandoffPacketId = "provider-handoff-packet-001",
            providerHandoffPreflightId = "provider-handoff-preflight-001",
            providerHandoffDecisionId = "provider-handoff-decision-001",
            externalResultIntakeFile = "external_result_intake.json",
            resultId = "external-result-001",
            decisionStatus = decisionStatus,
            decisionAllowedDownstream = decisionAllowedDownstream,
            imageResults = imageResults,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun resultItem(
        resultItemId: String = "image-result-001",
        promptItemId: String = "img-req-door-mark-prompt",
        sourceCandidateId: String = "asset-door-mark-candidate",
        assetType: String = "prop",
        localImagePath: String = "outputs/candidates/door-mark-001.png",
        metadataOnlyReference: String = "",
        providerJobId: String = "",
        apiResponse: String = "",
        remoteUrl: String = "",
        generatedStatus: String = "",
        candidateStatus: String = "external_result_candidate",
        notes: String = "candidate only",
    ): ImageCandidateExternalResultItem =
        ImageCandidateExternalResultItem(
            resultItemId = resultItemId,
            promptItemId = promptItemId,
            sourceCandidateId = sourceCandidateId,
            assetType = assetType,
            candidateStatus = candidateStatus,
            localImagePath = localImagePath,
            metadataOnlyReference = metadataOnlyReference,
            doNotShowChecklist = listOf("no hidden truth shown"),
            driftReviewChecklist = listOf("matches prompt item public visual requirement"),
            residualRisks = listOf("needs future validation before registry preparation"),
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
