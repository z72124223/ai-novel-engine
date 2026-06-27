package com.localnovel.engine.service

import com.localnovel.engine.model.CandidateReferenceHumanAcceptanceDecision
import com.localnovel.engine.model.CandidateReferenceHumanAcceptanceDecisionIntakeEvidence
import com.localnovel.engine.model.CandidateReferenceHumanAcceptanceDecisionIntakeFinding
import com.localnovel.engine.model.CandidateReferenceHumanAcceptanceDecisionIntakeReport
import com.localnovel.engine.model.ImageCandidateExternalResultItem
import com.localnovel.engine.model.ImageCandidateReferencePacketValidationReport

class CandidateReferenceHumanAcceptanceDecisionIntakeService {
    private val requiredUpstreamStep = "candidate_reference_human_acceptance_ready"
    private val downstreamStatus = "ALLOW_CANDIDATE_REFERENCE_REGISTRY_PREFLIGHT"
    private val nextAllowedDownstreamStep = "candidate_reference_registry_preflight"
    private val allowedSources = setOf("human", "user", "01_control")
    private val disallowedSources = setOf("auto", "model", "generated")
    private val allowedStatuses = setOf(downstreamStatus, "REQUEST_CHANGES", "REJECT")
    private val positiveChecklistResults = setOf("PASS", "PASSED", "ACCEPTED", "OK", "YES")
    private val requiredChecklistCategories = setOf("source_binding", "visual_drift", "do_not_show", "continuity_scene_fit", "prompt_safety", "downstream_boundary", "no_approval_no_lock")
    private val resolvedIssueStatuses = setOf("RESOLVED", "CLOSED", "ADDRESSED")
    private val blockingIssueSeverities = setOf("BLOCKING", "CRITICAL", "MAJOR")
    private val allowedCandidateStatuses = setOf("external_result_candidate", "needs_review")
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
    private val urlRegex = Regex("""(?i)^[a-z][a-z0-9+.-]*://""")
    private val embeddedUrlRegex = Regex("""(?i)\b[a-z][a-z0-9+.-]*://""")
    private val providerReferenceRegex = Regex("""(?i)\bprovider[-_\s]?job\b|\bprovider[-_\s]?job[-_\s]?id\b|\bapi[-_\s]?response\b""")
    private val forbiddenStatusPatterns = listOf(
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
        Regex("""(?i)\buser\s+approved\b"""),
        Regex("""(?i)\basset\s+approved\b"""),
        Regex("""(?i)\bassets\s+approved\b"""),
        Regex("""(?i)\basset\s+approval\s+complete\b"""),
        Regex("""(?i)\bproduction\s+locked\b"""),
        Regex("""(?i)\bcanon\s+applied\b"""),
        Regex("""(?i)\bruntime\s+scene\s+created\b"""),
        Regex("\u5716\u7247\u5df2\u751f\u6210|\u7d20\u6750\u5df2\u6838\u51c6|\u4f7f\u7528\u8005\u5df2\u6838\u51c6|\u88fd\u4f5c\u9396\u5b9a|\u6b63\u5178\u5df2\u5957\u7528"),
    )

