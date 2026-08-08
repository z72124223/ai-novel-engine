package com.localnovel.engine.service

import com.localnovel.engine.model.CandidateReferenceRegistryPacket
import com.localnovel.engine.model.CandidateReferenceRegistryPacketEvidence
import com.localnovel.engine.model.CandidateReferenceRegistryPacketFinding
import com.localnovel.engine.model.CandidateReferenceRegistryPreflightReport
import com.localnovel.engine.model.ImageCandidateExternalResultItem

class CandidateReferenceRegistryPacketService {
    private val requiredPreflightNextStep = "candidate_reference_registry_packet"
    private val nextAllowedDownstreamStep = "candidate_reference_registry_packet_validation"
    private val allowedPacketStatuses = setOf("candidate_reference_candidate", "candidate_registry_preparation")
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

    fun create(
        registryPreflight: CandidateReferenceRegistryPreflightReport,
        packetId: String = "",
        registryPreflightFile: String = "",
    ): CandidateReferenceRegistryPacket {
        val findings = mutableListOf<CandidateReferenceRegistryPacketFinding>()
        validatePreflight(registryPreflight, findings)
        val sourceCount = validateSourceFields(registryPreflight, registryPreflightFile, findings)
        val resolvedPacketId = packetId.ifBlank { registryPreflight.preflightId.takeIf { it.isNotBlank() }?.let { "$it-registry-packet" }.orEmpty() }
        validatePacketBasics(resolvedPacketId, registryPreflight.imageResults, findings)
        val forbiddenRemoteOrProviderCount = validateForbiddenRemoteOrProviderFields(registryPreflight.imageResults, findings)
        val forbiddenStatusCount = scanText("candidate_reference_candidate", registryPreflight.imageResults, forbiddenStatusPatterns, "CANDIDATE_REFERENCE_STATUS_FORBIDDEN", "candidate_reference_boundary", findings)
        val forbiddenClaimCount = scanText("candidate_reference_candidate", registryPreflight.imageResults, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId } else ""

        return CandidateReferenceRegistryPacket(
            verdict = verdict,
            packetId = resolvedPacketId,
            packetStatus = "candidate_reference_candidate",
            registryPreflightFile = registryPreflightFile,
            preflightId = registryPreflight.preflightId,
            humanAcceptanceDecisionIntakeFile = registryPreflight.humanAcceptanceDecisionIntakeFile,
            decisionId = registryPreflight.decisionId,
            candidateReferencePacketValidationReportFile = registryPreflight.candidateReferencePacketValidationReportFile,
            referencePacketFile = registryPreflight.referencePacketFile,
            referencePacketId = registryPreflight.packetId,
            resultReviewPreflightId = registryPreflight.resultReviewPreflightId,
            resultReviewDecisionIntakeFile = registryPreflight.resultReviewDecisionIntakeFile,
            resultReviewDecisionId = registryPreflight.resultReviewDecisionId,
            acceptedScriptPacketId = registryPreflight.acceptedScriptPacketId,
            scriptPacketDraftFile = registryPreflight.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = registryPreflight.scriptPacketDraftContentSha256,
            providerHandoffPacketValidationReportFile = registryPreflight.providerHandoffPacketValidationReportFile,
            providerHandoffPacketId = registryPreflight.providerHandoffPacketId,
            providerHandoffPreflightId = registryPreflight.providerHandoffPreflightId,
            providerHandoffDecisionId = registryPreflight.providerHandoffDecisionId,
            externalResultIntakeFile = registryPreflight.externalResultIntakeFile,
            resultId = registryPreflight.resultId,
            imageResults = if (verdict == "FAIL") emptyList() else registryPreflight.imageResults,
            preparationChecklist = if (verdict == "FAIL") emptyList() else listOf(
                "source_binding_preserved",
                "candidate_only_status_preserved",
                "remote_provider_fields_absent",
                "no_approval_no_lock_no_canon",
                "requires_future_validation_before_registry_write",
            ),
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = CandidateReferenceRegistryPacketEvidence(
                registryPreflightVerdict = registryPreflight.verdict,
                registryPreflightNextAllowedDownstreamStep = registryPreflight.nextAllowedDownstreamStep,
                sourceFieldCount = sourceCount,
                imageResultCount = registryPreflight.imageResults.size,
                forbiddenRemoteOrProviderFieldCount = forbiddenRemoteOrProviderCount,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
            ),
        )
    }

