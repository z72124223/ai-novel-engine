package com.localnovel.engine.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
enum class RebootValidationClass {
    PASS,
    PROTOCOL_HARD_FAIL,
    WORLD_LEGALITY_HARD_FAIL,
    IRREVERSIBLE_APPROVAL_REQUIRED,
    QUALITY_WARNING,
}

@Serializable
data class RebootValidationEvidenceFinding(
    val code: String,
    val classification: RebootValidationClass,
    val message: String,
    val severity: String = "",
    val suggestedFix: String = "",
)

@Serializable
data class IrreversibleChangeContract(
    val changeId: String,
    val changeTypes: List<String>,
    val summary: String,
    val contextHash: String,
    val resultId: String,
    val affectedEntityIds: List<String> = emptyList(),
    val requiredStateDeltaTargetIds: List<String> = emptyList(),
)

@Serializable
data class IrreversibleApprovalArtifact(
    val approvalId: String,
    val changeId: String,
    val contextHash: String,
    val resultId: String,
    val humanActorId: String,
    val decision: String,
    val approvedAt: String,
)

@Serializable
data class RebootRunEvidenceRecord(
    val runId: String,
    val projectId: String,
    val sessionId: String,
    val turnNumber: Int,
    val scenarioSeed: String = "",
    val contextId: String,
    val contextHash: String = "",
    val playerInput: String = "",
    val contextSnapshotJson: String = "{}",
    val resultId: String,
    val rawDraft: String,
    val rawResultJson: String,
    val criticScoresJson: String = "{}",
    val revisionSuggestion: String = "",
    val revisedDraft: String = "",
    val validationFindingsJson: String = "[]",
    val validationClassification: RebootValidationClass = RebootValidationClass.PASS,
    val stateDeltaJson: String = "[]",
    val irreversibleChangeJson: String = "{}",
    val irreversibleApprovalJson: String = "{}",
    val irreversibleApprovalActor: String = "",
    val irreversibleApprovalGranted: Boolean = false,
    val applyResultJson: String = "{}",
    val modelMetadata: JsonObject = JsonObject(emptyMap()),
    val elapsedTimeMs: Long = 0,
    val status: String = "EVALUATED",
)
