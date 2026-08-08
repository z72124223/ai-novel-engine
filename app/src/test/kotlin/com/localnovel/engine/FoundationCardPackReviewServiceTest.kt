package com.localnovel.engine

import com.localnovel.engine.model.FoundationCardPack
import com.localnovel.engine.model.FoundationCardPackReviewReport
import com.localnovel.engine.model.FoundationCardPackValidationFinding
import com.localnovel.engine.model.FoundationCardPackValidationReport
import com.localnovel.engine.service.FoundationCardPackReviewService
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class FoundationCardPackReviewServiceTest {
    @Test
    fun reviewsValidatedPackForDemandDrivenQueryContractSpec() {
        val report = FoundationCardPackReviewService().reviewFile(
            packPath = FoundationCardPackValidationServiceTest.samplePackPath,
            validationPath = validationSamplePath,
            reviewId = "chenxing-09-foundation-card-pack-review-v0-001",
        )

        assertPassReport(report)
        assertEquals("chenxing-09-foundation-card-pack-review-v0-001", report.reviewId)
        assertEquals("chenxing-09-foundation-card-pack-validation-v0", report.sourceValidationId)
        assertEquals("09-foundation-pack-chenxing-v0-001", report.packId)
    }

    @Test
    fun validationFailStatusFailsReview() {
        val validation = validValidation().copy(
            verdict = "FAIL",
            status = "FOUNDATION_CARD_PACK_VALIDATION_FAIL",
            nextAllowedDownstreamStep = "",
        )
        val report = review(validation = validation)

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "FOUNDATION_CARD_PACK_VALIDATION_STATE_INVALID" })
    }

    @Test
    fun sourceAlignmentMismatchFailsReview() {
        val pack = validPack().copy(packId = "different-pack-id")
        val report = review(pack = pack)

        assertEquals("FAIL", report.verdict)
        assertFalse(report.evidence.sourceAligned)
        assertTrue(report.findings.any { it.ruleId == "FOUNDATION_CARD_PACK_REVIEW_SOURCE_ALIGNMENT_INVALID" })
    }

    @Test
    fun validationFindingsFailReview() {
        val validation = validValidation().copy(
            findingCount = 1,
            findings = listOf(
                FoundationCardPackValidationFinding(
                    ruleId = "SAMPLE_FINDING",
                    category = "boundary",
                    severity = "CRITICAL",
                    path = "$.sample",
                    message = "sample finding",
                ),
            ),
        )
        val report = review(validation = validation)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "FOUNDATION_CARD_PACK_VALIDATION_HEALTH_INVALID" })
    }

    @Test
    fun missingRequiredFamilyFailsReview() {
        val pack = validPack().copy(cards = validPack().cards.filterNot { it.family == "situation_seed" })
        val report = review(pack = pack)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.evidence.missingFamilies.contains("situation_seed"))
        assertTrue(report.findings.any { it.ruleId == "FOUNDATION_CARD_PACK_REVIEW_REQUIRED_FAMILIES_INVALID" })
    }

    @Test
    fun unsafeBoundaryFlagsFailReview() {
        val pack = validPack().copy(
            boundaryFlags = validPack().boundaryFlags?.copy(
                mediaGenerated = true,
                providerApiCalls = true,
                productionLock = true,
            ),
        )
        val report = review(pack = pack)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.evidence.unsafeBoundaryFlagCount >= 1)
        assertTrue(report.boundarySummary.mediaGenerated)
        assertTrue(report.boundarySummary.providerApiCalls)
        assertTrue(report.boundarySummary.productionLock)
    }

    @Test
    fun eligibilityClaimsFailReview() {
        val pack = validPack().copy(
            downstreamUse = validPack().downstreamUse?.copy(queryEligible = true),
        )
        val report = review(pack = pack)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.evidence.eligibilityClaimCount >= 1)
        assertTrue(report.findings.any { it.ruleId == "FOUNDATION_CARD_PACK_REVIEW_DOWNSTREAM_USE_INVALID" })
    }

    companion object {
        val validationSamplePath: Path = ChenxingStoryTestFixtures.chenxingFoundationValidationPath()

        fun validPack(): FoundationCardPack =
            JsonSupport.json.decodeFromString(
                Files.readString(FoundationCardPackValidationServiceTest.samplePackPath).removePrefix("\uFEFF"),
            )

        fun validValidation(): FoundationCardPackValidationReport =
            JsonSupport.json.decodeFromString(Files.readString(validationSamplePath).removePrefix("\uFEFF"))

        fun review(
            pack: FoundationCardPack = validPack(),
            validation: FoundationCardPackValidationReport = validValidation(),
        ): FoundationCardPackReviewReport =
            FoundationCardPackReviewService().review(
                pack = pack,
                validation = validation,
                sourcePackFile = validation.sourcePackFile,
                sourceValidationFile = validationSamplePath.toString(),
                reviewId = "chenxing-09-foundation-card-pack-review-v0-test",
            )

        fun assertPassReport(report: FoundationCardPackReviewReport) {
            assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
            assertEquals("FOUNDATION_CARD_PACK_REVIEW_PASS", report.status)
            assertEquals("foundation_card_pack_review_only_not_canon_or_runtime", report.downstreamBoundary)
            assertEquals("demand_driven_query_contract_spec_v0", report.nextAllowedDownstreamStep)
            assertEquals("local baseline 100% is not true final product completion", report.progressCaveat)
            assertTrue(report.checks.isNotEmpty())
            assertTrue(report.checks.all { it.status == "PASS" })
            assertEquals(0, report.findingCount)
            assertTrue(report.evidence.sourceAligned)
            assertEquals(8, report.evidence.cardCount)
            assertEquals(7, report.evidence.presentRequiredFamilyCount)
            assertEquals(0, report.evidence.validationFailedCheckCount)
            assertEquals(0, report.evidence.validationFindingCount)
            assertEquals(0, report.evidence.reviewFailedCheckCount)
            assertTrue(report.boundarySummary.technicalReviewOnly)
            assertTrue(report.boundarySummary.candidateOnly)
            assertTrue(report.boundarySummary.validationPass)
            assertFalse(report.boundarySummary.humanApproval)
            assertFalse(report.boundarySummary.canonApproval)
            assertFalse(report.boundarySummary.settingPackConversion)
            assertFalse(report.boundarySummary.runtimeSqliteData)
            assertFalse(report.boundarySummary.assetRegistryApproval)
            assertFalse(report.boundarySummary.providerApiCalls)
            assertFalse(report.boundarySummary.mediaGenerated)
            assertFalse(report.boundarySummary.finalMediaApproval)
            assertFalse(report.boundarySummary.releaseApproved)
            assertFalse(report.boundarySummary.productionLock)
            assertFalse(report.boundarySummary.demandDrivenQueryImplementation)
            assertTrue(report.boundarySummary.allRendererInvocationNone)
            assertTrue(report.boundarySummary.allFfmpegInvocationNone)
            assertTrue(report.boundarySummary.allTtsInvocationNone)
        }
    }
}
