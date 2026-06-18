package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.ApplyResult
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.CharacterMemory
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DirectorPressure
import com.localnovel.engine.model.OutputRequirements
import com.localnovel.engine.model.StorySession
import com.localnovel.engine.model.ValidationError
import com.localnovel.engine.model.ValidationReport
import com.localnovel.engine.model.allDeltaOperations
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.security.MessageDigest
import java.time.Instant

class MultiTurnRuntime(private val repository: SqliteStoryRepository) {
    fun startSession(projectId: String, sceneId: String): StorySession {
        require(repository.getProject(projectId) != null) { "Project not found: $projectId" }
        require(repository.targetExists(projectId, "scene", sceneId)) { "Scene not found: $sceneId" }
        return repository.startSession(projectId, sceneId)
    }

    fun prepareTurn(sessionId: String, playerInput: String): ContextPacket {
        val context = buildTurnContext(sessionId, playerInput)
        repository.savePendingContext(context)
        return context
    }

    fun previewTurnContext(sessionId: String, playerInput: String): ContextPacket =
        buildTurnContext(sessionId, playerInput)

    private fun buildTurnContext(sessionId: String, playerInput: String): ContextPacket {
        val session = repository.getSession(sessionId) ?: error("Session not found: $sessionId")
        require(session.status == "active") { "Session is not active: $sessionId" }
        val project = repository.getProject(session.projectId) ?: error("Project not found: ${session.projectId}")
        val activeScene = repository.getActiveScene(session.projectId) ?: error("Active scene not found.")
        require(activeScene.id == session.currentSceneId) { "Session scene '${session.currentSceneId}' does not match project active scene '${activeScene.id}'." }
        val noChangeFlags = repository.lastTurnStateChangeFlags(session.projectId, 2)
        val stalled = noChangeFlags.size == 2 && noChangeFlags.all { !it }
        val contextId = SqliteStoryRepository.newId("ctx")
        val memorySelection = MemoryRetriever(repository).retrieve(session, playerInput)
        val contextMemories = memorySelection.retrievedMemories.map {
            CharacterMemory(
                id = it.memoryId,
                projectId = project.id,
                characterId = it.ownerCharacterId,
                ownerCharacterId = it.ownerCharacterId,
                memoryType = it.type,
                summary = it.summary,
                emotionalSignature = it.emotionalSignature,
                salience = it.salience,
                confidence = it.confidence,
            )
        }
        val baseContext = ContextPacket(
            protocolVersion = SUPPORTED_PROTOCOL_VERSION,
            project = project,
            turnId = "${session.id}-turn-${session.nextTurnNumber}",
            turnNumber = session.nextTurnNumber,
            playerInput = playerInput,
            activeScene = activeScene,
            sceneContract = repository.getSceneContract(session.projectId, activeScene.id),
            characters = repository.listCharacters(session.projectId),
            characterMemories = contextMemories,
            characterStates = repository.listCharacterStates(session.projectId),
            worldRules = repository.listWorldRules(session.projectId),
            facts = repository.listFacts(session.projectId),
            revealedSecrets = repository.listRevealedSecrets(session.projectId),
            items = repository.listItems(session.projectId),
            techniques = repository.listTechniques(session.projectId),
            recentEvents = repository.listRecentEvents(session.projectId, 3),
            openTasks = repository.listOpenTasks(session.projectId),
            contextId = contextId,
            sessionId = session.id,
            baseStateVersion = session.stateVersion,
            contextHash = "",
            generatedAt = Instant.now().toString(),
            recentTurnSummaries = repository.listRecentTurnSummaries(session.id, 3),
            sceneProgress = activeScene.progress,
            directorPressure = if (stalled) {
                DirectorPressure(
                    enabled = true,
                    allowedPressureTypes = listOf("time_pressure", "environment_change", "external_force_action", "item_reaction", "approaching_danger"),
                    mustProduceMeaningfulStateChange = true,
                )
            } else {
                DirectorPressure()
            },
            outputRequirements = OutputRequirements(
                mustProduceMeaningfulStateChange = stalled,
            ),
            retrievedMemories = memorySelection.retrievedMemories,
            knowledgeClaims = memorySelection.knowledgeClaims,
            sceneLifecycle = SceneLifecycleService(repository).lifecycle(session.id),
            sceneRequirements = repository.listSceneRequirementViews(session.id),
            activeStoryThreads = repository.listActiveStoryThreadViews(session.id),
            availableTransitions = TransitionPlanner(repository).plan(session.id, playerInput),
            pendingWorldEvents = repository.listPendingWorldEventViews(session.id),
            worldClock = com.localnovel.engine.model.WorldClock(
                worldTime = session.worldTime,
                elapsedMinutes = session.elapsedMinutes,
                turnNumber = session.nextTurnNumber,
            ),
            continuityConstraints = repository.getContinuityConstraints(session.id),
        )
        val enrichedContext = SettingContextResolver(repository).enrich(baseContext)
        val contextWithoutHash = ContextBudgeter().applyBudget(
            enrichedContext,
            omittedMemoryCount = memorySelection.omittedMemoryCount,
            omittedKnowledgeCount = memorySelection.omittedKnowledgeCount,
        )
        return ContextPacketFinalizer().finalize(contextWithoutHash, ::hashContext)
    }

