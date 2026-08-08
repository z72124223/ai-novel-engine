@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalRendererAdapterImplementationDraftReviewReport(
    val verdict: String,
    val status: String = "",
    val reviewId: String = "",
    val sourceDraftFile: String = "",
    val sourceDraftId: String = "",
    val sourcePlanId: String = "",
    val sourcePlanReviewId: String = "",
    val sourceDryRunId: String = "",
    val sourceHandoffId: String = "",
    val sourceHandoffReviewId: String = "",
    val sourceStoryInputPackId: String = "",
    val checks: List<LocalRendererAdapterImplementationDraftReviewCheck> = emptyList(),
    val draftSummary: LocalRendererAdapterImplementationDraftReviewSummary =
        LocalRendererAdapterImplementationDraftReviewSummary(),
    val boundarySummary: LocalRendererAdapterImplementationDraftReviewBoundarySummary =
        LocalRendererAdapterImplementationDraftReviewBoundarySummary(),
    val rendererCompatibilitySummary: LocalRendererAdapterImplementationDraftReviewRendererCompatibilitySummary =
        LocalRendererAdapterImplementationDraftReviewRendererCompatibilitySummary(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val boundaries: LocalRendererAdapterImplementationDraftBoundaries =
        LocalRendererAdapterImplementationDraftBoundaries(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String =
        "local_renderer_adapter_implementation_draft_review_only_not_manifest_generation",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<LocalRendererAdapterImplementationDraftReviewFinding> = emptyList(),
)

@Serializable
data class LocalRendererAdapterImplementationDraftReviewCheck(
    val checkId: String = "",
    val status: String = "FAIL",
    val category: String = "",
    val message: String = "",
    val evidence: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredForReview: Boolean = true,
)

@Serializable
data class LocalRendererAdapterImplementationDraftReviewSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceChecksPass: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceChecksFail: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val implementationTasks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mappingDraftPanels: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mappingDraftFields: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val verificationSteps: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftOnly: Boolean = true,
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
data class LocalRendererAdapterImplementationDraftReviewBoundarySummary(
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
data class LocalRendererAdapterImplementationDraftReviewRendererCompatibilitySummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val ready: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mp4ReadinessClaimed: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterExecutionStillRequiresExplicitTask: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestDraftPlanStillRequiresExplicitTask: Boolean = true,
    val currentRendererExpectedInput: String = "ComicVideoRenderHandoffManifest",
    val currentReviewInput: String = "LocalRendererAdapterImplementationDraft",
    val requiredBeforeManifestDraftPlan: List<String> = emptyList(),
    val summary: String = "",
)

@Serializable
data class LocalRendererAdapterImplementationDraftReviewFinding(
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
