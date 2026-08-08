package com.localnovel.engine

import com.localnovel.engine.model.LocalNpcActorGenerationResult
import com.localnovel.engine.model.LocalNpcActorInvocationAudit
import com.localnovel.engine.model.LocalNpcActorSafetyLoopRequest
import com.localnovel.engine.model.NpcActorPhysicalPrompt
import com.localnovel.engine.model.NpcActorSafetyRepairRequest
import com.localnovel.engine.service.LocalNpcActorGenerator
import com.localnovel.engine.service.LocalNpcActorSafetyLoopService
import com.localnovel.engine.service.NpcActorSafetyLoopFixtureFactory
import com.localnovel.engine.service.NpcActorSafetyLoopService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.encodeToString

class LocalNpcActorSafetyLoopServiceTest {
    @Test
    fun `accepted first generation stays behind the safety loop`() {
        val fixture = NpcActorSafetyLoopFixtureFactory.createCases().first()
        val generator = QueueGenerator(listOf(fixture.input.responseAttempts.single()))
        val report = LocalNpcActorSafetyLoopService(generator).run(fixture.context, fixture.request())

        assertEquals(NpcActorSafetyLoopService.ACCEPTED_FOR_FORMAL_RESOLVE_CANDIDATE, report.safetyResult.status)
        assertEquals(1, report.generatedAttemptCount)
        assertFalse(report.repairAttempted)
        assertFalse(report.formalStateWritePerformed)
        assertEquals(1, generator.calls.size)
        assertEquals(null, generator.calls.single())
    }

    @Test
    fun `one sanitized repair is generated and revalidated`() {
        val fixture = NpcActorSafetyLoopFixtureFactory.createCases()
            .single { it.caseId == "npc-actor05-lian-knowledge-repair" }
        val generator = QueueGenerator(fixture.input.responseAttempts)
        val report = LocalNpcActorSafetyLoopService(generator).run(fixture.context, fixture.request())

        assertEquals(NpcActorSafetyLoopService.ACCEPTED_FOR_FORMAL_RESOLVE_CANDIDATE, report.safetyResult.status)
        assertEquals(2, report.generatedAttemptCount)
        assertTrue(report.repairAttempted)
        assertEquals(2, generator.calls.size)
        val repair = assertNotNull(generator.calls.last())
        assertFalse(repair.engineOwnedContextIncluded)
        assertEquals(NpcActorSafetyLoopService.ACTOR_CONTRACT, repair.rejectionStage)
    }

    @Test
    fun `CLI fails closed when local runtime paths are missing`() {
        val fixture = NpcActorSafetyLoopFixtureFactory.createCases().first()
        val temp = java.nio.file.Files.createTempDirectory("npc-actor06-cli")
        val contextPath = temp.resolve("context.json")
        val requestPath = temp.resolve("request.json")
        java.nio.file.Files.writeString(
            contextPath,
            com.localnovel.engine.service.NpcActorJson.strict.encodeToString(fixture.context),
        )
        java.nio.file.Files.writeString(
            requestPath,
            com.localnovel.engine.service.NpcActorJson.strict.encodeToString(fixture.request()),
        )

        val exit = Cli().run(
            listOf(
                "run-local-npc-actor-safety-loop",
                "--repo", temp.toString(),
                "--context", contextPath.toString(),
                "--request", requestPath.toString(),
                "--llama-cli", temp.resolve("missing-llama-cli.exe").toString(),
                "--model", temp.resolve("missing-model.gguf").toString(),
                "--schema", temp.resolve("missing-schema.json").toString(),
                "--out", temp.resolve("report.json").toString(),
            ),
        )

        assertEquals(1, exit)
        assertFalse(java.nio.file.Files.exists(temp.resolve("report.json")))
    }

    private fun com.localnovel.engine.service.NpcActorSafetyLoopFixtureCase.request() =
        LocalNpcActorSafetyLoopRequest(
            loopId = input.loopId,
            prompt = input.prompt,
            engineContext = input.engineContext,
            maxAttempts = input.maxAttempts,
            maxElapsedMinutes = input.maxElapsedMinutes,
        )

    private class QueueGenerator(
        responses: List<com.localnovel.engine.model.NpcActorPhysicalResponse>,
    ) : LocalNpcActorGenerator {
        private val queue = ArrayDeque(responses)
        val calls = mutableListOf<NpcActorSafetyRepairRequest?>()

        override fun generate(
            prompt: NpcActorPhysicalPrompt,
            repairRequest: NpcActorSafetyRepairRequest?,
        ): LocalNpcActorGenerationResult {
            calls += repairRequest
            val response = queue.removeFirst()
            return LocalNpcActorGenerationResult(
                response = response,
                audit = LocalNpcActorInvocationAudit(
                    modelId = "fixture-model",
                    modelRevision = "fixture-revision",
                    modelSha256 = "0".repeat(64),
                    modelFileName = "fixture.gguf",
                    runtimeId = "fixture-runtime",
                    contextTokens = 2048,
                    maxOutputTokens = 512,
                    seed = 1,
                    elapsedMillis = 10,
                    rawOutputBytes = 100,
                    promptSha256 = "1".repeat(64),
                    responseSha256 = "2".repeat(64),
                ),
                promptText = "fixture",
                rawOutput = "{}",
                responseJson = "{}",
            )
        }
    }
}
