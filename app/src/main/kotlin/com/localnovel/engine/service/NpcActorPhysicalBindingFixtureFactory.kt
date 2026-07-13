package com.localnovel.engine.service

import com.localnovel.engine.model.NpcActorDecisionSummary
import com.localnovel.engine.model.NpcActorOutputConstraints
import com.localnovel.engine.model.NpcActorPhysicalBindingCase
import com.localnovel.engine.model.NpcActorPhysicalBindingSuite
import com.localnovel.engine.model.NpcActorRequest
import com.localnovel.engine.model.NpcActorResponse
import com.localnovel.engine.model.NpcVoiceProfile
import com.localnovel.engine.model.PhysicalActionProposal
import com.localnovel.engine.model.PhysicalActionType

object NpcActorPhysicalBindingFixtureFactory {
    fun createSuite(): NpcActorPhysicalBindingSuite {
        val physicalSuite = PhysicalAffordanceFixtureFactory.createSuite()
        val sourceCases = physicalSuite.cases.associateBy { it.caseId }

        fun source(caseId: String) = sourceCases[caseId] ?: error("Missing physical source case: $caseId")
        fun bindingCase(
            caseId: String,
            sourceCaseId: String,
            allowedActions: List<PhysicalActionType>? = null,
            visibleTargets: List<String>? = null,
            physicalProposalTransform: (PhysicalActionProposal) -> PhysicalActionProposal = { it },
            actorResponseTransform: (NpcActorResponse) -> NpcActorResponse = { it },
            expectedAccepted: Boolean,
            expectedStatus: String,
            expectedFindings: List<String> = emptyList(),
            useSourcePhysicalExpectation: Boolean = false,
        ): NpcActorPhysicalBindingCase {
            val sourceCase = source(sourceCaseId)
            val proposal = physicalProposalTransform(
                sourceCase.proposal.copy(actionId = "wp02-$caseId"),
            )
            val request = actorRequest(caseId, sourceCase.proposal.actorId)
            val response = actorResponseTransform(actorResponse(request, caseId))
            return NpcActorPhysicalBindingCase(
                caseId = caseId,
                snapshotId = sourceCase.snapshotId,
                actorRequest = request,
                actorResponse = response,
                physicalActionProposal = proposal,
                allowedPhysicalActionTypes = allowedActions ?: listOf(sourceCase.proposal.actionType),
                visiblePhysicalTargetIds = visibleTargets ?: listOfNotNull(
                    sourceCase.proposal.targetId,
                    sourceCase.proposal.secondaryTargetId,
                ).distinct(),
                expectedAccepted = expectedAccepted,
                expectedStatus = expectedStatus,
                expectedFindings = expectedFindings,
                expectedPhysicalRejectionCodes = if (useSourcePhysicalExpectation) {
                    sourceCase.expectedRejectionCodes
                } else {
                    emptyList()
                },
                expectedRequiredEffects = if (useSourcePhysicalExpectation) {
                    sourceCase.expectedRequiredEffects
                } else {
                    emptyList()
                },
            )
        }

        val cases = listOf(
            bindingCase(
                caseId = "binding-01-chair-support-accepted",
                sourceCaseId = "support-01-chair-assist",
                expectedAccepted = true,
                expectedStatus = NpcActorPhysicalBindingValidator.ACCEPTED_FOR_GAME_MASTER_REVIEW_PHYSICALLY_VALIDATED,
                useSourcePhysicalExpectation = true,
            ),
            bindingCase(
                caseId = "binding-02-dog-shoulder-rejected",
                sourceCaseId = "support-02-dog-shoulder-adult",
                expectedAccepted = false,
                expectedStatus = NpcActorPhysicalBindingValidator.REJECTED_PHYSICAL_AFFORDANCE,
                useSourcePhysicalExpectation = true,
            ),
            bindingCase(
                caseId = "binding-03-reach-through-wall-rejected",
                sourceCaseId = "reach-04-wall",
                expectedAccepted = false,
                expectedStatus = NpcActorPhysicalBindingValidator.REJECTED_PHYSICAL_AFFORDANCE,
                useSourcePhysicalExpectation = true,
            ),
            bindingCase(
                caseId = "binding-04-drop-effects-accepted",
                sourceCaseId = "drop-01-held-above-ground",
                expectedAccepted = true,
                expectedStatus = NpcActorPhysicalBindingValidator.ACCEPTED_FOR_GAME_MASTER_REVIEW_PHYSICALLY_VALIDATED,
                useSourcePhysicalExpectation = true,
            ),
            bindingCase(
                caseId = "binding-05-action-not-allowlisted",
                sourceCaseId = "support-01-chair-assist",
                allowedActions = listOf(PhysicalActionType.REACH),
                expectedAccepted = false,
                expectedStatus = NpcActorPhysicalBindingValidator.REJECTED_PHYSICAL_BINDING_CONTRACT,
                expectedFindings = listOf("binding:physical_action_not_allowlisted"),
            ),
            bindingCase(
                caseId = "binding-06-target-not-bound",
                sourceCaseId = "support-01-chair-assist",
                visibleTargets = listOf("dog"),
                expectedAccepted = false,
                expectedStatus = NpcActorPhysicalBindingValidator.REJECTED_PHYSICAL_BINDING_CONTRACT,
                expectedFindings = listOf("binding:physical_target_not_bound"),
            ),
            bindingCase(
                caseId = "binding-07-physical-actor-mismatch",
                sourceCaseId = "support-01-chair-assist",
                physicalProposalTransform = { it.copy(actorId = "child") },
                expectedAccepted = false,
                expectedStatus = NpcActorPhysicalBindingValidator.REJECTED_PHYSICAL_BINDING_CONTRACT,
                expectedFindings = listOf("binding:physical_actor_mismatch"),
            ),
            bindingCase(
                caseId = "binding-08-actor-contract-fails-first",
                sourceCaseId = "support-01-chair-assist",
                actorResponseTransform = { it.copy(requestId = "wrong-request-id") },
                expectedAccepted = false,
                expectedStatus = NpcActorPhysicalBindingValidator.REJECTED_ACTOR_CONTRACT,
                expectedFindings = listOf("actor:request_id_mismatch"),
            ),
        )

        return NpcActorPhysicalBindingSuite(
            physicalArchetypes = physicalSuite.archetypes,
            physicalSnapshots = physicalSuite.snapshots.filter { snapshot ->
                snapshot.snapshotId in cases.map { it.snapshotId }.toSet()
            },
            cases = cases,
        )
    }

