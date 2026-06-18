package com.localnovel.engine.service

import com.localnovel.engine.model.ContextAuditReport
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.ContextSectionByteReport
import com.localnovel.engine.model.PilotPreflightCheck
import com.localnovel.engine.model.PilotPreflightReport
import com.localnovel.engine.model.SessionEvidenceExport
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

class ContextAuditService(
    private val maxBytes: Int = 24576,
) {
    fun auditFile(path: Path): ContextAuditReport {
        val raw = Files.readAllBytes(path)
        val text = raw.toString(Charsets.UTF_8).removePrefix("\uFEFF")
        val unknownTopLevelKeys = ContextPacketTopLevelAllowlist.unknownTopLevelKeys(text)
        val context = JsonSupport.json.decodeFromString<ContextPacket>(text)
        return audit(context, path.toString(), raw.size, unknownTopLevelKeys)
    }

    fun audit(
        context: ContextPacket,
        file: String = "",
        actualFileBytes: Int? = null,
        unknownTopLevelKeys: List<String> = emptyList(),
    ): ContextAuditReport {
        val canonical = JsonSupport.json.encodeToString(context)
        val canonicalBytes = canonical.toByteArray(Charsets.UTF_8).size
        val actualBytes = actualFileBytes ?: canonicalBytes
        val recomputedHash = sha256(JsonSupport.json.encodeToString(context.copy(contextHash = "")).toByteArray(Charsets.UTF_8))
        val mandatoryFieldsPresent = context.project.id.isNotBlank() &&
            context.turnId.isNotBlank() &&
            context.contextId.isNotBlank() &&
            context.sessionId.isNotBlank() &&
            context.activeScene != null &&
            context.sceneContract != null &&
            context.worldRules.isNotEmpty() &&
            context.characters.isNotEmpty() &&
            context.characterStates.isNotEmpty()
        val violations = buildList {
            if (actualBytes > maxBytes) add("context_over_budget:$actualBytes>$maxBytes")
            if (context.contextBudget.actualBytes != actualBytes) add("declared_actual_bytes_mismatch:${context.contextBudget.actualBytes}!=$actualBytes")
            if (context.contextBudget.maxBytes != maxBytes) add("max_bytes_mismatch:${context.contextBudget.maxBytes}!=$maxBytes")
            if (context.contextHash != recomputedHash) add("context_hash_mismatch")
            if (!mandatoryFieldsPresent) add("mandatory_fields_missing")
            if (context.sceneContract?.forbiddenReveals == null) add("forbidden_reveals_missing")
            unknownTopLevelKeys.forEach { add("unknown_top_level_field:$it") }
            if (ContextPacketTopLevelAllowlist.diagnosticKeys.any { canonical.contains("\"$it\"") }) {
                add("diagnostics_present_in_context")
            }
            if (context.contextBudget.truncationReasons.isNotEmpty()) {
                add("legacy_context_budget_truncation_reasons_present")
            }
            if (context.settingContextBudget.reasons.isNotEmpty()) {
                add("legacy_setting_budget_reasons_present")
            }
        }
        return ContextAuditReport(
            valid = violations.isEmpty(),
            file = file,
            maxBytes = maxBytes,
            actualBytes = actualBytes,
            canonicalBytes = canonicalBytes,
            declaredActualBytes = context.contextBudget.actualBytes,
            contextHash = context.contextHash,
            recomputedContextHash = recomputedHash,
            hashValid = context.contextHash == recomputedHash,
            mandatoryFieldsPresent = mandatoryFieldsPresent,
            violations = violations,
            sections = sections(context),
            forensics = ContextBudgetForensicsService(maxBytes).report(context.copy(contextHash = ""), context),
        )
    }

    private fun sections(context: ContextPacket): List<ContextSectionByteReport> =
        listOf(
            section("project", context.project, 1),
            section("activeScene", context.activeScene, if (context.activeScene == null) 0 else 1),
            section("sceneContract", context.sceneContract, if (context.sceneContract == null) 0 else 1),
            section("characters", context.characters, context.characters.size),
            section("characterStates", context.characterStates, context.characterStates.size),
            section("retrievedMemories", context.retrievedMemories, context.retrievedMemories.size),
            section("knowledgeClaims", context.knowledgeClaims, context.knowledgeClaims.size),
            section("worldRules", context.worldRules, context.worldRules.size),
            section("facts", context.facts, context.facts.size),
            section("items", context.items, context.items.size),
            section("techniques", context.techniques, context.techniques.size),
            section("recentEvents", context.recentEvents, context.recentEvents.size),
            section("activeStoryThreads", context.activeStoryThreads, context.activeStoryThreads.size),
            section("availableTransitions", context.availableTransitions, context.availableTransitions.size),
            section("pendingWorldEvents", context.pendingWorldEvents, context.pendingWorldEvents.size),
            section("relevantSettings", context.relevantSettings, context.relevantSettings.size),
        )

    private inline fun <reified T> section(name: String, value: T, count: Int): ContextSectionByteReport =
        ContextSectionByteReport(name, JsonSupport.json.encodeToString(value).toByteArray(Charsets.UTF_8).size, count)
}

