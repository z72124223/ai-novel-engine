package com.localnovel.engine

import com.localnovel.engine.model.NpcActorHardBlindAnswerKey
import com.localnovel.engine.model.NpcActorHardBlindCandidate
import com.localnovel.engine.model.NpcActorHardBlindHumanRating
import com.localnovel.engine.model.NpcActorHardBlindHumanReviewResult
import com.localnovel.engine.model.NpcActorHardBlindReviewPacket
import com.localnovel.engine.model.NpcActorHardBlindSuite
import com.localnovel.engine.model.NpcActorHardBlindTechnicalReport
import com.localnovel.engine.service.NpcActorHardBlindEvaluationService
import com.localnovel.engine.service.NpcActorHardBlindFixtureFactory
import com.localnovel.engine.service.NpcActorJson
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class NpcActorHardBlindEvaluationTest {
    private val service = NpcActorHardBlindEvaluationService()

    @Test
    fun `fixed hard blind suite passes technical gates and balances identities`() {
        val suite = NpcActorHardBlindFixtureFactory.create()
        val artifacts = service.evaluate(suite)

        assertEquals(12, suite.scenarios.size)
        assertEquals(4, artifacts.report.profileCount)
        assertEquals("TECHNICAL_PASS_HUMAN_REVIEW_PENDING", artifacts.report.technicalDecision)
        assertEquals("HUMAN_BLIND_REVIEW_PENDING", artifacts.report.humanBlindReviewStatus)
        assertEquals(0, artifacts.report.turnStructureViolationCount)
        assertEquals(0, artifacts.report.strawmanPatternCount)
        assertEquals(0, artifacts.report.forbiddenInformationViolationCount)
        assertEquals(0, artifacts.report.lengthBalanceViolationCount)
        assertTrue(artifacts.report.maximumSpokenLengthRatio <= 1.25)
        assertEquals(6, artifacts.report.actorOptionACount)
        assertEquals(6, artifacts.report.actorOptionBCount)
        assertEquals(12, artifacts.blindReviewPacket.items.size)
        assertEquals(12, artifacts.blindReviewAnswerKey.answers.size)
        assertTrue(artifacts.blindReviewPacket.items.all { it.ratingDimensions.size == 6 })
        assertTrue(suite.scenarios.all { it.sharedOpeningTurns.size + it.actorProfileCandidate.turns.size in 3..5 })
    }

    @Test
    fun `packet is deterministic and does not reveal hidden source or evidence`() {
        val suite = NpcActorHardBlindFixtureFactory.create()
        val first = service.evaluate(suite)
        val second = service.evaluate(suite)
        assertEquals(first.blindReviewPacket, second.blindReviewPacket)
        assertEquals(first.blindReviewAnswerKey, second.blindReviewAnswerKey)

        val packetJson = NpcActorJson.strict.encodeToString(first.blindReviewPacket)
        assertTrue(!packetJson.contains("ACTOR_PROFILE_FIXTURE"))
        assertTrue(!packetJson.contains("NATURAL_CONTROL_FIXTURE"))
        assertTrue(!packetJson.contains("randomizationSeed"))
        assertTrue(!packetJson.contains("actorOwnedEvidence"))
        assertTrue(!packetJson.contains("forbiddenEvidence"))
        assertTrue(!packetJson.contains("AUTHOR_ONLY"))
    }

    @Test
    fun `service rejects strawman forbidden knowledge and unmatched interlocutor turns`() {
        val suite = NpcActorHardBlindFixtureFactory.create()
        val first = suite.scenarios.first()
        val strawmanTurns = first.naturalControlCandidate.turns.toMutableList().also { turns ->
            turns[0] = turns[0].copy(utterance = "根據目前情況，我可以提供完整說明。")
        }
        val strawman = replaceFirst(
            suite,
            first.copy(naturalControlCandidate = first.naturalControlCandidate.copy(turns = strawmanTurns)),
        )
        assertFailsWith<IllegalArgumentException> { service.evaluate(strawman) }

        val leakedTurns = first.actorProfileCandidate.turns.toMutableList().also { turns ->
            turns[0] = turns[0].copy(utterance = first.forbiddenEvidence.single().summary)
        }
        val leaked = replaceFirst(
            suite,
            first.copy(actorProfileCandidate = first.actorProfileCandidate.copy(turns = leakedTurns)),
        )
        assertFailsWith<IllegalArgumentException> { service.evaluate(leaked) }

        val unmatchedTurns = first.naturalControlCandidate.turns.toMutableList().also { turns ->
            turns[1] = turns[1].copy(utterance = "控制組私自改變了追問。")
        }
        val unmatched = replaceFirst(
            suite,
            first.copy(naturalControlCandidate = first.naturalControlCandidate.copy(turns = unmatchedTurns)),
        )
        assertFailsWith<IllegalArgumentException> { service.evaluate(unmatched) }
    }

    @Test
    fun `service rejects length imbalance and canon boundary`() {
        val suite = NpcActorHardBlindFixtureFactory.create()
        val first = suite.scenarios.first()
        val longTurns = first.actorProfileCandidate.turns.toMutableList().also { turns ->
            turns[0] = turns[0].copy(utterance = "這是一段刻意拉得很長而且遠超另一個候選長度的對白，用來證明長度不平衡會被技術 Gate 攔下來。")
        }
        val imbalanced = replaceFirst(
            suite,
            first.copy(actorProfileCandidate = first.actorProfileCandidate.copy(turns = longTurns)),
        )
        assertFailsWith<IllegalArgumentException> { service.evaluate(imbalanced) }

        val canon = replaceFirst(suite, first.copy(boundary = "CANON"))
        assertFailsWith<IllegalArgumentException> { service.evaluate(canon) }
    }

    @Test
    fun `cli rebuilds strict hard blind artifacts`() {
        val temp = Files.createTempDirectory("npc-actor02-hard-blind")
        val suitePath = temp.resolve("suite.json")
        val reportPath = temp.resolve("report.json")
        val packetPath = temp.resolve("packet.json")
        val answerPath = temp.resolve("answer.json")

        assertEquals(
            0,
            Cli().run(listOf("create-npc-actor-hard-blind-suite", "--out", suitePath.toString())),
        )
        assertEquals(
            0,
            Cli().run(
                listOf(
                    "evaluate-npc-actor-hard-blind",
                    "--suite", suitePath.toString(),
                    "--report", reportPath.toString(),
                    "--blind", packetPath.toString(),
                    "--answer-key", answerPath.toString(),
                ),
            ),
        )

        val suite = read<NpcActorHardBlindSuite>(suitePath)
        val report = read<NpcActorHardBlindTechnicalReport>(reportPath)
        val packet = read<NpcActorHardBlindReviewPacket>(packetPath)
        val answer = read<NpcActorHardBlindAnswerKey>(answerPath)
        assertEquals(12, suite.scenarios.size)
        assertEquals("TECHNICAL_PASS_HUMAN_REVIEW_PENDING", report.technicalDecision)
        assertEquals(12, packet.items.size)
        assertEquals(packet.packetId, answer.packetId)
    }

    @Test
    fun `human result contract requires complete ratings before answer disclosure`() {
        val artifacts = service.evaluate(NpcActorHardBlindFixtureFactory.create())
        val packet = artifacts.blindReviewPacket
        val selections = listOf("A", "A", "B", "B", "B", "A", "B", "TIE", "TIE", "B", "B", "B")
        val result = NpcActorHardBlindHumanReviewResult(
            packetId = packet.packetId,
            ratings = packet.items.mapIndexed { index, item ->
                NpcActorHardBlindHumanRating(
                    reviewId = item.reviewId,
                    selection = selections[index],
                    flaggedDimensions = emptyList(),
                )
            },
        )
        service.validateHumanReviewResult(packet, result)
        val summary = service.revealHumanReviewResult(packet, artifacts.blindReviewAnswerKey, result)
        assertEquals(4, summary.actorPreferredCount)
        assertEquals(6, summary.controlPreferredCount)
        assertEquals(2, summary.tieCount)
        assertEquals(0, summary.bothFailCount)
        assertEquals("NO_ACTOR_ADVANTAGE_OBSERVED", summary.overallDecision)
        assertFailsWith<IllegalArgumentException> {
            service.validateHumanReviewResult(packet, result.copy(answerKeyOpenedBeforeCompletion = true))
        }
        assertFailsWith<IllegalArgumentException> {
            service.validateHumanReviewResult(packet, result.copy(ratings = result.ratings.dropLast(1)))
        }
        assertFailsWith<IllegalArgumentException> {
            service.validateHumanReviewResult(
                packet,
                result.copy(ratings = listOf(result.ratings.first().copy(selection = "ACTOR")) + result.ratings.drop(1)),
            )
        }
    }

    private fun replaceFirst(
        suite: NpcActorHardBlindSuite,
        replacement: com.localnovel.engine.model.NpcActorHardBlindScenario,
    ): NpcActorHardBlindSuite = suite.copy(
        scenarios = listOf(replacement) + suite.scenarios.drop(1),
    )

    private inline fun <reified T> read(path: java.nio.file.Path): T =
        NpcActorJson.strict.decodeFromString(Files.readString(path))
}
