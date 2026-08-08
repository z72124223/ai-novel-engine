package com.localnovel.engine.service

import com.localnovel.engine.model.CandidateReferenceAcceptancePacket
import com.localnovel.engine.model.CandidateReferenceAcceptancePacketValidationEvidence
import com.localnovel.engine.model.CandidateReferenceAcceptancePacketValidationFinding
import com.localnovel.engine.model.CandidateReferenceAcceptancePacketValidationReport
import com.localnovel.engine.model.ImageCandidateExternalResultItem
import java.security.MessageDigest

class CandidateReferenceAcceptancePacketValidationService {
    private val requiredPacketStatus = "candidate_reference_acceptance_candidate"
    private val requiredPacketNextStep = "candidate_reference_acceptance_packet_validation"
    private val nextAllowedDownstreamStep = "candidate_reference_acceptance_packet_validated"
    private val allowedHumanSources = setOf("human", "user", "01_control")
    private val disallowedAcceptanceSourceRegex = Regex("""(?i)\b(auto|model|generated|provider|system)\b""")
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
    private val urlRegex = Regex("""(?i)^[a-z][a-z0-9+.-]*://""")
    private val embeddedUrlRegex = Regex("""(?i)\b[a-z][a-z0-9+.-]*://""")
    private val providerReferenceRegex = Regex("""(?i)\bprovider[-_\s]?job\b|\bprovider[-_\s]?id\b|\bapi[-_\s]?(response|result)\b""")
    private val forbiddenStatusPatterns = listOf(
        Regex("""(?i)\bapproved\b"""),
        Regex("""(?i)\bapproved[-_\s]?reference\b"""),
        Regex("""(?i)\bregistered\b"""),
        Regex("""(?i)\bregistered[-_\s]?reference\b"""),
        Regex("""(?i)\bproduction[-_\s]?ready\b"""),
        Regex("""(?i)\bproduction[-_\s]?lock(?:ed)?\b"""),
        Regex("""(?i)\basset[-_\s]?lock(?:ed)?\b"""),
        Regex("""(?i)\blocked\b"""),
        Regex("""(?i)\bfinal(?:[-_\s]?asset)?\b"""),
        Regex("""(?i)\bcanon\b"""),
        Regex("\u6838\u51c6|\u6279\u51c6|\u767b\u8a18\u5b8c\u6210|\u6b63\u5178|\u9396\u5b9a|\u6b63\u5f0f\u7d20\u6750"),
    )
    private val forbiddenClaimPatterns = listOf(
        Regex("""(?i)\bprovider[-_\s]?job\b"""),
        Regex("""(?i)\bapi[-_\s]?(response|result)\b"""),
        Regex("""(?i)\bimage\s+generated\b|\bgenerated\s+image\b"""),
        Regex("""(?i)\bimage[-_\s]?output[-_\s]?path\b"""),
        Regex("""(?i)\bvideo[-_\s]?output[-_\s]?path\b"""),
        Regex("""(?i)\bruntime\s+scene\b"""),
        Regex("""(?i)\basset\s+approval\b|\basset\s+approved\b"""),
        Regex("""(?i)\buser\s+approval\b|\buser\s+approved\b"""),
    )

