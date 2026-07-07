@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalRendererManifestDraftJsonDraft(
    val verdict: String,
    val status: String = "",
    val draftId: String = "",
    val sourcePlanFile: String = "",
    val sourcePlanReviewFile: String = "",
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
    val checks: List<LocalRendererManifestDraftJsonDraftCheck> = emptyList(),
    val sourceAlignmentSummary: LocalRendererManifestDraftJsonDraftSourceAlignmentSummary =
        LocalRendererManifestDraftJsonDraftSourceAlignmentSummary(),
    val draftManifestSummary: LocalRendererManifestDraftJsonDraftSummary =
        LocalRendererManifestDraftJsonDraftSummary(),
    val draftPanelManifestEntries: List<LocalRendererDraftPanelManifestEntry> = emptyList(),
    val draftFieldCoverageSummary: LocalRendererManifestDraftJsonFieldCoverageSummary =
        LocalRendererManifestDraftJsonFieldCoverageSummary(),
    val boundarySummary: LocalRendererManifestDraftJsonDraftBoundarySummary =
        LocalRendererManifestDraftJsonDraftBoundarySummary(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val boundaries: LocalRendererManifestDraftJsonDraftBoundaries =
        LocalRendererManifestDraftJsonDraftBoundaries(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String =
        "local_renderer_manifest_draft_json_draft_only_not_production_manifest",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<LocalRendererManifestDraftJsonDraftFinding> = emptyList(),
)

@Serializable
data class LocalRendererManifestDraftJsonDraftCheck(
    val checkId: String = "",
    val status: String = "FAIL",
    val category: String = "",
    val message: String = "",
    val evidence: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredForDraft: Boolean = true,
)

@Serializable
data class LocalRendererManifestDraftJsonDraftSourceAlignmentSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestDraftPlanIdAligned: Boolean = false,
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
    val manifestDraftPlanId: String = "",
    val reviewSourceManifestDraftPlanId: String = "",
    val planSourceImplementationDraftId: String = "",
    val reviewSourceImplementationDraftId: String = "",
    val planSourceImplementationDraftReviewId: String = "",
    val reviewSourceImplementationDraftReviewId: String = "",
    val planSourcePlanId: String = "",
    val reviewSourcePlanId: String = "",
    val planSourcePlanReviewId: String = "",
    val reviewSourcePlanReviewId: String = "",
    val planSourceDryRunId: String = "",
    val reviewSourceDryRunId: String = "",
    val planSourceHandoffId: String = "",
    val reviewSourceHandoffId: String = "",
    val planSourceHandoffReviewId: String = "",
    val reviewSourceHandoffReviewId: String = "",
    val planSourceStoryInputPackId: String = "",
    val reviewSourceStoryInputPackId: String = "",
)

@Serializable
data class LocalRendererManifestDraftJsonDraftSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourcePlanChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourcePlanReviewChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourcePlanReviewChecksPass: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourcePlanReviewChecksFail: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftPanelEntries: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftManifestFields: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredManifestDraftWorkItems: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val verificationSteps: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val targetDurationMillis: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftOnly: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftManifestGenerated: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionManifestGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionReady: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterImplementationExecuted: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val rendererReady: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mp4ReadinessClaimed: Boolean = false,
)

@Serializable
data class LocalRendererDraftPanelManifestEntry(
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
    val draftPanelTimelineFields: List<LocalRendererManifestDraftJsonField> = emptyList(),
    val draftVoiceTimelineFields: List<LocalRendererManifestDraftJsonField> = emptyList(),
    val draftSubtitleTimelineFields: List<LocalRendererManifestDraftJsonField> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftOnly: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftManifestGenerated: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionManifestGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionReady: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val rendererReady: Boolean = false,
    val note: String = "",
)

@Serializable
data class LocalRendererManifestDraftJsonField(
    val targetSection: String = "",
    val targetField: String = "",
    val sourcePlanPath: String = "",
    val sourcePlanReviewPath: String = "",
    val draftValueHint: String = "",
    val implementationNote: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val required: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftOnly: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionReady: Boolean = false,
)

@Serializable
data class LocalRendererManifestDraftJsonFieldCoverageSummary(
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
    val missingRequiredDraftFields: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftOnly: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionManifestGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionReady: Boolean = false,
)

@Serializable
data class LocalRendererManifestDraftJsonDraftBoundarySummary(
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
    val productionManifestGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionReady: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val finalMediaApproval: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val providerMediaHandoff: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val canonRegistryOrProductionLock: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val rendererReady: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mp4ReadinessClaimed: Boolean = false,
)

@Serializable
data class LocalRendererManifestDraftJsonDraftBoundaries(
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
    val draftManifestGenerated: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionManifestGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionReady: Boolean = false,
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
data class LocalRendererManifestDraftJsonDraftFinding(
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
