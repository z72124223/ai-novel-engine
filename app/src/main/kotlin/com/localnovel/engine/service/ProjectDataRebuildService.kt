package com.localnovel.engine.service

import com.localnovel.engine.model.AuthorityClassSummary
import com.localnovel.engine.model.AuthorityInventoryEntry
import com.localnovel.engine.model.AuthorityInventoryReport
import com.localnovel.engine.model.AuthorityRouteSummary
import com.localnovel.engine.model.DuplicateHashGroup
import com.localnovel.engine.model.LogicalContentManifest
import com.localnovel.engine.model.LogicalDuplicateGroup
import com.localnovel.engine.model.LogicalTableDigest
import com.localnovel.engine.model.ProjectDataRebuildManifest
import com.localnovel.engine.model.ProjectDataRebuildReport
import com.localnovel.engine.model.RebuildFinding
import com.localnovel.engine.model.RebuildInput
import com.localnovel.engine.model.RetentionStatusSummary
import com.localnovel.engine.model.VerifiedRebuildInput
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.sql.DriverManager
import kotlinx.serialization.decodeFromString

class ProjectDataRebuildService {
    fun rebuild(
        repositoryRoot: Path,
        manifestPath: Path,
        databasePath: Path,
    ): ProjectDataRebuildReport {
        val root = repositoryRoot.toAbsolutePath().normalize()
        require(Files.isDirectory(root)) { "Repository root does not exist: $root" }
        val resolvedManifest = resolveRepositoryPath(root, manifestPath.toString(), allowAbsolute = true)
        val manifest = JsonSupport.json.decodeFromString<ProjectDataRebuildManifest>(
            Files.readString(resolvedManifest).removePrefix("\uFEFF"),
        )
        validateManifestBoundaries(manifest)
        require(!Files.exists(databasePath)) {
            "Refusing to overwrite an existing database: ${databasePath.fileName}"
        }

        val findings = mutableListOf<RebuildFinding>()
        val inputs = (manifest.migrationInputs + manifest.settingPackInputs + manifest.worldSourceInputs)
            .sortedWith(compareBy<RebuildInput> { it.inputType }.thenBy { it.order }.thenBy { it.path })
        val verifiedInputs = inputs.map { verifyInput(root, it, manifest, findings) }
        require(findings.none { it.severity == "error" }) {
            findings.joinToString("\n") { "${it.ruleId}: ${it.message}" }
        }

        val repository = SqliteStoryRepository(databasePath)
        repository.initialize()
        val settingPacksApplied = manifest.settingPackInputs.sortedBy { it.order }.map { input ->
            val path = resolveRepositoryPath(root, input.path)
            val pack = SettingPackParser().parse(path)
            val result = SettingPackImportService(repository).apply(pack, input.path)
            "${result.packId}@${result.packVersion}:${result.canonicalHash}"
        }
        val worldResult = manifest.worldSourceInputs.sortedBy { it.order }.fold<RebuildInput, IntArray>(IntArray(2)) { counts, input ->
            val result = WorldSourceArchiveImportService(repository).applyManifestFile(
                manifestPath = resolveRepositoryPath(root, input.path),
                databasePath = databasePath,
            )
            require(result.verdict == "PASS") { "World Source import failed for ${input.path}." }
            counts[0] += result.documentRowsWritten
            counts[1] += result.chunkRowsWritten
            counts
        }
        val canon = CanonConsistencyValidator(repository).validate(manifest.projectId)
        if (!canon.isValid) {
            canon.issues.forEach {
                findings += RebuildFinding("CANON_${it.code}", it.severity, it.message, it.path)
            }
        }

        val inventory = buildAuthorityInventory(root, manifest)
        inventory.unclassifiedPaths.forEach {
            findings += RebuildFinding("AUTHORITY_UNCLASSIFIED", "error", "Tracked governed file has no classification.", it)
        }
        inventory.missingRequiredPaths.forEach {
            findings += RebuildFinding("REQUIRED_PATH_MISSING", "error", "Required tracked path is missing.", it)
        }
        inventory.privateOrAbsolutePathFindings.forEach {
            findings += RebuildFinding("PRIVATE_OR_ABSOLUTE_PATH", "error", "Repository input is not machine-portable.", it)
        }
        val logicalContent = buildLogicalContentManifest(databasePath, manifest)
        val pass = canon.isValid && findings.none { it.severity == "error" }
        return ProjectDataRebuildReport(
            verdict = if (pass) "PASS" else "FAIL",
            status = if (pass) "PROJECT_DATA_REBUILD_PASS" else "PROJECT_DATA_REBUILD_FAIL",
            manifestId = manifest.manifestId,
            projectId = manifest.projectId,
            databaseCreated = Files.exists(databasePath),
            databaseFileName = databasePath.fileName.toString(),
            verifiedInputs = verifiedInputs,
            settingPacksApplied = settingPacksApplied,
            canonValid = canon.isValid,
            worldSourceDocumentCount = worldResult[0],
            worldSourceChunkCount = worldResult[1],
            authorityInventory = inventory,
            logicalContent = logicalContent,
            findings = findings.sortedWith(compareBy<RebuildFinding> { it.severity }.thenBy { it.ruleId }.thenBy { it.path }),
            nextAllowedStep = if (pass) "RETRIEVAL_00_REVIEW" else "RETRIEVAL_00_REPAIR",
            boundary = manifest.boundary,
        )
    }

