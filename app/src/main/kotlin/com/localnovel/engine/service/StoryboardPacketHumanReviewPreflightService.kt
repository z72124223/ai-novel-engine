package com.localnovel.engine.service

import com.localnovel.engine.model.StoryboardPacketHumanReviewPreflightEvidence
import com.localnovel.engine.model.StoryboardPacketHumanReviewPreflightFinding
import com.localnovel.engine.model.StoryboardPacketHumanReviewPreflightReport
import com.localnovel.engine.model.StoryboardPacketReviewPacket

class StoryboardPacketHumanReviewPreflightService {
    private val requiredReviewStep = "storyboard_packet_human_review"
    private val nextAllowedDownstreamStep = "storyboard_packet_human_review_ready"
    private val knownVerdicts = setOf("PASS", "PASS_WITH_WARNINGS", "FAIL")
    private val sha256Regex = Regex("""[0-9a-f]{64}""")

    fun validate(
        storyboardPacketReview: StoryboardPacketReviewPacket,
        preflightId: String = "",
        storyboardPacketReviewFile: String = "",
    ): StoryboardPacketHumanReviewPreflightReport {
        val findings = mutableListOf<StoryboardPacketHumanReviewPreflightFinding>()
        val reviewVerdict = storyboardPacketReview.verdict.normalizedVerdict()
        validateReviewVerdict(reviewVerdict, storyboardPacketReview.verdict, findings)
        if (storyboardPacketReview.reviewStatus != "REVIEW_READY") {
            findings.add(
                finding(
                    ruleId = "STORYBOARD_PACKET_REVIEW_STATUS_NOT_READY",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.storyboardPacketReview.reviewStatus",
                    message = "Human review preflight requires reviewStatus=REVIEW_READY.",
                    evidence = listOf(storyboardPacketReview.reviewStatus),
                    suggestedFix = "Create a review-ready storyboard packet before human review preflight.",
                ),
            )
        }
        if (storyboardPacketReview.nextAllowedDownstreamStep != requiredReviewStep) {
            findings.add(
                finding(
                    ruleId = "STORYBOARD_PACKET_REVIEW_NEXT_STEP_NOT_HUMAN_REVIEW",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.storyboardPacketReview.nextAllowedDownstreamStep",
                    message = "Human review preflight requires nextAllowedDownstreamStep=storyboard_packet_human_review.",
                    evidence = listOf(storyboardPacketReview.nextAllowedDownstreamStep),
                    suggestedFix = "Use the storyboard packet review packet that passed into human review.",
                ),
            )
        }

        val sourceFieldCount = validateSourceFields(storyboardPacketReview, storyboardPacketReviewFile, findings)
        validateChecklist(storyboardPacketReview, findings)
        validateFrames(storyboardPacketReview, findings)
        validateContinuityAndRisks(storyboardPacketReview, findings)
        val forbiddenClaimCount = validateForbiddenClaims(storyboardPacketReview, findings)

        val verdict = when {
            findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
            findings.any { it.severity == "WARNING" } -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }
        val blockedReason = if (verdict == "FAIL") {
            findings
                .filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }
                .joinToString("; ") { it.ruleId }
        } else {
            ""
        }
        val resolvedPreflightId = preflightId.ifBlank {
            storyboardPacketReview.reviewId.takeIf { it.isNotBlank() }?.let { "$it-human-review-preflight" }.orEmpty()
        }

