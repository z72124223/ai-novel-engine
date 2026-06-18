package com.localnovel.engine.service

import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.RelevantSetting
import com.localnovel.engine.model.SemanticCoverageItem
import com.localnovel.engine.model.SemanticCoverageReport
import com.localnovel.engine.model.SemanticRequirement
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.encodeToString

data class ProtectedContextIds(
    val memoryIds: Set<String> = emptySet(),
    val knowledgeClaimIds: Set<String> = emptySet(),
    val settingKeys: Set<String> = emptySet(),
    val capabilityKeys: Set<String> = emptySet(),
)

class TurnSemanticPlanner {
    fun plan(context: ContextPacket): List<SemanticRequirement> {
        val requirements = mutableListOf<SemanticRequirement>()
        val input = context.playerInput
        val inputTerms = semanticTerms(input)
        val temporalCue = hasTemporalCue(input)
        val questionCue = hasQuestionCue(input)
        val transitionCue = hasTransitionCue(input)

        if (temporalCue) {
            val candidates = context.retrievedMemories
                .map { it.memoryId to memoryCueScore(input, inputTerms, it) }
                .filter { it.second > 0 }
                .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })
                .take(4)
                .map { it.first }
            requirements += SemanticRequirement(
                requirementId = "req-explicit-memory-cue",
                semanticType = "EXPLICIT_MEMORY_CUE",
                source = "playerInput",
                targetCharacterIds = context.characters.filter { character ->
                    input.contains(character.name, ignoreCase = true) ||
                        context.retrievedMemories.any { it.ownerCharacterId == character.id && it.memoryId in candidates }
                }.map { it.id }.sorted(),
                referencedMemoryCandidates = candidates,
                priority = 100,
                reason = "Player input contains an explicit temporal/recall cue.",
                mustBeSatisfied = candidates.isNotEmpty(),
                acceptableContextPaths = listOf("$.retrievedMemories[].memoryId"),
            )
        }

        val beliefCandidates = context.knowledgeClaims
            .map { claim ->
                claim.claimId to (
                    overlapScore(inputTerms, claim.statement) * 10 +
                        overlapScore(inputTerms, claim.propositionKey) * 5 +
                        if (questionCue || transitionCue) claim.confidence / 20 else 0
                    )
            }
            .filter { it.second > 0 }
            .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })
            .take(4)
            .map { it.first }
        if (beliefCandidates.isNotEmpty()) {
            requirements += SemanticRequirement(
                requirementId = "req-relevant-belief",
                semanticType = "RELEVANT_BELIEF",
                source = "knowledgeClaims",
                referencedKnowledgeCandidates = beliefCandidates,
                priority = 90,
                reason = "A character-facing belief overlaps the current question or observation.",
                mustBeSatisfied = true,
                acceptableContextPaths = listOf("$.knowledgeClaims[].claimId"),
            )
        }

        val referencedItemSettings = context.items
            .filter { item -> input.contains(item.name, ignoreCase = true) || item.tags.any { input.contains(it, ignoreCase = true) } }
            .mapNotNull { item -> context.relevantSettings.firstOrNull { it.entityType == "item" && it.entityId == item.id } }
        referencedItemSettings.forEach { setting ->
            requirements += settingRequirement("REFERENCED_ITEM", "req-item-${setting.entityId}", setting)
        }

        val referencedTechniqueSettings = context.techniques
            .filter { technique -> input.contains(technique.name, ignoreCase = true) || technique.tags.any { input.contains(it, ignoreCase = true) } }
            .mapNotNull { technique -> context.relevantSettings.firstOrNull { it.entityType == "technique" && it.entityId == technique.id } }
        referencedTechniqueSettings.forEach { setting ->
            requirements += settingRequirement("REFERENCED_TECHNIQUE", "req-technique-${setting.entityId}", setting)
        }

        detectReferences(context).forEach { reference ->
            requirements += SemanticRequirement(
                requirementId = "req-capability-${reference.entityType}-${reference.entityId}",
                semanticType = when (reference.entityType) {
                    "technique" -> "REFERENCED_TECHNIQUE"
                    "weapon" -> "REFERENCED_WEAPON"
                    else -> "REFERENCED_ITEM"
                },
                source = "playerInput",
                targetCharacterIds = listOf(reference.actorId),
                referencedEntityIds = listOf("${reference.entityType}:${reference.entityId}"),
                priority = 98,
                reason = "Player explicitly uses or depends on a capability; a compact capability contract is required.",
                mustBeSatisfied = true,
                acceptableContextPaths = listOf("$.activeCapabilityContracts[]"),
                actorId = reference.actorId,
                entityType = reference.entityType,
                entityId = reference.entityId,
                referenceReason = "explicit player input capability use",
                requestedUse = reference.requestedUse,
                requiredContractFields = capabilityRequiredFields,
            )
        }

        if (transitionCue) {
            requirements += SemanticRequirement(
                requirementId = "req-transition-location-constraints",
                semanticType = "TRANSITION_CONSTRAINT",
                source = "playerInput",
                referencedEntityIds = listOfNotNull(context.activeScene?.id, context.activeScene?.locationId),
                priority = 95,
                reason = "Player asks whether withdrawal or movement is possible.",
                mustBeSatisfied = true,
                acceptableContextPaths = listOf("$.continuityConstraints", "$.activeScene.locationId", "$.sceneLifecycle", "$.availableTransitions"),
            )
            requirements += SemanticRequirement(
                requirementId = "req-location-continuity",
                semanticType = "LOCATION_CONSTRAINT",
                source = "continuityConstraints",
                referencedEntityIds = listOfNotNull(context.continuityConstraints.currentSceneLocationId),
                priority = 94,
                reason = "Movement questions need current scene and character location constraints.",
                mustBeSatisfied = true,
                acceptableContextPaths = listOf("$.continuityConstraints.currentSceneLocationId", "$.continuityConstraints.characterLocations"),
            )
        }

        if (context.characterStates.any { it.flags["current_goal"].orEmpty().isNotBlank() }) {
            requirements += SemanticRequirement(
                requirementId = "req-active-character-goals",
                semanticType = "CHARACTER_GOAL",
                source = "characterStates",
                targetCharacterIds = context.characterStates.map { it.characterId }.sorted(),
                priority = 100,
                reason = "Active character current goals are required for autonomous action.",
                mustBeSatisfied = true,
                acceptableContextPaths = listOf("$.characterStates[].flags.current_goal"),
            )
        }

        if (context.worldRules.isNotEmpty()) {
            requirements += SemanticRequirement(
                requirementId = "req-hard-world-rules",
                semanticType = "WORLD_RULE",
                source = "worldRules",
                referencedEntityIds = context.worldRules.map { it.id }.sorted(),
                priority = 100,
                reason = "Hard world rules remain part of Tier 0 safety.",
                mustBeSatisfied = true,
                acceptableContextPaths = listOf("$.worldRules"),
            )
        }

        if (temporalCue && context.recentEvents.isNotEmpty()) {
            requirements += SemanticRequirement(
                requirementId = "req-recent-event-continuity",
                semanticType = "RECENT_EVENT_CONTINUITY",
                source = "recentEvents",
                referencedEntityIds = context.recentEvents.map { it.id }.take(3),
                priority = 60,
                reason = "Recent event continuity can support but cannot replace recalled memory.",
                mustBeSatisfied = false,
                acceptableContextPaths = listOf("$.recentEvents", "$.recentTurnSummaries"),
            )
        }

        return requirements
            .distinctBy { it.requirementId }
            .sortedWith(compareByDescending<SemanticRequirement> { it.priority }.thenBy { it.requirementId })
    }

    private fun settingRequirement(type: String, id: String, setting: RelevantSetting): SemanticRequirement =
        SemanticRequirement(
            requirementId = id,
            semanticType = type,
            source = "playerInput",
            referencedEntityIds = listOf("${setting.entityType}:${setting.entityId}"),
            referencedSettingCandidates = listOf("${setting.entityType}:${setting.entityId}"),
            priority = 92,
            reason = "Player references a current ${setting.entityType}; legality constraints must remain available when present.",
            mustBeSatisfied = true,
            acceptableContextPaths = listOf("$.relevantSettings[]", "$.items[]", "$.techniques[]"),
        )

    private fun hasTemporalCue(text: String): Boolean =
        listOf("剛才", "剛剛", "先前", "之前", "記得", "上次", "那件事", "方才", "earlier", "previous").any {
            text.contains(it, ignoreCase = true)
        }

    private fun hasQuestionCue(text: String): Boolean =
        listOf("能不能", "是否", "有沒有", "是不是", "為什麼", "怎麼", "可不可以", "?", "？", "can ", "whether").any {
            text.contains(it, ignoreCase = true)
        }

    private fun hasTransitionCue(text: String): Boolean =
        listOf("退", "撤", "離開", "門外", "出去", "走", "轉場", "移動", "靠近", "退到", "withdraw", "leave", "move").any {
            text.contains(it, ignoreCase = true)
        }

    private fun memoryCueScore(input: String, inputTerms: Set<String>, memory: com.localnovel.engine.model.RetrievedMemory): Int {
        val summary = memory.summary
        val overlap = overlapScore(inputTerms, summary) * 10
        val cueBonus = recallCueTerms.count { input.contains(it, ignoreCase = true) && summary.contains(it, ignoreCase = true) } * 12
        val temporalBonus = if (hasTemporalCue(input) && temporalEvidenceTerms.any { summary.contains(it, ignoreCase = true) }) 8 else 0
        return overlap + cueBonus + temporalBonus + memory.retrievalScore / 100
    }

    companion object {
        private val recallCueTerms = listOf("頸側", "忍住", "低吼", "守", "護主", "安靜", "不說話", "十息", "沒有")
        private val temporalEvidenceTerms = listOf("剛才", "剛剛", "十息", "先前", "記得", "上一回", "上回")
    }
}

