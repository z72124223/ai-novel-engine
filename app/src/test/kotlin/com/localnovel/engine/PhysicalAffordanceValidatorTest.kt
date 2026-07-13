package com.localnovel.engine

import com.localnovel.engine.model.PhysicalActionProposal
import com.localnovel.engine.model.PhysicalActionType
import com.localnovel.engine.model.PhysicalAffordanceSuite
import com.localnovel.engine.model.PhysicalEvidenceFile
import com.localnovel.engine.model.PhysicalEvidenceManifest
import com.localnovel.engine.model.PhysicalSupportUse
import com.localnovel.engine.service.PhysicalAffordanceEvaluationService
import com.localnovel.engine.service.PhysicalAffordanceFixtureFactory
import com.localnovel.engine.service.PhysicalAffordanceJson
import com.localnovel.engine.service.PhysicalAffordanceValidator
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class PhysicalAffordanceValidatorTest {
    private val validator = PhysicalAffordanceValidator()
    private val evaluationService = PhysicalAffordanceEvaluationService(validator)
    private val suite = PhysicalAffordanceFixtureFactory.createSuite()

    @Test
    fun `all 64 fixed cases match deterministic expectations`() {
        val report = evaluationService.evaluate(suite)

        assertEquals(64, report.caseCount)
        assertEquals(64, report.expectationMatchCount)
        assertEquals(0, report.expectationMismatchCount)
        assertEquals(8, report.actionCoverage.size)
        assertTrue(report.actionCoverage.all { it.caseCount == 8 })
        assertEquals("FOUNDATION_TECHNICAL_PASS_RUNTIME_NOT_CONNECTED", report.technicalDecision)
        assertEquals("NOT_CONNECTED", report.runtimeIntegrationStatus)
    }

    @Test
    fun `adult cannot use medium dog shoulder as standing support`() {
        val evaluationCase = suite.cases.single { it.caseId == "support-02-dog-shoulder-adult" }
        val snapshot = suite.snapshots.single { it.snapshotId == evaluationCase.snapshotId }

        val result = validator.validate(suite.archetypes, snapshot, evaluationCase.proposal)

        assertFalse(result.accepted)
        assertEquals(
            listOf(
                PhysicalAffordanceValidator.BODY_PLAN_NOT_SUPPORTED,
                PhysicalAffordanceValidator.SUPPORT_CAPACITY_EXCEEDED,
                PhysicalAffordanceValidator.SUPPORT_USE_NOT_ALLOWED,
                PhysicalAffordanceValidator.TARGET_NOT_STABLE_SUPPORT,
            ).sorted(),
            result.rejectionCodes,
        )
    }

    @Test
    fun `stable chair arm can support bounded stand up assistance`() {
        val evaluationCase = suite.cases.single { it.caseId == "support-01-chair-assist" }
        val snapshot = suite.snapshots.single { it.snapshotId == evaluationCase.snapshotId }

        val result = validator.validate(suite.archetypes, snapshot, evaluationCase.proposal)

        assertTrue(result.accepted)
        assertEquals(emptyList(), result.rejectionCodes)
    }

    @Test
    fun `distance and wall are computed from engine snapshot`() {
        val far = suite.cases.single { it.caseId == "reach-02-far-chair" }
        val blocked = suite.cases.single { it.caseId == "reach-04-wall" }

        assertEquals(
            listOf(PhysicalAffordanceValidator.OUT_OF_REACH),
            validator.validate(suite.archetypes, suite.snapshots.single { it.snapshotId == far.snapshotId }, far.proposal).rejectionCodes,
        )
        assertEquals(
            listOf(PhysicalAffordanceValidator.REACH_BLOCKED_BY_BARRIER),
            validator.validate(suite.archetypes, suite.snapshots.single { it.snapshotId == blocked.snapshotId }, blocked.proposal).rejectionCodes,
        )
    }

    @Test
    fun `drop returns gravity effects instead of mutating state`() {
        val evaluationCase = suite.cases.single { it.caseId == "drop-01-held-above-ground" }
        val snapshot = suite.snapshots.single { it.snapshotId == evaluationCase.snapshotId }
        val before = PhysicalAffordanceJson.strict.encodeToString(snapshot)

        val result = validator.validate(suite.archetypes, snapshot, evaluationCase.proposal)

        assertTrue(result.accepted)
        assertEquals(
            listOf(PhysicalAffordanceValidator.FALL_DOWNWARD, PhysicalAffordanceValidator.RELEASE_HELD_OBJECT),
            result.requiredEffects,
        )
        assertEquals(before, PhysicalAffordanceJson.strict.encodeToString(snapshot))
    }

    @Test
    fun `missing physical evidence fails closed`() {
        val snapshot = suite.snapshots.single { it.snapshotId == "snapshot-base" }
        val missingTarget = PhysicalActionProposal(
            actionId = "missing-target",
            actionType = PhysicalActionType.USE_AS_SUPPORT,
            actorId = "adult",
            targetId = "unknown",
            supportSurfaceId = "shoulder",
            supportUse = PhysicalSupportUse.STAND_UP_ASSIST,
        )

        val result = validator.validate(suite.archetypes, snapshot, missingTarget)

        assertFalse(result.accepted)
        assertEquals(listOf(PhysicalAffordanceValidator.TARGET_NOT_FOUND), result.rejectionCodes)
    }

    @Test
    fun `committed evidence is strict and reproducible`() {
        writeEvidenceWhenRequested()
        val root = repositoryRoot()
        val suitePath = root.resolve(SUITE_PATH)
        val reportPath = root.resolve(REPORT_PATH)
        val manifestPath = root.resolve(MANIFEST_PATH)
        if (!Files.isRegularFile(suitePath) || !Files.isRegularFile(reportPath) || !Files.isRegularFile(manifestPath)) return

        val committedSuite = PhysicalAffordanceJson.strict.decodeFromString<PhysicalAffordanceSuite>(Files.readString(suitePath))
        assertEquals(suite, committedSuite)
        assertEquals(evaluationService.evaluate(suite), PhysicalAffordanceJson.strict.decodeFromString(Files.readString(reportPath)))

        val manifest = PhysicalAffordanceJson.strict.decodeFromString<PhysicalEvidenceManifest>(Files.readString(manifestPath))
        manifest.files.forEach { file ->
            val path = root.resolve(file.path)
            assertEquals(file.bytes, Files.size(path), file.path)
            assertEquals(file.sha256, sha256(path), file.path)
        }
    }

    private fun writeEvidenceWhenRequested() {
        val outputRoot = System.getenv("WORLD_PHYSICS_EVIDENCE_ROOT")?.takeIf { it.isNotBlank() }?.let(::Path) ?: return
        val suitePath = outputRoot.resolve(SUITE_PATH)
        val reportPath = outputRoot.resolve(REPORT_PATH)
        val manifestPath = outputRoot.resolve(MANIFEST_PATH)
        Files.createDirectories(suitePath.parent)
        Files.writeString(suitePath, PhysicalAffordanceJson.strict.encodeToString(suite) + "\n", StandardCharsets.UTF_8)
        Files.writeString(reportPath, PhysicalAffordanceJson.strict.encodeToString(evaluationService.evaluate(suite)) + "\n", StandardCharsets.UTF_8)
        val evidencePaths = listOf(SUITE_PATH, REPORT_PATH, SCHEMA_PATH)
        val manifest = PhysicalEvidenceManifest(
            files = evidencePaths.map { relative ->
                val path = outputRoot.resolve(relative)
                PhysicalEvidenceFile(relative.replace('\\', '/'), sha256(path), Files.size(path))
            },
        )
        Files.writeString(manifestPath, PhysicalAffordanceJson.strict.encodeToString(manifest) + "\n", StandardCharsets.UTF_8)
    }

    private fun repositoryRoot(): Path = Path("").toAbsolutePath().normalize().let { current ->
        if (Files.isDirectory(current.resolve("app"))) current else current.parent
    }

    private fun sha256(path: Path): String = MessageDigest.getInstance("SHA-256")
        .digest(Files.readAllBytes(path))
        .joinToString("") { byte -> "%02x".format(byte) }

    private companion object {
        const val SUITE_PATH = "samples/world_physics01/physical_affordance_suite_v1.json"
        const val REPORT_PATH = "samples/world_physics01/physical_affordance_technical_report_v1.json"
        const val MANIFEST_PATH = "samples/world_physics01/physical_affordance_evidence_hashes.json"
        const val SCHEMA_PATH = "schemas/world_physics/physical_affordance_v1.schema.json"
    }
}
