package com.localnovel.engine.service

import com.localnovel.engine.model.AssetCandidateBrief
import com.localnovel.engine.model.AssetCandidateBriefRequirement
import com.localnovel.engine.model.AssetCandidateDraft
import com.localnovel.engine.model.AssetCandidateDraftEvidence
import com.localnovel.engine.model.AssetCandidateDraftFinding
import com.localnovel.engine.model.AssetCandidateDraftItem

class AssetCandidateDraftService {
    private val requiredBriefStep = "asset_candidate_draft"
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

    fun create(
        assetCandidateBrief: AssetCandidateBrief,
        draftId: String = "",
        assetCandidateBriefFile: String = "",
    ): AssetCandidateDraft {
        val findings = mutableListOf<AssetCandidateDraftFinding>()
        validateBrief(assetCandidateBrief, findings)
        val sourceFieldCount = validateSourceFields(assetCandidateBrief, assetCandidateBriefFile, findings)
        val candidateItems = buildDraftItems(assetCandidateBrief.assetCandidateRequirements, findings)
        validateCandidateItems(candidateItems, findings)
        val forbiddenStatusCount = validateCandidateStatusText(assetCandidateBrief.assetCandidateRequirements, candidateItems, findings)
        val forbiddenClaimCount = validateForbiddenClaims(assetCandidateBrief, candidateItems, findings)

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
        val resolvedDraftId = draftId.ifBlank {
            assetCandidateBrief.briefId
                .takeIf { it.isNotBlank() }
                ?.let { "$it-draft" }
                .orEmpty()
        }

        return AssetCandidateDraft(
            verdict = verdict,
            draftId = resolvedDraftId,
            assetCandidateBriefFile = assetCandidateBriefFile,
            assetCandidateBriefId = assetCandidateBrief.briefId,
            assetCandidatePreflightFile = assetCandidateBrief.assetCandidatePreflightFile,
            assetCandidatePreflightId = assetCandidateBrief.assetCandidatePreflightId,
            humanReviewDecisionIntakeFile = assetCandidateBrief.humanReviewDecisionIntakeFile,
            humanReviewDecisionId = assetCandidateBrief.humanReviewDecisionId,
            storyboardPacketReviewFile = assetCandidateBrief.storyboardPacketReviewFile,
            storyboardPacketReviewId = assetCandidateBrief.storyboardPacketReviewId,
            acceptedScriptPacketId = assetCandidateBrief.acceptedScriptPacketId,
            scriptPacketDraftFile = assetCandidateBrief.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = assetCandidateBrief.scriptPacketDraftContentSha256,
            candidateItems = if (verdict == "FAIL") emptyList() else candidateItems,
            downstreamBoundary = "asset_candidate_draft_only",
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = AssetCandidateDraftEvidence(
                briefVerdict = assetCandidateBrief.verdict,
                briefNextAllowedDownstreamStep = assetCandidateBrief.nextAllowedDownstreamStep,
                sourceFieldCount = sourceFieldCount,
                candidateItemCount = assetCandidateBrief.assetCandidateRequirements.size,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
            ),
        )
    }

    private fun validateBrief(
        brief: AssetCandidateBrief,
        findings: MutableList<AssetCandidateDraftFinding>,
    ) {
        if (brief.verdict.trim().uppercase() != "PASS") {
            findings.add(
                finding(
                    ruleId = "ASSET_CANDIDATE_BRIEF_NOT_PASS",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.assetCandidateBrief.verdict",
                    message = "Asset candidate draft requires a PASS asset candidate brief.",
                    evidence = listOf(brief.verdict),
                    suggestedFix = "Use the PASS asset candidate brief.",
                ),
            )
        }
        if (brief.nextAllowedDownstreamStep != requiredBriefStep) {
            findings.add(
                finding(
                    ruleId = "ASSET_CANDIDATE_BRIEF_NEXT_STEP_MISMATCH",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.assetCandidateBrief.nextAllowedDownstreamStep",
                    message = "Asset candidate draft requires nextAllowedDownstreamStep=asset_candidate_draft.",
                    evidence = listOf(brief.nextAllowedDownstreamStep),
                    suggestedFix = "Use the brief that explicitly allows asset_candidate_draft.",
                ),
            )
        }
    }

