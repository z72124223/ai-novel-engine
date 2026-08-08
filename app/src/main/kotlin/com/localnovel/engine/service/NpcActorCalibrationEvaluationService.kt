package com.localnovel.engine.service

import com.localnovel.engine.model.NpcActorCalibrationAnswer
import com.localnovel.engine.model.NpcActorCalibrationAnswerKey
import com.localnovel.engine.model.NpcActorCalibrationCandidate
import com.localnovel.engine.model.NpcActorCalibrationHumanReviewResult
import com.localnovel.engine.model.NpcActorCalibrationPolicy
import com.localnovel.engine.model.NpcActorCalibrationReviewItem
import com.localnovel.engine.model.NpcActorCalibrationReviewOption
import com.localnovel.engine.model.NpcActorCalibrationReviewPacket
import com.localnovel.engine.model.NpcActorCalibrationScenario
import com.localnovel.engine.model.NpcActorCalibrationSuite
import com.localnovel.engine.model.NpcActorCalibrationTechnicalReport
import com.localnovel.engine.model.NpcActorHardBlindScenario
import com.localnovel.engine.model.NpcActorHardBlindSuite
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

data class NpcActorCalibrationArtifacts(
    val report: NpcActorCalibrationTechnicalReport,
    val blindReviewPacket: NpcActorCalibrationReviewPacket,
    val blindReviewAnswerKey: NpcActorCalibrationAnswerKey,
)

