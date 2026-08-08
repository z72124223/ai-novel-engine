package com.localnovel.engine.service

import com.localnovel.engine.model.AssetCandidateBrief
import com.localnovel.engine.model.AssetCandidateBriefChecklistItem
import com.localnovel.engine.model.AssetCandidateBriefEvidence
import com.localnovel.engine.model.AssetCandidateBriefFinding
import com.localnovel.engine.model.AssetCandidateBriefRequirement
import com.localnovel.engine.model.AssetCandidatePreflightReport
import com.localnovel.engine.model.StoryboardPacketReviewAssetRequirement

class AssetCandidateBriefService {
    private val requiredPreflightStep = "asset_candidate_brief"
    private val nextAllowedDownstreamStep = "asset_candidate_draft"
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
    private val forbiddenCompletionClaimPatterns = listOf(
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
    private val forbiddenCandidateStatusPatterns = listOf(
        Regex("""(?i)\bapproved\b"""),
        Regex("""(?i)\blocked\b"""),
        Regex("""(?i)\bfinal\b"""),
        Regex("""(?i)\bproduction[-_\s]?ready\b"""),
        Regex("""(?i)\bapproved_reference\b"""),
        Regex("""(?i)\bproduction_locked\b"""),
        Regex("\u6838\u51c6|\u6279\u51c6|\u9396\u5b9a"),
    )

    fun create(
        assetCandidatePreflight: AssetCandidatePreflightReport,
        briefId: String = "",
        assetCandidatePreflightFile: String = "",
    ): AssetCandidateBrief {
        val findings = mutableListOf<AssetCandidateBriefFinding>()
        validatePreflight(assetCandidatePreflight, findings)
        val sourceFieldCount = validateSourceFields(assetCandidatePreflight, assetCandidatePreflightFile, findings)
        validateAssetCandidateRequirements(assetCandidatePreflight.assetCandidateRequirements, findings)
        val requirements = assetCandidatePreflight.assetCandidateRequirements.map {
            AssetCandidateBriefRequirement(
                candidateId = it.candidateId,
                requirement = it.requirement,
                sourceFrameIds = it.sourceFrameIds,
                candidateStatus = "candidate_only",
            )
        }
        val checklist = preparationChecklist()
        val forbiddenStatusCount = validateCandidateOnlyText(requirements, findings)
        val forbiddenClaimCount = validateForbiddenClaims(assetCandidatePreflight, requirements, checklist, findings)

        val verdict = when {
            findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
            findings.any { it.severity == "WARNING" } -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }
        val blockedReason = if (verdict == "FAIL") {
            findings
                .filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }
                .joinToString("; ") { it.ruleId }
        } else {
            ""
        }
        val resolvedBriefId = briefId.ifBlank {
            assetCandidatePreflight.preflightId
                .takeIf { it.isNotBlank() }
                ?.let { "$it-brief" }
                .orEmpty()
        }

        return AssetCandidateBrief(
            verdict = verdict,
            briefId = resolvedBriefId,
            assetCandidatePreflightFile = assetCandidatePreflightFile,
            assetCandidatePreflightId = assetCandidatePreflight.preflightId,
            humanReviewDecisionIntakeFile = assetCandidatePreflight.humanReviewDecisionIntakeFile,
            humanReviewDecisionId = assetCandidatePreflight.decisionId,
            storyboardPacketReviewFile = assetCandidatePreflight.storyboardPacketReviewFile,
            storyboardPacketReviewId = assetCandidatePreflight.storyboardPacketReviewId,
            acceptedScriptPacketId = assetCandidatePreflight.acceptedScriptPacketId,
            scriptPacketDraftFile = assetCandidatePreflight.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = assetCandidatePreflight.scriptPacketDraftContentSha256,
            assetCandidateRequirements = if (verdict == "FAIL") emptyList() else requirements,
            preparationChecklist = if (verdict == "FAIL") emptyList() else checklist,
            downstreamBoundary = "asset_candidate_brief_only",
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = AssetCandidateBriefEvidence(
                preflightVerdict = assetCandidatePreflight.verdict,
                preflightNextAllowedDownstreamStep = assetCandidatePreflight.nextAllowedDownstreamStep,
                sourceFieldCount = sourceFieldCount,
                assetCandidateRequirementCount = assetCandidatePreflight.assetCandidateRequirements.size,
                preparationChecklistCount = checklist.size,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
            ),
        )
    }

