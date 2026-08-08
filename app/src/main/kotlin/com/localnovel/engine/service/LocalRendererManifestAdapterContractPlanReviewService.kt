package com.localnovel.engine.service

import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlan
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanBoundaries
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanReviewBoundarySummary
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanReviewBoundaries
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanReviewCheck
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanReviewFinding
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanReviewReport
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanReviewSourceAlignmentSummary
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanReviewSummary
import com.localnovel.engine.model.LocalRendererManifestAdapterContractVerificationReviewSummary
import com.localnovel.engine.model.LocalRendererManifestReviewedContractRequirementsSummary
import com.localnovel.engine.model.LocalRendererManifestReviewedInputMappingSummary
import com.localnovel.engine.model.LocalRendererManifestReviewedWorkItemsSummary
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class LocalRendererManifestAdapterContractPlanReviewService {
    private val requiredPlanStatus = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN"
    private val requiredPlanBoundary = "local_renderer_manifest_adapter_contract_plan_only_not_adapter_implementation"
    private val requiredPlanNextStep = "local_renderer_manifest_adapter_contract_plan_review"
    private val passStatus = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_REVIEW_PASS"
    private val failStatus = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_REVIEW_FAIL"
    private val downstreamBoundary = "local_renderer_manifest_adapter_contract_plan_review_only_not_adapter_implementation"
    private val nextAllowedDownstreamStep = "local_renderer_manifest_adapter_contract_draft"
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

    fun reviewFile(
        planPath: Path,
        outPath: Path? = null,
        reviewId: String = "",
    ): LocalRendererManifestAdapterContractPlanReviewReport {
        val plan = JsonSupport.json.decodeFromString<LocalRendererManifestAdapterContractPlan>(
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
        plan: LocalRendererManifestAdapterContractPlan,
        planFile: String = "",
        reviewId: String = "",
    ): LocalRendererManifestAdapterContractPlanReviewReport {
        val findings = mutableListOf<LocalRendererManifestAdapterContractPlanReviewFinding>()
        val alignment = sourceAlignmentSummary(plan)
        val requirementsSummary = requirementsSummary(plan)
        val inputMappingSummary = inputMappingSummary(plan)
        val workItemsSummary = workItemsSummary(plan)
        val verificationSummary = verificationSummary(plan)
        val boundarySummary = boundarySummary(plan)
        val reviewSummary = reviewSummary(
            plan = plan,
            requirementsSummary = requirementsSummary,
            inputMappingSummary = inputMappingSummary,
            workItemsSummary = workItemsSummary,
            verificationSummary = verificationSummary,
        )
        val checks = buildChecks(
            plan = plan,
            planFile = planFile,
            alignment = alignment,
            reviewSummary = reviewSummary,
            requirementsSummary = requirementsSummary,
            inputMappingSummary = inputMappingSummary,
            workItemsSummary = workItemsSummary,
            verificationSummary = verificationSummary,
            boundarySummary = boundarySummary,
            findings = findings,
        )
        val verdict = if (findings.hasBlocking()) "FAIL" else "PASS"
        val resolvedReviewId = reviewId.ifBlank {
            plan.planId.takeIf { it.isNotBlank() }?.let { "$it-review-v0" } ?: ""
        }

        return LocalRendererManifestAdapterContractPlanReviewReport(
            verdict = verdict,
            status = if (verdict == "PASS") passStatus else failStatus,
            reviewId = resolvedReviewId,
            sourcePlanFile = planFile,
            sourcePlanId = plan.planId,
            sourcePhase16DryRunId = plan.sourcePhase16DryRunId,
            sourceDraftId = plan.sourceDraftId,
            sourceReviewId = plan.sourceReviewId,
            sourceManifestDraftPlanId = plan.sourceManifestDraftPlanId,
            sourceManifestDraftPlanReviewId = plan.sourceManifestDraftPlanReviewId,
            sourceImplementationDraftId = plan.sourceImplementationDraftId,
            sourceImplementationDraftReviewId = plan.sourceImplementationDraftReviewId,
            sourceAdapterImplementationPlanId = plan.sourcePlanId,
            sourceAdapterImplementationPlanReviewId = plan.sourcePlanReviewId,
            sourceLocalRendererAdapterDryRunId = plan.sourceLocalRendererAdapterDryRunId,
            sourceHandoffId = plan.sourceHandoffId,
            sourceHandoffReviewId = plan.sourceHandoffReviewId,
            sourceStoryInputPackId = plan.sourceStoryInputPackId,
            checks = checks,
            sourceAlignmentSummary = alignment,
            adapterContractPlanReviewSummary = reviewSummary,
            reviewedContractRequirementsSummary = requirementsSummary,
            reviewedInputMappingSummary = inputMappingSummary,
            reviewedWorkItemsSummary = workItemsSummary,
            verificationReviewSummary = verificationSummary,
            boundarySummary = boundarySummary,
            boundaries = outputBoundaries(plan.boundaries.draftManifestGenerated),
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
        plan: LocalRendererManifestAdapterContractPlan,
        planFile: String,
        alignment: LocalRendererManifestAdapterContractPlanReviewSourceAlignmentSummary,
        reviewSummary: LocalRendererManifestAdapterContractPlanReviewSummary,
        requirementsSummary: LocalRendererManifestReviewedContractRequirementsSummary,
        inputMappingSummary: LocalRendererManifestReviewedInputMappingSummary,
        workItemsSummary: LocalRendererManifestReviewedWorkItemsSummary,
        verificationSummary: LocalRendererManifestAdapterContractVerificationReviewSummary,
        boundarySummary: LocalRendererManifestAdapterContractPlanReviewBoundarySummary,
        findings: MutableList<LocalRendererManifestAdapterContractPlanReviewFinding>,
    ): List<LocalRendererManifestAdapterContractPlanReviewCheck> =
        listOf(
            check(
                checkId = "explicit-source-plan",
                category = "source_validation",
                passed = planFile.isNotBlank() &&
                    plan.planId.isNotBlank() &&
                    plan.sourcePhase16DryRunId.isNotBlank() &&
                    plan.sourceStoryInputPackId.isNotBlank(),
                message = "Adapter contract plan review requires an explicit Phase 17 plan file.",
                evidence = listOf(
                    "planFile=$planFile",
                    "planId=${plan.planId}",
                    "sourcePhase16DryRunId=${plan.sourcePhase16DryRunId}",
                    "sourceStoryInputPackId=${plan.sourceStoryInputPackId}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_REVIEW_SOURCE_FILE_MISSING",
                path = "$.inputs",
                suggestedFix = "Pass --plan and --out explicitly.",
            ),
            check(
                checkId = "plan-state",
                category = "source_validation",
                passed = plan.verdict == "PASS" &&
                    plan.status == requiredPlanStatus &&
                    plan.downstreamBoundary == requiredPlanBoundary &&
                    plan.nextAllowedDownstreamStep == requiredPlanNextStep,
                message = "Adapter contract plan review requires PASS Phase 17 plan state.",
                evidence = listOf(
                    "verdict=${plan.verdict}",
                    "status=${plan.status}",
                    "downstreamBoundary=${plan.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${plan.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_REVIEW_PLAN_STATE_INVALID",
                path = "$.planState",
                suggestedFix = "Use the accepted PASS Phase 17 adapter contract plan.",
            ),
            check(
                checkId = "source-ids-present",
                category = "source_linkage",
                passed = alignment.sourceIdsPresent,
                message = "Adapter contract plan review requires non-empty source ids.",
                evidence = listOf(
                    "sourcePlanIdPresent=${alignment.sourcePlanIdPresent}",
                    "sourcePhase16DryRunIdPresent=${alignment.sourcePhase16DryRunIdPresent}",
                    "sourceDraftIdPresent=${alignment.sourceDraftIdPresent}",
                    "sourceReviewIdPresent=${alignment.sourceReviewIdPresent}",
                    "sourceStoryInputPackIdPresent=${alignment.sourceStoryInputPackIdPresent}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_REVIEW_SOURCE_IDS_MISSING",
                path = "$.sourceAlignmentSummary",
                suggestedFix = "Use a Phase 17 plan generated from complete Phase 16 source ids.",
            ),
            check(
                checkId = "plan-checks-pass",
                category = "review_gate",
                passed = plan.checks.isNotEmpty() &&
                    plan.checks.filter { it.requiredForPlan }.all { it.status == "PASS" } &&
                    plan.findingCount == 0 &&
                    plan.findings.isEmpty(),
                message = "Adapter contract plan review requires non-empty Phase 17 checks, all required checks PASS, and no findings.",
                evidence = listOf(
                    "sourceChecks=${plan.checks.size}",
                    "sourceRequiredFail=${plan.checks.count { it.requiredForPlan && it.status != "PASS" }}",
                    "sourceFindingCount=${plan.findingCount}",
                    "sourceFindings=${plan.findings.size}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_REVIEW_SOURCE_CHECKS_OR_FINDINGS_FAIL",
                path = "$.sourceChecks",
                suggestedFix = "Resolve Phase 17 checks and findings before review.",
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
                message = "Adapter contract plan review requires local-only no-media boundaries.",
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
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_REVIEW_BOUNDARY_FAIL",
                path = "$.boundaries",
                suggestedFix = "Use local-only JSON with no provider/API, renderer, TTS, media, adapter execution, production manifest, production approval, readiness claim, release, or production lock.",
            ),
            check(
                checkId = "contract-content-present",
                category = "plan_completeness",
                passed = requirementsSummary.requirementCount > 0 &&
                    requirementsSummary.requiredFieldCount > 0 &&
                    inputMappingSummary.mappingEntries > 0 &&
                    inputMappingSummary.requiredFieldCount > 0 &&
                    workItemsSummary.workItemCount > 0 &&
                    verificationSummary.verificationStepCount > 0,
                message = "Adapter contract plan review requires requirements, input mapping, work items, and verification steps.",
                evidence = listOf(
                    "requirementCount=${requirementsSummary.requirementCount}",
                    "requiredFieldCount=${requirementsSummary.requiredFieldCount}",
                    "mappingEntries=${inputMappingSummary.mappingEntries}",
                    "workItemCount=${workItemsSummary.workItemCount}",
                    "verificationStepCount=${verificationSummary.verificationStepCount}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_REVIEW_CONTENT_MISSING",
                path = "$.adapterContractPlan",
                suggestedFix = "Use a complete Phase 17 plan with requirements, mapping, work items, and verification plan.",
            ),
            check(
                checkId = "mapping-complete",
                category = "adapter_mapping",
                passed = reviewSummary.mappedPanelCount > 0 &&
                    reviewSummary.unmappedPanelCount == 0 &&
                    reviewSummary.missingRequiredFieldCount == 0 &&
                    inputMappingSummary.mappingEntries > 0 &&
                    inputMappingSummary.requiredFieldCount > 0 &&
                    inputMappingSummary.missingRequiredFieldCount == 0 &&
                    plan.adapterInputMappingPlan.entries.all {
                        it.panelId.isNotBlank() &&
                            it.plannedAdapterContractFields.isNotEmpty() &&
                            it.missingRequiredFieldCount == 0
                    },
                message = "Adapter contract plan review requires complete mapped panel contract fields.",
                evidence = listOf(
                    "mappedPanelCount=${reviewSummary.mappedPanelCount}",
                    "unmappedPanelCount=${reviewSummary.unmappedPanelCount}",
                    "missingRequiredFieldCount=${reviewSummary.missingRequiredFieldCount}",
                    "inputMappingEntries=${inputMappingSummary.mappingEntries}",
                    "inputMappingRequiredFieldCount=${inputMappingSummary.requiredFieldCount}",
                    "inputMappingMissingRequiredFieldCount=${inputMappingSummary.missingRequiredFieldCount}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_REVIEW_MAPPING_INCOMPLETE",
                path = "$.adapterInputMappingPlan",
                suggestedFix = "Resolve unmapped panels, missing fields, and empty planned adapter contract fields.",
            ),
            check(
                checkId = "review-only-boundary",
                category = "adapter_contract",
                passed = inputMappingSummary.planningOnly &&
                    !inputMappingSummary.adapterImplementationGenerated &&
                    !inputMappingSummary.adapterExecuted &&
                    inputMappingSummary.rendererInvocationCount == 0 &&
                    inputMappingSummary.mediaGeneratedCount == 0 &&
                    inputMappingSummary.productionManifestGeneratedCount == 0 &&
                    requirementsSummary.implementedNowCount == 0 &&
                    requirementsSummary.adapterExecutedCount == 0 &&
                    workItemsSummary.workItemCount > 0 &&
                    workItemsSummary.allRequireReviewGate &&
                    verificationSummary.verificationStepCount > 0 &&
                    verificationSummary.adapterExecutionChecks == 0 &&
                    verificationSummary.rendererExecutionChecks == 0 &&
                    verificationSummary.mediaGenerationChecks == 0 &&
                    verificationSummary.productionApprovalChecks == 0,
                message = "Adapter contract plan review is review only; no adapter implementation, execution, renderer, media, or production approval is allowed.",
                evidence = listOf(
                    "planningOnly=${inputMappingSummary.planningOnly}",
                    "adapterImplementationGenerated=${inputMappingSummary.adapterImplementationGenerated}",
                    "adapterExecuted=${inputMappingSummary.adapterExecuted}",
                    "workItemCount=${workItemsSummary.workItemCount}",
                    "verificationStepCount=${verificationSummary.verificationStepCount}",
                    "adapter contract",
                    "no media generation",
                    "ComicVideoRenderHandoffManifest generation=false",
                    "production lock=false",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_REVIEW_EXECUTION_FORBIDDEN",
                path = "$.adapterContractPlanReview",
                suggestedFix = "Keep Phase 18 as review only.",
            ),
        )

    private fun sourceAlignmentSummary(
        plan: LocalRendererManifestAdapterContractPlan,
    ): LocalRendererManifestAdapterContractPlanReviewSourceAlignmentSummary {
        val present = listOf(
            plan.planId,
            plan.sourcePhase16DryRunId,
            plan.sourceDraftId,
            plan.sourceReviewId,
            plan.sourceManifestDraftPlanId,
            plan.sourceManifestDraftPlanReviewId,
            plan.sourceImplementationDraftId,
            plan.sourceImplementationDraftReviewId,
            plan.sourcePlanId,
            plan.sourcePlanReviewId,
            plan.sourceLocalRendererAdapterDryRunId,
            plan.sourceHandoffId,
            plan.sourceHandoffReviewId,
            plan.sourceStoryInputPackId,
        ).all { it.isNotBlank() }
        return LocalRendererManifestAdapterContractPlanReviewSourceAlignmentSummary(
            sourceIdsPresent = present,
            sourcePlanIdPresent = plan.planId.isNotBlank(),
            sourcePhase16DryRunIdPresent = plan.sourcePhase16DryRunId.isNotBlank(),
            sourceDraftIdPresent = plan.sourceDraftId.isNotBlank(),
            sourceReviewIdPresent = plan.sourceReviewId.isNotBlank(),
            sourceStoryInputPackIdPresent = plan.sourceStoryInputPackId.isNotBlank(),
            sourcePlanId = plan.planId,
            sourcePhase16DryRunId = plan.sourcePhase16DryRunId,
            sourceDraftId = plan.sourceDraftId,
            sourceReviewId = plan.sourceReviewId,
            sourceManifestDraftPlanId = plan.sourceManifestDraftPlanId,
            sourceManifestDraftPlanReviewId = plan.sourceManifestDraftPlanReviewId,
            sourceImplementationDraftId = plan.sourceImplementationDraftId,
            sourceImplementationDraftReviewId = plan.sourceImplementationDraftReviewId,
            sourceAdapterImplementationPlanId = plan.sourcePlanId,
            sourceAdapterImplementationPlanReviewId = plan.sourcePlanReviewId,
            sourceLocalRendererAdapterDryRunId = plan.sourceLocalRendererAdapterDryRunId,
            sourceHandoffId = plan.sourceHandoffId,
            sourceHandoffReviewId = plan.sourceHandoffReviewId,
            sourceStoryInputPackId = plan.sourceStoryInputPackId,
        )
    }

    private fun reviewSummary(
        plan: LocalRendererManifestAdapterContractPlan,
        requirementsSummary: LocalRendererManifestReviewedContractRequirementsSummary,
        inputMappingSummary: LocalRendererManifestReviewedInputMappingSummary,
        workItemsSummary: LocalRendererManifestReviewedWorkItemsSummary,
        verificationSummary: LocalRendererManifestAdapterContractVerificationReviewSummary,
    ): LocalRendererManifestAdapterContractPlanReviewSummary =
        LocalRendererManifestAdapterContractPlanReviewSummary(
            sourceChecks = plan.checks.size,
            sourceChecksPass = plan.checks.count { it.status == "PASS" },
            sourceChecksFail = plan.checks.count { it.status != "PASS" },
            sourceFindingCount = plan.findingCount,
            adapterContractRequirements = requirementsSummary.requirementCount,
            adapterInputMappingEntries = inputMappingSummary.mappingEntries,
            requiredAdapterContractWorkItems = workItemsSummary.workItemCount,
            verificationSteps = verificationSummary.verificationStepCount,
            mappedPanelCount = plan.adapterContractPlanSummary.mappedPanelCount,
            unmappedPanelCount = plan.adapterContractPlanSummary.unmappedPanelCount,
            missingRequiredFieldCount = plan.adapterContractPlanSummary.missingRequiredFieldCount,
            totalDraftFields = plan.adapterContractPlanSummary.totalDraftFields,
            reviewOnly = true,
            adapterImplementationExecuted = plan.boundaries.adapterImplementationExecuted,
            adapterExecuted = plan.boundaries.adapterExecuted,
            rendererReady = plan.boundaries.rendererReady,
            mp4ReadinessClaimed = plan.boundaries.mp4ReadinessClaimed,
        )

    private fun requirementsSummary(
        plan: LocalRendererManifestAdapterContractPlan,
    ): LocalRendererManifestReviewedContractRequirementsSummary =
        LocalRendererManifestReviewedContractRequirementsSummary(
            requirementCount = plan.adapterContractRequirements.size,
            requiredFieldCount = plan.adapterContractRequirements.sumOf { it.requiredFields.size },
            implementedNowCount = plan.adapterContractRequirements.count { it.implementedNow },
            adapterExecutedCount = plan.adapterContractRequirements.count { it.adapterExecuted },
            requirementIds = plan.adapterContractRequirements.map { it.requirementId },
        )

    private fun inputMappingSummary(
        plan: LocalRendererManifestAdapterContractPlan,
    ): LocalRendererManifestReviewedInputMappingSummary =
        LocalRendererManifestReviewedInputMappingSummary(
            planningOnly = plan.adapterInputMappingPlan.planningOnly,
            mappingEntries = plan.adapterInputMappingPlan.entries.size,
            mappedPanelCount = plan.adapterInputMappingPlan.entries.count { it.missingRequiredFieldCount == 0 },
            requiredFieldCount = plan.adapterInputMappingPlan.entries.sumOf { it.requiredFieldCount },
            missingRequiredFieldCount = plan.adapterInputMappingPlan.entries.sumOf { it.missingRequiredFieldCount },
            adapterImplementationGenerated = plan.adapterInputMappingPlan.adapterImplementationGenerated ||
                plan.adapterInputMappingPlan.entries.any { it.adapterImplementationGenerated },
            adapterExecuted = plan.adapterInputMappingPlan.adapterExecuted ||
                plan.adapterInputMappingPlan.entries.any { it.adapterExecuted },
            rendererInvocationCount = plan.adapterInputMappingPlan.entries.count { it.rendererInvocation != "none" },
            mediaGeneratedCount = plan.adapterInputMappingPlan.entries.count { it.mediaGenerated },
            productionManifestGeneratedCount = plan.adapterInputMappingPlan.entries.count {
                it.productionManifestGenerated
            },
        )

    private fun workItemsSummary(
        plan: LocalRendererManifestAdapterContractPlan,
    ): LocalRendererManifestReviewedWorkItemsSummary =
        LocalRendererManifestReviewedWorkItemsSummary(
            workItemCount = plan.requiredAdapterContractWorkItems.size,
            mustNotDoItemCount = plan.requiredAdapterContractWorkItems.sumOf { it.mustNotDo.size },
            allRequireReviewGate = plan.requiredAdapterContractWorkItems.isNotEmpty() &&
                plan.requiredAdapterContractWorkItems.all {
                    it.reviewGateRequiredBeforeImplementation == requiredPlanNextStep
                },
        )

    private fun verificationSummary(
        plan: LocalRendererManifestAdapterContractPlan,
    ): LocalRendererManifestAdapterContractVerificationReviewSummary =
        LocalRendererManifestAdapterContractVerificationReviewSummary(
            verificationStepCount = plan.verificationPlan.size,
            adapterExecutionChecks = plan.verificationPlan.count { it.requiresAdapterExecution },
            rendererExecutionChecks = plan.verificationPlan.count { it.requiresRendererExecution },
            mediaGenerationChecks = plan.verificationPlan.count { it.requiresMediaGeneration },
            productionApprovalChecks = plan.verificationPlan.count { it.requiresProductionManifestApproval },
        )

    private fun boundarySummary(
        plan: LocalRendererManifestAdapterContractPlan,
    ): LocalRendererManifestAdapterContractPlanReviewBoundarySummary =
        LocalRendererManifestAdapterContractPlanReviewBoundarySummary(
            sourceLocalOnly = localBoundariesOk(plan.boundaries),
            mediaGenerated = plan.boundaries.mediaGenerated,
            rendererInvocation = plan.boundaries.rendererInvocation,
            providerInvocation = plan.boundaries.providerInvocation,
            ttsInvocation = plan.boundaries.ttsInvocation,
            adapterImplementationExecuted = plan.boundaries.adapterImplementationExecuted,
            adapterExecuted = plan.boundaries.adapterExecuted,
            draftManifestGenerated = plan.boundaries.draftManifestGenerated,
            productionManifestGenerated = plan.boundaries.productionManifestGenerated,
            productionReady = plan.boundaries.productionReady,
            productionApproved = plan.boundaries.productionApproved,
            finalMediaApproval = plan.boundaries.finalMediaApproval,
            providerMediaHandoff = plan.boundaries.providerMediaHandoff,
            release = plan.boundaries.release,
            canonRegistryOrProductionLock = plan.boundaries.canonRegistryOrProductionLock,
            rendererReady = plan.boundaries.rendererReady,
            mp4ReadinessClaimed = plan.boundaries.mp4ReadinessClaimed,
        )

    private fun outputBoundaries(draftManifestGenerated: Boolean): LocalRendererManifestAdapterContractPlanReviewBoundaries =
        LocalRendererManifestAdapterContractPlanReviewBoundaries(
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

    private fun localBoundariesOk(boundaries: LocalRendererManifestAdapterContractPlanBoundaries): Boolean =
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

    private fun check(
        checkId: String,
        category: String,
        passed: Boolean,
        message: String,
        evidence: List<String>,
        findings: MutableList<LocalRendererManifestAdapterContractPlanReviewFinding>,
        ruleId: String,
        path: String,
        suggestedFix: String,
    ): LocalRendererManifestAdapterContractPlanReviewCheck {
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
        return LocalRendererManifestAdapterContractPlanReviewCheck(
            checkId = checkId,
            status = if (passed) "PASS" else "FAIL",
            category = category,
            message = message,
            evidence = evidence,
        )
    }

    private fun List<LocalRendererManifestAdapterContractPlanReviewFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): LocalRendererManifestAdapterContractPlanReviewFinding =
        LocalRendererManifestAdapterContractPlanReviewFinding(
            ruleId,
            category,
            severity,
            path,
            message,
            evidence,
            suggestedFix,
        )
}
