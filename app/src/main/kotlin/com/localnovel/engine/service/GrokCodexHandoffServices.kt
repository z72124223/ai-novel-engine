package com.localnovel.engine.service

import com.localnovel.engine.model.CodexImagePromptDraft
import com.localnovel.engine.model.CodexProductionHandoffDraft
import com.localnovel.engine.model.CodexProductionHandoffEvidence
import com.localnovel.engine.model.CodexReferenceCheck
import com.localnovel.engine.model.CodexReturnToGrokBlock
import com.localnovel.engine.model.CodexSceneSummary
import com.localnovel.engine.model.CodexShotCandidate
import com.localnovel.engine.model.GrokCodexBoundaryFlags
import com.localnovel.engine.model.GrokProductionRequest
import com.localnovel.engine.model.GrokReferenceRequirement
import com.localnovel.engine.model.GrokStoryChunk
import com.localnovel.engine.model.GrokStoryHandoff
import com.localnovel.engine.model.GrokStoryHandoffFinding
import com.localnovel.engine.model.GrokStoryHandoffValidationEvidence
import com.localnovel.engine.model.GrokStoryHandoffValidationReport
import com.localnovel.engine.model.ReturnToGrokRequest

class GrokStoryHandoffTemplateService {
    fun create(
        handoffId: String = "",
        projectId: String = "chenxing",
        title: String = "Grok story handoff template",
        locale: String = "zh-TW",
    ): GrokStoryHandoff {
        val resolvedHandoffId = handoffId.ifBlank { "$projectId-grok-story-handoff-template-v0" }
        return GrokStoryHandoff(
            handoffId = resolvedHandoffId,
            projectId = projectId,
            title = title,
            locale = locale,
            sourceOwner = "grok_build",
            sourceLabel = "Grok Build",
            storyStatus = "draft_for_codex_support",
            storyChunks = listOf(
                GrokStoryChunk(
                    chunkId = "chunk-001",
                    sceneId = "scene-001",
                    title = "Replace with scene title",
                    location = "Replace with visible location",
                    characterIds = listOf("char-lu-chenxing", "char-aijiang"),
                    textForCodex = "Replace with safe story text Codex may process.",
                    safeSummary = "Replace with a short safe summary.",
                    visualIntent = "Replace with visible action, mood, framing, and reference needs.",
                    audioIntent = "Replace with dialogue, ambient sound, silence, or music intent.",
                    continuityNotes = listOf("Replace with continuity notes Codex may use."),
                ),
            ),
            productionRequests = listOf(
                GrokProductionRequest(
                    requestId = "request-001",
                    targetChunkId = "chunk-001",
                    requestType = "scene_summary",
                    request = "Codex should summarize the scene and prepare production support.",
                ),
                GrokProductionRequest(
                    requestId = "request-002",
                    targetChunkId = "chunk-001",
                    requestType = "image_prompt_drafts",
                    request = "Codex should draft candidate-only image prompts using locked references.",
                ),
            ),
            referenceRequirements = chenxingReferenceRequirements(),
            returnToGrok = emptyList(),
            boundaries = GrokCodexBoundaryFlags(),
        )
    }

    private fun chenxingReferenceRequirements(): List<GrokReferenceRequirement> =
        listOf(
            GrokReferenceRequirement(
                requirementId = "ref-lu-chenxing-turnaround",
                label = "Lu Chenxing locked body turnaround",
                path = "world/chenxing/assets/turnaround/lu_chenxing_photo_closest_174_80_turnaround_v2.png",
                description = "Required for Lu Chenxing face, body, glasses, hair, and body proportion.",
                requiredFor = listOf("character_likeness", "image_prompt_drafts"),
            ),
            GrokReferenceRequirement(
                requirementId = "ref-lu-chenxing-opening-costume",
                label = "Lu Chenxing opening modern costume",
                path = "world/chenxing/assets/costume/lu_chenxing_opening_modern_costume_turnaround_v1.png",
                description = "Required for 2026 opening clothing.",
                requiredFor = listOf("costume", "image_prompt_drafts"),
            ),
            GrokReferenceRequirement(
                requirementId = "ref-aijiang-turnaround",
                label = "Aijiang locked production turnaround",
                path = "world/chenxing/assets/turnaround/aijiang_production_turnaround_v1.jpg",
                description = "Required for Aijiang markings, build, eyes, ears, and tail.",
                requiredFor = listOf("character_likeness", "image_prompt_drafts"),
            ),
            GrokReferenceRequirement(
                requirementId = "ref-chenxing-style",
                label = "Chenxing style references",
                path = "world/chenxing/assets/style_reference/",
                description = "Required for misty cinematic, B/W storyboard, or dark rain teal mode.",
                requiredFor = listOf("render_style", "image_prompt_drafts"),
            ),
        )
}

