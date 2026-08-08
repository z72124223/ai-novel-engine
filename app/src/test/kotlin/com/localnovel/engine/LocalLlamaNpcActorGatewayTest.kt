package com.localnovel.engine

import com.localnovel.engine.model.LocalNpcActorProcessResult
import com.localnovel.engine.model.LocalNpcActorRuntimeConfig
import com.localnovel.engine.model.NpcActorDecisionSummary
import com.localnovel.engine.model.NpcActorPhysicalPrompt
import com.localnovel.engine.model.NpcActorPhysicalResponse
import com.localnovel.engine.model.NpcActorRequest
import com.localnovel.engine.model.NpcActorResponse
import com.localnovel.engine.model.NpcVoiceProfile
import com.localnovel.engine.model.PhysicalActionProposal
import com.localnovel.engine.model.PhysicalActionType
import com.localnovel.engine.service.LlamaCppNpcActorPhysicalGateway
import com.localnovel.engine.service.LocalNpcActorProcessExecutor
import com.localnovel.engine.service.NpcActorJson
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.encodeToString

class LocalLlamaNpcActorGatewayTest {
    @Test
    fun `valid transcript wrapped JSON is strictly parsed and audited`() {
        val fixture = Fixture()
        val response = fixture.response()
        val executor = writingExecutor("User:\nignored\n\nAssistant:\n${NpcActorJson.strict.encodeToString(response)}\n")
        val result = fixture.gateway(executor).generate(fixture.prompt())

        assertEquals(response, result.response)
        assertEquals("LOCAL_LLAMA_CPP_DIRECT_CLI", result.audit.provider)
        assertFalse(result.audit.serverStarted)
        assertFalse(result.audit.formalStateWritePerformed)
        assertEquals(64, result.audit.promptSha256.length)
        assertFalse(result.promptText.contains("engineContext"))
        assertFalse(result.promptText.contains("gravityMps2"))
    }

    @Test
    fun `unknown response field is rejected by strict parser`() {
        val fixture = Fixture()
        val json = NpcActorJson.strict.encodeToString(fixture.response()).dropLast(2) + ",\n  \"unexpected\": true\n}"
        val error = assertFailsWith<IllegalArgumentException> {
            fixture.gateway(writingExecutor(json)).generate(fixture.prompt())
        }
        assertTrue(error.message.orEmpty().contains("unknown key", ignoreCase = true))
    }

    @Test
    fun `text around JSON and request identity drift fail closed`() {
        val fixture = Fixture()
        assertFailsWith<IllegalArgumentException> {
            fixture.gateway(writingExecutor("```json\n${NpcActorJson.strict.encodeToString(fixture.response())}\n```"))
                .generate(fixture.prompt())
        }
        val drifted = fixture.response().copy(requestId = "wrong-request")
        assertFailsWith<IllegalArgumentException> {
            fixture.gateway(writingExecutor(NpcActorJson.strict.encodeToString(drifted))).generate(fixture.prompt())
        }
    }

    @Test
    fun `timeout nonzero exit missing file and oversized output fail closed`() {
        val fixture = Fixture(maxOutputBytes = 1_024)
        val timeout = LocalNpcActorProcessExecutor { _, _, _, _ -> LocalNpcActorProcessResult(-1, true, 10) }
        assertFailsWith<IllegalArgumentException> { fixture.gateway(timeout).generate(fixture.prompt()) }

        val nonzero = LocalNpcActorProcessExecutor { _, _, _, _ -> LocalNpcActorProcessResult(7, false, 10) }
        assertFailsWith<IllegalArgumentException> { fixture.gateway(nonzero).generate(fixture.prompt()) }

        val missing = LocalNpcActorProcessExecutor { _, _, _, _ -> LocalNpcActorProcessResult(0, false, 10) }
        assertFailsWith<IllegalArgumentException> { fixture.gateway(missing).generate(fixture.prompt()) }

        val oversized = writingExecutor("x".repeat(1_025))
        assertFailsWith<IllegalArgumentException> { fixture.gateway(oversized).generate(fixture.prompt()) }
    }

