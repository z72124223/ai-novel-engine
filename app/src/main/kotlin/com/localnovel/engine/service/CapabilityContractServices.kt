package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CapabilityContract
import com.localnovel.engine.model.CapabilityCoverageReport
import com.localnovel.engine.model.CapabilityCoverageSelection
import com.localnovel.engine.model.CapabilityProvenanceReport
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.SemanticRequirement
import com.localnovel.engine.model.SettingPackEntity
import com.localnovel.engine.model.ValidationError
import com.localnovel.engine.model.allDeltaOperations
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

internal val capabilityRequiredFields = listOf(
    "id",
    "name",
    "authorizedActorIds",
    "effects",
    "prerequisites",
    "requirements",
    "costs",
    "limitations",
    "forbiddenUses",
    "currentEligibility",
)

class CapabilityContractResolver(private val repository: SqliteStoryRepository) {
    fun enrich(context: ContextPacket): ContextPacket {
        val refs = detectReferences(context)
        if (refs.isEmpty()) return context
        val contracts = refs.mapNotNull { reference ->
            val report = explain(
                projectId = context.project.id,
                sessionId = context.sessionId,
                entityType = reference.entityType,
                entityId = reference.entityId,
                actorId = reference.actorId,
                intendedUse = reference.requestedUse,
            )
            if (report.status == "OK") report.toContract(reference.requestedUse) else null
        }.distinctBy { "${it.entityType}:${it.entityId}:${it.authorizedActorIds.joinToString(",")}" }
        return context.copy(activeCapabilityContracts = contracts)
    }

    fun explain(
        projectId: String,
        sessionId: String,
        entityType: String,
        entityId: String,
        actorId: String? = null,
        intendedUse: String = "",
    ): CapabilityProvenanceReport {
        val normalizedType = entityType.normalizeCapabilityType()
        val row = tableRow(projectId, normalizedType, entityId)
        val revision = repository.latestEntityRevision(projectId, normalizedType, entityId)
        val entity = revision?.get("snapshot_json")?.let { runCatching { JsonSupport.json.decodeFromString<SettingPackEntity>(it) }.getOrNull() }
        val owner = row.string("owner_character_id") ?: entity?.ownerCharacterId ?: entity?.initialOwnerId
        val authorized = buildList {
            owner?.takeIf { it.isNotBlank() }?.let { add(it) }
            entity?.knownBy.orEmpty().forEach { add(it) }
        }.distinct().sorted()
        val snapshot = revision?.get("snapshot_json")
            ?.let { runCatching { JsonSupport.json.parseToJsonElement(it) as? JsonObject }.getOrNull() }
        val missing = buildList {
            if (entity == null) add("canonical_revision")
            if (entity != null && snapshot != null) {
                listOf("effects", "prerequisites", "costs", "limitations", "forbiddenUses")
                    .filterNot { it in snapshot.keys }
                    .forEach { add(it) }
            }
        }
        val prerequisites = explicitNone(entity?.prerequisites.orEmpty())
        val requirements = explicitNone(entity?.metadata?.stringList("requirements").orEmpty())
        val costs = explicitNone(entity?.costs.orEmpty())
        val limitations = explicitNone(entity?.limitations.orEmpty())
        val forbiddenUses = explicitNone(entity?.forbiddenUses.orEmpty())
        val effects = explicitNone(entity?.effects.orEmpty())
        val eligibility = eligibility(projectId, sessionId, actorId ?: owner, authorized, prerequisites)
        val revisionId = revision?.get("id").orEmpty()
        return CapabilityProvenanceReport(
            projectId = projectId,
            sessionId = sessionId,
            entityType = normalizedType,
            entityId = entityId,
            canonicalEntitySource = if (entity != null) "entity_revisions.snapshot_json" else "missing",
            tableRow = JsonObject(row.mapValues { JsonPrimitive(it.value) }),
            entityRevisionId = revisionId,
            sourcePackId = revision?.get("source_pack_id"),
            sourcePackVersion = revision?.get("source_pack_version")?.toIntOrNull(),
            sourceRevision = listOfNotNull(revisionId, revision?.get("entity_version")?.let { "v$it" }).joinToString("@"),
            dependencyRelations = repository.listDependencyRows(projectId)
                .filter { it["from_id"] == entityId || it["to_id"] == entityId }
                .map { "${it["from_type"]}:${it["from_id"]}-${it["relation"]}->${it["to_type"]}:${it["to_id"]}" },
            owner = owner,
            authorizedActorIds = authorized,
            effects = effects,
            prerequisites = prerequisites,
            requirements = requirements,
            costs = costs,
            limitations = limitations,
            forbiddenUses = forbiddenUses,
            visibility = revision?.get("visibility") ?: entity?.visibility ?: "public",
            currentSessionAccessibility = if (actorId == null || actorId in authorized || authorized.isEmpty()) "ACCESSIBLE" else "INACCESSIBLE",
            currentEligibility = eligibility.first,
            eligibilityReasons = eligibility.second,
            status = if (missing.isEmpty()) "OK" else "CANON_CAPABILITY_CONTRACT_INCOMPLETE",
            missingFields = missing,
        )
    }

