package com.localnovel.engine

import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.WorldSourceArchiveImportService
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorldSourceArchiveImportServiceTest {
    @Test
    fun validatesChenxingWorldSourceArchiveManifest() {
        val report = WorldSourceArchiveImportService().validateManifestFile(manifestPath)

        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("WORLD_SOURCE_ARCHIVE_VALIDATION_PASS", report.status)
        assertEquals("chenxing-world-source-manifest-v0", report.manifestId)
        assertEquals(9, report.evidence.sourceCount)
        assertEquals(9, report.evidence.verifiedSourceCount)
        assertEquals(0, report.evidence.hashMismatchCount)
        assertEquals(0, report.evidence.missingRawFileCount)
        assertEquals(0, report.evidence.unsafeBoundaryFlagCount)
        assertTrue(report.evidence.officialSourceData)
        assertEquals("official_source", report.evidence.sourceAuthorityStatus)
        assertEquals("塵行", report.evidence.projectLabel)
        assertEquals("穿越4000年", report.evidence.providedSourceTitle)
        assertEquals("world_source_archive_sql_import", report.nextAllowedDownstreamStep)
    }

    @Test
    fun importsChenxingWorldSourceArchiveIntoSqlite() {
        val tempDir = Files.createTempDirectory("world-source-archive-import")
        val dbPath = tempDir.resolve("story.db")
        val repository = SqliteStoryRepository(dbPath)

        val result = WorldSourceArchiveImportService(repository).applyManifestFile(
            manifestPath = manifestPath,
            databasePath = dbPath,
        )

        assertEquals("PASS", result.verdict, result.validation.findings.joinToString { it.ruleId })
        assertEquals("WORLD_SOURCE_ARCHIVE_SQL_IMPORT_PASS", result.status)
        assertEquals(1, result.archiveRowsWritten)
        assertEquals(9, result.documentRowsWritten)
        assertTrue(result.chunkRowsWritten >= 9)
        assertEquals(9, repository.tableRows("world_source_documents").size)
        assertTrue(repository.tableRows("world_source_chunks").size >= 9)
        val archiveRows = repository.tableRows("world_source_archives")
        assertEquals(1, archiveRows.size)
        assertEquals("chenxing-world-source-manifest-v0", archiveRows.first()["archive_id"])
        assertEquals("1", archiveRows.first()["official_source_data"])
        assertEquals("official_source", archiveRows.first()["source_authority_status"])
        assertTrue(repository.tableRows("world_source_documents").any { it["title"] == "愛醬詳細設定" })
    }

    companion object {
        private val manifestRelativePath: Path = Path.of("memory/chenxing_world_source_manifest.json")
        val manifestPath: Path =
            generateSequence(Path.of("").toAbsolutePath()) { it.parent }
                .map { it.resolve(manifestRelativePath) }
                .firstOrNull { Files.exists(it) }
                ?: manifestRelativePath
    }
}
