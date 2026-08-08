package com.localnovel.engine.service

import com.localnovel.engine.model.ImageCandidatePromptItem
import com.localnovel.engine.model.ImageCandidatePromptPreparation
import com.localnovel.engine.model.ImageCandidatePromptValidationEvidence
import com.localnovel.engine.model.ImageCandidatePromptValidationFinding
import com.localnovel.engine.model.ImageCandidatePromptValidationReport

class ImageCandidatePromptValidationService {
    private val requiredPreparationNextStep = "image_candidate_prompt_validation"
    private val nextAllowedDownstreamStep = "image_candidate_provider_handoff_preflight"
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
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
    private val authorOnlyPatterns = listOf(
        Regex("""(?i)\bauthor[-_\s]?only\b"""),
        Regex("""(?i)\bdirector[-_\s]?only\b"""),
        Regex("""(?i)\bhidden\s+truth\b"""),
        Regex("""(?i)\bsecret\s+cause\b"""),
        Regex("""(?i)\bhidden\s+motive\b"""),
        Regex("""(?i)\bfuture\s+reveal\b"""),
        Regex("\u4f5c\u8005\u5c08\u7528|\u96b1\u85cf\u771f\u76f8|\u79d8\u5bc6\u539f\u56e0|\u96b1\u85cf\u52d5\u6a5f|\u672a\u4f86\u63ed\u9732"),
    )

    fun validate(
        promptPreparation: ImageCandidatePromptPreparation,
        promptPreparationFile: String = "",
    ): ImageCandidatePromptValidationReport {
        val findings = mutableListOf<ImageCandidatePromptValidationFinding>()
        validatePreparation(promptPreparation, findings)
        val sourceFieldCount = validateSourceFields(promptPreparation, promptPreparationFile, findings)
        validatePromptItems(promptPreparation.promptItems, findings)
        val forbiddenOutputFieldCount = validateForbiddenOutputFields(promptPreparation.promptItems, findings)
        val forbiddenStatusCount = scanPromptText(promptPreparation.promptItems, forbiddenStatusPatterns, "PROMPT_STATUS_UPGRADE_FORBIDDEN", "prompt_boundary", "Prompt validation cannot allow approved, locked, final, review-ready, production-ready, approved-reference, or production-locked wording.", findings)
        val forbiddenClaimCount = scanPromptText(promptPreparation.promptItems, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", "Prompt validation cannot allow prompt submission, generated media, asset approval, production lock, canon application, or runtime scene creation claims.", findings)
        val authorOnlyLeakCount = scanPromptText(promptPreparation.promptItems, authorOnlyPatterns, "AUTHOR_ONLY_PROMPT_TEXT_FORBIDDEN", "prompt_safety", "Prompt-facing text cannot contain author-only, hidden truth, secret cause, hidden motive, or future reveal text.", findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") {
            findings
                .filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }
                .joinToString("; ") { it.ruleId }
        } else {
            ""
        }

        return ImageCandidatePromptValidationReport(
            verdict = verdict,
            imageCandidatePromptPreparationFile = promptPreparationFile,
            preparationId = promptPreparation.preparationId,
            imageCandidateBriefFile = promptPreparation.imageCandidateBriefFile,
            imageCandidateBriefId = promptPreparation.imageCandidateBriefId,
            imageCandidatePreflightFile = promptPreparation.imageCandidatePreflightFile,
            imageCandidatePreflightId = promptPreparation.imageCandidatePreflightId,
            assetCandidateHumanReviewDecisionIntakeFile = promptPreparation.assetCandidateHumanReviewDecisionIntakeFile,
            decisionId = promptPreparation.decisionId,
            assetCandidateReviewPreflightFile = promptPreparation.assetCandidateReviewPreflightFile,
            assetCandidateReviewPreflightId = promptPreparation.assetCandidateReviewPreflightId,
            assetCandidateDraftValidationReportFile = promptPreparation.assetCandidateDraftValidationReportFile,
            assetCandidateDraftFile = promptPreparation.assetCandidateDraftFile,
            assetCandidateDraftId = promptPreparation.assetCandidateDraftId,
            assetCandidateBriefFile = promptPreparation.assetCandidateBriefFile,
            assetCandidateBriefId = promptPreparation.assetCandidateBriefId,
            assetCandidatePreflightFile = promptPreparation.assetCandidatePreflightFile,
            assetCandidatePreflightId = promptPreparation.assetCandidatePreflightId,
            humanReviewDecisionIntakeFile = promptPreparation.humanReviewDecisionIntakeFile,
            humanReviewDecisionId = promptPreparation.humanReviewDecisionId,
            storyboardPacketReviewFile = promptPreparation.storyboardPacketReviewFile,
            storyboardPacketReviewId = promptPreparation.storyboardPacketReviewId,
            acceptedScriptPacketId = promptPreparation.acceptedScriptPacketId,
            scriptPacketDraftFile = promptPreparation.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = promptPreparation.scriptPacketDraftContentSha256,
            promptItems = if (verdict == "FAIL") emptyList() else promptPreparation.promptItems,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = ImageCandidatePromptValidationEvidence(
                promptPreparationVerdict = promptPreparation.verdict,
                promptPreparationNextAllowedDownstreamStep = promptPreparation.nextAllowedDownstreamStep,
                sourceFieldCount = sourceFieldCount,
                promptItemCount = promptPreparation.promptItems.size,
                authorOnlyLeakCount = authorOnlyLeakCount,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
                forbiddenOutputFieldCount = forbiddenOutputFieldCount,
            ),
        )
    }

