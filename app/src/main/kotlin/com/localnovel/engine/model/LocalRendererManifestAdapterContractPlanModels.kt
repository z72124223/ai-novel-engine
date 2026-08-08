@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalRendererManifestAdapterContractPlan(
    val verdict: String,
    val status: String = "",
    val planId: String = "",
    val sourceDryRunReportFile: String = "",
    val sourcePhase16DryRunId: String = "",
    val sourceDraftId: String = "",
    val sourceReviewId: String = "",
    val sourceManifestDraftPlanId: String = "",
    val sourceManifestDraftPlanReviewId: String = "",
    val sourceImplementationDraftId: String = "",
    val sourceImplementationDraftReviewId: String = "",
    val sourcePlanId: String = "",
    val sourcePlanReviewId: String = "",
    val sourceLocalRendererAdapterDryRunId: String = "",
    val sourceHandoffId: String = "",
    val sourceHandoffReviewId: String = "",
    val sourceStoryInputPackId: String = "",
    val checks: List<LocalRendererManifestAdapterContractPlanCheck> = emptyList(),
    val sourceAlignmentSummary: LocalRendererManifestAdapterContractSourceAlignmentSummary =
        LocalRendererManifestAdapterContractSourceAlignmentSummary(),
    val adapterContractPlanSummary: LocalRendererManifestAdapterContractPlanSummary =
        LocalRendererManifestAdapterContractPlanSummary(),
    val adapterContractRequirements: List<LocalRendererManifestAdapterContractRequirement> = emptyList(),
    val adapterInputMappingPlan: LocalRendererManifestAdapterInputMappingPlan =
        LocalRendererManifestAdapterInputMappingPlan(),
    val requiredAdapterContractWorkItems: List<LocalRendererManifestAdapterContractWorkItem> = emptyList(),
    val verificationPlan: List<LocalRendererManifestAdapterContractVerificationStep> = emptyList(),
    val boundarySummary: LocalRendererManifestAdapterContractBoundarySummary =
        LocalRendererManifestAdapterContractBoundarySummary(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val boundaries: LocalRendererManifestAdapterContractPlanBoundaries =
        LocalRendererManifestAdapterContractPlanBoundaries(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String =
        "local_renderer_manifest_adapter_contract_plan_only_not_adapter_implementation",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<LocalRendererManifestAdapterContractPlanFinding> = emptyList(),
)

@Serializable
data class LocalRendererManifestAdapterContractPlanCheck(
    val checkId: String = "",
    val status: String = "FAIL",
    val category: String = "",
    val message: String = "",
    val evidence: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredForPlan: Boolean = true,
)

@Serializable
data class LocalRendererManifestAdapterContractSourceAlignmentSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceIdsPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourcePhase16DryRunIdPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceDraftIdPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceReviewIdPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceStoryInputPackIdPresent: Boolean = false,
    val sourcePhase16DryRunId: String = "",
    val sourceDraftId: String = "",
    val sourceReviewId: String = "",
    val sourceManifestDraftPlanId: String = "",
    val sourceManifestDraftPlanReviewId: String = "",
    val sourceImplementationDraftId: String = "",
    val sourceImplementationDraftReviewId: String = "",
    val sourcePlanId: String = "",
    val sourcePlanReviewId: String = "",
    val sourceLocalRendererAdapterDryRunId: String = "",
    val sourceHandoffId: String = "",
    val sourceHandoffReviewId: String = "",
    val sourceStoryInputPackId: String = "",
)

@Serializable
data class LocalRendererManifestAdapterContractPlanSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceChecksPass: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceChecksFail: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceFindingCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftPanelCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterDryRunMappingEntries: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mappedPanelCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val unmappedPanelCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val totalDraftFields: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mappedDraftFields: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val missingRequiredFieldCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val targetDurationMillis: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterContractRequirements: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterInputMappingEntries: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredAdapterContractWorkItems: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val verificationSteps: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val planningOnly: Boolean = true,
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
data class LocalRendererManifestAdapterContractRequirement(
    val requirementId: String = "",
    val title: String = "",
    val description: String = "",
    val requiredFields: List<String> = emptyList(),
    val sourceEvidence: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val implementedNow: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterExecuted: Boolean = false,
    val boundary: String = "adapter contract planning only; no adapter implementation and no media generation",
)

@Serializable
data class LocalRendererManifestAdapterInputMappingPlan(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val planningOnly: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterImplementationGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterExecuted: Boolean = false,
    val targetFutureContract: String = "local_renderer_manifest_adapter_contract",
    val targetFutureManifestType: String = "ComicVideoRenderHandoffManifest",
    val mappingBoundary: String = "local_renderer_manifest_adapter_contract_plan_only_not_adapter_implementation",
    val entries: List<LocalRendererManifestAdapterInputMappingEntry> = emptyList(),
    val note: String = "",
)

@Serializable
data class LocalRendererManifestAdapterInputMappingEntry(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val order: Int = 0,
    val panelId: String = "",
    val sourceSceneId: String = "",
    val sourceBeatId: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val startMillis: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val endMillis: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val durationMillis: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceMappedDraftFieldCount: Int = 0,
    val sourceMappedDraftFieldTargets: List<String> = emptyList(),
    val plannedAdapterContractFields: List<String> = emptyList(),
    val sourceDryRunMappingPath: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredFieldCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val missingRequiredFieldCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterImplementationGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterExecuted: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val rendererInvocation: String = "none",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mediaGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionManifestGenerated: Boolean = false,
    val note: String = "",
)

@Serializable
data class LocalRendererManifestAdapterContractWorkItem(
    val workItemId: String = "",
    val title: String = "",
    val description: String = "",
    val targetOutcome: String = "",
    val mustNotDo: List<String> = emptyList(),
    val reviewGateRequiredBeforeImplementation: String =
        "local_renderer_manifest_adapter_contract_plan_review",
)

@Serializable
data class LocalRendererManifestAdapterContractVerificationStep(
    val verificationId: String = "",
    val commandOrCheck: String = "",
    val expectedResult: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiresAdapterExecution: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiresRendererExecution: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiresMediaGeneration: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiresProductionManifestApproval: Boolean = false,
)

@Serializable
data class LocalRendererManifestAdapterContractBoundarySummary(
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
data class LocalRendererManifestAdapterContractPlanBoundaries(
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
data class LocalRendererManifestAdapterContractPlanFinding(
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
