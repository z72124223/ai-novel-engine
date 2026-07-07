package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererManifestDraftJsonAdapterDryRunReport
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class LocalRendererManifestDraftJsonAdapterDryRunCliTest {
    @Test
    fun cliWritesLocalRendererManifestDraftJsonAdapterDryRunReport() {
        val tempDir = Files.createTempDirectory("local-renderer-manifest-draft-json-adapter-dry-run")
        val outPath = tempDir.resolve("manifest_draft_json_adapter_dry_run.json")

        val exit = Cli().run(
            listOf(
                "dry-run-local-renderer-manifest-draft-json-adapter",
                "--draft",
                LocalRendererManifestDraftJsonAdapterDryRunServiceTest.sampleDraftPath.toString(),
                "--review",
                LocalRendererManifestDraftJsonAdapterDryRunServiceTest.sampleReviewPath.toString(),
                "--dry-run-id",
                "manifest-draft-json-adapter-dry-run-cli-001",
                "--out",
                outPath.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(outPath))
        val report = readJson<LocalRendererManifestDraftJsonAdapterDryRunReport>(outPath)
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_PASS", report.status)
        assertEquals("manifest-draft-json-adapter-dry-run-cli-001", report.dryRunId)
        assertEquals("rain-gate-local-renderer-manifest-draft-json-draft-v0-001", report.sourceDraftId)
        assertEquals("rain-gate-local-renderer-manifest-draft-json-review-v0-001", report.sourceReviewId)
        assertEquals("rain-gate-local-renderer-manifest-draft-plan-v0-001", report.sourceManifestDraftPlanId)
        assertEquals("local-story-input-rain-gate-v0-001", report.sourceStoryInputPackId)
        assertTrue(report.checks.isNotEmpty())
        assertTrue(report.checks.all { it.status == "PASS" })
        assertEquals(2, report.panelMappingSummary.draftPanelCount)
        assertEquals(2, report.panelMappingSummary.mappedPanelCount)
        assertEquals(0, report.panelMappingSummary.unmappedPanelCount)
        assertEquals(46, report.draftFieldCoverageSummary.totalDraftFields)
        assertTrue(report.boundaries.draftManifestGenerated)
        assertFalse(report.boundaries.productionManifestGenerated)
        assertFalse(report.boundaries.productionReady)
        assertFalse(report.boundaries.productionApproved)
        assertFalse(report.boundaries.mediaGenerated)
        assertEquals("none", report.boundaries.rendererInvocation)
        assertEquals("none", report.boundaries.providerInvocation)
        assertEquals("none", report.boundaries.ttsInvocation)
        assertFalse(report.boundaries.adapterImplementationExecuted)
        assertFalse(report.boundaries.adapterExecuted)
        assertTrue(report.boundaries.noExternalPaidProviderTools)
        assertFalse(report.boundaries.rendererReady)
        assertFalse(report.boundaries.mp4ReadinessClaimed)
        assertEquals(
            "local_renderer_manifest_draft_json_adapter_dry_run_only_not_renderer_execution",
            report.downstreamBoundary,
        )
        assertEquals("local_renderer_manifest_adapter_contract_plan", report.nextAllowedDownstreamStep)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
}
