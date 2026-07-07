package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanReviewReport
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalRendererAdapterImplementationPlanReviewCliTest {
    @Test
    fun cliWritesLocalRendererAdapterImplementationPlanReviewReport() {
        val tempDir = Files.createTempDirectory("local-renderer-adapter-implementation-plan-review")
        val planPath = tempDir.resolve("adapter_implementation_plan.json")
        val reviewPath = tempDir.resolve("adapter_implementation_plan_review.json")
        val plan = LocalRendererAdapterImplementationPlanReviewServiceTest.validPlan()
        writeJson(planPath, plan)

        val exit = Cli().run(
            listOf(
                "review-local-renderer-adapter-implementation-plan",
                "--plan",
                planPath.toString(),
                "--review",
                "adapter-implementation-plan-review-cli-001",
                "--out",
                reviewPath.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(reviewPath))
        val report = readJson<LocalRendererAdapterImplementationPlanReviewReport>(reviewPath)
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_REVIEW_PASS", report.status)
        assertEquals("adapter-implementation-plan-review-cli-001", report.reviewId)
        assertEquals(plan.planId, report.sourcePlanId)
        assertEquals(plan.sourceDryRunId, report.sourceDryRunId)
        assertEquals(plan.sourceHandoffId, report.sourceHandoffId)
        assertEquals(plan.sourceReviewId, report.sourceReviewId)
        assertEquals(plan.sourceStoryInputPackId, report.sourceStoryInputPackId)
        assertTrue(report.planSummary.requiredAdapterWorkItems > 0)
        assertTrue(report.planSummary.targetMappingNoteCount > 0)
        assertTrue(report.planSummary.planningOnly)
        assertFalse(report.boundaries.mediaGenerated)
        assertEquals("none", report.boundaries.rendererInvocation)
        assertEquals("none", report.boundaries.providerInvocation)
        assertEquals("none", report.boundaries.ttsInvocation)
        assertFalse(report.boundaries.manifestGenerated)
        assertFalse(report.boundaries.adapterImplementationExecuted)
        assertTrue(report.boundaries.noExternalPaidProviderTools)
        assertFalse(report.rendererCompatibilitySummary.ready)
        assertFalse(report.rendererCompatibilitySummary.mp4ReadinessClaimed)
        assertEquals("local_renderer_adapter_implementation_plan_review_only_not_implementation", report.downstreamBoundary)
        assertEquals("local_renderer_adapter_implementation_draft", report.nextAllowedDownstreamStep)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
