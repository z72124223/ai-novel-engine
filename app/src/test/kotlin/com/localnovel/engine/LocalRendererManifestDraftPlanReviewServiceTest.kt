package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererManifestDraftPlan
import com.localnovel.engine.model.LocalRendererManifestDraftPlanFinding
import com.localnovel.engine.service.LocalRendererManifestDraftPlanReviewService
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class LocalRendererManifestDraftPlanReviewServiceTest {
    @Test
    fun passesRainGateManifestDraftPlanReview() {
        val plan = validPlan()

        val report = LocalRendererManifestDraftPlanReviewService().review(
            plan = plan,
            planFile = samplePlanPath.toString(),
            reviewId = "chenxing-manifest-draft-plan-review-001",
        )

        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_PASS", report.status)
        assertEquals("chenxing-manifest-draft-plan-review-001", report.reviewId)
        assertEquals(plan.planId, report.sourceManifestDraftPlanId)
        assertEquals(plan.sourceDraftId, report.sourceImplementationDraftId)
        assertEquals(plan.sourceDraftReviewId, report.sourceImplementationDraftReviewId)
        assertEquals(plan.sourceStoryInputPackId, report.sourceStoryInputPackId)
        assertEquals(10, report.checks.size)
        assertTrue(report.checks.all { it.status == "PASS" })
        assertTrue(report.sourceAlignmentSummary.manifestDraftPlanIdPresent)
        assertTrue(report.sourceAlignmentSummary.sourceAlignmentFromPlanPass)
        assertEquals(3, report.planReviewSummary.manifestPanels)
        assertTrue(report.planReviewSummary.manifestFields > 0)
        assertEquals(5, report.planReviewSummary.requiredManifestDraftWorkItems)
        assertTrue(report.planReviewSummary.verificationSteps > 0)
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
        assertEquals(0, report.findingCount)
    }

    @Test
    fun failsWhenPlanStateIsWrong() {
        val base = validPlan()

        val failReport = LocalRendererManifestDraftPlanReviewService().review(
            plan = base.copy(verdict = "FAIL", status = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_FAIL"),
            planFile = samplePlanPath.toString(),
        )
        val wrongStatusReport = LocalRendererManifestDraftPlanReviewService().review(
            plan = base.copy(status = "WRONG_STATUS"),
            planFile = samplePlanPath.toString(),
        )

        assertEquals("FAIL", failReport.verdict)
        assertEquals("LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_FAIL", failReport.status)
        assertEquals("", failReport.nextAllowedDownstreamStep)
        assertTrue(failReport.checks.any { it.checkId == "plan-state" && it.status == "FAIL" })

        assertEquals("FAIL", wrongStatusReport.verdict)
        assertEquals("", wrongStatusReport.nextAllowedDownstreamStep)
        assertTrue(wrongStatusReport.checks.any { it.checkId == "plan-state" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenNextStepIsWrong() {
        val report = LocalRendererManifestDraftPlanReviewService().review(
            plan = validPlan().copy(nextAllowedDownstreamStep = "wrong-next-step"),
            planFile = samplePlanPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "plan-state" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenMediaGeneratedBoundaryIsForged() {
        val plan = validPlan()
        val report = LocalRendererManifestDraftPlanReviewService().review(
            plan = plan.copy(boundaries = plan.boundaries.copy(mediaGenerated = true)),
            planFile = samplePlanPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenManifestGeneratedBoundaryIsForged() {
        val plan = validPlan()
        val report = LocalRendererManifestDraftPlanReviewService().review(
            plan = plan.copy(
                boundaries = plan.boundaries.copy(manifestGenerated = true),
                manifestFieldPlan = plan.manifestFieldPlan.copy(manifestGenerated = true),
            ),
            planFile = samplePlanPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "manifest-remains-plan-only" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenRendererOrMp4ReadinessIsClaimed() {
        val plan = validPlan()
        val report = LocalRendererManifestDraftPlanReviewService().review(
            plan = plan.copy(
                boundaries = plan.boundaries.copy(
                    rendererReady = true,
                    mp4ReadinessClaimed = true,
                ),
            ),
            planFile = samplePlanPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "no-readiness-overclaim" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenFindingsArePresent() {
        val plan = validPlan()
        val report = LocalRendererManifestDraftPlanReviewService().review(
            plan = plan.copy(
                findingCount = 1,
                findings = listOf(
                    LocalRendererManifestDraftPlanFinding(
                        ruleId = "TEST_FINDING",
                        category = "test",
                        severity = "CRITICAL",
                        path = "$.test",
                        message = "Injected finding.",
                    ),
                ),
            ),
            planFile = samplePlanPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "no-findings" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenPlanSectionsAreEmpty() {
        val plan = validPlan()
        val report = LocalRendererManifestDraftPlanReviewService().review(
            plan = plan.copy(
                manifestFieldPlan = plan.manifestFieldPlan.copy(panelManifestPlans = emptyList()),
                requiredManifestDraftWorkItems = emptyList(),
                verificationPlan = emptyList(),
            ),
            planFile = samplePlanPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "plan-sections-present" && it.status == "FAIL" })
    }

    companion object {
        val samplePlanPath: Path = locateSample("chenxing_local_renderer_manifest_draft_plan_sample.json")

        fun validPlan(): LocalRendererManifestDraftPlan =
            JsonSupport.json.decodeFromString(Files.readString(samplePlanPath).removePrefix("\uFEFF"))

        private fun locateSample(fileName: String): Path {
            val relativePath = Path.of("samples", "chenxing", "pipeline", fileName)
            return generateSequence(Path.of("").toAbsolutePath().normalize()) { it.parent }
                .map { it.resolve(relativePath) }
                .firstOrNull { Files.exists(it) }
                ?: relativePath
        }
    }
}
