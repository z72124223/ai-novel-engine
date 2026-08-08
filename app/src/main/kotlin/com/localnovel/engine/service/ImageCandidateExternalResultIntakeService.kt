package com.localnovel.engine.service

import com.localnovel.engine.model.ImageCandidateExternalResult
import com.localnovel.engine.model.ImageCandidateExternalResultIntakeEvidence
import com.localnovel.engine.model.ImageCandidateExternalResultIntakeFinding
import com.localnovel.engine.model.ImageCandidateExternalResultIntakeReport
import com.localnovel.engine.model.ImageCandidateExternalResultItem
import com.localnovel.engine.model.ImageCandidateProviderHandoffPacketValidationReport
import com.localnovel.engine.model.ImageCandidatePromptItem

class ImageCandidateExternalResultIntakeService {
    private val requiredValidationStep = "image_candidate_manual_external_action_ready"
    private val nextAllowedDownstreamStep = "image_candidate_result_review_preflight"
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
    private val allowedSources = setOf("human", "user", "01_control")
    private val disallowedSources = setOf("auto", "model", "generated")
    private val allowedStatuses = setOf("external_result_candidate", "needs_review")
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

    fun validate(
        providerHandoffPacketValidation: ImageCandidateProviderHandoffPacketValidationReport,
        externalResult: ImageCandidateExternalResult,
        providerHandoffPacketValidationReportFile: String = "",
        externalResultFile: String = "",
    ): ImageCandidateExternalResultIntakeReport {
        val findings = mutableListOf<ImageCandidateExternalResultIntakeFinding>()
        validateUpstream(providerHandoffPacketValidation, findings)
        val sourceCount = validateSourceBinding(providerHandoffPacketValidation, externalResult, providerHandoffPacketValidationReportFile, externalResultFile, findings)
        validateResultSource(externalResult, findings)
        validateStatus("$.externalResult.resultStatus", externalResult.resultStatus, "RESULT_STATUS_NOT_ALLOWED", findings)
        val linkedItemCount = validateImageResults(providerHandoffPacketValidation.promptItems, externalResult.imageResults, findings)
        val forbiddenRemoteOrProviderCount = validateForbiddenRemoteOrProviderFields(externalResult, findings)
        val forbiddenStatusCount = scanText(externalResult, forbiddenStatusPatterns, "IMAGE_RESULT_STATUS_FORBIDDEN", "image_result_boundary", "External result intake cannot contain approved, locked, final, review-ready, production-ready, approved-reference, or production-locked wording.", findings)
        val forbiddenClaimCount = scanText(externalResult, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", "External result intake cannot claim generated media, asset approval, production lock, canon application, prompt submission, or runtime scene creation.", findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") {
            findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId }
        } else {
            ""
        }

