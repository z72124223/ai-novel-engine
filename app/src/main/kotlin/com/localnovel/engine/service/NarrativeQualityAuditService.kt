package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.NarrativeQualityAuditEvidence
import com.localnovel.engine.model.NarrativeQualityAuditReport
import com.localnovel.engine.model.NarrativeQualityFinding
import com.localnovel.engine.model.allDeltaOperations
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.encodeToString

data class NarrativeQualityAuditInput(
    val contextFile: String = "",
    val resultFile: String = "",
    val evidenceFile: String = "",
    val evidenceText: String = "",
)

class NarrativeQualityAuditService {
    fun audit(
        context: ContextPacket,
        result: AiTurnResult,
        input: NarrativeQualityAuditInput = NarrativeQualityAuditInput(),
    ): NarrativeQualityAuditReport {
        val findings = buildList {
            addAll(checkNpcLiveliness(context, result, input))
            addAll(checkAiFeeling(result, input))
            addAll(checkWorldAutonomy(context, result, input))
            addAll(checkContextQuality(context, result, input))
            addAll(checkKnowledgeBoundaries(context, result, input))
            addAll(checkStyle(result, input))
            addAll(checkConsequences(result, input))
            addAll(checkPilotEvidence(input))
        }.distinctBy { "${it.ruleId}:${it.affectedFileOrTurn}:${it.evidence.joinToString("|")}" }
            .sortedWith(compareBy<NarrativeQualityFinding> { severityRank(it.severity) }.thenBy { it.ruleId })

        val score = findings.fold(100) { current, finding -> (current - scorePenalty(finding.severity)).coerceAtLeast(0) }
        val severity = findings.minByOrNull { severityRank(it.severity) }?.severity ?: "INFO"
        val verdict = when {
            findings.any { it.severity == "CRITICAL" } -> "FAIL"
            score >= 85 -> "PASS"
            score >= 65 -> "PASS_WITH_WARNINGS"
            else -> "NEEDS_REVIEW"
        }

        return NarrativeQualityAuditReport(
            verdict = verdict,
            score = score,
            severity = severity,
            findings = findings,
            evidence = NarrativeQualityAuditEvidence(
                contextFile = input.contextFile,
                resultFile = input.resultFile,
                evidenceFile = input.evidenceFile,
                projectId = context.project.id,
                turnId = context.turnId,
                contextId = context.contextId,
                resultId = result.resultId,
                turnNumber = context.turnNumber,
                narrativeLength = result.narrative.length,
                characterActionCount = result.characterActions.size,
                stateDeltaCount = result.allDeltaOperations().size,
                retrievedMemoryCount = context.retrievedMemories.size,
                knowledgeClaimCount = context.knowledgeClaims.size,
                activeThreadCount = context.activeStoryThreads.size,
                pendingWorldEventCount = context.pendingWorldEvents.size,
            ),
        )
    }

