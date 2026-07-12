package com.localnovel.engine.model

import kotlinx.serialization.Serializable

@Serializable
enum class ProductionRuleCategory {
    DIALOGUE,
    CHARACTER_VOICE,
    LANGUAGE_LOCALE,
    NARRATIVE_CAUSALITY,
    KNOWLEDGE_BOUNDARY,
    APPROVAL_LIFECYCLE,
    MEDIA_HANDOFF,
}

@Serializable
enum class ProductionRuleSeverity { INFO, WARNING, BLOCKER }

@Serializable
enum class ProductionRuleCode {
    UNNATURAL_SPOKEN_CHINESE,
    ESSAY_LIKE_DIALOGUE,
    SYMMETRIC_ARGUMENT_EXCHANGE,
    CHARACTER_VOICE_COLLAPSE,
    HIGH_PRESSURE_OVERLONG_LINE,
    FORCED_WIT_UNDER_DANGER,
    ACCIDENTAL_FLIRTATION_OR_DOUBLE_MEANING,
    REQUIRES_PERFORMANCE_TO_AVOID_MISREADING,
    DIALOGUE_ACTION_REDUNDANCY,
    EXPOSITION_AS_SPEECH,
    AMBIGUOUS_PRONOUN_OR_OBJECT,
    MEMORABLE_LINE_DENSITY_EXCEEDED,
    UNSUPPORTED_REGIONAL_TERM,
    TRANSLATION_LIKE_PHRASING,
}

@Serializable
data class ProductionRuleDefinition(
    val code: ProductionRuleCode,
    val category: ProductionRuleCategory,
    val defaultSeverity: ProductionRuleSeverity,
    val hardBlocker: Boolean,
    val description: String,
)

@Serializable
data class ProductionRuleSet(
    val ruleSetId: String,
    val version: String,
    val rules: List<ProductionRuleDefinition>,
    val requiredReviewerRoles: List<DialogueReviewerRole>,
    val hardBlockerCodes: List<ProductionRuleCode>,
    val contentSha256: String = "",
)

@Serializable
data class CharacterVoiceProfile(
    val profileId: String,
    val version: String,
    val characterId: String,
    val ruleSetId: String,
    val ruleSetVersion: String,
    val voiceRules: List<String>,
    val forbiddenPatterns: List<String>,
    val profileSha256: String = "",
)

@Serializable
data class LanguageProductionProfile(
    val profileId: String,
    val version: String,
    val ruleSetId: String,
    val ruleSetVersion: String,
    val language: String,
    val spokenRegister: String,
    val localizationPolicy: List<String>,
    val profileSha256: String = "",
)

@Serializable
enum class DialogueArtifactKind { INITIAL, REVISED }

@Serializable
enum class DialogueApprovalState {
    DRAFT,
    REVIEW_PENDING,
    REVISE_REQUIRED,
    REVISED_PENDING_REVIEW,
    SELECTABLE,
    HUMAN_DECISION_REQUIRED,
    SELECTED,
}

@Serializable
data class DialogueLine(
    val lineId: String,
    val sceneId: String,
    val speakerId: String,
    val text: String,
    val pressureLevel: String = "NORMAL",
    val shotIds: List<String> = emptyList(),
)

@Serializable
data class DialogueArtifact(
    val artifactId: String,
    val productionId: String,
    val artifactKind: DialogueArtifactKind,
    val parentArtifactSha256: String = "",
    val contentSha256: String,
    val ruleSetId: String,
    val ruleSetVersion: String,
    val ruleSetContentSha256: String = "",
    val characterVoiceProfileIds: List<String>,
    val characterVoiceProfileHashes: Map<String, String> = emptyMap(),
    val languageProfileId: String,
    val languageProfileSha256: String = "",
    val lines: List<DialogueLine>,
    val autoRevisionCount: Int,
    val boundary: String = "NON_CANON_PILOT",
    val sourceCommitSha: String,
)

