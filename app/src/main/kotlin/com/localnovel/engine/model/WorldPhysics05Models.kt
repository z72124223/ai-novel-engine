package com.localnovel.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class WorldPhysics05SourceEvidence(
    val path: String,
    val sha256: String,
    val bytes: Long,
)
@Serializable
data class WorldPhysics05EntityEvidence(
    val entityId: String,
    val centerCm: PhysicalVector3Cm,
    val heldByEntityId: String? = null,
    val supportedByEntityId: String? = null,
    val supportedBySurfaceId: String? = null,
    val containedByEntityId: String? = null,
)

@Serializable
data class WorldPhysics05TurnEvidence(
    val turnNumber: Int,
    val playerInput: String,
    val rejectedActionId: String? = null,
    val rejectionCodes: List<String> = emptyList(),
    val resolvedActionIds: List<String>,
    val resolvedActionTypes: List<PhysicalActionType>,
    val storyStateVersionBefore: Int,
    val storyStateVersionAfter: Int,
    val physicalStateVersionBefore: Int,
    val physicalStateVersionAfter: Int,
    val transitionCountBefore: Int,
    val transitionCountAfter: Int,
    val actorAfter: WorldPhysics05EntityEvidence,
    val toolAfter: WorldPhysics05EntityEvidence,
)

@Serializable
data class WorldPhysics05PilotChecks(
    val sourceHashesVerified: Boolean,
    val dogSupportRejected: Boolean,
    val rejectedAttemptStateUnchanged: Boolean,
    val samePendingTurnRepairApplied: Boolean,
    val heldToolFollowedHolder: Boolean,
    val placedToolRelationPersisted: Boolean,
    val droppedToolRelationsCleared: Boolean,
    val formalRuntimeTurnCountVerified: Boolean,
)

@Serializable
data class WorldPhysics05PilotReport(
    val schemaVersion: String = "1.0",
    val pilotId: String = "world-physics05-screen01-scene03-five-turn-v1",
    val boundary: String = "NON_CANON_EVALUATION",
    val storyBoundary: String = "NON_CANON_PILOT",
    val storyTitle: String,
    val sceneId: String,
    val locationId: String,
    val sourceFiles: List<WorldPhysics05SourceEvidence>,
    val resolvedTurnCount: Int,
    val rejectedAttemptCount: Int,
    val physicalTransitionCount: Int,
    val turns: List<WorldPhysics05TurnEvidence>,
    val checks: WorldPhysics05PilotChecks,
    val technicalDecision: String,
    val claimBoundary: List<String>,
)
