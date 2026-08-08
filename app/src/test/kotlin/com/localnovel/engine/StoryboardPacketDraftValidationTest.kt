package com.localnovel.engine

import com.localnovel.engine.model.AcceptedScriptPacketContinuityAnchorsSummary
import com.localnovel.engine.model.StoryboardPacketAssetCandidateDraft
import com.localnovel.engine.model.StoryboardPacketDraft
import com.localnovel.engine.model.StoryboardPacketDraftValidationReport
import com.localnovel.engine.model.StoryboardPacketFrameDraft
import com.localnovel.engine.model.StoryboardPlanningBrief
import com.localnovel.engine.model.StoryboardPlanningBriefSourceReports
import com.localnovel.engine.service.StoryboardPacketDraftValidationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StoryboardPacketDraftValidationTest {
    @Test
    fun passPlanningBriefAndTextOnlyStoryboardPacketDraftPassesReviewStep() {
        val report = StoryboardPacketDraftValidationService().validate(
            storyboardPlanningBrief = planningBrief(),
            storyboardPacketDraft = storyboardPacketDraft(),
            storyboardPlanningBriefFile = "storyboard_planning_brief.json",
            storyboardPacketDraftFile = "storyboard_packet_draft.json",
        )

        assertEquals("PASS", report.verdict)
        assertEquals("storyboard_packet_review", report.nextAllowedDownstreamStep)
        assertEquals("storyboard-brief-001", report.storyboardPlanningBriefId)
        assertEquals("storyboard-packet-draft-001", report.storyboardPacketDraftId)
        assertEquals(1, report.evidence.frameCount)
        assertEquals(2, report.evidence.protectedContinuityAnchorCount)
        assertEquals(2, report.evidence.protectedContinuityAnchorReferencedCount)
    }

    @Test
    fun nonPassPlanningBriefFailsStoryboardPacketDraftGate() {
        val report = StoryboardPacketDraftValidationService().validate(
            storyboardPlanningBrief = planningBrief(verdict = "FAIL"),
            storyboardPacketDraft = storyboardPacketDraft(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "STORYBOARD_PLANNING_BRIEF_NOT_PASS" })
    }

    @Test
    fun wrongPlanningBriefNextStepFailsStoryboardPacketDraftGate() {
        val report = StoryboardPacketDraftValidationService().validate(
            storyboardPlanningBrief = planningBrief(nextAllowedDownstreamStep = "asset_candidate_preflight"),
            storyboardPacketDraft = storyboardPacketDraft(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "PLANNING_BRIEF_NEXT_STEP_NOT_STORYBOARD_PACKET_DRAFT" })
    }

    @Test
    fun draftSourceBindingMissingOrMismatchFails() {
        val missing = StoryboardPacketDraftValidationService().validate(
            storyboardPlanningBrief = planningBrief(),
            storyboardPacketDraft = storyboardPacketDraft(storyboardPlanningBriefId = ""),
        )
        val mismatch = StoryboardPacketDraftValidationService().validate(
            storyboardPlanningBrief = planningBrief(),
            storyboardPacketDraft = storyboardPacketDraft(scriptPacketDraftContentSha256 = "b".repeat(64)),
        )

        assertEquals("FAIL", missing.verdict)
        assertTrue(missing.findings.any { it.ruleId == "STORYBOARD_PLANNING_BRIEF_ID_MISSING" })
        assertEquals("FAIL", mismatch.verdict)
        assertTrue(mismatch.findings.any { it.ruleId == "SCRIPT_PACKET_DRAFT_HASH_MISMATCH" })
    }

    @Test
    fun missingPureChineseProtectedAnchorFails() {
        val report = StoryboardPacketDraftValidationService().validate(
            storyboardPlanningBrief = planningBrief(),
            storyboardPacketDraft = storyboardPacketDraft(
                protectedContinuityAnchors = listOf("The guard requires a visible mark before answering questions."),
                frames = listOf(
                    storyboardFrame(
                        mustShow = listOf("The guard studies the visible mark before answering."),
                        continuityAnchors = listOf("The guard requires a visible mark before answering questions."),
                    ),
                ),
            ),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals(2, report.evidence.protectedContinuityAnchorCount)
        assertEquals(1, report.evidence.protectedContinuityAnchorReferencedCount)
        assertTrue(report.findings.any { it.ruleId == "PROTECTED_CONTINUITY_ANCHOR_NOT_REFERENCED" })
    }

    @Test
    fun forbiddenCompletionClaimInFrameAssetOrReviewTextFails() {
        val report = StoryboardPacketDraftValidationService().validate(
            storyboardPlanningBrief = planningBrief(),
            storyboardPacketDraft = storyboardPacketDraft(
                frames = listOf(
                    storyboardFrame(reviewNotes = "storyboard generated and ready for delivery"),
                ),
                assetCandidates = listOf(
                    StoryboardPacketAssetCandidateDraft(
                        candidateId = "asset-door-mark",
                        requirement = "visible mark reference only",
                        approvalStatus = "assets approved",
                    ),
                ),
                reviewNotes = "final video can use this directly",
            ),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.evidence.forbiddenClaimCount >= 3)
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
    }

    @Test
    fun imageVideoOutputPathOrExternalGenerationApiFails() {
        val report = StoryboardPacketDraftValidationService().validate(
            storyboardPlanningBrief = planningBrief(),
            storyboardPacketDraft = storyboardPacketDraft(
                frames = listOf(
                    storyboardFrame(
                        imageOutputPath = "outputs/frame_001.png",
                        videoOutputPath = "outputs/shot_001.mp4",
                        generationApi = "openai api request id should not be here",
                    ),
                ),
            ),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.evidence.forbiddenOutputPathCount >= 2)
        assertTrue(report.evidence.externalGenerationApiHitCount >= 1)
        assertTrue(report.findings.any { it.ruleId == "IMAGE_OR_VIDEO_OUTPUT_PATH_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "EXTERNAL_GENERATION_API_INFO_FORBIDDEN" })
    }

    @Test
    fun shortAssetCandidateApprovalStatusFails() {
        val report = StoryboardPacketDraftValidationService().validate(
            storyboardPlanningBrief = planningBrief(),
            storyboardPacketDraft = storyboardPacketDraft(
                assetCandidates = listOf(
                    StoryboardPacketAssetCandidateDraft(
                        candidateId = "asset-door-mark",
                        requirement = "visible mark reference only",
                        approvalStatus = "approved",
                    ),
                ),
            ),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any {
            it.ruleId == "ASSET_APPROVAL_STATUS_FORBIDDEN" &&
                it.path == "$.storyboardPacketDraft.assetCandidates[0].approvalStatus"
        })
    }

    @Test
    fun shortFrameAssetApprovalStatusFails() {
        val report = StoryboardPacketDraftValidationService().validate(
            storyboardPlanningBrief = planningBrief(),
            storyboardPacketDraft = storyboardPacketDraft(
                frames = listOf(storyboardFrame(assetApprovalStatus = "approved")),
            ),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any {
            it.ruleId == "ASSET_APPROVAL_STATUS_FORBIDDEN" &&
                it.path == "$.storyboardPacketDraft.frames[0].assetApprovalStatus"
        })
    }

    @Test
    fun shortImageOutputPathWithoutExtensionFails() {
        val report = StoryboardPacketDraftValidationService().validate(
            storyboardPlanningBrief = planningBrief(),
            storyboardPacketDraft = storyboardPacketDraft(
                frames = listOf(storyboardFrame(imageOutputPath = "frame-001")),
            ),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "IMAGE_OUTPUT_PATH_FORBIDDEN" })
    }

    @Test
    fun shortVideoOutputPathWithoutExtensionFails() {
        val report = StoryboardPacketDraftValidationService().validate(
            storyboardPlanningBrief = planningBrief(),
            storyboardPacketDraft = storyboardPacketDraft(
                frames = listOf(storyboardFrame(videoOutputPath = "shot-001")),
            ),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "VIDEO_OUTPUT_PATH_FORBIDDEN" })
    }

    @Test
    fun shortGenerationApiWithoutProviderKeywordFails() {
        val report = StoryboardPacketDraftValidationService().validate(
            storyboardPlanningBrief = planningBrief(),
            storyboardPacketDraft = storyboardPacketDraft(
                frames = listOf(storyboardFrame(generationApi = "manual-provider-job")),
            ),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "GENERATION_API_FORBIDDEN" })
    }

    @Test
    fun cliWritesStoryboardPacketDraftValidationReportWithoutDatabase() {
        val tempDir = Files.createTempDirectory("storyboard-packet-draft")
        val briefPath = tempDir.resolve("storyboard_planning_brief.json")
        val draftPath = tempDir.resolve("storyboard_packet_draft.json")
        val out = tempDir.resolve("storyboard_packet_draft_report.json")
        writeJson(briefPath, planningBrief())
        writeJson(draftPath, storyboardPacketDraft(sourceStoryboardPlanningBriefFile = briefPath.toString()))

        val exit = Cli().run(
            listOf(
                "validate-storyboard-packet-draft",
                "--storyboard-planning-brief",
                briefPath.toString(),
                "--storyboard-packet-draft",
                draftPath.toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<StoryboardPacketDraftValidationReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("storyboard_packet_review", report.nextAllowedDownstreamStep)
        assertEquals(briefPath.toString(), report.storyboardPlanningBriefFile)
        assertEquals(draftPath.toString(), report.storyboardPacketDraftFile)
    }

    private fun planningBrief(
        verdict: String = "PASS",
        briefId: String = "storyboard-brief-001",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        scriptPacketDraftFile: String = "script_packet_draft.json",
        scriptPacketDraftContentSha256: String = "a".repeat(64),
        nextAllowedDownstreamStep: String = "storyboard_packet_draft",
    ): StoryboardPlanningBrief =
        StoryboardPlanningBrief(
            verdict = verdict,
            briefId = briefId,
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = scriptPacketDraftContentSha256,
            sourceReports = StoryboardPlanningBriefSourceReports(
                acceptedScriptPacketVerdict = "PASS",
                storyboardPlanningPreflightVerdict = "PASS",
                structureBridgeVerdict = "PASS",
                sourceBindingVerdict = "PASS",
                structureVerdict = "PASS",
            ),
            protectedContinuityAnchorsSummary = AcceptedScriptPacketContinuityAnchorsSummary(
                protectedContinuityAnchorCount = 2,
                protectedContinuityAnchors = protectedAnchors,
            ),
            mustShowBoundary = protectedAnchors,
            mustNotShowBoundary = listOf("Do not reveal author-only culprit evidence."),
            remainingResidualRisks = listOf("Storyboard packet review has not yet run."),
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun storyboardPacketDraft(
        packetId: String = "storyboard-packet-draft-001",
        storyboardPlanningBriefId: String = "storyboard-brief-001",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        scriptPacketDraftFile: String = "script_packet_draft.json",
        scriptPacketDraftContentSha256: String = "a".repeat(64),
        sourceStoryboardPlanningBriefFile: String = "storyboard_planning_brief.json",
        protectedContinuityAnchors: List<String> = protectedAnchors,
        frames: List<StoryboardPacketFrameDraft> = listOf(storyboardFrame()),
        assetCandidates: List<StoryboardPacketAssetCandidateDraft> = listOf(
            StoryboardPacketAssetCandidateDraft(
                candidateId = "asset-door-mark",
                requirement = "candidate only: visible mark reference for guard checkpoint frame",
                sourceFrameIds = listOf("frame-001"),
            ),
        ),
        reviewNotes: String = "Text-only storyboard packet draft for review.",
    ): StoryboardPacketDraft =
        StoryboardPacketDraft(
            packetId = packetId,
            storyboardPlanningBriefId = storyboardPlanningBriefId,
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = scriptPacketDraftContentSha256,
            sourceStoryboardPlanningBriefFile = sourceStoryboardPlanningBriefFile,
            protectedContinuityAnchors = protectedContinuityAnchors,
            frames = frames,
            assetCandidates = assetCandidates,
            reviewNotes = reviewNotes,
        )

    private fun storyboardFrame(
        frameId: String = "frame-001",
        sceneReference: String = "checkpoint-gate",
        beatReference: String = "beat-001",
        visualIntent: String = "Show Mira arriving after the paid toll while the guard checks the visible mark.",
        cameraIntent: String = "Medium shot clarifying gate distance and visible mark.",
        mustShow: List<String> = listOf("米拉已經付過門費才抵達關卡", "visible mark before guard answer"),
        doNotShow: List<String> = listOf("Do not reveal author-only culprit evidence."),
        continuityAnchors: List<String> = protectedAnchors,
        reviewNotes: String = "Text-only frame; no asset approval.",
        imageOutputPath: String = "",
        videoOutputPath: String = "",
        generationApi: String = "",
        assetApprovalStatus: String = "",
    ): StoryboardPacketFrameDraft =
        StoryboardPacketFrameDraft(
            frameId = frameId,
            sceneReference = sceneReference,
            beatReference = beatReference,
            visualIntent = visualIntent,
            cameraIntent = cameraIntent,
            mustShow = mustShow,
            doNotShow = doNotShow,
            continuityAnchors = continuityAnchors,
            reviewNotes = reviewNotes,
            imageOutputPath = imageOutputPath,
            videoOutputPath = videoOutputPath,
            generationApi = generationApi,
            assetApprovalStatus = assetApprovalStatus,
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }

    private companion object {
        val protectedAnchors = listOf(
            "米拉已經付過門費才抵達關卡",
            "The guard requires a visible mark before answering questions.",
        )
    }
}
