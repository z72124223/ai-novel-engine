package com.localnovel.engine.service

import com.localnovel.engine.model.LocalRendererManifestAdapterContractDraft
import com.localnovel.engine.model.LocalRendererManifestAdapterContractDraftBoundaries
import com.localnovel.engine.model.LocalRendererManifestAdapterContractDraftBoundarySummary
import com.localnovel.engine.model.LocalRendererManifestAdapterContractDraftCheck
import com.localnovel.engine.model.LocalRendererManifestAdapterContractDraftFailStopRule
import com.localnovel.engine.model.LocalRendererManifestAdapterContractDraftFieldMapping
import com.localnovel.engine.model.LocalRendererManifestAdapterContractDraftFinding
import com.localnovel.engine.model.LocalRendererManifestAdapterContractDraftObligation
import com.localnovel.engine.model.LocalRendererManifestAdapterContractDraftRendererProhibition
import com.localnovel.engine.model.LocalRendererManifestAdapterContractDraftRequiredInput
import com.localnovel.engine.model.LocalRendererManifestAdapterContractDraftSourceAlignmentSummary
import com.localnovel.engine.model.LocalRendererManifestAdapterContractDraftSummary
import com.localnovel.engine.model.LocalRendererManifestAdapterContractDraftVerificationRequirement
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlan
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanBoundaries
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanReviewBoundaries
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanReviewReport
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class LocalRendererManifestAdapterContractDraftService {
    private val requiredPlanStatus = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN"
    private val requiredPlanBoundary = "local_renderer_manifest_adapter_contract_plan_only_not_adapter_implementation"
    private val requiredPlanNextStep = "local_renderer_manifest_adapter_contract_plan_review"
    private val requiredReviewStatus = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_REVIEW_PASS"
    private val requiredReviewBoundary = "local_renderer_manifest_adapter_contract_plan_review_only_not_adapter_implementation"
    private val requiredReviewNextStep = "local_renderer_manifest_adapter_contract_draft"
    private val passStatus = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_DRAFT"
    private val failStatus = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_DRAFT_FAIL"
    private val downstreamBoundary = "local_renderer_manifest_adapter_contract_draft_only_not_adapter_execution"
    private val nextAllowedDownstreamStep = "local_renderer_manifest_adapter_contract_draft_review"
    private val requiredBoundaryFlags = listOf(
        "contractDraftGenerated=true",
        "mediaGenerated=false",
        "rendererInvocation=none",
        "providerInvocation=none",
        "ttsInvocation=none",
        "adapterImplementationExecuted=false",
        "adapterExecuted=false",
        "noExternalPaidProviderTools=true",
        "rendererReady=false",
        "mp4ReadinessClaimed=false",
        "productionManifestGenerated=false",
        "productionReady=false",
        "productionApproved=false",
    )

    fun draftFile(
        planPath: Path,
        planReviewPath: Path,
        outPath: Path? = null,
        draftId: String = "",
    ): LocalRendererManifestAdapterContractDraft {
        val plan = JsonSupport.json.decodeFromString<LocalRendererManifestAdapterContractPlan>(
            Files.readString(planPath).removePrefix("\uFEFF"),
        )
        val review = JsonSupport.json.decodeFromString<LocalRendererManifestAdapterContractPlanReviewReport>(
            Files.readString(planReviewPath).removePrefix("\uFEFF"),
        )
        val draft = draft(
            plan = plan,
            review = review,
            planFile = planPath.toString(),
            planReviewFile = planReviewPath.toString(),
            draftId = draftId,
        )
        if (outPath != null) {
            outPath.parent?.let { Files.createDirectories(it) }
            Files.writeString(outPath, JsonSupport.json.encodeToString(draft))
        }
        return draft
    }

    fun draft(
        plan: LocalRendererManifestAdapterContractPlan,
        review: LocalRendererManifestAdapterContractPlanReviewReport,
        planFile: String = "",
        planReviewFile: String = "",
        draftId: String = "",
    ): LocalRendererManifestAdapterContractDraft {
        val findings = mutableListOf<LocalRendererManifestAdapterContractDraftFinding>()
        val alignment = sourceAlignmentSummary(plan, review)
        val requiredInputs = requiredInputs(planFile, planReviewFile)
        val normalizedFieldMapping = normalizedFieldMapping(plan)
        val adapterObligations = adapterObligations(plan)
        val failStopRules = failStopRules(plan, review)
        val rendererProhibition = LocalRendererManifestAdapterContractDraftRendererProhibition()
        val verificationRequirements = verificationRequirements(plan)
        val knownNonGoals = knownNonGoals()
        val boundarySummary = boundarySummary(plan, review)
        val summary = draftSummary(
            plan = plan,
            review = review,
            requiredInputs = requiredInputs,
            normalizedFieldMapping = normalizedFieldMapping,
            adapterObligations = adapterObligations,
            failStopRules = failStopRules,
            verificationRequirements = verificationRequirements,
            knownNonGoals = knownNonGoals,
        )
        val checks = buildChecks(
            plan = plan,
            review = review,
            planFile = planFile,
            planReviewFile = planReviewFile,
            alignment = alignment,
            summary = summary,
            requiredInputs = requiredInputs,
            normalizedFieldMapping = normalizedFieldMapping,
            adapterObligations = adapterObligations,
            failStopRules = failStopRules,
            verificationRequirements = verificationRequirements,
            knownNonGoals = knownNonGoals,
            boundarySummary = boundarySummary,
            findings = findings,
        )
        val verdict = if (findings.hasBlocking()) "FAIL" else "PASS"
        val resolvedDraftId = draftId.ifBlank {
            plan.planId.takeIf { it.isNotBlank() }?.replace(
                "adapter-contract-plan",
                "adapter-contract-draft",
            ) ?: ""
        }

        return LocalRendererManifestAdapterContractDraft(
            verdict = verdict,
            status = if (verdict == "PASS") passStatus else failStatus,
            draftId = resolvedDraftId,
            sourcePlanFile = planFile,
            sourcePlanReviewFile = planReviewFile,
            sourcePlanId = plan.planId,
            sourcePlanReviewId = review.reviewId,
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
            contractDraftSummary = summary,
            requiredInputs = requiredInputs,
            normalizedFieldMapping = normalizedFieldMapping,
            adapterObligations = adapterObligations,
            failStopRules = failStopRules,
            rendererInvocationProhibition = rendererProhibition,
            verificationRequirements = verificationRequirements,
            knownNonGoals = knownNonGoals,
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
        plan: LocalRendererManifestAdapterContractPlan,
        review: LocalRendererManifestAdapterContractPlanReviewReport,
        planFile: String,
        planReviewFile: String,
        alignment: LocalRendererManifestAdapterContractDraftSourceAlignmentSummary,
        summary: LocalRendererManifestAdapterContractDraftSummary,
        requiredInputs: List<LocalRendererManifestAdapterContractDraftRequiredInput>,
        normalizedFieldMapping: List<LocalRendererManifestAdapterContractDraftFieldMapping>,
        adapterObligations: List<LocalRendererManifestAdapterContractDraftObligation>,
        failStopRules: List<LocalRendererManifestAdapterContractDraftFailStopRule>,
        verificationRequirements: List<LocalRendererManifestAdapterContractDraftVerificationRequirement>,
        knownNonGoals: List<String>,
        boundarySummary: LocalRendererManifestAdapterContractDraftBoundarySummary,
        findings: MutableList<LocalRendererManifestAdapterContractDraftFinding>,
    ): List<LocalRendererManifestAdapterContractDraftCheck> =
        listOf(
            check(
                checkId = "explicit-source-files",
                category = "source_validation",
                passed = planFile.isNotBlank() &&
                    planReviewFile.isNotBlank() &&
                    plan.planId.isNotBlank() &&
                    review.reviewId.isNotBlank(),
                message = "Adapter contract draft requires explicit Phase 17 plan and Phase 18 review files.",
                evidence = listOf(
                    "planFile=$planFile",
                    "planReviewFile=$planReviewFile",
                    "planId=${plan.planId}",
                    "reviewId=${review.reviewId}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_DRAFT_SOURCE_FILES_MISSING",
                path = "$.inputs",
                suggestedFix = "Pass --plan, --plan-review, and --out explicitly.",
            ),
            check(
                checkId = "plan-state",
                category = "source_validation",
                passed = plan.verdict == "PASS" &&
                    plan.status == requiredPlanStatus &&
                    plan.downstreamBoundary == requiredPlanBoundary &&
                    plan.nextAllowedDownstreamStep == requiredPlanNextStep,
                message = "Adapter contract draft requires PASS Phase 17 plan state.",
                evidence = listOf(
                    "verdict=${plan.verdict}",
                    "status=${plan.status}",
                    "downstreamBoundary=${plan.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${plan.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_DRAFT_PLAN_STATE_INVALID",
                path = "$.planState",
                suggestedFix = "Use the accepted PASS Phase 17 adapter contract plan.",
            ),
            check(
                checkId = "plan-review-state",
                category = "source_validation",
                passed = review.verdict == "PASS" &&
                    review.status == requiredReviewStatus &&
                    review.downstreamBoundary == requiredReviewBoundary &&
                    review.nextAllowedDownstreamStep == requiredReviewNextStep,
                message = "Adapter contract draft requires PASS Phase 18 review state.",
                evidence = listOf(
                    "verdict=${review.verdict}",
                    "status=${review.status}",
                    "downstreamBoundary=${review.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${review.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_DRAFT_REVIEW_STATE_INVALID",
                path = "$.planReviewState",
                suggestedFix = "Use the accepted PASS Phase 18 adapter contract plan review report.",
            ),
            check(
                checkId = "source-alignment",
                category = "source_linkage",
                passed = alignment.sourceIdsPresent &&
                    alignment.planReviewIdsMatch &&
                    alignment.phase16DryRunIdsMatch &&
                    alignment.draftIdsMatch &&
                    alignment.reviewIdsMatch &&
                    alignment.storyInputPackIdsMatch,
                message = "Adapter contract draft requires Phase 17 and Phase 18 source ids to align.",
                evidence = listOf(
                    "sourceIdsPresent=${alignment.sourceIdsPresent}",
                    "planReviewIdsMatch=${alignment.planReviewIdsMatch}",
                    "phase16DryRunIdsMatch=${alignment.phase16DryRunIdsMatch}",
                    "draftIdsMatch=${alignment.draftIdsMatch}",
                    "reviewIdsMatch=${alignment.reviewIdsMatch}",
                    "storyInputPackIdsMatch=${alignment.storyInputPackIdsMatch}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_DRAFT_SOURCE_ALIGNMENT_FAIL",
                path = "$.sourceAlignmentSummary",
                suggestedFix = "Regenerate or select matching Phase 17/18 source files.",
            ),
            check(
                checkId = "source-checks-pass",
                category = "review_gate",
                passed = plan.checks.isNotEmpty() &&
                    review.checks.isNotEmpty() &&
                    plan.checks.filter { it.requiredForPlan }.all { it.status == "PASS" } &&
                    review.checks.filter { it.requiredForReview }.all { it.status == "PASS" } &&
                    plan.findingCount == 0 &&
                    review.findingCount == 0 &&
                    plan.findings.isEmpty() &&
                    review.findings.isEmpty(),
                message = "Adapter contract draft requires non-empty PASS source checks and no findings.",
                evidence = listOf(
                    "planChecks=${plan.checks.size}",
                    "reviewChecks=${review.checks.size}",
                    "planRequiredFail=${plan.checks.count { it.requiredForPlan && it.status != "PASS" }}",
                    "reviewRequiredFail=${review.checks.count { it.requiredForReview && it.status != "PASS" }}",
                    "planFindingCount=${plan.findingCount}",
                    "reviewFindingCount=${review.findingCount}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_DRAFT_SOURCE_CHECKS_OR_FINDINGS_FAIL",
                path = "$.sourceChecks",
                suggestedFix = "Resolve Phase 17/18 checks and findings before contract drafting.",
            ),
            check(
                checkId = "local-boundaries",
                category = "boundary",
                passed = boundarySummary.planLocalOnly &&
                    boundarySummary.reviewLocalOnly &&
                    !boundarySummary.mediaGenerated &&
                    boundarySummary.rendererInvocation == "none" &&
                    boundarySummary.providerInvocation == "none" &&
                    boundarySummary.ttsInvocation == "none" &&
                    !boundarySummary.adapterImplementationExecuted &&
                    !boundarySummary.adapterExecuted &&
                    boundarySummary.contractDraftGenerated &&
                    !boundarySummary.productionManifestGenerated &&
                    !boundarySummary.productionReady &&
                    !boundarySummary.productionApproved &&
                    !boundarySummary.finalMediaApproval &&
                    !boundarySummary.providerMediaHandoff &&
                    !boundarySummary.release &&
                    !boundarySummary.canonRegistryOrProductionLock &&
                    !boundarySummary.rendererReady &&
                    !boundarySummary.mp4ReadinessClaimed,
                message = "Adapter contract draft requires local-only no-media boundaries.",
                evidence = listOf(
                    "planLocalOnly=${boundarySummary.planLocalOnly}",
                    "reviewLocalOnly=${boundarySummary.reviewLocalOnly}",
                    "contractDraftGenerated=${boundarySummary.contractDraftGenerated}",
                    "mediaGenerated=${boundarySummary.mediaGenerated}",
                    "rendererInvocation=${boundarySummary.rendererInvocation}",
                    "providerInvocation=${boundarySummary.providerInvocation}",
                    "ttsInvocation=${boundarySummary.ttsInvocation}",
                    "adapterImplementationExecuted=${boundarySummary.adapterImplementationExecuted}",
                    "adapterExecuted=${boundarySummary.adapterExecuted}",
                    "productionManifestGenerated=${boundarySummary.productionManifestGenerated}",
                    "productionReady=${boundarySummary.productionReady}",
                    "productionApproved=${boundarySummary.productionApproved}",
                    "rendererReady=${boundarySummary.rendererReady}",
                    "mp4ReadinessClaimed=${boundarySummary.mp4ReadinessClaimed}",
                ) + requiredBoundaryFlags,
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_DRAFT_BOUNDARY_FAIL",
                path = "$.boundaries",
                suggestedFix = "Use local-only JSON with no provider/API, renderer, TTS, media, adapter execution, production manifest, production approval, readiness claim, release, or production lock.",
            ),
            check(
                checkId = "contract-sections-present",
                category = "draft_completeness",
                passed = requiredInputs.isNotEmpty() &&
                    normalizedFieldMapping.isNotEmpty() &&
                    adapterObligations.isNotEmpty() &&
                    failStopRules.isNotEmpty() &&
                    verificationRequirements.isNotEmpty() &&
                    knownNonGoals.isNotEmpty(),
                message = "Adapter contract draft requires required inputs, field mapping, obligations, fail-stop rules, verification, and non-goals.",
                evidence = listOf(
                    "requiredInputs=${requiredInputs.size}",
                    "normalizedFieldMapping=${normalizedFieldMapping.size}",
                    "adapterObligations=${adapterObligations.size}",
                    "failStopRules=${failStopRules.size}",
                    "verificationRequirements=${verificationRequirements.size}",
                    "knownNonGoals=${knownNonGoals.size}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_DRAFT_SECTIONS_MISSING",
                path = "$.contractDraft",
                suggestedFix = "Create a complete contract draft from the accepted Phase 17 plan and Phase 18 review.",
            ),
            check(
                checkId = "field-mapping-normalized",
                category = "adapter_mapping",
                passed = summary.normalizedFieldMappings > 0 &&
                    summary.mappedPanelCount > 0 &&
                    summary.unmappedPanelCount == 0 &&
                    summary.missingRequiredFieldCount == 0 &&
                    normalizedFieldMapping.all {
                        it.panelId.isNotBlank() &&
                            it.normalizedContractFields.isNotEmpty() &&
                            it.missingRequiredFieldCount == 0
                    },
                message = "Adapter contract draft requires normalized field mappings for every mapped panel.",
                evidence = listOf(
                    "normalizedFieldMappings=${summary.normalizedFieldMappings}",
                    "mappedPanelCount=${summary.mappedPanelCount}",
                    "unmappedPanelCount=${summary.unmappedPanelCount}",
                    "missingRequiredFieldCount=${summary.missingRequiredFieldCount}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_DRAFT_MAPPING_INCOMPLETE",
                path = "$.normalizedFieldMapping",
                suggestedFix = "Resolve unmapped panels and missing normalized contract fields.",
            ),
            check(
                checkId = "draft-only-boundary",
                category = "adapter_contract",
                passed = summary.contractDraftGenerated &&
                    !summary.adapterImplementationExecuted &&
                    !summary.adapterExecuted &&
                    !summary.rendererReady &&
                    !summary.mp4ReadinessClaimed &&
                    !rendererInvocationRequested(verificationRequirements),
                message = "Adapter contract draft is draft-only; no adapter implementation, execution, renderer, media, or production approval is allowed.",
                evidence = listOf(
                    "contractDraftGenerated=${summary.contractDraftGenerated}",
                    "adapterImplementationExecuted=${summary.adapterImplementationExecuted}",
                    "adapterExecuted=${summary.adapterExecuted}",
                    "rendererReady=${summary.rendererReady}",
                    "mp4ReadinessClaimed=${summary.mp4ReadinessClaimed}",
                    "adapter contract",
                    "no media generation",
                    "ComicVideoRenderHandoffManifest generation=false",
                    "production lock=false",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_DRAFT_EXECUTION_FORBIDDEN",
                path = "$.contractDraftBoundary",
                suggestedFix = "Keep Phase 19 as contract draft only.",
            ),
        )

    private fun sourceAlignmentSummary(
        plan: LocalRendererManifestAdapterContractPlan,
        review: LocalRendererManifestAdapterContractPlanReviewReport,
    ): LocalRendererManifestAdapterContractDraftSourceAlignmentSummary {
        val present = listOf(
            plan.planId,
            review.reviewId,
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
        return LocalRendererManifestAdapterContractDraftSourceAlignmentSummary(
            sourceIdsPresent = present,
            planReviewIdsMatch = plan.planId.isNotBlank() &&
                review.reviewId.isNotBlank() &&
                review.sourcePlanId == plan.planId,
            phase16DryRunIdsMatch = plan.sourcePhase16DryRunId.isNotBlank() &&
                review.sourcePhase16DryRunId == plan.sourcePhase16DryRunId,
            draftIdsMatch = plan.sourceDraftId.isNotBlank() &&
                review.sourceDraftId == plan.sourceDraftId,
            reviewIdsMatch = plan.sourceReviewId.isNotBlank() &&
                review.sourceReviewId == plan.sourceReviewId,
            storyInputPackIdsMatch = plan.sourceStoryInputPackId.isNotBlank() &&
                review.sourceStoryInputPackId == plan.sourceStoryInputPackId,
            sourcePlanId = plan.planId,
            sourcePlanReviewId = review.reviewId,
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

    private fun requiredInputs(
        planFile: String,
        reviewFile: String,
    ): List<LocalRendererManifestAdapterContractDraftRequiredInput> =
        listOf(
            LocalRendererManifestAdapterContractDraftRequiredInput(
                inputId = "phase17-adapter-contract-plan",
                description = "Accepted Phase 17 adapter contract plan JSON.",
                sourcePath = planFile,
                requiredStatus = requiredPlanStatus,
                requiredDownstreamBoundary = requiredPlanBoundary,
                requiredNextStep = requiredPlanNextStep,
            ),
            LocalRendererManifestAdapterContractDraftRequiredInput(
                inputId = "phase18-adapter-contract-plan-review",
                description = "Accepted Phase 18 adapter contract plan review JSON.",
                sourcePath = reviewFile,
                requiredStatus = requiredReviewStatus,
                requiredDownstreamBoundary = requiredReviewBoundary,
                requiredNextStep = requiredReviewNextStep,
            ),
        )

    private fun normalizedFieldMapping(
        plan: LocalRendererManifestAdapterContractPlan,
    ): List<LocalRendererManifestAdapterContractDraftFieldMapping> =
        plan.adapterInputMappingPlan.entries.map {
            LocalRendererManifestAdapterContractDraftFieldMapping(
                order = it.order,
                panelId = it.panelId,
                sourceSceneId = it.sourceSceneId,
                sourceBeatId = it.sourceBeatId,
                startMillis = it.startMillis,
                endMillis = it.endMillis,
                durationMillis = it.durationMillis,
                normalizedContractFields = it.plannedAdapterContractFields.distinct(),
                sourceMappedDraftFieldTargets = it.sourceMappedDraftFieldTargets,
                requiredFieldCount = it.requiredFieldCount,
                missingRequiredFieldCount = it.missingRequiredFieldCount,
                contractDraftOnly = true,
                adapterImplementationExecuted = false,
                adapterExecuted = false,
                mediaGenerated = false,
                productionManifestGenerated = false,
            )
        }

    private fun adapterObligations(
        plan: LocalRendererManifestAdapterContractPlan,
    ): List<LocalRendererManifestAdapterContractDraftObligation> {
        val requirementIds = plan.adapterContractRequirements.map { it.requirementId }
        return plan.requiredAdapterContractWorkItems.map {
            LocalRendererManifestAdapterContractDraftObligation(
                obligationId = it.workItemId,
                title = it.title,
                description = it.description,
                sourceRequirementIds = requirementIds,
                mustPreserve = listOf(
                    it.targetOutcome,
                    "local file-protocol JSON only",
                    "review gate before adapter implementation",
                ).filter { value -> value.isNotBlank() },
                mustNotDo = it.mustNotDo,
                adapterImplementationExecuted = false,
                adapterExecuted = false,
            )
        }
    }

    private fun failStopRules(
        plan: LocalRendererManifestAdapterContractPlan,
        review: LocalRendererManifestAdapterContractPlanReviewReport,
    ): List<LocalRendererManifestAdapterContractDraftFailStopRule> =
        listOf(
            LocalRendererManifestAdapterContractDraftFailStopRule(
                ruleId = "phase17-plan-must-pass",
                description = "Stop if the Phase 17 adapter contract plan is not PASS.",
                failureMode = "planVerdict=${plan.verdict}; planStatus=${plan.status}",
            ),
            LocalRendererManifestAdapterContractDraftFailStopRule(
                ruleId = "phase18-review-must-pass",
                description = "Stop if the Phase 18 adapter contract plan review is not PASS.",
                failureMode = "reviewVerdict=${review.verdict}; reviewStatus=${review.status}",
            ),
            LocalRendererManifestAdapterContractDraftFailStopRule(
                ruleId = "source-id-alignment-required",
                description = "Stop if the plan and review source ids do not match.",
                failureMode = "source alignment mismatch",
            ),
            LocalRendererManifestAdapterContractDraftFailStopRule(
                ruleId = "local-boundary-overclaim",
                description = "Stop if any source or output boundary claims media, renderer, provider, adapter execution, production approval, release, canon, registry, or production lock.",
                failureMode = "boundary overclaim",
                evidence = requiredBoundaryFlags,
            ),
            LocalRendererManifestAdapterContractDraftFailStopRule(
                ruleId = "empty-contract-content",
                description = "Stop if required inputs, normalized mappings, obligations, fail-stop rules, verification requirements, or non-goals are missing.",
                failureMode = "draft section missing",
            ),
            LocalRendererManifestAdapterContractDraftFailStopRule(
                ruleId = "adapter-renderer-media-execution-request",
                description = "Stop if the draft asks to execute an adapter, renderer, ffmpeg, TTS, media generation, provider/API, or production manifest approval.",
                failureMode = "forbidden execution request",
            ),
        )

    private fun verificationRequirements(
        plan: LocalRendererManifestAdapterContractPlan,
    ): List<LocalRendererManifestAdapterContractDraftVerificationRequirement> =
        plan.verificationPlan.map {
            LocalRendererManifestAdapterContractDraftVerificationRequirement(
                verificationId = it.verificationId,
                commandOrCheck = it.commandOrCheck,
                expectedResult = it.expectedResult,
                requiresAdapterExecution = it.requiresAdapterExecution,
                requiresRendererExecution = it.requiresRendererExecution,
                requiresMediaGeneration = it.requiresMediaGeneration,
                requiresProductionManifestApproval = it.requiresProductionManifestApproval,
            )
        }

    private fun knownNonGoals(): List<String> =
        listOf(
            "no adapter implementation",
            "no adapter execution",
            "no renderer/ffmpeg/TTS execution",
            "no media generation or final video",
            "no provider/API calls",
            "no real production ComicVideoRenderHandoffManifest generation or approval",
            "no final media approval",
            "no provider/media handoff",
            "no canon/registry/release/approval/production lock",
            "no renderer readiness, MP4 readiness, or final video readiness claim",
        )

    private fun draftSummary(
        plan: LocalRendererManifestAdapterContractPlan,
        review: LocalRendererManifestAdapterContractPlanReviewReport,
        requiredInputs: List<LocalRendererManifestAdapterContractDraftRequiredInput>,
        normalizedFieldMapping: List<LocalRendererManifestAdapterContractDraftFieldMapping>,
        adapterObligations: List<LocalRendererManifestAdapterContractDraftObligation>,
        failStopRules: List<LocalRendererManifestAdapterContractDraftFailStopRule>,
        verificationRequirements: List<LocalRendererManifestAdapterContractDraftVerificationRequirement>,
        knownNonGoals: List<String>,
    ): LocalRendererManifestAdapterContractDraftSummary =
        LocalRendererManifestAdapterContractDraftSummary(
            sourcePlanChecks = plan.checks.size,
            sourcePlanReviewChecks = review.checks.size,
            sourcePlanFindings = plan.findingCount,
            sourcePlanReviewFindings = review.findingCount,
            requiredInputs = requiredInputs.size,
            normalizedFieldMappings = normalizedFieldMapping.size,
            adapterObligations = adapterObligations.size,
            failStopRules = failStopRules.size,
            verificationRequirements = verificationRequirements.size,
            knownNonGoals = knownNonGoals.size,
            mappedPanelCount = plan.adapterContractPlanSummary.mappedPanelCount,
            unmappedPanelCount = plan.adapterContractPlanSummary.unmappedPanelCount,
            missingRequiredFieldCount = plan.adapterContractPlanSummary.missingRequiredFieldCount,
            totalDraftFields = plan.adapterContractPlanSummary.totalDraftFields,
            contractDraftGenerated = true,
            adapterImplementationExecuted = false,
            adapterExecuted = false,
            rendererReady = false,
            mp4ReadinessClaimed = false,
        )

    private fun boundarySummary(
        plan: LocalRendererManifestAdapterContractPlan,
        review: LocalRendererManifestAdapterContractPlanReviewReport,
    ): LocalRendererManifestAdapterContractDraftBoundarySummary =
        LocalRendererManifestAdapterContractDraftBoundarySummary(
            planLocalOnly = localBoundariesOk(plan.boundaries),
            reviewLocalOnly = localBoundariesOk(review.boundaries),
            mediaGenerated = plan.boundaries.mediaGenerated || review.boundaries.mediaGenerated,
            rendererInvocation = listOf(plan.boundaries.rendererInvocation, review.boundaries.rendererInvocation)
                .firstOrNull { it != "none" } ?: "none",
            providerInvocation = listOf(plan.boundaries.providerInvocation, review.boundaries.providerInvocation)
                .firstOrNull { it != "none" } ?: "none",
            ttsInvocation = listOf(plan.boundaries.ttsInvocation, review.boundaries.ttsInvocation)
                .firstOrNull { it != "none" } ?: "none",
            adapterImplementationExecuted = plan.boundaries.adapterImplementationExecuted ||
                review.boundaries.adapterImplementationExecuted,
            adapterExecuted = plan.boundaries.adapterExecuted || review.boundaries.adapterExecuted,
            contractDraftGenerated = true,
            productionManifestGenerated = plan.boundaries.productionManifestGenerated ||
                review.boundaries.productionManifestGenerated,
            productionReady = plan.boundaries.productionReady || review.boundaries.productionReady,
            productionApproved = plan.boundaries.productionApproved || review.boundaries.productionApproved,
            finalMediaApproval = plan.boundaries.finalMediaApproval || review.boundaries.finalMediaApproval,
            providerMediaHandoff = plan.boundaries.providerMediaHandoff || review.boundaries.providerMediaHandoff,
            release = plan.boundaries.release || review.boundaries.release,
            canonRegistryOrProductionLock = plan.boundaries.canonRegistryOrProductionLock ||
                review.boundaries.canonRegistryOrProductionLock,
            rendererReady = plan.boundaries.rendererReady || review.boundaries.rendererReady,
            mp4ReadinessClaimed = plan.boundaries.mp4ReadinessClaimed || review.boundaries.mp4ReadinessClaimed,
        )

    private fun outputBoundaries(): LocalRendererManifestAdapterContractDraftBoundaries =
        LocalRendererManifestAdapterContractDraftBoundaries(
            fileProtocolJsonOnly = true,
            noExternalPaidProviderTools = true,
            providerInvocation = "none",
            ttsInvocation = "none",
            mediaGenerated = false,
            rendererInvocation = "none",
            directSqliteWrites = false,
            providerApiCalls = false,
            contractDraftGenerated = true,
            adapterImplementationExecuted = false,
            adapterExecuted = false,
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

    private fun rendererInvocationRequested(
        verificationRequirements: List<LocalRendererManifestAdapterContractDraftVerificationRequirement>,
    ): Boolean =
        verificationRequirements.any {
            it.requiresAdapterExecution ||
                it.requiresRendererExecution ||
                it.requiresMediaGeneration ||
                it.requiresProductionManifestApproval
        }

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

    private fun localBoundariesOk(boundaries: LocalRendererManifestAdapterContractPlanReviewBoundaries): Boolean =
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
        findings: MutableList<LocalRendererManifestAdapterContractDraftFinding>,
        ruleId: String,
        path: String,
        suggestedFix: String,
    ): LocalRendererManifestAdapterContractDraftCheck {
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
        return LocalRendererManifestAdapterContractDraftCheck(
            checkId = checkId,
            status = if (passed) "PASS" else "FAIL",
            category = category,
            message = message,
            evidence = evidence,
        )
    }

    private fun List<LocalRendererManifestAdapterContractDraftFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): LocalRendererManifestAdapterContractDraftFinding =
        LocalRendererManifestAdapterContractDraftFinding(
            ruleId,
            category,
            severity,
            path,
            message,
            evidence,
            suggestedFix,
        )
}