class GrokStoryHandoffValidationService {
    private val nextAllowedDownstreamStep = "codex_production_handoff_draft"
    private val allowedStoryStatuses = setOf("draft_for_codex_support", "revision_for_codex_support", "partial_with_return_to_grok")
    private val allowedRequestTypes = setOf(
        "scene_summary",
        "shot_candidates",
        "image_prompt_drafts",
        "reference_check",
        "integration",
        "validation",
    )
    private val forbiddenBoundaryTextPatterns = listOf(
        Regex("""(?i)\bapproved\b"""),
        Regex("""(?i)\bfinal\s+(image|media|video|asset)\b"""),
        Regex("""(?i)\bcanon(?:ical)?\b"""),
        Regex("""(?i)\bregistered\s+reference\b|\bregistry\b"""),
        Regex("""(?i)\bproduction[-_\s]?lock(?:ed)?\b"""),
        Regex("""(?i)\bprovider\s+job\b|\bapi\s+response\b"""),
        Regex("""(?i)\bgenerated\s+(image|video|audio)\b|\b(image|video|audio)\s+generated\b"""),
        Regex("\u6838\u51c6|\u6279\u51c6|\u6b63\u5178|\u767b\u9304|\u767b\u8a18|\u751f\u7522\u9396\u5b9a|\u5716\u7247\u5df2\u751f\u6210"),
    )

