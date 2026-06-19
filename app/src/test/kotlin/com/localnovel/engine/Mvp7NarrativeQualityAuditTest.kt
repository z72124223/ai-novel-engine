package com.localnovel.engine

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.NarrativeQualityAuditReport
import com.localnovel.engine.model.StateDelta
import com.localnovel.engine.service.NarrativeQualityAuditInput
import com.localnovel.engine.service.NarrativeQualityAuditService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Mvp7NarrativeQualityAuditTest {
    @Test
    fun sampleContextAndResultProduceStableReport() {
        val contextPath = samplePath("sample_context_packet.json")
        val resultPath = samplePath("sample_ai_turn_result.json")
        val context = readJson<ContextPacket>(contextPath)
        val result = readJson<AiTurnResult>(resultPath)

        val report = NarrativeQualityAuditService().audit(
            context = context,
            result = result,
            input = NarrativeQualityAuditInput(
                contextFile = contextPath.toString(),
                resultFile = resultPath.toString(),
            ),
        )

        assertTrue(report.verdict in setOf("PASS", "PASS_WITH_WARNINGS", "NEEDS_REVIEW", "FAIL"))
        assertTrue(report.score in 0..100)
        assertEquals("demo", report.evidence.projectId)
        assertEquals("demo-turn-1", report.evidence.turnId)
        assertTrue(report.findings.any { it.ruleId == "NPC_LIVELINESS_MISSING_ACTION" })
    }

    @Test
    fun auditFlagsForbiddenRevealAndUnknownKnowledgeReference() {
        val context = readJson<ContextPacket>(samplePath("sample_context_packet.json"))
        val result = AiTurnResult(
            projectId = context.project.id,
            turnId = context.turnId,
            narrative = "沈霜低聲說：祠堂下面鎮著一口會呼吸的黑棺。",
            characterActions = listOf(
                CharacterAction(
                    characterId = "char-shen-shuang",
                    intent = "用不該知道的線索說服玩家退後",
                    visibleAction = "沈霜直接說出地底真相。",
                    hiddenMotive = "測試稽核能否擋下越界資訊。",
                    usedKnowledgeClaimIds = listOf("claim-not-in-context"),
                    characterFitScore = 40,
                    storyProgressScore = 20,
                ),
            ),
        )

        val report = NarrativeQualityAuditService().audit(context, result)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "KNOWLEDGE_FORBIDDEN_REVEAL" && it.severity == "CRITICAL" })
        assertTrue(report.findings.any { it.ruleId == "KNOWLEDGE_UNKNOWN_CLAIM_USED" })
    }

    @Test
    fun auditFlagsAiFeelingAndMissingConsequences() {
        val context = readJson<ContextPacket>(samplePath("sample_context_packet.json"))
        val result = AiTurnResult(
            projectId = context.project.id,
            turnId = context.turnId,
            narrative = "總之，這意味著她非常害怕，也非常焦慮。不是選擇，而是命運；既是天地深處的回聲，也是萬物無聲地宣告。",
            characterActions = listOf(
                CharacterAction(
                    characterId = "char-shen-shuang",
                    intent = "維持沉默",
                    visibleAction = "沈霜站在原地。",
                    hiddenMotive = "不讓玩家靠近。",
                    characterFitScore = 72,
                    storyProgressScore = 20,
                ),
            ),
        )

        val report = NarrativeQualityAuditService().audit(context, result)

        assertTrue(report.findings.any { it.ruleId == "AI_FEELING_OVER_SUMMARY" })
        assertTrue(report.findings.any { it.ruleId == "STYLE_PARALLELISM_STACKING" })
        assertTrue(report.findings.any { it.ruleId == "CONSEQUENCE_NO_STATE_EVIDENCE" })
    }

    @Test
    fun cliWritesNarrativeQualityReportWithoutDatabase() {
        val out = Files.createTempDirectory("narrative-quality-audit").resolve("report.json")

        val exit = Cli().run(
            listOf(
                "audit-narrative-quality",
                "--context",
                samplePath("sample_context_packet.json").toString(),
                "--result",
                samplePath("sample_ai_turn_result.json").toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<NarrativeQualityAuditReport>(out)
        assertTrue(report.evidence.contextFile.endsWith("sample_context_packet.json"))
        assertTrue(report.evidence.resultFile.endsWith("sample_ai_turn_result.json"))
        assertTrue(report.findings.isNotEmpty())
    }

    @Test
    fun auditTreatsRecordedStateDeltaAsConsequence() {
        val context = readJson<ContextPacket>(samplePath("sample_context_packet.json"))
        val result = AiTurnResult(
            projectId = context.project.id,
            turnId = context.turnId,
            narrative = "沈霜退了半步，仍守在外廊。",
            characterActions = listOf(
                CharacterAction(
                    characterId = "char-shen-shuang",
                    intent = "守住後殿",
                    visibleAction = "沈霜退半步後重新站定。",
                    hiddenMotive = "避免失言。",
                    characterFitScore = 82,
                    storyProgressScore = 42,
                ),
            ),
            stateDelta = StateDelta(
                operations = listOf(
                    DeltaOperation(
                        type = DeltaOperationType.ADD_EVENT,
                        targetType = "event",
                        targetId = "event-mvp7-audit",
                        payload = JsonObject(mapOf("summary" to JsonPrimitive("沈霜守住外廊。"))),
                    ),
                ),
            ),
        )

        val report = NarrativeQualityAuditService().audit(context, result)

        assertTrue(report.findings.none { it.ruleId == "CONSEQUENCE_NO_STATE_EVIDENCE" })
    }

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
