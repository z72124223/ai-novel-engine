package com.localnovel.engine.service

import com.localnovel.engine.model.CharacterProfile
import com.localnovel.engine.model.CharacterState
import com.localnovel.engine.model.ContextBudget
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.Event
import com.localnovel.engine.model.RelevantSetting
import com.localnovel.engine.model.SettingContextBudget
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object ContextPacketTopLevelAllowlist {
    val keys: Set<String> = setOf(
        "protocolVersion",
        "project",
        "turnId",
        "turnNumber",
        "playerInput",
        "activeScene",
        "sceneContract",
        "characters",
        "characterMemories",
        "characterStates",
        "worldRules",
        "facts",
        "revealedSecrets",
        "items",
        "techniques",
        "recentEvents",
        "openTasks",
        "contextId",
        "sessionId",
        "baseStateVersion",
        "contextHash",
        "generatedAt",
        "recentTurnSummaries",
        "sceneProgress",
        "directorPressure",
        "outputRequirements",
        "retrievedMemories",
        "knowledgeClaims",
        "contextBudget",
        "sceneLifecycle",
        "sceneRequirements",
        "activeStoryThreads",
        "availableTransitions",
        "pendingWorldEvents",
        "worldClock",
        "continuityConstraints",
        "relevantSettings",
        "activeCapabilityContracts",
        "settingContextBudget",
        "canonRevisionSummary",
    )

    val diagnosticKeys: Set<String> = setOf(
        "perSectionBytesBefore",
        "perSectionBytesAfter",
        "topLargestJsonPaths",
        "duplicateContentGroups",
        "removedItems",
        "retainedItems",
        "trimmingSimulation",
        "validationDebugTrace",
        "contextBudgetForensics",
        "otherMetadata",
    )

    fun unknownTopLevelKeys(rawJson: String): List<String> {
        val root = runCatching { JsonSupport.json.parseToJsonElement(rawJson).jsonObject }.getOrNull()
            ?: return listOf("invalid_json_root")
        return root.keys.filterNot { it in keys }.sorted()
    }
}

object ContextPacketNormalizer {
    private val retainedSettingMetadataKeys = setOf(
        "costs",
        "limitations",
        "requirements",
        "prerequisites",
        "forbiddenUses",
        "effects",
    )

    fun normalizeForFinalContext(context: ContextPacket): ContextPacket {
        val mandatoryTexts = mandatoryTextFragments(context)
        return context.copy(
            characters = context.characters.map(::compactCharacter).distinctBy { it.id },
            characterMemories = emptyList(),
            characterStates = context.characterStates.map(::compactState).distinctBy { it.characterId },
            facts = context.facts.distinctBy { it.id },
            revealedSecrets = emptyList(),
            items = context.items.distinctBy { it.id },
            techniques = context.techniques.map { technique ->
                if (context.activeCapabilityContracts.any { it.entityType == "technique" && it.entityId == technique.id }) {
                    technique.copy(description = "", tags = emptyList())
                } else {
                    technique
                }
            }.distinctBy { it.id },
            recentEvents = context.recentEvents.map(::compactEvent).distinctBy { it.id },
            openTasks = context.openTasks.distinctBy { it.id },
            recentTurnSummaries = context.recentTurnSummaries.distinctBy { it.turnNumber },
            sceneProgress = compactJsonObject(context.sceneProgress),
            directorPressure = context.directorPressure?.takeIf {
                it.enabled || it.mustProduceMeaningfulStateChange || it.allowedPressureTypes.isNotEmpty()
            },
            relevantSettings = context.relevantSettings.map { compactSetting(it, mandatoryTexts) }.distinctBy { "${it.entityType}:${it.entityId}" },
            activeCapabilityContracts = context.activeCapabilityContracts.distinctBy { "${it.entityType}:${it.entityId}:${it.authorizedActorIds.joinToString(",")}" },
            contextBudget = context.contextBudget.withoutLegacyDiagnostics(),
            settingContextBudget = context.settingContextBudget.withoutLegacyDiagnostics(),
            canonRevisionSummary = context.canonRevisionSummary.copy(
                relevantRevisionIds = context.canonRevisionSummary.relevantRevisionIds.take(8),
            ),
        )
    }

    fun reasonCodes(reasons: List<String>): List<String> =
        reasons.map { reason ->
            val lower = reason.lowercase()
            when {
                "canon" in lower -> "canon_provenance_compacted"
                "character profile" in lower || "runtime template" in lower -> "character_profile_compacted"
                "deduplicated" in lower || "setting" in lower -> "setting_context_compacted"
                "memory" in lower -> "memory_omitted"
                "knowledge" in lower -> "knowledge_omitted"
                "summary" in lower -> "turn_summary_omitted"
                "thread" in lower -> "story_thread_omitted"
                "world event" in lower -> "world_event_omitted"
                "transition" in lower -> "transition_preview_omitted"
                "requirement" in lower -> "scene_requirement_preview_omitted"
                "distant character" in lower -> "distant_character_omitted"
                "item" in lower || "technique" in lower -> "item_technique_compacted"
                "fact" in lower -> "fact_omitted"
                "world rule" in lower -> "world_rule_compacted"
                "state flags" in lower -> "character_state_flags_compacted"
                "public descriptions" in lower -> "character_profile_compacted"
                "scene" in lower || "project" in lower -> "scene_project_summary_compacted"
                "budget exceeded" in lower -> "budget_exceeded_safety_retained"
                else -> lower.replace(Regex("[^a-z0-9]+"), "_").trim('_').ifBlank { "context_compacted" }
            }
        }.distinct().sorted()

