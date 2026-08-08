package com.localnovel.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class ProvisionalAgencyProfile(
    val desire: String = "",
    val fearOrCost: String = "",
    val blindSpot: String = "",
    val refusalCapacity: String = "",
)

@Serializable
data class ProvisionalEntityProposal(
    val proposalId: String,
    val entityType: String,
    val displayName: String,
    val thinZoneId: String,
    val lifespan: String = "SCENE_ONLY",
    val introductionBeat: String,
    val agencyProfile: ProvisionalAgencyProfile = ProvisionalAgencyProfile(),
    val narrativeRole: String,
    val evidenceHooks: List<String> = emptyList(),
    val perceivedByCharacterIds: List<String> = emptyList(),
)

@Serializable
data class ProvisionalPromotionRecommendation(
    val recommendationId: String,
    val proposalId: String? = null,
    val entityId: String? = null,
    val source: String = "AI",
    val qualificationChecks: List<String> = emptyList(),
    val reason: String,
    val readerValue: String,
    val irreplaceability: String,
    val formalIdentityDraft: String,
    val exitIfNotPromoted: String,
)

@Serializable
data class CreativeGapNote(
    val gapId: String,
    val thinZoneId: String,
    val readerInterestHypothesis: String,
    val chosenProposalIds: List<String> = emptyList(),
)

@Serializable
data class CanonThinZoneView(
    val thinZoneId: String,
    val label: String,
    val reason: String,
    val allowedProposalTypes: List<String> = emptyList(),
    val status: String = "ACTIVE",
)

@Serializable
data class ProvisionalEntityView(
    val entityId: String,
    val proposalId: String,
    val entityType: String,
    val displayName: String,
    val thinZoneId: String,
    val lifespan: String,
    val introductionBeat: String,
    val agencyProfile: ProvisionalAgencyProfile = ProvisionalAgencyProfile(),
    val narrativeRole: String,
    val status: String,
    val currentSceneId: String? = null,
    val promotionQueueStatus: String? = null,
)

@Serializable
data class CanonThinZoneImportResult(
    val verdict: String,
    val status: String,
    val projectId: String,
    val sourcePath: String,
    val importedCount: Int,
    val activeCount: Int,
    val thinZoneIds: List<String> = emptyList(),
)

@Serializable
data class ProvisionalBacklogExport(
    val sessionId: String,
    val projectId: String,
    val activeEntities: List<ProvisionalEntityView> = emptyList(),
    val pendingAiRecommendations: List<ProvisionalPromotionRecommendation> = emptyList(),
    val pendingAuthorItems: Int = 0,
)

@Serializable
data class ProvisionalPromotionPatchDraft(
    val patchId: String,
    val projectId: String,
    val entityId: String,
    val displayName: String,
    val sourceRecommendationJson: String,
    val entities: SettingPackEntities = SettingPackEntities(),
    val note: String = "MVP-8 provisional promotion patch draft; validate before apply.",
)