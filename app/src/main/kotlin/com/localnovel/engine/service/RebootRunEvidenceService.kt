package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.NarrativeQualityAuditReport
import com.localnovel.engine.model.NarrativeQualityFinding
import com.localnovel.engine.model.RebootRunEvidenceRecord
import com.localnovel.engine.model.RebootValidationClass
import com.localnovel.engine.model.RebootValidationEvidenceFinding
import com.localnovel.engine.model.ValidationError
import com.localnovel.engine.model.allDeltaOperations
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.support.JsonSupport
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object RebootValidationClassifier {
    private val protocolCodes = setOf(
        "unsupported_protocol",
        "project_mismatch",
        "turn_id_mismatch",
        "session_mismatch",
        "context_mismatch",
        "turn_mismatch",
        "state_version_mismatch",
        "hash_mismatch",
        "missing_result_id",
        "missing_turn_summary",
        "blank_narrative",
        "duplicate_result",
        "missing_session",
        "inactive_branch",
        "stale_state",
        "stale_turn",
    )

    fun fromValidationErrors(errors: List<ValidationError>): List<RebootValidationEvidenceFinding> =
        errors.map { error ->
            RebootValidationEvidenceFinding(
                code = error.code,
                classification = if (error.code in protocolCodes) {
                    RebootValidationClass.PROTOCOL_HARD_FAIL
                } else {
                    RebootValidationClass.WORLD_LEGALITY_HARD_FAIL
                },
                message = error.message,
            )
        }

    fun fromNarrativeFindings(findings: List<NarrativeQualityFinding>): List<RebootValidationEvidenceFinding> =
        findings.map { finding ->
            RebootValidationEvidenceFinding(
                code = finding.ruleId,
                classification = if (finding.ruleId in NarrativeQualityGatePolicy.blockingRuleIds) {
                    RebootValidationClass.WORLD_LEGALITY_HARD_FAIL
                } else {
                    RebootValidationClass.QUALITY_WARNING
                },
                message = finding.message,
                severity = finding.severity,
                suggestedFix = finding.suggestedFix,
            )
        }

    fun primary(findings: List<RebootValidationEvidenceFinding>): RebootValidationClass {
        val classes = findings.map { it.classification }.toSet()
        return when {
            RebootValidationClass.PROTOCOL_HARD_FAIL in classes -> RebootValidationClass.PROTOCOL_HARD_FAIL
            RebootValidationClass.WORLD_LEGALITY_HARD_FAIL in classes -> RebootValidationClass.WORLD_LEGALITY_HARD_FAIL
            RebootValidationClass.IRREVERSIBLE_APPROVAL_REQUIRED in classes -> RebootValidationClass.IRREVERSIBLE_APPROVAL_REQUIRED
            RebootValidationClass.QUALITY_WARNING in classes -> RebootValidationClass.QUALITY_WARNING
            else -> RebootValidationClass.PASS
        }
    }
}

object IrreversibleChangeBoundaryValidator {
    private val allowedChangeTypes = setOf(
        "relationship_obligation",
        "permanent_injury",
        "permanent_loss",
        "faction_commitment",
        "world_state_lock",
    )
    private val humanActorPattern = Regex("^human:[A-Za-z0-9][A-Za-z0-9._-]*$")
    private val protectedFlagPrefixes = setOf(
        "relationship",
        "obligation",
        "faction",
        "permanent",
        "irreversible",
        "world_lock",
        "canon_lock",
        "dead",
        "deceased",
    )
    private val permanentHealthMarkers = setOf(
        "dead",
        "deceased",
        "permanent",
        "amputat",
        "blind",
        "paraly",
        "死亡",
        "永久",
        "截肢",
        "失明",
        "癱瘓",
        "残疾",
        "殘疾",
    )

    private data class Requirement(
        val changeType: String,
        val targetId: String,
        val affectedEntityId: String,
        val reason: String,
    )

