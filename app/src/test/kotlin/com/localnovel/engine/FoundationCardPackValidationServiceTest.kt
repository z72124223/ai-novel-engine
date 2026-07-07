package com.localnovel.engine

import com.localnovel.engine.model.FoundationCardPack
import com.localnovel.engine.model.FoundationCardPackValidationReport
import com.localnovel.engine.support.JsonSupport
import com.localnovel.engine.service.FoundationCardPackValidationService
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class FoundationCardPackValidationServiceTest {
    @Test
    fun validatesRoundOneContractSample() {
        val report = FoundationCardPackValidationService().validateFile(
            inputPath = samplePackPath,
            validationId = "rain-gate-09-foundation-card-pack-validation-v0-001",
        )

        assertPassReport(report)
        assertEquals("rain-gate-09-foundation-card-pack-validation-v0-001", report.validationId)
        assertEquals("09-foundation-pack-rain-gate-v0-001", report.packId)
        assertEquals(7, report.evidence.cardCount)
        assertEquals(7, report.evidence.presentRequiredFamilyCount)
        assertTrue(report.evidence.missingFamilies.isEmpty())
        assertEquals(0, report.evidence.duplicateCardIdCount)
        assertEquals(0, report.evidence.missingSourceNotesCount)
        assertEquals(0, report.evidence.missingRetrievalTagsCount)
        assertEquals(0, report.evidence.missingDownstreamUseCount)
        assertEquals(0, report.evidence.unsafeBoundaryFlagCount)
        assertEquals(0, report.evidence.readinessClaimCount)
        assertEquals(0, report.evidence.eligibilityClaimCount)
    }

    @Test
    fun missingPackIdFailsValidation() {
        val report = FoundationCardPackValidationService().validate(validPack().copy(packId = ""))

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "FOUNDATION_CARD_PACK_ID_MISSING" })
        assertEquals("", report.nextAllowedDownstreamStep)
    }

    @Test
    fun wrongStatusFailsValidation() {
        val report = FoundationCardPackValidationService().validate(
            validPack().copy(status = "FOUNDATION_CARD_PACK_APPROVED"),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "FOUNDATION_CARD_PACK_METADATA_INVALID" })
    }

    @Test
    fun missingRequiredFamilyFailsValidation() {
        val pack = validPack().copy(cards = validPack().cards.filterNot { it.family == "situation_seed" })
        val report = FoundationCardPackValidationService().validate(pack)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.evidence.missingFamilies.contains("situation_seed"))
        assertTrue(report.findings.any { it.ruleId == "FOUNDATION_CARD_REQUIRED_FAMILY_MISSING" })
    }

    @Test
    fun duplicateCardIdsFailValidation() {
        val cards = validPack().cards
        val pack = validPack().copy(cards = cards + cards.first().copy(family = "situation_seed"))
        val report = FoundationCardPackValidationService().validate(pack)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.evidence.duplicateCardIdCount >= 1)
        assertTrue(report.findings.any { it.ruleId == "FOUNDATION_CARD_ID_DUPLICATE" })
    }

    @Test
    fun unsafeBoundaryFlagsFailValidation() {
        val pack = validPack().copy(
            boundaryFlags = validPack().boundaryFlags?.copy(
                mediaGenerated = true,
                providerApiCalls = true,
                productionLock = true,
            ),
        )
        val report = FoundationCardPackValidationService().validate(pack)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.evidence.unsafeBoundaryFlagCount >= 1)
        assertTrue(report.boundarySummary.mediaGenerated)
        assertTrue(report.boundarySummary.providerApiCalls)
        assertTrue(report.boundarySummary.productionLock)
    }

    @Test
    fun readinessAndEligibilityClaimsFailValidation() {
        val pack = validPack().copy(
            settingPackReadiness = "ready",
            downstreamUse = validPack().downstreamUse?.copy(
                queryEligible = true,
                settingPackConversionEligible = true,
                assetRegistryEligible = true,
            ),
        )
        val report = FoundationCardPackValidationService().validate(pack)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.evidence.readinessClaimCount >= 1)
        assertTrue(report.evidence.eligibilityClaimCount >= 1)
        assertTrue(report.findings.any { it.ruleId == "FOUNDATION_CARD_PACK_CANDIDATE_STATUS_INVALID" })
        assertTrue(report.findings.any { it.ruleId == "FOUNDATION_CARD_PACK_DOWNSTREAM_USE_INVALID" })
    }

    @Test
    fun missingTraceabilityFieldsFailValidation() {
        val pack = validPack().copy(
            sourceNotes = emptyList(),
            retrievalTags = emptyList(),
            downstreamUse = null,
        )
        val report = FoundationCardPackValidationService().validate(pack)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.evidence.missingSourceNotesCount >= 1)
        assertTrue(report.evidence.missingRetrievalTagsCount >= 1)
        assertTrue(report.evidence.missingDownstreamUseCount >= 1)
    }

    companion object {
        private val samplePackRelativePath: Path =
            Path.of("samples/local_story_input/rain_gate_09_foundation_card_pack_contract_sample.json")
        val samplePackPath: Path =
            generateSequence(Path.of("").toAbsolutePath()) { it.parent }
                .map { it.resolve(samplePackRelativePath) }
                .firstOrNull { Files.exists(it) }
                ?: samplePackRelativePath

        fun validPack(): FoundationCardPack =
            JsonSupport.json.decodeFromString(Files.readString(samplePackPath).removePrefix("\uFEFF"))

        fun assertPassReport(report: FoundationCardPackValidationReport) {
            assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
            assertEquals("FOUNDATION_CARD_PACK_VALIDATION_PASS", report.status)
            assertEquals("foundation_card_pack_validation_v0_only_not_canon_or_runtime", report.downstreamBoundary)
            assertEquals("09_foundation_card_pack_review", report.nextAllowedDownstreamStep)
            assertEquals("local baseline 100% is not final product completion", report.progressCaveat)
            assertTrue(report.checks.isNotEmpty())
            assertTrue(report.checks.all { it.status == "PASS" })
            assertEquals(0, report.findingCount)
            assertTrue(report.boundarySummary.candidateOnly)
            assertFalse(report.boundarySummary.canonApproved)
            assertFalse(report.boundarySummary.settingPackData)
            assertFalse(report.boundarySummary.runtimeSqliteData)
            assertFalse(report.boundarySummary.approvedAssetData)
            assertFalse(report.boundarySummary.providerApiCalls)
            assertFalse(report.boundarySummary.mediaGenerated)
            assertFalse(report.boundarySummary.finalMediaApproval)
            assertFalse(report.boundarySummary.releaseApproved)
            assertFalse(report.boundarySummary.productionLock)
            assertTrue(report.boundarySummary.allRendererInvocationNone)
            assertTrue(report.boundarySummary.allFfmpegInvocationNone)
            assertTrue(report.boundarySummary.allTtsInvocationNone)
        }
    }
}
