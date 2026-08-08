@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalStoryVideoPreviewHandoffDraft(
    val status: String,
    val handoffId: String = "",
    val sourceStoryInputPackId: String = "",
    val sourceChainReportFile: String = "",
    val sourceRenderPackageFile: String = "",
    val sourceAssetPromptCardsFile: String = "",
    val sourceStoryInputFile: String = "",
    val sourceChainStatus: String = "",
    val sourceRenderPackageStatus: String = "",
    val sourceAssetPromptCardsStatus: String = "",
    val readinessChecks: List<LocalStoryVideoPreviewHandoffReadinessCheck> = emptyList(),
    val previewPlan: LocalStoryVideoPreviewPlan = LocalStoryVideoPreviewPlan(),
    val localRendererCompatibility: LocalStoryVideoLocalRendererCompatibility = LocalStoryVideoLocalRendererCompatibility(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val boundaries: LocalStoryVideoPreviewHandoffBoundaries = LocalStoryVideoPreviewHandoffBoundaries(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String = "local_story_video_preview_handoff_draft_only_not_media_generation",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findings: List<LocalStoryVideoPreviewHandoffFinding> = emptyList(),
    val evidence: LocalStoryVideoPreviewHandoffEvidence = LocalStoryVideoPreviewHandoffEvidence(),
)

@Serializable
data class LocalStoryVideoPreviewHandoffReadinessCheck(
    val checkId: String = "",
    val status: String = "FAIL",
    val category: String = "",
    val message: String = "",
    val evidence: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredForDraft: Boolean = true,
)

@Serializable
data class LocalStoryVideoPreviewPlan(
    val sceneOrdering: List<LocalStoryVideoPreviewSceneOrder> = emptyList(),
    val panelOrdering: List<LocalStoryVideoPreviewPanelPlan> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val targetDurationMillis: Int = 0,
    val qaSummary: LocalStoryVideoPreviewQaSummary = LocalStoryVideoPreviewQaSummary(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val planBoundary: String = "local_preview_handoff_plan_only",
)

@Serializable
data class LocalStoryVideoPreviewSceneOrder(
    val scenePlanId: String = "",
    val sourceSceneId: String = "",
    val title: String = "",
    val location: String = "",
    val mood: String = "",
    val panelIds: List<String> = emptyList(),
)

@Serializable
data class LocalStoryVideoPreviewPanelPlan(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val order: Int = 0,
    val panelId: String = "",
    val sourceBeatId: String = "",
    val sourceSceneId: String = "",
    val sceneTitle: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val startMillis: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val endMillis: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val durationMillis: Int = 0,
    val captionCue: String = "",
    val dialogueCue: String = "",
    val visualIntent: String = "",
    val audioIntent: String = "",
    val characterCardIds: List<String> = emptyList(),
    val visualPromptCardId: String = "",
    val visualPromptSeed: String = "",
    val audioCueCardId: String = "",
    val audioCueSummary: String = "",
    val assetPromptReference: LocalStoryVideoPreviewAssetPromptReference = LocalStoryVideoPreviewAssetPromptReference(),
)

@Serializable
data class LocalStoryVideoPreviewAssetPromptReference(
    val sourceBeatId: String = "",
    val sourceSceneId: String = "",
    val beatCardId: String = "",
    val sceneCardId: String = "",
    val visualPromptCardId: String = "",
    val audioCueCardId: String = "",
    val characterCardIds: List<String> = emptyList(),
)

@Serializable
data class LocalStoryVideoPreviewQaSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val qaChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val qaPass: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val qaFail: Int = 0,
    val sourceQaCheckIds: List<String> = emptyList(),
)

@Serializable
data class LocalStoryVideoLocalRendererCompatibility(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val ready: Boolean = false,
    val currentRendererExpectedInput: String = "ComicVideoRenderHandoffManifest",
    val currentDraftInput: String = "LocalStoryVideoPreviewHandoffDraft",
    val readyFields: List<String> = emptyList(),
    val missingFields: List<String> = emptyList(),
    val requiredChecksBeforeMp4: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val compatibilityBoundary: String = "review_only_not_renderer_invocation",
)

@Serializable
data class LocalStoryVideoPreviewHandoffBoundaries(
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
    val finalMediaApproval: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val release: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val canonRegistryOrProductionLock: Boolean = false,
)

@Serializable
data class LocalStoryVideoPreviewHandoffFinding(
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

@Serializable
data class LocalStoryVideoPreviewHandoffEvidence(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val readinessCheckCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val readinessPassCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val readinessFailCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sceneCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val panelCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val visualPromptReferenceCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val audioCueReferenceCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val targetDurationMillis: Int = 0,
    val localRendererReady: Boolean = false,
)
