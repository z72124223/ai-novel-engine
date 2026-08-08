package com.localnovel.engine

import com.localnovel.engine.model.StoryChapterDraftIntake
import com.localnovel.engine.model.StoryChapterPilotSelection
import com.localnovel.engine.model.StoryChapterProductionSeed
import com.localnovel.engine.model.StoryChapterProductionStatus
import com.localnovel.engine.model.StoryChapterReviewRecord
import com.localnovel.engine.model.StoryChapterReviewerRole
import com.localnovel.engine.model.StoryChapterReviewVerdict
import com.localnovel.engine.model.StoryChapterRevisionIntake
import com.localnovel.engine.model.StoryChapterFullTextReview
import com.localnovel.engine.support.JsonSupport
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.repository.StoryChapterProductionRepository
import java.nio.file.Files
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.serialization.encodeToString

class StoryChapterProductionRepositoryTest {
    @Test
    fun persistsThreeIndependentReviewsOneRevisionAndNonCanonSelection() {
        val fixture = fixture()
        val draft = "第一章初稿：潮聲壓住了愛醬的低吼。"
        fixture.chapter.ingestDraft(
            StoryChapterDraftIntake(PRODUCTION_ID, hash(SOURCE_PACKET), draft, hash(draft), "{\"model\":\"manual-codex\"}"),
        )
        fixture.chapter.recordReviewBatch(PRODUCTION_ID, reviews(draft, hash(draft)))
        val revised = "第一章修訂稿：潮聲壓住愛醬的低吼，陸塵行終於看見礁石後的血痕。"
        fixture.chapter.ingestRevision(
            StoryChapterRevisionIntake(PRODUCTION_ID, hash(draft), revised, hash(revised), "{\"revision\":1}"),
        )
        fixture.chapter.selectPilot(
            StoryChapterPilotSelection(PRODUCTION_ID, hash(revised), "APPROVE_PILOT_DRAFT", "product-owner"),
        )

        val view = fixture.chapter.get(PRODUCTION_ID)
        assertEquals(StoryChapterProductionStatus.SELECTED_PILOT_DRAFT, view.status)
        assertEquals(3, view.reviews.size)
        assertEquals(1, view.revisionCount)
        assertEquals(revised, view.selectedDraft)
        assertEquals("NON_CANON_PILOT", view.canonStatus)
    }

    @Test
    fun reviewBatchRollsBackCompletelyWhenAnyInsertFails() {
        val fixture = fixture()
        val draft = "需要審查的完整章節。"
        fixture.chapter.ingestDraft(StoryChapterDraftIntake(PRODUCTION_ID, hash(SOURCE_PACKET), draft, hash(draft)))
        val invalid = reviews(draft, hash(draft)).mapIndexed { index, review ->
            if (index == 1) review.copy(reviewId = "review-logic") else review
        }

        assertFailsWith<Exception> { fixture.chapter.recordReviewBatch(PRODUCTION_ID, invalid) }
        assertEquals(0, fixture.repository.tableCount("story_chapter_reviews"))
        assertEquals(StoryChapterProductionStatus.DRAFT_INGESTED, fixture.chapter.get(PRODUCTION_ID).status)
    }

    @Test
    fun rejectsHashDriftSecondRevisionAndUnreviewedSelection() {
        val fixture = fixture()
        assertFailsWith<IllegalArgumentException> {
            fixture.chapter.ingestDraft(StoryChapterDraftIntake(PRODUCTION_ID, hash(SOURCE_PACKET), "draft", "wrong"))
        }
        val draft = "可驗證初稿。"
        fixture.chapter.ingestDraft(StoryChapterDraftIntake(PRODUCTION_ID, hash(SOURCE_PACKET), draft, hash(draft)))
        assertFailsWith<IllegalArgumentException> {
            fixture.chapter.selectPilot(
                StoryChapterPilotSelection(PRODUCTION_ID, hash(draft), "APPROVE_PILOT_DRAFT", "product-owner"),
            )
        }
        fixture.chapter.recordReviewBatch(PRODUCTION_ID, reviews(draft, hash(draft)))
        assertFailsWith<IllegalArgumentException> {
            fixture.chapter.ingestRevision(StoryChapterRevisionIntake(PRODUCTION_ID, hash(draft), draft, hash(draft)))
        }
        val revised = "唯一修訂稿。"
        fixture.chapter.ingestRevision(StoryChapterRevisionIntake(PRODUCTION_ID, hash(draft), revised, hash(revised)))
        assertFailsWith<IllegalArgumentException> {
            fixture.chapter.ingestRevision(StoryChapterRevisionIntake(PRODUCTION_ID, hash(draft), "第二次修訂", hash("第二次修訂")))
        }
        assertTrue(fixture.chapter.get(PRODUCTION_ID).selectedDraft.isBlank())
    }

