package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StoryboardPlanningPreflightReport(
    val verdict: String,
    val preflightId: String = "",
    val acceptedScriptPacketFile: String = "",
    val acceptedScriptPacketId: String = "",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<StoryboardPlanningPreflightFinding> = emptyList(),
    val evidence: StoryboardPlanningPreflightEvidence = StoryboardPlanningPreflightEvidence(),
)

@Serializable
data class StoryboardPlanningPreflightFinding(
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

@Serializable
data class StoryboardPlanningPreflightEvidence(
    val packetVerdict: String = "",
    val structureBridgeVerdict: String = "",
    val sourceBindingVerdict: String = "",
    val structureVerdict: String = "",
    val scriptPacketDraftHashPresent: Boolean = false,
    val sourceReportFileCount: Int = 0,
    val sourceReportVerdictCount: Int = 0,
    val protectedContinuityAnchorCount: Int = 0,
    val residualRiskCount: Int = 0,
)

@Serializable
data class StoryboardPlanningBrief(
    val verdict: String,
    val briefId: String = "",
    val acceptedScriptPacketFile: String = "",
    val storyboardPlanningPreflightFile: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val sourceReports: StoryboardPlanningBriefSourceReports = StoryboardPlanningBriefSourceReports(),
    val protectedContinuityAnchorsSummary: AcceptedScriptPacketContinuityAnchorsSummary =
        AcceptedScriptPacketContinuityAnchorsSummary(),
    val remainingResidualRisks: List<String> = emptyList(),
    val sceneBeatPlanningSummary: StoryboardPlanningSceneBeatSummary = StoryboardPlanningSceneBeatSummary(),
    val framePlanningConstraints: StoryboardPlanningFrameConstraints = StoryboardPlanningFrameConstraints(),
    val mustShowBoundary: List<String> = emptyList(),
    val mustNotShowBoundary: List<String> = emptyList(),
    val assetNeedsCarriedForward: StoryboardPlanningAssetCarryForward = StoryboardPlanningAssetCarryForward(),
    val reviewChecklist: List<StoryboardPlanningReviewChecklistItem> = emptyList(),
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<StoryboardPlanningBriefFinding> = emptyList(),
    val evidence: StoryboardPlanningBriefEvidence = StoryboardPlanningBriefEvidence(),
)

@Serializable
data class StoryboardPlanningBriefSourceReports(
    val acceptedScriptPacketVerdict: String = "",
    val storyboardPlanningPreflightReportFile: String = "",
    val storyboardPlanningPreflightVerdict: String = "",
    val structureBridgeReportFile: String = "",
    val structureBridgeVerdict: String = "",
    val sourceBindingVerdict: String = "",
    val structureVerdict: String = "",
    val scriptPacketPreflightReportFile: String = "",
    val acceptedDraftPacketFile: String = "",
)

@Serializable
data class StoryboardPlanningSceneBeatSummary(
    val planningBasis: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val sceneBeatExtractionStatus: String = "not_extracted_in_this_gate",
    val notes: List<String> = emptyList(),
)

@Serializable
data class StoryboardPlanningFrameConstraints(
    val frameDraftingStatus: String = "not_started",
    val continuityPolicy: String = "",
    val cameraPolicy: String = "",
    val downstreamBoundary: String = "",
)

@Serializable
data class StoryboardPlanningAssetCarryForward(
    val sourceScriptPacketDraftFile: String = "",
    val extractionStatus: String = "not_extracted_in_this_gate",
    val policy: String = "",
)

@Serializable
data class StoryboardPlanningReviewChecklistItem(
    val itemId: String = "",
    val category: String = "",
    val requiredCheck: String = "",
    val status: String = "required_for_next_gate",
)

@Serializable
data class StoryboardPlanningBriefFinding(
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

@Serializable
data class StoryboardPlanningBriefEvidence(
    val acceptedScriptPacketVerdict: String = "",
    val storyboardPlanningPreflightVerdict: String = "",
    val scriptPacketDraftHashPresent: Boolean = false,
    val sourceReportFileCount: Int = 0,
    val sourceReportVerdictCount: Int = 0,
    val protectedContinuityAnchorCount: Int = 0,
    val residualRiskCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val reviewChecklistCount: Int = 0,
)

@Serializable
data class StoryboardPacketDraft(
    val packetId: String = "",
    val storyboardPlanningBriefId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val sourceStoryboardPlanningBriefFile: String = "",
    val protectedContinuityAnchors: List<String> = emptyList(),
    val frames: List<StoryboardPacketFrameDraft> = emptyList(),
    val assetCandidates: List<StoryboardPacketAssetCandidateDraft> = emptyList(),
    val reviewNotes: String = "",
)

@Serializable
data class StoryboardPacketFrameDraft(
    val frameId: String = "",
    val sceneReference: String = "",
    val beatReference: String = "",
    val visualIntent: String = "",
    val cameraIntent: String = "",
    val mustShow: List<String> = emptyList(),
    val doNotShow: List<String> = emptyList(),
    val continuityAnchors: List<String> = emptyList(),
    val assetCandidateRefs: List<String> = emptyList(),
    val reviewNotes: String = "",
    val imageOutputPath: String = "",
    val videoOutputPath: String = "",
    val generationApi: String = "",
    val assetApprovalStatus: String = "",
)

@Serializable
data class StoryboardPacketAssetCandidateDraft(
    val candidateId: String = "",
    val requirement: String = "",
    val sourceFrameIds: List<String> = emptyList(),
    val approvalStatus: String = "candidate_only",
    val reviewNotes: String = "",
)

@Serializable
data class StoryboardPacketDraftValidationReport(
    val verdict: String,
    val storyboardPlanningBriefFile: String = "",
    val storyboardPacketDraftFile: String = "",
    val storyboardPlanningBriefId: String = "",
    val storyboardPacketDraftId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<StoryboardPacketDraftValidationFinding> = emptyList(),
    val evidence: StoryboardPacketDraftValidationEvidence = StoryboardPacketDraftValidationEvidence(),
)

@Serializable
data class StoryboardPacketDraftValidationFinding(
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

@Serializable
data class StoryboardPacketDraftValidationEvidence(
    val planningBriefVerdict: String = "",
    val planningBriefNextAllowedDownstreamStep: String = "",
    val sourceBindingFieldCount: Int = 0,
    val frameCount: Int = 0,
    val protectedContinuityAnchorCount: Int = 0,
    val protectedContinuityAnchorReferencedCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenOutputPathCount: Int = 0,
    val externalGenerationApiHitCount: Int = 0,
)

@Serializable
data class StoryboardPacketReviewPacket(
    val verdict: String,
    val reviewId: String = "",
    val reviewStatus: String = "FAIL",
    val storyboardPacketDraftFile: String = "",
    val storyboardPacketDraftReportFile: String = "",
    val storyboardPacketDraftId: String = "",
    val storyboardPlanningBriefId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val sourceReports: StoryboardPacketReviewSourceReports = StoryboardPacketReviewSourceReports(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val frameCount: Int = 0,
    val frameIds: List<String> = emptyList(),
    val protectedContinuityAnchorsSummary: AcceptedScriptPacketContinuityAnchorsSummary =
        AcceptedScriptPacketContinuityAnchorsSummary(),
    val assetCandidateRequirements: List<StoryboardPacketReviewAssetRequirement> = emptyList(),
    val remainingResidualRisks: List<String> = emptyList(),
    val reviewChecklist: List<StoryboardPacketReviewChecklistItem> = emptyList(),
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<StoryboardPacketReviewFinding> = emptyList(),
    val evidence: StoryboardPacketReviewEvidence = StoryboardPacketReviewEvidence(),
)

@Serializable
data class StoryboardPacketReviewSourceReports(
    val storyboardPacketDraftValidationReportFile: String = "",
    val storyboardPacketDraftValidationVerdict: String = "",
    val storyboardPacketDraftValidationNextStep: String = "",
)

@Serializable
data class StoryboardPacketReviewAssetRequirement(
    val candidateId: String = "",
    val requirement: String = "",
    val sourceFrameIds: List<String> = emptyList(),
)

@Serializable
data class StoryboardPacketReviewChecklistItem(
    val itemId: String = "",
    val category: String = "",
    val requiredCheck: String = "",
    val status: String = "required_for_human_review",
)

@Serializable
data class StoryboardPacketReviewFinding(
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

@Serializable
data class StoryboardPacketReviewEvidence(
    val draftValidationVerdict: String = "",
    val draftValidationNextAllowedDownstreamStep: String = "",
    val sourceBindingFieldCount: Int = 0,
    val frameCount: Int = 0,
    val assetCandidateRequirementCount: Int = 0,
    val protectedContinuityAnchorCount: Int = 0,
    val residualRiskCount: Int = 0,
    val reviewChecklistCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenOutputPathCount: Int = 0,
    val externalGenerationApiHitCount: Int = 0,
)

@Serializable
data class StoryboardPacketHumanReviewPreflightReport(
    val verdict: String,
    val preflightId: String = "",
    val storyboardPacketReviewFile: String = "",
    val storyboardPacketReviewId: String = "",
    val storyboardPacketDraftFile: String = "",
    val storyboardPacketDraftReportFile: String = "",
    val storyboardPacketDraftId: String = "",
    val storyboardPlanningBriefId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val reviewStatus: String = "",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<StoryboardPacketHumanReviewPreflightFinding> = emptyList(),
    val evidence: StoryboardPacketHumanReviewPreflightEvidence = StoryboardPacketHumanReviewPreflightEvidence(),
)

@Serializable
data class StoryboardPacketHumanReviewPreflightFinding(
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

@Serializable
data class StoryboardPacketHumanReviewPreflightEvidence(
    val reviewPacketVerdict: String = "",
    val reviewStatus: String = "",
    val reviewNextAllowedDownstreamStep: String = "",
    val sourceFieldCount: Int = 0,
    val frameCount: Int = 0,
    val frameIdCount: Int = 0,
    val reviewChecklistCount: Int = 0,
    val protectedContinuityAnchorCount: Int = 0,
    val residualRiskCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
)

@Serializable
data class StoryboardStoryMovementAuditReport(
    val verdict: String,
    val auditId: String = "",
    val storyboardPacketDraftFile: String = "",
    val storyboardPacketReviewFile: String = "",
    val storyboardHumanReviewPreflightFile: String = "",
    val storyboardPacketReviewId: String = "",
    val storyboardPacketDraftId: String = "",
    val storyboardPlanningBriefId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val frameCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val movementEvidenceCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val coveredContinuityAnchorCount: Int = 0,
    val downstreamBoundary: String = "storyboard_story_movement_audit_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<StoryboardStoryMovementAuditFinding> = emptyList(),
    val evidence: StoryboardStoryMovementAuditEvidence = StoryboardStoryMovementAuditEvidence(),
)

@Serializable
data class StoryboardStoryMovementAuditFinding(
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

@Serializable
data class StoryboardStoryMovementAuditEvidence(
    val humanReviewPreflightVerdict: String = "",
    val humanReviewPreflightNextAllowedDownstreamStep: String = "",
    val reviewPacketVerdict: String = "",
    val reviewStatus: String = "",
    val sourceBindingFieldCount: Int = 0,
    val frameCount: Int = 0,
    val uniqueFrameIdCount: Int = 0,
    val movementEvidenceCount: Int = 0,
    val openingPressureEvidenceCount: Int = 0,
    val actionEvidenceCount: Int = 0,
    val consequenceOrChoiceEvidenceCount: Int = 0,
    val protectedContinuityAnchorCount: Int = 0,
    val coveredContinuityAnchorCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
)

@Serializable
data class ReviewableStoryboardPacket(
    val verdict: String,
    val packetId: String = "",
    val packetStatus: String = "FAIL",
    val storyboardPacketDraftFile: String = "",
    val storyboardPacketReviewFile: String = "",
    val storyboardHumanReviewPreflightFile: String = "",
    val storyboardStoryMovementAuditFile: String = "",
    val storyboardPacketReviewId: String = "",
    val storyboardStoryMovementAuditId: String = "",
    val storyboardPacketDraftId: String = "",
    val storyboardPlanningBriefId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val sourceVerdicts: ReviewableStoryboardSourceVerdicts = ReviewableStoryboardSourceVerdicts(),
    val movementEvidenceSummary: ReviewableStoryboardMovementEvidenceSummary =
        ReviewableStoryboardMovementEvidenceSummary(),
    val humanReviewReadinessSummary: ReviewableStoryboardHumanReviewReadinessSummary =
        ReviewableStoryboardHumanReviewReadinessSummary(),
    val frameSummaries: List<ReviewableStoryboardFrameSummary> = emptyList(),
    val protectedContinuityAnchorsSummary: AcceptedScriptPacketContinuityAnchorsSummary =
        AcceptedScriptPacketContinuityAnchorsSummary(),
    val remainingResidualRisks: List<String> = emptyList(),
    val reviewChecklist: List<StoryboardPacketReviewChecklistItem> = emptyList(),
    val downstreamBoundary: String = "reviewable_storyboard_packet_packaging_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ReviewableStoryboardPacketFinding> = emptyList(),
    val evidence: ReviewableStoryboardPacketEvidence = ReviewableStoryboardPacketEvidence(),
)

@Serializable
data class ReviewableStoryboardSourceVerdicts(
    val storyboardPacketReviewVerdict: String = "",
    val storyboardPacketReviewStatus: String = "",
    val storyboardHumanReviewPreflightVerdict: String = "",
    val storyboardStoryMovementAuditVerdict: String = "",
)

@Serializable
data class ReviewableStoryboardMovementEvidenceSummary(
    val frameCount: Int = 0,
    val movementEvidenceCount: Int = 0,
    val openingPressureEvidenceCount: Int = 0,
    val actionEvidenceCount: Int = 0,
    val consequenceOrChoiceEvidenceCount: Int = 0,
    val coveredContinuityAnchorCount: Int = 0,
)

@Serializable
data class ReviewableStoryboardHumanReviewReadinessSummary(
    val reviewStatus: String = "",
    val reviewChecklistCount: Int = 0,
    val residualRiskCount: Int = 0,
    val packetStatus: String = "reviewable_storyboard_packet_candidate",
)

@Serializable
data class ReviewableStoryboardFrameSummary(
    val frameId: String = "",
    val sceneReference: String = "",
    val beatReference: String = "",
    val visualIntent: String = "",
    val cameraIntent: String = "",
    val mustShow: List<String> = emptyList(),
    val doNotShow: List<String> = emptyList(),
    val continuityAnchors: List<String> = emptyList(),
    val assetCandidateRefs: List<String> = emptyList(),
)

@Serializable
data class ReviewableStoryboardPacketFinding(
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

@Serializable
data class ReviewableStoryboardPacketEvidence(
    val sourceBindingFieldCount: Int = 0,
    val frameCount: Int = 0,
    val uniqueFrameIdCount: Int = 0,
    val movementEvidenceCount: Int = 0,
    val protectedContinuityAnchorCount: Int = 0,
    val coveredContinuityAnchorCount: Int = 0,
    val residualRiskCount: Int = 0,
    val reviewChecklistCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
)

@Serializable
data class ImageFacingDeliveryProof(
    val verdict: String,
    val proofId: String = "",
    val proofStatus: String = "FAIL",
    val reviewableStoryboardPacketFile: String = "",
    val reviewableStoryboardPacketId: String = "",
    val reviewableStoryboardPacketStatus: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val sourceVerdict: String = "",
    val sourceNextAllowedDownstreamStep: String = "",
    val frameDeliveryCards: List<ImageFacingDeliveryFrameCard> = emptyList(),
    val downstreamBoundary: String = "image_facing_delivery_proof_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ImageFacingDeliveryProofFinding> = emptyList(),
    val evidence: ImageFacingDeliveryProofEvidence = ImageFacingDeliveryProofEvidence(),
)

@Serializable
data class ImageFacingDeliveryFrameCard(
    val frameId: String = "",
    val sceneReference: String = "",
    val beatReference: String = "",
    val visualIntent: String = "",
    val cameraIntent: String = "",
    val mustShow: List<String> = emptyList(),
    val doNotShow: List<String> = emptyList(),
    val continuityAnchors: List<String> = emptyList(),
    val assetCandidateRefs: List<String> = emptyList(),
    val deliveryStatus: String = "image_facing_candidate_only",
)

@Serializable
data class ImageFacingDeliveryProofFinding(
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

@Serializable
data class ImageFacingDeliveryProofEvidence(
    val sourceBindingFieldCount: Int = 0,
    val frameCount: Int = 0,
    val uniqueFrameIdCount: Int = 0,
    val frameWithVisualIntentCount: Int = 0,
    val frameWithCameraIntentCount: Int = 0,
    val frameWithMustShowCount: Int = 0,
    val frameWithDoNotShowCount: Int = 0,
    val frameWithContinuityAnchorCount: Int = 0,
    val assetCandidateRefCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val remoteReferenceCount: Int = 0,
)
