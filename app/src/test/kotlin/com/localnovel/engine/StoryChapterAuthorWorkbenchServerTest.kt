package com.localnovel.engine

import com.localnovel.engine.model.StoryChapterDraftIntake
import com.localnovel.engine.model.StoryChapterProductionSeed
import com.localnovel.engine.model.StoryChapterProductionStatus
import com.localnovel.engine.model.StoryChapterReviewRecord
import com.localnovel.engine.model.StoryChapterReviewerRole
import com.localnovel.engine.model.StoryChapterReviewVerdict
import com.localnovel.engine.model.StoryChapterRevisionIntake
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.repository.StoryChapterProductionRepository
import com.localnovel.engine.service.StoryChapterAuthorWorkbenchServer
import com.localnovel.engine.support.JsonSupport
import java.net.Socket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class StoryChapterAuthorWorkbenchServerTest {
    @Test
    fun servesReviewsAndAppliesDecisionOnlyToIsolatedDatabase() {
        val source = fixture()
        var workRoot: Path? = null
        StoryChapterAuthorWorkbenchServer(source.databasePath(), PRODUCTION_ID, 0).start().use { server ->
            assertTrue(server.bindingAddress.isLoopbackAddress)
            val base = "http://127.0.0.1:${server.port}"
            assertTrue(get("$base/").contains("STORY-01 作者工作台"))
            assertEquals("STORY01_ISOLATED_AUTHOR_WORKBENCH", json(get("$base/api/health"))["mode"]!!.jsonPrimitive.content)
            assertTrue(rawGet(server.port, "evil.example").startsWith("HTTP/1.1 403"))
            val foreign = client.send(
                HttpRequest.newBuilder(URI("$base/api/chapter"))
                    .header("Origin", "http://evil.example")
                    .GET().build(),
                HttpResponse.BodyHandlers.ofString(),
            )
            assertEquals(403, foreign.statusCode())

            val before = json(get("$base/api/chapter"))
            assertEquals("REVISION_INGESTED", before["status"]!!.jsonPrimitive.content)
            assertEquals(3, before["reviews"]!!.jsonArray.size)
            assertTrue(before["initialDraft"]!!.jsonPrimitive.content.contains("初稿"))
            assertTrue(before["revisedDraft"]!!.jsonPrimitive.content.contains("修訂"))

            val selected = json(post("$base/api/chapter/decision", buildJsonObject {
                put("productionId", PRODUCTION_ID)
                put("decision", "SELECT_REVISED")
                put("actorId", "human:test-author")
            }.toString()))
            assertEquals("SELECTED_PILOT_DRAFT", selected["status"]!!.jsonPrimitive.content)
            assertEquals("NON_CANON_PILOT", selected["canonStatus"]!!.jsonPrimitive.content)
            workRoot = server.workRootForTest()
            assertTrue(Files.exists(server.workingDatabasePathForTest()))
        }
        assertFalse(Files.exists(workRoot!!))
        assertEquals(
            StoryChapterProductionStatus.REVISION_INGESTED,
            StoryChapterProductionRepository(source).get(PRODUCTION_ID).status,
            "Workbench decision must not mutate the source database.",
        )
    }

    private fun fixture(): SqliteStoryRepository {
        val repository = SqliteStoryRepository(Files.createTempDirectory("story-workbench-source").resolve("story.db")).also { it.initialize() }
        val chapter = StoryChapterProductionRepository(repository)
        val packet = "{\"source\":\"sql\"}"
        val initial = "初稿全文"
        val revised = "修訂全文"
        chapter.create(StoryChapterProductionSeed(PRODUCTION_ID, "chenxing", "session", "seed", "commit", packet, hash(packet)))
        chapter.ingestDraft(StoryChapterDraftIntake(PRODUCTION_ID, hash(packet), initial, hash(initial)))
        chapter.recordReviewBatch(PRODUCTION_ID, StoryChapterReviewerRole.entries.map { role ->
            StoryChapterReviewRecord(
                reviewId = "review-${role.name}",
                productionId = PRODUCTION_ID,
                reviewerRole = role,
                reviewerInstanceId = "agent-${role.name}",
                sourceDraftSha256 = hash(initial),
                verdict = if (role == StoryChapterReviewerRole.CHARACTER) StoryChapterReviewVerdict.PASS else StoryChapterReviewVerdict.REVISE,
                reviewJson = "{\"summary\":\"全文評審 ${role.name}\"}",
            )
        })
        chapter.ingestRevision(StoryChapterRevisionIntake(PRODUCTION_ID, hash(initial), revised, hash(revised)))
        return repository
    }

    private val client = HttpClient.newHttpClient()
    private fun get(url: String): String = client.send(HttpRequest.newBuilder(URI(url)).GET().build(), HttpResponse.BodyHandlers.ofString()).body()
    private fun post(url: String, body: String): String = client.send(
        HttpRequest.newBuilder(URI(url)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build(),
        HttpResponse.BodyHandlers.ofString(),
    ).body()
    private fun json(value: String) = JsonSupport.json.parseToJsonElement(value).jsonObject
    private fun rawGet(port: Int, host: String): String = Socket("127.0.0.1", port).use { socket ->
        socket.getOutputStream().write("GET /api/health HTTP/1.1\r\nHost: $host\r\nConnection: close\r\n\r\n".toByteArray())
        socket.getOutputStream().flush()
        socket.getInputStream().bufferedReader().readText()
    }

    private fun hash(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

    private companion object { const val PRODUCTION_ID = "story01-workbench" }
}
