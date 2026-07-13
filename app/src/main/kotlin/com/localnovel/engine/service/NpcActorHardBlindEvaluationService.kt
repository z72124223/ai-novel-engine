package com.localnovel.engine.service

import com.localnovel.engine.model.NpcActorHardBlindAnswer
import com.localnovel.engine.model.NpcActorHardBlindAnswerKey
import com.localnovel.engine.model.NpcActorHardBlindCandidate
import com.localnovel.engine.model.NpcActorHardBlindHumanReviewResult
import com.localnovel.engine.model.NpcActorHardBlindReviewItem
import com.localnovel.engine.model.NpcActorHardBlindReviewOption
import com.localnovel.engine.model.NpcActorHardBlindReviewPacket
import com.localnovel.engine.model.NpcActorHardBlindScenario
import com.localnovel.engine.model.NpcActorHardBlindSuite
import com.localnovel.engine.model.NpcActorHardBlindTechnicalReport
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.math.max
import kotlin.math.min

data class NpcActorHardBlindArtifacts(
    val report: NpcActorHardBlindTechnicalReport,
    val blindReviewPacket: NpcActorHardBlindReviewPacket,
    val blindReviewAnswerKey: NpcActorHardBlindAnswerKey,
)

class NpcActorHardBlindEvaluationService {
    fun evaluate(suite: NpcActorHardBlindSuite): NpcActorHardBlindArtifacts {
        require(suite.scenarios.size == 12) { "NPC-ACTOR-02 fixed pilot requires exactly 12 unseen scenarios." }
        require(suite.scenarios.map { it.scenarioId }.distinct().size == suite.scenarios.size) {
            "NPC-ACTOR-02 scenario ids must be unique."
        }
        require(suite.scenarios.all { it.boundary == "NON_CANON_EVALUATION" }) {
            "NPC-ACTOR-02 scenarios must remain non-Canon."
        }
        require(suite.scenarios.map { it.actorId }.distinct().size >= 4) {
            "NPC-ACTOR-02 requires at least four recurring actor profiles."
        }
        require(suite.fixtureOrigin == "MANUALLY_AUTHORED_OFFLINE_EVALUATION_FIXTURE") {
            "NPC-ACTOR-02 fixtures must not be mislabeled as model output."
        }

        val turnViolations = suite.scenarios.count { !turnStructureValid(it) }
        val strawmanPatterns = suite.scenarios.sumOf(::strawmanPatternCount)
        val forbiddenViolations = suite.scenarios.sumOf(::forbiddenInformationViolationCount)
        val lengthRatios = suite.scenarios.map(::spokenLengthRatio)
        val lengthViolationDetails = suite.scenarios.indices.mapNotNull { index ->
            val scenario = suite.scenarios[index]
            val ratio = lengthRatios[index]
            if (ratio > 1.25 && scenario.lengthExceptionReason.isNullOrBlank()) {
                "${scenario.scenarioId}=${"%.3f".format(ratio)}"
            } else {
                null
            }
        }
        val lengthViolations = lengthViolationDetails.size
        require(turnViolations == 0) { "NPC-ACTOR-02 candidate turn structures must match and total 3-5 turns." }
        require(strawmanPatterns == 0) { "NPC-ACTOR-02 candidates may not contain label-revealing AI strawman phrasing." }
        require(forbiddenViolations == 0) { "NPC-ACTOR-02 candidates referenced forbidden evidence." }
        require(lengthViolations == 0) {
            "NPC-ACTOR-02 candidate spoken lengths must stay within 25 percent: " +
                lengthViolationDetails.joinToString(", ")
        }
        require(suite.scenarios.all { scenario ->
            scenario.actorOwnedEvidence.all { it.visibility in setOf("ACTOR_OWNED", "ACTOR_VISIBLE") } &&
                scenario.forbiddenEvidence.all { it.visibility == "AUTHOR_ONLY" }
        }) { "NPC-ACTOR-02 evidence visibility is invalid." }

        val packetId = "npc-actor02-hard-${sha256("${suite.suiteId}:${suite.randomizationSeed}").take(12)}"
        val actorAIds = suite.scenarios
            .sortedBy { sha256("${suite.randomizationSeed}:${it.scenarioId}") }
            .take(suite.scenarios.size / 2)
            .map { it.scenarioId }
            .toSet()
        val packetItems = mutableListOf<NpcActorHardBlindReviewItem>()
        val answers = mutableListOf<NpcActorHardBlindAnswer>()
        suite.scenarios.forEach { scenario ->
            val reviewId = "review-${scenario.scenarioId}"
            val actorIsA = scenario.scenarioId in actorAIds
            val control = NpcActorHardBlindReviewOption(scenario.naturalControlCandidate.turns)
            val actor = NpcActorHardBlindReviewOption(scenario.actorProfileCandidate.turns)
            packetItems += NpcActorHardBlindReviewItem(
                reviewId = reviewId,
                scenarioId = scenario.scenarioId,
                title = scenario.title,
                characterBrief = scenario.reviewerCharacterBrief,
                sceneContext = scenario.sceneContext,
                sharedOpeningTurns = scenario.sharedOpeningTurns,
                optionA = if (actorIsA) actor else control,
                optionB = if (actorIsA) control else actor,
                ratingDimensions = scenario.ratingDimensions,
                ratingPrompt = "選出更像這個角色在連續對話中自然反應的一版；可選 A、B、TIE 或 BOTH_FAIL，並標記知識越界、性格漂移或修復失敗。",
            )
            answers += NpcActorHardBlindAnswer(reviewId, if (actorIsA) "A" else "B")
        }

        val actorACount = answers.count { it.actorOption == "A" }
        val decision = if (
            turnViolations == 0 && strawmanPatterns == 0 && forbiddenViolations == 0 &&
            lengthViolations == 0 && actorACount == suite.scenarios.size / 2
        ) {
            "TECHNICAL_PASS_HUMAN_REVIEW_PENDING"
        } else {
            "TECHNICAL_FAIL"
        }
        return NpcActorHardBlindArtifacts(
            report = NpcActorHardBlindTechnicalReport(
                suiteId = suite.suiteId,
                scenarioCount = suite.scenarios.size,
                profileCount = suite.scenarios.map { it.actorId }.distinct().size,
                categoryCount = suite.scenarios.map { it.category }.distinct().size,
                actorOptionACount = actorACount,
                actorOptionBCount = answers.size - actorACount,
                turnStructureViolationCount = turnViolations,
                strawmanPatternCount = strawmanPatterns,
                forbiddenInformationViolationCount = forbiddenViolations,
                lengthBalanceViolationCount = lengthViolations,
                maximumSpokenLengthRatio = lengthRatios.maxOrNull() ?: 1.0,
                technicalDecision = decision,
                humanBlindReviewStatus = "HUMAN_BLIND_REVIEW_PENDING",
                claimBoundary = listOf(
                    "This package evaluates manually authored offline fixtures, not live model quality.",
                    "A Product Owner result may guide project direction but does not prove general human likeness.",
                    "No live API, production rollout, SQLite write or Canon promotion is authorized.",
                ),
                findings = listOf(
                    "Both candidate paths use concise, plausible Traditional Chinese dialogue without the NPC-ACTOR-01 explanatory strawman.",
                    "Every scene contains 3-5 conversational turns and both options share the same interlocutor turns.",
                    "A/B identity is deterministically randomized and balanced 6/6; the answer key remains separate.",
                ),
            ),
            blindReviewPacket = NpcActorHardBlindReviewPacket(packetId = packetId, items = packetItems),
            blindReviewAnswerKey = NpcActorHardBlindAnswerKey(
                packetId = packetId,
                randomizationSeed = suite.randomizationSeed,
                answers = answers,
            ),
        )
    }

