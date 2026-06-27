package com.localnovel.engine.service

import com.localnovel.engine.model.StoryboardPacketDraft
import com.localnovel.engine.model.StoryboardPacketDraftValidationEvidence
import com.localnovel.engine.model.StoryboardPacketDraftValidationFinding
import com.localnovel.engine.model.StoryboardPacketDraftValidationReport
import com.localnovel.engine.model.StoryboardPlanningBrief
import kotlin.math.ceil

class StoryboardPacketDraftValidationService {
    private val requiredBriefStep = "storyboard_packet_draft"
    private val nextAllowedDownstreamStep = "storyboard_packet_review"
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
    private val knownVerdicts = setOf("PASS", "PASS_WITH_WARNINGS", "FAIL")

    fun validate(
        storyboardPlanningBrief: StoryboardPlanningBrief,
        storyboardPacketDraft: StoryboardPacketDraft,
        storyboardPlanningBriefFile: String = "",
        storyboardPacketDraftFile: String = "",
    ): StoryboardPacketDraftValidationReport {
        val findings = mutableListOf<StoryboardPacketDraftValidationFinding>()
        val briefVerdict = storyboardPlanningBrief.verdict.normalizedVerdict()

        requirePassVerdict(
            verdict = briefVerdict,
            originalVerdict = storyboardPlanningBrief.verdict,
            path = "$.storyboardPlanningBrief.verdict",
            findings = findings,
        )
        if (storyboardPlanningBrief.nextAllowedDownstreamStep != requiredBriefStep) {
            findings.add(
                finding(
                    ruleId = "PLANNING_BRIEF_NEXT_STEP_NOT_STORYBOARD_PACKET_DRAFT",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.storyboardPlanningBrief.nextAllowedDownstreamStep",
                    message = "Storyboard packet draft validation requires planning brief nextAllowedDownstreamStep=storyboard_packet_draft.",
                    evidence = listOf(storyboardPlanningBrief.nextAllowedDownstreamStep),
                    suggestedFix = "Use the storyboard planning brief that passed into storyboard_packet_draft.",
                ),
            )
        }

        val sourceBindingFieldCount = validateSourceBinding(storyboardPlanningBrief, storyboardPacketDraft, findings)
        val anchorReferencedCount = validateContinuityAnchors(storyboardPlanningBrief, storyboardPacketDraft, findings)
        validateFrames(storyboardPacketDraft, findings)
        val assetApprovalStatusForbiddenCount = validateAssetApprovalStatuses(storyboardPacketDraft, findings)
        val forbiddenClaimCount = validateForbiddenClaims(storyboardPacketDraft, findings) + assetApprovalStatusForbiddenCount
        val forbiddenOutputPathCount = validateOutputPaths(storyboardPacketDraft, findings)
        val externalGenerationApiHitCount = validateExternalGenerationApiHints(storyboardPacketDraft, findings)

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

        return StoryboardPacketDraftValidationReport(
            verdict = verdict,
            storyboardPlanningBriefFile = storyboardPlanningBriefFile,
            storyboardPacketDraftFile = storyboardPacketDraftFile,
            storyboardPlanningBriefId = storyboardPacketDraft.storyboardPlanningBriefId,
            storyboardPacketDraftId = storyboardPacketDraft.packetId,
            acceptedScriptPacketId = storyboardPacketDraft.acceptedScriptPacketId,
            scriptPacketDraftFile = storyboardPacketDraft.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = storyboardPacketDraft.scriptPacketDraftContentSha256,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = StoryboardPacketDraftValidationEvidence(
                planningBriefVerdict = storyboardPlanningBrief.verdict,
                planningBriefNextAllowedDownstreamStep = storyboardPlanningBrief.nextAllowedDownstreamStep,
                sourceBindingFieldCount = sourceBindingFieldCount,
                frameCount = storyboardPacketDraft.frames.size,
                protectedContinuityAnchorCount = storyboardPlanningBrief
                    .protectedContinuityAnchorsSummary
                    .protectedContinuityAnchors
                    .count { it.isNotBlank() },
                protectedContinuityAnchorReferencedCount = anchorReferencedCount,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenOutputPathCount = forbiddenOutputPathCount,
                externalGenerationApiHitCount = externalGenerationApiHitCount,
            ),
        )
    }

