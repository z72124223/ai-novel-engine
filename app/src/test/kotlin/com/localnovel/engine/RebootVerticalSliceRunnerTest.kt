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
            addonPackPath = projectPath("samples/reboot01/chenxing_world_patch_candidate_v1.json"),
            npcProfilePath = projectPath("samples/reboot01/lin_yao_offscreen_profile.json"),
            approvalGrantPath = projectPath("samples/reboot01/irreversible_approval_grant.json"),
        )

        assertEquals(6, summary.appliedTurns)
        assertEquals(6, summary.stateVersion)
        assertEquals(35, summary.elapsedMinutes)
        assertEquals(1, summary.decisionTraceCount)
        assertEquals(6, summary.dramaEvaluationCount)
        assertEquals(6, summary.runEvidenceCount)
        assertEquals(1, summary.irreversibleApprovalCount)
        assertTrue(Files.exists(output.resolve("turn-02-decision-trace-row.json")))
        assertTrue(Files.exists(output.resolve("turn-05-drama.json")))
        assertTrue(Files.exists(output.resolve("sql-state-snapshot.json")))
        assertTrue(Files.exists(output.resolve("world-patch-parse.json")))
        assertTrue(Files.exists(output.resolve("world-patch-validation.json")))
        assertTrue(Files.exists(output.resolve("world-patch-diff.json")))
        assertTrue(Files.exists(output.resolve("world-patch-impact-preview.json")))
        assertTrue(Files.exists(output.resolve("world-patch-sandbox-apply.json")))
        assertTrue(Files.exists(output.resolve("protagonist-growth-evidence.json")))
        assertTrue(Files.readString(output.resolve("world-patch-validation.json")).contains("\"valid\": true"))
        assertTrue(Files.readString(output.resolve("world-patch-sandbox-apply.json")).contains("reboot01-chenxing-world-patch-candidate-v1"))
        assertTrue(Files.readString(output.resolve("turn-01-context.json")).contains("rule-sandbox-evidence-has-social-cost"))
        assertTrue(Files.readString(output.resolve("turn-01-context.json")).contains("fact-sandbox-shelter-pressure"))
        assertTrue(Files.readString(output.resolve("turn-06-reader-narrative.md")).contains("愛醬"))
        assertTrue(Files.readString(output.resolve("turn-03-reader-narrative.md")).contains("保固是不是只到退潮"))
        assertTrue(Files.readString(output.resolve("turn-05-reader-narrative.md")).contains("海浪沒有替任何人辯解"))
        assertTrue(Files.readString(output.resolve("turn-06-reader-narrative.md")).contains("朝山坡更深處"))
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

    @Test
    fun runnerRejectsAnAddonThatIsNotTheExactResettableNonCanonWorldPatch() {
        val root = Files.createTempDirectory("reboot01-invalid-world-patch")
        val failure = assertFailsWith<IllegalArgumentException> {
            RebootVerticalSliceRunner(SqliteStoryRepository(root.resolve("story.db")), root.resolve("evidence")).run(
                basePackPath = projectPath("world/chenxing/engine/chenxing_setting_pack_v1.json"),
                addonPackPath = projectPath("samples/reboot01/chenxing_vertical_slice_addon.json"),
                npcProfilePath = projectPath("samples/reboot01/lin_yao_offscreen_profile.json"),
                approvalGrantPath = projectPath("samples/reboot01/irreversible_approval_grant.json"),
            )
        }
        assertEquals("REBOOT_WORLD_PATCH_ID_REQUIRED", failure.message)
    }

    private fun projectPath(relative: String): Path {
        val candidates = listOf(Path.of(relative), Path.of("..", relative))
        return candidates.firstOrNull { Files.exists(it) } ?: error("Cannot find project path: $relative")
    }
}
