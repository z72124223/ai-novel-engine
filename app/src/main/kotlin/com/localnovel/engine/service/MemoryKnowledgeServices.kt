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

class ContextBudgetUnsatisfiableException(message: String) : IllegalStateException(message)

class ContextPacketFinalizer(
    private val maxContextBytes: Int = 24576,
) {
    fun finalize(context: ContextPacket, hashContext: (ContextPacket) -> String): ContextPacket {
        var candidate = context.copy(contextHash = "")
        val reasons = candidate.contextBudget.truncationReasons.toMutableList()
        repeat(512) {
            val finalized = withStableAccounting(candidate, hashContext, reasons)
            if (encodedSize(finalized) <= maxContextBytes) return finalized
            val reduced = reduceOne(candidate, reasons)
                ?: throw ContextBudgetUnsatisfiableException(
                    "CONTEXT_BUDGET_UNSATISFIABLE: final ContextPacket is ${encodedSize(finalized)} bytes, max is $maxContextBytes",
                )
            candidate = reduced.copy(contextHash = "")
        }
        val finalized = withStableAccounting(candidate, hashContext, reasons)
        throw ContextBudgetUnsatisfiableException(
            "CONTEXT_BUDGET_UNSATISFIABLE: context budget did not converge, final size is ${encodedSize(finalized)} bytes, max is $maxContextBytes",
        )
    }

    private fun withStableAccounting(
        context: ContextPacket,
        hashContext: (ContextPacket) -> String,
        reasons: List<String>,
    ): ContextPacket {
        var candidate = context.copy(contextHash = "")
        var previousBytes = -1
        var previousHash = ""
        repeat(24) {
            val withHash = candidate.copy(contextHash = hashContext(candidate))
            val actualBytes = encodedSize(withHash)
            val settingBytes = JsonSupport.json.encodeToString(withHash.relevantSettings).toByteArray(Charsets.UTF_8).size
            val next = withHash.copy(
                contextBudget = withHash.contextBudget.copy(
                    maxBytes = maxContextBytes,
                    actualBytes = actualBytes,
                    includedMemoryCount = withHash.retrievedMemories.size,
                    includedKnowledgeCount = withHash.knowledgeClaims.size,
                    truncationApplied = reasons.isNotEmpty(),
                    truncationReasons = reasons.distinct(),
                ),
                settingContextBudget = withHash.settingContextBudget.copy(
                    actualBytes = settingBytes,
                    includedSettingCount = withHash.relevantSettings.size,
                    truncationApplied = withHash.settingContextBudget.omittedSettingCount > 0 ||
                        withHash.settingContextBudget.reasons.isNotEmpty() ||
                        reasons.any { it.contains("setting", ignoreCase = true) },
                    reasons = (withHash.settingContextBudget.reasons + reasons.filter { it.contains("setting", ignoreCase = true) }).distinct(),
                ),
            )
            if (actualBytes == previousBytes && next.contextHash == previousHash) return next
            previousBytes = actualBytes
            previousHash = next.contextHash
            candidate = next.copy(contextHash = "")
        }
        return candidate.copy(contextHash = hashContext(candidate))
    }

    private fun reduceOne(context: ContextPacket, reasons: MutableList<String>): ContextPacket? {
        if (context.relevantSettings.isNotEmpty()) {
            reasons += "omitted lowest-priority relevant setting"
            return context.copy(
                relevantSettings = context.relevantSettings.dropLast(1),
                settingContextBudget = context.settingContextBudget.copy(
                    omittedSettingCount = context.settingContextBudget.omittedSettingCount + 1,
                    truncationApplied = true,
                    reasons = (context.settingContextBudget.reasons + "setting_context_budget_exceeded").distinct(),
                ),
            )
        }
        if (context.retrievedMemories.isNotEmpty()) {
            val memories = context.retrievedMemories.dropLast(1)
            reasons += "omitted lowest-ranked memory"
            return context.copy(
                retrievedMemories = memories,
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
                contextBudget = context.contextBudget.copy(
                    omittedMemoryCount = context.contextBudget.omittedMemoryCount + 1,
                    truncationApplied = true,
                ),
            )
        }
        if (context.knowledgeClaims.isNotEmpty()) {
            reasons += "omitted lowest-priority knowledge claim"
            return context.copy(
                knowledgeClaims = context.knowledgeClaims.dropLast(1),
                contextBudget = context.contextBudget.copy(
                    omittedKnowledgeCount = context.contextBudget.omittedKnowledgeCount + 1,
                    truncationApplied = true,
                ),
            )
        }
        if (context.recentTurnSummaries.isNotEmpty()) {
            reasons += "omitted oldest recent turn summary"
            return context.copy(recentTurnSummaries = context.recentTurnSummaries.drop(1))
        }
        if (context.activeStoryThreads.size > 1) {
            reasons += "omitted lowest-priority story thread view"
            return context.copy(activeStoryThreads = context.activeStoryThreads.dropLast(1))
        }
        if (context.pendingWorldEvents.size > 1) {
            reasons += "omitted lowest-priority pending world event"
            return context.copy(pendingWorldEvents = context.pendingWorldEvents.dropLast(1))
        }
        if (context.availableTransitions.size > 3) {
            reasons += "omitted surplus transition previews"
            return context.copy(availableTransitions = context.availableTransitions.take(3))
        }
        if (context.sceneRequirements.size > 6) {
            reasons += "omitted surplus scene requirement previews"
            return context.copy(sceneRequirements = context.sceneRequirements.take(6))
        }
        shrinkDistantCharacters(context)?.let {
            reasons += "omitted distant character profiles"
            return it
        }
        shrinkDistantItemsAndTechniques(context)?.let {
            reasons += "omitted distant item and technique profiles"
            return it
        }
        if (context.openTasks.size > 5) {
            reasons += "omitted surplus open tasks"
            return context.copy(openTasks = context.openTasks.take(5))
        }
        if (context.facts.size > 12) {
            reasons += "omitted surplus public facts"
            return context.copy(facts = context.facts.takeLast(12))
        }
        if (context.worldRules.size > 6) {
            reasons += "omitted lower-priority world rules"
            return context.copy(worldRules = context.worldRules.take(6))
        }
        shrinkCharacterStateFlags(context, 160)?.let {
            reasons += "trimmed character state flags"
            return it
        }
        shrinkCharacterStateFlags(context, 80)?.let {
            reasons += "trimmed character state flags aggressively"
            return it
        }
        trimCharacters(context, 240)?.let {
            reasons += "trimmed character public descriptions"
            return it
        }
        trimCharacters(context, 80)?.let {
            reasons += "trimmed character public descriptions aggressively"
            return it
        }
        trimCharacters(context, 0)?.let {
            reasons += "omitted character public descriptions"
            return it
        }
        trimItems(context, 120)?.let {
            reasons += "trimmed item and technique descriptions"
            return it
        }
        trimWorldRules(context, 240)?.let {
            reasons += "trimmed world rule public text"
            return it
        }
        trimSceneAndProject(context, 320)?.let {
            reasons += "trimmed project or scene summary"
            return it
        }
        return null
    }

    private fun shrinkDistantCharacters(context: ContextPacket): ContextPacket? {
        val currentLocation = context.activeScene?.locationId
        val activeIds = context.characterStates
            .filter { currentLocation == null || it.locationId == currentLocation }
            .map { it.characterId }
            .toSet()
        val keepIds = activeIds + context.characters.filter { it.isPlayer }.map { it.id }
        val kept = context.characters.filter { it.id in keepIds }
        return if (kept.isNotEmpty() && kept.size < context.characters.size) context.copy(characters = kept) else null
    }

    private fun shrinkDistantItemsAndTechniques(context: ContextPacket): ContextPacket? {
        val currentLocation = context.activeScene?.locationId
        val activeCharacterIds = context.characterStates
            .filter { currentLocation == null || it.locationId == currentLocation }
            .map { it.characterId }
            .toSet()
        val inventoryIds = context.characterStates.flatMap { it.inventory }.toSet()
        val items = context.items.filter { item ->
            item.id in inventoryIds || item.ownerCharacterId in activeCharacterIds || item.locationId == currentLocation
        }
        val techniques = context.techniques.filter { it.ownerCharacterId in activeCharacterIds }
        return if (items.size < context.items.size || techniques.size < context.techniques.size) {
            context.copy(items = items, techniques = techniques)
        } else {
            null
        }
    }

    private fun shrinkCharacterStateFlags(context: ContextPacket, maxChars: Int): ContextPacket? {
        var changed = false
        val states = context.characterStates.map { state ->
            val flags = state.flags.mapValues { (key, value) ->
                val keepFull = key in setOf("current_goal", "dominant_emotion", "idle_turn_count") || key.startsWith("emotion.")
                val trimmed = if (keepFull) value else trimText(value, maxChars)
                if (trimmed != value) changed = true
                trimmed
            }
            state.copy(flags = flags)
        }
        return if (changed) context.copy(characterStates = states) else null
    }

    private fun trimCharacters(context: ContextPacket, maxChars: Int): ContextPacket? {
        var changed = false
        val characters = context.characters.map { character ->
            val trimmed = trimText(character.description, maxChars)
            if (trimmed != character.description) changed = true
            character.copy(description = trimmed)
        }
        return if (changed) context.copy(characters = characters) else null
    }

    private fun trimItems(context: ContextPacket, maxChars: Int): ContextPacket? {
        var changed = false
        val items = context.items.map { item ->
            val trimmed = trimText(item.description, maxChars)
            if (trimmed != item.description) changed = true
            item.copy(description = trimmed)
        }
        val techniques = context.techniques.map { technique ->
            val trimmed = trimText(technique.description, maxChars)
            if (trimmed != technique.description) changed = true
            technique.copy(description = trimmed)
        }
        return if (changed) context.copy(items = items, techniques = techniques) else null
    }

    private fun trimWorldRules(context: ContextPacket, maxChars: Int): ContextPacket? {
        var changed = false
        val rules = context.worldRules.map { rule ->
            val trimmed = trimText(rule.content, maxChars)
            if (trimmed != rule.content) changed = true
            rule.copy(content = trimmed)
        }
        return if (changed) context.copy(worldRules = rules) else null
    }

    private fun trimSceneAndProject(context: ContextPacket, maxChars: Int): ContextPacket? {
        val projectSummary = trimText(context.project.summary, maxChars)
        val sceneSummary = context.activeScene?.summary?.let { trimText(it, maxChars) }
        val changed = projectSummary != context.project.summary || (sceneSummary != null && sceneSummary != context.activeScene?.summary)
        return if (changed) {
            context.copy(
                project = context.project.copy(summary = projectSummary),
                activeScene = context.activeScene?.copy(summary = sceneSummary ?: context.activeScene.summary),
            )
        } else {
            null
        }
    }

    private fun trimText(text: String, maxChars: Int): String =
        when {
            text.length <= maxChars -> text
            maxChars <= 0 -> ""
            maxChars <= 3 -> text.take(maxChars)
            else -> text.take(maxChars - 3) + "..."
        }

    private fun encodedSize(context: ContextPacket): Int =
        JsonSupport.json.encodeToString(context).toByteArray(Charsets.UTF_8).size
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
