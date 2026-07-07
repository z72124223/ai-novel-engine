package com.localnovel.engine

import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffDraft
import com.localnovel.engine.service.LocalStoryVideoChainVerificationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalStoryVideoPreviewHandoffDraftCliTest {
    @Test
    fun cliWritesPreviewHandoffDraft() {
        val tempDir = Files.createTempDirectory("local-story-video-preview-handoff")
        val storyInput = tempDir.resolve("story_input.json")
        val chainReport = tempDir.resolve("chain_report.json")
        val renderPackage = tempDir.resolve("local_render_package_draft.json")
        val assetPromptCards = tempDir.resolve("local_asset_prompt_cards_draft.json")
        val handoff = tempDir.resolve("preview_handoff.json")

        val pack = LocalStoryInputPackValidationServiceTest.validPack()
        val chain = LocalStoryVideoChainVerificationService().verify(
            storyInputPack = pack,
            storyInputPackFile = storyInput.toString(),
            outputDir = tempDir.resolve("chain_outputs").toString(),
            chainReportFile = chainReport.toString(),
        )
        writeJson(storyInput, pack)
        writeJson(chainReport, chain.report)
        writeJson(renderPackage, requireNotNull(chain.localRenderPackageDraft))
        writeJson(assetPromptCards, requireNotNull(chain.assetPromptCardsDraft))

        val exit = Cli().run(
            listOf(
                "draft-local-story-video-preview-handoff",
                "--chain-report",
                chainReport.toString(),
                "--render-package",
                renderPackage.toString(),
                "--asset-prompt-cards",
                assetPromptCards.toString(),
                "--story-input",
                storyInput.toString(),
                "--handoff",
                "preview-handoff-cli-001",
                "--out",
                handoff.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(handoff))
        val draft = readJson<LocalStoryVideoPreviewHandoffDraft>(handoff)
        assertEquals("LOCAL_STORY_VIDEO_PREVIEW_HANDOFF_DRAFT", draft.status, draft.findings.joinToString { it.ruleId })
        assertEquals("preview-handoff-cli-001", draft.handoffId)
        assertEquals(pack.packId, draft.sourceStoryInputPackId)
        assertEquals(2, draft.previewPlan.panelOrdering.size)
        assertFalse(draft.localRendererCompatibility.ready)
        assertEquals("none", draft.boundaries.providerInvocation)
        assertEquals("none", draft.boundaries.ttsInvocation)
        assertFalse(draft.boundaries.mediaGenerated)
        assertEquals("none", draft.boundaries.rendererInvocation)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
