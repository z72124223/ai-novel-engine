package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanReviewReport
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class LocalRendererManifestAdapterContractPlanReviewCliTest {
    @Test
    fun cliWritesLocalRendererManifestAdapterContractPlanReviewReport() {
        val tempDir = Files.createTempDirectory("local-renderer-manifest-adapter-contract-plan-review")
        val outPath = tempDir.resolve("manifest_adapter_contract_plan_review.json")

        val exit = Cli().run(
            listOf(
                "review-local-renderer-manifest-adapter-contract-plan",
                "--plan",
                LocalRendererManifestAdapterContractPlanReviewServiceTest.samplePlanPath.toString(),
                "--review",
                "manifest-adapter-contract-plan-review-cli-001",
                "--out",
                outPath.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(outPath))
        val report = readJson<LocalRendererManifestAdapterContractPlanReviewReport>(outPath)
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_REVIEW_PASS", report.status)
        assertEquals("manifest-adapter-contract-plan-review-cli-001", report.reviewId)
        assertEquals(
            "chenxing-local-renderer-manifest-adapter-contract-plan-v0-001",
            report.sourcePlanId,
        )
        assertEquals(
            "chenxing-local-renderer-manifest-draft-json-adapter-dry-run-v0-001",
            report.sourcePhase16DryRunId,
        )
        assertEquals("local-story-input-chenxing-v0-001", report.sourceStoryInputPackId)
        assertTrue(report.checks.isNotEmpty())
        assertTrue(report.checks.all { it.status == "PASS" })
        assertEquals(4, report.reviewedContractRequirementsSummary.requirementCount)
        assertEquals(3, report.reviewedInputMappingSummary.mappingEntries)
        assertEquals(3, report.reviewedWorkItemsSummary.workItemCount)
        assertEquals(3, report.verificationReviewSummary.verificationStepCount)
        assertTrue(report.boundaries.draftManifestGenerated)
        assertFalse(report.boundaries.productionManifestGenerated)
        assertFalse(report.boundaries.productionReady)
        assertFalse(report.boundaries.productionApproved)
        assertFalse(report.boundaries.mediaGenerated)
        assertEquals("none", report.boundaries.rendererInvocation)
        assertEquals("none", report.boundaries.providerInvocation)
        assertEquals("none", report.boundaries.ttsInvocation)
        assertFalse(report.boundaries.adapterImplementationExecuted)
        assertFalse(report.boundaries.adapterExecuted)
        assertTrue(report.boundaries.noExternalPaidProviderTools)
        assertFalse(report.boundaries.rendererReady)
        assertFalse(report.boundaries.mp4ReadinessClaimed)
        assertEquals(
            "local_renderer_manifest_adapter_contract_plan_review_only_not_adapter_implementation",
            report.downstreamBoundary,
        )
        assertEquals("local_renderer_manifest_adapter_contract_draft", report.nextAllowedDownstreamStep)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
}
