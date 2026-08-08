package com.localnovel.engine

import com.localnovel.engine.model.CapabilityContract
import com.localnovel.engine.model.CharacterProfile
import com.localnovel.engine.model.CharacterState
import com.localnovel.engine.model.ContextKnowledgeClaim
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.Event
import com.localnovel.engine.model.NpcActorDecisionSummary
import com.localnovel.engine.model.NpcActorEvidenceRef
import com.localnovel.engine.model.NpcActorOutputConstraints
import com.localnovel.engine.model.NpcActorRequest
import com.localnovel.engine.model.NpcActorResponse
import com.localnovel.engine.model.NpcAutonomyProfile
import com.localnovel.engine.model.NpcGoalPlan
import com.localnovel.engine.model.NpcPerception
import com.localnovel.engine.model.NpcVoiceProfile
import com.localnovel.engine.model.Project
import com.localnovel.engine.model.RetrievedMemory
import com.localnovel.engine.model.Secret
import com.localnovel.engine.service.FakeNpcActorGateway
import com.localnovel.engine.service.FileNpcActorGateway
import com.localnovel.engine.service.NpcActorJson
import com.localnovel.engine.service.NpcActorPilotService
import com.localnovel.engine.service.NpcActorRequestBuilder
import com.localnovel.engine.service.NpcActorResponseValidator
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.encodeToString

class NpcActorServicesTest {
    @Test
    fun `request builder exposes only actor-owned bounded evidence`() {
        val request = NpcActorRequestBuilder().build(
            context = sampleContext(),
            profile = sampleProfile(),
            perception = NpcPerception(
                npcId = ACTOR_ID,
                locationId = "location-market",
                playerIsElsewhere = false,
                perceivedSituation = "市場出現騷動。",
                knowledgeClaimIds = listOf("claim-owned", "claim-foreign"),
                visibleEventIds = listOf("event-visible"),
            ),
            goal = NpcGoalPlan(activeGoal = "弄清楚錢袋是誰的", priority = 80, source = "profile"),
            scenarioId = "scenario-builder",
            targetIds = listOf(TARGET_ID),
        )

        assertEquals(listOf("memory-owned"), request.ownedMemories.map { it.evidenceId })
        assertEquals(
            listOf("claim-owned", "profile:$ACTOR_ID:false-belief"),
            request.ownedBeliefs.map { it.evidenceId },
        )
        assertEquals(listOf("event-visible"), request.perceivedFacts.map { it.evidenceId })
        assertEquals(listOf("cap-owned"), request.capabilities.map { it.evidenceId })
        assertEquals(listOf("claim-owned", "profile:$ACTOR_ID:false-belief"), request.revealableInformationIds)
        assertEquals(listOf("claim-owned", "profile:$ACTOR_ID:false-belief"), request.withholdableInformationIds)
        assertTrue(request.goals.any { it.kind == "MOTIVATION" && it.summary == "守住市場信譽" })
        assertTrue(request.goals.any { it.kind == "FEAR" && it.summary == "被當成包庇盜賊" })

        val encoded = NpcActorJson.strict.encodeToString(request)
        assertFalse(encoded.contains("foreign memory"))
        assertFalse(encoded.contains("foreign belief"))
        assertFalse(encoded.contains("author-only-secret"))
        assertFalse(encoded.contains("directorPressure"))
    }

    @Test
    fun `valid fake response maps only to game master review proposal`() {
        val request = sampleRequest()
        val response = sampleResponse(request)
        val result = NpcActorPilotService().propose(
            request,
            FakeNpcActorGateway(mapOf(request.requestId to response)),
        )

        assertTrue(result.accepted)
        assertEquals("ACCEPTED_FOR_GAME_MASTER_REVIEW", result.status)
        assertNotNull(result.proposal)
        assertEquals(ACTOR_ID, result.proposal.npcId)
        assertEquals(listOf("memory-owned"), result.proposal.usedMemoryIds)
        assertEquals(listOf("claim-owned"), result.proposal.usedKnowledgeClaimIds)
    }

    @Test
    fun `foreign evidence and forbidden evidence are rejected without proposal`() {
        val request = sampleRequest().copy(forbiddenInformationIds = listOf("secret-forbidden"))
        val response = sampleResponse(request).copy(
            memoryIds = listOf("memory-foreign"),
            beliefClaimIds = listOf("claim-foreign"),
            decisionSummary = NpcActorDecisionSummary(
                evidenceIds = listOf("secret-forbidden"),
                selectedActionLabel = "查看錢袋",
            ),
        )

        val result = NpcActorResponseValidator().validateAndMap(request, response)

        assertFalse(result.accepted)
        assertNull(result.proposal)
        assertTrue("foreign_memory_reference" in result.findings)
        assertTrue("foreign_knowledge_reference" in result.findings)
        assertTrue("unknown_decision_evidence_reference" in result.findings)
        assertTrue("forbidden_information_reference" in result.findings)
    }

    @Test
    fun `file gateway writes request and strictly rejects unknown response fields`() {
        val tempDir = Files.createTempDirectory("npc-actor-file-gateway")
        val requestPath = tempDir.resolve("out/request.json")
        val responsePath = tempDir.resolve("response.json")
        val request = sampleRequest()
        val validJson = NpcActorJson.strict.encodeToString(sampleResponse(request))
        Files.writeString(responsePath, validJson.dropLast(2) + ",\n  \"unexpected\": true\n}")

        assertFails { FileNpcActorGateway(requestPath, responsePath).propose(request) }
        assertTrue(Files.isRegularFile(requestPath))
        assertEquals(request, NpcActorJson.strict.decodeFromString<NpcActorRequest>(Files.readString(requestPath)))
    }

