package com.localnovel.engine

import com.localnovel.engine.model.CandidateReferenceRegistryPacket
import com.localnovel.engine.model.CandidateReferenceRegistryPacketValidationReport
import com.localnovel.engine.model.ImageCandidateExternalResultItem
import com.localnovel.engine.service.CandidateReferenceRegistryPacketValidationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CandidateReferenceRegistryPacketValidationTest {
    @Test
    fun passRegistryPacketValidationWaitsForHumanAcceptance() {
        val report = CandidateReferenceRegistryPacketValidationService().validate(
            registryPacket = packet(),
            registryPacketFile = "registry_packet.json",
        )

        assertEquals("PASS", report.verdict)
        assertEquals("candidate_reference_registry_human_acceptance_ready", report.nextAllowedDownstreamStep)
        assertEquals(1, report.imageResults.size)
        assertEquals("result-review-preflight-001", report.resultReviewPreflightId)
        assertEquals("provider-handoff-decision-001", report.providerHandoffDecisionId)
    }

    @Test
    fun nonPassWrongNextOrInvalidShaFailsRegistryPacketValidation() {
        val nonPass = CandidateReferenceRegistryPacketValidationService().validate(
            registryPacket = packet(verdict = "FAIL"),
            registryPacketFile = "registry_packet.json",
        )
        val wrongNext = CandidateReferenceRegistryPacketValidationService().validate(
            registryPacket = packet(nextAllowedDownstreamStep = "candidate_reference_registry_human_acceptance_ready"),
            registryPacketFile = "registry_packet.json",
        )
        val badHash = CandidateReferenceRegistryPacketValidationService().validate(
            registryPacket = packet(scriptPacketDraftContentSha256 = "ABC"),
            registryPacketFile = "registry_packet.json",
        )

        assertEquals("FAIL", nonPass.verdict)
        assertTrue(nonPass.findings.any { it.ruleId == "REGISTRY_PACKET_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertTrue(wrongNext.findings.any { it.ruleId == "REGISTRY_PACKET_NEXT_STEP_MISMATCH" })
        assertEquals("FAIL", badHash.verdict)
        assertTrue(badHash.findings.any { it.ruleId == "SCRIPT_PACKET_DRAFT_HASH_INVALID" })
    }

    @Test
    fun missingLineageOrImageResultFailsRegistryPacketValidation() {
        val missingLineage = CandidateReferenceRegistryPacketValidationService().validate(
            registryPacket = packet(resultReviewDecisionId = ""),
            registryPacketFile = "registry_packet.json",
        )
        val missingImageResult = CandidateReferenceRegistryPacketValidationService().validate(
            registryPacket = packet(imageResults = emptyList()),
            registryPacketFile = "registry_packet.json",
        )

        assertEquals("FAIL", missingLineage.verdict)
        assertTrue(missingLineage.findings.any { it.ruleId == "RESULT_REVIEW_DECISION_ID_MISSING" })
        assertEquals("FAIL", missingImageResult.verdict)
        assertTrue(missingImageResult.findings.any { it.ruleId == "IMAGE_RESULTS_MISSING" })
    }

    @Test
    fun forbiddenRemoteRegisteredApprovalAndProviderClaimsFailRegistryPacketValidation() {
        val report = CandidateReferenceRegistryPacketValidationService().validate(
            registryPacket = packet(
                packetStatus = "approved_reference",
                imageResults = listOf(
                    resultItem(
                        localImagePath = "http://provider.example/image.png",
                        metadataOnlyReference = "manual metadata from provider://job-001",
                        providerJobId = "job-001",
                        apiResponse = "api response",
                        remoteUrl = "https://provider.example/image.png",
                        generatedStatus = "image generated",
                        notes = "asset approved canon applied",
                    ),
                ),
            ),
            registryPacketFile = "registry_packet.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "REMOTE_IMAGE_PATH_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "REMOTE_METADATA_REFERENCE_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "PROVIDER_JOB_ID_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "API_RESPONSE_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "REMOTE_URL_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "GENERATED_STATUS_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "CANDIDATE_REFERENCE_STATUS_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
    }

    @Test
    fun cliWritesRegistryPacketValidationReportWithoutDatabase() {
        val tempDir = Files.createTempDirectory("candidate-reference-registry-packet-validation")
        val packetPath = tempDir.resolve("registry_packet.json")
        val out = tempDir.resolve("registry_packet_validation.json")
        writeJson(packetPath, packet())

        val exit = Cli().run(
            listOf(
                "validate-candidate-reference-registry-packet",
                "--candidate-reference-registry-packet",
                packetPath.toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<CandidateReferenceRegistryPacketValidationReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("candidate_reference_registry_human_acceptance_ready", report.nextAllowedDownstreamStep)
    }

    private fun packet(
        verdict: String = "PASS",
        packetStatus: String = "candidate_reference_candidate",
        nextAllowedDownstreamStep: String = "candidate_reference_registry_packet_validation",
        scriptPacketDraftContentSha256: String = "a".repeat(64),
        resultReviewDecisionId: String = "result-human-decision-001",
        imageResults: List<ImageCandidateExternalResultItem> = listOf(resultItem()),
    ): CandidateReferenceRegistryPacket =
        CandidateReferenceRegistryPacket(
            verdict = verdict,
            packetId = "registry-packet-001",
            packetStatus = packetStatus,
            registryPreflightFile = "registry_preflight.json",
            preflightId = "registry-preflight-001",
            humanAcceptanceDecisionIntakeFile = "human_acceptance_intake.json",
            decisionId = "human-acceptance-decision-001",
            candidateReferencePacketValidationReportFile = "candidate_reference_validation.json",
            referencePacketFile = "candidate_reference_packet.json",
            referencePacketId = "candidate-reference-packet-001",
            resultReviewPreflightId = "result-review-preflight-001",
            resultReviewDecisionIntakeFile = "result_decision_intake.json",
            resultReviewDecisionId = resultReviewDecisionId,
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = scriptPacketDraftContentSha256,
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            providerHandoffPacketId = "provider-handoff-packet-001",
            providerHandoffPreflightId = "provider-handoff-preflight-001",
            providerHandoffDecisionId = "provider-handoff-decision-001",
            externalResultIntakeFile = "external_result_intake.json",
            resultId = "external-result-001",
            imageResults = imageResults,
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
