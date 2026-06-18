package com.localnovel.engine.service

import com.localnovel.engine.model.ContextBudgetForensicsReport
import com.localnovel.engine.model.ContextBudgetItemReport
import com.localnovel.engine.model.ContextDanglingReferenceCheck
import com.localnovel.engine.model.ContextDuplicateContentGroup
import com.localnovel.engine.model.ContextJsonPathByteReport
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.ContextSafetyFieldPresence
import com.localnovel.engine.model.ContextSectionByteReport
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.security.MessageDigest
import kotlin.math.max

class ContextBudgetForensicsService(
    private val maxContextBytes: Int = 24576,
) {
    fun report(
        before: ContextPacket,
        after: ContextPacket,
        unsatisfiedReason: String = "",
    ): ContextBudgetForensicsReport {
        val beforeBytes = bytes(before.copy(contextHash = ""))
        val afterBytes = bytes(after)
        val mandatorySections = mandatorySectionNames
        val afterSections = sectionReports(after)
        val mandatoryBytes = afterSections.filter { it.section in mandatorySections }.sumOf { it.utf8Bytes }
        val duplicateGroups = duplicateGroups(after)
        val duplicatePaths = duplicateGroups.flatMap { it.paths }.toSet()
        val recomputedHash = sha256(JsonSupport.json.encodeToString(after.copy(contextHash = "")).toByteArray(Charsets.UTF_8))
        return ContextBudgetForensicsReport(
            sessionId = after.sessionId,
            turnNumber = after.turnNumber,
            baseStateVersion = after.baseStateVersion,
            maxContextBytes = maxContextBytes,
            candidateBytesBeforeTrimming = beforeBytes,
            candidateBytesAfterTrimming = afterBytes,
            bytesOverLimit = max(0, afterBytes - maxContextBytes),
            mandatoryBytes = mandatoryBytes,
            optionalBytesRemaining = max(0, afterBytes - mandatoryBytes),
            perSectionBytesBefore = sectionReports(before),
            perSectionBytesAfter = afterSections,
            mandatorySections = mandatorySections,
            optionalSections = optionalSectionNames,
            removedItems = removedItems(before, after),
            retainedItems = retainedItems(after),
            topLargestJsonPaths = largestPaths(after, duplicatePaths),
            duplicateContentGroups = duplicateGroups,
            danglingReferenceCheck = danglingReferenceCheck(after),
            safetyFieldPresence = safetyFieldPresence(after),
            unsatisfiedReason = unsatisfiedReason,
            canFit = afterBytes <= maxContextBytes && unsatisfiedReason.isBlank(),
            contextHash = after.contextHash,
            actualBytesMatchesSerializedPayload = after.contextBudget.actualBytes == afterBytes,
            hashValid = after.contextHash == recomputedHash,
        )
    }

    fun sectionReports(context: ContextPacket): List<ContextSectionByteReport> =
        listOf(
            section("protocolIdentity", mapOf(
                "protocolVersion" to context.protocolVersion,
                "turnId" to context.turnId,
                "contextId" to context.contextId,
                "sessionId" to context.sessionId,
                "turnNumber" to context.turnNumber.toString(),
                "baseStateVersion" to context.baseStateVersion.toString(),
                "contextHash" to context.contextHash,
            ), 1),
            section("playerInput", context.playerInput, 1),
            section("sceneLifecycle", context.sceneLifecycle, 1),
            section("sceneContract", context.sceneContract, if (context.sceneContract == null) 0 else 1),
            section("activeCharacters", context.characters, context.characters.size),
            section("characterKnowledge", context.knowledgeClaims, context.knowledgeClaims.size),
            section("retrievedMemories", context.retrievedMemories, context.retrievedMemories.size),
            section("relevantSettings", context.relevantSettings, context.relevantSettings.size),
            section("storyThreads", context.activeStoryThreads, context.activeStoryThreads.size),
            section("recentTurnSummaries", context.recentTurnSummaries, context.recentTurnSummaries.size),
            section("worldRules", context.worldRules, context.worldRules.size),
            section("worldEvents", context.pendingWorldEvents, context.pendingWorldEvents.size),
            section("continuityConstraints", context.continuityConstraints, 1),
            section("forbiddenReveals", context.sceneContract?.forbiddenReveals.orEmpty(), context.sceneContract?.forbiddenReveals?.size ?: 0),
            section("outputRequirements", context.outputRequirements, 1),
            section("contextBudgetMetadata", JsonObject(mapOf(
                "contextBudget" to jsonElement(context.contextBudget),
                "settingContextBudget" to jsonElement(context.settingContextBudget),
            )), 2),
            section("canonRevisionSummary", context.canonRevisionSummary, context.canonRevisionSummary.relevantRevisionIds.size),
            section("otherMetadata", JsonObject(mapOf(
                "project" to jsonElement(context.project),
                "activeScene" to jsonElement(context.activeScene),
                "characterStates" to jsonElement(context.characterStates),
                "facts" to jsonElement(context.facts),
                "revealedSecrets" to jsonElement(context.revealedSecrets),
                "items" to jsonElement(context.items),
                "techniques" to jsonElement(context.techniques),
                "recentEvents" to jsonElement(context.recentEvents),
                "openTasks" to jsonElement(context.openTasks),
                "sceneProgress" to context.sceneProgress,
                "directorPressure" to jsonElement(context.directorPressure),
                "sceneRequirements" to jsonElement(context.sceneRequirements),
                "availableTransitions" to jsonElement(context.availableTransitions),
                "worldClock" to jsonElement(context.worldClock),
                "generatedAt" to JsonPrimitive(context.generatedAt),
            )), 1),
        )

    private fun removedItems(before: ContextPacket, after: ContextPacket): List<ContextBudgetItemReport> {
        val beforeIds = itemMap(before)
        val afterIds = itemMap(after)
        return beforeIds.filterKeys { it !in afterIds }
            .toSortedMap()
            .map { (id, section) -> ContextBudgetItemReport(id = id, section = section, reason = "trimmed_or_compacted") }
    }

    private fun retainedItems(after: ContextPacket): List<ContextBudgetItemReport> =
        itemMap(after).toSortedMap().map { (id, section) -> ContextBudgetItemReport(id = id, section = section, reason = "retained") }

    private fun itemMap(context: ContextPacket): Map<String, String> = buildMap {
        context.characters.forEach { put("character:${it.id}", "activeCharacters") }
        context.characterStates.forEach { put("character_state:${it.characterId}", "otherMetadata") }
        context.retrievedMemories.forEach { put("memory:${it.memoryId}", "retrievedMemories") }
        context.knowledgeClaims.forEach { put("knowledge:${it.claimId}", "characterKnowledge") }
        context.relevantSettings.forEach { put("setting:${it.entityType}:${it.entityId}", "relevantSettings") }
        context.activeStoryThreads.forEach { put("thread:${it.threadId}", "storyThreads") }
        context.recentTurnSummaries.forEach { put("summary:${it.turnNumber}", "recentTurnSummaries") }
        context.worldRules.forEach { put("world_rule:${it.id}", "worldRules") }
        context.pendingWorldEvents.forEach { put("world_event:${it.eventId}", "worldEvents") }
        context.facts.forEach { put("fact:${it.id}", "otherMetadata") }
        context.items.forEach { put("item:${it.id}", "otherMetadata") }
        context.techniques.forEach { put("technique:${it.id}", "otherMetadata") }
    }

    private fun largestPaths(context: ContextPacket, duplicatePaths: Set<String>): List<ContextJsonPathByteReport> {
        val root = JsonSupport.json.parseToJsonElement(JsonSupport.json.encodeToString(context))
        val reports = mutableListOf<ContextJsonPathByteReport>()
        collectPaths(root, "$", reports, duplicatePaths)
        return reports
            .asSequence()
            .filter { it.path != "$" }
            .sortedWith(compareByDescending<ContextJsonPathByteReport> { it.utf8Bytes }.thenBy { it.path })
            .take(25)
            .toList()
    }

    private fun collectPaths(
        element: JsonElement,
        path: String,
        out: MutableList<ContextJsonPathByteReport>,
        duplicatePaths: Set<String>,
    ) {
        out += ContextJsonPathByteReport(
            path = path,
            utf8Bytes = JsonSupport.json.encodeToString(element).toByteArray(Charsets.UTF_8).size,
            mandatory = isMandatoryPath(path),
            deduplicatable = path in duplicatePaths,
        )
        when (element) {
            is JsonObject -> element.toSortedMap().forEach { (key, value) -> collectPaths(value, "$path.$key", out, duplicatePaths) }
            is JsonArray -> element.forEachIndexed { index, value -> collectPaths(value, "$path[$index]", out, duplicatePaths) }
            is JsonPrimitive -> Unit
        }
    }

    private fun duplicateGroups(context: ContextPacket): List<ContextDuplicateContentGroup> {
        val root = JsonSupport.json.parseToJsonElement(JsonSupport.json.encodeToString(context))
        val fragments = mutableListOf<Pair<String, String>>()
        collectStringFragments(root, "$", fragments)
        return fragments
            .mapNotNull { (path, text) ->
                val normalized = text.trim().replace(Regex("\\s+"), " ")
                if (normalized.length < 20) null else Triple(sha256(normalized.toByteArray(Charsets.UTF_8)), path, normalized)
            }
            .groupBy { it.first }
            .filterValues { it.size > 1 }
            .toSortedMap()
            .map { (hash, group) ->
                ContextDuplicateContentGroup(
                    hash = hash,
                    normalizedBytes = group.first().third.toByteArray(Charsets.UTF_8).size,
                    paths = group.map { it.second }.sorted(),
                    sample = group.first().third.take(120),
                )
            }
            .take(25)
    }

    private fun collectStringFragments(element: JsonElement, path: String, out: MutableList<Pair<String, String>>) {
        when (element) {
            is JsonObject -> element.toSortedMap().forEach { (key, value) -> collectStringFragments(value, "$path.$key", out) }
            is JsonArray -> element.forEachIndexed { index, value -> collectStringFragments(value, "$path[$index]", out) }
            is JsonPrimitive -> element.contentOrNull?.let { out += path to it }
        }
    }

    private fun danglingReferenceCheck(context: ContextPacket): ContextDanglingReferenceCheck {
        val characterIds = context.characters.map { it.id }.toSet()
        val stateCharacterIds = context.characterStates.map { it.characterId }.toSet()
        val itemIds = context.items.map { it.id }.toSet()
        val techniqueIds = context.techniques.map { it.id }.toSet()
        val factIds = context.facts.map { it.id }.toSet()
        val memoryIds = context.retrievedMemories.map { it.memoryId }.toSet() + context.characterMemories.map { it.id }
        val missing = mutableListOf<String>()
        context.characterStates.forEach { state ->
            if (state.characterId !in characterIds) missing += "character_state:${state.characterId}->characters"
            state.inventory.filterNot { it in itemIds }.forEach { missing += "character_state:${state.characterId}.inventory:$it" }
        }
        context.items.forEach { item ->
            item.ownerCharacterId?.takeIf { it !in characterIds && it !in stateCharacterIds }?.let { missing += "item:${item.id}.owner:$it" }
        }
        context.techniques.forEach { technique ->
            technique.ownerCharacterId?.takeIf { it !in characterIds && it !in stateCharacterIds }?.let { missing += "technique:${technique.id}.owner:$it" }
        }
        context.knowledgeClaims.forEach { claim ->
            claim.sourceFactId?.takeIf { it !in factIds }?.let { missing += "knowledge:${claim.claimId}.fact:$it" }
            claim.sourceMemoryId?.takeIf { it !in memoryIds }?.let { missing += "knowledge:${claim.claimId}.memory:$it" }
        }
        return ContextDanglingReferenceCheck(missing.isEmpty(), missing.sorted())
    }

    private fun safetyFieldPresence(context: ContextPacket): ContextSafetyFieldPresence =
        ContextSafetyFieldPresence(
            protocolIdentity = context.protocolVersion.isNotBlank() && context.turnId.isNotBlank() && context.contextId.isNotBlank() && context.sessionId.isNotBlank(),
            sceneContract = context.sceneContract != null,
            forbiddenReveals = context.sceneContract?.forbiddenReveals != null,
            worldRules = context.worldRules.isNotEmpty(),
            activeCharacters = context.characters.isNotEmpty(),
            characterStates = context.characterStates.isNotEmpty(),
            continuityConstraints = context.continuityConstraints.currentSceneInstanceId.isNotBlank(),
            contextHash = context.contextHash.isNotBlank(),
        )

    private fun isMandatoryPath(path: String): Boolean =
        mandatoryPathPrefixes.any { path == it || path.startsWith("$it.") || path.startsWith("$it[") }

    private inline fun <reified T> section(name: String, value: T, count: Int): ContextSectionByteReport =
        ContextSectionByteReport(name, JsonSupport.json.encodeToString(value).toByteArray(Charsets.UTF_8).size, count)

    private inline fun <reified T> jsonElement(value: T): JsonElement =
        JsonSupport.json.parseToJsonElement(JsonSupport.json.encodeToString(value))

    private fun bytes(context: ContextPacket): Int =
        JsonSupport.json.encodeToString(context).toByteArray(Charsets.UTF_8).size

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    companion object {
        val mandatorySectionNames = listOf(
            "protocolIdentity",
            "playerInput",
            "sceneLifecycle",
            "sceneContract",
            "activeCharacters",
            "worldRules",
            "continuityConstraints",
            "forbiddenReveals",
            "outputRequirements",
            "contextBudgetMetadata",
        )

        val optionalSectionNames = listOf(
            "characterKnowledge",
            "retrievedMemories",
            "relevantSettings",
            "storyThreads",
            "recentTurnSummaries",
            "worldEvents",
            "canonRevisionSummary",
            "otherMetadata",
        )

        private val mandatoryPathPrefixes = setOf(
            "$.protocolVersion",
            "$.turnId",
            "$.turnNumber",
            "$.contextId",
            "$.sessionId",
            "$.baseStateVersion",
            "$.contextHash",
            "$.playerInput",
            "$.sceneLifecycle",
            "$.sceneContract",
            "$.characters",
            "$.worldRules",
            "$.continuityConstraints",
            "$.outputRequirements",
            "$.contextBudget",
        )
    }
}
