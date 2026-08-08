package com.localnovel.engine.service

import com.localnovel.engine.model.AssetCandidateHumanReviewDecisionIntakeReport
import com.localnovel.engine.model.ImageCandidatePreflightEvidence
import com.localnovel.engine.model.ImageCandidatePreflightFinding
import com.localnovel.engine.model.ImageCandidatePreflightReport
import com.localnovel.engine.model.ImageCandidateRequirement

class ImageCandidatePreflightService {
    private val requiredDecisionStatus = "ALLOW_IMAGE_CANDIDATE_PREFLIGHT"
    private val requiredDecisionNextStep = "image_candidate_preflight"
    private val nextAllowedDownstreamStep = "image_candidate_brief"
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

    fun validate(
        decisionIntake: AssetCandidateHumanReviewDecisionIntakeReport,
        preflightId: String = "",
        decisionIntakeFile: String = "",
    ): ImageCandidatePreflightReport {
        val findings = mutableListOf<ImageCandidatePreflightFinding>()
        validateDecisionIntake(decisionIntake, findings)
        val sourceCount = validateSourceFields(decisionIntake, decisionIntakeFile, findings)
        validateImageCandidateRequirements(decisionIntake.imageCandidateRequirements, findings)
        val forbiddenStatusCount = validateForbiddenStatusText(decisionIntake.imageCandidateRequirements, findings)
        val forbiddenOutputFieldCount = validateForbiddenOutputFields(decisionIntake.imageCandidateRequirements, findings)
        val forbiddenClaimCount = validateForbiddenClaims(decisionIntake.imageCandidateRequirements, findings)

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
            decisionIntake.decisionId
                .takeIf { it.isNotBlank() }
                ?.let { "$it-image-candidate-preflight" }
                .orEmpty()
        }