    private fun compactCharacter(character: CharacterProfile): CharacterProfile {
        val profile = JsonObject(
            character.profile
                .filterKeys { it !in setOf("initialRuntimeTemplate", "history", "relationships", "fullProfile", "diagnostics") }
                .filterValues { it.hasMeaningfulContent() },
        )
        return character.copy(
            description = trimText(character.description, 220),
            profile = profile,
        )
    }

    private fun compactState(state: CharacterState): CharacterState =
        state.copy(
            inventory = state.inventory.distinct().sorted(),
            flags = state.flags
                .toSortedMap()
                .mapNotNull { (key, value) ->
                    compactFlag(key, value)?.let { key to it }
                }
                .toMap(),
        )

    private fun compactFlag(key: String, value: String): String? {
        if (value.isBlank()) return null
        val lower = key.lowercase()
        if (
            ".last_" in lower ||
            lower.endsWith(".source") ||
            lower.endsWith(".source_id") ||
            lower.endsWith(".reason") ||
            lower.endsWith(".notes") ||
            lower.contains("debug") ||
            lower.contains("diagnostic")
        ) {
            return null
        }
        val keep = lower in setOf(
            "current_goal",
            "dominant_emotion",
            "available_actions",
            "forbidden_actions",
            "relationship_summary",
            "current_turn",
            "idle_turn_count",
            "initiative_level",
        ) ||
            lower.startsWith("emotion.") && value.trim().matches(Regex("-?\\d{1,3}")) ||
            listOf("belief", "knowledge", "goal", "emotion", "relationship", "constraint", "intent", "trust", "fear", "suspicion").any { it in lower }
        return if (keep) trimText(value, 180) else null
    }

    private fun compactEvent(event: Event): Event {
        val payload = JsonObject(
            event.payload
                .filterKeys { it !in setOf("summary", "turnSummary", "debug", "diagnostics") }
                .filterValues { element ->
                    val text = element.allStringValues().joinToString(" ").trim()
                    element.hasMeaningfulContent() && text != event.summary.trim()
                },
        )
        return event.copy(summary = trimText(event.summary, 180), payload = payload)
    }

    private fun mandatoryTextFragments(context: ContextPacket): Set<String> =
        buildSet {
            add(context.project.summary.normalizedFragment())
            context.activeScene?.let {
                add(it.title.normalizedFragment())
                add(it.summary.normalizedFragment())
            }
            context.characters.forEach {
                add(it.name.normalizedFragment())
                add(it.description.normalizedFragment())
            }
            context.worldRules.forEach {
                add(it.title.normalizedFragment())
                add(it.content.normalizedFragment())
            }
            context.facts.forEach { add(it.content.normalizedFragment()) }
            context.items.forEach {
                add(it.name.normalizedFragment())
                add(it.description.normalizedFragment())
            }
            context.techniques.forEach {
                add(it.name.normalizedFragment())
                add(it.description.normalizedFragment())
            }
        }.filter { it.length >= 20 }.toSet()

    private fun compactSetting(setting: RelevantSetting, mandatoryTexts: Set<String>): RelevantSetting {
        val duplicateSummary = setting.summary.normalizedFragment() in mandatoryTexts
        return setting.copy(
            summary = if (duplicateSummary) "" else trimText(setting.summary, if (setting.requiredRuntimeUse) 220 else 100),
            relationPath = if (setting.requiredRuntimeUse) setting.relationPath.take(1) else emptyList(),
            metadata = JsonObject(setting.metadata.filterKeys { it in retainedSettingMetadataKeys }.filterValues { it.hasMeaningfulContent() }),
        )
    }

    private fun compactJsonObject(value: JsonObject): JsonObject =
        JsonObject(value.filterValues { it.hasMeaningfulContent() })

    private fun ContextBudget.withoutLegacyDiagnostics(): ContextBudget =
        copy(
            truncationReasonCodes = (truncationReasonCodes + reasonCodes(truncationReasons)).distinct().sorted(),
            truncationReasons = emptyList(),
        )

    private fun SettingContextBudget.withoutLegacyDiagnostics(): SettingContextBudget =
        copy(
            reasonCodes = (reasonCodes + ContextPacketNormalizer.reasonCodes(reasons)).distinct().sorted(),
            reasons = emptyList(),
        )

    private fun JsonElement.hasMeaningfulContent(): Boolean =
        when (this) {
            is JsonObject -> isNotEmpty() && values.any { it.hasMeaningfulContent() }
            is JsonArray -> isNotEmpty() && any { it.hasMeaningfulContent() }
            is JsonPrimitive -> contentOrNull?.isNotBlank() == true
        }

    private fun JsonElement.allStringValues(): List<String> =
        when (this) {
            is JsonObject -> values.flatMap { it.allStringValues() }
            is JsonArray -> flatMap { it.allStringValues() }
            is JsonPrimitive -> listOfNotNull(contentOrNull)
        }

    private fun trimText(text: String, maxChars: Int): String =
        when {
            text.length <= maxChars -> text
            maxChars <= 0 -> ""
            maxChars <= 3 -> text.take(maxChars)
            else -> text.take(maxChars - 3) + "..."
        }

    private fun String.normalizedFragment(): String =
        trim().replace(Regex("\\s+"), " ")

    fun serializedTopLevelKeys(context: ContextPacket): Set<String> =
        JsonSupport.json.parseToJsonElement(JsonSupport.json.encodeToString(context)).jsonObject.keys
}
