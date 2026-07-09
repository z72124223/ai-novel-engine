package com.localnovel.engine.service

import com.localnovel.engine.model.ComicVideoAudioCue
import com.localnovel.engine.model.ComicVideoVoiceTimelineFill
import com.localnovel.engine.model.NarrativeQualityFinding
import java.nio.file.Files
import java.nio.file.Path
object AudioNovelSubtitlePolicy {
    const val MAX_CHARS_PER_CUE = 12
    const val MAX_DWELL_MS = 2100
    const val LONG_DWELL_SHORT_TEXT_CHARS = 10
    const val MAX_TURN_GAP_MS = 1250
    const val MAX_DIALOGUE_GAP_MS = 800
    const val TURN_GAP_MS = 1000
}

data class ParsedSubtitleCue(
    val index: Int,
    val startMs: Int,
    val endMs: Int,
    val text: String,
) {
    val dwellMs: Int get() = endMs - startMs
}

class AudioNovelSubtitleAuditService {
    fun auditSrt(srtPath: Path, sourceLabel: String = srtPath.toString()): List<NarrativeQualityFinding> {
        if (!Files.exists(srtPath)) return emptyList()
        val cues = parseSrt(Files.readString(srtPath))
        return auditParsedCues(cues, sourceLabel)
    }

    fun auditVoiceTimeline(
        timeline: ComicVideoVoiceTimelineFill,
        sourceLabel: String = "voice_timeline.json",
    ): List<NarrativeQualityFinding> = buildList {
        timeline.audioCues.forEach { cue ->
            addAll(checkCuePublishText(cue, sourceLabel))
        }
    }

    private fun auditParsedCues(
        cues: List<ParsedSubtitleCue>,
        sourceLabel: String,
    ): List<NarrativeQualityFinding> = buildList {
        cues.forEach { cue ->
            addAll(checkCueDwell(cue, sourceLabel))
            addAll(checkBrokenEmDash(cue, sourceLabel))
            addAll(checkOrphanPunctuation(cue, sourceLabel))
            addAll(checkSimplifiedCharacters(cue.text, "subtitle:${cue.index}", sourceLabel))
        }
        for (index in 1 until cues.size) {
            addAll(checkGapBetween(cues[index - 1], cues[index], sourceLabel))
        }
    }

    private fun checkCueDwell(cue: ParsedSubtitleCue, sourceLabel: String): List<NarrativeQualityFinding> {
        val textLength = cue.text.replace("\\s+".toRegex(), "").length
        val tooLongForShortText =
            cue.dwellMs > AudioNovelSubtitlePolicy.MAX_DWELL_MS &&
                textLength <= AudioNovelSubtitlePolicy.LONG_DWELL_SHORT_TEXT_CHARS
        val tooLongOverall = cue.dwellMs > AudioNovelSubtitlePolicy.MAX_DWELL_MS + 400
        if (!tooLongForShortText && !tooLongOverall) return emptyList()
        return listOf(
            subtitleFinding(
                ruleId = "SUBTITLE_DWELL_TOO_LONG",
                severity = if (tooLongForShortText) "MAJOR" else "WARNING",
                message = "Burned-in subtitle stays on screen too long for its text length.",
                evidence = listOf(
                    "cue=${cue.index}",
                    "dwellMs=${cue.dwellMs}",
                    "chars=$textLength",
                    "text=${cue.text}",
                ),
                suggestedFix = "Split the cue into shorter chunks and cap each block at ${AudioNovelSubtitlePolicy.MAX_DWELL_MS}ms.",
                affected = "$sourceLabel#${cue.index}",
            ),
        )
    }

    private fun checkGapBetween(
        previous: ParsedSubtitleCue,
        next: ParsedSubtitleCue,
        sourceLabel: String,
    ): List<NarrativeQualityFinding> {
        val gapMs = next.startMs - previous.endMs
        if (gapMs < AudioNovelSubtitlePolicy.MAX_TURN_GAP_MS) return emptyList()
        val severity = if (gapMs >= AudioNovelSubtitlePolicy.TURN_GAP_MS * 2 - 200) "MAJOR" else "WARNING"
        return listOf(
            subtitleFinding(
                ruleId = if (gapMs >= 1500) "SUBTITLE_DOUBLE_TURN_GAP" else "SUBTITLE_GAP_TOO_LONG",
                severity = severity,
                message = "No subtitle is shown during a long silence between oral segments.",
                evidence = listOf(
                    "gapMs=$gapMs",
                    "after=${previous.index}:${previous.text}",
                    "before=${next.index}:${next.text}",
                ),
                suggestedFix = "Keep turn transitions near ${AudioNovelSubtitlePolicy.TURN_GAP_MS}ms and avoid applying turn gaps twice.",
                affected = "$sourceLabel#${previous.index}->${next.index}",
            ),
        )
    }

    private fun checkBrokenEmDash(cue: ParsedSubtitleCue, sourceLabel: String): List<NarrativeQualityFinding> {
        val text = cue.text.trim()
        if (text == "—" || text == "-" || text.endsWith("—") && !text.endsWith("——")) {
            return listOf(
                subtitleFinding(
                    ruleId = "SUBTITLE_BROKEN_EM_DASH",
                    severity = "MAJOR",
                    message = "Subtitle splits an em dash across cues or leaves a dangling dash line.",
                    evidence = listOf("cue=${cue.index}", "text=$text"),
                    suggestedFix = "Split only after a full 「——」 marker, never on a single dash.",
                    affected = "$sourceLabel#${cue.index}",
                ),
            )
        }
        return emptyList()
    }

