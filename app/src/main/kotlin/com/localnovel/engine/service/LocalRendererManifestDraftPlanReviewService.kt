package com.localnovel.engine.service

import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftBoundaries
import com.localnovel.engine.model.LocalRendererManifestDraftPlan
import com.localnovel.engine.model.LocalRendererManifestDraftPlanReviewBoundarySummary
import com.localnovel.engine.model.LocalRendererManifestDraftPlanReviewCheck
import com.localnovel.engine.model.LocalRendererManifestDraftPlanReviewFinding
import com.localnovel.engine.model.LocalRendererManifestDraftPlanReviewReport
import com.localnovel.engine.model.LocalRendererManifestDraftPlanReviewSourceAlignmentSummary
import com.localnovel.engine.model.LocalRendererManifestDraftPlanReviewSummary
import com.localnovel.engine.model.LocalRendererManifestDraftPlanReadinessSummary
import com.localnovel.engine.model.LocalRendererManifestFieldMappingPlan
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class LocalRendererManifestDraftPlanReviewService {
    private val proposedCommand = "review-local-renderer-manifest-draft-plan"
    private val requiredPlanStatus = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN"
    private val requiredPlanBoundary = "local_renderer_manifest_draft_plan_only_not_manifest_generation"
    private val requiredPlanNextStep = "local_renderer_manifest_draft_plan_review"
    private val passStatus = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_PASS"
    private val failStatus = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_FAIL"
    private val downstreamBoundary = "local_renderer_manifest_draft_plan_review_only_not_manifest_generation"
    private val nextAllowedDownstreamStep = "local_renderer_manifest_draft_json_draft"
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

    fun reviewFile(
        planPath: Path,
        outPath: Path? = null,
        reviewId: String = "",
    ): LocalRendererManifestDraftPlanReviewReport {
        val plan = JsonSupport.json.decodeFromString<LocalRendererManifestDraftPlan>(
            Files.readString(planPath).removePrefix("\uFEFF"),
        )
        val report = review(
            plan = plan,
            planFile = planPath.toString(),
            reviewId = reviewId,
        )
        if (outPath != null) {
            outPath.parent?.let { Files.createDirectories(it) }
            Files.writeString(outPath, JsonSupport.json.encodeToString(report))
        }
        return report
    }

    fun review(
        plan: LocalRendererManifestDraftPlan,
        planFile: String = "",
        reviewId: String = "",
    ): LocalRendererManifestDraftPlanReviewReport {
        val findings = mutableListOf<LocalRendererManifestDraftPlanReviewFinding>()
        val planReviewSummary = planReviewSummary(plan)
        val sourceAlignmentSummary = sourceAlignmentSummary(plan)
        val boundarySummary = boundarySummary(plan.boundaries)
        val checks = buildChecks(
            plan = plan,
            planFile = planFile,
            planReviewSummary = planReviewSummary,
            sourceAlignmentSummary = sourceAlignmentSummary,
            boundarySummary = boundarySummary,
            findings = findings,
        )
        val verdict = if (findings.hasBlocking()) "FAIL" else "PASS"
        val resolvedReviewId = reviewId.ifBlank {
            plan.planId.takeIf { it.isNotBlank() }?.let { "$it-review-v0" }.orEmpty()
        }

        return LocalRendererManifestDraftPlanReviewReport(
            verdict = verdict,
            status = if (verdict == "PASS") passStatus else failStatus,
            reviewId = resolvedReviewId,
            sourcePlanFile = planFile,
            sourceManifestDraftPlanId = plan.planId,
            sourceImplementationDraftId = plan.sourceDraftId,
            sourceImplementationDraftReviewId = plan.sourceDraftReviewId,
            sourcePlanId = plan.sourcePlanId,
            sourcePlanReviewId = plan.sourcePlanReviewId,
            sourceDryRunId = plan.sourceDryRunId,
            sourceHandoffId = plan.sourceHandoffId,
            sourceHandoffReviewId = plan.sourceHandoffReviewId,
            sourceStoryInputPackId = plan.sourceStoryInputPackId,
            checks = checks,
            planReviewSummary = planReviewSummary,
            sourceAlignmentSummary = sourceAlignmentSummary,
            manifestDraftPlanReadinessSummary = readinessSummary(verdict == "PASS"),
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
        plan: LocalRendererManifestDraftPlan,
        planFile: String,
        planReviewSummary: LocalRendererManifestDraftPlanReviewSummary,
        sourceAlignmentSummary: LocalRendererManifestDraftPlanReviewSourceAlignmentSummary,
        boundarySummary: LocalRendererManifestDraftPlanReviewBoundarySummary,
        findings: MutableList<LocalRendererManifestDraftPlanReviewFinding>,
    ): List<LocalRendererManifestDraftPlanReviewCheck> =
        listOf(
            check(
                checkId = "source-plan-present",
                category = "source_validation",
                passed = planFile.isNotBlank() &&
                    plan.planId.isNotBlank() &&
                    plan.sourceDraftId.isNotBlank() &&
                    plan.sourceDraftReviewId.isNotBlank() &&
                    plan.sourceStoryInputPackId.isNotBlank(),
                message = "Manifest draft plan review requires an explicit Phase 12 plan file and non-empty source ids.",
                evidence = listOf(
                    "proposedCommand=$proposedCommand",
                    "planFile=$planFile",
                    "planId=${plan.planId}",
                    "sourceDraftId=${plan.sourceDraftId}",
                    "sourceDraftReviewId=${plan.sourceDraftReviewId}",
                    "sourceStoryInputPackId=${plan.sourceStoryInputPackId}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_SOURCE_MISSING",
                path = "$.sourcePlan",
                suggestedFix = "Use a complete PASS Phase 12 manifest draft plan JSON and pass it explicitly.",
            ),
            check(
                checkId = "plan-state",
                category = "source_validation",
                passed = plan.verdict == "PASS" &&
                    plan.status == requiredPlanStatus &&
                    plan.downstreamBoundary == requiredPlanBoundary &&
                    plan.nextAllowedDownstreamStep == requiredPlanNextStep,
                message = "Manifest draft plan review requires PASS Phase 12 plan state and plan-review downstream step.",
                evidence = listOf(
                    "verdict=${plan.verdict}",
                    "status=${plan.status}",
                    "downstreamBoundary=${plan.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${plan.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_STATE_INVALID",
                path = "$.planState",
                suggestedFix = "Use the accepted PASS Phase 12 manifest draft plan.",
            ),
            check(
                checkId = "source-alignment",
                category = "source_linkage",
                passed = sourceAlignmentSummary.manifestDraftPlanIdPresent &&
                    sourceAlignmentSummary.sourceImplementationDraftIdPresent &&
                    sourceAlignmentSummary.sourceImplementationDraftReviewIdPresent &&
                    sourceAlignmentSummary.sourceStoryInputPackIdPresent &&
                    sourceAlignmentSummary.sourceAlignmentFromPlanPass,
                message = "Manifest draft plan review requires non-empty source ids and PASS source-alignment evidence from Phase 12.",
                evidence = listOf(
                    "manifestDraftPlanIdPresent=${sourceAlignmentSummary.manifestDraftPlanIdPresent}",
                    "sourceImplementationDraftIdPresent=${sourceAlignmentSummary.sourceImplementationDraftIdPresent}",
                    "sourceImplementationDraftReviewIdPresent=${sourceAlignmentSummary.sourceImplementationDraftReviewIdPresent}",
                    "sourceStoryInputPackIdPresent=${sourceAlignmentSummary.sourceStoryInputPackIdPresent}",
                    "sourceAlignmentFromPlanPass=${sourceAlignmentSummary.sourceAlignmentFromPlanPass}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_SOURCE_ALIGNMENT_FAIL",
                path = "$.sourceAlignmentSummary",
                suggestedFix = "Use a Phase 12 plan generated from aligned Phase 10/11 inputs.",
            ),
            check(
                checkId = "local-boundaries",
                category = "boundary",
                passed = localBoundariesOk(plan.boundaries),
                message = "Manifest draft plan review requires local-only no-media boundaries with no adapter execution or manifest generation.",
                evidence = listOf(
                    "mediaGenerated=${plan.boundaries.mediaGenerated}",
                    "rendererInvocation=${plan.boundaries.rendererInvocation}",
                    "providerInvocation=${plan.boundaries.providerInvocation}",
                    "ttsInvocation=${plan.boundaries.ttsInvocation}",
                    "manifestGenerated=${plan.boundaries.manifestGenerated}",
                    "adapterImplementationExecuted=${plan.boundaries.adapterImplementationExecuted}",
                    "noExternalPaidProviderTools=${plan.boundaries.noExternalPaidProviderTools}",
                    "rendererReady=${plan.boundaries.rendererReady}",
                    "mp4ReadinessClaimed=${plan.boundaries.mp4ReadinessClaimed}",
                    "boundarySummary.mediaGenerated=${boundarySummary.mediaGenerated}",
                    "boundarySummary.manifestGenerated=${boundarySummary.manifestGenerated}",
                    "boundarySummary.adapterImplementationExecuted=${boundarySummary.adapterImplementationExecuted}",
                ) + requiredBoundaryFlags,
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_BOUNDARY_FAIL",
                path = "$.boundaries",
                suggestedFix = "Use a Phase 12 plan with no provider/API, renderer, TTS, media, adapter execution, manifest generation, readiness claim, release, or production lock.",
            ),
            check(
                checkId = "no-finalization-boundaries",
                category = "boundary",
                passed = !plan.boundaries.finalMediaApproval &&
                    !plan.boundaries.providerMediaHandoff &&
                    !plan.boundaries.canonRegistryOrProductionLock,
                message = "Manifest draft plan review is not final media approval, not provider/media handoff, and no production lock.",
                evidence = listOf(
                    "finalMediaApproval=${plan.boundaries.finalMediaApproval}",
                    "providerMediaHandoff=${plan.boundaries.providerMediaHandoff}",
                    "canonRegistryOrProductionLock=${plan.boundaries.canonRegistryOrProductionLock}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_FINALIZATION_FORBIDDEN",
                path = "$.boundaries.finalization",
                suggestedFix = "Keep Phase 13 as a local JSON review gate with no final media approval, no provider/media handoff, and no production lock.",
            ),
            check(
                checkId = "source-checks-pass",
                category = "review_gate",
                passed = plan.checks.isNotEmpty() &&
                    plan.checks.filter { it.requiredForPlan }.all { it.status == "PASS" },
                message = "Manifest draft plan review requires non-empty Phase 12 checks and all required checks PASS.",
                evidence = listOf(
                    "checks=${plan.checks.size}",
                    "requiredFail=${plan.checks.count { it.requiredForPlan && it.status != "PASS" }}",
                    "summaryChecksFail=${planReviewSummary.sourcePlanChecksFail}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_CHECKS_NOT_PASS",
                path = "$.checks",
                suggestedFix = "Resolve Phase 12 plan checks before reviewing the plan.",
            ),
            check(
                checkId = "plan-sections-present",
                category = "plan_completeness",
                passed = plan.manifestFieldPlan.panelManifestPlans.isNotEmpty() &&
                    planReviewSummary.manifestFields > 0 &&
                    plan.requiredManifestDraftWorkItems.isNotEmpty() &&
                    plan.verificationPlan.isNotEmpty() &&
                    planReviewSummary.targetDurationMillis > 0,
                message = "Manifest draft plan review requires manifest field plan, mapped panel fields, work items, verification plan, and target duration.",
                evidence = listOf(
                    "manifestPanels=${plan.manifestFieldPlan.panelManifestPlans.size}",
                    "manifestFields=${planReviewSummary.manifestFields}",
                    "requiredManifestDraftWorkItems=${plan.requiredManifestDraftWorkItems.size}",
                    "verificationSteps=${plan.verificationPlan.size}",
                    "targetDurationMillis=${planReviewSummary.targetDurationMillis}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_SECTIONS_MISSING",
                path = "$.manifestDraftPlanSections",
                suggestedFix = "Regenerate Phase 12 plan with manifestFieldPlan, requiredManifestDraftWorkItems, and verificationPlan.",
            ),
            check(
                checkId = "manifest-remains-plan-only",
                category = "manifest_boundary",
                passed = plan.manifestFieldPlan.planningOnly &&
                    !plan.manifestFieldPlan.manifestGenerated &&
                    plan.manifestFieldPlan.panelManifestPlans.all { !it.manifestGenerated && !it.rendererReady } &&
                    manifestFieldMappings(plan).all { !it.generatedNow } &&
                    !plan.manifestFieldPlan.renderSettingsPlan.generatedNow,
                message = "Manifest draft plan review allows ComicVideoRenderHandoffManifest only as planning notes; no real manifest may be generated.",
                evidence = listOf(
                    "planningOnly=${plan.manifestFieldPlan.planningOnly}",
                    "manifestGenerated=${plan.manifestFieldPlan.manifestGenerated}",
                    "panelManifestGenerated=${plan.manifestFieldPlan.panelManifestPlans.count { it.manifestGenerated }}",
                    "panelRendererReady=${plan.manifestFieldPlan.panelManifestPlans.count { it.rendererReady }}",
                    "generatedNowFields=${manifestFieldMappings(plan).count { it.generatedNow }}",
                    "renderSettingsGeneratedNow=${plan.manifestFieldPlan.renderSettingsPlan.generatedNow}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_MANIFEST_GENERATED_FORBIDDEN",
                path = "$.manifestFieldPlan",
                suggestedFix = "Keep Phase 13 as review only and defer any ComicVideoRenderHandoffManifest generation to a future explicit task.",
            ),
            check(
                checkId = "no-findings",
                category = "review_gate",
                passed = plan.findingCount == 0 && plan.findings.isEmpty(),
                message = "Manifest draft plan review requires no Phase 12 findings.",
                evidence = listOf(
                    "findingCount=${plan.findingCount}",
                    "findings=${plan.findings.size}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_FINDINGS_PRESENT",
                path = "$.findings",
                suggestedFix = "Resolve Phase 12 plan findings before Phase 13 review.",
            ),
            check(
                checkId = "no-readiness-overclaim",
                category = "renderer_compatibility",
                passed = !plan.boundaries.rendererReady &&
                    !plan.boundaries.mp4ReadinessClaimed &&
                    !planReviewSummary.rendererReady &&
                    !planReviewSummary.mp4ReadinessClaimed &&
                    !planReviewSummary.manifestGenerated,
                message = "Manifest draft plan review must preserve rendererReady=false, mp4ReadinessClaimed=false, manifestGenerated=false, adapterImplementationExecuted=false, and mediaGenerated=false.",
                evidence = listOf(
                    "rendererReady=${plan.boundaries.rendererReady}",
                    "mp4ReadinessClaimed=${plan.boundaries.mp4ReadinessClaimed}",
                    "manifestGenerated=${planReviewSummary.manifestGenerated}",
                    "adapterImplementationExecuted=${planReviewSummary.adapterImplementationExecuted}",
                    "mediaGenerated=${plan.boundaries.mediaGenerated}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_READY_OVERCLAIM",
                path = "$.rendererCompatibility",
                suggestedFix = "Keep rendererReady=false, mp4ReadinessClaimed=false, and all no-media/no-manifest flags false until future explicit verification.",
            ),
        )

    private fun planReviewSummary(
        plan: LocalRendererManifestDraftPlan,
    ): LocalRendererManifestDraftPlanReviewSummary =
        LocalRendererManifestDraftPlanReviewSummary(
            sourcePlanChecks = plan.checks.size,
            sourcePlanChecksPass = plan.checks.count { it.status == "PASS" },
            sourcePlanChecksFail = plan.checks.count { it.status != "PASS" },
            manifestPanels = plan.manifestFieldPlan.panelManifestPlans.size,
            manifestFields = manifestFieldMappings(plan).size,
            requiredManifestDraftWorkItems = plan.requiredManifestDraftWorkItems.size,
            verificationSteps = plan.verificationPlan.size,
            targetDurationMillis = plan.manifestFieldPlan.renderSettingsPlan.totalDurationMillis,
            findingCount = plan.findingCount,
            planningOnly = plan.manifestFieldPlan.planningOnly,
            manifestGenerated = plan.boundaries.manifestGenerated ||
                plan.manifestDraftPlanSummary.manifestGenerated ||
                plan.manifestFieldPlan.manifestGenerated,
            adapterImplementationExecuted = plan.boundaries.adapterImplementationExecuted ||
                plan.manifestDraftPlanSummary.adapterImplementationExecuted,
            rendererReady = plan.boundaries.rendererReady ||
                plan.manifestDraftPlanSummary.rendererReady ||
                plan.manifestFieldPlan.panelManifestPlans.any { it.rendererReady },
            mp4ReadinessClaimed = plan.boundaries.mp4ReadinessClaimed ||
                plan.manifestDraftPlanSummary.mp4ReadinessClaimed,
        )

    private fun sourceAlignmentSummary(
        plan: LocalRendererManifestDraftPlan,
    ): LocalRendererManifestDraftPlanReviewSourceAlignmentSummary =
        LocalRendererManifestDraftPlanReviewSourceAlignmentSummary(
            manifestDraftPlanIdPresent = plan.planId.isNotBlank(),
            sourceImplementationDraftIdPresent = plan.sourceDraftId.isNotBlank(),
            sourceImplementationDraftReviewIdPresent = plan.sourceDraftReviewId.isNotBlank(),
            sourceStoryInputPackIdPresent = plan.sourceStoryInputPackId.isNotBlank(),
            sourceAlignmentFromPlanPass = plan.sourceAlignmentSummary.draftIdAligned &&
                plan.sourceAlignmentSummary.planIdAligned &&
                plan.sourceAlignmentSummary.planReviewIdAligned &&
                plan.sourceAlignmentSummary.dryRunIdAligned &&
                plan.sourceAlignmentSummary.handoffIdAligned &&
                plan.sourceAlignmentSummary.handoffReviewIdAligned &&
                plan.sourceAlignmentSummary.storyInputPackIdAligned,
            manifestDraftPlanId = plan.planId,
            sourceImplementationDraftId = plan.sourceDraftId,
            sourceImplementationDraftReviewId = plan.sourceDraftReviewId,
            sourcePlanId = plan.sourcePlanId,
            sourcePlanReviewId = plan.sourcePlanReviewId,
            sourceDryRunId = plan.sourceDryRunId,
            sourceHandoffId = plan.sourceHandoffId,
            sourceHandoffReviewId = plan.sourceHandoffReviewId,
            sourceStoryInputPackId = plan.sourceStoryInputPackId,
        )

    private fun boundarySummary(
        boundaries: LocalRendererAdapterImplementationDraftBoundaries,
    ): LocalRendererManifestDraftPlanReviewBoundarySummary =
        LocalRendererManifestDraftPlanReviewBoundarySummary(
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
            rendererReady = boundaries.rendererReady,
            mp4ReadinessClaimed = boundaries.mp4ReadinessClaimed,
        )

    private fun readinessSummary(
        passed: Boolean,
    ): LocalRendererManifestDraftPlanReadinessSummary =
        LocalRendererManifestDraftPlanReadinessSummary(
            readyForFutureManifestJsonDraft = passed,
            realManifestGenerated = false,
            rendererReady = false,
            mp4ReadinessClaimed = false,
            adapterExecutionStillRequiresExplicitTask = true,
            manifestJsonDraftStillRequiresExplicitTask = true,
            requiredBeforeManifestJsonDraft = listOf(
                "Pass Phase 13 manifest draft plan review.",
                "Assign a separate local_renderer_manifest_draft_json_draft task.",
                "Do not generate ComicVideoRenderHandoffManifest in Phase 13.",
                "Do not run renderer/ffmpeg/TTS or generate media.",
                "Boundary: no media generation, no adapter execution, no real ComicVideoRenderHandoffManifest.",
            ),
            summary = "Review gate only: Phase 12 plan may be accepted for a future manifest JSON draft task, but no real manifest generation, renderer readiness, MP4 readiness, adapter execution, or media generation is claimed.",
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

    private fun manifestFieldMappings(plan: LocalRendererManifestDraftPlan): List<LocalRendererManifestFieldMappingPlan> =
        plan.manifestFieldPlan.panelManifestPlans.flatMap {
            it.plannedPanelTimelineFields + it.plannedVoiceTimelineFields + it.plannedSubtitleTimelineFields
        }

    private fun check(
        checkId: String,
        category: String,
        passed: Boolean,
        message: String,
        evidence: List<String>,
        findings: MutableList<LocalRendererManifestDraftPlanReviewFinding>,
        ruleId: String,
        path: String,
        suggestedFix: String,
    ): LocalRendererManifestDraftPlanReviewCheck {
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
        return LocalRendererManifestDraftPlanReviewCheck(
            checkId = checkId,
            status = if (passed) "PASS" else "FAIL",
            category = category,
            message = message,
            evidence = evidence,
        )
    }

    private fun List<LocalRendererManifestDraftPlanReviewFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): LocalRendererManifestDraftPlanReviewFinding =
        LocalRendererManifestDraftPlanReviewFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
}
