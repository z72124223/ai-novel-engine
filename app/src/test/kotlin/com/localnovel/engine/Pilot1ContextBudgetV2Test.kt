package com.localnovel.engine

import com.localnovel.engine.model.ActiveStoryThreadView
import com.localnovel.engine.model.CanonRevisionSummary
import com.localnovel.engine.model.CharacterMemory
import com.localnovel.engine.model.CharacterProfile
import com.localnovel.engine.model.CharacterState
import com.localnovel.engine.model.ContextBudget
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.InitialRuntimeTemplate
import com.localnovel.engine.model.Item
import com.localnovel.engine.model.Project
import com.localnovel.engine.model.RelevantSetting
import com.localnovel.engine.model.Scene
import com.localnovel.engine.model.SceneContract
import com.localnovel.engine.model.SettingPack
import com.localnovel.engine.model.SettingPackEntities
import com.localnovel.engine.model.SettingPackEntity
import com.localnovel.engine.model.SettingPackRelation
import com.localnovel.engine.model.SettingPackSource
import com.localnovel.engine.model.Technique
import com.localnovel.engine.model.WorldRule
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.ContextAuditService
import com.localnovel.engine.service.ContextBudgetForensicsService
import com.localnovel.engine.service.ContextBudgetUnsatisfiableException
import com.localnovel.engine.service.ContextBudgeter
import com.localnovel.engine.service.ContextPacketFinalizer
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.service.SettingPackImportService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.nio.file.Files
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Pilot1ContextBudgetV2Test {
    @Test
    fun oversizedCandidateCanBeTrimmedUnderHardBudget() {
        val context = largeContext()
        val finalized = finalize(context, 10_000)

        assertTrue(finalized.contextBudget.actualBytes <= 10_000)
    }

    @Test
    fun maxContextBytesIsNotRaised() {
        val finalized = finalize(largeContext(), 10_000)

        assertEquals(10_000, finalized.contextBudget.maxBytes)
    }

    @Test
    fun mandatorySafetyFieldsAreRetainedAfterTrimming() {
        val finalized = finalize(largeContext(), 10_000)

        assertEquals("2", finalized.protocolVersion)
        assertTrue(finalized.sceneContract?.forbiddenReveals?.contains("hidden phrase") == true)
        assertTrue(finalized.worldRules.any { it.id == "rule-hard" })
        assertTrue(finalized.characters.any { it.id == "char-warden" })
        assertTrue(finalized.characterStates.any { it.characterId == "char-warden" })
    }

    @Test
    fun activeItemAndTechniqueLimitationsStayAvailable() {
        val finalized = finalize(largeContext(), 10_000)

        assertTrue(finalized.items.single { it.id == "item-bell" }.description.contains("cannot be rung silently"))
        assertTrue(finalized.techniques.single { it.id == "tech-breath" }.description.contains("costs breath"))
    }

    @Test
    fun duplicateSettingSummaryIsCompactedToShortReference() {
        val context = largeContext().copy(
            relevantSettings = listOf(
                RelevantSetting(
                    entityType = "world_rule",
                    entityId = "rule-hard",
                    name = "Identity Law",
                    summary = "Names and papers have legal force. Names and papers have legal force.",
                    relationPath = listOf("world_rule:rule-hard-APPLIES_TO->scene:scene-gate", "world_rule:rule-hard-APPLIES_TO->character:char-warden"),
                    requiredRuntimeUse = true,
                ),
            ),
        )
        val finalized = finalize(context, 10_000)

        assertTrue(finalized.relevantSettings.isEmpty() || finalized.relevantSettings.first().summary.length < context.relevantSettings.first().summary.length)
    }

    @Test
    fun dynamicSettingBudgetDoesNotReserveEightKilobytes() {
        val finalized = finalize(largeContext(), 10_000)

        assertTrue(finalized.settingContextBudget.actualBytes <= JsonSupport.json.encodeToString(finalized.relevantSettings).toByteArray(Charsets.UTF_8).size)
        assertTrue(finalized.contextBudget.actualBytes <= finalized.contextBudget.maxBytes)
    }

    @Test
    fun finalActualBytesEqualsSerializedFileBytes() {
        val finalized = finalize(largeContext(), 10_000)
        val file = Files.createTempDirectory("budget-v2-bytes").resolve("context.json")
        Files.writeString(file, JsonSupport.json.encodeToString(finalized))

        assertEquals(Files.readAllBytes(file).size, finalized.contextBudget.actualBytes)
        assertTrue(ContextAuditService(maxBytes = 10_000).auditFile(file).valid)
    }

    @Test
    fun contextHashUsesFinalTrimmedPayload() {
        val finalized = finalize(largeContext(), 10_000)

        assertEquals(hash(finalized), finalized.contextHash)
    }

    @Test
    fun trimmingIsDeterministic() {
        val first = finalize(largeContext(), 10_000)
        val second = finalize(largeContext(), 10_000)

        assertEquals(JsonSupport.json.encodeToString(first), JsonSupport.json.encodeToString(second))
    }

    @Test
    fun forensicsReportsNoDanglingReferences() {
        val before = largeContext()
        val after = finalize(before, 10_000)
        val report = ContextBudgetForensicsService(10_000).report(before, after)

        assertTrue(report.danglingReferenceCheck.valid, report.danglingReferenceCheck.missingReferences.joinToString())
    }

    @Test
    fun failedPrepareWritesBudgetFailureReportReadOnly() {
        val db = importedDb()
        val repository = SqliteStoryRepository(db)
        val session = MultiTurnRuntime(repository).startSession("budget-v2", "scene-gate")
        val beforeTurns = repository.tableRows("turn_logs").size
        val out = Files.createTempDirectory("budget-v2-cli-fail").resolve("context.json")
        val report = out.parent.resolve("failure.json")
        val exit = Cli().run(listOf("prepare-turn", "--db", db.toString(), "--session", session.id, "--input", "界".repeat(30_000), "--out", out.toString(), "--budget-report", report.toString()))

        assertEquals(1, exit)
        assertFalse(Files.exists(out))
        assertTrue(Files.exists(report))
        assertEquals(beforeTurns, repository.tableRows("turn_logs").size)
    }

    @Test
    fun explainContextBudgetCliIsReadOnly() {
        val db = importedDb()
        val repository = SqliteStoryRepository(db)
        val session = MultiTurnRuntime(repository).startSession("budget-v2", "scene-gate")
        val beforeTurns = repository.tableRows("turn_logs").size
        val beforeStateVersion = repository.getSession(session.id)?.stateVersion
        val input = Files.createTempDirectory("budget-v2-explain").resolve("input.txt")
        val out = input.parent.resolve("explain.json")
        Files.writeString(input, "我站在門前觀察。")

        val exit = Cli().run(listOf("explain-context-budget", "--db", db.toString(), "--session", session.id, "--input-file", input.toString(), "--out", out.toString()))

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        assertEquals(beforeTurns, repository.tableRows("turn_logs").size)
        assertEquals(beforeStateVersion, repository.getSession(session.id)?.stateVersion)
    }

    @Test
    fun impossibleMandatoryPayloadFailsWithDiagnostic() {
        val context = baseContext(playerInput = "界".repeat(5000), relevantSettings = emptyList()).copy(
            contextBudget = ContextBudget(maxBytes = 1000),
        )

        val error = assertFailsWith<ContextBudgetUnsatisfiableException> { finalize(context, 1000) }

        assertNotNull(error.report)
        assertFalse(error.report!!.canFit)
        assertTrue(error.report!!.bytesOverLimit > 0)
    }

    @Test
    fun failedPrepareDoesNotCreatePendingContext() {
        val db = importedDb()
        val repository = SqliteStoryRepository(db)
        val session = MultiTurnRuntime(repository).startSession("budget-v2", "scene-gate")
        Cli().run(listOf("prepare-turn", "--db", db.toString(), "--session", session.id, "--input", "界".repeat(30_000), "--out", Files.createTempDirectory("budget-v2-pending").resolve("context.json").toString()))

        val pending = repository.tableRows("turn_logs").count { it["session_id"] == session.id && it["validation_status"] == "pending" }

        assertEquals(0, pending)
    }

    @Test
    fun failedPrepareDoesNotAdvanceStateVersionOrTurnNumber() {
        val db = importedDb()
        val repository = SqliteStoryRepository(db)
        val session = MultiTurnRuntime(repository).startSession("budget-v2", "scene-gate")
        Cli().run(listOf("prepare-turn", "--db", db.toString(), "--session", session.id, "--input", "界".repeat(30_000), "--out", Files.createTempDirectory("budget-v2-state").resolve("context.json").toString()))

        val after = repository.getSession(session.id)

        assertEquals(session.stateVersion, after?.stateVersion)
        assertEquals(session.nextTurnNumber, after?.nextTurnNumber)
    }

    @Test
    fun abortPendingTurnRemovesOnlyUnresolvedContextLog() {
        val db = importedDb()
        val repository = SqliteStoryRepository(db)
        val session = MultiTurnRuntime(repository).startSession("budget-v2", "scene-gate")
        val context = MultiTurnRuntime(repository).prepareTurn(session.id, "I listen.")
        val beforeAbort = repository.tableRows("turn_logs")

        val aborted = repository.abortPendingContext(session.id, context.contextId)

        val afterAbort = repository.tableRows("turn_logs")
        assertEquals(1, aborted)
        assertEquals(beforeAbort.size - 1, afterAbort.size)
        assertEquals(0, afterAbort.count { it["session_id"] == session.id && it["validation_status"] == "pending" })
        assertEquals(session.stateVersion, repository.getSession(session.id)?.stateVersion)
        assertEquals(session.nextTurnNumber, repository.getSession(session.id)?.nextTurnNumber)
    }

    @Test
    fun copyE2ePrepareAuditAbortLeavesNoFormalTurnFourLog() {
        val db = importedDb()
        val repository = SqliteStoryRepository(db)
        val session = MultiTurnRuntime(repository).startSession("budget-v2", "scene-gate")
        val context = MultiTurnRuntime(repository).prepareTurn(session.id, "我仍留在原地觀察。")
        val audit = ContextAuditService().audit(context)

        repository.abortPendingContext(session.id, context.contextId)

        assertTrue(audit.valid)
        assertEquals(0, repository.tableRows("turn_logs").count { it["session_id"] == session.id })
        assertEquals(0, repository.tableRows("state_deltas").count { it["project_id"] == "budget-v2" })
        assertEquals(session.stateVersion, repository.getSession(session.id)?.stateVersion)
        assertEquals(session.nextTurnNumber, repository.getSession(session.id)?.nextTurnNumber)
    }

    @Test
    fun utf8ChinesePlayerInputBytesAreAccounted() {
        val context = finalize(baseContext(playerInput = "我盯著影子。"), 10_000)
        val inputBytes = "我盯著影子。".toByteArray(Charsets.UTF_8).size

        assertTrue(inputBytes > "我盯著影子。".length)
        assertEquals(JsonSupport.json.encodeToString(context).toByteArray(Charsets.UTF_8).size, context.contextBudget.actualBytes)
    }

    @Test
    fun explainDoesNotModifyExistingEvidenceFiles() {
        val db = importedDb()
        val repository = SqliteStoryRepository(db)
        val session = MultiTurnRuntime(repository).startSession("budget-v2", "scene-gate")
        val sentinel = Files.createTempDirectory("budget-v2-evidence").resolve("evidence.json")
        Files.writeString(sentinel, "sentinel")

        MultiTurnRuntime(repository).explainContextBudget(session.id, "我等待。")

        assertEquals("sentinel", Files.readString(sentinel))
    }

    @Test
    fun forbiddenSecretDoesNotEnterCompactContext() {
        val finalized = finalize(baseContext(playerInput = "我等待。"), 10_000)
        val text = JsonSupport.json.encodeToString(finalized)

        assertFalse(text.contains("the hidden patron controls the gate"))
    }

    @Test
    fun lowRelevanceSettingsAreTrimmedBeforeHighPriorityMemory() {
        val memory = com.localnovel.engine.model.RetrievedMemory(
            memoryId = "memory-important",
            ownerCharacterId = "char-warden",
            type = "EPISODIC",
            summary = "The stranger once stopped before a danger line.",
            emotionalSignature = JsonObject(mapOf("alert" to JsonPrimitive("high"))),
            confidence = 90,
            salience = 90,
            retrievalScore = 999,
        )
        val context = largeContext().copy(retrievedMemories = listOf(memory), characterMemories = emptyList())
        val finalized = finalize(context, 10_000)

        assertTrue(finalized.retrievedMemories.any { it.memoryId == "memory-important" })
        assertTrue(finalized.settingContextBudget.omittedSettingCount > 0 || finalized.relevantSettings.size < context.relevantSettings.size)
    }

    @Test
    fun currentCharacterRuntimeStateKeepsNecessaryFields() {
        val finalized = finalize(largeContext(), 10_000)
        val state = finalized.characterStates.single { it.characterId == "char-warden" }

        assertEquals("Hold position and test the stranger.", state.flags["current_goal"])
        assertEquals("alert", state.flags["dominant_emotion"])
        assertEquals("65", state.flags["emotion.alert"])
        assertTrue(state.flags["available_actions"].orEmpty().contains("item-bell"))
    }

    private fun finalize(context: ContextPacket, maxBytes: Int): ContextPacket {
        val budgeted = ContextBudgeter(maxBytes).applyBudget(context, omittedMemoryCount = 0, omittedKnowledgeCount = 0)
        return ContextPacketFinalizer(maxBytes).finalize(budgeted, ::hash)
    }

    private fun largeContext(): ContextPacket {
        val settings = (1..18).map {
            RelevantSetting(
                entityType = "item",
                entityId = "setting-$it",
                name = "Setting $it",
                summary = "Names and papers have legal force. Names and papers have legal force. " + "extra ".repeat(80),
                relationPath = listOf("item:setting-$it-RELATED_TO->scene:scene-gate", "item:setting-$it-RELATED_TO->character:char-warden"),
                requiredRuntimeUse = false,
            )
        }
        return baseContext(
            playerInput = "我用中文測試聲音方向。",
            relevantSettings = settings,
        ).copy(
            recentTurnSummaries = (1..6).map { com.localnovel.engine.model.TurnSummary(it, "result-$it", "summary ".repeat(80)) },
            canonRevisionSummary = CanonRevisionSummary(
                importedPacks = listOf("pack-budget-v2@1"),
                relevantRevisionIds = (1..30).map { "entity-revision-${it.toString().padStart(3, '0')}-aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee" },
                latestImportId = "setting-import-aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
            ),
        )
    }

    private fun baseContext(
        playerInput: String,
        relevantSettings: List<RelevantSetting> = emptyList(),
    ): ContextPacket {
        val template = JsonObject(
            mapOf(
                "initialRuntimeTemplate" to JsonObject(
                    mapOf(
                        "initialGoal" to JsonPrimitive("Hold position and test the stranger."),
                        "dominantEmotion" to JsonPrimitive("alert"),
                        "availableActions" to JsonPrimitive("ask for identity; use item-bell"),
                    ),
                ),
            ),
        )
        return ContextPacket(
            project = Project("budget-v2", "Budget V2", "Names and papers have legal force. Names and papers have legal force.", "scene-gate"),
            turnId = "session-budget-turn-1",
            turnNumber = 1,
            playerInput = playerInput,
            activeScene = Scene("scene-gate", "budget-v2", "Gate", "A gate scene.", "loc-gate"),
            sceneContract = SceneContract("contract-gate", "budget-v2", "scene-gate", forbiddenReveals = listOf("hidden phrase")),
            characters = listOf(
                CharacterProfile("char-player", "budget-v2", "Player", "player", "A careful stranger.", isPlayer = true, profile = template),
                CharacterProfile("char-warden", "budget-v2", "Warden", "guard", "A cautious guard who respects procedure.", profile = template),
            ),
            characterStates = listOf(
                CharacterState(
                    "char-player",
                    "budget-v2",
                    "loc-gate",
                    "calm",
                    "normal",
                    inventory = listOf("item-bell"),
                    flags = mapOf(
                        "current_goal" to "Stay back and observe.",
                        "dominant_emotion" to "calm",
                        "available_actions" to "wait; point; speak",
                        "emotion.calm" to "60",
                    ),
                ),
                CharacterState(
                    "char-warden",
                    "budget-v2",
                    "loc-gate",
                    "alert",
                    "normal",
                    flags = mapOf(
                        "current_goal" to "Hold position and test the stranger.",
                        "dominant_emotion" to "alert",
                        "available_actions" to "ask for identity; use item-bell",
                        "emotion.alert" to "65",
                    ),
                ),
            ),
            worldRules = listOf(WorldRule("rule-hard", "budget-v2", "Identity Law", "Names and papers have legal force. Names and papers have legal force.", 10)),
            facts = listOf(com.localnovel.engine.model.Fact("fact-public", "budget-v2", "The gate is watched.", "public", "seed", 0)),
            items = listOf(Item("item-bell", "budget-v2", "Signal Bell", "A signal bell that cannot be rung silently and alerts nearby guards.", "char-player")),
            techniques = listOf(Technique("tech-breath", "budget-v2", "Breath Method", "A breath method that costs breath and cannot cross sealed doors.", "char-warden")),
            contextId = "ctx-budget",
            sessionId = "session-budget",
            baseStateVersion = 0,
            generatedAt = "2026-01-01T00:00:00Z",
            activeStoryThreads = listOf(ActiveStoryThreadView("thread-shelter", "survival", "Find lawful shelter.", "OPEN", 0, 0, "Use evidence.")),
            relevantSettings = relevantSettings,
        )
    }

    private fun importedDb(): java.nio.file.Path {
        val db = Files.createTempDirectory("budget-v2-db").resolve("story.db")
        val repository = SqliteStoryRepository(db)
        repository.initialize()
        SettingPackImportService(repository).apply(validPack())
        return db
    }

    private fun validPack(): SettingPack =
        SettingPack(
            schemaVersion = "1.0",
            packId = "pack-budget-v2",
            packVersion = 1,
            projectId = "budget-v2",
            name = "Budget V2",
            mode = "BASE",
            source = SettingPackSource(author = "test"),
            entities = SettingPackEntities(
                projects = listOf(SettingPackEntity(id = "budget-v2", name = "Budget V2", summary = "Small test.", metadata = JsonObject(mapOf("activeSceneId" to JsonPrimitive("scene-gate"))))),
                locations = listOf(SettingPackEntity("loc-gate", name = "Gate", description = "A gate.")),
                characters = listOf(
                    SettingPackEntity(
                        "char-warden",
                        name = "Warden",
                        role = "guard",
                        description = "Cautious.",
                        initialRuntimeTemplate = InitialRuntimeTemplate(
                            initialLocationId = "loc-gate",
                            initialGoal = "Guard the gate.",
                            dominantEmotion = "alert",
                            emotionState = mapOf("alert" to 60),
                            availableActions = listOf("ask for identity", "use item-bell"),
                            initiativeLevel = 40,
                        ),
                    ),
                ),
                worldRules = listOf(SettingPackEntity("rule-hard", title = "Identity Law", content = "Names matter.", priority = 10)),
                items = listOf(SettingPackEntity("item-bell", name = "Bell", description = "A bell.", initialOwnerId = "char-warden")),
                facts = listOf(SettingPackEntity("fact-public", content = "The gate is watched.", visibility = "public")),
                secrets = listOf(SettingPackEntity("secret-hidden", content = "the hidden patron controls the gate", visibility = "author_only", revealed = false)),
                scenes = listOf(SettingPackEntity("scene-gate", title = "Gate", summary = "At the gate.", locationId = "loc-gate")),
                sceneContracts = listOf(SettingPackEntity("contract-gate", sceneId = "scene-gate", forbiddenReveals = listOf("hidden patron"))),
            ),
            relations = listOf(
                SettingPackRelation("rel-warden-gate", "character", "char-warden", "location", "loc-gate", "LOCATED_IN"),
                SettingPackRelation("rel-scene-gate", "scene", "scene-gate", "location", "loc-gate", "LOCATED_IN"),
            ),
        )

    private fun hash(context: ContextPacket): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(JsonSupport.json.encodeToString(context.copy(contextHash = "")).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
