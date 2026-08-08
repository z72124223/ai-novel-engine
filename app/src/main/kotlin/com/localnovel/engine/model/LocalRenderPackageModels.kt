@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalRenderPackageDraft(
    val status: String,
    val packageId: String = "",
    val sourceStoryInputPackFile: String = "",
    val sourceAssetPromptCardsFile: String = "",
    val sourceStoryInputPackId: String = "",
    val sourceAssetPromptCardsStatus: String = "",
    val sourceAssetPromptCardsDraftId: String = "",
    val sourceTitle: String = "",
    val locale: String = "",
    val localOnlyConstraints: LocalRenderPackageConstraints = LocalRenderPackageConstraints(),
    val timelinePlan: List<LocalRenderTimelineItem> = emptyList(),
    val scenePlan: List<LocalRenderScenePlan> = emptyList(),
    val panelPlan: List<LocalRenderPanelPlan> = emptyList(),
    val assetPromptReferences: List<LocalRenderAssetPromptReference> = emptyList(),
    val qaChecklist: List<LocalRenderQaCheck> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val renderBoundary: String = "local_render_package_draft_only_not_media_generation",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String = "local_render_package_draft_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findings: List<LocalRenderPackageDraftFinding> = emptyList(),
    val evidence: LocalRenderPackageDraftEvidence = LocalRenderPackageDraftEvidence(),
)

@Serializable
data class LocalRenderPackageConstraints(
    val localOnly: Boolean = false,
    val noExternalPaidProviderTools: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val fileProtocolJsonOnly: Boolean = true,
    val notes: List<String> = emptyList(),
)

@Serializable
data class LocalRenderTimelineItem(
    val itemId: String = "",
    val sourceBeatId: String = "",
    val sourceSceneId: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val startMillis: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val endMillis: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val durationMillis: Int = 0,
    val narrativeIntent: String = "",
    val dialogueIntent: String = "",
    val audioIntent: String = "",
)

@Serializable
data class LocalRenderScenePlan(
    val scenePlanId: String = "",
    val sourceSceneId: String = "",
    val sceneCardId: String = "",
    val title: String = "",
    val location: String = "",
    val mood: String = "",
    val panelIds: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sceneBoundary: String = "local_scene_render_plan_only",
)

@Serializable
data class LocalRenderPanelPlan(
    val panelId: String = "",
    val timelineItemId: String = "",
    val sourceBeatId: String = "",
    val sourceSceneId: String = "",
    val sceneTitle: String = "",
    val characterCardIds: List<String> = emptyList(),
    val visualPromptCardId: String = "",
    val audioCueCardId: String = "",
    val durationMillis: Int = 0,
    val visualIntent: String = "",
    val audioIntent: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val panelBoundary: String = "local_panel_render_plan_only",
)

@Serializable
data class LocalRenderAssetPromptReference(
    val sourceBeatId: String = "",
    val sourceSceneId: String = "",
    val characterCardIds: List<String> = emptyList(),
    val sceneCardId: String = "",
    val beatCardId: String = "",
    val visualPromptCardId: String = "",
    val audioCueCardId: String = "",
)

@Serializable
data class LocalRenderQaCheck(
    val checkId: String = "",
    val status: String = "FAIL",
    val category: String = "",
    val message: String = "",
    val evidence: List<String> = emptyList(),
    val requiredForPass: Boolean = true,
)

@Serializable
data class LocalRenderPackageDraftFinding(
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
data class LocalRenderPackageDraftEvidence(
    val assetPromptCardsStatus: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val timelineItemCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val scenePlanCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val panelPlanCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val assetPromptReferenceCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val qaCheckCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val qaPassCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val qaFailCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val targetDurationMillis: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val timelineTotalDurationMillis: Int = 0,
    val localOnlyDeclared: Boolean = false,
    val noExternalPaidProviderToolsDeclared: Boolean = false,
    val fileProtocolJsonOnlyDeclared: Boolean = false,
)
