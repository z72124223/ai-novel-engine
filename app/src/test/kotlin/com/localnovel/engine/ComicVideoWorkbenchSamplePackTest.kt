package com.localnovel.engine

import com.localnovel.engine.model.ComicVideoPacketValidationReport
import com.localnovel.engine.model.ComicVideoRenderHandoffManifest
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComicVideoWorkbenchSamplePackTest {
    @Test
    fun committedSamplePackRerunsThroughCliJsonProtocol() {
        rerunSamplePack(
            sampleDirName = "comic_video_workbench",
            sourceProofFile = "image_facing_delivery_proof_sample.json",
            sourceFillFile = "voice_timeline_fill_sample.json",
            committedValidationFile = "comic_video_packet_validation_sample.json",
            committedManifestFile = "comic_video_render_handoff_manifest_sample.json",
            packetId = "comic-video-sample-packet-001",
            panelDurationMillis = "2600",
            fillTemplateId = "voice-timeline-fill-template-sample-001",
            voiceId = "voice-lin",
            characterId = "char-lin",
            displayName = "Lin",
            voiceLabel = "local-lin-clear",
            validationId = "comic-video-validation-sample-001",
            manifestId = "comic-video-render-handoff-sample-001",
            expectedDurationMillis = 7800,
        )
    }

    @Test
    fun committedTraditionalChineseSamplePackRerunsThroughCliJsonProtocol() {
        rerunSamplePack(
            sampleDirName = "comic_video_workbench_zh_tw",
            sourceProofFile = "image_facing_delivery_proof_zh_tw_sample.json",
            sourceFillFile = "voice_timeline_fill_zh_tw_sample.json",
            committedValidationFile = "comic_video_packet_validation_zh_tw_sample.json",
            committedManifestFile = "comic_video_render_handoff_manifest_zh_tw_sample.json",
            packetId = "comic-video-zh-tw-sample-packet-001",
            panelDurationMillis = "3000",
            fillTemplateId = "voice-timeline-fill-template-zh-tw-sample-001",
            voiceId = "voice-lin",
            characterId = "char-lin",
            displayName = "林",
            voiceLabel = "local-hanhan-lin",
            validationId = "comic-video-validation-zh-tw-sample-001",
            manifestId = "comic-video-render-handoff-zh-tw-sample-001",
            expectedDurationMillis = 9000,
        )
    }

    private fun rerunSamplePack(
        sampleDirName: String,
        sourceProofFile: String,
        sourceFillFile: String,
        committedValidationFile: String,
        committedManifestFile: String,
        packetId: String,
        panelDurationMillis: String,
        fillTemplateId: String,
        voiceId: String,
        characterId: String,
        displayName: String,
        voiceLabel: String,
        validationId: String,
        manifestId: String,
        expectedDurationMillis: Int,
    ) {
        val sampleDir = sampleDir(sampleDirName)
        val sourceProof = sampleDir.resolve(sourceProofFile)
        val sourceFill = sampleDir.resolve(sourceFillFile)
        val committedValidation = sampleDir.resolve(committedValidationFile)
        val committedManifest = sampleDir.resolve(committedManifestFile)

        listOf(sourceProof, sourceFill, committedValidation, committedManifest).forEach { path ->
            assertTrue(Files.exists(path), "Missing sample file: $path")
        }

        val staticValidation = readJson<ComicVideoPacketValidationReport>(committedValidation)
        val staticManifest = readJson<ComicVideoRenderHandoffManifest>(committedManifest)
        assertEquals("PASS", staticValidation.verdict, staticValidation.findings.joinToString { it.ruleId })
        assertEquals("PASS", staticManifest.verdict, staticManifest.findings.joinToString { it.ruleId })
        assertEquals(3, staticManifest.panelTimeline.size)
        assertEquals(3, staticManifest.voiceTimeline.size)
        assertEquals(3, staticManifest.subtitleTimeline.size)

        val tempDir = Files.createTempDirectory("comic-video-workbench-sample-pack")
        val packet = tempDir.resolve("comic_video_packet.json")
        val template = tempDir.resolve("voice_timeline_fill_template.json")
        val filledPacket = tempDir.resolve("comic_video_packet_filled.json")
        val validation = tempDir.resolve("comic_video_packet_validation.json")
        val manifest = tempDir.resolve("comic_video_render_handoff_manifest.json")

        assertEquals(
            0,
            Cli().run(
                listOf(
                    "create-comic-video-packet",
                    "--image-facing-delivery-proof",
                    sourceProof.toString(),
                    "--packet",
                    packetId,
                    "--panel-duration-ms",
                    panelDurationMillis,
                    "--out",
                    packet.toString(),
                ),
            ),
        )
        assertEquals(
            0,
            Cli().run(
                listOf(
                    "create-comic-video-voice-timeline-fill-template",
                    "--comic-video-packet",
                    packet.toString(),
                    "--fill",
                    fillTemplateId,
                    "--voice-id",
                    voiceId,
                    "--character-id",
                    characterId,
                    "--display-name",
                    displayName,
                    "--voice-label",
                    voiceLabel,
                    "--out",
                    template.toString(),
                ),
            ),
        )
        assertEquals(
            0,
            Cli().run(
                listOf(
                    "fill-comic-video-voice-timeline",
                    "--comic-video-packet",
                    packet.toString(),
                    "--voice-timeline-fill",
                    sourceFill.toString(),
                    "--out",
                    filledPacket.toString(),
                ),
            ),
        )
        assertEquals(
            0,
            Cli().run(
                listOf(
                    "validate-comic-video-packet",
                    "--comic-video-packet",
                    filledPacket.toString(),
                    "--validation",
                    validationId,
                    "--out",
                    validation.toString(),
                ),
            ),
        )
        assertEquals(
            0,
            Cli().run(
                listOf(
                    "create-comic-video-render-handoff-manifest",
                    "--comic-video-packet-validation",
                    validation.toString(),
                    "--manifest",
                    manifestId,
                    "--out",
                    manifest.toString(),
                ),
            ),
        )

        val rerunValidation = readJson<ComicVideoPacketValidationReport>(validation)
        val rerunManifest = readJson<ComicVideoRenderHandoffManifest>(manifest)
        assertEquals("PASS", rerunValidation.verdict, rerunValidation.findings.joinToString { it.ruleId })
        assertEquals("PASS", rerunManifest.verdict, rerunManifest.findings.joinToString { it.ruleId })
        assertEquals(3, rerunManifest.panelTimeline.size)
        assertEquals(3, rerunManifest.voiceTimeline.size)
        assertEquals(3, rerunManifest.subtitleTimeline.size)
        assertEquals(expectedDurationMillis, rerunManifest.renderSettings.totalDurationMillis)
        assertEquals(0, rerunManifest.evidence.mediaOutputFieldCount)
        assertTrue(rerunManifest.panelTimeline.all { it.mediaSlotStatus == "external_panel_image_required" })
        assertTrue(rerunManifest.voiceTimeline.all { it.mediaSlotStatus == "external_audio_required" })
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private fun sampleDir(sampleDirName: String): Path {
        var current: Path? = Path.of("").toAbsolutePath()
        while (current != null) {
            val candidate = current.resolve("samples").resolve(sampleDirName)
            if (Files.isDirectory(candidate)) {
                return candidate
            }
            current = current.parent
        }
        error("Could not locate samples/$sampleDirName from test working directory.")
    }
}
