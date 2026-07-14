package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.GameMasterResolution
import com.localnovel.engine.model.NpcActionProposal
import com.localnovel.engine.model.NpcAutonomyProfile
import com.localnovel.engine.model.NpcDecisionTrace
import com.localnovel.engine.model.NpcGoalPlan
import com.localnovel.engine.model.NpcMemoryAssessment
import com.localnovel.engine.model.NpcPerception
import com.localnovel.engine.model.NpcReflection
import com.localnovel.engine.model.NpcSchedule
import com.localnovel.engine.model.OffscreenSimulationResult
import com.localnovel.engine.model.SandboxWorldPatchCandidate
import com.localnovel.engine.repository.SqliteStoryRepository
import java.util.UUID
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class NpcPerceptionService {
    fun perceive(context: ContextPacket, profile: NpcAutonomyProfile): NpcPerception {
        val npc = context.characters.singleOrNull { it.id == profile.npcId && !it.isPlayer }
            ?: error("NPC profile does not match a non-player character: ${profile.npcId}")
        val npcState = context.characterStates.singleOrNull { it.characterId == npc.id }
            ?: error("NPC state is unavailable: ${npc.id}")
        val npcLocation = context.continuityConstraints.characterLocations[npc.id] ?: npcState.locationId
        val playerLocations = context.characters.filter { it.isPlayer }.mapNotNull { player ->
            context.continuityConstraints.characterLocations[player.id]
                ?: context.characterStates.singleOrNull { it.characterId == player.id }?.locationId
        }.toSet()
        val ownClaims = context.knowledgeClaims.filter { it.characterId == npc.id }
        return NpcPerception(
            npcId = npc.id,
            locationId = npcLocation.orEmpty(),
            playerIsElsewhere = npcLocation != null && npcLocation !in playerLocations,
            perceivedSituation = buildString {
                append(npc.name).append(" remains at ").append(npcLocation ?: "unknown")
                append(" while pursuing private interests")
                if (ownClaims.isNotEmpty()) append(" with ${ownClaims.size} supported beliefs")
            },
            knowledgeClaimIds = ownClaims.map { it.claimId },
            visibleEventIds = context.recentEvents.map { it.id },
        )
    }
}

class NpcMemoryImportanceService {
    fun assess(context: ContextPacket, profile: NpcAutonomyProfile): NpcMemoryAssessment {
        val memories = context.characterMemories
            .filter { it.ownerCharacterId == profile.npcId || it.characterId == profile.npcId }
            .sortedByDescending { memory ->
                memory.tags.count { tag -> profile.offscreenAgenda.any { it.contains(tag, ignoreCase = true) } }
            }
            .take(3)
        return NpcMemoryAssessment(
            memoryIds = memories.map { it.id },
            importanceScore = (40 + memories.size * 15).coerceAtMost(100),
            reason = if (memories.isEmpty()) "No owned memory is available; act conservatively from current state." else "Owned memories overlap the NPC agenda.",
        )
    }
}

class NpcReflectionService {
    fun reflect(profile: NpcAutonomyProfile, perception: NpcPerception, memory: NpcMemoryAssessment): NpcReflection =
        NpcReflection(
            conclusion = "${profile.deepMotivation}; therefore ${profile.offscreenAgenda.first()}.",
            tension = "Fear: ${profile.fear}; false belief: ${profile.falseBelief}; memory importance: ${memory.importanceScore}.",
        )
}

class NpcGoalPlanner {
    fun plan(context: ContextPacket, profile: NpcAutonomyProfile, reflection: NpcReflection): NpcGoalPlan {
        val stateGoal = context.characterStates.single { it.characterId == profile.npcId }.flags["current_goal"]
        return NpcGoalPlan(
            activeGoal = stateGoal?.takeIf { it.isNotBlank() } ?: profile.offscreenAgenda.first(),
            priority = if (reflection.tension.isNotBlank()) 80 else 60,
            source = if (stateGoal.isNullOrBlank()) "sandbox_profile" else "runtime_character_state",
        )
    }
}

class NpcScheduleService {
    fun schedule(goal: NpcGoalPlan, perception: NpcPerception): NpcSchedule =
        NpcSchedule(
            tickMinutes = if (goal.priority >= 80) 10 else 20,
            plannedAction = "inspect_local_pressure",
            locationId = perception.locationId,
        )
}

