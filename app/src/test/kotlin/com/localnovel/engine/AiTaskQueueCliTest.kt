package com.localnovel.engine

import com.localnovel.engine.model.AiTaskQueueCommandResult
import com.localnovel.engine.model.AiTaskQueueListReport
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class AiTaskQueueCliTest {
    @Test
    fun cliSupportsTaskQueueLifecycle() {
        val tempDir = Files.createTempDirectory("ai-task-queue-cli")
        val dbPath = tempDir.resolve("story.db")
        val payloadPath = tempDir.resolve("payload.json")
        val claimOut = tempDir.resolve("claim.json")
        val completeOut = tempDir.resolve("complete.json")
        val listOut = tempDir.resolve("list.json")
        Files.writeString(payloadPath, """{"next":"query-world-source","q":"愛醬"}""")

        val upsertExit = Cli().run(
            listOf(
                "upsert-ai-task",
                "--db",
                dbPath.toString(),
                "--task",
                "task-aijiang-query",
                "--title",
                "查詢愛醬正式來源資料",
                "--payload",
                payloadPath.toString(),
                "--target",
                "B組02",
                "--priority",
                "5",
            ),
        )
        assertEquals(0, upsertExit)

        val claimExit = Cli().run(
            listOf(
                "claim-ai-task",
                "--db",
                dbPath.toString(),
                "--worker",
                "B組02",
                "--target",
                "B組02",
                "--out",
                claimOut.toString(),
            ),
        )
        assertEquals(0, claimExit)
        val claim = readJson<AiTaskQueueCommandResult>(claimOut)
        assertEquals("AI_TASK_CLAIMED", claim.status)
        assertEquals("task-aijiang-query", claim.task?.taskId)
        assertEquals("IN_PROGRESS", claim.task?.status)

        val completeExit = Cli().run(
            listOf(
                "complete-ai-task",
                "--db",
                dbPath.toString(),
                "--task",
                "task-aijiang-query",
                "--worker",
                "B組02",
                "--result",
                "PASS",
                "--summary",
                "查詢完成",
                "--report",
                "memory/task-aijiang-query-report.json",
                "--out",
                completeOut.toString(),
            ),
        )
        assertEquals(0, completeExit)
        val complete = readJson<AiTaskQueueCommandResult>(completeOut)
        assertEquals("AI_TASK_COMPLETED", complete.status)
        assertEquals("DONE", complete.task?.status)

        val listExit = Cli().run(
            listOf(
                "list-ai-tasks",
                "--db",
                dbPath.toString(),
                "--status",
                "DONE",
                "--out",
                listOut.toString(),
            ),
        )
        assertEquals(0, listExit)
        val list = readJson<AiTaskQueueListReport>(listOut)
        assertEquals(1, list.count)
        assertTrue(list.tasks.any { it.taskId == "task-aijiang-query" })
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
}
