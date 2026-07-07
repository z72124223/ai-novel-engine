package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlan
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanFinding
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanReviewReport
import com.localnovel.engine.service.LocalRendererManifestAdapterContractPlanReviewService
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class LocalRendererManifestAdapterContractPlanReviewServiceTest {
    @Test
    fun passesRainGateAdapterContractPlanReview() {
        val plan = validPlan()
        val report = LocalRendererManifestAdapterContractPlanReviewService().review(
            plan = plan,
            planFile = samplePlanPath.toString(),
            reviewId = "rain-gate-local-renderer-manifest-adapter-contract-plan-review-v0-001",
        )

        assertPassReport(report)
        assertEquals("rain-gate-local-renderer-manifest-adapter-contract-plan-review-v0-001", report.reviewId)
        assertEquals(plan.planId, report.sourcePlanId)
        assertEquals(plan.sourcePhase16DryRunId, report.sourcePhase16DryRunId)
        assertEquals(plan.sourceStoryInputPackId, report.sourceStoryInputPackId)
        assertEquals(4, report.reviewedContractRequirementsSummary.requirementCount)
        assertEquals(2, report.reviewedInputMappingSummary.mappingEntries)
        assertEquals(3, report.reviewedWorkItemsSummary.workItemCount)
        assertEquals(3, report.verificationReviewSummary.verificationStepCount)
    }

    @Test
    fun failsWhenPlanStateIsWrong() {
        val wrongStatus = LocalRendererManifestAdapterContractPlanReviewService().review(
            plan = validPlan().copy(status = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_FAIL"),
            planFile = samplePlanPath.toString(),
        )
        val wrongBoundary = LocalRendererManifestAdapterContractPlanReviewService().review(
            plan = validPlan().copy(downstreamBoundary = "wrong-boundary"),
            planFile = samplePlanPath.toString(),
        )
        val wrongNextStep = LocalRendererManifestAdapterContractPlanReviewService().review(
            plan = validPlan().copy(nextAllowedDownstreamStep = "wrong-next-step"),
            planFile = samplePlanPath.toString(),
        )

        assertEquals("FAIL", wrongStatus.verdict)
        assertTrue(wrongStatus.checks.any { it.checkId == "plan-state" && it.status == "FAIL" })
        assertEquals("", wrongStatus.nextAllowedDownstreamStep)

        assertEquals("FAIL", wrongBoundary.verdict)
        assertTrue(wrongBoundary.checks.any { it.checkId == "plan-state" && it.status == "FAIL" })

        assertEquals("FAIL", wrongNextStep.verdict)
        assertTrue(wrongNextStep.checks.any { it.checkId == "plan-state" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenMediaOrAdapterExecutionBoundaryIsForged() {
        val plan = validPlan()
        val report = LocalRendererManifestAdapterContractPlanReviewService().review(
            plan = plan.copy(
                boundaries = plan.boundaries.copy(
                    mediaGenerated = true,
                    adapterImplementationExecuted = true,
                    adapterExecuted = true,
                ),
            ),
            planFile = samplePlanPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenProductionOrReadinessIsClaimed() {
        val plan = validPlan()
        val report = LocalRendererManifestAdapterContractPlanReviewService().review(
            plan = plan.copy(
                boundaries = plan.boundaries.copy(
                    productionManifestGenerated = true,
                    productionReady = true,
                    productionApproved = true,
                    rendererReady = true,
                    mp4ReadinessClaimed = true,
                ),
            ),
            planFile = samplePlanPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenFindingsOrCheckFailuresExist() {
        val plan = validPlan()
        val report = LocalRendererManifestAdapterContractPlanReviewService().review(
            plan = plan.copy(
                checks = listOf(plan.checks.first().copy(status = "FAIL")) + plan.checks.drop(1),
                findingCount = 1,
                findings = listOf(
                    LocalRendererManifestAdapterContractPlanFinding(
                        ruleId = "TEST_CONTRACT_PLAN_FINDING",
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
        assertTrue(report.checks.any { it.checkId == "plan-checks-pass" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenMappingHasUnmappedPanelsOrMissingFields() {
        val plan = validPlan()
        val brokenEntry = plan.adapterInputMappingPlan.entries.first().copy(
            plannedAdapterContractFields = emptyList(),
            missingRequiredFieldCount = 1,
        )
        val report = LocalRendererManifestAdapterContractPlanReviewService().review(
            plan = plan.copy(
                adapterContractPlanSummary = plan.adapterContractPlanSummary.copy(
                    mappedPanelCount = 1,
                    unmappedPanelCount = 1,
                    missingRequiredFieldCount = 1,
                ),
                adapterInputMappingPlan = plan.adapterInputMappingPlan.copy(
                    entries = listOf(brokenEntry) + plan.adapterInputMappingPlan.entries.drop(1),
                ),
            ),
            planFile = samplePlanPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "mapping-complete" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenRequiredContentIsEmpty() {
        val plan = validPlan()
        val report = LocalRendererManifestAdapterContractPlanReviewService().review(
            plan = plan.copy(
                adapterContractRequirements = emptyList(),
                adapterInputMappingPlan = plan.adapterInputMappingPlan.copy(entries = emptyList()),
                requiredAdapterContractWorkItems = emptyList(),
                verificationPlan = emptyList(),
            ),
            planFile = samplePlanPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "contract-content-present" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "mapping-complete" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "review-only-boundary" && it.status == "FAIL" })
    }

    companion object {
        val samplePlanPath: Path =
            locateSample("rain_gate_local_renderer_manifest_adapter_contract_plan_sample.json")

        fun validPlan(): LocalRendererManifestAdapterContractPlan =
            readJson(samplePlanPath)

        fun assertPassReport(report: LocalRendererManifestAdapterContractPlanReviewReport) {
            assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
            assertEquals("LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_REVIEW_PASS", report.status)
            assertEquals(8, report.checks.size)
            assertTrue(report.checks.all { it.status == "PASS" })
            assertEquals(2, report.adapterContractPlanReviewSummary.mappedPanelCount)
            assertEquals(0, report.adapterContractPlanReviewSummary.unmappedPanelCount)
            assertEquals(0, report.adapterContractPlanReviewSummary.missingRequiredFieldCount)
            assertEquals(46, report.adapterContractPlanReviewSummary.totalDraftFields)
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
            assertEquals(0, report.findingCount)
        }

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
