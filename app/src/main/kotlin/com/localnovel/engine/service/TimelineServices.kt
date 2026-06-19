package com.localnovel.engine.service

import com.localnovel.engine.model.CheckpointSnapshot
import com.localnovel.engine.model.ForkSessionResult
import com.localnovel.engine.model.ReplayAuditIssue
import com.localnovel.engine.model.ReplayAuditReport
import com.localnovel.engine.model.SessionCheckpoint
import com.localnovel.engine.model.TimelineBranch
import com.localnovel.engine.model.TimelineExport
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import java.security.MessageDigest
import java.sql.Connection
import java.time.Instant

class CheckpointService(private val repository: SqliteStoryRepository) {
    fun createCheckpoint(
        sessionId: String,
        label: String,
        kind: String = "MANUAL",
        reason: String = "",
    ): SessionCheckpoint = repository.withTransaction { connection ->
        val session = sessionRow(connection, sessionId) ?: error("Session not found: $sessionId")
        val projectId = session.required("project_id")
        val branchId = ensureBranchForSession(connection, session)
        require(pendingContextCount(connection, sessionId) == 0) { "Session has pending contexts: $sessionId" }
        val checkpoint = createCheckpointInTransaction(
            connection = connection,
            session = session + ("branch_id" to branchId),
            label = label,
            kind = kind,
            reason = reason,
        )
        insertTimelineEvent(
            connection,
            projectId = projectId,
            branchId = branchId,
            sessionId = sessionId,
            eventType = "checkpoint_created",
            sourceCheckpointId = checkpoint.id,
            details = mapOf("kind" to kind, "label" to label),
        )
        checkpoint
    }

    fun listCheckpoints(sessionId: String): List<SessionCheckpoint> = repository.withConnection { connection ->
        checkpointRows(
            connection,
            "SELECT * FROM session_checkpoints WHERE session_id = ? ORDER BY state_version ASC, created_at ASC",
            sessionId,
        )
    }

    fun showCheckpoint(checkpointId: String): SessionCheckpoint = repository.withConnection { connection ->
        checkpointRows(connection, "SELECT * FROM session_checkpoints WHERE id = ?", checkpointId).singleOrNull()
            ?: error("Checkpoint not found: $checkpointId")
    }

