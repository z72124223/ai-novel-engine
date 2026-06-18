package com.localnovel.engine.service

import com.localnovel.engine.model.CanonRevisionSummary
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.ImpactPath
import com.localnovel.engine.model.RelevantSetting
import com.localnovel.engine.model.SettingContextBudget
import com.localnovel.engine.model.SettingPack
import com.localnovel.engine.model.SettingPackApplyResult
import com.localnovel.engine.model.SettingPackDiff
import com.localnovel.engine.model.SettingPackDiffEntry
import com.localnovel.engine.model.SettingPackEntity
import com.localnovel.engine.model.SettingPackImpactReport
import com.localnovel.engine.model.SettingPackPreview
import com.localnovel.engine.model.SettingPackRelation
import com.localnovel.engine.model.SettingPackValidationIssue
import com.localnovel.engine.model.SettingPackValidationReport
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

class SettingPackParser {
    fun parse(path: Path): SettingPack =
        try {
            JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
        } catch (error: SerializationException) {
            throw IllegalArgumentException("Invalid setting pack JSON at $path: ${error.message}", error)
        }
}

class SettingPackSchemaValidator(private val repository: SqliteStoryRepository) {
    fun validate(pack: SettingPack): SettingPackValidationReport {
        val issues = mutableListOf<SettingPackValidationIssue>()
        if (pack.schemaVersion != "1.0") issues += error("unsupported_schema", "schemaVersion must be 1.0.", "$.schemaVersion")
        if (pack.packId.isBlank()) issues += error("missing_pack_id", "packId is required.", "$.packId")
        if (pack.packVersion < 1) issues += error("invalid_pack_version", "packVersion must be >= 1.", "$.packVersion")
        if (pack.projectId.isBlank()) issues += error("missing_project_id", "projectId is required.", "$.projectId")
        if (pack.name.isBlank()) issues += error("missing_name", "name is required.", "$.name")
        if (pack.mode !in setOf("BASE", "ADD_ON", "PATCH_CANON")) {
            issues += error("invalid_mode", "mode must be BASE, ADD_ON, or PATCH_CANON.", "$.mode")
        }
        if (pack.source.author.isBlank()) issues += error("missing_source_author", "source.author is required.", "$.source.author")
        pack.dependencies.forEachIndexed { index, dependency ->
            if (repository.settingPackHash(dependency.packId, dependency.minVersion) == null) {
                issues += error("missing_dependency", "Dependency ${dependency.packId} v${dependency.minVersion} has not been imported.", "$.dependencies[$index]")
            }
        }
        pack.allTypedEntities().forEach { (type, entity) ->
            val latest = repository.latestEntityRevision(pack.projectId, type, entity.id)
            val latestVersion = latest?.get("entity_version")?.toIntOrNull()
            val latestSnapshot = latest?.get("snapshot_json")
            val currentSnapshot = JsonSupport.json.encodeToString(entity)
            if (latestVersion != null && entity.entityVersion <= latestVersion && latestSnapshot != currentSnapshot) {
                issues += error("non_monotonic_entity_version", "Entity ${type}:${entity.id} must increase entityVersion above $latestVersion before changing canon.", "$.entities.$type[${entity.id}].entityVersion")
            }
            val path = "$.entities.$type[${entity.id}]"
            if (!entity.id.matches(ID_REGEX)) issues += error("invalid_id", "Entity id '${entity.id}' contains unsupported characters.", path)
            if (entity.entityVersion < 1) issues += error("invalid_entity_version", "entityVersion must be >= 1.", "$path.entityVersion")
            if (entity.visibility !in VISIBILITIES) issues += error("invalid_visibility", "Unsupported visibility '${entity.visibility}'.", "$path.visibility")
            if (entity.status !in STATUSES) issues += error("invalid_status", "Unsupported status '${entity.status}'.", "$path.status")
        }
        val duplicates = pack.allTypedEntities()
            .groupBy { it.first to it.second.id }
            .filterValues { it.size > 1 }
            .keys
        duplicates.forEach { (type, id) -> issues += error("duplicate_entity", "Duplicate entity id '$id' in $type.", "$.entities.$type") }
        pack.relations.forEachIndexed { index, relation ->
            if (relation.id.isBlank()) issues += error("missing_relation_id", "Relation id is required.", "$.relations[$index].id")
            if (relation.fromId.isBlank() || relation.toId.isBlank() || relation.relation.isBlank()) {
                issues += error("invalid_relation", "Relation requires fromType/fromId/toType/toId/relation.", "$.relations[$index]")
            }
        }
        pack.locationConnections.forEachIndexed { index, connection ->
            val path = "$.locationConnections[$index]"
            if (!connection.id.matches(ID_REGEX)) issues += error("invalid_id", "Location connection id '${connection.id}' contains unsupported characters.", "$path.id")
            if (connection.projectId != pack.projectId) issues += error("project_mismatch", "Location connection '${connection.id}' projectId must match pack.projectId.", "$path.projectId")
            if (connection.fromLocationId.isBlank() || connection.toLocationId.isBlank()) {
                issues += error("invalid_location_connection", "Location connection '${connection.id}' requires fromLocationId and toLocationId.", path)
            }
            if (connection.visibility !in VISIBILITIES) issues += error("invalid_visibility", "Unsupported visibility '${connection.visibility}'.", "$path.visibility")
            if (connection.travelMinutes < 0) issues += error("invalid_travel_minutes", "Location connection '${connection.id}' travelMinutes must be >= 0.", "$path.travelMinutes")
        }
        pack.sceneTransitions.forEachIndexed { index, transition ->
            val path = "$.sceneTransitions[$index]"
            if (!transition.id.matches(ID_REGEX)) issues += error("invalid_id", "Scene transition id '${transition.id}' contains unsupported characters.", "$path.id")
            if (transition.projectId != pack.projectId) issues += error("project_mismatch", "Scene transition '${transition.id}' projectId must match pack.projectId.", "$path.projectId")
            if (transition.fromSceneId.isBlank() || transition.toSceneId.isBlank()) {
                issues += error("invalid_scene_transition", "Scene transition '${transition.id}' requires fromSceneId and toSceneId.", path)
            }
            if (transition.requiredPlayerIntent.isBlank()) {
                issues += error("missing_transition_intent", "Scene transition '${transition.id}' requires explicit requiredPlayerIntent.", "$path.requiredPlayerIntent")
            }
            if (transition.visibility !in VISIBILITIES) issues += error("invalid_visibility", "Unsupported visibility '${transition.visibility}'.", "$path.visibility")
        }
        pack.scheduledWorldEvents.forEachIndexed { index, event ->
            val path = "$.scheduledWorldEvents[$index]"
            if (!event.id.matches(ID_REGEX)) issues += error("invalid_id", "Scheduled world event id '${event.id}' contains unsupported characters.", "$path.id")
            if (event.projectId != pack.projectId) issues += error("project_mismatch", "Scheduled world event '${event.id}' projectId must match pack.projectId.", "$path.projectId")
            if (event.eventType.isBlank()) issues += error("missing_event_type", "Scheduled world event '${event.id}' requires eventType.", "$path.eventType")
            if (event.dueTurnNumber < 1) issues += error("invalid_due_turn", "Scheduled world event '${event.id}' dueTurnNumber must be >= 1.", "$path.dueTurnNumber")
            if (event.visibility !in VISIBILITIES || event.visibility == "author_only") {
                issues += error("invalid_visibility", "Scheduled world event '${event.id}' must be runtime-visible and not author_only.", "$path.visibility")
            }
            val publicEffect = event.payload.stringValue("publicEffect") ?: event.payload.stringValue("public_effect")
            if (publicEffect.isNullOrBlank()) {
                issues += error("missing_public_effect", "Scheduled world event '${event.id}' requires payload.publicEffect.", "$path.payload.publicEffect")
            }
            if (event.status !in setOf("scheduled", "triggered")) {
                issues += error("invalid_event_status", "Scheduled world event '${event.id}' status must be scheduled or triggered.", "$path.status")
            }
        }
        return report(pack, issues)
    }

