package com.localnovel.engine

import com.localnovel.engine.service.RebootLocalWebServer
import com.localnovel.engine.support.JsonSupport
import java.net.URI
import java.net.Socket
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class RebootLocalWebServerTest {
    @Test
    fun servesLoopbackOnlySixTurnSliceWithDebugSaveAndLoad() {
        var roots = emptyList<Path>()
        RebootLocalWebServer(projectPath("samples/reboot01/vertical_slice_run"), 0).start().use { server ->
            assertTrue(server.bindingAddress.isLoopbackAddress)
            val base = "http://127.0.0.1:${server.port}"
            assertTrue(get("$base/").contains("塵行：大坪海岸深度切片"))
            val health = json(get("$base/api/health"))
            assertEquals("ok", health["status"]?.jsonPrimitive?.content)
            assertEquals("LOCAL_ENGINE_BACKED_SCENARIO", health["mode"]?.jsonPrimitive?.content)
            assertEquals("6", health["turnCount"]?.jsonPrimitive?.content)
            assertTrue(rawGet(server.port, "evil.example").startsWith("HTTP/1.1 403"))
            assertTrue(rawGet(server.port, "localhost.:${server.port}").startsWith("HTTP/1.1 403"))
            val foreignOrigin = client.send(
                HttpRequest.newBuilder(URI("$base/api/start"))
                    .header("Content-Type", "application/json")
                    .header("Origin", "http://evil.example")
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build(),
                HttpResponse.BodyHandlers.ofString(),
            )
            assertEquals(403, foreignOrigin.statusCode())

            var state = json(post("$base/api/start", "{}"))
            val sessionId = state["sessionId"]!!.jsonPrimitive.content
            repeat(6) { index ->
                val submittedInput = "本機測試行動 ${index + 1}"
                val body = buildJsonObject {
                    put("sessionId", sessionId)
                    put("playerInput", submittedInput)
                    put("debug", true)
                }.toString()
                state = json(post("$base/api/turn", body))
                assertEquals((index + 1).toString(), state["turnNumber"]?.jsonPrimitive?.content)
                assertEquals((index + 1).toString(), state["engineAppliedTurns"]?.jsonPrimitive?.content)
                assertEquals("LOCAL_ENGINE_BACKED_SCENARIO", state["mode"]?.jsonPrimitive?.content)
                assertFalse(state["story"]?.jsonPrimitive?.content.isNullOrBlank())
                assertTrue(state["debug"]?.jsonObject?.containsKey("context") == true)
                assertTrue(state["debug"]?.jsonObject?.containsKey("stateDelta") == true)
                assertEquals(submittedInput, state["debug"]!!.jsonObject["context"]!!.jsonObject["playerInput"]!!.jsonPrimitive.content)
            }
            assertEquals("true", state["completed"]?.jsonPrimitive?.content)
            assertEquals("T+35m", state["worldTime"]?.jsonPrimitive?.content)

            val save = get("$base/api/save?sessionId=$sessionId")
            val loaded = json(post("$base/api/load", save))
            assertEquals("6", loaded["turnNumber"]?.jsonPrimitive?.content)
            assertEquals("true", loaded["completed"]?.jsonPrimitive?.content)
            assertTrue(loaded["sessionId"]?.jsonPrimitive?.content != sessionId)
            roots = server.sessionWorkRootsForTest()
            assertTrue(roots.all(Files::exists))
        }
        assertTrue(roots.all { !Files.exists(it) })
    }

    @Test
    fun playerInputReachesContextAndProducesDifferentSqlEvidence() {
        RebootLocalWebServer(projectPath("samples/reboot01/vertical_slice_run"), 0).start().use { server ->
            val base = "http://127.0.0.1:${server.port}"
            val first = json(post("$base/api/start", "{}"))
            val second = json(post("$base/api/start", "{}"))
            val a = json(post("$base/api/turn", buildJsonObject {
                put("sessionId", first["sessionId"]!!.jsonPrimitive.content)
                put("playerInput", "先撤到高處，沿著安全線觀察。")
                put("debug", true)
            }.toString()))
            val b = json(post("$base/api/turn", buildJsonObject {
                put("sessionId", second["sessionId"]!!.jsonPrimitive.content)
                put("playerInput", "先帶愛醬撤退，再記下水線位置。")
                put("debug", true)
            }.toString()))
            val contextA = a["debug"]!!.jsonObject["context"]!!.jsonObject
            val contextB = b["debug"]!!.jsonObject["context"]!!.jsonObject
            assertEquals("先撤到高處，沿著安全線觀察。", contextA["playerInput"]!!.jsonPrimitive.content)
            assertEquals("先帶愛醬撤退，再記下水線位置。", contextB["playerInput"]!!.jsonPrimitive.content)
            assertTrue(contextA["contextHash"]!!.jsonPrimitive.content != contextB["contextHash"]!!.jsonPrimitive.content)
            assertTrue(a["debug"]!!.jsonObject["stateDelta"].toString().contains("先撤到高處"))
            assertTrue(b["debug"]!!.jsonObject["stateDelta"].toString().contains("先帶愛醬撤退"))
        }
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
    private fun projectPath(relative: String): Path = listOf(Path.of(relative), Path.of("..", relative)).firstOrNull(Files::exists) ?: error("Cannot find $relative")
}
