package com.localnovel.engine

import com.localnovel.engine.model.ComicVideoAudioCue
import com.localnovel.engine.model.ComicVideoPacket
import com.localnovel.engine.model.ComicVideoPacketSourceVerdicts
import com.localnovel.engine.model.ComicVideoPacketValidationReport
import com.localnovel.engine.model.ComicVideoPanelTrack
import com.localnovel.engine.model.ComicVideoSubtitleCue
import com.localnovel.engine.model.ComicVideoTiming
import com.localnovel.engine.model.ComicVideoVoiceCast
import com.localnovel.engine.service.ComicVideoPacketValidationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComicVideoPacketValidationTest {
    @Test
    fun happyPathMetadataOnlyComicVideoPacketValidates() {
        val report = validate(validPacket())

        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("comic-video-packet-001-validation", report.validationId)
        assertEquals("comic_video_packet_validated", report.nextAllowedDownstreamStep)
        assertEquals("comic_video_packet_validation_only", report.downstreamBoundary)
        assertEquals(2, report.panelTracks.size)
        assertEquals(1, report.voiceCast.size)
        assertEquals(2, report.audioCues.size)
        assertEquals(2, report.subtitleCues.size)
        assertEquals(0, report.evidence.crossReferenceMismatchCount)
        assertEquals(0, report.evidence.generatedMediaFieldCount)
        assertEquals(0, report.evidence.remoteOrProviderFieldCount)
        assertEquals(0, report.evidence.forbiddenClaimCount)
        assertEquals(0, report.evidence.forbiddenStatusCount)
    }

    @Test
    fun mediaOutputRemoteProviderAndCompletionClaimsFail() {
        val packet = validPacket().copy(
            panelTracks = listOf(
                panel(
                    localImagePath = "outputs/panels/panel-001.png",
                    remoteImageUrl = "https://example.test/panel.png",
                    providerJobId = "job-001",
                ),
            ),
            audioCues = listOf(
                audioCue(
                    audioAssetPath = "outputs/audio/audio-001.wav",
                    remoteAudioUrl = "https://example.test/audio.wav",
                    providerJobId = "job-voice-001",
                    generatedStatus = "audio generated",
                ),
            ),
            subtitleCues = listOf(subtitleCue()),
            voiceCast = listOf(voice(notes = "production lock canon")),
            timing = timing(totalDurationMillis = 3000).copy(outputVideoPath = "outputs/video/final.mp4"),
        )
        val report = validate(packet)

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.panelTracks.isEmpty())
        assertTrue(report.findings.any { it.ruleId == "MEDIA_OUTPUT_FIELD_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "REMOTE_OR_PROVIDER_REFERENCE_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "COMIC_VIDEO_STATUS_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_MEDIA_COMPLETION_CLAIM_FORBIDDEN" })
        assertTrue(report.evidence.generatedMediaFieldCount >= 6)
    }

    @Test
    fun brokenAudioAndSubtitleReferencesFail() {
        val report = validate(
            validPacket().copy(
                audioCues = listOf(audioCue(panelId = "panel-missing", voiceId = "voice-missing")),
                subtitleCues = listOf(subtitleCue(panelId = "panel-missing", audioCueId = "audio-missing")),
            ),
        )

        assertEquals("FAIL", report.verdict)
        assertEquals("", report.nextAllowedDownstreamStep)
        assertTrue(report.findings.any { it.ruleId == "AUDIO_CUE_PANEL_REF_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "AUDIO_CUE_VOICE_REF_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "SUBTITLE_PANEL_REF_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "SUBTITLE_AUDIO_CUE_REF_MISSING" })
        assertTrue(report.evidence.crossReferenceMismatchCount >= 4)
    }

    @Test
    fun sourceStateAndShaBoundaryFailWhenIncomplete() {
        val report = validate(
            validPacket().copy(
                sourceVerdicts = ComicVideoPacketSourceVerdicts(
                    reviewableStoryboardPacketVerdict = "FAIL",
                    imageFacingDeliveryProofVerdict = "PASS",
                    imageFacingDeliveryProofNextAllowedDownstreamStep = "image_facing_delivery_proof_ready",
                ),
                scriptPacketDraftContentSha256 = "ABC",
            ),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "SOURCE_REVIEWABLE_STORYBOARD_PACKET_NOT_PASS" })
        assertTrue(report.findings.any { it.ruleId == "SCRIPT_PACKET_DRAFT_HASH_INVALID" })
    }

    @Test
    fun cliWritesValidationReportWithoutDatabase() {
        val tempDir = Files.createTempDirectory("comic-video-packet-validation")
        val packetPath = tempDir.resolve("comic_video_packet.json")
        val out = tempDir.resolve("comic_video_packet_validation.json")
        writeJson(packetPath, validPacket())

        val exit = Cli().run(
            listOf(
                "validate-comic-video-packet",
                "--comic-video-packet",
                packetPath.toString(),
                "--validation",
                "comic-video-validation-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<ComicVideoPacketValidationReport>(out)
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("comic-video-validation-001", report.validationId)
        assertEquals("comic_video_packet_validated", report.nextAllowedDownstreamStep)
    }

    private fun validate(packet: ComicVideoPacket): ComicVideoPacketValidationReport =
        ComicVideoPacketValidationService().validate(
            packet = packet,
            packetFile = "comic_video_packet.json",
        )

    private fun validPacket(): ComicVideoPacket =
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
                panel(),
                panel(
                    panelId = "panel-002",
                    sourceFrameId = "frame-002",
                    beatReference = "beat-question",
                    visualIntent = "Show the guard requiring the visible mark before giving route information.",
                    cameraIntent = "Close shot on the mark and the guard's restrained response.",
                    displayDurationMillis = 2400,
                ),
            ),
            voiceCast = listOf(voice()),
            audioCues = listOf(
                audioCue(),
                audioCue(
                    cueId = "audio-002",
                    panelId = "panel-002",
                    text = "先把印記露出來。",
                    startMillis = 2200,
                    durationMillis = 2200,
                ),
            ),
            subtitleCues = listOf(
                subtitleCue(),
                subtitleCue(
                    cueId = "subtitle-002",
                    panelId = "panel-002",
                    audioCueId = "audio-002",
                    text = "先把印記露出來。",
                    startMillis = 2200,
                    endMillis = 4400,
                ),
            ),
            timing = timing(totalDurationMillis = 4400),
            boundaryChecklist = listOf("metadata_only", "manual_voice_plan", "candidate_timeline"),
            residualRisks = listOf("Needs later renderer handoff review."),
        )

    private fun panel(
        panelId: String = "panel-001",
        sourceFrameId: String = "frame-001",
        beatReference: String = "beat-arrival",
        visualIntent: String = "Show Mira arriving after the paid toll while the guard checks the visible mark.",
        cameraIntent: String = "Medium shot clarifying gate distance and visible mark.",
        displayDurationMillis: Int = 2000,
        localImagePath: String = "",
        remoteImageUrl: String = "",
        providerJobId: String = "",
    ): ComicVideoPanelTrack =
        ComicVideoPanelTrack(
            panelId = panelId,
            sourceFrameId = sourceFrameId,
            sceneReference = "checkpoint-gate",
            beatReference = beatReference,
            visualIntent = visualIntent,
            cameraIntent = cameraIntent,
            displayDurationMillis = displayDurationMillis,
            mustShow = listOf("Mira has already paid the door toll before reaching the checkpoint."),
            doNotShow = listOf("Do not reveal hidden culprit evidence."),
            continuityAnchors = listOf("The guard requires a visible mark before answering questions."),
            assetCandidateRefs = listOf("asset-visible-mark"),
            localImagePath = localImagePath,
            remoteImageUrl = remoteImageUrl,
            providerJobId = providerJobId,
        )

    private fun voice(notes: String = ""): ComicVideoVoiceCast =
        ComicVideoVoiceCast(
            voiceId = "voice-mira",
            characterId = "char-mira",
            displayName = "Mira",
            voiceLabel = "local-voice-mira",
            notes = notes,
        )

    private fun audioCue(
        cueId: String = "audio-001",
        panelId: String = "panel-001",
        voiceId: String = "voice-mira",
        text: String = "我已經付過門費。",
        startMillis: Int = 0,
        durationMillis: Int = 1800,
        audioAssetPath: String = "",
        remoteAudioUrl: String = "",
        providerJobId: String = "",
        generatedStatus: String = "",
    ): ComicVideoAudioCue =
        ComicVideoAudioCue(
            cueId = cueId,
            panelId = panelId,
            voiceId = voiceId,
            characterId = "char-mira",
            text = text,
            startMillis = startMillis,
            durationMillis = durationMillis,
            audioAssetPath = audioAssetPath,
            remoteAudioUrl = remoteAudioUrl,
            providerJobId = providerJobId,
            generatedStatus = generatedStatus,
        )

    private fun subtitleCue(
        cueId: String = "subtitle-001",
        panelId: String = "panel-001",
        audioCueId: String = "audio-001",
        text: String = "我已經付過門費。",
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
