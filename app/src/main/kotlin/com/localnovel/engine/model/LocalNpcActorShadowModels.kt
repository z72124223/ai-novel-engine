package com.localnovel.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class LocalNpcActorShadowInput(
    val schemaVersion: String = "1.0",
    val boundary: String = "FORMAL_RUNTIME_SHADOW_INPUT",
    val shadowRunId: String,
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
data class LocalNpcActorShadowStateEvidence(
    val databaseSha256: String,
    val sessionStateVersion: Int,
    val nextTurnNumber: Int,
    val appliedTurnCount: Int,
    val turnLogCount: Int,
    val eventCount: Int,
    val formalPhysicalTransitionCount: Int,
    val formalPhysicalStateVersion: Int? = null,
    val previewContextPersisted: Boolean = false,
)

@Serializable
data class LocalNpcActorVisibleSummary(
    val actorId: String,
    val targetIds: List<String>,
    val locationId: String,
    val playerIsElsewhere: Boolean,
    val perceivedEventIds: List<String>,
    val ownedMemoryIds: List<String>,
    val ownedBeliefIds: List<String>,
    val capabilityIds: List<String>,
    val recentDialogueEvidenceIds: List<String>,
)

@Serializable
data class LocalNpcActorShadowReport(
    val schemaVersion: String = "1.0",
    val boundary: String = "FORMAL_RUNTIME_SHADOW_REPORT",
    val shadowRunId: String,
    val projectId: String,
    val sessionId: String,
    val turnNumber: Int,
    val baseStateVersion: Int,
    val contextId: String,
    val contextHash: String,
    val actorRequest: NpcActorRequest,
    val visibleSummary: LocalNpcActorVisibleSummary,
    val localModelReport: LocalNpcActorSafetyLoopReport,
    val beforeState: LocalNpcActorShadowStateEvidence,
    val afterState: LocalNpcActorShadowStateEvidence,
    val technicalDecision: String,
    val formalStateWritePerformed: Boolean = false,
    val claimBoundary: List<String> = listOf(
        "The local model received only the actor-visible request and bounded physical allowlists.",
        "The formal Session was previewed without creating a pending context.",
        "No SQLite, Canon, Memory, Knowledge, event, turn or physical state was written.",
        "An accepted result remains an untrusted candidate and was not passed to resolve-turn.",
    ),
)
