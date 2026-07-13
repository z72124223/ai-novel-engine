package com.localnovel.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class RebuildInput(
    val path: String,
    val inputType: String,
    val order: Int,
    val sha256: String,
    val byteSize: Long,
)

@Serializable
data class AuthorityClassificationRule(
    val pathPrefix: String,
    val primaryClass: String,
    val retentionStatus: String,
    val reason: String,
)

@Serializable
data class ProjectDataRebuildManifest(
    val schemaVersion: String,
    val manifestId: String,
    val status: String,
    val projectId: String,
    val migrationInputs: List<RebuildInput>,
    val settingPackInputs: List<RebuildInput>,
    val worldSourceInputs: List<RebuildInput>,
    val governedRoots: List<String>,
    val classificationRules: List<AuthorityClassificationRule>,
    val logicalTables: List<String>,
    val volatileColumns: List<String>,
    val volatileColumnsByTable: Map<String, List<String>> = emptyMap(),
    val requiredTrackedPaths: List<String>,
    val boundary: RebuildBoundary = RebuildBoundary(),
)

@Serializable
data class RebuildBoundary(
    val allowExistingDatabaseOverwrite: Boolean = false,
    val allowAbsoluteRepositoryInputs: Boolean = false,
    val allowCanonPromotion: Boolean = false,
    val allowRuntimeStateMutation: Boolean = false,
    val allowDeletion: Boolean = false,
    val allowProviderCalls: Boolean = false,
)

@Serializable
data class VerifiedRebuildInput(
    val path: String,
    val inputType: String,
    val order: Int,
    val sha256: String,
    val byteSize: Long,
    val workingTreeByteSize: Long = byteSize,
    val lineEndingNormalizationApplied: Boolean = false,
    val status: String,
)

@Serializable
data class AuthorityInventoryEntry(
    val path: String,
    val primaryClass: String,
    val retentionStatus: String,
    val byteSize: Long,
    val sha256: String,
    val matchedRulePrefix: String,
)

@Serializable
data class AuthorityClassSummary(
    val primaryClass: String,
    val fileCount: Int,
    val byteSize: Long,
)

@Serializable
data class RetentionStatusSummary(
    val retentionStatus: String,
    val fileCount: Int,
    val byteSize: Long,
)

@Serializable
data class AuthorityRouteSummary(
    val route: String,
    val fileCount: Int,
    val byteSize: Long,
)

@Serializable
data class DuplicateHashGroup(
    val sha256: String,
    val byteSize: Long,
    val paths: List<String>,
)

@Serializable
data class LogicalDuplicateGroup(
    val normalizedSha256: String,
    val paths: List<String>,
)

@Serializable
data class AuthorityInventoryReport(
    val trackedFileCount: Int,
    val trackedByteSize: Long,
    val classSummaries: List<AuthorityClassSummary>,
    val retentionSummaries: List<RetentionStatusSummary>,
    val routeSummaries: List<AuthorityRouteSummary>,
    val duplicateHashGroups: List<DuplicateHashGroup>,
    val logicalDuplicateGroups: List<LogicalDuplicateGroup>,
    val unclassifiedPaths: List<String>,
    val missingRequiredPaths: List<String>,
    val privateOrAbsolutePathFindings: List<String>,
    val historicalPrivateMetadataFindings: List<String>,
    val archiveCandidates: List<String>,
    val deleteCandidates: List<String>,
    val localDatabaseCandidates: List<String>,
    val entries: List<AuthorityInventoryEntry>,
)

@Serializable
data class LogicalTableDigest(
    val table: String,
    val rowCount: Int,
    val includedColumns: List<String>,
    val excludedColumns: List<String>,
    val sha256: String,
)

@Serializable
data class LogicalContentManifest(
    val schemaVersion: String,
    val migrationCount: Int,
    val tables: List<LogicalTableDigest>,
    val totalRowCount: Int,
    val rootSha256: String,
)

@Serializable
data class RebuildFinding(
    val ruleId: String,
    val severity: String,
    val message: String,
    val path: String = "",
)

@Serializable
data class ProjectDataRebuildReport(
    val verdict: String,
    val status: String,
    val manifestId: String,
    val projectId: String,
    val databaseCreated: Boolean,
    val databaseFileName: String,
    val verifiedInputs: List<VerifiedRebuildInput>,
    val settingPacksApplied: List<String>,
    val canonValid: Boolean,
    val worldSourceDocumentCount: Int,
    val worldSourceChunkCount: Int,
    val authorityInventory: AuthorityInventoryReport,
    val logicalContent: LogicalContentManifest,
    val findings: List<RebuildFinding>,
    val nextAllowedStep: String,
    val boundary: RebuildBoundary,
)
