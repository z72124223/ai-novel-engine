package com.localnovel.engine

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.CheckpointService
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.service.ReplayAuditService
import com.localnovel.engine.service.TimelineBranchService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Mvp6CheckpointBranchingRuntimeTest {
    @Test
    fun schemaInitializesCheckpointBranchingTables() {
        val repository = newRepository()
        repository.initialize()

        assertTrue(
            repository.listTables().containsAll(
                setOf("timeline_branches", "session_checkpoints", "timeline_events", "replay_audit_runs"),
            ),
        )
    }

    @Test
    fun startSessionCreatesActiveTimelineBranch() {
        val repository = seededRepository()
        val session = MultiTurnRuntime(repository).startSession("demo", "scene-shrine-questioning")

        assertNotNull(session.branchId)
        val branch = repository.tableRows("timeline_branches").single { it["id"] == session.branchId }
        assertEquals("ACTIVE", branch["status"])
        assertEquals(session.id, branch["active_session_id"])
    }

    @Test
    fun createCheckpointStoresStableSnapshotWithoutAdvancingSession() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "第一回。")
        runtime.resolveTurn(session.id, resultFor(context, "mvp6-turn-1"))
        val before = repository.getSession(session.id)!!

        val checkpoint = CheckpointService(repository).createCheckpoint(session.id, "after-turn-1")
        val after = repository.getSession(session.id)!!
        val shown = CheckpointService(repository).showCheckpoint(checkpoint.id)

        assertEquals(before.stateVersion, after.stateVersion)
        assertEquals(before.nextTurnNumber, after.nextTurnNumber)
        assertEquals(1, checkpoint.turnNumber)
        assertEquals(1, checkpoint.stateVersion)
        assertEquals(checkpoint.snapshotHash, shown.snapshotHash)
        assertFalse(JsonSupport.json.encodeToString(shown).contains("祠堂下面鎮著一口會呼吸的黑棺"))
    }

    @Test
    fun forkSessionCreatesNewActiveBranchWithoutModifyingSourceTurnLogs() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val first = runtime.prepareTurn(session.id, "第一回。")
        runtime.resolveTurn(session.id, resultFor(first, "mvp6-source-1"))
        val second = runtime.prepareTurn(session.id, "第二回。")
        runtime.resolveTurn(session.id, resultFor(second, "mvp6-source-2"))
        val checkpoint = CheckpointService(repository).createCheckpoint(session.id, "fork-point")
        val third = runtime.prepareTurn(session.id, "原時間線第三回。")
        runtime.resolveTurn(session.id, resultFor(third, "mvp6-source-3"))

        val fork = TimelineBranchService(repository).forkSession(checkpoint.id, "alternate")

        assertEquals(3, repository.tableRows("turn_logs").count { it["session_id"] == session.id && it["validation_status"] == "applied" })
        assertEquals(2, fork.stateVersion)
        assertEquals(3, fork.nextTurnNumber)
        assertNotEquals(session.id, fork.sessionId)
        assertEquals("INACTIVE", repository.tableRows("timeline_branches").single { it["id"] == session.branchId }["status"])
        assertEquals("ACTIVE", repository.tableRows("timeline_branches").single { it["id"] == fork.branchId }["status"])
        assertFailsInactiveSourcePrepare(runtime, session.id)
    }

    @Test
    fun forkedBranchCanResolveDifferentTurnAndReplayAuditPasses() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val first = runtime.prepareTurn(session.id, "第一回。")
        runtime.resolveTurn(session.id, resultFor(first, "mvp6-replay-1"))
        val second = runtime.prepareTurn(session.id, "第二回。")
        runtime.resolveTurn(session.id, resultFor(second, "mvp6-replay-2"))
        val checkpoint = CheckpointService(repository).createCheckpoint(session.id, "fork-point")
        val fork = TimelineBranchService(repository).forkSession(checkpoint.id, "alternate")

        val forkContext = runtime.prepareTurn(fork.sessionId, "分支第三回。")
        runtime.resolveTurn(fork.sessionId, resultFor(forkContext, "mvp6-fork-3"))

        val audit = ReplayAuditService(repository).verify(fork.sessionId)

        assertEquals("PASS", audit.status)
        assertEquals(1, audit.checkedTurnCount)
        assertEquals(4, repository.getSession(fork.sessionId)?.nextTurnNumber)
        assertTrue(repository.tableRows("turn_logs").any { it["session_id"] == fork.sessionId && it["result_id"] == "mvp6-fork-3" })
    }

    @Test
    fun checkoutBranchCreatesPreCheckoutCheckpointAndSwitchesActiveBranch() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val first = runtime.prepareTurn(session.id, "第一回。")
        runtime.resolveTurn(session.id, resultFor(first, "mvp6-checkout-1"))
        val checkpoint = CheckpointService(repository).createCheckpoint(session.id, "source")
        val fork = TimelineBranchService(repository).forkSession(checkpoint.id, "alternate")
        val forkContext = runtime.prepareTurn(fork.sessionId, "分支第二回。")
        runtime.resolveTurn(fork.sessionId, resultFor(forkContext, "mvp6-checkout-fork-2"))

        val checkedOut = TimelineBranchService(repository).checkoutBranch(session.branchId!!)

        assertEquals(session.branchId, checkedOut.id)
        assertEquals("ACTIVE", repository.tableRows("timeline_branches").single { it["id"] == session.branchId }["status"])
        assertEquals("INACTIVE", repository.tableRows("timeline_branches").single { it["id"] == fork.branchId }["status"])
        assertTrue(repository.tableRows("session_checkpoints").any { it["branch_id"] == fork.branchId && it["kind"] == "PRE_CHECKOUT" })
    }

    @Test
    fun exportTimelineRedactsSnapshotAndHiddenSecretContent() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "第一回。")
        runtime.resolveTurn(session.id, resultFor(context, "mvp6-export-1"))
        CheckpointService(repository).createCheckpoint(session.id, "export-point")

        val export = TimelineBranchService(repository).exportTimeline("demo")
        val json = JsonSupport.json.encodeToString(export)

        assertEquals(1, export.branches.size)
        assertEquals(1, export.checkpoints.size)
        assertFalse(json.contains("祠堂下面鎮著一口會呼吸的黑棺"))
        assertFalse(json.contains("snapshot_json"))
    }

    @Test
    fun branchMetadataDoesNotBreakContextBudget() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我觀察沈霜。")

        assertEquals(session.branchId, context.branchId)
        assertTrue(context.contextBudget.actualBytes <= 24576)
    }

    private fun assertFailsInactiveSourcePrepare(runtime: MultiTurnRuntime, sessionId: String) {
        val result = runCatching { runtime.prepareTurn(sessionId, "不該能繼續。") }
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("active timeline branch"))
    }

    private fun newRepository(): SqliteStoryRepository =
        SqliteStoryRepository(Files.createTempDirectory("mvp6-checkpoint-test").resolve("story.db"))

    private fun seededRepository(): SqliteStoryRepository {
        val repository = newRepository()
        repository.initialize()
        repository.seedDemo(JsonSupport.json.decodeFromString(Files.readString(samplePath("demo_project_seed.json"))))
        return repository
    }

    private fun resultFor(context: ContextPacket, resultId: String): AiTurnResult =
        AiTurnResult(
            projectId = context.project.id,
            turnId = context.turnId,
            narrative = "本回合留下可重播的狀態證據。",
            resultId = resultId,
            contextId = context.contextId,
            sessionId = context.sessionId,
            turnNumber = context.turnNumber,
            baseStateVersion = context.baseStateVersion,
            contextHash = context.contextHash,
            turnSummary = "留下分支測試事件。",
            characterActions = listOf(
                CharacterAction(
                    characterId = context.characters.first().id,
                    intent = "保持現場狀態並觀察。",
                    visibleAction = "角色保持位置，確認現場沒有越界行動。",
                    hiddenMotive = "維持安全與連續性。",
                    triggeringConditions = listOf(context.playerInput),
                    characterFitScore = 90,
                    storyProgressScore = 75,
                ),
            ),
            stateDeltas = listOf(
                DeltaOperation(
                    type = DeltaOperationType.ADD_EVENT,
                    targetType = "event",
                    targetId = "event-$resultId",
                    payload = JsonObject(mapOf("summary" to JsonPrimitive("MVP-6 branch test event $resultId."))),
                ),
            ),
        )

    private fun samplePath(fileName: String): Path =
        listOf(
            Path.of("samples", fileName),
            Path.of("..", "samples", fileName),
        ).firstOrNull { Files.exists(it) } ?: error("Cannot find sample file: $fileName")
}
