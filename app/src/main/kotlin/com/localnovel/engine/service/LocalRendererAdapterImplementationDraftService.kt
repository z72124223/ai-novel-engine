package com.localnovel.engine.service

import com.localnovel.engine.model.LocalRendererAdapterDraftFieldMapping
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraft
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftBoundarySummary
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftBoundaries
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftCheck
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftFinding
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftSourceAlignmentSummary
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftSummary
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftTask
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftVerificationStep
import com.localnovel.engine.model.LocalRendererAdapterImplementationPanelPlan
import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanBoundaries
import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanDraft
import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanReviewReport
import com.localnovel.engine.model.LocalRendererAdapterMappingDraft
import com.localnovel.engine.model.LocalRendererAdapterPanelImplementationDraft
import com.localnovel.engine.model.LocalRendererAdapterRenderSettingsDraft
import com.localnovel.engine.model.LocalRendererAdapterTargetFieldMappingNote
import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffBoundaries
import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffDraft
import com.localnovel.engine.model.LocalStoryVideoPreviewPanelPlan

class LocalRendererAdapterImplementationDraftService {
    private val requiredHandoffStatus = "LOCAL_STORY_VIDEO_PREVIEW_HANDOFF_DRAFT"
    private val requiredPlanStatus = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_DRAFT"
    private val requiredPlanBoundary = "local_renderer_adapter_implementation_plan_only_not_implementation"
    private val requiredPlanNextStep = "local_renderer_adapter_implementation_review"
    private val requiredReviewStatus = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_REVIEW_PASS"
    private val requiredReviewBoundary = "local_renderer_adapter_implementation_plan_review_only_not_implementation"
    private val requiredReviewNextStep = "local_renderer_adapter_implementation_draft"
    private val passStatus = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT"
    private val failStatus = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_FAIL"
    private val downstreamBoundary = "local_renderer_adapter_implementation_draft_only_not_manifest_generation"
    private val nextAllowedDownstreamStep = "local_renderer_adapter_implementation_draft_review"

