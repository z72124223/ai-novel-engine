@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
data class FormalTurnPhysicalValidationContext(
    val schemaVersion: String = "1.0",
    val boundary: String = "FORMAL_TURN_VALIDATION_INPUT",
    val projectId: String,
    val sessionId: String,
    val turnId: String,
    val contextId: String,
    val contextHash: String,
    val turnNumber: Int,
    val baseStateVersion: Int,
    val resultId: String,
    val resultCanonicalSha256: String,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val physicalStateVersion: Int? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val physicalSnapshotCanonicalSha256: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val physicalArchetypeRegistryCanonicalSha256: String? = null,
    val physicalArchetypes: List<PhysicalArchetype>,
    val physicalSnapshot: PhysicalWorldSnapshot,
    val allowedPhysicalActionTypes: List<PhysicalActionType>,
    val allowedPhysicalActorIds: List<String>,
    val allowedPhysicalTargetIds: List<String>,
)

@Serializable
data class FormalTurnPhysicalGateCase(
    val caseId: String,
    val context: ContextPacket,
    val result: AiTurnResult,
    val physicalContext: FormalTurnPhysicalValidationContext? = null,
    val expectedValid: Boolean,
    val expectedErrorCodes: List<String> = emptyList(),
)

@Serializable
data class FormalTurnPhysicalGateSuite(
    val schemaVersion: String = "1.0",
    val suiteId: String = "world-physics03-formal-turn-gate-v1",
    val boundary: String = "NON_CANON_EVALUATION",
    val cases: List<FormalTurnPhysicalGateCase>,
)

@Serializable
data class FormalTurnPhysicalGateOutcome(
    val caseId: String,
    val matchedExpectation: Boolean,
    val actualValid: Boolean,
    val actualErrorCodes: List<String>,
)

@Serializable
data class FormalTurnPhysicalGateTechnicalReport(
    val schemaVersion: String = "1.0",
    val suiteId: String,
    val caseCount: Int,
    val expectationMatchCount: Int,
    val expectationMismatchCount: Int,
    val outcomes: List<FormalTurnPhysicalGateOutcome>,
    val technicalDecision: String,
    val transactionBoundaryStatus: String,
    val physicalStatePersistenceStatus: String,
    val claimBoundary: List<String>,
)

@Serializable
data class FormalPhysicalStateRecord(
    val schemaVersion: String = "2.0",
    val boundary: String = "FORMAL_TURN_RUNTIME_NON_CANON",
    val projectId: String,
    val sessionId: String,
    val physicalStateVersion: Int,
    val physicalArchetypeRegistryCanonicalSha256: String,
    val physicalSnapshotCanonicalSha256: String,
    val physicalSnapshot: PhysicalWorldSnapshot,
    val sourceContextId: String,
    val lastTurnId: String,
    val lastResultId: String,
)

@Serializable
data class FormalPhysicalStateView(
    val schemaVersion: String = "2.0",
    val boundary: String = "FORMAL_TURN_RUNTIME_NON_CANON",
    val status: String,
    val sessionId: String,
    val transitionCount: Int,
    val state: FormalPhysicalStateRecord? = null,
)

@Serializable
data class FormalPhysicalStateTransition(
    val actionId: String,
    val actionType: PhysicalActionType,
    val beforeStateVersion: Int,
    val afterStateVersion: Int,
    val beforeSnapshotCanonicalSha256: String,
    val afterSnapshotCanonicalSha256: String,
    val proposal: PhysicalActionProposal,
    val appliedEffects: List<String>,
    val afterSnapshot: PhysicalWorldSnapshot,
)

@Serializable
data class FormalPhysicalStateTransitionPlan(
    val schemaVersion: String = "2.0",
    val boundary: String = "FORMAL_TURN_RUNTIME_NON_CANON",
    val projectId: String,
    val sessionId: String,
    val startStateVersion: Int,
    val finalStateVersion: Int,
    val physicalArchetypeRegistryCanonicalSha256: String,
    val startSnapshotCanonicalSha256: String,
    val finalSnapshotCanonicalSha256: String,
    val transitions: List<FormalPhysicalStateTransition>,
    val finalSnapshot: PhysicalWorldSnapshot,
)
