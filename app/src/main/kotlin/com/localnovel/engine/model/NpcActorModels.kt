package com.localnovel.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class NpcVoiceProfile(
    val locale: String = "zh-Hant-TW",
    val register: String = "natural_spoken_mandarin",
    val principles: List<String> = emptyList(),
    val preferredPatterns: List<String> = emptyList(),
    val forbiddenPatterns: List<String> = emptyList(),
)

@Serializable
data class NpcActorEvidenceRef(
    val evidenceId: String,
    val kind: String,
    val summary: String,
    val visibility: String,
    val sourceSha256: String? = null,
)

@Serializable
data class NpcActorOutputConstraints(
    val maxUtteranceChars: Int = 48,
    val silenceAllowed: Boolean = true,
    val lieAllowed: Boolean = false,
    val nonverbalAllowed: Boolean = true,
    val forbiddenPatterns: List<String> = emptyList(),
)

@Serializable
data class NpcActorRequest(
    val protocolVersion: String = "1.0",
    val requestId: String,
    val scenarioId: String,
    val actorId: String,
    val targetIds: List<String> = emptyList(),
    val locationId: String,
    val voiceProfile: NpcVoiceProfile,
    val physicalState: String,
    val emotionalState: String,
    val perceivedFacts: List<NpcActorEvidenceRef> = emptyList(),
    val ownedMemories: List<NpcActorEvidenceRef> = emptyList(),
    val ownedBeliefs: List<NpcActorEvidenceRef> = emptyList(),
    val goals: List<NpcActorEvidenceRef> = emptyList(),
    val relationships: List<NpcActorEvidenceRef> = emptyList(),
    val recentDialogue: List<NpcActorEvidenceRef> = emptyList(),
    val capabilities: List<NpcActorEvidenceRef> = emptyList(),
    val revealableInformationIds: List<String> = emptyList(),
    val withholdableInformationIds: List<String> = emptyList(),
    val forbiddenInformationIds: List<String> = emptyList(),
    val outputConstraints: NpcActorOutputConstraints = NpcActorOutputConstraints(),
)

@Serializable
data class NpcActorDecisionSummary(
    val evidenceIds: List<String> = emptyList(),
    val selectedActionLabel: String,
    val rejectedActionLabels: List<String> = emptyList(),
)

@Serializable
data class NpcActorResponse(
    val protocolVersion: String = "1.0",
    val requestId: String,
    val actorId: String,
    val targetIds: List<String> = emptyList(),
    val speechAct: String,
    val immediateGoal: String,
    val beliefClaimIds: List<String> = emptyList(),
    val memoryIds: List<String> = emptyList(),
    val emotionShown: String,
    val emotionHidden: String,
    val withheldInformationIds: List<String> = emptyList(),
    val actionType: String,
    val actionIntent: String,
    val visibleAction: String,
    val utterance: String,
    val nonverbalAction: String,
    val interruptible: Boolean = true,
    val silenceAllowed: Boolean = true,
    val capabilityUseChecks: List<CapabilityUseCheck> = emptyList(),
    val predictedCost: String,
    val observableConsequence: String,
    val confidence: Int,
    val decisionSummary: NpcActorDecisionSummary,
)

@Serializable
data class NpcActorValidationResult(
    val accepted: Boolean,
    val status: String,
    val findings: List<String> = emptyList(),
    val proposal: NpcActionProposal? = null,
)

@Serializable
data class NpcActorExpectedBehavior(
    val preferredSpeechActs: List<String> = emptyList(),
    val silenceOrEvasionAppropriate: Boolean = false,
    val nonverbalAppropriate: Boolean = false,
)

@Serializable
data class NpcActorEvaluationScenario(
    val scenarioId: String,
    val title: String,
    val category: String,
    val boundary: String = "NON_CANON_EVALUATION",
    val request: NpcActorRequest,
    val baselineResponse: NpcActorResponse,
    val actorResponse: NpcActorResponse,
    val expectedBehavior: NpcActorExpectedBehavior = NpcActorExpectedBehavior(),
)

@Serializable
data class NpcActorEvaluationSuite(
    val schemaVersion: String = "1.0",
    val suiteId: String,
    val status: String = "NON_CANON_EVALUATION_READY",
    val scenarios: List<NpcActorEvaluationScenario>,
)

@Serializable
data class NpcActorMetricSummary(
    val outputCount: Int,
    val validationFailureCount: Int,
    val knowledgeLeakCount: Int,
    val continuityViolationCount: Int,
    val expositionPatternCount: Int,
    val repeatedContrastPatternCount: Int,
    val averageUtteranceChars: Double,
    val p95UtteranceChars: Int,
    val expectedBehaviorMatchCount: Int,
)

@Serializable
data class NpcActorEvaluationReport(
    val schemaVersion: String = "1.0",
    val suiteId: String,
    val scenarioCount: Int,
    val baseline: NpcActorMetricSummary,
    val actor: NpcActorMetricSummary,
    val technicalDecision: String,
    val humanBlindReviewStatus: String,
    val findings: List<String> = emptyList(),
)

@Serializable
data class NpcActorBlindReviewItem(
    val reviewId: String,
    val scenarioId: String,
    val context: String,
    val optionA: String,
    val optionB: String,
    val ratingPrompt: String,
)

@Serializable
data class NpcActorBlindReviewPacket(
    val schemaVersion: String = "1.0",
    val packetId: String,
    val status: String = "HUMAN_REVIEW_REQUIRED",
    val items: List<NpcActorBlindReviewItem>,
)

@Serializable
data class NpcActorBlindReviewAnswer(
    val reviewId: String,
    val actorOption: String,
)

@Serializable
data class NpcActorBlindReviewAnswerKey(
    val schemaVersion: String = "1.0",
    val packetId: String,
    val answers: List<NpcActorBlindReviewAnswer>,
)