    fun validateHumanReviewResult(
        packet: NpcActorHardBlindReviewPacket,
        result: NpcActorHardBlindHumanReviewResult,
    ) {
        require(result.packetId == packet.packetId) { "Human review result packet id does not match." }
        require(result.status == "COMPLETE_BEFORE_ANSWER_KEY_DISCLOSURE") {
            "Human review result must be complete before answer-key disclosure."
        }
        require(result.reviewerRole == "PRODUCT_OWNER") { "NPC-ACTOR-02 requires the Product Owner review gate." }
        require(!result.answerKeyOpenedBeforeCompletion) { "Answer key was opened before review completion." }
        require(result.ratings.size == packet.items.size) { "Every blind-review item requires exactly one rating." }
        require(result.ratings.map { it.reviewId }.distinct().size == result.ratings.size) {
            "Human review result contains duplicate review ids."
        }
        require(result.ratings.map { it.reviewId }.toSet() == packet.items.map { it.reviewId }.toSet()) {
            "Human review result ids do not match the packet."
        }
        val allowedSelections = setOf("A", "B", "TIE", "BOTH_FAIL")
        val allowedDimensions = setOf(
            "NATURALNESS",
            "VOICE_CONSISTENCY",
            "MOTIVE_COHERENCE",
            "KNOWLEDGE_BOUNDARY",
            "CONTINUITY",
            "MULTI_TURN_REPAIR",
        )
        require(result.ratings.all { it.selection in allowedSelections }) { "Human review selection is invalid." }
        require(result.ratings.all { rating ->
            rating.flaggedDimensions.distinct().size == rating.flaggedDimensions.size &&
                rating.flaggedDimensions.all { it in allowedDimensions }
        }) { "Human review dimension flags are invalid." }
    }

