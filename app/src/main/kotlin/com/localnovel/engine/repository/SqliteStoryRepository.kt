package com.localnovel.engine.repository

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.ApplyResult
import com.localnovel.engine.model.CharacterKnowledgeClaim
import com.localnovel.engine.model.CharacterMemory
import com.localnovel.engine.model.CharacterProfile
import com.localnovel.engine.model.CharacterState
import com.localnovel.engine.model.ContextKnowledgeClaim
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.ContinuityConstraints
import com.localnovel.engine.model.DemoProjectSeed
import com.localnovel.engine.model.DependencyEdge
import com.localnovel.engine.model.Event
import com.localnovel.engine.model.Fact
import com.localnovel.engine.model.Faction
import com.localnovel.engine.model.InitialRuntimeTemplate
import com.localnovel.engine.model.Item
import com.localnovel.engine.model.Location
import com.localnovel.engine.model.LocationConnection
import com.localnovel.engine.model.MemoryCandidate
import com.localnovel.engine.model.SettingPack
import com.localnovel.engine.model.SettingPackApplyResult
import com.localnovel.engine.model.SettingPackDiff
import com.localnovel.engine.model.SettingPackEntity
import com.localnovel.engine.model.SettingPackImpactReport
import com.localnovel.engine.model.SettingPackPreview
import com.localnovel.engine.model.SettingPackRelation
import com.localnovel.engine.model.Project
import com.localnovel.engine.model.RetrievedMemory
import com.localnovel.engine.model.ScheduledWorldEventSeed
import com.localnovel.engine.model.Scene
import com.localnovel.engine.model.SceneContract
import com.localnovel.engine.model.SceneLifecycle
import com.localnovel.engine.model.SceneRequirementView
import com.localnovel.engine.model.SceneRequirementDefinition
import com.localnovel.engine.model.SceneTransitionDefinition
import com.localnovel.engine.model.Secret
import com.localnovel.engine.model.SessionThreadState
import com.localnovel.engine.model.StoryThread
import com.localnovel.engine.model.StorySession
import com.localnovel.engine.model.Task
import com.localnovel.engine.model.Technique
import com.localnovel.engine.model.TimelineBranch
import com.localnovel.engine.model.ActiveStoryThreadView
import com.localnovel.engine.model.AvailableTransition
import com.localnovel.engine.model.PendingWorldEventView
import com.localnovel.engine.model.WorldRule
import com.localnovel.engine.model.allDeltaOperations
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
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
                listOf(
                    "001_init.sql",
                    "002_multi_turn_runtime.sql",
                    "003_memory_knowledge_runtime.sql",
                    "004_scene_thread_runtime.sql",
                    "005_setting_pack_runtime.sql",
                    "006_checkpoint_branching_runtime.sql",
                ).forEach { fileName ->
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
                seed.knowledgeClaims.forEach { insertKnowledgeClaim(connection, it) }
                seed.storyThreads.forEach { insertStoryThread(connection, it) }
                seed.sceneRequirements.forEach { insertSceneRequirement(connection, it) }
                seed.locationConnections.forEach { insertLocationConnection(connection, it) }
                seed.sceneTransitions.forEach { insertSceneTransition(connection, it) }
                seed.scheduledWorldEvents.forEach { insertScheduledWorldEvent(connection, it) }
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
            SELECT id, project_id, character_id, owner_character_id, memory_type, summary, details_json,
                   source_type, source_id, subject_type, subject_id, tags_json, trigger_tags_json,
                   emotional_signature_json, salience, confidence, reliability, visibility,
                   created_turn, created_turn_number, last_recalled_turn_number, recall_count,
                   decay_rate, status, is_canon, supersedes_memory_id
            FROM character_memories
            WHERE project_id = ? AND status = 'active' AND visibility != 'author_only'
            ORDER BY salience DESC, created_turn_number DESC, id ASC
            """.trimIndent(),
            projectId,
        ) { it.toCharacterMemory() }
    }

    fun listAllCharacterMemories(projectId: String): List<CharacterMemory> = connect { connection ->
        connection.queryMany(
            """
            SELECT id, project_id, character_id, owner_character_id, memory_type, summary, details_json,
                   source_type, source_id, subject_type, subject_id, tags_json, trigger_tags_json,
                   emotional_signature_json, salience, confidence, reliability, visibility,
                   created_turn, created_turn_number, last_recalled_turn_number, recall_count,
                   decay_rate, status, is_canon, supersedes_memory_id
            FROM character_memories
            WHERE project_id = ?
            ORDER BY owner_character_id ASC, salience DESC, created_turn_number DESC, id ASC
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

    fun listKnowledgeClaims(projectId: String, characterId: String? = null): List<CharacterKnowledgeClaim> = connect { connection ->
        val sql = if (characterId == null) {
            """
            SELECT id, project_id, character_id, proposition_key, statement, epistemic_state,
                   confidence, source_fact_id, source_memory_id, acquired_turn_number,
                   updated_turn_number, visibility, status
            FROM character_knowledge
            WHERE project_id = ? AND status = 'active'
            ORDER BY character_id ASC, confidence DESC, id ASC
            """.trimIndent()
        } else {
            """
            SELECT id, project_id, character_id, proposition_key, statement, epistemic_state,
                   confidence, source_fact_id, source_memory_id, acquired_turn_number,
                   updated_turn_number, visibility, status
            FROM character_knowledge
            WHERE project_id = ? AND character_id = ? AND status = 'active'
            ORDER BY confidence DESC, id ASC
            """.trimIndent()
        }
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, projectId)
            if (characterId != null) statement.setString(2, characterId)
            statement.executeQuery().use { rows ->
                buildList {
                    while (rows.next()) add(rows.toKnowledgeClaim())
                }
            }
        }
    }

    fun listRecallLogs(sessionId: String): List<Map<String, String>> = connect { connection ->
        connection.prepareStatement(
            """
            SELECT turn_number, context_id, character_id, memory_id, retrieval_score, retrieval_reasons_json, included_in_context
            FROM memory_recall_logs
            WHERE session_id = ?
            ORDER BY turn_number ASC, character_id ASC, retrieval_score DESC, memory_id ASC
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, sessionId)
            statement.executeQuery().use { rows ->
                buildList {
                    while (rows.next()) {
                        add(
                            mapOf(
                                "turnNumber" to rows.getInt("turn_number").toString(),
                                "contextId" to rows.getString("context_id"),
                                "characterId" to rows.getString("character_id"),
                                "memoryId" to rows.getString("memory_id"),
                                "score" to rows.getInt("retrieval_score").toString(),
                                "reasons" to rows.getString("retrieval_reasons_json"),
                                "included" to (rows.getInt("included_in_context") == 1).toString(),
                            ),
                        )
                    }
                }
            }
        }
    }

    fun getCharacterMemory(projectId: String, memoryId: String): CharacterMemory? = connect { connection ->
        selectCharacterMemory(connection, projectId, memoryId)
    }

    fun getKnowledgeClaim(projectId: String, characterId: String, propositionKey: String): CharacterKnowledgeClaim? = connect { connection ->
        selectKnowledgeClaim(connection, projectId, characterId, propositionKey)
    }

    fun upsertCharacterMemory(memory: CharacterMemory) {
        connect { connection -> insertCharacterMemory(connection, memory) }
    }

    fun upsertKnowledgeClaim(claim: CharacterKnowledgeClaim) {
        connect { connection -> insertKnowledgeClaim(connection, claim) }
    }

    fun getSceneLifecycle(sessionId: String): SceneLifecycle = connect { connection ->
        val session = selectSession(connection, sessionId) ?: error("Session not found: $sessionId")
        val instanceId = session.currentSceneInstanceId ?: ensureActiveSceneInstance(connection, session)
        connection.prepareStatement(
            """
            SELECT id, scene_id, visit_number, status, started_turn_number, summary
            FROM scene_instances
            WHERE id = ?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, instanceId)
            statement.executeQuery().use { rows ->
                if (!rows.next()) return@connect SceneLifecycle()
                val status = rows.getString("status")
                val reasons = if (rows.getString("summary").isNullOrBlank()) emptyList() else listOf(rows.getString("summary"))
                SceneLifecycle(
                    sceneInstanceId = rows.getString("id"),
                    sceneId = rows.getString("scene_id"),
                    visitNumber = rows.getInt("visit_number"),
                    status = status,
                    startedTurnNumber = rows.getInt("started_turn_number"),
                    readyToClose = status == "READY_TO_CLOSE",
                    closureReasons = reasons,
                )
            }
        }
    }

    fun listSceneRequirementViews(sessionId: String): List<SceneRequirementView> = connect { connection ->
        val session = selectSession(connection, sessionId) ?: error("Session not found: $sessionId")
        val instanceId = session.currentSceneInstanceId ?: ensureActiveSceneInstance(connection, session)
        connection.prepareStatement(
            """
            SELECT r.id, r.requirement_type, r.description, r.visibility, s.status, s.progress
            FROM scene_requirements r
            LEFT JOIN session_scene_requirement_states s
              ON s.requirement_id = r.id AND s.session_id = ? AND s.scene_instance_id = ?
            WHERE r.scene_id = ?
            ORDER BY r.priority DESC, r.id ASC
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, session.id)
            statement.setString(2, instanceId)
            statement.setString(3, session.currentSceneId)
            statement.executeQuery().use { rows ->
                buildList {
                    while (rows.next()) {
                        val status = rows.getString("status") ?: "OPEN"
                        add(
                            SceneRequirementView(
                                requirementId = rows.getString("id"),
                                type = rows.getString("requirement_type"),
                                description = rows.getString("description"),
                                status = status,
                                progress = rows.getInt("progress"),
                                remainingCondition = if (status == "COMPLETED") "" else rows.getString("description"),
                                directorOnly = rows.getString("visibility") != "public",
                            ),
                        )
                    }
                }
            }
        }
    }

    fun listActiveStoryThreadViews(sessionId: String): List<ActiveStoryThreadView> = connect { connection ->
        val session = selectSession(connection, sessionId) ?: error("Session not found: $sessionId")
        connection.prepareStatement(
            """
            SELECT t.id, t.thread_type, t.public_summary, s.status, s.progress, s.urgency
            FROM story_threads t
            JOIN session_thread_states s ON s.thread_id = t.id
            WHERE s.session_id = ? AND t.project_id = ? AND s.status NOT IN ('RESOLVED', 'FAILED', 'ABANDONED')
            ORDER BY t.priority DESC, t.id ASC
            LIMIT 3
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, session.id)
            statement.setString(2, session.projectId)
            statement.executeQuery().use { rows ->
                buildList {
                    while (rows.next()) {
                        add(
                            ActiveStoryThreadView(
                                threadId = rows.getString("id"),
                                type = rows.getString("thread_type"),
                                publicSummary = rows.getString("public_summary"),
                                status = rows.getString("status"),
                                progress = rows.getInt("progress"),
                                urgency = rows.getInt("urgency"),
                                directorGuidance = "Use evidence; do not override character goals.",
                            ),
                        )
                    }
                }
            }
        }
    }

    fun listAvailableTransitions(sessionId: String, playerInput: String): List<AvailableTransition> = connect { connection ->
        val session = selectSession(connection, sessionId) ?: error("Session not found: $sessionId")
        selectAvailableTransitions(connection, session, playerInput)
    }

    fun listPendingWorldEventViews(sessionId: String): List<PendingWorldEventView> = connect { connection ->
        val session = selectSession(connection, sessionId) ?: error("Session not found: $sessionId")
        selectPendingWorldEvents(connection, session)
    }

    fun getContinuityConstraints(sessionId: String): ContinuityConstraints = connect { connection ->
        val session = selectSession(connection, sessionId) ?: error("Session not found: $sessionId")
        val scene = getActiveScene(session.projectId)
        val characters = listCharacterStates(session.projectId).associate { it.characterId to it.locationId }
        val items = listItems(session.projectId).associate { it.id to (it.ownerCharacterId ?: it.locationId) }
        val closedSummaries = connection.prepareStatement(
            """
            SELECT summary FROM scene_instances
            WHERE session_id = ? AND status IN ('CLOSED', 'FAILED', 'ABORTED') AND summary != ''
            ORDER BY ended_turn_number DESC, updated_at DESC
            LIMIT 3
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, session.id)
            statement.executeQuery().use { rows ->
                buildList {
                    while (rows.next()) add(rows.getString("summary").take(240))
                }
            }
        }
        ContinuityConstraints(
            currentSceneInstanceId = session.currentSceneInstanceId.orEmpty(),
            currentSceneLocationId = scene?.locationId,
            characterLocations = characters,
            itemLocations = items,
            closedSceneSummaries = closedSummaries,
        )
    }

    fun activeSceneInstanceCount(sessionId: String): Int = connect { connection ->
        connection.prepareStatement("SELECT COUNT(*) FROM scene_instances WHERE session_id = ? AND status IN ('ACTIVE', 'READY_TO_CLOSE')").use { statement ->
            statement.setString(1, sessionId)
            statement.executeQuery().use { rows ->
                rows.next()
                rows.getInt(1)
            }
        }
    }

    fun tableRows(tableName: String): List<Map<String, String>> = connect { connection ->
        require(tableName.matches(Regex("[a-zA-Z_]+"))) { "Unsafe table name: $tableName" }
        connection.prepareStatement("SELECT * FROM $tableName").use { statement ->
            statement.executeQuery().use { rows ->
                val meta = rows.metaData
                buildList {
                    while (rows.next()) {
                        add(
                            (1..meta.columnCount).associate { index ->
                                meta.getColumnName(index) to (rows.getString(index) ?: "")
                            },
                        )
                    }
                }
            }
        }
    }

    fun getSession(sessionId: String): StorySession? = connect { connection ->
        connection.prepareStatement(
            """
            SELECT id, project_id, current_scene_id, next_turn_number, state_version, status,
                   current_scene_instance_id, world_time, elapsed_minutes, branch_id
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

    fun isSessionActiveForMutation(sessionId: String): Boolean = connect { connection ->
        val session = selectSession(connection, sessionId) ?: return@connect false
        val branchId = session.branchId ?: return@connect true
        connection.prepareStatement(
            """
            SELECT 1
            FROM timeline_branches
            WHERE id = ? AND project_id = ? AND status = 'ACTIVE' AND active_session_id = ?
            LIMIT 1
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, branchId)
            statement.setString(2, session.projectId)
            statement.setString(3, sessionId)
            statement.executeQuery().use { it.next() }
        }
    }

    fun startSession(projectId: String, sceneId: String): StorySession = connect { connection ->
        connection.autoCommit = false
        try {
            val sessionId = newId("session")
            val sceneInstanceId = newId("scene-instance")
            val branchId = newId("branch")
            val session = StorySession(
                id = sessionId,
                projectId = projectId,
                currentSceneId = sceneId,
                nextTurnNumber = 1,
                stateVersion = 0,
                branchId = branchId,
                currentSceneInstanceId = sceneInstanceId,
                worldTime = "0001-01-01T00:00:00Z",
                elapsedMinutes = 0,
            )
            val timestamp = now()
            val startScene = selectScene(connection, projectId, sceneId)
                ?: error("Scene not found: $sceneId")
            connection.prepareStatement(
                "UPDATE timeline_branches SET status = 'INACTIVE', updated_at = ? WHERE project_id = ? AND status = 'ACTIVE'",
            ).use { statement ->
                statement.setString(1, timestamp)
                statement.setString(2, projectId)
                statement.executeUpdate()
            }
            insertTimelineBranch(
                connection = connection,
                branch = TimelineBranch(
                    id = branchId,
                    projectId = projectId,
                    label = "main",
                    status = "ACTIVE",
                    activeSessionId = null,
                    createdFromSessionId = null,
                    createdFromTurnNumber = 0,
                    createdFromStateVersion = 0,
                    reason = "start-session",
                    createdAt = timestamp,
                    updatedAt = timestamp,
                ),
            )
            connection.prepareStatement(
                """
                INSERT INTO story_sessions(
                    id, project_id, current_scene_id, next_turn_number, state_version, status,
                    current_scene_instance_id, world_time, elapsed_minutes, branch_id, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, session.id)
                statement.setString(2, session.projectId)
                statement.setString(3, session.currentSceneId)
                statement.setInt(4, session.nextTurnNumber)
                statement.setInt(5, session.stateVersion)
                statement.setString(6, session.status)
                statement.setString(7, sceneInstanceId)
                statement.setString(8, session.worldTime)
                statement.setInt(9, session.elapsedMinutes)
                statement.setString(10, branchId)
                statement.setString(11, timestamp)
                statement.setString(12, timestamp)
                statement.executeUpdate()
            }
            connection.prepareStatement("UPDATE timeline_branches SET active_session_id = ?, updated_at = ? WHERE id = ?").use { statement ->
                statement.setString(1, sessionId)
                statement.setString(2, timestamp)
                statement.setString(3, branchId)
                statement.executeUpdate()
            }
            connection.prepareStatement(
                "UPDATE projects SET active_scene_id = ?, updated_at = ? WHERE id = ?",
            ).use { statement ->
                statement.setString(1, sceneId)
                statement.setString(2, timestamp)
                statement.setString(3, projectId)
                statement.executeUpdate()
            }
            if (startScene.locationId != null) {
                connection.prepareStatement(
                    "UPDATE character_states SET location_id = ?, updated_at = ? WHERE project_id = ?",
                ).use { statement ->
                    statement.setString(1, startScene.locationId)
                    statement.setString(2, timestamp)
                    statement.setString(3, projectId)
                    statement.executeUpdate()
                }
                initializeOpeningRuntimeStates(connection, projectId, startScene.locationId, timestamp)
            }
            insertSceneInstance(connection, session.id, sceneId, sceneInstanceId, 1, 1, "start-session")
            initializeSceneRequirementStates(connection, session.id, sceneInstanceId, sceneId)
            initializeSessionThreadStates(connection, session.id, projectId)
            assignSessionlessWorldEvents(connection, projectId, session.id)
            connection.commit()
            session
        } catch (error: Throwable) {
            connection.rollback()
            throw error
        } finally {
            connection.autoCommit = true
        }
    }

    private fun initializeOpeningRuntimeStates(connection: Connection, projectId: String, openingLocationId: String, timestamp: String) {
        val charactersById = selectCharacters(connection, projectId).associateBy { it.id }
        val openingCharacterIds = buildSet {
            connection.prepareStatement(
                """
                SELECT from_id
                FROM dependency_edges
                WHERE project_id = ?
                  AND from_type = 'character'
                  AND to_type = 'location'
                  AND to_id = ?
                  AND UPPER(relation) = 'LOCATED_IN'
                  AND status = 'active'
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, projectId)
                statement.setString(2, openingLocationId)
                statement.executeQuery().use { rows ->
                    while (rows.next()) add(rows.getString("from_id"))
                }
            }
            charactersById.values
                .filter { it.initialRuntimeTemplate()?.initialLocationId == openingLocationId }
                .forEach { add(it.id) }
        }
        openingCharacterIds.sorted().forEach { characterId ->
            if (selectCharacterState(connection, projectId, characterId) != null) return@forEach
            val character = charactersById[characterId] ?: error("MISSING_INITIAL_RUNTIME_TEMPLATE: opening character not found: $characterId")
            val template = character.initialRuntimeTemplate()
                ?: error("MISSING_INITIAL_RUNTIME_TEMPLATE: opening active character '$characterId' requires initialRuntimeTemplate")
            if (template.initialLocationId != openingLocationId) {
                error("INVALID_INITIAL_CHARACTER_LOCATION: '$characterId' initialLocationId '${template.initialLocationId}' does not match opening location '$openingLocationId'")
            }
            if (template.initialGoal.isBlank() || template.dominantEmotion.isBlank()) {
                error("MISSING_INITIAL_RUNTIME_TEMPLATE: '$characterId' initialGoal and dominantEmotion are required")
            }
            if (template.emotionState.values.any { it !in 0..100 } || template.initiativeLevel !in 0..100) {
                error("INVALID_INITIAL_RUNTIME_TEMPLATE: '$characterId' has invalid emotionState or initiativeLevel")
            }
            val flags = buildMap {
                put("current_goal", template.initialGoal)
                put("dominant_emotion", template.dominantEmotion)
                put("available_actions", JsonSupport.json.encodeToString(template.availableActions))
                put("relationship_defaults", JsonSupport.json.encodeToString(template.relationshipDefaults))
                put("initiative_level", template.initiativeLevel.toString())
                template.emotionState.forEach { (emotion, value) -> put("emotion.$emotion", value.toString()) }
            }
            insertCharacterState(
                connection,
                CharacterState(
                    characterId = characterId,
                    projectId = projectId,
                    locationId = template.initialLocationId,
                    mood = template.dominantEmotion,
                    health = "normal",
                    inventory = selectItemsOwnedBy(connection, projectId, characterId),
                    flags = flags,
                ),
            )
        }
    }

    fun savePendingContext(context: ContextPacket) {
        connect { connection ->
            connection.prepareStatement(
                """
                INSERT INTO turn_logs(
                    id, project_id, turn_number, player_input, context_json, result_json, narrative,
                    has_state_change, created_at, session_id, context_id, base_state_version, context_hash,
                    turn_summary, validation_status, branch_id
                )
                VALUES (?, ?, ?, ?, ?, '', '', 0, ?, ?, ?, ?, ?, '', 'pending', ?)
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
                statement.setString(11, context.branchId)
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
            "DELETE FROM turn_logs WHERE session_id = ? AND validation_status = 'pending'"
        } else {
            "DELETE FROM turn_logs WHERE session_id = ? AND context_id = ? AND validation_status = 'pending'"
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

    fun activeSessionIds(projectId: String): List<String> = connect { connection ->
        connection.prepareStatement(
            "SELECT id FROM story_sessions WHERE project_id = ? AND status = 'active' ORDER BY id ASC",
        ).use { statement ->
            statement.setString(1, projectId)
            statement.executeQuery().use { rows ->
                buildList {
                    while (rows.next()) add(rows.getString("id"))
                }
            }
        }
    }

    fun closeSession(sessionId: String) {
        connect { connection ->
            connection.prepareStatement("UPDATE story_sessions SET status = 'closed', updated_at = ? WHERE id = ?").use { statement ->
                statement.setString(1, now())
                statement.setString(2, sessionId)
                statement.executeUpdate()
            }
        }
    }

    fun settingPackHash(packId: String, packVersion: Int): String? = connect { connection ->
        selectSettingPackHash(connection, packId, packVersion)
    }

    fun settingPackJson(packId: String, packVersion: Int): String? = connect { connection ->
        connection.prepareStatement("SELECT pack_json FROM setting_packs WHERE pack_id = ? AND pack_version = ?").use { statement ->
            statement.setString(1, packId)
            statement.setInt(2, packVersion)
            statement.executeQuery().use { rows -> if (rows.next()) rows.getString("pack_json") else null }
        }
    }

    fun settingPackRows(projectId: String): List<Map<String, String>> = connect { connection ->
        connection.prepareStatement(
            """
            SELECT pack_id, pack_version, project_id, name, mode, canonical_hash, created_at
            FROM setting_packs
            WHERE project_id = ?
            ORDER BY pack_id ASC, pack_version ASC
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, projectId)
            statement.executeQuery().use { rows ->
                val meta = rows.metaData
                buildList {
                    while (rows.next()) {
                        add((1..meta.columnCount).associate { index -> meta.getColumnName(index) to (rows.getString(index) ?: "") })
                    }
                }
            }
        }
    }

    fun latestEntityRevision(projectId: String, entityType: String, entityId: String): Map<String, String>? = connect { connection ->
        connection.prepareStatement(
            """
            SELECT id, project_id, entity_type, entity_id, entity_version, source_pack_id,
                   source_pack_version, canonical_hash, snapshot_json, visibility, status, created_at
            FROM entity_revisions
            WHERE project_id = ? AND entity_type = ? AND entity_id = ?
            ORDER BY entity_version DESC, created_at DESC
            LIMIT 1
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, projectId)
            statement.setString(2, entityType)
            statement.setString(3, entityId)
            statement.executeQuery().use { rows ->
                if (!rows.next()) return@connect null
                val meta = rows.metaData
                (1..meta.columnCount).associate { index -> meta.getColumnName(index) to (rows.getString(index) ?: "") }
            }
        }
    }

    fun listEntityRevisionRows(projectId: String, entityType: String? = null): List<Map<String, String>> = connect { connection ->
        val sql = if (entityType == null) {
            """
            SELECT id, project_id, entity_type, entity_id, entity_version, source_pack_id,
                   source_pack_version, canonical_hash, snapshot_json, visibility, status, created_at
            FROM entity_revisions
            WHERE project_id = ?
            ORDER BY entity_type ASC, entity_id ASC, entity_version ASC
            """.trimIndent()
        } else {
            """
            SELECT id, project_id, entity_type, entity_id, entity_version, source_pack_id,
                   source_pack_version, canonical_hash, snapshot_json, visibility, status, created_at
            FROM entity_revisions
            WHERE project_id = ? AND entity_type = ?
            ORDER BY entity_id ASC, entity_version ASC
            """.trimIndent()
        }
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, projectId)
            if (entityType != null) statement.setString(2, entityType)
            statement.executeQuery().use { rows ->
                val meta = rows.metaData
                buildList {
                    while (rows.next()) {
                        add((1..meta.columnCount).associate { index -> meta.getColumnName(index) to (rows.getString(index) ?: "") })
                    }
                }
            }
        }
    }

    fun listDependencyRows(projectId: String): List<Map<String, String>> = connect { connection ->
        connection.prepareStatement(
            """
            SELECT *
            FROM dependency_edges
            WHERE project_id = ? AND status = 'active'
            ORDER BY from_type ASC, from_id ASC, relation ASC, to_type ASC, to_id ASC
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, projectId)
            statement.executeQuery().use { rows ->
                val meta = rows.metaData
                buildList {
                    while (rows.next()) {
                        add((1..meta.columnCount).associate { index -> meta.getColumnName(index) to (rows.getString(index) ?: "") })
                    }
                }
            }
        }
    }

    fun applySettingPack(preview: SettingPackPreview, sourcePath: String = ""): SettingPackApplyResult = connect { connection ->
        connection.autoCommit = false
        try {
            val pack = preview.pack
            val importId = newId("setting-import")
            val existingHash = selectSettingPackHash(connection, pack.packId, pack.packVersion)
            if (existingHash != null && existingHash != preview.canonicalHash) {
                error("Setting pack ${pack.packId} v${pack.packVersion} already exists with a different canonical hash.")
            }
            if (existingHash == preview.canonicalHash) {
                insertSettingPackImport(connection, importId, pack, preview, sourcePath, "no_change")
                finishSettingPackImport(connection, importId, "no_change")
                connection.commit()
                return@connect SettingPackApplyResult(false, importId, pack.packId, pack.packVersion, preview.canonicalHash, preview.diff, preview.impact)
            }

            ensureProjectForPack(connection, pack)
            insertSettingPackImport(connection, importId, pack, preview, sourcePath, "applying")
            insertSettingPackRecord(connection, pack, preview.canonicalHash)
            applyPackEntities(connection, pack, preview.canonicalHash)
            pack.locationConnections.forEach { insertLocationConnection(connection, it) }
            pack.sceneTransitions.forEach { insertSceneTransition(connection, it) }
            pack.scheduledWorldEvents.forEach { insertScheduledWorldEvent(connection, it) }
            pack.relations.forEach { insertSettingPackRelation(connection, pack, it) }
            finishSettingPackImport(connection, importId, "applied")
            connection.commit()
            SettingPackApplyResult(true, importId, pack.packId, pack.packVersion, preview.canonicalHash, preview.diff, preview.impact)
        } catch (error: Throwable) {
            connection.rollback()
            throw error
        } finally {
            connection.autoCommit = true
        }
    }

    fun resolvePendingTurn(context: ContextPacket, result: AiTurnResult, hasStateChange: Boolean): ApplyResult = connect { connection ->
        connection.autoCommit = false
        try {
            val session = selectSession(connection, context.sessionId)
                ?: error("Session not found: ${context.sessionId}")
            require(session.branchId == null || selectBranchIsActive(connection, session.projectId, session.branchId, context.sessionId)) {
                "Session is not on the active timeline branch: ${context.sessionId}"
            }
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
            applyMemoryCandidates(connection, context, result.memoryCandidates)
            applyKnowledgeUpdates(connection, context, result)
            applySceneRequirementUpdates(connection, context, result)
            applyThreadProgressUpdates(connection, context, result)
            applyWorldEventAcknowledgements(connection, context, result)
            applySceneTransition(connection, context, result)
            applyWorldClock(connection, context, result)
            recordSuccessfulRecall(connection, context)
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
            "project", "projects" -> "projects"
            "character", "characters" -> "characters"
            "character_state", "character_states" -> "character_states"
            "world_rule", "world_rules" -> "world_rules"
            "faction", "factions" -> "factions"
            "location", "locations" -> "locations"
            "fact", "facts" -> "facts"
            "secret", "secrets" -> "secrets"
            "event", "events" -> "events"
            "task", "task_queue", "tasks" -> "task_queue"
            "item", "items" -> "items"
            "technique", "techniques" -> "techniques"
            "scene", "scenes" -> "scenes"
            "memory", "character_memory", "character_memories" -> "character_memories"
            "knowledge", "character_knowledge" -> "character_knowledge"
            "scene_instance", "scene_instances" -> "scene_instances"
            "scene_requirement", "scene_requirements" -> "scene_requirements"
            "location_connection", "location_connections" -> "location_connections"
            "story_thread", "story_threads" -> "story_threads"
            "scene_transition", "scene_transitions" -> "scene_transitions"
            "world_event", "scheduled_world_events" -> "scheduled_world_events"
            else -> return@connect false
        }
        if (table == "projects") {
            return@connect connection.prepareStatement("SELECT 1 FROM projects WHERE id = ? LIMIT 1").use { statement ->
                statement.setString(1, targetId)
                statement.executeQuery().use { it.next() }
            }
        }
        if (table == "scene_instances") {
            return@connect connection.prepareStatement("SELECT 1 FROM scene_instances WHERE id = ? LIMIT 1").use { statement ->
                statement.setString(1, targetId)
                statement.executeQuery().use { it.next() }
            }
        }
        if (table == "scene_requirements") {
            return@connect connection.prepareStatement(
                """
                SELECT 1
                FROM scene_requirements r
                JOIN scenes s ON s.id = r.scene_id
                WHERE s.project_id = ? AND r.id = ?
                LIMIT 1
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, projectId)
                statement.setString(2, targetId)
                statement.executeQuery().use { it.next() }
            }
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

    internal fun <T> withConnection(block: (Connection) -> T): T = connect(block)

    internal fun <T> withTransaction(block: (Connection) -> T): T = connect { connection ->
        connection.autoCommit = false
        try {
            val result = block(connection)
            connection.commit()
            result
        } catch (error: Throwable) {
            connection.rollback()
            throw error
        } finally {
            connection.autoCommit = true
        }
    }

    internal fun queryRows(connection: Connection, sql: String, vararg args: String?): List<Map<String, String?>> =
        connection.prepareStatement(sql).use { statement ->
            args.forEachIndexed { index, value -> statement.setString(index + 1, value) }
            statement.executeQuery().use { rows ->
                val meta = rows.metaData
                buildList {
                    while (rows.next()) {
                        add(
                            (1..meta.columnCount).associate { index ->
                                meta.getColumnName(index) to rows.getString(index)
                            }.toSortedMap(),
                        )
                    }
                }
            }
        }

    internal fun executeUpdate(connection: Connection, sql: String, vararg args: String?): Int =
        connection.prepareStatement(sql).use { statement ->
            args.forEachIndexed { index, value -> statement.setString(index + 1, value) }
            statement.executeUpdate()
        }

    internal fun insertRawRow(connection: Connection, tableName: String, row: Map<String, String?>) {
        require(tableName.matches(Regex("[a-zA-Z_]+"))) { "Unsafe table name: $tableName" }
        val columns = row.keys.toList()
        require(columns.all { it.matches(Regex("[a-zA-Z_]+")) }) { "Unsafe column in $tableName" }
        val placeholders = columns.joinToString(", ") { "?" }
        val sql = "INSERT OR REPLACE INTO $tableName(${columns.joinToString(", ")}) VALUES ($placeholders)"
        connection.prepareStatement(sql).use { statement ->
            columns.forEachIndexed { index, column ->
                val value = row[column]
                if (value == null) statement.setNull(index + 1, java.sql.Types.VARCHAR) else statement.setString(index + 1, value)
            }
            statement.executeUpdate()
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

    private fun insertTimelineBranch(connection: Connection, branch: TimelineBranch) {
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

    private fun insertProject(connection: Connection, project: Project) {
        connection.prepareStatement(
            """
            INSERT INTO projects(id, name, summary, active_scene_id, current_turn, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                name = excluded.name,
                summary = excluded.summary,
                active_scene_id = excluded.active_scene_id,
                current_turn = excluded.current_turn,
                updated_at = excluded.updated_at
            """.trimIndent(),
        ).use { statement ->
            val timestamp = now()
            statement.setString(1, project.id)
            statement.setString(2, project.name)
            statement.setString(3, project.summary)
            statement.setString(4, project.activeSceneId)
            statement.setInt(5, project.currentTurn)
            statement.setString(6, timestamp)
            statement.setString(7, timestamp)
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
            INSERT OR REPLACE INTO character_memories(
                id, project_id, character_id, owner_character_id, memory_type, summary, details_json,
                source_type, source_id, subject_type, subject_id, tags_json, trigger_tags_json,
                emotional_signature_json, salience, confidence, reliability, visibility, created_turn,
                created_turn_number, last_recalled_turn_number, recall_count, decay_rate, status,
                is_canon, supersedes_memory_id, created_at, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, COALESCE((SELECT created_at FROM character_memories WHERE id = ?), ?), ?)
            """.trimIndent(),
        ).use { statement ->
            val timestamp = now()
            statement.setString(1, memory.id)
            statement.setString(2, memory.projectId)
            statement.setString(3, memory.characterId)
            statement.setString(4, memory.ownerCharacterId)
            statement.setString(5, memory.memoryType)
            statement.setString(6, memory.summary)
            statement.setString(7, JsonSupport.json.encodeToString(memory.details))
            statement.setString(8, memory.sourceType)
            statement.setString(9, memory.sourceId)
            statement.setString(10, memory.subjectType)
            statement.setString(11, memory.subjectId)
            statement.setString(12, JsonSupport.json.encodeToString(memory.tags))
            statement.setString(13, JsonSupport.json.encodeToString(memory.triggerTags))
            statement.setString(14, JsonSupport.json.encodeToString(memory.emotionalSignature))
            statement.setInt(15, memory.salience.coerceIn(0, 100))
            statement.setInt(16, memory.confidence.coerceIn(0, 100))
            statement.setInt(17, memory.reliability.coerceIn(0, 100))
            statement.setString(18, memory.visibility)
            statement.setInt(19, memory.createdTurn)
            statement.setInt(20, memory.createdTurn)
            if (memory.lastRecalledTurnNumber == null) {
                statement.setNull(21, java.sql.Types.INTEGER)
            } else {
                statement.setInt(21, memory.lastRecalledTurnNumber)
            }
            statement.setInt(22, memory.recallCount)
            statement.setDouble(23, memory.decayRate)
            statement.setString(24, memory.status)
            statement.setInt(25, if (memory.isCanon) 1 else 0)
            statement.setString(26, memory.supersedesMemoryId)
            statement.setString(27, memory.id)
            statement.setString(28, timestamp)
            statement.setString(29, timestamp)
            statement.executeUpdate()
        }
    }

    private fun insertKnowledgeClaim(connection: Connection, claim: CharacterKnowledgeClaim) {
        connection.prepareStatement(
            """
            INSERT OR REPLACE INTO character_knowledge(
                id, project_id, character_id, proposition_key, statement, epistemic_state, confidence,
                source_fact_id, source_memory_id, acquired_turn_number, updated_turn_number,
                visibility, status, created_at, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, COALESCE((SELECT created_at FROM character_knowledge WHERE id = ?), ?), ?)
            """.trimIndent(),
        ).use { statement ->
            val timestamp = now()
            statement.setString(1, claim.id)
            statement.setString(2, claim.projectId)
            statement.setString(3, claim.characterId)
            statement.setString(4, claim.propositionKey)
            statement.setString(5, claim.statement)
            statement.setString(6, claim.epistemicState)
            statement.setInt(7, claim.confidence.coerceIn(0, 100))
            statement.setString(8, claim.sourceFactId)
            statement.setString(9, claim.sourceMemoryId)
            statement.setInt(10, claim.acquiredTurnNumber)
            statement.setInt(11, claim.updatedTurnNumber)
            statement.setString(12, claim.visibility)
            statement.setString(13, claim.status)
            statement.setString(14, claim.id)
            statement.setString(15, timestamp)
            statement.setString(16, timestamp)
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

    private fun insertStoryThread(connection: Connection, thread: StoryThread) {
        connection.prepareStatement(
            """
            INSERT OR REPLACE INTO story_threads(
                id, project_id, thread_type, title, director_goal, public_summary,
                priority, visibility, parent_thread_id, created_at, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, COALESCE((SELECT created_at FROM story_threads WHERE id = ?), ?), ?)
            """.trimIndent(),
        ).use { statement ->
            val timestamp = now()
            statement.setString(1, thread.id)
            statement.setString(2, thread.projectId)
            statement.setString(3, thread.threadType)
            statement.setString(4, thread.title)
            statement.setString(5, thread.directorGoal)
            statement.setString(6, thread.publicSummary)
            statement.setInt(7, thread.priority)
            statement.setString(8, thread.visibility)
            statement.setString(9, thread.parentThreadId)
            statement.setString(10, thread.id)
            statement.setString(11, timestamp)
            statement.setString(12, timestamp)
            statement.executeUpdate()
        }
    }

    private fun insertSceneRequirement(connection: Connection, requirement: SceneRequirementDefinition) {
        connection.prepareStatement(
            """
            INSERT OR REPLACE INTO scene_requirements(
                id, scene_id, requirement_type, description, completion_condition_json,
                failure_condition_json, priority, visibility, created_at, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, COALESCE((SELECT created_at FROM scene_requirements WHERE id = ?), ?), ?)
            """.trimIndent(),
        ).use { statement ->
            val timestamp = now()
            statement.setString(1, requirement.id)
            statement.setString(2, requirement.sceneId)
            statement.setString(3, requirement.requirementType)
            statement.setString(4, requirement.description)
            statement.setString(5, JsonSupport.json.encodeToString(requirement.completionCondition))
            statement.setString(6, JsonSupport.json.encodeToString(requirement.failureCondition))
            statement.setInt(7, requirement.priority)
            statement.setString(8, requirement.visibility)
            statement.setString(9, requirement.id)
            statement.setString(10, timestamp)
            statement.setString(11, timestamp)
            statement.executeUpdate()
        }
    }

    private fun insertLocationConnection(connection: Connection, connectionDef: LocationConnection) {
        connection.prepareStatement(
            """
            INSERT OR REPLACE INTO location_connections(
                id, project_id, from_location_id, to_location_id, connection_type,
                bidirectional, travel_minutes, prerequisites_json, restrictions_json,
                visibility, created_at, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, COALESCE((SELECT created_at FROM location_connections WHERE id = ?), ?), ?)
            """.trimIndent(),
        ).use { statement ->
            val timestamp = now()
            statement.setString(1, connectionDef.id)
            statement.setString(2, connectionDef.projectId)
            statement.setString(3, connectionDef.fromLocationId)
            statement.setString(4, connectionDef.toLocationId)
            statement.setString(5, connectionDef.connectionType)
            statement.setInt(6, if (connectionDef.bidirectional) 1 else 0)
            statement.setInt(7, connectionDef.travelMinutes)
            statement.setString(8, JsonSupport.json.encodeToString(connectionDef.prerequisites))
            statement.setString(9, JsonSupport.json.encodeToString(connectionDef.restrictions))
            statement.setString(10, connectionDef.visibility)
            statement.setString(11, connectionDef.id)
            statement.setString(12, timestamp)
            statement.setString(13, timestamp)
            statement.executeUpdate()
        }
    }

    private fun insertSceneTransition(connection: Connection, transition: SceneTransitionDefinition) {
        connection.prepareStatement(
            """
            INSERT OR REPLACE INTO scene_transitions(
                id, project_id, from_scene_id, to_scene_id, transition_type, prerequisites_json,
                costs_json, required_player_intent, forced_transition_allowed, visibility,
                created_at, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, COALESCE((SELECT created_at FROM scene_transitions WHERE id = ?), ?), ?)
            """.trimIndent(),
        ).use { statement ->
            val timestamp = now()
            statement.setString(1, transition.id)
            statement.setString(2, transition.projectId)
            statement.setString(3, transition.fromSceneId)
            statement.setString(4, transition.toSceneId)
            statement.setString(5, transition.transitionType)
            statement.setString(6, JsonSupport.json.encodeToString(transition.prerequisites))
            statement.setString(7, JsonSupport.json.encodeToString(transition.costs))
            statement.setString(8, transition.requiredPlayerIntent)
            statement.setInt(9, if (transition.forcedTransitionAllowed) 1 else 0)
            statement.setString(10, transition.visibility)
            statement.setString(11, transition.id)
            statement.setString(12, timestamp)
            statement.setString(13, timestamp)
            statement.executeUpdate()
        }
    }

    private fun insertScheduledWorldEvent(connection: Connection, event: ScheduledWorldEventSeed) {
        connection.prepareStatement(
            """
            INSERT OR REPLACE INTO scheduled_world_events(
                id, project_id, session_id, event_type, due_turn_number, due_world_time,
                trigger_conditions_json, payload_json, visibility, status,
                triggered_turn_number, consumed_turn_number, created_at, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, NULL, COALESCE((SELECT created_at FROM scheduled_world_events WHERE id = ?), ?), ?)
            """.trimIndent(),
        ).use { statement ->
            val timestamp = now()
            statement.setString(1, event.id)
            statement.setString(2, event.projectId)
            statement.setString(3, event.sessionId)
            statement.setString(4, event.eventType)
            statement.setInt(5, event.dueTurnNumber)
            statement.setString(6, event.dueWorldTime)
            statement.setString(7, JsonSupport.json.encodeToString(event.triggerConditions))
            statement.setString(8, JsonSupport.json.encodeToString(event.payload))
            statement.setString(9, event.visibility)
            statement.setString(10, event.status)
            statement.setString(11, event.id)
            statement.setString(12, timestamp)
            statement.setString(13, timestamp)
            statement.executeUpdate()
        }
    }

    private fun selectSettingPackHash(connection: Connection, packId: String, packVersion: Int): String? =
        connection.prepareStatement("SELECT canonical_hash FROM setting_packs WHERE pack_id = ? AND pack_version = ?").use { statement ->
            statement.setString(1, packId)
            statement.setInt(2, packVersion)
            statement.executeQuery().use { rows -> if (rows.next()) rows.getString("canonical_hash") else null }
        }

    private fun insertSettingPackImport(
        connection: Connection,
        importId: String,
        pack: SettingPack,
        preview: SettingPackPreview,
        sourcePath: String,
        status: String,
    ) {
        connection.prepareStatement(
            """
            INSERT INTO setting_pack_imports(
                id, pack_id, pack_version, project_id, mode, canonical_hash, source_path,
                status, validation_json, diff_json, impact_json, created_at, applied_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, importId)
            statement.setString(2, pack.packId)
            statement.setInt(3, pack.packVersion)
            statement.setString(4, pack.projectId)
            statement.setString(5, pack.mode)
            statement.setString(6, preview.canonicalHash)
            statement.setString(7, sourcePath)
            statement.setString(8, status)
            statement.setString(9, JsonSupport.json.encodeToString(preview.validation))
            statement.setString(10, JsonSupport.json.encodeToString(preview.diff))
            statement.setString(11, JsonSupport.json.encodeToString(preview.impact))
            statement.setString(12, now())
            statement.executeUpdate()
        }
    }

    private fun finishSettingPackImport(connection: Connection, importId: String, status: String) {
        connection.prepareStatement("UPDATE setting_pack_imports SET status = ?, applied_at = ? WHERE id = ?").use { statement ->
            statement.setString(1, status)
            statement.setString(2, now())
            statement.setString(3, importId)
            statement.executeUpdate()
        }
    }

    private fun insertSettingPackRecord(connection: Connection, pack: SettingPack, canonicalHash: String) {
        connection.prepareStatement(
            """
            INSERT INTO setting_packs(
                pack_id, pack_version, project_id, name, description, mode, canonical_hash,
                pack_json, source_json, metadata_json, created_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, pack.packId)
            statement.setInt(2, pack.packVersion)
            statement.setString(3, pack.projectId)
            statement.setString(4, pack.name)
            statement.setString(5, pack.description)
            statement.setString(6, pack.mode)
            statement.setString(7, canonicalHash)
            statement.setString(8, JsonSupport.json.encodeToString(pack))
            statement.setString(9, JsonSupport.json.encodeToString(pack.source))
            statement.setString(10, JsonSupport.json.encodeToString(pack.metadata))
            statement.setString(11, now())
            statement.executeUpdate()
        }
    }

    private fun ensureProjectForPack(connection: Connection, pack: SettingPack) {
        val existing = connection.prepareStatement("SELECT 1 FROM projects WHERE id = ? LIMIT 1").use { statement ->
            statement.setString(1, pack.projectId)
            statement.executeQuery().use { it.next() }
        }
        if (existing) return
        val projectEntity = pack.entities.projects.firstOrNull { it.id == pack.projectId } ?: pack.entities.projects.firstOrNull()
        val activeSceneId = pack.entities.scenes.firstOrNull()?.id
        insertProject(
            connection,
            Project(
                id = pack.projectId,
                name = projectEntity?.displayName() ?: pack.name,
                summary = projectEntity?.bodyText()?.ifBlank { pack.description } ?: pack.description.ifBlank { pack.name },
                activeSceneId = activeSceneId,
                currentTurn = 0,
            ),
        )
    }

    private fun applyPackEntities(connection: Connection, pack: SettingPack, packHash: String) {
        pack.entities.projects.forEach { entity ->
            insertProject(
                connection,
                Project(
                    id = entity.id,
                    name = entity.displayName(),
                    summary = entity.bodyText().ifBlank { entity.summary.ifBlank { pack.description } },
                    activeSceneId = entity.metadata.stringValue("activeSceneId") ?: pack.entities.scenes.firstOrNull()?.id,
                ),
            )
            recordEntityRevision(connection, pack, "project", entity, packHash)
        }
        pack.entities.locations.forEach { entity ->
            insertLocation(connection, Location(entity.id, pack.projectId, entity.displayName(), entity.bodyText(), entity.parentLocationId))
            recordEntityRevision(connection, pack, "location", entity, packHash)
        }
        pack.entities.factions.forEach { entity ->
            insertFaction(connection, Faction(entity.id, pack.projectId, entity.displayName(), entity.bodyText(), entity.metadata.stringValue("stance") ?: "neutral"))
            recordEntityRevision(connection, pack, "faction", entity, packHash)
        }
        pack.entities.worldRules.forEach { entity ->
            insertWorldRule(connection, WorldRule(entity.id, pack.projectId, entity.displayName(), entity.bodyText(), entity.priority))
            recordEntityRevision(connection, pack, "world_rule", entity, packHash)
        }
        pack.entities.characters.forEach { entity ->
            val profile = if (entity.initialRuntimeTemplate == null) {
                entity.metadata
            } else {
                JsonObject(entity.metadata + ("initialRuntimeTemplate" to JsonSupport.json.encodeToJsonElement(entity.initialRuntimeTemplate)))
            }
            insertCharacter(connection, CharacterProfile(entity.id, pack.projectId, entity.displayName(), entity.role, entity.bodyText(), entity.metadata.stringValue("isPlayer") == "true", profile))
            recordEntityRevision(connection, pack, "character", entity, packHash)
        }
        pack.entities.scenes.forEach { entity ->
            insertScene(connection, Scene(entity.id, pack.projectId, entity.displayName(), entity.summary.ifBlank { entity.bodyText() }, entity.locationId, entity.status))
            recordEntityRevision(connection, pack, "scene", entity, packHash)
        }
        pack.entities.sceneContracts.forEach { entity ->
            val sceneId = entity.sceneId ?: entity.metadata.stringValue("sceneId") ?: entity.id
            insertSceneContract(connection, SceneContract(entity.id, pack.projectId, sceneId, entity.allowedSecretIds, entity.requiredBeats, entity.forbiddenReveals))
            recordEntityRevision(connection, pack, "scene_contract", entity, packHash)
        }
        pack.entities.facts.forEach { entity ->
            insertFact(connection, Fact(entity.id, pack.projectId, entity.bodyText(), entity.visibility, "setting_pack:${pack.packId}", 0))
            recordEntityRevision(connection, pack, "fact", entity, packHash)
        }
        pack.entities.secrets.forEach { entity ->
            insertSecret(connection, Secret(entity.id, pack.projectId, entity.bodyText(), entity.revealed, entity.revealCondition, 0, null))
            recordEntityRevision(connection, pack, "secret", entity, packHash)
        }
        pack.entities.items.forEach { entity ->
            insertItem(connection, Item(entity.id, pack.projectId, entity.displayName(), entity.bodyText(), entity.initialOwnerId ?: entity.ownerCharacterId, entity.initialLocationId ?: entity.locationId, entity.tags))
            recordEntityRevision(connection, pack, "item", entity, packHash)
        }
        pack.entities.techniques.forEach { entity ->
            insertTechnique(connection, Technique(entity.id, pack.projectId, entity.displayName(), entity.bodyText(), entity.ownerCharacterId ?: entity.initialOwnerId, entity.tags))
            recordEntityRevision(connection, pack, "technique", entity, packHash)
        }
        pack.entities.storyThreads.forEach { entity ->
            insertStoryThread(
                connection,
                StoryThread(
                    id = entity.id,
                    projectId = pack.projectId,
                    threadType = entity.threadType.ifBlank { entity.type.ifBlank { "plot" } },
                    title = entity.displayName(),
                    directorGoal = entity.directorGoal,
                    publicSummary = entity.publicSummary.ifBlank { entity.summary.ifBlank { entity.bodyText() } },
                    priority = entity.priority,
                    visibility = entity.visibility,
                    parentThreadId = entity.parentThreadId,
                ),
            )
            recordEntityRevision(connection, pack, "story_thread", entity, packHash)
        }
        pack.entities.characterMemories.forEach { entity ->
            val characterId = entity.ownerCharacterId ?: entity.metadata.stringValue("characterId") ?: entity.knownBy.firstOrNull().orEmpty()
            insertCharacterMemory(
                connection,
                CharacterMemory(
                    id = entity.id,
                    projectId = pack.projectId,
                    characterId = characterId,
                    ownerCharacterId = characterId,
                    memoryType = entity.type.ifBlank { "SEMANTIC" },
                    summary = entity.summary.ifBlank { entity.bodyText() },
                    sourceType = "setting_pack",
                    sourceId = pack.packId,
                    tags = entity.tags,
                    salience = entity.priority.coerceIn(0, 100),
                    confidence = entity.metadata.stringValue("confidence")?.toIntOrNull()?.coerceIn(0, 100) ?: 100,
                    visibility = entity.visibility,
                    isCanon = true,
                ),
            )
            recordEntityRevision(connection, pack, "character_memory", entity, packHash)
        }
        pack.entities.characterKnowledge.forEach { entity ->
            val characterId = entity.ownerCharacterId ?: entity.metadata.stringValue("characterId") ?: entity.knownBy.firstOrNull().orEmpty()
            insertKnowledgeClaim(
                connection,
                CharacterKnowledgeClaim(
                    id = entity.id,
                    projectId = pack.projectId,
                    characterId = characterId,
                    propositionKey = entity.metadata.stringValue("propositionKey") ?: entity.id,
                    statement = entity.statement.ifBlank { entity.bodyText() },
                    epistemicState = entity.metadata.stringValue("epistemicState") ?: "BELIEVED",
                    confidence = entity.metadata.stringValue("confidence")?.toIntOrNull()?.coerceIn(0, 100) ?: 70,
                    sourceFactId = entity.metadata.stringValue("sourceFactId"),
                    sourceMemoryId = entity.metadata.stringValue("sourceMemoryId"),
                    visibility = entity.visibility,
                ),
            )
            recordEntityRevision(connection, pack, "character_knowledge", entity, packHash)
        }
    }

    private fun recordEntityRevision(connection: Connection, pack: SettingPack, entityType: String, entity: SettingPackEntity, packHash: String) {
        val snapshot = JsonSupport.json.encodeToString(entity)
        connection.prepareStatement(
            """
            INSERT OR REPLACE INTO entity_revisions(
                id, project_id, entity_type, entity_id, entity_version, source_pack_id,
                source_pack_version, canonical_hash, snapshot_json, visibility, status, created_at
            )
            VALUES (
                COALESCE((SELECT id FROM entity_revisions WHERE project_id = ? AND entity_type = ? AND entity_id = ? AND entity_version = ?), ?),
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
            )
            """.trimIndent(),
        ).use { statement ->
            val revisionId = newId("entity-revision")
            val entityHash = sha256("$packHash:$entityType:${entity.id}:${entity.entityVersion}:$snapshot")
            statement.setString(1, pack.projectId)
            statement.setString(2, entityType)
            statement.setString(3, entity.id)
            statement.setInt(4, entity.entityVersion)
            statement.setString(5, revisionId)
            statement.setString(6, pack.projectId)
            statement.setString(7, entityType)
            statement.setString(8, entity.id)
            statement.setInt(9, entity.entityVersion)
            statement.setString(10, pack.packId)
            statement.setInt(11, pack.packVersion)
            statement.setString(12, entityHash)
            statement.setString(13, snapshot)
            statement.setString(14, entity.visibility)
            statement.setString(15, entity.status)
            statement.setString(16, now())
            statement.executeUpdate()
        }
    }

    private fun insertSettingPackRelation(connection: Connection, pack: SettingPack, relation: SettingPackRelation) {
        connection.prepareStatement(
            """
            INSERT OR REPLACE INTO dependency_edges(
                id, project_id, from_type, from_id, to_type, to_id, relation,
                source_pack_id, source_pack_version, visibility, runtime_relevant,
                strength, reason, metadata_json, canonical_hash, status
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'active')
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, relation.id)
            statement.setString(2, pack.projectId)
            statement.setString(3, relation.fromType)
            statement.setString(4, relation.fromId)
            statement.setString(5, relation.toType)
            statement.setString(6, relation.toId)
            statement.setString(7, relation.relation)
            statement.setString(8, pack.packId)
            statement.setInt(9, pack.packVersion)
            statement.setString(10, relation.visibility)
            statement.setInt(11, if (relation.runtimeRelevant) 1 else 0)
            statement.setInt(12, relation.strength)
            statement.setString(13, relation.reason)
            statement.setString(14, JsonSupport.json.encodeToString(relation.metadata))
            statement.setString(15, sha256(JsonSupport.json.encodeToString(relation)))
            statement.executeUpdate()
        }
    }

    private fun SettingPackEntity.displayName(): String =
        name.ifBlank { title.ifBlank { id } }

    private fun SettingPackEntity.bodyText(): String =
        content.ifBlank { statement.ifBlank { description.ifBlank { summary } } }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    private fun ensureActiveSceneInstance(connection: Connection, session: StorySession): String {
        val active = connection.prepareStatement(
            "SELECT id FROM scene_instances WHERE session_id = ? AND status = 'ACTIVE' LIMIT 1",
        ).use { statement ->
            statement.setString(1, session.id)
            statement.executeQuery().use { rows -> if (rows.next()) rows.getString("id") else null }
        }
        if (active != null) return active
        val sceneInstanceId = newId("scene-instance")
        insertSceneInstance(connection, session.id, session.currentSceneId, sceneInstanceId, nextVisitNumber(connection, session.id, session.currentSceneId), session.nextTurnNumber, "auto-created")
        initializeSceneRequirementStates(connection, session.id, sceneInstanceId, session.currentSceneId)
        connection.prepareStatement("UPDATE story_sessions SET current_scene_instance_id = ?, updated_at = ? WHERE id = ?").use { statement ->
            statement.setString(1, sceneInstanceId)
            statement.setString(2, now())
            statement.setString(3, session.id)
            statement.executeUpdate()
        }
        return sceneInstanceId
    }

    private fun insertSceneInstance(connection: Connection, sessionId: String, sceneId: String, sceneInstanceId: String, visitNumber: Int, startedTurn: Int, entryReason: String) {
        connection.prepareStatement(
            """
            INSERT INTO scene_instances(
                id, session_id, scene_id, visit_number, status, started_turn_number,
                ended_turn_number, entry_reason, exit_reason, summary, created_at, updated_at
            )
            VALUES (?, ?, ?, ?, 'ACTIVE', ?, NULL, ?, '', '', ?, ?)
            """.trimIndent(),
        ).use { statement ->
            val timestamp = now()
            statement.setString(1, sceneInstanceId)
            statement.setString(2, sessionId)
            statement.setString(3, sceneId)
            statement.setInt(4, visitNumber)
            statement.setInt(5, startedTurn)
            statement.setString(6, entryReason)
            statement.setString(7, timestamp)
            statement.setString(8, timestamp)
            statement.executeUpdate()
        }
    }

    private fun initializeSceneRequirementStates(connection: Connection, sessionId: String, sceneInstanceId: String, sceneId: String) {
        connection.prepareStatement(
            "SELECT id FROM scene_requirements WHERE scene_id = ?",
        ).use { statement ->
            statement.setString(1, sceneId)
            statement.executeQuery().use { rows ->
                while (rows.next()) {
                    connection.prepareStatement(
                        """
                        INSERT OR IGNORE INTO session_scene_requirement_states(
                            session_id, scene_instance_id, requirement_id, status, progress,
                            evidence_json, completed_turn_number, created_at, updated_at
                        )
                        VALUES (?, ?, ?, 'OPEN', 0, '{}', NULL, ?, ?)
                        """.trimIndent(),
                    ).use { insert ->
                        val timestamp = now()
                        insert.setString(1, sessionId)
                        insert.setString(2, sceneInstanceId)
                        insert.setString(3, rows.getString("id"))
                        insert.setString(4, timestamp)
                        insert.setString(5, timestamp)
                        insert.executeUpdate()
                    }
                }
            }
        }
    }

    private fun initializeSessionThreadStates(connection: Connection, sessionId: String, projectId: String) {
        connection.prepareStatement("SELECT id FROM story_threads WHERE project_id = ?").use { statement ->
            statement.setString(1, projectId)
            statement.executeQuery().use { rows ->
                while (rows.next()) {
                    connection.prepareStatement(
                        """
                        INSERT OR IGNORE INTO session_thread_states(
                            session_id, thread_id, status, progress, urgency, blocked_reason,
                            last_advanced_turn, evidence_json, created_at, updated_at
                        )
                        VALUES (?, ?, 'OPEN', 0, 0, '', 0, '{}', ?, ?)
                        """.trimIndent(),
                    ).use { insert ->
                        val timestamp = now()
                        insert.setString(1, sessionId)
                        insert.setString(2, rows.getString("id"))
                        insert.setString(3, timestamp)
                        insert.setString(4, timestamp)
                        insert.executeUpdate()
                    }
                }
            }
        }
    }

    private fun assignSessionlessWorldEvents(connection: Connection, projectId: String, sessionId: String) {
        connection.prepareStatement(
            "UPDATE scheduled_world_events SET session_id = ?, updated_at = ? WHERE project_id = ? AND session_id IS NULL",
        ).use { statement ->
            statement.setString(1, sessionId)
            statement.setString(2, now())
            statement.setString(3, projectId)
            statement.executeUpdate()
        }
    }

    private fun nextVisitNumber(connection: Connection, sessionId: String, sceneId: String): Int =
        connection.prepareStatement("SELECT COALESCE(MAX(visit_number), 0) + 1 FROM scene_instances WHERE session_id = ? AND scene_id = ?").use { statement ->
            statement.setString(1, sessionId)
            statement.setString(2, sceneId)
            statement.executeQuery().use { rows ->
                rows.next()
                rows.getInt(1)
            }
        }

    private fun selectAvailableTransitions(connection: Connection, session: StorySession, playerInput: String): List<AvailableTransition> {
        val currentScene = selectScene(connection, session.projectId, session.currentSceneId) ?: return emptyList()
        return connection.prepareStatement(
            """
            SELECT t.id, t.to_scene_id, t.required_player_intent, t.forced_transition_allowed,
                   t.prerequisites_json, s.location_id AS to_location_id
            FROM scene_transitions t
            JOIN scenes s ON s.id = t.to_scene_id
            WHERE t.project_id = ? AND t.from_scene_id = ? AND t.visibility != 'hidden'
            ORDER BY t.id ASC
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, session.projectId)
            statement.setString(2, session.currentSceneId)
            statement.executeQuery().use { rows ->
                buildList {
                    while (rows.next()) {
                        val toLocation = rows.getString("to_location_id")
                        val connectionRow = currentScene.locationId?.let { from -> findLocationConnection(connection, session.projectId, from, toLocation) }
                        val requirement = rows.getString("required_player_intent").orEmpty()
                        val intentSatisfied = requirement.isBlank() || playerInput.contains(requirement, ignoreCase = true)
                        val reachable = currentScene.locationId == null || toLocation == null || currentScene.locationId == toLocation || connectionRow != null
                        val prereq = decodeJsonObject(rows.getString("prerequisites_json"))
                        val unmet = buildList {
                            if (!intentSatisfied) add("player intent '$requirement' not present")
                            if (!reachable) add("target location is unreachable")
                            val requiredFact = prereq.stringValue("requiredFactId")
                            if (requiredFact != null && !targetExists(session.projectId, "fact", requiredFact)) add("missing required fact $requiredFact")
                        }
                        add(
                            AvailableTransition(
                                transitionId = rows.getString("id"),
                                toSceneId = rows.getString("to_scene_id"),
                                targetLocation = toLocation,
                                playerIntentRequired = requirement,
                                prerequisitesSatisfied = unmet.isEmpty(),
                                unmetPrerequisites = unmet,
                                travelMinutes = connectionRow?.get("travel_minutes")?.toIntOrNull() ?: 0,
                                forcedAllowed = rows.getInt("forced_transition_allowed") == 1,
                                visibleReason = if (unmet.isEmpty()) "reachable from current scene" else unmet.joinToString("; "),
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun selectPendingWorldEvents(connection: Connection, session: StorySession): List<PendingWorldEventView> =
        connection.prepareStatement(
            """
            SELECT id, event_type, due_turn_number, payload_json
            FROM scheduled_world_events
            WHERE project_id = ? AND (session_id = ? OR session_id IS NULL)
              AND status IN ('scheduled', 'triggered')
              AND due_turn_number <= ?
            ORDER BY due_turn_number ASC, id ASC
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, session.projectId)
            statement.setString(2, session.id)
            statement.setInt(3, session.nextTurnNumber)
            statement.executeQuery().use { rows ->
                buildList {
                    while (rows.next()) {
                        val payload = decodeJsonObject(rows.getString("payload_json"))
                        add(
                            PendingWorldEventView(
                                eventId = rows.getString("id"),
                                type = rows.getString("event_type"),
                                publicEffect = payload.stringValue("publicEffect") ?: payload.stringValue("public_effect") ?: rows.getString("event_type"),
                                dueReason = "due at turn ${rows.getInt("due_turn_number")}",
                                allowedNarrativeUses = payload.decodeListField("allowedNarrativeUses"),
                                forbiddenCharacterUses = payload.decodeListField("forbiddenCharacterUses"),
                            ),
                        )
                    }
                }
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
                val nextFlags = current.flags.toMutableMap()
                operation.payload.decodeValue<Map<String, String>>("flags")?.let { nextFlags.putAll(it) }
                applyEmotionDeltasToFlags(operation.payload, nextFlags)
                insertCharacterState(
                    connection,
                    CharacterState(
                        characterId = current.characterId,
                        projectId = current.projectId,
                        locationId = operation.payload.stringValue("locationId") ?: operation.payload.stringValue("location_id") ?: current.locationId,
                        mood = operation.payload.stringValue("mood") ?: current.mood,
                        health = operation.payload.stringValue("health") ?: current.health,
                        inventory = operation.payload.decodeValue("inventory") ?: current.inventory,
                        flags = nextFlags,
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

    private fun applyEmotionDeltasToFlags(payload: JsonObject, flags: MutableMap<String, String>) {
        val deltas = payload["emotionDeltas"] ?: payload["emotion_deltas"] ?: return
        val array = runCatching { deltas as kotlinx.serialization.json.JsonArray }.getOrNull() ?: return
        array.forEach { element ->
            val obj = runCatching { element.jsonObject }.getOrNull() ?: return@forEach
            val emotion = obj.stringValue("emotion") ?: return@forEach
            val delta = obj.stringValue("delta")?.toIntOrNull()
                ?: runCatching { obj["delta"]?.jsonPrimitive?.content?.toInt() }.getOrNull()
                ?: return@forEach
            val key = "emotion.$emotion"
            val oldValue = flags[key]?.toIntOrNull() ?: 50
            flags[key] = (oldValue + delta).coerceIn(0, 100).toString()
            flags["emotion.$emotion.last_trigger"] = obj.stringValue("trigger") ?: ""
            flags["emotion.$emotion.last_source"] = listOfNotNull(obj.stringValue("sourceType"), obj.stringValue("sourceId")).joinToString(":")
            flags["emotion.$emotion.last_reason"] = obj.stringValue("reason") ?: ""
        }
    }

    private fun applyMemoryCandidates(connection: Connection, context: ContextPacket, candidates: List<MemoryCandidate>) {
        candidates.forEach { candidate ->
            when (candidate.lifecycleAction.lowercase()) {
                "archive" -> {
                    val targetId = candidate.supersedesMemoryId ?: candidate.candidateId
                    connection.prepareStatement(
                        "UPDATE character_memories SET status = 'archived', updated_at = ? WHERE project_id = ? AND id = ?",
                    ).use { statement ->
                        statement.setString(1, now())
                        statement.setString(2, context.project.id)
                        statement.setString(3, targetId)
                        statement.executeUpdate()
                    }
                }
                "reinforce" -> reinforceMemory(connection, context.project.id, candidate)
                else -> {
                    val duplicateId = selectDuplicateMemoryId(connection, context.project.id, candidate)
                    if (duplicateId != null && candidate.lifecycleAction.lowercase() != "reframe") {
                        reinforceExistingMemory(connection, context.project.id, duplicateId, candidate.salience, candidate.confidence)
                    } else {
                        insertCharacterMemory(
                            connection,
                            CharacterMemory(
                                id = candidate.candidateId,
                                projectId = context.project.id,
                                characterId = candidate.ownerCharacterId,
                                ownerCharacterId = candidate.ownerCharacterId,
                                memoryType = candidate.memoryType,
                                summary = candidate.summary,
                                details = JsonObject(mapOf("reason" to kotlinx.serialization.json.JsonPrimitive(candidate.reason))),
                                sourceType = when {
                                    candidate.sourceEventIds.isNotEmpty() -> "event"
                                    candidate.sourceFactIds.isNotEmpty() -> "fact"
                                    else -> "turn"
                                },
                                sourceId = candidate.sourceEventIds.firstOrNull() ?: candidate.sourceFactIds.firstOrNull() ?: context.turnId,
                                tags = candidate.tags,
                                triggerTags = candidate.tags,
                                emotionalSignature = candidate.emotionalSignature,
                                salience = candidate.salience,
                                confidence = candidate.confidence,
                                reliability = candidate.confidence,
                                visibility = "private",
                                createdTurn = context.turnNumber,
                                status = "active",
                                isCanon = false,
                                supersedesMemoryId = candidate.supersedesMemoryId,
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun reinforceMemory(connection: Connection, projectId: String, candidate: MemoryCandidate) {
        val targetId = candidate.supersedesMemoryId ?: selectDuplicateMemoryId(connection, projectId, candidate) ?: return
        reinforceExistingMemory(connection, projectId, targetId, candidate.salience, candidate.confidence)
    }

    private fun reinforceExistingMemory(connection: Connection, projectId: String, memoryId: String, salience: Int, confidence: Int) {
        connection.prepareStatement(
            """
            UPDATE character_memories
            SET salience = MIN(100, MAX(salience, ?)),
                confidence = MIN(100, confidence + ?),
                updated_at = ?
            WHERE project_id = ? AND id = ?
            """.trimIndent(),
        ).use { statement ->
            statement.setInt(1, salience.coerceIn(0, 100))
            statement.setInt(2, (confidence / 10).coerceAtLeast(1))
            statement.setString(3, now())
            statement.setString(4, projectId)
            statement.setString(5, memoryId)
            statement.executeUpdate()
        }
    }

    private fun selectDuplicateMemoryId(connection: Connection, projectId: String, candidate: MemoryCandidate): String? =
        connection.prepareStatement(
            """
            SELECT id
            FROM character_memories
            WHERE project_id = ? AND owner_character_id = ? AND source_type = ?
              AND COALESCE(source_id, '') = ? AND summary = ? AND status != 'archived'
            LIMIT 1
            """.trimIndent(),
        ).use { statement ->
            val sourceType = when {
                candidate.sourceEventIds.isNotEmpty() -> "event"
                candidate.sourceFactIds.isNotEmpty() -> "fact"
                else -> "turn"
            }
            val sourceId = candidate.sourceEventIds.firstOrNull() ?: candidate.sourceFactIds.firstOrNull() ?: ""
            statement.setString(1, projectId)
            statement.setString(2, candidate.ownerCharacterId)
            statement.setString(3, sourceType)
            statement.setString(4, sourceId)
            statement.setString(5, candidate.summary)
            statement.executeQuery().use { rows -> if (rows.next()) rows.getString("id") else null }
        }

    private fun applyKnowledgeUpdates(connection: Connection, context: ContextPacket, result: AiTurnResult) {
        result.knowledgeUpdates.forEach { update ->
            val existing = selectKnowledgeClaim(connection, context.project.id, update.characterId, update.propositionKey)
            val nextState = if (
                existing != null &&
                update.requestedState == "KNOWN" &&
                existing.epistemicState in setOf("BELIEVED", "SUSPECTED", "DOUBTED", "MISREMEMBERED") &&
                update.confidenceDelta < 50
            ) {
                "DOUBTED"
            } else {
                update.requestedState
            }
            val nextConfidence = ((existing?.confidence ?: 0) + update.confidenceDelta).coerceIn(0, 100)
            insertKnowledgeClaim(
                connection,
                CharacterKnowledgeClaim(
                    id = existing?.id ?: newId("claim"),
                    projectId = context.project.id,
                    characterId = update.characterId,
                    propositionKey = update.propositionKey,
                    statement = existing?.statement ?: update.reason,
                    epistemicState = nextState,
                    confidence = nextConfidence,
                    sourceFactId = update.sourceFactIds.firstOrNull() ?: existing?.sourceFactId,
                    sourceMemoryId = update.sourceMemoryIds.firstOrNull() ?: existing?.sourceMemoryId,
                    acquiredTurnNumber = existing?.acquiredTurnNumber ?: context.turnNumber,
                    updatedTurnNumber = context.turnNumber,
                    visibility = existing?.visibility ?: "private",
                    status = "active",
                ),
            )
        }
    }

    private fun recordSuccessfulRecall(connection: Connection, context: ContextPacket) {
        context.retrievedMemories.forEach { memory ->
            connection.prepareStatement(
                """
                INSERT OR IGNORE INTO memory_recall_logs(
                    id, session_id, turn_number, context_id, character_id, memory_id,
                    retrieval_score, retrieval_reasons_json, included_in_context, created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, ?)
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, newId("recall"))
                statement.setString(2, context.sessionId)
                statement.setInt(3, context.turnNumber)
                statement.setString(4, context.contextId)
                statement.setString(5, memory.ownerCharacterId)
                statement.setString(6, memory.memoryId)
                statement.setInt(7, memory.retrievalScore)
                statement.setString(8, JsonSupport.json.encodeToString(memory.retrievalReasons))
                statement.setString(9, now())
                statement.executeUpdate()
            }
            connection.prepareStatement(
                """
                UPDATE character_memories
                SET recall_count = recall_count + 1,
                    last_recalled_turn_number = ?,
                    updated_at = ?
                WHERE project_id = ? AND id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setInt(1, context.turnNumber)
                statement.setString(2, now())
                statement.setString(3, context.project.id)
                statement.setString(4, memory.memoryId)
                statement.executeUpdate()
            }
        }
    }

    private fun applySceneRequirementUpdates(connection: Connection, context: ContextPacket, result: AiTurnResult) {
        if (result.sceneRequirementUpdates.isEmpty()) return
        result.sceneRequirementUpdates.forEach { update ->
            val status = update.requestedStatus
            connection.prepareStatement(
                """
                UPDATE session_scene_requirement_states
                SET status = ?, progress = MIN(100, MAX(0, progress + ?)), evidence_json = ?,
                    completed_turn_number = CASE WHEN ? = 'COMPLETED' THEN ? ELSE completed_turn_number END,
                    updated_at = ?
                WHERE session_id = ? AND scene_instance_id = ? AND requirement_id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, status)
                statement.setInt(2, update.progressDelta)
                statement.setString(3, JsonSupport.json.encodeToString(update))
                statement.setString(4, status)
                statement.setInt(5, context.turnNumber)
                statement.setString(6, now())
                statement.setString(7, context.sessionId)
                statement.setString(8, context.sceneLifecycle.sceneInstanceId)
                statement.setString(9, update.requirementId)
                statement.executeUpdate()
            }
        }
        refreshSceneLifecycleStatus(connection, context)
    }

    private fun refreshSceneLifecycleStatus(connection: Connection, context: ContextPacket) {
        val requiredOpen = connection.prepareStatement(
            """
            SELECT COUNT(*)
            FROM scene_requirements r
            JOIN session_scene_requirement_states s ON s.requirement_id = r.id
            WHERE s.session_id = ? AND s.scene_instance_id = ?
              AND r.requirement_type = 'REQUIRED' AND s.status != 'COMPLETED'
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, context.sessionId)
            statement.setString(2, context.sceneLifecycle.sceneInstanceId)
            statement.executeQuery().use { rows ->
                rows.next()
                rows.getInt(1)
            }
        }
        val failureTriggered = connection.prepareStatement(
            """
            SELECT COUNT(*)
            FROM scene_requirements r
            JOIN session_scene_requirement_states s ON s.requirement_id = r.id
            WHERE s.session_id = ? AND s.scene_instance_id = ?
              AND r.requirement_type = 'FAILURE_GUARD' AND s.status = 'FAILED'
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, context.sessionId)
            statement.setString(2, context.sceneLifecycle.sceneInstanceId)
            statement.executeQuery().use { rows ->
                rows.next()
                rows.getInt(1)
            }
        }
        val nextStatus = when {
            failureTriggered > 0 -> "FAILED"
            requiredOpen == 0 -> "READY_TO_CLOSE"
            else -> null
        }
        if (nextStatus != null) {
            connection.prepareStatement(
                "UPDATE scene_instances SET status = ?, updated_at = ? WHERE id = ? AND status = 'ACTIVE'",
            ).use { statement ->
                statement.setString(1, nextStatus)
                statement.setString(2, now())
                statement.setString(3, context.sceneLifecycle.sceneInstanceId)
                statement.executeUpdate()
            }
        }
    }

    private fun applyThreadProgressUpdates(connection: Connection, context: ContextPacket, result: AiTurnResult) {
        result.threadProgressUpdates.forEach { update ->
            connection.prepareStatement(
                """
                UPDATE session_thread_states
                SET progress = MIN(100, MAX(0, progress + ?)),
                    status = ?,
                    last_advanced_turn = ?,
                    evidence_json = ?,
                    updated_at = ?
                WHERE session_id = ? AND thread_id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setInt(1, update.progressDelta.coerceIn(-25, 25))
                statement.setString(2, update.requestedStatus)
                statement.setInt(3, context.turnNumber)
                statement.setString(4, JsonSupport.json.encodeToString(update))
                statement.setString(5, now())
                statement.setString(6, context.sessionId)
                statement.setString(7, update.threadId)
                statement.executeUpdate()
            }
        }
    }

    private fun applyWorldEventAcknowledgements(connection: Connection, context: ContextPacket, result: AiTurnResult) {
        result.worldEventAcknowledgements.forEach { acknowledgement ->
            connection.prepareStatement(
                """
                UPDATE scheduled_world_events
                SET status = 'consumed',
                    triggered_turn_number = COALESCE(triggered_turn_number, ?),
                    consumed_turn_number = ?,
                    updated_at = ?
                WHERE id = ? AND project_id = ? AND (session_id = ? OR session_id IS NULL) AND status != 'consumed'
                """.trimIndent(),
            ).use { statement ->
                statement.setInt(1, context.turnNumber)
                statement.setInt(2, context.turnNumber)
                statement.setString(3, now())
                statement.setString(4, acknowledgement.eventId)
                statement.setString(5, context.project.id)
                statement.setString(6, context.sessionId)
                statement.executeUpdate()
            }
        }
    }

    private fun applySceneTransition(connection: Connection, context: ContextPacket, result: AiTurnResult) {
        val proposal = result.transitionProposal ?: return
        selectTransition(connection, context.project.id, proposal.transitionId)
            ?: error("Transition not found: ${proposal.transitionId}")
        val toScene = selectScene(connection, context.project.id, proposal.toSceneId)
            ?: error("Target scene not found: ${proposal.toSceneId}")
        val currentInstanceId = context.sceneLifecycle.sceneInstanceId
        val summary = result.turnSummary.take(240)
        connection.prepareStatement(
            """
            INSERT INTO pending_scene_transitions(
                id, session_id, result_id, from_scene_instance_id, to_scene_id, proposal_type,
                reason, evidence_json, status, rejection_reason, created_turn_number, resolved_turn_number,
                created_at, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'accepted', '', ?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            val timestamp = now()
            statement.setString(1, newId("transition-proposal"))
            statement.setString(2, context.sessionId)
            statement.setString(3, result.resultId)
            statement.setString(4, currentInstanceId)
            statement.setString(5, proposal.toSceneId)
            statement.setString(6, proposal.proposalType)
            statement.setString(7, proposal.reason)
            statement.setString(8, JsonSupport.json.encodeToString(proposal))
            statement.setInt(9, context.turnNumber)
            statement.setInt(10, context.turnNumber)
            statement.setString(11, timestamp)
            statement.setString(12, timestamp)
            statement.executeUpdate()
        }
        connection.prepareStatement(
            """
            UPDATE scene_instances
            SET status = 'CLOSED', ended_turn_number = ?, exit_reason = ?, summary = ?, updated_at = ?
            WHERE id = ? AND session_id = ? AND status IN ('ACTIVE', 'READY_TO_CLOSE')
            """.trimIndent(),
        ).use { statement ->
            statement.setInt(1, context.turnNumber)
            statement.setString(2, proposal.reason)
            statement.setString(3, summary)
            statement.setString(4, now())
            statement.setString(5, currentInstanceId)
            statement.setString(6, context.sessionId)
            statement.executeUpdate()
        }
        val newInstanceId = newId("scene-instance")
        val visitNumber = nextVisitNumber(connection, context.sessionId, proposal.toSceneId)
        insertSceneInstance(connection, context.sessionId, proposal.toSceneId, newInstanceId, visitNumber, context.turnNumber, proposal.reason)
        initializeSceneRequirementStates(connection, context.sessionId, newInstanceId, proposal.toSceneId)
        connection.prepareStatement(
            """
            UPDATE story_sessions
            SET current_scene_id = ?, current_scene_instance_id = ?, updated_at = ?
            WHERE id = ?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, toScene.id)
            statement.setString(2, newInstanceId)
            statement.setString(3, now())
            statement.setString(4, context.sessionId)
            statement.executeUpdate()
        }
        connection.prepareStatement(
            "UPDATE projects SET active_scene_id = ?, updated_at = ? WHERE id = ?",
        ).use { statement ->
            statement.setString(1, toScene.id)
            statement.setString(2, now())
            statement.setString(3, context.project.id)
            statement.executeUpdate()
        }
        if (toScene.locationId != null) {
            proposal.participatingCharacterIds.forEach { characterId ->
                connection.prepareStatement(
                    "UPDATE character_states SET location_id = ?, updated_at = ? WHERE project_id = ? AND character_id = ?",
                ).use { statement ->
                    statement.setString(1, toScene.locationId)
                    statement.setString(2, now())
                    statement.setString(3, context.project.id)
                    statement.setString(4, characterId)
                    statement.executeUpdate()
                }
            }
        }
    }

    private fun applyWorldClock(connection: Connection, context: ContextPacket, result: AiTurnResult) {
        val proposedMinutes = result.timeAdvanceProposal?.minutes ?: 5
        val transitionMinutes = result.transitionProposal?.let { proposal ->
            context.availableTransitions.firstOrNull { it.transitionId == proposal.transitionId }?.travelMinutes
        } ?: 0
        val minutes = (proposedMinutes + transitionMinutes).coerceAtLeast(0)
        connection.prepareStatement(
            """
            UPDATE story_sessions
            SET elapsed_minutes = elapsed_minutes + ?,
                world_time = ?,
                updated_at = ?
            WHERE id = ?
            """.trimIndent(),
        ).use { statement ->
            val nextElapsed = context.worldClock.elapsedMinutes + minutes
            statement.setInt(1, minutes)
            statement.setString(2, "T+${nextElapsed}m")
            statement.setString(3, now())
            statement.setString(4, context.sessionId)
            statement.executeUpdate()
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
            """
            SELECT id, project_id, current_scene_id, next_turn_number, state_version, status,
                   current_scene_instance_id, world_time, elapsed_minutes, branch_id
            FROM story_sessions
            WHERE id = ?
            """.trimIndent(),
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

    private fun selectBranchIsActive(connection: Connection, projectId: String, branchId: String, sessionId: String): Boolean =
        connection.prepareStatement(
            """
            SELECT 1
            FROM timeline_branches
            WHERE id = ? AND project_id = ? AND status = 'ACTIVE' AND active_session_id = ?
            LIMIT 1
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, branchId)
            statement.setString(2, projectId)
            statement.setString(3, sessionId)
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

    private fun selectScene(connection: Connection, projectId: String, sceneId: String): Scene? =
        connection.prepareStatement(
            "SELECT id, project_id, title, summary, location_id, status, progress_json, completed_requirements_json FROM scenes WHERE project_id = ? AND id = ?",
        ).use { statement ->
            statement.setString(1, projectId)
            statement.setString(2, sceneId)
            statement.executeQuery().use { rows -> if (rows.next()) rows.toScene() else null }
        }

    private fun findLocationConnection(connection: Connection, projectId: String, fromLocationId: String, toLocationId: String?): Map<String, String>? {
        if (toLocationId == null) return null
        return connection.prepareStatement(
            """
            SELECT id, travel_minutes
            FROM location_connections
            WHERE project_id = ? AND (
                (from_location_id = ? AND to_location_id = ?)
                OR (bidirectional = 1 AND from_location_id = ? AND to_location_id = ?)
            )
            LIMIT 1
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, projectId)
            statement.setString(2, fromLocationId)
            statement.setString(3, toLocationId)
            statement.setString(4, toLocationId)
            statement.setString(5, fromLocationId)
            statement.executeQuery().use { rows ->
                if (rows.next()) {
                    mapOf("id" to rows.getString("id"), "travel_minutes" to rows.getInt("travel_minutes").toString())
                } else {
                    null
                }
            }
        }
    }

    private fun selectTransition(connection: Connection, projectId: String, transitionId: String): SceneTransitionDefinition? =
        connection.prepareStatement(
            """
            SELECT id, project_id, from_scene_id, to_scene_id, transition_type, prerequisites_json,
                   costs_json, required_player_intent, forced_transition_allowed, visibility
            FROM scene_transitions
            WHERE project_id = ? AND id = ?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, projectId)
            statement.setString(2, transitionId)
            statement.executeQuery().use { rows ->
                if (!rows.next()) return@use null
                SceneTransitionDefinition(
                    id = rows.getString("id"),
                    projectId = rows.getString("project_id"),
                    fromSceneId = rows.getString("from_scene_id"),
                    toSceneId = rows.getString("to_scene_id"),
                    transitionType = rows.getString("transition_type"),
                    prerequisites = decodeJsonObject(rows.getString("prerequisites_json")),
                    costs = decodeJsonObject(rows.getString("costs_json")),
                    requiredPlayerIntent = rows.getString("required_player_intent"),
                    forcedTransitionAllowed = rows.getInt("forced_transition_allowed") == 1,
                    visibility = rows.getString("visibility"),
                )
            }
        }

    private fun selectCharacterMemory(connection: Connection, projectId: String, memoryId: String): CharacterMemory? =
        connection.prepareStatement(
            """
            SELECT id, project_id, character_id, owner_character_id, memory_type, summary, details_json,
                   source_type, source_id, subject_type, subject_id, tags_json, trigger_tags_json,
                   emotional_signature_json, salience, confidence, reliability, visibility,
                   created_turn, created_turn_number, last_recalled_turn_number, recall_count,
                   decay_rate, status, is_canon, supersedes_memory_id
            FROM character_memories
            WHERE project_id = ? AND id = ?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, projectId)
            statement.setString(2, memoryId)
            statement.executeQuery().use { rows -> if (rows.next()) rows.toCharacterMemory() else null }
        }

    private fun selectKnowledgeClaim(connection: Connection, projectId: String, characterId: String, propositionKey: String): CharacterKnowledgeClaim? =
        connection.prepareStatement(
            """
            SELECT id, project_id, character_id, proposition_key, statement, epistemic_state,
                   confidence, source_fact_id, source_memory_id, acquired_turn_number,
                   updated_turn_number, visibility, status
            FROM character_knowledge
            WHERE project_id = ? AND character_id = ? AND proposition_key = ? AND status = 'active'
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, projectId)
            statement.setString(2, characterId)
            statement.setString(3, propositionKey)
            statement.executeQuery().use { rows -> if (rows.next()) rows.toKnowledgeClaim() else null }
        }

    private fun selectCharacters(connection: Connection, projectId: String): List<CharacterProfile> =
        connection.queryMany(
            """
            SELECT id, project_id, name, role, description, is_player, profile_json
            FROM characters
            WHERE project_id = ?
            ORDER BY is_player DESC, name ASC
            """.trimIndent(),
            projectId,
        ) { it.toCharacterProfile() }

    private fun selectItemsOwnedBy(connection: Connection, projectId: String, characterId: String): List<String> =
        connection.prepareStatement(
            """
            SELECT id
            FROM items
            WHERE project_id = ? AND owner_character_id = ?
            ORDER BY id ASC
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, projectId)
            statement.setString(2, characterId)
            statement.executeQuery().use { rows ->
                buildList {
                    while (rows.next()) add(rows.getString("id"))
                }
            }
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

    private fun ResultSet.toCharacterMemory(): CharacterMemory {
        val lastRecalled = getInt("last_recalled_turn_number")
        return CharacterMemory(
            id = getString("id"),
            projectId = getString("project_id"),
            characterId = getString("character_id"),
            ownerCharacterId = getString("owner_character_id") ?: getString("character_id"),
            memoryType = getString("memory_type") ?: "EPISODIC",
            summary = getString("summary"),
            details = decodeJsonObject(getString("details_json")),
            sourceType = getString("source_type") ?: "seed",
            sourceId = getString("source_id"),
            subjectType = getString("subject_type"),
            subjectId = getString("subject_id"),
            tags = decodeList(getString("tags_json")),
            triggerTags = decodeList(getString("trigger_tags_json")),
            emotionalSignature = decodeJsonObject(getString("emotional_signature_json")),
            salience = getInt("salience"),
            confidence = getInt("confidence"),
            reliability = getInt("reliability"),
            visibility = getString("visibility"),
            createdTurn = runCatching { getInt("created_turn_number") }.getOrElse { getInt("created_turn") },
            lastRecalledTurnNumber = if (wasNull()) null else lastRecalled,
            recallCount = getInt("recall_count"),
            decayRate = getDouble("decay_rate"),
            status = getString("status") ?: "active",
            isCanon = getInt("is_canon") == 1,
            supersedesMemoryId = getString("supersedes_memory_id"),
        )
    }

    private fun ResultSet.toKnowledgeClaim(): CharacterKnowledgeClaim = CharacterKnowledgeClaim(
        id = getString("id"),
        projectId = getString("project_id"),
        characterId = getString("character_id"),
        propositionKey = getString("proposition_key"),
        statement = getString("statement"),
        epistemicState = getString("epistemic_state"),
        confidence = getInt("confidence"),
        sourceFactId = getString("source_fact_id"),
        sourceMemoryId = getString("source_memory_id"),
        acquiredTurnNumber = getInt("acquired_turn_number"),
        updatedTurnNumber = getInt("updated_turn_number"),
        visibility = getString("visibility"),
        status = getString("status"),
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
        branchId = runCatching { getString("branch_id") }.getOrNull(),
        currentSceneInstanceId = runCatching { getString("current_scene_instance_id") }.getOrNull(),
        worldTime = runCatching { getString("world_time") }.getOrNull().orEmpty(),
        elapsedMinutes = runCatching { getInt("elapsed_minutes") }.getOrDefault(0),
    )

    private fun decodeJsonObject(value: String?): JsonObject =
        value?.let { runCatching { JsonSupport.json.decodeFromString<JsonObject>(it) }.getOrNull() }
            ?: JsonObject(emptyMap())

    private fun decodeList(value: String?): List<String> =
        value?.let { runCatching { JsonSupport.json.decodeFromString<List<String>>(it) }.getOrNull() }
            ?: emptyList()

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

private fun JsonObject.decodeListField(key: String): List<String> =
    decodeValue<List<String>>(key).orEmpty()

private fun CharacterProfile.initialRuntimeTemplate(): InitialRuntimeTemplate? =
    profile.decodeValue("initialRuntimeTemplate")
