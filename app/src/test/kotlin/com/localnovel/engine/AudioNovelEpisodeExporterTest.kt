package com.localnovel.engine

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.DemoProjectSeed
import com.localnovel.engine.model.StateDelta
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.AudioNovelEpisodeExporter
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class AudioNovelEpisodeExporterTest {
    @Test
    fun exportBuildsScriptAndVoiceTimelineFromAppliedTurn() {
        val repository = seededRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("demo", "scene-shrine-questioning")
        val context = runtime.prepareTurn(session.id, "我質問沈霜。")
        runtime.resolveTurn(session.id, oralDemoResult(context))

        val export = AudioNovelEpisodeExporter(repository).export(session.id)

        assertEquals(1, export.turnCount)
        assertTrue(export.scriptMarkdown.contains("第一回　塵"))
        assertTrue(export.scriptMarkdown.contains("門閂昨夜被人動過"))
        assertTrue(export.voiceTimeline.audioCues.size >= 3)
        assertEquals("narrator", export.voiceTimeline.audioCues.first().characterId)
        assertEquals("廟裡很靜。", export.voiceTimeline.audioCues[0].text)
        assertEquals("沈霜低聲說：「門閂昨夜被人動過。」", export.voiceTimeline.audioCues[1].text)
        assertEquals("她沒有再多說。", export.voiceTimeline.audioCues.last().text)
        assertEquals("audio_novel_episode_draft", export.voiceTimeline.fillStatus)
        assertTrue(export.voiceTimeline.timing.totalDurationMillis > 0)
    }

    private fun oralDemoResult(context: ContextPacket): AiTurnResult =
        AiTurnResult(
            protocolVersion = "2",
            projectId = context.project.id,
            turnId = context.turnId,
            resultId = "audio-novel-export-demo-turn-1",
            contextId = context.contextId,
            sessionId = context.sessionId,
            turnNumber = context.turnNumber,
            baseStateVersion = context.baseStateVersion,
            contextHash = context.contextHash,
            narrative = "廟裡很靜。沈霜低聲說：「門閂昨夜被人動過。」她沒有再多說。",
            turnSummary = "沈霜只承認門閂異常。",
            characterActions = listOf(
                CharacterAction(
                    characterId = "char-shen-shuang",
                    intent = "守住後殿並控制回答邊界",
                    visibleAction = "沈霜擋住門，只承認門閂異常。",
                    hiddenMotive = "避免外人知道祠堂地底的禁忌。",
                    characterFitScore = 88,
                    storyProgressScore = 76,
                ),
            ),
            stateDelta = StateDelta(
                operations = listOf(
                    DeltaOperation(
                        type = DeltaOperationType.ADD_EVENT,
                        targetType = "event",
                        targetId = "event-audio-novel-export-demo",
                        payload = JsonObject(mapOf("summary" to JsonPrimitive("測試事件。"))),
                    ),
                ),
            ),
        )

    private fun seededRepository(): SqliteStoryRepository {
        val repository = SqliteStoryRepository(Files.createTempDirectory("story-engine-audio-novel-export").resolve("story.db"))
        repository.initialize()
        repository.seedDemo(readJson<DemoProjectSeed>(samplePath("demo_project_seed.json")))
        return repository
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private fun samplePath(fileName: String): Path {
        val candidates = listOf(
            Path.of("samples", fileName),
            Path.of("..", "samples", fileName),
        )
        return candidates.firstOrNull { Files.exists(it) } ?: error("Cannot find sample file: $fileName")
    }
}