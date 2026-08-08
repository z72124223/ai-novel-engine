package com.localnovel.engine

import com.localnovel.engine.service.LocalAssetPromptCardDraftService
import com.localnovel.engine.service.LocalStoryInputPackValidationService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocalAssetPromptCardDraftServiceTest {
    @Test
    fun draftsCardsFromPassStoryInputPackValidation() {
        val pack = LocalStoryInputPackValidationServiceTest.validPack()
        val validation = LocalStoryInputPackValidationService().validate(
            pack = pack,
            packFile = "chenxing_story_input_pack_sample.json",
            validationId = "chenxing-story-input-validation-001",
        )

        val draft = LocalAssetPromptCardDraftService().create(
            storyInputPack = pack,
            storyValidationReport = validation,
            storyInputPackFile = "chenxing_story_input_pack_sample.json",
            storyValidationReportFile = "chenxing_story_input_pack_validation_sample.json",
        )

        assertEquals("LOCAL_ASSET_PROMPT_CARDS_DRAFT", draft.status, draft.findings.joinToString { it.ruleId })
        assertEquals("local-story-input-chenxing-v0-001", draft.sourceStoryInputPackId)
        assertEquals("chenxing-story-input-validation-001", draft.sourceStoryValidationId)
        assertEquals("local_story_input_pack_validation_only", validation.downstreamBoundary)
        assertEquals("local_asset_prompt_cards_draft_only", draft.downstreamBoundary)
        assertEquals("local_asset_prompt_cards_review", draft.nextAllowedDownstreamStep)
        assertEquals(3, draft.characterCards.size)
        assertEquals(1, draft.sceneCards.size)
        assertEquals(3, draft.beatCards.size)
        assertEquals(3, draft.visualPromptCards.size)
        assertEquals(3, draft.audioCueCards.size)
        assertEquals(9000, draft.renderIntent.targetDurationMillis)
        assertTrue(draft.localOnlyConstraints.localOnly)
        assertTrue(draft.localOnlyConstraints.noExternalPaidProviderTools)
        assertTrue(draft.localOnlyConstraints.fileProtocolJsonOnly)
        assertTrue(draft.boundaries.contains("no provider/API"))
        assertTrue(draft.boundaries.contains("no media generation"))
        assertTrue(draft.boundaries.contains("no production lock"))
        assertTrue(draft.characterCards.first { it.sourceCharacterId == "char-luozhixing" }.visualCue.contains("修理"))
        assertTrue(draft.characterCards.first { it.sourceCharacterId == "char-luozhixing" }.voiceCue.contains("低而穩"))
        assertEquals(
            listOf(
                "local-character-card-char-luozhixing",
                "local-character-card-char-aijiang",
                "local-character-card-char-old-woman",
            ),
            draft.beatCards.first().characterCardIds,
        )
        assertTrue(draft.visualPromptCards.all { it.providerInvocation == "none" && it.submissionStatus == "not_submitted" })
        assertTrue(draft.audioCueCards.all { it.ttsInvocation == "none" })
        assertEquals(0, draft.findingCount)
        assertEquals(3, draft.evidence.characterCardCount)
        assertEquals(3, draft.evidence.visualPromptCardCount)
        assertEquals("PASS", draft.evidence.storyValidationVerdict)
    }

    @Test
    fun refusesToDraftWhenStoryValidationIsNotPass() {
        val pack = LocalStoryInputPackValidationServiceTest.validPack()
        val validation = LocalStoryInputPackValidationService().validate(pack).copy(
            verdict = "FAIL",
            blockedReason = "forced test failure",
        )

        val draft = LocalAssetPromptCardDraftService().create(
            storyInputPack = pack,
            storyValidationReport = validation,
        )

        assertEquals("FAIL", draft.status)
        assertTrue(draft.findings.any { it.ruleId == "LOCAL_ASSET_PROMPT_STORY_VALIDATION_NOT_PASS" })
        assertEquals(0, draft.characterCards.size)
        assertEquals("", draft.nextAllowedDownstreamStep)
    }

    @Test
    fun refusesToDraftWhenValidationPackIdDoesNotMatchStoryInputPack() {
        val pack = LocalStoryInputPackValidationServiceTest.validPack()
        val validation = LocalStoryInputPackValidationService().validate(pack).copy(
            packId = "other-pack",
        )

        val draft = LocalAssetPromptCardDraftService().create(
            storyInputPack = pack,
            storyValidationReport = validation,
        )

        assertEquals("FAIL", draft.status)
        assertTrue(draft.findings.any { it.ruleId == "LOCAL_ASSET_PROMPT_STORY_PACK_ID_MISMATCH" })
        assertEquals(0, draft.visualPromptCards.size)
    }
}
