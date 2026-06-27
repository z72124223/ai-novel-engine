package com.localnovel.engine.service

import com.localnovel.engine.model.StoryboardPacketHumanReviewDecision
import com.localnovel.engine.model.StoryboardPacketHumanReviewDecisionIntakeEvidence
import com.localnovel.engine.model.StoryboardPacketHumanReviewDecisionIntakeFinding
import com.localnovel.engine.model.StoryboardPacketHumanReviewDecisionIntakeReport
import com.localnovel.engine.model.StoryboardPacketHumanReviewPreflightReport

class StoryboardPacketHumanReviewDecisionIntakeService {
    private val requiredPreflightStep = "storyboard_packet_human_review_ready"
    private val nextAllowedDownstreamStep = "asset_candidate_preflight"
    private val allowedSources = setOf("human", "01_control", "user")
    private val disallowedSources = setOf("auto", "model", "generated")
    private val allowedStatuses = setOf("ALLOW_ASSET_CANDIDATE_PREFLIGHT", "REQUEST_CHANGES", "REJECT")
    private val downstreamStatus = "ALLOW_ASSET_CANDIDATE_PREFLIGHT"
    private val positiveChecklistResults = setOf("PASS", "PASSED", "ACCEPTED", "OK", "YES")
    private val requiredChecklistCategories = setOf(
        "scene_lock",
        "continuity",
        "knowledge_boundary",
        "downstream_boundary",
    )
    private val assetChecklistCategories = setOf("asset_candidates", "assets")
    private val resolvedIssueStatuses = setOf("RESOLVED", "CLOSED", "ADDRESSED")
    private val blockingIssueSeverities = setOf("BLOCKING", "CRITICAL", "MAJOR")
    private val forbiddenClaimPatterns = listOf(
        Regex("""(?i)\bimage\s+generated\b"""),
        Regex("""(?i)\bvideo\s+generated\b"""),
        Regex("""(?i)\basset\s+approved\b"""),
        Regex("""(?i)\bassets\s+approved\b"""),
        Regex("""(?i)\basset\s+approval\s+complete\b"""),
        Regex("""(?i)\bproduction\s+locked\b"""),
        Regex("""(?i)\bcanon\s+applied\b"""),
        Regex("""(?i)\bruntime\s+scene\s+created\b"""),
        Regex("""圖片已生成|影片已生成|素材已核准|素材核准完成|製作鎖定|正典已套用|執行期場景已建立"""),
    )

