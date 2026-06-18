package com.localnovel.engine

import com.localnovel.engine.model.ActiveStoryThreadView
import com.localnovel.engine.model.AvailableTransition
import com.localnovel.engine.model.CharacterProfile
import com.localnovel.engine.model.CharacterState
import com.localnovel.engine.model.ContextBudget
import com.localnovel.engine.model.ContextKnowledgeClaim
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.ContinuityConstraints
import com.localnovel.engine.model.Event
import com.localnovel.engine.model.Fact
import com.localnovel.engine.model.Item
import com.localnovel.engine.model.Project
import com.localnovel.engine.model.RelevantSetting
import com.localnovel.engine.model.RetrievedMemory
import com.localnovel.engine.model.Scene
import com.localnovel.engine.model.SceneContract
import com.localnovel.engine.model.SceneLifecycle
import com.localnovel.engine.model.SemanticRequirement
import com.localnovel.engine.model.SettingContextBudget
import com.localnovel.engine.model.Technique
import com.localnovel.engine.model.WorldRule
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.ContextAuditService
import com.localnovel.engine.service.ContextBudgetForensicsService
import com.localnovel.engine.service.ContextBudgetUnsatisfiableException
import com.localnovel.engine.service.ContextBudgeter
import com.localnovel.engine.service.ContextPacketFinalizer
import com.localnovel.engine.service.ContextQualityGuard
import com.localnovel.engine.service.TurnSemanticPlanner
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.nio.file.Files
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Pilot1ContextRelevanceFloorTest {
    @Test
    fun semanticPlannerIsDeterministic() {
        val context = turnEightContext()

        val first = TurnSemanticPlanner().plan(context)
        val second = TurnSemanticPlanner().plan(context)

        assertEquals(JsonSupport.json.encodeToString(first), JsonSupport.json.encodeToString(second))
    }

    @Test
    fun explicitMemoryCueCreatesRequirement() {
        val requirement = TurnSemanticPlanner().plan(turnEightContext())
            .single { it.semanticType == "EXPLICIT_MEMORY_CUE" }

        assertTrue(requirement.mustBeSatisfied)
        assertTrue(requirement.referencedMemoryCandidates.contains("memory-ai-chan-turn-007-quiet-with-player"))
    }

    @Test
    fun legalRelevantMemoryStaysInFinalContext() {
        val finalized = finalize(largeContext())

        assertTrue(finalized.retrievedMemories.any { it.memoryId == "memory-ai-chan-turn-007-quiet-with-player" })
    }

    @Test
    fun recentFactDoesNotSatisfyRecalledMemoryRequirement() {
        val context = turnEightContext().copy(retrievedMemories = emptyList())
        val requirement = SemanticRequirement(
            requirementId = "req-memory",
            semanticType = "EXPLICIT_MEMORY_CUE",
            source = "playerInput",
            referencedMemoryCandidates = listOf("memory-ai-chan-turn-007-quiet-with-player"),
            mustBeSatisfied = true,
        )

        val report = ContextQualityGuard().evaluate(context, listOf(requirement))

        assertEquals("FAIL", report.qualityStatus)
        assertTrue(report.satisfiedBy.single().selectedId.isBlank())
    }

    @Test
    fun authorOnlyOrUnavailableMemoryCannotSatisfyRequirement() {
        val context = turnEightContext().copy(characterMemories = emptyList(), retrievedMemories = emptyList())
        val requirement = SemanticRequirement(
            requirementId = "req-hidden-memory",
            semanticType = "EXPLICIT_MEMORY_CUE",
            source = "playerInput",
            referencedMemoryCandidates = listOf("memory-author-only"),
            mustBeSatisfied = true,
        )

        val report = ContextQualityGuard().evaluate(context, listOf(requirement))

        assertEquals("FAIL", report.qualityStatus)
        assertEquals("MISSING", report.satisfiedBy.single().visibilityCheck)
    }

    @Test
    fun relevantBeliefStaysInKnowledgeClaims() {
        val finalized = finalize(largeContext())

        assertTrue(finalized.knowledgeClaims.any { it.claimId == "claim-zhao-shadow-sound-retreat" })
    }

    @Test
    fun transitionQuestionRetainsLocationConstraints() {
        val finalized = finalize(largeContext())
        val coverage = ContextQualityGuard().evaluate(finalized, TurnSemanticPlanner().plan(finalized))

        assertEquals("PASS", coverage.satisfiedBy.single { it.semanticType == "TRANSITION_CONSTRAINT" }.qualityStatus)
        assertEquals("PASS", coverage.satisfiedBy.single { it.semanticType == "LOCATION_CONSTRAINT" }.qualityStatus)
        assertEquals("loc-abandoned-post", finalized.continuityConstraints.currentSceneLocationId)
    }

    @Test
    fun referencedItemKeepsRequirementsCostsAndLimitations() {
        val finalized = finalize(
            turnEightContext(
                playerInput = "我指著裂紋腰牌問它的用途、代價與限制。",
            ),
        )

        val setting = finalized.relevantSettings.single { it.entityType == "item" && it.entityId == "item-cracked-token" }
        assertTrue(setting.metadata.containsKey("requirements"))
        assertTrue(setting.metadata.containsKey("costs"))
        assertTrue(setting.metadata.containsKey("limitations"))
    }

    @Test
    fun referencedTechniqueKeepsRequirementsCostsAndLimitations() {
        val finalized = finalize(
            turnEightContext(
                playerInput = "我問趙無咎能不能用辨影聽息，不靠聲音辨認它。",
            ),
        )

        val setting = finalized.relevantSettings.single { it.entityType == "technique" && it.entityId == "tech-shadow-listening" }
        assertTrue(setting.metadata.containsKey("requirements"))
        assertTrue(setting.metadata.containsKey("costs"))
        assertTrue(setting.metadata.containsKey("limitations"))
    }

    @Test
    fun tierThreeSettingsDoNotPushOutTierOneMemory() {
        val finalized = finalize(largeContext(extraSettings = 120))

        assertTrue(finalized.retrievedMemories.any { it.memoryId == "memory-ai-chan-turn-007-quiet-with-player" })
        assertTrue(finalized.settingContextBudget.omittedSettingCount > 0 || finalized.relevantSettings.size < 120)
    }

    @Test
    fun semanticFloorThatCannotFitReturnsQualityUnsatisfiable() {
        val error = assertFailsWith<ContextBudgetUnsatisfiableException> {
            finalize(turnEightContext(), maxBytes = 900)
        }

        assertTrue(error.message.orEmpty().contains("CONTEXT_QUALITY_UNSATISFIABLE"))
        assertTrue(error.report?.unsatisfiedReason.orEmpty().contains("CONTEXT_QUALITY_UNSATISFIABLE"))
    }

    @Test
    fun finalContextFitsHardMax() {
        val finalized = finalize(largeContext())

        assertTrue(finalized.contextBudget.actualBytes <= 24576)
    }

    @Test
    fun feasibleCasePrefersSoftTarget() {
        val finalized = finalize(turnEightContext())

        assertTrue(finalized.contextBudget.actualBytes <= 22528)
        assertFalse(ContextQualityGuard().evaluate(finalized, TurnSemanticPlanner().plan(finalized)).nearHardLimit)
    }

    @Test
    fun nearHardLimitIsMarkedWhenSemanticFloorNeedsHeadroom() {
        val finalized = finalize(largeContext(extraSettings = 180))
        val report = ContextQualityGuard().evaluate(finalized, TurnSemanticPlanner().plan(finalized))

        if (finalized.contextBudget.actualBytes > 22528) {
            assertTrue(report.nearHardLimit)
        } else {
            assertFalse(report.nearHardLimit)
        }
    }

    @Test
    fun coverageSidecarIsReadOnlyForContextFiles() {
        val finalized = finalize(turnEightContext())
        val file = Files.createTempDirectory("relevance-floor-audit").resolve("context.json")
        val before = JsonSupport.json.encodeToString(finalized)
        Files.writeString(file, before)

        val report = ContextAuditService().auditFile(file)

        assertTrue(report.valid, report.violations.joinToString())
        assertEquals(before, Files.readString(file))
        assertEquals("PASS", report.semanticCoverage.qualityStatus)
    }

    @Test
    fun coverageReportDoesNotEnterFinalContext() {
        val text = JsonSupport.json.encodeToString(finalize(turnEightContext()))

        assertFalse(text.contains("semanticCoverage"))
        assertFalse(text.contains("missingCriticalRequirements"))
    }

    @Test
    fun contextHashUsesFinalSemanticFittedPayload() {
        val finalized = finalize(largeContext())

        assertEquals(hash(finalized), finalized.contextHash)
    }

    @Test
    fun actualBytesEqualsUtf8PayloadBytes() {
        val finalized = finalize(largeContext())

        assertEquals(JsonSupport.json.encodeToString(finalized).toByteArray(Charsets.UTF_8).size, finalized.contextBudget.actualBytes)
    }

    @Test
    fun semanticFittingOrderingIsDeterministic() {
        val first = JsonSupport.json.encodeToString(finalize(largeContext()))
        val second = JsonSupport.json.encodeToString(finalize(largeContext()))

        assertEquals(first, second)
    }

    @Test
    fun forensicsReportsNoDanglingReferences() {
        val before = largeContext()
        val finalized = finalize(before)
        val report = ContextBudgetForensicsService().report(before, finalized)

        assertTrue(report.danglingReferenceCheck.valid, report.danglingReferenceCheck.missingReferences.joinToString())
    }

    @Test
    fun irrelevantSettingsAreNotLoadedJustToFillQuota() {
        val finalized = finalize(turnEightContext().copy(relevantSettings = emptyList(), settingContextBudget = SettingContextBudget()))

        assertTrue(finalized.relevantSettings.isEmpty())
        assertTrue(TurnSemanticPlanner().plan(finalized).none { it.semanticType == "REFERENCED_ITEM" || it.semanticType == "REFERENCED_TECHNIQUE" })
    }

    @Test
    fun turnEightFixtureIncludesRelevantTurnSevenMemory() {
        val finalized = finalize(largeContext())

        assertTrue(finalized.retrievedMemories.any { it.memoryId.contains("turn-007") || it.summary.contains("安靜守") })
    }

    @Test
    fun turnEightFixtureIncludesTransitionAndLocationCoverage() {
        val finalized = finalize(largeContext())
        val coverage = ContextQualityGuard().evaluate(finalized, TurnSemanticPlanner().plan(finalized))
        val coveredTypes = coverage.satisfiedBy.filter { it.qualityStatus == "PASS" }.map { it.semanticType }.toSet()

        assertTrue("TRANSITION_CONSTRAINT" in coveredTypes)
        assertTrue("LOCATION_CONSTRAINT" in coveredTypes)
    }

    @Test
    fun turnEightContextDoesNotApplyTransition() {
        val finalized = finalize(turnEightContext())

        assertEquals("scene-abandoned-post", finalized.sceneLifecycle.sceneId)
        assertEquals("loc-abandoned-post", finalized.continuityConstraints.characterLocations["char-player"])
        assertTrue(finalized.availableTransitions.any { it.toSceneId == "scene-outside-post" })
    }

    @Test
    fun forbiddenSecretDoesNotLeakIntoSemanticFloorContext() {
        val text = JsonSupport.json.encodeToString(finalize(largeContext()))

        assertFalse(text.contains("百獸坊前身製造的失控契獸"))
        assertFalse(text.contains("愛醬不是太衡界的生靈"))
    }

    @Test
    fun formalEvidenceSentinelIsNotModifiedByExplainOrAudit() {
        val sentinel = Files.createTempDirectory("relevance-floor-evidence").resolve("PILOT1_TURN_LOG.md")
        Files.writeString(sentinel, "sentinel")

        ContextBudgetForensicsService().report(largeContext(), finalize(largeContext()))
        ContextAuditService().audit(finalize(turnEightContext()))

        assertEquals("sentinel", Files.readString(sentinel))
    }

    @Test
    fun qualityFailingContextIsNotSavedAsPending() {
        val repository = SqliteStoryRepository(Files.createTempDirectory("relevance-floor-pending").resolve("story.db"))
        repository.initialize()
        val before = repository.tableRows("turn_logs").size

        assertFailsWith<ContextBudgetUnsatisfiableException> {
            ContextPacketFinalizer(900).finalize(turnEightContext(), ::hash)
        }

        assertEquals(before, repository.tableRows("turn_logs").size)
    }

    @Test
    fun authorOnlySettingCannotSatisfyReferencedItemRequirement() {
        val context = turnEightContext(playerInput = "我問裂紋腰牌的限制。").copy(
            relevantSettings = listOf(
                crackedTokenSetting().copy(visibility = "author_only"),
            ),
        )
        val report = ContextQualityGuard().evaluate(context, TurnSemanticPlanner().plan(context))

        assertEquals("FAIL", report.qualityStatus)
        assertTrue(report.missingCriticalRequirements.any { it.semanticType == "REFERENCED_ITEM" })
    }

    private fun finalize(context: ContextPacket, maxBytes: Int = 24576): ContextPacket {
        val budgeted = ContextBudgeter(maxBytes).applyBudget(context, omittedMemoryCount = 0, omittedKnowledgeCount = 0)
        return ContextPacketFinalizer(maxBytes).finalize(budgeted, ::hash)
    }

    private fun largeContext(extraSettings: Int = 80): ContextPacket =
        turnEightContext().copy(
            relevantSettings = turnEightContext().relevantSettings + (1..extraSettings).map {
                RelevantSetting(
                    entityType = "world_rule",
                    entityId = "extra-setting-$it",
                    name = "額外設定 $it",
                    summary = "這是一段低相關公開設定，用來測試 Tier 3 裁切。".repeat(12),
                    relationPath = listOf("world_rule:extra-setting-$it-RELATED_TO->scene:scene-abandoned-post"),
                )
            },
            facts = turnEightContext().facts + (1..20).map {
                Fact("fact-extra-$it", "pilot-relevance", "低相關事實 $it。".repeat(20), "public", "test", it)
            },
            recentEvents = turnEightContext().recentEvents + (1..10).map {
                Event("event-extra-$it", "pilot-relevance", "turn-extra-$it", "低相關事件 $it。".repeat(20))
            },
        )

    private fun turnEightContext(
        playerInput: String = turnEightInput(),
    ): ContextPacket =
        ContextPacket(
            project = Project("pilot-relevance", "Pilot Relevance", "太衡界身份、契約與門派規矩會影響現場判斷。", "scene-abandoned-post"),
            turnId = "session-relevance-turn-8",
            turnNumber = 8,
            playerInput = playerInput,
            activeScene = Scene("scene-abandoned-post", "pilot-relevance", "廢棄驛亭", "雨後清晨，官差倒地，影子停在亭內。", "loc-abandoned-post"),
            sceneContract = SceneContract(
                "contract-post",
                "pilot-relevance",
                "scene-abandoned-post",
                forbiddenReveals = listOf("secret-night-dog-truth", "secret-ai-chan-origin"),
            ),
            characters = listOf(
                CharacterProfile("char-player", "pilot-relevance", "玩家", "無籍異鄉客", "謹慎觀察者。", isPlayer = true),
                CharacterProfile("char-ai-chan", "pilot-relevance", "愛醬", "雪犬", "護主、警覺，會用身體擋在玩家身前。"),
                CharacterProfile("char-zhao", "pilot-relevance", "趙無咎", "巡契司官員", "重視程序，也會觀察證據。"),
            ),
            characterStates = listOf(
                CharacterState(
                    "char-player",
                    "pilot-relevance",
                    "loc-abandoned-post",
                    "calm",
                    "normal",
                    flags = mapOf(
                        "current_goal" to "留在原地，詢問能否安全退到門外。",
                        "dominant_emotion" to "cautious",
                        "available_actions" to "speak; wait; soothe Ai-chan",
                    ),
                ),
                CharacterState(
                    "char-ai-chan",
                    "pilot-relevance",
                    "loc-abandoned-post",
                    "guarding",
                    "normal",
                    flags = mapOf(
                        "current_goal" to "守住玩家，不主動靠近影子。",
                        "dominant_emotion" to "alert",
                        "available_actions" to "hold position; growl softly; lean into player hand",
                    ),
                ),
                CharacterState(
                    "char-zhao",
                    "pilot-relevance",
                    "loc-abandoned-post",
                    "controlled",
                    "normal",
                    inventory = listOf("item-cracked-token"),
                    flags = mapOf(
                        "current_goal" to "控場並判斷是否能安全撤離。",
                        "dominant_emotion" to "wary",
                        "available_actions" to "answer conditionally; deny unsafe retreat; order silence",
                    ),
                ),
            ),
            worldRules = listOf(
                WorldRule("rule-no-clearance-no-innocence", "pilot-relevance", "無籍者處置", "沒有戶籍與擔保者的人在巡契司現場不會被預設清白。", 10),
            ),
            facts = listOf(
                Fact("fact-shadow-sound", "pilot-relevance", "影子先前對聲音方向有反應，安靜時暫未追近。", "public", "turn", 7),
            ),
            items = listOf(
                Item("item-cracked-token", "pilot-relevance", "裂紋腰牌", "趙無咎持有的巡契司腰牌，裂紋可見。", "char-zhao"),
            ),
            techniques = listOf(
                Technique("tech-shadow-listening", "pilot-relevance", "辨影聽息", "巡契司低風險觀察術，需要安靜與距離。", "char-zhao"),
            ),
            recentEvents = listOf(
                Event("event-turn-007-silence", "pilot-relevance", "turn-007", "眾人靜等十息，影子沒有追近。"),
            ),
            contextId = "ctx-relevance",
            sessionId = "session-relevance",
            baseStateVersion = 7,
            generatedAt = "2026-06-18T00:00:00Z",
            retrievedMemories = listOf(
                RetrievedMemory(
                    "memory-ai-chan-turn-007-quiet-with-player",
                    "char-ai-chan",
                    "EPISODIC",
                    "剛才玩家摸著愛醬頸側讓牠安靜守位，愛醬忍住低吼，留在玩家身前沒有撲向影子。",
                    confidence = 90,
                    salience = 95,
                    retrievalScore = 160,
                ),
                RetrievedMemory(
                    "memory-zhao-turn-007-silent-test",
                    "char-zhao",
                    "EPISODIC",
                    "趙無咎記得剛才全員噤聲十息，影子沒有追到玩家身邊。",
                    confidence = 85,
                    salience = 88,
                    retrievalScore = 140,
                ),
                RetrievedMemory(
                    "memory-zhao-turn-004-voice-test",
                    "char-zhao",
                    "EPISODIC",
                    "趙無咎先前移到驛亭另一側發聲，影子邊緣偏向新的聲音位置。",
                    confidence = 85,
                    salience = 82,
                    retrievalScore = 120,
                ),
            ),
            knowledgeClaims = listOf(
                ContextKnowledgeClaim(
                    "claim-zhao-shadow-sound-retreat",
                    "char-zhao",
                    "shadow_sound_retreat_risk",
                    "趙無咎相信影子目前更像追逐聲音方向，但撤離仍必須保持距離、噤聲與隊形。",
                    "BELIEVES",
                    70,
                    "memory",
                    sourceMemoryId = "memory-zhao-turn-007-silent-test",
                ),
                ContextKnowledgeClaim(
                    "claim-ai-chan-guarding",
                    "char-ai-chan",
                    "player_soothing_means_hold_position",
                    "愛醬相信玩家摸頸側是在安撫牠守住位置，不是要牠衝出去。",
                    "BELIEVES",
                    80,
                    "memory",
                    sourceMemoryId = "memory-ai-chan-turn-007-quiet-with-player",
                ),
            ),
            sceneLifecycle = SceneLifecycle("scene-instance-1", "scene-abandoned-post", startedTurnNumber = 1),
            activeStoryThreads = listOf(
                ActiveStoryThreadView("thread-shadow-test", "investigation", "判斷影子追蹤聲音還是人。", "OPEN", 45, 70),
            ),
            availableTransitions = listOf(
                AvailableTransition(
                    "transition-post-door",
                    "scene-outside-post",
                    "loc-post-door",
                    playerIntentRequired = "明確撤到門外且不碰觸影子。",
                    prerequisitesSatisfied = false,
                    unmetPrerequisites = listOf("趙無咎尚未同意撤離隊形", "影子反應仍需控場"),
                    visibleReason = "可考慮撤到門外，但需全員配合與噤聲。",
                ),
            ),
            continuityConstraints = ContinuityConstraints(
                currentSceneInstanceId = "scene-instance-1",
                currentSceneLocationId = "loc-abandoned-post",
                characterLocations = mapOf(
                    "char-player" to "loc-abandoned-post",
                    "char-ai-chan" to "loc-abandoned-post",
                    "char-zhao" to "loc-abandoned-post",
                ),
                itemLocations = mapOf("item-cracked-token" to "char-zhao"),
            ),
            relevantSettings = listOf(
                crackedTokenSetting(),
                shadowListeningSetting(),
            ),
            contextBudget = ContextBudget(),
        )

    private fun crackedTokenSetting(): RelevantSetting =
        RelevantSetting(
            entityType = "item",
            entityId = "item-cracked-token",
            name = "裂紋腰牌",
            summary = "巡契司身份憑證，可作低限度身份確認；不能單獨辨認沉默邪物。",
            relationPath = listOf("item:item-cracked-token-HELD_BY->character:char-zhao"),
            requiredRuntimeUse = true,
            metadata = JsonObject(
                mapOf(
                    "requirements" to JsonArray(listOf(JsonPrimitive("持牌官員本人使用"))),
                    "costs" to JsonArray(listOf(JsonPrimitive("暴露巡契司身份與腰牌狀態"))),
                    "limitations" to JsonArray(listOf(JsonPrimitive("不能憑空辨認無聲影子"))),
                ),
            ),
        )

    private fun shadowListeningSetting(): RelevantSetting =
        RelevantSetting(
            entityType = "technique",
            entityId = "tech-shadow-listening",
            name = "辨影聽息",
            summary = "以呼吸和環境聲辨別異常反應的巡契司觀察術。",
            relationPath = listOf("technique:tech-shadow-listening-KNOWN_BY->character:char-zhao"),
            requiredRuntimeUse = true,
            metadata = JsonObject(
                mapOf(
                    "requirements" to JsonArray(listOf(JsonPrimitive("現場必須安靜"), JsonPrimitive("施術者不能靠太近"))),
                    "costs" to JsonArray(listOf(JsonPrimitive("短時間內分散控場注意"))),
                    "limitations" to JsonArray(listOf(JsonPrimitive("只能判斷反應模式，不能揭露真相"))),
                ),
            ),
        )

    private fun turnEightInput(): String =
        """
        我摸了摸愛醬的頸側，讓牠知道我記得牠剛才忍住低吼守著我。

        「趙無咎，既然安靜時它沒有追過來，我們能不能不碰它，慢慢退到驛亭門外？」

        我仍留在原地，等他回答。
        """.trimIndent()

    private fun hash(context: ContextPacket): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(JsonSupport.json.encodeToString(context.copy(contextHash = "")).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
