package com.localnovel.engine.service

import com.localnovel.engine.support.JsonSupport
import com.localnovel.engine.repository.SqliteStoryRepository
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class RebootLocalWebServer(
    private val evidenceDir: Path,
    private val requestedPort: Int = 0,
    private val basePackPath: Path = Path.of("world/chenxing/engine/chenxing_setting_pack_v1.json"),
    private val worldPatchPath: Path = Path.of("samples/reboot01/chenxing_world_patch_candidate_v1.json"),
    private val npcProfilePath: Path = Path.of("samples/reboot01/lin_yao_offscreen_profile.json"),
    private val approvalGrantPath: Path = Path.of("samples/reboot01/irreversible_approval_grant.json"),
) : AutoCloseable {
    private data class UiSession(
        val id: String,
        val workRoot: Path,
        var turnNumber: Int = 0,
        val playerInputs: MutableList<String> = mutableListOf(),
        var liveEvidenceDir: Path? = null,
    )
    private data class TurnEvidence(
        val turnNumber: Int,
        val context: JsonObject,
        val result: JsonObject,
        val drama: JsonObject?,
        val decisionTrace: JsonObject?,
        val narrative: String,
    )

    private val sessions = ConcurrentHashMap<String, UiSession>()
    private val fixtureTurns = loadTurns(evidenceDir, 6)
    private val lifecycleLock = Any()
    @Volatile private var closing = false
    private var server: HttpServer? = null
    private var executor: ExecutorService? = null

    val port: Int get() = server?.address?.port ?: error("Server has not started")
    val bindingAddress: InetAddress get() = server?.address?.address ?: error("Server has not started")

    fun start(): RebootLocalWebServer {
        check(server == null) { "Server already started" }
        val created = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), requestedPort), 0)
        val createdExecutor = Executors.newFixedThreadPool(4)
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
        synchronized(lifecycleLock) {
            sessions.values.forEach { deleteDirectory(it.workRoot) }
            sessions.clear()
            server = null
            executor = null
        }
    }

    private fun route(exchange: HttpExchange) {
        synchronized(lifecycleLock) {
            if (closing) {
                sendError(exchange, 503, "SERVER_CLOSING")
                return
            }
            if (!isTrustedBrowserRequest(exchange)) {
                sendError(exchange, 403, "LOOPBACK_ORIGIN_REQUIRED")
                return
            }
            runCatching {
                when {
                    exchange.requestMethod == "GET" && exchange.requestURI.path == "/" -> sendResource(exchange)
                    exchange.requestMethod == "GET" && exchange.requestURI.path == "/api/health" -> sendJson(exchange, 200, buildJsonObject {
                        put("status", "ok")
                        put("mode", "LOCAL_ENGINE_BACKED_SCENARIO")
                        put("resultAdapter", "DETERMINISTIC_SCENARIO_ADAPTER")
                        put("turnCount", fixtureTurns.size)
                        put("loopbackOnly", true)
                    })
                    exchange.requestMethod == "POST" && exchange.requestURI.path == "/api/start" -> startSession(exchange)
                    exchange.requestMethod == "POST" && exchange.requestURI.path == "/api/turn" -> advanceTurn(exchange)
                    exchange.requestMethod == "GET" && exchange.requestURI.path == "/api/save" -> saveSession(exchange)
                    exchange.requestMethod == "POST" && exchange.requestURI.path == "/api/load" -> loadSession(exchange)
                    else -> sendError(exchange, 404, "NOT_FOUND")
                }
            }.onFailure { failure ->
                if (exchange.responseCode == -1) sendError(exchange, 400, failure.message ?: "INVALID_REQUEST")
            }
        }
    }

    private fun isTrustedBrowserRequest(exchange: HttpExchange): Boolean {
        val host = exchange.requestHeaders.getFirst("Host") ?: return false
        if (!isLoopbackUri(runCatching { URI("http://$host") }.getOrNull())) return false
        val origin = exchange.requestHeaders.getFirst("Origin") ?: return true
        return isLoopbackUri(runCatching { URI(origin) }.getOrNull())
    }

    private fun isLoopbackUri(uri: URI?): Boolean {
        if (uri == null || uri.scheme != "http" || uri.userInfo != null) return false
        val literalHost = uri.host?.lowercase() ?: return false
        if (literalHost !in setOf("localhost", "127.0.0.1", "::1", "[::1]")) return false
        val effectivePort = if (uri.port == -1) 80 else uri.port
        return effectivePort == port
    }

    private fun startSession(exchange: HttpExchange) {
        val session = UiSession("web-${UUID.randomUUID()}", Files.createTempDirectory("reboot01-web-session-"))
        sessions[session.id] = session
        sendJson(exchange, 200, view(session, debug = false))
    }

    private fun advanceTurn(exchange: HttpExchange) {
        val body = requestJson(exchange)
        val sessionId = body.string("sessionId")
        val input = body.string("playerInput").trim()
        require(input.isNotEmpty()) { "PLAYER_INPUT_REQUIRED" }
        require(input.length <= 2000) { "PLAYER_INPUT_TOO_LONG" }
        val session = sessions[sessionId] ?: error("SESSION_NOT_FOUND")
        val response = synchronized(session) {
            require(session.turnNumber < fixtureTurns.size) { "SLICE_ALREADY_COMPLETE" }
            val nextTurn = session.turnNumber + 1
            val proposedInputs = session.playerInputs + input
            executeEngineThrough(session, nextTurn, proposedInputs)
            session.playerInputs.clear()
            session.playerInputs.addAll(proposedInputs)
            session.turnNumber = nextTurn
            view(session, body["debug"]?.jsonPrimitive?.booleanOrNull == true)
        }
        sendJson(exchange, 200, response)
    }

    private fun saveSession(exchange: HttpExchange) {
        val sessionId = query(exchange, "sessionId") ?: error("SESSION_ID_REQUIRED")
        val session = sessions[sessionId] ?: error("SESSION_NOT_FOUND")
        val response = synchronized(session) {
            buildJsonObject {
                put("format", "reboot01-engine-save-v2")
                put("scenarioSeed", "reboot01-chenxing-depth-v1")
                put("turnNumber", session.turnNumber)
                put("playerInputs", JsonArray(session.playerInputs.map(::JsonPrimitive)))
            }
        }
        sendJson(exchange, 200, response)
    }

    private fun loadSession(exchange: HttpExchange) {
        val body = requestJson(exchange)
        require(body.string("format") == "reboot01-engine-save-v2") { "SAVE_FORMAT_INVALID" }
        require(body.string("scenarioSeed") == "reboot01-chenxing-depth-v1") { "SAVE_SCENARIO_INVALID" }
        val turnNumber = body["turnNumber"]?.jsonPrimitive?.intOrNull ?: error("SAVE_TURN_INVALID")
        require(turnNumber in 0..fixtureTurns.size) { "SAVE_TURN_INVALID" }
        val inputs = body["playerInputs"]?.jsonArray?.map { it.jsonPrimitive.content }.orEmpty()
        require(inputs.size == turnNumber && inputs.all { it.isNotBlank() && it.length <= 2000 }) { "SAVE_INPUTS_INVALID" }
        val workRoot = Files.createTempDirectory("reboot01-web-session-")
        val session = UiSession(
            id = "web-${UUID.randomUUID()}",
            workRoot = workRoot,
            turnNumber = turnNumber,
            playerInputs = inputs.toMutableList(),
        )
        try {
            if (turnNumber > 0) executeEngineThrough(session, turnNumber, inputs)
            val response = view(session, debug = false)
            sessions[session.id] = session
            sendJson(exchange, 200, response)
        } catch (failure: Throwable) {
            deleteDirectory(workRoot)
            throw failure
        }
    }

    private fun view(session: UiSession, debug: Boolean): JsonObject {
        val liveTurns = session.liveEvidenceDir?.let { loadTurns(it, session.turnNumber) }.orEmpty()
        val evidence = liveTurns.lastOrNull()
        val context = evidence?.context ?: fixtureTurns.first().context
        val characters = context["characters"]?.jsonArray ?: JsonArray(emptyList())
        val worldTime = if (session.turnNumber == fixtureTurns.size) "T+35m" else context["worldClock"]?.jsonObject?.get("worldTime")?.jsonPrimitive?.contentOrNull.orEmpty()
        return buildJsonObject {
            put("sessionId", session.id)
            put("mode", "LOCAL_ENGINE_BACKED_SCENARIO")
            put("modeNotice", "Each submit rebuilds an isolated SQLite sandbox and executes the formal prepare/validate/resolve path through the selected turn. Narrative resolution is a bounded deterministic scenario adapter, not an external model or free-form generator.")
            put("turnNumber", session.turnNumber)
            put("turnCount", fixtureTurns.size)
            put("completed", session.turnNumber == fixtureTurns.size)
            put("engineAppliedTurns", session.turnNumber)
            put("isolatedDatabase", true)
            put("worldTime", worldTime)
            put("story", evidence?.narrative.orEmpty())
            put("characters", buildJsonArray {
                characters.forEach { character ->
                    val item = character.jsonObject
                    add(buildJsonObject {
                        put("id", item["id"] ?: JsonPrimitive(""))
                        put("name", item["name"] ?: JsonPrimitive(""))
                        put("role", item["role"] ?: JsonPrimitive(""))
                    })
                }
            })
            put("relationshipSummary", relationshipSummary(session.turnNumber))
            put("suggestedInput", suggestedInput((session.turnNumber + 1).coerceAtMost(fixtureTurns.size)))
            if (debug && evidence != null) {
                put("debug", buildJsonObject {
                    put("context", evidence.context)
                    put("critic", evidence.drama ?: JsonNull)
                    put("decisionTrace", evidence.decisionTrace ?: JsonNull)
                    put("stateDelta", evidence.result["stateDeltas"] ?: JsonArray(emptyList()))
                })
            }
        }
    }

    private fun executeEngineThrough(session: UiSession, targetTurn: Int, submittedInputs: List<String>) {
        val attemptRoot = session.workRoot.resolve("attempt-${UUID.randomUUID()}")
        Files.createDirectories(attemptRoot)
        val liveDir = attemptRoot.resolve("evidence")
        val inputs = submittedInputs + RebootVerticalSliceRunner.defaultPlayerInputs.drop(submittedInputs.size)
        try {
            RebootVerticalSliceRunner(
                repository = SqliteStoryRepository(attemptRoot.resolve("story.db")),
                outputDir = liveDir,
            ).run(
                basePackPath = resolveProjectPath(basePackPath),
                addonPackPath = resolveProjectPath(worldPatchPath),
                npcProfilePath = resolveProjectPath(npcProfilePath),
                approvalGrantPath = resolveProjectPath(approvalGrantPath),
                playerInputs = inputs,
                maxTurns = targetTurn,
            )
        } catch (failure: Throwable) {
            deleteDirectory(attemptRoot)
            throw failure
        }
        val previousAttempt = session.liveEvidenceDir?.parent
        session.liveEvidenceDir = liveDir
        if (previousAttempt != null && previousAttempt != attemptRoot) deleteDirectory(previousAttempt)
    }

    private fun loadTurns(sourceDir: Path, count: Int): List<TurnEvidence> = (1..count).map { number ->
        val stem = "turn-${number.toString().padStart(2, '0')}"
        TurnEvidence(
            turnNumber = number,
            context = readJson(sourceDir, "$stem-context.json"),
            result = readJson(sourceDir, "$stem-result.json"),
            drama = optionalJson(sourceDir, "$stem-drama.json"),
            decisionTrace = optionalJson(sourceDir, "$stem-decision-trace-row.json"),
            narrative = Files.readString(sourceDir.resolve("$stem-reader-narrative.md"), StandardCharsets.UTF_8),
        )
    }

    private fun readJson(sourceDir: Path, name: String): JsonObject = JsonSupport.json.parseToJsonElement(Files.readString(sourceDir.resolve(name), StandardCharsets.UTF_8)).jsonObject
    private fun optionalJson(sourceDir: Path, name: String): JsonObject? = sourceDir.resolve(name).takeIf(Files::exists)?.let { JsonSupport.json.parseToJsonElement(Files.readString(it, StandardCharsets.UTF_8)).jsonObject }

    private fun resolveProjectPath(path: Path): Path = listOf(path, Path.of("..").resolve(path)).firstOrNull(Files::exists)
        ?: error("PROJECT_PATH_NOT_FOUND:${path.toString().replace('\\', '/')}")

    private fun deleteDirectory(path: Path) {
        if (Files.exists(path)) Files.walk(path).use { stream -> stream.sorted(Comparator.reverseOrder()).forEach(Files::deleteIfExists) }
    }

    internal fun sessionWorkRootsForTest(): List<Path> = sessions.values.map { it.workRoot }

    private fun requestJson(exchange: HttpExchange): JsonObject {
        val bytes = exchange.requestBody.readNBytes(65_537)
        require(bytes.size <= 65_536) { "REQUEST_TOO_LARGE" }
        return JsonSupport.json.parseToJsonElement(bytes.toString(StandardCharsets.UTF_8)).jsonObject
    }

    private fun sendResource(exchange: HttpExchange) {
        val bytes = requireNotNull(javaClass.getResourceAsStream("/reboot01-web/index.html")) { "WEB_RESOURCE_MISSING" }.use { it.readBytes() }
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

    private fun query(exchange: HttpExchange, key: String): String? = exchange.requestURI.rawQuery.orEmpty().split('&').mapNotNull {
        val parts = it.split('=', limit = 2)
        if (parts.size == 2 && parts[0] == key) java.net.URLDecoder.decode(parts[1], StandardCharsets.UTF_8) else null
    }.firstOrNull()

    private fun JsonObject.string(key: String): String = this[key]?.jsonPrimitive?.contentOrNull ?: error("${key.uppercase()}_REQUIRED")

    private fun relationshipSummary(turn: Int): String = when (turn) {
        0 -> "陸塵行與愛醬互相信任；其餘人物仍未建立可靠關係。"
        1 -> "陸塵行先保護愛醬撤離，兩者維持穩定搭檔關係。"
        2 -> "林遙在主角不在場時獨立保全證據，尚未取得主角信任。"
        3 -> "林遙留下的記號獲得驗證；陸塵行對她的信任上升。"
        4 -> "拾潮者利用主角的安全選擇轉移證據，對立加深。"
        5 -> "拾潮者永久受傷，敵意與救援責任同時存在。"
        else -> "救援換來有條件的停火；第二名搬運者仍是未解問題。"
    }

    private fun suggestedInput(turn: Int): String = when (turn) {
        1 -> "先離開潮間帶，別讓看得見的危險替我們做決定。"
        2 -> "讓留在現場的人先保存證據，我們從高處觀察。"
        3 -> "等水線穩住再回去，看誰趁我們不在動過現場。"
        4 -> "先找退路，再追碎片。明知道是陷阱還直走，那叫替對方省事。"
        5 -> "潮溝要塌了，先把人拉出來。"
        else -> "先救人，再問他把東西交給了誰。線索可以追，手接不回來。"
    }
}