    private fun validatePreparation(
        preparation: ImageCandidatePromptPreparation,
        findings: MutableList<ImageCandidatePromptValidationFinding>,
    ) {
        if (preparation.verdict.trim().uppercase() != "PASS") {
            findings.add(finding("PROMPT_PREPARATION_NOT_PASS", "upstream", "CRITICAL", "$.imageCandidatePromptPreparation.verdict", "Prompt validation requires a PASS prompt preparation.", evidence = listOf(preparation.verdict), suggestedFix = "Use the PASS image candidate prompt preparation."))
        }
        if (preparation.nextAllowedDownstreamStep != requiredPreparationNextStep) {
            findings.add(finding("PROMPT_PREPARATION_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.imageCandidatePromptPreparation.nextAllowedDownstreamStep", "Prompt validation requires nextAllowedDownstreamStep=image_candidate_prompt_validation.", evidence = listOf(preparation.nextAllowedDownstreamStep), suggestedFix = "Use the preparation that explicitly allows image_candidate_prompt_validation."))
        }
    }

    private fun validateSourceFields(
        preparation: ImageCandidatePromptPreparation,
        preparationFile: String,
        findings: MutableList<ImageCandidatePromptValidationFinding>,
    ): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) {
                findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Prompt validation requires nonblank $label.", suggestedFix = "Use the complete prompt preparation JSON."))
            } else {
                count += 1
            }
        }
        requireNonBlank(preparationFile, "$.inputs.imageCandidatePromptPreparationFile", "PROMPT_PREPARATION_FILE_MISSING", "prompt preparation file")
        requireNonBlank(preparation.preparationId, "$.imageCandidatePromptPreparation.preparationId", "PROMPT_PREPARATION_ID_MISSING", "prompt preparation id")
        requireNonBlank(preparation.imageCandidateBriefFile, "$.imageCandidatePromptPreparation.imageCandidateBriefFile", "IMAGE_CANDIDATE_BRIEF_FILE_MISSING", "image candidate brief file")
        requireNonBlank(preparation.imageCandidateBriefId, "$.imageCandidatePromptPreparation.imageCandidateBriefId", "IMAGE_CANDIDATE_BRIEF_ID_MISSING", "image candidate brief id")
        requireNonBlank(preparation.imageCandidatePreflightFile, "$.imageCandidatePromptPreparation.imageCandidatePreflightFile", "IMAGE_CANDIDATE_PREFLIGHT_FILE_MISSING", "image candidate preflight file")
        requireNonBlank(preparation.imageCandidatePreflightId, "$.imageCandidatePromptPreparation.imageCandidatePreflightId", "IMAGE_CANDIDATE_PREFLIGHT_ID_MISSING", "image candidate preflight id")
        requireNonBlank(preparation.assetCandidateHumanReviewDecisionIntakeFile, "$.imageCandidatePromptPreparation.assetCandidateHumanReviewDecisionIntakeFile", "ASSET_CANDIDATE_HUMAN_REVIEW_DECISION_INTAKE_FILE_MISSING", "asset candidate human review decision intake file")
        requireNonBlank(preparation.decisionId, "$.imageCandidatePromptPreparation.decisionId", "DECISION_ID_MISSING", "decision id")
        requireNonBlank(preparation.assetCandidateReviewPreflightFile, "$.imageCandidatePromptPreparation.assetCandidateReviewPreflightFile", "ASSET_CANDIDATE_REVIEW_PREFLIGHT_FILE_MISSING", "asset candidate review preflight file")
        requireNonBlank(preparation.assetCandidateReviewPreflightId, "$.imageCandidatePromptPreparation.assetCandidateReviewPreflightId", "ASSET_CANDIDATE_REVIEW_PREFLIGHT_ID_MISSING", "asset candidate review preflight id")
        requireNonBlank(preparation.assetCandidateDraftValidationReportFile, "$.imageCandidatePromptPreparation.assetCandidateDraftValidationReportFile", "ASSET_CANDIDATE_DRAFT_VALIDATION_REPORT_FILE_MISSING", "asset candidate draft validation report file")
        requireNonBlank(preparation.assetCandidateDraftFile, "$.imageCandidatePromptPreparation.assetCandidateDraftFile", "ASSET_CANDIDATE_DRAFT_FILE_MISSING", "asset candidate draft file")
        requireNonBlank(preparation.assetCandidateDraftId, "$.imageCandidatePromptPreparation.assetCandidateDraftId", "ASSET_CANDIDATE_DRAFT_ID_MISSING", "asset candidate draft id")
        requireNonBlank(preparation.assetCandidateBriefFile, "$.imageCandidatePromptPreparation.assetCandidateBriefFile", "ASSET_CANDIDATE_BRIEF_FILE_MISSING", "asset candidate brief file")
        requireNonBlank(preparation.assetCandidateBriefId, "$.imageCandidatePromptPreparation.assetCandidateBriefId", "ASSET_CANDIDATE_BRIEF_ID_MISSING", "asset candidate brief id")
        requireNonBlank(preparation.assetCandidatePreflightFile, "$.imageCandidatePromptPreparation.assetCandidatePreflightFile", "ASSET_CANDIDATE_PREFLIGHT_FILE_MISSING", "asset candidate preflight file")
        requireNonBlank(preparation.assetCandidatePreflightId, "$.imageCandidatePromptPreparation.assetCandidatePreflightId", "ASSET_CANDIDATE_PREFLIGHT_ID_MISSING", "asset candidate preflight id")
        requireNonBlank(preparation.humanReviewDecisionIntakeFile, "$.imageCandidatePromptPreparation.humanReviewDecisionIntakeFile", "STORYBOARD_HUMAN_REVIEW_DECISION_INTAKE_FILE_MISSING", "storyboard human review decision intake file")
        requireNonBlank(preparation.humanReviewDecisionId, "$.imageCandidatePromptPreparation.humanReviewDecisionId", "STORYBOARD_HUMAN_REVIEW_DECISION_ID_MISSING", "storyboard human review decision id")
        requireNonBlank(preparation.storyboardPacketReviewFile, "$.imageCandidatePromptPreparation.storyboardPacketReviewFile", "STORYBOARD_PACKET_REVIEW_FILE_MISSING", "storyboard packet review file")
        requireNonBlank(preparation.storyboardPacketReviewId, "$.imageCandidatePromptPreparation.storyboardPacketReviewId", "STORYBOARD_PACKET_REVIEW_ID_MISSING", "storyboard packet review id")
        requireNonBlank(preparation.acceptedScriptPacketId, "$.imageCandidatePromptPreparation.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(preparation.scriptPacketDraftFile, "$.imageCandidatePromptPreparation.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(preparation.scriptPacketDraftContentSha256, "$.imageCandidatePromptPreparation.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        if (preparation.scriptPacketDraftContentSha256.isNotBlank() &&
            !sha256Regex.matches(preparation.scriptPacketDraftContentSha256.trim().lowercase())
        ) {
            findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.imageCandidatePromptPreparation.scriptPacketDraftContentSha256", "Prompt validation requires a valid lowercase SHA-256 script packet draft hash.", evidence = listOf(preparation.scriptPacketDraftContentSha256), suggestedFix = "Use the hash carried by the prompt preparation."))
        }
        return count
    }

    private fun validatePromptItems(
        items: List<ImageCandidatePromptItem>,
        findings: MutableList<ImageCandidatePromptValidationFinding>,
    ) {
        if (items.isEmpty()) {
            findings.add(finding("PROMPT_ITEMS_MISSING", "prompt_boundary", "CRITICAL", "$.imageCandidatePromptPreparation.promptItems", "Prompt validation requires at least one prompt item.", suggestedFix = "Use prompt preparation with candidate prompt items."))
            return
        }
        items.forEachIndexed { index, item ->
            if (item.promptItemId.isBlank() || item.sourceRequirementId.isBlank() || item.sourceCandidateId.isBlank() || item.assetType.isBlank()) {
                findings.add(finding("PROMPT_ITEM_SOURCE_INCOMPLETE", "prompt_boundary", "CRITICAL", "$.imageCandidatePromptPreparation.promptItems[$index]", "Prompt item requires prompt item id, source requirement id, source candidate id, and asset type.", suggestedFix = "Use complete prompt item source fields."))
            }
            if (item.positivePromptDraft.isBlank()) {
                findings.add(finding("POSITIVE_PROMPT_DRAFT_MISSING", "prompt_boundary", "CRITICAL", "$.imageCandidatePromptPreparation.promptItems[$index].positivePromptDraft", "Prompt item requires a positive prompt draft.", suggestedFix = "Provide public-only positive visual prompt draft text."))
            }
            if (item.negativePromptDraft.isBlank() && item.doNotShow.isEmpty()) {
                findings.add(finding("NEGATIVE_PROMPT_OR_DO_NOT_SHOW_MISSING", "prompt_boundary", "CRITICAL", "$.imageCandidatePromptPreparation.promptItems[$index]", "Prompt item requires a negative prompt or do-not-show boundary.", suggestedFix = "Provide negative prompt text or do-not-show entries."))
            }
            if (item.promptSafetyNotes.isEmpty()) {
                findings.add(finding("PROMPT_SAFETY_NOTES_MISSING", "prompt_boundary", "CRITICAL", "$.imageCandidatePromptPreparation.promptItems[$index].promptSafetyNotes", "Prompt item requires prompt safety notes.", suggestedFix = "Declare public-only, no author-only, no provider, no approval safety notes."))
            }
        }
    }

    private fun validateForbiddenOutputFields(
        items: List<ImageCandidatePromptItem>,
        findings: MutableList<ImageCandidatePromptValidationFinding>,
    ): Int {
        var count = 0
        items.forEachIndexed { index, item ->
            listOf(
                Triple(item.providerName, "providerName", "PROVIDER_NAME_FORBIDDEN"),
                Triple(item.generationApi, "generationApi", "GENERATION_API_FORBIDDEN"),
                Triple(item.providerJobId, "providerJobId", "PROVIDER_JOB_ID_FORBIDDEN"),
                Triple(item.imageOutputPath, "imageOutputPath", "IMAGE_OUTPUT_PATH_FORBIDDEN"),
            ).forEach { (value, field, ruleId) ->
                if (value.isNotBlank()) {
                    count += 1
                    findings.add(finding(ruleId, "safety_boundary", "CRITICAL", "$.imageCandidatePromptPreparation.promptItems[$index].$field", "Prompt validation cannot carry provider, generation API, job, or image output path information.", evidence = listOf(value), suggestedFix = "Remove provider/job/output details before validation."))
                }
            }
        }
        return count
    }

    private fun scanPromptText(
        items: List<ImageCandidatePromptItem>,
        patterns: List<Regex>,
        ruleId: String,
        category: String,
        message: String,
        findings: MutableList<ImageCandidatePromptValidationFinding>,
    ): Int {
        var count = 0
        items.forEachIndexed { index, item ->
            collectPromptText(item, index).forEach { field ->
                patterns.forEach { pattern ->
                    if (pattern.containsMatchIn(field.value)) {
                        count += 1
                        findings.add(finding(ruleId, category, "CRITICAL", field.path, message, evidence = listOf(field.value), suggestedFix = "Keep prompt text public-only, candidate-only, unsubmitted, and provider-neutral."))
                    }
                }
            }
        }
        return count
    }

    private fun collectPromptText(item: ImageCandidatePromptItem, index: Int): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) {
            if (value.isNotBlank()) fields += TextField(path, value)
        }
        add("$.imageCandidatePromptPreparation.promptItems[$index].assetType", item.assetType)
        add("$.imageCandidatePromptPreparation.promptItems[$index].positivePromptDraft", item.positivePromptDraft)
        add("$.imageCandidatePromptPreparation.promptItems[$index].negativePromptDraft", item.negativePromptDraft)
        item.doNotShow.forEachIndexed { itemIndex, value -> add("$.imageCandidatePromptPreparation.promptItems[$index].doNotShow[$itemIndex]", value) }
        item.promptSafetyNotes.forEachIndexed { itemIndex, value -> add("$.imageCandidatePromptPreparation.promptItems[$index].promptSafetyNotes[$itemIndex]", value) }
        item.publicVisualRequirements.forEachIndexed { itemIndex, value -> add("$.imageCandidatePromptPreparation.promptItems[$index].publicVisualRequirements[$itemIndex]", value) }
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
    ): ImageCandidatePromptValidationFinding =
        ImageCandidatePromptValidationFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private data class TextField(val path: String, val value: String)
}
