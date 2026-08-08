package com.localnovel.engine.service

import com.localnovel.engine.model.NpcActorPhysicalBindingOutcome
import com.localnovel.engine.model.NpcActorPhysicalBindingSuite
import com.localnovel.engine.model.NpcActorPhysicalBindingTechnicalReport
import com.localnovel.engine.model.NpcActorPhysicalResponse

class NpcActorPhysicalBindingEvaluationService(
    private val validator: NpcActorPhysicalBindingValidator = NpcActorPhysicalBindingValidator(),
    private val requestBuilder: NpcActorPhysicalBindingRequestBuilder = NpcActorPhysicalBindingRequestBuilder(),
) {
    fun evaluate(suite: NpcActorPhysicalBindingSuite): NpcActorPhysicalBindingTechnicalReport {
        require(suite.boundary == NON_CANON_EVALUATION) {
            "NPC physical binding evidence must remain NON_CANON_EVALUATION."
        }
        val snapshots = suite.physicalSnapshots.associateBy { it.snapshotId }
        val outcomes = suite.cases.map { case ->
            val snapshot = snapshots[case.snapshotId]
                ?: error("Missing binding snapshot: ${case.snapshotId}")
            val input = requestBuilder.build(
                actorRequest = case.actorRequest,
                physicalArchetypes = suite.physicalArchetypes,
                physicalSnapshot = snapshot,
                allowedPhysicalActionTypes = case.allowedPhysicalActionTypes,
                visiblePhysicalTargetIds = case.visiblePhysicalTargetIds,
            )
            val result = validator.validateAndMap(
                prompt = input.prompt,
                engineContext = input.engineContext,
                response = NpcActorPhysicalResponse(
                    requestId = case.actorRequest.requestId,
                    actorResponse = case.actorResponse,
                    physicalActionProposal = case.physicalActionProposal,
                ),
            )
            val physicalCodes = result.physicalValidation?.rejectionCodes.orEmpty()
            val requiredEffects = result.physicalValidation?.requiredEffects.orEmpty()
            val matched = result.accepted == case.expectedAccepted &&
                result.status == case.expectedStatus &&
                case.expectedFindings.all { it in result.findings } &&
                physicalCodes == case.expectedPhysicalRejectionCodes.sorted() &&
                requiredEffects == case.expectedRequiredEffects
            NpcActorPhysicalBindingOutcome(
                caseId = case.caseId,
                matchedExpectation = matched,
                actualAccepted = result.accepted,
                actualStatus = result.status,
                actualFindings = result.findings,
                actualPhysicalRejectionCodes = physicalCodes,
                actualRequiredEffects = requiredEffects,
            )
        }
        val matched = outcomes.count { it.matchedExpectation }
        return NpcActorPhysicalBindingTechnicalReport(
            suiteId = suite.suiteId,
            caseCount = outcomes.size,
            expectationMatchCount = matched,
            expectationMismatchCount = outcomes.size - matched,
            acceptedCaseCount = outcomes.count { it.actualAccepted },
            rejectedCaseCount = outcomes.count { !it.actualAccepted },
            outcomes = outcomes,
            technicalDecision = if (matched == outcomes.size) {
                "NPC_ACTION_BINDING_TECHNICAL_PASS_FORMAL_RUNTIME_NOT_CONNECTED"
            } else {
                "NPC_ACTION_BINDING_TECHNICAL_FAIL"
            },
            claimBoundary = listOf(
                "The committed offline cases are reproducible.",
                "A pass means only that no implemented V1 rule rejected the action.",
                "Formal turn validation, SQLite, Canon and live model execution remain disconnected.",
            ),
        )
    }

    private companion object {
        const val NON_CANON_EVALUATION = "NON_CANON_EVALUATION"
    }
}