    private fun sampleContext(): ContextPacket = ContextPacket(
        project = Project("project", "NPC Actor Pilot", "non-canon evaluation"),
        turnId = "turn-1",
        turnNumber = 1,
        playerInput = "這錢袋是誰的？",
        activeScene = null,
        sceneContract = null,
        characters = listOf(
            CharacterProfile(ACTOR_ID, "project", "林婶", "merchant", "謹慎的商販"),
            CharacterProfile(TARGET_ID, "project", "旅人", "player", "路過市場", isPlayer = true),
            CharacterProfile("npc-foreign", "project", "張漢", "guard", "巡邏守衛"),
        ),
        characterStates = listOf(
            CharacterState(ACTOR_ID, "project", "location-market", "警覺", "健康"),
            CharacterState(TARGET_ID, "project", "location-market", "平靜", "健康"),
        ),
        retrievedMemories = listOf(
            RetrievedMemory("memory-owned", ACTOR_ID, "EPISODIC", "昨晚有人掉了錢袋", confidence = 90, salience = 70, retrievalScore = 95),
            RetrievedMemory("memory-foreign", "npc-foreign", "EPISODIC", "foreign memory", confidence = 90, salience = 70, retrievalScore = 95),
        ),
        knowledgeClaims = listOf(
            ContextKnowledgeClaim("claim-owned", ACTOR_ID, "purse.owner", "錢袋可能屬於老陳", "BELIEVES", 75, "memory", sourceMemoryId = "memory-owned"),
            ContextKnowledgeClaim("claim-owned-unselected", ACTOR_ID, "market.weather", "明日可能下雨", "BELIEVES", 60, "memory"),
            ContextKnowledgeClaim("claim-foreign", "npc-foreign", "purse.stolen", "foreign belief", "KNOWS", 100, "fact"),
        ),
        recentEvents = listOf(
            Event("event-visible", "project", "turn-1", "旅人拿起地上的錢袋"),
            Event("event-hidden", "project", "turn-1", "author-only-secret"),
        ),
        revealedSecrets = listOf(Secret("secret", "project", "author-only-secret", revealed = true)),
        activeCapabilityContracts = listOf(
            CapabilityContract("skill", "cap-owned", "辨認貨幣", listOf(ACTOR_ID), "檢查錢袋", limitations = listOf("只能辨認當地錢幣")),
            CapabilityContract("skill", "cap-foreign", "追蹤", listOf("npc-foreign"), "追查腳印"),
        ),
    )

    private fun sampleProfile(): NpcAutonomyProfile = NpcAutonomyProfile(
        npcId = ACTOR_ID,
        deepMotivation = "守住市場信譽",
        falseBelief = "陌生人多半不可信",
        fear = "被當成包庇盜賊",
        relationshipDebts = listOf("曾受老陳幫助"),
        offscreenAgenda = listOf("盤點貨物"),
        voiceProfile = NpcVoiceProfile(
            principles = listOf("先確認再下判斷"),
            forbiddenPatterns = listOf("身為一個AI"),
        ),
    )

    private fun sampleRequest(): NpcActorRequest = NpcActorRequest(
        requestId = "request-1",
        scenarioId = "scenario-1",
        actorId = ACTOR_ID,
        targetIds = listOf(TARGET_ID),
        locationId = "location-market",
        voiceProfile = sampleProfile().voiceProfile,
        physicalState = "健康",
        emotionalState = "警覺",
        perceivedFacts = listOf(NpcActorEvidenceRef("event-visible", "PERCEPTION", "旅人拿起錢袋", "ACTOR_VISIBLE")),
        ownedMemories = listOf(NpcActorEvidenceRef("memory-owned", "MEMORY", "昨晚有人掉錢袋", "ACTOR_OWNED")),
        ownedBeliefs = listOf(NpcActorEvidenceRef("claim-owned", "BELIEF", "錢袋可能屬於老陳", "ACTOR_OWNED")),
        goals = listOf(NpcActorEvidenceRef("goal:$ACTOR_ID", "GOAL", "確認錢袋的主人", "ACTOR_OWNED")),
        revealableInformationIds = listOf("claim-owned"),
        withholdableInformationIds = listOf("claim-owned"),
        outputConstraints = NpcActorOutputConstraints(maxUtteranceChars = 48),
    )

    private fun sampleResponse(request: NpcActorRequest): NpcActorResponse = NpcActorResponse(
        requestId = request.requestId,
        actorId = request.actorId,
        targetIds = request.targetIds,
        speechAct = "QUESTION",
        immediateGoal = "確認錢袋主人",
        beliefClaimIds = listOf("claim-owned"),
        memoryIds = listOf("memory-owned"),
        emotionShown = "警覺",
        emotionHidden = "擔心誤會旅人",
        actionType = "seek_local_evidence",
        actionIntent = "請對方說明錢袋來源",
        visibleAction = "伸手示意先別離開",
        utterance = "先別走。這錢袋你從哪裡拿的？",
        nonverbalAction = "視線落在錢袋扣環上",
        predictedCost = "可能引起旅人不滿",
        observableConsequence = "對話暫停，錢袋來源成為當前問題",
        confidence = 82,
        decisionSummary = NpcActorDecisionSummary(
            evidenceIds = listOf("event-visible", "memory-owned", "claim-owned"),
            selectedActionLabel = "詢問來源",
            rejectedActionLabels = listOf("直接指控"),
        ),
    )

    private companion object {
        const val ACTOR_ID = "npc-lin-wan"
        const val TARGET_ID = "player-traveler"
    }
}