    fun resolveTurn(sessionId: String, result: AiTurnResult): ApplyResult {
        val context = repository.getPendingContext(sessionId, result.contextId)
            ?: error("Pending context not found for session '$sessionId' and context '${result.contextId}'.")
        val report = validate(context, result)
        if (!report.isValid) {
            error(report.errors.joinToString(separator = "\n") { "${it.code}: ${it.message}" })
        }
        val hasStateChange = result.allDeltaOperations().any { it.type != com.localnovel.engine.model.DeltaOperationType.NOOP } ||
            result.revealedFacts.isNotEmpty() ||
            result.sceneProgressUpdates.any { it.status == "completed" } ||
            result.sceneRequirementUpdates.isNotEmpty() ||
            result.threadProgressUpdates.isNotEmpty() ||
            result.transitionProposal != null ||
            result.worldEventAcknowledgements.isNotEmpty() ||
            result.timeAdvanceProposal != null
        return repository.resolvePendingTurn(context, result, hasStateChange)
    }

    fun validate(context: ContextPacket, result: AiTurnResult): ValidationReport {
        val errors = mutableListOf<ValidationError>()
        if (context.protocolVersion != SUPPORTED_PROTOCOL_VERSION || result.protocolVersion != SUPPORTED_PROTOCOL_VERSION) {
            errors += ValidationError("unsupported_protocol", "Only protocolVersion $SUPPORTED_PROTOCOL_VERSION is supported for multi-turn runtime.")
        }
        if (result.sessionId != context.sessionId) errors += ValidationError("session_mismatch", "sessionId does not match context.")
        if (result.contextId != context.contextId) errors += ValidationError("context_mismatch", "contextId does not match context.")
        if (result.turnNumber != context.turnNumber) errors += ValidationError("turn_mismatch", "turnNumber does not match context.")
        if (result.baseStateVersion != context.baseStateVersion) errors += ValidationError("state_version_mismatch", "baseStateVersion does not match context.")
        if (result.contextHash != context.contextHash) errors += ValidationError("hash_mismatch", "contextHash does not match context.")
        if (result.resultId.isBlank()) errors += ValidationError("missing_result_id", "resultId is required.")
        if (result.turnSummary.isBlank()) errors += ValidationError("missing_turn_summary", "turnSummary is required.")
        if (result.narrative.isBlank()) errors += ValidationError("blank_narrative", "narrative is required.")
        if (result.resultId.isNotBlank() && repository.appliedResultExists(result.resultId)) {
            errors += ValidationError("duplicate_result", "resultId was already applied: ${result.resultId}")
        }

        val session = repository.getSession(context.sessionId)
        if (session == null) {
            errors += ValidationError("missing_session", "Session not found: ${context.sessionId}")
        } else {
            if (session.stateVersion != result.baseStateVersion) {
                errors += ValidationError("stale_state", "Current stateVersion is ${session.stateVersion}, result baseStateVersion is ${result.baseStateVersion}.")
            }
            if (session.nextTurnNumber != result.turnNumber) {
                errors += ValidationError("stale_turn", "Current nextTurnNumber is ${session.nextTurnNumber}, result turnNumber is ${result.turnNumber}.")
            }
        }

        val allowedSecretIds = context.sceneContract?.allowedSecretIds.orEmpty().toSet()
        result.allDeltaOperations().forEachIndexed { index, operation ->
            errors += validateDelta(context, operation, index, allowedSecretIds)
        }
        result.characterActions.forEachIndexed { index, action ->
            errors += validateCharacterAction(context, action, index)
        }
        errors += SceneLifecycleService(repository).validate(context)
        errors += SceneRequirementEvaluator().validate(context, result)
        errors += StoryThreadManager(repository).validate(context, result)
        errors += TransitionValidator(repository).validate(context, result)
        errors += WorldClockService().validate(result)
        errors += ScheduledWorldEventService().validate(context, result)
        errors += NarrativeContinuityValidator(repository).validate(context, result)
        errors += KnowledgeBoundaryValidator(repository).validate(context, result)
        errors += MemoryLifecycleService(repository).validate(context, result)
        errors += EmotionReducer().validate(context, result)
        if (result.characterActions.isEmpty()) {
            errors += ValidationError("missing_character_actions", "At least one characterAction is required.")
        }

        val textToScan = buildList {
            add(result.narrative)
            add(result.turnSummary)
            addAll(result.revealedFacts.map { it.content })
            addAll(result.memoryEffects.map { it.effect })
            addAll(result.newTasks.map { it.title })
            result.memoryCandidates.forEach {
                add(it.summary)
                add(it.reason)
                addAll(it.tags)
                addAll(it.emotionalSignature.allStringValues())
            }
            result.knowledgeUpdates.forEach {
                add(it.propositionKey)
                add(it.requestedState)
                add(it.reason)
            }
            result.sceneRequirementUpdates.forEach {
                add(it.requirementId)
                add(it.requestedStatus)
                add(it.reason)
            }
            result.threadProgressUpdates.forEach {
                add(it.threadId)
                add(it.requestedStatus)
                add(it.reason)
            }
            result.transitionProposal?.let {
                add(it.transitionId)
                add(it.toSceneId)
                add(it.playerIntentEvidence)
                add(it.worldEventEvidence.orEmpty())
                add(it.reason)
            }
            result.worldEventAcknowledgements.forEach {
                add(it.eventId)
                add(it.narrativeUse)
                add(it.reason)
                addAll(it.resultingEventIds)
            }
            result.timeAdvanceProposal?.let {
                add(it.sourceType)
                add(it.sourceId)
                add(it.reason)
            }
            result.allDeltaOperations().forEach { addAll(it.payload.allStringValues()) }
        }
        val requestedRevealIds = result.allDeltaOperations()
            .filter { it.type == com.localnovel.engine.model.DeltaOperationType.REVEAL_SECRET }
            .mapNotNull { it.targetId }
            .toSet()
        repository.listSecrets(context.project.id)
            .filterNot { it.revealed }
            .filterNot { it.id in requestedRevealIds && it.id in allowedSecretIds }
            .forEach { secret ->
                if (secret.content.isNotBlank() && textToScan.any { it.contains(secret.content) }) {
                    errors += ValidationError("forbidden_reveal", "Hidden secret '${secret.id}' is revealed before it is unlocked.")
                }
            }
        context.sceneContract?.forbiddenReveals.orEmpty().forEach { phrase ->
            if (phrase.isNotBlank() && textToScan.any { it.contains(phrase) }) {
                errors += ValidationError("forbidden_reveal", "Forbidden reveal phrase appears: '$phrase'.")
            }
        }
        return ValidationReport(errors)
    }

