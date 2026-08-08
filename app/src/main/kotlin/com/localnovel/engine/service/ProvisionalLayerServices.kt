package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CanonThinZoneImportResult
import com.localnovel.engine.model.CanonThinZoneView
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.ProvisionalAgencyProfile
import com.localnovel.engine.model.ProvisionalBacklogExport
import com.localnovel.engine.model.ProvisionalEntityProposal
import com.localnovel.engine.model.ProvisionalEntityView
import com.localnovel.engine.model.ProvisionalPromotionPatchDraft
import com.localnovel.engine.model.ProvisionalPromotionRecommendation
import com.localnovel.engine.model.SettingPackEntities
import com.localnovel.engine.model.SettingPackEntity
import com.localnovel.engine.model.ValidationError
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant

object ProvisionalEntityTypes {
    val all: Set<String> = setOf("CHARACTER", "CREATURE", "FACTION_PRESENCE", "LOCATION_PATCH", "RUMOR_EVENT")
    val chineseToCode: Map<String, String> = mapOf(
        "臨時人物" to "CHARACTER",
        "生物" to "CREATURE",
        "勢力跡象" to "FACTION_PRESENCE",
        "小地點" to "LOCATION_PATCH",
        "傳聞" to "RUMOR_EVENT",
    )
}

object ProvisionalThinZoneStatuses {
    const val ACTIVE = "ACTIVE"
    const val PAUSED = "PAUSED"
    const val CLOSED = "CLOSED"
}

class ProvisionalLayerCharterImporter(private val repository: SqliteStoryRepository) {
    fun importCharterFile(projectId: String, charterPath: Path): CanonThinZoneImportResult {
        val text = Files.readString(charterPath).removePrefix("\uFEFF")
        val root = JsonSupport.json.parseToJsonElement(text).jsonObject
        val zones = root["留白區"]?.jsonArray ?: error("Charter missing 留白區 array: $charterPath")
        val imported = zones.mapNotNull { element ->
            val zone = element.jsonObject
            val thinZoneId = zone.stringField("留白區代號") ?: return@mapNotNull null
            val label = zone.stringField("名稱") ?: thinZoneId
            val reason = zone.stringField("原因") ?: ""
            val status = when (zone.stringField("狀態")) {
                "啟用" -> ProvisionalThinZoneStatuses.ACTIVE
                "暫停" -> ProvisionalThinZoneStatuses.PAUSED
                "關閉" -> ProvisionalThinZoneStatuses.CLOSED
                else -> ProvisionalThinZoneStatuses.ACTIVE
            }
            val allowedTypes = zone["可補類型"]?.jsonArray?.mapNotNull { item ->
                ProvisionalEntityTypes.chineseToCode[item.jsonPrimitive.contentOrNull.orEmpty()]
                    ?: item.jsonPrimitive.contentOrNull
            }.orEmpty().filter { it in ProvisionalEntityTypes.all }
            repository.upsertCanonThinZone(
                projectId = projectId,
                thinZoneId = thinZoneId,
                label = label,
                reason = reason,
                allowedProposalTypes = allowedTypes,
                status = status,
                sourcePath = charterPath.toString().replace('\\', '/'),
            )
            thinZoneId
        }
        val activeCount = repository.listCanonThinZones(projectId).count { it.status == ProvisionalThinZoneStatuses.ACTIVE }
        return CanonThinZoneImportResult(
            verdict = "PASS",
            status = "CANON_THIN_ZONE_IMPORT_PASS",
            projectId = projectId,
            sourcePath = charterPath.toString().replace('\\', '/'),
            importedCount = imported.size,
            activeCount = activeCount,
            thinZoneIds = imported,
        )
    }
}

class ProvisionalLayerValidator(private val repository: SqliteStoryRepository) {
    private val plotCourierRoles = setOf("exposition_only", "plot_courier", "純解說員", "工具人信使")
    private val weakRefusalPhrases = listOf("願意幫忙", "會配合", "樂意協助", "願意帶路", "願意解釋")
    private val expositionBeatPhrases = listOf("提供情報", "帶路", "解釋設定", "說明世界", "告知設定")
    private val qualificationKeys = listOf("SOUL", "IRREPLACEABLE", "READER_WANTS_MORE", "NARRATIVE_LANDED")

