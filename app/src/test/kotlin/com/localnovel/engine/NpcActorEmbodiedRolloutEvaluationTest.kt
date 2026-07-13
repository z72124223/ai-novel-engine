package com.localnovel.engine

import com.localnovel.engine.model.NpcActorCalibrationPolicy
import com.localnovel.engine.model.NpcActorCalibrationSuite
import com.localnovel.engine.model.NpcActorHardBlindSuite
import com.localnovel.engine.model.NpcEmbodiedHumanRating
import com.localnovel.engine.model.NpcEmbodiedHumanReviewResult
import com.localnovel.engine.model.NpcEmbodiedReviewPacket
import com.localnovel.engine.model.NpcEmbodiedRolloutSuite
import com.localnovel.engine.service.NpcActorCalibrationFixtureFactory
import com.localnovel.engine.service.NpcActorEmbodiedRolloutEvaluationService
import com.localnovel.engine.service.NpcActorEmbodiedRolloutFixtureFactory
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

class NpcActorEmbodiedRolloutEvaluationTest {
    private val service = NpcActorEmbodiedRolloutEvaluationService()
    private val policy = NpcActorCalibrationFixtureFactory.createPolicy()
    private val actor02 = NpcActorHardBlindFixtureFactory.create()
    private val actor03 = NpcActorCalibrationFixtureFactory.createSuite()
    private val suite = NpcActorEmbodiedRolloutFixtureFactory.create()

    @Test
    fun `four complete scenes pass spatial temporal state and anonymity gates`() {
        val artifacts = service.evaluate(policy, suite, actor02, actor03)

        assertEquals(4, artifacts.report.scenarioCount)
        assertEquals(4, artifacts.report.profileCount)
        assertEquals(12, artifacts.report.beatCount)
        assertEquals(2, artifacts.report.embodiedOptionACount)
        assertEquals(2, artifacts.report.embodiedOptionBCount)
        assertEquals(0, artifacts.report.unknownReferenceViolationCount)
        assertEquals(0, artifacts.report.visibilityViolationCount)
        assertEquals(0, artifacts.report.adjacencyViolationCount)
        assertEquals(0, artifacts.report.timingViolationCount)
        assertEquals(0, artifacts.report.stateContinuityViolationCount)
        assertEquals(0, artifacts.report.requiredBehaviorViolationCount)
        assertEquals(0, artifacts.report.forbiddenInformationViolationCount)
        assertEquals("TECHNICAL_PASS_HUMAN_REVIEW_PENDING", artifacts.report.technicalDecision)
        assertTrue(artifacts.report.maximumSpokenLengthRatio <= 1.25)
    }

    @Test
    fun `review packet is deterministic and does not reveal source or evidence`() {
        val first = service.evaluate(policy, suite, actor02, actor03)
        val second = service.evaluate(policy, suite, actor02, actor03)
        val packetJson = NpcActorJson.strict.encodeToString(first.blindReviewPacket)

        assertEquals(first.blindReviewPacket, second.blindReviewPacket)
        assertEquals(first.blindReviewAnswerKey, second.blindReviewAnswerKey)
        assertFalse(packetJson.contains("sourceType"))
        assertFalse(packetJson.contains("actorOwnedEvidence"))
        assertFalse(packetJson.contains("forbiddenEvidence"))
        assertFalse(packetJson.contains("randomizationSeed"))
        assertFalse(packetJson.contains("EMBODIED_ACTOR_TRAJECTORY"))
        assertFalse(packetJson.contains("AUTHOR_ONLY_"))
    }

    @Test
    fun `second policy revision and prior scenario reuse are rejected`() {
        assertFailsWith<IllegalArgumentException> {
            service.evaluate(policy.copy(revisionNumber = 2), suite, actor02, actor03)
        }
        val reused = suite.scenarios.first().copy(scenarioId = actor03.scenarios.first().scenarioId)
        assertFailsWith<IllegalArgumentException> {
            evaluateWith(reused)
        }
    }

    @Test
    fun `non adjacent movement and invisible perception are rejected`() {
        val base = suite.scenarios.first()
        val badMoveStep = base.embodiedActorTrajectory.steps.first().copy(
            stateChanges = base.embodiedActorTrajectory.steps.first().stateChanges.map { change ->
                if (change.entityId == base.actorId && change.field == "zoneId") change.copy(after = "kitchen") else change
            },
        )
        val badMove = base.copy(
            embodiedActorTrajectory = base.embodiedActorTrajectory.copy(
                steps = listOf(badMoveStep) + base.embodiedActorTrajectory.steps.drop(1),
            ),
        )
        assertFailsWith<IllegalArgumentException> { evaluateWith(badMove) }

        val dock = suite.scenarios.last()
        val invisibleStep = dock.naturalControlTrajectory.steps.first().copy(
            perceivedEntityIds = dock.naturalControlTrajectory.steps.first().perceivedEntityIds + "deckhand",
        )
        val invisible = dock.copy(
            naturalControlTrajectory = dock.naturalControlTrajectory.copy(
                steps = listOf(invisibleStep) + dock.naturalControlTrajectory.steps.drop(1),
            ),
        )
        assertFailsWith<IllegalArgumentException> {
            service.evaluate(policy, suite.copy(scenarios = suite.scenarios.dropLast(1) + invisible), actor02, actor03)
        }
    }