@Serializable
data class DialogueArtifactHashPayload(
    val artifactId: String,
    val productionId: String,
    val artifactKind: DialogueArtifactKind,
    val parentArtifactSha256: String,
    val ruleSetId: String,
    val ruleSetVersion: String,
    val ruleSetContentSha256: String,
    val characterVoiceProfileIds: List<String>,
    val characterVoiceProfileHashes: Map<String, String>,
    val languageProfileId: String,
    val languageProfileSha256: String,
    val lines: List<DialogueLine>,
    val autoRevisionCount: Int,
    val boundary: String,
    val sourceCommitSha: String,
)

@Serializable
enum class DialogueReviewerRole {
    TAIWAN_SPOKEN_DIALOGUE,
    ACTOR_BREATH_AND_PERFORMANCE,
    PAN_CHINESE_COMPREHENSION,
}

@Serializable
enum class DialogueReviewVerdict { PASS, REVISE, HUMAN_DECISION_REQUIRED }

@Serializable
data class DialogueFinding(
    val findingId: String,
    val lineId: String,
    val exactQuote: String,
    val ruleCode: ProductionRuleCode,
    val severity: ProductionRuleSeverity,
    val explanation: String,
    val suggestedText: String = "",
    val requiresHumanApproval: Boolean = false,
)

@Serializable
data class DialogueLineReview(
    val lineId: String,
    val exactQuote: String,
    val findings: List<DialogueFinding> = emptyList(),
    val note: String = "",
)

@Serializable
data class DialogueReviewRun(
    val reviewId: String,
    val artifactId: String,
    val artifactSha256: String,
    val ruleSetId: String,
    val ruleSetVersion: String,
    val reviewerRole: DialogueReviewerRole,
    val reviewerInstanceId: String,
    val lineReviews: List<DialogueLineReview>,
    val overallScore: Int,
    val verdict: DialogueReviewVerdict,
    val summary: String,
    val sourceCommitSha: String,
)

@Serializable
data class DialogueReviewBatch(
    val artifactId: String,
    val reviews: List<DialogueReviewRun>,
)

@Serializable
data class DialogueReviewHandoff(
    val artifact: DialogueArtifact,
    val ruleSet: ProductionRuleSet,
    val characterVoiceProfiles: List<CharacterVoiceProfile>,
    val languageProfile: LanguageProductionProfile,
    val requiredReviewerRoles: List<DialogueReviewerRole>,
    val hardBlockerCodes: List<ProductionRuleCode>,
    val deterministicFindings: List<DialogueFinding>,
    val instruction: String,
)

@Serializable
data class DialogueSelectionRequest(
    val decisionId: String,
    val artifactId: String,
    val decisionActorId: String,
    val sourceCommitSha: String,
)

@Serializable
enum class DialogueSelectionStatus { SELECTED, NOT_SELECTABLE, HUMAN_DECISION_REQUIRED }

@Serializable
data class DialogueSelectionDecision(
    val decisionId: String,
    val productionId: String,
    val artifactId: String,
    val selectedArtifactSha256: String,
    val ruleSetId: String,
    val ruleSetVersion: String,
    val status: DialogueSelectionStatus,
    val blockerCount: Int,
    val warningCount: Int,
    val reasons: List<String>,
    val decisionActorId: String,
    val sourceCommitSha: String,
    val decisionSha256: String = "",
)

@Serializable
data class DialogueEvaluationRequest(val artifact: DialogueArtifact)

@Serializable
data class DialogueFinalizeRequest(
    val reviewBatch: DialogueReviewBatch,
    val selection: DialogueSelectionRequest,
)

@Serializable
data class DialogueArtifactView(
    val artifact: DialogueArtifact,
    val approvalState: DialogueApprovalState,
    val reviews: List<DialogueReviewRun>,
    val findings: List<DialogueFinding>,
    val decisions: List<DialogueSelectionDecision>,
)