    fun validate(
        context: ContextPacket,
        result: AiTurnResult,
        options: ResolveTurnOptions,
    ): List<RebootValidationEvidenceFinding> {
        val contract = options.irreversibleChangeContract
        val approval = options.irreversibleApprovalArtifact
        val requirements = detectRequirements(context, result)
        if (contract == null) {
            return if (requirements.isNotEmpty() || approval != null) {
                listOf(
                    RebootValidationEvidenceFinding(
                        code = "irreversible_contract_required",
                        classification = RebootValidationClass.IRREVERSIBLE_APPROVAL_REQUIRED,
                        message = "Engine-detected irreversible change requires a bound contract: " +
                            requirements.joinToString { it.reason }.ifBlank { "approval artifact supplied without contract" },
                    ),
                )
            } else {
                emptyList()
            }
        }
        val findings = mutableListOf<RebootValidationEvidenceFinding>()
        if (
            contract.changeId.isBlank() ||
            contract.summary.isBlank() ||
            contract.changeTypes.isEmpty() ||
            contract.changeTypes.any { it !in allowedChangeTypes } ||
            contract.changeTypes.distinct().size != contract.changeTypes.size
        ) {
            findings += RebootValidationEvidenceFinding(
                code = "invalid_irreversible_change",
                classification = RebootValidationClass.WORLD_LEGALITY_HARD_FAIL,
                message = "Irreversible contract requires a changeId, summary, and unique allowlisted changeTypes.",
            )
        }
        if (contract.contextHash != context.contextHash || contract.resultId != result.resultId) {
            findings += RebootValidationEvidenceFinding(
                code = "irreversible_contract_identity_mismatch",
                classification = RebootValidationClass.PROTOCOL_HARD_FAIL,
                message = "Irreversible contract must bind the current contextHash and resultId.",
            )
        }
        if (contract.affectedEntityIds.isEmpty() || contract.requiredStateDeltaTargetIds.isEmpty()) {
            findings += RebootValidationEvidenceFinding(
                code = "irreversible_change_evidence_missing",
                classification = RebootValidationClass.WORLD_LEGALITY_HARD_FAIL,
                message = "Irreversible change requires affected entities and explicit state delta target evidence.",
            )
        }
        val deltaTargetIds = result.allDeltaOperations().mapNotNull { it.targetId }.toSet()
        val missingTargets = contract.requiredStateDeltaTargetIds.filterNot { it in deltaTargetIds }
        if (missingTargets.isNotEmpty()) {
            findings += RebootValidationEvidenceFinding(
                code = "irreversible_change_delta_missing",
                classification = RebootValidationClass.WORLD_LEGALITY_HARD_FAIL,
                message = "Irreversible change references missing state delta targets: ${missingTargets.joinToString()}",
            )
        }
        val uncoveredTargets = requirements.map { it.targetId }.filterNot { it in contract.requiredStateDeltaTargetIds }.distinct()
        val uncoveredEntities = requirements.map { it.affectedEntityId }.filterNot { it in contract.affectedEntityIds }.distinct()
        if (uncoveredTargets.isNotEmpty() || uncoveredEntities.isNotEmpty()) {
            findings += RebootValidationEvidenceFinding(
                code = "irreversible_contract_scope_incomplete",
                classification = RebootValidationClass.WORLD_LEGALITY_HARD_FAIL,
                message = "Contract does not cover engine-detected targets/entities: " +
                    "targets=${uncoveredTargets.joinToString()} entities=${uncoveredEntities.joinToString()}",
            )
        }
        val detectedTypes = requirements.map { it.changeType }.toSet()
        if (detectedTypes.isNotEmpty() && contract.changeTypes.toSet() != detectedTypes) {
            findings += RebootValidationEvidenceFinding(
                code = "irreversible_contract_type_mismatch",
                classification = RebootValidationClass.WORLD_LEGALITY_HARD_FAIL,
                message = "Contract types '${contract.changeTypes.joinToString()}' do not exactly match engine-detected types: ${detectedTypes.joinToString()}.",
            )
        }
        if (approval == null) {
            findings += RebootValidationEvidenceFinding(
                code = "irreversible_approval_required",
                classification = RebootValidationClass.IRREVERSIBLE_APPROVAL_REQUIRED,
                message = "Irreversible change '${contract.changeId}' requires a separate approval artifact.",
            )
        } else {
            if (
                approval.approvalId.isBlank() ||
                runCatching { Instant.parse(approval.approvedAt) }.isFailure ||
                approval.decision != "APPROVE" ||
                !humanActorPattern.matches(approval.humanActorId)
            ) {
                findings += RebootValidationEvidenceFinding(
                    code = "invalid_irreversible_approval",
                    classification = RebootValidationClass.WORLD_LEGALITY_HARD_FAIL,
                    message = "Approval requires an id, timestamp, APPROVE decision, and human:<nonblank-id> actor.",
                )
            }
            if (
                approval.changeId != contract.changeId ||
                approval.contextHash != context.contextHash ||
                approval.resultId != result.resultId
            ) {
                findings += RebootValidationEvidenceFinding(
                    code = "irreversible_approval_identity_mismatch",
                    classification = RebootValidationClass.PROTOCOL_HARD_FAIL,
                    message = "Approval artifact must bind the contract changeId, current contextHash, and resultId.",
                )
            }
        }
        return findings
    }

