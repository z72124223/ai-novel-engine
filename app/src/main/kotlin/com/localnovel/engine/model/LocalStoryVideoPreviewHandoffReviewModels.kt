@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalStoryVideoPreviewHandoffReviewReport(
    val verdict: String,
    val reviewId: String = "",
    val sourceHandoffFile: String = "",
    val sourceHandoffId: String = "",
    val sourceStoryInputPackId: String = "",
    val handoffStatus: String = "",
    val checks: List<LocalStoryVideoPreviewHandoffReviewCheck> = emptyList(),
    val readinessSummary: LocalStoryVideoPreviewHandoffReviewReadinessSummary =
        LocalStoryVideoPreviewHandoffReviewReadinessSummary(),
    val rendererCompatibilitySummary: LocalStoryVideoPreviewHandoffReviewRendererCompatibilitySummary =
        LocalStoryVideoPreviewHandoffReviewRendererCompatibilitySummary(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val boundaries: LocalStoryVideoPreviewHandoffBoundaries = LocalStoryVideoPreviewHandoffBoundaries(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String = "local_story_video_preview_handoff_review_only_not_media_generation",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<LocalStoryVideoPreviewHandoffReviewFinding> = emptyList(),
)

@Serializable
data class LocalStoryVideoPreviewHandoffReviewCheck(
    val checkId: String = "",
    val status: String = "FAIL",
    val category: String = "",
    val message: String = "",
    val evidence: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredForReview: Boolean = true,
)

@Serializable
data class LocalStoryVideoPreviewHandoffReviewReadinessSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val readinessChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val readinessPass: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val readinessFail: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredReadinessChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredReadinessPass: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredReadinessFail: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val previewPanels: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val previewTargetDurationMillis: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val previewPanelDurationMillis: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val qaChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val qaPass: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val qaFail: Int = 0,
)

@Serializable
data class LocalStoryVideoPreviewHandoffReviewRendererCompatibilitySummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val ready: Boolean = false,
    val currentRendererExpectedInput: String = "",
    val currentDraftInput: String = "",
    val readyFields: List<String> = emptyList(),
    val missingFields: List<String> = emptyList(),
    val requiredChecksBeforeMp4: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val readyFalseAccepted: Boolean = true,
    val summary: String = "",
)

@Serializable
data class LocalStoryVideoPreviewHandoffReviewFinding(
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
