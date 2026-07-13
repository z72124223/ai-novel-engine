package com.localnovel.engine

import com.localnovel.engine.model.PhysicalEvidenceManifest
import com.localnovel.engine.model.WorldPhysics05PilotReport
import com.localnovel.engine.service.FormalTurnPhysicalGateValidator
import com.localnovel.engine.service.FormalTurnPhysicalJson
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class WorldPhysics05EmbodiedScenePilotTest {
    @Test
    fun `screen01 scene03 completes five formal turns with embodied continuity evidence`() {
        val root = repositoryRoot()
        val output = Files.createTempFile("world-physics05-cli-", ".json")
        val exitCode = Cli().run(
            listOf(
                "run-world-physics-embodied-pilot",
                "--repo",
                root.toString(),
                "--out",
                output.toString(),
            ),
        )

        assertEquals(0, exitCode)
        val generated = FormalTurnPhysicalJson.strict.decodeFromString<WorldPhysics05PilotReport>(Files.readString(output))
        val committed = FormalTurnPhysicalJson.strict.decodeFromString<WorldPhysics05PilotReport>(
            Files.readString(root.resolve(REPORT_PATH)),
        )
        assertEquals(committed, generated)
        assertEquals("WORLD_PHYSICS05_EMBODIED_SCENE_PILOT_PASS", generated.technicalDecision)
        assertEquals("NON_CANON_EVALUATION", generated.boundary)
        assertEquals(5, generated.resolvedTurnCount)
        assertEquals(1, generated.rejectedAttemptCount)
        assertEquals(5, generated.physicalTransitionCount)
        assertEquals((1..5).toList(), generated.turns.map { it.turnNumber })
        assertTrue(
            FormalTurnPhysicalGateValidator.PHYSICAL_AFFORDANCE_REJECTED in generated.turns.first().rejectionCodes,
        )

        val passThrough = generated.turns.single { it.turnNumber == 3 }
        assertEquals(passThrough.actorAfter.centerCm, passThrough.toolAfter.centerCm)
        assertEquals("character-lu-chenxing", passThrough.toolAfter.heldByEntityId)

        val placed = generated.turns.single { it.turnNumber == 4 }.toolAfter
        assertEquals("physical-stable-rock-fulcrum", placed.supportedByEntityId)
        assertEquals("fulcrum", placed.supportedBySurfaceId)

        val dropped = generated.turns.single { it.turnNumber == 5 }.toolAfter
        assertNull(dropped.heldByEntityId)
        assertNull(dropped.supportedByEntityId)
        assertNull(dropped.containedByEntityId)
        assertEquals(2.5, dropped.centerCm.z)

        assertTrue(generated.checks.sourceHashesVerified)
        assertTrue(generated.checks.dogSupportRejected)
        assertTrue(generated.checks.rejectedAttemptStateUnchanged)
        assertTrue(generated.checks.samePendingTurnRepairApplied)
        assertTrue(generated.checks.heldToolFollowedHolder)
        assertTrue(generated.checks.placedToolRelationPersisted)
        assertTrue(generated.checks.droppedToolRelationsCleared)
        assertTrue(generated.checks.formalRuntimeTurnCountVerified)

        val manifest = FormalTurnPhysicalJson.strict.decodeFromString<PhysicalEvidenceManifest>(
            Files.readString(root.resolve(MANIFEST_PATH)),
        )
        assertEquals("WORLD-PHYSICS-05 EMBODIED SCENE PILOT", manifest.scope)
        manifest.files.forEach { file ->
            val path = root.resolve(file.path)
            assertEquals(file.bytes, Files.size(path), file.path)
            assertEquals(file.sha256, sha256(path), file.path)
        }
    }

    private fun repositoryRoot(): Path = Path.of("").toAbsolutePath().normalize().let { current ->
        if (Files.isDirectory(current.resolve("app"))) current else current.parent
    }

    private fun sha256(path: Path): String = MessageDigest.getInstance("SHA-256")
        .digest(Files.readAllBytes(path))
        .joinToString("") { "%02x".format(it) }

    private companion object {
        const val REPORT_PATH = "samples/world_physics05/screen01_scene03_embodied_physical_pilot_v1.json"
        const val MANIFEST_PATH = "samples/world_physics05/world_physics05_evidence_hashes.json"
    }
}
