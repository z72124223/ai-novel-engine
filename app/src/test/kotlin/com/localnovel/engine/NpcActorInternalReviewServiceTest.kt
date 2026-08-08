package com.localnovel.engine

import com.localnovel.engine.model.NpcEmbodiedInternalDimensionScore
import com.localnovel.engine.model.NpcEmbodiedInternalRating
import com.localnovel.engine.model.NpcEmbodiedInternalReviewAggregate
import com.localnovel.engine.model.NpcEmbodiedInternalReviewResult
import com.localnovel.engine.model.NpcEmbodiedReviewPacket
import com.localnovel.engine.service.NpcActorCalibrationFixtureFactory
import com.localnovel.engine.service.NpcActorEmbodiedRolloutEvaluationService
import com.localnovel.engine.service.NpcActorEmbodiedRolloutFixtureFactory
import com.localnovel.engine.service.NpcActorHardBlindFixtureFactory
import com.localnovel.engine.service.NpcActorInternalReviewService
import com.localnovel.engine.service.NpcActorJson
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class NpcActorInternalReviewServiceTest {
    private val embodied = NpcActorEmbodiedRolloutEvaluationService().evaluate(
        NpcActorCalibrationFixtureFactory.createPolicy(),
        NpcActorEmbodiedRolloutFixtureFactory.create(),
        NpcActorHardBlindFixtureFactory.create(),
        NpcActorCalibrationFixtureFactory.createSuite(),
    )
    private val service = NpcActorInternalReviewService()

    @Test
    fun `three complete independent reviews aggregate after answer disclosure`() {
        val selections = embodied.blindReviewAnswerKey.answers.associate { it.reviewId to it.embodiedActorOption }
        val reviews = validReviews { reviewId, _ -> selections.getValue(reviewId) }

        val result = service.aggregate(embodied.blindReviewPacket, reviews, embodied.blindReviewAnswerKey)

        assertEquals(4, result.actorWins)
        assertEquals(0, result.controlWins)
        assertEquals(0, result.noConsensus)
        assertEquals("INTERNAL_REVIEW_COMPLETE", result.overallDecision)
        assertEquals(3, result.reviewContentSha256.size)
        assertTrue(result.packetSha256.matches(Regex("^[0-9a-f]{64}$")))
        assertTrue(result.answerKeySha256.matches(Regex("^[0-9a-f]{64}$")))
    }

    @Test
    fun `missing role duplicate reviewer and answer leakage are rejected`() {
        val reviews = validReviews { _, _ -> "TIE" }
        assertFailsWith<IllegalArgumentException> {
            service.aggregate(
                embodied.blindReviewPacket,
                reviews.dropLast(1),
                embodied.blindReviewAnswerKey,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.aggregate(
                embodied.blindReviewPacket,
                reviews.mapIndexed { index, review -> if (index == 1) review.copy(reviewerId = reviews.first().reviewerId) else review },
                embodied.blindReviewAnswerKey,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.aggregate(
                embodied.blindReviewPacket,
                reviews.mapIndexed { index, review ->
                    if (index == 0) review.copy(
                        ratings = review.ratings.mapIndexed { ratingIndex, rating ->
                            if (ratingIndex == 0) rating.copy(rationale = "EMBODIED_ACTOR_TRAJECTORY") else rating
                        },
                    ) else review
                },
                embodied.blindReviewAnswerKey,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.aggregate(
                embodied.blindReviewPacket,
                reviews.mapIndexed { index, review ->
                    if (index == 0) review.copy(claimBoundary = "Human preference confirmed.") else review
                },
                embodied.blindReviewAnswerKey,
            )
        }
    }

    @Test
    fun `three-way disagreement closes as insufficient consensus without user review`() {
        val reviews = validReviews { _, reviewerIndex -> listOf("A", "B", "TIE")[reviewerIndex] }

        val result = service.aggregate(embodied.blindReviewPacket, reviews, embodied.blindReviewAnswerKey)

        assertEquals(4, result.noConsensus)
        assertEquals("INSUFFICIENT_INTERNAL_CONSENSUS", result.overallDecision)
    }

    @Test
    fun `cli writes the same deterministic aggregate`() {
        val dir = Files.createTempDirectory("npc-actor04a-cli-")
        val packet = write(dir.resolve("packet.json"), embodied.blindReviewPacket)
        val answer = write(dir.resolve("answer.json"), embodied.blindReviewAnswerKey)
        val reviews = validReviews { _, _ -> "TIE" }
        val environment = write(dir.resolve("environment.json"), reviews[0])
        val dialogue = write(dir.resolve("dialogue.json"), reviews[1])
        val adversarial = write(dir.resolve("adversarial.json"), reviews[2])
        val output = dir.resolve("aggregate.json")

        assertEquals(
            0,
            Cli().run(
                listOf(
                    "aggregate-npc-actor-embodied-internal-review",
                    "--packet", packet.toString(),
                    "--environment-review", environment.toString(),
                    "--dialogue-review", dialogue.toString(),
                    "--adversarial-review", adversarial.toString(),
                    "--answer-key", answer.toString(),
                    "--out", output.toString(),
                ),
            ),
        )
        val actual = NpcActorJson.strict.decodeFromString<NpcEmbodiedInternalReviewAggregate>(Files.readString(output))
        val expected = service.aggregate(embodied.blindReviewPacket, reviews, embodied.blindReviewAnswerKey)
        assertEquals(expected, actual)
        assertEquals("NO_INTERNAL_ADVANTAGE", actual.overallDecision)
    }

    private fun validReviews(selection: (String, Int) -> String): List<NpcEmbodiedInternalReviewResult> = roles.mapIndexed { index, role ->
        NpcEmbodiedInternalReviewResult(
            packetId = embodied.blindReviewPacket.packetId,
            reviewerId = "internal-reviewer-${index + 1}",
            reviewerRole = role,
            ratings = embodied.blindReviewPacket.items.map { item -> rating(item, selection(item.reviewId, index)) },
        )
    }

    private fun rating(item: com.localnovel.engine.model.NpcEmbodiedReviewItem, selection: String) =
        NpcEmbodiedInternalRating(
            reviewId = item.reviewId,
            selection = selection,
            dimensionScores = item.ratingDimensions.map { NpcEmbodiedInternalDimensionScore(it, 4, 4) },
            evidence = listOf("匿名包中的第一個可見行動。", "匿名包中的後續狀態延續。"),
            issueTags = listOf("NONE"),
            rationale = "只依匿名包完成內部工程評審。",
        )

    private inline fun <reified T> write(path: Path, value: T): Path {
        Files.writeString(path, NpcActorJson.strict.encodeToString(value))
        return path
    }

    private companion object {
        val roles = listOf(
            "ENVIRONMENT_AND_PHYSICS_REVIEWER",
            "CHARACTER_AND_DIALOGUE_REVIEWER",
            "ADVERSARIAL_BOUNDARY_REVIEWER",
        )
    }
}