    private fun checkNpcLiveliness(
        context: ContextPacket,
        result: AiTurnResult,
        input: NarrativeQualityAuditInput,
    ): List<NarrativeQualityFinding> = buildList {
        val nonPlayerCharacters = context.characters.filterNot { it.isPlayer }
        val nonPlayerStates = context.characterStates.filter { state ->
            nonPlayerCharacters.any { it.id == state.characterId }
        }
        val actions = result.characterActions.filter { action ->
            nonPlayerCharacters.any { it.id == action.characterId }
        }

        if (nonPlayerCharacters.isNotEmpty() && actions.isEmpty()) {
            add(
                finding(
                    ruleId = "NPC_LIVELINESS_MISSING_ACTION",
                    category = "npc_liveliness",
                    severity = "MAJOR",
                    message = "NPC exists in context but result contains no characterActions for NPC agency evidence.",
                    evidence = nonPlayerCharacters.map { it.id },
                    suggestedFix = "Add characterActions showing each active NPC intent, visible action, motive boundary, and evidence.",
                    input = input,
                    turn = context.turnNumber,
                ),
            )
        }

        actions.filter { it.intent.isBlank() || it.visibleAction.isBlank() }.forEach { action ->
            add(
                finding(
                    ruleId = "NPC_LIVELINESS_WEAK_ACTION",
                    category = "npc_liveliness",
                    severity = "WARNING",
                    message = "NPC action lacks intent or visible action.",
                    evidence = listOf(action.characterId, "intent=${action.intent}", "visibleAction=${action.visibleAction}"),
                    suggestedFix = "Ground the NPC response in a concrete intent and visible behavior.",
                    input = input,
                    turn = context.turnNumber,
                ),
            )
        }

        actions.filter { it.characterFitScore in 1..59 }.forEach { action ->
            add(
                finding(
                    ruleId = "NPC_LIVELINESS_LOW_FIT_SCORE",
                    category = "npc_liveliness",
                    severity = "WARNING",
                    message = "NPC characterFitScore is low.",
                    evidence = listOf(action.characterId, "characterFitScore=${action.characterFitScore}"),
                    suggestedFix = "Revise the result so the action follows the NPC goal, emotion, memory, or knowledge.",
                    input = input,
                    turn = context.turnNumber,
                ),
            )
        }

        val missingGoals = nonPlayerStates.filter { it.flags["current_goal"].isNullOrBlank() }
        if (nonPlayerStates.isNotEmpty() && missingGoals.isNotEmpty()) {
            add(
                finding(
                    ruleId = "NPC_LIVELINESS_CONTEXT_GOAL_MISSING",
                    category = "npc_liveliness",
                    severity = "WARNING",
                    message = "Context lacks current_goal for one or more active NPC states.",
                    evidence = missingGoals.map { it.characterId },
                    suggestedFix = "Ensure future context includes current NPC goals before expecting strong agency.",
                    input = input,
                    turn = context.turnNumber,
                ),
            )
        }

        val convenienceTerms = listOf("立刻答應", "毫不猶豫地答應", "完全配合", "照你的意思", "順著你的話")
        val matched = convenienceTerms.filter { containsUnnegatedTerm(result.narrative, it) }
        if (matched.isNotEmpty()) {
            add(
                finding(
                    ruleId = "NPC_TOOLMAN_CONVENIENT_COMPLIANCE",
                    category = "npc_liveliness",
                    severity = "WARNING",
                    message = "Narrative may make an NPC conveniently serve the player without resistance evidence.",
                    evidence = matched,
                    suggestedFix = "Show why the NPC accepts, refuses, modifies, or conditions the player's request from their own motive.",
                    input = input,
                    turn = context.turnNumber,
                ),
            )
        }
    }

