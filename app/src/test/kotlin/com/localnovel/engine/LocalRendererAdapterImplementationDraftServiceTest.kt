package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanDraft
import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanReviewReport
import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffDraft
import com.localnovel.engine.service.LocalRendererAdapterImplementationDraftService
import com.localnovel.engine.service.LocalRendererAdapterImplementationPlanReviewService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalRendererAdapterImplementationDraftServiceTest {
    @Test
    fun passesAcceptedPlanReviewIntoImplementationDraft() {
        val (handoff, plan, planReview) = validInputs()

        val draft = LocalRendererAdapterImplementationDraftService().draft(
            handoff = handoff,
            plan = plan,
            planReview = planReview,
            handoffFile = "preview_handoff.json",
            planFile = "adapter_implementation_plan.json",
            planReviewFile = "adapter_implementation_plan_review.json",
            draftId = "adapter-implementation-draft-001",
        )

        assertEquals("PASS", draft.verdict, draft.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT", draft.status)
        assertEquals("adapter-implementation-draft-001", draft.draftId)
        assertEquals(plan.planId, draft.sourcePlanId)
        assertEquals(planReview.reviewId, draft.sourcePlanReviewId)
        assertEquals(plan.sourceDryRunId, draft.sourceDryRunId)
        assertEquals(handoff.handoffId, draft.sourceHandoffId)
        assertEquals(plan.sourceReviewId, draft.sourceHandoffReviewId)
        assertEquals(handoff.sourceStoryInputPackId, draft.sourceStoryInputPackId)
        assertEquals(10, draft.checks.size)
        assertTrue(draft.checks.all { it.status == "PASS" })
        assertEquals(2, draft.implementationDraftSummary.previewPanelCount)
        assertEquals(2, draft.implementationDraftSummary.mappingDraftPanelCount)
        assertTrue(draft.implementationDraftSummary.requiredAdapterWorkItems > 0)
        assertTrue(draft.implementationDraftSummary.implementationTaskCount > 0)
        assertTrue(draft.implementationDraftSummary.verificationStepCount > 0)
        assertTrue(draft.implementationDraftSummary.targetMappingNoteCount > 0)
        assertEquals(0, draft.implementationDraftSummary.missingMappingNoteCount)
        assertEquals(9000, draft.implementationDraftSummary.targetDurationMillis)
        assertFalse(draft.implementationDraftSummary.manifestGenerated)
        assertFalse(draft.implementationDraftSummary.adapterImplementationExecuted)
        assertTrue(draft.sourceAlignmentSummary.storyInputPackIdAligned)
        assertTrue(draft.sourceAlignmentSummary.handoffIdAligned)
        assertTrue(draft.sourceAlignmentSummary.handoffReviewIdAligned)
        assertTrue(draft.sourceAlignmentSummary.dryRunIdAligned)
        assertTrue(draft.sourceAlignmentSummary.planIdAligned)
        assertTrue(draft.adapterMappingDraft.draftOnly)
        assertFalse(draft.adapterMappingDraft.manifestGenerated)
        assertEquals("ComicVideoRenderHandoffManifest", draft.adapterMappingDraft.targetManifestType)
        assertEquals(2, draft.adapterMappingDraft.panelDrafts.size)
        assertTrue(draft.adapterMappingDraft.panelDrafts.all { !it.manifestGenerated && !it.rendererReady })
        assertTrue(draft.implementationTasks.isNotEmpty())
        assertTrue(draft.verificationPlan.isNotEmpty())
        assertTrue(draft.boundaries.fileProtocolJsonOnly)
        assertTrue(draft.boundaries.noExternalPaidProviderTools)
        assertEquals("none", draft.boundaries.providerInvocation)
        assertEquals("none", draft.boundaries.ttsInvocation)
        assertFalse(draft.boundaries.mediaGenerated)
        assertEquals("none", draft.boundaries.rendererInvocation)
        assertFalse(draft.boundaries.manifestGenerated)
        assertFalse(draft.boundaries.adapterImplementationExecuted)
        assertFalse(draft.boundaries.rendererReady)
        assertFalse(draft.boundaries.mp4ReadinessClaimed)
        assertEquals("local_renderer_adapter_implementation_draft_only_not_manifest_generation", draft.downstreamBoundary)
        assertEquals("local_renderer_adapter_implementation_draft_review", draft.nextAllowedDownstreamStep)
        assertEquals(0, draft.findingCount)
    }

    @Test
    fun failsWhenPlanReviewIsFail() {
        val (handoff, plan, planReview) = validInputs()

        val draft = LocalRendererAdapterImplementationDraftService().draft(
            handoff = handoff,
            plan = plan,
            planReview = planReview.copy(verdict = "FAIL"),
            handoffFile = "preview_handoff.json",
            planFile = "adapter_implementation_plan.json",
            planReviewFile = "adapter_implementation_plan_review.json",
        )

        assertEquals("FAIL", draft.verdict)
        assertEquals("LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_FAIL", draft.status)
        assertEquals("", draft.nextAllowedDownstreamStep)
        assertTrue(draft.checks.any { it.checkId == "plan-review-state" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenPlanReviewNextStepIsWrong() {
        val (handoff, plan, planReview) = validInputs()

        val draft = LocalRendererAdapterImplementationDraftService().draft(
            handoff = handoff,
            plan = plan,
            planReview = planReview.copy(nextAllowedDownstreamStep = "wrong-next-step"),
            handoffFile = "preview_handoff.json",
            planFile = "adapter_implementation_plan.json",
            planReviewFile = "adapter_implementation_plan_review.json",
        )

        assertEquals("FAIL", draft.verdict)
        assertEquals("", draft.nextAllowedDownstreamStep)
        assertTrue(draft.checks.any { it.checkId == "plan-review-state" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenMediaOrManifestBoundaryIsForged() {
        val (handoff, plan, planReview) = validInputs()
        val unsafePlan = plan.copy(
            boundaries = plan.boundaries.copy(manifestGenerated = true),
            targetManifestFieldMappingPlan = plan.targetManifestFieldMappingPlan.copy(manifestGenerated = true),
        )

        val draft = LocalRendererAdapterImplementationDraftService().draft(
            handoff = handoff.copy(boundaries = handoff.boundaries.copy(mediaGenerated = true)),
            plan = unsafePlan,
            planReview = planReview,
            handoffFile = "preview_handoff.json",
            planFile = "adapter_implementation_plan.json",
            planReviewFile = "adapter_implementation_plan_review.json",
        )

        assertEquals("FAIL", draft.verdict)
        assertEquals("", draft.nextAllowedDownstreamStep)
        assertTrue(draft.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
        assertTrue(draft.checks.any { it.checkId == "mapping-remains-draft-only" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenSourceIdsDoNotAlign() {
        val (handoff, plan, planReview) = validInputs()

        val draft = LocalRendererAdapterImplementationDraftService().draft(
            handoff = handoff,
            plan = plan,
            planReview = planReview.copy(sourceHandoffId = "wrong-handoff-id"),
            handoffFile = "preview_handoff.json",
            planFile = "adapter_implementation_plan.json",
            planReviewFile = "adapter_implementation_plan_review.json",
        )

        assertEquals("FAIL", draft.verdict)
        assertEquals("", draft.nextAllowedDownstreamStep)
        assertTrue(draft.checks.any { it.checkId == "source-ids-align" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenPreviewPanelsOrMappingNotesAreMissing() {
        val (handoff, plan, planReview) = validInputs()
        val emptyPlan = plan.copy(
            targetManifestFieldMappingPlan = plan.targetManifestFieldMappingPlan.copy(
                panelTimelineFields = emptyList(),
                voiceTimelineFields = emptyList(),
                subtitleTimelineFields = emptyList(),
                renderSettingsFields = emptyList(),
                perPanelMappingNotes = emptyList(),
            ),
        )

        val draft = LocalRendererAdapterImplementationDraftService().draft(
            handoff = handoff.copy(previewPlan = handoff.previewPlan.copy(panelOrdering = emptyList())),
            plan = emptyPlan,
            planReview = planReview,
            handoffFile = "preview_handoff.json",
            planFile = "adapter_implementation_plan.json",
            planReviewFile = "adapter_implementation_plan_review.json",
        )

        assertEquals("FAIL", draft.verdict)
        assertEquals("", draft.nextAllowedDownstreamStep)
        assertTrue(draft.checks.any { it.checkId == "required-content-present" && it.status == "FAIL" })
    }

    companion object {
        fun validInputs(): Triple<
            LocalStoryVideoPreviewHandoffDraft,
            LocalRendererAdapterImplementationPlanDraft,
            LocalRendererAdapterImplementationPlanReviewReport,
        > {
            val (handoff, _) = LocalRendererAdapterDryRunServiceTest.validInputs()
            val plan = LocalRendererAdapterImplementationPlanReviewServiceTest.validPlan()
            val planReview = LocalRendererAdapterImplementationPlanReviewService().review(
                plan = plan,
                planFile = "adapter_implementation_plan.json",
                reviewId = "adapter-implementation-plan-review-001",
            )
            return Triple(handoff, plan, planReview)
        }
    }
}
