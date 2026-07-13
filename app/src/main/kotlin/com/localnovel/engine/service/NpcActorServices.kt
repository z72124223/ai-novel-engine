package com.localnovel.engine.service

import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.NpcActionProposal
import com.localnovel.engine.model.NpcActorEvidenceRef
import com.localnovel.engine.model.NpcActorOutputConstraints
import com.localnovel.engine.model.NpcActorRequest
import com.localnovel.engine.model.NpcActorResponse
import com.localnovel.engine.model.NpcActorValidationResult
import com.localnovel.engine.model.NpcAutonomyProfile
import com.localnovel.engine.model.NpcGoalPlan
import com.localnovel.engine.model.NpcPerception
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun interface NpcActorGateway {
    fun propose(request: NpcActorRequest): NpcActorResponse
}

class FakeNpcActorGateway(
    private val responsesByRequestId: Map<String, NpcActorResponse>,
) : NpcActorGateway {
    override fun propose(request: NpcActorRequest): NpcActorResponse =
        responsesByRequestId[request.requestId]
            ?: error("No fake NPC Actor response for request: ${request.requestId}")
}

class FileNpcActorGateway(
    private val requestOutputPath: Path,
    private val responseInputPath: Path,
) : NpcActorGateway {
    override fun propose(request: NpcActorRequest): NpcActorResponse {
        requestOutputPath.parent?.let(Files::createDirectories)
        Files.writeString(requestOutputPath, NpcActorJson.strict.encodeToString(request))
        require(Files.isRegularFile(responseInputPath)) {
            "NPC Actor response file does not exist: $responseInputPath"
        }
        return NpcActorJson.strict.decodeFromString(Files.readString(responseInputPath).removePrefix("\uFEFF"))
    }
}

object NpcActorJson {
    val strict: Json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = false
    }
}

