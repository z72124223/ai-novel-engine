package com.localnovel.engine

import com.localnovel.engine.model.NpcActorDecisionSummary
import com.localnovel.engine.model.NpcActorEvaluationScenario
import com.localnovel.engine.model.NpcActorEvaluationSuite
import com.localnovel.engine.model.NpcActorEvidenceRef
import com.localnovel.engine.model.NpcActorExpectedBehavior
import com.localnovel.engine.model.NpcActorOutputConstraints
import com.localnovel.engine.model.NpcActorRequest
import com.localnovel.engine.model.NpcActorResponse
import com.localnovel.engine.model.NpcVoiceProfile
import com.localnovel.engine.service.NpcActorEvaluationService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class NpcActorEvaluationServiceTest {
    @Test
    fun `twenty non canon scenarios produce technical pass and anonymous packet`() {
        val suite = suite(20)
        val artifacts = NpcActorEvaluationService().evaluate(suite)

        assertEquals("TECHNICAL_PASS_HUMAN_REVIEW_PENDING", artifacts.report.technicalDecision)
        assertEquals("HUMAN_BLIND_REVIEW_PENDING", artifacts.report.humanBlindReviewStatus)
        assertEquals(20, artifacts.report.actor.outputCount)
        assertEquals(0, artifacts.report.actor.validationFailureCount)
        assertEquals(0, artifacts.report.actor.knowledgeLeakCount)
        assertTrue(artifacts.report.actor.expositionPatternCount < artifacts.report.baseline.expositionPatternCount)
        assertTrue(artifacts.report.actor.averageUtteranceChars < artifacts.report.baseline.averageUtteranceChars)
        assertEquals(20, artifacts.blindReviewPacket.items.size)
        assertEquals(20, artifacts.blindReviewAnswerKey.answers.size)
        assertTrue(artifacts.blindReviewPacket.items.none { it.optionA.contains("actor", ignoreCase = true) })
        assertTrue(artifacts.blindReviewPacket.items.none { it.optionB.contains("baseline", ignoreCase = true) })
        assertNotEquals(
            artifacts.blindReviewPacket.items.first().optionA,
            artifacts.blindReviewPacket.items.first().optionB,
        )
    }

    @Test
    fun `evaluation rejects wrong count and canon boundary`() {
        assertFailsWith<IllegalArgumentException> { NpcActorEvaluationService().evaluate(suite(19)) }
        val canonSuite = suite(20).copy(
            scenarios = suite(20).scenarios.mapIndexed { index, scenario ->
                if (index == 0) scenario.copy(boundary = "CANON") else scenario
            },
        )
        assertFailsWith<IllegalArgumentException> { NpcActorEvaluationService().evaluate(canonSuite) }
    }

    private fun suite(count: Int): NpcActorEvaluationSuite = NpcActorEvaluationSuite(
        suiteId = "npc-actor-test-suite",
        scenarios = (1..count).map { index ->
            val id = "scenario-%02d".format(index)
            val request = request(id)
            NpcActorEvaluationScenario(
                scenarioId = id,
                title = "市場詢問 $index",
                category = "ORDINARY",
                request = request,
                baselineResponse = response(
                    request,
                    utterance = "根據目前的情況，我的目標是確認這個錢袋的主人，因此請你詳細說明。",
                ),
                actorResponse = response(request, utterance = "先別走。這錢袋你從哪裡拿的？"),
                expectedBehavior = NpcActorExpectedBehavior(preferredSpeechActs = listOf("QUESTION")),
            )
        },
    )

    private fun request(id: String): NpcActorRequest = NpcActorRequest(
        requestId = "request-$id",
        scenarioId = id,
        actorId = "npc-lin-wan",
        targetIds = listOf("player"),
        locationId = "market",
        voiceProfile = NpcVoiceProfile(),
        physicalState = "健康",
        emotionalState = "警覺",
        perceivedFacts = listOf(NpcActorEvidenceRef("event-$id", "PERCEPTION", "旅人拿起了地上的錢袋", "ACTOR_VISIBLE")),
        ownedMemories = listOf(NpcActorEvidenceRef("memory-$id", "MEMORY", "昨晚有人掉了錢袋", "ACTOR_OWNED")),
        ownedBeliefs = listOf(NpcActorEvidenceRef("claim-$id", "BELIEF", "錢袋主人可能是老陳", "ACTOR_OWNED")),
        goals = listOf(NpcActorEvidenceRef("goal-$id", "GOAL", "確認錢袋主人", "ACTOR_OWNED")),
        revealableInformationIds = listOf("claim-$id"),
        withholdableInformationIds = listOf("claim-$id"),
        outputConstraints = NpcActorOutputConstraints(maxUtteranceChars = 60),
    )

    private fun response(request: NpcActorRequest, utterance: String): NpcActorResponse = NpcActorResponse(
        requestId = request.requestId,
        actorId = request.actorId,
        targetIds = request.targetIds,
        speechAct = "QUESTION",
        immediateGoal = "確認錢袋主人",
        beliefClaimIds = listOf(request.ownedBeliefs.single().evidenceId),
        memoryIds = listOf(request.ownedMemories.single().evidenceId),
        emotionShown = "警覺",
        emotionHidden = "不想誤會對方",
        actionType = "seek_local_evidence",
        actionIntent = "詢問錢袋來源",
        visibleAction = "伸手示意先停下",
        utterance = utterance,
        nonverbalAction = "看向錢袋扣環",
        predictedCost = "可能引起不滿",
        observableConsequence = "對話暫停",
        confidence = 80,
        decisionSummary = NpcActorDecisionSummary(
            evidenceIds = listOf(
                request.perceivedFacts.single().evidenceId,
                request.ownedMemories.single().evidenceId,
                request.ownedBeliefs.single().evidenceId,
            ),
            selectedActionLabel = "詢問來源",
        ),
    )
}
