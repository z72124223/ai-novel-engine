package com.localnovel.engine

import com.localnovel.engine.model.NpcActorPhysicalBindingSuite
import com.localnovel.engine.model.NpcActorPhysicalBindingTechnicalReport
import com.localnovel.engine.model.NpcActorPhysicalResponse
import com.localnovel.engine.model.PhysicalEvidenceFile
import com.localnovel.engine.model.PhysicalEvidenceManifest
import com.localnovel.engine.service.FakeNpcActorPhysicalGateway
import com.localnovel.engine.service.FileNpcActorPhysicalGateway
import com.localnovel.engine.service.NpcActorPhysicalBindingEvaluationService
import com.localnovel.engine.service.NpcActorPhysicalBindingFixtureFactory
import com.localnovel.engine.service.NpcActorPhysicalBindingRequestBuilder
import com.localnovel.engine.service.NpcActorPhysicalBindingValidator
import com.localnovel.engine.service.NpcActorPhysicalJson
import com.localnovel.engine.service.NpcActorPhysicalPilotService
import com.localnovel.engine.service.PhysicalAffordanceValidator
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.Path
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
import kotlinx.serialization.json.jsonObject

class NpcActorPhysicalBindingTest {
    private val suite = NpcActorPhysicalBindingFixtureFactory.createSuite()
    private val evaluationService = NpcActorPhysicalBindingEvaluationService()
    private val requestBuilder = NpcActorPhysicalBindingRequestBuilder()
    private val validator = NpcActorPhysicalBindingValidator()

    @Test
    fun `all eight binding cases match deterministic expectations`() {
        val report = evaluationService.evaluate(suite)

        assertEquals(8, report.caseCount)
        assertEquals(8, report.expectationMatchCount)
        assertEquals(0, report.expectationMismatchCount)
        assertEquals(2, report.acceptedCaseCount)
        assertEquals(6, report.rejectedCaseCount)
        assertEquals(
            "NPC_ACTION_BINDING_TECHNICAL_PASS_FORMAL_RUNTIME_NOT_CONNECTED",
            report.technicalDecision,
        )
        assertEquals("NOT_CONNECTED", report.formalTurnRuntimeStatus)
    }

    @Test
    fun `adult dog shoulder proposal never reaches game master review`() {
        val result = evaluateCase("binding-02-dog-shoulder-rejected")

        assertFalse(result.accepted)
        assertEquals(NpcActorPhysicalBindingValidator.REJECTED_PHYSICAL_AFFORDANCE, result.status)
        assertNull(result.proposal)
        assertEquals(
            listOf(
                PhysicalAffordanceValidator.BODY_PLAN_NOT_SUPPORTED,
                PhysicalAffordanceValidator.SUPPORT_CAPACITY_EXCEEDED,
                PhysicalAffordanceValidator.SUPPORT_USE_NOT_ALLOWED,
                PhysicalAffordanceValidator.TARGET_NOT_STABLE_SUPPORT,
            ).sorted(),
            result.physicalValidation?.rejectionCodes,
        )
    }

    @Test
    fun `valid chair support passes both gates but remains game master candidate`() {
        val result = evaluateCase("binding-01-chair-support-accepted")

        assertTrue(result.accepted)
        assertEquals(
            NpcActorPhysicalBindingValidator.ACCEPTED_FOR_GAME_MASTER_REVIEW_PHYSICALLY_VALIDATED,
            result.status,
        )
        assertNotNull(result.proposal)
        assertTrue(result.actorValidation.accepted)
        assertTrue(result.physicalValidation?.accepted == true)
    }

    @Test
    fun `wall reach and unbound targets fail before state proposal`() {
        val wall = evaluateCase("binding-03-reach-through-wall-rejected")
        val unbound = evaluateCase("binding-06-target-not-bound")

        assertEquals(
            listOf(PhysicalAffordanceValidator.REACH_BLOCKED_BY_BARRIER),
            wall.physicalValidation?.rejectionCodes,
        )
        assertNull(wall.proposal)
        assertTrue("binding:physical_target_not_bound" in unbound.findings)
        assertNull(unbound.physicalValidation)
        assertNull(unbound.proposal)
    }

