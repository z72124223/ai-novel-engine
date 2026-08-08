@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalRendererManifestAdapterContractDraft(
    val verdict: String,
    val status: String = "",
    val draftId: String = "",
    val sourcePlanFile: String = "",
    val sourcePlanReviewFile: String = "",
    val sourcePlanId: String = "",
    val sourcePlanReviewId: String = "",
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
    val checks: List<LocalRendererManifestAdapterContractDraftCheck> = emptyList(),
    val sourceAlignmentSummary: LocalRendererManifestAdapterContractDraftSourceAlignmentSummary =
        LocalRendererManifestAdapterContractDraftSourceAlignmentSummary(),
    val contractDraftSummary: LocalRendererManifestAdapterContractDraftSummary =
        LocalRendererManifestAdapterContractDraftSummary(),
    val requiredInputs: List<LocalRendererManifestAdapterContractDraftRequiredInput> = emptyList(),
    val normalizedFieldMapping: List<LocalRendererManifestAdapterContractDraftFieldMapping> = emptyList(),
    val adapterObligations: List<LocalRendererManifestAdapterContractDraftObligation> = emptyList(),
    val failStopRules: List<LocalRendererManifestAdapterContractDraftFailStopRule> = emptyList(),
    val rendererInvocationProhibition: LocalRendererManifestAdapterContractDraftRendererProhibition =
        LocalRendererManifestAdapterContractDraftRendererProhibition(),
    val verificationRequirements: List<LocalRendererManifestAdapterContractDraftVerificationRequirement> =
        emptyList(),
    val knownNonGoals: List<String> = emptyList(),
    val boundarySummary: LocalRendererManifestAdapterContractDraftBoundarySummary =
        LocalRendererManifestAdapterContractDraftBoundarySummary(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val boundaries: LocalRendererManifestAdapterContractDraftBoundaries =
        LocalRendererManifestAdapterContractDraftBoundaries(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String =
        "local_renderer_manifest_adapter_contract_draft_only_not_adapter_execution",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<LocalRendererManifestAdapterContractDraftFinding> = emptyList(),
)

@Serializable
data class LocalRendererManifestAdapterContractDraftCheck(
    val checkId: String = "",
    val status: String = "FAIL",
    val category: String = "",
    val message: String = "",
    val evidence: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredForDraft: Boolean = true,
)

@Serializable
data class LocalRendererManifestAdapterContractDraftSourceAlignmentSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceIdsPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val planReviewIdsMatch: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val phase16DryRunIdsMatch: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftIdsMatch: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val reviewIdsMatch: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val storyInputPackIdsMatch: Boolean = false,
    val sourcePlanId: String = "",
    val sourcePlanReviewId: String = "",
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
data class LocalRendererManifestAdapterContractDraftSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourcePlanChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourcePlanReviewChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourcePlanFindings: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourcePlanReviewFindings: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredInputs: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val normalizedFieldMappings: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterObligations: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val failStopRules: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val verificationRequirements: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val knownNonGoals: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mappedPanelCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val unmappedPanelCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val missingRequiredFieldCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val totalDraftFields: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val contractDraftGenerated: Boolean = true,
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
data class LocalRendererManifestAdapterContractDraftRequiredInput(
    val inputId: String = "",
    val description: String = "",
    val sourcePath: String = "",
    val requiredStatus: String = "",
    val requiredDownstreamBoundary: String = "",
    val requiredNextStep: String = "",
)

@Serializable
data class LocalRendererManifestAdapterContractDraftFieldMapping(
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
    val normalizedContractFields: List<String> = emptyList(),
    val sourceMappedDraftFieldTargets: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredFieldCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val missingRequiredFieldCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val contractDraftOnly: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterImplementationExecuted: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterExecuted: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mediaGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionManifestGenerated: Boolean = false,
)

@Serializable
data class LocalRendererManifestAdapterContractDraftObligation(
    val obligationId: String = "",
    val title: String = "",
    val description: String = "",
    val sourceRequirementIds: List<String> = emptyList(),
    val mustPreserve: List<String> = emptyList(),
    val mustNotDo: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterImplementationExecuted: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterExecuted: Boolean = false,
)

@Serializable
data class LocalRendererManifestAdapterContractDraftFailStopRule(
    val ruleId: String = "",
    val description: String = "",
    val failureMode: String = "",
    val evidence: List<String> = emptyList(),
)

@Serializable
data class LocalRendererManifestAdapterContractDraftRendererProhibition(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterExecutionAllowed: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val rendererInvocationAllowed: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val ffmpegExecutionAllowed: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val ttsExecutionAllowed: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mediaGenerationAllowed: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val providerApiCallsAllowed: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionManifestApprovalAllowed: Boolean = false,
    val note: String = "contract draft only; no adapter execution, renderer/ffmpeg/TTS, media generation, provider/API, or production approval",
)

@Serializable
data class LocalRendererManifestAdapterContractDraftVerificationRequirement(
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
data class LocalRendererManifestAdapterContractDraftBoundarySummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val planLocalOnly: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val reviewLocalOnly: Boolean = false,
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
    val contractDraftGenerated: Boolean = true,
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
data class LocalRendererManifestAdapterContractDraftBoundaries(
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
    val contractDraftGenerated: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterImplementationExecuted: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterExecuted: Boolean = false,
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
data class LocalRendererManifestAdapterContractDraftFinding(
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
