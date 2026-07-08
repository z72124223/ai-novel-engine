package com.localnovel.engine

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.StateDelta
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.service.NarrativeQualityGateService
import com.localnovel.engine.service.ResolveTurnOptions
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class Mvp7NarrativeQualityGateTest {
    @Test
    fun gateTreatsContextQualityAsBlocking() {
        val context = readJson<ContextPacket>(samplePath("sample_context_packet.json"))
        val result = readJson<AiTurnResult>(samplePath("sample_ai_turn_result.json"))
        val gateResult = NarrativeQualityGateService().evaluate(
            context = context,
            result = result,
            options = com.localnovel.engine.service.NarrativeQualityGateOptions(enforceSceneContextFloor = true),
        )
        val contextFindings = gateResult.auditReport.findings.filter { it.ruleId == "CONTEXT_SETTING_TOO_THIN" }

        if (contextFindings.isNotEmpty()) {
            assertTrue(gateResult.blockingFindings.any { it.ruleId == "CONTEXT_SETTING_TOO_THIN" })
        }
    }

    @Test
    fun gateTreatsForbiddenRevealAsBlocking() {
        val context = readJson<ContextPacket>(samplePath("sample_context_packet.json"))
        val result = AiTurnResult(
            projectId = context.project.id,
            turnId = context.turnId,
            narrative = "沈霜低聲說：祠堂下面鎮著一口會呼吸的黑棺。",
            characterActions = listOf(
                CharacterAction(
                    characterId = "char-shen-shuang",
                    intent = "測試",
                    visibleAction = "沈霜說話。",
                    hiddenMotive = "測試",
                    characterFitScore = 70,
                    storyProgressScore = 20,
                ),
            ),
        )

        val gateResult = NarrativeQualityGateService().evaluate(context, result)

        assertTrue(gateResult.blockingFindings.any { it.ruleId == "KNOWLEDGE_FORBIDDEN_REVEAL" })
    }

    @Test
    fun validateResultGateAllowsAdvisoryOnlyFindings() {
        val context = readJson<ContextPacket>(samplePath("sample_context_packet.json"))
        val result = readJson<AiTurnResult>(samplePath("sample_ai_turn_result.json"))
        val gateResult = NarrativeQualityGateService().evaluate(context, result)
        val errors = NarrativeQualityGateService().toValidationErrors(
            gateResult = gateResult,
            advisoryRequiresAcknowledge = false,
        )

        assertTrue(errors.isEmpty() || gateResult.blockingFindings.isNotEmpty())
        if (gateResult.blockingFindings.isEmpty()) {
            assertTrue(errors.isEmpty())
        }
    }

    @Test
    fun resolveTurnBlocksForbiddenRevealBeforeStateWrite() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我追問禁忌。")
        val beforeDeltas = repository.tableCount("state_deltas")
        val result = validMultiTurnResult(context, "result-gate-forbidden").copy(
            narrative = "沈霜低聲說：祠堂下面鎮著一口會呼吸的黑棺。",
        )

        assertFailsWith<IllegalStateException> {
            runtime.resolveTurn(session.id, result)
        }
        assertEquals(beforeDeltas, repository.tableCount("state_deltas"))
    }

    @Test
    fun resolveTurnBlocksNotButReframePattern() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我觀察現場。")
        val result = validMultiTurnResult(context, "result-gate-not-but").copy(
            narrative = "這不是猶豫，而是警戒。沈霜仍擋在門前，沒有讓出半步。",
        )

        assertFailsWith<IllegalStateException> {
            runtime.resolveTurn(session.id, result)
        }
        assertEquals(0, repository.appliedTurnCount(session.id))
    }

    @Test
    fun resolveTurnBlocksAiFeelingEvenWithAcknowledgeFlag() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我觀察現場。")
        val result = validMultiTurnResult(context, "result-gate-ai-feeling").copy(
            narrative = "總之，這意味著氣氛變了，可以看出眾人都很緊張，因此氣氛更沉，所以沒人先動。",
            characterActions = listOf(
                CharacterAction(
                    characterId = "char-shen-shuang",
                    intent = "維持距離",
                    visibleAction = "沈霜站在原地。",
                    hiddenMotive = "避免衝突。",
                    characterFitScore = 72,
                    storyProgressScore = 20,
                ),
            ),
        )

        assertFailsWith<IllegalStateException> {
            runtime.resolveTurn(session.id, result)
        }
        assertFailsWith<IllegalStateException> {
            runtime.resolveTurn(
                session.id,
                result.copy(resultId = "result-gate-ai-feeling-ack"),
                ResolveTurnOptions(acknowledgeNarrativeQualityWarnings = true),
            )
        }
        assertEquals(0, repository.appliedTurnCount(session.id))
    }

    private fun seededRepository(): SqliteStoryRepository {
        val dbPath = Files.createTempDirectory("story-engine-gate-test").resolve("story.db")
        val repository = SqliteStoryRepository(dbPath)
        repository.initialize()
        repository.seedDemo(readJson(samplePath("demo_project_seed.json")))
        return repository
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

    private fun validMultiTurnResult(context: ContextPacket, resultId: String): AiTurnResult =
        AiTurnResult(
            protocolVersion = "2",
            projectId = context.project.id,
            turnId = context.turnId,
            resultId = resultId,
            contextId = context.contextId,
            sessionId = context.sessionId,
            turnNumber = context.turnNumber,
            baseStateVersion = context.baseStateVersion,
            contextHash = context.contextHash,
            narrative = "沈霜沒有順著禁忌說下去，只承認昨夜後殿門閂被人動過。",
            turnSummary = "沈霜承認後殿門閂異常，但沒有揭露禁忌。",
            characterActions = listOf(
                CharacterAction(
                    characterId = "char-shen-shuang",
                    intent = "守住後殿並控制回答邊界",
                    visibleAction = "沈霜擋住門，只承認門閂異常。",
                    hiddenMotive = "避免外人知道祠堂地底的禁忌。",
                    usedMemoryIds = listOf("memory-shen-shuang-vow"),
                    characterFitScore = 88,
                    storyProgressScore = 76,
                ),
            ),
            stateDelta = StateDelta(
                operations = listOf(
                    DeltaOperation(
                        type = DeltaOperationType.ADD_EVENT,
                        targetType = "event",
                        targetId = "event-gate-test",
                        payload = JsonObject(mapOf("summary" to JsonPrimitive("測試事件。"))),
                    ),
                ),
            ),
        )
}