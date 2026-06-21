package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.AvailableTransition
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.PendingWorldEventView
import com.localnovel.engine.model.SceneLifecycle
import com.localnovel.engine.model.ValidationError
import com.localnovel.engine.model.allDeltaOperations
import com.localnovel.engine.repository.SqliteStoryRepository

class SceneLifecycleService(private val repository: SqliteStoryRepository) {
    fun lifecycle(sessionId: String): SceneLifecycle = repository.getSceneLifecycle(sessionId)

    fun validate(context: ContextPacket): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        if (context.sceneLifecycle.sceneInstanceId.isBlank()) {
            errors += ValidationError("missing_scene_instance", "Context must include the active scene instance.")
        }
        if (context.sceneLifecycle.sceneId != context.activeScene?.id) {
            errors += ValidationError("scene_instance_mismatch", "Scene lifecycle does not match active scene.")
        }
        if (repository.activeSceneInstanceCount(context.sessionId) != 1) {
            errors += ValidationError("scene_lifecycle_invalid", "A session must have exactly one ACTIVE scene instance.")
        }
        return errors
    }
}

class SceneRequirementEvaluator {
    fun validate(context: ContextPacket, result: AiTurnResult): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val requirements = context.sceneRequirements.associateBy { it.requirementId }
        val validStatuses = setOf("OPEN", "IN_PROGRESS", "COMPLETED", "FAILED")
        val eventIds = context.recentEvents.map { it.id }.toSet() +
            result.allDeltaOperations().filter { it.type == DeltaOperationType.ADD_EVENT }.mapNotNull { it.targetId } +
            result.worldEventAcknowledgements.flatMap { it.resultingEventIds }
        val factIds = context.facts.map { it.id }.toSet() +
            result.revealedFacts.map { it.factId } +
            result.allDeltaOperations().filter { it.type == DeltaOperationType.ADD_FACT }.mapNotNull { it.targetId }
        val hasDeltaEvidence = result.allDeltaOperations().any { it.type != DeltaOperationType.NOOP }

        result.sceneRequirementUpdates.forEachIndexed { index, update ->
            val requirement = requirements[update.requirementId]
            if (requirement == null) {
                errors += ValidationError("unknown_scene_requirement", "sceneRequirementUpdates[$index] references unknown requirement: ${update.requirementId}")
                return@forEachIndexed
            }
            if (update.requestedStatus !in validStatuses) {
                errors += ValidationError("invalid_requirement_status", "sceneRequirementUpdates[$index] has invalid status: ${update.requestedStatus}")
            }
            if (update.progressDelta !in -100..100) {
                errors += ValidationError("invalid_requirement_progress", "sceneRequirementUpdates[$index] progressDelta must be between -100 and 100.")
            }
            update.evidenceEventIds.filterNot { it in eventIds }.forEach {
                errors += ValidationError("unknown_event", "sceneRequirementUpdates[$index] references unknown event: $it")
            }
            update.evidenceFactIds.filterNot { it in factIds }.forEach {
                errors += ValidationError("unknown_fact", "sceneRequirementUpdates[$index] references unknown fact: $it")
            }
            if (update.evidenceStateDeltaIds.isNotEmpty() && !hasDeltaEvidence) {
                errors += ValidationError("unknown_state_delta", "sceneRequirementUpdates[$index] references state delta evidence but no state delta exists.")
            }
            if (
                update.requestedStatus in setOf("COMPLETED", "FAILED") &&
                update.evidenceEventIds.isEmpty() &&
                update.evidenceFactIds.isEmpty() &&
                update.evidenceStateDeltaIds.isEmpty()
            ) {
                errors += ValidationError("missing_evidence", "sceneRequirementUpdates[$index] must cite event, fact, or state-delta evidence.")
            }
            if (requirement.type == "OPTIONAL" && update.requestedStatus == "COMPLETED" && update.reason.isBlank()) {
                errors += ValidationError("missing_reason", "Optional requirement completion must include a reason.")
            }
        }
        return errors
    }
}

