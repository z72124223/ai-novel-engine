package com.localnovel.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class NpcActorPhysicalPrompt(
    val schemaVersion: String = "1.0",
    val boundary: String = "NON_CANON_EVALUATION",
    val actorRequest: NpcActorRequest,
    val allowedPhysicalActionTypes: List<PhysicalActionType>,
    val visiblePhysicalTargetIds: List<String>,
)

@Serializable
data class NpcActorPhysicalEngineContext(
    val schemaVersion: String = "1.0",
    val boundary: String = "NON_CANON_EVALUATION",
    val requestId: String,
    val physicalArchetypes: List<PhysicalArchetype>,
    val physicalSnapshot: PhysicalWorldSnapshot,
    val allowedPhysicalActionTypes: List<PhysicalActionType>,
    val allowedPhysicalTargetIds: List<String>,
)

data class NpcActorPhysicalBindingInput(
    val prompt: NpcActorPhysicalPrompt,
    val engineContext: NpcActorPhysicalEngineContext,
)

@Serializable
data class NpcActorPhysicalResponse(
    val schemaVersion: String = "1.0",
    val requestId: String,
    val actorResponse: NpcActorResponse,
    val physicalActionProposal: PhysicalActionProposal,
)

@Serializable
data class NpcActorPhysicalBindingResult(
    val accepted: Boolean,
    val status: String,
    val findings: List<String> = emptyList(),
    val actorValidation: NpcActorValidationResult,
    val physicalValidation: PhysicalAffordanceValidationResult? = null,
    val proposal: NpcActionProposal? = null,
)

@Serializable
data class NpcActorPhysicalBindingCase(
    val caseId: String,
    val snapshotId: String,
    val actorRequest: NpcActorRequest,
    val actorResponse: NpcActorResponse,
    val physicalActionProposal: PhysicalActionProposal,
    val allowedPhysicalActionTypes: List<PhysicalActionType>,
    val visiblePhysicalTargetIds: List<String>,
    val expectedAccepted: Boolean,
    val expectedStatus: String,
    val expectedFindings: List<String> = emptyList(),
    val expectedPhysicalRejectionCodes: List<String> = emptyList(),
    val expectedRequiredEffects: List<String> = emptyList(),
)

@Serializable
data class NpcActorPhysicalBindingSuite(
    val schemaVersion: String = "1.0",
    val suiteId: String = "world-physics02-npc-action-binding-v1",
    val boundary: String = "NON_CANON_EVALUATION",
    val physicalArchetypes: List<PhysicalArchetype>,
    val physicalSnapshots: List<PhysicalWorldSnapshot>,
    val cases: List<NpcActorPhysicalBindingCase>,
)

@Serializable
data class NpcActorPhysicalBindingOutcome(
    val caseId: String,
    val matchedExpectation: Boolean,
    val actualAccepted: Boolean,
    val actualStatus: String,
    val actualFindings: List<String>,
    val actualPhysicalRejectionCodes: List<String>,
    val actualRequiredEffects: List<String>,
)

@Serializable
data class NpcActorPhysicalBindingTechnicalReport(
    val schemaVersion: String = "1.0",
    val suiteId: String,
    val caseCount: Int,
    val expectationMatchCount: Int,
    val expectationMismatchCount: Int,
    val acceptedCaseCount: Int,
    val rejectedCaseCount: Int,
    val outcomes: List<NpcActorPhysicalBindingOutcome>,
    val technicalDecision: String,
    val formalTurnRuntimeStatus: String = "NOT_CONNECTED",
    val claimBoundary: List<String>,
)