    fun validate(
        handoff: GrokStoryHandoff,
        handoffFile: String = "",
        validationId: String = "",
    ): GrokStoryHandoffValidationReport {
        val findings = mutableListOf<GrokStoryHandoffFinding>()
        validateMetadata(handoff, findings)
        val chunkIds = validateChunks(handoff.storyChunks, findings)
        val requestValidation = validateProductionRequests(handoff.productionRequests, chunkIds, findings)
        validateReferenceRequirements(handoff.referenceRequirements, findings)
        validateReturnToGrok(handoff.returnToGrok, findings)
        val unsafeBoundaryFlagCount = validateBoundaries(handoff.boundaries, findings)
        validateBoundaryText(handoff, findings)

        val failureFindings = findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }
        val verdict = if (failureFindings.isEmpty()) "PASS" else "FAIL"
        val resolvedValidationId = validationId.ifBlank {
            handoff.handoffId.takeIf { it.isNotBlank() }?.let { "$it-validation-v0" }.orEmpty()
        }
        return GrokStoryHandoffValidationReport(
            verdict = verdict,
            validationId = resolvedValidationId,
            handoffFile = handoffFile,
            handoffId = handoff.handoffId,
            projectId = handoff.projectId,
            title = handoff.title,
            nextAllowedDownstreamStep = if (verdict == "PASS") nextAllowedDownstreamStep else "",
            blockedReason = if (verdict == "FAIL") failureFindings.joinToString("; ") { it.ruleId } else "",
            findingCount = findings.size,
            findings = findings,
            evidence = GrokStoryHandoffValidationEvidence(
                storyChunkCount = handoff.storyChunks.size,
                productionRequestCount = handoff.productionRequests.size,
                referenceRequirementCount = handoff.referenceRequirements.size,
                returnToGrokCount = handoff.returnToGrok.size,
                linkedProductionRequestCount = requestValidation.linkedRequestCount,
                crossReferenceMismatchCount = requestValidation.crossReferenceMismatchCount,
                unsafeBoundaryFlagCount = unsafeBoundaryFlagCount,
            ),
        )
    }

    private fun validateMetadata(
        handoff: GrokStoryHandoff,
        findings: MutableList<GrokStoryHandoffFinding>,
    ) {
        requireField(handoff.handoffId, "$.handoffId", "GROK_HANDOFF_ID_MISSING", "handoff id", findings)
        requireField(handoff.projectId, "$.projectId", "GROK_PROJECT_ID_MISSING", "project id", findings)
        requireField(handoff.title, "$.title", "GROK_TITLE_MISSING", "title", findings)
        requireField(handoff.locale, "$.locale", "GROK_LOCALE_MISSING", "locale", findings)
        if (handoff.sourceOwner != "grok_build") {
            findings += finding(
                "GROK_SOURCE_OWNER_MISMATCH",
                "source",
                "CRITICAL",
                "$.sourceOwner",
                "Grok handoff sourceOwner must be grok_build.",
                listOf(handoff.sourceOwner),
                "Set sourceOwner to grok_build.",
            )
        }
        if (handoff.storyStatus.trim().lowercase() !in allowedStoryStatuses) {
            findings += finding(
                "GROK_STORY_STATUS_NOT_ALLOWED",
                "status",
                "CRITICAL",
                "$.storyStatus",
                "Story status must stay draft/revision/partial for Codex support.",
                listOf(handoff.storyStatus),
                "Use draft_for_codex_support, revision_for_codex_support, or partial_with_return_to_grok.",
            )
        }
        if (handoff.storyChunks.isEmpty() && handoff.returnToGrok.isEmpty()) {
            findings += finding(
                "GROK_HANDOFF_HAS_NO_WORK",
                "structure",
                "CRITICAL",
                "$",
                "Handoff requires at least one safe story chunk or RETURN_TO_GROK request.",
            )
        }
    }

    private fun validateChunks(
        chunks: List<GrokStoryChunk>,
        findings: MutableList<GrokStoryHandoffFinding>,
    ): Set<String> {
        val ids = chunks.map { it.chunkId.trim() }.filter { it.isNotBlank() }
        duplicateValues(ids).forEach { duplicate ->
            findings += finding("GROK_CHUNK_IDS_NOT_UNIQUE", "structure", "CRITICAL", "$.storyChunks", "Story chunk ids must be unique.", listOf(duplicate))
        }
        chunks.forEachIndexed { index, chunk ->
            val path = "$.storyChunks[$index]"
            requireField(chunk.chunkId, "$path.chunkId", "GROK_CHUNK_ID_MISSING", "chunk id", findings)
            requireField(chunk.sceneId, "$path.sceneId", "GROK_CHUNK_SCENE_ID_MISSING", "scene id", findings)
            requireField(chunk.title, "$path.title", "GROK_CHUNK_TITLE_MISSING", "chunk title", findings)
            requireField(chunk.safeSummary, "$path.safeSummary", "GROK_CHUNK_SAFE_SUMMARY_MISSING", "safe summary", findings)
            requireField(chunk.visualIntent, "$path.visualIntent", "GROK_CHUNK_VISUAL_INTENT_MISSING", "visual intent", findings)
            if (chunk.textForCodex.isBlank()) {
                findings += finding(
                    "GROK_CHUNK_TEXT_FOR_CODEX_MISSING",
                    "structure",
                    "MAJOR",
                    "$path.textForCodex",
                    "Safe chunk should include textForCodex or be represented as RETURN_TO_GROK.",
                    suggestedFix = "Provide safe textForCodex, or remove this chunk and add a RETURN_TO_GROK request.",
                )
            }
        }
        return ids.toSet()
    }

    private fun validateProductionRequests(
        requests: List<GrokProductionRequest>,
        chunkIds: Set<String>,
        findings: MutableList<GrokStoryHandoffFinding>,
    ): RequestValidation {
        var linked = 0
        var mismatch = 0
        val ids = requests.map { it.requestId.trim() }.filter { it.isNotBlank() }
        duplicateValues(ids).forEach { duplicate ->
            findings += finding("GROK_REQUEST_IDS_NOT_UNIQUE", "structure", "CRITICAL", "$.productionRequests", "Production request ids must be unique.", listOf(duplicate))
        }
        requests.forEachIndexed { index, request ->
            val path = "$.productionRequests[$index]"
            requireField(request.requestId, "$path.requestId", "GROK_REQUEST_ID_MISSING", "request id", findings)
            requireField(request.requestType, "$path.requestType", "GROK_REQUEST_TYPE_MISSING", "request type", findings)
            requireField(request.request, "$path.request", "GROK_REQUEST_TEXT_MISSING", "request text", findings)
            if (request.requestType.trim().lowercase() !in allowedRequestTypes) {
                findings += finding(
                    "GROK_REQUEST_TYPE_NOT_ALLOWED",
                    "structure",
                    "CRITICAL",
                    "$path.requestType",
                    "Production request type is not supported.",
                    listOf(request.requestType),
                    "Use scene_summary, shot_candidates, image_prompt_drafts, reference_check, integration, or validation.",
                )
            }
            if (request.targetChunkId.isNotBlank()) {
                if (request.targetChunkId !in chunkIds) {
                    mismatch += 1
                    findings += finding(
                        "GROK_REQUEST_TARGET_CHUNK_MISSING",
                        "cross_reference",
                        "CRITICAL",
                        "$path.targetChunkId",
                        "Production request targetChunkId must reference an existing story chunk.",
                        listOf(request.targetChunkId),
                    )
                } else {
                    linked += 1
                }
            }
        }
        return RequestValidation(linked, mismatch)
    }

    private fun validateReferenceRequirements(
        requirements: List<GrokReferenceRequirement>,
        findings: MutableList<GrokStoryHandoffFinding>,
    ) {
        val ids = requirements.map { it.requirementId.trim() }.filter { it.isNotBlank() }
        duplicateValues(ids).forEach { duplicate ->
            findings += finding("GROK_REFERENCE_IDS_NOT_UNIQUE", "structure", "CRITICAL", "$.referenceRequirements", "Reference requirement ids must be unique.", listOf(duplicate))
        }
        requirements.forEachIndexed { index, requirement ->
            val path = "$.referenceRequirements[$index]"
            requireField(requirement.requirementId, "$path.requirementId", "GROK_REFERENCE_ID_MISSING", "reference requirement id", findings)
            requireField(requirement.label, "$path.label", "GROK_REFERENCE_LABEL_MISSING", "reference label", findings)
            if (requirement.path.isBlank() && requirement.description.isBlank()) {
                findings += finding(
                    "GROK_REFERENCE_DETAIL_MISSING",
                    "structure",
                    "CRITICAL",
                    path,
                    "Reference requirement needs a path or description.",
                )
            }
            if (requirement.requiredFor.isEmpty()) {
                findings += finding(
                    "GROK_REFERENCE_REQUIRED_FOR_MISSING",
                    "structure",
                    "MAJOR",
                    "$path.requiredFor",
                    "Reference requirement should say what it is required for.",
                )
            }
        }
    }

    private fun validateReturnToGrok(
        requests: List<ReturnToGrokRequest>,
        findings: MutableList<GrokStoryHandoffFinding>,
    ) {
        val ids = requests.map { it.requestId.trim() }.filter { it.isNotBlank() }
        duplicateValues(ids).forEach { duplicate ->
            findings += finding("RETURN_TO_GROK_IDS_NOT_UNIQUE", "structure", "CRITICAL", "$.returnToGrok", "RETURN_TO_GROK ids must be unique.", listOf(duplicate))
        }
        requests.forEachIndexed { index, request ->
            val path = "$.returnToGrok[$index]"
            requireField(request.requestId, "$path.requestId", "RETURN_TO_GROK_ID_MISSING", "return request id", findings)
            requireField(request.source, "$path.source", "RETURN_TO_GROK_SOURCE_MISSING", "source", findings)
            requireField(request.location, "$path.location", "RETURN_TO_GROK_LOCATION_MISSING", "location", findings)
            requireField(request.reason, "$path.reason", "RETURN_TO_GROK_REASON_MISSING", "reason", findings)
            requireField(request.neededFromGrok, "$path.neededFromGrok", "RETURN_TO_GROK_NEEDED_MISSING", "needed from Grok", findings)
        }
    }

    private fun validateBoundaries(
        boundaries: GrokCodexBoundaryFlags,
        findings: MutableList<GrokStoryHandoffFinding>,
    ): Int {
        val checks = listOf(
            Triple(boundaries.candidateOnly, "$.boundaries.candidateOnly", "GROK_BOUNDARY_CANDIDATE_ONLY_FALSE"),
            Triple(boundaries.noProviderApiCalls, "$.boundaries.noProviderApiCalls", "GROK_BOUNDARY_PROVIDER_API_FALSE"),
            Triple(boundaries.noGeneratedMediaClaim, "$.boundaries.noGeneratedMediaClaim", "GROK_BOUNDARY_GENERATED_MEDIA_FALSE"),
            Triple(boundaries.noCanonApproval, "$.boundaries.noCanonApproval", "GROK_BOUNDARY_CANON_APPROVAL_FALSE"),
            Triple(boundaries.noAssetApproval, "$.boundaries.noAssetApproval", "GROK_BOUNDARY_ASSET_APPROVAL_FALSE"),
            Triple(boundaries.noProductionLock, "$.boundaries.noProductionLock", "GROK_BOUNDARY_PRODUCTION_LOCK_FALSE"),
            Triple(boundaries.noBlockedStoryRewriteByCodex, "$.boundaries.noBlockedStoryRewriteByCodex", "GROK_BOUNDARY_BLOCKED_REWRITE_FALSE"),
        )
        var unsafe = 0
        checks.forEach { (value, path, ruleId) ->
            if (!value) {
                unsafe += 1
                findings += finding(ruleId, "boundary", "CRITICAL", path, "Grok/Codex handoff boundary flag must remain true.")
            }
        }
        return unsafe
    }

    private fun validateBoundaryText(
        handoff: GrokStoryHandoff,
        findings: MutableList<GrokStoryHandoffFinding>,
    ) {
        collectBoundaryText(handoff).filter { it.value.isNotBlank() }.forEach { field ->
            forbiddenBoundaryTextPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    findings += finding(
                        "GROK_FORBIDDEN_BOUNDARY_CLAIM",
                        "boundary",
                        "CRITICAL",
                        field.path,
                        "Handoff metadata cannot claim approval, final media, generated output, provider job, canon, registry, or production lock.",
                        listOf(field.value),
                        "Keep this handoff candidate-only and move blocked story work to RETURN_TO_GROK.",
                    )
                }
            }
        }
    }

    private fun collectBoundaryText(handoff: GrokStoryHandoff): List<TextField> {
        val fields = mutableListOf(
            TextField("$.handoffId", handoff.handoffId),
            TextField("$.title", handoff.title),
            TextField("$.sourceOwner", handoff.sourceOwner),
            TextField("$.sourceLabel", handoff.sourceLabel),
            TextField("$.storyStatus", handoff.storyStatus),
        )
        handoff.productionRequests.forEachIndexed { index, request ->
            fields += TextField("$.productionRequests[$index].requestType", request.requestType)
            fields += TextField("$.productionRequests[$index].request", request.request)
        }
        handoff.referenceRequirements.forEachIndexed { index, requirement ->
            fields += TextField("$.referenceRequirements[$index].label", requirement.label)
            fields += TextField("$.referenceRequirements[$index].description", requirement.description)
        }
        handoff.returnToGrok.forEachIndexed { index, request ->
            fields += TextField("$.returnToGrok[$index].reason", request.reason)
            fields += TextField("$.returnToGrok[$index].neededFromGrok", request.neededFromGrok)
        }
        return fields
    }

    private fun requireField(
        value: String,
        path: String,
        ruleId: String,
        label: String,
        findings: MutableList<GrokStoryHandoffFinding>,
    ) {
        if (value.isBlank()) {
            findings += finding(ruleId, "structure", "CRITICAL", path, "Grok handoff requires nonblank $label.")
        }
    }

    private fun duplicateValues(values: List<String>): List<String> =
        values.groupingBy { it }.eachCount().filter { it.key.isNotBlank() && it.value > 1 }.keys.toList()

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): GrokStoryHandoffFinding =
        GrokStoryHandoffFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private data class RequestValidation(
        val linkedRequestCount: Int = 0,
        val crossReferenceMismatchCount: Int = 0,
    )

    private data class TextField(val path: String, val value: String)
}

