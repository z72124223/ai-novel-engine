package com.localnovel.engine.service

import com.localnovel.engine.model.PhysicalActionProposal
import com.localnovel.engine.model.PhysicalActionType
import com.localnovel.engine.model.PhysicalAffordanceValidationResult
import com.localnovel.engine.model.PhysicalArchetype
import com.localnovel.engine.model.PhysicalBarrier
import com.localnovel.engine.model.PhysicalBoundsCm
import com.localnovel.engine.model.PhysicalDimensionsCm
import com.localnovel.engine.model.PhysicalEntityInstance
import com.localnovel.engine.model.PhysicalSupportSurface
import com.localnovel.engine.model.PhysicalSupportUse
import com.localnovel.engine.model.PhysicalVector3Cm
import com.localnovel.engine.model.PhysicalWorldSnapshot
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

class PhysicalAffordanceValidator {
    fun validate(
        archetypeRegistry: List<PhysicalArchetype>,
        snapshot: PhysicalWorldSnapshot,
        proposal: PhysicalActionProposal,
        expectedBoundary: String = NON_CANON_EVALUATION_BOUNDARY,
    ): PhysicalAffordanceValidationResult {
        val findings = linkedSetOf<String>()
        findings += validateSnapshot(archetypeRegistry, snapshot, expectedBoundary)

        val archetypes = archetypeRegistry.associateBy { it.archetypeId }
        val entities = snapshot.entities.associateBy { it.entityId }
        val actor = entities[proposal.actorId]
        val target = entities[proposal.targetId]
        val actorArchetype = actor?.let { archetypes[it.archetypeId] }
        val targetArchetype = target?.let { archetypes[it.archetypeId] }

        if (actor == null) findings += ACTOR_NOT_FOUND
        if (target == null) findings += TARGET_NOT_FOUND
        if (actor != null && actorArchetype == null) findings += ACTOR_ARCHETYPE_NOT_FOUND
        if (target != null && targetArchetype == null) findings += TARGET_ARCHETYPE_NOT_FOUND

        val requiredEffects = mutableListOf<String>()
        if (findings.none { it in fatalReferenceCodes } && actor != null && target != null && actorArchetype != null && targetArchetype != null) {
            when (proposal.actionType) {
                PhysicalActionType.USE_AS_SUPPORT -> validateSupport(
                    archetypes, snapshot, proposal, actor, actorArchetype, target, targetArchetype, findings,
                )
                PhysicalActionType.LIFT -> validateLift(
                    archetypes, snapshot, actor, actorArchetype, target, targetArchetype, findings,
                )
                PhysicalActionType.PUSH -> validatePush(
                    archetypes, snapshot, proposal, actor, actorArchetype, target, targetArchetype, findings,
                )
                PhysicalActionType.REACH -> validateReach(
                    archetypes, snapshot, actor, actorArchetype, target, findings,
                )
                PhysicalActionType.PLACE_ON -> validatePlaceOn(
                    snapshot, proposal, actor, actorArchetype, target, targetArchetype, archetypes, entities, findings,
                )
                PhysicalActionType.DROP -> validateDrop(
                    snapshot, actor, target, targetArchetype, findings, requiredEffects,
                )
                PhysicalActionType.ENTER_CONTAINER -> validateEnterContainer(
                    snapshot, actor, actorArchetype, target, targetArchetype, findings,
                )
                PhysicalActionType.PASS_THROUGH -> validatePassThrough(
                    snapshot, proposal, actor, actorArchetype, targetArchetype, findings,
                )
            }
        }

        val rejectionCodes = findings.toList().sorted()
        return PhysicalAffordanceValidationResult(
            actionId = proposal.actionId,
            accepted = rejectionCodes.isEmpty(),
            status = if (rejectionCodes.isEmpty()) "PHYSICALLY_FEASIBLE_V1" else "REJECTED_PHYSICAL_AFFORDANCE",
            rejectionCodes = rejectionCodes,
            requiredEffects = if (rejectionCodes.isEmpty()) requiredEffects.sorted() else emptyList(),
        )
    }

