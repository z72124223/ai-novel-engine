package com.localnovel.engine

import com.localnovel.engine.model.CandidateReferenceRegistryPacket
import com.localnovel.engine.model.CandidateReferenceRegistryPreflightReport
import com.localnovel.engine.model.ImageCandidateExternalResultItem
import com.localnovel.engine.service.CandidateReferenceRegistryPacketService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CandidateReferenceRegistryPacketTest {
    @Test
    fun passRegistryPreflightCreatesCandidateOnlyRegistryPacket() {
        val packet = CandidateReferenceRegistryPacketService().create(
            registryPreflight = preflight(),
            registryPreflightFile = "registry_preflight.json",
        )

        assertEquals("PASS", packet.verdict)
        assertEquals("candidate_reference_candidate", packet.packetStatus)
        assertEquals("candidate_reference_registry_packet_validation", packet.nextAllowedDownstreamStep)
        assertEquals(1, packet.imageResults.size)
    }

    @Test
    fun nonPassOrWrongNextStepFailsRegistryPacket() {
        val nonPass = CandidateReferenceRegistryPacketService().create(
            registryPreflight = preflight(verdict = "FAIL"),
            registryPreflightFile = "registry_preflight.json",
        )
        val wrongNext = CandidateReferenceRegistryPacketService().create(
            registryPreflight = preflight(nextAllowedDownstreamStep = "candidate_reference_registry_packet_validation"),
            registryPreflightFile = "registry_preflight.json",
        )

        assertEquals("FAIL", nonPass.verdict)
        assertTrue(nonPass.findings.any { it.ruleId == "REGISTRY_PREFLIGHT_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertTrue(wrongNext.findings.any { it.ruleId == "REGISTRY_PREFLIGHT_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun missingSourceOrImageResultsFailsRegistryPacket() {
        val missingSource = CandidateReferenceRegistryPacketService().create(
            registryPreflight = preflight(acceptedScriptPacketId = ""),
            registryPreflightFile = "registry_preflight.json",
        )
        val missingResults = CandidateReferenceRegistryPacketService().create(
            registryPreflight = preflight(imageResults = emptyList()),
            registryPreflightFile = "registry_preflight.json",
        )

        assertEquals("FAIL", missingSource.verdict)
        assertTrue(missingSource.findings.any { it.ruleId == "ACCEPTED_SCRIPT_PACKET_ID_MISSING" })
        assertEquals("FAIL", missingResults.verdict)
        assertTrue(missingResults.findings.any { it.ruleId == "IMAGE_RESULTS_MISSING" })
    }

    @Test
    fun forbiddenRemoteProviderApprovalCanonAndGeneratedClaimsFailRegistryPacket() {
        val packet = CandidateReferenceRegistryPacketService().create(
            registryPreflight = preflight(
                imageResults = listOf(
                    resultItem(
                        localImagePath = "https://provider.example/image.png",
                        metadataOnlyReference = "manual metadata from provider://job-001",
                        providerJobId = "job-001",
                        apiResponse = "api response",
                        remoteUrl = "https://provider.example/image.png",
                        generatedStatus = "image generated",
                        notes = "approved_reference production_locked canon applied",
                    ),
                ),
            ),
            registryPreflightFile = "registry_preflight.json",
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
    fun cliWritesRegistryPacketWithoutDatabase() {
        val tempDir = Files.createTempDirectory("candidate-reference-registry-packet")
        val preflightPath = tempDir.resolve("registry_preflight.json")
        val out = tempDir.resolve("registry_packet.json")
        writeJson(preflightPath, preflight())

        val exit = Cli().run(
            listOf(
                "create-candidate-reference-registry-packet",
                "--registry-preflight",
                preflightPath.toString(),
                "--packet",
                "registry-packet-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val packet = readJson<CandidateReferenceRegistryPacket>(out)
        assertEquals("PASS", packet.verdict)
        assertEquals("registry-packet-001", packet.packetId)
        assertEquals("candidate_reference_registry_packet_validation", packet.nextAllowedDownstreamStep)
    }

    private fun preflight(
        verdict: String = "PASS",
        nextAllowedDownstreamStep: String = "candidate_reference_registry_packet",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        imageResults: List<ImageCandidateExternalResultItem> = listOf(resultItem()),
    ): CandidateReferenceRegistryPreflightReport =
        CandidateReferenceRegistryPreflightReport(
            verdict = verdict,
            preflightId = "registry-preflight-001",
            humanAcceptanceDecisionIntakeFile = "human_acceptance_intake.json",
            candidateReferencePacketValidationReportFile = "candidate_reference_validation.json",
            decisionId = "human-acceptance-decision-001",
            referencePacketFile = "candidate_reference_packet.json",
            packetId = "candidate-reference-packet-001",
            resultReviewPreflightId = "result-review-preflight-001",
            resultReviewDecisionIntakeFile = "decision_intake.json",
            resultReviewDecisionId = "result-human-decision-001",
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
