package com.localnovel.engine.service

import com.localnovel.engine.model.LocalRendererAdapterCompatibilitySummary
import com.localnovel.engine.model.LocalRendererAdapterDryRunCheck
import com.localnovel.engine.model.LocalRendererAdapterDryRunFinding
import com.localnovel.engine.model.LocalRendererAdapterDryRunReport
import com.localnovel.engine.model.LocalRendererAdapterMappingSummary
import com.localnovel.engine.model.LocalRendererAdapterPanelMappingNote
import com.localnovel.engine.model.LocalRendererAdapterPanelTimelineFieldNotes
import com.localnovel.engine.model.LocalRendererAdapterSubtitleTimelineFieldNotes
import com.localnovel.engine.model.LocalRendererAdapterVoiceTimelineFieldNotes
import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffBoundaries
import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffDraft
import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffReviewReport
import com.localnovel.engine.model.LocalStoryVideoPreviewPanelPlan

class LocalRendererAdapterDryRunService {
    private val requiredHandoffStatus = "LOCAL_STORY_VIDEO_PREVIEW_HANDOFF_DRAFT"
    private val requiredHandoffBoundary = "local_story_video_preview_handoff_draft_only_not_media_generation"
    private val requiredHandoffNextStep = "local_preview_handoff_review"
    private val requiredReviewBoundary = "local_story_video_preview_handoff_review_only_not_media_generation"
    private val requiredReviewNextStep = "local_renderer_adapter_dry_run"
    private val passStatus = "LOCAL_RENDERER_ADAPTER_DRY_RUN_PASS"
    private val failStatus = "LOCAL_RENDERER_ADAPTER_DRY_RUN_FAIL"
    private val downstreamBoundary = "local_renderer_adapter_dry_run_only_not_media_generation"
    private val nextAllowedDownstreamStep = "local_renderer_adapter_implementation_plan"

