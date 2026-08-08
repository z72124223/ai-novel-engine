package com.localnovel.engine.service

import com.localnovel.engine.model.CandidateReferenceAcceptancePacket
import com.localnovel.engine.model.CandidateReferenceAcceptancePacketEvidence
import com.localnovel.engine.model.CandidateReferenceAcceptancePacketFinding
import com.localnovel.engine.model.CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport
import com.localnovel.engine.model.CandidateReferenceRegistryPacketValidationReport
import com.localnovel.engine.model.ImageCandidateExternalResultItem

class CandidateReferenceAcceptancePacketService {
    private val requiredDecisionStatus = "ALLOW_CANDIDATE_REFERENCE_ACCEPTANCE_PACKET"
    private val requiredDecisionNextStep = "candidate_reference_acceptance_packet"
    private val requiredValidationNextStep = "candidate_reference_registry_human_acceptance_ready"
    private val nextAllowedDownstreamStep = "candidate_reference_acceptance_packet_validation"
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
    private val urlRegex = Regex("""(?i)^[a-z][a-z0-9+.-]*://""")
    private val embeddedUrlRegex = Regex("""(?i)\b[a-z][a-z0-9+.-]*://""")
    private val providerReferenceRegex = Regex("""(?i)\bprovider[-_\s]?job\b|\bprovider[-_\s]?id\b|\bapi[-_\s]?response\b""")
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

    fun create(
        registryPacketValidation: CandidateReferenceRegistryPacketValidationReport,
        decisionIntake: CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport,
        packetId: String = "",
        registryPacketValidationReportFile: String = "",
        humanAcceptanceDecisionIntakeFile: String = "",
    ): CandidateReferenceAcceptancePacket {
        val findings = mutableListOf<CandidateReferenceAcceptancePacketFinding>()
        validateUpstreams(registryPacketValidation, decisionIntake, findings)
        val sourceCount = validateSourceBinding(registryPacketValidation, decisionIntake, registryPacketValidationReportFile, humanAcceptanceDecisionIntakeFile, findings) +
            validateImageResultSourceBinding(registryPacketValidation.imageResults, decisionIntake.imageResults, findings)
        val resolvedPacketId = packetId.ifBlank { decisionIntake.decisionId.takeIf { it.isNotBlank() }?.let { "$it-acceptance-packet" }.orEmpty() }
        validatePacketBasics(resolvedPacketId, decisionIntake.imageResults, findings)
        val forbiddenRemoteOrProviderCount = validateForbiddenRemoteOrProviderFields(decisionIntake.imageResults, findings)
        val forbiddenStatusCount = scanText("candidate_reference_acceptance_candidate", decisionIntake.imageResults, forbiddenStatusPatterns, "CANDIDATE_REFERENCE_STATUS_FORBIDDEN", "candidate_reference_boundary", findings)
        val forbiddenClaimCount = scanText("candidate_reference_acceptance_candidate", decisionIntake.imageResults, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId } else ""

        return CandidateReferenceAcceptancePacket(
            verdict = verdict,
            packetId = resolvedPacketId,
            packetStatus = "candidate_reference_acceptance_candidate",
            registryPacketValidationReportFile = registryPacketValidationReportFile,
            humanAcceptanceDecisionIntakeFile = humanAcceptanceDecisionIntakeFile,
            decisionId = decisionIntake.decisionId,
            registryPacketFile = registryPacketValidation.registryPacketFile,
            registryPacketId = registryPacketValidation.packetId,
            registryPreflightFile = registryPacketValidation.registryPreflightFile,
            registryPreflightId = registryPacketValidation.preflightId,
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
            imageResults = if (verdict == "FAIL") emptyList() else registryPacketValidation.imageResults,
            boundaryChecklist = if (verdict == "FAIL") emptyList() else listOf(
                "candidate_boundary_preserved",
                "formal_registry_write_not_performed",
                "no_status_upgrade_no_lock_no_canon",
                "requires_future_validation_before_any_registry_action",
            ),
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = CandidateReferenceAcceptancePacketEvidence(
                registryPacketValidationVerdict = registryPacketValidation.verdict,
                decisionIntakeVerdict = decisionIntake.verdict,
                decisionAllowedDownstream = decisionIntake.decisionAllowedDownstream,
                sourceFieldCount = sourceCount,
                imageResultCount = registryPacketValidation.imageResults.size,
                forbiddenRemoteOrProviderFieldCount = forbiddenRemoteOrProviderCount,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
            ),
        )
    }

