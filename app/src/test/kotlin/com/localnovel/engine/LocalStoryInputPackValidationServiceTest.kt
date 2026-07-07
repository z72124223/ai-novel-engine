package com.localnovel.engine

import com.localnovel.engine.model.LocalStoryInputAudioStyle
import com.localnovel.engine.model.LocalStoryInputBeat
import com.localnovel.engine.model.LocalStoryInputCharacter
import com.localnovel.engine.model.LocalStoryInputConstraints
import com.localnovel.engine.model.LocalStoryInputPack
import com.localnovel.engine.model.LocalStoryInputPackValidationReport
import com.localnovel.engine.model.LocalStoryInputScene
import com.localnovel.engine.model.LocalStoryInputVisualStyle
import com.localnovel.engine.service.LocalStoryInputPackValidationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalStoryInputPackValidationServiceTest {
    @Test
    fun validatesCleanLocalStoryInputPack() {
        val report = LocalStoryInputPackValidationService().validate(
            pack = validPack(),
            packFile = "rain_gate_story_input_pack_sample.json",
            validationId = "rain-gate-story-input-validation-001",
        )

        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("rain-gate-story-input-validation-001", report.validationId)
        assertEquals("local_story_input_pack_validation_only", report.downstreamBoundary)
        assertEquals("local_story_input_pack_validated_for_review", report.nextAllowedDownstreamStep)
        assertEquals(2, report.evidence.characterCount)
        assertEquals(1, report.evidence.sceneCount)
        assertEquals(2, report.evidence.beatCount)
        assertEquals(2, report.evidence.linkedBeatCount)
        assertEquals(9000, report.evidence.durationTargetMillis)
        assertTrue(report.evidence.localOnlyDeclared)
        assertTrue(report.evidence.noExternalPaidProviderToolsDeclared)
        assertTrue(report.evidence.fileProtocolJsonOnlyDeclared)
        assertEquals(0, report.findingCount)
        assertSafeBoundary(report)
    }

    @Test
    fun missingLocalOnlyConstraintFailsValidation() {
        val pack = validPack().copy(
            constraints = validPack().constraints.copy(localOnly = false),
        )
        val report = LocalStoryInputPackValidationService().validate(pack)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "LOCAL_STORY_LOCAL_ONLY_NOT_DECLARED" })
        assertEquals("", report.nextAllowedDownstreamStep)
    }

    @Test
    fun beatMustReferenceExistingScene() {
        val pack = validPack().copy(
            beats = listOf(validPack().beats.first().copy(sceneId = "missing-scene")),
        )
        val report = LocalStoryInputPackValidationService().validate(pack)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "LOCAL_STORY_BEAT_SCENE_REF_MISSING" })
        assertEquals(1, report.evidence.crossReferenceMismatchCount)
    }

    @Test
    fun forbiddenReleaseOrProductionLockClaimFailsValidation() {
        val pack = validPack().copy(
            title = "Rain Gate release with production lock",
        )
        val report = LocalStoryInputPackValidationService().validate(pack)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "LOCAL_STORY_FORBIDDEN_BOUNDARY_CLAIM" })
        assertTrue(report.evidence.forbiddenBoundaryClaimCount >= 1)
    }

    companion object {
        fun validPack(): LocalStoryInputPack =
            LocalStoryInputPack(
                packId = "local-story-input-rain-gate-001",
                title = "Rain Gate local story input",
                locale = "zh-TW",
                worldPremise = "A checkpoint at the rain gate only reacts to visible marks and spoken choices.",
                characters = listOf(
                    LocalStoryInputCharacter(
                        id = "char-lin",
                        name = "Lin",
                        role = "protagonist",
                        visualCue = "Wet dark coat, visible silver wrist mark, tired but alert posture.",
                        voiceCue = "Calm low voice with restrained urgency.",
                        publicMotivation = "Enter the gate without hiding the visible mark.",
                    ),
                    LocalStoryInputCharacter(
                        id = "char-gatekeeper",
                        name = "Gatekeeper",
                        role = "threshold keeper",
                        visualCue = "Lantern held at chest height, face half-lit by rain.",
                        voiceCue = "Dry official tone, short questions.",
                        publicMotivation = "Confirm whether Lin is allowed to pass.",
                    ),
                ),
                scenes = listOf(
                    LocalStoryInputScene(
                        id = "scene-rain-gate",
                        title = "Rain Gate Checkpoint",
                        location = "Stone gate under heavy rain",
                        mood = "tense, quiet, watchful",
                        visibleSituation = "Lin steps into the lantern light while the gatekeeper studies the wrist mark.",
                    ),
                ),
                beats = listOf(
                    LocalStoryInputBeat(
                        id = "beat-arrival",
                        sceneId = "scene-rain-gate",
                        characterIds = listOf("char-lin", "char-gatekeeper"),
                        narrativeIntent = "Establish the wet checkpoint and the visible mark as the only immediate proof.",
                        dialogueIntent = "Gatekeeper asks Lin to keep the mark visible.",
                        visualIntent = "Medium shot with lantern reflection on the wrist mark.",
                        audioIntent = "Rain bed with a short lantern chain sound.",
                    ),
                    LocalStoryInputBeat(
                        id = "beat-answer",
                        sceneId = "scene-rain-gate",
                        characterIds = listOf("char-lin"),
                        narrativeIntent = "Lin chooses honesty instead of concealing the mark.",
                        dialogueIntent = "Lin says she will keep the mark in the light.",
                        visualIntent = "Close shot of Lin raising her wrist into the lantern glow.",
                        audioIntent = "Voice stays steady over rain.",
                    ),
                ),
                visualStyle = LocalStoryInputVisualStyle(
                    tone = "grounded short-drama tension",
                    colorPalette = "cold rain blue, lantern amber, dark stone",
                    cameraNotes = "Readable medium shots and one close shot; no fast cuts.",
                    subtitleStyle = "Traditional Chinese subtitles, high contrast, one line when possible.",
                ),
                audioStyle = LocalStoryInputAudioStyle(
                    voiceDirection = "Natural spoken Traditional Chinese, restrained urgency.",
                    musicMood = "Minimal low pulse, not heroic.",
                    soundDesign = "Rain, small chain movement, soft gate echo.",
                ),
                durationTargetMillis = 9000,
                constraints = LocalStoryInputConstraints(
                    localOnly = true,
                    noExternalPaidProviderTools = true,
                    fileProtocolJsonOnly = true,
                    notes = listOf(
                        "local-only story input for the repo CLI",
                        "no external paid/provider tools",
                        "file-protocol JSON only",
                    ),
                ),
            )

        private fun assertSafeBoundary(report: LocalStoryInputPackValidationReport) {
            val text = JsonSupport.json.encodeToString(report)
            listOf(
                "final media approval",
                "release",
                "canon",
                "registry",
                "production lock",
                "provider job",
                "api response",
                "generated video",
            ).forEach { forbidden ->
                assertFalse(text.contains(forbidden, ignoreCase = true), "Forbidden boundary text leaked: $forbidden")
            }
        }
    }
}