    fun validate(
        humanReviewPreflight: StoryboardPacketHumanReviewPreflightReport,
        humanReviewDecision: StoryboardPacketHumanReviewDecision,
        humanReviewPreflightFile: String = "",
        humanReviewDecisionFile: String = "",
    ): StoryboardPacketHumanReviewDecisionIntakeReport {
        val findings = mutableListOf<StoryboardPacketHumanReviewDecisionIntakeFinding>()
        validatePreflight(humanReviewPreflight, findings)
        val sourceBindingCount = validateSourceBinding(
            preflight = humanReviewPreflight,
            decision = humanReviewDecision,
            humanReviewPreflightFile = humanReviewPreflightFile,
            humanReviewDecisionFile = humanReviewDecisionFile,
            findings = findings,
        )
        validateDecisionSource(humanReviewDecision, findings)
        val normalizedStatus = humanReviewDecision.decisionStatus.trim().uppercase()
        validateDecisionStatus(normalizedStatus, humanReviewDecision.decisionStatus, findings)
        val allowDownstream = normalizedStatus == downstreamStatus
        val checklistPassedCount = validateChecklist(humanReviewDecision, allowDownstream, findings)
        val unresolvedBlockingIssueCount = validateBlockingIssues(humanReviewDecision, allowDownstream, findings)
        val forbiddenClaimCount = validateForbiddenClaims(humanReviewDecision, findings)

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

        return StoryboardPacketHumanReviewDecisionIntakeReport(
            verdict = verdict,
            decisionId = humanReviewDecision.decisionId,
            preflightId = humanReviewDecision.preflightId,
            humanReviewPreflightFile = humanReviewPreflightFile,
            humanReviewDecisionFile = humanReviewDecisionFile,
            storyboardPacketReviewFile = humanReviewDecision.storyboardPacketReviewFile,
            storyboardPacketReviewId = humanReviewDecision.storyboardPacketReviewId,
            storyboardPacketDraftFile = humanReviewDecision.storyboardPacketDraftFile,
            storyboardPacketDraftReportFile = humanReviewDecision.storyboardPacketDraftReportFile,
            storyboardPacketDraftId = humanReviewDecision.storyboardPacketDraftId,
            storyboardPlanningBriefId = humanReviewDecision.storyboardPlanningBriefId,
            acceptedScriptPacketId = humanReviewDecision.acceptedScriptPacketId,
            scriptPacketDraftFile = humanReviewDecision.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = humanReviewDecision.scriptPacketDraftContentSha256,
            decisionStatus = humanReviewDecision.decisionStatus,
            decisionAllowedDownstream = verdict != "FAIL" && allowDownstream,
            nextAllowedDownstreamStep = if (verdict != "FAIL" && allowDownstream) nextAllowedDownstreamStep else "",
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = StoryboardPacketHumanReviewDecisionIntakeEvidence(
                preflightVerdict = humanReviewPreflight.verdict,
                preflightNextAllowedDownstreamStep = humanReviewPreflight.nextAllowedDownstreamStep,
                sourceBindingFieldCount = sourceBindingCount,
                decisionSource = humanReviewDecision.decisionSource,
                decisionStatus = humanReviewDecision.decisionStatus,
                checklistItemCount = humanReviewDecision.checklistItems.size,
                checklistPassedCount = checklistPassedCount,
                unresolvedBlockingIssueCount = unresolvedBlockingIssueCount,
                forbiddenClaimCount = forbiddenClaimCount,
            ),
        )
    }

    private fun validatePreflight(
        preflight: StoryboardPacketHumanReviewPreflightReport,
        findings: MutableList<StoryboardPacketHumanReviewDecisionIntakeFinding>,
    ) {
        if (preflight.verdict.normalizedVerdict() != "PASS") {
            findings.add(
                finding(
                    ruleId = "HUMAN_REVIEW_PREFLIGHT_NOT_PASS",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.humanReviewPreflight.verdict",
                    message = "Human review decision intake requires a PASS human review preflight.",
                    evidence = listOf(preflight.verdict),
                    suggestedFix = "Use the PASS preflight report created for storyboard_packet_human_review_ready.",
                ),
            )
        }
        if (preflight.nextAllowedDownstreamStep != requiredPreflightStep) {
            findings.add(
                finding(
                    ruleId = "HUMAN_REVIEW_PREFLIGHT_NEXT_STEP_MISMATCH",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.humanReviewPreflight.nextAllowedDownstreamStep",
                    message = "Human review decision intake requires nextAllowedDownstreamStep=storyboard_packet_human_review_ready.",
                    evidence = listOf(preflight.nextAllowedDownstreamStep),
                    suggestedFix = "Use the preflight report that explicitly allows human review decision intake.",
                ),
            )
        }
    }

    private fun validateSourceBinding(
        preflight: StoryboardPacketHumanReviewPreflightReport,
        decision: StoryboardPacketHumanReviewDecision,
        humanReviewPreflightFile: String,
        humanReviewDecisionFile: String,
        findings: MutableList<StoryboardPacketHumanReviewDecisionIntakeFinding>,
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
                        message = "Human review decision intake requires nonblank $label.",
                        suggestedFix = "Use the human review preflight report and matching external human decision JSON.",
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
            val normalizedPreflight = normalize(preflightValue)
            val normalizedDecision = normalize(decisionValue)
            if (normalizedPreflight.isNotBlank() && normalizedDecision.isNotBlank() && normalizedPreflight != normalizedDecision) {
                findings.add(
                    finding(
                        ruleId = mismatchRuleId,
                        category = "source_linkage",
                        severity = "CRITICAL",
                        path = decisionPath,
                        message = "Human review decision source binding mismatch for $label.",
                        evidence = listOf("preflight=$preflightValue", "decision=$decisionValue"),
                        suggestedFix = "Use a decision JSON created for the same human review preflight.",
                    ),
                )
            }
        }

