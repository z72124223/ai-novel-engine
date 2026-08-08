package com.localnovel.engine

import com.localnovel.engine.model.AssetCandidateHumanReviewDecisionIntakeReport
import com.localnovel.engine.model.ImageCandidatePreflightReport
import com.localnovel.engine.model.ImageCandidateRequirement
import com.localnovel.engine.service.ImageCandidatePreflightService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageCandidatePreflightTest {
    @Test
    fun passDecisionIntakeAllowsImageCandidateBrief() {
        val report = ImageCandidatePreflightService().validate(
            decisionIntake = intakeReport(),
            preflightId = "image-candidate-preflight-001",
            decisionIntakeFile = "asset_candidate_human_review_decision_intake.json",
        )

        assertEquals("PASS", report.verdict)
        assertEquals("image-candidate-preflight-001", report.preflightId)
        assertEquals("image_candidate_brief", report.nextAllowedDownstreamStep)
        assertEquals(1, report.imageCandidateRequirements.size)
    }

    @Test
    fun nonPassDecisionOrNoDownstreamFailsPreflight() {
        val nonPass = ImageCandidatePreflightService().validate(
            decisionIntake = intakeReport(verdict = "FAIL"),
            decisionIntakeFile = "asset_candidate_human_review_decision_intake.json",
        )
        val noDownstream = ImageCandidatePreflightService().validate(
            decisionIntake = intakeReport(decisionAllowedDownstream = false),
            decisionIntakeFile = "asset_candidate_human_review_decision_intake.json",
        )

        assertEquals("FAIL", nonPass.verdict)
        assertTrue(nonPass.findings.any { it.ruleId == "ASSET_CANDIDATE_HUMAN_REVIEW_DECISION_INTAKE_NOT_PASS" })
        assertEquals("FAIL", noDownstream.verdict)
        assertTrue(noDownstream.findings.any { it.ruleId == "DECISION_NOT_DOWNSTREAM_ALLOWED" })
    }

    @Test
    fun wrongDecisionStatusOrNextStepFailsPreflight() {
        val wrongStatus = ImageCandidatePreflightService().validate(
            decisionIntake = intakeReport(decisionStatus = "REQUEST_CHANGES"),
            decisionIntakeFile = "asset_candidate_human_review_decision_intake.json",
        )
        val wrongNext = ImageCandidatePreflightService().validate(
            decisionIntake = intakeReport(nextAllowedDownstreamStep = "image_candidate_brief"),
            decisionIntakeFile = "asset_candidate_human_review_decision_intake.json",
        )

        assertEquals("FAIL", wrongStatus.verdict)
        assertTrue(wrongStatus.findings.any { it.ruleId == "DECISION_STATUS_MISMATCH" })
        assertEquals("FAIL", wrongNext.verdict)
        assertTrue(wrongNext.findings.any { it.ruleId == "DECISION_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun missingSourceFieldOrRequirementFailsPreflight() {
        val missingSource = ImageCandidatePreflightService().validate(
            decisionIntake = intakeReport(assetCandidateDraftId = ""),
            decisionIntakeFile = "asset_candidate_human_review_decision_intake.json",
        )
        val missingRequirement = ImageCandidatePreflightService().validate(
            decisionIntake = intakeReport(imageCandidateRequirements = emptyList()),
            decisionIntakeFile = "asset_candidate_human_review_decision_intake.json",
        )

        assertEquals("FAIL", missingSource.verdict)
        assertTrue(missingSource.findings.any { it.ruleId == "ASSET_CANDIDATE_DRAFT_ID_MISSING" })
        assertEquals("FAIL", missingRequirement.verdict)
        assertTrue(missingRequirement.findings.any { it.ruleId == "IMAGE_CANDIDATE_REQUIREMENTS_MISSING" })
    }

    @Test
    fun forbiddenWordingOutputPathApiOrJobFailsPreflight() {
        val report = ImageCandidatePreflightService().validate(
            decisionIntake = intakeReport(
                imageCandidateRequirements = listOf(
                    imageRequirement(
                        status = "approved_reference",
                        requirement = "image generated; production locked",
                        imageOutputPath = "frame-001",
                        generationApi = "manual-provider-job",
                        providerJobId = "job-001",
                    ),
                ),
            ),
            decisionIntakeFile = "asset_candidate_human_review_decision_intake.json",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "IMAGE_CANDIDATE_STATUS_NOT_ALLOWED" })
        assertTrue(report.findings.any { it.ruleId == "IMAGE_CANDIDATE_STATUS_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "IMAGE_OUTPUT_PATH_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "GENERATION_API_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "PROVIDER_JOB_ID_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
    }

    @Test
    fun cliWritesImageCandidatePreflightWithoutDatabase() {
        val tempDir = Files.createTempDirectory("image-candidate-preflight")
        val intakePath = tempDir.resolve("asset_candidate_human_review_decision_intake.json")
        val out = tempDir.resolve("image_candidate_preflight.json")
        writeJson(intakePath, intakeReport())

        val exit = Cli().run(
            listOf(
                "validate-image-candidate-preflight",
                "--asset-candidate-human-review-decision-intake",
                intakePath.toString(),
                "--preflight",
                "image-candidate-preflight-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<ImageCandidatePreflightReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("image_candidate_brief", report.nextAllowedDownstreamStep)
    }

    private fun intakeReport(
        verdict: String = "PASS",
        decisionAllowedDownstream: Boolean = true,
        decisionStatus: String = "ALLOW_IMAGE_CANDIDATE_PREFLIGHT",
        nextAllowedDownstreamStep: String = "image_candidate_preflight",
        assetCandidateDraftId: String = "asset-candidate-draft-001",
        imageCandidateRequirements: List<ImageCandidateRequirement> = listOf(imageRequirement()),
    ): AssetCandidateHumanReviewDecisionIntakeReport =
        AssetCandidateHumanReviewDecisionIntakeReport(
            verdict = verdict,
            decisionId = "asset-candidate-human-decision-001",
            preflightId = "asset-candidate-review-preflight-001",
            assetCandidateReviewPreflightFile = "asset_candidate_review_preflight.json",
            assetCandidateHumanReviewDecisionFile = "asset_candidate_human_review_decision.json",
            assetCandidateDraftValidationReportFile = "asset_candidate_draft_validation.json",
            assetCandidateDraftFile = "asset_candidate_draft.json",
            assetCandidateDraftId = assetCandidateDraftId,
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
            decisionStatus = decisionStatus,
            decisionAllowedDownstream = decisionAllowedDownstream,
            imageCandidateRequirements = imageCandidateRequirements,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun imageRequirement(
        status: String = "image_candidate",
        requirement: String = "candidate only: prepare prompt-safe door mark reference exploration",
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
            doNotShow = listOf("no production-state wording"),
            promptSafetyNotes = listOf("preparation only; no media request or status upgrade"),
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