    @Test
    fun `repair request with engine context marker is rejected before execution`() {
        val fixture = Fixture()
        val repair = com.localnovel.engine.model.NpcActorSafetyRepairRequest(
            loopId = "loop",
            requestId = REQUEST_ID,
            failedAttemptNumber = 1,
            nextAttemptNumber = 2,
            maxAttempts = 2,
            rejectionStage = "PHYSICAL_AFFORDANCE",
            rejectionCodes = listOf("physical:target_unreachable"),
            actorId = ACTOR_ID,
            targetIds = listOf(TARGET_ID),
            locationId = "location-market",
            allowedEvidenceIds = emptyList(),
            allowedPhysicalActionTypes = listOf(PhysicalActionType.REACH),
            visiblePhysicalTargetIds = listOf(TARGET_ID),
            repairInstructions = listOf("Choose a reachable target."),
            engineOwnedContextIncluded = true,
        )
        assertFailsWith<IllegalArgumentException> {
            fixture.gateway(writingExecutor("{}"))
                .generate(fixture.prompt(), repair)
        }
    }

    private fun writingExecutor(content: String): LocalNpcActorProcessExecutor =
        LocalNpcActorProcessExecutor { _, outputPath, _, _ ->
            Files.writeString(outputPath, content)
            LocalNpcActorProcessResult(0, false, 25)
        }

    private class Fixture(private val maxOutputBytes: Long = 262_144) {
        private val root = Files.createTempDirectory("npc-actor06-test")
        private val cli = Files.writeString(root.resolve("llama-cli.exe"), "fixture")
        private val model = Files.writeString(root.resolve("model.gguf"), "fixture-model")
        private val schema = Files.writeString(root.resolve("response.schema.json"), "{}")

        fun gateway(executor: LocalNpcActorProcessExecutor): LlamaCppNpcActorPhysicalGateway =
            LlamaCppNpcActorPhysicalGateway(
                config = LocalNpcActorRuntimeConfig(
                    llamaCliPath = cli,
                    modelPath = model,
                    responseSchemaPath = schema,
                    modelSha256 = sha256(Files.readAllBytes(model)),
                    maxOutputBytes = maxOutputBytes,
                ),
                processExecutor = executor,
            )

        fun prompt(): NpcActorPhysicalPrompt = NpcActorPhysicalPrompt(
            actorRequest = NpcActorRequest(
                requestId = REQUEST_ID,
                scenarioId = "scenario-local-model",
                actorId = ACTOR_ID,
                targetIds = listOf(TARGET_ID),
                locationId = "location-market",
                voiceProfile = NpcVoiceProfile(
                    principles = listOf("先看清楚再開口"),
                    forbiddenPatterns = listOf("身為一個AI"),
                ),
                physicalState = "健康",
                emotionalState = "警覺",
            ),
            allowedPhysicalActionTypes = listOf(PhysicalActionType.REACH),
            visiblePhysicalTargetIds = listOf(TARGET_ID),
        )

        fun response(): NpcActorPhysicalResponse {
            val actorResponse = NpcActorResponse(
                requestId = REQUEST_ID,
                actorId = ACTOR_ID,
                targetIds = listOf(TARGET_ID),
                speechAct = "WARN",
                immediateGoal = "阻止對方靠近危險物",
                emotionShown = "緊張",
                emotionHidden = "擔心來不及",
                actionType = "seek_local_evidence",
                actionIntent = "示意對方停下",
                visibleAction = "抬手攔住對方",
                utterance = "先別碰。",
                nonverbalAction = "視線盯著物件邊緣",
                predictedCost = "可能讓對方不悅",
                observableConsequence = "對方暫停動作",
                confidence = 70,
                decisionSummary = NpcActorDecisionSummary(
                    selectedActionLabel = "先阻止接觸",
                ),
            )
            return NpcActorPhysicalResponse(
                requestId = REQUEST_ID,
                actorResponse = actorResponse,
                physicalActionProposal = PhysicalActionProposal(
                    actionId = "action-local-1",
                    actionType = PhysicalActionType.REACH,
                    actorId = ACTOR_ID,
                    targetId = TARGET_ID,
                ),
            )
        }

        private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val REQUEST_ID = "request-local-model"
        const val ACTOR_ID = "npc-lin-wan"
        const val TARGET_ID = "player-traveler"
    }
}
