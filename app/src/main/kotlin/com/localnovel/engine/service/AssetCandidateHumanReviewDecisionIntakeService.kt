package com.localnovel.engine.service

import com.localnovel.engine.model.AssetCandidateHumanReviewDecision
import com.localnovel.engine.model.AssetCandidateHumanReviewDecisionIntakeEvidence
import com.localnovel.engine.model.AssetCandidateHumanReviewDecisionIntakeFinding
import com.localnovel.engine.model.AssetCandidateHumanReviewDecisionIntakeReport
import com.localnovel.engine.model.AssetCandidateReviewPreflightReport
import com.localnovel.engine.model.ImageCandidateRequirement

class AssetCandidateHumanReviewDecisionIntakeService {
    private val requiredPreflightStep = "asset_candidate_human_review_ready"
    private val downstreamStatus = "ALLOW_IMAGE_CANDIDATE_PREFLIGHT"
    private val nextAllowedDownstreamStep = "image_candidate_preflight"
    private val allowedSources = setOf("human", "user", "01_control")
    private val disallowedSources = setOf("auto", "model", "generated")
    private val allowedStatuses = setOf(downstreamStatus, "REQUEST_CHANGES", "REJECT")
    private val positiveChecklistResults = setOf("PASS", "PASSED", "ACCEPTED", "OK", "YES")
    private val requiredChecklistCategories = setOf(
        "candidate_identity",
        "asset_type",
        "visual_requirements",
        "continuity_scene_fit",
        "downstream_boundary",
        "no_approval_no_lock",
    )
    private val resolvedIssueStatuses = setOf("RESOLVED", "CLOSED", "ADDRESSED")
    private val blockingIssueSeverities = setOf("BLOCKING", "CRITICAL", "MAJOR")
    private val allowedRequirementStatuses = setOf("candidate", "candidate_only", "draft_candidate", "image_candidate")
    private val forbiddenStatusPatterns = listOf(
        Regex("""(?i)\breview_ready\b"""),
        Regex("""(?i)\bapproved\b"""),
        Regex("""(?i)\blocked\b"""),
        Regex("""(?i)\bfinal\b"""),
        Regex("""(?i)\bproduction[-_\s]?ready\b"""),
        Regex("""(?i)\bapproved[-_\s]?reference\b"""),
        Regex("""(?i)\breference\s+approved\b"""),
        Regex("""(?i)\bproduction[-_\s]?locked\b"""),
        Regex("\u6838\u51c6|\u6279\u51c6|\u9396\u5b9a"),
    )
    private val forbiddenClaimPatterns = listOf(
        Regex("""(?i)\bimage\s+generated\b"""),
        Regex("""(?i)\bgenerated\s+image\b"""),
        Regex("""(?i)\bvideo\s+generated\b"""),
        Regex("""(?i)\basset\s+approved\b"""),
        Regex("""(?i)\bassets\s+approved\b"""),
        Regex("""(?i)\basset\s+approval\s+complete\b"""),
        Regex("""(?i)\bproduction\s+locked\b"""),
        Regex("""(?i)\bcanon\s+applied\b"""),
        Regex("""(?i)\bruntime\s+scene\s+created\b"""),
        Regex("\u5716\u7247\u5df2\u751f\u6210|\u5f71\u7247\u5df2\u751f\u6210|\u7d20\u6750\u5df2\u6838\u51c6|\u7d20\u6750\u6838\u51c6\u5b8c\u6210|\u88fd\u4f5c\u9396\u5b9a|\u6b63\u5178\u5df2\u5957\u7528|\u57f7\u884c\u671f\u5834\u666f\u5df2\u5efa\u7acb"),
    )

