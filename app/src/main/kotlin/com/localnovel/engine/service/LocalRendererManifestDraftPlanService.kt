package com.localnovel.engine.service

import com.localnovel.engine.model.LocalRendererAdapterDraftFieldMapping
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraft
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftBoundaries
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftReviewReport
import com.localnovel.engine.model.LocalRendererManifestDraftPlan
import com.localnovel.engine.model.LocalRendererManifestDraftPlanBoundarySummary
import com.localnovel.engine.model.LocalRendererManifestDraftPlanCheck
import com.localnovel.engine.model.LocalRendererManifestDraftPlanFinding
import com.localnovel.engine.model.LocalRendererManifestDraftPlanSourceAlignmentSummary
import com.localnovel.engine.model.LocalRendererManifestDraftPlanSummary
import com.localnovel.engine.model.LocalRendererManifestDraftPlanVerificationStep
import com.localnovel.engine.model.LocalRendererManifestDraftWorkItem
import com.localnovel.engine.model.LocalRendererManifestFieldMappingPlan
import com.localnovel.engine.model.LocalRendererManifestFieldPlan
import com.localnovel.engine.model.LocalRendererManifestPanelPlan
import com.localnovel.engine.model.LocalRendererManifestRenderSettingsPlan
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class LocalRendererManifestDraftPlanService {
    private val requiredDraftStatus = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT"
    private val requiredDraftBoundary = "local_renderer_adapter_implementation_draft_only_not_manifest_generation"
    private val requiredDraftNextStep = "local_renderer_adapter_implementation_draft_review"
    private val requiredReviewStatus = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_PASS"
    private val requiredReviewBoundary = "local_renderer_adapter_implementation_draft_review_only_not_manifest_generation"
    private val requiredReviewNextStep = "local_renderer_manifest_draft_plan"
    private val passStatus = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN"
    private val failStatus = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_FAIL"
    private val downstreamBoundary = "local_renderer_manifest_draft_plan_only_not_manifest_generation"
    private val nextAllowedDownstreamStep = "local_renderer_manifest_draft_plan_review"
    private val requiredBoundaryFlags = listOf(
        "mediaGenerated=false",
        "rendererInvocation=none",
        "providerInvocation=none",
        "ttsInvocation=none",
        "manifestGenerated=false",
        "adapterImplementationExecuted=false",
        "noExternalPaidProviderTools=true",
        "rendererReady=false",
        "mp4ReadinessClaimed=false",
    )

    fun planFile(
        implementationDraftPath: Path,
        draftReviewPath: Path,
        outPath: Path? = null,
        planId: String = "",
    ): LocalRendererManifestDraftPlan {
        val draft = JsonSupport.json.decodeFromString<LocalRendererAdapterImplementationDraft>(
            Files.readString(implementationDraftPath).removePrefix("\uFEFF"),
        )
        val review = JsonSupport.json.decodeFromString<LocalRendererAdapterImplementationDraftReviewReport>(
            Files.readString(draftReviewPath).removePrefix("\uFEFF"),
        )
        val plan = plan(
            draft = draft,
            review = review,
            implementationDraftFile = implementationDraftPath.toString(),
            draftReviewFile = draftReviewPath.toString(),
            planId = planId,
        )
        if (outPath != null) {
            outPath.parent?.let { Files.createDirectories(it) }
            Files.writeString(outPath, JsonSupport.json.encodeToString(plan))
        }
        return plan
    }

    fun plan(
        draft: LocalRendererAdapterImplementationDraft,
        review: LocalRendererAdapterImplementationDraftReviewReport,
        implementationDraftFile: String = "",
        draftReviewFile: String = "",
        planId: String = "",
    ): LocalRendererManifestDraftPlan {
        val findings = mutableListOf<LocalRendererManifestDraftPlanFinding>()
        val alignment = sourceAlignmentSummary(draft, review)
        val summary = manifestDraftPlanSummary(draft, review)
        val boundarySummary = boundarySummary(draft, review)
        val checks = buildChecks(
            draft = draft,
            review = review,
            implementationDraftFile = implementationDraftFile,
            draftReviewFile = draftReviewFile,
            alignment = alignment,
            summary = summary,
            boundarySummary = boundarySummary,
            findings = findings,
        )
        val verdict = if (findings.hasBlocking()) "FAIL" else "PASS"
        val resolvedPlanId = planId.ifBlank {
            review.reviewId.takeIf { it.isNotBlank() }?.let { "$it-manifest-draft-plan-v0" }
                ?: draft.draftId.takeIf { it.isNotBlank() }?.let { "$it-manifest-draft-plan-v0" }
                ?: ""
        }

        return LocalRendererManifestDraftPlan(
            verdict = verdict,
            status = if (verdict == "PASS") passStatus else failStatus,
            planId = resolvedPlanId,
            sourceImplementationDraftFile = implementationDraftFile,
            sourceDraftReviewFile = draftReviewFile,
            sourceDraftId = draft.draftId,
            sourceDraftReviewId = review.reviewId,
            sourcePlanId = draft.sourcePlanId,
            sourcePlanReviewId = draft.sourcePlanReviewId,
            sourceDryRunId = draft.sourceDryRunId,
            sourceHandoffId = draft.sourceHandoffId,
            sourceHandoffReviewId = draft.sourceHandoffReviewId,
            sourceStoryInputPackId = draft.sourceStoryInputPackId,
            checks = checks,
            manifestDraftPlanSummary = summary,
            sourceAlignmentSummary = alignment,
            manifestFieldPlan = if (verdict == "PASS") manifestFieldPlan(draft) else LocalRendererManifestFieldPlan(),
            requiredManifestDraftWorkItems = if (verdict == "PASS") manifestDraftWorkItems(draft) else emptyList(),
            verificationPlan = if (verdict == "PASS") verificationPlan(draft) else emptyList(),
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
        draft: LocalRendererAdapterImplementationDraft,
        review: LocalRendererAdapterImplementationDraftReviewReport,
        implementationDraftFile: String,
        draftReviewFile: String,
        alignment: LocalRendererManifestDraftPlanSourceAlignmentSummary,
        summary: LocalRendererManifestDraftPlanSummary,
        boundarySummary: LocalRendererManifestDraftPlanBoundarySummary,
        findings: MutableList<LocalRendererManifestDraftPlanFinding>,
    ): List<LocalRendererManifestDraftPlanCheck> =
        listOf(
            check(
                checkId = "explicit-source-files",
                category = "source_validation",
                passed = implementationDraftFile.isNotBlank() &&
                    draftReviewFile.isNotBlank() &&
                    draft.draftId.isNotBlank() &&
                    review.reviewId.isNotBlank() &&
                    draft.sourceStoryInputPackId.isNotBlank() &&
                    draft.sourceHandoffId.isNotBlank(),
                message = "Manifest draft plan requires explicit Phase 10 implementation draft and Phase 11 review files.",
                evidence = listOf(
                    "implementationDraftFile=$implementationDraftFile",
                    "draftReviewFile=$draftReviewFile",
                    "draftId=${draft.draftId}",
                    "reviewId=${review.reviewId}",
                    "sourceStoryInputPackId=${draft.sourceStoryInputPackId}",
                    "sourceHandoffId=${draft.sourceHandoffId}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_SOURCE_FILE_MISSING",
                path = "$.inputs",
                suggestedFix = "Pass --implementation-draft, --draft-review, and --out explicitly.",
            ),
            check(
                checkId = "implementation-draft-state",
                category = "source_validation",
                passed = draft.verdict == "PASS" &&
                    draft.status == requiredDraftStatus &&
                    draft.downstreamBoundary == requiredDraftBoundary &&
                    draft.nextAllowedDownstreamStep == requiredDraftNextStep,
                message = "Manifest draft plan requires PASS Phase 10 implementation draft state.",
                evidence = listOf(
                    "verdict=${draft.verdict}",
                    "status=${draft.status}",
                    "downstreamBoundary=${draft.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${draft.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_DRAFT_STATE_INVALID",
                path = "$.implementationDraft",
                suggestedFix = "Use the accepted PASS Phase 10 implementation draft.",
            ),
            check(
                checkId = "draft-review-state",
                category = "source_validation",
                passed = review.verdict == "PASS" &&
                    review.status == requiredReviewStatus &&
                    review.downstreamBoundary == requiredReviewBoundary &&
                    review.nextAllowedDownstreamStep == requiredReviewNextStep,
                message = "Manifest draft plan requires PASS Phase 11 implementation draft review state.",
                evidence = listOf(
                    "verdict=${review.verdict}",
                    "status=${review.status}",
                    "downstreamBoundary=${review.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${review.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_STATE_INVALID",
                path = "$.draftReview",
                suggestedFix = "Use the accepted PASS Phase 11 implementation draft review report.",
            ),
            check(
                checkId = "source-ids-align",
                category = "source_linkage",
                passed = alignment.draftIdAligned &&
                    alignment.planIdAligned &&
                    alignment.planReviewIdAligned &&
                    alignment.dryRunIdAligned &&
                    alignment.handoffIdAligned &&
                    alignment.handoffReviewIdAligned &&
                    alignment.storyInputPackIdAligned,
                message = "Manifest draft plan requires Phase 10 and Phase 11 source ids to align.",
                evidence = listOf(
                    "draftIdAligned=${alignment.draftIdAligned}",
                    "planIdAligned=${alignment.planIdAligned}",
                    "planReviewIdAligned=${alignment.planReviewIdAligned}",
                    "dryRunIdAligned=${alignment.dryRunIdAligned}",
                    "handoffIdAligned=${alignment.handoffIdAligned}",
                    "handoffReviewIdAligned=${alignment.handoffReviewIdAligned}",
                    "storyInputPackIdAligned=${alignment.storyInputPackIdAligned}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_SOURCE_ID_MISMATCH",
                path = "$.sourceAlignmentSummary",
                suggestedFix = "Use Phase 10 and Phase 11 JSON files produced from the same story-video chain.",
            ),
            check(
                checkId = "local-boundaries",
                category = "boundary",
                passed = boundarySummary.draftLocalOnly &&
                    boundarySummary.reviewLocalOnly &&
                    !boundarySummary.mediaGenerated &&
                    boundarySummary.rendererInvocation == "none" &&
                    boundarySummary.providerInvocation == "none" &&
                    boundarySummary.ttsInvocation == "none" &&
                    !boundarySummary.manifestGenerated &&
                    !boundarySummary.adapterImplementationExecuted &&
                    !boundarySummary.finalMediaApproval &&
                    !boundarySummary.providerMediaHandoff &&
                    !boundarySummary.canonRegistryOrProductionLock &&
                    !boundarySummary.rendererReady &&
                    !boundarySummary.mp4ReadinessClaimed,
                message = "Manifest draft plan requires local-only no-media boundaries across Phase 10 and Phase 11 inputs.",
                evidence = listOf(
                    "draftLocalOnly=${boundarySummary.draftLocalOnly}",
                    "reviewLocalOnly=${boundarySummary.reviewLocalOnly}",
                    "mediaGenerated=${boundarySummary.mediaGenerated}",
                    "rendererInvocation=${boundarySummary.rendererInvocation}",
                    "providerInvocation=${boundarySummary.providerInvocation}",
                    "ttsInvocation=${boundarySummary.ttsInvocation}",
                    "manifestGenerated=${boundarySummary.manifestGenerated}",
                    "adapterImplementationExecuted=${boundarySummary.adapterImplementationExecuted}",
                    "rendererReady=${boundarySummary.rendererReady}",
                    "mp4ReadinessClaimed=${boundarySummary.mp4ReadinessClaimed}",
                ) + requiredBoundaryFlags,
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_BOUNDARY_FAIL",
                path = "$.boundaries",
                suggestedFix = "Use local-only JSON with no provider/API, renderer, TTS, media, adapter execution, manifest generation, readiness claim, release, or production lock.",
            ),
            check(
                checkId = "no-findings-in-sources",
                category = "review_gate",
                passed = draft.findingCount == 0 &&
                    draft.findings.isEmpty() &&
                    review.findingCount == 0 &&
                    review.findings.isEmpty(),
                message = "Manifest draft plan requires no findings in Phase 10 draft or Phase 11 review.",
                evidence = listOf(
                    "draftFindingCount=${draft.findingCount}",
                    "reviewFindingCount=${review.findingCount}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_SOURCE_FINDINGS_PRESENT",
                path = "$.sourceFindings",
                suggestedFix = "Resolve Phase 10/11 findings before planning a manifest draft.",
            ),
            check(
                checkId = "review-checks-pass",
                category = "review_gate",
                passed = review.checks.isNotEmpty() &&
                    review.checks.filter { it.requiredForReview }.all { it.status == "PASS" },
                message = "Manifest draft plan requires non-empty Phase 11 checks with all required checks PASS.",
                evidence = listOf(
                    "reviewChecks=${review.checks.size}",
                    "reviewRequiredFail=${review.checks.count { it.requiredForReview && it.status != "PASS" }}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_CHECKS_NOT_PASS",
                path = "$.draftReview.checks",
                suggestedFix = "Resolve Phase 11 review checks before planning a manifest draft.",
            ),
            check(
                checkId = "required-content-present",
                category = "plan_completeness",
                passed = draft.implementationTasks.isNotEmpty() &&
                    draft.adapterMappingDraft.panelDrafts.isNotEmpty() &&
                    summary.mappingDraftFields > 0 &&
                    draft.verificationPlan.isNotEmpty() &&
                    summary.targetDurationMillis > 0,
                message = "Manifest draft plan requires implementation tasks, mapping draft, verification plan, and target duration.",
                evidence = listOf(
                    "implementationTasks=${draft.implementationTasks.size}",
                    "mappingDraftPanels=${draft.adapterMappingDraft.panelDrafts.size}",
                    "mappingDraftFields=${summary.mappingDraftFields}",
                    "verificationSteps=${draft.verificationPlan.size}",
                    "targetDurationMillis=${summary.targetDurationMillis}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_CONTENT_MISSING",
                path = "$.manifestDraftPlanSummary",
                suggestedFix = "Use a complete Phase 10 implementation draft with mapping draft, work items, and verification plan.",
            ),
            check(
                checkId = "manifest-remains-plan-only",
                category = "manifest_boundary",
                passed = draft.adapterMappingDraft.draftOnly &&
                    !draft.adapterMappingDraft.manifestGenerated &&
                    draft.adapterMappingDraft.panelDrafts.all { !it.manifestGenerated && !it.rendererReady } &&
                    targetFieldMappings(draft).all { !it.generatedNow } &&
                    !draft.adapterMappingDraft.renderSettingsDraft.generatedNow,
                message = "Manifest draft plan may mention ComicVideoRenderHandoffManifest only as planning notes; no real manifest may be generated.",
                evidence = listOf(
                    "draftOnly=${draft.adapterMappingDraft.draftOnly}",
                    "mappingManifestGenerated=${draft.adapterMappingDraft.manifestGenerated}",
                    "panelManifestGenerated=${draft.adapterMappingDraft.panelDrafts.count { it.manifestGenerated }}",
                    "panelRendererReady=${draft.adapterMappingDraft.panelDrafts.count { it.rendererReady }}",
                    "generatedNowFields=${targetFieldMappings(draft).count { it.generatedNow }}",
                    "renderSettingsGeneratedNow=${draft.adapterMappingDraft.renderSettingsDraft.generatedNow}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_MANIFEST_GENERATED_FORBIDDEN",
                path = "$.manifestFieldPlan",
                suggestedFix = "Keep Phase 12 as planning only; do not generate ComicVideoRenderHandoffManifest.",
            ),
            check(
                checkId = "no-readiness-overclaim",
                category = "renderer_compatibility",
                passed = !draft.boundaries.rendererReady &&
                    !draft.boundaries.mp4ReadinessClaimed &&
                    !review.boundaries.rendererReady &&
                    !review.boundaries.mp4ReadinessClaimed &&
                    !review.rendererCompatibilitySummary.ready &&
                    !review.rendererCompatibilitySummary.mp4ReadinessClaimed,
                message = "Manifest draft plan must preserve rendererReady=false and mp4ReadinessClaimed=false.",
                evidence = listOf(
                    "draft.rendererReady=${draft.boundaries.rendererReady}",
                    "draft.mp4ReadinessClaimed=${draft.boundaries.mp4ReadinessClaimed}",
                    "review.rendererReady=${review.boundaries.rendererReady}",
                    "review.mp4ReadinessClaimed=${review.boundaries.mp4ReadinessClaimed}",
                    "reviewCompatibility.ready=${review.rendererCompatibilitySummary.ready}",
                    "reviewCompatibility.mp4ReadinessClaimed=${review.rendererCompatibilitySummary.mp4ReadinessClaimed}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_READY_OVERCLAIM",
                path = "$.rendererCompatibility",
                suggestedFix = "Keep rendererReady=false and mp4ReadinessClaimed=false until future explicit renderer/MP4 verification.",
            ),
        )

    private fun sourceAlignmentSummary(
        draft: LocalRendererAdapterImplementationDraft,
        review: LocalRendererAdapterImplementationDraftReviewReport,
    ): LocalRendererManifestDraftPlanSourceAlignmentSummary =
        LocalRendererManifestDraftPlanSourceAlignmentSummary(
            draftIdAligned = draft.draftId.isNotBlank() && draft.draftId == review.sourceDraftId,
            planIdAligned = draft.sourcePlanId.isNotBlank() && draft.sourcePlanId == review.sourcePlanId,
            planReviewIdAligned = draft.sourcePlanReviewId.isNotBlank() &&
                draft.sourcePlanReviewId == review.sourcePlanReviewId,
            dryRunIdAligned = draft.sourceDryRunId.isNotBlank() && draft.sourceDryRunId == review.sourceDryRunId,
            handoffIdAligned = draft.sourceHandoffId.isNotBlank() &&
                draft.sourceHandoffId == review.sourceHandoffId,
            handoffReviewIdAligned = draft.sourceHandoffReviewId.isNotBlank() &&
                draft.sourceHandoffReviewId == review.sourceHandoffReviewId,
            storyInputPackIdAligned = draft.sourceStoryInputPackId.isNotBlank() &&
                draft.sourceStoryInputPackId == review.sourceStoryInputPackId,
            draftId = draft.draftId,
            reviewSourceDraftId = review.sourceDraftId,
            draftSourcePlanId = draft.sourcePlanId,
            reviewSourcePlanId = review.sourcePlanId,
            draftSourcePlanReviewId = draft.sourcePlanReviewId,
            reviewSourcePlanReviewId = review.sourcePlanReviewId,
            draftSourceDryRunId = draft.sourceDryRunId,
            reviewSourceDryRunId = review.sourceDryRunId,
            draftSourceHandoffId = draft.sourceHandoffId,
            reviewSourceHandoffId = review.sourceHandoffId,
            draftSourceHandoffReviewId = draft.sourceHandoffReviewId,
            reviewSourceHandoffReviewId = review.sourceHandoffReviewId,
            draftSourceStoryInputPackId = draft.sourceStoryInputPackId,
            reviewSourceStoryInputPackId = review.sourceStoryInputPackId,
        )

    private fun manifestDraftPlanSummary(
        draft: LocalRendererAdapterImplementationDraft,
        review: LocalRendererAdapterImplementationDraftReviewReport,
    ): LocalRendererManifestDraftPlanSummary =
        LocalRendererManifestDraftPlanSummary(
            sourceDraftChecks = draft.checks.size,
            sourceDraftReviewChecks = review.checks.size,
            sourceDraftReviewChecksPass = review.checks.count { it.status == "PASS" },
            sourceDraftReviewChecksFail = review.checks.count { it.status != "PASS" },
            implementationTasks = draft.implementationTasks.size,
            mappingDraftPanels = draft.adapterMappingDraft.panelDrafts.size,
            mappingDraftFields = targetFieldMappings(draft).size,
            requiredManifestDraftWorkItems = draft.implementationTasks.size,
            verificationSteps = draft.verificationPlan.size + 1,
            targetDurationMillis = draft.adapterMappingDraft.renderSettingsDraft.totalDurationMillis,
            findingCount = draft.findingCount + review.findingCount,
            planningOnly = true,
            manifestGenerated = draft.boundaries.manifestGenerated ||
                draft.implementationDraftSummary.manifestGenerated ||
                draft.adapterMappingDraft.manifestGenerated ||
                review.boundaries.manifestGenerated,
            adapterImplementationExecuted = draft.boundaries.adapterImplementationExecuted ||
                draft.implementationDraftSummary.adapterImplementationExecuted ||
                review.boundaries.adapterImplementationExecuted,
            rendererReady = draft.boundaries.rendererReady ||
                review.boundaries.rendererReady ||
                review.rendererCompatibilitySummary.ready ||
                draft.adapterMappingDraft.panelDrafts.any { it.rendererReady },
            mp4ReadinessClaimed = draft.boundaries.mp4ReadinessClaimed ||
                review.boundaries.mp4ReadinessClaimed ||
                review.rendererCompatibilitySummary.mp4ReadinessClaimed,
        )

    private fun boundarySummary(
        draft: LocalRendererAdapterImplementationDraft,
        review: LocalRendererAdapterImplementationDraftReviewReport,
    ): LocalRendererManifestDraftPlanBoundarySummary =
        LocalRendererManifestDraftPlanBoundarySummary(
            draftLocalOnly = localBoundariesOk(draft.boundaries),
            reviewLocalOnly = localBoundariesOk(review.boundaries),
            mediaGenerated = draft.boundaries.mediaGenerated || review.boundaries.mediaGenerated,
            rendererInvocation = firstUnsafeInvocation(
                draft.boundaries.rendererInvocation,
                review.boundaries.rendererInvocation,
            ),
            providerInvocation = firstUnsafeInvocation(
                draft.boundaries.providerInvocation,
                review.boundaries.providerInvocation,
            ),
            ttsInvocation = firstUnsafeInvocation(
                draft.boundaries.ttsInvocation,
                review.boundaries.ttsInvocation,
            ),
            manifestGenerated = draft.boundaries.manifestGenerated ||
                draft.adapterMappingDraft.manifestGenerated ||
                review.boundaries.manifestGenerated,
            adapterImplementationExecuted = draft.boundaries.adapterImplementationExecuted ||
                review.boundaries.adapterImplementationExecuted,
            finalMediaApproval = draft.boundaries.finalMediaApproval || review.boundaries.finalMediaApproval,
            providerMediaHandoff = draft.boundaries.providerMediaHandoff || review.boundaries.providerMediaHandoff,
            canonRegistryOrProductionLock = draft.boundaries.canonRegistryOrProductionLock ||
                review.boundaries.canonRegistryOrProductionLock,
            rendererReady = draft.boundaries.rendererReady ||
                review.boundaries.rendererReady ||
                review.rendererCompatibilitySummary.ready,
            mp4ReadinessClaimed = draft.boundaries.mp4ReadinessClaimed ||
                review.boundaries.mp4ReadinessClaimed ||
                review.rendererCompatibilitySummary.mp4ReadinessClaimed,
        )

    private fun manifestFieldPlan(
        draft: LocalRendererAdapterImplementationDraft,
    ): LocalRendererManifestFieldPlan =
        LocalRendererManifestFieldPlan(
            planningOnly = true,
            manifestGenerated = false,
            targetManifestType = draft.adapterMappingDraft.targetManifestType.ifBlank {
                "ComicVideoRenderHandoffManifest"
            },
            planningBoundary = downstreamBoundary,
            panelManifestPlans = draft.adapterMappingDraft.panelDrafts.map { panel ->
                LocalRendererManifestPanelPlan(
                    order = panel.order,
                    panelId = panel.panelId,
                    sourceSceneId = panel.sourceSceneId,
                    sourceBeatId = panel.sourceBeatId,
                    startMillis = panel.startMillis,
                    endMillis = panel.endMillis,
                    durationMillis = panel.durationMillis,
                    plannedPanelTimelineFields = panel.targetPanelTimelineDraft.map {
                        fieldPlan(it, panel.panelId, "targetPanelTimelineDraft")
                    },
                    plannedVoiceTimelineFields = panel.targetVoiceTimelineDraft.map {
                        fieldPlan(it, panel.panelId, "targetVoiceTimelineDraft")
                    },
                    plannedSubtitleTimelineFields = panel.targetSubtitleTimelineDraft.map {
                        fieldPlan(it, panel.panelId, "targetSubtitleTimelineDraft")
                    },
                    manifestGenerated = false,
                    rendererReady = false,
                    note = "Manifest draft field plan only; no ComicVideoRenderHandoffManifest is generated.",
                )
            },
            renderSettingsPlan = LocalRendererManifestRenderSettingsPlan(
                totalDurationMillis = draft.adapterMappingDraft.renderSettingsDraft.totalDurationMillis,
                timelineStatusHint = draft.adapterMappingDraft.renderSettingsDraft.timelineStatusHint,
                frameRateHint = draft.adapterMappingDraft.renderSettingsDraft.frameRateHint,
                resolutionHint = draft.adapterMappingDraft.renderSettingsDraft.resolutionHint,
                rendererBoundaryHint = "local_renderer_manifest_draft_plan_only_not_renderer_invocation",
                generatedNow = false,
            ),
            note = "Planning only. A later explicit review step is required before any real manifest draft.",
        )

    private fun fieldPlan(
        mapping: LocalRendererAdapterDraftFieldMapping,
        panelId: String,
        sourceList: String,
    ): LocalRendererManifestFieldMappingPlan =
        LocalRendererManifestFieldMappingPlan(
            targetSection = mapping.targetSection,
            targetField = mapping.targetField,
            sourceDraftPath = "$.adapterMappingDraft.panelDrafts[panelId=$panelId].$sourceList[targetField=${mapping.targetField}]",
            plannedValueHint = mapping.draftValueHint,
            implementationNote = mapping.implementationNote,
            required = mapping.required,
            generatedNow = false,
        )

    private fun manifestDraftWorkItems(
        draft: LocalRendererAdapterImplementationDraft,
    ): List<LocalRendererManifestDraftWorkItem> =
        draft.implementationTasks.mapIndexed { index, task ->
            LocalRendererManifestDraftWorkItem(
                workItemId = "manifest-draft-work-item-${index + 1}",
                sourceImplementationTaskId = task.taskId,
                title = "Plan manifest draft: ${task.title}",
                description = task.description,
                targetOutcome = task.targetOutcome,
                mustNotDo = commonMustNotDo(),
                reviewGateRequiredBeforeDraft = nextAllowedDownstreamStep,
            )
        }

    private fun verificationPlan(
        draft: LocalRendererAdapterImplementationDraft,
    ): List<LocalRendererManifestDraftPlanVerificationStep> =
        draft.verificationPlan.mapIndexed { index, verification ->
            LocalRendererManifestDraftPlanVerificationStep(
                verificationId = "manifest-draft-plan-verification-${index + 1}",
                sourceVerificationId = verification.verificationId,
                commandOrCheck = verification.commandOrCheck,
                expectedResult = verification.expectedResult,
                requiresMediaGeneration = false,
                requiresManifestGeneration = false,
            )
        } + LocalRendererManifestDraftPlanVerificationStep(
            verificationId = "future-manifest-draft-plan-review-gate",
            sourceVerificationId = "phase12-boundary",
            commandOrCheck = "Future explicit task must review this manifest draft plan before any real manifest draft.",
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

    private fun localBoundariesOk(boundaries: LocalRendererAdapterImplementationDraftBoundaries): Boolean =
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
            !boundaries.canonRegistryOrProductionLock &&
            !boundaries.rendererReady &&
            !boundaries.mp4ReadinessClaimed

    private fun targetFieldMappings(draft: LocalRendererAdapterImplementationDraft): List<LocalRendererAdapterDraftFieldMapping> =
        draft.adapterMappingDraft.panelDrafts.flatMap {
            it.targetPanelTimelineDraft + it.targetVoiceTimelineDraft + it.targetSubtitleTimelineDraft
        }

    private fun firstUnsafeInvocation(vararg values: String): String =
        values.firstOrNull { it != "none" } ?: "none"

    private fun commonMustNotDo(): List<String> =
        listOf(
            "Do not generate ComicVideoRenderHandoffManifest in Phase 12.",
            "Do not execute adapter implementation.",
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
        findings: MutableList<LocalRendererManifestDraftPlanFinding>,
        ruleId: String,
        path: String,
        suggestedFix: String,
    ): LocalRendererManifestDraftPlanCheck {
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
        return LocalRendererManifestDraftPlanCheck(
            checkId = checkId,
            status = if (passed) "PASS" else "FAIL",
            category = category,
            message = message,
            evidence = evidence,
        )
    }

    private fun List<LocalRendererManifestDraftPlanFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): LocalRendererManifestDraftPlanFinding =
        LocalRendererManifestDraftPlanFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
}
