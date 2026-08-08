package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.FormalTurnPhysicalValidationContext
import com.localnovel.engine.model.PhysicalActionType
import com.localnovel.engine.model.ValidationError
import com.localnovel.engine.support.JsonSupport
import java.security.MessageDigest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object FormalTurnPhysicalJson {
    val strict: Json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = false
    }
}

class FormalTurnPhysicalGateValidator(
    private val physicalValidator: PhysicalAffordanceValidator = PhysicalAffordanceValidator(),
) {
    fun validate(
        context: ContextPacket,
        result: AiTurnResult,
        physicalContext: FormalTurnPhysicalValidationContext?,
    ): List<ValidationError> {
        val proposals = result.physicalActionProposals
        if (proposals.isEmpty() && physicalContext == null) return emptyList()
        if (proposals.isNotEmpty() && physicalContext == null) {
            return listOf(
                ValidationError(
                    PHYSICAL_CONTEXT_REQUIRED,
                    "physicalActionProposals require an engine-owned formal turn physical context.",
                ),
            )
        }
        if (proposals.isEmpty()) {
            return listOf(
                ValidationError(
                    PHYSICAL_PROPOSAL_REQUIRED,
                    "A formal turn physical context cannot be supplied without physicalActionProposals.",
                ),
            )
        }

        physicalContext ?: return emptyList()
        val errors = mutableListOf<ValidationError>()
        validateContextContract(context, result, physicalContext, errors)
        validateProposals(result, physicalContext, errors)
        return errors.distinctBy { it.code to it.message }
    }

    fun canonicalResultSha256(result: AiTurnResult): String =
        sha256(JsonSupport.json.encodeToString(result))

    private fun validateContextContract(
        context: ContextPacket,
        result: AiTurnResult,
        physicalContext: FormalTurnPhysicalValidationContext,
        errors: MutableList<ValidationError>,
    ) {
        if (physicalContext.schemaVersion !in SUPPORTED_SCHEMA_VERSIONS) {
            errors += ValidationError(PHYSICAL_SCHEMA_VERSION_MISMATCH, "Only physical context schemaVersion 1.0 or 2.0 is supported.")
        }
        if (physicalContext.boundary != FORMAL_TURN_BOUNDARY ||
            physicalContext.physicalSnapshot.boundary != FORMAL_TURN_BOUNDARY
        ) {
            errors += ValidationError(
                PHYSICAL_BOUNDARY_MISMATCH,
                "Physical context and snapshot boundary must both be $FORMAL_TURN_BOUNDARY.",
            )
        }

        val bindingsMatch = physicalContext.projectId == context.project.id &&
            physicalContext.sessionId == context.sessionId &&
            physicalContext.turnId == context.turnId &&
            physicalContext.contextId == context.contextId &&
            physicalContext.contextHash == context.contextHash &&
            physicalContext.turnNumber == context.turnNumber &&
            physicalContext.baseStateVersion == context.baseStateVersion &&
            physicalContext.resultId == result.resultId
        if (!bindingsMatch) {
            errors += ValidationError(
                PHYSICAL_CONTEXT_BINDING_MISMATCH,
                "Physical context identity, state version or Context hash does not match the pending turn and result.",
            )
        }

        val actualResultHash = canonicalResultSha256(result)
        if (!physicalContext.resultCanonicalSha256.equals(actualResultHash, ignoreCase = true)) {
            errors += ValidationError(
                PHYSICAL_RESULT_HASH_MISMATCH,
                "Physical context resultCanonicalSha256 does not match the canonical AiTurnResult hash.",
            )
        }

        if (physicalContext.schemaVersion == PhysicalStateReducer.SCHEMA_VERSION) {
            val stateVersion = physicalContext.physicalStateVersion
            if (stateVersion == null || stateVersion < 0) {
                errors += ValidationError(PHYSICAL_STATE_VERSION_INVALID, "V2 physicalStateVersion must be non-negative.")
            }
            val snapshotHash = FormalPhysicalStateHash.snapshot(physicalContext.physicalSnapshot)
            if (!snapshotHash.equals(physicalContext.physicalSnapshotCanonicalSha256, ignoreCase = true)) {
                errors += ValidationError(PHYSICAL_SNAPSHOT_HASH_MISMATCH, "V2 physicalSnapshotCanonicalSha256 does not match the canonical snapshot.")
            }
            val registryHash = FormalPhysicalStateHash.archetypeRegistry(physicalContext.physicalArchetypes)
            if (!registryHash.equals(physicalContext.physicalArchetypeRegistryCanonicalSha256, ignoreCase = true)) {
                errors += ValidationError(PHYSICAL_ARCHETYPE_REGISTRY_HASH_MISMATCH, "V2 physicalArchetypeRegistryCanonicalSha256 does not match the canonical registry.")
            }
        }

        if (physicalContext.physicalArchetypes.isEmpty() ||
            physicalContext.physicalArchetypes.size > MAX_ARCHETYPES ||
            physicalContext.physicalSnapshot.entities.isEmpty() ||
            physicalContext.physicalSnapshot.entities.size > MAX_ENTITIES ||
            physicalContext.physicalSnapshot.barriers.size > MAX_BARRIERS
        ) {
            errors += ValidationError(PHYSICAL_CONTEXT_SIZE_INVALID, "Physical context is empty or exceeds bounded V1 limits.")
        }

        val archetypeIds = physicalContext.physicalArchetypes.map { it.archetypeId }
        val entityIds = physicalContext.physicalSnapshot.entities.map { it.entityId }
        if (archetypeIds.any(String::isBlank) || archetypeIds.size != archetypeIds.distinct().size) {
            errors += ValidationError(PHYSICAL_ARCHETYPE_IDS_INVALID, "Physical archetype IDs must be non-blank and unique.")
        }
        if (entityIds.any(String::isBlank) || entityIds.size != entityIds.distinct().size) {
            errors += ValidationError(PHYSICAL_ENTITY_IDS_INVALID, "Physical entity IDs must be non-blank and unique.")
        }
        if (physicalContext.physicalSnapshot.entities.any { it.archetypeId !in archetypeIds }) {
            errors += ValidationError(PHYSICAL_ENTITY_ARCHETYPE_UNBOUND, "Every physical entity must reference an engine-owned archetype.")
        }

        validateAllowlist(
            "action",
            physicalContext.allowedPhysicalActionTypes,
            MAX_ACTION_TYPES,
            errors,
        )
        validateAllowlist("actor", physicalContext.allowedPhysicalActorIds, MAX_ACTORS, errors)
        validateAllowlist("target", physicalContext.allowedPhysicalTargetIds, MAX_TARGETS, errors)
        if (physicalContext.allowedPhysicalActorIds.any { it !in entityIds } ||
            physicalContext.allowedPhysicalTargetIds.any { it !in entityIds }
        ) {
            errors += ValidationError(PHYSICAL_ALLOWLIST_ENTITY_UNBOUND, "Physical actor and target allowlists must reference snapshot entities.")
        }
    }

    private fun validateProposals(
        result: AiTurnResult,
        physicalContext: FormalTurnPhysicalValidationContext,
        errors: MutableList<ValidationError>,
    ) {
        val proposals = result.physicalActionProposals
        if (proposals.size > MAX_PROPOSALS) {
            errors += ValidationError(PHYSICAL_PROPOSAL_COUNT_INVALID, "A formal turn may declare at most $MAX_PROPOSALS physical proposals.")
        }
        val actionIds = proposals.map { it.actionId }
        if (actionIds.any(String::isBlank) || actionIds.size != actionIds.distinct().size) {
            errors += ValidationError(PHYSICAL_ACTION_IDS_INVALID, "Physical action IDs must be non-blank and unique within the turn.")
        }

        proposals.forEach { proposal ->
            if (proposal.actionType !in physicalContext.allowedPhysicalActionTypes) {
                errors += ValidationError(
                    PHYSICAL_ACTION_NOT_ALLOWLISTED,
                    "Physical action '${proposal.actionId}' uses non-allowlisted type ${proposal.actionType}.",
                )
            }
            if (proposal.actorId !in physicalContext.allowedPhysicalActorIds) {
                errors += ValidationError(
                    PHYSICAL_ACTOR_NOT_ALLOWLISTED,
                    "Physical action '${proposal.actionId}' uses non-allowlisted actor '${proposal.actorId}'.",
                )
            }
            if (proposal.targetId !in physicalContext.allowedPhysicalTargetIds ||
                proposal.secondaryTargetId?.let { it !in physicalContext.allowedPhysicalTargetIds } == true
            ) {
                errors += ValidationError(
                    PHYSICAL_TARGET_NOT_ALLOWLISTED,
                    "Physical action '${proposal.actionId}' uses a non-allowlisted target.",
                )
            }
            if (proposal.actionType !in FORMAL_TRANSIENT_ACTIONS && physicalContext.schemaVersion != PhysicalStateReducer.SCHEMA_VERSION) {
                errors += ValidationError(
                    PHYSICAL_STATE_PERSISTENCE_UNAVAILABLE,
                    "Physical action '${proposal.actionId}' (${proposal.actionType}) requires a Hash-bound V2 physical state context.",
                )
            }
        }

        if (errors.isNotEmpty()) return

        if (proposals.any { it.actionType in PhysicalStateReducer.STATEFUL_ACTIONS }) {
            runCatching { PhysicalStateReducer(physicalValidator).plan(physicalContext, result) }
                .exceptionOrNull()
                ?.let { error ->
                    errors += ValidationError(
                        PHYSICAL_STATE_REDUCTION_REJECTED,
                        error.message ?: "Stateful physical action reduction failed.",
                    )
                }
            return
        }

        proposals.forEach { proposal ->
            val validation = physicalValidator.validate(
                physicalContext.physicalArchetypes,
                physicalContext.physicalSnapshot,
                proposal,
                FORMAL_TURN_BOUNDARY,
            )
            if (!validation.accepted) {
                errors += ValidationError(
                    PHYSICAL_AFFORDANCE_REJECTED,
                    "Physical action '${proposal.actionId}' was rejected: ${validation.rejectionCodes.joinToString()}.",
                )
            } else if (validation.requiredEffects.isNotEmpty()) {
                errors += ValidationError(
                    PHYSICAL_UNPERSISTED_REQUIRED_EFFECTS,
                    "Physical action '${proposal.actionId}' requires unpersisted effects: ${validation.requiredEffects.joinToString()}.",
                )
            }
        }
    }

    private fun <T> validateAllowlist(
        label: String,
        values: List<T>,
        maximum: Int,
        errors: MutableList<ValidationError>,
    ) {
        if (values.isEmpty() || values.size > maximum || values.size != values.distinct().size) {
            errors += ValidationError(PHYSICAL_ALLOWLIST_INVALID, "Physical $label allowlist must be non-empty, unique and contain at most $maximum entries.")
        }
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    companion object {
        const val PHYSICAL_CONTEXT_REQUIRED = "physical_context_required"
        const val PHYSICAL_PROPOSAL_REQUIRED = "physical_proposal_required"
        const val PHYSICAL_SCHEMA_VERSION_MISMATCH = "physical_schema_version_mismatch"
        const val PHYSICAL_BOUNDARY_MISMATCH = "physical_boundary_mismatch"
        const val PHYSICAL_CONTEXT_BINDING_MISMATCH = "physical_context_binding_mismatch"
        const val PHYSICAL_RESULT_HASH_MISMATCH = "physical_result_hash_mismatch"
        const val PHYSICAL_STATE_VERSION_INVALID = "physical_state_version_invalid"
        const val PHYSICAL_SNAPSHOT_HASH_MISMATCH = "physical_snapshot_hash_mismatch"
        const val PHYSICAL_ARCHETYPE_REGISTRY_HASH_MISMATCH = "physical_archetype_registry_hash_mismatch"
        const val PHYSICAL_CONTEXT_SIZE_INVALID = "physical_context_size_invalid"
        const val PHYSICAL_ARCHETYPE_IDS_INVALID = "physical_archetype_ids_invalid"
        const val PHYSICAL_ENTITY_IDS_INVALID = "physical_entity_ids_invalid"
        const val PHYSICAL_ENTITY_ARCHETYPE_UNBOUND = "physical_entity_archetype_unbound"
        const val PHYSICAL_ALLOWLIST_INVALID = "physical_allowlist_invalid"
        const val PHYSICAL_ALLOWLIST_ENTITY_UNBOUND = "physical_allowlist_entity_unbound"
        const val PHYSICAL_PROPOSAL_COUNT_INVALID = "physical_proposal_count_invalid"
        const val PHYSICAL_ACTION_IDS_INVALID = "physical_action_ids_invalid"
        const val PHYSICAL_ACTION_NOT_ALLOWLISTED = "physical_action_not_allowlisted"
        const val PHYSICAL_ACTOR_NOT_ALLOWLISTED = "physical_actor_not_allowlisted"
        const val PHYSICAL_TARGET_NOT_ALLOWLISTED = "physical_target_not_allowlisted"
        const val PHYSICAL_STATE_PERSISTENCE_UNAVAILABLE = "physical_state_persistence_unavailable"
        const val PHYSICAL_STATE_REDUCTION_REJECTED = "physical_state_reduction_rejected"
        const val PHYSICAL_AFFORDANCE_REJECTED = "physical_affordance_rejected"
        const val PHYSICAL_UNPERSISTED_REQUIRED_EFFECTS = "physical_unpersisted_required_effects"

        const val SCHEMA_VERSION = "1.0"
        val SUPPORTED_SCHEMA_VERSIONS = setOf(SCHEMA_VERSION, PhysicalStateReducer.SCHEMA_VERSION)
        const val FORMAL_TURN_BOUNDARY = "FORMAL_TURN_VALIDATION_INPUT"
        private val FORMAL_TRANSIENT_ACTIONS = setOf(
            PhysicalActionType.USE_AS_SUPPORT,
            PhysicalActionType.REACH,
        )
        private const val MAX_PROPOSALS = 8
        private const val MAX_ACTION_TYPES = 8
        private const val MAX_ACTORS = 16
        private const val MAX_TARGETS = 32
        private const val MAX_ARCHETYPES = 32
        private const val MAX_ENTITIES = 32
        private const val MAX_BARRIERS = 32
    }
}