    private fun tableRow(projectId: String, entityType: String, entityId: String): Map<String, String> {
        val table = when (entityType) {
            "technique" -> "techniques"
            "item", "weapon" -> "items"
            else -> return emptyMap()
        }
        return repository.tableRows(table).firstOrNull { it["project_id"] == projectId && it["id"] == entityId }.orEmpty()
    }

    private fun eligibility(
        projectId: String,
        sessionId: String,
        actorId: String?,
        authorized: List<String>,
        prerequisites: List<String>,
    ): Pair<String, List<String>> {
        val reasons = mutableListOf<String>()
        if (actorId.isNullOrBlank()) reasons += "actor_unknown"
        if (authorized.isNotEmpty() && actorId !in authorized) reasons += "actor_not_authorized"
        val knownTechniques = repository.listTechniques(projectId).map { it.id }.toSet()
        prerequisites.noneValues().filter { it !in knownTechniques }.forEach { reasons += "missing_prerequisite:$it" }
        if (sessionId.isNotBlank() && actorId != null && repository.getSession(sessionId) == null) reasons += "session_missing"
        return if (reasons.isEmpty()) "ELIGIBLE" to listOf("actor authorized and listed prerequisites are present") else "INELIGIBLE" to reasons
    }
}

class CapabilityCoverageAnalyzer {
    fun report(context: ContextPacket, requirements: List<SemanticRequirement> = TurnSemanticPlanner().plan(context)): CapabilityCoverageReport {
        val refs = requirements.filter {
            it.semanticType in setOf("REFERENCED_TECHNIQUE", "REFERENCED_ITEM", "REFERENCED_WEAPON") &&
                it.entityId.isNotBlank() &&
                it.requiredContractFields.isNotEmpty()
        }
        val selections = refs.mapNotNull { req ->
            val contract = context.activeCapabilityContracts.firstOrNull {
                it.entityType == req.entityType && it.entityId == req.entityId
            } ?: return@mapNotNull null
            CapabilityCoverageSelection(
                entityId = contract.entityId,
                actorId = req.actorId,
                selectedContextPath = "$.activeCapabilityContracts[${context.activeCapabilityContracts.indexOf(contract)}]",
                bytes = JsonSupport.json.encodeToString(contract).toByteArray(Charsets.UTF_8).size,
                sourceProvenance = contract.sourceRevision,
                requiredFieldsPresent = presentFields(contract),
                currentEligibility = contract.currentEligibility,
                visibilityStatus = "public",
            )
        }
        val selectedKeys = selections.mapNotNull { selection ->
            context.activeCapabilityContracts.firstOrNull { it.entityId == selection.entityId }
                ?.let { "${it.entityType}:${selection.entityId}" }
        }.toSet()
        val missing = refs.filter { "${it.entityType}:${it.entityId}" !in selectedKeys }.map { "${it.entityType}:${it.entityId}" }
        val auth = refs.map { req ->
            val contract = context.activeCapabilityContracts.firstOrNull { it.entityType == req.entityType && it.entityId == req.entityId }
            "${req.actorId}:${req.entityId}=" + if (contract != null && req.actorId in contract.authorizedActorIds) "PASS" else "FAIL"
        }
        val prereq = refs.map { req ->
            val contract = context.activeCapabilityContracts.firstOrNull { it.entityType == req.entityType && it.entityId == req.entityId }
            "${req.entityId}=" + (contract?.currentEligibility ?: "MISSING")
        }
        return CapabilityCoverageReport(
            referencedCapabilities = refs.map { "${it.entityType}:${it.entityId}" },
            selectedContracts = selections,
            missingContracts = missing,
            authorizationChecks = auth,
            prerequisiteChecks = prereq,
            costCoverage = selections.map { "${it.entityId}:present" },
            limitationCoverage = selections.map { "${it.entityId}:present" },
            qualityStatus = if (missing.isEmpty() && auth.none { it.endsWith("FAIL") } && selections.all { it.requiredFieldsPresent.containsAll(capabilityRequiredFields) }) "PASS" else "FAIL",
        )
    }

