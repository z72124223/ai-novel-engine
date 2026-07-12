package com.localnovel.engine.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

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
data class StoryChapterDraftFileIntake(
    val productionId: String,
    val sourcePacketSha256: String,
    val draftPath: String,
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
data class StoryChapterRevisionFileIntake(
    val productionId: String,
    val sourceDraftSha256: String,
    val revisedDraftPath: String,
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

@Serializable
data class StoryChapterWorldQuery(
    val text: String,
    val classification: String = "",
    val limit: Int = 8,
    val allowedSourceIds: List<String>,
)

@Serializable
data class StoryChapterHandoffRequest(
    val productionId: String,
    val projectId: String,
    val sessionId: String,
    val runId: String,
    val runtimeScenarioSeed: String,
    val scenarioSeed: String,
    val sourceCommitSha: String,
    val chapterTitle: String,
    val premise: String,
    val targetChineseCharactersMin: Int = 2500,
    val targetChineseCharactersMax: Int = 4500,
    val worldQueries: List<StoryChapterWorldQuery>,
    val requiredBeats: List<String>,
    val forbiddenOutcomes: List<String>,
    val voiceRequirements: List<String>,
)

@Serializable
data class StoryChapterSourcePacket(
    val packetVersion: String = "story-chapter-source-v1",
    val productionId: String,
    val projectId: String,
    val sessionId: String,
    val scenarioSeed: String,
    val sourceCommitSha: String,
    val chapterTitle: String,
    val premise: String,
    val targetChineseCharactersMin: Int,
    val targetChineseCharactersMax: Int,
    val requiredBeats: List<String>,
    val forbiddenOutcomes: List<String>,
    val voiceRequirements: List<String>,
    val runtimeContextId: String,
    val runtimeContextHash: String,
    val runtimeContextJson: String,
    val worldSourceQueries: List<WorldSourceQueryReport>,
    val boundary: String = "NON_CANON_PILOT_NO_DIRECT_SQL_WRITES",
)

@Serializable
data class StoryChapterHandoffArtifact(
    val packet: StoryChapterSourcePacket,
    val sourcePacketSha256: String,
)

@Serializable
data class StoryChapterReviewBatch(
    val productionId: String,
    val reviews: List<StoryChapterReviewRecord>,
)

@Serializable
data class StoryChapterReviewFileBatch(
    val productionId: String,
    val reviewFiles: List<String>,
)

@Serializable
data class StoryChapterFullTextReview(
    val reviewVersion: String = "story-chapter-review-v1",
    val reviewerRole: StoryChapterReviewerRole,
    val reviewerInstanceId: String,
    val sourceDraftSha256: String,
    val verdict: StoryChapterReviewVerdict,
    val summary: String,
    val quotedEvidence: List<String>,
    val strengths: List<String>,
    val findings: List<String>,
    val requiredChanges: List<String> = emptyList(),
    val scorecard: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class StoryChapterDecisionArtifact(
    val artifactVersion: String = "story-chapter-decision-v1",
    val productionId: String,
    val decision: String,
    val selectedDraftSha256: String = "",
    val actorId: String,
    val sourceStatus: StoryChapterProductionStatus,
    val canonStatus: String = "NON_CANON_PILOT",
    val createdAt: String,
)

@Serializable
data class StoryChapterDecisionEnvelope(
    val artifact: StoryChapterDecisionArtifact,
    val artifactSha256: String,
)
