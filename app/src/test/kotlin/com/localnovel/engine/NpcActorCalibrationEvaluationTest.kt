package com.localnovel.engine

import com.localnovel.engine.model.NpcActorCalibrationCandidate
import com.localnovel.engine.model.NpcActorCalibrationHumanRating
import com.localnovel.engine.model.NpcActorCalibrationHumanReviewResult
import com.localnovel.engine.model.NpcActorCalibrationPolicy
import com.localnovel.engine.model.NpcActorCalibrationReviewPacket
import com.localnovel.engine.model.NpcActorCalibrationSuite
import com.localnovel.engine.model.NpcActorCalibrationTechnicalReport
import com.localnovel.engine.model.NpcActorCalibrationTurn
import com.localnovel.engine.model.NpcActorHardBlindSuite
import com.localnovel.engine.service.NpcActorCalibrationEvaluationService
import com.localnovel.engine.service.NpcActorCalibrationFixtureFactory
import com.localnovel.engine.service.NpcActorHardBlindFixtureFactory
import com.localnovel.engine.service.NpcActorJson
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class NpcActorCalibrationEvaluationTest {
    private val service = NpcActorCalibrationEvaluationService()
    private val policy = NpcActorCalibrationFixtureFactory.createPolicy()
    private val suite = NpcActorCalibrationFixtureFactory.createSuite()
    private val development = NpcActorHardBlindFixtureFactory.create()

    @Test
    fun `single calibration passes technical gates with new balanced holdout`() {
        val artifacts = service.evaluate(policy, suite, development)

        assertEquals(8, suite.scenarios.size)
        assertEquals(mapOf("npc-eval-mira" to 2, "npc-eval-yan" to 2, "npc-eval-lian" to 2, "npc-eval-ren" to 2), suite.scenarios.groupingBy { it.actorId }.eachCount())
        assertEquals("TECHNICAL_PASS_HUMAN_REVIEW_PENDING", artifacts.report.technicalDecision)
        assertEquals(4, artifacts.report.adaptiveOptionACount)
        assertEquals(4, artifacts.report.adaptiveOptionBCount)
        assertEquals(0, artifacts.report.developmentScenarioIdOverlapCount)
        assertEquals(0, artifacts.report.developmentContentFingerprintOverlapCount)
        assertTrue(artifacts.report.maximumSpokenLengthRatio <= 1.25)
    }

    @Test
    fun `anonymous packet is deterministic and hides calibration internals`() {
        val first = service.evaluate(policy, suite, development)
        val second = service.evaluate(policy, suite, development)
        val packetJson = NpcActorJson.strict.encodeToString(first.blindReviewPacket)

        assertEquals(first.blindReviewPacket, second.blindReviewPacket)
        assertEquals(first.blindReviewAnswerKey, second.blindReviewAnswerKey)
        assertFalse(packetJson.contains("policyId"))
        assertFalse(packetJson.contains("calibrationSignals"))
        assertFalse(packetJson.contains("sourceType"))
        assertFalse(packetJson.contains("actorOwnedEvidence"))
        assertFalse(packetJson.contains("forbiddenEvidence"))
        assertFalse(packetJson.contains("randomizationSeed"))
        assertFalse(packetJson.contains("ADAPTIVE_ACTOR_FIXTURE"))
    }

    @Test
    fun `second calibration revision is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            service.evaluate(policy.copy(revisionNumber = 2), suite, development)
        }
    }

    @Test
    fun `development scene content reuse is rejected`() {
        val old = development.scenarios.first()
        val current = suite.scenarios.first()
        val reused = current.copy(
            title = old.title,
            sceneContext = old.sceneContext,
            sharedOpeningTurns = old.sharedOpeningTurns.map { NpcActorCalibrationTurn(it.speakerId, it.speakerLabel, it.utterance, it.nonverbal) },
            naturalControlCandidate = current.naturalControlCandidate.copy(
                turns = current.naturalControlCandidate.turns.mapIndexed { index, turn ->
                    if (turn.speakerId != current.actorSpeakerId) {
                        val oldTurn = old.naturalControlCandidate.turns.first { it.speakerId != old.actorSpeakerId }
                        NpcActorCalibrationTurn(turn.speakerId, turn.speakerLabel, oldTurn.utterance, oldTurn.nonverbal)
                    } else turn
                },
            ),
        )

        assertFailsWith<IllegalArgumentException> {
            service.evaluate(policy, suite.copy(scenarios = listOf(reused) + suite.scenarios.drop(1)), development)
        }
    }

    @Test
    fun `unsafe structure secret strawman and length variants are rejected`() {
        val base = suite.scenarios.first()
        val badSecret = base.copy(
            adaptiveActorCandidate = base.adaptiveActorCandidate.copy(
                turns = base.adaptiveActorCandidate.turns.mapIndexed { index, turn ->
                    if (index == 0) turn.copy(utterance = base.forbiddenEvidence.first().summary) else turn
                },
            ),
        )
        assertFailsWith<IllegalArgumentException> { evaluateWith(badSecret) }

        val badStrawman = base.copy(
            adaptiveActorCandidate = base.adaptiveActorCandidate.copy(
                turns = base.adaptiveActorCandidate.turns.mapIndexed { index, turn ->
                    if (index == 0) turn.copy(utterance = "根據我的設定，我必須展現個性。") else turn
                },
            ),
        )
        assertFailsWith<IllegalArgumentException> { evaluateWith(badStrawman) }

        val badLength = base.copy(
            adaptiveActorCandidate = NpcActorCalibrationCandidate(
                sourceType = "ADAPTIVE_ACTOR_FIXTURE",
                turns = base.adaptiveActorCandidate.turns.mapIndexed { index, turn ->
                    if (turn.speakerId == base.actorSpeakerId && index == 0) turn.copy(utterance = turn.utterance.repeat(8)) else turn
                },
            ),
        )
        assertFailsWith<IllegalArgumentException> { evaluateWith(badLength) }
    }

    @Test
    fun `cli rebuilds strict artifacts and human result contract is enforced`() {
        val dir = Files.createTempDirectory("npc-actor03-cli-")
        val policyPath = write(dir.resolve("policy.json"), policy)
        val developmentPath = write(dir.resolve("development.json"), development)
        val suitePath = dir.resolve("suite.json")
        val reportPath = dir.resolve("report.json")
        val packetPath = dir.resolve("packet.json")
        val keyPath = dir.resolve("key.json")

        assertEquals(0, Cli().run(listOf("create-npc-actor-calibration-suite", "--policy", policyPath.toString(), "--out", suitePath.toString())))
        assertEquals(
            0,
            Cli().run(
                listOf(
                    "evaluate-npc-actor-calibration", "--policy", policyPath.toString(),
                    "--suite", suitePath.toString(), "--development-suite", developmentPath.toString(),
                    "--report", reportPath.toString(), "--blind", packetPath.toString(),
                    "--answer-key", keyPath.toString(),
                ),
            ),
        )

        val rebuiltSuite = read<NpcActorCalibrationSuite>(suitePath)
        val report = read<NpcActorCalibrationTechnicalReport>(reportPath)
        val packet = read<NpcActorCalibrationReviewPacket>(packetPath)
        assertEquals(suite, rebuiltSuite)
        assertEquals("TECHNICAL_PASS_HUMAN_REVIEW_PENDING", report.technicalDecision)

        val complete = NpcActorCalibrationHumanReviewResult(
            packetId = packet.packetId,
            ratings = packet.items.map { NpcActorCalibrationHumanRating(it.reviewId, "TIE") },
        )
        service.validateHumanReviewResult(packet, complete)
        assertFailsWith<IllegalArgumentException> {
            service.validateHumanReviewResult(packet, complete.copy(answerKeyOpenedBeforeCompletion = true))
        }
        assertFailsWith<IllegalArgumentException> {
            service.validateHumanReviewResult(packet, complete.copy(ratings = complete.ratings.dropLast(1)))
        }
    }

    private fun evaluateWith(replacement: com.localnovel.engine.model.NpcActorCalibrationScenario) {
        service.evaluate(policy, suite.copy(scenarios = listOf(replacement) + suite.scenarios.drop(1)), development)
    }

    private inline fun <reified T> write(path: Path, value: T): Path {
        Files.writeString(path, NpcActorJson.strict.encodeToString(value))
        return path
    }

    private inline fun <reified T> read(path: Path): T =
        NpcActorJson.strict.decodeFromString(Files.readString(path))
}
