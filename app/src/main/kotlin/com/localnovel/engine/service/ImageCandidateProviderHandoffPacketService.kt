package com.localnovel.engine.service

import com.localnovel.engine.model.ImageCandidatePromptItem
import com.localnovel.engine.model.ImageCandidateProviderHandoffDecisionIntakeReport
import com.localnovel.engine.model.ImageCandidateProviderHandoffPacket
import com.localnovel.engine.model.ImageCandidateProviderHandoffPacketEvidence
import com.localnovel.engine.model.ImageCandidateProviderHandoffPacketFinding
import com.localnovel.engine.model.ImageCandidateProviderHandoffPreflightReport

class ImageCandidateProviderHandoffPacketService {
    private val requiredPreflightStep = "image_candidate_manual_provider_handoff_ready"
    private val requiredDecisionStep = "image_candidate_manual_provider_handoff_packet"
    private val requiredDecisionStatus = "ALLOW_MANUAL_PROVIDER_HANDOFF_PACKET"
    private val nextAllowedDownstreamStep = "image_candidate_provider_handoff_packet_validation"
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

    fun create(
        providerHandoffPreflight: ImageCandidateProviderHandoffPreflightReport,
        decisionIntake: ImageCandidateProviderHandoffDecisionIntakeReport,
        packetId: String = "",
        providerHandoffPreflightFile: String = "",
        providerHandoffDecisionIntakeFile: String = "",
    ): ImageCandidateProviderHandoffPacket {
        val findings = mutableListOf<ImageCandidateProviderHandoffPacketFinding>()
        validatePreflight(providerHandoffPreflight, findings)
        validateDecisionIntake(decisionIntake, findings)
        val sourceCount = validateSourceBinding(providerHandoffPreflight, decisionIntake, providerHandoffPreflightFile, providerHandoffDecisionIntakeFile, findings)
        validatePromptItems(providerHandoffPreflight.promptItems, findings)
        val instructions = manualInstructions()
        val forbiddenOutputFieldCount = validateForbiddenOutputFields(providerHandoffPreflight.promptItems, findings)
        val forbiddenStatusCount = scanText(providerHandoffPreflight.promptItems, instructions, forbiddenStatusPatterns, "PROMPT_STATUS_UPGRADE_FORBIDDEN", "provider_handoff_boundary", "Provider handoff packet cannot contain approved, locked, final, review-ready, production-ready, approved-reference, or production-locked wording.", findings)
        val forbiddenClaimCount = scanText(providerHandoffPreflight.promptItems, instructions, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", "Provider handoff packet cannot claim prompt submission, generated media, asset approval, production lock, canon application, or runtime scene creation.", findings)
        val authorOnlyLeakCount = scanText(providerHandoffPreflight.promptItems, instructions, authorOnlyPatterns, "AUTHOR_ONLY_PROMPT_TEXT_FORBIDDEN", "prompt_safety", "Provider handoff packet cannot carry author-only, hidden truth, secret cause, hidden motive, or future reveal text.", findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") {
            findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId }
        } else {
            ""
        }
        val resolvedPacketId = packetId.ifBlank {
            decisionIntake.decisionId.takeIf { it.isNotBlank() }?.let { "$it-packet" }.orEmpty()
        }

        return ImageCandidateProviderHandoffPacket(
            verdict = verdict,
            packetId = resolvedPacketId,
            providerHandoffPreflightFile = providerHandoffPreflightFile,
            providerHandoffDecisionIntakeFile = providerHandoffDecisionIntakeFile,
            decisionId = decisionIntake.decisionId,
            preflightId = decisionIntake.preflightId,
            imageCandidatePromptValidationReportFile = providerHandoffPreflight.imageCandidatePromptValidationReportFile,
            imageCandidatePromptPreparationFile = providerHandoffPreflight.imageCandidatePromptPreparationFile,
            preparationId = providerHandoffPreflight.preparationId,
            imageCandidateBriefFile = providerHandoffPreflight.imageCandidateBriefFile,
            imageCandidateBriefId = providerHandoffPreflight.imageCandidateBriefId,
            imageCandidatePreflightFile = providerHandoffPreflight.imageCandidatePreflightFile,
            imageCandidatePreflightId = providerHandoffPreflight.imageCandidatePreflightId,
            assetCandidateHumanReviewDecisionIntakeFile = providerHandoffPreflight.assetCandidateHumanReviewDecisionIntakeFile,
            assetDecisionId = providerHandoffPreflight.decisionId,
            acceptedScriptPacketId = providerHandoffPreflight.acceptedScriptPacketId,
            scriptPacketDraftFile = providerHandoffPreflight.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = providerHandoffPreflight.scriptPacketDraftContentSha256,
            promptItems = if (verdict == "FAIL") emptyList() else providerHandoffPreflight.promptItems,
            manualInstructions = if (verdict == "FAIL") emptyList() else instructions,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = ImageCandidateProviderHandoffPacketEvidence(
                providerHandoffPreflightVerdict = providerHandoffPreflight.verdict,
                decisionIntakeVerdict = decisionIntake.verdict,
                decisionAllowedDownstream = decisionIntake.decisionAllowedDownstream,
                sourceBindingFieldCount = sourceCount,
                promptItemCount = providerHandoffPreflight.promptItems.size,
                manualInstructionCount = instructions.size,
                authorOnlyLeakCount = authorOnlyLeakCount,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
                forbiddenOutputFieldCount = forbiddenOutputFieldCount,
            ),
        )
    }