    fun validate(context: ContextPacket, result: AiTurnResult): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val thinZones = repository.listCanonThinZones(context.project.id)
            .associateBy { it.thinZoneId }
        val existingProposalIds = repository.listProvisionalEntities(context.sessionId)
            .map { it.proposalId }
            .toSet()
        val characterIds = context.characters.map { it.id }.toSet()

        result.entityProposals.forEachIndexed { index, proposal ->
            errors += validateProposal(context, proposal, index, thinZones, existingProposalIds, characterIds)
        }
        result.promotionRecommendations.forEachIndexed { index, recommendation ->
            errors += validatePromotionRecommendation(context, recommendation, index, result.entityProposals)
        }
        return errors
    }

    private fun validateProposal(
        context: ContextPacket,
        proposal: ProvisionalEntityProposal,
        index: Int,
        thinZones: Map<String, CanonThinZoneView>,
        existingProposalIds: Set<String>,
        characterIds: Set<String>,
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val prefix = "entityProposals[$index]"
        if (proposal.proposalId.isBlank()) {
            errors += ValidationError("PROVISIONAL_INVALID_PROPOSAL", "$prefix proposalId is required.")
        }
        if (proposal.proposalId in existingProposalIds) {
            errors += ValidationError("PROVISIONAL_DUPLICATE_ID", "$prefix proposalId already exists in session: ${proposal.proposalId}")
        }
        val zone = thinZones[proposal.thinZoneId]
        if (zone == null || zone.status != ProvisionalThinZoneStatuses.ACTIVE) {
            errors += ValidationError("PROVISIONAL_THIN_ZONE_REQUIRED", "$prefix thinZoneId is missing or not ACTIVE: ${proposal.thinZoneId}")
        } else if (proposal.entityType !in zone.allowedProposalTypes) {
            errors += ValidationError("PROVISIONAL_THIN_ZONE_TYPE", "$prefix entityType ${proposal.entityType} is not allowed in thin zone ${proposal.thinZoneId}")
        }
        if (proposal.entityType !in ProvisionalEntityTypes.all) {
            errors += ValidationError("PROVISIONAL_INVALID_TYPE", "$prefix entityType is invalid: ${proposal.entityType}")
        }
        if (proposal.displayName.isBlank() || proposal.introductionBeat.isBlank()) {
            errors += ValidationError("PROVISIONAL_INCOMPLETE", "$prefix displayName and introductionBeat are required.")
        }
        proposal.perceivedByCharacterIds.forEach { actorId ->
            if (actorId !in characterIds) {
                errors += ValidationError("PROVISIONAL_UNKNOWN_ACTOR", "$prefix perceivedByCharacterIds contains unknown actor: $actorId")
            }
        }
        if (proposal.entityType == "CHARACTER") {
            errors += validateCharacterSoul(prefix, proposal)
        }
        return errors
    }

    private fun validateCharacterSoul(prefix: String, proposal: ProvisionalEntityProposal): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val agency = proposal.agencyProfile
        if (agency.desire.isBlank() || agency.fearOrCost.isBlank() || agency.blindSpot.isBlank() || agency.refusalCapacity.isBlank()) {
            errors += ValidationError("PROVISIONAL_INCOMPLETE", "$prefix CHARACTER requires full agencyProfile.")
        }
        if (proposal.narrativeRole.isBlank() || proposal.narrativeRole in plotCourierRoles) {
            errors += ValidationError("PROVISIONAL_PLOT_COURIER", "$prefix narrativeRole looks like a plot courier.")
        }
        if (weakRefusalPhrases.any { agency.refusalCapacity.contains(it) }) {
            errors += ValidationError("PROVISIONAL_PLOT_COURIER", "$prefix refusalCapacity is too compliant for a soulful character.")
        }
        if (expositionBeatPhrases.any { proposal.introductionBeat.contains(it) } &&
            agency.fearOrCost.length < 6
        ) {
            errors += ValidationError("PROVISIONAL_PLOT_COURIER", "$prefix introductionBeat is exposition-only without cost.")
        }
        return errors
    }

    private fun validatePromotionRecommendation(
        context: ContextPacket,
        recommendation: ProvisionalPromotionRecommendation,
        index: Int,
        proposals: List<ProvisionalEntityProposal>,
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val prefix = "promotionRecommendations[$index]"
        if (recommendation.recommendationId.isBlank()) {
            errors += ValidationError("PROVISIONAL_PROMOTION_INCOMPLETE", "$prefix recommendationId is required.")
        }
        val targetProposalId = recommendation.proposalId
        val targetEntityId = recommendation.entityId
        if (targetProposalId.isNullOrBlank() && targetEntityId.isNullOrBlank()) {
            errors += ValidationError("PROVISIONAL_PROMOTION_INCOMPLETE", "$prefix must reference proposalId or entityId.")
        }
        if (recommendation.reason.isBlank() || recommendation.readerValue.isBlank() ||
            recommendation.irreplaceability.isBlank() || recommendation.formalIdentityDraft.isBlank() ||
            recommendation.exitIfNotPromoted.isBlank()
        ) {
            errors += ValidationError("PROVISIONAL_PROMOTION_INCOMPLETE", "$prefix must include reason, readerValue, irreplaceability, formalIdentityDraft, exitIfNotPromoted.")
        }
        val matchedProposal = proposals.firstOrNull { it.proposalId == targetProposalId }
            ?: targetEntityId?.let { entityId ->
                repository.listProvisionalEntities(context.sessionId).firstOrNull { it.entityId == entityId }
                    ?.let { view ->
                        ProvisionalEntityProposal(
                            proposalId = view.proposalId,
                            entityType = view.entityType,
                            displayName = view.displayName,
                            thinZoneId = view.thinZoneId,
                            lifespan = view.lifespan,
                            introductionBeat = view.introductionBeat,
                            agencyProfile = view.agencyProfile,
                            narrativeRole = view.narrativeRole,
                            evidenceHooks = emptyList(),
                        )
                    }
            }
        if (matchedProposal == null && targetProposalId != null) {
            errors += ValidationError("PROVISIONAL_PROMOTION_PREMATURE", "$prefix references unknown provisional entity.")
        }
        if (matchedProposal != null && matchedProposal.entityType == "CHARACTER") {
            val passed = recommendation.qualificationChecks.map { it.uppercase() }.toSet()
            val inferred = inferQualificationChecks(recommendation, matchedProposal)
            val effectivePassed = passed.ifEmpty { inferred }.intersect(qualificationKeys.toSet())
            if (effectivePassed.size < 3) {
                errors += ValidationError("PROVISIONAL_PROMOTION_UNQUALIFIED", "$prefix needs at least 3 qualification checks; got ${effectivePassed.size}.")
            }
            errors += validateCharacterSoul(prefix, matchedProposal)
        }
        return errors
    }

    private fun inferQualificationChecks(
        recommendation: ProvisionalPromotionRecommendation,
        proposal: ProvisionalEntityProposal,
    ): Set<String> {
        val checks = mutableSetOf<String>()
        val agency = proposal.agencyProfile
        if (agency.desire.isNotBlank() && agency.fearOrCost.isNotBlank() &&
            agency.blindSpot.isNotBlank() && agency.refusalCapacity.isNotBlank() &&
            proposal.narrativeRole !in plotCourierRoles
        ) {
            checks += "SOUL"
        }
        if (recommendation.irreplaceability.length >= 12) checks += "IRREPLACEABLE"
        if (recommendation.readerValue.length >= 12) checks += "READER_WANTS_MORE"
        if (proposal.introductionBeat.length >= 12 || recommendation.reason.length >= 20) checks += "NARRATIVE_LANDED"
        return checks
    }
}

