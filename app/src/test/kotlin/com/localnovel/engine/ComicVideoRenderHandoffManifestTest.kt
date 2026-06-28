package com.localnovel.engine

import com.localnovel.engine.model.ComicVideoAudioCue
import com.localnovel.engine.model.ComicVideoPacketSourceVerdicts
import com.localnovel.engine.model.ComicVideoPacketValidationReport
import com.localnovel.engine.model.ComicVideoPanelTrack
import com.localnovel.engine.model.ComicVideoRenderHandoffManifest
import com.localnovel.engine.model.ComicVideoSubtitleCue
import com.localnovel.engine.model.ComicVideoTiming
import com.localnovel.engine.model.ComicVideoVoiceCast
import com.localnovel.engine.service.ComicVideoRenderHandoffManifestService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComicVideoRenderHandoffManifestTest {
    @Test
    fun passValidationReportCreatesMetadataOnlyRenderHandoffManifest() {
        val manifest = ComicVideoRenderHandoffManifestService().create(
            validationReport = validationReport(),
            validationReportFile = "comic_video_packet_validation.json",
            manifestId = "render-handoff-manifest-001",
        )

        assertEquals("PASS", manifest.verdict, manifest.findings.joinToString { it.ruleId })
        assertEquals("render-handoff-manifest-001", manifest.manifestId)
        assertEquals("comic_video_render_handoff_manifest_candidate", manifest.manifestStatus)
        assertEquals("comic_video_render_handoff_manifest_metadata_only", manifest.downstreamBoundary)
        assertEquals("external_manual_renderer_handoff", manifest.nextAllowedDownstreamStep)
        assertEquals("comic-video-validation-001", manifest.validationId)
        assertEquals("comic-video-packet-001", manifest.packetId)
        assertEquals(2, manifest.panelTimeline.size)
        assertEquals(2, manifest.voiceTimeline.size)
        assertEquals(2, manifest.subtitleTimeline.size)
        assertEquals(4400, manifest.renderSettings.totalDurationMillis)
        assertEquals("external_manual_renderer_only", manifest.renderSettings.rendererBoundary)
        assertEquals("external_panel_image_required", manifest.panelTimeline.first().mediaSlotStatus)
        assertEquals("external_audio_required", manifest.voiceTimeline.first().mediaSlotStatus)
        assertEquals(0, manifest.evidence.mediaOutputFieldCount)
    }

    @Test
    fun failValidationReportOrMediaOutputFieldsFailManifest() {
        val failSource = ComicVideoRenderHandoffManifestService().create(
            validationReport = validationReport(verdict = "FAIL", nextAllowedDownstreamStep = ""),
            validationReportFile = "comic_video_packet_validation.json",
        )
        val unsafeSource = ComicVideoRenderHandoffManifestService().create(
            validationReport = validationReport(
                panelTracks = listOf(
                    panel(
                        panelId = "panel-001",
                        sourceFrameId = "frame-001",
                        beatReference = "beat-arrival",
                        displayDurationMillis = 2000,
                        localImagePath = "outputs/panels/panel-001.png",
                    ),
                ),
            ),
            validationReportFile = "comic_video_packet_validation.json",
        )

        assertEquals("FAIL", failSource.verdict)
        assertEquals("", failSource.nextAllowedDownstreamStep)
        assertTrue(failSource.panelTimeline.isEmpty())
        assertTrue(failSource.findings.any { it.ruleId == "COMIC_VIDEO_VALIDATION_NOT_PASS" })
        assertEquals("FAIL", unsafeSource.verdict)
        assertEquals("", unsafeSource.nextAllowedDownstreamStep)
        assertTrue(unsafeSource.findings.any { it.ruleId == "MEDIA_OUTPUT_FIELD_FORBIDDEN" })
        assertTrue(unsafeSource.evidence.mediaOutputFieldCount > 0)
    }

    @Test
    fun cliWritesRenderHandoffManifestWithoutDatabase() {
        val tempDir = Files.createTempDirectory("comic-video-render-handoff-manifest")
        val validationPath = tempDir.resolve("comic_video_packet_validation.json")
        val out = tempDir.resolve("render_handoff_manifest.json")
        writeJson(validationPath, validationReport())

        val exit = Cli().run(
            listOf(
                "create-comic-video-render-handoff-manifest",
                "--comic-video-packet-validation",
                validationPath.toString(),
                "--manifest",
                "render-handoff-manifest-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val manifest = readJson<ComicVideoRenderHandoffManifest>(out)
        assertEquals("PASS", manifest.verdict, manifest.findings.joinToString { it.ruleId })
        assertEquals("render-handoff-manifest-cli-001", manifest.manifestId)
        assertEquals(2, manifest.panelTimeline.size)
        assertEquals(2, manifest.voiceTimeline.size)
        assertEquals(2, manifest.subtitleTimeline.size)
        assertEquals("external_manual_renderer_handoff", manifest.nextAllowedDownstreamStep)
    }

    private fun validationReport(
        verdict: String = "PASS",
        nextAllowedDownstreamStep: String = "comic_video_packet_validated",
        panelTracks: List<ComicVideoPanelTrack> = listOf(
            panel("panel-001", "frame-001", "beat-arrival", 2000),
            panel("panel-002", "frame-002", "beat-check", 2400),
        ),
    ): ComicVideoPacketValidationReport =
        ComicVideoPacketValidationReport(
            verdict = verdict,
            validationId = "comic-video-validation-001",
            comicVideoPacketFile = "comic_video_packet.json",
            packetId = "comic-video-packet-001",
            packetStatus = "comic_video_packet_candidate",
            reviewableStoryboardPacketFile = "reviewable_storyboard_packet.json",
            imageFacingDeliveryProofFile = "image_facing_delivery_proof.json",
            sourceReviewableStoryboardPacketId = "reviewable-storyboard-packet-001",
            sourceImageFacingDeliveryProofId = "image-facing-delivery-proof-001",
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            sourceVerdicts = ComicVideoPacketSourceVerdicts(
                reviewableStoryboardPacketVerdict = "PASS",
                imageFacingDeliveryProofVerdict = "PASS",
                imageFacingDeliveryProofNextAllowedDownstreamStep = "image_facing_delivery_proof_ready",
            ),
            panelTracks = if (verdict == "PASS") panelTracks else emptyList(),
            voiceCast = if (verdict == "PASS") listOf(voice()) else emptyList(),
            audioCues = if (verdict == "PASS") listOf(
                audioCue(),
                audioCue(
                    cueId = "audio-002",
                    panelId = "panel-002",
                    text = "Show the mark first.",
                    startMillis = 2200,
                    durationMillis = 2200,
                ),
            ) else emptyList(),
            subtitleCues = if (verdict == "PASS") listOf(
                subtitleCue(),
                subtitleCue(
                    cueId = "subtitle-002",
                    panelId = "panel-002",
                    audioCueId = "audio-002",
                    text = "Show the mark first.",
                    startMillis = 2200,
                    endMillis = 4400,
                ),
            ) else emptyList(),
            timing = if (verdict == "PASS") timing() else ComicVideoTiming(),
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun panel(
        panelId: String,
        sourceFrameId: String,
        beatReference: String,
        displayDurationMillis: Int,
        localImagePath: String = "",
    ): ComicVideoPanelTrack =
        ComicVideoPanelTrack(
            panelId = panelId,
            sourceFrameId = sourceFrameId,
            sceneReference = "checkpoint-gate",
            beatReference = beatReference,
            visualIntent = "Show checkpoint action.",
            cameraIntent = "Medium shot.",
            displayDurationMillis = displayDurationMillis,
            mustShow = listOf("Visible mark."),
            doNotShow = listOf("Hidden culprit evidence."),
            continuityAnchors = listOf("Guard asks for the visible mark."),
            assetCandidateRefs = listOf("asset-visible-mark"),
            localImagePath = localImagePath,
        )

    private fun voice(): ComicVideoVoiceCast =
        ComicVideoVoiceCast(
            voiceId = "voice-mira",
            characterId = "char-mira",
            displayName = "Mira",
            voiceLabel = "local-voice-mira",
            notes = "manual voice plan",
        )

    private fun audioCue(
        cueId: String = "audio-001",
        panelId: String = "panel-001",
        text: String = "I already paid the gate toll.",
        startMillis: Int = 0,
        durationMillis: Int = 1800,
    ): ComicVideoAudioCue =
        ComicVideoAudioCue(
            cueId = cueId,
            panelId = panelId,
            voiceId = "voice-mira",
            characterId = "char-mira",
            text = text,
            startMillis = startMillis,
            durationMillis = durationMillis,
        )

    private fun subtitleCue(
        cueId: String = "subtitle-001",
        panelId: String = "panel-001",
        audioCueId: String = "audio-001",
        text: String = "I already paid the gate toll.",
        startMillis: Int = 0,
        endMillis: Int = 1800,
    ): ComicVideoSubtitleCue =
        ComicVideoSubtitleCue(
            cueId = cueId,
            panelId = panelId,
            audioCueId = audioCueId,
            text = text,
            startMillis = startMillis,
            endMillis = endMillis,
        )

    private fun timing(): ComicVideoTiming =
        ComicVideoTiming(
            totalDurationMillis = 4400,
            frameRate = "24",
            resolution = "1920x1080",
            backgroundPolicy = "panel_track_only",
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
