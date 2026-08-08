package com.localnovel.engine

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.FormalTurnPhysicalValidationContext
import com.localnovel.engine.model.PhysicalActionProposal
import com.localnovel.engine.model.PhysicalActionType
import com.localnovel.engine.model.PhysicalVector3Cm
import com.localnovel.engine.service.FormalPhysicalStateHash
import com.localnovel.engine.service.FormalTurnPhysicalGateValidator
import com.localnovel.engine.service.PhysicalAffordanceFixtureFactory
import com.localnovel.engine.service.PhysicalStateReducer
import kotlin.test.Test
import kotlin.test.assertEquals

class PhysicalAttachedRelationContinuityTest {
    @Test
    fun `held object follows holder through a passage without breaking relation`() {
        val suite = PhysicalAffordanceFixtureFactory.createSuite()
        val snapshot = suite.snapshots.single { it.snapshotId == "snapshot-holding-light" }.copy(
            snapshotId = "wp05-attached-passage-v0",
            boundary = FormalTurnPhysicalGateValidator.FORMAL_TURN_BOUNDARY,
        )
        val result = AiTurnResult(
            protocolVersion = "2",
            projectId = "wp05",
            turnId = "wp05-attached-turn",
            resultId = "wp05-attached-result",
            contextId = "wp05-attached-context",
            sessionId = "wp05-attached-session",
            turnNumber = 1,
            baseStateVersion = 0,
            contextHash = "wp05-attached-context-hash",
            narrative = "The holder passes through the opening with the held object.",
            turnSummary = "Attached relation continuity test.",
            physicalActionProposals = listOf(
                PhysicalActionProposal(
                    actionId = "wp05-pass-with-held-tool",
                    actionType = PhysicalActionType.PASS_THROUGH,
                    actorId = "adult",
                    targetId = "wide-door",
                    destinationCenterCm = PhysicalVector3Cm(160.0, 0.0, 87.5),
                ),
            ),
        )
        val context = FormalTurnPhysicalValidationContext(
            schemaVersion = PhysicalStateReducer.SCHEMA_VERSION,
            projectId = result.projectId,
            sessionId = result.sessionId,
            turnId = result.turnId,
            contextId = result.contextId,
            contextHash = result.contextHash,
            turnNumber = result.turnNumber,
            baseStateVersion = result.baseStateVersion,
            resultId = result.resultId,
            resultCanonicalSha256 = FormalTurnPhysicalGateValidator().canonicalResultSha256(result),
            physicalStateVersion = 0,
            physicalSnapshotCanonicalSha256 = FormalPhysicalStateHash.snapshot(snapshot),
            physicalArchetypeRegistryCanonicalSha256 = FormalPhysicalStateHash.archetypeRegistry(suite.archetypes),
            physicalArchetypes = suite.archetypes,
            physicalSnapshot = snapshot,
            allowedPhysicalActionTypes = listOf(PhysicalActionType.PASS_THROUGH),
            allowedPhysicalActorIds = listOf("adult"),
            allowedPhysicalTargetIds = listOf("wide-door"),
        )

        val finalSnapshot = PhysicalStateReducer().plan(context, result).finalSnapshot
        val actor = finalSnapshot.entities.single { it.entityId == "adult" }
        val held = finalSnapshot.entities.single { it.entityId == "light-crate" }

        assertEquals(PhysicalVector3Cm(160.0, 0.0, 87.5), actor.centerCm)
        assertEquals("adult", held.heldByEntityId)
        assertEquals(PhysicalVector3Cm(180.0, 0.0, 120.0), held.centerCm)
    }
}
