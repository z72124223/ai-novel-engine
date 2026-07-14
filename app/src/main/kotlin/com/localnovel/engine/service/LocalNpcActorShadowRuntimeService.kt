package com.localnovel.engine.service

import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.LocalNpcActorShadowInput
import com.localnovel.engine.model.LocalNpcActorShadowReport
import com.localnovel.engine.model.LocalNpcActorShadowStateEvidence
import com.localnovel.engine.model.LocalNpcActorSafetyLoopRequest
import com.localnovel.engine.model.LocalNpcActorVisibleSummary
import com.localnovel.engine.repository.SqliteStoryRepository
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

class LocalNpcActorShadowRuntimeService(
    private val repository: SqliteStoryRepository,
    generator: LocalNpcActorGenerator,
    private val runtime: MultiTurnRuntime = MultiTurnRuntime(repository),
    private val contextCompiler: LocalNpcActorContextCompiler = LocalNpcActorContextCompiler(),
) {
    private val localSafetyLoop = LocalNpcActorSafetyLoopService(generator)

    fun run(input: LocalNpcActorShadowInput): LocalNpcActorShadowReport {
        validateInput(input)
        val session = requireNotNull(repository.getSession(input.sessionId)) {
            "Formal Session not found: ${input.sessionId}"
        }
        val before = captureState(input.sessionId)
        val context = runtime.previewTurnContext(input.sessionId, input.playerInput)
        require(context.sessionId == session.id && context.project.id == session.projectId) {
            "Preview context does not belong to the requested formal Session."
        }
        require(context.baseStateVersion == session.stateVersion && context.turnNumber == session.nextTurnNumber) {
            "Preview context is not based on the current formal Session version."
        }

        val prepared = contextCompiler.compile(
            context = context,
            runId = input.shadowRunId,
            profile = input.profile,
            targetIds = input.targetIds,
            physicalContext = input.physicalContext,
            outputConstraints = input.outputConstraints,
        )
        val localReport = localSafetyLoop.run(
            context = context,
            request = LocalNpcActorSafetyLoopRequest(
                loopId = input.shadowRunId,
                prompt = prepared.prompt,
                engineContext = input.physicalContext,
                maxAttempts = input.maxAttempts,
                maxElapsedMinutes = input.maxElapsedMinutes,
            ),
        )

        val after = captureState(input.sessionId, context.contextId)
        requireUnchanged(before, after)
        require(!localReport.formalStateWritePerformed) {
            "Local NPC Actor safety loop reported a formal state write."
        }
        val accepted = localReport.safetyResult.accepted
        return LocalNpcActorShadowReport(
            shadowRunId = input.shadowRunId,
            projectId = context.project.id,
            sessionId = context.sessionId,
            turnNumber = context.turnNumber,
            baseStateVersion = context.baseStateVersion,
            contextId = context.contextId,
            contextHash = context.contextHash,
            actorRequest = prepared.actorRequest,
            visibleSummary = prepared.visibleSummary,
            localModelReport = localReport,
            beforeState = before,
            afterState = after,
            technicalDecision = if (accepted) {
                "SHADOW_CANDIDATE_ACCEPTED_NO_STATE_WRITE"
            } else {
                "SHADOW_CANDIDATE_REJECTED_NO_STATE_WRITE"
            },
        )
    }

    private fun validateInput(input: LocalNpcActorShadowInput) {
        require(input.schemaVersion == "1.0") { "Unsupported formal runtime shadow input version." }
        require(input.boundary == "FORMAL_RUNTIME_SHADOW_INPUT") { "Invalid formal runtime shadow boundary." }
        require(input.shadowRunId.isNotBlank()) { "Shadow run id is required." }
        require(input.sessionId.isNotBlank()) { "Formal Session id is required." }
        require(input.playerInput.isNotBlank()) { "A bounded turn stimulus is required." }
        require(input.profile.sandboxCandidateOnly) { "Shadow runtime accepts sandbox candidate profiles only." }
        require(input.profile.npcId.isNotBlank()) { "NPC profile id is required." }
        require(input.targetIds.size <= MAX_TARGET_IDS && input.targetIds.size == input.targetIds.distinct().size) {
            "Shadow runtime target ids are invalid."
        }
        require(input.maxAttempts in 1..2) { "Shadow runtime permits at most one repair." }
        require(input.maxElapsedMinutes in 1..60) { "Shadow runtime elapsed-minute budget is invalid." }
    }

    private fun captureState(sessionId: String, previewContextId: String? = null): LocalNpcActorShadowStateEvidence {
        val session = requireNotNull(repository.getSession(sessionId)) { "Formal Session disappeared: $sessionId" }
        return LocalNpcActorShadowStateEvidence(
            databaseSha256 = sha256(repository.databasePath()),
            sessionStateVersion = session.stateVersion,
            nextTurnNumber = session.nextTurnNumber,
            appliedTurnCount = repository.appliedTurnCount(sessionId),
            turnLogCount = repository.tableCount("turn_logs"),
            eventCount = repository.tableCount("events"),
            formalPhysicalTransitionCount = repository.formalPhysicalTransitionCount(sessionId),
            formalPhysicalStateVersion = repository.getFormalPhysicalState(sessionId)?.physicalStateVersion,
            previewContextPersisted = previewContextId?.let { repository.getPendingContext(sessionId, it) != null } ?: false,
        )
    }

    private fun requireUnchanged(
        before: LocalNpcActorShadowStateEvidence,
        after: LocalNpcActorShadowStateEvidence,
    ) {
        require(!after.previewContextPersisted) { "Shadow preview created a pending context." }
        require(after.copy(previewContextPersisted = false) == before.copy(previewContextPersisted = false)) {
            "Formal Session or SQLite state changed during the shadow run."
        }
    }

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
        private const val MAX_TARGET_IDS = 8
    }

}
