package com.localnovel.engine.service

import com.localnovel.engine.model.CandidateReferenceRegistryPacket
import com.localnovel.engine.model.CandidateReferenceRegistryPacketValidationEvidence
import com.localnovel.engine.model.CandidateReferenceRegistryPacketValidationFinding
import com.localnovel.engine.model.CandidateReferenceRegistryPacketValidationReport
import com.localnovel.engine.model.ImageCandidateExternalResultItem

class CandidateReferenceRegistryPacketValidationService {
    private val requiredPacketStatus = "candidate_reference_candidate"
    private val requiredPacketNextStep = "candidate_reference_registry_packet_validation"
    private val nextAllowedDownstreamStep = "candidate_reference_registry_human_acceptance_ready"
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
        registryPacket: CandidateReferenceRegistryPacket,
        registryPacketFile: String = "",
    ): CandidateReferenceRegistryPacketValidationReport {
        val findings = mutableListOf<CandidateReferenceRegistryPacketValidationFinding>()
        validatePacketState(registryPacket, findings)
        val sourceCount = validateSourceFields(registryPacket, registryPacketFile, findings)
        validateImageResults(registryPacket.imageResults, findings)
        val forbiddenRemoteOrProviderCount = validateForbiddenRemoteOrProviderFields(registryPacket.imageResults, findings)
        val forbiddenStatusCount = scanText(registryPacket, forbiddenStatusPatterns, "CANDIDATE_REFERENCE_STATUS_FORBIDDEN", "candidate_reference_boundary", findings)
        val forbiddenClaimCount = scanText(registryPacket, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", findings)
        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId } else ""

        return CandidateReferenceRegistryPacketValidationReport(
            verdict = verdict,
            registryPacketFile = registryPacketFile,
            packetId = registryPacket.packetId,
            packetStatus = registryPacket.packetStatus,
            registryPreflightFile = registryPacket.registryPreflightFile,
            preflightId = registryPacket.preflightId,
            humanAcceptanceDecisionIntakeFile = registryPacket.humanAcceptanceDecisionIntakeFile,
            decisionId = registryPacket.decisionId,
            candidateReferencePacketValidationReportFile = registryPacket.candidateReferencePacketValidationReportFile,
            referencePacketFile = registryPacket.referencePacketFile,
            referencePacketId = registryPacket.referencePacketId,
            resultReviewPreflightId = registryPacket.resultReviewPreflightId,
            resultReviewDecisionIntakeFile = registryPacket.resultReviewDecisionIntakeFile,
            resultReviewDecisionId = registryPacket.resultReviewDecisionId,
            acceptedScriptPacketId = registryPacket.acceptedScriptPacketId,
            scriptPacketDraftFile = registryPacket.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = registryPacket.scriptPacketDraftContentSha256,
            providerHandoffPacketValidationReportFile = registryPacket.providerHandoffPacketValidationReportFile,
            providerHandoffPacketId = registryPacket.providerHandoffPacketId,
            providerHandoffPreflightId = registryPacket.providerHandoffPreflightId,
            providerHandoffDecisionId = registryPacket.providerHandoffDecisionId,
            externalResultIntakeFile = registryPacket.externalResultIntakeFile,
            resultId = registryPacket.resultId,
            imageResults = if (verdict == "FAIL") emptyList() else registryPacket.imageResults,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = CandidateReferenceRegistryPacketValidationEvidence(
                registryPacketVerdict = registryPacket.verdict,
                registryPacketNextAllowedDownstreamStep = registryPacket.nextAllowedDownstreamStep,
                sourceFieldCount = sourceCount,
                imageResultCount = registryPacket.imageResults.size,
                forbiddenRemoteOrProviderFieldCount = forbiddenRemoteOrProviderCount,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
            ),
        )
    }

    private fun validatePacketState(packet: CandidateReferenceRegistryPacket, findings: MutableList<CandidateReferenceRegistryPacketValidationFinding>) {
        if (packet.verdict.trim().uppercase() != "PASS") findings.add(finding("REGISTRY_PACKET_NOT_PASS", "upstream", "CRITICAL", "$.candidateReferenceRegistryPacket.verdict", "Registry packet validation requires a PASS registry preparation packet.", evidence = listOf(packet.verdict)))
        if (packet.packetStatus != requiredPacketStatus) findings.add(finding("REGISTRY_PACKET_STATUS_MISMATCH", "candidate_reference_boundary", "CRITICAL", "$.candidateReferenceRegistryPacket.packetStatus", "Registry packet validation requires packetStatus=candidate_reference_candidate.", evidence = listOf(packet.packetStatus)))
        if (packet.nextAllowedDownstreamStep != requiredPacketNextStep) findings.add(finding("REGISTRY_PACKET_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.candidateReferenceRegistryPacket.nextAllowedDownstreamStep", "Registry packet validation requires nextAllowedDownstreamStep=candidate_reference_registry_packet_validation.", evidence = listOf(packet.nextAllowedDownstreamStep)))
    }

    private fun validateSourceFields(packet: CandidateReferenceRegistryPacket, packetFile: String, findings: MutableList<CandidateReferenceRegistryPacketValidationFinding>): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) { if (value.isBlank()) findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Registry packet validation requires nonblank $label.")) else count += 1 }
        requireNonBlank(packetFile, "$.inputs.registryPacketFile", "REGISTRY_PACKET_FILE_MISSING", "registry packet file")
        requireNonBlank(packet.packetId, "$.candidateReferenceRegistryPacket.packetId", "REGISTRY_PACKET_ID_MISSING", "registry packet id")
        requireNonBlank(packet.registryPreflightFile, "$.candidateReferenceRegistryPacket.registryPreflightFile", "REGISTRY_PREFLIGHT_FILE_MISSING", "registry preflight file")
        requireNonBlank(packet.preflightId, "$.candidateReferenceRegistryPacket.preflightId", "REGISTRY_PREFLIGHT_ID_MISSING", "registry preflight id")
        requireNonBlank(packet.humanAcceptanceDecisionIntakeFile, "$.candidateReferenceRegistryPacket.humanAcceptanceDecisionIntakeFile", "HUMAN_ACCEPTANCE_DECISION_INTAKE_FILE_MISSING", "human acceptance decision intake file")
        requireNonBlank(packet.decisionId, "$.candidateReferenceRegistryPacket.decisionId", "DECISION_ID_MISSING", "decision id")
        requireNonBlank(packet.candidateReferencePacketValidationReportFile, "$.candidateReferenceRegistryPacket.candidateReferencePacketValidationReportFile", "CANDIDATE_REFERENCE_PACKET_VALIDATION_REPORT_FILE_MISSING", "candidate reference packet validation report file")
        requireNonBlank(packet.referencePacketFile, "$.candidateReferenceRegistryPacket.referencePacketFile", "REFERENCE_PACKET_FILE_MISSING", "reference packet file")
        requireNonBlank(packet.referencePacketId, "$.candidateReferenceRegistryPacket.referencePacketId", "REFERENCE_PACKET_ID_MISSING", "reference packet id")
        requireNonBlank(packet.resultReviewPreflightId, "$.candidateReferenceRegistryPacket.resultReviewPreflightId", "RESULT_REVIEW_PREFLIGHT_ID_MISSING", "result review preflight id")
        requireNonBlank(packet.resultReviewDecisionIntakeFile, "$.candidateReferenceRegistryPacket.resultReviewDecisionIntakeFile", "RESULT_REVIEW_DECISION_INTAKE_FILE_MISSING", "result review decision intake file")
        requireNonBlank(packet.resultReviewDecisionId, "$.candidateReferenceRegistryPacket.resultReviewDecisionId", "RESULT_REVIEW_DECISION_ID_MISSING", "result review decision id")
        requireNonBlank(packet.acceptedScriptPacketId, "$.candidateReferenceRegistryPacket.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(packet.scriptPacketDraftFile, "$.candidateReferenceRegistryPacket.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(packet.scriptPacketDraftContentSha256, "$.candidateReferenceRegistryPacket.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        requireNonBlank(packet.providerHandoffPacketValidationReportFile, "$.candidateReferenceRegistryPacket.providerHandoffPacketValidationReportFile", "PROVIDER_HANDOFF_PACKET_VALIDATION_REPORT_FILE_MISSING", "provider handoff packet validation report file")
        requireNonBlank(packet.providerHandoffPacketId, "$.candidateReferenceRegistryPacket.providerHandoffPacketId", "PROVIDER_HANDOFF_PACKET_ID_MISSING", "provider handoff packet id")
        requireNonBlank(packet.providerHandoffPreflightId, "$.candidateReferenceRegistryPacket.providerHandoffPreflightId", "PROVIDER_HANDOFF_PREFLIGHT_ID_MISSING", "provider handoff preflight id")
        requireNonBlank(packet.providerHandoffDecisionId, "$.candidateReferenceRegistryPacket.providerHandoffDecisionId", "PROVIDER_HANDOFF_DECISION_ID_MISSING", "provider handoff decision id")
        requireNonBlank(packet.externalResultIntakeFile, "$.candidateReferenceRegistryPacket.externalResultIntakeFile", "EXTERNAL_RESULT_INTAKE_FILE_MISSING", "external result intake file")
        requireNonBlank(packet.resultId, "$.candidateReferenceRegistryPacket.resultId", "RESULT_ID_MISSING", "result id")
        if (packet.scriptPacketDraftContentSha256.isNotBlank() && !sha256Regex.matches(packet.scriptPacketDraftContentSha256.trim().lowercase())) findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.candidateReferenceRegistryPacket.scriptPacketDraftContentSha256", "Registry packet validation requires a valid lowercase SHA-256 script packet draft hash.", evidence = listOf(packet.scriptPacketDraftContentSha256)))
        return count
    }

    private fun validateImageResults(results: List<ImageCandidateExternalResultItem>, findings: MutableList<CandidateReferenceRegistryPacketValidationFinding>) {
        if (results.isEmpty()) findings.add(finding("IMAGE_RESULTS_MISSING", "candidate_reference_boundary", "CRITICAL", "$.candidateReferenceRegistryPacket.imageResults", "Registry packet validation requires candidate image result entries."))
        results.forEachIndexed { index, item ->
            if (item.resultItemId.isBlank() || item.promptItemId.isBlank() || item.sourceCandidateId.isBlank() || item.assetType.isBlank()) findings.add(finding("IMAGE_RESULT_ITEM_INCOMPLETE", "candidate_reference_boundary", "CRITICAL", "$.candidateReferenceRegistryPacket.imageResults[$index]", "Image result item requires result id, prompt id, candidate id, and asset type."))
            if (item.localImagePath.isBlank() && item.metadataOnlyReference.isBlank()) findings.add(finding("IMAGE_RESULT_REFERENCE_MISSING", "candidate_reference_boundary", "CRITICAL", "$.candidateReferenceRegistryPacket.imageResults[$index]", "Image result item requires local image path or metadata-only reference."))
        }
    }

    private fun validateForbiddenRemoteOrProviderFields(results: List<ImageCandidateExternalResultItem>, findings: MutableList<CandidateReferenceRegistryPacketValidationFinding>): Int {
        var count = 0
        results.forEachIndexed { index, item ->
            listOf(Triple(item.providerJobId, "providerJobId", "PROVIDER_JOB_ID_FORBIDDEN"), Triple(item.apiResponse, "apiResponse", "API_RESPONSE_FORBIDDEN"), Triple(item.remoteUrl, "remoteUrl", "REMOTE_URL_FORBIDDEN"), Triple(item.generatedStatus, "generatedStatus", "GENERATED_STATUS_FORBIDDEN")).forEach { (value, field, ruleId) ->
                if (value.isNotBlank()) { count += 1; findings.add(finding(ruleId, "safety_boundary", "CRITICAL", "$.candidateReferenceRegistryPacket.imageResults[$index].$field", "Registry packet validation cannot carry provider job, API response, remote URL, or generated status.", evidence = listOf(value))) }
            }
            if (item.localImagePath.isNotBlank() && urlRegex.containsMatchIn(item.localImagePath.trim())) { count += 1; findings.add(finding("REMOTE_IMAGE_PATH_FORBIDDEN", "safety_boundary", "CRITICAL", "$.candidateReferenceRegistryPacket.imageResults[$index].localImagePath", "Registry packet validation only allows local image paths.", evidence = listOf(item.localImagePath))) }
            if (item.metadataOnlyReference.isNotBlank() && (embeddedUrlRegex.containsMatchIn(item.metadataOnlyReference) || providerReferenceRegex.containsMatchIn(item.metadataOnlyReference))) { count += 1; findings.add(finding("REMOTE_METADATA_REFERENCE_FORBIDDEN", "safety_boundary", "CRITICAL", "$.candidateReferenceRegistryPacket.imageResults[$index].metadataOnlyReference", "Registry packet validation cannot carry remote URLs, provider job references, or API response references.", evidence = listOf(item.metadataOnlyReference))) }
        }
        return count
    }

    private fun scanText(packet: CandidateReferenceRegistryPacket, patterns: List<Regex>, ruleId: String, category: String, findings: MutableList<CandidateReferenceRegistryPacketValidationFinding>): Int {
        var count = 0
        val fields = mutableListOf(TextField("$.candidateReferenceRegistryPacket.packetStatus", packet.packetStatus))
        packet.preparationChecklist.forEachIndexed { index, value -> fields += TextField("$.candidateReferenceRegistryPacket.preparationChecklist[$index]", value) }
        packet.imageResults.forEachIndexed { index, item ->
            fields += TextField("$.candidateReferenceRegistryPacket.imageResults[$index].assetType", item.assetType)
            fields += TextField("$.candidateReferenceRegistryPacket.imageResults[$index].candidateStatus", item.candidateStatus)
            fields += TextField("$.candidateReferenceRegistryPacket.imageResults[$index].localImagePath", item.localImagePath)
            fields += TextField("$.candidateReferenceRegistryPacket.imageResults[$index].metadataOnlyReference", item.metadataOnlyReference)
            fields += TextField("$.candidateReferenceRegistryPacket.imageResults[$index].notes", item.notes)
            item.residualRisks.forEachIndexed { itemIndex, value -> fields += TextField("$.candidateReferenceRegistryPacket.imageResults[$index].residualRisks[$itemIndex]", value) }
        }
        fields.filter { it.value.isNotBlank() }.forEach { field -> patterns.forEach { pattern -> if (pattern.containsMatchIn(field.value)) { count += 1; findings.add(finding(ruleId, category, "CRITICAL", field.path, "Registry packet validation cannot carry approval, registered, lock, final, generated, canon, or runtime claims.", evidence = listOf(field.value))) } } }
        return count
    }

    private fun finding(ruleId: String, category: String, severity: String, path: String, message: String, evidence: List<String> = emptyList(), suggestedFix: String = ""): CandidateReferenceRegistryPacketValidationFinding =
        CandidateReferenceRegistryPacketValidationFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
    private data class TextField(val path: String, val value: String)
}
