package com.localnovel.engine

import com.localnovel.engine.model.NpcActorDecisionSummary
import com.localnovel.engine.model.NpcActorOutputConstraints
import com.localnovel.engine.model.NpcActorRequest
import com.localnovel.engine.model.NpcActorResponse
import com.localnovel.engine.model.NpcAutonomyProfile
import com.localnovel.engine.model.NpcVoiceProfile
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NpcActorContractTest {
    @Test
    fun existingNpcProfileJsonReceivesBackwardCompatibleVoiceDefaults() {
        val profile = JsonSupport.json.decodeFromString<NpcAutonomyProfile>(
            Files.readString(samplePath("reboot01/shen_shuang_offscreen_profile.json")),
        )

        assertEquals("zh-Hant-TW", profile.voiceProfile.locale)
        assertEquals("natural_spoken_mandarin", profile.voiceProfile.register)
        assertTrue(profile.sandboxCandidateOnly)
    }

    @Test
    fun requestAndResponseSerializeWithoutPrivateReasoningSurface() {
        val request = NpcActorRequest(
            requestId = "request-contract-01",
            scenarioId = "scenario-contract-01",
            actorId = "npc-01",
            locationId = "location-01",
            voiceProfile = NpcVoiceProfile(),
            physicalState = "steady",
            emotionalState = "guarded",
            outputConstraints = NpcActorOutputConstraints(maxUtteranceChars = 32),
        )
        val response = NpcActorResponse(
            requestId = request.requestId,
            actorId = request.actorId,
            speechAct = "SILENCE",
            immediateGoal = "Avoid revealing unsupported information.",
            emotionShown = "calm",
            emotionHidden = "uneasy",
            actionType = "inspect_local_pressure",
            actionIntent = "Wait and observe.",
            visibleAction = "Looks toward the closed door.",
            utterance = "",
            nonverbalAction = "Tightens one hand around the cup.",
            predictedCost = "A short delay.",
            observableConsequence = "The actor withholds an answer.",
            confidence = 90,
            decisionSummary = NpcActorDecisionSummary(
                selectedActionLabel = "wait_and_observe",
            ),
        )

        val encodedRequest = JsonSupport.json.encodeToString(request)
        val encodedResponse = JsonSupport.json.encodeToString(response)
        assertEquals(request, JsonSupport.json.decodeFromString<NpcActorRequest>(encodedRequest))
        assertEquals(response, JsonSupport.json.decodeFromString<NpcActorResponse>(encodedResponse))
        assertFalse(encodedResponse.contains("reasoning", ignoreCase = true))
        assertFalse(encodedResponse.contains("chainOfThought", ignoreCase = true))
    }

    @Test
    fun schemasAreStrictAndEvaluationSuiteRequiresTwentyScenarios() {
        val parser = Json { ignoreUnknownKeys = false }
        val requestSchema = parser.parseToJsonElement(
            Files.readString(schemaPath("npc_actor_request_v1.schema.json")),
        ).jsonObject
        val responseSchema = parser.parseToJsonElement(
            Files.readString(schemaPath("npc_actor_response_v1.schema.json")),
        ).jsonObject
        val suiteSchema = parser.parseToJsonElement(
            Files.readString(schemaPath("npc_actor_evaluation_suite_v1.schema.json")),
        ).jsonObject

        assertEquals("false", requestSchema["additionalProperties"].toString())
        assertEquals("false", responseSchema["additionalProperties"].toString())
        val scenarios = suiteSchema["properties"]!!.jsonObject["scenarios"]!!.jsonObject
        assertEquals("20", scenarios["minItems"].toString())
        assertEquals("20", scenarios["maxItems"].toString())
    }

    private fun samplePath(relative: String): Path =
        listOf(Path.of("samples", relative), Path.of("..", "samples", relative))
            .firstOrNull(Files::exists)
            ?: error("Cannot find sample: $relative")

    private fun schemaPath(fileName: String): Path =
        listOf(Path.of("schemas", "npc_actor", fileName), Path.of("..", "schemas", "npc_actor", fileName))
            .firstOrNull(Files::exists)
            ?: error("Cannot find schema: $fileName")
}
