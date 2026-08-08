package com.localnovel.engine

import com.localnovel.engine.model.ImageCandidateBrief
import com.localnovel.engine.model.ImageCandidatePreflightReport
import com.localnovel.engine.model.ImageCandidateRequirement
import com.localnovel.engine.service.ImageCandidateBriefService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageCandidateBriefTest {
    @Test
    fun passPreflightCreatesImageCandidateBrief() {
        val brief = ImageCandidateBriefService().create(
            imageCandidatePreflight = preflight(),
            briefId = "image-candidate-brief-001",
            imageCandidatePreflightFile = "image_candidate_preflight.json",
        )

        assertEquals("PASS", brief.verdict)
        assertEquals("image-candidate-brief-001", brief.briefId)
        assertEquals("image_candidate_prompt_preparation", brief.nextAllowedDownstreamStep)
        assertEquals(1, brief.imageCandidateRequirements.size)
        assertTrue(brief.preparationChecklist.isNotEmpty())
    }

    @Test
    fun nonPassPreflightOrWrongNextStepFailsBrief() {
        val nonPass = ImageCandidateBriefService().create(
            imageCandidatePreflight = preflight(verdict = "FAIL"),
            imageCandidatePreflightFile = "image_candidate_preflight.json",
        )
        val wrongNext = ImageCandidateBriefService().create(
            imageCandidatePreflight = preflight(nextAllowedDownstreamStep = "image_candidate_prompt_preparation"),
            imageCandidatePreflightFile = "image_candidate_preflight.json",
        )

        assertEquals("FAIL", nonPass.verdict)
        assertTrue(nonPass.findings.any { it.ruleId == "IMAGE_CANDIDATE_PREFLIGHT_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertTrue(wrongNext.findings.any { it.ruleId == "IMAGE_CANDIDATE_PREFLIGHT_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun missingSourceOrRequirementFailsBrief() {
        val missingSource = ImageCandidateBriefService().create(
            imageCandidatePreflight = preflight(decisionId = ""),
            imageCandidatePreflightFile = "image_candidate_preflight.json",
        )
        val missingRequirements = ImageCandidateBriefService().create(
            imageCandidatePreflight = preflight(imageCandidateRequirements = emptyList()),
            imageCandidatePreflightFile = "image_candidate_preflight.json",
        )

        assertEquals("FAIL", missingSource.verdict)
        assertTrue(missingSource.findings.any { it.ruleId == "DECISION_ID_MISSING" })
        assertEquals("FAIL", missingRequirements.verdict)
        assertTrue(missingRequirements.findings.any { it.ruleId == "IMAGE_CANDIDATE_REQUIREMENTS_MISSING" })
    }

    @Test
    fun missingDoNotShowOrPromptSafetyNotesFailsBrief() {
        val brief = ImageCandidateBriefService().create(
            imageCandidatePreflight = preflight(
                imageCandidateRequirements = listOf(
                    imageRequirement(doNotShow = emptyList(), promptSafetyNotes = emptyList()),
                ),
            ),
            imageCandidatePreflightFile = "image_candidate_preflight.json",
        )

        assertEquals("FAIL", brief.verdict)
        assertTrue(brief.findings.any { it.ruleId == "IMAGE_CANDIDATE_DO_NOT_SHOW_MISSING" })
        assertTrue(brief.findings.any { it.ruleId == "IMAGE_CANDIDATE_PROMPT_SAFETY_NOTES_MISSING" })
    }

    @Test
    fun forbiddenWordingOutputPathApiOrJobFailsBrief() {
        val brief = ImageCandidateBriefService().create(
            imageCandidatePreflight = preflight(
                imageCandidateRequirements = listOf(
                    imageRequirement(
                        status = "production_locked",
                        requirement = "generated image; asset approved",
                        imageOutputPath = "frame-001",
                        generationApi = "manual-provider-job",
                        providerJobId = "job-001",
                    ),
                ),
            ),
            imageCandidatePreflightFile = "image_candidate_preflight.json",
        )

        assertEquals("FAIL", brief.verdict)
        assertTrue(brief.findings.any { it.ruleId == "IMAGE_CANDIDATE_STATUS_NOT_ALLOWED" })
        assertTrue(brief.findings.any { it.ruleId == "IMAGE_CANDIDATE_STATUS_FORBIDDEN" })
        assertTrue(brief.findings.any { it.ruleId == "IMAGE_OUTPUT_PATH_FORBIDDEN" })
        assertTrue(brief.findings.any { it.ruleId == "GENERATION_API_FORBIDDEN" })
        assertTrue(brief.findings.any { it.ruleId == "PROVIDER_JOB_ID_FORBIDDEN" })
        assertTrue(brief.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
    }

    @Test
    fun cliWritesImageCandidateBriefWithoutDatabase() {
        val tempDir = Files.createTempDirectory("image-candidate-brief")
        val preflightPath = tempDir.resolve("image_candidate_preflight.json")
        val out = tempDir.resolve("image_candidate_brief.json")
        writeJson(preflightPath, preflight())

        val exit = Cli().run(
            listOf(
                "create-image-candidate-brief",
                "--image-candidate-preflight",
                preflightPath.toString(),
                "--brief",
                "image-candidate-brief-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val brief = readJson<ImageCandidateBrief>(out)
        assertEquals("PASS", brief.verdict)
        assertEquals("image_candidate_prompt_preparation", brief.nextAllowedDownstreamStep)
    }

    private fun preflight(
        verdict: String = "PASS",
        preflightId: String = "image-candidate-preflight-001",
        decisionId: String = "asset-candidate-human-decision-001",
        imageCandidateRequirements: List<ImageCandidateRequirement> = listOf(imageRequirement()),
        nextAllowedDownstreamStep: String = "image_candidate_brief",
    ): ImageCandidatePreflightReport =
        ImageCandidatePreflightReport(
            verdict = verdict,
            preflightId = preflightId,
            assetCandidateHumanReviewDecisionIntakeFile = "asset_candidate_human_review_decision_intake.json",
            decisionId = decisionId,
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
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            imageCandidateRequirements = imageCandidateRequirements,
            downstreamBoundary = "image_candidate_preflight_only",
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun imageRequirement(
        status: String = "image_candidate",
        requirement: String = "candidate only: prepare prompt-safe door mark reference exploration",
        doNotShow: List<String> = listOf("no production-state wording"),
        promptSafetyNotes: List<String> = listOf("preparation only; no media request or status upgrade"),
        imageOutputPath: String = "",
        generationApi: String = "",
        providerJobId: String = "",
    ): ImageCandidateRequirement =
        ImageCandidateRequirement(
            requirementId = "img-req-door-mark",
            candidateId = "asset-door-mark-candidate",
            assetType = "prop",
            sourceRequirementId = "asset-door-mark-candidate",
            status = status,
            requirement = requirement,
            referenceRequirements = listOf("front view candidate reference needed"),
            doNotShow = doNotShow,
            promptSafetyNotes = promptSafetyNotes,
            imageOutputPath = imageOutputPath,
            generationApi = generationApi,
            providerJobId = providerJobId,
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
