package com.localnovel.engine

import com.localnovel.engine.service.LocalStoryVideoChainVerificationService
import com.localnovel.engine.service.LocalStoryVideoPreviewHandoffDraftService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalStoryVideoPreviewHandoffDraftServiceTest {
    @Test
    fun draftsPreviewHandoffFromVerifiedLocalStoryVideoChain() {
        val pack = LocalStoryInputPackValidationServiceTest.validPack()
        val chain = LocalStoryVideoChainVerificationService().verify(
            storyInputPack = pack,
            storyInputPackFile = "story_input.json",
            outputDir = "chain_outputs",
            chainReportFile = "chain_report.json",
        )

        val draft = LocalStoryVideoPreviewHandoffDraftService().create(
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

        assertEquals("LOCAL_STORY_VIDEO_PREVIEW_HANDOFF_DRAFT", draft.status, draft.findings.joinToString { it.ruleId })
        assertEquals("preview-handoff-001", draft.handoffId)
        assertEquals(pack.packId, draft.sourceStoryInputPackId)
        assertEquals("LOCAL_STORY_VIDEO_CHAIN_VERIFIED", draft.sourceChainStatus)
        assertEquals("LOCAL_RENDER_PACKAGE_DRAFT", draft.sourceRenderPackageStatus)
        assertEquals("LOCAL_ASSET_PROMPT_CARDS_DRAFT", draft.sourceAssetPromptCardsStatus)
        assertEquals(9, draft.readinessChecks.size)
        assertEquals(9, draft.evidence.readinessPassCount)
        assertEquals(0, draft.evidence.readinessFailCount)
        assertEquals(1, draft.previewPlan.sceneOrdering.size)
        assertEquals(3, draft.previewPlan.panelOrdering.size)
        assertEquals(9000, draft.previewPlan.targetDurationMillis)
        assertEquals(7, draft.previewPlan.qaSummary.qaChecks)
        assertFalse(draft.localRendererCompatibility.ready)
        assertTrue(draft.localRendererCompatibility.missingFields.any { it.contains("ComicVideoRenderHandoffManifest") })
        assertTrue(draft.boundaries.fileProtocolJsonOnly)
        assertTrue(draft.boundaries.noExternalPaidProviderTools)
        assertEquals("none", draft.boundaries.providerInvocation)
        assertEquals("none", draft.boundaries.ttsInvocation)
        assertFalse(draft.boundaries.mediaGenerated)
        assertEquals("none", draft.boundaries.rendererInvocation)
        assertEquals("local_story_video_preview_handoff_draft_only_not_media_generation", draft.downstreamBoundary)
        assertEquals("local_preview_handoff_review", draft.nextAllowedDownstreamStep)
        assertEquals(0, draft.findingCount)
    }

    @Test
    fun failsWhenChainReportIsNotVerified() {
        val pack = LocalStoryInputPackValidationServiceTest.validPack()
        val chain = LocalStoryVideoChainVerificationService().verify(
            storyInputPack = pack,
            storyInputPackFile = "story_input.json",
            outputDir = "chain_outputs",
            chainReportFile = "chain_report.json",
        )

        val draft = LocalStoryVideoPreviewHandoffDraftService().create(
            chainReport = chain.report.copy(status = "FAIL"),
            renderPackage = requireNotNull(chain.localRenderPackageDraft),
            assetPromptCards = requireNotNull(chain.assetPromptCardsDraft),
            storyInputPack = pack,
            chainReportFile = "chain_report.json",
            renderPackageFile = "local_render_package_draft.json",
            assetPromptCardsFile = "local_asset_prompt_cards_draft.json",
            storyInputFile = "story_input.json",
        )

        assertEquals("FAIL", draft.status)
        assertTrue(draft.readinessChecks.any { it.checkId == "chain-status" && it.status == "FAIL" })
        assertTrue(draft.findings.any { it.ruleId == "LOCAL_PREVIEW_HANDOFF_CHAIN_STATUS_NOT_VERIFIED" })
        assertEquals("", draft.nextAllowedDownstreamStep)
        assertEquals(0, draft.previewPlan.panelOrdering.size)
        assertFalse(draft.boundaries.mediaGenerated)
    }
}
