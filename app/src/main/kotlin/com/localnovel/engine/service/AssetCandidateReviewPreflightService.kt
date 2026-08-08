package com.localnovel.engine.service

import com.localnovel.engine.model.AssetCandidateDraftItem
import com.localnovel.engine.model.AssetCandidateDraftValidationReport
import com.localnovel.engine.model.AssetCandidateReviewPreflightEvidence
import com.localnovel.engine.model.AssetCandidateReviewPreflightFinding
import com.localnovel.engine.model.AssetCandidateReviewPreflightReport

class AssetCandidateReviewPreflightService {
    private val requiredValidationNextStep = "asset_candidate_review_preflight"
    private val nextAllowedDownstreamStep = "asset_candidate_human_review_ready"
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
    private val allowedStatuses = setOf("candidate", "candidate_only", "draft_candidate")
    private val forbiddenStatusPatterns = listOf(
        Regex("""(?i)\breview_ready\b"""),
        Regex("""(?i)\bapproved\b"""),
        Regex("""(?i)\blocked\b"""),
        Regex("""(?i)\bfinal\b"""),
        Regex("""(?i)\bproduction[-_\s]?ready\b"""),
        Regex("""(?i)\bapproved_reference\b"""),
        Regex("""(?i)\bproduction_locked\b"""),
        Regex("\u6838\u51c6|\u6279\u51c6|\u9396\u5b9a"),
    )
    private val forbiddenClaimPatterns = listOf(
        Regex("""(?i)\bimage\s+generated\b"""),
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
        assetCandidateDraftValidation: AssetCandidateDraftValidationReport,
        preflightId: String = "",
        assetCandidateDraftValidationReportFile: String = "",
    ): AssetCandidateReviewPreflightReport {
        val findings = mutableListOf<AssetCandidateReviewPreflightFinding>()
        validateValidationReportState(assetCandidateDraftValidation, findings)
        val sourceFieldCount = validateSourceFields(assetCandidateDraftValidation, assetCandidateDraftValidationReportFile, findings)
        validateCandidateItems(assetCandidateDraftValidation.candidateItems, findings)
        val forbiddenStatusCount = validateCandidateStatusText(assetCandidateDraftValidation.candidateItems, findings)
        val forbiddenClaimCount = validateForbiddenClaims(assetCandidateDraftValidation, findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) {
            "FAIL"
        } else {
            "PASS"
        }
        val blockedReason = if (verdict == "FAIL") {
            findings
                .filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }
                .joinToString("; ") { it.ruleId }
        } else {
            ""
        }
        val resolvedPreflightId = preflightId.ifBlank {
            assetCandidateDraftValidation.assetCandidateDraftId
                .takeIf { it.isNotBlank() }
                ?.let { "$it-review-preflight" }
                .orEmpty()
        }

