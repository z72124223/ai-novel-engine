package com.localnovel.engine.model

import kotlinx.serialization.Serializable

@Serializable
enum class StoryChapterProductionStatus {
    DRAFT_REQUESTED,
    DRAFT_INGESTED,
    REVIEWS_COMPLETE,
    REVISION_INGESTED,
    SELECTED_PILOT_DRAFT,
    HUMAN_DECISION_REQUIRED,
    REJECTED,
}

@Serializable
enum class StoryChapterReviewerRole {
    LOGIC,
    CHARACTER,
    READER,
}

@Serializable
enum class StoryChapterReviewVerdict {
    PASS,
    REVISE,
    HUMAN_DECISION_REQUIRED,
}

@Serializable
data class StoryChapterProductionSeed(
    val productionId: String,
    val projectId: String,
    val sessionId: String,
    val scenarioSeed: String,
    val sourceCommitSha: String,
    val sourcePacketJson: String,
    val sourcePacketSha256: String,
)

@Serializable
data class StoryChapterDraftIntake(
    val productionId: String,
    val sourcePacketSha256: String,
    val draft: String,
    val draftSha256: String,
    val modelMetadataJson: String = "{}",
)

@Serializable
data class StoryChapterReviewRecord(
    val reviewId: String,
    val productionId: String,
    val reviewerRole: StoryChapterReviewerRole,
    val reviewerInstanceId: String,
    val sourceDraftSha256: String,
    val verdict: StoryChapterReviewVerdict,
    val reviewJson: String,
)

@Serializable
data class StoryChapterRevisionIntake(
    val productionId: String,
    val sourceDraftSha256: String,
    val revisedDraft: String,
    val revisedDraftSha256: String,
    val modelMetadataJson: String = "{}",
)

@Serializable
data class StoryChapterPilotSelection(
    val productionId: String,
    val selectedDraftSha256: String,
    val humanDecision: String,
    val humanActorId: String,
)

@Serializable
data class StoryChapterProductionView(
    val productionId: String,
    val projectId: String,
    val sessionId: String,
    val scenarioSeed: String,
    val sourceCommitSha: String,
    val sourcePacketJson: String,
    val sourcePacketSha256: String,
    val status: StoryChapterProductionStatus,
    val initialDraft: String,
    val initialDraftSha256: String,
    val revisedDraft: String,
    val revisedDraftSha256: String,
    val selectedDraft: String,
    val selectedDraftSha256: String,
    val revisionCount: Int,
    val humanDecision: String,
    val humanActorId: String,
    val canonStatus: String,
    val reviews: List<StoryChapterReviewRecord> = emptyList(),
)
