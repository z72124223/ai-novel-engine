package com.localnovel.engine

import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.RebootVerticalSliceRunner
import com.localnovel.engine.service.RebootIrreversibleApprovalGrant
import com.localnovel.engine.model.IrreversibleChangeContract
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import kotlinx.serialization.decodeFromString

class RebootVerticalSliceRunnerTest {
    @Test
    fun runsSixTurnResettableChenxingSliceWithSqlEvidence() {
        val root = Files.createTempDirectory("reboot01-vertical-slice")
        val repository = SqliteStoryRepository(root.resolve("story.db"))
        val output = root.resolve("evidence")

        val summary = RebootVerticalSliceRunner(repository, output).run(
            basePackPath = projectPath("world/chenxing/engine/chenxing_setting_pack_v1.json"),
            addonPackPath = projectPath("samples/reboot01/chenxing_vertical_slice_addon.json"),
            npcProfilePath = projectPath("samples/reboot01/lin_yao_offscreen_profile.json"),
            approvalGrantPath = projectPath("samples/reboot01/irreversible_approval_grant.json"),
        )

        assertEquals(6, summary.appliedTurns)
        assertEquals(6, summary.stateVersion)
        assertEquals(35, summary.elapsedMinutes)
        assertEquals(1, summary.decisionTraceCount)
        assertEquals(5, summary.dramaEvaluationCount)
        assertEquals(6, summary.runEvidenceCount)
        assertEquals(1, summary.irreversibleApprovalCount)
        assertTrue(Files.exists(output.resolve("turn-02-decision-trace-row.json")))
        assertTrue(Files.exists(output.resolve("turn-05-drama.json")))
        assertTrue(Files.exists(output.resolve("sql-state-snapshot.json")))
        assertTrue(Files.readString(output.resolve("turn-06-reader-narrative.md")).contains("愛醬"))
        val antagonist = repository.tableRows("character_states").single { it["character_id"] == "char-tide-scavenger-sandbox" }
        assertEquals("permanent_hand_injury", antagonist["health"])
        assertTrue(repository.tableRows("sandbox_world_patch_candidates").all { it["canon_mutation_allowed"] == "0" })
    }

    @Test
    fun irreversibleGrantMustBeSeparateAndExactlyAuthorizeTheContract() {
        val grant = JsonSupport.json.decodeFromString<RebootIrreversibleApprovalGrant>(
            Files.readString(projectPath("samples/reboot01/irreversible_approval_grant.json")),
        )
        val contract = IrreversibleChangeContract(
            changeId = "sandbox-permanent-hand-injury",
            changeTypes = listOf("permanent_injury"),
            summary = "sandbox",
            contextHash = "context",
            resultId = "result",
            affectedEntityIds = listOf("char-tide-scavenger-sandbox"),
            requiredStateDeltaTargetIds = listOf("char-tide-scavenger-sandbox"),
        )
        grant.requireAuthorizes(contract, "reboot01-chenxing-depth-v1")
        assertFailsWith<IllegalArgumentException> {
            grant.copy(decision = "REJECT").requireAuthorizes(contract, "reboot01-chenxing-depth-v1")
        }
        assertFailsWith<IllegalArgumentException> {
            grant.copy(affectedEntityIds = listOf("char-lu-chenxing")).requireAuthorizes(contract, "reboot01-chenxing-depth-v1")
        }
    }

    private fun projectPath(relative: String): Path {
        val candidates = listOf(Path.of(relative), Path.of("..", relative))
        return candidates.firstOrNull { Files.exists(it) } ?: error("Cannot find project path: $relative")
    }
}
