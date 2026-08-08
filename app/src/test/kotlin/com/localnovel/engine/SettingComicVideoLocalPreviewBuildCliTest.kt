package com.localnovel.engine

import com.localnovel.engine.model.ComicVideoLocalPreviewBuildReport
import com.localnovel.engine.model.ImageFacingDeliveryProof
import com.localnovel.engine.model.SettingImageFacingProofPreviewBuildReport
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingComicVideoLocalPreviewBuildCliTest {
    @Test
    fun dryRunBuildsSettingSceneToComicVideoPreviewChainWithoutMedia() {
        val tempDir = Files.createTempDirectory("setting-comic-video-local-preview-dry-run")
        val outputVideo = tempDir.resolve("preview.mp4")

        val exit = Cli().run(
            listOf(
                "build-setting-comic-video-local-preview",
                "--setting-pack",
                ChenxingStoryTestFixtures.chenxingSettingSourceSample("setting_pack_chenxing_preview_sample.json").toString(),
                "--scene",
                "scene-huiguan-market",
                "--voice-timeline-fill",
                ChenxingStoryTestFixtures.chenxingSettingSourceSample("voice_timeline_fill_chenxing_preview_sample.json").toString(),
                "--output-dir",
                tempDir.toString(),
                "--out-video",
                outputVideo.toString(),
                "--build",
                "setting-comic-video-preview-test-001",
                "--panel-duration-ms",
                "3000",
                "--dry-run",
            ),
        )

        assertEquals(0, exit)
        assertFalse(Files.exists(outputVideo), "Dry-run must not create MP4 output.")

        val settingDir = tempDir.resolve("setting_image_facing_preview")
        val videoDir = tempDir.resolve("comic_video_local_preview")
        val settingReport = readJson<SettingImageFacingProofPreviewBuildReport>(
            settingDir.resolve("setting_image_facing_proof_preview_build_report.json"),
        )
        val proof = readJson<ImageFacingDeliveryProof>(settingDir.resolve("image_facing_delivery_proof.json"))
        val videoReport = readJson<ComicVideoLocalPreviewBuildReport>(
            videoDir.resolve("comic_video_local_preview_build_report.json"),
        )

        assertEquals("PASS", settingReport.verdict, settingReport.findings.joinToString { it.ruleId })
        assertEquals("PASS", proof.verdict, proof.findings.joinToString { it.ruleId })
        assertEquals(3, proof.frameDeliveryCards.size)
        assertEquals("PASS", videoReport.verdict, videoReport.findings.joinToString { it.ruleId })
        assertEquals("DRY_RUN", videoReport.buildStatus)
        assertEquals(false, videoReport.mediaGenerated)
        assertEquals("PASS", videoReport.evidence.packetValidationVerdict)
        assertEquals("PASS", videoReport.evidence.manifestVerdict)
        assertEquals("PASS", videoReport.evidence.localRenderVerdict)
        assertEquals(9000, videoReport.evidence.totalDurationMillis)
    }

    @Test
    fun missingSceneStopsBeforeComicVideoBuild() {
        val tempDir = Files.createTempDirectory("setting-comic-video-local-preview-missing-scene")

        val exit = Cli().run(
            listOf(
                "build-setting-comic-video-local-preview",
                "--setting-pack",
                ChenxingStoryTestFixtures.chenxingSettingSourceSample("setting_pack_chenxing_preview_sample.json").toString(),
                "--scene",
                "scene-missing",
                "--voice-timeline-fill",
                ChenxingStoryTestFixtures.chenxingSettingSourceSample("voice_timeline_fill_chenxing_preview_sample.json").toString(),
                "--output-dir",
                tempDir.toString(),
                "--dry-run",
            ),
        )

        assertEquals(1, exit)
        val settingReport = readJson<SettingImageFacingProofPreviewBuildReport>(
            tempDir.resolve("setting_image_facing_preview/setting_image_facing_proof_preview_build_report.json"),
        )
        assertEquals("FAIL", settingReport.verdict)
        assertTrue(settingReport.findings.any { it.ruleId == "SETTING_SCRIPT_PACKET_DRAFT_BUILD_FAILED" })
        assertFalse(Files.exists(tempDir.resolve("comic_video_local_preview/comic_video_local_preview_build_report.json")))
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
}