class NpcActionProposalService {
    fun propose(
        profile: NpcAutonomyProfile,
        perception: NpcPerception,
        memory: NpcMemoryAssessment,
        reflection: NpcReflection,
        goal: NpcGoalPlan,
        schedule: NpcSchedule,
    ): NpcActionProposal = NpcActionProposal(
        proposalId = "npc-proposal-${UUID.randomUUID()}",
        npcId = profile.npcId,
        actionType = schedule.plannedAction,
        intent = goal.activeGoal,
        visibleAction = "Inspects the immediate area and secures evidence before anyone returns.",
        hiddenMotive = reflection.conclusion,
        locationId = schedule.locationId,
        usedMemoryIds = memory.memoryIds,
        usedKnowledgeClaimIds = perception.knowledgeClaimIds,
        predictedCost = "${schedule.tickMinutes} minutes and increased exposure",
        observableConsequence = "Signs of a careful off-screen search remain at ${schedule.locationId}.",
    )
}

class GameMasterResolutionService {
    private val allowedActions = setOf("inspect_local_pressure", "protect_local_resource", "seek_local_evidence")

    fun resolve(context: ContextPacket, proposal: NpcActionProposal): GameMasterResolution {
        val findings = mutableListOf<String>()
        val npc = context.characters.singleOrNull { it.id == proposal.npcId && !it.isPlayer }
        val state = context.characterStates.singleOrNull { it.characterId == proposal.npcId }
        val stateLocation = context.continuityConstraints.characterLocations[proposal.npcId] ?: state?.locationId
        val playerLocations = context.characters.filter { it.isPlayer }.mapNotNull { player ->
            context.continuityConstraints.characterLocations[player.id]
                ?: context.characterStates.singleOrNull { it.characterId == player.id }?.locationId
        }.toSet()
        val ownClaimIds = context.knowledgeClaims.filter { it.characterId == proposal.npcId }.map { it.claimId }.toSet()
        val ownMemoryIds = (
            context.characterMemories
                .filter { it.ownerCharacterId == proposal.npcId || it.characterId == proposal.npcId }
                .map { it.id } +
                context.retrievedMemories
                    .filter { it.ownerCharacterId == proposal.npcId }
                    .map { it.memoryId }
            ).toSet()
        if (npc == null) findings += "unknown_or_player_actor"
        if (stateLocation != proposal.locationId) findings += "location_mismatch"
        if (proposal.locationId in playerLocations) findings += "player_not_offscreen"
        if (proposal.actionType !in allowedActions) findings += "action_not_allowlisted"
        if (proposal.usedKnowledgeClaimIds.any { it !in ownClaimIds }) findings += "foreign_knowledge_reference"
        if (proposal.usedMemoryIds.any { it !in ownMemoryIds }) findings += "foreign_memory_reference"
        if (proposal.intent.isBlank() || proposal.observableConsequence.isBlank()) findings += "incomplete_action_proposal"
        return GameMasterResolution(
            accepted = findings.isEmpty(),
            status = if (findings.isEmpty()) "ACCEPTED_FOR_FORMAL_RESOLVE" else "REJECTED_WORLD_LEGALITY",
            findings = findings,
            elapsedMinutes = if (findings.isEmpty()) 10 else 0,
            observableConsequence = if (findings.isEmpty()) proposal.observableConsequence else "",
        )
    }
}

class NpcTurnScheduler {
    fun buildTrace(
        context: ContextPacket,
        profile: NpcAutonomyProfile,
        scenarioSeed: String,
        perception: NpcPerception,
        memory: NpcMemoryAssessment,
        goal: NpcGoalPlan,
        proposal: NpcActionProposal,
    ): NpcDecisionTrace = NpcDecisionTrace(
        traceId = "npc-trace-${UUID.randomUUID()}",
        projectId = context.project.id,
        sessionId = context.sessionId,
        turnNumber = context.turnNumber,
        npcId = profile.npcId,
        scenarioSeed = scenarioSeed,
        activeGoal = goal.activeGoal,
        relevantMemories = memory.memoryIds,
        currentBeliefs = context.knowledgeClaims.filter { it.characterId == profile.npcId }.map { it.claimId },
        perceivedSituation = perception.perceivedSituation,
        consideredActions = listOf("inspect_local_pressure", "protect_local_resource", "wait_and_observe"),
        selectedAction = proposal.actionType,
        predictedCost = proposal.predictedCost,
        rejectedAlternatives = listOf("wait_and_observe: agenda remains unresolved", "seek_player: player is offscreen"),
        offscreenReason = "NPC and player occupy different locations during this tick.",
        proposal = proposal,
    )
}