    fun isApprovalValid(
        context: ContextPacket,
        result: AiTurnResult,
        options: ResolveTurnOptions,
    ): Boolean =
        options.irreversibleChangeContract != null &&
            options.irreversibleApprovalArtifact != null &&
            validate(context, result, options).isEmpty()

    private fun detectRequirements(context: ContextPacket, result: AiTurnResult): List<Requirement> =
        result.allDeltaOperations().flatMap { operation ->
            val targetId = operation.targetId.orEmpty()
            when (operation.type) {
                com.localnovel.engine.model.DeltaOperationType.UPDATE_CHARACTER_STATE -> {
                    val current = context.characterStates.firstOrNull { it.characterId == targetId }
                    buildList {
                        val nextHealth = (operation.payload["health"] as? JsonPrimitive)?.content
                        if (
                            nextHealth != null &&
                            nextHealth != current?.health &&
                            permanentHealthMarkers.any { marker -> nextHealth.lowercase().contains(marker) }
                        ) {
                            add(Requirement("permanent_injury", targetId, targetId, "health mutation:$targetId"))
                        }
                        val nextInventory = (operation.payload["inventory"] as? JsonArray)
                            ?.mapNotNull { (it as? JsonPrimitive)?.content }
                        if (nextInventory != null && current != null && current.inventory.any { it !in nextInventory }) {
                            add(Requirement("permanent_loss", targetId, targetId, "inventory removal:$targetId"))
                        }
                        val protectedFlags = (operation.payload["flags"] as? JsonObject)
                            ?.keys
                            .orEmpty()
                            .filter { key -> protectedFlagPrefixes.any { prefix -> key.lowercase().startsWith(prefix) } }
                        protectedFlags.forEach { key ->
                            val type = when {
                                key.lowercase().startsWith("relationship") || key.lowercase().startsWith("obligation") -> "relationship_obligation"
                                key.lowercase().startsWith("faction") -> "faction_commitment"
                                key.lowercase().startsWith("dead") || key.lowercase().startsWith("deceased") -> "permanent_injury"
                                else -> "world_state_lock"
                            }
                            add(Requirement(type, targetId, targetId, "protected flag:$key"))
                        }
                    }
                }
                com.localnovel.engine.model.DeltaOperationType.ADD_FACT -> {
                    val explicitLock = listOf("irreversible", "permanent", "locksWorldState")
                        .any { key -> (operation.payload[key] as? JsonPrimitive)?.content == "true" }
                    val protectedTarget = operation.targetType.orEmpty().lowercase() in setOf(
                        "world_lock",
                        "canon_lock",
                        "permanent_fact",
                    )
                    if (explicitLock || protectedTarget) {
                        listOf(Requirement("world_state_lock", targetId, targetId, "locked fact:$targetId"))
                    } else {
                        emptyList()
                    }
                }
                else -> emptyList()
            }
        }.distinct()
}