    private fun validatePreflight(
        preflight: AssetCandidatePreflightReport,
        findings: MutableList<AssetCandidateBriefFinding>,
    ) {
        if (preflight.verdict.trim().uppercase() != "PASS") {
            findings.add(
                finding(
                    ruleId = "ASSET_CANDIDATE_PREFLIGHT_NOT_PASS",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.assetCandidatePreflight.verdict",
                    message = "Asset candidate brief requires a PASS asset candidate preflight.",
                    evidence = listOf(preflight.verdict),
                    suggestedFix = "Use the PASS asset candidate preflight report.",
                ),
            )
        }
        if (preflight.nextAllowedDownstreamStep != requiredPreflightStep) {
            findings.add(
                finding(
                    ruleId = "ASSET_CANDIDATE_PREFLIGHT_NEXT_STEP_MISMATCH",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.assetCandidatePreflight.nextAllowedDownstreamStep",
                    message = "Asset candidate brief requires nextAllowedDownstreamStep=asset_candidate_brief.",
                    evidence = listOf(preflight.nextAllowedDownstreamStep),
                    suggestedFix = "Use the preflight report that explicitly allows asset_candidate_brief.",
                ),
            )
        }
    }

    private fun validateSourceFields(
        preflight: AssetCandidatePreflightReport,
        preflightFile: String,
        findings: MutableList<AssetCandidateBriefFinding>,
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
                        message = "Asset candidate brief requires nonblank $label.",
                        suggestedFix = "Use the complete asset candidate preflight report.",
                    ),
                )
            } else {
                count += 1
            }
        }
        requireNonBlank(preflightFile, "$.inputs.assetCandidatePreflightFile", "ASSET_CANDIDATE_PREFLIGHT_FILE_MISSING", "asset candidate preflight file")
        requireNonBlank(preflight.preflightId, "$.assetCandidatePreflight.preflightId", "ASSET_CANDIDATE_PREFLIGHT_ID_MISSING", "asset candidate preflight id")
        requireNonBlank(preflight.humanReviewDecisionIntakeFile, "$.assetCandidatePreflight.humanReviewDecisionIntakeFile", "HUMAN_REVIEW_DECISION_INTAKE_FILE_MISSING", "human review decision intake file")
        requireNonBlank(preflight.decisionId, "$.assetCandidatePreflight.decisionId", "HUMAN_REVIEW_DECISION_ID_MISSING", "human review decision id")
        requireNonBlank(preflight.storyboardPacketReviewFile, "$.assetCandidatePreflight.storyboardPacketReviewFile", "STORYBOARD_PACKET_REVIEW_FILE_MISSING", "storyboard packet review file")
        requireNonBlank(preflight.storyboardPacketReviewId, "$.assetCandidatePreflight.storyboardPacketReviewId", "STORYBOARD_PACKET_REVIEW_ID_MISSING", "storyboard packet review id")
        requireNonBlank(preflight.acceptedScriptPacketId, "$.assetCandidatePreflight.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(preflight.scriptPacketDraftFile, "$.assetCandidatePreflight.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(preflight.scriptPacketDraftContentSha256, "$.assetCandidatePreflight.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        if (preflight.scriptPacketDraftContentSha256.isNotBlank() &&
            !sha256Regex.matches(preflight.scriptPacketDraftContentSha256.trim().lowercase())
        ) {
            findings.add(
                finding(
                    ruleId = "SCRIPT_PACKET_DRAFT_HASH_INVALID",
                    category = "source_linkage",
                    severity = "CRITICAL",
                    path = "$.assetCandidatePreflight.scriptPacketDraftContentSha256",
                    message = "Asset candidate brief requires a valid lowercase SHA-256 script packet draft hash.",
                    evidence = listOf(preflight.scriptPacketDraftContentSha256),
                    suggestedFix = "Use the source hash carried by the asset candidate preflight report.",
                ),
            )
        }
        return count
    }

    private fun validateAssetCandidateRequirements(
        requirements: List<StoryboardPacketReviewAssetRequirement>,
        findings: MutableList<AssetCandidateBriefFinding>,
    ) {
        if (requirements.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "ASSET_CANDIDATE_REQUIREMENTS_MISSING",
                    category = "asset_candidate_boundary",
                    severity = "CRITICAL",
                    path = "$.assetCandidatePreflight.assetCandidateRequirements",
                    message = "Asset candidate brief requires at least one asset candidate requirement.",
                    suggestedFix = "Return to asset candidate preflight with candidate-only requirements.",
                ),
            )
            return
        }
        requirements.forEachIndexed { index, requirement ->
            if (requirement.candidateId.isBlank() || requirement.requirement.isBlank()) {
                findings.add(
                    finding(
                        ruleId = "ASSET_CANDIDATE_REQUIREMENT_INCOMPLETE",
                        category = "asset_candidate_boundary",
                        severity = "CRITICAL",
                        path = "$.assetCandidatePreflight.assetCandidateRequirements[$index]",
                        message = "Asset candidate requirement requires candidateId and requirement text.",
                        suggestedFix = "Provide candidate-only requirement text before creating an asset candidate brief.",
                    ),
                )
            }
        }
    }

    private fun validateCandidateOnlyText(
        requirements: List<AssetCandidateBriefRequirement>,
        findings: MutableList<AssetCandidateBriefFinding>,
    ): Int {
        var count = 0
        requirements.forEachIndexed { index, requirement ->
            val fields = listOf(
                "$.assetCandidateBrief.assetCandidateRequirements[$index].candidateId" to requirement.candidateId,
                "$.assetCandidateBrief.assetCandidateRequirements[$index].requirement" to requirement.requirement,
                "$.assetCandidateBrief.assetCandidateRequirements[$index].candidateStatus" to requirement.candidateStatus,
            ) + requirement.sourceFrameIds.mapIndexed { frameIndex, frameId ->
                "$.assetCandidateBrief.assetCandidateRequirements[$index].sourceFrameIds[$frameIndex]" to frameId
            }
            fields.forEach { (path, value) ->
                forbiddenCandidateStatusPatterns.forEach { pattern ->
                    if (value.isNotBlank() && pattern.containsMatchIn(value)) {
                        count += 1
                        findings.add(
                            finding(
                                ruleId = "ASSET_CANDIDATE_STATUS_FORBIDDEN",
                                category = "asset_candidate_boundary",
                                severity = "CRITICAL",
                                path = path,
                                message = "Asset candidate brief only accepts candidate / requirement / reference-needed wording, not approved, locked, final, or production-ready status.",
                                evidence = listOf(value),
                                suggestedFix = "Reword the asset requirement as candidate-only or reference-needed material.",
                            ),
                        )
                    }
                }
            }
        }
        return count
    }

    private fun validateForbiddenClaims(
        preflight: AssetCandidatePreflightReport,
        requirements: List<AssetCandidateBriefRequirement>,
        checklist: List<AssetCandidateBriefChecklistItem>,
        findings: MutableList<AssetCandidateBriefFinding>,
    ): Int {
        var count = 0
        collectTextFields(preflight, requirements, checklist).forEach { field ->
            forbiddenCompletionClaimPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings.add(
                        finding(
                            ruleId = "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN",
                            category = "safety_boundary",
                            severity = "CRITICAL",
                            path = field.path,
                            message = "Asset candidate brief cannot claim generated media, asset approval, production lock, canon application, or runtime scene creation.",
                            evidence = listOf(field.value),
                            suggestedFix = "Keep this brief limited to candidate preparation only.",
                        ),
                    )
                }
            }
        }
        return count
    }

    private fun collectTextFields(
        preflight: AssetCandidatePreflightReport,
        requirements: List<AssetCandidateBriefRequirement>,
        checklist: List<AssetCandidateBriefChecklistItem>,
    ): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) {
            if (value.isNotBlank()) fields += TextField(path, value)
        }
        add("$.assetCandidatePreflight.downstreamBoundary", preflight.downstreamBoundary)
        add("$.assetCandidatePreflight.blockedReason", preflight.blockedReason)
        requirements.forEachIndexed { index, requirement ->
            add("$.assetCandidateBrief.assetCandidateRequirements[$index].candidateId", requirement.candidateId)
            add("$.assetCandidateBrief.assetCandidateRequirements[$index].requirement", requirement.requirement)
            add("$.assetCandidateBrief.assetCandidateRequirements[$index].candidateStatus", requirement.candidateStatus)
        }
        checklist.forEachIndexed { index, item ->
            add("$.assetCandidateBrief.preparationChecklist[$index].category", item.category)
            add("$.assetCandidateBrief.preparationChecklist[$index].requiredCheck", item.requiredCheck)
            add("$.assetCandidateBrief.preparationChecklist[$index].status", item.status)
        }
        return fields
    }

    private fun preparationChecklist(): List<AssetCandidateBriefChecklistItem> =
        listOf(
            AssetCandidateBriefChecklistItem(
                itemId = "candidate-only-status",
                category = "candidate_only_boundary",
                requiredCheck = "Every asset remains candidate-only and unapproved.",
            ),
            AssetCandidateBriefChecklistItem(
                itemId = "manual-review-required",
                category = "manual_review",
                requiredCheck = "Human review is still required before any asset approval or lock.",
            ),
            AssetCandidateBriefChecklistItem(
                itemId = "no-media-generation",
                category = "downstream_boundary",
                requiredCheck = "Do not use this brief for image generation, video generation, production lock, or canon.",
            ),
        )

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): AssetCandidateBriefFinding =
        AssetCandidateBriefFinding(
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