    internal fun createCheckpointInTransaction(
        connection: Connection,
        session: Map<String, String?>,
        label: String,
        kind: String,
        reason: String,
    ): SessionCheckpoint {
        val sessionId = session.required("id")
        val projectId = session.required("project_id")
        val branchId = session["branch_id"]?.takeIf { it.isNotBlank() }
        val turnNumber = (session["next_turn_number"]?.toIntOrNull() ?: 1) - 1
        val stateVersion = session["state_version"]?.toIntOrNull() ?: 0
        val snapshot = buildSnapshot(connection, session + ("branch_id" to branchId), turnNumber, stateVersion)
        val snapshotJson = JsonSupport.json.encodeToString(snapshot)
        val snapshotHash = sha256(snapshotJson)
        val checkpointId = SqliteStoryRepository.newId("checkpoint")
        val timestamp = now()
        val sourceTurnLogId = latestAppliedTurnLogId(connection, sessionId, turnNumber)
        connection.prepareStatement(
            """
            INSERT INTO session_checkpoints(
                id, project_id, session_id, branch_id, turn_number, state_version,
                source_turn_log_id, kind, label, reason, snapshot_hash, snapshot_json, created_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, checkpointId)
            statement.setString(2, projectId)
            statement.setString(3, sessionId)
            statement.setString(4, branchId)
            statement.setInt(5, turnNumber)
            statement.setInt(6, stateVersion)
            statement.setString(7, sourceTurnLogId)
            statement.setString(8, kind)
            statement.setString(9, label)
            statement.setString(10, reason)
            statement.setString(11, snapshotHash)
            statement.setString(12, snapshotJson)
            statement.setString(13, timestamp)
            statement.executeUpdate()
        }
        return SessionCheckpoint(
            id = checkpointId,
            projectId = projectId,
            sessionId = sessionId,
            branchId = branchId,
            turnNumber = turnNumber,
            stateVersion = stateVersion,
            sourceTurnLogId = sourceTurnLogId,
            kind = kind,
            label = label,
            reason = reason,
            snapshotHash = snapshotHash,
            snapshot = snapshot,
            createdAt = timestamp,
        )
    }

    internal fun buildSnapshot(
        connection: Connection,
        session: Map<String, String?>,
        turnNumber: Int,
        stateVersion: Int,
    ): CheckpointSnapshot {
        val projectId = session.required("project_id")
        val sessionId = session.required("id")
        val branchId = session["branch_id"]?.takeIf { it.isNotBlank() }
        val tables = linkedMapOf<String, List<Map<String, String?>>>(
            "projects" to rows(connection, "SELECT * FROM projects WHERE id = ?", projectId),
            "story_sessions" to listOf(session.toSortedMap()),
            "character_states" to rows(connection, "SELECT * FROM character_states WHERE project_id = ? ORDER BY character_id", projectId),
            "items" to rows(connection, "SELECT * FROM items WHERE project_id = ? ORDER BY id", projectId),
            "facts" to rows(connection, "SELECT * FROM facts WHERE project_id = ? ORDER BY id", projectId),
            "secrets" to rows(connection, "SELECT * FROM secrets WHERE project_id = ? ORDER BY id", projectId),
            "events" to rows(connection, "SELECT * FROM events WHERE project_id = ? ORDER BY id", projectId),
            "task_queue" to rows(connection, "SELECT * FROM task_queue WHERE project_id = ? ORDER BY id", projectId),
            "character_memories" to rows(connection, "SELECT * FROM character_memories WHERE project_id = ? ORDER BY id", projectId),
            "character_knowledge" to rows(connection, "SELECT * FROM character_knowledge WHERE project_id = ? ORDER BY id", projectId),
            "scene_instances" to rows(connection, "SELECT * FROM scene_instances WHERE session_id = ? ORDER BY visit_number, id", sessionId),
            "session_scene_requirement_states" to rows(connection, "SELECT * FROM session_scene_requirement_states WHERE session_id = ? ORDER BY scene_instance_id, requirement_id", sessionId),
            "session_thread_states" to rows(connection, "SELECT * FROM session_thread_states WHERE session_id = ? ORDER BY thread_id", sessionId),
            "scheduled_world_events" to rows(
                connection,
                "SELECT * FROM scheduled_world_events WHERE project_id = ? AND (session_id IS NULL OR session_id = ?) ORDER BY id",
                projectId,
                sessionId,
            ),
        )
        return CheckpointSnapshot(
            projectId = projectId,
            sessionId = sessionId,
            branchId = branchId,
            turnNumber = turnNumber,
            stateVersion = stateVersion,
            worldTime = session["world_time"].orEmpty(),
            elapsedMinutes = session["elapsed_minutes"]?.toIntOrNull() ?: 0,
            activeSceneId = tables["projects"]?.firstOrNull()?.get("active_scene_id"),
            currentSceneInstanceId = session["current_scene_instance_id"],
            tables = tables,
        )
    }
}

class TimelineBranchService(private val repository: SqliteStoryRepository) {
    private val checkpointService = CheckpointService(repository)

    fun forkSession(checkpointId: String, label: String, reason: String = ""): ForkSessionResult =
        repository.withTransaction { connection ->
            val checkpointRow = rows(connection, "SELECT * FROM session_checkpoints WHERE id = ?", checkpointId).singleOrNull()
                ?: error("Checkpoint not found: $checkpointId")
            val checkpoint = checkpointRow.toCheckpoint(includeSnapshot = true)
            val snapshot = checkpoint.snapshot ?: error("Checkpoint snapshot missing: $checkpointId")
            require(pendingContextCount(connection, checkpoint.sessionId) == 0) { "Source session has pending contexts: ${checkpoint.sessionId}" }
            val projectId = checkpoint.projectId
            val branchId = SqliteStoryRepository.newId("branch")
            val sessionId = SqliteStoryRepository.newId("session")
            val timestamp = now()
            repository.executeUpdate(
                connection,
                "UPDATE timeline_branches SET status = 'INACTIVE', updated_at = ? WHERE project_id = ? AND status = 'ACTIVE'",
                timestamp,
                projectId,
            )
            insertBranch(
                connection,
                TimelineBranch(
                    id = branchId,
                    projectId = projectId,
                    label = label,
                    status = "ACTIVE",
                    parentBranchId = checkpoint.branchId,
                    sourceCheckpointId = checkpointId,
                    activeSessionId = null,
                    createdFromSessionId = checkpoint.sessionId,
                    createdFromTurnNumber = checkpoint.turnNumber,
                    createdFromStateVersion = checkpoint.stateVersion,
                    reason = reason,
                    createdAt = timestamp,
                    updatedAt = timestamp,
                ),
            )
            restoreSnapshotToSession(connection, snapshot, sessionId, branchId, createSession = true)
            repository.executeUpdate(connection, "UPDATE timeline_branches SET active_session_id = ?, updated_at = ? WHERE id = ?", sessionId, timestamp, branchId)
            val branchStartSession = sessionRow(connection, sessionId) ?: error("Forked session was not created: $sessionId")
            checkpointService.createCheckpointInTransaction(
                connection = connection,
                session = branchStartSession + ("branch_id" to branchId),
                label = "branch-start",
                kind = "BRANCH_START",
                reason = "forked from $checkpointId",
            )
            insertTimelineEvent(
                connection,
                projectId = projectId,
                branchId = branchId,
                sessionId = sessionId,
                eventType = "branch_forked",
                sourceCheckpointId = checkpointId,
                details = mapOf("label" to label, "reason" to reason),
            )
            ForkSessionResult(
                branchId = branchId,
                sessionId = sessionId,
                sourceCheckpointId = checkpointId,
                stateVersion = checkpoint.stateVersion,
                nextTurnNumber = checkpoint.turnNumber + 1,
            )
        }

    fun checkoutBranch(branchId: String): TimelineBranch = repository.withTransaction { connection ->
        val target = branchRows(connection, "SELECT * FROM timeline_branches WHERE id = ?", branchId).singleOrNull()
            ?: error("Branch not found: $branchId")
        val active = branchRows(
            connection,
            "SELECT * FROM timeline_branches WHERE project_id = ? AND status = 'ACTIVE' ORDER BY updated_at DESC LIMIT 1",
            target.projectId,
        ).firstOrNull()
        if (active != null && active.id != target.id) {
            active.activeSessionId?.let { activeSessionId ->
                require(pendingContextCount(connection, activeSessionId) == 0) { "Active branch session has pending contexts: $activeSessionId" }
                val session = sessionRow(connection, activeSessionId) ?: error("Active session not found: $activeSessionId")
                CheckpointService(repository).createCheckpointInTransaction(
                    connection = connection,
                    session = session + ("branch_id" to active.id),
                    label = "pre-checkout",
                    kind = "PRE_CHECKOUT",
                    reason = "checkout $branchId",
                )
            }
        }
        val latest = rows(
            connection,
            "SELECT * FROM session_checkpoints WHERE branch_id = ? ORDER BY state_version DESC, created_at DESC LIMIT 1",
            branchId,
        ).singleOrNull()?.toCheckpoint(includeSnapshot = true)
            ?: error("Branch has no checkpoint: $branchId")
        val targetSessionId = target.activeSessionId ?: latest.sessionId
        restoreSnapshotToSession(connection, latest.snapshot ?: error("Checkpoint snapshot missing: ${latest.id}"), targetSessionId, branchId, createSession = false)
        val timestamp = now()
        repository.executeUpdate(connection, "UPDATE timeline_branches SET status = 'INACTIVE', updated_at = ? WHERE project_id = ? AND status = 'ACTIVE'", timestamp, target.projectId)
        repository.executeUpdate(connection, "UPDATE timeline_branches SET status = 'ACTIVE', active_session_id = ?, updated_at = ? WHERE id = ?", targetSessionId, timestamp, branchId)
        insertTimelineEvent(
            connection,
            projectId = target.projectId,
            branchId = branchId,
            sessionId = targetSessionId,
            eventType = "branch_checked_out",
            sourceCheckpointId = latest.id,
            details = mapOf("label" to target.label),
        )
        branchRows(connection, "SELECT * FROM timeline_branches WHERE id = ?", branchId).single()
    }

    fun showTimeline(projectId: String): TimelineExport = repository.withConnection { connection ->
        val branches = branchRows(connection, "SELECT * FROM timeline_branches WHERE project_id = ? ORDER BY created_at ASC", projectId)
        val checkpoints = checkpointRows(
            connection,
            "SELECT * FROM session_checkpoints WHERE project_id = ? ORDER BY created_at ASC",
            projectId,
        )
        TimelineExport(
            projectId = projectId,
            activeBranchId = branches.firstOrNull { it.status == "ACTIVE" }?.id,
            branches = branches,
            checkpoints = checkpoints,
        )
    }

    fun exportTimeline(projectId: String): TimelineExport = showTimeline(projectId)
}

class ReplayAuditService(private val repository: SqliteStoryRepository) {
    fun verify(sessionId: String, record: Boolean = false): ReplayAuditReport = repository.withTransaction { connection ->
        val session = sessionRow(connection, sessionId) ?: error("Session not found: $sessionId")
        val projectId = session.required("project_id")
        val branchId = session["branch_id"]?.takeIf { it.isNotBlank() }
        val turns = rows(
            connection,
            "SELECT * FROM turn_logs WHERE session_id = ? AND validation_status = 'applied' ORDER BY turn_number ASC",
            sessionId,
        )
        val issues = mutableListOf<ReplayAuditIssue>()
        val seenTurns = mutableSetOf<String>()
        val seenResults = mutableSetOf<String>()
        var expectedBase: Int? = null
        turns.forEach { row ->
            val turnNumber = row["turn_number"]?.toIntOrNull()
            if (turnNumber != null && !seenTurns.add(turnNumber.toString())) {
                issues += ReplayAuditIssue("duplicate_turn", "Duplicate applied turn number.", turnNumber)
            }
            val resultId = row["result_id"].orEmpty()
            if (resultId.isBlank()) issues += ReplayAuditIssue("missing_result_id", "Applied turn missing result id.", turnNumber)
            if (resultId.isNotBlank() && !seenResults.add(resultId)) {
                issues += ReplayAuditIssue("duplicate_result_id", "Duplicate result id: $resultId", turnNumber)
            }
            val context = runCatching { JsonSupport.json.decodeFromString<com.localnovel.engine.model.ContextPacket>(row["context_json"].orEmpty()) }.getOrNull()
            val result = runCatching { JsonSupport.json.decodeFromString<com.localnovel.engine.model.AiTurnResult>(row["result_json"].orEmpty()) }.getOrNull()
            if (context == null || result == null) {
                issues += ReplayAuditIssue("invalid_json", "Applied turn has invalid context or result JSON.", turnNumber)
            } else {
                if (context.sessionId != sessionId || result.sessionId != sessionId) issues += ReplayAuditIssue("session_mismatch", "Context/result session mismatch.", turnNumber)
                if (context.contextId != row["context_id"] || result.contextId != row["context_id"]) issues += ReplayAuditIssue("context_mismatch", "Context/result context id mismatch.", turnNumber)
                if (context.contextHash != row["context_hash"] || result.contextHash != row["context_hash"]) issues += ReplayAuditIssue("hash_mismatch", "Context/result hash mismatch.", turnNumber)
                if (context.turnNumber.toString() != row["turn_number"] || result.turnNumber.toString() != row["turn_number"]) issues += ReplayAuditIssue("turn_mismatch", "Context/result turn mismatch.", turnNumber)
                if (result.baseStateVersion != context.baseStateVersion) issues += ReplayAuditIssue("state_version_mismatch", "Result baseStateVersion does not match context.", turnNumber)
                expectedBase?.let {
                    if (context.baseStateVersion != it) issues += ReplayAuditIssue("state_chain_broken", "Expected baseStateVersion $it but got ${context.baseStateVersion}.", turnNumber)
                }
                expectedBase = context.baseStateVersion + 1
            }
        }
        if (branchId != null) {
            val branch = rows(connection, "SELECT * FROM timeline_branches WHERE id = ?", branchId).singleOrNull()
            if (branch == null) {
                issues += ReplayAuditIssue("missing_branch", "Session branch does not exist.")
            } else if (!branch["source_checkpoint_id"].isNullOrBlank()) {
                val exists = rows(connection, "SELECT id FROM session_checkpoints WHERE id = ?", branch["source_checkpoint_id"]).isNotEmpty()
                if (!exists) issues += ReplayAuditIssue("missing_source_checkpoint", "Branch source checkpoint does not exist.")
            }
        }
        val report = ReplayAuditReport(
            projectId = projectId,
            sessionId = sessionId,
            branchId = branchId,
            status = if (issues.isEmpty()) "PASS" else "FAIL",
            checkedTurnCount = turns.size,
            issues = issues,
        )
        if (record) {
            connection.prepareStatement(
                """
                INSERT INTO replay_audit_runs(id, project_id, session_id, branch_id, status, checked_turn_count, report_json, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, SqliteStoryRepository.newId("replay-audit"))
                statement.setString(2, projectId)
                statement.setString(3, sessionId)
                statement.setString(4, branchId)
                statement.setString(5, report.status)
                statement.setInt(6, report.checkedTurnCount)
                statement.setString(7, JsonSupport.json.encodeToString(report))
                statement.setString(8, now())
                statement.executeUpdate()
            }
        }
        report
    }
}

