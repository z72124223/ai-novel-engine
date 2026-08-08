package com.localnovel.engine

import com.localnovel.engine.model.NpcActorBlindReviewAnswerKey
import com.localnovel.engine.model.NpcActorBlindReviewPacket
import com.localnovel.engine.model.NpcActorEvaluationReport
import com.localnovel.engine.model.NpcActorEvaluationSuite
import com.localnovel.engine.service.NpcActorJson
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class NpcActorCliTest {
    @Test
    fun cliCreatesAndEvaluatesFixedTwentyScenarioSuite() {
        val temp = Files.createTempDirectory("npc-actor-cli")
        val suite = temp.resolve("suite.json")
        val report = temp.resolve("report.json")
        val blind = temp.resolve("blind.json")
        val answer = temp.resolve("answer.json")

        assertEquals(0, Cli().run(listOf("create-npc-actor-evaluation-suite", "--out", suite.toString())))
        assertEquals(
            0,
            Cli().run(
                listOf(
                    "evaluate-npc-actor-pilot",
                    "--suite", suite.toString(),
                    "--report", report.toString(),
                    "--blind", blind.toString(),
                    "--answer-key", answer.toString(),
                ),
            ),
        )

        val parsedSuite = read<NpcActorEvaluationSuite>(suite)
        val parsedReport = read<NpcActorEvaluationReport>(report)
        val parsedBlind = read<NpcActorBlindReviewPacket>(blind)
        val parsedAnswer = read<NpcActorBlindReviewAnswerKey>(answer)
        assertEquals(20, parsedSuite.scenarios.size)
        assertTrue(parsedSuite.scenarios.all { it.boundary == "NON_CANON_EVALUATION" })
        assertEquals("TECHNICAL_PASS_HUMAN_REVIEW_PENDING", parsedReport.technicalDecision)
        assertEquals(20, parsedBlind.items.size)
        assertEquals(parsedBlind.packetId, parsedAnswer.packetId)
    }

    private inline fun <reified T> read(path: java.nio.file.Path): T =
        NpcActorJson.strict.decodeFromString(Files.readString(path))
}
