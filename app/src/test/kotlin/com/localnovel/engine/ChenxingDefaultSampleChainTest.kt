package com.localnovel.engine

import com.localnovel.engine.model.FoundationCardPackValidationReport
import com.localnovel.engine.model.LocalStoryInputPackValidationReport
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class ChenxingDefaultSampleChainTest {
    @Test
    fun chenxingStoryInputPackSamplePassesValidation() {
        val input = resolveRepoPath("samples/chenxing/chenxing_story_input_pack_sample.json")
        val out = Files.createTempDirectory("chenxing-story-input-validation").resolve("report.json")

        val exit = Cli().run(
            listOf(
                "validate-local-story-input-pack",
                "--input",
                input.toString(),
                "--validation",
                "chenxing-story-input-validation-v0",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        val report = readJson<LocalStoryInputPackValidationReport>(out)
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("local-story-input-chenxing-v0-001", report.packId)
        assertEquals(3, report.evidence.characterCount)
        assertEquals(1, report.evidence.sceneCount)
        assertEquals(3, report.evidence.beatCount)
        assertTrue(report.evidence.localOnlyDeclared)
    }

    @Test
    fun chenxingFoundationCardPackContractSamplePassesValidation() {
        val input = resolveRepoPath(
            "samples/local_story_input/chenxing_09_foundation_card_pack_contract_sample.json",
        )
        val out = Files.createTempDirectory("chenxing-foundation-card-validation").resolve("report.json")

        val exit = Cli().run(
            listOf(
                "validate-09-foundation-card-pack",
                "--input",
                input.toString(),
                "--validation",
                "chenxing-09-foundation-card-pack-validation-v0",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        val report = readJson<FoundationCardPackValidationReport>(out)
        FoundationCardPackValidationServiceTest.assertPassReport(report)
        assertEquals("09-foundation-pack-chenxing-v0-001", report.packId)
        assertEquals(8, report.evidence.cardCount)
        assertEquals(7, report.evidence.presentRequiredFamilyCount)
    }

    private fun resolveRepoPath(relative: String): Path =
        generateSequence(Path.of("").toAbsolutePath()) { it.parent }
            .map { it.resolve(relative) }
            .first { Files.exists(it) }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
}