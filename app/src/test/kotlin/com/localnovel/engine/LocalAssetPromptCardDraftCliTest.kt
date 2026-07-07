package com.localnovel.engine

import com.localnovel.engine.model.LocalAssetPromptCardsDraft
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocalAssetPromptCardDraftCliTest {
    @Test
    fun cliWritesLocalAssetPromptCardsDraft() {
        val tempDir = Files.createTempDirectory("local-asset-prompt-cards")
        val storyInput = tempDir.resolve("rain_gate_story_input_pack_sample.json")
        val storyValidation = tempDir.resolve("rain_gate_story_input_pack_validation_sample.json")
        val out = tempDir.resolve("rain_gate_asset_prompt_cards_draft_sample.json")
        val pack = LocalStoryInputPackValidationServiceTest.validPack()
        val validation = com.localnovel.engine.service.LocalStoryInputPackValidationService().validate(
            pack = pack,
            packFile = storyInput.toString(),
            validationId = "local-story-input-rain-gate-validation-001",
        )
        writeJson(storyInput, pack)
        writeJson(storyValidation, validation)

        val exit = Cli().run(
            listOf(
                "draft-local-asset-prompt-cards",
                "--story-input",
                storyInput.toString(),
                "--story-validation",
                storyValidation.toString(),
                "--draft",
                "rain-gate-asset-prompt-cards-v0",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val draft = readJson<LocalAssetPromptCardsDraft>(out)
        assertEquals("LOCAL_ASSET_PROMPT_CARDS_DRAFT", draft.status, draft.findings.joinToString { it.ruleId })
        assertEquals("rain-gate-asset-prompt-cards-v0", draft.draftId)
        assertEquals(pack.packId, draft.sourceStoryInputPackId)
        assertEquals(validation.validationId, draft.sourceStoryValidationId)
        assertEquals(2, draft.characterCards.size)
        assertEquals(1, draft.sceneCards.size)
        assertEquals(2, draft.beatCards.size)
        assertEquals(2, draft.visualPromptCards.size)
        assertEquals(2, draft.audioCueCards.size)
        assertTrue(draft.visualPromptCards.all { it.providerInvocation == "none" })
        assertTrue(draft.audioCueCards.all { it.ttsInvocation == "none" })
        assertTrue(draft.boundaries.contains("no provider/API"))
        assertTrue(draft.boundaries.contains("no media generation"))
        assertTrue(draft.boundaries.contains("no production lock"))
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
