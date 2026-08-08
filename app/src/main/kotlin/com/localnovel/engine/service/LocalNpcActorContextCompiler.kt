package com.localnovel.engine.service

import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.LocalNpcActorVisibleSummary
import com.localnovel.engine.model.NpcActorEvidenceRef
import com.localnovel.engine.model.NpcActorOutputConstraints
import com.localnovel.engine.model.NpcActorPhysicalEngineContext
import com.localnovel.engine.model.NpcActorPhysicalPrompt
import com.localnovel.engine.model.NpcActorRequest
import com.localnovel.engine.model.NpcAutonomyProfile
import com.localnovel.engine.model.NpcPerception
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

data class LocalNpcActorPreparedContext(
    val actorRequest: NpcActorRequest,
    val visibleSummary: LocalNpcActorVisibleSummary,
    val perception: NpcPerception,
    val prompt: NpcActorPhysicalPrompt,
)

class LocalNpcActorContextCompiler {
    fun compile(
        context: ContextPacket,
        runId: String,
        profile: NpcAutonomyProfile,
        targetIds: List<String>,
        physicalContext: NpcActorPhysicalEngineContext,
        outputConstraints: NpcActorOutputConstraints,
    ): LocalNpcActorPreparedContext {
        require(runId.isNotBlank()) { "NPC Actor run id is required." }
        require(profile.sandboxCandidateOnly) { "Formal NPC Actor input requires a sandbox candidate profile." }
        require(targetIds.size <= MAX_TARGET_IDS && targetIds.size == targetIds.distinct().size) {
            "Formal NPC Actor target ids are invalid."
        }

        val perception = actorVisiblePerception(context, profile, targetIds)
        val memory = NpcMemoryImportanceService().assess(context, profile)
        val reflection = NpcReflectionService().reflect(profile, perception, memory)
        val goal = NpcGoalPlanner().plan(context, profile, reflection)
        var actorRequest = NpcActorRequestBuilder().build(
            context = context,
            profile = profile,
            perception = perception,
            goal = goal,
            scenarioId = runId,
            requestId = physicalContext.requestId,
            targetIds = targetIds,
            outputConstraints = outputConstraints,
        )
        val dialogue = buildRecentDialogue(context, actorRequest.locationId, targetIds)
        if (dialogue.isNotEmpty()) actorRequest = actorRequest.copy(recentDialogue = dialogue)
        validatePhysicalContext(physicalContext, actorRequest.actorId)

        val prompt = NpcActorPhysicalPrompt(
            actorRequest = actorRequest,
            allowedPhysicalActionTypes = physicalContext.allowedPhysicalActionTypes,
            visiblePhysicalTargetIds = physicalContext.allowedPhysicalTargetIds,
        )
        return LocalNpcActorPreparedContext(
            actorRequest = actorRequest,
            visibleSummary = LocalNpcActorVisibleSummary(
                actorId = actorRequest.actorId,
                targetIds = actorRequest.targetIds,
                locationId = actorRequest.locationId,
                playerIsElsewhere = perception.playerIsElsewhere,
                perceivedEventIds = actorRequest.perceivedFacts.map { it.evidenceId },
                ownedMemoryIds = actorRequest.ownedMemories.map { it.evidenceId },
                ownedBeliefIds = actorRequest.ownedBeliefs.map { it.evidenceId },
                capabilityIds = actorRequest.capabilities.map { it.evidenceId },
                recentDialogueEvidenceIds = actorRequest.recentDialogue.map { it.evidenceId },
            ),
            perception = perception,
            prompt = prompt,
        )
    }

    private fun actorVisiblePerception(
        context: ContextPacket,
        profile: NpcAutonomyProfile,
        targetIds: List<String>,
    ): NpcPerception {
        val actor = context.characters.singleOrNull { it.id == profile.npcId && !it.isPlayer }
            ?: error("NPC profile does not match exactly one non-player character: ${profile.npcId}")
        val actorState = context.characterStates.singleOrNull { it.characterId == actor.id }
            ?: error("NPC state is unavailable: ${actor.id}")
        val actorLocation = context.continuityConstraints.characterLocations[actor.id]
            ?: actorState.locationId
            ?: error("NPC location is unavailable: ${actor.id}")
        val characterLocations = context.characters.associate { character ->
            character.id to (
                context.continuityConstraints.characterLocations[character.id]
                    ?: context.characterStates.singleOrNull { it.characterId == character.id }?.locationId
                )
        }
        require(targetIds.all { targetId ->
            targetId in characterLocations && characterLocations[targetId] == actorLocation
        }) { "Direct NPC Actor targets must exist at the actor's current location." }
        val playerLocations = context.characters.filter { it.isPlayer }
            .mapNotNull { characterLocations[it.id] }
            .toSet()
        val visibleEventIds = context.recentEvents.filter { event ->
            val eventNpcId = (event.payload["npcId"] as? JsonPrimitive)?.contentOrNull
            val eventLocationId = (event.payload["locationId"] as? JsonPrimitive)?.contentOrNull
            eventNpcId == actor.id || eventLocationId == actorLocation
        }.map { it.id }
        val claimIds = context.knowledgeClaims
            .filter { it.characterId == actor.id }
            .map { it.claimId }
        return NpcPerception(
            npcId = actor.id,
            locationId = actorLocation,
            playerIsElsewhere = actorLocation !in playerLocations,
            perceivedSituation = buildString {
                append(actor.name).append(" is at ").append(actorLocation)
                if (claimIds.isEmpty()) append(" with no supported private claim in this context")
                else append(" with ").append(claimIds.size).append(" actor-owned supported claim(s)")
            },
            knowledgeClaimIds = claimIds,
            visibleEventIds = visibleEventIds,
        )
    }

    private fun buildRecentDialogue(
        context: ContextPacket,
        actorLocation: String,
        targetIds: List<String>,
    ): List<NpcActorEvidenceRef> {
        if (targetIds.isEmpty()) return emptyList()
        val allTargetsPresent = targetIds.all { targetId ->
            val targetLocation = context.continuityConstraints.characterLocations[targetId]
                ?: context.characterStates.singleOrNull { it.characterId == targetId }?.locationId
            targetLocation == actorLocation
        }
        if (!allTargetsPresent) return emptyList()
        return listOf(
            NpcActorEvidenceRef(
                evidenceId = "dialogue:${context.turnId}:stimulus",
                kind = "RECENT_DIALOGUE",
                summary = context.playerInput,
                visibility = "ACTOR_VISIBLE",
            ),
        )
    }

    private fun validatePhysicalContext(physical: NpcActorPhysicalEngineContext, actorId: String) {
        require(physical.schemaVersion == "1.0" && physical.boundary == "NON_CANON_EVALUATION") {
            "Physical context must remain a non-Canon evaluation input."
        }
        require(physical.requestId.isNotBlank()) { "Physical request id is required." }
        val entityIds = physical.physicalSnapshot.entities.map { it.entityId }
        require(actorId in entityIds) { "NPC actor is missing from the physical snapshot." }
        require(physical.allowedPhysicalActionTypes.isNotEmpty()) { "Physical action allowlist is required." }
        require(physical.allowedPhysicalTargetIds.isNotEmpty()) { "Physical target allowlist is required." }
        require(physical.allowedPhysicalTargetIds.all { it in entityIds }) {
            "A physical target is missing from the physical snapshot."
        }
    }

    companion object {
        private const val MAX_TARGET_IDS = 8
    }
}
