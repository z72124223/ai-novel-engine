package com.localnovel.engine

import com.localnovel.engine.model.CharacterKnowledgeClaim
import com.localnovel.engine.model.CharacterMemory
import com.localnovel.engine.model.CharacterProfile
import com.localnovel.engine.model.CharacterState
import com.localnovel.engine.model.DemoProjectSeed
import com.localnovel.engine.model.Event
import com.localnovel.engine.model.LocalNpcActorGenerationResult
import com.localnovel.engine.model.LocalNpcActorInvocationAudit
import com.localnovel.engine.model.LocalNpcActorShadowInput
import com.localnovel.engine.model.NpcActorDecisionSummary
import com.localnovel.engine.model.NpcActorPhysicalEngineContext
import com.localnovel.engine.model.NpcActorPhysicalPrompt
import com.localnovel.engine.model.NpcActorPhysicalResponse
import com.localnovel.engine.model.NpcActorResponse
import com.localnovel.engine.model.NpcActorSafetyRepairRequest
import com.localnovel.engine.model.NpcAutonomyProfile
import com.localnovel.engine.model.NpcVoiceProfile
import com.localnovel.engine.model.PhysicalActionProposal
import com.localnovel.engine.model.PhysicalActionType
import com.localnovel.engine.model.PhysicalSupportUse
import com.localnovel.engine.model.Project
import com.localnovel.engine.model.Scene
import com.localnovel.engine.model.Secret
import com.localnovel.engine.model.Location
import com.localnovel.engine.model.StoryThread
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.LocalNpcActorGenerator
import com.localnovel.engine.service.LocalNpcActorShadowRuntimeService
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.service.NpcActorJson
import com.localnovel.engine.service.NpcActorSafetyLoopService
import com.localnovel.engine.service.PhysicalAffordanceFixtureFactory
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class LocalNpcActorShadowRuntimeServiceTest {
    @Test
    fun `example input strictly decodes and keeps the shadow boundary`() {
        val repositoryRoot = generateSequence(java.nio.file.Path.of(System.getProperty("user.dir")).toAbsolutePath()) { it.parent }
            .first { Files.isRegularFile(it.resolve("settings.gradle.kts")) }
        val input = NpcActorJson.strict.decodeFromString<LocalNpcActorShadowInput>(
            Files.readString(repositoryRoot.resolve("samples/npc_actor07/pilot/npc_actor07_formal_runtime_shadow_input.json")),
        )

        assertEquals("FORMAL_RUNTIME_SHADOW_INPUT", input.boundary)
        assertTrue(input.profile.sandboxCandidateOnly)
        assertEquals("NON_CANON_EVALUATION", input.physicalContext.boundary)
        assertEquals(2, input.maxAttempts)
    }

    @Test
    fun `formal offscreen Session can produce an accepted candidate without any write`() {
        val fixture = formalSession(playerElsewhere = true)
        val generator = CapturingGenerator(unsafeDogSupport = false)

        val report = LocalNpcActorShadowRuntimeService(fixture.repository, generator)
            .run(fixture.input(targetIds = emptyList(), physicalTargetId = "chair"))

        assertEquals(
            "SHADOW_CANDIDATE_ACCEPTED_NO_STATE_WRITE",
            report.technicalDecision,
            report.localModelReport.safetyResult.attemptResults.toString(),
        )
        assertEquals(NpcActorSafetyLoopService.ACCEPTED_FOR_FORMAL_RESOLVE_CANDIDATE, report.localModelReport.safetyResult.status)
        assertEquals(report.beforeState, report.afterState)
        assertFalse(report.formalStateWritePerformed)
        assertFalse(report.afterState.previewContextPersisted)
        assertEquals(null, fixture.repository.getPendingContext(report.sessionId, report.contextId))
        assertTrue(report.visibleSummary.playerIsElsewhere)
    }

    @Test
    fun `actor prompt excludes foreign memories secrets and director-only text`() {
        val fixture = formalSession(playerElsewhere = true)
        val generator = CapturingGenerator(unsafeDogSupport = false)

        val report = LocalNpcActorShadowRuntimeService(fixture.repository, generator)
            .run(fixture.input(targetIds = emptyList(), physicalTargetId = "chair"))

        val promptJson = NpcActorJson.strict.encodeToString(generator.prompts.first())
        assertFalse(promptJson.contains(FOREIGN_MEMORY_TEXT))
        assertFalse(promptJson.contains(AUTHOR_SECRET_TEXT))
        assertFalse(promptJson.contains(DIRECTOR_ONLY_TEXT))
        assertTrue(report.actorRequest.ownedMemories.all { it.evidenceId != "memory-player-private-shadow" })
        assertTrue(report.actorRequest.ownedBeliefs.all { it.evidenceId != "claim-player-private-shadow" })
        assertEquals(listOf("event-shadow-actor-visible"), report.visibleSummary.perceivedEventIds)
    }

    @Test
    fun `same-location dialogue is visible but existing offscreen world gate rejects it without writes`() {
        val fixture = formalSession(playerElsewhere = false)
        val generator = CapturingGenerator(unsafeDogSupport = false)

        val report = LocalNpcActorShadowRuntimeService(fixture.repository, generator)
            .run(fixture.input(targetIds = listOf(PLAYER_ID), physicalTargetId = "chair"))

        assertEquals("SHADOW_CANDIDATE_REJECTED_NO_STATE_WRITE", report.technicalDecision)
        assertEquals(NpcActorSafetyLoopService.REJECTED_AFTER_MAX_ATTEMPTS, report.localModelReport.safetyResult.status)
        assertTrue(report.actorRequest.recentDialogue.single().summary.contains("你聽見什麼了"))
        assertTrue(report.localModelReport.safetyResult.attemptResults.flatMap { it.findings }.contains("world:player_not_offscreen"))
        assertEquals(report.beforeState, report.afterState)
    }

    @Test
    fun `physically impossible dog shoulder support is rejected without writes`() {
        val fixture = formalSession(playerElsewhere = true)
        val generator = CapturingGenerator(unsafeDogSupport = true)

        val report = LocalNpcActorShadowRuntimeService(fixture.repository, generator)
            .run(fixture.input(targetIds = emptyList(), physicalTargetId = "dog"))

        assertEquals("SHADOW_CANDIDATE_REJECTED_NO_STATE_WRITE", report.technicalDecision)
        assertEquals(NpcActorSafetyLoopService.REJECTED_AFTER_MAX_ATTEMPTS, report.localModelReport.safetyResult.status)
        val findings = report.localModelReport.safetyResult.attemptResults.flatMap { it.findings }
        assertTrue(findings.any { it.contains("body_plan_not_supported", ignoreCase = true) })
        assertEquals(report.beforeState, report.afterState)
    }

    @Test
    fun `shadow CLI fails closed when local runtime files are missing`() {
        val fixture = formalSession(playerElsewhere = true)
        val temp = Files.createTempDirectory("npc-actor07-shadow-cli")
        val request = temp.resolve("request.json")
        val output = temp.resolve("report.json")
        Files.writeString(request, NpcActorJson.strict.encodeToString(fixture.input(emptyList(), "chair")))

        val exit = Cli().run(
            listOf(
                "run-local-npc-actor-shadow",
                "--repo", java.nio.file.Path.of(System.getProperty("user.dir")).toAbsolutePath().toString(),
                "--db", fixture.repository.databasePath().toString(),
                "--request", request.toString(),
                "--llama-cli", temp.resolve("missing-llama-cli.exe").toString(),
                "--model", temp.resolve("missing-model.gguf").toString(),
                "--schema", temp.resolve("missing-schema.json").toString(),
                "--out", output.toString(),
            ),
        )

        assertEquals(1, exit)
        assertFalse(Files.exists(output))
    }

    private fun formalSession(playerElsewhere: Boolean): FormalSessionFixture {
        val temp = Files.createTempDirectory("npc-actor07-shadow-test")
        val repository = SqliteStoryRepository(temp.resolve("formal.db"))
        repository.initialize()
        val playerLocation = if (playerElsewhere) "loc-shrine-rear-hall" else "loc-shrine-corridor"
        val seed = DemoProjectSeed(
            project = Project(
                id = PROJECT_ID,
                name = "NPC Actor 07 Shadow Test",
                summary = "Isolated formal Session for read-only shadow verification.",
                activeSceneId = SCENE_ID,
            ),
            characters = listOf(
                CharacterProfile(ACTOR_ID, PROJECT_ID, "沈霜", "witness", "警戒而克制的非玩家角色。"),
                CharacterProfile(PLAYER_ID, PROJECT_ID, "玩家", "player", "正式玩家角色。", isPlayer = true),
            ),
            characterStates = listOf(
                CharacterState(
                    characterId = ACTOR_ID,
                    projectId = PROJECT_ID,
                    locationId = "loc-shrine-corridor",
                    mood = "警戒",
                    health = "steady",
                    flags = mapOf("current_goal" to "確認走廊異響來源。"),
                ),
                CharacterState(PLAYER_ID, PROJECT_ID, playerLocation, "neutral", "steady"),
            ),
            characterMemories = listOf(
                CharacterMemory(
                    id = "memory-actor-shadow-sound",
                    projectId = PROJECT_ID,
                    characterId = ACTOR_ID,
                    ownerCharacterId = ACTOR_ID,
                    summary = "沈霜記得走廊異響曾在危險前出現。",
                    visibility = "private",
                    isCanon = true,
                ),
                CharacterMemory(
                    id = "memory-player-private-shadow",
                    projectId = PROJECT_ID,
                    characterId = PLAYER_ID,
                    ownerCharacterId = PLAYER_ID,
                    summary = FOREIGN_MEMORY_TEXT,
                    visibility = "private",
                    isCanon = true,
                ),
            ),
            locations = listOf(
                Location("loc-shrine-corridor", PROJECT_ID, "神龕走廊", "狹長的石造走廊。"),
                Location("loc-shrine-rear-hall", PROJECT_ID, "神龕後殿", "與走廊分隔的後殿。"),
            ),
            scenes = listOf(
                Scene(SCENE_ID, PROJECT_ID, "影子測試", "只讀 NPC 演出測試。", locationId = null),
            ),
            knowledgeClaims = listOf(
                CharacterKnowledgeClaim(
                    id = "claim-actor-shadow-sound",
                    projectId = PROJECT_ID,
                    characterId = ACTOR_ID,
                    propositionKey = "shadow.actor.sound",
                    statement = "異響來自走廊附近。",
                    epistemicState = "BELIEVES",
                    confidence = 75,
                    sourceMemoryId = "memory-actor-shadow-sound",
                ),
                CharacterKnowledgeClaim(
                    id = "claim-player-private-shadow",
                    projectId = PROJECT_ID,
                    characterId = PLAYER_ID,
                    propositionKey = "shadow.player.private",
                    statement = FOREIGN_MEMORY_TEXT,
                    epistemicState = "BELIEVES",
                    confidence = 90,
                ),
            ),
            secrets = listOf(Secret(
                id = "secret-shadow-author-only",
                projectId = PROJECT_ID,
                content = AUTHOR_SECRET_TEXT,
                revealCondition = "author_only",
            )),
            events = listOf(
                Event(
                    id = "event-shadow-actor-visible",
                    projectId = PROJECT_ID,
                    turnId = "seed-shadow",
                    summary = "沈霜剛才在走廊聽見瓦片滑動。",
                    payload = JsonObject(mapOf("npcId" to JsonPrimitive(ACTOR_ID), "locationId" to JsonPrimitive("loc-shrine-corridor"))),
                ),
                Event(
                    id = "event-shadow-player-private",
                    projectId = PROJECT_ID,
                    turnId = "seed-shadow",
                    summary = FOREIGN_MEMORY_TEXT,
                    payload = JsonObject(mapOf("npcId" to JsonPrimitive(PLAYER_ID), "locationId" to JsonPrimitive("loc-shrine-rear-hall"))),
                ),
            ),
            storyThreads = listOf(
                StoryThread(
                    id = "thread-shadow-director-only",
                    projectId = PROJECT_ID,
                    threadType = "director",
                    title = "不可洩漏的導演線",
                    directorGoal = DIRECTOR_ONLY_TEXT,
                    publicSummary = "走廊仍有未解異響。",
                ),
            ),
        )
        repository.seedDemo(seed)
        val session = MultiTurnRuntime(repository).startSession(PROJECT_ID, SCENE_ID)
        return FormalSessionFixture(repository, session.id)
    }

    private data class FormalSessionFixture(
        val repository: SqliteStoryRepository,
        val sessionId: String,
    ) {
        fun input(targetIds: List<String>, physicalTargetId: String): LocalNpcActorShadowInput {
            val physicalSuite = PhysicalAffordanceFixtureFactory.createSuite()
            val baseSnapshot = physicalSuite.snapshots.single { it.snapshotId == "snapshot-base" }
            val physicalSnapshot = baseSnapshot.copy(
                snapshotId = "npc-actor07-formal-shadow-snapshot",
                entities = baseSnapshot.entities.map { entity ->
                    if (entity.entityId == "adult") entity.copy(entityId = ACTOR_ID) else entity
                },
            )
            return LocalNpcActorShadowInput(
                shadowRunId = "npc-actor07-test-${if (targetIds.isEmpty()) "offscreen" else "dialogue"}-$physicalTargetId",
                sessionId = sessionId,
                playerInput = if (targetIds.isEmpty()) {
                    "[SHADOW_NPC_TICK] 進行一次只讀離場檢查，不得改變正式狀態。"
                } else {
                    "你聽見什麼了？"
                },
                profile = NpcAutonomyProfile(
                    npcId = ACTOR_ID,
                    deepMotivation = "保住自己能證明真相的線索。",
                    falseBelief = "只要不說出口，危險就不會靠近。",
                    fear = "線索被奪走。",
                    relationshipDebts = listOf("欠玩家一次誠實回答。"),
                    offscreenAgenda = listOf("確認走廊異響來源。"),
                    voiceProfile = NpcVoiceProfile(
                        principles = listOf("只說自己知道的事。"),
                        forbiddenPatterns = listOf("AUTHOR_ONLY"),
                    ),
                ),
                targetIds = targetIds,
                physicalContext = NpcActorPhysicalEngineContext(
                    requestId = "npc-actor07-request-${if (targetIds.isEmpty()) "offscreen" else "dialogue"}-$physicalTargetId",
                    physicalArchetypes = physicalSuite.archetypes,
                    physicalSnapshot = physicalSnapshot,
                    allowedPhysicalActionTypes = listOf(PhysicalActionType.USE_AS_SUPPORT),
                    allowedPhysicalTargetIds = listOf(physicalTargetId),
                ),
            )
        }
    }

    private class CapturingGenerator(
        private val unsafeDogSupport: Boolean,
    ) : LocalNpcActorGenerator {
        val prompts = mutableListOf<NpcActorPhysicalPrompt>()

        override fun generate(
            prompt: NpcActorPhysicalPrompt,
            repairRequest: NpcActorSafetyRepairRequest?,
        ): LocalNpcActorGenerationResult {
            prompts += prompt
            val physicalTarget = prompt.visiblePhysicalTargetIds.single()
            val response = NpcActorPhysicalResponse(
                requestId = prompt.actorRequest.requestId,
                actorResponse = NpcActorResponse(
                    requestId = prompt.actorRequest.requestId,
                    actorId = prompt.actorRequest.actorId,
                    targetIds = prompt.actorRequest.targetIds,
                    speechAct = if (prompt.actorRequest.targetIds.isEmpty()) "NONVERBAL" else "ANSWER",
                    immediateGoal = "先確認眼前狀況。",
                    beliefClaimIds = prompt.actorRequest.ownedBeliefs.take(1).map { it.evidenceId },
                    memoryIds = prompt.actorRequest.ownedMemories.take(1).map { it.evidenceId },
                    emotionShown = "警戒",
                    emotionHidden = "不安",
                    actionType = "inspect_local_pressure",
                    actionIntent = "只做一次可中止的檢查。",
                    visibleAction = if (unsafeDogSupport) "扶著狗的肩側站起來。" else "扶住椅子把手確認穩定。",
                    utterance = if (prompt.actorRequest.targetIds.isEmpty()) "" else "只聽見一聲，還不能確定。",
                    nonverbalAction = "先停下來觀察。",
                    predictedCost = "十秒與少量注意力。",
                    observableConsequence = "只留下尚未套用的候選行動。",
                    confidence = 70,
                    decisionSummary = NpcActorDecisionSummary(
                        evidenceIds = prompt.actorRequest.goals.take(1).map { it.evidenceId },
                        selectedActionLabel = "bounded_check",
                    ),
                ),
                physicalActionProposal = PhysicalActionProposal(
                    actionId = "npc-actor07-${if (unsafeDogSupport) "unsafe" else "safe"}",
                    actionType = PhysicalActionType.USE_AS_SUPPORT,
                    actorId = prompt.actorRequest.actorId,
                    targetId = physicalTarget,
                    supportSurfaceId = if (unsafeDogSupport) "shoulder" else "arm",
                    supportUse = PhysicalSupportUse.STAND_UP_ASSIST,
                ),
            )
            return LocalNpcActorGenerationResult(
                response = response,
                audit = LocalNpcActorInvocationAudit(
                    modelId = "fixture-model",
                    modelRevision = "fixture-revision",
                    modelSha256 = "0".repeat(64),
                    modelFileName = "fixture.gguf",
                    runtimeId = "fixture-runtime",
                    contextTokens = 2048,
                    maxOutputTokens = 512,
                    seed = 1,
                    elapsedMillis = 10,
                    rawOutputBytes = 100,
                    promptSha256 = "1".repeat(64),
                    responseSha256 = "2".repeat(64),
                ),
                promptText = "fixture",
                rawOutput = "{}",
                responseJson = "{}",
            )
        }
    }

    companion object {
        private const val ACTOR_ID = "char-shen-shuang"
        private const val PLAYER_ID = "char-player"
        private const val PROJECT_ID = "npc-actor07-shadow-test"
        private const val SCENE_ID = "scene-npc-actor07-shadow"
        private const val FOREIGN_MEMORY_TEXT = "FOREIGN_PLAYER_MEMORY_MUST_NOT_LEAK"
        private const val AUTHOR_SECRET_TEXT = "AUTHOR_ONLY_SHADOW_SECRET_MUST_NOT_LEAK"
        private const val DIRECTOR_ONLY_TEXT = "DIRECTOR_ONLY_SHADOW_GOAL_MUST_NOT_LEAK"
    }
}
