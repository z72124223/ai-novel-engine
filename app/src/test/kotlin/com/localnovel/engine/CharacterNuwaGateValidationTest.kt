package com.localnovel.engine

import com.localnovel.engine.model.CharacterNuwaGateFile
import com.localnovel.engine.model.CharacterNuwaGateValidationReport
import com.localnovel.engine.service.CharacterNuwaGateValidationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CharacterNuwaGateValidationTest {
    @Test
    fun sampleCharacterNuwaGatePassesValidation() {
        val gate = readJson<CharacterNuwaGateFile>(samplePath("character_nuwa_gate_sample.json"))

        val report = CharacterNuwaGateValidationService().validate(gate)

        assertEquals("PASS", report.verdict)
        assertEquals("character-nuwa-gate-sample-001", report.gateId)
        assertEquals(2, report.requiredSpeakingCharacterCount)
        assertEquals(2, report.cardCount)
        assertTrue(report.findings.isEmpty())
    }

    @Test
    fun missingRequiredFullCardFailsValidation() {
        val gate = readJson<CharacterNuwaGateFile>(samplePath("character_nuwa_gate_sample.json"))
        val broken = gate.copy(
            characterCards = gate.characterCards.filterNot { it.characterId == "char-mira" },
        )

        val report = CharacterNuwaGateValidationService().validate(broken)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "REQUIRED_SPEAKER_CARD_MISSING" && it.severity == "CRITICAL" })
    }

    @Test
    fun bannedCalibrationStructureInAcceptedLineFailsValidation() {
        val gate = readJson<CharacterNuwaGateFile>(samplePath("character_nuwa_gate_sample.json"))
        val broken = gate.copy(
            characterCards = gate.characterCards.map { card ->
                if (card.characterId != "char-mira") return@map card
                card.copy(
                    sampleLines = card.sampleLines.copy(
                        accepted = card.sampleLines.accepted.toMutableList().also {
                            it[0] = "這不是逃跑，這是生路。"
                        },
                    ),
                )
            },
        )

        val report = CharacterNuwaGateValidationService().validate(broken)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId.startsWith("BANNED_CALIBRATION_STRUCTURE") && it.severity == "CRITICAL" })
        assertEquals(1, report.evidence.bannedCalibrationHitCount)
    }

    @Test
    fun cliWritesCharacterNuwaGateValidationReportWithoutDatabase() {
        val out = Files.createTempDirectory("character-nuwa-gate").resolve("report.json")

        val exit = Cli().run(
            listOf(
                "validate-character-nuwa-gate",
                "--file",
                samplePath("character_nuwa_gate_sample.json").toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<CharacterNuwaGateValidationReport>(out)
        assertEquals("PASS", report.verdict)
        assertTrue(report.gateFile.endsWith("character_nuwa_gate_sample.json"))
    }

    @Test
    fun cliCreatesCharacterNuwaGateTemplateWithoutDatabase() {
        val out = Files.createTempDirectory("character-nuwa-template").resolve("character_nuwa_gate.json")

        val exit = Cli().run(
            listOf(
                "create-character-nuwa-card-template",
                "--project",
                "demo-project",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val gate = readJson<CharacterNuwaGateFile>(out)
        assertEquals("demo-project", gate.projectId)
        assertTrue(gate.requiredSpeakingCharacters.isNotEmpty())
        assertTrue(gate.characterCards.isNotEmpty())
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private fun samplePath(fileName: String): Path {
        val candidates = listOf(
            Path.of("samples", fileName),
            Path.of("..", "samples", fileName),
        )
        return candidates.firstOrNull { Files.exists(it) } ?: error("Cannot find sample file: $fileName")
    }
}