    fun dryRun(
        handoff: LocalStoryVideoPreviewHandoffDraft,
        review: LocalStoryVideoPreviewHandoffReviewReport,
        handoffFile: String = "",
        reviewFile: String = "",
        dryRunId: String = "",
    ): LocalRendererAdapterDryRunReport {
        val findings = mutableListOf<LocalRendererAdapterDryRunFinding>()
        val mappingPlan = buildMappingPlan(handoff)
        val mappingSummary = mappingSummary(handoff, mappingPlan)
        val checks = buildChecks(handoff, review, handoffFile, reviewFile, mappingSummary, findings)
        val verdict = if (findings.hasBlocking()) "FAIL" else "PASS"
        val resolvedDryRunId = dryRunId.ifBlank {
            review.reviewId.takeIf { it.isNotBlank() }?.let { "$it-adapter-dry-run-v0" }
                ?: handoff.handoffId.takeIf { it.isNotBlank() }?.let { "$it-adapter-dry-run-v0" }
                ?: ""
        }

        return LocalRendererAdapterDryRunReport(
            verdict = verdict,
            status = if (verdict == "PASS") passStatus else failStatus,
            dryRunId = resolvedDryRunId,
            sourceHandoffFile = handoffFile,
            sourceReviewFile = reviewFile,
            sourceHandoffId = handoff.handoffId,
            sourceReviewId = review.reviewId,
            sourceStoryInputPackId = handoff.sourceStoryInputPackId,
            checks = checks,
            adapterMappingSummary = mappingSummary,
            dryRunMappingPlan = if (verdict == "PASS") mappingPlan else emptyList(),
            rendererCompatibilitySummary = compatibilitySummary(handoff, review),
            boundaries = outputBoundaries(handoff, review),
            downstreamBoundary = downstreamBoundary,
            nextAllowedDownstreamStep = if (verdict == "PASS") nextAllowedDownstreamStep else "",
            blockedReason = if (verdict == "PASS") "" else findings
                .filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }
                .joinToString("; ") { it.ruleId },
            findingCount = findings.size,
            findings = findings,
        )
    }

    private fun buildChecks(
        handoff: LocalStoryVideoPreviewHandoffDraft,
        review: LocalStoryVideoPreviewHandoffReviewReport,
        handoffFile: String,
        reviewFile: String,
        mappingSummary: LocalRendererAdapterMappingSummary,
        findings: MutableList<LocalRendererAdapterDryRunFinding>,
    ): List<LocalRendererAdapterDryRunCheck> =
        listOf(
            check(
                checkId = "explicit-source-files",
                category = "source_validation",
                passed = handoffFile.isNotBlank() && reviewFile.isNotBlank(),
                message = "Adapter dry-run requires explicit handoff and review report files.",
                evidence = listOf("handoffFile=$handoffFile", "reviewFile=$reviewFile"),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_DRY_RUN_SOURCE_FILE_MISSING",
                path = "$.inputs",
                suggestedFix = "Pass --handoff, --review, and --out explicitly.",
            ),
            check(
                checkId = "handoff-state",
                category = "source_validation",
                passed = handoff.status == requiredHandoffStatus &&
                    handoff.downstreamBoundary == requiredHandoffBoundary &&
                    handoff.nextAllowedDownstreamStep == requiredHandoffNextStep,
                message = "Adapter dry-run requires the Phase 5 handoff draft state and no-media downstream boundary.",
                evidence = listOf(
                    "status=${handoff.status}",
                    "downstreamBoundary=${handoff.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${handoff.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_DRY_RUN_HANDOFF_STATE_INVALID",
                path = "$.handoff",
                suggestedFix = "Use the PASS Phase 5 preview handoff draft.",
            ),
            check(
                checkId = "review-state",
                category = "source_validation",
                passed = review.verdict == "PASS" &&
                    review.downstreamBoundary == requiredReviewBoundary &&
                    review.nextAllowedDownstreamStep == requiredReviewNextStep,
                message = "Adapter dry-run requires PASS Phase 6 review and nextAllowedDownstreamStep=local_renderer_adapter_dry_run.",
                evidence = listOf(
                    "verdict=${review.verdict}",
                    "downstreamBoundary=${review.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${review.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_DRY_RUN_REVIEW_STATE_INVALID",
                path = "$.review",
                suggestedFix = "Use the PASS Phase 6 preview handoff review report.",
            ),
            check(
                checkId = "source-id-match",
                category = "source_linkage",
                passed = handoff.handoffId.isNotBlank() &&
                    review.reviewId.isNotBlank() &&
                    handoff.sourceStoryInputPackId.isNotBlank() &&
                    review.sourceHandoffId == handoff.handoffId &&
                    review.sourceStoryInputPackId == handoff.sourceStoryInputPackId &&
                    review.handoffStatus == handoff.status,
                message = "Adapter dry-run requires matching handoff id, story input pack id, and reviewed handoff status.",
                evidence = listOf(
                    "handoffId=${handoff.handoffId}",
                    "review.sourceHandoffId=${review.sourceHandoffId}",
                    "handoffStory=${handoff.sourceStoryInputPackId}",
                    "reviewStory=${review.sourceStoryInputPackId}",
                    "review.handoffStatus=${review.handoffStatus}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_DRY_RUN_SOURCE_ID_MISMATCH",
                path = "$.sourceLinkage",
                suggestedFix = "Use the Phase 6 review report generated from the supplied Phase 5 handoff.",
            ),
            check(
                checkId = "local-boundaries",
                category = "boundary",
                passed = localBoundariesOk(handoff.boundaries) && localBoundariesOk(review.boundaries),
                message = "Adapter dry-run requires local-only boundaries with no provider/API, renderer, TTS, media generation, release, or production lock.",
                evidence = listOf(
                    "handoff.mediaGenerated=${handoff.boundaries.mediaGenerated}",
                    "handoff.rendererInvocation=${handoff.boundaries.rendererInvocation}",
                    "handoff.providerInvocation=${handoff.boundaries.providerInvocation}",
                    "handoff.ttsInvocation=${handoff.boundaries.ttsInvocation}",
                    "review.mediaGenerated=${review.boundaries.mediaGenerated}",
                    "review.rendererInvocation=${review.boundaries.rendererInvocation}",
                    "review.providerInvocation=${review.boundaries.providerInvocation}",
                    "review.ttsInvocation=${review.boundaries.ttsInvocation}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_DRY_RUN_BOUNDARY_FAIL",
                path = "$.boundaries",
                suggestedFix = "Use local-only Phase 5/6 JSON with no provider/API, renderer, TTS, media, release, or production lock claims.",
            ),
            check(
                checkId = "renderer-compatibility-not-ready",
                category = "renderer_compatibility",
                passed = !handoff.localRendererCompatibility.ready &&
                    !review.rendererCompatibilitySummary.ready &&
                    review.rendererCompatibilitySummary.readyFalseAccepted,
                message = "Adapter dry-run requires ready=false to remain preserved as the not-yet-direct-renderable signal.",
                evidence = listOf(
                    "handoff.ready=${handoff.localRendererCompatibility.ready}",
                    "review.ready=${review.rendererCompatibilitySummary.ready}",
                    "review.readyFalseAccepted=${review.rendererCompatibilitySummary.readyFalseAccepted}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_DRY_RUN_READY_TRUE_FORBIDDEN",
                path = "$.rendererCompatibility",
                suggestedFix = "Keep ready=false until an explicit adapter implementation and later renderer dry-run exist.",
            ),
            check(
                checkId = "review-checks-pass",
                category = "review_gate",
                passed = review.checks.isNotEmpty() &&
                    review.checks.filter { it.requiredForReview }.all { it.status == "PASS" } &&
                    review.findingCount == 0 &&
                    review.findings.isEmpty(),
                message = "Adapter dry-run requires all required Phase 6 review checks to remain PASS with no findings.",
                evidence = listOf(
                    "reviewChecks=${review.checks.size}",
                    "requiredReviewFail=${review.checks.count { it.requiredForReview && it.status != "PASS" }}",
                    "findingCount=${review.findingCount}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_DRY_RUN_REVIEW_CHECKS_NOT_PASS",
                path = "$.review.checks",
                suggestedFix = "Resolve Phase 6 review findings before adapter dry-run.",
            ),
            check(
                checkId = "preview-plan-mappable",
                category = "adapter_mapping",
                passed = mappingSummary.panelCount > 0 &&
                    mappingSummary.targetDurationMillis > 0 &&
                    mappingSummary.missingRequiredFieldCount == 0 &&
                    mappingSummary.mappedPanelCount == mappingSummary.panelCount &&
                    mappingSummary.unmappedPanelCount == 0,
                message = "Adapter dry-run requires ordered panels with positive duration, cue text, visual refs, and audio refs.",
                evidence = listOf(
                    "panelCount=${mappingSummary.panelCount}",
                    "targetDurationMillis=${mappingSummary.targetDurationMillis}",
                    "mappedPanelCount=${mappingSummary.mappedPanelCount}",
                    "unmappedPanelCount=${mappingSummary.unmappedPanelCount}",
                    "missingRequiredFieldCount=${mappingSummary.missingRequiredFieldCount}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_DRY_RUN_MAPPING_FIELDS_MISSING",
                path = "$.handoff.previewPlan.panelOrdering",
                suggestedFix = "Use a Phase 5 handoff with complete panel ids, source ids, durations, cue text, visual refs, and audio refs.",
            ),
            check(
                checkId = "qa-summary-pass",
                category = "qa",
                passed = mappingSummary.qaChecks > 0 &&
                    mappingSummary.qaPass == mappingSummary.qaChecks &&
                    mappingSummary.qaFail == 0,
                message = "Adapter dry-run requires QA summary with no failed checks.",
                evidence = listOf(
                    "qaChecks=${mappingSummary.qaChecks}",
                    "qaPass=${mappingSummary.qaPass}",
                    "qaFail=${mappingSummary.qaFail}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_DRY_RUN_QA_NOT_PASS",
                path = "$.handoff.previewPlan.qaSummary",
                suggestedFix = "Use a reviewed handoff with zero QA failures.",
            ),
        )

    private fun buildMappingPlan(handoff: LocalStoryVideoPreviewHandoffDraft): List<LocalRendererAdapterPanelMappingNote> =
        handoff.previewPlan.panelOrdering
            .sortedWith(compareBy<LocalStoryVideoPreviewPanelPlan> { it.order }.thenBy { it.startMillis })
            .map { panel ->
                val missing = missingPanelFields(panel)
                LocalRendererAdapterPanelMappingNote(
                    order = panel.order,
                    panelId = panel.panelId,
                    sourceSceneId = panel.sourceSceneId,
                    sourceBeatId = panel.sourceBeatId,
                    startMillis = panel.startMillis,
                    endMillis = panel.endMillis,
                    durationMillis = panel.durationMillis,
                    proposedPanelTimelineFields = LocalRendererAdapterPanelTimelineFieldNotes(
                        wouldMapPanelIdFrom = "$.previewPlan.panelOrdering[${panel.order}].panelId",
                        wouldMapSourceFrameIdFrom = "$.previewPlan.panelOrdering[${panel.order}].visualPromptCardId",
                        wouldMapSceneReferenceFrom = "$.previewPlan.panelOrdering[${panel.order}].sourceSceneId",
                        wouldMapBeatReferenceFrom = "$.previewPlan.panelOrdering[${panel.order}].sourceBeatId",
                        wouldMapVisualIntentFrom = "$.previewPlan.panelOrdering[${panel.order}].visualIntent + visualPromptSeed",
                        wouldMapCameraIntentFrom = "future adapter implementation required; not present as renderer-ready camera intent",
                        wouldMapTimingFrom = "$.previewPlan.panelOrdering[${panel.order}].startMillis/endMillis/durationMillis",
                        wouldMapAssetCandidateRefsFrom = "$.previewPlan.panelOrdering[${panel.order}].assetPromptReference.characterCardIds",
                    ),
                    proposedVoiceTimelineFields = LocalRendererAdapterVoiceTimelineFieldNotes(
                        wouldMapCueIdFrom = "$.previewPlan.panelOrdering[${panel.order}].audioCueCardId",
                        wouldMapPanelIdFrom = "$.previewPlan.panelOrdering[${panel.order}].panelId",
                        wouldMapTextFrom = "$.previewPlan.panelOrdering[${panel.order}].dialogueCue or audioCueSummary",
                        wouldMapTimingFrom = "$.previewPlan.panelOrdering[${panel.order}].startMillis/endMillis/durationMillis",
                        wouldNeedVoiceLabelsFrom = "future adapter implementation must choose voice labels from local story/card data",
                    ),
                    proposedSubtitleTimelineFields = LocalRendererAdapterSubtitleTimelineFieldNotes(
                        wouldMapCueIdFrom = "subtitle-${panel.panelId}",
                        wouldMapPanelIdFrom = "$.previewPlan.panelOrdering[${panel.order}].panelId",
                        wouldMapAudioCueIdFrom = "$.previewPlan.panelOrdering[${panel.order}].audioCueCardId",
                        wouldMapTextFrom = "$.previewPlan.panelOrdering[${panel.order}].captionCue or dialogueCue",
                        wouldMapTimingFrom = "$.previewPlan.panelOrdering[${panel.order}].startMillis/endMillis",
                    ),
                    missingRequiredFields = missing,
                    canMapForFutureAdapter = missing.isEmpty(),
                    manifestGenerated = false,
                    note = "Dry-run mapping note only; ComicVideoRenderHandoffManifest is not generated in Phase 7.",
                )
            }

    private fun missingPanelFields(panel: LocalStoryVideoPreviewPanelPlan): List<String> {
        val missing = mutableListOf<String>()
        fun require(value: Boolean, field: String) {
            if (!value) missing += field
        }
        require(panel.panelId.isNotBlank(), "panelId")
        require(panel.sourceSceneId.isNotBlank(), "sourceSceneId")
        require(panel.sourceBeatId.isNotBlank(), "sourceBeatId")
        require(panel.durationMillis > 0, "durationMillis")
        require(panel.endMillis > panel.startMillis, "endMillis")
        require(panel.captionCue.isNotBlank() || panel.dialogueCue.isNotBlank(), "captionCueOrDialogueCue")
        require(panel.visualPromptCardId.isNotBlank(), "visualPromptCardId")
        require(panel.visualPromptSeed.isNotBlank() || panel.visualIntent.isNotBlank(), "visualPromptSeedOrVisualIntent")
        require(panel.audioCueCardId.isNotBlank(), "audioCueCardId")
        require(panel.audioCueSummary.isNotBlank() || panel.audioIntent.isNotBlank() || panel.dialogueCue.isNotBlank(), "audioCueSummaryOrAudioIntent")
        return missing
    }

    private fun mappingSummary(
        handoff: LocalStoryVideoPreviewHandoffDraft,
        mappingPlan: List<LocalRendererAdapterPanelMappingNote>,
    ): LocalRendererAdapterMappingSummary {
        val missingCount = mappingPlan.sumOf { it.missingRequiredFields.size }
        return LocalRendererAdapterMappingSummary(
            sceneCount = handoff.previewPlan.sceneOrdering.size,
            panelCount = handoff.previewPlan.panelOrdering.size,
            targetDurationMillis = handoff.previewPlan.targetDurationMillis,
            mappedPanelCount = mappingPlan.count { it.canMapForFutureAdapter },
            unmappedPanelCount = mappingPlan.count { !it.canMapForFutureAdapter },
            missingRequiredFieldCount = missingCount,
            visualPromptReferenceCount = handoff.previewPlan.panelOrdering.count { it.visualPromptCardId.isNotBlank() },
            audioCueReferenceCount = handoff.previewPlan.panelOrdering.count { it.audioCueCardId.isNotBlank() },
            qaChecks = handoff.previewPlan.qaSummary.qaChecks,
            qaPass = handoff.previewPlan.qaSummary.qaPass,
            qaFail = handoff.previewPlan.qaSummary.qaFail,
        )
    }

    private fun compatibilitySummary(
        handoff: LocalStoryVideoPreviewHandoffDraft,
        review: LocalStoryVideoPreviewHandoffReviewReport,
    ): LocalRendererAdapterCompatibilitySummary =
        LocalRendererAdapterCompatibilitySummary(
            ready = handoff.localRendererCompatibility.ready || review.rendererCompatibilitySummary.ready,
            currentRendererExpectedInput = handoff.localRendererCompatibility.currentRendererExpectedInput
                .ifBlank { review.rendererCompatibilitySummary.currentRendererExpectedInput },
            currentDraftInput = handoff.localRendererCompatibility.currentDraftInput
                .ifBlank { review.rendererCompatibilitySummary.currentDraftInput },
            adapterImplementationRequired = true,
            manifestGenerated = false,
            requiredImplementationWork = listOf(
                "Design adapter fields for ComicVideoRenderHandoffManifest in a future explicit task.",
                "Choose local sourceFrameId, mustShow, doNotShow, and continuityAnchors policy.",
                "Choose local voice labels and subtitle text policy.",
                "Run a later renderer dry-run/preflight before any media generation.",
            ),
            summary = "Dry-run only: mapping data is present, but adapter implementation and renderer dry-run remain required before any MP4 generation.",
        )

    private fun outputBoundaries(
        handoff: LocalStoryVideoPreviewHandoffDraft,
        review: LocalStoryVideoPreviewHandoffReviewReport,
    ): LocalStoryVideoPreviewHandoffBoundaries =
        LocalStoryVideoPreviewHandoffBoundaries(
            fileProtocolJsonOnly = handoff.boundaries.fileProtocolJsonOnly && review.boundaries.fileProtocolJsonOnly,
            noExternalPaidProviderTools = handoff.boundaries.noExternalPaidProviderTools &&
                review.boundaries.noExternalPaidProviderTools,
            providerInvocation = if (handoff.boundaries.providerInvocation == "none" &&
                review.boundaries.providerInvocation == "none"
            ) "none" else "present",
            ttsInvocation = if (handoff.boundaries.ttsInvocation == "none" &&
                review.boundaries.ttsInvocation == "none"
            ) "none" else "present",
            mediaGenerated = false,
            rendererInvocation = "none",
            directSqliteWrites = false,
            providerApiCalls = false,
            finalMediaApproval = false,
            release = false,
            canonRegistryOrProductionLock = false,
        )

    private fun localBoundariesOk(boundaries: LocalStoryVideoPreviewHandoffBoundaries): Boolean =
        boundaries.fileProtocolJsonOnly &&
            boundaries.noExternalPaidProviderTools &&
            boundaries.providerInvocation == "none" &&
            boundaries.ttsInvocation == "none" &&
            !boundaries.mediaGenerated &&
            boundaries.rendererInvocation == "none" &&
            !boundaries.directSqliteWrites &&
            !boundaries.providerApiCalls &&
            !boundaries.finalMediaApproval &&
            !boundaries.release &&
            !boundaries.canonRegistryOrProductionLock

    private fun check(
        checkId: String,
        category: String,
        passed: Boolean,
        message: String,
        evidence: List<String>,
        findings: MutableList<LocalRendererAdapterDryRunFinding>,
        ruleId: String,
        path: String,
        suggestedFix: String,
    ): LocalRendererAdapterDryRunCheck {
        if (!passed) {
            findings += finding(
                ruleId = ruleId,
                category = category,
                severity = "CRITICAL",
                path = path,
                message = message,
                evidence = evidence,
                suggestedFix = suggestedFix,
            )
        }
        return LocalRendererAdapterDryRunCheck(
            checkId = checkId,
            status = if (passed) "PASS" else "FAIL",
            category = category,
            message = message,
            evidence = evidence,
        )
    }

    private fun List<LocalRendererAdapterDryRunFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): LocalRendererAdapterDryRunFinding =
        LocalRendererAdapterDryRunFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
}