    companion object {
        private val ID_REGEX = Regex("[A-Za-z0-9._:-]+")
        private val VISIBILITIES = setOf("public", "private", "director_only", "author_only")
        private val STATUSES = setOf("active", "draft", "deprecated", "archived")
    }
}

class CanonSemanticValidator(private val repository: SqliteStoryRepository) {
    fun validate(pack: SettingPack): SettingPackValidationReport {
        val issues = mutableListOf<SettingPackValidationIssue>()
        val idsByType = pack.allTypedEntities().groupBy({ it.first }, { it.second.id })
        val allLocalIds = pack.allTypedEntities().map { it.second.id }.toSet()
        val localByType = idsByType.mapValues { it.value.toSet() }

        pack.entities.characters.forEach { character ->
            character.startingItems.filterNot { exists(pack, "item", it, localByType) }.forEach {
                issues += error("unknown_reference", "Character '${character.id}' references unknown starting item '$it'.", "$.entities.characters[${character.id}].startingItems")
            }
            character.startingTechniques.filterNot { exists(pack, "technique", it, localByType) }.forEach {
                issues += error("unknown_reference", "Character '${character.id}' references unknown starting technique '$it'.", "$.entities.characters[${character.id}].startingTechniques")
            }
            character.startingKnowledge.forEach { secretId ->
                val secret = pack.entities.secrets.firstOrNull { it.id == secretId }
                if (secret != null && secret.visibility == "author_only" && character.id !in secret.knownBy) {
                    issues += error("author_only_knowledge_leak", "Author-only secret '$secretId' cannot enter '${character.id}' initial knowledge.", "$.entities.characters[${character.id}].startingKnowledge")
                }
            }
            character.initialRuntimeTemplate?.let { template ->
                if (template.initialLocationId.isBlank()) {
                    issues += error("missing_initial_runtime_template_field", "Character '${character.id}' initialRuntimeTemplate.initialLocationId is required.", "$.entities.characters[${character.id}].initialRuntimeTemplate.initialLocationId")
                } else if (!exists(pack, "location", template.initialLocationId, localByType)) {
                    issues += error("unknown_reference", "Character '${character.id}' initialRuntimeTemplate references unknown location '${template.initialLocationId}'.", "$.entities.characters[${character.id}].initialRuntimeTemplate.initialLocationId")
                }
                if (template.initialGoal.isBlank()) {
                    issues += error("missing_initial_runtime_template_field", "Character '${character.id}' initialRuntimeTemplate.initialGoal is required.", "$.entities.characters[${character.id}].initialRuntimeTemplate.initialGoal")
                }
                if (template.dominantEmotion.isBlank()) {
                    issues += error("missing_initial_runtime_template_field", "Character '${character.id}' initialRuntimeTemplate.dominantEmotion is required.", "$.entities.characters[${character.id}].initialRuntimeTemplate.dominantEmotion")
                }
                template.emotionState.filterValues { it !in 0..100 }.forEach { (emotion, value) ->
                    issues += error("invalid_initial_emotion_state", "Character '${character.id}' emotion '$emotion' must be between 0 and 100, got $value.", "$.entities.characters[${character.id}].initialRuntimeTemplate.emotionState.$emotion")
                }
                if (template.initiativeLevel !in 0..100) {
                    issues += error("invalid_initial_initiative", "Character '${character.id}' initiativeLevel must be between 0 and 100.", "$.entities.characters[${character.id}].initialRuntimeTemplate.initiativeLevel")
                }
                val directorGoals = pack.entities.storyThreads.map { it.directorGoal }.filter { it.isNotBlank() }
                if (directorGoals.any { it == template.initialGoal || template.initialGoal.contains(it) }) {
                    issues += error("director_goal_leak", "Character '${character.id}' initialGoal cannot copy a story thread directorGoal.", "$.entities.characters[${character.id}].initialRuntimeTemplate.initialGoal")
                }
                validateInitialRuntimeKnowledgeBoundary(pack, character.id, template.allRuntimeText(), localByType).forEach { issue ->
                    issues += issue
                }
                template.availableActions.forEachIndexed { actionIndex, action ->
                    val tokens = Regex("[A-Za-z0-9._:-]+").findAll(action).map { it.value }.toList()
                    tokens.filter { it.startsWith("item-") && !exists(pack, "item", it, localByType) }.forEach {
                        issues += error("invalid_initial_character_action", "Character '${character.id}' availableAction references unknown item '$it'.", "$.entities.characters[${character.id}].initialRuntimeTemplate.availableActions[$actionIndex]")
                    }
                    tokens.filter { it.startsWith("technique-") && !exists(pack, "technique", it, localByType) }.forEach {
                        issues += error("invalid_initial_character_action", "Character '${character.id}' availableAction references unknown technique '$it'.", "$.entities.characters[${character.id}].initialRuntimeTemplate.availableActions[$actionIndex]")
                    }
                    tokens.filter { it.startsWith("secret-") }.forEach {
                        issues += error("initial_runtime_knowledge_leak", "Character '${character.id}' availableAction cannot reference hidden secret '$it'.", "$.entities.characters[${character.id}].initialRuntimeTemplate.availableActions[$actionIndex]")
                    }
                }
            }
        }

        pack.entities.items.forEach { item ->
            if (item.initialOwnerId != null && item.initialLocationId != null) {
                issues += error("item_location_owner_conflict", "Item '${item.id}' cannot have both initialOwnerId and initialLocationId.", "$.entities.items[${item.id}]")
            }
            item.initialOwnerId?.takeUnless { exists(pack, "character", it, localByType) }?.let {
                issues += error("unknown_reference", "Item '${item.id}' references unknown owner '$it'.", "$.entities.items[${item.id}].initialOwnerId")
            }
            item.initialLocationId?.takeUnless { exists(pack, "location", it, localByType) }?.let {
                issues += error("unknown_reference", "Item '${item.id}' references unknown location '$it'.", "$.entities.items[${item.id}].initialLocationId")
            }
        }
        pack.entities.techniques.forEach { technique ->
            technique.prerequisites.filterNot { exists(pack, "technique", it, localByType) }.forEach {
                issues += error("unknown_reference", "Technique '${technique.id}' references unknown prerequisite '$it'.", "$.entities.techniques[${technique.id}].prerequisites")
            }
            val powerful = technique.effects.any { it.contains("powerful", true) || it.contains("禁術") || it.contains("ultimate", true) } ||
                (technique.metadata["powerLevel"]?.jsonPrimitive?.intOrNull ?: 0) >= 8
            if (powerful && technique.costs.isEmpty() && technique.limitations.isEmpty()) {
                issues += error("unbounded_powerful_technique", "Powerful technique '${technique.id}' must declare costs or limitations.", "$.entities.techniques[${technique.id}]")
            }
            technique.forbiddenUses.intersect(technique.effects.toSet()).forEach {
                issues += error("effect_forbidden_use_conflict", "Technique '${technique.id}' both allows and forbids '$it'.", "$.entities.techniques[${technique.id}]")
            }
        }
        pack.entities.locations.forEach { location ->
            location.parentLocationId?.takeUnless { exists(pack, "location", it, localByType) }?.let {
                issues += error("unknown_reference", "Location '${location.id}' references unknown parent '$it'.", "$.entities.locations[${location.id}].parentLocationId")
            }
        }
        pack.entities.scenes.forEach { scene ->
            scene.locationId?.takeUnless { exists(pack, "location", it, localByType) }?.let {
                issues += error("unknown_reference", "Scene '${scene.id}' references unknown location '$it'.", "$.entities.scenes[${scene.id}].locationId")
            }
        }
        pack.locationConnections.forEachIndexed { index, connection ->
            if (!exists(pack, "location", connection.fromLocationId, localByType)) {
                issues += error("unknown_reference", "Location connection '${connection.id}' references unknown fromLocationId '${connection.fromLocationId}'.", "$.locationConnections[$index].fromLocationId")
            }
            if (!exists(pack, "location", connection.toLocationId, localByType)) {
                issues += error("unknown_reference", "Location connection '${connection.id}' references unknown toLocationId '${connection.toLocationId}'.", "$.locationConnections[$index].toLocationId")
            }
        }
        pack.sceneTransitions.forEachIndexed { index, transition ->
            if (!exists(pack, "scene", transition.fromSceneId, localByType)) {
                issues += error("unknown_reference", "Scene transition '${transition.id}' references unknown fromSceneId '${transition.fromSceneId}'.", "$.sceneTransitions[$index].fromSceneId")
            }
            if (!exists(pack, "scene", transition.toSceneId, localByType)) {
                issues += error("unknown_reference", "Scene transition '${transition.id}' references unknown toSceneId '${transition.toSceneId}'.", "$.sceneTransitions[$index].toSceneId")
            }
            val fromLocation = sceneLocation(pack, transition.fromSceneId)
            val toLocation = sceneLocation(pack, transition.toSceneId)
            if (fromLocation != null && toLocation != null && fromLocation != toLocation && !locationConnectionExists(pack, fromLocation, toLocation)) {
                issues += error("missing_location_connection", "Scene transition '${transition.id}' requires a location connection from '$fromLocation' to '$toLocation'.", "$.sceneTransitions[$index]")
            }
            transition.prerequisites.stringValue("requiredFactId")?.let { factId ->
                if (!exists(pack, "fact", factId, localByType)) {
                    issues += error("unknown_reference", "Scene transition '${transition.id}' requires unknown fact '$factId'.", "$.sceneTransitions[$index].prerequisites.requiredFactId")
                }
            }
        }
        val authorOnlySecrets = pack.entities.secrets.filter { it.visibility == "author_only" }
        pack.scheduledWorldEvents.forEachIndexed { index, event ->
            val payloadText = event.payload.allStringValues().joinToString("\n")
            authorOnlySecrets.forEach { secret ->
                val secretText = secret.bodyText()
                if (payloadText.contains(secret.id) || (secretText.isNotBlank() && payloadText.contains(secretText))) {
                    issues += error("author_only_world_event_leak", "Scheduled world event '${event.id}' payload leaks author-only secret '${secret.id}'.", "$.scheduledWorldEvents[$index].payload")
                }
            }
        }
        pack.entities.sceneContracts.forEach { contract ->
            val sceneId = contract.sceneId ?: contract.metadata.stringValue("sceneId")
            if (sceneId == null || !exists(pack, "scene", sceneId, localByType)) {
                issues += error("unknown_reference", "Scene contract '${contract.id}' references unknown scene '${sceneId.orEmpty()}'.", "$.entities.sceneContracts[${contract.id}].sceneId")
            }
        }
        pack.relations.forEachIndexed { index, relation ->
            if (!exists(pack, relation.fromType, relation.fromId, localByType)) {
                issues += error("unknown_reference", "Relation '${relation.id}' has unknown source ${relation.fromType}:${relation.fromId}.", "$.relations[$index].fromId")
            }
            if (!exists(pack, relation.toType, relation.toId, localByType)) {
                issues += error("unknown_reference", "Relation '${relation.id}' has unknown target ${relation.toType}:${relation.toId}.", "$.relations[$index].toId")
            }
            if (!compatible(relation)) {
                issues += error("incompatible_relation", "Relation '${relation.relation}' is not compatible with ${relation.fromType}->${relation.toType}.", "$.relations[$index]")
            }
        }

        issues += detectCycles("technique", pack.entities.techniques.associate { it.id to it.prerequisites })
        issues += detectCycles("location", pack.entities.locations.mapNotNull { entity -> entity.parentLocationId?.let { entity.id to listOf(it) } }.toMap())
        val hardRules = pack.entities.worldRules.filter { it.metadata.stringValue("severity") == "hard" || it.metadata["hard"]?.jsonPrimitive?.booleanOrNull == true }
        hardRules.groupBy { it.metadata.stringValue("conflictKey") ?: it.title.ifBlank { it.name } }
            .filterKeys { it.isNotBlank() }
            .filterValues { rules -> rules.map { it.bodyText() }.toSet().size > 1 }
            .forEach { (key, _) -> issues += error("hard_rule_conflict", "Hard world rules conflict on key '$key'.", "$.entities.worldRules") }
        if (pack.mode == "PATCH_CANON" && repository.activeSessionIds(pack.projectId).isNotEmpty()) {
            val updatesExisting = pack.allTypedEntities().any { (type, entity) -> repository.latestEntityRevision(pack.projectId, type, entity.id) != null } ||
                pack.locationConnections.any { repository.targetExists(pack.projectId, "location_connection", it.id) } ||
                pack.sceneTransitions.any { repository.targetExists(pack.projectId, "scene_transition", it.id) } ||
                pack.scheduledWorldEvents.any { repository.targetExists(pack.projectId, "world_event", it.id) }
            if (updatesExisting) {
                issues += error("active_session_patch_blocked", "PATCH_CANON cannot update existing canon while active sessions exist.", "$.mode")
            }
        }
        idsByType["items"]?.groupingBy { it }?.eachCount()?.filterValues { it > 1 }?.keys.orEmpty().forEach {
            issues += error("unique_item_multi_owner", "Unique item '$it' appears more than once.", "$.entities.items")
        }
        issues += validateOpeningRuntimeTemplates(pack, localByType)
        return report(pack, issues)
    }