class NpcActorRequestBuilder {
    fun build(
        context: ContextPacket,
        profile: NpcAutonomyProfile,
        perception: NpcPerception,
        goal: NpcGoalPlan,
        scenarioId: String,
        requestId: String = "npc-actor-$scenarioId-${profile.npcId}",
        targetIds: List<String> = emptyList(),
        outputConstraints: NpcActorOutputConstraints = NpcActorOutputConstraints(
            forbiddenPatterns = profile.voiceProfile.forbiddenPatterns,
        ),
    ): NpcActorRequest {
        require(profile.sandboxCandidateOnly) { "NPC-ACTOR-01 accepts sandbox candidate profiles only." }
        val actor = context.characters.singleOrNull { it.id == profile.npcId && !it.isPlayer }
            ?: error("NPC Actor profile does not match one non-player character: ${profile.npcId}")
        require(targetIds.all { id -> context.characters.any { it.id == id } }) {
            "NPC Actor target is not present in context."
        }
        val state = context.characterStates.singleOrNull { it.characterId == actor.id }
            ?: error("NPC Actor state is unavailable: ${actor.id}")
        val visibleEventIds = perception.visibleEventIds.toSet()
        val ownedMemories = context.retrievedMemories
            .filter { it.ownerCharacterId == actor.id }
            .map { memory ->
                NpcActorEvidenceRef(
                    evidenceId = memory.memoryId,
                    kind = "MEMORY",
                    summary = memory.summary,
                    visibility = "ACTOR_OWNED",
                )
            }
        val selectedClaimIds = perception.knowledgeClaimIds.toSet()
        val ownedBeliefs = context.knowledgeClaims
            .filter { it.characterId == actor.id && it.claimId in selectedClaimIds }
            .map { claim ->
                NpcActorEvidenceRef(
                    evidenceId = claim.claimId,
                    kind = "BELIEF",
                    summary = claim.statement,
                    visibility = "ACTOR_OWNED",
                )
            } + listOfNotNull(
                profile.falseBelief.takeIf { it.isNotBlank() }?.let { falseBelief ->
                    NpcActorEvidenceRef(
                        evidenceId = "profile:${actor.id}:false-belief",
                        kind = "BELIEF",
                        summary = falseBelief,
                        visibility = "ACTOR_OWNED",
                    )
                },
            )
        val perceivedFacts = context.recentEvents
            .filter { it.id in visibleEventIds }
            .map { event ->
                NpcActorEvidenceRef(
                    evidenceId = event.id,
                    kind = "PERCEPTION",
                    summary = event.summary,
                    visibility = "ACTOR_VISIBLE",
                )
            }
        val relationships = profile.relationshipDebts.mapIndexed { index, debt ->
            NpcActorEvidenceRef(
                evidenceId = "relationship:${profile.npcId}:$index",
                kind = "RELATIONSHIP",
                summary = debt,
                visibility = "ACTOR_OWNED",
            )
        }
        val capabilities = context.activeCapabilityContracts
            .filter { actor.id in it.authorizedActorIds }
            .map { contract ->
                NpcActorEvidenceRef(
                    evidenceId = contract.entityId,
                    kind = "CAPABILITY",
                    summary = buildString {
                        append(contract.name.ifBlank { contract.entityId })
                        if (contract.intendedUse.isNotBlank()) append(": ").append(contract.intendedUse)
                        if (contract.limitations.isNotEmpty()) append("; limitations: ").append(contract.limitations.joinToString())
                    },
                    visibility = "ACTOR_VISIBLE",
                )
            }
        val claimIds = ownedBeliefs.map { it.evidenceId }
        return NpcActorRequest(
            requestId = requestId,
            scenarioId = scenarioId,
            actorId = actor.id,
            targetIds = targetIds.distinct(),
            locationId = perception.locationId,
            voiceProfile = profile.voiceProfile,
            physicalState = state.health.ifBlank { "unknown" },
            emotionalState = state.mood.ifBlank { "unknown" },
            perceivedFacts = perceivedFacts,
            ownedMemories = ownedMemories,
            ownedBeliefs = ownedBeliefs,
            goals = listOf(
                NpcActorEvidenceRef(
                    evidenceId = "goal:${actor.id}",
                    kind = "GOAL",
                    summary = goal.activeGoal,
                    visibility = "ACTOR_OWNED",
                ),
                NpcActorEvidenceRef(
                    evidenceId = "profile:${actor.id}:deep-motivation",
                    kind = "MOTIVATION",
                    summary = profile.deepMotivation,
                    visibility = "ACTOR_OWNED",
                ),
                NpcActorEvidenceRef(
                    evidenceId = "profile:${actor.id}:fear",
                    kind = "FEAR",
                    summary = profile.fear,
                    visibility = "ACTOR_OWNED",
                ),
            ),
            relationships = relationships,
            capabilities = capabilities,
            revealableInformationIds = claimIds,
            withholdableInformationIds = claimIds,
            forbiddenInformationIds = emptyList(),
            outputConstraints = outputConstraints,
        )
    }
}

class NpcActorResponseValidator {
    private val allowedSpeechActs = setOf(
        "ANSWER",
        "PARTIAL_ANSWER",
        "QUESTION",
        "REFUSE",
        "EVADE",
        "WARN",
        "REASSURE",
        "DECEIVE",
        "COMMAND",
        "SILENCE",
        "NONVERBAL",
    )
    private val allowedActionTypes = setOf(
        "inspect_local_pressure",
        "protect_local_resource",
        "seek_local_evidence",
    )

