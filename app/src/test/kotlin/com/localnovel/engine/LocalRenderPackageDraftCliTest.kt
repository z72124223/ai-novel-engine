package com.localnovel.engine

import com.localnovel.engine.model.LocalRenderPackageDraft
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocalRenderPackageDraftCliTest {
    @Test
    fun cliWritesLocalRenderPackageDraft() {
        val tempDir = Files.createTempDirectory("local-render-package-draft")
        val storyInput = tempDir.resolve("rain_gate_story_input_pack_sample.json")
        val assetPromptCards = tempDir.resolve("rain_gate_asset_prompt_cards_draft_sample.json")
        val out = tempDir.resolve("rain_gate_local_render_package_draft_sample.json")
        val pack = LocalStoryInputPackValidationServiceTest.validPack()
        val cards = LocalRenderPackageDraftServiceTest.validCards()
        writeJson(storyInput, pack)
        writeJson(assetPromptCards, cards)

        val exit = Cli().run(
            listOf(
                "draft-local-render-package",
                "--story-input",
                storyInput.toString(),
                "--asset-prompt-cards",
                assetPromptCards.toString(),
                "--package",
                "rain-gate-local-render-package-v0",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val draft = readJson<LocalRenderPackageDraft>(out)
        assertEquals("LOCAL_RENDER_PACKAGE_DRAFT", draft.status, draft.findings.joinToString { it.ruleId })
        assertEquals("rain-gate-local-render-package-v0", draft.packageId)
        assertEquals(pack.packId, draft.sourceStoryInputPackId)
        assertEquals("LOCAL_ASSET_PROMPT_CARDS_DRAFT", draft.sourceAssetPromptCardsStatus)
        assertEquals(2, draft.timelinePlan.size)
        assertEquals(1, draft.scenePlan.size)
        assertEquals(2, draft.panelPlan.size)
        assertEquals(2, draft.assetPromptReferences.size)
        assertTrue(draft.qaChecklist.all { it.status == "PASS" })
        assertEquals(9000, draft.evidence.timelineTotalDurationMillis)
        assertEquals("local_render_package_draft_only", draft.downstreamBoundary)
        assertEquals("local_render_package_review", draft.nextAllowedDownstreamStep)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
