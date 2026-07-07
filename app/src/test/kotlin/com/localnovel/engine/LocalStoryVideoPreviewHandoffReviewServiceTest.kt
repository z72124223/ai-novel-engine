package com.localnovel.engine

import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffDraft
import com.localnovel.engine.service.LocalStoryVideoChainVerificationService
import com.localnovel.engine.service.LocalStoryVideoPreviewHandoffDraftService
import com.localnovel.engine.service.LocalStoryVideoPreviewHandoffReviewService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalStoryVideoPreviewHandoffReviewServiceTest {
    @Test
    fun passesVerifiedPreviewHandoffDraft() {
        val handoff = validHandoff()

        val report = LocalStoryVideoPreviewHandoffReviewService().review(
            handoff = handoff,
            handoffFile = "preview_handoff.json",
            reviewId = "preview-handoff-review-001",
        )

        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("preview-handoff-review-001", report.reviewId)
        assertEquals("preview_handoff.json", report.sourceHandoffFile)
        assertEquals(handoff.handoffId, report.sourceHandoffId)
        assertEquals(handoff.sourceStoryInputPackId, report.sourceStoryInputPackId)
        assertEquals("LOCAL_STORY_VIDEO_PREVIEW_HANDOFF_DRAFT", report.handoffStatus)
        assertEquals(10, report.checks.size)
        assertTrue(report.checks.all { it.status == "PASS" })
        assertEquals(9, report.readinessSummary.readinessChecks)
        assertEquals(9, report.readinessSummary.requiredReadinessPass)
        assertEquals(0, report.readinessSummary.requiredReadinessFail)
        assertEquals(3, report.readinessSummary.previewPanels)
        assertEquals(9000, report.readinessSummary.previewTargetDurationMillis)
        assertEquals(9000, report.readinessSummary.previewPanelDurationMillis)
        assertEquals(7, report.readinessSummary.qaChecks)
        assertEquals(0, report.readinessSummary.qaFail)
        assertFalse(report.rendererCompatibilitySummary.ready)
        assertTrue(report.rendererCompatibilitySummary.readyFalseAccepted)
        assertTrue(report.rendererCompatibilitySummary.missingFields.any { it.contains("adapter") })
        assertTrue(report.boundaries.fileProtocolJsonOnly)
        assertTrue(report.boundaries.noExternalPaidProviderTools)
        assertEquals("none", report.boundaries.providerInvocation)
        assertEquals("none", report.boundaries.ttsInvocation)
        assertFalse(report.boundaries.mediaGenerated)
        assertEquals("none", report.boundaries.rendererInvocation)
        assertFalse(report.boundaries.directSqliteWrites)
        assertFalse(report.boundaries.providerApiCalls)
        assertFalse(report.boundaries.finalMediaApproval)
        assertFalse(report.boundaries.release)
        assertFalse(report.boundaries.canonRegistryOrProductionLock)
        assertEquals("local_story_video_preview_handoff_review_only_not_media_generation", report.downstreamBoundary)
        assertEquals("local_renderer_adapter_dry_run", report.nextAllowedDownstreamStep)
        assertEquals(0, report.findingCount)
    }

    @Test
    fun failsWhenHandoffStatusIsWrong() {
        val report = LocalStoryVideoPreviewHandoffReviewService().review(
            handoff = validHandoff().copy(status = "FAIL"),
            handoffFile = "preview_handoff.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "handoff-status" && it.status == "FAIL" })
        assertTrue(report.findings.any { it.ruleId == "LOCAL_PREVIEW_HANDOFF_REVIEW_STATUS_NOT_DRAFT" })
    }

    @Test
    fun failsWhenRequiredReadinessDoesNotPass() {
        val handoff = validHandoff()
        val unsafe = handoff.copy(
            readinessChecks = handoff.readinessChecks.mapIndexed { index, check ->
                if (index == 0) check.copy(status = "FAIL") else check
            },
        )

        val report = LocalStoryVideoPreviewHandoffReviewService().review(
            handoff = unsafe,
            handoffFile = "preview_handoff.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "required-readiness-pass" && it.status == "FAIL" })
        assertTrue(report.findings.any { it.ruleId == "LOCAL_PREVIEW_HANDOFF_REVIEW_REQUIRED_READINESS_FAIL" })
    }

    @Test
    fun failsWhenMediaGeneratedBoundaryIsForged() {
        val handoff = validHandoff()
        val unsafe = handoff.copy(boundaries = handoff.boundaries.copy(mediaGenerated = true))

        val report = LocalStoryVideoPreviewHandoffReviewService().review(
            handoff = unsafe,
            handoffFile = "preview_handoff.json",
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.checks.any { it.checkId == "local-boundaries" && it.status == "FAIL" })
        assertTrue(report.findings.any { it.ruleId == "LOCAL_PREVIEW_HANDOFF_REVIEW_LOCAL_BOUNDARY_FAIL" })
    }

    companion object {
        fun validHandoff(): LocalStoryVideoPreviewHandoffDraft {
            val pack = LocalStoryInputPackValidationServiceTest.validPack()
            val chain = LocalStoryVideoChainVerificationService().verify(
                storyInputPack = pack,
                storyInputPackFile = "story_input.json",
                outputDir = "chain_outputs",
                chainReportFile = "chain_report.json",
            )
            return LocalStoryVideoPreviewHandoffDraftService().create(
                chainReport = chain.report,
                renderPackage = requireNotNull(chain.localRenderPackageDraft),
                assetPromptCards = requireNotNull(chain.assetPromptCardsDraft),
                storyInputPack = pack,
                chainReportFile = "chain_report.json",
                renderPackageFile = "local_render_package_draft.json",
                assetPromptCardsFile = "local_asset_prompt_cards_draft.json",
                storyInputFile = "story_input.json",
                handoffId = "preview-handoff-001",
            )
        }
    }
}
