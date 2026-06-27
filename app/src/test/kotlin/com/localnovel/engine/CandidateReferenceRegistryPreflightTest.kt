package com.localnovel.engine

import com.localnovel.engine.model.CandidateReferenceHumanAcceptanceDecisionIntakeReport
import com.localnovel.engine.model.CandidateReferenceRegistryPreflightReport
import com.localnovel.engine.model.ImageCandidateExternalResultItem
import com.localnovel.engine.model.ImageCandidateReferencePacketValidationReport
import com.localnovel.engine.service.CandidateReferenceRegistryPreflightService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CandidateReferenceRegistryPreflightTest {
    @Test
    fun passDecisionIntakeAndDirectValidationAllowsRegistryPacket() {
        val report = CandidateReferenceRegistryPreflightService().validate(
            humanAcceptanceDecisionIntake = intake(),
            candidateReferencePacketValidation = validationReport(),
            humanAcceptanceDecisionIntakeFile = "human_acceptance_intake.json",
            candidateReferencePacketValidationReportFile = "candidate_reference_validation.json",
        )

        assertEquals("PASS", report.verdict)
        assertEquals("candidate_reference_registry_packet", report.nextAllowedDownstreamStep)
        assertEquals(1, report.imageResults.size)
    }

    @Test
    fun nonPassOrWrongNextStepFailsPreflight() {
        val nonPass = CandidateReferenceRegistryPreflightService().validate(
            humanAcceptanceDecisionIntake = intake(verdict = "FAIL"),
            candidateReferencePacketValidation = validationReport(),
            humanAcceptanceDecisionIntakeFile = "human_acceptance_intake.json",
            candidateReferencePacketValidationReportFile = "candidate_reference_validation.json",
        )
        val wrongNext = CandidateReferenceRegistryPreflightService().validate(
            humanAcceptanceDecisionIntake = intake(nextAllowedDownstreamStep = "candidate_reference_registry_packet"),
            candidateReferencePacketValidation = validationReport(),
            humanAcceptanceDecisionIntakeFile = "human_acceptance_intake.json",
            candidateReferencePacketValidationReportFile = "candidate_reference_validation.json",
        )

        assertEquals("FAIL", nonPass.verdict)
        assertTrue(nonPass.findings.any { it.ruleId == "HUMAN_ACCEPTANCE_DECISION_INTAKE_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertTrue(wrongNext.findings.any { it.ruleId == "DECISION_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun forgedIntakeSourceOrImageResultMismatchFailsPreflight() {
        val sourceMismatch = CandidateReferenceRegistryPreflightService().validate(
            humanAcceptanceDecisionIntake = intake(packetId = "other-packet"),
            candidateReferencePacketValidation = validationReport(),
            humanAcceptanceDecisionIntakeFile = "human_acceptance_intake.json",
            candidateReferencePacketValidationReportFile = "candidate_reference_validation.json",
        )
        val forgedImage = CandidateReferenceRegistryPreflightService().validate(
            humanAcceptanceDecisionIntake = intake(imageResults = listOf(resultItem(resultItemId = "forged", promptItemId = "forged-prompt", sourceCandidateId = "forged-candidate"))),
            candidateReferencePacketValidation = validationReport(),
            humanAcceptanceDecisionIntakeFile = "human_acceptance_intake.json",
            candidateReferencePacketValidationReportFile = "candidate_reference_validation.json",
        )

        assertEquals("FAIL", sourceMismatch.verdict)
        assertTrue(sourceMismatch.findings.any { it.ruleId == "PACKET_ID_MISMATCH" })
        assertEquals("FAIL", forgedImage.verdict)
        assertEquals("", forgedImage.nextAllowedDownstreamStep)
        assertTrue(forgedImage.findings.any { it.ruleId == "IMAGE_RESULT_SET_MISMATCH" })
    }

    @Test
    fun forbiddenRemoteProviderAndApprovalClaimsFailPreflight() {
        val report = CandidateReferenceRegistryPreflightService().validate(
            humanAcceptanceDecisionIntake = intake(
                imageResults = listOf(resultItem(metadataOnlyReference = "manual metadata from provider://job-001", notes = "approved_reference canon applied")),
            ),
            candidateReferencePacketValidation = validationReport(
                imageResults = listOf(resultItem(metadataOnlyReference = "manual metadata from provider://job-001", notes = "approved_reference canon applied")),
            ),
            humanAcceptanceDecisionIntakeFile = "human_acceptance_intake.json",
            candidateReferencePacketValidationReportFile = "candidate_reference_validation.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "REMOTE_METADATA_REFERENCE_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "CANDIDATE_REFERENCE_STATUS_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
    }

    @Test
    fun cliWritesRegistryPreflightWithoutDatabase() {
        val tempDir = Files.createTempDirectory("candidate-reference-registry-preflight")
        val intakePath = tempDir.resolve("human_acceptance_intake.json")
        val validationPath = tempDir.resolve("candidate_reference_validation.json")
        val out = tempDir.resolve("registry_preflight.json")
        writeJson(intakePath, intake(candidateReferencePacketValidationReportFile = validationPath.toString()))
        writeJson(validationPath, validationReport())

        val exit = Cli().run(
            listOf(
                "validate-candidate-reference-registry-preflight",
                "--human-acceptance-decision-intake",
                intakePath.toString(),
                "--candidate-reference-packet-validation",
                validationPath.toString(),
                "--preflight",
                "registry-preflight-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<CandidateReferenceRegistryPreflightReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("registry-preflight-001", report.preflightId)
        assertEquals("candidate_reference_registry_packet", report.nextAllowedDownstreamStep)
    }

    private fun intake(
        verdict: String = "PASS",
        nextAllowedDownstreamStep: String = "candidate_reference_registry_preflight",
        candidateReferencePacketValidationReportFile: String = "candidate_reference_validation.json",
        packetId: String = "candidate-reference-packet-001",
        imageResults: List<ImageCandidateExternalResultItem> = listOf(resultItem()),
    ): CandidateReferenceHumanAcceptanceDecisionIntakeReport =
        CandidateReferenceHumanAcceptanceDecisionIntakeReport(
            verdict = verdict,
            decisionId = "human-acceptance-decision-001",
            candidateReferencePacketValidationReportFile = candidateReferencePacketValidationReportFile,
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
            referencePacketFile = "candidate_reference_packet.json",
            packetId = packetId,
            resultReviewPreflightId = "result-review-preflight-001",
            resultReviewDecisionIntakeFile = "decision_intake.json",
            resultReviewDecisionId = "result-human-decision-001",
            externalResultIntakeFile = "external_result_intake.json",
            resultId = "external-result-001",
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            providerHandoffPacketId = "provider-handoff-packet-001",
            providerHandoffPreflightId = "provider-handoff-preflight-001",
            providerHandoffDecisionId = "provider-handoff-decision-001",
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            decisionStatus = "ALLOW_CANDIDATE_REFERENCE_REGISTRY_PREFLIGHT",
            decisionAllowedDownstream = true,
            imageResults = imageResults,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun validationReport(
        imageResults: List<ImageCandidateExternalResultItem> = listOf(resultItem()),
    ): ImageCandidateReferencePacketValidationReport =
        ImageCandidateReferencePacketValidationReport(
            verdict = "PASS",
            referencePacketFile = "candidate_reference_packet.json",
            packetId = "candidate-reference-packet-001",
            resultReviewPreflightId = "result-review-preflight-001",
            resultReviewDecisionIntakeFile = "decision_intake.json",
            decisionId = "result-human-decision-001",
            externalResultIntakeFile = "external_result_intake.json",
            resultId = "external-result-001",
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            providerHandoffPacketId = "provider-handoff-packet-001",
            providerHandoffPreflightId = "provider-handoff-preflight-001",
            providerHandoffDecisionId = "provider-handoff-decision-001",
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            imageResults = imageResults,
            nextAllowedDownstreamStep = "candidate_reference_human_acceptance_ready",
        )

    private fun resultItem(
        resultItemId: String = "image-result-001",
        promptItemId: String = "img-req-door-mark-prompt",
        sourceCandidateId: String = "asset-door-mark-candidate",
        metadataOnlyReference: String = "",
        notes: String = "candidate only",
    ): ImageCandidateExternalResultItem =
        ImageCandidateExternalResultItem(
            resultItemId = resultItemId,
            promptItemId = promptItemId,
            sourceCandidateId = sourceCandidateId,
            assetType = "prop",
            candidateStatus = "external_result_candidate",
            localImagePath = "outputs/candidates/door-mark-001.png",
            metadataOnlyReference = metadataOnlyReference,
            doNotShowChecklist = listOf("no hidden truth shown"),
            driftReviewChecklist = listOf("matches prompt item public visual requirement"),
            residualRisks = listOf("needs future validation before registry preparation"),
            notes = notes,
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
