package com.localnovel.engine.service

import com.localnovel.engine.model.NpcActorPhysicalBindingInput
import com.localnovel.engine.model.NpcActorPhysicalBindingResult
import com.localnovel.engine.model.NpcActorPhysicalEngineContext
import com.localnovel.engine.model.NpcActorPhysicalPrompt
import com.localnovel.engine.model.NpcActorPhysicalResponse
import com.localnovel.engine.model.NpcActorRequest
import com.localnovel.engine.model.PhysicalActionType
import com.localnovel.engine.model.PhysicalArchetype
import com.localnovel.engine.model.PhysicalWorldSnapshot
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun interface NpcActorPhysicalGateway {
    fun propose(prompt: NpcActorPhysicalPrompt): NpcActorPhysicalResponse
}

class FakeNpcActorPhysicalGateway(
    private val responsesByRequestId: Map<String, NpcActorPhysicalResponse>,
) : NpcActorPhysicalGateway {
    override fun propose(prompt: NpcActorPhysicalPrompt): NpcActorPhysicalResponse =
        responsesByRequestId[prompt.actorRequest.requestId]
            ?: error("No fake NPC physical response for request: ${prompt.actorRequest.requestId}")
}

class FileNpcActorPhysicalGateway(
    private val promptOutputPath: Path,
    private val responseInputPath: Path,
) : NpcActorPhysicalGateway {
    override fun propose(prompt: NpcActorPhysicalPrompt): NpcActorPhysicalResponse {
        promptOutputPath.parent?.let(Files::createDirectories)
        Files.writeString(promptOutputPath, NpcActorPhysicalJson.strict.encodeToString(prompt))
        require(Files.isRegularFile(responseInputPath)) {
            "NPC physical response file does not exist: $responseInputPath"
        }
        return NpcActorPhysicalJson.strict.decodeFromString(
            Files.readString(responseInputPath).removePrefix("\uFEFF"),
        )
    }
}

object NpcActorPhysicalJson {
    val strict: Json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = false
    }
}

class NpcActorPhysicalBindingRequestBuilder {
    fun build(
        actorRequest: NpcActorRequest,
        physicalArchetypes: List<PhysicalArchetype>,
        physicalSnapshot: PhysicalWorldSnapshot,
        allowedPhysicalActionTypes: List<PhysicalActionType>,
        visiblePhysicalTargetIds: List<String>,
    ): NpcActorPhysicalBindingInput {
        val actions = allowedPhysicalActionTypes.distinct()
        val targets = visiblePhysicalTargetIds.distinct()
        return NpcActorPhysicalBindingInput(
            prompt = NpcActorPhysicalPrompt(
                actorRequest = actorRequest,
                allowedPhysicalActionTypes = actions,
                visiblePhysicalTargetIds = targets,
            ),
            engineContext = NpcActorPhysicalEngineContext(
                requestId = actorRequest.requestId,
                physicalArchetypes = physicalArchetypes,
                physicalSnapshot = physicalSnapshot,
                allowedPhysicalActionTypes = actions,
                allowedPhysicalTargetIds = targets,
            ),
        )
    }
}

