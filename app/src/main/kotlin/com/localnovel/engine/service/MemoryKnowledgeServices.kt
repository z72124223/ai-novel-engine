package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterKnowledgeClaim
import com.localnovel.engine.model.CharacterMemory
import com.localnovel.engine.model.CharacterState
import com.localnovel.engine.model.ContextBudget
import com.localnovel.engine.model.ContextKnowledgeClaim
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.RetrievedMemory
import com.localnovel.engine.model.StorySession
import com.localnovel.engine.model.ValidationError
import com.localnovel.engine.model.allDeltaOperations
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.max
import kotlin.math.roundToInt

data class MemoryRetrievalSelection(
    val retrievedMemories: List<RetrievedMemory>,
    val knowledgeClaims: List<ContextKnowledgeClaim>,
    val omittedMemoryCount: Int,
    val omittedKnowledgeCount: Int,
)

class MemoryRetriever(
    private val repository: SqliteStoryRepository,
    private val maxMemoriesPerCharacter: Int = 5,
    private val maxKnowledgeClaimsPerCharacter: Int = 8,
) {
    fun retrieve(session: StorySession, playerInput: String): MemoryRetrievalSelection {
        val projectId = session.projectId
        val scene = repository.getActiveScene(projectId)
        val characters = repository.listCharacters(projectId).filterNot { it.isPlayer }
        val states = repository.listCharacterStates(projectId).associateBy { it.characterId }
        val items = repository.listItems(projectId)
        val secrets = repository.listSecrets(projectId).filterNot { it.revealed }
        val hiddenTexts = secrets.map { it.content }.filter { it.isNotBlank() }

        val selected = mutableListOf<RetrievedMemory>()
        var omittedMemories = 0
        characters.forEach { character ->
            val state = states[character.id]
            val ranked = repository.listAllCharacterMemories(projectId)
                .asSequence()
                .filter { it.ownerCharacterId == character.id || it.characterId == character.id }
                .filter { it.status == "active" }
                .filterNot { it.visibility in setOf("author_only", "inaccessible") }
                .filterNot { memory -> hiddenTexts.any { memory.summary.contains(it) } }
                .map { memory -> scoreMemory(memory, state, playerInput, scene?.title.orEmpty(), scene?.summary.orEmpty(), items.map { it.name } + items.flatMap { it.tags }) }
                .filter { it.score > 0 }
                .sortedWith(compareByDescending<ScoredMemory> { it.score }.thenBy { it.memory.id })
                .toList()
            selected += ranked.take(maxMemoriesPerCharacter).map { it.toRetrievedMemory() }
            omittedMemories += max(0, ranked.size - maxMemoriesPerCharacter)
        }

        val selectedKnowledge = mutableListOf<ContextKnowledgeClaim>()
        var omittedKnowledge = 0
        characters.forEach { character ->
            val claims = repository.listKnowledgeClaims(projectId, character.id)
                .filterNot { it.visibility == "author_only" }
                .filterNot { claim -> hiddenTexts.any { claim.statement.contains(it) } }
                .sortedWith(compareByDescending<CharacterKnowledgeClaim> { it.confidence }.thenBy { it.id })
            selectedKnowledge += claims.take(maxKnowledgeClaimsPerCharacter).map { it.toContextClaim() }
            omittedKnowledge += max(0, claims.size - maxKnowledgeClaimsPerCharacter)
        }

        return MemoryRetrievalSelection(
            retrievedMemories = selected.sortedWith(compareByDescending<RetrievedMemory> { it.retrievalScore }.thenBy { it.memoryId }),
            knowledgeClaims = selectedKnowledge,
            omittedMemoryCount = omittedMemories,
            omittedKnowledgeCount = omittedKnowledge,
        )
    }

    fun preview(sessionId: String, characterId: String, playerInput: String): List<RetrievedMemory> {
        val session = repository.getSession(sessionId) ?: error("Session not found: $sessionId")
        val projectId = session.projectId
        val scene = repository.getActiveScene(projectId)
        val state = repository.getCharacterState(projectId, characterId)
        val items = repository.listItems(projectId)
        return repository.listAllCharacterMemories(projectId)
            .filter { it.ownerCharacterId == characterId || it.characterId == characterId }
            .filter { it.status == "active" }
            .filterNot { it.visibility in setOf("author_only", "inaccessible") }
            .map { scoreMemory(it, state, playerInput, scene?.title.orEmpty(), scene?.summary.orEmpty(), items.map { item -> item.name } + items.flatMap { item -> item.tags }) }
            .filter { it.score > 0 }
            .sortedWith(compareByDescending<ScoredMemory> { it.score }.thenBy { it.memory.id })
            .map { it.toRetrievedMemory() }
    }

    private fun scoreMemory(
        memory: CharacterMemory,
        state: CharacterState?,
        playerInput: String,
        sceneTitle: String,
        sceneSummary: String,
        entityTokens: List<String>,
    ): ScoredMemory {
        val reasons = mutableListOf<String>()
        val inputText = listOf(playerInput, sceneTitle, sceneSummary).joinToString(" ")
        val currentGoal = state?.flags?.get("current_goal").orEmpty()
        val mood = state?.mood.orEmpty()
        val memoryTags = (memory.tags + memory.triggerTags).filter { it.isNotBlank() }
        val tagOverlap = memoryTags.count { inputText.contains(it, ignoreCase = true) || currentGoal.contains(it, ignoreCase = true) }
        if (tagOverlap > 0) reasons += "matched player input tag"

        val directEntityMatch = entityTokens.any { token ->
            token.isNotBlank() && (memory.summary.contains(token, ignoreCase = true) || inputText.contains(token, ignoreCase = true) && memoryTags.contains(token))
        }
        if (directEntityMatch) reasons += "direct entity match"

        val goalMatch = tokenize(currentGoal).any { it.length >= 2 && memory.summary.contains(it, ignoreCase = true) }
        if (goalMatch) reasons += "related to current goal"

        val emotionalMatch = mood.isNotBlank() && (
            memory.summary.contains(mood, ignoreCase = true) ||
                memory.emotionalSignature.keys.any { it.contains(mood, ignoreCase = true) || mood.contains(it, ignoreCase = true) }
            )
        if (emotionalMatch) reasons += "emotional trigger matched"

        val dependencyRelation = memory.subjectId != null && inputText.contains(memory.subjectId, ignoreCase = true)
        if (dependencyRelation) reasons += "dependency relation matched"

        val unresolved = memory.memoryType in setOf("TRAUMA", "PROMISE", "RELATIONAL") || memory.tags.any { it in setOf("誓言", "創傷", "衝突", "promise", "trauma", "conflict") }
        if (unresolved) reasons += "unresolved promise or conflict"

        val age = max(0, (state?.flags?.get("current_turn")?.toIntOrNull() ?: 0) - memory.createdTurn)
        val recency = max(0, 20 - age)
        val decayPenalty = (age * memory.decayRate).roundToInt()
        val score = (memory.salience * 2) +
            (tagOverlap * 20) +
            (if (directEntityMatch) 25 else 0) +
            (if (goalMatch) 15 else 0) +
            (if (emotionalMatch) 10 else 0) +
            (if (dependencyRelation) 10 else 0) +
            recency +
            (if (unresolved) 15 else 0) -
            decayPenalty
        return ScoredMemory(memory, score.coerceAtLeast(0), reasons.ifEmpty { listOf("baseline salience and recency") })
    }

    private fun tokenize(text: String): List<String> =
        Regex("[\\p{L}\\p{N}_]+").findAll(text).map { it.value }.toList()

    private fun CharacterKnowledgeClaim.toContextClaim(): ContextKnowledgeClaim =
        ContextKnowledgeClaim(
            claimId = id,
            characterId = characterId,
            propositionKey = propositionKey,
            statement = statement,
            epistemicState = epistemicState,
            confidence = confidence,
            sourceType = when {
                sourceFactId != null -> "fact"
                sourceMemoryId != null -> "memory"
                else -> "belief"
            },
            sourceFactId = sourceFactId,
            sourceMemoryId = sourceMemoryId,
        )

    private data class ScoredMemory(
        val memory: CharacterMemory,
        val score: Int,
        val reasons: List<String>,
    ) {
        fun toRetrievedMemory(): RetrievedMemory =
            RetrievedMemory(
                memoryId = memory.id,
                ownerCharacterId = memory.ownerCharacterId,
                type = memory.memoryType,
                summary = memory.summary,
                emotionalSignature = memory.emotionalSignature,
                confidence = memory.confidence,
                salience = memory.salience,
                retrievalScore = score,
                retrievalReasons = reasons,
            )
    }
}

