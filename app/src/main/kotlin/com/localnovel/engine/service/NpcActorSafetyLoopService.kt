package com.localnovel.engine.service

import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.NpcActorPhysicalBindingResult
import com.localnovel.engine.model.NpcActorPhysicalResponse
import com.localnovel.engine.model.NpcActorSafetyLoopAttemptResult
import com.localnovel.engine.model.NpcActorSafetyLoopCaseOutcome
import com.localnovel.engine.model.NpcActorSafetyLoopInput
import com.localnovel.engine.model.NpcActorSafetyLoopResult
import com.localnovel.engine.model.NpcActorSafetyLoopSuite
import com.localnovel.engine.model.NpcActorSafetyLoopTechnicalReport
import com.localnovel.engine.model.NpcActorSafetyRepairRequest
import com.localnovel.engine.model.PhysicalEvidenceFile
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlinx.serialization.decodeFromString

class NpcActorSafetyLoopService(
    private val bindingValidator: NpcActorPhysicalBindingValidator = NpcActorPhysicalBindingValidator(),
    private val gameMaster: GameMasterResolutionService = GameMasterResolutionService(),
) {
    fun run(context: ContextPacket, input: NpcActorSafetyLoopInput): NpcActorSafetyLoopResult {
        validateInput(input)
        val attempts = mutableListOf<NpcActorSafetyLoopAttemptResult>()

        input.responseAttempts.forEachIndexed { index, response ->
            val attemptNumber = index + 1
            val evaluation = evaluateAttempt(context, input, response, attemptNumber)
            if (evaluation.accepted) {
                attempts += evaluation.result
                return result(
                    input = input,
                    accepted = true,
                    status = ACCEPTED_FOR_FORMAL_RESOLVE_CANDIDATE,
                    attempts = attempts,
                    acceptedAttemptNumber = attemptNumber,
                    acceptedProposal = evaluation.binding.proposal,
                    unusedResponseAttemptCount = input.responseAttempts.size - attemptNumber,
                )
            }

            val canRetry = attemptNumber < input.maxAttempts
            val repairRequest = if (canRetry) {
                buildRepairRequest(input, attemptNumber, evaluation.rejectionStage!!, evaluation.findings)
            } else {
                null
            }
            attempts += evaluation.result.copy(repairRequest = repairRequest)

            if (canRetry && attemptNumber == input.responseAttempts.size) {
                return result(
                    input = input,
                    accepted = false,
                    status = REPAIR_RESPONSE_REQUIRED,
                    attempts = attempts,
                )
            }
        }

        return result(
            input = input,
            accepted = false,
            status = REJECTED_AFTER_MAX_ATTEMPTS,
            attempts = attempts,
        )
    }

    private fun evaluateAttempt(
        context: ContextPacket,
        input: NpcActorSafetyLoopInput,
        response: NpcActorPhysicalResponse,
        attemptNumber: Int,
    ): AttemptEvaluation {
        val binding = bindingValidator.validateAndMap(input.prompt, input.engineContext, response)
        if (!binding.accepted) {
            val stage = when (binding.status) {
                NpcActorPhysicalBindingValidator.REJECTED_ACTOR_CONTRACT -> ACTOR_CONTRACT
                NpcActorPhysicalBindingValidator.REJECTED_PHYSICAL_BINDING_CONTRACT -> PHYSICAL_BINDING_CONTRACT
                else -> PHYSICAL_AFFORDANCE
            }
            return rejectedAttempt(response, binding, stage, binding.findings, attemptNumber)
        }

        val proposal = requireNotNull(binding.proposal) { "Accepted binding must contain a proposal." }
        val gameMasterResult = gameMaster.resolve(context, proposal)
        if (!gameMasterResult.accepted) {
            return AttemptEvaluation(
                accepted = false,
                rejectionStage = WORLD_LEGALITY,
                findings = gameMasterResult.findings.map { "world:$it" },
                binding = binding,
                result = NpcActorSafetyLoopAttemptResult(
                    attemptNumber = attemptNumber,
                    requestId = response.requestId,
                    bindingAccepted = true,
                    bindingStatus = binding.status,
                    gameMasterAccepted = false,
                    gameMasterStatus = gameMasterResult.status,
                    rejectionStage = WORLD_LEGALITY,
                    findings = gameMasterResult.findings.map { "world:$it" },
                ),
            )
        }

        if (gameMasterResult.elapsedMinutes > input.maxElapsedMinutes) {
            val findings = listOf("time:elapsed_minutes_exceeds_budget")
            return AttemptEvaluation(
                accepted = false,
                rejectionStage = TIME_BUDGET,
                findings = findings,
                binding = binding,
                result = NpcActorSafetyLoopAttemptResult(
                    attemptNumber = attemptNumber,
                    requestId = response.requestId,
                    bindingAccepted = true,
                    bindingStatus = binding.status,
                    gameMasterAccepted = true,
                    gameMasterStatus = gameMasterResult.status,
                    timeBudgetAccepted = false,
                    elapsedMinutes = gameMasterResult.elapsedMinutes,
                    rejectionStage = TIME_BUDGET,
                    findings = findings,
                ),
            )
        }

        return AttemptEvaluation(
            accepted = true,
            binding = binding,
            result = NpcActorSafetyLoopAttemptResult(
                attemptNumber = attemptNumber,
                requestId = response.requestId,
                bindingAccepted = true,
                bindingStatus = binding.status,
                gameMasterAccepted = true,
                gameMasterStatus = gameMasterResult.status,
                timeBudgetAccepted = true,
                elapsedMinutes = gameMasterResult.elapsedMinutes,
            ),
        )
    }

    private fun rejectedAttempt(
        response: NpcActorPhysicalResponse,
        binding: NpcActorPhysicalBindingResult,
        stage: String,
        findings: List<String>,
        attemptNumber: Int,
    ): AttemptEvaluation = AttemptEvaluation(
        accepted = false,
        rejectionStage = stage,
        findings = findings,
        binding = binding,
        result = NpcActorSafetyLoopAttemptResult(
            attemptNumber = attemptNumber,
            requestId = response.requestId,
            bindingAccepted = false,
            bindingStatus = binding.status,
            rejectionStage = stage,
            findings = findings,
        ),
    )

    private fun buildRepairRequest(
        input: NpcActorSafetyLoopInput,
        attemptNumber: Int,
        stage: String,
        findings: List<String>,
    ): NpcActorSafetyRepairRequest {
        val request = input.prompt.actorRequest
        val allowedEvidenceIds = listOf(
            request.perceivedFacts,
            request.ownedMemories,
            request.ownedBeliefs,
            request.goals,
            request.relationships,
            request.recentDialogue,
            request.capabilities,
        ).flatten().map { it.evidenceId }.distinct().sorted()
        return NpcActorSafetyRepairRequest(
            loopId = input.loopId,
            requestId = request.requestId,
            failedAttemptNumber = attemptNumber,
            nextAttemptNumber = attemptNumber + 1,
            maxAttempts = input.maxAttempts,
            rejectionStage = stage,
            rejectionCodes = sanitizeCodes(findings),
            actorId = request.actorId,
            targetIds = request.targetIds.distinct(),
            locationId = request.locationId,
            allowedEvidenceIds = allowedEvidenceIds,
            allowedPhysicalActionTypes = input.prompt.allowedPhysicalActionTypes,
            visiblePhysicalTargetIds = input.prompt.visiblePhysicalTargetIds,
            repairInstructions = repairInstructions(stage),
        )
    }

    private fun sanitizeCodes(findings: List<String>): List<String> = findings
        .filter { it.matches(SAFE_CODE) }
        .distinct()
        .sorted()
        .take(MAX_REPAIR_CODES)

    private fun repairInstructions(stage: String): List<String> = when (stage) {
        ACTOR_CONTRACT -> listOf(
            "Preserve the request, actor, target and protocol identifiers exactly.",
            "Use only the evidence identifiers and output choices allowed by the actor-visible request.",
        )
        PHYSICAL_BINDING_CONTRACT -> listOf(
            "Use only the listed physical action types and visible physical target identifiers.",
            "Keep the physical actor identical to the actor-visible request.",
        )
        PHYSICAL_AFFORDANCE -> listOf(
            "Choose a different bounded physical action that can be validated without claiming success in advance.",
            "Do not invent measurements, capacities, geometry or hidden engine facts.",
        )
        WORLD_LEGALITY -> listOf(
            "Keep the actor at the declared location and use only actor-owned knowledge and memories.",
            "Do not act as an off-screen NPC while occupying the same location as the player.",
        )
        TIME_BUDGET -> listOf(
            "Choose one bounded action that fits within the declared offline turn time limit.",
        )
        else -> error("Unsupported repair stage: $stage")
    }

    private fun validateInput(input: NpcActorSafetyLoopInput) {
        require(input.schemaVersion == SCHEMA_VERSION) { "Unsupported NPC safety-loop schema version." }
        require(input.boundary == NON_CANON_EVALUATION) { "NPC safety loop is evaluation-only." }
        require(input.loopId.isNotBlank()) { "NPC safety-loop id is required." }
        require(input.maxAttempts in 1..MAX_ATTEMPTS) { "NPC safety loop allows at most two attempts." }
        require(input.responseAttempts.isNotEmpty()) { "At least one NPC response attempt is required." }
        require(input.responseAttempts.size <= input.maxAttempts) { "Response attempts exceed the configured maximum." }
        require(input.maxElapsedMinutes in 1..MAX_ELAPSED_MINUTES) { "Invalid NPC safety-loop time limit." }
    }

    private fun result(
        input: NpcActorSafetyLoopInput,
        accepted: Boolean,
        status: String,
        attempts: List<NpcActorSafetyLoopAttemptResult>,
        acceptedAttemptNumber: Int? = null,
        acceptedProposal: com.localnovel.engine.model.NpcActionProposal? = null,
        unusedResponseAttemptCount: Int = 0,
    ): NpcActorSafetyLoopResult = NpcActorSafetyLoopResult(
        loopId = input.loopId,
        accepted = accepted,
        status = status,
        attemptsProcessed = attempts.size,
        unusedResponseAttemptCount = unusedResponseAttemptCount,
        acceptedAttemptNumber = acceptedAttemptNumber,
        attemptResults = attempts,
        acceptedProposal = acceptedProposal,
        claimBoundary = CLAIM_BOUNDARY,
    )

    private data class AttemptEvaluation(
        val accepted: Boolean,
        val rejectionStage: String? = null,
        val findings: List<String> = emptyList(),
        val binding: NpcActorPhysicalBindingResult,
        val result: NpcActorSafetyLoopAttemptResult,
    )

    companion object {
        const val REPAIR_RESPONSE_REQUIRED = "REPAIR_RESPONSE_REQUIRED"
        const val ACCEPTED_FOR_FORMAL_RESOLVE_CANDIDATE = "ACCEPTED_FOR_FORMAL_RESOLVE_CANDIDATE"
        const val REJECTED_AFTER_MAX_ATTEMPTS = "REJECTED_AFTER_MAX_ATTEMPTS"
        const val ACTOR_CONTRACT = "ACTOR_CONTRACT"
        const val PHYSICAL_BINDING_CONTRACT = "PHYSICAL_BINDING_CONTRACT"
        const val PHYSICAL_AFFORDANCE = "PHYSICAL_AFFORDANCE"
        const val WORLD_LEGALITY = "WORLD_LEGALITY"
        const val TIME_BUDGET = "TIME_BUDGET"
        private const val SCHEMA_VERSION = "1.0"
        private const val NON_CANON_EVALUATION = "NON_CANON_EVALUATION"
        private const val MAX_ATTEMPTS = 2
        private const val MAX_ELAPSED_MINUTES = 60
        private const val MAX_REPAIR_CODES = 16
        private val SAFE_CODE = Regex("^[A-Za-z0-9_.:-]{1,96}$")
        private val CLAIM_BOUNDARY = listOf(
            "No SQLite, Canon, knowledge, memory or formal world state was modified.",
            "An accepted result is only a candidate for the existing formal resolve-turn pipeline.",
            "The repair request exposes error codes and actor-visible allowlists, not engine-owned measurements or geometry.",
            "Passing fixed offline fixtures does not prove human-like behavior, complete physics understanding or live-model quality.",
        )
    }
}

