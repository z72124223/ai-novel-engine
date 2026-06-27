package com.localnovel.engine

import com.localnovel.engine.model.CandidateReferenceHumanAcceptanceChecklistItem
import com.localnovel.engine.model.CandidateReferenceHumanAcceptanceDecision
import com.localnovel.engine.model.CandidateReferenceHumanAcceptanceDecisionIntakeReport
import com.localnovel.engine.model.ImageCandidateExternalResultItem
import com.localnovel.engine.model.ImageCandidateReferencePacketValidationReport
import com.localnovel.engine.service.CandidateReferenceHumanAcceptanceDecisionIntakeService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CandidateReferenceHumanAcceptanceDecisionIntakeTest {
    @Test
    fun passValidationAndHumanAllowDecisionAllowsRegistryPreflight() {
        val report = CandidateReferenceHumanAcceptanceDecisionIntakeService().validate(
            candidateReferencePacketValidation = validationReport(),
            humanAcceptanceDecision = decision(),
            candidateReferencePacketValidationReportFile = "candidate_reference_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
        )

        assertEquals("PASS", report.verdict)
        assertTrue(report.decisionAllowedDownstream)
        assertEquals("candidate_reference_registry_preflight", report.nextAllowedDownstreamStep)
        assertEquals(1, report.imageResults.size)
    }

    @Test
    fun upstreamNonPassOrWrongNextStepFailsDecisionIntake() {
        val nonPass = CandidateReferenceHumanAcceptanceDecisionIntakeService().validate(
            candidateReferencePacketValidation = validationReport(verdict = "FAIL"),
            humanAcceptanceDecision = decision(),
            candidateReferencePacketValidationReportFile = "candidate_reference_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
        )
        val wrongNext = CandidateReferenceHumanAcceptanceDecisionIntakeService().validate(
            candidateReferencePacketValidation = validationReport(nextAllowedDownstreamStep = "candidate_reference_registry_preflight"),
            humanAcceptanceDecision = decision(),
            candidateReferencePacketValidationReportFile = "candidate_reference_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
        )

        assertEquals("FAIL", nonPass.verdict)
        assertTrue(nonPass.findings.any { it.ruleId == "CANDIDATE_REFERENCE_PACKET_VALIDATION_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertTrue(wrongNext.findings.any { it.ruleId == "CANDIDATE_REFERENCE_PACKET_VALIDATION_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun autoSourceIncompleteChecklistAndForgedImageResultFailDecisionIntake() {
        val autoSource = CandidateReferenceHumanAcceptanceDecisionIntakeService().validate(
            candidateReferencePacketValidation = validationReport(),
            humanAcceptanceDecision = decision(decisionSource = "model"),
            candidateReferencePacketValidationReportFile = "candidate_reference_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
        )
        val incompleteChecklist = CandidateReferenceHumanAcceptanceDecisionIntakeService().validate(
            candidateReferencePacketValidation = validationReport(),
            humanAcceptanceDecision = decision(checklistItems = listOf(checklist("source_binding"))),
            candidateReferencePacketValidationReportFile = "candidate_reference_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
        )
        val forgedImage = CandidateReferenceHumanAcceptanceDecisionIntakeService().validate(
            candidateReferencePacketValidation = validationReport(),
            humanAcceptanceDecision = decision(imageResults = listOf(resultItem(resultItemId = "forged", promptItemId = "forged-prompt", sourceCandidateId = "forged-candidate"))),
            candidateReferencePacketValidationReportFile = "candidate_reference_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
        )

        assertEquals("FAIL", autoSource.verdict)
        assertTrue(autoSource.findings.any { it.ruleId == "DECISION_SOURCE_NOT_HUMAN" })
        assertEquals("FAIL", incompleteChecklist.verdict)
        assertTrue(incompleteChecklist.findings.any { it.ruleId == "HUMAN_CHECKLIST_REQUIRED_CATEGORY_MISSING" })
        assertEquals("FAIL", forgedImage.verdict)
        assertEquals("", forgedImage.nextAllowedDownstreamStep)
        assertTrue(forgedImage.findings.any { it.ruleId == "IMAGE_RESULT_SET_MISMATCH" })
    }

    @Test
    fun requestChangesAndRejectDoNotReleaseDownstream() {
        val requestChanges = CandidateReferenceHumanAcceptanceDecisionIntakeService().validate(
            candidateReferencePacketValidation = validationReport(),
            humanAcceptanceDecision = decision(decisionStatus = "REQUEST_CHANGES"),
            candidateReferencePacketValidationReportFile = "candidate_reference_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
        )
        val reject = CandidateReferenceHumanAcceptanceDecisionIntakeService().validate(
            candidateReferencePacketValidation = validationReport(),
            humanAcceptanceDecision = decision(decisionStatus = "REJECT"),
            candidateReferencePacketValidationReportFile = "candidate_reference_validation.json",
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
    fun forbiddenApprovalRemoteProviderAndCanonClaimsFailDecisionIntake() {
        val report = CandidateReferenceHumanAcceptanceDecisionIntakeService().validate(
            candidateReferencePacketValidation = validationReport(
                imageResults = listOf(resultItem(metadataOnlyReference = "manual metadata from https://provider.example/image.png", notes = "asset approved; canon applied")),
            ),
            humanAcceptanceDecision = decision(notes = "approved_reference production_locked"),
            candidateReferencePacketValidationReportFile = "candidate_reference_validation.json",
            humanAcceptanceDecisionFile = "human_acceptance_decision.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "REMOTE_METADATA_REFERENCE_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "CANDIDATE_REFERENCE_STATUS_FORBIDDEN" })
    }

    @Test
    fun cliWritesHumanAcceptanceDecisionIntakeWithoutDatabase() {
        val tempDir = Files.createTempDirectory("candidate-reference-human-acceptance")
        val validationPath = tempDir.resolve("candidate_reference_validation.json")
        val decisionPath = tempDir.resolve("human_acceptance_decision.json")
        val out = tempDir.resolve("human_acceptance_intake.json")
        writeJson(validationPath, validationReport())
        writeJson(decisionPath, decision(candidateReferencePacketValidationReportFile = validationPath.toString()))

        val exit = Cli().run(
            listOf(
                "validate-candidate-reference-human-acceptance-decision",
                "--candidate-reference-packet-validation",
                validationPath.toString(),
                "--human-acceptance-decision",
                decisionPath.toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<CandidateReferenceHumanAcceptanceDecisionIntakeReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("candidate_reference_registry_preflight", report.nextAllowedDownstreamStep)
    }

    private fun validationReport(
        verdict: String = "PASS",
        nextAllowedDownstreamStep: String = "candidate_reference_human_acceptance_ready",
        imageResults: List<ImageCandidateExternalResultItem> = listOf(resultItem()),
    ): ImageCandidateReferencePacketValidationReport =
        ImageCandidateReferencePacketValidationReport(
            verdict = verdict,
            referencePacketFile = "candidate_reference_packet.json",
            packetId = "candidate-reference-packet-001",
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
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            imageResults = imageResults,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun decision(
        candidateReferencePacketValidationReportFile: String = "candidate_reference_validation.json",
        decisionSource: String = "human",
        decisionStatus: String = "ALLOW_CANDIDATE_REFERENCE_REGISTRY_PREFLIGHT",
        checklistItems: List<CandidateReferenceHumanAcceptanceChecklistItem> = checklistItems(),
        imageResults: List<ImageCandidateExternalResultItem> = listOf(resultItem()),
        notes: String = "candidate registry preflight may be prepared",
    ): CandidateReferenceHumanAcceptanceDecision =
        CandidateReferenceHumanAcceptanceDecision(
            decisionId = "human-acceptance-decision-001",
            candidateReferencePacketValidationReportFile = candidateReferencePacketValidationReportFile,
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
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            decisionSource = decisionSource,
            reviewer = "human-reviewer",
            reviewerRole = "01_control",
            decisionAt = "2026-06-25T00:00:00Z",
            decisionStatus = decisionStatus,
            imageResults = imageResults,
            checklistItems = checklistItems,
            notes = notes,
        )

    private fun checklistItems(): List<CandidateReferenceHumanAcceptanceChecklistItem> =
        listOf("source_binding", "visual_drift", "continuity_scene_fit", "prompt_safety", "downstream_boundary", "no_approval_no_lock").map(::checklist)

    private fun checklist(category: String): CandidateReferenceHumanAcceptanceChecklistItem =
        CandidateReferenceHumanAcceptanceChecklistItem("check-$category", category, "PASS", "checked")

    private fun resultItem(
        resultItemId: String = "image-result-001",
        promptItemId: String = "img-req-door-mark-prompt",
        sourceCandidateId: String = "asset-door-mark-candidate",
        localImagePath: String = "outputs/candidates/door-mark-001.png",
        metadataOnlyReference: String = "",
        notes: String = "candidate only",
    ): ImageCandidateExternalResultItem =
        ImageCandidateExternalResultItem(
            resultItemId = resultItemId,
            promptItemId = promptItemId,
            sourceCandidateId = sourceCandidateId,
            assetType = "prop",
            candidateStatus = "external_result_candidate",
            localImagePath = localImagePath,
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