class RebootRunEvidenceService(private val repository: SqliteStoryRepository) {
    fun persistProtocolEnvelope(
        cliSessionId: String,
        rawResultText: String,
        parsedResult: AiTurnResult?,
        options: ResolveTurnOptions,
        code: String,
        message: String,
        startedAtNanos: Long,
    ): String {
        val finding = RebootValidationEvidenceFinding(
            code = code,
            classification = RebootValidationClass.PROTOCOL_HARD_FAIL,
            message = message,
        )
        val runId = "reboot-run-${UUID.randomUUID()}"
        repository.insertRebootRunEvidence(
            RebootRunEvidenceRecord(
                runId = runId,
                projectId = parsedResult?.projectId.orEmpty(),
                sessionId = cliSessionId,
                turnNumber = parsedResult?.turnNumber ?: 0,
                scenarioSeed = options.scenarioSeed,
                contextId = parsedResult?.contextId.orEmpty(),
                contextHash = parsedResult?.contextHash.orEmpty(),
                playerInput = "",
                contextSnapshotJson = "{}",
                resultId = parsedResult?.resultId.orEmpty(),
                rawDraft = parsedResult?.narrative.orEmpty(),
                rawResultJson = rawResultText,
                validationFindingsJson = JsonSupport.json.encodeToString(listOf(finding)),
                validationClassification = RebootValidationClass.PROTOCOL_HARD_FAIL,
                stateDeltaJson = parsedResult
                    ?.let { JsonSupport.json.encodeToString(it.allDeltaOperations()) }
                    ?: "[]",
                modelMetadata = options.modelMetadata,
                elapsedTimeMs = elapsedMillis(startedAtNanos),
                status = "REJECTED",
            ),
        )
        return runId
    }

    fun persistAttempt(
        context: ContextPacket,
        result: AiTurnResult,
        options: ResolveTurnOptions,
        auditReport: NarrativeQualityAuditReport?,
        findings: List<RebootValidationEvidenceFinding>,
        status: String,
        startedAtNanos: Long,
    ): String {
        val runId = "reboot-run-${UUID.randomUUID()}"
        val criticScores = auditReport?.let { report ->
            buildJsonObject {
                put("narrativeQualityScore", report.score)
                put("verdict", report.verdict)
                put("severity", report.severity)
                put("findingCount", report.findings.size)
            }
        } ?: JsonObject(emptyMap())
        val suggestedRevision = options.revisionSuggestion.ifBlank {
            findings.map { it.suggestedFix }.filter { it.isNotBlank() }.distinct().joinToString("\n")
        }
        val approvalValid = IrreversibleChangeBoundaryValidator.isApprovalValid(context, result, options)
        repository.insertRebootRunEvidence(
            RebootRunEvidenceRecord(
                runId = runId,
                projectId = context.project.id,
                sessionId = context.sessionId,
                turnNumber = context.turnNumber,
                scenarioSeed = options.scenarioSeed,
                contextId = context.contextId,
                contextHash = context.contextHash,
                playerInput = context.playerInput,
                contextSnapshotJson = JsonSupport.json.encodeToString(context),
                resultId = result.resultId,
                rawDraft = result.narrative,
                rawResultJson = JsonSupport.json.encodeToString(result),
                criticScoresJson = JsonSupport.json.encodeToString(criticScores),
                revisionSuggestion = suggestedRevision,
                revisedDraft = options.revisedDraft,
                validationFindingsJson = JsonSupport.json.encodeToString(findings),
                validationClassification = RebootValidationClassifier.primary(findings),
                stateDeltaJson = JsonSupport.json.encodeToString(result.allDeltaOperations()),
                irreversibleChangeJson = options.irreversibleChangeContract
                    ?.let { JsonSupport.json.encodeToString(it) }
                    ?: "{}",
                irreversibleApprovalJson = options.irreversibleApprovalArtifact
                    ?.let { JsonSupport.json.encodeToString(it) }
                    ?: "{}",
                irreversibleApprovalActor = options.irreversibleApprovalArtifact?.humanActorId.orEmpty(),
                irreversibleApprovalGranted = approvalValid,
                modelMetadata = options.modelMetadata,
                elapsedTimeMs = elapsedMillis(startedAtNanos),
                status = status,
            ),
        )
        return runId
    }

    fun markFailed(runId: String, error: Throwable, startedAtNanos: Long) {
        val payload = buildJsonObject {
            put("status", "APPLY_FAILED")
            put("errorType", error::class.simpleName.orEmpty())
            put("message", error.message.orEmpty())
        }
        repository.updateRebootRunEvidenceOutcome(
            runId = runId,
            status = "APPLY_FAILED",
            applyResultJson = JsonSupport.json.encodeToString(payload),
            elapsedTimeMs = elapsedMillis(startedAtNanos),
        )
    }

    fun elapsedMillis(startedAtNanos: Long): Long =
        ((System.nanoTime() - startedAtNanos) / 1_000_000).coerceAtLeast(0)
}

fun defaultRebootModelMetadata(): JsonObject = buildJsonObject {
    put("status", JsonPrimitive("NOT_AVAILABLE"))
}