    fun hashContext(context: ContextPacket): String {
        val canonical = JsonSupport.json.encodeToString(context.copy(contextHash = ""))
        val bytes = MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun validateDelta(
        context: ContextPacket,
        operation: com.localnovel.engine.model.DeltaOperation,
        index: Int,
        allowedSecretIds: Set<String>,
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        when (operation.type) {
            com.localnovel.engine.model.DeltaOperationType.UPDATE_CHARACTER_STATE -> {
                val targetId = operation.targetId
                if (targetId == null || !repository.targetExists(context.project.id, "character_state", targetId)) {
                    errors += ValidationError("missing_target", "stateDeltas[$index] target character state does not exist.")
                }
            }
            com.localnovel.engine.model.DeltaOperationType.ADD_FACT -> {
                if (operation.payload.mtStringField("content").isNullOrBlank()) {
                    errors += ValidationError("invalid_delta", "stateDeltas[$index] add_fact requires payload.content.")
                }
            }
            com.localnovel.engine.model.DeltaOperationType.REVEAL_SECRET -> {
                val targetId = operation.targetId
                if (targetId == null || !repository.targetExists(context.project.id, "secret", targetId)) {
                    errors += ValidationError("missing_target", "stateDeltas[$index] target secret does not exist.")
                } else if (targetId !in allowedSecretIds) {
                    errors += ValidationError("forbidden_reveal", "Secret '$targetId' is not allowed by the scene contract.")
                }
            }
            com.localnovel.engine.model.DeltaOperationType.ADD_EVENT -> {
                if (operation.payload.mtStringField("summary").isNullOrBlank()) {
                    errors += ValidationError("invalid_delta", "stateDeltas[$index] add_event requires payload.summary.")
                }
            }
            com.localnovel.engine.model.DeltaOperationType.UPSERT_TASK -> {
                val exists = operation.targetId?.let { repository.targetExists(context.project.id, "task", it) } == true
                if (!exists && operation.payload.mtStringField("title").isNullOrBlank()) {
                    errors += ValidationError("invalid_delta", "stateDeltas[$index] upsert_task requires title for new task.")
                }
            }
            com.localnovel.engine.model.DeltaOperationType.NOOP -> Unit
        }
        return errors
    }

    private fun validateCharacterAction(context: ContextPacket, action: CharacterAction, index: Int): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        if (context.characters.none { it.id == action.characterId }) {
            errors += ValidationError("unknown_actor", "characterActions[$index] actor is not in context: ${action.characterId}")
        }
        if (action.characterFitScore < 70) {
            errors += ValidationError("low_character_fit", "characterActions[$index] characterFitScore must be >= 70.")
        }
        val memoryIds = (context.characterMemories.map { it.id } + context.retrievedMemories.map { it.memoryId }).toSet()
        val itemIds = context.items.map { it.id }.toSet()
        val techniqueIds = context.techniques.map { it.id }.toSet()
        action.usedMemoryIds.filterNot { it in memoryIds }.forEach {
            errors += ValidationError("unknown_memory", "characterActions[$index] uses unknown memory: $it")
        }
        action.usedItemIds.filterNot { it in itemIds }.forEach {
            errors += ValidationError("unknown_item", "characterActions[$index] uses unknown item: $it")
        }
        action.usedTechniqueIds.filterNot { it in techniqueIds }.forEach {
            errors += ValidationError("unknown_technique", "characterActions[$index] uses unknown technique: $it")
        }
        return errors
    }

    companion object {
        const val SUPPORTED_PROTOCOL_VERSION = "2"
    }
}

private fun JsonObject.mtStringField(key: String): String? =
    this[key]?.jsonPrimitive?.contentOrNull

private fun JsonElement.allStringValues(): List<String> =
    when (this) {
        is JsonObject -> values.flatMap { it.allStringValues() }
        is JsonArray -> flatMap { it.allStringValues() }
        is JsonPrimitive -> listOfNotNull(contentOrNull)
    }
