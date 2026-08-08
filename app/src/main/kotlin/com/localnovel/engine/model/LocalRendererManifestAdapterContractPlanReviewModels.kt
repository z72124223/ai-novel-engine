@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalRendererManifestAdapterContractPlanReviewReport(
    val verdict: String,
    val status: String = "",
    val reviewId: String = "",
    val sourcePlanFile: String = "",
    val sourcePlanId: String = "",
    val sourcePhase16DryRunId: String = "",
    val sourceDraftId: String = "",
    val sourceReviewId: String = "",
    val sourceManifestDraftPlanId: String = "",
    val sourceManifestDraftPlanReviewId: String = "",
    val sourceImplementationDraftId: String = "",
    val sourceImplementationDraftReviewId: String = "",
    val sourceAdapterImplementationPlanId: String = "",
    val sourceAdapterImplementationPlanReviewId: String = "",
    val sourceLocalRendererAdapterDryRunId: String = "",
    val sourceHandoffId: String = "",
    val sourceHandoffReviewId: String = "",
    val sourceStoryInputPackId: String = "",
    val checks: List<LocalRendererManifestAdapterContractPlanReviewCheck> = emptyList(),
    val sourceAlignmentSummary: LocalRendererManifestAdapterContractPlanReviewSourceAlignmentSummary =
        LocalRendererManifestAdapterContractPlanReviewSourceAlignmentSummary(),
    val adapterContractPlanReviewSummary: LocalRendererManifestAdapterContractPlanReviewSummary =
        LocalRendererManifestAdapterContractPlanReviewSummary(),
    val reviewedContractRequirementsSummary: LocalRendererManifestReviewedContractRequirementsSummary =
        LocalRendererManifestReviewedContractRequirementsSummary(),
    val reviewedInputMappingSummary: LocalRendererManifestReviewedInputMappingSummary =
        LocalRendererManifestReviewedInputMappingSummary(),
    val reviewedWorkItemsSummary: LocalRendererManifestReviewedWorkItemsSummary =
        LocalRendererManifestReviewedWorkItemsSummary(),
    val verificationReviewSummary: LocalRendererManifestAdapterContractVerificationReviewSummary =
        LocalRendererManifestAdapterContractVerificationReviewSummary(),
    val boundarySummary: LocalRendererManifestAdapterContractPlanReviewBoundarySummary =
        LocalRendererManifestAdapterContractPlanReviewBoundarySummary(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val boundaries: LocalRendererManifestAdapterContractPlanReviewBoundaries =
        LocalRendererManifestAdapterContractPlanReviewBoundaries(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String =
        "local_renderer_manifest_adapter_contract_plan_review_only_not_adapter_implementation",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<LocalRendererManifestAdapterContractPlanReviewFinding> = emptyList(),
)

@Serializable
data class LocalRendererManifestAdapterContractPlanReviewCheck(
    val checkId: String = "",
    val status: String = "FAIL",
    val category: String = "",
    val message: String = "",
    val evidence: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredForReview: Boolean = true,
)

@Serializable
data class LocalRendererManifestAdapterContractPlanReviewSourceAlignmentSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceIdsPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourcePlanIdPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourcePhase16DryRunIdPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceDraftIdPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceReviewIdPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceStoryInputPackIdPresent: Boolean = false,
    val sourcePlanId: String = "",
    val sourcePhase16DryRunId: String = "",
    val sourceDraftId: String = "",
    val sourceReviewId: String = "",
    val sourceManifestDraftPlanId: String = "",
    val sourceManifestDraftPlanReviewId: String = "",
    val sourceImplementationDraftId: String = "",
    val sourceImplementationDraftReviewId: String = "",
    val sourceAdapterImplementationPlanId: String = "",
    val sourceAdapterImplementationPlanReviewId: String = "",
    val sourceLocalRendererAdapterDryRunId: String = "",
    val sourceHandoffId: String = "",
    val sourceHandoffReviewId: String = "",
    val sourceStoryInputPackId: String = "",
)

@Serializable
data class LocalRendererManifestAdapterContractPlanReviewSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceChecksPass: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceChecksFail: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceFindingCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterContractRequirements: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterInputMappingEntries: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredAdapterContractWorkItems: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val verificationSteps: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mappedPanelCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val unmappedPanelCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val missingRequiredFieldCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val totalDraftFields: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val reviewOnly: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterImplementationExecuted: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterExecuted: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val rendererReady: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mp4ReadinessClaimed: Boolean = false,
)

@Serializable
data class LocalRendererManifestReviewedContractRequirementsSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requirementCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredFieldCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val implementedNowCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterExecutedCount: Int = 0,
    val requirementIds: List<String> = emptyList(),
)

@Serializable
data class LocalRendererManifestReviewedInputMappingSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val planningOnly: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mappingEntries: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mappedPanelCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredFieldCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val missingRequiredFieldCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterImplementationGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterExecuted: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val rendererInvocationCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mediaGeneratedCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionManifestGeneratedCount: Int = 0,
)

@Serializable
data class LocalRendererManifestReviewedWorkItemsSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val workItemCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mustNotDoItemCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val allRequireReviewGate: Boolean = false,
)

@Serializable
data class LocalRendererManifestAdapterContractVerificationReviewSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val verificationStepCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterExecutionChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val rendererExecutionChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mediaGenerationChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionApprovalChecks: Int = 0,
)

@Serializable
data class LocalRendererManifestAdapterContractPlanReviewBoundarySummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceLocalOnly: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mediaGenerated: Boolean = false,
    val rendererInvocation: String = "none",
    val providerInvocation: String = "none",
    val ttsInvocation: String = "none",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterImplementationExecuted: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterExecuted: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftManifestGenerated: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionManifestGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionReady: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionApproved: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val finalMediaApproval: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val providerMediaHandoff: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val release: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val canonRegistryOrProductionLock: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val rendererReady: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mp4ReadinessClaimed: Boolean = false,
)

@Serializable
data class LocalRendererManifestAdapterContractPlanReviewBoundaries(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val fileProtocolJsonOnly: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val noExternalPaidProviderTools: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val providerInvocation: String = "none",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val ttsInvocation: String = "none",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mediaGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val rendererInvocation: String = "none",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val directSqliteWrites: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val providerApiCalls: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterImplementationExecuted: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterExecuted: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftManifestGenerated: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionManifestGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionReady: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionApproved: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val finalMediaApproval: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val providerMediaHandoff: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val release: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val canonRegistryOrProductionLock: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val rendererReady: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mp4ReadinessClaimed: Boolean = false,
)

@Serializable
data class LocalRendererManifestAdapterContractPlanReviewFinding(
    @SerialName("rule_id")
    val ruleId: String,
    val category: String,
    val severity: String,
    val path: String,
    val message: String,
    val evidence: List<String> = emptyList(),
    @SerialName("suggested_fix")
    val suggestedFix: String = "",
)
