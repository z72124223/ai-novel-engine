package com.localnovel.engine

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.DemoProjectSeed
import com.localnovel.engine.model.FormalTurnPhysicalValidationContext
import com.localnovel.engine.model.FormalTurnPhysicalGateSuite
import com.localnovel.engine.model.FormalTurnPhysicalGateTechnicalReport
import com.localnovel.engine.model.PhysicalEvidenceFile
import com.localnovel.engine.model.PhysicalEvidenceManifest
import com.localnovel.engine.model.PhysicalActionProposal
import com.localnovel.engine.model.PhysicalActionType
import com.localnovel.engine.model.RevealedFact
import com.localnovel.engine.model.SceneProgressUpdate
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.AiTurnResultValidator
import com.localnovel.engine.service.FormalTurnPhysicalGateValidator
import com.localnovel.engine.service.FormalTurnPhysicalGateEvaluationService
import com.localnovel.engine.service.FormalTurnPhysicalGateFixtureFactory
import com.localnovel.engine.service.FormalTurnPhysicalJson
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.service.PhysicalAffordanceFixtureFactory
import com.localnovel.engine.service.ResolveTurnOptions
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class FormalTurnPhysicalGateTest {
    private val physicalSuite = PhysicalAffordanceFixtureFactory.createSuite()
    private val gate = FormalTurnPhysicalGateValidator()

    @Test
    fun `all eight formal gate evidence cases match expectations`() {
        val report = FormalTurnPhysicalGateEvaluationService().evaluate(
            FormalTurnPhysicalGateFixtureFactory.createSuite(),
        )

        assertEquals(8, report.caseCount)
        assertEquals(8, report.expectationMatchCount)
        assertEquals(0, report.expectationMismatchCount)
        assertEquals("FORMAL_TURN_PHYSICAL_GATE_PASS_STATEFUL_ACTIONS_BLOCKED", report.technicalDecision)
        assertEquals("VALIDATES_BEFORE_RESOLVE_PENDING_TURN", report.transactionBoundaryStatus)
        assertEquals("NOT_IMPLEMENTED_STATEFUL_ACTIONS_FAIL_CLOSED", report.physicalStatePersistenceStatus)
    }

    @Test
    fun `stable chair support passes formal pre-transaction gate`() {
        val fixture = turnFixture("chair-pass", "support-01-chair-assist")

        val report = AiTurnResultValidator(fixture.repository).validate(
            fixture.context,
            fixture.result,
            fixture.physicalContext,
        )
        val runtimeReport = MultiTurnRuntime(fixture.repository).validate(
            fixture.context,
            fixture.result,
            fixture.physicalContext,
        )

        assertTrue(report.isValid, report.errors.joinToString { "${it.code}:${it.message}" })
        assertTrue(runtimeReport.isValid, runtimeReport.errors.joinToString { "${it.code}:${it.message}" })
    }

    @Test
    fun `adult dog shoulder support is rejected by formal turn validator`() {
        val fixture = turnFixture("dog-reject", "support-02-dog-shoulder-adult")

        val report = AiTurnResultValidator(fixture.repository).validate(
            fixture.context,
            fixture.result,
            fixture.physicalContext,
        )

        assertFalse(report.isValid)
        val physicalError = report.errors.single { it.code == FormalTurnPhysicalGateValidator.PHYSICAL_AFFORDANCE_REJECTED }
        assertTrue(physicalError.message.contains("BODY_PLAN_NOT_SUPPORTED"))
        assertTrue(physicalError.message.contains("SUPPORT_CAPACITY_EXCEEDED"))
        assertTrue(physicalError.message.contains("TARGET_NOT_STABLE_SUPPORT"))
    }

    @Test
    fun `proposal and engine context must be supplied together`() {
        val fixture = turnFixture("pairing", "support-01-chair-assist")
        val missingContext = AiTurnResultValidator(fixture.repository).validate(
            fixture.context,
            fixture.result,
        )
        val missingProposal = AiTurnResultValidator(fixture.repository).validate(
            fixture.context,
            fixture.result.copy(physicalActionProposals = emptyList()),
            fixture.physicalContext,
        )

        assertTrue(missingContext.errors.any { it.code == FormalTurnPhysicalGateValidator.PHYSICAL_CONTEXT_REQUIRED })
        assertTrue(missingProposal.errors.any { it.code == FormalTurnPhysicalGateValidator.PHYSICAL_PROPOSAL_REQUIRED })
    }

    @Test
    fun `context identity and canonical result hash are fail closed`() {
        val fixture = turnFixture("hash-binding", "support-01-chair-assist")
        val contextMismatch = fixture.physicalContext.copy(contextHash = "wrong-context-hash")
        val resultHashMismatch = fixture.physicalContext.copy(resultCanonicalSha256 = "0".repeat(64))

        val contextErrors = gate.validate(fixture.context, fixture.result, contextMismatch)
        val resultErrors = gate.validate(fixture.context, fixture.result, resultHashMismatch)

        assertTrue(contextErrors.any { it.code == FormalTurnPhysicalGateValidator.PHYSICAL_CONTEXT_BINDING_MISMATCH })
        assertTrue(resultErrors.any { it.code == FormalTurnPhysicalGateValidator.PHYSICAL_RESULT_HASH_MISMATCH })
    }

    @Test
    fun `non allowlisted actor action and target are rejected`() {
        val fixture = turnFixture("allowlist", "support-01-chair-assist")
        val proposal = fixture.result.physicalActionProposals.single()
        val tampered = fixture.result.copy(
            physicalActionProposals = listOf(
                proposal.copy(
                    actorId = "dog",
                    targetId = "adult",
                    actionType = PhysicalActionType.REACH,
                ),
            ),
        )
        val rebound = fixture.physicalContext.copy(
            resultCanonicalSha256 = gate.canonicalResultSha256(tampered),
        )

        val errors = gate.validate(fixture.context, tampered, rebound)

        assertTrue(errors.any { it.code == FormalTurnPhysicalGateValidator.PHYSICAL_ACTION_NOT_ALLOWLISTED })
        assertTrue(errors.any { it.code == FormalTurnPhysicalGateValidator.PHYSICAL_ACTOR_NOT_ALLOWLISTED })
        assertTrue(errors.any { it.code == FormalTurnPhysicalGateValidator.PHYSICAL_TARGET_NOT_ALLOWLISTED })
    }

    @Test
    fun `stateful physical action remains blocked without relation persistence`() {
        val fixture = turnFixture("drop-blocked", "drop-01-held-above-ground")

        val errors = gate.validate(fixture.context, fixture.result, fixture.physicalContext)

        assertTrue(errors.any { it.code == FormalTurnPhysicalGateValidator.PHYSICAL_STATE_PERSISTENCE_UNAVAILABLE })
    }

    @Test
    fun `physical rejection happens before pending turn transaction`() {
        val fixture = turnFixture("transaction-reject", "support-02-dog-shoulder-adult")
        val beforeSession = fixture.repository.getSession(fixture.context.sessionId)

        val failure = assertFails {
            MultiTurnRuntime(fixture.repository).resolveTurn(
                fixture.context.sessionId,
                fixture.result,
                ResolveTurnOptions(formalTurnPhysicalContext = fixture.physicalContext),
            )
        }

        assertTrue(failure.message.orEmpty().contains(FormalTurnPhysicalGateValidator.PHYSICAL_AFFORDANCE_REJECTED))
        val afterSession = fixture.repository.getSession(fixture.context.sessionId)
        assertEquals(beforeSession?.stateVersion, afterSession?.stateVersion)
        assertEquals(beforeSession?.nextTurnNumber, afterSession?.nextTurnNumber)
        assertEquals(0, fixture.repository.appliedTurnCount(fixture.context.sessionId))
        assertNotNull(fixture.repository.getPendingContext(fixture.context.sessionId, fixture.context.contextId))
    }

    @Test
    fun `strict physical context rejects unknown keys and legacy result remains compatible`() {
        val fixture = turnFixture("strict-json", "support-01-chair-assist")
        val json = FormalTurnPhysicalJson.strict.encodeToString(
            FormalTurnPhysicalValidationContext.serializer(),
            fixture.physicalContext,
        )
        assertFails {
            FormalTurnPhysicalJson.strict.decodeFromString<FormalTurnPhysicalValidationContext>(
                json.dropLast(2) + ",\n  \"unexpected\": true\n}",
            )
        }

        val legacy = fixture.result.copy(physicalActionProposals = emptyList())
        val legacyJson = JsonSupport.json.encodeToString(AiTurnResult.serializer(), legacy)
        val decoded = JsonSupport.json.decodeFromString<AiTurnResult>(legacyJson)
        assertTrue(decoded.physicalActionProposals.isEmpty())
        assertTrue(AiTurnResultValidator(fixture.repository).validate(fixture.context, decoded).isValid)
    }

    @Test
    fun `formal physical schema is strict and committed evidence is reproducible`() {
        writeEvidenceWhenRequested()
        val root = repositoryRoot()
        val schemaRoot = Json.parseToJsonElement(Files.readString(root.resolve(SCHEMA_PATH))).jsonObject
        assertEquals("false", schemaRoot["additionalProperties"].toString())
        assertTrue(schemaRoot["required"]!!.toString().contains("resultCanonicalSha256"))

        val suitePath = root.resolve(SUITE_PATH)
        val reportPath = root.resolve(REPORT_PATH)
        val examplePath = root.resolve(EXAMPLE_PATH)
        val manifestPath = root.resolve(MANIFEST_PATH)
        if (!Files.isRegularFile(suitePath) || !Files.isRegularFile(reportPath) ||
            !Files.isRegularFile(examplePath) || !Files.isRegularFile(manifestPath)
        ) return

        val suite = FormalTurnPhysicalJson.strict.decodeFromString<FormalTurnPhysicalGateSuite>(Files.readString(suitePath))
        val report = FormalTurnPhysicalJson.strict.decodeFromString<FormalTurnPhysicalGateTechnicalReport>(Files.readString(reportPath))
        assertEquals(FormalTurnPhysicalGateFixtureFactory.createSuite(), suite)
        assertEquals(FormalTurnPhysicalGateEvaluationService().evaluate(suite), report)
        assertEquals(
            suite.cases.first().physicalContext,
            FormalTurnPhysicalJson.strict.decodeFromString<FormalTurnPhysicalValidationContext>(Files.readString(examplePath)),
        )

        val manifest = FormalTurnPhysicalJson.strict.decodeFromString<PhysicalEvidenceManifest>(Files.readString(manifestPath))
        assertEquals("WORLD-PHYSICS-03 FORMAL TURN PHYSICAL GATE", manifest.scope)
        manifest.files.forEach { file ->
            val path = root.resolve(file.path)
            assertEquals(file.bytes, Files.size(path), file.path)
            assertEquals(file.sha256, sha256(path), file.path)
        }
    }

    private fun turnFixture(label: String, sourceCaseId: String): TurnFixture {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我進行一個受物理規則約束的動作。")
        val sourceCase = physicalSuite.cases.single { it.caseId == sourceCaseId }
        val sourceSnapshot = physicalSuite.snapshots.single { it.snapshotId == sourceCase.snapshotId }
        val proposal = sourceCase.proposal.copy(actionId = "wp03-$label")
        val result = validResult(context, "result-wp03-$label").copy(
            physicalActionProposals = listOf(proposal),
        )
        val physicalContext = FormalTurnPhysicalValidationContext(
            projectId = context.project.id,
            sessionId = context.sessionId,
            turnId = context.turnId,
            contextId = context.contextId,
            contextHash = context.contextHash,
            turnNumber = context.turnNumber,
            baseStateVersion = context.baseStateVersion,
            resultId = result.resultId,
            resultCanonicalSha256 = gate.canonicalResultSha256(result),
            physicalArchetypes = physicalSuite.archetypes,
            physicalSnapshot = sourceSnapshot.copy(
                snapshotId = "wp03-$label-snapshot",
                boundary = FormalTurnPhysicalGateValidator.FORMAL_TURN_BOUNDARY,
            ),
            allowedPhysicalActionTypes = listOf(proposal.actionType),
            allowedPhysicalActorIds = listOf(proposal.actorId),
            allowedPhysicalTargetIds = listOfNotNull(proposal.targetId, proposal.secondaryTargetId).distinct(),
        )
        return TurnFixture(repository, context, result, physicalContext)
    }

    private fun seededRepository(): SqliteStoryRepository {
        val repository = SqliteStoryRepository(Files.createTempDirectory("formal-physical-gate").resolve("story.db"))
        repository.initialize()
        repository.seedDemo(
            JsonSupport.json.decodeFromString<DemoProjectSeed>(samplePath("demo_project_seed.json").toFile().readText()),
        )
        return repository
    }

    private fun validResult(context: ContextPacket, resultId: String): AiTurnResult = AiTurnResult(
        protocolVersion = "2",
        projectId = context.project.id,
        turnId = context.turnId,
        resultId = resultId,
        contextId = context.contextId,
        sessionId = context.sessionId,
        turnNumber = context.turnNumber,
        baseStateVersion = context.baseStateVersion,
        contextHash = context.contextHash,
        narrative = "沈霜先確認接觸點，再完成受引擎規則約束的動作。",
        turnSummary = "沈霜完成一個受物理規則驗證的動作。",
        characterActions = listOf(
            CharacterAction(
                characterId = "char-shen-shuang",
                intent = "只做已通過規則驗證的動作",
                visibleAction = "沈霜確認接觸點後才行動。",
                hiddenMotive = "避免暴露祠堂地底的秘密。",
                triggeringConditions = listOf("玩家提出動作"),
                usedMemoryIds = listOf("memory-shen-shuang-vow"),
                usedItemIds = listOf("item-copper-token"),
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
                targetId = "fact-physical-${context.turnNumber}",
                payload = JsonObject(
                    mapOf(
                        "content" to JsonPrimitive("沈霜先確認接觸點再行動。"),
                        "source" to JsonPrimitive("ai_turn_result"),
                    ),
                ),
            ),
        ),
        revealedFacts = listOf(RevealedFact("fact-physical-${context.turnNumber}", "沈霜先確認接觸點再行動。")),
        sceneProgressUpdates = listOf(SceneProgressUpdate("沈霜可以承認昨夜後殿門閂被人動過。", "completed", "沈霜承認門閂異常。")),
    )

    private fun samplePath(fileName: String): Path = listOf(
        Path.of("samples", fileName),
        Path.of("..", "samples", fileName),
    ).firstOrNull(Files::exists) ?: error("Cannot find sample file: $fileName")

    private fun writeEvidenceWhenRequested() {
        val outputRoot = System.getenv("WORLD_PHYSICS03_EVIDENCE_ROOT")
            ?.takeIf(String::isNotBlank)
            ?.let(Path::of)
            ?: return
        val suite = FormalTurnPhysicalGateFixtureFactory.createSuite()
        val report = FormalTurnPhysicalGateEvaluationService().evaluate(suite)
        val suitePath = outputRoot.resolve(SUITE_PATH)
        val reportPath = outputRoot.resolve(REPORT_PATH)
        val examplePath = outputRoot.resolve(EXAMPLE_PATH)
        val manifestPath = outputRoot.resolve(MANIFEST_PATH)
        Files.createDirectories(suitePath.parent)
        Files.writeString(suitePath, FormalTurnPhysicalJson.strict.encodeToString(FormalTurnPhysicalGateSuite.serializer(), suite) + "\n", StandardCharsets.UTF_8)
        Files.writeString(reportPath, FormalTurnPhysicalJson.strict.encodeToString(FormalTurnPhysicalGateTechnicalReport.serializer(), report) + "\n", StandardCharsets.UTF_8)
        Files.writeString(
            examplePath,
            FormalTurnPhysicalJson.strict.encodeToString(
                FormalTurnPhysicalValidationContext.serializer(),
                requireNotNull(suite.cases.first().physicalContext),
            ) + "\n",
            StandardCharsets.UTF_8,
        )
        val manifest = PhysicalEvidenceManifest(
            scope = "WORLD-PHYSICS-03 FORMAL TURN PHYSICAL GATE",
            files = listOf(SUITE_PATH, REPORT_PATH, EXAMPLE_PATH, SCHEMA_PATH).map { relative ->
                val path = outputRoot.resolve(relative)
                PhysicalEvidenceFile(relative, sha256(path), Files.size(path))
            },
        )
        Files.writeString(manifestPath, FormalTurnPhysicalJson.strict.encodeToString(PhysicalEvidenceManifest.serializer(), manifest) + "\n", StandardCharsets.UTF_8)
    }

    private fun repositoryRoot(): Path = Path.of("").toAbsolutePath().normalize().let { current ->
        if (Files.isDirectory(current.resolve("app"))) current else current.parent
    }

    private fun sha256(path: Path): String = MessageDigest.getInstance("SHA-256")
        .digest(Files.readAllBytes(path))
        .joinToString("") { "%02x".format(it) }

    private data class TurnFixture(
        val repository: SqliteStoryRepository,
        val context: ContextPacket,
        val result: AiTurnResult,
        val physicalContext: FormalTurnPhysicalValidationContext,
    )

    private companion object {
        const val SUITE_PATH = "samples/world_physics03/formal_turn_physical_gate_suite_v1.json"
        const val REPORT_PATH = "samples/world_physics03/formal_turn_physical_gate_report_v1.json"
        const val EXAMPLE_PATH = "samples/world_physics03/formal_turn_physical_context_example_v1.json"
        const val MANIFEST_PATH = "samples/world_physics03/formal_turn_physical_gate_evidence_hashes.json"
        const val SCHEMA_PATH = "schemas/world_physics/formal_turn_physical_context_v1.schema.json"
    }
}
