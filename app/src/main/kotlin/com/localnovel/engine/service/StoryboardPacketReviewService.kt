package com.localnovel.engine.service

import com.localnovel.engine.model.AcceptedScriptPacketContinuityAnchorsSummary
import com.localnovel.engine.model.StoryboardPacketDraft
import com.localnovel.engine.model.StoryboardPacketDraftValidationReport
import com.localnovel.engine.model.StoryboardPacketReviewAssetRequirement
import com.localnovel.engine.model.StoryboardPacketReviewChecklistItem
import com.localnovel.engine.model.StoryboardPacketReviewEvidence
import com.localnovel.engine.model.StoryboardPacketReviewFinding
import com.localnovel.engine.model.StoryboardPacketReviewPacket
import com.localnovel.engine.model.StoryboardPacketReviewSourceReports

class StoryboardPacketReviewService {
    private val requiredReportStep = "storyboard_packet_review"
    private val nextAllowedDownstreamStep = "storyboard_packet_human_review"
    private val knownVerdicts = setOf("PASS", "PASS_WITH_WARNINGS", "FAIL")
    private val sha256Regex = Regex("""[0-9a-f]{64}""")

    fun create(
        storyboardPacketDraft: StoryboardPacketDraft,
        storyboardPacketDraftReport: StoryboardPacketDraftValidationReport,
        reviewId: String = "",
        storyboardPacketDraftFile: String = "",
        storyboardPacketDraftReportFile: String = "",
    ): StoryboardPacketReviewPacket {
        val findings = mutableListOf<StoryboardPacketReviewFinding>()
        val reportVerdict = storyboardPacketDraftReport.verdict.normalizedVerdict()
        requirePassReport(reportVerdict, storyboardPacketDraftReport.verdict, findings)
        if (storyboardPacketDraftReport.nextAllowedDownstreamStep != requiredReportStep) {
            findings.add(
                finding(
                    ruleId = "DRAFT_REPORT_NEXT_STEP_NOT_REVIEW",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.storyboardPacketDraftReport.nextAllowedDownstreamStep",
                    message = "Storyboard packet review requires validation report nextAllowedDownstreamStep=storyboard_packet_review.",
                    evidence = listOf(storyboardPacketDraftReport.nextAllowedDownstreamStep),
                    suggestedFix = "Use the storyboard packet draft validation report that passed into storyboard_packet_review.",
                ),
            )
        }

        val sourceBindingFieldCount = validateSourceBinding(
            draft = storyboardPacketDraft,
            report = storyboardPacketDraftReport,
            draftFile = storyboardPacketDraftFile,
            reportFile = storyboardPacketDraftReportFile,
            findings = findings,
        )
        val frameIds = validateFrames(storyboardPacketDraft, findings)
        val assetRequirements = validateAssetRequirements(storyboardPacketDraft, findings)
        val residualRisks = residualRisks(storyboardPacketDraftReport)
        val reviewChecklist = reviewChecklist()
        val forbiddenStatusCount = validateApprovalStatuses(storyboardPacketDraft, findings)
        val forbiddenOutputPathCount = validateOutputPaths(storyboardPacketDraft, findings)
        val externalGenerationApiHitCount = validateGenerationApiFields(storyboardPacketDraft, findings)
        val forbiddenClaimCount = validateForbiddenClaims(storyboardPacketDraft, storyboardPacketDraftReport, findings) +
            forbiddenStatusCount

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
        val resolvedReviewId = reviewId.ifBlank {
            storyboardPacketDraft.packetId.takeIf { it.isNotBlank() }?.let { "$it-review" }.orEmpty()
        }

        return StoryboardPacketReviewPacket(
            verdict = verdict,
            reviewId = resolvedReviewId,
            reviewStatus = if (verdict == "FAIL") "FAIL" else "REVIEW_READY",
            storyboardPacketDraftFile = storyboardPacketDraftFile,
            storyboardPacketDraftReportFile = storyboardPacketDraftReportFile,
            storyboardPacketDraftId = storyboardPacketDraft.packetId,
            storyboardPlanningBriefId = storyboardPacketDraft.storyboardPlanningBriefId,
            acceptedScriptPacketId = storyboardPacketDraft.acceptedScriptPacketId,
            scriptPacketDraftFile = storyboardPacketDraft.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = storyboardPacketDraft.scriptPacketDraftContentSha256,
            sourceReports = StoryboardPacketReviewSourceReports(
                storyboardPacketDraftValidationReportFile = storyboardPacketDraftReportFile,
                storyboardPacketDraftValidationVerdict = storyboardPacketDraftReport.verdict,
                storyboardPacketDraftValidationNextStep = storyboardPacketDraftReport.nextAllowedDownstreamStep,
            ),
            frameCount = storyboardPacketDraft.frames.size,
            frameIds = frameIds,
            protectedContinuityAnchorsSummary = AcceptedScriptPacketContinuityAnchorsSummary(
                protectedContinuityAnchorCount = storyboardPacketDraft.protectedContinuityAnchors.count { it.isNotBlank() },
                protectedContinuityAnchors = storyboardPacketDraft.protectedContinuityAnchors
                    .filter { it.isNotBlank() }
                    .distinct(),
            ),
            assetCandidateRequirements = assetRequirements,
            remainingResidualRisks = residualRisks,
            reviewChecklist = reviewChecklist,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = StoryboardPacketReviewEvidence(
                draftValidationVerdict = storyboardPacketDraftReport.verdict,
                draftValidationNextAllowedDownstreamStep = storyboardPacketDraftReport.nextAllowedDownstreamStep,
                sourceBindingFieldCount = sourceBindingFieldCount,
                frameCount = storyboardPacketDraft.frames.size,
                assetCandidateRequirementCount = assetRequirements.size,
                protectedContinuityAnchorCount = storyboardPacketDraft.protectedContinuityAnchors.count { it.isNotBlank() },
                residualRiskCount = residualRisks.size,
                reviewChecklistCount = reviewChecklist.size,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenOutputPathCount = forbiddenOutputPathCount,
                externalGenerationApiHitCount = externalGenerationApiHitCount,
            ),
        )
    }

