package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererAdapterDryRunReport
import com.localnovel.engine.service.LocalRendererAdapterDryRunService
import com.localnovel.engine.service.LocalRendererAdapterImplementationPlanService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalRendererAdapterImplementationPlanServiceTest {
    @Test
    fun passesAcceptedDryRunReportIntoImplementationPlanDraft() {
        val dryRunReport = validDryRunReport()

        val plan = LocalRendererAdapterImplementationPlanService().plan(
            dryRunReport = dryRunReport,
            dryRunReportFile = "adapter_dry_run.json",
            planId = "adapter-plan-001",
        )

        assertEquals("PASS", plan.verdict, plan.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_DRAFT", plan.status)
        assertEquals("adapter-plan-001", plan.planId)
        assertEquals(dryRunReport.dryRunId, plan.sourceDryRunId)
        assertEquals(dryRunReport.sourceHandoffId, plan.sourceHandoffId)
        assertEquals(dryRunReport.sourceReviewId, plan.sourceReviewId)
        assertEquals(dryRunReport.sourceStoryInputPackId, plan.sourceStoryInputPackId)
        assertEquals(8, plan.sourceValidationChecks.size)
        assertTrue(plan.sourceValidationChecks.all { it.status == "PASS" })
        assertTrue(plan.requiredAdapterWorkItems.isNotEmpty())
        assertTrue(plan.implementationSteps.isNotEmpty())
        assertTrue(plan.riskChecklist.isNotEmpty())
        assertTrue(plan.verificationPlan.isNotEmpty())
        assertTrue(plan.targetManifestFieldMappingPlan.planningOnly)
        assertFalse(plan.targetManifestFieldMappingPlan.manifestGenerated)
        assertEquals(3, plan.targetManifestFieldMappingPlan.perPanelMappingNotes.size)
        assertTrue(plan.targetManifestFieldMappingPlan.perPanelMappingNotes.all { !it.manifestGenerated })
        assertTrue(plan.boundaries.fileProtocolJsonOnly)
        assertTrue(plan.boundaries.noExternalPaidProviderTools)
        assertEquals("none", plan.boundaries.providerInvocation)
        assertEquals("none", plan.boundaries.ttsInvocation)
        assertFalse(plan.boundaries.mediaGenerated)
        assertEquals("none", plan.boundaries.rendererInvocation)
        assertFalse(plan.boundaries.adapterImplementationExecuted)
        assertFalse(plan.boundaries.manifestGenerated)
        assertEquals("local_renderer_adapter_implementation_plan_only_not_implementation", plan.downstreamBoundary)
        assertEquals("local_renderer_adapter_implementation_review", plan.nextAllowedDownstreamStep)
        assertEquals(0, plan.findingCount)
    }

    @Test
    fun failsWhenDryRunReportIsFail() {
        val dryRunReport = validDryRunReport().copy(verdict = "FAIL")

        val plan = LocalRendererAdapterImplementationPlanService().plan(
            dryRunReport = dryRunReport,
            dryRunReportFile = "adapter_dry_run.json",
        )

        assertEquals("FAIL", plan.verdict)
        assertEquals("LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_FAIL", plan.status)
        assertEquals("", plan.nextAllowedDownstreamStep)
        assertTrue(plan.sourceValidationChecks.any { it.checkId == "phase7-state" && it.status == "FAIL" })
        assertTrue(plan.findings.any { it.ruleId == "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_PHASE7_STATE_INVALID" })
    }

    @Test
    fun failsWhenNextAllowedStepIsWrong() {
        val dryRunReport = validDryRunReport().copy(nextAllowedDownstreamStep = "wrong-next-step")

        val plan = LocalRendererAdapterImplementationPlanService().plan(
            dryRunReport = dryRunReport,
            dryRunReportFile = "adapter_dry_run.json",
        )

        assertEquals("FAIL", plan.verdict)
        assertEquals("", plan.nextAllowedDownstreamStep)
        assertTrue(plan.sourceValidationChecks.any { it.checkId == "phase7-state" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenMediaOrManifestBoundaryIsForged() {
        val dryRunReport = validDryRunReport().copy(
            boundaries = validDryRunReport().boundaries.copy(mediaGenerated = true),
            rendererCompatibilitySummary = validDryRunReport().rendererCompatibilitySummary.copy(manifestGenerated = true),
        )

        val plan = LocalRendererAdapterImplementationPlanService().plan(
            dryRunReport = dryRunReport,
            dryRunReportFile = "adapter_dry_run.json",
        )

        assertEquals("FAIL", plan.verdict)
        assertEquals("", plan.nextAllowedDownstreamStep)
        assertTrue(plan.sourceValidationChecks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
        assertTrue(plan.findings.any { it.ruleId == "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_BOUNDARY_FAIL" })
    }

    @Test
    fun failsWhenMappedPanelsAreMissing() {
        val base = validDryRunReport()
        val dryRunReport = base.copy(
            adapterMappingSummary = base.adapterMappingSummary.copy(
                mappedPanelCount = 0,
                unmappedPanelCount = base.adapterMappingSummary.panelCount,
            ),
            dryRunMappingPlan = emptyList(),
        )

        val plan = LocalRendererAdapterImplementationPlanService().plan(
            dryRunReport = dryRunReport,
            dryRunReportFile = "adapter_dry_run.json",
        )

        assertEquals("FAIL", plan.verdict)
        assertEquals("", plan.nextAllowedDownstreamStep)
        assertTrue(plan.sourceValidationChecks.any { it.checkId == "mapping-summary-pass" && it.status == "FAIL" })
        assertTrue(plan.sourceValidationChecks.any { it.checkId == "mapping-notes-planning-only" && it.status == "FAIL" })
    }

    companion object {
        fun validDryRunReport(): LocalRendererAdapterDryRunReport {
            val (handoff, review) = LocalRendererAdapterDryRunServiceTest.validInputs()
            return LocalRendererAdapterDryRunService().dryRun(
                handoff = handoff,
                review = review,
                handoffFile = "preview_handoff.json",
                reviewFile = "preview_handoff_review.json",
                dryRunId = "adapter-dry-run-001",
            )
        }
    }
}
