@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
enum class PhysicalActionType {
    USE_AS_SUPPORT,
    LIFT,
    PUSH,
    REACH,
    PLACE_ON,
    DROP,
    ENTER_CONTAINER,
    PASS_THROUGH,
}

@Serializable
enum class PhysicalSupportUse {
    LIGHT_CONTACT,
    STAND_UP_ASSIST,
    FULL_BODY_SUPPORT,
    OBJECT_PLACEMENT,
}

@Serializable
data class PhysicalVector3Cm(
    val x: Double,
    val y: Double,
    val z: Double,
)

@Serializable
data class PhysicalDimensionsCm(
    val width: Double,
    val depth: Double,
    val height: Double,
)

@Serializable
data class PhysicalBoundsCm(
    val center: PhysicalVector3Cm,
    val dimensions: PhysicalDimensionsCm,
)

@Serializable
data class PhysicalSupportSurface(
    val surfaceId: String,
    val stable: Boolean,
    val maxLoadKg: Double,
    val widthCm: Double,
    val depthCm: Double,
    val allowedUses: List<PhysicalSupportUse>,
    val allowedBodyPlans: List<String> = emptyList(),
)

@Serializable
data class PhysicalArchetype(
    val archetypeId: String,
    val entityKind: String,
    val bodyPlan: String,
    val massKg: Double,
    val dimensionsCm: PhysicalDimensionsCm,
    val movable: Boolean,
    val canGrasp: Boolean = false,
    val maxReachCm: Double = 0.0,
    val maxLiftKg: Double = 0.0,
    val maxPushKg: Double = 0.0,
    val supportSurfaces: List<PhysicalSupportSurface> = emptyList(),
    val containerInteriorCm: PhysicalDimensionsCm? = null,
    val openingCm: PhysicalDimensionsCm? = null,
    val maxContainedLoadKg: Double? = null,
)

@Serializable
data class PhysicalEntityInstance(
    val entityId: String,
    val label: String,
    val archetypeId: String,
    val centerCm: PhysicalVector3Cm,
    val heldByEntityId: String? = null,
    val supportedByEntityId: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val supportedBySurfaceId: String? = null,
    val containedByEntityId: String? = null,
    val supportLoadKgBySurface: Map<String, Double> = emptyMap(),
)

@Serializable
data class PhysicalBarrier(
    val barrierId: String,
    val boundsCm: PhysicalBoundsCm,
    val blocksReach: Boolean = true,
    val blocksMovement: Boolean = true,
)

@Serializable
data class PhysicalWorldSnapshot(
    val snapshotId: String,
    val boundary: String = "NON_CANON_EVALUATION",
    val gravityMps2: Double = 9.80665,
    val entities: List<PhysicalEntityInstance>,
    val barriers: List<PhysicalBarrier> = emptyList(),
)

@Serializable
data class PhysicalActionProposal(
    val actionId: String,
    val actionType: PhysicalActionType,
    val actorId: String,
    val targetId: String,
    val secondaryTargetId: String? = null,
    val supportSurfaceId: String? = null,
    val supportUse: PhysicalSupportUse? = null,
    val destinationCenterCm: PhysicalVector3Cm? = null,
)

@Serializable
data class PhysicalAffordanceValidationResult(
    val actionId: String,
    val accepted: Boolean,
    val status: String,
    val rejectionCodes: List<String>,
    val requiredEffects: List<String> = emptyList(),
)

@Serializable
data class PhysicalAffordanceEvaluationCase(
    val caseId: String,
    val snapshotId: String,
    val proposal: PhysicalActionProposal,
    val expectedAccepted: Boolean,
    val expectedRejectionCodes: List<String> = emptyList(),
    val expectedRequiredEffects: List<String> = emptyList(),
)

@Serializable
data class PhysicalAffordanceSuite(
    val schemaVersion: String = "1.0",
    val suiteId: String = "world-physics01-affordance-64-v1",
    val boundary: String = "NON_CANON_EVALUATION",
    val archetypes: List<PhysicalArchetype>,
    val snapshots: List<PhysicalWorldSnapshot>,
    val cases: List<PhysicalAffordanceEvaluationCase>,
)

@Serializable
data class PhysicalAffordanceCaseOutcome(
    val caseId: String,
    val matchedExpectation: Boolean,
    val actualAccepted: Boolean,
    val actualRejectionCodes: List<String>,
    val actualRequiredEffects: List<String>,
)

@Serializable
data class PhysicalActionCoverage(
    val actionType: PhysicalActionType,
    val caseCount: Int,
    val acceptedCaseCount: Int,
    val rejectedCaseCount: Int,
)

@Serializable
data class PhysicalAffordanceTechnicalReport(
    val schemaVersion: String = "1.0",
    val suiteId: String,
    val caseCount: Int,
    val expectationMatchCount: Int,
    val expectationMismatchCount: Int,
    val actionCoverage: List<PhysicalActionCoverage>,
    val rejectionCodeCounts: Map<String, Int>,
    val outcomes: List<PhysicalAffordanceCaseOutcome>,
    val technicalDecision: String,
    val runtimeIntegrationStatus: String = "NOT_CONNECTED",
    val claimBoundary: List<String>,
)

@Serializable
data class PhysicalEvidenceFile(
    val path: String,
    val sha256: String,
    val bytes: Long,
)

@Serializable
data class PhysicalEvidenceManifest(
    val schemaVersion: String = "1.0",
    val hashAlgorithm: String = "SHA-256",
    val scope: String = "WORLD-PHYSICS-01 PHYSICAL AFFORDANCE FOUNDATION",
    val selfHashExcluded: Boolean = true,
    val files: List<PhysicalEvidenceFile>,
)
