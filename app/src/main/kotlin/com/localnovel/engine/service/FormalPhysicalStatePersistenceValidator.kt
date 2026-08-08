package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.FormalTurnPhysicalValidationContext
import com.localnovel.engine.model.ValidationError
import com.localnovel.engine.repository.SqliteStoryRepository

class FormalPhysicalStatePersistenceValidator(
    private val repository: SqliteStoryRepository,
) {
    fun validate(
        context: ContextPacket,
        result: AiTurnResult,
        physicalContext: FormalTurnPhysicalValidationContext?,
    ): List<ValidationError> {
        if (result.physicalActionProposals.none { it.actionType in PhysicalStateReducer.STATEFUL_ACTIONS }) return emptyList()
        if (physicalContext == null || physicalContext.schemaVersion != PhysicalStateReducer.SCHEMA_VERSION) return emptyList()

        val errors = mutableListOf<ValidationError>()
        val suppliedVersion = physicalContext.physicalStateVersion
        val suppliedSnapshotHash = physicalContext.physicalSnapshotCanonicalSha256
        val suppliedRegistryHash = physicalContext.physicalArchetypeRegistryCanonicalSha256
        val persisted = repository.getFormalPhysicalState(context.sessionId)
        if (persisted == null) {
            if (suppliedVersion != 0) {
                errors += ValidationError(
                    PHYSICAL_STATE_INITIAL_VERSION_REQUIRED,
                    "The first persisted physical turn must use physicalStateVersion 0.",
                )
            }
            return errors
        }

        if (persisted.projectId != context.project.id || persisted.sessionId != context.sessionId) {
            errors += ValidationError(PHYSICAL_PERSISTED_STATE_IDENTITY_MISMATCH, "Persisted physical state identity does not match the pending turn.")
        }
        if (suppliedVersion != persisted.physicalStateVersion) {
            errors += ValidationError(
                PHYSICAL_STATE_STALE_VERSION,
                "Persisted physical state version is ${persisted.physicalStateVersion}, supplied version is $suppliedVersion.",
            )
        }
        if (!persisted.physicalSnapshotCanonicalSha256.equals(suppliedSnapshotHash, ignoreCase = true)) {
            errors += ValidationError(PHYSICAL_STATE_STALE_SNAPSHOT, "Supplied physical snapshot Hash does not match persisted Session state.")
        }
        if (!persisted.physicalArchetypeRegistryCanonicalSha256.equals(suppliedRegistryHash, ignoreCase = true)) {
            errors += ValidationError(PHYSICAL_STATE_REGISTRY_MISMATCH, "Supplied physical archetype registry Hash does not match persisted Session state.")
        }
        return errors
    }

    companion object {
        const val PHYSICAL_STATE_INITIAL_VERSION_REQUIRED = "physical_state_initial_version_required"
        const val PHYSICAL_PERSISTED_STATE_IDENTITY_MISMATCH = "physical_persisted_state_identity_mismatch"
        const val PHYSICAL_STATE_STALE_VERSION = "physical_state_stale_version"
        const val PHYSICAL_STATE_STALE_SNAPSHOT = "physical_state_stale_snapshot"
        const val PHYSICAL_STATE_REGISTRY_MISMATCH = "physical_state_registry_mismatch"
    }
}
