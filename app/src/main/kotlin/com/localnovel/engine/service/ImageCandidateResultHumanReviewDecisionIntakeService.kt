package com.localnovel.engine.service

import com.localnovel.engine.model.ImageCandidateExternalResultItem
import com.localnovel.engine.model.ImageCandidateResultReviewDecision
import com.localnovel.engine.model.ImageCandidateResultReviewDecisionIntakeEvidence
import com.localnovel.engine.model.ImageCandidateResultReviewDecisionIntakeFinding
import com.localnovel.engine.model.ImageCandidateResultReviewDecisionIntakeReport
import com.localnovel.engine.model.ImageCandidateResultReviewPreflightReport

class ImageCandidateResultHumanReviewDecisionIntakeService {
    private val requiredPreflightStep = "image_candidate_result_human_review_ready"
    private val downstreamStatus = "ALLOW_CANDIDATE_REFERENCE_PACKET"
    private val nextAllowedDownstreamStep = "image_candidate_reference_packet"
    private val allowedSources = setOf("human", "user", "01_control")
    private val disallowedSources = setOf("auto", "model", "generated")
    private val allowedStatuses = setOf(downstreamStatus, "REQUEST_CHANGES", "REJECT")
    private val positiveChecklistResults = setOf("PASS", "PASSED", "ACCEPTED", "OK", "YES")
    private val requiredChecklistCategories = setOf("visual_drift", "source_binding", "do_not_show_compliance", "prompt_safety", "downstream_boundary", "no_approval_no_lock")
    private val resolvedIssueStatuses = setOf("RESOLVED", "CLOSED", "ADDRESSED")
    private val blockingIssueSeverities = setOf("BLOCKING", "CRITICAL", "MAJOR")
    private val allowedResultStatuses = setOf("external_result_candidate", "needs_review")
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
    private val urlRegex = Regex("""(?i)^[a-z][a-z0-9+.-]*://""")
    private val embeddedUrlRegex = Regex("""(?i)\b[a-z][a-z0-9+.-]*://""")
    private val providerReferenceRegex = Regex("""(?i)\bprovider[-_\s]?job\b|\bprovider[-_\s]?job[-_\s]?id\b|\bapi[-_\s]?response\b""")
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
        Regex("""(?i)\basset\s+approved\b"""),
        Regex("""(?i)\bassets\s+approved\b"""),
        Regex("""(?i)\basset\s+approval\s+complete\b"""),
        Regex("""(?i)\bproduction\s+locked\b"""),
        Regex("""(?i)\bcanon\s+applied\b"""),
        Regex("""(?i)\bruntime\s+scene\s+created\b"""),
        Regex("\u5716\u7247\u5df2\u751f\u6210|\u7d20\u6750\u5df2\u6838\u51c6|\u88fd\u4f5c\u9396\u5b9a|\u6b63\u5178\u5df2\u5957\u7528|\u57f7\u884c\u671f\u5834\u666f\u5df2\u5efa\u7acb"),
    )

    fun validate(
        resultReviewPreflight: ImageCandidateResultReviewPreflightReport,
        resultReviewDecision: ImageCandidateResultReviewDecision,
        resultReviewPreflightFile: String = "",
        resultReviewDecisionFile: String = "",
    ): ImageCandidateResultReviewDecisionIntakeReport {
        val findings = mutableListOf<ImageCandidateResultReviewDecisionIntakeFinding>()
        validatePreflight(resultReviewPreflight, findings)
        val sourceCount = validateSourceBinding(resultReviewPreflight, resultReviewDecision, resultReviewPreflightFile, resultReviewDecisionFile, findings)
        validateDecisionSource(resultReviewDecision, findings)
        val normalizedStatus = resultReviewDecision.decisionStatus.trim().uppercase()
        validateDecisionStatus(normalizedStatus, resultReviewDecision.decisionStatus, findings)
        val allowDownstream = normalizedStatus == downstreamStatus
        val checklistPassedCount = validateChecklist(resultReviewDecision, allowDownstream, findings)
        val unresolvedBlockingCount = validateBlockingIssues(resultReviewDecision, allowDownstream, findings)
        validateImageResults(resultReviewPreflight.imageResults, allowDownstream, findings)
        val forbiddenRemoteOrProviderCount = validateForbiddenRemoteOrProviderFields(resultReviewPreflight.imageResults, findings)
        val forbiddenStatusCount = scanText(resultReviewDecision, resultReviewPreflight.imageResults, forbiddenStatusPatterns, "IMAGE_RESULT_STATUS_FORBIDDEN", "image_result_boundary", "Image result review decision cannot contain approved, locked, final, approved-reference, production-ready, or production-locked wording.", findings)
        val forbiddenClaimCount = scanText(resultReviewDecision, resultReviewPreflight.imageResults, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", "Image result review decision cannot claim generated media, asset approval, production lock, canon application, or runtime scene creation.", findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val downstreamAllowed = verdict == "PASS" && allowDownstream
        val blockedReason = if (verdict == "FAIL") findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId } else ""

        return ImageCandidateResultReviewDecisionIntakeReport(
            verdict = verdict,
            decisionId = resultReviewDecision.decisionId,
            resultReviewPreflightFile = resultReviewPreflightFile,
            resultReviewDecisionFile = resultReviewDecisionFile,
            resultReviewPreflightId = resultReviewPreflight.preflightId,
            externalResultIntakeFile = resultReviewPreflight.externalResultIntakeFile,
            resultId = resultReviewPreflight.resultId,
            providerHandoffPacketValidationReportFile = resultReviewPreflight.providerHandoffPacketValidationReportFile,
            packetId = resultReviewPreflight.packetId,
            preflightSourceId = resultReviewPreflight.preflightSourceId,
            providerDecisionId = resultReviewPreflight.decisionId,
            acceptedScriptPacketId = resultReviewPreflight.acceptedScriptPacketId,
            scriptPacketDraftFile = resultReviewPreflight.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = resultReviewPreflight.scriptPacketDraftContentSha256,
            decisionStatus = resultReviewDecision.decisionStatus,
            decisionAllowedDownstream = downstreamAllowed,
            imageResults = if (downstreamAllowed) resultReviewPreflight.imageResults else emptyList(),
            nextAllowedDownstreamStep = if (downstreamAllowed) nextAllowedDownstreamStep else "",
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = ImageCandidateResultReviewDecisionIntakeEvidence(
                resultReviewPreflightVerdict = resultReviewPreflight.verdict,
                resultReviewPreflightNextAllowedDownstreamStep = resultReviewPreflight.nextAllowedDownstreamStep,
                sourceBindingFieldCount = sourceCount,
                decisionSource = resultReviewDecision.decisionSource,
                decisionStatus = resultReviewDecision.decisionStatus,
                checklistItemCount = resultReviewDecision.checklistItems.size,
                checklistPassedCount = checklistPassedCount,
                unresolvedBlockingIssueCount = unresolvedBlockingCount,
                imageResultCount = resultReviewPreflight.imageResults.size,
                forbiddenRemoteOrProviderFieldCount = forbiddenRemoteOrProviderCount,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
            ),
        )
    }

    private fun validatePreflight(preflight: ImageCandidateResultReviewPreflightReport, findings: MutableList<ImageCandidateResultReviewDecisionIntakeFinding>) {
        if (preflight.verdict.trim().uppercase() != "PASS") findings.add(finding("RESULT_REVIEW_PREFLIGHT_NOT_PASS", "upstream", "CRITICAL", "$.resultReviewPreflight.verdict", "Image result review decision requires a PASS result review preflight.", evidence = listOf(preflight.verdict), suggestedFix = "Use the PASS result review preflight."))
        if (preflight.nextAllowedDownstreamStep != requiredPreflightStep) findings.add(finding("RESULT_REVIEW_PREFLIGHT_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.resultReviewPreflight.nextAllowedDownstreamStep", "Image result review decision requires nextAllowedDownstreamStep=image_candidate_result_human_review_ready.", evidence = listOf(preflight.nextAllowedDownstreamStep), suggestedFix = "Use the preflight that explicitly allows human review readiness."))
    }

    private fun validateSourceBinding(preflight: ImageCandidateResultReviewPreflightReport, decision: ImageCandidateResultReviewDecision, preflightFile: String, decisionFile: String, findings: MutableList<ImageCandidateResultReviewDecisionIntakeFinding>): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Image result review decision intake requires nonblank $label.", suggestedFix = "Use matching result review preflight and human decision JSON.")) else count += 1
        }
        fun requireMatch(preflightValue: String, decisionValue: String, preflightPath: String, decisionPath: String, missingRuleId: String, mismatchRuleId: String, label: String, normalize: (String) -> String = { it.trim() }) {
            requireNonBlank(preflightValue, preflightPath, "${missingRuleId}_PREFLIGHT", "$label in preflight")
            requireNonBlank(decisionValue, decisionPath, missingRuleId, "$label in decision")
            val left = normalize(preflightValue)
            val right = normalize(decisionValue)
            if (left.isNotBlank() && right.isNotBlank() && left != right) findings.add(finding(mismatchRuleId, "source_linkage", "CRITICAL", decisionPath, "Image result review decision source binding mismatch for $label.", evidence = listOf("preflight=$preflightValue", "decision=$decisionValue"), suggestedFix = "Use a decision JSON created for this result review preflight."))
        }
        requireNonBlank(preflightFile, "$.inputs.resultReviewPreflightFile", "RESULT_REVIEW_PREFLIGHT_FILE_MISSING", "result review preflight file")
        requireNonBlank(decisionFile, "$.inputs.resultReviewDecisionFile", "RESULT_REVIEW_DECISION_FILE_MISSING", "result review decision file")
        requireNonBlank(decision.decisionId, "$.resultReviewDecision.decisionId", "DECISION_ID_MISSING", "decision id")
        requireNonBlank(decision.reviewer, "$.resultReviewDecision.reviewer", "DECISION_REVIEWER_MISSING", "reviewer")
        requireNonBlank(decision.reviewerRole, "$.resultReviewDecision.reviewerRole", "DECISION_REVIEWER_ROLE_MISSING", "reviewer role")
        requireNonBlank(decision.decisionAt, "$.resultReviewDecision.decisionAt", "DECISION_AT_MISSING", "decision timestamp")
        requireNonBlank(decision.resultReviewPreflightFile, "$.resultReviewDecision.resultReviewPreflightFile", "DECISION_PREFLIGHT_FILE_MISSING", "preflight file in decision")
        if (preflightFile.isNotBlank() && decision.resultReviewPreflightFile.isNotBlank() && normalizePath(preflightFile) != normalizePath(decision.resultReviewPreflightFile)) findings.add(finding("DECISION_PREFLIGHT_FILE_MISMATCH", "source_linkage", "CRITICAL", "$.resultReviewDecision.resultReviewPreflightFile", "Decision must point to the same result review preflight file.", evidence = listOf("input=$preflightFile", "decision=${decision.resultReviewPreflightFile}"), suggestedFix = "Use the decision JSON for this preflight file."))
        requireMatch(preflight.preflightId, decision.resultReviewPreflightId, "$.resultReviewPreflight.preflightId", "$.resultReviewDecision.resultReviewPreflightId", "DECISION_PREFLIGHT_ID_MISSING", "DECISION_PREFLIGHT_ID_MISMATCH", "result review preflight id")
        requireMatch(preflight.externalResultIntakeFile, decision.externalResultIntakeFile, "$.resultReviewPreflight.externalResultIntakeFile", "$.resultReviewDecision.externalResultIntakeFile", "DECISION_EXTERNAL_RESULT_INTAKE_FILE_MISSING", "DECISION_EXTERNAL_RESULT_INTAKE_FILE_MISMATCH", "external result intake file", ::normalizePath)
        requireMatch(preflight.resultId, decision.resultId, "$.resultReviewPreflight.resultId", "$.resultReviewDecision.resultId", "DECISION_RESULT_ID_MISSING", "DECISION_RESULT_ID_MISMATCH", "result id")
        requireMatch(preflight.providerHandoffPacketValidationReportFile, decision.providerHandoffPacketValidationReportFile, "$.resultReviewPreflight.providerHandoffPacketValidationReportFile", "$.resultReviewDecision.providerHandoffPacketValidationReportFile", "DECISION_PROVIDER_HANDOFF_PACKET_VALIDATION_REPORT_FILE_MISSING", "DECISION_PROVIDER_HANDOFF_PACKET_VALIDATION_REPORT_FILE_MISMATCH", "provider handoff packet validation report file", ::normalizePath)
        requireMatch(preflight.packetId, decision.packetId, "$.resultReviewPreflight.packetId", "$.resultReviewDecision.packetId", "DECISION_PACKET_ID_MISSING", "DECISION_PACKET_ID_MISMATCH", "provider handoff packet id")
        requireMatch(preflight.preflightSourceId, decision.preflightSourceId, "$.resultReviewPreflight.preflightSourceId", "$.resultReviewDecision.preflightSourceId", "DECISION_PROVIDER_PREFLIGHT_ID_MISSING", "DECISION_PROVIDER_PREFLIGHT_ID_MISMATCH", "provider handoff preflight id")
        requireMatch(preflight.decisionId, decision.providerDecisionId, "$.resultReviewPreflight.decisionId", "$.resultReviewDecision.providerDecisionId", "DECISION_PROVIDER_DECISION_ID_MISSING", "DECISION_PROVIDER_DECISION_ID_MISMATCH", "provider handoff decision id")
        requireMatch(preflight.acceptedScriptPacketId, decision.acceptedScriptPacketId, "$.resultReviewPreflight.acceptedScriptPacketId", "$.resultReviewDecision.acceptedScriptPacketId", "DECISION_ACCEPTED_SCRIPT_PACKET_ID_MISSING", "DECISION_ACCEPTED_SCRIPT_PACKET_ID_MISMATCH", "accepted script packet id")
        requireMatch(preflight.scriptPacketDraftFile, decision.scriptPacketDraftFile, "$.resultReviewPreflight.scriptPacketDraftFile", "$.resultReviewDecision.scriptPacketDraftFile", "DECISION_SCRIPT_PACKET_DRAFT_FILE_MISSING", "DECISION_SCRIPT_PACKET_DRAFT_FILE_MISMATCH", "script packet draft file", ::normalizePath)
        requireMatch(preflight.scriptPacketDraftContentSha256, decision.scriptPacketDraftContentSha256, "$.resultReviewPreflight.scriptPacketDraftContentSha256", "$.resultReviewDecision.scriptPacketDraftContentSha256", "DECISION_SCRIPT_PACKET_DRAFT_HASH_MISSING", "DECISION_SCRIPT_PACKET_DRAFT_HASH_MISMATCH", "script packet draft SHA-256", { it.trim().lowercase() })
        if (decision.scriptPacketDraftContentSha256.isNotBlank() && !sha256Regex.matches(decision.scriptPacketDraftContentSha256.trim().lowercase())) findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.resultReviewDecision.scriptPacketDraftContentSha256", "Image result review decision requires a valid lowercase SHA-256 script packet draft hash.", evidence = listOf(decision.scriptPacketDraftContentSha256), suggestedFix = "Use the hash carried by the preflight."))
        return count
    }

    private fun validateDecisionSource(decision: ImageCandidateResultReviewDecision, findings: MutableList<ImageCandidateResultReviewDecisionIntakeFinding>) {
        val source = decision.decisionSource.trim().lowercase()
        if (source.isBlank()) findings.add(finding("DECISION_SOURCE_MISSING", "human_decision", "CRITICAL", "$.resultReviewDecision.decisionSource", "Image result review decision must declare a human/control/user source.", suggestedFix = "Set decisionSource to human, user, or 01_control."))
        else if (source in disallowedSources || source !in allowedSources) findings.add(finding("DECISION_SOURCE_NOT_HUMAN", "human_decision", "CRITICAL", "$.resultReviewDecision.decisionSource", "Image result review decision source cannot be automatic, model, generated, or unknown.", evidence = listOf(decision.decisionSource), suggestedFix = "Use an explicit external human/control/user decision source."))
    }

    private fun validateDecisionStatus(normalizedStatus: String, rawStatus: String, findings: MutableList<ImageCandidateResultReviewDecisionIntakeFinding>) {
        if (normalizedStatus.isBlank()) findings.add(finding("DECISION_STATUS_MISSING", "human_decision", "CRITICAL", "$.resultReviewDecision.decisionStatus", "Image result review decision status must be declared.", suggestedFix = "Use ALLOW_CANDIDATE_REFERENCE_PACKET, REQUEST_CHANGES, or REJECT."))
        else if (normalizedStatus !in allowedStatuses) findings.add(finding("DECISION_STATUS_UNKNOWN", "human_decision", "CRITICAL", "$.resultReviewDecision.decisionStatus", "Image result review decision status is outside the narrow allowed vocabulary.", evidence = listOf(rawStatus), suggestedFix = "Use ALLOW_CANDIDATE_REFERENCE_PACKET, REQUEST_CHANGES, or REJECT."))
    }

    private fun validateChecklist(decision: ImageCandidateResultReviewDecision, allowDownstream: Boolean, findings: MutableList<ImageCandidateResultReviewDecisionIntakeFinding>): Int {
        if (decision.checklistItems.isEmpty()) {
            findings.add(finding("HUMAN_CHECKLIST_MISSING", "human_decision", "CRITICAL", "$.resultReviewDecision.checklistItems", "Image result review decision must include checklist item results.", suggestedFix = "Record human checklist results before allowing candidate reference packet creation."))
            return 0
        }
        var passed = 0
        decision.checklistItems.forEachIndexed { index, item ->
            if (item.itemId.isBlank() || item.category.isBlank() || item.result.isBlank()) findings.add(finding("HUMAN_CHECKLIST_ITEM_INCOMPLETE", "human_decision", "CRITICAL", "$.resultReviewDecision.checklistItems[$index]", "Human checklist item requires itemId, category, and result.", suggestedFix = "Complete every checklist item."))
            val result = item.result.trim().uppercase()
            if (result in positiveChecklistResults) passed += 1 else if (allowDownstream) findings.add(finding("HUMAN_CHECKLIST_ITEM_NOT_ACCEPTED", "human_decision", "CRITICAL", "$.resultReviewDecision.checklistItems[$index].result", "Allowing candidate reference packet creation requires every checklist item to pass.", evidence = listOf(item.result), suggestedFix = "Use REQUEST_CHANGES or REJECT until every required item is accepted."))
        }
        if (allowDownstream) {
            val accepted = decision.checklistItems.filter { it.result.trim().uppercase() in positiveChecklistResults }.map { it.category.trim().lowercase() }.toSet()
            val missing = requiredChecklistCategories.filterNot { it in accepted }
            if (missing.isNotEmpty()) findings.add(finding("HUMAN_CHECKLIST_REQUIRED_CATEGORY_MISSING", "human_decision", "CRITICAL", "$.resultReviewDecision.checklistItems", "Allowing candidate reference packet creation requires accepted checklist coverage for visual drift, source binding, do-not-show compliance, prompt safety, downstream boundary, and no approval/no lock.", evidence = missing, suggestedFix = "Record positive human checklist results for every required category."))
        }
        return passed
    }

    private fun validateBlockingIssues(decision: ImageCandidateResultReviewDecision, allowDownstream: Boolean, findings: MutableList<ImageCandidateResultReviewDecisionIntakeFinding>): Int {
        var count = 0
        decision.blockingIssues.forEachIndexed { index, issue ->
            val unresolvedBlocking = issue.severity.trim().uppercase() in blockingIssueSeverities && issue.status.trim().uppercase() !in resolvedIssueStatuses
            if (unresolvedBlocking) {
                count += 1
                if (allowDownstream) findings.add(finding("UNRESOLVED_BLOCKING_ISSUE_PRESENT", "human_decision", "CRITICAL", "$.resultReviewDecision.blockingIssues[$index]", "Allowing candidate reference packet creation requires no unresolved blocking issue.", evidence = listOf(issue.issueId, issue.severity, issue.status).filter { it.isNotBlank() }, suggestedFix = "Resolve the blocking issue or use REQUEST_CHANGES / REJECT."))
            }
        }
        return count
    }

    private fun validateImageResults(results: List<ImageCandidateExternalResultItem>, allowDownstream: Boolean, findings: MutableList<ImageCandidateResultReviewDecisionIntakeFinding>) {
        if (allowDownstream && results.isEmpty()) findings.add(finding("IMAGE_RESULTS_MISSING", "image_result_boundary", "CRITICAL", "$.resultReviewPreflight.imageResults", "Allowing candidate reference packet creation requires candidate image result entries.", suggestedFix = "Use a result review preflight carrying candidate results."))
        results.forEachIndexed { index, item ->
            if (item.resultItemId.isBlank() || item.promptItemId.isBlank() || item.sourceCandidateId.isBlank() || item.assetType.isBlank()) findings.add(finding("IMAGE_RESULT_ITEM_INCOMPLETE", "image_result_boundary", "CRITICAL", "$.resultReviewPreflight.imageResults[$index]", "Image result item requires result id, prompt item id, source candidate id, and asset type.", suggestedFix = "Use complete candidate result entries."))
            if (item.localImagePath.isBlank() && item.metadataOnlyReference.isBlank()) findings.add(finding("IMAGE_RESULT_REFERENCE_MISSING", "image_result_boundary", "CRITICAL", "$.resultReviewPreflight.imageResults[$index]", "Image result item requires local image path or metadata-only reference.", suggestedFix = "Keep the human-provided candidate reference visible."))
            if (item.candidateStatus.trim().lowercase() !in allowedResultStatuses) findings.add(finding("IMAGE_RESULT_STATUS_NOT_ALLOWED", "image_result_boundary", "CRITICAL", "$.resultReviewPreflight.imageResults[$index].candidateStatus", "Image result status must remain candidate-only.", evidence = listOf(item.candidateStatus), suggestedFix = "Use external_result_candidate or needs_review."))
        }
    }

    private fun validateForbiddenRemoteOrProviderFields(results: List<ImageCandidateExternalResultItem>, findings: MutableList<ImageCandidateResultReviewDecisionIntakeFinding>): Int {
        var count = 0
        results.forEachIndexed { index, item ->
            listOf(
                Triple(item.providerJobId, "$.resultReviewPreflight.imageResults[$index].providerJobId", "PROVIDER_JOB_ID_FORBIDDEN"),
                Triple(item.apiResponse, "$.resultReviewPreflight.imageResults[$index].apiResponse", "API_RESPONSE_FORBIDDEN"),
                Triple(item.remoteUrl, "$.resultReviewPreflight.imageResults[$index].remoteUrl", "REMOTE_URL_FORBIDDEN"),
                Triple(item.generatedStatus, "$.resultReviewPreflight.imageResults[$index].generatedStatus", "GENERATED_STATUS_FORBIDDEN"),
            ).forEach { (value, path, ruleId) ->
                if (value.isNotBlank()) { count += 1; findings.add(finding(ruleId, "safety_boundary", "CRITICAL", path, "Image result review decision cannot carry provider job, API response, remote URL, or generated status.", evidence = listOf(value), suggestedFix = "Keep only local candidate review metadata.")) }
            }
            if (item.localImagePath.isNotBlank() && urlRegex.containsMatchIn(item.localImagePath.trim())) { count += 1; findings.add(finding("REMOTE_IMAGE_PATH_FORBIDDEN", "safety_boundary", "CRITICAL", "$.resultReviewPreflight.imageResults[$index].localImagePath", "Image result review decision only allows local/controlled image path references, not remote URLs.", evidence = listOf(item.localImagePath), suggestedFix = "Use a local path or metadata-only reference.")) }
            if (item.metadataOnlyReference.isNotBlank() && (embeddedUrlRegex.containsMatchIn(item.metadataOnlyReference) || providerReferenceRegex.containsMatchIn(item.metadataOnlyReference))) { count += 1; findings.add(finding("REMOTE_METADATA_REFERENCE_FORBIDDEN", "safety_boundary", "CRITICAL", "$.resultReviewPreflight.imageResults[$index].metadataOnlyReference", "Image result review decision cannot use metadataOnlyReference to carry remote URLs, provider job references, or API response references.", evidence = listOf(item.metadataOnlyReference), suggestedFix = "Use local candidate metadata that does not point to remote provider state.")) }
        }
        return count
    }

    private fun scanText(decision: ImageCandidateResultReviewDecision, results: List<ImageCandidateExternalResultItem>, patterns: List<Regex>, ruleId: String, category: String, message: String, findings: MutableList<ImageCandidateResultReviewDecisionIntakeFinding>): Int {
        var count = 0
        collectTextFields(decision, results).forEach { field -> patterns.forEach { pattern -> if (pattern.containsMatchIn(field.value)) { count += 1; findings.add(finding(ruleId, category, "CRITICAL", field.path, message, evidence = listOf(field.value), suggestedFix = "Keep the decision candidate-only and unapproved.")) } } }
        return count
    }

    private fun collectTextFields(decision: ImageCandidateResultReviewDecision, results: List<ImageCandidateExternalResultItem>): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) { if (value.isNotBlank()) fields += TextField(path, value) }
        add("$.resultReviewDecision.decisionSource", decision.decisionSource)
        add("$.resultReviewDecision.reviewer", decision.reviewer)
        add("$.resultReviewDecision.reviewerRole", decision.reviewerRole)
        add("$.resultReviewDecision.decisionStatus", decision.decisionStatus)
        add("$.resultReviewDecision.notes", decision.notes)
        decision.checklistItems.forEachIndexed { index, item ->
            add("$.resultReviewDecision.checklistItems[$index].category", item.category)
            add("$.resultReviewDecision.checklistItems[$index].result", item.result)
            add("$.resultReviewDecision.checklistItems[$index].note", item.note)
        }
        decision.blockingIssues.forEachIndexed { index, issue ->
            add("$.resultReviewDecision.blockingIssues[$index].description", issue.description)
            add("$.resultReviewDecision.blockingIssues[$index].status", issue.status)
        }
        results.forEachIndexed { index, item ->
            add("$.resultReviewPreflight.imageResults[$index].assetType", item.assetType)
            add("$.resultReviewPreflight.imageResults[$index].candidateStatus", item.candidateStatus)
            add("$.resultReviewPreflight.imageResults[$index].localImagePath", item.localImagePath)
            add("$.resultReviewPreflight.imageResults[$index].metadataOnlyReference", item.metadataOnlyReference)
            add("$.resultReviewPreflight.imageResults[$index].notes", item.notes)
            item.doNotShowChecklist.forEachIndexed { itemIndex, value -> add("$.resultReviewPreflight.imageResults[$index].doNotShowChecklist[$itemIndex]", value) }
            item.driftReviewChecklist.forEachIndexed { itemIndex, value -> add("$.resultReviewPreflight.imageResults[$index].driftReviewChecklist[$itemIndex]", value) }
            item.residualRisks.forEachIndexed { itemIndex, value -> add("$.resultReviewPreflight.imageResults[$index].residualRisks[$itemIndex]", value) }
        }
        return fields
    }

    private fun normalizePath(value: String): String = value.trim().replace('\\', '/')
    private fun finding(ruleId: String, category: String, severity: String, path: String, message: String, evidence: List<String> = emptyList(), suggestedFix: String = ""): ImageCandidateResultReviewDecisionIntakeFinding =
        ImageCandidateResultReviewDecisionIntakeFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
    private data class TextField(val path: String, val value: String)
}
