package com.localnovel.engine

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.ProvisionalAgencyProfile
import com.localnovel.engine.model.ProvisionalEntityProposal
import com.localnovel.engine.model.ProvisionalPromotionRecommendation
import com.localnovel.engine.model.StateDelta
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.service.ProvisionalLayerCharterImporter
import com.localnovel.engine.service.ProvisionalLayerValidator
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Mvp8ProvisionalLayerTest {
    @Test
    fun provisionalLayerMigrationCreatesTables() {
        val repository = newRepository()
        repository.initialize()

        val expected = setOf(
            "canon_thin_zones",
            "provisional_entities",
            "provisional_entity_evidence",
            "provisional_promotion_queue",
            "provisional_promotion_records",
        )
        assertTrue(repository.listTables().containsAll(expected))
    }

    @Test
    fun charterImportCreatesActiveThinZones() {
        val repository = seededRepository()
        val result = ProvisionalLayerCharterImporter(repository).importCharterFile(
            projectId = "demo",
            charterPath = charterPath(),
        )

        assertEquals("PASS", result.verdict)
        assertTrue(result.importedCount >= 4)
        assertTrue(result.activeCount >= 3)
        val zones = repository.listCanonThinZones("demo")
        assertTrue(zones.any { it.thinZoneId == "thin-chenxing-supporting-cast" && it.status == "ACTIVE" })
        assertTrue(zones.any { it.thinZoneId == "thin-chenxing-post-crossing" && it.status == "PAUSED" })
    }

    @Test
    fun soulfulProvisionalCharacterResolves() {
        val repository = seededRepositoryWithThinZones()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning", readerMode = true)
        val context = runtime.prepareTurn(session.id, "我想在碼頭找人搭船。")
        val result = validMultiTurnResult(context, "result-prov-soulful").copy(
            entityProposals = listOf(soulfulCharacterProposal()),
        )

        runtime.resolveTurn(session.id, result)

        val entities = repository.listProvisionalEntities(session.id)
        assertEquals(1, entities.size)
        assertEquals("蘇橋", entities.single().displayName)
        assertEquals("ACTIVE", entities.single().status)
        assertTrue(repository.getSessionReaderMode(session.id))
    }

    @Test
    fun plotCourierProposalBlocked() {
        val repository = seededRepositoryWithThinZones()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我想找人帶路。")
        val result = validMultiTurnResult(context, "result-prov-courier").copy(
            entityProposals = listOf(
                soulfulCharacterProposal().copy(
                    proposalId = "prov-char-courier",
                    displayName = "信使",
                    narrativeRole = "plot_courier",
                    introductionBeat = "他主動提供情報並願意帶路。",
                    agencyProfile = ProvisionalAgencyProfile(
                        desire = "完成任務",
                        fearOrCost = "無",
                        blindSpot = "無",
                        refusalCapacity = "願意幫忙帶路",
                    ),
                ),
            ),
        )

        val report = runtime.validate(context, result)
        assertFalse(report.isValid)
        assertTrue(report.errors.any { it.code == "PROVISIONAL_PLOT_COURIER" })
        assertFailsWith<IllegalStateException> {
            runtime.resolveTurn(session.id, result)
        }
    }

    @Test
    fun unqualifiedPromotionRecommendationBlocked() {
        val repository = seededRepositoryWithThinZones()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我想認識碼頭的人。")
        val proposal = soulfulCharacterProposal()
        val result = validMultiTurnResult(context, "result-prov-unqualified-promo").copy(
            entityProposals = listOf(proposal),
            promotionRecommendations = listOf(
                ProvisionalPromotionRecommendation(
                    recommendationId = "promo-weak-1",
                    proposalId = proposal.proposalId,
                    qualificationChecks = listOf("SOUL"),
                    reason = "還不夠",
                    readerValue = "短",
                    irreplaceability = "短",
                    formalIdentityDraft = "蘇橋",
                    exitIfNotPromoted = "離場",
                ),
            ),
        )

        val report = runtime.validate(context, result)
        assertFalse(report.isValid)
        assertTrue(report.errors.any { it.code == "PROVISIONAL_PROMOTION_UNQUALIFIED" })
    }

    @Test
    fun aiPromotionRecommendationEntersQueue() {
        val repository = seededRepositoryWithThinZones()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我想認識碼頭的人。")
        val proposal = soulfulCharacterProposal()
        val result = validMultiTurnResult(context, "result-prov-promo-queue").copy(
            entityProposals = listOf(proposal),
            promotionRecommendations = listOf(
                ProvisionalPromotionRecommendation(
                    recommendationId = "promo-strong-1",
                    proposalId = proposal.proposalId,
                    qualificationChecks = listOf("SOUL", "IRREPLACEABLE", "READER_WANTS_MORE"),
                    reason = "讀者已經在意他會不會拒絕載客，他的代價感與盲點讓衝突有延伸空間。",
                    readerValue = "讀者想看他下次還會不會為了家人再次拒絕。",
                    irreplaceability = "目前沒有第二個能同時代表碼頭生計與官府壓力的人物。",
                    formalIdentityDraft = "蘇橋｜碼頭船家｜壓力測試者",
                    exitIfNotPromoted = "降為傳聞：碼頭有個難纏的船家。",
                ),
            ),
        )

        runtime.resolveTurn(session.id, result)

        val recommendations = repository.listPendingPromotionRecommendations(session.id)
        assertEquals(1, recommendations.size)
        assertEquals("promo-strong-1", recommendations.single().recommendationId)
        assertNotNull(recommendations.single().entityId)
    }

    @Test
    fun prepareTurnIncludesThinZonesAndReaderMode() {
        val repository = seededRepositoryWithThinZones()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning", readerMode = true)
        val context = runtime.prepareTurn(session.id, "我先看看四周。")

        assertTrue(context.readerMode)
        assertTrue(context.canonThinZones.isNotEmpty())
        assertTrue(context.canonThinZones.any { it.thinZoneId == "thin-chenxing-supporting-cast" })
    }

    @Test
    fun provisionalValidatorRejectsUnknownThinZone() {
        val repository = seededRepositoryWithThinZones()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "測試。")
        val result = validMultiTurnResult(context, "result-prov-bad-zone").copy(
            entityProposals = listOf(
                soulfulCharacterProposal().copy(thinZoneId = "thin-does-not-exist"),
            ),
        )

        val errors = ProvisionalLayerValidator(repository).validate(context, result)
        assertTrue(errors.any { it.code == "PROVISIONAL_THIN_ZONE_REQUIRED" })
    }

    private fun newRepository(): SqliteStoryRepository {
        val dbPath = Files.createTempDirectory("story-engine-mvp8-test").resolve("story.db")
        return SqliteStoryRepository(dbPath)
    }

    private fun seededRepository(): SqliteStoryRepository {
        val repository = newRepository()
        repository.initialize()
        repository.seedDemo(readJson(samplePath("demo_project_seed.json")))
        return repository
    }

    private fun seededRepositoryWithThinZones(): SqliteStoryRepository {
        val repository = seededRepository()
        ProvisionalLayerCharterImporter(repository).importCharterFile("demo", charterPath())
        return repository
    }

    private fun soulfulCharacterProposal(): ProvisionalEntityProposal =
        ProvisionalEntityProposal(
            proposalId = "prov-char-test-1",
            entityType = "CHARACTER",
            displayName = "蘇橋",
            thinZoneId = "thin-chenxing-supporting-cast",
            introductionBeat = "他在碼頭拒絕讓陌生人搭船，只問對方願意付什麼代價。",
            agencyProfile = ProvisionalAgencyProfile(
                desire = "保住船隊與家人飯碗",
                fearOrCost = "被官府記名後全家連坐",
                blindSpot = "以為錢能擋住所有禍",
                refusalCapacity = "可以拒絕載客，甚至可以放火燒船不讓外人上",
            ),
            narrativeRole = "壓力測試者",
        )

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
            narrative = "沈霜只看了玩家一眼，沒有多說，仍把視線留在門上。",
            turnSummary = "場面維持緊張，沒有揭露禁忌。",
            characterActions = listOf(
                CharacterAction(
                    characterId = "char-shen-shuang",
                    intent = "守住後殿並控制回答邊界",
                    visibleAction = "沈霜站在門前，沒有回答。",
                    hiddenMotive = "避免外人知道祠堂地底的禁忌。",
                    characterFitScore = 88,
                    storyProgressScore = 76,
                ),
            ),
            stateDelta = StateDelta(
                operations = listOf(
                    DeltaOperation(
                        type = DeltaOperationType.ADD_EVENT,
                        targetType = "event",
                        targetId = "event-mvp8-test",
                        payload = JsonObject(mapOf("summary" to JsonPrimitive("試用層測試事件。"))),
                    ),
                ),
            ),
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

    private fun charterPath(): Path {
        val candidates = listOf(
            Path.of("world", "chenxing", "formal", "35_provisional_layer_charter_v1.json"),
            Path.of("..", "world", "chenxing", "formal", "35_provisional_layer_charter_v1.json"),
        )
        return candidates.firstOrNull { Files.exists(it) }
            ?: error("Cannot find charter file: 35_provisional_layer_charter_v1.json")
    }
}