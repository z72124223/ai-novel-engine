package com.localnovel.engine

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.DemoProjectSeed
import com.localnovel.engine.model.StateDelta
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.AiTurnResultValidator
import com.localnovel.engine.service.ContextCompiler
import com.localnovel.engine.service.StallDetector
import com.localnovel.engine.service.StateReducer
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EngineTest {
    @Test
    fun sqliteSchemaCanInitialize() {
        val repository = newRepository()
        repository.initialize()

        val expectedTables = setOf(
            "projects",
            "characters",
            "character_states",
            "character_memories",
            "world_rules",
            "factions",
            "locations",
            "items",
            "techniques",
            "scenes",
            "scene_contracts",
            "facts",
            "secrets",
            "events",
            "turn_logs",
            "state_deltas",
            "dependency_edges",
            "task_queue",
        )

        assertTrue(repository.listTables().containsAll(expectedTables))
    }

    @Test
    fun demoSeedCanWrite() {
        val repository = seededRepository()

        val project = repository.getProject("demo")
        assertNotNull(project)
        assertEquals("霜祠夜問", project.name)
        assertEquals(2, repository.listCharacters("demo").size)
        assertEquals(2, repository.listSecrets("demo").size)
    }

    @Test
    fun compileContextProducesContextPacket() {
        val repository = seededRepository()

        val context = ContextCompiler(repository).compile("demo", "I try the brass key.")

        assertEquals("demo-turn-1", context.turnId)
        assertEquals("I try the brass key.", context.playerInput)
        assertNotNull(context.activeScene)
        assertTrue(context.characters.isNotEmpty())
        assertTrue(context.characterMemories.isNotEmpty())
        assertTrue(context.characterStates.isNotEmpty())
        assertTrue(context.worldRules.isNotEmpty())
        assertTrue(context.facts.isNotEmpty())
    }

    @Test
    fun validatorBlocksForbiddenReveal() {
        val repository = seededRepository()
        val context = ContextCompiler(repository).compile("demo", "I inspect the floor.")
        val forbidden = AiTurnResult(
            projectId = "demo",
            turnId = context.turnId,
            narrative = "祠堂下面鎮著一口會呼吸的黑棺。",
            stateDelta = StateDelta(),
        )

        val report = AiTurnResultValidator(repository).validate(context, forbidden)

        assertFalse(report.isValid)
        assertTrue(report.errors.any { it.code == "forbidden_reveal" })
    }

    @Test
    fun validatorBlocksForbiddenRevealInsideStateDeltaPayload() {
        val repository = seededRepository()
        val context = ContextCompiler(repository).compile("demo", "I inspect the floor.")
        val forbidden = AiTurnResult(
            projectId = "demo",
            turnId = context.turnId,
            narrative = "沈霜避開了你的問題。",
            stateDelta = StateDelta(
                operations = listOf(
                    DeltaOperation(
                        type = DeltaOperationType.ADD_FACT,
                        targetType = "fact",
                        targetId = "fact-invalid",
                        payload = kotlinx.serialization.json.JsonObject(
                            mapOf(
                                "content" to kotlinx.serialization.json.JsonPrimitive("祠堂下面鎮著一口會呼吸的黑棺。"),
                            ),
                        ),
                    ),
                ),
            ),
        )

        val report = AiTurnResultValidator(repository).validate(context, forbidden)

        assertFalse(report.isValid)
        assertTrue(report.errors.any { it.message.contains("secret-breathing-coffin") })
    }

    @Test
    fun stateReducerAppliesLegalStateDelta() {
        val repository = seededRepository()
        val context = ContextCompiler(repository).compile("demo", "I try the brass key in the archive door.")
        val result = readJson<AiTurnResult>(samplePath("sample_ai_turn_result.json"))
        val report = AiTurnResultValidator(repository).validate(context, result)
        assertTrue(report.isValid, report.errors.joinToString { it.message })

        val applyResult = StateReducer(repository).apply(context, result)

        assertTrue(applyResult.hasStateChange)
        assertEquals(1, repository.getProject("demo")?.currentTurn)
        assertEquals("緊繃", repository.getCharacterState("demo", "char-shen-shuang")?.mood)
        assertTrue(repository.listFacts("demo").any { it.id == "fact-door-bolt-moved" })
        assertTrue(repository.listSecrets("demo").first { it.id == "secret-door-bolt-moved" }.revealed)
    }

    @Test
    fun stallDetectorDetectsTwoTurnsWithoutStateChange() {
        val repository = seededRepository()
        val reducer = StateReducer(repository)
        val compiler = ContextCompiler(repository)

        val firstContext = compiler.compile("demo", "I wait.")
        reducer.apply(
            firstContext,
            AiTurnResult(
                projectId = "demo",
                turnId = firstContext.turnId,
                narrative = "Lin waits and listens.",
                stateDelta = StateDelta(),
            ),
        )

        val secondContext = compiler.compile("demo", "I wait again.")
        reducer.apply(
            secondContext,
            AiTurnResult(
                projectId = "demo",
                turnId = secondContext.turnId,
                narrative = "The hall remains quiet.",
                stateDelta = StateDelta(
                    operations = listOf(DeltaOperation(type = DeltaOperationType.NOOP)),
                ),
            ),
        )

        assertTrue(StallDetector(repository).hasStalled("demo"))
        assertTrue(
            StallDetector().hasTwoConsecutiveNoStateChangeDeltas(
                listOf(StateDelta(), StateDelta(operations = listOf(DeltaOperation(type = DeltaOperationType.NOOP)))),
            ),
        )
    }

    private fun newRepository(): SqliteStoryRepository {
        val dbPath = Files.createTempDirectory("story-engine-test").resolve("story.db")
        return SqliteStoryRepository(dbPath)
    }

    private fun seededRepository(): SqliteStoryRepository {
        val repository = newRepository()
        repository.initialize()
        repository.seedDemo(readJson(samplePath("demo_project_seed.json")))
        return repository
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path))

    private fun samplePath(fileName: String): Path {
        val candidates = listOf(
            Path.of("samples", fileName),
            Path.of("..", "samples", fileName),
        )
        return candidates.firstOrNull { Files.exists(it) }
            ?: error("Cannot find sample file: $fileName")
    }
}
