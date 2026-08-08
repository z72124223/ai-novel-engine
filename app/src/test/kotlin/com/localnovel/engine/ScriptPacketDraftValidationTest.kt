package com.localnovel.engine

import com.localnovel.engine.model.ScriptPacketDraft
import com.localnovel.engine.model.ScriptPacketDraftValidationReport
import com.localnovel.engine.service.ScriptPacketDraftValidationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScriptPacketDraftValidationTest {
    @Test
    fun sampleDraftPassesValidation() {
        val packet = readJson<ScriptPacketDraft>(samplePath("script_packet_draft_sample.json"))

        val report = ScriptPacketDraftValidationService().validate(packet)

        assertEquals("PASS", report.verdict)
        assertEquals("script-packet-draft-sample-001", report.packetId)
        assertEquals(1, report.segmentCount)
        assertTrue(report.findings.isEmpty())
    }

    @Test
    fun missingBeatVisibleActionFailsValidation() {
        val packet = readJson<ScriptPacketDraft>(samplePath("script_packet_draft_sample.json"))
        val broken = packet.copy(
            segments = packet.segments.mapIndexed { segmentIndex, segment ->
                if (segmentIndex != 0) return@mapIndexed segment
                segment.copy(
                    beatList = segment.beatList.mapIndexed { beatIndex, beat ->
                        if (beatIndex == 0) beat.copy(visibleAction = "") else beat
                    },
                )
            },
        )

        val report = ScriptPacketDraftValidationService().validate(broken)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "BEAT_VISIBLE_ACTION_MISSING" && it.severity == "MAJOR" })
    }

    @Test
    fun authorOnlyLeakInVisibleBeatFailsValidation() {
        val packet = readJson<ScriptPacketDraft>(samplePath("script_packet_draft_sample.json"))
        val broken = packet.copy(
            segments = packet.segments.mapIndexed { segmentIndex, segment ->
                if (segmentIndex != 0) return@mapIndexed segment
                segment.copy(
                    beatList = segment.beatList.mapIndexed { beatIndex, beat ->
                        if (beatIndex == 0) {
                            beat.copy(visibleAction = "The hidden culprit steps into the visible frame.")
                        } else {
                            beat
                        }
                    },
                )
            },
        )

        val report = ScriptPacketDraftValidationService().validate(broken)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "AUTHOR_ONLY_LEAK_RISK" && it.severity == "CRITICAL" })
    }

    @Test
    fun cliWritesScriptPacketDraftValidationReportWithoutDatabase() {
        val out = Files.createTempDirectory("script-packet-validation").resolve("report.json")

        val exit = Cli().run(
            listOf(
                "validate-script-packet-draft",
                "--packet",
                samplePath("script_packet_draft_sample.json").toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<ScriptPacketDraftValidationReport>(out)
        assertEquals("PASS", report.verdict)
        assertTrue(report.packetFile.endsWith("script_packet_draft_sample.json"))
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
