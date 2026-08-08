package com.localnovel.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class NpcActorSafetyLoopInput(
    val schemaVersion: String = "1.0",
    val loopId: String,
    val boundary: String = "NON_CANON_EVALUATION",
    val prompt: NpcActorPhysicalPrompt,
    val engineContext: NpcActorPhysicalEngineContext,
    val responseAttempts: List<NpcActorPhysicalResponse>,
    val maxAttempts: Int = 2,
    val maxElapsedMinutes: Int = 10,
)

@Serializable
data class NpcActorSafetyRepairRequest(
    val schemaVersion: String = "1.0",
    val loopId: String,
    val requestId: String,
    val failedAttemptNumber: Int,
    val nextAttemptNumber: Int,
    val maxAttempts: Int,
    val rejectionStage: String,
    val rejectionCodes: List<String>,
    val actorId: String,
    val targetIds: List<String>,
    val locationId: String,
    val allowedEvidenceIds: List<String>,
    val allowedPhysicalActionTypes: List<PhysicalActionType>,
    val visiblePhysicalTargetIds: List<String>,
    val repairInstructions: List<String>,
    val engineOwnedContextIncluded: Boolean = false,
)

@Serializable
data class NpcActorSafetyLoopAttemptResult(
    val attemptNumber: Int,
    val requestId: String,
    val bindingAccepted: Boolean,
    val bindingStatus: String,
    val gameMasterAccepted: Boolean? = null,
    val gameMasterStatus: String? = null,
    val timeBudgetAccepted: Boolean? = null,
    val elapsedMinutes: Int = 0,
    val rejectionStage: String? = null,
    val findings: List<String> = emptyList(),
    val repairRequest: NpcActorSafetyRepairRequest? = null,
)

@Serializable
data class NpcActorSafetyLoopResult(
    val schemaVersion: String = "1.0",
    val loopId: String,
    val boundary: String = "NON_CANON_EVALUATION",
    val accepted: Boolean,
    val status: String,
    val attemptsProcessed: Int,
    val unusedResponseAttemptCount: Int = 0,
    val acceptedAttemptNumber: Int? = null,
    val attemptResults: List<NpcActorSafetyLoopAttemptResult>,
    val acceptedProposal: NpcActionProposal? = null,
    val formalStateWritePerformed: Boolean = false,
    val claimBoundary: List<String>,
)

@Serializable
data class NpcActorSafetyLoopCaseReference(
    val caseId: String,
    val title: String,
    val category: String,
    val contextFile: PhysicalEvidenceFile,
    val inputFile: PhysicalEvidenceFile,
    val expectedStatus: String,
    val expectedAcceptedAttemptNumber: Int? = null,
    val expectedAttemptStages: List<String>,
    val expectedRepairRequestCount: Int,
)

@Serializable
data class NpcActorSafetyLoopSuite(
    val schemaVersion: String = "1.0",
    val suiteId: String = "npc-actor05-offline-safety-loop-v1",
    val boundary: String = "NON_CANON_EVALUATION",
    val fixtureOrigin: String = "MANUALLY_AUTHORED_OFFLINE_SAFETY_FIXTURE",
    val cases: List<NpcActorSafetyLoopCaseReference>,
)

@Serializable
data class NpcActorSafetyLoopCaseOutcome(
    val caseId: String,
    val matchedExpectation: Boolean,
    val actualStatus: String,
    val actualAcceptedAttemptNumber: Int? = null,
    val actualAttemptStages: List<String>,
    val actualRepairRequestCount: Int,
    val findings: List<String> = emptyList(),
)

@Serializable
data class NpcActorSafetyLoopTechnicalReport(
    val schemaVersion: String = "1.0",
    val suiteId: String,
    val caseCount: Int,
    val expectationMatchCount: Int,
    val expectationMismatchCount: Int,
    val acceptedCaseCount: Int,
    val rejectedCaseCount: Int,
    val repairRequiredCaseCount: Int,
    val outcomes: List<NpcActorSafetyLoopCaseOutcome>,
    val technicalDecision: String,
    val stateMutationPerformed: Boolean = false,
    val liveModelStatus: String = "NOT_CONNECTED_AUTHORIZATION_REQUIRED",
    val claimBoundary: List<String>,
)