    private fun validateOpeningRuntimeTemplates(pack: SettingPack, localByType: Map<String, Set<String>>): List<SettingPackValidationIssue> {
        val openingSceneId = pack.entities.projects.firstOrNull { it.id == pack.projectId }?.metadata?.stringValue("activeSceneId")
            ?: pack.entities.projects.firstOrNull()?.metadata?.stringValue("activeSceneId")
            ?: pack.entities.scenes.firstOrNull()?.id
            ?: return emptyList()
        val openingScene = pack.entities.scenes.firstOrNull { it.id == openingSceneId } ?: return emptyList()
        val openingLocation = openingScene.locationId ?: return emptyList()
        val activeCharacterIds = buildSet {
            pack.relations
                .filter { it.relation.equals("LOCATED_IN", true) }
                .filter { it.fromType.toCanonicalEntityType() == "character" && it.toType.toCanonicalEntityType() == "location" && it.toId == openingLocation }
                .forEach { add(it.fromId) }
            pack.entities.characters
                .filter { it.initialRuntimeTemplate?.initialLocationId == openingLocation }
                .forEach { add(it.id) }
        }
        return activeCharacterIds.flatMap { characterId ->
            val character = pack.entities.characters.firstOrNull { it.id == characterId }
            when {
                character == null && !exists(pack, "character", characterId, localByType) ->
                    listOf(error("unknown_reference", "Opening scene references unknown active character '$characterId'.", "$.relations"))
                character?.initialRuntimeTemplate == null ->
                    listOf(error("missing_initial_runtime_template", "Opening active character '$characterId' requires initialRuntimeTemplate.", "$.entities.characters[$characterId].initialRuntimeTemplate"))
                character.initialRuntimeTemplate.initialLocationId != openingLocation ->
                    listOf(error("invalid_initial_character_location", "Opening active character '$characterId' initial location must match opening scene location '$openingLocation'.", "$.entities.characters[$characterId].initialRuntimeTemplate.initialLocationId"))
                else -> emptyList()
            }
        }
    }

