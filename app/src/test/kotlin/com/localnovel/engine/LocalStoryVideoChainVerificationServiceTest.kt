package com.localnovel.engine

import com.localnovel.engine.service.LocalStoryVideoChainVerificationService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import java.nio.file.Path

class LocalStoryVideoChainVerificationServiceTest {
    @Test
    fun verifiesStoryToRenderChainWithoutMediaGeneration() {
        val pack = LocalStoryInputPackValidationServiceTest.validPack()
        val outputDir = Path.of("chain-output")
        val chainReportFile = outputDir.resolve("chain_report.json")

        val result = LocalStoryVideoChainVerificationService().verify(
            storyInputPack = pack,
            storyInputPackFile = "chenxing_story_input_pack_sample.json",
            outputDir = outputDir.toString(),
            chainReportFile = chainReportFile.toString(),
        )

        assertEquals("LOCAL_STORY_VIDEO_CHAIN_VERIFIED", result.report.status, result.report.findings.joinToString { it.ruleId })
        assertEquals(pack.packId, result.report.sourceStoryInputPackId)
        assertEquals("PASS", result.report.stageResults.storyValidation.status)
        assertEquals("LOCAL_ASSET_PROMPT_CARDS_DRAFT", result.report.stageResults.assetPromptCards.status)
        assertEquals("LOCAL_RENDER_PACKAGE_DRAFT", result.report.stageResults.localRenderPackage.status)
        assertEquals(outputDir.resolve("local_story_input_pack_validation_report.json").toString(), result.report.generatedFiles.storyValidationReportFile)
        assertEquals(outputDir.resolve("local_asset_prompt_cards_draft.json").toString(), result.report.generatedFiles.assetPromptCardsFile)
        assertEquals(outputDir.resolve("local_render_package_draft.json").toString(), result.report.generatedFiles.localRenderPackageFile)
        assertEquals(chainReportFile.toString(), result.report.generatedFiles.chainReportFile)
        assertEquals(3, result.report.counts.characters)
        assertEquals(1, result.report.counts.scenes)
        assertEquals(3, result.report.counts.beats)
        assertEquals(3, result.report.counts.visualPromptCards)
        assertEquals(3, result.report.counts.audioCueCards)
        assertEquals(3, result.report.counts.panelPlans)
        assertEquals(7, result.report.counts.qaChecks)
        assertEquals(7, result.report.counts.qaPass)
        assertEquals(0, result.report.counts.qaFail)
        assertEquals(9000, result.report.counts.targetDurationMillis)
        assertTrue(result.report.boundaries.fileProtocolJsonOnly)
        assertTrue(result.report.boundaries.noExternalPaidProviderTools)
        assertEquals("none", result.report.boundaries.providerInvocation)
        assertEquals("none", result.report.boundaries.ttsInvocation)
        assertFalse(result.report.boundaries.mediaGenerated)
        assertEquals("none", result.report.boundaries.rendererInvocation)
        assertEquals("local_story_video_chain_verified_only_not_media_generation", result.report.downstreamBoundary)
        assertEquals("local_chain_review", result.report.nextAllowedDownstreamStep)
        assertEquals(0, result.report.findingCount)
        assertEquals("PASS", result.storyValidationReport.verdict)
        assertNotNull(result.assetPromptCardsDraft)
        assertNotNull(result.localRenderPackageDraft)
    }

    @Test
    fun stopsChainWhenStoryValidationFails() {
        val pack = LocalStoryInputPackValidationServiceTest.validPack().copy(
            durationTargetMillis = 0,
        )

        val result = LocalStoryVideoChainVerificationService().verify(
            storyInputPack = pack,
            storyInputPackFile = "invalid_story_input_pack.json",
            outputDir = "chain-output",
            chainReportFile = "chain-output/chain_report.json",
        )

        assertEquals("FAIL", result.report.status)
        assertEquals("FAIL", result.report.stageResults.storyValidation.status)
        assertEquals("SKIPPED", result.report.stageResults.assetPromptCards.status)
        assertEquals("SKIPPED", result.report.stageResults.localRenderPackage.status)
        assertTrue(result.report.findings.any { it.ruleId == "LOCAL_CHAIN_STORY_VALIDATION_FAILED" })
        assertEquals("", result.report.nextAllowedDownstreamStep)
        assertEquals(0, result.report.counts.visualPromptCards)
        assertEquals(0, result.report.counts.panelPlans)
        assertEquals(null, result.assetPromptCardsDraft)
        assertEquals(null, result.localRenderPackageDraft)
    }
}