private fun rows(connection: Connection, sql: String, vararg args: String?): List<Map<String, String?>> {
    return connection.prepareStatement(sql).use { statement ->
        args.forEachIndexed { index, value -> statement.setString(index + 1, value) }
        statement.executeQuery().use { resultSet ->
            val metadata = resultSet.metaData
            buildList {
                while (resultSet.next()) {
                    val row = (1..metadata.columnCount).associate { column ->
                        metadata.getColumnName(column) to resultSet.getString(column)
                    }.toSortedMap()
                    add(row)
                }
            }
        }
    }
}

private fun sessionRow(connection: Connection, sessionId: String): Map<String, String?>? =
    rows(
        connection,
        """
        SELECT id, project_id, current_scene_id, next_turn_number, state_version, status,
               current_scene_instance_id, world_time, elapsed_minutes, branch_id,
               created_at, updated_at
        FROM story_sessions
        WHERE id = ?
        """.trimIndent(),
        sessionId,
    ).singleOrNull()

private fun ensureBranchForSession(connection: Connection, session: Map<String, String?>): String {
    val existing = session["branch_id"]?.takeIf { it.isNotBlank() }
    if (existing != null) return existing
    val branchId = SqliteStoryRepository.newId("branch")
    val projectId = session.required("project_id")
    val sessionId = session.required("id")
    val timestamp = now()
    connection.prepareStatement("UPDATE timeline_branches SET status = 'INACTIVE', updated_at = ? WHERE project_id = ? AND status = 'ACTIVE'").use {
        it.setString(1, timestamp)
        it.setString(2, projectId)
        it.executeUpdate()
    }
    insertBranch(
        connection,
        TimelineBranch(
            id = branchId,
            projectId = projectId,
            label = "adopted-main",
            status = "ACTIVE",
            activeSessionId = sessionId,
            createdFromSessionId = sessionId,
            reason = "adopt legacy session",
            createdAt = timestamp,
            updatedAt = timestamp,
        ),
    )
    connection.prepareStatement("UPDATE story_sessions SET branch_id = ?, updated_at = ? WHERE id = ?").use {
        it.setString(1, branchId)
        it.setString(2, timestamp)
        it.setString(3, sessionId)
        it.executeUpdate()
    }
    connection.prepareStatement("UPDATE turn_logs SET branch_id = ? WHERE session_id = ? AND (branch_id IS NULL OR branch_id = '')").use {
        it.setString(1, branchId)
        it.setString(2, sessionId)
        it.executeUpdate()
    }
    return branchId
}

