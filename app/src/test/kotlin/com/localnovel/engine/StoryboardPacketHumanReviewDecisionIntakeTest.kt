package com.localnovel.engine

import com.localnovel.engine.model.StoryboardPacketHumanReviewDecision
import com.localnovel.engine.model.StoryboardPacketHumanReviewDecisionBlockingIssue
import com.localnovel.engine.model.StoryboardPacketHumanReviewDecisionChecklistItem
import com.localnovel.engine.model.StoryboardPacketHumanReviewDecisionIntakeReport
import com.localnovel.engine.model.StoryboardPacketHumanReviewPreflightReport
import com.localnovel.engine.service.StoryboardPacketHumanReviewDecisionIntakeService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoryboardPacketHumanReviewDecisionIntakeTest {
    @Test
    fun humanAllowDecisionPassesAndAllowsAssetCandidatePreflight() {
        val report = StoryboardPacketHumanReviewDecisionIntakeService().validate(
            humanReviewPreflight = preflight(),
            humanReviewDecision = decision(),
            humanReviewPreflightFile = "human_review_preflight.json",
            humanReviewDecisionFile = "human_review_decision.json",
        )

        assertEquals("PASS", report.verdict)
        assertEquals("human-review-decision-001", report.decisionId)
        assertEquals("ALLOW_ASSET_CANDIDATE_PREFLIGHT", report.decisionStatus)
        assertTrue(report.decisionAllowedDownstream)
        assertEquals("asset_candidate_preflight", report.nextAllowedDownstreamStep)
        assertEquals(5, report.evidence.checklistPassedCount)
    }

    @Test
    fun preflightFailOrWrongNextStepFailsDecisionIntake() {
        val failed = StoryboardPacketHumanReviewDecisionIntakeService().validate(
            humanReviewPreflight = preflight(verdict = "FAIL", nextAllowedDownstreamStep = ""),
            humanReviewDecision = decision(),
            humanReviewPreflightFile = "human_review_preflight.json",
            humanReviewDecisionFile = "human_review_decision.json",
        )
        val wrongStep = StoryboardPacketHumanReviewDecisionIntakeService().validate(
            humanReviewPreflight = preflight(nextAllowedDownstreamStep = "asset_candidate_preflight"),
            humanReviewDecision = decision(),
            humanReviewPreflightFile = "human_review_preflight.json",
            humanReviewDecisionFile = "human_review_decision.json",
        )

        assertEquals("FAIL", failed.verdict)
        assertTrue(failed.findings.any { it.ruleId == "HUMAN_REVIEW_PREFLIGHT_NOT_PASS" })
        assertEquals("FAIL", wrongStep.verdict)
        assertTrue(wrongStep.findings.any { it.ruleId == "HUMAN_REVIEW_PREFLIGHT_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun automaticModelOrBlankDecisionSourceFails() {
        val blank = StoryboardPacketHumanReviewDecisionIntakeService().validate(
            humanReviewPreflight = preflight(),
            humanReviewDecision = decision(decisionSource = ""),
            humanReviewPreflightFile = "human_review_preflight.json",
            humanReviewDecisionFile = "human_review_decision.json",
        )
        val model = StoryboardPacketHumanReviewDecisionIntakeService().validate(
            humanReviewPreflight = preflight(),
            humanReviewDecision = decision(decisionSource = "model"),
            humanReviewPreflightFile = "human_review_preflight.json",
            humanReviewDecisionFile = "human_review_decision.json",
        )

        assertEquals("FAIL", blank.verdict)
        assertTrue(blank.findings.any { it.ruleId == "DECISION_SOURCE_MISSING" })
        assertEquals("FAIL", model.verdict)
        assertTrue(model.findings.any { it.ruleId == "DECISION_SOURCE_NOT_HUMAN" })
    }

    @Test
    fun sourceBindingMissingOrMismatchFails() {
        val missing = StoryboardPacketHumanReviewDecisionIntakeService().validate(
            humanReviewPreflight = preflight(),
            humanReviewDecision = decision(preflightId = ""),
            humanReviewPreflightFile = "human_review_preflight.json",
            humanReviewDecisionFile = "human_review_decision.json",
        )
        val mismatch = StoryboardPacketHumanReviewDecisionIntakeService().validate(
            humanReviewPreflight = preflight(),
            humanReviewDecision = decision(acceptedScriptPacketId = "accepted-script-packet-other"),
            humanReviewPreflightFile = "human_review_preflight.json",
            humanReviewDecisionFile = "human_review_decision.json",
        )

        assertEquals("FAIL", missing.verdict)
        assertTrue(missing.findings.any { it.ruleId == "DECISION_PREFLIGHT_ID_MISSING" })
        assertEquals("FAIL", mismatch.verdict)
        assertTrue(mismatch.findings.any { it.ruleId == "DECISION_ACCEPTED_SCRIPT_PACKET_ID_MISMATCH" })
    }

    @Test
    fun unknownDecisionStatusFails() {
        val report = StoryboardPacketHumanReviewDecisionIntakeService().validate(
            humanReviewPreflight = preflight(),
            humanReviewDecision = decision(decisionStatus = "APPROVED"),
            humanReviewPreflightFile = "human_review_preflight.json",
            humanReviewDecisionFile = "human_review_decision.json",
        )

        assertEquals("FAIL", report.verdict)
        assertFalse(report.decisionAllowedDownstream)
        assertTrue(report.findings.any { it.ruleId == "DECISION_STATUS_UNKNOWN" })
    }

    @Test
    fun allowDownstreamWithFailedChecklistOrBlockingIssueFails() {
        val failedChecklist = StoryboardPacketHumanReviewDecisionIntakeService().validate(
            humanReviewPreflight = preflight(),
            humanReviewDecision = decision(
                checklistItems = checklist().mapIndexed { index, item ->
                    if (index == 0) item.copy(result = "needs_changes") else item
                },
            ),
            humanReviewPreflightFile = "human_review_preflight.json",
            humanReviewDecisionFile = "human_review_decision.json",
        )
        val blockingIssue = StoryboardPacketHumanReviewDecisionIntakeService().validate(
            humanReviewPreflight = preflight(),
            humanReviewDecision = decision(
                blockingIssues = listOf(
                    StoryboardPacketHumanReviewDecisionBlockingIssue(
                        issueId = "issue-continuity",
                        severity = "BLOCKING",
                        status = "OPEN",
                        description = "Continuity anchor is not accepted yet.",
                    ),
                ),
            ),
            humanReviewPreflightFile = "human_review_preflight.json",
            humanReviewDecisionFile = "human_review_decision.json",
        )

        assertEquals("FAIL", failedChecklist.verdict)
        assertFalse(failedChecklist.decisionAllowedDownstream)
        assertTrue(failedChecklist.findings.any { it.ruleId == "HUMAN_CHECKLIST_ITEM_NOT_ACCEPTED" })
        assertEquals("FAIL", blockingIssue.verdict)
        assertFalse(blockingIssue.decisionAllowedDownstream)
        assertTrue(blockingIssue.findings.any { it.ruleId == "UNRESOLVED_BLOCKING_ISSUE_PRESENT" })
    }

    @Test
    fun allowDownstreamWithOnlyDownstreamBoundaryChecklistFailsCoverage() {
        val report = StoryboardPacketHumanReviewDecisionIntakeService().validate(
            humanReviewPreflight = preflight(),
            humanReviewDecision = decision(
                checklistItems = listOf(
                    StoryboardPacketHumanReviewDecisionChecklistItem(
                        itemId = "check-downstream",
                        category = "downstream_boundary",
                        result = "PASS",
                    ),
                ),
            ),
            humanReviewPreflightFile = "human_review_preflight.json",
            humanReviewDecisionFile = "human_review_decision.json",
        )

        assertEquals("FAIL", report.verdict)
        assertFalse(report.decisionAllowedDownstream)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "HUMAN_CHECKLIST_REQUIRED_CATEGORY_MISSING" })
    }

    @Test
    fun requestChangesAndRejectAreValidButDoNotAllowDownstream() {
        val requestChanges = StoryboardPacketHumanReviewDecisionIntakeService().validate(
            humanReviewPreflight = preflight(),
            humanReviewDecision = decision(decisionStatus = "REQUEST_CHANGES"),
            humanReviewPreflightFile = "human_review_preflight.json",
            humanReviewDecisionFile = "human_review_decision.json",
        )
        val reject = StoryboardPacketHumanReviewDecisionIntakeService().validate(
            humanReviewPreflight = preflight(),
            humanReviewDecision = decision(decisionStatus = "REJECT"),
            humanReviewPreflightFile = "human_review_preflight.json",
            humanReviewDecisionFile = "human_review_decision.json",
        )

        assertEquals("PASS", requestChanges.verdict)
        assertFalse(requestChanges.decisionAllowedDownstream)
        assertEquals("", requestChanges.nextAllowedDownstreamStep)
        assertEquals("PASS", reject.verdict)
        assertFalse(reject.decisionAllowedDownstream)
        assertEquals("", reject.nextAllowedDownstreamStep)
    }

    @Test
    fun forbiddenApprovalCanonOrGeneratedMediaClaimFails() {
        val report = StoryboardPacketHumanReviewDecisionIntakeService().validate(
            humanReviewPreflight = preflight(),
            humanReviewDecision = decision(notes = "asset approved; canon applied; video generated"),
            humanReviewPreflightFile = "human_review_preflight.json",
            humanReviewDecisionFile = "human_review_decision.json",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.evidence.forbiddenClaimCount >= 3)
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
    }

    @Test
    fun cliWritesHumanReviewDecisionIntakeWithoutDatabase() {
        val tempDir = Files.createTempDirectory("storyboard-human-review-decision")
        val preflightPath = tempDir.resolve("human_review_preflight.json")
        val decisionPath = tempDir.resolve("human_review_decision.json")
        val out = tempDir.resolve("human_review_decision_intake.json")
        writeJson(preflightPath, preflight())
        writeJson(decisionPath, decision())

        val exit = Cli().run(
            listOf(
                "validate-storyboard-packet-human-review-decision",
                "--human-review-preflight",
                preflightPath.toString(),
                "--human-review-decision",
                decisionPath.toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<StoryboardPacketHumanReviewDecisionIntakeReport>(out)
        assertEquals("PASS", report.verdict)
        assertTrue(report.decisionAllowedDownstream)
        assertEquals("asset_candidate_preflight", report.nextAllowedDownstreamStep)
    }

    private fun preflight(
        verdict: String = "PASS",
        preflightId: String = "human-review-preflight-001",
        storyboardPacketReviewFile: String = "storyboard_packet_review.json",
        storyboardPacketReviewId: String = "storyboard-review-001",
        storyboardPacketDraftFile: String = "storyboard_packet_draft.json",
        storyboardPacketDraftReportFile: String = "storyboard_packet_draft_report.json",
        storyboardPacketDraftId: String = "storyboard-packet-draft-001",
        storyboardPlanningBriefId: String = "storyboard-brief-001",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        scriptPacketDraftFile: String = "script_packet_draft.json",
        scriptPacketDraftContentSha256: String = "a".repeat(64),
        reviewStatus: String = "REVIEW_READY",
        nextAllowedDownstreamStep: String = "storyboard_packet_human_review_ready",
    ): StoryboardPacketHumanReviewPreflightReport =
        StoryboardPacketHumanReviewPreflightReport(
            verdict = verdict,
            preflightId = preflightId,
            storyboardPacketReviewFile = storyboardPacketReviewFile,
            storyboardPacketReviewId = storyboardPacketReviewId,
            storyboardPacketDraftFile = storyboardPacketDraftFile,
            storyboardPacketDraftReportFile = storyboardPacketDraftReportFile,
            storyboardPacketDraftId = storyboardPacketDraftId,
            storyboardPlanningBriefId = storyboardPlanningBriefId,
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = scriptPacketDraftContentSha256,
            reviewStatus = reviewStatus,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun decision(
        decisionId: String = "human-review-decision-001",
        preflightId: String = "human-review-preflight-001",
        storyboardPacketReviewFile: String = "storyboard_packet_review.json",
        storyboardPacketReviewId: String = "storyboard-review-001",
        storyboardPacketDraftFile: String = "storyboard_packet_draft.json",
        storyboardPacketDraftReportFile: String = "storyboard_packet_draft_report.json",
        storyboardPacketDraftId: String = "storyboard-packet-draft-001",
        storyboardPlanningBriefId: String = "storyboard-brief-001",
        acceptedScriptPacketId: String = "accepted-script-packet-001",
        scriptPacketDraftFile: String = "script_packet_draft.json",
        scriptPacketDraftContentSha256: String = "a".repeat(64),
        decisionSource: String = "01_control",
        reviewer: String = "01 主控窗口",
        reviewerRole: String = "human_controller",
        decisionAt: String = "2026-06-25T00:00:00+08:00",
        decisionStatus: String = "ALLOW_ASSET_CANDIDATE_PREFLIGHT",
        checklistItems: List<StoryboardPacketHumanReviewDecisionChecklistItem> = checklist(),
        blockingIssues: List<StoryboardPacketHumanReviewDecisionBlockingIssue> = emptyList(),
        notes: String = "Allow only asset candidate preflight; no asset approval or media generation.",
    ): StoryboardPacketHumanReviewDecision =
        StoryboardPacketHumanReviewDecision(
            decisionId = decisionId,
            preflightId = preflightId,
            storyboardPacketReviewFile = storyboardPacketReviewFile,
            storyboardPacketReviewId = storyboardPacketReviewId,
            storyboardPacketDraftFile = storyboardPacketDraftFile,
            storyboardPacketDraftReportFile = storyboardPacketDraftReportFile,
            storyboardPacketDraftId = storyboardPacketDraftId,
            storyboardPlanningBriefId = storyboardPlanningBriefId,
            acceptedScriptPacketId = acceptedScriptPacketId,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = scriptPacketDraftContentSha256,
            decisionSource = decisionSource,
            reviewer = reviewer,
            reviewerRole = reviewerRole,
            decisionAt = decisionAt,
            decisionStatus = decisionStatus,
            checklistItems = checklistItems,
            blockingIssues = blockingIssues,
            notes = notes,
        )

    private fun checklist(): List<StoryboardPacketHumanReviewDecisionChecklistItem> =
        listOf(
            StoryboardPacketHumanReviewDecisionChecklistItem("check-scene-lock", "scene_lock", "OK"),
            StoryboardPacketHumanReviewDecisionChecklistItem("check-continuity", "continuity", "ACCEPTED"),
            StoryboardPacketHumanReviewDecisionChecklistItem("check-secrecy", "knowledge_boundary", "PASS"),
            StoryboardPacketHumanReviewDecisionChecklistItem("check-assets", "asset_candidates", "PASSED"),
            StoryboardPacketHumanReviewDecisionChecklistItem("check-downstream", "downstream_boundary", "YES"),
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