    private fun validateManifestBoundaries(manifest: ProjectDataRebuildManifest) {
        require(manifest.schemaVersion == "1.0") { "Unsupported rebuild manifest schemaVersion: ${manifest.schemaVersion}" }
        require(manifest.status == "RETRIEVAL_00_REBUILD_READY") { "Manifest is not ready for rebuild." }
        require(manifest.migrationInputs.isNotEmpty()) { "At least one migration input is required." }
        require(manifest.settingPackInputs.isNotEmpty()) { "At least one Setting Pack input is required." }
        require(manifest.worldSourceInputs.isNotEmpty()) { "At least one World Source input is required." }
        require(!manifest.boundary.allowExistingDatabaseOverwrite)
        require(!manifest.boundary.allowAbsoluteRepositoryInputs)
        require(!manifest.boundary.allowCanonPromotion)
        require(!manifest.boundary.allowRuntimeStateMutation)
        require(!manifest.boundary.allowDeletion)
        require(!manifest.boundary.allowProviderCalls)
    }

    private fun verifyInput(
        root: Path,
        input: RebuildInput,
        manifest: ProjectDataRebuildManifest,
        findings: MutableList<RebuildFinding>,
    ): VerifiedRebuildInput {
        val path = runCatching { resolveRepositoryPath(root, input.path, manifest.boundary.allowAbsoluteRepositoryInputs) }
            .getOrElse { error ->
                findings += RebuildFinding("UNSAFE_INPUT_PATH", "error", error.message.orEmpty(), input.path)
                return VerifiedRebuildInput(input.path, input.inputType, input.order, input.sha256, input.byteSize, "FAIL")
            }
        if (!Files.isRegularFile(path)) {
            findings += RebuildFinding("INPUT_MISSING", "error", "Input file is missing.", input.path)
            return VerifiedRebuildInput(input.path, input.inputType, input.order, input.sha256, input.byteSize, "FAIL")
        }
        val actualBytes = Files.size(path)
        val actualHash = sha256(path)
        if (actualBytes != input.byteSize) {
            findings += RebuildFinding("INPUT_SIZE_MISMATCH", "error", "Expected ${input.byteSize}, got $actualBytes.", input.path)
        }
        if (!actualHash.equals(input.sha256, ignoreCase = true)) {
            findings += RebuildFinding("INPUT_HASH_MISMATCH", "error", "Expected ${input.sha256}, got $actualHash.", input.path)
        }
        return VerifiedRebuildInput(
            path = input.path,
            inputType = input.inputType,
            order = input.order,
            sha256 = actualHash,
            byteSize = actualBytes,
            status = if (actualBytes == input.byteSize && actualHash.equals(input.sha256, true)) "PASS" else "FAIL",
        )
    }

