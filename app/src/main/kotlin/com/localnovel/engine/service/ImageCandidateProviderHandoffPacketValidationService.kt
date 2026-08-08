package com.localnovel.engine.service

import com.localnovel.engine.model.ImageCandidatePromptItem
import com.localnovel.engine.model.ImageCandidateProviderHandoffPacket
import com.localnovel.engine.model.ImageCandidateProviderHandoffPacketValidationEvidence
import com.localnovel.engine.model.ImageCandidateProviderHandoffPacketValidationFinding
import com.localnovel.engine.model.ImageCandidateProviderHandoffPacketValidationReport

class ImageCandidateProviderHandoffPacketValidationService {
    private val requiredPacketNextStep = "image_candidate_provider_handoff_packet_validation"
    private val nextAllowedDownstreamStep = "image_candidate_manual_external_action_ready"
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
        providerHandoffPacket: ImageCandidateProviderHandoffPacket,
        providerHandoffPacketFile: String = "",
    ): ImageCandidateProviderHandoffPacketValidationReport {
        val findings = mutableListOf<ImageCandidateProviderHandoffPacketValidationFinding>()
        validatePacket(providerHandoffPacket, findings)
        val sourceCount = validateSourceFields(providerHandoffPacket, providerHandoffPacketFile, findings)
        validatePromptItems(providerHandoffPacket.promptItems, findings)
        validateManualInstructions(providerHandoffPacket.manualInstructions, findings)
        val forbiddenOutputFieldCount = validateForbiddenOutputFields(providerHandoffPacket, findings)
        val forbiddenStatusCount = scanText(providerHandoffPacket.promptItems, providerHandoffPacket.manualInstructions, forbiddenStatusPatterns, "PROMPT_STATUS_UPGRADE_FORBIDDEN", "provider_handoff_boundary", "Provider handoff packet validation cannot allow approved, locked, final, review-ready, production-ready, approved-reference, or production-locked wording.", findings)
        val forbiddenClaimCount = scanText(providerHandoffPacket.promptItems, providerHandoffPacket.manualInstructions, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", "Provider handoff packet validation cannot allow prompt submission, generated media, asset approval, production lock, canon application, or runtime scene creation claims.", findings)
        val authorOnlyLeakCount = scanText(providerHandoffPacket.promptItems, providerHandoffPacket.manualInstructions, authorOnlyPatterns, "AUTHOR_ONLY_PROMPT_TEXT_FORBIDDEN", "prompt_safety", "Provider handoff packet validation cannot carry author-only, hidden truth, secret cause, hidden motive, or future reveal text.", findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") {
            findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId }
        } else {
            ""
        }

        return ImageCandidateProviderHandoffPacketValidationReport(
            verdict = verdict,
            providerHandoffPacketFile = providerHandoffPacketFile,
            packetId = providerHandoffPacket.packetId,
            providerHandoffPreflightFile = providerHandoffPacket.providerHandoffPreflightFile,
            providerHandoffDecisionIntakeFile = providerHandoffPacket.providerHandoffDecisionIntakeFile,
            decisionId = providerHandoffPacket.decisionId,
            preflightId = providerHandoffPacket.preflightId,
            imageCandidatePromptValidationReportFile = providerHandoffPacket.imageCandidatePromptValidationReportFile,
            imageCandidatePromptPreparationFile = providerHandoffPacket.imageCandidatePromptPreparationFile,
            preparationId = providerHandoffPacket.preparationId,
            acceptedScriptPacketId = providerHandoffPacket.acceptedScriptPacketId,
            scriptPacketDraftFile = providerHandoffPacket.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = providerHandoffPacket.scriptPacketDraftContentSha256,
            promptItems = if (verdict == "FAIL") emptyList() else providerHandoffPacket.promptItems,
            manualInstructions = if (verdict == "FAIL") emptyList() else providerHandoffPacket.manualInstructions,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = ImageCandidateProviderHandoffPacketValidationEvidence(
                packetVerdict = providerHandoffPacket.verdict,
                packetNextAllowedDownstreamStep = providerHandoffPacket.nextAllowedDownstreamStep,
                sourceFieldCount = sourceCount,
                promptItemCount = providerHandoffPacket.promptItems.size,
                manualInstructionCount = providerHandoffPacket.manualInstructions.size,
                authorOnlyLeakCount = authorOnlyLeakCount,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
                forbiddenOutputFieldCount = forbiddenOutputFieldCount,
            ),
        )
    }

    private fun validatePacket(packet: ImageCandidateProviderHandoffPacket, findings: MutableList<ImageCandidateProviderHandoffPacketValidationFinding>) {
        if (packet.verdict.trim().uppercase() != "PASS") {
            findings.add(finding("PROVIDER_HANDOFF_PACKET_NOT_PASS", "upstream", "CRITICAL", "$.providerHandoffPacket.verdict", "Provider handoff packet validation requires a PASS packet.", evidence = listOf(packet.verdict), suggestedFix = "Use the PASS provider handoff packet."))
        }
        if (packet.nextAllowedDownstreamStep != requiredPacketNextStep) {
            findings.add(finding("PROVIDER_HANDOFF_PACKET_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.providerHandoffPacket.nextAllowedDownstreamStep", "Provider handoff packet validation requires nextAllowedDownstreamStep=image_candidate_provider_handoff_packet_validation.", evidence = listOf(packet.nextAllowedDownstreamStep), suggestedFix = "Use the packet that explicitly allows validation."))
        }
    }

    private fun validateSourceFields(packet: ImageCandidateProviderHandoffPacket, packetFile: String, findings: MutableList<ImageCandidateProviderHandoffPacketValidationFinding>): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) {
                findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Provider handoff packet validation requires nonblank $label.", suggestedFix = "Use a complete provider handoff packet."))
            } else {
                count += 1
            }
        }
        requireNonBlank(packetFile, "$.inputs.providerHandoffPacketFile", "PROVIDER_HANDOFF_PACKET_FILE_MISSING", "provider handoff packet file")
        requireNonBlank(packet.packetId, "$.providerHandoffPacket.packetId", "PROVIDER_HANDOFF_PACKET_ID_MISSING", "packet id")
        requireNonBlank(packet.providerHandoffPreflightFile, "$.providerHandoffPacket.providerHandoffPreflightFile", "PROVIDER_HANDOFF_PREFLIGHT_FILE_MISSING", "provider handoff preflight file")
        requireNonBlank(packet.providerHandoffDecisionIntakeFile, "$.providerHandoffPacket.providerHandoffDecisionIntakeFile", "PROVIDER_HANDOFF_DECISION_INTAKE_FILE_MISSING", "provider handoff decision intake file")
        requireNonBlank(packet.decisionId, "$.providerHandoffPacket.decisionId", "DECISION_ID_MISSING", "decision id")
        requireNonBlank(packet.preflightId, "$.providerHandoffPacket.preflightId", "PREFLIGHT_ID_MISSING", "preflight id")
        requireNonBlank(packet.imageCandidatePromptValidationReportFile, "$.providerHandoffPacket.imageCandidatePromptValidationReportFile", "PROMPT_VALIDATION_REPORT_FILE_MISSING", "prompt validation report file")
        requireNonBlank(packet.imageCandidatePromptPreparationFile, "$.providerHandoffPacket.imageCandidatePromptPreparationFile", "PROMPT_PREPARATION_FILE_MISSING", "prompt preparation file")
        requireNonBlank(packet.preparationId, "$.providerHandoffPacket.preparationId", "PROMPT_PREPARATION_ID_MISSING", "prompt preparation id")
        requireNonBlank(packet.acceptedScriptPacketId, "$.providerHandoffPacket.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(packet.scriptPacketDraftFile, "$.providerHandoffPacket.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(packet.scriptPacketDraftContentSha256, "$.providerHandoffPacket.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        if (packet.scriptPacketDraftContentSha256.isNotBlank() && !sha256Regex.matches(packet.scriptPacketDraftContentSha256.trim().lowercase())) {
            findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.providerHandoffPacket.scriptPacketDraftContentSha256", "Provider handoff packet validation requires a valid lowercase SHA-256 script packet draft hash.", evidence = listOf(packet.scriptPacketDraftContentSha256), suggestedFix = "Use the hash carried by the packet."))
        }
        return count
    }

    private fun validatePromptItems(items: List<ImageCandidatePromptItem>, findings: MutableList<ImageCandidateProviderHandoffPacketValidationFinding>) {
        if (items.isEmpty()) {
            findings.add(finding("PROMPT_ITEMS_MISSING", "provider_handoff_boundary", "CRITICAL", "$.providerHandoffPacket.promptItems", "Provider handoff packet validation requires prompt items.", suggestedFix = "Use a packet with validated prompt items."))
        }
        items.forEachIndexed { index, item ->
            if (item.promptItemId.isBlank() || item.sourceCandidateId.isBlank() || item.positivePromptDraft.isBlank() || (item.negativePromptDraft.isBlank() && item.doNotShow.isEmpty()) || item.promptSafetyNotes.isEmpty()) {
                findings.add(finding("PROMPT_ITEM_INCOMPLETE", "provider_handoff_boundary", "CRITICAL", "$.providerHandoffPacket.promptItems[$index]", "Provider handoff packet validation requires prompt id, candidate id, positive prompt, negative/do-not-show, and safety notes.", suggestedFix = "Use complete packet prompt items."))
            }
        }
    }

    private fun validateManualInstructions(instructions: List<String>, findings: MutableList<ImageCandidateProviderHandoffPacketValidationFinding>) {
        if (instructions.isEmpty()) {
            findings.add(finding("MANUAL_INSTRUCTIONS_MISSING", "provider_handoff_boundary", "CRITICAL", "$.providerHandoffPacket.manualInstructions", "Provider handoff packet validation requires manual instructions.", suggestedFix = "Keep manual copy and no-output instructions attached."))
        }
    }

    private fun validateForbiddenOutputFields(packet: ImageCandidateProviderHandoffPacket, findings: MutableList<ImageCandidateProviderHandoffPacketValidationFinding>): Int {
        var count = 0
        listOf(
            Triple(packet.providerApiEndpoint, "providerApiEndpoint", "PROVIDER_API_ENDPOINT_FORBIDDEN"),
            Triple(packet.providerJobId, "providerJobId", "PROVIDER_JOB_ID_FORBIDDEN"),
            Triple(packet.submittedAt, "submittedAt", "SUBMITTED_AT_FORBIDDEN"),
            Triple(packet.outputPath, "outputPath", "OUTPUT_PATH_FORBIDDEN"),
            Triple(packet.generatedStatus, "generatedStatus", "GENERATED_STATUS_FORBIDDEN"),
        ).forEach { (value, field, ruleId) ->
            if (value.isNotBlank()) {
                count += 1
                findings.add(finding(ruleId, "safety_boundary", "CRITICAL", "$.providerHandoffPacket.$field", "Provider handoff packet validation cannot carry provider API, job, submission, output path, or generated status.", evidence = listOf(value), suggestedFix = "Remove provider/job/submission/output/generated fields."))
            }
        }
        packet.promptItems.forEachIndexed { index, item ->
            listOf(
                Triple(item.providerName, "providerName", "PROVIDER_NAME_FORBIDDEN"),
                Triple(item.generationApi, "generationApi", "GENERATION_API_FORBIDDEN"),
                Triple(item.providerJobId, "providerJobId", "PROVIDER_JOB_ID_FORBIDDEN"),
                Triple(item.imageOutputPath, "imageOutputPath", "IMAGE_OUTPUT_PATH_FORBIDDEN"),
            ).forEach { (value, field, ruleId) ->
                if (value.isNotBlank()) {
                    count += 1
                    findings.add(finding(ruleId, "safety_boundary", "CRITICAL", "$.providerHandoffPacket.promptItems[$index].$field", "Provider handoff packet validation cannot carry provider, generation API, job, or image output path information.", evidence = listOf(value), suggestedFix = "Remove provider/job/output details."))
                }
            }
        }
        return count
    }

    private fun scanText(items: List<ImageCandidatePromptItem>, instructions: List<String>, patterns: List<Regex>, ruleId: String, category: String, message: String, findings: MutableList<ImageCandidateProviderHandoffPacketValidationFinding>): Int {
        var count = 0
        collectTextFields(items, instructions).forEach { field ->
            patterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings.add(finding(ruleId, category, "CRITICAL", field.path, message, evidence = listOf(field.value), suggestedFix = "Keep the packet manual, unsubmitted, output-free, and unapproved."))
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
            add("$.providerHandoffPacket.promptItems[$index].assetType", item.assetType)
            add("$.providerHandoffPacket.promptItems[$index].positivePromptDraft", item.positivePromptDraft)
            add("$.providerHandoffPacket.promptItems[$index].negativePromptDraft", item.negativePromptDraft)
            item.doNotShow.forEachIndexed { itemIndex, value -> add("$.providerHandoffPacket.promptItems[$index].doNotShow[$itemIndex]", value) }
            item.promptSafetyNotes.forEachIndexed { itemIndex, value -> add("$.providerHandoffPacket.promptItems[$index].promptSafetyNotes[$itemIndex]", value) }
            item.publicVisualRequirements.forEachIndexed { itemIndex, value -> add("$.providerHandoffPacket.promptItems[$index].publicVisualRequirements[$itemIndex]", value) }
        }
        instructions.forEachIndexed { index, instruction -> add("$.providerHandoffPacket.manualInstructions[$index]", instruction) }
        return fields
    }

    private fun finding(ruleId: String, category: String, severity: String, path: String, message: String, evidence: List<String> = emptyList(), suggestedFix: String = ""): ImageCandidateProviderHandoffPacketValidationFinding =
        ImageCandidateProviderHandoffPacketValidationFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private data class TextField(val path: String, val value: String)
}
