package com.localnovel.engine.service

import com.localnovel.engine.model.AudienceExpectationLedger
import com.localnovel.engine.model.DramaCriticFinding
import com.localnovel.engine.model.DramaCriticScores
import com.localnovel.engine.model.DramaEvaluationInput
import com.localnovel.engine.model.DramaEvaluationReport
import com.localnovel.engine.model.NarrativeSignals
import com.localnovel.engine.model.SceneDramaFrame
import com.localnovel.engine.repository.SqliteStoryRepository

class NarrativeStallDetector {
    fun isStalled(previous: List<SceneDramaFrame>, current: SceneDramaFrame): Boolean =
        (previous + current).takeLast(2).size == 2 &&
            (previous + current).takeLast(2).all { frame ->
                frame.sceneValue.valueBefore == frame.sceneValue.valueAfter &&
                    frame.cost.isBlank() &&
                    frame.irreversibleChange.isBlank() &&
                    frame.relationshipChange.isBlank() &&
                    frame.reveal.isBlank()
            }

    fun allowedNextMoves(stalled: Boolean): List<String> =
        if (stalled) listOf("ESCALATE", "CHOOSE", "REVEAL", "PAY_COST", "END_SCENE") else emptyList()
}

class LogicCritic {
    fun findings(signals: NarrativeSignals, ledger: AudienceExpectationLedger): List<DramaCriticFinding> = buildList {
        if (!signals.riskTakingHasReason) add(finding("IDIOT_PLOT_RISK", "critical", "Risk-taking lacks a character or situation reason.", "Add information, time, duty, relationship, false-belief, or cost pressure."))
        if (signals.omniscientKnowledgeUsed) add(finding("GENRE_AWARENESS_BECAME_OMNISCIENCE", "critical", "Genre awareness uses unavailable knowledge.", "Keep recognition limited to visible danger and owned knowledge."))
        if (signals.cheapTwist || ledger.retrospectiveClueValidity.isBlank()) add(finding("CHEAP_TWIST_RISK", "major", "The surprise lacks retrospective clue validity.", "Plant a clue and derive the second layer from world or antagonist adaptation."))
        if (signals.directorMetadataLeak) add(finding("DIRECTOR_METADATA_LEAK", "critical", "Director-only metadata entered reader-facing prose.", "Remove director metadata from character knowledge and narrative."))
        if (signals.authorMetaCommentary) add(finding("AUTHOR_META_COMMENTARY", "major", "A character quip became author or production commentary.", "Keep the quip grounded in visible circumstances and character knowledge."))
    }

    private fun finding(code: String, severity: String, message: String, fix: String) =
        DramaCriticFinding(code, "LogicCritic", severity, message, fix)
}

class CharacterDramaCritic {
    fun findings(signals: NarrativeSignals, frame: SceneDramaFrame): List<DramaCriticFinding> = buildList {
        if (!signals.genreDangerRecognized) add(DramaCriticFinding("OBVIOUS_DANGER_UNRECOGNIZED", "CharacterDramaCritic", "major", "The character ignores an obvious genre danger.", "Let the character recognize the danger without learning hidden facts."))
        if (frame.valueConflict.valueA.isBlank() || frame.valueConflict.valueB.isBlank()) add(DramaCriticFinding("VALUE_CONFLICT_MISSING", "CharacterDramaCritic", "major", "Scene lacks a real value conflict.", "Name two values that cannot both be preserved."))
        if (signals.sarcasmPresent && signals.seriousBeat) add(DramaCriticFinding("SARCASM_UNDERCUTS_PRESSURE", "CharacterDramaCritic", "minor", "Sarcasm may undercut a serious beat.", "Reduce jokes when pressure becomes real."))
        if (signals.sarcasmPresent && !signals.sarcasmCreatesSocialCost) add(DramaCriticFinding("SARCASM_WITHOUT_SOCIAL_COST", "CharacterDramaCritic", "minor", "The quip has no observable relationship consequence.", "Let another character react or make the speaker repair the social cost later."))
        if (signals.griefOrInjuryBeat && (!signals.griefRestraint || signals.sarcasmPresent)) add(DramaCriticFinding("GRIEF_RESTRAINT_FAILED", "CharacterDramaCritic", "major", "Humor or emotional distance weakens a real injury or grief beat.", "Suppress quips and respond through concrete care, silence, anger or acknowledged loss."))
        if (signals.dailyQuip && signals.dangerObservation) add(DramaCriticFinding("VOICE_MODE_COLLISION", "CharacterDramaCritic", "minor", "A daily quip is being used as the same evidence as danger recognition.", "Give daily friction and danger observation separate beats."))
    }
}

