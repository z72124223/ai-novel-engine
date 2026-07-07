package com.localnovel.engine

import com.localnovel.engine.model.FoundationCardPackReviewReport
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class FoundationCardPackReviewCliTest {
    @Test
    fun cliWritesFoundationCardPackReviewReport() {
        val tempDir = Files.createTempDirectory("foundation-card-pack-review")
        val outPath = tempDir.resolve("rain_gate_09_foundation_card_pack_review_report_sample.json")

        val exit = Cli().run(
            listOf(
                "review-09-foundation-card-pack",
                "--pack",
                FoundationCardPackValidationServiceTest.samplePackPath.toString(),
                "--validation",
                FoundationCardPackReviewServiceTest.validationSamplePath.toString(),
                "--review",
                "rain-gate-09-foundation-card-pack-review-v0-cli",
                "--out",
                outPath.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(outPath))
        val report = readJson<FoundationCardPackReviewReport>(outPath)
        FoundationCardPackReviewServiceTest.assertPassReport(report)
        assertEquals("rain-gate-09-foundation-card-pack-review-v0-cli", report.reviewId)
        assertEquals("rain-gate-09-foundation-card-pack-validation-v0-001", report.sourceValidationId)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
}
