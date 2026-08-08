@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalAssetPromptCardsDraft(
    val status: String,
    val draftId: String = "",
    val sourceStoryInputPackFile: String = "",
    val sourceStoryValidationReportFile: String = "",
    val sourceStoryInputPackId: String = "",
    val sourceStoryValidationId: String = "",
    val sourceTitle: String = "",
    val locale: String = "",
    val localOnlyConstraints: LocalAssetPromptCardsConstraints = LocalAssetPromptCardsConstraints(),
    val characterCards: List<LocalCharacterAssetCard> = emptyList(),
    val sceneCards: List<LocalSceneAssetCard> = emptyList(),
    val beatCards: List<LocalBeatAssetCard> = emptyList(),
    val visualPromptCards: List<LocalVisualPromptCard> = emptyList(),
    val audioCueCards: List<LocalAudioCueCard> = emptyList(),
    val renderIntent: LocalRenderIntent = LocalRenderIntent(),
    val boundaries: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String = "local_asset_prompt_cards_draft_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findings: List<LocalAssetPromptCardsDraftFinding> = emptyList(),
    val evidence: LocalAssetPromptCardsDraftEvidence = LocalAssetPromptCardsDraftEvidence(),
)

@Serializable
data class LocalAssetPromptCardsConstraints(
    val localOnly: Boolean = false,
    val noExternalPaidProviderTools: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val fileProtocolJsonOnly: Boolean = true,
    val notes: List<String> = emptyList(),
)

@Serializable
data class LocalCharacterAssetCard(
    val cardId: String = "",
    val sourceCharacterId: String = "",
    val name: String = "",
    val role: String = "",
    val visualCue: String = "",
    val voiceCue: String = "",
    val publicMotivation: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val cardBoundary: String = "local_character_asset_card_review_only",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val reviewStatus: String = "local_review_required",
)

@Serializable
data class LocalSceneAssetCard(
    val cardId: String = "",
    val sourceSceneId: String = "",
    val title: String = "",
    val location: String = "",
    val mood: String = "",
    val visibleSituation: String = "",
    val visualStyleSeed: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val cardBoundary: String = "local_scene_asset_card_review_only",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val reviewStatus: String = "local_review_required",
)

@Serializable
data class LocalBeatAssetCard(
    val cardId: String = "",
    val sourceBeatId: String = "",
    val sourceSceneId: String = "",
    val sceneTitle: String = "",
    val characterIds: List<String> = emptyList(),
    val characterCardIds: List<String> = emptyList(),
    val narrativeIntent: String = "",
    val dialogueIntent: String = "",
    val visualIntent: String = "",
    val audioIntent: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val cardBoundary: String = "local_beat_asset_card_review_only",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val reviewStatus: String = "local_review_required",
)

@Serializable
data class LocalVisualPromptCard(
    val cardId: String = "",
    val sourceBeatId: String = "",
    val sourceSceneId: String = "",
    val promptSeed: String = "",
    val negativeBoundaryNotes: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val providerInvocation: String = "none",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val submissionStatus: String = "not_submitted",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val reviewStatus: String = "local_review_required",
)

@Serializable
data class LocalAudioCueCard(
    val cardId: String = "",
    val sourceBeatId: String = "",
    val sourceSceneId: String = "",
    val characterIds: List<String> = emptyList(),
    val voiceCueSummary: String = "",
    val audioIntent: String = "",
    val audioStyleSeed: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val ttsInvocation: String = "none",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val reviewStatus: String = "local_review_required",
)

@Serializable
data class LocalRenderIntent(
    val targetDurationMillis: Int = 0,
    val estimatedBeatCount: Int = 0,
    val suggestedPanelCount: Int = 0,
    val locale: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val renderBoundary: String = "local_render_planning_only",
    val summary: String = "",
)

@Serializable
data class LocalAssetPromptCardsDraftFinding(
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
data class LocalAssetPromptCardsDraftEvidence(
    val storyValidationVerdict: String = "",
    val characterCardCount: Int = 0,
    val sceneCardCount: Int = 0,
    val beatCardCount: Int = 0,
    val visualPromptCardCount: Int = 0,
    val audioCueCardCount: Int = 0,
    val targetDurationMillis: Int = 0,
    val localOnlyDeclared: Boolean = false,
    val noExternalPaidProviderToolsDeclared: Boolean = false,
    val fileProtocolJsonOnlyDeclared: Boolean = false,
)
