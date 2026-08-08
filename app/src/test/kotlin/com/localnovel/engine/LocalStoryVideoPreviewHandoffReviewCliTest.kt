package com.localnovel.engine

import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffReviewReport
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalStoryVideoPreviewHandoffReviewCliTest {
    @Test
    fun cliWritesPreviewHandoffReviewReport() {
        val tempDir = Files.createTempDirectory("local-story-video-preview-handoff-review")
        val handoffPath = tempDir.resolve("preview_handoff.json")
        val reviewPath = tempDir.resolve("preview_handoff_review.json")
        val handoff = LocalStoryVideoPreviewHandoffReviewServiceTest.validHandoff()
        writeJson(handoffPath, handoff)

        val exit = Cli().run(
            listOf(
                "review-local-story-video-preview-handoff",
                "--handoff",
                handoffPath.toString(),
                "--review",
                "preview-handoff-review-cli-001",
                "--out",
                reviewPath.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(reviewPath))
        val report = readJson<LocalStoryVideoPreviewHandoffReviewReport>(reviewPath)
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("preview-handoff-review-cli-001", report.reviewId)
        assertEquals(handoff.handoffId, report.sourceHandoffId)
        assertEquals(handoff.sourceStoryInputPackId, report.sourceStoryInputPackId)
        assertEquals("LOCAL_STORY_VIDEO_PREVIEW_HANDOFF_DRAFT", report.handoffStatus)
        assertEquals("local_story_video_preview_handoff_review_only_not_media_generation", report.downstreamBoundary)
        assertEquals("local_renderer_adapter_dry_run", report.nextAllowedDownstreamStep)
        assertFalse(report.boundaries.mediaGenerated)
        assertEquals("none", report.boundaries.rendererInvocation)
        assertEquals("none", report.boundaries.providerInvocation)
        assertEquals("none", report.boundaries.ttsInvocation)
        assertTrue(report.boundaries.noExternalPaidProviderTools)
        assertFalse(report.rendererCompatibilitySummary.ready)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