    fun validate(
        packet: CandidateReferenceAcceptancePacket,
        acceptancePacketFile: String = "",
        validationId: String = "",
    ): CandidateReferenceAcceptancePacketValidationReport {
        val findings = mutableListOf<CandidateReferenceAcceptancePacketValidationFinding>()
        validatePacketState(packet, findings)
        val sourceFieldCount = validateSourceFields(packet, acceptancePacketFile, findings)
        val imageResultCount = validateImageResults(packet, findings)
        val forbiddenRemoteOrProviderCount = validateForbiddenRemoteOrProviderFields(packet.imageResults, findings)
        val forbiddenStatusCount = scanText(packet, forbiddenStatusPatterns, "CANDIDATE_REFERENCE_STATUS_FORBIDDEN", "candidate_reference_boundary", findings)
        val forbiddenClaimCount = scanText(packet, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId } else ""
        val resolvedValidationId = validationId.ifBlank {
            packet.packetId.takeIf { it.isNotBlank() }?.let { "$it-validation" }.orEmpty()
        }

        return CandidateReferenceAcceptancePacketValidationReport(
            verdict = verdict,
            validationId = resolvedValidationId,
            acceptancePacketFile = acceptancePacketFile,
            packetId = packet.packetId,
            packetStatus = packet.packetStatus,
            registryPacketValidationReportFile = packet.registryPacketValidationReportFile,
            humanAcceptanceDecisionIntakeFile = packet.humanAcceptanceDecisionIntakeFile,
            decisionId = packet.decisionId,
            humanAcceptanceDecisionSource = packet.humanAcceptanceDecisionSource,
            registryPacketFile = packet.registryPacketFile,
            registryPacketId = packet.registryPacketId,
            registryPreflightFile = packet.registryPreflightFile,
            registryPreflightId = packet.registryPreflightId,
            acceptedScriptPacketId = packet.acceptedScriptPacketId,
            scriptPacketDraftFile = packet.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = packet.scriptPacketDraftContentSha256,
            referencePacketFile = packet.referencePacketFile,
            referencePacketId = packet.referencePacketId,
            resultReviewPreflightId = packet.resultReviewPreflightId,
            resultReviewDecisionIntakeFile = packet.resultReviewDecisionIntakeFile,
            resultReviewDecisionId = packet.resultReviewDecisionId,
            providerHandoffPacketValidationReportFile = packet.providerHandoffPacketValidationReportFile,
            providerHandoffPacketId = packet.providerHandoffPacketId,
            providerHandoffPreflightId = packet.providerHandoffPreflightId,
            providerHandoffDecisionId = packet.providerHandoffDecisionId,
            externalResultIntakeFile = packet.externalResultIntakeFile,
            resultId = packet.resultId,
            sourceImageResultIdentityDigest = packet.sourceImageResultIdentityDigest,
            imageResults = if (verdict == "FAIL") emptyList() else packet.imageResults,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = CandidateReferenceAcceptancePacketValidationEvidence(
                acceptancePacketVerdict = packet.verdict,
                acceptancePacketNextAllowedDownstreamStep = packet.nextAllowedDownstreamStep,
                registryPacketValidationVerdict = packet.evidence.registryPacketValidationVerdict,
                decisionIntakeVerdict = packet.evidence.decisionIntakeVerdict,
                decisionAllowedDownstream = packet.evidence.decisionAllowedDownstream,
                humanAcceptanceDecisionSource = packet.humanAcceptanceDecisionSource,
                sourceFieldCount = sourceFieldCount,
                imageResultCount = imageResultCount,
                forbiddenRemoteOrProviderFieldCount = forbiddenRemoteOrProviderCount,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
            ),
        )
    }

