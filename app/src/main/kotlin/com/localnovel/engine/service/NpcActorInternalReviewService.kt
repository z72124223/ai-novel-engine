package com.localnovel.engine.service

import com.localnovel.engine.model.NpcEmbodiedAnswerKey
import com.localnovel.engine.model.NpcEmbodiedInternalReviewAggregate
import com.localnovel.engine.model.NpcEmbodiedInternalReviewResult
import com.localnovel.engine.model.NpcEmbodiedInternalReviewerSelection
import com.localnovel.engine.model.NpcEmbodiedInternalSceneAggregate
import com.localnovel.engine.model.NpcEmbodiedReviewPacket
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlinx.serialization.encodeToString

class NpcActorInternalReviewService {
    fun aggregate(
        packet: NpcEmbodiedReviewPacket,
        reviews: List<NpcEmbodiedInternalReviewResult>,
        answerKey: NpcEmbodiedAnswerKey,
    ): NpcEmbodiedInternalReviewAggregate {
        validateReviews(packet, reviews)
        validateAnswerKey(packet, answerKey)

        val answers = answerKey.answers.associate { it.reviewId to it.embodiedActorOption }
        val scenes = packet.items.map { item ->
            val ratings = reviews.map { review -> review to review.ratings.single { it.reviewId == item.reviewId } }
            val consensus = consensus(ratings.map { it.second.selection })
            val actorOption = answers.getValue(item.reviewId)
            val outcome = when {
                consensus == "NO_CONSENSUS" -> "NO_CONSENSUS"
                consensus == "TIE" -> "TIE"
                consensus == "BOTH_FAIL" -> "BOTH_FAIL"
                consensus == actorOption -> "ACTOR_PREFERRED"
                else -> "CONTROL_PREFERRED"
            }
            NpcEmbodiedInternalSceneAggregate(
                reviewId = item.reviewId,
                reviewerSelections = ratings.map { (review, rating) ->
                    NpcEmbodiedInternalReviewerSelection(review.reviewerId, review.reviewerRole, rating.selection)
                },
                consensusSelection = consensus,
                embodiedActorOption = actorOption,
                outcome = outcome,
                issueTags = ratings.flatMap { it.second.issueTags }.distinct().sorted(),
            )
        }

        val actorWins = scenes.count { it.outcome == "ACTOR_PREFERRED" }
        val controlWins = scenes.count { it.outcome == "CONTROL_PREFERRED" }
        val ties = scenes.count { it.outcome == "TIE" }
        val bothFail = scenes.count { it.outcome == "BOTH_FAIL" }
        val noConsensus = scenes.count { it.outcome == "NO_CONSENSUS" }
        val overallDecision = when {
            noConsensus > 0 -> "INSUFFICIENT_INTERNAL_CONSENSUS"
            actorWins > controlWins -> "INTERNAL_REVIEW_COMPLETE"
            else -> "NO_INTERNAL_ADVANTAGE"
        }
        val revisionCount = reviews.maxOf { it.revisionNumber }

        return NpcEmbodiedInternalReviewAggregate(
            packetId = packet.packetId,
            reviewerIds = reviews.map { it.reviewerId }.sorted(),
            reviewerRoles = reviews.map { it.reviewerRole }.sorted(),
            packetSha256 = contentSha256(packet),
            answerKeySha256 = contentSha256(answerKey),
            reviewContentSha256 = reviews.associate { it.reviewerId to contentSha256(it) }.toSortedMap(),
            revisionCount = revisionCount,
            scenes = scenes,
            actorWins = actorWins,
            controlWins = controlWins,
            ties = ties,
            bothFail = bothFail,
            noConsensus = noConsensus,
            overallDecision = overallDecision,
            claimBoundary = listOf(
                "Three internal agent reviews do not establish human preference.",
                "The reviewed trajectories are manually authored offline fixtures, not live model outputs.",
                "This result does not authorize live API, production rollout, Canon promotion or media work.",
            ),
        )
    }

