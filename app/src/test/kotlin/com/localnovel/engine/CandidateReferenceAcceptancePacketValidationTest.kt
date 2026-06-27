package com.localnovel.engine

import com.localnovel.engine.model.CandidateReferenceAcceptancePacket
import com.localnovel.engine.model.CandidateReferenceAcceptancePacketValidationReport
import com.localnovel.engine.model.CandidateReferenceRegistryHumanAcceptanceDecisionIntakeEvidence
import com.localnovel.engine.model.CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport
import com.localnovel.engine.model.CandidateReferenceRegistryPacketValidationReport
import com.localnovel.engine.model.ImageCandidateExternalResultItem
import com.localnovel.engine.service.CandidateReferenceAcceptancePacketService
import com.localnovel.engine.service.CandidateReferenceAcceptancePacketValidationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CandidateReferenceAcceptancePacketValidationTest {
    @Test
    fun happyPathCandidateOnlyAcceptancePacketValidates() {
        val report = CandidateReferenceAcceptancePacketValidationService().validate(
            packet = acceptancePacket(),
            acceptancePacketFile = "acceptance_packet.json",
            validationId = "acceptance-validation-001",
        )

        assertEquals("PASS", report.verdict)
        assertEquals("acceptance-validation-001", report.validationId)
        assertEquals("candidate_reference_acceptance_packet_validated", report.nextAllowedDownstreamStep)
        assertEquals(1, report.imageResults.size)
        assertEquals("01_control", report.humanAcceptanceDecisionSource)
    }

    @Test
    fun nonPassOrWrongNextStepInputFails() {
        val notPass = validate(acceptancePacket().copy(verdict = "FAIL"))
        val wrongNext = validate(acceptancePacket().copy(nextAllowedDownstreamStep = "formal_registry_write"))

        assertEquals("FAIL", notPass.verdict)
        assertTrue(notPass.findings.any { it.ruleId == "ACCEPTANCE_PACKET_NOT_PASS" })
        assertEquals("", notPass.nextAllowedDownstreamStep)
        assertEquals("FAIL", wrongNext.verdict)
        assertTrue(wrongNext.findings.any { it.ruleId == "ACCEPTANCE_PACKET_NEXT_STEP_MISMATCH" })
        assertEquals("", wrongNext.nextAllowedDownstreamStep)
    }

    @Test
    fun missingSourceFieldInvalidShaOrSourceShaMismatchFails() {
        val missingSource = validate(acceptancePacket().copy(acceptedScriptPacketId = ""))
        val invalidSha = validate(acceptancePacket().copy(scriptPacketDraftContentSha256 = "ABC"))
        val sourceShaMismatch = acceptancePacket().let { packet ->
            validate(packet.copy(evidence = packet.evidence.copy(scriptPacketDraftContentSha256 = "b".repeat(64))))
        }

        assertEquals("FAIL", missingSource.verdict)
        assertTrue(missingSource.findings.any { it.ruleId == "ACCEPTED_SCRIPT_PACKET_ID_MISSING" })
        assertEquals("FAIL", invalidSha.verdict)
        assertTrue(invalidSha.findings.any { it.ruleId == "SCRIPT_PACKET_DRAFT_HASH_INVALID" })
        assertEquals("FAIL", sourceShaMismatch.verdict)
        assertTrue(sourceShaMismatch.findings.any { it.ruleId == "SOURCE_SCRIPT_PACKET_DRAFT_HASH_MISMATCH" })
    }

    @Test
    fun forgedImageResultIdentityDigestFails() {
        val packet = acceptancePacket()
        val forged = packet.copy(
            imageResults = listOf(resultItem(resultItemId = "image-result-FORGED", localImagePath = "outputs/candidates/forged.png")),
        )
        val report = validate(forged)

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "IMAGE_RESULT_IDENTITY_DIGEST_MISMATCH" })
    }

    @Test
    fun assetTypeStatusUpgradeFails() {
        val packet = acceptancePacket()
        val forged = packet.copy(imageResults = listOf(resultItem(assetType = "approved_reference")))
        val report = validate(forged)

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "CANDIDATE_REFERENCE_STATUS_FORBIDDEN" })
    }

    @Test
    fun forbiddenRegisteredLockCanonProviderApiGeneratedAndRemoteClaimsFail() {
        val packet = acceptancePacket().copy(
            imageResults = listOf(
                resultItem(
                    candidateStatus = "production_locked",
                    metadataOnlyReference = "manual metadata from https://provider.example/image.png",
                    notes = "registered_reference canon provider job API response generated image",
                    providerJobId = "job-001",
                    apiResponse = "api result payload",
                    remoteUrl = "https://provider.example/image.png",
                    generatedStatus = "image generated",
                ),
            ),
        )
        val report = validate(packet)

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "REMOTE_METADATA_REFERENCE_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "PROVIDER_JOB_ID_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "API_RESPONSE_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "REMOTE_URL_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "GENERATED_STATUS_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "CANDIDATE_REFERENCE_STATUS_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
    }

    @Test
    fun nonHumanAcceptanceSourceFails() {
        val packet = acceptancePacket().copy(
            humanAcceptanceDecisionSource = "model",
            evidence = acceptancePacket().evidence.copy(humanAcceptanceDecisionSource = "model"),
        )
        val report = validate(packet)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "HUMAN_ACCEPTANCE_SOURCE_NOT_ALLOWED" })
    }

    @Test
    fun cliWritesValidationReportWithoutDatabase() {
        val tempDir = Files.createTempDirectory("candidate-reference-acceptance-validation")
        val packetPath = tempDir.resolve("acceptance_packet.json")
        val out = tempDir.resolve("acceptance_validation.json")
        writeJson(packetPath, acceptancePacket())

        val exit = Cli().run(
            listOf(
                "validate-candidate-reference-acceptance-packet",
                "--candidate-reference-acceptance-packet",
                packetPath.toString(),
                "--validation",
                "acceptance-validation-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<CandidateReferenceAcceptancePacketValidationReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("acceptance-validation-001", report.validationId)
        assertEquals("candidate_reference_acceptance_packet_validated", report.nextAllowedDownstreamStep)
    }

    private fun validate(packet: CandidateReferenceAcceptancePacket): CandidateReferenceAcceptancePacketValidationReport =
        CandidateReferenceAcceptancePacketValidationService().validate(
            packet = packet,
            acceptancePacketFile = "acceptance_packet.json",
        )

    private fun acceptancePacket(): CandidateReferenceAcceptancePacket =
        CandidateReferenceAcceptancePacketService().create(
            registryPacketValidation = validationReport(),
            decisionIntake = decisionIntake(),
            packetId = "acceptance-packet-001",
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionIntakeFile = "registry_human_acceptance_intake.json",
        )

    private fun validationReport(): CandidateReferenceRegistryPacketValidationReport =
        CandidateReferenceRegistryPacketValidationReport(
            verdict = "PASS",
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
            imageResults = listOf(resultItem()),
            nextAllowedDownstreamStep = "candidate_reference_registry_human_acceptance_ready",
        )

    private fun decisionIntake(): CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport =
        CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport(
            verdict = "PASS",
            decisionId = "registry-human-acceptance-decision-001",
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
            registryPacketFile = "registry_packet.json",
            registryPacketId = "registry-packet-001",
            registryPreflightFile = "registry_preflight.json",
            registryPreflightId = "registry-preflight-001",
            humanAcceptanceDecisionIntakeFile = "prior_human_acceptance_intake.json",
            priorHumanAcceptanceDecisionId = "prior-human-acceptance-decision-001",
            acceptedScriptPacketId = "accepted-script-packet-001",
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
            decisionStatus = "ALLOW_CANDIDATE_REFERENCE_ACCEPTANCE_PACKET",
            decisionAllowedDownstream = true,
            imageResults = listOf(resultItem()),
            nextAllowedDownstreamStep = "candidate_reference_acceptance_packet",
            evidence = CandidateReferenceRegistryHumanAcceptanceDecisionIntakeEvidence(
                decisionSource = "01_control",
                decisionStatus = "ALLOW_CANDIDATE_REFERENCE_ACCEPTANCE_PACKET",
            ),
        )

    private fun resultItem(
        resultItemId: String = "image-result-001",
        promptItemId: String = "img-req-door-mark-prompt",
        sourceCandidateId: String = "asset-door-mark-candidate",
        assetType: String = "prop",
        candidateStatus: String = "external_result_candidate",
        localImagePath: String = "outputs/candidates/door-mark-001.png",
        metadataOnlyReference: String = "",
        sourceFileSha256: String = "b".repeat(64),
        providerJobId: String = "",
        apiResponse: String = "",
        remoteUrl: String = "",
        generatedStatus: String = "",
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
            sourceFileSha256 = sourceFileSha256,
            doNotShowChecklist = listOf("no hidden truth shown"),
            driftReviewChecklist = listOf("matches prompt item public visual requirement"),
            residualRisks = listOf("needs future validation before registry write"),
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