    @Test
    fun `drop returns required effects and does not mutate engine context`() {
        val case = suite.cases.single { it.caseId == "binding-04-drop-effects-accepted" }
        val input = inputFor(case.caseId)
        val before = NpcActorPhysicalJson.strict.encodeToString(input.engineContext)
        val result = validator.validateAndMap(
            input.prompt,
            input.engineContext,
            responseFor(case.caseId),
        )

        assertTrue(result.accepted)
        assertEquals(
            listOf(PhysicalAffordanceValidator.FALL_DOWNWARD, PhysicalAffordanceValidator.RELEASE_HELD_OBJECT),
            result.physicalValidation?.requiredEffects,
        )
        assertEquals(before, NpcActorPhysicalJson.strict.encodeToString(input.engineContext))
    }

    @Test
    fun `model prompt excludes engine owned mass geometry gravity and barriers`() {
        val promptJson = NpcActorPhysicalJson.strict.encodeToString(
            inputFor("binding-01-chair-support-accepted").prompt,
        )

        assertFalse(promptJson.contains("massKg"))
        assertFalse(promptJson.contains("dimensionsCm"))
        assertFalse(promptJson.contains("gravityMps2"))
        assertFalse(promptJson.contains("barriers"))
        assertFalse(promptJson.contains("physicalArchetypes"))
        assertTrue(promptJson.contains("allowedPhysicalActionTypes"))
        assertTrue(promptJson.contains("visiblePhysicalTargetIds"))
    }

    @Test
    fun `fake and file gateways receive prompt only and strict response rejects unknown fields`() {
        val input = inputFor("binding-01-chair-support-accepted")
        val response = responseFor("binding-01-chair-support-accepted")
        val fakeResult = NpcActorPhysicalPilotService().propose(
            input,
            FakeNpcActorPhysicalGateway(mapOf(input.prompt.actorRequest.requestId to response)),
        )
        assertTrue(fakeResult.accepted)

        val temp = Files.createTempDirectory("npc-physical-file-gateway")
        val promptPath = temp.resolve("prompt.json")
        val responsePath = temp.resolve("response.json")
        val validJson = NpcActorPhysicalJson.strict.encodeToString(response)
        Files.writeString(responsePath, validJson.dropLast(2) + ",\n  \"unexpected\": true\n}")

        assertFails { FileNpcActorPhysicalGateway(promptPath, responsePath).propose(input.prompt) }
        val writtenPrompt = Files.readString(promptPath)
        assertFalse(writtenPrompt.contains("massKg"))
        assertEquals(input.prompt, NpcActorPhysicalJson.strict.decodeFromString(writtenPrompt))
    }

    @Test
    fun `binding schema is strict and fixes the evidence case count`() {
        val root = repositoryRoot()
        val schema = Json.parseToJsonElement(Files.readString(root.resolve(SCHEMA_PATH))).jsonObject
        val definitions = schema["\$defs"]!!.jsonObject

        assertEquals("false", definitions["suite"]!!.jsonObject["additionalProperties"].toString())
        assertEquals("false", definitions["bindingCase"]!!.jsonObject["additionalProperties"].toString())
        val cases = definitions["suite"]!!.jsonObject["properties"]!!.jsonObject["cases"]!!.jsonObject
        assertEquals("8", cases["minItems"].toString())
        assertEquals("8", cases["maxItems"].toString())
    }

