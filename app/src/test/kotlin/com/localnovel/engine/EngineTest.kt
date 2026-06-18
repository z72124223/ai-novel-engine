package com.localnovel.engine

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.CharacterKnowledgeClaim
import com.localnovel.engine.model.CharacterMemory
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.ContextBudget
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.DemoProjectSeed
import com.localnovel.engine.model.KnowledgeUpdate
import com.localnovel.engine.model.MemoryCandidate
import com.localnovel.engine.model.RevealedFact
import com.localnovel.engine.model.SceneProgressUpdate
import com.localnovel.engine.model.StateDelta
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.AiTurnResultValidator
import com.localnovel.engine.service.ContextBudgeter
import com.localnovel.engine.service.ContextCompiler
import com.localnovel.engine.service.MemoryRetriever
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.service.StallDetector
import com.localnovel.engine.service.StateReducer
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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

    @Test
    fun unknownSecretMemoryIsNotRetrieved() {
        val repository = seededRepository()
        repository.upsertCharacterMemory(
            CharacterMemory(
                id = "memory-hidden-secret",
                projectId = "demo",
                characterId = "char-shen-shuang",
                memoryType = "SEMANTIC",
                summary = "祠堂下面鎮著一口會呼吸的黑棺。",
                salience = 100,
                visibility = "private",
            ),
        )
        val session = MultiTurnRuntime(repository).startSession("demo", "scene-shrine-questioning")

        val context = MultiTurnRuntime(repository).prepareTurn(session.id, "我追問祠堂下面。")

        assertFalse(context.retrievedMemories.any { it.memoryId == "memory-hidden-secret" })
    }

    @Test
    fun authorOnlyMemoryDoesNotEnterCharacterContext() {
        val repository = seededRepository()
        repository.upsertCharacterMemory(
            CharacterMemory(
                id = "memory-author-only",
                projectId = "demo",
                characterId = "char-shen-shuang",
                memoryType = "SEMANTIC",
                summary = "作者才知道的真相。",
                tags = listOf("沈家銅牌"),
                salience = 100,
                visibility = "author_only",
            ),
        )
        val session = MultiTurnRuntime(repository).startSession("demo", "scene-shrine-questioning")

        val context = MultiTurnRuntime(repository).prepareTurn(session.id, "我拿出沈家銅牌。")

        assertFalse(context.retrievedMemories.any { it.memoryId == "memory-author-only" })
        assertFalse(context.characterMemories.any { it.id == "memory-author-only" })
    }

    @Test
    fun memoryRankingIsDeterministicForSameInputAndState() {
        val repository = seededRepository()
        val session = MultiTurnRuntime(repository).startSession("demo", "scene-shrine-questioning")
        val retriever = MemoryRetriever(repository)

        val first = retriever.preview(session.id, "char-shen-shuang", "我拿出沈家銅牌。")
        val second = retriever.preview(session.id, "char-shen-shuang", "我拿出沈家銅牌。")

        assertEquals(first.map { it.memoryId to it.retrievalScore }, second.map { it.memoryId to it.retrievalScore })
    }

    @Test
    fun highlyRelevantOldMemoryBeatsRecentUnrelatedMemory() {
        val repository = seededRepository()
        repository.upsertCharacterMemory(
            CharacterMemory(
                id = "memory-old-token",
                projectId = "demo",
                characterId = "char-shen-shuang",
                memoryType = "PROMISE",
                summary = "很久以前，沈霜曾因沈家銅牌向父親立下守祠誓言。",
                tags = listOf("沈家銅牌", "誓言"),
                triggerTags = listOf("沈家銅牌"),
                salience = 90,
                createdTurn = 0,
            ),
        )
        repository.upsertCharacterMemory(
            CharacterMemory(
                id = "memory-recent-unrelated",
                projectId = "demo",
                characterId = "char-shen-shuang",
                memoryType = "EPISODIC",
                summary = "上一回她整理了外廊雨具。",
                tags = listOf("雨具"),
                salience = 5,
                createdTurn = 99,
            ),
        )
        val session = MultiTurnRuntime(repository).startSession("demo", "scene-shrine-questioning")

        val preview = MemoryRetriever(repository).preview(session.id, "char-shen-shuang", "我展示沈家銅牌。")

        assertEquals("memory-old-token", preview.first().memoryId)
        assertTrue(preview.first { it.memoryId == "memory-old-token" }.retrievalScore > preview.first { it.memoryId == "memory-recent-unrelated" }.retrievalScore)
    }

    @Test
    fun contextBudgetTruncatesMemoryButKeepsSafetyFields() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我拿出沈家銅牌。")

        val budgeted = ContextBudgeter(maxContextBytes = 900).applyBudget(context, omittedMemoryCount = 0, omittedKnowledgeCount = 0)

        assertNotNull(budgeted.sceneContract)
        assertTrue(budgeted.sceneContract!!.forbiddenReveals.isNotEmpty())
        assertTrue(budgeted.worldRules.isNotEmpty())
        assertTrue(budgeted.contextBudget.truncationApplied)
    }

    @Test
    fun characterActionReferencingUnknownFactIsRejected() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我追問。")
        val result = validMultiTurnResult(context, "result-unknown-fact").copy(
            characterActions = validMultiTurnResult(context, "unused").characterActions.map {
                it.copy(usedFactIds = listOf("fact-private-never-in-context"))
            },
        )

        val report = runtime.validate(context, result)

        assertFalse(report.isValid)
        assertTrue(report.errors.any { it.code == "unknown_fact" })
    }

    @Test
    fun characterCanActFromWrongBeliefWithoutChangingCanonicalFact() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我展示沈家銅牌。")
        val beliefId = "claim-shen-player-bait"
        val result = validMultiTurnResult(context, "result-belief").copy(
            characterActions = validMultiTurnResult(context, "unused").characterActions.map {
                it.copy(usedKnowledgeClaimIds = listOf(beliefId), hiddenMotive = "她仍相信玩家可能是誘餌，因此不願完全交底。")
            },
        )

        val report = runtime.validate(context, result)
        runtime.resolveTurn(session.id, result)

        assertTrue(report.isValid, report.errors.joinToString { it.message })
        assertFalse(repository.listFacts("demo").any { it.content.contains("誘餌") })
    }

    @Test
    fun successfulTurnWritesMemoryCandidateInSameTransaction() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我展示沈家銅牌。")
        val result = validMultiTurnResult(context, "result-memory-write").copy(
            stateDeltas = validMultiTurnResult(context, "unused").stateDeltas + DeltaOperation(
                type = DeltaOperationType.ADD_EVENT,
                targetType = "event",
                targetId = "event-token-shown",
                payload = JsonObject(mapOf("summary" to JsonPrimitive("玩家展示沈家銅牌，沈霜因此動搖。"))),
            ),
            memoryCandidates = listOf(
                MemoryCandidate(
                    candidateId = "memory-runtime-token-shown",
                    ownerCharacterId = "char-shen-shuang",
                    memoryType = "EPISODIC",
                    summary = "沈霜記得玩家展示沈家銅牌時，她對誘餌的判斷開始鬆動。",
                    sourceEventIds = listOf("event-token-shown"),
                    tags = listOf("沈家銅牌", "誘餌", "動搖"),
                    salience = 72,
                    confidence = 70,
                    reason = "這會影響沈霜後續是否繼續把玩家視為誘餌。",
                ),
            ),
        )

        runtime.resolveTurn(session.id, result)

        assertNotNull(repository.getCharacterMemory("demo", "memory-runtime-token-shown"))
    }

    @Test
    fun rejectedTurnLeavesNoMemoryKnowledgeRecallOrEmotionWrites() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我展示沈家銅牌。")
        val beforeMemories = repository.tableCount("character_memories")
        val beforeKnowledge = repository.tableCount("character_knowledge")
        val beforeRecall = repository.tableCount("memory_recall_logs")
        val beforeState = repository.getCharacterState("demo", "char-shen-shuang")
        val invalid = validMultiTurnResult(context, "result-rejected-memory").copy(
            memoryCandidates = listOf(
                MemoryCandidate(
                    candidateId = "memory-should-not-write",
                    ownerCharacterId = "char-shen-shuang",
                    memoryType = "EPISODIC",
                    summary = "祠堂下面鎮著一口會呼吸的黑棺。",
                    reason = "故意違規。",
                ),
            ),
            knowledgeUpdates = listOf(
                KnowledgeUpdate(
                    characterId = "char-shen-shuang",
                    propositionKey = "hidden.secret",
                    requestedState = "KNOWN",
                    confidenceDelta = 50,
                    reason = "祠堂下面鎮著一口會呼吸的黑棺。",
                ),
            ),
        )

        val report = runtime.validate(context, invalid)
        assertFalse(report.isValid)
        assertFailsWith<IllegalStateException> { runtime.resolveTurn(session.id, invalid) }
        assertEquals(beforeMemories, repository.tableCount("character_memories"))
        assertEquals(beforeKnowledge, repository.tableCount("character_knowledge"))
        assertEquals(beforeRecall, repository.tableCount("memory_recall_logs"))
        assertEquals(beforeState, repository.getCharacterState("demo", "char-shen-shuang"))
    }

    @Test
    fun emotionDeltaOverLimitIsRejected() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我展示沈家銅牌。")
        val result = validMultiTurnResult(context, "result-emotion-over").copy(
            stateDeltas = validMultiTurnResult(context, "unused").stateDeltas + DeltaOperation(
                type = DeltaOperationType.UPDATE_CHARACTER_STATE,
                targetType = "character_state",
                targetId = "char-shen-shuang",
                payload = JsonObject(
                    mapOf(
                        "emotionDeltas" to JsonArray(
                            listOf(
                                JsonObject(
                                    mapOf(
                                        "emotion" to JsonPrimitive("戒備"),
                                        "delta" to JsonPrimitive(30),
                                        "trigger" to JsonPrimitive("銅牌刺激"),
                                        "sourceType" to JsonPrimitive("memory"),
                                        "sourceId" to JsonPrimitive("memory-shen-shuang-vow"),
                                        "reason" to JsonPrimitive("超界測試"),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        val report = runtime.validate(context, result)

        assertFalse(report.isValid)
        assertTrue(report.errors.any { it.code == "emotion_delta_too_large" })
    }

    @Test
    fun memoryReframeCreatesSupersedesRelationWithoutDeletingOldMemory() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我展示沈家銅牌。")
        val result = validMultiTurnResult(context, "result-reframe").copy(
            memoryCandidates = listOf(
                MemoryCandidate(
                    candidateId = "memory-reframed-vow",
                    ownerCharacterId = "char-shen-shuang",
                    memoryType = "RELATIONAL",
                    summary = "沈霜重新理解父親誓言：銅牌持有者未必只是誘餌，也可能帶來線索。",
                    sourceFactIds = listOf("fact-night-sound"),
                    tags = listOf("沈家銅牌", "誓言", "重估"),
                    salience = 80,
                    confidence = 68,
                    reason = "新證據讓她重估舊誓言的含義。",
                    lifecycleAction = "reframe",
                    supersedesMemoryId = "memory-shen-shuang-vow",
                ),
            ),
        )

        runtime.resolveTurn(session.id, result)

        assertNotNull(repository.getCharacterMemory("demo", "memory-shen-shuang-vow"))
        assertEquals("memory-shen-shuang-vow", repository.getCharacterMemory("demo", "memory-reframed-vow")?.supersedesMemoryId)
    }

    @Test
    fun previewRecallDoesNotModifyDatabase() {
        val repository = seededRepository()
        val session = MultiTurnRuntime(repository).startSession("demo", "scene-shrine-questioning")
        val beforeLogs = repository.tableCount("memory_recall_logs")
        val beforeMemory = repository.getCharacterMemory("demo", "memory-shen-shuang-vow")

        MemoryRetriever(repository).preview(session.id, "char-shen-shuang", "我展示沈家銅牌。")

        assertEquals(beforeLogs, repository.tableCount("memory_recall_logs"))
        assertEquals(beforeMemory?.recallCount, repository.getCharacterMemory("demo", "memory-shen-shuang-vow")?.recallCount)
    }

    @Test
    fun prepareTurnDoesNotIncreaseFormalRecallCount() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")

        runtime.prepareTurn(session.id, "我展示沈家銅牌。")

        assertEquals(0, repository.tableCount("memory_recall_logs"))
        assertEquals(0, repository.getCharacterMemory("demo", "memory-shen-shuang-vow")?.recallCount)
    }

    @Test
    fun successfulResolveIncrementsRecallCountOnlyOnce() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我展示沈家銅牌。")

        runtime.resolveTurn(session.id, validMultiTurnResult(context, "result-recall-once"))

        assertEquals(1, repository.tableCount("memory_recall_logs"))
        assertEquals(1, repository.getCharacterMemory("demo", "memory-shen-shuang-vow")?.recallCount)
    }

    @Test
    fun duplicateResultDoesNotRepeatRecallOrMemoryWrites() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我展示沈家銅牌。")
        val result = validMultiTurnResult(context, "result-dup-mvp3").copy(
            memoryCandidates = listOf(
                MemoryCandidate(
                    candidateId = "memory-dup-candidate",
                    ownerCharacterId = "char-shen-shuang",
                    memoryType = "EPISODIC",
                    summary = "沈霜記得自己第一次因銅牌對玩家產生遲疑。",
                    sourceFactIds = listOf("fact-night-sound"),
                    tags = listOf("沈家銅牌"),
                    reason = "用來測試 duplicate result 不重複寫入。",
                ),
            ),
        )

        runtime.resolveTurn(session.id, result)
        val memoryCount = repository.tableCount("character_memories")
        val recallCount = repository.tableCount("memory_recall_logs")
        runCatching { runtime.resolveTurn(session.id, result) }

        assertEquals(memoryCount, repository.tableCount("character_memories"))
        assertEquals(recallCount, repository.tableCount("memory_recall_logs"))
    }

    @Test
    fun knowledgeConflictDoesNotSilentlyOverwriteBeliefAsKnown() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我展示沈家銅牌。")
        val result = validMultiTurnResult(context, "result-knowledge-conflict").copy(
            knowledgeUpdates = listOf(
                KnowledgeUpdate(
                    characterId = "char-shen-shuang",
                    propositionKey = "player.may_be_outsider_bait",
                    requestedState = "KNOWN",
                    confidenceDelta = 10,
                    sourceMemoryIds = listOf("memory-shen-shuang-vow"),
                    reason = "銅牌提供了相反線索，但不足以讓她完全信任玩家。",
                ),
            ),
        )

        runtime.resolveTurn(session.id, result)

        val claim = repository.getKnowledgeClaim("demo", "char-shen-shuang", "player.may_be_outsider_bait")
        assertNotNull(claim)
        assertEquals("DOUBTED", claim.epistemicState)
    }

    @Test
    fun runtimeMemoryDuplicateBecomesReinforcement() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val firstContext = runtime.prepareTurn(session.id, "我展示沈家銅牌。")
        val candidate = MemoryCandidate(
            candidateId = "memory-repeat-1",
            ownerCharacterId = "char-shen-shuang",
            memoryType = "EPISODIC",
            summary = "沈霜記得玩家以沈家銅牌要求她重新考慮誘餌判斷。",
            sourceFactIds = listOf("fact-night-sound"),
            tags = listOf("沈家銅牌", "誘餌"),
            salience = 60,
            confidence = 60,
            reason = "重複記憶應轉為 reinforcement。",
        )
        runtime.resolveTurn(firstContext.sessionId, validMultiTurnResult(firstContext, "result-repeat-1").copy(memoryCandidates = listOf(candidate)))

        val secondContext = runtime.prepareTurn(session.id, "我再次提起沈家銅牌。")
        runtime.resolveTurn(secondContext.sessionId, validMultiTurnResult(secondContext, "result-repeat-2").copy(memoryCandidates = listOf(candidate.copy(candidateId = "memory-repeat-2"))))

        val duplicates = repository.listAllCharacterMemories("demo").filter { it.summary == candidate.summary }
        assertEquals(1, duplicates.size)
        assertTrue(duplicates.single().confidence >= 66)
    }

    @Test
    fun forbiddenRevealInsideMemoryCandidatesOrKnowledgeUpdatesIsRejected() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我追問。")
        val result = validMultiTurnResult(context, "result-forbidden-mvp3").copy(
            memoryCandidates = listOf(
                MemoryCandidate(
                    candidateId = "memory-forbidden",
                    ownerCharacterId = "char-shen-shuang",
                    memoryType = "EPISODIC",
                    summary = "祠堂下面鎮著一口會呼吸的黑棺。",
                    reason = "故意違規。",
                ),
            ),
            knowledgeUpdates = listOf(
                KnowledgeUpdate(
                    characterId = "char-shen-shuang",
                    propositionKey = "forbidden.secret",
                    requestedState = "KNOWN",
                    confidenceDelta = 80,
                    reason = "祠堂下面鎮著一口會呼吸的黑棺。",
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