    fun validateReviews(packet: NpcEmbodiedReviewPacket, reviews: List<NpcEmbodiedInternalReviewResult>) {
        require(packet.status == "HUMAN_REVIEW_REQUIRED") { "Unexpected NPC-ACTOR-04 packet status." }
        require(reviews.size == requiredRoles.size) { "Exactly three internal reviews are required." }
        require(reviews.map { it.reviewerId }.distinct().size == reviews.size) { "Reviewer ids must be unique." }
        require(reviews.map { it.reviewerRole }.toSet() == requiredRoles) { "Internal reviewer roles are incomplete or duplicated." }
        require(reviews.map { it.revisionNumber }.distinct().size == 1) { "All internal reviews must use the same revision number." }
        require(reviews.first().revisionNumber in 0..1) { "At most one internal evidence revision is allowed." }

        val expectedReviewIds = packet.items.map { it.reviewId }.toSet()
        reviews.forEach { review ->
            require(review.schemaVersion == "1.0") { "Unsupported internal review schema version." }
            require(review.artifactType == "NPC_ACTOR04_INTERNAL_REVIEW") { "Internal review artifact type is invalid." }
            require(review.packetId == packet.packetId) { "Internal review packet id does not match." }
            require(review.status == "COMPLETE_BEFORE_ANSWER_KEY_DISCLOSURE") { "Internal review must be complete before answer disclosure." }
            require(!review.answerKeyReadBeforeCompletion) { "Internal reviewer read the answer key before completion." }
            require(review.claimBoundary == internalReviewClaimBoundary) {
                "Internal review claim boundary must preserve the non-human, non-live limitation."
            }
            require(review.ratings.size == packet.items.size) { "Every scene requires one internal rating." }
            require(review.ratings.map { it.reviewId }.toSet() == expectedReviewIds) { "Internal review ids do not match the packet." }
            require(review.ratings.map { it.reviewId }.distinct().size == review.ratings.size) { "Internal review ids must be unique." }

            review.ratings.forEach { rating ->
                val item = packet.items.single { it.reviewId == rating.reviewId }
                require(rating.selection in allowedSelections) { "Internal review selection is invalid." }
                require(rating.dimensionScores.map { it.dimension }.toSet() == item.ratingDimensions.toSet()) {
                    "Internal review dimension scores do not match the packet."
                }
                require(rating.dimensionScores.map { it.dimension }.distinct().size == rating.dimensionScores.size) {
                    "Internal review dimensions must be unique."
                }
                require(rating.dimensionScores.all { it.optionA in 1..5 && it.optionB in 1..5 }) {
                    "Internal review scores must be between 1 and 5."
                }
                require(rating.evidence.size in 2..4 && rating.evidence.all { it.isNotBlank() }) {
                    "Every internal rating requires two to four evidence statements."
                }
                require(rating.issueTags.isNotEmpty() && rating.issueTags.all { issueTag.matches(it) }) {
                    "Internal review issue tags are invalid."
                }
                require(rating.rationale.isNotBlank()) { "Internal review rationale is required." }
            }

            val serialized = NpcActorJson.strict.encodeToString(review)
            require(forbiddenAnswerTokens.none { serialized.contains(it, ignoreCase = true) }) {
                "Internal review contains answer identity or source-label leakage."
            }
        }
    }

    private fun validateAnswerKey(packet: NpcEmbodiedReviewPacket, answerKey: NpcEmbodiedAnswerKey) {
        require(answerKey.packetId == packet.packetId) { "Answer key packet id does not match." }
        require(answerKey.answers.map { it.reviewId }.toSet() == packet.items.map { it.reviewId }.toSet()) {
            "Answer key review ids do not match the packet."
        }
        require(answerKey.answers.map { it.reviewId }.distinct().size == answerKey.answers.size) {
            "Answer key review ids must be unique."
        }
        require(answerKey.answers.all { it.embodiedActorOption in setOf("A", "B") }) {
            "Answer key actor option is invalid."
        }
    }

    private fun consensus(selections: List<String>): String = selections
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
        .firstOrNull { it.value >= 2 }
        ?.key
        ?: "NO_CONSENSUS"

    private inline fun <reified T> contentSha256(value: T): String = sha256(NpcActorJson.strict.encodeToString(value))

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte) }

    private companion object {
        val requiredRoles = setOf(
            "ENVIRONMENT_AND_PHYSICS_REVIEWER",
            "CHARACTER_AND_DIALOGUE_REVIEWER",
            "ADVERSARIAL_BOUNDARY_REVIEWER",
        )
        val allowedSelections = setOf("A", "B", "TIE", "BOTH_FAIL")
        val issueTag = Regex("^[A-Z0-9_]+$")
        val forbiddenAnswerTokens = listOf(
            "embodiedActorOption",
            "EMBODIED_ACTOR_TRAJECTORY",
            "CONCISE_CONTROL_TRAJECTORY",
            "randomizationSeed",
        )
        const val internalReviewClaimBoundary =
            "Internal agent review only; not human preference or live-model proof."
    }
}
