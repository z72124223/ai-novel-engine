package com.localnovel.engine.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GrokStoryHandoff(
    val handoffId: String = "",
    val projectId: String = "",
    val title: String = "",
    val locale: String = "",
    val sourceOwner: String = "grok_build",
    val sourceLabel: String = "",
    val storyStatus: String = "draft_for_codex_support",
    val storyChunks: List<GrokStoryChunk> = emptyList(),
    val productionRequests: List<GrokProductionRequest> = emptyList(),
    val referenceRequirements: List<GrokReferenceRequirement> = emptyList(),
    val returnToGrok: List<ReturnToGrokRequest> = emptyList(),
    val boundaries: GrokCodexBoundaryFlags = GrokCodexBoundaryFlags(),
)

@Serializable
data class GrokStoryChunk(
    val chunkId: String = "",
    val sceneId: String = "",
    val title: String = "",
    val location: String = "",
    val characterIds: List<String> = emptyList(),
    val textForCodex: String = "",
    val safeSummary: String = "",
    val visualIntent: String = "",
    val audioIntent: String = "",
    val continuityNotes: List<String> = emptyList(),
)

@Serializable
data class GrokProductionRequest(
    val requestId: String = "",
    val targetChunkId: String = "",
    val requestType: String = "",
    val request: String = "",
)

@Serializable
data class GrokReferenceRequirement(
    val requirementId: String = "",
    val label: String = "",
    val path: String = "",
    val description: String = "",
    val requiredFor: List<String> = emptyList(),
)

@Serializable
data class ReturnToGrokRequest(
    val requestId: String = "",
    val source: String = "",
    val location: String = "",
    val reason: String = "",
    val neededFromGrok: String = "",
    val codexCanContinueAfter: String = "",
)

@Serializable
data class GrokCodexBoundaryFlags(
    val candidateOnly: Boolean = true,
    val noProviderApiCalls: Boolean = true,
    val noGeneratedMediaClaim: Boolean = true,
    val noCanonApproval: Boolean = true,
    val noAssetApproval: Boolean = true,
    val noProductionLock: Boolean = true,
    val noBlockedStoryRewriteByCodex: Boolean = true,
)

@Serializable
data class GrokStoryHandoffValidationReport(
    val verdict: String,
    val validationId: String = "",
    val handoffFile: String = "",
    val handoffId: String = "",
    val projectId: String = "",
    val title: String = "",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    val findingCount: Int = 0,
    val findings: List<GrokStoryHandoffFinding> = emptyList(),
    val evidence: GrokStoryHandoffValidationEvidence = GrokStoryHandoffValidationEvidence(),
)

@Serializable
data class GrokStoryHandoffFinding(
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
data class GrokStoryHandoffValidationEvidence(
    val storyChunkCount: Int = 0,
    val productionRequestCount: Int = 0,
    val referenceRequirementCount: Int = 0,
    val returnToGrokCount: Int = 0,
    val linkedProductionRequestCount: Int = 0,
    val crossReferenceMismatchCount: Int = 0,
    val unsafeBoundaryFlagCount: Int = 0,
)

@Serializable
data class CodexProductionHandoffDraft(
    val status: String,
    val draftId: String = "",
    val grokHandoffFile: String = "",
    val grokHandoffValidationFile: String = "",
    val sourceHandoffId: String = "",
    val projectId: String = "",
    val title: String = "",
    val sceneSummaries: List<CodexSceneSummary> = emptyList(),
    val shotCandidates: List<CodexShotCandidate> = emptyList(),
    val imagePromptDrafts: List<CodexImagePromptDraft> = emptyList(),
    val referenceChecks: List<CodexReferenceCheck> = emptyList(),
    val returnToGrok: List<CodexReturnToGrokBlock> = emptyList(),
    val boundaries: GrokCodexBoundaryFlags = GrokCodexBoundaryFlags(),
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    val findingCount: Int = 0,
    val findings: List<GrokStoryHandoffFinding> = emptyList(),
    val evidence: CodexProductionHandoffEvidence = CodexProductionHandoffEvidence(),
)

@Serializable
data class CodexSceneSummary(
    val summaryId: String = "",
    val sourceChunkId: String = "",
    val sceneId: String = "",
    val title: String = "",
    val location: String = "",
    val safeSummary: String = "",
    val continuityNotes: List<String> = emptyList(),
)

@Serializable
data class CodexShotCandidate(
    val shotId: String = "",
    val sourceChunkId: String = "",
    val sceneId: String = "",
    val title: String = "",
    val characterIds: List<String> = emptyList(),
    val visibleAction: String = "",
    val cameraIntent: String = "",
    val audioIntent: String = "",
    val candidateOnly: Boolean = true,
)

@Serializable
data class CodexImagePromptDraft(
    val promptId: String = "",
    val sourceShotId: String = "",
    val positivePromptDraft: String = "",
    val negativePromptDraft: String = "",
    val referenceRequirementIds: List<String> = emptyList(),
    val promptSafetyNotes: List<String> = emptyList(),
)

@Serializable
data class CodexReferenceCheck(
    val checkId: String = "",
    val requirementId: String = "",
    val label: String = "",
    val path: String = "",
    val description: String = "",
    val requiredFor: List<String> = emptyList(),
    val status: String = "candidate_reference_required",
)

@Serializable
data class CodexReturnToGrokBlock(
    val requestId: String = "",
    val source: String = "",
    val location: String = "",
    val reason: String = "",
    val neededFromGrok: String = "",
    val codexCanContinueAfter: String = "",
    val formattedBlock: String = "",
)

@Serializable
data class CodexProductionHandoffEvidence(
    val sourceValidationVerdict: String = "",
    val sceneSummaryCount: Int = 0,
    val shotCandidateCount: Int = 0,
    val imagePromptDraftCount: Int = 0,
    val referenceCheckCount: Int = 0,
    val returnToGrokCount: Int = 0,
)
