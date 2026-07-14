package com.localnovel.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class NpcEmbodiedInternalDimensionScore(
    val dimension: String,
    val optionA: Int,
    val optionB: Int,
)

@Serializable
data class NpcEmbodiedInternalRating(
    val reviewId: String,
    val selection: String,
    val dimensionScores: List<NpcEmbodiedInternalDimensionScore>,
    val evidence: List<String>,
    val issueTags: List<String>,
    val rationale: String,
)

@Serializable
data class NpcEmbodiedInternalReviewResult(
    val schemaVersion: String = "1.0",
    val artifactType: String = "NPC_ACTOR04_INTERNAL_REVIEW",
    val packetId: String,
    val status: String = "COMPLETE_BEFORE_ANSWER_KEY_DISCLOSURE",
    val reviewerId: String,
    val reviewerRole: String,
    val answerKeyReadBeforeCompletion: Boolean = false,
    val revisionNumber: Int = 0,
    val ratings: List<NpcEmbodiedInternalRating>,
    val claimBoundary: String = "Internal agent review only; not human preference or live-model proof.",
)

@Serializable
data class NpcEmbodiedInternalReviewerSelection(
    val reviewerId: String,
    val reviewerRole: String,
    val selection: String,
)

@Serializable
data class NpcEmbodiedInternalSceneAggregate(
    val reviewId: String,
    val reviewerSelections: List<NpcEmbodiedInternalReviewerSelection>,
    val consensusSelection: String,
    val embodiedActorOption: String,
    val outcome: String,
    val issueTags: List<String>,
)

@Serializable
data class NpcEmbodiedInternalReviewAggregate(
    val schemaVersion: String = "1.0",
    val artifactType: String = "NPC_ACTOR04_INTERNAL_REVIEW_AGGREGATE",
    val packetId: String,
    val status: String = "COMPLETE_AFTER_ANSWER_KEY_DISCLOSURE",
    val reviewerIds: List<String>,
    val reviewerRoles: List<String>,
    val packetSha256: String,
    val answerKeySha256: String,
    val reviewContentSha256: Map<String, String>,
    val revisionCount: Int,
    val scenes: List<NpcEmbodiedInternalSceneAggregate>,
    val actorWins: Int,
    val controlWins: Int,
    val ties: Int,
    val bothFail: Int,
    val noConsensus: Int,
    val overallDecision: String,
    val claimBoundary: List<String>,
)