class StoryThreadManager(private val repository: SqliteStoryRepository) {
    fun validate(context: ContextPacket, result: AiTurnResult): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val activeThreads = context.activeStoryThreads.associateBy { it.threadId }
        val eventIds = context.recentEvents.map { it.id }.toSet() +
            result.allDeltaOperations().filter { it.type == DeltaOperationType.ADD_EVENT }.mapNotNull { it.targetId } +
            result.worldEventAcknowledgements.flatMap { it.resultingEventIds }
        val factIds = context.facts.map { it.id }.toSet() +
            result.revealedFacts.map { it.factId } +
            result.allDeltaOperations().filter { it.type == DeltaOperationType.ADD_FACT }.mapNotNull { it.targetId }
        val validStatuses = setOf("OPEN", "DORMANT", "BLOCKED", "RESOLVED", "FAILED", "ABANDONED")

        result.threadProgressUpdates.forEachIndexed { index, update ->
            if (update.threadId !in activeThreads) {
                errors += ValidationError("unknown_story_thread", "threadProgressUpdates[$index] references a thread not present in context: ${update.threadId}")
            }
            if (update.progressDelta !in -25..25) {
                errors += ValidationError("invalid_thread_progress", "threadProgressUpdates[$index] progressDelta must be between -25 and 25.")
            }
            if (update.requestedStatus !in validStatuses) {
                errors += ValidationError("invalid_thread_status", "threadProgressUpdates[$index] has invalid status: ${update.requestedStatus}")
            }
            update.evidenceEventIds.filterNot { it in eventIds }.forEach {
                errors += ValidationError("unknown_event", "threadProgressUpdates[$index] references unknown event: $it")
            }
            update.evidenceFactIds.filterNot { it in factIds }.forEach {
                errors += ValidationError("unknown_fact", "threadProgressUpdates[$index] references unknown fact: $it")
            }
            if (update.progressDelta != 0 && update.evidenceEventIds.isEmpty() && update.evidenceFactIds.isEmpty()) {
                errors += ValidationError("missing_evidence", "threadProgressUpdates[$index] must cite event or fact evidence.")
            }
        }

        val directorGoals = repository.tableRows("story_threads")
            .filter { it["project_id"] == context.project.id }
            .mapNotNull { it["director_goal"] }
            .filter { it.isNotBlank() }
        result.characterActions.forEachIndexed { index, action ->
            directorGoals.forEach { goal ->
                if (action.intent.equals(goal, ignoreCase = true) || action.intent.contains(goal, ignoreCase = true)) {
                    errors += ValidationError("director_goal_leak", "characterActions[$index] uses a story thread director goal as character intent.")
                }
                if (action.visibleAction.contains(goal, ignoreCase = true) || action.hiddenMotive.contains(goal, ignoreCase = true)) {
                    errors += ValidationError("director_goal_leak", "characterActions[$index] leaks a director-only thread goal into character-facing action.")
                }
            }
        }
        return errors
    }
}

class TransitionPlanner(private val repository: SqliteStoryRepository) {
    fun plan(sessionId: String, playerInput: String): List<AvailableTransition> =
        repository.listAvailableTransitions(sessionId, playerInput)
}

