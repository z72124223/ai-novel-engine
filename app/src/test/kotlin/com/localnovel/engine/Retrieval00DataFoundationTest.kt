package com.localnovel.engine

import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.model.ProjectDataRebuildManifest
import com.localnovel.engine.service.ProjectDataRebuildService
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class Retrieval00DataFoundationTest {
    @Test
    fun rebuildsChenxingTwiceWithTheSameLogicalContent() {
        val firstDirectory = Files.createTempDirectory("retrieval00-first")
        val secondDirectory = Files.createTempDirectory("retrieval00-second")
        val firstDatabase = firstDirectory.resolve("story.db")
        val secondDatabase = secondDirectory.resolve("story.db")
        val service = ProjectDataRebuildService()

        val first = service.rebuild(repositoryRoot, manifestPath, firstDatabase)
        val second = service.rebuild(repositoryRoot, manifestPath, secondDatabase)

        assertEquals("PASS", first.verdict, first.findings.joinToString { it.ruleId })
        assertEquals("PASS", second.verdict, second.findings.joinToString { it.ruleId })
        assertEquals(22, first.verifiedInputs.size)
        assertTrue(first.verifiedInputs.all { it.status == "PASS" })
        assertEquals(5, first.settingPacksApplied.size)
        assertTrue(first.canonValid)
        assertEquals(9, first.worldSourceDocumentCount)
        assertEquals(255, first.worldSourceChunkCount)
        assertEquals(first.logicalContent.rootSha256, second.logicalContent.rootSha256)
        assertEquals(first.logicalContent.tables, second.logicalContent.tables)
        assertEquals(16, first.logicalContent.migrationCount)
        assertTrue(first.authorityInventory.unclassifiedPaths.isEmpty())
        assertTrue(first.authorityInventory.missingRequiredPaths.isEmpty())
        assertTrue(first.authorityInventory.privateOrAbsolutePathFindings.isEmpty())
        assertTrue(first.authorityInventory.trackedFileCount > 0)
        assertTrue(first.authorityInventory.classSummaries.isNotEmpty())
        assertTrue(first.authorityInventory.routeSummaries.isNotEmpty())
    }

    @Test
    fun preservesAuthorOnlyAndWorldSourceSafetyBoundaries() {
        val database = Files.createTempDirectory("retrieval00-boundary").resolve("story.db")
        val report = ProjectDataRebuildService().rebuild(repositoryRoot, manifestPath, database)
        val repository = SqliteStoryRepository(database)

        assertEquals("PASS", report.verdict)
        assertFalse(report.boundary.allowCanonPromotion)
        assertFalse(report.boundary.allowRuntimeStateMutation)
        assertFalse(report.boundary.allowDeletion)
        assertFalse(report.boundary.allowProviderCalls)
        assertTrue(
            repository.tableRows("dependency_edges").none {
                it["visibility"] == "author_only" && it["runtime_relevant"] == "1"
            },
        )
        val archive = repository.tableRows("world_source_archives").single()
        assertEquals("1", archive["official_source_data"])
        assertEquals("official_source", archive["source_authority_status"])
        assertEquals("0", archive["canon_approved"])
        assertEquals("0", archive["runtime_sqlite_data"])
    }

    @Test
    fun rejectsHashMismatchAndExistingDatabase() {
        val tempDirectory = Files.createTempDirectory("retrieval00-negative")
        val manifest = JsonSupport.json.decodeFromString<ProjectDataRebuildManifest>(Files.readString(manifestPath))
        val copiedManifest = tempDirectory.resolve(manifestRelativePath)
        copiedManifest.parent?.let(Files::createDirectories)
        Files.copy(manifestPath, copiedManifest)
        (manifest.migrationInputs + manifest.settingPackInputs + manifest.worldSourceInputs).forEach { input ->
            val source = repositoryRoot.resolve(input.path)
            val target = tempDirectory.resolve(input.path)
            target.parent?.let(Files::createDirectories)
            Files.copy(source, target)
        }
        Files.writeString(
            tempDirectory.resolve(manifest.migrationInputs.first().path),
            Files.readString(repositoryRoot.resolve(manifest.migrationInputs.first().path)) + "\n-- tampered",
        )
        val service = ProjectDataRebuildService()
        val mismatchError = assertFailsWith<IllegalArgumentException> {
            service.rebuild(tempDirectory, copiedManifest, tempDirectory.resolve("mismatch.db"))
        }
        assertTrue(mismatchError.message.orEmpty().contains("INPUT_HASH_MISMATCH"), mismatchError.message)

        val existingDatabase = tempDirectory.resolve("existing.db")
        Files.writeString(existingDatabase, "must-not-be-overwritten")
        val overwriteError = assertFailsWith<IllegalArgumentException> {
            service.rebuild(repositoryRoot, manifestPath, existingDatabase)
        }
        assertTrue(overwriteError.message.orEmpty().contains("Refusing to overwrite"))
        assertEquals("must-not-be-overwritten", Files.readString(existingDatabase))
    }

    companion object {
        private val manifestRelativePath = Path.of("world/chenxing/engine/chenxing_data_rebuild_manifest_v1.json")
        private val repositoryRoot: Path = generateSequence(Path.of("").toAbsolutePath()) { it.parent }
            .first { Files.isRegularFile(it.resolve(manifestRelativePath)) }
        private val manifestPath: Path = repositoryRoot.resolve(manifestRelativePath)
    }
}