class ProsePacingCritic {
    fun findings(signals: NarrativeSignals, frame: SceneDramaFrame): List<DramaCriticFinding> = buildList {
        if (!signals.warmBeat && !signals.seriousBeat) add(DramaCriticFinding("EMOTIONAL_WAVE_FLAT", "ProsePacingCritic", "major", "No warm or serious emotional movement is marked.", "Create a contrast beat before the next escalation."))
        if (frame.emotionalBeat.isBlank()) add(DramaCriticFinding("EMOTIONAL_BEAT_MISSING", "ProsePacingCritic", "major", "Scene frame has no emotional beat.", "Record the emotional change caused by the scene."))
    }
}

class DramaScoringService {
    fun score(signals: NarrativeSignals, ledger: AudienceExpectationLedger): DramaCriticScores {
        val clueValid = ledger.retrospectiveClueValidity.isNotBlank()
        val scores = DramaCriticScores(
            sarcasmVoiceFit = if (signals.sarcasmPresent) 90 else 65,
            banterNaturalness = if (signals.banterPresent) 85 else 60,
            genreAwarenessWithoutOmniscience = when {
                signals.omniscientKnowledgeUsed -> 10
                signals.genreDangerRecognized -> 95
                else -> 50
            },
            stupidityDrivenPlotRisk = if (signals.riskTakingHasReason) 10 else 95,
            audiencePredictionRisk = if (signals.secondOrderConsequence) 35 else 75,
            fairSurprise = if (signals.secondOrderConsequence && clueValid && !signals.cheapTwist) 90 else 35,
            cheapTwistRisk = if (signals.cheapTwist || !clueValid) 90 else 10,
            secondOrderConsequence = if (signals.secondOrderConsequence) 90 else 35,
            antagonistAdaptation = if (signals.antagonistAdapts) 90 else 35,
            emotionalWaveCoverage = when {
                signals.warmBeat && signals.seriousBeat -> 95
                signals.warmBeat || signals.seriousBeat -> 70
                else -> 30
            },
            readerAttachment = if (signals.warmBeat) 85 else 55,
            overall = 0,
        )
        val qualityValues = listOf(
            scores.sarcasmVoiceFit,
            scores.banterNaturalness,
            scores.genreAwarenessWithoutOmniscience,
            100 - scores.stupidityDrivenPlotRisk,
            100 - scores.audiencePredictionRisk,
            scores.fairSurprise,
            100 - scores.cheapTwistRisk,
            scores.secondOrderConsequence,
            scores.antagonistAdaptation,
            scores.emotionalWaveCoverage,
            scores.readerAttachment,
        )
        return scores.copy(overall = qualityValues.average().toInt())
    }
}

class NarrativeRevisionCoordinator {
    fun evaluate(input: DramaEvaluationInput): DramaEvaluationReport {
        val scorer = DramaScoringService()
        val rawScores = scorer.score(input.rawSignals, input.expectationLedger)
        val findings = LogicCritic().findings(input.rawSignals, input.expectationLedger) +
            CharacterDramaCritic().findings(input.rawSignals, input.sceneFrame) +
            ProsePacingCritic().findings(input.rawSignals, input.sceneFrame)
        val suggestion = findings.joinToString(" ") { it.suggestedFix }.trim()
        val mayRevise = input.priorRevisionCount == 0 && input.revisedDraftCandidate.isNotBlank() && input.revisedSignals != null
        val revisedScores = if (mayRevise) scorer.score(input.revisedSignals, input.expectationLedger) else null
        val useRevision = revisedScores != null && revisedScores.overall > rawScores.overall
        val stall = NarrativeStallDetector().isStalled(input.previousFrames, input.sceneFrame)
        return DramaEvaluationReport(
            evaluationId = input.evaluationId,
            rawScores = rawScores,
            revisedScores = revisedScores,
            findings = findings,
            revisionSuggestion = suggestion,
            revisionCount = if (useRevision) 1 else 0,
            selectedDraft = if (useRevision) input.revisedDraftCandidate else input.rawDraft,
            selectedVersion = if (useRevision) "REVISED_ONCE" else if (input.priorRevisionCount > 0) "REVISION_LIMIT_REACHED" else "RAW",
            stallDetected = stall,
            allowedNextMoves = NarrativeStallDetector().allowedNextMoves(stall),
        )
    }
}

class RebootDramaLoopService(private val repository: SqliteStoryRepository) {
    fun evaluateAndPersist(input: DramaEvaluationInput): DramaEvaluationReport {
        require(input.priorRevisionCount in 0..1) { "priorRevisionCount must be zero or one." }
        val report = NarrativeRevisionCoordinator().evaluate(input)
        repository.recordDramaEvaluation(input, report)
        return report
    }
}
