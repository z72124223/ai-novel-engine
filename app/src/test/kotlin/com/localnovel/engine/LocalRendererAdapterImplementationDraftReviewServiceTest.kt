package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererAdapterImplementationDraft
import com.localnovel.engine.service.LocalRendererAdapterImplementationDraftReviewService
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class LocalRendererAdapterImplementationDraftReviewServiceTest {
    @Test
    fun passesRainGateImplementationDraftReview() {
        val draft = validDraft()

        val report = LocalRendererAdapterImplementationDraftReviewService().review(
            draft = draft,
            draftFile = sampleDraftPath.toString(),
            reviewId = "chenxing-implementation-draft-review-001",
        )

        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_PASS", report.status)
        assertEquals("chenxing-implementation-draft-review-001", report.reviewId)
        assertEquals(draft.draftId, report.sourceDraftId)
        assertEquals(draft.sourcePlanId, report.sourcePlanId)
        assertEquals(draft.sourcePlanReviewId, report.sourcePlanReviewId)
        assertEquals(draft.sourceDryRunId, report.sourceDryRunId)
        assertEquals(draft.sourceHandoffId, report.sourceHandoffId)
        assertEquals(draft.sourceHandoffReviewId, report.sourceHandoffReviewId)
        assertEquals(draft.sourceStoryInputPackId, report.sourceStoryInputPackId)
        assertEquals(9, report.checks.size)
        assertTrue(report.checks.all { it.status == "PASS" })
        assertEquals(10, report.draftSummary.sourceChecks)
        assertEquals(10, report.draftSummary.sourceChecksPass)
        assertEquals(0, report.draftSummary.sourceChecksFail)
        assertEquals(5, report.draftSummary.implementationTasks)
        assertEquals(3, report.draftSummary.mappingDraftPanels)
        assertTrue(report.draftSummary.mappingDraftFields > 0)
        assertEquals(4, report.draftSummary.verificationSteps)
        assertEquals(0, report.draftSummary.findingCount)
        assertTrue(report.draftSummary.draftOnly)
        assertFalse(report.draftSummary.manifestGenerated)
        assertFalse(report.draftSummary.adapterImplementationExecuted)
        assertFalse(report.draftSummary.rendererReady)
        assertFalse(report.draftSummary.mp4ReadinessClaimed)
        assertTrue(report.boundaries.fileProtocolJsonOnly)
        assertTrue(report.boundaries.noExternalPaidProviderTools)
        assertEquals("none", report.boundaries.providerInvocation)
        assertEquals("none", report.boundaries.ttsInvocation)
        assertFalse(report.boundaries.mediaGenerated)
        assertEquals("none", report.boundaries.rendererInvocation)
        assertFalse(report.boundaries.manifestGenerated)
        assertFalse(report.boundaries.adapterImplementationExecuted)
        assertFalse(report.boundaries.rendererReady)
        assertFalse(report.boundaries.mp4ReadinessClaimed)
        assertFalse(report.rendererCompatibilitySummary.ready)
        assertFalse(report.rendererCompatibilitySummary.mp4ReadinessClaimed)
        assertEquals("local_renderer_adapter_implementation_draft_review_only_not_manifest_generation", report.downstreamBoundary)
        assertEquals("local_renderer_manifest_draft_plan", report.nextAllowedDownstreamStep)
        assertEquals(0, report.findingCount)
    }

    @Test
    fun failsWhenDraftStateIsWrong() {
        val base = validDraft()

        val failReport = LocalRendererAdapterImplementationDraftReviewService().review(
            draft = base.copy(verdict = "FAIL", status = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_FAIL"),
            draftFile = sampleDraftPath.toString(),
        )
        val wrongNextStepReport = LocalRendererAdapterImplementationDraftReviewService().review(
            draft = base.copy(nextAllowedDownstreamStep = "wrong-next-step"),
            draftFile = sampleDraftPath.toString(),
        )

        assertEquals("FAIL", failReport.verdict)
        assertEquals("LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_FAIL", failReport.status)
        assertEquals("", failReport.nextAllowedDownstreamStep)
        assertTrue(failReport.checks.any { it.checkId == "draft-state" && it.status == "FAIL" })
        assertTrue(
            failReport.findings.any {
                it.ruleId == "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_STATE_INVALID"
            },
        )

        assertEquals("FAIL", wrongNextStepReport.verdict)
        assertEquals("", wrongNextStepReport.nextAllowedDownstreamStep)
        assertTrue(wrongNextStepReport.checks.any { it.checkId == "draft-state" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenMediaGeneratedBoundaryIsForged() {
        val base = validDraft()
        val report = LocalRendererAdapterImplementationDraftReviewService().review(
            draft = base.copy(boundaries = base.boundaries.copy(mediaGenerated = true)),
            draftFile = sampleDraftPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenManifestGeneratedBoundaryIsForged() {
        val base = validDraft()
        val report = LocalRendererAdapterImplementationDraftReviewService().review(
            draft = base.copy(
                boundaries = base.boundaries.copy(manifestGenerated = true),
                adapterMappingDraft = base.adapterMappingDraft.copy(manifestGenerated = true),
            ),
            draftFile = sampleDraftPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "mapping-remains-draft-only" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenRendererOrMp4ReadinessIsClaimed() {
        val base = validDraft()
        val report = LocalRendererAdapterImplementationDraftReviewService().review(
            draft = base.copy(
                boundaries = base.boundaries.copy(
                    rendererReady = true,
                    mp4ReadinessClaimed = true,
                ),
            ),
            draftFile = sampleDraftPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "no-readiness-overclaim" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenDraftSectionsAreEmpty() {
        val base = validDraft()
        val report = LocalRendererAdapterImplementationDraftReviewService().review(
            draft = base.copy(
                implementationTasks = emptyList(),
                adapterMappingDraft = base.adapterMappingDraft.copy(panelDrafts = emptyList()),
                verificationPlan = emptyList(),
            ),
            draftFile = sampleDraftPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "draft-sections-present" && it.status == "FAIL" })
    }

    companion object {
        val sampleDraftPath: Path = locateSampleDraft()

        fun validDraft(): LocalRendererAdapterImplementationDraft =
            JsonSupport.json.decodeFromString(Files.readString(sampleDraftPath).removePrefix("\uFEFF"))

        private fun locateSampleDraft(): Path {
            val relativePath = Path.of(
                "samples",
                "chenxing", "pipeline",
                "chenxing_local_renderer_adapter_implementation_draft_sample.json",
            )
            return generateSequence(Path.of("").toAbsolutePath().normalize()) { it.parent }
                .map { it.resolve(relativePath) }
                .firstOrNull { Files.exists(it) }
                ?: relativePath
        }
    }
}
