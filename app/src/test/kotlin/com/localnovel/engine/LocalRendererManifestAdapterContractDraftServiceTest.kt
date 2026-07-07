package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererManifestAdapterContractDraft
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlan
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanFinding
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanReviewFinding
import com.localnovel.engine.model.LocalRendererManifestAdapterContractPlanReviewReport
import com.localnovel.engine.service.LocalRendererManifestAdapterContractDraftService
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class LocalRendererManifestAdapterContractDraftServiceTest {
    @Test
    fun passesRainGateAdapterContractDraft() {
        val plan = validPlan()
        val review = validReview()
        val draft = LocalRendererManifestAdapterContractDraftService().draft(
            plan = plan,
            review = review,
            planFile = samplePlanPath.toString(),
            planReviewFile = sampleReviewPath.toString(),
            draftId = "rain-gate-local-renderer-manifest-adapter-contract-draft-v0-001",
        )

        assertPassDraft(draft)
        assertEquals("rain-gate-local-renderer-manifest-adapter-contract-draft-v0-001", draft.draftId)
        assertEquals(plan.planId, draft.sourcePlanId)
        assertEquals(review.reviewId, draft.sourcePlanReviewId)
        assertEquals(plan.sourcePhase16DryRunId, draft.sourcePhase16DryRunId)
        assertEquals(plan.sourceStoryInputPackId, draft.sourceStoryInputPackId)
        assertEquals(2, draft.requiredInputs.size)
        assertEquals(2, draft.normalizedFieldMapping.size)
        assertEquals(3, draft.adapterObligations.size)
        assertEquals(6, draft.failStopRules.size)
        assertEquals(3, draft.verificationRequirements.size)
        assertEquals(10, draft.knownNonGoals.size)
    }

    @Test
    fun failsWhenPlanOrReviewStateIsWrong() {
        val wrongPlan = LocalRendererManifestAdapterContractDraftService().draft(
            plan = validPlan().copy(status = "LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_FAIL"),
            review = validReview(),
            planFile = samplePlanPath.toString(),
            planReviewFile = sampleReviewPath.toString(),
        )
        val wrongReview = LocalRendererManifestAdapterContractDraftService().draft(
            plan = validPlan(),
            review = validReview().copy(nextAllowedDownstreamStep = "wrong-next-step"),
            planFile = samplePlanPath.toString(),
            planReviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", wrongPlan.verdict)
        assertTrue(wrongPlan.checks.any { it.checkId == "plan-state" && it.status == "FAIL" })
        assertEquals("", wrongPlan.nextAllowedDownstreamStep)

        assertEquals("FAIL", wrongReview.verdict)
        assertTrue(wrongReview.checks.any { it.checkId == "plan-review-state" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenSourceIdsMismatch() {
        val report = LocalRendererManifestAdapterContractDraftService().draft(
            plan = validPlan(),
            review = validReview().copy(sourcePlanId = "wrong-plan-id"),
            planFile = samplePlanPath.toString(),
            planReviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "source-alignment" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenMediaOrAdapterExecutionBoundaryIsForged() {
        val plan = validPlan()
        val review = validReview()
        val report = LocalRendererManifestAdapterContractDraftService().draft(
            plan = plan.copy(
                boundaries = plan.boundaries.copy(
                    mediaGenerated = true,
                    adapterImplementationExecuted = true,
                    adapterExecuted = true,
                ),
            ),
            review = review.copy(
                boundaries = review.boundaries.copy(
                    mediaGenerated = true,
                    adapterExecuted = true,
                ),
            ),
            planFile = samplePlanPath.toString(),
            planReviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenProductionOrReadinessIsClaimed() {
        val plan = validPlan()
        val report = LocalRendererManifestAdapterContractDraftService().draft(
            plan = plan.copy(
                boundaries = plan.boundaries.copy(
                    productionManifestGenerated = true,
                    productionReady = true,
                    productionApproved = true,
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
    }

    @Test
    fun failsWhenFindingsOrCheckFailuresExist() {
        val plan = validPlan()
        val review = validReview()
        val report = LocalRendererManifestAdapterContractDraftService().draft(
            plan = plan.copy(
                checks = listOf(plan.checks.first().copy(status = "FAIL")) + plan.checks.drop(1),
                findingCount = 1,
                findings = listOf(
                    LocalRendererManifestAdapterContractPlanFinding(
                        ruleId = "TEST_CONTRACT_PLAN_FINDING",
                        category = "test",
                        severity = "CRITICAL",
                        path = "$.test",
                        message = "Injected plan finding.",
                    ),
                ),
            ),
            review = review.copy(
                checks = listOf(review.checks.first().copy(status = "FAIL")) + review.checks.drop(1),
                findingCount = 1,
                findings = listOf(
                    LocalRendererManifestAdapterContractPlanReviewFinding(
                        ruleId = "TEST_CONTRACT_PLAN_REVIEW_FINDING",
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
        assertTrue(report.checks.any { it.checkId == "source-checks-pass" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenRequiredContentIsEmpty() {
        val plan = validPlan()
        val report = LocalRendererManifestAdapterContractDraftService().draft(
            plan = plan.copy(
                adapterContractRequirements = emptyList(),
                adapterInputMappingPlan = plan.adapterInputMappingPlan.copy(entries = emptyList()),
                requiredAdapterContractWorkItems = emptyList(),
                verificationPlan = emptyList(),
            ),
            review = validReview(),
            planFile = samplePlanPath.toString(),
            planReviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "contract-sections-present" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "field-mapping-normalized" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenVerificationRequestsExecution() {
        val plan = validPlan()
        val report = LocalRendererManifestAdapterContractDraftService().draft(
            plan = plan.copy(
                verificationPlan = listOf(
                    plan.verificationPlan.first().copy(
                        requiresAdapterExecution = true,
                        requiresRendererExecution = true,
                        requiresMediaGeneration = true,
                        requiresProductionManifestApproval = true,
                    ),
                ) + plan.verificationPlan.drop(1),
            ),
            review = validReview(),
            planFile = samplePlanPath.toString(),
            planReviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "draft-only-boundary" && it.status == "FAIL" })
    }

    companion object {
        val samplePlanPath: Path =
            locateSample("rain_gate_local_renderer_manifest_adapter_contract_plan_sample.json")
        val sampleReviewPath: Path =
            locateSample("rain_gate_local_renderer_manifest_adapter_contract_plan_review_report_sample.json")

        fun validPlan(): LocalRendererManifestAdapterContractPlan =
            readJson(samplePlanPath)

        fun validReview(): LocalRendererManifestAdapterContractPlanReviewReport =
            readJson(sampleReviewPath)

        fun assertPassDraft(draft: LocalRendererManifestAdapterContractDraft) {
            assertEquals("PASS", draft.verdict, draft.findings.joinToString { it.ruleId })
            assertEquals("LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_DRAFT", draft.status)
            assertEquals(9, draft.checks.size)
            assertTrue(draft.checks.all { it.status == "PASS" })
            assertEquals(2, draft.contractDraftSummary.mappedPanelCount)
            assertEquals(0, draft.contractDraftSummary.unmappedPanelCount)
            assertEquals(0, draft.contractDraftSummary.missingRequiredFieldCount)
            assertEquals(46, draft.contractDraftSummary.totalDraftFields)
            assertTrue(draft.boundaries.contractDraftGenerated)
            assertFalse(draft.boundaries.productionManifestGenerated)
            assertFalse(draft.boundaries.productionReady)
            assertFalse(draft.boundaries.productionApproved)
            assertFalse(draft.boundaries.mediaGenerated)
            assertEquals("none", draft.boundaries.rendererInvocation)
            assertEquals("none", draft.boundaries.providerInvocation)
            assertEquals("none", draft.boundaries.ttsInvocation)
            assertFalse(draft.boundaries.adapterImplementationExecuted)
            assertFalse(draft.boundaries.adapterExecuted)
            assertTrue(draft.boundaries.noExternalPaidProviderTools)
            assertFalse(draft.boundaries.rendererReady)
            assertFalse(draft.boundaries.mp4ReadinessClaimed)
            assertEquals(
                "local_renderer_manifest_adapter_contract_draft_only_not_adapter_execution",
                draft.downstreamBoundary,
            )
            assertEquals("local_renderer_manifest_adapter_contract_draft_review", draft.nextAllowedDownstreamStep)
            assertEquals(0, draft.findingCount)
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
