package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.NarrativeQualityAuditReport
import com.localnovel.engine.model.NarrativeRulePromotionReport
import java.nio.file.Path

data class NarrativeQualityAuditRunResult(
    val auditReport: NarrativeQualityAuditReport,
    val promotionReport: NarrativeRulePromotionReport? = null,
    val gateResult: NarrativeQualityGateResult? = null,
)

object NarrativeQualityAuditRunner {
    fun audit(
        context: ContextPacket,
        result: AiTurnResult,
        input: NarrativeQualityAuditInput,
        autoPromoteLearnedRules: Boolean,
    ): NarrativeQualityAuditRunResult {
        val auditService = NarrativeQualityAuditService()
        var report = auditService.audit(context, result, input)
        if (!autoPromoteLearnedRules || input.dbPath.isBlank()) {
            return NarrativeQualityAuditRunResult(auditReport = report)
        }

        val dbPath = Path.of(input.dbPath)
        val promotion = NarrativeRuleAutoPromoter.promote(
            projectId = context.project.id,
            dbPath = dbPath,
            findings = report.findings,
        )
        val promotionReport = NarrativeRulePromotionReport(
            projectId = context.project.id,
            promotedCount = promotion.promotedRules.size,
            skippedCount = promotion.skippedCount,
            promotedRuleIds = promotion.promotedRules.map { it.ruleId },
            storePath = NarrativeRuleLearningStore.storePath(dbPath, context.project.id).toString(),
        )
        if (promotion.promotedRules.isNotEmpty()) {
            report = auditService.audit(context, result, input)
        }
        return NarrativeQualityAuditRunResult(
            auditReport = report,
            promotionReport = promotionReport,
        )
    }
}