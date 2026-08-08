package com.localnovel.engine.service

import com.localnovel.engine.model.AssetCandidatePreflightEvidence
import com.localnovel.engine.model.AssetCandidatePreflightFinding
import com.localnovel.engine.model.AssetCandidatePreflightReport
import com.localnovel.engine.model.StoryboardPacketHumanReviewDecisionIntakeReport
import com.localnovel.engine.model.StoryboardPacketReviewAssetRequirement
import com.localnovel.engine.model.StoryboardPacketReviewPacket

class AssetCandidatePreflightService {
    private val requiredDecisionStatus = "ALLOW_ASSET_CANDIDATE_PREFLIGHT"
    private val requiredDecisionNextStep = "asset_candidate_preflight"
    private val requiredReviewNextStep = "storyboard_packet_human_review"
    private val nextAllowedDownstreamStep = "asset_candidate_brief"
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
    private val forbiddenCompletionClaimPatterns = listOf(
        Regex("""(?i)\bimage\s+generated\b"""),
        Regex("""(?i)\bvideo\s+generated\b"""),
        Regex("""(?i)\basset\s+approved\b"""),
        Regex("""(?i)\bassets\s+approved\b"""),
        Regex("""(?i)\basset\s+approval\s+complete\b"""),
        Regex("""(?i)\bproduction\s+locked\b"""),
        Regex("""(?i)\bcanon\s+applied\b"""),
        Regex("""(?i)\bruntime\s+scene\s+created\b"""),
        Regex("\u5716\u7247\u5df2\u751f\u6210|\u5f71\u7247\u5df2\u751f\u6210|\u7d20\u6750\u5df2\u6838\u51c6|\u7d20\u6750\u6838\u51c6\u5b8c\u6210|\u88fd\u4f5c\u9396\u5b9a|\u6b63\u5178\u5df2\u5957\u7528|\u57f7\u884c\u671f\u5834\u666f\u5df2\u5efa\u7acb"),
    )
    private val forbiddenCandidateStatusPatterns = listOf(
        Regex("""(?i)\bapproved\b"""),
        Regex("""(?i)\blocked\b"""),
        Regex("""(?i)\bfinal\b"""),
        Regex("""(?i)\bproduction[-_\s]?ready\b"""),
        Regex("""(?i)\bapproved_reference\b"""),
        Regex("""(?i)\bproduction_locked\b"""),
        Regex("\u6838\u51c6|\u6279\u51c6|\u9396\u5b9a"),
    )

    fun validate(
        humanReviewDecisionIntake: StoryboardPacketHumanReviewDecisionIntakeReport,
        storyboardPacketReview: StoryboardPacketReviewPacket,
        preflightId: String = "",
        humanReviewDecisionIntakeFile: String = "",
        storyboardPacketReviewFile: String = "",
    ): AssetCandidatePreflightReport {
        val findings = mutableListOf<AssetCandidatePreflightFinding>()
        validateDecisionIntake(humanReviewDecisionIntake, findings)
        validateReviewPacketState(storyboardPacketReview, findings)
        val sourceBindingFieldCount = validateSourceBinding(
            intake = humanReviewDecisionIntake,
            review = storyboardPacketReview,
            intakeFile = humanReviewDecisionIntakeFile,
            reviewFile = storyboardPacketReviewFile,
            findings = findings,
        )
        validateAssetCandidateRequirements(storyboardPacketReview.assetCandidateRequirements, findings)
        val forbiddenStatusCount = validateCandidateOnlyText(storyboardPacketReview.assetCandidateRequirements, findings)
        val forbiddenClaimCount = validateForbiddenClaims(humanReviewDecisionIntake, storyboardPacketReview, findings)

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
            humanReviewDecisionIntake.decisionId
                .takeIf { it.isNotBlank() }
                ?.let { "$it-asset-candidate-preflight" }
                .orEmpty()
        }

