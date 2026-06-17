package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.ApplyResult
import com.localnovel.engine.model.ContextPacket

class TurnRunner(
    private val contextCompiler: ContextCompiler,
    private val validator: AiTurnResultValidator,
    private val stateReducer: StateReducer,
) {
    fun compile(projectId: String, playerInput: String): ContextPacket =
        contextCompiler.compile(projectId, playerInput)

    fun validate(context: ContextPacket, result: AiTurnResult) =
        validator.validate(context, result)

    fun apply(context: ContextPacket, result: AiTurnResult): ApplyResult {
        val report = validator.validate(context, result)
        if (!report.isValid) {
            error(report.errors.joinToString(separator = "\n") { "${it.code}: ${it.message}" })
        }
        return stateReducer.apply(context, result)
    }
}
