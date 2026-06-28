package com.localnovel.engine

import com.localnovel.engine.model.ComicVideoAudioCue
import com.localnovel.engine.model.ComicVideoPacket
import com.localnovel.engine.model.ComicVideoPacketValidationReport
import com.localnovel.engine.model.ComicVideoRenderHandoffManifest
import com.localnovel.engine.model.ComicVideoSubtitleCue
import com.localnovel.engine.model.ComicVideoTiming
import com.localnovel.engine.model.ComicVideoVoiceCast
import com.localnovel.engine.model.ComicVideoVoiceTimelineFill
import com.localnovel.engine.model.ImageFacingDeliveryFrameCard
import com.localnovel.engine.model.ImageFacingDeliveryProof
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComicVideoWorkbenchPipelineFixtureTest {
    @Test
    fun imageFacingProofReachesRenderHandoffManifestThroughCliJsonProtocol() {
        val tempDir = Files.createTempDirectory("comic-video-workbench-pipeline")
        val proofPath = tempDir.resolve("image_facing_delivery_proof.json")
        val packetPath = tempDir.resolve("comic_video_packet.json")
        val fillPath = tempDir.resolve("voice_timeline_fill.json")
        val filledPacketPath = tempDir.resolve("comic_video_packet_filled.json")
        val validationPath = tempDir.resolve("comic_video_packet_validation.json")
        val manifestPath = tempDir.resolve("render_handoff_manifest.json")

        writeJson(proofPath, imageFacingDeliveryProof())

        val createPacketExit = Cli().run(
            listOf(
                "create-comic-video-packet",
                "--image-facing-delivery-proof",
                proofPath.toString(),
                "--packet",
                "comic-video-packet-fixture-001",
                "--panel-duration-ms",
                "2200",
                "--out",
                packetPath.toString(),
            ),
        )
        assertEquals(0, createPacketExit)
        val packet = readJson<ComicVideoPacket>(packetPath)
        assertEquals("comic-video-packet-fixture-001", packet.packetId)
        assertEquals(2, packet.panelTracks.size)
        assertTrue(packet.voiceCast.isEmpty())
        assertTrue(packet.audioCues.isEmpty())
        assertTrue(packet.subtitleCues.isEmpty())

        writeJson(fillPath, voiceTimelineFill(packet.packetId))
        val fillExit = Cli().run(
            listOf(
                "fill-comic-video-voice-timeline",
                "--comic-video-packet",
                packetPath.toString(),
                "--voice-timeline-fill",
                fillPath.toString(),
                "--out",
                filledPacketPath.toString(),
            ),
        )
        assertEquals(0, fillExit)
        val filledPacket = readJson<ComicVideoPacket>(filledPacketPath)
        assertEquals(1, filledPacket.voiceCast.size)
        assertEquals(2, filledPacket.audioCues.size)
        assertEquals(2, filledPacket.subtitleCues.size)
        assertEquals(4400, filledPacket.timing.totalDurationMillis)

        val validateExit = Cli().run(
            listOf(
                "validate-comic-video-packet",
                "--comic-video-packet",
                filledPacketPath.toString(),
                "--validation",
                "comic-video-validation-fixture-001",
                "--out",
                validationPath.toString(),
            ),
        )
        assertEquals(0, validateExit)
        val validation = readJson<ComicVideoPacketValidationReport>(validationPath)
        assertEquals("PASS", validation.verdict, validation.findings.joinToString { it.ruleId })
        assertEquals("comic_video_packet_validated", validation.nextAllowedDownstreamStep)

        val manifestExit = Cli().run(
            listOf(
                "create-comic-video-render-handoff-manifest",
                "--comic-video-packet-validation",
                validationPath.toString(),
                "--manifest",
                "render-handoff-manifest-fixture-001",
                "--out",
                manifestPath.toString(),
            ),
        )
        assertEquals(0, manifestExit)
        val manifest = readJson<ComicVideoRenderHandoffManifest>(manifestPath)
        assertEquals("PASS", manifest.verdict, manifest.findings.joinToString { it.ruleId })
        assertEquals("render-handoff-manifest-fixture-001", manifest.manifestId)
        assertEquals("comic_video_render_handoff_manifest_candidate", manifest.manifestStatus)
        assertEquals("comic_video_render_handoff_manifest_metadata_only", manifest.downstreamBoundary)
        assertEquals("external_manual_renderer_handoff", manifest.nextAllowedDownstreamStep)
        assertEquals(2, manifest.panelTimeline.size)
        assertEquals(2, manifest.voiceTimeline.size)
        assertEquals(2, manifest.subtitleTimeline.size)
        assertEquals(4400, manifest.renderSettings.totalDurationMillis)
        assertSafeWorkbenchBoundary(manifest)
    }

    private fun imageFacingDeliveryProof(): ImageFacingDeliveryProof =
        ImageFacingDeliveryProof(
            verdict = "PASS",
            proofId = "image-facing-delivery-proof-fixture-001",
            proofStatus = "image_facing_delivery_proof_candidate",
            reviewableStoryboardPacketFile = "reviewable_storyboard_packet.json",
            reviewableStoryboardPacketId = "reviewable-storyboard-packet-fixture-001",
            reviewableStoryboardPacketStatus = "reviewable_storyboard_packet_candidate",
            acceptedScriptPacketId = "accepted-script-packet-fixture-001",
            scriptPacketDraftFile = "script_packet_draft_fixture.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            sourceVerdict = "PASS",
            sourceNextAllowedDownstreamStep = "reviewable_storyboard_packet_ready",
            frameDeliveryCards = listOf(
                frame("frame-001", "beat-arrival", "Show Mira at the checkpoint gate with the visible mark."),
                frame("frame-002", "beat-check", "Show the guard asking to see the visible mark first."),
            ),
            downstreamBoundary = "image_facing_delivery_proof_only",
            nextAllowedDownstreamStep = "image_facing_delivery_proof_ready",
        )

    private fun frame(frameId: String, beatReference: String, visualIntent: String): ImageFacingDeliveryFrameCard =
        ImageFacingDeliveryFrameCard(
            frameId = frameId,
            sceneReference = "checkpoint-gate",
            beatReference = beatReference,
            visualIntent = visualIntent,
            cameraIntent = "Medium shot that preserves gate distance and visible mark continuity.",
            mustShow = listOf("Mira has already paid the door toll.", "The visible mark remains clear."),
            doNotShow = listOf("No hidden culprit evidence.", "No media artifact."),
            continuityAnchors = listOf(
                "Mira has already paid the door toll before reaching the checkpoint.",
                "The guard requires a visible mark before answering questions.",
            ),
            assetCandidateRefs = listOf("asset-visible-mark"),
        )

    private fun voiceTimelineFill(packetId: String): ComicVideoVoiceTimelineFill =
        ComicVideoVoiceTimelineFill(
            fillId = "voice-timeline-fill-fixture-001",
            sourceComicVideoPacketId = packetId,
            voiceCast = listOf(
                ComicVideoVoiceCast(
                    voiceId = "voice-mira",
                    characterId = "char-mira",
                    displayName = "Mira",
                    voiceLabel = "local-voice-mira",
                    notes = "manual voice plan",
                ),
            ),
            audioCues = listOf(
                audioCue("audio-001", "panel-frame-001", "I already paid the gate toll.", 0, 1800),
                audioCue("audio-002", "panel-frame-002", "Show the mark first.", 2200, 2200),
            ),
            subtitleCues = listOf(
                subtitleCue("subtitle-001", "panel-frame-001", "audio-001", "I already paid the gate toll.", 0, 1800),
                subtitleCue("subtitle-002", "panel-frame-002", "audio-002", "Show the mark first.", 2200, 4400),
            ),
            timing = ComicVideoTiming(
                totalDurationMillis = 4400,
                frameRate = "24",
                resolution = "1920x1080",
                backgroundPolicy = "panel_track_only",
            ),
            boundaryChecklist = listOf("metadata_only", "manual_voice_plan"),
            notes = listOf("Fixture proves CLI JSON protocol only."),
        )

    private fun audioCue(
        cueId: String,
        panelId: String,
        text: String,
        startMillis: Int,
        durationMillis: Int,
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
        cueId: String,
        panelId: String,
        audioCueId: String,
        text: String,
        startMillis: Int,
        endMillis: Int,
    ): ComicVideoSubtitleCue =
        ComicVideoSubtitleCue(
            cueId = cueId,
            panelId = panelId,
            audioCueId = audioCueId,
            text = text,
            startMillis = startMillis,
            endMillis = endMillis,
        )

    private fun assertSafeWorkbenchBoundary(manifest: ComicVideoRenderHandoffManifest) {
        assertEquals(0, manifest.evidence.mediaOutputFieldCount)
        assertFalse(manifest.downstreamBoundary.contains("production", ignoreCase = true))
        assertFalse(manifest.nextAllowedDownstreamStep.contains("provider", ignoreCase = true))
        assertFalse(manifest.nextAllowedDownstreamStep.contains("rendered", ignoreCase = true))
        assertTrue(manifest.panelTimeline.all { it.mediaSlotStatus == "external_panel_image_required" })
        assertTrue(manifest.voiceTimeline.all { it.mediaSlotStatus == "external_audio_required" })
        val text = JsonSupport.json.encodeToString(manifest)
        listOf(
            "providerJobId",
            "remoteImageUrl",
            "remoteAudioUrl",
            "audioAssetPath",
            "outputVideoPath",
            "renderedVideoPath",
            "production lock",
            "canon",
        ).forEach { forbidden ->
            assertFalse(text.contains(forbidden, ignoreCase = true), "Forbidden boundary text leaked: $forbidden")
        }
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