    private fun validateSupport(
        archetypes: Map<String, PhysicalArchetype>,
        snapshot: PhysicalWorldSnapshot,
        proposal: PhysicalActionProposal,
        actor: PhysicalEntityInstance,
        actorArchetype: PhysicalArchetype,
        target: PhysicalEntityInstance,
        targetArchetype: PhysicalArchetype,
        findings: MutableSet<String>,
    ) {
        validateContact(archetypes, snapshot, actor, actorArchetype, target, findings)
        val supportUse = proposal.supportUse
        if (supportUse == null) findings += SUPPORT_USE_REQUIRED
        val surface = proposal.supportSurfaceId?.let { id ->
            targetArchetype.supportSurfaces.firstOrNull { it.surfaceId == id }
        }
        if (surface == null) {
            findings += SUPPORT_SURFACE_NOT_FOUND
            return
        }
        if (!surface.stable) findings += TARGET_NOT_STABLE_SUPPORT
        if (supportUse != null && supportUse !in surface.allowedUses) findings += SUPPORT_USE_NOT_ALLOWED
        if (surface.allowedBodyPlans.isNotEmpty() && actorArchetype.bodyPlan !in surface.allowedBodyPlans) {
            findings += BODY_PLAN_NOT_SUPPORTED
        }
        if (supportUse != null) {
            val transferredLoad = actorArchetype.massKg * supportLoadFraction(supportUse)
            val existingLoad = target.supportLoadKgBySurface[surface.surfaceId] ?: 0.0
            if (existingLoad + transferredLoad > surface.maxLoadKg + EPSILON) {
                findings += SUPPORT_CAPACITY_EXCEEDED
            }
        }
    }

    private fun validateLift(
        archetypes: Map<String, PhysicalArchetype>,
        snapshot: PhysicalWorldSnapshot,
        actor: PhysicalEntityInstance,
        actorArchetype: PhysicalArchetype,
        target: PhysicalEntityInstance,
        targetArchetype: PhysicalArchetype,
        findings: MutableSet<String>,
    ) {
        validateContact(archetypes, snapshot, actor, actorArchetype, target, findings)
        if (!actorArchetype.canGrasp) findings += GRASPING_NOT_AVAILABLE
        if (!targetArchetype.movable) findings += TARGET_NOT_MOVABLE
        if (targetArchetype.massKg > actorArchetype.maxLiftKg + EPSILON) findings += LIFT_LIMIT_EXCEEDED
    }

    private fun validatePush(
        archetypes: Map<String, PhysicalArchetype>,
        snapshot: PhysicalWorldSnapshot,
        proposal: PhysicalActionProposal,
        actor: PhysicalEntityInstance,
        actorArchetype: PhysicalArchetype,
        target: PhysicalEntityInstance,
        targetArchetype: PhysicalArchetype,
        findings: MutableSet<String>,
    ) {
        validateContact(archetypes, snapshot, actor, actorArchetype, target, findings)
        if (!targetArchetype.movable) findings += TARGET_NOT_MOVABLE
        if (targetArchetype.massKg > actorArchetype.maxPushKg + EPSILON) findings += PUSH_LIMIT_EXCEEDED
        val destination = proposal.destinationCenterCm
        if (destination == null) {
            findings += DESTINATION_REQUIRED
        } else if (snapshot.barriers.any { it.blocksMovement && segmentIntersects(target.centerCm, destination, it) }) {
            findings += MOVEMENT_BLOCKED_BY_BARRIER
        }
    }

    private fun validateReach(
        archetypes: Map<String, PhysicalArchetype>,
        snapshot: PhysicalWorldSnapshot,
        actor: PhysicalEntityInstance,
        actorArchetype: PhysicalArchetype,
        target: PhysicalEntityInstance,
        findings: MutableSet<String>,
    ) {
        validateContact(archetypes, snapshot, actor, actorArchetype, target, findings)
    }

