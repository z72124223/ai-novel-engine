package com.localnovel.engine

import com.localnovel.engine.model.WorldSourceQueryReport
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class WorldSourceQueryCliTest {
    @Test
    fun cliQueriesImportedOfficialWorldSourceChunks() {
        val tempDir = Files.createTempDirectory("world-source-query-cli")
        val dbPath = tempDir.resolve("story.db")
        val importOut = tempDir.resolve("import.json")
        val queryOut = tempDir.resolve("query_aijiang.json")

        val importExit = Cli().run(
            listOf(
                "apply-world-source-archive",
                "--db",
                dbPath.toString(),
                "--manifest",
                WorldSourceArchiveImportServiceTest.manifestPath.toString(),
                "--out",
                importOut.toString(),
            ),
        )
        assertEquals(0, importExit)

        val queryExit = Cli().run(
            listOf(
                "query-world-source",
                "--db",
                dbPath.toString(),
                "--q",
                "愛醬",
                "--classification",
                "companion_detailed_setting",
                "--limit",
                "5",
                "--out",
                queryOut.toString(),
            ),
        )

        assertEquals(0, queryExit)
        val report = readJson<WorldSourceQueryReport>(queryOut)
        assertEquals("PASS", report.verdict)
        assertEquals("WORLD_SOURCE_QUERY_PASS", report.status)
        assertEquals("companion_detailed_setting", report.classification)
        assertTrue(report.hitCount >= 1)
        assertTrue(report.hits.any { it.title == "愛醬詳細設定" })
        assertTrue(report.hits.all { it.officialSourceData })
        assertTrue(report.hits.all { it.sourceAuthorityStatus == "official_source" })
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
}