class OffscreenSimulationService(private val repository: SqliteStoryRepository) {
    fun run(
        sessionId: String,
        profile: NpcAutonomyProfile,
        scenarioSeed: String,
        proposalOverride: NpcActionProposal? = null,
        turnInput: String = "[OFFSCREEN_TICK:$scenarioSeed] Advance the world without player intervention.",
    ): OffscreenSimulationResult {
        require(profile.sandboxCandidateOnly) { "Stage 2 accepts sandbox candidate profiles only." }
        val session = repository.getSession(sessionId) ?: error("Session not found: $sessionId")
        repository.recordSandboxWorldPatchCandidate(
            SandboxWorldPatchCandidate(
                patchId = "sandbox-patch-$scenarioSeed-${profile.npcId}",
                projectId = session.projectId,
                scenarioSeed = scenarioSeed,
                npcProfiles = listOf(profile),
                worldPressure = "The NPC agenda continues while the player is elsewhere.",
                valueConflict = "Protect private interests or preserve evidence for later accountability.",
            ),
        )
        val runtime = MultiTurnRuntime(repository)
        val context = runtime.prepareTurn(sessionId, turnInput)
        val perception = NpcPerceptionService().perceive(context, profile)
        val memory = NpcMemoryImportanceService().assess(context, profile)
        val reflection = NpcReflectionService().reflect(profile, perception, memory)
        val goal = NpcGoalPlanner().plan(context, profile, reflection)
        val schedule = NpcScheduleService().schedule(goal, perception)
        val proposal = proposalOverride ?: NpcActionProposalService().propose(profile, perception, memory, reflection, goal, schedule)
        val trace = NpcTurnScheduler().buildTrace(context, profile, scenarioSeed, perception, memory, goal, proposal)
        val resolution = GameMasterResolutionService().resolve(context, proposal)
        if (!resolution.accepted) {
            repository.recordNpcDecisionTrace(trace, resolution, "REJECTED")
            repository.abortPendingContext(sessionId, context.contextId)
            return OffscreenSimulationResult(trace.traceId, false, resolution.status)
        }
        val resultId = "offscreen-result-${UUID.randomUUID()}"
        val result = AiTurnResult(
            protocolVersion = context.protocolVersion,
            projectId = context.project.id,
            turnId = context.turnId,
            resultId = resultId,
            contextId = context.contextId,
            sessionId = context.sessionId,
            turnNumber = context.turnNumber,
            baseStateVersion = context.baseStateVersion,
            contextHash = context.contextHash,
            narrative = "Elsewhere, ${proposal.visibleAction} ${proposal.observableConsequence}",
            turnSummary = "${profile.npcId} completed a bounded off-screen action.",
            characterActions = listOf(
                CharacterAction(
                    characterId = profile.npcId,
                    intent = proposal.intent,
                    visibleAction = proposal.visibleAction,
                    hiddenMotive = proposal.hiddenMotive,
                    usedMemoryIds = proposal.usedMemoryIds,
                    usedKnowledgeClaimIds = proposal.usedKnowledgeClaimIds,
                    characterFitScore = 90,
                    storyProgressScore = 70,
                ),
            ),
            stateDeltas = listOf(
                DeltaOperation(
                    type = DeltaOperationType.ADD_EVENT,
                    targetType = "event",
                    targetId = "event-${trace.traceId}",
                    payload = JsonObject(
                        mapOf(
                            "summary" to JsonPrimitive(proposal.observableConsequence),
                            "npcId" to JsonPrimitive(profile.npcId),
                            "locationId" to JsonPrimitive(proposal.locationId),
                            "offscreen" to JsonPrimitive(true),
                        ),
                    ),
                ),
            ),
            timeAdvanceProposal = com.localnovel.engine.model.TimeAdvanceProposal(
                minutes = resolution.elapsedMinutes,
                sourceType = "offscreen_npc",
                sourceId = trace.traceId,
                reason = "Bounded NPC autonomy tick.",
            ),
        )
        runtime.resolveTurn(
            sessionId,
            result,
            ResolveTurnOptions(
                scenarioSeed = scenarioSeed,
                npcDecisionTrace = trace,
                npcGameMasterResolution = resolution,
            ),
        )
        return OffscreenSimulationResult(
            traceId = trace.traceId,
            accepted = true,
            status = "APPLIED",
            resultId = resultId,
            observableConsequence = proposal.observableConsequence,
        )
    }
}
