package com.localnovel.engine.repository

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.ApplyResult
import com.localnovel.engine.model.CharacterMemory
import com.localnovel.engine.model.CharacterProfile
import com.localnovel.engine.model.CharacterState
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DemoProjectSeed
import com.localnovel.engine.model.DependencyEdge
import com.localnovel.engine.model.Event
import com.localnovel.engine.model.Fact
import com.localnovel.engine.model.Faction
import com.localnovel.engine.model.Item
import com.localnovel.engine.model.Location
import com.localnovel.engine.model.Project
import com.localnovel.engine.model.Scene
import com.localnovel.engine.model.SceneContract
import com.localnovel.engine.model.Secret
import com.localnovel.engine.model.StorySession
import com.localnovel.engine.model.Task
import com.localnovel.engine.model.Technique
import com.localnovel.engine.model.WorldRule
import com.localnovel.engine.model.allDeltaOperations
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.time.Instant
import java.util.UUID

class SqliteStoryRepository(private val dbPath: Path) : StoryRepository {
    override fun initialize() {
        dbPath.parent?.let { Files.createDirectories(it) }
        connect { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("PRAGMA foreign_keys = ON")
                listOf("001_init.sql", "002_multi_turn_runtime.sql").forEach { fileName ->
                    val migration = javaClass.classLoader.getResource("migrations/$fileName")
                        ?: error("Missing migration resource: migrations/$fileName")
                    migration.readText().splitToStatements().forEach { sql ->
                        executeMigrationStatement(statement, sql)
                    }
                }
            }
        }
    }

    override fun seedDemo(seed: DemoProjectSeed) {
        connect { connection ->
            connection.autoCommit = false
            try {
                clearProject(connection, seed.project.id)
                insertProject(connection, seed.project)
                seed.locations.forEach { insertLocation(connection, it) }
                seed.factions.forEach { insertFaction(connection, it) }
                seed.worldRules.forEach { insertWorldRule(connection, it) }
                seed.characters.forEach { insertCharacter(connection, it) }
                seed.characterStates.forEach { insertCharacterState(connection, it) }
                seed.characterMemories.forEach { insertCharacterMemory(connection, it) }
                seed.items.forEach { insertItem(connection, it) }
                seed.techniques.forEach { insertTechnique(connection, it) }
                seed.scenes.forEach { insertScene(connection, it) }
                seed.sceneContracts.forEach { insertSceneContract(connection, it) }
                seed.facts.forEach { insertFact(connection, it) }
                seed.secrets.forEach { insertSecret(connection, it) }
                seed.events.forEach { insertEvent(connection, it) }
                seed.dependencyEdges.forEach { insertDependencyEdge(connection, it) }
                seed.tasks.forEach { insertTask(connection, it) }
                connection.commit()
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    override fun getProject(projectId: String): Project? = connect { connection ->
        connection.prepareStatement(
            "SELECT id, name, summary, active_scene_id, current_turn FROM projects WHERE id = ?",
        ).use { statement ->
            statement.setString(1, projectId)
            statement.executeQuery().use { rows ->
                if (rows.next()) rows.toProject() else null
            }
        }
    }

    override fun getActiveScene(projectId: String): Scene? {
        val activeSceneId = getProject(projectId)?.activeSceneId ?: return null
        return connect { connection ->
            connection.prepareStatement(
            "SELECT id, project_id, title, summary, location_id, status, progress_json, completed_requirements_json FROM scenes WHERE project_id = ? AND id = ?",
            ).use { statement ->
                statement.setString(1, projectId)
                statement.setString(2, activeSceneId)
                statement.executeQuery().use { rows ->
                    if (rows.next()) rows.toScene() else null
                }
            }
        }
    }

    override fun getSceneContract(projectId: String, sceneId: String): SceneContract? = connect { connection ->
        connection.prepareStatement(
            """
            SELECT id, project_id, scene_id, allowed_secret_ids_json, required_beats_json, forbidden_reveals_json
            FROM scene_contracts
            WHERE project_id = ? AND scene_id = ?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, projectId)
            statement.setString(2, sceneId)
            statement.executeQuery().use { rows ->
                if (rows.next()) rows.toSceneContract() else null
            }
        }
    }

    override fun getCharacterState(projectId: String, characterId: String): CharacterState? = connect { connection ->
        connection.prepareStatement(
            """
            SELECT character_id, project_id, location_id, mood, health, inventory_json, flags_json
            FROM character_states
            WHERE project_id = ? AND character_id = ?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, projectId)
            statement.setString(2, characterId)
            statement.executeQuery().use { rows ->
                if (rows.next()) rows.toCharacterState() else null
            }
        }
    }

    override fun getTask(projectId: String, taskId: String): Task? = connect { connection ->
        connection.prepareStatement(
            """
            SELECT id, project_id, title, status, payload_json, created_turn, updated_turn
            FROM task_queue
            WHERE project_id = ? AND id = ?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, projectId)
            statement.setString(2, taskId)
            statement.executeQuery().use { rows ->
                if (rows.next()) rows.toTask() else null
            }
        }
    }

    override fun listCharacters(projectId: String): List<CharacterProfile> = connect { connection ->
        connection.queryMany(
            """
            SELECT id, project_id, name, role, description, is_player, profile_json
            FROM characters
            WHERE project_id = ?
            ORDER BY is_player DESC, name ASC
            """.trimIndent(),
            projectId,
        ) { it.toCharacterProfile() }
    }

    override fun listCharacterMemories(projectId: String): List<CharacterMemory> = connect { connection ->
        connection.queryMany(
            """
            SELECT id, project_id, character_id, summary, salience, visibility, created_turn
            FROM character_memories
            WHERE project_id = ?
            ORDER BY salience DESC, created_turn DESC, id ASC
            """.trimIndent(),
            projectId,
        ) { it.toCharacterMemory() }
    }

    override fun listCharacterStates(projectId: String): List<CharacterState> = connect { connection ->
        connection.queryMany(
            """
            SELECT character_id, project_id, location_id, mood, health, inventory_json, flags_json
            FROM character_states
            WHERE project_id = ?
            ORDER BY character_id ASC
            """.trimIndent(),
            projectId,
        ) { it.toCharacterState() }
    }

    override fun listWorldRules(projectId: String): List<WorldRule> = connect { connection ->
        connection.queryMany(
            """
            SELECT id, project_id, title, content, priority
            FROM world_rules
            WHERE project_id = ?
            ORDER BY priority DESC, id ASC
            """.trimIndent(),
            projectId,
        ) { it.toWorldRule() }
    }

    override fun listFacts(projectId: String): List<Fact> = connect { connection ->
        connection.queryMany(
            """
            SELECT id, project_id, content, visibility, source, created_turn
            FROM facts
            WHERE project_id = ?
            ORDER BY created_turn ASC, id ASC
            """.trimIndent(),
            projectId,
        ) { it.toFact() }
    }

    override fun listSecrets(projectId: String): List<Secret> = connect { connection ->
        connection.queryMany(
            """
            SELECT id, project_id, content, revealed, reveal_condition, created_turn, revealed_turn
            FROM secrets
            WHERE project_id = ?
            ORDER BY id ASC
            """.trimIndent(),
            projectId,
        ) { it.toSecret() }
    }

    override fun listRevealedSecrets(projectId: String): List<Secret> = listSecrets(projectId).filter { it.revealed }

    override fun listItems(projectId: String): List<Item> = connect { connection ->
        connection.queryMany(
            """
            SELECT id, project_id, name, description, owner_character_id, location_id, tags_json
            FROM items
            WHERE project_id = ?
            ORDER BY id ASC
            """.trimIndent(),
            projectId,
        ) { it.toItem() }
    }

    override fun listTechniques(projectId: String): List<Technique> = connect { connection ->
        connection.queryMany(
            """
            SELECT id, project_id, name, description, owner_character_id, tags_json
            FROM techniques
            WHERE project_id = ?
            ORDER BY id ASC
            """.trimIndent(),
            projectId,
        ) { it.toTechnique() }
    }

    override fun listRecentEvents(projectId: String, limit: Int): List<Event> = connect { connection ->
        connection.prepareStatement(
            """
            SELECT id, project_id, turn_id, summary, payload_json
            FROM events
            WHERE project_id = ?
            ORDER BY created_at DESC, id DESC
            LIMIT ?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, projectId)
            statement.setInt(2, limit)
            statement.executeQuery().use { rows ->
                buildList {
                    while (rows.next()) add(rows.toEvent())
                }.asReversed()
            }
        }
    }

    override fun listOpenTasks(projectId: String): List<Task> = connect { connection ->
        connection.queryMany(
            """
            SELECT id, project_id, title, status, payload_json, created_turn, updated_turn
            FROM task_queue
            WHERE project_id = ? AND status != 'done'
            ORDER BY updated_turn ASC, id ASC
            """.trimIndent(),
            projectId,
        ) { it.toTask() }
    }

    fun getSession(sessionId: String): StorySession? = connect { connection ->
        connection.prepareStatement(
            """
            SELECT id, project_id, current_scene_id, next_turn_number, state_version, status
            FROM story_sessions
            WHERE id = ?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, sessionId)
            statement.executeQuery().use { rows ->
                if (rows.next()) rows.toStorySession() else null
            }
        }
    }

    fun startSession(projectId: String, sceneId: String): StorySession = connect { connection ->
        val session = StorySession(
            id = newId("session"),
            projectId = projectId,
            currentSceneId = sceneId,
            nextTurnNumber = 1,
            stateVersion = 0,
        )
        connection.prepareStatement(
            """
            INSERT INTO story_sessions(id, project_id, current_scene_id, next_turn_number, state_version, status, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            val timestamp = now()
            statement.setString(1, session.id)
            statement.setString(2, session.projectId)
            statement.setString(3, session.currentSceneId)
            statement.setInt(4, session.nextTurnNumber)
            statement.setInt(5, session.stateVersion)
            statement.setString(6, session.status)
            statement.setString(7, timestamp)
            statement.setString(8, timestamp)
            statement.executeUpdate()
        }
        session
    }

    fun savePendingContext(context: ContextPacket) {
        connect { connection ->
            connection.prepareStatement(
                """
                INSERT INTO turn_logs(
                    id, project_id, turn_number, player_input, context_json, result_json, narrative,
                    has_state_change, created_at, session_id, context_id, base_state_version, context_hash,
                    turn_summary, validation_status
                )
                VALUES (?, ?, ?, ?, ?, '', '', 0, ?, ?, ?, ?, ?, '', 'pending')
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, newId("pending"))
                statement.setString(2, context.project.id)
                statement.setInt(3, context.turnNumber)
                statement.setString(4, context.playerInput)
                statement.setString(5, JsonSupport.json.encodeToString(context))
                statement.setString(6, now())
                statement.setString(7, context.sessionId)
                statement.setString(8, context.contextId)
                statement.setInt(9, context.baseStateVersion)
                statement.setString(10, context.contextHash)
                statement.executeUpdate()
            }
        }
    }

    fun getPendingContext(sessionId: String, contextId: String): ContextPacket? = connect { connection ->
        connection.prepareStatement(
            """
            SELECT context_json
            FROM turn_logs
            WHERE session_id = ? AND context_id = ? AND validation_status = 'pending'
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, sessionId)
            statement.setString(2, contextId)
            statement.executeQuery().use { rows ->
                if (rows.next()) JsonSupport.json.decodeFromString(rows.getString("context_json")) else null
            }
        }
    }

    fun abortPendingContext(sessionId: String, contextId: String?): Int = connect { connection ->
        val sql = if (contextId == null) {
            "UPDATE turn_logs SET validation_status = 'aborted' WHERE session_id = ? AND validation_status = 'pending'"
        } else {
            "UPDATE turn_logs SET validation_status = 'aborted' WHERE session_id = ? AND context_id = ? AND validation_status = 'pending'"
        }
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, sessionId)
            if (contextId != null) statement.setString(2, contextId)
            statement.executeUpdate()
        }
    }

    fun listRecentTurnSummaries(sessionId: String, limit: Int): List<com.localnovel.engine.model.TurnSummary> = connect { connection ->
        connection.prepareStatement(
            """
            SELECT turn_number, result_id, turn_summary
            FROM turn_logs
            WHERE session_id = ? AND validation_status = 'applied'
            ORDER BY turn_number DESC
            LIMIT ?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, sessionId)
            statement.setInt(2, limit)
            statement.executeQuery().use { rows ->
                buildList {
                    while (rows.next()) {
                        add(
                            com.localnovel.engine.model.TurnSummary(
                                turnNumber = rows.getInt("turn_number"),
                                resultId = rows.getString("result_id") ?: "",
                                summary = rows.getString("turn_summary") ?: "",
                            ),
                        )
                    }
                }.asReversed()
            }
        }
    }

    fun appliedResultExists(resultId: String): Boolean = connect { connection ->
        connection.prepareStatement(
            "SELECT 1 FROM turn_logs WHERE result_id = ? AND validation_status = 'applied' LIMIT 1",
        ).use { statement ->
            statement.setString(1, resultId)
            statement.executeQuery().use { it.next() }
        }
    }

    fun appliedTurnCount(sessionId: String): Int = connect { connection ->
        connection.prepareStatement(
            "SELECT COUNT(*) FROM turn_logs WHERE session_id = ? AND validation_status = 'applied'",
        ).use { statement ->
            statement.setString(1, sessionId)
            statement.executeQuery().use { rows ->
                rows.next()
                rows.getInt(1)
            }
        }
    }

    fun tableCount(tableName: String): Int = connect { connection ->
        require(tableName.matches(Regex("[a-zA-Z_]+"))) { "Unsafe table name: $tableName" }
        connection.prepareStatement("SELECT COUNT(*) FROM $tableName").use { statement ->
            statement.executeQuery().use { rows ->
                rows.next()
                rows.getInt(1)
            }
        }
    }

    fun resolvePendingTurn(context: ContextPacket, result: AiTurnResult, hasStateChange: Boolean): ApplyResult = connect { connection ->
        connection.autoCommit = false
        try {
            val session = selectSession(connection, context.sessionId)
                ?: error("Session not found: ${context.sessionId}")
            require(session.stateVersion == context.baseStateVersion) { "Stale baseStateVersion: expected ${session.stateVersion}, got ${context.baseStateVersion}." }
            require(session.nextTurnNumber == context.turnNumber) { "Stale turnNumber: expected ${session.nextTurnNumber}, got ${context.turnNumber}." }

            val pendingId = selectPendingTurnLogId(connection, context.sessionId, context.contextId)
                ?: error("Pending context not found: ${context.contextId}")
            require(!result.resultId.isBlank()) { "resultId is required." }
            require(!selectResultExists(connection, result.resultId)) { "Result already applied: ${result.resultId}" }

            result.allDeltaOperations().forEach { operation ->
                recordStateDelta(connection, context.project.id, context.turnId, operation.type.name.lowercase(), operation.targetType, operation.targetId, JsonSupport.json.encodeToString(operation.payload))
                applyOperation(connection, context, operation)
            }

            applySceneProgressUpdates(connection, context, result)
            connection.prepareStatement(
                """
                UPDATE turn_logs
                SET result_json = ?, narrative = ?, has_state_change = ?, result_id = ?,
                    turn_summary = ?, validation_status = 'applied'
                WHERE id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, JsonSupport.json.encodeToString(result))
                statement.setString(2, result.narrative)
                statement.setInt(3, if (hasStateChange) 1 else 0)
                statement.setString(4, result.resultId)
                statement.setString(5, result.turnSummary.take(500))
                statement.setString(6, pendingId)
                statement.executeUpdate()
            }
            connection.prepareStatement(
                """
                UPDATE story_sessions
                SET state_version = state_version + 1, next_turn_number = next_turn_number + 1, updated_at = ?
                WHERE id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, now())
                statement.setString(2, context.sessionId)
                statement.executeUpdate()
            }
            updateProjectTurn(connection, context.project.id, context.turnNumber)
            connection.commit()
            ApplyResult(pendingId, result.narrative, hasStateChange)
        } catch (error: Throwable) {
            connection.rollback()
            throw error
        } finally {
            connection.autoCommit = true
        }
    }

    override fun listTables(): Set<String> = connect { connection ->
        connection.prepareStatement(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'",
        ).use { statement ->
            statement.executeQuery().use { rows ->
                buildSet {
                    while (rows.next()) add(rows.getString("name"))
                }
            }
        }
    }

    override fun targetExists(projectId: String, targetType: String, targetId: String): Boolean = connect { connection ->
        val table = when (targetType) {
            "character", "characters" -> "characters"
            "character_state", "character_states" -> "character_states"
            "fact", "facts" -> "facts"
            "secret", "secrets" -> "secrets"
            "event", "events" -> "events"
            "task", "task_queue", "tasks" -> "task_queue"
            "item", "items" -> "items"
            "technique", "techniques" -> "techniques"
            "scene", "scenes" -> "scenes"
            else -> return@connect false
        }
        val idColumn = if (table == "character_states") "character_id" else "id"
        connection.prepareStatement("SELECT 1 FROM $table WHERE project_id = ? AND $idColumn = ? LIMIT 1").use { statement ->
            statement.setString(1, projectId)
            statement.setString(2, targetId)
            statement.executeQuery().use { it.next() }
        }
    }

    override fun lastTurnStateChangeFlags(projectId: String, limit: Int): List<Boolean> = connect { connection ->
        connection.prepareStatement(
            """
            SELECT has_state_change
            FROM turn_logs
            WHERE project_id = ?
            ORDER BY turn_number DESC
            LIMIT ?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, projectId)
            statement.setInt(2, limit)
            statement.executeQuery().use { rows ->
                buildList {
                    while (rows.next()) add(rows.getInt("has_state_change") == 1)
                }
            }
        }
    }

    override fun saveTurnLog(context: ContextPacket, result: AiTurnResult, hasStateChange: Boolean): String = connect { connection ->
        val id = newId("turn")
        connection.prepareStatement(
            """
            INSERT INTO turn_logs(id, project_id, turn_number, player_input, context_json, result_json, narrative, has_state_change, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, id)
            statement.setString(2, context.project.id)
            statement.setInt(3, context.turnNumber)
            statement.setString(4, context.playerInput)
            statement.setString(5, JsonSupport.json.encodeToString(context))
            statement.setString(6, JsonSupport.json.encodeToString(result))
            statement.setString(7, result.narrative)
            statement.setInt(8, if (hasStateChange) 1 else 0)
            statement.setString(9, now())
            statement.executeUpdate()
        }
        id
    }

    override fun recordStateDelta(projectId: String, turnId: String, operationType: String, targetType: String?, targetId: String?, payloadJson: String) {
        connect { connection ->
            connection.prepareStatement(
                """
                INSERT INTO state_deltas(id, project_id, turn_id, operation_type, target_type, target_id, payload_json, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, newId("delta"))
                statement.setString(2, projectId)
                statement.setString(3, turnId)
                statement.setString(4, operationType)
                statement.setString(5, targetType)
                statement.setString(6, targetId)
                statement.setString(7, payloadJson)
                statement.setString(8, now())
                statement.executeUpdate()
            }
        }
    }

    override fun updateCharacterState(state: CharacterState) {
        connect { connection -> insertCharacterState(connection, state) }
    }

    override fun addFact(fact: Fact) {
        connect { connection -> insertFact(connection, fact) }
    }

    override fun revealSecret(projectId: String, secretId: String, revealedTurn: Int) {
        connect { connection ->
            connection.prepareStatement(
                "UPDATE secrets SET revealed = 1, revealed_turn = ? WHERE project_id = ? AND id = ?",
            ).use { statement ->
                statement.setInt(1, revealedTurn)
                statement.setString(2, projectId)
                statement.setString(3, secretId)
                statement.executeUpdate()
            }
        }
    }

    override fun addEvent(event: Event) {
        connect { connection -> insertEvent(connection, event) }
    }

    override fun upsertTask(task: Task) {
        connect { connection -> insertTask(connection, task) }
    }

    override fun updateProjectTurn(projectId: String, turnNumber: Int) {
        connect { connection ->
            connection.prepareStatement("UPDATE projects SET current_turn = ?, updated_at = ? WHERE id = ?").use { statement ->
                statement.setInt(1, turnNumber)
                statement.setString(2, now())
                statement.setString(3, projectId)
                statement.executeUpdate()
            }
        }
    }

    private fun <T> connect(block: (Connection) -> T): T =
        DriverManager.getConnection("jdbc:sqlite:$dbPath").use { connection ->
            connection.createStatement().use { it.executeUpdate("PRAGMA foreign_keys = ON") }
            block(connection)
        }

    private fun executeMigrationStatement(statement: java.sql.Statement, sql: String) {
        if (sql.isBlank()) return
        try {
            statement.executeUpdate(sql)
        } catch (error: SQLException) {
            val message = error.message.orEmpty()
            val duplicateColumn = sql.startsWith("ALTER TABLE", ignoreCase = true) &&
                message.contains("duplicate column name", ignoreCase = true)
            if (!duplicateColumn) throw error
        }
    }

    private fun clearProject(connection: Connection, projectId: String) {
        connection.prepareStatement("DELETE FROM projects WHERE id = ?").use { statement ->
            statement.setString(1, projectId)
            statement.executeUpdate()
        }
    }

    private fun insertProject(connection: Connection, project: Project) {
        connection.prepareStatement(
            """
            INSERT OR REPLACE INTO projects(id, name, summary, active_scene_id, current_turn, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, COALESCE((SELECT created_at FROM projects WHERE id = ?), ?), ?)
            """.trimIndent(),
        ).use { statement ->
            val timestamp = now()
            statement.setString(1, project.id)
            statement.setString(2, project.name)
            statement.setString(3, project.summary)
            statement.setString(4, project.activeSceneId)
            statement.setInt(5, project.currentTurn)
            statement.setString(6, project.id)
            statement.setString(7, timestamp)
            statement.setString(8, timestamp)
            statement.executeUpdate()
        }
    }

    private fun insertCharacter(connection: Connection, character: CharacterProfile) {
        connection.prepareStatement(
            """
            INSERT OR REPLACE INTO characters(id, project_id, name, role, description, is_player, profile_json)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, character.id)
            statement.setString(2, character.projectId)
            statement.setString(3, character.name)
            statement.setString(4, character.role)
            statement.setString(5, character.description)
            statement.setInt(6, if (character.isPlayer) 1 else 0)
            statement.setString(7, JsonSupport.json.encodeToString(character.profile))
            statement.executeUpdate()
        }
    }

    private fun insertCharacterState(connection: Connection, state: CharacterState) {
        connection.prepareStatement(
            """
            INSERT OR REPLACE INTO character_states(character_id, project_id, location_id, mood, health, inventory_json, flags_json, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, state.characterId)
            statement.setString(2, state.projectId)
            statement.setString(3, state.locationId)
            statement.setString(4, state.mood)
            statement.setString(5, state.health)
            statement.setString(6, JsonSupport.json.encodeToString(state.inventory))
            statement.setString(7, JsonSupport.json.encodeToString(state.flags))
            statement.setString(8, now())
            statement.executeUpdate()
        }
    }

    private fun insertCharacterMemory(connection: Connection, memory: CharacterMemory) {
        connection.prepareStatement(
            """
            INSERT OR REPLACE INTO character_memories(id, project_id, character_id, summary, salience, visibility, created_turn)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, memory.id)
            statement.setString(2, memory.projectId)
            statement.setString(3, memory.characterId)
            statement.setString(4, memory.summary)
            statement.setInt(5, memory.salience)
            statement.setString(6, memory.visibility)
            statement.setInt(7, memory.createdTurn)
            statement.executeUpdate()
        }
    }

    private fun insertWorldRule(connection: Connection, rule: WorldRule) {
        connection.prepareStatement(
            "INSERT OR REPLACE INTO world_rules(id, project_id, title, content, priority) VALUES (?, ?, ?, ?, ?)",
        ).use { statement ->
            statement.setString(1, rule.id)
            statement.setString(2, rule.projectId)
            statement.setString(3, rule.title)
            statement.setString(4, rule.content)
            statement.setInt(5, rule.priority)
            statement.executeUpdate()
        }
    }

    private fun insertFaction(connection: Connection, faction: Faction) {
        connection.prepareStatement(
            "INSERT OR REPLACE INTO factions(id, project_id, name, description, stance) VALUES (?, ?, ?, ?, ?)",
        ).use { statement ->
            statement.setString(1, faction.id)
            statement.setString(2, faction.projectId)
            statement.setString(3, faction.name)
            statement.setString(4, faction.description)
            statement.setString(5, faction.stance)
            statement.executeUpdate()
        }
    }

    private fun insertLocation(connection: Connection, location: Location) {
        connection.prepareStatement(
            "INSERT OR REPLACE INTO locations(id, project_id, name, description, parent_location_id) VALUES (?, ?, ?, ?, ?)",
        ).use { statement ->
            statement.setString(1, location.id)
            statement.setString(2, location.projectId)
            statement.setString(3, location.name)
            statement.setString(4, location.description)
            statement.setString(5, location.parentLocationId)
            statement.executeUpdate()
        }
    }

    private fun insertItem(connection: Connection, item: Item) {
        connection.prepareStatement(
            """
            INSERT OR REPLACE INTO items(id, project_id, name, description, owner_character_id, location_id, tags_json)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, item.id)
            statement.setString(2, item.projectId)
            statement.setString(3, item.name)
            statement.setString(4, item.description)
            statement.setString(5, item.ownerCharacterId)
            statement.setString(6, item.locationId)
            statement.setString(7, JsonSupport.json.encodeToString(item.tags))
            statement.executeUpdate()
        }
    }

    private fun insertTechnique(connection: Connection, technique: Technique) {
        connection.prepareStatement(
            """
            INSERT OR REPLACE INTO techniques(id, project_id, name, description, owner_character_id, tags_json)
            VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, technique.id)
            statement.setString(2, technique.projectId)
            statement.setString(3, technique.name)
            statement.setString(4, technique.description)
            statement.setString(5, technique.ownerCharacterId)
            statement.setString(6, JsonSupport.json.encodeToString(technique.tags))
            statement.executeUpdate()
        }
    }

    private fun insertScene(connection: Connection, scene: Scene) {
        connection.prepareStatement(
            """
            INSERT OR REPLACE INTO scenes(id, project_id, title, summary, location_id, status, progress_json, completed_requirements_json)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, scene.id)
            statement.setString(2, scene.projectId)
            statement.setString(3, scene.title)
            statement.setString(4, scene.summary)
            statement.setString(5, scene.locationId)
            statement.setString(6, scene.status)
            statement.setString(7, JsonSupport.json.encodeToString(scene.progress))
            statement.setString(8, JsonSupport.json.encodeToString(scene.completedRequirements))
            statement.executeUpdate()
        }
    }

    private fun insertSceneContract(connection: Connection, contract: SceneContract) {
        connection.prepareStatement(
            """
            INSERT OR REPLACE INTO scene_contracts(id, project_id, scene_id, allowed_secret_ids_json, required_beats_json, forbidden_reveals_json)
            VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, contract.id)
            statement.setString(2, contract.projectId)
            statement.setString(3, contract.sceneId)
            statement.setString(4, JsonSupport.json.encodeToString(contract.allowedSecretIds))
            statement.setString(5, JsonSupport.json.encodeToString(contract.requiredBeats))
            statement.setString(6, JsonSupport.json.encodeToString(contract.forbiddenReveals))
            statement.executeUpdate()
        }
    }

    private fun insertFact(connection: Connection, fact: Fact) {
        connection.prepareStatement(
            "INSERT OR REPLACE INTO facts(id, project_id, content, visibility, source, created_turn) VALUES (?, ?, ?, ?, ?, ?)",
        ).use { statement ->
            statement.setString(1, fact.id)
            statement.setString(2, fact.projectId)
            statement.setString(3, fact.content)
            statement.setString(4, fact.visibility)
            statement.setString(5, fact.source)
            statement.setInt(6, fact.createdTurn)
            statement.executeUpdate()
        }
    }

    private fun insertSecret(connection: Connection, secret: Secret) {
        connection.prepareStatement(
            """
            INSERT OR REPLACE INTO secrets(id, project_id, content, revealed, reveal_condition, created_turn, revealed_turn)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, secret.id)
            statement.setString(2, secret.projectId)
            statement.setString(3, secret.content)
            statement.setInt(4, if (secret.revealed) 1 else 0)
            statement.setString(5, secret.revealCondition)
            statement.setInt(6, secret.createdTurn)
            if (secret.revealedTurn == null) {
                statement.setNull(7, java.sql.Types.INTEGER)
            } else {
                statement.setInt(7, secret.revealedTurn)
            }
            statement.executeUpdate()
        }
    }

    private fun insertEvent(connection: Connection, event: Event) {
        connection.prepareStatement(
            "INSERT OR REPLACE INTO events(id, project_id, turn_id, summary, payload_json, created_at) VALUES (?, ?, ?, ?, ?, ?)",
        ).use { statement ->
            statement.setString(1, event.id)
            statement.setString(2, event.projectId)
            statement.setString(3, event.turnId)
            statement.setString(4, event.summary)
            statement.setString(5, JsonSupport.json.encodeToString(event.payload))
            statement.setString(6, now())
            statement.executeUpdate()
        }
    }

    private fun insertDependencyEdge(connection: Connection, edge: DependencyEdge) {
        connection.prepareStatement(
            """
            INSERT OR REPLACE INTO dependency_edges(id, project_id, from_type, from_id, to_type, to_id, relation)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, edge.id)
            statement.setString(2, edge.projectId)
            statement.setString(3, edge.fromType)
            statement.setString(4, edge.fromId)
            statement.setString(5, edge.toType)
            statement.setString(6, edge.toId)
            statement.setString(7, edge.relation)
            statement.executeUpdate()
        }
    }

    private fun insertTask(connection: Connection, task: Task) {
        connection.prepareStatement(
            """
            INSERT OR REPLACE INTO task_queue(id, project_id, title, status, payload_json, created_turn, updated_turn)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, task.id)
            statement.setString(2, task.projectId)
            statement.setString(3, task.title)
            statement.setString(4, task.status)
            statement.setString(5, JsonSupport.json.encodeToString(task.payload))
            statement.setInt(6, task.createdTurn)
            statement.setInt(7, task.updatedTurn)
            statement.executeUpdate()
        }
    }

    private fun recordStateDelta(connection: Connection, projectId: String, turnId: String, operationType: String, targetType: String?, targetId: String?, payloadJson: String) {
        connection.prepareStatement(
            """
            INSERT INTO state_deltas(id, project_id, turn_id, operation_type, target_type, target_id, payload_json, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, newId("delta"))
            statement.setString(2, projectId)
            statement.setString(3, turnId)
            statement.setString(4, operationType)
            statement.setString(5, targetType)
            statement.setString(6, targetId)
            statement.setString(7, payloadJson)
            statement.setString(8, now())
            statement.executeUpdate()
        }
    }

    private fun applyOperation(connection: Connection, context: ContextPacket, operation: com.localnovel.engine.model.DeltaOperation) {
        when (operation.type) {
            com.localnovel.engine.model.DeltaOperationType.UPDATE_CHARACTER_STATE -> {
                val current = selectCharacterState(connection, context.project.id, operation.targetId ?: error("Missing character state target"))
                    ?: error("Character state not found: ${operation.targetId}")
                insertCharacterState(
                    connection,
                    CharacterState(
                        characterId = current.characterId,
                        projectId = current.projectId,
                        locationId = operation.payload.stringValue("locationId") ?: operation.payload.stringValue("location_id") ?: current.locationId,
                        mood = operation.payload.stringValue("mood") ?: current.mood,
                        health = operation.payload.stringValue("health") ?: current.health,
                        inventory = operation.payload.decodeValue("inventory") ?: current.inventory,
                        flags = operation.payload.decodeValue("flags") ?: current.flags,
                    ),
                )
            }
            com.localnovel.engine.model.DeltaOperationType.ADD_FACT -> {
                insertFact(
                    connection,
                    Fact(
                        id = operation.targetId ?: operation.payload.stringValue("id") ?: newId("fact"),
                        projectId = context.project.id,
                        content = operation.payload.stringValue("content") ?: error("add_fact requires content"),
                        visibility = operation.payload.stringValue("visibility") ?: "public",
                        source = operation.payload.stringValue("source") ?: "ai_turn_result",
                        createdTurn = context.turnNumber,
                    ),
                )
            }
            com.localnovel.engine.model.DeltaOperationType.REVEAL_SECRET -> {
                connection.prepareStatement(
                    "UPDATE secrets SET revealed = 1, revealed_turn = ? WHERE project_id = ? AND id = ?",
                ).use { statement ->
                    statement.setInt(1, context.turnNumber)
                    statement.setString(2, context.project.id)
                    statement.setString(3, operation.targetId ?: error("reveal_secret requires targetId"))
                    statement.executeUpdate()
                }
            }
            com.localnovel.engine.model.DeltaOperationType.ADD_EVENT -> {
                insertEvent(
                    connection,
                    Event(
                        id = operation.targetId ?: operation.payload.stringValue("id") ?: newId("event"),
                        projectId = context.project.id,
                        turnId = context.turnId,
                        summary = operation.payload.stringValue("summary") ?: error("add_event requires summary"),
                        payload = operation.payload,
                    ),
                )
            }
            com.localnovel.engine.model.DeltaOperationType.UPSERT_TASK -> {
                val taskId = operation.targetId ?: operation.payload.stringValue("id") ?: newId("task")
                val existing = selectTask(connection, context.project.id, taskId)
                insertTask(
                    connection,
                    Task(
                        id = taskId,
                        projectId = context.project.id,
                        title = operation.payload.stringValue("title") ?: existing?.title ?: error("upsert_task requires title"),
                        status = operation.payload.stringValue("status") ?: existing?.status ?: "open",
                        payload = operation.payload,
                        createdTurn = existing?.createdTurn ?: context.turnNumber,
                        updatedTurn = context.turnNumber,
                    ),
                )
            }
            com.localnovel.engine.model.DeltaOperationType.NOOP -> Unit
        }
    }

    private fun applySceneProgressUpdates(connection: Connection, context: ContextPacket, result: AiTurnResult) {
        val scene = context.activeScene ?: return
        val completed = scene.completedRequirements.toMutableSet()
        result.sceneProgressUpdates
            .filter { it.status == "completed" }
            .forEach { completed += it.requirement }
        if (result.sceneProgressUpdates.isEmpty()) return
        val progress = JsonObject(
            result.sceneProgressUpdates.associate {
                it.requirement to kotlinx.serialization.json.JsonPrimitive(it.status)
            },
        )
        connection.prepareStatement(
            "UPDATE scenes SET progress_json = ?, completed_requirements_json = ? WHERE project_id = ? AND id = ?",
        ).use { statement ->
            statement.setString(1, JsonSupport.json.encodeToString(progress))
            statement.setString(2, JsonSupport.json.encodeToString(completed.toList()))
            statement.setString(3, scene.projectId)
            statement.setString(4, scene.id)
            statement.executeUpdate()
        }
    }

    private fun updateProjectTurn(connection: Connection, projectId: String, turnNumber: Int) {
        connection.prepareStatement("UPDATE projects SET current_turn = ?, updated_at = ? WHERE id = ?").use { statement ->
            statement.setInt(1, turnNumber)
            statement.setString(2, now())
            statement.setString(3, projectId)
            statement.executeUpdate()
        }
    }

    private fun selectSession(connection: Connection, sessionId: String): StorySession? =
        connection.prepareStatement(
            "SELECT id, project_id, current_scene_id, next_turn_number, state_version, status FROM story_sessions WHERE id = ?",
        ).use { statement ->
            statement.setString(1, sessionId)
            statement.executeQuery().use { rows -> if (rows.next()) rows.toStorySession() else null }
        }

    private fun selectPendingTurnLogId(connection: Connection, sessionId: String, contextId: String): String? =
        connection.prepareStatement(
            "SELECT id FROM turn_logs WHERE session_id = ? AND context_id = ? AND validation_status = 'pending'",
        ).use { statement ->
            statement.setString(1, sessionId)
            statement.setString(2, contextId)
            statement.executeQuery().use { rows -> if (rows.next()) rows.getString("id") else null }
        }

    private fun selectResultExists(connection: Connection, resultId: String): Boolean =
        connection.prepareStatement(
            "SELECT 1 FROM turn_logs WHERE result_id = ? AND validation_status = 'applied' LIMIT 1",
        ).use { statement ->
            statement.setString(1, resultId)
            statement.executeQuery().use { it.next() }
        }

    private fun selectCharacterState(connection: Connection, projectId: String, characterId: String): CharacterState? =
        connection.prepareStatement(
            "SELECT character_id, project_id, location_id, mood, health, inventory_json, flags_json FROM character_states WHERE project_id = ? AND character_id = ?",
        ).use { statement ->
            statement.setString(1, projectId)
            statement.setString(2, characterId)
            statement.executeQuery().use { rows -> if (rows.next()) rows.toCharacterState() else null }
        }

    private fun selectTask(connection: Connection, projectId: String, taskId: String): Task? =
        connection.prepareStatement(
            "SELECT id, project_id, title, status, payload_json, created_turn, updated_turn FROM task_queue WHERE project_id = ? AND id = ?",
        ).use { statement ->
            statement.setString(1, projectId)
            statement.setString(2, taskId)
            statement.executeQuery().use { rows -> if (rows.next()) rows.toTask() else null }
        }

    private fun <T> Connection.queryMany(sql: String, projectId: String, mapper: (ResultSet) -> T): List<T> =
        prepareStatement(sql).use { statement ->
            statement.setString(1, projectId)
            statement.executeQuery().use { rows ->
                buildList {
                    while (rows.next()) add(mapper(rows))
                }
            }
        }

    private fun ResultSet.toProject(): Project = Project(
        id = getString("id"),
        name = getString("name"),
        summary = getString("summary"),
        activeSceneId = getString("active_scene_id"),
        currentTurn = getInt("current_turn"),
    )

    private fun ResultSet.toCharacterProfile(): CharacterProfile = CharacterProfile(
        id = getString("id"),
        projectId = getString("project_id"),
        name = getString("name"),
        role = getString("role"),
        description = getString("description"),
        isPlayer = getInt("is_player") == 1,
        profile = JsonSupport.json.decodeFromString(getString("profile_json")),
    )

    private fun ResultSet.toCharacterMemory(): CharacterMemory = CharacterMemory(
        id = getString("id"),
        projectId = getString("project_id"),
        characterId = getString("character_id"),
        summary = getString("summary"),
        salience = getInt("salience"),
        visibility = getString("visibility"),
        createdTurn = getInt("created_turn"),
    )

    private fun ResultSet.toCharacterState(): CharacterState = CharacterState(
        characterId = getString("character_id"),
        projectId = getString("project_id"),
        locationId = getString("location_id"),
        mood = getString("mood"),
        health = getString("health"),
        inventory = JsonSupport.json.decodeFromString(getString("inventory_json")),
        flags = JsonSupport.json.decodeFromString(getString("flags_json")),
    )

    private fun ResultSet.toWorldRule(): WorldRule = WorldRule(
        id = getString("id"),
        projectId = getString("project_id"),
        title = getString("title"),
        content = getString("content"),
        priority = getInt("priority"),
    )

    private fun ResultSet.toItem(): Item = Item(
        id = getString("id"),
        projectId = getString("project_id"),
        name = getString("name"),
        description = getString("description"),
        ownerCharacterId = getString("owner_character_id"),
        locationId = getString("location_id"),
        tags = JsonSupport.json.decodeFromString(getString("tags_json")),
    )

    private fun ResultSet.toTechnique(): Technique = Technique(
        id = getString("id"),
        projectId = getString("project_id"),
        name = getString("name"),
        description = getString("description"),
        ownerCharacterId = getString("owner_character_id"),
        tags = JsonSupport.json.decodeFromString(getString("tags_json")),
    )

    private fun ResultSet.toScene(): Scene = Scene(
        id = getString("id"),
        projectId = getString("project_id"),
        title = getString("title"),
        summary = getString("summary"),
        locationId = getString("location_id"),
        status = getString("status"),
        progress = runCatching { JsonSupport.json.decodeFromString<JsonObject>(getString("progress_json")) }.getOrDefault(JsonObject(emptyMap())),
        completedRequirements = runCatching { JsonSupport.json.decodeFromString<List<String>>(getString("completed_requirements_json")) }.getOrDefault(emptyList()),
    )

    private fun ResultSet.toSceneContract(): SceneContract = SceneContract(
        id = getString("id"),
        projectId = getString("project_id"),
        sceneId = getString("scene_id"),
        allowedSecretIds = JsonSupport.json.decodeFromString(getString("allowed_secret_ids_json")),
        requiredBeats = JsonSupport.json.decodeFromString(getString("required_beats_json")),
        forbiddenReveals = JsonSupport.json.decodeFromString(getString("forbidden_reveals_json")),
    )

    private fun ResultSet.toFact(): Fact = Fact(
        id = getString("id"),
        projectId = getString("project_id"),
        content = getString("content"),
        visibility = getString("visibility"),
        source = getString("source"),
        createdTurn = getInt("created_turn"),
    )

    private fun ResultSet.toSecret(): Secret {
        val revealedTurnValue = getInt("revealed_turn")
        val revealedTurn = if (wasNull()) null else revealedTurnValue
        return Secret(
            id = getString("id"),
            projectId = getString("project_id"),
            content = getString("content"),
            revealed = getInt("revealed") == 1,
            revealCondition = getString("reveal_condition"),
            createdTurn = getInt("created_turn"),
            revealedTurn = revealedTurn,
        )
    }

    private fun ResultSet.toEvent(): Event = Event(
        id = getString("id"),
        projectId = getString("project_id"),
        turnId = getString("turn_id"),
        summary = getString("summary"),
        payload = JsonSupport.json.decodeFromString(getString("payload_json")),
    )

    private fun ResultSet.toTask(): Task = Task(
        id = getString("id"),
        projectId = getString("project_id"),
        title = getString("title"),
        status = getString("status"),
        payload = JsonSupport.json.decodeFromString(getString("payload_json")),
        createdTurn = getInt("created_turn"),
        updatedTurn = getInt("updated_turn"),
    )

    private fun ResultSet.toStorySession(): StorySession = StorySession(
        id = getString("id"),
        projectId = getString("project_id"),
        currentSceneId = getString("current_scene_id"),
        nextTurnNumber = getInt("next_turn_number"),
        stateVersion = getInt("state_version"),
        status = getString("status"),
    )

    private fun String.splitToStatements(): List<String> =
        lineSequence()
            .filterNot { it.trimStart().startsWith("--") }
            .joinToString("\n")
            .split(";")
            .map { it.trim() }

    private fun now(): String = Instant.now().toString()

    companion object {
        fun newId(prefix: String): String = "$prefix-${UUID.randomUUID()}"
    }
}

private fun JsonObject.stringValue(key: String): String? =
    this[key]?.let { runCatching { it.jsonPrimitive.content }.getOrNull() }

private inline fun <reified T> JsonObject.decodeValue(key: String): T? {
    val value = this[key] ?: return null
    return runCatching { JsonSupport.json.decodeFromString<T>(JsonSupport.json.encodeToString(value)) }.getOrNull()
}
