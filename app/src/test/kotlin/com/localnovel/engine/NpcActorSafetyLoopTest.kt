package com.localnovel.engine

import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.NpcActorSafetyLoopInput
import com.localnovel.engine.model.NpcActorSafetyLoopResult
import com.localnovel.engine.model.NpcActorSafetyLoopSuite
import com.localnovel.engine.model.NpcActorSafetyLoopTechnicalReport
import com.localnovel.engine.model.PhysicalEvidenceManifest
import com.localnovel.engine.service.NpcActorJson
import com.localnovel.engine.service.NpcActorSafetyLoopEvaluationService
import com.localnovel.engine.service.NpcActorSafetyLoopFixtureFactory
import com.localnovel.engine.service.NpcActorSafetyLoopService
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class NpcActorSafetyLoopTest {
    private val fixtures = NpcActorSafetyLoopFixtureFactory.createCases().associateBy { it.caseId }
    private val service = NpcActorSafetyLoopService()

    @Test
    fun `first valid attempt becomes candidate without state mutation`() {
        val fixture = fixture("npc-actor05-mira-stable-first-pass")
        val result = service.run(fixture.context, fixture.input)

        assertTrue(result.accepted)
        assertEquals(NpcActorSafetyLoopService.ACCEPTED_FOR_FORMAL_RESOLVE_CANDIDATE, result.status)
        assertEquals(1, result.attemptsProcessed)
        assertEquals(1, result.acceptedAttemptNumber)
        assertNotNull(result.acceptedProposal)
        assertFalse(result.formalStateWritePerformed)
        assertTrue(result.attemptResults.single().timeBudgetAccepted == true)
    }

    @Test
    fun `actor knowledge failure creates safe repair and second attempt succeeds`() {
        val fixture = fixture("npc-actor05-lian-knowledge-repair")
        val result = service.run(fixture.context, fixture.input)

        assertEquals(NpcActorSafetyLoopService.ACCEPTED_FOR_FORMAL_RESOLVE_CANDIDATE, result.status)
        assertEquals(2, result.acceptedAttemptNumber)
        assertEquals(NpcActorSafetyLoopService.ACTOR_CONTRACT, result.attemptResults.first().rejectionStage)
        val repair = assertNotNull(result.attemptResults.first().repairRequest)
        assertEquals(listOf("actor:foreign_knowledge_reference"), repair.rejectionCodes)
        assertFalse(repair.engineOwnedContextIncluded)
        assertNull(result.attemptResults.last().repairRequest)
    }

    @Test
    fun `single failed file response returns repair required without engine measurements`() {
        val fixture = fixture("npc-actor05-ren-load-repair")
        val firstOnly = fixture.input.copy(responseAttempts = fixture.input.responseAttempts.take(1))
        val result = service.run(fixture.context, firstOnly)
        val encoded = NpcActorJson.strict.encodeToString(result)

        assertEquals(NpcActorSafetyLoopService.REPAIR_RESPONSE_REQUIRED, result.status)
        assertFalse(result.accepted)
        assertNull(result.acceptedProposal)
        assertNotNull(result.attemptResults.single().repairRequest)
        listOf("physicalArchetypes", "physicalSnapshot", "massKg", "dimensionsCm", "maxLoadKg", "gravityMps2")
            .forEach { forbidden -> assertFalse(encoded.contains(forbidden), "Repair output leaked $forbidden") }
    }

    @Test
    fun `world location and time budget remain separate fail closed stages`() {
        val fixture = fixture("npc-actor05-mira-stable-first-pass")
        val oneAttempt = fixture.input.copy(maxAttempts = 1)
        val actorLocation = fixture.context.continuityConstraints.characterLocations["adult"]!!
        val sameLocationContext = fixture.context.copy(
            continuityConstraints = fixture.context.continuityConstraints.copy(
                characterLocations = fixture.context.continuityConstraints.characterLocations + ("player" to actorLocation),
            ),
        )
        val worldRejected = service.run(sameLocationContext, oneAttempt)
        val timeRejected = service.run(fixture.context, oneAttempt.copy(maxElapsedMinutes = 5))

        assertEquals(NpcActorSafetyLoopService.WORLD_LEGALITY, worldRejected.attemptResults.single().rejectionStage)
        assertTrue("world:player_not_offscreen" in worldRejected.attemptResults.single().findings)
        assertEquals(NpcActorSafetyLoopService.TIME_BUDGET, timeRejected.attemptResults.single().rejectionStage)
        assertTrue(timeRejected.attemptResults.single().gameMasterAccepted == true)
        assertTrue(timeRejected.attemptResults.single().timeBudgetAccepted == false)
    }

    @Test
    fun `storm gate keeps both unsafe attempts rejected`() {
        val fixture = fixture("npc-actor05-yan-storm-gate")
        val result = service.run(fixture.context, fixture.input)

        assertEquals(NpcActorSafetyLoopService.REJECTED_AFTER_MAX_ATTEMPTS, result.status)
        assertEquals(2, result.attemptsProcessed)
        assertNull(result.acceptedAttemptNumber)
        assertNull(result.acceptedProposal)
        assertTrue(result.attemptResults.all { it.rejectionStage == NpcActorSafetyLoopService.PHYSICAL_AFFORDANCE })
        assertTrue(result.attemptResults.first().findings.any { "PUSH_LIMIT_EXCEEDED" in it })
        assertTrue(result.attemptResults.last().findings.any { "MOVEMENT_BLOCKED_BY_BARRIER" in it })
    }

    @Test
    fun `strict file protocol creates and evaluates all four committed-style cases`() {
        val repositoryRoot = Files.createTempDirectory("npc-actor05-fixture-root")
        val (suitePath, suite) = NpcActorSafetyLoopFixtureFactory.writeFixtures(repositoryRoot)
        val report = NpcActorSafetyLoopEvaluationService().evaluate(repositoryRoot, suite)

        assertTrue(Files.isRegularFile(suitePath))
        assertEquals(4, suite.cases.size)
        assertEquals(4, report.expectationMatchCount)
        assertEquals(0, report.expectationMismatchCount)
        assertEquals(3, report.acceptedCaseCount)
        assertEquals(1, report.rejectedCaseCount)
        assertEquals("TECHNICAL_PASS_MODEL_INTEGRATION_NOT_AUTHORIZED", report.technicalDecision)
        assertFalse(report.stateMutationPerformed)
    }

    @Test
    fun `cli reaches fixture creation single loop and four case evaluation`() {
        val repositoryRoot = Files.createTempDirectory("npc-actor05-cli-root")
        assertEquals(
            0,
            Cli().run(
                listOf(
                    "create-npc-actor-safety-loop-suite",
                    "--repo", repositoryRoot.toString(),
                ),
            ),
        )
        val suitePath = repositoryRoot.resolve("samples/npc_actor05/${NpcActorSafetyLoopFixtureFactory.SUITE_FILE_NAME}")
        val suite = read<NpcActorSafetyLoopSuite>(suitePath)
        val first = suite.cases.first()
        val singleResultPath = repositoryRoot.resolve("single-result.json")
        val reportPath = repositoryRoot.resolve("report.json")

        assertEquals(
            0,
            Cli().run(
                listOf(
                    "run-npc-actor-safety-loop",
                    "--context", repositoryRoot.resolve(first.contextFile.path).toString(),
                    "--input", repositoryRoot.resolve(first.inputFile.path).toString(),
                    "--out", singleResultPath.toString(),
                ),
            ),
        )
        assertEquals(
            0,
            Cli().run(
                listOf(
                    "evaluate-npc-actor-safety-loop-suite",
                    "--repo", repositoryRoot.toString(),
                    "--suite", suitePath.toString(),
                    "--report", reportPath.toString(),
                ),
            ),
        )

        assertEquals(NpcActorSafetyLoopService.ACCEPTED_FOR_FORMAL_RESOLVE_CANDIDATE, read<NpcActorSafetyLoopResult>(singleResultPath).status)
        assertEquals(4, read<NpcActorSafetyLoopTechnicalReport>(reportPath).expectationMatchCount)
    }

    @Test
    fun `schema keeps loop repair and result objects strict`() {
        val schema = Json.parseToJsonElement(Files.readString(schemaPath())).jsonObject
        val definitions = schema["\$defs"]!!.jsonObject

        assertEquals("false", definitions["input"]!!.jsonObject["additionalProperties"].toString())
        assertEquals("false", definitions["repairRequest"]!!.jsonObject["additionalProperties"].toString())
        assertEquals("false", definitions["result"]!!.jsonObject["additionalProperties"].toString())
        assertEquals("2", definitions["input"]!!.jsonObject["properties"]!!.jsonObject["maxAttempts"]!!.jsonObject["maximum"].toString())
    }

    @Test
    fun `committed fixtures report and hashes remain reproducible`() {
        val root = repositoryRoot()
        val committedDir = root.resolve("samples/npc_actor05")
        val suite = read<NpcActorSafetyLoopSuite>(committedDir.resolve(NpcActorSafetyLoopFixtureFactory.SUITE_FILE_NAME))
        val committedReport = read<NpcActorSafetyLoopTechnicalReport>(committedDir.resolve("npc_actor05_safety_loop_report_v1.json"))
        val manifest = read<PhysicalEvidenceManifest>(committedDir.resolve("npc_actor05_evidence_hashes.json"))
        val regeneratedRoot = Files.createTempDirectory("npc-actor05-regenerated")
        val (_, regeneratedSuite) = NpcActorSafetyLoopFixtureFactory.writeFixtures(regeneratedRoot)

        assertEquals(committedReport, NpcActorSafetyLoopEvaluationService().evaluate(root, suite))
        assertEquals(suite, regeneratedSuite)
        suite.cases.flatMap { listOf(it.contextFile.path, it.inputFile.path) }
            .forEach { relative ->
                assertTrue(
                    Files.readAllBytes(root.resolve(relative)).contentEquals(Files.readAllBytes(regeneratedRoot.resolve(relative))),
                    "Fixture generator drifted: $relative",
                )
            }
        manifest.files.forEach { evidence ->
            val path = root.resolve(evidence.path)
            assertEquals(evidence.bytes, Files.size(path), "Byte size drifted: ${evidence.path}")
            assertEquals(evidence.sha256, sha256(path), "SHA-256 drifted: ${evidence.path}")
        }
    }

    private fun fixture(caseId: String) = fixtures[caseId] ?: error("Missing NPC-ACTOR-05 fixture: $caseId")

    private inline fun <reified T> read(path: Path): T =
        NpcActorJson.strict.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private fun schemaPath(): Path = listOf(
        Path.of("schemas/npc_actor/npc_actor_safety_loop_v1.schema.json"),
        Path.of("../schemas/npc_actor/npc_actor_safety_loop_v1.schema.json"),
    ).firstOrNull(Files::exists) ?: error("Cannot find NPC-ACTOR-05 schema.")

    private fun repositoryRoot(): Path = listOf(Path.of("."), Path.of(".."))
        .map { it.toAbsolutePath().normalize() }
        .firstOrNull { Files.isRegularFile(it.resolve("settings.gradle.kts")) }
        ?: error("Cannot find repository root.")

    private fun sha256(path: Path): String = MessageDigest.getInstance("SHA-256")
        .digest(Files.readAllBytes(path))
        .joinToString("") { "%02x".format(it) }
}