    private fun validatePlaceOn(
        snapshot: PhysicalWorldSnapshot,
        proposal: PhysicalActionProposal,
        actor: PhysicalEntityInstance,
        actorArchetype: PhysicalArchetype,
        placedObject: PhysicalEntityInstance,
        placedArchetype: PhysicalArchetype,
        archetypes: Map<String, PhysicalArchetype>,
        entities: Map<String, PhysicalEntityInstance>,
        findings: MutableSet<String>,
    ) {
        if (placedObject.heldByEntityId != actor.entityId) findings += OBJECT_NOT_HELD_BY_ACTOR
        val supportEntity = proposal.secondaryTargetId?.let(entities::get)
        if (supportEntity == null) {
            findings += SECONDARY_TARGET_NOT_FOUND
            return
        }
        val supportArchetype = archetypes[supportEntity.archetypeId]
        if (supportArchetype == null) {
            findings += TARGET_ARCHETYPE_NOT_FOUND
            return
        }
        validateContact(archetypes, snapshot, actor, actorArchetype, supportEntity, findings)
        val surface = proposal.supportSurfaceId?.let { id ->
            supportArchetype.supportSurfaces.firstOrNull { it.surfaceId == id }
        }
        if (surface == null) {
            findings += SUPPORT_SURFACE_NOT_FOUND
            return
        }
        if (!surface.stable) findings += TARGET_NOT_STABLE_SUPPORT
        if (PhysicalSupportUse.OBJECT_PLACEMENT !in surface.allowedUses) findings += SUPPORT_USE_NOT_ALLOWED
        val existingLoad = supportEntity.supportLoadKgBySurface[surface.surfaceId] ?: 0.0
        if (existingLoad + placedArchetype.massKg > surface.maxLoadKg + EPSILON) {
            findings += SUPPORT_CAPACITY_EXCEEDED
        }
        if (!footprintFits(placedArchetype.dimensionsCm, surface)) findings += SUPPORT_FOOTPRINT_TOO_SMALL
    }

    private fun validateDrop(
        snapshot: PhysicalWorldSnapshot,
        actor: PhysicalEntityInstance,
        target: PhysicalEntityInstance,
        targetArchetype: PhysicalArchetype,
        findings: MutableSet<String>,
        requiredEffects: MutableList<String>,
    ) {
        if (target.heldByEntityId != actor.entityId) findings += OBJECT_NOT_HELD_BY_ACTOR
        if (!targetArchetype.movable) findings += TARGET_NOT_MOVABLE
        if (snapshot.gravityMps2 <= EPSILON) findings += GRAVITY_NOT_POSITIVE
        val bottomHeight = target.centerCm.z - targetArchetype.dimensionsCm.height / 2.0
        if (bottomHeight <= EPSILON) findings += NO_FALL_DISTANCE
        if (findings.isEmpty()) {
            requiredEffects += RELEASE_HELD_OBJECT
            requiredEffects += FALL_DOWNWARD
        }
    }

    private fun validateEnterContainer(
        snapshot: PhysicalWorldSnapshot,
        actor: PhysicalEntityInstance,
        actorArchetype: PhysicalArchetype,
        container: PhysicalEntityInstance,
        containerArchetype: PhysicalArchetype,
        findings: MutableSet<String>,
    ) {
        val opening = containerArchetype.openingCm
        val interior = containerArchetype.containerInteriorCm
        val capacity = containerArchetype.maxContainedLoadKg
        if (opening == null || interior == null || capacity == null) {
            findings += TARGET_NOT_CONTAINER
            return
        }
        if (!fitsOpening(actorArchetype.dimensionsCm, opening)) findings += OPENING_TOO_SMALL
        if (!fitsInterior(actorArchetype.dimensionsCm, interior)) findings += CONTAINER_INTERIOR_TOO_SMALL
        if (actorArchetype.massKg > capacity + EPSILON) findings += CONTAINER_LOAD_EXCEEDED
        if (snapshot.barriers.any { it.blocksMovement && segmentIntersects(actor.centerCm, container.centerCm, it) }) {
            findings += MOVEMENT_BLOCKED_BY_BARRIER
        }
    }

    private fun validatePassThrough(
        snapshot: PhysicalWorldSnapshot,
        proposal: PhysicalActionProposal,
        actor: PhysicalEntityInstance,
        actorArchetype: PhysicalArchetype,
        openingArchetype: PhysicalArchetype,
        findings: MutableSet<String>,
    ) {
        val opening = openingArchetype.openingCm
        if (opening == null) {
            findings += TARGET_NOT_OPENING
            return
        }
        if (!fitsOpening(actorArchetype.dimensionsCm, opening)) findings += OPENING_TOO_SMALL
        val destination = proposal.destinationCenterCm
        if (destination == null) {
            findings += DESTINATION_REQUIRED
        } else if (snapshot.barriers.any { it.blocksMovement && segmentIntersects(actor.centerCm, destination, it) }) {
            findings += MOVEMENT_BLOCKED_BY_BARRIER
        }
    }

