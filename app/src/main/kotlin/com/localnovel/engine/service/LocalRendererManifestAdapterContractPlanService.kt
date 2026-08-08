package com.localnovel.engine.service

import com.localnovel.engine.model.LocalRendererManifestAdapterContractBoundarySummary
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlan
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanBoundaries
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanCheck
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanFinding
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanSummary
import com.localnovel.engine.model.LocalRendererManifestAdapterContractRequirement
import com.localnovel.engine.model.LocalRendererManifestAdapterContractSourceAlignmentSummary
import com.localnovel.engine.model.LocalRendererManifestAdapterContractVerificationStep
import com.localnovel.engine.model.LocalRendererManifestAdapterContractWorkItem
import com.localnovel.engine.model.LocalRendererManifestAdapterInputMappingEntry
import com.localnovel.engine.model.LocalRendererManifestAdapterInputMappingPlan
import com.localnovel.engine.model.LocalRendererManifestDraftJsonAdapterDryRunReport
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class LocalRendererManifestAdapterContractPlanService {
    private val requiredDryRunStatus = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_PASS"
    private val requiredDryRunBoundary = "local_renderer_manifest_draft_json_adapter_dry_run_only_not_renderer_execution"
    private val requiredDryRunNextStep = "local_renderer_manifest_adapter_contract_plan"
    private val passStatus = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN"
    private val failStatus = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_FAIL"
    private val downstreamBoundary = "local_renderer_manifest_adapter_contract_plan_only_not_adapter_implementation"
    private val nextAllowedDownstreamStep = "local_renderer_manifest_adapter_contract_plan_review"
    private val requiredBoundaryFlags = listOf(
        "mediaGenerated=false",
        "rendererInvocation=none",
        "providerInvocation=none",
        "ttsInvocation=none",
        "adapterImplementationExecuted=false",
        "adapterExecuted=false",
        "noExternalPaidProviderTools=true",
        "rendererReady=false",
        "mp4ReadinessClaimed=false",
        "draftManifestGenerated=true",
        "productionManifestGenerated=false",
        "productionReady=false",
        "productionApproved=false",
    )

    fun planFile(
        dryRunReportPath: Path,
        outPath: Path? = null,
        planId: String = "",
    ): LocalRendererManifestAdapterContractPlan {
        val dryRun = JsonSupport.json.decodeFromString<LocalRendererManifestDraftJsonAdapterDryRunReport>(
            Files.readString(dryRunReportPath).removePrefix("\uFEFF"),
        )
        val plan = plan(
            dryRun = dryRun,
            dryRunReportFile = dryRunReportPath.toString(),
            planId = planId,
        )
        if (outPath != null) {
            outPath.parent?.let { Files.createDirectories(it) }
            Files.writeString(outPath, JsonSupport.json.encodeToString(plan))
        }
        return plan
    }

    fun plan(
        dryRun: LocalRendererManifestDraftJsonAdapterDryRunReport,
        dryRunReportFile: String = "",
        planId: String = "",
    ): LocalRendererManifestAdapterContractPlan {
        val findings = mutableListOf<LocalRendererManifestAdapterContractPlanFinding>()
        val alignment = sourceAlignmentSummary(dryRun)
        val mappingPlan = adapterInputMappingPlan(dryRun)
        val requirements = adapterContractRequirements(dryRun)
        val workItems = requiredAdapterContractWorkItems()
        val verification = verificationPlan()
        val boundarySummary = boundarySummary(dryRun)
        val summary = adapterContractPlanSummary(
            dryRun = dryRun,
            mappingPlan = mappingPlan,
            requirements = requirements,
            workItems = workItems,
            verification = verification,
        )
        val checks = buildChecks(
            dryRun = dryRun,
            dryRunReportFile = dryRunReportFile,
            alignment = alignment,
            summary = summary,
            mappingPlan = mappingPlan,
            requirements = requirements,
            workItems = workItems,
            verification = verification,
            boundarySummary = boundarySummary,
            findings = findings,
        )
        val verdict = if (findings.hasBlocking()) "FAIL" else "PASS"
        val resolvedPlanId = planId.ifBlank {
            dryRun.dryRunId.takeIf { it.isNotBlank() }?.let { "$it-adapter-contract-plan-v0" }
                ?: dryRun.sourceDraftId.takeIf { it.isNotBlank() }?.let { "$it-adapter-contract-plan-v0" }
                ?: ""
        }

        return LocalRendererManifestAdapterContractPlan(
            verdict = verdict,
            status = if (verdict == "PASS") passStatus else failStatus,
            planId = resolvedPlanId,
            sourceDryRunReportFile = dryRunReportFile,
            sourcePhase16DryRunId = dryRun.dryRunId,
            sourceDraftId = dryRun.sourceDraftId,
            sourceReviewId = dryRun.sourceReviewId,
            sourceManifestDraftPlanId = dryRun.sourceManifestDraftPlanId,
            sourceManifestDraftPlanReviewId = dryRun.sourceManifestDraftPlanReviewId,
            sourceImplementationDraftId = dryRun.sourceImplementationDraftId,
            sourceImplementationDraftReviewId = dryRun.sourceImplementationDraftReviewId,
            sourcePlanId = dryRun.sourcePlanId,
            sourcePlanReviewId = dryRun.sourcePlanReviewId,
            sourceLocalRendererAdapterDryRunId = dryRun.sourceDryRunId,
            sourceHandoffId = dryRun.sourceHandoffId,
            sourceHandoffReviewId = dryRun.sourceHandoffReviewId,
            sourceStoryInputPackId = dryRun.sourceStoryInputPackId,
            checks = checks,
            sourceAlignmentSummary = alignment,
            adapterContractPlanSummary = summary,
            adapterContractRequirements = if (verdict == "PASS") requirements else emptyList(),
            adapterInputMappingPlan = if (verdict == "PASS") mappingPlan else LocalRendererManifestAdapterInputMappingPlan(),
            requiredAdapterContractWorkItems = if (verdict == "PASS") workItems else emptyList(),
            verificationPlan = if (verdict == "PASS") verification else emptyList(),
            boundarySummary = boundarySummary,
            boundaries = outputBoundaries(dryRun.boundaries.draftManifestGenerated),
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
        dryRun: LocalRendererManifestDraftJsonAdapterDryRunReport,
        dryRunReportFile: String,
        alignment: LocalRendererManifestAdapterContractSourceAlignmentSummary,
        summary: LocalRendererManifestAdapterContractPlanSummary,
        mappingPlan: LocalRendererManifestAdapterInputMappingPlan,
        requirements: List<LocalRendererManifestAdapterContractRequirement>,
        workItems: List<LocalRendererManifestAdapterContractWorkItem>,
        verification: List<LocalRendererManifestAdapterContractVerificationStep>,
        boundarySummary: LocalRendererManifestAdapterContractBoundarySummary,
        findings: MutableList<LocalRendererManifestAdapterContractPlanFinding>,
    ): List<LocalRendererManifestAdapterContractPlanCheck> =
        listOf(
            check(
                checkId = "explicit-source-file",
                category = "source_validation",
                passed = dryRunReportFile.isNotBlank() &&
                    dryRun.dryRunId.isNotBlank() &&
                    dryRun.sourceDraftId.isNotBlank() &&
                    dryRun.sourceReviewId.isNotBlank() &&
                    dryRun.sourceStoryInputPackId.isNotBlank(),
                message = "Adapter contract plan requires an explicit Phase 16 dry-run report file.",
                evidence = listOf(
                    "dryRunReportFile=$dryRunReportFile",
                    "dryRunId=${dryRun.dryRunId}",
                    "sourceDraftId=${dryRun.sourceDraftId}",
                    "sourceReviewId=${dryRun.sourceReviewId}",
                    "sourceStoryInputPackId=${dryRun.sourceStoryInputPackId}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_SOURCE_FILE_MISSING",
                path = "$.inputs",
                suggestedFix = "Pass --dry-run-report and --out explicitly.",
            ),
            check(
                checkId = "dry-run-state",
                category = "source_validation",
                passed = dryRun.verdict == "PASS" &&
                    dryRun.status == requiredDryRunStatus &&
                    dryRun.downstreamBoundary == requiredDryRunBoundary &&
                    dryRun.nextAllowedDownstreamStep == requiredDryRunNextStep,
                message = "Adapter contract plan requires PASS Phase 16 adapter dry-run state.",
                evidence = listOf(
                    "verdict=${dryRun.verdict}",
                    "status=${dryRun.status}",
                    "downstreamBoundary=${dryRun.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${dryRun.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_DRY_RUN_STATE_INVALID",
                path = "$.dryRunState",
                suggestedFix = "Use the accepted PASS Phase 16 adapter dry-run report.",
            ),
            check(
                checkId = "source-ids-present",
                category = "source_linkage",
                passed = alignment.sourceIdsPresent,
                message = "Adapter contract plan requires non-empty source ids from Phase 16.",
                evidence = listOf(
                    "sourcePhase16DryRunIdPresent=${alignment.sourcePhase16DryRunIdPresent}",
                    "sourceDraftIdPresent=${alignment.sourceDraftIdPresent}",
                    "sourceReviewIdPresent=${alignment.sourceReviewIdPresent}",
                    "sourceStoryInputPackIdPresent=${alignment.sourceStoryInputPackIdPresent}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_SOURCE_IDS_MISSING",
                path = "$.sourceAlignmentSummary",
                suggestedFix = "Use a Phase 16 dry-run report generated from complete upstream source files.",
            ),
            check(
                checkId = "source-checks-pass",
                category = "review_gate",
                passed = dryRun.checks.isNotEmpty() &&
                    dryRun.checks.filter { it.requiredForDryRun }.all { it.status == "PASS" } &&
                    dryRun.findingCount == 0 &&
                    dryRun.findings.isEmpty(),
                message = "Adapter contract plan requires non-empty Phase 16 checks, all required checks PASS, and no findings.",
                evidence = listOf(
                    "sourceChecks=${dryRun.checks.size}",
                    "sourceRequiredFail=${dryRun.checks.count { it.requiredForDryRun && it.status != "PASS" }}",
                    "sourceFindingCount=${dryRun.findingCount}",
                    "sourceFindings=${dryRun.findings.size}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_SOURCE_CHECKS_OR_FINDINGS_FAIL",
                path = "$.sourceChecks",
                suggestedFix = "Resolve Phase 16 required checks and findings before planning an adapter contract.",
            ),
            check(
                checkId = "local-boundaries",
                category = "boundary",
                passed = boundarySummary.sourceLocalOnly &&
                    !boundarySummary.mediaGenerated &&
                    boundarySummary.rendererInvocation == "none" &&
                    boundarySummary.providerInvocation == "none" &&
                    boundarySummary.ttsInvocation == "none" &&
                    !boundarySummary.adapterImplementationExecuted &&
                    !boundarySummary.adapterExecuted &&
                    boundarySummary.draftManifestGenerated &&
                    !boundarySummary.productionManifestGenerated &&
                    !boundarySummary.productionReady &&
                    !boundarySummary.productionApproved &&
                    !boundarySummary.finalMediaApproval &&
                    !boundarySummary.providerMediaHandoff &&
                    !boundarySummary.release &&
                    !boundarySummary.canonRegistryOrProductionLock &&
                    !boundarySummary.rendererReady &&
                    !boundarySummary.mp4ReadinessClaimed,
                message = "Adapter contract plan requires local-only no-media boundaries from Phase 16.",
                evidence = listOf(
                    "sourceLocalOnly=${boundarySummary.sourceLocalOnly}",
                    "mediaGenerated=${boundarySummary.mediaGenerated}",
                    "rendererInvocation=${boundarySummary.rendererInvocation}",
                    "providerInvocation=${boundarySummary.providerInvocation}",
                    "ttsInvocation=${boundarySummary.ttsInvocation}",
                    "adapterImplementationExecuted=${boundarySummary.adapterImplementationExecuted}",
                    "adapterExecuted=${boundarySummary.adapterExecuted}",
                    "draftManifestGenerated=${boundarySummary.draftManifestGenerated}",
                    "productionManifestGenerated=${boundarySummary.productionManifestGenerated}",
                    "productionReady=${boundarySummary.productionReady}",
                    "productionApproved=${boundarySummary.productionApproved}",
                    "rendererReady=${boundarySummary.rendererReady}",
                    "mp4ReadinessClaimed=${boundarySummary.mp4ReadinessClaimed}",
                ) + requiredBoundaryFlags,
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_BOUNDARY_FAIL",
                path = "$.boundaries",
                suggestedFix = "Use local-only JSON with no provider/API, renderer, TTS, media, adapter execution, production manifest, production approval, readiness claim, release, or production lock.",
            ),
            check(
                checkId = "dry-run-mapping-complete",
                category = "adapter_mapping",
                passed = dryRun.panelMappingSummary.draftPanelCount > 0 &&
                    dryRun.panelMappingSummary.adapterDryRunMappingEntries == dryRun.panelMappingSummary.draftPanelCount &&
                    dryRun.panelMappingSummary.mappedPanelCount == dryRun.panelMappingSummary.draftPanelCount &&
                    dryRun.panelMappingSummary.unmappedPanelCount == 0 &&
                    dryRun.panelMappingSummary.missingRequiredFieldCount == 0 &&
                    dryRun.adapterDryRunMappingEntries.size == dryRun.panelMappingSummary.draftPanelCount &&
                    dryRun.adapterDryRunMappingEntries.all {
                        it.canMapForFutureAdapter &&
                            it.missingRequiredFields.isEmpty() &&
                            it.mappedDraftFieldCount > 0
                    },
                message = "Adapter contract plan requires every Phase 16 dry-run panel to be mapped with no missing fields.",
                evidence = listOf(
                    "draftPanelCount=${dryRun.panelMappingSummary.draftPanelCount}",
                    "adapterDryRunMappingEntries=${dryRun.panelMappingSummary.adapterDryRunMappingEntries}",
                    "mappedPanelCount=${dryRun.panelMappingSummary.mappedPanelCount}",
                    "unmappedPanelCount=${dryRun.panelMappingSummary.unmappedPanelCount}",
                    "missingRequiredFieldCount=${dryRun.panelMappingSummary.missingRequiredFieldCount}",
                    "mappingEntries=${dryRun.adapterDryRunMappingEntries.size}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_MAPPING_INCOMPLETE",
                path = "$.adapterDryRunMappingEntries",
                suggestedFix = "Resolve unmapped panels and missing required fields in Phase 16 before contract planning.",
            ),
            check(
                checkId = "field-coverage-complete",
                category = "field_coverage",
                passed = dryRun.draftFieldCoverageSummary.totalDraftFields > 0 &&
                    dryRun.draftFieldCoverageSummary.mappedDraftFields ==
                    dryRun.draftFieldCoverageSummary.totalDraftFields &&
                    dryRun.draftFieldCoverageSummary.missingRequiredDraftFields == 0 &&
                    summary.totalDraftFields > 0,
                message = "Adapter contract plan requires complete draft field coverage from Phase 16.",
                evidence = listOf(
                    "totalDraftFields=${dryRun.draftFieldCoverageSummary.totalDraftFields}",
                    "mappedDraftFields=${dryRun.draftFieldCoverageSummary.mappedDraftFields}",
                    "missingRequiredDraftFields=${dryRun.draftFieldCoverageSummary.missingRequiredDraftFields}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_FIELD_COVERAGE_INCOMPLETE",
                path = "$.draftFieldCoverageSummary",
                suggestedFix = "Use a Phase 16 dry-run report with all required draft fields mapped.",
            ),
            check(
                checkId = "contract-content-present",
                category = "plan_completeness",
                passed = requirements.isNotEmpty() &&
                    mappingPlan.entries.isNotEmpty() &&
                    workItems.isNotEmpty() &&
                    verification.isNotEmpty() &&
                    mappingPlan.entries.all { it.plannedAdapterContractFields.isNotEmpty() },
                message = "Adapter contract plan must contain contract requirements, input mapping, work items, and verification steps.",
                evidence = listOf(
                    "requirements=${requirements.size}",
                    "mappingEntries=${mappingPlan.entries.size}",
                    "workItems=${workItems.size}",
                    "verificationSteps=${verification.size}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_CONTENT_MISSING",
                path = "$.adapterContractPlan",
                suggestedFix = "Build adapter contract requirements, mapping plan, work items, and verification plan from Phase 16 mappings.",
            ),
            check(
                checkId = "plan-only-boundary",
                category = "adapter_contract",
                passed = mappingPlan.planningOnly &&
                    !mappingPlan.adapterImplementationGenerated &&
                    !mappingPlan.adapterExecuted &&
                    mappingPlan.entries.all {
                        !it.adapterImplementationGenerated &&
                            !it.adapterExecuted &&
                            it.rendererInvocation == "none" &&
                            !it.mediaGenerated &&
                            !it.productionManifestGenerated
                    } &&
                    workItems.all { it.reviewGateRequiredBeforeImplementation == nextAllowedDownstreamStep } &&
                    verification.all {
                        !it.requiresAdapterExecution &&
                            !it.requiresRendererExecution &&
                            !it.requiresMediaGeneration &&
                            !it.requiresProductionManifestApproval
                    },
                message = "Adapter contract plan is planning only; no adapter implementation, execution, renderer, media, or production approval is allowed.",
                evidence = listOf(
                    "planningOnly=${mappingPlan.planningOnly}",
                    "adapterImplementationGenerated=${mappingPlan.adapterImplementationGenerated}",
                    "adapterExecuted=${mappingPlan.adapterExecuted}",
                    "adapter contract",
                    "no media generation",
                    "ComicVideoRenderHandoffManifest generation=false",
                    "production lock=false",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_EXECUTION_FORBIDDEN",
                path = "$.adapterInputMappingPlan",
                suggestedFix = "Keep Phase 17 as adapter contract planning only.",
            ),
        )

    private fun sourceAlignmentSummary(
        dryRun: LocalRendererManifestDraftJsonAdapterDryRunReport,
    ): LocalRendererManifestAdapterContractSourceAlignmentSummary {
        val present = listOf(
            dryRun.dryRunId,
            dryRun.sourceDraftId,
            dryRun.sourceReviewId,
            dryRun.sourceManifestDraftPlanId,
            dryRun.sourceManifestDraftPlanReviewId,
            dryRun.sourceImplementationDraftId,
            dryRun.sourceImplementationDraftReviewId,
            dryRun.sourcePlanId,
            dryRun.sourcePlanReviewId,
            dryRun.sourceDryRunId,
            dryRun.sourceHandoffId,
            dryRun.sourceHandoffReviewId,
            dryRun.sourceStoryInputPackId,
        ).all { it.isNotBlank() }
        return LocalRendererManifestAdapterContractSourceAlignmentSummary(
            sourceIdsPresent = present,
            sourcePhase16DryRunIdPresent = dryRun.dryRunId.isNotBlank(),
            sourceDraftIdPresent = dryRun.sourceDraftId.isNotBlank(),
            sourceReviewIdPresent = dryRun.sourceReviewId.isNotBlank(),
            sourceStoryInputPackIdPresent = dryRun.sourceStoryInputPackId.isNotBlank(),
            sourcePhase16DryRunId = dryRun.dryRunId,
            sourceDraftId = dryRun.sourceDraftId,
            sourceReviewId = dryRun.sourceReviewId,
            sourceManifestDraftPlanId = dryRun.sourceManifestDraftPlanId,
            sourceManifestDraftPlanReviewId = dryRun.sourceManifestDraftPlanReviewId,
            sourceImplementationDraftId = dryRun.sourceImplementationDraftId,
            sourceImplementationDraftReviewId = dryRun.sourceImplementationDraftReviewId,
            sourcePlanId = dryRun.sourcePlanId,
            sourcePlanReviewId = dryRun.sourcePlanReviewId,
            sourceLocalRendererAdapterDryRunId = dryRun.sourceDryRunId,
            sourceHandoffId = dryRun.sourceHandoffId,
            sourceHandoffReviewId = dryRun.sourceHandoffReviewId,
            sourceStoryInputPackId = dryRun.sourceStoryInputPackId,
        )
    }

    private fun adapterContractPlanSummary(
        dryRun: LocalRendererManifestDraftJsonAdapterDryRunReport,
        mappingPlan: LocalRendererManifestAdapterInputMappingPlan,
        requirements: List<LocalRendererManifestAdapterContractRequirement>,
        workItems: List<LocalRendererManifestAdapterContractWorkItem>,
        verification: List<LocalRendererManifestAdapterContractVerificationStep>,
    ): LocalRendererManifestAdapterContractPlanSummary =
        LocalRendererManifestAdapterContractPlanSummary(
            sourceChecks = dryRun.checks.size,
            sourceChecksPass = dryRun.checks.count { it.status == "PASS" },
            sourceChecksFail = dryRun.checks.count { it.status != "PASS" },
            sourceFindingCount = dryRun.findingCount,
            draftPanelCount = dryRun.panelMappingSummary.draftPanelCount,
            adapterDryRunMappingEntries = dryRun.panelMappingSummary.adapterDryRunMappingEntries,
            mappedPanelCount = dryRun.panelMappingSummary.mappedPanelCount,
            unmappedPanelCount = dryRun.panelMappingSummary.unmappedPanelCount,
            totalDraftFields = dryRun.draftFieldCoverageSummary.totalDraftFields,
            mappedDraftFields = dryRun.draftFieldCoverageSummary.mappedDraftFields,
            missingRequiredFieldCount = dryRun.panelMappingSummary.missingRequiredFieldCount,
            targetDurationMillis = dryRun.panelMappingSummary.targetDurationMillis,
            adapterContractRequirements = requirements.size,
            adapterInputMappingEntries = mappingPlan.entries.size,
            requiredAdapterContractWorkItems = workItems.size,
            verificationSteps = verification.size,
            planningOnly = true,
            adapterImplementationExecuted = dryRun.boundaries.adapterImplementationExecuted,
            adapterExecuted = dryRun.boundaries.adapterExecuted,
            rendererReady = dryRun.boundaries.rendererReady,
            mp4ReadinessClaimed = dryRun.boundaries.mp4ReadinessClaimed,
        )

    private fun adapterContractRequirements(
        dryRun: LocalRendererManifestDraftJsonAdapterDryRunReport,
    ): List<LocalRendererManifestAdapterContractRequirement> =
        listOf(
            LocalRendererManifestAdapterContractRequirement(
                requirementId = "explicit-input-contract",
                title = "Explicit adapter input contract",
                description = "Future adapter implementation must accept a reviewed draft manifest JSON packet and reviewed adapter contract plan explicitly.",
                requiredFields = listOf("sourceDryRunId", "sourceDraftId", "sourceReviewId", "sourceStoryInputPackId"),
                sourceEvidence = listOf("dryRunId=${dryRun.dryRunId}", "sourceStoryInputPackId=${dryRun.sourceStoryInputPackId}"),
            ),
            LocalRendererManifestAdapterContractRequirement(
                requirementId = "panel-mapping-contract",
                title = "Panel mapping contract",
                description = "Future adapter implementation must preserve one deterministic mapping per draft panel.",
                requiredFields = listOf("panelId", "sourceSceneId", "sourceBeatId", "startMillis", "endMillis", "durationMillis"),
                sourceEvidence = listOf(
                    "draftPanelCount=${dryRun.panelMappingSummary.draftPanelCount}",
                    "mappedPanelCount=${dryRun.panelMappingSummary.mappedPanelCount}",
                    "unmappedPanelCount=${dryRun.panelMappingSummary.unmappedPanelCount}",
                ),
            ),
            LocalRendererManifestAdapterContractRequirement(
                requirementId = "field-coverage-contract",
                title = "Draft field coverage contract",
                description = "Future adapter implementation must account for every mapped draft field without treating it as production manifest approval.",
                requiredFields = listOf("mappedDraftFieldTargets", "mappedDraftFieldCount", "missingRequiredFieldCount"),
                sourceEvidence = listOf(
                    "totalDraftFields=${dryRun.draftFieldCoverageSummary.totalDraftFields}",
                    "mappedDraftFields=${dryRun.draftFieldCoverageSummary.mappedDraftFields}",
                    "missingRequiredDraftFields=${dryRun.draftFieldCoverageSummary.missingRequiredDraftFields}",
                ),
            ),
            LocalRendererManifestAdapterContractRequirement(
                requirementId = "local-boundary-contract",
                title = "Local-only no-media boundary contract",
                description = "Future adapter implementation must preserve no provider/API, no renderer/ffmpeg/TTS, no media generation, and no production lock.",
                requiredFields = requiredBoundaryFlags,
                sourceEvidence = listOf("adapter contract", "no media generation", "production lock=false"),
            ),
        )

    private fun adapterInputMappingPlan(
        dryRun: LocalRendererManifestDraftJsonAdapterDryRunReport,
    ): LocalRendererManifestAdapterInputMappingPlan =
        LocalRendererManifestAdapterInputMappingPlan(
            planningOnly = true,
            adapterImplementationGenerated = false,
            adapterExecuted = false,
            targetFutureContract = "local_renderer_manifest_adapter_contract",
            targetFutureManifestType = "ComicVideoRenderHandoffManifest",
            mappingBoundary = downstreamBoundary,
            entries = dryRun.adapterDryRunMappingEntries
                .sortedWith(compareBy({ it.order }, { it.startMillis }, { it.panelId }))
                .mapIndexed { index, mapping ->
                    val requiredFields = listOf(
                        "panelId",
                        "sourceSceneId",
                        "sourceBeatId",
                        "startMillis",
                        "endMillis",
                        "durationMillis",
                        "mappedDraftFieldTargets",
                        "localOnlyBoundaryFlags",
                    )
                    LocalRendererManifestAdapterInputMappingEntry(
                        order = mapping.order,
                        panelId = mapping.panelId,
                        sourceSceneId = mapping.sourceSceneId,
                        sourceBeatId = mapping.sourceBeatId,
                        startMillis = mapping.startMillis,
                        endMillis = mapping.endMillis,
                        durationMillis = mapping.durationMillis,
                        sourceMappedDraftFieldCount = mapping.mappedDraftFieldCount,
                        sourceMappedDraftFieldTargets = mapping.mappedDraftFieldTargets,
                        plannedAdapterContractFields = requiredFields,
                        sourceDryRunMappingPath = "$.adapterDryRunMappingEntries[$index]",
                        requiredFieldCount = requiredFields.size,
                        missingRequiredFieldCount = mapping.missingRequiredFields.size,
                        adapterImplementationGenerated = false,
                        adapterExecuted = false,
                        rendererInvocation = "none",
                        mediaGenerated = false,
                        productionManifestGenerated = false,
                        note = "Adapter contract planning only; no adapter implementation, no renderer execution, and no media generation.",
                    )
                },
            note = "Planning only. A later explicit review gate is required before any adapter implementation draft.",
        )

    private fun requiredAdapterContractWorkItems(): List<LocalRendererManifestAdapterContractWorkItem> =
        listOf(
            LocalRendererManifestAdapterContractWorkItem(
                workItemId = "adapter-contract-review-gate",
                title = "Review adapter contract plan before implementation",
                description = "Create a deterministic review gate for this adapter contract plan before any implementation draft.",
                targetOutcome = "Only reviewed contract requirements may feed a future adapter implementation draft.",
                mustNotDo = commonMustNotDo(),
            ),
            LocalRendererManifestAdapterContractWorkItem(
                workItemId = "adapter-contract-input-schema",
                title = "Define future adapter input schema",
                description = "Specify required input fields for future adapter implementation without creating code execution paths now.",
                targetOutcome = "A future explicit task can draft the adapter input contract from reviewed JSON.",
                mustNotDo = commonMustNotDo(),
            ),
            LocalRendererManifestAdapterContractWorkItem(
                workItemId = "adapter-contract-boundary-tests",
                title = "Plan boundary tests for future adapter work",
                description = "Future implementation must fail on media/provider/production overclaims and unmapped panels.",
                targetOutcome = "Future adapter implementation draft has a bounded verification plan.",
                mustNotDo = commonMustNotDo(),
            ),
        )

    private fun verificationPlan(): List<LocalRendererManifestAdapterContractVerificationStep> =
        listOf(
            LocalRendererManifestAdapterContractVerificationStep(
                verificationId = "contract-plan-review-json-parse",
                commandOrCheck = "Parse reviewed adapter contract plan JSON and confirm PASS boundaries.",
                expectedResult = "PASS with local-only/no-media/no-production-approval fields preserved.",
            ),
            LocalRendererManifestAdapterContractVerificationStep(
                verificationId = "contract-plan-mapping-coverage",
                commandOrCheck = "Verify every Phase 16 mapped panel has one planned adapter contract mapping entry.",
                expectedResult = "mappedPanelCount equals draftPanelCount and unmappedPanelCount remains 0.",
            ),
            LocalRendererManifestAdapterContractVerificationStep(
                verificationId = "contract-plan-boundary-regression",
                commandOrCheck = "Verify future tests fail on mediaGenerated=true, adapterExecuted=true, productionReady=true, or rendererReady=true.",
                expectedResult = "Unsafe boundary mutations produce FAIL reports.",
            ),
        )

    private fun boundarySummary(
        dryRun: LocalRendererManifestDraftJsonAdapterDryRunReport,
    ): LocalRendererManifestAdapterContractBoundarySummary =
        LocalRendererManifestAdapterContractBoundarySummary(
            sourceLocalOnly = localBoundariesOk(dryRun.boundaries),
            mediaGenerated = dryRun.boundaries.mediaGenerated,
            rendererInvocation = dryRun.boundaries.rendererInvocation,
            providerInvocation = dryRun.boundaries.providerInvocation,
            ttsInvocation = dryRun.boundaries.ttsInvocation,
            adapterImplementationExecuted = dryRun.boundaries.adapterImplementationExecuted,
            adapterExecuted = dryRun.boundaries.adapterExecuted,
            draftManifestGenerated = dryRun.boundaries.draftManifestGenerated,
            productionManifestGenerated = dryRun.boundaries.productionManifestGenerated,
            productionReady = dryRun.boundaries.productionReady,
            productionApproved = dryRun.boundaries.productionApproved,
            finalMediaApproval = dryRun.boundaries.finalMediaApproval,
            providerMediaHandoff = dryRun.boundaries.providerMediaHandoff,
            release = dryRun.boundaries.release,
            canonRegistryOrProductionLock = dryRun.boundaries.canonRegistryOrProductionLock,
            rendererReady = dryRun.boundaries.rendererReady,
            mp4ReadinessClaimed = dryRun.boundaries.mp4ReadinessClaimed,
        )

    private fun outputBoundaries(draftManifestGenerated: Boolean): LocalRendererManifestAdapterContractPlanBoundaries =
        LocalRendererManifestAdapterContractPlanBoundaries(
            fileProtocolJsonOnly = true,
            noExternalPaidProviderTools = true,
            providerInvocation = "none",
            ttsInvocation = "none",
            mediaGenerated = false,
            rendererInvocation = "none",
            directSqliteWrites = false,
            providerApiCalls = false,
            adapterImplementationExecuted = false,
            adapterExecuted = false,
            draftManifestGenerated = draftManifestGenerated,
            productionManifestGenerated = false,
            productionReady = false,
            productionApproved = false,
            finalMediaApproval = false,
            providerMediaHandoff = false,
            release = false,
            canonRegistryOrProductionLock = false,
            rendererReady = false,
            mp4ReadinessClaimed = false,
        )

    private fun localBoundariesOk(boundaries: com.localnovel.engine.model.LocalRendererManifestDraftJsonAdapterDryRunBoundaries): Boolean =
        boundaries.fileProtocolJsonOnly &&
            boundaries.noExternalPaidProviderTools &&
            boundaries.providerInvocation == "none" &&
            boundaries.ttsInvocation == "none" &&
            !boundaries.mediaGenerated &&
            boundaries.rendererInvocation == "none" &&
            !boundaries.directSqliteWrites &&
            !boundaries.providerApiCalls &&
            !boundaries.adapterImplementationExecuted &&
            !boundaries.adapterExecuted &&
            boundaries.draftManifestGenerated &&
            !boundaries.productionManifestGenerated &&
            !boundaries.productionReady &&
            !boundaries.productionApproved &&
            !boundaries.finalMediaApproval &&
            !boundaries.providerMediaHandoff &&
            !boundaries.release &&
            !boundaries.canonRegistryOrProductionLock &&
            !boundaries.rendererReady &&
            !boundaries.mp4ReadinessClaimed

    private fun commonMustNotDo(): List<String> =
        listOf(
            "Do not implement or execute an adapter.",
            "Do not run renderer/ffmpeg/TTS.",
            "Do not generate media or final video.",
            "Do not generate or approve a real production ComicVideoRenderHandoffManifest.",
            "Do not call provider/API or create provider/media handoff.",
            "Do not claim final media approval, release, canon, registry, approval, or production lock.",
        )

    private fun check(
        checkId: String,
        category: String,
        passed: Boolean,
        message: String,
        evidence: List<String>,
        findings: MutableList<LocalRendererManifestAdapterContractPlanFinding>,
        ruleId: String,
        path: String,
        suggestedFix: String,
    ): LocalRendererManifestAdapterContractPlanCheck {
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
        return LocalRendererManifestAdapterContractPlanCheck(
            checkId = checkId,
            status = if (passed) "PASS" else "FAIL",
            category = category,
            message = message,
            evidence = evidence,
        )
    }

    private fun List<LocalRendererManifestAdapterContractPlanFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): LocalRendererManifestAdapterContractPlanFinding =
        LocalRendererManifestAdapterContractPlanFinding(
            ruleId,
            category,
            severity,
            path,
            message,
            evidence,
            suggestedFix,
        )
}
