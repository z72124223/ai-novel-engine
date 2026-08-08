package com.localnovel.engine.service

import com.localnovel.engine.model.RetrievalBaselineCategorySummary
import com.localnovel.engine.model.RetrievalBaselineHit
import com.localnovel.engine.model.RetrievalBaselineQueryResult
import com.localnovel.engine.model.RetrievalBenchmarkQuery
import com.localnovel.engine.model.RetrievalBenchmarkQuerySet
import com.localnovel.engine.model.RetrievalExactBaselineReport
import com.localnovel.engine.model.WorldSourceArchiveManifest
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlinx.serialization.decodeFromString

class RetrievalExactBaselineService(
    private val repository: SqliteStoryRepository,
) {
    fun evaluate(querySetPath: Path, topK: Int = 8): RetrievalExactBaselineReport {
        val queryBytes = Files.readAllBytes(querySetPath).toString(Charsets.UTF_8)
            .replace("\r\n", "\n").replace("\r", "\n").toByteArray(Charsets.UTF_8)
        val querySet = JsonSupport.json.decodeFromString<RetrievalBenchmarkQuerySet>(
            queryBytes.toString(Charsets.UTF_8).removePrefix("\uFEFF"),
        )
        validateQuerySet(querySet)
        val sourceManifest = resolveSourceManifest(querySetPath, querySet.sourceManifestPath)
        val canonicalSourceManifestBytes = Files.readAllBytes(sourceManifest).toString(Charsets.UTF_8)
            .replace("\r\n", "\n").replace("\r", "\n").toByteArray(Charsets.UTF_8)
        require(sha256(canonicalSourceManifestBytes).equals(querySet.sourceManifestSha256, true)) {
            "Query-set source manifest SHA-256 mismatch: ${querySet.sourceManifestPath}"
        }
        val sourceEvidence = JsonSupport.json.decodeFromString<WorldSourceArchiveManifest>(
            canonicalSourceManifestBytes.toString(Charsets.UTF_8),
        ).sourceSets.associate { source -> source.sourceId to (source.rawPath.replace('\\', '/') to source.sha256) }
        repository.initialize()
        val chunks = loadChunks(sourceEvidence)
        val results = querySet.queries.map { query -> evaluateQuery(query, chunks, topK.coerceIn(1, 50)) }
        val retrievable = results.filter { it.expectedDisposition == "RETRIEVE" }
        val findings = buildList {
            if (querySet.queries.size < 50) add("QUERY_SET_BELOW_50")
            if (results.any { it.visibilityLeak }) add("VISIBILITY_LEAK")
            if (results.any { it.expectedDisposition == "CORPUS_GAP" }) add("NON_WORLD_CORPUS_NOT_INDEXED")
            if (results.any { it.expectedDisposition == "BLOCK_BY_POLICY" }) add("CHARACTER_VISIBLE_RAW_SOURCE_REQUIRES_EXPLICIT_POLICY_BLOCK")
            add("MODEL_DOWNLOAD_NOT_AUTHORIZED")
            add("EMBEDDING_AND_RERANKER_NOT_EXECUTED")
        }
        val categorySummaries = results.groupBy { it.category }.map { (category, categoryResults) ->
            RetrievalBaselineCategorySummary(
                category = category,
                queryCount = categoryResults.size,
                retrievableCount = categoryResults.count { it.expectedDisposition == "RETRIEVE" },
                likeTop8HitCount = categoryResults.count { it.likeTop8Hit },
                lexicalTop8HitCount = categoryResults.count { it.lexicalTop8Hit },
                blockedByPolicyCount = categoryResults.count { it.actualDisposition == "BLOCKED_BY_POLICY" },
                corpusGapCount = categoryResults.count { it.actualDisposition == "CORPUS_GAP_RECORDED" },
            )
        }.sortedBy { it.category }
        val likeHits = retrievable.count { it.likeTop8Hit }
        val lexicalHits = retrievable.count { it.lexicalTop8Hit }
        val leakCount = results.count { it.visibilityLeak }
        return RetrievalExactBaselineReport(
            verdict = if (querySet.queries.size >= 50 && leakCount == 0) "PASS" else "FAIL",
            status = "RETRIEVAL_EXACT_BASELINE_RECORDED",
            querySetId = querySet.querySetId,
            querySetSha256 = sha256(queryBytes),
            queryCount = results.size,
            retrievableCount = retrievable.size,
            blockedByPolicyCount = results.count { it.actualDisposition == "BLOCKED_BY_POLICY" },
            corpusGapCount = results.count { it.actualDisposition == "CORPUS_GAP_RECORDED" },
            likeTop8HitCount = likeHits,
            lexicalTop8HitCount = lexicalHits,
            likeTop8HitRate = rate(likeHits, retrievable.size),
            lexicalTop8HitRate = rate(lexicalHits, retrievable.size),
            visibilityLeakCount = leakCount,
            categorySummaries = categorySummaries,
            results = results,
            findings = findings,
            nextAllowedStep = "MODEL_EXECUTION_GATE_REVIEW",
        )
    }

    private fun evaluateQuery(query: RetrievalBenchmarkQuery, chunks: List<CorpusChunk>, topK: Int): RetrievalBaselineQueryResult {
        if (query.expectedDisposition == "BLOCK_BY_POLICY") {
            return emptyResult(query, "BLOCKED_BY_POLICY")
        }
        if (query.expectedDisposition == "CORPUS_GAP") {
            return emptyResult(query, "CORPUS_GAP_RECORDED")
        }
        val normalizedQuery = query.text.normalizeForRetrieval()
        val likeHits = chunks.filter { normalizedQuery.isNotBlank() && it.searchText.normalizeForRetrieval().contains(normalizedQuery) }
            .sortedWith(compareBy<CorpusChunk> { it.sourceId }.thenBy { it.chunkOrder }).take(topK)
        val grams = lexicalUnits(normalizedQuery)
        val lexical = chunks.mapNotNull { chunk ->
            val haystack = chunk.searchText.normalizeForRetrieval()
            val matched = grams.filter(haystack::contains)
            if (matched.isEmpty()) return@mapNotNull null
            val exactBonus = if (haystack.contains(normalizedQuery)) 100.0 else 0.0
            val score = exactBonus + matched.sumOf { if (it.length >= 4) 4.0 else 1.0 } + matched.distinct().size.toDouble() / grams.size.coerceAtLeast(1)
            ScoredChunk(chunk, score, matched.distinct().sorted())
        }.sortedWith(compareByDescending<ScoredChunk> { it.score }.thenBy { it.chunk.sourceId }.thenBy { it.chunk.chunkOrder })
            .take(topK)
        val hits = lexical.mapIndexed { index, scored -> scored.toHit(index + 1) }
        return RetrievalBaselineQueryResult(
            queryId = query.queryId,
            category = query.category,
            text = query.text,
            expectedDisposition = query.expectedDisposition,
            actualDisposition = "RETRIEVED",
            expectedSourceIds = query.expectedSourceIds,
            likeTop8Hit = likeHits.any { it.sourceId in query.expectedSourceIds },
            lexicalTop8Hit = hits.any { it.sourceId in query.expectedSourceIds },
            visibilityLeak = query.visibilityTarget != "AUTHOR" && hits.isNotEmpty(),
            hits = hits,
        )
    }

    private fun emptyResult(query: RetrievalBenchmarkQuery, disposition: String) = RetrievalBaselineQueryResult(
        queryId = query.queryId,
        category = query.category,
        text = query.text,
        expectedDisposition = query.expectedDisposition,
        actualDisposition = disposition,
        expectedSourceIds = query.expectedSourceIds,
        likeTop8Hit = false,
        lexicalTop8Hit = false,
        visibilityLeak = false,
        hits = emptyList(),
    )

    private fun loadChunks(sourceEvidence: Map<String, Pair<String, String>>): List<CorpusChunk> = repository.withConnection { connection ->
        repository.queryRows(
            connection,
            """
            SELECT c.chunk_id, c.source_id, c.chunk_order, c.heading, c.content, c.search_text,
                   c.sha256 AS chunk_sha256
            FROM world_source_chunks c
            JOIN world_source_documents d ON d.document_id = c.document_id
            WHERE c.official_source_data = 1 AND c.source_authority_status = 'official_source'
            ORDER BY c.source_id, c.chunk_order
            """.trimIndent(),
        ).map { row ->
            CorpusChunk(
                chunkId = row["chunk_id"].orEmpty(),
                sourceId = row["source_id"].orEmpty(),
                chunkOrder = row["chunk_order"]?.toIntOrNull() ?: 0,
                heading = row["heading"].orEmpty(),
                content = row["content"].orEmpty(),
                searchText = row["search_text"].orEmpty(),
                chunkSha256 = row["chunk_sha256"].orEmpty(),
                sourcePath = sourceEvidence[row["source_id"].orEmpty()]?.first
                    ?: error("Source path missing from manifest for ${row["source_id"]}"),
                sourceSha256 = sourceEvidence[row["source_id"].orEmpty()]?.second
                    ?: error("Source hash missing from manifest for ${row["source_id"]}"),
            )
        }
    }

    private fun validateQuerySet(querySet: RetrievalBenchmarkQuerySet) {
        require(querySet.schemaVersion == "1.0")
        require(querySet.status == "RETRIEVAL_01_LABELED_QUERY_SET_READY")
        require(querySet.queries.map { it.queryId }.distinct().size == querySet.queries.size)
        require(querySet.queries.all { it.category in ALLOWED_CATEGORIES })
        require(querySet.queries.all { it.visibilityTarget in setOf("AUTHOR", "CHARACTER") })
        require(querySet.queries.all { it.expectedDisposition in setOf("RETRIEVE", "BLOCK_BY_POLICY", "CORPUS_GAP") })
        require(querySet.queries.filter { it.expectedDisposition == "RETRIEVE" }.all { it.expectedSourceIds.isNotEmpty() })
    }

    private fun resolveSourceManifest(querySetPath: Path, relativePath: String): Path {
        val candidate = Path.of(relativePath)
        require(!candidate.isAbsolute && !candidate.normalize().startsWith("..")) {
            "Query-set source manifest must be Repository-relative."
        }
        val starts = listOfNotNull(querySetPath.toAbsolutePath().normalize().parent, Path.of("").toAbsolutePath().normalize())
        return starts.asSequence().flatMap { start -> generateSequence(start) { it.parent } }
            .map { it.resolve(candidate).normalize() }
            .firstOrNull(Files::isRegularFile)
            ?: error("Query-set source manifest not found: $relativePath")
    }

    private fun lexicalUnits(text: String): List<String> {
        val units = mutableSetOf<String>()
        Regex("[A-Za-z0-9_-]{2,}").findAll(text).forEach { units += it.value.lowercase() }
        val hanRuns = Regex("[\\p{IsHan}]+").findAll(text).map { it.value }.toList()
        hanRuns.forEach { run ->
            if (run.length == 1) units += run
            if (run.length >= 2) (0 until run.length - 1).forEach { units += run.substring(it, it + 2) }
        }
        return units.sorted()
    }

    private fun String.normalizeForRetrieval(): String = lowercase().replace(Regex("\\s+"), " ").trim()

    private fun ScoredChunk.toHit(rank: Int): RetrievalBaselineHit = RetrievalBaselineHit(
        rank = rank,
        chunkId = chunk.chunkId,
        sourceId = chunk.sourceId,
        sourcePath = chunk.sourcePath,
        sourceSha256 = chunk.sourceSha256,
        chunkSha256 = chunk.chunkSha256,
        heading = chunk.heading,
        sourceQuote = chunk.content.replace(Regex("\\s+"), " ").trim().take(320).replace("\\", "\\\\"),
        score = score,
        reasons = matchedUnits.map { "LEXICAL:$it" },
    )

    private fun rate(numerator: Int, denominator: Int): Double =
        if (denominator == 0) 0.0 else numerator.toDouble() / denominator

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes).joinToString("") { "%02x".format(it) }

    private data class CorpusChunk(
        val chunkId: String,
        val sourceId: String,
        val chunkOrder: Int,
        val heading: String,
        val content: String,
        val searchText: String,
        val chunkSha256: String,
        val sourcePath: String,
        val sourceSha256: String,
    )

    private data class ScoredChunk(val chunk: CorpusChunk, val score: Double, val matchedUnits: List<String>)

    companion object {
        val ALLOWED_CATEGORIES = setOf(
            "EXACT_ENTITY",
            "SEMANTIC_PARAPHRASE",
            "TEMPORAL_CURRENT_STATE",
            "CAUSAL",
            "CODE_DEVELOPMENT_DOC",
            "KNOWLEDGE_BOUNDARY",
            "CHAPTER_CONTINUITY",
        )
    }
}
