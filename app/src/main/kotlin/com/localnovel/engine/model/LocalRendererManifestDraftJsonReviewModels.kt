@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalRendererManifestDraftJsonReviewReport(
    val verdict: String,
    val status: String = "",
    val reviewId: String = "",
    val sourceDraftFile: String = "",
    val sourceDraftId: String = "",
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
    val checks: List<LocalRendererManifestDraftJsonReviewCheck> = emptyList(),
    val sourceAlignmentSummary: LocalRendererManifestDraftJsonReviewSourceAlignmentSummary =
        LocalRendererManifestDraftJsonReviewSourceAlignmentSummary(),
    val draftManifestReviewSummary: LocalRendererManifestDraftJsonReviewSummary =
        LocalRendererManifestDraftJsonReviewSummary(),
    val draftFieldCoverageReviewSummary: LocalRendererManifestDraftJsonFieldCoverageReviewSummary =
        LocalRendererManifestDraftJsonFieldCoverageReviewSummary(),
    val boundarySummary: LocalRendererManifestDraftJsonReviewBoundarySummary =
        LocalRendererManifestDraftJsonReviewBoundarySummary(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val boundaries: LocalRendererManifestDraftJsonReviewBoundaries =
        LocalRendererManifestDraftJsonReviewBoundaries(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String =
        "local_renderer_manifest_draft_json_review_only_not_production_approval",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<LocalRendererManifestDraftJsonReviewFinding> = emptyList(),
)

@Serializable
data class LocalRendererManifestDraftJsonReviewCheck(
    val checkId: String = "",
    val status: String = "FAIL",
    val category: String = "",
    val message: String = "",
    val evidence: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredForReview: Boolean = true,
)

@Serializable
data class LocalRendererManifestDraftJsonReviewSourceAlignmentSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftIdPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestDraftPlanIdPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestDraftPlanReviewIdPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceImplementationDraftIdPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceImplementationDraftReviewIdPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceStoryInputPackIdPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceAlignmentFromDraftPass: Boolean = false,
    val draftId: String = "",
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
)

@Serializable
data class LocalRendererManifestDraftJsonReviewSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceDraftChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceDraftChecksPass: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceDraftChecksFail: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftPanelEntries: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftManifestFields: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val targetDurationMillis: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceFindingCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftOnly: Boolean = true,
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
    val rendererReady: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mp4ReadinessClaimed: Boolean = false,
)

@Serializable
data class LocalRendererManifestDraftJsonFieldCoverageReviewSummary(
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
data class LocalRendererManifestDraftJsonReviewBoundarySummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val fileProtocolJsonOnly: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val noExternalPaidProviderTools: Boolean = true,
    val providerInvocation: String = "none",
    val ttsInvocation: String = "none",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mediaGenerated: Boolean = false,
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
data class LocalRendererManifestDraftJsonReviewBoundaries(
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
data class LocalRendererManifestDraftJsonReviewFinding(
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
