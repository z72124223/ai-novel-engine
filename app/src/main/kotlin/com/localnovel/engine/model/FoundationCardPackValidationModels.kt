@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FoundationCardPack(
    val schemaVersion: String = "",
    val status: String = "",
    val packId: String = "",
    val title: String = "",
    val locale: String = "",
    val sourceNotes: List<String> = emptyList(),
    val canonStatus: String = "",
    val approvalStatus: String = "",
    val settingPackReadiness: String = "",
    val retrievalTags: List<String> = emptyList(),
    val downstreamUse: FoundationCardDownstreamUse? = null,
    val boundaryFlags: FoundationCardBoundaryFlags? = null,
    val cards: List<FoundationCard> = emptyList(),
)

@Serializable
data class FoundationCard(
    val id: String = "",
    val family: String = "",
    val title: String = "",
    val sourceNotes: List<String> = emptyList(),
    val visibility: String = "",
    val canonStatus: String = "",
    val approvalStatus: String = "",
    val settingPackReadiness: String = "",
    val retrievalTags: List<String> = emptyList(),
    val downstreamUse: FoundationCardDownstreamUse? = null,
    val boundaryFlags: FoundationCardBoundaryFlags? = null,
    val publicSummary: String = "",
    val reviewOnlyNotes: List<String> = emptyList(),
    val constraints: List<String> = emptyList(),
    val forbiddenImplications: List<String> = emptyList(),
)

@Serializable
data class FoundationCardDownstreamUse(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val queryEligible: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val scriptReadyScenePacketEligible: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val settingPackConversionEligible: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val assetRegistryEligible: Boolean = false,
)

@Serializable
data class FoundationCardBoundaryFlags(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val canonApproved: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val settingPackData: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val runtimeSqliteData: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val approvedAssetData: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val providerApiCalls: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val rendererInvocation: String = "none",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val ffmpegInvocation: String = "none",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val ttsInvocation: String = "none",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mediaGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val finalMediaApproval: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val releaseApproved: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionLock: Boolean = false,
)

@Serializable
data class FoundationCardPackValidationReport(
    val verdict: String,
    val status: String = "",
    val validationId: String = "",
    val sourcePackFile: String = "",
    val packId: String = "",
    val title: String = "",
    val locale: String = "",
    val checks: List<FoundationCardPackValidationCheck> = emptyList(),
    val evidence: FoundationCardPackValidationEvidence = FoundationCardPackValidationEvidence(),
    val boundarySummary: FoundationCardPackValidationBoundarySummary =
        FoundationCardPackValidationBoundarySummary(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String = "foundation_card_pack_validation_v0_only_not_canon_or_runtime",
    val nextAllowedDownstreamStep: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val progressCaveat: String = "local baseline 100% is not final product completion",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<FoundationCardPackValidationFinding> = emptyList(),
)

@Serializable
data class FoundationCardPackValidationCheck(
    val checkId: String = "",
    val status: String = "FAIL",
    val category: String = "",
    val message: String = "",
    val evidence: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredForValidation: Boolean = true,
)

@Serializable
data class FoundationCardPackValidationEvidence(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val cardCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredFamilyCount: Int = 7,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val presentRequiredFamilyCount: Int = 0,
    val presentFamilies: List<String> = emptyList(),
    val missingFamilies: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val duplicateCardIdCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val missingSourceNotesCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val missingRetrievalTagsCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val missingDownstreamUseCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val unsafeBoundaryFlagCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val readinessClaimCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val eligibilityClaimCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val failedCheckCount: Int = 0,
)

@Serializable
data class FoundationCardPackValidationBoundarySummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val candidateOnly: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val canonApproved: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val settingPackData: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val runtimeSqliteData: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val approvedAssetData: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val providerApiCalls: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mediaGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val finalMediaApproval: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val releaseApproved: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val productionLock: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val allRendererInvocationNone: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val allFfmpegInvocationNone: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val allTtsInvocationNone: Boolean = false,
)

@Serializable
data class FoundationCardPackValidationFinding(
    @SerialName("rule_id")
    val ruleId: String,
    val category: String,
    val severity: String,
    val path: String,
    val message: String,
    val evidence: List<String> = emptyList(),
    @SerialName("suggested_fix")
    val suggestedFix: String = "",
)
