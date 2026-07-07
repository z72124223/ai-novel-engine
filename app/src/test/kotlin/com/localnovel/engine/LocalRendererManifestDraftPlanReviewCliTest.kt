package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererManifestDraftPlanReviewReport
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class LocalRendererManifestDraftPlanReviewCliTest {
    @Test
    fun cliWritesLocalRendererManifestDraftPlanReviewReport() {
        val tempDir = Files.createTempDirectory("local-renderer-manifest-draft-plan-review")
        val outPath = tempDir.resolve("manifest_draft_plan_review.json")

        val exit = Cli().run(
            listOf(
                "review-local-renderer-manifest-draft-plan",
                "--plan",
                LocalRendererManifestDraftPlanReviewServiceTest.samplePlanPath.toString(),
                "--review",
                "manifest-draft-plan-review-cli-001",
                "--out",
                outPath.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(outPath))
        val report = readJson<LocalRendererManifestDraftPlanReviewReport>(outPath)
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_PASS", report.status)
        assertEquals("manifest-draft-plan-review-cli-001", report.reviewId)
        assertEquals("rain-gate-local-renderer-manifest-draft-plan-v0-001", report.sourceManifestDraftPlanId)
        assertEquals("local-story-input-rain-gate-v0-001", report.sourceStoryInputPackId)
        assertTrue(report.checks.isNotEmpty())
        assertTrue(report.checks.all { it.status == "PASS" })
        assertTrue(report.manifestDraftPlanReadinessSummary.readyForFutureManifestJsonDraft)
        assertFalse(report.manifestDraftPlanReadinessSummary.realManifestGenerated)
        assertFalse(report.boundaries.mediaGenerated)
        assertEquals("none", report.boundaries.rendererInvocation)
        assertEquals("none", report.boundaries.providerInvocation)
        assertEquals("none", report.boundaries.ttsInvocation)
        assertFalse(report.boundaries.manifestGenerated)
        assertFalse(report.boundaries.adapterImplementationExecuted)
        assertTrue(report.boundaries.noExternalPaidProviderTools)
        assertFalse(report.boundaries.rendererReady)
        assertFalse(report.boundaries.mp4ReadinessClaimed)
        assertEquals("local_renderer_manifest_draft_plan_review_only_not_manifest_generation", report.downstreamBoundary)
        assertEquals("local_renderer_manifest_draft_json_draft", report.nextAllowedDownstreamStep)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
}
