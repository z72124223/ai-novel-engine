package com.localnovel.engine

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.DemoProjectSeed
import com.localnovel.engine.model.Item
import com.localnovel.engine.model.SceneRequirementUpdate
import com.localnovel.engine.model.SceneTransitionDefinition
import com.localnovel.engine.model.ScheduledWorldEventSeed
import com.localnovel.engine.model.ThreadProgressUpdate
import com.localnovel.engine.model.TimeAdvanceProposal
import com.localnovel.engine.model.TransitionProposal
import com.localnovel.engine.model.WorldEventAcknowledgement
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.ContextBudgeter
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
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

class Mvp4SceneThreadRuntimeTest {
    @Test
    fun migration004CreatesSceneThreadWorldEventTables() {
        val repository = seededRepository()

        assertTrue(
            repository.listTables().containsAll(
                setOf(
                    "story_threads",
                    "session_thread_states",
                    "scene_requirements",
                    "session_scene_requirement_states",
                    "scene_instances",
                    "location_connections",
                    "scene_transitions",
                    "pending_scene_transitions",
                    "scheduled_world_events",
                ),
            ),
        )
    }

    @Test
    fun startSessionCreatesExactlyOneActiveSceneInstance() {
        val repository = seededRepository()
        val session = MultiTurnRuntime(repository).startSession("demo", "scene-rear-hall")

        val lifecycle = repository.getSceneLifecycle(session.id)

        assertEquals(1, repository.activeSceneInstanceCount(session.id))
        assertEquals("scene-rear-hall", lifecycle.sceneId)
        assertEquals(1, lifecycle.visitNumber)
        assertEquals("ACTIVE", lifecycle.status)
    }

    @Test
    fun startSessionAlignsProjectSceneAndCharacterLocations() {
        val repository = seededRepository()
        val session = MultiTurnRuntime(repository).startSession("demo", "scene-rear-hall")

        assertEquals("scene-rear-hall", repository.getProject("demo")?.activeSceneId)
        assertEquals("loc-shrine-rear-hall", repository.getCharacterState("demo", "char-player")?.locationId)
        assertEquals("loc-shrine-rear-hall", repository.getCharacterState("demo", "char-shen-shuang")?.locationId)
        assertEquals("scene-rear-hall", repository.getSceneLifecycle(session.id).sceneId)
    }

    @Test
    fun prepareTurnIncludesSceneThreadWorldClockAndContinuityContext() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")

        val context = runtime.prepareTurn(session.id, "我查看後殿門閂。")