class NpcActorPhysicalBindingValidator(
    private val actorValidator: NpcActorResponseValidator = NpcActorResponseValidator(),
    private val physicalValidator: PhysicalAffordanceValidator = PhysicalAffordanceValidator(),
) {
    fun validateAndMap(
        prompt: NpcActorPhysicalPrompt,
        engineContext: NpcActorPhysicalEngineContext,
        response: NpcActorPhysicalResponse,
    ): NpcActorPhysicalBindingResult {
        val bindingFindings = validateBindingContract(prompt, engineContext, response)
        val actorValidation = actorValidator.validateAndMap(prompt.actorRequest, response.actorResponse)
        val actorFindings = actorValidation.findings.map { "actor:$it" }

        if (!actorValidation.accepted) {
            return NpcActorPhysicalBindingResult(
                accepted = false,
                status = REJECTED_ACTOR_CONTRACT,
                findings = (bindingFindings + actorFindings).distinct(),
                actorValidation = actorValidation,
            )
        }
        if (bindingFindings.isNotEmpty()) {
            return NpcActorPhysicalBindingResult(
                accepted = false,
                status = REJECTED_PHYSICAL_BINDING_CONTRACT,
                findings = bindingFindings.distinct(),
                actorValidation = actorValidation,
            )
        }

        val physicalValidation = physicalValidator.validate(
            engineContext.physicalArchetypes,
            engineContext.physicalSnapshot,
            response.physicalActionProposal,
        )
        if (!physicalValidation.accepted) {
            return NpcActorPhysicalBindingResult(
                accepted = false,
                status = REJECTED_PHYSICAL_AFFORDANCE,
                findings = physicalValidation.rejectionCodes.map { "physical:$it" },
                actorValidation = actorValidation,
                physicalValidation = physicalValidation,
            )
        }

        return NpcActorPhysicalBindingResult(
            accepted = true,
            status = ACCEPTED_FOR_GAME_MASTER_REVIEW_PHYSICALLY_VALIDATED,
            actorValidation = actorValidation,
            physicalValidation = physicalValidation,
            proposal = actorValidation.proposal,
        )
    }

    private fun validateBindingContract(
        prompt: NpcActorPhysicalPrompt,
        engineContext: NpcActorPhysicalEngineContext,
        response: NpcActorPhysicalResponse,
    ): List<String> {
        val findings = mutableListOf<String>()
        if (prompt.schemaVersion != SCHEMA_VERSION || response.schemaVersion != SCHEMA_VERSION ||
            engineContext.schemaVersion != SCHEMA_VERSION
        ) {
            findings += "binding:schema_version_mismatch"
        }
        if (prompt.boundary != NON_CANON_EVALUATION || engineContext.boundary != NON_CANON_EVALUATION ||
            engineContext.physicalSnapshot.boundary != NON_CANON_EVALUATION
        ) {
            findings += "binding:boundary_not_non_canon_evaluation"
        }
        val requestId = prompt.actorRequest.requestId
        if (engineContext.requestId != requestId || response.requestId != requestId) {
            findings += "binding:request_id_mismatch"
        }
        if (prompt.allowedPhysicalActionTypes != engineContext.allowedPhysicalActionTypes ||
            prompt.visiblePhysicalTargetIds != engineContext.allowedPhysicalTargetIds
        ) {
            findings += "binding:prompt_context_allowlist_mismatch"
        }
        if (prompt.allowedPhysicalActionTypes.isEmpty() ||
            prompt.allowedPhysicalActionTypes.size > MAX_ACTION_TYPES ||
            prompt.allowedPhysicalActionTypes.size != prompt.allowedPhysicalActionTypes.distinct().size
        ) {
            findings += "binding:invalid_action_allowlist"
        }
        if (prompt.visiblePhysicalTargetIds.size > MAX_TARGETS ||
            prompt.visiblePhysicalTargetIds.size != prompt.visiblePhysicalTargetIds.distinct().size
        ) {
            findings += "binding:invalid_target_allowlist"
        }
        if (engineContext.physicalArchetypes.size > MAX_ARCHETYPES ||
            engineContext.physicalSnapshot.entities.size > MAX_ENTITIES ||
            engineContext.physicalSnapshot.barriers.size > MAX_BARRIERS
        ) {
            findings += "binding:physical_context_too_large"
        }
        val archetypeIds = engineContext.physicalArchetypes.map { it.archetypeId }
        if (archetypeIds.size != archetypeIds.distinct().size) findings += "binding:duplicate_archetype_id"
        val entityIds = engineContext.physicalSnapshot.entities.map { it.entityId }
        if (entityIds.size != entityIds.distinct().size) findings += "binding:duplicate_entity_id"
        if (engineContext.physicalSnapshot.entities.any { it.archetypeId !in archetypeIds }) {
            findings += "binding:unbound_entity_archetype"
        }
        if (prompt.actorRequest.actorId !in entityIds) findings += "binding:actor_not_in_physical_snapshot"
        if (prompt.visiblePhysicalTargetIds.any { it !in entityIds }) {
            findings += "binding:visible_target_not_in_physical_snapshot"
        }

        val proposal = response.physicalActionProposal
        if (proposal.actorId != prompt.actorRequest.actorId) findings += "binding:physical_actor_mismatch"
        if (proposal.actionType !in prompt.allowedPhysicalActionTypes) findings += "binding:physical_action_not_allowlisted"
        if (proposal.targetId !in prompt.visiblePhysicalTargetIds) findings += "binding:physical_target_not_bound"
        if (proposal.secondaryTargetId != null && proposal.secondaryTargetId !in prompt.visiblePhysicalTargetIds) {
            findings += "binding:physical_secondary_target_not_bound"
        }
        if (proposal.actionId.isBlank()) findings += "binding:physical_action_id_missing"
        return findings
    }

    companion object {
        const val ACCEPTED_FOR_GAME_MASTER_REVIEW_PHYSICALLY_VALIDATED =
            "ACCEPTED_FOR_GAME_MASTER_REVIEW_PHYSICALLY_VALIDATED"
        const val REJECTED_ACTOR_CONTRACT = "REJECTED_ACTOR_CONTRACT"
        const val REJECTED_PHYSICAL_BINDING_CONTRACT = "REJECTED_PHYSICAL_BINDING_CONTRACT"
        const val REJECTED_PHYSICAL_AFFORDANCE = "REJECTED_PHYSICAL_AFFORDANCE"
        private const val SCHEMA_VERSION = "1.0"
        private const val NON_CANON_EVALUATION = "NON_CANON_EVALUATION"
        private const val MAX_ACTION_TYPES = 8
        private const val MAX_TARGETS = 32
        private const val MAX_ARCHETYPES = 32
        private const val MAX_ENTITIES = 32
        private const val MAX_BARRIERS = 32
    }
}

class NpcActorPhysicalPilotService(
    private val validator: NpcActorPhysicalBindingValidator = NpcActorPhysicalBindingValidator(),
) {
    fun propose(
        input: NpcActorPhysicalBindingInput,
        gateway: NpcActorPhysicalGateway,
    ): NpcActorPhysicalBindingResult = validator.validateAndMap(
        input.prompt,
        input.engineContext,
        gateway.propose(input.prompt),
    )
}