class ContextQualityGuard(
    private val maxBytes: Int = 24576,
    private val softTargetBytes: Int = 22528,
) {
    fun protectedIds(requirements: List<SemanticRequirement>): ProtectedContextIds =
        ProtectedContextIds(
            memoryIds = requirements.filter { it.mustBeSatisfied }.flatMap { it.referencedMemoryCandidates }.toSet(),
            knowledgeClaimIds = requirements.filter { it.mustBeSatisfied }.flatMap { it.referencedKnowledgeCandidates }.toSet(),
            settingKeys = requirements.filter { it.mustBeSatisfied }.flatMap { it.referencedSettingCandidates }.toSet(),
            capabilityKeys = requirements.filter { it.mustBeSatisfied && it.entityId.isNotBlank() }.map { "${it.entityType}:${it.entityId}" }.toSet(),
        )

    fun evaluate(context: ContextPacket, requirements: List<SemanticRequirement>): SemanticCoverageReport {
        val items = requirements.map { requirement -> evaluateRequirement(context, requirement) }
        val missing = requirements
            .filter { it.mustBeSatisfied }
            .filter { req -> items.none { it.requirementId == req.requirementId && it.qualityStatus == "PASS" } }
        val actualBytes = JsonSupport.json.encodeToString(context).toByteArray(Charsets.UTF_8).size
        return SemanticCoverageReport(
            requirements = requirements,
            satisfiedCount = items.count { it.qualityStatus == "PASS" },
            unsatisfiedCount = missing.size,
            satisfiedBy = items,
            missingCriticalRequirements = missing,
            qualityStatus = if (missing.isEmpty()) "PASS" else "FAIL",
            nearHardLimit = actualBytes > softTargetBytes,
            remainingBytes = maxBytes - actualBytes,
            softTargetBytes = softTargetBytes,
            maxBytes = maxBytes,
            tierBytes = tierBytes(context, requirements),
        )
    }

    private fun evaluateRequirement(context: ContextPacket, requirement: SemanticRequirement): SemanticCoverageItem {
        val selected = when (requirement.semanticType) {
            "EXPLICIT_MEMORY_CUE" -> context.retrievedMemories
                .firstOrNull { it.memoryId in requirement.referencedMemoryCandidates }
                ?.let { it.memoryId to "$.retrievedMemories[${context.retrievedMemories.indexOf(it)}]" }
            "RELEVANT_BELIEF" -> context.knowledgeClaims
                .firstOrNull { it.claimId in requirement.referencedKnowledgeCandidates }
                ?.let { it.claimId to "$.knowledgeClaims[${context.knowledgeClaims.indexOf(it)}]" }
            "REFERENCED_ITEM", "REFERENCED_TECHNIQUE", "REFERENCED_WEAPON" -> {
                if (requirement.requiredContractFields.isNotEmpty() || requirement.entityId.isNotBlank()) {
                    context.activeCapabilityContracts
                        .firstOrNull { contract ->
                            contract.entityType == requirement.entityType &&
                                contract.entityId == requirement.entityId &&
                                capabilityContractComplete(contract, requirement)
                        }
                        ?.let { "${it.entityType}:${it.entityId}" to "$.activeCapabilityContracts[${context.activeCapabilityContracts.indexOf(it)}]" }
                } else {
                    context.relevantSettings
                        .firstOrNull {
                            "${it.entityType}:${it.entityId}" in requirement.referencedSettingCandidates &&
                                it.visibility != "author_only" &&
                                hasLegalityMetadata(it)
                        }
                        ?.let { "${it.entityType}:${it.entityId}" to "$.relevantSettings[${context.relevantSettings.indexOf(it)}]" }
                }
            }
            "TRANSITION_CONSTRAINT" -> if (
                context.continuityConstraints.currentSceneInstanceId.isNotBlank() &&
                    context.activeScene?.locationId != null &&
                    context.sceneLifecycle.sceneInstanceId.isNotBlank()
            ) {
                (context.activeScene?.id.orEmpty() to "$.continuityConstraints")
            } else {
                null
            }
            "LOCATION_CONSTRAINT" -> if (
                context.continuityConstraints.currentSceneLocationId != null ||
                    context.continuityConstraints.characterLocations.isNotEmpty()
            ) {
                (context.continuityConstraints.currentSceneLocationId.orEmpty() to "$.continuityConstraints")
            } else {
                null
            }
            "CHARACTER_GOAL" -> context.characterStates
                .firstOrNull { it.flags["current_goal"].orEmpty().isNotBlank() }
                ?.let { it.characterId to "$.characterStates[${context.characterStates.indexOf(it)}].flags.current_goal" }
            "WORLD_RULE" -> context.worldRules.firstOrNull()?.let { it.id to "$.worldRules[0]" }
            "RECENT_EVENT_CONTINUITY" -> context.recentEvents.firstOrNull()?.let { it.id to "$.recentEvents[0]" }
            else -> null
        }
        val candidateIds = requirement.referencedMemoryCandidates +
            requirement.referencedKnowledgeCandidates +
            requirement.referencedSettingCandidates +
            requirement.referencedEntityIds
        val selectedBytes = selected?.second?.let { pathBytes(context, it.substringBefore("[").removeSuffix(".")) } ?: 0
        val passed = selected != null || !requirement.mustBeSatisfied
        return SemanticCoverageItem(
            requirementId = requirement.requirementId,
            semanticType = requirement.semanticType,
            reason = requirement.reason,
            candidateIds = candidateIds.distinct(),
            selectedId = selected?.first.orEmpty(),
            selectedContextPath = selected?.second.orEmpty(),
            bytes = selectedBytes,
            omittedAlternatives = candidateIds.filterNot { it == selected?.first }.distinct(),
            visibilityCheck = if (passed) "PASS" else "MISSING",
            qualityStatus = if (passed) "PASS" else "FAIL",
        )
    }

    private fun hasLegalityMetadata(setting: RelevantSetting): Boolean =
        setting.metadata.keys.any { it in setOf("costs", "limitations", "requirements", "prerequisites", "forbiddenUses") } ||
            setting.summary.isNotBlank()

    private fun tierBytes(context: ContextPacket, requirements: List<SemanticRequirement>): Map<String, Int> {
        val tier1 = requirements.filter { it.mustBeSatisfied }.sumOf { requirement ->
            when (requirement.semanticType) {
                "EXPLICIT_MEMORY_CUE" -> context.retrievedMemories.filter { it.memoryId in requirement.referencedMemoryCandidates }.sumOf { bytes(it) }
                "RELEVANT_BELIEF" -> context.knowledgeClaims.filter { it.claimId in requirement.referencedKnowledgeCandidates }.sumOf { bytes(it) }
            "REFERENCED_ITEM", "REFERENCED_TECHNIQUE", "REFERENCED_WEAPON" -> {
                if (requirement.entityId.isNotBlank()) {
                    context.activeCapabilityContracts.filter { "${it.entityType}:${it.entityId}" == "${requirement.entityType}:${requirement.entityId}" }.sumOf { bytes(it) }
                } else {
                    context.relevantSettings.filter { "${it.entityType}:${it.entityId}" in requirement.referencedSettingCandidates }.sumOf { bytes(it) }
                }
            }
                else -> 0
            }
        }
        val tier0 = bytes(context.protocolVersion) +
            bytes(context.turnId) +
            bytes(context.contextId) +
            bytes(context.sessionId) +
            bytes(context.turnNumber) +
            bytes(context.baseStateVersion) +
            bytes(context.sceneContract) +
            bytes(context.worldRules) +
            bytes(context.characterStates) +
            bytes(context.continuityConstraints)
        val tier2 = bytes(context.facts) +
            bytes(context.recentEvents) +
            bytes(context.activeStoryThreads) +
            bytes(context.pendingWorldEvents)
        val total = JsonSupport.json.encodeToString(context).toByteArray(Charsets.UTF_8).size
        return linkedMapOf(
            "tier0" to tier0,
            "tier1" to tier1,
            "tier2" to tier2,
            "tier3" to (total - tier0 - tier1 - tier2).coerceAtLeast(0),
        )
    }

    private fun pathBytes(context: ContextPacket, path: String): Int =
        when (path) {
            "$.retrievedMemories" -> bytes(context.retrievedMemories)
            "$.knowledgeClaims" -> bytes(context.knowledgeClaims)
            "$.relevantSettings" -> bytes(context.relevantSettings)
            "$.activeCapabilityContracts" -> bytes(context.activeCapabilityContracts)
            "$.continuityConstraints" -> bytes(context.continuityConstraints)
            else -> 0
        }

    private fun capabilityContractComplete(
        contract: com.localnovel.engine.model.CapabilityContract,
        requirement: SemanticRequirement,
    ): Boolean {
        if (requirement.actorId.isNotBlank() && requirement.actorId !in contract.authorizedActorIds) return false
        if (contract.currentEligibility.isBlank()) return false
        return requirement.requiredContractFields.all { field ->
            when (field) {
                "id" -> contract.entityId.isNotBlank()
                "name" -> contract.name.isNotBlank()
                "authorizedActorIds" -> contract.authorizedActorIds.isNotEmpty()
                "effects" -> contract.effects.isNotEmpty()
                "prerequisites" -> contract.prerequisites.isNotEmpty()
                "requirements" -> contract.requirements.isNotEmpty()
                "costs" -> contract.costs.isNotEmpty()
                "limitations" -> contract.limitations.isNotEmpty()
                "forbiddenUses" -> contract.forbiddenUses.isNotEmpty()
                "currentEligibility" -> contract.currentEligibility.isNotBlank()
                else -> true
            }
        }
    }

    private inline fun <reified T> bytes(value: T): Int =
        JsonSupport.json.encodeToString(value).toByteArray(Charsets.UTF_8).size
}

private fun semanticTerms(text: String): Set<String> {
    val normalized = text.lowercase()
    val tokenTerms = Regex("[\\p{L}\\p{N}_]+")
        .findAll(normalized)
        .map { it.value }
        .filter { it.length >= 2 }
        .toSet()
    val cjkBigrams = normalized
        .filter { Character.UnicodeScript.of(it.code) in setOf(Character.UnicodeScript.HAN, Character.UnicodeScript.HIRAGANA, Character.UnicodeScript.KATAKANA) }
        .windowed(2, 1)
        .toSet()
    return tokenTerms + cjkBigrams
}

private fun overlapScore(terms: Set<String>, text: String): Int {
    if (terms.isEmpty() || text.isBlank()) return 0
    val lower = text.lowercase()
    return terms.count { lower.contains(it) }
}