    private fun validatePacketState(packet: CandidateReferenceAcceptancePacket, findings: MutableList<CandidateReferenceAcceptancePacketValidationFinding>) {
        if (packet.verdict.trim().uppercase() != "PASS") findings.add(finding("ACCEPTANCE_PACKET_NOT_PASS", "upstream", "CRITICAL", "$.verdict", "Acceptance packet validation requires input packet verdict=PASS.", evidence = listOf(packet.verdict)))
        if (packet.packetStatus != requiredPacketStatus) findings.add(finding("ACCEPTANCE_PACKET_STATUS_MISMATCH", "candidate_reference_boundary", "CRITICAL", "$.packetStatus", "Acceptance packet status must remain candidate_reference_acceptance_candidate.", evidence = listOf(packet.packetStatus)))
        if (packet.nextAllowedDownstreamStep != requiredPacketNextStep) findings.add(finding("ACCEPTANCE_PACKET_NEXT_STEP_MISMATCH", "candidate_reference_boundary", "CRITICAL", "$.nextAllowedDownstreamStep", "Acceptance packet must enter validation from candidate_reference_acceptance_packet_validation.", evidence = listOf(packet.nextAllowedDownstreamStep)))
        if (packet.downstreamBoundary != "candidate_reference_acceptance_packet_boundary_only") findings.add(finding("DOWNSTREAM_BOUNDARY_MISMATCH", "candidate_reference_boundary", "CRITICAL", "$.downstreamBoundary", "Acceptance packet must preserve candidate-only boundary wording.", evidence = listOf(packet.downstreamBoundary)))
        if (packet.evidence.registryPacketValidationVerdict.trim().uppercase() != "PASS") findings.add(finding("SOURCE_REGISTRY_PACKET_VALIDATION_NOT_PASS", "source_linkage", "CRITICAL", "$.evidence.registryPacketValidationVerdict", "Acceptance packet evidence must carry PASS registry packet validation.", evidence = listOf(packet.evidence.registryPacketValidationVerdict)))
        if (packet.evidence.decisionIntakeVerdict.trim().uppercase() != "PASS") findings.add(finding("SOURCE_DECISION_INTAKE_NOT_PASS", "source_linkage", "CRITICAL", "$.evidence.decisionIntakeVerdict", "Acceptance packet evidence must carry PASS human decision intake.", evidence = listOf(packet.evidence.decisionIntakeVerdict)))
        if (!packet.evidence.decisionAllowedDownstream) findings.add(finding("SOURCE_DECISION_NOT_DOWNSTREAM_ALLOWED", "source_linkage", "CRITICAL", "$.evidence.decisionAllowedDownstream", "Acceptance packet evidence must carry decisionAllowedDownstream=true.", evidence = listOf(packet.evidence.decisionAllowedDownstream.toString())))
    }

