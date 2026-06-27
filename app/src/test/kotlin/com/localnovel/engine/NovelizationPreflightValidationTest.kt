package com.localnovel.engine

import com.localnovel.engine.model.CharacterNuwaGateFile
import com.localnovel.engine.model.NovelizationPreflightReport
import com.localnovel.engine.service.CharacterNuwaGateValidationService
import com.localnovel.engine.service.NovelizationPreflightValidationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NovelizationPreflightValidationTest {
    @Test
    fun passingCharacterNuwaReportPassesPreflight() {
        val gate = readJson<CharacterNuwaGateFile>(samplePath("character_nuwa_gate_sample.json"))
        val characterReport = CharacterNuwaGateValidationService().validate(gate)

        val report = NovelizationPreflightValidationService().validate(
            characterNuwaReport = characterReport,
            preflightId = "novelization-preflight-sample-001",
        )

        assertEquals("PASS", report.verdict)
        assertEquals("character-nuwa-gate-sample-001", report.characterNuwaGateId)
        assertEquals(2, report.evidence.requiredSpeakingCharacterCount)
        assertTrue(report.findings.isEmpty())
    }

    @Test
    fun failingCharacterNuwaReportFailsPreflight() {
        val gate = readJson<CharacterNuwaGateFile>(samplePath("character_nuwa_gate_sample.json"))
        val brokenGate = gate.copy(
            characterCards = gate.characterCards.filterNot { it.characterId == "char-mira" },
        )
        val characterReport = CharacterNuwaGateValidationService().validate(brokenGate)

        val report = NovelizationPreflightValidationService().validate(
            characterNuwaReport = characterReport,
            preflightId = "novelization-preflight-sample-001",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "CHARACTER_NUWA_GATE_FAILED" && it.severity == "CRITICAL" })
    }

    @Test
    fun cliWritesNovelizationPreflightReportWithoutDatabase() {
        val tempDir = Files.createTempDirectory("novelization-preflight")
        val characterReportPath = tempDir.resolve("character_nuwa_report.json")
        val out = tempDir.resolve("novelization_preflight_report.json")
        val gate = readJson<CharacterNuwaGateFile>(samplePath("character_nuwa_gate_sample.json"))
        val characterReport = CharacterNuwaGateValidationService().validate(
            gate = gate,
            gateFile = samplePath("character_nuwa_gate_sample.json").toString(),
        )
        writeJson(characterReportPath, characterReport)

        val exit = Cli().run(
            listOf(
                "validate-novelization-preflight",
                "--character-nuwa-report",
                characterReportPath.toString(),
                "--preflight",
                "novelization-preflight-sample-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<NovelizationPreflightReport>(out)
        assertEquals("PASS", report.verdict)
        assertTrue(report.characterNuwaReportFile.endsWith("character_nuwa_report.json"))
    }

    @Test
    fun cliCanRunPreflightDirectlyFromCharacterNuwaGateWithoutIntermediateReport() {
        val out = Files.createTempDirectory("novelization-preflight-direct").resolve("novelization_preflight_report.json")

        val exit = Cli().run(
            listOf(
                "validate-novelization-preflight",
                "--character-nuwa-gate",
                samplePath("character_nuwa_gate_sample.json").toString(),
                "--preflight",
                "novelization-preflight-direct-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<NovelizationPreflightReport>(out)
        assertEquals("PASS", report.verdict)
        assertTrue(report.characterNuwaGateFile.endsWith("character_nuwa_gate_sample.json"))
        assertEquals("", report.characterNuwaReportFile)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }

    private fun samplePath(fileName: String): Path {
        val candidates = listOf(
            Path.of("samples", fileName),
            Path.of("..", "samples", fileName),
        )
        return candidates.firstOrNull { Files.exists(it) } ?: error("Cannot find sample file: $fileName")
    }
}
