package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.FormalPhysicalStateTransition
import com.localnovel.engine.model.FormalPhysicalStateTransitionPlan
import com.localnovel.engine.model.FormalTurnPhysicalValidationContext
import com.localnovel.engine.model.PhysicalActionProposal
import com.localnovel.engine.model.PhysicalActionType
import com.localnovel.engine.model.PhysicalArchetype
import com.localnovel.engine.model.PhysicalEntityInstance
import com.localnovel.engine.model.PhysicalVector3Cm
import com.localnovel.engine.model.PhysicalWorldSnapshot
import com.localnovel.engine.support.JsonSupport
import java.security.MessageDigest
import kotlinx.serialization.encodeToString

object FormalPhysicalStateHash {
    fun archetypeRegistry(archetypes: List<PhysicalArchetype>): String = sha256(
        JsonSupport.json.encodeToString(
            normalizeArchetypes(archetypes),
        ),
    )

    fun normalizeArchetypes(archetypes: List<PhysicalArchetype>): List<PhysicalArchetype> =
        archetypes.sortedBy { it.archetypeId }.map { archetype ->
            archetype.copy(
                supportSurfaces = archetype.supportSurfaces.sortedBy { it.surfaceId }.map { surface ->
                    surface.copy(
                        allowedUses = surface.allowedUses.distinct().sortedBy { it.name },
                        allowedBodyPlans = surface.allowedBodyPlans.distinct().sorted(),
                    )
                },
            )
        }

    fun snapshot(snapshot: PhysicalWorldSnapshot): String = sha256(
        JsonSupport.json.encodeToString(normalizeSnapshot(snapshot)),
    )

    fun normalizeSnapshot(snapshot: PhysicalWorldSnapshot): PhysicalWorldSnapshot = snapshot.copy(
        entities = snapshot.entities.sortedBy { it.entityId }.map { entity ->
            entity.copy(supportLoadKgBySurface = entity.supportLoadKgBySurface.toSortedMap())
        },
        barriers = snapshot.barriers.sortedBy { it.barrierId },
    )

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}

