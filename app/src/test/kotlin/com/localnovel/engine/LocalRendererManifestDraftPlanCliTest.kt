package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererManifestDraftPlan
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class LocalRendererManifestDraftPlanCliTest {
    @Test
    fun cliWritesLocalRendererManifestDraftPlan() {
        val tempDir = Files.createTempDirectory("local-renderer-manifest-draft-plan")
        val outPath = tempDir.resolve("manifest_draft_plan.json")

        val exit = Cli().run(
            listOf(
                "plan-local-renderer-manifest-draft",
                "--implementation-draft",
                LocalRendererManifestDraftPlanServiceTest.sampleDraftPath.toString(),
                "--draft-review",
                LocalRendererManifestDraftPlanServiceTest.sampleReviewPath.toString(),
                "--plan",
                "manifest-draft-plan-cli-001",
                "--out",
                outPath.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(outPath))
        val plan = readJson<LocalRendererManifestDraftPlan>(outPath)
        assertEquals("PASS", plan.verdict, plan.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_MANIFEST_DRAFT_PLAN", plan.status)
        assertEquals("manifest-draft-plan-cli-001", plan.planId)
        assertEquals("chenxing-local-renderer-adapter-implementation-draft-v0-001", plan.sourceDraftId)
        assertEquals(
            "chenxing-local-renderer-adapter-implementation-draft-review-v0-001",
            plan.sourceDraftReviewId,
        )
        assertTrue(plan.checks.isNotEmpty())
        assertTrue(plan.checks.all { it.status == "PASS" })
        assertTrue(plan.manifestFieldPlan.planningOnly)
        assertFalse(plan.manifestFieldPlan.manifestGenerated)
        assertTrue(plan.manifestFieldPlan.panelManifestPlans.isNotEmpty())
        assertTrue(plan.requiredManifestDraftWorkItems.isNotEmpty())
        assertTrue(plan.verificationPlan.isNotEmpty())
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
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
}
