package com.localnovel.engine.service

import com.localnovel.engine.model.AssetCandidateDraft
import com.localnovel.engine.model.AssetCandidateDraftItem
import com.localnovel.engine.model.AssetCandidateDraftValidationEvidence
import com.localnovel.engine.model.AssetCandidateDraftValidationFinding
import com.localnovel.engine.model.AssetCandidateDraftValidationReport

class AssetCandidateDraftValidationService {
    private val requiredDraftNextStep = "asset_candidate_review_preflight"
    private val nextAllowedDownstreamStep = "asset_candidate_review_preflight"
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
        assetCandidateDraft: AssetCandidateDraft,
        assetCandidateDraftFile: String = "",
    ): AssetCandidateDraftValidationReport {
        val findings = mutableListOf<AssetCandidateDraftValidationFinding>()
        validateDraftState(assetCandidateDraft, findings)
        val sourceFieldCount = validateSourceFields(assetCandidateDraft, assetCandidateDraftFile, findings)
        validateCandidateItems(assetCandidateDraft.candidateItems, findings)
        val forbiddenStatusCount = validateCandidateStatusText(assetCandidateDraft, findings)
        val forbiddenClaimCount = validateForbiddenClaims(assetCandidateDraft, findings)

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

        return AssetCandidateDraftValidationReport(
            verdict = verdict,
            assetCandidateDraftFile = assetCandidateDraftFile,
            assetCandidateDraftId = assetCandidateDraft.draftId,
            assetCandidateBriefFile = assetCandidateDraft.assetCandidateBriefFile,
            assetCandidateBriefId = assetCandidateDraft.assetCandidateBriefId,
            assetCandidatePreflightFile = assetCandidateDraft.assetCandidatePreflightFile,
            assetCandidatePreflightId = assetCandidateDraft.assetCandidatePreflightId,
            humanReviewDecisionIntakeFile = assetCandidateDraft.humanReviewDecisionIntakeFile,
            humanReviewDecisionId = assetCandidateDraft.humanReviewDecisionId,
            storyboardPacketReviewFile = assetCandidateDraft.storyboardPacketReviewFile,
            storyboardPacketReviewId = assetCandidateDraft.storyboardPacketReviewId,
            acceptedScriptPacketId = assetCandidateDraft.acceptedScriptPacketId,
            scriptPacketDraftFile = assetCandidateDraft.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = assetCandidateDraft.scriptPacketDraftContentSha256,
            candidateItems = if (verdict == "FAIL") emptyList() else assetCandidateDraft.candidateItems,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = AssetCandidateDraftValidationEvidence(
                draftVerdict = assetCandidateDraft.verdict,
                draftNextAllowedDownstreamStep = assetCandidateDraft.nextAllowedDownstreamStep,
                sourceFieldCount = sourceFieldCount,
                candidateItemCount = assetCandidateDraft.candidateItems.size,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
            ),
        )
    }

    private fun validateDraftState(
        draft: AssetCandidateDraft,
        findings: MutableList<AssetCandidateDraftValidationFinding>,
    ) {
        if (draft.verdict.trim().uppercase() != "PASS") {
            findings.add(
                finding(
                    ruleId = "ASSET_CANDIDATE_DRAFT_NOT_PASS",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.assetCandidateDraft.verdict",
                    message = "Asset candidate draft validation requires a PASS draft.",
                    evidence = listOf(draft.verdict),
                    suggestedFix = "Use the PASS asset candidate draft.",
                ),
            )
        }
        if (draft.nextAllowedDownstreamStep != requiredDraftNextStep) {
            findings.add(
                finding(
                    ruleId = "ASSET_CANDIDATE_DRAFT_NEXT_STEP_MISMATCH",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.assetCandidateDraft.nextAllowedDownstreamStep",
                    message = "Asset candidate draft validation requires nextAllowedDownstreamStep=asset_candidate_review_preflight.",
                    evidence = listOf(draft.nextAllowedDownstreamStep),
                    suggestedFix = "Use the draft that explicitly allows asset_candidate_review_preflight.",
                ),
            )
        }
    }

    private fun validateSourceFields(
        draft: AssetCandidateDraft,
        draftFile: String,
        findings: MutableList<AssetCandidateDraftValidationFinding>,
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
                        message = "Asset candidate draft validation requires nonblank $label.",
                        suggestedFix = "Use the complete asset candidate draft JSON.",
                    ),
                )
            } else {
                count += 1
            }
        }
        requireNonBlank(draftFile, "$.inputs.assetCandidateDraftFile", "ASSET_CANDIDATE_DRAFT_FILE_MISSING", "asset candidate draft file")
        requireNonBlank(draft.draftId, "$.assetCandidateDraft.draftId", "ASSET_CANDIDATE_DRAFT_ID_MISSING", "asset candidate draft id")
        requireNonBlank(draft.assetCandidateBriefFile, "$.assetCandidateDraft.assetCandidateBriefFile", "ASSET_CANDIDATE_BRIEF_FILE_MISSING", "asset candidate brief file")
        requireNonBlank(draft.assetCandidateBriefId, "$.assetCandidateDraft.assetCandidateBriefId", "ASSET_CANDIDATE_BRIEF_ID_MISSING", "asset candidate brief id")
        requireNonBlank(draft.assetCandidatePreflightFile, "$.assetCandidateDraft.assetCandidatePreflightFile", "ASSET_CANDIDATE_PREFLIGHT_FILE_MISSING", "asset candidate preflight file")
        requireNonBlank(draft.assetCandidatePreflightId, "$.assetCandidateDraft.assetCandidatePreflightId", "ASSET_CANDIDATE_PREFLIGHT_ID_MISSING", "asset candidate preflight id")
        requireNonBlank(draft.humanReviewDecisionIntakeFile, "$.assetCandidateDraft.humanReviewDecisionIntakeFile", "HUMAN_REVIEW_DECISION_INTAKE_FILE_MISSING", "human review decision intake file")
        requireNonBlank(draft.humanReviewDecisionId, "$.assetCandidateDraft.humanReviewDecisionId", "HUMAN_REVIEW_DECISION_ID_MISSING", "human review decision id")
        requireNonBlank(draft.storyboardPacketReviewFile, "$.assetCandidateDraft.storyboardPacketReviewFile", "STORYBOARD_PACKET_REVIEW_FILE_MISSING", "storyboard packet review file")
        requireNonBlank(draft.storyboardPacketReviewId, "$.assetCandidateDraft.storyboardPacketReviewId", "STORYBOARD_PACKET_REVIEW_ID_MISSING", "storyboard packet review id")
        requireNonBlank(draft.acceptedScriptPacketId, "$.assetCandidateDraft.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(draft.scriptPacketDraftFile, "$.assetCandidateDraft.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(draft.scriptPacketDraftContentSha256, "$.assetCandidateDraft.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        if (draft.scriptPacketDraftContentSha256.isNotBlank() &&
            !sha256Regex.matches(draft.scriptPacketDraftContentSha256.trim().lowercase())
        ) {
            findings.add(
                finding(
                    ruleId = "SCRIPT_PACKET_DRAFT_HASH_INVALID",
                    category = "source_linkage",
                    severity = "CRITICAL",
                    path = "$.assetCandidateDraft.scriptPacketDraftContentSha256",
                    message = "Asset candidate draft validation requires a valid lowercase SHA-256 script packet draft hash.",
                    evidence = listOf(draft.scriptPacketDraftContentSha256),
                    suggestedFix = "Use the source hash carried by the asset candidate draft.",
                ),
            )
        }
        return count
    }

    private fun validateCandidateItems(
        items: List<AssetCandidateDraftItem>,
        findings: MutableList<AssetCandidateDraftValidationFinding>,
    ) {
        if (items.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "ASSET_CANDIDATE_ITEMS_MISSING",
                    category = "asset_candidate_boundary",
                    severity = "CRITICAL",
                    path = "$.assetCandidateDraft.candidateItems",
                    message = "Asset candidate draft validation requires at least one candidate item.",
                    suggestedFix = "Create a candidate-only draft before validation.",
                ),
            )
            return
        }
        items.forEachIndexed { index, item ->
            requireItemField(item.candidateId, index, "candidateId", "ASSET_CANDIDATE_ID_MISSING", findings)
            requireItemField(item.assetType, index, "assetType", "ASSET_CANDIDATE_TYPE_MISSING", findings)
            requireItemField(item.sourceRequirementId, index, "sourceRequirementId", "SOURCE_REQUIREMENT_ID_MISSING", findings)
            requireItemField(item.requirement, index, "requirement", "ASSET_CANDIDATE_REQUIREMENT_MISSING", findings)
            if (item.publicDescription.isBlank() && item.visualReferenceRequirement.isBlank()) {
                findings.add(
                    finding(
                        ruleId = "ASSET_CANDIDATE_DESCRIPTION_MISSING",
                        category = "asset_candidate_boundary",
                        severity = "CRITICAL",
                        path = "$.assetCandidateDraft.candidateItems[$index]",
                        message = "Asset candidate draft item requires public description or visual reference requirement.",
                        suggestedFix = "Carry candidate-only public description or visual reference requirement.",
                    ),
                )
            }
            val normalizedStatus = item.candidateStatus.trim().lowercase()
            if (normalizedStatus !in allowedStatuses) {
                findings.add(
                    finding(
                        ruleId = "ASSET_CANDIDATE_STATUS_NOT_ALLOWED",
                        category = "asset_candidate_boundary",
                        severity = "CRITICAL",
                        path = "$.assetCandidateDraft.candidateItems[$index].candidateStatus",
                        message = "Asset candidate draft item status must remain candidate-only.",
                        evidence = listOf(item.candidateStatus),
                        suggestedFix = "Use candidate, candidate_only, or draft_candidate only.",
                    ),
                )
            }
        }
    }

    private fun requireItemField(
        value: String,
        index: Int,
        field: String,
        ruleId: String,
        findings: MutableList<AssetCandidateDraftValidationFinding>,
    ) {
        if (value.isBlank()) {
            findings.add(
                finding(
                    ruleId = ruleId,
                    category = "asset_candidate_boundary",
                    severity = "CRITICAL",
                    path = "$.assetCandidateDraft.candidateItems[$index].$field",
                    message = "Asset candidate draft item requires nonblank $field.",
                    suggestedFix = "Use a complete candidate-only draft item.",
                ),
            )
        }
    }

    private fun validateCandidateStatusText(
        draft: AssetCandidateDraft,
        findings: MutableList<AssetCandidateDraftValidationFinding>,
    ): Int {
        var count = 0
        draft.candidateItems.forEachIndexed { index, item ->
            listOf(
                "$.assetCandidateDraft.candidateItems[$index].candidateId" to item.candidateId,
                "$.assetCandidateDraft.candidateItems[$index].candidateStatus" to item.candidateStatus,
                "$.assetCandidateDraft.candidateItems[$index].requirement" to item.requirement,
                "$.assetCandidateDraft.candidateItems[$index].publicDescription" to item.publicDescription,
                "$.assetCandidateDraft.candidateItems[$index].visualReferenceRequirement" to item.visualReferenceRequirement,
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
                                message = "Asset candidate draft validation forbids approved, locked, final, review-ready, production-ready, or production-locked status wording.",
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
        draft: AssetCandidateDraft,
        findings: MutableList<AssetCandidateDraftValidationFinding>,
    ): Int {
        var count = 0
        collectTextFields(draft).forEach { field ->
            forbiddenClaimPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings.add(
                        finding(
                            ruleId = "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN",
                            category = "safety_boundary",
                            severity = "CRITICAL",
                            path = field.path,
                            message = "Asset candidate draft validation forbids generated media, asset approval, production lock, canon application, or runtime scene creation claims.",
                            evidence = listOf(field.value),
                            suggestedFix = "Keep this draft limited to candidate-only preparation.",
                        ),
                    )
                }
            }
        }
        return count
    }

    private fun collectTextFields(draft: AssetCandidateDraft): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) {
            if (value.isNotBlank()) fields += TextField(path, value)
        }
        add("$.assetCandidateDraft.downstreamBoundary", draft.downstreamBoundary)
        add("$.assetCandidateDraft.blockedReason", draft.blockedReason)
        draft.candidateItems.forEachIndexed { index, item ->
            add("$.assetCandidateDraft.candidateItems[$index].candidateId", item.candidateId)
            add("$.assetCandidateDraft.candidateItems[$index].assetType", item.assetType)
            add("$.assetCandidateDraft.candidateItems[$index].candidateStatus", item.candidateStatus)
            add("$.assetCandidateDraft.candidateItems[$index].requirement", item.requirement)
            add("$.assetCandidateDraft.candidateItems[$index].publicDescription", item.publicDescription)
            add("$.assetCandidateDraft.candidateItems[$index].visualReferenceRequirement", item.visualReferenceRequirement)
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
    ): AssetCandidateDraftValidationFinding =
        AssetCandidateDraftValidationFinding(
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
