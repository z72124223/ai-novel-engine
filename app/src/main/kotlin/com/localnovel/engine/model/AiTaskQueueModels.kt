package com.localnovel.engine.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class AiQueueTask(
    val taskId: String,
    val title: String,
    val taskType: String = "implementation",
    val ownerRole: String = "B組01",
    val targetRole: String = "B組02",
    val priority: Int = 100,
    val status: String = "READY",
    val source: String = "user",
    val payload: JsonElement = JsonObject(emptyMap()),
    val claimedBy: String = "",
    val claimedAt: String = "",
    val completedAt: String = "",
    val resultStatus: String = "",
    val resultSummary: String = "",
    val resultReportPath: String = "",
    val attemptCount: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = "",
)

@Serializable
data class AiTaskQueueCommandResult(
    val verdict: String,
    val status: String,
    val task: AiQueueTask? = null,
    val message: String = "",
    val nextAllowedStep: String = "",
)

@Serializable
data class AiTaskQueueListReport(
    val verdict: String,
    val status: String,
    val filterStatus: String = "",
    val count: Int,
    val tasks: List<AiQueueTask>,
)