    private fun checkContextQuality(
        context: ContextPacket,
        result: AiTurnResult,
        input: NarrativeQualityAuditInput,
    ): List<NarrativeQualityFinding> = buildList {
        val hasSettingPressure = context.settingContextBudget.omittedSettingCount > 0 ||
            context.contextBudget.omittedSettingCount > 0 ||
            context.availableTransitions.isNotEmpty() ||
            context.pendingWorldEvents.isNotEmpty() ||
            context.activeStoryThreads.isNotEmpty()
        if (context.turnNumber > 0 && context.settingContextBudget.actualBytes <= 16 && hasSettingPressure) {
            add(
                finding(
                    ruleId = "CONTEXT_SETTING_TOO_THIN",
                    category = "context_quality",
                    severity = "WARNING",
                    message = "Setting context is nearly empty while runtime-relevant setting pressure appears to exist.",
                    evidence = listOf(
                        "settingBytes=${context.settingContextBudget.actualBytes}/${context.settingContextBudget.maxBytes}",
                        "includedSettings=${context.settingContextBudget.includedSettingCount}",
                        "omittedSettings=${context.settingContextBudget.omittedSettingCount}",
                        "contextOmittedSettings=${context.contextBudget.omittedSettingCount}",
                    ),
                    suggestedFix = "Review context selection so relevant public setting constraints are preserved without loading the full setting pack.",
                    input = input,
                    turn = context.turnNumber,
                ),
            )
        }

        val transitionIntentTerms = listOf("退到", "撤到", "退往", "門外", "離開", "跨過門檻", "轉場", "退路", "撤離", "門口")
        val transitionSurface = context.playerInput + "\n" + result.narrative
        val transitionTerms = transitionIntentTerms.filter { transitionSurface.contains(it) }
        if (transitionTerms.isNotEmpty() && context.availableTransitions.isEmpty() && result.transitionProposal == null) {
            add(
                finding(
                    ruleId = "CONTEXT_TRANSITION_UNAVAILABLE",
                    category = "context_quality",
                    severity = "WARNING",
                    message = "Turn mentions retreat, exit, or transition intent but context contains no available transition.",
                    evidence = transitionTerms.distinct().take(5),
                    suggestedFix = "Check whether a legal transition definition, location connection, and exit gate should be available, or record why staying in the scene is correct.",
                    input = input,
                    turn = context.turnNumber,
                ),
            )
        }

        if (context.turnNumber >= 4 && context.activeStoryThreads.isNotEmpty() && context.pendingWorldEvents.isEmpty()) {
            add(
                finding(
                    ruleId = "CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE",
                    category = "context_quality",
                    severity = "WARNING",
                    message = "Active story threads exist at a later turn, but no pending scheduled world event pressure is present.",
                    evidence = context.activeStoryThreads.map { "${it.threadId}:${it.status}:progress=${it.progress}" },
                    suggestedFix = "If the scene has stalled, consider whether an already-defined scheduled world event should become visible pressure; do not invent one in the result.",
                    input = input,
                    turn = context.turnNumber,
                ),
            )
        }

        val sceneStillActive = context.sceneLifecycle.status == "ACTIVE" && !context.sceneLifecycle.readyToClose
        if (context.turnNumber >= 8 &&
            sceneStillActive &&
            context.activeStoryThreads.isNotEmpty() &&
            context.availableTransitions.isEmpty() &&
            context.pendingWorldEvents.isEmpty() &&
            result.transitionProposal == null &&
            result.worldEventAcknowledgements.isEmpty() &&
            result.timeAdvanceProposal == null
        ) {
            add(
                finding(
                    ruleId = "CONTEXT_PROCEDURAL_STASIS_RISK",
                    category = "context_quality",
                    severity = "WARNING",
                    message = "Late active scene has thread pressure but no transition, world event, or time-pressure surface.",
                    evidence = listOf(
                        "turnNumber=${context.turnNumber}",
                        "sceneStatus=${context.sceneLifecycle.status}",
                        "readyToClose=${context.sceneLifecycle.readyToClose}",
                    ) + context.activeStoryThreads.map { "${it.threadId}:progress=${it.progress}" },
                    suggestedFix = "Review whether the scene needs a legal exit, scheduled pressure, or explicit blocked-state evidence to avoid procedural stasis.",
                    input = input,
                    turn = context.turnNumber,
                ),
            )
        }
    }

