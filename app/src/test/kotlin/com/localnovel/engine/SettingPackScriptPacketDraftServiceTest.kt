package com.localnovel.engine

import com.localnovel.engine.model.ScriptPacketDraft
import com.localnovel.engine.model.ScriptPacketDraftValidationReport
import com.localnovel.engine.model.SettingPack
import com.localnovel.engine.model.SettingPackScriptPacketDraftBuildReport
import com.localnovel.engine.service.ScriptPacketDraftValidationService
import com.localnovel.engine.service.SettingPackScriptPacketDraftService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SettingPackScriptPacketDraftServiceTest {
    @Test
    fun publicSettingSceneBuildsValidScriptPacketDraftWithoutSecretLeak() {
        val pack = readJson<SettingPack>(samplePath("comic_video_setting_source/setting_pack_rain_gate_sample.json"))

        val result = SettingPackScriptPacketDraftService().create(
            settingPack = pack,
            settingPackFile = "setting_pack_rain_gate_sample.json",
            requestedSceneId = "scene-rain-gate",
            packetId = "script-packet-draft-rain-gate-sample-001",
        )

        val draft = assertNotNull(result.draft)
        assertEquals("scene-rain-gate", result.selectedSceneId)
        assertEquals("loc-rain-gate", result.selectedLocationId)
        assertEquals(1, result.evidence.excludedSecretCount)
        assertEquals(3, draft.storyboardNotes.frameCandidates.size)
        assertEquals("settingPack", draft.source.inputType)
        val encoded = JsonSupport.json.encodeToString(draft)
        assertFalse(encoded.contains("second bell", ignoreCase = true))
        assertFalse(encoded.contains("author_only", ignoreCase = true))

        val validation = ScriptPacketDraftValidationService().validate(draft)

        assertEquals("PASS", validation.verdict, validation.findings.joinToString { it.ruleId })
    }

    @Test
    fun cliWritesDraftValidationAndBuildReport() {
        val tempDir = Files.createTempDirectory("setting-script-packet-draft-cli")
        val draftPath = tempDir.resolve("script_packet_draft.json")
        val validationPath = tempDir.resolve("script_packet_draft_validation.json")
        val reportPath = tempDir.resolve("setting_script_packet_draft_build_report.json")

        val exit = Cli().run(
            listOf(
                "create-setting-script-packet-draft",
                "--setting-pack",
                samplePath("comic_video_setting_source/setting_pack_rain_gate_sample.json").toString(),
                "--scene",
                "scene-rain-gate",
                "--packet",
                "script-packet-draft-rain-gate-sample-001",
                "--build",
                "setting-script-packet-rain-gate-sample-001",
                "--out",
                draftPath.toString(),
                "--validation-out",
                validationPath.toString(),
                "--report-out",
                reportPath.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(draftPath))
        assertTrue(Files.exists(validationPath))
        assertTrue(Files.exists(reportPath))
        val draft = readJson<ScriptPacketDraft>(draftPath)
        val validation = readJson<ScriptPacketDraftValidationReport>(validationPath)
        val report = readJson<SettingPackScriptPacketDraftBuildReport>(reportPath)
        assertEquals("script-packet-draft-rain-gate-sample-001", draft.packetId)
        assertEquals("PASS", validation.verdict)
        assertEquals("PASS", report.verdict)
        assertEquals("script_packet_draft_review", report.nextAllowedDownstreamStep)
        assertEquals(3, report.evidence.generatedBeatCount)
        assertEquals(1, report.evidence.excludedSecretCount)
    }

    @Test
    fun cliReportsMissingRequestedSceneWithoutWritingDraft() {
        val tempDir = Files.createTempDirectory("setting-script-packet-draft-missing-scene")
        val draftPath = tempDir.resolve("script_packet_draft.json")
        val reportPath = tempDir.resolve("setting_script_packet_draft_build_report.json")

        val exit = Cli().run(
            listOf(
                "create-setting-script-packet-draft",
                "--setting-pack",
                samplePath("comic_video_setting_source/setting_pack_rain_gate_sample.json").toString(),
                "--scene",
                "scene-missing",
                "--out",
                draftPath.toString(),
                "--report-out",
                reportPath.toString(),
            ),
        )

        assertEquals(1, exit)
        assertFalse(Files.exists(draftPath))
        val report = readJson<SettingPackScriptPacketDraftBuildReport>(reportPath)
        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "REQUESTED_PUBLIC_SCENE_NOT_FOUND" })
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private fun samplePath(relative: String): Path {
        val candidates = listOf(
            Path.of("samples", relative),
            Path.of("..", "samples", relative),
        )
        return candidates.firstOrNull { Files.exists(it) } ?: error("Cannot find sample file: $relative")
    }
}