    private fun validatePreflight(preflight: CandidateReferenceRegistryPreflightReport, findings: MutableList<CandidateReferenceRegistryPacketFinding>) {
        if (preflight.verdict.trim().uppercase() != "PASS") findings.add(finding("REGISTRY_PREFLIGHT_NOT_PASS", "upstream", "CRITICAL", "$.registryPreflight.verdict", "Candidate reference registry packet requires a PASS registry preflight.", evidence = listOf(preflight.verdict)))
        if (preflight.nextAllowedDownstreamStep != requiredPreflightNextStep) findings.add(finding("REGISTRY_PREFLIGHT_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.registryPreflight.nextAllowedDownstreamStep", "Candidate reference registry packet requires nextAllowedDownstreamStep=candidate_reference_registry_packet.", evidence = listOf(preflight.nextAllowedDownstreamStep)))
    }

    private fun validateSourceFields(preflight: CandidateReferenceRegistryPreflightReport, preflightFile: String, findings: MutableList<CandidateReferenceRegistryPacketFinding>): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) { if (value.isBlank()) findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Candidate reference registry packet requires nonblank $label.")) else count += 1 }
        requireNonBlank(preflightFile, "$.inputs.registryPreflightFile", "REGISTRY_PREFLIGHT_FILE_MISSING", "registry preflight file")
        requireNonBlank(preflight.preflightId, "$.registryPreflight.preflightId", "REGISTRY_PREFLIGHT_ID_MISSING", "registry preflight id")
        requireNonBlank(preflight.humanAcceptanceDecisionIntakeFile, "$.registryPreflight.humanAcceptanceDecisionIntakeFile", "HUMAN_ACCEPTANCE_DECISION_INTAKE_FILE_MISSING", "human acceptance decision intake file")
        requireNonBlank(preflight.decisionId, "$.registryPreflight.decisionId", "DECISION_ID_MISSING", "decision id")
        requireNonBlank(preflight.referencePacketFile, "$.registryPreflight.referencePacketFile", "REFERENCE_PACKET_FILE_MISSING", "reference packet file")
        requireNonBlank(preflight.packetId, "$.registryPreflight.packetId", "REFERENCE_PACKET_ID_MISSING", "reference packet id")
        requireNonBlank(preflight.acceptedScriptPacketId, "$.registryPreflight.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(preflight.scriptPacketDraftFile, "$.registryPreflight.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(preflight.scriptPacketDraftContentSha256, "$.registryPreflight.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        if (preflight.scriptPacketDraftContentSha256.isNotBlank() && !sha256Regex.matches(preflight.scriptPacketDraftContentSha256.trim().lowercase())) findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.registryPreflight.scriptPacketDraftContentSha256", "Candidate reference registry packet requires a valid lowercase SHA-256 script packet draft hash.", evidence = listOf(preflight.scriptPacketDraftContentSha256)))
        return count
    }

    private fun validatePacketBasics(packetId: String, results: List<ImageCandidateExternalResultItem>, findings: MutableList<CandidateReferenceRegistryPacketFinding>) {
        if (packetId.isBlank()) findings.add(finding("REGISTRY_PACKET_ID_MISSING", "candidate_reference_registry_boundary", "CRITICAL", "$.candidateReferenceRegistryPacket.packetId", "Candidate reference registry packet requires packet id.", suggestedFix = "Provide --packet or use preflight id."))
        if ("candidate_reference_candidate" !in allowedPacketStatuses) findings.add(finding("REGISTRY_PACKET_STATUS_NOT_ALLOWED", "candidate_reference_registry_boundary", "CRITICAL", "$.candidateReferenceRegistryPacket.packetStatus", "Candidate reference registry packet status must remain candidate-only."))
        if (results.isEmpty()) findings.add(finding("IMAGE_RESULTS_MISSING", "candidate_reference_registry_boundary", "CRITICAL", "$.registryPreflight.imageResults", "Candidate reference registry packet requires candidate image results."))
    }

    private fun validateForbiddenRemoteOrProviderFields(results: List<ImageCandidateExternalResultItem>, findings: MutableList<CandidateReferenceRegistryPacketFinding>): Int {
        var count = 0
        results.forEachIndexed { index, item ->
            listOf(Triple(item.providerJobId, "providerJobId", "PROVIDER_JOB_ID_FORBIDDEN"), Triple(item.apiResponse, "apiResponse", "API_RESPONSE_FORBIDDEN"), Triple(item.remoteUrl, "remoteUrl", "REMOTE_URL_FORBIDDEN"), Triple(item.generatedStatus, "generatedStatus", "GENERATED_STATUS_FORBIDDEN")).forEach { (value, field, ruleId) ->
                if (value.isNotBlank()) { count += 1; findings.add(finding(ruleId, "safety_boundary", "CRITICAL", "$.registryPreflight.imageResults[$index].$field", "Candidate reference registry packet cannot carry provider job, API response, remote URL, or generated status.", evidence = listOf(value))) }
            }
            if (item.localImagePath.isNotBlank() && urlRegex.containsMatchIn(item.localImagePath.trim())) { count += 1; findings.add(finding("REMOTE_IMAGE_PATH_FORBIDDEN", "safety_boundary", "CRITICAL", "$.registryPreflight.imageResults[$index].localImagePath", "Candidate reference registry packet only allows local image paths.", evidence = listOf(item.localImagePath))) }
            if (item.metadataOnlyReference.isNotBlank() && (embeddedUrlRegex.containsMatchIn(item.metadataOnlyReference) || providerReferenceRegex.containsMatchIn(item.metadataOnlyReference))) { count += 1; findings.add(finding("REMOTE_METADATA_REFERENCE_FORBIDDEN", "safety_boundary", "CRITICAL", "$.registryPreflight.imageResults[$index].metadataOnlyReference", "Candidate reference registry packet cannot carry remote URLs, provider job references, or API response references.", evidence = listOf(item.metadataOnlyReference))) }
        }
        return count
    }

    private fun scanText(status: String, results: List<ImageCandidateExternalResultItem>, patterns: List<Regex>, ruleId: String, category: String, findings: MutableList<CandidateReferenceRegistryPacketFinding>): Int {
        var count = 0
        val fields = mutableListOf(TextField("$.candidateReferenceRegistryPacket.packetStatus", status))
        results.forEachIndexed { index, item ->
            fields += TextField("$.registryPreflight.imageResults[$index].assetType", item.assetType)
            fields += TextField("$.registryPreflight.imageResults[$index].candidateStatus", item.candidateStatus)
            fields += TextField("$.registryPreflight.imageResults[$index].localImagePath", item.localImagePath)
            fields += TextField("$.registryPreflight.imageResults[$index].metadataOnlyReference", item.metadataOnlyReference)
            fields += TextField("$.registryPreflight.imageResults[$index].notes", item.notes)
            item.residualRisks.forEachIndexed { itemIndex, value -> fields += TextField("$.registryPreflight.imageResults[$index].residualRisks[$itemIndex]", value) }
        }
        fields.filter { it.value.isNotBlank() }.forEach { field -> patterns.forEach { pattern -> if (pattern.containsMatchIn(field.value)) { count += 1; findings.add(finding(ruleId, category, "CRITICAL", field.path, "Candidate reference registry packet cannot carry approval, lock, final, generated, canon, or runtime claims.", evidence = listOf(field.value))) } } }
        return count
    }

    private fun finding(ruleId: String, category: String, severity: String, path: String, message: String, evidence: List<String> = emptyList(), suggestedFix: String = ""): CandidateReferenceRegistryPacketFinding =
        CandidateReferenceRegistryPacketFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
    private data class TextField(val path: String, val value: String)
}
