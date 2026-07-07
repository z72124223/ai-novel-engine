package com.localnovel.engine

import com.localnovel.engine.model.LocalStoryInputPackValidationReport
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocalStoryInputPackValidationCliTest {
    @Test
    fun cliWritesLocalStoryInputPackValidationReport() {
        val tempDir = Files.createTempDirectory("local-story-input-pack-validation")
        val input = tempDir.resolve("rain_gate_story_input_pack_sample.json")
        val out = tempDir.resolve("rain_gate_story_input_pack_validation_sample.json")
        writeJson(input, LocalStoryInputPackValidationServiceTest.validPack())

        val exit = Cli().run(
            listOf(
                "validate-local-story-input-pack",
                "--input",
                input.toString(),
                "--validation",
                "local-story-input-rain-gate-validation-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<LocalStoryInputPackValidationReport>(out)
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("local-story-input-rain-gate-validation-001", report.validationId)
        assertEquals("local_story_input_pack_validated_for_review", report.nextAllowedDownstreamStep)
        assertEquals(2, report.evidence.characterCount)
        assertEquals(1, report.evidence.sceneCount)
        assertEquals(2, report.evidence.beatCount)
        assertTrue(report.evidence.localOnlyDeclared)
        assertTrue(report.evidence.noExternalPaidProviderToolsDeclared)
        assertTrue(report.evidence.fileProtocolJsonOnlyDeclared)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