    private fun checkAiFeeling(result: AiTurnResult, input: NarrativeQualityAuditInput): List<NarrativeQualityFinding> = buildList {
        val narrative = result.narrative
        val summaryTerms = listOf("總之", "可以看出", "這意味著", "這代表", "換言之", "從某種意義上")
        val summaryMatches = summaryTerms.filter { narrative.contains(it) }
        if (summaryMatches.isNotEmpty()) {
            add(
                finding(
                    ruleId = "AI_FEELING_OVER_SUMMARY",
                    category = "ai_feeling",
                    severity = "WARNING",
                    message = "Narrative contains explicit summary/explanation phrases.",
                    evidence = summaryMatches,
                    suggestedFix = "Replace abstract summary with concrete sensory detail, visible behavior, or consequence.",
                    input = input,
                ),
            )
        }

        val explainTerms = listOf("因為", "所以", "因此", "代表著", "象徵著", "說明了")
        val explainCount = explainTerms.sumOf { term -> narrative.windowed(term.length, 1).count { it == term } }
        if (explainCount >= 4) {
            add(
                finding(
                    ruleId = "AI_FEELING_OVER_EXPLAINED",
                    category = "ai_feeling",
                    severity = "WARNING",
                    message = "Narrative may explain too much instead of dramatizing action.",
                    evidence = listOf("explanationTermCount=$explainCount"),
                    suggestedFix = "Let action, silence, dialogue, and state deltas carry part of the meaning.",
                    input = input,
                ),
            )
        }

        val directEmotionTerms = listOf("害怕", "恐懼", "悲傷", "憤怒", "焦慮", "緊張", "震驚", "崩潰")
        val directEmotionMatches = directEmotionTerms.filter { narrative.contains(it) }
        val actionCueTerms = listOf("指節", "退", "停", "看", "握", "低聲", "沉默", "皺眉", "吸氣", "呼吸")
        if (directEmotionMatches.size >= 2 && actionCueTerms.none { narrative.contains(it) }) {
            add(
                finding(
                    ruleId = "AI_FEELING_EMOTION_TOLD_NOT_SHOWN",
                    category = "ai_feeling",
                    severity = "WARNING",
                    message = "Narrative names emotions without enough visible behavior.",
                    evidence = directEmotionMatches,
                    suggestedFix = "Show emotion through action, gesture, voice, posture, or concrete choice.",
                    input = input,
                ),
            )
        }
    }

    private fun checkWorldAutonomy(
        context: ContextPacket,
        result: AiTurnResult,
        input: NarrativeQualityAuditInput,
    ): List<NarrativeQualityFinding> = buildList {
        if (context.pendingWorldEvents.isNotEmpty() && result.worldEventAcknowledgements.isEmpty()) {
            add(
                finding(
                    ruleId = "WORLD_AUTONOMY_PENDING_EVENT_UNUSED",
                    category = "world_autonomy",
                    severity = "MAJOR",
                    message = "Context contains pendingWorldEvents but result does not acknowledge any world event.",
                    evidence = context.pendingWorldEvents.map { it.eventId },
                    suggestedFix = "If the event is due and visible, acknowledge its public pressure or explain why it remains untriggered.",
                    input = input,
                    turn = context.turnNumber,
                ),
            )
        }

        if (context.activeStoryThreads.isNotEmpty() &&
            result.threadProgressUpdates.isEmpty() &&
            result.sceneProgressUpdates.isEmpty() &&
            result.allDeltaOperations().none { it.targetType == "event" || it.type.name == "ADD_EVENT" }
        ) {
            add(
                finding(
                    ruleId = "WORLD_AUTONOMY_THREADS_IDLE",
                    category = "world_autonomy",
                    severity = "WARNING",
                    message = "Active story threads exist but result records no thread, scene, or event pressure.",
                    evidence = context.activeStoryThreads.map { "${it.threadId}:${it.status}" },
                    suggestedFix = "Record progress, blockage, or pressure evidence when a thread matters to the turn.",
                    input = input,
                    turn = context.turnNumber,
                ),
            )
        }

        val protagonistOnlyTerms = listOf("只因你", "因你出現", "因為你在場", "所有人都看向你")
        val matched = protagonistOnlyTerms.filter { result.narrative.contains(it) }
        if (matched.isNotEmpty() && context.pendingWorldEvents.isEmpty() && context.activeStoryThreads.isEmpty()) {
            add(
                finding(
                    ruleId = "WORLD_AUTONOMY_PROTAGONIST_ONLY_PRESSURE",
                    category = "world_autonomy",
                    severity = "WARNING",
                    message = "World pressure appears to exist only because the protagonist is present.",
                    evidence = matched,
                    suggestedFix = "Tie external pressure to scene requirements, world events, faction rules, or NPC duties.",
                    input = input,
                    turn = context.turnNumber,
                ),
            )
        }
    }

