package com.localnovel.engine

import com.localnovel.engine.model.CharacterKnowledgeClaim
import com.localnovel.engine.model.CharacterMemory
import com.localnovel.engine.model.CharacterProfile
import com.localnovel.engine.model.CharacterState
import com.localnovel.engine.model.DemoProjectSeed
import com.localnovel.engine.model.LocalNpcActorFormalDecisionType
import com.localnovel.engine.model.LocalNpcActorFormalResolveReport
import com.localnovel.engine.model.LocalNpcActorFormalTurnCandidatePackage
import com.localnovel.engine.model.LocalNpcActorFormalTurnDecision
import com.localnovel.engine.model.LocalNpcActorFormalTurnInput
import com.localnovel.engine.model.LocalNpcActorGenerationResult
import com.localnovel.engine.model.LocalNpcActorInvocationAudit
import com.localnovel.engine.model.Location
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
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.LocalNpcActorFormalTurnHash
import com.localnovel.engine.service.LocalNpcActorFormalTurnPreparationService
import com.localnovel.engine.service.LocalNpcActorFormalTurnResolutionService
import com.localnovel.engine.service.LocalNpcActorGenerator
import com.localnovel.engine.service.LocalNpcActorFormalTurnResolutionService.Companion.REQUIRED_ACKNOWLEDGEMENT
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.service.NpcActorJson
import com.localnovel.engine.service.PhysicalAffordanceFixtureFactory
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class LocalNpcActorFormalTurnServiceTest {
    @Test
    fun `preparation creates one hash-bound pending candidate without applying the turn`() {
        val fixture = fixture()
        val candidate = fixture.prepare()

        assertEquals("HUMAN_APPROVAL_REQUIRED", candidate.status)
        assertEquals(LocalNpcActorFormalTurnHash.candidate(candidate.payload), candidate.candidateSha256)
        assertNotNull(fixture.repository.getPendingContext(candidate.payload.sessionId, candidate.payload.contextId))
        assertEquals(candidate.payload.preparationBeforeState.sessionStateVersion, candidate.payload.preparationAfterState.sessionStateVersion)
        assertEquals(candidate.payload.preparationBeforeState.appliedTurnCount, candidate.payload.preparationAfterState.appliedTurnCount)
        assertTrue(candidate.payload.preparationAfterState.pendingContextPresent)
        assertFalse(candidate.payload.localModelReport.formalStateWritePerformed)
        assertTrue(candidate.payload.aiTurnResult.memoryCandidates.isEmpty())
        assertTrue(candidate.payload.aiTurnResult.knowledgeUpdates.isEmpty())
    }

    @Test
    fun `tampered package or mismatched human decision cannot apply`() {
        val fixture = fixture()
        val candidate = fixture.prepare()
        val service = LocalNpcActorFormalTurnResolutionService(fixture.repository)
        val tampered = candidate.copy(
            payload = candidate.payload.copy(
                aiTurnResult = candidate.payload.aiTurnResult.copy(narrative = "遭竄改的候選內容。"),
            ),
        )

        assertFailsWith<IllegalArgumentException> {
            service.resolve(tampered, decision(candidate, LocalNpcActorFormalDecisionType.APPROVE_NON_CANON_FORMAL_TURN))
        }
        assertFailsWith<IllegalArgumentException> {
            service.resolve(
                candidate,
                decision(candidate, LocalNpcActorFormalDecisionType.APPROVE_NON_CANON_FORMAL_TURN)
                    .copy(contextHash = "0".repeat(64)),
            )
        }
        val session = requireNotNull(fixture.repository.getSession(candidate.payload.sessionId))
        assertEquals(candidate.payload.baseStateVersion, session.stateVersion)
        assertNotNull(fixture.repository.getPendingContext(candidate.payload.sessionId, candidate.payload.contextId))
    }

    @Test
    fun `human rejection clears pending Context without formal turn data`() {
        val fixture = fixture()
        val candidate = fixture.prepare()
        val before = candidate.payload.preparationAfterState

        val report = LocalNpcActorFormalTurnResolutionService(fixture.repository).resolve(
            candidate,
            decision(candidate, LocalNpcActorFormalDecisionType.REJECT_NON_CANON_FORMAL_TURN),
        )

        assertEquals("REJECTED_AND_ABORTED", report.status)
        assertFalse(report.formalTurnApplied)
        assertTrue(report.pendingContextCleared)
        assertEquals(before.sessionStateVersion, report.afterState.sessionStateVersion)
        assertEquals(before.appliedTurnCount, report.afterState.appliedTurnCount)
        assertEquals(before.eventCount, report.afterState.eventCount)
        assertEquals(null, fixture.repository.getPendingContext(candidate.payload.sessionId, candidate.payload.contextId))
    }

    @Test
    fun `test-only exact approval applies one non-Canon turn and replay is blocked`() {
        val fixture = fixture()
        val candidate = fixture.prepare()
        val service = LocalNpcActorFormalTurnResolutionService(fixture.repository)
        val approval = decision(candidate, LocalNpcActorFormalDecisionType.APPROVE_NON_CANON_FORMAL_TURN)

        val report = service.resolve(candidate, approval)

        assertEquals("APPROVED_AND_APPLIED_NON_CANON", report.status)
        assertTrue(report.formalTurnApplied)
        assertTrue(report.testOnlyApproval)
        assertEquals(report.beforeState.sessionStateVersion + 1, report.afterState.sessionStateVersion)
        assertEquals(report.beforeState.nextTurnNumber + 1, report.afterState.nextTurnNumber)
        assertEquals(report.beforeState.appliedTurnCount + 1, report.afterState.appliedTurnCount)
        assertEquals(report.beforeState.eventCount + 1, report.afterState.eventCount)
        assertEquals(report.beforeState.memoryCount, report.afterState.memoryCount)
        assertEquals(report.beforeState.knowledgeCount, report.afterState.knowledgeCount)
        assertFailsWith<IllegalStateException> { service.resolve(candidate, approval) }
    }

    @Test
    fun `candidate becomes stale when another approved Context advances the Session`() {
        val fixture = fixture()
        val first = fixture.prepare(preparationId = "npc-actor08-first")
        val second = fixture.prepare(preparationId = "npc-actor08-second")
        val service = LocalNpcActorFormalTurnResolutionService(fixture.repository)

        service.resolve(second, decision(second, LocalNpcActorFormalDecisionType.APPROVE_NON_CANON_FORMAL_TURN))

        assertFailsWith<IllegalArgumentException> {
            service.resolve(first, decision(first, LocalNpcActorFormalDecisionType.APPROVE_NON_CANON_FORMAL_TURN))
        }
        assertEquals(1, fixture.repository.appliedTurnCount(fixture.sessionId))

        val rejected = service.resolve(first, decision(first, LocalNpcActorFormalDecisionType.REJECT_NON_CANON_FORMAL_TURN))
        assertEquals("REJECTED_AND_ABORTED", rejected.status)
        assertEquals(null, fixture.repository.getPendingContext(first.payload.sessionId, first.payload.contextId))
    }

    @Test
    fun `rejected model output removes preparation pending Context`() {
        val fixture = fixture(unsafeDogSupport = true)
        val sessionBefore = requireNotNull(fixture.repository.getSession(fixture.sessionId))

        assertFailsWith<IllegalArgumentException> { fixture.prepare(physicalTargetId = "dog") }

        val sessionAfter = requireNotNull(fixture.repository.getSession(fixture.sessionId))
        assertEquals(sessionBefore, sessionAfter)
        assertEquals(0, fixture.repository.appliedTurnCount(fixture.sessionId))
        assertEquals(0, fixture.repository.tableCount("turn_logs"))
    }

    @Test
    fun `sample input and decision template strictly decode`() {
        val root = generateSequence(java.nio.file.Path.of(System.getProperty("user.dir")).toAbsolutePath()) { it.parent }
            .first { Files.isRegularFile(it.resolve("settings.gradle.kts")) }
        val input = NpcActorJson.strict.decodeFromString<LocalNpcActorFormalTurnInput>(
            Files.readString(root.resolve("samples/npc_actor08/npc_actor08_formal_turn_input.example.json")),
        )
        val decision = NpcActorJson.strict.decodeFromString<LocalNpcActorFormalTurnDecision>(
            Files.readString(root.resolve("samples/npc_actor08/npc_actor08_human_decision.template.json")),
        )

        assertEquals("FORMAL_RUNTIME_CANDIDATE_INPUT", input.boundary)
        assertEquals("HUMAN_APPROVAL_DECISION", decision.boundary)
        assertFalse(decision.testOnly)
        val schema = Json.parseToJsonElement(
            Files.readString(root.resolve("schemas/npc_actor/npc_actor_formal_approval_v1.schema.json")),
        ).jsonObject
        assertEquals("NPC-ACTOR-08 Human-Approved Formal Resolve V1", schema["title"]?.toString()?.trim('"'))
        assertTrue(schema["\$defs"]!!.jsonObject.containsKey("humanDecision"))
    }

    @Test
    fun `resolve CLI reaches the exact approval service and writes a strict report`() {
        val fixture = fixture()
        val candidate = fixture.prepare()
        val approval = decision(candidate, LocalNpcActorFormalDecisionType.APPROVE_NON_CANON_FORMAL_TURN)
        val temp = Files.createTempDirectory("npc-actor08-resolve-cli")
        val candidatePath = temp.resolve("candidate.json")
        val decisionPath = temp.resolve("decision.json")
        val reportPath = temp.resolve("report.json")
        Files.writeString(candidatePath, NpcActorJson.strict.encodeToString(candidate))
        Files.writeString(decisionPath, NpcActorJson.strict.encodeToString(approval))

        val exit = Cli().run(
            listOf(
                "resolve-local-npc-actor-formal-turn",
                "--db", fixture.repository.databasePath().toString(),
                "--candidate", candidatePath.toString(),
                "--decision", decisionPath.toString(),
                "--out", reportPath.toString(),
            ),
        )

        assertEquals(0, exit)
        val report = NpcActorJson.strict.decodeFromString<LocalNpcActorFormalResolveReport>(Files.readString(reportPath))
        assertEquals("APPROVED_AND_APPLIED_NON_CANON", report.status)
        assertTrue(report.formalTurnApplied)
    }

    private fun fixture(unsafeDogSupport: Boolean = false): Fixture {
        val temp = Files.createTempDirectory("npc-actor08-formal-turn-test")
        val repository = SqliteStoryRepository(temp.resolve("formal.db"))
        repository.initialize()
        repository.seedDemo(
            DemoProjectSeed(
                project = Project(PROJECT_ID, "NPC Actor 08 Test", "Non-Canon formal approval test.", SCENE_ID),
                characters = listOf(
                    CharacterProfile(ACTOR_ID, PROJECT_ID, "沈霜", "witness", "警覺、克制，先確認現場。"),
                    CharacterProfile(PLAYER_ID, PROJECT_ID, "旅人", "player", "玩家角色。", isPlayer = true),
                ),
                characterStates = listOf(
                    CharacterState(
                        characterId = ACTOR_ID,
                        projectId = PROJECT_ID,
                        locationId = ACTOR_LOCATION,
                        mood = "警覺",
                        health = "steady",
                        flags = mapOf("current_goal" to "確認走廊聲響來源"),
                    ),
                    CharacterState(PLAYER_ID, PROJECT_ID, PLAYER_LOCATION, "neutral", "steady"),
                ),
                characterMemories = listOf(
                    CharacterMemory(
                        id = MEMORY_ID,
                        projectId = PROJECT_ID,
                        characterId = ACTOR_ID,
                        ownerCharacterId = ACTOR_ID,
                        summary = "沈霜記得走廊木板在受潮後會發出短促聲響。",
                        visibility = "private",
                        isCanon = true,
                    ),
                    CharacterMemory(
                        id = "memory-player-secret",
                        projectId = PROJECT_ID,
                        characterId = PLAYER_ID,
                        ownerCharacterId = PLAYER_ID,
                        summary = "FOREIGN_MEMORY_MUST_NOT_REACH_ACTOR",
                        visibility = "private",
                        isCanon = true,
                    ),
                ),
                knowledgeClaims = listOf(
                    CharacterKnowledgeClaim(
                        id = CLAIM_ID,
                        projectId = PROJECT_ID,
                        characterId = ACTOR_ID,
                        propositionKey = "corridor.board.sound",
                        statement = "受潮木板會在受力時發出短促聲響。",
                        epistemicState = "BELIEVES",
                        confidence = 80,
                        sourceMemoryId = MEMORY_ID,
                    ),
                ),
                locations = listOf(
                    Location(ACTOR_LOCATION, PROJECT_ID, "舊廊", "潮濕而狹窄的木造走廊。"),
                    Location(PLAYER_LOCATION, PROJECT_ID, "後殿", "與舊廊分隔的後殿。"),
                ),
                scenes = listOf(Scene(SCENE_ID, PROJECT_ID, "離場觀察", "NPC 在玩家不在場時檢查環境。")),
            ),
        )
        val session = MultiTurnRuntime(repository).startSession(PROJECT_ID, SCENE_ID)
        return Fixture(repository, session.id, CapturingGenerator(unsafeDogSupport))
    }

    private data class Fixture(
        val repository: SqliteStoryRepository,
        val sessionId: String,
        val generator: CapturingGenerator,
    ) {
        fun prepare(
            physicalTargetId: String = "chair",
            preparationId: String = "npc-actor08-test-$physicalTargetId",
        ): LocalNpcActorFormalTurnCandidatePackage =
            LocalNpcActorFormalTurnPreparationService(repository, generator)
                .prepare(input(physicalTargetId, preparationId))

        private fun input(physicalTargetId: String, preparationId: String): LocalNpcActorFormalTurnInput {
            val suite = PhysicalAffordanceFixtureFactory.createSuite()
            val base = suite.snapshots.single { it.snapshotId == "snapshot-base" }
            return LocalNpcActorFormalTurnInput(
                preparationId = preparationId,
                sessionId = sessionId,
                playerInput = "[NPC_ACTOR08_NON_CANON_TICK] 玩家仍在後殿，沈霜只檢查舊廊的聲響。",
                profile = NpcAutonomyProfile(
                    npcId = ACTOR_ID,
                    deepMotivation = "保護現場，不讓未知危險擴大。",
                    falseBelief = "所有異聲都可能是入侵者造成。",
                    fear = "錯過真正的危險徵兆。",
                    offscreenAgenda = listOf("確認走廊聲響來源"),
                    voiceProfile = NpcVoiceProfile(
                        principles = listOf("先觀察再下判斷"),
                        forbiddenPatterns = listOf("AUTHOR_ONLY"),
                    ),
                ),
                physicalContext = NpcActorPhysicalEngineContext(
                    requestId = "npc-actor08-request-$physicalTargetId",
                    physicalArchetypes = suite.archetypes,
                    physicalSnapshot = base.copy(
                        snapshotId = "npc-actor08-test-snapshot",
                        entities = base.entities.map { if (it.entityId == "adult") it.copy(entityId = ACTOR_ID) else it },
                    ),
                    allowedPhysicalActionTypes = listOf(PhysicalActionType.USE_AS_SUPPORT),
                    allowedPhysicalTargetIds = listOf(physicalTargetId),
                ),
            )
        }
    }

    private class CapturingGenerator(private val unsafeDogSupport: Boolean) : LocalNpcActorGenerator {
        override fun generate(
            prompt: NpcActorPhysicalPrompt,
            repairRequest: NpcActorSafetyRepairRequest?,
        ): LocalNpcActorGenerationResult {
            val target = prompt.visiblePhysicalTargetIds.single()
            val response = NpcActorPhysicalResponse(
                requestId = prompt.actorRequest.requestId,
                actorResponse = NpcActorResponse(
                    requestId = prompt.actorRequest.requestId,
                    actorId = prompt.actorRequest.actorId,
                    speechAct = "NONVERBAL",
                    immediateGoal = "確認聲響而不破壞現場",
                    beliefClaimIds = prompt.actorRequest.ownedBeliefs.take(1).map { it.evidenceId },
                    memoryIds = prompt.actorRequest.ownedMemories.take(1).map { it.evidenceId },
                    emotionShown = "警覺",
                    emotionHidden = "擔心判斷失誤",
                    actionType = "inspect_local_pressure",
                    actionIntent = "借穩固物件調整姿勢並查看地板痕跡",
                    visibleAction = if (unsafeDogSupport) "沈霜把重量壓向狗的肩側。" else "沈霜扶住椅背，蹲低查看木板接縫。",
                    utterance = "",
                    nonverbalAction = "她停了一拍，先聽清第二次聲響。",
                    predictedCost = "十分鐘與短暫暴露",
                    observableConsequence = "舊廊留下她檢查過木板接縫的可見痕跡。",
                    confidence = 78,
                    decisionSummary = NpcActorDecisionSummary(
                        evidenceIds = prompt.actorRequest.goals.take(1).map { it.evidenceId },
                        selectedActionLabel = "bounded_inspection",
                        rejectedActionLabels = listOf("追出走廊"),
                    ),
                ),
                physicalActionProposal = PhysicalActionProposal(
                    actionId = "npc-actor08-action-${if (unsafeDogSupport) "dog" else "chair"}",
                    actionType = PhysicalActionType.USE_AS_SUPPORT,
                    actorId = prompt.actorRequest.actorId,
                    targetId = target,
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
                    seed = 8,
                    elapsedMillis = 10,
                    rawOutputBytes = 100,
                    promptSha256 = "1".repeat(64),
                    responseSha256 = "2".repeat(64),
                ),
                promptText = NpcActorJson.strict.encodeToString(prompt),
                rawOutput = "{}",
                responseJson = NpcActorJson.strict.encodeToString(response),
            )
        }
    }

    private fun decision(
        candidate: LocalNpcActorFormalTurnCandidatePackage,
        type: LocalNpcActorFormalDecisionType,
    ): LocalNpcActorFormalTurnDecision {
        val payload = candidate.payload
        return LocalNpcActorFormalTurnDecision(
            decisionId = "npc-actor08-test-decision-${type.name.lowercase()}",
            decision = type,
            candidateId = payload.candidateId,
            candidateSha256 = candidate.candidateSha256,
            projectId = payload.projectId,
            sessionId = payload.sessionId,
            turnId = payload.turnId,
            contextId = payload.contextId,
            contextHash = payload.contextHash,
            turnNumber = payload.turnNumber,
            baseStateVersion = payload.baseStateVersion,
            resultId = payload.resultId,
            resultCanonicalSha256 = payload.resultCanonicalSha256,
            reviewerId = "TEST_HUMAN_REVIEWER",
            reviewedAt = "2026-07-15T12:00:00+08:00",
            acknowledgement = REQUIRED_ACKNOWLEDGEMENT,
            note = "Automated test fixture; not Product Owner content approval.",
            testOnly = true,
        )
    }

    companion object {
        private const val PROJECT_ID = "npc-actor08-formal-test"
        private const val SCENE_ID = "scene-npc-actor08"
        private const val ACTOR_ID = "char-shen-shuang"
        private const val PLAYER_ID = "char-player"
        private const val ACTOR_LOCATION = "loc-old-corridor"
        private const val PLAYER_LOCATION = "loc-rear-hall"
        private const val MEMORY_ID = "memory-actor-board-sound"
        private const val CLAIM_ID = "claim-actor-board-sound"
    }
}
