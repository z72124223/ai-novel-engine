package com.localnovel.engine.service

import com.localnovel.engine.model.ReviewableStoryboardFrameSummary
import com.localnovel.engine.model.ReviewableStoryboardHumanReviewReadinessSummary
import com.localnovel.engine.model.ReviewableStoryboardMovementEvidenceSummary
import com.localnovel.engine.model.ReviewableStoryboardPacket
import com.localnovel.engine.model.ReviewableStoryboardPacketEvidence
import com.localnovel.engine.model.ReviewableStoryboardPacketFinding
import com.localnovel.engine.model.ReviewableStoryboardSourceVerdicts
import com.localnovel.engine.model.StoryboardPacketDraft
import com.localnovel.engine.model.StoryboardPacketHumanReviewPreflightReport
import com.localnovel.engine.model.StoryboardPacketReviewPacket
import com.localnovel.engine.model.StoryboardStoryMovementAuditReport

class ReviewableStoryboardPacketService {
    private val reviewNextStep = "storyboard_packet_human_review"
    private val preflightNextStep = "storyboard_packet_human_review_ready"
    private val auditNextStep = "story_movement_audited"
    private val packetNextStep = "reviewable_storyboard_packet_ready"
    private val packetStatus = "reviewable_storyboard_packet_candidate"
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
    private val forbiddenClaimPatterns = listOf(
        Regex("""(?i)\basset\s+approval\b|\basset\s+approved\b"""),
        Regex("""(?i)\bapproved[-_\s]?reference\b"""),
        Regex("""(?i)\bregistered[-_\s]?reference\b"""),
        Regex("""(?i)\bcanon\b"""),
        Regex("""(?i)\bproduction[-_\s]?lock(?:ed)?\b"""),
        Regex("""(?i)\bimage\s+generated\b|\bgenerated\s+image\b"""),
        Regex("""(?i)\bvideo\s+generated\b|\bgenerated\s+video\b"""),
        Regex("""(?i)\bprovider[-_\s]?job\b"""),
        Regex("""(?i)\bruntime\s+scene\s+created\b"""),
        Regex("""(?i)\b(final|completed)\s+(storyboard|asset|image|video|media)\b"""),
        Regex("\u6838\u51c6|\u6279\u51c6|\u6b63\u5178|\u9396\u5b9a|\u6b63\u5f0f\u7d20\u6750"),
    )

    fun create(
        storyboardPacketDraft: StoryboardPacketDraft,
        storyboardPacketReview: StoryboardPacketReviewPacket,
        storyboardHumanReviewPreflight: StoryboardPacketHumanReviewPreflightReport,
        storyboardStoryMovementAudit: StoryboardStoryMovementAuditReport,
        storyboardPacketDraftFile: String = "",
        storyboardPacketReviewFile: String = "",
        storyboardHumanReviewPreflightFile: String = "",
        storyboardStoryMovementAuditFile: String = "",
        packetId: String = "",
    ): ReviewableStoryboardPacket {
        val findings = mutableListOf<ReviewableStoryboardPacketFinding>()
        validateUpstreamStates(storyboardPacketReview, storyboardHumanReviewPreflight, storyboardStoryMovementAudit, findings)
        val sourceBindingFieldCount = validateSourceBinding(
            draft = storyboardPacketDraft,
            review = storyboardPacketReview,
            preflight = storyboardHumanReviewPreflight,
            audit = storyboardStoryMovementAudit,
            draftFile = storyboardPacketDraftFile,
            reviewFile = storyboardPacketReviewFile,
            preflightFile = storyboardHumanReviewPreflightFile,
            auditFile = storyboardStoryMovementAuditFile,
            findings = findings,
        )
        validateFrameSet(storyboardPacketDraft, storyboardPacketReview, findings)
        validateMovementEvidence(storyboardPacketDraft, storyboardPacketReview, storyboardStoryMovementAudit, findings)
        validateContinuitySummary(storyboardPacketReview, storyboardStoryMovementAudit, findings)
        val forbiddenClaimCount = validateForbiddenDraftOutputFields(storyboardPacketDraft, findings) +
            scanForbiddenClaims(storyboardPacketDraft, storyboardPacketReview, storyboardHumanReviewPreflight, storyboardStoryMovementAudit, findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") {
            findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId }
        } else {
            ""
        }
        val resolvedPacketId = packetId.ifBlank {
            storyboardStoryMovementAudit.auditId.takeIf { it.isNotBlank() }?.let { "$it-reviewable-storyboard-packet" }.orEmpty()
        }