    fun validate(
        candidateReferencePacketValidation: ImageCandidateReferencePacketValidationReport,
        humanAcceptanceDecision: CandidateReferenceHumanAcceptanceDecision,
        candidateReferencePacketValidationReportFile: String = "",
        humanAcceptanceDecisionFile: String = "",
    ): CandidateReferenceHumanAcceptanceDecisionIntakeReport {
        val findings = mutableListOf<CandidateReferenceHumanAcceptanceDecisionIntakeFinding>()
        validateUpstream(candidateReferencePacketValidation, findings)
        val sourceCount = validateSourceBinding(candidateReferencePacketValidation, humanAcceptanceDecision, candidateReferencePacketValidationReportFile, humanAcceptanceDecisionFile, findings)
        validateDecisionSource(humanAcceptanceDecision, findings)
        val normalizedStatus = humanAcceptanceDecision.decisionStatus.trim().uppercase()
        validateDecisionStatus(normalizedStatus, humanAcceptanceDecision.decisionStatus, findings)
        val allowDownstream = normalizedStatus == downstreamStatus
        val checklistPassedCount = validateChecklist(humanAcceptanceDecision, allowDownstream, findings)
        val unresolvedBlockingCount = validateBlockingIssues(humanAcceptanceDecision, allowDownstream, findings)
        validateImageResults(candidateReferencePacketValidation.imageResults, findings)
        val itemSourceCount = validateImageResultSourceBinding(candidateReferencePacketValidation.imageResults, humanAcceptanceDecision.imageResults, findings)
        val forbiddenRemoteOrProviderCount = validateForbiddenRemoteOrProviderFields(candidateReferencePacketValidation.imageResults + humanAcceptanceDecision.imageResults, findings)
        val forbiddenStatusCount = scanText(humanAcceptanceDecision, candidateReferencePacketValidation.imageResults + humanAcceptanceDecision.imageResults, forbiddenStatusPatterns, "CANDIDATE_REFERENCE_STATUS_FORBIDDEN", "candidate_reference_boundary", "Human acceptance decision cannot contain approved, locked, final, approved-reference, production-ready, or production-locked wording.", findings)
        val forbiddenClaimCount = scanText(humanAcceptanceDecision, candidateReferencePacketValidation.imageResults + humanAcceptanceDecision.imageResults, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", "Human acceptance decision cannot claim generated media, asset approval, user approval, production lock, canon, or runtime scene creation.", findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val downstreamAllowed = verdict == "PASS" && allowDownstream
        val blockedReason = if (verdict == "FAIL") findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId } else ""

        return CandidateReferenceHumanAcceptanceDecisionIntakeReport(
            verdict = verdict,
            decisionId = humanAcceptanceDecision.decisionId,
            candidateReferencePacketValidationReportFile = candidateReferencePacketValidationReportFile,
            humanAcceptanceDecisionFile = humanAcceptanceDecisionFile,
            referencePacketFile = candidateReferencePacketValidation.referencePacketFile,
            packetId = candidateReferencePacketValidation.packetId,
            resultReviewPreflightId = candidateReferencePacketValidation.resultReviewPreflightId,
            resultReviewDecisionIntakeFile = candidateReferencePacketValidation.resultReviewDecisionIntakeFile,
            resultReviewDecisionId = candidateReferencePacketValidation.decisionId,
            externalResultIntakeFile = candidateReferencePacketValidation.externalResultIntakeFile,
            resultId = candidateReferencePacketValidation.resultId,
            providerHandoffPacketValidationReportFile = candidateReferencePacketValidation.providerHandoffPacketValidationReportFile,
            providerHandoffPacketId = candidateReferencePacketValidation.providerHandoffPacketId,
            providerHandoffPreflightId = candidateReferencePacketValidation.providerHandoffPreflightId,
            providerHandoffDecisionId = candidateReferencePacketValidation.providerHandoffDecisionId,
            acceptedScriptPacketId = candidateReferencePacketValidation.acceptedScriptPacketId,
            scriptPacketDraftFile = candidateReferencePacketValidation.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = candidateReferencePacketValidation.scriptPacketDraftContentSha256,
            decisionStatus = humanAcceptanceDecision.decisionStatus,
            decisionAllowedDownstream = downstreamAllowed,
            imageResults = if (downstreamAllowed) candidateReferencePacketValidation.imageResults else emptyList(),
            nextAllowedDownstreamStep = if (downstreamAllowed) nextAllowedDownstreamStep else "",
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = CandidateReferenceHumanAcceptanceDecisionIntakeEvidence(
                candidateReferencePacketValidationVerdict = candidateReferencePacketValidation.verdict,
                candidateReferencePacketValidationNextAllowedDownstreamStep = candidateReferencePacketValidation.nextAllowedDownstreamStep,
                sourceBindingFieldCount = sourceCount + itemSourceCount,
                decisionSource = humanAcceptanceDecision.decisionSource,
                decisionStatus = humanAcceptanceDecision.decisionStatus,
                checklistItemCount = humanAcceptanceDecision.checklistItems.size,
                checklistPassedCount = checklistPassedCount,
                unresolvedBlockingIssueCount = unresolvedBlockingCount,
                imageResultCount = candidateReferencePacketValidation.imageResults.size,
                forbiddenRemoteOrProviderFieldCount = forbiddenRemoteOrProviderCount,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
            ),
        )
    }

    private fun validateUpstream(report: ImageCandidateReferencePacketValidationReport, findings: MutableList<CandidateReferenceHumanAcceptanceDecisionIntakeFinding>) {
        if (report.verdict.trim().uppercase() != "PASS") findings.add(finding("CANDIDATE_REFERENCE_PACKET_VALIDATION_NOT_PASS", "upstream", "CRITICAL", "$.candidateReferencePacketValidation.verdict", "Human acceptance decision requires a PASS candidate reference packet validation report.", evidence = listOf(report.verdict), suggestedFix = "Use a PASS validation report."))
        if (report.nextAllowedDownstreamStep != requiredUpstreamStep) findings.add(finding("CANDIDATE_REFERENCE_PACKET_VALIDATION_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.candidateReferencePacketValidation.nextAllowedDownstreamStep", "Human acceptance decision requires nextAllowedDownstreamStep=candidate_reference_human_acceptance_ready.", evidence = listOf(report.nextAllowedDownstreamStep), suggestedFix = "Use the validation report that explicitly allows human acceptance readiness."))
    }

