package com.localnovel.engine.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class WorldSourceTitleRelationship(
    val activeProjectLabel: String = "",
    val providedSourceTitle: String = "",
    val status: String = "",
    val rule: String = "",
)

@Serializable
data class WorldSourceBoundaryFlags(
    val canonApproved: Boolean = false,
    val settingPackData: Boolean = false,
    val runtimeSqliteData: Boolean = false,
    val approvedAssetData: Boolean = false,
    val providerApiCalls: Boolean = false,
    val rendererInvocation: String = "none",
    val ffmpegInvocation: String = "none",
    val ttsInvocation: String = "none",
    val mediaGenerated: Boolean = false,
    val finalMediaApproval: Boolean = false,
    val releaseApproved: Boolean = false,
    val productionLock: Boolean = false,
)

@Serializable
data class WorldSourceSet(
    val sourceId: String,
    val order: Int,
    val classification: String,
    val title: String,
    val rawPath: String,
    val originalAttachmentPath: String? = null,
    val sourceKind: String = "",
    val byteSize: Long,
    val sha256: String,
    val copyMode: String = "",
    val rawTextUnmodified: Boolean = true,
)

@Serializable
data class WorldSourceArchiveManifest(
    val manifestId: String,
    val status: String,
    val updatedAt: String = "",
    val locale: String = "",
    val canonicalWorkspace: String = "",
    val archiveRoot: String = "",
    val rawTextUnmodified: Boolean = false,
    val classificationOnly: Boolean = false,
    val contentRewriteAllowed: Boolean = false,
    val contentSummaryAllowed: Boolean = false,
    val formattingAllowedOutsideRawText: Boolean = false,
    val titleRelationship: WorldSourceTitleRelationship = WorldSourceTitleRelationship(),
    val sourceSets: List<WorldSourceSet> = emptyList(),
    val nextAllowedContentStep: String = "",
    val boundaryFlags: WorldSourceBoundaryFlags = WorldSourceBoundaryFlags(),
)

@Serializable
data class WorldSourceArchiveCheck(
    val ruleId: String,
    val status: String,
    val detail: String = "",
)

@Serializable
data class WorldSourceArchiveFinding(
    val ruleId: String,
    val severity: String,
    val message: String,
    val path: String = "",
)

@Serializable
data class WorldSourceArchiveEvidence(
    val sourceCount: Int = 0,
    val verifiedSourceCount: Int = 0,
    val totalByteSize: Long = 0,
    val hashMismatchCount: Int = 0,
    val missingRawFileCount: Int = 0,
    val unsafeBoundaryFlagCount: Int = 0,
    val projectLabel: String = "",
    val providedSourceTitle: String = "",
    val titleRelationshipStatus: String = "",
    val officialSourceData: Boolean = true,
    val sourceAuthorityStatus: String = "official_source",
)

@Serializable
data class WorldSourceArchiveValidationReport(
    val verdict: String,
    val status: String,
    val manifestId: String = "",
    val sourceManifestPath: String = "",
    val downstreamBoundary: String = "official_world_source_archive_validation_only_not_runtime_entity_conversion",
    val nextAllowedDownstreamStep: String = "",
    val evidence: WorldSourceArchiveEvidence = WorldSourceArchiveEvidence(),
    val checks: List<WorldSourceArchiveCheck> = emptyList(),
    val findings: List<WorldSourceArchiveFinding> = emptyList(),
    val findingCount: Int = findings.size,
    val progressCaveat: String = "local baseline 100% is not final product completion",
)

@Serializable
data class WorldSourceArchiveSqlImportResult(
    val verdict: String,
    val status: String,
    val archiveId: String = "",
    val sourceManifestPath: String = "",
    val databasePath: String = "",
    val downstreamBoundary: String = "official_world_source_sql_import_only_not_runtime_entity_conversion",
    val nextAllowedDownstreamStep: String = "query_world_source_or_transform_to_setting_pack_plan",
    val archiveRowsWritten: Int = 0,
    val documentRowsWritten: Int = 0,
    val chunkRowsWritten: Int = 0,
    val sourceCount: Int = 0,
    val totalByteSize: Long = 0,
    val validation: WorldSourceArchiveValidationReport,
    val boundaryFlags: WorldSourceBoundaryFlags = WorldSourceBoundaryFlags(),
    val sqlTables: List<String> = listOf("world_source_archives", "world_source_documents", "world_source_chunks"),
    val findingCount: Int = validation.findingCount,
)

@Serializable
data class WorldSourceDocumentImport(
    val source: WorldSourceSet,
    val rawText: String,
    val resolvedRawPath: String,
    val metadata: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class WorldSourceQueryHit(
    val chunkId: String,
    val archiveId: String,
    val sourceId: String,
    val classification: String,
    val title: String,
    val heading: String,
    val chunkOrder: Int,
    val snippet: String,
    val officialSourceData: Boolean,
    val sourceAuthorityStatus: String,
)

@Serializable
data class WorldSourceQueryReport(
    val verdict: String,
    val status: String,
    val query: String,
    val classification: String = "",
    val hitCount: Int,
    val hits: List<WorldSourceQueryHit>,
    val downstreamBoundary: String = "world_source_query_only_not_runtime_or_canon_mutation",
    val sqlTables: List<String> = listOf("world_source_chunks", "world_source_documents", "world_source_archives"),
)
