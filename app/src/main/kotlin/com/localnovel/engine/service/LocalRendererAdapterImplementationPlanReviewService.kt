package com.localnovel.engine.service

import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanBoundaries
import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanDraft
import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanReviewBoundarySummary
import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanReviewCheck
import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanReviewFinding
import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanReviewRendererCompatibilitySummary
import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanReviewReport
import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanReviewSummary
import com.localnovel.engine.model.LocalRendererAdapterTargetFieldMappingNote

class LocalRendererAdapterImplementationPlanReviewService {
    private val requiredPlanStatus = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_DRAFT"
    private val requiredPlanBoundary = "local_renderer_adapter_implementation_plan_only_not_implementation"
    private val requiredPlanNextStep = "local_renderer_adapter_implementation_review"
    private val passStatus = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_REVIEW_PASS"
    private val failStatus = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_REVIEW_FAIL"
    private val reviewBoundary = "local_renderer_adapter_implementation_plan_review_only_not_implementation"
    private val nextAllowedDownstreamStep = "local_renderer_adapter_implementation_draft"

    fun review(
        plan: LocalRendererAdapterImplementationPlanDraft,
        planFile: String = "",
        reviewId: String = "",
    ): LocalRendererAdapterImplementationPlanReviewReport {
        val findings = mutableListOf<LocalRendererAdapterImplementationPlanReviewFinding>()
        val planSummary = planSummary(plan)
        val boundarySummary = boundarySummary(plan.boundaries)
        val checks = buildChecks(plan, planFile, planSummary, findings)
        val verdict = if (findings.hasBlocking()) "FAIL" else "PASS"
        val resolvedReviewId = reviewId.ifBlank {
            plan.planId.takeIf { it.isNotBlank() }?.let { "$it-review-v0" }.orEmpty()
        }

        return LocalRendererAdapterImplementationPlanReviewReport(
            verdict = verdict,
            status = if (verdict == "PASS") passStatus else failStatus,
            reviewId = resolvedReviewId,
            sourcePlanFile = planFile,
            sourcePlanId = plan.planId,
            sourceDryRunId = plan.sourceDryRunId,
            sourceHandoffId = plan.sourceHandoffId,
            sourceReviewId = plan.sourceReviewId,
            sourceStoryInputPackId = plan.sourceStoryInputPackId,
            checks = checks,
            planSummary = planSummary,
            boundarySummary = boundarySummary,
            rendererCompatibilitySummary = rendererCompatibilitySummary(),
            boundaries = outputBoundaries(),
            downstreamBoundary = reviewBoundary,
            nextAllowedDownstreamStep = if (verdict == "PASS") nextAllowedDownstreamStep else "",
            blockedReason = if (verdict == "PASS") "" else findings
                .filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }
                .joinToString("; ") { it.ruleId },
            findingCount = findings.size,
            findings = findings,
        )
    }

    private fun buildChecks(
        plan: LocalRendererAdapterImplementationPlanDraft,
        planFile: String,
        planSummary: LocalRendererAdapterImplementationPlanReviewSummary,
        findings: MutableList<LocalRendererAdapterImplementationPlanReviewFinding>,
    ): List<LocalRendererAdapterImplementationPlanReviewCheck> =
        listOf(
            check(
                checkId = "source-plan-present",
                category = "source_validation",
                passed = planFile.isNotBlank() &&
                    plan.planId.isNotBlank() &&
                    plan.sourceDryRunId.isNotBlank() &&
                    plan.sourceHandoffId.isNotBlank() &&
                    plan.sourceReviewId.isNotBlank() &&
                    plan.sourceStoryInputPackId.isNotBlank(),
                message = "Implementation plan review requires an explicit plan file and non-empty source ids.",
                evidence = listOf(
                    "planFile=$planFile",
                    "planId=${plan.planId}",
                    "sourceDryRunId=${plan.sourceDryRunId}",
                    "sourceHandoffId=${plan.sourceHandoffId}",
                    "sourceReviewId=${plan.sourceReviewId}",
                    "sourceStoryInputPackId=${plan.sourceStoryInputPackId}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_REVIEW_SOURCE_MISSING",
                path = "$.sourcePlan",
                suggestedFix = "Use a complete PASS Phase 8 implementation plan JSON and pass it with --plan.",
            ),
            check(
                checkId = "plan-state",
                category = "source_validation",
                passed = plan.verdict == "PASS" &&
                    plan.status == requiredPlanStatus &&
                    plan.downstreamBoundary == requiredPlanBoundary &&
                    plan.nextAllowedDownstreamStep == requiredPlanNextStep,
                message = "Implementation plan review requires PASS Phase 8 plan state and review downstream step.",
                evidence = listOf(
                    "verdict=${plan.verdict}",
                    "status=${plan.status}",
                    "downstreamBoundary=${plan.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${plan.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_REVIEW_STATE_INVALID",
                path = "$.planState",
                suggestedFix = "Use the accepted PASS Phase 8 implementation plan.",
            ),
            check(
                checkId = "local-boundaries",
                category = "boundary",
                passed = localBoundariesOk(plan.boundaries),
                message = "Implementation plan review requires local-only no-media boundaries with no adapter execution or manifest generation.",
                evidence = listOf(
                    "mediaGenerated=${plan.boundaries.mediaGenerated}",
                    "rendererInvocation=${plan.boundaries.rendererInvocation}",
                    "providerInvocation=${plan.boundaries.providerInvocation}",
                    "ttsInvocation=${plan.boundaries.ttsInvocation}",
                    "manifestGenerated=${plan.boundaries.manifestGenerated}",
                    "adapterImplementationExecuted=${plan.boundaries.adapterImplementationExecuted}",
                    "noExternalPaidProviderTools=${plan.boundaries.noExternalPaidProviderTools}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_REVIEW_BOUNDARY_FAIL",
                path = "$.boundaries",
                suggestedFix = "Use a Phase 8 plan with no provider/API, renderer, TTS, media, adapter execution, manifest generation, release, or production lock.",
            ),
            check(
                checkId = "target-mapping-planning-only",
                category = "manifest_boundary",
                passed = plan.targetManifestFieldMappingPlan.planningOnly &&
                    !plan.targetManifestFieldMappingPlan.manifestGenerated &&
                    planSummary.targetMappingNoteCount > 0 &&
                    planSummary.perPanelMappingNotes > 0 &&
                    targetFieldNotes(plan).all { !it.generatedNow } &&
                    plan.targetManifestFieldMappingPlan.perPanelMappingNotes.all { !it.manifestGenerated },
                message = "Implementation plan review requires target manifest mapping notes to remain planning-only with no generated manifest.",
                evidence = listOf(
                    "planningOnly=${plan.targetManifestFieldMappingPlan.planningOnly}",
                    "targetManifestGenerated=${plan.targetManifestFieldMappingPlan.manifestGenerated}",
                    "targetMappingNoteCount=${planSummary.targetMappingNoteCount}",
                    "perPanelMappingNotes=${planSummary.perPanelMappingNotes}",
                    "generatedNowNotes=${targetFieldNotes(plan).count { it.generatedNow }}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_REVIEW_MAPPING_NOT_PLANNING_ONLY",
                path = "$.targetManifestFieldMappingPlan",
                suggestedFix = "Use a Phase 8 plan whose target mapping section is planning-only and non-empty.",
            ),
            check(
                checkId = "source-validation-checks-pass",
                category = "review_gate",
                passed = plan.sourceValidationChecks.isNotEmpty() &&
                    plan.sourceValidationChecks.filter { it.requiredForPlan }.all { it.status == "PASS" } &&
                    plan.findingCount == 0 &&
                    plan.findings.isEmpty(),
                message = "Implementation plan review requires all required Phase 8 source validation checks to pass with no findings.",
                evidence = listOf(
                    "sourceValidationChecks=${plan.sourceValidationChecks.size}",
                    "requiredFail=${plan.sourceValidationChecks.count { it.requiredForPlan && it.status != "PASS" }}",
                    "findingCount=${plan.findingCount}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_REVIEW_SOURCE_CHECKS_NOT_PASS",
                path = "$.sourceValidationChecks",
                suggestedFix = "Resolve Phase 8 plan findings before review.",
            ),
            check(
                checkId = "work-items-present",
                category = "plan_completeness",
                passed = plan.requiredAdapterWorkItems.isNotEmpty(),
                message = "Implementation plan review requires non-empty requiredAdapterWorkItems.",
                evidence = listOf("requiredAdapterWorkItems=${plan.requiredAdapterWorkItems.size}"),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_REVIEW_WORK_ITEMS_MISSING",
                path = "$.requiredAdapterWorkItems",
                suggestedFix = "Regenerate Phase 8 plan with required adapter work items.",
            ),
            check(
                checkId = "planning-sections-present",
                category = "plan_completeness",
                passed = plan.implementationSteps.isNotEmpty() &&
                    plan.riskChecklist.isNotEmpty() &&
                    plan.verificationPlan.isNotEmpty(),
                message = "Implementation plan review requires implementation steps, risk checklist, and verification plan.",
                evidence = listOf(
                    "implementationSteps=${plan.implementationSteps.size}",
                    "riskChecklist=${plan.riskChecklist.size}",
                    "verificationPlan=${plan.verificationPlan.size}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_REVIEW_SECTIONS_MISSING",
                path = "$.implementationPlanSections",
                suggestedFix = "Regenerate Phase 8 plan with implementationSteps, riskChecklist, and verificationPlan.",
            ),
            check(
                checkId = "no-readiness-overclaim",
                category = "renderer_compatibility",
                passed = plan.boundaries.mediaGenerated == false &&
                    plan.boundaries.rendererInvocation == "none" &&
                    plan.boundaries.manifestGenerated == false &&
                    plan.boundaries.adapterImplementationExecuted == false,
                message = "Implementation plan review must preserve no renderer readiness, no MP4 readiness, and no implementation execution claim.",
                evidence = listOf(
                    "mediaGenerated=${plan.boundaries.mediaGenerated}",
                    "rendererInvocation=${plan.boundaries.rendererInvocation}",
                    "manifestGenerated=${plan.boundaries.manifestGenerated}",
                    "adapterImplementationExecuted=${plan.boundaries.adapterImplementationExecuted}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_REVIEW_READY_OVERCLAIM",
                path = "$.rendererCompatibility",
                suggestedFix = "Keep Phase 8 plan review-only until a future explicit adapter implementation draft task.",
            ),
        )

    private fun planSummary(plan: LocalRendererAdapterImplementationPlanDraft): LocalRendererAdapterImplementationPlanReviewSummary {
        val fieldNotes = targetFieldNotes(plan)
        return LocalRendererAdapterImplementationPlanReviewSummary(
            sourceValidationChecks = plan.sourceValidationChecks.size,
            sourceValidationPass = plan.sourceValidationChecks.count { it.status == "PASS" },
            sourceValidationFail = plan.sourceValidationChecks.count { it.status != "PASS" },
            requiredAdapterWorkItems = plan.requiredAdapterWorkItems.size,
            targetMappingNoteCount = fieldNotes.size,
            perPanelMappingNotes = plan.targetManifestFieldMappingPlan.perPanelMappingNotes.size,
            implementationSteps = plan.implementationSteps.size,
            riskChecks = plan.riskChecklist.size,
            verificationSteps = plan.verificationPlan.size,
            planningOnly = plan.targetManifestFieldMappingPlan.planningOnly,
            targetManifestGenerated = plan.targetManifestFieldMappingPlan.manifestGenerated,
            findingCount = plan.findingCount,
        )
    }

    private fun boundarySummary(
        boundaries: LocalRendererAdapterImplementationPlanBoundaries,
    ): LocalRendererAdapterImplementationPlanReviewBoundarySummary =
        LocalRendererAdapterImplementationPlanReviewBoundarySummary(
            fileProtocolJsonOnly = boundaries.fileProtocolJsonOnly,
            noExternalPaidProviderTools = boundaries.noExternalPaidProviderTools,
            providerInvocation = boundaries.providerInvocation,
            ttsInvocation = boundaries.ttsInvocation,
            mediaGenerated = boundaries.mediaGenerated,
            rendererInvocation = boundaries.rendererInvocation,
            directSqliteWrites = boundaries.directSqliteWrites,
            providerApiCalls = boundaries.providerApiCalls,
            adapterImplementationExecuted = boundaries.adapterImplementationExecuted,
            manifestGenerated = boundaries.manifestGenerated,
            finalMediaApproval = boundaries.finalMediaApproval,
            providerMediaHandoff = boundaries.providerMediaHandoff,
            release = boundaries.release,
            canonRegistryOrProductionLock = boundaries.canonRegistryOrProductionLock,
        )

    private fun outputBoundaries(): LocalRendererAdapterImplementationPlanBoundaries =
        LocalRendererAdapterImplementationPlanBoundaries(
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
        )

    private fun rendererCompatibilitySummary(): LocalRendererAdapterImplementationPlanReviewRendererCompatibilitySummary =
        LocalRendererAdapterImplementationPlanReviewRendererCompatibilitySummary(
            ready = false,
            mp4ReadinessClaimed = false,
            adapterImplementationStillRequired = true,
            manifestGenerationStillRequiresExplicitTask = true,
            requiredBeforeMp4 = listOf(
                "Pass Phase 9 implementation plan review.",
                "Assign and complete a separate local renderer adapter implementation draft task.",
                "Run a later adapter implementation review gate.",
                "Run a later local renderer dry-run/preflight before any media generation.",
            ),
            summary = "Review gate only: Phase 8 plan is valid for a future adapter implementation draft, but no renderer readiness or MP4 readiness is claimed.",
        )

    private fun localBoundariesOk(boundaries: LocalRendererAdapterImplementationPlanBoundaries): Boolean =
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

    private fun check(
        checkId: String,
        category: String,
        passed: Boolean,
        message: String,
        evidence: List<String>,
        findings: MutableList<LocalRendererAdapterImplementationPlanReviewFinding>,
        ruleId: String,
        path: String,
        suggestedFix: String,
    ): LocalRendererAdapterImplementationPlanReviewCheck {
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
        return LocalRendererAdapterImplementationPlanReviewCheck(
            checkId = checkId,
            status = if (passed) "PASS" else "FAIL",
            category = category,
            message = message,
            evidence = evidence,
        )
    }

    private fun List<LocalRendererAdapterImplementationPlanReviewFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): LocalRendererAdapterImplementationPlanReviewFinding =
        LocalRendererAdapterImplementationPlanReviewFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
}