    private fun validatePreflight(preflight: ImageCandidateProviderHandoffPreflightReport, findings: MutableList<ImageCandidateProviderHandoffPacketFinding>) {
        if (preflight.verdict.trim().uppercase() != "PASS") {
            findings.add(finding("PROVIDER_HANDOFF_PREFLIGHT_NOT_PASS", "upstream", "CRITICAL", "$.providerHandoffPreflight.verdict", "Provider handoff packet requires a PASS preflight.", evidence = listOf(preflight.verdict), suggestedFix = "Use the PASS provider handoff preflight."))
        }
        if (preflight.nextAllowedDownstreamStep != requiredPreflightStep) {
            findings.add(finding("PROVIDER_HANDOFF_PREFLIGHT_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.providerHandoffPreflight.nextAllowedDownstreamStep", "Provider handoff packet requires nextAllowedDownstreamStep=image_candidate_manual_provider_handoff_ready.", evidence = listOf(preflight.nextAllowedDownstreamStep), suggestedFix = "Use the preflight that explicitly allows manual provider handoff readiness."))
        }
    }

    private fun validateDecisionIntake(intake: ImageCandidateProviderHandoffDecisionIntakeReport, findings: MutableList<ImageCandidateProviderHandoffPacketFinding>) {
        if (intake.verdict.trim().uppercase() != "PASS") {
            findings.add(finding("PROVIDER_HANDOFF_DECISION_INTAKE_NOT_PASS", "upstream", "CRITICAL", "$.decisionIntake.verdict", "Provider handoff packet requires a PASS decision intake report.", evidence = listOf(intake.verdict), suggestedFix = "Use the PASS provider handoff decision intake report."))
        }
        if (!intake.decisionAllowedDownstream) {
            findings.add(finding("PROVIDER_HANDOFF_DECISION_NOT_ALLOWED_DOWNSTREAM", "upstream", "CRITICAL", "$.decisionIntake.decisionAllowedDownstream", "Provider handoff packet requires decisionAllowedDownstream=true.", suggestedFix = "Use an ALLOW_MANUAL_PROVIDER_HANDOFF_PACKET decision intake report."))
        }
        if (intake.decisionStatus != requiredDecisionStatus) {
            findings.add(finding("PROVIDER_HANDOFF_DECISION_STATUS_MISMATCH", "upstream", "CRITICAL", "$.decisionIntake.decisionStatus", "Provider handoff packet requires decisionStatus=ALLOW_MANUAL_PROVIDER_HANDOFF_PACKET.", evidence = listOf(intake.decisionStatus), suggestedFix = "Use the narrow allow-packet decision status."))
        }
        if (intake.nextAllowedDownstreamStep != requiredDecisionStep) {
            findings.add(finding("PROVIDER_HANDOFF_DECISION_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.decisionIntake.nextAllowedDownstreamStep", "Provider handoff packet requires nextAllowedDownstreamStep=image_candidate_manual_provider_handoff_packet.", evidence = listOf(intake.nextAllowedDownstreamStep), suggestedFix = "Use the decision intake report that explicitly allows packet creation."))
        }
    }