    private fun validateSourceBinding(
        brief: StoryboardPlanningBrief,
        draft: StoryboardPacketDraft,
        findings: MutableList<StoryboardPacketDraftValidationFinding>,
    ): Int {
        var presentCount = 0
        fun requireMatch(
            draftValue: String,
            briefValue: String,
            path: String,
            missingRuleId: String,
            mismatchRuleId: String,
            label: String,
            normalize: (String) -> String = { it.trim() },
        ) {
            val normalizedDraft = normalize(draftValue)
            val normalizedBrief = normalize(briefValue)
            if (normalizedDraft.isNotBlank()) presentCount += 1
            if (normalizedDraft.isBlank()) {
                findings.add(
                    finding(
                        ruleId = missingRuleId,
                        category = "source_linkage",
                        severity = "CRITICAL",
                        path = path,
                        message = "Storyboard packet draft must bind to the planning brief $label.",
                        suggestedFix = "Copy the $label from the PASS storyboard planning brief into the draft source binding.",
                    ),
                )
            } else if (normalizedBrief.isBlank() || normalizedDraft != normalizedBrief) {
                findings.add(
                    finding(
                        ruleId = mismatchRuleId,
                        category = "source_linkage",
                        severity = "CRITICAL",
                        path = path,
                        message = "Storyboard packet draft $label does not match the planning brief.",
                        evidence = listOf("draft=$draftValue", "brief=$briefValue"),
                        suggestedFix = "Use the storyboard packet draft created from the matching planning brief.",
                    ),
                )
            }
        }

        requireMatch(
            draftValue = draft.storyboardPlanningBriefId,
            briefValue = brief.briefId,
            path = "$.storyboardPacketDraft.storyboardPlanningBriefId",
            missingRuleId = "STORYBOARD_PLANNING_BRIEF_ID_MISSING",
            mismatchRuleId = "STORYBOARD_PLANNING_BRIEF_ID_MISMATCH",
            label = "brief id",
        )
        requireMatch(
            draftValue = draft.acceptedScriptPacketId,
            briefValue = brief.acceptedScriptPacketId,
            path = "$.storyboardPacketDraft.acceptedScriptPacketId",
            missingRuleId = "ACCEPTED_SCRIPT_PACKET_ID_MISSING",
            mismatchRuleId = "ACCEPTED_SCRIPT_PACKET_ID_MISMATCH",
            label = "accepted script packet id",
        )
        requireMatch(
            draftValue = draft.scriptPacketDraftFile,
            briefValue = brief.scriptPacketDraftFile,
            path = "$.storyboardPacketDraft.scriptPacketDraftFile",
            missingRuleId = "SCRIPT_PACKET_DRAFT_FILE_MISSING",
            mismatchRuleId = "SCRIPT_PACKET_DRAFT_FILE_MISMATCH",
            label = "script packet draft file",
            normalize = ::normalizePath,
        )
        requireMatch(
            draftValue = draft.scriptPacketDraftContentSha256,
            briefValue = brief.scriptPacketDraftContentSha256,
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
                    message = "Storyboard packet draft source hash must be a lowercase SHA-256 hex string.",
                    evidence = listOf(draft.scriptPacketDraftContentSha256),
                    suggestedFix = "Use the exact 64-character lowercase SHA-256 from the planning brief.",
                ),
            )
        }
        if (draft.packetId.isBlank()) {
            findings.add(
                finding(
                    ruleId = "STORYBOARD_PACKET_DRAFT_ID_MISSING",
                    category = "source_linkage",
                    severity = "MAJOR",
                    path = "$.storyboardPacketDraft.packetId",
                    message = "Storyboard packet draft must have a stable draft packet id.",
                    suggestedFix = "Assign a stable draft packet id before validation.",
                ),
            )
        }
        return presentCount
    }

    private fun validateContinuityAnchors(
        brief: StoryboardPlanningBrief,
        draft: StoryboardPacketDraft,
        findings: MutableList<StoryboardPacketDraftValidationFinding>,
    ): Int {
        val protectedAnchors = brief.protectedContinuityAnchorsSummary.protectedContinuityAnchors
            .filter { it.isNotBlank() }
        if (protectedAnchors.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "PROTECTED_CONTINUITY_ANCHORS_MISSING",
                    category = "continuity",
                    severity = "MAJOR",
                    path = "$.storyboardPlanningBrief.protectedContinuityAnchorsSummary.protectedContinuityAnchors",
                    message = "Storyboard packet draft validation requires protected continuity anchors from the planning brief.",
                    suggestedFix = "Regenerate the planning brief with protected continuity anchors before storyboard packet drafting.",
                ),
            )
            return 0
        }

        val combinedDraftText = collectTextFields(draft).joinToString("\n") { it.value }
        var referencedCount = 0
        protectedAnchors.forEachIndexed { index, anchor ->
            if (anchorReferenced(anchor, combinedDraftText)) {
                referencedCount += 1
            } else {
                findings.add(
                    finding(
                        ruleId = "PROTECTED_CONTINUITY_ANCHOR_NOT_REFERENCED",
                        category = "continuity",
                        severity = "MAJOR",
                        path = "$.storyboardPlanningBrief.protectedContinuityAnchorsSummary.protectedContinuityAnchors[$index]",
                        message = "Storyboard packet draft must preserve or explicitly reference every protected continuity anchor.",
                        evidence = listOf(anchor),
                        suggestedFix = "Carry the anchor into draft protectedContinuityAnchors or frame continuityAnchors/mustShow text.",
                    ),
                )
            }
        }
        return referencedCount
    }

    private fun validateFrames(
        draft: StoryboardPacketDraft,
        findings: MutableList<StoryboardPacketDraftValidationFinding>,
    ) {
        if (draft.frames.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "STORYBOARD_FRAMES_MISSING",
                    category = "frames",
                    severity = "CRITICAL",
                    path = "$.storyboardPacketDraft.frames",
                    message = "Storyboard packet draft must contain text-only frame entries.",
                    suggestedFix = "Add at least one frame with frame id, scene/beat reference, visual or camera intent, and visual boundaries.",
                ),
            )
            return
        }
        draft.frames.forEachIndexed { index, frame ->
            val path = "$.storyboardPacketDraft.frames[$index]"
            if (frame.frameId.isBlank()) {
                findings.add(missingFrameFinding("FRAME_ID_MISSING", "$path.frameId", "frame id"))
            }
            if (frame.sceneReference.isBlank() && frame.beatReference.isBlank()) {
                findings.add(
                    missingFrameFinding(
                        ruleId = "FRAME_SCENE_OR_BEAT_REFERENCE_MISSING",
                        path = "$path.sceneReference",
                        label = "scene or beat reference",
                    ),
                )
            }
            if (frame.visualIntent.isBlank() && frame.cameraIntent.isBlank()) {
                findings.add(
                    missingFrameFinding(
                        ruleId = "FRAME_VISUAL_OR_CAMERA_INTENT_MISSING",
                        path = "$path.visualIntent",
                        label = "visual or camera intent",
                    ),
                )
            }
            if (frame.mustShow.none { it.isNotBlank() } && frame.doNotShow.none { it.isNotBlank() }) {
                findings.add(
                    missingFrameFinding(
                        ruleId = "FRAME_VISUAL_BOUNDARY_MISSING",
                        path = "$path.mustShow",
                        label = "must-show or do-not-show boundary",
                    ),
                )
            }
        }
    }

    private fun validateForbiddenClaims(
        draft: StoryboardPacketDraft,
        findings: MutableList<StoryboardPacketDraftValidationFinding>,
    ): Int {
        var count = 0
        collectTextFields(draft).forEach { field ->
            forbiddenClaimPatterns.forEach { pattern ->
                val hit = pattern.find(field.value) ?: return@forEach
                count += 1
                findings.add(
                    finding(
                        ruleId = "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN",
                        category = "downstream_boundary",
                        severity = "MAJOR",
                        path = field.path,
                        message = "Storyboard packet draft must not claim storyboard, image, video, runtime scene, canon, or asset approval completion.",
                        evidence = listOf(hit.value.take(160)),
                        suggestedFix = "Keep the draft text-only and route it to storyboard_packet_review before any downstream asset/image/video step.",
                    ),
                )
            }
        }
        return count
    }

    private fun validateAssetApprovalStatuses(
        draft: StoryboardPacketDraft,
        findings: MutableList<StoryboardPacketDraftValidationFinding>,
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
                        message = "Storyboard packet draft frames must not carry asset approval status.",
                        evidence = listOf(frame.assetApprovalStatus),
                        suggestedFix = "Use blank or candidate_only; approval belongs to a later explicit asset review gate.",
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
                        message = "Storyboard packet draft asset candidates must not claim approval, readiness, lock, or finality.",
                        evidence = listOf(candidate.approvalStatus),
                        suggestedFix = "Use blank or candidate_only; approval belongs to a later explicit asset review gate.",
                    ),
                )
            }
        }
        return count
    }

    private fun validateOutputPaths(
        draft: StoryboardPacketDraft,
        findings: MutableList<StoryboardPacketDraftValidationFinding>,
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
                        message = "Storyboard packet draft frames must not include image output paths.",
                        evidence = listOf(frame.imageOutputPath),
                        suggestedFix = "Remove image output paths; this gate accepts only text storyboard planning.",
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
                        message = "Storyboard packet draft frames must not include video output paths.",
                        evidence = listOf(frame.videoOutputPath),
                        suggestedFix = "Remove video output paths; video belongs to a later explicit downstream gate.",
                    ),
                )
            }
        }
        collectTextFields(draft).forEach { field ->
            val hit = outputPathPattern.find(field.value) ?: return@forEach
            count += 1
            findings.add(
                finding(
                    ruleId = "IMAGE_OR_VIDEO_OUTPUT_PATH_FORBIDDEN",
                    category = "downstream_boundary",
                    severity = "MAJOR",
                    path = field.path,
                    message = "Storyboard packet draft must not include completed image or video output paths.",
                    evidence = listOf(hit.value.take(160)),
                    suggestedFix = "Use text-only candidate ids or requirements; remove generated media paths until a later approved asset/image gate.",
                ),
            )
        }
        return count
    }

    private fun validateExternalGenerationApiHints(
        draft: StoryboardPacketDraft,
        findings: MutableList<StoryboardPacketDraftValidationFinding>,
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
                        message = "Storyboard packet draft frames must not include generation API information.",
                        evidence = listOf(frame.generationApi),
                        suggestedFix = "Remove provider/job/API details; this gate accepts only text storyboard planning.",
                    ),
                )
            }
        }
        collectTextFields(draft).forEach { field ->
            generationApiPatterns.forEach { pattern ->
                val hit = pattern.find(field.value) ?: return@forEach
                count += 1
                findings.add(
                    finding(
                        ruleId = "EXTERNAL_GENERATION_API_INFO_FORBIDDEN",
                        category = "downstream_boundary",
                        severity = "MAJOR",
                        path = field.path,
                        message = "Storyboard packet draft must not include external generation API call details.",
                        evidence = listOf(hit.value.take(160)),
                        suggestedFix = "Remove API/provider/endpoint/key details and keep only text storyboard requirements.",
                    ),
                )
            }
        }
        return count
    }

    private fun anchorReferenced(anchor: String, combinedText: String): Boolean {
        val normalizedAnchor = normalizeForSearch(anchor)
        val normalizedText = normalizeForSearch(combinedText)
        if (normalizedAnchor.isNotBlank() && normalizedText.contains(normalizedAnchor)) {
            return true
        }
        val terms = significantTerms(anchor)
        if (terms.isEmpty()) return false
        val matched = terms.count { normalizedText.contains(it) }
        val required = if (terms.size <= 2) terms.size else ceil(terms.size * 0.55).toInt().coerceAtLeast(2)
        return matched >= required
    }

    private fun significantTerms(text: String): List<String> {
        val normalized = normalizeForSearch(text)
        val latinTerms = Regex("""[a-z0-9][a-z0-9'-]{2,}""")
            .findAll(normalized)
            .map { it.value }
            .filterNot { it in stopWords }
        val cjkTerms = Regex("""[\u4E00-\u9FFF]{2,}""")
            .findAll(normalized)
            .flatMap { match -> match.value.windowed(size = 2, step = 1).asSequence() }
        return (latinTerms + cjkTerms)
            .distinct()
            .take(32)
            .toList()
    }

    private fun collectTextFields(draft: StoryboardPacketDraft): List<TextField> {
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
        return fields
    }

    private fun isAllowedAssetApprovalStatus(value: String): Boolean =
        value.isBlank() || value.trim().lowercase() == "candidate_only"

    private fun requirePassVerdict(
        verdict: String,
        originalVerdict: String,
        path: String,
        findings: MutableList<StoryboardPacketDraftValidationFinding>,
    ) {
        when {
            verdict.isBlank() -> findings.add(
                finding("STORYBOARD_PLANNING_BRIEF_VERDICT_MISSING", "upstream", "MAJOR", path, "Storyboard planning brief has no verdict.", suggestedFix = "Regenerate the upstream storyboard planning brief."),
            )
            verdict !in knownVerdicts -> findings.add(
                finding("STORYBOARD_PLANNING_BRIEF_VERDICT_UNKNOWN", "upstream", "MAJOR", path, "Storyboard planning brief has an unknown verdict.", evidence = listOf(originalVerdict), suggestedFix = "Use a brief with verdict PASS, PASS_WITH_WARNINGS, or FAIL."),
            )
            verdict != "PASS" -> findings.add(
                finding("STORYBOARD_PLANNING_BRIEF_NOT_PASS", "upstream", "CRITICAL", path, "Storyboard packet draft validation requires storyboard planning brief PASS.", evidence = listOf(originalVerdict), suggestedFix = "Fix and rerun the storyboard planning brief before validating a storyboard packet draft."),
            )
        }
    }

    private fun missingFrameFinding(
        ruleId: String,
        path: String,
        label: String,
    ): StoryboardPacketDraftValidationFinding =
        finding(
            ruleId = ruleId,
            category = "frames",
            severity = "MAJOR",
            path = path,
            message = "Storyboard packet draft frame must include $label.",
            suggestedFix = "Fill every text-only frame entry before storyboard packet review.",
        )

    private fun normalizeForSearch(text: String): String =
        text.lowercase()
            .replace(Regex("""\s+"""), " ")
            .trim()

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
    ): StoryboardPacketDraftValidationFinding =
        StoryboardPacketDraftValidationFinding(
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
        val stopWords = setOf(
            "the",
            "and",
            "but",
            "for",
            "from",
            "that",
            "this",
            "with",
            "before",
            "after",
            "already",
            "requires",
        )
        val forbiddenClaimPatterns = listOf(
            Regex("""(?i)\bstoryboard\s+(generated|created|complete|completed|approved|final)\b"""),
            Regex("""(?i)\b(generated|created|completed|approved|final)\s+storyboard\b"""),
            Regex("""(?i)\bimage\s+(generated|created|complete|completed|final)\b"""),
            Regex("""(?i)\b(generated|created|completed|final)\s+image\b"""),
            Regex("""(?i)\bapproved\s+assets?\b"""),
            Regex("""(?i)\bassets?\s+(?:are\s+)?approved\b"""),
            Regex("""(?i)\bassets?\s+approval\s+(?:is\s+)?(complete|completed|ready|done)\b"""),
            Regex("""(?i)\basset\s+approval\s+complete\b"""),
            Regex("""(?i)\bfinal\s+video\b"""),
            Regex("""(?i)\bvideo\s+(generated|created|complete|completed|final)\b"""),
            Regex("""(?i)\bruntime\s+scene\b"""),
            Regex("""(?i)\bsetting\s+pack\s+canon\b"""),
            Regex("""(?i)\bcanon\s+(applied|approved|complete|completed)\b"""),
            Regex("""(?i)\bis\s+canon\b"""),
            Regex("""(?i)\bcanonical\b"""),
            Regex("\u5df2\u7522\u751f\u5206\u93e1"),
            Regex("\u5df2\u751f\u6210\u5206\u93e1"),
            Regex("\u5df2\u6838\u51c6\u7d20\u6750"),
            Regex("\u6700\u7d42\u5f71\u7247"),
            Regex("\u57f7\u884c\u671f\u5834\u666f"),
            Regex("\u8a2d\u5b9a\u5305\u6b63\u5178"),
        )
        val outputPathPattern = Regex("""(?i)(?:^|[\\/\s:])[\w .-]+\.(png|jpe?g|webp|gif|mp4|mov|avi|mkv)\b""")
        val generationApiPatterns = listOf(
            Regex("""(?i)\b(api[_-]?key|secret[_-]?key|bearer\s+token|endpoint\s*=|https?://)\b"""),
            Regex("""(?i)\b(openai|midjourney|seedance|oiioii|runway|pika|stable\s+diffusion)\s+(api|endpoint|job|request)\b"""),
        )
    }
}