    private fun validateSourceBinding(report: ImageCandidateReferencePacketValidationReport, decision: CandidateReferenceHumanAcceptanceDecision, reportFile: String, decisionFile: String, findings: MutableList<CandidateReferenceHumanAcceptanceDecisionIntakeFinding>): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) { if (value.isBlank()) findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Human acceptance decision intake requires nonblank $label.", suggestedFix = "Use matching validation and decision JSON.")) else count += 1 }
        fun requireMatch(leftValue: String, rightValue: String, leftPath: String, rightPath: String, missingRuleId: String, mismatchRuleId: String, label: String, normalize: (String) -> String = { it.trim() }) {
            requireNonBlank(leftValue, leftPath, "${missingRuleId}_VALIDATION", "$label in validation report")
            requireNonBlank(rightValue, rightPath, missingRuleId, "$label in decision")
            val left = normalize(leftValue); val right = normalize(rightValue)
            if (left.isNotBlank() && right.isNotBlank() && left != right) findings.add(finding(mismatchRuleId, "source_linkage", "CRITICAL", rightPath, "Human acceptance decision source binding mismatch for $label.", evidence = listOf("validation=$leftValue", "decision=$rightValue"), suggestedFix = "Use a decision JSON created for this validation report."))
        }
        requireNonBlank(reportFile, "$.inputs.candidateReferencePacketValidationReportFile", "CANDIDATE_REFERENCE_PACKET_VALIDATION_REPORT_FILE_MISSING", "candidate reference packet validation report file")
        requireNonBlank(decisionFile, "$.inputs.humanAcceptanceDecisionFile", "HUMAN_ACCEPTANCE_DECISION_FILE_MISSING", "human acceptance decision file")
        requireNonBlank(decision.decisionId, "$.humanAcceptanceDecision.decisionId", "DECISION_ID_MISSING", "decision id")
        requireNonBlank(decision.reviewer, "$.humanAcceptanceDecision.reviewer", "DECISION_REVIEWER_MISSING", "reviewer")
        requireNonBlank(decision.reviewerRole, "$.humanAcceptanceDecision.reviewerRole", "DECISION_REVIEWER_ROLE_MISSING", "reviewer role")
        requireNonBlank(decision.decisionAt, "$.humanAcceptanceDecision.decisionAt", "DECISION_AT_MISSING", "decision timestamp")
        requireNonBlank(decision.candidateReferencePacketValidationReportFile, "$.humanAcceptanceDecision.candidateReferencePacketValidationReportFile", "DECISION_VALIDATION_REPORT_FILE_MISSING", "validation report file in decision")
        if (reportFile.isNotBlank() && decision.candidateReferencePacketValidationReportFile.isNotBlank() && normalizePath(reportFile) != normalizePath(decision.candidateReferencePacketValidationReportFile)) findings.add(finding("DECISION_VALIDATION_REPORT_FILE_MISMATCH", "source_linkage", "CRITICAL", "$.humanAcceptanceDecision.candidateReferencePacketValidationReportFile", "Decision must point to the same validation report file.", evidence = listOf("input=$reportFile", "decision=${decision.candidateReferencePacketValidationReportFile}"), suggestedFix = "Use the decision JSON for this validation report file."))
        requireMatch(report.referencePacketFile, decision.referencePacketFile, "$.candidateReferencePacketValidation.referencePacketFile", "$.humanAcceptanceDecision.referencePacketFile", "DECISION_REFERENCE_PACKET_FILE_MISSING", "DECISION_REFERENCE_PACKET_FILE_MISMATCH", "reference packet file", ::normalizePath)
        requireMatch(report.packetId, decision.packetId, "$.candidateReferencePacketValidation.packetId", "$.humanAcceptanceDecision.packetId", "DECISION_PACKET_ID_MISSING", "DECISION_PACKET_ID_MISMATCH", "reference packet id")
        requireMatch(report.resultReviewPreflightId, decision.resultReviewPreflightId, "$.candidateReferencePacketValidation.resultReviewPreflightId", "$.humanAcceptanceDecision.resultReviewPreflightId", "DECISION_RESULT_REVIEW_PREFLIGHT_ID_MISSING", "DECISION_RESULT_REVIEW_PREFLIGHT_ID_MISMATCH", "result review preflight id")
        requireMatch(report.resultReviewDecisionIntakeFile, decision.resultReviewDecisionIntakeFile, "$.candidateReferencePacketValidation.resultReviewDecisionIntakeFile", "$.humanAcceptanceDecision.resultReviewDecisionIntakeFile", "DECISION_RESULT_REVIEW_DECISION_INTAKE_FILE_MISSING", "DECISION_RESULT_REVIEW_DECISION_INTAKE_FILE_MISMATCH", "result review decision intake file", ::normalizePath)
        requireMatch(report.decisionId, decision.resultReviewDecisionId, "$.candidateReferencePacketValidation.decisionId", "$.humanAcceptanceDecision.resultReviewDecisionId", "DECISION_RESULT_REVIEW_DECISION_ID_MISSING", "DECISION_RESULT_REVIEW_DECISION_ID_MISMATCH", "result review decision id")
        requireMatch(report.externalResultIntakeFile, decision.externalResultIntakeFile, "$.candidateReferencePacketValidation.externalResultIntakeFile", "$.humanAcceptanceDecision.externalResultIntakeFile", "DECISION_EXTERNAL_RESULT_INTAKE_FILE_MISSING", "DECISION_EXTERNAL_RESULT_INTAKE_FILE_MISMATCH", "external result intake file", ::normalizePath)
        requireMatch(report.resultId, decision.resultId, "$.candidateReferencePacketValidation.resultId", "$.humanAcceptanceDecision.resultId", "DECISION_RESULT_ID_MISSING", "DECISION_RESULT_ID_MISMATCH", "result id")
        requireMatch(report.providerHandoffPacketValidationReportFile, decision.providerHandoffPacketValidationReportFile, "$.candidateReferencePacketValidation.providerHandoffPacketValidationReportFile", "$.humanAcceptanceDecision.providerHandoffPacketValidationReportFile", "DECISION_PROVIDER_HANDOFF_PACKET_VALIDATION_REPORT_FILE_MISSING", "DECISION_PROVIDER_HANDOFF_PACKET_VALIDATION_REPORT_FILE_MISMATCH", "provider handoff packet validation report file", ::normalizePath)
        requireMatch(report.providerHandoffPacketId, decision.providerHandoffPacketId, "$.candidateReferencePacketValidation.providerHandoffPacketId", "$.humanAcceptanceDecision.providerHandoffPacketId", "DECISION_PROVIDER_HANDOFF_PACKET_ID_MISSING", "DECISION_PROVIDER_HANDOFF_PACKET_ID_MISMATCH", "provider handoff packet id")
        requireMatch(report.providerHandoffPreflightId, decision.providerHandoffPreflightId, "$.candidateReferencePacketValidation.providerHandoffPreflightId", "$.humanAcceptanceDecision.providerHandoffPreflightId", "DECISION_PROVIDER_HANDOFF_PREFLIGHT_ID_MISSING", "DECISION_PROVIDER_HANDOFF_PREFLIGHT_ID_MISMATCH", "provider handoff preflight id")
        requireMatch(report.providerHandoffDecisionId, decision.providerHandoffDecisionId, "$.candidateReferencePacketValidation.providerHandoffDecisionId", "$.humanAcceptanceDecision.providerHandoffDecisionId", "DECISION_PROVIDER_HANDOFF_DECISION_ID_MISSING", "DECISION_PROVIDER_HANDOFF_DECISION_ID_MISMATCH", "provider handoff decision id")
        requireMatch(report.acceptedScriptPacketId, decision.acceptedScriptPacketId, "$.candidateReferencePacketValidation.acceptedScriptPacketId", "$.humanAcceptanceDecision.acceptedScriptPacketId", "DECISION_ACCEPTED_SCRIPT_PACKET_ID_MISSING", "DECISION_ACCEPTED_SCRIPT_PACKET_ID_MISMATCH", "accepted script packet id")
        requireMatch(report.scriptPacketDraftFile, decision.scriptPacketDraftFile, "$.candidateReferencePacketValidation.scriptPacketDraftFile", "$.humanAcceptanceDecision.scriptPacketDraftFile", "DECISION_SCRIPT_PACKET_DRAFT_FILE_MISSING", "DECISION_SCRIPT_PACKET_DRAFT_FILE_MISMATCH", "script packet draft file", ::normalizePath)
        requireMatch(report.scriptPacketDraftContentSha256, decision.scriptPacketDraftContentSha256, "$.candidateReferencePacketValidation.scriptPacketDraftContentSha256", "$.humanAcceptanceDecision.scriptPacketDraftContentSha256", "DECISION_SCRIPT_PACKET_DRAFT_HASH_MISSING", "DECISION_SCRIPT_PACKET_DRAFT_HASH_MISMATCH", "script packet draft SHA-256", { it.trim().lowercase() })
        if (decision.scriptPacketDraftContentSha256.isNotBlank() && !sha256Regex.matches(decision.scriptPacketDraftContentSha256.trim().lowercase())) findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.humanAcceptanceDecision.scriptPacketDraftContentSha256", "Human acceptance decision requires a valid lowercase SHA-256 script packet draft hash.", evidence = listOf(decision.scriptPacketDraftContentSha256), suggestedFix = "Use the hash carried by the validation report."))
        return count
    }

    private fun validateDecisionSource(decision: CandidateReferenceHumanAcceptanceDecision, findings: MutableList<CandidateReferenceHumanAcceptanceDecisionIntakeFinding>) {
        val source = decision.decisionSource.trim().lowercase()
        if (source.isBlank()) findings.add(finding("DECISION_SOURCE_MISSING", "human_decision", "CRITICAL", "$.humanAcceptanceDecision.decisionSource", "Human acceptance decision must declare a human/control/user source.", suggestedFix = "Set decisionSource to human, user, or 01_control."))
        else if (source in disallowedSources || source !in allowedSources) findings.add(finding("DECISION_SOURCE_NOT_HUMAN", "human_decision", "CRITICAL", "$.humanAcceptanceDecision.decisionSource", "Human acceptance decision source cannot be automatic, model, generated, or unknown.", evidence = listOf(decision.decisionSource), suggestedFix = "Use an explicit external human/control/user decision source."))
    }

    private fun validateDecisionStatus(normalizedStatus: String, rawStatus: String, findings: MutableList<CandidateReferenceHumanAcceptanceDecisionIntakeFinding>) {
        if (normalizedStatus.isBlank()) findings.add(finding("DECISION_STATUS_MISSING", "human_decision", "CRITICAL", "$.humanAcceptanceDecision.decisionStatus", "Human acceptance decision status must be declared.", suggestedFix = "Use ALLOW_CANDIDATE_REFERENCE_REGISTRY_PREFLIGHT, REQUEST_CHANGES, or REJECT."))
        else if (normalizedStatus !in allowedStatuses) findings.add(finding("DECISION_STATUS_UNKNOWN", "human_decision", "CRITICAL", "$.humanAcceptanceDecision.decisionStatus", "Human acceptance decision status is outside the narrow allowed vocabulary.", evidence = listOf(rawStatus), suggestedFix = "Use ALLOW_CANDIDATE_REFERENCE_REGISTRY_PREFLIGHT, REQUEST_CHANGES, or REJECT."))
    }

    private fun validateChecklist(decision: CandidateReferenceHumanAcceptanceDecision, allowDownstream: Boolean, findings: MutableList<CandidateReferenceHumanAcceptanceDecisionIntakeFinding>): Int {
        if (decision.checklistItems.isEmpty()) {
            findings.add(finding("HUMAN_CHECKLIST_MISSING", "human_decision", "CRITICAL", "$.humanAcceptanceDecision.checklistItems", "Human acceptance decision must include checklist item results.", suggestedFix = "Record human checklist results before allowing registry preflight."))
            return 0
        }
        var passed = 0
        decision.checklistItems.forEachIndexed { index, item ->
            if (item.itemId.isBlank() || item.category.isBlank() || item.result.isBlank()) findings.add(finding("HUMAN_CHECKLIST_ITEM_INCOMPLETE", "human_decision", "CRITICAL", "$.humanAcceptanceDecision.checklistItems[$index]", "Human checklist item requires itemId, category, and result.", suggestedFix = "Complete every checklist item."))
            val result = item.result.trim().uppercase()
            if (result in positiveChecklistResults) passed += 1 else if (allowDownstream) findings.add(finding("HUMAN_CHECKLIST_ITEM_NOT_ACCEPTED", "human_decision", "CRITICAL", "$.humanAcceptanceDecision.checklistItems[$index].result", "Allowing registry preflight requires every checklist item to pass.", evidence = listOf(item.result), suggestedFix = "Use REQUEST_CHANGES or REJECT until every required item is accepted."))
        }
        if (allowDownstream) {
            val accepted = decision.checklistItems.filter { it.result.trim().uppercase() in positiveChecklistResults }.map { it.category.trim().lowercase() }.toSet()
            val hasVisualOrDoNotShow = "visual_drift" in accepted || "do_not_show" in accepted
            val required = requiredChecklistCategories.filter { it != "visual_drift" && it != "do_not_show" }
            val missing = required.filterNot { it in accepted }.toMutableList()
            if (!hasVisualOrDoNotShow) missing += "visual_drift_or_do_not_show"
            if (missing.isNotEmpty()) findings.add(finding("HUMAN_CHECKLIST_REQUIRED_CATEGORY_MISSING", "human_decision", "CRITICAL", "$.humanAcceptanceDecision.checklistItems", "Allowing registry preflight requires accepted checklist coverage for source binding, visual drift/do-not-show, continuity scene fit, prompt safety, downstream boundary, and no approval/no lock.", evidence = missing, suggestedFix = "Record positive human checklist results for every required category."))
        }
        return passed
    }

    private fun validateBlockingIssues(decision: CandidateReferenceHumanAcceptanceDecision, allowDownstream: Boolean, findings: MutableList<CandidateReferenceHumanAcceptanceDecisionIntakeFinding>): Int {
        var count = 0
        decision.blockingIssues.forEachIndexed { index, issue ->
            val unresolvedBlocking = issue.severity.trim().uppercase() in blockingIssueSeverities && issue.status.trim().uppercase() !in resolvedIssueStatuses
            if (unresolvedBlocking) {
                count += 1
                if (allowDownstream) findings.add(finding("UNRESOLVED_BLOCKING_ISSUE_PRESENT", "human_decision", "CRITICAL", "$.humanAcceptanceDecision.blockingIssues[$index]", "Allowing registry preflight requires no unresolved blocking issue.", evidence = listOf(issue.issueId, issue.severity, issue.status).filter { it.isNotBlank() }, suggestedFix = "Resolve the blocking issue or use REQUEST_CHANGES / REJECT."))
            }
        }
        return count
    }

    private fun validateImageResults(results: List<ImageCandidateExternalResultItem>, findings: MutableList<CandidateReferenceHumanAcceptanceDecisionIntakeFinding>) {
        if (results.isEmpty()) findings.add(finding("IMAGE_RESULTS_MISSING", "candidate_reference_boundary", "CRITICAL", "$.candidateReferencePacketValidation.imageResults", "Human acceptance decision requires candidate image result entries.", suggestedFix = "Use a validation report carrying candidate results."))
        results.forEachIndexed { index, item ->
            if (item.resultItemId.isBlank() || item.promptItemId.isBlank() || item.sourceCandidateId.isBlank() || item.assetType.isBlank()) findings.add(finding("IMAGE_RESULT_ITEM_INCOMPLETE", "candidate_reference_boundary", "CRITICAL", "$.candidateReferencePacketValidation.imageResults[$index]", "Image result item requires result id, prompt id, candidate id, and asset type.", suggestedFix = "Use complete candidate result entries."))
            if (item.localImagePath.isBlank() && item.metadataOnlyReference.isBlank()) findings.add(finding("IMAGE_RESULT_REFERENCE_MISSING", "candidate_reference_boundary", "CRITICAL", "$.candidateReferencePacketValidation.imageResults[$index]", "Image result item requires local image path or metadata-only reference.", suggestedFix = "Keep the candidate reference visible."))
            if (item.candidateStatus.trim().lowercase() !in allowedCandidateStatuses) findings.add(finding("IMAGE_RESULT_STATUS_NOT_ALLOWED", "candidate_reference_boundary", "CRITICAL", "$.candidateReferencePacketValidation.imageResults[$index].candidateStatus", "Image result status must remain candidate-only.", evidence = listOf(item.candidateStatus), suggestedFix = "Use external_result_candidate or needs_review."))
        }
    }

    private fun validateImageResultSourceBinding(validationResults: List<ImageCandidateExternalResultItem>, decisionResults: List<ImageCandidateExternalResultItem>, findings: MutableList<CandidateReferenceHumanAcceptanceDecisionIntakeFinding>): Int {
        var count = 0
        val validationIds = validationResults.map { it.resultItemId.trim() }
        val decisionIds = decisionResults.map { it.resultItemId.trim() }
        if (validationIds.toSet() != decisionIds.toSet()) findings.add(finding("IMAGE_RESULT_SET_MISMATCH", "source_linkage", "CRITICAL", "$.humanAcceptanceDecision.imageResults", "Human acceptance decision image result id set must match the validation report image result id set.", evidence = listOf("validation=${validationIds.joinToString(",")}", "decision=${decisionIds.joinToString(",")}"), suggestedFix = "Use image result items copied from the validation report."))
        val validationById = validationResults.associateBy { it.resultItemId.trim() }
        decisionResults.forEachIndexed { index, item ->
            val source = validationById[item.resultItemId.trim()]
            if (item.resultItemId.isBlank() || source == null) {
                findings.add(finding("IMAGE_RESULT_NOT_IN_VALIDATION_REPORT", "source_linkage", "CRITICAL", "$.humanAcceptanceDecision.imageResults[$index].resultItemId", "Human acceptance decision image result must exist in the validation report.", evidence = listOf(item.resultItemId), suggestedFix = "Do not replace candidate image result items in the human decision."))
                return@forEachIndexed
            }
            count += 1
            compareField(source.promptItemId, item.promptItemId, "$.humanAcceptanceDecision.imageResults[$index].promptItemId", "promptItemId", findings)
            compareField(source.sourceCandidateId, item.sourceCandidateId, "$.humanAcceptanceDecision.imageResults[$index].sourceCandidateId", "sourceCandidateId", findings)
            compareField(source.assetType, item.assetType, "$.humanAcceptanceDecision.imageResults[$index].assetType", "assetType", findings)
            compareField(source.candidateStatus, item.candidateStatus, "$.humanAcceptanceDecision.imageResults[$index].candidateStatus", "candidateStatus", findings)
            compareField(source.localImagePath, item.localImagePath, "$.humanAcceptanceDecision.imageResults[$index].localImagePath", "localImagePath", findings, ::normalizePath)
            compareField(source.metadataOnlyReference, item.metadataOnlyReference, "$.humanAcceptanceDecision.imageResults[$index].metadataOnlyReference", "metadataOnlyReference", findings)
            compareField(source.sourceFileSha256, item.sourceFileSha256, "$.humanAcceptanceDecision.imageResults[$index].sourceFileSha256", "sourceFileSha256", findings, { it.trim().lowercase() })
            compareList(source.doNotShowChecklist, item.doNotShowChecklist, "$.humanAcceptanceDecision.imageResults[$index].doNotShowChecklist", "doNotShowChecklist", findings)
            compareList(source.driftReviewChecklist, item.driftReviewChecklist, "$.humanAcceptanceDecision.imageResults[$index].driftReviewChecklist", "driftReviewChecklist", findings)
            compareList(source.residualRisks, item.residualRisks, "$.humanAcceptanceDecision.imageResults[$index].residualRisks", "residualRisks", findings)
        }
        return count
    }

    private fun validateForbiddenRemoteOrProviderFields(results: List<ImageCandidateExternalResultItem>, findings: MutableList<CandidateReferenceHumanAcceptanceDecisionIntakeFinding>): Int {
        var count = 0
        results.forEachIndexed { index, item ->
            listOf(Triple(item.providerJobId, "providerJobId", "PROVIDER_JOB_ID_FORBIDDEN"), Triple(item.apiResponse, "apiResponse", "API_RESPONSE_FORBIDDEN"), Triple(item.remoteUrl, "remoteUrl", "REMOTE_URL_FORBIDDEN"), Triple(item.generatedStatus, "generatedStatus", "GENERATED_STATUS_FORBIDDEN")).forEach { (value, field, ruleId) ->
                if (value.isNotBlank()) { count += 1; findings.add(finding(ruleId, "safety_boundary", "CRITICAL", "$.imageResults[$index].$field", "Human acceptance decision cannot carry provider job, API response, remote URL, or generated status.", evidence = listOf(value), suggestedFix = "Keep only local candidate references.")) }
            }
            if (item.localImagePath.isNotBlank() && urlRegex.containsMatchIn(item.localImagePath.trim())) { count += 1; findings.add(finding("REMOTE_IMAGE_PATH_FORBIDDEN", "safety_boundary", "CRITICAL", "$.imageResults[$index].localImagePath", "Human acceptance decision only allows local image paths.", evidence = listOf(item.localImagePath), suggestedFix = "Use a local path or metadata-only reference.")) }
            if (item.metadataOnlyReference.isNotBlank() && (embeddedUrlRegex.containsMatchIn(item.metadataOnlyReference) || providerReferenceRegex.containsMatchIn(item.metadataOnlyReference))) { count += 1; findings.add(finding("REMOTE_METADATA_REFERENCE_FORBIDDEN", "safety_boundary", "CRITICAL", "$.imageResults[$index].metadataOnlyReference", "Human acceptance decision cannot carry remote URLs, provider job references, or API response references.", evidence = listOf(item.metadataOnlyReference), suggestedFix = "Use local candidate metadata.")) }
        }
        return count
    }

    private fun scanText(decision: CandidateReferenceHumanAcceptanceDecision, results: List<ImageCandidateExternalResultItem>, patterns: List<Regex>, ruleId: String, category: String, message: String, findings: MutableList<CandidateReferenceHumanAcceptanceDecisionIntakeFinding>): Int {
        var count = 0
        collectTextFields(decision, results).forEach { field -> patterns.forEach { pattern -> if (pattern.containsMatchIn(field.value)) { count += 1; findings.add(finding(ruleId, category, "CRITICAL", field.path, message, evidence = listOf(field.value), suggestedFix = "Keep the decision candidate-only and unapproved.")) } } }
        return count
    }

    private fun collectTextFields(decision: CandidateReferenceHumanAcceptanceDecision, results: List<ImageCandidateExternalResultItem>): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) { if (value.isNotBlank()) fields += TextField(path, value) }
        add("$.humanAcceptanceDecision.decisionSource", decision.decisionSource)
        add("$.humanAcceptanceDecision.reviewer", decision.reviewer)
        add("$.humanAcceptanceDecision.reviewerRole", decision.reviewerRole)
        add("$.humanAcceptanceDecision.decisionStatus", decision.decisionStatus)
        add("$.humanAcceptanceDecision.notes", decision.notes)
        decision.checklistItems.forEachIndexed { index, item ->
            add("$.humanAcceptanceDecision.checklistItems[$index].category", item.category)
            add("$.humanAcceptanceDecision.checklistItems[$index].result", item.result)
            add("$.humanAcceptanceDecision.checklistItems[$index].note", item.note)
        }
        decision.blockingIssues.forEachIndexed { index, issue ->
            add("$.humanAcceptanceDecision.blockingIssues[$index].description", issue.description)
            add("$.humanAcceptanceDecision.blockingIssues[$index].status", issue.status)
        }
        results.forEachIndexed { index, item ->
            add("$.imageResults[$index].assetType", item.assetType)
            add("$.imageResults[$index].candidateStatus", item.candidateStatus)
            add("$.imageResults[$index].localImagePath", item.localImagePath)
            add("$.imageResults[$index].metadataOnlyReference", item.metadataOnlyReference)
            add("$.imageResults[$index].notes", item.notes)
            item.doNotShowChecklist.forEachIndexed { itemIndex, value -> add("$.imageResults[$index].doNotShowChecklist[$itemIndex]", value) }
            item.driftReviewChecklist.forEachIndexed { itemIndex, value -> add("$.imageResults[$index].driftReviewChecklist[$itemIndex]", value) }
            item.residualRisks.forEachIndexed { itemIndex, value -> add("$.imageResults[$index].residualRisks[$itemIndex]", value) }
        }
        return fields
    }

    private fun compareField(sourceValue: String, decisionValue: String, path: String, label: String, findings: MutableList<CandidateReferenceHumanAcceptanceDecisionIntakeFinding>, normalize: (String) -> String = { it.trim() }) {
        if (normalize(sourceValue) != normalize(decisionValue)) findings.add(finding("IMAGE_RESULT_SOURCE_MISMATCH", "source_linkage", "CRITICAL", path, "Human acceptance decision image result $label must match the validation report.", evidence = listOf("validation=$sourceValue", "decision=$decisionValue"), suggestedFix = "Use the image result item carried by the validation report."))
    }

    private fun compareList(sourceValue: List<String>, decisionValue: List<String>, path: String, label: String, findings: MutableList<CandidateReferenceHumanAcceptanceDecisionIntakeFinding>) {
        val left = sourceValue.map { it.trim() }
        val right = decisionValue.map { it.trim() }
        if (left != right) findings.add(finding("IMAGE_RESULT_SOURCE_MISMATCH", "source_linkage", "CRITICAL", path, "Human acceptance decision image result $label must match the validation report.", evidence = listOf("validation=${left.joinToString("|")}", "decision=${right.joinToString("|")}"), suggestedFix = "Use checklist/risk fields carried by the validation report."))
    }

    private fun normalizePath(value: String): String = value.trim().replace('\\', '/')
    private fun finding(ruleId: String, category: String, severity: String, path: String, message: String, evidence: List<String> = emptyList(), suggestedFix: String = ""): CandidateReferenceHumanAcceptanceDecisionIntakeFinding =
        CandidateReferenceHumanAcceptanceDecisionIntakeFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
    private data class TextField(val path: String, val value: String)
}
