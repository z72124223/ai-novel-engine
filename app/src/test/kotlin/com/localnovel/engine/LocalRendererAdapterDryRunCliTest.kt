package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererAdapterDryRunReport
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalRendererAdapterDryRunCliTest {
    @Test
    fun cliWritesLocalRendererAdapterDryRunReport() {
        val tempDir = Files.createTempDirectory("local-renderer-adapter-dry-run")
        val handoffPath = tempDir.resolve("preview_handoff.json")
        val reviewPath = tempDir.resolve("preview_handoff_review.json")
        val reportPath = tempDir.resolve("adapter_dry_run.json")
        val (handoff, review) = LocalRendererAdapterDryRunServiceTest.validInputs()
        writeJson(handoffPath, handoff)
        writeJson(reviewPath, review)

        val exit = Cli().run(
            listOf(
                "dry-run-local-renderer-adapter",
                "--handoff",
                handoffPath.toString(),
                "--review",
                reviewPath.toString(),
                "--dry-run-id",
                "adapter-dry-run-cli-001",
                "--out",
                reportPath.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(reportPath))
        val report = readJson<LocalRendererAdapterDryRunReport>(reportPath)
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_ADAPTER_DRY_RUN_PASS", report.status)
        assertEquals("adapter-dry-run-cli-001", report.dryRunId)
        assertEquals(handoff.handoffId, report.sourceHandoffId)
        assertEquals(review.reviewId, report.sourceReviewId)
        assertEquals(handoff.sourceStoryInputPackId, report.sourceStoryInputPackId)
        assertEquals(2, report.adapterMappingSummary.panelCount)
        assertEquals(2, report.adapterMappingSummary.mappedPanelCount)
        assertEquals(0, report.adapterMappingSummary.missingRequiredFieldCount)
        assertEquals(2, report.dryRunMappingPlan.size)
        assertFalse(report.rendererCompatibilitySummary.ready)
        assertFalse(report.rendererCompatibilitySummary.manifestGenerated)
        assertFalse(report.boundaries.mediaGenerated)
        assertEquals("none", report.boundaries.rendererInvocation)
        assertEquals("none", report.boundaries.providerInvocation)
        assertEquals("none", report.boundaries.ttsInvocation)
        assertTrue(report.boundaries.noExternalPaidProviderTools)
        assertEquals("local_renderer_adapter_dry_run_only_not_media_generation", report.downstreamBoundary)
        assertEquals("local_renderer_adapter_implementation_plan", report.nextAllowedDownstreamStep)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