class ContextBudgeter(
    private val maxContextBytes: Int = 24576,
) {
    fun applyBudget(context: ContextPacket, omittedMemoryCount: Int, omittedKnowledgeCount: Int): ContextPacket {
        var memories = context.retrievedMemories
        var claims = context.knowledgeClaims
        val reasons = mutableListOf<String>()
        var candidate = context.copy(
            contextBudget = ContextBudget(
                maxBytes = maxContextBytes,
                includedMemoryCount = memories.size,
                omittedMemoryCount = omittedMemoryCount,
                includedKnowledgeCount = claims.size,
                omittedKnowledgeCount = omittedKnowledgeCount,
            ),
        )
        var size = encodedSize(candidate)
        while (size > maxContextBytes && (memories.isNotEmpty() || claims.isNotEmpty())) {
            if (memories.isNotEmpty()) {
                memories = memories.dropLast(1)
                reasons += "omitted lowest-ranked memory"
            } else {
                claims = claims.dropLast(1)
                reasons += "omitted lowest-priority knowledge claim"
            }
            candidate = candidate.copy(
                retrievedMemories = memories,
                knowledgeClaims = claims,
                characterMemories = memories.map {
                    CharacterMemory(
                        id = it.memoryId,
                        projectId = context.project.id,
                        characterId = it.ownerCharacterId,
                        ownerCharacterId = it.ownerCharacterId,
                        memoryType = it.type,
                        summary = it.summary,
                        emotionalSignature = it.emotionalSignature,
                        salience = it.salience,
                        confidence = it.confidence,
                    )
                },
                contextBudget = ContextBudget(
                    maxBytes = maxContextBytes,
                    includedMemoryCount = memories.size,
                    omittedMemoryCount = omittedMemoryCount + context.retrievedMemories.size - memories.size,
                    includedKnowledgeCount = claims.size,
                    omittedKnowledgeCount = omittedKnowledgeCount + context.knowledgeClaims.size - claims.size,
                    truncationApplied = true,
                    truncationReasons = reasons.distinct(),
                ),
            )
            size = encodedSize(candidate)
        }
        if (size > maxContextBytes) reasons += "budget exceeded but safety fields retained"
        return candidate.copy(
            contextBudget = candidate.contextBudget.copy(
                actualBytes = size,
                truncationApplied = reasons.isNotEmpty(),
                truncationReasons = reasons.distinct(),
            ),
        )
    }

    private fun encodedSize(context: ContextPacket): Int =
        JsonSupport.json.encodeToString(context.copy(contextHash = "")).toByteArray(Charsets.UTF_8).size
}

