package com.localnovel.engine.service

import com.localnovel.engine.model.ImageCandidatePromptItem
import com.localnovel.engine.model.ImageCandidatePromptValidationReport
import com.localnovel.engine.model.ImageCandidateProviderHandoffPreflightEvidence
import com.localnovel.engine.model.ImageCandidateProviderHandoffPreflightFinding
import com.localnovel.engine.model.ImageCandidateProviderHandoffPreflightReport

class ImageCandidateProviderHandoffPreflightService {
    private val requiredValidationNextStep = "image_candidate_provider_handoff_preflight"
    private val nextAllowedDownstreamStep = "image_candidate_manual_provider_handoff_ready"
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
        promptValidationReport: ImageCandidatePromptValidationReport,
        preflightId: String = "",
        promptValidationReportFile: String = "",
    ): ImageCandidateProviderHandoffPreflightReport {
        val findings = mutableListOf<ImageCandidateProviderHandoffPreflightFinding>()
        validatePromptValidationReport(promptValidationReport, findings)
        val sourceFieldCount = validateSourceFields(promptValidationReport, promptValidationReportFile, findings)
        validatePromptItems(promptValidationReport.promptItems, findings)
        val forbiddenOutputFieldCount = validateForbiddenOutputFields(promptValidationReport.promptItems, findings)
        val forbiddenStatusCount = scanPromptText(promptValidationReport.promptItems, forbiddenStatusPatterns, "PROMPT_STATUS_UPGRADE_FORBIDDEN", "provider_handoff_boundary", "Provider handoff preflight cannot allow approved, locked, final, review-ready, production-ready, approved-reference, or production-locked wording.", findings)
        val forbiddenClaimCount = scanPromptText(promptValidationReport.promptItems, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", "Provider handoff preflight cannot allow prompt submission, generated media, asset approval, production lock, canon application, or runtime scene creation claims.", findings)
        val authorOnlyLeakCount = scanPromptText(promptValidationReport.promptItems, authorOnlyPatterns, "AUTHOR_ONLY_PROMPT_TEXT_FORBIDDEN", "prompt_safety", "Provider handoff preflight cannot carry author-only, hidden truth, secret cause, hidden motive, or future reveal text.", findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") {
            findings
                .filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }
                .joinToString("; ") { it.ruleId }
        } else {
            ""
        }
        val resolvedPreflightId = preflightId.ifBlank {
            promptValidationReport.preparationId
                .takeIf { it.isNotBlank() }
                ?.let { "$it-provider-handoff-preflight" }
                .orEmpty()
        }

        return ImageCandidateProviderHandoffPreflightReport(
            verdict = verdict,
            preflightId = resolvedPreflightId,
            imageCandidatePromptValidationReportFile = promptValidationReportFile,
            imageCandidatePromptPreparationFile = promptValidationReport.imageCandidatePromptPreparationFile,
            preparationId = promptValidationReport.preparationId,
            imageCandidateBriefFile = promptValidationReport.imageCandidateBriefFile,
            imageCandidateBriefId = promptValidationReport.imageCandidateBriefId,
            imageCandidatePreflightFile = promptValidationReport.imageCandidatePreflightFile,
            imageCandidatePreflightId = promptValidationReport.imageCandidatePreflightId,
            assetCandidateHumanReviewDecisionIntakeFile = promptValidationReport.assetCandidateHumanReviewDecisionIntakeFile,
            decisionId = promptValidationReport.decisionId,
            assetCandidateReviewPreflightFile = promptValidationReport.assetCandidateReviewPreflightFile,
            assetCandidateReviewPreflightId = promptValidationReport.assetCandidateReviewPreflightId,
            assetCandidateDraftValidationReportFile = promptValidationReport.assetCandidateDraftValidationReportFile,
            assetCandidateDraftFile = promptValidationReport.assetCandidateDraftFile,
            assetCandidateDraftId = promptValidationReport.assetCandidateDraftId,
            assetCandidateBriefFile = promptValidationReport.assetCandidateBriefFile,
            assetCandidateBriefId = promptValidationReport.assetCandidateBriefId,
            assetCandidatePreflightFile = promptValidationReport.assetCandidatePreflightFile,
            assetCandidatePreflightId = promptValidationReport.assetCandidatePreflightId,
            humanReviewDecisionIntakeFile = promptValidationReport.humanReviewDecisionIntakeFile,
            humanReviewDecisionId = promptValidationReport.humanReviewDecisionId,
            storyboardPacketReviewFile = promptValidationReport.storyboardPacketReviewFile,
            storyboardPacketReviewId = promptValidationReport.storyboardPacketReviewId,
            acceptedScriptPacketId = promptValidationReport.acceptedScriptPacketId,
            scriptPacketDraftFile = promptValidationReport.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = promptValidationReport.scriptPacketDraftContentSha256,
            promptItems = if (verdict == "FAIL") emptyList() else promptValidationReport.promptItems,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = ImageCandidateProviderHandoffPreflightEvidence(
                promptValidationVerdict = promptValidationReport.verdict,
                promptValidationNextAllowedDownstreamStep = promptValidationReport.nextAllowedDownstreamStep,
                sourceFieldCount = sourceFieldCount,
                promptItemCount = promptValidationReport.promptItems.size,
                authorOnlyLeakCount = authorOnlyLeakCount,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
                forbiddenOutputFieldCount = forbiddenOutputFieldCount,
            ),
        )
    }

    private fun validatePromptValidationReport(
        report: ImageCandidatePromptValidationReport,
        findings: MutableList<ImageCandidateProviderHandoffPreflightFinding>,
    ) {
        if (report.verdict.trim().uppercase() != "PASS") {
            findings.add(finding("PROMPT_VALIDATION_REPORT_NOT_PASS", "upstream", "CRITICAL", "$.imageCandidatePromptValidationReport.verdict", "Provider handoff preflight requires a PASS prompt validation report.", evidence = listOf(report.verdict), suggestedFix = "Use the PASS image candidate prompt validation report."))
        }
        if (report.nextAllowedDownstreamStep != requiredValidationNextStep) {
            findings.add(finding("PROMPT_VALIDATION_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.imageCandidatePromptValidationReport.nextAllowedDownstreamStep", "Provider handoff preflight requires nextAllowedDownstreamStep=image_candidate_provider_handoff_preflight.", evidence = listOf(report.nextAllowedDownstreamStep), suggestedFix = "Use the validation report that explicitly allows image_candidate_provider_handoff_preflight."))
        }
    }

    private fun validateSourceFields(
        report: ImageCandidatePromptValidationReport,
        reportFile: String,
        findings: MutableList<ImageCandidateProviderHandoffPreflightFinding>,
    ): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) {
                findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Provider handoff preflight requires nonblank $label.", suggestedFix = "Use the complete prompt validation report."))
            } else {
                count += 1
            }
        }
        requireNonBlank(reportFile, "$.inputs.imageCandidatePromptValidationReportFile", "PROMPT_VALIDATION_REPORT_FILE_MISSING", "prompt validation report file")
        requireNonBlank(report.imageCandidatePromptPreparationFile, "$.imageCandidatePromptValidationReport.imageCandidatePromptPreparationFile", "PROMPT_PREPARATION_FILE_MISSING", "prompt preparation file")
        requireNonBlank(report.preparationId, "$.imageCandidatePromptValidationReport.preparationId", "PROMPT_PREPARATION_ID_MISSING", "prompt preparation id")
        requireNonBlank(report.imageCandidateBriefFile, "$.imageCandidatePromptValidationReport.imageCandidateBriefFile", "IMAGE_CANDIDATE_BRIEF_FILE_MISSING", "image candidate brief file")
        requireNonBlank(report.imageCandidateBriefId, "$.imageCandidatePromptValidationReport.imageCandidateBriefId", "IMAGE_CANDIDATE_BRIEF_ID_MISSING", "image candidate brief id")
        requireNonBlank(report.imageCandidatePreflightFile, "$.imageCandidatePromptValidationReport.imageCandidatePreflightFile", "IMAGE_CANDIDATE_PREFLIGHT_FILE_MISSING", "image candidate preflight file")
        requireNonBlank(report.imageCandidatePreflightId, "$.imageCandidatePromptValidationReport.imageCandidatePreflightId", "IMAGE_CANDIDATE_PREFLIGHT_ID_MISSING", "image candidate preflight id")
        requireNonBlank(report.assetCandidateHumanReviewDecisionIntakeFile, "$.imageCandidatePromptValidationReport.assetCandidateHumanReviewDecisionIntakeFile", "ASSET_CANDIDATE_HUMAN_REVIEW_DECISION_INTAKE_FILE_MISSING", "asset candidate human review decision intake file")
        requireNonBlank(report.decisionId, "$.imageCandidatePromptValidationReport.decisionId", "DECISION_ID_MISSING", "decision id")
        requireNonBlank(report.assetCandidateReviewPreflightFile, "$.imageCandidatePromptValidationReport.assetCandidateReviewPreflightFile", "ASSET_CANDIDATE_REVIEW_PREFLIGHT_FILE_MISSING", "asset candidate review preflight file")
        requireNonBlank(report.assetCandidateReviewPreflightId, "$.imageCandidatePromptValidationReport.assetCandidateReviewPreflightId", "ASSET_CANDIDATE_REVIEW_PREFLIGHT_ID_MISSING", "asset candidate review preflight id")
        requireNonBlank(report.assetCandidateDraftValidationReportFile, "$.imageCandidatePromptValidationReport.assetCandidateDraftValidationReportFile", "ASSET_CANDIDATE_DRAFT_VALIDATION_REPORT_FILE_MISSING", "asset candidate draft validation report file")
        requireNonBlank(report.assetCandidateDraftFile, "$.imageCandidatePromptValidationReport.assetCandidateDraftFile", "ASSET_CANDIDATE_DRAFT_FILE_MISSING", "asset candidate draft file")
        requireNonBlank(report.assetCandidateDraftId, "$.imageCandidatePromptValidationReport.assetCandidateDraftId", "ASSET_CANDIDATE_DRAFT_ID_MISSING", "asset candidate draft id")
        requireNonBlank(report.assetCandidateBriefFile, "$.imageCandidatePromptValidationReport.assetCandidateBriefFile", "ASSET_CANDIDATE_BRIEF_FILE_MISSING", "asset candidate brief file")
        requireNonBlank(report.assetCandidateBriefId, "$.imageCandidatePromptValidationReport.assetCandidateBriefId", "ASSET_CANDIDATE_BRIEF_ID_MISSING", "asset candidate brief id")
        requireNonBlank(report.assetCandidatePreflightFile, "$.imageCandidatePromptValidationReport.assetCandidatePreflightFile", "ASSET_CANDIDATE_PREFLIGHT_FILE_MISSING", "asset candidate preflight file")
        requireNonBlank(report.assetCandidatePreflightId, "$.imageCandidatePromptValidationReport.assetCandidatePreflightId", "ASSET_CANDIDATE_PREFLIGHT_ID_MISSING", "asset candidate preflight id")
        requireNonBlank(report.humanReviewDecisionIntakeFile, "$.imageCandidatePromptValidationReport.humanReviewDecisionIntakeFile", "STORYBOARD_HUMAN_REVIEW_DECISION_INTAKE_FILE_MISSING", "storyboard human review decision intake file")
        requireNonBlank(report.humanReviewDecisionId, "$.imageCandidatePromptValidationReport.humanReviewDecisionId", "STORYBOARD_HUMAN_REVIEW_DECISION_ID_MISSING", "storyboard human review decision id")
        requireNonBlank(report.storyboardPacketReviewFile, "$.imageCandidatePromptValidationReport.storyboardPacketReviewFile", "STORYBOARD_PACKET_REVIEW_FILE_MISSING", "storyboard packet review file")
        requireNonBlank(report.storyboardPacketReviewId, "$.imageCandidatePromptValidationReport.storyboardPacketReviewId", "STORYBOARD_PACKET_REVIEW_ID_MISSING", "storyboard packet review id")
        requireNonBlank(report.acceptedScriptPacketId, "$.imageCandidatePromptValidationReport.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(report.scriptPacketDraftFile, "$.imageCandidatePromptValidationReport.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(report.scriptPacketDraftContentSha256, "$.imageCandidatePromptValidationReport.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        if (report.scriptPacketDraftContentSha256.isNotBlank() &&
            !sha256Regex.matches(report.scriptPacketDraftContentSha256.trim().lowercase())
        ) {
            findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.imageCandidatePromptValidationReport.scriptPacketDraftContentSha256", "Provider handoff preflight requires a valid lowercase SHA-256 script packet draft hash.", evidence = listOf(report.scriptPacketDraftContentSha256), suggestedFix = "Use the hash carried by the prompt validation report."))
        }
        return count
    }

    private fun validatePromptItems(
        items: List<ImageCandidatePromptItem>,
        findings: MutableList<ImageCandidateProviderHandoffPreflightFinding>,
    ) {
        if (items.isEmpty()) {
            findings.add(finding("PROMPT_ITEMS_MISSING", "provider_handoff_boundary", "CRITICAL", "$.imageCandidatePromptValidationReport.promptItems", "Provider handoff preflight requires validated prompt items.", suggestedFix = "Use a prompt validation report with prompt items."))
            return
        }
        items.forEachIndexed { index, item ->
            if (item.promptItemId.isBlank() || item.sourceRequirementId.isBlank() || item.sourceCandidateId.isBlank() || item.assetType.isBlank() || item.positivePromptDraft.isBlank()) {
                findings.add(finding("PROMPT_ITEM_INCOMPLETE", "provider_handoff_boundary", "CRITICAL", "$.imageCandidatePromptValidationReport.promptItems[$index]", "Provider handoff preflight requires complete prompt item source fields and positive prompt draft.", suggestedFix = "Use complete validated prompt items."))
            }
            if (item.negativePromptDraft.isBlank() && item.doNotShow.isEmpty()) {
                findings.add(finding("NEGATIVE_PROMPT_OR_DO_NOT_SHOW_MISSING", "provider_handoff_boundary", "CRITICAL", "$.imageCandidatePromptValidationReport.promptItems[$index]", "Provider handoff preflight requires negative prompt or do-not-show boundary.", suggestedFix = "Keep do-not-show boundary attached to prompt items."))
            }
            if (item.promptSafetyNotes.isEmpty()) {
                findings.add(finding("PROMPT_SAFETY_NOTES_MISSING", "provider_handoff_boundary", "CRITICAL", "$.imageCandidatePromptValidationReport.promptItems[$index].promptSafetyNotes", "Provider handoff preflight requires prompt safety notes.", suggestedFix = "Keep prompt safety notes attached to prompt items."))
            }
        }
    }

    private fun validateForbiddenOutputFields(
        items: List<ImageCandidatePromptItem>,
        findings: MutableList<ImageCandidateProviderHandoffPreflightFinding>,
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
                    findings.add(finding(ruleId, "safety_boundary", "CRITICAL", "$.imageCandidatePromptValidationReport.promptItems[$index].$field", "Provider handoff preflight cannot carry provider, generation API, job, or image output path information.", evidence = listOf(value), suggestedFix = "Remove provider/job/output details before provider handoff preflight."))
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
        findings: MutableList<ImageCandidateProviderHandoffPreflightFinding>,
    ): Int {
        var count = 0
        items.forEachIndexed { index, item ->
            collectPromptText(item, index).forEach { field ->
                patterns.forEach { pattern ->
                    if (pattern.containsMatchIn(field.value)) {
                        count += 1
                        findings.add(finding(ruleId, category, "CRITICAL", field.path, message, evidence = listOf(field.value), suggestedFix = "Keep provider handoff preflight manual-ready only, provider-neutral, unsubmitted, and unapproved."))
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
        add("$.imageCandidatePromptValidationReport.promptItems[$index].assetType", item.assetType)
        add("$.imageCandidatePromptValidationReport.promptItems[$index].positivePromptDraft", item.positivePromptDraft)
        add("$.imageCandidatePromptValidationReport.promptItems[$index].negativePromptDraft", item.negativePromptDraft)
        item.doNotShow.forEachIndexed { itemIndex, value -> add("$.imageCandidatePromptValidationReport.promptItems[$index].doNotShow[$itemIndex]", value) }
        item.promptSafetyNotes.forEachIndexed { itemIndex, value -> add("$.imageCandidatePromptValidationReport.promptItems[$index].promptSafetyNotes[$itemIndex]", value) }
        item.publicVisualRequirements.forEachIndexed { itemIndex, value -> add("$.imageCandidatePromptValidationReport.promptItems[$index].publicVisualRequirements[$itemIndex]", value) }
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
    ): ImageCandidateProviderHandoffPreflightFinding =
        ImageCandidateProviderHandoffPreflightFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private data class TextField(val path: String, val value: String)
}