        return AssetCandidatePreflightReport(
            verdict = verdict,
            preflightId = resolvedPreflightId,
            humanReviewDecisionIntakeFile = humanReviewDecisionIntakeFile,
            storyboardPacketReviewFile = storyboardPacketReviewFile,
            decisionId = humanReviewDecisionIntake.decisionId,
            humanReviewPreflightId = humanReviewDecisionIntake.preflightId,
            storyboardPacketReviewId = humanReviewDecisionIntake.storyboardPacketReviewId,
            acceptedScriptPacketId = storyboardPacketReview.acceptedScriptPacketId,
            scriptPacketDraftFile = storyboardPacketReview.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = storyboardPacketReview.scriptPacketDraftContentSha256,
            assetCandidateRequirements = if (verdict == "FAIL") emptyList() else storyboardPacketReview.assetCandidateRequirements,
            downstreamBoundary = "asset_candidate_preparation_only",
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = AssetCandidatePreflightEvidence(
                decisionIntakeVerdict = humanReviewDecisionIntake.verdict,
                decisionAllowedDownstream = humanReviewDecisionIntake.decisionAllowedDownstream,
                decisionStatus = humanReviewDecisionIntake.decisionStatus,
                decisionNextAllowedDownstreamStep = humanReviewDecisionIntake.nextAllowedDownstreamStep,
                sourceBindingFieldCount = sourceBindingFieldCount,
                assetCandidateRequirementCount = storyboardPacketReview.assetCandidateRequirements.size,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
            ),
        )
    }

    private fun validateReviewPacketState(
        review: StoryboardPacketReviewPacket,
        findings: MutableList<AssetCandidatePreflightFinding>,
    ) {
        if (review.verdict.trim().uppercase() != "PASS") {
            findings.add(
                finding(
                    ruleId = "STORYBOARD_PACKET_REVIEW_NOT_PASS",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.storyboardPacketReview.verdict",
                    message = "Asset candidate preflight requires a PASS storyboard packet review.",
                    evidence = listOf(review.verdict),
                    suggestedFix = "Use the review-ready storyboard packet review JSON that passed the review gate.",
                ),
            )
        }
        if (review.reviewStatus != "REVIEW_READY") {
            findings.add(
                finding(
                    ruleId = "STORYBOARD_PACKET_REVIEW_NOT_READY",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.storyboardPacketReview.reviewStatus",
                    message = "Asset candidate preflight requires reviewStatus=REVIEW_READY.",
                    evidence = listOf(review.reviewStatus),
                    suggestedFix = "Use the review-ready storyboard packet review JSON.",
                ),
            )
        }
        if (review.nextAllowedDownstreamStep != requiredReviewNextStep) {
            findings.add(
                finding(
                    ruleId = "STORYBOARD_PACKET_REVIEW_NEXT_STEP_MISMATCH",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.storyboardPacketReview.nextAllowedDownstreamStep",
                    message = "Asset candidate preflight requires the review packet next step to remain storyboard_packet_human_review.",
                    evidence = listOf(review.nextAllowedDownstreamStep),
                    suggestedFix = "Use the storyboard packet review JSON that fed the human review preflight.",
                ),
            )
        }
    }

    private fun validateDecisionIntake(
        intake: StoryboardPacketHumanReviewDecisionIntakeReport,
        findings: MutableList<AssetCandidatePreflightFinding>,
    ) {
        if (intake.verdict.trim().uppercase() != "PASS") {
            findings.add(
                finding(
                    ruleId = "HUMAN_REVIEW_DECISION_INTAKE_NOT_PASS",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.humanReviewDecisionIntake.verdict",
                    message = "Asset candidate preflight requires a PASS human review decision intake report.",
                    evidence = listOf(intake.verdict),
                    suggestedFix = "Use the PASS decision intake report created after human review.",
                ),
            )
        }
        if (!intake.decisionAllowedDownstream) {
            findings.add(
                finding(
                    ruleId = "DECISION_NOT_ALLOWED_DOWNSTREAM",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.humanReviewDecisionIntake.decisionAllowedDownstream",
                    message = "Asset candidate preflight requires decisionAllowedDownstream=true.",
                    evidence = listOf(intake.decisionAllowedDownstream.toString()),
                    suggestedFix = "Use a human decision intake report that explicitly allows asset candidate preflight.",
                ),
            )
        }
        if (intake.decisionStatus != requiredDecisionStatus) {
            findings.add(
                finding(
                    ruleId = "DECISION_STATUS_NOT_ASSET_CANDIDATE_PREFLIGHT",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.humanReviewDecisionIntake.decisionStatus",
                    message = "Asset candidate preflight requires decisionStatus=ALLOW_ASSET_CANDIDATE_PREFLIGHT.",
                    evidence = listOf(intake.decisionStatus),
                    suggestedFix = "Use the human decision that only allows asset candidate preflight.",
                ),
            )
        }
        if (intake.nextAllowedDownstreamStep != requiredDecisionNextStep) {
            findings.add(
                finding(
                    ruleId = "DECISION_NEXT_STEP_NOT_ASSET_CANDIDATE_PREFLIGHT",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.humanReviewDecisionIntake.nextAllowedDownstreamStep",
                    message = "Asset candidate preflight requires nextAllowedDownstreamStep=asset_candidate_preflight.",
                    evidence = listOf(intake.nextAllowedDownstreamStep),
                    suggestedFix = "Use the decision intake report whose next step is asset_candidate_preflight.",
                ),
            )
        }
    }

    private fun validateSourceBinding(
        intake: StoryboardPacketHumanReviewDecisionIntakeReport,
        review: StoryboardPacketReviewPacket,
        intakeFile: String,
        reviewFile: String,
        findings: MutableList<AssetCandidatePreflightFinding>,
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
                        message = "Asset candidate preflight requires nonblank $label.",
                        suggestedFix = "Use the matching decision intake report and storyboard packet review JSON.",
                    ),
                )
            } else {
                count += 1
            }
        }
        fun requireMatch(
            intakeValue: String,
            reviewValue: String,
            intakePath: String,
            reviewPath: String,
            missingRuleId: String,
            mismatchRuleId: String,
            label: String,
            normalize: (String) -> String = { it.trim() },
        ) {
            requireNonBlank(intakeValue, intakePath, missingRuleId, "$label in decision intake")
            requireNonBlank(reviewValue, reviewPath, "${missingRuleId}_REVIEW", "$label in storyboard packet review")
            val normalizedIntake = normalize(intakeValue)
            val normalizedReview = normalize(reviewValue)
            if (normalizedIntake.isNotBlank() && normalizedReview.isNotBlank() && normalizedIntake != normalizedReview) {
                findings.add(
                    finding(
                        ruleId = mismatchRuleId,
                        category = "source_linkage",
                        severity = "CRITICAL",
                        path = reviewPath,
                        message = "Asset candidate preflight source binding mismatch for $label.",
                        evidence = listOf("decisionIntake=$intakeValue", "storyboardReview=$reviewValue"),
                        suggestedFix = "Use the storyboard packet review JSON referenced by the decision intake report.",
                    ),
                )
            }
        }

        requireNonBlank(intakeFile, "$.inputs.humanReviewDecisionIntakeFile", "DECISION_INTAKE_FILE_MISSING", "decision intake file")
        requireNonBlank(reviewFile, "$.inputs.storyboardPacketReviewFile", "STORYBOARD_PACKET_REVIEW_FILE_MISSING", "storyboard packet review file")
        requireNonBlank(intake.decisionId, "$.humanReviewDecisionIntake.decisionId", "DECISION_ID_MISSING", "decision id")
        requireNonBlank(intake.preflightId, "$.humanReviewDecisionIntake.preflightId", "HUMAN_REVIEW_PREFLIGHT_ID_MISSING", "human review preflight id")
        requireNonBlank(intake.humanReviewPreflightFile, "$.humanReviewDecisionIntake.humanReviewPreflightFile", "HUMAN_REVIEW_PREFLIGHT_FILE_MISSING", "human review preflight file")
        requireMatch(
            intakeValue = intake.storyboardPacketReviewFile,
            reviewValue = reviewFile,
            intakePath = "$.humanReviewDecisionIntake.storyboardPacketReviewFile",
            reviewPath = "$.inputs.storyboardPacketReviewFile",
            missingRuleId = "DECISION_INTAKE_REVIEW_FILE_MISSING",
            mismatchRuleId = "DECISION_INTAKE_REVIEW_FILE_MISMATCH",
            label = "storyboard packet review file",
            normalize = ::normalizePath,
        )
        requireMatch(
            intakeValue = intake.storyboardPacketReviewId,
            reviewValue = review.reviewId,
            intakePath = "$.humanReviewDecisionIntake.storyboardPacketReviewId",
            reviewPath = "$.storyboardPacketReview.reviewId",
            missingRuleId = "STORYBOARD_PACKET_REVIEW_ID_MISSING",
            mismatchRuleId = "STORYBOARD_PACKET_REVIEW_ID_MISMATCH",
            label = "storyboard packet review id",
        )
        requireMatch(
            intakeValue = intake.storyboardPacketDraftFile,
            reviewValue = review.storyboardPacketDraftFile,
            intakePath = "$.humanReviewDecisionIntake.storyboardPacketDraftFile",
            reviewPath = "$.storyboardPacketReview.storyboardPacketDraftFile",
            missingRuleId = "STORYBOARD_PACKET_DRAFT_FILE_MISSING",
            mismatchRuleId = "STORYBOARD_PACKET_DRAFT_FILE_MISMATCH",
            label = "storyboard packet draft file",
            normalize = ::normalizePath,
        )
        requireMatch(
            intakeValue = intake.storyboardPacketDraftReportFile,
            reviewValue = review.storyboardPacketDraftReportFile,
            intakePath = "$.humanReviewDecisionIntake.storyboardPacketDraftReportFile",
            reviewPath = "$.storyboardPacketReview.storyboardPacketDraftReportFile",
            missingRuleId = "STORYBOARD_PACKET_DRAFT_REPORT_FILE_MISSING",
            mismatchRuleId = "STORYBOARD_PACKET_DRAFT_REPORT_FILE_MISMATCH",
            label = "storyboard packet draft report file",
            normalize = ::normalizePath,
        )
        requireMatch(
            intakeValue = intake.storyboardPacketDraftId,
            reviewValue = review.storyboardPacketDraftId,
            intakePath = "$.humanReviewDecisionIntake.storyboardPacketDraftId",
            reviewPath = "$.storyboardPacketReview.storyboardPacketDraftId",
            missingRuleId = "STORYBOARD_PACKET_DRAFT_ID_MISSING",
            mismatchRuleId = "STORYBOARD_PACKET_DRAFT_ID_MISMATCH",
            label = "storyboard packet draft id",
        )
        requireMatch(
            intakeValue = intake.storyboardPlanningBriefId,
            reviewValue = review.storyboardPlanningBriefId,
            intakePath = "$.humanReviewDecisionIntake.storyboardPlanningBriefId",
            reviewPath = "$.storyboardPacketReview.storyboardPlanningBriefId",
            missingRuleId = "STORYBOARD_PLANNING_BRIEF_ID_MISSING",
            mismatchRuleId = "STORYBOARD_PLANNING_BRIEF_ID_MISMATCH",
            label = "storyboard planning brief id",
        )
        requireMatch(
            intakeValue = intake.acceptedScriptPacketId,
            reviewValue = review.acceptedScriptPacketId,
            intakePath = "$.humanReviewDecisionIntake.acceptedScriptPacketId",
            reviewPath = "$.storyboardPacketReview.acceptedScriptPacketId",
            missingRuleId = "ACCEPTED_SCRIPT_PACKET_ID_MISSING",
            mismatchRuleId = "ACCEPTED_SCRIPT_PACKET_ID_MISMATCH",
            label = "accepted script packet id",
        )
        requireMatch(
            intakeValue = intake.scriptPacketDraftFile,
            reviewValue = review.scriptPacketDraftFile,
            intakePath = "$.humanReviewDecisionIntake.scriptPacketDraftFile",
            reviewPath = "$.storyboardPacketReview.scriptPacketDraftFile",
            missingRuleId = "SCRIPT_PACKET_DRAFT_FILE_MISSING",
            mismatchRuleId = "SCRIPT_PACKET_DRAFT_FILE_MISMATCH",
            label = "script packet draft file",
            normalize = ::normalizePath,
        )
        requireMatch(
            intakeValue = intake.scriptPacketDraftContentSha256,
            reviewValue = review.scriptPacketDraftContentSha256,
            intakePath = "$.humanReviewDecisionIntake.scriptPacketDraftContentSha256",
            reviewPath = "$.storyboardPacketReview.scriptPacketDraftContentSha256",
            missingRuleId = "SCRIPT_PACKET_DRAFT_HASH_MISSING",
            mismatchRuleId = "SCRIPT_PACKET_DRAFT_HASH_MISMATCH",
            label = "script packet draft SHA-256",
            normalize = { it.trim().lowercase() },
        )
        if (review.scriptPacketDraftContentSha256.isNotBlank() &&
            !sha256Regex.matches(review.scriptPacketDraftContentSha256.trim().lowercase())
        ) {
            findings.add(
                finding(
                    ruleId = "SCRIPT_PACKET_DRAFT_HASH_INVALID",
                    category = "source_linkage",
                    severity = "CRITICAL",
                    path = "$.storyboardPacketReview.scriptPacketDraftContentSha256",
                    message = "Asset candidate preflight requires a valid lowercase SHA-256 script packet draft hash.",
                    evidence = listOf(review.scriptPacketDraftContentSha256),
                    suggestedFix = "Use the accepted script packet source hash produced by the storyboard review pipeline.",
                ),
            )
        }
        return count
    }

    private fun validateAssetCandidateRequirements(
        requirements: List<StoryboardPacketReviewAssetRequirement>,
        findings: MutableList<AssetCandidatePreflightFinding>,
    ) {
        if (requirements.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "ASSET_CANDIDATE_REQUIREMENTS_MISSING",
                    category = "asset_candidate_boundary",
                    severity = "CRITICAL",
                    path = "$.storyboardPacketReview.assetCandidateRequirements",
                    message = "Asset candidate preflight requires at least one asset candidate requirement.",
                    suggestedFix = "Return to storyboard packet review and carry forward candidate-only asset requirements.",
                ),
            )
            return
        }
        requirements.forEachIndexed { index, requirement ->
            if (requirement.candidateId.isBlank() || requirement.requirement.isBlank()) {
                findings.add(
                    finding(
                        ruleId = "ASSET_CANDIDATE_REQUIREMENT_INCOMPLETE",
                        category = "asset_candidate_boundary",
                        severity = "CRITICAL",
                        path = "$.storyboardPacketReview.assetCandidateRequirements[$index]",
                        message = "Asset candidate requirement requires candidateId and requirement text.",
                        suggestedFix = "Provide candidate-only requirement text before asset candidate preflight.",
                    ),
                )
            }
        }
    }

    private fun validateCandidateOnlyText(
        requirements: List<StoryboardPacketReviewAssetRequirement>,
        findings: MutableList<AssetCandidatePreflightFinding>,
    ): Int {
        var count = 0
        requirements.forEachIndexed { index, requirement ->
            val fields = listOf(
                "$.storyboardPacketReview.assetCandidateRequirements[$index].candidateId" to requirement.candidateId,
                "$.storyboardPacketReview.assetCandidateRequirements[$index].requirement" to requirement.requirement,
            ) + requirement.sourceFrameIds.mapIndexed { frameIndex, frameId ->
                "$.storyboardPacketReview.assetCandidateRequirements[$index].sourceFrameIds[$frameIndex]" to frameId
            }
            fields.forEach { (path, value) ->
                forbiddenCandidateStatusPatterns.forEach { pattern ->
                    if (value.isNotBlank() && pattern.containsMatchIn(value)) {
                        count += 1
                        findings.add(
                            finding(
                                ruleId = "ASSET_CANDIDATE_STATUS_FORBIDDEN",
                                category = "asset_candidate_boundary",
                                severity = "CRITICAL",
                                path = path,
                                message = "Asset candidate preflight only accepts candidate / requirement / reference-needed wording, not approved, locked, final, or production-ready status.",
                                evidence = listOf(value),
                                suggestedFix = "Reword the asset requirement as candidate-only or reference-needed material.",
                            ),
                        )
                    }
                }
            }
        }
        return count
    }

    private fun validateForbiddenClaims(
        intake: StoryboardPacketHumanReviewDecisionIntakeReport,
        review: StoryboardPacketReviewPacket,
        findings: MutableList<AssetCandidatePreflightFinding>,
    ): Int {
        var count = 0
        collectTextFields(intake, review).forEach { field ->
            forbiddenCompletionClaimPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings.add(
                        finding(
                            ruleId = "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN",
                            category = "safety_boundary",
                            severity = "CRITICAL",
                            path = field.path,
                            message = "Asset candidate preflight cannot claim generated media, asset approval, production lock, canon application, or runtime scene creation.",
                            evidence = listOf(field.value),
                            suggestedFix = "Keep this preflight limited to asset candidate preparation only.",
                        ),
                    )
                }
            }
        }
        return count
    }

    private fun collectTextFields(
        intake: StoryboardPacketHumanReviewDecisionIntakeReport,
        review: StoryboardPacketReviewPacket,
    ): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) {
            if (value.isNotBlank()) fields += TextField(path, value)
        }
        add("$.humanReviewDecisionIntake.decisionStatus", intake.decisionStatus)
        add("$.humanReviewDecisionIntake.nextAllowedDownstreamStep", intake.nextAllowedDownstreamStep)
        add("$.humanReviewDecisionIntake.blockedReason", intake.blockedReason)
        review.remainingResidualRisks.forEachIndexed { index, risk ->
            add("$.storyboardPacketReview.remainingResidualRisks[$index]", risk)
        }
        review.assetCandidateRequirements.forEachIndexed { index, requirement ->
            add("$.storyboardPacketReview.assetCandidateRequirements[$index].candidateId", requirement.candidateId)
            add("$.storyboardPacketReview.assetCandidateRequirements[$index].requirement", requirement.requirement)
        }
        return fields
    }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): AssetCandidatePreflightFinding =
        AssetCandidatePreflightFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            path = path,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
        )

    private fun normalizePath(value: String): String = value.trim().replace('\\', '/')

    private data class TextField(val path: String, val value: String)
}