    private fun checkKnowledgeBoundaries(
        context: ContextPacket,
        result: AiTurnResult,
        input: NarrativeQualityAuditInput,
    ): List<NarrativeQualityFinding> = buildList {
        val resultSurface = JsonSupport.json.encodeToString(result)
        val forbiddenTexts = context.sceneContract?.forbiddenReveals.orEmpty().filter { it.isNotBlank() }
        val forbiddenMatches = forbiddenTexts.filter { resultSurface.contains(it) }
        if (forbiddenMatches.isNotEmpty()) {
            add(
                finding(
                    ruleId = "KNOWLEDGE_FORBIDDEN_REVEAL",
                    category = "knowledge_boundary",
                    severity = "CRITICAL",
                    message = "Result surface contains forbidden reveal text from the context scene contract.",
                    evidence = forbiddenMatches,
                    suggestedFix = "Remove the forbidden reveal or wait until runtime reveal conditions allow it.",
                    input = input,
                    turn = context.turnNumber,
                ),
            )
        }

        val knownFactIds = context.facts.map { it.id }.toSet() +
            context.revealedSecrets.map { it.id }.toSet() +
            result.revealedFacts.map { it.factId }.toSet()
        result.characterActions
            .flatMap { action -> action.usedFactIds.map { action.characterId to it } }
            .filter { (_, factId) -> factId !in knownFactIds }
            .forEach { (characterId, factId) ->
                add(
                    finding(
                        ruleId = "KNOWLEDGE_UNKNOWN_FACT_USED",
                        category = "knowledge_boundary",
                        severity = "MAJOR",
                        message = "Character action references a fact that is not visible in context or revealed by result.",
                        evidence = listOf("character=$characterId", "factId=$factId"),
                        suggestedFix = "Use visible facts only, or add a legal revealedFact/state delta first.",
                        input = input,
                        turn = context.turnNumber,
                    ),
                )
            }

        val knownClaimIds = context.knowledgeClaims.map { it.claimId }.toSet()
        result.characterActions
            .flatMap { action -> action.usedKnowledgeClaimIds.map { action.characterId to it } }
            .filter { (_, claimId) -> claimId !in knownClaimIds }
            .forEach { (characterId, claimId) ->
                add(
                    finding(
                        ruleId = "KNOWLEDGE_UNKNOWN_CLAIM_USED",
                        category = "knowledge_boundary",
                        severity = "MAJOR",
                        message = "Character action references a knowledge claim that is not in context.",
                        evidence = listOf("character=$characterId", "claimId=$claimId"),
                        suggestedFix = "Ground the action in selected knowledgeClaims or visible scene evidence.",
                        input = input,
                        turn = context.turnNumber,
                    ),
                )
            }

        val retrievedMemoryIds = context.retrievedMemories.map { it.memoryId }.toSet() +
            context.characterMemories.map { it.id }.toSet()
        result.characterActions
            .flatMap { action -> action.usedMemoryIds.map { action.characterId to it } }
            .filter { (_, memoryId) -> memoryId !in retrievedMemoryIds }
            .forEach { (characterId, memoryId) ->
                add(
                    finding(
                        ruleId = "KNOWLEDGE_MEMORY_NOT_RETRIEVED",
                        category = "knowledge_boundary",
                        severity = "MAJOR",
                        message = "Character action claims use of a memory that was not retrieved or included in context.",
                        evidence = listOf("character=$characterId", "memoryId=$memoryId"),
                        suggestedFix = "Only claim recalled memory when it appears in retrievedMemories or legacy characterMemories.",
                        input = input,
                        turn = context.turnNumber,
                    ),
                )
            }
    }