    fun validate(
        assetCandidateReviewPreflight: AssetCandidateReviewPreflightReport,
        assetCandidateHumanReviewDecision: AssetCandidateHumanReviewDecision,
        assetCandidateReviewPreflightFile: String = "",
        assetCandidateHumanReviewDecisionFile: String = "",
    ): AssetCandidateHumanReviewDecisionIntakeReport {
        val findings = mutableListOf<AssetCandidateHumanReviewDecisionIntakeFinding>()
        validatePreflight(assetCandidateReviewPreflight, findings)
        val sourceCount = validateSourceBinding(
            preflight = assetCandidateReviewPreflight,
            decision = assetCandidateHumanReviewDecision,
            preflightFile = assetCandidateReviewPreflightFile,
            decisionFile = assetCandidateHumanReviewDecisionFile,
            findings = findings,
        )
        validateDecisionSource(assetCandidateHumanReviewDecision, findings)
        val normalizedStatus = assetCandidateHumanReviewDecision.decisionStatus.trim().uppercase()
        validateDecisionStatus(normalizedStatus, assetCandidateHumanReviewDecision.decisionStatus, findings)
        val allowDownstream = normalizedStatus == downstreamStatus
        val checklistPassedCount = validateChecklist(assetCandidateHumanReviewDecision, allowDownstream, findings)
        val unresolvedBlockingCount = validateBlockingIssues(assetCandidateHumanReviewDecision, allowDownstream, findings)
        validateImageCandidateRequirements(assetCandidateHumanReviewDecision.imageCandidateRequirements, allowDownstream, findings)
        val forbiddenStatusCount = validateForbiddenStatusText(assetCandidateHumanReviewDecision, findings)
        val forbiddenOutputFieldCount = validateForbiddenOutputFields(assetCandidateHumanReviewDecision.imageCandidateRequirements, findings)
        val forbiddenClaimCount = validateForbiddenClaims(assetCandidateHumanReviewDecision, findings)

        val verdict = when {
            findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
            findings.any { it.severity == "WARNING" } -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }
        val downstreamAllowed = verdict != "FAIL" && allowDownstream
        val blockedReason = if (verdict == "FAIL") {
            findings
                .filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }
                .joinToString("; ") { it.ruleId }
        } else {
            ""
        }