    private fun validateInitialRuntimeKnowledgeBoundary(
        pack: SettingPack,
        characterId: String,
        texts: List<String>,
        localByType: Map<String, Set<String>>,
    ): List<SettingPackValidationIssue> {
        val issues = mutableListOf<SettingPackValidationIssue>()
        pack.entities.secrets.filter { !it.revealed }.forEach { secret ->
            val secretText = secret.bodyText()
            val leakedId = texts.any { it.contains(secret.id) }
            val leakedContent = secretText.isNotBlank() && texts.any { it.contains(secretText) }
            if (leakedId || leakedContent) {
                issues += error("initial_runtime_knowledge_leak", "Character '$characterId' initialRuntimeTemplate leaks hidden secret '${secret.id}'.", "$.entities.characters[$characterId].initialRuntimeTemplate")
            }
        }
        pack.entities.secrets.filter { it.visibility == "author_only" }.forEach { secret ->
            val secretText = secret.bodyText()
            if (texts.any { it.contains(secret.id) || (secretText.isNotBlank() && it.contains(secretText)) }) {
                issues += error("author_only_knowledge_leak", "Author-only secret '${secret.id}' cannot enter '$characterId' initial runtime.", "$.entities.characters[$characterId].initialRuntimeTemplate")
            }
        }
        return issues
    }

    private fun exists(pack: SettingPack, type: String, id: String, localByType: Map<String, Set<String>>): Boolean {
        val canonical = type.toCanonicalEntityType()
        return id in localByType[canonical].orEmpty() || repository.targetExists(pack.projectId, canonical, id)
    }

    private fun sceneLocation(pack: SettingPack, sceneId: String): String? =
        pack.entities.scenes.firstOrNull { it.id == sceneId }?.locationId
            ?: repository.tableRows("scenes").firstOrNull { it["project_id"] == pack.projectId && it["id"] == sceneId }?.get("location_id")

    private fun locationConnectionExists(pack: SettingPack, fromLocationId: String, toLocationId: String): Boolean {
        val local = pack.locationConnections.any {
            it.fromLocationId == fromLocationId && it.toLocationId == toLocationId ||
                it.bidirectional && it.fromLocationId == toLocationId && it.toLocationId == fromLocationId
        }
        if (local) return true
        return repository.tableRows("location_connections").any {
            it["project_id"] == pack.projectId &&
                (
                    it["from_location_id"] == fromLocationId && it["to_location_id"] == toLocationId ||
                        it["bidirectional"] == "1" && it["from_location_id"] == toLocationId && it["to_location_id"] == fromLocationId
                    )
        }
    }

