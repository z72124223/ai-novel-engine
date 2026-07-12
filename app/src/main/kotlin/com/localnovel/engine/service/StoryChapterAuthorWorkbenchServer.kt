package com.localnovel.engine.service

import com.localnovel.engine.model.StoryChapterPilotSelection
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.repository.StoryChapterProductionRepository
import com.localnovel.engine.support.JsonSupport
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class StoryChapterAuthorWorkbenchServer(
    private val sourceDatabasePath: Path,
    private val productionId: String,
    private val requestedPort: Int = 0,
) : AutoCloseable {
    private val workRoot: Path = Files.createTempDirectory("story01-author-workbench-")
    private val workingDatabasePath: Path = workRoot.resolve("story.db")
    private val repository: SqliteStoryRepository
    private val chapter: StoryChapterProductionRepository
    private var server: HttpServer? = null
    private var executor: ExecutorService? = null
    @Volatile private var closing = false

    init {
        require(Files.isRegularFile(sourceDatabasePath)) { "STORY_DATABASE_NOT_FOUND" }
        Files.copy(sourceDatabasePath, workingDatabasePath)
        repository = SqliteStoryRepository(workingDatabasePath).also { it.initialize() }
        chapter = StoryChapterProductionRepository(repository)
        chapter.get(productionId)
    }

    val port: Int get() = server?.address?.port ?: error("Server has not started")
    val bindingAddress: InetAddress get() = server?.address?.address ?: error("Server has not started")

    fun start(): StoryChapterAuthorWorkbenchServer {
        check(server == null) { "Server already started" }
        val created = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), requestedPort), 0)
        val createdExecutor = Executors.newFixedThreadPool(2)
        created.executor = createdExecutor
        created.createContext("/") { exchange -> route(exchange) }
        created.start()
        server = created
        executor = createdExecutor
        return this
    }

    override fun close() {
        closing = true
        server?.stop(0)
        executor?.shutdownNow()
        server = null
        executor = null
        if (Files.exists(workRoot)) Files.walk(workRoot).use { paths ->
            paths.sorted(Comparator.reverseOrder()).forEach(Files::deleteIfExists)
        }
    }

    private fun route(exchange: HttpExchange) {
        if (closing) return sendError(exchange, 503, "SERVER_CLOSING")
        if (!isTrusted(exchange)) return sendError(exchange, 403, "LOOPBACK_ORIGIN_REQUIRED")
        runCatching {
            when {
                exchange.requestMethod == "GET" && exchange.requestURI.path == "/" -> sendResource(exchange)
                exchange.requestMethod == "GET" && exchange.requestURI.path == "/api/health" -> sendJson(exchange, 200, buildJsonObject {
                    put("status", "ok")
                    put("mode", "STORY01_ISOLATED_AUTHOR_WORKBENCH")
                    put("loopbackOnly", true)
                    put("sourceDatabaseMutable", false)
                })
                exchange.requestMethod == "GET" && exchange.requestURI.path == "/api/chapter" -> sendChapter(exchange)
                exchange.requestMethod == "POST" && exchange.requestURI.path == "/api/chapter/decision" -> applyDecision(exchange)
                else -> sendError(exchange, 404, "NOT_FOUND")
            }
        }.onFailure { if (exchange.responseCode == -1) sendError(exchange, 400, it.message ?: "INVALID_REQUEST") }
    }

    private fun sendChapter(exchange: HttpExchange) {
        sendJson(exchange, 200, JsonSupport.json.encodeToJsonElement(com.localnovel.engine.model.StoryChapterProductionView.serializer(), chapter.get(productionId)))
    }

    private fun applyDecision(exchange: HttpExchange) {
        val body = requestJson(exchange)
        require(body.string("productionId") == productionId) { "PRODUCTION_ID_MISMATCH" }
        val actor = body.string("actorId")
        val before = chapter.get(productionId)
        when (body.string("decision")) {
            "SELECT_INITIAL" -> chapter.selectPilot(StoryChapterPilotSelection(productionId, before.initialDraftSha256, "APPROVE_PILOT_DRAFT", actor))
            "SELECT_REVISED" -> chapter.selectPilot(StoryChapterPilotSelection(productionId, before.revisedDraftSha256, "APPROVE_PILOT_DRAFT", actor))
            "REJECT" -> chapter.rejectPilot(productionId, actor)
            "REQUIRE_HUMAN_DECISION" -> chapter.requireHumanDecision(productionId)
            else -> error("DECISION_INVALID")
        }
        sendChapter(exchange)
    }

    private fun isTrusted(exchange: HttpExchange): Boolean {
        val host = exchange.requestHeaders.getFirst("Host") ?: return false
        if (!isLoopback(runCatching { URI("http://$host") }.getOrNull())) return false
        val origin = exchange.requestHeaders.getFirst("Origin") ?: return true
        return isLoopback(runCatching { URI(origin) }.getOrNull())
    }

    private fun isLoopback(uri: URI?): Boolean {
        if (uri == null || uri.scheme != "http" || uri.userInfo != null) return false
        if (uri.host?.lowercase() !in setOf("localhost", "127.0.0.1", "::1", "[::1]")) return false
        return (if (uri.port == -1) 80 else uri.port) == port
    }

    private fun requestJson(exchange: HttpExchange): JsonObject {
        val bytes = exchange.requestBody.readNBytes(65_537)
        require(bytes.size <= 65_536) { "REQUEST_TOO_LARGE" }
        return JsonSupport.json.parseToJsonElement(bytes.toString(StandardCharsets.UTF_8)).jsonObject
    }

    private fun sendResource(exchange: HttpExchange) {
        val bytes = requireNotNull(javaClass.getResourceAsStream("/story01-web/index.html")) { "WEB_RESOURCE_MISSING" }.use { it.readBytes() }
        exchange.responseHeaders.set("Content-Type", "text/html; charset=utf-8")
        send(exchange, 200, bytes)
    }

    private fun sendJson(exchange: HttpExchange, status: Int, body: JsonElement) {
        exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
        exchange.responseHeaders.set("Cache-Control", "no-store")
        send(exchange, status, JsonSupport.json.encodeToString(JsonElement.serializer(), body).toByteArray(StandardCharsets.UTF_8))
    }

    private fun sendError(exchange: HttpExchange, status: Int, code: String) = sendJson(exchange, status, buildJsonObject { put("error", code) })
    private fun send(exchange: HttpExchange, status: Int, bytes: ByteArray) {
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }

    private fun JsonObject.string(key: String): String = this[key]?.jsonPrimitive?.contentOrNull ?: error("${key.uppercase()}_REQUIRED")

    internal fun workingDatabasePathForTest(): Path = workingDatabasePath
    internal fun workRootForTest(): Path = workRoot
}
