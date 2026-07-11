package com.localnovel.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class NpcAutonomyProfile(
    val npcId: String,
    val deepMotivation: String,
    val falseBelief: String,
    val fear: String,
    val relationshipDebts: List<String> = emptyList(),
    val offscreenAgenda: List<String>,
    val resourceIds: List<String> = emptyList(),
    val sandboxCandidateOnly: Boolean = true,
)

@Serializable
data class NpcPerception(
    val npcId: String,
    val locationId: String,
    val playerIsElsewhere: Boolean,
    val perceivedSituation: String,
    val knowledgeClaimIds: List<String> = emptyList(),
    val visibleEventIds: List<String> = emptyList(),
)

@Serializable
data class NpcMemoryAssessment(
    val memoryIds: List<String> = emptyList(),
    val importanceScore: Int,
    val reason: String,
)

@Serializable
data class NpcReflection(
    val conclusion: String,
    val tension: String,
)

@Serializable
data class NpcGoalPlan(
    val activeGoal: String,
    val priority: Int,
    val source: String,
)

@Serializable
data class NpcSchedule(
    val tickMinutes: Int,
    val plannedAction: String,
    val locationId: String,
)

@Serializable
data class NpcActionProposal(
    val proposalId: String,
    val npcId: String,
    val actionType: String,
    val intent: String,
    val visibleAction: String,
    val hiddenMotive: String,
    val locationId: String,
    val usedMemoryIds: List<String> = emptyList(),
    val usedKnowledgeClaimIds: List<String> = emptyList(),
    val predictedCost: String,
    val observableConsequence: String,
)

@Serializable
data class NpcDecisionTrace(
    val traceId: String,
    val projectId: String,
    val sessionId: String,
    val turnNumber: Int,
    val npcId: String,
    val scenarioSeed: String,
    val activeGoal: String,
    val relevantMemories: List<String>,
    val currentBeliefs: List<String>,
    val perceivedSituation: String,
    val consideredActions: List<String>,
    val selectedAction: String,
    val predictedCost: String,
    val rejectedAlternatives: List<String>,
    val offscreenReason: String,
    val proposal: NpcActionProposal,
)

@Serializable
data class GameMasterResolution(
    val accepted: Boolean,
    val status: String,
    val findings: List<String> = emptyList(),
    val elapsedMinutes: Int = 0,
    val observableConsequence: String = "",
)

@Serializable
data class OffscreenSimulationResult(
    val traceId: String,
    val accepted: Boolean,
    val status: String,
    val resultId: String = "",
    val observableConsequence: String = "",
)

@Serializable
data class SandboxWorldPatchCandidate(
    val patchId: String,
    val projectId: String,
    val scenarioSeed: String,
    val status: String = "SANDBOX_CANDIDATE",
    val npcProfiles: List<NpcAutonomyProfile>,
    val worldPressure: String,
    val valueConflict: String,
    val canonMutationAllowed: Boolean = false,
)
