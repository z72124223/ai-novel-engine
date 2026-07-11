package com.localnovel.engine

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.DemoProjectSeed
import com.localnovel.engine.model.NpcActionProposal
import com.localnovel.engine.model.NpcAutonomyProfile
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.service.OffscreenSimulationService
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class NpcAutonomyTest {
    @Test
    fun migrationIsAdditiveAndIdempotent() {
        val repository = seededOffscreenRepository()
        repository.initialize()
        assertTrue("npc_decision_traces" in repository.listTables())
        assertTrue("sandbox_world_patch_candidates" in repository.listTables())
    }

    @Test
    fun playerAbsenceAllowsBoundedNpcActionAndObservableReturnConsequence() {
        val repository = seededOffscreenRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        movePlayerAway(runtime, session.id)

        val result = OffscreenSimulationService(repository).run(
            sessionId = session.id,
            profile = profile(),
            scenarioSeed = "stage2-accepted-seed",
        )

        assertTrue(result.accepted, repository.tableRows("npc_decision_traces").toString())
        assertEquals("APPLIED", result.status)
        val trace = repository.tableRows("npc_decision_traces").single()
        assertEquals("APPLIED", trace["status"])
        assertEquals("char-shen-shuang", trace["npc_id"])
        assertTrue(trace["decision_trace_json"].orEmpty().contains("activeGoal"))
        assertTrue(trace["decision_trace_json"].orEmpty().contains("offscreenReason"))
        assertEquals(1, repository.tableCount("events"))
        val patch = repository.tableRows("sandbox_world_patch_candidates").single()
        assertEquals("SANDBOX_APPLIED", patch["status"])
        assertEquals("0", patch["canon_mutation_allowed"])
        assertEquals(2, repository.appliedTurnCount(session.id))
        val sessionRow = repository.tableRows("story_sessions").single { it["id"] == session.id }
        assertEquals("15", sessionRow["elapsed_minutes"])

        val returnContext = runtime.prepareTurn(session.id, "I return to the corridor and look for changes.")
        assertTrue(returnContext.recentEvents.any { it.summary.contains("off-screen search") })
        repository.abortPendingContext(session.id, returnContext.contextId)
    }

    @Test
    fun illegalForeignKnowledgeActionIsRejectedButTracePersists() {
        val repository = seededOffscreenRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        movePlayerAway(runtime, session.id)
        val invalid = NpcActionProposal(
            proposalId = "invalid-foreign-knowledge",
            npcId = "char-shen-shuang",
            actionType = "inspect_local_pressure",
            intent = "Use knowledge that belongs to somebody else.",
            visibleAction = "Acts on unsupported information.",
            hiddenMotive = "Convenience for the player.",
            locationId = "loc-shrine-corridor",
            usedKnowledgeClaimIds = listOf("foreign-claim"),
            predictedCost = "none",
            observableConsequence = "An invalid consequence.",
        )

        val result = OffscreenSimulationService(repository).run(
            sessionId = session.id,
            profile = profile(),
            scenarioSeed = "stage2-rejected-seed",
            proposalOverride = invalid,
        )

        assertTrue(!result.accepted)
        assertEquals("REJECTED_WORLD_LEGALITY", result.status)
        val trace = repository.tableRows("npc_decision_traces").single()
        assertEquals("REJECTED", trace["status"])
        assertTrue(trace["resolution_json"].orEmpty().contains("foreign_knowledge_reference"))
        assertEquals(0, repository.tableCount("events"))
        assertEquals(1, repository.appliedTurnCount(session.id))
    }

    @Test
    fun npcTraceFailureRollsBackFormalTick() {
        val repository = seededOffscreenRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        movePlayerAway(runtime, session.id)
        DriverManager.getConnection("jdbc:sqlite:${repository.databasePath()}").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(
                    """
                    CREATE TRIGGER fail_npc_trace BEFORE INSERT ON npc_decision_traces
                    BEGIN SELECT RAISE(ABORT, 'forced npc trace rollback'); END
                    """.trimIndent(),
                )
            }
        }

        assertFailsWith<Exception> {
            OffscreenSimulationService(repository).run(session.id, profile(), "stage2-rollback-seed")
        }

        assertEquals(0, repository.tableCount("npc_decision_traces"))
        assertEquals(0, repository.tableCount("events"))
        assertEquals(1, repository.appliedTurnCount(session.id))
        assertTrue(repository.tableRows("reboot_run_evidence").any { it["status"] == "APPLY_FAILED" })
    }

    @Test
    fun cliRunsOneOffscreenTickAndWritesResult() {
        val repository = seededOffscreenRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        movePlayerAway(runtime, session.id)
        val out = Files.createTempFile("offscreen-tick", ".json")

        val exit = Cli().run(
            listOf(
                "run-offscreen-npc-tick",
                "--db",
                repository.databasePath().toString(),
                "--session",
                session.id,
                "--profile",
                samplePath("reboot01/shen_shuang_offscreen_profile.json").toString(),
                "--scenario-seed",
                "stage2-cli-seed",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.readString(out).contains("\"accepted\": true"))
        assertEquals(1, repository.tableCount("npc_decision_traces"))
    }

    private fun seededOffscreenRepository(): SqliteStoryRepository {
        val repository = SqliteStoryRepository(Files.createTempDirectory("npc-autonomy-test").resolve("story.db"))
        repository.initialize()
        val seed = readJson<DemoProjectSeed>(samplePath("demo_project_seed.json"))
        val movedPlayer = seed.copy(
            characterStates = seed.characterStates.map { state ->
                if (state.characterId == "char-player") state.copy(locationId = "loc-shrine-rear-hall") else state
            },
        )
        repository.seedDemo(movedPlayer)
        return repository
    }

    private fun movePlayerAway(runtime: MultiTurnRuntime, sessionId: String) {
        val context = runtime.prepareTurn(sessionId, "I leave the corridor before anyone acts off-screen.")
        runtime.resolveTurn(
            sessionId,
            AiTurnResult(
                protocolVersion = context.protocolVersion,
                projectId = context.project.id,
                turnId = context.turnId,
                resultId = "move-player-${context.contextId}",
                contextId = context.contextId,
                sessionId = context.sessionId,
                turnNumber = context.turnNumber,
                baseStateVersion = context.baseStateVersion,
                contextHash = context.contextHash,
                narrative = "The player leaves the corridor for the rear hall.",
                turnSummary = "The player moves away before the off-screen tick.",
                characterActions = listOf(
                    CharacterAction(
                        characterId = "char-player",
                        intent = "Leave the corridor.",
                        visibleAction = "Walks to the rear hall.",
                        hiddenMotive = "Create distance before deciding what to reveal.",
                        characterFitScore = 90,
                        storyProgressScore = 70,
                    ),
                ),
                stateDeltas = listOf(
                    DeltaOperation(
                        type = DeltaOperationType.UPDATE_CHARACTER_STATE,
                        targetType = "character_state",
                        targetId = "char-player",
                        payload = JsonObject(mapOf("locationId" to JsonPrimitive("loc-shrine-rear-hall"))),
                    ),
                ),
            ),
        )
    }

    private fun profile(): NpcAutonomyProfile =
        readJson(samplePath("reboot01/shen_shuang_offscreen_profile.json"))

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private fun samplePath(relative: String): Path {
        val candidates = listOf(Path.of("samples", relative), Path.of("..", "samples", relative))
        return candidates.firstOrNull { Files.exists(it) } ?: error("Cannot find sample: $relative")
    }
}
