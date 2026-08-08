package com.localnovel.engine

import com.localnovel.engine.model.FoundationCardPackValidationReport
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class FoundationCardPackValidationCliTest {
    @Test
    fun cliWritesFoundationCardPackValidationReport() {
        val tempDir = Files.createTempDirectory("foundation-card-pack-validation")
        val outPath = tempDir.resolve("chenxing_09_foundation_card_pack_validation_report_sample.json")

        val exit = Cli().run(
            listOf(
                "validate-09-foundation-card-pack",
                "--input",
                FoundationCardPackValidationServiceTest.samplePackPath.toString(),
                "--validation",
                "chenxing-09-foundation-card-pack-validation-v0-cli",
                "--out",
                outPath.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(outPath))
        val report = readJson<FoundationCardPackValidationReport>(outPath)
        FoundationCardPackValidationServiceTest.assertPassReport(report)
        assertEquals("chenxing-09-foundation-card-pack-validation-v0-cli", report.validationId)
        assertEquals(8, report.evidence.cardCount)
        assertEquals(7, report.evidence.presentRequiredFamilyCount)
        assertEquals(0, report.evidence.failedCheckCount)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
}
