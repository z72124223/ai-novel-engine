package com.localnovel.engine.service

import com.localnovel.engine.model.StoryboardPacketDraft
import com.localnovel.engine.model.StoryboardPacketFrameDraft
import com.localnovel.engine.model.StoryboardPacketHumanReviewPreflightReport
import com.localnovel.engine.model.StoryboardPacketReviewPacket
import com.localnovel.engine.model.StoryboardStoryMovementAuditEvidence
import com.localnovel.engine.model.StoryboardStoryMovementAuditFinding
import com.localnovel.engine.model.StoryboardStoryMovementAuditReport

class StoryboardStoryMovementAuditService {
    private val requiredPreflightNextStep = "storyboard_packet_human_review_ready"
    private val requiredReviewNextStep = "storyboard_packet_human_review"
    private val nextAllowedDownstreamStep = "story_movement_audited"
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
        Regex("""(?i)\b(final|completed)\s+(storyboard|asset|image|video)\b"""),
        Regex("\u6838\u51c6|\u6279\u51c6|\u6b63\u5178|\u9396\u5b9a|\u6b63\u5f0f\u7d20\u6750"),
    )
    private val openingPressureMarkers = listOf(
        "pressure", "gate", "guard", "toll", "cost", "must", "requires", "cannot", "blocked", "checkpoint",
        "\u58d3\u529b", "\u9580", "\u5b88\u885b", "\u4ee3\u50f9", "\u5fc5\u9808", "\u8981\u6c42", "\u4e0d\u80fd", "\u95dc\u5361",
    )
    private val actionMarkers = listOf(
        "arrive", "arriving", "check", "checks", "checking", "requires", "keeps", "asks", "answers", "moves",
        "pays", "paid", "blocks", "gives", "receives", "evaluating", "answering",
        "\u62b5\u9054", "\u5230\u9054", "\u6aa2\u67e5", "\u8981\u6c42", "\u4fdd\u6301", "\u8a62\u554f", "\u56de\u7b54", "\u79fb\u52d5",
    )
    private val consequenceOrChoiceMarkers = listOf(
        "before", "after", "therefore", "because", "so ", "choice", "chooses", "consequence", "route", "information",
        "receives", "does not receive", "answer", "still", "waits",
        "\u4e4b\u524d", "\u4e4b\u5f8c", "\u56e0\u6b64", "\u56e0\u70ba", "\u6240\u4ee5", "\u9078\u64c7", "\u5f8c\u679c", "\u8def\u7dda", "\u60c5\u5831",
    )

    fun audit(
        storyboardPacketDraft: StoryboardPacketDraft,
        storyboardPacketReview: StoryboardPacketReviewPacket,
        storyboardHumanReviewPreflight: StoryboardPacketHumanReviewPreflightReport,
        storyboardPacketDraftFile: String = "",
        storyboardPacketReviewFile: String = "",
        storyboardHumanReviewPreflightFile: String = "",
        auditId: String = "",
    ): StoryboardStoryMovementAuditReport {
        val findings = mutableListOf<StoryboardStoryMovementAuditFinding>()
        validateUpstreamState(storyboardPacketReview, storyboardHumanReviewPreflight, findings)
        val sourceBindingFieldCount = validateSourceBinding(
            draft = storyboardPacketDraft,
            review = storyboardPacketReview,
            preflight = storyboardHumanReviewPreflight,
            draftFile = storyboardPacketDraftFile,
            reviewFile = storyboardPacketReviewFile,
            preflightFile = storyboardHumanReviewPreflightFile,
            findings = findings,
        )
        val frameEvidence = validateFrameMovement(storyboardPacketDraft, storyboardPacketReview, findings)
        val coveredContinuityAnchorCount = validateContinuityAnchors(storyboardPacketDraft, storyboardPacketReview, findings)
        val forbiddenClaimCount =
            validateForbiddenDraftOutputFields(storyboardPacketDraft, findings) +
                scanForbiddenClaims(storyboardPacketDraft, storyboardPacketReview, storyboardHumanReviewPreflight, findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") {
            findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId }
        } else {
            ""
        }
        val resolvedAuditId = auditId.ifBlank {
            storyboardPacketReview.reviewId.takeIf { it.isNotBlank() }?.let { "$it-story-movement-audit" }.orEmpty()
        }

        return StoryboardStoryMovementAuditReport(
            verdict = verdict,
            auditId = resolvedAuditId,
            storyboardPacketDraftFile = storyboardPacketDraftFile,
            storyboardPacketReviewFile = storyboardPacketReviewFile,
            storyboardHumanReviewPreflightFile = storyboardHumanReviewPreflightFile,
            storyboardPacketReviewId = storyboardPacketReview.reviewId,
            storyboardPacketDraftId = storyboardPacketDraft.packetId,
            storyboardPlanningBriefId = storyboardPacketDraft.storyboardPlanningBriefId,
            acceptedScriptPacketId = storyboardPacketDraft.acceptedScriptPacketId,
            scriptPacketDraftFile = storyboardPacketDraft.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = storyboardPacketDraft.scriptPacketDraftContentSha256,
            frameCount = if (verdict == "FAIL") 0 else storyboardPacketDraft.frames.size,
            movementEvidenceCount = if (verdict == "FAIL") 0 else frameEvidence.movementEvidenceCount,
            coveredContinuityAnchorCount = if (verdict == "FAIL") 0 else coveredContinuityAnchorCount,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = StoryboardStoryMovementAuditEvidence(
                humanReviewPreflightVerdict = storyboardHumanReviewPreflight.verdict,
                humanReviewPreflightNextAllowedDownstreamStep = storyboardHumanReviewPreflight.nextAllowedDownstreamStep,
                reviewPacketVerdict = storyboardPacketReview.verdict,
                reviewStatus = storyboardPacketReview.reviewStatus,
                sourceBindingFieldCount = sourceBindingFieldCount,
                frameCount = storyboardPacketDraft.frames.size,
                uniqueFrameIdCount = storyboardPacketDraft.frames.map { it.frameId.trim() }.filter { it.isNotBlank() }.distinct().size,
                movementEvidenceCount = frameEvidence.movementEvidenceCount,
                openingPressureEvidenceCount = frameEvidence.openingPressureEvidenceCount,
                actionEvidenceCount = frameEvidence.actionEvidenceCount,
                consequenceOrChoiceEvidenceCount = frameEvidence.consequenceOrChoiceEvidenceCount,
                protectedContinuityAnchorCount = storyboardPacketReview.protectedContinuityAnchorsSummary.protectedContinuityAnchorCount,
                coveredContinuityAnchorCount = coveredContinuityAnchorCount,
                forbiddenClaimCount = forbiddenClaimCount,
            ),
        )
    }

    private fun validateUpstreamState(
        review: StoryboardPacketReviewPacket,
        preflight: StoryboardPacketHumanReviewPreflightReport,
        findings: MutableList<StoryboardStoryMovementAuditFinding>,
    ) {
        if (review.verdict.trim().uppercase() != "PASS") findings.add(finding("STORYBOARD_REVIEW_NOT_PASS", "upstream", "CRITICAL", "$.storyboardPacketReview.verdict", "Story movement audit requires PASS storyboard packet review.", listOf(review.verdict)))
        if (review.reviewStatus != "REVIEW_READY") findings.add(finding("STORYBOARD_REVIEW_STATUS_NOT_READY", "upstream", "CRITICAL", "$.storyboardPacketReview.reviewStatus", "Story movement audit requires reviewStatus=REVIEW_READY.", listOf(review.reviewStatus)))
        if (review.nextAllowedDownstreamStep != requiredReviewNextStep) findings.add(finding("STORYBOARD_REVIEW_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.storyboardPacketReview.nextAllowedDownstreamStep", "Story movement audit requires review nextAllowedDownstreamStep=storyboard_packet_human_review.", listOf(review.nextAllowedDownstreamStep)))
        if (preflight.verdict.trim().uppercase() != "PASS") findings.add(finding("HUMAN_REVIEW_PREFLIGHT_NOT_PASS", "upstream", "CRITICAL", "$.storyboardHumanReviewPreflight.verdict", "Story movement audit requires PASS human review preflight.", listOf(preflight.verdict)))
        if (preflight.nextAllowedDownstreamStep != requiredPreflightNextStep) findings.add(finding("HUMAN_REVIEW_PREFLIGHT_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.storyboardHumanReviewPreflight.nextAllowedDownstreamStep", "Story movement audit requires preflight nextAllowedDownstreamStep=storyboard_packet_human_review_ready.", listOf(preflight.nextAllowedDownstreamStep)))
    }

    private fun validateSourceBinding(
        draft: StoryboardPacketDraft,
        review: StoryboardPacketReviewPacket,
        preflight: StoryboardPacketHumanReviewPreflightReport,
        draftFile: String,
        reviewFile: String,
        preflightFile: String,
        findings: MutableList<StoryboardStoryMovementAuditFinding>,
    ): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Story movement audit requires nonblank $label.")) else count += 1
        }
        fun requireMatch(leftValue: String, rightValue: String, leftPath: String, rightPath: String, missingRuleId: String, mismatchRuleId: String, label: String, normalize: (String) -> String = { it.trim() }) {
            requireNonBlank(leftValue, leftPath, missingRuleId, label)
            val left = normalize(leftValue)
            val right = normalize(rightValue)
            if (left.isNotBlank() && (right.isBlank() || left != right)) {
                findings.add(finding(mismatchRuleId, "source_linkage", "CRITICAL", rightPath, "Story movement audit source binding mismatch for $label.", listOf("left=$leftValue", "right=$rightValue")))
            }
        }

        requireNonBlank(draftFile, "$.inputs.storyboardPacketDraftFile", "STORYBOARD_PACKET_DRAFT_FILE_MISSING", "storyboard packet draft input file")
        requireNonBlank(reviewFile, "$.inputs.storyboardPacketReviewFile", "STORYBOARD_PACKET_REVIEW_FILE_MISSING", "storyboard packet review input file")
        requireNonBlank(preflightFile, "$.inputs.storyboardHumanReviewPreflightFile", "HUMAN_REVIEW_PREFLIGHT_FILE_MISSING", "human review preflight input file")
        requireMatch(draftFile, review.storyboardPacketDraftFile, "$.inputs.storyboardPacketDraftFile", "$.storyboardPacketReview.storyboardPacketDraftFile", "STORYBOARD_PACKET_DRAFT_FILE_MISSING", "STORYBOARD_PACKET_DRAFT_FILE_MISMATCH", "storyboard packet draft file", ::normalizePath)
        requireMatch(reviewFile, preflight.storyboardPacketReviewFile, "$.inputs.storyboardPacketReviewFile", "$.storyboardHumanReviewPreflight.storyboardPacketReviewFile", "STORYBOARD_PACKET_REVIEW_FILE_MISSING", "STORYBOARD_PACKET_REVIEW_FILE_MISMATCH", "storyboard packet review file", ::normalizePath)
        requireMatch(review.reviewId, preflight.storyboardPacketReviewId, "$.storyboardPacketReview.reviewId", "$.storyboardHumanReviewPreflight.storyboardPacketReviewId", "STORYBOARD_PACKET_REVIEW_ID_MISSING", "STORYBOARD_PACKET_REVIEW_ID_MISMATCH", "storyboard packet review id")
        requireMatch(draft.packetId, review.storyboardPacketDraftId, "$.storyboardPacketDraft.packetId", "$.storyboardPacketReview.storyboardPacketDraftId", "STORYBOARD_PACKET_DRAFT_ID_MISSING", "STORYBOARD_PACKET_DRAFT_ID_MISMATCH", "storyboard packet draft id")
        requireMatch(draft.packetId, preflight.storyboardPacketDraftId, "$.storyboardPacketDraft.packetId", "$.storyboardHumanReviewPreflight.storyboardPacketDraftId", "STORYBOARD_PACKET_DRAFT_ID_MISSING", "PREFLIGHT_STORYBOARD_PACKET_DRAFT_ID_MISMATCH", "preflight storyboard packet draft id")
        requireMatch(draft.storyboardPlanningBriefId, review.storyboardPlanningBriefId, "$.storyboardPacketDraft.storyboardPlanningBriefId", "$.storyboardPacketReview.storyboardPlanningBriefId", "STORYBOARD_PLANNING_BRIEF_ID_MISSING", "STORYBOARD_PLANNING_BRIEF_ID_MISMATCH", "storyboard planning brief id")
        requireMatch(draft.storyboardPlanningBriefId, preflight.storyboardPlanningBriefId, "$.storyboardPacketDraft.storyboardPlanningBriefId", "$.storyboardHumanReviewPreflight.storyboardPlanningBriefId", "STORYBOARD_PLANNING_BRIEF_ID_MISSING", "PREFLIGHT_STORYBOARD_PLANNING_BRIEF_ID_MISMATCH", "preflight storyboard planning brief id")
        requireMatch(draft.acceptedScriptPacketId, review.acceptedScriptPacketId, "$.storyboardPacketDraft.acceptedScriptPacketId", "$.storyboardPacketReview.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "ACCEPTED_SCRIPT_PACKET_ID_MISMATCH", "accepted script packet id")
        requireMatch(draft.acceptedScriptPacketId, preflight.acceptedScriptPacketId, "$.storyboardPacketDraft.acceptedScriptPacketId", "$.storyboardHumanReviewPreflight.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "PREFLIGHT_ACCEPTED_SCRIPT_PACKET_ID_MISMATCH", "preflight accepted script packet id")
        requireMatch(draft.scriptPacketDraftFile, review.scriptPacketDraftFile, "$.storyboardPacketDraft.scriptPacketDraftFile", "$.storyboardPacketReview.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "SCRIPT_PACKET_DRAFT_FILE_MISMATCH", "script packet draft file", ::normalizePath)
        requireMatch(draft.scriptPacketDraftFile, preflight.scriptPacketDraftFile, "$.storyboardPacketDraft.scriptPacketDraftFile", "$.storyboardHumanReviewPreflight.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "PREFLIGHT_SCRIPT_PACKET_DRAFT_FILE_MISMATCH", "preflight script packet draft file", ::normalizePath)
        requireMatch(draft.scriptPacketDraftContentSha256, review.scriptPacketDraftContentSha256, "$.storyboardPacketDraft.scriptPacketDraftContentSha256", "$.storyboardPacketReview.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "SCRIPT_PACKET_DRAFT_HASH_MISMATCH", "script packet draft SHA-256", { it.trim().lowercase() })
        requireMatch(draft.scriptPacketDraftContentSha256, preflight.scriptPacketDraftContentSha256, "$.storyboardPacketDraft.scriptPacketDraftContentSha256", "$.storyboardHumanReviewPreflight.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "PREFLIGHT_SCRIPT_PACKET_DRAFT_HASH_MISMATCH", "preflight script packet draft SHA-256", { it.trim().lowercase() })
        requireMatch(review.storyboardPacketDraftReportFile, preflight.storyboardPacketDraftReportFile, "$.storyboardPacketReview.storyboardPacketDraftReportFile", "$.storyboardHumanReviewPreflight.storyboardPacketDraftReportFile", "STORYBOARD_PACKET_DRAFT_REPORT_FILE_MISSING", "STORYBOARD_PACKET_DRAFT_REPORT_FILE_MISMATCH", "storyboard packet draft validation report file", ::normalizePath)
        if (draft.scriptPacketDraftContentSha256.isNotBlank() && !sha256Regex.matches(draft.scriptPacketDraftContentSha256.trim())) {
            findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.storyboardPacketDraft.scriptPacketDraftContentSha256", "Story movement audit requires lowercase 64-character SHA-256.", listOf(draft.scriptPacketDraftContentSha256)))
        }
        return count
    }

    private fun validateFrameMovement(
        draft: StoryboardPacketDraft,
        review: StoryboardPacketReviewPacket,
        findings: MutableList<StoryboardStoryMovementAuditFinding>,
    ): FrameEvidence {
        if (draft.frames.isEmpty()) {
            findings.add(finding("STORYBOARD_FRAMES_MISSING", "story_movement", "CRITICAL", "$.storyboardPacketDraft.frames", "Story movement audit requires at least one storyboard frame."))
            return FrameEvidence()
        }
        val ids = draft.frames.map { it.frameId.trim() }
        ids.forEachIndexed { index, id ->
            if (id.isBlank()) findings.add(finding("FRAME_ID_MISSING", "story_movement", "CRITICAL", "$.storyboardPacketDraft.frames[$index].frameId", "Every storyboard frame requires a nonblank frame id."))
        }
        val duplicates = ids.groupingBy { it }.eachCount().filter { it.key.isNotBlank() && it.value > 1 }.keys
        if (duplicates.isNotEmpty()) findings.add(finding("FRAME_ID_DUPLICATE", "story_movement", "CRITICAL", "$.storyboardPacketDraft.frames", "Storyboard frame ids must be unique.", duplicates.toList()))
        if (review.frameIds.filter { it.isNotBlank() }.toSet() != ids.filter { it.isNotBlank() }.toSet()) {
            findings.add(finding("REVIEW_FRAME_ID_SET_MISMATCH", "source_linkage", "CRITICAL", "$.storyboardPacketReview.frameIds", "Review packet frame ids must match storyboard draft frame ids.", listOf("draft=${ids.joinToString(",")}", "review=${review.frameIds.joinToString(",")}")))
        }

        var movement = 0
        var pressure = 0
        var action = 0
        var consequenceOrChoice = 0
        draft.frames.forEachIndexed { index, frame ->
            if (frame.sceneReference.isBlank()) findings.add(finding("FRAME_SCENE_REFERENCE_MISSING", "story_movement", "MAJOR", "$.storyboardPacketDraft.frames[$index].sceneReference", "Every frame must reference a scene."))
            if (frame.beatReference.isBlank()) findings.add(finding("FRAME_BEAT_REFERENCE_MISSING", "story_movement", "MAJOR", "$.storyboardPacketDraft.frames[$index].beatReference", "Every frame must reference a beat."))
            val text = frameMovementText(frame)
            val hasPressure = containsAny(text, openingPressureMarkers)
            val hasAction = containsAny(text, actionMarkers)
            val hasConsequenceOrChoice = containsAny(text, consequenceOrChoiceMarkers)
            if (hasPressure) pressure += 1
            if (hasAction) action += 1
            if (hasConsequenceOrChoice) consequenceOrChoice += 1
            if (hasAction || hasConsequenceOrChoice) {
                movement += 1
            } else {
                findings.add(finding("FRAME_MOVEMENT_EVIDENCE_MISSING", "story_movement", "MAJOR", "$.storyboardPacketDraft.frames[$index]", "Frame must include deterministic action, choice, or consequence evidence, not only static visual description.", listOf(text.take(180))))
            }
        }
        if (pressure == 0) findings.add(finding("OPENING_PRESSURE_EVIDENCE_MISSING", "story_movement", "MAJOR", "$.storyboardPacketDraft.frames", "Storyboard needs at least one opening pressure marker."))
        if (action == 0) findings.add(finding("ACTION_BEAT_EVIDENCE_MISSING", "story_movement", "MAJOR", "$.storyboardPacketDraft.frames", "Storyboard needs at least one action beat marker."))
        if (consequenceOrChoice == 0) findings.add(finding("CONSEQUENCE_OR_CHOICE_EVIDENCE_MISSING", "story_movement", "MAJOR", "$.storyboardPacketDraft.frames", "Storyboard needs at least one consequence or choice marker."))
        return FrameEvidence(movement, pressure, action, consequenceOrChoice)
    }

    private fun validateContinuityAnchors(
        draft: StoryboardPacketDraft,
        review: StoryboardPacketReviewPacket,
        findings: MutableList<StoryboardStoryMovementAuditFinding>,
    ): Int {
        val anchors = review.protectedContinuityAnchorsSummary.protectedContinuityAnchors.filter { it.isNotBlank() }
        if (review.protectedContinuityAnchorsSummary.protectedContinuityAnchorCount <= 0 || anchors.isEmpty()) {
            findings.add(finding("PROTECTED_CONTINUITY_ANCHORS_MISSING", "continuity", "CRITICAL", "$.storyboardPacketReview.protectedContinuityAnchorsSummary", "Story movement audit requires protected continuity anchors."))
            return 0
        }
        val draftText = collectDraftText(draft).joinToString("\n") { it.value }
        var covered = 0
        anchors.forEachIndexed { index, anchor ->
            if (anchorReferenced(anchor, draftText)) {
                covered += 1
            } else {
                findings.add(finding("PROTECTED_CONTINUITY_ANCHOR_NOT_COVERED", "continuity", "MAJOR", "$.storyboardPacketReview.protectedContinuityAnchorsSummary.protectedContinuityAnchors[$index]", "Storyboard draft must visibly carry every protected continuity anchor.", listOf(anchor)))
            }
        }
        return covered
    }

    private fun validateForbiddenDraftOutputFields(
        draft: StoryboardPacketDraft,
        findings: MutableList<StoryboardStoryMovementAuditFinding>,
    ): Int {
        var count = 0
        draft.frames.forEachIndexed { index, frame ->
            val path = "$.storyboardPacketDraft.frames[$index]"
            if (frame.imageOutputPath.isNotBlank()) {
                count += 1
                findings.add(finding("IMAGE_OUTPUT_PATH_FORBIDDEN", "safety_boundary", "CRITICAL", "$path.imageOutputPath", "Story movement audit is text-only and cannot carry image output paths.", listOf(frame.imageOutputPath)))
            }
            if (frame.videoOutputPath.isNotBlank()) {
                count += 1
                findings.add(finding("VIDEO_OUTPUT_PATH_FORBIDDEN", "safety_boundary", "CRITICAL", "$path.videoOutputPath", "Story movement audit is text-only and cannot carry video output paths.", listOf(frame.videoOutputPath)))
            }
            if (frame.generationApi.isNotBlank()) {
                count += 1
                findings.add(finding("GENERATION_API_FORBIDDEN", "safety_boundary", "CRITICAL", "$path.generationApi", "Story movement audit cannot carry generation API or provider handoff fields.", listOf(frame.generationApi)))
            }
            val approvalStatus = frame.assetApprovalStatus.trim()
            if (approvalStatus.isNotBlank() && !approvalStatus.equals("candidate_only", ignoreCase = true)) {
                count += 1
                findings.add(finding("ASSET_APPROVAL_STATUS_FORBIDDEN", "safety_boundary", "CRITICAL", "$path.assetApprovalStatus", "Story movement audit allows only blank or candidate_only asset approval status.", listOf(frame.assetApprovalStatus)))
            }
        }
        return count
    }

    private fun scanForbiddenClaims(
        draft: StoryboardPacketDraft,
        review: StoryboardPacketReviewPacket,
        preflight: StoryboardPacketHumanReviewPreflightReport,
        findings: MutableList<StoryboardStoryMovementAuditFinding>,
    ): Int {
        var count = 0
        val fields = mutableListOf<TextField>()
        fields += collectDraftText(draft)
        fields += TextField("$.storyboardPacketReview.reviewStatus", review.reviewStatus)
        fields += TextField("$.storyboardPacketReview.nextAllowedDownstreamStep", review.nextAllowedDownstreamStep)
        fields += TextField("$.storyboardHumanReviewPreflight.nextAllowedDownstreamStep", preflight.nextAllowedDownstreamStep)
        review.assetCandidateRequirements.forEachIndexed { index, item ->
            fields += TextField("$.storyboardPacketReview.assetCandidateRequirements[$index].requirement", item.requirement)
        }
        review.remainingResidualRisks.forEachIndexed { index, risk ->
            fields += TextField("$.storyboardPacketReview.remainingResidualRisks[$index]", risk)
        }
        fields.filter { it.value.isNotBlank() }.forEach { field ->
            forbiddenClaimPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings.add(finding("DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", "CRITICAL", field.path, "Story movement audit cannot carry approval, generated media, provider job, canon, production lock, or runtime scene claims.", listOf(field.value)))
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

    private fun frameMovementText(frame: StoryboardPacketFrameDraft): String =
        listOf(
            frame.sceneReference,
            frame.beatReference,
            frame.visualIntent,
            frame.cameraIntent,
            frame.reviewNotes,
            frame.mustShow.joinToString(" "),
            frame.continuityAnchors.joinToString(" "),
        ).joinToString(" ")

    private fun containsAny(text: String, markers: List<String>): Boolean =
        markers.any { text.contains(it, ignoreCase = true) }

    private fun anchorReferenced(anchor: String, text: String): Boolean {
        if (text.contains(anchor, ignoreCase = true)) return true
        val terms = significantTerms(anchor)
        if (terms.isEmpty()) return false
        val requiredMatches = if (terms.size == 1) 1 else maxOf(2, (terms.size * 0.6).toInt())
        return terms.count { text.contains(it, ignoreCase = true) } >= requiredMatches
    }

    private fun significantTerms(value: String): List<String> {
        val english = Regex("""[a-z0-9][a-z0-9'-]{2,}""", RegexOption.IGNORE_CASE)
            .findAll(value)
            .map { it.value.lowercase() }
            .filterNot { it in setOf("the", "and", "before", "after", "with") }
            .toList()
        val cjk = Regex("""[\u4E00-\u9FFF]{2,}""")
            .findAll(value)
            .flatMap { match -> match.value.windowed(2, 1, partialWindows = false) }
            .distinct()
            .toList()
        return (english + cjk).distinct()
    }

    private fun normalizePath(value: String): String = value.trim().replace('\\', '/')

    private fun finding(ruleId: String, category: String, severity: String, path: String, message: String, evidence: List<String> = emptyList(), suggestedFix: String = ""): StoryboardStoryMovementAuditFinding =
        StoryboardStoryMovementAuditFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private data class FrameEvidence(
        val movementEvidenceCount: Int = 0,
        val openingPressureEvidenceCount: Int = 0,
        val actionEvidenceCount: Int = 0,
        val consequenceOrChoiceEvidenceCount: Int = 0,
    )

    private data class TextField(val path: String, val value: String)
}