    fun draft(
        handoff: LocalStoryVideoPreviewHandoffDraft,
        plan: LocalRendererAdapterImplementationPlanDraft,
        planReview: LocalRendererAdapterImplementationPlanReviewReport,
        handoffFile: String = "",
        planFile: String = "",
        planReviewFile: String = "",
        draftId: String = "",
    ): LocalRendererAdapterImplementationDraft {
        val findings = mutableListOf<LocalRendererAdapterImplementationDraftFinding>()
        val alignment = sourceAlignmentSummary(handoff, plan, planReview)
        val summary = draftSummary(handoff, plan)
        val boundarySummary = boundarySummary(handoff, plan, planReview)
        val checks = buildChecks(
            handoff = handoff,
            plan = plan,
            planReview = planReview,
            handoffFile = handoffFile,
            planFile = planFile,
            planReviewFile = planReviewFile,
            alignment = alignment,
            summary = summary,
            boundarySummary = boundarySummary,
            findings = findings,
        )
        val verdict = if (findings.hasBlocking()) "FAIL" else "PASS"
        val resolvedDraftId = draftId.ifBlank {
            plan.planId.takeIf { it.isNotBlank() }?.let { "$it-implementation-draft-v0" }.orEmpty()
        }

        return LocalRendererAdapterImplementationDraft(
            verdict = verdict,
            status = if (verdict == "PASS") passStatus else failStatus,
            draftId = resolvedDraftId,
            sourceHandoffFile = handoffFile,
            sourcePlanFile = planFile,
            sourcePlanReviewFile = planReviewFile,
            sourcePlanId = plan.planId,
            sourcePlanReviewId = planReview.reviewId,
            sourceDryRunId = plan.sourceDryRunId,
            sourceHandoffId = handoff.handoffId,
            sourceHandoffReviewId = plan.sourceReviewId,
            sourceStoryInputPackId = handoff.sourceStoryInputPackId,
            checks = checks,
            implementationDraftSummary = summary,
            sourceAlignmentSummary = alignment,
            adapterMappingDraft = if (verdict == "PASS") adapterMappingDraft(handoff, plan) else LocalRendererAdapterMappingDraft(),
            implementationTasks = if (verdict == "PASS") implementationTasks(plan) else emptyList(),
            verificationPlan = if (verdict == "PASS") verificationPlan(plan) else emptyList(),
            boundarySummary = boundarySummary,
            boundaries = outputBoundaries(),
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
        plan: LocalRendererAdapterImplementationPlanDraft,
        planReview: LocalRendererAdapterImplementationPlanReviewReport,
        handoffFile: String,
        planFile: String,
        planReviewFile: String,
        alignment: LocalRendererAdapterImplementationDraftSourceAlignmentSummary,
        summary: LocalRendererAdapterImplementationDraftSummary,
        boundarySummary: LocalRendererAdapterImplementationDraftBoundarySummary,
        findings: MutableList<LocalRendererAdapterImplementationDraftFinding>,
    ): List<LocalRendererAdapterImplementationDraftCheck> =
        listOf(
            check(
                checkId = "explicit-source-files",
                category = "source_validation",
                passed = handoffFile.isNotBlank() && planFile.isNotBlank() && planReviewFile.isNotBlank(),
                message = "Implementation draft requires explicit Phase 5 handoff, Phase 8 plan, and Phase 9 review files.",
                evidence = listOf(
                    "handoffFile=$handoffFile",
                    "planFile=$planFile",
                    "planReviewFile=$planReviewFile",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_SOURCE_FILE_MISSING",
                path = "$.inputs",
                suggestedFix = "Pass --handoff, --plan, --plan-review, and --out explicitly.",
            ),
            check(
                checkId = "handoff-state",
                category = "source_validation",
                passed = handoff.status == requiredHandoffStatus &&
                    handoff.downstreamBoundary == "local_story_video_preview_handoff_draft_only_not_media_generation" &&
                    handoff.nextAllowedDownstreamStep == "local_preview_handoff_review",
                message = "Implementation draft requires a Phase 5 preview handoff draft that still points to local preview handoff review.",
                evidence = listOf(
                    "status=${handoff.status}",
                    "downstreamBoundary=${handoff.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${handoff.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_HANDOFF_STATE_INVALID",
                path = "$.handoff",
                suggestedFix = "Use the accepted Phase 5 preview handoff draft sample or regenerate it through Phase 5.",
            ),
            check(
                checkId = "plan-state",
                category = "source_validation",
                passed = plan.verdict == "PASS" &&
                    plan.status == requiredPlanStatus &&
                    plan.downstreamBoundary == requiredPlanBoundary &&
                    plan.nextAllowedDownstreamStep == requiredPlanNextStep,
                message = "Implementation draft requires PASS Phase 8 implementation plan state.",
                evidence = listOf(
                    "verdict=${plan.verdict}",
                    "status=${plan.status}",
                    "downstreamBoundary=${plan.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${plan.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_PLAN_STATE_INVALID",
                path = "$.plan",
                suggestedFix = "Use the accepted PASS Phase 8 implementation plan.",
            ),
            check(
                checkId = "plan-review-state",
                category = "source_validation",
                passed = planReview.verdict == "PASS" &&
                    planReview.status == requiredReviewStatus &&
                    planReview.downstreamBoundary == requiredReviewBoundary &&
                    planReview.nextAllowedDownstreamStep == requiredReviewNextStep,
                message = "Implementation draft requires PASS Phase 9 review state and implementation-draft downstream step.",
                evidence = listOf(
                    "verdict=${planReview.verdict}",
                    "status=${planReview.status}",
                    "downstreamBoundary=${planReview.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${planReview.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_PLAN_REVIEW_STATE_INVALID",
                path = "$.planReview",
                suggestedFix = "Use the accepted PASS Phase 9 implementation plan review report.",
            ),
            check(
                checkId = "source-ids-align",
                category = "source_linkage",
                passed = alignment.storyInputPackIdAligned &&
                    alignment.handoffIdAligned &&
                    alignment.handoffReviewIdAligned &&
                    alignment.dryRunIdAligned &&
                    alignment.planIdAligned,
                message = "Implementation draft requires Phase 5/8/9 story, handoff, handoff-review, dry-run, and plan ids to align.",
                evidence = listOf(
                    "storyInputPackIdAligned=${alignment.storyInputPackIdAligned}",
                    "handoffIdAligned=${alignment.handoffIdAligned}",
                    "handoffReviewIdAligned=${alignment.handoffReviewIdAligned}",
                    "dryRunIdAligned=${alignment.dryRunIdAligned}",
                    "planIdAligned=${alignment.planIdAligned}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_SOURCE_ID_MISMATCH",
                path = "$.sourceAlignmentSummary",
                suggestedFix = "Use Phase 5/8/9 JSON files generated from the same story-video chain.",
            ),
            check(
                checkId = "local-boundaries",
                category = "boundary",
                passed = boundarySummary.handoffLocalOnly &&
                    boundarySummary.planLocalOnly &&
                    boundarySummary.reviewLocalOnly &&
                    !boundarySummary.mediaGenerated &&
                    boundarySummary.rendererInvocation == "none" &&
                    boundarySummary.providerInvocation == "none" &&
                    boundarySummary.ttsInvocation == "none" &&
                    !boundarySummary.manifestGenerated &&
                    !boundarySummary.adapterImplementationExecuted &&
                    !boundarySummary.finalMediaApproval &&
                    !boundarySummary.providerMediaHandoff &&
                    !boundarySummary.canonRegistryOrProductionLock,
                message = "Implementation draft requires local-only no-media boundaries across all Phase 5/8/9 inputs.",
                evidence = listOf(
                    "handoffLocalOnly=${boundarySummary.handoffLocalOnly}",
                    "planLocalOnly=${boundarySummary.planLocalOnly}",
                    "reviewLocalOnly=${boundarySummary.reviewLocalOnly}",
                    "mediaGenerated=${boundarySummary.mediaGenerated}",
                    "rendererInvocation=${boundarySummary.rendererInvocation}",
                    "providerInvocation=${boundarySummary.providerInvocation}",
                    "ttsInvocation=${boundarySummary.ttsInvocation}",
                    "manifestGenerated=${boundarySummary.manifestGenerated}",
                    "adapterImplementationExecuted=${boundarySummary.adapterImplementationExecuted}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_BOUNDARY_FAIL",
                path = "$.boundaries",
                suggestedFix = "Use local-only JSON with no provider/API, renderer, TTS, media, adapter execution, manifest generation, release, or production lock.",
            ),
            check(
                checkId = "no-findings-in-sources",
                category = "review_gate",
                passed = plan.findingCount == 0 &&
                    plan.findings.isEmpty() &&
                    planReview.findingCount == 0 &&
                    planReview.findings.isEmpty(),
                message = "Implementation draft requires no findings in Phase 8 plan or Phase 9 review.",
                evidence = listOf(
                    "planFindingCount=${plan.findingCount}",
                    "planReviewFindingCount=${planReview.findingCount}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_SOURCE_FINDINGS_PRESENT",
                path = "$.sourceFindings",
                suggestedFix = "Resolve Phase 8/9 findings before drafting implementation.",
            ),
            check(
                checkId = "required-content-present",
                category = "draft_completeness",
                passed = handoff.previewPlan.panelOrdering.isNotEmpty() &&
                    plan.requiredAdapterWorkItems.isNotEmpty() &&
                    plan.implementationSteps.isNotEmpty() &&
                    plan.verificationPlan.isNotEmpty() &&
                    summary.targetMappingNoteCount > 0 &&
                    summary.missingMappingNoteCount == 0 &&
                    summary.previewPanelCount > 0 &&
                    summary.targetDurationMillis > 0,
                message = "Implementation draft requires non-empty preview panels, work items, implementation steps, verification plan, and mapping notes.",
                evidence = listOf(
                    "previewPanels=${summary.previewPanelCount}",
                    "requiredAdapterWorkItems=${summary.requiredAdapterWorkItems}",
                    "implementationSteps=${plan.implementationSteps.size}",
                    "verificationSteps=${plan.verificationPlan.size}",
                    "targetMappingNoteCount=${summary.targetMappingNoteCount}",
                    "missingMappingNoteCount=${summary.missingMappingNoteCount}",
                    "targetDurationMillis=${summary.targetDurationMillis}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_CONTENT_MISSING",
                path = "$.implementationDraftSummary",
                suggestedFix = "Use complete Phase 5 handoff and Phase 8 plan samples with preview panels and target mapping notes.",
            ),
            check(
                checkId = "mapping-remains-draft-only",
                category = "manifest_boundary",
                passed = plan.targetManifestFieldMappingPlan.planningOnly &&
                    !plan.targetManifestFieldMappingPlan.manifestGenerated &&
                    plan.targetManifestFieldMappingPlan.perPanelMappingNotes.all { !it.manifestGenerated } &&
                    targetFieldNotes(plan).all { !it.generatedNow },
                message = "Implementation draft may mention ComicVideoRenderHandoffManifest only as draft mapping notes; it must not generate the manifest.",
                evidence = listOf(
                    "planningOnly=${plan.targetManifestFieldMappingPlan.planningOnly}",
                    "manifestGenerated=${plan.targetManifestFieldMappingPlan.manifestGenerated}",
                    "perPanelManifestGenerated=${plan.targetManifestFieldMappingPlan.perPanelMappingNotes.count { it.manifestGenerated }}",
                    "generatedNowNotes=${targetFieldNotes(plan).count { it.generatedNow }}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_MANIFEST_GENERATED_FORBIDDEN",
                path = "$.adapterMappingDraft",
                suggestedFix = "Keep mapping as draft notes only and defer any manifest generation to a future explicit task.",
            ),
            check(
                checkId = "no-readiness-overclaim",
                category = "renderer_compatibility",
                passed = !handoff.localRendererCompatibility.ready &&
                    !planReview.rendererCompatibilitySummary.ready &&
                    !planReview.rendererCompatibilitySummary.mp4ReadinessClaimed,
                message = "Implementation draft must not claim renderer readiness or MP4 readiness.",
                evidence = listOf(
                    "handoff.localRendererReady=${handoff.localRendererCompatibility.ready}",
                    "review.rendererReady=${planReview.rendererCompatibilitySummary.ready}",
                    "review.mp4ReadinessClaimed=${planReview.rendererCompatibilitySummary.mp4ReadinessClaimed}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_READY_OVERCLAIM",
                path = "$.rendererCompatibility",
                suggestedFix = "Keep rendererReady=false and mp4ReadinessClaimed=false until future explicit renderer/MP4 verification.",
            ),
        )

    private fun sourceAlignmentSummary(
        handoff: LocalStoryVideoPreviewHandoffDraft,
        plan: LocalRendererAdapterImplementationPlanDraft,
        planReview: LocalRendererAdapterImplementationPlanReviewReport,
    ): LocalRendererAdapterImplementationDraftSourceAlignmentSummary =
        LocalRendererAdapterImplementationDraftSourceAlignmentSummary(
            storyInputPackIdAligned = handoff.sourceStoryInputPackId.isNotBlank() &&
                handoff.sourceStoryInputPackId == plan.sourceStoryInputPackId &&
                handoff.sourceStoryInputPackId == planReview.sourceStoryInputPackId,
            handoffIdAligned = handoff.handoffId.isNotBlank() &&
                handoff.handoffId == plan.sourceHandoffId &&
                handoff.handoffId == planReview.sourceHandoffId,
            handoffReviewIdAligned = plan.sourceReviewId.isNotBlank() &&
                plan.sourceReviewId == planReview.sourceReviewId,
            dryRunIdAligned = plan.sourceDryRunId.isNotBlank() &&
                plan.sourceDryRunId == planReview.sourceDryRunId,
            planIdAligned = plan.planId.isNotBlank() &&
                plan.planId == planReview.sourcePlanId,
            handoffStoryInputPackId = handoff.sourceStoryInputPackId,
            planStoryInputPackId = plan.sourceStoryInputPackId,
            reviewStoryInputPackId = planReview.sourceStoryInputPackId,
            handoffId = handoff.handoffId,
            planSourceHandoffId = plan.sourceHandoffId,
            reviewSourceHandoffId = planReview.sourceHandoffId,
            planSourceHandoffReviewId = plan.sourceReviewId,
            reviewSourceHandoffReviewId = planReview.sourceReviewId,
            planSourceDryRunId = plan.sourceDryRunId,
            reviewSourceDryRunId = planReview.sourceDryRunId,
            planId = plan.planId,
            reviewSourcePlanId = planReview.sourcePlanId,
        )

    private fun draftSummary(
        handoff: LocalStoryVideoPreviewHandoffDraft,
        plan: LocalRendererAdapterImplementationPlanDraft,
    ): LocalRendererAdapterImplementationDraftSummary {
        val fieldNotes = targetFieldNotes(plan)
        val panelNotes = plan.targetManifestFieldMappingPlan.perPanelMappingNotes
        val missingMappingNotes = handoff.previewPlan.panelOrdering.count { panel ->
            panelNotes.none { it.panelId == panel.panelId }
        }
        return LocalRendererAdapterImplementationDraftSummary(
            previewPanelCount = handoff.previewPlan.panelOrdering.size,
            mappingDraftPanelCount = panelNotes.size,
            requiredAdapterWorkItems = plan.requiredAdapterWorkItems.size,
            implementationTaskCount = plan.requiredAdapterWorkItems.size,
            verificationStepCount = plan.verificationPlan.size + 1,
            targetMappingNoteCount = fieldNotes.size,
            missingMappingNoteCount = missingMappingNotes,
            targetDurationMillis = handoff.previewPlan.targetDurationMillis,
            manifestGenerated = false,
            adapterImplementationExecuted = false,
        )
    }

    private fun boundarySummary(
        handoff: LocalStoryVideoPreviewHandoffDraft,
        plan: LocalRendererAdapterImplementationPlanDraft,
        planReview: LocalRendererAdapterImplementationPlanReviewReport,
    ): LocalRendererAdapterImplementationDraftBoundarySummary =
        LocalRendererAdapterImplementationDraftBoundarySummary(
            handoffLocalOnly = localHandoffBoundariesOk(handoff.boundaries),
            planLocalOnly = localPlanBoundariesOk(plan.boundaries),
            reviewLocalOnly = localPlanBoundariesOk(planReview.boundaries),
            mediaGenerated = handoff.boundaries.mediaGenerated ||
                plan.boundaries.mediaGenerated ||
                planReview.boundaries.mediaGenerated,
            rendererInvocation = firstUnsafeInvocation(
                handoff.boundaries.rendererInvocation,
                plan.boundaries.rendererInvocation,
                planReview.boundaries.rendererInvocation,
            ),
            providerInvocation = firstUnsafeInvocation(
                handoff.boundaries.providerInvocation,
                plan.boundaries.providerInvocation,
                planReview.boundaries.providerInvocation,
            ),
            ttsInvocation = firstUnsafeInvocation(
                handoff.boundaries.ttsInvocation,
                plan.boundaries.ttsInvocation,
                planReview.boundaries.ttsInvocation,
            ),
            manifestGenerated = plan.boundaries.manifestGenerated ||
                plan.targetManifestFieldMappingPlan.manifestGenerated ||
                planReview.boundaries.manifestGenerated,
            adapterImplementationExecuted = plan.boundaries.adapterImplementationExecuted ||
                planReview.boundaries.adapterImplementationExecuted,
            finalMediaApproval = handoff.boundaries.finalMediaApproval ||
                plan.boundaries.finalMediaApproval ||
                planReview.boundaries.finalMediaApproval,
            providerMediaHandoff = plan.boundaries.providerMediaHandoff ||
                planReview.boundaries.providerMediaHandoff,
            canonRegistryOrProductionLock = handoff.boundaries.canonRegistryOrProductionLock ||
                plan.boundaries.canonRegistryOrProductionLock ||
                planReview.boundaries.canonRegistryOrProductionLock,
        )

    private fun adapterMappingDraft(
        handoff: LocalStoryVideoPreviewHandoffDraft,
        plan: LocalRendererAdapterImplementationPlanDraft,
    ): LocalRendererAdapterMappingDraft =
        LocalRendererAdapterMappingDraft(
            draftOnly = true,
            manifestGenerated = false,
            targetManifestType = plan.targetManifestFieldMappingPlan.targetManifestType.ifBlank {
                "ComicVideoRenderHandoffManifest"
            },
            mappingBoundary = "mapping_draft_only_not_manifest_generation",
            panelDrafts = handoff.previewPlan.panelOrdering.map { panel ->
                panelDraft(panel, plan.targetManifestFieldMappingPlan.perPanelMappingNotes.find { it.panelId == panel.panelId })
            },
            renderSettingsDraft = LocalRendererAdapterRenderSettingsDraft(
                totalDurationMillis = handoff.previewPlan.targetDurationMillis,
                timelineStatusHint = "metadata_only_timing_plan",
                frameRateHint = "24",
                resolutionHint = "1920x1080",
                rendererBoundaryHint = "local_renderer_adapter_draft_only_not_renderer_invocation",
                generatedNow = false,
            ),
            note = "Draft mapping only. ComicVideoRenderHandoffManifest is not generated in Phase 10.",
        )

    private fun panelDraft(
        panel: LocalStoryVideoPreviewPanelPlan,
        panelPlan: LocalRendererAdapterImplementationPanelPlan?,
    ): LocalRendererAdapterPanelImplementationDraft =
        LocalRendererAdapterPanelImplementationDraft(
            order = panel.order,
            panelId = panel.panelId,
            sourceSceneId = panel.sourceSceneId,
            sourceBeatId = panel.sourceBeatId,
            startMillis = panel.startMillis,
            endMillis = panel.endMillis,
            durationMillis = panel.durationMillis,
            captionCue = panel.captionCue,
            dialogueCue = panel.dialogueCue,
            visualPromptCardId = panel.visualPromptCardId,
            audioCueCardId = panel.audioCueCardId,
            targetPanelTimelineDraft = panelPlan?.plannedPanelTimelineMapping.orEmpty().map {
                fieldMapping(
                    note = it,
                    panel = panel,
                    sourcePlanPath = "$.targetManifestFieldMappingPlan.perPanelMappingNotes[panelId=${panel.panelId}].plannedPanelTimelineMapping",
                    draftValueHint = panelHint(panel, it.targetField),
                )
            },
            targetVoiceTimelineDraft = panelPlan?.plannedVoiceTimelineMapping.orEmpty().map {
                fieldMapping(
                    note = it,
                    panel = panel,
                    sourcePlanPath = "$.targetManifestFieldMappingPlan.perPanelMappingNotes[panelId=${panel.panelId}].plannedVoiceTimelineMapping",
                    draftValueHint = panelHint(panel, it.targetField),
                )
            },
            targetSubtitleTimelineDraft = panelPlan?.plannedSubtitleTimelineMapping.orEmpty().map {
                fieldMapping(
                    note = it,
                    panel = panel,
                    sourcePlanPath = "$.targetManifestFieldMappingPlan.perPanelMappingNotes[panelId=${panel.panelId}].plannedSubtitleTimelineMapping",
                    draftValueHint = panelHint(panel, it.targetField),
                )
            },
            manifestGenerated = false,
            rendererReady = false,
            note = "Panel mapping draft only; no adapter execution, renderer invocation, TTS, media generation, or manifest generation.",
        )

    private fun fieldMapping(
        note: LocalRendererAdapterTargetFieldMappingNote,
        panel: LocalStoryVideoPreviewPanelPlan,
        sourcePlanPath: String,
        draftValueHint: String,
    ): LocalRendererAdapterDraftFieldMapping =
        LocalRendererAdapterDraftFieldMapping(
            targetSection = note.targetSection,
            targetField = note.targetField,
            sourcePreviewPath = "$.previewPlan.panelOrdering[panelId=${panel.panelId}]",
            sourcePlanPath = sourcePlanPath,
            draftValueHint = draftValueHint,
            implementationNote = note.implementationNote,
            required = note.required,
            generatedNow = false,
        )

    private fun panelHint(panel: LocalStoryVideoPreviewPanelPlan, targetField: String): String =
        when (targetField) {
            "panelId" -> panel.panelId
            "sourceFrameId" -> panel.visualPromptCardId
            "sceneReference" -> panel.sourceSceneId
            "beatReference" -> panel.sourceBeatId
            "visualIntent" -> panel.visualIntent
            "cameraIntent" -> panel.sceneTitle
            "startMillis" -> panel.startMillis.toString()
            "endMillis" -> panel.endMillis.toString()
            "displayDurationMillis", "durationMillis" -> panel.durationMillis.toString()
            "assetCandidateRefs" -> panel.characterCardIds.joinToString(",")
            "cueId", "audioCueId" -> panel.audioCueCardId
            "voiceLabel" -> panel.audioCueSummary
            "text" -> listOf(panel.dialogueCue, panel.captionCue).filter { it.isNotBlank() }.joinToString(" / ")
            else -> ""
        }

    private fun implementationTasks(
        plan: LocalRendererAdapterImplementationPlanDraft,
    ): List<LocalRendererAdapterImplementationDraftTask> =
        plan.requiredAdapterWorkItems.mapIndexed { index, workItem ->
            val step = plan.implementationSteps.getOrNull(index)
            LocalRendererAdapterImplementationDraftTask(
                taskId = "implementation-draft-task-${index + 1}",
                sourceWorkItemId = workItem.workItemId,
                sourceImplementationStepId = step?.stepId.orEmpty(),
                status = "DRAFTED",
                title = workItem.title,
                description = workItem.description,
                targetOutcome = workItem.targetOutcome,
                mustNotDo = commonMustNotDo(),
                reviewGateRequiredBeforeExecution = nextAllowedDownstreamStep,
            )
        }

    private fun verificationPlan(
        plan: LocalRendererAdapterImplementationPlanDraft,
    ): List<LocalRendererAdapterImplementationDraftVerificationStep> =
        plan.verificationPlan.mapIndexed { index, verification ->
            LocalRendererAdapterImplementationDraftVerificationStep(
                verificationId = "implementation-draft-verification-${index + 1}",
                sourceVerificationId = verification.verificationId,
                commandOrCheck = verification.commandOrCheck,
                expectedResult = verification.expectedResult,
                requiresMediaGeneration = false,
                requiresManifestGeneration = false,
            )
        } + LocalRendererAdapterImplementationDraftVerificationStep(
            verificationId = "future-manifest-draft-review-gate",
            sourceVerificationId = "phase10-boundary",
            commandOrCheck = "Future explicit task must review this implementation draft before any manifest draft or renderer step.",
            expectedResult = "No ComicVideoRenderHandoffManifest generation, no renderer/ffmpeg/TTS execution, and no media generation before review PASS.",
            requiresMediaGeneration = false,
            requiresManifestGeneration = false,
        )

    private fun outputBoundaries(): LocalRendererAdapterImplementationDraftBoundaries =
        LocalRendererAdapterImplementationDraftBoundaries(
            fileProtocolJsonOnly = true,
            noExternalPaidProviderTools = true,
            providerInvocation = "none",
            ttsInvocation = "none",
            mediaGenerated = false,
            rendererInvocation = "none",
            directSqliteWrites = false,
            providerApiCalls = false,
            adapterImplementationExecuted = false,
            manifestGenerated = false,
            finalMediaApproval = false,
            providerMediaHandoff = false,
            release = false,
            canonRegistryOrProductionLock = false,
            rendererReady = false,
            mp4ReadinessClaimed = false,
        )

    private fun localHandoffBoundariesOk(boundaries: LocalStoryVideoPreviewHandoffBoundaries): Boolean =
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

    private fun localPlanBoundariesOk(boundaries: LocalRendererAdapterImplementationPlanBoundaries): Boolean =
        boundaries.fileProtocolJsonOnly &&
            boundaries.noExternalPaidProviderTools &&
            boundaries.providerInvocation == "none" &&
            boundaries.ttsInvocation == "none" &&
            !boundaries.mediaGenerated &&
            boundaries.rendererInvocation == "none" &&
            !boundaries.directSqliteWrites &&
            !boundaries.providerApiCalls &&
            !boundaries.adapterImplementationExecuted &&
            !boundaries.manifestGenerated &&
            !boundaries.finalMediaApproval &&
            !boundaries.providerMediaHandoff &&
            !boundaries.release &&
            !boundaries.canonRegistryOrProductionLock

    private fun targetFieldNotes(
        plan: LocalRendererAdapterImplementationPlanDraft,
    ): List<LocalRendererAdapterTargetFieldMappingNote> =
        plan.targetManifestFieldMappingPlan.panelTimelineFields +
            plan.targetManifestFieldMappingPlan.voiceTimelineFields +
            plan.targetManifestFieldMappingPlan.subtitleTimelineFields +
            plan.targetManifestFieldMappingPlan.renderSettingsFields +
            plan.targetManifestFieldMappingPlan.perPanelMappingNotes.flatMap {
                it.plannedPanelTimelineMapping + it.plannedVoiceTimelineMapping + it.plannedSubtitleTimelineMapping
            }

    private fun firstUnsafeInvocation(vararg values: String): String =
        values.firstOrNull { it != "none" } ?: "none"

    private fun commonMustNotDo(): List<String> =
        listOf(
            "Do not execute adapter implementation in Phase 10.",
            "Do not generate ComicVideoRenderHandoffManifest in Phase 10.",
            "Do not run renderer/ffmpeg/TTS.",
            "Do not generate media or claim final media approval.",
            "Do not call provider/API or create provider/media handoff.",
            "Do not claim canon, registry, release, approval, or production lock.",
        )

    private fun check(
        checkId: String,
        category: String,
        passed: Boolean,
        message: String,
        evidence: List<String>,
        findings: MutableList<LocalRendererAdapterImplementationDraftFinding>,
        ruleId: String,
        path: String,
        suggestedFix: String,
    ): LocalRendererAdapterImplementationDraftCheck {
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
        return LocalRendererAdapterImplementationDraftCheck(
            checkId = checkId,
            status = if (passed) "PASS" else "FAIL",
            category = category,
            message = message,
            evidence = evidence,
        )
    }

    private fun List<LocalRendererAdapterImplementationDraftFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): LocalRendererAdapterImplementationDraftFinding =
        LocalRendererAdapterImplementationDraftFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
}
