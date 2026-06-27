package com.localnovel.engine.service

import com.localnovel.engine.model.CandidateReferenceRegistryHumanAcceptanceDecision
import com.localnovel.engine.model.CandidateReferenceRegistryHumanAcceptanceDecisionIntakeEvidence
import com.localnovel.engine.model.CandidateReferenceRegistryHumanAcceptanceDecisionIntakeFinding
import com.localnovel.engine.model.CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport
import com.localnovel.engine.model.CandidateReferenceRegistryPacketValidationReport
import com.localnovel.engine.model.ImageCandidateExternalResultItem

class CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService {
    private val requiredValidationStep = "candidate_reference_registry_human_acceptance_ready"
    private val downstreamStatus = "ALLOW_CANDIDATE_REFERENCE_ACCEPTANCE_PACKET"
    private val nextAllowedDownstreamStep = "candidate_reference_acceptance_packet"
    private val allowedSources = setOf("human", "user", "01_control")
    private val disallowedSources = setOf("auto", "model", "generated")
    private val allowedStatuses = setOf(downstreamStatus, "REQUEST_CHANGES", "REJECT")
    private val positiveChecklistResults = setOf("PASS", "PASSED", "ACCEPTED", "OK", "YES")
    private val requiredChecklistCategories = setOf("source_binding", "local_reference_integrity", "no_remote_provider", "visual_drift", "do_not_show", "downstream_boundary", "no_approval_no_lock")
    private val blockingIssueSeverities = setOf("BLOCKING", "CRITICAL", "MAJOR")
    private val resolvedIssueStatuses = setOf("RESOLVED", "CLOSED", "ADDRESSED")
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
    private val urlRegex = Regex("""(?i)^[a-z][a-z0-9+.-]*://""")
    private val embeddedUrlRegex = Regex("""(?i)\b[a-z][a-z0-9+.-]*://""")
    private val providerReferenceRegex = Regex("""(?i)\bprovider[-_\s]?job\b|\bprovider[-_\s]?job[-_\s]?id\b|\bapi[-_\s]?response\b""")
    private val forbiddenStatusPatterns = listOf(
        Regex("""(?i)\bapproved\b"""),
        Regex("""(?i)\bregistered\b"""),
        Regex("""(?i)\blocked\b"""),
        Regex("""(?i)\bfinal\b"""),
        Regex("""(?i)\bproduction[-_\s]?ready\b"""),
        Regex("""(?i)\bapproved[-_\s]?reference\b"""),
        Regex("""(?i)\bregistered[-_\s]?reference\b"""),
        Regex("""(?i)\bproduction[-_\s]?locked\b"""),
        Regex("\u6838\u51c6|\u6279\u51c6|\u767b\u8a18|\u9396\u5b9a"),
    )
    private val forbiddenClaimPatterns = listOf(
        Regex("""(?i)\bimage\s+generated\b"""),
        Regex("""(?i)\buser\s+approved\b"""),
        Regex("""(?i)\basset\s+approved\b"""),
        Regex("""(?i)\basset\s+approval\s+complete\b"""),
        Regex("""(?i)\bcanon\s+applied\b"""),
        Regex("""(?i)\bruntime\s+scene\s+created\b"""),
    )

