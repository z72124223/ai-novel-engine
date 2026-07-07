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
            packFile = "rain_gate_story_input_pack_sample.json",
            validationId = "rain-gate-story-input-validation-001",
        )

        val draft = LocalAssetPromptCardDraftService().create(
            storyInputPack = pack,
            storyValidationReport = validation,
            storyInputPackFile = "rain_gate_story_input_pack_sample.json",
            storyValidationReportFile = "rain_gate_story_input_pack_validation_sample.json",
        )

        assertEquals("LOCAL_ASSET_PROMPT_CARDS_DRAFT", draft.status, draft.findings.joinToString { it.ruleId })
        assertEquals("local-story-input-rain-gate-001", draft.sourceStoryInputPackId)
        assertEquals("rain-gate-story-input-validation-001", draft.sourceStoryValidationId)
        assertEquals("local_story_input_pack_validation_only", validation.downstreamBoundary)
        assertEquals("local_asset_prompt_cards_draft_only", draft.downstreamBoundary)
        assertEquals("local_asset_prompt_cards_review", draft.nextAllowedDownstreamStep)
        assertEquals(2, draft.characterCards.size)
        assertEquals(1, draft.sceneCards.size)
        assertEquals(2, draft.beatCards.size)
        assertEquals(2, draft.visualPromptCards.size)
        assertEquals(2, draft.audioCueCards.size)
        assertEquals(9000, draft.renderIntent.targetDurationMillis)
        assertTrue(draft.localOnlyConstraints.localOnly)
        assertTrue(draft.localOnlyConstraints.noExternalPaidProviderTools)
        assertTrue(draft.localOnlyConstraints.fileProtocolJsonOnly)
        assertTrue(draft.boundaries.contains("no provider/API"))
        assertTrue(draft.boundaries.contains("no media generation"))
        assertTrue(draft.boundaries.contains("no production lock"))
        assertTrue(draft.characterCards.first { it.sourceCharacterId == "char-lin" }.visualCue.contains("silver wrist mark"))
        assertTrue(draft.characterCards.first { it.sourceCharacterId == "char-lin" }.voiceCue.contains("Calm low voice"))
        assertEquals(listOf("local-character-card-char-lin", "local-character-card-char-gatekeeper"), draft.beatCards.first().characterCardIds)
        assertTrue(draft.visualPromptCards.all { it.providerInvocation == "none" && it.submissionStatus == "not_submitted" })
        assertTrue(draft.audioCueCards.all { it.ttsInvocation == "none" })
        assertEquals(0, draft.findingCount)
        assertEquals(2, draft.evidence.characterCardCount)
        assertEquals(2, draft.evidence.visualPromptCardCount)
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
