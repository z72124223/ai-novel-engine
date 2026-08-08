package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanDraft
import com.localnovel.engine.service.LocalRendererAdapterImplementationPlanReviewService
import com.localnovel.engine.service.LocalRendererAdapterImplementationPlanService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalRendererAdapterImplementationPlanReviewServiceTest {
    @Test
    fun passesAcceptedImplementationPlanReview() {
        val plan = validPlan()

        val report = LocalRendererAdapterImplementationPlanReviewService().review(
            plan = plan,
            planFile = "adapter_implementation_plan.json",
            reviewId = "adapter-implementation-plan-review-001",
        )

        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_REVIEW_PASS", report.status)
        assertEquals("adapter-implementation-plan-review-001", report.reviewId)
        assertEquals(plan.planId, report.sourcePlanId)
        assertEquals(plan.sourceDryRunId, report.sourceDryRunId)
        assertEquals(plan.sourceHandoffId, report.sourceHandoffId)
        assertEquals(plan.sourceReviewId, report.sourceReviewId)
        assertEquals(plan.sourceStoryInputPackId, report.sourceStoryInputPackId)
        assertEquals(8, report.checks.size)
        assertTrue(report.checks.all { it.status == "PASS" })
        assertEquals(8, report.planSummary.sourceValidationChecks)
        assertEquals(8, report.planSummary.sourceValidationPass)
        assertTrue(report.planSummary.requiredAdapterWorkItems > 0)
        assertTrue(report.planSummary.targetMappingNoteCount > 0)
        assertTrue(report.planSummary.perPanelMappingNotes > 0)
        assertTrue(report.planSummary.implementationSteps > 0)
        assertTrue(report.planSummary.riskChecks > 0)
        assertTrue(report.planSummary.verificationSteps > 0)
        assertTrue(report.planSummary.planningOnly)
        assertFalse(report.planSummary.targetManifestGenerated)
        assertTrue(report.boundaries.fileProtocolJsonOnly)
        assertTrue(report.boundaries.noExternalPaidProviderTools)
        assertEquals("none", report.boundaries.providerInvocation)
        assertEquals("none", report.boundaries.ttsInvocation)
        assertFalse(report.boundaries.mediaGenerated)
        assertEquals("none", report.boundaries.rendererInvocation)
        assertFalse(report.boundaries.manifestGenerated)
        assertFalse(report.boundaries.adapterImplementationExecuted)
        assertFalse(report.rendererCompatibilitySummary.ready)
        assertFalse(report.rendererCompatibilitySummary.mp4ReadinessClaimed)
        assertTrue(report.rendererCompatibilitySummary.adapterImplementationStillRequired)
        assertEquals("local_renderer_adapter_implementation_plan_review_only_not_implementation", report.downstreamBoundary)
        assertEquals("local_renderer_adapter_implementation_draft", report.nextAllowedDownstreamStep)
        assertEquals(0, report.findingCount)
    }

    @Test
    fun failsWhenPlanVerdictIsFail() {
        val report = LocalRendererAdapterImplementationPlanReviewService().review(
            plan = validPlan().copy(verdict = "FAIL"),
            planFile = "adapter_implementation_plan.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_REVIEW_FAIL", report.status)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "plan-state" && it.status == "FAIL" })
        assertTrue(report.findings.any { it.ruleId == "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_REVIEW_STATE_INVALID" })
    }

    @Test
    fun failsWhenNextAllowedStepIsWrong() {
        val report = LocalRendererAdapterImplementationPlanReviewService().review(
            plan = validPlan().copy(nextAllowedDownstreamStep = "wrong-next-step"),
            planFile = "adapter_implementation_plan.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "plan-state" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenMediaOrManifestBoundaryIsForged() {
        val base = validPlan()
        val unsafe = base.copy(
            boundaries = base.boundaries.copy(
                mediaGenerated = true,
                manifestGenerated = true,
            ),
            targetManifestFieldMappingPlan = base.targetManifestFieldMappingPlan.copy(
                manifestGenerated = true,
            ),
        )

        val report = LocalRendererAdapterImplementationPlanReviewService().review(
            plan = unsafe,
            planFile = "adapter_implementation_plan.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "target-mapping-planning-only" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenWorkItemsOrPlanningNotesAreMissing() {
        val base = validPlan()
        val incomplete = base.copy(
            requiredAdapterWorkItems = emptyList(),
            targetManifestFieldMappingPlan = base.targetManifestFieldMappingPlan.copy(
                panelTimelineFields = emptyList(),
                voiceTimelineFields = emptyList(),
                subtitleTimelineFields = emptyList(),
                renderSettingsFields = emptyList(),
                perPanelMappingNotes = emptyList(),
            ),
        )

        val report = LocalRendererAdapterImplementationPlanReviewService().review(
            plan = incomplete,
            planFile = "adapter_implementation_plan.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "work-items-present" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "target-mapping-planning-only" && it.status == "FAIL" })
    }

    companion object {
        fun validPlan(): LocalRendererAdapterImplementationPlanDraft =
            LocalRendererAdapterImplementationPlanService().plan(
                dryRunReport = LocalRendererAdapterImplementationPlanServiceTest.validDryRunReport(),
                dryRunReportFile = "adapter_dry_run.json",
                planId = "adapter-plan-001",
            )
    }
}
