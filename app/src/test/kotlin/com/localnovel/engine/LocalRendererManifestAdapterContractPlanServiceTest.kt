package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlan
import com.localnovel.engine.model.LocalRendererManifestDraftJsonAdapterDryRunReport
import com.localnovel.engine.service.LocalRendererManifestAdapterContractPlanService
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class LocalRendererManifestAdapterContractPlanServiceTest {
    @Test
    fun passesRainGateAdapterContractPlan() {
        val dryRun = validDryRun()
        val plan = LocalRendererManifestAdapterContractPlanService().plan(
            dryRun = dryRun,
            dryRunReportFile = sampleDryRunPath.toString(),
            planId = "chenxing-local-renderer-manifest-adapter-contract-plan-v0-001",
        )

        assertPassPlan(plan)
        assertEquals("chenxing-local-renderer-manifest-adapter-contract-plan-v0-001", plan.planId)
        assertEquals(dryRun.dryRunId, plan.sourcePhase16DryRunId)
        assertEquals(dryRun.sourceDraftId, plan.sourceDraftId)
        assertEquals(dryRun.sourceReviewId, plan.sourceReviewId)
        assertEquals(dryRun.sourceStoryInputPackId, plan.sourceStoryInputPackId)
        assertEquals(4, plan.adapterContractRequirements.size)
        assertEquals(3, plan.adapterInputMappingPlan.entries.size)
        assertEquals(3, plan.requiredAdapterContractWorkItems.size)
        assertEquals(3, plan.verificationPlan.size)
        assertTrue(plan.adapterInputMappingPlan.planningOnly)
        assertFalse(plan.adapterInputMappingPlan.adapterExecuted)
    }

    @Test
    fun failsWhenDryRunStateIsWrong() {
        val wrongStatus = LocalRendererManifestAdapterContractPlanService().plan(
            dryRun = validDryRun().copy(status = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_FAIL"),
            dryRunReportFile = sampleDryRunPath.toString(),
        )
        val wrongNextStep = LocalRendererManifestAdapterContractPlanService().plan(
            dryRun = validDryRun().copy(nextAllowedDownstreamStep = "wrong-next-step"),
            dryRunReportFile = sampleDryRunPath.toString(),
        )

        assertEquals("FAIL", wrongStatus.verdict)
        assertTrue(wrongStatus.checks.any { it.checkId == "dry-run-state" && it.status == "FAIL" })
        assertEquals("", wrongStatus.nextAllowedDownstreamStep)

        assertEquals("FAIL", wrongNextStep.verdict)
        assertTrue(wrongNextStep.checks.any { it.checkId == "dry-run-state" && it.status == "FAIL" })
        assertEquals("", wrongNextStep.nextAllowedDownstreamStep)
    }

    @Test
    fun failsWhenMediaOrAdapterExecutionBoundaryIsForged() {
        val dryRun = validDryRun()
        val report = LocalRendererManifestAdapterContractPlanService().plan(
            dryRun = dryRun.copy(
                boundaries = dryRun.boundaries.copy(
                    mediaGenerated = true,
                    adapterExecuted = true,
                ),
            ),
            dryRunReportFile = sampleDryRunPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenProductionOrReadinessIsClaimed() {
        val dryRun = validDryRun()
        val report = LocalRendererManifestAdapterContractPlanService().plan(
            dryRun = dryRun.copy(
                boundaries = dryRun.boundaries.copy(
                    productionManifestGenerated = true,
                    productionReady = true,
                    productionApproved = true,
                    rendererReady = true,
                    mp4ReadinessClaimed = true,
                ),
            ),
            dryRunReportFile = sampleDryRunPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenMappedPanelsAreIncomplete() {
        val dryRun = validDryRun()
        val firstBroken = dryRun.adapterDryRunMappingEntries.first().copy(
            canMapForFutureAdapter = false,
            missingRequiredFields = listOf("durationMillis"),
        )
        val report = LocalRendererManifestAdapterContractPlanService().plan(
            dryRun = dryRun.copy(
                panelMappingSummary = dryRun.panelMappingSummary.copy(
                    mappedPanelCount = 1,
                    unmappedPanelCount = 1,
                    missingRequiredFieldCount = 1,
                ),
                adapterDryRunMappingEntries = listOf(firstBroken) + dryRun.adapterDryRunMappingEntries.drop(1),
            ),
            dryRunReportFile = sampleDryRunPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "dry-run-mapping-complete" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenDraftFieldCoverageIsIncomplete() {
        val dryRun = validDryRun()
        val report = LocalRendererManifestAdapterContractPlanService().plan(
            dryRun = dryRun.copy(
                draftFieldCoverageSummary = dryRun.draftFieldCoverageSummary.copy(
                    mappedDraftFields = 45,
                    missingRequiredDraftFields = 1,
                ),
            ),
            dryRunReportFile = sampleDryRunPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "field-coverage-complete" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenMappingContentIsEmpty() {
        val dryRun = validDryRun()
        val report = LocalRendererManifestAdapterContractPlanService().plan(
            dryRun = dryRun.copy(adapterDryRunMappingEntries = emptyList()),
            dryRunReportFile = sampleDryRunPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "dry-run-mapping-complete" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "contract-content-present" && it.status == "FAIL" })
    }

    companion object {
        val sampleDryRunPath: Path =
            locateSample("chenxing_local_renderer_manifest_draft_json_adapter_dry_run_report_sample.json")

        fun validDryRun(): LocalRendererManifestDraftJsonAdapterDryRunReport =
            readJson(sampleDryRunPath)

        fun assertPassPlan(plan: LocalRendererManifestAdapterContractPlan) {
            assertEquals("PASS", plan.verdict, plan.findings.joinToString { it.ruleId })
            assertEquals("LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN", plan.status)
            assertEquals(9, plan.checks.size)
            assertTrue(plan.checks.all { it.status == "PASS" })
            assertEquals(3, plan.adapterContractPlanSummary.draftPanelCount)
            assertEquals(3, plan.adapterContractPlanSummary.mappedPanelCount)
            assertEquals(0, plan.adapterContractPlanSummary.unmappedPanelCount)
            assertEquals(69, plan.adapterContractPlanSummary.totalDraftFields)
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
            assertEquals(0, plan.findingCount)
        }

        private inline fun <reified T> readJson(path: Path): T =
            JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

        private fun locateSample(fileName: String): Path {
            val relativePath = Path.of("samples", "chenxing", "pipeline", fileName)
            return generateSequence(Path.of("").toAbsolutePath().normalize()) { it.parent }
                .map { it.resolve(relativePath) }
                .firstOrNull { Files.exists(it) }
                ?: relativePath
        }
    }
}