class NpcActorCalibrationEvaluationService {
    fun evaluate(
        policy: NpcActorCalibrationPolicy,
        suite: NpcActorCalibrationSuite,
        developmentSuite: NpcActorHardBlindSuite,
    ): NpcActorCalibrationArtifacts {
        validatePolicy(policy)
        require(suite.policyId == policy.policyId) { "Calibration suite policy id does not match." }
        require(suite.status == "NON_CANON_HOLDOUT_READY") { "Calibration suite status is invalid." }
        require(suite.fixtureOrigin == "MANUALLY_AUTHORED_OFFLINE_HOLDOUT_FIXTURE") {
            "Calibration fixtures must remain labeled as manually authored offline holdout fixtures."
        }
        require(suite.scenarios.size == 8) { "NPC-ACTOR-03 requires exactly 8 new holdout scenarios." }
        require(suite.scenarios.map { it.scenarioId }.distinct().size == 8) { "Holdout scenario ids must be unique." }
        require(suite.scenarios.all { it.boundary == "NON_CANON_EVALUATION" }) {
            "NPC-ACTOR-03 holdout scenarios must remain non-Canon."
        }
        require(suite.scenarios.all { it.policyId == policy.policyId }) { "Every scenario must reference the policy." }
        val profileCounts = suite.scenarios.groupingBy { it.actorId }.eachCount()
        require(profileCounts.size == 4 && profileCounts.values.all { it == 2 }) {
            "NPC-ACTOR-03 requires four profiles with exactly two holdout scenarios each."
        }
        validateSignalVocabulary(suite)

        val developmentIdOverlap = suite.scenarios.map { it.scenarioId }.toSet()
            .intersect(developmentSuite.scenarios.map { it.scenarioId }.toSet()).size
        val developmentFingerprints = developmentSuite.scenarios.map(::fingerprint).toSet()
        val contentOverlap = suite.scenarios.count { fingerprint(it) in developmentFingerprints }
        require(developmentIdOverlap == 0 && contentOverlap == 0) {
            "NPC-ACTOR-02 development scenes cannot be reused as NPC-ACTOR-03 holdout evidence."
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
        require(turnViolations == 0) { "Candidate paths must match and contain 3-5 total turns." }
        require(strawmanPatterns == 0) { "Holdout candidates contain label-revealing strawman phrasing." }
        require(forbiddenViolations == 0) { "Holdout candidates referenced forbidden evidence." }
        require(lengthViolations == 0) {
            "Candidate spoken lengths must stay within 25 percent: ${lengthViolationDetails.joinToString(", ")}"
        }
        require(suite.scenarios.all { scenario ->
            scenario.actorOwnedEvidence.all { it.visibility in allowedActorVisibility } &&
                scenario.forbiddenEvidence.all { it.visibility == "AUTHOR_ONLY" }
        }) { "Holdout evidence visibility is invalid." }

        val packetId = "npc-actor03-calibration-${sha256("${suite.suiteId}:${suite.randomizationSeed}").take(12)}"
        val adaptiveAIds = suite.scenarios
            .sortedBy { sha256("${suite.randomizationSeed}:${it.scenarioId}") }
            .take(4)
            .map { it.scenarioId }
            .toSet()
        val items = mutableListOf<NpcActorCalibrationReviewItem>()
        val answers = mutableListOf<NpcActorCalibrationAnswer>()
        suite.scenarios.forEach { scenario ->
            val reviewId = "review-${scenario.scenarioId}"
            val adaptiveIsA = scenario.scenarioId in adaptiveAIds
            val control = NpcActorCalibrationReviewOption(scenario.naturalControlCandidate.turns)
            val adaptive = NpcActorCalibrationReviewOption(scenario.adaptiveActorCandidate.turns)
            items += NpcActorCalibrationReviewItem(
                reviewId = reviewId,
                scenarioId = scenario.scenarioId,
                title = scenario.title,
                characterBrief = scenario.reviewerCharacterBrief,
                sceneContext = scenario.sceneContext,
                sharedOpeningTurns = scenario.sharedOpeningTurns,
                optionA = if (adaptiveIsA) adaptive else control,
                optionB = if (adaptiveIsA) control else adaptive,
                ratingDimensions = scenario.ratingDimensions,
                ratingPrompt = "請選較自然、像真人且符合角色與情境的版本：A、B、TIE 或 BOTH_FAIL；需要時再標記問題。",
            )
            answers += NpcActorCalibrationAnswer(reviewId, if (adaptiveIsA) "A" else "B")
        }
        val adaptiveACount = answers.count { it.adaptiveActorOption == "A" }
        require(adaptiveACount == 4) { "NPC-ACTOR-03 A/B identity must be balanced 4/4." }

        return NpcActorCalibrationArtifacts(
            report = NpcActorCalibrationTechnicalReport(
                suiteId = suite.suiteId,
                policyId = policy.policyId,
                scenarioCount = suite.scenarios.size,
                profileCount = profileCounts.size,
                adaptiveOptionACount = adaptiveACount,
                adaptiveOptionBCount = answers.size - adaptiveACount,
                developmentScenarioIdOverlapCount = developmentIdOverlap,
                developmentContentFingerprintOverlapCount = contentOverlap,
                turnStructureViolationCount = turnViolations,
                strawmanPatternCount = strawmanPatterns,
                forbiddenInformationViolationCount = forbiddenViolations,
                lengthBalanceViolationCount = lengthViolations,
                maximumSpokenLengthRatio = lengthRatios.maxOrNull() ?: 1.0,
                technicalDecision = "TECHNICAL_PASS_HUMAN_REVIEW_PENDING",
                humanBlindReviewStatus = "HUMAN_BLIND_REVIEW_PENDING",
                claimBoundary = policy.claimBoundary,
            ),
            blindReviewPacket = NpcActorCalibrationReviewPacket(packetId = packetId, items = items),
            blindReviewAnswerKey = NpcActorCalibrationAnswerKey(
                packetId = packetId,
                randomizationSeed = suite.randomizationSeed,
                answers = answers,
            ),
        )
    }

    fun validateHumanReviewResult(
        packet: NpcActorCalibrationReviewPacket,
        result: NpcActorCalibrationHumanReviewResult,
    ) {
        require(result.packetId == packet.packetId) { "Human review result packet id does not match." }
        require(result.status == "COMPLETE_BEFORE_ANSWER_KEY_DISCLOSURE") {
            "Human review result must be complete before answer-key disclosure."
        }
        require(result.reviewerRole == "PRODUCT_OWNER") { "NPC-ACTOR-03 requires the Product Owner review gate." }
        require(!result.answerKeyOpenedBeforeCompletion) { "Answer key was opened before review completion." }
        require(result.ratings.size == packet.items.size) { "Every review item requires exactly one rating." }
        require(result.ratings.map { it.reviewId }.distinct().size == result.ratings.size) {
            "Human review result contains duplicate review ids."
        }
        require(result.ratings.map { it.reviewId }.toSet() == packet.items.map { it.reviewId }.toSet()) {
            "Human review result ids do not match the packet."
        }
        require(result.ratings.all { it.selection in allowedSelections }) { "Human review selection is invalid." }
        require(result.ratings.all { rating ->
            rating.flaggedDimensions.distinct().size == rating.flaggedDimensions.size &&
                rating.flaggedDimensions.all { it in allowedDimensions }
        }) { "Human review dimension flags are invalid." }
    }

    private fun validatePolicy(policy: NpcActorCalibrationPolicy) {
        require(policy.policyId == NpcActorCalibrationFixtureFactory.POLICY_ID) { "Unexpected calibration policy id." }
        require(policy.revisionNumber == 1) { "NPC-ACTOR-03 permits exactly one calibration revision." }
        require(policy.status == "EVALUATION_ONLY") { "Calibration policy must remain evaluation-only." }
        require(policy.decisionOrder == requiredDecisionOrder) { "Calibration decision order is incomplete or reordered." }
        require(policy.rules.map { it.ruleId }.toSet() == requiredRuleIds) { "Calibration policy rules are incomplete." }
    }

    private fun validateSignalVocabulary(suite: NpcActorCalibrationSuite) {
        require(suite.scenarios.all { it.calibrationSignals.immediateNeed.isNotBlank() }) {
            "Every holdout scenario requires an immediate need."
        }
        require(suite.scenarios.all { it.calibrationSignals.relationshipDistance in relationshipValues }) {
            "Relationship-distance signal is invalid."
        }
        require(suite.scenarios.all { it.calibrationSignals.audiencePressure in levelValues }) {
            "Audience-pressure signal is invalid."
        }
        require(suite.scenarios.all { it.calibrationSignals.urgency in levelValues }) { "Urgency signal is invalid." }
        require(suite.scenarios.all { it.calibrationSignals.expressionLevel in expressionValues }) {
            "Expression-level signal is invalid."
        }
    }

    private fun turnStructureValid(scenario: NpcActorCalibrationScenario): Boolean {
        val control = scenario.naturalControlCandidate.turns
        val adaptive = scenario.adaptiveActorCandidate.turns
        val controlTotal = scenario.sharedOpeningTurns.size + control.size
        val adaptiveTotal = scenario.sharedOpeningTurns.size + adaptive.size
        if (controlTotal !in 3..5 || adaptiveTotal !in 3..5 || control.size != adaptive.size) return false
        if (scenario.naturalControlCandidate.sourceType != "NATURAL_CONTROL_FIXTURE") return false
        if (scenario.adaptiveActorCandidate.sourceType != "ADAPTIVE_ACTOR_FIXTURE") return false
        return control.indices.all { index ->
            val c = control[index]
            val a = adaptive[index]
            c.speakerId == a.speakerId && c.speakerLabel == a.speakerLabel &&
                (c.speakerId == scenario.actorSpeakerId || (c.utterance == a.utterance && c.nonverbal == a.nonverbal))
        }
    }

    private fun strawmanPatternCount(scenario: NpcActorCalibrationScenario): Int =
        listOf(scenario.naturalControlCandidate, scenario.adaptiveActorCandidate).sumOf { candidate ->
            val text = candidate.turns.joinToString(" ") { "${it.utterance} ${it.nonverbal}" }
            strawmanPatterns.count { text.contains(it, ignoreCase = true) }
        }

    private fun forbiddenInformationViolationCount(scenario: NpcActorCalibrationScenario): Int {
        val forbidden = scenario.forbiddenEvidence.map { it.summary }.filter { it.isNotBlank() }
        return listOf(scenario.naturalControlCandidate, scenario.adaptiveActorCandidate).sumOf { candidate ->
            val text = candidate.turns.joinToString(" ") { "${it.utterance} ${it.nonverbal}" }
            forbidden.count { text.contains(it, ignoreCase = true) }
        }
    }

    private fun spokenLengthRatio(scenario: NpcActorCalibrationScenario): Double {
        val control = spokenLength(scenario.naturalControlCandidate, scenario.actorSpeakerId)
        val adaptive = spokenLength(scenario.adaptiveActorCandidate, scenario.actorSpeakerId)
        if (control == 0 && adaptive == 0) return 1.0
        if (min(control, adaptive) == 0) return Double.POSITIVE_INFINITY
        return max(control, adaptive).toDouble() / min(control, adaptive).toDouble()
    }

    private fun spokenLength(candidate: NpcActorCalibrationCandidate, actorSpeakerId: String): Int =
        candidate.turns.filter { it.speakerId == actorSpeakerId }.sumOf { it.utterance.length }

    private fun fingerprint(scenario: NpcActorCalibrationScenario): String = sha256(
        normalize(
            listOf(
                scenario.title,
                scenario.sceneContext,
                scenario.sharedOpeningTurns.joinToString("|") { it.utterance },
                scenario.naturalControlCandidate.turns
                    .filter { it.speakerId != scenario.actorSpeakerId }
                    .joinToString("|") { it.utterance },
            ).joinToString("||"),
        ),
    )

    private fun fingerprint(scenario: NpcActorHardBlindScenario): String = sha256(
        normalize(
            listOf(
                scenario.title,
                scenario.sceneContext,
                scenario.sharedOpeningTurns.joinToString("|") { it.utterance },
                scenario.naturalControlCandidate.turns
                    .filter { it.speakerId != scenario.actorSpeakerId }
                    .joinToString("|") { it.utterance },
            ).joinToString("||"),
        ),
    )

    private fun normalize(value: String): String = value.lowercase(Locale.ROOT).replace(Regex("\\s+"), "")

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte) }

    private companion object {
        val allowedActorVisibility = setOf("ACTOR_OWNED", "ACTOR_VISIBLE")
        val allowedSelections = setOf("A", "B", "TIE", "BOTH_FAIL")
        val allowedDimensions = setOf(
            "NATURALNESS", "VOICE_CONSISTENCY", "MOTIVE_COHERENCE",
            "KNOWLEDGE_BOUNDARY", "CONTINUITY", "MULTI_TURN_REPAIR",
        )
        val relationshipValues = setOf("STRANGER", "CUSTOMER", "KNOWN_PROFESSIONAL", "WORKING_ALLY", "SUBORDINATE", "APPRENTICE", "LONG_TERM_PATIENT", "EMPLOYEE")
        val levelValues = setOf("LOW", "MEDIUM", "HIGH")
        val expressionValues = setOf("MINIMAL", "LOW", "RESTRAINED", "MEDIUM")
        val requiredDecisionOrder = listOf(
            "IMMEDIATE_NEED", "KNOWLEDGE_AND_SAFETY_BOUNDARY", "RELATIONSHIP_AND_AUDIENCE",
            "PRIVATE_MOTIVE_EXPRESSION_CHANNEL", "VOICE_INTENSITY",
        )
        val requiredRuleIds = setOf(
            "need-first", "action-before-exposition", "scale-by-context",
            "neutral-is-valid", "preserve-withholding", "no-signature-every-turn",
        )
        val strawmanPatterns = listOf(
            "身為一個", "我的人格設定", "根據我的設定", "作為一個AI", "身為一個AI",
            "我必須展現", "這就是我的個性",
        )
    }
}