class ProvisionalLayerService(private val repository: SqliteStoryRepository) {
    fun enrichContext(context: ContextPacket): ContextPacket {
        val thinZones = repository.listCanonThinZones(context.project.id)
            .filter { it.status == ProvisionalThinZoneStatuses.ACTIVE }
        val sceneId = context.activeScene?.id
        val provisionalEntities = repository.listProvisionalEntities(context.sessionId)
            .filter { it.status == "ACTIVE" }
            .filter { sceneId == null || it.currentSceneId == null || it.currentSceneId == sceneId }
        val readerMode = repository.getSessionReaderMode(context.sessionId)
        return NarrativeDeliveryResolver.resolve(
            context.copy(
                canonThinZones = thinZones,
                provisionalEntities = provisionalEntities,
                readerMode = readerMode,
            ),
            readerMode = readerMode,
        )
    }

    fun applyTurnProvisionalWrites(context: ContextPacket, result: AiTurnResult) {
        repository.applyProvisionalTurnWrites(context, result)
    }

    fun exportBacklog(sessionId: String): ProvisionalBacklogExport {
        val session = repository.getSession(sessionId) ?: error("Session not found: $sessionId")
        val entities = repository.listProvisionalEntities(sessionId)
        val recommendations = repository.listPendingPromotionRecommendations(sessionId)
        return ProvisionalBacklogExport(
            sessionId = sessionId,
            projectId = session.projectId,
            activeEntities = entities.filter { it.status == "ACTIVE" },
            pendingAiRecommendations = recommendations,
            pendingAuthorItems = recommendations.size,
        )
    }

