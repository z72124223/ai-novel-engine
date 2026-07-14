package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.FormalTurnPhysicalValidationContext
import com.localnovel.engine.model.GameMasterResolution
import com.localnovel.engine.model.LocalNpcActorFormalDecisionType
import com.localnovel.engine.model.LocalNpcActorFormalResolveReport
import com.localnovel.engine.model.LocalNpcActorFormalStateEvidence
import com.localnovel.engine.model.LocalNpcActorFormalTurnCandidatePackage
import com.localnovel.engine.model.LocalNpcActorFormalTurnCandidatePayload
import com.localnovel.engine.model.LocalNpcActorFormalTurnDecision
import com.localnovel.engine.model.LocalNpcActorFormalTurnInput
import com.localnovel.engine.model.LocalNpcActorSafetyLoopRequest
import com.localnovel.engine.model.NpcActionProposal
import com.localnovel.engine.model.NpcActorPhysicalResponse
import com.localnovel.engine.model.NpcDecisionTrace
import com.localnovel.engine.model.PhysicalActionType
import com.localnovel.engine.model.TimeAdvanceProposal
import com.localnovel.engine.repository.SqliteStoryRepository
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.time.OffsetDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object LocalNpcActorFormalTurnHash {
    fun candidate(payload: LocalNpcActorFormalTurnCandidatePayload): String =
        sha256(NpcActorJson.strict.encodeToString(payload))

    fun decision(decision: LocalNpcActorFormalTurnDecision): String =
        sha256(NpcActorJson.strict.encodeToString(decision))

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}