        assertEquals("scene-rear-hall", context.sceneLifecycle.sceneId)
        assertTrue(context.sceneRequirements.any { it.requirementId == "req-rear-door-bolt-evidence" })
        assertTrue(context.activeStoryThreads.isNotEmpty())
        assertTrue(context.availableTransitions.isNotEmpty())
        assertEquals(0, context.worldClock.elapsedMinutes)
        assertEquals("loc-shrine-rear-hall", context.continuityConstraints.currentSceneLocationId)
    }

    @Test
    fun transitionPreviewRespectsPlayerIntent() {
        val repository = seededRepository()
        val session = MultiTurnRuntime(repository).startSession("demo", "scene-rear-hall")

        val blocked = repository.listAvailableTransitions(session.id, "我還要留下。").single { it.transitionId == "transition-rear-to-corridor" }
        val allowed = repository.listAvailableTransitions(session.id, "我決定離開後殿。").single { it.transitionId == "transition-rear-to-corridor" }

        assertFalse(blocked.prerequisitesSatisfied)
        assertTrue(allowed.prerequisitesSatisfied)
    }

    @Test
    fun requiredRequirementBlocksNormalTransition() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(session.id, "我決定離開後殿。")
        val result = resultFor(context, "mvp4-required-block").copy(
            transitionProposal = transitionToCorridor(context, "PLAYER_INITIATED"),
        )

        val report = runtime.validate(context, result)

        assertFalse(report.isValid)
        assertTrue(report.errors.any { it.code == "exit_gate_unmet" })
    }

    @Test
    fun optionalRequirementCompletionDoesNotCloseSceneByItself() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(session.id, "我看牌位背面。")
        val result = resultFor(context, "mvp4-optional-only").copy(
            sceneRequirementUpdates = listOf(
                SceneRequirementUpdate(
                    requirementId = "req-rear-inscription-optional",
                    requestedStatus = "COMPLETED",
                    progressDelta = 100,
                    evidenceEventIds = listOf(eventId("mvp4-optional-only")),
                    reason = "玩家注意到牌位後方刮痕。",
                ),
            ),
        )

        runtime.resolveTurn(session.id, result)

        assertEquals("ACTIVE", repository.getSceneLifecycle(session.id).status)
    }

    @Test
    fun requiredRequirementCompletionMakesSceneReadyToClose() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(session.id, "我確認門閂。")

        runtime.resolveTurn(session.id, resultFor(context, "mvp4-required-done").copy(sceneRequirementUpdates = listOf(requiredDone("mvp4-required-done"))))

        val lifecycle = repository.getSceneLifecycle(session.id)
        assertTrue(lifecycle.readyToClose)
        assertEquals("READY_TO_CLOSE", lifecycle.status)
    }

    @Test
    fun readyToCloseDoesNotForceTransition() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val first = runtime.prepareTurn(session.id, "我確認門閂。")
        runtime.resolveTurn(session.id, resultFor(first, "mvp4-ready-no-force-1").copy(sceneRequirementUpdates = listOf(requiredDone("mvp4-ready-no-force-1"))))
        val second = runtime.prepareTurn(session.id, "我暫時留下觀察沈霜。")
        val result = resultFor(second, "mvp4-ready-no-force-2")

        val report = runtime.validate(second, result)
        runtime.resolveTurn(session.id, result)

        assertTrue(report.isValid, report.errors.joinToString { it.message })
        assertEquals("scene-rear-hall", repository.getSession(session.id)?.currentSceneId)
    }

    @Test
    fun playerRefusalBlocksNormalTransition() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(session.id, "我不走，我要留下。")
        val result = resultFor(context, "mvp4-refusal").copy(
            sceneRequirementUpdates = listOf(requiredDone("mvp4-refusal")),
            transitionProposal = transitionToCorridor(context, "PLAYER_INITIATED"),
        )

        val report = runtime.validate(context, result)

        assertFalse(report.isValid)
        assertTrue(report.errors.any { it.code == "player_refused_transition" })
    }

    @Test
    fun nonMovementBoundaryDoesNotBlockConsentedTransition() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(
            session.id,
            "I decide to 離開 the rear hall. I refuse to open cargo, touch seals, or enter the sealed warehouse.",
        )
        val result = resultFor(context, "mvp4-boundary-not-refusal").copy(
            sceneRequirementUpdates = listOf(requiredDone("mvp4-boundary-not-refusal")),
            transitionProposal = transitionToCorridor(context, "PLAYER_INITIATED"),
        )

        val report = runtime.validate(context, result)

        assertTrue(report.isValid, report.errors.joinToString { "${it.code}: ${it.message}" })
        runtime.resolveTurn(session.id, result)
        assertEquals("scene-shrine-questioning", repository.getSession(session.id)?.currentSceneId)
    }

    @Test
    fun unreachableLocationTransitionIsRejected() {
        val seed = readSeed().copy(
            sceneTransitions = readSeed().sceneTransitions + SceneTransitionDefinition(
                id = "transition-rear-to-steps-unreachable",
                projectId = "demo",
                fromSceneId = "scene-rear-hall",
                toSceneId = "scene-stone-steps",
                requiredPlayerIntent = "石階",
                forcedTransitionAllowed = false,
            ),
        )
        val repository = seededRepository(seed)
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(session.id, "我往石階去。")
        val result = resultFor(context, "mvp4-unreachable").copy(
            sceneRequirementUpdates = listOf(requiredDone("mvp4-unreachable")),
            transitionProposal = TransitionProposal(
                fromSceneInstanceId = context.sceneLifecycle.sceneInstanceId,
                transitionId = "transition-rear-to-steps-unreachable",
                toSceneId = "scene-stone-steps",
                proposalType = "PLAYER_INITIATED",
                playerIntentEvidence = "玩家說往石階去。",
                participatingCharacterIds = listOf("char-player", "char-shen-shuang"),
                reason = "測試不可達轉場。",
            ),
        )

        val report = runtime.validate(context, result)

        assertFalse(report.isValid)
        assertTrue(report.errors.any { it.code == "transition_prerequisite_unmet" })
    }

    @Test
    fun entryPrerequisiteTransitionIsRejectedWhenFactMissing() {
        val seed = readSeed().copy(
            sceneTransitions = readSeed().sceneTransitions + SceneTransitionDefinition(
                id = "transition-rear-prereq",
                projectId = "demo",
                fromSceneId = "scene-rear-hall",
                toSceneId = "scene-shrine-questioning",
                prerequisites = JsonObject(mapOf("requiredFactId" to JsonPrimitive("fact-never-created"))),
                requiredPlayerIntent = "離開",
                forcedTransitionAllowed = false,
            ),
        )
        val repository = seededRepository(seed)
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(session.id, "我離開後殿。")
        val result = resultFor(context, "mvp4-prereq").copy(
            sceneRequirementUpdates = listOf(requiredDone("mvp4-prereq")),
            transitionProposal = TransitionProposal(
                fromSceneInstanceId = context.sceneLifecycle.sceneInstanceId,
                transitionId = "transition-rear-prereq",
                toSceneId = "scene-shrine-questioning",
                proposalType = "PLAYER_INITIATED",
                playerIntentEvidence = "玩家說離開。",
                participatingCharacterIds = listOf("char-player", "char-shen-shuang"),
                reason = "測試缺少入口前提。",
            ),
        )

        val report = runtime.validate(context, result)

        assertFalse(report.isValid)
        assertTrue(report.errors.any { it.code == "transition_prerequisite_unmet" })
    }

    @Test
    fun worldForcedTransitionRequiresPendingWorldEventEvidence() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(session.id, "我還在後殿。")
        val result = resultFor(context, "mvp4-world-forced-missing").copy(
            transitionProposal = transitionToCorridor(context, "WORLD_FORCED").copy(worldEventEvidence = null),
        )

        val report = runtime.validate(context, result)

        assertFalse(report.isValid)
        assertTrue(report.errors.any { it.code == "missing_world_event_evidence" })
    }

    @Test
    fun validWorldForcedTransitionCanCloseScene() {
        val repository = seededRepository(withDueWorldEventAtTurnOne())
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(session.id, "我還在後殿。")
        val result = resultFor(context, "mvp4-world-forced-valid").copy(
            transitionProposal = transitionToCorridor(context, "WORLD_FORCED").copy(worldEventEvidence = "world-event-pursuers-now"),
            worldEventAcknowledgements = listOf(worldAck("world-event-pursuers-now")),
        )

        val report = runtime.validate(context, result)
        runtime.resolveTurn(session.id, result)

        assertTrue(report.isValid, report.errors.joinToString { it.message })
        assertEquals("scene-shrine-questioning", repository.getSession(session.id)?.currentSceneId)
        assertEquals(1, repository.activeSceneInstanceCount(session.id))
    }

    @Test
    fun transitionClosesOldSceneAndCreatesNewActiveInstance() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(session.id, "我離開後殿。")

        runtime.resolveTurn(session.id, resultFor(context, "mvp4-transition-apply").copy(sceneRequirementUpdates = listOf(requiredDone("mvp4-transition-apply")), transitionProposal = transitionToCorridor(context, "PLAYER_INITIATED")))

        val instances = repository.tableRows("scene_instances").filter { it["session_id"] == session.id }
        assertTrue(instances.any { it["scene_id"] == "scene-rear-hall" && it["status"] == "CLOSED" })
        assertTrue(instances.any { it["scene_id"] == "scene-shrine-questioning" && it["status"] == "ACTIVE" })
        assertEquals(1, repository.activeSceneInstanceCount(session.id))
    }

    @Test
    fun transitionAddsTravelTimeInSameResolve() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(session.id, "我離開後殿。")

        runtime.resolveTurn(session.id, resultFor(context, "mvp4-travel-time").copy(sceneRequirementUpdates = listOf(requiredDone("mvp4-travel-time")), transitionProposal = transitionToCorridor(context, "PLAYER_INITIATED")))

        assertEquals(8, repository.getSession(session.id)?.elapsedMinutes)
    }

    @Test
    fun invalidTransitionRollsBackSceneThreadTimeAndWorldEventWrites() {
        val repository = seededRepository(withDueWorldEventAtTurnOne())
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(session.id, "我不走。")
        val before = snapshot(repository, session.id)
        val invalid = resultFor(context, "mvp4-invalid-rollback").copy(
            transitionProposal = transitionToCorridor(context, "PLAYER_INITIATED"),
            worldEventAcknowledgements = listOf(worldAck("world-event-pursuers-now")),
        )

        assertFalse(runtime.validate(context, invalid).isValid)
        assertFailsWith<IllegalStateException> { runtime.resolveTurn(session.id, invalid) }

        assertEquals(before, snapshot(repository, session.id))
    }

    @Test
    fun worldEventAppearsOnlyWhenDue() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        assertTrue(runtime.prepareTurn(session.id, "我查看後殿。").pendingWorldEvents.isEmpty())
        repeat(3) { index ->
            val context = repository.getPendingContext(session.id, repository.tableRows("turn_logs").last { it["session_id"] == session.id }["context_id"].orEmpty())
            repository.abortPendingContext(session.id, context?.contextId)
            val next = runtime.prepareTurn(session.id, "我等待第${index + 1}次。")
            runtime.resolveTurn(session.id, resultFor(next, "mvp4-due-$index"))
        }

        val due = runtime.prepareTurn(session.id, "我聽見外面聲響。")

        assertTrue(due.pendingWorldEvents.any { it.eventId == "world-event-pursuers-turn4" })
    }

    @Test
    fun worldEventAcknowledgementConsumesEventOnce() {
        val repository = seededRepository(withDueWorldEventAtTurnOne())
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(session.id, "外面有火把。")

        runtime.resolveTurn(session.id, resultFor(context, "mvp4-world-consume").copy(worldEventAcknowledgements = listOf(worldAck("world-event-pursuers-now"))))

        val rows = repository.tableRows("scheduled_world_events").filter { it["id"] == "world-event-pursuers-now" }
        assertEquals("consumed", rows.single()["status"])
        assertEquals("1", rows.single()["consumed_turn_number"])
    }

    @Test
    fun threadProgressRequiresEvidence() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(session.id, "我逼問沈霜。")
        val result = resultFor(context, "mvp4-thread-no-evidence").copy(
            threadProgressUpdates = listOf(ThreadProgressUpdate("thread-character-shen-trust", progressDelta = 5, requestedStatus = "OPEN", reason = "無證據測試")),
        )

        val report = runtime.validate(context, result)

        assertFalse(report.isValid)
        assertTrue(report.errors.any { it.code == "missing_evidence" })
    }

    @Test
    fun validThreadProgressIsAppliedWithEvidence() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(session.id, "我放低聲音。")

        runtime.resolveTurn(session.id, resultFor(context, "mvp4-thread-valid").copy(threadProgressUpdates = listOf(threadAdvance("mvp4-thread-valid"))))

        val row = repository.tableRows("session_thread_states").single { it["session_id"] == session.id && it["thread_id"] == "thread-character-shen-trust" }
        assertEquals("7", row["progress"])
        assertEquals("1", row["last_advanced_turn"])
    }

    @Test
    fun directorGoalCannotBecomeCharacterIntent() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(session.id, "我看著沈霜。")
        val goal = repository.tableRows("story_threads").first { it["id"] == "thread-main-shrine-truth" }["director_goal"].orEmpty()
        val result = resultFor(context, "mvp4-director-goal").copy(
            characterActions = listOf(defaultAction(context).copy(intent = goal)),
        )

        val report = runtime.validate(context, result)

        assertFalse(report.isValid)
        assertTrue(report.errors.any { it.code == "director_goal_leak" })
    }

    @Test
    fun characterOutsideCurrentLocationCannotActInScene() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(session.id, "我看著沈霜。").copy(
            continuityConstraints = runtime.prepareTurn(session.id, "影子 context").continuityConstraints.copy(
                characterLocations = mapOf("char-shen-shuang" to "loc-shrine-steps", "char-player" to "loc-shrine-rear-hall"),
            ),
        )
        val result = resultFor(context, "mvp4-location-mismatch")

        val report = runtime.validate(context, result)

        assertFalse(report.isValid)
        assertTrue(report.errors.any { it.code == "character_location_mismatch" })
    }

    @Test
    fun itemOutsideActorReachIsRejected() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(session.id, "沈霜伸手。").copy(
            items = runtime.prepareTurn(session.id, "影子 context").items + Item(
                id = "item-far-lantern",
                projectId = "demo",
                name = "遠處燈籠",
                description = "放在石階盡頭。",
                ownerCharacterId = null,
                locationId = "loc-shrine-steps",
            ),
        )
        val result = resultFor(context, "mvp4-item-mismatch").copy(
            characterActions = listOf(defaultAction(context).copy(usedItemIds = listOf("item-far-lantern"))),
        )

        val report = runtime.validate(context, result)

        assertFalse(report.isValid)
        assertTrue(report.errors.any { it.code == "item_location_mismatch" })
    }

    @Test
    fun contextBudgetKeepsSceneThreadSafetyFields() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(session.id, "我查看後殿。")

        val budgeted = ContextBudgeter(maxContextBytes = 1200).applyBudget(context, omittedMemoryCount = 0, omittedKnowledgeCount = 0)

        assertTrue(budgeted.contextBudget.truncationApplied)
        assertEquals(context.sceneLifecycle.sceneInstanceId, budgeted.sceneLifecycle.sceneInstanceId)
        assertTrue(budgeted.sceneRequirements.isNotEmpty())
        assertTrue(budgeted.activeStoryThreads.isNotEmpty())
        assertEquals(context.continuityConstraints.currentSceneLocationId, budgeted.continuityConstraints.currentSceneLocationId)
    }

    @Test
    fun returningToSceneCreatesNewVisitNumber() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val first = runtime.prepareTurn(session.id, "我離開後殿。")
        runtime.resolveTurn(session.id, resultFor(first, "mvp4-revisit-1").copy(sceneRequirementUpdates = listOf(requiredDone("mvp4-revisit-1")), transitionProposal = transitionToCorridor(first, "PLAYER_INITIATED")))
        val second = runtime.prepareTurn(session.id, "我回到後殿。")
        runtime.resolveTurn(session.id, resultFor(second, "mvp4-revisit-2").copy(transitionProposal = transitionToRear(second)))

        val rearVisits = repository.tableRows("scene_instances")
            .filter { it["session_id"] == session.id && it["scene_id"] == "scene-rear-hall" }
            .map { it["visit_number"] }
            .toSet()

        assertEquals(setOf("1", "2"), rearVisits)
        assertEquals("scene-rear-hall", repository.getSession(session.id)?.currentSceneId)
    }

    @Test
    fun closedSceneSummaryAppearsInLaterContinuityContext() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val first = runtime.prepareTurn(session.id, "我離開後殿。")
        runtime.resolveTurn(session.id, resultFor(first, "mvp4-closed-summary").copy(sceneRequirementUpdates = listOf(requiredDone("mvp4-closed-summary")), transitionProposal = transitionToCorridor(first, "PLAYER_INITIATED")))

        val next = runtime.prepareTurn(session.id, "我回頭看後殿門口。")

        assertTrue(next.continuityConstraints.closedSceneSummaries.any { it.contains("門閂") || it.contains("沈霜") })
    }

    @Test
    fun narrativeCannotEnterTargetSceneWithoutTransitionProposal() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")
        val context = runtime.prepareTurn(session.id, "我先不離開，只看向外廊。")
        val targetTitle = repository.tableRows("scenes").single { it["id"] == "scene-shrine-questioning" }["title"].orEmpty()
        val result = resultFor(context, "mvp4-implicit-transition").copy(
            narrative = "沈霜沒有等玩家同意，就把人帶進$targetTitle。",
        )

        val report = runtime.validate(context, result)

        assertFalse(report.isValid)
        assertTrue(report.errors.any { it.code == "implicit_transition" })
    }

    @Test
    fun sevenTurnCrossSceneE2EPasses() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")

        val t1 = runtime.prepareTurn(session.id, "我檢查門閂。")
        runtime.resolveTurn(session.id, resultFor(t1, "mvp4-e2e-1").copy(sceneRequirementUpdates = listOf(requiredDone("mvp4-e2e-1"))))
        val t2 = runtime.prepareTurn(session.id, "我查看牌位背後。")
        runtime.resolveTurn(session.id, resultFor(t2, "mvp4-e2e-2").copy(sceneRequirementUpdates = listOf(SceneRequirementUpdate("req-rear-inscription-optional", "COMPLETED", 100, evidenceEventIds = listOf(eventId("mvp4-e2e-2")), reason = "牌位背後有刮痕。"))))
        val t3 = runtime.prepareTurn(session.id, "我不走，繼續追問。")
        runtime.resolveTurn(session.id, resultFor(t3, "mvp4-e2e-3"))
        val t4 = runtime.prepareTurn(session.id, "我聽見外面有聲響。")
        assertTrue(t4.pendingWorldEvents.any { it.eventId == "world-event-pursuers-turn4" })
        runtime.resolveTurn(session.id, resultFor(t4, "mvp4-e2e-4").copy(worldEventAcknowledgements = listOf(worldAck("world-event-pursuers-turn4"))))
        val t5 = runtime.prepareTurn(session.id, "我決定離開後殿。")
        runtime.resolveTurn(session.id, resultFor(t5, "mvp4-e2e-5").copy(transitionProposal = transitionToCorridor(t5, "PLAYER_INITIATED")))
        val t6 = runtime.prepareTurn(session.id, "我走向石階。")
        runtime.resolveTurn(session.id, resultFor(t6, "mvp4-e2e-6").copy(transitionProposal = transitionToSteps(t6)))
        val t7 = runtime.prepareTurn(session.id, "我在石階前停下。")
        runtime.resolveTurn(session.id, resultFor(t7, "mvp4-e2e-7").copy(threadProgressUpdates = listOf(threadAdvance("mvp4-e2e-7"))))

        assertEquals(8, repository.getSession(session.id)?.nextTurnNumber)
        assertEquals(1, repository.activeSceneInstanceCount(session.id))
        assertEquals("scene-stone-steps", repository.getSession(session.id)?.currentSceneId)
        assertTrue(repository.tableRows("scene_instances").any { it["scene_id"] == "scene-rear-hall" && it["status"] == "CLOSED" })
        assertTrue(repository.tableRows("scheduled_world_events").any { it["id"] == "world-event-pursuers-turn4" && it["status"] == "consumed" })
    }

    @Test
    fun twentyTurnDeterministicSoakPasses() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-rear-hall")

        repeat(20) { index ->
            val turn = index + 1
            val input = when (turn) {
                1 -> "我離開後殿。"
                2 -> "我往石階去。"
                else -> "我保持警戒，觀察沈霜與四周。"
            }
            val context = runtime.prepareTurn(session.id, input)
            assertTrue(context.contextBudget.actualBytes <= context.contextBudget.maxBytes || context.contextBudget.truncationApplied)
            val result = when (turn) {
                1 -> resultFor(context, "mvp4-soak-$turn").copy(sceneRequirementUpdates = listOf(requiredDone("mvp4-soak-$turn")), transitionProposal = transitionToCorridor(context, "PLAYER_INITIATED"))
                2 -> resultFor(context, "mvp4-soak-$turn").copy(transitionProposal = transitionToSteps(context))
                4 -> resultFor(context, "mvp4-soak-$turn").copy(worldEventAcknowledgements = listOf(worldAck("world-event-pursuers-turn4")))
                else -> resultFor(context, "mvp4-soak-$turn")
            }
            runtime.resolveTurn(session.id, result)
            assertEquals(turn, repository.getSession(session.id)?.stateVersion)
            assertEquals(1, repository.activeSceneInstanceCount(session.id))
        }

        val instances = repository.tableRows("scene_instances").filter { it["session_id"] == session.id }
        assertTrue(instances.size >= 3)
        assertEquals(21, repository.getSession(session.id)?.nextTurnNumber)
        assertTrue(repository.tableRows("scheduled_world_events").count { it["status"] == "consumed" } <= 1)
    }

    private fun seededRepository(seed: DemoProjectSeed = readSeed()): SqliteStoryRepository {
        val repository = SqliteStoryRepository(Files.createTempDirectory("story-engine-mvp4-test").resolve("story.db"))
        repository.initialize()
        repository.seedDemo(seed)
        return repository
    }

    private fun readSeed(): DemoProjectSeed =
        JsonSupport.json.decodeFromString(Files.readString(samplePath("demo_project_seed.json")))

    private fun withDueWorldEventAtTurnOne(): DemoProjectSeed =
        readSeed().copy(
            scheduledWorldEvents = readSeed().scheduledWorldEvents + ScheduledWorldEventSeed(
                id = "world-event-pursuers-now",
                projectId = "demo",
                eventType = "approaching_pursuers",
                dueTurnNumber = 1,
                payload = JsonObject(
                    mapOf(
                        "publicEffect" to JsonPrimitive("祠堂外有火把逼近。"),
                        "allowedNarrativeUses" to kotlinx.serialization.json.JsonArray(listOf(JsonPrimitive("火把逼近"))),
                        "forbiddenCharacterUses" to kotlinx.serialization.json.JsonArray(listOf(JsonPrimitive("追兵知道地下入口"))),
                    ),
                ),
            ),
        )

    private fun resultFor(context: ContextPacket, resultId: String): AiTurnResult =
        AiTurnResult(
            projectId = context.project.id,
            turnId = context.turnId,
            resultId = resultId,
            contextId = context.contextId,
            sessionId = context.sessionId,
            turnNumber = context.turnNumber,
            baseStateVersion = context.baseStateVersion,
            contextHash = context.contextHash,
            narrative = "沈霜按住袖口，避開牌位的陰影，只承認門閂附近確實有新痕。她沒有坦白更多，只提醒玩家別把聲音傳到外面。",
            turnSummary = "沈霜在防備中承認門閂痕跡，玩家取得可驗證線索。",
            characterActions = listOf(defaultAction(context)),
            stateDeltas = listOf(
                DeltaOperation(
                    type = DeltaOperationType.ADD_EVENT,
                    targetType = "event",
                    targetId = eventId(resultId),
                    payload = JsonObject(mapOf("summary" to JsonPrimitive("沈霜承認門閂附近有新痕。"))),
                ),
            ),
            timeAdvanceProposal = TimeAdvanceProposal(5, "turn", context.turnId, "對話與檢查耗時。"),
        )

    private fun defaultAction(context: ContextPacket): CharacterAction =
        CharacterAction(
            characterId = "char-shen-shuang",
            intent = "保護自己並拖延玩家的追問",
            visibleAction = "沈霜低聲回答，只承認自己看見門閂附近有痕跡。",
            hiddenMotive = "她擔心玩家追得太快，也擔心外面有人聽見。",
            triggeringConditions = listOf(context.playerInput.take(40)),
            characterFitScore = 88,
            storyProgressScore = 70,
        )

    private fun requiredDone(resultId: String): SceneRequirementUpdate =
        SceneRequirementUpdate(
            requirementId = "req-rear-door-bolt-evidence",
            requestedStatus = "COMPLETED",
            progressDelta = 100,
            evidenceEventIds = listOf(eventId(resultId)),
            reason = "沈霜承認門閂附近有新痕，足以完成本場景必需證據。",
        )

    private fun threadAdvance(resultId: String): ThreadProgressUpdate =
        ThreadProgressUpdate(
            threadId = "thread-character-shen-trust",
            progressDelta = 7,
            requestedStatus = "OPEN",
            evidenceEventIds = listOf(eventId(resultId)),
            reason = "沈霜願意給出有限但真實的線索。",
        )

    private fun transitionToCorridor(context: ContextPacket, type: String): TransitionProposal =
        TransitionProposal(
            fromSceneInstanceId = context.sceneLifecycle.sceneInstanceId,
            transitionId = "transition-rear-to-corridor",
            toSceneId = "scene-shrine-questioning",
            proposalType = type,
            playerIntentEvidence = context.playerInput,
            participatingCharacterIds = listOf("char-player", "char-shen-shuang"),
            reason = "玩家與沈霜離開後殿，回到外廊承受新的壓力。",
        )

    private fun transitionToRear(context: ContextPacket): TransitionProposal =
        TransitionProposal(
            fromSceneInstanceId = context.sceneLifecycle.sceneInstanceId,
            transitionId = "transition-corridor-to-rear",
            toSceneId = "scene-rear-hall",
            proposalType = "PLAYER_INITIATED",
            playerIntentEvidence = context.playerInput,
            participatingCharacterIds = listOf("char-player", "char-shen-shuang"),
            reason = "玩家回到後殿核對剛才的線索。",
        )

    private fun transitionToSteps(context: ContextPacket): TransitionProposal =
        TransitionProposal(
            fromSceneInstanceId = context.sceneLifecycle.sceneInstanceId,
            transitionId = "transition-corridor-to-steps",
            toSceneId = "scene-stone-steps",
            proposalType = "PLAYER_INITIATED",
            playerIntentEvidence = context.playerInput,
            participatingCharacterIds = listOf("char-player", "char-shen-shuang"),
            reason = "玩家沿著可見石階向下方移動。",
        )

    private fun worldAck(eventId: String): WorldEventAcknowledgement =
        WorldEventAcknowledgement(
            eventId = eventId,
            narrativeUse = "火把逼近",
            perceivedByCharacterIds = listOf("char-player", "char-shen-shuang"),
            reason = "外部壓力只以聲光進入敘事，不讓角色知道未見資訊。",
        )

    private fun eventId(resultId: String): String = "event-$resultId"

    private fun snapshot(repository: SqliteStoryRepository, sessionId: String): List<Int> =
        listOf(
            repository.activeSceneInstanceCount(sessionId),
            repository.getSession(sessionId)?.stateVersion ?: -1,
            repository.getSession(sessionId)?.elapsedMinutes ?: -1,
            repository.tableRows("scheduled_world_events").count { it["status"] == "consumed" },
            repository.tableRows("pending_scene_transitions").size,
        )

    private fun samplePath(fileName: String): Path {
        val candidates = listOf(
            Path.of("samples", fileName),
            Path.of("..", "samples", fileName),
        )
        return candidates.firstOrNull { Files.exists(it) }
            ?: error("Cannot find sample file: $fileName")
    }
}