    private fun validateContact(
        archetypes: Map<String, PhysicalArchetype>,
        snapshot: PhysicalWorldSnapshot,
        actor: PhysicalEntityInstance,
        actorArchetype: PhysicalArchetype,
        target: PhysicalEntityInstance,
        findings: MutableSet<String>,
    ) {
        val targetArchetype = archetypes.getValue(target.archetypeId)
        val distance = surfaceDistanceCm(
            PhysicalBoundsCm(actor.centerCm, actorArchetype.dimensionsCm),
            PhysicalBoundsCm(target.centerCm, targetArchetype.dimensionsCm),
        )
        if (distance > actorArchetype.maxReachCm + EPSILON) findings += OUT_OF_REACH
        if (snapshot.barriers.any { it.blocksReach && segmentIntersects(actor.centerCm, target.centerCm, it) }) {
            findings += REACH_BLOCKED_BY_BARRIER
        }
    }

    private fun validateSnapshot(
        archetypeRegistry: List<PhysicalArchetype>,
        snapshot: PhysicalWorldSnapshot,
        expectedBoundary: String,
    ): Set<String> {
        val findings = linkedSetOf<String>()
        if (snapshot.boundary != expectedBoundary) findings += INVALID_EVALUATION_BOUNDARY
        if (!snapshot.gravityMps2.isFinite() || snapshot.gravityMps2 < 0.0) findings += INVALID_PHYSICAL_DATA
        if (archetypeRegistry.map { it.archetypeId }.distinct().size != archetypeRegistry.size) findings += INVALID_PHYSICAL_DATA
        if (snapshot.entities.map { it.entityId }.distinct().size != snapshot.entities.size) findings += INVALID_PHYSICAL_DATA
        if (snapshot.barriers.map { it.barrierId }.distinct().size != snapshot.barriers.size) findings += INVALID_PHYSICAL_DATA
        val archetypeIds = archetypeRegistry.map { it.archetypeId }.toSet()
        val entityIds = snapshot.entities.map { it.entityId }.toSet()
        if (snapshot.entities.any { it.archetypeId !in archetypeIds }) findings += INVALID_PHYSICAL_DATA
        if (snapshot.entities.any { entity ->
                listOf(entity.heldByEntityId, entity.supportedByEntityId, entity.containedByEntityId)
                    .filterNotNull().any { it !in entityIds }
            }
        ) findings += INVALID_PHYSICAL_DATA
        if (archetypeRegistry.any { archetype ->
                archetype.archetypeId.isBlank() || archetype.bodyPlan.isBlank() ||
                    !archetype.massKg.isFinite() || archetype.massKg <= 0.0 ||
                    !validDimensions(archetype.dimensionsCm) ||
                    !archetype.maxReachCm.isFinite() || archetype.maxReachCm < 0.0 ||
                    !archetype.maxLiftKg.isFinite() || archetype.maxLiftKg < 0.0 ||
                    !archetype.maxPushKg.isFinite() || archetype.maxPushKg < 0.0 ||
                    archetype.supportSurfaces.map { it.surfaceId }.distinct().size != archetype.supportSurfaces.size ||
                    archetype.supportSurfaces.any { !validSurface(it) } ||
                    (archetype.containerInteriorCm != null && !validDimensions(archetype.containerInteriorCm)) ||
                    (archetype.openingCm != null && !validDimensions(archetype.openingCm)) ||
                    (archetype.maxContainedLoadKg != null && (!archetype.maxContainedLoadKg.isFinite() || archetype.maxContainedLoadKg < 0.0))
            }
        ) findings += INVALID_PHYSICAL_DATA
        if (snapshot.entities.any { entity ->
                !validVector(entity.centerCm) || entity.supportLoadKgBySurface.values.any { !it.isFinite() || it < 0.0 }
            }
        ) findings += INVALID_PHYSICAL_DATA
        if (snapshot.barriers.any { !validDimensions(it.boundsCm.dimensions) || !validVector(it.boundsCm.center) }) {
            findings += INVALID_PHYSICAL_DATA
        }
        return findings
    }

