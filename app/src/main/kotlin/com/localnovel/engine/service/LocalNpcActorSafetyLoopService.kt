package com.localnovel.engine.service

import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.LocalNpcActorGenerationResult
import com.localnovel.engine.model.LocalNpcActorSafetyLoopReport
import com.localnovel.engine.model.LocalNpcActorSafetyLoopRequest
import com.localnovel.engine.model.NpcActorSafetyLoopInput

class LocalNpcActorSafetyLoopService(
    private val generator: LocalNpcActorGenerator,
    private val safetyLoop: NpcActorSafetyLoopService = NpcActorSafetyLoopService(),
) {
    fun run(
        context: ContextPacket,
        request: LocalNpcActorSafetyLoopRequest,
    ): LocalNpcActorSafetyLoopReport {
        validateRequest(request)
        val generations = mutableListOf<LocalNpcActorGenerationResult>()

        generations += generator.generate(request.prompt)
        var safetyResult = safetyLoop.run(context, request.toSafetyInput(generations))

        if (
            safetyResult.status == NpcActorSafetyLoopService.REPAIR_RESPONSE_REQUIRED &&
            generations.size < request.maxAttempts
        ) {
            val repair = requireNotNull(safetyResult.attemptResults.lastOrNull()?.repairRequest) {
                "Safety loop requested a repair without a sanitized repair request."
            }
            require(!repair.engineOwnedContextIncluded) {
                "Safety repair must never expose engine-owned context."
            }
            generations += generator.generate(request.prompt, repair)
            safetyResult = safetyLoop.run(context, request.toSafetyInput(generations))
        }

        return LocalNpcActorSafetyLoopReport(
            loopId = request.loopId,
            generatedAttemptCount = generations.size,
            generatedResponses = generations.map { it.response },
            invocationAudits = generations.map { it.audit },
            safetyResult = safetyResult,
            repairAttempted = generations.size > 1,
        )
    }

    private fun validateRequest(request: LocalNpcActorSafetyLoopRequest) {
        require(request.schemaVersion == "1.0") { "Unsupported local NPC Actor Pilot request version." }
        require(request.boundary == "NON_CANON_EVALUATION") { "Local NPC Actor Pilot must remain non-Canon." }
        require(request.prompt.boundary == request.boundary) { "Prompt boundary mismatch." }
        require(request.engineContext.boundary == request.boundary) { "Engine-context boundary mismatch." }
        require(request.prompt.actorRequest.requestId == request.engineContext.requestId) { "Request id mismatch." }
        require(request.maxAttempts in 1..2) { "Local NPC Actor Pilot permits at most one repair." }
        require(request.maxElapsedMinutes in 1..60) { "Local NPC Actor elapsed-minute budget is invalid." }
    }

    private fun LocalNpcActorSafetyLoopRequest.toSafetyInput(
        generations: List<LocalNpcActorGenerationResult>,
    ): NpcActorSafetyLoopInput = NpcActorSafetyLoopInput(
        loopId = loopId,
        prompt = prompt,
        engineContext = engineContext,
        responseAttempts = generations.map { it.response },
        maxAttempts = maxAttempts,
        maxElapsedMinutes = maxElapsedMinutes,
    )
}
