@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalRendererAdapterDryRunReport(
    val verdict: String,
    val status: String = "",
    val dryRunId: String = "",
    val sourceHandoffFile: String = "",
    val sourceReviewFile: String = "",
    val sourceHandoffId: String = "",
    val sourceReviewId: String = "",
    val sourceStoryInputPackId: String = "",
    val checks: List<LocalRendererAdapterDryRunCheck> = emptyList(),
    val adapterMappingSummary: LocalRendererAdapterMappingSummary = LocalRendererAdapterMappingSummary(),
    val dryRunMappingPlan: List<LocalRendererAdapterPanelMappingNote> = emptyList(),
    val rendererCompatibilitySummary: LocalRendererAdapterCompatibilitySummary =
        LocalRendererAdapterCompatibilitySummary(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val boundaries: LocalStoryVideoPreviewHandoffBoundaries = LocalStoryVideoPreviewHandoffBoundaries(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String = "local_renderer_adapter_dry_run_only_not_media_generation",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<LocalRendererAdapterDryRunFinding> = emptyList(),
)

@Serializable
data class LocalRendererAdapterDryRunCheck(
    val checkId: String = "",
    val status: String = "FAIL",
    val category: String = "",
    val message: String = "",
    val evidence: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredForDryRun: Boolean = true,
)

@Serializable
data class LocalRendererAdapterMappingSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sceneCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val panelCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val targetDurationMillis: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mappedPanelCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val unmappedPanelCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val missingRequiredFieldCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val visualPromptReferenceCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val audioCueReferenceCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val qaChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val qaPass: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val qaFail: Int = 0,
)

@Serializable
data class LocalRendererAdapterPanelMappingNote(
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
    val proposedPanelTimelineFields: LocalRendererAdapterPanelTimelineFieldNotes =
        LocalRendererAdapterPanelTimelineFieldNotes(),
    val proposedVoiceTimelineFields: LocalRendererAdapterVoiceTimelineFieldNotes =
        LocalRendererAdapterVoiceTimelineFieldNotes(),
    val proposedSubtitleTimelineFields: LocalRendererAdapterSubtitleTimelineFieldNotes =
        LocalRendererAdapterSubtitleTimelineFieldNotes(),
    val missingRequiredFields: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val canMapForFutureAdapter: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestGenerated: Boolean = false,
    val note: String = "",
)

@Serializable
data class LocalRendererAdapterPanelTimelineFieldNotes(
    val wouldMapPanelIdFrom: String = "",
    val wouldMapSourceFrameIdFrom: String = "",
    val wouldMapSceneReferenceFrom: String = "",
    val wouldMapBeatReferenceFrom: String = "",
    val wouldMapVisualIntentFrom: String = "",
    val wouldMapCameraIntentFrom: String = "",
    val wouldMapTimingFrom: String = "",
    val wouldMapAssetCandidateRefsFrom: String = "",
)

@Serializable
data class LocalRendererAdapterVoiceTimelineFieldNotes(
    val wouldMapCueIdFrom: String = "",
    val wouldMapPanelIdFrom: String = "",
    val wouldMapTextFrom: String = "",
    val wouldMapTimingFrom: String = "",
    val wouldNeedVoiceLabelsFrom: String = "",
)

@Serializable
data class LocalRendererAdapterSubtitleTimelineFieldNotes(
    val wouldMapCueIdFrom: String = "",
    val wouldMapPanelIdFrom: String = "",
    val wouldMapAudioCueIdFrom: String = "",
    val wouldMapTextFrom: String = "",
    val wouldMapTimingFrom: String = "",
)

@Serializable
data class LocalRendererAdapterCompatibilitySummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val ready: Boolean = false,
    val currentRendererExpectedInput: String = "",
    val currentDraftInput: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterImplementationRequired: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestGenerated: Boolean = false,
    val requiredImplementationWork: List<String> = emptyList(),
    val summary: String = "",
)

@Serializable
data class LocalRendererAdapterDryRunFinding(
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
