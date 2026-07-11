package com.localnovel.engine

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeliveryMode
import com.localnovel.engine.model.NarrativeDeliveryExpectation
import com.localnovel.engine.model.NarrativeQualityFinding
import com.localnovel.engine.model.Project
import com.localnovel.engine.model.Scene
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.NarrativeQualityAuditInput
import com.localnovel.engine.service.NarrativeQualityAuditRunner
import com.localnovel.engine.service.NarrativeQualityAuditService
import com.localnovel.engine.service.NarrativeQualityGateService
import com.localnovel.engine.service.NarrativeRuleLearningStore
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NarrativeRuleLearningTest {
    @Test
    fun autoPromoterExtractsQuotedPhraseFromFindingEvidence() {
        val repository = tempRepository()
        val context = testContext()
        val result = testResult(
            context = context,
            narrative = "陸塵行低聲說：「還來得及，退潮窗口還在。」",
        )
        val findings = listOf(
            NarrativeQualityFinding(
                ruleId = "LOGIC_ORAL_JARGON",
                category = "logic",
                severity = "MAJOR",
                message = "Dialogue uses technical jargon.",
                evidence = listOf("quote=退潮窗口"),
                suggestedFix = "Use plain speech from oral candidates.",
            ),
        )

        val auditRun = NarrativeQualityAuditRunner.audit(
            context = context,
            result = result,
            input = NarrativeQualityAuditInput(
                dbPath = repository.databasePath().toString(),
                logicAuditFindings = findings,
            ),
            autoPromoteLearnedRules = true,
        )

        assertEquals(1, auditRun.promotionReport?.promotedCount)
        val pack = NarrativeRuleLearningStore.load("chenxing", repository.databasePath())
        assertTrue(pack.rules.any { it.pattern == "退潮窗口" })
        assertTrue(
            auditRun.auditReport.findings.any {
                it.ruleId.startsWith("LEARNED-") && "退潮窗口" in it.evidence
            },
        )
    }

    @Test
    fun learnedRuleWarnsByDefaultAndBlocksInStrictMode() {
        val repository = tempRepository()
        val context = testContext()
        val result = testResult(
            context = context,
            narrative = "他說：「退潮窗口還在。」",
        )
        NarrativeRuleLearningStore.promotePatterns(
            projectId = "chenxing",
            dbPath = repository.databasePath(),
            entries = listOf(
                NarrativeRuleLearningStore.PromotionEntry(
                    pattern = "退潮窗口",
                    category = "logic",
                    severity = "MAJOR",
                    message = "Learned block: narrative contains '退潮窗口'.",
                    suggestedFix = "Replace with plain speech.",
                    sourceRuleId = "LOGIC_ORAL_JARGON",
                    sourceEvidence = "退潮窗口",
                ),
            ),
        )

        val findings = NarrativeQualityAuditService().audit(
            context = context,
            result = result,
            input = NarrativeQualityAuditInput(dbPath = repository.databasePath().toString()),
        ).findings

        assertTrue(findings.any { it.ruleId.startsWith("LEARNED-") })
        val gateResult = NarrativeQualityGateService().evaluate(
            context = context,
            result = result,
            input = NarrativeQualityAuditInput(dbPath = repository.databasePath().toString()),
        )
        val strictGateResult = NarrativeQualityGateService().evaluate(
            context = context,
            result = result,
            options = com.localnovel.engine.service.NarrativeQualityGateOptions(strictMode = true),
            input = NarrativeQualityAuditInput(dbPath = repository.databasePath().toString()),
        )
        assertTrue(gateResult.advisoryFindings.any { it.ruleId.startsWith("LEARNED-") })
        assertTrue(strictGateResult.blockingFindings.any { it.ruleId.startsWith("LEARNED-") })
    }

    @Test
    fun autoPromoteSkipsWhenDisabled() {
        val repository = tempRepository()
        val context = testContext()
        val result = testResult(
            context = context,
            narrative = "他說：「退潮窗口還在。」",
        )
        val findings = listOf(
            NarrativeQualityFinding(
                ruleId = "LOGIC_ORAL_JARGON",
                category = "logic",
                severity = "MAJOR",
                message = "Dialogue uses technical jargon.",
                evidence = listOf("quote=退潮窗口"),
                suggestedFix = "Replace with plain speech.",
            ),
        )

        val auditRun = NarrativeQualityAuditRunner.audit(
            context = context,
            result = result,
            input = NarrativeQualityAuditInput(
                dbPath = repository.databasePath().toString(),
                logicAuditFindings = findings,
            ),
            autoPromoteLearnedRules = false,
        )

        assertEquals(null, auditRun.promotionReport)
        val pack = NarrativeRuleLearningStore.load("chenxing", repository.databasePath())
        assertTrue(pack.rules.isEmpty())
    }

    private fun tempRepository(): SqliteStoryRepository {
        val repository = SqliteStoryRepository(
            Files.createTempDirectory("story-engine-learned-rules-test").resolve("story.db"),
        )
        repository.initialize()
        return repository
    }

    private fun testContext(): ContextPacket =
        readJson<ContextPacket>(samplePath("sample_context_packet.json")).copy(
            project = Project(
                id = "chenxing",
                name = "塵行",
                summary = "塵行開場",
                activeSceneId = "scene-daping-coast-opening",
            ),
            turnNumber = 1,
            activeScene = Scene(
                id = "scene-daping-coast-opening",
                projectId = "chenxing",
                title = "大坪海岸開場",
                summary = "陸塵行在大坪海岸潮間帶查潮汐。",
            ),
            narrativeDelivery = NarrativeDeliveryExpectation(
                deliveryMode = DeliveryMode.PLAIN_ORAL,
                activeBeatId = "shot-01-tide-check",
            ),
        )

    private fun testResult(context: ContextPacket, narrative: String): AiTurnResult =
        AiTurnResult(
            protocolVersion = "2",
            projectId = context.project.id,
            turnId = context.turnId,
            resultId = "result-learned-rule-test",
            contextId = context.contextId,
            sessionId = context.sessionId,
            turnNumber = context.turnNumber,
            baseStateVersion = context.baseStateVersion,
            contextHash = context.contextHash,
            narrative = narrative,
            turnSummary = "學習規則測試。",
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private fun samplePath(fileName: String): Path {
        val candidates = listOf(
            Path.of("samples", fileName),
            Path.of("..", "samples", fileName),
        )
        return candidates.firstOrNull { Files.exists(it) } ?: error("Cannot find sample file: $fileName")
    }
}
