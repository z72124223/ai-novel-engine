package com.localnovel.engine.service

import com.localnovel.engine.model.ImageCandidateBrief
import com.localnovel.engine.model.ImageCandidatePromptItem
import com.localnovel.engine.model.ImageCandidatePromptPreparation
import com.localnovel.engine.model.ImageCandidatePromptPreparationEvidence
import com.localnovel.engine.model.ImageCandidatePromptPreparationFinding
import com.localnovel.engine.model.ImageCandidateRequirement

class ImageCandidatePromptPreparationService {
    private val requiredBriefNextStep = "image_candidate_prompt_preparation"
    private val nextAllowedDownstreamStep = "image_candidate_prompt_validation"
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
        Regex("""(?i)\bprompt\s+submitted\b"""),
        Regex("""(?i)\bsubmitted\s+prompt\b"""),
        Regex("""(?i)\bimage\s+generated\b"""),
        Regex("""(?i)\bgenerated\s+image\b"""),
        Regex("""(?i)\bvideo\s+generated\b"""),
        Regex("""(?i)\basset\s+approved\b"""),
        Regex("""(?i)\bassets\s+approved\b"""),
        Regex("""(?i)\basset\s+approval\s+complete\b"""),
        Regex("""(?i)\bproduction\s+locked\b"""),
        Regex("""(?i)\bcanon\s+applied\b"""),
        Regex("""(?i)\bruntime\s+scene\s+created\b"""),
        Regex("\u63d0\u793a\u5df2\u63d0\u4ea4|\u5716\u7247\u5df2\u751f\u6210|\u5f71\u7247\u5df2\u751f\u6210|\u7d20\u6750\u5df2\u6838\u51c6|\u7d20\u6750\u6838\u51c6\u5b8c\u6210|\u88fd\u4f5c\u9396\u5b9a|\u6b63\u5178\u5df2\u5957\u7528|\u57f7\u884c\u671f\u5834\u666f\u5df2\u5efa\u7acb"),
    )

    fun create(
        imageCandidateBrief: ImageCandidateBrief,
        preparationId: String = "",
        imageCandidateBriefFile: String = "",
    ): ImageCandidatePromptPreparation {
        val findings = mutableListOf<ImageCandidatePromptPreparationFinding>()
        validateBrief(imageCandidateBrief, findings)
        val sourceFieldCount = validateSourceFields(imageCandidateBrief, imageCandidateBriefFile, findings)
        validateRequirements(imageCandidateBrief.imageCandidateRequirements, findings)
        val promptItems = buildPromptItems(imageCandidateBrief.imageCandidateRequirements)
        val forbiddenStatusCount = validateForbiddenStatusText(imageCandidateBrief.imageCandidateRequirements, findings)
        val forbiddenOutputFieldCount = validateForbiddenOutputFields(imageCandidateBrief.imageCandidateRequirements, findings)
        val forbiddenClaimCount = validateForbiddenClaims(imageCandidateBrief.imageCandidateRequirements, findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") {
            findings
                .filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }
                .joinToString("; ") { it.ruleId }
        } else {
            ""
        }
        val resolvedPreparationId = preparationId.ifBlank {
            imageCandidateBrief.briefId
                .takeIf { it.isNotBlank() }
                ?.let { "$it-prompt-preparation" }
                .orEmpty()
        }

        return ImageCandidatePromptPreparation(
            verdict = verdict,
            preparationId = resolvedPreparationId,
            imageCandidateBriefFile = imageCandidateBriefFile,
            imageCandidateBriefId = imageCandidateBrief.briefId,
            imageCandidatePreflightFile = imageCandidateBrief.imageCandidatePreflightFile,
            imageCandidatePreflightId = imageCandidateBrief.imageCandidatePreflightId,
            assetCandidateHumanReviewDecisionIntakeFile = imageCandidateBrief.assetCandidateHumanReviewDecisionIntakeFile,
            decisionId = imageCandidateBrief.decisionId,
            assetCandidateReviewPreflightFile = imageCandidateBrief.assetCandidateReviewPreflightFile,
            assetCandidateReviewPreflightId = imageCandidateBrief.assetCandidateReviewPreflightId,
            assetCandidateDraftValidationReportFile = imageCandidateBrief.assetCandidateDraftValidationReportFile,
            assetCandidateDraftFile = imageCandidateBrief.assetCandidateDraftFile,
            assetCandidateDraftId = imageCandidateBrief.assetCandidateDraftId,
            assetCandidateBriefFile = imageCandidateBrief.assetCandidateBriefFile,
            assetCandidateBriefId = imageCandidateBrief.assetCandidateBriefId,
            assetCandidatePreflightFile = imageCandidateBrief.assetCandidatePreflightFile,
            assetCandidatePreflightId = imageCandidateBrief.assetCandidatePreflightId,
            humanReviewDecisionIntakeFile = imageCandidateBrief.humanReviewDecisionIntakeFile,
            humanReviewDecisionId = imageCandidateBrief.humanReviewDecisionId,
            storyboardPacketReviewFile = imageCandidateBrief.storyboardPacketReviewFile,
            storyboardPacketReviewId = imageCandidateBrief.storyboardPacketReviewId,
            acceptedScriptPacketId = imageCandidateBrief.acceptedScriptPacketId,
            scriptPacketDraftFile = imageCandidateBrief.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = imageCandidateBrief.scriptPacketDraftContentSha256,
            promptItems = if (verdict == "FAIL") emptyList() else promptItems,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = ImageCandidatePromptPreparationEvidence(
                imageCandidateBriefVerdict = imageCandidateBrief.verdict,
                imageCandidateBriefNextAllowedDownstreamStep = imageCandidateBrief.nextAllowedDownstreamStep,
                sourceFieldCount = sourceFieldCount,
                promptItemCount = promptItems.size,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
                forbiddenOutputFieldCount = forbiddenOutputFieldCount,
            ),
        )
    }

    private fun validateBrief(
        brief: ImageCandidateBrief,
        findings: MutableList<ImageCandidatePromptPreparationFinding>,
    ) {
        if (brief.verdict.trim().uppercase() != "PASS") {
            findings.add(finding("IMAGE_CANDIDATE_BRIEF_NOT_PASS", "upstream", "CRITICAL", "$.imageCandidateBrief.verdict", "Prompt preparation requires a PASS image candidate brief.", evidence = listOf(brief.verdict), suggestedFix = "Use the PASS image candidate brief."))
        }
        if (brief.nextAllowedDownstreamStep != requiredBriefNextStep) {
            findings.add(finding("IMAGE_CANDIDATE_BRIEF_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.imageCandidateBrief.nextAllowedDownstreamStep", "Prompt preparation requires nextAllowedDownstreamStep=image_candidate_prompt_preparation.", evidence = listOf(brief.nextAllowedDownstreamStep), suggestedFix = "Use the brief that explicitly allows image_candidate_prompt_preparation."))
        }
    }

    private fun validateSourceFields(
        brief: ImageCandidateBrief,
        briefFile: String,
        findings: MutableList<ImageCandidatePromptPreparationFinding>,
    ): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) {
                findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Prompt preparation requires nonblank $label.", suggestedFix = "Use the complete image candidate brief."))
            } else {
                count += 1
            }
        }
        requireNonBlank(briefFile, "$.inputs.imageCandidateBriefFile", "IMAGE_CANDIDATE_BRIEF_FILE_MISSING", "image candidate brief file")
        requireNonBlank(brief.briefId, "$.imageCandidateBrief.briefId", "IMAGE_CANDIDATE_BRIEF_ID_MISSING", "image candidate brief id")
        requireNonBlank(brief.imageCandidatePreflightFile, "$.imageCandidateBrief.imageCandidatePreflightFile", "IMAGE_CANDIDATE_PREFLIGHT_FILE_MISSING", "image candidate preflight file")
        requireNonBlank(brief.imageCandidatePreflightId, "$.imageCandidateBrief.imageCandidatePreflightId", "IMAGE_CANDIDATE_PREFLIGHT_ID_MISSING", "image candidate preflight id")
        requireNonBlank(brief.assetCandidateHumanReviewDecisionIntakeFile, "$.imageCandidateBrief.assetCandidateHumanReviewDecisionIntakeFile", "ASSET_CANDIDATE_HUMAN_REVIEW_DECISION_INTAKE_FILE_MISSING", "asset candidate human review decision intake file")
        requireNonBlank(brief.decisionId, "$.imageCandidateBrief.decisionId", "DECISION_ID_MISSING", "decision id")
        requireNonBlank(brief.assetCandidateReviewPreflightFile, "$.imageCandidateBrief.assetCandidateReviewPreflightFile", "ASSET_CANDIDATE_REVIEW_PREFLIGHT_FILE_MISSING", "asset candidate review preflight file")
        requireNonBlank(brief.assetCandidateReviewPreflightId, "$.imageCandidateBrief.assetCandidateReviewPreflightId", "ASSET_CANDIDATE_REVIEW_PREFLIGHT_ID_MISSING", "asset candidate review preflight id")
        requireNonBlank(brief.assetCandidateDraftValidationReportFile, "$.imageCandidateBrief.assetCandidateDraftValidationReportFile", "ASSET_CANDIDATE_DRAFT_VALIDATION_REPORT_FILE_MISSING", "asset candidate draft validation report file")
        requireNonBlank(brief.assetCandidateDraftFile, "$.imageCandidateBrief.assetCandidateDraftFile", "ASSET_CANDIDATE_DRAFT_FILE_MISSING", "asset candidate draft file")
        requireNonBlank(brief.assetCandidateDraftId, "$.imageCandidateBrief.assetCandidateDraftId", "ASSET_CANDIDATE_DRAFT_ID_MISSING", "asset candidate draft id")
        requireNonBlank(brief.assetCandidateBriefFile, "$.imageCandidateBrief.assetCandidateBriefFile", "ASSET_CANDIDATE_BRIEF_FILE_MISSING", "asset candidate brief file")
        requireNonBlank(brief.assetCandidateBriefId, "$.imageCandidateBrief.assetCandidateBriefId", "ASSET_CANDIDATE_BRIEF_ID_MISSING", "asset candidate brief id")
        requireNonBlank(brief.assetCandidatePreflightFile, "$.imageCandidateBrief.assetCandidatePreflightFile", "ASSET_CANDIDATE_PREFLIGHT_FILE_MISSING", "asset candidate preflight file")
        requireNonBlank(brief.assetCandidatePreflightId, "$.imageCandidateBrief.assetCandidatePreflightId", "ASSET_CANDIDATE_PREFLIGHT_ID_MISSING", "asset candidate preflight id")
        requireNonBlank(brief.humanReviewDecisionIntakeFile, "$.imageCandidateBrief.humanReviewDecisionIntakeFile", "STORYBOARD_HUMAN_REVIEW_DECISION_INTAKE_FILE_MISSING", "storyboard human review decision intake file")
        requireNonBlank(brief.humanReviewDecisionId, "$.imageCandidateBrief.humanReviewDecisionId", "STORYBOARD_HUMAN_REVIEW_DECISION_ID_MISSING", "storyboard human review decision id")
        requireNonBlank(brief.storyboardPacketReviewFile, "$.imageCandidateBrief.storyboardPacketReviewFile", "STORYBOARD_PACKET_REVIEW_FILE_MISSING", "storyboard packet review file")
        requireNonBlank(brief.storyboardPacketReviewId, "$.imageCandidateBrief.storyboardPacketReviewId", "STORYBOARD_PACKET_REVIEW_ID_MISSING", "storyboard packet review id")
        requireNonBlank(brief.acceptedScriptPacketId, "$.imageCandidateBrief.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(brief.scriptPacketDraftFile, "$.imageCandidateBrief.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(brief.scriptPacketDraftContentSha256, "$.imageCandidateBrief.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        if (brief.scriptPacketDraftContentSha256.isNotBlank() &&
            !sha256Regex.matches(brief.scriptPacketDraftContentSha256.trim().lowercase())
        ) {
            findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.imageCandidateBrief.scriptPacketDraftContentSha256", "Prompt preparation requires a valid lowercase SHA-256 script packet draft hash.", evidence = listOf(brief.scriptPacketDraftContentSha256), suggestedFix = "Use the hash carried by the image candidate brief."))
        }
        return count
    }

    private fun validateRequirements(
        requirements: List<ImageCandidateRequirement>,
        findings: MutableList<ImageCandidatePromptPreparationFinding>,
    ) {
        if (requirements.isEmpty()) {
            findings.add(finding("IMAGE_CANDIDATE_REQUIREMENTS_MISSING", "prompt_boundary", "CRITICAL", "$.imageCandidateBrief.imageCandidateRequirements", "Prompt preparation requires at least one image candidate requirement.", suggestedFix = "Use a brief with candidate-only image requirements."))
            return
        }
        requirements.forEachIndexed { index, requirement ->
            if (requirement.requirementId.isBlank() || requirement.candidateId.isBlank() || requirement.assetType.isBlank() || requirement.sourceRequirementId.isBlank() || requirement.requirement.isBlank()) {
                findings.add(finding("IMAGE_CANDIDATE_REQUIREMENT_INCOMPLETE", "prompt_boundary", "CRITICAL", "$.imageCandidateBrief.imageCandidateRequirements[$index]", "Prompt preparation requires complete requirement id, candidate id, type, source requirement id, and requirement text.", suggestedFix = "Use complete image candidate requirements."))
            }
            if (requirement.referenceRequirements.isEmpty()) {
                findings.add(finding("IMAGE_CANDIDATE_REFERENCE_REQUIREMENTS_MISSING", "prompt_boundary", "CRITICAL", "$.imageCandidateBrief.imageCandidateRequirements[$index].referenceRequirements", "Prompt preparation requires public visual reference requirements.", suggestedFix = "Declare public-only visual constraints."))
            }
            if (requirement.doNotShow.isEmpty()) {
                findings.add(finding("IMAGE_CANDIDATE_DO_NOT_SHOW_MISSING", "prompt_boundary", "CRITICAL", "$.imageCandidateBrief.imageCandidateRequirements[$index].doNotShow", "Prompt preparation requires do-not-show boundary.", suggestedFix = "Declare negative prompt / do-not-show boundary."))
            }
            if (requirement.promptSafetyNotes.isEmpty()) {
                findings.add(finding("IMAGE_CANDIDATE_PROMPT_SAFETY_NOTES_MISSING", "prompt_boundary", "CRITICAL", "$.imageCandidateBrief.imageCandidateRequirements[$index].promptSafetyNotes", "Prompt preparation requires prompt safety notes.", suggestedFix = "Declare no author-only, no provider, no generation, and no approval safety notes."))
            }
            if (requirement.status.trim().lowercase() !in allowedRequirementStatuses) {
                findings.add(finding("IMAGE_CANDIDATE_STATUS_NOT_ALLOWED", "prompt_boundary", "CRITICAL", "$.imageCandidateBrief.imageCandidateRequirements[$index].status", "Prompt preparation requirement status must remain candidate-only.", evidence = listOf(requirement.status), suggestedFix = "Use candidate, candidate_only, draft_candidate, or image_candidate."))
            }
        }
    }

    private fun buildPromptItems(requirements: List<ImageCandidateRequirement>): List<ImageCandidatePromptItem> =
        requirements.map { requirement ->
            ImageCandidatePromptItem(
                promptItemId = "${requirement.requirementId}-prompt",
                sourceRequirementId = requirement.requirementId,
                sourceCandidateId = requirement.candidateId,
                assetType = requirement.assetType,
                positivePromptDraft = listOf(
                    "candidate-only ${requirement.assetType} visual requirement",
                    requirement.requirement,
                    requirement.referenceRequirements.joinToString("; "),
                ).filter { it.isNotBlank() }.joinToString(": "),
                negativePromptDraft = requirement.doNotShow.joinToString("; "),
                doNotShow = requirement.doNotShow,
                promptSafetyNotes = requirement.promptSafetyNotes,
                publicVisualRequirements = requirement.referenceRequirements,
            )
        }

    private fun validateForbiddenStatusText(
        requirements: List<ImageCandidateRequirement>,
        findings: MutableList<ImageCandidatePromptPreparationFinding>,
    ): Int =
        scanRequirementText(requirements, forbiddenStatusPatterns, "IMAGE_CANDIDATE_STATUS_FORBIDDEN", "prompt_boundary", "Prompt preparation cannot contain approved, locked, final, review-ready, production-ready, approved-reference, or production-locked wording.", findings)

    private fun validateForbiddenClaims(
        requirements: List<ImageCandidateRequirement>,
        findings: MutableList<ImageCandidatePromptPreparationFinding>,
    ): Int =
        scanRequirementText(requirements, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", "Prompt preparation cannot claim prompt submission, generated media, asset approval, production lock, canon application, or runtime scene creation.", findings)

    private fun validateForbiddenOutputFields(
        requirements: List<ImageCandidateRequirement>,
        findings: MutableList<ImageCandidatePromptPreparationFinding>,
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
                    findings.add(finding(ruleId, "safety_boundary", "CRITICAL", "$.imageCandidateBrief.imageCandidateRequirements[$index].$field", "Prompt preparation cannot carry provider, generation API, job, or image output path information.", evidence = listOf(value), suggestedFix = "Remove provider/job/output details before prompt preparation."))
                }
            }
        }
        return count
    }

    private fun scanRequirementText(
        requirements: List<ImageCandidateRequirement>,
        patterns: List<Regex>,
        ruleId: String,
        category: String,
        message: String,
        findings: MutableList<ImageCandidatePromptPreparationFinding>,
    ): Int {
        var count = 0
        requirements.forEachIndexed { index, requirement ->
            collectRequirementText(requirement, index).forEach { field ->
                patterns.forEach { pattern ->
                    if (pattern.containsMatchIn(field.value)) {
                        count += 1
                        findings.add(finding(ruleId, category, "CRITICAL", field.path, message, evidence = listOf(field.value), suggestedFix = "Keep prompt preparation local, candidate-only, public-only, and unsubmitted."))
                    }
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
        add("$.imageCandidateBrief.imageCandidateRequirements[$index].assetType", requirement.assetType)
        add("$.imageCandidateBrief.imageCandidateRequirements[$index].status", requirement.status)
        add("$.imageCandidateBrief.imageCandidateRequirements[$index].requirement", requirement.requirement)
        requirement.referenceRequirements.forEachIndexed { noteIndex, value -> add("$.imageCandidateBrief.imageCandidateRequirements[$index].referenceRequirements[$noteIndex]", value) }
        requirement.doNotShow.forEachIndexed { noteIndex, value -> add("$.imageCandidateBrief.imageCandidateRequirements[$index].doNotShow[$noteIndex]", value) }
        requirement.promptSafetyNotes.forEachIndexed { noteIndex, value -> add("$.imageCandidateBrief.imageCandidateRequirements[$index].promptSafetyNotes[$noteIndex]", value) }
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
    ): ImageCandidatePromptPreparationFinding =
        ImageCandidatePromptPreparationFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private data class TextField(val path: String, val value: String)
}
