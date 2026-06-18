package com.localnovel.engine

import com.localnovel.engine.model.InitialRuntimeTemplate
import com.localnovel.engine.model.SettingPack
import com.localnovel.engine.model.SettingPackEntities
import com.localnovel.engine.model.SettingPackEntity
import com.localnovel.engine.model.SettingPackRelation
import com.localnovel.engine.model.SettingPackSource
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.ContextAuditService
import com.localnovel.engine.service.CanonSemanticValidator
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.service.PilotPreflightService
import com.localnovel.engine.service.SettingPackImportService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Pilot1StabilizationTest {
    @Test
    fun settingPackOpeningCharacterRequiresInitialRuntimeTemplate() {
        val repository = newRepository()
        repository.initialize()
        val pack = validPack().copy(
            entities = validPack().entities.copy(
                characters = listOf(validPack().entities.characters.first().copy(initialRuntimeTemplate = null)),
            ),
        )

        val report = CanonSemanticValidator(repository).validate(pack)

        assertFalse(report.isValid)
        assertTrue(report.issues.any { it.code == "missing_initial_runtime_template" })
    }

    @Test
    fun initialRuntimeTemplateEmotionRangeIsValidated() {
        val repository = newRepository()
        repository.initialize()
        val character = validPack().entities.characters.first().copy(
            initialRuntimeTemplate = validTemplate().copy(emotionState = mapOf("fear" to 101)),
        )
        val pack = validPack().copy(entities = validPack().entities.copy(characters = listOf(character)))

        val report = CanonSemanticValidator(repository).validate(pack)

        assertFalse(report.isValid)
        assertTrue(report.issues.any { it.code == "invalid_initial_emotion_state" })
    }

    @Test
    fun initialRuntimeTemplateCannotLeakHiddenSecret() {
        val repository = newRepository()
        repository.initialize()
        val secretText = "the hidden contract root"
        val character = validPack().entities.characters.first().copy(
            initialRuntimeTemplate = validTemplate().copy(initialGoal = "Investigate $secretText"),
        )
        val pack = validPack().copy(
            entities = validPack().entities.copy(
                characters = listOf(character),
                secrets = listOf(SettingPackEntity("secret-root", content = secretText, visibility = "author_only", revealed = false)),
            ),
        )

        val report = CanonSemanticValidator(repository).validate(pack)

        assertFalse(report.isValid)
        assertTrue(report.issues.any { it.code == "initial_runtime_knowledge_leak" || it.code == "author_only_knowledge_leak" })
    }

    @Test
    fun startSessionCreatesRuntimeCharacterStateFromTemplate() {
        val repository = importedRepository()
        val session = MultiTurnRuntime(repository).startSession("pilot-stab", "scene-gate")

        val state = repository.getCharacterState("pilot-stab", "char-warden")

        assertNotNull(state)
        assertEquals("loc-gate", state.locationId)
        assertEquals("alert", state.mood)
        assertEquals("Hold the gate and test the stranger's intent.", state.flags["current_goal"])
        assertEquals("alert", state.flags["dominant_emotion"])
        assertEquals("60", state.flags["emotion.alert"])
        assertEquals(1, repository.activeSceneInstanceCount(session.id))
    }

    @Test
    fun preparedContextIsAuditedAgainstExactUtf8BytesAndHash() {
        val repository = importedRepository()
        val session = MultiTurnRuntime(repository).startSession("pilot-stab", "scene-gate")
        val context = MultiTurnRuntime(repository).prepareTurn(session.id, "I stop before the door.")
        val file = Files.createTempDirectory("pilot1-audit").resolve("context.json")
        Files.writeString(file, JsonSupport.json.encodeToString(context))

        val report = ContextAuditService().auditFile(file)

        assertTrue(report.valid, report.violations.joinToString())
        assertTrue(report.actualBytes <= 24576)
        assertEquals(Files.readAllBytes(file).size, report.declaredActualBytes)
        assertTrue(report.hashValid)
        assertTrue(report.sections.any { it.section == "relevantSettings" })
    }

    @Test
    fun auditContextRejectsDeclaredByteMismatch() {
        val repository = importedRepository()
        val session = MultiTurnRuntime(repository).startSession("pilot-stab", "scene-gate")
        val context = MultiTurnRuntime(repository).prepareTurn(session.id, "I wait.").copy(
            contextBudget = MultiTurnRuntime(repository).prepareTurn(session.id, "I wait again.").contextBudget.copy(actualBytes = 0),
        )
        val file = Files.createTempDirectory("pilot1-audit-bad").resolve("context.json")
        Files.writeString(file, JsonSupport.json.encodeToString(context))

        val report = ContextAuditService().auditFile(file)

        assertFalse(report.valid)
        assertTrue(report.violations.any { it.startsWith("declared_actual_bytes_mismatch") })
    }

    @Test
    fun pilotPreflightPassesForFreshRuntimeSession() {
        val repository = importedRepository()
        val session = MultiTurnRuntime(repository).startSession("pilot-stab", "scene-gate")

        val report = PilotPreflightService(repository).run("pilot-stab", session.id)

        assertTrue(report.valid, report.checks.joinToString { "${it.name}:${it.passed}:${it.details}" })
        assertTrue(report.contextAudit?.valid == true)
        assertTrue(report.contextAudit!!.actualBytes <= 24576)
    }

    @Test
    fun pilotPreflightFailsWhenPendingContextExists() {
        val repository = importedRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("pilot-stab", "scene-gate")
        runtime.prepareTurn(session.id, "I create a pending turn.")

        val report = PilotPreflightService(repository).run("pilot-stab", session.id)

        assertFalse(report.valid)
        assertTrue(report.checks.any { it.name == "no_pending_context_conflict" && !it.passed })
    }

    @Test
    fun exportSessionEvidenceIsReadOnlyAndIncludesRuntimeState() {
        val repository = importedRepository()
        val session = MultiTurnRuntime(repository).startSession("pilot-stab", "scene-gate")
        val beforeTurns = repository.tableRows("turn_logs").size

        val export = com.localnovel.engine.service.SessionEvidenceExporter(repository).export(session.id)

        assertEquals(beforeTurns, repository.tableRows("turn_logs").size)
        assertEquals(session.id, export.session.id)
        assertEquals(1, export.activeSceneInstanceCount)
        assertTrue(export.characterStates.any { it.characterId == "char-warden" })
    }

    @Test
    fun cliAuditContextExportEvidenceAndPreflightCommandsWork() {
        val db = Files.createTempDirectory("pilot1-cli").resolve("story.db")
        val packFile = Files.createTempDirectory("pilot1-cli-pack").resolve("pack.json")
        Files.writeString(packFile, JsonSupport.json.encodeToString(validPack()))
        val cli = Cli()

        assertEquals(0, cli.run(listOf("init-db", "--db", db.toString())))
        assertEquals(0, cli.run(listOf("apply-setting-pack", "--db", db.toString(), "--file", packFile.toString())))
        assertEquals(0, cli.run(listOf("start-session", "--db", db.toString(), "--project", "pilot-stab", "--scene", "scene-gate")))
        val repository = SqliteStoryRepository(db)
        val sessionId = repository.tableRows("story_sessions").single()["id"].orEmpty()
        val contextFile = packFile.parent.resolve("context.json")
        val auditFile = packFile.parent.resolve("audit.json")
        val evidenceFile = packFile.parent.resolve("evidence.json")
        val preflightFile = packFile.parent.resolve("preflight.json")

        assertEquals(0, cli.run(listOf("prepare-turn", "--db", db.toString(), "--session", sessionId, "--input", "I listen.", "--out", contextFile.toString())))
        assertEquals(0, cli.run(listOf("audit-context", "--file", contextFile.toString(), "--out", auditFile.toString())))
        assertEquals(0, cli.run(listOf("export-session-evidence", "--db", db.toString(), "--session", sessionId, "--out", evidenceFile.toString())))
        assertEquals(1, cli.run(listOf("pilot-preflight", "--db", db.toString(), "--project", "pilot-stab", "--session", sessionId, "--out", preflightFile.toString())))
        assertTrue(Files.exists(auditFile))
        assertTrue(Files.exists(evidenceFile))
        assertTrue(Files.exists(preflightFile))
    }

    private fun importedRepository(): SqliteStoryRepository {
        val repository = newRepository()
        repository.initialize()
        SettingPackImportService(repository).apply(validPack())
        return repository
    }

    private fun newRepository(): SqliteStoryRepository =
        SqliteStoryRepository(Files.createTempDirectory("pilot1-stabilization-test").resolve("story.db"))

    private fun validTemplate(): InitialRuntimeTemplate =
        InitialRuntimeTemplate(
            initialLocationId = "loc-gate",
            initialGoal = "Hold the gate and test the stranger's intent.",
            dominantEmotion = "alert",
            emotionState = mapOf("alert" to 60, "doubt" to 35),
            availableActions = listOf("ask for identity", "block the gate", "signal item-bell"),
            relationshipDefaults = mapOf("player" to "unknown"),
            initiativeLevel = 40,
        )

    private fun validPack(): SettingPack =
        SettingPack(
            schemaVersion = "1.0",
            packId = "pack-pilot-stab",
            packVersion = 1,
            projectId = "pilot-stab",
            name = "Pilot Stabilization Pack",
            description = "Small canon slice for stabilization tests.",
            mode = "BASE",
            source = SettingPackSource(author = "test"),
            entities = SettingPackEntities(
                projects = listOf(
                    SettingPackEntity(
                        id = "pilot-stab",
                        name = "Pilot Stabilization",
                        summary = "A tiny gate scene.",
                        metadata = kotlinx.serialization.json.JsonObject(mapOf("activeSceneId" to kotlinx.serialization.json.JsonPrimitive("scene-gate"))),
                    ),
                ),
                locations = listOf(SettingPackEntity("loc-gate", name = "North Gate", description = "A narrow gate.")),
                characters = listOf(
                    SettingPackEntity(
                        "char-warden",
                        name = "Gate Warden",
                        role = "warden",
                        description = "A cautious local guard.",
                        initialRuntimeTemplate = validTemplate(),
                    ),
                ),
                worldRules = listOf(SettingPackEntity("rule-identity", title = "Identity matters", content = "Names and papers have legal force.", priority = 10)),
                items = listOf(SettingPackEntity("item-bell", name = "Signal Bell", description = "A hand bell.", initialOwnerId = "char-warden")),
                facts = listOf(SettingPackEntity("fact-gate-public", content = "The north gate is watched.", visibility = "public")),
                secrets = listOf(SettingPackEntity("secret-root", content = "The warden serves a hidden patron.", visibility = "author_only", revealed = false)),
                scenes = listOf(SettingPackEntity("scene-gate", title = "At the Gate", summary = "The warden blocks the gate.", locationId = "loc-gate")),
                sceneContracts = listOf(SettingPackEntity("contract-gate", sceneId = "scene-gate", forbiddenReveals = listOf("hidden patron"))),
            ),
            relations = listOf(
                SettingPackRelation("rel-warden-gate", "character", "char-warden", "location", "loc-gate", "LOCATED_IN"),
                SettingPackRelation("rel-scene-gate", "scene", "scene-gate", "location", "loc-gate", "LOCATED_IN"),
                SettingPackRelation("rel-warden-bell", "character", "char-warden", "item", "item-bell", "OWNS"),
            ),
        )
}
