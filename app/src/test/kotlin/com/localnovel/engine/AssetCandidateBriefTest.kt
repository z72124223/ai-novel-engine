package com.localnovel.engine

import com.localnovel.engine.model.AssetCandidateBrief
import com.localnovel.engine.model.AssetCandidatePreflightReport
import com.localnovel.engine.model.StoryboardPacketReviewAssetRequirement
import com.localnovel.engine.service.AssetCandidateBriefService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssetCandidateBriefTest {
    @Test
    fun passPreflightCreatesCandidateOnlyBrief() {
        val brief = AssetCandidateBriefService().create(
            assetCandidatePreflight = preflight(),
            briefId = "asset-candidate-brief-001",
            assetCandidatePreflightFile = "asset_candidate_preflight.json",
        )

        assertEquals("PASS", brief.verdict)
        assertEquals("asset-candidate-brief-001", brief.briefId)
        assertEquals("asset-candidate-preflight-001", brief.assetCandidatePreflightId)
        assertEquals("asset_candidate_draft", brief.nextAllowedDownstreamStep)
        assertEquals(1, brief.assetCandidateRequirements.size)
        assertEquals("candidate_only", brief.assetCandidateRequirements.single().candidateStatus)
        assertTrue(brief.preparationChecklist.isNotEmpty())
    }

    @Test
    fun nonPassPreflightFailsBrief() {
        val brief = AssetCandidateBriefService().create(
            assetCandidatePreflight = preflight(verdict = "FAIL"),
            assetCandidatePreflightFile = "asset_candidate_preflight.json",
        )

        assertEquals("FAIL", brief.verdict)
        assertEquals("", brief.nextAllowedDownstreamStep)
        assertTrue(brief.findings.any { it.ruleId == "ASSET_CANDIDATE_PREFLIGHT_NOT_PASS" })
    }

    @Test
    fun wrongPreflightNextStepFailsBrief() {
        val brief = AssetCandidateBriefService().create(
            assetCandidatePreflight = preflight(nextAllowedDownstreamStep = "asset_candidate_draft"),
            assetCandidatePreflightFile = "asset_candidate_preflight.json",
        )

        assertEquals("FAIL", brief.verdict)
        assertEquals("", brief.nextAllowedDownstreamStep)
        assertTrue(brief.findings.any { it.ruleId == "ASSET_CANDIDATE_PREFLIGHT_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun missingSourceBindingFieldFailsBrief() {
        val brief = AssetCandidateBriefService().create(
            assetCandidatePreflight = preflight(decisionId = ""),
            assetCandidatePreflightFile = "asset_candidate_preflight.json",
        )

        assertEquals("FAIL", brief.verdict)
        assertEquals("", brief.nextAllowedDownstreamStep)
        assertTrue(brief.findings.any { it.ruleId == "HUMAN_REVIEW_DECISION_ID_MISSING" })
    }

    @Test
    fun missingAssetCandidateRequirementsFailsBrief() {
        val brief = AssetCandidateBriefService().create(
            assetCandidatePreflight = preflight(assetCandidateRequirements = emptyList()),
            assetCandidatePreflightFile = "asset_candidate_preflight.json",
        )

        assertEquals("FAIL", brief.verdict)
        assertEquals("", brief.nextAllowedDownstreamStep)
        assertTrue(brief.findings.any { it.ruleId == "ASSET_CANDIDATE_REQUIREMENTS_MISSING" })
    }

    @Test
    fun forbiddenApprovalLockFinalCanonOrGeneratedMediaClaimFailsBrief() {
        val brief = AssetCandidateBriefService().create(
            assetCandidatePreflight = preflight(
                assetCandidateRequirements = listOf(
                    StoryboardPacketReviewAssetRequirement(
                        candidateId = "asset-final-door",
                        requirement = "asset approved; production locked; canon applied; image generated",
                        sourceFrameIds = listOf("frame-001"),
                    ),
                ),
            ),
            assetCandidatePreflightFile = "asset_candidate_preflight.json",
        )

        assertEquals("FAIL", brief.verdict)
        assertEquals("", brief.nextAllowedDownstreamStep)
        assertTrue(brief.findings.any { it.ruleId == "ASSET_CANDIDATE_STATUS_FORBIDDEN" })
        assertTrue(brief.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
    }

    @Test
    fun cliWritesAssetCandidateBriefWithoutDatabase() {
        val tempDir = Files.createTempDirectory("asset-candidate-brief")
        val preflightPath = tempDir.resolve("asset_candidate_preflight.json")
        val out = tempDir.resolve("asset_candidate_brief.json")
        writeJson(preflightPath, preflight())

        val exit = Cli().run(
            listOf(
                "create-asset-candidate-brief",
                "--asset-candidate-preflight",
                preflightPath.toString(),
                "--brief",
                "asset-candidate-brief-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val brief = readJson<AssetCandidateBrief>(out)
        assertEquals("PASS", brief.verdict)
        assertEquals("asset-candidate-brief-cli-001", brief.briefId)
        assertEquals("asset_candidate_draft", brief.nextAllowedDownstreamStep)
    }

    private fun preflight(
        verdict: String = "PASS",
        preflightId: String = "asset-candidate-preflight-001",
        humanReviewDecisionIntakeFile: String = "human_review_decision_intake.json",
        storyboardPacketReviewFile: String = "storyboard_packet_review.json",
        decisionId: String = "human-review-decision-001",
        humanReviewPreflightId: String = "human-review-preflight-001",
        storyboardPacketReviewId: String = "storyboard-review-001",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        scriptPacketDraftFile: String = "script_packet_draft.json",
        scriptPacketDraftContentSha256: String = "a".repeat(64),
        assetCandidateRequirements: List<StoryboardPacketReviewAssetRequirement> = assetRequirements(),
        nextAllowedDownstreamStep: String = "asset_candidate_brief",
    ): AssetCandidatePreflightReport =
        AssetCandidatePreflightReport(
            verdict = verdict,
            preflightId = preflightId,
            humanReviewDecisionIntakeFile = humanReviewDecisionIntakeFile,
            storyboardPacketReviewFile = storyboardPacketReviewFile,
            decisionId = decisionId,
            humanReviewPreflightId = humanReviewPreflightId,
            storyboardPacketReviewId = storyboardPacketReviewId,
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = scriptPacketDraftContentSha256,
            assetCandidateRequirements = assetCandidateRequirements,
            downstreamBoundary = "asset_candidate_preparation_only",
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun assetRequirements(): List<StoryboardPacketReviewAssetRequirement> =
        listOf(
            StoryboardPacketReviewAssetRequirement(
                candidateId = "asset-door-mark-candidate",
                requirement = "candidate only: visible door mark reference needed for frame consistency",
                sourceFrameIds = listOf("frame-001"),
            ),
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