        return ReviewableStoryboardPacket(
            verdict = verdict,
            packetId = resolvedPacketId,
            packetStatus = if (verdict == "PASS") packetStatus else "FAIL",
            storyboardPacketDraftFile = storyboardPacketDraftFile,
            storyboardPacketReviewFile = storyboardPacketReviewFile,
            storyboardHumanReviewPreflightFile = storyboardHumanReviewPreflightFile,
            storyboardStoryMovementAuditFile = storyboardStoryMovementAuditFile,
            storyboardPacketReviewId = storyboardPacketReview.reviewId,
            storyboardStoryMovementAuditId = storyboardStoryMovementAudit.auditId,
            storyboardPacketDraftId = storyboardPacketDraft.packetId,
            storyboardPlanningBriefId = storyboardPacketDraft.storyboardPlanningBriefId,
            acceptedScriptPacketId = storyboardPacketDraft.acceptedScriptPacketId,
            scriptPacketDraftFile = storyboardPacketDraft.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = storyboardPacketDraft.scriptPacketDraftContentSha256,
            sourceVerdicts = ReviewableStoryboardSourceVerdicts(
                storyboardPacketReviewVerdict = storyboardPacketReview.verdict,
                storyboardPacketReviewStatus = storyboardPacketReview.reviewStatus,
                storyboardHumanReviewPreflightVerdict = storyboardHumanReviewPreflight.verdict,
                storyboardStoryMovementAuditVerdict = storyboardStoryMovementAudit.verdict,
            ),
            movementEvidenceSummary = if (verdict == "PASS") movementSummary(storyboardStoryMovementAudit) else ReviewableStoryboardMovementEvidenceSummary(),
            humanReviewReadinessSummary = if (verdict == "PASS") {
                ReviewableStoryboardHumanReviewReadinessSummary(
                    reviewStatus = storyboardPacketReview.reviewStatus,
                    reviewChecklistCount = storyboardPacketReview.reviewChecklist.size,
                    residualRiskCount = storyboardPacketReview.remainingResidualRisks.size,
                    packetStatus = packetStatus,
                )
            } else {
                ReviewableStoryboardHumanReviewReadinessSummary()
            },
            frameSummaries = if (verdict == "PASS") storyboardPacketDraft.frames.map { frame ->
                ReviewableStoryboardFrameSummary(
                    frameId = frame.frameId,
                    sceneReference = frame.sceneReference,
                    beatReference = frame.beatReference,
                    visualIntent = frame.visualIntent,
                    cameraIntent = frame.cameraIntent,
                    mustShow = frame.mustShow,
                    doNotShow = frame.doNotShow,
                    continuityAnchors = frame.continuityAnchors,
                    assetCandidateRefs = frame.assetCandidateRefs,
                )
            } else {
                emptyList()
            },
            protectedContinuityAnchorsSummary = if (verdict == "PASS") storyboardPacketReview.protectedContinuityAnchorsSummary else storyboardPacketReview.protectedContinuityAnchorsSummary.copy(protectedContinuityAnchorCount = 0, protectedContinuityAnchors = emptyList()),
            remainingResidualRisks = if (verdict == "PASS") storyboardPacketReview.remainingResidualRisks else emptyList(),
            reviewChecklist = if (verdict == "PASS") storyboardPacketReview.reviewChecklist else emptyList(),
            nextAllowedDownstreamStep = if (verdict == "PASS") packetNextStep else "",
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = ReviewableStoryboardPacketEvidence(
                sourceBindingFieldCount = sourceBindingFieldCount,
                frameCount = storyboardPacketDraft.frames.size,
                uniqueFrameIdCount = storyboardPacketDraft.frames.map { it.frameId.trim() }.filter { it.isNotBlank() }.distinct().size,
                movementEvidenceCount = storyboardStoryMovementAudit.evidence.movementEvidenceCount,
                protectedContinuityAnchorCount = storyboardPacketReview.protectedContinuityAnchorsSummary.protectedContinuityAnchorCount,
                coveredContinuityAnchorCount = storyboardStoryMovementAudit.evidence.coveredContinuityAnchorCount,
                residualRiskCount = storyboardPacketReview.remainingResidualRisks.size,
                reviewChecklistCount = storyboardPacketReview.reviewChecklist.size,
                forbiddenClaimCount = forbiddenClaimCount,
            ),
        )
    }

    private fun validateUpstreamStates(
        review: StoryboardPacketReviewPacket,
        preflight: StoryboardPacketHumanReviewPreflightReport,
        audit: StoryboardStoryMovementAuditReport,
        findings: MutableList<ReviewableStoryboardPacketFinding>,
    ) {
        if (review.verdict.trim().uppercase() != "PASS") findings.add(finding("STORYBOARD_REVIEW_NOT_PASS", "upstream", "CRITICAL", "$.storyboardPacketReview.verdict", "Reviewable storyboard packet requires PASS storyboard packet review.", listOf(review.verdict)))
        if (review.reviewStatus != "REVIEW_READY") findings.add(finding("STORYBOARD_REVIEW_STATUS_NOT_READY", "upstream", "CRITICAL", "$.storyboardPacketReview.reviewStatus", "Reviewable storyboard packet requires reviewStatus=REVIEW_READY.", listOf(review.reviewStatus)))
        if (review.nextAllowedDownstreamStep != reviewNextStep) findings.add(finding("STORYBOARD_REVIEW_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.storyboardPacketReview.nextAllowedDownstreamStep", "Reviewable storyboard packet requires review nextAllowedDownstreamStep=storyboard_packet_human_review.", listOf(review.nextAllowedDownstreamStep)))
        if (preflight.verdict.trim().uppercase() != "PASS") findings.add(finding("HUMAN_REVIEW_PREFLIGHT_NOT_PASS", "upstream", "CRITICAL", "$.storyboardHumanReviewPreflight.verdict", "Reviewable storyboard packet requires PASS human review preflight.", listOf(preflight.verdict)))
        if (preflight.nextAllowedDownstreamStep != preflightNextStep) findings.add(finding("HUMAN_REVIEW_PREFLIGHT_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.storyboardHumanReviewPreflight.nextAllowedDownstreamStep", "Reviewable storyboard packet requires preflight nextAllowedDownstreamStep=storyboard_packet_human_review_ready.", listOf(preflight.nextAllowedDownstreamStep)))
        if (audit.verdict.trim().uppercase() != "PASS") findings.add(finding("STORY_MOVEMENT_AUDIT_NOT_PASS", "upstream", "CRITICAL", "$.storyboardStoryMovementAudit.verdict", "Reviewable storyboard packet requires PASS story movement audit.", listOf(audit.verdict)))
        if (audit.nextAllowedDownstreamStep != auditNextStep) findings.add(finding("STORY_MOVEMENT_AUDIT_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.storyboardStoryMovementAudit.nextAllowedDownstreamStep", "Reviewable storyboard packet requires audit nextAllowedDownstreamStep=story_movement_audited.", listOf(audit.nextAllowedDownstreamStep)))
    }

    private fun validateSourceBinding(
        draft: StoryboardPacketDraft,
        review: StoryboardPacketReviewPacket,
        preflight: StoryboardPacketHumanReviewPreflightReport,
        audit: StoryboardStoryMovementAuditReport,
        draftFile: String,
        reviewFile: String,
        preflightFile: String,
        auditFile: String,
        findings: MutableList<ReviewableStoryboardPacketFinding>,
    ): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Reviewable storyboard packet requires nonblank $label.")) else count += 1
        }
        fun requireMatch(leftValue: String, rightValue: String, leftPath: String, rightPath: String, missingRuleId: String, mismatchRuleId: String, label: String, normalize: (String) -> String = { it.trim() }) {
            requireNonBlank(leftValue, leftPath, missingRuleId, label)
            val left = normalize(leftValue)
            val right = normalize(rightValue)
            if (left.isNotBlank() && (right.isBlank() || left != right)) {
                findings.add(finding(mismatchRuleId, "source_linkage", "CRITICAL", rightPath, "Reviewable storyboard packet source binding mismatch for $label.", listOf("left=$leftValue", "right=$rightValue")))
            }
        }

        requireNonBlank(draftFile, "$.inputs.storyboardPacketDraftFile", "STORYBOARD_PACKET_DRAFT_FILE_MISSING", "storyboard packet draft input file")
        requireNonBlank(reviewFile, "$.inputs.storyboardPacketReviewFile", "STORYBOARD_PACKET_REVIEW_FILE_MISSING", "storyboard packet review input file")
        requireNonBlank(preflightFile, "$.inputs.storyboardHumanReviewPreflightFile", "HUMAN_REVIEW_PREFLIGHT_FILE_MISSING", "human review preflight input file")
        requireNonBlank(auditFile, "$.inputs.storyboardStoryMovementAuditFile", "STORY_MOVEMENT_AUDIT_FILE_MISSING", "story movement audit input file")
        requireMatch(draftFile, review.storyboardPacketDraftFile, "$.inputs.storyboardPacketDraftFile", "$.storyboardPacketReview.storyboardPacketDraftFile", "STORYBOARD_PACKET_DRAFT_FILE_MISSING", "STORYBOARD_PACKET_DRAFT_FILE_MISMATCH", "storyboard packet draft file", ::normalizePath)
        requireMatch(draftFile, preflight.storyboardPacketDraftFile, "$.inputs.storyboardPacketDraftFile", "$.storyboardHumanReviewPreflight.storyboardPacketDraftFile", "STORYBOARD_PACKET_DRAFT_FILE_MISSING", "PREFLIGHT_STORYBOARD_PACKET_DRAFT_FILE_MISMATCH", "preflight storyboard packet draft file", ::normalizePath)
        requireMatch(draftFile, audit.storyboardPacketDraftFile, "$.inputs.storyboardPacketDraftFile", "$.storyboardStoryMovementAudit.storyboardPacketDraftFile", "STORYBOARD_PACKET_DRAFT_FILE_MISSING", "AUDIT_STORYBOARD_PACKET_DRAFT_FILE_MISMATCH", "audit storyboard packet draft file", ::normalizePath)
        requireMatch(reviewFile, preflight.storyboardPacketReviewFile, "$.inputs.storyboardPacketReviewFile", "$.storyboardHumanReviewPreflight.storyboardPacketReviewFile", "STORYBOARD_PACKET_REVIEW_FILE_MISSING", "STORYBOARD_PACKET_REVIEW_FILE_MISMATCH", "storyboard packet review file", ::normalizePath)
        requireMatch(reviewFile, audit.storyboardPacketReviewFile, "$.inputs.storyboardPacketReviewFile", "$.storyboardStoryMovementAudit.storyboardPacketReviewFile", "STORYBOARD_PACKET_REVIEW_FILE_MISSING", "AUDIT_STORYBOARD_PACKET_REVIEW_FILE_MISMATCH", "audit storyboard packet review file", ::normalizePath)
        requireMatch(preflightFile, audit.storyboardHumanReviewPreflightFile, "$.inputs.storyboardHumanReviewPreflightFile", "$.storyboardStoryMovementAudit.storyboardHumanReviewPreflightFile", "HUMAN_REVIEW_PREFLIGHT_FILE_MISSING", "AUDIT_HUMAN_REVIEW_PREFLIGHT_FILE_MISMATCH", "audit human review preflight file", ::normalizePath)
        requireMatch(review.reviewId, preflight.storyboardPacketReviewId, "$.storyboardPacketReview.reviewId", "$.storyboardHumanReviewPreflight.storyboardPacketReviewId", "STORYBOARD_PACKET_REVIEW_ID_MISSING", "STORYBOARD_PACKET_REVIEW_ID_MISMATCH", "storyboard packet review id")
        requireMatch(review.reviewId, audit.storyboardPacketReviewId, "$.storyboardPacketReview.reviewId", "$.storyboardStoryMovementAudit.storyboardPacketReviewId", "STORYBOARD_PACKET_REVIEW_ID_MISSING", "AUDIT_STORYBOARD_PACKET_REVIEW_ID_MISMATCH", "audit storyboard packet review id")
        requireMatch(draft.packetId, review.storyboardPacketDraftId, "$.storyboardPacketDraft.packetId", "$.storyboardPacketReview.storyboardPacketDraftId", "STORYBOARD_PACKET_DRAFT_ID_MISSING", "STORYBOARD_PACKET_DRAFT_ID_MISMATCH", "storyboard packet draft id")
        requireMatch(draft.packetId, preflight.storyboardPacketDraftId, "$.storyboardPacketDraft.packetId", "$.storyboardHumanReviewPreflight.storyboardPacketDraftId", "STORYBOARD_PACKET_DRAFT_ID_MISSING", "PREFLIGHT_STORYBOARD_PACKET_DRAFT_ID_MISMATCH", "preflight storyboard packet draft id")
        requireMatch(draft.packetId, audit.storyboardPacketDraftId, "$.storyboardPacketDraft.packetId", "$.storyboardStoryMovementAudit.storyboardPacketDraftId", "STORYBOARD_PACKET_DRAFT_ID_MISSING", "AUDIT_STORYBOARD_PACKET_DRAFT_ID_MISMATCH", "audit storyboard packet draft id")
        requireMatch(draft.storyboardPlanningBriefId, review.storyboardPlanningBriefId, "$.storyboardPacketDraft.storyboardPlanningBriefId", "$.storyboardPacketReview.storyboardPlanningBriefId", "STORYBOARD_PLANNING_BRIEF_ID_MISSING", "STORYBOARD_PLANNING_BRIEF_ID_MISMATCH", "storyboard planning brief id")
        requireMatch(draft.storyboardPlanningBriefId, preflight.storyboardPlanningBriefId, "$.storyboardPacketDraft.storyboardPlanningBriefId", "$.storyboardHumanReviewPreflight.storyboardPlanningBriefId", "STORYBOARD_PLANNING_BRIEF_ID_MISSING", "PREFLIGHT_STORYBOARD_PLANNING_BRIEF_ID_MISMATCH", "preflight storyboard planning brief id")
        requireMatch(draft.storyboardPlanningBriefId, audit.storyboardPlanningBriefId, "$.storyboardPacketDraft.storyboardPlanningBriefId", "$.storyboardStoryMovementAudit.storyboardPlanningBriefId", "STORYBOARD_PLANNING_BRIEF_ID_MISSING", "AUDIT_STORYBOARD_PLANNING_BRIEF_ID_MISMATCH", "audit storyboard planning brief id")
        requireMatch(draft.acceptedScriptPacketId, review.acceptedScriptPacketId, "$.storyboardPacketDraft.acceptedScriptPacketId", "$.storyboardPacketReview.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "ACCEPTED_SCRIPT_PACKET_ID_MISMATCH", "accepted script packet id")
        requireMatch(draft.acceptedScriptPacketId, preflight.acceptedScriptPacketId, "$.storyboardPacketDraft.acceptedScriptPacketId", "$.storyboardHumanReviewPreflight.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "PREFLIGHT_ACCEPTED_SCRIPT_PACKET_ID_MISMATCH", "preflight accepted script packet id")
        requireMatch(draft.acceptedScriptPacketId, audit.acceptedScriptPacketId, "$.storyboardPacketDraft.acceptedScriptPacketId", "$.storyboardStoryMovementAudit.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "AUDIT_ACCEPTED_SCRIPT_PACKET_ID_MISMATCH", "audit accepted script packet id")
        requireMatch(draft.scriptPacketDraftFile, review.scriptPacketDraftFile, "$.storyboardPacketDraft.scriptPacketDraftFile", "$.storyboardPacketReview.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "SCRIPT_PACKET_DRAFT_FILE_MISMATCH", "script packet draft file", ::normalizePath)
        requireMatch(draft.scriptPacketDraftFile, preflight.scriptPacketDraftFile, "$.storyboardPacketDraft.scriptPacketDraftFile", "$.storyboardHumanReviewPreflight.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "PREFLIGHT_SCRIPT_PACKET_DRAFT_FILE_MISMATCH", "preflight script packet draft file", ::normalizePath)
        requireMatch(draft.scriptPacketDraftFile, audit.scriptPacketDraftFile, "$.storyboardPacketDraft.scriptPacketDraftFile", "$.storyboardStoryMovementAudit.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "AUDIT_SCRIPT_PACKET_DRAFT_FILE_MISMATCH", "audit script packet draft file", ::normalizePath)
        requireMatch(draft.scriptPacketDraftContentSha256, review.scriptPacketDraftContentSha256, "$.storyboardPacketDraft.scriptPacketDraftContentSha256", "$.storyboardPacketReview.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "SCRIPT_PACKET_DRAFT_HASH_MISMATCH", "script packet draft SHA-256", { it.trim().lowercase() })
        requireMatch(draft.scriptPacketDraftContentSha256, preflight.scriptPacketDraftContentSha256, "$.storyboardPacketDraft.scriptPacketDraftContentSha256", "$.storyboardHumanReviewPreflight.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "PREFLIGHT_SCRIPT_PACKET_DRAFT_HASH_MISMATCH", "preflight script packet draft SHA-256", { it.trim().lowercase() })
        requireMatch(draft.scriptPacketDraftContentSha256, audit.scriptPacketDraftContentSha256, "$.storyboardPacketDraft.scriptPacketDraftContentSha256", "$.storyboardStoryMovementAudit.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "AUDIT_SCRIPT_PACKET_DRAFT_HASH_MISMATCH", "audit script packet draft SHA-256", { it.trim().lowercase() })
        requireMatch(review.storyboardPacketDraftReportFile, preflight.storyboardPacketDraftReportFile, "$.storyboardPacketReview.storyboardPacketDraftReportFile", "$.storyboardHumanReviewPreflight.storyboardPacketDraftReportFile", "STORYBOARD_PACKET_DRAFT_REPORT_FILE_MISSING", "STORYBOARD_PACKET_DRAFT_REPORT_FILE_MISMATCH", "storyboard packet draft validation report file", ::normalizePath)
        if (draft.scriptPacketDraftContentSha256.isNotBlank() && !sha256Regex.matches(draft.scriptPacketDraftContentSha256.trim())) {
            findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.storyboardPacketDraft.scriptPacketDraftContentSha256", "Reviewable storyboard packet requires lowercase 64-character SHA-256.", listOf(draft.scriptPacketDraftContentSha256)))
        }
        return count
    }

    private fun validateFrameSet(
        draft: StoryboardPacketDraft,
        review: StoryboardPacketReviewPacket,
        findings: MutableList<ReviewableStoryboardPacketFinding>,
    ) {
        if (draft.frames.isEmpty()) {
            findings.add(finding("STORYBOARD_FRAMES_MISSING", "storyboard_packet", "CRITICAL", "$.storyboardPacketDraft.frames", "Reviewable storyboard packet requires at least one frame."))
            return
        }
        val ids = draft.frames.map { it.frameId.trim() }
        ids.forEachIndexed { index, id ->
            if (id.isBlank()) findings.add(finding("FRAME_ID_MISSING", "storyboard_packet", "CRITICAL", "$.storyboardPacketDraft.frames[$index].frameId", "Every reviewable storyboard frame requires a nonblank frame id."))
        }
        val duplicates = ids.groupingBy { it }.eachCount().filter { it.key.isNotBlank() && it.value > 1 }.keys
        if (duplicates.isNotEmpty()) findings.add(finding("FRAME_ID_DUPLICATE", "storyboard_packet", "CRITICAL", "$.storyboardPacketDraft.frames", "Reviewable storyboard frame ids must be unique.", duplicates.toList()))
        if (ids.filter { it.isNotBlank() }.toSet() != review.frameIds.filter { it.isNotBlank() }.toSet()) {
            findings.add(finding("FRAME_ID_SET_MISMATCH", "source_linkage", "CRITICAL", "$.storyboardPacketReview.frameIds", "Reviewable storyboard frame ids must match the reviewed frame id set.", listOf("draft=${ids.joinToString(",")}", "review=${review.frameIds.joinToString(",")}")))
        }
    }

    private fun validateMovementEvidence(
        draft: StoryboardPacketDraft,
        review: StoryboardPacketReviewPacket,
        audit: StoryboardStoryMovementAuditReport,
        findings: MutableList<ReviewableStoryboardPacketFinding>,
    ) {
        if (audit.frameCount != draft.frames.size || audit.evidence.frameCount != draft.frames.size || review.frameCount != draft.frames.size) {
            findings.add(finding("MOVEMENT_FRAME_COUNT_MISMATCH", "story_movement", "CRITICAL", "$.storyboardStoryMovementAudit.frameCount", "Audit, review, and draft frame counts must match.", listOf("draft=${draft.frames.size}", "review=${review.frameCount}", "audit=${audit.frameCount}", "auditEvidence=${audit.evidence.frameCount}")))
        }
        if (audit.movementEvidenceCount <= 0 || audit.evidence.movementEvidenceCount <= 0) {
            findings.add(finding("MOVEMENT_EVIDENCE_MISSING", "story_movement", "CRITICAL", "$.storyboardStoryMovementAudit.movementEvidenceCount", "Reviewable storyboard packet requires movement evidence from the story movement audit."))
        }
        if (audit.evidence.openingPressureEvidenceCount <= 0) findings.add(finding("OPENING_PRESSURE_EVIDENCE_MISSING", "story_movement", "MAJOR", "$.storyboardStoryMovementAudit.evidence.openingPressureEvidenceCount", "Reviewable storyboard packet requires opening pressure evidence."))
        if (audit.evidence.actionEvidenceCount <= 0) findings.add(finding("ACTION_EVIDENCE_MISSING", "story_movement", "MAJOR", "$.storyboardStoryMovementAudit.evidence.actionEvidenceCount", "Reviewable storyboard packet requires action evidence."))
        if (audit.evidence.consequenceOrChoiceEvidenceCount <= 0) findings.add(finding("CONSEQUENCE_OR_CHOICE_EVIDENCE_MISSING", "story_movement", "MAJOR", "$.storyboardStoryMovementAudit.evidence.consequenceOrChoiceEvidenceCount", "Reviewable storyboard packet requires consequence or choice evidence."))
    }

    private fun validateContinuitySummary(
        review: StoryboardPacketReviewPacket,
        audit: StoryboardStoryMovementAuditReport,
        findings: MutableList<ReviewableStoryboardPacketFinding>,
    ) {
        val protectedCount = review.protectedContinuityAnchorsSummary.protectedContinuityAnchorCount
        if (protectedCount <= 0 || review.protectedContinuityAnchorsSummary.protectedContinuityAnchors.isEmpty()) {
            findings.add(finding("PROTECTED_CONTINUITY_ANCHORS_MISSING", "continuity", "CRITICAL", "$.storyboardPacketReview.protectedContinuityAnchorsSummary", "Reviewable storyboard packet requires protected continuity anchors."))
        }
        if (audit.evidence.protectedContinuityAnchorCount != protectedCount || audit.evidence.coveredContinuityAnchorCount != protectedCount || audit.coveredContinuityAnchorCount != protectedCount) {
            findings.add(finding("CONTINUITY_ANCHOR_COVERAGE_MISMATCH", "continuity", "CRITICAL", "$.storyboardStoryMovementAudit.evidence.coveredContinuityAnchorCount", "Story movement audit must cover every protected continuity anchor before packaging.", listOf("protected=$protectedCount", "auditProtected=${audit.evidence.protectedContinuityAnchorCount}", "auditCovered=${audit.evidence.coveredContinuityAnchorCount}", "reportCovered=${audit.coveredContinuityAnchorCount}")))
        }
    }

    private fun validateForbiddenDraftOutputFields(
        draft: StoryboardPacketDraft,
        findings: MutableList<ReviewableStoryboardPacketFinding>,
    ): Int {
        var count = 0
        draft.frames.forEachIndexed { index, frame ->
            val path = "$.storyboardPacketDraft.frames[$index]"
            if (frame.imageOutputPath.isNotBlank()) {
                count += 1
                findings.add(finding("IMAGE_OUTPUT_PATH_FORBIDDEN", "safety_boundary", "CRITICAL", "$path.imageOutputPath", "Reviewable storyboard packet cannot carry image output paths.", listOf(frame.imageOutputPath)))
            }
            if (frame.videoOutputPath.isNotBlank()) {
                count += 1
                findings.add(finding("VIDEO_OUTPUT_PATH_FORBIDDEN", "safety_boundary", "CRITICAL", "$path.videoOutputPath", "Reviewable storyboard packet cannot carry video output paths.", listOf(frame.videoOutputPath)))
            }
            if (frame.generationApi.isNotBlank()) {
                count += 1
                findings.add(finding("GENERATION_API_FORBIDDEN", "safety_boundary", "CRITICAL", "$path.generationApi", "Reviewable storyboard packet cannot carry generation API or provider fields.", listOf(frame.generationApi)))
            }
            val approvalStatus = frame.assetApprovalStatus.trim()
            if (approvalStatus.isNotBlank() && !approvalStatus.equals("candidate_only", ignoreCase = true)) {
                count += 1
                findings.add(finding("ASSET_APPROVAL_STATUS_FORBIDDEN", "safety_boundary", "CRITICAL", "$path.assetApprovalStatus", "Reviewable storyboard packet allows only blank or candidate_only frame asset status.", listOf(frame.assetApprovalStatus)))
            }
        }
        draft.assetCandidates.forEachIndexed { index, candidate ->
            val approvalStatus = candidate.approvalStatus.trim()
            if (approvalStatus.isNotBlank() && !approvalStatus.equals("candidate_only", ignoreCase = true)) {
                count += 1
                findings.add(finding("ASSET_APPROVAL_STATUS_FORBIDDEN", "safety_boundary", "CRITICAL", "$.storyboardPacketDraft.assetCandidates[$index].approvalStatus", "Reviewable storyboard packet allows only candidate_only asset candidate status.", listOf(candidate.approvalStatus)))
            }
        }
        return count
    }

    private fun scanForbiddenClaims(
        draft: StoryboardPacketDraft,
        review: StoryboardPacketReviewPacket,
        preflight: StoryboardPacketHumanReviewPreflightReport,
        audit: StoryboardStoryMovementAuditReport,
        findings: MutableList<ReviewableStoryboardPacketFinding>,
    ): Int {
        var count = 0
        val fields = mutableListOf<TextField>()
        fields += collectDraftText(draft)
        fields += TextField("$.storyboardPacketReview.reviewStatus", review.reviewStatus)
        fields += TextField("$.storyboardPacketReview.nextAllowedDownstreamStep", review.nextAllowedDownstreamStep)
        fields += TextField("$.storyboardHumanReviewPreflight.nextAllowedDownstreamStep", preflight.nextAllowedDownstreamStep)
        fields += TextField("$.storyboardStoryMovementAudit.nextAllowedDownstreamStep", audit.nextAllowedDownstreamStep)
        review.assetCandidateRequirements.forEachIndexed { index, item -> fields += TextField("$.storyboardPacketReview.assetCandidateRequirements[$index].requirement", item.requirement) }
        review.remainingResidualRisks.forEachIndexed { index, risk -> fields += TextField("$.storyboardPacketReview.remainingResidualRisks[$index]", risk) }
        fields.filter { it.value.isNotBlank() }.forEach { field ->
            forbiddenClaimPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings.add(finding("DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", "CRITICAL", field.path, "Reviewable storyboard packet cannot carry approval, generated media, provider job, canon, production lock, or runtime scene claims.", listOf(field.value)))
                }
            }
        }
        return count
    }

    private fun collectDraftText(draft: StoryboardPacketDraft): List<TextField> {
        val fields = mutableListOf<TextField>()
        fields += TextField("$.storyboardPacketDraft.reviewNotes", draft.reviewNotes)
        draft.protectedContinuityAnchors.forEachIndexed { index, anchor -> fields += TextField("$.storyboardPacketDraft.protectedContinuityAnchors[$index]", anchor) }
        draft.frames.forEachIndexed { index, frame ->
            val path = "$.storyboardPacketDraft.frames[$index]"
            fields += TextField("$path.sceneReference", frame.sceneReference)
            fields += TextField("$path.beatReference", frame.beatReference)
            fields += TextField("$path.visualIntent", frame.visualIntent)
            fields += TextField("$path.cameraIntent", frame.cameraIntent)
            fields += TextField("$path.reviewNotes", frame.reviewNotes)
            fields += TextField("$path.imageOutputPath", frame.imageOutputPath)
            fields += TextField("$path.videoOutputPath", frame.videoOutputPath)
            fields += TextField("$path.generationApi", frame.generationApi)
            fields += TextField("$path.assetApprovalStatus", frame.assetApprovalStatus)
            frame.mustShow.forEachIndexed { itemIndex, value -> fields += TextField("$path.mustShow[$itemIndex]", value) }
            frame.doNotShow.forEachIndexed { itemIndex, value -> fields += TextField("$path.doNotShow[$itemIndex]", value) }
            frame.continuityAnchors.forEachIndexed { itemIndex, value -> fields += TextField("$path.continuityAnchors[$itemIndex]", value) }
        }
        draft.assetCandidates.forEachIndexed { index, candidate ->
            val path = "$.storyboardPacketDraft.assetCandidates[$index]"
            fields += TextField("$path.candidateId", candidate.candidateId)
            fields += TextField("$path.requirement", candidate.requirement)
            fields += TextField("$path.approvalStatus", candidate.approvalStatus)
            fields += TextField("$path.reviewNotes", candidate.reviewNotes)
        }
        return fields
    }

    private fun movementSummary(audit: StoryboardStoryMovementAuditReport): ReviewableStoryboardMovementEvidenceSummary =
        ReviewableStoryboardMovementEvidenceSummary(
            frameCount = audit.evidence.frameCount,
            movementEvidenceCount = audit.evidence.movementEvidenceCount,
            openingPressureEvidenceCount = audit.evidence.openingPressureEvidenceCount,
            actionEvidenceCount = audit.evidence.actionEvidenceCount,
            consequenceOrChoiceEvidenceCount = audit.evidence.consequenceOrChoiceEvidenceCount,
            coveredContinuityAnchorCount = audit.evidence.coveredContinuityAnchorCount,
        )

    private fun normalizePath(value: String): String = value.trim().replace('\\', '/')

    private fun finding(ruleId: String, category: String, severity: String, path: String, message: String, evidence: List<String> = emptyList(), suggestedFix: String = ""): ReviewableStoryboardPacketFinding =
        ReviewableStoryboardPacketFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private data class TextField(val path: String, val value: String)
}
