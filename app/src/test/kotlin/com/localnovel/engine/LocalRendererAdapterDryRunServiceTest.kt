package com.localnovel.engine

import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffDraft
import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffReviewReport
import com.localnovel.engine.service.LocalRendererAdapterDryRunService
import com.localnovel.engine.service.LocalStoryVideoPreviewHandoffReviewService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalRendererAdapterDryRunServiceTest {
    @Test
    fun passesReviewedPreviewHandoffDryRun() {
        val (handoff, review) = validInputs()

        val report = LocalRendererAdapterDryRunService().dryRun(
            handoff = handoff,
            review = review,
            handoffFile = "preview_handoff.json",
            reviewFile = "preview_handoff_review.json",
            dryRunId = "adapter-dry-run-001",
        )

        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_ADAPTER_DRY_RUN_PASS", report.status)
        assertEquals("adapter-dry-run-001", report.dryRunId)
        assertEquals(handoff.handoffId, report.sourceHandoffId)
        assertEquals(review.reviewId, report.sourceReviewId)
        assertEquals(handoff.sourceStoryInputPackId, report.sourceStoryInputPackId)
        assertEquals(9, report.checks.size)
        assertTrue(report.checks.all { it.status == "PASS" })
        assertEquals(1, report.adapterMappingSummary.sceneCount)
        assertEquals(3, report.adapterMappingSummary.panelCount)
        assertEquals(9000, report.adapterMappingSummary.targetDurationMillis)
        assertEquals(3, report.adapterMappingSummary.mappedPanelCount)
        assertEquals(0, report.adapterMappingSummary.unmappedPanelCount)
        assertEquals(0, report.adapterMappingSummary.missingRequiredFieldCount)
        assertEquals(3, report.adapterMappingSummary.visualPromptReferenceCount)
        assertEquals(3, report.adapterMappingSummary.audioCueReferenceCount)
        assertEquals(7, report.adapterMappingSummary.qaChecks)
        assertEquals(0, report.adapterMappingSummary.qaFail)
        assertEquals(3, report.dryRunMappingPlan.size)
        assertTrue(report.dryRunMappingPlan.all { it.canMapForFutureAdapter })
        assertTrue(report.dryRunMappingPlan.all { !it.manifestGenerated })
        assertFalse(report.rendererCompatibilitySummary.ready)
        assertTrue(report.rendererCompatibilitySummary.adapterImplementationRequired)
        assertFalse(report.rendererCompatibilitySummary.manifestGenerated)
        assertTrue(report.boundaries.fileProtocolJsonOnly)
        assertTrue(report.boundaries.noExternalPaidProviderTools)
        assertEquals("none", report.boundaries.providerInvocation)
        assertEquals("none", report.boundaries.ttsInvocation)
        assertFalse(report.boundaries.mediaGenerated)
        assertEquals("none", report.boundaries.rendererInvocation)
        assertEquals("local_renderer_adapter_dry_run_only_not_media_generation", report.downstreamBoundary)
        assertEquals("local_renderer_adapter_implementation_plan", report.nextAllowedDownstreamStep)
        assertEquals(0, report.findingCount)
    }

    @Test
    fun failsWhenReviewVerdictIsFail() {
        val (handoff, review) = validInputs()

        val report = LocalRendererAdapterDryRunService().dryRun(
            handoff = handoff,
            review = review.copy(verdict = "FAIL"),
            handoffFile = "preview_handoff.json",
            reviewFile = "preview_handoff_review.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("LOCAL_RENDERER_ADAPTER_DRY_RUN_FAIL", report.status)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "review-state" && it.status == "FAIL" })
        assertTrue(report.findings.any { it.ruleId == "LOCAL_RENDERER_ADAPTER_DRY_RUN_REVIEW_STATE_INVALID" })
    }

    @Test
    fun failsWhenHandoffAndReviewIdsDoNotMatch() {
        val (handoff, review) = validInputs()

        val report = LocalRendererAdapterDryRunService().dryRun(
            handoff = handoff,
            review = review.copy(sourceHandoffId = "wrong-handoff-id"),
            handoffFile = "preview_handoff.json",
            reviewFile = "preview_handoff_review.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "source-id-match" && it.status == "FAIL" })
        assertTrue(report.findings.any { it.ruleId == "LOCAL_RENDERER_ADAPTER_DRY_RUN_SOURCE_ID_MISMATCH" })
    }

    @Test
    fun failsWhenMediaGeneratedBoundaryIsForged() {
        val (handoff, review) = validInputs()

        val report = LocalRendererAdapterDryRunService().dryRun(
            handoff = handoff.copy(boundaries = handoff.boundaries.copy(mediaGenerated = true)),
            review = review,
            handoffFile = "preview_handoff.json",
            reviewFile = "preview_handoff_review.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
        assertTrue(report.findings.any { it.ruleId == "LOCAL_RENDERER_ADAPTER_DRY_RUN_BOUNDARY_FAIL" })
    }

    companion object {
        fun validInputs(): Pair<LocalStoryVideoPreviewHandoffDraft, LocalStoryVideoPreviewHandoffReviewReport> {
            val handoff = LocalStoryVideoPreviewHandoffReviewServiceTest.validHandoff()
            val review = LocalStoryVideoPreviewHandoffReviewService().review(
                handoff = handoff,
                handoffFile = "preview_handoff.json",
                reviewId = "preview-handoff-review-001",
            )
            return handoff to review
        }
    }
}