    private fun checkOrphanPunctuation(cue: ParsedSubtitleCue, sourceLabel: String): List<NarrativeQualityFinding> {
        val text = cue.text.trim()
        if (text.matches("^[。！？；，、…]+$".toRegex())) {
            return listOf(
                subtitleFinding(
                    ruleId = "SUBTITLE_ORPHAN_PUNCTUATION",
                    severity = "MAJOR",
                    message = "Subtitle cue contains punctuation only.",
                    evidence = listOf("cue=${cue.index}", "text=$text"),
                    suggestedFix = "Merge orphan punctuation into the previous subtitle chunk.",
                    affected = "$sourceLabel#${cue.index}",
                ),
            )
        }
        return emptyList()
    }

    private fun checkSimplifiedCharacters(
        text: String,
        label: String,
        sourceLabel: String,
    ): List<NarrativeQualityFinding> {
        val hits = SIMPLIFIED_CHAR_FIXES.filter { (simplified, _) -> text.contains(simplified) }
        if (hits.isEmpty()) return emptyList()
        return listOf(
            subtitleFinding(
                ruleId = "SIMPLIFIED_CHINESE_IN_PUBLISH_TEXT",
                severity = "WARNING",
                message = "Publish-facing text contains simplified Chinese characters.",
                evidence = hits.map { (from, to) -> "$from->$to" } + listOf("text=$text"),
                suggestedFix = "Rewrite using Traditional characters before export/render.",
                affected = "$sourceLabel:$label",
            ),
        )
    }

    private fun checkCuePublishText(
        cue: ComicVideoAudioCue,
        sourceLabel: String,
    ): List<NarrativeQualityFinding> = buildList {
        val publishText = publishTextForCue(cue)
        addAll(checkSimplifiedCharacters(publishText, cue.cueId, sourceLabel))
        val chunks = resolvePublishChunks(publishText)
        if (chunks.size == 1 && cue.durationMillis > AudioNovelSubtitlePolicy.MAX_DWELL_MS) {
            add(
                subtitleFinding(
                    ruleId = "SUBTITLE_SINGLE_CHUNK_LONG_DWELL_RISK",
                    severity = "WARNING",
                    message = "Audio cue publish text is likely to remain one long subtitle for the full TTS duration.",
                    evidence = listOf(
                        "cueId=${cue.cueId}",
                        "durationMs=${cue.durationMillis}",
                        "text=$publishText",
                    ),
                    suggestedFix = "Add punctuation breakpoints or shorter clauses so publish subtitles can split under ${AudioNovelSubtitlePolicy.MAX_DWELL_MS}ms.",
                    affected = "$sourceLabel:${cue.cueId}",
                ),
            )
        }
    }

    fun publishTextForCue(cue: ComicVideoAudioCue): String {
        val text = cue.text.trim()
        if (cue.characterId != "narrator") {
            val quote = Regex("「([^」]+)」").find(text)?.groupValues?.getOrNull(1)?.trim().orEmpty()
            if (quote.isNotBlank()) return quote
        }
        return text.replace(Regex("shot-\\d+[-\\w]*"), "").replace("\\s+".toRegex(), "").trim()
    }

    fun resolvePublishChunks(text: String, maxChars: Int = AudioNovelSubtitlePolicy.MAX_CHARS_PER_CUE): List<String> {
        val normalized = text.replace("\\s+".toRegex(), "").trim()
        if (normalized.isBlank()) return emptyList()
        if (normalized.length <= maxChars) return listOf(normalized)
        val segments = normalized.split(Regex("(?<=[。！？；])|(?<=[，、])|(?<=——)|(?<=……)"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (segments.size <= 1) return listOf(normalized)
        return segments
    }

    private fun parseSrt(raw: String): List<ParsedSubtitleCue> {
        val blocks = raw.trim().split(Regex("\\R\\R+"))
        return blocks.mapNotNull { block ->
            val lines = block.lines().filter { it.isNotBlank() }
            if (lines.size < 3) return@mapNotNull null
            val index = lines.first().toIntOrNull() ?: return@mapNotNull null
            val timing = lines[1]
            val parts = timing.split("-->")
            if (parts.size != 2) return@mapNotNull null
            ParsedSubtitleCue(
                index = index,
                startMs = parseSrtTime(parts[0]),
                endMs = parseSrtTime(parts[1]),
                text = lines.drop(2).joinToString(""),
            )
        }
    }

    private fun parseSrtTime(value: String): Int {
        val match = Regex("(\\d+):(\\d+):(\\d+),(\\d+)").find(value.trim()) ?: return 0
        val (hours, minutes, seconds, millis) = match.destructured
        return hours.toInt() * 3_600_000 +
            minutes.toInt() * 60_000 +
            seconds.toInt() * 1_000 +
            millis.toInt()
    }

    private fun subtitleFinding(
        ruleId: String,
        severity: String,
        message: String,
        evidence: List<String>,
        suggestedFix: String,
        affected: String,
    ): NarrativeQualityFinding =
        NarrativeQualityFinding(
            ruleId = ruleId,
            category = "audio_novel_subtitle",
            severity = severity,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
            affectedFileOrTurn = affected,
        )

    companion object {
        private val SIMPLIFIED_CHAR_FIXES = listOf(
            "竖" to "豎",
            "紧" to "緊",
            "发" to "發",
            "听" to "聽",
            "复" to "複",
            "制" to "製",
        )
    }
}