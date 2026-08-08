package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererManifestDraftJsonAdapterDryRunReport
import com.localnovel.engine.model.LocalRendererManifestDraftJsonDraft
import com.localnovel.engine.model.LocalRendererManifestDraftJsonDraftFinding
import com.localnovel.engine.model.LocalRendererManifestDraftJsonReviewReport
import com.localnovel.engine.service.LocalRendererManifestDraftJsonAdapterDryRunService
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class LocalRendererManifestDraftJsonAdapterDryRunServiceTest {
    @Test
    fun passesRainGateManifestDraftJsonAdapterDryRun() {
        val draft = validDraft()
        val review = validReview()

        val report = LocalRendererManifestDraftJsonAdapterDryRunService().dryRun(
            draft = draft,
            review = review,
            draftFile = sampleDraftPath.toString(),
            reviewFile = sampleReviewPath.toString(),
            dryRunId = "chenxing-manifest-draft-json-adapter-dry-run-001",
        )

        assertPassReport(report)
        assertEquals("chenxing-manifest-draft-json-adapter-dry-run-001", report.dryRunId)
        assertEquals(draft.draftId, report.sourceDraftId)
        assertEquals(review.reviewId, report.sourceReviewId)
        assertEquals(draft.sourceStoryInputPackId, report.sourceStoryInputPackId)
        assertTrue(report.sourceAlignmentSummary.draftIdAligned)
        assertTrue(report.sourceAlignmentSummary.sourceStoryInputPackIdAligned)
        assertEquals(3, report.panelMappingSummary.draftPanelCount)
        assertEquals(3, report.panelMappingSummary.adapterDryRunMappingEntries)
        assertEquals(3, report.panelMappingSummary.mappedPanelCount)
        assertEquals(0, report.panelMappingSummary.unmappedPanelCount)
        assertEquals(69, report.draftFieldCoverageSummary.totalDraftFields)
        assertEquals(69, report.draftFieldCoverageSummary.mappedDraftFields)
        assertEquals(0, report.panelMappingSummary.missingRequiredFieldCount)
        assertEquals(3, report.adapterDryRunMappingEntries.size)
        assertTrue(report.adapterDryRunMappingEntries.all { it.canMapForFutureAdapter })
        assertTrue(report.adapterDryRunMappingEntries.all { !it.adapterExecuted })
    }

    @Test
    fun failsWhenDraftOrReviewStateIsWrong() {
        val wrongDraft = LocalRendererManifestDraftJsonAdapterDryRunService().dryRun(
            draft = validDraft().copy(
                verdict = "FAIL",
                status = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT_FAIL",
            ),
            review = validReview(),
            draftFile = sampleDraftPath.toString(),
            reviewFile = sampleReviewPath.toString(),
        )
        val wrongReview = LocalRendererManifestDraftJsonAdapterDryRunService().dryRun(
            draft = validDraft(),
            review = validReview().copy(nextAllowedDownstreamStep = "wrong-next-step"),
            draftFile = sampleDraftPath.toString(),
            reviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", wrongDraft.verdict)
        assertTrue(wrongDraft.checks.any { it.checkId == "draft-state" && it.status == "FAIL" })
        assertEquals("", wrongDraft.nextAllowedDownstreamStep)

        assertEquals("FAIL", wrongReview.verdict)
        assertTrue(wrongReview.checks.any { it.checkId == "review-state" && it.status == "FAIL" })
        assertEquals("", wrongReview.nextAllowedDownstreamStep)
    }

    @Test
    fun failsWhenSourceIdsDoNotAlign() {
        val report = LocalRendererManifestDraftJsonAdapterDryRunService().dryRun(
            draft = validDraft(),
            review = validReview().copy(sourceDraftId = "wrong-draft-id"),
            draftFile = sampleDraftPath.toString(),
            reviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "source-ids-align" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenSourceFindingsArePresent() {
        val report = LocalRendererManifestDraftJsonAdapterDryRunService().dryRun(
            draft = validDraft().copy(
                findingCount = 1,
                findings = listOf(
                    LocalRendererManifestDraftJsonDraftFinding(
                        ruleId = "TEST_DRAFT_FINDING",
                        category = "test",
                        severity = "CRITICAL",
                        path = "$.test",
                        message = "Injected draft finding.",
                    ),
                ),
            ),
            review = validReview(),
            draftFile = sampleDraftPath.toString(),
            reviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "no-findings-in-sources" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenMediaGeneratedOrAdapterExecutedBoundaryIsForged() {
        val draft = validDraft()
        val review = validReview()
        val report = LocalRendererManifestDraftJsonAdapterDryRunService().dryRun(
            draft = draft.copy(boundaries = draft.boundaries.copy(mediaGenerated = true)),
            review = review.copy(boundaries = review.boundaries.copy(adapterImplementationExecuted = true)),
            draftFile = sampleDraftPath.toString(),
            reviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenProductionOrReadinessIsClaimed() {
        val review = validReview()
        val report = LocalRendererManifestDraftJsonAdapterDryRunService().dryRun(
            draft = validDraft(),
            review = review.copy(
                boundaries = review.boundaries.copy(
                    productionManifestGenerated = true,
                    productionReady = true,
                    productionApproved = true,
                    rendererReady = true,
                    mp4ReadinessClaimed = true,
                ),
            ),
            draftFile = sampleDraftPath.toString(),
            reviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "no-production-or-readiness-overclaim" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenRawProductionApprovedIsForged() {
        val report = LocalRendererManifestDraftJsonAdapterDryRunService().dryRun(
            draft = validDraft(),
            review = validReview(),
            draftFile = sampleDraftPath.toString(),
            reviewFile = sampleReviewPath.toString(),
            sourceProductionApprovedClaim = true,
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenDraftContentIsMissing() {
        val draft = validDraft()
        val report = LocalRendererManifestDraftJsonAdapterDryRunService().dryRun(
            draft = draft.copy(
                draftPanelManifestEntries = emptyList(),
                draftFieldCoverageSummary = draft.draftFieldCoverageSummary.copy(
                    panelEntries = 0,
                    totalDraftFields = 0,
                ),
            ),
            review = validReview(),
            draftFile = sampleDraftPath.toString(),
            reviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "draft-content-present" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "adapter-mapping-complete" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenAnyDraftPanelCannotMap() {
        val draft = validDraft()
        val brokenPanel = draft.draftPanelManifestEntries.first().copy(durationMillis = 0)
        val report = LocalRendererManifestDraftJsonAdapterDryRunService().dryRun(
            draft = draft.copy(
                draftPanelManifestEntries = listOf(brokenPanel) + draft.draftPanelManifestEntries.drop(1),
            ),
            review = validReview(),
            draftFile = sampleDraftPath.toString(),
            reviewFile = sampleReviewPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "adapter-mapping-complete" && it.status == "FAIL" })
        assertTrue(report.panelMappingSummary.unmappedPanelCount > 0)
    }

    companion object {
        val sampleDraftPath: Path =
            locateSample("chenxing_local_renderer_manifest_draft_json_draft_sample.json")
        val sampleReviewPath: Path =
            locateSample("chenxing_local_renderer_manifest_draft_json_review_report_sample.json")

        fun validDraft(): LocalRendererManifestDraftJsonDraft =
            readJson(sampleDraftPath)

        fun validReview(): LocalRendererManifestDraftJsonReviewReport =
            readJson(sampleReviewPath)

        fun assertPassReport(report: LocalRendererManifestDraftJsonAdapterDryRunReport) {
            assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
            assertEquals("LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_PASS", report.status)
            assertEquals(10, report.checks.size)
            assertTrue(report.checks.all { it.status == "PASS" })
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
                "local_renderer_manifest_draft_json_adapter_dry_run_only_not_renderer_execution",
                report.downstreamBoundary,
            )
            assertEquals("local_renderer_manifest_adapter_contract_plan", report.nextAllowedDownstreamStep)
            assertEquals(0, report.findingCount)
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
