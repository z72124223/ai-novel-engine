package com.localnovel.engine

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.DemoProjectSeed
import com.localnovel.engine.model.RevealedFact
import com.localnovel.engine.model.SceneProgressUpdate
import com.localnovel.engine.model.StateDelta
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.AiTurnResultValidator
import com.localnovel.engine.service.ContextCompiler
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.service.StallDetector
import com.localnovel.engine.service.StateReducer
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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
            "story_sessions",
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

    @Test
    fun prepareTurnDoesNotChangeFormalStoryState() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")

        runtime.prepareTurn(session.id, "我只是觀察。")

        assertEquals(0, repository.getProject("demo")?.currentTurn)
        assertEquals(0, repository.getSession(session.id)?.stateVersion)
        assertEquals(1, repository.getSession(session.id)?.nextTurnNumber)
        assertEquals(0, repository.appliedTurnCount(session.id))
    }

    @Test
    fun multiTurnResolveAdvancesStateVersionOnceAndRejectsDuplicateResult() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我質問沈霜。")
        val result = validMultiTurnResult(context, "result-001")

        runtime.resolveTurn(session.id, result)

        assertEquals(1, repository.getSession(session.id)?.stateVersion)
        assertEquals(2, repository.getSession(session.id)?.nextTurnNumber)
        assertEquals(1, repository.appliedTurnCount(session.id))
        val duplicate = runtime.validate(context, result)
        assertFalse(duplicate.isValid)
        assertTrue(duplicate.errors.any { it.code == "duplicate_result" || it.code == "stale_state" })
    }

    @Test
    fun multiTurnRejectsStaleHashAndTurnMismatch() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我質問沈霜。")

        val stale = runtime.validate(context, validMultiTurnResult(context.copy(baseStateVersion = 99), "result-stale"))
        val badHash = runtime.validate(context, validMultiTurnResult(context, "result-hash").copy(contextHash = "bad"))
        val badTurn = runtime.validate(context, validMultiTurnResult(context, "result-turn").copy(turnNumber = 99))

        assertFalse(stale.isValid)
        assertFalse(badHash.isValid)
        assertFalse(badTurn.isValid)
        assertTrue(badHash.errors.any { it.code == "hash_mismatch" })
        assertTrue(badTurn.errors.any { it.code == "turn_mismatch" })
    }

    @Test
    fun illegalDeltaPreventsAnyWrites() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我質問沈霜。")
        val beforeFacts = repository.tableCount("facts")
        val beforeDeltas = repository.tableCount("state_deltas")
        val invalid = validMultiTurnResult(context, "result-invalid").copy(
            stateDeltas = listOf(
                DeltaOperation(
                    type = DeltaOperationType.ADD_FACT,
                    targetType = "fact",
                    targetId = "fact-should-not-write",
                    payload = JsonObject(mapOf("content" to JsonPrimitive("這筆不應寫入。"))),
                ),
                DeltaOperation(
                    type = DeltaOperationType.REVEAL_SECRET,
                    targetType = "secret",
                    targetId = "secret-breathing-coffin",
                    payload = JsonObject(emptyMap()),
                ),
            ),
        )

        val report = runtime.validate(context, invalid)

        assertFalse(report.isValid)
        assertEquals(beforeFacts, repository.tableCount("facts"))
        assertEquals(beforeDeltas, repository.tableCount("state_deltas"))
    }

    @Test
    fun transactionRollsBackDeltasWhenTurnLogWriteFails() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val first = runtime.prepareTurn(session.id, "第一回。")
        val firstResult = validMultiTurnResult(first, "result-same")
        runtime.resolveTurn(session.id, firstResult)

        val second = runtime.prepareTurn(session.id, "第二回。")
        val duplicateResult = validMultiTurnResult(second, "result-same")
        val beforeDeltas = repository.tableCount("state_deltas")

        runCatching { repository.resolvePendingTurn(second, duplicateResult, true) }

        assertEquals(beforeDeltas, repository.tableCount("state_deltas"))
        assertEquals(1, repository.appliedTurnCount(session.id))
    }

    @Test
    fun forbiddenRevealIsRejectedAcrossAllResultSurfaces() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我追問。")
        val forbiddenPhrase = "祠堂下面鎮著一口會呼吸的黑棺。"
        val result = validMultiTurnResult(context, "result-forbidden").copy(
            narrative = "沈霜沒有說破。",
            revealedFacts = listOf(RevealedFact("fact-x", forbiddenPhrase)),
            memoryEffects = listOf(com.localnovel.engine.model.MemoryEffect("char-shen-shuang", "memory-shen-shuang-vow", forbiddenPhrase)),
            newTasks = listOf(com.localnovel.engine.model.Task("task-x", "demo", forbiddenPhrase)),
            stateDeltas = listOf(
                DeltaOperation(
                    type = DeltaOperationType.ADD_FACT,
                    targetType = "fact",
                    targetId = "fact-x",
                    payload = JsonObject(mapOf("content" to JsonPrimitive(forbiddenPhrase))),
                ),
            ),
        )

        val report = runtime.validate(context, result)

        assertFalse(report.isValid)
        assertTrue(report.errors.any { it.code == "forbidden_reveal" })
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

    private fun validMultiTurnResult(context: ContextPacket, resultId: String): AiTurnResult =
        AiTurnResult(
            protocolVersion = "2",
            projectId = context.project.id,
            turnId = context.turnId,
            resultId = resultId,
            contextId = context.contextId,
            sessionId = context.sessionId,
            turnNumber = context.turnNumber,
            baseStateVersion = context.baseStateVersion,
            contextHash = context.contextHash,
            narrative = "沈霜沒有順著禁忌說下去，只承認昨夜後殿門閂被人動過。",
            turnSummary = "沈霜承認後殿門閂異常，但沒有揭露禁忌。",
            characterActions = listOf(
                CharacterAction(
                    characterId = "char-shen-shuang",
                    intent = "守住後殿並控制回答邊界",
                    visibleAction = "沈霜擋住門，只承認門閂異常。",
                    hiddenMotive = "避免外人知道祠堂地底的禁忌。",
                    triggeringConditions = listOf("玩家追問祠堂下方"),
                    usedMemoryIds = listOf("memory-shen-shuang-vow"),
                    usedItemIds = listOf("item-copper-token"),
                    usedTechniqueIds = emptyList(),
                    characterFitScore = 88,
                    storyProgressScore = 76,
                ),
            ),
            stateDeltas = listOf(
                DeltaOperation(
                    type = DeltaOperationType.REVEAL_SECRET,
                    targetType = "secret",
                    targetId = "secret-door-bolt-moved",
                    payload = JsonObject(mapOf("reason" to JsonPrimitive("場景合約允許揭露門閂異常。"))),
                ),
                DeltaOperation(
                    type = DeltaOperationType.ADD_FACT,
                    targetType = "fact",
                    targetId = "fact-door-bolt-moved-${context.turnNumber}",
                    payload = JsonObject(
                        mapOf(
                            "content" to JsonPrimitive("沈霜承認昨夜後殿門閂被人動過。"),
                            "source" to JsonPrimitive("ai_turn_result"),
                        ),
                    ),
                ),
            ),
            revealedFacts = listOf(RevealedFact("fact-door-bolt-moved-${context.turnNumber}", "沈霜承認昨夜後殿門閂被人動過。")),
            sceneProgressUpdates = listOf(SceneProgressUpdate("沈霜可以承認昨夜後殿門閂被人動過。", "completed", "沈霜承認門閂異常。")),
        )

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
