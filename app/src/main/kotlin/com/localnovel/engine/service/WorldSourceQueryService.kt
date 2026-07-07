package com.localnovel.engine.service

import com.localnovel.engine.model.WorldSourceQueryHit
import com.localnovel.engine.model.WorldSourceQueryReport
import com.localnovel.engine.repository.SqliteStoryRepository

class WorldSourceQueryService(
    private val repository: SqliteStoryRepository,
) {
    fun query(
        text: String,
        classification: String = "",
        limit: Int = 10,
    ): WorldSourceQueryReport {
        repository.initialize()
        val safeLimit = limit.coerceIn(1, 50)
        val normalizedQuery = text.trim()
        val normalizedClassification = classification.trim()
        val rows = repository.withConnection { connection ->
            val where = mutableListOf<String>()
            val args = mutableListOf<String?>()
            if (normalizedQuery.isNotBlank()) {
                where += "search_text LIKE ? ESCAPE '\\'"
                args += normalizedQuery.likePattern()
            }
            if (normalizedClassification.isNotBlank()) {
                where += "classification = ?"
                args += normalizedClassification
            }
            val sql = buildString {
                append(
                    """
                    SELECT chunk_id, archive_id, source_id, classification, title, heading,
                           chunk_order, content, official_source_data, source_authority_status
                    FROM world_source_chunks
                    """.trimIndent(),
                )
                if (where.isNotEmpty()) append("\nWHERE ${where.joinToString(" AND ")}")
                append("\nORDER BY source_id, chunk_order")
                append("\nLIMIT $safeLimit")
            }
            repository.queryRows(connection, sql, *args.toTypedArray())
        }
        val hits = rows.map { row ->
            val content = row["content"].orEmpty()
            WorldSourceQueryHit(
                chunkId = row["chunk_id"].orEmpty(),
                archiveId = row["archive_id"].orEmpty(),
                sourceId = row["source_id"].orEmpty(),
                classification = row["classification"].orEmpty(),
                title = row["title"].orEmpty(),
                heading = row["heading"].orEmpty(),
                chunkOrder = row["chunk_order"]?.toIntOrNull() ?: 0,
                snippet = content.snippetAround(normalizedQuery),
                officialSourceData = row["official_source_data"] == "1",
                sourceAuthorityStatus = row["source_authority_status"].orEmpty(),
            )
        }
        return WorldSourceQueryReport(
            verdict = "PASS",
            status = "WORLD_SOURCE_QUERY_PASS",
            query = normalizedQuery,
            classification = normalizedClassification,
            hitCount = hits.size,
            hits = hits,
        )
    }

    private fun String.likePattern(): String =
        "%" + replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_") + "%"

    private fun String.snippetAround(query: String, radius: Int = 160): String {
        if (isBlank()) return ""
        if (query.isBlank()) return take(radius * 2).normalizeWhitespace()
        val index = indexOf(query, ignoreCase = true)
        if (index < 0) return take(radius * 2).normalizeWhitespace()
        val start = (index - radius).coerceAtLeast(0)
        val end = (index + query.length + radius).coerceAtMost(length)
        val prefix = if (start > 0) "..." else ""
        val suffix = if (end < length) "..." else ""
        return (prefix + substring(start, end) + suffix).normalizeWhitespace()
    }

    private fun String.normalizeWhitespace(): String =
        replace(Regex("\\s+"), " ").trim()
}