    private fun compatible(relation: SettingPackRelation): Boolean =
        when (relation.relation.uppercase()) {
            "LOCATED_IN" -> relation.fromType.toCanonicalEntityType() in setOf("character", "item", "scene") && relation.toType.toCanonicalEntityType() == "location"
            "OWNS" -> relation.fromType.toCanonicalEntityType() == "character" && relation.toType.toCanonicalEntityType() == "item"
            "KNOWS" -> relation.fromType.toCanonicalEntityType() == "character" && relation.toType.toCanonicalEntityType() in setOf("fact", "secret")
            "REQUIRES", "PREREQUISITE_FOR" -> relation.fromType.toCanonicalEntityType() in setOf("technique", "scene", "story_thread") && relation.toType.toCanonicalEntityType() in setOf("technique", "fact", "item", "scene")
            "BLOCKS", "AFFECTS", "DEPENDS_ON", "PART_OF", "PARENT_OF", "RELATES_TO" -> true
            else -> false
        }

    private fun detectCycles(type: String, edges: Map<String, List<String>>): List<SettingPackValidationIssue> {
        val issues = mutableListOf<SettingPackValidationIssue>()
        fun visit(node: String, stack: List<String>, seen: MutableSet<String>) {
            if (node in stack) {
                issues += error("dependency_cycle", "Cycle in $type dependencies: ${(stack + node).joinToString(" -> ")}.", "$.entities.$type")
                return
            }
            if (!seen.add(node)) return
            edges[node].orEmpty().forEach { visit(it, stack + node, seen) }
        }
        edges.keys.sorted().forEach { visit(it, emptyList(), mutableSetOf()) }
        return issues
    }
}

class SettingPackDiffService(private val repository: SqliteStoryRepository) {
    fun diff(pack: SettingPack, canonicalHash: String): SettingPackDiff {
        val entityEntries = pack.allTypedEntities().map { (type, entity) ->
            val latest = repository.latestEntityRevision(pack.projectId, type, entity.id)
            val previousVersion = latest?.get("entity_version")?.toIntOrNull()
            val latestSnapshot = latest?.get("snapshot_json")
            val currentSnapshot = JsonSupport.json.encodeToString(entity)
            val action = when {
                latest == null -> "ADD"
                latestSnapshot == currentSnapshot -> "NO_CHANGE"
                else -> "UPDATE"
            }
            SettingPackDiffEntry(
                action = action,
                entityType = type,
                entityId = entity.id,
                previousVersion = previousVersion,
                newVersion = entity.entityVersion,
                fieldChanges = if (latestSnapshot == null || latestSnapshot == currentSnapshot) emptyMap() else mapOf("snapshot" to if (entity.visibility == "author_only") "***" else "changed"),
                sensitive = entity.visibility == "author_only" || type == "secret",
            )
        }
        val topologyEntries =
            pack.locationConnections.map { topologyDiffEntry(pack.projectId, "location_connection", it.id, it.visibility) } +
                pack.sceneTransitions.map { topologyDiffEntry(pack.projectId, "scene_transition", it.id, it.visibility) } +
                pack.scheduledWorldEvents.map { topologyDiffEntry(pack.projectId, "world_event", it.id, it.visibility) }
        val entries = entityEntries + topologyEntries
        return SettingPackDiff(pack.packId, pack.packVersion, entries, canonicalHash)
    }

    private fun topologyDiffEntry(projectId: String, type: String, id: String, visibility: String): SettingPackDiffEntry =
        SettingPackDiffEntry(
            action = if (repository.targetExists(projectId, type, id)) "NO_CHANGE" else "ADD",
            entityType = type,
            entityId = id,
            previousVersion = if (repository.targetExists(projectId, type, id)) 1 else null,
            newVersion = 1,
            sensitive = visibility == "author_only",
        )
}

class ImpactAnalyzer(private val repository: SqliteStoryRepository) {
    fun analyze(pack: SettingPack, diff: SettingPackDiff): SettingPackImpactReport {
        val changedRoots = diff.entries.filter { it.action != "NO_CHANGE" }.map { "${it.entityType}:${it.entityId}" }.sorted()
        val activeSessions = repository.activeSessionIds(pack.projectId)
        val dependencyRows = repository.listDependencyRows(pack.projectId)
        val paths = changedRoots.flatMap { root ->
            val (type, id) = root.split(":", limit = 2)
            traverse(type, id, dependencyRows).map { target ->
                ImpactPath(
                    rootType = type,
                    rootId = id,
                    impactedType = target.first,
                    impactedId = target.second,
                    relationPath = target.third,
                    severity = if (target.first in setOf("scene", "story_thread", "character")) "MEDIUM" else "LOW",
                    reason = "Dependency path from changed canon.",
                )
            }
        }.distinctBy { listOf(it.rootType, it.rootId, it.impactedType, it.impactedId, it.relationPath.joinToString(">")).joinToString(":") }
            .sortedWith(compareBy<ImpactPath> { it.rootType }.thenBy { it.rootId }.thenBy { it.impactedType }.thenBy { it.impactedId })
        val touchesRuntimeTopology = pack.locationConnections.isNotEmpty() ||
            pack.sceneTransitions.isNotEmpty() ||
            pack.scheduledWorldEvents.isNotEmpty()
        val blockers = if (pack.mode == "PATCH_CANON" && activeSessions.isNotEmpty() && (diff.entries.any { it.action != "NO_CHANGE" } || touchesRuntimeTopology)) {
            listOf("active_session_patch_blocked")
        } else {
            emptyList()
        }
        return SettingPackImpactReport(
            packId = pack.packId,
            packVersion = pack.packVersion,
            changedRoots = changedRoots,
            impactedSessions = activeSessions,
            impactedCharacters = paths.filter { it.impactedType == "character" }.map { it.impactedId }.distinct().sorted(),
            impactedScenes = paths.filter { it.impactedType == "scene" }.map { it.impactedId }.distinct().sorted(),
            impactedThreads = paths.filter { it.impactedType == "story_thread" }.map { it.impactedId }.distinct().sorted(),
            paths = paths.take(100),
            blockers = blockers,
        )
    }

    private fun traverse(type: String, id: String, rows: List<Map<String, String>>): List<Triple<String, String, List<String>>> {
        val queue = ArrayDeque<Triple<String, String, List<String>>>()
        val visited = mutableSetOf<String>()
        queue += Triple(type, id, emptyList())
        val result = mutableListOf<Triple<String, String, List<String>>>()
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val key = "${current.first}:${current.second}"
            if (!visited.add(key) || current.third.size >= 4) continue
            rows.filter {
                (it["from_type"] == current.first && it["from_id"] == current.second) ||
                    (it["to_type"] == current.first && it["to_id"] == current.second)
            }.sortedBy { it["id"] }.forEach { row ->
                val nextType = if (row["from_type"] == current.first && row["from_id"] == current.second) row["to_type"].orEmpty() else row["from_type"].orEmpty()
                val nextId = if (row["from_type"] == current.first && row["from_id"] == current.second) row["to_id"].orEmpty() else row["from_id"].orEmpty()
                val nextPath = current.third + "${row["relation"]}:${nextType}:${nextId}"
                result += Triple(nextType, nextId, nextPath)
                queue += Triple(nextType, nextId, nextPath)
            }
        }
        return result
    }
}

