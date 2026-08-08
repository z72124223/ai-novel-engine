package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.FormalTurnPhysicalGateCase
import com.localnovel.engine.model.FormalTurnPhysicalGateSuite
import com.localnovel.engine.model.FormalTurnPhysicalValidationContext
import com.localnovel.engine.model.PhysicalActionProposal
import com.localnovel.engine.model.PhysicalActionType
import com.localnovel.engine.model.Project

object FormalTurnPhysicalGateFixtureFactory {
    fun createSuite(): FormalTurnPhysicalGateSuite {
        val physicalSuite = PhysicalAffordanceFixtureFactory.createSuite()
        val sourceCases = physicalSuite.cases.associateBy { it.caseId }
        val sourceSnapshots = physicalSuite.snapshots.associateBy { it.snapshotId }
        val gate = FormalTurnPhysicalGateValidator()
        val context = ContextPacket(
            project = Project("wp03", "WORLD-PHYSICS-03", "Non-Canon formal gate fixture"),
            turnId = "wp03-turn-1",
            turnNumber = 1,
            playerInput = "Perform one declared physical action.",
            activeScene = null,
            sceneContract = null,
            contextId = "wp03-context-1",
            sessionId = "wp03-session-1",
            baseStateVersion = 0,
            contextHash = "wp03-context-hash-v1",
        )

        fun source(caseId: String) = sourceCases[caseId] ?: error("Missing physical source case: $caseId")
        fun buildResult(caseId: String, proposal: PhysicalActionProposal?) = AiTurnResult(
            protocolVersion = "2",
            projectId = context.project.id,
            turnId = context.turnId,
            narrative = "A declared physical action is evaluated before formal state writes.",
            resultId = "wp03-result-$caseId",
            contextId = context.contextId,
            sessionId = context.sessionId,
            turnNumber = context.turnNumber,
            baseStateVersion = context.baseStateVersion,
            contextHash = context.contextHash,
            turnSummary = "Formal physical gate fixture $caseId.",
            physicalActionProposals = listOfNotNull(proposal),
        )

        fun buildPhysicalContext(
            result: AiTurnResult,
            sourceCaseId: String,
            allowedActions: List<PhysicalActionType>? = null,
        ): FormalTurnPhysicalValidationContext {
            val sourceCase = source(sourceCaseId)
            val sourceSnapshot = sourceSnapshots[sourceCase.snapshotId]
                ?: error("Missing physical snapshot: ${sourceCase.snapshotId}")
            val snapshot = sourceSnapshot.copy(
                snapshotId = "wp03-${result.resultId}-snapshot",
                boundary = FormalTurnPhysicalGateValidator.FORMAL_TURN_BOUNDARY,
            )
            val requiredArchetypeIds = snapshot.entities.map { it.archetypeId }.toSet()
            return FormalTurnPhysicalValidationContext(
                projectId = context.project.id,
                sessionId = context.sessionId,
                turnId = context.turnId,
                contextId = context.contextId,
                contextHash = context.contextHash,
                turnNumber = context.turnNumber,
                baseStateVersion = context.baseStateVersion,
                resultId = result.resultId,
                resultCanonicalSha256 = gate.canonicalResultSha256(result),
                physicalArchetypes = physicalSuite.archetypes.filter { it.archetypeId in requiredArchetypeIds },
                physicalSnapshot = snapshot,
                allowedPhysicalActionTypes = allowedActions ?: result.physicalActionProposals.map { it.actionType }.distinct(),
                allowedPhysicalActorIds = result.physicalActionProposals.map { it.actorId }.distinct().ifEmpty {
                    listOf(sourceCase.proposal.actorId)
                },
                allowedPhysicalTargetIds = result.physicalActionProposals.flatMap {
                    listOfNotNull(it.targetId, it.secondaryTargetId)
                }.distinct().ifEmpty {
                    listOfNotNull(sourceCase.proposal.targetId, sourceCase.proposal.secondaryTargetId).distinct()
                },
            )
        }

        fun physicalCase(
            caseId: String,
            sourceCaseId: String,
            expectedValid: Boolean,
            expectedErrorCodes: List<String> = emptyList(),
            transformContext: (FormalTurnPhysicalValidationContext) -> FormalTurnPhysicalValidationContext = { it },
        ): FormalTurnPhysicalGateCase {
            val proposal = source(sourceCaseId).proposal.copy(actionId = "wp03-$caseId")
            val result = buildResult(caseId, proposal)
            return FormalTurnPhysicalGateCase(
                caseId = caseId,
                context = context,
                result = result,
                physicalContext = transformContext(buildPhysicalContext(result, sourceCaseId)),
                expectedValid = expectedValid,
                expectedErrorCodes = expectedErrorCodes,
            )
        }

        val chair = physicalCase("case-01-chair-support-pass", "support-01-chair-assist", true)
        val dog = physicalCase(
            "case-02-dog-shoulder-reject",
            "support-02-dog-shoulder-adult",
            false,
            listOf(FormalTurnPhysicalGateValidator.PHYSICAL_AFFORDANCE_REJECTED),
        )
        val missingContextProposal = source("support-01-chair-assist").proposal.copy(actionId = "wp03-case-03")
        val missingContextResult = buildResult("case-03-context-required", missingContextProposal)
        val missingContext = FormalTurnPhysicalGateCase(
            caseId = "case-03-context-required",
            context = context,
            result = missingContextResult,
            physicalContext = null,
            expectedValid = false,
            expectedErrorCodes = listOf(FormalTurnPhysicalGateValidator.PHYSICAL_CONTEXT_REQUIRED),
        )
        val noProposalResult = buildResult("case-04-proposal-required", null)
        val missingProposal = FormalTurnPhysicalGateCase(
            caseId = "case-04-proposal-required",
            context = context,
            result = noProposalResult,
            physicalContext = buildPhysicalContext(noProposalResult, "support-01-chair-assist"),
            expectedValid = false,
            expectedErrorCodes = listOf(FormalTurnPhysicalGateValidator.PHYSICAL_PROPOSAL_REQUIRED),
        )
        val contextMismatch = physicalCase(
            "case-05-context-binding-reject",
            "support-01-chair-assist",
            false,
            listOf(FormalTurnPhysicalGateValidator.PHYSICAL_CONTEXT_BINDING_MISMATCH),
        ) { it.copy(contextHash = "wrong-context-hash") }
        val resultHashMismatch = physicalCase(
            "case-06-result-hash-reject",
            "support-01-chair-assist",
            false,
            listOf(FormalTurnPhysicalGateValidator.PHYSICAL_RESULT_HASH_MISMATCH),
        ) { it.copy(resultCanonicalSha256 = "0".repeat(64)) }
        val nonAllowlisted = physicalCase(
            "case-07-action-allowlist-reject",
            "support-01-chair-assist",
            false,
            listOf(FormalTurnPhysicalGateValidator.PHYSICAL_ACTION_NOT_ALLOWLISTED),
        ) { it.copy(allowedPhysicalActionTypes = listOf(PhysicalActionType.REACH)) }
        val stateful = physicalCase(
            "case-08-stateful-drop-blocked",
            "drop-01-held-above-ground",
            false,
            listOf(FormalTurnPhysicalGateValidator.PHYSICAL_STATE_PERSISTENCE_UNAVAILABLE),
        )

        return FormalTurnPhysicalGateSuite(
            cases = listOf(
                chair,
                dog,
                missingContext,
                missingProposal,
                contextMismatch,
                resultHashMismatch,
                nonAllowlisted,
                stateful,
            ),
        )
    }
}
