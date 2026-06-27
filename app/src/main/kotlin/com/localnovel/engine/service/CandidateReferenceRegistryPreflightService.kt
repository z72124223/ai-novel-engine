package com.localnovel.engine.service

import com.localnovel.engine.model.CandidateReferenceHumanAcceptanceDecisionIntakeReport
import com.localnovel.engine.model.CandidateReferenceRegistryPreflightEvidence
import com.localnovel.engine.model.CandidateReferenceRegistryPreflightFinding
import com.localnovel.engine.model.CandidateReferenceRegistryPreflightReport
import com.localnovel.engine.model.ImageCandidateExternalResultItem
import com.localnovel.engine.model.ImageCandidateReferencePacketValidationReport

class CandidateReferenceRegistryPreflightService {
    private val requiredDecisionStatus = "ALLOW_CANDIDATE_REFERENCE_REGISTRY_PREFLIGHT"
    private val requiredDecisionNextStep = "candidate_reference_registry_preflight"
    private val requiredValidationNextStep = "candidate_reference_human_acceptance_ready"
    private val nextAllowedDownstreamStep = "candidate_reference_registry_packet"
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
        Regex("""(?i)\bproduction[-_\s]?locked\b"""),
        Regex("\u6838\u51c6|\u6279\u51c6|\u9396\u5b9a"),
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
        humanAcceptanceDecisionIntake: CandidateReferenceHumanAcceptanceDecisionIntakeReport,
        candidateReferencePacketValidation: ImageCandidateReferencePacketValidationReport,
        preflightId: String = "",
        humanAcceptanceDecisionIntakeFile: String = "",
        candidateReferencePacketValidationReportFile: String = "",
    ): CandidateReferenceRegistryPreflightReport {
        val findings = mutableListOf<CandidateReferenceRegistryPreflightFinding>()
        validateUpstreams(humanAcceptanceDecisionIntake, candidateReferencePacketValidation, findings)
        val sourceCount = validateSourceBinding(humanAcceptanceDecisionIntake, candidateReferencePacketValidation, humanAcceptanceDecisionIntakeFile, candidateReferencePacketValidationReportFile, findings) +
            validateImageResultSourceBinding(candidateReferencePacketValidation.imageResults, humanAcceptanceDecisionIntake.imageResults, findings)
        val forbiddenRemoteOrProviderCount = validateForbiddenRemoteOrProviderFields(humanAcceptanceDecisionIntake.imageResults + candidateReferencePacketValidation.imageResults, findings)
        val forbiddenStatusCount = scanText(humanAcceptanceDecisionIntake.imageResults + candidateReferencePacketValidation.imageResults, forbiddenStatusPatterns, "CANDIDATE_REFERENCE_STATUS_FORBIDDEN", "candidate_reference_boundary", findings)
        val forbiddenClaimCount = scanText(humanAcceptanceDecisionIntake.imageResults + candidateReferencePacketValidation.imageResults, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId } else ""
        val resolvedPreflightId = preflightId.ifBlank { humanAcceptanceDecisionIntake.decisionId.takeIf { it.isNotBlank() }?.let { "$it-registry-preflight" }.orEmpty() }

        return CandidateReferenceRegistryPreflightReport(
            verdict = verdict,
            preflightId = resolvedPreflightId,
            humanAcceptanceDecisionIntakeFile = humanAcceptanceDecisionIntakeFile,
            candidateReferencePacketValidationReportFile = candidateReferencePacketValidationReportFile,
            decisionId = humanAcceptanceDecisionIntake.decisionId,
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
            imageResults = if (verdict == "FAIL") emptyList() else candidateReferencePacketValidation.imageResults,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = CandidateReferenceRegistryPreflightEvidence(
                humanAcceptanceDecisionIntakeVerdict = humanAcceptanceDecisionIntake.verdict,
                humanAcceptanceDecisionIntakeNextAllowedDownstreamStep = humanAcceptanceDecisionIntake.nextAllowedDownstreamStep,
                candidateReferencePacketValidationVerdict = candidateReferencePacketValidation.verdict,
                candidateReferencePacketValidationNextAllowedDownstreamStep = candidateReferencePacketValidation.nextAllowedDownstreamStep,
                sourceBindingFieldCount = sourceCount,
                imageResultCount = candidateReferencePacketValidation.imageResults.size,
                forbiddenRemoteOrProviderFieldCount = forbiddenRemoteOrProviderCount,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
            ),
        )
    }

    private fun validateUpstreams(intake: CandidateReferenceHumanAcceptanceDecisionIntakeReport, validation: ImageCandidateReferencePacketValidationReport, findings: MutableList<CandidateReferenceRegistryPreflightFinding>) {
        if (intake.verdict.trim().uppercase() != "PASS") findings.add(finding("HUMAN_ACCEPTANCE_DECISION_INTAKE_NOT_PASS", "upstream", "CRITICAL", "$.humanAcceptanceDecisionIntake.verdict", "Registry preflight requires a PASS human acceptance decision intake.", evidence = listOf(intake.verdict)))
        if (!intake.decisionAllowedDownstream) findings.add(finding("DECISION_NOT_DOWNSTREAM_ALLOWED", "upstream", "CRITICAL", "$.humanAcceptanceDecisionIntake.decisionAllowedDownstream", "Registry preflight requires decisionAllowedDownstream=true.", evidence = listOf(intake.decisionAllowedDownstream.toString())))
        if (intake.decisionStatus != requiredDecisionStatus) findings.add(finding("DECISION_STATUS_MISMATCH", "upstream", "CRITICAL", "$.humanAcceptanceDecisionIntake.decisionStatus", "Registry preflight requires decisionStatus=ALLOW_CANDIDATE_REFERENCE_REGISTRY_PREFLIGHT.", evidence = listOf(intake.decisionStatus)))
        if (intake.nextAllowedDownstreamStep != requiredDecisionNextStep) findings.add(finding("DECISION_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.humanAcceptanceDecisionIntake.nextAllowedDownstreamStep", "Registry preflight requires nextAllowedDownstreamStep=candidate_reference_registry_preflight.", evidence = listOf(intake.nextAllowedDownstreamStep)))
        if (validation.verdict.trim().uppercase() != "PASS") findings.add(finding("CANDIDATE_REFERENCE_PACKET_VALIDATION_NOT_PASS", "upstream", "CRITICAL", "$.candidateReferencePacketValidation.verdict", "Registry preflight requires a direct PASS candidate reference packet validation report.", evidence = listOf(validation.verdict)))
        if (validation.nextAllowedDownstreamStep != requiredValidationNextStep) findings.add(finding("CANDIDATE_REFERENCE_PACKET_VALIDATION_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.candidateReferencePacketValidation.nextAllowedDownstreamStep", "Registry preflight requires direct validation nextAllowedDownstreamStep=candidate_reference_human_acceptance_ready.", evidence = listOf(validation.nextAllowedDownstreamStep)))
    }

    private fun validateSourceBinding(intake: CandidateReferenceHumanAcceptanceDecisionIntakeReport, validation: ImageCandidateReferencePacketValidationReport, intakeFile: String, validationFile: String, findings: MutableList<CandidateReferenceRegistryPreflightFinding>): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) { if (value.isBlank()) findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Registry preflight requires nonblank $label.")) else count += 1 }
        fun requireMatch(leftValue: String, rightValue: String, leftPath: String, rightPath: String, missingRuleId: String, mismatchRuleId: String, label: String, normalize: (String) -> String = { it.trim() }) {
            requireNonBlank(leftValue, leftPath, "${missingRuleId}_INTAKE", "$label in decision intake")
            requireNonBlank(rightValue, rightPath, missingRuleId, "$label in validation report")
            val left = normalize(leftValue); val right = normalize(rightValue)
            if (left.isNotBlank() && right.isNotBlank() && left != right) findings.add(finding(mismatchRuleId, "source_linkage", "CRITICAL", rightPath, "Registry preflight source binding mismatch for $label.", evidence = listOf("intake=$leftValue", "validation=$rightValue")))
        }
        requireNonBlank(intakeFile, "$.inputs.humanAcceptanceDecisionIntakeFile", "HUMAN_ACCEPTANCE_DECISION_INTAKE_FILE_MISSING", "human acceptance decision intake file")
        requireNonBlank(validationFile, "$.inputs.candidateReferencePacketValidationReportFile", "CANDIDATE_REFERENCE_PACKET_VALIDATION_REPORT_FILE_MISSING", "candidate reference packet validation report file")
        requireMatch(intake.candidateReferencePacketValidationReportFile, validationFile, "$.humanAcceptanceDecisionIntake.candidateReferencePacketValidationReportFile", "$.inputs.candidateReferencePacketValidationReportFile", "VALIDATION_REPORT_FILE_MISSING", "VALIDATION_REPORT_FILE_MISMATCH", "validation report file", ::normalizePath)
        requireMatch(intake.referencePacketFile, validation.referencePacketFile, "$.humanAcceptanceDecisionIntake.referencePacketFile", "$.candidateReferencePacketValidation.referencePacketFile", "REFERENCE_PACKET_FILE_MISSING", "REFERENCE_PACKET_FILE_MISMATCH", "reference packet file", ::normalizePath)
        requireMatch(intake.packetId, validation.packetId, "$.humanAcceptanceDecisionIntake.packetId", "$.candidateReferencePacketValidation.packetId", "PACKET_ID_MISSING", "PACKET_ID_MISMATCH", "reference packet id")
        requireMatch(intake.resultReviewPreflightId, validation.resultReviewPreflightId, "$.humanAcceptanceDecisionIntake.resultReviewPreflightId", "$.candidateReferencePacketValidation.resultReviewPreflightId", "RESULT_REVIEW_PREFLIGHT_ID_MISSING", "RESULT_REVIEW_PREFLIGHT_ID_MISMATCH", "result review preflight id")
        requireMatch(intake.resultReviewDecisionId, validation.decisionId, "$.humanAcceptanceDecisionIntake.resultReviewDecisionId", "$.candidateReferencePacketValidation.decisionId", "RESULT_REVIEW_DECISION_ID_MISSING", "RESULT_REVIEW_DECISION_ID_MISMATCH", "result review decision id")
        requireMatch(intake.resultId, validation.resultId, "$.humanAcceptanceDecisionIntake.resultId", "$.candidateReferencePacketValidation.resultId", "RESULT_ID_MISSING", "RESULT_ID_MISMATCH", "result id")
        requireMatch(intake.acceptedScriptPacketId, validation.acceptedScriptPacketId, "$.humanAcceptanceDecisionIntake.acceptedScriptPacketId", "$.candidateReferencePacketValidation.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "ACCEPTED_SCRIPT_PACKET_ID_MISMATCH", "accepted script packet id")
        requireMatch(intake.scriptPacketDraftFile, validation.scriptPacketDraftFile, "$.humanAcceptanceDecisionIntake.scriptPacketDraftFile", "$.candidateReferencePacketValidation.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "SCRIPT_PACKET_DRAFT_FILE_MISMATCH", "script packet draft file", ::normalizePath)
        requireMatch(intake.scriptPacketDraftContentSha256, validation.scriptPacketDraftContentSha256, "$.humanAcceptanceDecisionIntake.scriptPacketDraftContentSha256", "$.candidateReferencePacketValidation.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "SCRIPT_PACKET_DRAFT_HASH_MISMATCH", "script packet draft SHA-256", { it.trim().lowercase() })
        if (intake.scriptPacketDraftContentSha256.isNotBlank() && !sha256Regex.matches(intake.scriptPacketDraftContentSha256.trim().lowercase())) findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.humanAcceptanceDecisionIntake.scriptPacketDraftContentSha256", "Registry preflight requires a valid lowercase SHA-256 script packet draft hash.", evidence = listOf(intake.scriptPacketDraftContentSha256)))
        return count
    }

    private fun validateImageResultSourceBinding(validationResults: List<ImageCandidateExternalResultItem>, intakeResults: List<ImageCandidateExternalResultItem>, findings: MutableList<CandidateReferenceRegistryPreflightFinding>): Int {
        var count = 0
        val validationIds = validationResults.map { it.resultItemId.trim() }
        val intakeIds = intakeResults.map { it.resultItemId.trim() }
        if (validationIds.toSet() != intakeIds.toSet()) findings.add(finding("IMAGE_RESULT_SET_MISMATCH", "source_linkage", "CRITICAL", "$.humanAcceptanceDecisionIntake.imageResults", "Registry preflight image result id set must match the direct validation report.", evidence = listOf("intake=${intakeIds.joinToString(",")}", "validation=${validationIds.joinToString(",")}")))
        val validationById = validationResults.associateBy { it.resultItemId.trim() }
        intakeResults.forEachIndexed { index, item ->
            val source = validationById[item.resultItemId.trim()]
            if (item.resultItemId.isBlank() || source == null) {
                findings.add(finding("IMAGE_RESULT_NOT_IN_VALIDATION_REPORT", "source_linkage", "CRITICAL", "$.humanAcceptanceDecisionIntake.imageResults[$index].resultItemId", "Registry preflight image result must exist in the direct validation report.", evidence = listOf(item.resultItemId)))
                return@forEachIndexed
            }
            count += 1
            compareField(source.promptItemId, item.promptItemId, "$.humanAcceptanceDecisionIntake.imageResults[$index].promptItemId", "promptItemId", findings)
            compareField(source.sourceCandidateId, item.sourceCandidateId, "$.humanAcceptanceDecisionIntake.imageResults[$index].sourceCandidateId", "sourceCandidateId", findings)
            compareField(source.localImagePath, item.localImagePath, "$.humanAcceptanceDecisionIntake.imageResults[$index].localImagePath", "localImagePath", findings, ::normalizePath)
            compareField(source.metadataOnlyReference, item.metadataOnlyReference, "$.humanAcceptanceDecisionIntake.imageResults[$index].metadataOnlyReference", "metadataOnlyReference", findings)
        }
        return count
    }

    private fun validateForbiddenRemoteOrProviderFields(results: List<ImageCandidateExternalResultItem>, findings: MutableList<CandidateReferenceRegistryPreflightFinding>): Int {
        var count = 0
        results.forEachIndexed { index, item ->
            listOf(Triple(item.providerJobId, "providerJobId", "PROVIDER_JOB_ID_FORBIDDEN"), Triple(item.apiResponse, "apiResponse", "API_RESPONSE_FORBIDDEN"), Triple(item.remoteUrl, "remoteUrl", "REMOTE_URL_FORBIDDEN"), Triple(item.generatedStatus, "generatedStatus", "GENERATED_STATUS_FORBIDDEN")).forEach { (value, field, ruleId) ->
                if (value.isNotBlank()) { count += 1; findings.add(finding(ruleId, "safety_boundary", "CRITICAL", "$.imageResults[$index].$field", "Registry preflight cannot carry provider job, API response, remote URL, or generated status.", evidence = listOf(value))) }
            }
            if (item.localImagePath.isNotBlank() && urlRegex.containsMatchIn(item.localImagePath.trim())) { count += 1; findings.add(finding("REMOTE_IMAGE_PATH_FORBIDDEN", "safety_boundary", "CRITICAL", "$.imageResults[$index].localImagePath", "Registry preflight only allows local image paths.", evidence = listOf(item.localImagePath))) }
            if (item.metadataOnlyReference.isNotBlank() && (embeddedUrlRegex.containsMatchIn(item.metadataOnlyReference) || providerReferenceRegex.containsMatchIn(item.metadataOnlyReference))) { count += 1; findings.add(finding("REMOTE_METADATA_REFERENCE_FORBIDDEN", "safety_boundary", "CRITICAL", "$.imageResults[$index].metadataOnlyReference", "Registry preflight cannot carry remote URLs, provider job references, or API response references.", evidence = listOf(item.metadataOnlyReference))) }
        }
        return count
    }

    private fun scanText(results: List<ImageCandidateExternalResultItem>, patterns: List<Regex>, ruleId: String, category: String, findings: MutableList<CandidateReferenceRegistryPreflightFinding>): Int {
        var count = 0
        results.flatMapIndexed { index, item ->
            listOf(
                TextField("$.imageResults[$index].assetType", item.assetType),
                TextField("$.imageResults[$index].candidateStatus", item.candidateStatus),
                TextField("$.imageResults[$index].localImagePath", item.localImagePath),
                TextField("$.imageResults[$index].metadataOnlyReference", item.metadataOnlyReference),
                TextField("$.imageResults[$index].notes", item.notes),
            ) + item.residualRisks.mapIndexed { itemIndex, value -> TextField("$.imageResults[$index].residualRisks[$itemIndex]", value) }
        }.filter { it.value.isNotBlank() }.forEach { field -> patterns.forEach { pattern -> if (pattern.containsMatchIn(field.value)) { count += 1; findings.add(finding(ruleId, category, "CRITICAL", field.path, "Registry preflight cannot carry approval, lock, final, generated, canon, or runtime claims.", evidence = listOf(field.value))) } } }
        return count
    }

    private fun compareField(sourceValue: String, intakeValue: String, path: String, label: String, findings: MutableList<CandidateReferenceRegistryPreflightFinding>, normalize: (String) -> String = { it.trim() }) {
        if (normalize(sourceValue) != normalize(intakeValue)) findings.add(finding("IMAGE_RESULT_SOURCE_MISMATCH", "source_linkage", "CRITICAL", path, "Registry preflight image result $label must match the direct validation report.", evidence = listOf("validation=$sourceValue", "intake=$intakeValue")))
    }

    private fun normalizePath(value: String): String = value.trim().replace('\\', '/')
    private fun finding(ruleId: String, category: String, severity: String, path: String, message: String, evidence: List<String> = emptyList(), suggestedFix: String = ""): CandidateReferenceRegistryPreflightFinding =
        CandidateReferenceRegistryPreflightFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
    private data class TextField(val path: String, val value: String)
}
