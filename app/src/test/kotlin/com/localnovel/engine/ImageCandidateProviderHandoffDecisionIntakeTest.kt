package com.localnovel.engine

import com.localnovel.engine.model.ImageCandidatePromptItem
import com.localnovel.engine.model.ImageCandidateProviderHandoffDecision
import com.localnovel.engine.model.ImageCandidateProviderHandoffDecisionBlockingIssue
import com.localnovel.engine.model.ImageCandidateProviderHandoffDecisionChecklistItem
import com.localnovel.engine.model.ImageCandidateProviderHandoffDecisionIntakeReport
import com.localnovel.engine.model.ImageCandidateProviderHandoffPreflightReport
import com.localnovel.engine.service.ImageCandidateProviderHandoffDecisionIntakeService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageCandidateProviderHandoffDecisionIntakeTest {
    @Test
    fun passPreflightAndHumanDecisionAllowsHandoffPacket() {
        val report = ImageCandidateProviderHandoffDecisionIntakeService().validate(
            providerHandoffPreflight = preflight(),
            providerHandoffDecision = decision(),
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionFile = "provider_handoff_decision.json",
        )

        assertEquals("PASS", report.verdict)
        assertTrue(report.decisionAllowedDownstream)
        assertEquals("image_candidate_manual_provider_handoff_packet", report.nextAllowedDownstreamStep)
        assertEquals(1, report.promptItems.size)
    }

    @Test
    fun upstreamNonPassOrWrongNextStepFailsDecisionIntake() {
        val nonPass = ImageCandidateProviderHandoffDecisionIntakeService().validate(
            providerHandoffPreflight = preflight(verdict = "FAIL"),
            providerHandoffDecision = decision(),
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionFile = "provider_handoff_decision.json",
        )
        val wrongNext = ImageCandidateProviderHandoffDecisionIntakeService().validate(
            providerHandoffPreflight = preflight(nextAllowedDownstreamStep = "image_candidate_manual_provider_handoff_packet"),
            providerHandoffDecision = decision(),
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionFile = "provider_handoff_decision.json",
        )

        assertEquals("FAIL", nonPass.verdict)
        assertTrue(nonPass.findings.any { it.ruleId == "PROVIDER_HANDOFF_PREFLIGHT_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertTrue(wrongNext.findings.any { it.ruleId == "PROVIDER_HANDOFF_PREFLIGHT_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun autoSourceUnknownStatusAndMismatchFailDecisionIntake() {
        val report = ImageCandidateProviderHandoffDecisionIntakeService().validate(
            providerHandoffPreflight = preflight(),
            providerHandoffDecision = decision(
                decisionSource = "auto",
                decisionStatus = "APPROVED",
                preparationId = "other-prep",
            ),
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionFile = "provider_handoff_decision.json",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "DECISION_SOURCE_NOT_HUMAN" })
        assertTrue(report.findings.any { it.ruleId == "DECISION_STATUS_UNKNOWN" })
        assertTrue(report.findings.any { it.ruleId == "DECISION_PROMPT_PREPARATION_ID_MISMATCH" })
    }

    @Test
    fun requestChangesOrRejectDoesNotAllowDownstream() {
        val requestChanges = ImageCandidateProviderHandoffDecisionIntakeService().validate(
            providerHandoffPreflight = preflight(),
            providerHandoffDecision = decision(decisionStatus = "REQUEST_CHANGES", checklistItems = listOf(checklistItem("prompt_safety"))),
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionFile = "provider_handoff_decision.json",
        )
        val reject = ImageCandidateProviderHandoffDecisionIntakeService().validate(
            providerHandoffPreflight = preflight(),
            providerHandoffDecision = decision(decisionStatus = "REJECT", checklistItems = listOf(checklistItem("prompt_safety"))),
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionFile = "provider_handoff_decision.json",
        )

        assertEquals("PASS", requestChanges.verdict)
        assertEquals(false, requestChanges.decisionAllowedDownstream)
        assertEquals("", requestChanges.nextAllowedDownstreamStep)
        assertEquals("PASS", reject.verdict)
        assertEquals(false, reject.decisionAllowedDownstream)
        assertEquals("", reject.nextAllowedDownstreamStep)
    }

    @Test
    fun incompleteChecklistBlockingIssueAndForbiddenPromptFieldsFailDecisionIntake() {
        val shortChecklist = ImageCandidateProviderHandoffDecisionIntakeService().validate(
            providerHandoffPreflight = preflight(),
            providerHandoffDecision = decision(checklistItems = listOf(checklistItem("prompt_safety"))),
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionFile = "provider_handoff_decision.json",
        )
        val blockingAndForbidden = ImageCandidateProviderHandoffDecisionIntakeService().validate(
            providerHandoffPreflight = preflight(
                promptItems = listOf(
                    promptItem(
                        positivePromptDraft = "image generated; asset approved; canon applied",
                        providerName = "provider-x",
                        generationApi = "provider-api",
                        providerJobId = "job-001",
                        imageOutputPath = "x.png",
                    ),
                ),
            ),
            providerHandoffDecision = decision(blockingIssues = listOf(blockingIssue())),
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionFile = "provider_handoff_decision.json",
        )

        assertEquals("FAIL", shortChecklist.verdict)
        assertTrue(shortChecklist.findings.any { it.ruleId == "HUMAN_CHECKLIST_REQUIRED_CATEGORY_MISSING" })
        assertEquals("FAIL", blockingAndForbidden.verdict)
        assertTrue(blockingAndForbidden.findings.any { it.ruleId == "UNRESOLVED_BLOCKING_ISSUE_PRESENT" })
        assertTrue(blockingAndForbidden.findings.any { it.ruleId == "PROVIDER_JOB_ID_FORBIDDEN" })
        assertTrue(blockingAndForbidden.findings.any { it.ruleId == "IMAGE_OUTPUT_PATH_FORBIDDEN" })
        assertTrue(blockingAndForbidden.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
        assertTrue(blockingAndForbidden.findings.any { it.ruleId == "PROMPT_STATUS_UPGRADE_FORBIDDEN" })
    }

    @Test
    fun blankAcceptedScriptPacketIdOrScriptPacketDraftFileFailsDecisionIntake() {
        val missingAcceptedScript = ImageCandidateProviderHandoffDecisionIntakeService().validate(
            providerHandoffPreflight = preflight(acceptedScriptPacketId = ""),
            providerHandoffDecision = decision(),
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionFile = "provider_handoff_decision.json",
        )
        val missingScriptDraftFile = ImageCandidateProviderHandoffDecisionIntakeService().validate(
            providerHandoffPreflight = preflight(scriptPacketDraftFile = ""),
            providerHandoffDecision = decision(),
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionFile = "provider_handoff_decision.json",
        )

        assertEquals("FAIL", missingAcceptedScript.verdict)
        assertTrue(missingAcceptedScript.findings.any { it.ruleId == "ACCEPTED_SCRIPT_PACKET_ID_MISSING" })
        assertEquals("FAIL", missingScriptDraftFile.verdict)
        assertTrue(missingScriptDraftFile.findings.any { it.ruleId == "SCRIPT_PACKET_DRAFT_FILE_MISSING" })
    }

    @Test
    fun cliWritesDecisionIntakeWithoutDatabase() {
        val tempDir = Files.createTempDirectory("image-provider-handoff-decision")
        val preflightPath = tempDir.resolve("provider_handoff_preflight.json")
        val decisionPath = tempDir.resolve("provider_handoff_decision.json")
        val out = tempDir.resolve("provider_handoff_decision_intake.json")
        writeJson(preflightPath, preflight())
        writeJson(decisionPath, decision(providerHandoffPreflightFile = preflightPath.toString()))

        val exit = Cli().run(
            listOf(
                "validate-image-candidate-provider-handoff-decision",
                "--provider-handoff-preflight",
                preflightPath.toString(),
                "--provider-handoff-decision",
                decisionPath.toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<ImageCandidateProviderHandoffDecisionIntakeReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("image_candidate_manual_provider_handoff_packet", report.nextAllowedDownstreamStep)
    }

    private fun preflight(
        verdict: String = "PASS",
        nextAllowedDownstreamStep: String = "image_candidate_manual_provider_handoff_ready",
        promptItems: List<ImageCandidatePromptItem> = listOf(promptItem()),
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        scriptPacketDraftFile: String = "script_packet_draft.json",
    ): ImageCandidateProviderHandoffPreflightReport =
        ImageCandidateProviderHandoffPreflightReport(
            verdict = verdict,
            preflightId = "provider-handoff-preflight-001",
            imageCandidatePromptValidationReportFile = "image_candidate_prompt_validation.json",
            imageCandidatePromptPreparationFile = "image_candidate_prompt_preparation.json",
            preparationId = "image-candidate-prompt-prep-001",
            imageCandidateBriefFile = "image_candidate_brief.json",
            imageCandidateBriefId = "image-candidate-brief-001",
            imageCandidatePreflightFile = "image_candidate_preflight.json",
            imageCandidatePreflightId = "image-candidate-preflight-001",
            assetCandidateHumanReviewDecisionIntakeFile = "asset_candidate_human_review_decision_intake.json",
            decisionId = "asset-candidate-human-decision-001",
            assetCandidateReviewPreflightFile = "asset_candidate_review_preflight.json",
            assetCandidateReviewPreflightId = "asset-candidate-review-preflight-001",
            assetCandidateDraftValidationReportFile = "asset_candidate_draft_validation.json",
            assetCandidateDraftFile = "asset_candidate_draft.json",
            assetCandidateDraftId = "asset-candidate-draft-001",
            assetCandidateBriefFile = "asset_candidate_brief.json",
            assetCandidateBriefId = "asset-candidate-brief-001",
            assetCandidatePreflightFile = "asset_candidate_preflight.json",
            assetCandidatePreflightId = "asset-candidate-preflight-001",
            humanReviewDecisionIntakeFile = "human_review_decision_intake.json",
            humanReviewDecisionId = "human-review-decision-001",
            storyboardPacketReviewFile = "storyboard_packet_review.json",
            storyboardPacketReviewId = "storyboard-review-001",
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = "a".repeat(64),
            promptItems = promptItems,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun decision(
        decisionSource: String = "01_control",
        decisionStatus: String = "ALLOW_MANUAL_PROVIDER_HANDOFF_PACKET",
        providerHandoffPreflightFile: String = "provider_handoff_preflight.json",
        preparationId: String = "image-candidate-prompt-prep-001",
        checklistItems: List<ImageCandidateProviderHandoffDecisionChecklistItem> = checklist(),
        blockingIssues: List<ImageCandidateProviderHandoffDecisionBlockingIssue> = emptyList(),
    ): ImageCandidateProviderHandoffDecision =
        ImageCandidateProviderHandoffDecision(
            decisionId = "provider-handoff-decision-001",
            preflightId = "provider-handoff-preflight-001",
            providerHandoffPreflightFile = providerHandoffPreflightFile,
            imageCandidatePromptValidationReportFile = "image_candidate_prompt_validation.json",
            imageCandidatePromptPreparationFile = "image_candidate_prompt_preparation.json",
            preparationId = preparationId,
            imageCandidateBriefFile = "image_candidate_brief.json",
            imageCandidateBriefId = "image-candidate-brief-001",
            imageCandidatePreflightFile = "image_candidate_preflight.json",
            imageCandidatePreflightId = "image-candidate-preflight-001",
            assetCandidateHumanReviewDecisionIntakeFile = "asset_candidate_human_review_decision_intake.json",
            decisionSource = decisionSource,
            reviewer = "01",
            reviewerRole = "control",
            decisionAt = "2026-06-25T00:00:00Z",
            decisionStatus = decisionStatus,
            checklistItems = checklistItems,
            blockingIssues = blockingIssues,
        )

    private fun checklist(): List<ImageCandidateProviderHandoffDecisionChecklistItem> =
        listOf(
            "prompt_safety",
            "source_binding",
            "do_not_show_boundary",
            "no_provider_job_yet",
            "no_output_path",
            "no_approval_lock_canon",
        ).map(::checklistItem)

    private fun checklistItem(category: String, result: String = "PASS"): ImageCandidateProviderHandoffDecisionChecklistItem =
        ImageCandidateProviderHandoffDecisionChecklistItem(
            itemId = "$category-check",
            category = category,
            result = result,
            note = "accepted",
        )

    private fun blockingIssue(): ImageCandidateProviderHandoffDecisionBlockingIssue =
        ImageCandidateProviderHandoffDecisionBlockingIssue(
            issueId = "blocker-001",
            severity = "BLOCKING",
            status = "OPEN",
            description = "Prompt packet still has provider job metadata.",
        )

    private fun promptItem(
        positivePromptDraft: String = "candidate-only prop visual requirement: door mark reference; front view candidate reference needed",
        negativePromptDraft: String = "no production-state wording",
        doNotShow: List<String> = listOf("no production-state wording"),
        promptSafetyNotes: List<String> = listOf("public visual boundary text only; keep secret material out of prompts"),
        providerName: String = "",
        generationApi: String = "",
        providerJobId: String = "",
        imageOutputPath: String = "",
    ): ImageCandidatePromptItem =
        ImageCandidatePromptItem(
            promptItemId = "img-req-door-mark-prompt",
            sourceRequirementId = "img-req-door-mark",
            sourceCandidateId = "asset-door-mark-candidate",
            assetType = "prop",
            positivePromptDraft = positivePromptDraft,
            negativePromptDraft = negativePromptDraft,
            doNotShow = doNotShow,
            promptSafetyNotes = promptSafetyNotes,
            publicVisualRequirements = listOf("front view candidate reference needed"),
            providerName = providerName,
            generationApi = generationApi,
            providerJobId = providerJobId,
            imageOutputPath = imageOutputPath,
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
