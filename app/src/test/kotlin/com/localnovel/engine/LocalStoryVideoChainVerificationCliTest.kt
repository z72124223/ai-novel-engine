package com.localnovel.engine

import com.localnovel.engine.model.LocalAssetPromptCardsDraft
import com.localnovel.engine.model.LocalRenderPackageDraft
import com.localnovel.engine.model.LocalStoryInputPackValidationReport
import com.localnovel.engine.model.LocalStoryVideoChainReport
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalStoryVideoChainVerificationCliTest {
    @Test
    fun cliWritesStoryVideoChainArtifactsAndReport() {
        val tempDir = Files.createTempDirectory("local-story-video-chain")
        val storyInput = tempDir.resolve("rain_gate_story_input_pack_sample.json")
        val outputDir = tempDir.resolve("chain_outputs")
        val reportPath = tempDir.resolve("rain_gate_story_video_chain_report_sample.json")
        writeJson(storyInput, LocalStoryInputPackValidationServiceTest.validPack())

        val exit = Cli().run(
            listOf(
                "verify-local-story-video-chain",
                "--story-input",
                storyInput.toString(),
                "--output-dir",
                outputDir.toString(),
                "--chain",
                "rain-gate-story-video-chain-v0",
                "--out",
                reportPath.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(reportPath))
        val report = readJson<LocalStoryVideoChainReport>(reportPath)
        assertEquals("LOCAL_STORY_VIDEO_CHAIN_VERIFIED", report.status, report.findings.joinToString { it.ruleId })
        assertEquals("rain-gate-story-video-chain-v0", report.chainId)
        assertEquals("PASS", report.stageResults.storyValidation.status)
        assertEquals("LOCAL_ASSET_PROMPT_CARDS_DRAFT", report.stageResults.assetPromptCards.status)
        assertEquals("LOCAL_RENDER_PACKAGE_DRAFT", report.stageResults.localRenderPackage.status)
        assertTrue(Files.exists(Path.of(report.generatedFiles.storyValidationReportFile)))
        assertTrue(Files.exists(Path.of(report.generatedFiles.assetPromptCardsFile)))
        assertTrue(Files.exists(Path.of(report.generatedFiles.localRenderPackageFile)))
        assertEquals(2, report.counts.visualPromptCards)
        assertEquals(2, report.counts.audioCueCards)
        assertEquals(2, report.counts.panelPlans)
        assertEquals(7, report.counts.qaChecks)
        assertEquals(7, report.counts.qaPass)
        assertEquals(0, report.counts.qaFail)
        assertEquals(9000, report.counts.targetDurationMillis)
        assertTrue(report.boundaries.fileProtocolJsonOnly)
        assertTrue(report.boundaries.noExternalPaidProviderTools)
        assertEquals("none", report.boundaries.providerInvocation)
        assertEquals("none", report.boundaries.ttsInvocation)
        assertFalse(report.boundaries.mediaGenerated)
        assertEquals("none", report.boundaries.rendererInvocation)

        val validation = readJson<LocalStoryInputPackValidationReport>(Path.of(report.generatedFiles.storyValidationReportFile))
        val cards = readJson<LocalAssetPromptCardsDraft>(Path.of(report.generatedFiles.assetPromptCardsFile))
        val renderPackage = readJson<LocalRenderPackageDraft>(Path.of(report.generatedFiles.localRenderPackageFile))
        assertEquals("PASS", validation.verdict)
        assertEquals("LOCAL_ASSET_PROMPT_CARDS_DRAFT", cards.status)
        assertEquals("LOCAL_RENDER_PACKAGE_DRAFT", renderPackage.status)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