    private fun checkStyle(result: AiTurnResult, input: NarrativeQualityAuditInput): List<NarrativeQualityFinding> = buildList {
        val narrative = result.narrative
        val modernTerms = listOf("OK", "ok", "笑死", "靠北", "蛤", "打卡", "破防", "社死")
        val modernMatches = modernTerms.filter { narrative.contains(it) }
        if (modernMatches.isNotEmpty()) {
            add(
                finding(
                    ruleId = "STYLE_OVER_MODERN_SLANG",
                    category = "style",
                    severity = "WARNING",
                    message = "Narrative contains modern slang that may break classical-plain tone.",
                    evidence = modernMatches,
                    suggestedFix = "Keep modern terms only when the speaking character and setting justify them.",
                    input = input,
                ),
            )
        }

        val hardClassicalTerms = listOf("夫", "蓋", "然則", "是以", "嗚呼", "其謂", "焉得")
        val classicalCount = hardClassicalTerms.sumOf { term -> narrative.windowed(term.length, 1).count { it == term } }
        if (classicalCount >= 4) {
            add(
                finding(
                    ruleId = "STYLE_OVER_CLASSICAL",
                    category = "style",
                    severity = "WARNING",
                    message = "Narrative may be too classical and hard to read.",
                    evidence = listOf("classicalTermCount=$classicalCount"),
                    suggestedFix = "Use clear modern Chinese as the base, with only light classical flavor.",
                    input = input,
                ),
            )
        }

        val parallelSignals = Regex("不是[^。！？；;]{1,40}(而是|只是)|既[^。！？；;]{1,40}也|一邊[^。！？；;]{1,40}一邊")
            .findAll(narrative)
            .map { it.value }
            .toList()
        if (parallelSignals.size >= 2) {
            add(
                finding(
                    ruleId = "STYLE_PARALLELISM_STACKING",
                    category = "style",
                    severity = "WARNING",
                    message = "Narrative uses repeated parallel structures.",
                    evidence = parallelSignals.take(5),
                    suggestedFix = "Break decorative parallelism into concrete action and consequence.",
                    input = input,
                ),
            )
        }

        val purpleTerms = listOf("命運", "宿命", "天地", "萬物", "深處", "無聲地宣告", "彷彿整個世界")
        val purpleMatches = purpleTerms.filter { narrative.contains(it) }
        if (purpleMatches.size >= 3) {
            add(
                finding(
                    ruleId = "STYLE_EMPTY_GRANDIOSE_PROSE",
                    category = "style",
                    severity = "WARNING",
                    message = "Narrative may lean on grand abstract prose.",
                    evidence = purpleMatches,
                    suggestedFix = "Replace grand abstraction with observable scene detail and playable information.",
                    input = input,
                ),
            )
        }
    }

    private fun checkConsequences(result: AiTurnResult, input: NarrativeQualityAuditInput): List<NarrativeQualityFinding> = buildList {
        val hasConsequence = result.allDeltaOperations().isNotEmpty() ||
            result.revealedFacts.isNotEmpty() ||
            result.memoryCandidates.isNotEmpty() ||
            result.knowledgeUpdates.isNotEmpty() ||
            result.sceneRequirementUpdates.isNotEmpty() ||
            result.threadProgressUpdates.isNotEmpty() ||
            result.transitionProposal != null ||
            result.worldEventAcknowledgements.isNotEmpty() ||
            result.timeAdvanceProposal != null

        if (!hasConsequence) {
            add(
                finding(
                    ruleId = "CONSEQUENCE_NO_STATE_EVIDENCE",
                    category = "consequence",
                    severity = "MAJOR",
                    message = "Result contains no state, memory, knowledge, scene, thread, event, transition, or time consequence.",
                    evidence = listOf("stateDeltaCount=0"),
                    suggestedFix = "Add a legal state delta or explicit no-change reason with scene/thread tension evidence.",
                    input = input,
                ),
            )
        }

        val injuryOrCostTerms = listOf("受傷", "流血", "疲憊", "代價", "消耗", "裂開", "疼")
        val mentionsCost = injuryOrCostTerms.any { result.narrative.contains(it) }
        val costDelta = result.allDeltaOperations().any { operation ->
            val payload = operation.payload.toString()
            listOf("health", "fatigue", "cost", "injury", "傷", "疲").any { payload.contains(it, ignoreCase = true) }
        }
        if (mentionsCost && !costDelta) {
            add(
                finding(
                    ruleId = "CONSEQUENCE_COST_NOT_RECORDED",
                    category = "consequence",
                    severity = "WARNING",
                    message = "Narrative mentions injury, fatigue, or cost without matching state delta evidence.",
                    evidence = injuryOrCostTerms.filter { result.narrative.contains(it) },
                    suggestedFix = "Reflect the cost in a legal state delta, memory, fact, or explicit limitation.",
                    input = input,
                ),
            )
        }
    }

