package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.ApplyResult
import com.localnovel.engine.model.CharacterState
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.Event
import com.localnovel.engine.model.Fact
import com.localnovel.engine.model.Task
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.repository.StoryRepository
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class StateReducer(private val repository: StoryRepository) {
    fun apply(context: ContextPacket, result: AiTurnResult): ApplyResult {
        val hasStateChange = result.stateDelta.operations.any { it.type != DeltaOperationType.NOOP }

        result.stateDelta.operations.forEach { operation ->
            repository.recordStateDelta(
                projectId = context.project.id,
                turnId = context.turnId,
                operationType = operation.type.name.lowercase(),
                targetType = operation.targetType,
                targetId = operation.targetId,
                payloadJson = JsonSupport.json.encodeToString(operation.payload),
            )
            applyOperation(context, operation)
        }

        val turnLogId = repository.saveTurnLog(context, result, hasStateChange)
        repository.updateProjectTurn(context.project.id, context.turnNumber)
        return ApplyResult(
            turnLogId = turnLogId,
            narrative = result.narrative,
            hasStateChange = hasStateChange,
        )
    }

    private fun applyOperation(context: ContextPacket, operation: DeltaOperation) {
        when (operation.type) {
            DeltaOperationType.UPDATE_CHARACTER_STATE -> updateCharacterState(context, operation)
            DeltaOperationType.ADD_FACT -> addFact(context, operation)
            DeltaOperationType.REVEAL_SECRET -> repository.revealSecret(context.project.id, operation.targetId.orEmpty(), context.turnNumber)
            DeltaOperationType.ADD_EVENT -> addEvent(context, operation)
            DeltaOperationType.UPSERT_TASK -> upsertTask(context, operation)
            DeltaOperationType.NOOP -> Unit
        }
    }

    private fun updateCharacterState(context: ContextPacket, operation: DeltaOperation) {
        val characterId = requireNotNull(operation.targetId) { "update_character_state requires targetId" }
        val current = repository.getCharacterState(context.project.id, characterId)
            ?: error("Character state not found: $characterId")
        val nextFlags = current.flags.toMutableMap()
        operation.payload.decodeField<Map<String, String>>("flags")?.let { nextFlags.putAll(it) }
        val next = CharacterState(
            characterId = current.characterId,
            projectId = current.projectId,
            locationId = operation.payload.stringField("locationId")
                ?: operation.payload.stringField("location_id")
                ?: current.locationId,
            mood = operation.payload.stringField("mood") ?: current.mood,
            health = operation.payload.stringField("health") ?: current.health,
            inventory = operation.payload.decodeField("inventory") ?: current.inventory,
            flags = nextFlags,
        )
        repository.updateCharacterState(next)
    }

    private fun addFact(context: ContextPacket, operation: DeltaOperation) {
        val payload = operation.payload
        repository.addFact(
            Fact(
                id = operation.targetId ?: payload.stringField("id") ?: SqliteStoryRepository.newId("fact"),
                projectId = context.project.id,
                content = requireNotNull(payload.stringField("content")) { "add_fact requires content" },
                visibility = payload.stringField("visibility") ?: "public",
                source = payload.stringField("source") ?: "ai_turn_result",
                createdTurn = context.turnNumber,
            ),
        )
    }

    private fun addEvent(context: ContextPacket, operation: DeltaOperation) {
        val payload = operation.payload
        repository.addEvent(
            Event(
                id = operation.targetId ?: payload.stringField("id") ?: SqliteStoryRepository.newId("event"),
                projectId = context.project.id,
                turnId = context.turnId,
                summary = requireNotNull(payload.stringField("summary")) { "add_event requires summary" },
                payload = payload["payload"]?.jsonObjectOrNull() ?: payload,
            ),
        )
    }

    private fun upsertTask(context: ContextPacket, operation: DeltaOperation) {
        val payload = operation.payload
        val taskId = operation.targetId ?: payload.stringField("id") ?: SqliteStoryRepository.newId("task")
        val existing = repository.getTask(context.project.id, taskId)
        repository.upsertTask(
            Task(
                id = taskId,
                projectId = context.project.id,
                title = payload.stringField("title") ?: existing?.title ?: error("upsert_task requires title"),
                status = payload.stringField("status") ?: existing?.status ?: "open",
                payload = payload["payload"]?.jsonObjectOrNull() ?: existing?.payload ?: JsonObject(emptyMap()),
                createdTurn = existing?.createdTurn ?: context.turnNumber,
                updatedTurn = context.turnNumber,
            ),
        )
    }
}

private inline fun <reified T> JsonObject.decodeField(key: String): T? {
    val element = this[key] ?: return null
    return JsonSupport.json.decodeFromJsonElementCompat(element)
}

private inline fun <reified T> kotlinx.serialization.json.Json.decodeFromJsonElementCompat(element: JsonElement): T =
    decodeFromString(encodeToString(element))

private fun JsonElement.jsonObjectOrNull(): JsonObject? =
    runCatching { jsonObject }.getOrNull()