class LocalNpcActorFormalTurnPreparationService(
    private val repository: SqliteStoryRepository,
    generator: LocalNpcActorGenerator,
    private val runtime: MultiTurnRuntime = MultiTurnRuntime(repository),
    private val contextCompiler: LocalNpcActorContextCompiler = LocalNpcActorContextCompiler(),
) {
    private val localSafetyLoop = LocalNpcActorSafetyLoopService(generator)

    fun prepare(input: LocalNpcActorFormalTurnInput): LocalNpcActorFormalTurnCandidatePackage {
        validateInput(input)
        val session = requireNotNull(repository.getSession(input.sessionId)) {
            "Formal Session not found: ${input.sessionId}"
        }
        val before = captureState(input.sessionId, null)
        var pendingContextId: String? = null
        try {
            val context = runtime.prepareTurn(input.sessionId, input.playerInput)
            pendingContextId = context.contextId
            require(context.sessionId == session.id && context.project.id == session.projectId) {
                "Prepared Context does not belong to the requested formal Session."
            }
            require(context.baseStateVersion == session.stateVersion && context.turnNumber == session.nextTurnNumber) {
                "Prepared Context is not based on the current formal Session version."
            }
            val prepared = contextCompiler.compile(
                context = context,
                runId = input.preparationId,
                profile = input.profile,
                targetIds = input.targetIds,
                physicalContext = input.physicalContext,
                outputConstraints = input.outputConstraints,
            )
            val localReport = localSafetyLoop.run(
                context = context,
                request = LocalNpcActorSafetyLoopRequest(
                    loopId = input.preparationId,
                    prompt = prepared.prompt,
                    engineContext = input.physicalContext,
                    maxAttempts = input.maxAttempts,
                    maxElapsedMinutes = input.maxElapsedMinutes,
                ),
            )
            require(localReport.safetyResult.accepted) {
                "Local NPC candidate did not pass the safety loop: ${localReport.safetyResult.status}"
            }
            val acceptedIndex = requireNotNull(localReport.safetyResult.acceptedAttemptNumber) - 1
            val selectedResponse = localReport.generatedResponses.getOrNull(acceptedIndex)
                ?: error("Accepted NPC attempt is missing its generated response.")
            val acceptedProposal = requireNotNull(localReport.safetyResult.acceptedProposal) {
                "Accepted NPC attempt is missing its mapped proposal."
            }
            require(selectedResponse.physicalActionProposal.actionType in TRANSIENT_PHYSICAL_ACTIONS) {
                "NPC-ACTOR-08 V1 permits only transient physical actions; stateful actions require a later reviewed package."
            }
            val gameMasterResolution = GameMasterResolutionService().resolve(context, acceptedProposal)
            require(gameMasterResolution.accepted) { "Accepted safety result no longer passes Game Master review." }

            val candidateSeed = "${input.preparationId}:${context.sessionId}:${context.contextId}:${context.contextHash}"
            val candidateId = "npc-actor08-${sha256(candidateSeed).take(20)}"
            val resultId = "npc-actor08-result-${sha256("$candidateSeed:result").take(20)}"
            val result = compileResult(
                context = context,
                candidateId = candidateId,
                resultId = resultId,
                response = selectedResponse,
                proposal = acceptedProposal,
                resolution = gameMasterResolution,
            )
            val formalPhysicalContext = bindPhysicalContext(context, result, input, selectedResponse)
            val validation = runtime.validate(context, result, formalPhysicalContext)
            require(validation.isValid) {
                validation.errors.joinToString(separator = "\n") { "${it.code}: ${it.message}" }
            }
            val trace = buildTrace(
                context = context,
                input = input,
                prepared = prepared,
                response = selectedResponse,
                proposal = acceptedProposal,
                candidateId = candidateId,
            )
            val after = captureState(input.sessionId, context.contextId)
            require(after.pendingContextPresent) { "Prepared candidate lost its pending Context." }
            require(after.sessionStateVersion == before.sessionStateVersion && after.nextTurnNumber == before.nextTurnNumber) {
                "Candidate preparation changed the formal Session version before approval."
            }
            require(after.appliedTurnCount == before.appliedTurnCount && after.eventCount == before.eventCount) {
                "Candidate preparation applied formal turn data before approval."
            }
            val payload = LocalNpcActorFormalTurnCandidatePayload(
                candidateId = candidateId,
                preparationId = input.preparationId,
                projectId = context.project.id,
                sessionId = context.sessionId,
                turnId = context.turnId,
                contextId = context.contextId,
                contextHash = context.contextHash,
                turnNumber = context.turnNumber,
                baseStateVersion = context.baseStateVersion,
                resultId = resultId,
                resultCanonicalSha256 = formalPhysicalContext.resultCanonicalSha256,
                actorRequest = prepared.actorRequest,
                visibleSummary = prepared.visibleSummary,
                selectedResponse = selectedResponse,
                acceptedProposal = acceptedProposal,
                localModelReport = localReport,
                aiTurnResult = result,
                formalPhysicalContext = formalPhysicalContext,
                npcDecisionTrace = trace,
                gameMasterResolution = gameMasterResolution,
                preparationBeforeState = before,
                preparationAfterState = after,
            )
            return LocalNpcActorFormalTurnCandidatePackage(
                candidateSha256 = LocalNpcActorFormalTurnHash.candidate(payload),
                payload = payload,
            )
        } catch (error: Throwable) {
            pendingContextId?.let { repository.abortPendingContext(input.sessionId, it) }
            throw error
        }
    }

    private fun validateInput(input: LocalNpcActorFormalTurnInput) {
        require(input.schemaVersion == "1.0") { "Unsupported formal NPC candidate input version." }
        require(input.boundary == "FORMAL_RUNTIME_CANDIDATE_INPUT") { "Invalid formal NPC candidate boundary." }
        require(input.preparationId.isNotBlank()) { "Preparation id is required." }
        require(input.sessionId.isNotBlank()) { "Formal Session id is required." }
        require(input.playerInput.isNotBlank()) { "A bounded turn stimulus is required." }
        require(input.maxAttempts in 1..2) { "NPC-ACTOR-08 permits at most one repair." }
        require(input.maxElapsedMinutes in 1..60) { "NPC-ACTOR-08 elapsed-minute budget is invalid." }
    }

    private fun compileResult(
        context: com.localnovel.engine.model.ContextPacket,
        candidateId: String,
        resultId: String,
        response: NpcActorPhysicalResponse,
        proposal: NpcActionProposal,
        resolution: GameMasterResolution,
    ): AiTurnResult {
        val actor = response.actorResponse
        val narrative = listOf(
            actor.visibleAction,
            actor.nonverbalAction,
            actor.utterance.takeIf { it.isNotBlank() }?.let { "「$it」" },
            proposal.observableConsequence,
        ).filterNotNull().filter { it.isNotBlank() }.distinct().joinToString(" ")
        require(narrative.isNotBlank()) { "Engine-compiled NPC narrative is empty." }
        return AiTurnResult(
            protocolVersion = context.protocolVersion,
            projectId = context.project.id,
            turnId = context.turnId,
            resultId = resultId,
            contextId = context.contextId,
            sessionId = context.sessionId,
            turnNumber = context.turnNumber,
            baseStateVersion = context.baseStateVersion,
            contextHash = context.contextHash,
            narrative = narrative,
            turnSummary = "${actor.actorId} completed one human-approved bounded non-Canon NPC action.",
            characterActions = listOf(
                CharacterAction(
                    characterId = actor.actorId,
                    intent = actor.actionIntent,
                    visibleAction = actor.visibleAction,
                    hiddenMotive = actor.emotionHidden,
                    usedMemoryIds = actor.memoryIds,
                    usedKnowledgeClaimIds = actor.beliefClaimIds,
                    characterFitScore = actor.confidence.coerceIn(0, 100),
                    storyProgressScore = 60,
                ),
            ),
            stateDeltas = listOf(
                DeltaOperation(
                    type = DeltaOperationType.ADD_EVENT,
                    targetType = "event",
                    targetId = "event-$candidateId",
                    payload = JsonObject(
                        mapOf(
                            "summary" to JsonPrimitive(proposal.observableConsequence),
                            "npcId" to JsonPrimitive(proposal.npcId),
                            "locationId" to JsonPrimitive(proposal.locationId),
                            "offscreen" to JsonPrimitive(true),
                            "sourceCandidateId" to JsonPrimitive(candidateId),
                        ),
                    ),
                ),
            ),
            timeAdvanceProposal = TimeAdvanceProposal(
                minutes = resolution.elapsedMinutes,
                sourceType = "human_approved_npc_actor",
                sourceId = candidateId,
                reason = "Human-approved bounded non-Canon NPC action.",
            ),
            capabilityUseChecks = actor.capabilityUseChecks,
            physicalActionProposals = listOf(response.physicalActionProposal),
            validationNotes = listOf("NPC-ACTOR-08 engine-owned compilation; model-authored state deltas are not accepted."),
        )
    }

    private fun bindPhysicalContext(
        context: com.localnovel.engine.model.ContextPacket,
        result: AiTurnResult,
        input: LocalNpcActorFormalTurnInput,
        response: NpcActorPhysicalResponse,
    ): FormalTurnPhysicalValidationContext {
        val physical = input.physicalContext
        val snapshot = physical.physicalSnapshot.copy(boundary = FormalTurnPhysicalGateValidator.FORMAL_TURN_BOUNDARY)
        return FormalTurnPhysicalValidationContext(
            projectId = context.project.id,
            sessionId = context.sessionId,
            turnId = context.turnId,
            contextId = context.contextId,
            contextHash = context.contextHash,
            turnNumber = context.turnNumber,
            baseStateVersion = context.baseStateVersion,
            resultId = result.resultId,
            resultCanonicalSha256 = FormalTurnPhysicalGateValidator().canonicalResultSha256(result),
            physicalArchetypes = physical.physicalArchetypes,
            physicalSnapshot = snapshot,
            allowedPhysicalActionTypes = listOf(response.physicalActionProposal.actionType),
            allowedPhysicalActorIds = listOf(response.physicalActionProposal.actorId),
            allowedPhysicalTargetIds = listOfNotNull(
                response.physicalActionProposal.targetId,
                response.physicalActionProposal.secondaryTargetId,
            ).distinct(),
        )
    }

    private fun buildTrace(
        context: com.localnovel.engine.model.ContextPacket,
        input: LocalNpcActorFormalTurnInput,
        prepared: LocalNpcActorPreparedContext,
        response: NpcActorPhysicalResponse,
        proposal: NpcActionProposal,
        candidateId: String,
    ): NpcDecisionTrace {
        val actor = response.actorResponse
        return NpcDecisionTrace(
            traceId = "npc-trace-$candidateId",
            projectId = context.project.id,
            sessionId = context.sessionId,
            turnNumber = context.turnNumber,
            npcId = actor.actorId,
            scenarioSeed = input.preparationId,
            activeGoal = actor.immediateGoal,
            relevantMemories = actor.memoryIds,
            currentBeliefs = actor.beliefClaimIds,
            perceivedSituation = prepared.perception.perceivedSituation,
            consideredActions = listOf(actor.actionType) + actor.decisionSummary.rejectedActionLabels,
            selectedAction = actor.actionType,
            predictedCost = actor.predictedCost,
            rejectedAlternatives = actor.decisionSummary.rejectedActionLabels,
            offscreenReason = "NPC and player occupy different locations during this approved non-Canon turn.",
            proposal = proposal,
        )
    }

    private fun captureState(sessionId: String, contextId: String?): LocalNpcActorFormalStateEvidence {
        val session = requireNotNull(repository.getSession(sessionId)) { "Formal Session disappeared: $sessionId" }
        return LocalNpcActorFormalStateEvidence(
            databaseSha256 = sha256(repository.databasePath()),
            sessionStateVersion = session.stateVersion,
            nextTurnNumber = session.nextTurnNumber,
            appliedTurnCount = repository.appliedTurnCount(sessionId),
            turnLogCount = repository.tableCount("turn_logs"),
            eventCount = repository.tableCount("events"),
            memoryCount = repository.tableCount("character_memories"),
            knowledgeCount = repository.tableCount("character_knowledge"),
            formalPhysicalTransitionCount = repository.formalPhysicalTransitionCount(sessionId),
            formalPhysicalStateVersion = repository.getFormalPhysicalState(sessionId)?.physicalStateVersion,
            pendingContextPresent = contextId?.let { repository.getPendingContext(sessionId, it) != null } ?: false,
        )
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private fun sha256(path: Path): String {
        require(Files.isRegularFile(path)) { "Formal SQLite database does not exist: $path" }
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).buffered().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                if (read > 0) digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        private val TRANSIENT_PHYSICAL_ACTIONS = setOf(PhysicalActionType.USE_AS_SUPPORT, PhysicalActionType.REACH)
    }
}

