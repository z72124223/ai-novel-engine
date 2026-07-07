package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererManifestDraftJsonDraft
import com.localnovel.engine.model.LocalRendererManifestDraftPlan
import com.localnovel.engine.model.LocalRendererManifestDraftPlanFinding
import com.localnovel.engine.model.LocalRendererManifestDraftPlanReviewFinding
import com.localnovel.engine.model.LocalRendererManifestDraftPlanReviewReport
import com.localnovel.engine.service.LocalRendererManifestDraftJsonDraftService
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class LocalRendererManifestDraftJsonDraftServiceTest {
    @Test
    fun passesRainGateManifestDraftJsonDraft() {
        val plan = validPlan()
        val review = validReview()

        val draft = LocalRendererManifestDraftJsonDraftService().draft(
            plan = plan,
            review = review,
            planFile = samplePlanPath.toString(),
            planReviewFile = sampleReviewPath.toString(),
            draftId = "rain-gate-manifest-draft-json-draft-001",
        )

        assertEquals("PASS", draft.verdict, draft.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT", draft.status)
        assertEquals("rain-gate-manifest-draft-json-draft-001", draft.draftId)
        assertEquals(plan.planId, draft.sourceManifestDraftPlanId)
        assertEquals(review.reviewId, draft.sourceManifestDraftPlanReviewId)
        assertEquals(plan.sourceDraftId, draft.sourceImplementationDraftId)
        assertEquals(plan.sourceDraftReviewId, draft.sourceImplementationDraftReviewId)
        assertEquals(plan.sourceStoryInputPackId, draft.sourceStoryInputPackId)
        assertEquals(10, draft.checks.size)
        assertTrue(draft.checks.all { it.status == "PASS" })
        assertTrue(draft.sourceAlignmentSummary.manifestDraftPlanIdAligned)
        assertTrue(draft.sourceAlignmentSummary.sourceStoryInputPackIdAligned)
        assertEquals(2, draft.draftPanelManifestEntries.size)
        assertTrue(draft.draftFieldCoverageSummary.totalDraftFields > 0)
        assertEquals(0, draft.draftFieldCoverageSummary.missingRequiredDraftFields)
        assertTrue(draft.draftManifestSummary.draftOnly)
        assertTrue(draft.draftManifestSummary.draftManifestGenerated)
        assertFalse(draft.draftManifestSummary.productionManifestGenerated)
        assertFalse(draft.draftManifestSummary.productionReady)
        assertTrue(draft.boundaries.draftManifestGenerated)
        assertFalse(draft.boundaries.productionManifestGenerated)
        assertFalse(draft.boundaries.productionReady)
        assertFalse(draft.boundaries.mediaGenerated)
        assertEquals("none", draft.boundaries.rendererInvocation)
        assertEquals("none", draft.boundaries.providerInvocation)
        assertEquals("none", draft.boundaries.ttsInvocation)
        assertFalse(draft.boundaries.adapterImplementationExecuted)
        assertTrue(draft.boundaries.noExternalPaidProviderTools)
        assertFalse(draft.boundaries.rendererReady)
        assertFalse(draft.boundaries.mp4ReadinessClaimed)
        assertEquals("local_renderer_manifest_draft_json_draft_only_not_production_manifest", draft.downstreamBoundary)
        assertEquals("local_renderer_manifest_draft_json_review", draft.nextAllowedDownstreamStep)
        assertEquals(0, draft.findingCount)
    }

    @Test
    fun failsWhenPlanStateIsWrong() {
        val report = LocalRendererManifestDraftJsonDraftService().draft(
            plan = validPlan().copy(status = "WRONG_STATUS"),
            review = validReview(),
            planFile = samplePlanPath.toString(),
            planReviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT_FAIL", report.status)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertFalse(report.boundaries.draftManifestGenerated)
        assertTrue(report.checks.any { it.checkId == "plan-state" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenPlanReviewStateIsWrong() {
        val failReport = LocalRendererManifestDraftJsonDraftService().draft(
            plan = validPlan(),
            review = validReview().copy(
                verdict = "FAIL",
                status = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_FAIL",
            ),
            planFile = samplePlanPath.toString(),
            planReviewFile = sampleReviewPath.toString(),
        )
        val wrongNextReport = LocalRendererManifestDraftJsonDraftService().draft(
            plan = validPlan(),
            review = validReview().copy(nextAllowedDownstreamStep = "wrong-next-step"),
            planFile = samplePlanPath.toString(),
            planReviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", failReport.verdict)
        assertTrue(failReport.checks.any { it.checkId == "plan-review-state" && it.status == "FAIL" })
        assertEquals("FAIL", wrongNextReport.verdict)
        assertEquals("", wrongNextReport.nextAllowedDownstreamStep)
        assertTrue(wrongNextReport.checks.any { it.checkId == "plan-review-state" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenMediaGeneratedBoundaryIsForged() {
        val plan = validPlan()
        val report = LocalRendererManifestDraftJsonDraftService().draft(
            plan = plan.copy(boundaries = plan.boundaries.copy(mediaGenerated = true)),
            review = validReview(),
            planFile = samplePlanPath.toString(),
            planReviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenProductionManifestIsClaimed() {
        val plan = validPlan()
        val review = validReview()
        val report = LocalRendererManifestDraftJsonDraftService().draft(
            plan = plan.copy(
                manifestFieldPlan = plan.manifestFieldPlan.copy(manifestGenerated = true),
            ),
            review = review.copy(
                manifestDraftPlanReadinessSummary = review.manifestDraftPlanReadinessSummary.copy(
                    realManifestGenerated = true,
                ),
            ),
            planFile = samplePlanPath.toString(),
            planReviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "draft-only-not-production-manifest" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenRendererOrMp4ReadinessIsClaimed() {
        val plan = validPlan()
        val report = LocalRendererManifestDraftJsonDraftService().draft(
            plan = plan.copy(
                boundaries = plan.boundaries.copy(
                    rendererReady = true,
                    mp4ReadinessClaimed = true,
                ),
            ),
            review = validReview(),
            planFile = samplePlanPath.toString(),
            planReviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "no-readiness-overclaim" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenFindingsArePresent() {
        val plan = validPlan()
        val review = validReview()
        val report = LocalRendererManifestDraftJsonDraftService().draft(
            plan = plan.copy(
                findingCount = 1,
                findings = listOf(
                    LocalRendererManifestDraftPlanFinding(
                        ruleId = "TEST_PLAN_FINDING",
                        category = "test",
                        severity = "CRITICAL",
                        path = "$.test",
                        message = "Injected plan finding.",
                    ),
                ),
            ),
            review = review.copy(
                findingCount = 1,
                findings = listOf(
                    LocalRendererManifestDraftPlanReviewFinding(
                        ruleId = "TEST_REVIEW_FINDING",
                        category = "test",
                        severity = "CRITICAL",
                        path = "$.test",
                        message = "Injected review finding.",
                    ),
                ),
            ),
            planFile = samplePlanPath.toString(),
            planReviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "no-findings-in-sources" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenSourceIdsDoNotAlign() {
        val report = LocalRendererManifestDraftJsonDraftService().draft(
            plan = validPlan(),
            review = validReview().copy(sourceManifestDraftPlanId = "wrong-plan-id"),
            planFile = samplePlanPath.toString(),
            planReviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "source-ids-align" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenDraftContentIsMissing() {
        val plan = validPlan()
        val report = LocalRendererManifestDraftJsonDraftService().draft(
            plan = plan.copy(
                manifestFieldPlan = plan.manifestFieldPlan.copy(panelManifestPlans = emptyList()),
                requiredManifestDraftWorkItems = emptyList(),
                verificationPlan = emptyList(),
            ),
            review = validReview(),
            planFile = samplePlanPath.toString(),
            planReviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "draft-content-present" && it.status == "FAIL" })
    }

    companion object {
        val samplePlanPath: Path = locateSample("rain_gate_local_renderer_manifest_draft_plan_sample.json")
        val sampleReviewPath: Path =
            locateSample("rain_gate_local_renderer_manifest_draft_plan_review_report_sample.json")

        fun validPlan(): LocalRendererManifestDraftPlan =
            readJson(samplePlanPath)

        fun validReview(): LocalRendererManifestDraftPlanReviewReport =
            readJson(sampleReviewPath)

        private inline fun <reified T> readJson(path: Path): T =
            JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

        private fun locateSample(fileName: String): Path {
            val relativePath = Path.of("samples", "local_story_input", fileName)
            return generateSequence(Path.of("").toAbsolutePath().normalize()) { it.parent }
                .map { it.resolve(relativePath) }
                .firstOrNull { Files.exists(it) }
                ?: relativePath
        }
    }
}