        return ImageCandidatePreflightReport(
            verdict = verdict,
            preflightId = resolvedPreflightId,
            assetCandidateHumanReviewDecisionIntakeFile = decisionIntakeFile,
            decisionId = decisionIntake.decisionId,
            assetCandidateReviewPreflightFile = decisionIntake.assetCandidateReviewPreflightFile,
            assetCandidateReviewPreflightId = decisionIntake.preflightId,
            assetCandidateDraftValidationReportFile = decisionIntake.assetCandidateDraftValidationReportFile,
            assetCandidateDraftFile = decisionIntake.assetCandidateDraftFile,
            assetCandidateDraftId = decisionIntake.assetCandidateDraftId,
            assetCandidateBriefFile = decisionIntake.assetCandidateBriefFile,
            assetCandidateBriefId = decisionIntake.assetCandidateBriefId,
            assetCandidatePreflightFile = decisionIntake.assetCandidatePreflightFile,
            assetCandidatePreflightId = decisionIntake.assetCandidatePreflightId,
            humanReviewDecisionIntakeFile = decisionIntake.humanReviewDecisionIntakeFile,
            humanReviewDecisionId = decisionIntake.humanReviewDecisionId,
            storyboardPacketReviewFile = decisionIntake.storyboardPacketReviewFile,
            storyboardPacketReviewId = decisionIntake.storyboardPacketReviewId,
            acceptedScriptPacketId = decisionIntake.acceptedScriptPacketId,
            scriptPacketDraftFile = decisionIntake.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = decisionIntake.scriptPacketDraftContentSha256,
            imageCandidateRequirements = if (verdict == "FAIL") emptyList() else decisionIntake.imageCandidateRequirements,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = ImageCandidatePreflightEvidence(
                decisionIntakeVerdict = decisionIntake.verdict,
                decisionAllowedDownstream = decisionIntake.decisionAllowedDownstream,
                decisionStatus = decisionIntake.decisionStatus,
                decisionNextAllowedDownstreamStep = decisionIntake.nextAllowedDownstreamStep,
                sourceFieldCount = sourceCount,
                imageCandidateRequirementCount = decisionIntake.imageCandidateRequirements.size,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
                forbiddenOutputFieldCount = forbiddenOutputFieldCount,
            ),
        )
    }

    private fun validateDecisionIntake(
        report: AssetCandidateHumanReviewDecisionIntakeReport,
        findings: MutableList<ImageCandidatePreflightFinding>,
    ) {
        if (report.verdict.trim().uppercase() != "PASS") {
            findings.add(finding("ASSET_CANDIDATE_HUMAN_REVIEW_DECISION_INTAKE_NOT_PASS", "upstream", "CRITICAL", "$.decisionIntake.verdict", "Image candidate preflight requires a PASS asset candidate human review decision intake report.", evidence = listOf(report.verdict), suggestedFix = "Use the PASS decision intake report."))
        }
        if (!report.decisionAllowedDownstream) {
            findings.add(finding("DECISION_NOT_DOWNSTREAM_ALLOWED", "upstream", "CRITICAL", "$.decisionIntake.decisionAllowedDownstream", "Image candidate preflight requires decisionAllowedDownstream=true.", evidence = listOf(report.decisionAllowedDownstream.toString()), suggestedFix = "Use a human decision intake report that explicitly allows image candidate preflight."))
        }
        if (report.decisionStatus != requiredDecisionStatus) {
            findings.add(finding("DECISION_STATUS_MISMATCH", "upstream", "CRITICAL", "$.decisionIntake.decisionStatus", "Image candidate preflight requires decisionStatus=ALLOW_IMAGE_CANDIDATE_PREFLIGHT.", evidence = listOf(report.decisionStatus), suggestedFix = "Use an allow-image-candidate-preflight decision intake report."))
        }
        if (report.nextAllowedDownstreamStep != requiredDecisionNextStep) {
            findings.add(finding("DECISION_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.decisionIntake.nextAllowedDownstreamStep", "Image candidate preflight requires nextAllowedDownstreamStep=image_candidate_preflight.", evidence = listOf(report.nextAllowedDownstreamStep), suggestedFix = "Use the decision intake report that explicitly allows image_candidate_preflight."))
        }
    }

    private fun validateSourceFields(
        report: AssetCandidateHumanReviewDecisionIntakeReport,
        reportFile: String,
        findings: MutableList<ImageCandidatePreflightFinding>,
    ): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) {
                findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Image candidate preflight requires nonblank $label.", suggestedFix = "Use the complete decision intake report."))
            } else {
                count += 1
            }
        }
        requireNonBlank(reportFile, "$.inputs.assetCandidateHumanReviewDecisionIntakeFile", "ASSET_CANDIDATE_HUMAN_REVIEW_DECISION_INTAKE_FILE_MISSING", "decision intake file")
        requireNonBlank(report.decisionId, "$.decisionIntake.decisionId", "DECISION_ID_MISSING", "decision id")
        requireNonBlank(report.preflightId, "$.decisionIntake.preflightId", "ASSET_CANDIDATE_REVIEW_PREFLIGHT_ID_MISSING", "asset candidate review preflight id")
        requireNonBlank(report.assetCandidateReviewPreflightFile, "$.decisionIntake.assetCandidateReviewPreflightFile", "ASSET_CANDIDATE_REVIEW_PREFLIGHT_FILE_MISSING", "asset candidate review preflight file")
        requireNonBlank(report.assetCandidateDraftValidationReportFile, "$.decisionIntake.assetCandidateDraftValidationReportFile", "ASSET_CANDIDATE_DRAFT_VALIDATION_REPORT_FILE_MISSING", "asset candidate draft validation report file")
        requireNonBlank(report.assetCandidateDraftFile, "$.decisionIntake.assetCandidateDraftFile", "ASSET_CANDIDATE_DRAFT_FILE_MISSING", "asset candidate draft file")
        requireNonBlank(report.assetCandidateDraftId, "$.decisionIntake.assetCandidateDraftId", "ASSET_CANDIDATE_DRAFT_ID_MISSING", "asset candidate draft id")
        requireNonBlank(report.assetCandidateBriefFile, "$.decisionIntake.assetCandidateBriefFile", "ASSET_CANDIDATE_BRIEF_FILE_MISSING", "asset candidate brief file")
        requireNonBlank(report.assetCandidateBriefId, "$.decisionIntake.assetCandidateBriefId", "ASSET_CANDIDATE_BRIEF_ID_MISSING", "asset candidate brief id")
        requireNonBlank(report.assetCandidatePreflightFile, "$.decisionIntake.assetCandidatePreflightFile", "ASSET_CANDIDATE_PREFLIGHT_FILE_MISSING", "asset candidate preflight file")
        requireNonBlank(report.assetCandidatePreflightId, "$.decisionIntake.assetCandidatePreflightId", "ASSET_CANDIDATE_PREFLIGHT_ID_MISSING", "asset candidate preflight id")
        requireNonBlank(report.humanReviewDecisionIntakeFile, "$.decisionIntake.humanReviewDecisionIntakeFile", "STORYBOARD_HUMAN_REVIEW_DECISION_INTAKE_FILE_MISSING", "storyboard human review decision intake file")
        requireNonBlank(report.humanReviewDecisionId, "$.decisionIntake.humanReviewDecisionId", "STORYBOARD_HUMAN_REVIEW_DECISION_ID_MISSING", "storyboard human review decision id")
        requireNonBlank(report.storyboardPacketReviewFile, "$.decisionIntake.storyboardPacketReviewFile", "STORYBOARD_PACKET_REVIEW_FILE_MISSING", "storyboard packet review file")
        requireNonBlank(report.storyboardPacketReviewId, "$.decisionIntake.storyboardPacketReviewId", "STORYBOARD_PACKET_REVIEW_ID_MISSING", "storyboard packet review id")
        requireNonBlank(report.acceptedScriptPacketId, "$.decisionIntake.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(report.scriptPacketDraftFile, "$.decisionIntake.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(report.scriptPacketDraftContentSha256, "$.decisionIntake.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        if (report.scriptPacketDraftContentSha256.isNotBlank() &&
            !sha256Regex.matches(report.scriptPacketDraftContentSha256.trim().lowercase())
        ) {
            findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.decisionIntake.scriptPacketDraftContentSha256", "Image candidate preflight requires a valid lowercase SHA-256 script packet draft hash.", evidence = listOf(report.scriptPacketDraftContentSha256), suggestedFix = "Use the hash carried by the decision intake report."))
        }
        return count
    }

    private fun validateImageCandidateRequirements(
        requirements: List<ImageCandidateRequirement>,
        findings: MutableList<ImageCandidatePreflightFinding>,
    ) {
        if (requirements.isEmpty()) {
            findings.add(finding("IMAGE_CANDIDATE_REQUIREMENTS_MISSING", "image_candidate_boundary", "CRITICAL", "$.decisionIntake.imageCandidateRequirements", "Image candidate preflight requires at least one image candidate requirement.", suggestedFix = "Use a decision intake report with candidate-only image requirements."))
            return
        }
        requirements.forEachIndexed { index, requirement ->
            if (requirement.requirementId.isBlank() || requirement.candidateId.isBlank() || requirement.assetType.isBlank() || requirement.sourceRequirementId.isBlank() || requirement.requirement.isBlank()) {
                findings.add(finding("IMAGE_CANDIDATE_REQUIREMENT_INCOMPLETE", "image_candidate_boundary", "CRITICAL", "$.decisionIntake.imageCandidateRequirements[$index]", "Image candidate preflight requires complete requirement id, candidate id, type, source requirement id, and requirement text.", suggestedFix = "Use complete image candidate requirements."))
            }
            if (requirement.referenceRequirements.isEmpty()) {
                findings.add(finding("IMAGE_CANDIDATE_REFERENCE_REQUIREMENTS_MISSING", "image_candidate_boundary", "CRITICAL", "$.decisionIntake.imageCandidateRequirements[$index].referenceRequirements", "Image candidate preflight requires reference requirements.", suggestedFix = "Declare needed views or candidate references."))
            }
            if (requirement.promptSafetyNotes.isEmpty()) {
                findings.add(finding("IMAGE_CANDIDATE_PROMPT_SAFETY_NOTES_MISSING", "image_candidate_boundary", "CRITICAL", "$.decisionIntake.imageCandidateRequirements[$index].promptSafetyNotes", "Image candidate preflight requires prompt safety notes.", suggestedFix = "Declare no author-only, no approval, and no generation safety notes."))
            }
            if (requirement.status.trim().lowercase() !in allowedRequirementStatuses) {
                findings.add(finding("IMAGE_CANDIDATE_STATUS_NOT_ALLOWED", "image_candidate_boundary", "CRITICAL", "$.decisionIntake.imageCandidateRequirements[$index].status", "Image candidate requirement status must remain candidate-only.", evidence = listOf(requirement.status), suggestedFix = "Use candidate, candidate_only, draft_candidate, or image_candidate."))
            }
        }
    }

    private fun validateForbiddenStatusText(
        requirements: List<ImageCandidateRequirement>,
        findings: MutableList<ImageCandidatePreflightFinding>,
    ): Int =
        scanRequirements(requirements, forbiddenStatusPatterns, "IMAGE_CANDIDATE_STATUS_FORBIDDEN", "image_candidate_boundary", "Image candidate preflight cannot contain approved, locked, final, review-ready, production-ready, approved-reference, or production-locked wording.", findings)

    private fun validateForbiddenClaims(
        requirements: List<ImageCandidateRequirement>,
        findings: MutableList<ImageCandidatePreflightFinding>,
    ): Int =
        scanRequirements(requirements, forbiddenClaimPatterns, "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", "Image candidate preflight cannot claim generated media, asset approval, production lock, canon application, or runtime scene creation.", findings)

    private fun validateForbiddenOutputFields(
        requirements: List<ImageCandidateRequirement>,
        findings: MutableList<ImageCandidatePreflightFinding>,
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
                    findings.add(finding(ruleId, "safety_boundary", "CRITICAL", "$.decisionIntake.imageCandidateRequirements[$index].$field", "Image candidate preflight cannot carry image output path, generation API, provider, or job information.", evidence = listOf(value), suggestedFix = "Remove provider/job/output details before preflight."))
                }
            }
        }
        return count
    }

    private fun scanRequirements(
        requirements: List<ImageCandidateRequirement>,
        patterns: List<Regex>,
        ruleId: String,
        category: String,
        message: String,
        findings: MutableList<ImageCandidatePreflightFinding>,
    ): Int {
        var count = 0
        requirements.forEachIndexed { index, requirement ->
            collectTextFields(requirement, index).forEach { field ->
                patterns.forEach { pattern ->
                    if (pattern.containsMatchIn(field.value)) {
                        count += 1
                        findings.add(finding(ruleId, category, "CRITICAL", field.path, message, evidence = listOf(field.value), suggestedFix = "Keep image candidate material preparation-only and unapproved."))
                    }
                }
            }
        }
        return count
    }

    private fun collectTextFields(requirement: ImageCandidateRequirement, index: Int): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) {
            if (value.isNotBlank()) fields += TextField(path, value)
        }
        add("$.decisionIntake.imageCandidateRequirements[$index].status", requirement.status)
        add("$.decisionIntake.imageCandidateRequirements[$index].requirement", requirement.requirement)
        requirement.referenceRequirements.forEachIndexed { noteIndex, value -> add("$.decisionIntake.imageCandidateRequirements[$index].referenceRequirements[$noteIndex]", value) }
        requirement.doNotShow.forEachIndexed { noteIndex, value -> add("$.decisionIntake.imageCandidateRequirements[$index].doNotShow[$noteIndex]", value) }
        requirement.promptSafetyNotes.forEachIndexed { noteIndex, value -> add("$.decisionIntake.imageCandidateRequirements[$index].promptSafetyNotes[$noteIndex]", value) }
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
    ): ImageCandidatePreflightFinding =
        ImageCandidatePreflightFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private data class TextField(val path: String, val value: String)
}