        return AssetCandidateHumanReviewDecisionIntakeReport(
            verdict = verdict,
            decisionId = assetCandidateHumanReviewDecision.decisionId,
            preflightId = assetCandidateHumanReviewDecision.preflightId,
            assetCandidateReviewPreflightFile = assetCandidateReviewPreflightFile,
            assetCandidateHumanReviewDecisionFile = assetCandidateHumanReviewDecisionFile,
            assetCandidateDraftValidationReportFile = assetCandidateHumanReviewDecision.assetCandidateDraftValidationReportFile,
            assetCandidateDraftFile = assetCandidateHumanReviewDecision.assetCandidateDraftFile,
            assetCandidateDraftId = assetCandidateHumanReviewDecision.assetCandidateDraftId,
            assetCandidateBriefFile = assetCandidateHumanReviewDecision.assetCandidateBriefFile,
            assetCandidateBriefId = assetCandidateHumanReviewDecision.assetCandidateBriefId,
            assetCandidatePreflightFile = assetCandidateHumanReviewDecision.assetCandidatePreflightFile,
            assetCandidatePreflightId = assetCandidateHumanReviewDecision.assetCandidatePreflightId,
            humanReviewDecisionIntakeFile = assetCandidateHumanReviewDecision.humanReviewDecisionIntakeFile,
            humanReviewDecisionId = assetCandidateHumanReviewDecision.humanReviewDecisionId,
            storyboardPacketReviewFile = assetCandidateHumanReviewDecision.storyboardPacketReviewFile,
            storyboardPacketReviewId = assetCandidateHumanReviewDecision.storyboardPacketReviewId,
            acceptedScriptPacketId = assetCandidateHumanReviewDecision.acceptedScriptPacketId,
            scriptPacketDraftFile = assetCandidateHumanReviewDecision.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = assetCandidateHumanReviewDecision.scriptPacketDraftContentSha256,
            decisionStatus = assetCandidateHumanReviewDecision.decisionStatus,
            decisionAllowedDownstream = downstreamAllowed,
            imageCandidateRequirements = if (downstreamAllowed) assetCandidateHumanReviewDecision.imageCandidateRequirements else emptyList(),
            nextAllowedDownstreamStep = if (downstreamAllowed) nextAllowedDownstreamStep else "",
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = AssetCandidateHumanReviewDecisionIntakeEvidence(
                preflightVerdict = assetCandidateReviewPreflight.verdict,
                preflightNextAllowedDownstreamStep = assetCandidateReviewPreflight.nextAllowedDownstreamStep,
                sourceBindingFieldCount = sourceCount,
                decisionSource = assetCandidateHumanReviewDecision.decisionSource,
                decisionStatus = assetCandidateHumanReviewDecision.decisionStatus,
                checklistItemCount = assetCandidateHumanReviewDecision.checklistItems.size,
                checklistPassedCount = checklistPassedCount,
                unresolvedBlockingIssueCount = unresolvedBlockingCount,
                imageCandidateRequirementCount = assetCandidateHumanReviewDecision.imageCandidateRequirements.size,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
                forbiddenOutputFieldCount = forbiddenOutputFieldCount,
            ),
        )
    }

    private fun validatePreflight(
        preflight: AssetCandidateReviewPreflightReport,
        findings: MutableList<AssetCandidateHumanReviewDecisionIntakeFinding>,
    ) {
        if (preflight.verdict.trim().uppercase() != "PASS") {
            findings.add(
                finding(
                    ruleId = "ASSET_CANDIDATE_REVIEW_PREFLIGHT_NOT_PASS",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.assetCandidateReviewPreflight.verdict",
                    message = "Asset candidate human review decision intake requires a PASS review preflight.",
                    evidence = listOf(preflight.verdict),
                    suggestedFix = "Use the PASS asset candidate review preflight report.",
                ),
            )
        }
        if (preflight.nextAllowedDownstreamStep != requiredPreflightStep) {
            findings.add(
                finding(
                    ruleId = "ASSET_CANDIDATE_REVIEW_PREFLIGHT_NEXT_STEP_MISMATCH",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.assetCandidateReviewPreflight.nextAllowedDownstreamStep",
                    message = "Asset candidate human review decision intake requires nextAllowedDownstreamStep=asset_candidate_human_review_ready.",
                    evidence = listOf(preflight.nextAllowedDownstreamStep),
                    suggestedFix = "Use the preflight that explicitly allows asset_candidate_human_review_ready.",
                ),
            )
        }
    }

    private fun validateSourceBinding(
        preflight: AssetCandidateReviewPreflightReport,
        decision: AssetCandidateHumanReviewDecision,
        preflightFile: String,
        decisionFile: String,
        findings: MutableList<AssetCandidateHumanReviewDecisionIntakeFinding>,
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
                        message = "Asset candidate human review decision intake requires nonblank $label.",
                        suggestedFix = "Use the matching review preflight and human decision JSON.",
                    ),
                )
            } else {
                count += 1
            }
        }

        fun requireMatch(
            preflightValue: String,
            decisionValue: String,
            preflightPath: String,
            decisionPath: String,
            missingRuleId: String,
            mismatchRuleId: String,
            label: String,
            normalize: (String) -> String = { it.trim() },
        ) {
            requireNonBlank(preflightValue, preflightPath, "${missingRuleId}_PREFLIGHT", "$label in preflight")
            requireNonBlank(decisionValue, decisionPath, missingRuleId, "$label in decision")
            val left = normalize(preflightValue)
            val right = normalize(decisionValue)
            if (left.isNotBlank() && right.isNotBlank() && left != right) {
                findings.add(
                    finding(
                        ruleId = mismatchRuleId,
                        category = "source_linkage",
                        severity = "CRITICAL",
                        path = decisionPath,
                        message = "Asset candidate human review decision source binding mismatch for $label.",
                        evidence = listOf("preflight=$preflightValue", "decision=$decisionValue"),
                        suggestedFix = "Use a decision JSON created for the same asset candidate review preflight.",
                    ),
                )
            }
        }

        requireNonBlank(preflightFile, "$.inputs.assetCandidateReviewPreflightFile", "ASSET_CANDIDATE_REVIEW_PREFLIGHT_FILE_MISSING", "asset candidate review preflight file")
        requireNonBlank(decisionFile, "$.inputs.assetCandidateHumanReviewDecisionFile", "ASSET_CANDIDATE_HUMAN_REVIEW_DECISION_FILE_MISSING", "human review decision file")
        requireNonBlank(decision.decisionId, "$.assetCandidateHumanReviewDecision.decisionId", "DECISION_ID_MISSING", "decision id")
        requireNonBlank(decision.reviewer, "$.assetCandidateHumanReviewDecision.reviewer", "DECISION_REVIEWER_MISSING", "reviewer")
        requireNonBlank(decision.reviewerRole, "$.assetCandidateHumanReviewDecision.reviewerRole", "DECISION_REVIEWER_ROLE_MISSING", "reviewer role")
        requireNonBlank(decision.decisionAt, "$.assetCandidateHumanReviewDecision.decisionAt", "DECISION_AT_MISSING", "decision timestamp")
        requireNonBlank(decision.assetCandidateReviewPreflightFile, "$.assetCandidateHumanReviewDecision.assetCandidateReviewPreflightFile", "DECISION_PREFLIGHT_FILE_MISSING", "preflight file in decision")
        if (preflightFile.isNotBlank() &&
            decision.assetCandidateReviewPreflightFile.isNotBlank() &&
            normalizePath(preflightFile) != normalizePath(decision.assetCandidateReviewPreflightFile)
        ) {
            findings.add(
                finding(
                    ruleId = "DECISION_PREFLIGHT_FILE_MISMATCH",
                    category = "source_linkage",
                    severity = "CRITICAL",
                    path = "$.assetCandidateHumanReviewDecision.assetCandidateReviewPreflightFile",
                    message = "Human review decision must point to the same asset candidate review preflight file.",
                    evidence = listOf("input=$preflightFile", "decision=${decision.assetCandidateReviewPreflightFile}"),
                    suggestedFix = "Use the decision JSON for this preflight file.",
                ),
            )
        }

        requireMatch(preflight.preflightId, decision.preflightId, "$.assetCandidateReviewPreflight.preflightId", "$.assetCandidateHumanReviewDecision.preflightId", "DECISION_PREFLIGHT_ID_MISSING", "DECISION_PREFLIGHT_ID_MISMATCH", "preflight id")
        requireMatch(preflight.assetCandidateDraftValidationReportFile, decision.assetCandidateDraftValidationReportFile, "$.assetCandidateReviewPreflight.assetCandidateDraftValidationReportFile", "$.assetCandidateHumanReviewDecision.assetCandidateDraftValidationReportFile", "DECISION_DRAFT_VALIDATION_REPORT_FILE_MISSING", "DECISION_DRAFT_VALIDATION_REPORT_FILE_MISMATCH", "draft validation report file", ::normalizePath)
        requireMatch(preflight.assetCandidateDraftFile, decision.assetCandidateDraftFile, "$.assetCandidateReviewPreflight.assetCandidateDraftFile", "$.assetCandidateHumanReviewDecision.assetCandidateDraftFile", "DECISION_ASSET_CANDIDATE_DRAFT_FILE_MISSING", "DECISION_ASSET_CANDIDATE_DRAFT_FILE_MISMATCH", "asset candidate draft file", ::normalizePath)
        requireMatch(preflight.assetCandidateDraftId, decision.assetCandidateDraftId, "$.assetCandidateReviewPreflight.assetCandidateDraftId", "$.assetCandidateHumanReviewDecision.assetCandidateDraftId", "DECISION_ASSET_CANDIDATE_DRAFT_ID_MISSING", "DECISION_ASSET_CANDIDATE_DRAFT_ID_MISMATCH", "asset candidate draft id")
        requireMatch(preflight.assetCandidateBriefFile, decision.assetCandidateBriefFile, "$.assetCandidateReviewPreflight.assetCandidateBriefFile", "$.assetCandidateHumanReviewDecision.assetCandidateBriefFile", "DECISION_ASSET_CANDIDATE_BRIEF_FILE_MISSING", "DECISION_ASSET_CANDIDATE_BRIEF_FILE_MISMATCH", "asset candidate brief file", ::normalizePath)
        requireMatch(preflight.assetCandidateBriefId, decision.assetCandidateBriefId, "$.assetCandidateReviewPreflight.assetCandidateBriefId", "$.assetCandidateHumanReviewDecision.assetCandidateBriefId", "DECISION_ASSET_CANDIDATE_BRIEF_ID_MISSING", "DECISION_ASSET_CANDIDATE_BRIEF_ID_MISMATCH", "asset candidate brief id")
        requireMatch(preflight.assetCandidatePreflightFile, decision.assetCandidatePreflightFile, "$.assetCandidateReviewPreflight.assetCandidatePreflightFile", "$.assetCandidateHumanReviewDecision.assetCandidatePreflightFile", "DECISION_ASSET_CANDIDATE_PREFLIGHT_FILE_MISSING", "DECISION_ASSET_CANDIDATE_PREFLIGHT_FILE_MISMATCH", "asset candidate preflight file", ::normalizePath)
        requireMatch(preflight.assetCandidatePreflightId, decision.assetCandidatePreflightId, "$.assetCandidateReviewPreflight.assetCandidatePreflightId", "$.assetCandidateHumanReviewDecision.assetCandidatePreflightId", "DECISION_ASSET_CANDIDATE_PREFLIGHT_ID_MISSING", "DECISION_ASSET_CANDIDATE_PREFLIGHT_ID_MISMATCH", "asset candidate preflight id")
        requireMatch(preflight.humanReviewDecisionIntakeFile, decision.humanReviewDecisionIntakeFile, "$.assetCandidateReviewPreflight.humanReviewDecisionIntakeFile", "$.assetCandidateHumanReviewDecision.humanReviewDecisionIntakeFile", "DECISION_STORYBOARD_HUMAN_REVIEW_DECISION_INTAKE_FILE_MISSING", "DECISION_STORYBOARD_HUMAN_REVIEW_DECISION_INTAKE_FILE_MISMATCH", "storyboard human review decision intake file", ::normalizePath)
        requireMatch(preflight.humanReviewDecisionId, decision.humanReviewDecisionId, "$.assetCandidateReviewPreflight.humanReviewDecisionId", "$.assetCandidateHumanReviewDecision.humanReviewDecisionId", "DECISION_STORYBOARD_HUMAN_REVIEW_DECISION_ID_MISSING", "DECISION_STORYBOARD_HUMAN_REVIEW_DECISION_ID_MISMATCH", "storyboard human review decision id")
        requireMatch(preflight.storyboardPacketReviewFile, decision.storyboardPacketReviewFile, "$.assetCandidateReviewPreflight.storyboardPacketReviewFile", "$.assetCandidateHumanReviewDecision.storyboardPacketReviewFile", "DECISION_STORYBOARD_PACKET_REVIEW_FILE_MISSING", "DECISION_STORYBOARD_PACKET_REVIEW_FILE_MISMATCH", "storyboard packet review file", ::normalizePath)
        requireMatch(preflight.storyboardPacketReviewId, decision.storyboardPacketReviewId, "$.assetCandidateReviewPreflight.storyboardPacketReviewId", "$.assetCandidateHumanReviewDecision.storyboardPacketReviewId", "DECISION_STORYBOARD_PACKET_REVIEW_ID_MISSING", "DECISION_STORYBOARD_PACKET_REVIEW_ID_MISMATCH", "storyboard packet review id")
        requireMatch(preflight.acceptedScriptPacketId, decision.acceptedScriptPacketId, "$.assetCandidateReviewPreflight.acceptedScriptPacketId", "$.assetCandidateHumanReviewDecision.acceptedScriptPacketId", "DECISION_ACCEPTED_SCRIPT_PACKET_ID_MISSING", "DECISION_ACCEPTED_SCRIPT_PACKET_ID_MISMATCH", "accepted script packet id")
        requireMatch(preflight.scriptPacketDraftFile, decision.scriptPacketDraftFile, "$.assetCandidateReviewPreflight.scriptPacketDraftFile", "$.assetCandidateHumanReviewDecision.scriptPacketDraftFile", "DECISION_SCRIPT_PACKET_DRAFT_FILE_MISSING", "DECISION_SCRIPT_PACKET_DRAFT_FILE_MISMATCH", "script packet draft file", ::normalizePath)
        requireMatch(preflight.scriptPacketDraftContentSha256, decision.scriptPacketDraftContentSha256, "$.assetCandidateReviewPreflight.scriptPacketDraftContentSha256", "$.assetCandidateHumanReviewDecision.scriptPacketDraftContentSha256", "DECISION_SCRIPT_PACKET_DRAFT_HASH_MISSING", "DECISION_SCRIPT_PACKET_DRAFT_HASH_MISMATCH", "script packet draft SHA-256", { it.trim().lowercase() })
        return count
    }

    private fun validateDecisionSource(
        decision: AssetCandidateHumanReviewDecision,
        findings: MutableList<AssetCandidateHumanReviewDecisionIntakeFinding>,
    ) {
        val source = decision.decisionSource.trim().lowercase()
        if (source.isBlank()) {
            findings.add(finding("DECISION_SOURCE_MISSING", "human_decision", "CRITICAL", "$.assetCandidateHumanReviewDecision.decisionSource", "Asset candidate human review decision must declare a human/control/user source.", suggestedFix = "Set decisionSource to human, user, or 01_control."))
        } else if (source in disallowedSources || source !in allowedSources) {
            findings.add(finding("DECISION_SOURCE_NOT_HUMAN", "human_decision", "CRITICAL", "$.assetCandidateHumanReviewDecision.decisionSource", "Asset candidate human review decision source cannot be automatic, model, generated, or unknown.", evidence = listOf(decision.decisionSource), suggestedFix = "Use an explicit external human/control/user decision source."))
        }
    }

    private fun validateDecisionStatus(
        normalizedStatus: String,
        rawStatus: String,
        findings: MutableList<AssetCandidateHumanReviewDecisionIntakeFinding>,
    ) {
        if (normalizedStatus.isBlank()) {
            findings.add(finding("DECISION_STATUS_MISSING", "human_decision", "CRITICAL", "$.assetCandidateHumanReviewDecision.decisionStatus", "Asset candidate human review decision status must be declared.", suggestedFix = "Use ALLOW_IMAGE_CANDIDATE_PREFLIGHT, REQUEST_CHANGES, or REJECT."))
        } else if (normalizedStatus !in allowedStatuses) {
            findings.add(finding("DECISION_STATUS_UNKNOWN", "human_decision", "CRITICAL", "$.assetCandidateHumanReviewDecision.decisionStatus", "Asset candidate human review decision status is outside the narrow allowed vocabulary.", evidence = listOf(rawStatus), suggestedFix = "Use ALLOW_IMAGE_CANDIDATE_PREFLIGHT, REQUEST_CHANGES, or REJECT."))
        }
    }

    private fun validateChecklist(
        decision: AssetCandidateHumanReviewDecision,
        allowDownstream: Boolean,
        findings: MutableList<AssetCandidateHumanReviewDecisionIntakeFinding>,
    ): Int {
        if (decision.checklistItems.isEmpty()) {
            findings.add(finding("HUMAN_CHECKLIST_MISSING", "human_decision", "CRITICAL", "$.assetCandidateHumanReviewDecision.checklistItems", "Asset candidate human review decision must include checklist item results.", suggestedFix = "Record human checklist results before allowing image candidate preflight."))
            return 0
        }
        var passed = 0
        decision.checklistItems.forEachIndexed { index, item ->
            if (item.itemId.isBlank() || item.category.isBlank() || item.result.isBlank()) {
                findings.add(finding("HUMAN_CHECKLIST_ITEM_INCOMPLETE", "human_decision", "CRITICAL", "$.assetCandidateHumanReviewDecision.checklistItems[$index]", "Human review checklist item requires itemId, category, and result.", suggestedFix = "Complete every checklist item."))
            }
            val result = item.result.trim().uppercase()
            if (result in positiveChecklistResults) {
                passed += 1
            } else if (allowDownstream) {
                findings.add(finding("HUMAN_CHECKLIST_ITEM_NOT_ACCEPTED", "human_decision", "CRITICAL", "$.assetCandidateHumanReviewDecision.checklistItems[$index].result", "Allowing image candidate preflight requires every checklist item to pass.", evidence = listOf(item.result), suggestedFix = "Use REQUEST_CHANGES or REJECT until every required item is accepted."))
            }
        }
        if (allowDownstream) {
            val accepted = decision.checklistItems
                .filter { it.result.trim().uppercase() in positiveChecklistResults }
                .map { it.category.trim().lowercase() }
                .toSet()
            val missing = requiredChecklistCategories.filterNot { it in accepted }
            if (missing.isNotEmpty()) {
                findings.add(finding("HUMAN_CHECKLIST_REQUIRED_CATEGORY_MISSING", "human_decision", "CRITICAL", "$.assetCandidateHumanReviewDecision.checklistItems", "Allowing image candidate preflight requires accepted checklist coverage for candidate identity/type, visual requirements, continuity/scene fit, downstream boundary, and no approval/no lock.", evidence = missing, suggestedFix = "Record positive human checklist results for every required category."))
            }
        }
        return passed
    }

    private fun validateBlockingIssues(
        decision: AssetCandidateHumanReviewDecision,
        allowDownstream: Boolean,
        findings: MutableList<AssetCandidateHumanReviewDecisionIntakeFinding>,
    ): Int {
        var count = 0
        decision.blockingIssues.forEachIndexed { index, issue ->
            val unresolvedBlocking = issue.severity.trim().uppercase() in blockingIssueSeverities &&
                issue.status.trim().uppercase() !in resolvedIssueStatuses
            if (unresolvedBlocking) {
                count += 1
                if (allowDownstream) {
                    findings.add(finding("UNRESOLVED_BLOCKING_ISSUE_PRESENT", "human_decision", "CRITICAL", "$.assetCandidateHumanReviewDecision.blockingIssues[$index]", "Allowing image candidate preflight requires no unresolved blocking issue.", evidence = listOf(issue.issueId, issue.severity, issue.status).filter { it.isNotBlank() }, suggestedFix = "Resolve the blocking issue or use REQUEST_CHANGES / REJECT."))
                }
            }
        }
        return count
    }

    private fun validateImageCandidateRequirements(
        requirements: List<ImageCandidateRequirement>,
        allowDownstream: Boolean,
        findings: MutableList<AssetCandidateHumanReviewDecisionIntakeFinding>,
    ) {
        if (allowDownstream && requirements.isEmpty()) {
            findings.add(finding("IMAGE_CANDIDATE_REQUIREMENTS_MISSING", "image_candidate_boundary", "CRITICAL", "$.assetCandidateHumanReviewDecision.imageCandidateRequirements", "Allowing image candidate preflight requires at least one image candidate requirement.", suggestedFix = "Provide candidate-only image requirements or request changes."))
        }
        requirements.forEachIndexed { index, requirement ->
            requireRequirementField(requirement.requirementId, index, "requirementId", "IMAGE_CANDIDATE_REQUIREMENT_ID_MISSING", findings)
            requireRequirementField(requirement.candidateId, index, "candidateId", "IMAGE_CANDIDATE_CANDIDATE_ID_MISSING", findings)
            requireRequirementField(requirement.assetType, index, "assetType", "IMAGE_CANDIDATE_ASSET_TYPE_MISSING", findings)
            requireRequirementField(requirement.sourceRequirementId, index, "sourceRequirementId", "IMAGE_CANDIDATE_SOURCE_REQUIREMENT_ID_MISSING", findings)
            requireRequirementField(requirement.requirement, index, "requirement", "IMAGE_CANDIDATE_REQUIREMENT_TEXT_MISSING", findings)
            if (requirement.referenceRequirements.isEmpty()) {
                findings.add(finding("IMAGE_CANDIDATE_REFERENCE_REQUIREMENTS_MISSING", "image_candidate_boundary", "CRITICAL", "$.assetCandidateHumanReviewDecision.imageCandidateRequirements[$index].referenceRequirements", "Image candidate requirement needs reference requirements.", suggestedFix = "Describe needed candidate reference views or constraints."))
            }
            if (requirement.promptSafetyNotes.isEmpty()) {
                findings.add(finding("IMAGE_CANDIDATE_PROMPT_SAFETY_NOTES_MISSING", "image_candidate_boundary", "CRITICAL", "$.assetCandidateHumanReviewDecision.imageCandidateRequirements[$index].promptSafetyNotes", "Image candidate requirement needs prompt safety notes.", suggestedFix = "Declare prompt-safe boundaries and no-generation approval semantics."))
            }
            if (requirement.status.trim().lowercase() !in allowedRequirementStatuses) {
                findings.add(finding("IMAGE_CANDIDATE_STATUS_NOT_ALLOWED", "image_candidate_boundary", "CRITICAL", "$.assetCandidateHumanReviewDecision.imageCandidateRequirements[$index].status", "Image candidate requirement status must remain candidate-only.", evidence = listOf(requirement.status), suggestedFix = "Use candidate, candidate_only, draft_candidate, or image_candidate."))
            }
        }
    }

    private fun requireRequirementField(
        value: String,
        index: Int,
        field: String,
        ruleId: String,
        findings: MutableList<AssetCandidateHumanReviewDecisionIntakeFinding>,
    ) {
        if (value.isBlank()) {
            findings.add(finding(ruleId, "image_candidate_boundary", "CRITICAL", "$.assetCandidateHumanReviewDecision.imageCandidateRequirements[$index].$field", "Image candidate requirement requires nonblank $field.", suggestedFix = "Use a complete candidate-only image requirement."))
        }
    }

    private fun validateForbiddenStatusText(
        decision: AssetCandidateHumanReviewDecision,
        findings: MutableList<AssetCandidateHumanReviewDecisionIntakeFinding>,
    ): Int {
        var count = 0
        collectTextFields(decision).forEach { field ->
            forbiddenStatusPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings.add(finding("IMAGE_CANDIDATE_STATUS_FORBIDDEN", "image_candidate_boundary", "CRITICAL", field.path, "Image candidate decision cannot contain approved, locked, final, review-ready, production-ready, approved-reference, or production-locked wording.", evidence = listOf(field.value), suggestedFix = "Keep wording candidate-only and unapproved."))
                }
            }
        }
        return count
    }

    private fun validateForbiddenOutputFields(
        requirements: List<ImageCandidateRequirement>,
        findings: MutableList<AssetCandidateHumanReviewDecisionIntakeFinding>,
    ): Int {
        var count = 0
        requirements.forEachIndexed { index, requirement ->
            listOf(
                Triple(requirement.imageOutputPath, "imageOutputPath", "IMAGE_OUTPUT_PATH_FORBIDDEN"),
                Triple(requirement.generationApi, "generationApi", "GENERATION_API_FORBIDDEN"),
                Triple(requirement.providerJobId, "providerJobId", "PROVIDER_JOB_ID_FORBIDDEN"),
            ).forEach { (value, field, ruleId) ->
                if (value.isNotBlank()) {
                    count += 1
                    findings.add(finding(ruleId, "safety_boundary", "CRITICAL", "$.assetCandidateHumanReviewDecision.imageCandidateRequirements[$index].$field", "Image candidate decision intake cannot carry image output path, generation API, provider, or job information.", evidence = listOf(value), suggestedFix = "Remove provider/job/output details; this gate is preparation-only."))
                }
            }
        }
        return count
    }

    private fun validateForbiddenClaims(
        decision: AssetCandidateHumanReviewDecision,
        findings: MutableList<AssetCandidateHumanReviewDecisionIntakeFinding>,
    ): Int {
        var count = 0
        collectTextFields(decision).forEach { field ->
            forbiddenClaimPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings.add(finding("DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", "CRITICAL", field.path, "Image candidate decision intake cannot claim generated media, asset approval, production lock, canon application, or runtime scene creation.", evidence = listOf(field.value), suggestedFix = "Keep this decision limited to allowing image candidate preflight, requesting changes, or rejecting."))
                }
            }
        }
        return count
    }

    private fun collectTextFields(decision: AssetCandidateHumanReviewDecision): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) {
            if (value.isNotBlank()) fields += TextField(path, value)
        }
        add("$.assetCandidateHumanReviewDecision.decisionSource", decision.decisionSource)
        add("$.assetCandidateHumanReviewDecision.reviewer", decision.reviewer)
        add("$.assetCandidateHumanReviewDecision.reviewerRole", decision.reviewerRole)
        add("$.assetCandidateHumanReviewDecision.decisionStatus", decision.decisionStatus)
        add("$.assetCandidateHumanReviewDecision.notes", decision.notes)
        decision.checklistItems.forEachIndexed { index, item ->
            add("$.assetCandidateHumanReviewDecision.checklistItems[$index].category", item.category)
            add("$.assetCandidateHumanReviewDecision.checklistItems[$index].result", item.result)
            add("$.assetCandidateHumanReviewDecision.checklistItems[$index].note", item.note)
        }
        decision.blockingIssues.forEachIndexed { index, issue ->
            add("$.assetCandidateHumanReviewDecision.blockingIssues[$index].description", issue.description)
            add("$.assetCandidateHumanReviewDecision.blockingIssues[$index].status", issue.status)
        }
        decision.imageCandidateRequirements.forEachIndexed { index, requirement ->
            add("$.assetCandidateHumanReviewDecision.imageCandidateRequirements[$index].status", requirement.status)
            add("$.assetCandidateHumanReviewDecision.imageCandidateRequirements[$index].requirement", requirement.requirement)
            requirement.referenceRequirements.forEachIndexed { noteIndex, value ->
                add("$.assetCandidateHumanReviewDecision.imageCandidateRequirements[$index].referenceRequirements[$noteIndex]", value)
            }
            requirement.doNotShow.forEachIndexed { noteIndex, value ->
                add("$.assetCandidateHumanReviewDecision.imageCandidateRequirements[$index].doNotShow[$noteIndex]", value)
            }
            requirement.promptSafetyNotes.forEachIndexed { noteIndex, value ->
                add("$.assetCandidateHumanReviewDecision.imageCandidateRequirements[$index].promptSafetyNotes[$noteIndex]", value)
            }
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
    ): AssetCandidateHumanReviewDecisionIntakeFinding =
        AssetCandidateHumanReviewDecisionIntakeFinding(
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