        return ImageCandidateExternalResultIntakeReport(
            verdict = verdict,
            resultId = externalResult.resultId,
            providerHandoffPacketValidationReportFile = providerHandoffPacketValidationReportFile,
            externalResultFile = externalResultFile,
            packetId = externalResult.packetId,
            preflightId = externalResult.preflightId,
            decisionId = externalResult.decisionId,
            acceptedScriptPacketId = externalResult.acceptedScriptPacketId,
            scriptPacketDraftFile = externalResult.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = externalResult.scriptPacketDraftContentSha256,
            resultStatus = externalResult.resultStatus,
            imageResults = if (verdict == "FAIL") emptyList() else externalResult.imageResults,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = ImageCandidateExternalResultIntakeEvidence(
                providerHandoffPacketValidationVerdict = providerHandoffPacketValidation.verdict,
                providerHandoffPacketValidationNextAllowedDownstreamStep = providerHandoffPacketValidation.nextAllowedDownstreamStep,
                sourceBindingFieldCount = sourceCount,
                resultSource = externalResult.resultSource,
                resultStatus = externalResult.resultStatus,
                promptItemCount = providerHandoffPacketValidation.promptItems.size,
                imageResultCount = externalResult.imageResults.size,
                localImageReferenceCount = externalResult.imageResults.count { it.localImagePath.isNotBlank() },
                metadataOnlyReferenceCount = externalResult.imageResults.count { it.metadataOnlyReference.isNotBlank() },
                forbiddenRemoteOrProviderFieldCount = forbiddenRemoteOrProviderCount,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
            ),
        )
    }

    private fun validateUpstream(
        report: ImageCandidateProviderHandoffPacketValidationReport,
        findings: MutableList<ImageCandidateExternalResultIntakeFinding>,
    ) {
        if (report.verdict.trim().uppercase() != "PASS") {
            findings.add(finding("PROVIDER_HANDOFF_PACKET_VALIDATION_NOT_PASS", "upstream", "CRITICAL", "$.providerHandoffPacketValidation.verdict", "External result intake requires a PASS provider handoff packet validation report.", evidence = listOf(report.verdict), suggestedFix = "Use the PASS packet validation report."))
        }
        if (report.nextAllowedDownstreamStep != requiredValidationStep) {
            findings.add(finding("PROVIDER_HANDOFF_PACKET_VALIDATION_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.providerHandoffPacketValidation.nextAllowedDownstreamStep", "External result intake requires nextAllowedDownstreamStep=image_candidate_manual_external_action_ready.", evidence = listOf(report.nextAllowedDownstreamStep), suggestedFix = "Use the validation report that explicitly allows manual external action readiness."))
        }
    }

    private fun validateSourceBinding(
        report: ImageCandidateProviderHandoffPacketValidationReport,
        result: ImageCandidateExternalResult,
        reportFile: String,
        resultFile: String,
        findings: MutableList<ImageCandidateExternalResultIntakeFinding>,
    ): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) {
                findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "External result intake requires nonblank $label.", suggestedFix = "Use a complete validation report and matching external result JSON."))
            } else {
                count += 1
            }
        }
        fun requireMatch(reportValue: String, resultValue: String, reportPath: String, resultPath: String, missingRuleId: String, mismatchRuleId: String, label: String, normalize: (String) -> String = { it.trim() }) {
            requireNonBlank(reportValue, reportPath, "${missingRuleId}_REPORT", "$label in validation report")
            requireNonBlank(resultValue, resultPath, missingRuleId, "$label in external result")
            val left = normalize(reportValue)
            val right = normalize(resultValue)
            if (left.isNotBlank() && right.isNotBlank() && left != right) {
                findings.add(finding(mismatchRuleId, "source_linkage", "CRITICAL", resultPath, "External result source binding mismatch for $label.", evidence = listOf("report=$reportValue", "externalResult=$resultValue"), suggestedFix = "Use an external result JSON created for this provider handoff packet validation report."))
            }
        }

        requireNonBlank(reportFile, "$.inputs.providerHandoffPacketValidationReportFile", "PROVIDER_HANDOFF_PACKET_VALIDATION_REPORT_FILE_MISSING", "provider handoff packet validation report file")
        requireNonBlank(resultFile, "$.inputs.externalResultFile", "EXTERNAL_RESULT_FILE_MISSING", "external result file")
        requireNonBlank(result.resultId, "$.externalResult.resultId", "EXTERNAL_RESULT_ID_MISSING", "external result id")
        requireNonBlank(result.providerHandoffPacketValidationReportFile, "$.externalResult.providerHandoffPacketValidationReportFile", "EXTERNAL_RESULT_VALIDATION_REPORT_FILE_MISSING", "validation report file in external result")
        if (reportFile.isNotBlank() && result.providerHandoffPacketValidationReportFile.isNotBlank() && normalizePath(reportFile) != normalizePath(result.providerHandoffPacketValidationReportFile)) {
            findings.add(finding("EXTERNAL_RESULT_VALIDATION_REPORT_FILE_MISMATCH", "source_linkage", "CRITICAL", "$.externalResult.providerHandoffPacketValidationReportFile", "External result must point to the same provider handoff packet validation report file.", evidence = listOf("input=$reportFile", "externalResult=${result.providerHandoffPacketValidationReportFile}"), suggestedFix = "Use the external result JSON for this validation report."))
        }
        requireMatch(report.packetId, result.packetId, "$.providerHandoffPacketValidation.packetId", "$.externalResult.packetId", "PACKET_ID_MISSING", "PACKET_ID_MISMATCH", "packet id")
        requireMatch(report.preflightId, result.preflightId, "$.providerHandoffPacketValidation.preflightId", "$.externalResult.preflightId", "PREFLIGHT_ID_MISSING", "PREFLIGHT_ID_MISMATCH", "preflight id")
        requireMatch(report.decisionId, result.decisionId, "$.providerHandoffPacketValidation.decisionId", "$.externalResult.decisionId", "DECISION_ID_MISSING", "DECISION_ID_MISMATCH", "decision id")
        requireMatch(report.acceptedScriptPacketId, result.acceptedScriptPacketId, "$.providerHandoffPacketValidation.acceptedScriptPacketId", "$.externalResult.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "ACCEPTED_SCRIPT_PACKET_ID_MISMATCH", "accepted script packet id")
        requireMatch(report.scriptPacketDraftFile, result.scriptPacketDraftFile, "$.providerHandoffPacketValidation.scriptPacketDraftFile", "$.externalResult.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "SCRIPT_PACKET_DRAFT_FILE_MISMATCH", "script packet draft file", ::normalizePath)
        requireMatch(report.scriptPacketDraftContentSha256, result.scriptPacketDraftContentSha256, "$.providerHandoffPacketValidation.scriptPacketDraftContentSha256", "$.externalResult.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "SCRIPT_PACKET_DRAFT_HASH_MISMATCH", "script packet draft SHA-256", { it.trim().lowercase() })
        if (result.scriptPacketDraftContentSha256.isNotBlank() && !sha256Regex.matches(result.scriptPacketDraftContentSha256.trim().lowercase())) {
            findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.externalResult.scriptPacketDraftContentSha256", "External result intake requires a valid lowercase SHA-256 script packet draft hash.", evidence = listOf(result.scriptPacketDraftContentSha256), suggestedFix = "Use the script hash carried by the validation report lineage."))
        }
        return count
    }

    private fun validateResultSource(result: ImageCandidateExternalResult, findings: MutableList<ImageCandidateExternalResultIntakeFinding>) {
        val source = result.resultSource.trim().lowercase()
        if (source.isBlank()) {
            findings.add(finding("RESULT_SOURCE_MISSING", "human_result", "CRITICAL", "$.externalResult.resultSource", "External result must declare a human/control/user source.", suggestedFix = "Set resultSource to human, user, or 01_control."))
        } else if (source in disallowedSources || source !in allowedSources) {
            findings.add(finding("RESULT_SOURCE_NOT_HUMAN", "human_result", "CRITICAL", "$.externalResult.resultSource", "External result source cannot be automatic, model, generated, or unknown.", evidence = listOf(result.resultSource), suggestedFix = "Use an explicit external human/control/user result source."))
        }
        listOf(
            Triple(result.recorder, "$.externalResult.recorder", "RESULT_RECORDER_MISSING"),
            Triple(result.recorderRole, "$.externalResult.recorderRole", "RESULT_RECORDER_ROLE_MISSING"),
            Triple(result.resultAt, "$.externalResult.resultAt", "RESULT_AT_MISSING"),
        ).forEach { (value, path, ruleId) ->
            if (value.isBlank()) {
                findings.add(finding(ruleId, "human_result", "CRITICAL", path, "External result requires recorder, role, and timestamp.", suggestedFix = "Record who provided the external result and when."))
            }
        }
    }

    private fun validateStatus(path: String, status: String, ruleId: String, findings: MutableList<ImageCandidateExternalResultIntakeFinding>) {
        val normalized = status.trim().lowercase()
        if (normalized.isBlank() || normalized !in allowedStatuses) {
            findings.add(finding(ruleId, "image_result_boundary", "CRITICAL", path, "External result status must remain candidate-only: external_result_candidate or needs_review.", evidence = listOf(status), suggestedFix = "Use external_result_candidate or needs_review."))
        }
    }

    private fun validateImageResults(
        promptItems: List<ImageCandidatePromptItem>,
        results: List<ImageCandidateExternalResultItem>,
        findings: MutableList<ImageCandidateExternalResultIntakeFinding>,
    ): Int {
        if (results.isEmpty()) {
            findings.add(finding("IMAGE_RESULTS_MISSING", "image_result_boundary", "CRITICAL", "$.externalResult.imageResults", "External result intake requires at least one image result candidate.", suggestedFix = "Provide a candidate image result entry or metadata-only reference."))
            return 0
        }
        val promptMap = promptItems.associateBy { it.promptItemId }
        var linked = 0
        results.forEachIndexed { index, item ->
            if (item.resultItemId.isBlank() || item.promptItemId.isBlank() || item.sourceCandidateId.isBlank() || item.assetType.isBlank()) {
                findings.add(finding("IMAGE_RESULT_ITEM_INCOMPLETE", "image_result_boundary", "CRITICAL", "$.externalResult.imageResults[$index]", "Image result item requires result id, prompt item id, source candidate id, and asset type.", suggestedFix = "Provide a complete candidate result item."))
            }
            val prompt = promptMap[item.promptItemId]
            if (prompt == null) {
                findings.add(finding("PROMPT_ITEM_ID_NOT_FOUND", "source_linkage", "CRITICAL", "$.externalResult.imageResults[$index].promptItemId", "Image result item must reference a prompt item from the provider handoff packet validation report.", evidence = listOf(item.promptItemId), suggestedFix = "Use a promptItemId carried by the validation report."))
            } else if (prompt.sourceCandidateId != item.sourceCandidateId) {
                findings.add(finding("SOURCE_CANDIDATE_ID_MISMATCH", "source_linkage", "CRITICAL", "$.externalResult.imageResults[$index].sourceCandidateId", "Image result item source candidate id must match the referenced prompt item.", evidence = listOf("prompt=${prompt.sourceCandidateId}", "result=${item.sourceCandidateId}"), suggestedFix = "Use the sourceCandidateId paired with the prompt item."))
            } else {
                linked += 1
            }
            if (item.localImagePath.isBlank() && item.metadataOnlyReference.isBlank()) {
                findings.add(finding("IMAGE_RESULT_REFERENCE_MISSING", "image_result_boundary", "CRITICAL", "$.externalResult.imageResults[$index]", "Image result item requires a local image path or metadata-only reference.", suggestedFix = "Provide a local controlled file path or metadata-only placeholder for later human review."))
            }
            if (item.doNotShowChecklist.isEmpty()) {
                findings.add(finding("DO_NOT_SHOW_CHECKLIST_MISSING", "image_result_boundary", "CRITICAL", "$.externalResult.imageResults[$index].doNotShowChecklist", "Image result intake requires do-not-show checklist evidence.", suggestedFix = "Record do-not-show checks before review preflight."))
            }
            if (item.driftReviewChecklist.isEmpty()) {
                findings.add(finding("DRIFT_REVIEW_CHECKLIST_MISSING", "image_result_boundary", "CRITICAL", "$.externalResult.imageResults[$index].driftReviewChecklist", "Image result intake requires drift review checklist evidence.", suggestedFix = "Record basic drift checks before review preflight."))
            }
            if (item.residualRisks.isEmpty()) {
                findings.add(finding("RESIDUAL_RISKS_MISSING", "image_result_boundary", "CRITICAL", "$.externalResult.imageResults[$index].residualRisks", "Image result intake requires residual risks to remain visible.", suggestedFix = "Carry residual risks into human review."))
            }
            validateStatus("$.externalResult.imageResults[$index].candidateStatus", item.candidateStatus, "IMAGE_RESULT_STATUS_NOT_ALLOWED", findings)
            if (item.sourceFileSha256.isNotBlank() && !sha256Regex.matches(item.sourceFileSha256.trim().lowercase())) {
                findings.add(finding("IMAGE_RESULT_SOURCE_FILE_HASH_INVALID", "image_result_boundary", "CRITICAL", "$.externalResult.imageResults[$index].sourceFileSha256", "Image result source file hash must be lowercase SHA-256 when provided.", evidence = listOf(item.sourceFileSha256), suggestedFix = "Use a valid SHA-256 or leave sourceFileSha256 blank."))
            }
        }
        return linked
    }

    private fun validateForbiddenRemoteOrProviderFields(result: ImageCandidateExternalResult, findings: MutableList<ImageCandidateExternalResultIntakeFinding>): Int {
        var count = 0
        listOf(
            Triple(result.providerJobId, "$.externalResult.providerJobId", "PROVIDER_JOB_ID_FORBIDDEN"),
            Triple(result.apiResponse, "$.externalResult.apiResponse", "API_RESPONSE_FORBIDDEN"),
            Triple(result.remoteUrl, "$.externalResult.remoteUrl", "REMOTE_URL_FORBIDDEN"),
        ).forEach { (value, path, ruleId) ->
            if (value.isNotBlank()) {
                count += 1
                findings.add(finding(ruleId, "safety_boundary", "CRITICAL", path, "External result intake cannot carry provider job id, API response, or remote URL.", evidence = listOf(value), suggestedFix = "Record only human-provided local candidate metadata."))
            }
        }
        result.imageResults.forEachIndexed { index, item ->
            listOf(
                Triple(item.providerJobId, "$.externalResult.imageResults[$index].providerJobId", "PROVIDER_JOB_ID_FORBIDDEN"),
                Triple(item.apiResponse, "$.externalResult.imageResults[$index].apiResponse", "API_RESPONSE_FORBIDDEN"),
                Triple(item.remoteUrl, "$.externalResult.imageResults[$index].remoteUrl", "REMOTE_URL_FORBIDDEN"),
                Triple(item.generatedStatus, "$.externalResult.imageResults[$index].generatedStatus", "GENERATED_STATUS_FORBIDDEN"),
            ).forEach { (value, path, ruleId) ->
                if (value.isNotBlank()) {
                    count += 1
                    findings.add(finding(ruleId, "safety_boundary", "CRITICAL", path, "External result intake cannot carry provider job, API response, remote URL, or generated status.", evidence = listOf(value), suggestedFix = "Keep only local candidate review metadata."))
                }
            }
            if (item.localImagePath.isNotBlank() && urlRegex.containsMatchIn(item.localImagePath.trim())) {
                count += 1
                findings.add(finding("REMOTE_IMAGE_PATH_FORBIDDEN", "safety_boundary", "CRITICAL", "$.externalResult.imageResults[$index].localImagePath", "External result intake only allows local/controlled image path references, not remote URLs.", evidence = listOf(item.localImagePath), suggestedFix = "Use a local path or metadata-only reference; do not store remote provider URLs."))
            }
            if (item.metadataOnlyReference.isNotBlank() &&
                (embeddedUrlRegex.containsMatchIn(item.metadataOnlyReference) || providerReferenceRegex.containsMatchIn(item.metadataOnlyReference))
            ) {
                count += 1
                findings.add(finding("REMOTE_METADATA_REFERENCE_FORBIDDEN", "safety_boundary", "CRITICAL", "$.externalResult.imageResults[$index].metadataOnlyReference", "External result intake cannot use metadataOnlyReference to carry remote URLs, provider job references, or API response references.", evidence = listOf(item.metadataOnlyReference), suggestedFix = "Use local candidate metadata that does not point to remote provider state."))
            }
        }
        return count
    }

    private fun scanText(
        result: ImageCandidateExternalResult,
        patterns: List<Regex>,
        ruleId: String,
        category: String,
        message: String,
        findings: MutableList<ImageCandidateExternalResultIntakeFinding>,
    ): Int {
        var count = 0
        collectTextFields(result).forEach { field ->
            patterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings.add(finding(ruleId, category, "CRITICAL", field.path, message, evidence = listOf(field.value), suggestedFix = "Keep external results candidate-only and unapproved."))
                }
            }
        }
        return count
    }

    private fun collectTextFields(result: ImageCandidateExternalResult): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) {
            if (value.isNotBlank()) fields += TextField(path, value)
        }
        add("$.externalResult.resultSource", result.resultSource)
        add("$.externalResult.recorder", result.recorder)
        add("$.externalResult.recorderRole", result.recorderRole)
        add("$.externalResult.resultStatus", result.resultStatus)
        add("$.externalResult.notes", result.notes)
        result.imageResults.forEachIndexed { index, item ->
            add("$.externalResult.imageResults[$index].assetType", item.assetType)
            add("$.externalResult.imageResults[$index].candidateStatus", item.candidateStatus)
            add("$.externalResult.imageResults[$index].localImagePath", item.localImagePath)
            add("$.externalResult.imageResults[$index].metadataOnlyReference", item.metadataOnlyReference)
            add("$.externalResult.imageResults[$index].notes", item.notes)
            item.doNotShowChecklist.forEachIndexed { itemIndex, value -> add("$.externalResult.imageResults[$index].doNotShowChecklist[$itemIndex]", value) }
            item.driftReviewChecklist.forEachIndexed { itemIndex, value -> add("$.externalResult.imageResults[$index].driftReviewChecklist[$itemIndex]", value) }
            item.residualRisks.forEachIndexed { itemIndex, value -> add("$.externalResult.imageResults[$index].residualRisks[$itemIndex]", value) }
        }
        return fields
    }

    private fun normalizePath(value: String): String = value.trim().replace('\\', '/')

    private fun finding(ruleId: String, category: String, severity: String, path: String, message: String, evidence: List<String> = emptyList(), suggestedFix: String = ""): ImageCandidateExternalResultIntakeFinding =
        ImageCandidateExternalResultIntakeFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private data class TextField(val path: String, val value: String)
}
