package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftReviewReport
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class LocalRendererAdapterImplementationDraftReviewCliTest {
    @Test
    fun cliWritesLocalRendererAdapterImplementationDraftReviewReport() {
        val tempDir = Files.createTempDirectory("local-renderer-adapter-implementation-draft-review")
        val draftPath = LocalRendererAdapterImplementationDraftReviewServiceTest.sampleDraftPath
        val reviewPath = tempDir.resolve("adapter_implementation_draft_review.json")

        val exit = Cli().run(
            listOf(
                "review-local-renderer-adapter-implementation-draft",
                "--draft",
                draftPath.toString(),
                "--review",
                "adapter-implementation-draft-review-cli-001",
                "--out",
                reviewPath.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(reviewPath))
        val report = readJson<LocalRendererAdapterImplementationDraftReviewReport>(reviewPath)
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_PASS", report.status)
        assertEquals("adapter-implementation-draft-review-cli-001", report.reviewId)
        assertEquals("chenxing-local-renderer-adapter-implementation-draft-v0-001", report.sourceDraftId)
        assertEquals("local-story-input-chenxing-v0-001", report.sourceStoryInputPackId)
        assertTrue(report.checks.isNotEmpty())
        assertTrue(report.checks.all { it.status == "PASS" })
        assertTrue(report.draftSummary.implementationTasks > 0)
        assertTrue(report.draftSummary.mappingDraftFields > 0)
        assertTrue(report.draftSummary.verificationSteps > 0)
        assertFalse(report.draftSummary.manifestGenerated)
        assertFalse(report.draftSummary.adapterImplementationExecuted)
        assertFalse(report.boundaries.mediaGenerated)
        assertEquals("none", report.boundaries.rendererInvocation)
        assertEquals("none", report.boundaries.providerInvocation)
        assertEquals("none", report.boundaries.ttsInvocation)
        assertFalse(report.boundaries.manifestGenerated)
        assertFalse(report.boundaries.adapterImplementationExecuted)
        assertTrue(report.boundaries.noExternalPaidProviderTools)
        assertFalse(report.boundaries.rendererReady)
        assertFalse(report.boundaries.mp4ReadinessClaimed)
        assertEquals("local_renderer_adapter_implementation_draft_review_only_not_manifest_generation", report.downstreamBoundary)
        assertEquals("local_renderer_manifest_draft_plan", report.nextAllowedDownstreamStep)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
}