class SettingPackImportService(private val repository: SqliteStoryRepository) {
    private val schemaValidator = SettingPackSchemaValidator(repository)
    private val semanticValidator = CanonSemanticValidator(repository)
    private val diffService = SettingPackDiffService(repository)
    private val impactAnalyzer = ImpactAnalyzer(repository)

    fun preview(pack: SettingPack): SettingPackPreview {
        val canonicalHash = canonicalHash(pack)
        val schema = schemaValidator.validate(pack)
        val semantic = semanticValidator.validate(pack)
        val issues = schema.issues + semantic.issues
        val validation = SettingPackValidationReport(pack.packId, pack.packVersion, issues.none { it.severity == "error" }, issues)
        val diff = diffService.diff(pack, canonicalHash)
        val impact = impactAnalyzer.analyze(pack, diff)
        val blockers = impact.blockers.map { error(it, "Impact blocker: $it.", "$.impact") }
        val finalValidation = validation.copy(valid = validation.isValid && blockers.isEmpty(), issues = validation.issues + blockers)
        return SettingPackPreview(pack, finalValidation, diff, impact, canonicalHash)
    }

    fun apply(pack: SettingPack, sourcePath: String = ""): SettingPackApplyResult {
        val preview = preview(pack)
        if (!preview.validation.isValid) {
            error(preview.validation.issues.joinToString(separator = "\n") { "${it.code}: ${it.message}" })
        }
        val existingHash = repository.settingPackHash(pack.packId, pack.packVersion)
        if (existingHash != null && existingHash != preview.canonicalHash) {
            error("pack_hash_conflict: ${pack.packId} v${pack.packVersion} already exists with a different canonical hash.")
        }
        return repository.applySettingPack(preview, sourcePath)
    }

    fun export(projectId: String, packId: String, packVersion: Int): SettingPack =
        repository.settingPackJson(packId, packVersion)?.let { JsonSupport.json.decodeFromString<SettingPack>(it) }
            ?: error("Setting pack not found: $packId v$packVersion for project $projectId")
}

class DependencyGraphService(private val repository: SqliteStoryRepository) {
    fun show(projectId: String, entityType: String? = null, entityId: String? = null, depth: Int = 2): List<String> {
        val rows = repository.listDependencyRows(projectId)
        val filtered = if (entityType == null || entityId == null) {
            rows
        } else {
            bounded(rows, entityType.toCanonicalEntityType(), entityId, depth)
        }
        return filtered.map {
            "${it["from_type"]}:${it["from_id"]} -${it["relation"]}-> ${it["to_type"]}:${it["to_id"]} runtime=${it["runtime_relevant"]} visibility=${it["visibility"]}"
        }
    }

    private fun bounded(rows: List<Map<String, String>>, type: String, id: String, depth: Int): List<Map<String, String>> {
        val result = mutableListOf<Map<String, String>>()
        val queue = ArrayDeque<Triple<String, String, Int>>()
        val seen = mutableSetOf<String>()
        queue += Triple(type, id, 0)
        while (queue.isNotEmpty()) {
            val (currentType, currentId, currentDepth) = queue.removeFirst()
            if (currentDepth > depth || !seen.add("$currentType:$currentId")) continue
            rows.filter { it["from_type"] == currentType && it["from_id"] == currentId || it["to_type"] == currentType && it["to_id"] == currentId }
                .sortedBy { it["id"] }
                .forEach { row ->
                    result += row
                    val nextType = if (row["from_type"] == currentType && row["from_id"] == currentId) row["to_type"].orEmpty() else row["from_type"].orEmpty()
                    val nextId = if (row["from_type"] == currentType && row["from_id"] == currentId) row["to_id"].orEmpty() else row["from_id"].orEmpty()
                    queue += Triple(nextType, nextId, currentDepth + 1)
                }
        }
        return result.distinctBy { it["id"] }
    }
}

class CanonConsistencyValidator(private val repository: SqliteStoryRepository) {
    fun validate(projectId: String): SettingPackValidationReport {
        val issues = mutableListOf<SettingPackValidationIssue>()
        val revisions = repository.listEntityRevisionRows(projectId)
        val duplicateActive = revisions.groupBy { Triple(it["entity_type"], it["entity_id"], it["entity_version"]) }
            .filterValues { it.size > 1 }
        duplicateActive.keys.forEach { issues += error("duplicate_revision", "Duplicate revision for ${it.first}:${it.second} v${it.third}.", "entity_revisions") }
        repository.listDependencyRows(projectId).forEach { row ->
            if (row["visibility"] == "author_only" && row["runtime_relevant"] == "1") {
                issues += error("author_only_runtime_dependency", "Author-only dependency '${row["id"]}' cannot be runtime relevant.", "dependency_edges.${row["id"]}")
            }
        }
        return SettingPackValidationReport(projectId, 0, issues.isEmpty(), issues)
    }
}

class SettingContextResolver(private val repository: SqliteStoryRepository, private val maxBytes: Int = 8192) {
    fun enrich(context: ContextPacket): ContextPacket {
        val candidates = resolveCandidates(context)
        val selected = mutableListOf<RelevantSetting>()
        var bytes = 2
        var omitted = 0
        candidates.forEach { candidate ->
            val candidateBytes = JsonSupport.json.encodeToString(candidate).toByteArray(Charsets.UTF_8).size
            if (bytes + candidateBytes <= maxBytes) {
                selected += candidate
                bytes += candidateBytes
            } else {
                omitted += 1
            }
        }
        val budget = SettingContextBudget(
            maxBytes = maxBytes,
            actualBytes = bytes,
            includedSettingCount = selected.size,
            omittedSettingCount = omitted,
            truncationApplied = omitted > 0,
            reasonCodes = if (omitted > 0) listOf("setting_context_budget_exceeded") else emptyList(),
            reasons = if (omitted > 0) listOf("setting_context_budget_exceeded") else emptyList(),
        )
        val revisions = selected.mapNotNull { repository.latestEntityRevision(context.project.id, it.entityType, it.entityId)?.get("id") }.distinct().sorted()
        val packs = repository.settingPackRows(context.project.id).map { "${it["pack_id"]}@${it["pack_version"]}" }
        return context.copy(
            relevantSettings = selected,
            settingContextBudget = budget,
            canonRevisionSummary = CanonRevisionSummary(
                importedPacks = packs,
                relevantRevisionIds = revisions,
                latestImportId = repository.tableRows("setting_pack_imports")
                    .filter { it["project_id"] == context.project.id && it["status"] in setOf("applied", "no_change") }
                    .maxByOrNull { it["created_at"].orEmpty() }
                    ?.get("id"),
            ),
        )
    }

