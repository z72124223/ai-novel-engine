@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalRendererManifestDraftJsonAdapterDryRunReport(
    val verdict: String,
    val status: String = "",
    val dryRunId: String = "",
    val sourceDraftFile: String = "",
    val sourceReviewFile: String = "",
    val sourceDraftId: String = "",
    val sourceReviewId: String = "",
    val sourceManifestDraftPlanId: String = "",
    val sourceManifestDraftPlanReviewId: String = "",
    val sourceImplementationDraftId: String = "",
    val sourceImplementationDraftReviewId: String = "",
    val sourcePlanId: String = "",
    val sourcePlanReviewId: String = "",
    val sourceDryRunId: String = "",
    val sourceHandoffId: String = "",
    val sourceHandoffReviewId: String = "",
    val sourceStoryInputPackId: String = "",
    val checks: List<LocalRendererManifestDraftJsonAdapterDryRunCheck> = emptyList(),
    val sourceAlignmentSummary: LocalRendererManifestDraftJsonAdapterDryRunSourceAlignmentSummary =
        LocalRendererManifestDraftJsonAdapterDryRunSourceAlignmentSummary(),
    val adapterDryRunSummary: LocalRendererManifestDraftJsonAdapterDryRunSummary =
        LocalRendererManifestDraftJsonAdapterDryRunSummary(),
    val panelMappingSummary: LocalRendererManifestDraftJsonAdapterPanelMappingSummary =
        LocalRendererManifestDraftJsonAdapterPanelMappingSummary(),
    val draftFieldCoverageSummary: LocalRendererManifestDraftJsonAdapterFieldCoverageSummary =
        LocalRendererManifestDraftJsonAdapterFieldCoverageSummary(),
    val boundarySummary: LocalRendererManifestDraftJsonAdapterDryRunBoundarySummary =
        LocalRendererManifestDraftJsonAdapterDryRunBoundarySummary(),
    val adapterDryRunMappingEntries: List<LocalRendererManifestDraftJsonAdapterPanelMappingEntry> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val boundaries: LocalRendererManifestDraftJsonAdapterDryRunBoundaries =
        LocalRendererManifestDraftJsonAdapterDryRunBoundaries(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String =
        "local_renderer_manifest_draft_json_adapter_dry_run_only_not_renderer_execution",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<LocalRendererManifestDraftJsonAdapterDryRunFinding> = emptyList(),
)

@Serializable
data class LocalRendererManifestDraftJsonAdapterDryRunCheck(
    val checkId: String = "",
    val status: String = "FAIL",
    val category: String = "",
    val message: String = "",
    val evidence: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredForDryRun: Boolean = true,
)

@Serializable
data class LocalRendererManifestDraftJsonAdapterDryRunSourceAlignmentSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestDraftPlanIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestDraftPlanReviewIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceImplementationDraftIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceImplementationDraftReviewIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourcePlanIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourcePlanReviewIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceDryRunIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceHandoffIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceHandoffReviewIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceStoryInputPackIdAligned: Boolean = false,
    val draftId: String = "",
    val reviewSourceDraftId: String = "",
    val draftSourceManifestDraftPlanId: String = "",
    val reviewSourceManifestDraftPlanId: String = "",
    val draftSourceManifestDraftPlanReviewId: String = "",
    val reviewSourceManifestDraftPlanReviewId: String = "",
    val draftSourceImplementationDraftId: String = "",
    val reviewSourceImplementationDraftId: String = "",
    val draftSourceImplementationDraftReviewId: String = "",
    val reviewSourceImplementationDraftReviewId: String = "",
    val draftSourcePlanId: String = "",
    val reviewSourcePlanId: String = "",
    val draftSourcePlanReviewId: String = "",
    val reviewSourcePlanReviewId: String = "",
    val draftSourceDryRunId: String = "",
    val reviewSourceDryRunId: String = "",
    val draftSourceHandoffId: String = "",
    val reviewSourceHandoffId: String = "",
    val draftSourceHandoffReviewId: String = "",
    val reviewSourceHandoffReviewId: String = "",
    val draftSourceStoryInputPackId: String = "",
    val reviewSourceStoryInputPackId: String = "",
)

@Serializable
data class LocalRendererManifestDraftJsonAdapterDryRunSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceDraftChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceReviewChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceReviewChecksPass: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceReviewChecksFail: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceDraftFindings: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceReviewFindings: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val targetDurationMillis: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftPanelEntries: Int = 0,
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
    val draftManifestGenerated: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionManifestGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionReady: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionApproved: Boolean = false,
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
data class LocalRendererManifestDraftJsonAdapterPanelMappingSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftPanelCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterDryRunMappingEntries: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mappedPanelCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val unmappedPanelCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val missingRequiredFieldCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val targetDurationMillis: Int = 0,
)

@Serializable
data class LocalRendererManifestDraftJsonAdapterFieldCoverageSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val panelEntries: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val panelTimelineFields: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val voiceTimelineFields: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val subtitleTimelineFields: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val totalDraftFields: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mappedDraftFields: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val missingRequiredDraftFields: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftOnly: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionManifestGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionReady: Boolean = false,
)

@Serializable
data class LocalRendererManifestDraftJsonAdapterDryRunBoundarySummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftLocalOnly: Boolean = false,
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
data class LocalRendererManifestDraftJsonAdapterPanelMappingEntry(
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
    val panelTimelineFieldCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val voiceTimelineFieldCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val subtitleTimelineFieldCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mappedDraftFieldCount: Int = 0,
    val mappedDraftFieldTargets: List<String> = emptyList(),
    val missingRequiredFields: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val canMapForFutureAdapter: Boolean = false,
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
data class LocalRendererManifestDraftJsonAdapterDryRunBoundaries(
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
data class LocalRendererManifestDraftJsonAdapterDryRunFinding(
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
