package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererManifestDraftJsonReviewReport
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class LocalRendererManifestDraftJsonReviewCliTest {
    @Test
    fun cliWritesLocalRendererManifestDraftJsonReviewReport() {
        val tempDir = Files.createTempDirectory("local-renderer-manifest-draft-json-review")
        val outPath = tempDir.resolve("manifest_draft_json_review.json")

        val exit = Cli().run(
            listOf(
                "review-local-renderer-manifest-draft-json",
                "--draft",
                LocalRendererManifestDraftJsonReviewServiceTest.sampleDraftPath.toString(),
                "--review",
                "manifest-draft-json-review-cli-001",
                "--out",
                outPath.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(outPath))
        val report = readJson<LocalRendererManifestDraftJsonReviewReport>(outPath)
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_PASS", report.status)
        assertEquals("manifest-draft-json-review-cli-001", report.reviewId)
        assertEquals("chenxing-local-renderer-manifest-draft-json-draft-v0-001", report.sourceDraftId)
        assertEquals("chenxing-local-renderer-manifest-draft-plan-v0-001", report.sourceManifestDraftPlanId)
        assertEquals("chenxing-local-renderer-manifest-draft-plan-review-v0-001", report.sourceManifestDraftPlanReviewId)
        assertEquals("local-story-input-chenxing-v0-001", report.sourceStoryInputPackId)
        assertTrue(report.checks.isNotEmpty())
        assertTrue(report.checks.all { it.status == "PASS" })
        assertTrue(report.draftManifestReviewSummary.draftManifestGenerated)
        assertFalse(report.draftManifestReviewSummary.productionManifestGenerated)
        assertFalse(report.draftManifestReviewSummary.productionReady)
        assertFalse(report.draftManifestReviewSummary.productionApproved)
        assertTrue(report.boundaries.draftManifestGenerated)
        assertFalse(report.boundaries.productionManifestGenerated)
        assertFalse(report.boundaries.productionReady)
        assertFalse(report.boundaries.productionApproved)
        assertFalse(report.boundaries.mediaGenerated)
        assertEquals("none", report.boundaries.rendererInvocation)
        assertEquals("none", report.boundaries.providerInvocation)
        assertEquals("none", report.boundaries.ttsInvocation)
        assertFalse(report.boundaries.adapterImplementationExecuted)
        assertTrue(report.boundaries.noExternalPaidProviderTools)
        assertFalse(report.boundaries.rendererReady)
        assertFalse(report.boundaries.mp4ReadinessClaimed)
        assertEquals("local_renderer_manifest_draft_json_review_only_not_production_approval", report.downstreamBoundary)
        assertEquals("local_renderer_manifest_draft_json_adapter_dry_run", report.nextAllowedDownstreamStep)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
}