class TransitionValidator(private val repository: SqliteStoryRepository) {
    fun validate(context: ContextPacket, result: AiTurnResult): List<ValidationError> {
        val proposal = result.transitionProposal ?: return emptyList()
        val errors = mutableListOf<ValidationError>()
        val available = context.availableTransitions.firstOrNull { it.transitionId == proposal.transitionId }
        val validProposalTypes = setOf("PLAYER_INITIATED", "CHARACTER_INITIATED", "WORLD_FORCED")
        if (proposal.proposalType !in validProposalTypes) {
            errors += ValidationError("invalid_transition_type", "transitionProposal.proposalType is invalid: ${proposal.proposalType}")
        }
        if (proposal.fromSceneInstanceId != context.sceneLifecycle.sceneInstanceId) {
            errors += ValidationError("transition_scene_mismatch", "transitionProposal.fromSceneInstanceId does not match context scene instance.")
        }
        if (context.sceneLifecycle.status !in setOf("ACTIVE", "READY_TO_CLOSE")) {
            errors += ValidationError("transition_scene_closed", "Transition can only start from ACTIVE or READY_TO_CLOSE scene instance.")
        }
        if (available == null) {
            errors += ValidationError("unknown_transition", "transitionProposal.transitionId is not in availableTransitions: ${proposal.transitionId}")
        } else {
            if (available.toSceneId != proposal.toSceneId) {
                errors += ValidationError("transition_target_mismatch", "transitionProposal.toSceneId does not match transition definition.")
            }
            if (!available.prerequisitesSatisfied && proposal.proposalType != "WORLD_FORCED") {
                errors += ValidationError("transition_prerequisite_unmet", "Transition prerequisites are not satisfied: ${available.unmetPrerequisites.joinToString("; ")}")
            }
            if (proposal.proposalType == "WORLD_FORCED" && !available.forcedAllowed) {
                errors += ValidationError("forced_transition_not_allowed", "Transition does not allow WORLD_FORCED proposals.")
            }
        }
        if (!repository.targetExists(context.project.id, "scene", proposal.toSceneId)) {
            errors += ValidationError("unknown_scene", "transitionProposal.toSceneId does not exist: ${proposal.toSceneId}")
        }
        if (!repository.targetExists(context.project.id, "scene_transition", proposal.transitionId)) {
            errors += ValidationError("unknown_transition", "transitionProposal.transitionId does not exist: ${proposal.transitionId}")
        }
        val remainingRequired = context.sceneRequirements
            .filter { it.type == "REQUIRED" }
            .filterNot { requirement ->
                requirement.status == "COMPLETED" ||
                    result.sceneRequirementUpdates.any { it.requirementId == requirement.requirementId && it.requestedStatus == "COMPLETED" }
            }
        if (remainingRequired.isNotEmpty() && proposal.proposalType != "WORLD_FORCED") {
            errors += ValidationError("exit_gate_unmet", "Required scene requirements must complete before normal transition.")
        }
        if (proposal.proposalType == "WORLD_FORCED") {
            val evidence = proposal.worldEventEvidence
            if (evidence == null || context.pendingWorldEvents.none { it.eventId == evidence }) {
                errors += ValidationError("missing_world_event_evidence", "WORLD_FORCED transition requires a pending world event evidence id.")
            }
        }
        if (proposal.proposalType == "CHARACTER_INITIATED") {
            val actors = proposal.participatingCharacterIds.toSet()
            if (actors.isEmpty() || result.characterActions.none { it.characterId in actors }) {
                errors += ValidationError("missing_character_transition_actor", "CHARACTER_INITIATED transition requires a participating character action.")
            }
        }
        if (context.playerInput.refusesSceneTransition() && proposal.proposalType != "WORLD_FORCED") {
            errors += ValidationError("player_refused_transition", "Player input refuses transition; normal transition cannot be forced.")
        }
        return errors
    }

    private fun String.refusesSceneTransition(): Boolean {
        val normalized = lowercase()
        val movementRefusalPhrases = listOf(
            "不走",
            "留下",
            "拒絕離開",
            "拒絕走",
            "不離開",
            "不要走",
            "不要離開",
            "stay exactly",
            "stay where",
            "stay here",
            "stay put",
            "refuse to leave",
            "refuse to move",
            "refuse to go",
            "refuse transition",
            "refuse the transition",
            "do not leave",
            "don't leave",
            "do not move",
            "don't move",
            "do not go",
            "don't go",
            "will not leave",
            "won't leave",
            "will not go",
            "won't go",
        )
        return movementRefusalPhrases.any { normalized.contains(it) }
    }
}

class WorldClockService {
    fun validate(result: AiTurnResult): List<ValidationError> {
        val proposal = result.timeAdvanceProposal ?: return emptyList()
        val errors = mutableListOf<ValidationError>()
        if (proposal.minutes !in 0..120) {
            errors += ValidationError("invalid_time_advance", "timeAdvanceProposal.minutes must be between 0 and 120.")
        }
        if (proposal.sourceType.isBlank() || proposal.sourceId.isBlank() || proposal.reason.isBlank()) {
            errors += ValidationError("invalid_time_advance", "timeAdvanceProposal requires sourceType, sourceId, and reason.")
        }
        return errors
    }
}