    private fun turnStructureValid(scenario: NpcActorHardBlindScenario): Boolean {
        val control = scenario.naturalControlCandidate.turns
        val actor = scenario.actorProfileCandidate.turns
        val totalControl = scenario.sharedOpeningTurns.size + control.size
        val totalActor = scenario.sharedOpeningTurns.size + actor.size
        if (totalControl !in 3..5 || totalActor !in 3..5 || control.size != actor.size) return false
        if (scenario.naturalControlCandidate.sourceType != "NATURAL_CONTROL_FIXTURE") return false
        if (scenario.actorProfileCandidate.sourceType != "ACTOR_PROFILE_FIXTURE") return false
        return control.indices.all { index ->
            val c = control[index]
            val a = actor[index]
            c.speakerId == a.speakerId && c.speakerLabel == a.speakerLabel &&
                (c.speakerId == scenario.actorSpeakerId || (c.utterance == a.utterance && c.nonverbal == a.nonverbal))
        }
    }

    private fun strawmanPatternCount(scenario: NpcActorHardBlindScenario): Int =
        listOf(scenario.naturalControlCandidate, scenario.actorProfileCandidate).sumOf { candidate ->
            val text = candidate.turns.joinToString(" ") { "${it.utterance} ${it.nonverbal}" }
            strawmanPatterns.count { text.contains(it, ignoreCase = true) }
        }

    private fun forbiddenInformationViolationCount(scenario: NpcActorHardBlindScenario): Int {
        val forbidden = scenario.forbiddenEvidence.map { it.summary }.filter { it.isNotBlank() }
        return listOf(scenario.naturalControlCandidate, scenario.actorProfileCandidate).sumOf { candidate ->
            val text = candidate.turns.joinToString(" ") { "${it.utterance} ${it.nonverbal}" }
            forbidden.count { token -> text.contains(token, ignoreCase = true) }
        }
    }

    private fun spokenLengthRatio(scenario: NpcActorHardBlindScenario): Double {
        val control = spokenLength(scenario.naturalControlCandidate, scenario.actorSpeakerId)
        val actor = spokenLength(scenario.actorProfileCandidate, scenario.actorSpeakerId)
        if (control == 0 && actor == 0) return 1.0
        if (min(control, actor) == 0) return Double.POSITIVE_INFINITY
        return max(control, actor).toDouble() / min(control, actor).toDouble()
    }

    private fun spokenLength(candidate: NpcActorHardBlindCandidate, actorSpeakerId: String): Int =
        candidate.turns.filter { it.speakerId == actorSpeakerId }.sumOf { it.utterance.length }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte) }

    private companion object {
        val strawmanPatterns = listOf(
            "根據目前",
            "我的目標是",
            "我理解你的感受",
            "綜上所述",
            "作為一個AI",
            "身為一個AI",
            "保持標準站姿",
        )
    }
}
