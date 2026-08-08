@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ComicVideoProductionPackageDraft(
    val verdict: String,
    val packageId: String = "",
    val packageStatus: String = "FAIL",
    val imageFacingDeliveryProofFile: String = "",
    val voiceTimelineFillFile: String = "",
    val generatedComicVideoPacketId: String = "",
    val generatedPacketValidationId: String = "",
    val sourceImageFacingDeliveryProofId: String = "",
    val sourceReviewableStoryboardPacketId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val sourceVerdicts: ComicVideoPacketSourceVerdicts = ComicVideoPacketSourceVerdicts(),
    val storyboardProductionTable: List<ComicVideoStoryboardProductionRow> = emptyList(),
    val shotPromptPacketId: String = "",
    val shotPrompts: List<ComicVideoShotPromptDraft> = emptyList(),
    val assetPromptCards: List<ComicVideoAssetPromptCard> = emptyList(),
    val voiceStyleCards: List<ComicVideoVoiceStyleCard> = emptyList(),
    val downstreamBoundary: String = "comic_video_production_package_draft_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    val boundaryChecklist: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ComicVideoProductionPackageDraftFinding> = emptyList(),
    val evidence: ComicVideoProductionPackageDraftEvidence = ComicVideoProductionPackageDraftEvidence(),
)

@Serializable
data class ComicVideoStoryboardProductionRow(
    val shotId: String = "",
    val panelId: String = "",
    val sourceFrameId: String = "",
    val sceneReference: String = "",
    val beatReference: String = "",
    val durationMillis: Int = 0,
    val visualGoal: String = "",
    val camera: String = "",
    val onScreenAssetCardIds: List<String> = emptyList(),
    val voiceId: String = "",
    val characterId: String = "",
    val spokenText: String = "",
    val subtitleText: String = "",
    val continuityMustShow: List<String> = emptyList(),
    val continuityMustNotShow: List<String> = emptyList(),
    val productionNotes: List<String> = emptyList(),
)

@Serializable
data class ComicVideoShotPromptDraft(
    val shotId: String = "",
    val panelId: String = "",
    val sourceFrameId: String = "",
    val promptBoundary: String = "draft_prompt_not_submitted",
    val positivePromptDraft: String = "",
    val negativePromptDraft: String = "",
    val referenceAssetCardIds: List<String> = emptyList(),
    val cameraMotion: String = "none",
    val durationMillis: Int = 0,
    val mustPreserve: List<String> = emptyList(),
    val forbiddenContent: List<String> = emptyList(),
    val reviewStatus: String = "draft_review_required",
)

@Serializable
data class ComicVideoAssetPromptCard(
    val assetCardId: String = "",
    val sourceAssetRef: String = "",
    val assetType: String = "visual_asset",
    val sourcePanelIds: List<String> = emptyList(),
    val publicDescription: String = "",
    val mustRemainStable: List<String> = emptyList(),
    val allowedVariation: List<String> = emptyList(),
    val forbiddenDrift: List<String> = emptyList(),
    val promptSafeDetails: List<String> = emptyList(),
    val authorOnlyExcludedCount: Int = 0,
    val reviewStatus: String = "draft_review_required",
)

@Serializable
data class ComicVideoVoiceStyleCard(
    val voiceCardId: String = "",
    val voiceId: String = "",
    val characterId: String = "",
    val displayName: String = "",
    val voiceLabel: String = "",
    val voiceBoundary: String = "voice_style_card_plan_only",
    val sourceCueIds: List<String> = emptyList(),
    val promptSafeNotes: List<String> = emptyList(),
    val reviewStatus: String = "draft_review_required",
)

@Serializable
data class ComicVideoProductionPackageDraftFinding(
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
data class ComicVideoProductionPackageDraftEvidence(
    val proofVerdict: String = "",
    val packetValidationVerdict: String = "",
    val packetPanelCount: Int = 0,
    val storyboardRowCount: Int = 0,
    val shotPromptCount: Int = 0,
    val assetCardCount: Int = 0,
    val voiceStyleCardCount: Int = 0,
    val audioCueCount: Int = 0,
    val subtitleCueCount: Int = 0,
    val totalDurationMillis: Int = 0,
    val forbiddenTextFindingCount: Int = 0,
)

@Serializable
data class ComicVideoProductionPackageValidationReport(
    val verdict: String,
    val validationId: String = "",
    val productionPackageDraftFile: String = "",
    val packageId: String = "",
    val packageStatus: String = "",
    val sourceImageFacingDeliveryProofId: String = "",
    val sourceReviewableStoryboardPacketId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val sourceVerdicts: ComicVideoPacketSourceVerdicts = ComicVideoPacketSourceVerdicts(),
    val storyboardProductionTable: List<ComicVideoStoryboardProductionRow> = emptyList(),
    val shotPromptPacketId: String = "",
    val shotPrompts: List<ComicVideoShotPromptDraft> = emptyList(),
    val assetPromptCards: List<ComicVideoAssetPromptCard> = emptyList(),
    val voiceStyleCards: List<ComicVideoVoiceStyleCard> = emptyList(),
    val downstreamBoundary: String = "comic_video_production_package_validation_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ComicVideoProductionPackageValidationFinding> = emptyList(),
    val evidence: ComicVideoProductionPackageValidationEvidence = ComicVideoProductionPackageValidationEvidence(),
)

@Serializable
data class ComicVideoProductionPackageValidationFinding(
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
data class ComicVideoProductionPackageValidationEvidence(
    val packageVerdict: String = "",
    val packageNextAllowedDownstreamStep: String = "",
    val sourceFieldCount: Int = 0,
    val storyboardRowCount: Int = 0,
    val uniqueShotIdCount: Int = 0,
    val shotPromptCount: Int = 0,
    val assetCardCount: Int = 0,
    val voiceStyleCardCount: Int = 0,
    val crossReferenceMismatchCount: Int = 0,
    val promptSafetyFindingCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
    val remoteOrProviderReferenceCount: Int = 0,
    val authorOnlyLeakCount: Int = 0,
    val totalDurationMillis: Int = 0,
)
