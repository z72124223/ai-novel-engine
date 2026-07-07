@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FoundationCardPackReviewReport(
    val verdict: String,
    val status: String = "",
    val reviewId: String = "",
    val sourcePackFile: String = "",
    val sourceValidationFile: String = "",
    val sourceValidationId: String = "",
    val packId: String = "",
    val title: String = "",
    val locale: String = "",
    val checks: List<FoundationCardPackReviewCheck> = emptyList(),
    val evidence: FoundationCardPackReviewEvidence = FoundationCardPackReviewEvidence(),
    val boundarySummary: FoundationCardPackReviewBoundarySummary = FoundationCardPackReviewBoundarySummary(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String = "foundation_card_pack_review_only_not_canon_or_runtime",
    val nextAllowedDownstreamStep: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val progressCaveat: String = "local baseline 100% is not true final product completion",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<FoundationCardPackReviewFinding> = emptyList(),
)

@Serializable
data class FoundationCardPackReviewCheck(
    val checkId: String = "",
    val status: String = "FAIL",
    val category: String = "",
    val message: String = "",
    val evidence: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredForReview: Boolean = true,
)

@Serializable
data class FoundationCardPackReviewEvidence(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourcePackFileMatches: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val packIdAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val titleAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val localeAligned: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val cardCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredFamilyCount: Int = 7,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val presentRequiredFamilyCount: Int = 0,
    val presentFamilies: List<String> = emptyList(),
    val missingFamilies: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val validationFailedCheckCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val validationFindingCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val reviewFailedCheckCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val duplicateCardIdCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val missingTraceabilityCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val missingDownstreamUseCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val unsafeBoundaryFlagCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val readinessClaimCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val eligibilityClaimCount: Int = 0,
)

@Serializable
data class FoundationCardPackReviewBoundarySummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val technicalReviewOnly: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val candidateOnly: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val validationPass: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val humanApproval: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val canonApproval: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val settingPackConversion: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val runtimeSqliteData: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val assetRegistryApproval: Boolean = false,
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
    val demandDrivenQueryImplementation: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val allRendererInvocationNone: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val allFfmpegInvocationNone: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val allTtsInvocationNone: Boolean = false,
)

@Serializable
data class FoundationCardPackReviewFinding(
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