    private fun buildAuthorityInventory(root: Path, manifest: ProjectDataRebuildManifest): AuthorityInventoryReport {
        val tracked = gitTrackedFiles(root, manifest.governedRoots)
        val entries = tracked.map { relative ->
            val path = resolveRepositoryPath(root, relative)
            val rule = manifest.classificationRules
                .filter { relative == it.pathPrefix.trimEnd('/') || relative.startsWith("${it.pathPrefix.trimEnd('/')}/") }
                .maxByOrNull { it.pathPrefix.length }
            AuthorityInventoryEntry(
                path = relative,
                primaryClass = rule?.primaryClass ?: "UNCLASSIFIED",
                retentionStatus = rule?.retentionStatus ?: "UNKNOWN_REQUIRES_REVIEW",
                byteSize = Files.size(path),
                sha256 = sha256(path),
                matchedRulePrefix = rule?.pathPrefix.orEmpty(),
            )
        }.sortedBy { it.path }
        val duplicates = entries.groupBy { it.sha256 }
            .values.filter { it.size > 1 }
            .map { group -> DuplicateHashGroup(group.first().sha256, group.first().byteSize, group.map { it.path }.sorted()) }
            .sortedBy { it.paths.first() }
        val logicalDuplicates = entries.asSequence()
            .filter { it.path.endsWith(".md") || it.path.endsWith(".txt") || it.path.endsWith(".json") }
            .mapNotNull { entry ->
                val path = resolveRepositoryPath(root, entry.path)
                runCatching {
                    val normalized = Files.readString(path).replace("\r\n", "\n")
                        .lineSequence().joinToString("\n") { it.trimEnd() }.trim()
                    entry.path to sha256(normalized.toByteArray(Charsets.UTF_8))
                }.getOrNull()
            }.groupBy({ it.second }, { it.first })
            .filterValues { it.size > 1 }
            .map { (hash, paths) -> LogicalDuplicateGroup(hash, paths.sorted()) }
            .sortedBy { it.paths.first() }
        val trackedRequiredPaths = gitTrackedFiles(root, manifest.requiredTrackedPaths).toSet()
        val missing = manifest.requiredTrackedPaths.filter {
            !Files.isRegularFile(runCatching { resolveRepositoryPath(root, it) }.getOrDefault(root.resolve("__unsafe__"))) ||
                it !in trackedRequiredPaths
        }
        val unsafe = (manifest.migrationInputs + manifest.settingPackInputs + manifest.worldSourceInputs)
            .map { it.path }
            .filter { isAbsoluteOrEscaping(it) }
        val historicalPrivateMetadata = entries.asSequence()
            .filter { it.path.endsWith(".json") || it.path.endsWith(".md") || it.path.endsWith(".txt") }
            .flatMap { entry ->
                val text = runCatching { Files.readString(resolveRepositoryPath(root, entry.path)) }.getOrDefault("")
                Regex("(?i)(?:[A-Z]:\\\\(?:Users|Documents and Settings)\\\\[^\\\"\\r\\n]+|/[Hh]ome/[^/\\\"\\r\\n]+)")
                    .findAll(text).map { "${entry.path}:contains-private-absolute-path" }
            }.distinct().sorted().toList()
        val localDatabases = listOf(root.resolve("data"), root.resolve("app/data"))
            .filter(Files::isDirectory)
            .flatMap { directory ->
                Files.walk(directory).use { stream ->
                    stream.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".db", true) }
                        .map { root.relativize(it.toAbsolutePath().normalize()).toString().replace('\\', '/') }
                        .toList()
                }
            }.sorted()
        return AuthorityInventoryReport(
            trackedFileCount = entries.size,
            trackedByteSize = entries.sumOf { it.byteSize },
            classSummaries = entries.groupBy { it.primaryClass }.map { (name, values) ->
                AuthorityClassSummary(name, values.size, values.sumOf { it.byteSize })
            }.sortedBy { it.primaryClass },
            retentionSummaries = entries.groupBy { it.retentionStatus }.map { (name, values) ->
                RetentionStatusSummary(name, values.size, values.sumOf { it.byteSize })
            }.sortedBy { it.retentionStatus },
            routeSummaries = entries.groupBy { it.path.substringBefore('/') }.map { (route, values) ->
                AuthorityRouteSummary(route, values.size, values.sumOf { it.byteSize })
            }.sortedBy { it.route },
            duplicateHashGroups = duplicates,
            logicalDuplicateGroups = logicalDuplicates,
            unclassifiedPaths = entries.filter { it.primaryClass == "UNCLASSIFIED" }.map { it.path },
            missingRequiredPaths = missing,
            privateOrAbsolutePathFindings = unsafe,
            historicalPrivateMetadataFindings = historicalPrivateMetadata,
            archiveCandidates = entries.filter { it.retentionStatus == "ARCHIVE_CANDIDATE" }.map { it.path },
            deleteCandidates = entries.filter { it.retentionStatus == "DELETE_CANDIDATE" }.map { it.path },
            localDatabaseCandidates = localDatabases,
            entries = entries,
        )
    }

    private fun buildLogicalContentManifest(databasePath: Path, manifest: ProjectDataRebuildManifest): LogicalContentManifest {
        val digests = DriverManager.getConnection("jdbc:sqlite:$databasePath").use { connection ->
            manifest.logicalTables.sorted().map { table ->
                require(table.matches(Regex("[a-zA-Z_]+"))) { "Unsafe logical table name: $table" }
                connection.prepareStatement("SELECT * FROM $table").use { statement ->
                    statement.executeQuery().use { rows ->
                        val meta = rows.metaData
                        val allColumns = (1..meta.columnCount).map(meta::getColumnName)
                        val excludedNames = (manifest.volatileColumns + manifest.volatileColumnsByTable[table].orEmpty())
                            .map(String::lowercase).toSet()
                        val excluded = allColumns.filter { it.lowercase() in excludedNames }
                        val included = allColumns.filterNot(excluded::contains).sorted()
                        val rowValues = mutableListOf<String>()
                        while (rows.next()) {
                            rowValues += included.joinToString("|") { column ->
                                val value = rows.getObject(column)
                                val text = when (value) {
                                    null -> "<NULL>"
                                    is ByteArray -> value.joinToString("") { "%02x".format(it) }
                                    else -> value.toString().replace("\r\n", "\n")
                                }
                                "$column:${text.toByteArray(Charsets.UTF_8).size}:$text"
                            }
                        }
                        val canonical = rowValues.sorted().joinToString("\n")
                        LogicalTableDigest(table, rowValues.size, included, excluded.sorted(), sha256(canonical.toByteArray(Charsets.UTF_8)))
                    }
                }
            }
        }
        val rootMaterial = digests.joinToString("\n") { "${it.table}|${it.rowCount}|${it.sha256}" }
        return LogicalContentManifest(
            schemaVersion = manifest.schemaVersion,
            migrationCount = manifest.migrationInputs.size,
            tables = digests,
            totalRowCount = digests.sumOf { it.rowCount },
            rootSha256 = sha256(rootMaterial.toByteArray(Charsets.UTF_8)),
        )
    }

    private fun gitTrackedFiles(root: Path, governedRoots: List<String>): List<String> {
        val command = mutableListOf("git", "-C", root.toString(), "ls-files", "-z", "--")
        command += governedRoots
        val process = ProcessBuilder(command).redirectErrorStream(true).start()
        val bytes = process.inputStream.readAllBytes()
        check(process.waitFor() == 0) { "git ls-files failed: ${bytes.toString(Charsets.UTF_8)}" }
        return bytes.toString(Charsets.UTF_8).split('\u0000').filter(String::isNotBlank).map { it.replace('\\', '/') }.sorted()
    }

    private fun resolveRepositoryPath(root: Path, pathText: String, allowAbsolute: Boolean = false): Path {
        val candidate = Path.of(pathText)
        require(allowAbsolute || !candidate.isAbsolute) { "Absolute repository input is forbidden: $pathText" }
        val resolved = (if (candidate.isAbsolute) candidate else root.resolve(candidate)).toAbsolutePath().normalize()
        require(resolved.startsWith(root)) { "Repository input escapes the root: $pathText" }
        return resolved
    }

    private fun isAbsoluteOrEscaping(pathText: String): Boolean = runCatching {
        val path = Path.of(pathText)
        path.isAbsolute || path.normalize().startsWith("..")
    }.getOrDefault(true)

    private fun sha256(path: Path): String = sha256(Files.readAllBytes(path))

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes).joinToString("") { "%02x".format(it) }
}
