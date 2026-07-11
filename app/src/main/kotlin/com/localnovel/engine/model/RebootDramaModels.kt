package com.localnovel.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class DramaticQuestion(val question: String, val status: String = "OPEN")

@Serializable
data class ValueConflict(val valueA: String, val valueB: String, val forcedChoice: Boolean = false)

@Serializable
data class EmotionalWaveState(
    val previousBeat: String,
    val currentBeat: String,
    val nextDesiredBeat: String,
)

@Serializable
data class SceneValueState(val dimension: String, val valueBefore: Int, val valueAfter: Int)

@Serializable
data class PromisePayoffTracker(
    val promiseId: String,
    val promise: String,
    val plantedClue: String,
    val payoffStatus: String,
)

@Serializable
data class SceneDramaFrame(
    val sceneGoal: String,
    val dramaticQuestion: DramaticQuestion,
    val valueConflict: ValueConflict,
    val sceneValue: SceneValueState,
    val stakes: String,
    val cost: String = "",
    val irreversibleChange: String = "",
    val relationshipChange: String = "",
    val reveal: String = "",
    val emotionalBeat: String,
    val promises: List<PromisePayoffTracker> = emptyList(),
)

@Serializable
data class AudienceExpectationLedger(
    val obviousDanger: String,
    val characterRecognition: String,
    val firstLayerCountermeasure: String,
    val worldOrAntagonistAdaptation: String,
    val secondOrderSurprise: String,
    val retrospectiveClueValidity: String,
)

@Serializable
data class NarrativeSignals(
    val sarcasmPresent: Boolean = false,
    val banterPresent: Boolean = false,
    val genreDangerRecognized: Boolean = false,
    val omniscientKnowledgeUsed: Boolean = false,
    val riskTakingHasReason: Boolean = true,
    val cheapTwist: Boolean = false,
    val antagonistAdapts: Boolean = false,
    val secondOrderConsequence: Boolean = false,
    val warmBeat: Boolean = false,
    val seriousBeat: Boolean = false,
    val directorMetadataLeak: Boolean = false,
)

@Serializable
data class DramaCriticScores(
    val sarcasmVoiceFit: Int,
    val banterNaturalness: Int,
    val genreAwarenessWithoutOmniscience: Int,
    val stupidityDrivenPlotRisk: Int,
    val audiencePredictionRisk: Int,
    val fairSurprise: Int,
    val cheapTwistRisk: Int,
    val secondOrderConsequence: Int,
    val antagonistAdaptation: Int,
    val emotionalWaveCoverage: Int,
    val readerAttachment: Int,
    val overall: Int,
)

@Serializable
data class DramaCriticFinding(
    val code: String,
    val critic: String,
    val severity: String,
    val message: String,
    val suggestedFix: String,
)

@Serializable
data class DramaEvaluationInput(
    val evaluationId: String,
    val runId: String,
    val projectId: String,
    val sessionId: String,
    val turnNumber: Int,
    val scenarioSeed: String,
    val rawDraft: String,
    val revisedDraftCandidate: String = "",
    val rawSignals: NarrativeSignals,
    val revisedSignals: NarrativeSignals? = null,
    val sceneFrame: SceneDramaFrame,
    val previousFrames: List<SceneDramaFrame> = emptyList(),
    val expectationLedger: AudienceExpectationLedger,
    val priorRevisionCount: Int = 0,
)

@Serializable
data class DramaEvaluationReport(
    val evaluationId: String,
    val rawScores: DramaCriticScores,
    val revisedScores: DramaCriticScores? = null,
    val findings: List<DramaCriticFinding>,
    val revisionSuggestion: String,
    val revisionCount: Int,
    val selectedDraft: String,
    val selectedVersion: String,
    val stallDetected: Boolean,
    val allowedNextMoves: List<String> = emptyList(),
)