    private fun validateSourceBinding(
        draft: StoryboardPacketDraft,
        report: StoryboardPacketDraftValidationReport,
        draftFile: String,
        reportFile: String,
        findings: MutableList<StoryboardPacketReviewFinding>,
    ): Int {
        var presentCount = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) {
                findings.add(
                    finding(
                        ruleId = ruleId,
                        category = "source_linkage",
                        severity = "CRITICAL",
                        path = path,
                        message = "Storyboard packet review requires nonblank $label.",
                        suggestedFix = "Use the draft and validation report produced by the storyboard packet draft gate.",
                    ),
                )
            } else {
                presentCount += 1
            }
        }
        fun requireMatch(
            draftValue: String,
            reportValue: String,
            path: String,
            missingRuleId: String,
            mismatchRuleId: String,
            label: String,
            normalize: (String) -> String = { it.trim() },
        ) {
            val normalizedDraft = normalize(draftValue)
            val normalizedReport = normalize(reportValue)
            requireNonBlank(draftValue, path, missingRuleId, label)
            if (normalizedDraft.isNotBlank() && (normalizedReport.isBlank() || normalizedDraft != normalizedReport)) {
                findings.add(
                    finding(
                        ruleId = mismatchRuleId,
                        category = "source_linkage",
                        severity = "CRITICAL",
                        path = path,
                        message = "Storyboard packet review source binding mismatch for $label.",
                        evidence = listOf("draft=$draftValue", "report=$reportValue"),
                        suggestedFix = "Use the storyboard packet draft that matches the validation report.",
                    ),
                )
            }
        }

        requireNonBlank(draftFile, "$.inputs.storyboardPacketDraftFile", "REVIEW_INPUT_DRAFT_FILE_MISSING", "review input draft file")
        requireNonBlank(reportFile, "$.inputs.storyboardPacketDraftReportFile", "REVIEW_INPUT_DRAFT_REPORT_FILE_MISSING", "review input draft report file")
        requireNonBlank(
            report.storyboardPacketDraftFile,
            "$.storyboardPacketDraftReport.storyboardPacketDraftFile",
            "DRAFT_REPORT_DRAFT_FILE_MISSING",
            "validation report draft file",
        )
        if (draftFile.isNotBlank() &&
            report.storyboardPacketDraftFile.isNotBlank() &&
            normalizePath(draftFile) != normalizePath(report.storyboardPacketDraftFile)
        ) {
            findings.add(
                finding(
                    ruleId = "REVIEW_INPUT_DRAFT_FILE_MISMATCH",
                    category = "source_linkage",
                    severity = "CRITICAL",
                    path = "$.storyboardPacketDraftReport.storyboardPacketDraftFile",
                    message = "Storyboard packet draft report points at a different draft file.",
                    evidence = listOf(draftFile, report.storyboardPacketDraftFile),
                    suggestedFix = "Use the validation report created for the provided storyboard packet draft.",
                ),
            )
        }
        requireMatch(
            draftValue = draft.packetId,
            reportValue = report.storyboardPacketDraftId,
            path = "$.storyboardPacketDraft.packetId",
            missingRuleId = "STORYBOARD_PACKET_DRAFT_ID_MISSING",
            mismatchRuleId = "STORYBOARD_PACKET_DRAFT_ID_MISMATCH",
            label = "storyboard packet draft id",
        )
        requireMatch(
            draftValue = draft.storyboardPlanningBriefId,
            reportValue = report.storyboardPlanningBriefId,
            path = "$.storyboardPacketDraft.storyboardPlanningBriefId",
            missingRuleId = "STORYBOARD_PLANNING_BRIEF_ID_MISSING",
            mismatchRuleId = "STORYBOARD_PLANNING_BRIEF_ID_MISMATCH",
            label = "storyboard planning brief id",
        )
        requireMatch(
            draftValue = draft.acceptedScriptPacketId,
            reportValue = report.acceptedScriptPacketId,
            path = "$.storyboardPacketDraft.acceptedScriptPacketId",
            missingRuleId = "ACCEPTED_SCRIPT_PACKET_ID_MISSING",
            mismatchRuleId = "ACCEPTED_SCRIPT_PACKET_ID_MISMATCH",
            label = "accepted script packet id",
        )
        requireMatch(
            draftValue = draft.scriptPacketDraftFile,
            reportValue = report.scriptPacketDraftFile,
            path = "$.storyboardPacketDraft.scriptPacketDraftFile",
            missingRuleId = "SCRIPT_PACKET_DRAFT_FILE_MISSING",
            mismatchRuleId = "SCRIPT_PACKET_DRAFT_FILE_MISMATCH",
            label = "script packet draft file",
            normalize = ::normalizePath,
        )
        requireMatch(
            draftValue = draft.scriptPacketDraftContentSha256,
            reportValue = report.scriptPacketDraftContentSha256,
            path = "$.storyboardPacketDraft.scriptPacketDraftContentSha256",
            missingRuleId = "SCRIPT_PACKET_DRAFT_HASH_MISSING",
            mismatchRuleId = "SCRIPT_PACKET_DRAFT_HASH_MISMATCH",
            label = "script packet draft SHA-256",
            normalize = { it.trim().lowercase() },
        )
        if (draft.scriptPacketDraftContentSha256.isNotBlank() &&
            !draft.scriptPacketDraftContentSha256.trim().matches(sha256Regex)
        ) {
            findings.add(
                finding(
                    ruleId = "SCRIPT_PACKET_DRAFT_HASH_INVALID",
                    category = "source_linkage",
                    severity = "MAJOR",
                    path = "$.storyboardPacketDraft.scriptPacketDraftContentSha256",
                    message = "Storyboard packet review requires a lowercase SHA-256 source hash.",
                    evidence = listOf(draft.scriptPacketDraftContentSha256),
                    suggestedFix = "Use the exact 64-character lowercase SHA-256 from the validation report.",
                ),
            )
        }
        return presentCount
    }

    private fun validateFrames(
        draft: StoryboardPacketDraft,
        findings: MutableList<StoryboardPacketReviewFinding>,
    ): List<String> {
        if (draft.frames.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "REVIEW_FRAME_IDS_MISSING",
                    category = "review_packet",
                    severity = "CRITICAL",
                    path = "$.storyboardPacketDraft.frames",
                    message = "Storyboard packet review requires at least one text frame.",
                    suggestedFix = "Use the PASS storyboard packet draft with text frame entries.",
                ),
            )
            return emptyList()
        }
        val ids = draft.frames.map { it.frameId }.filter { it.isNotBlank() }
        if (ids.size != draft.frames.size) {
            findings.add(
                finding(
                    ruleId = "REVIEW_FRAME_ID_MISSING",
                    category = "review_packet",
                    severity = "MAJOR",
                    path = "$.storyboardPacketDraft.frames",
                    message = "Every frame must have a frame id before review handoff.",
                    suggestedFix = "Fill missing frame ids and rerun storyboard packet draft validation.",
                ),
            )
        }
        return ids.distinct()
    }

    private fun validateAssetRequirements(
        draft: StoryboardPacketDraft,
        findings: MutableList<StoryboardPacketReviewFinding>,
    ): List<StoryboardPacketReviewAssetRequirement> {
        val requirements = draft.assetCandidates
            .filter { it.candidateId.isNotBlank() || it.requirement.isNotBlank() }
            .map {
                StoryboardPacketReviewAssetRequirement(
                    candidateId = it.candidateId,
                    requirement = it.requirement,
                    sourceFrameIds = it.sourceFrameIds.filter { frameId -> frameId.isNotBlank() },
                )
            }
        if (requirements.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "ASSET_CANDIDATE_REQUIREMENTS_MISSING",
                    category = "review_packet",
                    severity = "MAJOR",
                    path = "$.storyboardPacketDraft.assetCandidates",
                    message = "Storyboard packet review packet must preserve asset candidate requirements.",
                    suggestedFix = "Carry candidate-only asset requirements from the storyboard packet draft.",
                ),
            )
        }
        return requirements
    }

    private fun validateApprovalStatuses(
        draft: StoryboardPacketDraft,
        findings: MutableList<StoryboardPacketReviewFinding>,
    ): Int {
        var count = 0
        draft.frames.forEachIndexed { index, frame ->
            if (!isAllowedAssetApprovalStatus(frame.assetApprovalStatus)) {
                count += 1
                findings.add(
                    finding(
                        ruleId = "ASSET_APPROVAL_STATUS_FORBIDDEN",
                        category = "downstream_boundary",
                        severity = "MAJOR",
                        path = "$.storyboardPacketDraft.frames[$index].assetApprovalStatus",
                        message = "Storyboard packet review must not carry frame asset approval status.",
                        evidence = listOf(frame.assetApprovalStatus),
                        suggestedFix = "Use blank or candidate_only; approval belongs to a later explicit human/asset gate.",
                    ),
                )
            }
        }
        draft.assetCandidates.forEachIndexed { index, candidate ->
            if (!isAllowedAssetApprovalStatus(candidate.approvalStatus)) {
                count += 1
                findings.add(
                    finding(
                        ruleId = "ASSET_APPROVAL_STATUS_FORBIDDEN",
                        category = "downstream_boundary",
                        severity = "MAJOR",
                        path = "$.storyboardPacketDraft.assetCandidates[$index].approvalStatus",
                        message = "Storyboard packet review must not carry approved, ready, locked, or final asset status.",
                        evidence = listOf(candidate.approvalStatus),
                        suggestedFix = "Use blank or candidate_only; approval belongs to a later explicit human/asset gate.",
                    ),
                )
            }
        }
        return count
    }

    private fun validateOutputPaths(
        draft: StoryboardPacketDraft,
        findings: MutableList<StoryboardPacketReviewFinding>,
    ): Int {
        var count = 0
        draft.frames.forEachIndexed { index, frame ->
            if (frame.imageOutputPath.isNotBlank()) {
                count += 1
                findings.add(
                    finding(
                        ruleId = "IMAGE_OUTPUT_PATH_FORBIDDEN",
                        category = "downstream_boundary",
                        severity = "MAJOR",
                        path = "$.storyboardPacketDraft.frames[$index].imageOutputPath",
                        message = "Storyboard packet review must not carry image output paths.",
                        evidence = listOf(frame.imageOutputPath),
                        suggestedFix = "Remove image output paths; this packet is review-ready text only.",
                    ),
                )
            }
            if (frame.videoOutputPath.isNotBlank()) {
                count += 1
                findings.add(
                    finding(
                        ruleId = "VIDEO_OUTPUT_PATH_FORBIDDEN",
                        category = "downstream_boundary",
                        severity = "MAJOR",
                        path = "$.storyboardPacketDraft.frames[$index].videoOutputPath",
                        message = "Storyboard packet review must not carry video output paths.",
                        evidence = listOf(frame.videoOutputPath),
                        suggestedFix = "Remove video output paths; video belongs to a later explicit downstream gate.",
                    ),
                )
            }
        }
        return count
    }

    private fun validateGenerationApiFields(
        draft: StoryboardPacketDraft,
        findings: MutableList<StoryboardPacketReviewFinding>,
    ): Int {
        var count = 0
        draft.frames.forEachIndexed { index, frame ->
            if (frame.generationApi.isNotBlank()) {
                count += 1
                findings.add(
                    finding(
                        ruleId = "GENERATION_API_FORBIDDEN",
                        category = "downstream_boundary",
                        severity = "MAJOR",
                        path = "$.storyboardPacketDraft.frames[$index].generationApi",
                        message = "Storyboard packet review must not carry generation API information.",
                        evidence = listOf(frame.generationApi),
                        suggestedFix = "Remove provider/job/API details; this packet is review-ready text only.",
                    ),
                )
            }
        }
        return count
    }

    private fun validateForbiddenClaims(
        draft: StoryboardPacketDraft,
        report: StoryboardPacketDraftValidationReport,
        findings: MutableList<StoryboardPacketReviewFinding>,
    ): Int {
        var count = 0
        collectTextFields(draft, report).forEach { field ->
            forbiddenClaimPatterns.forEach { pattern ->
                val hit = pattern.find(field.value) ?: return@forEach
                count += 1
                findings.add(
                    finding(
                        ruleId = "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN",
                        category = "downstream_boundary",
                        severity = "MAJOR",
                        path = field.path,
                        message = "Storyboard packet review must not claim image/video generation, asset approval, production lock, canon, user approval, or runtime scene completion.",
                        evidence = listOf(hit.value.take(160)),
                        suggestedFix = "Keep review packet language limited to REVIEW_READY and require later human approval gates.",
                    ),
                )
            }
        }
        return count
    }

    private fun residualRisks(report: StoryboardPacketDraftValidationReport): List<String> =
        buildList {
            add("Human review has not completed this storyboard packet.")
            add("Asset candidates are requirements only; no asset decision is granted.")
            add("No image or video generation has run.")
            if (report.findingCount > 0) {
                add("Validation report findings must be reviewed before downstream movement.")
            }
        }

    private fun reviewChecklist(): List<StoryboardPacketReviewChecklistItem> =
        listOf(
            StoryboardPacketReviewChecklistItem(
                itemId = "review-scene-lock",
                category = "scene_lock",
                requiredCheck = "Confirm text frames preserve scene lock, blocking, and spatial continuity.",
            ),
            StoryboardPacketReviewChecklistItem(
                itemId = "review-continuity-anchors",
                category = "continuity",
                requiredCheck = "Confirm every protected continuity anchor is visible or explicitly preserved.",
            ),
            StoryboardPacketReviewChecklistItem(
                itemId = "review-secrecy-boundary",
                category = "knowledge_boundary",
                requiredCheck = "Confirm frames do not reveal author-only or inaccessible causal information.",
            ),
            StoryboardPacketReviewChecklistItem(
                itemId = "review-asset-candidates",
                category = "assets",
                requiredCheck = "Confirm asset entries are requirements only and do not claim an asset decision.",
            ),
            StoryboardPacketReviewChecklistItem(
                itemId = "review-downstream-boundary",
                category = "downstream_boundary",
                requiredCheck = "Confirm no image, video, runtime, production-state, or asset-decision claims are present.",
            ),
        )

    private fun collectTextFields(
        draft: StoryboardPacketDraft,
        report: StoryboardPacketDraftValidationReport,
    ): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) {
            if (value.isNotBlank()) fields.add(TextField(path, value))
        }
        add("$.storyboardPacketDraft.packetId", draft.packetId)
        add("$.storyboardPacketDraft.storyboardPlanningBriefId", draft.storyboardPlanningBriefId)
        add("$.storyboardPacketDraft.acceptedScriptPacketId", draft.acceptedScriptPacketId)
        add("$.storyboardPacketDraft.scriptPacketDraftFile", draft.scriptPacketDraftFile)
        add("$.storyboardPacketDraft.sourceStoryboardPlanningBriefFile", draft.sourceStoryboardPlanningBriefFile)
        draft.protectedContinuityAnchors.forEachIndexed { index, value ->
            add("$.storyboardPacketDraft.protectedContinuityAnchors[$index]", value)
        }
        draft.frames.forEachIndexed { index, frame ->
            val path = "$.storyboardPacketDraft.frames[$index]"
            add("$path.frameId", frame.frameId)
            add("$path.sceneReference", frame.sceneReference)
            add("$path.beatReference", frame.beatReference)
            add("$path.visualIntent", frame.visualIntent)
            add("$path.cameraIntent", frame.cameraIntent)
            frame.mustShow.forEachIndexed { itemIndex, value -> add("$path.mustShow[$itemIndex]", value) }
            frame.doNotShow.forEachIndexed { itemIndex, value -> add("$path.doNotShow[$itemIndex]", value) }
            frame.continuityAnchors.forEachIndexed { itemIndex, value -> add("$path.continuityAnchors[$itemIndex]", value) }
            frame.assetCandidateRefs.forEachIndexed { itemIndex, value -> add("$path.assetCandidateRefs[$itemIndex]", value) }
            add("$path.reviewNotes", frame.reviewNotes)
            add("$path.imageOutputPath", frame.imageOutputPath)
            add("$path.videoOutputPath", frame.videoOutputPath)
            add("$path.generationApi", frame.generationApi)
            add("$path.assetApprovalStatus", frame.assetApprovalStatus)
        }
        draft.assetCandidates.forEachIndexed { index, candidate ->
            val path = "$.storyboardPacketDraft.assetCandidates[$index]"
            add("$path.candidateId", candidate.candidateId)
            add("$path.requirement", candidate.requirement)
            candidate.sourceFrameIds.forEachIndexed { itemIndex, value -> add("$path.sourceFrameIds[$itemIndex]", value) }
            add("$path.approvalStatus", candidate.approvalStatus)
            add("$path.reviewNotes", candidate.reviewNotes)
        }
        add("$.storyboardPacketDraft.reviewNotes", draft.reviewNotes)
        add("$.storyboardPacketDraftReport.blockedReason", report.blockedReason)
        report.findings.forEachIndexed { index, finding ->
            add("$.storyboardPacketDraftReport.findings[$index].message", finding.message)
            finding.evidence.forEachIndexed { evidenceIndex, value ->
                add("$.storyboardPacketDraftReport.findings[$index].evidence[$evidenceIndex]", value)
            }
            add("$.storyboardPacketDraftReport.findings[$index].suggestedFix", finding.suggestedFix)
        }
        return fields
    }

    private fun requirePassReport(
        verdict: String,
        originalVerdict: String,
        findings: MutableList<StoryboardPacketReviewFinding>,
    ) {
        when {
            verdict.isBlank() -> findings.add(
                finding("DRAFT_VALIDATION_REPORT_VERDICT_MISSING", "upstream", "MAJOR", "$.storyboardPacketDraftReport.verdict", "Storyboard packet draft validation report has no verdict.", suggestedFix = "Regenerate the storyboard packet draft validation report."),
            )
            verdict !in knownVerdicts -> findings.add(
                finding("DRAFT_VALIDATION_REPORT_VERDICT_UNKNOWN", "upstream", "MAJOR", "$.storyboardPacketDraftReport.verdict", "Storyboard packet draft validation report has an unknown verdict.", evidence = listOf(originalVerdict), suggestedFix = "Use a report with verdict PASS, PASS_WITH_WARNINGS, or FAIL."),
            )
            verdict != "PASS" -> findings.add(
                finding("DRAFT_VALIDATION_REPORT_NOT_PASS", "upstream", "CRITICAL", "$.storyboardPacketDraftReport.verdict", "Storyboard packet review requires draft validation report PASS.", evidence = listOf(originalVerdict), suggestedFix = "Fix and rerun storyboard packet draft validation before creating a review packet."),
            )
        }
    }

    private fun isAllowedAssetApprovalStatus(value: String): Boolean =
        value.isBlank() || value.trim().lowercase() == "candidate_only"

    private fun normalizePath(path: String): String =
        path.trim()
            .replace("\\", "/")
            .lowercase()

    private fun String.normalizedVerdict(): String =
        trim().uppercase()

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): StoryboardPacketReviewFinding =
        StoryboardPacketReviewFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            path = path,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
        )

    private data class TextField(
        val path: String,
        val value: String,
    )

    private companion object {
        val forbiddenClaimPatterns = listOf(
            Regex("""(?i)\bstoryboard\s+(generated|created|complete|completed|approved|final)\b"""),
            Regex("""(?i)\b(generated|created|completed|approved|final)\s+storyboard\b"""),
            Regex("""(?i)\bimage\s+(generated|created|complete|completed|final)\b"""),
            Regex("""(?i)\b(generated|created|completed|final)\s+image\b"""),
            Regex("""(?i)\bapproved\s+assets?\b"""),
            Regex("""(?i)\bassets?\s+(?:are\s+)?approved\b"""),
            Regex("""(?i)\bassets?\s+approval\s+(?:is\s+)?(complete|completed|ready|done)\b"""),
            Regex("""(?i)\basset\s+approval\s+complete\b"""),
            Regex("""(?i)\bproduction\s+(locked|lock|ready|final)\b"""),
            Regex("""(?i)\bfinal\s+video\b"""),
            Regex("""(?i)\bvideo\s+(generated|created|complete|completed|final)\b"""),
            Regex("""(?i)\buser\s+(approved|approval|accepted)\b"""),
            Regex("""(?i)\bruntime\s+scene\b"""),
            Regex("""(?i)\bsetting\s+pack\s+canon\b"""),
            Regex("""(?i)\bcanon\s+(applied|approved|complete|completed)\b"""),
            Regex("""(?i)\bis\s+canon\b"""),
            Regex("""(?i)\bcanonical\b"""),
            Regex("\u5df2\u7522\u751f\u5206\u93e1"),
            Regex("\u5df2\u751f\u6210\u5206\u93e1"),
            Regex("\u5df2\u6838\u51c6\u7d20\u6750"),
            Regex("\u6700\u7d42\u5f71\u7247"),
            Regex("\u88fd\u4f5c\u9396\u5b9a"),
            Regex("\u4f7f\u7528\u8005\u5df2\u6279\u51c6"),
            Regex("\u57f7\u884c\u671f\u5834\u666f"),
            Regex("\u8a2d\u5b9a\u5305\u6b63\u5178"),
        )
    }
}
