package com.localnovel.engine

import com.localnovel.engine.model.ImageCandidateExternalResultItem
import com.localnovel.engine.model.ImageCandidateResultReviewDecision
import com.localnovel.engine.model.ImageCandidateResultReviewDecisionChecklistItem
import com.localnovel.engine.model.ImageCandidateResultReviewDecisionIntakeReport
import com.localnovel.engine.model.ImageCandidateResultReviewPreflightReport
import com.localnovel.engine.service.ImageCandidateResultHumanReviewDecisionIntakeService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageCandidateResultHumanReviewDecisionIntakeTest {
    @Test
    fun passPreflightAndHumanAllowDecisionAllowsCandidateReferencePacket() {
        val report = ImageCandidateResultHumanReviewDecisionIntakeService().validate(
            resultReviewPreflight = preflight(),
            resultReviewDecision = decision(),
            resultReviewPreflightFile = "result_review_preflight.json",
            resultReviewDecisionFile = "result_review_decision.json",
        )

        assertEquals("PASS", report.verdict)
        assertTrue(report.decisionAllowedDownstream)
        assertEquals("image_candidate_reference_packet", report.nextAllowedDownstreamStep)
        assertEquals(1, report.imageResults.size)
    }

    @Test
    fun nonPassPreflightOrWrongNextStepFailsDecisionIntake() {
        val nonPass = ImageCandidateResultHumanReviewDecisionIntakeService().validate(
            resultReviewPreflight = preflight(verdict = "FAIL"),
            resultReviewDecision = decision(),
            resultReviewPreflightFile = "result_review_preflight.json",
            resultReviewDecisionFile = "result_review_decision.json",
        )
        val wrongNext = ImageCandidateResultHumanReviewDecisionIntakeService().validate(
            resultReviewPreflight = preflight(nextAllowedDownstreamStep = "image_candidate_reference_packet"),
            resultReviewDecision = decision(),
            resultReviewPreflightFile = "result_review_preflight.json",
            resultReviewDecisionFile = "result_review_decision.json",
        )

        assertEquals("FAIL", nonPass.verdict)
        assertTrue(nonPass.findings.any { it.ruleId == "RESULT_REVIEW_PREFLIGHT_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertTrue(wrongNext.findings.any { it.ruleId == "RESULT_REVIEW_PREFLIGHT_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun sourceMismatchOrAutoDecisionSourceFailsDecisionIntake() {
        val mismatch = ImageCandidateResultHumanReviewDecisionIntakeService().validate(
            resultReviewPreflight = preflight(),
            resultReviewDecision = decision(resultId = "other-result"),
            resultReviewPreflightFile = "result_review_preflight.json",
            resultReviewDecisionFile = "result_review_decision.json",
        )
        val autoSource = ImageCandidateResultHumanReviewDecisionIntakeService().validate(
            resultReviewPreflight = preflight(),
            resultReviewDecision = decision(decisionSource = "auto"),
            resultReviewPreflightFile = "result_review_preflight.json",
            resultReviewDecisionFile = "result_review_decision.json",
        )

        assertEquals("FAIL", mismatch.verdict)
        assertTrue(mismatch.findings.any { it.ruleId == "DECISION_RESULT_ID_MISMATCH" })
        assertEquals("FAIL", autoSource.verdict)
        assertTrue(autoSource.findings.any { it.ruleId == "DECISION_SOURCE_NOT_HUMAN" })
    }

    @Test
    fun requestChangesAndRejectDoNotReleaseDownstream() {
        val requestChanges = ImageCandidateResultHumanReviewDecisionIntakeService().validate(
            resultReviewPreflight = preflight(),
            resultReviewDecision = decision(decisionStatus = "REQUEST_CHANGES"),
            resultReviewPreflightFile = "result_review_preflight.json",
            resultReviewDecisionFile = "result_review_decision.json",
        )
        val reject = ImageCandidateResultHumanReviewDecisionIntakeService().validate(
            resultReviewPreflight = preflight(),
            resultReviewDecision = decision(decisionStatus = "REJECT"),
            resultReviewPreflightFile = "result_review_preflight.json",
            resultReviewDecisionFile = "result_review_decision.json",
        )

        assertEquals("PASS", requestChanges.verdict)
        assertEquals(false, requestChanges.decisionAllowedDownstream)
        assertEquals("", requestChanges.nextAllowedDownstreamStep)
        assertEquals("PASS", reject.verdict)
        assertEquals(false, reject.decisionAllowedDownstream)
        assertEquals("", reject.nextAllowedDownstreamStep)
    }

    @Test
    fun allowDecisionRequiresFullPositiveChecklistCoverage() {
        val report = ImageCandidateResultHumanReviewDecisionIntakeService().validate(
            resultReviewPreflight = preflight(),
            resultReviewDecision = decision(checklistItems = listOf(checklist("downstream_boundary"))),
            resultReviewPreflightFile = "result_review_preflight.json",
            resultReviewDecisionFile = "result_review_decision.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals(false, report.decisionAllowedDownstream)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "HUMAN_CHECKLIST_REQUIRED_CATEGORY_MISSING" })
    }

    @Test
    fun forbiddenApprovalCanonAndRemoteMetadataFailDecisionIntake() {
        val report = ImageCandidateResultHumanReviewDecisionIntakeService().validate(
            resultReviewPreflight = preflight(
                imageResults = listOf(
                    resultItem(
                        localImagePath = "",
                        metadataOnlyReference = "manual metadata from https://provider.example/image.png",
                        notes = "asset approved; canon applied",
                    ),
                ),
            ),
            resultReviewDecision = decision(notes = "do not make this approved_reference or production_locked"),
            resultReviewPreflightFile = "result_review_preflight.json",
            resultReviewDecisionFile = "result_review_decision.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "REMOTE_METADATA_REFERENCE_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "IMAGE_RESULT_STATUS_FORBIDDEN" })
    }

    @Test
    fun cliWritesHumanReviewDecisionIntakeWithoutDatabase() {
        val tempDir = Files.createTempDirectory("image-result-human-review-decision")
        val preflightPath = tempDir.resolve("result_review_preflight.json")
        val decisionPath = tempDir.resolve("result_review_decision.json")
        val out = tempDir.resolve("decision_intake.json")
        writeJson(preflightPath, preflight())
        writeJson(decisionPath, decision(resultReviewPreflightFile = preflightPath.toString()))

        val exit = Cli().run(
            listOf(
                "validate-image-candidate-result-human-review-decision",
                "--result-review-preflight",
                preflightPath.toString(),
                "--result-review-decision",
                decisionPath.toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<ImageCandidateResultReviewDecisionIntakeReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("image_candidate_reference_packet", report.nextAllowedDownstreamStep)
    }

    private fun preflight(
        verdict: String = "PASS",
        nextAllowedDownstreamStep: String = "image_candidate_result_human_review_ready",
        imageResults: List<ImageCandidateExternalResultItem> = listOf(resultItem()),
    ): ImageCandidateResultReviewPreflightReport =
        ImageCandidateResultReviewPreflightReport(
            verdict = verdict,
            preflightId = "result-review-preflight-001",
            externalResultIntakeFile = "external_result_intake.json",
            resultId = "external-result-001",
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            packetId = "provider-handoff-packet-001",
            preflightSourceId = "provider-handoff-preflight-001",
            decisionId = "provider-handoff-decision-001",
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            imageResults = imageResults,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun decision(
        resultReviewPreflightFile: String = "result_review_preflight.json",
        resultId: String = "external-result-001",
        decisionSource: String = "human",
        decisionStatus: String = "ALLOW_CANDIDATE_REFERENCE_PACKET",
        checklistItems: List<ImageCandidateResultReviewDecisionChecklistItem> = checklistItems(),
        notes: String = "candidate reference packet may be prepared for later review only",
    ): ImageCandidateResultReviewDecision =
        ImageCandidateResultReviewDecision(
            decisionId = "result-human-decision-001",
            resultReviewPreflightFile = resultReviewPreflightFile,
            resultReviewPreflightId = "result-review-preflight-001",
            externalResultIntakeFile = "external_result_intake.json",
            resultId = resultId,
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            packetId = "provider-handoff-packet-001",
            preflightSourceId = "provider-handoff-preflight-001",
            providerDecisionId = "provider-handoff-decision-001",
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            decisionSource = decisionSource,
            reviewer = "human-reviewer",
            reviewerRole = "01_control",
            decisionAt = "2026-06-25T00:00:00Z",
            decisionStatus = decisionStatus,
            checklistItems = checklistItems,
            notes = notes,
        )

    private fun checklistItems(): List<ImageCandidateResultReviewDecisionChecklistItem> =
        listOf("visual_drift", "source_binding", "do_not_show_compliance", "prompt_safety", "downstream_boundary", "no_approval_no_lock").map(::checklist)

    private fun checklist(category: String): ImageCandidateResultReviewDecisionChecklistItem =
        ImageCandidateResultReviewDecisionChecklistItem(
            itemId = "check-$category",
            category = category,
            result = "PASS",
            note = "checked",
        )

    private fun resultItem(
        localImagePath: String = "outputs/candidates/door-mark-001.png",
        metadataOnlyReference: String = "",
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
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
