package com.localnovel.engine

import com.localnovel.engine.model.ImageCandidatePromptItem
import com.localnovel.engine.model.ImageCandidateProviderHandoffDecisionIntakeReport
import com.localnovel.engine.model.ImageCandidateProviderHandoffPacket
import com.localnovel.engine.model.ImageCandidateProviderHandoffPreflightReport
import com.localnovel.engine.service.ImageCandidateProviderHandoffPacketService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageCandidateProviderHandoffPacketTest {
    @Test
    fun passPreflightAndDecisionIntakeCreatesPacket() {
        val packet = ImageCandidateProviderHandoffPacketService().create(
            providerHandoffPreflight = preflight(),
            decisionIntake = intake(),
            packetId = "provider-handoff-packet-001",
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionIntakeFile = "provider_handoff_decision_intake.json",
        )

        assertEquals("PASS", packet.verdict)
        assertEquals("provider-handoff-packet-001", packet.packetId)
        assertEquals("image_candidate_provider_handoff_packet_validation", packet.nextAllowedDownstreamStep)
        assertEquals(1, packet.promptItems.size)
        assertTrue(packet.manualInstructions.isNotEmpty())
        assertTrue(packet.providerJobId.isBlank())
        assertTrue(packet.outputPath.isBlank())
    }

    @Test
    fun nonPassPreflightOrDecisionNotAllowedFailsPacket() {
        val nonPassPreflight = ImageCandidateProviderHandoffPacketService().create(
            providerHandoffPreflight = preflight(verdict = "FAIL"),
            decisionIntake = intake(),
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionIntakeFile = "provider_handoff_decision_intake.json",
        )
        val notAllowed = ImageCandidateProviderHandoffPacketService().create(
            providerHandoffPreflight = preflight(),
            decisionIntake = intake(decisionAllowedDownstream = false, nextAllowedDownstreamStep = ""),
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionIntakeFile = "provider_handoff_decision_intake.json",
        )

        assertEquals("FAIL", nonPassPreflight.verdict)
        assertTrue(nonPassPreflight.findings.any { it.ruleId == "PROVIDER_HANDOFF_PREFLIGHT_NOT_PASS" })
        assertEquals("FAIL", notAllowed.verdict)
        assertTrue(notAllowed.findings.any { it.ruleId == "PROVIDER_HANDOFF_DECISION_NOT_ALLOWED_DOWNSTREAM" })
    }

    @Test
    fun sourceMismatchAndForbiddenPromptFieldsFailPacket() {
        val mismatch = ImageCandidateProviderHandoffPacketService().create(
            providerHandoffPreflight = preflight(),
            decisionIntake = intake(preparationId = "other-prep"),
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionIntakeFile = "provider_handoff_decision_intake.json",
        )
        val forbidden = ImageCandidateProviderHandoffPacketService().create(
            providerHandoffPreflight = preflight(
                promptItems = listOf(
                    promptItem(
                        positivePromptDraft = "image generated; asset approved; runtime scene created",
                        providerName = "provider-x",
                        generationApi = "provider-api",
                        providerJobId = "job-001",
                        imageOutputPath = "x.png",
                    ),
                ),
            ),
            decisionIntake = intake(),
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionIntakeFile = "provider_handoff_decision_intake.json",
        )

        assertEquals("FAIL", mismatch.verdict)
        assertTrue(mismatch.findings.any { it.ruleId == "PROMPT_PREPARATION_ID_MISMATCH" })
        assertEquals("FAIL", forbidden.verdict)
        assertTrue(forbidden.findings.any { it.ruleId == "PROVIDER_JOB_ID_FORBIDDEN" })
        assertTrue(forbidden.findings.any { it.ruleId == "IMAGE_OUTPUT_PATH_FORBIDDEN" })
        assertTrue(forbidden.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
        assertTrue(forbidden.findings.any { it.ruleId == "PROMPT_STATUS_UPGRADE_FORBIDDEN" })
    }

    @Test
    fun blankAcceptedScriptPacketIdScriptPacketDraftFileOrDecisionIdFailsPacket() {
        val missingAcceptedScript = ImageCandidateProviderHandoffPacketService().create(
            providerHandoffPreflight = preflight(acceptedScriptPacketId = ""),
            decisionIntake = intake(acceptedScriptPacketId = ""),
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionIntakeFile = "provider_handoff_decision_intake.json",
        )
        val missingScriptDraftFile = ImageCandidateProviderHandoffPacketService().create(
            providerHandoffPreflight = preflight(scriptPacketDraftFile = ""),
            decisionIntake = intake(scriptPacketDraftFile = ""),
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionIntakeFile = "provider_handoff_decision_intake.json",
        )
        val missingDecisionId = ImageCandidateProviderHandoffPacketService().create(
            providerHandoffPreflight = preflight(),
            decisionIntake = intake(decisionId = ""),
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionIntakeFile = "provider_handoff_decision_intake.json",
        )

        assertEquals("FAIL", missingAcceptedScript.verdict)
        assertTrue(missingAcceptedScript.findings.any { it.ruleId == "ACCEPTED_SCRIPT_PACKET_ID_MISSING_PREFLIGHT" || it.ruleId == "ACCEPTED_SCRIPT_PACKET_ID_MISSING" })
        assertEquals("FAIL", missingScriptDraftFile.verdict)
        assertTrue(missingScriptDraftFile.findings.any { it.ruleId == "SCRIPT_PACKET_DRAFT_FILE_MISSING_PREFLIGHT" || it.ruleId == "SCRIPT_PACKET_DRAFT_FILE_MISSING" })
        assertEquals("FAIL", missingDecisionId.verdict)
        assertTrue(missingDecisionId.findings.any { it.ruleId == "DECISION_ID_MISSING" })
    }

    @Test
    fun cliWritesPacketWithoutDatabase() {
        val tempDir = Files.createTempDirectory("image-provider-handoff-packet")
        val preflightPath = tempDir.resolve("provider_handoff_preflight.json")
        val intakePath = tempDir.resolve("provider_handoff_decision_intake.json")
        val out = tempDir.resolve("provider_handoff_packet.json")
        writeJson(preflightPath, preflight())
        writeJson(intakePath, intake())

        val exit = Cli().run(
            listOf(
                "create-image-candidate-provider-handoff-packet",
                "--provider-handoff-preflight",
                preflightPath.toString(),
                "--provider-handoff-decision-intake",
                intakePath.toString(),
                "--packet",
                "provider-handoff-packet-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val packet = readJson<ImageCandidateProviderHandoffPacket>(out)
        assertEquals("PASS", packet.verdict)
        assertEquals("image_candidate_provider_handoff_packet_validation", packet.nextAllowedDownstreamStep)
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
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = "a".repeat(64),
            promptItems = promptItems,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun intake(
        verdict: String = "PASS",
        decisionId: String = "provider-handoff-decision-001",
        decisionAllowedDownstream: Boolean = true,
        decisionStatus: String = "ALLOW_MANUAL_PROVIDER_HANDOFF_PACKET",
        preparationId: String = "image-candidate-prompt-prep-001",
        nextAllowedDownstreamStep: String = "image_candidate_manual_provider_handoff_packet",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        scriptPacketDraftFile: String = "script_packet_draft.json",
    ): ImageCandidateProviderHandoffDecisionIntakeReport =
        ImageCandidateProviderHandoffDecisionIntakeReport(
            verdict = verdict,
            decisionId = decisionId,
            preflightId = "provider-handoff-preflight-001",
            providerHandoffPreflightFile = "provider_handoff_preflight.json",
            providerHandoffDecisionFile = "provider_handoff_decision.json",
            imageCandidatePromptValidationReportFile = "image_candidate_prompt_validation.json",
            imageCandidatePromptPreparationFile = "image_candidate_prompt_preparation.json",
            preparationId = preparationId,
            imageCandidateBriefFile = "image_candidate_brief.json",
            imageCandidateBriefId = "image-candidate-brief-001",
            imageCandidatePreflightFile = "image_candidate_preflight.json",
            imageCandidatePreflightId = "image-candidate-preflight-001",
            assetCandidateHumanReviewDecisionIntakeFile = "asset_candidate_human_review_decision_intake.json",
            assetDecisionId = "asset-candidate-human-decision-001",
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = "a".repeat(64),
            decisionStatus = decisionStatus,
            decisionAllowedDownstream = decisionAllowedDownstream,
            promptItems = if (decisionAllowedDownstream) listOf(promptItem()) else emptyList(),
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
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