    private fun validateSourceFields(
        brief: AssetCandidateBrief,
        briefFile: String,
        findings: MutableList<AssetCandidateDraftFinding>,
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
                        message = "Asset candidate draft requires nonblank $label.",
                        suggestedFix = "Use the complete asset candidate brief JSON.",
                    ),
                )
            } else {
                count += 1
            }
        }
        requireNonBlank(briefFile, "$.inputs.assetCandidateBriefFile", "ASSET_CANDIDATE_BRIEF_FILE_MISSING", "asset candidate brief file")
        requireNonBlank(brief.briefId, "$.assetCandidateBrief.briefId", "ASSET_CANDIDATE_BRIEF_ID_MISSING", "asset candidate brief id")
        requireNonBlank(brief.assetCandidatePreflightFile, "$.assetCandidateBrief.assetCandidatePreflightFile", "ASSET_CANDIDATE_PREFLIGHT_FILE_MISSING", "asset candidate preflight file")
        requireNonBlank(brief.assetCandidatePreflightId, "$.assetCandidateBrief.assetCandidatePreflightId", "ASSET_CANDIDATE_PREFLIGHT_ID_MISSING", "asset candidate preflight id")
        requireNonBlank(brief.humanReviewDecisionIntakeFile, "$.assetCandidateBrief.humanReviewDecisionIntakeFile", "HUMAN_REVIEW_DECISION_INTAKE_FILE_MISSING", "human review decision intake file")
        requireNonBlank(brief.humanReviewDecisionId, "$.assetCandidateBrief.humanReviewDecisionId", "HUMAN_REVIEW_DECISION_ID_MISSING", "human review decision id")
        requireNonBlank(brief.storyboardPacketReviewFile, "$.assetCandidateBrief.storyboardPacketReviewFile", "STORYBOARD_PACKET_REVIEW_FILE_MISSING", "storyboard packet review file")
        requireNonBlank(brief.storyboardPacketReviewId, "$.assetCandidateBrief.storyboardPacketReviewId", "STORYBOARD_PACKET_REVIEW_ID_MISSING", "storyboard packet review id")
        requireNonBlank(brief.acceptedScriptPacketId, "$.assetCandidateBrief.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(brief.scriptPacketDraftFile, "$.assetCandidateBrief.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(brief.scriptPacketDraftContentSha256, "$.assetCandidateBrief.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        if (brief.scriptPacketDraftContentSha256.isNotBlank() &&
            !sha256Regex.matches(brief.scriptPacketDraftContentSha256.trim().lowercase())
        ) {
            findings.add(
                finding(
                    ruleId = "SCRIPT_PACKET_DRAFT_HASH_INVALID",
                    category = "source_linkage",
                    severity = "CRITICAL",
                    path = "$.assetCandidateBrief.scriptPacketDraftContentSha256",
                    message = "Asset candidate draft requires a valid lowercase SHA-256 script packet draft hash.",
                    evidence = listOf(brief.scriptPacketDraftContentSha256),
                    suggestedFix = "Use the source hash carried by the asset candidate brief.",
                ),
            )
        }
        return count
    }

    private fun buildDraftItems(
        requirements: List<AssetCandidateBriefRequirement>,
        findings: MutableList<AssetCandidateDraftFinding>,
    ): List<AssetCandidateDraftItem> {
        if (requirements.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "ASSET_CANDIDATE_REQUIREMENTS_MISSING",
                    category = "asset_candidate_boundary",
                    severity = "CRITICAL",
                    path = "$.assetCandidateBrief.assetCandidateRequirements",
                    message = "Asset candidate draft requires at least one brief requirement.",
                    suggestedFix = "Create a brief with candidate-only requirements before drafting asset candidates.",
                ),
            )
            return emptyList()
        }
        return requirements.mapIndexed { index, requirement ->
            AssetCandidateDraftItem(
                candidateId = requirement.candidateId,
                assetType = inferAssetType(requirement),
                sourceRequirementId = requirement.candidateId,
                candidateStatus = "draft_candidate",
                requirement = requirement.requirement,
                publicDescription = requirement.requirement,
                visualReferenceRequirement = requirement.requirement,
                sourceFrameIds = requirement.sourceFrameIds,
            ).also { item ->
                if (requirement.candidateId.isBlank()) {
                    findings.add(
                        finding(
                            ruleId = "ASSET_CANDIDATE_ID_MISSING",
                            category = "asset_candidate_boundary",
                            severity = "CRITICAL",
                            path = "$.assetCandidateBrief.assetCandidateRequirements[$index].candidateId",
                            message = "Asset candidate draft item requires candidate id.",
                            suggestedFix = "Provide a candidate id in the asset candidate brief.",
                        ),
                    )
                }
                if (requirement.requirement.isBlank()) {
                    findings.add(
                        finding(
                            ruleId = "ASSET_CANDIDATE_REQUIREMENT_MISSING",
                            category = "asset_candidate_boundary",
                            severity = "CRITICAL",
                            path = "$.assetCandidateBrief.assetCandidateRequirements[$index].requirement",
                            message = "Asset candidate draft item requires requirement text.",
                            suggestedFix = "Provide candidate-only requirement text in the asset candidate brief.",
                        ),
                    )
                }
                if (item.assetType.isBlank()) {
                    findings.add(
                        finding(
                            ruleId = "ASSET_CANDIDATE_TYPE_MISSING",
                            category = "asset_candidate_boundary",
                            severity = "CRITICAL",
                            path = "$.assetCandidateBrief.assetCandidateRequirements[$index]",
                            message = "Asset candidate draft item requires a deterministic asset type.",
                            evidence = listOf(requirement.candidateId, requirement.requirement).filter { it.isNotBlank() },
                            suggestedFix = "Include a clear asset type term such as character, prop, costume, weapon, scene, zone, vehicle, document, animal, or visual effect.",
                        ),
                    )
                }
            }
        }
    }

    private fun validateCandidateItems(
        items: List<AssetCandidateDraftItem>,
        findings: MutableList<AssetCandidateDraftFinding>,
    ) {
        items.forEachIndexed { index, item ->
            if (item.sourceRequirementId.isBlank()) {
                findings.add(
                    finding(
                        ruleId = "SOURCE_REQUIREMENT_ID_MISSING",
                        category = "source_linkage",
                        severity = "CRITICAL",
                        path = "$.assetCandidateDraft.candidateItems[$index].sourceRequirementId",
                        message = "Asset candidate draft item requires source requirement id.",
                        suggestedFix = "Carry the brief requirement candidate id into sourceRequirementId.",
                    ),
                )
            }
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
                        message = "Asset candidate draft status must remain candidate-only.",
                        evidence = listOf(item.candidateStatus),
                        suggestedFix = "Use candidate, candidate_only, or draft_candidate only.",
                    ),
                )
            }
        }
    }

    private fun validateCandidateStatusText(
        requirements: List<AssetCandidateBriefRequirement>,
        items: List<AssetCandidateDraftItem>,
        findings: MutableList<AssetCandidateDraftFinding>,
    ): Int {
        var count = 0
        fun scan(path: String, value: String) {
            forbiddenStatusPatterns.forEach { pattern ->
                if (value.isNotBlank() && pattern.containsMatchIn(value)) {
                    count += 1
                    findings.add(
                        finding(
                            ruleId = "ASSET_CANDIDATE_STATUS_FORBIDDEN",
                            category = "asset_candidate_boundary",
                            severity = "CRITICAL",
                            path = path,
                            message = "Asset candidate draft cannot contain approved, locked, final, review-ready, production-ready, or production-locked status wording.",
                            evidence = listOf(value),
                            suggestedFix = "Keep the draft item status and wording candidate-only.",
                        ),
                    )
                }
            }
        }
        requirements.forEachIndexed { index, requirement ->
            scan("$.assetCandidateBrief.assetCandidateRequirements[$index].candidateStatus", requirement.candidateStatus)
            scan("$.assetCandidateBrief.assetCandidateRequirements[$index].candidateId", requirement.candidateId)
            scan("$.assetCandidateBrief.assetCandidateRequirements[$index].requirement", requirement.requirement)
        }
        items.forEachIndexed { index, item ->
            scan("$.assetCandidateDraft.candidateItems[$index].candidateStatus", item.candidateStatus)
            scan("$.assetCandidateDraft.candidateItems[$index].candidateId", item.candidateId)
            scan("$.assetCandidateDraft.candidateItems[$index].requirement", item.requirement)
        }
        return count
    }

    private fun validateForbiddenClaims(
        brief: AssetCandidateBrief,
        items: List<AssetCandidateDraftItem>,
        findings: MutableList<AssetCandidateDraftFinding>,
    ): Int {
        var count = 0
        collectTextFields(brief, items).forEach { field ->
            forbiddenClaimPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings.add(
                        finding(
                            ruleId = "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN",
                            category = "safety_boundary",
                            severity = "CRITICAL",
                            path = field.path,
                            message = "Asset candidate draft cannot claim generated media, asset approval, production lock, canon application, or runtime scene creation.",
                            evidence = listOf(field.value),
                            suggestedFix = "Keep this draft limited to candidate-only preparation.",
                        ),
                    )
                }
            }
        }
        return count
    }

    private fun collectTextFields(
        brief: AssetCandidateBrief,
        items: List<AssetCandidateDraftItem>,
    ): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) {
            if (value.isNotBlank()) fields += TextField(path, value)
        }
        add("$.assetCandidateBrief.downstreamBoundary", brief.downstreamBoundary)
        add("$.assetCandidateBrief.blockedReason", brief.blockedReason)
        brief.preparationChecklist.forEachIndexed { index, item ->
            add("$.assetCandidateBrief.preparationChecklist[$index].requiredCheck", item.requiredCheck)
        }
        items.forEachIndexed { index, item ->
            add("$.assetCandidateDraft.candidateItems[$index].candidateId", item.candidateId)
            add("$.assetCandidateDraft.candidateItems[$index].assetType", item.assetType)
            add("$.assetCandidateDraft.candidateItems[$index].candidateStatus", item.candidateStatus)
            add("$.assetCandidateDraft.candidateItems[$index].requirement", item.requirement)
            add("$.assetCandidateDraft.candidateItems[$index].publicDescription", item.publicDescription)
            add("$.assetCandidateDraft.candidateItems[$index].visualReferenceRequirement", item.visualReferenceRequirement)
        }
        return fields
    }

    private fun inferAssetType(requirement: AssetCandidateBriefRequirement): String {
        val text = "${requirement.candidateId} ${requirement.requirement}".lowercase()
        return when {
            hasAnyTypeTerm(text, "character", "person", "npc", "角色", "人物") -> "character"
            hasAnyTypeTerm(text, "animal", "creature", "beast", "動物") -> "animal"
            hasAnyTypeTerm(text, "costume", "clothing", "robe", "服裝", "衣") -> "costume"
            hasAnyTypeTerm(text, "weapon", "sword", "blade", "gun", "武器", "劍", "刀") -> "weapon"
            hasAnyTypeTerm(text, "prop", "door", "mark", "sign", "token", "key", "物件", "道具", "門") -> "prop"
            hasAnyTypeTerm(text, "scene", "set", "interior", "exterior", "場景") -> "scene"
            hasAnyTypeTerm(text, "zone", "district", "region", "區域") -> "zone"
            hasAnyTypeTerm(text, "vehicle", "car", "boat", "cart", "車", "船") -> "vehicle"
            hasAnyTypeTerm(text, "document", "paper", "letter", "scroll", "文件", "信", "卷") -> "document"
            hasAnyTypeTerm(text, "effect", "visual effect", "vfx", "光效", "特效") -> "visualEffect"
            else -> ""
        }
    }

    private fun hasAnyTypeTerm(text: String, vararg terms: String): Boolean =
        terms.any { term ->
            if (term.any { it.code > 127 }) {
                term in text
            } else {
                Regex("""(?i)(^|[^a-z0-9])${Regex.escape(term)}([^a-z0-9]|$)""").containsMatchIn(text)
            }
        }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): AssetCandidateDraftFinding =
        AssetCandidateDraftFinding(
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