class CodexProductionHandoffDraftService {
    private val requiredValidationStep = "codex_production_handoff_draft"

    fun create(
        grokHandoff: GrokStoryHandoff,
        validationReport: GrokStoryHandoffValidationReport,
        grokHandoffFile: String = "",
        validationReportFile: String = "",
        draftId: String = "",
    ): CodexProductionHandoffDraft {
        val findings = mutableListOf<GrokStoryHandoffFinding>()
        validateUpstream(grokHandoff, validationReport, grokHandoffFile, validationReportFile, findings)
        val failureFindings = findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }
        val upstreamOk = failureFindings.isEmpty()
        val sceneSummaries = if (upstreamOk) buildSceneSummaries(grokHandoff.storyChunks) else emptyList()
        val shotCandidates = if (upstreamOk) buildShotCandidates(grokHandoff.storyChunks) else emptyList()
        val referenceChecks = if (upstreamOk) buildReferenceChecks(grokHandoff.referenceRequirements) else emptyList()
        val promptDrafts = if (upstreamOk) buildImagePromptDrafts(shotCandidates, referenceChecks) else emptyList()
        val returnBlocks = if (upstreamOk) buildReturnBlocks(grokHandoff.returnToGrok) else emptyList()
        val status = when {
            !upstreamOk -> "FAIL"
            returnBlocks.isNotEmpty() -> "CODEX_PRODUCTION_HANDOFF_READY_WITH_RETURN_TO_GROK"
            else -> "CODEX_PRODUCTION_HANDOFF_READY"
        }

