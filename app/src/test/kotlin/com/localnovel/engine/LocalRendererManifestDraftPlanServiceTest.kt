package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererAdapterImplementationDraft
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftReviewReport
import com.localnovel.engine.service.LocalRendererManifestDraftPlanService
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class LocalRendererManifestDraftPlanServiceTest {
    @Test
    fun passesRainGateManifestDraftPlan() {
        val draft = validDraft()
        val review = validReview()

        val plan = LocalRendererManifestDraftPlanService().plan(
            draft = draft,
            review = review,
            implementationDraftFile = sampleDraftPath.toString(),
            draftReviewFile = sampleReviewPath.toString(),
            planId = "rain-gate-manifest-draft-plan-001",
        )

        assertEquals("PASS", plan.verdict, plan.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_MANIFEST_DRAFT_PLAN", plan.status)
        assertEquals("rain-gate-manifest-draft-plan-001", plan.planId)
        assertEquals(draft.draftId, plan.sourceDraftId)
        assertEquals(review.reviewId, plan.sourceDraftReviewId)
        assertEquals(draft.sourcePlanId, plan.sourcePlanId)
        assertEquals(draft.sourcePlanReviewId, plan.sourcePlanReviewId)
        assertEquals(draft.sourceDryRunId, plan.sourceDryRunId)
        assertEquals(draft.sourceHandoffId, plan.sourceHandoffId)
        assertEquals(draft.sourceHandoffReviewId, plan.sourceHandoffReviewId)
        assertEquals(draft.sourceStoryInputPackId, plan.sourceStoryInputPackId)
        assertEquals(10, plan.checks.size)
        assertTrue(plan.checks.all { it.status == "PASS" })
        assertTrue(plan.sourceAlignmentSummary.draftIdAligned)
        assertTrue(plan.sourceAlignmentSummary.storyInputPackIdAligned)
        assertEquals(2, plan.manifestFieldPlan.panelManifestPlans.size)
        assertTrue(plan.manifestDraftPlanSummary.mappingDraftFields > 0)
        assertTrue(plan.requiredManifestDraftWorkItems.isNotEmpty())
        assertTrue(plan.verificationPlan.isNotEmpty())
        assertTrue(plan.manifestFieldPlan.planningOnly)
        assertFalse(plan.manifestFieldPlan.manifestGenerated)
        assertFalse(plan.boundaries.mediaGenerated)
        assertEquals("none", plan.boundaries.rendererInvocation)
        assertEquals("none", plan.boundaries.providerInvocation)
        assertEquals("none", plan.boundaries.ttsInvocation)
        assertFalse(plan.boundaries.manifestGenerated)
        assertFalse(plan.boundaries.adapterImplementationExecuted)
        assertTrue(plan.boundaries.noExternalPaidProviderTools)
        assertFalse(plan.boundaries.rendererReady)
        assertFalse(plan.boundaries.mp4ReadinessClaimed)
        assertEquals("local_renderer_manifest_draft_plan_only_not_manifest_generation", plan.downstreamBoundary)
        assertEquals("local_renderer_manifest_draft_plan_review", plan.nextAllowedDownstreamStep)
        assertEquals(0, plan.findingCount)
    }

    @Test
    fun failsWhenDraftReviewStateIsWrong() {
        val review = validReview()

        val failReport = LocalRendererManifestDraftPlanService().plan(
            draft = validDraft(),
            review = review.copy(
                verdict = "FAIL",
                status = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_FAIL",
            ),
            implementationDraftFile = sampleDraftPath.toString(),
            draftReviewFile = sampleReviewPath.toString(),
        )
        val wrongNextStepReport = LocalRendererManifestDraftPlanService().plan(
            draft = validDraft(),
            review = review.copy(nextAllowedDownstreamStep = "wrong-next-step"),
            implementationDraftFile = sampleDraftPath.toString(),
            draftReviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", failReport.verdict)
        assertEquals("LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_FAIL", failReport.status)
        assertEquals("", failReport.nextAllowedDownstreamStep)
        assertTrue(failReport.checks.any { it.checkId == "draft-review-state" && it.status == "FAIL" })

        assertEquals("FAIL", wrongNextStepReport.verdict)
        assertEquals("", wrongNextStepReport.nextAllowedDownstreamStep)
        assertTrue(wrongNextStepReport.checks.any { it.checkId == "draft-review-state" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenMediaGeneratedBoundaryIsForged() {
        val draft = validDraft()
        val report = LocalRendererManifestDraftPlanService().plan(
            draft = draft.copy(boundaries = draft.boundaries.copy(mediaGenerated = true)),
            review = validReview(),
            implementationDraftFile = sampleDraftPath.toString(),
            draftReviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenManifestGeneratedBoundaryIsForged() {
        val draft = validDraft()
        val report = LocalRendererManifestDraftPlanService().plan(
            draft = draft.copy(
                boundaries = draft.boundaries.copy(manifestGenerated = true),
                adapterMappingDraft = draft.adapterMappingDraft.copy(manifestGenerated = true),
            ),
            review = validReview(),
            implementationDraftFile = sampleDraftPath.toString(),
            draftReviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "manifest-remains-plan-only" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenRendererOrMp4ReadinessIsClaimed() {
        val draft = validDraft()
        val report = LocalRendererManifestDraftPlanService().plan(
            draft = draft.copy(
                boundaries = draft.boundaries.copy(
                    rendererReady = true,
                    mp4ReadinessClaimed = true,
                ),
            ),
            review = validReview(),
            implementationDraftFile = sampleDraftPath.toString(),
            draftReviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "no-readiness-overclaim" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenSourceIdsDoNotAlign() {
        val report = LocalRendererManifestDraftPlanService().plan(
            draft = validDraft(),
            review = validReview().copy(sourceDraftId = "wrong-draft-id"),
            implementationDraftFile = sampleDraftPath.toString(),
            draftReviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "source-ids-align" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenMappingVerificationOrWorkItemsAreEmpty() {
        val draft = validDraft()
        val report = LocalRendererManifestDraftPlanService().plan(
            draft = draft.copy(
                implementationTasks = emptyList(),
                adapterMappingDraft = draft.adapterMappingDraft.copy(panelDrafts = emptyList()),
                verificationPlan = emptyList(),
            ),
            review = validReview(),
            implementationDraftFile = sampleDraftPath.toString(),
            draftReviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "required-content-present" && it.status == "FAIL" })
    }

    companion object {
        val sampleDraftPath: Path = locateSample("rain_gate_local_renderer_adapter_implementation_draft_sample.json")
        val sampleReviewPath: Path =
            locateSample("rain_gate_local_renderer_adapter_implementation_draft_review_report_sample.json")

        fun validDraft(): LocalRendererAdapterImplementationDraft =
            JsonSupport.json.decodeFromString(Files.readString(sampleDraftPath).removePrefix("\uFEFF"))

        fun validReview(): LocalRendererAdapterImplementationDraftReviewReport =
            JsonSupport.json.decodeFromString(Files.readString(sampleReviewPath).removePrefix("\uFEFF"))

        private fun locateSample(fileName: String): Path {
            val relativePath = Path.of("samples", "local_story_input", fileName)
            return generateSequence(Path.of("").toAbsolutePath().normalize()) { it.parent }
                .map { it.resolve(relativePath) }
                .firstOrNull { Files.exists(it) }
                ?: relativePath
        }
    }
}