    private fun validateUpstreams(validation: CandidateReferenceRegistryPacketValidationReport, intake: CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport, findings: MutableList<CandidateReferenceAcceptancePacketFinding>) {
        if (validation.verdict.trim().uppercase() != "PASS") findings.add(finding("REGISTRY_PACKET_VALIDATION_NOT_PASS", "upstream", "CRITICAL", "$.registryPacketValidation.verdict", "Acceptance packet requires a PASS registry packet validation report.", evidence = listOf(validation.verdict)))
        if (validation.nextAllowedDownstreamStep != requiredValidationNextStep) findings.add(finding("REGISTRY_PACKET_VALIDATION_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.registryPacketValidation.nextAllowedDownstreamStep", "Acceptance packet requires nextAllowedDownstreamStep=candidate_reference_registry_human_acceptance_ready.", evidence = listOf(validation.nextAllowedDownstreamStep)))
        if (intake.verdict.trim().uppercase() != "PASS") findings.add(finding("HUMAN_ACCEPTANCE_DECISION_INTAKE_NOT_PASS", "upstream", "CRITICAL", "$.decisionIntake.verdict", "Acceptance packet requires a PASS human acceptance decision intake.", evidence = listOf(intake.verdict)))
        if (!intake.decisionAllowedDownstream) findings.add(finding("DECISION_NOT_DOWNSTREAM_ALLOWED", "upstream", "CRITICAL", "$.decisionIntake.decisionAllowedDownstream", "Acceptance packet requires decisionAllowedDownstream=true.", evidence = listOf(intake.decisionAllowedDownstream.toString())))
        if (intake.decisionStatus != requiredDecisionStatus) findings.add(finding("DECISION_STATUS_MISMATCH", "upstream", "CRITICAL", "$.decisionIntake.decisionStatus", "Acceptance packet requires decisionStatus=ALLOW_CANDIDATE_REFERENCE_ACCEPTANCE_PACKET.", evidence = listOf(intake.decisionStatus)))
        if (intake.nextAllowedDownstreamStep != requiredDecisionNextStep) findings.add(finding("DECISION_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.decisionIntake.nextAllowedDownstreamStep", "Acceptance packet requires nextAllowedDownstreamStep=candidate_reference_acceptance_packet.", evidence = listOf(intake.nextAllowedDownstreamStep)))
    }