    private fun validateSourceBinding(
        preflight: ImageCandidateProviderHandoffPreflightReport,
        intake: ImageCandidateProviderHandoffDecisionIntakeReport,
        preflightFile: String,
        intakeFile: String,
        findings: MutableList<ImageCandidateProviderHandoffPacketFinding>,
    ): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) {
                findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Provider handoff packet requires nonblank $label.", suggestedFix = "Use matching preflight and decision intake reports."))
            } else {
                count += 1
            }
        }
        fun requireMatch(preflightValue: String, intakeValue: String, preflightPath: String, intakePath: String, missingRuleId: String, mismatchRuleId: String, label: String, normalize: (String) -> String = { it.trim() }) {
            requireNonBlank(preflightValue, preflightPath, "${missingRuleId}_PREFLIGHT", "$label in preflight")
            requireNonBlank(intakeValue, intakePath, missingRuleId, "$label in decision intake")
            val left = normalize(preflightValue)
            val right = normalize(intakeValue)
            if (left.isNotBlank() && right.isNotBlank() && left != right) {
                findings.add(finding(mismatchRuleId, "source_linkage", "CRITICAL", intakePath, "Provider handoff packet source binding mismatch for $label.", evidence = listOf("preflight=$preflightValue", "intake=$intakeValue"), suggestedFix = "Use reports from the same handoff chain."))
            }
        }
        requireNonBlank(preflightFile, "$.inputs.providerHandoffPreflightFile", "PROVIDER_HANDOFF_PREFLIGHT_FILE_MISSING", "provider handoff preflight file")
        requireNonBlank(intakeFile, "$.inputs.providerHandoffDecisionIntakeFile", "PROVIDER_HANDOFF_DECISION_INTAKE_FILE_MISSING", "provider handoff decision intake file")
        requireNonBlank(intake.decisionId, "$.decisionIntake.decisionId", "DECISION_ID_MISSING", "decision id")
        requireMatch(preflight.preflightId, intake.preflightId, "$.providerHandoffPreflight.preflightId", "$.decisionIntake.preflightId", "PREFLIGHT_ID_MISSING", "PREFLIGHT_ID_MISMATCH", "preflight id")
        requireMatch(preflight.imageCandidatePromptValidationReportFile, intake.imageCandidatePromptValidationReportFile, "$.providerHandoffPreflight.imageCandidatePromptValidationReportFile", "$.decisionIntake.imageCandidatePromptValidationReportFile", "PROMPT_VALIDATION_REPORT_FILE_MISSING", "PROMPT_VALIDATION_REPORT_FILE_MISMATCH", "prompt validation report file", ::normalizePath)
        requireMatch(preflight.imageCandidatePromptPreparationFile, intake.imageCandidatePromptPreparationFile, "$.providerHandoffPreflight.imageCandidatePromptPreparationFile", "$.decisionIntake.imageCandidatePromptPreparationFile", "PROMPT_PREPARATION_FILE_MISSING", "PROMPT_PREPARATION_FILE_MISMATCH", "prompt preparation file", ::normalizePath)
        requireMatch(preflight.preparationId, intake.preparationId, "$.providerHandoffPreflight.preparationId", "$.decisionIntake.preparationId", "PROMPT_PREPARATION_ID_MISSING", "PROMPT_PREPARATION_ID_MISMATCH", "prompt preparation id")
        requireMatch(preflight.acceptedScriptPacketId, intake.acceptedScriptPacketId, "$.providerHandoffPreflight.acceptedScriptPacketId", "$.decisionIntake.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "ACCEPTED_SCRIPT_PACKET_ID_MISMATCH", "accepted script packet id")
        requireMatch(preflight.scriptPacketDraftFile, intake.scriptPacketDraftFile, "$.providerHandoffPreflight.scriptPacketDraftFile", "$.decisionIntake.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "SCRIPT_PACKET_DRAFT_FILE_MISMATCH", "script packet draft file", ::normalizePath)
        requireMatch(preflight.scriptPacketDraftContentSha256, intake.scriptPacketDraftContentSha256, "$.providerHandoffPreflight.scriptPacketDraftContentSha256", "$.decisionIntake.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "SCRIPT_PACKET_DRAFT_HASH_MISMATCH", "script packet draft SHA-256", { it.trim().lowercase() })
        if (preflight.scriptPacketDraftContentSha256.isNotBlank() && !sha256Regex.matches(preflight.scriptPacketDraftContentSha256.trim().lowercase())) {
            findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.providerHandoffPreflight.scriptPacketDraftContentSha256", "Provider handoff packet requires a valid lowercase SHA-256 script packet draft hash.", evidence = listOf(preflight.scriptPacketDraftContentSha256), suggestedFix = "Use the hash carried by the preflight."))
        }
        return count
    }

    private fun validatePromptItems(items: List<ImageCandidatePromptItem>, findings: MutableList<ImageCandidateProviderHandoffPacketFinding>) {
        if (items.isEmpty()) {
            findings.add(finding("PROMPT_ITEMS_MISSING", "provider_handoff_boundary", "CRITICAL", "$.providerHandoffPreflight.promptItems", "Provider handoff packet requires prompt items.", suggestedFix = "Use a preflight with validated prompt items."))
        }
        items.forEachIndexed { index, item ->
            if (item.promptItemId.isBlank() || item.sourceCandidateId.isBlank() || item.positivePromptDraft.isBlank() || (item.negativePromptDraft.isBlank() && item.doNotShow.isEmpty()) || item.promptSafetyNotes.isEmpty()) {
                findings.add(finding("PROMPT_ITEM_INCOMPLETE", "provider_handoff_boundary", "CRITICAL", "$.providerHandoffPreflight.promptItems[$index]", "Provider handoff packet requires prompt id, candidate id, positive prompt, negative/do-not-show, and safety notes.", suggestedFix = "Use complete validated prompt items."))
            }
        }
    }

    private fun manualInstructions(): List<String> =
        listOf(
            "Manual copy only: inspect the prompt item before using any external tool.",
            "Keep negative prompt and do-not-show boundary attached to each prompt item.",
            "Do not record provider task metadata or image file paths in this packet.",
            "Return for a later output intake gate after any external human action.",
        )

    private fun validateForbiddenOutputFields(items: List<ImageCandidatePromptItem>, findings: MutableList<ImageCandidateProviderHandoffPacketFinding>): Int {
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
                    findings.add(finding(ruleId, "safety_boundary", "CRITICAL", "$.providerHandoffPreflight.promptItems[$index].$field", "Provider handoff packet cannot carry provider, generation API, job, or image output path information.", evidence = listOf(value), suggestedFix = "Remove provider/job/output details before packet creation."))
                }
            }
        }
        return count
    }

    private fun scanText(items: List<ImageCandidatePromptItem>, instructions: List<String>, patterns: List<Regex>, ruleId: String, category: String, message: String, findings: MutableList<ImageCandidateProviderHandoffPacketFinding>): Int {
        var count = 0
        collectTextFields(items, instructions).forEach { field ->
            patterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings.add(finding(ruleId, category, "CRITICAL", field.path, message, evidence = listOf(field.value), suggestedFix = "Keep the handoff packet manual, unsubmitted, output-free, and unapproved."))
                }
            }
        }
        return count
    }

    private fun collectTextFields(items: List<ImageCandidatePromptItem>, instructions: List<String>): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) {
            if (value.isNotBlank()) fields += TextField(path, value)
        }
        items.forEachIndexed { index, item ->
            add("$.providerHandoffPreflight.promptItems[$index].assetType", item.assetType)
            add("$.providerHandoffPreflight.promptItems[$index].positivePromptDraft", item.positivePromptDraft)
            add("$.providerHandoffPreflight.promptItems[$index].negativePromptDraft", item.negativePromptDraft)
            item.doNotShow.forEachIndexed { itemIndex, value -> add("$.providerHandoffPreflight.promptItems[$index].doNotShow[$itemIndex]", value) }
            item.promptSafetyNotes.forEachIndexed { itemIndex, value -> add("$.providerHandoffPreflight.promptItems[$index].promptSafetyNotes[$itemIndex]", value) }
            item.publicVisualRequirements.forEachIndexed { itemIndex, value -> add("$.providerHandoffPreflight.promptItems[$index].publicVisualRequirements[$itemIndex]", value) }
        }
        instructions.forEachIndexed { index, instruction -> add("$.providerHandoffPacket.manualInstructions[$index]", instruction) }
        return fields
    }

    private fun finding(ruleId: String, category: String, severity: String, path: String, message: String, evidence: List<String> = emptyList(), suggestedFix: String = ""): ImageCandidateProviderHandoffPacketFinding =
        ImageCandidateProviderHandoffPacketFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private fun normalizePath(value: String): String = value.trim().replace('\\', '/')

    private data class TextField(val path: String, val value: String)
}