    @Test
    fun databaseRejectsImpossibleCrossFieldStatus() {
        val fixture = fixture()
        assertFailsWith<Exception> {
            fixture.repository.withTransaction { connection ->
                fixture.repository.executeUpdate(
                    connection,
                    "UPDATE story_chapter_productions SET status = 'SELECTED_PILOT_DRAFT' WHERE production_id = ?",
                    PRODUCTION_ID,
                )
            }
        }
        assertEquals(StoryChapterProductionStatus.DRAFT_REQUESTED, fixture.chapter.get(PRODUCTION_ID).status)
    }

    private fun fixture(): Fixture {
        val repository = SqliteStoryRepository(Files.createTempDirectory("story-chapter-production").resolve("story.db"))
        repository.initialize()
        val chapter = StoryChapterProductionRepository(repository)
        chapter.create(
            StoryChapterProductionSeed(
                productionId = PRODUCTION_ID,
                projectId = "chenxing",
                sessionId = "story01-pilot",
                scenarioSeed = "story01-real-chapter-v1",
                sourceCommitSha = "4b7a064",
                sourcePacketJson = SOURCE_PACKET,
                sourcePacketSha256 = hash(SOURCE_PACKET),
            ),
        )
        return Fixture(repository, chapter)
    }

    private fun reviews(draft: String, draftHash: String): List<StoryChapterReviewRecord> = listOf(
        StoryChapterReviewRecord(
            "review-logic", PRODUCTION_ID, StoryChapterReviewerRole.LOGIC, "agent-logic", draftHash,
            StoryChapterReviewVerdict.REVISE, reviewJson(StoryChapterReviewerRole.LOGIC, "agent-logic", draft, draftHash, StoryChapterReviewVerdict.REVISE),
        ),
        StoryChapterReviewRecord(
            "review-character", PRODUCTION_ID, StoryChapterReviewerRole.CHARACTER, "agent-character", draftHash,
            StoryChapterReviewVerdict.PASS, reviewJson(StoryChapterReviewerRole.CHARACTER, "agent-character", draft, draftHash, StoryChapterReviewVerdict.PASS),
        ),
        StoryChapterReviewRecord(
            "review-reader", PRODUCTION_ID, StoryChapterReviewerRole.READER, "agent-reader", draftHash,
            StoryChapterReviewVerdict.PASS, reviewJson(StoryChapterReviewerRole.READER, "agent-reader", draft, draftHash, StoryChapterReviewVerdict.PASS),
        ),
    )

    private fun reviewJson(role: StoryChapterReviewerRole, instance: String, draft: String, hash: String, verdict: StoryChapterReviewVerdict): String =
        JsonSupport.json.encodeToString(
            StoryChapterFullTextReview(
                reviewerRole = role,
                reviewerInstanceId = instance,
                sourceDraftSha256 = hash,
                verdict = verdict,
                summary = "完整全文評審",
                quotedEvidence = List(3) { draft.take(8) },
                strengths = listOf("可驗證優點"),
                findings = listOf("可驗證發現"),
            ),
        )

    private fun hash(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private data class Fixture(
        val repository: SqliteStoryRepository,
        val chapter: StoryChapterProductionRepository,
    )

    private companion object {
        const val PRODUCTION_ID = "story01-chapter-001"
        const val SOURCE_PACKET = "{\"chapter\":\"story01\"}"
    }
}
