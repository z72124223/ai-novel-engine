@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalRendererAdapterImplementationDraft(
    val verdict: String,
    val status: String = "",
    val draftId: String = "",
    val sourceHandoffFile: String = "",
    val sourcePlanFile: String = "",
    val sourcePlanReviewFile: String = "",
    val sourcePlanId: String = "",
    val sourcePlanReviewId: String = "",
    val sourceDryRunId: String = "",
    val sourceHandoffId: String = "",
    val sourceHandoffReviewId: String = "",
    val sourceStoryInputPackId: String = "",
    val checks: List<LocalRendererAdapterImplementationDraftCheck> = emptyList(),
    val implementationDraftSummary: LocalRendererAdapterImplementationDraftSummary =
        LocalRendererAdapterImplementationDraftSummary(),
    val sourceAlignmentSummary: LocalRendererAdapterImplementationDraftSourceAlignmentSummary =
        LocalRendererAdapterImplementationDraftSourceAlignmentSummary(),
    val adapterMappingDraft: LocalRendererAdapterMappingDraft = LocalRendererAdapterMappingDraft(),
    val implementationTasks: List<LocalRendererAdapterImplementationDraftTask> = emptyList(),
    val verificationPlan: List<LocalRendererAdapterImplementationDraftVerificationStep> = emptyList(),
    val boundarySummary: LocalRendererAdapterImplementationDraftBoundarySummary =
        LocalRendererAdapterImplementationDraftBoundarySummary(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val boundaries: LocalRendererAdapterImplementationDraftBoundaries =
        LocalRendererAdapterImplementationDraftBoundaries(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String = "local_renderer_adapter_implementation_draft_only_not_manifest_generation",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<LocalRendererAdapterImplementationDraftFinding> = emptyList(),
)

@Serializable
data class LocalRendererAdapterImplementationDraftCheck(
    val checkId: String = "",
    val status: String = "FAIL",
    val category: String = "",
    val message: String = "",
    val evidence: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredForDraft: Boolean = true,
)

@Serializable
data class LocalRendererAdapterImplementationDraftSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val previewPanelCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mappingDraftPanelCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredAdapterWorkItems: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val implementationTaskCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val verificationStepCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val targetMappingNoteCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val missingMappingNoteCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val targetDurationMillis: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterImplementationExecuted: Boolean = false,
)

@Serializable
data class LocalRendererAdapterImplementationDraftSourceAlignmentSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val storyInputPackIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val handoffIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val handoffReviewIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val dryRunIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val planIdAligned: Boolean = false,
    val handoffStoryInputPackId: String = "",
    val planStoryInputPackId: String = "",
    val reviewStoryInputPackId: String = "",
    val handoffId: String = "",
    val planSourceHandoffId: String = "",
    val reviewSourceHandoffId: String = "",
    val planSourceHandoffReviewId: String = "",
    val reviewSourceHandoffReviewId: String = "",
    val planSourceDryRunId: String = "",
    val reviewSourceDryRunId: String = "",
    val planId: String = "",
    val reviewSourcePlanId: String = "",
)

@Serializable
data class LocalRendererAdapterMappingDraft(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val draftOnly: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestGenerated: Boolean = false,
    val targetManifestType: String = "ComicVideoRenderHandoffManifest",
    val mappingBoundary: String = "mapping_draft_only_not_manifest_generation",
    val panelDrafts: List<LocalRendererAdapterPanelImplementationDraft> = emptyList(),
    val renderSettingsDraft: LocalRendererAdapterRenderSettingsDraft = LocalRendererAdapterRenderSettingsDraft(),
    val note: String = "",
)

@Serializable
data class LocalRendererAdapterPanelImplementationDraft(
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
    val captionCue: String = "",
    val dialogueCue: String = "",
    val visualPromptCardId: String = "",
    val audioCueCardId: String = "",
    val targetPanelTimelineDraft: List<LocalRendererAdapterDraftFieldMapping> = emptyList(),
    val targetVoiceTimelineDraft: List<LocalRendererAdapterDraftFieldMapping> = emptyList(),
    val targetSubtitleTimelineDraft: List<LocalRendererAdapterDraftFieldMapping> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val rendererReady: Boolean = false,
    val note: String = "",
)

@Serializable
data class LocalRendererAdapterDraftFieldMapping(
    val targetSection: String = "",
    val targetField: String = "",
    val sourcePreviewPath: String = "",
    val sourcePlanPath: String = "",
    val draftValueHint: String = "",
    val implementationNote: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val required: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val generatedNow: Boolean = false,
)

@Serializable
data class LocalRendererAdapterRenderSettingsDraft(
    val targetSection: String = "renderSettings",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val totalDurationMillis: Int = 0,
    val timelineStatusHint: String = "metadata_only_timing_plan",
    val frameRateHint: String = "24",
    val resolutionHint: String = "1920x1080",
    val rendererBoundaryHint: String = "local_renderer_adapter_draft_only_not_renderer_invocation",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val generatedNow: Boolean = false,
)

@Serializable
data class LocalRendererAdapterImplementationDraftTask(
    val taskId: String = "",
    val sourceWorkItemId: String = "",
    val sourceImplementationStepId: String = "",
    val status: String = "DRAFTED",
    val title: String = "",
    val description: String = "",
    val targetOutcome: String = "",
    val mustNotDo: List<String> = emptyList(),
    val reviewGateRequiredBeforeExecution: String = "local_renderer_adapter_implementation_draft_review",
)

@Serializable
data class LocalRendererAdapterImplementationDraftVerificationStep(
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
data class LocalRendererAdapterImplementationDraftBoundarySummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val handoffLocalOnly: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val planLocalOnly: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val reviewLocalOnly: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mediaGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val rendererInvocation: String = "none",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val providerInvocation: String = "none",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
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
)

@Serializable
data class LocalRendererAdapterImplementationDraftBoundaries(
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
data class LocalRendererAdapterImplementationDraftFinding(
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
