package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.AudioNovelEpisodeExport
import com.localnovel.engine.model.AudioNovelEpisodeSection
import com.localnovel.engine.model.ComicVideoAudioCue
import com.localnovel.engine.model.ComicVideoTiming
import com.localnovel.engine.model.ComicVideoVoiceCast
import com.localnovel.engine.model.ComicVideoVoiceTimelineFill
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString

data class AudioNovelEpisodeExportOptions(
    val episodeId: String = "",
    val title: String = "第一回　塵",
    val fromTurn: Int = 1,
    val toTurn: Int = Int.MAX_VALUE,
    val coverImagePath: String = "",
    val millisPerCharacter: Int = 220,
    val gapMillis: Int = 350,
    val sentenceGapMillis: Int = 420,
    val dialogueGapMillis: Int = 720,
    val turnGapMillis: Int = 1000,
)

class AudioNovelEpisodeExporter(
    private val repository: SqliteStoryRepository,
) {
    fun export(sessionId: String, options: AudioNovelEpisodeExportOptions = AudioNovelEpisodeExportOptions()): AudioNovelEpisodeExport {
        val session = repository.getSession(sessionId) ?: error("Session not found: $sessionId")
        val turnLogs = repository.tableRows("turn_logs")
            .filter {
                it["session_id"] == sessionId &&
                    it["validation_status"] == "applied" &&
                    (it["turn_number"]?.toIntOrNull() ?: 0) in options.fromTurn..options.toTurn
            }
            .sortedBy { it["turn_number"]?.toIntOrNull() ?: Int.MAX_VALUE }
        require(turnLogs.isNotEmpty()) {
            "No applied turns found for session '$sessionId' in range ${options.fromTurn}-${options.toTurn}."
        }

        val sections = turnLogs.map { row -> sectionFromTurnLog(row) }
        val voiceCast = defaultVoiceCast(session.projectId)
        val audioCues = buildAudioCues(
            sections = sections,
            voiceCast = voiceCast,
            millisPerCharacter = options.millisPerCharacter,
            sentenceGapMillis = options.sentenceGapMillis,
            dialogueGapMillis = options.dialogueGapMillis,
            turnGapMillis = options.turnGapMillis,
        )
        val episodeId = options.episodeId.ifBlank { "audio-novel-${session.projectId}-${sessionId.takeLast(8)}" }
        val cover = options.coverImagePath.ifBlank { defaultCoverImagePath(session.projectId) }
        val scriptMarkdown = buildScriptMarkdown(options.title, sections)
        val totalDuration = audioCues.lastOrNull()?.let { it.startMillis + it.durationMillis } ?: 0

        return AudioNovelEpisodeExport(
            episodeId = episodeId,
            title = options.title,
            projectId = session.projectId,
            sessionId = sessionId,
            fromTurn = sections.first().turnNumber,
            toTurn = sections.last().turnNumber,
            turnCount = sections.size,
            scriptMarkdown = scriptMarkdown,
            sections = sections,
            voiceTimeline = ComicVideoVoiceTimelineFill(
                fillId = "$episodeId-voice-timeline",
                sourceComicVideoPacketId = episodeId,
                fillStatus = "audio_novel_episode_draft",
                voiceCast = voiceCast,
                audioCues = audioCues,
                timing = ComicVideoTiming(
                    timelineStatus = "audio_novel_metadata_only",
                    totalDurationMillis = totalDuration,
                    frameRate = "24",
                    resolution = "1920x1080",
                    backgroundPolicy = "single_cover_image",
                ),
                boundaryChecklist = listOf(
                    "no_provider_job_ids",
                    "no_remote_audio_urls",
                    "no_canon_or_registry_writes",
                    "local_tts_or_recording_outside_engine",
                ),
                notes = listOf(
                    "Exported from session turn logs for audio-novel / YouTube narration workflow.",
                    "sessionId=$sessionId",
                ),
            ),
            coverImagePath = cover,
            notes = listOf(
                "Use coverImagePath as static visual for ffmpeg image+audio MP4.",
                "Generate audio outside engine, then mux locally.",
            ),
        )
    }

    private fun sectionFromTurnLog(row: Map<String, String>): AudioNovelEpisodeSection {
        val result = JsonSupport.json.decodeFromString<AiTurnResult>(row["result_json"].orEmpty())
        val context = runCatching {
            JsonSupport.json.decodeFromString<ContextPacket>(row["context_json"].orEmpty())
        }.getOrNull()
        return AudioNovelEpisodeSection(
            turnNumber = row["turn_number"]?.toIntOrNull() ?: result.turnNumber,
            turnId = result.turnId,
            beatId = context?.narrativeDelivery?.activeBeatId.orEmpty(),
            beatTitle = context?.narrativeDelivery?.activeBeatTitle.orEmpty(),
            playerInput = row["player_input"].orEmpty(),
            narrative = result.narrative,
            turnSummary = result.turnSummary,
        )
    }

    private fun buildScriptMarkdown(title: String, sections: List<AudioNovelEpisodeSection>): String {
        val header = buildString {
            appendLine("# $title")
            appendLine()
            appendLine("> 有聲小說試聽稿 · ${sections.size} 回合")
            appendLine()
        }
        val body = sections.joinToString(separator = "\n") { section ->
            buildString {
                val beatLabel = when {
                    section.beatTitle.isNotBlank() && section.beatId.isNotBlank() ->
                        "${section.beatId}（${section.beatTitle}）"
                    section.beatId.isNotBlank() -> section.beatId
                    else -> "回合 ${section.turnNumber}"
                }
                appendLine("## 回合 ${section.turnNumber} · $beatLabel")
                appendLine()
                appendLine(section.narrative)
                appendLine()
                if (section.turnSummary.isNotBlank()) {
                    appendLine("*摘要：${section.turnSummary}*")
                    appendLine()
                }
                appendLine("---")
            }
        }
        return header + body
    }

    private fun buildAudioCues(
        sections: List<AudioNovelEpisodeSection>,
        voiceCast: List<ComicVideoVoiceCast>,
        millisPerCharacter: Int,
        sentenceGapMillis: Int,
        dialogueGapMillis: Int,
        turnGapMillis: Int,
    ): List<ComicVideoAudioCue> {
        val narrator = voiceCast.firstOrNull { it.characterId == "narrator" } ?: voiceCast.first()
        val playerVoice = voiceCast.firstOrNull { it.characterId == "char-lu-chenxing" }
        val characterNames = voiceCast
            .filter { it.characterId != "narrator" }
            .map { it.displayName }
            .filter { it.isNotBlank() }
        var cursor = 0
        val cues = mutableListOf<ComicVideoAudioCue>()
        var cueIndex = 0

        sections.forEachIndexed { sectionIndex, section ->
            val panelId = section.beatId.ifBlank { "turn-${section.turnNumber}" }
            val segments = NarrativeOralFormatter.splitIntoOralSegments(
                narrative = section.narrative,
                characterNames = characterNames,
            )
            segments.forEachIndexed { segmentIndex, segment ->
                val voice = when (segment.kind) {
                    OralAudioSegmentKind.NARRATION -> narrator
                    OralAudioSegmentKind.DIALOGUE -> playerVoice ?: narrator
                }
                val duration = estimateDurationMillis(segment.text, millisPerCharacter)
                cues += ComicVideoAudioCue(
                    cueId = "audio-${cueIndex.toString().padStart(3, '0')}",
                    panelId = panelId,
                    voiceId = voice.voiceId,
                    characterId = voice.characterId,
                    text = segment.text,
                    startMillis = cursor,
                    durationMillis = duration,
                )
                cursor += duration + gapAfterSegment(
                    current = segment,
                    next = segments.getOrNull(segmentIndex + 1),
                    isLastInTurn = segmentIndex == segments.lastIndex,
                    isLastTurn = sectionIndex == sections.lastIndex,
                    sentenceGapMillis = sentenceGapMillis,
                    dialogueGapMillis = dialogueGapMillis,
                    turnGapMillis = turnGapMillis,
                )
                cueIndex += 1
            }
        }
        return cues
    }

    private fun gapAfterSegment(
        current: OralAudioSegment,
        next: OralAudioSegment?,
        isLastInTurn: Boolean,
        isLastTurn: Boolean,
        sentenceGapMillis: Int,
        dialogueGapMillis: Int,
        turnGapMillis: Int,
    ): Int = when {
        next == null && isLastInTurn && !isLastTurn -> turnGapMillis
        next == null -> 0
        isLastInTurn -> turnGapMillis
        current.kind == OralAudioSegmentKind.DIALOGUE || next.kind == OralAudioSegmentKind.DIALOGUE -> dialogueGapMillis
        else -> sentenceGapMillis
    }

    private fun estimateDurationMillis(text: String, millisPerCharacter: Int): Int {
        val length = text.trim().length
        return (length * millisPerCharacter).coerceIn(1200, 60000)
    }

    private fun defaultVoiceCast(projectId: String): List<ComicVideoVoiceCast> =
        when (projectId) {
            "chenxing" -> listOf(
                ComicVideoVoiceCast(
                    voiceId = "voice-narrator",
                    characterId = "narrator",
                    displayName = "旁白",
                    voiceLabel = "local-narrator-oral-story",
                    notes = "場景敘事旁白。",
                ),
                ComicVideoVoiceCast(
                    voiceId = "voice-lu-chenxing",
                    characterId = "char-lu-chenxing",
                    displayName = "陸塵行",
                    voiceLabel = "local-lu-chenxing-calm-low",
                    notes = "低聲實務型語氣。",
                ),
            )
            else -> listOf(
                ComicVideoVoiceCast(
                    voiceId = "voice-narrator",
                    characterId = "narrator",
                    displayName = "旁白",
                    voiceLabel = "local-narrator-default",
                ),
            )
        }

    private fun defaultCoverImagePath(projectId: String): String =
        when (projectId) {
            "chenxing" -> "world/chenxing/assets/cover/chenxing_cover_v1.png"
            else -> ""
        }
}