        requireNonBlank(humanReviewPreflightFile, "$.inputs.humanReviewPreflightFile", "HUMAN_REVIEW_PREFLIGHT_FILE_MISSING", "preflight file")
        requireNonBlank(humanReviewDecisionFile, "$.inputs.humanReviewDecisionFile", "HUMAN_REVIEW_DECISION_FILE_MISSING", "decision file")
        requireNonBlank(decision.decisionId, "$.humanReviewDecision.decisionId", "DECISION_ID_MISSING", "decision id")
        requireNonBlank(decision.reviewer, "$.humanReviewDecision.reviewer", "DECISION_REVIEWER_MISSING", "reviewer")
        requireNonBlank(decision.reviewerRole, "$.humanReviewDecision.reviewerRole", "DECISION_REVIEWER_ROLE_MISSING", "reviewer role")
        requireNonBlank(decision.decisionAt, "$.humanReviewDecision.decisionAt", "DECISION_AT_MISSING", "decision timestamp")

        requireMatch(
            preflightValue = preflight.preflightId,
            decisionValue = decision.preflightId,
            preflightPath = "$.humanReviewPreflight.preflightId",
            decisionPath = "$.humanReviewDecision.preflightId",
            missingRuleId = "DECISION_PREFLIGHT_ID_MISSING",
            mismatchRuleId = "DECISION_PREFLIGHT_ID_MISMATCH",
            label = "preflight id",
        )
        requireMatch(
            preflightValue = preflight.storyboardPacketReviewFile,
            decisionValue = decision.storyboardPacketReviewFile,
            preflightPath = "$.humanReviewPreflight.storyboardPacketReviewFile",
            decisionPath = "$.humanReviewDecision.storyboardPacketReviewFile",
            missingRuleId = "DECISION_REVIEW_PACKET_FILE_MISSING",
            mismatchRuleId = "DECISION_REVIEW_PACKET_FILE_MISMATCH",
            label = "storyboard packet review file",
            normalize = ::normalizePath,
        )
        requireMatch(
            preflightValue = preflight.storyboardPacketReviewId,
            decisionValue = decision.storyboardPacketReviewId,
            preflightPath = "$.humanReviewPreflight.storyboardPacketReviewId",
            decisionPath = "$.humanReviewDecision.storyboardPacketReviewId",
            missingRuleId = "DECISION_REVIEW_ID_MISSING",
            mismatchRuleId = "DECISION_REVIEW_ID_MISMATCH",
            label = "storyboard packet review id",
        )
        requireMatch(
            preflightValue = preflight.storyboardPacketDraftFile,
            decisionValue = decision.storyboardPacketDraftFile,
            preflightPath = "$.humanReviewPreflight.storyboardPacketDraftFile",
            decisionPath = "$.humanReviewDecision.storyboardPacketDraftFile",
            missingRuleId = "DECISION_DRAFT_FILE_MISSING",
            mismatchRuleId = "DECISION_DRAFT_FILE_MISMATCH",
            label = "storyboard packet draft file",
            normalize = ::normalizePath,
        )
        requireMatch(
            preflightValue = preflight.storyboardPacketDraftReportFile,
            decisionValue = decision.storyboardPacketDraftReportFile,
            preflightPath = "$.humanReviewPreflight.storyboardPacketDraftReportFile",
            decisionPath = "$.humanReviewDecision.storyboardPacketDraftReportFile",
            missingRuleId = "DECISION_DRAFT_REPORT_FILE_MISSING",
            mismatchRuleId = "DECISION_DRAFT_REPORT_FILE_MISMATCH",
            label = "storyboard packet draft report file",
            normalize = ::normalizePath,
        )
        requireMatch(
            preflightValue = preflight.storyboardPacketDraftId,
            decisionValue = decision.storyboardPacketDraftId,
            preflightPath = "$.humanReviewPreflight.storyboardPacketDraftId",
            decisionPath = "$.humanReviewDecision.storyboardPacketDraftId",
            missingRuleId = "DECISION_DRAFT_ID_MISSING",
            mismatchRuleId = "DECISION_DRAFT_ID_MISMATCH",
            label = "storyboard packet draft id",
        )
        requireMatch(
            preflightValue = preflight.storyboardPlanningBriefId,
            decisionValue = decision.storyboardPlanningBriefId,
            preflightPath = "$.humanReviewPreflight.storyboardPlanningBriefId",
            decisionPath = "$.humanReviewDecision.storyboardPlanningBriefId",
            missingRuleId = "DECISION_PLANNING_BRIEF_ID_MISSING",
            mismatchRuleId = "DECISION_PLANNING_BRIEF_ID_MISMATCH",
            label = "storyboard planning brief id",
        )
        requireMatch(
            preflightValue = preflight.acceptedScriptPacketId,
            decisionValue = decision.acceptedScriptPacketId,
            preflightPath = "$.humanReviewPreflight.acceptedScriptPacketId",
            decisionPath = "$.humanReviewDecision.acceptedScriptPacketId",
            missingRuleId = "DECISION_ACCEPTED_SCRIPT_PACKET_ID_MISSING",
            mismatchRuleId = "DECISION_ACCEPTED_SCRIPT_PACKET_ID_MISMATCH",
            label = "accepted script packet id",
        )
        requireMatch(
            preflightValue = preflight.scriptPacketDraftFile,
            decisionValue = decision.scriptPacketDraftFile,
            preflightPath = "$.humanReviewPreflight.scriptPacketDraftFile",
            decisionPath = "$.humanReviewDecision.scriptPacketDraftFile",
            missingRuleId = "DECISION_SCRIPT_PACKET_DRAFT_FILE_MISSING",
            mismatchRuleId = "DECISION_SCRIPT_PACKET_DRAFT_FILE_MISMATCH",
            label = "script packet draft file",
            normalize = ::normalizePath,
        )
        requireMatch(
            preflightValue = preflight.scriptPacketDraftContentSha256,
            decisionValue = decision.scriptPacketDraftContentSha256,
            preflightPath = "$.humanReviewPreflight.scriptPacketDraftContentSha256",
            decisionPath = "$.humanReviewDecision.scriptPacketDraftContentSha256",
            missingRuleId = "DECISION_SCRIPT_PACKET_DRAFT_HASH_MISSING",
            mismatchRuleId = "DECISION_SCRIPT_PACKET_DRAFT_HASH_MISMATCH",
            label = "script packet draft SHA-256",
            normalize = { it.trim().lowercase() },
        )
        return count
    }

    private fun validateDecisionSource(
        decision: StoryboardPacketHumanReviewDecision,
        findings: MutableList<StoryboardPacketHumanReviewDecisionIntakeFinding>,
    ) {
        val source = decision.decisionSource.trim().lowercase()
        if (source.isBlank()) {
            findings.add(
                finding(
                    ruleId = "DECISION_SOURCE_MISSING",
                    category = "human_decision",
                    severity = "CRITICAL",
                    path = "$.humanReviewDecision.decisionSource",
                    message = "Human review decision must declare a human/control/user decision source.",
                    suggestedFix = "Set decisionSource to human, 01_control, or user.",
                ),
            )
        } else if (source in disallowedSources || source !in allowedSources) {
            findings.add(
                finding(
                    ruleId = "DECISION_SOURCE_NOT_HUMAN",
                    category = "human_decision",
                    severity = "CRITICAL",
                    path = "$.humanReviewDecision.decisionSource",
                    message = "Human review decision source cannot be automatic, model, generated, or unknown.",
                    evidence = listOf(decision.decisionSource),
                    suggestedFix = "Use an explicit external human/control/user decision source.",
                ),
            )
        }
    }

    private fun validateDecisionStatus(
        normalizedStatus: String,
        rawStatus: String,
        findings: MutableList<StoryboardPacketHumanReviewDecisionIntakeFinding>,
    ) {
        if (normalizedStatus.isBlank()) {
            findings.add(
                finding(
                    ruleId = "DECISION_STATUS_MISSING",
                    category = "human_decision",
                    severity = "CRITICAL",
                    path = "$.humanReviewDecision.decisionStatus",
                    message = "Human review decision status must be declared.",
                    suggestedFix = "Use ALLOW_ASSET_CANDIDATE_PREFLIGHT, REQUEST_CHANGES, or REJECT.",
                ),
            )
        } else if (normalizedStatus !in allowedStatuses) {
            findings.add(
                finding(
                    ruleId = "DECISION_STATUS_UNKNOWN",
                    category = "human_decision",
                    severity = "CRITICAL",
                    path = "$.humanReviewDecision.decisionStatus",
                    message = "Human review decision status is outside the narrow allowed vocabulary.",
                    evidence = listOf(rawStatus),
                    suggestedFix = "Use ALLOW_ASSET_CANDIDATE_PREFLIGHT, REQUEST_CHANGES, or REJECT.",
                ),
            )
        }
    }

    private fun validateChecklist(
        decision: StoryboardPacketHumanReviewDecision,
        allowDownstream: Boolean,
        findings: MutableList<StoryboardPacketHumanReviewDecisionIntakeFinding>,
    ): Int {
        if (decision.checklistItems.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "HUMAN_CHECKLIST_MISSING",
                    category = "human_decision",
                    severity = "CRITICAL",
                    path = "$.humanReviewDecision.checklistItems",
                    message = "Human review decision must include checklist item results.",
                    suggestedFix = "Record the human checklist results before allowing downstream work.",
                ),
            )
            return 0
        }
        var passed = 0
        decision.checklistItems.forEachIndexed { index, item ->
            if (item.itemId.isBlank() || item.category.isBlank() || item.result.isBlank()) {
                findings.add(
                    finding(
                        ruleId = "HUMAN_CHECKLIST_ITEM_INCOMPLETE",
                        category = "human_decision",
                        severity = "CRITICAL",
                        path = "$.humanReviewDecision.checklistItems[$index]",
                        message = "Human review checklist item requires itemId, category, and result.",
                        suggestedFix = "Complete every checklist item in the human decision JSON.",
                    ),
                )
            }
            val result = item.result.trim().uppercase()
            if (result in positiveChecklistResults) {
                passed += 1
            } else if (allowDownstream) {
                findings.add(
                    finding(
                        ruleId = "HUMAN_CHECKLIST_ITEM_NOT_ACCEPTED",
                        category = "human_decision",
                        severity = "CRITICAL",
                        path = "$.humanReviewDecision.checklistItems[$index].result",
                        message = "Allowing asset candidate preflight requires every human checklist item to pass.",
                        evidence = listOf(item.result),
                        suggestedFix = "Use REQUEST_CHANGES or REJECT until every required checklist item is accepted.",
                    ),
                )
            }
        }
        if (allowDownstream) {
            validateRequiredChecklistCoverage(decision, findings)
        }
        return passed
    }

    private fun validateRequiredChecklistCoverage(
        decision: StoryboardPacketHumanReviewDecision,
        findings: MutableList<StoryboardPacketHumanReviewDecisionIntakeFinding>,
    ) {
        val acceptedCategories = decision.checklistItems
            .filter { it.result.trim().uppercase() in positiveChecklistResults }
            .map { it.category.trim().lowercase() }
            .toSet()
        val missingCategories = requiredChecklistCategories
            .filterNot { it in acceptedCategories }
            .toMutableList()
        if (assetChecklistCategories.none { it in acceptedCategories }) {
            missingCategories += "asset_candidates_or_assets"
        }
        if (missingCategories.isNotEmpty()) {
            findings.add(
                finding(
                    ruleId = "HUMAN_CHECKLIST_REQUIRED_CATEGORY_MISSING",
                    category = "human_decision",
                    severity = "CRITICAL",
                    path = "$.humanReviewDecision.checklistItems",
                    message = "Allowing asset candidate preflight requires accepted checklist coverage for scene lock, continuity, knowledge boundary, asset candidates/assets, and downstream boundary.",
                    evidence = missingCategories,
                    suggestedFix = "Record positive human checklist results for every required category before allowing asset candidate preflight.",
                ),
            )
        }
    }

    private fun validateBlockingIssues(
        decision: StoryboardPacketHumanReviewDecision,
        allowDownstream: Boolean,
        findings: MutableList<StoryboardPacketHumanReviewDecisionIntakeFinding>,
    ): Int {
        var unresolvedBlockingCount = 0
        decision.blockingIssues.forEachIndexed { index, issue ->
            val status = issue.status.trim().uppercase()
            val severity = issue.severity.trim().uppercase()
            val unresolvedBlocking = severity in blockingIssueSeverities && status !in resolvedIssueStatuses
            if (unresolvedBlocking) {
                unresolvedBlockingCount += 1
                if (allowDownstream) {
                    findings.add(
                        finding(
                            ruleId = "UNRESOLVED_BLOCKING_ISSUE_PRESENT",
                            category = "human_decision",
                            severity = "CRITICAL",
                            path = "$.humanReviewDecision.blockingIssues[$index]",
                            message = "Allowing asset candidate preflight requires no unresolved blocking issue.",
                            evidence = listOf(issue.issueId, issue.severity, issue.status).filter { it.isNotBlank() },
                            suggestedFix = "Resolve the blocking issue or use REQUEST_CHANGES / REJECT.",
                        ),
                    )
                }
            }
        }
        return unresolvedBlockingCount
    }

    private fun validateForbiddenClaims(
        decision: StoryboardPacketHumanReviewDecision,
        findings: MutableList<StoryboardPacketHumanReviewDecisionIntakeFinding>,
    ): Int {
        var count = 0
        collectTextFields(decision).forEach { field ->
            forbiddenClaimPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings.add(
                        finding(
                            ruleId = "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN",
                            category = "safety_boundary",
                            severity = "CRITICAL",
                            path = field.path,
                            message = "Human review decision intake cannot claim generated media, asset approval, production lock, canon application, or runtime scene creation.",
                            evidence = listOf(field.value),
                            suggestedFix = "Keep this decision limited to allowing asset candidate preflight, requesting changes, or rejecting.",
                        ),
                    )
                }
            }
        }
        return count
    }

    private fun collectTextFields(decision: StoryboardPacketHumanReviewDecision): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) {
            if (value.isNotBlank()) fields += TextField(path, value)
        }
        add("$.humanReviewDecision.decisionId", decision.decisionId)
        add("$.humanReviewDecision.decisionSource", decision.decisionSource)
        add("$.humanReviewDecision.reviewer", decision.reviewer)
        add("$.humanReviewDecision.reviewerRole", decision.reviewerRole)
        add("$.humanReviewDecision.decisionStatus", decision.decisionStatus)
        add("$.humanReviewDecision.notes", decision.notes)
        decision.checklistItems.forEachIndexed { index, item ->
            add("$.humanReviewDecision.checklistItems[$index].itemId", item.itemId)
            add("$.humanReviewDecision.checklistItems[$index].category", item.category)
            add("$.humanReviewDecision.checklistItems[$index].result", item.result)
            add("$.humanReviewDecision.checklistItems[$index].note", item.note)
        }
        decision.blockingIssues.forEachIndexed { index, issue ->
            add("$.humanReviewDecision.blockingIssues[$index].issueId", issue.issueId)
            add("$.humanReviewDecision.blockingIssues[$index].severity", issue.severity)
            add("$.humanReviewDecision.blockingIssues[$index].status", issue.status)
            add("$.humanReviewDecision.blockingIssues[$index].description", issue.description)
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
    ): StoryboardPacketHumanReviewDecisionIntakeFinding =
        StoryboardPacketHumanReviewDecisionIntakeFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            path = path,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
        )

    private fun String.normalizedVerdict(): String = trim().uppercase()

    private fun normalizePath(value: String): String = value.trim().replace('\\', '/')

    private data class TextField(val path: String, val value: String)
}