    @Test
    fun `broken before state and exposition only trajectory are rejected`() {
        val base = suite.scenarios.first()
        val badStateStep = base.naturalControlTrajectory.steps.first().copy(
            stateChanges = base.naturalControlTrajectory.steps.first().stateChanges.mapIndexed { index, change ->
                if (index == 0) change.copy(before = "不存在的前置狀態") else change
            },
        )
        assertFailsWith<IllegalArgumentException> {
            evaluateWith(
                base.copy(
                    naturalControlTrajectory = base.naturalControlTrajectory.copy(
                        steps = listOf(badStateStep) + base.naturalControlTrajectory.steps.drop(1),
                    ),
                ),
            )
        }

        val noSilence = base.copy(
            naturalControlTrajectory = base.naturalControlTrajectory.copy(
                steps = base.naturalControlTrajectory.steps.map { it.copy(utterance = if (it.utterance.isBlank()) "我先說明。" else it.utterance) },
            ),
        )
        assertFailsWith<IllegalArgumentException> { evaluateWith(noSilence) }
    }

    @Test
    fun `author secret and strawman wording are rejected`() {
        val base = suite.scenarios.first()
        val secret = base.copy(
            naturalControlTrajectory = base.naturalControlTrajectory.copy(
                steps = base.naturalControlTrajectory.steps.mapIndexed { index, step ->
                    if (index == 0) step.copy(utterance = base.forbiddenEvidence.first().summary) else step
                },
            ),
        )
        assertFailsWith<IllegalArgumentException> { evaluateWith(secret) }

        val strawman = base.copy(
            naturalControlTrajectory = base.naturalControlTrajectory.copy(
                steps = base.naturalControlTrajectory.steps.mapIndexed { index, step ->
                    if (index == 0) step.copy(utterance = "根據我的設定，我必須展現勇敢。") else step
                },
            ),
        )
        assertFailsWith<IllegalArgumentException> { evaluateWith(strawman) }
    }

    @Test
    fun `cli rebuilds artifacts and product owner review contract is strict`() {
        val dir = Files.createTempDirectory("npc-actor04-cli-")
        val policyPath = write(dir.resolve("policy.json"), policy)
        val actor02Path = write(dir.resolve("actor02.json"), actor02)
        val actor03Path = write(dir.resolve("actor03.json"), actor03)
        val suitePath = dir.resolve("suite.json")
        val reportPath = dir.resolve("report.json")
        val packetPath = dir.resolve("packet.json")
        val keyPath = dir.resolve("key.json")

        assertEquals(0, Cli().run(listOf("create-npc-actor-embodied-suite", "--policy", policyPath.toString(), "--out", suitePath.toString())))
        assertEquals(
            0,
            Cli().run(
                listOf(
                    "evaluate-npc-actor-embodied-rollout", "--policy", policyPath.toString(),
                    "--suite", suitePath.toString(), "--actor02-suite", actor02Path.toString(),
                    "--actor03-suite", actor03Path.toString(), "--report", reportPath.toString(),
                    "--blind", packetPath.toString(), "--answer-key", keyPath.toString(),
                ),
            ),
        )
        assertEquals(suite, read<NpcEmbodiedRolloutSuite>(suitePath))
        val packet = read<NpcEmbodiedReviewPacket>(packetPath)
        val complete = NpcEmbodiedHumanReviewResult(
            packetId = packet.packetId,
            ratings = packet.items.map { NpcEmbodiedHumanRating(it.reviewId, "TIE") },
        )
        service.validateHumanReviewResult(packet, complete)
        assertFailsWith<IllegalArgumentException> {
            service.validateHumanReviewResult(packet, complete.copy(answerKeyOpenedBeforeCompletion = true))
        }
        assertFailsWith<IllegalArgumentException> {
            service.validateHumanReviewResult(packet, complete.copy(ratings = complete.ratings.dropLast(1)))
        }
    }

    private fun evaluateWith(replacement: com.localnovel.engine.model.NpcEmbodiedRolloutScenario) {
        service.evaluate(policy, suite.copy(scenarios = listOf(replacement) + suite.scenarios.drop(1)), actor02, actor03)
    }

    private inline fun <reified T> write(path: Path, value: T): Path {
        Files.writeString(path, NpcActorJson.strict.encodeToString(value))
        return path
    }

    private inline fun <reified T> read(path: Path): T =
        NpcActorJson.strict.decodeFromString(Files.readString(path))
}
