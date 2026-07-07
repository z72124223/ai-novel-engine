package com.localnovel.engine.service

import com.localnovel.engine.model.AiQueueTask
import com.localnovel.engine.model.AiTaskQueueCommandResult
import com.localnovel.engine.model.AiTaskQueueListReport
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.time.Instant
import java.util.UUID

class AiTaskQueueService(
    private val repository: SqliteStoryRepository,
) {
    fun upsertTask(
        taskId: String,
        title: String,
        payloadJson: String = "{}",
        taskType: String = "implementation",
        ownerRole: String = "B組01",
        targetRole: String = "B組02",
        priority: Int = 100,
        status: String = "READY",
        source: String = "user",
        actor: String = "user",
    ): AiTaskQueueCommandResult {
        repository.initialize()
        require(taskId.isNotBlank()) { "taskId must not be blank." }
        require(title.isNotBlank()) { "title must not be blank." }
        val payload = parsePayload(payloadJson)
        val now = Instant.now().toString()
        val task = repository.withTransaction { connection ->
            val existing = repository.queryRows(
                connection,
                "SELECT created_at, attempt_count FROM ai_task_queue WHERE task_id = ?",
                taskId,
            ).firstOrNull()
            repository.insertRawRow(
                connection = connection,
                tableName = "ai_task_queue",
                row = mapOf(
                    "task_id" to taskId,
                    "title" to title,
                    "task_type" to taskType,
                    "owner_role" to ownerRole,
                    "target_role" to targetRole,
                    "priority" to priority.toString(),
                    "status" to status,
                    "source" to source,
                    "payload_json" to JsonSupport.json.encodeToString(JsonElement.serializer(), payload),
                    "claimed_by" to null,
                    "claimed_at" to null,
                    "completed_at" to null,
                    "result_status" to null,
                    "result_summary" to null,
                    "result_report_path" to null,
                    "attempt_count" to (existing?.get("attempt_count") ?: "0"),
                    "created_at" to (existing?.get("created_at") ?: now),
                    "updated_at" to now,
                ),
            )
            insertEvent(connection, taskId, "UPSERT", actor, "Task upserted.", payloadJson)
            rowToTask(
                repository.queryRows(connection, "SELECT * FROM ai_task_queue WHERE task_id = ?", taskId).first(),
            )
        }
        return AiTaskQueueCommandResult(
            verdict = "PASS",
            status = "AI_TASK_UPSERTED",
            task = task,
            nextAllowedStep = "claim-ai-task",
        )
    }

    fun claimNextTask(
        worker: String,
        targetRole: String = "",
    ): AiTaskQueueCommandResult {
        repository.initialize()
        require(worker.isNotBlank()) { "worker must not be blank." }
        val now = Instant.now().toString()
        val task = repository.withTransaction { connection ->
            val rows = if (targetRole.isBlank()) {
                repository.queryRows(
                    connection,
                    """
                    SELECT * FROM ai_task_queue
                    WHERE status = 'READY'
                    ORDER BY priority ASC, created_at ASC
                    LIMIT 1
                    """.trimIndent(),
                )
            } else {
                repository.queryRows(
                    connection,
                    """
                    SELECT * FROM ai_task_queue
                    WHERE status = 'READY' AND (target_role = ? OR target_role = '')
                    ORDER BY priority ASC, created_at ASC
                    LIMIT 1
                    """.trimIndent(),
                    targetRole,
                )
            }
            val row = rows.firstOrNull() ?: return@withTransaction null
            val taskId = row["task_id"].orEmpty()
            val attempt = (row["attempt_count"]?.toIntOrNull() ?: 0) + 1
            repository.executeUpdate(
                connection,
                """
                UPDATE ai_task_queue
                SET status = 'IN_PROGRESS',
                    claimed_by = ?,
                    claimed_at = ?,
                    attempt_count = ?,
                    updated_at = ?
                WHERE task_id = ? AND status = 'READY'
                """.trimIndent(),
                worker,
                now,
                attempt.toString(),
                now,
                taskId,
            )
            insertEvent(connection, taskId, "CLAIM", worker, "Task claimed by $worker.", "{}")
            rowToTask(repository.queryRows(connection, "SELECT * FROM ai_task_queue WHERE task_id = ?", taskId).first())
        }
        return if (task == null) {
            AiTaskQueueCommandResult(
                verdict = "PASS",
                status = "AI_TASK_QUEUE_EMPTY",
                message = "No READY task is available.",
                nextAllowedStep = "wait_or_upsert_ai_task",
            )
        } else {
            AiTaskQueueCommandResult(
                verdict = "PASS",
                status = "AI_TASK_CLAIMED",
                task = task,
                nextAllowedStep = "complete-ai-task",
            )
        }
    }

    fun completeTask(
        taskId: String,
        worker: String,
        resultStatus: String,
        summary: String = "",
        reportPath: String = "",
    ): AiTaskQueueCommandResult {
        repository.initialize()
        require(taskId.isNotBlank()) { "taskId must not be blank." }
        require(worker.isNotBlank()) { "worker must not be blank." }
        require(resultStatus in setOf("PASS", "FAIL", "NEEDS_REVISION", "BLOCKED")) {
            "result must be PASS, FAIL, NEEDS_REVISION, or BLOCKED."
        }
        val now = Instant.now().toString()
        val task = repository.withTransaction { connection ->
            val existing = repository.queryRows(connection, "SELECT * FROM ai_task_queue WHERE task_id = ?", taskId)
                .firstOrNull()
                ?: error("Task not found: $taskId")
            val finalStatus = if (resultStatus == "PASS") "DONE" else resultStatus
            repository.executeUpdate(
                connection,
                """
                UPDATE ai_task_queue
                SET status = ?,
                    completed_at = ?,
                    result_status = ?,
                    result_summary = ?,
                    result_report_path = ?,
                    updated_at = ?
                WHERE task_id = ?
                """.trimIndent(),
                finalStatus,
                now,
                resultStatus,
                summary,
                reportPath,
                now,
                taskId,
            )
            val note = if (summary.isBlank()) "Task completed with $resultStatus." else summary
            insertEvent(connection, taskId, "COMPLETE", worker, note, existing["payload_json"].orEmpty())
            rowToTask(repository.queryRows(connection, "SELECT * FROM ai_task_queue WHERE task_id = ?", taskId).first())
        }
        return AiTaskQueueCommandResult(
            verdict = "PASS",
            status = "AI_TASK_COMPLETED",
            task = task,
            nextAllowedStep = "claim-ai-task",
        )
    }

    fun listTasks(status: String = ""): AiTaskQueueListReport {
        repository.initialize()
        val tasks = repository.withConnection { connection ->
            val rows = if (status.isBlank()) {
                repository.queryRows(
                    connection,
                    "SELECT * FROM ai_task_queue ORDER BY status, priority ASC, created_at ASC",
                )
            } else {
                repository.queryRows(
                    connection,
                    "SELECT * FROM ai_task_queue WHERE status = ? ORDER BY priority ASC, created_at ASC",
                    status,
                )
            }
            rows.map(::rowToTask)
        }
        return AiTaskQueueListReport(
            verdict = "PASS",
            status = "AI_TASK_QUEUE_LIST",
            filterStatus = status,
            count = tasks.size,
            tasks = tasks,
        )
    }

    private fun parsePayload(payloadJson: String): JsonElement =
        if (payloadJson.isBlank()) JsonObject(emptyMap()) else JsonSupport.json.parseToJsonElement(payloadJson)

    private fun insertEvent(
        connection: java.sql.Connection,
        taskId: String,
        eventType: String,
        actor: String,
        note: String,
        payloadJson: String,
    ) {
        repository.insertRawRow(
            connection = connection,
            tableName = "ai_task_events",
            row = mapOf(
                "event_id" to UUID.randomUUID().toString(),
                "task_id" to taskId,
                "event_type" to eventType,
                "actor" to actor,
                "note" to note,
                "payload_json" to payloadJson.ifBlank { "{}" },
                "created_at" to Instant.now().toString(),
            ),
        )
    }

    private fun rowToTask(row: Map<String, String?>): AiQueueTask =
        AiQueueTask(
            taskId = row["task_id"].orEmpty(),
            title = row["title"].orEmpty(),
            taskType = row["task_type"].orEmpty(),
            ownerRole = row["owner_role"].orEmpty(),
            targetRole = row["target_role"].orEmpty(),
            priority = row["priority"]?.toIntOrNull() ?: 100,
            status = row["status"].orEmpty(),
            source = row["source"].orEmpty(),
            payload = parsePayload(row["payload_json"].orEmpty()),
            claimedBy = row["claimed_by"].orEmpty(),
            claimedAt = row["claimed_at"].orEmpty(),
            completedAt = row["completed_at"].orEmpty(),
            resultStatus = row["result_status"].orEmpty(),
            resultSummary = row["result_summary"].orEmpty(),
            resultReportPath = row["result_report_path"].orEmpty(),
            attemptCount = row["attempt_count"]?.toIntOrNull() ?: 0,
            createdAt = row["created_at"].orEmpty(),
            updatedAt = row["updated_at"].orEmpty(),
        )
}