class NpcActorSafetyLoopEvaluationService(
    private val loopService: NpcActorSafetyLoopService = NpcActorSafetyLoopService(),
) {
    fun evaluate(repositoryRoot: Path, suite: NpcActorSafetyLoopSuite): NpcActorSafetyLoopTechnicalReport {
        require(suite.schemaVersion == "1.0") { "Unsupported NPC safety-loop suite schema version." }
        require(suite.boundary == "NON_CANON_EVALUATION") { "NPC safety-loop suite must remain non-Canon." }
        require(suite.cases.size == 4) { "NPC-ACTOR-05 requires exactly four fixed cases." }
        require(suite.cases.map { it.caseId }.distinct().size == suite.cases.size) { "Duplicate NPC safety-loop case id." }

        val outcomes = suite.cases.map { case ->
            val context = readStrict<ContextPacket>(repositoryRoot, case.contextFile)
            val input = readStrict<NpcActorSafetyLoopInput>(repositoryRoot, case.inputFile)
            val result = loopService.run(context, input)
            val stages = result.attemptResults.map { it.rejectionStage ?: "ACCEPTED" }
            val repairCount = result.attemptResults.count { it.repairRequest != null }
            val matched = result.status == case.expectedStatus &&
                result.acceptedAttemptNumber == case.expectedAcceptedAttemptNumber &&
                stages == case.expectedAttemptStages &&
                repairCount == case.expectedRepairRequestCount
            NpcActorSafetyLoopCaseOutcome(
                caseId = case.caseId,
                matchedExpectation = matched,
                actualStatus = result.status,
                actualAcceptedAttemptNumber = result.acceptedAttemptNumber,
                actualAttemptStages = stages,
                actualRepairRequestCount = repairCount,
                findings = result.attemptResults.flatMap { it.findings }.distinct().sorted(),
            )
        }
        val matches = outcomes.count { it.matchedExpectation }
        return NpcActorSafetyLoopTechnicalReport(
            suiteId = suite.suiteId,
            caseCount = outcomes.size,
            expectationMatchCount = matches,
            expectationMismatchCount = outcomes.size - matches,
            acceptedCaseCount = outcomes.count { it.actualStatus == NpcActorSafetyLoopService.ACCEPTED_FOR_FORMAL_RESOLVE_CANDIDATE },
            rejectedCaseCount = outcomes.count { it.actualStatus == NpcActorSafetyLoopService.REJECTED_AFTER_MAX_ATTEMPTS },
            repairRequiredCaseCount = outcomes.count { it.actualRepairRequestCount > 0 },
            outcomes = outcomes,
            technicalDecision = if (matches == outcomes.size) "TECHNICAL_PASS_MODEL_INTEGRATION_NOT_AUTHORIZED" else "TECHNICAL_FAIL",
            claimBoundary = listOf(
                "The four cases are deterministic non-Canon fixtures, not live-model generations.",
                "The storm-gate case remains rejected when both bounded attempts are unsafe.",
                "No accepted proposal was applied to SQLite or formal world state.",
            ),
        )
    }

    private inline fun <reified T> readStrict(repositoryRoot: Path, evidence: PhysicalEvidenceFile): T {
        val root = repositoryRoot.toAbsolutePath().normalize()
        val path = root.resolve(evidence.path).normalize()
        require(path.startsWith(root)) { "Evidence path escapes the repository root: ${evidence.path}" }
        require(Files.isRegularFile(path)) { "Evidence file does not exist: ${evidence.path}" }
        require(Files.size(path) == evidence.bytes) { "Evidence byte size mismatch: ${evidence.path}" }
        require(sha256(path) == evidence.sha256) { "Evidence SHA-256 mismatch: ${evidence.path}" }
        return NpcActorJson.strict.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
    }

    private fun sha256(path: Path): String = MessageDigest.getInstance("SHA-256")
        .digest(Files.readAllBytes(path))
        .joinToString("") { "%02x".format(it) }
}
