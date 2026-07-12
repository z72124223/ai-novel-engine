package com.localnovel.engine.repository

import com.localnovel.engine.model.StoryChapterDraftIntake
import com.localnovel.engine.model.StoryChapterPilotSelection
import com.localnovel.engine.model.StoryChapterProductionSeed
import com.localnovel.engine.model.StoryChapterProductionStatus
import com.localnovel.engine.model.StoryChapterProductionView
import com.localnovel.engine.model.StoryChapterReviewRecord
import com.localnovel.engine.model.StoryChapterReviewerRole
import com.localnovel.engine.model.StoryChapterRevisionIntake
import java.security.MessageDigest
import java.time.Instant

class StoryChapterProductionRepository(
    private val repository: SqliteStoryRepository,
) {
    fun create(seed: StoryChapterProductionSeed) = repository.withTransaction { connection ->
        require(seed.productionId.isNotBlank() && seed.projectId.isNotBlank() && seed.sessionId.isNotBlank())
        require(seed.scenarioSeed.isNotBlank() && seed.sourceCommitSha.isNotBlank())
        requireHash(seed.sourcePacketJson, seed.sourcePacketSha256, "source packet")
        val timestamp = now()
        repository.executeUpdate(
            connection,
            """
            INSERT INTO story_chapter_productions(
                production_id, project_id, session_id, scenario_seed, source_commit_sha,
                source_packet_json, source_packet_sha256, status, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, 'DRAFT_REQUESTED', ?, ?)
            """.trimIndent(),
            seed.productionId,
            seed.projectId,
            seed.sessionId,
            seed.scenarioSeed,
            seed.sourceCommitSha,
            seed.sourcePacketJson,
            normalizedHash(seed.sourcePacketSha256),
            timestamp,
            timestamp,
        )
    }

    fun ingestDraft(input: StoryChapterDraftIntake) = repository.withTransaction { connection ->
        val row = productionRow(connection, input.productionId)
        require(row.getValue("status") == StoryChapterProductionStatus.DRAFT_REQUESTED.name) {
            "Initial draft may only be ingested from DRAFT_REQUESTED."
        }
        require(normalizedHash(input.sourcePacketSha256) == row.getValue("source_packet_sha256")) {
            "Draft source packet hash does not match the persisted handoff."
        }
        require(input.draft.isNotBlank()) { "Chapter draft cannot be blank." }
        requireHash(input.draft, input.draftSha256, "initial draft")
        check(
            repository.executeUpdate(
                connection,
                """
                UPDATE story_chapter_productions
                SET initial_draft = ?, initial_draft_sha256 = ?, initial_model_metadata_json = ?,
                    status = 'DRAFT_INGESTED', updated_at = ?
                WHERE production_id = ? AND status = 'DRAFT_REQUESTED'
                """.trimIndent(),
                input.draft,
                normalizedHash(input.draftSha256),
                input.modelMetadataJson,
                now(),
                input.productionId,
            ) == 1,
        ) { "Chapter draft state changed concurrently." }
    }

    fun recordReviewBatch(productionId: String, reviews: List<StoryChapterReviewRecord>) =
        repository.withTransaction { connection ->
            val row = productionRow(connection, productionId)
            require(row.getValue("status") == StoryChapterProductionStatus.DRAFT_INGESTED.name) {
                "Reviews may only be recorded for an ingested initial draft."
            }
            require(reviews.size == StoryChapterReviewerRole.entries.size) { "Exactly three independent reviews are required." }
            require(reviews.map { it.reviewerRole }.toSet() == StoryChapterReviewerRole.entries.toSet()) {
                "Review batch must contain LOGIC, CHARACTER, and READER roles exactly once."
            }
            require(reviews.map { it.reviewerInstanceId }.toSet().size == reviews.size) {
                "Each review role must use an independent reviewer instance."
            }
            val draftHash = row.getValue("initial_draft_sha256")
            reviews.forEach { review ->
                require(review.productionId == productionId)
                require(normalizedHash(review.sourceDraftSha256) == draftHash) {
                    "Review ${review.reviewId} does not reference the persisted initial draft."
                }
                require(review.reviewJson.isNotBlank()) { "Review evidence cannot be blank." }
                repository.executeUpdate(
                    connection,
                    """
                    INSERT INTO story_chapter_reviews(
                        review_id, production_id, reviewer_role, reviewer_instance_id,
                        source_draft_sha256, verdict, review_json, created_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent(),
                    review.reviewId,
                    productionId,
                    review.reviewerRole.name,
                    review.reviewerInstanceId,
                    draftHash,
                    review.verdict.name,
                    review.reviewJson,
                    now(),
                )
            }
            check(
                repository.executeUpdate(
                    connection,
                    "UPDATE story_chapter_productions SET status = 'REVIEWS_COMPLETE', updated_at = ? WHERE production_id = ? AND status = 'DRAFT_INGESTED'",
                    now(),
                    productionId,
                ) == 1,
            ) { "Chapter review state changed concurrently." }
        }

    fun ingestRevision(input: StoryChapterRevisionIntake) = repository.withTransaction { connection ->
        val row = productionRow(connection, input.productionId)
        require(row.getValue("status") == StoryChapterProductionStatus.REVIEWS_COMPLETE.name) {
            "Revision may only be ingested after all three reviews."
        }
        require(row.getValue("revision_count").toInt() == 0) { "Only one chapter revision is allowed." }
        require(normalizedHash(input.sourceDraftSha256) == row.getValue("initial_draft_sha256")) {
            "Revision must reference the reviewed initial draft."
        }
        require(input.revisedDraft.isNotBlank()) { "Revised chapter cannot be blank." }
        requireHash(input.revisedDraft, input.revisedDraftSha256, "revised draft")
        check(
            repository.executeUpdate(
                connection,
                """
                UPDATE story_chapter_productions
                SET revised_draft = ?, revised_draft_sha256 = ?, revision_model_metadata_json = ?,
                    revision_count = 1, status = 'REVISION_INGESTED', updated_at = ?
                WHERE production_id = ? AND status = 'REVIEWS_COMPLETE' AND revision_count = 0
                """.trimIndent(),
                input.revisedDraft,
                normalizedHash(input.revisedDraftSha256),
                input.modelMetadataJson,
                now(),
                input.productionId,
            ) == 1,
        ) { "Chapter revision state changed concurrently." }
    }

    fun selectPilot(input: StoryChapterPilotSelection) = repository.withTransaction { connection ->
        val row = productionRow(connection, input.productionId)
        require(row.getValue("status") in setOf(
            StoryChapterProductionStatus.REVIEWS_COMPLETE.name,
            StoryChapterProductionStatus.REVISION_INGESTED.name,
        )) { "Pilot selection requires completed reviews and at most one revision." }
        require(input.humanDecision == "APPROVE_PILOT_DRAFT" && input.humanActorId.isNotBlank()) {
            "A named human must approve the non-Canon pilot draft."
        }
        val selectedHash = normalizedHash(input.selectedDraftSha256)
        val revisedHash = row.getValue("revised_draft_sha256")
        val selectedDraft = when (selectedHash) {
            row.getValue("initial_draft_sha256") -> row.getValue("initial_draft")
            revisedHash.takeIf { it.isNotBlank() } -> row.getValue("revised_draft")
            else -> error("Selected draft hash does not identify a persisted draft.")
        }
        val reviewsRequireRevision = repository.queryRows(
            connection,
            "SELECT verdict FROM story_chapter_reviews WHERE production_id = ?",
            input.productionId,
        ).any { it["verdict"] == "REVISE" }
        require(!reviewsRequireRevision || selectedHash == revisedHash) {
            "A revision requested by reviewers must be resolved before pilot selection."
        }
        check(
            repository.executeUpdate(
                connection,
                """
                UPDATE story_chapter_productions
                SET selected_draft = ?, selected_draft_sha256 = ?, human_decision = ?, human_actor_id = ?,
                    status = 'SELECTED_PILOT_DRAFT', updated_at = ?
                WHERE production_id = ? AND status IN ('REVIEWS_COMPLETE', 'REVISION_INGESTED')
                """.trimIndent(),
                selectedDraft,
                selectedHash,
                input.humanDecision,
                input.humanActorId,
                now(),
                input.productionId,
            ) == 1,
        ) { "Chapter selection state changed concurrently." }
    }

    fun rejectPilot(productionId: String, humanActorId: String) = repository.withTransaction { connection ->
        require(humanActorId.isNotBlank()) { "A named human must reject the pilot." }
        check(
            repository.executeUpdate(
                connection,
                """
                UPDATE story_chapter_productions
                SET status = 'REJECTED', human_decision = 'REJECT_PILOT_DRAFT', human_actor_id = ?, updated_at = ?
                WHERE production_id = ? AND status IN ('REVIEWS_COMPLETE', 'REVISION_INGESTED', 'HUMAN_DECISION_REQUIRED')
                """.trimIndent(),
                humanActorId,
                now(),
                productionId,
            ) == 1,
        ) { "Pilot cannot be rejected from its current state." }
    }

    fun requireHumanDecision(productionId: String) = repository.withTransaction { connection ->
        check(
            repository.executeUpdate(
                connection,
                """
                UPDATE story_chapter_productions SET status = 'HUMAN_DECISION_REQUIRED', updated_at = ?
                WHERE production_id = ? AND status IN ('REVIEWS_COMPLETE', 'REVISION_INGESTED')
                """.trimIndent(),
                now(),
                productionId,
            ) == 1,
        ) { "Human-decision state cannot be entered from the current state." }
    }

    fun get(productionId: String): StoryChapterProductionView = repository.withConnection { connection ->
        val row = productionRow(connection, productionId)
        val reviews = repository.queryRows(
            connection,
            "SELECT * FROM story_chapter_reviews WHERE production_id = ? ORDER BY reviewer_role",
            productionId,
        ).map { review ->
            StoryChapterReviewRecord(
                reviewId = review.getValue("review_id").orEmpty(),
                productionId = productionId,
                reviewerRole = StoryChapterReviewerRole.valueOf(review.getValue("reviewer_role").orEmpty()),
                reviewerInstanceId = review.getValue("reviewer_instance_id").orEmpty(),
                sourceDraftSha256 = review.getValue("source_draft_sha256").orEmpty(),
                verdict = com.localnovel.engine.model.StoryChapterReviewVerdict.valueOf(review.getValue("verdict").orEmpty()),
                reviewJson = review.getValue("review_json").orEmpty(),
            )
        }
        StoryChapterProductionView(
            productionId = row.getValue("production_id"),
            projectId = row.getValue("project_id"),
            sessionId = row.getValue("session_id"),
            scenarioSeed = row.getValue("scenario_seed"),
            sourceCommitSha = row.getValue("source_commit_sha"),
            sourcePacketJson = row.getValue("source_packet_json"),
            sourcePacketSha256 = row.getValue("source_packet_sha256"),
            status = StoryChapterProductionStatus.valueOf(row.getValue("status")),
            initialDraft = row.getValue("initial_draft"),
            initialDraftSha256 = row.getValue("initial_draft_sha256"),
            revisedDraft = row.getValue("revised_draft"),
            revisedDraftSha256 = row.getValue("revised_draft_sha256"),
            selectedDraft = row.getValue("selected_draft"),
            selectedDraftSha256 = row.getValue("selected_draft_sha256"),
            revisionCount = row.getValue("revision_count").toInt(),
            humanDecision = row.getValue("human_decision"),
            humanActorId = row.getValue("human_actor_id"),
            canonStatus = row.getValue("canon_status"),
            reviews = reviews,
        )
    }

    private fun productionRow(connection: java.sql.Connection, productionId: String): Map<String, String> =
        repository.queryRows(
            connection,
            "SELECT * FROM story_chapter_productions WHERE production_id = ?",
            productionId,
        ).singleOrNull()?.mapValues { it.value.orEmpty() }
            ?: error("Unknown story chapter production: $productionId")

    private fun requireHash(content: String, expected: String, label: String) {
        require(sha256(content) == normalizedHash(expected)) { "$label SHA-256 mismatch." }
    }

    private fun normalizedHash(value: String): String = value.trim().lowercase()

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private fun now(): String = Instant.now().toString()
}