    private fun checkPilotEvidence(input: NarrativeQualityAuditInput): List<NarrativeQualityFinding> {
        if (input.evidenceText.isBlank()) return emptyList()
        val turnLogSignals = Regex("\"turnLogs\"|\"turn_logs\"").containsMatchIn(input.evidenceText)
        if (!turnLogSignals) return emptyList()
        val narratives = Regex("\"narrative\"\\s*:\\s*\"([^\"]{20,200})\"")
            .findAll(input.evidenceText)
            .map { it.groupValues[1].take(80) }
            .toList()
        val repeated = narratives
            .groupingBy { normalizeForRepetition(it) }
            .eachCount()
            .filter { it.value >= 2 && it.key.length >= 20 }
            .keys
        if (repeated.isEmpty()) return emptyList()
        return listOf(
            finding(
                ruleId = "PILOT_EVIDENCE_REPEATED_NARRATIVE",
                category = "ai_feeling",
                severity = "WARNING",
                message = "Pilot evidence contains repeated narrative fragments.",
                evidence = repeated.take(3).toList(),
                suggestedFix = "Review repeated beats across turns and vary response cause, consequence, and scene pressure.",
                input = input,
            ),
        )
    }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        message: String,
        evidence: List<String>,
        suggestedFix: String,
        input: NarrativeQualityAuditInput,
        turn: Int = 0,
    ): NarrativeQualityFinding =
        NarrativeQualityFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            message = message,
            evidence = evidence.filter { it.isNotBlank() },
            suggestedFix = suggestedFix,
            affectedFileOrTurn = buildString {
                if (input.resultFile.isNotBlank()) append(input.resultFile)
                if (turn > 0) {
                    if (isNotEmpty()) append("#")
                    append("turn-$turn")
                }
                if (isEmpty() && input.evidenceFile.isNotBlank()) append(input.evidenceFile)
            },
        )

    private fun scorePenalty(severity: String): Int =
        when (severity) {
            "CRITICAL" -> 35
            "MAJOR" -> 18
            "WARNING" -> 8
            else -> 0
        }

    private fun severityRank(severity: String): Int =
        when (severity) {
            "CRITICAL" -> 0
            "MAJOR" -> 1
            "WARNING" -> 2
            else -> 3
        }

    private fun containsUnnegatedTerm(text: String, term: String): Boolean {
        var index = text.indexOf(term)
        while (index >= 0) {
            val prefix = text.substring(maxOf(0, index - 8), index)
            val negated = listOf("沒有", "並未", "不是", "不曾", "不能", "未曾", "未能", "沒有要")
                .any { prefix.contains(it) }
            if (!negated) return true
            index = text.indexOf(term, startIndex = index + term.length)
        }
        return false
    }

    private fun normalizeForRepetition(value: String): String =
        value.lowercase()
            .replace(Regex("\\s+"), "")
            .replace(Regex("[，。！？；：、,.!?;:]"), "")
            .take(80)
}
