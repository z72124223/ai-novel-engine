package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanDraft
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalRendererAdapterImplementationPlanCliTest {
    @Test
    fun cliWritesLocalRendererAdapterImplementationPlanDraft() {
        val tempDir = Files.createTempDirectory("local-renderer-adapter-implementation-plan")
        val dryRunReportPath = tempDir.resolve("adapter_dry_run.json")
        val planPath = tempDir.resolve("adapter_implementation_plan.json")
        val dryRunReport = LocalRendererAdapterImplementationPlanServiceTest.validDryRunReport()
        writeJson(dryRunReportPath, dryRunReport)

        val exit = Cli().run(
            listOf(
                "plan-local-renderer-adapter-implementation",
                "--dry-run-report",
                dryRunReportPath.toString(),
                "--plan",
                "adapter-plan-cli-001",
                "--out",
                planPath.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(planPath))
        val plan = readJson<LocalRendererAdapterImplementationPlanDraft>(planPath)
        assertEquals("PASS", plan.verdict, plan.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_DRAFT", plan.status)
        assertEquals("adapter-plan-cli-001", plan.planId)
        assertEquals(dryRunReport.dryRunId, plan.sourceDryRunId)
        assertEquals(dryRunReport.sourceHandoffId, plan.sourceHandoffId)
        assertEquals(dryRunReport.sourceReviewId, plan.sourceReviewId)
        assertEquals(dryRunReport.sourceStoryInputPackId, plan.sourceStoryInputPackId)
        assertTrue(plan.requiredAdapterWorkItems.isNotEmpty())
        assertTrue(plan.targetManifestFieldMappingPlan.planningOnly)
        assertFalse(plan.targetManifestFieldMappingPlan.manifestGenerated)
        assertFalse(plan.boundaries.mediaGenerated)
        assertEquals("none", plan.boundaries.rendererInvocation)
        assertEquals("none", plan.boundaries.providerInvocation)
        assertEquals("none", plan.boundaries.ttsInvocation)
        assertFalse(plan.boundaries.manifestGenerated)
        assertFalse(plan.boundaries.adapterImplementationExecuted)
        assertTrue(plan.boundaries.noExternalPaidProviderTools)
        assertEquals("local_renderer_adapter_implementation_plan_only_not_implementation", plan.downstreamBoundary)
        assertEquals("local_renderer_adapter_implementation_review", plan.nextAllowedDownstreamStep)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
