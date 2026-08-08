package com.localnovel.engine

import com.localnovel.engine.model.AssetCandidateBrief
import com.localnovel.engine.model.AssetCandidateBriefChecklistItem
import com.localnovel.engine.model.AssetCandidateBriefRequirement
import com.localnovel.engine.model.AssetCandidateDraft
import com.localnovel.engine.service.AssetCandidateDraftService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssetCandidateDraftTest {
    @Test
    fun passBriefCreatesCandidateOnlyDraft() {
        val draft = AssetCandidateDraftService().create(
            assetCandidateBrief = brief(),
            draftId = "asset-candidate-draft-001",
            assetCandidateBriefFile = "asset_candidate_brief.json",
        )

        assertEquals("PASS", draft.verdict)
        assertEquals("asset-candidate-draft-001", draft.draftId)
        assertEquals("asset-candidate-brief-001", draft.assetCandidateBriefId)
        assertEquals("asset_candidate_review_preflight", draft.nextAllowedDownstreamStep)
        assertEquals(1, draft.candidateItems.size)
        assertEquals("prop", draft.candidateItems.single().assetType)
        assertEquals("draft_candidate", draft.candidateItems.single().candidateStatus)
    }

    @Test
    fun nonPassBriefFailsDraft() {
        val draft = AssetCandidateDraftService().create(
            assetCandidateBrief = brief(verdict = "FAIL"),
            assetCandidateBriefFile = "asset_candidate_brief.json",
        )

        assertEquals("FAIL", draft.verdict)
        assertEquals("", draft.nextAllowedDownstreamStep)
        assertTrue(draft.findings.any { it.ruleId == "ASSET_CANDIDATE_BRIEF_NOT_PASS" })
    }

    @Test
    fun wrongBriefNextStepFailsDraft() {
        val draft = AssetCandidateDraftService().create(
            assetCandidateBrief = brief(nextAllowedDownstreamStep = "asset_candidate_review"),
            assetCandidateBriefFile = "asset_candidate_brief.json",
        )

        assertEquals("FAIL", draft.verdict)
        assertEquals("", draft.nextAllowedDownstreamStep)
        assertTrue(draft.findings.any { it.ruleId == "ASSET_CANDIDATE_BRIEF_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun missingSourceBindingFieldFailsDraft() {
        val draft = AssetCandidateDraftService().create(
            assetCandidateBrief = brief(humanReviewDecisionId = ""),
            assetCandidateBriefFile = "asset_candidate_brief.json",
        )

        assertEquals("FAIL", draft.verdict)
        assertEquals("", draft.nextAllowedDownstreamStep)
        assertTrue(draft.findings.any { it.ruleId == "HUMAN_REVIEW_DECISION_ID_MISSING" })
    }

    @Test
    fun missingCandidateIdRequirementOrSourceRequirementFailsDraft() {
        val missingId = AssetCandidateDraftService().create(
            assetCandidateBrief = brief(
                assetCandidateRequirements = listOf(
                    requirement(candidateId = "", requirement = "candidate only: prop reference needed"),
                ),
            ),
            assetCandidateBriefFile = "asset_candidate_brief.json",
        )
        val missingRequirement = AssetCandidateDraftService().create(
            assetCandidateBrief = brief(
                assetCandidateRequirements = listOf(
                    requirement(candidateId = "asset-door-mark-candidate", requirement = ""),
                ),
            ),
            assetCandidateBriefFile = "asset_candidate_brief.json",
        )

        assertEquals("FAIL", missingId.verdict)
        assertEquals("", missingId.nextAllowedDownstreamStep)
        assertTrue(missingId.findings.any { it.ruleId == "ASSET_CANDIDATE_ID_MISSING" })
        assertTrue(missingId.findings.any { it.ruleId == "SOURCE_REQUIREMENT_ID_MISSING" })
        assertEquals("FAIL", missingRequirement.verdict)
        assertTrue(missingRequirement.findings.any { it.ruleId == "ASSET_CANDIDATE_REQUIREMENT_MISSING" })
    }

    @Test
    fun missingAssetTypeFailsDraft() {
        val draft = AssetCandidateDraftService().create(
            assetCandidateBrief = brief(
                assetCandidateRequirements = listOf(
                    requirement(
                        candidateId = "asset-unclear-candidate",
                        requirement = "candidate only: reference needed for consistency",
                    ),
                ),
            ),
            assetCandidateBriefFile = "asset_candidate_brief.json",
        )

        assertEquals("FAIL", draft.verdict)
        assertEquals("", draft.nextAllowedDownstreamStep)
        assertTrue(draft.findings.any { it.ruleId == "ASSET_CANDIDATE_TYPE_MISSING" })
    }

    @Test
    fun forbiddenCandidateStatusFailsDraft() {
        val draft = AssetCandidateDraftService().create(
            assetCandidateBrief = brief(
                assetCandidateRequirements = listOf(
                    requirement(candidateStatus = "review_ready"),
                ),
            ),
            assetCandidateBriefFile = "asset_candidate_brief.json",
        )

        assertEquals("FAIL", draft.verdict)
        assertEquals("", draft.nextAllowedDownstreamStep)
        assertTrue(draft.findings.any { it.ruleId == "ASSET_CANDIDATE_STATUS_FORBIDDEN" })
    }

    @Test
    fun forbiddenApprovalCanonRuntimeOrGeneratedMediaClaimFailsDraft() {
        val draft = AssetCandidateDraftService().create(
            assetCandidateBrief = brief(
                assetCandidateRequirements = listOf(
                    requirement(requirement = "asset approved; canon applied; runtime scene created; video generated"),
                ),
            ),
            assetCandidateBriefFile = "asset_candidate_brief.json",
        )

        assertEquals("FAIL", draft.verdict)
        assertEquals("", draft.nextAllowedDownstreamStep)
        assertTrue(draft.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
    }

    @Test
    fun cliWritesAssetCandidateDraftWithoutDatabase() {
        val tempDir = Files.createTempDirectory("asset-candidate-draft")
        val briefPath = tempDir.resolve("asset_candidate_brief.json")
        val out = tempDir.resolve("asset_candidate_draft.json")
        writeJson(briefPath, brief())

        val exit = Cli().run(
            listOf(
                "create-asset-candidate-draft",
                "--asset-candidate-brief",
                briefPath.toString(),
                "--draft",
                "asset-candidate-draft-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val draft = readJson<AssetCandidateDraft>(out)
        assertEquals("PASS", draft.verdict)
        assertEquals("asset-candidate-draft-cli-001", draft.draftId)
        assertEquals("asset_candidate_review_preflight", draft.nextAllowedDownstreamStep)
    }

    private fun brief(
        verdict: String = "PASS",
        briefId: String = "asset-candidate-brief-001",
        assetCandidatePreflightFile: String = "asset_candidate_preflight.json",
        assetCandidatePreflightId: String = "asset-candidate-preflight-001",
        humanReviewDecisionIntakeFile: String = "human_review_decision_intake.json",
        humanReviewDecisionId: String = "human-review-decision-001",
        storyboardPacketReviewFile: String = "storyboard_packet_review.json",
        storyboardPacketReviewId: String = "storyboard-review-001",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        scriptPacketDraftFile: String = "script_packet_draft.json",
        scriptPacketDraftContentSha256: String = "a".repeat(64),
        assetCandidateRequirements: List<AssetCandidateBriefRequirement> = listOf(requirement()),
        preparationChecklist: List<AssetCandidateBriefChecklistItem> = checklist(),
        nextAllowedDownstreamStep: String = "asset_candidate_draft",
    ): AssetCandidateBrief =
        AssetCandidateBrief(
            verdict = verdict,
            briefId = briefId,
            assetCandidatePreflightFile = assetCandidatePreflightFile,
            assetCandidatePreflightId = assetCandidatePreflightId,
            humanReviewDecisionIntakeFile = humanReviewDecisionIntakeFile,
            humanReviewDecisionId = humanReviewDecisionId,
            storyboardPacketReviewFile = storyboardPacketReviewFile,
            storyboardPacketReviewId = storyboardPacketReviewId,
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = scriptPacketDraftContentSha256,
            assetCandidateRequirements = assetCandidateRequirements,
            preparationChecklist = preparationChecklist,
            downstreamBoundary = "asset_candidate_brief_only",
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun requirement(
        candidateId: String = "asset-door-mark-candidate",
        requirement: String = "candidate only: visible door mark prop reference needed for frame consistency",
        sourceFrameIds: List<String> = listOf("frame-001"),
        candidateStatus: String = "candidate_only",
    ): AssetCandidateBriefRequirement =
        AssetCandidateBriefRequirement(
            candidateId = candidateId,
            requirement = requirement,
            sourceFrameIds = sourceFrameIds,
            candidateStatus = candidateStatus,
        )

    private fun checklist(): List<AssetCandidateBriefChecklistItem> =
        listOf(
            AssetCandidateBriefChecklistItem("candidate-only-status", "candidate_only_boundary", "Every asset remains candidate-only and unapproved."),
            AssetCandidateBriefChecklistItem("manual-review-required", "manual_review", "Human review is still required before any asset approval or lock."),
            AssetCandidateBriefChecklistItem("no-media-generation", "downstream_boundary", "Do not use this brief for image generation, video generation, production lock, or canon."),
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
