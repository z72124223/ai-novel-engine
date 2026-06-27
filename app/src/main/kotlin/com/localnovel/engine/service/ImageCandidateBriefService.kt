package com.localnovel.engine.service

import com.localnovel.engine.model.ImageCandidateBrief
import com.localnovel.engine.model.ImageCandidateBriefChecklistItem
import com.localnovel.engine.model.ImageCandidateBriefEvidence
import com.localnovel.engine.model.ImageCandidateBriefFinding
import com.localnovel.engine.model.ImageCandidatePreflightReport
import com.localnovel.engine.model.ImageCandidateRequirement

class ImageCandidateBriefService {
    private val requiredPreflightNextStep = "image_candidate_brief"
    private val nextAllowedDownstreamStep = "image_candidate_prompt_preparation"
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
    private val allowedRequirementStatuses = setOf("candidate", "candidate_only", "draft_candidate", "image_candidate")
    private val forbiddenStatusPatterns = listOf(
        Regex("""(?i)\breview_ready\b"""),
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
        Regex("""(?i)\bimage\s+generated\b"""),
        Regex("""(?i)\bgenerated\s+image\b"""),
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
        imageCandidatePreflight: ImageCandidatePreflightReport,
        briefId: String = "",
        imageCandidatePreflightFile: String = "",
    ): ImageCandidateBrief {
        val findings = mutableListOf<ImageCandidateBriefFinding>()
        validatePreflight(imageCandidatePreflight, findings)
        val sourceFieldCount = validateSourceFields(imageCandidatePreflight, imageCandidatePreflightFile, findings)
        validateImageCandidateRequirements(imageCandidatePreflight.imageCandidateRequirements, findings)
        val checklist = preparationChecklist()
        val forbiddenStatusCount = validateForbiddenStatusText(imageCandidatePreflight.imageCandidateRequirements, checklist, findings)
        val forbiddenOutputFieldCount = validateForbiddenOutputFields(imageCandidatePreflight.imageCandidateRequirements, findings)
        val forbiddenClaimCount = validateForbiddenClaims(imageCandidatePreflight.imageCandidateRequirements, checklist, findings)

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
        val resolvedBriefId = briefId.ifBlank {
            imageCandidatePreflight.preflightId
                .takeIf { it.isNotBlank() }
                ?.let { "$it-brief" }
                .orEmpty()
        }

        return ImageCandidateBrief(
            verdict = verdict,
            briefId = resolvedBriefId,
            imageCandidatePreflightFile = imageCandidatePreflightFile,
            imageCandidatePreflightId = imageCandidatePreflight.preflightId,
            assetCandidateHumanReviewDecisionIntakeFile = imageCandidatePreflight.assetCandidateHumanReviewDecisionIntakeFile,
            decisionId = imageCandidatePreflight.decisionId,
            assetCandidateReviewPreflightFile = imageCandidatePreflight.assetCandidateReviewPreflightFile,
            assetCandidateReviewPreflightId = imageCandidatePreflight.assetCandidateReviewPreflightId,
            assetCandidateDraftValidationReportFile = imageCandidatePreflight.assetCandidateDraftValidationReportFile,
            assetCandidateDraftFile = imageCandidatePreflight.assetCandidateDraftFile,
            assetCandidateDraftId = imageCandidatePreflight.assetCandidateDraftId,
            assetCandidateBriefFile = imageCandidatePreflight.assetCandidateBriefFile,
            assetCandidateBriefId = imageCandidatePreflight.assetCandidateBriefId,
            assetCandidatePreflightFile = imageCandidatePreflight.assetCandidatePreflightFile,
            assetCandidatePreflightId = imageCandidatePreflight.assetCandidatePreflightId,
            humanReviewDecisionIntakeFile = imageCandidatePreflight.humanReviewDecisionIntakeFile,
            humanReviewDecisionId = imageCandidatePreflight.humanReviewDecisionId,
            storyboardPacketReviewFile = imageCandidatePreflight.storyboardPacketReviewFile,
            storyboardPacketReviewId = imageCandidatePreflight.storyboardPacketReviewId,
            acceptedScriptPacketId = imageCandidatePreflight.acceptedScriptPacketId,
            scriptPacketDraftFile = imageCandidatePreflight.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = imageCandidatePreflight.scriptPacketDraftContentSha256,
            imageCandidateRequirements = if (verdict == "FAIL") emptyList() else imageCandidatePreflight.imageCandidateRequirements,
            preparationChecklist = if (verdict == "FAIL") emptyList() else checklist,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = ImageCandidateBriefEvidence(
                imageCandidatePreflightVerdict = imageCandidatePreflight.verdict,
                imageCandidatePreflightNextAllowedDownstreamStep = imageCandidatePreflight.nextAllowedDownstreamStep,
                sourceFieldCount = sourceFieldCount,
                imageCandidateRequirementCount = imageCandidatePreflight.imageCandidateRequirements.size,
                preparationChecklistCount = checklist.size,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
                forbiddenOutputFieldCount = forbiddenOutputFieldCount,
            ),
        )
    }

