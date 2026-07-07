package com.localnovel.engine

import com.localnovel.engine.model.ComicVideoLocalPreviewBuildReport
import com.localnovel.engine.model.ComicVideoPacketValidationReport
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComicVideoLocalPreviewBuildCliTest {
    @Test
    fun dryRunBuildsTraditionalChinesePreviewChainWithoutMedia() {
        val sampleDir = sampleDir("comic_video_workbench_zh_tw")
        val tempDir = Files.createTempDirectory("comic-video-local-preview-build-dry-run")
        val reportPath = tempDir.resolve("build_report.json")
        val outputVideo = tempDir.resolve("preview.mp4")

        val exit = Cli().run(
            listOf(
                "build-comic-video-local-preview",
                "--image-facing-delivery-proof",
                sampleDir.resolve("image_facing_delivery_proof_zh_tw_sample.json").toString(),
                "--voice-timeline-fill",
                sampleDir.resolve("voice_timeline_fill_zh_tw_sample.json").toString(),
                "--output-dir",
                tempDir.toString(),
                "--out-video",
                outputVideo.toString(),
                "--build",
                "comic-video-zh-tw-build-test",
                "--packet",
                "comic-video-zh-tw-sample-packet-001",
                "--validation",
                "comic-video-validation-zh-tw-sample-001",
                "--manifest",
                "comic-video-render-handoff-zh-tw-sample-001",
                "--render",
                "comic-video-zh-tw-local-render-test",
                "--panel-duration-ms",
                "3000",
                "--report-out",
                reportPath.toString(),
                "--dry-run",
            ),
        )

        assertEquals(0, exit)
        assertFalse(Files.exists(outputVideo), "Dry-run must not create MP4 output.")
        assertTrue(Files.exists(reportPath))
        listOf(
            "comic_video_packet.json",
            "voice_timeline_fill_template.json",
            "comic_video_packet_filled.json",
            "comic_video_packet_validation.json",
            "comic_video_render_handoff_manifest.json",
            "comic_video_local_render_report.json",
        ).forEach { fileName ->
            assertTrue(Files.exists(tempDir.resolve(fileName)), "Missing build artifact: $fileName")
        }

        val report = readJson<ComicVideoLocalPreviewBuildReport>(reportPath)
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("DRY_RUN", report.buildStatus)
        assertEquals(false, report.mediaGenerated)
        assertEquals("PASS", report.evidence.packetValidationVerdict)
        assertEquals("PASS", report.evidence.manifestVerdict)
        assertEquals("PASS", report.evidence.localRenderVerdict)
        assertEquals(9000, report.evidence.totalDurationMillis)
        assertEquals(true, report.renderReport?.dryRun)
    }

    @Test
    fun dryRunBuildsSettingSourcePreviewProofAndVoiceFillWithoutMedia() {
        val sampleDir = sampleDir("chenxing/comic_video_setting_source")
        val tempDir = Files.createTempDirectory("comic-video-setting-source-preview-build-dry-run")
        val reportPath = tempDir.resolve("build_report.json")
        val outputVideo = tempDir.resolve("preview.mp4")

        val exit = Cli().run(
            listOf(
                "build-comic-video-local-preview",
                "--image-facing-delivery-proof",
                sampleDir.resolve("image_facing_preview_build/image_facing_delivery_proof.json").toString(),
                "--voice-timeline-fill",
                sampleDir.resolve("voice_timeline_fill_chenxing_preview_sample.json").toString(),
                "--output-dir",
                tempDir.toString(),
                "--out-video",
                outputVideo.toString(),
                "--build",
                "comic-video-setting-chenxing-build-test",
                "--packet",
                "comic-video-setting-chenxing-preview-001",
                "--validation",
                "comic-video-validation-setting-chenxing-preview-test",
                "--manifest",
                "comic-video-render-handoff-setting-chenxing-preview-test",
                "--render",
                "comic-video-setting-chenxing-local-render-test",
                "--panel-duration-ms",
                "3000",
                "--report-out",
                reportPath.toString(),
                "--dry-run",
            ),
        )

        assertEquals(0, exit)
        assertFalse(Files.exists(outputVideo), "Dry-run must not create MP4 output.")
        assertTrue(Files.exists(reportPath))
        val report = readJson<ComicVideoLocalPreviewBuildReport>(reportPath)
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("DRY_RUN", report.buildStatus)
        assertEquals(false, report.mediaGenerated)
        assertEquals("PASS", report.evidence.packetValidationVerdict)
        assertEquals("PASS", report.evidence.manifestVerdict)
        assertEquals("PASS", report.evidence.localRenderVerdict)
        assertEquals(9000, report.evidence.totalDurationMillis)
        assertEquals(true, report.renderReport?.dryRun)
    }

    @Test
    fun validationFailureWritesBuildReportAndReturnsNonZero() {
        val sampleDir = sampleDir("comic_video_workbench_zh_tw")
        val tempDir = Files.createTempDirectory("comic-video-local-preview-build-fail")
        val badFill = tempDir.resolve("bad_voice_timeline_fill.json")
        val reportPath = tempDir.resolve("build_report.json")
        Files.writeString(
            badFill,
            Files.readString(sampleDir.resolve("voice_timeline_fill_zh_tw_sample.json"))
                .replace("此樣本只用於本機預覽流程測試。", "final asset"),
        )

        val exit = Cli().run(
            listOf(
                "build-comic-video-local-preview",
                "--image-facing-delivery-proof",
                sampleDir.resolve("image_facing_delivery_proof_zh_tw_sample.json").toString(),
                "--voice-timeline-fill",
                badFill.toString(),
                "--output-dir",
                tempDir.toString(),
                "--out-video",
                tempDir.resolve("preview.mp4").toString(),
                "--build",
                "comic-video-zh-tw-build-fail-test",
                "--packet",
                "comic-video-zh-tw-sample-packet-001",
                "--validation",
                "comic-video-validation-zh-tw-sample-001",
                "--manifest",
                "comic-video-render-handoff-zh-tw-sample-001",
                "--render",
                "comic-video-zh-tw-local-render-fail-test",
                "--panel-duration-ms",
                "3000",
                "--report-out",
                reportPath.toString(),
                "--dry-run",
            ),
        )

        assertEquals(1, exit)
        assertTrue(Files.exists(reportPath))
        val validation = readJson<ComicVideoPacketValidationReport>(tempDir.resolve("comic_video_packet_validation.json"))
        val report = readJson<ComicVideoLocalPreviewBuildReport>(reportPath)
        assertEquals("FAIL", validation.verdict)
        assertEquals("FAIL", report.verdict)
        assertEquals("FAIL", report.buildStatus)
        assertTrue(report.findings.any { it.ruleId == "COMIC_VIDEO_PACKET_VALIDATION_FAILED" })
        assertEquals("FAIL", report.evidence.packetValidationVerdict)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private fun sampleDir(sampleDirName: String): Path {
        var current: Path? = Path.of("").toAbsolutePath()
        while (current != null) {
            val candidate = current.resolve("samples").resolve(sampleDirName)
            if (Files.isDirectory(candidate)) {
                return candidate
            }
            current = current.parent
        }
        error("Could not locate samples/$sampleDirName from test working directory.")
    }
}