class ScheduledWorldEventService {
    fun validate(context: ContextPacket, result: AiTurnResult): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val pendingById = context.pendingWorldEvents.associateBy { it.eventId }
        val seen = mutableSetOf<String>()
        result.worldEventAcknowledgements.forEachIndexed { index, acknowledgement ->
            val event = pendingById[acknowledgement.eventId]
            if (event == null) {
                errors += ValidationError("unknown_world_event", "worldEventAcknowledgements[$index] references event not pending in context: ${acknowledgement.eventId}")
                return@forEachIndexed
            }
            if (!seen.add(acknowledgement.eventId)) {
                errors += ValidationError("duplicate_world_event_ack", "worldEventAcknowledgements[$index] duplicates event: ${acknowledgement.eventId}")
            }
            if (acknowledgement.reason.isBlank() || acknowledgement.narrativeUse.isBlank()) {
                errors += ValidationError("invalid_world_event_ack", "worldEventAcknowledgements[$index] requires narrativeUse and reason.")
            }
            if (event.allowedNarrativeUses.isNotEmpty() && event.allowedNarrativeUses.none { acknowledgement.narrativeUse.contains(it, ignoreCase = true) || it.contains(acknowledgement.narrativeUse, ignoreCase = true) }) {
                errors += ValidationError("world_event_use_not_allowed", "worldEventAcknowledgements[$index] narrativeUse is not allowed for event ${event.eventId}.")
            }
            acknowledgement.perceivedByCharacterIds.filterNot { characterId -> context.characters.any { it.id == characterId } }.forEach {
                errors += ValidationError("unknown_actor", "worldEventAcknowledgements[$index] perceivedByCharacterIds contains unknown character: $it")
            }
            event.forbiddenCharacterUses.forEach { forbidden ->
                if (result.characterActions.any { it.visibleAction.contains(forbidden, ignoreCase = true) || it.intent.contains(forbidden, ignoreCase = true) }) {
                    errors += ValidationError("world_event_character_omniscience", "Character action uses forbidden world event information: $forbidden")
                }
            }
        }
        return errors
    }
}

class NarrativeContinuityValidator(private val repository: SqliteStoryRepository) {
    fun validate(context: ContextPacket, result: AiTurnResult): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val characterLocations = context.continuityConstraints.characterLocations
        val sceneLocation = context.continuityConstraints.currentSceneLocationId
        result.characterActions.forEachIndexed { index, action ->
            val actorLocation = characterLocations[action.characterId]
            if (sceneLocation != null && actorLocation != null && actorLocation != sceneLocation) {
                errors += ValidationError("character_location_mismatch", "characterActions[$index] actor is not in the current scene location.")
            }
            action.usedItemIds.forEach { itemId ->
                val item = context.items.firstOrNull { it.id == itemId }
                if (item == null) {
                    errors += ValidationError("unknown_item", "characterActions[$index] uses unknown item: $itemId")
                } else {
                    val actorOwns = item.ownerCharacterId == action.characterId
                    val playerOwnsAndVisible = item.ownerCharacterId == context.characters.firstOrNull { it.isPlayer }?.id
                    val inScene = item.locationId == null || item.locationId == sceneLocation
                    if (!actorOwns && !playerOwnsAndVisible && !inScene) {
                        errors += ValidationError("item_location_mismatch", "characterActions[$index] uses item outside actor reach: $itemId")
                    }
                }
            }
        }
        val noTransition = result.transitionProposal == null
        if (noTransition) {
            val sceneTitlesById = repository.tableRows("scenes")
                .filter { it["project_id"] == context.project.id }
                .associate { it["id"].orEmpty() to it["title"].orEmpty() }
            context.availableTransitions.forEach { transition ->
                val targetTitle = sceneTitlesById[transition.toSceneId].orEmpty()
                if (targetTitle.isNotBlank() && result.narrative.contains(targetTitle)) {
                    errors += ValidationError("implicit_transition", "Narrative appears to enter another scene without transitionProposal.")
                }
            }
        }
        context.pendingWorldEvents.forEach { event: PendingWorldEventView ->
            event.forbiddenCharacterUses.forEach { forbidden ->
                if (result.narrative.contains(forbidden, ignoreCase = true)) {
                    errors += ValidationError("world_event_character_omniscience", "Narrative uses forbidden world event character knowledge: $forbidden")
                }
            }
        }
        return errors
    }
}
