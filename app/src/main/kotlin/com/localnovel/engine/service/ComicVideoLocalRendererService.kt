package com.localnovel.engine.service

import com.localnovel.engine.model.ComicVideoLocalRenderArtifact
import com.localnovel.engine.model.ComicVideoLocalRenderEvidence
import com.localnovel.engine.model.ComicVideoLocalRenderFinding
import com.localnovel.engine.model.ComicVideoLocalRenderReport
import com.localnovel.engine.model.ComicVideoRenderHandoffManifest
import com.localnovel.engine.model.ComicVideoRenderPanelTimelineItem
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.roundToInt

class ComicVideoLocalRendererService(
    private val commandRunner: CommandRunner = DefaultCommandRunner(),
) {
    interface CommandRunner {
        fun run(command: List<String>, workingDir: Path): CommandResult
    }

    data class CommandResult(
        val exitCode: Int,
        val output: String,
    )

    fun render(
        manifest: ComicVideoRenderHandoffManifest,
        manifestFile: String,
        outputVideoPath: Path,
        workDir: Path,
        voiceName: String = "Microsoft Hanhan Desktop",
        dryRun: Boolean = false,
        renderId: String = "",
    ): ComicVideoLocalRenderReport {
        val findings = mutableListOf<ComicVideoLocalRenderFinding>()
        val resolvedRenderId = renderId.ifBlank {
            listOf(manifest.manifestId, manifest.packetId, "comic-video")
                .firstOrNull { it.isNotBlank() }
                .orEmpty()
                .plus("-local-render")
        }
        val output = outputVideoPath.toAbsolutePath().normalize()
        val work = workDir.toAbsolutePath().normalize()
        val resolution = parseResolution(manifest.renderSettings.resolution, findings)
        val frameRate = parseFrameRate(manifest.renderSettings.frameRate, findings)
        validateManifest(manifest, manifestFile, output, work, findings)

        if (findings.hasBlocking()) {
            return report(
                verdict = "FAIL",
                renderStatus = "FAIL",
                renderId = resolvedRenderId,
                manifest = manifest,
                manifestFile = manifestFile,
                output = output,
                work = work,
                voiceName = voiceName,
                dryRun = dryRun,
                mediaGenerated = false,
                nextAllowedDownstreamStep = "",
                findings = findings,
                artifacts = plannedArtifacts(output),
            )
        }

        if (dryRun) {
            return report(
                verdict = "PASS",
                renderStatus = "DRY_RUN",
                renderId = resolvedRenderId,
                manifest = manifest,
                manifestFile = manifestFile,
                output = output,
                work = work,
                voiceName = voiceName,
                dryRun = true,
                mediaGenerated = false,
                nextAllowedDownstreamStep = "run_render_comic_video_local_without_dry_run",
                findings = findings,
                artifacts = plannedArtifacts(output),
            )
        }

        val artifacts = mutableListOf<ComicVideoLocalRenderArtifact>()
        var ffmpegCommandCount = 0
        var speechCommandCount = 0
        try {
            Files.createDirectories(work)
            output.parent?.let { Files.createDirectories(it) }

            val width = resolution!!.first
            val height = resolution.second
            val fps = frameRate!!
            val panelFrames = manifest.panelTimeline.mapIndexed { index, panel ->
                val framePath = work.resolve("panel_%03d.png".format(index + 1))
                renderPanelImage(manifest, panel, index, width, height, framePath)
                artifacts.add(artifact("panel_png", framePath))
                framePath
            }
            val videoConcat = writeVideoConcat(work, manifest.panelTimeline, panelFrames)
            artifacts.add(artifact("video_concat_manifest", videoConcat))

            val silentVideo = work.resolve("video_silent.mp4")
            runRequired(
                command = listOf(
                    "ffmpeg",
                    "-y",
                    "-hide_banner",
                    "-f",
                    "concat",
                    "-safe",
                    "0",
                    "-i",
                    videoConcat.fileName.toString(),
                    "-vf",
                    "fps=${fpsString(fps)},format=yuv420p",
                    "-pix_fmt",
                    "yuv420p",
                    silentVideo.fileName.toString(),
                ),
                workingDir = work,
                findings = findings,
                ruleId = "FFMPEG_VIDEO_ASSEMBLY_FAILED",
            )
            ffmpegCommandCount += 1
            artifacts.add(artifact("silent_video_mp4", silentVideo))

            val speechScript = writeSpeechScript(work)
            artifacts.add(artifact("speech_script", speechScript))
            val audioSegments = manifest.panelTimeline.mapIndexed { index, panel ->
                val segment = work.resolve("audio_segment_%03d.wav".format(index + 1))
                val spokenText = spokenTextForPanel(manifest, panel)
                val durationSeconds = seconds(panel.displayDurationMillis)
                if (spokenText.isBlank()) {
                    runRequired(
                        command = listOf(
                            "ffmpeg",
                            "-y",
                            "-hide_banner",
                            "-f",
                            "lavfi",
                            "-i",
                            "anullsrc=r=48000:cl=stereo",
                            "-t",
                            durationSeconds,
                            segment.fileName.toString(),
                        ),
                        workingDir = work,
                        findings = findings,
                        ruleId = "FFMPEG_SILENCE_SEGMENT_FAILED",
                    )
                    ffmpegCommandCount += 1
                } else {
                    val textPath = work.resolve("speech_%03d.txt".format(index + 1))
                    val rawSpeech = work.resolve("speech_%03d.wav".format(index + 1))
                    Files.writeString(textPath, spokenText, StandardCharsets.UTF_8)
                    artifacts.add(artifact("speech_text", textPath))
                    runRequired(
                        command = listOf(
                            "powershell",
                            "-NoProfile",
                            "-ExecutionPolicy",
                            "Bypass",
                            "-File",
                            speechScript.fileName.toString(),
                            "-TextPath",
                            textPath.fileName.toString(),
                            "-OutPath",
                            rawSpeech.fileName.toString(),
                            "-VoiceName",
                            voiceName,
                        ),
                        workingDir = work,
                        findings = findings,
                        ruleId = "WINDOWS_SPEECH_SYNTHESIS_FAILED",
                    )
                    speechCommandCount += 1
                    artifacts.add(artifact("speech_wav_raw", rawSpeech))
                    runRequired(
                        command = listOf(
                            "ffmpeg",
                            "-y",
                            "-hide_banner",
                            "-i",
                            rawSpeech.fileName.toString(),
                            "-af",
                            "aformat=sample_rates=48000:channel_layouts=stereo,apad,atrim=0:$durationSeconds",
                            segment.fileName.toString(),
                        ),
                        workingDir = work,
                        findings = findings,
                        ruleId = "FFMPEG_AUDIO_SEGMENT_FAILED",
                    )
                    ffmpegCommandCount += 1
                }
                artifacts.add(artifact("audio_segment_wav", segment))
                segment
            }
            val audioConcat = writeAudioConcat(work, audioSegments)
            artifacts.add(artifact("audio_concat_manifest", audioConcat))
            val narration = work.resolve("narration.wav")
            runRequired(
                command = listOf(
                    "ffmpeg",
                    "-y",
                    "-hide_banner",
                    "-f",
                    "concat",
                    "-safe",
                    "0",
                    "-i",
                    audioConcat.fileName.toString(),
                    "-c",
                    "copy",
                    narration.fileName.toString(),
                ),
                workingDir = work,
                findings = findings,
                ruleId = "FFMPEG_AUDIO_CONCAT_FAILED",
            )
            ffmpegCommandCount += 1
            artifacts.add(artifact("narration_wav", narration))

            runRequired(
                command = listOf(
                    "ffmpeg",
                    "-y",
                    "-hide_banner",
                    "-i",
                    silentVideo.fileName.toString(),
                    "-i",
                    narration.fileName.toString(),
                    "-map",
                    "0:v:0",
                    "-map",
                    "1:a:0",
                    "-t",
                    seconds(manifest.renderSettings.totalDurationMillis),
                    "-c:v",
                    "libx264",
                    "-pix_fmt",
                    "yuv420p",
                    "-c:a",
                    "aac",
                    "-b:a",
                    "128k",
                    output.toString(),
                ),
                workingDir = work,
                findings = findings,
                ruleId = "FFMPEG_FINAL_MUX_FAILED",
            )
            ffmpegCommandCount += 1
            artifacts.add(artifact("output_video_mp4", output))

            if (!Files.exists(output) || Files.size(output) <= 0) {
                findings.add(finding("OUTPUT_VIDEO_MISSING", "media_output", "CRITICAL", "$.outputVideoPath", "Local renderer did not create a non-empty MP4 output.", listOf(output.toString())))
            }
        } catch (exception: Exception) {
            findings.add(
                finding(
                    "LOCAL_RENDER_EXCEPTION",
                    "execution",
                    "CRITICAL",
                    "$.execution",
                    "Local renderer failed before completing MP4 output.",
                    listOf(exception::class.java.simpleName, exception.message.orEmpty()),
                ),
            )
        }

        val verdict = if (findings.hasBlocking()) "FAIL" else "PASS"
        return report(
            verdict = verdict,
            renderStatus = if (verdict == "PASS") "local_preview_video_rendered" else "FAIL",
            renderId = resolvedRenderId,
            manifest = manifest,
            manifestFile = manifestFile,
            output = output,
            work = work,
            voiceName = voiceName,
            dryRun = false,
            mediaGenerated = verdict == "PASS",
            nextAllowedDownstreamStep = if (verdict == "PASS") "local_preview_video_review" else "",
            findings = findings,
            artifacts = artifacts,
            ffmpegCommandCount = ffmpegCommandCount,
            speechCommandCount = speechCommandCount,
        )
    }

    private fun validateManifest(
        manifest: ComicVideoRenderHandoffManifest,
        manifestFile: String,
        output: Path,
        work: Path,
        findings: MutableList<ComicVideoLocalRenderFinding>,
    ) {
        if (manifest.verdict.trim().uppercase() != "PASS") {
            findings.add(finding("MANIFEST_VERDICT_NOT_PASS", "upstream", "CRITICAL", "$.verdict", "Local renderer requires a PASS render handoff manifest.", listOf(manifest.verdict)))
        }
        if (manifest.manifestStatus != "comic_video_render_handoff_manifest_candidate") {
            findings.add(finding("MANIFEST_STATUS_MISMATCH", "upstream", "CRITICAL", "$.manifestStatus", "Local renderer requires manifestStatus=comic_video_render_handoff_manifest_candidate.", listOf(manifest.manifestStatus)))
        }
        if (manifest.nextAllowedDownstreamStep != "external_manual_renderer_handoff") {
            findings.add(finding("MANIFEST_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.nextAllowedDownstreamStep", "Local renderer requires nextAllowedDownstreamStep=external_manual_renderer_handoff.", listOf(manifest.nextAllowedDownstreamStep)))
        }
        if (manifestFile.isBlank()) {
            findings.add(finding("MANIFEST_FILE_MISSING", "source_linkage", "CRITICAL", "$.inputs.renderHandoffManifestFile", "Local renderer requires a manifest source file path."))
        }
        if (manifest.panelTimeline.isEmpty()) {
            findings.add(finding("PANEL_TIMELINE_EMPTY", "timeline", "CRITICAL", "$.panelTimeline", "Local renderer requires at least one panel timeline item."))
        }
        if (manifest.voiceTimeline.isEmpty()) {
            findings.add(finding("VOICE_TIMELINE_EMPTY", "timeline", "CRITICAL", "$.voiceTimeline", "Local renderer requires at least one voice cue for audible output."))
        }
        if (manifest.renderSettings.totalDurationMillis <= 0) {
            findings.add(finding("TOTAL_DURATION_INVALID", "timeline", "CRITICAL", "$.renderSettings.totalDurationMillis", "Local renderer requires a positive total duration.", listOf(manifest.renderSettings.totalDurationMillis.toString())))
        }
        manifest.panelTimeline.forEachIndexed { index, panel ->
            if (panel.panelId.isBlank()) {
                findings.add(finding("PANEL_ID_MISSING", "timeline", "CRITICAL", "$.panelTimeline[$index].panelId", "Every panel timeline item needs a panel id."))
            }
            if (panel.displayDurationMillis <= 0 || panel.endMillis <= panel.startMillis) {
                findings.add(finding("PANEL_DURATION_INVALID", "timeline", "CRITICAL", "$.panelTimeline[$index]", "Every panel needs a positive display duration and endMillis after startMillis."))
            }
        }
        if (output.fileName == null) {
            findings.add(finding("OUTPUT_VIDEO_PATH_INVALID", "media_output", "CRITICAL", "$.outputVideoPath", "Local renderer requires an output MP4 path."))
        }
        if (work.fileName == null) {
            findings.add(finding("WORK_DIR_INVALID", "media_output", "CRITICAL", "$.workDir", "Local renderer requires a work directory."))
        }
    }

    private fun parseResolution(
        value: String,
        findings: MutableList<ComicVideoLocalRenderFinding>,
    ): Pair<Int, Int>? {
        val parts = value.lowercase().split("x")
        val width = parts.getOrNull(0)?.toIntOrNull()
        val height = parts.getOrNull(1)?.toIntOrNull()
        if (parts.size != 2 || width == null || height == null || width <= 0 || height <= 0) {
            findings.add(finding("RESOLUTION_INVALID", "render_settings", "CRITICAL", "$.renderSettings.resolution", "Local renderer requires resolution formatted like 1920x1080.", listOf(value)))
            return null
        }
        return width to height
    }

    private fun parseFrameRate(
        value: String,
        findings: MutableList<ComicVideoLocalRenderFinding>,
    ): Double? {
        val trimmed = value.trim()
        val parsed = if ("/" in trimmed) {
            val numerator = trimmed.substringBefore("/").toDoubleOrNull()
            val denominator = trimmed.substringAfter("/").toDoubleOrNull()
            if (numerator != null && denominator != null && denominator != 0.0) numerator / denominator else null
        } else {
            trimmed.toDoubleOrNull()
        }
        if (parsed == null || parsed <= 0.0) {
            findings.add(finding("FRAME_RATE_INVALID", "render_settings", "CRITICAL", "$.renderSettings.frameRate", "Local renderer requires a positive frame rate.", listOf(value)))
            return null
        }
        return parsed
    }

    private fun renderPanelImage(
        manifest: ComicVideoRenderHandoffManifest,
        panel: ComicVideoRenderPanelTimelineItem,
        index: Int,
        width: Int,
        height: Int,
        output: Path,
    ) {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            graphics.color = Color(15, 21, 31)
            graphics.fillRect(0, 0, width, height)
            drawBackgroundRain(graphics, width, height, index)

            val margin = (width * 0.055).roundToInt()
            val panelTop = (height * 0.07).roundToInt()
            val panelHeight = (height * 0.70).roundToInt()
            graphics.color = Color(28, 40, 51)
            graphics.fillRect(margin, panelTop, width - margin * 2, panelHeight)
            graphics.stroke = BasicStroke(7f)
            graphics.color = Color(77, 183, 222)
            graphics.drawRect(margin, panelTop, width - margin * 2, panelHeight)
            graphics.stroke = BasicStroke(3f)
            graphics.color = Color(210, 218, 226)
            graphics.drawRect(margin + 36, panelTop + 34, width - margin * 2 - 72, panelHeight - 68)

            drawPanelBadge(graphics, index + 1, margin + 68, panelTop + 76)
            val titleFont = font(Font.BOLD, (height * 0.064).roundToInt())
            val labelFont = font(Font.BOLD, (height * 0.030).roundToInt())
            graphics.font = titleFont
            graphics.color = Color.WHITE
            graphics.drawString(panel.sceneReference.ifBlank { panel.panelId }, margin + 230, panelTop + 128)
            graphics.font = labelFont
            graphics.color = Color(225, 232, 238)
            graphics.drawString(panel.beatReference.ifBlank { "local preview render" }, margin + 230, panelTop + 172)

            drawPreviewShapes(graphics, width, height, index)

            val bodyFont = font(Font.PLAIN, (height * 0.032).roundToInt())
            graphics.font = bodyFont
            graphics.color = Color(235, 239, 243)
            val bodyText = listOf(panel.visualIntent, panel.cameraIntent, panel.mustShow.joinToString("、"))
                .filter { it.isNotBlank() }
                .joinToString(" ")
            drawWrapped(graphics, bodyText, margin + 170, panelTop + panelHeight - 104, width - margin * 2 - 340, 44, 2)

            val subtitleBoxHeight = (height * 0.17).roundToInt()
            graphics.color = Color(3, 8, 14)
            graphics.fillRect(0, height - subtitleBoxHeight, width, subtitleBoxHeight)
            graphics.color = Color(77, 183, 222)
            graphics.fillRect(0, height - subtitleBoxHeight, 28, subtitleBoxHeight)
            graphics.font = font(Font.BOLD, (height * 0.047).roundToInt())
            graphics.color = Color.WHITE
            drawWrapped(
                graphics = graphics,
                text = subtitleTextForPanel(manifest, panel).ifBlank { spokenTextForPanel(manifest, panel) },
                x = margin,
                y = height - subtitleBoxHeight + 78,
                maxWidth = width - margin * 2,
                lineHeight = 56,
                maxLines = 2,
            )
        } finally {
            graphics.dispose()
        }
        Files.createDirectories(output.parent)
        ImageIO.write(image, "png", output.toFile())
    }

    private fun drawBackgroundRain(graphics: Graphics2D, width: Int, height: Int, index: Int) {
        graphics.color = Color(27 + index * 6, 38 + index * 5, 50 + index * 4)
        graphics.fillRect(0, 0, width, height)
        graphics.stroke = BasicStroke(4f)
        graphics.color = Color(96, 123, 151, 140)
        var x = -120 + index * 35
        while (x < width + 120) {
            graphics.drawLine(x, (height * 0.08).roundToInt(), x - 70, (height * 0.78).roundToInt())
            x += 85
        }
    }

    private fun drawPanelBadge(graphics: Graphics2D, number: Int, centerX: Int, centerY: Int) {
        graphics.color = Color(78, 182, 223)
        graphics.fillOval(centerX - 58, centerY - 58, 116, 116)
        graphics.font = Font("Arial", Font.BOLD, 48)
        graphics.color = Color.BLACK
        val label = "%02d".format(number)
        val metrics = graphics.fontMetrics
        graphics.drawString(label, centerX - metrics.stringWidth(label) / 2, centerY + metrics.ascent / 3)
    }

    private fun drawPreviewShapes(graphics: Graphics2D, width: Int, height: Int, index: Int) {
        val baseX = (width * 0.20).roundToInt()
        val baseY = (height * 0.36).roundToInt()
        graphics.color = Color(49, 72, 88)
        graphics.fillRect(baseX - 120, baseY - 40, 430, 260)
        graphics.color = Color(35, 40, 50)
        graphics.fillOval(baseX + 85, baseY + 32, 94, 94)
        graphics.color = Color(49, 96, 128)
        graphics.fillRect(baseX + 44, baseY + 118, 168, 212)
        graphics.color = Color(240, 224, 183)
        graphics.fillRect(baseX + 214, baseY + 150, 220, 112)
        graphics.color = Color(52, 43, 30)
        graphics.stroke = BasicStroke(4f)
        graphics.drawLine(baseX + 214, baseY + 150, baseX + 324, baseY + 214)
        graphics.drawLine(baseX + 434, baseY + 150, baseX + 324, baseY + 214)
        graphics.color = Color(195, 36, 52)
        graphics.fillOval(baseX + 314, baseY + 190, 32, 32)
        graphics.color = if (index % 2 == 0) Color(250, 206, 78) else Color(113, 205, 177)
        graphics.fillOval((width * 0.66).roundToInt(), (height * 0.18).roundToInt(), 96, 96)
        graphics.color = Color(4, 14, 16, 180)
        graphics.fillRect((width * 0.37).roundToInt(), (height * 0.32).roundToInt(), (width * 0.38).roundToInt(), (height * 0.30).roundToInt())
    }

    private fun writeVideoConcat(
        work: Path,
        panels: List<ComicVideoRenderPanelTimelineItem>,
        frames: List<Path>,
    ): Path {
        val concat = work.resolve("video_concat.txt")
        val lines = mutableListOf<String>()
        panels.zip(frames).forEach { (panel, frame) ->
            lines.add("file '${frame.fileName}'")
            lines.add("duration ${seconds(panel.displayDurationMillis)}")
        }
        frames.lastOrNull()?.let { lines.add("file '${it.fileName}'") }
        Files.writeString(concat, lines.joinToString(System.lineSeparator()), StandardCharsets.US_ASCII)
        return concat
    }

    private fun writeAudioConcat(work: Path, audioSegments: List<Path>): Path {
        val concat = work.resolve("audio_concat.txt")
        val lines = audioSegments.map { "file '${it.fileName}'" }
        Files.writeString(concat, lines.joinToString(System.lineSeparator()), StandardCharsets.US_ASCII)
        return concat
    }

    private fun writeSpeechScript(work: Path): Path {
        val script = work.resolve("synthesize_speech.ps1")
        Files.writeString(
            script,
            """
            param(
                [Parameter(Mandatory=${'$'}true)][string]${'$'}TextPath,
                [Parameter(Mandatory=${'$'}true)][string]${'$'}OutPath,
                [string]${'$'}VoiceName = ""
            )
            Add-Type -AssemblyName System.Speech
            ${'$'}synth = New-Object System.Speech.Synthesis.SpeechSynthesizer
            if (${'$'}VoiceName.Trim().Length -gt 0) {
                ${'$'}synth.SelectVoice(${'$'}VoiceName)
            }
            ${'$'}text = Get-Content -LiteralPath ${'$'}TextPath -Raw -Encoding UTF8
            ${'$'}synth.SetOutputToWaveFile(${'$'}OutPath)
            ${'$'}synth.Speak(${'$'}text)
            ${'$'}synth.Dispose()
            """.trimIndent(),
            StandardCharsets.UTF_8,
        )
        return script
    }

    private fun runRequired(
        command: List<String>,
        workingDir: Path,
        findings: MutableList<ComicVideoLocalRenderFinding>,
        ruleId: String,
    ) {
        val result = commandRunner.run(command, workingDir)
        if (result.exitCode != 0) {
            findings.add(
                finding(
                    ruleId,
                    "external_tool",
                    "CRITICAL",
                    "$.command",
                    "Local renderer external tool command failed.",
                    listOf(command.joinToString(" "), result.output.take(1200)),
                ),
            )
            error("$ruleId failed")
        }
    }

    private fun subtitleTextForPanel(
        manifest: ComicVideoRenderHandoffManifest,
        panel: ComicVideoRenderPanelTimelineItem,
    ): String =
        manifest.subtitleTimeline
            .filter { it.panelId == panel.panelId }
            .joinToString(" ") { it.text.trim() }
            .trim()

    private fun spokenTextForPanel(
        manifest: ComicVideoRenderHandoffManifest,
        panel: ComicVideoRenderPanelTimelineItem,
    ): String =
        manifest.voiceTimeline
            .filter { it.panelId == panel.panelId }
            .joinToString(" ") { cue ->
                val speaker = cue.displayName.ifBlank { cue.characterId }
                listOf(speaker, cue.text).filter { it.isNotBlank() }.joinToString("：")
            }
            .trim()

    private fun drawWrapped(
        graphics: Graphics2D,
        text: String,
        x: Int,
        y: Int,
        maxWidth: Int,
        lineHeight: Int,
        maxLines: Int,
    ) {
        val lines = wrapText(text, graphics, maxWidth).take(maxLines)
        lines.forEachIndexed { index, line ->
            graphics.drawString(line, x, y + index * lineHeight)
        }
    }

    private fun wrapText(text: String, graphics: Graphics2D, maxWidth: Int): List<String> {
        val normalized = text.replace('\r', ' ').replace('\n', ' ').trim()
        if (normalized.isBlank()) return emptyList()
        if (normalized.any { it.isWhitespace() }) {
            return wrapWords(normalized, graphics, maxWidth)
        }
        return wrapCharacters(normalized, graphics, maxWidth)
    }

    private fun wrapWords(text: String, graphics: Graphics2D, maxWidth: Int): List<String> {
        val metrics = graphics.fontMetrics
        val lines = mutableListOf<String>()
        var current = ""
        text.split(Regex("\\s+")).forEach { word ->
            val candidate = if (current.isBlank()) word else "$current $word"
            if (current.isEmpty() || metrics.stringWidth(candidate) <= maxWidth) {
                current = candidate
            } else if (metrics.stringWidth(word) > maxWidth) {
                if (current.isNotBlank()) lines.add(current.trim())
                val brokenWord = wrapCharacters(word, graphics, maxWidth)
                lines.addAll(brokenWord.dropLast(1))
                current = brokenWord.lastOrNull().orEmpty()
            } else {
                lines.add(current.trim())
                current = word
            }
        }
        if (current.isNotBlank()) lines.add(current.trim())
        return lines
    }

    private fun wrapCharacters(text: String, graphics: Graphics2D, maxWidth: Int): List<String> {
        val metrics = graphics.fontMetrics
        val lines = mutableListOf<String>()
        var current = ""
        text.forEach { char ->
            val candidate = current + char
            if (current.isEmpty() || metrics.stringWidth(candidate) <= maxWidth) {
                current = candidate
            } else {
                lines.add(current)
                current = char.toString()
            }
        }
        if (current.isNotBlank()) lines.add(current)
        return lines
    }

    private fun font(style: Int, size: Int): Font {
        val noto = Path.of("C:\\Windows\\Fonts\\NotoSansTC-VF.ttf")
        return try {
            if (Files.exists(noto)) {
                val base = Font.createFont(Font.TRUETYPE_FONT, noto.toFile())
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(base)
                base.deriveFont(style, max(12, size).toFloat())
            } else {
                Font("Microsoft JhengHei", style, max(12, size))
            }
        } catch (_: Exception) {
            Font("Dialog", style, max(12, size))
        }
    }

    private fun plannedArtifacts(output: Path): List<ComicVideoLocalRenderArtifact> =
        listOf(artifact("output_video_mp4", output))

    private fun artifact(type: String, path: Path): ComicVideoLocalRenderArtifact =
        ComicVideoLocalRenderArtifact(
            artifactType = type,
            path = path.toString(),
            exists = Files.exists(path),
            bytes = if (Files.exists(path)) Files.size(path) else 0,
        )

    private fun report(
        verdict: String,
        renderStatus: String,
        renderId: String,
        manifest: ComicVideoRenderHandoffManifest,
        manifestFile: String,
        output: Path,
        work: Path,
        voiceName: String,
        dryRun: Boolean,
        mediaGenerated: Boolean,
        nextAllowedDownstreamStep: String,
        findings: List<ComicVideoLocalRenderFinding>,
        artifacts: List<ComicVideoLocalRenderArtifact>,
        ffmpegCommandCount: Int = 0,
        speechCommandCount: Int = 0,
    ): ComicVideoLocalRenderReport =
        ComicVideoLocalRenderReport(
            verdict = verdict,
            renderId = renderId,
            renderStatus = renderStatus,
            renderHandoffManifestFile = manifestFile,
            manifestId = manifest.manifestId,
            packetId = manifest.packetId,
            outputVideoPath = output.toString(),
            workDir = work.toString(),
            voiceName = voiceName,
            dryRun = dryRun,
            mediaGenerated = mediaGenerated,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
            blockedReason = if (verdict == "FAIL") findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId } else "",
            findingCount = findings.size,
            findings = findings,
            artifacts = artifacts,
            evidence = ComicVideoLocalRenderEvidence(
                manifestVerdict = manifest.verdict,
                manifestNextAllowedDownstreamStep = manifest.nextAllowedDownstreamStep,
                panelCount = manifest.panelTimeline.size,
                voiceCueCount = manifest.voiceTimeline.size,
                subtitleCueCount = manifest.subtitleTimeline.size,
                totalDurationMillis = manifest.renderSettings.totalDurationMillis,
                resolution = manifest.renderSettings.resolution,
                frameRate = manifest.renderSettings.frameRate,
                ffmpegCommandCount = ffmpegCommandCount,
                speechCommandCount = speechCommandCount,
            ),
        )

    private fun MutableList<ComicVideoLocalRenderFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): ComicVideoLocalRenderFinding =
        ComicVideoLocalRenderFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private fun seconds(milliseconds: Int): String =
        String.format(Locale.US, "%.3f", milliseconds / 1000.0)

    private fun fpsString(frameRate: Double): String =
        if (frameRate % 1.0 == 0.0) frameRate.toInt().toString() else String.format(Locale.US, "%.3f", frameRate)

    private class DefaultCommandRunner : CommandRunner {
        override fun run(command: List<String>, workingDir: Path): CommandResult {
            val process = ProcessBuilder(command)
                .directory(workingDir.toFile())
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader(StandardCharsets.UTF_8).readText()
            val exitCode = process.waitFor()
            return CommandResult(exitCode, output)
        }
    }
}