class PhysicalStateReducer(
    private val affordanceValidator: PhysicalAffordanceValidator = PhysicalAffordanceValidator(),
) {
    fun plan(
        context: FormalTurnPhysicalValidationContext,
        result: AiTurnResult,
    ): FormalPhysicalStateTransitionPlan {
        require(context.schemaVersion == SCHEMA_VERSION) { "Stateful physical persistence requires schemaVersion $SCHEMA_VERSION." }
        val startVersion = requireNotNull(context.physicalStateVersion) { "physicalStateVersion is required." }
        require(startVersion >= 0) { "physicalStateVersion must be non-negative." }
        val registryHash = FormalPhysicalStateHash.archetypeRegistry(context.physicalArchetypes)
        val startHash = FormalPhysicalStateHash.snapshot(context.physicalSnapshot)
        require(registryHash.equals(context.physicalArchetypeRegistryCanonicalSha256, ignoreCase = true)) {
            "Physical archetype registry Hash mismatch."
        }
        require(startHash.equals(context.physicalSnapshotCanonicalSha256, ignoreCase = true)) {
            "Physical snapshot Hash mismatch."
        }

        var stateVersion = startVersion
        var snapshot = FormalPhysicalStateHash.normalizeSnapshot(context.physicalSnapshot)
        val transitions = mutableListOf<FormalPhysicalStateTransition>()
        result.physicalActionProposals.forEach { proposal ->
            val validation = affordanceValidator.validate(
                context.physicalArchetypes,
                snapshot,
                proposal,
                FormalTurnPhysicalGateValidator.FORMAL_TURN_BOUNDARY,
            )
            require(validation.accepted) {
                "Physical action '${proposal.actionId}' is not feasible: ${validation.rejectionCodes.joinToString()}."
            }
            if (proposal.actionType !in STATEFUL_ACTIONS) return@forEach

            val beforeHash = FormalPhysicalStateHash.snapshot(snapshot)
            val reduction = reduceOne(context.physicalArchetypes, snapshot, proposal)
            stateVersion += 1
            snapshot = FormalPhysicalStateHash.normalizeSnapshot(
                reduction.snapshot.copy(snapshotId = "${context.sessionId}-physical-v$stateVersion"),
            )
            val afterHash = FormalPhysicalStateHash.snapshot(snapshot)
            transitions += FormalPhysicalStateTransition(
                actionId = proposal.actionId,
                actionType = proposal.actionType,
                beforeStateVersion = stateVersion - 1,
                afterStateVersion = stateVersion,
                beforeSnapshotCanonicalSha256 = beforeHash,
                afterSnapshotCanonicalSha256 = afterHash,
                proposal = proposal,
                appliedEffects = (validation.requiredEffects + reduction.effects).distinct().sorted(),
                afterSnapshot = snapshot,
            )
        }
        require(transitions.isNotEmpty()) { "No stateful physical action was supplied." }
        return FormalPhysicalStateTransitionPlan(
            projectId = context.projectId,
            sessionId = context.sessionId,
            startStateVersion = startVersion,
            finalStateVersion = stateVersion,
            physicalArchetypeRegistryCanonicalSha256 = registryHash,
            startSnapshotCanonicalSha256 = startHash,
            finalSnapshotCanonicalSha256 = FormalPhysicalStateHash.snapshot(snapshot),
            transitions = transitions,
            finalSnapshot = snapshot,
        )
    }

    private fun reduceOne(
        archetypes: List<PhysicalArchetype>,
        snapshot: PhysicalWorldSnapshot,
        proposal: PhysicalActionProposal,
    ): Reduction {
        val archetypeById = archetypes.associateBy { it.archetypeId }
        val entityById = snapshot.entities.associateBy { it.entityId }.toMutableMap()
        val actor = requireNotNull(entityById[proposal.actorId]) { "Physical actor not found: ${proposal.actorId}" }
        val target = requireNotNull(entityById[proposal.targetId]) { "Physical target not found: ${proposal.targetId}" }
        val effects = mutableListOf<String>()

        when (proposal.actionType) {
            PhysicalActionType.LIFT -> {
                val movedDependents = translateWithAttachedDependents(entityById, target.entityId, actor.centerCm)
                entityById[target.entityId] = target.copy(
                    centerCm = actor.centerCm,
                    heldByEntityId = actor.entityId,
                    supportedByEntityId = null,
                    supportedBySurfaceId = null,
                    containedByEntityId = null,
                )
                effects += listOf("SET_HELD_BY", "CLEAR_SUPPORTED_BY", "CLEAR_CONTAINED_BY", "MOVE_TO_ACTOR_ANCHOR")
                if (movedDependents) effects += "MOVE_ATTACHED_DEPENDENTS"
            }

            PhysicalActionType.PUSH -> {
                val destination = requireNotNull(proposal.destinationCenterCm) { "PUSH requires destinationCenterCm." }
                val movedDependents = translateWithAttachedDependents(entityById, target.entityId, destination)
                entityById[target.entityId] = requireNotNull(entityById[target.entityId]).withExclusivePosition(destination)
                effects += listOf("MOVE_TO_DESTINATION", "CLEAR_EXCLUSIVE_RELATIONS")
                if (movedDependents) effects += "MOVE_ATTACHED_DEPENDENTS"
            }

            PhysicalActionType.PLACE_ON -> {
                val support = requireNotNull(proposal.secondaryTargetId?.let(entityById::get)) {
                    "PLACE_ON requires an existing secondary target."
                }
                val supportArchetype = requireNotNull(archetypeById[support.archetypeId])
                val targetArchetype = requireNotNull(archetypeById[target.archetypeId])
                val surfaceId = requireNotNull(proposal.supportSurfaceId) { "PLACE_ON requires supportSurfaceId." }
                require(supportArchetype.supportSurfaces.any { it.surfaceId == surfaceId }) {
                    "Support surface not found: $surfaceId"
                }
                val destination = PhysicalVector3Cm(
                    x = support.centerCm.x,
                    y = support.centerCm.y,
                    z = support.centerCm.z + supportArchetype.dimensionsCm.height / 2.0 + targetArchetype.dimensionsCm.height / 2.0,
                )
                val movedDependents = translateWithAttachedDependents(entityById, target.entityId, destination)
                entityById[target.entityId] = requireNotNull(entityById[target.entityId]).copy(
                    centerCm = destination,
                    heldByEntityId = null,
                    supportedByEntityId = support.entityId,
                    supportedBySurfaceId = surfaceId,
                    containedByEntityId = null,
                )
                effects += listOf("RELEASE_HELD_OBJECT", "SET_SUPPORTED_BY", "MOVE_TO_SUPPORT_SURFACE")
                if (movedDependents) effects += "MOVE_ATTACHED_DEPENDENTS"
            }

            PhysicalActionType.DROP -> {
                val targetArchetype = requireNotNull(archetypeById[target.archetypeId])
                val destination = target.centerCm.copy(z = targetArchetype.dimensionsCm.height / 2.0)
                val movedDependents = translateWithAttachedDependents(entityById, target.entityId, destination)
                entityById[target.entityId] = requireNotNull(entityById[target.entityId]).copy(
                    centerCm = destination,
                    heldByEntityId = null,
                    supportedByEntityId = null,
                    supportedBySurfaceId = null,
                    containedByEntityId = null,
                )
                effects += listOf("RELEASE_HELD_OBJECT", "FALL_DOWNWARD", "MOVE_TO_GROUND_PLANE")
                if (movedDependents) effects += "MOVE_ATTACHED_DEPENDENTS"
            }

            PhysicalActionType.ENTER_CONTAINER -> {
                val movedDependents = translateWithAttachedDependents(entityById, actor.entityId, target.centerCm)
                entityById[actor.entityId] = requireNotNull(entityById[actor.entityId]).copy(
                    centerCm = target.centerCm,
                    heldByEntityId = null,
                    supportedByEntityId = null,
                    supportedBySurfaceId = null,
                    containedByEntityId = target.entityId,
                )
                effects += listOf("SET_CONTAINED_BY", "MOVE_TO_CONTAINER_ANCHOR", "CLEAR_SUPPORT_RELATION")
                if (movedDependents) effects += "MOVE_ATTACHED_DEPENDENTS"
            }

            PhysicalActionType.PASS_THROUGH -> {
                val destination = requireNotNull(proposal.destinationCenterCm) { "PASS_THROUGH requires destinationCenterCm." }
                val movedDependents = translateWithAttachedDependents(entityById, actor.entityId, destination)
                entityById[actor.entityId] = requireNotNull(entityById[actor.entityId]).copy(
                    centerCm = destination,
                    supportedByEntityId = null,
                    supportedBySurfaceId = null,
                    containedByEntityId = null,
                )
                effects += listOf("MOVE_TO_DESTINATION", "CLEAR_SUPPORT_RELATION", "CLEAR_CONTAINMENT_RELATION")
                if (movedDependents) effects += "MOVE_ATTACHED_DEPENDENTS"
            }

            PhysicalActionType.USE_AS_SUPPORT,
            PhysicalActionType.REACH,
            -> error("Transient physical action cannot create a persisted transition: ${proposal.actionType}")
        }

        val withLoads = recomputeSupportLoads(archetypeById, entityById.values.toList())
        return Reduction(snapshot.copy(entities = withLoads), effects)
    }

    private fun recomputeSupportLoads(
        archetypes: Map<String, PhysicalArchetype>,
        entities: List<PhysicalEntityInstance>,
    ): List<PhysicalEntityInstance> {
        val loads = mutableMapOf<String, MutableMap<String, Double>>()
        entities.forEach { entity ->
            val supportId = entity.supportedByEntityId ?: return@forEach
            val surfaceId = requireNotNull(entity.supportedBySurfaceId) {
                "supportedBySurfaceId is required when supportedByEntityId is set."
            }
            val mass = requireNotNull(archetypes[entity.archetypeId]).massKg
            val surfaceLoads = loads.getOrPut(supportId) { mutableMapOf() }
            surfaceLoads[surfaceId] = (surfaceLoads[surfaceId] ?: 0.0) + mass
        }
        return entities.map { entity ->
            entity.copy(supportLoadKgBySurface = loads[entity.entityId]?.toSortedMap().orEmpty())
        }
    }

    private fun translateWithAttachedDependents(
        entityById: MutableMap<String, PhysicalEntityInstance>,
        rootEntityId: String,
        destination: PhysicalVector3Cm,
    ): Boolean {
        val root = requireNotNull(entityById[rootEntityId]) { "Physical entity not found: $rootEntityId" }
        val delta = PhysicalVector3Cm(
            x = destination.x - root.centerCm.x,
            y = destination.y - root.centerCm.y,
            z = destination.z - root.centerCm.z,
        )
        val movedIds = linkedSetOf(rootEntityId)
        val queue = ArrayDeque<String>()
        queue.add(rootEntityId)
        while (queue.isNotEmpty()) {
            val parentId = queue.removeFirst()
            entityById.values
                .filter { entity ->
                    entity.heldByEntityId == parentId ||
                        entity.supportedByEntityId == parentId ||
                        entity.containedByEntityId == parentId
                }
                .forEach { dependent ->
                    if (movedIds.add(dependent.entityId)) queue.add(dependent.entityId)
                }
        }
        movedIds.forEach { entityId ->
            val entity = requireNotNull(entityById[entityId])
            entityById[entityId] = entity.copy(
                centerCm = PhysicalVector3Cm(
                    x = entity.centerCm.x + delta.x,
                    y = entity.centerCm.y + delta.y,
                    z = entity.centerCm.z + delta.z,
                ),
            )
        }
        return movedIds.size > 1
    }

    private fun PhysicalEntityInstance.withExclusivePosition(destination: PhysicalVector3Cm): PhysicalEntityInstance = copy(
        centerCm = destination,
        heldByEntityId = null,
        supportedByEntityId = null,
        supportedBySurfaceId = null,
        containedByEntityId = null,
    )

    private data class Reduction(
        val snapshot: PhysicalWorldSnapshot,
        val effects: List<String>,
    )

    companion object {
        const val SCHEMA_VERSION = "2.0"
        const val RUNTIME_BOUNDARY = "FORMAL_TURN_RUNTIME_NON_CANON"
        val STATEFUL_ACTIONS: Set<PhysicalActionType> = setOf(
            PhysicalActionType.LIFT,
            PhysicalActionType.PUSH,
            PhysicalActionType.PLACE_ON,
            PhysicalActionType.DROP,
            PhysicalActionType.ENTER_CONTAINER,
            PhysicalActionType.PASS_THROUGH,
        )
    }
}
