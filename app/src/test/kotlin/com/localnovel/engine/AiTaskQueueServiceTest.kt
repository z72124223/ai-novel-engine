package com.localnovel.engine

import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.AiTaskQueueService
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AiTaskQueueServiceTest {
    @Test
    fun upsertClaimCompleteAndEmptyQueue() {
        val repository = SqliteStoryRepository(Files.createTempDirectory("ai-task-queue-service").resolve("story.db"))
        val service = AiTaskQueueService(repository)

        val upsert = service.upsertTask(
            taskId = "task-world-source-query-check",
            title = "確認世界觀 SQL 查詢入口",
            payloadJson = """{"route":"world_source_sql","command":"query-world-source"}""",
            targetRole = "B組02",
            priority = 10,
        )
        assertEquals("AI_TASK_UPSERTED", upsert.status)
        assertEquals("READY", upsert.task?.status)

        val claim = service.claimNextTask(worker = "B組02", targetRole = "B組02")
        assertEquals("AI_TASK_CLAIMED", claim.status)
        assertEquals("task-world-source-query-check", claim.task?.taskId)
        assertEquals("IN_PROGRESS", claim.task?.status)
        assertEquals(1, claim.task?.attemptCount)

        val completed = service.completeTask(
            taskId = "task-world-source-query-check",
            worker = "B組02",
            resultStatus = "PASS",
            summary = "SQL query entrypoint checked.",
            reportPath = "memory/task-world-source-query-check-report.json",
        )
        assertEquals("AI_TASK_COMPLETED", completed.status)
        assertEquals("DONE", completed.task?.status)
        assertEquals("PASS", completed.task?.resultStatus)
        assertEquals("memory/task-world-source-query-check-report.json", completed.task?.resultReportPath)

        val empty = service.claimNextTask(worker = "B組02", targetRole = "B組02")
        assertEquals("AI_TASK_QUEUE_EMPTY", empty.status)
        assertEquals(null, empty.task)

        val done = service.listTasks(status = "DONE")
        assertEquals(1, done.count)
        assertNotNull(done.tasks.firstOrNull { it.taskId == "task-world-source-query-check" })
        assertEquals(3, repository.tableRows("ai_task_events").size)
    }
}
