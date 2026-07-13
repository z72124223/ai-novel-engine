package com.localnovel.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class NpcEmbodiedZone(
    val zoneId: String,
    val label: String,
    val adjacentZoneIds: List<String>,
    val visibleZoneIds: List<String>,
)

@Serializable
data class NpcEmbodiedEntityState(
    val entityId: String,
    val label: String,
    val entityType: String,
    val zoneId: String,
    val state: String,
)

@Serializable
data class NpcEmbodiedEnvironmentBeat(
    val beatId: String,
    val tick: Int,
    val eventSummary: String,
    val visibleEntityIdsAdded: List<String> = emptyList(),
)

@Serializable
data class NpcEmbodiedStateChange(
    val entityId: String,
    val field: String,
    val before: String,
    val after: String,
)

@Serializable
data class NpcEmbodiedDecisionStep(
    val beatId: String,
    val perceivedEntityIds: List<String>,
    val chosenPriority: String,
    val actionType: String,
    val targetEntityIds: List<String>,
    val actionDescription: String,
    val utterance: String = "",
    val nonverbal: String = "",
    val stateChanges: List<NpcEmbodiedStateChange>,
    val otherActorReactions: List<String>,
    val resultingStateSummary: String,
)

@Serializable
data class NpcEmbodiedTrajectory(
    val sourceType: String,
    val steps: List<NpcEmbodiedDecisionStep>,
)

@Serializable
data class NpcEmbodiedRolloutScenario(
    val scenarioId: String,
    val title: String,
    val category: String,
    val boundary: String = "NON_CANON_EVALUATION",
    val actorId: String,
    val actorLabel: String,
    val reviewerCharacterBrief: String,
    val worldBrief: String,
    val zones: List<NpcEmbodiedZone>,
    val initialEntities: List<NpcEmbodiedEntityState>,
    val environmentBeats: List<NpcEmbodiedEnvironmentBeat>,
    val actorOwnedEvidence: List<NpcActorEvidenceRef>,
    val forbiddenEvidence: List<NpcActorEvidenceRef>,
    val naturalControlTrajectory: NpcEmbodiedTrajectory,
    val embodiedActorTrajectory: NpcEmbodiedTrajectory,
    val ratingDimensions: List<String>,
)

@Serializable
data class NpcEmbodiedRolloutSuite(
    val schemaVersion: String = "1.0",
    val suiteId: String,
    val status: String = "NON_CANON_EMBODIED_HOLDOUT_READY",
    val fixtureOrigin: String = "MANUALLY_AUTHORED_OFFLINE_EMBODIED_FIXTURE",
    val frozenPolicyId: String,
    val randomizationSeed: String,
    val scenarios: List<NpcEmbodiedRolloutScenario>,
)

@Serializable
data class NpcEmbodiedReviewStep(
    val beatId: String,
    val perceived: List<String>,
    val chosenPriority: String,
    val action: String,
    val utterance: String,
    val nonverbal: String,
    val otherActorReactions: List<String>,
    val resultingStateSummary: String,
)

@Serializable
data class NpcEmbodiedReviewTrajectory(
    val steps: List<NpcEmbodiedReviewStep>,
)

@Serializable
data class NpcEmbodiedReviewItem(
    val reviewId: String,
    val scenarioId: String,
    val title: String,
    val characterBrief: String,
    val worldBrief: String,
    val zoneMap: List<String>,
    val initialState: List<String>,
    val sharedEnvironmentBeats: List<NpcEmbodiedEnvironmentBeat>,
    val optionA: NpcEmbodiedReviewTrajectory,
    val optionB: NpcEmbodiedReviewTrajectory,
    val ratingDimensions: List<String>,
    val ratingPrompt: String,
)

@Serializable
data class NpcEmbodiedReviewPacket(
    val schemaVersion: String = "1.0",
    val packetId: String,
    val status: String = "HUMAN_REVIEW_REQUIRED",
    val sourceDisclosure: String = "Both options are manually authored offline scene trajectories; source identity is withheld until review completion.",
    val items: List<NpcEmbodiedReviewItem>,
)

@Serializable
data class NpcEmbodiedAnswer(
    val reviewId: String,
    val embodiedActorOption: String,
)

@Serializable
data class NpcEmbodiedAnswerKey(
    val schemaVersion: String = "1.0",
    val packetId: String,
    val randomizationMethod: String = "SHA256_RANK_BALANCED",
    val randomizationSeed: String,
    val answers: List<NpcEmbodiedAnswer>,
)

@Serializable
data class NpcEmbodiedTechnicalReport(
    val schemaVersion: String = "1.0",
    val suiteId: String,
    val scenarioCount: Int,
    val profileCount: Int,
    val beatCount: Int,
    val embodiedOptionACount: Int,
    val embodiedOptionBCount: Int,
    val priorScenarioIdOverlapCount: Int,
    val priorContentFingerprintOverlapCount: Int,
    val unknownReferenceViolationCount: Int,
    val visibilityViolationCount: Int,
    val adjacencyViolationCount: Int,
    val timingViolationCount: Int,
    val stateContinuityViolationCount: Int,
    val requiredBehaviorViolationCount: Int,
    val forbiddenInformationViolationCount: Int,
    val lengthBalanceViolationCount: Int,
    val maximumSpokenLengthRatio: Double,
    val technicalDecision: String,
    val humanReviewStatus: String,
    val claimBoundary: List<String>,
)

@Serializable
data class NpcEmbodiedHumanRating(
    val reviewId: String,
    val selection: String,
    val flaggedDimensions: List<String> = emptyList(),
    val knowledgeBoundaryConcern: Boolean = false,
    val safetyBoundaryConcern: Boolean = false,
    val notes: String = "",
)

@Serializable
data class NpcEmbodiedHumanReviewResult(
    val schemaVersion: String = "1.0",
    val packetId: String,
    val status: String = "COMPLETE_BEFORE_ANSWER_KEY_DISCLOSURE",
    val reviewerRole: String = "PRODUCT_OWNER",
    val answerKeyOpenedBeforeCompletion: Boolean = false,
    val ratings: List<NpcEmbodiedHumanRating>,
)