private fun insertBranch(connection: Connection, branch: TimelineBranch) {
    connection.prepareStatement(
        """
        INSERT OR REPLACE INTO timeline_branches(
            id, project_id, label, status, parent_branch_id, source_checkpoint_id,
            active_session_id, created_from_session_id, created_from_turn_number,
            created_from_state_version, reason, metadata_json, created_at, updated_at
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent(),
    ).use { statement ->
        val timestamp = now()
        statement.setString(1, branch.id)
        statement.setString(2, branch.projectId)
        statement.setString(3, branch.label)
        statement.setString(4, branch.status)
        statement.setString(5, branch.parentBranchId)
        statement.setString(6, branch.sourceCheckpointId)
        statement.setString(7, branch.activeSessionId)
        statement.setString(8, branch.createdFromSessionId)
        statement.setInt(9, branch.createdFromTurnNumber)
        statement.setInt(10, branch.createdFromStateVersion)
        statement.setString(11, branch.reason)
        statement.setString(12, JsonSupport.json.encodeToString(branch.metadata))
        statement.setString(13, branch.createdAt.ifBlank { timestamp })
        statement.setString(14, branch.updatedAt.ifBlank { timestamp })
        statement.executeUpdate()
    }
}

private fun restoreSnapshotToSession(
    connection: Connection,
    snapshot: CheckpointSnapshot,
    targetSessionId: String,
    branchId: String,
    createSession: Boolean,
) {
    val projectId = snapshot.projectId
    val timestamp = now()
    val sceneIdMap = mutableMapOf<String, String>()
    val sourceSession = snapshot.tables["story_sessions"].orEmpty().firstOrNull()
        ?: error("Snapshot missing story session row.")
    val sourceCurrentSceneInstance = sourceSession["current_scene_instance_id"]
    snapshot.tables["scene_instances"].orEmpty().forEach { row ->
        val oldId = row.required("id")
        sceneIdMap[oldId] = SqliteStoryRepository.newId("scene-instance")
    }
    val currentSceneInstanceId = sourceCurrentSceneInstance?.let { sceneIdMap[it] } ?: SqliteStoryRepository.newId("scene-instance")

    restoreProjectRow(connection, snapshot)
    replaceProjectRows(connection, "character_states", projectId, snapshot.tables["character_states"].orEmpty())
    replaceProjectRows(connection, "items", projectId, snapshot.tables["items"].orEmpty())
    replaceProjectRows(connection, "facts", projectId, snapshot.tables["facts"].orEmpty())
    replaceProjectRows(connection, "secrets", projectId, snapshot.tables["secrets"].orEmpty())
    replaceProjectRows(connection, "events", projectId, snapshot.tables["events"].orEmpty())
    replaceProjectRows(connection, "task_queue", projectId, snapshot.tables["task_queue"].orEmpty())
    replaceProjectRows(connection, "character_knowledge", projectId, snapshot.tables["character_knowledge"].orEmpty())
    snapshot.tables["character_memories"].orEmpty().forEach { insertRaw(connection, "character_memories", it) }

    if (createSession) {
        val sessionRow = sourceSession.toMutableMap()
        sessionRow["id"] = targetSessionId
        sessionRow["branch_id"] = branchId
        sessionRow["status"] = "active"
        sessionRow["current_scene_instance_id"] = currentSceneInstanceId
        sessionRow["created_at"] = timestamp
        sessionRow["updated_at"] = timestamp
        upsertRaw(connection, "story_sessions", sessionRow, keyColumns = listOf("id"))
    } else {
        val sessionRow = sourceSession.toMutableMap()
        sessionRow["id"] = targetSessionId
        sessionRow["branch_id"] = branchId
        sessionRow["status"] = "active"
        sessionRow["current_scene_instance_id"] = currentSceneInstanceId
        sessionRow["updated_at"] = timestamp
        upsertRaw(connection, "story_sessions", sessionRow, keyColumns = listOf("id"))
    }

    connection.prepareStatement("DELETE FROM scene_instances WHERE session_id = ?").use {
        it.setString(1, targetSessionId)
        it.executeUpdate()
    }
    snapshot.tables["scene_instances"].orEmpty().forEach { original ->
        val row = original.toMutableMap()
        row["id"] = sceneIdMap[original.required("id")]
        row["session_id"] = targetSessionId
        insertRaw(connection, "scene_instances", row)
    }
    connection.prepareStatement("DELETE FROM session_scene_requirement_states WHERE session_id = ?").use {
        it.setString(1, targetSessionId)
        it.executeUpdate()
    }
    snapshot.tables["session_scene_requirement_states"].orEmpty().forEach { original ->
        val row = original.toMutableMap()
        row["session_id"] = targetSessionId
        row["scene_instance_id"] = original["scene_instance_id"]?.let { sceneIdMap[it] }
        insertRaw(connection, "session_scene_requirement_states", row)
    }
    connection.prepareStatement("DELETE FROM session_thread_states WHERE session_id = ?").use {
        it.setString(1, targetSessionId)
        it.executeUpdate()
    }
    snapshot.tables["session_thread_states"].orEmpty().forEach { original ->
        val row = original.toMutableMap()
        row["session_id"] = targetSessionId
        insertRaw(connection, "session_thread_states", row)
    }
    connection.prepareStatement("DELETE FROM scheduled_world_events WHERE project_id = ?").use {
        it.setString(1, projectId)
        it.executeUpdate()
    }
    snapshot.tables["scheduled_world_events"].orEmpty().forEach { original ->
        val row = original.toMutableMap()
        if (!row["session_id"].isNullOrBlank()) {
            row["id"] = SqliteStoryRepository.newId("world-event")
            row["session_id"] = targetSessionId
        }
        insertRaw(connection, "scheduled_world_events", row)
    }
}

private fun restoreProjectRow(connection: Connection, snapshot: CheckpointSnapshot) {
    val row = snapshot.tables["projects"].orEmpty().firstOrNull() ?: return
    upsertRaw(connection, "projects", row, keyColumns = listOf("id"))
}

private fun replaceProjectRows(connection: Connection, table: String, projectId: String, rows: List<Map<String, String?>>) {
    connection.prepareStatement("DELETE FROM $table WHERE project_id = ?").use {
        it.setString(1, projectId)
        it.executeUpdate()
    }
    rows.forEach { insertRaw(connection, table, it) }
}

private fun insertRaw(connection: Connection, table: String, row: Map<String, String?>) {
    require(table.matches(Regex("[a-zA-Z_]+"))) { "Unsafe table name: $table" }
    val columns = row.keys.sorted()
    require(columns.all { it.matches(Regex("[a-zA-Z_]+")) }) { "Unsafe column in $table" }
    val placeholders = columns.joinToString(", ") { "?" }
    val sql = "INSERT OR REPLACE INTO $table(${columns.joinToString(", ")}) VALUES ($placeholders)"
    connection.prepareStatement(sql).use { statement ->
        columns.forEachIndexed { index, column ->
            val value = row[column]
            if (value == null) {
                statement.setNull(index + 1, java.sql.Types.VARCHAR)
            } else {
                statement.setString(index + 1, value)
            }
        }
        statement.executeUpdate()
    }
}

private fun upsertRaw(connection: Connection, table: String, row: Map<String, String?>, keyColumns: List<String>) {
    require(table.matches(Regex("[a-zA-Z_]+"))) { "Unsafe table name: $table" }
    val columns = row.keys.sorted()
    require(columns.all { it.matches(Regex("[a-zA-Z_]+")) }) { "Unsafe column in $table" }
    require(keyColumns.isNotEmpty() && keyColumns.all { it in columns }) { "Invalid key columns for $table" }
    val placeholders = columns.joinToString(", ") { "?" }
    val conflictTarget = keyColumns.joinToString(", ")
    val updateColumns = columns.filterNot { it in keyColumns }
    val conflictClause = if (updateColumns.isEmpty()) {
        "DO NOTHING"
    } else {
        "DO UPDATE SET " + updateColumns.joinToString(", ") { "$it = excluded.$it" }
    }
    val sql = "INSERT INTO $table(${columns.joinToString(", ")}) VALUES ($placeholders) ON CONFLICT($conflictTarget) $conflictClause"
    connection.prepareStatement(sql).use { statement ->
        columns.forEachIndexed { index, column ->
            val value = row[column]
            if (value == null) {
                statement.setNull(index + 1, java.sql.Types.VARCHAR)
            } else {
                statement.setString(index + 1, value)
            }
        }
        statement.executeUpdate()
    }
}

private fun checkpointRows(connection: Connection, sql: String, vararg args: String?): List<SessionCheckpoint> =
    rows(connection, sql, *args).map { it.toCheckpoint(includeSnapshot = false) }

private fun branchRows(connection: Connection, sql: String, vararg args: String?): List<TimelineBranch> =
    rows(connection, sql, *args).map { it.toBranch() }

private fun Map<String, String?>.toCheckpoint(includeSnapshot: Boolean): SessionCheckpoint {
    val snapshot = if (includeSnapshot) JsonSupport.json.decodeFromString<CheckpointSnapshot>(required("snapshot_json")) else null
    return SessionCheckpoint(
        id = required("id"),
        projectId = required("project_id"),
        sessionId = required("session_id"),
        branchId = this["branch_id"]?.takeIf { it.isNotBlank() },
        turnNumber = required("turn_number").toInt(),
        stateVersion = required("state_version").toInt(),
        sourceTurnLogId = this["source_turn_log_id"]?.takeIf { it.isNotBlank() },
        kind = required("kind"),
        label = this["label"].orEmpty(),
        reason = this["reason"].orEmpty(),
        snapshotHash = required("snapshot_hash"),
        snapshot = snapshot,
        createdAt = this["created_at"].orEmpty(),
    )
}

private fun Map<String, String?>.toBranch(): TimelineBranch =
    TimelineBranch(
        id = required("id"),
        projectId = required("project_id"),
        label = this["label"].orEmpty(),
        status = this["status"].orEmpty(),
        parentBranchId = this["parent_branch_id"]?.takeIf { it.isNotBlank() },
        sourceCheckpointId = this["source_checkpoint_id"]?.takeIf { it.isNotBlank() },
        activeSessionId = this["active_session_id"]?.takeIf { it.isNotBlank() },
        createdFromSessionId = this["created_from_session_id"]?.takeIf { it.isNotBlank() },
        createdFromTurnNumber = this["created_from_turn_number"]?.toIntOrNull() ?: 0,
        createdFromStateVersion = this["created_from_state_version"]?.toIntOrNull() ?: 0,
        reason = this["reason"].orEmpty(),
        metadata = runCatching { JsonSupport.json.decodeFromString<JsonObject>(this["metadata_json"].orEmpty()) }.getOrDefault(JsonObject(emptyMap())),
        createdAt = this["created_at"].orEmpty(),
        updatedAt = this["updated_at"].orEmpty(),
    )

private fun insertTimelineEvent(
    connection: Connection,
    projectId: String,
    branchId: String?,
    sessionId: String?,
    eventType: String,
    sourceCheckpointId: String?,
    details: Map<String, String>,
) {
    connection.prepareStatement(
        """
        INSERT INTO timeline_events(id, project_id, branch_id, session_id, event_type, source_checkpoint_id, details_json, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent(),
    ).use { statement ->
        statement.setString(1, SqliteStoryRepository.newId("timeline-event"))
        statement.setString(2, projectId)
        statement.setString(3, branchId)
        statement.setString(4, sessionId)
        statement.setString(5, eventType)
        statement.setString(6, sourceCheckpointId)
        statement.setString(7, JsonSupport.json.encodeToString(details))
        statement.setString(8, now())
        statement.executeUpdate()
    }
}

private fun latestAppliedTurnLogId(connection: Connection, sessionId: String, turnNumber: Int): String? =
    rows(
        connection,
        """
        SELECT id
        FROM turn_logs
        WHERE session_id = ? AND validation_status = 'applied' AND turn_number <= ?
        ORDER BY turn_number DESC
        LIMIT 1
        """.trimIndent(),
        sessionId,
        turnNumber.toString(),
    ).firstOrNull()?.get("id")

private fun pendingContextCount(connection: Connection, sessionId: String): Int =
    rows(connection, "SELECT COUNT(*) AS count FROM turn_logs WHERE session_id = ? AND validation_status = 'pending'", sessionId)
        .firstOrNull()?.get("count")?.toIntOrNull() ?: 0

private fun Map<String, String?>.required(key: String): String =
    this[key] ?: error("Missing required row field: $key")

private fun sha256(text: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(text.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

private fun now(): String = Instant.now().toString()