    private fun presentFields(contract: CapabilityContract): List<String> = buildList {
        add("id")
        add("name")
        if (contract.authorizedActorIds.isNotEmpty()) add("authorizedActorIds")
        if (contract.effects.isNotEmpty()) add("effects")
        if (contract.prerequisites.isNotEmpty()) add("prerequisites")
        if (contract.requirements.isNotEmpty()) add("requirements")
        if (contract.costs.isNotEmpty()) add("costs")
        if (contract.limitations.isNotEmpty()) add("limitations")
        if (contract.forbiddenUses.isNotEmpty()) add("forbiddenUses")
        add("currentEligibility")
    }
}

class CapabilityUseValidator {
    fun validate(context: ContextPacket, result: AiTurnResult): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val contracts = context.activeCapabilityContracts.associateBy { "${it.entityType}:${it.entityId}" }
        val checks = result.capabilityUseChecks.associateBy { "${it.actorId}:${it.entityType.normalizeCapabilityType()}:${it.entityId}" }
        result.characterActions.forEachIndexed { index, action ->
            action.usedTechniqueIds.forEach { id ->
                val contract = contracts["technique:$id"]
                val check = checks["${action.characterId}:technique:$id"]
                if (contract == null) {
                    errors += ValidationError("REFERENCED_CAPABILITY_CONTRACT_MISSING", "characterActions[$index] uses technique without activeCapabilityContract: $id")
                } else {
                    errors += validateUse(action.characterId, "technique", id, contract, check, result)
                }
            }
            action.usedItemIds.forEach { id ->
                val type = contracts.keys.firstOrNull { it.endsWith(":$id") }?.substringBefore(":") ?: "item"
                val contract = contracts["$type:$id"]
                val check = checks["${action.characterId}:$type:$id"]
                if (contract != null) errors += validateUse(action.characterId, type, id, contract, check, result)
            }
        }
        result.capabilityUseChecks.forEachIndexed { index, check ->
            val key = "${check.entityType.normalizeCapabilityType()}:${check.entityId}"
            if (contracts[key] == null) {
                errors += ValidationError("REFERENCED_CAPABILITY_CONTRACT_MISSING", "capabilityUseChecks[$index] references capability not in context: $key")
            }
        }
        return errors
    }

    private fun validateUse(
        actorId: String,
        entityType: String,
        entityId: String,
        contract: CapabilityContract,
        check: com.localnovel.engine.model.CapabilityUseCheck?,
        result: AiTurnResult,
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        if (actorId !in contract.authorizedActorIds) {
            errors += ValidationError("CAPABILITY_ACTOR_NOT_AUTHORIZED", "$actorId is not authorized to use $entityType:$entityId")
        }
        val incomplete = capabilityRequiredFields.filter { field ->
            when (field) {
                "id" -> contract.entityId.isBlank()
                "name" -> contract.name.isBlank()
                "authorizedActorIds" -> contract.authorizedActorIds.isEmpty()
                "effects" -> contract.effects.isEmpty()
                "prerequisites" -> contract.prerequisites.isEmpty()
                "requirements" -> contract.requirements.isEmpty()
                "costs" -> contract.costs.isEmpty()
                "limitations" -> contract.limitations.isEmpty()
                "forbiddenUses" -> contract.forbiddenUses.isEmpty()
                "currentEligibility" -> contract.currentEligibility.isBlank()
                else -> false
            }
        }
        if (incomplete.isNotEmpty()) {
            errors += ValidationError("CAPABILITY_CONTRACT_INCOMPLETE", "$entityType:$entityId missing ${incomplete.joinToString(",")}")
        }
        if (check == null) {
            errors += ValidationError("missing_capability_use_check", "$actorId uses $entityType:$entityId without capabilityUseChecks entry.")
            return errors
        }
        val missingPrereqs = contract.prerequisites.noneValues().filterNot { it in check.satisfiedPrerequisites }
        if (contract.currentEligibility == "INELIGIBLE" && missingPrereqs.isNotEmpty() && check.eligibilityClaim == "SUCCESS") {
            errors += ValidationError("CAPABILITY_PREREQUISITE_UNSATISFIED", "$entityType:$entityId prerequisites are not satisfied: ${missingPrereqs.joinToString(",")}")
        }
        val costDeltas = result.allDeltaOperations().filter { op ->
            op.type == DeltaOperationType.UPDATE_CHARACTER_STATE &&
                op.targetId == actorId &&
                op.payload.toString().contains(entityId)
        }
        if (check.eligibilityClaim == "SUCCESS" && contract.costs.noneValues().isNotEmpty()) {
            if (check.appliedCosts.isEmpty()) {
                errors += ValidationError("missing_capability_cost_delta", "$entityType:$entityId successful use must list appliedCosts.")
            }
            if (costDeltas.isEmpty()) {
                errors += ValidationError("missing_capability_cost_delta", "$entityType:$entityId successful use requires cost state delta.")
            }
        }
        contract.forbiddenUses.forEach { forbidden ->
            if (check.intendedUse.contains(forbidden, ignoreCase = true) || result.narrative.contains(forbidden, ignoreCase = true)) {
                errors += ValidationError("capability_forbidden_use", "$entityType:$entityId forbidden use appears: $forbidden")
            }
        }
        return errors
    }
}

