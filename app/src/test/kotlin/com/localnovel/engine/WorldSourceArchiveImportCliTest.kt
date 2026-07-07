package com.localnovel.engine

import com.localnovel.engine.model.WorldSourceArchiveSqlImportResult
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class WorldSourceArchiveImportCliTest {
    @Test
    fun cliAppliesWorldSourceArchiveIntoSqlite() {
        val tempDir = Files.createTempDirectory("world-source-archive-cli")
        val dbPath = tempDir.resolve("story.db")
        val outPath = tempDir.resolve("world_source_archive_sql_import_result.json")

        val exit = Cli().run(
            listOf(
                "apply-world-source-archive",
                "--db",
                dbPath.toString(),
                "--manifest",
                WorldSourceArchiveImportServiceTest.manifestPath.toString(),
                "--out",
                outPath.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(outPath))
        val result = readJson<WorldSourceArchiveSqlImportResult>(outPath)
        assertEquals("PASS", result.verdict)
        assertEquals("WORLD_SOURCE_ARCHIVE_SQL_IMPORT_PASS", result.status)
        assertEquals("official_world_source_sql_import_only_not_runtime_entity_conversion", result.downstreamBoundary)
        assertEquals(9, result.documentRowsWritten)
        assertTrue(result.chunkRowsWritten >= 9)
        assertEquals(9, SqliteStoryRepository(dbPath).tableRows("world_source_documents").size)
        assertTrue(SqliteStoryRepository(dbPath).tableRows("world_source_chunks").size >= 9)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
}
