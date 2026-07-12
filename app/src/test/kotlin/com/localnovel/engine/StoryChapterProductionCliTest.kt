package com.localnovel.engine

import com.localnovel.engine.model.StoryChapterDraftIntake
import com.localnovel.engine.model.StoryChapterHandoffRequest
import com.localnovel.engine.model.StoryChapterReviewBatch
import com.localnovel.engine.model.StoryChapterReviewRecord
import com.localnovel.engine.model.StoryChapterReviewerRole
import com.localnovel.engine.model.StoryChapterReviewVerdict
import com.localnovel.engine.model.StoryChapterWorldQuery
import com.localnovel.engine.model.StoryChapterFullTextReview
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StoryChapterProductionCliTest {
    @Test
    fun cliBuildsSqlBackedHashBoundHandoffAndPersistsIntakes() {
        val root = Files.createTempDirectory("story-chapter-cli")
        val db = root.resolve("story.db")
        val repository = SqliteStoryRepository(db).also { it.initialize() }
        val runtimeContext = seedSqlEvidence(repository)
        val request = StoryChapterHandoffRequest(
                productionId = "story01-cli",
                projectId = "chenxing",
                sessionId = runtimeContext.sessionId,
                runId = "run-story01",
                scenarioSeed = "story01-seed",
                sourceCommitSha = "ccce1af",
                chapterTitle = "潮痕",
                premise = "陸塵行與愛醬在大坪海岸發現不應出現的血痕。",
                runtimeScenarioSeed = "story01-seed",
                worldQueries = listOf(
                    StoryChapterWorldQuery("愛醬", allowedSourceIds = listOf("source-01")),
                    StoryChapterWorldQuery("大坪海岸", allowedSourceIds = listOf("source-01")),
                ),
                requiredBeats = listOf("觀察", "選擇", "代價"),
                forbiddenOutcomes = listOf("自動改寫 Canon"),
                voiceRequirements = listOf("繁體中文", "角色聲線克制"),
            )
        val requestPath = write(root.resolve("request.json"), request)
        val handoff = root.resolve("handoff.json")
        assertEquals(0, Cli().run(listOf("create-story-chapter-handoff", "--db", db.toString(), "--input", requestPath.toString(), "--out", handoff.toString())))
        val handoffText = Files.readString(handoff)
        assertTrue(handoffText.contains("SQL 愛醬設定"))
        assertTrue(handoffText.contains(runtimeContext.contextId))
        assertTrue(!handoffText.contains("OTHER_PROJECT_SECRET"))

        val tamperedRequest = write(root.resolve("tampered-request.json"), request.copy(productionId = "story01-tampered", runId = "run-tampered"))
        assertEquals(1, Cli().run(listOf("create-story-chapter-handoff", "--db", db.toString(), "--input", tamperedRequest.toString(), "--out", root.resolve("tampered.json").toString())))

        val sourceHash = repository.tableRows("story_chapter_productions").single().getValue("source_packet_sha256")
        val draft = "完整中文初稿，包含實際場景推進。"
        val draftInput = write(
            root.resolve("draft.json"),
            StoryChapterDraftIntake("story01-cli", sourceHash, draft, hash(draft)),
        )
        assertEquals(0, Cli().run(listOf("ingest-story-chapter-draft", "--db", db.toString(), "--input", draftInput.toString())))

        val reviews = StoryChapterReviewerRole.entries.map { role ->
            StoryChapterReviewRecord(
                reviewId = "review-${role.name.lowercase()}",
                productionId = "story01-cli",
                reviewerRole = role,
                reviewerInstanceId = "agent-${role.name.lowercase()}",
                sourceDraftSha256 = hash(draft),
                verdict = StoryChapterReviewVerdict.PASS,
                reviewJson = JsonSupport.json.encodeToString(
                    StoryChapterFullTextReview(
                        reviewerRole = role,
                        reviewerInstanceId = "agent-${role.name.lowercase()}",
                        sourceDraftSha256 = hash(draft),
                        verdict = StoryChapterReviewVerdict.PASS,
                        summary = "完整全文評審",
                        quotedEvidence = List(3) { "實際場景推進" },
                        strengths = listOf("推進成立"),
                        findings = listOf("沒有阻斷問題"),
                    ),
                ),
            )
        }
        val reviewInput = write(root.resolve("reviews.json"), StoryChapterReviewBatch("story01-cli", reviews))
        val state = root.resolve("state.json")
        assertEquals(0, Cli().run(listOf("ingest-story-chapter-reviews", "--db", db.toString(), "--input", reviewInput.toString(), "--out", state.toString())))
        assertTrue(Files.readString(state).contains("REVIEWS_COMPLETE"))
    }

    private fun seedSqlEvidence(repository: SqliteStoryRepository): ContextPacket {
        val contextJson = Files.readString(samplePath("reboot01/vertical_slice_run/turn-01-context.json")).removePrefix("\uFEFF")
        val context = JsonSupport.json.decodeFromString<ContextPacket>(contextJson)
        repository.withTransaction { connection ->
            repository.insertRawRow(connection, "world_source_archives", mapOf(
                "archive_id" to "archive", "project_label" to "chenxing", "provided_source_title" to "source",
                "title_relationship_status" to "provided", "status" to "IMPORTED", "locale" to "zh-TW",
                "manifest_path" to "manifest.json", "manifest_json" to "{}", "raw_text_unmodified" to "1",
                "classification_only" to "1", "content_rewrite_allowed" to "0", "content_summary_allowed" to "0",
                "official_source_data" to "1", "source_authority_status" to "PROVIDED_SOURCE_DATA",
                "canon_approved" to "0", "setting_pack_data" to "0", "runtime_sqlite_data" to "1",
                "approved_asset_data" to "0", "source_count" to "1", "total_byte_size" to "10",
                "created_at" to "2026-07-12T00:00:00Z", "updated_at" to "2026-07-12T00:00:00Z",
            ))
            repository.insertRawRow(connection, "world_source_documents", mapOf(
                "document_id" to "doc", "archive_id" to "archive", "project_label" to "chenxing",
                "source_id" to "source-01", "document_order" to "1", "classification" to "setting",
                "title" to "塵行設定", "raw_path" to "raw.md", "source_kind" to "text", "copy_mode" to "verbatim",
                "byte_size" to "10", "sha256" to "hash", "raw_text" to "SQL 愛醬設定與大坪海岸",
                "raw_text_unmodified" to "1", "official_source_data" to "1", "source_authority_status" to "PROVIDED_SOURCE_DATA",
                "metadata_json" to "{}", "created_at" to "2026-07-12T00:00:00Z",
            ))
            repository.insertRawRow(connection, "world_source_chunks", mapOf(
                "chunk_id" to "chunk", "document_id" to "doc", "archive_id" to "archive", "project_label" to "chenxing",
                "source_id" to "source-01", "chunk_order" to "1", "classification" to "setting", "title" to "塵行設定",
                "heading" to "角色與場景", "content" to "SQL 愛醬設定與大坪海岸", "search_text" to "SQL 愛醬設定與大坪海岸",
                "byte_size" to "10", "sha256" to "hash", "official_source_data" to "1",
                "source_authority_status" to "PROVIDED_SOURCE_DATA", "created_at" to "2026-07-12T00:00:00Z",
            ))
            repository.insertRawRow(connection, "world_source_archives", mapOf(
                "archive_id" to "other-archive", "project_label" to "other-project", "provided_source_title" to "other",
                "title_relationship_status" to "provided", "status" to "IMPORTED", "locale" to "zh-TW",
                "manifest_path" to "other.json", "manifest_json" to "{}", "raw_text_unmodified" to "1",
                "classification_only" to "1", "content_rewrite_allowed" to "0", "content_summary_allowed" to "0",
                "official_source_data" to "1", "source_authority_status" to "PROVIDED_SOURCE_DATA",
                "canon_approved" to "0", "setting_pack_data" to "0", "runtime_sqlite_data" to "1",
                "approved_asset_data" to "0", "source_count" to "1", "total_byte_size" to "10",
                "created_at" to "2026-07-12T00:00:00Z", "updated_at" to "2026-07-12T00:00:00Z",
            ))
            repository.insertRawRow(connection, "world_source_documents", mapOf(
                "document_id" to "other-doc", "archive_id" to "other-archive", "project_label" to "other-project",
                "source_id" to "source-01", "document_order" to "1", "classification" to "setting", "title" to "other",
                "raw_path" to "other.md", "source_kind" to "text", "copy_mode" to "verbatim", "byte_size" to "10",
                "sha256" to "hash", "raw_text" to "OTHER_PROJECT_SECRET 愛醬 大坪海岸", "raw_text_unmodified" to "1",
                "official_source_data" to "1", "source_authority_status" to "PROVIDED_SOURCE_DATA", "metadata_json" to "{}",
                "created_at" to "2026-07-12T00:00:00Z",
            ))
            repository.insertRawRow(connection, "world_source_chunks", mapOf(
                "chunk_id" to "other-chunk", "document_id" to "other-doc", "archive_id" to "other-archive",
                "project_label" to "other-project", "source_id" to "source-01", "chunk_order" to "1",
                "classification" to "setting", "title" to "other", "heading" to "secret",
                "content" to "OTHER_PROJECT_SECRET 愛醬 大坪海岸", "search_text" to "OTHER_PROJECT_SECRET 愛醬 大坪海岸",
                "byte_size" to "10", "sha256" to "hash", "official_source_data" to "1",
                "source_authority_status" to "PROVIDED_SOURCE_DATA", "created_at" to "2026-07-12T00:00:00Z",
            ))
            repository.insertRawRow(connection, "reboot_run_evidence", mapOf(
                "run_id" to "run-story01", "project_id" to "chenxing", "session_id" to context.sessionId, "turn_number" to "1",
                "scenario_seed" to "story01-seed", "context_id" to context.contextId, "context_hash" to context.contextHash,
                "player_input" to "沿海岸調查", "context_snapshot_json" to contextJson, "result_id" to "result",
                "raw_draft" to "draft", "raw_result_json" to "{}", "critic_scores_json" to "{}", "revision_suggestion" to "",
                "revised_draft" to "", "validation_findings_json" to "[]", "validation_classification" to "PASS",
                "state_delta_json" to "[]", "irreversible_change_json" to "{}", "irreversible_approval_json" to "{}",
                "irreversible_approval_actor" to "", "irreversible_approval_granted" to "0", "apply_result_json" to "{}",
                "model_metadata_json" to "{}", "elapsed_time_ms" to "1", "status" to "APPLIED",
                "created_at" to "2026-07-12T00:00:00Z", "updated_at" to "2026-07-12T00:00:00Z",
            ))
            repository.insertRawRow(connection, "reboot_run_evidence", mapOf(
                "run_id" to "run-tampered", "project_id" to "chenxing", "session_id" to context.sessionId, "turn_number" to "1",
                "scenario_seed" to "story01-seed", "context_id" to context.contextId, "context_hash" to "tampered-hash",
                "player_input" to "沿海岸調查", "context_snapshot_json" to contextJson, "result_id" to "tampered-result",
                "raw_draft" to "draft", "raw_result_json" to "{}", "critic_scores_json" to "{}", "revision_suggestion" to "",
                "revised_draft" to "", "validation_findings_json" to "[]", "validation_classification" to "PASS",
                "state_delta_json" to "[]", "irreversible_change_json" to "{}", "irreversible_approval_json" to "{}",
                "irreversible_approval_actor" to "", "irreversible_approval_granted" to "0", "apply_result_json" to "{}",
                "model_metadata_json" to "{}", "elapsed_time_ms" to "1", "status" to "APPLIED",
                "created_at" to "2026-07-12T00:00:00Z", "updated_at" to "2026-07-12T00:00:00Z",
            ))
        }
        return context
    }

    private fun samplePath(relative: String): Path = listOf(Path.of("samples", relative), Path.of("..", "samples", relative))
        .firstOrNull(Files::exists) ?: error("Cannot find sample: $relative")

    private inline fun <reified T> write(path: Path, value: T): Path {
        Files.writeString(path, JsonSupport.json.encodeToString(value))
        return path
    }

    private fun hash(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
