package com.localnovel.engine

import com.localnovel.engine.model.ComicVideoAudioCue
import com.localnovel.engine.model.ComicVideoPacket
import com.localnovel.engine.model.ComicVideoPacketSourceVerdicts
import com.localnovel.engine.model.ComicVideoPanelTrack
import com.localnovel.engine.model.ComicVideoSubtitleCue
import com.localnovel.engine.model.ComicVideoTiming
import com.localnovel.engine.model.ComicVideoVoiceCast
import com.localnovel.engine.model.ComicVideoVoiceTimelineFill
import com.localnovel.engine.service.ComicVideoPacketValidationService
import com.localnovel.engine.service.ComicVideoVoiceTimelineFillService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ComicVideoVoiceTimelineFillServiceTest {
    @Test
    fun createsEditableTemplateFromPacketPanelTracks() {
        val template = ComicVideoVoiceTimelineFillService().createTemplate(
            packet = packet(),
            fillId = "voice-fill-template-001",
            voiceId = "voice-narrator",
            characterId = "char-narrator",
            displayName = "Narrator",
            voiceLabel = "local-narrator",
        )

        assertEquals("voice-fill-template-001", template.fillId)
        assertEquals("comic-video-packet-001", template.sourceComicVideoPacketId)
        assertEquals("comic_video_voice_timeline_fill_candidate", template.fillStatus)
        assertEquals(1, template.voiceCast.size)
        assertEquals("voice-narrator", template.voiceCast.single().voiceId)
        assertEquals("char-narrator", template.voiceCast.single().characterId)
        assertEquals(2, template.audioCues.size)
        assertEquals("panel-001", template.audioCues[0].panelId)
        assertEquals("panel-002", template.audioCues[1].panelId)
        assertEquals(0, template.audioCues[0].startMillis)
        assertEquals(2000, template.audioCues[0].durationMillis)
        assertEquals(2000, template.audioCues[1].startMillis)
        assertEquals(2400, template.audioCues[1].durationMillis)
        assertEquals(2, template.subtitleCues.size)
        assertEquals("audio-001", template.subtitleCues[0].audioCueId)
        assertEquals("audio-002", template.subtitleCues[1].audioCueId)
        assertEquals(4400, template.timing.totalDurationMillis)
        assertEquals("24", template.timing.frameRate)
        assertEquals("1920x1080", template.timing.resolution)
        assertTrue(template.boundaryChecklist.contains("voice_timeline_fill_template"))
        assertTrue(template.audioCues.all { it.audioAssetPath.isBlank() && it.remoteAudioUrl.isBlank() && it.providerJobId.isBlank() && it.generatedStatus.isBlank() })
        assertTrue(template.timing.outputVideoPath.isBlank())
        assertTrue(template.timing.renderedVideoPath.isBlank())
    }

    @Test
    fun fillsVoiceAudioSubtitleTimelineAndResultPassesValidation() {
        val filled = ComicVideoVoiceTimelineFillService().fill(
            packet = packet(),
            fill = fill(),
        )
        val report = ComicVideoPacketValidationService().validate(
            packet = filled,
            packetFile = "comic_video_packet_filled.json",
        )

        assertEquals("comic-video-packet-001", filled.packetId)
        assertEquals(2, filled.panelTracks.size)
        assertEquals("panel-001", filled.panelTracks.first().panelId)
        assertEquals("frame-001", filled.panelTracks.first().sourceFrameId)
        assertEquals(1, filled.voiceCast.size)
        assertEquals(2, filled.audioCues.size)
        assertEquals(2, filled.subtitleCues.size)
        assertEquals(4400, filled.timing.totalDurationMillis)
        assertTrue(filled.boundaryChecklist.contains("voice_timeline_metadata_filled"))
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("comic_video_packet_validated", report.nextAllowedDownstreamStep)
    }

    @Test
    fun sourceMismatchOrUnsafeMediaFieldsAreRejectedBeforeOutput() {
        assertFailsWith<IllegalArgumentException> {
            ComicVideoVoiceTimelineFillService().fill(
                packet = packet(),
                fill = fill(sourceComicVideoPacketId = "other-packet"),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            ComicVideoVoiceTimelineFillService().fill(
                packet = packet(),
                fill = fill(audioCues = listOf(audioCue(audioAssetPath = "outputs/audio/line-001.wav"))),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            ComicVideoVoiceTimelineFillService().fill(
                packet = packet(),
                fill = fill(timing = timing(totalDurationMillis = 4400).copy(outputVideoPath = "outputs/video/demo.mp4")),
            )
        }
    }

    @Test
    fun templateCreationRejectsInvalidPanelDurations() {
        val invalidPacket = packet().copy(
            panelTracks = listOf(
                panel("panel-001", "frame-001", "beat-arrival", 0),
            ),
        )

        assertFailsWith<IllegalArgumentException> {
            ComicVideoVoiceTimelineFillService().createTemplate(packet = invalidPacket)
        }
    }

    @Test
    fun cliWritesFilledPacketWithoutDatabase() {
        val tempDir = Files.createTempDirectory("comic-video-voice-timeline-fill")
        val packetPath = tempDir.resolve("comic_video_packet.json")
        val fillPath = tempDir.resolve("voice_timeline_fill.json")
        val out = tempDir.resolve("comic_video_packet_filled.json")
        writeJson(packetPath, packet())
        writeJson(fillPath, fill())

        val exit = Cli().run(
            listOf(
                "fill-comic-video-voice-timeline",
                "--comic-video-packet",
                packetPath.toString(),
                "--voice-timeline-fill",
                fillPath.toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val filled = readJson<ComicVideoPacket>(out)
        val report = ComicVideoPacketValidationService().validate(
            packet = filled,
            packetFile = out.toString(),
        )
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals(1, filled.voiceCast.size)
        assertEquals(2, filled.audioCues.size)
        assertEquals(2, filled.subtitleCues.size)
    }

    @Test
    fun cliWritesVoiceTimelineFillTemplateWithoutDatabase() {
        val tempDir = Files.createTempDirectory("comic-video-voice-timeline-fill-template")
        val packetPath = tempDir.resolve("comic_video_packet.json")
        val out = tempDir.resolve("voice_timeline_fill_template.json")
        writeJson(packetPath, packet())

        val exit = Cli().run(
            listOf(
                "create-comic-video-voice-timeline-fill-template",
                "--comic-video-packet",
                packetPath.toString(),
                "--fill",
                "voice-fill-template-cli-001",
                "--voice-id",
                "voice-cli",
                "--character-id",
                "char-cli",
                "--display-name",
                "CLI Voice",
                "--voice-label",
                "local-cli-voice",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val template = readJson<ComicVideoVoiceTimelineFill>(out)
        assertEquals("voice-fill-template-cli-001", template.fillId)
        assertEquals("comic-video-packet-001", template.sourceComicVideoPacketId)
        assertEquals("voice-cli", template.voiceCast.single().voiceId)
        assertEquals("char-cli", template.voiceCast.single().characterId)
        assertEquals("CLI Voice", template.voiceCast.single().displayName)
        assertEquals("local-cli-voice", template.voiceCast.single().voiceLabel)
        assertEquals(listOf("panel-001", "panel-002"), template.audioCues.map { it.panelId })
        assertEquals(4400, template.timing.totalDurationMillis)
    }

    private fun packet(): ComicVideoPacket =
        ComicVideoPacket(
            packetId = "comic-video-packet-001",
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
            panelTracks = listOf(
                panel("panel-001", "frame-001", "beat-arrival", 2000),
                panel("panel-002", "frame-002", "beat-check", 2400),
            ),
            timing = timing(totalDurationMillis = 4400),
            boundaryChecklist = listOf("metadata_only", "candidate_timeline"),
            residualRisks = listOf("Needs voice timeline fill."),
        )

    private fun panel(
        panelId: String,
        sourceFrameId: String,
        beatReference: String,
        displayDurationMillis: Int,
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
        )

    private fun fill(
        sourceComicVideoPacketId: String = "comic-video-packet-001",
        audioCues: List<ComicVideoAudioCue> = listOf(
            audioCue(),
            audioCue(
                cueId = "audio-002",
                panelId = "panel-002",
                text = "Show the mark first.",
                startMillis = 2200,
                durationMillis = 2200,
            ),
        ),
        timing: ComicVideoTiming = timing(totalDurationMillis = 4400),
    ): ComicVideoVoiceTimelineFill =
        ComicVideoVoiceTimelineFill(
            fillId = "voice-timeline-fill-001",
            sourceComicVideoPacketId = sourceComicVideoPacketId,
            voiceCast = listOf(voice()),
            audioCues = audioCues,
            subtitleCues = listOf(
                subtitleCue(),
                subtitleCue(
                    cueId = "subtitle-002",
                    panelId = "panel-002",
                    audioCueId = "audio-002",
                    text = "Show the mark first.",
                    startMillis = 2200,
                    endMillis = 4400,
                ),
            ),
            timing = timing,
            boundaryChecklist = listOf("metadata_only", "manual_voice_plan"),
            notes = listOf("Author-filled voice timeline metadata."),
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
        audioAssetPath: String = "",
    ): ComicVideoAudioCue =
        ComicVideoAudioCue(
            cueId = cueId,
            panelId = panelId,
            voiceId = "voice-mira",
            characterId = "char-mira",
            text = text,
            startMillis = startMillis,
            durationMillis = durationMillis,
            audioAssetPath = audioAssetPath,
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

    private fun timing(totalDurationMillis: Int): ComicVideoTiming =
        ComicVideoTiming(
            totalDurationMillis = totalDurationMillis,
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
