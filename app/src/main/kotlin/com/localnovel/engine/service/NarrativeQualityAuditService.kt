package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeliveryMode
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
    val sampleLabel: String = "",
    val dbPath: String = "",
    val logicAuditFindings: List<com.localnovel.engine.model.NarrativeQualityFinding> = emptyList(),
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
            addAll(checkNarrativeDelivery(context, result, input))
            addAll(checkConsequences(result, input))
            addAll(checkPilotEvidence(input))
            addAll(checkLearnedRules(context, result, input))
            addAll(input.logicAuditFindings)
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
            findingSummaryByCategory = findings.groupingBy { it.category }.eachCount(),
            evidence = NarrativeQualityAuditEvidence(
                contextFile = input.contextFile,
                resultFile = input.resultFile,
                evidenceFile = input.evidenceFile,
                sampleLabel = input.sampleLabel,
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

        val structuralConvenience = actions.filter { action ->
            val state = nonPlayerStates.firstOrNull { it.characterId == action.characterId }
            val surface = listOf(result.narrative, action.intent, action.visibleAction, action.hiddenMotive).joinToString("\n")
            hasNpcConstraintPressure(context, state) &&
                containsAny(surface, structuralComplianceTerms) &&
                !containsAny(surface, structuralBoundaryTerms) &&
                action.characterFitScore <= 74
        }
        if (structuralConvenience.isNotEmpty()) {
            add(
                finding(
                    ruleId = "NPC_TOOLMAN_STRUCTURAL_CONVENIENCE",
                    category = "npc_liveliness",
                    severity = "WARNING",
                    message = "NPC appears to grant the player convenience despite contextual duty, risk, or procedure pressure.",
                    evidence = structuralConvenience.map {
                        "${it.characterId}:fit=${it.characterFitScore}:${it.intent.take(40)}"
                    },
                    suggestedFix = "Show refusal, conditions, procedure, cost, or evidence that justifies why the NPC cooperates from their own constraints.",
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
        val hasSettingPressure = context.availableTransitions.isNotEmpty() ||
            context.pendingWorldEvents.isNotEmpty() ||
            context.activeStoryThreads.isNotEmpty() ||
            context.sceneRequirements.any { it.status != "COMPLETED" }
        val needsSettingExcerpt = context.activeStoryThreads.isNotEmpty() ||
            context.pendingWorldEvents.isNotEmpty()
        if (context.turnNumber > 0 && context.settingContextBudget.includedSettingCount == 0 && needsSettingExcerpt) {
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

        val directEmotionTerms = listOf(
            "害怕",
            "恐懼",
            "悲傷",
            "憤怒",
            "焦慮",
            "緊張",
            "震驚",
            "崩潰",
            "愧疚",
            "安心",
            "感動",
            "生氣",
            "痛苦",
            "恐慌",
        )
        val directEmotionMatches = directEmotionTerms.filter { narrative.contains(it) }
        val explicitEmotionMatches = directEmotionTerms.flatMap { term ->
            listOf("很$term", "非常$term", "十分$term", "極度$term", "感到$term", "內心$term", "心裡$term", "心中$term")
        }.filter { narrative.contains(it) }
        val strongBehaviorMatches = strongEmotionBehaviorTerms.filter { narrative.contains(it) }
        val emotionOutrunsBehavior =
            strongBehaviorMatches.isEmpty() ||
                (directEmotionMatches.size >= 3 && strongBehaviorMatches.size < 2)
        if ((directEmotionMatches.size >= 2 || explicitEmotionMatches.isNotEmpty()) && emotionOutrunsBehavior) {
            add(
                finding(
                    ruleId = "AI_FEELING_EMOTION_TOLD_NOT_SHOWN",
                    category = "ai_feeling",
                    severity = "WARNING",
                    message = "Narrative names emotions without enough visible behavior.",
                    evidence = (explicitEmotionMatches + directEmotionMatches).distinct().take(8),
                    suggestedFix = "Show emotion through action, gesture, voice, posture, or concrete choice.",
                    input = input,
                ),
            )
        }

        val causalChains = Regex("因為[^。！？；;]{1,40}，?所以")
            .findAll(narrative)
            .map { it.value }
            .toList()
        if (causalChains.size >= 2) {
            add(
                finding(
                    ruleId = "AI_FEELING_CAUSAL_CHAIN_STACKING",
                    category = "ai_feeling",
                    severity = "WARNING",
                    message = "Narrative stacks multiple because-so explanation chains instead of dramatizing action.",
                    evidence = causalChains.take(5),
                    suggestedFix = "Replace stacked causal explanation with visible choice, dialogue, or consequence.",
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

        val notButReframes = Regex("(?:這)?不是[^。！？；;]{1,36}，?(?:而是|是|只是)")
            .findAll(narrative)
            .map { it.value }
            .toList()
        if (notButReframes.isNotEmpty()) {
            add(
                finding(
                    ruleId = "STYLE_NOT_BUT_REFRAME",
                    category = "style",
                    severity = "WARNING",
                    message = "Narrative uses AI-like 'not X but Y' reframing instead of concrete scene action.",
                    evidence = notButReframes.take(5),
                    suggestedFix = "State what visibly happens; avoid decorative contrast chains like 'this is not A, it is B'.",
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

    private fun checkLearnedRules(
        context: ContextPacket,
        result: AiTurnResult,
        input: NarrativeQualityAuditInput,
    ): List<NarrativeQualityFinding> {
        if (input.dbPath.isBlank()) return emptyList()
        val pack = NarrativeRuleLearningStore.load(
            projectId = context.project.id,
            dbPath = java.nio.file.Path.of(input.dbPath),
        )
        return LearnedNarrativeRuleEvaluator.evaluate(result.narrative, pack.rules)
    }

    private fun checkNarrativeDelivery(
        context: ContextPacket,
        result: AiTurnResult,
        input: NarrativeQualityAuditInput,
    ): List<NarrativeQualityFinding> {
        val delivery = context.narrativeDelivery
        if (delivery.deliveryMode == DeliveryMode.DEFAULT) return emptyList()
        return buildList {
            addAll(checkSubstanceFloor(context, result, delivery.deliveryMode, input))
            addAll(checkOralLineCandidates(context, result, input))
            addAll(checkDialogueSpeakerAttribution(context, result, delivery.deliveryMode, input))
            addAll(checkCompanionCommandDialogue(context, result, delivery.deliveryMode, input))
            addAll(checkSimplifiedChineseInNarrative(result, input, context.turnNumber))
            addAll(checkOralClarity(result, delivery.deliveryMode, input, context.turnNumber))
            addAll(checkUnshootableFiller(result, delivery.deliveryMode, input))
            addAll(checkBeatMismatch(result, delivery, input, context.turnNumber))
        }
    }

    private fun checkSubstanceFloor(
        context: ContextPacket,
        result: AiTurnResult,
        deliveryMode: DeliveryMode,
        input: NarrativeQualityAuditInput,
    ): List<NarrativeQualityFinding> = buildList {
        val narrative = result.narrative
        val characterNames = context.characters.map { it.name }.filter { it.isNotBlank() }
        val locationSurface = buildString {
            append(context.activeScene?.title.orEmpty())
            append(' ')
            append(context.activeScene?.summary.orEmpty())
            append(' ')
            append(context.playerInput)
        }

        if (context.turnNumber <= 1 && "WHO" in context.narrativeDelivery.requiredExposition) {
            val whoMatched = characterNames.any { narrative.contains(it) } ||
                listOf("他", "她", "主角", "玩家").any { narrative.contains(it) }
            if (!whoMatched) {
                add(
                    finding(
                        ruleId = "NARRATIVE_SUBSTANCE_FLOOR",
                        category = "narrative_delivery",
                        severity = "MAJOR",
                        message = "Opening narrative must name who is acting.",
                        evidence = listOf("missing=WHO"),
                        suggestedFix = "Name the acting character in plain language before atmosphere or abstraction.",
                        input = input,
                        turn = context.turnNumber,
                    ),
                )
            }
        }

        if (context.turnNumber <= 1 && "WHERE" in context.narrativeDelivery.requiredExposition) {
            val whereTerms = listOf("海岸", "海邊", "山坡", "廢墟", "碼頭", "祠堂", "廊", "殿", "門", "街", "房", "屋", "路", "山", "港", "灣", "平台", "潮間帶")
            val whereMatched = whereTerms.any { locationSurface.contains(it) || narrative.contains(it) }
            if (!whereMatched) {
                add(
                    finding(
                        ruleId = "NARRATIVE_SUBSTANCE_FLOOR",
                        category = "narrative_delivery",
                        severity = "MAJOR",
                        message = "Opening narrative must establish where the scene happens.",
                        evidence = listOf("missing=WHERE"),
                        suggestedFix = "State the visible place in one plain sentence.",
                        input = input,
                        turn = context.turnNumber,
                    ),
                )
            }
        }

        if (context.turnNumber <= 1 && "WHY" in context.narrativeDelivery.requiredExposition) {
            val whyTerms = listOf("為了", "因為", "得", "要", "趕", "查", "估", "還有", "窗口", "目的", "任務", "調查", "確認", "等待")
            val whyMatched = whyTerms.any { narrative.contains(it) || context.playerInput.contains(it) }
            if (!whyMatched) {
                add(
                    finding(
                        ruleId = "NARRATIVE_SUBSTANCE_FLOOR",
                        category = "narrative_delivery",
                        severity = "MAJOR",
                        message = "Opening narrative must explain why the character is here or what they are trying to do.",
                        evidence = listOf("missing=WHY"),
                        suggestedFix = "Add one concrete purpose, deadline, or immediate task.",
                        input = input,
                        turn = context.turnNumber,
                    ),
                )
            }
        }

        val actionVerbs = listOf("走", "跑", "退", "蹲", "看", "說", "喊", "摸", "按", "舉", "轉", "拉", "推", "抓", "站", "坐", "抬", "低", "握", "攀", "躲", "查", "估", "記", "寫", "聽", "問", "答", "守", "擋", "讓", "跟")
        val dialogueMarkers = listOf("「", "」", "：", "——")
        val hasNarrativeAction = actionVerbs.any { narrative.contains(it) } ||
            dialogueMarkers.any { narrative.contains(it) }
        if (!hasNarrativeAction) {
            add(
                finding(
                    ruleId = "NARRATIVE_SUBSTANCE_FLOOR",
                    category = "narrative_delivery",
                    severity = "MAJOR",
                    message = "Narrative lacks a concrete visible action.",
                    evidence = listOf("missing=EVENT"),
                    suggestedFix = "Show what someone does, says, or changes in the scene.",
                    input = input,
                    turn = context.turnNumber,
                ),
            )
        }

        if (deliveryMode == DeliveryMode.SHOOTABLE && result.characterActions.none { it.visibleAction.length >= 6 }) {
            add(
                finding(
                    ruleId = "NARRATIVE_SUBSTANCE_FLOOR",
                    category = "narrative_delivery",
                    severity = "MAJOR",
                    message = "Shootable delivery requires at least one explicit visible action line.",
                    evidence = listOf("missing=visibleAction"),
                    suggestedFix = "Add a characterActions visibleAction that a camera can capture.",
                    input = input,
                    turn = context.turnNumber,
                ),
            )
        }
    }

    private fun checkOralLineCandidates(
        context: ContextPacket,
        result: AiTurnResult,
        input: NarrativeQualityAuditInput,
    ): List<NarrativeQualityFinding> = buildList {
        if (context.oralLineCandidates.isEmpty()) return@buildList
        if (context.narrativeDelivery.deliveryMode == DeliveryMode.DEFAULT) return@buildList

        val dialogueCandidates = context.oralLineCandidates
            .filter { it.lineType == "dialogue" && it.line.isNotBlank() }
            .map { it.line }
        if (dialogueCandidates.isEmpty()) return@buildList

        val quotedDialogue = OralLineMatcher.extractQuotedDialogue(result.narrative)
        if (quotedDialogue.isEmpty()) {
            add(
                finding(
                    ruleId = "DIALOGUE_MUST_USE_ORAL_CANDIDATES",
                    category = "narrative_delivery",
                    severity = "MAJOR",
                    message = "Oral delivery requires quoted dialogue from oralLineCandidates when dialogue slots exist.",
                    evidence = dialogueCandidates.take(2),
                    suggestedFix = "Use one of context.oralLineCandidates dialogue lines inside Chinese quotation marks.",
                    input = input,
                    turn = context.turnNumber,
                ),
            )
            return@buildList
        }

        val jargonTerms = listOf("退潮窗口", "時間窗口", "敘事窗口", "語義", "敘事節奏", "窗口期")
        quotedDialogue.forEach { quote ->
            val jargonMatches = jargonTerms.filter { quote.contains(it) }
            if (jargonMatches.isNotEmpty()) {
                add(
                    finding(
                        ruleId = "DIALOGUE_MUST_USE_ORAL_CANDIDATES",
                        category = "narrative_delivery",
                        severity = "MAJOR",
                        message = "Quoted dialogue contains technical or AI-like jargon instead of oral candidate lines.",
                        evidence = jargonMatches,
                        suggestedFix = "Replace jargon dialogue with a context.oralLineCandidates line.",
                        input = input,
                        turn = context.turnNumber,
                    ),
                )
            }
            if (dialogueCandidates.none { candidate -> OralLineMatcher.matchesSpokenLine(quote, candidate) }) {
                add(
                    finding(
                        ruleId = "DIALOGUE_MUST_USE_ORAL_CANDIDATES",
                        category = "narrative_delivery",
                        severity = "MAJOR",
                        message = "Quoted dialogue does not match any oralLineCandidates dialogue slot.",
                        evidence = listOf("quote=$quote") + dialogueCandidates.take(2),
                        suggestedFix = "Pick a dialogue line from context.oralLineCandidates; minor wording edits only.",
                        input = input,
                        turn = context.turnNumber,
                    ),
                )
            }
        }
    }

    private fun checkDialogueSpeakerAttribution(
        context: ContextPacket,
        result: AiTurnResult,
        deliveryMode: DeliveryMode,
        input: NarrativeQualityAuditInput,
    ): List<NarrativeQualityFinding> = buildList {
        if (deliveryMode == DeliveryMode.DEFAULT) return@buildList
        val quotedDialogue = OralLineMatcher.extractQuotedDialogue(result.narrative)
        if (quotedDialogue.isEmpty()) return@buildList

        val characterNames = context.characters.map { it.name }.filter { it.isNotBlank() }
        val unattributed = NarrativeOralFormatter.findDialogueWithoutAttribution(
            narrative = result.narrative,
            characterNames = characterNames,
        )
        if (unattributed.isNotEmpty()) {
            add(
                finding(
                    ruleId = "DIALOGUE_REQUIRES_SPEAKER_ATTRIBUTION",
                    category = "narrative_delivery",
                    severity = "MAJOR",
                    message = "Quoted dialogue must be introduced with a speaker attribution such as 某某說/道/問 before the quotation marks.",
                    evidence = unattributed.take(2).map { "quote=$it" },
                    suggestedFix = "Rewrite as `陸塵行低聲道：「...」` or `沈霜問：「...」`; do not place dialogue directly after narration without 說/道/問/答.",
                    input = input,
                    turn = context.turnNumber,
                ),
            )
        }
    }

    private fun checkSimplifiedChineseInNarrative(
        result: AiTurnResult,
        input: NarrativeQualityAuditInput,
        turnNumber: Int,
    ): List<NarrativeQualityFinding> = buildList {
        val hits = SIMPLIFIED_CHINESE_MARKERS.filter { (simplified, _) -> result.narrative.contains(simplified) }
        if (hits.isEmpty()) return@buildList
        add(
            finding(
                ruleId = "SIMPLIFIED_CHINESE_IN_NARRATIVE",
                category = "narrative_delivery",
                severity = "WARNING",
                message = "Narrative contains simplified Chinese characters unsuitable for Traditional publish text.",
                evidence = hits.map { (from, to) -> "$from->$to" },
                suggestedFix = "Rewrite with Traditional characters, e.g. ${hits.first().second}.",
                input = input,
                turn = turnNumber,
            ),
        )
    }

    private fun checkCompanionCommandDialogue(
        context: ContextPacket,
        result: AiTurnResult,
        deliveryMode: DeliveryMode,
        input: NarrativeQualityAuditInput,
    ): List<NarrativeQualityFinding> = buildList {
        if (deliveryMode == DeliveryMode.DEFAULT) return@buildList
        val companionNames = context.characters
            .filter { character ->
                character.role == "co-protagonist" || character.id == "char-aijiang"
            }
            .map { it.name }
            .filter { it.isNotBlank() }
        if (companionNames.isEmpty()) return@buildList

        val commandTerms = listOf("跟我走", "跟我來", "過來", "快走", "跟上", "聽話", "乖")
        OralLineMatcher.extractQuotedDialogue(result.narrative).forEach { quote ->
            val addressedCompanion = companionNames.any { quote.contains(it) }
            val hasCommand = commandTerms.any { quote.contains(it) }
            if (addressedCompanion && hasCommand) {
                add(
                    finding(
                        ruleId = "COMPANION_COMMAND_DIALOGUE_INCONGRUENT",
                        category = "narrative_delivery",
                        severity = "MAJOR",
                        message = "Quoted dialogue treats a默契家人同伴 like a pet to be commanded.",
                        evidence = listOf("quote=$quote", "companions=${companionNames.joinToString(",")}"),
                        suggestedFix = "Show默契跟隨 in narration, or let the protagonist mutter to himself (自言) without calling the companion.",
                        input = input,
                        turn = context.turnNumber,
                    ),
                )
            }
        }
    }

    private fun checkOralClarity(
        result: AiTurnResult,
        deliveryMode: DeliveryMode,
        input: NarrativeQualityAuditInput,
        turnNumber: Int,
    ): List<NarrativeQualityFinding> = buildList {
        val narrative = result.narrative
        val danglingRumor = Regex("(?:聽說|傳聞|謠言|有人說)[^。！？；;]{0,24}……")
            .findAll(narrative)
            .map { it.value }
            .toList()
        if (danglingRumor.isNotEmpty()) {
            add(
                finding(
                    ruleId = "NARRATIVE_ORAL_CLARITY",
                    category = "narrative_delivery",
                    severity = "MAJOR",
                    message = "Oral delivery cannot leave a rumor or hearsay half-finished.",
                    evidence = danglingRumor.take(3),
                    suggestedFix = "Finish the rumor in plain language or omit it until it can be stated fully.",
                    input = input,
                    turn = turnNumber,
                ),
            )
        }

        val unexplainedTerms = listOf("逆行門", "别界", "裂縫", "先遣隊", "契印", "靈脈", "法器")
        val unexplainedMatches = unexplainedTerms.filter { narrative.contains(it) }
        if (turnNumber <= 2 && unexplainedMatches.size >= 2) {
            add(
                finding(
                    ruleId = "NARRATIVE_ORAL_CLARITY",
                    category = "narrative_delivery",
                    severity = "MAJOR",
                    message = "Early oral delivery stacks unexplained specialized terms.",
                    evidence = unexplainedMatches,
                    suggestedFix = "Replace jargon with what the listener can hear and understand in one pass.",
                    input = input,
                    turn = turnNumber,
                ),
            )
        }

        if (deliveryMode == DeliveryMode.PLAIN_ORAL) {
            val denseStack = Regex("[^。！？；;]{48,}")
                .findAll(narrative)
                .count { it.value.count { ch -> ch == '，' || ch == '、' } >= 4 }
            if (denseStack >= 1) {
                add(
                    finding(
                        ruleId = "NARRATIVE_ORAL_CLARITY",
                        category = "narrative_delivery",
                        severity = "WARNING",
                        message = "Oral delivery contains an overlong clause chain that is hard to follow when heard once.",
                        evidence = listOf("denseClauseCount=$denseStack"),
                        suggestedFix = "Split into shorter sentences with one fact or action each.",
                        input = input,
                        turn = turnNumber,
                    ),
                )
            }
        }
    }

    private fun checkUnshootableFiller(
        result: AiTurnResult,
        deliveryMode: DeliveryMode,
        input: NarrativeQualityAuditInput,
    ): List<NarrativeQualityFinding> = buildList {
        val narrative = result.narrative
        val sentences = narrative.split(Regex("[。！？!?]")).map { it.trim() }.filter { it.isNotBlank() }
        if (sentences.isEmpty()) return@buildList

        val atmosphereTerms = listOf("風裡", "天色", "彷彿", "無聲", "鹹澀", "鹹味", "沉了一分", "壓下來", "深處", "命運", "宿命", "天地", "萬物", "宣告", "回聲", "氛圍", "氣息", "寂靜", "朦朧")
        val actionVerbs = listOf("走", "跑", "退", "蹲", "看", "說", "喊", "摸", "按", "舉", "轉", "拉", "推", "抓", "站", "坐", "抬", "握", "攀", "躲", "查", "聽", "問", "答", "守", "擋", "讓", "跟", "按", "打", "踢", "開", "關", "進", "出")
        val dialogueMarkers = listOf("「", "」", "：", "——", "說", "問", "答", "喊", "叫")

        val atmosphereOnlySentences = sentences.filter { sentence ->
            atmosphereTerms.any { sentence.contains(it) } &&
                actionVerbs.none { sentence.contains(it) } &&
                dialogueMarkers.none { sentence.contains(it) }
        }
        if (atmosphereOnlySentences.size >= 2) {
            add(
                finding(
                    ruleId = "NARRATIVE_UNSHOOTABLE_FILLER",
                    category = "narrative_delivery",
                    severity = "MAJOR",
                    message = "Narrative contains multiple atmosphere-only sentences without action, dialogue, or visible objects.",
                    evidence = atmosphereOnlySentences.take(3),
                    suggestedFix = "Replace decorative atmosphere with who does what, where, and what changes.",
                    input = input,
                ),
            )
        }

        val consecutiveAtmosphere = countConsecutiveAtmosphereRuns(sentences, atmosphereTerms, actionVerbs, dialogueMarkers)
        if (consecutiveAtmosphere >= 3) {
            add(
                finding(
                    ruleId = "NARRATIVE_UNSHOOTABLE_FILLER",
                    category = "narrative_delivery",
                    severity = "MAJOR",
                    message = "Narrative stacks consecutive unshootable atmosphere sentences.",
                    evidence = listOf("consecutiveAtmosphere=$consecutiveAtmosphere"),
                    suggestedFix = "Break the run with visible action, dialogue, or a concrete object change.",
                    input = input,
                ),
            )
        }

        if (deliveryMode == DeliveryMode.SHOOTABLE) {
            val cameraTerms = listOf("鏡頭", "特寫", "中景", "遠景", "跟拍", "cut", "CUT")
            if (cameraTerms.none { narrative.contains(it) } && result.characterActions.isEmpty()) {
                add(
                    finding(
                        ruleId = "NARRATIVE_UNSHOOTABLE_FILLER",
                        category = "narrative_delivery",
                        severity = "MAJOR",
                        message = "Shootable delivery needs visible action evidence, not prose-only atmosphere.",
                        evidence = listOf("missing=shootable_surface"),
                        suggestedFix = "Write per-shot visible action and dialogue instead of literary description.",
                        input = input,
                    ),
                )
            }
        }
    }

    private fun checkBeatMismatch(
        result: AiTurnResult,
        delivery: com.localnovel.engine.model.NarrativeDeliveryExpectation,
        input: NarrativeQualityAuditInput,
        turnNumber: Int,
    ): List<NarrativeQualityFinding> = buildList {
        if (delivery.activeBeatId.isBlank() || delivery.beatKeywords.isEmpty()) return@buildList
        val surface = result.narrative + "\n" + result.characterActions.joinToString("\n") { "${it.intent} ${it.visibleAction}" }
        val matched = delivery.beatKeywords.count { keyword -> surface.contains(keyword) }
        val requiredMatches = if (delivery.beatKeywords.size <= 3) 1 else 2
        if (matched < requiredMatches) {
            add(
                finding(
                    ruleId = "NARRATIVE_BEAT_MISMATCH",
                    category = "narrative_delivery",
                    severity = "MAJOR",
                    message = "Narrative does not land the active opening beat '${delivery.activeBeatTitle}'.",
                    evidence = listOf(
                        "beatId=${delivery.activeBeatId}",
                        "matchedKeywords=$matched",
                        "requiredMatches=$requiredMatches",
                    ) + delivery.beatKeywords.take(4),
                    suggestedFix = "Advance the active beat with concrete action and facts tied to ${delivery.activeBeatId}.",
                    input = input,
                    turn = turnNumber,
                ),
            )
        }
    }

    private fun countConsecutiveAtmosphereRuns(
        sentences: List<String>,
        atmosphereTerms: List<String>,
        actionVerbs: List<String>,
        dialogueMarkers: List<String>,
    ): Int {
        var current = 0
        var max = 0
        sentences.forEach { sentence ->
            val atmosphereOnly = atmosphereTerms.any { sentence.contains(it) } &&
                actionVerbs.none { sentence.contains(it) } &&
                dialogueMarkers.none { sentence.contains(it) }
            if (atmosphereOnly) {
                current += 1
                max = maxOf(max, current)
            } else {
                current = 0
            }
        }
        return max
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

        val costEvidence = costCueEvidence(result.narrative)
        val mentionsCost = costEvidence.isNotEmpty()
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
                    evidence = costEvidence,
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

    private fun hasNpcConstraintPressure(context: ContextPacket, state: com.localnovel.engine.model.CharacterState?): Boolean {
        val pressureSurface = buildString {
            append(state?.flags.orEmpty().values.joinToString(" "))
            append(' ')
            append(context.worldRules.joinToString(" ") { "${it.title} ${it.content}" })
            append(' ')
            append(context.sceneRequirements.joinToString(" ") { "${it.requirementId} ${it.description}" })
        }
        return containsAny(pressureSurface, npcConstraintTerms)
    }

    private fun containsAny(text: String, terms: List<String>): Boolean =
        terms.any { text.contains(it) }

    private fun costCueEvidence(text: String): List<String> {
        val directMatches = directCostTerms.filter { text.contains(it) }
        val crackMatches = Regex("[^，。！？；;,.!?]{0,10}(裂開|裂紋|骨裂)[^，。！？；;,.!?]{0,10}")
            .findAll(text)
            .map { it.value }
            .filter { window ->
                containsAny(window, crackCostTerms) &&
                    !(containsAny(window, nonCostCrackTerms) && !containsAny(window, hardCostTerms))
            }
            .toList()
        return (directMatches + crackMatches).distinct()
    }

    private companion object {
        val SIMPLIFIED_CHINESE_MARKERS = listOf(
            "竖" to "豎",
            "紧" to "緊",
            "发" to "發",
            "听" to "聽",
            "复" to "複",
        )
        val structuralComplianceTerms = listOf("允許", "放行", "照辦", "配合", "答應", "讓開", "交出", "帶路", "替你", "支走", "處理後續")
        val structuralBoundaryTerms = listOf(
            "不",
            "沒有",
            "不能",
            "拒",
            "要求",
            "條件",
            "先",
            "暫",
            "按規矩",
            "通報",
            "保留",
            "確認",
            "警告",
            "等待",
            "只允許",
            "限制",
            "代價",
            "風險",
            "程序",
            "查驗",
        )
        val npcConstraintTerms = listOf(
            "守",
            "查",
            "阻止",
            "不能",
            "規矩",
            "職責",
            "風險",
            "危險",
            "契印",
            "通報",
            "上級",
            "證物",
            "程序",
            "封控",
            "限制",
        )
        val strongEmotionBehaviorTerms = listOf(
            "指節",
            "發白",
            "喉結",
            "滾動",
            "退半步",
            "握緊",
            "屏住呼吸",
            "目光避開",
            "尾巴壓低",
            "肩背",
            "顫",
            "僵",
            "咬住",
            "皺眉",
            "吸氣",
            "呼吸",
            "低聲",
            "沉默",
            "跪",
            "踉蹌",
        )
        val directCostTerms = listOf(
            "受傷",
            "流血",
            "疲憊",
            "代價",
            "消耗",
            "疼",
            "痛",
            "傷口",
            "皮肉",
            "骨裂",
            "骨頭裂開",
            "經脈裂開",
            "氣血反噬",
            "反噬",
        )
        val crackCostTerms = listOf("傷口", "皮肉", "骨", "經脈", "血", "氣血", "法器", "資源", "能力", "失效", "碎", "損壞", "耗盡", "代價", "消耗")
        val hardCostTerms = listOf("傷口", "皮肉", "骨", "經脈", "血", "氣血", "資源", "能力", "失效", "損壞", "耗盡", "代價", "消耗")
        val nonCostCrackTerms = listOf("表情", "笑容", "沉默", "聲音", "腰牌", "門板")
    }

    private fun normalizeForRepetition(value: String): String =
        value.lowercase()
            .replace(Regex("\\s+"), "")
            .replace(Regex("[，。！？；：、,.!?;:]"), "")
            .take(80)
}
