package com.localnovel.engine

import com.localnovel.engine.model.AudienceExpectationLedger
import com.localnovel.engine.model.DramaEvaluationInput
import com.localnovel.engine.model.DramaticQuestion
import com.localnovel.engine.model.NarrativeSignals
import com.localnovel.engine.model.SceneDramaFrame
import com.localnovel.engine.model.SceneValueState
import com.localnovel.engine.model.ValueConflict
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.NarrativeRevisionCoordinator
import com.localnovel.engine.service.NarrativeStallDetector
import com.localnovel.engine.service.RebootDramaLoopService
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RebootDramaLoopTest {
    @Test
    fun migrationPersistsOneBoundedRevision() {
        val repository = repository()
        val report = RebootDramaLoopService(repository).evaluateAndPersist(sampleInput())

        assertEquals(1, report.revisionCount)
        assertEquals("REVISED_ONCE", report.selectedVersion)
        assertTrue(report.revisedScores!!.overall > report.rawScores.overall)
        val row = repository.tableRows("reboot_drama_evaluations").single()
        assertEquals("1", row["revision_count"])
        assertTrue(row["raw_scores_json"].orEmpty().contains("stupidityDrivenPlotRisk"))
        assertTrue(row["expectation_ledger_json"].orEmpty().contains("secondOrderSurprise"))
    }

    @Test
    fun priorRevisionCannotTriggerSecondRevision() {
        val input = sampleInput().copy(priorRevisionCount = 1)
        val report = NarrativeRevisionCoordinator().evaluate(input)
        assertEquals(0, report.revisionCount)
        assertEquals("REVISION_LIMIT_REACHED", report.selectedVersion)
        assertEquals(input.rawDraft, report.selectedDraft)
    }

    @Test
    fun twoMotionlessFramesTriggerOnlyBoundedNextMoves() {
        val flat = frame().copy(
            sceneValue = SceneValueState("control", 50, 50),
            cost = "",
            relationshipChange = "",
            reveal = "",
        )
        val detector = NarrativeStallDetector()
        assertTrue(detector.isStalled(listOf(flat), flat))
        assertEquals(listOf("ESCALATE", "CHOOSE", "REVEAL", "PAY_COST", "END_SCENE"), detector.allowedNextMoves(true))
    }

    @Test
    fun logicCriticFlagsIdiotPlotCheapTwistAndMetadataLeak() {
        val input = sampleInput().copy(
            rawSignals = sampleInput().rawSignals.copy(directorMetadataLeak = true),
            revisedDraftCandidate = "",
            revisedSignals = null,
        )
        val report = NarrativeRevisionCoordinator().evaluate(input)
        val codes = report.findings.map { it.code }.toSet()
        assertTrue("IDIOT_PLOT_RISK" in codes)
        assertTrue("CHEAP_TWIST_RISK" in codes)
        assertTrue("DIRECTOR_METADATA_LEAK" in codes)
    }

    @Test
    fun cliEvaluatesSampleAndWritesReport() {
        val repository = repository()
        val out = Files.createTempFile("reboot-drama", ".json")
        val exit = Cli().run(
            listOf(
                "evaluate-reboot-drama",
                "--db",
                repository.databasePath().toString(),
                "--input",
                samplePath("reboot01/drama_evaluation_input.json").toString(),
                "--out",
                out.toString(),
            ),
        )
        assertEquals(0, exit)
        assertTrue(Files.readString(out).contains("REVISED_ONCE"))
        assertEquals(1, repository.tableCount("reboot_drama_evaluations"))
    }

    private fun sampleInput(): DramaEvaluationInput =
        JsonSupport.json.decodeFromString(Files.readString(samplePath("reboot01/drama_evaluation_input.json")))

    private fun frame(): SceneDramaFrame = SceneDramaFrame(
        sceneGoal = "Make a meaningful choice.",
        dramaticQuestion = DramaticQuestion("Will caution preserve trust?"),
        valueConflict = ValueConflict("safety", "truth", true),
        sceneValue = SceneValueState("control", 60, 35),
        stakes = "evidence",
        emotionalBeat = "pressure",
    )

    private fun repository(): SqliteStoryRepository =
        SqliteStoryRepository(Files.createTempDirectory("reboot-drama-test").resolve("story.db")).also { it.initialize() }

    private fun samplePath(relative: String): Path {
        val candidates = listOf(Path.of("samples", relative), Path.of("..", "samples", relative))
        return candidates.firstOrNull { Files.exists(it) } ?: error("Cannot find sample: $relative")
    }
}
