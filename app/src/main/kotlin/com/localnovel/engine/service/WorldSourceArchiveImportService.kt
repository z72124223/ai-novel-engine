package com.localnovel.engine.service

import com.localnovel.engine.model.WorldSourceArchiveCheck
import com.localnovel.engine.model.WorldSourceArchiveEvidence
import com.localnovel.engine.model.WorldSourceArchiveFinding
import com.localnovel.engine.model.WorldSourceArchiveManifest
import com.localnovel.engine.model.WorldSourceArchiveSqlImportResult
import com.localnovel.engine.model.WorldSourceArchiveValidationReport
import com.localnovel.engine.model.WorldSourceBoundaryFlags
import com.localnovel.engine.model.WorldSourceDocumentImport
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.time.Instant

class WorldSourceArchiveImportService(
    private val repository: SqliteStoryRepository? = null,
) {
    fun validateManifestFile(manifestPath: Path): WorldSourceArchiveValidationReport {
        val manifestText = Files.readString(manifestPath).removePrefix("\uFEFF")
        val manifest = JsonSupport.json.decodeFromString<WorldSourceArchiveManifest>(manifestText)
        val findings = mutableListOf<WorldSourceArchiveFinding>()
        val checks = mutableListOf<WorldSourceArchiveCheck>()
        val documents = loadDocuments(manifest, manifestPath, findings)

        fun check(ruleId: String, passed: Boolean, detail: String) {
            checks += WorldSourceArchiveCheck(ruleId = ruleId, status = if (passed) "PASS" else "FAIL", detail = detail)
        }

        val expectedStatus = manifest.status == "CHENXING_WORLD_SOURCE_RAW_ARCHIVE_READY"
        check("WORLD_SOURCE_MANIFEST_STATUS", expectedStatus, manifest.status)
        if (!expectedStatus) {
            findings += WorldSourceArchiveFinding(
                ruleId = "WORLD_SOURCE_MANIFEST_STATUS_INVALID",
                severity = "error",
                message = "Manifest must be CHENXING_WORLD_SOURCE_RAW_ARCHIVE_READY before SQL archive import.",
                path = "status",
            )
        }

        val sourceSetPresent = manifest.sourceSets.isNotEmpty()
        check("WORLD_SOURCE_SOURCE_SETS_PRESENT", sourceSetPresent, "sourceSets=${manifest.sourceSets.size}")
        if (!sourceSetPresent) {
            findings += WorldSourceArchiveFinding(
                ruleId = "WORLD_SOURCE_SOURCE_SETS_MISSING",
                severity = "error",
                message = "Manifest has no sourceSets.",
                path = "sourceSets",
            )
        }

        val safeArchiveFlags = manifest.rawTextUnmodified &&
            manifest.classificationOnly &&
            !manifest.contentRewriteAllowed &&
            !manifest.contentSummaryAllowed
        check(
            "WORLD_SOURCE_RAW_ARCHIVE_FLAGS",
            safeArchiveFlags,
            "rawTextUnmodified=${manifest.rawTextUnmodified}, classificationOnly=${manifest.classificationOnly}",
        )
        if (!safeArchiveFlags) {
            findings += WorldSourceArchiveFinding(
                ruleId = "WORLD_SOURCE_RAW_ARCHIVE_FLAGS_INVALID",
                severity = "error",
                message = "Raw archive must preserve text and must not permit rewrite or summary.",
                path = "rawTextUnmodified",
            )
        }

        val unsafeBoundaryFlagCount = unsafeBoundaryFlagCount(manifest.boundaryFlags)
        check("WORLD_SOURCE_BOUNDARY_FLAGS_SAFE", unsafeBoundaryFlagCount == 0, "unsafeBoundaryFlagCount=$unsafeBoundaryFlagCount")
        if (unsafeBoundaryFlagCount > 0) {
            findings += WorldSourceArchiveFinding(
                ruleId = "WORLD_SOURCE_BOUNDARY_FLAGS_UNSAFE",
                severity = "error",
                message = "Official source archive cannot be runtime entity data, setting-pack data, media, release, or production lock.",
                path = "boundaryFlags",
            )
        }

        val idsUnique = manifest.sourceSets.map { it.sourceId }.distinct().size == manifest.sourceSets.size
        check("WORLD_SOURCE_IDS_UNIQUE", idsUnique, "sourceIds=${manifest.sourceSets.size}")
        if (!idsUnique) {
            findings += WorldSourceArchiveFinding(
                ruleId = "WORLD_SOURCE_ID_DUPLICATE",
                severity = "error",
                message = "sourceSets contain duplicate sourceId values.",
                path = "sourceSets",
            )
        }

        val verifiedSourceCount = documents.count { it != null }
        val missingRawFileCount = manifest.sourceSets.size - verifiedSourceCount
        val hashMismatchCount = findings.count { it.ruleId == "WORLD_SOURCE_HASH_MISMATCH" }
        check(
            "WORLD_SOURCE_RAW_FILES_VERIFIED",
            missingRawFileCount == 0 && hashMismatchCount == 0,
            "verified=$verifiedSourceCount/${manifest.sourceSets.size}, hashMismatchCount=$hashMismatchCount",
        )

        val evidence = WorldSourceArchiveEvidence(
            sourceCount = manifest.sourceSets.size,
            verifiedSourceCount = verifiedSourceCount,
            totalByteSize = documents.filterNotNull().sumOf { it.source.byteSize },
            hashMismatchCount = hashMismatchCount,
            missingRawFileCount = missingRawFileCount,
            unsafeBoundaryFlagCount = unsafeBoundaryFlagCount,
            projectLabel = manifest.titleRelationship.activeProjectLabel,
            providedSourceTitle = manifest.titleRelationship.providedSourceTitle,
            titleRelationshipStatus = manifest.titleRelationship.status,
            officialSourceData = true,
            sourceAuthorityStatus = "official_source",
        )
        val pass = findings.none { it.severity == "error" }
        return WorldSourceArchiveValidationReport(
            verdict = if (pass) "PASS" else "FAIL",
            status = if (pass) "WORLD_SOURCE_ARCHIVE_VALIDATION_PASS" else "WORLD_SOURCE_ARCHIVE_VALIDATION_FAIL",
            manifestId = manifest.manifestId,
            sourceManifestPath = manifestPath.toString(),
            nextAllowedDownstreamStep = if (pass) "world_source_archive_sql_import" else "",
            evidence = evidence,
            checks = checks,
            findings = findings,
        )
    }

    fun applyManifestFile(
        manifestPath: Path,
        databasePath: Path,
    ): WorldSourceArchiveSqlImportResult {
        val repository = repository ?: error("WorldSourceArchiveImportService.applyManifestFile requires a repository.")
        repository.initialize()
        val validation = validateManifestFile(manifestPath)
        val manifestText = Files.readString(manifestPath).removePrefix("\uFEFF")
        val manifest = JsonSupport.json.decodeFromString<WorldSourceArchiveManifest>(manifestText)
        if (validation.verdict != "PASS") {
            return WorldSourceArchiveSqlImportResult(
                verdict = "FAIL",
                status = "WORLD_SOURCE_ARCHIVE_SQL_IMPORT_BLOCKED",
                archiveId = manifest.manifestId,
                sourceManifestPath = manifestPath.toString(),
                databasePath = databasePath.toString(),
                validation = validation,
                boundaryFlags = manifest.boundaryFlags,
            )
        }

        val documents = loadDocuments(manifest, manifestPath, mutableListOf()).filterNotNull()
        val chunks = documents.flatMap { document -> document.toChunks(manifest) }
        val now = Instant.now().toString()
        repository.withTransaction { connection ->
            repository.executeUpdate(connection, "DELETE FROM world_source_chunks WHERE archive_id = ?", manifest.manifestId)
            repository.executeUpdate(connection, "DELETE FROM world_source_documents WHERE archive_id = ?", manifest.manifestId)
            repository.executeUpdate(connection, "DELETE FROM world_source_archives WHERE archive_id = ?", manifest.manifestId)
            repository.insertRawRow(
                connection = connection,
                tableName = "world_source_archives",
                row = mapOf(
                    "archive_id" to manifest.manifestId,
                    "project_label" to manifest.titleRelationship.activeProjectLabel,
                    "provided_source_title" to manifest.titleRelationship.providedSourceTitle,
                    "title_relationship_status" to manifest.titleRelationship.status,
                    "status" to manifest.status,
                    "locale" to manifest.locale,
                    "manifest_path" to manifestPath.toString(),
                    "manifest_json" to manifestText,
                    "raw_text_unmodified" to manifest.rawTextUnmodified.sqliteBool(),
                    "classification_only" to manifest.classificationOnly.sqliteBool(),
                    "content_rewrite_allowed" to manifest.contentRewriteAllowed.sqliteBool(),
                    "content_summary_allowed" to manifest.contentSummaryAllowed.sqliteBool(),
                    "official_source_data" to true.sqliteBool(),
                    "source_authority_status" to "official_source",
                    "canon_approved" to manifest.boundaryFlags.canonApproved.sqliteBool(),
                    "setting_pack_data" to manifest.boundaryFlags.settingPackData.sqliteBool(),
                    "runtime_sqlite_data" to manifest.boundaryFlags.runtimeSqliteData.sqliteBool(),
                    "approved_asset_data" to manifest.boundaryFlags.approvedAssetData.sqliteBool(),
                    "source_count" to manifest.sourceSets.size.toString(),
                    "total_byte_size" to documents.sumOf { it.source.byteSize }.toString(),
                    "created_at" to now,
                    "updated_at" to now,
                ),
            )
            documents.forEach { document ->
                repository.insertRawRow(
                    connection = connection,
                    tableName = "world_source_documents",
                    row = mapOf(
                        "document_id" to "${manifest.manifestId}:${document.source.sourceId}",
                        "archive_id" to manifest.manifestId,
                        "project_label" to manifest.titleRelationship.activeProjectLabel,
                        "source_id" to document.source.sourceId,
                        "document_order" to document.source.order.toString(),
                        "classification" to document.source.classification,
                        "title" to document.source.title,
                        "raw_path" to document.resolvedRawPath,
                        "original_attachment_path" to document.source.originalAttachmentPath,
                        "source_kind" to document.source.sourceKind,
                        "copy_mode" to document.source.copyMode,
                        "byte_size" to document.source.byteSize.toString(),
                        "sha256" to document.source.sha256.uppercase(),
                        "raw_text" to document.rawText,
                        "raw_text_unmodified" to document.source.rawTextUnmodified.sqliteBool(),
                        "official_source_data" to true.sqliteBool(),
                        "source_authority_status" to "official_source",
                        "metadata_json" to JsonSupport.json.encodeToString(document.metadata),
                        "created_at" to now,
                    ),
                )
            }
            chunks.forEach { chunk ->
                repository.insertRawRow(
                    connection = connection,
                    tableName = "world_source_chunks",
                    row = mapOf(
                        "chunk_id" to chunk.chunkId,
                        "document_id" to chunk.documentId,
                        "archive_id" to manifest.manifestId,
                        "project_label" to manifest.titleRelationship.activeProjectLabel,
                        "source_id" to chunk.sourceId,
                        "chunk_order" to chunk.chunkOrder.toString(),
                        "classification" to chunk.classification,
                        "title" to chunk.title,
                        "heading" to chunk.heading,
                        "content" to chunk.content,
                        "search_text" to chunk.searchText,
                        "byte_size" to chunk.byteSize.toString(),
                        "sha256" to chunk.sha256,
                        "official_source_data" to true.sqliteBool(),
                        "source_authority_status" to "official_source",
                        "created_at" to now,
                    ),
                )
            }
        }

        return WorldSourceArchiveSqlImportResult(
            verdict = "PASS",
            status = "WORLD_SOURCE_ARCHIVE_SQL_IMPORT_PASS",
            archiveId = manifest.manifestId,
            sourceManifestPath = manifestPath.toString(),
            databasePath = databasePath.toString(),
            archiveRowsWritten = 1,
            documentRowsWritten = documents.size,
            chunkRowsWritten = chunks.size,
            sourceCount = manifest.sourceSets.size,
            totalByteSize = documents.sumOf { it.source.byteSize },
            validation = validation,
            boundaryFlags = manifest.boundaryFlags,
        )
    }

    private fun loadDocuments(
        manifest: WorldSourceArchiveManifest,
        manifestPath: Path,
        findings: MutableList<WorldSourceArchiveFinding>,
    ): List<WorldSourceDocumentImport?> =
        manifest.sourceSets.map { source ->
            val rawPath = resolveRawPath(source.rawPath, manifestPath)
            if (!Files.exists(rawPath)) {
                findings += WorldSourceArchiveFinding(
                    ruleId = "WORLD_SOURCE_RAW_FILE_MISSING",
                    severity = "error",
                    message = "Raw source file is missing: ${source.rawPath}",
                    path = "sourceSets.${source.order}.rawPath",
                )
                return@map null
            }
            val bytes = Files.readAllBytes(rawPath)
            val actualHash = bytes.sha256Upper()
            if (actualHash != source.sha256.uppercase()) {
                findings += WorldSourceArchiveFinding(
                    ruleId = "WORLD_SOURCE_HASH_MISMATCH",
                    severity = "error",
                    message = "Raw source hash mismatch for ${source.sourceId}.",
                    path = "sourceSets.${source.order}.sha256",
                )
            }
            if (bytes.size.toLong() != source.byteSize) {
                findings += WorldSourceArchiveFinding(
                    ruleId = "WORLD_SOURCE_BYTE_SIZE_MISMATCH",
                    severity = "error",
                    message = "Raw source byte size mismatch for ${source.sourceId}.",
                    path = "sourceSets.${source.order}.byteSize",
                )
            }
            WorldSourceDocumentImport(
                source = source,
                rawText = bytes.toString(Charsets.UTF_8).removePrefix("\uFEFF"),
                resolvedRawPath = rawPath.toString(),
                metadata = JsonObject(
                    mapOf(
                        "sourceId" to JsonPrimitive(source.sourceId),
                        "manifestStatus" to JsonPrimitive(manifest.status),
                        "titleRelationshipStatus" to JsonPrimitive(manifest.titleRelationship.status),
                    ),
                ),
            )
        }

    private fun resolveRawPath(rawPath: String, manifestPath: Path): Path {
        val path = Path.of(rawPath)
        if (path.isAbsolute) return path.normalize()
        val cwd = Path.of("").toAbsolutePath().normalize()
        val candidates = buildList {
            add(cwd.resolve(path).normalize())
            generateSequence(cwd) { it.parent }.drop(1).forEach { add(it.resolve(path).normalize()) }
            manifestPath.parent?.let { add(it.resolve(path).normalize()) }
        }
        return candidates.firstOrNull { Files.exists(it) } ?: cwd.resolve(path).normalize()
    }

    private fun unsafeBoundaryFlagCount(flags: WorldSourceBoundaryFlags): Int =
        listOf(
            flags.canonApproved,
            flags.settingPackData,
            flags.runtimeSqliteData,
            flags.approvedAssetData,
            flags.providerApiCalls,
            flags.mediaGenerated,
            flags.finalMediaApproval,
            flags.releaseApproved,
            flags.productionLock,
            flags.rendererInvocation != "none",
            flags.ffmpegInvocation != "none",
            flags.ttsInvocation != "none",
        ).count { it }

    private fun Boolean.sqliteBool(): String = if (this) "1" else "0"

    private fun ByteArray.sha256Upper(): String =
        MessageDigest.getInstance("SHA-256")
            .digest(this)
            .joinToString("") { "%02X".format(it) }

    private fun String.sha256Upper(): String = toByteArray(Charsets.UTF_8).sha256Upper()

    private fun WorldSourceDocumentImport.toChunks(manifest: WorldSourceArchiveManifest): List<WorldSourceChunkImport> {
        val documentId = "${manifest.manifestId}:${source.sourceId}"
        val rawChunks = splitIntoChunks(rawText)
        return rawChunks.mapIndexed { index, chunk ->
            val chunkId = "$documentId:chunk-${(index + 1).toString().padStart(4, '0')}"
            val searchText = listOf(source.title, source.classification, chunk.heading, chunk.content)
                .filter { it.isNotBlank() }
                .joinToString("\n")
            WorldSourceChunkImport(
                chunkId = chunkId,
                documentId = documentId,
                sourceId = source.sourceId,
                chunkOrder = index + 1,
                classification = source.classification,
                title = source.title,
                heading = chunk.heading,
                content = chunk.content,
                searchText = searchText,
                byteSize = chunk.content.toByteArray(Charsets.UTF_8).size.toLong(),
                sha256 = chunk.content.sha256Upper(),
            )
        }
    }

    private fun splitIntoChunks(text: String): List<RawChunk> {
        val chunks = mutableListOf<RawChunk>()
        var heading = ""
        val buffer = StringBuilder()

        fun flush() {
            val content = buffer.toString().trim()
            if (content.isNotBlank()) chunks += RawChunk(heading = heading, content = content)
            buffer.clear()
        }

        text.lineSequence().forEach { line ->
            val trimmed = line.trim()
            val isHeading = trimmed.startsWith("#")
            if (isHeading && buffer.isNotBlank()) flush()
            if (isHeading) heading = trimmed.trimStart('#').trim()
            buffer.appendLine(line)
            if (buffer.length >= 2400 && trimmed.isBlank()) flush()
        }
        flush()
        return chunks.ifEmpty { listOf(RawChunk(heading = "", content = text)) }
    }

    private data class RawChunk(
        val heading: String,
        val content: String,
    )

    private data class WorldSourceChunkImport(
        val chunkId: String,
        val documentId: String,
        val sourceId: String,
        val chunkOrder: Int,
        val classification: String,
        val title: String,
        val heading: String,
        val content: String,
        val searchText: String,
        val byteSize: Long,
        val sha256: String,
    )
}
