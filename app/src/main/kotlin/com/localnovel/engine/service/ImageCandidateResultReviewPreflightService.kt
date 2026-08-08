package com.localnovel.engine.service

import com.localnovel.engine.model.ImageCandidateExternalResultIntakeReport
import com.localnovel.engine.model.ImageCandidateExternalResultItem
import com.localnovel.engine.model.ImageCandidateResultReviewPreflightEvidence
import com.localnovel.engine.model.ImageCandidateResultReviewPreflightFinding
import com.localnovel.engine.model.ImageCandidateResultReviewPreflightReport

class ImageCandidateResultReviewPreflightService {
    private val requiredIntakeStep = "image_candidate_result_review_preflight"
    private val nextAllowedDownstreamStep = "image_candidate_result_human_review_ready"
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
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
        externalResultIntake: ImageCandidateExternalResultIntakeReport,
        preflightId: String = "",
        externalResultIntakeFile: String = "",
    ): ImageCandidateResultReviewPreflightReport {
        val findings = mutableListOf<ImageCandidateResultReviewPreflightFinding>()
        validateIntake(externalResultIntake, findings)
        val sourceCount = validateSourceFields(externalResultIntake, externalResultIntakeFile, findings)
        validateImageResults(externalResultIntake.imageResults, findings)
        val forbiddenRemoteOrProviderCount = validateForbiddenRemoteOrProviderFields(externalResultIntake.imageResults, findings)
        val forbiddenStatusCount = scanText(externalResultIntake.imageResults, forbiddenStatusPatterns, "IMAGE_RESULT_STATUS_FORBIDDEN", "image_result_boundary", "Result review preflight cannot contain approved, locked, final, review-ready, production-ready, approved-reference, or production-locked wording.", findings)
        val forbiddenClaimCount = scanText(externalResultIntake.imageResults, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", "Result review preflight cannot claim generated media, asset approval, production lock, canon application, or runtime scene creation.", findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") {
            findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId }
        } else {
            ""
        }
        val resolvedPreflightId = preflightId.ifBlank {
            externalResultIntake.resultId
                .takeIf { it.isNotBlank() }
                ?.let { "$it-result-review-preflight" }
                .orEmpty()
        }

        return ImageCandidateResultReviewPreflightReport(
            verdict = verdict,
            preflightId = resolvedPreflightId,
            externalResultIntakeFile = externalResultIntakeFile,
            resultId = externalResultIntake.resultId,
            providerHandoffPacketValidationReportFile = externalResultIntake.providerHandoffPacketValidationReportFile,
            packetId = externalResultIntake.packetId,
            preflightSourceId = externalResultIntake.preflightId,
            decisionId = externalResultIntake.decisionId,
            acceptedScriptPacketId = externalResultIntake.acceptedScriptPacketId,
            scriptPacketDraftFile = externalResultIntake.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = externalResultIntake.scriptPacketDraftContentSha256,
            imageResults = if (verdict == "FAIL") emptyList() else externalResultIntake.imageResults,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = ImageCandidateResultReviewPreflightEvidence(
                externalResultIntakeVerdict = externalResultIntake.verdict,
                externalResultIntakeNextAllowedDownstreamStep = externalResultIntake.nextAllowedDownstreamStep,
                sourceFieldCount = sourceCount,
                imageResultCount = externalResultIntake.imageResults.size,
                localImageReferenceCount = externalResultIntake.imageResults.count { it.localImagePath.isNotBlank() },
                metadataOnlyReferenceCount = externalResultIntake.imageResults.count { it.metadataOnlyReference.isNotBlank() },
                forbiddenRemoteOrProviderFieldCount = forbiddenRemoteOrProviderCount,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
            ),
        )
    }

    private fun validateIntake(
        report: ImageCandidateExternalResultIntakeReport,
        findings: MutableList<ImageCandidateResultReviewPreflightFinding>,
    ) {
        if (report.verdict.trim().uppercase() != "PASS") {
            findings.add(finding("EXTERNAL_RESULT_INTAKE_NOT_PASS", "upstream", "CRITICAL", "$.externalResultIntake.verdict", "Result review preflight requires a PASS external result intake report.", evidence = listOf(report.verdict), suggestedFix = "Use the PASS external result intake report."))
        }
        if (report.nextAllowedDownstreamStep != requiredIntakeStep) {
            findings.add(finding("EXTERNAL_RESULT_INTAKE_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.externalResultIntake.nextAllowedDownstreamStep", "Result review preflight requires nextAllowedDownstreamStep=image_candidate_result_review_preflight.", evidence = listOf(report.nextAllowedDownstreamStep), suggestedFix = "Use the intake report that explicitly allows result review preflight."))
        }
    }

    private fun validateSourceFields(
        report: ImageCandidateExternalResultIntakeReport,
        reportFile: String,
        findings: MutableList<ImageCandidateResultReviewPreflightFinding>,
    ): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) {
                findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Result review preflight requires nonblank $label.", suggestedFix = "Use a complete external result intake report."))
            } else {
                count += 1
            }
        }
        requireNonBlank(reportFile, "$.inputs.externalResultIntakeFile", "EXTERNAL_RESULT_INTAKE_FILE_MISSING", "external result intake file")
        requireNonBlank(report.resultId, "$.externalResultIntake.resultId", "EXTERNAL_RESULT_ID_MISSING", "external result id")
        requireNonBlank(report.providerHandoffPacketValidationReportFile, "$.externalResultIntake.providerHandoffPacketValidationReportFile", "PROVIDER_HANDOFF_PACKET_VALIDATION_REPORT_FILE_MISSING", "provider handoff packet validation report file")
        requireNonBlank(report.packetId, "$.externalResultIntake.packetId", "PACKET_ID_MISSING", "packet id")
        requireNonBlank(report.preflightId, "$.externalResultIntake.preflightId", "PREFLIGHT_ID_MISSING", "preflight id")
        requireNonBlank(report.decisionId, "$.externalResultIntake.decisionId", "DECISION_ID_MISSING", "decision id")
        requireNonBlank(report.acceptedScriptPacketId, "$.externalResultIntake.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(report.scriptPacketDraftFile, "$.externalResultIntake.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(report.scriptPacketDraftContentSha256, "$.externalResultIntake.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        if (report.scriptPacketDraftContentSha256.isNotBlank() && !sha256Regex.matches(report.scriptPacketDraftContentSha256.trim().lowercase())) {
            findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.externalResultIntake.scriptPacketDraftContentSha256", "Result review preflight requires a valid lowercase SHA-256 script packet draft hash.", evidence = listOf(report.scriptPacketDraftContentSha256), suggestedFix = "Use the source hash carried by the intake report."))
        }
        return count
    }

    private fun validateImageResults(
        results: List<ImageCandidateExternalResultItem>,
        findings: MutableList<ImageCandidateResultReviewPreflightFinding>,
    ) {
        if (results.isEmpty()) {
            findings.add(finding("IMAGE_RESULTS_MISSING", "image_result_boundary", "CRITICAL", "$.externalResultIntake.imageResults", "Result review preflight requires candidate image result entries.", suggestedFix = "Use a PASS intake report carrying candidate results."))
            return
        }
        results.forEachIndexed { index, item ->
            if (item.resultItemId.isBlank() || item.promptItemId.isBlank() || item.sourceCandidateId.isBlank() || item.assetType.isBlank()) {
                findings.add(finding("IMAGE_RESULT_ITEM_INCOMPLETE", "image_result_boundary", "CRITICAL", "$.externalResultIntake.imageResults[$index]", "Result review preflight requires result id, prompt id, candidate id, and asset type.", suggestedFix = "Use complete candidate result entries."))
            }
            if (item.localImagePath.isBlank() && item.metadataOnlyReference.isBlank()) {
                findings.add(finding("IMAGE_RESULT_REFERENCE_MISSING", "image_result_boundary", "CRITICAL", "$.externalResultIntake.imageResults[$index]", "Result review preflight requires local image path or metadata-only reference.", suggestedFix = "Keep the human-provided candidate reference visible for review."))
            }
            if (item.doNotShowChecklist.isEmpty()) {
                findings.add(finding("DO_NOT_SHOW_CHECKLIST_MISSING", "image_result_boundary", "CRITICAL", "$.externalResultIntake.imageResults[$index].doNotShowChecklist", "Result review preflight requires do-not-show checklist evidence.", suggestedFix = "Carry do-not-show checks into human review."))
            }
            if (item.driftReviewChecklist.isEmpty()) {
                findings.add(finding("DRIFT_REVIEW_CHECKLIST_MISSING", "image_result_boundary", "CRITICAL", "$.externalResultIntake.imageResults[$index].driftReviewChecklist", "Result review preflight requires drift review checklist evidence.", suggestedFix = "Carry drift checks into human review."))
            }
            if (item.residualRisks.isEmpty()) {
                findings.add(finding("RESIDUAL_RISKS_MISSING", "image_result_boundary", "CRITICAL", "$.externalResultIntake.imageResults[$index].residualRisks", "Result review preflight requires residual risks.", suggestedFix = "Do not hide result risks before human review."))
            }
            val status = item.candidateStatus.trim().lowercase()
            if (status.isBlank() || status !in allowedStatuses) {
                findings.add(finding("IMAGE_RESULT_STATUS_NOT_ALLOWED", "image_result_boundary", "CRITICAL", "$.externalResultIntake.imageResults[$index].candidateStatus", "Result review preflight status must remain candidate-only.", evidence = listOf(item.candidateStatus), suggestedFix = "Use external_result_candidate or needs_review."))
            }
        }
    }

    private fun validateForbiddenRemoteOrProviderFields(
        results: List<ImageCandidateExternalResultItem>,
        findings: MutableList<ImageCandidateResultReviewPreflightFinding>,
    ): Int {
        var count = 0
        results.forEachIndexed { index, item ->
            listOf(
                Triple(item.providerJobId, "$.externalResultIntake.imageResults[$index].providerJobId", "PROVIDER_JOB_ID_FORBIDDEN"),
                Triple(item.apiResponse, "$.externalResultIntake.imageResults[$index].apiResponse", "API_RESPONSE_FORBIDDEN"),
                Triple(item.remoteUrl, "$.externalResultIntake.imageResults[$index].remoteUrl", "REMOTE_URL_FORBIDDEN"),
                Triple(item.generatedStatus, "$.externalResultIntake.imageResults[$index].generatedStatus", "GENERATED_STATUS_FORBIDDEN"),
            ).forEach { (value, path, ruleId) ->
                if (value.isNotBlank()) {
                    count += 1
                    findings.add(finding(ruleId, "safety_boundary", "CRITICAL", path, "Result review preflight cannot carry provider job, API response, remote URL, or generated status.", evidence = listOf(value), suggestedFix = "Keep only local candidate review metadata."))
                }
            }
            if (item.localImagePath.isNotBlank() && urlRegex.containsMatchIn(item.localImagePath.trim())) {
                count += 1
                findings.add(finding("REMOTE_IMAGE_PATH_FORBIDDEN", "safety_boundary", "CRITICAL", "$.externalResultIntake.imageResults[$index].localImagePath", "Result review preflight only allows local/controlled image path references, not remote URLs.", evidence = listOf(item.localImagePath), suggestedFix = "Use a local path or metadata-only reference."))
            }
            if (item.metadataOnlyReference.isNotBlank() &&
                (embeddedUrlRegex.containsMatchIn(item.metadataOnlyReference) || providerReferenceRegex.containsMatchIn(item.metadataOnlyReference))
            ) {
                count += 1
                findings.add(finding("REMOTE_METADATA_REFERENCE_FORBIDDEN", "safety_boundary", "CRITICAL", "$.externalResultIntake.imageResults[$index].metadataOnlyReference", "Result review preflight cannot use metadataOnlyReference to carry remote URLs, provider job references, or API response references.", evidence = listOf(item.metadataOnlyReference), suggestedFix = "Use local candidate metadata that does not point to remote provider state."))
            }
        }
        return count
    }

    private fun scanText(
        results: List<ImageCandidateExternalResultItem>,
        patterns: List<Regex>,
        ruleId: String,
        category: String,
        message: String,
        findings: MutableList<ImageCandidateResultReviewPreflightFinding>,
    ): Int {
        var count = 0
        collectTextFields(results).forEach { field ->
            patterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings.add(finding(ruleId, category, "CRITICAL", field.path, message, evidence = listOf(field.value), suggestedFix = "Keep result review candidate-only and unapproved."))
                }
            }
        }
        return count
    }

    private fun collectTextFields(results: List<ImageCandidateExternalResultItem>): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) {
            if (value.isNotBlank()) fields += TextField(path, value)
        }
        results.forEachIndexed { index, item ->
            add("$.externalResultIntake.imageResults[$index].assetType", item.assetType)
            add("$.externalResultIntake.imageResults[$index].candidateStatus", item.candidateStatus)
            add("$.externalResultIntake.imageResults[$index].localImagePath", item.localImagePath)
            add("$.externalResultIntake.imageResults[$index].metadataOnlyReference", item.metadataOnlyReference)
            add("$.externalResultIntake.imageResults[$index].notes", item.notes)
            item.doNotShowChecklist.forEachIndexed { itemIndex, value -> add("$.externalResultIntake.imageResults[$index].doNotShowChecklist[$itemIndex]", value) }
            item.driftReviewChecklist.forEachIndexed { itemIndex, value -> add("$.externalResultIntake.imageResults[$index].driftReviewChecklist[$itemIndex]", value) }
            item.residualRisks.forEachIndexed { itemIndex, value -> add("$.externalResultIntake.imageResults[$index].residualRisks[$itemIndex]", value) }
        }
        return fields
    }

    private fun finding(ruleId: String, category: String, severity: String, path: String, message: String, evidence: List<String> = emptyList(), suggestedFix: String = ""): ImageCandidateResultReviewPreflightFinding =
        ImageCandidateResultReviewPreflightFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private data class TextField(val path: String, val value: String)
}
