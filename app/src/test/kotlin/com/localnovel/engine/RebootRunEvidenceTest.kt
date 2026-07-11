package com.localnovel.engine

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.DemoProjectSeed
import com.localnovel.engine.model.IrreversibleApprovalArtifact
import com.localnovel.engine.model.IrreversibleChangeContract
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.service.ResolveTurnOptions
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RebootRunEvidenceTest {
    @Test
    fun migrationIsAdditiveAndIdempotent() {
        val repository = seededRepository()

        repository.initialize()

        assertTrue("reboot_run_evidence" in repository.listTables())
        assertEquals(0, repository.tableCount("reboot_run_evidence"))
    }

    @Test
    fun qualityWarningIsPersistedWithoutBlockingDefaultResolve() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "I observe the room before acting.")

        runtime.resolveTurn(session.id, validResult(context, "reboot-quality-warning"))

        val evidence = repository.tableRows("reboot_run_evidence").single()
        assertEquals("QUALITY_WARNING", evidence["validation_classification"])
        assertEquals("APPLIED", evidence["status"])
        assertEquals(1, repository.appliedTurnCount(session.id))
        assertTrue(evidence["raw_draft"].orEmpty().contains("player studies"))
        assertTrue(evidence["state_delta_json"].orEmpty().contains("add_event"))
        assertTrue(evidence["apply_result_json"].orEmpty().contains("APPLIED"))
        assertEquals(context.contextHash, evidence["context_hash"])
        assertEquals(context.playerInput, evidence["player_input"])
        assertTrue(evidence["context_snapshot_json"].orEmpty().contains(context.contextId))
    }

    @Test
    fun strictQualityModeCanRejectWithoutWritingStoryState() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "I observe the room before acting.")

        assertFailsWith<IllegalStateException> {
            runtime.resolveTurn(
                session.id,
                validResult(context, "reboot-strict-quality"),
                ResolveTurnOptions(strictNarrativeQuality = true),
            )
        }

        val evidence = repository.tableRows("reboot_run_evidence").single()
        assertEquals("QUALITY_WARNING", evidence["validation_classification"])
        assertEquals("REJECTED", evidence["status"])
        assertEquals(0, repository.appliedTurnCount(session.id))
        assertEquals(0, repository.tableCount("state_deltas"))
    }

    @Test
    fun protocolFailureIsPersistedWithoutWritingStoryState() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "I observe the room before acting.")
        val invalid = validResult(context, "reboot-bad-hash").copy(contextHash = "wrong-hash")

        assertFailsWith<IllegalStateException> {
            runtime.resolveTurn(session.id, invalid)
        }

        val evidence = repository.tableRows("reboot_run_evidence").single()
        assertEquals("PROTOCOL_HARD_FAIL", evidence["validation_classification"])
        assertTrue(evidence["validation_findings_json"].orEmpty().contains("hash_mismatch"))
        assertEquals(0, repository.appliedTurnCount(session.id))
        assertEquals(0, repository.tableCount("state_deltas"))
    }

    @Test
    fun projectAndTurnIdentityFailuresAreProtocolHardFails() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "I observe the room before acting.")
        val invalid = validResult(context, "reboot-bad-identity").copy(
            projectId = "other-project",
            turnId = "other-turn",
        )

        assertFailsWith<IllegalStateException> {
            runtime.resolveTurn(session.id, invalid)
        }

        val evidence = repository.tableRows("reboot_run_evidence").single()
        assertEquals("PROTOCOL_HARD_FAIL", evidence["validation_classification"])
        assertTrue(evidence["validation_findings_json"].orEmpty().contains("project_mismatch"))
        assertTrue(evidence["validation_findings_json"].orEmpty().contains("turn_id_mismatch"))
        assertEquals(0, repository.appliedTurnCount(session.id))
    }

    @Test
    fun missingPendingContextStillPersistsProtocolEnvelope() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "I observe the room before acting.")
        val invalid = validResult(context, "reboot-missing-context").copy(contextId = "missing-context")

        assertFailsWith<IllegalStateException> {
            runtime.resolveTurn(session.id, invalid)
        }

        val evidence = repository.tableRows("reboot_run_evidence").single()
        assertEquals("PROTOCOL_HARD_FAIL", evidence["validation_classification"])
        assertTrue(evidence["validation_findings_json"].orEmpty().contains("pending_context_not_found"))
        assertEquals("{}", evidence["context_snapshot_json"])
        assertEquals(0, repository.appliedTurnCount(session.id))
    }

    @Test
    fun malformedResultJsonStillPersistsProtocolEnvelope() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        runtime.prepareTurn(session.id, "I observe the room before acting.")
        val malformed = Files.createTempFile("reboot-malformed-result", ".json")
        Files.writeString(malformed, "{\"protocolVersion\":\"2\"")

        val exitCode = Cli().run(
            listOf(
                "resolve-turn",
                "--db",
                repository.databasePath().toString(),
                "--session",
                session.id,
                "--result",
                malformed.toString(),
            ),
        )

        assertEquals(1, exitCode)
        val evidence = repository.tableRows("reboot_run_evidence").single()
        assertEquals("PROTOCOL_HARD_FAIL", evidence["validation_classification"])
        assertTrue(evidence["validation_findings_json"].orEmpty().contains("malformed_result_json"))
        assertTrue(evidence["raw_result_json"].orEmpty().startsWith("{"))
        assertEquals(0, repository.appliedTurnCount(session.id))
    }

    @Test
    fun worldLegalityFailureIsPersistedWithoutWritingStoryState() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "I observe the room before acting.")
        val invalid = validResult(context, "reboot-unknown-actor").copy(
            characterActions = listOf(action("char-not-in-context")),
        )

        assertFailsWith<IllegalStateException> {
            runtime.resolveTurn(session.id, invalid)
        }

        val evidence = repository.tableRows("reboot_run_evidence").single()
        assertEquals("WORLD_LEGALITY_HARD_FAIL", evidence["validation_classification"])
        assertTrue(evidence["validation_findings_json"].orEmpty().contains("unknown_actor"))
        assertEquals(0, repository.appliedTurnCount(session.id))
    }

    @Test
    fun irreversibleChangeRequiresSeparateApprovalArtifactBeforeApply() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "I accept a lasting responsibility.")
        val result = validResult(context, "reboot-irreversible").copy(
            stateDeltas = listOf(eventDelta("event-reboot-irreversible")),
        )
        val contract = IrreversibleChangeContract(
            changeId = "lasting-duty",
            changeTypes = listOf("relationship_obligation"),
            summary = "The player accepts a lasting duty.",
            contextHash = context.contextHash,
            resultId = result.resultId,
            affectedEntityIds = listOf("char-player"),
            requiredStateDeltaTargetIds = listOf("event-reboot-irreversible"),
        )

        assertFailsWith<IllegalStateException> {
            runtime.resolveTurn(
                session.id,
                result,
                ResolveTurnOptions(irreversibleChangeContract = contract),
            )
        }
        assertEquals(0, repository.appliedTurnCount(session.id))
        assertEquals(
            "IRREVERSIBLE_APPROVAL_REQUIRED",
            repository.tableRows("reboot_run_evidence").single()["validation_classification"],
        )

        runtime.resolveTurn(
            session.id,
            result,
            ResolveTurnOptions(
                irreversibleChangeContract = contract,
                irreversibleApprovalArtifact = approval(context, result, contract.changeId),
            ),
        )

        assertEquals(1, repository.appliedTurnCount(session.id))
        val rows = repository.tableRows("reboot_run_evidence")
        assertEquals(2, rows.size)
        val applied = rows.single { it["status"] == "APPLIED" }
        assertEquals("human:test-reviewer", applied["irreversible_approval_actor"])
        assertEquals("1", applied["irreversible_approval_granted"])
        assertTrue(applied["irreversible_change_json"].orEmpty().contains("lasting-duty"))
        assertTrue(applied["irreversible_approval_json"].orEmpty().contains("approval-lasting-duty"))
    }

    @Test
    fun irreversibleContractMustBindContextResultAndHumanActor() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "I accept a lasting responsibility.")
        val result = validResult(context, "reboot-irreversible-binding").copy(
            stateDeltas = listOf(eventDelta("event-reboot-irreversible-binding")),
        )
        val invalidContract = IrreversibleChangeContract(
            changeId = "lasting-duty",
            changeTypes = listOf("relationship_obligation"),
            summary = "The player accepts a lasting duty.",
            contextHash = "wrong-context-hash",
            resultId = "wrong-result-id",
            affectedEntityIds = listOf("char-player"),
            requiredStateDeltaTargetIds = listOf("event-reboot-irreversible-binding"),
        )

        assertFailsWith<IllegalStateException> {
            runtime.resolveTurn(
                session.id,
                result,
                ResolveTurnOptions(
                    irreversibleChangeContract = invalidContract,
                    irreversibleApprovalArtifact = IrreversibleApprovalArtifact(
                        approvalId = "model-approval",
                        changeId = invalidContract.changeId,
                        contextHash = "wrong-context-hash",
                        resultId = "wrong-result-id",
                        humanActorId = "model-claimed-reviewer",
                        decision = "APPROVE",
                        approvedAt = "2026-07-11T00:00:00Z",
                    ),
                ),
            )
        }

        val evidence = repository.tableRows("reboot_run_evidence").single()
        assertEquals("PROTOCOL_HARD_FAIL", evidence["validation_classification"])
        assertTrue(evidence["validation_findings_json"].orEmpty().contains("irreversible_contract_identity_mismatch"))
        assertTrue(evidence["validation_findings_json"].orEmpty().contains("invalid_irreversible_approval"))
        assertEquals("0", evidence["irreversible_approval_granted"])
        assertEquals(0, repository.appliedTurnCount(session.id))
    }

    @Test
    fun approvalArtifactWithoutEngineContractIsRejected() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "I accept a lasting responsibility.")

        assertFailsWith<IllegalStateException> {
            runtime.resolveTurn(
                session.id,
                validResult(context, "reboot-approval-without-contract"),
                ResolveTurnOptions(
                    irreversibleApprovalArtifact = IrreversibleApprovalArtifact(
                        approvalId = "orphan-approval",
                        changeId = "missing-contract",
                        contextHash = context.contextHash,
                        resultId = "reboot-approval-without-contract",
                        humanActorId = "human:test-reviewer",
                        decision = "APPROVE",
                        approvedAt = "2026-07-11T00:00:00Z",
                    ),
                ),
            )
        }

        val evidence = repository.tableRows("reboot_run_evidence").single()
        assertEquals("IRREVERSIBLE_APPROVAL_REQUIRED", evidence["validation_classification"])
        assertTrue(evidence["validation_findings_json"].orEmpty().contains("irreversible_contract_required"))
        assertEquals("0", evidence["irreversible_approval_granted"])
        assertEquals(0, repository.appliedTurnCount(session.id))
    }

    @Test
    fun engineDetectedHealthMutationCannotOmitIrreversibleContract() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "I accept a permanent injury.")
        val result = validResult(context, "reboot-detected-permanent-injury").copy(
            stateDeltas = listOf(
                DeltaOperation(
                    type = DeltaOperationType.UPDATE_CHARACTER_STATE,
                    targetType = "character_state",
                    targetId = "char-player",
                    payload = JsonObject(mapOf("health" to JsonPrimitive("permanently_blinded"))),
                ),
            ),
        )

        assertFailsWith<IllegalStateException> { runtime.resolveTurn(session.id, result) }

        val evidence = repository.tableRows("reboot_run_evidence").single()
        assertEquals("IRREVERSIBLE_APPROVAL_REQUIRED", evidence["validation_classification"])
        assertTrue(evidence["validation_findings_json"].orEmpty().contains("health mutation:char-player"))
        assertEquals(0, repository.appliedTurnCount(session.id))
    }

    @Test
    fun contractMustCoverEveryEngineDetectedIrreversibleType() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "The injury also binds me to a faction.")
        val result = validResult(context, "reboot-multi-type").copy(
            stateDeltas = listOf(
                DeltaOperation(
                    type = DeltaOperationType.UPDATE_CHARACTER_STATE,
                    targetType = "character_state",
                    targetId = "char-player",
                    payload = JsonObject(
                        mapOf(
                            "health" to JsonPrimitive("permanently_blinded"),
                            "flags" to JsonObject(mapOf("faction_commitment" to JsonPrimitive("joined"))),
                        ),
                    ),
                ),
            ),
        )
        val incompleteContract = IrreversibleChangeContract(
            changeId = "injury-and-faction",
            changeTypes = listOf("permanent_injury"),
            summary = "Permanent injury and faction commitment.",
            contextHash = context.contextHash,
            resultId = result.resultId,
            affectedEntityIds = listOf("char-player"),
            requiredStateDeltaTargetIds = listOf("char-player"),
        )

        assertFailsWith<IllegalStateException> {
            runtime.resolveTurn(
                session.id,
                result,
                ResolveTurnOptions(
                    irreversibleChangeContract = incompleteContract,
                    irreversibleApprovalArtifact = approval(context, result, incompleteContract.changeId),
                ),
            )
        }

        val evidence = repository.tableRows("reboot_run_evidence").single()
        assertTrue(evidence["validation_findings_json"].orEmpty().contains("irreversible_contract_type_mismatch"))
        assertEquals("0", evidence["irreversible_approval_granted"])
        assertEquals(0, repository.appliedTurnCount(session.id))
    }

    @Test
    fun approvalTimestampMustBeStrictIsoInstant() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "I accept a lasting responsibility.")
        val result = validResult(context, "reboot-invalid-approval-time").copy(
            stateDeltas = listOf(eventDelta("event-reboot-invalid-approval-time")),
        )
        val contract = IrreversibleChangeContract(
            changeId = "invalid-time-duty",
            changeTypes = listOf("relationship_obligation"),
            summary = "A lasting duty.",
            contextHash = context.contextHash,
            resultId = result.resultId,
            affectedEntityIds = listOf("char-player"),
            requiredStateDeltaTargetIds = listOf("event-reboot-invalid-approval-time"),
        )
        val invalidApproval = approval(context, result, contract.changeId).copy(approvedAt = "garbage")

        assertFailsWith<IllegalStateException> {
            runtime.resolveTurn(
                session.id,
                result,
                ResolveTurnOptions(
                    irreversibleChangeContract = contract,
                    irreversibleApprovalArtifact = invalidApproval,
                ),
            )
        }

        val evidence = repository.tableRows("reboot_run_evidence").single()
        assertTrue(evidence["validation_findings_json"].orEmpty().contains("invalid_irreversible_approval"))
        assertEquals("0", evidence["irreversible_approval_granted"])
        assertEquals(0, repository.appliedTurnCount(session.id))
    }

    @Test
    fun cliRequiresSeparateBoundContractForIrreversibleApproval() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "I accept a lasting responsibility.")
        val result = validResult(context, "reboot-cli-irreversible").copy(
            stateDeltas = listOf(eventDelta("event-reboot-cli-irreversible")),
        )
        val contract = IrreversibleChangeContract(
            changeId = "lasting-cli-duty",
            changeTypes = listOf("relationship_obligation"),
            summary = "The player accepts a lasting CLI duty.",
            contextHash = context.contextHash,
            resultId = result.resultId,
            affectedEntityIds = listOf("char-player"),
            requiredStateDeltaTargetIds = listOf("event-reboot-cli-irreversible"),
        )
        val resultFile = Files.createTempFile("reboot-cli-result", ".json")
        val contractFile = Files.createTempFile("reboot-cli-contract", ".json")
        val approvalFile = Files.createTempFile("reboot-cli-approval", ".json")
        Files.writeString(resultFile, JsonSupport.json.encodeToString(result))
        Files.writeString(contractFile, JsonSupport.json.encodeToString(contract))
        Files.writeString(approvalFile, JsonSupport.json.encodeToString(approval(context, result, contract.changeId)))

        val exitCode = Cli().run(
            listOf(
                "resolve-turn",
                "--db",
                repository.databasePath().toString(),
                "--session",
                session.id,
                "--result",
                resultFile.toString(),
                "--irreversible-contract",
                contractFile.toString(),
                "--irreversible-approval",
                approvalFile.toString(),
            ),
        )

        assertEquals(0, exitCode)
        assertEquals(1, repository.appliedTurnCount(session.id))
        val evidence = repository.tableRows("reboot_run_evidence").single()
        assertEquals("human:test-reviewer", evidence["irreversible_approval_actor"])
        assertEquals("1", evidence["irreversible_approval_granted"])
    }

    @Test
    fun concurrentDuplicateResolveAppliesAtMostOnce() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "I observe the room before acting.")
        val result = validResult(context, "reboot-concurrent-result")
        val ready = CountDownLatch(2)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)
        val futures = (1..2).map {
            executor.submit<Boolean> {
                ready.countDown()
                start.await()
                runCatching { runtime.resolveTurn(session.id, result) }.isSuccess
            }
        }
        assertTrue(ready.await(10, TimeUnit.SECONDS))
        start.countDown()
        val successCount = futures.count { it.get(30, TimeUnit.SECONDS) }
        executor.shutdownNow()

        assertEquals(1, successCount)
        assertEquals(1, repository.appliedTurnCount(session.id))
        assertEquals(1, repository.tableCount("state_deltas"))
        val evidence = repository.tableRows("reboot_run_evidence")
        assertTrue(evidence.any { it["status"] == "APPLIED" })
        assertTrue(evidence.any { it["status"] != "APPLIED" })
    }

    @Test
    fun applyFailureRollsBackStoryStateAndMarksEvidence() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "I observe the room before acting.")
        DriverManager.getConnection("jdbc:sqlite:${repository.databasePath()}").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(
                    """
                    CREATE TRIGGER fail_reboot_test_turn_apply
                    BEFORE UPDATE OF validation_status ON turn_logs
                    WHEN NEW.validation_status = 'applied'
                    BEGIN
                        SELECT RAISE(ABORT, 'forced reboot test rollback');
                    END
                    """.trimIndent(),
                )
            }
        }

        assertFailsWith<Exception> {
            runtime.resolveTurn(session.id, validResult(context, "reboot-apply-failure"))
        }

        assertEquals(0, repository.appliedTurnCount(session.id))
        assertEquals(0, repository.tableCount("state_deltas"))
        val evidence = repository.tableRows("reboot_run_evidence").single()
        assertEquals("APPLY_FAILED", evidence["status"])
        assertTrue(evidence["apply_result_json"].orEmpty().contains("forced reboot test rollback"))
    }

    private fun validResult(context: ContextPacket, resultId: String): AiTurnResult =
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
            narrative = "The player studies the room while the witness keeps her own counsel.",
            turnSummary = "The player observes before acting.",
            characterActions = listOf(action("char-player")),
            stateDeltas = listOf(eventDelta("event-$resultId")),
        )

    private fun action(characterId: String): CharacterAction =
        CharacterAction(
            characterId = characterId,
            intent = "Understand the immediate situation.",
            visibleAction = "Studies the room and waits before committing.",
            hiddenMotive = "Avoid revealing uncertainty too early.",
            characterFitScore = 90,
            storyProgressScore = 72,
        )

    private fun eventDelta(targetId: String): DeltaOperation =
        DeltaOperation(
            type = DeltaOperationType.ADD_EVENT,
            targetType = "event",
            targetId = targetId,
            payload = JsonObject(mapOf("summary" to JsonPrimitive("A deliberate observation changes the immediate situation."))),
        )

    private fun approval(
        context: ContextPacket,
        result: AiTurnResult,
        changeId: String,
    ): IrreversibleApprovalArtifact =
        IrreversibleApprovalArtifact(
            approvalId = "approval-$changeId",
            changeId = changeId,
            contextHash = context.contextHash,
            resultId = result.resultId,
            humanActorId = "human:test-reviewer",
            decision = "APPROVE",
            approvedAt = "2026-07-11T00:00:00Z",
        )

    private fun seededRepository(): SqliteStoryRepository {
        val repository = SqliteStoryRepository(Files.createTempDirectory("reboot-run-evidence-test").resolve("story.db"))
        repository.initialize()
        repository.seedDemo(readJson(samplePath("demo_project_seed.json")))
        return repository
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private fun samplePath(fileName: String): Path {
        val candidates = listOf(Path.of("samples", fileName), Path.of("..", "samples", fileName))
        return candidates.firstOrNull { Files.exists(it) } ?: error("Cannot find sample file: $fileName")
    }
}
