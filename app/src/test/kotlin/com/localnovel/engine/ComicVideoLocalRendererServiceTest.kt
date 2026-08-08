package com.localnovel.engine

import com.localnovel.engine.model.ComicVideoLocalRenderReport
import com.localnovel.engine.model.ComicVideoRenderHandoffManifest
import com.localnovel.engine.model.ComicVideoRenderPanelTimelineItem
import com.localnovel.engine.model.ComicVideoRenderSettings
import com.localnovel.engine.model.ComicVideoRenderSubtitleTimelineItem
import com.localnovel.engine.model.ComicVideoRenderVoiceTimelineItem
import com.localnovel.engine.service.ComicVideoLocalRendererService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComicVideoLocalRendererServiceTest {
    @Test
    fun dryRunPassManifestPlansLocalMp4WithoutGeneratingMedia() {
        val tempDir = Files.createTempDirectory("comic-video-local-render-dry-run")
        val outputVideo = tempDir.resolve("demo.mp4")
        val report = ComicVideoLocalRendererService().render(
            manifest = manifest(),
            manifestFile = "render_handoff_manifest.json",
            outputVideoPath = outputVideo,
            workDir = tempDir.resolve("work"),
            voiceName = "Microsoft Hanhan Desktop",
            dryRun = true,
            renderId = "local-render-001",
        )

        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("DRY_RUN", report.renderStatus)
        assertEquals("local-render-001", report.renderId)
        assertEquals(false, report.mediaGenerated)
        assertEquals("run_render_comic_video_local_without_dry_run", report.nextAllowedDownstreamStep)
        assertEquals(2, report.evidence.panelCount)
        assertEquals(2, report.evidence.voiceCueCount)
        assertEquals(2, report.evidence.subtitleCueCount)
        assertFalse(Files.exists(outputVideo))
    }

    @Test
    fun failManifestIsBlockedBeforeLocalMediaGeneration() {
        val tempDir = Files.createTempDirectory("comic-video-local-render-fail")
        val report = ComicVideoLocalRendererService().render(
            manifest = manifest(verdict = "FAIL", nextAllowedDownstreamStep = ""),
            manifestFile = "render_handoff_manifest.json",
            outputVideoPath = tempDir.resolve("demo.mp4"),
            workDir = tempDir.resolve("work"),
            dryRun = true,
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("FAIL", report.renderStatus)
        assertEquals(false, report.mediaGenerated)
        assertTrue(report.findings.any { it.ruleId == "MANIFEST_VERDICT_NOT_PASS" })
        assertTrue(report.findings.any { it.ruleId == "MANIFEST_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun cliDryRunWritesLocalRenderReport() {
        val tempDir = Files.createTempDirectory("comic-video-local-render-cli")
        val manifestPath = tempDir.resolve("render_handoff_manifest.json")
        val reportPath = tempDir.resolve("local_render_report.json")
        val outputVideo = tempDir.resolve("demo.mp4")
        writeJson(manifestPath, manifest())

        val exit = Cli().run(
            listOf(
                "render-comic-video-local",
                "--render-handoff-manifest",
                manifestPath.toString(),
                "--out-video",
                outputVideo.toString(),
                "--work-dir",
                tempDir.resolve("work").toString(),
                "--render",
                "local-render-cli-001",
                "--report-out",
                reportPath.toString(),
                "--dry-run",
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(reportPath))
        assertFalse(Files.exists(outputVideo))
        val report = readJson<ComicVideoLocalRenderReport>(reportPath)
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("DRY_RUN", report.renderStatus)
        assertEquals("local-render-cli-001", report.renderId)
        assertEquals("comic-video-render-handoff-manifest-001", report.manifestId)
    }

    private fun manifest(
        verdict: String = "PASS",
        nextAllowedDownstreamStep: String = "external_manual_renderer_handoff",
    ): ComicVideoRenderHandoffManifest =
        ComicVideoRenderHandoffManifest(
            verdict = verdict,
            manifestId = "comic-video-render-handoff-manifest-001",
            manifestStatus = if (verdict == "PASS") "comic_video_render_handoff_manifest_candidate" else "FAIL",
            comicVideoPacketValidationReportFile = "comic_video_packet_validation.json",
            validationId = "comic-video-validation-001",
            packetId = "comic-video-packet-001",
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            panelTimeline = if (verdict == "PASS") listOf(
                panel("panel-001", "雨門", "遞信", 0, 2200),
                panel("panel-002", "側廊", "避雨", 2200, 4400),
            ) else emptyList(),
            voiceTimeline = if (verdict == "PASS") listOf(
                voice("audio-001", "panel-001", "林", "我把信帶到了。", 0, 1800),
                voice("audio-002", "panel-002", "守衛", "走側廊，別拆封。", 2200, 4200),
            ) else emptyList(),
            subtitleTimeline = if (verdict == "PASS") listOf(
                subtitle("subtitle-001", "panel-001", "audio-001", "林：我把信帶到了。", 0, 1800),
                subtitle("subtitle-002", "panel-002", "audio-002", "守衛：走側廊，別拆封。", 2200, 4200),
            ) else emptyList(),
            renderSettings = if (verdict == "PASS") {
                ComicVideoRenderSettings(
                    totalDurationMillis = 4400,
                    frameRate = "24",
                    resolution = "1920x1080",
                )
            } else {
                ComicVideoRenderSettings()
            },
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun panel(
        panelId: String,
        scene: String,
        beat: String,
        startMillis: Int,
        endMillis: Int,
    ): ComicVideoRenderPanelTimelineItem =
        ComicVideoRenderPanelTimelineItem(
            panelId = panelId,
            sourceFrameId = panelId.replace("panel", "frame"),
            sceneReference = scene,
            beatReference = beat,
            visualIntent = "雨夜中人物靠近門樓，手中有一封未拆的信。",
            cameraIntent = "中景，保留信封與屋簷。",
            startMillis = startMillis,
            endMillis = endMillis,
            displayDurationMillis = endMillis - startMillis,
            mustShow = listOf("信封未拆", "雨水", "門樓"),
            doNotShow = listOf("信件內容"),
            continuityAnchors = listOf("信仍保持封好"),
        )

    private fun voice(
        cueId: String,
        panelId: String,
        displayName: String,
        text: String,
        startMillis: Int,
        endMillis: Int,
    ): ComicVideoRenderVoiceTimelineItem =
        ComicVideoRenderVoiceTimelineItem(
            cueId = cueId,
            panelId = panelId,
            voiceId = "voice-$displayName",
            characterId = "char-$displayName",
            displayName = displayName,
            voiceLabel = "local-preview",
            text = text,
            startMillis = startMillis,
            endMillis = endMillis,
            durationMillis = endMillis - startMillis,
        )

    private fun subtitle(
        cueId: String,
        panelId: String,
        audioCueId: String,
        text: String,
        startMillis: Int,
        endMillis: Int,
    ): ComicVideoRenderSubtitleTimelineItem =
        ComicVideoRenderSubtitleTimelineItem(
            cueId = cueId,
            panelId = panelId,
            audioCueId = audioCueId,
            text = text,
            startMillis = startMillis,
            endMillis = endMillis,
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