    fun discardEntity(sessionId: String, entityId: String, actor: String, note: String) {
        repository.updateProvisionalEntityStatus(sessionId, entityId, "DISCARDED")
        repository.recordPromotionAction(
            projectId = repository.getSession(sessionId)?.projectId ?: error("Session not found"),
            sessionId = sessionId,
            entityId = entityId,
            queueId = null,
            action = "DISCARD",
            actor = actor,
            note = note,
        )
    }

    fun buildPromotionPatchDraft(sessionId: String, entityId: String): ProvisionalPromotionPatchDraft {
        val session = repository.getSession(sessionId) ?: error("Session not found: $sessionId")
        val entity = repository.listProvisionalEntities(sessionId).firstOrNull { it.entityId == entityId }
            ?: error("Provisional entity not found: $entityId")
        val recommendation = repository.listPendingPromotionRecommendations(sessionId)
            .firstOrNull { it.entityId == entityId || it.proposalId == entity.proposalId }
        val characterEntity = SettingPackEntity(
            id = "char-provisional-${entity.proposalId}",
            name = entity.displayName,
            description = recommendation?.formalIdentityDraft ?: entity.introductionBeat,
            role = entity.narrativeRole,
            visibility = "public",
        )
        return ProvisionalPromotionPatchDraft(
            patchId = "provisional-promotion-${entity.proposalId}",
            projectId = session.projectId,
            entityId = entityId,
            displayName = entity.displayName,
            sourceRecommendationJson = JsonSupport.json.encodeToString(recommendation ?: defaultSelfRecommendation(entity.toProposal())),
            entities = SettingPackEntities(characters = listOf(characterEntity)),
        )
    }

    private fun defaultSelfRecommendation(proposal: ProvisionalEntityProposal): ProvisionalPromotionRecommendation =
        ProvisionalPromotionRecommendation(
            recommendationId = "self-${proposal.proposalId}",
            proposalId = proposal.proposalId,
            qualificationChecks = listOf("SOUL", "NARRATIVE_LANDED"),
            reason = proposal.introductionBeat,
            readerValue = proposal.introductionBeat,
            irreplaceability = proposal.agencyProfile.desire,
            formalIdentityDraft = "${proposal.displayName}｜${proposal.narrativeRole}",
            exitIfNotPromoted = "降為傳聞或離場，不再以正式角色引用。",
        )

    private fun ProvisionalEntityView.toProposal(): ProvisionalEntityProposal =
        ProvisionalEntityProposal(
            proposalId = proposalId,
            entityType = entityType,
            displayName = displayName,
            thinZoneId = thinZoneId,
            lifespan = lifespan,
            introductionBeat = introductionBeat,
            agencyProfile = agencyProfile,
            narrativeRole = narrativeRole,
        )
}