    private fun resolveCandidates(context: ContextPacket): List<RelevantSetting> {
        val projectId = context.project.id
        val relevantIds = buildSet {
            add("project:${context.project.id}")
            context.activeScene?.let {
                add("scene:${it.id}")
                it.locationId?.let { locationId -> add("location:$locationId") }
            }
            context.characters.forEach { add("character:${it.id}") }
            context.characterStates.forEach { state ->
                state.locationId?.let { add("location:$it") }
                state.inventory.forEach { add("item:$it") }
            }
            context.items.forEach { item ->
                if (item.ownerCharacterId != null || item.locationId == context.activeScene?.locationId) add("item:${item.id}")
            }
            context.techniques.forEach { technique ->
                if (technique.ownerCharacterId != null) add("technique:${technique.id}")
            }
            context.activeStoryThreads.forEach { add("story_thread:${it.threadId}") }
            context.sceneRequirements.forEach { add("scene_requirement:${it.requirementId}") }
            context.availableTransitions.forEach { transition ->
                add("scene_transition:${transition.transitionId}")
                add("scene:${transition.toSceneId}")
                transition.targetLocation?.let { add("location:$it") }
            }
            context.pendingWorldEvents.forEach { add("world_event:${it.eventId}") }
        }
        val runtimeSettings = runtimeRelevantSettings(context)
        val dependencyRows = repository.listDependencyRows(projectId)
        val expandedIds = expandRelevantIds(relevantIds, dependencyRows)
        val revisions = repository.listEntityRevisionRows(projectId)
            .filter { "${it["entity_type"]}:${it["entity_id"]}" in expandedIds }
            .filter { it["visibility"] != "author_only" }
            .groupBy { "${it["entity_type"]}:${it["entity_id"]}" }
            .mapNotNull { (_, rows) -> rows.maxByOrNull { it["entity_version"]?.toIntOrNull() ?: 0 } }
            .sortedWith(compareBy<Map<String, String>> { it["entity_type"] }.thenBy { it["entity_id"] })
        val revisionSettings = revisions.mapNotNull { revision ->
            val type = revision["entity_type"].orEmpty()
            val id = revision["entity_id"].orEmpty()
            val entity = revision["snapshot_json"]?.let { runCatching { JsonSupport.json.decodeFromString<SettingPackEntity>(it) }.getOrNull() }
            if (entity == null) null else RelevantSetting(
                entityType = type,
                entityId = id,
                name = entity.displayName(),
                summary = entity.publicSummary.ifBlank { entity.summary.ifBlank { entity.description.ifBlank { entity.content.takeUnless { type == "secret" }.orEmpty() } } },
                visibility = entity.visibility,
                sourcePackId = revision["source_pack_id"],
                sourcePackVersion = revision["source_pack_version"]?.toIntOrNull(),
                relationPath = dependencyRows
                    .filter { it["from_id"] == id || it["to_id"] == id }
                    .map { "${it["from_type"]}:${it["from_id"]}-${it["relation"]}->${it["to_type"]}:${it["to_id"]}" }
                    .take(3),
                requiredRuntimeUse = type in setOf("world_rule", "technique", "item", "scene", "story_thread"),
                metadata = JsonObject(
                    buildMap {
                        entity.costs.takeIf { it.isNotEmpty() }?.let { put("costs", JsonArray(it.map(::JsonPrimitive))) }
                        entity.limitations.takeIf { it.isNotEmpty() }?.let { put("limitations", JsonArray(it.map(::JsonPrimitive))) }
                        entity.effects.takeIf { it.isNotEmpty() }?.let { put("effects", JsonArray(it.map(::JsonPrimitive))) }
                    },
                ),
            )
        }
        return (runtimeSettings + revisionSettings).distinctBy { "${it.entityType}:${it.entityId}" }
    }

    private fun runtimeRelevantSettings(context: ContextPacket): List<RelevantSetting> =
        buildList {
            val projectId = context.project.id
            val sceneTitles = repository.tableRows("scenes")
                .filter { it["project_id"] == projectId }
                .associate { it["id"].orEmpty() to it["title"].orEmpty() }
            context.availableTransitions.forEach { transition ->
                add(
                    RelevantSetting(
                        entityType = "scene_transition",
                        entityId = transition.transitionId,
                        name = "Available transition to ${sceneTitles[transition.toSceneId].orEmpty().ifBlank { transition.toSceneId }}",
                        summary = listOf(
                            "Target scene: ${transition.toSceneId}",
                            transition.targetLocation?.let { "Target location: $it" },
                            transition.playerIntentRequired.takeIf { it.isNotBlank() }?.let { "Requires player intent: $it" },
                            transition.visibleReason.takeIf { it.isNotBlank() }?.let { "Status: $it" },
                        ).filterNotNull().joinToString("; "),
                        visibility = "public",
                        requiredRuntimeUse = true,
                        metadata = JsonObject(
                            mapOf(
                                "targetSceneId" to JsonPrimitive(transition.toSceneId),
                                "targetLocationId" to JsonPrimitive(transition.targetLocation.orEmpty()),
                                "playerIntentRequired" to JsonPrimitive(transition.playerIntentRequired),
                                "prerequisitesSatisfied" to JsonPrimitive(transition.prerequisitesSatisfied),
                                "unmetPrerequisites" to JsonArray(transition.unmetPrerequisites.map(::JsonPrimitive)),
                                "travelMinutes" to JsonPrimitive(transition.travelMinutes),
                                "forcedAllowed" to JsonPrimitive(transition.forcedAllowed),
                            ),
                        ),
                    ),
                )
                val fromLocation = context.activeScene?.locationId
                val toLocation = transition.targetLocation
                if (fromLocation != null && toLocation != null && fromLocation != toLocation) {
                    findLocationConnection(projectId, fromLocation, toLocation)?.let { row ->
                        add(
                            RelevantSetting(
                                entityType = "location_connection",
                                entityId = row["id"].orEmpty(),
                                name = "Location connection ${row["from_location_id"]} -> ${row["to_location_id"]}",
                                summary = "Reachability between ${row["from_location_id"]} and ${row["to_location_id"]}; travel ${row["travel_minutes"] ?: "0"} minute(s).",
                                visibility = row["visibility"].orEmpty().ifBlank { "public" },
                                requiredRuntimeUse = true,
                                metadata = JsonObject(
                                    mapOf(
                                        "fromLocationId" to JsonPrimitive(row["from_location_id"].orEmpty()),
                                        "toLocationId" to JsonPrimitive(row["to_location_id"].orEmpty()),
                                        "connectionType" to JsonPrimitive(row["connection_type"].orEmpty()),
                                        "travelMinutes" to JsonPrimitive(row["travel_minutes"]?.toIntOrNull() ?: 0),
                                        "bidirectional" to JsonPrimitive(row["bidirectional"] == "1"),
                                    ),
                                ),
                            ),
                        )
                    }
                }
            }
            context.pendingWorldEvents.forEach { event ->
                add(
                    RelevantSetting(
                        entityType = "world_event",
                        entityId = event.eventId,
                        name = event.type,
                        summary = event.publicEffect,
                        visibility = "public",
                        requiredRuntimeUse = true,
                        metadata = JsonObject(
                            mapOf(
                                "dueReason" to JsonPrimitive(event.dueReason),
                                "allowedNarrativeUses" to JsonArray(event.allowedNarrativeUses.map(::JsonPrimitive)),
                                "forbiddenCharacterUses" to JsonArray(event.forbiddenCharacterUses.map(::JsonPrimitive)),
                            ),
                        ),
                    ),
                )
            }
        }