class KnowledgeBoundaryValidator(private val repository: SqliteStoryRepository) {
    fun validate(context: ContextPacket, result: AiTurnResult): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val memoryById = context.retrievedMemories.associateBy { it.memoryId }
        val contextMemoryIds = memoryById.keys + context.characterMemories.map { it.id }
        val itemIds = context.items.map { it.id }.toSet()
        val techniqueIds = context.techniques.map { it.id }.toSet()
        val knowledgeById = context.knowledgeClaims.associateBy { it.claimId }
        val factIds = context.facts.map { it.id }.toSet()
        val revealedFactIds = result.revealedFacts.map { it.factId }.toSet() +
            result.allDeltaOperations().filter { it.type == DeltaOperationType.ADD_FACT }.mapNotNull { it.targetId }

        result.characterActions.forEachIndexed { index, action ->
            action.usedMemoryIds.forEach { memoryId ->
                val memory = memoryById[memoryId]
                if (memoryId !in contextMemoryIds) {
                    errors += ValidationError("unknown_memory", "characterActions[$index] uses memory not present in context: $memoryId")
                } else if (memory != null && memory.ownerCharacterId != action.characterId) {
                    errors += ValidationError("knowledge_boundary", "characterActions[$index] uses another character's memory: $memoryId")
                }
            }
            action.usedKnowledgeClaimIds.forEach { claimId ->
                val claim = knowledgeById[claimId]
                if (claim == null || claim.characterId != action.characterId) {
                    errors += ValidationError("unknown_knowledge", "characterActions[$index] uses unavailable knowledge claim: $claimId")
                }
            }
            action.usedFactIds.forEach { factId ->
                val publicFact = context.facts.any { it.id == factId && it.visibility == "public" }
                val knownFact = context.knowledgeClaims.any { it.characterId == action.characterId && it.sourceFactId == factId }
                if (!publicFact && !knownFact && factId !in revealedFactIds) {
                    errors += ValidationError("unknown_fact", "characterActions[$index] uses fact outside character knowledge: $factId")
                }
            }
            action.usedItemIds.filterNot { it in itemIds }.forEach {
                errors += ValidationError("unknown_item", "characterActions[$index] uses unknown item: $it")
            }
            action.usedTechniqueIds.filterNot { it in techniqueIds }.forEach {
                errors += ValidationError("unknown_technique", "characterActions[$index] uses unknown technique: $it")
            }
        }

        result.revealedFacts.forEach { revealed ->
            if (revealed.factId !in factIds && revealed.factId !in revealedFactIds) {
                errors += ValidationError("invalid_revealed_fact", "revealedFacts references a fact not created or visible this turn: ${revealed.factId}")
            }
        }
        result.knowledgeUpdates.forEachIndexed { index, update ->
            update.sourceMemoryIds.forEach { memoryId ->
                if (memoryById[memoryId]?.ownerCharacterId != update.characterId) {
                    errors += ValidationError("knowledge_boundary", "knowledgeUpdates[$index] uses memory unavailable to character: $memoryId")
                }
            }
            update.sourceFactIds.forEach { factId ->
                val publicFact = context.facts.any { it.id == factId && it.visibility == "public" }
                if (!publicFact && factId !in revealedFactIds) {
                    errors += ValidationError("unknown_fact", "knowledgeUpdates[$index] uses fact outside current evidence: $factId")
                }
            }
        }
        return errors
    }
}

class MemoryLifecycleService(private val repository: SqliteStoryRepository) {
    fun validate(context: ContextPacket, result: AiTurnResult): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val eventIds = context.recentEvents.map { it.id }.toSet() +
            result.allDeltaOperations().filter { it.type == DeltaOperationType.ADD_EVENT }.mapNotNull { it.targetId }
        val factIds = context.facts.map { it.id }.toSet() +
            result.allDeltaOperations().filter { it.type == DeltaOperationType.ADD_FACT }.mapNotNull { it.targetId }
        val actorIds = result.characterActions.map { it.characterId }.toSet()
        result.memoryCandidates.forEachIndexed { index, candidate ->
            if (context.characters.none { it.id == candidate.ownerCharacterId }) {
                errors += ValidationError("unknown_actor", "memoryCandidates[$index] owner does not exist: ${candidate.ownerCharacterId}")
            }
            if (candidate.ownerCharacterId !in actorIds) {
                errors += ValidationError("memory_perception", "memoryCandidates[$index] owner did not act or perceive this turn.")
            }
            candidate.sourceEventIds.filterNot { it in eventIds }.forEach {
                errors += ValidationError("unknown_event", "memoryCandidates[$index] references unknown event: $it")
            }
            candidate.sourceFactIds.filterNot { it in factIds }.forEach {
                errors += ValidationError("unknown_fact", "memoryCandidates[$index] references unknown fact: $it")
            }
            if (
                result.narrative.isNotBlank() &&
                (candidate.summary.trim() == result.narrative.trim() || candidate.summary.contains(result.narrative.trim()))
            ) {
                errors += ValidationError("low_memory_value", "memoryCandidates[$index] appears to copy the narrative instead of summarizing a memory.")
            }
            if (candidate.summary.length < 8 || candidate.reason.isBlank()) {
                errors += ValidationError("low_memory_value", "memoryCandidates[$index] needs story/character value and a reason.")
            }
            if (candidate.lifecycleAction == "reframe" && candidate.supersedesMemoryId == null) {
                errors += ValidationError("invalid_reframe", "memoryCandidates[$index] reframe requires supersedesMemoryId.")
            }
            if (candidate.supersedesMemoryId != null) {
                val old = repository.getCharacterMemory(context.project.id, candidate.supersedesMemoryId)
                if (old == null || old.ownerCharacterId != candidate.ownerCharacterId) {
                    errors += ValidationError("invalid_reframe", "memoryCandidates[$index] cannot supersede unavailable memory.")
                }
            }
        }
        return errors
    }
}

class EmotionReducer {
    fun validate(context: ContextPacket, result: AiTurnResult): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val eventIds = context.recentEvents.map { it.id }.toSet() +
            result.allDeltaOperations().filter { it.type == DeltaOperationType.ADD_EVENT }.mapNotNull { it.targetId }
        val memoryIds = context.retrievedMemories.map { it.memoryId }.toSet()
        result.allDeltaOperations().forEachIndexed { deltaIndex, operation ->
            if (operation.type != DeltaOperationType.UPDATE_CHARACTER_STATE) return@forEachIndexed
            val deltas = operation.payload["emotionDeltas"] ?: operation.payload["emotion_deltas"] ?: return@forEachIndexed
            val array = runCatching { deltas as JsonArray }.getOrNull()
            if (array == null) {
                errors += ValidationError("invalid_emotion_delta", "stateDeltas[$deltaIndex] emotionDeltas must be an array.")
                return@forEachIndexed
            }
            array.forEachIndexed { emotionIndex, element ->
                val obj = runCatching { element.jsonObject }.getOrNull()
                if (obj == null) {
                    errors += ValidationError("invalid_emotion_delta", "emotionDeltas[$emotionIndex] must be an object.")
                    return@forEachIndexed
                }
                val delta = obj.mkIntField("delta")
                val emotion = obj.mkStringField("emotion")
                val trigger = obj.mkStringField("trigger")
                val sourceType = obj.mkStringField("sourceType") ?: obj.mkStringField("source_type")
                val sourceId = obj.mkStringField("sourceId") ?: obj.mkStringField("source_id")
                val reason = obj.mkStringField("reason")
                if (emotion.isNullOrBlank() || delta == null || trigger.isNullOrBlank() || sourceType.isNullOrBlank() || sourceId.isNullOrBlank() || reason.isNullOrBlank()) {
                    errors += ValidationError("invalid_emotion_delta", "emotionDeltas[$emotionIndex] requires emotion, delta, trigger, sourceType/sourceId and reason.")
                }
                if (delta != null && kotlin.math.abs(delta) > 25 && !(sourceType == "event" && sourceId in eventIds)) {
                    errors += ValidationError("emotion_delta_too_large", "emotionDeltas[$emotionIndex] exceeds 25 without a registered high-severity event.")
                }
                if (sourceType == "memory" && sourceId !in memoryIds) {
                    errors += ValidationError("unknown_memory", "emotionDeltas[$emotionIndex] references unavailable memory: $sourceId")
                }
            }
        }
        return errors
    }
}

private fun JsonObject.mkStringField(key: String): String? =
    this[key]?.jsonPrimitive?.contentOrNull

private fun JsonObject.mkIntField(key: String): Int? =
    mkStringField(key)?.toIntOrNull()

private fun JsonElement.allStrings(): List<String> =
    when (this) {
        is JsonObject -> values.flatMap { it.allStrings() }
        is JsonArray -> flatMap { it.allStrings() }
        else -> listOfNotNull(jsonPrimitive.contentOrNull)
    }
