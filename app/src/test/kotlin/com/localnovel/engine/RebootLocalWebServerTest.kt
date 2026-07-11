package com.localnovel.engine

import com.localnovel.engine.service.RebootLocalWebServer
import com.localnovel.engine.support.JsonSupport
import java.net.URI
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
        RebootLocalWebServer(projectPath("samples/reboot01/vertical_slice_run"), 0).start().use { server ->
            assertTrue(server.bindingAddress.isLoopbackAddress)
            val base = "http://127.0.0.1:${server.port}"
            assertTrue(get("$base/").contains("塵行：大坪海岸深度切片"))
            val health = json(get("$base/api/health"))
            assertEquals("ok", health["status"]?.jsonPrimitive?.content)
            assertEquals("6", health["turnCount"]?.jsonPrimitive?.content)

            var state = json(post("$base/api/start", "{}"))
            val sessionId = state["sessionId"]!!.jsonPrimitive.content
            repeat(6) { index ->
                val body = buildJsonObject {
                    put("sessionId", sessionId)
                    put("playerInput", "本機測試行動 ${index + 1}")
                    put("debug", true)
                }.toString()
                state = json(post("$base/api/turn", body))
                assertEquals((index + 1).toString(), state["turnNumber"]?.jsonPrimitive?.content)
                assertFalse(state["story"]?.jsonPrimitive?.content.isNullOrBlank())
                assertTrue(state["debug"]?.jsonObject?.containsKey("context") == true)
                assertTrue(state["debug"]?.jsonObject?.containsKey("stateDelta") == true)
            }
            assertEquals("true", state["completed"]?.jsonPrimitive?.content)
            assertEquals("T+35m", state["worldTime"]?.jsonPrimitive?.content)

            val save = get("$base/api/save?sessionId=$sessionId")
            val loaded = json(post("$base/api/load", save))
            assertEquals("6", loaded["turnNumber"]?.jsonPrimitive?.content)
            assertEquals("true", loaded["completed"]?.jsonPrimitive?.content)
            assertTrue(loaded["sessionId"]?.jsonPrimitive?.content != sessionId)
        }
    }

    private val client = HttpClient.newHttpClient()
    private fun get(url: String): String = client.send(HttpRequest.newBuilder(URI(url)).GET().build(), HttpResponse.BodyHandlers.ofString()).body()
    private fun post(url: String, body: String): String = client.send(
        HttpRequest.newBuilder(URI(url)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build(),
        HttpResponse.BodyHandlers.ofString(),
    ).body()
    private fun json(value: String) = JsonSupport.json.parseToJsonElement(value).jsonObject
    private fun projectPath(relative: String): Path = listOf(Path.of(relative), Path.of("..", relative)).firstOrNull(Files::exists) ?: error("Cannot find $relative")
}
