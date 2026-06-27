package com.localnovel.engine.service

import com.localnovel.engine.model.ImageCandidatePromptItem
import com.localnovel.engine.model.ImageCandidateProviderHandoffDecision
import com.localnovel.engine.model.ImageCandidateProviderHandoffDecisionIntakeEvidence
import com.localnovel.engine.model.ImageCandidateProviderHandoffDecisionIntakeFinding
import com.localnovel.engine.model.ImageCandidateProviderHandoffDecisionIntakeReport
import com.localnovel.engine.model.ImageCandidateProviderHandoffPreflightReport

class ImageCandidateProviderHandoffDecisionIntakeService {
    private val requiredPreflightStep = "image_candidate_manual_provider_handoff_ready"
    private val downstreamStatus = "ALLOW_MANUAL_PROVIDER_HANDOFF_PACKET"
    private val nextAllowedDownstreamStep = "image_candidate_manual_provider_handoff_packet"
    private val allowedSources = setOf("human", "user", "01_control")
    private val disallowedSources = setOf("auto", "model", "generated")
    private val allowedStatuses = setOf(downstreamStatus, "REQUEST_CHANGES", "REJECT")
    private val positiveChecklistResults = setOf("PASS", "PASSED", "ACCEPTED", "OK", "YES")
    private val requiredChecklistCategories = setOf(
        "prompt_safety",
        "source_binding",
        "do_not_show_boundary",
        "no_provider_job_yet",
        "no_output_path",
        "no_approval_lock_canon",
    )
    private val resolvedIssueStatuses = setOf("RESOLVED", "CLOSED", "ADDRESSED")
    private val blockingIssueSeverities = setOf("BLOCKING", "CRITICAL", "MAJOR")
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
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
        Regex("""(?i)\bprompt\s+submitted\b"""),
        Regex("""(?i)\bsubmitted\s+prompt\b"""),
        Regex("""(?i)\bimage\s+generated\b"""),
        Regex("""(?i)\bgenerated\s+image\b"""),
        Regex("""(?i)\bvideo\s+generated\b"""),
        Regex("""(?i)\basset\s+approved\b"""),
        Regex("""(?i)\bassets\s+approved\b"""),
        Regex("""(?i)\basset\s+approval\s+complete\b"""),
        Regex("""(?i)\bproduction\s+locked\b"""),
        Regex("""(?i)\bcanon\s+applied\b"""),
        Regex("""(?i)\bruntime\s+scene\s+created\b"""),
        Regex("\u63d0\u793a\u5df2\u63d0\u4ea4|\u5716\u7247\u5df2\u751f\u6210|\u5f71\u7247\u5df2\u751f\u6210|\u7d20\u6750\u5df2\u6838\u51c6|\u7d20\u6750\u6838\u51c6\u5b8c\u6210|\u88fd\u4f5c\u9396\u5b9a|\u6b63\u5178\u5df2\u5957\u7528|\u57f7\u884c\u671f\u5834\u666f\u5df2\u5efa\u7acb"),
    )
    private val authorOnlyPatterns = listOf(
        Regex("""(?i)\bauthor[-_\s]?only\b"""),
        Regex("""(?i)\bdirector[-_\s]?only\b"""),
        Regex("""(?i)\bhidden\s+truth\b"""),
        Regex("""(?i)\bsecret\s+cause\b"""),
        Regex("""(?i)\bhidden\s+motive\b"""),
        Regex("""(?i)\bfuture\s+reveal\b"""),
        Regex("\u4f5c\u8005\u5c08\u7528|\u96b1\u85cf\u771f\u76f8|\u79d8\u5bc6\u539f\u56e0|\u96b1\u85cf\u52d5\u6a5f|\u672a\u4f86\u63ed\u9732"),
    )

    fun validate(
        providerHandoffPreflight: ImageCandidateProviderHandoffPreflightReport,
        providerHandoffDecision: ImageCandidateProviderHandoffDecision,
        providerHandoffPreflightFile: String = "",
        providerHandoffDecisionFile: String = "",
    ): ImageCandidateProviderHandoffDecisionIntakeReport {
        val findings = mutableListOf<ImageCandidateProviderHandoffDecisionIntakeFinding>()
        validatePreflight(providerHandoffPreflight, findings)
        val sourceCount = validateSourceBinding(providerHandoffPreflight, providerHandoffDecision, providerHandoffPreflightFile, providerHandoffDecisionFile, findings)
        validateDecisionSource(providerHandoffDecision, findings)
        val normalizedStatus = providerHandoffDecision.decisionStatus.trim().uppercase()
        validateDecisionStatus(normalizedStatus, providerHandoffDecision.decisionStatus, findings)
        val allowDownstream = normalizedStatus == downstreamStatus
        val checklistPassedCount = validateChecklist(providerHandoffDecision, allowDownstream, findings)
        val unresolvedBlockingCount = validateBlockingIssues(providerHandoffDecision, allowDownstream, findings)
        validatePromptItems(providerHandoffPreflight.promptItems, allowDownstream, findings)
        val forbiddenOutputFieldCount = validateForbiddenOutputFields(providerHandoffPreflight.promptItems, findings)
        val forbiddenStatusCount = scanText(providerHandoffDecision, providerHandoffPreflight.promptItems, forbiddenStatusPatterns, "PROMPT_STATUS_UPGRADE_FORBIDDEN", "provider_handoff_boundary", "Provider handoff decision cannot contain approved, locked, final, review-ready, production-ready, approved-reference, or production-locked wording.", findings)
        val forbiddenClaimCount = scanText(providerHandoffDecision, providerHandoffPreflight.promptItems, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", "Provider handoff decision cannot claim prompt submission, generated media, asset approval, production lock, canon application, or runtime scene creation.", findings)
        val authorOnlyLeakCount = scanText(providerHandoffDecision, providerHandoffPreflight.promptItems, authorOnlyPatterns, "AUTHOR_ONLY_PROMPT_TEXT_FORBIDDEN", "prompt_safety", "Provider handoff decision cannot carry author-only, hidden truth, secret cause, hidden motive, or future reveal text.", findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val downstreamAllowed = verdict == "PASS" && allowDownstream
        val blockedReason = if (verdict == "FAIL") {
            findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId }
        } else {
            ""
        }

        return ImageCandidateProviderHandoffDecisionIntakeReport(
            verdict = verdict,
            decisionId = providerHandoffDecision.decisionId,
            preflightId = providerHandoffDecision.preflightId,
            providerHandoffPreflightFile = providerHandoffPreflightFile,
            providerHandoffDecisionFile = providerHandoffDecisionFile,
            imageCandidatePromptValidationReportFile = providerHandoffPreflight.imageCandidatePromptValidationReportFile,
            imageCandidatePromptPreparationFile = providerHandoffPreflight.imageCandidatePromptPreparationFile,
            preparationId = providerHandoffPreflight.preparationId,
            imageCandidateBriefFile = providerHandoffPreflight.imageCandidateBriefFile,
            imageCandidateBriefId = providerHandoffPreflight.imageCandidateBriefId,
            imageCandidatePreflightFile = providerHandoffPreflight.imageCandidatePreflightFile,
            imageCandidatePreflightId = providerHandoffPreflight.imageCandidatePreflightId,
            assetCandidateHumanReviewDecisionIntakeFile = providerHandoffPreflight.assetCandidateHumanReviewDecisionIntakeFile,
            assetDecisionId = providerHandoffPreflight.decisionId,
            assetCandidateReviewPreflightFile = providerHandoffPreflight.assetCandidateReviewPreflightFile,
            assetCandidateReviewPreflightId = providerHandoffPreflight.assetCandidateReviewPreflightId,
            assetCandidateDraftValidationReportFile = providerHandoffPreflight.assetCandidateDraftValidationReportFile,
            assetCandidateDraftFile = providerHandoffPreflight.assetCandidateDraftFile,
            assetCandidateDraftId = providerHandoffPreflight.assetCandidateDraftId,
            assetCandidateBriefFile = providerHandoffPreflight.assetCandidateBriefFile,
            assetCandidateBriefId = providerHandoffPreflight.assetCandidateBriefId,
            assetCandidatePreflightFile = providerHandoffPreflight.assetCandidatePreflightFile,
            assetCandidatePreflightId = providerHandoffPreflight.assetCandidatePreflightId,
            humanReviewDecisionIntakeFile = providerHandoffPreflight.humanReviewDecisionIntakeFile,
            humanReviewDecisionId = providerHandoffPreflight.humanReviewDecisionId,
            storyboardPacketReviewFile = providerHandoffPreflight.storyboardPacketReviewFile,
            storyboardPacketReviewId = providerHandoffPreflight.storyboardPacketReviewId,
            acceptedScriptPacketId = providerHandoffPreflight.acceptedScriptPacketId,
            scriptPacketDraftFile = providerHandoffPreflight.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = providerHandoffPreflight.scriptPacketDraftContentSha256,
            decisionStatus = providerHandoffDecision.decisionStatus,
            decisionAllowedDownstream = downstreamAllowed,
            promptItems = if (downstreamAllowed) providerHandoffPreflight.promptItems else emptyList(),
            nextAllowedDownstreamStep = if (downstreamAllowed) nextAllowedDownstreamStep else "",
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = ImageCandidateProviderHandoffDecisionIntakeEvidence(
                providerHandoffPreflightVerdict = providerHandoffPreflight.verdict,
                providerHandoffPreflightNextAllowedDownstreamStep = providerHandoffPreflight.nextAllowedDownstreamStep,
                sourceBindingFieldCount = sourceCount,
                decisionSource = providerHandoffDecision.decisionSource,
                decisionStatus = providerHandoffDecision.decisionStatus,
                checklistItemCount = providerHandoffDecision.checklistItems.size,
                checklistPassedCount = checklistPassedCount,
                unresolvedBlockingIssueCount = unresolvedBlockingCount,
                promptItemCount = providerHandoffPreflight.promptItems.size,
                authorOnlyLeakCount = authorOnlyLeakCount,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
                forbiddenOutputFieldCount = forbiddenOutputFieldCount,
            ),
        )
    }

    private fun validatePreflight(
        preflight: ImageCandidateProviderHandoffPreflightReport,
        findings: MutableList<ImageCandidateProviderHandoffDecisionIntakeFinding>,
    ) {
        if (preflight.verdict.trim().uppercase() != "PASS") {
            findings.add(finding("PROVIDER_HANDOFF_PREFLIGHT_NOT_PASS", "upstream", "CRITICAL", "$.providerHandoffPreflight.verdict", "Provider handoff decision requires a PASS provider handoff preflight.", evidence = listOf(preflight.verdict), suggestedFix = "Use the PASS provider handoff preflight."))
        }
        if (preflight.nextAllowedDownstreamStep != requiredPreflightStep) {
            findings.add(finding("PROVIDER_HANDOFF_PREFLIGHT_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.providerHandoffPreflight.nextAllowedDownstreamStep", "Provider handoff decision requires nextAllowedDownstreamStep=image_candidate_manual_provider_handoff_ready.", evidence = listOf(preflight.nextAllowedDownstreamStep), suggestedFix = "Use the preflight that explicitly allows manual provider handoff readiness."))
        }
    }

    private fun validateSourceBinding(
        preflight: ImageCandidateProviderHandoffPreflightReport,
        decision: ImageCandidateProviderHandoffDecision,
        preflightFile: String,
        decisionFile: String,
        findings: MutableList<ImageCandidateProviderHandoffDecisionIntakeFinding>,
    ): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) {
                findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Provider handoff decision intake requires nonblank $label.", suggestedFix = "Use matching provider handoff preflight and human decision JSON."))
            } else {
                count += 1
            }
        }
        fun requireMatch(preflightValue: String, decisionValue: String, preflightPath: String, decisionPath: String, missingRuleId: String, mismatchRuleId: String, label: String, normalize: (String) -> String = { it.trim() }) {
            requireNonBlank(preflightValue, preflightPath, "${missingRuleId}_PREFLIGHT", "$label in preflight")
            requireNonBlank(decisionValue, decisionPath, missingRuleId, "$label in decision")
            val left = normalize(preflightValue)
            val right = normalize(decisionValue)
            if (left.isNotBlank() && right.isNotBlank() && left != right) {
                findings.add(finding(mismatchRuleId, "source_linkage", "CRITICAL", decisionPath, "Provider handoff decision source binding mismatch for $label.", evidence = listOf("preflight=$preflightValue", "decision=$decisionValue"), suggestedFix = "Use a decision JSON created for this provider handoff preflight."))
            }
        }

        requireNonBlank(preflightFile, "$.inputs.providerHandoffPreflightFile", "PROVIDER_HANDOFF_PREFLIGHT_FILE_MISSING", "provider handoff preflight file")
        requireNonBlank(decisionFile, "$.inputs.providerHandoffDecisionFile", "PROVIDER_HANDOFF_DECISION_FILE_MISSING", "provider handoff decision file")
        requireNonBlank(decision.decisionId, "$.providerHandoffDecision.decisionId", "DECISION_ID_MISSING", "decision id")
        requireNonBlank(decision.reviewer, "$.providerHandoffDecision.reviewer", "DECISION_REVIEWER_MISSING", "reviewer")
        requireNonBlank(decision.reviewerRole, "$.providerHandoffDecision.reviewerRole", "DECISION_REVIEWER_ROLE_MISSING", "reviewer role")
        requireNonBlank(decision.decisionAt, "$.providerHandoffDecision.decisionAt", "DECISION_AT_MISSING", "decision timestamp")
        requireNonBlank(decision.providerHandoffPreflightFile, "$.providerHandoffDecision.providerHandoffPreflightFile", "DECISION_PREFLIGHT_FILE_MISSING", "preflight file in decision")
        if (preflightFile.isNotBlank() && decision.providerHandoffPreflightFile.isNotBlank() && normalizePath(preflightFile) != normalizePath(decision.providerHandoffPreflightFile)) {
            findings.add(finding("DECISION_PREFLIGHT_FILE_MISMATCH", "source_linkage", "CRITICAL", "$.providerHandoffDecision.providerHandoffPreflightFile", "Decision must point to the same provider handoff preflight file.", evidence = listOf("input=$preflightFile", "decision=${decision.providerHandoffPreflightFile}"), suggestedFix = "Use the decision JSON for this preflight file."))
        }
        requireMatch(preflight.preflightId, decision.preflightId, "$.providerHandoffPreflight.preflightId", "$.providerHandoffDecision.preflightId", "DECISION_PREFLIGHT_ID_MISSING", "DECISION_PREFLIGHT_ID_MISMATCH", "preflight id")
        requireMatch(preflight.imageCandidatePromptValidationReportFile, decision.imageCandidatePromptValidationReportFile, "$.providerHandoffPreflight.imageCandidatePromptValidationReportFile", "$.providerHandoffDecision.imageCandidatePromptValidationReportFile", "DECISION_PROMPT_VALIDATION_REPORT_FILE_MISSING", "DECISION_PROMPT_VALIDATION_REPORT_FILE_MISMATCH", "prompt validation report file", ::normalizePath)
        requireMatch(preflight.imageCandidatePromptPreparationFile, decision.imageCandidatePromptPreparationFile, "$.providerHandoffPreflight.imageCandidatePromptPreparationFile", "$.providerHandoffDecision.imageCandidatePromptPreparationFile", "DECISION_PROMPT_PREPARATION_FILE_MISSING", "DECISION_PROMPT_PREPARATION_FILE_MISMATCH", "prompt preparation file", ::normalizePath)
        requireMatch(preflight.preparationId, decision.preparationId, "$.providerHandoffPreflight.preparationId", "$.providerHandoffDecision.preparationId", "DECISION_PROMPT_PREPARATION_ID_MISSING", "DECISION_PROMPT_PREPARATION_ID_MISMATCH", "prompt preparation id")
        requireMatch(preflight.imageCandidateBriefFile, decision.imageCandidateBriefFile, "$.providerHandoffPreflight.imageCandidateBriefFile", "$.providerHandoffDecision.imageCandidateBriefFile", "DECISION_IMAGE_CANDIDATE_BRIEF_FILE_MISSING", "DECISION_IMAGE_CANDIDATE_BRIEF_FILE_MISMATCH", "image candidate brief file", ::normalizePath)
        requireMatch(preflight.imageCandidateBriefId, decision.imageCandidateBriefId, "$.providerHandoffPreflight.imageCandidateBriefId", "$.providerHandoffDecision.imageCandidateBriefId", "DECISION_IMAGE_CANDIDATE_BRIEF_ID_MISSING", "DECISION_IMAGE_CANDIDATE_BRIEF_ID_MISMATCH", "image candidate brief id")
        requireMatch(preflight.imageCandidatePreflightFile, decision.imageCandidatePreflightFile, "$.providerHandoffPreflight.imageCandidatePreflightFile", "$.providerHandoffDecision.imageCandidatePreflightFile", "DECISION_IMAGE_CANDIDATE_PREFLIGHT_FILE_MISSING", "DECISION_IMAGE_CANDIDATE_PREFLIGHT_FILE_MISMATCH", "image candidate preflight file", ::normalizePath)
        requireMatch(preflight.imageCandidatePreflightId, decision.imageCandidatePreflightId, "$.providerHandoffPreflight.imageCandidatePreflightId", "$.providerHandoffDecision.imageCandidatePreflightId", "DECISION_IMAGE_CANDIDATE_PREFLIGHT_ID_MISSING", "DECISION_IMAGE_CANDIDATE_PREFLIGHT_ID_MISMATCH", "image candidate preflight id")
        requireNonBlank(preflight.acceptedScriptPacketId, "$.providerHandoffPreflight.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(preflight.scriptPacketDraftFile, "$.providerHandoffPreflight.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(preflight.scriptPacketDraftContentSha256, "$.providerHandoffPreflight.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        if (preflight.scriptPacketDraftContentSha256.isNotBlank() && !sha256Regex.matches(preflight.scriptPacketDraftContentSha256.trim().lowercase())) {
            findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.providerHandoffPreflight.scriptPacketDraftContentSha256", "Provider handoff decision requires a valid lowercase SHA-256 script packet draft hash.", evidence = listOf(preflight.scriptPacketDraftContentSha256), suggestedFix = "Use the hash carried by the preflight."))
        }
        return count
    }

    private fun validateDecisionSource(decision: ImageCandidateProviderHandoffDecision, findings: MutableList<ImageCandidateProviderHandoffDecisionIntakeFinding>) {
        val source = decision.decisionSource.trim().lowercase()
        if (source.isBlank()) {
            findings.add(finding("DECISION_SOURCE_MISSING", "human_decision", "CRITICAL", "$.providerHandoffDecision.decisionSource", "Provider handoff decision must declare a human/control/user source.", suggestedFix = "Set decisionSource to human, user, or 01_control."))
        } else if (source in disallowedSources || source !in allowedSources) {
            findings.add(finding("DECISION_SOURCE_NOT_HUMAN", "human_decision", "CRITICAL", "$.providerHandoffDecision.decisionSource", "Provider handoff decision source cannot be automatic, model, generated, or unknown.", evidence = listOf(decision.decisionSource), suggestedFix = "Use an explicit external human/control/user decision source."))
        }
    }

    private fun validateDecisionStatus(normalizedStatus: String, rawStatus: String, findings: MutableList<ImageCandidateProviderHandoffDecisionIntakeFinding>) {
        if (normalizedStatus.isBlank()) {
            findings.add(finding("DECISION_STATUS_MISSING", "human_decision", "CRITICAL", "$.providerHandoffDecision.decisionStatus", "Provider handoff decision status must be declared.", suggestedFix = "Use ALLOW_MANUAL_PROVIDER_HANDOFF_PACKET, REQUEST_CHANGES, or REJECT."))
        } else if (normalizedStatus !in allowedStatuses) {
            findings.add(finding("DECISION_STATUS_UNKNOWN", "human_decision", "CRITICAL", "$.providerHandoffDecision.decisionStatus", "Provider handoff decision status is outside the narrow allowed vocabulary.", evidence = listOf(rawStatus), suggestedFix = "Use ALLOW_MANUAL_PROVIDER_HANDOFF_PACKET, REQUEST_CHANGES, or REJECT."))
        }
    }

    private fun validateChecklist(decision: ImageCandidateProviderHandoffDecision, allowDownstream: Boolean, findings: MutableList<ImageCandidateProviderHandoffDecisionIntakeFinding>): Int {
        if (decision.checklistItems.isEmpty()) {
            findings.add(finding("HUMAN_CHECKLIST_MISSING", "human_decision", "CRITICAL", "$.providerHandoffDecision.checklistItems", "Provider handoff decision must include checklist item results.", suggestedFix = "Record human checklist results before allowing packet creation."))
            return 0
        }
        var passed = 0
        decision.checklistItems.forEachIndexed { index, item ->
            if (item.itemId.isBlank() || item.category.isBlank() || item.result.isBlank()) {
                findings.add(finding("HUMAN_CHECKLIST_ITEM_INCOMPLETE", "human_decision", "CRITICAL", "$.providerHandoffDecision.checklistItems[$index]", "Human checklist item requires itemId, category, and result.", suggestedFix = "Complete every checklist item."))
            }
            val result = item.result.trim().uppercase()
            if (result in positiveChecklistResults) {
                passed += 1
            } else if (allowDownstream) {
                findings.add(finding("HUMAN_CHECKLIST_ITEM_NOT_ACCEPTED", "human_decision", "CRITICAL", "$.providerHandoffDecision.checklistItems[$index].result", "Allowing packet creation requires every checklist item to pass.", evidence = listOf(item.result), suggestedFix = "Use REQUEST_CHANGES or REJECT until every required item is accepted."))
            }
        }
        if (allowDownstream) {
            val accepted = decision.checklistItems.filter { it.result.trim().uppercase() in positiveChecklistResults }.map { it.category.trim().lowercase() }.toSet()
            val missing = requiredChecklistCategories.filterNot { it in accepted }
            if (missing.isNotEmpty()) {
                findings.add(finding("HUMAN_CHECKLIST_REQUIRED_CATEGORY_MISSING", "human_decision", "CRITICAL", "$.providerHandoffDecision.checklistItems", "Allowing provider handoff packet creation requires accepted checklist coverage for prompt safety, source binding, do-not-show boundary, no provider job yet, no output path, and no approval/lock/canon.", evidence = missing, suggestedFix = "Record positive human checklist results for every required category."))
            }
        }
        return passed
    }

    private fun validateBlockingIssues(decision: ImageCandidateProviderHandoffDecision, allowDownstream: Boolean, findings: MutableList<ImageCandidateProviderHandoffDecisionIntakeFinding>): Int {
        var count = 0
        decision.blockingIssues.forEachIndexed { index, issue ->
            val unresolvedBlocking = issue.severity.trim().uppercase() in blockingIssueSeverities && issue.status.trim().uppercase() !in resolvedIssueStatuses
            if (unresolvedBlocking) {
                count += 1
                if (allowDownstream) {
                    findings.add(finding("UNRESOLVED_BLOCKING_ISSUE_PRESENT", "human_decision", "CRITICAL", "$.providerHandoffDecision.blockingIssues[$index]", "Allowing packet creation requires no unresolved blocking issue.", evidence = listOf(issue.issueId, issue.severity, issue.status).filter { it.isNotBlank() }, suggestedFix = "Resolve the blocking issue or use REQUEST_CHANGES / REJECT."))
                }
            }
        }
        return count
    }

    private fun validatePromptItems(items: List<ImageCandidatePromptItem>, allowDownstream: Boolean, findings: MutableList<ImageCandidateProviderHandoffDecisionIntakeFinding>) {
        if (allowDownstream && items.isEmpty()) {
            findings.add(finding("PROMPT_ITEMS_MISSING", "provider_handoff_boundary", "CRITICAL", "$.providerHandoffPreflight.promptItems", "Allowing packet creation requires validated prompt items.", suggestedFix = "Use a provider handoff preflight with prompt items."))
        }
        items.forEachIndexed { index, item ->
            if (item.promptItemId.isBlank() || item.sourceRequirementId.isBlank() || item.sourceCandidateId.isBlank() || item.assetType.isBlank() || item.positivePromptDraft.isBlank()) {
                findings.add(finding("PROMPT_ITEM_INCOMPLETE", "provider_handoff_boundary", "CRITICAL", "$.providerHandoffPreflight.promptItems[$index]", "Prompt item requires source fields and positive prompt draft.", suggestedFix = "Use complete validated prompt items."))
            }
            if (item.negativePromptDraft.isBlank() && item.doNotShow.isEmpty()) {
                findings.add(finding("NEGATIVE_PROMPT_OR_DO_NOT_SHOW_MISSING", "provider_handoff_boundary", "CRITICAL", "$.providerHandoffPreflight.promptItems[$index]", "Prompt item requires negative prompt or do-not-show boundary.", suggestedFix = "Keep do-not-show boundary attached."))
            }
            if (item.promptSafetyNotes.isEmpty()) {
                findings.add(finding("PROMPT_SAFETY_NOTES_MISSING", "provider_handoff_boundary", "CRITICAL", "$.providerHandoffPreflight.promptItems[$index].promptSafetyNotes", "Prompt item requires prompt safety notes.", suggestedFix = "Keep safety notes attached."))
            }
        }
    }

    private fun validateForbiddenOutputFields(items: List<ImageCandidatePromptItem>, findings: MutableList<ImageCandidateProviderHandoffDecisionIntakeFinding>): Int {
        var count = 0
        items.forEachIndexed { index, item ->
            listOf(
                Triple(item.providerName, "providerName", "PROVIDER_NAME_FORBIDDEN"),
                Triple(item.generationApi, "generationApi", "GENERATION_API_FORBIDDEN"),
                Triple(item.providerJobId, "providerJobId", "PROVIDER_JOB_ID_FORBIDDEN"),
                Triple(item.imageOutputPath, "imageOutputPath", "IMAGE_OUTPUT_PATH_FORBIDDEN"),
            ).forEach { (value, field, ruleId) ->
                if (value.isNotBlank()) {
                    count += 1
                    findings.add(finding(ruleId, "safety_boundary", "CRITICAL", "$.providerHandoffPreflight.promptItems[$index].$field", "Provider handoff decision intake cannot carry provider, generation API, job, or image output path information.", evidence = listOf(value), suggestedFix = "Remove provider/job/output details."))
                }
            }
        }
        return count
    }

    private fun scanText(decision: ImageCandidateProviderHandoffDecision, items: List<ImageCandidatePromptItem>, patterns: List<Regex>, ruleId: String, category: String, message: String, findings: MutableList<ImageCandidateProviderHandoffDecisionIntakeFinding>): Int {
        var count = 0
        collectTextFields(decision, items).forEach { field ->
            patterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings.add(finding(ruleId, category, "CRITICAL", field.path, message, evidence = listOf(field.value), suggestedFix = "Keep this decision limited to manual packet creation only."))
                }
            }
        }
        return count
    }

    private fun collectTextFields(decision: ImageCandidateProviderHandoffDecision, items: List<ImageCandidatePromptItem>): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) {
            if (value.isNotBlank()) fields += TextField(path, value)
        }
        add("$.providerHandoffDecision.decisionSource", decision.decisionSource)
        add("$.providerHandoffDecision.reviewer", decision.reviewer)
        add("$.providerHandoffDecision.reviewerRole", decision.reviewerRole)
        add("$.providerHandoffDecision.decisionStatus", decision.decisionStatus)
        add("$.providerHandoffDecision.notes", decision.notes)
        decision.checklistItems.forEachIndexed { index, item ->
            add("$.providerHandoffDecision.checklistItems[$index].category", item.category)
            add("$.providerHandoffDecision.checklistItems[$index].result", item.result)
            add("$.providerHandoffDecision.checklistItems[$index].note", item.note)
        }
        decision.blockingIssues.forEachIndexed { index, issue ->
            add("$.providerHandoffDecision.blockingIssues[$index].description", issue.description)
            add("$.providerHandoffDecision.blockingIssues[$index].status", issue.status)
        }
        items.forEachIndexed { index, item ->
            add("$.providerHandoffPreflight.promptItems[$index].assetType", item.assetType)
            add("$.providerHandoffPreflight.promptItems[$index].positivePromptDraft", item.positivePromptDraft)
            add("$.providerHandoffPreflight.promptItems[$index].negativePromptDraft", item.negativePromptDraft)
            item.doNotShow.forEachIndexed { itemIndex, value -> add("$.providerHandoffPreflight.promptItems[$index].doNotShow[$itemIndex]", value) }
            item.promptSafetyNotes.forEachIndexed { itemIndex, value -> add("$.providerHandoffPreflight.promptItems[$index].promptSafetyNotes[$itemIndex]", value) }
            item.publicVisualRequirements.forEachIndexed { itemIndex, value -> add("$.providerHandoffPreflight.promptItems[$index].publicVisualRequirements[$itemIndex]", value) }
        }
        return fields
    }

    private fun finding(ruleId: String, category: String, severity: String, path: String, message: String, evidence: List<String> = emptyList(), suggestedFix: String = ""): ImageCandidateProviderHandoffDecisionIntakeFinding =
        ImageCandidateProviderHandoffDecisionIntakeFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private fun normalizePath(value: String): String = value.trim().replace('\\', '/')

    private data class TextField(val path: String, val value: String)
}