internal data class CapabilityReference(
    val actorId: String,
    val entityType: String,
    val entityId: String,
    val requestedUse: String,
)

internal fun detectReferences(context: ContextPacket): List<CapabilityReference> {
    val input = context.playerInput
    val playerId = context.characters.firstOrNull { it.isPlayer }?.id
        ?: context.characterStates.firstOrNull { state -> context.characters.any { it.id == state.characterId && it.isPlayer } }?.characterId
        ?: "char-player"
    val techniqueRefs = context.techniques
        .filter { technique -> matchesNamedReference(input, technique.name, technique.tags) && hasExplicitCapabilityIntent(input) }
        .map { CapabilityReference(playerId, "technique", it.id, requestedUse(input)) }
    val itemRefs = context.items
        .filter { item -> matchesNamedReference(input, item.name, item.tags) && hasExplicitCapabilityIntent(input) }
        .map {
            val type = if (it.tags.any { tag -> tag.equals("weapon", true) }) "weapon" else "item"
            CapabilityReference(playerId, type, it.id, requestedUse(input))
        }
    return (techniqueRefs + itemRefs).distinctBy { "${it.actorId}:${it.entityType}:${it.entityId}" }
}

internal fun CapabilityProvenanceReport.toContract(intendedUse: String): CapabilityContract =
    CapabilityContract(
        entityType = entityType,
        entityId = entityId,
        name = tableRow["name"]?.jsonPrimitive?.contentOrNull ?: entityId,
        authorizedActorIds = authorizedActorIds,
        intendedUse = intendedUse,
        effects = effects,
        prerequisites = prerequisites,
        requirements = requirements,
        costs = costs,
        limitations = limitations,
        forbiddenUses = forbiddenUses,
        currentEligibility = currentEligibility,
        eligibilityReasons = eligibilityReasons,
        allowedNarrativeClaims = effects,
        forbiddenNarrativeClaims = forbiddenUses + limitations.map { "不得違反限制：$it" },
        sourceRevision = sourceRevision,
    )

internal fun String.normalizeCapabilityType(): String =
    when (lowercase()) {
        "techniques" -> "technique"
        "items" -> "item"
        "weapons" -> "weapon"
        else -> lowercase()
    }

private fun Map<String, String>.string(key: String): String? = this[key]?.takeIf { it.isNotBlank() }

private fun JsonObject.stringList(key: String): List<String>? =
    this[key]?.let { element ->
        when (element) {
            is JsonArray -> element.mapNotNull { it.jsonPrimitive.contentOrNull }
            else -> element.jsonPrimitive.contentOrNull?.takeIf { it.isNotBlank() }?.let { listOf(it) }
        }
    }

private fun explicitNone(values: List<String>): List<String> =
    values.map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { listOf("NONE") }

private fun List<String>.noneValues(): List<String> =
    filterNot { it.equals("NONE", ignoreCase = true) }

private fun matchesNamedReference(input: String, name: String, tags: List<String>): Boolean {
    val candidates = (listOf(name) + tags).map { it.trim() }.filter { it.length >= 2 }
    return candidates.any { input.contains(it, ignoreCase = true) }
}

private fun hasExplicitCapabilityIntent(input: String): Boolean =
    listOf("使用", "運起", "運用", "施展", "啟動", "揮動", "揮", "依靠", "activate", "wield")
        .any { input.contains(it, ignoreCase = true) }

private fun requestedUse(input: String): String =
    input.trim().replace(Regex("\\s+"), " ").take(160)
