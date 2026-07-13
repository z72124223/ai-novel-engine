package com.localnovel.engine.service

import com.localnovel.engine.model.FormalTurnPhysicalGateOutcome
import com.localnovel.engine.model.FormalTurnPhysicalGateSuite
import com.localnovel.engine.model.FormalTurnPhysicalGateTechnicalReport

class FormalTurnPhysicalGateEvaluationService(
    private val validator: FormalTurnPhysicalGateValidator = FormalTurnPhysicalGateValidator(),
) {
    fun evaluate(suite: FormalTurnPhysicalGateSuite): FormalTurnPhysicalGateTechnicalReport {
        val outcomes = suite.cases.map { case ->
            val actualCodes = validator.validate(case.context, case.result, case.physicalContext)
                .map { it.code }
                .distinct()
                .sorted()
            val expectedCodes = case.expectedErrorCodes.distinct().sorted()
            val actualValid = actualCodes.isEmpty()
            FormalTurnPhysicalGateOutcome(
                caseId = case.caseId,
                matchedExpectation = actualValid == case.expectedValid && actualCodes == expectedCodes,
                actualValid = actualValid,
                actualErrorCodes = actualCodes,
            )
        }
        val matches = outcomes.count { it.matchedExpectation }
        return FormalTurnPhysicalGateTechnicalReport(
            suiteId = suite.suiteId,
            caseCount = outcomes.size,
            expectationMatchCount = matches,
            expectationMismatchCount = outcomes.size - matches,
            outcomes = outcomes,
            technicalDecision = if (matches == outcomes.size) {
                "FORMAL_TURN_PHYSICAL_GATE_PASS_STATEFUL_ACTIONS_BLOCKED"
            } else {
                "FORMAL_TURN_PHYSICAL_GATE_FAIL"
            },
            transactionBoundaryStatus = "VALIDATES_BEFORE_RESOLVE_PENDING_TURN",
            physicalStatePersistenceStatus = "NOT_IMPLEMENTED_STATEFUL_ACTIONS_FAIL_CLOSED",
            claimBoundary = listOf(
                "Only declared structured physical proposals are validated.",
                "A V1 pass does not prove complete real-world physical realism.",
                "No physical relation is persisted and no Canon measurement is introduced.",
            ),
        )
    }
}