    private fun actorRequest(caseId: String, actorId: String): NpcActorRequest = NpcActorRequest(
        requestId = "wp02-request-$caseId",
        scenarioId = "wp02-scenario-$caseId",
        actorId = actorId,
        locationId = "non-canon-physical-lab",
        voiceProfile = NpcVoiceProfile(
            principles = listOf("Act only on visible evidence."),
            forbiddenPatterns = listOf("author-only"),
        ),
        physicalState = "stable unless the engine-owned snapshot proves otherwise",
        emotionalState = "focused",
        outputConstraints = NpcActorOutputConstraints(maxUtteranceChars = 48),
    )

    private fun actorResponse(
        request: NpcActorRequest,
        caseId: String,
    ): NpcActorResponse = NpcActorResponse(
        requestId = request.requestId,
        actorId = request.actorId,
        targetIds = request.targetIds,
        speechAct = "NONVERBAL",
        immediateGoal = "Complete one bounded visible action.",
        emotionShown = "focused",
        emotionHidden = "cautious",
        actionType = "inspect_local_pressure",
        actionIntent = "Attempt the structured physical action for $caseId.",
        visibleAction = "Begins the proposed physical action.",
        utterance = "",
        nonverbalAction = "Tests the action without claiming its result.",
        predictedCost = "Engine validation required.",
        observableConsequence = "No state changes before Game Master review.",
        confidence = 70,
        decisionSummary = NpcActorDecisionSummary(
            selectedActionLabel = "bounded_physical_action",
        ),
    )
}
