package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlan
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class LocalRendererManifestAdapterContractPlanCliTest {
    @Test
    fun cliWritesLocalRendererManifestAdapterContractPlan() {
        val tempDir = Files.createTempDirectory("local-renderer-manifest-adapter-contract-plan")
        val outPath = tempDir.resolve("manifest_adapter_contract_plan.json")

        val exit = Cli().run(
            listOf(
                "plan-local-renderer-manifest-adapter-contract",
                "--dry-run-report",
                LocalRendererManifestAdapterContractPlanServiceTest.sampleDryRunPath.toString(),
                "--plan",
                "manifest-adapter-contract-plan-cli-001",
                "--out",
                outPath.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(outPath))
        val plan = readJson<LocalRendererManifestAdapterContractPlan>(outPath)
        assertEquals("PASS", plan.verdict, plan.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN", plan.status)
        assertEquals("manifest-adapter-contract-plan-cli-001", plan.planId)
        assertEquals(
            "rain-gate-local-renderer-manifest-draft-json-adapter-dry-run-v0-001",
            plan.sourcePhase16DryRunId,
        )
        assertEquals("rain-gate-local-renderer-manifest-draft-json-draft-v0-001", plan.sourceDraftId)
        assertEquals("rain-gate-local-renderer-manifest-draft-json-review-v0-001", plan.sourceReviewId)
        assertEquals("local-story-input-rain-gate-v0-001", plan.sourceStoryInputPackId)
        assertTrue(plan.checks.isNotEmpty())
        assertTrue(plan.checks.all { it.status == "PASS" })
        assertEquals(4, plan.adapterContractRequirements.size)
        assertEquals(2, plan.adapterInputMappingPlan.entries.size)
        assertEquals(2, plan.adapterContractPlanSummary.mappedPanelCount)
        assertEquals(0, plan.adapterContractPlanSummary.unmappedPanelCount)
        assertEquals(46, plan.adapterContractPlanSummary.totalDraftFields)
        assertTrue(plan.boundaries.draftManifestGenerated)
        assertFalse(plan.boundaries.productionManifestGenerated)
        assertFalse(plan.boundaries.productionReady)
        assertFalse(plan.boundaries.productionApproved)
        assertFalse(plan.boundaries.mediaGenerated)
        assertEquals("none", plan.boundaries.rendererInvocation)
        assertEquals("none", plan.boundaries.providerInvocation)
        assertEquals("none", plan.boundaries.ttsInvocation)
        assertFalse(plan.boundaries.adapterImplementationExecuted)
        assertFalse(plan.boundaries.adapterExecuted)
        assertTrue(plan.boundaries.noExternalPaidProviderTools)
        assertFalse(plan.boundaries.rendererReady)
        assertFalse(plan.boundaries.mp4ReadinessClaimed)
        assertEquals(
            "local_renderer_manifest_adapter_contract_plan_only_not_adapter_implementation",
            plan.downstreamBoundary,
        )
        assertEquals("local_renderer_manifest_adapter_contract_plan_review", plan.nextAllowedDownstreamStep)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
}
