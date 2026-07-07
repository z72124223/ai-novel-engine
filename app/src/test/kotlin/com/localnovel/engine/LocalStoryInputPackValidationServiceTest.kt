package com.localnovel.engine

import com.localnovel.engine.model.LocalStoryInputPack
import com.localnovel.engine.model.LocalStoryInputPackValidationReport
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
            packFile = "chenxing_story_input_pack_sample.json",
            validationId = "chenxing-story-input-validation-001",
        )

        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("chenxing-story-input-validation-001", report.validationId)
        assertEquals("local_story_input_pack_validation_only", report.downstreamBoundary)
        assertEquals("local_story_input_pack_validated_for_review", report.nextAllowedDownstreamStep)
        assertEquals(3, report.evidence.characterCount)
        assertEquals(1, report.evidence.sceneCount)
        assertEquals(3, report.evidence.beatCount)
        assertEquals(3, report.evidence.linkedBeatCount)
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
            title = "Final release with production lock",
        )
        val report = LocalStoryInputPackValidationService().validate(pack)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "LOCAL_STORY_FORBIDDEN_BOUNDARY_CLAIM" })
        assertTrue(report.evidence.forbiddenBoundaryClaimCount >= 1)
    }

    companion object {
        fun validPack(): LocalStoryInputPack = ChenxingStoryTestFixtures.chenxingStoryInputPack()

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