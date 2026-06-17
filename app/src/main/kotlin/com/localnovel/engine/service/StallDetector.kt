package com.localnovel.engine.service

import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.StateDelta
import com.localnovel.engine.repository.StoryRepository

class StallDetector(private val repository: StoryRepository? = null) {
    fun isNoStateChange(delta: StateDelta): Boolean =
        delta.operations.isEmpty() || delta.operations.all { it.type == DeltaOperationType.NOOP }

    fun hasTwoConsecutiveNoStateChangeDeltas(deltas: List<StateDelta>): Boolean =
        deltas.takeLast(2).size == 2 && deltas.takeLast(2).all { isNoStateChange(it) }

    fun hasStalled(projectId: String): Boolean {
        val repo = requireNotNull(repository) { "Repository is required for persisted stall detection." }
        val flags = repo.lastTurnStateChangeFlags(projectId, 2)
        return flags.size == 2 && flags.all { !it }
    }
}