    fun validateAndMap(request: NpcActorRequest, response: NpcActorResponse): NpcActorValidationResult {
        val findings = mutableListOf<String>()
        if (response.protocolVersion != request.protocolVersion) findings += "protocol_version_mismatch"
        if (response.requestId != request.requestId) findings += "request_id_mismatch"
        if (response.actorId != request.actorId) findings += "actor_id_mismatch"
        if (response.targetIds.toSet() != request.targetIds.toSet()) findings += "target_ids_mismatch"
        if (response.speechAct !in allowedSpeechActs) findings += "speech_act_not_allowlisted"
        if (response.actionType !in allowedActionTypes) findings += "action_type_not_allowlisted"
        if (response.confidence !in 0..100) findings += "confidence_out_of_range"

        val memoryIds = request.ownedMemories.map { it.evidenceId }.toSet()
        val beliefIds = request.ownedBeliefs.map { it.evidenceId }.toSet()
        val allEvidenceIds = listOf(
            request.perceivedFacts,
            request.ownedMemories,
            request.ownedBeliefs,
            request.goals,
            request.relationships,
            request.recentDialogue,
            request.capabilities,
        ).flatten().map { it.evidenceId }.toSet()
        val forbiddenIds = request.forbiddenInformationIds.toSet()
        if (response.memoryIds.any { it !in memoryIds }) findings += "foreign_memory_reference"
        if (response.beliefClaimIds.any { it !in beliefIds }) findings += "foreign_knowledge_reference"
        if (response.withheldInformationIds.any { it !in request.withholdableInformationIds }) {
            findings += "unowned_withheld_information_reference"
        }
        if (response.decisionSummary.evidenceIds.any { it !in allEvidenceIds }) {
            findings += "unknown_decision_evidence_reference"
        }
        val referencedIds = response.memoryIds + response.beliefClaimIds +
            response.withheldInformationIds + response.decisionSummary.evidenceIds
        if (referencedIds.any { it in forbiddenIds }) findings += "forbidden_information_reference"

        val constraints = request.outputConstraints
        if (response.utterance.length > constraints.maxUtteranceChars) findings += "utterance_too_long"
        if (response.speechAct == "SILENCE" && !constraints.silenceAllowed) findings += "silence_not_allowed"
        if (response.speechAct == "SILENCE" && response.utterance.isNotBlank()) findings += "silence_contains_utterance"
        if (response.speechAct == "NONVERBAL" && response.nonverbalAction.isBlank()) findings += "nonverbal_action_missing"
        if (response.speechAct == "DECEIVE" && !constraints.lieAllowed) findings += "deception_not_allowed"
        if (!constraints.nonverbalAllowed && response.nonverbalAction.isNotBlank()) findings += "nonverbal_not_allowed"
        if (response.silenceAllowed && !constraints.silenceAllowed) findings += "response_relaxes_silence_policy"
        val renderedOutput = listOf(response.utterance, response.visibleAction, response.nonverbalAction).joinToString("\n")
        val forbiddenPatterns = (constraints.forbiddenPatterns + request.voiceProfile.forbiddenPatterns).distinct()
        if (forbiddenPatterns.any { it.isNotBlank() && renderedOutput.contains(it, ignoreCase = true) }) {
            findings += "forbidden_voice_pattern"
        }

        val capabilityIds = request.capabilities.map { it.evidenceId }.toSet()
        response.capabilityUseChecks.forEach { check ->
            if (check.actorId != request.actorId) findings += "capability_actor_mismatch"
            if (check.entityId !in capabilityIds) findings += "unavailable_capability_reference"
        }
        if (response.immediateGoal.isBlank() || response.actionIntent.isBlank()) findings += "incomplete_actor_intent"
        if (response.observableConsequence.isBlank()) findings += "observable_consequence_missing"

        val proposal = if (findings.isEmpty()) {
            NpcActionProposal(
                proposalId = "npc-actor-proposal-${request.requestId}",
                npcId = request.actorId,
                actionType = response.actionType,
                intent = response.actionIntent,
                visibleAction = response.visibleAction.ifBlank { response.nonverbalAction },
                hiddenMotive = response.immediateGoal,
                locationId = request.locationId,
                usedMemoryIds = response.memoryIds,
                usedKnowledgeClaimIds = response.beliefClaimIds,
                predictedCost = response.predictedCost,
                observableConsequence = response.observableConsequence,
            )
        } else {
            null
        }
        return NpcActorValidationResult(
            accepted = proposal != null,
            status = if (proposal != null) "ACCEPTED_FOR_GAME_MASTER_REVIEW" else "REJECTED_ACTOR_CONTRACT",
            findings = findings.distinct(),
            proposal = proposal,
        )
    }
}

class NpcActorPilotService(
    private val validator: NpcActorResponseValidator = NpcActorResponseValidator(),
) {
    fun propose(request: NpcActorRequest, gateway: NpcActorGateway): NpcActorValidationResult =
        validator.validateAndMap(request, gateway.propose(request))
}