        return CodexProductionHandoffDraft(
            status = status,
            draftId = draftId.ifBlank { grokHandoff.handoffId.takeIf { it.isNotBlank() }?.let { "$it-codex-production-handoff-v0" }.orEmpty() },
            grokHandoffFile = grokHandoffFile,
            grokHandoffValidationFile = validationReportFile,
            sourceHandoffId = grokHandoff.handoffId,
            projectId = grokHandoff.projectId,
            title = grokHandoff.title,
            sceneSummaries = sceneSummaries,
            shotCandidates = shotCandidates,
            imagePromptDrafts = promptDrafts,
            referenceChecks = referenceChecks,
            returnToGrok = returnBlocks,
            boundaries = grokHandoff.boundaries,
            nextAllowedDownstreamStep = if (upstreamOk) "user_review_codex_production_handoff" else "",
            blockedReason = if (upstreamOk) "" else failureFindings.joinToString("; ") { it.ruleId },
            findingCount = findings.size,
            findings = findings,
            evidence = CodexProductionHandoffEvidence(
                sourceValidationVerdict = validationReport.verdict,
                sceneSummaryCount = sceneSummaries.size,
                shotCandidateCount = shotCandidates.size,
                imagePromptDraftCount = promptDrafts.size,
                referenceCheckCount = referenceChecks.size,
                returnToGrokCount = returnBlocks.size,
            ),
        )
    }

    private fun validateUpstream(
        handoff: GrokStoryHandoff,
        report: GrokStoryHandoffValidationReport,
        handoffFile: String,
        reportFile: String,
        findings: MutableList<GrokStoryHandoffFinding>,
    ) {
        if (report.verdict != "PASS") {
            findings += finding("GROK_HANDOFF_VALIDATION_NOT_PASS", "upstream", "CRITICAL", "$.validation.verdict", "Codex production handoff requires a PASS Grok handoff validation report.", listOf(report.verdict))
        }
        if (report.nextAllowedDownstreamStep != requiredValidationStep) {
            findings += finding("GROK_HANDOFF_VALIDATION_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.validation.nextAllowedDownstreamStep", "Validation report must allow codex_production_handoff_draft.", listOf(report.nextAllowedDownstreamStep))
        }
        if (handoffFile.isBlank()) {
            findings += finding("GROK_HANDOFF_FILE_MISSING", "source_linkage", "CRITICAL", "$.inputs.grokHandoffFile", "Codex production handoff requires the Grok handoff file path.")
        }
        if (reportFile.isBlank()) {
            findings += finding("GROK_HANDOFF_VALIDATION_FILE_MISSING", "source_linkage", "CRITICAL", "$.inputs.validationReportFile", "Codex production handoff requires the validation report file path.")
        }
        if (handoff.handoffId != report.handoffId) {
            findings += finding("GROK_HANDOFF_ID_MISMATCH", "source_linkage", "CRITICAL", "$.handoffId", "Grok handoff id must match validation report handoff id.", listOf("handoff=${handoff.handoffId}", "validation=${report.handoffId}"))
        }
        if (handoff.projectId != report.projectId) {
            findings += finding("GROK_PROJECT_ID_MISMATCH", "source_linkage", "CRITICAL", "$.projectId", "Grok project id must match validation report project id.", listOf("handoff=${handoff.projectId}", "validation=${report.projectId}"))
        }
    }

    private fun buildSceneSummaries(chunks: List<GrokStoryChunk>): List<CodexSceneSummary> =
        chunks.map { chunk ->
            CodexSceneSummary(
                summaryId = "${chunk.chunkId}-scene-summary",
                sourceChunkId = chunk.chunkId,
                sceneId = chunk.sceneId,
                title = chunk.title,
                location = chunk.location,
                safeSummary = chunk.safeSummary,
                continuityNotes = chunk.continuityNotes,
            )
        }

    private fun buildShotCandidates(chunks: List<GrokStoryChunk>): List<CodexShotCandidate> =
        chunks.map { chunk ->
            CodexShotCandidate(
                shotId = "${chunk.chunkId}-shot-001",
                sourceChunkId = chunk.chunkId,
                sceneId = chunk.sceneId,
                title = chunk.title,
                characterIds = chunk.characterIds,
                visibleAction = chunk.visualIntent.ifBlank { chunk.safeSummary },
                cameraIntent = "Candidate shot from Grok visual intent; requires user review before image generation.",
                audioIntent = chunk.audioIntent,
                candidateOnly = true,
            )
        }

    private fun buildReferenceChecks(requirements: List<GrokReferenceRequirement>): List<CodexReferenceCheck> =
        requirements.map { requirement ->
            CodexReferenceCheck(
                checkId = "${requirement.requirementId}-check",
                requirementId = requirement.requirementId,
                label = requirement.label,
                path = requirement.path,
                description = requirement.description,
                requiredFor = requirement.requiredFor,
                status = "candidate_reference_required",
            )
        }

    private fun buildImagePromptDrafts(
        shots: List<CodexShotCandidate>,
        references: List<CodexReferenceCheck>,
    ): List<CodexImagePromptDraft> =
        shots.map { shot ->
            CodexImagePromptDraft(
                promptId = "${shot.shotId}-prompt",
                sourceShotId = shot.shotId,
                positivePromptDraft = listOf(
                    "candidate-only image prompt",
                    shot.visibleAction,
                    "scene=${shot.sceneId}",
                    "characters=${shot.characterIds.joinToString(",")}",
                ).filter { it.isNotBlank() }.joinToString("; "),
                negativePromptDraft = "no canon approval; no production lock; no unreferenced costume or face drift; no provider metadata",
                referenceRequirementIds = references.map { it.requirementId },
                promptSafetyNotes = listOf(
                    "Use reference-first workflow for formal Chenxing frames.",
                    "Keep output candidate-only until user approval.",
                    "Do not include blocked Grok story text in prompts.",
                ),
            )
        }

    private fun buildReturnBlocks(requests: List<ReturnToGrokRequest>): List<CodexReturnToGrokBlock> =
        requests.map { request ->
            val formatted = """
                [RETURN_TO_GROK]
                Source: ${request.source}
                Location: ${request.location}
                Reason: ${request.reason}
                Needed from Grok: ${request.neededFromGrok}
                Codex can continue after: ${request.codexCanContinueAfter}
            """.trimIndent()
            CodexReturnToGrokBlock(
                requestId = request.requestId,
                source = request.source,
                location = request.location,
                reason = request.reason,
                neededFromGrok = request.neededFromGrok,
                codexCanContinueAfter = request.codexCanContinueAfter,
                formattedBlock = formatted,
            )
        }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): GrokStoryHandoffFinding =
        GrokStoryHandoffFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
}