    private fun validateSourceBinding(validation: CandidateReferenceRegistryPacketValidationReport, intake: CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport, validationFile: String, intakeFile: String, findings: MutableList<CandidateReferenceAcceptancePacketFinding>): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) { if (value.isBlank()) findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Acceptance packet requires nonblank $label.")) else count += 1 }
        fun requireMatch(leftValue: String, rightValue: String, leftPath: String, rightPath: String, missingRuleId: String, mismatchRuleId: String, label: String, normalize: (String) -> String = { it.trim() }) {
            requireNonBlank(leftValue, leftPath, "${missingRuleId}_VALIDATION", "$label in validation report")
            requireNonBlank(rightValue, rightPath, missingRuleId, "$label in decision intake")
            val left = normalize(leftValue); val right = normalize(rightValue)
            if (left.isNotBlank() && right.isNotBlank() && left != right) findings.add(finding(mismatchRuleId, "source_linkage", "CRITICAL", rightPath, "Acceptance packet source binding mismatch for $label.", evidence = listOf("validation=$leftValue", "decisionIntake=$rightValue")))
        }
        requireNonBlank(validationFile, "$.inputs.registryPacketValidationReportFile", "REGISTRY_PACKET_VALIDATION_REPORT_FILE_MISSING", "registry packet validation report file")
        requireNonBlank(intakeFile, "$.inputs.humanAcceptanceDecisionIntakeFile", "HUMAN_ACCEPTANCE_DECISION_INTAKE_FILE_MISSING", "human acceptance decision intake file")
        requireMatch(validationFile, intake.registryPacketValidationReportFile, "$.inputs.registryPacketValidationReportFile", "$.decisionIntake.registryPacketValidationReportFile", "REGISTRY_PACKET_VALIDATION_REPORT_FILE_MISSING", "REGISTRY_PACKET_VALIDATION_REPORT_FILE_MISMATCH", "registry packet validation report file", ::normalizePath)
        requireMatch(validation.registryPacketFile, intake.registryPacketFile, "$.registryPacketValidation.registryPacketFile", "$.decisionIntake.registryPacketFile", "REGISTRY_PACKET_FILE_MISSING", "REGISTRY_PACKET_FILE_MISMATCH", "registry packet file", ::normalizePath)
        requireMatch(validation.packetId, intake.registryPacketId, "$.registryPacketValidation.packetId", "$.decisionIntake.registryPacketId", "REGISTRY_PACKET_ID_MISSING", "REGISTRY_PACKET_ID_MISMATCH", "registry packet id")
        requireMatch(validation.preflightId, intake.registryPreflightId, "$.registryPacketValidation.preflightId", "$.decisionIntake.registryPreflightId", "REGISTRY_PREFLIGHT_ID_MISSING", "REGISTRY_PREFLIGHT_ID_MISMATCH", "registry preflight id")
        requireMatch(validation.acceptedScriptPacketId, intake.acceptedScriptPacketId, "$.registryPacketValidation.acceptedScriptPacketId", "$.decisionIntake.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "ACCEPTED_SCRIPT_PACKET_ID_MISMATCH", "accepted script packet id")
        requireMatch(validation.scriptPacketDraftFile, intake.scriptPacketDraftFile, "$.registryPacketValidation.scriptPacketDraftFile", "$.decisionIntake.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "SCRIPT_PACKET_DRAFT_FILE_MISMATCH", "script packet draft file", ::normalizePath)
        requireMatch(validation.scriptPacketDraftContentSha256, intake.scriptPacketDraftContentSha256, "$.registryPacketValidation.scriptPacketDraftContentSha256", "$.decisionIntake.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "SCRIPT_PACKET_DRAFT_HASH_MISMATCH", "script packet draft SHA-256", { it.trim().lowercase() })
        requireMatch(validation.referencePacketFile, intake.referencePacketFile, "$.registryPacketValidation.referencePacketFile", "$.decisionIntake.referencePacketFile", "REFERENCE_PACKET_FILE_MISSING", "REFERENCE_PACKET_FILE_MISMATCH", "reference packet file", ::normalizePath)
        requireMatch(validation.referencePacketId, intake.referencePacketId, "$.registryPacketValidation.referencePacketId", "$.decisionIntake.referencePacketId", "REFERENCE_PACKET_ID_MISSING", "REFERENCE_PACKET_ID_MISMATCH", "reference packet id")
        requireMatch(validation.resultReviewPreflightId, intake.resultReviewPreflightId, "$.registryPacketValidation.resultReviewPreflightId", "$.decisionIntake.resultReviewPreflightId", "RESULT_REVIEW_PREFLIGHT_ID_MISSING", "RESULT_REVIEW_PREFLIGHT_ID_MISMATCH", "result review preflight id")
        requireMatch(validation.resultReviewDecisionIntakeFile, intake.resultReviewDecisionIntakeFile, "$.registryPacketValidation.resultReviewDecisionIntakeFile", "$.decisionIntake.resultReviewDecisionIntakeFile", "RESULT_REVIEW_DECISION_INTAKE_FILE_MISSING", "RESULT_REVIEW_DECISION_INTAKE_FILE_MISMATCH", "result review decision intake file", ::normalizePath)
        requireMatch(validation.resultReviewDecisionId, intake.resultReviewDecisionId, "$.registryPacketValidation.resultReviewDecisionId", "$.decisionIntake.resultReviewDecisionId", "RESULT_REVIEW_DECISION_ID_MISSING", "RESULT_REVIEW_DECISION_ID_MISMATCH", "result review decision id")
        requireMatch(validation.providerHandoffPacketValidationReportFile, intake.providerHandoffPacketValidationReportFile, "$.registryPacketValidation.providerHandoffPacketValidationReportFile", "$.decisionIntake.providerHandoffPacketValidationReportFile", "PROVIDER_HANDOFF_PACKET_VALIDATION_REPORT_FILE_MISSING", "PROVIDER_HANDOFF_PACKET_VALIDATION_REPORT_FILE_MISMATCH", "provider handoff packet validation report file", ::normalizePath)
        requireMatch(validation.providerHandoffPacketId, intake.providerHandoffPacketId, "$.registryPacketValidation.providerHandoffPacketId", "$.decisionIntake.providerHandoffPacketId", "PROVIDER_HANDOFF_PACKET_ID_MISSING", "PROVIDER_HANDOFF_PACKET_ID_MISMATCH", "provider handoff packet id")
        requireMatch(validation.providerHandoffPreflightId, intake.providerHandoffPreflightId, "$.registryPacketValidation.providerHandoffPreflightId", "$.decisionIntake.providerHandoffPreflightId", "PROVIDER_HANDOFF_PREFLIGHT_ID_MISSING", "PROVIDER_HANDOFF_PREFLIGHT_ID_MISMATCH", "provider handoff preflight id")
        requireMatch(validation.providerHandoffDecisionId, intake.providerHandoffDecisionId, "$.registryPacketValidation.providerHandoffDecisionId", "$.decisionIntake.providerHandoffDecisionId", "PROVIDER_HANDOFF_DECISION_ID_MISSING", "PROVIDER_HANDOFF_DECISION_ID_MISMATCH", "provider handoff decision id")
        requireMatch(validation.externalResultIntakeFile, intake.externalResultIntakeFile, "$.registryPacketValidation.externalResultIntakeFile", "$.decisionIntake.externalResultIntakeFile", "EXTERNAL_RESULT_INTAKE_FILE_MISSING", "EXTERNAL_RESULT_INTAKE_FILE_MISMATCH", "external result intake file", ::normalizePath)
        requireMatch(validation.resultId, intake.resultId, "$.registryPacketValidation.resultId", "$.decisionIntake.resultId", "RESULT_ID_MISSING", "RESULT_ID_MISMATCH", "result id")
        if (intake.scriptPacketDraftContentSha256.isNotBlank() && !sha256Regex.matches(intake.scriptPacketDraftContentSha256.trim().lowercase())) findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.decisionIntake.scriptPacketDraftContentSha256", "Acceptance packet requires a valid lowercase SHA-256 script packet draft hash.", evidence = listOf(intake.scriptPacketDraftContentSha256)))
        return count
    }

    private fun validateImageResultSourceBinding(validationResults: List<ImageCandidateExternalResultItem>, intakeResults: List<ImageCandidateExternalResultItem>, findings: MutableList<CandidateReferenceAcceptancePacketFinding>): Int {
        var count = 0
        val validationIds = validationResults.map { it.resultItemId.trim() }
        val intakeIds = intakeResults.map { it.resultItemId.trim() }
        if (validationIds.toSet() != intakeIds.toSet()) findings.add(finding("IMAGE_RESULT_SET_MISMATCH", "source_linkage", "CRITICAL", "$.decisionIntake.imageResults", "Acceptance packet image result id set must match validation report.", evidence = listOf("validation=${validationIds.joinToString(",")}", "decisionIntake=${intakeIds.joinToString(",")}")))
        val validationById = validationResults.associateBy { it.resultItemId.trim() }
        intakeResults.forEachIndexed { index, item ->
            val source = validationById[item.resultItemId.trim()]
            if (item.resultItemId.isBlank() || source == null) { findings.add(finding("IMAGE_RESULT_NOT_IN_VALIDATION_REPORT", "source_linkage", "CRITICAL", "$.decisionIntake.imageResults[$index].resultItemId", "Acceptance packet image result must exist in validation report.", evidence = listOf(item.resultItemId))); return@forEachIndexed }
            count += 1
            compareField(source.assetType, item.assetType, "$.decisionIntake.imageResults[$index].assetType", "assetType", findings)
            compareField(source.promptItemId, item.promptItemId, "$.decisionIntake.imageResults[$index].promptItemId", "promptItemId", findings)
            compareField(source.sourceCandidateId, item.sourceCandidateId, "$.decisionIntake.imageResults[$index].sourceCandidateId", "sourceCandidateId", findings)
            compareField(source.localImagePath, item.localImagePath, "$.decisionIntake.imageResults[$index].localImagePath", "localImagePath", findings, ::normalizePath)
            compareField(source.metadataOnlyReference, item.metadataOnlyReference, "$.decisionIntake.imageResults[$index].metadataOnlyReference", "metadataOnlyReference", findings)
        }
        return count
    }

    private fun validatePacketBasics(packetId: String, results: List<ImageCandidateExternalResultItem>, findings: MutableList<CandidateReferenceAcceptancePacketFinding>) {
        if (packetId.isBlank()) findings.add(finding("ACCEPTANCE_PACKET_ID_MISSING", "candidate_reference_boundary", "CRITICAL", "$.candidateReferenceAcceptancePacket.packetId", "Acceptance packet requires packet id."))
        if (results.isEmpty()) findings.add(finding("IMAGE_RESULTS_MISSING", "candidate_reference_boundary", "CRITICAL", "$.candidateReferenceAcceptancePacket.imageResults", "Acceptance packet requires candidate image result entries."))
    }

    private fun validateForbiddenRemoteOrProviderFields(results: List<ImageCandidateExternalResultItem>, findings: MutableList<CandidateReferenceAcceptancePacketFinding>): Int {
        var count = 0
        results.forEachIndexed { index, item ->
            listOf(Triple(item.providerJobId, "providerJobId", "PROVIDER_JOB_ID_FORBIDDEN"), Triple(item.apiResponse, "apiResponse", "API_RESPONSE_FORBIDDEN"), Triple(item.remoteUrl, "remoteUrl", "REMOTE_URL_FORBIDDEN"), Triple(item.generatedStatus, "generatedStatus", "GENERATED_STATUS_FORBIDDEN")).forEach { (value, field, ruleId) ->
                if (value.isNotBlank()) { count += 1; findings.add(finding(ruleId, "safety_boundary", "CRITICAL", "$.decisionIntake.imageResults[$index].$field", "Acceptance packet cannot carry provider job, API response, remote URL, or generated status.", evidence = listOf(value))) }
            }
            if (item.localImagePath.isNotBlank() && urlRegex.containsMatchIn(item.localImagePath.trim())) { count += 1; findings.add(finding("REMOTE_IMAGE_PATH_FORBIDDEN", "safety_boundary", "CRITICAL", "$.decisionIntake.imageResults[$index].localImagePath", "Acceptance packet only allows local image paths.", evidence = listOf(item.localImagePath))) }
            if (item.metadataOnlyReference.isNotBlank() && (embeddedUrlRegex.containsMatchIn(item.metadataOnlyReference) || providerReferenceRegex.containsMatchIn(item.metadataOnlyReference))) { count += 1; findings.add(finding("REMOTE_METADATA_REFERENCE_FORBIDDEN", "safety_boundary", "CRITICAL", "$.decisionIntake.imageResults[$index].metadataOnlyReference", "Acceptance packet cannot carry remote URLs, provider job references, or API response references.", evidence = listOf(item.metadataOnlyReference))) }
        }
        return count
    }

    private fun scanText(status: String, results: List<ImageCandidateExternalResultItem>, patterns: List<Regex>, ruleId: String, category: String, findings: MutableList<CandidateReferenceAcceptancePacketFinding>): Int {
        var count = 0
        val fields = mutableListOf(TextField("$.candidateReferenceAcceptancePacket.packetStatus", status))
        results.forEachIndexed { index, item ->
            fields += TextField("$.decisionIntake.imageResults[$index].assetType", item.assetType)
            fields += TextField("$.decisionIntake.imageResults[$index].candidateStatus", item.candidateStatus)
            fields += TextField("$.decisionIntake.imageResults[$index].metadataOnlyReference", item.metadataOnlyReference)
            fields += TextField("$.decisionIntake.imageResults[$index].notes", item.notes)
        }
        fields.filter { it.value.isNotBlank() }.forEach { field -> patterns.forEach { pattern -> if (pattern.containsMatchIn(field.value)) { count += 1; findings.add(finding(ruleId, category, "CRITICAL", field.path, "Acceptance packet cannot carry approval, registered, lock, final, generated, canon, or runtime claims.", evidence = listOf(field.value))) } } }
        return count
    }

    private fun compareField(sourceValue: String, intakeValue: String, path: String, label: String, findings: MutableList<CandidateReferenceAcceptancePacketFinding>, normalize: (String) -> String = { it.trim() }) {
        if (normalize(sourceValue) != normalize(intakeValue)) findings.add(finding("IMAGE_RESULT_SOURCE_MISMATCH", "source_linkage", "CRITICAL", path, "Acceptance packet image result $label must match validation report.", evidence = listOf("validation=$sourceValue", "decisionIntake=$intakeValue")))
    }

    private fun normalizePath(value: String): String = value.trim().replace('\\', '/')
    private fun finding(ruleId: String, category: String, severity: String, path: String, message: String, evidence: List<String> = emptyList(), suggestedFix: String = ""): CandidateReferenceAcceptancePacketFinding =
        CandidateReferenceAcceptancePacketFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
    private data class TextField(val path: String, val value: String)
}
