package com.localnovel.engine.service

import com.localnovel.engine.model.PhysicalActionCoverage
import com.localnovel.engine.model.PhysicalActionType
import com.localnovel.engine.model.PhysicalAffordanceCaseOutcome
import com.localnovel.engine.model.PhysicalAffordanceSuite
import com.localnovel.engine.model.PhysicalAffordanceTechnicalReport
import kotlinx.serialization.json.Json

object PhysicalAffordanceJson {
    val strict: Json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = false
    }
}

class PhysicalAffordanceEvaluationService(
    private val validator: PhysicalAffordanceValidator = PhysicalAffordanceValidator(),
) {
    fun evaluate(suite: PhysicalAffordanceSuite): PhysicalAffordanceTechnicalReport {
        require(suite.schemaVersion == "1.0") { "WORLD-PHYSICS-01 suite schema version is invalid." }
        require(suite.suiteId == "world-physics01-affordance-64-v1") { "WORLD-PHYSICS-01 suite id is invalid." }
        require(suite.boundary == "NON_CANON_EVALUATION") { "WORLD-PHYSICS-01 fixtures must remain non-Canon." }
        require(suite.snapshots.map { it.snapshotId }.distinct().size == suite.snapshots.size) {
            "WORLD-PHYSICS-01 snapshot ids must be unique."
        }
        require(suite.archetypes.map { it.archetypeId }.distinct().size == suite.archetypes.size) {
            "WORLD-PHYSICS-01 archetype ids must be unique."
        }
        require(suite.cases.size >= 60) { "WORLD-PHYSICS-01 requires at least 60 adversarial cases." }
        require(suite.cases.map { it.caseId }.distinct().size == suite.cases.size) {
            "WORLD-PHYSICS-01 case ids must be unique."
        }
        val snapshots = suite.snapshots.associateBy { it.snapshotId }
        require(suite.cases.all { it.snapshotId in snapshots }) { "Every case must reference a declared snapshot." }
        require(PhysicalActionType.entries.all { action -> suite.cases.count { it.proposal.actionType == action } >= 4 }) {
            "Every V1 physical action type requires at least four cases."
        }

        val outcomes = suite.cases.map { evaluationCase ->
            val result = validator.validate(suite.archetypes, snapshots.getValue(evaluationCase.snapshotId), evaluationCase.proposal)
            val matched = result.accepted == evaluationCase.expectedAccepted &&
                result.rejectionCodes == evaluationCase.expectedRejectionCodes.sorted() &&
                result.requiredEffects == evaluationCase.expectedRequiredEffects.sorted()
            PhysicalAffordanceCaseOutcome(
                caseId = evaluationCase.caseId,
                matchedExpectation = matched,
                actualAccepted = result.accepted,
                actualRejectionCodes = result.rejectionCodes,
                actualRequiredEffects = result.requiredEffects,
            )
        }
        val mismatches = outcomes.count { !it.matchedExpectation }
        val actionCoverage = PhysicalActionType.entries.map { action ->
            val cases = suite.cases.filter { it.proposal.actionType == action }
            PhysicalActionCoverage(
                actionType = action,
                caseCount = cases.size,
                acceptedCaseCount = cases.count { it.expectedAccepted },
                rejectedCaseCount = cases.count { !it.expectedAccepted },
            )
        }
        val rejectionCounts = outcomes.flatMap { it.actualRejectionCodes }
            .groupingBy { it }
            .eachCount()
            .toSortedMap()
        return PhysicalAffordanceTechnicalReport(
            suiteId = suite.suiteId,
            caseCount = suite.cases.size,
            expectationMatchCount = outcomes.size - mismatches,
            expectationMismatchCount = mismatches,
            actionCoverage = actionCoverage,
            rejectionCodeCounts = rejectionCounts,
            outcomes = outcomes,
            technicalDecision = if (mismatches == 0) {
                "FOUNDATION_TECHNICAL_PASS_RUNTIME_NOT_CONNECTED"
            } else {
                "FOUNDATION_EXPECTATION_MISMATCH"
            },
            claimBoundary = listOf(
                "V1 is a deterministic rigid-body approximation, not a complete physics simulation.",
                "A pass means no V1 rule disproved the action; it does not prove general real-world realism.",
                "No formal Runtime, SQLite, Canon, Provider or model integration is authorized by this report.",
            ),
        )
    }
}
