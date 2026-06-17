package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.ValidationError
import com.localnovel.engine.model.ValidationReport
import com.localnovel.engine.repository.StoryRepository
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class AiTurnResultValidator(private val repository: StoryRepository) {
    fun validate(context: ContextPacket, result: AiTurnResult): ValidationReport {
        val errors = mutableListOf<ValidationError>()

        if (result.projectId != context.project.id) {
            errors += ValidationError("project_mismatch", "Result projectId '${result.projectId}' does not match context project '${context.project.id}'.")
        }
        if (result.turnId != context.turnId) {
            errors += ValidationError("turn_mismatch", "Result turnId '${result.turnId}' does not match context turn '${context.turnId}'.")
        }
        if (result.narrative.isBlank()) {
            errors += ValidationError("blank_narrative", "Narrative must not be blank.")
        }

        val allowedSecretIds = context.sceneContract?.allowedSecretIds.orEmpty().toSet()
        val requestedRevealIds = result.stateDelta.operations
            .filter { it.type == DeltaOperationType.REVEAL_SECRET }
            .mapNotNull { it.targetId }
            .toSet()

        result.stateDelta.operations.forEachIndexed { index, operation ->
            validateOperation(context, operation, index, allowedSecretIds, errors)
        }

        val operationPayloadText = result.stateDelta.operations.flatMap { it.payload.allStringValues() }

        repository.listSecrets(context.project.id)
            .filterNot { it.revealed }
            .filterNot { it.id in requestedRevealIds && it.id in allowedSecretIds }
            .forEach { secret ->
                if (secret.content.isNotBlank() && result.narrative.contains(secret.content)) {
                    errors += ValidationError(
                        "forbidden_reveal",
                        "Narrative reveals hidden secret '${secret.id}' without an allowed reveal operation.",
                    )
                }
                if (secret.content.isNotBlank() && operationPayloadText.any { it.contains(secret.content) }) {
                    errors += ValidationError(
                        "forbidden_reveal",
                        "State delta reveals hidden secret '${secret.id}' without an allowed reveal operation.",
                    )
                }
            }

        context.sceneContract?.forbiddenReveals.orEmpty().forEach { phrase ->
            if (phrase.isNotBlank() && result.narrative.contains(phrase)) {
                errors += ValidationError("forbidden_reveal", "Narrative contains forbidden reveal phrase: '$phrase'.")
            }
            if (phrase.isNotBlank() && operationPayloadText.any { it.contains(phrase) }) {
                errors += ValidationError("forbidden_reveal", "State delta contains forbidden reveal phrase: '$phrase'.")
            }
        }

        return ValidationReport(errors)
    }

    private fun validateOperation(
        context: ContextPacket,
        operation: DeltaOperation,
        index: Int,
        allowedSecretIds: Set<String>,
        errors: MutableList<ValidationError>,
    ) {
        fun missingTarget() {
            errors += ValidationError("missing_target", "Operation #$index (${operation.type}) requires targetId.")
        }

        when (operation.type) {
            DeltaOperationType.UPDATE_CHARACTER_STATE -> {
                val targetId = operation.targetId
                if (targetId == null) {
                    missingTarget()
                } else if (!repository.targetExists(context.project.id, "character_state", targetId)) {
                    errors += ValidationError("missing_target", "Character state target '$targetId' does not exist.")
                }
            }

            DeltaOperationType.ADD_FACT -> {
                val content = operation.payload.stringField("content")
                if (content.isNullOrBlank()) {
                    errors += ValidationError("invalid_payload", "add_fact operation #$index requires payload.content.")
                }
            }

            DeltaOperationType.REVEAL_SECRET -> {
                val targetId = operation.targetId
                if (targetId == null) {
                    missingTarget()
                } else {
                    if (!repository.targetExists(context.project.id, "secret", targetId)) {
                        errors += ValidationError("missing_target", "Secret target '$targetId' does not exist.")
                    }
                    if (targetId !in allowedSecretIds) {
                        errors += ValidationError("forbidden_reveal", "Secret '$targetId' is not allowed by the active scene contract.")
                    }
                }
            }

            DeltaOperationType.ADD_EVENT -> {
                val summary = operation.payload.stringField("summary")
                if (summary.isNullOrBlank()) {
                    errors += ValidationError("invalid_payload", "add_event operation #$index requires payload.summary.")
                }
            }

            DeltaOperationType.UPSERT_TASK -> {
                val hasExistingTarget = operation.targetId?.let { repository.targetExists(context.project.id, "task", it) } == true
                val title = operation.payload.stringField("title")
                if (!hasExistingTarget && title.isNullOrBlank()) {
                    errors += ValidationError("invalid_payload", "upsert_task operation #$index requires payload.title when creating a task.")
                }
            }

            DeltaOperationType.NOOP -> Unit
        }
    }
}

internal fun kotlinx.serialization.json.JsonObject.stringField(key: String): String? =
    this[key]?.jsonPrimitive?.contentOrNull

private fun JsonElement.allStringValues(): List<String> =
    when (this) {
        is JsonObject -> values.flatMap { it.allStringValues() }
        is JsonArray -> flatMap { it.allStringValues() }
        else -> listOfNotNull(jsonPrimitive.contentOrNull)
    }