    fun validate(
        registryPacketValidation: CandidateReferenceRegistryPacketValidationReport,
        humanAcceptanceDecision: CandidateReferenceRegistryHumanAcceptanceDecision,
        registryPacketValidationReportFile: String = "",
        humanAcceptanceDecisionFile: String = "",
    ): CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport {
        val findings = mutableListOf<CandidateReferenceRegistryHumanAcceptanceDecisionIntakeFinding>()
        validateValidationReport(registryPacketValidation, findings)
        val sourceCount = validateSourceBinding(registryPacketValidation, humanAcceptanceDecision, registryPacketValidationReportFile, humanAcceptanceDecisionFile, findings) +
            validateImageResultSourceBinding(registryPacketValidation.imageResults, humanAcceptanceDecision.imageResults, findings)
        validateDecisionSource(humanAcceptanceDecision, findings)
        val normalizedStatus = humanAcceptanceDecision.decisionStatus.trim().uppercase()
        validateDecisionStatus(normalizedStatus, humanAcceptanceDecision.decisionStatus, findings)
        val allowDownstream = normalizedStatus == downstreamStatus
        val checklistPassedCount = validateChecklist(humanAcceptanceDecision, allowDownstream, findings)
        val unresolvedBlockingCount = validateBlockingIssues(humanAcceptanceDecision, allowDownstream, findings)
        val forbiddenRemoteOrProviderCount = validateForbiddenRemoteOrProviderFields(registryPacketValidation.imageResults + humanAcceptanceDecision.imageResults, findings)
        val forbiddenStatusCount = scanText(humanAcceptanceDecision, registryPacketValidation.imageResults + humanAcceptanceDecision.imageResults, forbiddenStatusPatterns, "CANDIDATE_REFERENCE_STATUS_FORBIDDEN", "candidate_reference_boundary", findings)
        val forbiddenClaimCount = scanText(humanAcceptanceDecision, registryPacketValidation.imageResults + humanAcceptanceDecision.imageResults, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val downstreamAllowed = verdict == "PASS" && allowDownstream
        val blockedReason = if (verdict == "FAIL") findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId } else ""

        return CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport(
            verdict = verdict,
            decisionId = humanAcceptanceDecision.decisionId,
            registryPacketValidationReportFile = registryPacketValidationReportFile,
            humanAcceptanceDecisionFile = humanAcceptanceDecisionFile,
            registryPacketFile = registryPacketValidation.registryPacketFile,
            registryPacketId = registryPacketValidation.packetId,
            registryPreflightFile = registryPacketValidation.registryPreflightFile,
            registryPreflightId = registryPacketValidation.preflightId,
            humanAcceptanceDecisionIntakeFile = registryPacketValidation.humanAcceptanceDecisionIntakeFile,
            priorHumanAcceptanceDecisionId = registryPacketValidation.decisionId,
            acceptedScriptPacketId = registryPacketValidation.acceptedScriptPacketId,
            scriptPacketDraftFile = registryPacketValidation.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = registryPacketValidation.scriptPacketDraftContentSha256,
            referencePacketFile = registryPacketValidation.referencePacketFile,
            referencePacketId = registryPacketValidation.referencePacketId,
            resultReviewPreflightId = registryPacketValidation.resultReviewPreflightId,
            resultReviewDecisionIntakeFile = registryPacketValidation.resultReviewDecisionIntakeFile,
            resultReviewDecisionId = registryPacketValidation.resultReviewDecisionId,
            providerHandoffPacketValidationReportFile = registryPacketValidation.providerHandoffPacketValidationReportFile,
            providerHandoffPacketId = registryPacketValidation.providerHandoffPacketId,
            providerHandoffPreflightId = registryPacketValidation.providerHandoffPreflightId,
            providerHandoffDecisionId = registryPacketValidation.providerHandoffDecisionId,
            externalResultIntakeFile = registryPacketValidation.externalResultIntakeFile,
            resultId = registryPacketValidation.resultId,
            decisionStatus = humanAcceptanceDecision.decisionStatus,
            decisionAllowedDownstream = downstreamAllowed,
            imageResults = if (downstreamAllowed) registryPacketValidation.imageResults else emptyList(),
            nextAllowedDownstreamStep = if (downstreamAllowed) nextAllowedDownstreamStep else "",
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = CandidateReferenceRegistryHumanAcceptanceDecisionIntakeEvidence(
                registryPacketValidationVerdict = registryPacketValidation.verdict,
                registryPacketValidationNextAllowedDownstreamStep = registryPacketValidation.nextAllowedDownstreamStep,
                sourceBindingFieldCount = sourceCount,
                decisionSource = humanAcceptanceDecision.decisionSource,
                decisionStatus = humanAcceptanceDecision.decisionStatus,
                checklistItemCount = humanAcceptanceDecision.checklistItems.size,
                checklistPassedCount = checklistPassedCount,
                unresolvedBlockingIssueCount = unresolvedBlockingCount,
                imageResultCount = registryPacketValidation.imageResults.size,
                forbiddenRemoteOrProviderFieldCount = forbiddenRemoteOrProviderCount,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
            ),
        )
    }