    private fun validateSourceFields(packet: CandidateReferenceAcceptancePacket, packetFile: String, findings: MutableList<CandidateReferenceAcceptancePacketValidationFinding>): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Acceptance packet validation requires nonblank $label.")) else count += 1
        }

        requireNonBlank(packetFile, "$.inputs.candidateReferenceAcceptancePacketFile", "ACCEPTANCE_PACKET_FILE_MISSING", "acceptance packet file")
        requireNonBlank(packet.packetId, "$.packetId", "ACCEPTANCE_PACKET_ID_MISSING", "acceptance packet id")
        requireNonBlank(packet.registryPacketValidationReportFile, "$.registryPacketValidationReportFile", "REGISTRY_PACKET_VALIDATION_REPORT_FILE_MISSING", "registry packet validation report file")
        requireNonBlank(packet.humanAcceptanceDecisionIntakeFile, "$.humanAcceptanceDecisionIntakeFile", "HUMAN_ACCEPTANCE_DECISION_INTAKE_FILE_MISSING", "human acceptance decision intake file")
        requireNonBlank(packet.decisionId, "$.decisionId", "HUMAN_ACCEPTANCE_DECISION_ID_MISSING", "human acceptance decision id")
        requireNonBlank(packet.registryPacketFile, "$.registryPacketFile", "REGISTRY_PACKET_FILE_MISSING", "registry packet file")
        requireNonBlank(packet.registryPacketId, "$.registryPacketId", "REGISTRY_PACKET_ID_MISSING", "registry packet id")
        requireNonBlank(packet.registryPreflightFile, "$.registryPreflightFile", "REGISTRY_PREFLIGHT_FILE_MISSING", "registry preflight file")
        requireNonBlank(packet.registryPreflightId, "$.registryPreflightId", "REGISTRY_PREFLIGHT_ID_MISSING", "registry preflight id")
        requireNonBlank(packet.acceptedScriptPacketId, "$.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(packet.scriptPacketDraftFile, "$.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(packet.scriptPacketDraftContentSha256, "$.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        requireNonBlank(packet.referencePacketFile, "$.referencePacketFile", "REFERENCE_PACKET_FILE_MISSING", "candidate reference packet file")
        requireNonBlank(packet.referencePacketId, "$.referencePacketId", "REFERENCE_PACKET_ID_MISSING", "candidate reference packet id")
        requireNonBlank(packet.resultReviewPreflightId, "$.resultReviewPreflightId", "RESULT_REVIEW_PREFLIGHT_ID_MISSING", "result review preflight id")
        requireNonBlank(packet.resultReviewDecisionIntakeFile, "$.resultReviewDecisionIntakeFile", "RESULT_REVIEW_DECISION_INTAKE_FILE_MISSING", "result review decision intake file")
        requireNonBlank(packet.resultReviewDecisionId, "$.resultReviewDecisionId", "RESULT_REVIEW_DECISION_ID_MISSING", "result review decision id")
        requireNonBlank(packet.providerHandoffPacketValidationReportFile, "$.providerHandoffPacketValidationReportFile", "PROVIDER_HANDOFF_PACKET_VALIDATION_REPORT_FILE_MISSING", "provider handoff packet validation report file")
        requireNonBlank(packet.providerHandoffPacketId, "$.providerHandoffPacketId", "PROVIDER_HANDOFF_PACKET_ID_MISSING", "provider handoff packet id")
        requireNonBlank(packet.providerHandoffPreflightId, "$.providerHandoffPreflightId", "PROVIDER_HANDOFF_PREFLIGHT_ID_MISSING", "provider handoff preflight id")
        requireNonBlank(packet.providerHandoffDecisionId, "$.providerHandoffDecisionId", "PROVIDER_HANDOFF_DECISION_ID_MISSING", "provider handoff decision id")
        requireNonBlank(packet.externalResultIntakeFile, "$.externalResultIntakeFile", "EXTERNAL_RESULT_INTAKE_FILE_MISSING", "external result intake file")
        requireNonBlank(packet.resultId, "$.resultId", "RESULT_ID_MISSING", "external result id")
        requireNonBlank(packet.humanAcceptanceDecisionSource, "$.humanAcceptanceDecisionSource", "HUMAN_ACCEPTANCE_DECISION_SOURCE_MISSING", "human acceptance decision source")
        requireNonBlank(packet.sourceImageResultIdentityDigest, "$.sourceImageResultIdentityDigest", "IMAGE_RESULT_IDENTITY_DIGEST_MISSING", "image result identity digest")

        if (packet.scriptPacketDraftContentSha256.isNotBlank() && !sha256Regex.matches(packet.scriptPacketDraftContentSha256)) findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.scriptPacketDraftContentSha256", "Acceptance packet validation requires lowercase 64-char SHA-256.", evidence = listOf(packet.scriptPacketDraftContentSha256)))
        if (packet.evidence.scriptPacketDraftContentSha256.isBlank()) {
            findings.add(finding("EVIDENCE_SCRIPT_PACKET_DRAFT_HASH_MISSING", "source_linkage", "CRITICAL", "$.evidence.scriptPacketDraftContentSha256", "Acceptance packet evidence must carry source script packet draft hash."))
        } else if (packet.evidence.scriptPacketDraftContentSha256 != packet.scriptPacketDraftContentSha256) {
            findings.add(finding("SOURCE_SCRIPT_PACKET_DRAFT_HASH_MISMATCH", "source_linkage", "CRITICAL", "$.evidence.scriptPacketDraftContentSha256", "Acceptance packet source hash evidence must match packet hash.", evidence = listOf("packet=${packet.scriptPacketDraftContentSha256}", "evidence=${packet.evidence.scriptPacketDraftContentSha256}")))
        }
        validateHumanSource(packet, findings)
        return count
    }

    private fun validateHumanSource(packet: CandidateReferenceAcceptancePacket, findings: MutableList<CandidateReferenceAcceptancePacketValidationFinding>) {
        val source = packet.humanAcceptanceDecisionSource.trim()
        if (source.isBlank()) return
        if (disallowedAcceptanceSourceRegex.containsMatchIn(source) || source !in allowedHumanSources) {
            findings.add(finding("HUMAN_ACCEPTANCE_SOURCE_NOT_ALLOWED", "human_boundary", "CRITICAL", "$.humanAcceptanceDecisionSource", "Acceptance packet must trace to human/user/01_control, not auto/model/generated/provider/system-derived acceptance.", evidence = listOf(source)))
        }
        val evidenceSource = packet.evidence.humanAcceptanceDecisionSource.trim()
        if (evidenceSource.isBlank()) {
            findings.add(finding("EVIDENCE_HUMAN_ACCEPTANCE_SOURCE_MISSING", "human_boundary", "CRITICAL", "$.evidence.humanAcceptanceDecisionSource", "Acceptance packet evidence must carry the human acceptance source."))
        } else if (evidenceSource != source) {
            findings.add(finding("HUMAN_ACCEPTANCE_SOURCE_MISMATCH", "human_boundary", "CRITICAL", "$.evidence.humanAcceptanceDecisionSource", "Acceptance packet human source must match source evidence.", evidence = listOf("packet=$source", "evidence=$evidenceSource")))
        }
    }

    private fun validateImageResults(packet: CandidateReferenceAcceptancePacket, findings: MutableList<CandidateReferenceAcceptancePacketValidationFinding>): Int {
        if (packet.imageResults.isEmpty()) {
            findings.add(finding("IMAGE_RESULTS_MISSING", "source_linkage", "CRITICAL", "$.imageResults", "Acceptance packet validation requires image result entries."))
            return 0
        }
        val ids = packet.imageResults.map { it.resultItemId.trim() }
        val duplicates = ids.groupingBy { it }.eachCount().filter { it.key.isNotBlank() && it.value > 1 }.keys
        if (duplicates.isNotEmpty()) findings.add(finding("IMAGE_RESULT_IDS_NOT_UNIQUE", "source_linkage", "CRITICAL", "$.imageResults", "Acceptance packet image result ids must be unique.", evidence = duplicates.toList()))
        packet.imageResults.forEachIndexed { index, item ->
            requireItemField(item.resultItemId, "$.imageResults[$index].resultItemId", "IMAGE_RESULT_ID_MISSING", "image result id", findings)
            requireItemField(item.promptItemId, "$.imageResults[$index].promptItemId", "PROMPT_ITEM_ID_MISSING", "prompt item id", findings)
            requireItemField(item.sourceCandidateId, "$.imageResults[$index].sourceCandidateId", "SOURCE_CANDIDATE_ID_MISSING", "source candidate id", findings)
            requireItemField(item.assetType, "$.imageResults[$index].assetType", "ASSET_TYPE_MISSING", "asset type", findings)
            if (item.localImagePath.isBlank() && item.metadataOnlyReference.isBlank()) findings.add(finding("IMAGE_RESULT_REFERENCE_MISSING", "source_linkage", "CRITICAL", "$.imageResults[$index]", "Acceptance packet image result requires localImagePath or metadataOnlyReference."))
            requireItemField(item.sourceFileSha256, "$.imageResults[$index].sourceFileSha256", "SOURCE_FILE_SHA256_MISSING", "source file SHA-256", findings)
            if (item.sourceFileSha256.isNotBlank() && !sha256Regex.matches(item.sourceFileSha256.trim().lowercase())) findings.add(finding("SOURCE_FILE_SHA256_INVALID", "source_linkage", "CRITICAL", "$.imageResults[$index].sourceFileSha256", "Image result sourceFileSha256 must be lowercase 64-char SHA-256.", evidence = listOf(item.sourceFileSha256)))
        }
        val actualDigest = imageResultIdentityDigest(packet.imageResults)
        if (packet.sourceImageResultIdentityDigest.isNotBlank() && packet.sourceImageResultIdentityDigest != actualDigest) {
            findings.add(finding("IMAGE_RESULT_IDENTITY_DIGEST_MISMATCH", "source_linkage", "CRITICAL", "$.sourceImageResultIdentityDigest", "Acceptance packet image result identity digest must match packet imageResults.", evidence = listOf("packet=${packet.sourceImageResultIdentityDigest}", "actual=$actualDigest")))
        }
        if (packet.evidence.imageResultIdentityDigest.isBlank()) {
            findings.add(finding("EVIDENCE_IMAGE_RESULT_IDENTITY_DIGEST_MISSING", "source_linkage", "CRITICAL", "$.evidence.imageResultIdentityDigest", "Acceptance packet evidence must carry image result identity digest."))
        } else if (packet.evidence.imageResultIdentityDigest != packet.sourceImageResultIdentityDigest) {
            findings.add(finding("EVIDENCE_IMAGE_RESULT_IDENTITY_DIGEST_MISMATCH", "source_linkage", "CRITICAL", "$.evidence.imageResultIdentityDigest", "Acceptance packet evidence image result identity digest must match packet digest.", evidence = listOf("packet=${packet.sourceImageResultIdentityDigest}", "evidence=${packet.evidence.imageResultIdentityDigest}")))
        }
        return packet.imageResults.size
    }

    private fun requireItemField(value: String, path: String, ruleId: String, label: String, findings: MutableList<CandidateReferenceAcceptancePacketValidationFinding>) {
        if (value.isBlank()) findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Acceptance packet validation requires nonblank $label."))
    }

    private fun validateForbiddenRemoteOrProviderFields(results: List<ImageCandidateExternalResultItem>, findings: MutableList<CandidateReferenceAcceptancePacketValidationFinding>): Int {
        var count = 0
        results.forEachIndexed { index, item ->
            listOf(
                Triple(item.providerJobId, "providerJobId", "PROVIDER_JOB_ID_FORBIDDEN"),
                Triple(item.apiResponse, "apiResponse", "API_RESPONSE_FORBIDDEN"),
                Triple(item.remoteUrl, "remoteUrl", "REMOTE_URL_FORBIDDEN"),
                Triple(item.generatedStatus, "generatedStatus", "GENERATED_STATUS_FORBIDDEN"),
            ).forEach { (value, field, ruleId) ->
                if (value.isNotBlank()) {
                    count += 1
                    findings.add(finding(ruleId, "safety_boundary", "CRITICAL", "$.imageResults[$index].$field", "Acceptance packet validation cannot carry provider jobs, API responses, remote URLs, or generated status.", evidence = listOf(value)))
                }
            }
            if (item.localImagePath.isNotBlank() && urlRegex.containsMatchIn(item.localImagePath.trim())) {
                count += 1
                findings.add(finding("REMOTE_IMAGE_PATH_FORBIDDEN", "safety_boundary", "CRITICAL", "$.imageResults[$index].localImagePath", "Acceptance packet validation only allows controlled local image paths.", evidence = listOf(item.localImagePath)))
            }
            if (item.metadataOnlyReference.isNotBlank() && (embeddedUrlRegex.containsMatchIn(item.metadataOnlyReference) || providerReferenceRegex.containsMatchIn(item.metadataOnlyReference))) {
                count += 1
                findings.add(finding("REMOTE_METADATA_REFERENCE_FORBIDDEN", "safety_boundary", "CRITICAL", "$.imageResults[$index].metadataOnlyReference", "Acceptance packet validation cannot carry embedded remote URLs, provider jobs, or API response references.", evidence = listOf(item.metadataOnlyReference)))
            }
        }
        return count
    }

    private fun scanText(
        packet: CandidateReferenceAcceptancePacket,
        patterns: List<Regex>,
        ruleId: String,
        category: String,
        findings: MutableList<CandidateReferenceAcceptancePacketValidationFinding>,
    ): Int {
        var count = 0
        val fields = mutableListOf(
            TextField("$.packetStatus", packet.packetStatus),
            TextField("$.downstreamBoundary", packet.downstreamBoundary),
            TextField("$.blockedReason", packet.blockedReason),
            TextField("$.registryPacketValidationReportFile", packet.registryPacketValidationReportFile),
            TextField("$.humanAcceptanceDecisionIntakeFile", packet.humanAcceptanceDecisionIntakeFile),
            TextField("$.humanAcceptanceDecisionSource", packet.humanAcceptanceDecisionSource),
            TextField("$.registryPacketFile", packet.registryPacketFile),
            TextField("$.scriptPacketDraftFile", packet.scriptPacketDraftFile),
            TextField("$.referencePacketFile", packet.referencePacketFile),
            TextField("$.resultReviewDecisionIntakeFile", packet.resultReviewDecisionIntakeFile),
            TextField("$.providerHandoffPacketValidationReportFile", packet.providerHandoffPacketValidationReportFile),
            TextField("$.externalResultIntakeFile", packet.externalResultIntakeFile),
        )
        packet.boundaryChecklist.forEachIndexed { index, value -> fields += TextField("$.boundaryChecklist[$index]", value) }
        packet.findings.forEachIndexed { index, finding ->
            fields += TextField("$.findings[$index].message", finding.message)
            fields += TextField("$.findings[$index].suggestedFix", finding.suggestedFix)
            finding.evidence.forEachIndexed { evidenceIndex, evidence -> fields += TextField("$.findings[$index].evidence[$evidenceIndex]", evidence) }
        }
        packet.imageResults.forEachIndexed { index, item ->
            fields += TextField("$.imageResults[$index].resultItemId", item.resultItemId)
            fields += TextField("$.imageResults[$index].promptItemId", item.promptItemId)
            fields += TextField("$.imageResults[$index].sourceCandidateId", item.sourceCandidateId)
            fields += TextField("$.imageResults[$index].assetType", item.assetType)
            fields += TextField("$.imageResults[$index].candidateStatus", item.candidateStatus)
            fields += TextField("$.imageResults[$index].localImagePath", item.localImagePath)
            fields += TextField("$.imageResults[$index].metadataOnlyReference", item.metadataOnlyReference)
            fields += TextField("$.imageResults[$index].notes", item.notes)
            item.doNotShowChecklist.forEachIndexed { itemIndex, value -> fields += TextField("$.imageResults[$index].doNotShowChecklist[$itemIndex]", value) }
            item.driftReviewChecklist.forEachIndexed { itemIndex, value -> fields += TextField("$.imageResults[$index].driftReviewChecklist[$itemIndex]", value) }
            item.residualRisks.forEachIndexed { itemIndex, value -> fields += TextField("$.imageResults[$index].residualRisks[$itemIndex]", value) }
        }
        fields.filter { it.value.isNotBlank() }.forEach { field ->
            patterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings.add(finding(ruleId, category, "CRITICAL", field.path, "Acceptance packet validation cannot carry approval, registered, lock, final, generated, provider, canon, or runtime claims.", evidence = listOf(field.value)))
                }
            }
        }
        return count
    }

    private fun imageResultIdentityDigest(results: List<ImageCandidateExternalResultItem>): String {
        val canonical = results
            .sortedBy { it.resultItemId.trim() }
            .joinToString("\n") { item ->
                listOf(
                    item.resultItemId.trim(),
                    item.promptItemId.trim(),
                    item.sourceCandidateId.trim(),
                    item.assetType.trim(),
                    normalizePath(item.localImagePath),
                    item.metadataOnlyReference.trim(),
                    item.sourceFileSha256.trim().lowercase(),
                ).joinToString("\u001F")
            }
        return sha256(canonical)
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    private fun normalizePath(value: String): String = value.trim().replace('\\', '/')

    private fun finding(ruleId: String, category: String, severity: String, path: String, message: String, evidence: List<String> = emptyList(), suggestedFix: String = ""): CandidateReferenceAcceptancePacketValidationFinding =
        CandidateReferenceAcceptancePacketValidationFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private data class TextField(val path: String, val value: String)
}