class SessionEvidenceExporter(private val repository: SqliteStoryRepository) {
    fun export(sessionId: String): SessionEvidenceExport {
        val session = repository.getSession(sessionId) ?: error("Session not found: $sessionId")
        val turnLogs = repository.tableRows("turn_logs").filter { it["session_id"] == sessionId }
        return SessionEvidenceExport(
            session = session,
            activeSceneInstanceCount = repository.activeSceneInstanceCount(sessionId),
            characterStates = repository.listCharacterStates(session.projectId),
            turnLogs = turnLogs,
            stateDeltas = repository.tableRows("state_deltas").filter { delta -> turnLogs.any { it["id"] == delta["turn_id"] } || delta["project_id"] == session.projectId },
            facts = repository.listFacts(session.projectId),
            revealedSecrets = repository.listRevealedSecrets(session.projectId),
            pendingContextCount = turnLogs.count { it["validation_status"] == "pending" },
        )
    }
}

class PilotPreflightService(private val repository: SqliteStoryRepository) {
    fun run(projectId: String, sessionId: String): PilotPreflightReport {
        val checks = mutableListOf<PilotPreflightCheck>()
        val session = repository.getSession(sessionId)
        checks += PilotPreflightCheck("session_exists", session != null, session?.status ?: "missing")
        if (session == null) {
            return PilotPreflightReport(false, projectId, sessionId, checks)
        }
        checks += PilotPreflightCheck("session_project_matches", session.projectId == projectId, "sessionProject=${session.projectId}")
        checks += PilotPreflightCheck("session_active", session.status == "active", "status=${session.status}")
        val canon = CanonConsistencyValidator(repository).validate(projectId)
        checks += PilotPreflightCheck("canon_valid", canon.isValid, canon.issues.joinToString("; ") { it.code })
        checks += PilotPreflightCheck("single_active_scene_instance", repository.activeSceneInstanceCount(sessionId) == 1, "count=${repository.activeSceneInstanceCount(sessionId)}")
        val scene = repository.getActiveScene(projectId)
        val currentLocation = scene?.locationId
        val stateByCharacter = repository.listCharacterStates(projectId).associateBy { it.characterId }
        val openingIds = buildSet {
            if (currentLocation != null) {
                repository.listDependencyRows(projectId)
                    .filter { it["from_type"] == "character" && it["to_type"] == "location" && it["to_id"] == currentLocation && it["relation"]?.equals("LOCATED_IN", true) == true }
                    .forEach { add(it["from_id"].orEmpty()) }
                stateByCharacter.values.filter { it.locationId == currentLocation }.forEach { add(it.characterId) }
            }
        }.filter { it.isNotBlank() }
        val missingState = openingIds.filterNot { it in stateByCharacter }
        checks += PilotPreflightCheck("opening_characters_have_runtime_states", missingState.isEmpty(), "missing=${missingState.joinToString(",")}")
        val incompleteMind = openingIds.filter { id ->
            val state = stateByCharacter[id]
            state == null || state.flags["current_goal"].isNullOrBlank() || state.flags["dominant_emotion"].isNullOrBlank()
        }
        checks += PilotPreflightCheck("opening_character_mind_ready", incompleteMind.isEmpty(), "incomplete=${incompleteMind.joinToString(",")}")
        val pendingCount = repository.tableRows("turn_logs").count { it["session_id"] == sessionId && it["validation_status"] == "pending" }
        checks += PilotPreflightCheck("no_pending_context_conflict", pendingCount == 0, "pending=$pendingCount")

        val contextAudit = runCatching {
            val context = MultiTurnRuntime(repository).previewTurnContext(sessionId, "pilot-preflight")
            val hiddenSecretTexts = repository.listSecrets(projectId).filterNot { it.revealed }.map { it.content }.filter { it.isNotBlank() }
            val facingText = JsonSupport.json.encodeToString(
                mapOf(
                    "characters" to context.characters.map { it.description },
                    "characterStates" to context.characterStates.map { it.flags.values.joinToString(" ") },
                    "characterMemories" to context.characterMemories.map { it.summary },
                    "knowledgeClaims" to context.knowledgeClaims.map { it.statement },
                    "facts" to context.facts.map { it.content },
                    "relevantSettings" to context.relevantSettings.map { it.summary },
                ),
            )
            val leaked = hiddenSecretTexts.filter { facingText.contains(it) }
            checks += PilotPreflightCheck("no_hidden_secret_in_facing_context", leaked.isEmpty(), "leakCount=${leaked.size}")
            ContextAuditService().audit(context)
        }.getOrElse { error ->
            checks += PilotPreflightCheck("dry_run_context", false, error.message ?: error.toString())
            null
        }
        if (contextAudit != null) {
            checks += PilotPreflightCheck("dry_run_context", contextAudit.valid, contextAudit.violations.joinToString("; "))
        }
        return PilotPreflightReport(
            valid = checks.all { it.passed } && contextAudit?.valid != false,
            projectId = projectId,
            sessionId = sessionId,
            checks = checks,
            contextAudit = contextAudit,
            stateVersion = session.stateVersion,
            nextTurnNumber = session.nextTurnNumber,
            pendingContextCount = pendingCount,
        )
    }
}

private fun sha256(bytes: ByteArray): String =
    MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }
