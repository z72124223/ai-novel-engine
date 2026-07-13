package com.localnovel.engine

import com.localnovel.engine.service.ProjectDataRebuildService
import com.localnovel.engine.service.RetrievalExactBaselineService
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.model.RetrievalExactBaselineReport
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class Retrieval01ExactBaselineTest {
    @Test
    fun recordsDeterministicFiftyQueryBaselineWithNoVisibilityLeaks() {
        val firstDatabase = Files.createTempDirectory("retrieval01-first").resolve("story.db")
        val secondDatabase = Files.createTempDirectory("retrieval01-second").resolve("story.db")
        val rebuild = ProjectDataRebuildService()
        rebuild.rebuild(repositoryRoot, rebuildManifest, firstDatabase)
        rebuild.rebuild(repositoryRoot, rebuildManifest, secondDatabase)

        val first = RetrievalExactBaselineService(SqliteStoryRepository(firstDatabase)).evaluate(querySet)
        val second = RetrievalExactBaselineService(SqliteStoryRepository(secondDatabase)).evaluate(querySet)

        assertEquals("PASS", first.verdict)
        assertEquals(50, first.queryCount)
        assertEquals(38, first.retrievableCount)
        assertEquals(6, first.blockedByPolicyCount)
        assertEquals(6, first.corpusGapCount)
        assertEquals(0, first.visibilityLeakCount)
        assertEquals(first, second)
        assertEquals(7, first.categorySummaries.size)
        assertTrue(first.results.filter { it.expectedDisposition == "BLOCK_BY_POLICY" }.all { it.hits.isEmpty() })
        assertTrue(first.results.filter { it.expectedDisposition == "CORPUS_GAP" }.all { it.hits.isEmpty() })
        val invalidProvenanceHits = first.results.flatMap { it.hits }.filterNot { hit ->
            hit.sourcePath.startsWith("world/chenxing/raw/") &&
                hit.sourceSha256.matches(Regex("[A-Fa-f0-9]{64}")) &&
                hit.chunkSha256.matches(Regex("[A-Fa-f0-9]{64}")) &&
                hit.sourceQuote.isNotBlank()
        }
        assertTrue(invalidProvenanceHits.isEmpty(), invalidProvenanceHits.take(5).toString())
        assertFalse(first.findings.contains("VISIBILITY_LEAK"))
        val encoded = JsonSupport.json.encodeToString(first)
        assertEquals(first, JsonSupport.json.decodeFromString<RetrievalExactBaselineReport>(encoded))
    }

    companion object {
        private val rebuildRelative = Path.of("world/chenxing/engine/chenxing_data_rebuild_manifest_v1.json")
        private val queryRelative = Path.of("samples/retrieval/chenxing_retrieval_benchmark_queries_v1.json")
        private val repositoryRoot = generateSequence(Path.of("").toAbsolutePath()) { it.parent }
            .first { Files.isRegularFile(it.resolve(rebuildRelative)) }
        private val rebuildManifest = repositoryRoot.resolve(rebuildRelative)
        private val querySet = repositoryRoot.resolve(queryRelative)
    }
}
