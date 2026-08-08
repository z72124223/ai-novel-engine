package com.localnovel.engine

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.CharacterProfile
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeliveryMode
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.NarrativeDeliveryExpectation
import com.localnovel.engine.model.Project
import com.localnovel.engine.model.Scene
import com.localnovel.engine.model.StateDelta
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.service.NarrativeDeliveryResolver
import com.localnovel.engine.service.NarrativeQualityAuditService
import com.localnovel.engine.service.NarrativeQualityGateService
import com.localnovel.engine.service.ProvisionalLayerCharterImporter
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Mvp8NarrativeDeliveryTest {
    @Test
    fun readerModePrepareTurnSetsPlainOralDelivery() {
        val repository = seededRepositoryWithThinZones()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning", readerMode = true)
        val context = runtime.prepareTurn(session.id, "我先看看四周。")

        assertTrue(context.readerMode)
        assertEquals(DeliveryMode.PLAIN_ORAL, context.outputRequirements.deliveryMode)
        assertEquals(DeliveryMode.PLAIN_ORAL, context.narrativeDelivery.deliveryMode)
        assertTrue(context.narrativeDelivery.requiredExposition.isEmpty())
    }

    @Test
    fun chenxingOpeningTurnOneResolvesTideCheckBeat() {
        val context = chenxingOpeningContext(turnNumber = 1)
        val resolved = NarrativeDeliveryResolver.resolve(context, readerMode = true)

        assertEquals("shot-01-tide-check", resolved.narrativeDelivery.activeBeatId)
        assertEquals("查潮汐", resolved.narrativeDelivery.activeBeatTitle)
        assertTrue(resolved.narrativeDelivery.beatKeywords.contains("潮汐"))
        assertTrue(resolved.narrativeDelivery.requiredExposition.contains("WHO"))
        assertTrue(resolved.oralLineCandidates.any { it.lineType == "dialogue" && it.line.contains("潮水還沒漲上來") })
    }

    @Test
    fun chenxingOpeningTurnThreeResolvesHillsideRetreatBeat() {
        val context = chenxingOpeningContext(turnNumber = 3)
        val resolved = NarrativeDeliveryResolver.resolve(context, readerMode = true)

        assertEquals("shot-09-hillside-retreat", resolved.narrativeDelivery.activeBeatId)
        assertEquals("躲入山坡廢墟", resolved.narrativeDelivery.activeBeatTitle)
    }

    @Test
    fun auditFlagsSubstanceFloorForAtmosphereOnlyOpening() {
        val context = chenxingOpeningContext(turnNumber = 1).let {
            NarrativeDeliveryResolver.resolve(it, readerMode = true)
        }
        val result = deliveryResult(
            context = context,
            narrative = "風裡有鹹澀的味道。天色又沉了一分，彷彿整個世界無聲地宣告著什麼。",
            includeActions = false,
        )

        val ruleIds = NarrativeQualityAuditService().audit(context, result).findings.map { it.ruleId }.toSet()

        assertTrue("NARRATIVE_SUBSTANCE_FLOOR" in ruleIds)
        assertTrue("NARRATIVE_UNSHOOTABLE_FILLER" in ruleIds)
    }

    @Test
    fun auditAcceptsPlainSubstantiveOpeningNarrative() {
        val context = chenxingOpeningContext(turnNumber = 1).let {
            NarrativeDeliveryResolver.resolve(it, readerMode = true)
        }
        val result = deliveryResult(
            context = context,
            narrative = "2026年7月，颱風巴威還在海上。陸塵行站在基隆八斗子大坪海岸的潮間帶，愛醬跟在身邊。他打開潮汐紀錄，又對過手機上的天氣提醒，估計還有兩小時。他低聲說：「還來得及，潮水還沒漲上來。」愛醬豎耳望向海面。",
        )

        val deliveryFindings = NarrativeQualityAuditService().audit(context, result).findings
            .filter { it.category == "narrative_delivery" && it.severity == "MAJOR" }

        assertTrue(deliveryFindings.isEmpty())
    }

    @Test
    fun auditRejectsJargonDialogueNotFromOralCandidates() {
        val context = chenxingOpeningContext(turnNumber = 1).let {
            NarrativeDeliveryResolver.resolve(it, readerMode = true)
        }
        val result = deliveryResult(
            context = context,
            narrative = "2026年7月，陸塵行站在大坪海岸潮間帶查潮汐。他低聲說：「還來得及，退潮窗口還在。」",
        )

        val ruleIds = NarrativeQualityAuditService().audit(context, result).findings.map { it.ruleId }.toSet()

        assertTrue("DIALOGUE_MUST_USE_ORAL_CANDIDATES" in ruleIds)
    }

    @Test
    fun auditFlagsBeatMismatchWhenTurnDoesNotLandActiveBeat() {
        val context = chenxingOpeningContext(turnNumber = 2).let {
            NarrativeDeliveryResolver.resolve(it, readerMode = true)
        }
        val result = deliveryResult(
            context = context,
            narrative = "陸塵行站在祠堂外廊，只問趙無咎能不能先退到門口。",
        )

        val ruleIds = NarrativeQualityAuditService().audit(context, result).findings.map { it.ruleId }.toSet()

        assertTrue("NARRATIVE_BEAT_MISMATCH" in ruleIds)
    }

    @Test
    fun gateTreatsMajorDeliveryFindingsAsAdvisoryUnlessStrict() {
        val base = chenxingOpeningContext(turnNumber = 1)
        val context = base.copy(
            narrativeDelivery = NarrativeDeliveryExpectation(
                deliveryMode = DeliveryMode.PLAIN_ORAL,
                activeBeatId = "shot-01-tide-check",
                activeBeatTitle = "查潮汐",
                beatKeywords = listOf("潮汐", "退潮"),
                requiredExposition = listOf("WHO", "WHERE", "WHY", "EVENT"),
            ),
            outputRequirements = base.outputRequirements.copy(deliveryMode = DeliveryMode.PLAIN_ORAL),
        )
        val result = deliveryResult(
            context = context,
            narrative = "風裡有鹹澀的味道，天色又沉了一分。",
            includeActions = false,
        )

        val gateResult = NarrativeQualityGateService().evaluate(context, result)
        val strictGateResult = NarrativeQualityGateService().evaluate(
            context,
            result,
            com.localnovel.engine.service.NarrativeQualityGateOptions(strictMode = true),
        )

        assertTrue(gateResult.advisoryFindings.any { it.ruleId == "NARRATIVE_SUBSTANCE_FLOOR" })
        assertTrue(strictGateResult.blockingFindings.any { it.ruleId == "NARRATIVE_SUBSTANCE_FLOOR" })
    }

    @Test
    fun auditRejectsCompanionCommandDialogue() {
        val context = chenxingOpeningContext(turnNumber = 3)
            .copy(
                characters = chenxingOpeningContext(turnNumber = 3).characters + CharacterProfile(
                    id = "char-aijiang",
                    projectId = "chenxing",
                    name = "愛醬",
                    role = "co-protagonist",
                    description = "長毛哈士奇，陸塵行的家人同伴。",
                ),
            )
            .let { NarrativeDeliveryResolver.resolve(it, readerMode = true) }
        val result = deliveryResult(
            context = context,
            narrative = "愛醬已貼在他身側。陸塵行喘而穩地說：「愛醬，跟我走。上面那棟……至少能擋風。」",
        )

        val ruleIds = NarrativeQualityAuditService().audit(context, result).findings.map { it.ruleId }.toSet()

        assertTrue("COMPANION_COMMAND_DIALOGUE_INCONGRUENT" in ruleIds)
    }

    @Test
    fun auditRejectsDialogueWithoutSpeakerAttribution() {
        val context = chenxingOpeningContext(turnNumber = 2).let {
            NarrativeDeliveryResolver.resolve(it, readerMode = true)
        }
        val result = deliveryResult(
            context = context,
            narrative = "海水比表上漲得快。「不對——這不是平常漲潮。」愛醬貼近他小腿。",
        )

        val ruleIds = NarrativeQualityAuditService().audit(context, result).findings.map { it.ruleId }.toSet()

        assertTrue("DIALOGUE_REQUIRES_SPEAKER_ATTRIBUTION" in ruleIds)
    }

    @Test
    fun auditFlagsHalfFinishedRumorForOralClarity() {
        val context = readJson<ContextPacket>(samplePath("sample_context_packet.json")).copy(
            narrativeDelivery = NarrativeDeliveryExpectation(
                deliveryMode = DeliveryMode.PLAIN_ORAL,
            ),
            outputRequirements = readJson<ContextPacket>(samplePath("sample_context_packet.json"))
                .outputRequirements
                .copy(deliveryMode = DeliveryMode.PLAIN_ORAL),
        )
        val result = deliveryResult(
            context = context,
            narrative = "碼頭有人說，最近夜裡會看見……",
        )

        val ruleIds = NarrativeQualityAuditService().audit(context, result).findings.map { it.ruleId }.toSet()

        assertTrue("NARRATIVE_ORAL_CLARITY" in ruleIds)
    }

    private fun chenxingOpeningContext(turnNumber: Int): ContextPacket =
        readJson<ContextPacket>(samplePath("sample_context_packet.json")).copy(
            project = Project(
                id = "chenxing",
                name = "塵行",
                summary = "塵行開場",
                activeSceneId = "scene-daping-coast-opening",
            ),
            turnNumber = turnNumber,
            activeScene = Scene(
                id = "scene-daping-coast-opening",
                projectId = "chenxing",
                title = "大坪海岸開場",
                summary = "陸塵行在大坪海岸潮間帶查潮汐。",
            ),
            playerInput = if (turnNumber == 1) "查看潮汐表。" else "繼續行動。",
        )

    private fun deliveryResult(
        context: ContextPacket,
        narrative: String,
        includeActions: Boolean = true,
    ): AiTurnResult =
        AiTurnResult(
            protocolVersion = "2",
            projectId = context.project.id,
            turnId = context.turnId,
            resultId = "result-delivery-test",
            contextId = context.contextId,
            sessionId = context.sessionId,
            turnNumber = context.turnNumber,
            baseStateVersion = context.baseStateVersion,
            contextHash = context.contextHash,
            narrative = narrative,
            turnSummary = "交付測試。",
            characterActions = if (includeActions) {
                listOf(
                    CharacterAction(
                        characterId = "char-lu-chenxing",
                        intent = "推進當前 beat",
                        visibleAction = "陸塵行做出可見動作。",
                        hiddenMotive = "測試交付規則。",
                        characterFitScore = 80,
                        storyProgressScore = 60,
                    ),
                )
            } else {
                emptyList()
            },
            stateDelta = StateDelta(
                operations = listOf(
                    DeltaOperation(
                        type = DeltaOperationType.ADD_EVENT,
                        targetType = "event",
                        targetId = "event-delivery-test",
                        payload = JsonObject(mapOf("summary" to JsonPrimitive("交付測試事件。"))),
                    ),
                ),
            ),
        )

    private fun seededRepositoryWithThinZones(): SqliteStoryRepository {
        val repository = SqliteStoryRepository(Files.createTempDirectory("story-engine-mvp8-delivery-test").resolve("story.db"))
        repository.initialize()
        repository.seedDemo(readJson(samplePath("demo_project_seed.json")))
        ProvisionalLayerCharterImporter(repository).importCharterFile(
            projectId = "demo",
            charterPath = charterPath(),
        )
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

    private fun charterPath(): Path {
        val candidates = listOf(
            Path.of("world", "chenxing", "formal", "35_provisional_layer_charter_v1.json"),
            Path.of("..", "world", "chenxing", "formal", "35_provisional_layer_charter_v1.json"),
        )
        return candidates.firstOrNull { Files.exists(it) }
            ?: error("Cannot find charter file: 35_provisional_layer_charter_v1.json")
    }
}
