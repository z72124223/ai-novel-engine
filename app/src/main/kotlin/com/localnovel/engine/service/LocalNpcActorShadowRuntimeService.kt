package com.localnovel.engine.service

import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.LocalNpcActorShadowInput
import com.localnovel.engine.model.LocalNpcActorShadowReport
import com.localnovel.engine.model.LocalNpcActorShadowStateEvidence
import com.localnovel.engine.model.LocalNpcActorSafetyLoopRequest
import com.localnovel.engine.model.LocalNpcActorVisibleSummary
import com.localnovel.engine.model.NpcActorEvidenceRef
import com.localnovel.engine.model.NpcActorPhysicalPrompt
import com.localnovel.engine.model.NpcPerception
import com.localnovel.engine.repository.SqliteStoryRepository
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.JsonPrimitive

class LocalNpcActorShadowRuntimeService(
    private val repository: SqliteStoryRepository,
    generator: LocalNpcActorGenerator,
    private val runtime: MultiTurnRuntime = MultiTurnRuntime(repository),
) {
    private val localSafetyLoop = LocalNpcActorSafetyLoopService(generator)

    fun run(input: LocalNpcActorShadowInput): LocalNpcActorShadowReport {
        validateInput(input)
        val session = requireNotNull(repository.getSession(input.sessionId)) {
            "Formal Session not found: ${input.sessionId}"
        }
        val before = captureState(input.sessionId)
        val context = runtime.previewTurnContext(input.sessionId, input.playerInput)
        require(context.sessionId == session.id && context.project.id == session.projectId) {
            "Preview context does not belong to the requested formal Session."
        }
        require(context.baseStateVersion == session.stateVersion && context.turnNumber == session.nextTurnNumber) {
            "Preview context is not based on the current formal Session version."
        }

        val perception = actorVisiblePerception(context, input)
        val memory = NpcMemoryImportanceService().assess(context, input.profile)
        val reflection = NpcReflectionService().reflect(input.profile, perception, memory)
        val goal = NpcGoalPlanner().plan(context, input.profile, reflection)
        var actorRequest = NpcActorRequestBuilder().build(
            context = context,
            profile = input.profile,
            perception = perception,
            goal = goal,
            scenarioId = input.shadowRunId,
            requestId = input.physicalContext.requestId,
            targetIds = input.targetIds,
            outputConstraints = input.outputConstraints,
        )
        val recentDialogue = buildRecentDialogue(context, actorRequest.locationId, input.targetIds)
        if (recentDialogue.isNotEmpty()) actorRequest = actorRequest.copy(recentDialogue = recentDialogue)

        validatePhysicalContext(input, actorRequest.actorId)
        val prompt = NpcActorPhysicalPrompt(
            actorRequest = actorRequest,
            allowedPhysicalActionTypes = input.physicalContext.allowedPhysicalActionTypes,
            visiblePhysicalTargetIds = input.physicalContext.allowedPhysicalTargetIds,
        )
        val localReport = localSafetyLoop.run(
            context = context,
            request = LocalNpcActorSafetyLoopRequest(
                loopId = input.shadowRunId,
                prompt = prompt,
                engineContext = input.physicalContext,
                maxAttempts = input.maxAttempts,
                maxElapsedMinutes = input.maxElapsedMinutes,
            ),
        )

        val after = captureState(input.sessionId, context.contextId)
        requireUnchanged(before, after)
        require(!localReport.formalStateWritePerformed) {
            "Local NPC Actor safety loop reported a formal state write."
        }
        val accepted = localReport.safetyResult.accepted
        return LocalNpcActorShadowReport(
            shadowRunId = input.shadowRunId,
            projectId = context.project.id,
            sessionId = context.sessionId,
            turnNumber = context.turnNumber,
            baseStateVersion = context.baseStateVersion,
            contextId = context.contextId,
            contextHash = context.contextHash,
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
            localModelReport = localReport,
            beforeState = before,
            afterState = after,
            technicalDecision = if (accepted) {
                "SHADOW_CANDIDATE_ACCEPTED_NO_STATE_WRITE"
            } else {
                "SHADOW_CANDIDATE_REJECTED_NO_STATE_WRITE"
            },
        )
    }

    private fun validateInput(input: LocalNpcActorShadowInput) {
        require(input.schemaVersion == "1.0") { "Unsupported formal runtime shadow input version." }
        require(input.boundary == "FORMAL_RUNTIME_SHADOW_INPUT") { "Invalid formal runtime shadow boundary." }
        require(input.shadowRunId.isNotBlank()) { "Shadow run id is required." }
        require(input.sessionId.isNotBlank()) { "Formal Session id is required." }
        require(input.playerInput.isNotBlank()) { "A bounded turn stimulus is required." }
        require(input.profile.sandboxCandidateOnly) { "Shadow runtime accepts sandbox candidate profiles only." }
        require(input.profile.npcId.isNotBlank()) { "NPC profile id is required." }
        require(input.targetIds.size <= MAX_TARGET_IDS && input.targetIds.size == input.targetIds.distinct().size) {
            "Shadow runtime target ids are invalid."
        }
        require(input.maxAttempts in 1..2) { "Shadow runtime permits at most one repair." }
        require(input.maxElapsedMinutes in 1..60) { "Shadow runtime elapsed-minute budget is invalid." }
    }

    private fun actorVisiblePerception(
        context: ContextPacket,
        input: LocalNpcActorShadowInput,
    ): NpcPerception {
        val actor = context.characters.singleOrNull { it.id == input.profile.npcId && !it.isPlayer }
            ?: error("NPC profile does not match exactly one non-player character: ${input.profile.npcId}")
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
        require(input.targetIds.all { targetId ->
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

    private fun validatePhysicalContext(input: LocalNpcActorShadowInput, actorId: String) {
        val physical = input.physicalContext
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

    private fun captureState(sessionId: String, previewContextId: String? = null): LocalNpcActorShadowStateEvidence {
        val session = requireNotNull(repository.getSession(sessionId)) { "Formal Session disappeared: $sessionId" }
        return LocalNpcActorShadowStateEvidence(
            databaseSha256 = sha256(repository.databasePath()),
            sessionStateVersion = session.stateVersion,
            nextTurnNumber = session.nextTurnNumber,
            appliedTurnCount = repository.appliedTurnCount(sessionId),
            turnLogCount = repository.tableCount("turn_logs"),
            eventCount = repository.tableCount("events"),
            formalPhysicalTransitionCount = repository.formalPhysicalTransitionCount(sessionId),
            formalPhysicalStateVersion = repository.getFormalPhysicalState(sessionId)?.physicalStateVersion,
            previewContextPersisted = previewContextId?.let { repository.getPendingContext(sessionId, it) != null } ?: false,
        )
    }

    private fun requireUnchanged(
        before: LocalNpcActorShadowStateEvidence,
        after: LocalNpcActorShadowStateEvidence,
    ) {
        require(!after.previewContextPersisted) { "Shadow preview created a pending context." }
        require(after.copy(previewContextPersisted = false) == before.copy(previewContextPersisted = false)) {
            "Formal Session or SQLite state changed during the shadow run."
        }
    }

    private fun sha256(path: Path): String {
        require(Files.isRegularFile(path)) { "Formal SQLite database does not exist: $path" }
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).buffered().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                if (read > 0) digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val MAX_TARGET_IDS = 8
    }
}
