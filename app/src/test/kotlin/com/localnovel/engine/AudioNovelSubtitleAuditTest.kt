package com.localnovel.engine

import com.localnovel.engine.model.ComicVideoAudioCue
import com.localnovel.engine.model.ComicVideoVoiceTimelineFill
import com.localnovel.engine.service.AudioNovelSubtitleAuditService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AudioNovelSubtitleAuditTest {
    private val service = AudioNovelSubtitleAuditService()

    @Test
    fun flagsBrokenEmDashAndLongDwellInSrt() {
        val srt = """
            1
            00:00:45,928 --> 00:00:47,104
            不對—

            2
            00:00:47,104 --> 00:00:47,804
            —

            3
            00:00:20,241 --> 00:00:23,520
            潮水還沒漲上來。
        """.trimIndent()
        val findings = service.auditSrt(java.nio.file.Files.writeString(
            java.nio.file.Files.createTempFile("subtitle-audit", ".srt"),
            srt,
        ))
        val ruleIds = findings.map { it.ruleId }.toSet()
        assertTrue("SUBTITLE_BROKEN_EM_DASH" in ruleIds)
        assertTrue("SUBTITLE_DWELL_TOO_LONG" in ruleIds)
    }

    @Test
    fun flagsDoubleTurnGapInSrt() {
        val srt = """
            1
            00:00:26,332 --> 00:00:28,656
            沒有再往岩縫那邊踏。

            2
            00:00:31,456 --> 00:00:33,556
            陸塵行剛要再往前，
        """.trimIndent()
        val path = java.nio.file.Files.createTempFile("subtitle-gap", ".srt")
        java.nio.file.Files.writeString(path, srt)
        val findings = service.auditSrt(path)
        assertTrue(findings.any { it.ruleId == "SUBTITLE_DOUBLE_TURN_GAP" })
    }

    @Test
    fun flagsSimplifiedChineseInVoiceTimelineCue() {
        val timeline = ComicVideoVoiceTimelineFill(
            audioCues = listOf(
                ComicVideoAudioCue(
                    cueId = "audio-016",
                    characterId = "narrator",
                    text = "愛醬在門前放慢腳步，耳朵竖起，沒有立刻跟上。",
                    durationMillis = 4840,
                ),
            ),
        )
        val findings = service.auditVoiceTimeline(timeline)
        assertTrue(findings.any { it.ruleId == "SIMPLIFIED_CHINESE_IN_PUBLISH_TEXT" })
    }

    @Test
    fun publishTextStripsDialogueAttribution() {
        val cue = ComicVideoAudioCue(
            characterId = "char-lu-chenxing",
            text = "陸塵行低聲說：「還來得及，潮水還沒漲上來。」",
        )
        assertEquals("還來得及，潮水還沒漲上來。", service.publishTextForCue(cue))
    }
}