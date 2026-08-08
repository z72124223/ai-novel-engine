package com.localnovel.engine.service

import com.localnovel.engine.model.NpcActorBlindReviewAnswer
import com.localnovel.engine.model.NpcActorBlindReviewAnswerKey
import com.localnovel.engine.model.NpcActorBlindReviewItem
import com.localnovel.engine.model.NpcActorBlindReviewPacket
import com.localnovel.engine.model.NpcActorEvaluationReport
import com.localnovel.engine.model.NpcActorEvaluationScenario
import com.localnovel.engine.model.NpcActorEvaluationSuite
import com.localnovel.engine.model.NpcActorMetricSummary
import com.localnovel.engine.model.NpcActorResponse
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.math.ceil

data class NpcActorEvaluationArtifacts(
    val report: NpcActorEvaluationReport,
    val blindReviewPacket: NpcActorBlindReviewPacket,
    val blindReviewAnswerKey: NpcActorBlindReviewAnswerKey,
)

class NpcActorEvaluationService(
    private val validator: NpcActorResponseValidator = NpcActorResponseValidator(),
) {
    fun evaluate(suite: NpcActorEvaluationSuite): NpcActorEvaluationArtifacts {
        require(suite.scenarios.size == 20) { "NPC-ACTOR-01 evaluation requires exactly 20 scenarios." }
        require(suite.scenarios.map { it.scenarioId }.distinct().size == 20) {
            "NPC-ACTOR-01 scenario ids must be unique."
        }
        require(suite.scenarios.all { it.boundary == "NON_CANON_EVALUATION" }) {
            "NPC-ACTOR-01 evaluation scenarios must remain non-Canon."
        }

        val baseline = summarize(suite.scenarios, NpcActorEvaluationScenario::baselineResponse)
        val actor = summarize(suite.scenarios, NpcActorEvaluationScenario::actorResponse)
        val technicalPass = actor.validationFailureCount == 0 &&
            actor.knowledgeLeakCount == 0 &&
            actor.continuityViolationCount == 0 &&
            actor.expectedBehaviorMatchCount >= baseline.expectedBehaviorMatchCount &&
            actor.expositionPatternCount < baseline.expositionPatternCount &&
            actor.averageUtteranceChars <= baseline.averageUtteranceChars
        val packetId = "npc-actor01-blind-${sha256(suite.suiteId).take(12)}"
        val blindItems = mutableListOf<NpcActorBlindReviewItem>()
        val answerKeys = mutableListOf<NpcActorBlindReviewAnswer>()
        suite.scenarios.forEach { scenario ->
            val reviewId = "review-${scenario.scenarioId}"
            val actorIsA = sha256("$packetId:${scenario.scenarioId}").last().digitToInt(16) % 2 == 0
            val baselineText = renderOption(scenario.baselineResponse)
            val actorText = renderOption(scenario.actorResponse)
            blindItems += NpcActorBlindReviewItem(
                reviewId = reviewId,
                scenarioId = scenario.scenarioId,
                context = renderContext(scenario),
                optionA = if (actorIsA) actorText else baselineText,
                optionB = if (actorIsA) baselineText else actorText,
                ratingPrompt = "請選擇更像真人且符合當下處境的回應（A 或 B），另記錄知識泄漏或過度 AI 句式。",
            )
            answerKeys += NpcActorBlindReviewAnswer(
                reviewId = reviewId,
                actorOption = if (actorIsA) "A" else "B",
            )
        }

        return NpcActorEvaluationArtifacts(
            report = NpcActorEvaluationReport(
                suiteId = suite.suiteId,
                scenarioCount = suite.scenarios.size,
                baseline = baseline,
                actor = actor,
                technicalDecision = if (technicalPass) {
                    "TECHNICAL_PASS_HUMAN_REVIEW_PENDING"
                } else {
                    "TECHNICAL_FAIL"
                },
                humanBlindReviewStatus = "HUMAN_BLIND_REVIEW_PENDING",
                findings = buildList {
                    add("Offline Fake/File evaluation only; no provider or network request was used.")
                    add("Blind options are anonymized; the separate answer key must not be shown before rating.")
                    if (technicalPass) {
                        add("Automated safety and style indicators pass, but do not replace human naturalness review.")
                    } else {
                        add("At least one automated acceptance condition failed; inspect metric differences before review.")
                    }
                },
            ),
            blindReviewPacket = NpcActorBlindReviewPacket(
                packetId = packetId,
                items = blindItems,
            ),
            blindReviewAnswerKey = NpcActorBlindReviewAnswerKey(
                packetId = packetId,
                answers = answerKeys,
            ),
        )
    }

    private fun summarize(
        scenarios: List<NpcActorEvaluationScenario>,
        select: (NpcActorEvaluationScenario) -> NpcActorResponse,
    ): NpcActorMetricSummary {
        val results = scenarios.map { scenario -> validator.validateAndMap(scenario.request, select(scenario)) }
        val responses = scenarios.map(select)
        val lengths = responses.map { it.utterance.length }.sorted()
        val expositionPatterns = listOf(
            "我的目標是",
            "根據目前的情況",
            "因此",
            "換句話說",
            "綜上所述",
            "我理解你的感受",
        )
        val knowledgeFindingCodes = setOf(
            "foreign_memory_reference",
            "foreign_knowledge_reference",
            "unowned_withheld_information_reference",
            "unknown_decision_evidence_reference",
            "forbidden_information_reference",
        )
        val continuityFindingCodes = setOf(
            "protocol_version_mismatch",
            "request_id_mismatch",
            "actor_id_mismatch",
            "target_ids_mismatch",
            "action_type_not_allowlisted",
            "capability_actor_mismatch",
            "unavailable_capability_reference",
        )
        return NpcActorMetricSummary(
            outputCount = responses.size,
            validationFailureCount = results.count { !it.accepted },
            knowledgeLeakCount = results.sumOf { result -> result.findings.count { it in knowledgeFindingCodes } },
            continuityViolationCount = results.sumOf { result -> result.findings.count { it in continuityFindingCodes } },
            expositionPatternCount = responses.sumOf { response ->
                expositionPatterns.count { pattern -> response.utterance.contains(pattern) }
            },
            repeatedContrastPatternCount = responses.count { response ->
                Regex("不是.{0,24}而是").containsMatchIn(response.utterance)
            },
            averageUtteranceChars = if (lengths.isEmpty()) 0.0 else lengths.average(),
            p95UtteranceChars = if (lengths.isEmpty()) 0 else lengths[(ceil(lengths.size * 0.95).toInt() - 1).coerceAtLeast(0)],
            expectedBehaviorMatchCount = scenarios.count { scenario ->
                expectedBehaviorMatches(scenario, select(scenario))
            },
        )
    }

    private fun expectedBehaviorMatches(
        scenario: NpcActorEvaluationScenario,
        response: NpcActorResponse,
    ): Boolean {
        val expected = scenario.expectedBehavior
        val speechMatches = expected.preferredSpeechActs.isEmpty() || response.speechAct in expected.preferredSpeechActs
        val silenceOrEvasionMatches = !expected.silenceOrEvasionAppropriate ||
            response.speechAct in setOf("SILENCE", "EVADE", "REFUSE", "PARTIAL_ANSWER")
        val nonverbalMatches = !expected.nonverbalAppropriate || response.nonverbalAction.isNotBlank() ||
            response.speechAct == "NONVERBAL"
        return speechMatches && silenceOrEvasionMatches && nonverbalMatches
    }

    private fun renderContext(scenario: NpcActorEvaluationScenario): String = buildString {
        append(scenario.title).append("：")
        append(scenario.request.perceivedFacts.joinToString("；") { it.summary })
        if (scenario.request.emotionalState.isNotBlank()) append("｜角色狀態：").append(scenario.request.emotionalState)
    }

    private fun renderOption(response: NpcActorResponse): String = buildString {
        if (response.utterance.isNotBlank()) append("「").append(response.utterance).append("」")
        if (response.nonverbalAction.isNotBlank()) {
            if (isNotEmpty()) append(" ")
            append("[").append(response.nonverbalAction).append("]")
        }
        if (isEmpty()) append("[保持沉默]")
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte) }
}
