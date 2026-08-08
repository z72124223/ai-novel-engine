@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalRendererManifestDraftPlan(
    val verdict: String,
    val status: String = "",
    val planId: String = "",
    val sourceImplementationDraftFile: String = "",
    val sourceDraftReviewFile: String = "",
    val sourceDraftId: String = "",
    val sourceDraftReviewId: String = "",
    val sourcePlanId: String = "",
    val sourcePlanReviewId: String = "",
    val sourceDryRunId: String = "",
    val sourceHandoffId: String = "",
    val sourceHandoffReviewId: String = "",
    val sourceStoryInputPackId: String = "",
    val checks: List<LocalRendererManifestDraftPlanCheck> = emptyList(),
    val manifestDraftPlanSummary: LocalRendererManifestDraftPlanSummary =
        LocalRendererManifestDraftPlanSummary(),
    val sourceAlignmentSummary: LocalRendererManifestDraftPlanSourceAlignmentSummary =
        LocalRendererManifestDraftPlanSourceAlignmentSummary(),
    val manifestFieldPlan: LocalRendererManifestFieldPlan = LocalRendererManifestFieldPlan(),
    val requiredManifestDraftWorkItems: List<LocalRendererManifestDraftWorkItem> = emptyList(),
    val verificationPlan: List<LocalRendererManifestDraftPlanVerificationStep> = emptyList(),
    val boundarySummary: LocalRendererManifestDraftPlanBoundarySummary =
        LocalRendererManifestDraftPlanBoundarySummary(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val boundaries: LocalRendererAdapterImplementationDraftBoundaries =
        LocalRendererAdapterImplementationDraftBoundaries(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String = "local_renderer_manifest_draft_plan_only_not_manifest_generation",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<LocalRendererManifestDraftPlanFinding> = emptyList(),
)

@Serializable
data class LocalRendererManifestDraftPlanCheck(
    val checkId: String = "",
    val status: String = "FAIL",
    val category: String = "",
    val message: String = "",
    val evidence: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredForPlan: Boolean = true,
)

@Serializable
data class LocalRendererManifestDraftPlanSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceDraftChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceDraftReviewChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceDraftReviewChecksPass: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceDraftReviewChecksFail: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val implementationTasks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mappingDraftPanels: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mappingDraftFields: Int = 0,
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
data class LocalRendererManifestDraftPlanSourceAlignmentSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val planIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val planReviewIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val dryRunIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val handoffIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val handoffReviewIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val storyInputPackIdAligned: Boolean = false,
    val draftId: String = "",
    val reviewSourceDraftId: String = "",
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
data class LocalRendererManifestFieldPlan(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val planningOnly: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestGenerated: Boolean = false,
    val targetManifestType: String = "ComicVideoRenderHandoffManifest",
    val planningBoundary: String = "local_renderer_manifest_draft_plan_only_not_manifest_generation",
    val panelManifestPlans: List<LocalRendererManifestPanelPlan> = emptyList(),
    val renderSettingsPlan: LocalRendererManifestRenderSettingsPlan =
        LocalRendererManifestRenderSettingsPlan(),
    val note: String = "",
)

@Serializable
data class LocalRendererManifestPanelPlan(
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
    val plannedPanelTimelineFields: List<LocalRendererManifestFieldMappingPlan> = emptyList(),
    val plannedVoiceTimelineFields: List<LocalRendererManifestFieldMappingPlan> = emptyList(),
    val plannedSubtitleTimelineFields: List<LocalRendererManifestFieldMappingPlan> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val rendererReady: Boolean = false,
    val note: String = "",
)

@Serializable
data class LocalRendererManifestFieldMappingPlan(
    val targetSection: String = "",
    val targetField: String = "",
    val sourceDraftPath: String = "",
    val plannedValueHint: String = "",
    val implementationNote: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val required: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val generatedNow: Boolean = false,
)

@Serializable
data class LocalRendererManifestRenderSettingsPlan(
    val targetSection: String = "renderSettings",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val totalDurationMillis: Int = 0,
    val timelineStatusHint: String = "metadata_only_timing_plan",
    val frameRateHint: String = "24",
    val resolutionHint: String = "1920x1080",
    val rendererBoundaryHint: String = "local_renderer_manifest_draft_plan_only_not_renderer_invocation",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val generatedNow: Boolean = false,
)

@Serializable
data class LocalRendererManifestDraftWorkItem(
    val workItemId: String = "",
    val sourceImplementationTaskId: String = "",
    val title: String = "",
    val description: String = "",
    val targetOutcome: String = "",
    val mustNotDo: List<String> = emptyList(),
    val reviewGateRequiredBeforeDraft: String = "local_renderer_manifest_draft_plan_review",
)

@Serializable
data class LocalRendererManifestDraftPlanVerificationStep(
    val verificationId: String = "",
    val sourceVerificationId: String = "",
    val commandOrCheck: String = "",
    val expectedResult: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiresMediaGeneration: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiresManifestGeneration: Boolean = false,
)

@Serializable
data class LocalRendererManifestDraftPlanBoundarySummary(
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
    val manifestGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterImplementationExecuted: Boolean = false,
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
data class LocalRendererManifestDraftPlanFinding(
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