    private fun surfaceDistanceCm(first: PhysicalBoundsCm, second: PhysicalBoundsCm): Double {
        val dx = max(0.0, abs(first.center.x - second.center.x) - (first.dimensions.width + second.dimensions.width) / 2.0)
        val dy = max(0.0, abs(first.center.y - second.center.y) - (first.dimensions.depth + second.dimensions.depth) / 2.0)
        val dz = max(0.0, abs(first.center.z - second.center.z) - (first.dimensions.height + second.dimensions.height) / 2.0)
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    private fun segmentIntersects(start: PhysicalVector3Cm, end: PhysicalVector3Cm, barrier: PhysicalBarrier): Boolean {
        val half = PhysicalVector3Cm(
            barrier.boundsCm.dimensions.width / 2.0,
            barrier.boundsCm.dimensions.depth / 2.0,
            barrier.boundsCm.dimensions.height / 2.0,
        )
        val min = PhysicalVector3Cm(
            barrier.boundsCm.center.x - half.x,
            barrier.boundsCm.center.y - half.y,
            barrier.boundsCm.center.z - half.z,
        )
        val max = PhysicalVector3Cm(
            barrier.boundsCm.center.x + half.x,
            barrier.boundsCm.center.y + half.y,
            barrier.boundsCm.center.z + half.z,
        )
        var tMin = 0.0
        var tMax = 1.0
        val starts = doubleArrayOf(start.x, start.y, start.z)
        val ends = doubleArrayOf(end.x, end.y, end.z)
        val mins = doubleArrayOf(min.x, min.y, min.z)
        val maxs = doubleArrayOf(max.x, max.y, max.z)
        for (axis in 0..2) {
            val delta = ends[axis] - starts[axis]
            if (abs(delta) <= EPSILON) {
                if (starts[axis] < mins[axis] || starts[axis] > maxs[axis]) return false
            } else {
                var t1 = (mins[axis] - starts[axis]) / delta
                var t2 = (maxs[axis] - starts[axis]) / delta
                if (t1 > t2) t1 = t2.also { t2 = t1 }
                tMin = max(tMin, t1)
                tMax = kotlin.math.min(tMax, t2)
                if (tMin > tMax) return false
            }
        }
        return tMax >= 0.0 && tMin <= 1.0
    }

    private fun fitsOpening(body: PhysicalDimensionsCm, opening: PhysicalDimensionsCm): Boolean {
        val bodyCrossSection = minOf(body.width, body.depth)
        val openingCrossSection = maxOf(opening.width, opening.depth)
        return bodyCrossSection <= openingCrossSection + EPSILON && body.height <= opening.height + EPSILON
    }

    private fun fitsInterior(body: PhysicalDimensionsCm, interior: PhysicalDimensionsCm): Boolean {
        val direct = body.width <= interior.width + EPSILON && body.depth <= interior.depth + EPSILON
        val rotated = body.width <= interior.depth + EPSILON && body.depth <= interior.width + EPSILON
        return (direct || rotated) && body.height <= interior.height + EPSILON
    }

    private fun footprintFits(dimensions: PhysicalDimensionsCm, surface: PhysicalSupportSurface): Boolean {
        val direct = dimensions.width <= surface.widthCm + EPSILON && dimensions.depth <= surface.depthCm + EPSILON
        val rotated = dimensions.width <= surface.depthCm + EPSILON && dimensions.depth <= surface.widthCm + EPSILON
        return direct || rotated
    }

    private fun supportLoadFraction(use: PhysicalSupportUse): Double = when (use) {
        PhysicalSupportUse.LIGHT_CONTACT -> 0.05
        PhysicalSupportUse.STAND_UP_ASSIST -> 0.35
        PhysicalSupportUse.FULL_BODY_SUPPORT -> 1.0
        PhysicalSupportUse.OBJECT_PLACEMENT -> 1.0
    }

    private fun validVector(value: PhysicalVector3Cm): Boolean = value.x.isFinite() && value.y.isFinite() && value.z.isFinite()

    private fun validDimensions(value: PhysicalDimensionsCm): Boolean =
        value.width.isFinite() && value.width > 0.0 &&
            value.depth.isFinite() && value.depth > 0.0 &&
            value.height.isFinite() && value.height > 0.0

    private fun validSurface(value: PhysicalSupportSurface): Boolean =
        value.surfaceId.isNotBlank() && value.maxLoadKg.isFinite() && value.maxLoadKg >= 0.0 &&
            value.widthCm.isFinite() && value.widthCm > 0.0 &&
            value.depthCm.isFinite() && value.depthCm > 0.0 && value.allowedUses.isNotEmpty()

    companion object {
        const val NON_CANON_EVALUATION_BOUNDARY = "NON_CANON_EVALUATION"
        const val ACTOR_NOT_FOUND = "ACTOR_NOT_FOUND"
        const val TARGET_NOT_FOUND = "TARGET_NOT_FOUND"
        const val ACTOR_ARCHETYPE_NOT_FOUND = "ACTOR_ARCHETYPE_NOT_FOUND"
        const val TARGET_ARCHETYPE_NOT_FOUND = "TARGET_ARCHETYPE_NOT_FOUND"
        const val INVALID_EVALUATION_BOUNDARY = "INVALID_EVALUATION_BOUNDARY"
        const val INVALID_PHYSICAL_DATA = "INVALID_PHYSICAL_DATA"
        const val OUT_OF_REACH = "OUT_OF_REACH"
        const val REACH_BLOCKED_BY_BARRIER = "REACH_BLOCKED_BY_BARRIER"
        const val MOVEMENT_BLOCKED_BY_BARRIER = "MOVEMENT_BLOCKED_BY_BARRIER"
        const val SUPPORT_USE_REQUIRED = "SUPPORT_USE_REQUIRED"
        const val SUPPORT_SURFACE_NOT_FOUND = "SUPPORT_SURFACE_NOT_FOUND"
        const val TARGET_NOT_STABLE_SUPPORT = "TARGET_NOT_STABLE_SUPPORT"
        const val SUPPORT_USE_NOT_ALLOWED = "SUPPORT_USE_NOT_ALLOWED"
        const val BODY_PLAN_NOT_SUPPORTED = "BODY_PLAN_NOT_SUPPORTED"
        const val SUPPORT_CAPACITY_EXCEEDED = "SUPPORT_CAPACITY_EXCEEDED"
        const val GRASPING_NOT_AVAILABLE = "GRASPING_NOT_AVAILABLE"
        const val TARGET_NOT_MOVABLE = "TARGET_NOT_MOVABLE"
        const val LIFT_LIMIT_EXCEEDED = "LIFT_LIMIT_EXCEEDED"
        const val PUSH_LIMIT_EXCEEDED = "PUSH_LIMIT_EXCEEDED"
        const val DESTINATION_REQUIRED = "DESTINATION_REQUIRED"
        const val OBJECT_NOT_HELD_BY_ACTOR = "OBJECT_NOT_HELD_BY_ACTOR"
        const val SECONDARY_TARGET_NOT_FOUND = "SECONDARY_TARGET_NOT_FOUND"
        const val SUPPORT_FOOTPRINT_TOO_SMALL = "SUPPORT_FOOTPRINT_TOO_SMALL"
        const val GRAVITY_NOT_POSITIVE = "GRAVITY_NOT_POSITIVE"
        const val NO_FALL_DISTANCE = "NO_FALL_DISTANCE"
        const val TARGET_NOT_CONTAINER = "TARGET_NOT_CONTAINER"
        const val OPENING_TOO_SMALL = "OPENING_TOO_SMALL"
        const val CONTAINER_INTERIOR_TOO_SMALL = "CONTAINER_INTERIOR_TOO_SMALL"
        const val CONTAINER_LOAD_EXCEEDED = "CONTAINER_LOAD_EXCEEDED"
        const val TARGET_NOT_OPENING = "TARGET_NOT_OPENING"
        const val RELEASE_HELD_OBJECT = "RELEASE_HELD_OBJECT"
        const val FALL_DOWNWARD = "FALL_DOWNWARD"

        private const val EPSILON = 1e-9
        private val fatalReferenceCodes = setOf(
            ACTOR_NOT_FOUND,
            TARGET_NOT_FOUND,
            ACTOR_ARCHETYPE_NOT_FOUND,
            TARGET_ARCHETYPE_NOT_FOUND,
            INVALID_PHYSICAL_DATA,
        )
    }
}
