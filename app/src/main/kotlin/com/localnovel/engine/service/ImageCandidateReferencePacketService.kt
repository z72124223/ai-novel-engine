package com.localnovel.engine.service

import com.localnovel.engine.model.ImageCandidateExternalResultItem
import com.localnovel.engine.model.ImageCandidateReferencePacket
import com.localnovel.engine.model.ImageCandidateReferencePacketEvidence
import com.localnovel.engine.model.ImageCandidateReferencePacketFinding
import com.localnovel.engine.model.ImageCandidateResultReviewDecisionIntakeReport
import com.localnovel.engine.model.ImageCandidateResultReviewPreflightReport

class ImageCandidateReferencePacketService {
    private val requiredDecisionStatus = "ALLOW_CANDIDATE_REFERENCE_PACKET"
    private val requiredDecisionNextStep = "image_candidate_reference_packet"
    private val requiredPreflightNextStep = "image_candidate_result_human_review_ready"
    private val nextAllowedDownstreamStep = "candidate_reference_packet_validation"
    private val allowedPacketStatuses = setOf("candidate", "reference_candidate", "reviewed_candidate")
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

    fun create(
        resultReviewPreflight: ImageCandidateResultReviewPreflightReport,
        decisionIntake: ImageCandidateResultReviewDecisionIntakeReport,
        packetId: String = "",
        resultReviewPreflightFile: String = "",
        resultReviewDecisionIntakeFile: String = "",
    ): ImageCandidateReferencePacket {
        val findings = mutableListOf<ImageCandidateReferencePacketFinding>()
        validateUpstreams(resultReviewPreflight, decisionIntake, findings)
        val sourceCount =
            validateSourceBinding(resultReviewPreflight, decisionIntake, resultReviewPreflightFile, resultReviewDecisionIntakeFile, findings) +
                validateImageResultSourceBinding(resultReviewPreflight.imageResults, decisionIntake.imageResults, findings)
        val resolvedPacketId = packetId.ifBlank { decisionIntake.decisionId.takeIf { it.isNotBlank() }?.let { "$it-candidate-reference-packet" }.orEmpty() }
        validatePacketBasics(resolvedPacketId, "reference_candidate", decisionIntake.imageResults, findings)
        val forbiddenRemoteOrProviderCount = validateForbiddenRemoteOrProviderFields(decisionIntake.imageResults, findings)
        val forbiddenStatusCount = scanText("reference_candidate", decisionIntake.imageResults, forbiddenStatusPatterns, "CANDIDATE_REFERENCE_STATUS_FORBIDDEN", "candidate_reference_boundary", "Candidate reference packet cannot contain approved, locked, final, production-ready, approved-reference, or production-locked wording.", findings)
        val forbiddenClaimCount = scanText("reference_candidate", decisionIntake.imageResults, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", "Candidate reference packet cannot claim asset approval, production lock, canon application, or runtime scene creation.", findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId } else ""

        return ImageCandidateReferencePacket(
            verdict = verdict,
            packetId = resolvedPacketId,
            packetStatus = "reference_candidate",
            resultReviewPreflightFile = resultReviewPreflightFile,
            resultReviewDecisionIntakeFile = resultReviewDecisionIntakeFile,
            resultReviewPreflightId = resultReviewPreflight.preflightId,
            decisionId = decisionIntake.decisionId,
            externalResultIntakeFile = resultReviewPreflight.externalResultIntakeFile,
            resultId = resultReviewPreflight.resultId,
            providerHandoffPacketValidationReportFile = resultReviewPreflight.providerHandoffPacketValidationReportFile,
            providerHandoffPacketId = resultReviewPreflight.packetId,
            providerHandoffPreflightId = resultReviewPreflight.preflightSourceId,
            providerHandoffDecisionId = resultReviewPreflight.decisionId,
            acceptedScriptPacketId = resultReviewPreflight.acceptedScriptPacketId,
            scriptPacketDraftFile = resultReviewPreflight.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = resultReviewPreflight.scriptPacketDraftContentSha256,
            imageResults = if (verdict == "FAIL") emptyList() else decisionIntake.imageResults,
            reviewChecklistSummary = if (verdict == "FAIL") emptyList() else listOf(
                "visual_drift_checked",
                "source_binding_checked",
                "do_not_show_checked",
                "prompt_safety_checked",
                "downstream_boundary_candidate_only",
                "no_approval_no_lock",
            ),
            reviewNotes = if (verdict == "FAIL") emptyList() else listOf("Candidate reference packet remains a reviewed candidate for later human acceptance workflow."),
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = ImageCandidateReferencePacketEvidence(
                resultReviewPreflightVerdict = resultReviewPreflight.verdict,
                decisionIntakeVerdict = decisionIntake.verdict,
                decisionAllowedDownstream = decisionIntake.decisionAllowedDownstream,
                sourceBindingFieldCount = sourceCount,
                imageResultCount = decisionIntake.imageResults.size,
                checklistSummaryCount = if (verdict == "FAIL") 0 else 6,
                forbiddenRemoteOrProviderFieldCount = forbiddenRemoteOrProviderCount,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
            ),
        )
    }

    private fun validateUpstreams(preflight: ImageCandidateResultReviewPreflightReport, intake: ImageCandidateResultReviewDecisionIntakeReport, findings: MutableList<ImageCandidateReferencePacketFinding>) {
        if (preflight.verdict.trim().uppercase() != "PASS") findings.add(finding("RESULT_REVIEW_PREFLIGHT_NOT_PASS", "upstream", "CRITICAL", "$.resultReviewPreflight.verdict", "Candidate reference packet requires a PASS result review preflight.", evidence = listOf(preflight.verdict), suggestedFix = "Use a PASS result review preflight."))
        if (preflight.nextAllowedDownstreamStep != requiredPreflightNextStep) findings.add(finding("RESULT_REVIEW_PREFLIGHT_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.resultReviewPreflight.nextAllowedDownstreamStep", "Candidate reference packet requires nextAllowedDownstreamStep=image_candidate_result_human_review_ready.", evidence = listOf(preflight.nextAllowedDownstreamStep), suggestedFix = "Use the correct preflight."))
        if (intake.verdict.trim().uppercase() != "PASS") findings.add(finding("RESULT_REVIEW_DECISION_INTAKE_NOT_PASS", "upstream", "CRITICAL", "$.decisionIntake.verdict", "Candidate reference packet requires a PASS result review decision intake.", evidence = listOf(intake.verdict), suggestedFix = "Use a PASS decision intake report."))
        if (!intake.decisionAllowedDownstream) findings.add(finding("DECISION_NOT_DOWNSTREAM_ALLOWED", "upstream", "CRITICAL", "$.decisionIntake.decisionAllowedDownstream", "Candidate reference packet requires decisionAllowedDownstream=true.", evidence = listOf(intake.decisionAllowedDownstream.toString()), suggestedFix = "Use an allow-candidate-reference-packet decision intake report."))
        if (intake.decisionStatus != requiredDecisionStatus) findings.add(finding("DECISION_STATUS_MISMATCH", "upstream", "CRITICAL", "$.decisionIntake.decisionStatus", "Candidate reference packet requires decisionStatus=ALLOW_CANDIDATE_REFERENCE_PACKET.", evidence = listOf(intake.decisionStatus), suggestedFix = "Use the allow status only."))
        if (intake.nextAllowedDownstreamStep != requiredDecisionNextStep) findings.add(finding("DECISION_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.decisionIntake.nextAllowedDownstreamStep", "Candidate reference packet requires nextAllowedDownstreamStep=image_candidate_reference_packet.", evidence = listOf(intake.nextAllowedDownstreamStep), suggestedFix = "Use the matching decision intake report."))
    }

    private fun validateSourceBinding(preflight: ImageCandidateResultReviewPreflightReport, intake: ImageCandidateResultReviewDecisionIntakeReport, preflightFile: String, intakeFile: String, findings: MutableList<ImageCandidateReferencePacketFinding>): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) { if (value.isBlank()) findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Candidate reference packet requires nonblank $label.", suggestedFix = "Use matching preflight and decision intake reports.")) else count += 1 }
        fun requireMatch(leftValue: String, rightValue: String, leftPath: String, rightPath: String, missingRuleId: String, mismatchRuleId: String, label: String, normalize: (String) -> String = { it.trim() }) {
            requireNonBlank(leftValue, leftPath, "${missingRuleId}_PREFLIGHT", "$label in preflight")
            requireNonBlank(rightValue, rightPath, missingRuleId, "$label in decision intake")
            val left = normalize(leftValue); val right = normalize(rightValue)
            if (left.isNotBlank() && right.isNotBlank() && left != right) findings.add(finding(mismatchRuleId, "source_linkage", "CRITICAL", rightPath, "Candidate reference packet source binding mismatch for $label.", evidence = listOf("preflight=$leftValue", "decisionIntake=$rightValue"), suggestedFix = "Use reports from the same source chain."))
        }
        requireNonBlank(preflightFile, "$.inputs.resultReviewPreflightFile", "RESULT_REVIEW_PREFLIGHT_FILE_MISSING", "result review preflight file")
        requireNonBlank(intakeFile, "$.inputs.resultReviewDecisionIntakeFile", "RESULT_REVIEW_DECISION_INTAKE_FILE_MISSING", "decision intake file")
        requireNonBlank(intake.decisionId, "$.decisionIntake.decisionId", "DECISION_ID_MISSING", "decision id")
        requireMatch(preflight.preflightId, intake.resultReviewPreflightId, "$.resultReviewPreflight.preflightId", "$.decisionIntake.resultReviewPreflightId", "RESULT_REVIEW_PREFLIGHT_ID_MISSING", "RESULT_REVIEW_PREFLIGHT_ID_MISMATCH", "result review preflight id")
        requireMatch(preflight.externalResultIntakeFile, intake.externalResultIntakeFile, "$.resultReviewPreflight.externalResultIntakeFile", "$.decisionIntake.externalResultIntakeFile", "EXTERNAL_RESULT_INTAKE_FILE_MISSING", "EXTERNAL_RESULT_INTAKE_FILE_MISMATCH", "external result intake file", ::normalizePath)
        requireMatch(preflight.resultId, intake.resultId, "$.resultReviewPreflight.resultId", "$.decisionIntake.resultId", "RESULT_ID_MISSING", "RESULT_ID_MISMATCH", "result id")
        requireMatch(preflight.providerHandoffPacketValidationReportFile, intake.providerHandoffPacketValidationReportFile, "$.resultReviewPreflight.providerHandoffPacketValidationReportFile", "$.decisionIntake.providerHandoffPacketValidationReportFile", "PROVIDER_HANDOFF_PACKET_VALIDATION_REPORT_FILE_MISSING", "PROVIDER_HANDOFF_PACKET_VALIDATION_REPORT_FILE_MISMATCH", "provider handoff packet validation report file", ::normalizePath)
        requireMatch(preflight.packetId, intake.packetId, "$.resultReviewPreflight.packetId", "$.decisionIntake.packetId", "PACKET_ID_MISSING", "PACKET_ID_MISMATCH", "provider handoff packet id")
        requireMatch(preflight.preflightSourceId, intake.preflightSourceId, "$.resultReviewPreflight.preflightSourceId", "$.decisionIntake.preflightSourceId", "PROVIDER_PREFLIGHT_ID_MISSING", "PROVIDER_PREFLIGHT_ID_MISMATCH", "provider handoff preflight id")
        requireMatch(preflight.decisionId, intake.providerDecisionId, "$.resultReviewPreflight.decisionId", "$.decisionIntake.providerDecisionId", "PROVIDER_DECISION_ID_MISSING", "PROVIDER_DECISION_ID_MISMATCH", "provider handoff decision id")
        requireMatch(preflight.acceptedScriptPacketId, intake.acceptedScriptPacketId, "$.resultReviewPreflight.acceptedScriptPacketId", "$.decisionIntake.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "ACCEPTED_SCRIPT_PACKET_ID_MISMATCH", "accepted script packet id")
        requireMatch(preflight.scriptPacketDraftFile, intake.scriptPacketDraftFile, "$.resultReviewPreflight.scriptPacketDraftFile", "$.decisionIntake.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "SCRIPT_PACKET_DRAFT_FILE_MISMATCH", "script packet draft file", ::normalizePath)
        requireMatch(preflight.scriptPacketDraftContentSha256, intake.scriptPacketDraftContentSha256, "$.resultReviewPreflight.scriptPacketDraftContentSha256", "$.decisionIntake.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "SCRIPT_PACKET_DRAFT_HASH_MISMATCH", "script packet draft SHA-256", { it.trim().lowercase() })
        return count
    }

    private fun validateImageResultSourceBinding(
        preflightResults: List<ImageCandidateExternalResultItem>,
        intakeResults: List<ImageCandidateExternalResultItem>,
        findings: MutableList<ImageCandidateReferencePacketFinding>,
    ): Int {
        var count = 0
        val preflightIds = preflightResults.map { it.resultItemId.trim() }
        val intakeIds = intakeResults.map { it.resultItemId.trim() }
        if (preflightIds.size != preflightIds.toSet().size) {
            findings.add(finding("IMAGE_RESULT_SET_MISMATCH", "source_linkage", "CRITICAL", "$.resultReviewPreflight.imageResults", "Result review preflight image result ids must be unique.", evidence = preflightIds.filter { it.isNotBlank() }, suggestedFix = "Use a preflight report with unique candidate image result ids."))
        }
        if (intakeIds.size != intakeIds.toSet().size) {
            findings.add(finding("IMAGE_RESULT_SET_MISMATCH", "source_linkage", "CRITICAL", "$.decisionIntake.imageResults", "Decision intake image result ids must be unique.", evidence = intakeIds.filter { it.isNotBlank() }, suggestedFix = "Use a decision intake report carrying the reviewed preflight image result ids exactly once."))
        }
        if (preflightIds.toSet() != intakeIds.toSet()) {
            findings.add(finding("IMAGE_RESULT_SET_MISMATCH", "source_linkage", "CRITICAL", "$.decisionIntake.imageResults", "Decision intake image result id set must match the result review preflight image result id set.", evidence = listOf("preflight=${preflightIds.joinToString(",")}", "decisionIntake=${intakeIds.joinToString(",")}"), suggestedFix = "Use the decision intake generated from the same result review preflight."))
        }
        val preflightById = preflightResults.associateBy { it.resultItemId.trim() }
        intakeResults.forEachIndexed { index, intake ->
            val resultId = intake.resultItemId.trim()
            val preflight = preflightById[resultId]
            if (resultId.isBlank() || preflight == null) {
                findings.add(finding("IMAGE_RESULT_NOT_IN_PREFLIGHT", "source_linkage", "CRITICAL", "$.decisionIntake.imageResults[$index].resultItemId", "Decision intake image result must exist in the result review preflight.", evidence = listOf(intake.resultItemId), suggestedFix = "Do not replace reviewed image result items in the decision intake report."))
                return@forEachIndexed
            }
            count += 1
            compareImageResultField(preflight.promptItemId, intake.promptItemId, "$.decisionIntake.imageResults[$index].promptItemId", "promptItemId", findings)
            compareImageResultField(preflight.sourceCandidateId, intake.sourceCandidateId, "$.decisionIntake.imageResults[$index].sourceCandidateId", "sourceCandidateId", findings)
            compareImageResultField(preflight.assetType, intake.assetType, "$.decisionIntake.imageResults[$index].assetType", "assetType", findings)
            compareImageResultField(preflight.candidateStatus, intake.candidateStatus, "$.decisionIntake.imageResults[$index].candidateStatus", "candidateStatus", findings)
            compareImageResultField(preflight.localImagePath, intake.localImagePath, "$.decisionIntake.imageResults[$index].localImagePath", "localImagePath", findings, ::normalizePath)
            compareImageResultField(preflight.metadataOnlyReference, intake.metadataOnlyReference, "$.decisionIntake.imageResults[$index].metadataOnlyReference", "metadataOnlyReference", findings)
            compareImageResultField(preflight.sourceFileSha256, intake.sourceFileSha256, "$.decisionIntake.imageResults[$index].sourceFileSha256", "sourceFileSha256", findings, { it.trim().lowercase() })
            compareImageResultList(preflight.doNotShowChecklist, intake.doNotShowChecklist, "$.decisionIntake.imageResults[$index].doNotShowChecklist", "doNotShowChecklist", findings)
            compareImageResultList(preflight.driftReviewChecklist, intake.driftReviewChecklist, "$.decisionIntake.imageResults[$index].driftReviewChecklist", "driftReviewChecklist", findings)
            compareImageResultList(preflight.residualRisks, intake.residualRisks, "$.decisionIntake.imageResults[$index].residualRisks", "residualRisks", findings)
        }
        return count
    }

    private fun compareImageResultField(
        preflightValue: String,
        intakeValue: String,
        path: String,
        label: String,
        findings: MutableList<ImageCandidateReferencePacketFinding>,
        normalize: (String) -> String = { it.trim() },
    ) {
        if (normalize(preflightValue) != normalize(intakeValue)) {
            findings.add(finding("IMAGE_RESULT_SOURCE_MISMATCH", "source_linkage", "CRITICAL", path, "Decision intake image result $label must match the result review preflight.", evidence = listOf("preflight=$preflightValue", "decisionIntake=$intakeValue"), suggestedFix = "Use the image result item carried by the result review preflight."))
        }
    }

    private fun compareImageResultList(
        preflightValue: List<String>,
        intakeValue: List<String>,
        path: String,
        label: String,
        findings: MutableList<ImageCandidateReferencePacketFinding>,
    ) {
        val left = preflightValue.map { it.trim() }
        val right = intakeValue.map { it.trim() }
        if (left != right) {
            findings.add(finding("IMAGE_RESULT_SOURCE_MISMATCH", "source_linkage", "CRITICAL", path, "Decision intake image result $label must match the result review preflight.", evidence = listOf("preflight=${left.joinToString("|")}", "decisionIntake=${right.joinToString("|")}"), suggestedFix = "Use the checklist/risk fields carried by the result review preflight."))
        }
    }

    private fun validatePacketBasics(packetId: String, status: String, results: List<ImageCandidateExternalResultItem>, findings: MutableList<ImageCandidateReferencePacketFinding>) {
        if (packetId.isBlank()) findings.add(finding("REFERENCE_PACKET_ID_MISSING", "candidate_reference_boundary", "CRITICAL", "$.candidateReferencePacket.packetId", "Candidate reference packet requires packet id.", suggestedFix = "Provide --packet or use decision id."))
        if (status.trim().lowercase() !in allowedPacketStatuses) findings.add(finding("REFERENCE_PACKET_STATUS_NOT_ALLOWED", "candidate_reference_boundary", "CRITICAL", "$.candidateReferencePacket.packetStatus", "Candidate reference packet status must remain candidate-only.", evidence = listOf(status), suggestedFix = "Use candidate, reference_candidate, or reviewed_candidate."))
        if (results.isEmpty()) findings.add(finding("IMAGE_RESULTS_MISSING", "candidate_reference_boundary", "CRITICAL", "$.candidateReferencePacket.imageResults", "Candidate reference packet requires candidate image results.", suggestedFix = "Use a PASS decision intake with candidate results."))
    }

    private fun validateForbiddenRemoteOrProviderFields(results: List<ImageCandidateExternalResultItem>, findings: MutableList<ImageCandidateReferencePacketFinding>): Int {
        var count = 0
        results.forEachIndexed { index, item ->
            listOf(Triple(item.providerJobId, "providerJobId", "PROVIDER_JOB_ID_FORBIDDEN"), Triple(item.apiResponse, "apiResponse", "API_RESPONSE_FORBIDDEN"), Triple(item.remoteUrl, "remoteUrl", "REMOTE_URL_FORBIDDEN"), Triple(item.generatedStatus, "generatedStatus", "GENERATED_STATUS_FORBIDDEN")).forEach { (value, field, ruleId) ->
                if (value.isNotBlank()) { count += 1; findings.add(finding(ruleId, "safety_boundary", "CRITICAL", "$.candidateReferencePacket.imageResults[$index].$field", "Candidate reference packet cannot carry provider job, API response, remote URL, or generated status.", evidence = listOf(value), suggestedFix = "Keep only local candidate references.")) }
            }
            if (item.localImagePath.isNotBlank() && urlRegex.containsMatchIn(item.localImagePath.trim())) { count += 1; findings.add(finding("REMOTE_IMAGE_PATH_FORBIDDEN", "safety_boundary", "CRITICAL", "$.candidateReferencePacket.imageResults[$index].localImagePath", "Candidate reference packet only allows local image paths.", evidence = listOf(item.localImagePath), suggestedFix = "Use a local path or metadata-only reference.")) }
            if (item.metadataOnlyReference.isNotBlank() && (embeddedUrlRegex.containsMatchIn(item.metadataOnlyReference) || providerReferenceRegex.containsMatchIn(item.metadataOnlyReference))) { count += 1; findings.add(finding("REMOTE_METADATA_REFERENCE_FORBIDDEN", "safety_boundary", "CRITICAL", "$.candidateReferencePacket.imageResults[$index].metadataOnlyReference", "Candidate reference packet cannot carry remote URLs, provider job references, or API response references.", evidence = listOf(item.metadataOnlyReference), suggestedFix = "Use local candidate metadata.")) }
        }
        return count
    }

    private fun scanText(status: String, results: List<ImageCandidateExternalResultItem>, patterns: List<Regex>, ruleId: String, category: String, message: String, findings: MutableList<ImageCandidateReferencePacketFinding>): Int {
        var count = 0
        val fields = mutableListOf(TextField("$.candidateReferencePacket.packetStatus", status))
        results.forEachIndexed { index, item ->
            fields += TextField("$.candidateReferencePacket.imageResults[$index].assetType", item.assetType)
            fields += TextField("$.candidateReferencePacket.imageResults[$index].candidateStatus", item.candidateStatus)
            fields += TextField("$.candidateReferencePacket.imageResults[$index].localImagePath", item.localImagePath)
            fields += TextField("$.candidateReferencePacket.imageResults[$index].metadataOnlyReference", item.metadataOnlyReference)
            fields += TextField("$.candidateReferencePacket.imageResults[$index].notes", item.notes)
            item.residualRisks.forEachIndexed { itemIndex, value -> fields += TextField("$.candidateReferencePacket.imageResults[$index].residualRisks[$itemIndex]", value) }
        }
        fields.filter { it.value.isNotBlank() }.forEach { field -> patterns.forEach { pattern -> if (pattern.containsMatchIn(field.value)) { count += 1; findings.add(finding(ruleId, category, "CRITICAL", field.path, message, evidence = listOf(field.value), suggestedFix = "Keep candidate reference packet unapproved and unlocked.")) } } }
        return count
    }

    private fun normalizePath(value: String): String = value.trim().replace('\\', '/')
    private fun finding(ruleId: String, category: String, severity: String, path: String, message: String, evidence: List<String> = emptyList(), suggestedFix: String = ""): ImageCandidateReferencePacketFinding =
        ImageCandidateReferencePacketFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
    private data class TextField(val path: String, val value: String)
}
