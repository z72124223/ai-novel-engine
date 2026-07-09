package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.NarrativeQualityAuditReport
import com.localnovel.engine.model.NarrativeQualityFinding
import com.localnovel.engine.model.ValidationError

data class NarrativeQualityGateOptions(
    val strictMode: Boolean = false,
    val enforceSceneContextFloor: Boolean = false,
    val sampleLabel: String = "",
    val evidenceText: String = "",
    val autoPromoteLearnedRules: Boolean = true,
)

data class ResolveTurnOptions(
    val acknowledgeNarrativeQualityWarnings: Boolean = false,
    val requireNarrativeQualityAcknowledgement: Boolean = false,
    val strictNarrativeQuality: Boolean = false,
    val narrativeQualityEvidenceText: String = "",
    val narrativeQualitySampleLabel: String = "",
    val autoPromoteLearnedRules: Boolean = true,
    val logicAuditFile: String = "",
    val dbPath: String = "",
)

data class NarrativeQualityGateResult(
    val auditReport: NarrativeQualityAuditReport,
    val blockingFindings: List<NarrativeQualityFinding>,
    val advisoryFindings: List<NarrativeQualityFinding>,
) {
    val hasBlockingFindings: Boolean = blockingFindings.isNotEmpty()
    val hasAdvisoryFindings: Boolean = advisoryFindings.isNotEmpty()
}

object NarrativeQualityGatePolicy {
    val blockingRuleIds: Set<String> = setOf(
        "KNOWLEDGE_FORBIDDEN_REVEAL",
        "KNOWLEDGE_UNKNOWN_FACT_USED",
        "KNOWLEDGE_UNKNOWN_CLAIM_USED",
        "KNOWLEDGE_MEMORY_NOT_RETRIEVED",
    )

    val hardBlockCategories: Set<String> = setOf(
        "ai_feeling",
        "style",
    )

    val hardBlockDeliveryRuleIds: Set<String> = setOf(
        "NARRATIVE_SUBSTANCE_FLOOR",
        "NARRATIVE_ORAL_CLARITY",
        "NARRATIVE_UNSHOOTABLE_FILLER",
        "NARRATIVE_BEAT_MISMATCH",
        "DIALOGUE_MUST_USE_ORAL_CANDIDATES",
    )

    val hardBlockContextQualityRuleIds: Set<String> = setOf(
        "CONTEXT_SETTING_TOO_THIN",
        "CONTEXT_TRANSITION_UNAVAILABLE",
    )
}

class NarrativeQualityGateService(
    private val auditService: NarrativeQualityAuditService = NarrativeQualityAuditService(),
) {
    fun evaluate(
        context: ContextPacket,
        result: AiTurnResult,
        options: NarrativeQualityGateOptions = NarrativeQualityGateOptions(),
        input: NarrativeQualityAuditInput = NarrativeQualityAuditInput(),
    ): NarrativeQualityGateResult =
        evaluateRun(
            context = context,
            result = result,
            options = options,
            input = input,
        ).gateResult ?: error("Narrative quality gate result missing.")

    fun evaluateRun(
        context: ContextPacket,
        result: AiTurnResult,
        options: NarrativeQualityGateOptions = NarrativeQualityGateOptions(),
        input: NarrativeQualityAuditInput = NarrativeQualityAuditInput(),
    ): NarrativeQualityAuditRunResult {
        val mergedInput = input.copy(
            sampleLabel = options.sampleLabel.ifBlank { input.sampleLabel },
            evidenceText = options.evidenceText.ifBlank { input.evidenceText },
        )
        val auditRun = NarrativeQualityAuditRunner.audit(
            context = context,
            result = result,
            input = mergedInput,
            autoPromoteLearnedRules = options.autoPromoteLearnedRules,
        )
        val gateResult = buildGateResult(auditRun.auditReport, options)
        return auditRun.copy(gateResult = gateResult)
    }

    private fun buildGateResult(
        auditReport: NarrativeQualityAuditReport,
        options: NarrativeQualityGateOptions,
    ): NarrativeQualityGateResult {
        val partitioned = auditReport.findings.partition {
            isBlocking(it, options.strictMode, options.enforceSceneContextFloor)
        }
        return NarrativeQualityGateResult(
            auditReport = auditReport,
            blockingFindings = partitioned.first,
            advisoryFindings = partitioned.second,
        )
    }

    fun toValidationErrors(gateResult: NarrativeQualityGateResult, advisoryRequiresAcknowledge: Boolean): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        gateResult.blockingFindings.forEach { finding ->
            val regenerateHint = if (
                finding.category in NarrativeQualityGatePolicy.hardBlockCategories ||
                finding.ruleId in NarrativeQualityGatePolicy.hardBlockContextQualityRuleIds ||
                finding.ruleId in NarrativeQualityGatePolicy.hardBlockDeliveryRuleIds
            ) {
                " 請依提示修正後重新生成，不可略過。"
            } else {
                ""
            }
            errors += ValidationError(
                code = "narrative_quality_gate",
                message = "${finding.ruleId} (${finding.severity}): ${finding.message}$regenerateHint",
            )
        }
        if (advisoryRequiresAcknowledge && gateResult.hasAdvisoryFindings) {
            val summary = gateResult.advisoryFindings.joinToString(separator = "; ") { it.ruleId }
            errors += ValidationError(
                code = "narrative_quality_gate",
                message = "Advisory narrative quality findings require --acknowledge-narrative-quality-warnings before resolve: $summary",
            )
        }
        return errors
    }

    private fun isBlocking(
        finding: NarrativeQualityFinding,
        strictMode: Boolean,
        enforceSceneContextFloor: Boolean,
    ): Boolean {
        if (finding.category in NarrativeQualityGatePolicy.hardBlockCategories) {
            return true
        }
        if (
            finding.ruleId in NarrativeQualityGatePolicy.hardBlockContextQualityRuleIds &&
            (finding.ruleId != "CONTEXT_SETTING_TOO_THIN" || enforceSceneContextFloor)
        ) {
            return true
        }
        if (
            finding.ruleId in NarrativeQualityGatePolicy.hardBlockDeliveryRuleIds &&
            finding.severity in setOf("MAJOR", "CRITICAL")
        ) {
            return true
        }
        if (finding.ruleId.startsWith("LEARNED-") && finding.severity in setOf("MAJOR", "CRITICAL")) {
            return true
        }
        if (finding.ruleId in NarrativeQualityGatePolicy.blockingRuleIds || finding.severity == "CRITICAL") {
            return true
        }
        return strictMode && finding.severity == "MAJOR"
    }
}