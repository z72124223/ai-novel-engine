package com.localnovel.engine.service

import com.localnovel.engine.model.ImageCandidateExternalResultItem
import com.localnovel.engine.model.ImageCandidateReferencePacket
import com.localnovel.engine.model.ImageCandidateReferencePacketValidationEvidence
import com.localnovel.engine.model.ImageCandidateReferencePacketValidationFinding
import com.localnovel.engine.model.ImageCandidateReferencePacketValidationReport

class ImageCandidateReferencePacketValidationService {
    private val requiredPacketNextStep = "candidate_reference_packet_validation"
    private val nextAllowedDownstreamStep = "candidate_reference_human_acceptance_ready"
    private val allowedPacketStatuses = setOf("candidate", "reference_candidate", "reviewed_candidate")
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
        Regex("""(?i)\basset\s+approved\b"""),
        Regex("""(?i)\bassets\s+approved\b"""),
        Regex("""(?i)\basset\s+approval\s+complete\b"""),
        Regex("""(?i)\bproduction\s+locked\b"""),
        Regex("""(?i)\bcanon\s+applied\b"""),
        Regex("""(?i)\bruntime\s+scene\s+created\b"""),
        Regex("\u7d20\u6750\u5df2\u6838\u51c6|\u88fd\u4f5c\u9396\u5b9a|\u6b63\u5178\u5df2\u5957\u7528|\u57f7\u884c\u671f\u5834\u666f\u5df2\u5efa\u7acb"),
    )

    fun validate(
        referencePacket: ImageCandidateReferencePacket,
        referencePacketFile: String = "",
    ): ImageCandidateReferencePacketValidationReport {
        val findings = mutableListOf<ImageCandidateReferencePacketValidationFinding>()
        validatePacketState(referencePacket, findings)
        val sourceCount = validateSourceFields(referencePacket, referencePacketFile, findings)
        validateImageResults(referencePacket.imageResults, findings)
        val forbiddenRemoteOrProviderCount = validateForbiddenRemoteOrProviderFields(referencePacket.imageResults, findings)
        val forbiddenStatusCount = scanText(referencePacket, forbiddenStatusPatterns, "CANDIDATE_REFERENCE_STATUS_FORBIDDEN", "candidate_reference_boundary", "Candidate reference packet validation cannot allow approved, locked, final, production-ready, approved-reference, or production-locked wording.", findings)
        val forbiddenClaimCount = scanText(referencePacket, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", "Candidate reference packet validation cannot allow asset approval, production lock, canon application, or runtime scene creation claims.", findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId } else ""

        return ImageCandidateReferencePacketValidationReport(
            verdict = verdict,
            referencePacketFile = referencePacketFile,
            packetId = referencePacket.packetId,
            packetStatus = referencePacket.packetStatus,
            resultReviewPreflightFile = referencePacket.resultReviewPreflightFile,
            resultReviewDecisionIntakeFile = referencePacket.resultReviewDecisionIntakeFile,
            resultReviewPreflightId = referencePacket.resultReviewPreflightId,
            decisionId = referencePacket.decisionId,
            externalResultIntakeFile = referencePacket.externalResultIntakeFile,
            resultId = referencePacket.resultId,
            providerHandoffPacketValidationReportFile = referencePacket.providerHandoffPacketValidationReportFile,
            providerHandoffPacketId = referencePacket.providerHandoffPacketId,
            providerHandoffPreflightId = referencePacket.providerHandoffPreflightId,
            providerHandoffDecisionId = referencePacket.providerHandoffDecisionId,
            acceptedScriptPacketId = referencePacket.acceptedScriptPacketId,
            scriptPacketDraftFile = referencePacket.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = referencePacket.scriptPacketDraftContentSha256,
            imageResults = if (verdict == "FAIL") emptyList() else referencePacket.imageResults,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = ImageCandidateReferencePacketValidationEvidence(
                referencePacketVerdict = referencePacket.verdict,
                referencePacketNextAllowedDownstreamStep = referencePacket.nextAllowedDownstreamStep,
                sourceFieldCount = sourceCount,
                imageResultCount = referencePacket.imageResults.size,
                forbiddenRemoteOrProviderFieldCount = forbiddenRemoteOrProviderCount,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
            ),
        )
    }

    private fun validatePacketState(packet: ImageCandidateReferencePacket, findings: MutableList<ImageCandidateReferencePacketValidationFinding>) {
        if (packet.verdict.trim().uppercase() != "PASS") findings.add(finding("CANDIDATE_REFERENCE_PACKET_NOT_PASS", "upstream", "CRITICAL", "$.candidateReferencePacket.verdict", "Candidate reference packet validation requires a PASS packet.", evidence = listOf(packet.verdict), suggestedFix = "Use a PASS packet."))
        if (packet.nextAllowedDownstreamStep != requiredPacketNextStep) findings.add(finding("CANDIDATE_REFERENCE_PACKET_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.candidateReferencePacket.nextAllowedDownstreamStep", "Candidate reference packet validation requires nextAllowedDownstreamStep=candidate_reference_packet_validation.", evidence = listOf(packet.nextAllowedDownstreamStep), suggestedFix = "Use the packet that explicitly allows validation."))
        if (packet.packetStatus.trim().lowercase() !in allowedPacketStatuses) findings.add(finding("REFERENCE_PACKET_STATUS_NOT_ALLOWED", "candidate_reference_boundary", "CRITICAL", "$.candidateReferencePacket.packetStatus", "Candidate reference packet status must remain candidate-only.", evidence = listOf(packet.packetStatus), suggestedFix = "Use candidate, reference_candidate, or reviewed_candidate."))
    }

    private fun validateSourceFields(packet: ImageCandidateReferencePacket, packetFile: String, findings: MutableList<ImageCandidateReferencePacketValidationFinding>): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) { if (value.isBlank()) findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Candidate reference packet validation requires nonblank $label.", suggestedFix = "Use a complete packet.")) else count += 1 }
        requireNonBlank(packetFile, "$.inputs.referencePacketFile", "REFERENCE_PACKET_FILE_MISSING", "reference packet file")
        requireNonBlank(packet.packetId, "$.candidateReferencePacket.packetId", "REFERENCE_PACKET_ID_MISSING", "packet id")
        requireNonBlank(packet.resultReviewPreflightFile, "$.candidateReferencePacket.resultReviewPreflightFile", "RESULT_REVIEW_PREFLIGHT_FILE_MISSING", "result review preflight file")
        requireNonBlank(packet.resultReviewDecisionIntakeFile, "$.candidateReferencePacket.resultReviewDecisionIntakeFile", "RESULT_REVIEW_DECISION_INTAKE_FILE_MISSING", "decision intake file")
        requireNonBlank(packet.resultReviewPreflightId, "$.candidateReferencePacket.resultReviewPreflightId", "RESULT_REVIEW_PREFLIGHT_ID_MISSING", "result review preflight id")
        requireNonBlank(packet.decisionId, "$.candidateReferencePacket.decisionId", "DECISION_ID_MISSING", "decision id")
        requireNonBlank(packet.externalResultIntakeFile, "$.candidateReferencePacket.externalResultIntakeFile", "EXTERNAL_RESULT_INTAKE_FILE_MISSING", "external result intake file")
        requireNonBlank(packet.resultId, "$.candidateReferencePacket.resultId", "RESULT_ID_MISSING", "result id")
        requireNonBlank(packet.providerHandoffPacketValidationReportFile, "$.candidateReferencePacket.providerHandoffPacketValidationReportFile", "PROVIDER_HANDOFF_PACKET_VALIDATION_REPORT_FILE_MISSING", "provider handoff packet validation report file")
        requireNonBlank(packet.providerHandoffPacketId, "$.candidateReferencePacket.providerHandoffPacketId", "PROVIDER_HANDOFF_PACKET_ID_MISSING", "provider handoff packet id")
        requireNonBlank(packet.providerHandoffPreflightId, "$.candidateReferencePacket.providerHandoffPreflightId", "PROVIDER_HANDOFF_PREFLIGHT_ID_MISSING", "provider handoff preflight id")
        requireNonBlank(packet.providerHandoffDecisionId, "$.candidateReferencePacket.providerHandoffDecisionId", "PROVIDER_HANDOFF_DECISION_ID_MISSING", "provider handoff decision id")
        requireNonBlank(packet.acceptedScriptPacketId, "$.candidateReferencePacket.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(packet.scriptPacketDraftFile, "$.candidateReferencePacket.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(packet.scriptPacketDraftContentSha256, "$.candidateReferencePacket.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        if (packet.scriptPacketDraftContentSha256.isNotBlank() && !sha256Regex.matches(packet.scriptPacketDraftContentSha256.trim().lowercase())) findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.candidateReferencePacket.scriptPacketDraftContentSha256", "Candidate reference packet validation requires a valid lowercase SHA-256 script packet draft hash.", evidence = listOf(packet.scriptPacketDraftContentSha256), suggestedFix = "Use a valid SHA-256."))
        return count
    }

    private fun validateImageResults(results: List<ImageCandidateExternalResultItem>, findings: MutableList<ImageCandidateReferencePacketValidationFinding>) {
        if (results.isEmpty()) findings.add(finding("IMAGE_RESULTS_MISSING", "candidate_reference_boundary", "CRITICAL", "$.candidateReferencePacket.imageResults", "Candidate reference packet validation requires image result entries.", suggestedFix = "Use a packet with candidate results."))
        results.forEachIndexed { index, item ->
            if (item.resultItemId.isBlank() || item.promptItemId.isBlank() || item.sourceCandidateId.isBlank() || item.assetType.isBlank()) findings.add(finding("IMAGE_RESULT_ITEM_INCOMPLETE", "candidate_reference_boundary", "CRITICAL", "$.candidateReferencePacket.imageResults[$index]", "Image result item requires result id, prompt id, candidate id, and asset type.", suggestedFix = "Use complete result entries."))
            if (item.localImagePath.isBlank() && item.metadataOnlyReference.isBlank()) findings.add(finding("IMAGE_RESULT_REFERENCE_MISSING", "candidate_reference_boundary", "CRITICAL", "$.candidateReferencePacket.imageResults[$index]", "Image result item requires local image path or metadata-only reference.", suggestedFix = "Keep a local candidate reference visible."))
        }
    }

    private fun validateForbiddenRemoteOrProviderFields(results: List<ImageCandidateExternalResultItem>, findings: MutableList<ImageCandidateReferencePacketValidationFinding>): Int {
        var count = 0
        results.forEachIndexed { index, item ->
            listOf(Triple(item.providerJobId, "providerJobId", "PROVIDER_JOB_ID_FORBIDDEN"), Triple(item.apiResponse, "apiResponse", "API_RESPONSE_FORBIDDEN"), Triple(item.remoteUrl, "remoteUrl", "REMOTE_URL_FORBIDDEN"), Triple(item.generatedStatus, "generatedStatus", "GENERATED_STATUS_FORBIDDEN")).forEach { (value, field, ruleId) ->
                if (value.isNotBlank()) { count += 1; findings.add(finding(ruleId, "safety_boundary", "CRITICAL", "$.candidateReferencePacket.imageResults[$index].$field", "Candidate reference packet validation cannot carry provider job, API response, remote URL, or generated status.", evidence = listOf(value), suggestedFix = "Remove provider/job/remote/generated fields.")) }
            }
            if (item.localImagePath.isNotBlank() && urlRegex.containsMatchIn(item.localImagePath.trim())) { count += 1; findings.add(finding("REMOTE_IMAGE_PATH_FORBIDDEN", "safety_boundary", "CRITICAL", "$.candidateReferencePacket.imageResults[$index].localImagePath", "Candidate reference packet validation only allows local image paths.", evidence = listOf(item.localImagePath), suggestedFix = "Use a local path or metadata-only reference.")) }
            if (item.metadataOnlyReference.isNotBlank() && (embeddedUrlRegex.containsMatchIn(item.metadataOnlyReference) || providerReferenceRegex.containsMatchIn(item.metadataOnlyReference))) { count += 1; findings.add(finding("REMOTE_METADATA_REFERENCE_FORBIDDEN", "safety_boundary", "CRITICAL", "$.candidateReferencePacket.imageResults[$index].metadataOnlyReference", "Candidate reference packet validation cannot carry remote URLs, provider job references, or API response references in metadataOnlyReference.", evidence = listOf(item.metadataOnlyReference), suggestedFix = "Use local candidate metadata.")) }
        }
        return count
    }

    private fun scanText(packet: ImageCandidateReferencePacket, patterns: List<Regex>, ruleId: String, category: String, message: String, findings: MutableList<ImageCandidateReferencePacketValidationFinding>): Int {
        var count = 0
        val fields = mutableListOf(TextField("$.candidateReferencePacket.packetStatus", packet.packetStatus))
        packet.reviewNotes.forEachIndexed { index, value -> fields += TextField("$.candidateReferencePacket.reviewNotes[$index]", value) }
        packet.reviewChecklistSummary.forEachIndexed { index, value -> fields += TextField("$.candidateReferencePacket.reviewChecklistSummary[$index]", value) }
        packet.imageResults.forEachIndexed { index, item ->
            fields += TextField("$.candidateReferencePacket.imageResults[$index].assetType", item.assetType)
            fields += TextField("$.candidateReferencePacket.imageResults[$index].candidateStatus", item.candidateStatus)
            fields += TextField("$.candidateReferencePacket.imageResults[$index].localImagePath", item.localImagePath)
            fields += TextField("$.candidateReferencePacket.imageResults[$index].metadataOnlyReference", item.metadataOnlyReference)
            fields += TextField("$.candidateReferencePacket.imageResults[$index].notes", item.notes)
            item.residualRisks.forEachIndexed { itemIndex, value -> fields += TextField("$.candidateReferencePacket.imageResults[$index].residualRisks[$itemIndex]", value) }
        }
        fields.filter { it.value.isNotBlank() }.forEach { field -> patterns.forEach { pattern -> if (pattern.containsMatchIn(field.value)) { count += 1; findings.add(finding(ruleId, category, "CRITICAL", field.path, message, evidence = listOf(field.value), suggestedFix = "Keep candidate reference validation unapproved and unlocked.")) } } }
        return count
    }

    private fun finding(ruleId: String, category: String, severity: String, path: String, message: String, evidence: List<String> = emptyList(), suggestedFix: String = ""): ImageCandidateReferencePacketValidationFinding =
        ImageCandidateReferencePacketValidationFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
    private data class TextField(val path: String, val value: String)
}
