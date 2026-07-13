package com.localnovel.engine

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.DemoProjectSeed
import com.localnovel.engine.model.FormalPhysicalStateTransitionPlan
import com.localnovel.engine.model.FormalPhysicalStateView
import com.localnovel.engine.model.FormalTurnPhysicalValidationContext
import com.localnovel.engine.model.PhysicalActionProposal
import com.localnovel.engine.model.PhysicalActionType
import com.localnovel.engine.model.PhysicalEvidenceFile
import com.localnovel.engine.model.PhysicalEvidenceManifest
import com.localnovel.engine.model.PhysicalWorldSnapshot
import com.localnovel.engine.model.Project
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.FormalPhysicalStateHash
import com.localnovel.engine.service.FormalPhysicalStatePersistenceValidator
import com.localnovel.engine.service.FormalTurnPhysicalGateFixtureFactory
import com.localnovel.engine.service.FormalTurnPhysicalGateValidator
import com.localnovel.engine.service.FormalTurnPhysicalJson
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.service.PhysicalAffordanceFixtureFactory
import com.localnovel.engine.service.PhysicalStateReducer
import com.localnovel.engine.service.ResolveTurnOptions
import com.localnovel.engine.support.JsonSupport
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class FormalPhysicalStatePersistenceTest {
    private val physicalSuite = PhysicalAffordanceFixtureFactory.createSuite()
    private val gate = FormalTurnPhysicalGateValidator()

    @Test
    fun `migration 016 is additive and initialization is idempotent`() {
        val repository = newRepository()

        repository.initialize()
        repository.initialize()

        assertEquals(0, repository.tableCount("physical_archetype_registries"))
        assertEquals(0, repository.tableCount("formal_physical_states"))
        assertEquals(0, repository.tableCount("formal_physical_state_transitions"))
    }

    @Test
    fun `V1 transient compatibility and stateful fail closed remain stable`() {
        val suite = FormalTurnPhysicalGateFixtureFactory.createSuite()
        val transient = suite.cases.single { it.caseId == "case-01-chair-support-pass" }
        val stateful = suite.cases.single { it.caseId == "case-08-stateful-drop-blocked" }

        assertTrue(gate.validate(transient.context, transient.result, transient.physicalContext).isEmpty())
        assertTrue(
            gate.validate(stateful.context, stateful.result, stateful.physicalContext)
                .any { it.code == FormalTurnPhysicalGateValidator.PHYSICAL_STATE_PERSISTENCE_UNAVAILABLE },
        )
    }

    @Test
    fun `V2 canonical hashes and duplicate action ids fail closed`() {
        val context = evidenceContext()
        val lift = proposal("lift-01-light-crate", "duplicate")
        val drop = proposal("drop-01-held-above-ground", "duplicate")
        val result = physicalResult(context, "wp04-hash-result", listOf(lift, drop), includeCharacterAction = false)
        val physicalContext = v2Context(context, result, snapshot("snapshot-base"), 0)

        val duplicateErrors = gate.validate(context, result, physicalContext)
        val hashErrors = gate.validate(
            context,
            result,
            physicalContext.copy(physicalSnapshotCanonicalSha256 = "0".repeat(64)),
        )

        assertTrue(duplicateErrors.any { it.code == FormalTurnPhysicalGateValidator.PHYSICAL_ACTION_IDS_INVALID })
        assertTrue(hashErrors.any { it.code == FormalTurnPhysicalGateValidator.PHYSICAL_SNAPSHOT_HASH_MISMATCH })
    }

    @Test
    fun `all six stateful action effects reduce deterministically`() {
        val liftDrop = plan(
            snapshotId = "snapshot-base",
            sourceCaseIds = listOf("lift-01-light-crate", "drop-01-held-above-ground"),
        )
        val dropped = liftDrop.finalSnapshot.entity("light-crate")
        assertEquals(2, liftDrop.finalStateVersion)
        assertNull(dropped.heldByEntityId)
        assertEquals(15.0, dropped.centerCm.z)

        val pushed = plan("snapshot-base", listOf("push-01-heavy-crate")).finalSnapshot.entity("heavy-crate")
        assertEquals(140.0, pushed.centerCm.x)
        assertNull(pushed.heldByEntityId)

        val placedPlan = plan("snapshot-holding-light", listOf("place-01-light-on-table"))
        val placed = placedPlan.finalSnapshot.entity("light-crate")
        val table = placedPlan.finalSnapshot.entity("table")
        assertEquals("table", placed.supportedByEntityId)
        assertEquals("top", placed.supportedBySurfaceId)
        assertEquals(10.0, table.supportLoadKgBySurface["top"])

        val entered = plan("snapshot-base", listOf("enter-01-large-container")).finalSnapshot.entity("adult")
        assertEquals("large-container", entered.containedByEntityId)
        assertEquals(plan("snapshot-base", listOf("enter-01-large-container")).finalSnapshot.entity("large-container").centerCm, entered.centerCm)

        val passed = plan("snapshot-base", listOf("pass-01-wide-door")).finalSnapshot.entity("adult")
        assertEquals(160.0, passed.centerCm.x)
        assertNull(passed.containedByEntityId)

        assertEquals(
            setOf(
                PhysicalActionType.LIFT,
                PhysicalActionType.PUSH,
                PhysicalActionType.PLACE_ON,
                PhysicalActionType.DROP,
                PhysicalActionType.ENTER_CONTAINER,
                PhysicalActionType.PASS_THROUGH,
            ),
            (liftDrop.transitions +
                plan("snapshot-base", listOf("push-01-heavy-crate")).transitions +
                placedPlan.transitions +
                plan("snapshot-base", listOf("enter-01-large-container")).transitions +
                plan("snapshot-base", listOf("pass-01-wide-door")).transitions)
                .map { it.actionType }
                .toSet(),
        )
    }

    @Test
    fun `formal runtime persists sequential lift and drop then rejects stale and duplicate inputs`() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "Inspect the crate and move it safely.")
        val result = physicalResult(
            context,
            "wp04-runtime-result-1",
            listOf(
                proposal("lift-01-light-crate", "wp04-lift"),
                proposal("drop-01-held-above-ground", "wp04-drop"),
            ),
        )
        val physicalContext = v2Context(context, result, snapshot("snapshot-base"), 0)

        runtime.resolveTurn(
            session.id,
            result,
            ResolveTurnOptions(formalTurnPhysicalContext = physicalContext),
        )

        val persisted = assertNotNull(repository.getFormalPhysicalState(session.id))
        assertEquals(2, persisted.physicalStateVersion)
        assertEquals(2, repository.formalPhysicalTransitionCount(session.id))
        assertEquals(15.0, persisted.physicalSnapshot.entity("light-crate").centerCm.z)
        assertEquals(1, repository.getSession(session.id)?.stateVersion)

        val stateViewPath = Files.createTempFile("wp04-physical-state-view", ".json")
        assertEquals(
            0,
            Cli().run(
                listOf(
                    "show-physical-state",
                    "--db",
                    repository.databasePath().toString(),
                    "--session",
                    session.id,
                    "--out",
                    stateViewPath.toString(),
                ),
            ),
        )
        val stateView = FormalTurnPhysicalJson.strict.decodeFromString<FormalPhysicalStateView>(Files.readString(stateViewPath))
        assertEquals("PERSISTED", stateView.status)
        assertEquals(2, stateView.transitionCount)
        assertEquals(2, stateView.state?.physicalStateVersion)

        val nextContext = runtime.prepareTurn(session.id, "Lift the same crate again.")
        val duplicateResult = physicalResult(
            nextContext,
            result.resultId,
            listOf(proposal("lift-01-light-crate", "wp04-next-lift")),
        )
        val currentPhysicalContext = v2Context(
            nextContext,
            duplicateResult,
            persisted.physicalSnapshot,
            persisted.physicalStateVersion,
        )
        val duplicateReport = runtime.validate(nextContext, duplicateResult, currentPhysicalContext)
        assertTrue(duplicateReport.errors.any { it.code == "duplicate_result" })

        val uniqueResult = duplicateResult.copy(resultId = "wp04-runtime-result-2")
        val stalePhysicalContext = v2Context(
            nextContext,
            uniqueResult,
            persisted.physicalSnapshot,
            persisted.physicalStateVersion - 1,
        )
        val staleReport = runtime.validate(nextContext, uniqueResult, stalePhysicalContext)
        assertTrue(staleReport.errors.any { it.code == FormalPhysicalStatePersistenceValidator.PHYSICAL_STATE_STALE_VERSION })
        assertEquals(2, repository.getFormalPhysicalState(session.id)?.physicalStateVersion)
    }

    @Test
    fun `later story write failure rolls physical and story state back together`() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "Attempt one bounded physical action.")
        val invalidStoryWrite = DeltaOperation(
            type = DeltaOperationType.UPDATE_CHARACTER_STATE,
            targetType = "character_state",
            targetId = "missing-character-state",
            payload = JsonObject(emptyMap()),
        )
        val result = physicalResult(
            context,
            "wp04-rollback-result",
            listOf(proposal("lift-01-light-crate", "wp04-rollback-lift")),
        ).copy(stateDeltas = listOf(invalidStoryWrite))
        val physicalContext = v2Context(context, result, snapshot("snapshot-base"), 0)
        val before = repository.getSession(session.id)

        val failure = assertFails {
            repository.resolvePendingTurn(
                context = context,
                result = result,
                hasStateChange = true,
                formalTurnPhysicalContext = physicalContext,
            )
        }

        assertTrue(failure.message.orEmpty().contains("Character state not found"))
        assertNull(repository.getFormalPhysicalState(session.id))
        assertEquals(0, repository.formalPhysicalTransitionCount(session.id))
        assertEquals(0, repository.tableCount("physical_archetype_registries"))
        assertEquals(0, repository.tableCount("state_deltas"))
        assertEquals(before?.stateVersion, repository.getSession(session.id)?.stateVersion)
        assertEquals(0, repository.appliedTurnCount(session.id))
        assertNotNull(repository.getPendingContext(session.id, context.contextId))
    }

    @Test
    fun `committed V2 evidence is strict hash bound and reproducible`() {
        writeEvidenceWhenRequested()
        val root = repositoryRoot()
        val paths = listOf(CONTEXT_PATH, RESULT_PATH, PLAN_PATH, REPORT_PATH, SCHEMA_PATH, MIGRATION_PATH)
        if (paths.any { !Files.isRegularFile(root.resolve(it)) } || !Files.isRegularFile(root.resolve(MANIFEST_PATH))) return

        val result = FormalTurnPhysicalJson.strict.decodeFromString<AiTurnResult>(Files.readString(root.resolve(RESULT_PATH)))
        val context = FormalTurnPhysicalJson.strict.decodeFromString<FormalTurnPhysicalValidationContext>(Files.readString(root.resolve(CONTEXT_PATH)))
        val committedPlan = FormalTurnPhysicalJson.strict.decodeFromString<FormalPhysicalStateTransitionPlan>(Files.readString(root.resolve(PLAN_PATH)))
        val schema = Json.parseToJsonElement(Files.readString(root.resolve(SCHEMA_PATH))).jsonObject
        val report = Json.parseToJsonElement(Files.readString(root.resolve(REPORT_PATH))).jsonObject

        assertEquals("false", schema["additionalProperties"].toString())
        assertTrue(schema["required"].toString().contains("physicalStateVersion"))
        assertEquals(gate.canonicalResultSha256(result), context.resultCanonicalSha256)
        assertEquals(FormalPhysicalStateHash.snapshot(context.physicalSnapshot), context.physicalSnapshotCanonicalSha256)
        assertEquals(FormalPhysicalStateHash.archetypeRegistry(context.physicalArchetypes), context.physicalArchetypeRegistryCanonicalSha256)
        assertEquals(PhysicalStateReducer().plan(context, result), committedPlan)
        assertEquals("WORLD_PHYSICS04_PHYSICAL_STATE_PERSISTENCE_PASS", report["technicalDecision"]?.jsonPrimitive?.content)

        val manifest = FormalTurnPhysicalJson.strict.decodeFromString<PhysicalEvidenceManifest>(Files.readString(root.resolve(MANIFEST_PATH)))
        assertEquals("WORLD-PHYSICS-04 PHYSICAL STATE PERSISTENCE", manifest.scope)
        manifest.files.forEach { file ->
            val path = root.resolve(file.path)
            assertEquals(file.bytes, Files.size(path), file.path)
            assertEquals(file.sha256, sha256(path), file.path)
        }
    }

    private fun plan(snapshotId: String, sourceCaseIds: List<String>): FormalPhysicalStateTransitionPlan {
        val context = evidenceContext()
        val proposals = sourceCaseIds.mapIndexed { index, caseId -> proposal(caseId, "wp04-${index + 1}-${caseId.substringBefore('-')}") }
        val result = physicalResult(context, "wp04-plan-${sourceCaseIds.joinToString("-")}", proposals, includeCharacterAction = false)
        val physicalContext = v2Context(context, result, snapshot(snapshotId), 0)
        return PhysicalStateReducer().plan(physicalContext, result)
    }

    private fun proposal(sourceCaseId: String, actionId: String): PhysicalActionProposal =
        physicalSuite.cases.single { it.caseId == sourceCaseId }.proposal.copy(actionId = actionId)

    private fun snapshot(snapshotId: String): PhysicalWorldSnapshot =
        physicalSuite.snapshots.single { it.snapshotId == snapshotId }.copy(
            snapshotId = "wp04-$snapshotId-v0",
            boundary = FormalTurnPhysicalGateValidator.FORMAL_TURN_BOUNDARY,
        )

    private fun v2Context(
        context: ContextPacket,
        result: AiTurnResult,
        snapshot: PhysicalWorldSnapshot,
        physicalStateVersion: Int,
    ): FormalTurnPhysicalValidationContext = FormalTurnPhysicalValidationContext(
        schemaVersion = PhysicalStateReducer.SCHEMA_VERSION,
        projectId = context.project.id,
        sessionId = context.sessionId,
        turnId = context.turnId,
        contextId = context.contextId,
        contextHash = context.contextHash,
        turnNumber = context.turnNumber,
        baseStateVersion = context.baseStateVersion,
        resultId = result.resultId,
        resultCanonicalSha256 = gate.canonicalResultSha256(result),
        physicalStateVersion = physicalStateVersion,
        physicalSnapshotCanonicalSha256 = FormalPhysicalStateHash.snapshot(snapshot),
        physicalArchetypeRegistryCanonicalSha256 = FormalPhysicalStateHash.archetypeRegistry(physicalSuite.archetypes),
        physicalArchetypes = physicalSuite.archetypes,
        physicalSnapshot = snapshot,
        allowedPhysicalActionTypes = result.physicalActionProposals.map { it.actionType }.distinct(),
        allowedPhysicalActorIds = result.physicalActionProposals.map { it.actorId }.distinct(),
        allowedPhysicalTargetIds = result.physicalActionProposals.flatMap {
            listOfNotNull(it.targetId, it.secondaryTargetId)
        }.distinct(),
    )

    private fun physicalResult(
        context: ContextPacket,
        resultId: String,
        proposals: List<PhysicalActionProposal>,
        includeCharacterAction: Boolean = true,
    ): AiTurnResult = AiTurnResult(
        protocolVersion = "2",
        projectId = context.project.id,
        turnId = context.turnId,
        resultId = resultId,
        contextId = context.contextId,
        sessionId = context.sessionId,
        turnNumber = context.turnNumber,
        baseStateVersion = context.baseStateVersion,
        contextHash = context.contextHash,
        narrative = "The player performs only the declared bounded physical actions.",
        turnSummary = "Declared physical actions were validated and reduced deterministically.",
        characterActions = if (includeCharacterAction) listOf(
            CharacterAction(
                characterId = "char-player",
                intent = "Handle the nearby object without inventing an impossible action.",
                visibleAction = "Checks the distance and performs the declared physical action.",
                hiddenMotive = "Avoid causing an uncontrolled change.",
                characterFitScore = 90,
                storyProgressScore = 72,
            ),
        ) else {
            emptyList()
        },
        physicalActionProposals = proposals,
    )

    private fun evidenceContext(): ContextPacket = ContextPacket(
        project = Project("wp04", "WORLD-PHYSICS-04", "Non-Canon physical state persistence evidence"),
        turnId = "wp04-turn-1",
        turnNumber = 1,
        playerInput = "Lift and then drop the nearby light crate.",
        activeScene = null,
        sceneContract = null,
        contextId = "wp04-context-1",
        sessionId = "wp04-session-1",
        baseStateVersion = 0,
        contextHash = "wp04-context-hash-v2",
    )

    private fun newRepository(): SqliteStoryRepository =
        SqliteStoryRepository(Files.createTempDirectory("formal-physical-state").resolve("story.db"))

    private fun seededRepository(): SqliteStoryRepository = newRepository().also { repository ->
        repository.initialize()
        repository.seedDemo(
            JsonSupport.json.decodeFromString<DemoProjectSeed>(Files.readString(samplePath("demo_project_seed.json"))),
        )
    }

    private fun samplePath(fileName: String): Path = listOf(
        Path.of("samples", fileName),
        Path.of("..", "samples", fileName),
    ).firstOrNull(Files::isRegularFile) ?: error("Cannot find sample file: $fileName")

    private fun PhysicalWorldSnapshot.entity(entityId: String) =
        entities.single { it.entityId == entityId }

    private fun writeEvidenceWhenRequested() {
        val outputRoot = System.getenv("WORLD_PHYSICS04_EVIDENCE_ROOT")
            ?.takeIf(String::isNotBlank)
            ?.let(Path::of)
            ?: return
        val contextPacket = evidenceContext()
        val result = physicalResult(
            contextPacket,
            "wp04-evidence-result-1",
            listOf(
                proposal("lift-01-light-crate", "wp04-evidence-lift"),
                proposal("drop-01-held-above-ground", "wp04-evidence-drop"),
            ),
            includeCharacterAction = false,
        )
        val physicalContext = v2Context(contextPacket, result, snapshot("snapshot-base"), 0)
        val transitionPlan = PhysicalStateReducer().plan(physicalContext, result)
        val report = buildJsonObject {
            put("schemaVersion", JsonPrimitive("2.0"))
            put("boundary", JsonPrimitive(PhysicalStateReducer.RUNTIME_BOUNDARY))
            put("scenarioCount", JsonPrimitive(7))
            put("persistedTransitionCount", JsonPrimitive(2))
            put("coveredStatefulActionTypes", buildJsonArray {
                PhysicalStateReducer.STATEFUL_ACTIONS.sortedBy { it.name }.forEach { add(JsonPrimitive(it.name)) }
            })
            put("migrationStatus", JsonPrimitive("ADDITIVE_IDEMPOTENT"))
            put("rollbackStatus", JsonPrimitive("SINGLE_TRANSACTION_VERIFIED"))
            put("technicalDecision", JsonPrimitive("WORLD_PHYSICS04_PHYSICAL_STATE_PERSISTENCE_PASS"))
            put("canonPromotionAllowed", JsonPrimitive(false))
            put("claimBoundary", buildJsonArray {
                add(JsonPrimitive("Only declared bounded actions are reduced; free text is not converted into actions."))
                add(JsonPrimitive("The V1 ground plane and support-top approximation are not a complete physics simulation."))
                add(JsonPrimitive("Persisted runtime state is non-Canon and cannot promote Chenxing facts."))
            })
        }

        listOf(CONTEXT_PATH, RESULT_PATH, PLAN_PATH, REPORT_PATH).forEach { relative ->
            outputRoot.resolve(relative).parent?.let(Files::createDirectories)
        }
        Files.writeString(outputRoot.resolve(CONTEXT_PATH), FormalTurnPhysicalJson.strict.encodeToString(physicalContext) + "\n", StandardCharsets.UTF_8)
        Files.writeString(outputRoot.resolve(RESULT_PATH), FormalTurnPhysicalJson.strict.encodeToString(result) + "\n", StandardCharsets.UTF_8)
        Files.writeString(outputRoot.resolve(PLAN_PATH), FormalTurnPhysicalJson.strict.encodeToString(transitionPlan) + "\n", StandardCharsets.UTF_8)
        Files.writeString(outputRoot.resolve(REPORT_PATH), FormalTurnPhysicalJson.strict.encodeToString(JsonObject.serializer(), report) + "\n", StandardCharsets.UTF_8)

        val evidencePaths = listOf(CONTEXT_PATH, RESULT_PATH, PLAN_PATH, REPORT_PATH, SCHEMA_PATH, MIGRATION_PATH)
        val manifest = PhysicalEvidenceManifest(
            scope = "WORLD-PHYSICS-04 PHYSICAL STATE PERSISTENCE",
            files = evidencePaths.map { relative ->
                val path = outputRoot.resolve(relative)
                PhysicalEvidenceFile(relative, sha256(path), Files.size(path))
            },
        )
        outputRoot.resolve(MANIFEST_PATH).parent?.let(Files::createDirectories)
        Files.writeString(outputRoot.resolve(MANIFEST_PATH), FormalTurnPhysicalJson.strict.encodeToString(manifest) + "\n", StandardCharsets.UTF_8)
    }

    private fun repositoryRoot(): Path = generateSequence(Path.of("").toAbsolutePath().normalize()) { it.parent }
        .first { Files.isDirectory(it.resolve("app")) && Files.isDirectory(it.resolve("schemas")) }

    private fun sha256(path: Path): String = MessageDigest.getInstance("SHA-256")
        .digest(Files.readAllBytes(path))
        .joinToString("") { "%02x".format(it) }

    private companion object {
        const val CONTEXT_PATH = "samples/world_physics04/formal_physical_state_context_v2.json"
        const val RESULT_PATH = "samples/world_physics04/formal_physical_state_result_v2.json"
        const val PLAN_PATH = "samples/world_physics04/formal_physical_state_transition_plan_v2.json"
        const val REPORT_PATH = "samples/world_physics04/formal_physical_state_technical_report_v2.json"
        const val MANIFEST_PATH = "samples/world_physics04/formal_physical_state_evidence_hashes.json"
        const val SCHEMA_PATH = "schemas/world_physics/formal_physical_state_context_v2.schema.json"
        const val MIGRATION_PATH = "app/src/main/resources/migrations/016_physical_state_runtime.sql"
    }
}
