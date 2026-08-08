@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalRendererManifestDraftPlanReviewReport(
    val verdict: String,
    val status: String = "",
    val reviewId: String = "",
    val sourcePlanFile: String = "",
    val sourceManifestDraftPlanId: String = "",
    val sourceImplementationDraftId: String = "",
    val sourceImplementationDraftReviewId: String = "",
    val sourcePlanId: String = "",
    val sourcePlanReviewId: String = "",
    val sourceDryRunId: String = "",
    val sourceHandoffId: String = "",
    val sourceHandoffReviewId: String = "",
    val sourceStoryInputPackId: String = "",
    val checks: List<LocalRendererManifestDraftPlanReviewCheck> = emptyList(),
    val planReviewSummary: LocalRendererManifestDraftPlanReviewSummary =
        LocalRendererManifestDraftPlanReviewSummary(),
    val sourceAlignmentSummary: LocalRendererManifestDraftPlanReviewSourceAlignmentSummary =
        LocalRendererManifestDraftPlanReviewSourceAlignmentSummary(),
    val manifestDraftPlanReadinessSummary: LocalRendererManifestDraftPlanReadinessSummary =
        LocalRendererManifestDraftPlanReadinessSummary(),
    val boundarySummary: LocalRendererManifestDraftPlanReviewBoundarySummary =
        LocalRendererManifestDraftPlanReviewBoundarySummary(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val boundaries: LocalRendererAdapterImplementationDraftBoundaries =
        LocalRendererAdapterImplementationDraftBoundaries(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String =
        "local_renderer_manifest_draft_plan_review_only_not_manifest_generation",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<LocalRendererManifestDraftPlanReviewFinding> = emptyList(),
)

@Serializable
data class LocalRendererManifestDraftPlanReviewCheck(
    val checkId: String = "",
    val status: String = "FAIL",
    val category: String = "",
    val message: String = "",
    val evidence: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredForReview: Boolean = true,
)

@Serializable
data class LocalRendererManifestDraftPlanReviewSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourcePlanChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourcePlanChecksPass: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourcePlanChecksFail: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestPanels: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestFields: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredManifestDraftWorkItems: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val verificationSteps: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val targetDurationMillis: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val planningOnly: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterImplementationExecuted: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val rendererReady: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mp4ReadinessClaimed: Boolean = false,
)

@Serializable
data class LocalRendererManifestDraftPlanReviewSourceAlignmentSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestDraftPlanIdPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceImplementationDraftIdPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceImplementationDraftReviewIdPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceStoryInputPackIdPresent: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceAlignmentFromPlanPass: Boolean = false,
    val manifestDraftPlanId: String = "",
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
data class LocalRendererManifestDraftPlanReadinessSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val readyForFutureManifestJsonDraft: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val realManifestGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val rendererReady: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mp4ReadinessClaimed: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterExecutionStillRequiresExplicitTask: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestJsonDraftStillRequiresExplicitTask: Boolean = true,
    val currentRendererExpectedInput: String = "ComicVideoRenderHandoffManifest",
    val currentReviewInput: String = "LocalRendererManifestDraftPlan",
    val requiredBeforeManifestJsonDraft: List<String> = emptyList(),
    val summary: String = "",
)

@Serializable
data class LocalRendererManifestDraftPlanReviewBoundarySummary(
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
    val manifestGenerated: Boolean = false,
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
data class LocalRendererManifestDraftPlanReviewFinding(
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