    private fun validateValidationReport(report: CandidateReferenceRegistryPacketValidationReport, findings: MutableList<CandidateReferenceRegistryHumanAcceptanceDecisionIntakeFinding>) {
        if (report.verdict.trim().uppercase() != "PASS") findings.add(finding("REGISTRY_PACKET_VALIDATION_NOT_PASS", "upstream", "CRITICAL", "$.registryPacketValidation.verdict", "Human acceptance decision requires a PASS registry packet validation report.", evidence = listOf(report.verdict)))
        if (report.nextAllowedDownstreamStep != requiredValidationStep) findings.add(finding("REGISTRY_PACKET_VALIDATION_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.registryPacketValidation.nextAllowedDownstreamStep", "Human acceptance decision requires nextAllowedDownstreamStep=candidate_reference_registry_human_acceptance_ready.", evidence = listOf(report.nextAllowedDownstreamStep)))
    }

    private fun validateSourceBinding(report: CandidateReferenceRegistryPacketValidationReport, decision: CandidateReferenceRegistryHumanAcceptanceDecision, reportFile: String, decisionFile: String, findings: MutableList<CandidateReferenceRegistryHumanAcceptanceDecisionIntakeFinding>): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) { if (value.isBlank()) findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Human acceptance decision intake requires nonblank $label.")) else count += 1 }
        fun requireMatch(leftValue: String, rightValue: String, leftPath: String, rightPath: String, missingRuleId: String, mismatchRuleId: String, label: String, normalize: (String) -> String = { it.trim() }) {
            requireNonBlank(leftValue, leftPath, "${missingRuleId}_VALIDATION", "$label in validation report")
            requireNonBlank(rightValue, rightPath, missingRuleId, "$label in decision")
            val left = normalize(leftValue); val right = normalize(rightValue)
            if (left.isNotBlank() && right.isNotBlank() && left != right) findings.add(finding(mismatchRuleId, "source_linkage", "CRITICAL", rightPath, "Human acceptance decision source binding mismatch for $label.", evidence = listOf("validation=$leftValue", "decision=$rightValue")))
        }
        requireNonBlank(reportFile, "$.inputs.registryPacketValidationReportFile", "REGISTRY_PACKET_VALIDATION_REPORT_FILE_MISSING", "registry packet validation report file")
        requireNonBlank(decisionFile, "$.inputs.humanAcceptanceDecisionFile", "HUMAN_ACCEPTANCE_DECISION_FILE_MISSING", "human acceptance decision file")
        requireNonBlank(decision.decisionId, "$.humanAcceptanceDecision.decisionId", "DECISION_ID_MISSING", "decision id")
        requireNonBlank(decision.reviewer, "$.humanAcceptanceDecision.reviewer", "DECISION_REVIEWER_MISSING", "reviewer")
        requireNonBlank(decision.reviewerRole, "$.humanAcceptanceDecision.reviewerRole", "DECISION_REVIEWER_ROLE_MISSING", "reviewer role")
        requireNonBlank(decision.decisionAt, "$.humanAcceptanceDecision.decisionAt", "DECISION_AT_MISSING", "decision timestamp")
        requireMatch(reportFile, decision.registryPacketValidationReportFile, "$.inputs.registryPacketValidationReportFile", "$.humanAcceptanceDecision.registryPacketValidationReportFile", "DECISION_REGISTRY_PACKET_VALIDATION_REPORT_FILE_MISSING", "DECISION_REGISTRY_PACKET_VALIDATION_REPORT_FILE_MISMATCH", "registry packet validation report file", ::normalizePath)
        requireMatch(report.registryPacketFile, decision.registryPacketFile, "$.registryPacketValidation.registryPacketFile", "$.humanAcceptanceDecision.registryPacketFile", "DECISION_REGISTRY_PACKET_FILE_MISSING", "DECISION_REGISTRY_PACKET_FILE_MISMATCH", "registry packet file", ::normalizePath)
        requireMatch(report.packetId, decision.registryPacketId, "$.registryPacketValidation.packetId", "$.humanAcceptanceDecision.registryPacketId", "DECISION_REGISTRY_PACKET_ID_MISSING", "DECISION_REGISTRY_PACKET_ID_MISMATCH", "registry packet id")
        requireMatch(report.preflightId, decision.registryPreflightId, "$.registryPacketValidation.preflightId", "$.humanAcceptanceDecision.registryPreflightId", "DECISION_REGISTRY_PREFLIGHT_ID_MISSING", "DECISION_REGISTRY_PREFLIGHT_ID_MISMATCH", "registry preflight id")
        requireMatch(report.decisionId, decision.priorHumanAcceptanceDecisionId, "$.registryPacketValidation.decisionId", "$.humanAcceptanceDecision.priorHumanAcceptanceDecisionId", "DECISION_PRIOR_HUMAN_DECISION_ID_MISSING", "DECISION_PRIOR_HUMAN_DECISION_ID_MISMATCH", "prior human acceptance decision id")
        requireMatch(report.acceptedScriptPacketId, decision.acceptedScriptPacketId, "$.registryPacketValidation.acceptedScriptPacketId", "$.humanAcceptanceDecision.acceptedScriptPacketId", "DECISION_ACCEPTED_SCRIPT_PACKET_ID_MISSING", "DECISION_ACCEPTED_SCRIPT_PACKET_ID_MISMATCH", "accepted script packet id")
        requireMatch(report.scriptPacketDraftFile, decision.scriptPacketDraftFile, "$.registryPacketValidation.scriptPacketDraftFile", "$.humanAcceptanceDecision.scriptPacketDraftFile", "DECISION_SCRIPT_PACKET_DRAFT_FILE_MISSING", "DECISION_SCRIPT_PACKET_DRAFT_FILE_MISMATCH", "script packet draft file", ::normalizePath)
        requireMatch(report.scriptPacketDraftContentSha256, decision.scriptPacketDraftContentSha256, "$.registryPacketValidation.scriptPacketDraftContentSha256", "$.humanAcceptanceDecision.scriptPacketDraftContentSha256", "DECISION_SCRIPT_PACKET_DRAFT_HASH_MISSING", "DECISION_SCRIPT_PACKET_DRAFT_HASH_MISMATCH", "script packet draft SHA-256", { it.trim().lowercase() })
        requireMatch(report.referencePacketFile, decision.referencePacketFile, "$.registryPacketValidation.referencePacketFile", "$.humanAcceptanceDecision.referencePacketFile", "DECISION_REFERENCE_PACKET_FILE_MISSING", "DECISION_REFERENCE_PACKET_FILE_MISMATCH", "reference packet file", ::normalizePath)
        requireMatch(report.referencePacketId, decision.referencePacketId, "$.registryPacketValidation.referencePacketId", "$.humanAcceptanceDecision.referencePacketId", "DECISION_REFERENCE_PACKET_ID_MISSING", "DECISION_REFERENCE_PACKET_ID_MISMATCH", "reference packet id")
        requireMatch(report.resultReviewPreflightId, decision.resultReviewPreflightId, "$.registryPacketValidation.resultReviewPreflightId", "$.humanAcceptanceDecision.resultReviewPreflightId", "DECISION_RESULT_REVIEW_PREFLIGHT_ID_MISSING", "DECISION_RESULT_REVIEW_PREFLIGHT_ID_MISMATCH", "result review preflight id")
        requireMatch(report.resultReviewDecisionIntakeFile, decision.resultReviewDecisionIntakeFile, "$.registryPacketValidation.resultReviewDecisionIntakeFile", "$.humanAcceptanceDecision.resultReviewDecisionIntakeFile", "DECISION_RESULT_REVIEW_DECISION_INTAKE_FILE_MISSING", "DECISION_RESULT_REVIEW_DECISION_INTAKE_FILE_MISMATCH", "result review decision intake file", ::normalizePath)
        requireMatch(report.resultReviewDecisionId, decision.resultReviewDecisionId, "$.registryPacketValidation.resultReviewDecisionId", "$.humanAcceptanceDecision.resultReviewDecisionId", "DECISION_RESULT_REVIEW_DECISION_ID_MISSING", "DECISION_RESULT_REVIEW_DECISION_ID_MISMATCH", "result review decision id")
        requireMatch(report.providerHandoffPacketValidationReportFile, decision.providerHandoffPacketValidationReportFile, "$.registryPacketValidation.providerHandoffPacketValidationReportFile", "$.humanAcceptanceDecision.providerHandoffPacketValidationReportFile", "DECISION_PROVIDER_HANDOFF_PACKET_VALIDATION_REPORT_FILE_MISSING", "DECISION_PROVIDER_HANDOFF_PACKET_VALIDATION_REPORT_FILE_MISMATCH", "provider handoff packet validation report file", ::normalizePath)
        requireMatch(report.providerHandoffPacketId, decision.providerHandoffPacketId, "$.registryPacketValidation.providerHandoffPacketId", "$.humanAcceptanceDecision.providerHandoffPacketId", "DECISION_PROVIDER_HANDOFF_PACKET_ID_MISSING", "DECISION_PROVIDER_HANDOFF_PACKET_ID_MISMATCH", "provider handoff packet id")
        requireMatch(report.providerHandoffPreflightId, decision.providerHandoffPreflightId, "$.registryPacketValidation.providerHandoffPreflightId", "$.humanAcceptanceDecision.providerHandoffPreflightId", "DECISION_PROVIDER_HANDOFF_PREFLIGHT_ID_MISSING", "DECISION_PROVIDER_HANDOFF_PREFLIGHT_ID_MISMATCH", "provider handoff preflight id")
        requireMatch(report.providerHandoffDecisionId, decision.providerHandoffDecisionId, "$.registryPacketValidation.providerHandoffDecisionId", "$.humanAcceptanceDecision.providerHandoffDecisionId", "DECISION_PROVIDER_HANDOFF_DECISION_ID_MISSING", "DECISION_PROVIDER_HANDOFF_DECISION_ID_MISMATCH", "provider handoff decision id")
        requireMatch(report.externalResultIntakeFile, decision.externalResultIntakeFile, "$.registryPacketValidation.externalResultIntakeFile", "$.humanAcceptanceDecision.externalResultIntakeFile", "DECISION_EXTERNAL_RESULT_INTAKE_FILE_MISSING", "DECISION_EXTERNAL_RESULT_INTAKE_FILE_MISMATCH", "external result intake file", ::normalizePath)
        requireMatch(report.resultId, decision.resultId, "$.registryPacketValidation.resultId", "$.humanAcceptanceDecision.resultId", "DECISION_RESULT_ID_MISSING", "DECISION_RESULT_ID_MISMATCH", "result id")
        if (decision.scriptPacketDraftContentSha256.isNotBlank() && !sha256Regex.matches(decision.scriptPacketDraftContentSha256.trim().lowercase())) findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.humanAcceptanceDecision.scriptPacketDraftContentSha256", "Human acceptance decision requires a valid lowercase SHA-256 script packet draft hash.", evidence = listOf(decision.scriptPacketDraftContentSha256)))
        return count
    }

    private fun validateDecisionSource(decision: CandidateReferenceRegistryHumanAcceptanceDecision, findings: MutableList<CandidateReferenceRegistryHumanAcceptanceDecisionIntakeFinding>) {
        val source = decision.decisionSource.trim().lowercase()
        if (source.isBlank()) findings.add(finding("DECISION_SOURCE_MISSING", "human_decision", "CRITICAL", "$.humanAcceptanceDecision.decisionSource", "Human acceptance decision must declare a human/control/user source."))
        else if (source in disallowedSources || source !in allowedSources) findings.add(finding("DECISION_SOURCE_NOT_HUMAN", "human_decision", "CRITICAL", "$.humanAcceptanceDecision.decisionSource", "Human acceptance decision source cannot be automatic, model, generated, or unknown.", evidence = listOf(decision.decisionSource)))
    }

    private fun validateDecisionStatus(status: String, rawStatus: String, findings: MutableList<CandidateReferenceRegistryHumanAcceptanceDecisionIntakeFinding>) {
        if (status.isBlank()) findings.add(finding("DECISION_STATUS_MISSING", "human_decision", "CRITICAL", "$.humanAcceptanceDecision.decisionStatus", "Human acceptance decision status must be declared."))
        else if (status !in allowedStatuses) findings.add(finding("DECISION_STATUS_UNKNOWN", "human_decision", "CRITICAL", "$.humanAcceptanceDecision.decisionStatus", "Human acceptance decision status is outside the narrow allowed vocabulary.", evidence = listOf(rawStatus)))
    }

    private fun validateChecklist(decision: CandidateReferenceRegistryHumanAcceptanceDecision, allowDownstream: Boolean, findings: MutableList<CandidateReferenceRegistryHumanAcceptanceDecisionIntakeFinding>): Int {
        if (decision.checklistItems.isEmpty()) { findings.add(finding("HUMAN_CHECKLIST_MISSING", "human_decision", "CRITICAL", "$.humanAcceptanceDecision.checklistItems", "Human acceptance decision must include checklist item results.")); return 0 }
        var passed = 0
        decision.checklistItems.forEachIndexed { index, item ->
            val result = item.result.trim().uppercase()
            if (item.itemId.isBlank() || item.category.isBlank() || item.result.isBlank()) findings.add(finding("HUMAN_CHECKLIST_ITEM_INCOMPLETE", "human_decision", "CRITICAL", "$.humanAcceptanceDecision.checklistItems[$index]", "Human checklist item requires itemId, category, and result."))
            if (result in positiveChecklistResults) passed += 1 else if (allowDownstream) findings.add(finding("HUMAN_CHECKLIST_ITEM_NOT_ACCEPTED", "human_decision", "CRITICAL", "$.humanAcceptanceDecision.checklistItems[$index].result", "Allowing acceptance packet creation requires every checklist item to pass.", evidence = listOf(item.result)))
        }
        if (allowDownstream) {
            val accepted = decision.checklistItems.filter { it.result.trim().uppercase() in positiveChecklistResults }.map { it.category.trim().lowercase() }.toSet()
            val hasVisualOrDoNotShow = "visual_drift" in accepted || "do_not_show" in accepted
            val required = requiredChecklistCategories.filter { it != "visual_drift" && it != "do_not_show" }
            val missing = required.filterNot { it in accepted }.toMutableList()
            if (!hasVisualOrDoNotShow) missing += "visual_drift_or_do_not_show"
            if (missing.isNotEmpty()) findings.add(finding("HUMAN_CHECKLIST_REQUIRED_CATEGORY_MISSING", "human_decision", "CRITICAL", "$.humanAcceptanceDecision.checklistItems", "Allowing acceptance packet creation requires source binding, local reference integrity, no remote provider, visual drift/do-not-show, downstream boundary, and no approval/no lock.", evidence = missing))
        }
        return passed
    }

    private fun validateBlockingIssues(decision: CandidateReferenceRegistryHumanAcceptanceDecision, allowDownstream: Boolean, findings: MutableList<CandidateReferenceRegistryHumanAcceptanceDecisionIntakeFinding>): Int {
        var count = 0
        decision.blockingIssues.forEachIndexed { index, issue ->
            val unresolvedBlocking = issue.severity.trim().uppercase() in blockingIssueSeverities && issue.status.trim().uppercase() !in resolvedIssueStatuses
            if (unresolvedBlocking) { count += 1; if (allowDownstream) findings.add(finding("UNRESOLVED_BLOCKING_ISSUE_PRESENT", "human_decision", "CRITICAL", "$.humanAcceptanceDecision.blockingIssues[$index]", "Allowing acceptance packet creation requires no unresolved blocking issue.")) }
        }
        return count
    }

    private fun validateImageResultSourceBinding(validationResults: List<ImageCandidateExternalResultItem>, decisionResults: List<ImageCandidateExternalResultItem>, findings: MutableList<CandidateReferenceRegistryHumanAcceptanceDecisionIntakeFinding>): Int {
        var count = 0
        val validationIds = validationResults.map { it.resultItemId.trim() }
        val decisionIds = decisionResults.map { it.resultItemId.trim() }
        if (validationIds.toSet() != decisionIds.toSet()) findings.add(finding("IMAGE_RESULT_SET_MISMATCH", "source_linkage", "CRITICAL", "$.humanAcceptanceDecision.imageResults", "Human acceptance decision image result id set must match the validation report.", evidence = listOf("validation=${validationIds.joinToString(",")}", "decision=${decisionIds.joinToString(",")}")))
        val validationById = validationResults.associateBy { it.resultItemId.trim() }
        decisionResults.forEachIndexed { index, item ->
            val source = validationById[item.resultItemId.trim()]
            if (item.resultItemId.isBlank() || source == null) { findings.add(finding("IMAGE_RESULT_NOT_IN_VALIDATION_REPORT", "source_linkage", "CRITICAL", "$.humanAcceptanceDecision.imageResults[$index].resultItemId", "Human acceptance decision image result must exist in validation report.", evidence = listOf(item.resultItemId))); return@forEachIndexed }
            count += 1
            compareField(source.assetType, item.assetType, "$.humanAcceptanceDecision.imageResults[$index].assetType", "assetType", findings)
            compareField(source.promptItemId, item.promptItemId, "$.humanAcceptanceDecision.imageResults[$index].promptItemId", "promptItemId", findings)
            compareField(source.sourceCandidateId, item.sourceCandidateId, "$.humanAcceptanceDecision.imageResults[$index].sourceCandidateId", "sourceCandidateId", findings)
            compareField(source.localImagePath, item.localImagePath, "$.humanAcceptanceDecision.imageResults[$index].localImagePath", "localImagePath", findings, ::normalizePath)
            compareField(source.metadataOnlyReference, item.metadataOnlyReference, "$.humanAcceptanceDecision.imageResults[$index].metadataOnlyReference", "metadataOnlyReference", findings)
        }
        return count
    }

    private fun validateForbiddenRemoteOrProviderFields(results: List<ImageCandidateExternalResultItem>, findings: MutableList<CandidateReferenceRegistryHumanAcceptanceDecisionIntakeFinding>): Int {
        var count = 0
        results.forEachIndexed { index, item ->
            listOf(Triple(item.providerJobId, "providerJobId", "PROVIDER_JOB_ID_FORBIDDEN"), Triple(item.apiResponse, "apiResponse", "API_RESPONSE_FORBIDDEN"), Triple(item.remoteUrl, "remoteUrl", "REMOTE_URL_FORBIDDEN"), Triple(item.generatedStatus, "generatedStatus", "GENERATED_STATUS_FORBIDDEN")).forEach { (value, field, ruleId) ->
                if (value.isNotBlank()) { count += 1; findings.add(finding(ruleId, "safety_boundary", "CRITICAL", "$.imageResults[$index].$field", "Human acceptance decision cannot carry provider job, API response, remote URL, or generated status.", evidence = listOf(value))) }
            }
            if (item.localImagePath.isNotBlank() && urlRegex.containsMatchIn(item.localImagePath.trim())) { count += 1; findings.add(finding("REMOTE_IMAGE_PATH_FORBIDDEN", "safety_boundary", "CRITICAL", "$.imageResults[$index].localImagePath", "Human acceptance decision only allows local image paths.", evidence = listOf(item.localImagePath))) }
            if (item.metadataOnlyReference.isNotBlank() && (embeddedUrlRegex.containsMatchIn(item.metadataOnlyReference) || providerReferenceRegex.containsMatchIn(item.metadataOnlyReference))) { count += 1; findings.add(finding("REMOTE_METADATA_REFERENCE_FORBIDDEN", "safety_boundary", "CRITICAL", "$.imageResults[$index].metadataOnlyReference", "Human acceptance decision cannot carry remote URLs, provider job references, or API response references.", evidence = listOf(item.metadataOnlyReference))) }
        }
        return count
    }

    private fun scanText(decision: CandidateReferenceRegistryHumanAcceptanceDecision, results: List<ImageCandidateExternalResultItem>, patterns: List<Regex>, ruleId: String, category: String, findings: MutableList<CandidateReferenceRegistryHumanAcceptanceDecisionIntakeFinding>): Int {
        var count = 0
        val fields = mutableListOf(
            TextField("$.humanAcceptanceDecision.decisionStatus", decision.decisionStatus),
            TextField("$.humanAcceptanceDecision.notes", decision.notes),
        )
        decision.checklistItems.forEachIndexed { index, item -> fields += TextField("$.humanAcceptanceDecision.checklistItems[$index].note", item.note) }
        results.forEachIndexed { index, item ->
            fields += TextField("$.imageResults[$index].assetType", item.assetType)
            fields += TextField("$.imageResults[$index].candidateStatus", item.candidateStatus)
            fields += TextField("$.imageResults[$index].metadataOnlyReference", item.metadataOnlyReference)
            fields += TextField("$.imageResults[$index].notes", item.notes)
        }
        fields.filter { it.value.isNotBlank() }.forEach { field -> patterns.forEach { pattern -> if (pattern.containsMatchIn(field.value)) { count += 1; findings.add(finding(ruleId, category, "CRITICAL", field.path, "Human acceptance decision cannot carry approval, registered, lock, final, generated, canon, or runtime claims.", evidence = listOf(field.value))) } } }
        return count
    }

    private fun compareField(sourceValue: String, decisionValue: String, path: String, label: String, findings: MutableList<CandidateReferenceRegistryHumanAcceptanceDecisionIntakeFinding>, normalize: (String) -> String = { it.trim() }) {
        if (normalize(sourceValue) != normalize(decisionValue)) findings.add(finding("IMAGE_RESULT_SOURCE_MISMATCH", "source_linkage", "CRITICAL", path, "Human acceptance decision image result $label must match the validation report.", evidence = listOf("validation=$sourceValue", "decision=$decisionValue")))
    }

    private fun normalizePath(value: String): String = value.trim().replace('\\', '/')
    private fun finding(ruleId: String, category: String, severity: String, path: String, message: String, evidence: List<String> = emptyList(), suggestedFix: String = ""): CandidateReferenceRegistryHumanAcceptanceDecisionIntakeFinding =
        CandidateReferenceRegistryHumanAcceptanceDecisionIntakeFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
    private data class TextField(val path: String, val value: String)
}