        return StoryboardPacketHumanReviewPreflightReport(
            verdict = verdict,
            preflightId = resolvedPreflightId,
            storyboardPacketReviewFile = storyboardPacketReviewFile,
            storyboardPacketReviewId = storyboardPacketReview.reviewId,
            storyboardPacketDraftFile = storyboardPacketReview.storyboardPacketDraftFile,
            storyboardPacketDraftReportFile = storyboardPacketReview.storyboardPacketDraftReportFile,
            storyboardPacketDraftId = storyboardPacketReview.storyboardPacketDraftId,
            storyboardPlanningBriefId = storyboardPacketReview.storyboardPlanningBriefId,
            acceptedScriptPacketId = storyboardPacketReview.acceptedScriptPacketId,
            scriptPacketDraftFile = storyboardPacketReview.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = storyboardPacketReview.scriptPacketDraftContentSha256,
            reviewStatus = storyboardPacketReview.reviewStatus,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = StoryboardPacketHumanReviewPreflightEvidence(
                reviewPacketVerdict = storyboardPacketReview.verdict,
                reviewStatus = storyboardPacketReview.reviewStatus,
                reviewNextAllowedDownstreamStep = storyboardPacketReview.nextAllowedDownstreamStep,
                sourceFieldCount = sourceFieldCount,
                frameCount = storyboardPacketReview.frameCount,
                frameIdCount = storyboardPacketReview.frameIds.count { it.isNotBlank() },
                reviewChecklistCount = storyboardPacketReview.reviewChecklist.size,
                protectedContinuityAnchorCount = storyboardPacketReview
                    .protectedContinuityAnchorsSummary
                    .protectedContinuityAnchorCount,
                residualRiskCount = storyboardPacketReview.remainingResidualRisks.count { it.isNotBlank() },
                forbiddenClaimCount = forbiddenClaimCount,
            ),
        )
    }

    private fun validateSourceFields(
        review: StoryboardPacketReviewPacket,
        reviewFile: String,
        findings: MutableList<StoryboardPacketHumanReviewPreflightFinding>,
    ): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) {
                findings.add(
                    finding(
                        ruleId = ruleId,
                        category = "source_linkage",
                        severity = "CRITICAL",
                        path = path,
                        message = "Human review preflight requires nonblank $label.",
                        suggestedFix = "Use the complete storyboard packet review JSON produced by the review gate.",
                    ),
                )
            } else {
                count += 1
            }
        }
        requireNonBlank(reviewFile, "$.inputs.storyboardPacketReviewFile", "REVIEW_PACKET_FILE_MISSING", "review packet file")
        requireNonBlank(review.reviewId, "$.storyboardPacketReview.reviewId", "REVIEW_ID_MISSING", "review id")
        requireNonBlank(review.storyboardPacketDraftFile, "$.storyboardPacketReview.storyboardPacketDraftFile", "STORYBOARD_PACKET_DRAFT_FILE_MISSING", "source draft file")
        requireNonBlank(review.storyboardPacketDraftReportFile, "$.storyboardPacketReview.storyboardPacketDraftReportFile", "STORYBOARD_PACKET_DRAFT_REPORT_FILE_MISSING", "source validation report file")
        requireNonBlank(review.storyboardPacketDraftId, "$.storyboardPacketReview.storyboardPacketDraftId", "STORYBOARD_PACKET_DRAFT_ID_MISSING", "draft id")
        requireNonBlank(review.storyboardPlanningBriefId, "$.storyboardPacketReview.storyboardPlanningBriefId", "STORYBOARD_PLANNING_BRIEF_ID_MISSING", "planning brief id")
        requireNonBlank(review.acceptedScriptPacketId, "$.storyboardPacketReview.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(review.scriptPacketDraftFile, "$.storyboardPacketReview.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(review.scriptPacketDraftContentSha256, "$.storyboardPacketReview.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        requireNonBlank(
            review.sourceReports.storyboardPacketDraftValidationReportFile,
            "$.storyboardPacketReview.sourceReports.storyboardPacketDraftValidationReportFile",
            "SOURCE_DRAFT_VALIDATION_REPORT_FILE_MISSING",
            "source draft validation report file",
        )
        requireNonBlank(
            review.sourceReports.storyboardPacketDraftValidationVerdict,
            "$.storyboardPacketReview.sourceReports.storyboardPacketDraftValidationVerdict",
            "SOURCE_DRAFT_VALIDATION_REPORT_VERDICT_MISSING",
            "source draft validation report verdict",
        )
        val sourceValidationVerdict = review.sourceReports.storyboardPacketDraftValidationVerdict.normalizedVerdict()
        if (sourceValidationVerdict.isNotBlank() && sourceValidationVerdict !in knownVerdicts) {
            findings.add(
                finding(
                    ruleId = "SOURCE_DRAFT_VALIDATION_REPORT_VERDICT_UNKNOWN",
                    category = "source_linkage",
                    severity = "MAJOR",
                    path = "$.storyboardPacketReview.sourceReports.storyboardPacketDraftValidationVerdict",
                    message = "Human review preflight requires a known source draft validation verdict.",
                    evidence = listOf(review.sourceReports.storyboardPacketDraftValidationVerdict),
                    suggestedFix = "Use a source draft validation report verdict of PASS, PASS_WITH_WARNINGS, or FAIL.",
                ),
            )
        } else if (sourceValidationVerdict.isNotBlank() && sourceValidationVerdict != "PASS") {
            findings.add(
                finding(
                    ruleId = "SOURCE_DRAFT_VALIDATION_REPORT_NOT_PASS",
                    category = "source_linkage",
                    severity = "CRITICAL",
                    path = "$.storyboardPacketReview.sourceReports.storyboardPacketDraftValidationVerdict",
                    message = "Human review preflight requires source draft validation report PASS.",
                    evidence = listOf(review.sourceReports.storyboardPacketDraftValidationVerdict),
                    suggestedFix = "Fix and rerun storyboard packet draft validation before human review preflight.",
                ),
            )
        }
        requireNonBlank(
            review.sourceReports.storyboardPacketDraftValidationNextStep,
            "$.storyboardPacketReview.sourceReports.storyboardPacketDraftValidationNextStep",
            "SOURCE_DRAFT_VALIDATION_NEXT_STEP_MISSING",
            "source draft validation next step",
        )
        if (review.sourceReports.storyboardPacketDraftValidationNextStep.isNotBlank() &&
            review.sourceReports.storyboardPacketDraftValidationNextStep != "storyboard_packet_review"
        ) {
            findings.add(
                finding(
                    ruleId = "SOURCE_DRAFT_VALIDATION_NEXT_STEP_MISMATCH",
                    category = "source_linkage",
                    severity = "CRITICAL",
                    path = "$.storyboardPacketReview.sourceReports.storyboardPacketDraftValidationNextStep",
                    message = "Human review preflight requires source draft validation next step storyboard_packet_review.",
                    evidence = listOf(review.sourceReports.storyboardPacketDraftValidationNextStep),
                    suggestedFix = "Use the review packet produced from a draft validation report that allowed storyboard_packet_review.",
                ),
            )
        }
        if (review.scriptPacketDraftContentSha256.isNotBlank() &&
            !review.scriptPacketDraftContentSha256.trim().matches(sha256Regex)
        ) {
            findings.add(
                finding(
                    ruleId = "SCRIPT_PACKET_DRAFT_HASH_INVALID",
                    category = "source_linkage",
                    severity = "MAJOR",
                    path = "$.storyboardPacketReview.scriptPacketDraftContentSha256",
                    message = "Human review preflight requires a lowercase SHA-256 source hash.",
                    evidence = listOf(review.scriptPacketDraftContentSha256),
                    suggestedFix = "Use the exact 64-character lowercase SHA-256 from upstream review packet.",
                ),
            )
        }
        return count
    }

    private fun validateChecklist(
        review: StoryboardPacketReviewPacket,
        findings: MutableList<StoryboardPacketHumanReviewPreflightFinding>,
    ) {
        val categories = review.reviewChecklist
            .map { it.category.trim().lowercase() }
            .filter { it.isNotBlank() }
            .toSet()
        requiredChecklistCategories.forEach { required ->
            if (required !in categories) {
                findings.add(
                    finding(
                        ruleId = "REVIEW_CHECKLIST_REQUIRED_CATEGORY_MISSING",
                        category = "review_checklist",
                        severity = "MAJOR",
                        path = "$.storyboardPacketReview.reviewChecklist",
                        message = "Human review preflight requires checklist category $required.",
                        evidence = listOf("present=${categories.joinToString(",")}"),
                        suggestedFix = "Regenerate the review packet with the required human review checklist coverage.",
                    ),
                )
            }
        }
    }

    private fun validateFrames(
        review: StoryboardPacketReviewPacket,
        findings: MutableList<StoryboardPacketHumanReviewPreflightFinding>,
    ) {
        if (review.frameCount <= 0) {
            findings.add(
                finding(
                    ruleId = "FRAME_COUNT_MISSING",
                    category = "frames",
                    severity = "CRITICAL",
                    path = "$.storyboardPacketReview.frameCount",
                    message = "Human review preflight requires frameCount greater than zero.",
                    suggestedFix = "Use a review packet created from a storyboard packet draft with text frames.",
                ),
            )
        }
        val nonblankFrameIds = review.frameIds.filter { it.isNotBlank() }
        if (nonblankFrameIds.size != review.frameIds.size || nonblankFrameIds.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "FRAME_IDS_MISSING",
                    category = "frames",
                    severity = "MAJOR",
                    path = "$.storyboardPacketReview.frameIds",
                    message = "Human review preflight requires nonblank frame ids.",
                    suggestedFix = "Regenerate the review packet with every frame id preserved.",
                ),
            )
        }
        if (nonblankFrameIds.distinct().size != nonblankFrameIds.size) {
            findings.add(
                finding(
                    ruleId = "FRAME_IDS_DUPLICATE",
                    category = "frames",
                    severity = "MAJOR",
                    path = "$.storyboardPacketReview.frameIds",
                    message = "Human review preflight requires unique frame ids.",
                    evidence = nonblankFrameIds.groupingBy { it }.eachCount().filterValues { it > 1 }.keys.toList(),
                    suggestedFix = "Assign unique frame ids before human review preflight.",
                ),
            )
        }
        if (review.frameCount > 0 && nonblankFrameIds.size != review.frameCount) {
            findings.add(
                finding(
                    ruleId = "FRAME_ID_COUNT_MISMATCH",
                    category = "frames",
                    severity = "MAJOR",
                    path = "$.storyboardPacketReview.frameIds",
                    message = "Human review preflight requires frameIds count to match frameCount.",
                    evidence = listOf("frameCount=${review.frameCount}", "frameIds=${nonblankFrameIds.size}"),
                    suggestedFix = "Regenerate the review packet so frame count and ids match.",
                ),
            )
        }
    }

    private fun validateContinuityAndRisks(
        review: StoryboardPacketReviewPacket,
        findings: MutableList<StoryboardPacketHumanReviewPreflightFinding>,
    ) {
        if (review.protectedContinuityAnchorsSummary.protectedContinuityAnchorCount <= 0 ||
            review.protectedContinuityAnchorsSummary.protectedContinuityAnchors.none { it.isNotBlank() }
        ) {
            findings.add(
                finding(
                    ruleId = "PROTECTED_CONTINUITY_ANCHORS_MISSING",
                    category = "continuity",
                    severity = "MAJOR",
                    path = "$.storyboardPacketReview.protectedContinuityAnchorsSummary",
                    message = "Human review preflight requires protected continuity anchors summary.",
                    suggestedFix = "Regenerate the review packet with protected continuity anchors preserved.",
                ),
            )
        }
        if (review.remainingResidualRisks.none { it.isNotBlank() }) {
            findings.add(
                finding(
                    ruleId = "RESIDUAL_RISKS_MISSING",
                    category = "risk",
                    severity = "MAJOR",
                    path = "$.storyboardPacketReview.remainingResidualRisks",
                    message = "Human review preflight requires remaining residual risks.",
                    suggestedFix = "Regenerate the review packet with residual risks carried forward.",
                ),
            )
        }
    }

    private fun validateForbiddenClaims(
        review: StoryboardPacketReviewPacket,
        findings: MutableList<StoryboardPacketHumanReviewPreflightFinding>,
    ): Int {
        var count = 0
        collectTextFields(review).forEach { field ->
            forbiddenClaimPatterns.forEach { pattern ->
                val hit = pattern.find(field.value) ?: return@forEach
                count += 1
                findings.add(
                    finding(
                        ruleId = "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN",
                        category = "downstream_boundary",
                        severity = "MAJOR",
                        path = field.path,
                        message = "Human review preflight must not carry approval, lock, canon, generated media, or runtime completion claims.",
                        evidence = listOf(hit.value.take(160)),
                        suggestedFix = "Keep the packet in review-ready state and move decisions to a later human review gate.",
                    ),
                )
            }
        }
        return count
    }

    private fun collectTextFields(review: StoryboardPacketReviewPacket): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) {
            if (value.isNotBlank()) fields.add(TextField(path, value))
        }
        add("$.storyboardPacketReview.reviewId", review.reviewId)
        add("$.storyboardPacketReview.reviewStatus", review.reviewStatus)
        add("$.storyboardPacketReview.storyboardPacketDraftFile", review.storyboardPacketDraftFile)
        add("$.storyboardPacketReview.storyboardPacketDraftReportFile", review.storyboardPacketDraftReportFile)
        add("$.storyboardPacketReview.storyboardPacketDraftId", review.storyboardPacketDraftId)
        add("$.storyboardPacketReview.storyboardPlanningBriefId", review.storyboardPlanningBriefId)
        add("$.storyboardPacketReview.acceptedScriptPacketId", review.acceptedScriptPacketId)
        add("$.storyboardPacketReview.scriptPacketDraftFile", review.scriptPacketDraftFile)
        review.frameIds.forEachIndexed { index, value -> add("$.storyboardPacketReview.frameIds[$index]", value) }
        review.protectedContinuityAnchorsSummary.protectedContinuityAnchors.forEachIndexed { index, value ->
            add("$.storyboardPacketReview.protectedContinuityAnchorsSummary.protectedContinuityAnchors[$index]", value)
        }
        review.assetCandidateRequirements.forEachIndexed { index, requirement ->
            val path = "$.storyboardPacketReview.assetCandidateRequirements[$index]"
            add("$path.candidateId", requirement.candidateId)
            add("$path.requirement", requirement.requirement)
            requirement.sourceFrameIds.forEachIndexed { frameIndex, value -> add("$path.sourceFrameIds[$frameIndex]", value) }
        }
        review.remainingResidualRisks.forEachIndexed { index, value ->
            add("$.storyboardPacketReview.remainingResidualRisks[$index]", value)
        }
        review.reviewChecklist.forEachIndexed { index, item ->
            val path = "$.storyboardPacketReview.reviewChecklist[$index]"
            add("$path.itemId", item.itemId)
            add("$path.category", item.category)
            add("$path.requiredCheck", item.requiredCheck)
            add("$path.status", item.status)
        }
        add("$.storyboardPacketReview.blockedReason", review.blockedReason)
        review.findings.forEachIndexed { index, finding ->
            add("$.storyboardPacketReview.findings[$index].message", finding.message)
            finding.evidence.forEachIndexed { evidenceIndex, value ->
                add("$.storyboardPacketReview.findings[$index].evidence[$evidenceIndex]", value)
            }
            add("$.storyboardPacketReview.findings[$index].suggestedFix", finding.suggestedFix)
        }
        return fields
    }

    private fun validateReviewVerdict(
        verdict: String,
        originalVerdict: String,
        findings: MutableList<StoryboardPacketHumanReviewPreflightFinding>,
    ) {
        when {
            verdict.isBlank() -> findings.add(
                finding("STORYBOARD_PACKET_REVIEW_VERDICT_MISSING", "upstream", "MAJOR", "$.storyboardPacketReview.verdict", "Storyboard packet review has no verdict.", suggestedFix = "Regenerate the storyboard packet review packet."),
            )
            verdict !in knownVerdicts -> findings.add(
                finding("STORYBOARD_PACKET_REVIEW_VERDICT_UNKNOWN", "upstream", "MAJOR", "$.storyboardPacketReview.verdict", "Storyboard packet review has an unknown verdict.", evidence = listOf(originalVerdict), suggestedFix = "Use a review packet with verdict PASS, PASS_WITH_WARNINGS, or FAIL."),
            )
            verdict != "PASS" -> findings.add(
                finding("STORYBOARD_PACKET_REVIEW_NOT_PASS", "upstream", "CRITICAL", "$.storyboardPacketReview.verdict", "Human review preflight requires storyboard packet review PASS.", evidence = listOf(originalVerdict), suggestedFix = "Fix and rerun storyboard packet review before human review preflight."),
            )
        }
    }

    private fun String.normalizedVerdict(): String =
        trim().uppercase()

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): StoryboardPacketHumanReviewPreflightFinding =
        StoryboardPacketHumanReviewPreflightFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            path = path,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
        )

    private data class TextField(
        val path: String,
        val value: String,
    )

    private companion object {
        val requiredChecklistCategories = setOf(
            "scene_lock",
            "continuity",
            "knowledge_boundary",
            "assets",
            "downstream_boundary",
        )
        val forbiddenClaimPatterns = listOf(
            Regex("""(?i)\bstoryboard\s+(generated|created|complete|completed|approved|final)\b"""),
            Regex("""(?i)\b(generated|created|completed|approved|final)\s+storyboard\b"""),
            Regex("""(?i)\bimage\s+(generated|created|complete|completed|final)\b"""),
            Regex("""(?i)\b(generated|created|completed|final)\s+image\b"""),
            Regex("""(?i)\bapproved\s+assets?\b"""),
            Regex("""(?i)\bassets?\s+(?:are\s+)?approved\b"""),
            Regex("""(?i)\bassets?\s+approval\s+(?:is\s+)?(complete|completed|ready|done)\b"""),
            Regex("""(?i)\basset\s+approval\s+complete\b"""),
            Regex("""(?i)\bproduction\s+(locked|lock|ready|final)\b"""),
            Regex("""(?i)\bfinal\s+video\b"""),
            Regex("""(?i)\bvideo\s+(generated|created|complete|completed|final)\b"""),
            Regex("""(?i)\buser\s+(approved|approval|accepted)\b"""),
            Regex("""(?i)\bruntime\s+scene\b"""),
            Regex("""(?i)\bsetting\s+pack\s+canon\b"""),
            Regex("""(?i)\bcanon\s+(applied|approved|complete|completed)\b"""),
            Regex("""(?i)\bis\s+canon\b"""),
            Regex("""(?i)\bcanonical\b"""),
            Regex("\u5df2\u7522\u751f\u5206\u93e1"),
            Regex("\u5df2\u751f\u6210\u5206\u93e1"),
            Regex("\u5df2\u6838\u51c6\u7d20\u6750"),
            Regex("\u6700\u7d42\u5f71\u7247"),
            Regex("\u88fd\u4f5c\u9396\u5b9a"),
            Regex("\u4f7f\u7528\u8005\u5df2\u6279\u51c6"),
            Regex("\u57f7\u884c\u671f\u5834\u666f"),
            Regex("\u8a2d\u5b9a\u5305\u6b63\u5178"),
        )
    }
}
