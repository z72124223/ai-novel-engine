package com.localnovel.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class NpcActorCalibrationPolicyRule(
    val ruleId: String,
    val instruction: String,
)

@Serializable
data class NpcActorCalibrationPolicy(
    val schemaVersion: String = "1.0",
    val policyId: String,
    val revisionNumber: Int = 1,
    val status: String = "EVALUATION_ONLY",
    val decisionOrder: List<String>,
    val rules: List<NpcActorCalibrationPolicyRule>,
    val claimBoundary: List<String>,
)

@Serializable
data class NpcActorCalibrationTurn(
    val speakerId: String,
    val speakerLabel: String,
    val utterance: String = "",
    val nonverbal: String = "",
)

@Serializable
data class NpcActorCalibrationSignals(
    val immediateNeed: String,
    val relationshipDistance: String,
    val audiencePressure: String,
    val urgency: String,
    val expressionLevel: String,
)

@Serializable
data class NpcActorCalibrationCandidate(
    val sourceType: String,
    val turns: List<NpcActorCalibrationTurn>,
)

@Serializable
data class NpcActorCalibrationScenario(
    val scenarioId: String,
    val title: String,
    val category: String,
    val boundary: String = "NON_CANON_EVALUATION",
    val policyId: String,
    val actorId: String,
    val actorSpeakerId: String,
    val reviewerCharacterBrief: String,
    val sceneContext: String,
    val calibrationSignals: NpcActorCalibrationSignals,
    val actorOwnedEvidence: List<NpcActorEvidenceRef>,
    val forbiddenEvidence: List<NpcActorEvidenceRef> = emptyList(),
    val sharedOpeningTurns: List<NpcActorCalibrationTurn>,
    val naturalControlCandidate: NpcActorCalibrationCandidate,
    val adaptiveActorCandidate: NpcActorCalibrationCandidate,
    val ratingDimensions: List<String>,
    val lengthExceptionReason: String? = null,
)

@Serializable
data class NpcActorCalibrationSuite(
    val schemaVersion: String = "1.0",
    val suiteId: String,
    val status: String = "NON_CANON_HOLDOUT_READY",
    val fixtureOrigin: String = "MANUALLY_AUTHORED_OFFLINE_HOLDOUT_FIXTURE",
    val policyId: String,
    val randomizationSeed: String,
    val scenarios: List<NpcActorCalibrationScenario>,
)

@Serializable
data class NpcActorCalibrationReviewOption(
    val turns: List<NpcActorCalibrationTurn>,
)

@Serializable
data class NpcActorCalibrationReviewItem(
    val reviewId: String,
    val scenarioId: String,
    val title: String,
    val characterBrief: String,
    val sceneContext: String,
    val sharedOpeningTurns: List<NpcActorCalibrationTurn>,
    val optionA: NpcActorCalibrationReviewOption,
    val optionB: NpcActorCalibrationReviewOption,
    val ratingDimensions: List<String>,
    val ratingPrompt: String,
)

@Serializable
data class NpcActorCalibrationReviewPacket(
    val schemaVersion: String = "1.0",
    val packetId: String,
    val status: String = "HUMAN_REVIEW_REQUIRED",
    val sourceDisclosure: String = "Both options are manually authored offline holdout fixtures; source identity is withheld until review completion.",
    val items: List<NpcActorCalibrationReviewItem>,
)

@Serializable
data class NpcActorCalibrationAnswer(
    val reviewId: String,
    val adaptiveActorOption: String,
)

@Serializable
data class NpcActorCalibrationAnswerKey(
    val schemaVersion: String = "1.0",
    val packetId: String,
    val randomizationMethod: String = "SHA256_RANK_BALANCED",
    val randomizationSeed: String,
    val answers: List<NpcActorCalibrationAnswer>,
)

@Serializable
data class NpcActorCalibrationTechnicalReport(
    val schemaVersion: String = "1.0",
    val suiteId: String,
    val policyId: String,
    val scenarioCount: Int,
    val profileCount: Int,
    val adaptiveOptionACount: Int,
    val adaptiveOptionBCount: Int,
    val developmentScenarioIdOverlapCount: Int,
    val developmentContentFingerprintOverlapCount: Int,
    val turnStructureViolationCount: Int,
    val strawmanPatternCount: Int,
    val forbiddenInformationViolationCount: Int,
    val lengthBalanceViolationCount: Int,
    val maximumSpokenLengthRatio: Double,
    val technicalDecision: String,
    val humanBlindReviewStatus: String,
    val claimBoundary: List<String>,
)

@Serializable
data class NpcActorCalibrationHumanRating(
    val reviewId: String,
    val selection: String,
    val flaggedDimensions: List<String> = emptyList(),
    val knowledgeBoundaryConcern: Boolean = false,
    val safetyBoundaryConcern: Boolean = false,
    val notes: String = "",
)

@Serializable
data class NpcActorCalibrationHumanReviewResult(
    val schemaVersion: String = "1.0",
    val packetId: String,
    val status: String = "COMPLETE_BEFORE_ANSWER_KEY_DISCLOSURE",
    val reviewerRole: String = "PRODUCT_OWNER",
    val answerKeyOpenedBeforeCompletion: Boolean = false,
    val ratings: List<NpcActorCalibrationHumanRating>,
)
