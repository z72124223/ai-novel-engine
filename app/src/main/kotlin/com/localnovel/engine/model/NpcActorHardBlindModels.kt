package com.localnovel.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class NpcActorHardBlindTurn(
    val speakerId: String,
    val speakerLabel: String,
    val utterance: String = "",
    val nonverbal: String = "",
)

@Serializable
data class NpcActorHardBlindCandidate(
    val sourceType: String,
    val turns: List<NpcActorHardBlindTurn>,
)

@Serializable
data class NpcActorHardBlindScenario(
    val scenarioId: String,
    val title: String,
    val category: String,
    val boundary: String = "NON_CANON_EVALUATION",
    val actorId: String,
    val actorSpeakerId: String,
    val reviewerCharacterBrief: String,
    val sceneContext: String,
    val actorOwnedEvidence: List<NpcActorEvidenceRef>,
    val forbiddenEvidence: List<NpcActorEvidenceRef> = emptyList(),
    val sharedOpeningTurns: List<NpcActorHardBlindTurn>,
    val naturalControlCandidate: NpcActorHardBlindCandidate,
    val actorProfileCandidate: NpcActorHardBlindCandidate,
    val ratingDimensions: List<String>,
    val lengthExceptionReason: String? = null,
)

@Serializable
data class NpcActorHardBlindSuite(
    val schemaVersion: String = "1.0",
    val suiteId: String,
    val status: String = "NON_CANON_EVALUATION_READY",
    val fixtureOrigin: String = "MANUALLY_AUTHORED_OFFLINE_EVALUATION_FIXTURE",
    val randomizationSeed: String,
    val scenarios: List<NpcActorHardBlindScenario>,
)

@Serializable
data class NpcActorHardBlindReviewOption(
    val turns: List<NpcActorHardBlindTurn>,
)

@Serializable
data class NpcActorHardBlindReviewItem(
    val reviewId: String,
    val scenarioId: String,
    val title: String,
    val characterBrief: String,
    val sceneContext: String,
    val sharedOpeningTurns: List<NpcActorHardBlindTurn>,
    val optionA: NpcActorHardBlindReviewOption,
    val optionB: NpcActorHardBlindReviewOption,
    val ratingDimensions: List<String>,
    val ratingPrompt: String,
)

@Serializable
data class NpcActorHardBlindReviewPacket(
    val schemaVersion: String = "1.0",
    val packetId: String,
    val status: String = "HUMAN_REVIEW_REQUIRED",
    val sourceDisclosure: String = "Both options are manually authored offline evaluation fixtures; source identity is withheld until review completion.",
    val items: List<NpcActorHardBlindReviewItem>,
)

@Serializable
data class NpcActorHardBlindAnswer(
    val reviewId: String,
    val actorOption: String,
)

@Serializable
data class NpcActorHardBlindAnswerKey(
    val schemaVersion: String = "1.0",
    val packetId: String,
    val randomizationMethod: String = "SHA256_RANK_BALANCED",
    val randomizationSeed: String,
    val answers: List<NpcActorHardBlindAnswer>,
)

@Serializable
data class NpcActorHardBlindHumanRating(
    val reviewId: String,
    val selection: String,
    val flaggedDimensions: List<String> = emptyList(),
    val knowledgeBoundaryConcern: Boolean = false,
    val safetyBoundaryConcern: Boolean = false,
    val notes: String = "",
)

@Serializable
data class NpcActorHardBlindHumanReviewResult(
    val schemaVersion: String = "1.0",
    val packetId: String,
    val status: String = "COMPLETE_BEFORE_ANSWER_KEY_DISCLOSURE",
    val reviewerRole: String = "PRODUCT_OWNER",
    val answerKeyOpenedBeforeCompletion: Boolean = false,
    val ratings: List<NpcActorHardBlindHumanRating>,
)

@Serializable
data class NpcActorHardBlindRevealedRating(
    val reviewId: String,
    val selection: String,
    val actorOption: String,
    val outcome: String,
)

@Serializable
data class NpcActorHardBlindRevealedSummary(
    val schemaVersion: String = "1.0",
    val packetId: String,
    val status: String = "HUMAN_REVIEW_REVEALED",
    val actorPreferredCount: Int,
    val controlPreferredCount: Int,
    val tieCount: Int,
    val bothFailCount: Int,
    val knowledgeBoundaryConcernCount: Int,
    val safetyBoundaryConcernCount: Int,
    val overallDecision: String,
    val claimBoundary: List<String>,
    val itemResults: List<NpcActorHardBlindRevealedRating>,
)

@Serializable
data class NpcActorHardBlindTechnicalReport(
    val schemaVersion: String = "1.0",
    val suiteId: String,
    val scenarioCount: Int,
    val profileCount: Int,
    val categoryCount: Int,
    val actorOptionACount: Int,
    val actorOptionBCount: Int,
    val turnStructureViolationCount: Int,
    val strawmanPatternCount: Int,
    val forbiddenInformationViolationCount: Int,
    val lengthBalanceViolationCount: Int,
    val maximumSpokenLengthRatio: Double,
    val technicalDecision: String,
    val humanBlindReviewStatus: String,
    val claimBoundary: List<String>,
    val findings: List<String>,
)