    private fun findLocationConnection(projectId: String, fromLocationId: String, toLocationId: String): Map<String, String>? =
        repository.tableRows("location_connections")
            .filter { it["project_id"] == projectId && it["visibility"] != "hidden" }
            .firstOrNull {
                it["from_location_id"] == fromLocationId && it["to_location_id"] == toLocationId ||
                    it["bidirectional"] == "1" && it["from_location_id"] == toLocationId && it["to_location_id"] == fromLocationId
            }

    private fun expandRelevantIds(seed: Set<String>, rows: List<Map<String, String>>): Set<String> {
        val result = seed.toMutableSet()
        var frontier = seed
        repeat(2) {
            frontier = frontier.flatMap { key ->
                val (type, id) = key.split(":", limit = 2)
                rows.filter { row ->
                    row["runtime_relevant"] != "0" && (
                        row["from_type"] == type && row["from_id"] == id ||
                            row["to_type"] == type && row["to_id"] == id
                        )
                }.flatMap { row ->
                    listOf("${row["from_type"]}:${row["from_id"]}", "${row["to_type"]}:${row["to_id"]}")
                }
            }.filter { result.add(it) }.toSet()
        }
        return result
    }
}

fun canonicalHash(pack: SettingPack): String {
    val element = JsonSupport.json.encodeToJsonElement(SettingPack.serializer(), pack)
    val canonical = JsonSupport.json.encodeToString(sortJson(element))
    return sha256(canonical)
}

private fun SettingPack.allTypedEntities(): List<Pair<String, SettingPackEntity>> =
    listOf(
        "project" to entities.projects,
        "character" to entities.characters,
        "world_rule" to entities.worldRules,
        "faction" to entities.factions,
        "location" to entities.locations,
        "item" to entities.items,
        "technique" to entities.techniques,
        "fact" to entities.facts,
        "secret" to entities.secrets,
        "scene" to entities.scenes,
        "scene_contract" to entities.sceneContracts,
        "story_thread" to entities.storyThreads,
        "character_memory" to entities.characterMemories,
        "character_knowledge" to entities.characterKnowledge,
    ).flatMap { (type, entities) -> entities.map { type to it } }

private fun String.toCanonicalEntityType(): String =
    when (lowercase()) {
        "projects", "project" -> "project"
        "characters", "character" -> "character"
        "world_rules", "worldrule", "world_rule" -> "world_rule"
        "factions", "faction" -> "faction"
        "locations", "location" -> "location"
        "items", "item" -> "item"
        "techniques", "technique" -> "technique"
        "facts", "fact" -> "fact"
        "secrets", "secret" -> "secret"
        "scenes", "scene" -> "scene"
        "scene_contracts", "scenecontract", "scene_contract" -> "scene_contract"
        "story_threads", "storythread", "story_thread" -> "story_thread"
        "location_connections", "locationconnection", "location_connection" -> "location_connection"
        "scene_transitions", "scenetransition", "scene_transition" -> "scene_transition"
        "scheduled_world_events", "worldevent", "world_event" -> "world_event"
        "character_memories", "memory", "character_memory" -> "character_memory"
        "character_knowledge", "knowledge", "character_knowledge_claim" -> "character_knowledge"
        else -> lowercase()
    }

private fun report(pack: SettingPack, issues: List<SettingPackValidationIssue>): SettingPackValidationReport =
    SettingPackValidationReport(pack.packId, pack.packVersion, issues.none { it.severity == "error" }, issues)

private fun error(code: String, message: String, path: String): SettingPackValidationIssue =
    SettingPackValidationIssue(code, message, path, "error")

private fun JsonObject.stringValue(key: String): String? =
    this[key]?.jsonPrimitive?.contentOrNull

private fun JsonElement.allStringValues(): List<String> =
    when (this) {
        is JsonObject -> values.flatMap { it.allStringValues() }
        is JsonArray -> flatMap { it.allStringValues() }
        is JsonPrimitive -> listOfNotNull(contentOrNull)
        JsonNull -> emptyList()
    }

private fun SettingPackEntity.displayName(): String =
    name.ifBlank { title.ifBlank { id } }

private fun SettingPackEntity.bodyText(): String =
    content.ifBlank { statement.ifBlank { description.ifBlank { summary } } }

private fun com.localnovel.engine.model.InitialRuntimeTemplate.allRuntimeText(): List<String> =
    buildList {
        add(initialLocationId)
        add(initialGoal)
        add(dominantEmotion)
        addAll(emotionState.keys)
        addAll(availableActions)
        addAll(relationshipDefaults.keys)
        addAll(relationshipDefaults.values)
    }.filter { it.isNotBlank() }

private fun sortJson(element: JsonElement): JsonElement =
    when (element) {
        is JsonObject -> JsonObject(element.toSortedMap().mapValues { sortJson(it.value) })
        is JsonArray -> JsonArray(element.map(::sortJson))
        is JsonPrimitive -> element
        JsonNull -> JsonNull
    }

private fun sha256(value: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