    @Test
    fun `committed binding evidence is strict reproducible and hash verified`() {
        writeEvidenceWhenRequested()
        val root = repositoryRoot()
        val suitePath = root.resolve(SUITE_PATH)
        val reportPath = root.resolve(REPORT_PATH)
        val manifestPath = root.resolve(MANIFEST_PATH)
        if (!Files.isRegularFile(suitePath) || !Files.isRegularFile(reportPath) || !Files.isRegularFile(manifestPath)) return

        val committedSuite = NpcActorPhysicalJson.strict.decodeFromString<NpcActorPhysicalBindingSuite>(
            Files.readString(suitePath),
        )
        val committedReport = NpcActorPhysicalJson.strict.decodeFromString<NpcActorPhysicalBindingTechnicalReport>(
            Files.readString(reportPath),
        )
        assertEquals(suite, committedSuite)
        assertEquals(evaluationService.evaluate(suite), committedReport)

        val manifest = NpcActorPhysicalJson.strict.decodeFromString<PhysicalEvidenceManifest>(
            Files.readString(manifestPath),
        )
        assertEquals("WORLD-PHYSICS-02 NPC ACTION BINDING", manifest.scope)
        manifest.files.forEach { file ->
            val path = root.resolve(file.path)
            assertEquals(file.bytes, Files.size(path), file.path)
            assertEquals(file.sha256, sha256(path), file.path)
        }
    }

    private fun evaluateCase(caseId: String) = validator.validateAndMap(
        inputFor(caseId).prompt,
        inputFor(caseId).engineContext,
        responseFor(caseId),
    )

    private fun inputFor(caseId: String) = suite.cases.single { it.caseId == caseId }.let { case ->
        requestBuilder.build(
            actorRequest = case.actorRequest,
            physicalArchetypes = suite.physicalArchetypes,
            physicalSnapshot = suite.physicalSnapshots.single { it.snapshotId == case.snapshotId },
            allowedPhysicalActionTypes = case.allowedPhysicalActionTypes,
            visiblePhysicalTargetIds = case.visiblePhysicalTargetIds,
        )
    }

    private fun responseFor(caseId: String): NpcActorPhysicalResponse =
        suite.cases.single { it.caseId == caseId }.let { case ->
            NpcActorPhysicalResponse(
                requestId = case.actorRequest.requestId,
                actorResponse = case.actorResponse,
                physicalActionProposal = case.physicalActionProposal,
            )
        }

    private fun writeEvidenceWhenRequested() {
        val outputRoot = System.getenv("WORLD_PHYSICS02_EVIDENCE_ROOT")
            ?.takeIf { it.isNotBlank() }
            ?.let(::Path)
            ?: return
        val suitePath = outputRoot.resolve(SUITE_PATH)
        val reportPath = outputRoot.resolve(REPORT_PATH)
        val manifestPath = outputRoot.resolve(MANIFEST_PATH)
        Files.createDirectories(suitePath.parent)
        Files.writeString(
            suitePath,
            NpcActorPhysicalJson.strict.encodeToString(suite) + "\n",
            StandardCharsets.UTF_8,
        )
        Files.writeString(
            reportPath,
            NpcActorPhysicalJson.strict.encodeToString(evaluationService.evaluate(suite)) + "\n",
            StandardCharsets.UTF_8,
        )
        val manifest = PhysicalEvidenceManifest(
            scope = "WORLD-PHYSICS-02 NPC ACTION BINDING",
            files = listOf(SUITE_PATH, REPORT_PATH, SCHEMA_PATH).map { relative ->
                val path = outputRoot.resolve(relative)
                PhysicalEvidenceFile(relative.replace('\\', '/'), sha256(path), Files.size(path))
            },
        )
        Files.writeString(
            manifestPath,
            NpcActorPhysicalJson.strict.encodeToString(manifest) + "\n",
            StandardCharsets.UTF_8,
        )
    }

    private fun repositoryRoot(): Path = Path("").toAbsolutePath().normalize().let { current ->
        if (Files.isDirectory(current.resolve("app"))) current else current.parent
    }

    private fun sha256(path: Path): String = MessageDigest.getInstance("SHA-256")
        .digest(Files.readAllBytes(path))
        .joinToString("") { byte -> "%02x".format(byte) }

    private companion object {
        const val SUITE_PATH = "samples/world_physics02/npc_actor_physical_binding_suite_v1.json"
        const val REPORT_PATH = "samples/world_physics02/npc_actor_physical_binding_report_v1.json"
        const val MANIFEST_PATH = "samples/world_physics02/npc_actor_physical_binding_evidence_hashes.json"
        const val SCHEMA_PATH = "schemas/world_physics/npc_actor_physical_binding_v1.schema.json"
    }
}