class LocalNpcActorFormalTurnResolutionService(
    private val repository: SqliteStoryRepository,
    private val runtime: MultiTurnRuntime = MultiTurnRuntime(repository),
) {
    fun resolve(
        candidatePackage: LocalNpcActorFormalTurnCandidatePackage,
        decision: LocalNpcActorFormalTurnDecision,
    ): LocalNpcActorFormalResolveReport {
        val payload = candidatePackage.payload
        validatePackage(candidatePackage)
        validateDecision(payload, candidatePackage.candidateSha256, decision)
        val pending = repository.getPendingContext(payload.sessionId, payload.contextId)
            ?: error("Pending Context is missing; candidate is stale, already resolved, rejected or replayed.")
        require(pending.project.id == payload.projectId && pending.turnId == payload.turnId) {
            "Pending Context identity does not match the approved candidate."
        }
        require(pending.contextHash == payload.contextHash && pending.turnNumber == payload.turnNumber &&
            pending.baseStateVersion == payload.baseStateVersion
        ) { "Pending Context Hash, turn or state version does not match the approved candidate." }
        val before = captureState(payload.sessionId, payload.contextId)
        val formalTurnApplied = when (decision.decision) {
            LocalNpcActorFormalDecisionType.REJECT_NON_CANON_FORMAL_TURN -> {
                require(repository.abortPendingContext(payload.sessionId, payload.contextId) == 1) {
                    "Rejected candidate did not clear exactly one pending Context."
                }
                false
            }
            LocalNpcActorFormalDecisionType.APPROVE_NON_CANON_FORMAL_TURN -> {
                val currentSession = requireNotNull(repository.getSession(payload.sessionId)) {
                    "Formal Session not found: ${payload.sessionId}"
                }
                require(currentSession.stateVersion == payload.baseStateVersion && currentSession.nextTurnNumber == payload.turnNumber) {
                    "Formal Session moved after candidate preparation; approval is stale."
                }
                runtime.resolveTurn(
                    payload.sessionId,
                    payload.aiTurnResult,
                    ResolveTurnOptions(
                        scenarioSeed = payload.preparationId,
                        npcDecisionTrace = payload.npcDecisionTrace,
                        npcGameMasterResolution = payload.gameMasterResolution,
                        formalTurnPhysicalContext = payload.formalPhysicalContext,
                    ),
                )
                true
            }
        }
        val after = captureState(payload.sessionId, payload.contextId)
        require(!after.pendingContextPresent) { "Resolved candidate still has a pending Context." }
        if (formalTurnApplied) {
            require(after.sessionStateVersion == before.sessionStateVersion + 1) {
                "Approved turn did not advance the Session state version exactly once."
            }
            require(after.nextTurnNumber == before.nextTurnNumber + 1 && after.appliedTurnCount == before.appliedTurnCount + 1) {
                "Approved turn did not advance the formal turn exactly once."
            }
            require(after.memoryCount == before.memoryCount && after.knowledgeCount == before.knowledgeCount) {
                "NPC-ACTOR-08 unexpectedly changed formal Memory or Knowledge."
            }
        } else {
            require(after.sessionStateVersion == before.sessionStateVersion && after.nextTurnNumber == before.nextTurnNumber) {
                "Rejected candidate changed the formal Session."
            }
            require(after.appliedTurnCount == before.appliedTurnCount && after.eventCount == before.eventCount) {
                "Rejected candidate applied formal data."
            }
        }
        return LocalNpcActorFormalResolveReport(
            status = if (formalTurnApplied) "APPROVED_AND_APPLIED_NON_CANON" else "REJECTED_AND_ABORTED",
            candidateId = payload.candidateId,
            candidateSha256 = candidatePackage.candidateSha256,
            decisionId = decision.decisionId,
            decisionSha256 = LocalNpcActorFormalTurnHash.decision(decision),
            decision = decision.decision,
            projectId = payload.projectId,
            sessionId = payload.sessionId,
            turnId = payload.turnId,
            contextId = payload.contextId,
            turnNumber = payload.turnNumber,
            resultId = payload.resultId,
            beforeState = before,
            afterState = after,
            formalTurnApplied = formalTurnApplied,
            pendingContextCleared = !after.pendingContextPresent,
            testOnlyApproval = decision.testOnly,
        )
    }

    private fun validatePackage(candidatePackage: LocalNpcActorFormalTurnCandidatePackage) {
        require(candidatePackage.schemaVersion == "1.0" && candidatePackage.status == "HUMAN_APPROVAL_REQUIRED") {
            "Unsupported formal NPC candidate package."
        }
        val payload = candidatePackage.payload
        require(payload.schemaVersion == "1.0" && payload.boundary == "FORMAL_RUNTIME_NON_CANON_CANDIDATE") {
            "Invalid formal NPC candidate boundary."
        }
        require(LocalNpcActorFormalTurnHash.candidate(payload).equals(candidatePackage.candidateSha256, true)) {
            "Candidate package SHA-256 mismatch; content was changed after review packaging."
        }
        require(payload.aiTurnResult.resultId == payload.resultId &&
            payload.aiTurnResult.contextId == payload.contextId &&
            payload.aiTurnResult.contextHash == payload.contextHash
        ) { "Engine-compiled result identity does not match the candidate package." }
        val actualResultHash = FormalTurnPhysicalGateValidator().canonicalResultSha256(payload.aiTurnResult)
        require(actualResultHash.equals(payload.resultCanonicalSha256, true) &&
            actualResultHash.equals(payload.formalPhysicalContext.resultCanonicalSha256, true)
        ) { "Canonical AiTurnResult SHA-256 does not match the candidate package." }
        require(payload.gameMasterResolution.accepted) { "Candidate lacks an accepted Game Master resolution." }
        require(payload.localModelReport.safetyResult.accepted) { "Candidate lacks an accepted safety-loop result." }
        require(payload.aiTurnResult.memoryCandidates.isEmpty() && payload.aiTurnResult.knowledgeUpdates.isEmpty() &&
            payload.aiTurnResult.entityProposals.isEmpty() && payload.aiTurnResult.promotionRecommendations.isEmpty()
        ) { "NPC-ACTOR-08 candidate contains forbidden Memory, Knowledge or Canon/provisional writes." }
    }

    private fun validateDecision(
        payload: LocalNpcActorFormalTurnCandidatePayload,
        candidateSha256: String,
        decision: LocalNpcActorFormalTurnDecision,
    ) {
        require(decision.schemaVersion == "1.0" && decision.boundary == "HUMAN_APPROVAL_DECISION") {
            "Unsupported human decision contract."
        }
        require(decision.decisionId.isNotBlank() && decision.reviewerId.isNotBlank() && decision.reviewedAt.isNotBlank()) {
            "Human decision identity, reviewer and review time are required."
        }
        require(runCatching { OffsetDateTime.parse(decision.reviewedAt) }.isSuccess) {
            "Human decision reviewedAt must be a valid ISO-8601 timestamp with an offset."
        }
        require(if (decision.testOnly) decision.reviewerId.startsWith("TEST_") else !decision.reviewerId.startsWith("TEST_")) {
            "testOnly and reviewer identity are inconsistent."
        }
        require(decision.acknowledgement == REQUIRED_ACKNOWLEDGEMENT) {
            "Human decision acknowledgement is missing or invalid."
        }
        require(decision.candidateId == payload.candidateId && decision.candidateSha256.equals(candidateSha256, true)) {
            "Human decision does not bind the exact candidate id and SHA-256."
        }
        require(decision.projectId == payload.projectId && decision.sessionId == payload.sessionId &&
            decision.turnId == payload.turnId && decision.contextId == payload.contextId &&
            decision.contextHash == payload.contextHash && decision.turnNumber == payload.turnNumber &&
            decision.baseStateVersion == payload.baseStateVersion && decision.resultId == payload.resultId &&
            decision.resultCanonicalSha256.equals(payload.resultCanonicalSha256, true)
        ) { "Human decision does not bind the exact formal Session, Context, turn, state version and result Hash." }
    }

    private fun captureState(sessionId: String, contextId: String): LocalNpcActorFormalStateEvidence {
        val session = requireNotNull(repository.getSession(sessionId)) { "Formal Session disappeared: $sessionId" }
        return LocalNpcActorFormalStateEvidence(
            databaseSha256 = sha256(repository.databasePath()),
            sessionStateVersion = session.stateVersion,
            nextTurnNumber = session.nextTurnNumber,
            appliedTurnCount = repository.appliedTurnCount(sessionId),
            turnLogCount = repository.tableCount("turn_logs"),
            eventCount = repository.tableCount("events"),
            memoryCount = repository.tableCount("character_memories"),
            knowledgeCount = repository.tableCount("character_knowledge"),
            formalPhysicalTransitionCount = repository.formalPhysicalTransitionCount(sessionId),
            formalPhysicalStateVersion = repository.getFormalPhysicalState(sessionId)?.physicalStateVersion,
            pendingContextPresent = repository.getPendingContext(sessionId, contextId) != null,
        )
    }

    private fun sha256(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).buffered().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                if (read > 0) digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val REQUIRED_ACKNOWLEDGEMENT = "I_REVIEWED_AND_APPROVE_OR_REJECT_THIS_EXACT_NON_CANON_CANDIDATE"
    }
}
