@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalRendererAdapterImplementationPlanReviewReport(
    val verdict: String,
    val status: String = "",
    val reviewId: String = "",
    val sourcePlanFile: String = "",
    val sourcePlanId: String = "",
    val sourceDryRunId: String = "",
    val sourceHandoffId: String = "",
    val sourceReviewId: String = "",
    val sourceStoryInputPackId: String = "",
    val checks: List<LocalRendererAdapterImplementationPlanReviewCheck> = emptyList(),
    val planSummary: LocalRendererAdapterImplementationPlanReviewSummary =
        LocalRendererAdapterImplementationPlanReviewSummary(),
    val boundarySummary: LocalRendererAdapterImplementationPlanReviewBoundarySummary =
        LocalRendererAdapterImplementationPlanReviewBoundarySummary(),
    val rendererCompatibilitySummary: LocalRendererAdapterImplementationPlanReviewRendererCompatibilitySummary =
        LocalRendererAdapterImplementationPlanReviewRendererCompatibilitySummary(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val boundaries: LocalRendererAdapterImplementationPlanBoundaries =
        LocalRendererAdapterImplementationPlanBoundaries(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String = "local_renderer_adapter_implementation_plan_review_only_not_implementation",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<LocalRendererAdapterImplementationPlanReviewFinding> = emptyList(),
)

@Serializable
data class LocalRendererAdapterImplementationPlanReviewCheck(
    val checkId: String = "",
    val status: String = "FAIL",
    val category: String = "",
    val message: String = "",
    val evidence: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredForReview: Boolean = true,
)

@Serializable
data class LocalRendererAdapterImplementationPlanReviewSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceValidationChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceValidationPass: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceValidationFail: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredAdapterWorkItems: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val targetMappingNoteCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val perPanelMappingNotes: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val implementationSteps: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val riskChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val verificationSteps: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val planningOnly: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val targetManifestGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
)

@Serializable
data class LocalRendererAdapterImplementationPlanReviewBoundarySummary(
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
)

@Serializable
data class LocalRendererAdapterImplementationPlanReviewRendererCompatibilitySummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val ready: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mp4ReadinessClaimed: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterImplementationStillRequired: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestGenerationStillRequiresExplicitTask: Boolean = true,
    val currentRendererExpectedInput: String = "ComicVideoRenderHandoffManifest",
    val currentReviewInput: String = "LocalRendererAdapterImplementationPlanDraft",
    val requiredBeforeMp4: List<String> = emptyList(),
    val summary: String = "",
)

@Serializable
data class LocalRendererAdapterImplementationPlanReviewFinding(
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
