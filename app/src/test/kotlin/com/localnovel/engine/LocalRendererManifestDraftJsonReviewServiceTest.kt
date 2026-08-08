package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererManifestDraftJsonDraft
import com.localnovel.engine.model.LocalRendererManifestDraftJsonDraftFinding
import com.localnovel.engine.service.LocalRendererManifestDraftJsonReviewService
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class LocalRendererManifestDraftJsonReviewServiceTest {
    @Test
    fun passesRainGateManifestDraftJsonReview() {
        val draft = validDraft()

        val report = LocalRendererManifestDraftJsonReviewService().review(
            draft = draft,
            draftFile = sampleDraftPath.toString(),
            reviewId = "chenxing-manifest-draft-json-review-001",
        )

        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_PASS", report.status)
        assertEquals("chenxing-manifest-draft-json-review-001", report.reviewId)
        assertEquals(draft.draftId, report.sourceDraftId)
        assertEquals(draft.sourceManifestDraftPlanId, report.sourceManifestDraftPlanId)
        assertEquals(draft.sourceManifestDraftPlanReviewId, report.sourceManifestDraftPlanReviewId)
        assertEquals(draft.sourceStoryInputPackId, report.sourceStoryInputPackId)
        assertEquals(10, report.checks.size)
        assertTrue(report.checks.all { it.status == "PASS" })
        assertTrue(report.sourceAlignmentSummary.draftIdPresent)
        assertTrue(report.sourceAlignmentSummary.sourceAlignmentFromDraftPass)
        assertEquals(3, report.draftManifestReviewSummary.draftPanelEntries)
        assertTrue(report.draftManifestReviewSummary.draftManifestFields > 0)
        assertTrue(report.draftManifestReviewSummary.draftOnly)
        assertTrue(report.draftManifestReviewSummary.draftManifestGenerated)
        assertFalse(report.draftManifestReviewSummary.productionManifestGenerated)
        assertFalse(report.draftManifestReviewSummary.productionReady)
        assertFalse(report.draftManifestReviewSummary.productionApproved)
        assertTrue(report.boundaries.draftManifestGenerated)
        assertFalse(report.boundaries.productionManifestGenerated)
        assertFalse(report.boundaries.productionReady)
        assertFalse(report.boundaries.productionApproved)
        assertFalse(report.boundaries.mediaGenerated)
        assertEquals("none", report.boundaries.rendererInvocation)
        assertEquals("none", report.boundaries.providerInvocation)
        assertEquals("none", report.boundaries.ttsInvocation)
        assertFalse(report.boundaries.adapterImplementationExecuted)
        assertTrue(report.boundaries.noExternalPaidProviderTools)
        assertFalse(report.boundaries.rendererReady)
        assertFalse(report.boundaries.mp4ReadinessClaimed)
        assertEquals("local_renderer_manifest_draft_json_review_only_not_production_approval", report.downstreamBoundary)
        assertEquals("local_renderer_manifest_draft_json_adapter_dry_run", report.nextAllowedDownstreamStep)
        assertEquals(0, report.findingCount)
    }

    @Test
    fun failsWhenDraftStateIsWrong() {
        val failReport = LocalRendererManifestDraftJsonReviewService().review(
            draft = validDraft().copy(
                verdict = "FAIL",
                status = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT_FAIL",
            ),
            draftFile = sampleDraftPath.toString(),
        )
        val wrongNextReport = LocalRendererManifestDraftJsonReviewService().review(
            draft = validDraft().copy(nextAllowedDownstreamStep = "wrong-next-step"),
            draftFile = sampleDraftPath.toString(),
        )

        assertEquals("FAIL", failReport.verdict)
        assertEquals("LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_FAIL", failReport.status)
        assertTrue(failReport.checks.any { it.checkId == "draft-state" && it.status == "FAIL" })
        assertEquals("", failReport.nextAllowedDownstreamStep)

        assertEquals("FAIL", wrongNextReport.verdict)
        assertTrue(wrongNextReport.checks.any { it.checkId == "draft-state" && it.status == "FAIL" })
        assertEquals("", wrongNextReport.nextAllowedDownstreamStep)
    }

    @Test
    fun failsWhenMediaGeneratedBoundaryIsForged() {
        val draft = validDraft()
        val report = LocalRendererManifestDraftJsonReviewService().review(
            draft = draft.copy(boundaries = draft.boundaries.copy(mediaGenerated = true)),
            draftFile = sampleDraftPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenProductionManifestOrReadinessIsClaimed() {
        val draft = validDraft()
        val report = LocalRendererManifestDraftJsonReviewService().review(
            draft = draft.copy(
                boundaries = draft.boundaries.copy(
                    productionManifestGenerated = true,
                    productionReady = true,
                ),
                draftManifestSummary = draft.draftManifestSummary.copy(
                    productionManifestGenerated = true,
                    productionReady = true,
                ),
            ),
            draftFile = sampleDraftPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "no-production-approval" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "draft-only-not-production-manifest" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenRawProductionApprovedIsForged() {
        val tempDir = Files.createTempDirectory("manifest-draft-json-review-forged")
        val forgedPath = tempDir.resolve("forged-production-approved.json")
        val raw = Files.readString(sampleDraftPath).replace(
            "\"productionReady\": false",
            "\"productionReady\": false,\n        \"productionApproved\": true",
            ignoreCase = false,
        )
        Files.writeString(forgedPath, raw)

        val report = LocalRendererManifestDraftJsonReviewService().reviewFile(
            draftPath = forgedPath,
            reviewId = "forged-production-approved-review",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "no-production-approval" && it.status == "FAIL" })
        assertFalse(report.boundaries.productionApproved)
    }

    @Test
    fun failsWhenRendererOrMp4ReadinessIsClaimed() {
        val draft = validDraft()
        val report = LocalRendererManifestDraftJsonReviewService().review(
            draft = draft.copy(
                boundaries = draft.boundaries.copy(
                    rendererReady = true,
                    mp4ReadinessClaimed = true,
                ),
                draftManifestSummary = draft.draftManifestSummary.copy(
                    rendererReady = true,
                    mp4ReadinessClaimed = true,
                ),
            ),
            draftFile = sampleDraftPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "no-readiness-overclaim" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenFindingsArePresent() {
        val draft = validDraft()
        val report = LocalRendererManifestDraftJsonReviewService().review(
            draft = draft.copy(
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
            draftFile = sampleDraftPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "no-findings" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenDraftContentIsMissing() {
        val draft = validDraft()
        val report = LocalRendererManifestDraftJsonReviewService().review(
            draft = draft.copy(
                draftPanelManifestEntries = emptyList(),
                draftFieldCoverageSummary = draft.draftFieldCoverageSummary.copy(
                    panelEntries = 0,
                    totalDraftFields = 0,
                ),
                draftManifestSummary = draft.draftManifestSummary.copy(
                    draftPanelEntries = 0,
                    draftManifestFields = 0,
                ),
            ),
            draftFile = sampleDraftPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "draft-content-present" && it.status == "FAIL" })
    }

    @Test
    fun failsWhenSourceIdsAreMissing() {
        val report = LocalRendererManifestDraftJsonReviewService().review(
            draft = validDraft().copy(
                draftId = "",
                sourceManifestDraftPlanId = "",
                sourceStoryInputPackId = "",
            ),
            draftFile = sampleDraftPath.toString(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.checks.any { it.checkId == "source-draft-present" && it.status == "FAIL" })
        assertTrue(report.checks.any { it.checkId == "source-ids-present-and-aligned" && it.status == "FAIL" })
    }

    companion object {
        val sampleDraftPath: Path =
            locateSample("chenxing_local_renderer_manifest_draft_json_draft_sample.json")

        fun validDraft(): LocalRendererManifestDraftJsonDraft =
            readJson(sampleDraftPath)

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