    private fun validatePreflight(
        preflight: ImageCandidatePreflightReport,
        findings: MutableList<ImageCandidateBriefFinding>,
    ) {
        if (preflight.verdict.trim().uppercase() != "PASS") {
            findings.add(finding("IMAGE_CANDIDATE_PREFLIGHT_NOT_PASS", "upstream", "CRITICAL", "$.imageCandidatePreflight.verdict", "Image candidate brief requires a PASS image candidate preflight.", evidence = listOf(preflight.verdict), suggestedFix = "Use the PASS image candidate preflight report."))
        }
        if (preflight.nextAllowedDownstreamStep != requiredPreflightNextStep) {
            findings.add(finding("IMAGE_CANDIDATE_PREFLIGHT_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.imageCandidatePreflight.nextAllowedDownstreamStep", "Image candidate brief requires nextAllowedDownstreamStep=image_candidate_brief.", evidence = listOf(preflight.nextAllowedDownstreamStep), suggestedFix = "Use the preflight that explicitly allows image_candidate_brief."))
        }
    }

    private fun validateSourceFields(
        preflight: ImageCandidatePreflightReport,
        preflightFile: String,
        findings: MutableList<ImageCandidateBriefFinding>,
    ): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) {
                findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Image candidate brief requires nonblank $label.", suggestedFix = "Use the complete image candidate preflight report."))
            } else {
                count += 1
            }
        }
        requireNonBlank(preflightFile, "$.inputs.imageCandidatePreflightFile", "IMAGE_CANDIDATE_PREFLIGHT_FILE_MISSING", "image candidate preflight file")
        requireNonBlank(preflight.preflightId, "$.imageCandidatePreflight.preflightId", "IMAGE_CANDIDATE_PREFLIGHT_ID_MISSING", "image candidate preflight id")
        requireNonBlank(preflight.assetCandidateHumanReviewDecisionIntakeFile, "$.imageCandidatePreflight.assetCandidateHumanReviewDecisionIntakeFile", "ASSET_CANDIDATE_HUMAN_REVIEW_DECISION_INTAKE_FILE_MISSING", "asset candidate human review decision intake file")
        requireNonBlank(preflight.decisionId, "$.imageCandidatePreflight.decisionId", "DECISION_ID_MISSING", "decision id")
        requireNonBlank(preflight.assetCandidateReviewPreflightFile, "$.imageCandidatePreflight.assetCandidateReviewPreflightFile", "ASSET_CANDIDATE_REVIEW_PREFLIGHT_FILE_MISSING", "asset candidate review preflight file")
        requireNonBlank(preflight.assetCandidateReviewPreflightId, "$.imageCandidatePreflight.assetCandidateReviewPreflightId", "ASSET_CANDIDATE_REVIEW_PREFLIGHT_ID_MISSING", "asset candidate review preflight id")
        requireNonBlank(preflight.assetCandidateDraftValidationReportFile, "$.imageCandidatePreflight.assetCandidateDraftValidationReportFile", "ASSET_CANDIDATE_DRAFT_VALIDATION_REPORT_FILE_MISSING", "asset candidate draft validation report file")
        requireNonBlank(preflight.assetCandidateDraftFile, "$.imageCandidatePreflight.assetCandidateDraftFile", "ASSET_CANDIDATE_DRAFT_FILE_MISSING", "asset candidate draft file")
        requireNonBlank(preflight.assetCandidateDraftId, "$.imageCandidatePreflight.assetCandidateDraftId", "ASSET_CANDIDATE_DRAFT_ID_MISSING", "asset candidate draft id")
        requireNonBlank(preflight.assetCandidateBriefFile, "$.imageCandidatePreflight.assetCandidateBriefFile", "ASSET_CANDIDATE_BRIEF_FILE_MISSING", "asset candidate brief file")
        requireNonBlank(preflight.assetCandidateBriefId, "$.imageCandidatePreflight.assetCandidateBriefId", "ASSET_CANDIDATE_BRIEF_ID_MISSING", "asset candidate brief id")
        requireNonBlank(preflight.assetCandidatePreflightFile, "$.imageCandidatePreflight.assetCandidatePreflightFile", "ASSET_CANDIDATE_PREFLIGHT_FILE_MISSING", "asset candidate preflight file")
        requireNonBlank(preflight.assetCandidatePreflightId, "$.imageCandidatePreflight.assetCandidatePreflightId", "ASSET_CANDIDATE_PREFLIGHT_ID_MISSING", "asset candidate preflight id")
        requireNonBlank(preflight.humanReviewDecisionIntakeFile, "$.imageCandidatePreflight.humanReviewDecisionIntakeFile", "STORYBOARD_HUMAN_REVIEW_DECISION_INTAKE_FILE_MISSING", "storyboard human review decision intake file")
        requireNonBlank(preflight.humanReviewDecisionId, "$.imageCandidatePreflight.humanReviewDecisionId", "STORYBOARD_HUMAN_REVIEW_DECISION_ID_MISSING", "storyboard human review decision id")
        requireNonBlank(preflight.storyboardPacketReviewFile, "$.imageCandidatePreflight.storyboardPacketReviewFile", "STORYBOARD_PACKET_REVIEW_FILE_MISSING", "storyboard packet review file")
        requireNonBlank(preflight.storyboardPacketReviewId, "$.imageCandidatePreflight.storyboardPacketReviewId", "STORYBOARD_PACKET_REVIEW_ID_MISSING", "storyboard packet review id")
        requireNonBlank(preflight.acceptedScriptPacketId, "$.imageCandidatePreflight.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(preflight.scriptPacketDraftFile, "$.imageCandidatePreflight.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(preflight.scriptPacketDraftContentSha256, "$.imageCandidatePreflight.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        if (preflight.scriptPacketDraftContentSha256.isNotBlank() &&
            !sha256Regex.matches(preflight.scriptPacketDraftContentSha256.trim().lowercase())
        ) {
            findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.imageCandidatePreflight.scriptPacketDraftContentSha256", "Image candidate brief requires a valid lowercase SHA-256 script packet draft hash.", evidence = listOf(preflight.scriptPacketDraftContentSha256), suggestedFix = "Use the hash carried by the image candidate preflight."))
        }
        return count
    }

    private fun validateImageCandidateRequirements(
        requirements: List<ImageCandidateRequirement>,
        findings: MutableList<ImageCandidateBriefFinding>,
    ) {
        if (requirements.isEmpty()) {
            findings.add(finding("IMAGE_CANDIDATE_REQUIREMENTS_MISSING", "image_candidate_boundary", "CRITICAL", "$.imageCandidatePreflight.imageCandidateRequirements", "Image candidate brief requires at least one image candidate requirement.", suggestedFix = "Use a preflight report with candidate-only image requirements."))
            return
        }
        requirements.forEachIndexed { index, requirement ->
            if (requirement.requirementId.isBlank() || requirement.candidateId.isBlank() || requirement.assetType.isBlank() || requirement.sourceRequirementId.isBlank() || requirement.requirement.isBlank()) {
                findings.add(finding("IMAGE_CANDIDATE_REQUIREMENT_INCOMPLETE", "image_candidate_boundary", "CRITICAL", "$.imageCandidatePreflight.imageCandidateRequirements[$index]", "Image candidate brief requires complete requirement id, candidate id, type, source requirement id, and requirement text.", suggestedFix = "Use complete image candidate requirements."))
            }
            if (requirement.referenceRequirements.isEmpty()) {
                findings.add(finding("IMAGE_CANDIDATE_REFERENCE_REQUIREMENTS_MISSING", "image_candidate_boundary", "CRITICAL", "$.imageCandidatePreflight.imageCandidateRequirements[$index].referenceRequirements", "Image candidate brief requires reference requirements.", suggestedFix = "Declare needed views or reference constraints."))
            }
            if (requirement.doNotShow.isEmpty()) {
                findings.add(finding("IMAGE_CANDIDATE_DO_NOT_SHOW_MISSING", "image_candidate_boundary", "CRITICAL", "$.imageCandidatePreflight.imageCandidateRequirements[$index].doNotShow", "Image candidate brief requires do-not-show boundary.", suggestedFix = "Declare forbidden visual details for prompt safety."))
            }
            if (requirement.promptSafetyNotes.isEmpty()) {
                findings.add(finding("IMAGE_CANDIDATE_PROMPT_SAFETY_NOTES_MISSING", "image_candidate_boundary", "CRITICAL", "$.imageCandidatePreflight.imageCandidateRequirements[$index].promptSafetyNotes", "Image candidate brief requires prompt safety notes.", suggestedFix = "Declare no author-only, no approval, and no generation safety notes."))
            }
            if (requirement.status.trim().lowercase() !in allowedRequirementStatuses) {
                findings.add(finding("IMAGE_CANDIDATE_STATUS_NOT_ALLOWED", "image_candidate_boundary", "CRITICAL", "$.imageCandidatePreflight.imageCandidateRequirements[$index].status", "Image candidate requirement status must remain candidate-only.", evidence = listOf(requirement.status), suggestedFix = "Use candidate, candidate_only, draft_candidate, or image_candidate."))
            }
        }
    }

    private fun preparationChecklist(): List<ImageCandidateBriefChecklistItem> =
        listOf(
            ImageCandidateBriefChecklistItem("candidate-only", "candidate_boundary", "Every image candidate remains candidate-only and has no production status upgrade."),
            ImageCandidateBriefChecklistItem("no-generation", "downstream_boundary", "This brief cannot be used as an image generation request or provider job."),
            ImageCandidateBriefChecklistItem("prompt-safety", "prompt_safety", "Prompt preparation must keep author-only truth and status-upgrade wording out of prompt-facing text."),
        )

    private fun validateForbiddenStatusText(
        requirements: List<ImageCandidateRequirement>,
        checklist: List<ImageCandidateBriefChecklistItem>,
        findings: MutableList<ImageCandidateBriefFinding>,
    ): Int =
        scanText(requirements, checklist, forbiddenStatusPatterns, "IMAGE_CANDIDATE_STATUS_FORBIDDEN", "image_candidate_boundary", "Image candidate brief cannot contain approved, locked, final, review-ready, production-ready, approved-reference, or production-locked wording.", findings)

    private fun validateForbiddenClaims(
        requirements: List<ImageCandidateRequirement>,
        checklist: List<ImageCandidateBriefChecklistItem>,
        findings: MutableList<ImageCandidateBriefFinding>,
    ): Int =
        scanText(requirements, checklist, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", "Image candidate brief cannot claim generated media, asset approval, production lock, canon application, or runtime scene creation.", findings)

    private fun validateForbiddenOutputFields(
        requirements: List<ImageCandidateRequirement>,
        findings: MutableList<ImageCandidateBriefFinding>,
    ): Int {
        var count = 0
        requirements.forEachIndexed { index, requirement ->
            listOf(
                Triple(requirement.imageOutputPath, "imageOutputPath", "IMAGE_OUTPUT_PATH_FORBIDDEN"),
                Triple(requirement.generationApi, "generationApi", "GENERATION_API_FORBIDDEN"),
                Triple(requirement.providerJobId, "providerJobId", "PROVIDER_JOB_ID_FORBIDDEN"),
            ).forEach { (value, field, ruleId) ->
                if (value.isNotBlank()) {
                    count += 1
                    findings.add(finding(ruleId, "safety_boundary", "CRITICAL", "$.imageCandidatePreflight.imageCandidateRequirements[$index].$field", "Image candidate brief cannot carry image output path, generation API, provider, or job information.", evidence = listOf(value), suggestedFix = "Remove provider/job/output details before brief creation."))
                }
            }
        }
        return count
    }

    private fun scanText(
        requirements: List<ImageCandidateRequirement>,
        checklist: List<ImageCandidateBriefChecklistItem>,
        patterns: List<Regex>,
        ruleId: String,
        category: String,
        message: String,
        findings: MutableList<ImageCandidateBriefFinding>,
    ): Int {
        var count = 0
        val fields = mutableListOf<TextField>()
        requirements.forEachIndexed { index, requirement ->
            collectRequirementText(requirement, index).forEach(fields::add)
        }
        checklist.forEachIndexed { index, item ->
            fields += TextField("$.imageCandidateBrief.preparationChecklist[$index].requiredCheck", item.requiredCheck)
        }
        fields.forEach { field ->
            patterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings.add(finding(ruleId, category, "CRITICAL", field.path, message, evidence = listOf(field.value), suggestedFix = "Keep image candidate material preparation-only and unapproved."))
                }
            }
        }
        return count
    }

    private fun collectRequirementText(requirement: ImageCandidateRequirement, index: Int): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) {
            if (value.isNotBlank()) fields += TextField(path, value)
        }
        add("$.imageCandidatePreflight.imageCandidateRequirements[$index].status", requirement.status)
        add("$.imageCandidatePreflight.imageCandidateRequirements[$index].requirement", requirement.requirement)
        requirement.referenceRequirements.forEachIndexed { noteIndex, value -> add("$.imageCandidatePreflight.imageCandidateRequirements[$index].referenceRequirements[$noteIndex]", value) }
        requirement.doNotShow.forEachIndexed { noteIndex, value -> add("$.imageCandidatePreflight.imageCandidateRequirements[$index].doNotShow[$noteIndex]", value) }
        requirement.promptSafetyNotes.forEachIndexed { noteIndex, value -> add("$.imageCandidatePreflight.imageCandidateRequirements[$index].promptSafetyNotes[$noteIndex]", value) }
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
    ): ImageCandidateBriefFinding =
        ImageCandidateBriefFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private data class TextField(val path: String, val value: String)
}
