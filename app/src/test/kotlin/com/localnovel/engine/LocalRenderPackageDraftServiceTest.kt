package com.localnovel.engine

import com.localnovel.engine.service.LocalAssetPromptCardDraftService
import com.localnovel.engine.service.LocalRenderPackageDraftService
import com.localnovel.engine.service.LocalStoryInputPackValidationService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocalRenderPackageDraftServiceTest {
    @Test
    fun draftsRenderPackageFromStoryInputAndAssetPromptCards() {
        val pack = LocalStoryInputPackValidationServiceTest.validPack()
        val cards = validCards()

        val draft = LocalRenderPackageDraftService().create(
            storyInputPack = pack,
            assetPromptCards = cards,
            storyInputPackFile = "rain_gate_story_input_pack_sample.json",
            assetPromptCardsFile = "rain_gate_asset_prompt_cards_draft_sample.json",
        )

        assertEquals("LOCAL_RENDER_PACKAGE_DRAFT", draft.status, draft.findings.joinToString { it.ruleId })
        assertEquals("local-story-input-rain-gate-001", draft.sourceStoryInputPackId)
        assertEquals("LOCAL_ASSET_PROMPT_CARDS_DRAFT", draft.sourceAssetPromptCardsStatus)
        assertEquals("local_render_package_draft_only_not_media_generation", draft.renderBoundary)
        assertEquals("local_render_package_draft_only", draft.downstreamBoundary)
        assertEquals("local_render_package_review", draft.nextAllowedDownstreamStep)
        assertEquals(2, draft.timelinePlan.size)
        assertEquals(1, draft.scenePlan.size)
        assertEquals(2, draft.panelPlan.size)
        assertEquals(2, draft.assetPromptReferences.size)
        assertEquals(7, draft.qaChecklist.size)
        assertTrue(draft.qaChecklist.all { it.status == "PASS" })
        assertEquals(9000, draft.evidence.timelineTotalDurationMillis)
        assertEquals(4500, draft.timelinePlan[0].durationMillis)
        assertEquals(0, draft.timelinePlan[0].startMillis)
        assertEquals(4500, draft.timelinePlan[0].endMillis)
        assertEquals(4500, draft.timelinePlan[1].startMillis)
        assertEquals(9000, draft.timelinePlan[1].endMillis)
        assertEquals("local-visual-prompt-beat-arrival", draft.panelPlan[0].visualPromptCardId)
        assertEquals("local-audio-cue-beat-arrival", draft.panelPlan[0].audioCueCardId)
        assertTrue(draft.localOnlyConstraints.localOnly)
        assertTrue(draft.localOnlyConstraints.noExternalPaidProviderTools)
        assertTrue(draft.localOnlyConstraints.fileProtocolJsonOnly)
        assertEquals(0, draft.findingCount)
    }

    @Test
    fun refusesCardsWithoutDraftStatus() {
        val pack = LocalStoryInputPackValidationServiceTest.validPack()
        val cards = validCards().copy(status = "FAIL")

        val draft = LocalRenderPackageDraftService().create(
            storyInputPack = pack,
            assetPromptCards = cards,
        )

        assertEquals("FAIL", draft.status)
        assertTrue(draft.findings.any { it.ruleId == "LOCAL_RENDER_ASSET_PROMPT_STATUS_NOT_DRAFT" })
        assertTrue(draft.qaChecklist.any { it.checkId == "card-status" && it.status == "FAIL" })
        assertEquals(0, draft.timelinePlan.size)
        assertEquals("", draft.nextAllowedDownstreamStep)
    }

    @Test
    fun refusesCardsFromDifferentStoryInputPack() {
        val pack = LocalStoryInputPackValidationServiceTest.validPack()
        val cards = validCards().copy(sourceStoryInputPackId = "other-pack")

        val draft = LocalRenderPackageDraftService().create(
            storyInputPack = pack,
            assetPromptCards = cards,
        )

        assertEquals("FAIL", draft.status)
        assertTrue(draft.findings.any { it.ruleId == "LOCAL_RENDER_STORY_PACK_ID_MISMATCH" })
        assertTrue(draft.qaChecklist.any { it.checkId == "story-pack-match" && it.status == "FAIL" })
        assertEquals(0, draft.panelPlan.size)
    }

    companion object {
        fun validCards() =
            LocalAssetPromptCardDraftService().create(
                storyInputPack = LocalStoryInputPackValidationServiceTest.validPack(),
                storyValidationReport = LocalStoryInputPackValidationService().validate(
                    pack = LocalStoryInputPackValidationServiceTest.validPack(),
                    validationId = "rain-gate-story-input-validation-001",
                ),
                storyInputPackFile = "rain_gate_story_input_pack_sample.json",
                storyValidationReportFile = "rain_gate_story_input_pack_validation_sample.json",
            )
    }
}