        return AssetCandidateReviewPreflightReport(
            verdict = verdict,
            preflightId = resolvedPreflightId,
            assetCandidateDraftValidationReportFile = assetCandidateDraftValidationReportFile,
            assetCandidateDraftFile = assetCandidateDraftValidation.assetCandidateDraftFile,
            assetCandidateDraftId = assetCandidateDraftValidation.assetCandidateDraftId,
            assetCandidateBriefFile = assetCandidateDraftValidation.assetCandidateBriefFile,
            assetCandidateBriefId = assetCandidateDraftValidation.assetCandidateBriefId,
            assetCandidatePreflightFile = assetCandidateDraftValidation.assetCandidatePreflightFile,
            assetCandidatePreflightId = assetCandidateDraftValidation.assetCandidatePreflightId,
            humanReviewDecisionIntakeFile = assetCandidateDraftValidation.humanReviewDecisionIntakeFile,
            humanReviewDecisionId = assetCandidateDraftValidation.humanReviewDecisionId,
            storyboardPacketReviewFile = assetCandidateDraftValidation.storyboardPacketReviewFile,
            storyboardPacketReviewId = assetCandidateDraftValidation.storyboardPacketReviewId,
            acceptedScriptPacketId = assetCandidateDraftValidation.acceptedScriptPacketId,
            scriptPacketDraftFile = assetCandidateDraftValidation.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = assetCandidateDraftValidation.scriptPacketDraftContentSha256,
            candidateItemCount = if (verdict == "FAIL") 0 else assetCandidateDraftValidation.candidateItems.size,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = AssetCandidateReviewPreflightEvidence(
                draftValidationVerdict = assetCandidateDraftValidation.verdict,
                draftValidationNextAllowedDownstreamStep = assetCandidateDraftValidation.nextAllowedDownstreamStep,
                sourceFieldCount = sourceFieldCount,
                candidateItemCount = assetCandidateDraftValidation.candidateItems.size,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
            ),
        )
    }

    private fun validateValidationReportState(
        report: AssetCandidateDraftValidationReport,
        findings: MutableList<AssetCandidateReviewPreflightFinding>,
    ) {
        if (report.verdict.trim().uppercase() != "PASS") {
            findings.add(
                finding(
                    ruleId = "ASSET_CANDIDATE_DRAFT_VALIDATION_NOT_PASS",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.assetCandidateDraftValidation.verdict",
                    message = "Asset candidate review preflight requires a PASS draft validation report.",
                    evidence = listOf(report.verdict),
                    suggestedFix = "Use the PASS asset candidate draft validation report.",
                ),
            )
        }
        if (report.nextAllowedDownstreamStep != requiredValidationNextStep) {
            findings.add(
                finding(
                    ruleId = "ASSET_CANDIDATE_DRAFT_VALIDATION_NEXT_STEP_MISMATCH",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.assetCandidateDraftValidation.nextAllowedDownstreamStep",
                    message = "Asset candidate review preflight requires nextAllowedDownstreamStep=asset_candidate_review_preflight.",
                    evidence = listOf(report.nextAllowedDownstreamStep),
                    suggestedFix = "Use the validation report that explicitly allows asset_candidate_review_preflight.",
                ),
            )
        }
    }

    private fun validateSourceFields(
        report: AssetCandidateDraftValidationReport,
        reportFile: String,
        findings: MutableList<AssetCandidateReviewPreflightFinding>,
    ): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) {
                findings.add(
                    finding(
                        ruleId = ruleId,
                        category = "source_linkage",
                        severity = "CRITICAL",
                        path = path,
                        message = "Asset candidate review preflight requires nonblank $label.",
                        suggestedFix = "Use the complete asset candidate draft validation report.",
                    ),
                )
            } else {
                count += 1
            }
        }
        requireNonBlank(reportFile, "$.inputs.assetCandidateDraftValidationReportFile", "ASSET_CANDIDATE_DRAFT_VALIDATION_REPORT_FILE_MISSING", "asset candidate draft validation report file")
        requireNonBlank(report.assetCandidateDraftFile, "$.assetCandidateDraftValidation.assetCandidateDraftFile", "ASSET_CANDIDATE_DRAFT_FILE_MISSING", "asset candidate draft file")
        requireNonBlank(report.assetCandidateDraftId, "$.assetCandidateDraftValidation.assetCandidateDraftId", "ASSET_CANDIDATE_DRAFT_ID_MISSING", "asset candidate draft id")
        requireNonBlank(report.assetCandidateBriefFile, "$.assetCandidateDraftValidation.assetCandidateBriefFile", "ASSET_CANDIDATE_BRIEF_FILE_MISSING", "asset candidate brief file")
        requireNonBlank(report.assetCandidateBriefId, "$.assetCandidateDraftValidation.assetCandidateBriefId", "ASSET_CANDIDATE_BRIEF_ID_MISSING", "asset candidate brief id")
        requireNonBlank(report.assetCandidatePreflightFile, "$.assetCandidateDraftValidation.assetCandidatePreflightFile", "ASSET_CANDIDATE_PREFLIGHT_FILE_MISSING", "asset candidate preflight file")
        requireNonBlank(report.assetCandidatePreflightId, "$.assetCandidateDraftValidation.assetCandidatePreflightId", "ASSET_CANDIDATE_PREFLIGHT_ID_MISSING", "asset candidate preflight id")
        requireNonBlank(report.humanReviewDecisionIntakeFile, "$.assetCandidateDraftValidation.humanReviewDecisionIntakeFile", "HUMAN_REVIEW_DECISION_INTAKE_FILE_MISSING", "human review decision intake file")
        requireNonBlank(report.humanReviewDecisionId, "$.assetCandidateDraftValidation.humanReviewDecisionId", "HUMAN_REVIEW_DECISION_ID_MISSING", "human review decision id")
        requireNonBlank(report.storyboardPacketReviewFile, "$.assetCandidateDraftValidation.storyboardPacketReviewFile", "STORYBOARD_PACKET_REVIEW_FILE_MISSING", "storyboard packet review file")
        requireNonBlank(report.storyboardPacketReviewId, "$.assetCandidateDraftValidation.storyboardPacketReviewId", "STORYBOARD_PACKET_REVIEW_ID_MISSING", "storyboard packet review id")
        requireNonBlank(report.acceptedScriptPacketId, "$.assetCandidateDraftValidation.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(report.scriptPacketDraftFile, "$.assetCandidateDraftValidation.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(report.scriptPacketDraftContentSha256, "$.assetCandidateDraftValidation.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        if (report.scriptPacketDraftContentSha256.isNotBlank() &&
            !sha256Regex.matches(report.scriptPacketDraftContentSha256.trim().lowercase())
        ) {
            findings.add(
                finding(
                    ruleId = "SCRIPT_PACKET_DRAFT_HASH_INVALID",
                    category = "source_linkage",
                    severity = "CRITICAL",
                    path = "$.assetCandidateDraftValidation.scriptPacketDraftContentSha256",
                    message = "Asset candidate review preflight requires a valid lowercase SHA-256 script packet draft hash.",
                    evidence = listOf(report.scriptPacketDraftContentSha256),
                    suggestedFix = "Use the source hash carried by the draft validation report.",
                ),
            )
        }
        return count
    }

    private fun validateCandidateItems(
        items: List<AssetCandidateDraftItem>,
        findings: MutableList<AssetCandidateReviewPreflightFinding>,
    ) {
        if (items.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "ASSET_CANDIDATE_ITEMS_MISSING",
                    category = "asset_candidate_boundary",
                    severity = "CRITICAL",
                    path = "$.assetCandidateDraftValidation.candidateItems",
                    message = "Asset candidate review preflight requires at least one validated candidate item.",
                    suggestedFix = "Validate a complete candidate-only draft before review preflight.",
                ),
            )
            return
        }
        items.forEachIndexed { index, item ->
            if (item.candidateId.isBlank() || item.assetType.isBlank() || item.sourceRequirementId.isBlank() || item.requirement.isBlank()) {
                findings.add(
                    finding(
                        ruleId = "ASSET_CANDIDATE_ITEM_INCOMPLETE",
                        category = "asset_candidate_boundary",
                        severity = "CRITICAL",
                        path = "$.assetCandidateDraftValidation.candidateItems[$index]",
                        message = "Asset candidate review preflight requires complete candidate id, type, source requirement id, and requirement text.",
                        suggestedFix = "Use a complete draft validation report.",
                    ),
                )
            }
            if (item.publicDescription.isBlank() && item.visualReferenceRequirement.isBlank()) {
                findings.add(
                    finding(
                        ruleId = "ASSET_CANDIDATE_DESCRIPTION_MISSING",
                        category = "asset_candidate_boundary",
                        severity = "CRITICAL",
                        path = "$.assetCandidateDraftValidation.candidateItems[$index]",
                        message = "Asset candidate review preflight requires public description or visual reference requirement.",
                        suggestedFix = "Carry candidate-only public description or visual reference requirement.",
                    ),
                )
            }
            if (item.candidateStatus.trim().lowercase() !in allowedStatuses) {
                findings.add(
                    finding(
                        ruleId = "ASSET_CANDIDATE_STATUS_NOT_ALLOWED",
                        category = "asset_candidate_boundary",
                        severity = "CRITICAL",
                        path = "$.assetCandidateDraftValidation.candidateItems[$index].candidateStatus",
                        message = "Asset candidate review preflight status must remain candidate-only.",
                        evidence = listOf(item.candidateStatus),
                        suggestedFix = "Use candidate, candidate_only, or draft_candidate only.",
                    ),
                )
            }
        }
    }

    private fun validateCandidateStatusText(
        items: List<AssetCandidateDraftItem>,
        findings: MutableList<AssetCandidateReviewPreflightFinding>,
    ): Int {
        var count = 0
        items.forEachIndexed { index, item ->
            listOf(
                "$.assetCandidateDraftValidation.candidateItems[$index].candidateId" to item.candidateId,
                "$.assetCandidateDraftValidation.candidateItems[$index].candidateStatus" to item.candidateStatus,
                "$.assetCandidateDraftValidation.candidateItems[$index].requirement" to item.requirement,
                "$.assetCandidateDraftValidation.candidateItems[$index].publicDescription" to item.publicDescription,
                "$.assetCandidateDraftValidation.candidateItems[$index].visualReferenceRequirement" to item.visualReferenceRequirement,
            ).forEach { (path, value) ->
                forbiddenStatusPatterns.forEach { pattern ->
                    if (value.isNotBlank() && pattern.containsMatchIn(value)) {
                        count += 1
                        findings.add(
                            finding(
                                ruleId = "ASSET_CANDIDATE_STATUS_FORBIDDEN",
                                category = "asset_candidate_boundary",
                                severity = "CRITICAL",
                                path = path,
                                message = "Asset candidate review preflight forbids approved, locked, final, review-ready, production-ready, or production-locked status wording.",
                                evidence = listOf(value),
                                suggestedFix = "Keep candidate wording draft-only and unapproved.",
                            ),
                        )
                    }
                }
            }
        }
        return count
    }

    private fun validateForbiddenClaims(
        report: AssetCandidateDraftValidationReport,
        findings: MutableList<AssetCandidateReviewPreflightFinding>,
    ): Int {
        var count = 0
        collectTextFields(report).forEach { field ->
            forbiddenClaimPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings.add(
                        finding(
                            ruleId = "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN",
                            category = "safety_boundary",
                            severity = "CRITICAL",
                            path = field.path,
                            message = "Asset candidate review preflight forbids generated media, asset approval, production lock, canon application, or runtime scene creation claims.",
                            evidence = listOf(field.value),
                            suggestedFix = "Keep this preflight limited to human review preparation.",
                        ),
                    )
                }
            }
        }
        return count
    }

    private fun collectTextFields(report: AssetCandidateDraftValidationReport): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) {
            if (value.isNotBlank()) fields += TextField(path, value)
        }
        add("$.assetCandidateDraftValidation.downstreamBoundary", report.downstreamBoundary)
        add("$.assetCandidateDraftValidation.blockedReason", report.blockedReason)
        report.candidateItems.forEachIndexed { index, item ->
            add("$.assetCandidateDraftValidation.candidateItems[$index].candidateId", item.candidateId)
            add("$.assetCandidateDraftValidation.candidateItems[$index].assetType", item.assetType)
            add("$.assetCandidateDraftValidation.candidateItems[$index].candidateStatus", item.candidateStatus)
            add("$.assetCandidateDraftValidation.candidateItems[$index].requirement", item.requirement)
            add("$.assetCandidateDraftValidation.candidateItems[$index].publicDescription", item.publicDescription)
            add("$.assetCandidateDraftValidation.candidateItems[$index].visualReferenceRequirement", item.visualReferenceRequirement)
        }
        return fields
    }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): AssetCandidateReviewPreflightFinding =
        AssetCandidateReviewPreflightFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            path = path,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
        )

    private data class TextField(val path: String, val value: String)
}
