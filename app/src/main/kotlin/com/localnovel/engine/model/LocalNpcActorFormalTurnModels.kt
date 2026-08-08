package com.localnovel.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class LocalNpcActorFormalTurnInput(
    val schemaVersion: String = "1.0",
    val boundary: String = "FORMAL_RUNTIME_CANDIDATE_INPUT",
    val preparationId: String,
    val sessionId: String,
    val playerInput: String,
    val profile: NpcAutonomyProfile,
    val targetIds: List<String> = emptyList(),
    val physicalContext: NpcActorPhysicalEngineContext,
    val outputConstraints: NpcActorOutputConstraints = NpcActorOutputConstraints(),
    val maxAttempts: Int = 2,
    val maxElapsedMinutes: Int = 10,
)

@Serializable
data class LocalNpcActorFormalStateEvidence(
    val databaseSha256: String,
    val sessionStateVersion: Int,
    val nextTurnNumber: Int,
    val appliedTurnCount: Int,
    val turnLogCount: Int,
    val eventCount: Int,
    val memoryCount: Int,
    val knowledgeCount: Int,
    val formalPhysicalTransitionCount: Int,
    val formalPhysicalStateVersion: Int? = null,
    val pendingContextPresent: Boolean,
)

@Serializable
data class LocalNpcActorFormalTurnCandidatePayload(
    val schemaVersion: String = "1.0",
    val boundary: String = "FORMAL_RUNTIME_NON_CANON_CANDIDATE",
    val candidateId: String,
    val preparationId: String,
    val projectId: String,
    val sessionId: String,
    val turnId: String,
    val contextId: String,
    val contextHash: String,
    val turnNumber: Int,
    val baseStateVersion: Int,
    val resultId: String,
    val resultCanonicalSha256: String,
    val actorRequest: NpcActorRequest,
    val visibleSummary: LocalNpcActorVisibleSummary,
    val selectedResponse: NpcActorPhysicalResponse,
    val acceptedProposal: NpcActionProposal,
    val localModelReport: LocalNpcActorSafetyLoopReport,
    val aiTurnResult: AiTurnResult,
    val formalPhysicalContext: FormalTurnPhysicalValidationContext,
    val npcDecisionTrace: NpcDecisionTrace,
    val gameMasterResolution: GameMasterResolution,
    val preparationBeforeState: LocalNpcActorFormalStateEvidence,
    val preparationAfterState: LocalNpcActorFormalStateEvidence,
)

@Serializable
data class LocalNpcActorFormalTurnCandidatePackage(
    val schemaVersion: String = "1.0",
    val status: String = "HUMAN_APPROVAL_REQUIRED",
    val candidateSha256: String,
    val payload: LocalNpcActorFormalTurnCandidatePayload,
    val claimBoundary: List<String> = listOf(
        "Package authorization is not candidate approval.",
        "The model candidate cannot write formal state by itself.",
        "Only an exact-bound human decision may request the existing resolve-turn transaction.",
        "This candidate is non-Canon and cannot author Memory or Knowledge writes.",
    ),
)

@Serializable
enum class LocalNpcActorFormalDecisionType {
    APPROVE_NON_CANON_FORMAL_TURN,
    REJECT_NON_CANON_FORMAL_TURN,
}

@Serializable
data class LocalNpcActorFormalTurnDecision(
    val schemaVersion: String = "1.0",
    val boundary: String = "HUMAN_APPROVAL_DECISION",
    val decisionId: String,
    val decision: LocalNpcActorFormalDecisionType,
    val candidateId: String,
    val candidateSha256: String,
    val projectId: String,
    val sessionId: String,
    val turnId: String,
    val contextId: String,
    val contextHash: String,
    val turnNumber: Int,
    val baseStateVersion: Int,
    val resultId: String,
    val resultCanonicalSha256: String,
    val reviewerId: String,
    val reviewedAt: String,
    val acknowledgement: String,
    val note: String = "",
    val testOnly: Boolean = false,
)

@Serializable
data class LocalNpcActorFormalResolveReport(
    val schemaVersion: String = "1.0",
    val boundary: String = "FORMAL_RUNTIME_NON_CANON_RESOLVE_REPORT",
    val status: String,
    val candidateId: String,
    val candidateSha256: String,
    val decisionId: String,
    val decisionSha256: String,
    val decision: LocalNpcActorFormalDecisionType,
    val projectId: String,
    val sessionId: String,
    val turnId: String,
    val contextId: String,
    val turnNumber: Int,
    val resultId: String,
    val beforeState: LocalNpcActorFormalStateEvidence,
    val afterState: LocalNpcActorFormalStateEvidence,
    val formalTurnApplied: Boolean,
    val pendingContextCleared: Boolean,
    val testOnlyApproval: Boolean,
    val claimBoundary: List<String> = listOf(
        "The applied turn remains non-Canon.",
        "The local model did not write SQLite directly.",
        "The existing resolve-turn validators and transaction remained authoritative.",
        "A test-only approval is engineering evidence, not Product Owner content approval.",
    ),
)
