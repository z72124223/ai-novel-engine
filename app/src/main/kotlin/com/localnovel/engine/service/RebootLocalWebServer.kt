package com.localnovel.engine.service

import com.localnovel.engine.support.JsonSupport
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
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
) : AutoCloseable {
    private data class UiSession(val id: String, var turnNumber: Int = 0, val playerInputs: MutableList<String> = mutableListOf())
    private data class TurnEvidence(
        val turnNumber: Int,
        val context: JsonObject,
        val result: JsonObject,
        val drama: JsonObject?,
        val decisionTrace: JsonObject?,
        val narrative: String,
    )

    private val sessions = ConcurrentHashMap<String, UiSession>()
    private val turns = loadTurns()
    private var server: HttpServer? = null

    val port: Int get() = server?.address?.port ?: error("Server has not started")
    val bindingAddress: InetAddress get() = server?.address?.address ?: error("Server has not started")

    fun start(): RebootLocalWebServer {
        check(server == null) { "Server already started" }
        val created = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), requestedPort), 0)
        created.executor = Executors.newFixedThreadPool(4)
        created.createContext("/") { exchange -> route(exchange) }
        created.start()
        server = created
        return this
    }

    override fun close() {
        server?.stop(0)
        server = null
    }

    private fun route(exchange: HttpExchange) {
        runCatching {
            when {
                exchange.requestMethod == "GET" && exchange.requestURI.path == "/" -> sendResource(exchange)
                exchange.requestMethod == "GET" && exchange.requestURI.path == "/api/health" -> sendJson(exchange, 200, buildJsonObject {
                    put("status", "ok")
                    put("mode", "LOCAL_VERIFIED_REPLAY")
                    put("turnCount", turns.size)
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

    private fun startSession(exchange: HttpExchange) {
        val session = UiSession("web-${UUID.randomUUID()}")
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
        require(session.turnNumber < turns.size) { "SLICE_ALREADY_COMPLETE" }
        session.playerInputs += input
        session.turnNumber += 1
        sendJson(exchange, 200, view(session, body["debug"]?.jsonPrimitive?.booleanOrNull == true))
    }

    private fun saveSession(exchange: HttpExchange) {
        val sessionId = query(exchange, "sessionId") ?: error("SESSION_ID_REQUIRED")
        val session = sessions[sessionId] ?: error("SESSION_NOT_FOUND")
        sendJson(exchange, 200, buildJsonObject {
            put("format", "reboot01-local-save-v1")
            put("scenarioSeed", "reboot01-chenxing-depth-v1")
            put("turnNumber", session.turnNumber)
            put("playerInputs", JsonArray(session.playerInputs.map(::JsonPrimitive)))
        })
    }

    private fun loadSession(exchange: HttpExchange) {
        val body = requestJson(exchange)
        require(body.string("format") == "reboot01-local-save-v1") { "SAVE_FORMAT_INVALID" }
        require(body.string("scenarioSeed") == "reboot01-chenxing-depth-v1") { "SAVE_SCENARIO_INVALID" }
        val turnNumber = body["turnNumber"]?.jsonPrimitive?.intOrNull ?: error("SAVE_TURN_INVALID")
        require(turnNumber in 0..turns.size) { "SAVE_TURN_INVALID" }
        val inputs = body["playerInputs"]?.jsonArray?.map { it.jsonPrimitive.content }.orEmpty()
        require(inputs.size == turnNumber && inputs.all { it.isNotBlank() && it.length <= 2000 }) { "SAVE_INPUTS_INVALID" }
        val session = UiSession("web-${UUID.randomUUID()}", turnNumber, inputs.toMutableList())
        sessions[session.id] = session
        sendJson(exchange, 200, view(session, debug = false))
    }

    private fun view(session: UiSession, debug: Boolean): JsonObject {
        val evidence = session.turnNumber.takeIf { it > 0 }?.let { turns[it - 1] }
        val context = evidence?.context ?: turns.first().context
        val characters = context["characters"]?.jsonArray ?: JsonArray(emptyList())
        val worldTime = if (session.turnNumber == turns.size) "T+35m" else context["worldClock"]?.jsonObject?.get("worldTime")?.jsonPrimitive?.contentOrNull.orEmpty()
        return buildJsonObject {
            put("sessionId", session.id)
            put("mode", "LOCAL_VERIFIED_REPLAY")
            put("modeNotice", "This local slice records player input and advances through six engine-verified outcomes; it does not call an external model.")
            put("turnNumber", session.turnNumber)
            put("turnCount", turns.size)
            put("completed", session.turnNumber == turns.size)
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
            put("suggestedInput", suggestedInput((session.turnNumber + 1).coerceAtMost(turns.size)))
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

    private fun loadTurns(): List<TurnEvidence> = (1..6).map { number ->
        val stem = "turn-${number.toString().padStart(2, '0')}"
        TurnEvidence(
            turnNumber = number,
            context = readJson("$stem-context.json"),
            result = readJson("$stem-result.json"),
            drama = optionalJson("$stem-drama.json"),
            decisionTrace = optionalJson("$stem-decision-trace-row.json"),
            narrative = Files.readString(evidenceDir.resolve("$stem-reader-narrative.md"), StandardCharsets.UTF_8),
        )
    }

    private fun readJson(name: String): JsonObject = JsonSupport.json.parseToJsonElement(Files.readString(evidenceDir.resolve(name), StandardCharsets.UTF_8)).jsonObject
    private fun optionalJson(name: String): JsonObject? = evidenceDir.resolve(name).takeIf(Files::exists)?.let { JsonSupport.json.parseToJsonElement(Files.readString(it, StandardCharsets.UTF_8)).jsonObject }

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
