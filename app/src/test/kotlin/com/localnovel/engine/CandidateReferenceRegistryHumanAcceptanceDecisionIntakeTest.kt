package com.localnovel.engine

import com.localnovel.engine.model.CandidateReferenceRegistryHumanAcceptanceChecklistItem
import com.localnovel.engine.model.CandidateReferenceRegistryHumanAcceptanceDecision
import com.localnovel.engine.model.CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport
import com.localnovel.engine.model.CandidateReferenceRegistryPacketValidationReport
import com.localnovel.engine.model.ImageCandidateExternalResultItem
import com.localnovel.engine.service.CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CandidateReferenceRegistryHumanAcceptanceDecisionIntakeTest {
    @Test
    fun passHumanAcceptanceDecisionAllowsAcceptancePacketOnly() {
        val report = CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService().validate(
            registryPacketValidation = validationReport(),
            humanAcceptanceDecision = decision(),
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
        )

        assertEquals("PASS", report.verdict)
        assertEquals(true, report.decisionAllowedDownstream)
        assertEquals("candidate_reference_acceptance_packet", report.nextAllowedDownstreamStep)
        assertEquals(1, report.imageResults.size)
    }

    @Test
    fun requestChangesAndRejectDoNotReleaseDownstream() {
        val requestChanges = CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService().validate(
            registryPacketValidation = validationReport(),
            humanAcceptanceDecision = decision(decisionStatus = "REQUEST_CHANGES"),
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
        )
        val reject = CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService().validate(
            registryPacketValidation = validationReport(),
            humanAcceptanceDecision = decision(decisionStatus = "REJECT"),
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
        )

        assertEquals("PASS", requestChanges.verdict)
        assertEquals(false, requestChanges.decisionAllowedDownstream)
        assertEquals("", requestChanges.nextAllowedDownstreamStep)
        assertEquals("PASS", reject.verdict)
        assertEquals(false, reject.decisionAllowedDownstream)
        assertEquals("", reject.nextAllowedDownstreamStep)
    }

    @Test
    fun upstreamFailWrongNextAutoSourceAndIncompleteChecklistFail() {
        val upstreamFail = CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService().validate(
            registryPacketValidation = validationReport(verdict = "FAIL"),
            humanAcceptanceDecision = decision(),
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
        )
        val wrongNext = CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService().validate(
            registryPacketValidation = validationReport(nextAllowedDownstreamStep = "candidate_reference_acceptance_packet"),
            humanAcceptanceDecision = decision(),
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
        )
        val autoSource = CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService().validate(
            registryPacketValidation = validationReport(),
            humanAcceptanceDecision = decision(decisionSource = "model"),
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
        )
        val incompleteChecklist = CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService().validate(
            registryPacketValidation = validationReport(),
            humanAcceptanceDecision = decision(checklistItems = checklist().filterNot { it.category == "local_reference_integrity" }),
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
        )

        assertEquals("FAIL", upstreamFail.verdict)
        assertTrue(upstreamFail.findings.any { it.ruleId == "REGISTRY_PACKET_VALIDATION_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertTrue(wrongNext.findings.any { it.ruleId == "REGISTRY_PACKET_VALIDATION_NEXT_STEP_MISMATCH" })
        assertEquals("FAIL", autoSource.verdict)
        assertTrue(autoSource.findings.any { it.ruleId == "DECISION_SOURCE_NOT_HUMAN" })
        assertEquals("FAIL", incompleteChecklist.verdict)
        assertTrue(incompleteChecklist.findings.any { it.ruleId == "HUMAN_CHECKLIST_REQUIRED_CATEGORY_MISSING" })
    }

    @Test
    fun sourceMismatchAndForgedImageResultIdentityFail() {
        val sourceMismatch = CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService().validate(
            registryPacketValidation = validationReport(),
            humanAcceptanceDecision = decision(acceptedScriptPacketId = "forged-accepted-script-packet"),
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
        )
        val forgedImageResult = CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService().validate(
            registryPacketValidation = validationReport(),
            humanAcceptanceDecision = decision(imageResults = listOf(resultItem(resultItemId = "forged-result", localImagePath = "outputs/candidates/forged.png"))),
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
        )

        assertEquals("FAIL", sourceMismatch.verdict)
        assertTrue(sourceMismatch.findings.any { it.ruleId == "DECISION_ACCEPTED_SCRIPT_PACKET_ID_MISMATCH" })
        assertEquals("FAIL", forgedImageResult.verdict)
        assertTrue(forgedImageResult.findings.any { it.ruleId == "IMAGE_RESULT_SET_MISMATCH" || it.ruleId == "IMAGE_RESULT_NOT_IN_VALIDATION_REPORT" })
        assertEquals("", forgedImageResult.nextAllowedDownstreamStep)
    }

    @Test
    fun forgedAssetTypeStatusUpgradeFailsDecisionIntake() {
        val report = CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService().validate(
            registryPacketValidation = validationReport(),
            humanAcceptanceDecision = decision(imageResults = listOf(resultItem(assetType = "approved_reference"))),
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "IMAGE_RESULT_SOURCE_MISMATCH" || it.ruleId == "CANDIDATE_REFERENCE_STATUS_FORBIDDEN" })
    }

    @Test
    fun forbiddenRegisteredApprovalRemoteProviderAndGeneratedClaimsFailDecisionIntake() {
        val report = CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService().validate(
            registryPacketValidation = validationReport(),
            humanAcceptanceDecision = decision(
                notes = "asset approved canon applied",
                imageResults = listOf(
                    resultItem(
                        localImagePath = "https://provider.example/image.png",
                        metadataOnlyReference = "manual metadata from provider://job-001",
                        providerJobId = "job-001",
                        apiResponse = "api response",
                        remoteUrl = "https://provider.example/image.png",
                        generatedStatus = "image generated",
                        candidateStatus = "approved_reference",
                    ),
                ),
            ),
            registryPacketValidationReportFile = "registry_packet_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
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
    fun cliWritesHumanAcceptanceDecisionIntakeReportWithoutDatabase() {
        val tempDir = Files.createTempDirectory("candidate-reference-registry-human-acceptance")
        val validationPath = tempDir.resolve("registry_packet_validation.json")
        val decisionPath = tempDir.resolve("human_acceptance_decision.json")
        val out = tempDir.resolve("human_acceptance_intake.json")
        writeJson(validationPath, validationReport())
        writeJson(decisionPath, decision(registryPacketValidationReportFile = validationPath.toString()))

        val exit = Cli().run(
            listOf(
                "validate-candidate-reference-registry-human-acceptance-decision",
                "--registry-packet-validation",
                validationPath.toString(),
                "--human-acceptance-decision",
                decisionPath.toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("candidate_reference_acceptance_packet", report.nextAllowedDownstreamStep)
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

    private fun decision(
        decisionSource: String = "01_control",
        decisionStatus: String = "ALLOW_CANDIDATE_REFERENCE_ACCEPTANCE_PACKET",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        checklistItems: List<CandidateReferenceRegistryHumanAcceptanceChecklistItem> = checklist(),
        imageResults: List<ImageCandidateExternalResultItem> = listOf(resultItem()),
        notes: String = "allow candidate boundary packet only",
        registryPacketValidationReportFile: String = "registry_packet_validation.json",
    ): CandidateReferenceRegistryHumanAcceptanceDecision =
        CandidateReferenceRegistryHumanAcceptanceDecision(
            decisionId = "registry-human-acceptance-decision-001",
            registryPacketValidationReportFile = registryPacketValidationReportFile,
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
            decisionSource = decisionSource,
            reviewer = "01",
            reviewerRole = "controller",
            decisionAt = "2026-06-25T12:00:00Z",
            decisionStatus = decisionStatus,
            imageResults = imageResults,
            checklistItems = checklistItems,
            notes = notes,
        )

    private fun checklist(): List<CandidateReferenceRegistryHumanAcceptanceChecklistItem> =
        listOf(
            checklistItem("source_binding"),
            checklistItem("local_reference_integrity"),
            checklistItem("no_remote_provider"),
            checklistItem("visual_drift"),
            checklistItem("downstream_boundary"),
            checklistItem("no_approval_no_lock"),
        )

    private fun checklistItem(category: String): CandidateReferenceRegistryHumanAcceptanceChecklistItem =
        CandidateReferenceRegistryHumanAcceptanceChecklistItem(
            itemId = "$category-check",
            category = category,
            result = "PASS",
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
