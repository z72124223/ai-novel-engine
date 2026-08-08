package com.localnovel.engine.service

import com.localnovel.engine.model.ComicVideoAudioCue
import com.localnovel.engine.model.ComicVideoPacket
import com.localnovel.engine.model.ComicVideoSubtitleCue
import com.localnovel.engine.model.ComicVideoTiming
import com.localnovel.engine.model.ComicVideoVoiceCast
import com.localnovel.engine.model.ComicVideoVoiceTimelineFill

class ComicVideoVoiceTimelineFillService {
    private val requiredPacketStatus = "comic_video_packet_candidate"
    private val requiredPacketBoundary = "comic_video_packet_metadata_only"
    private val requiredPacketNextStep = "comic_video_packet_validation"
    private val requiredFillStatus = "comic_video_voice_timeline_fill_candidate"
    private val requiredTimelineStatus = "metadata_only_timing_plan"

    fun createTemplate(
        packet: ComicVideoPacket,
        fillId: String = "",
        voiceId: String = "voice-manual-001",
        characterId: String = "char-manual-001",
        displayName: String = "Manual Voice",
        voiceLabel: String = "local-manual-voice",
    ): ComicVideoVoiceTimelineFill {
        validatePacket(packet)
        require(voiceId.isNotBlank()) {
            "Comic video voice timeline fill template requires nonblank voice id."
        }
        require(characterId.isNotBlank()) {
            "Comic video voice timeline fill template requires nonblank character id."
        }
        require(displayName.isNotBlank()) {
            "Comic video voice timeline fill template requires nonblank display name."
        }
        require(voiceLabel.isNotBlank()) {
            "Comic video voice timeline fill template requires nonblank voice label."
        }

        var cursorMillis = 0
        val audioCues = mutableListOf<ComicVideoAudioCue>()
        val subtitleCues = mutableListOf<ComicVideoSubtitleCue>()
        packet.panelTracks.forEachIndexed { index, panel ->
            val panelPath = "panelTracks[$index]"
            require(panel.panelId.isNotBlank()) {
                "Comic video voice timeline fill template requires nonblank $panelPath.panelId."
            }
            require(panel.displayDurationMillis > 0) {
                "Comic video voice timeline fill template requires positive $panelPath.displayDurationMillis."
            }
            val sequence = (index + 1).toString().padStart(3, '0')
            val startMillis = cursorMillis
            val endMillis = startMillis + panel.displayDurationMillis
            val audioCueId = "audio-$sequence"
            audioCues += ComicVideoAudioCue(
                cueId = audioCueId,
                panelId = panel.panelId,
                voiceId = voiceId,
                characterId = characterId,
                text = "Replace with spoken line for ${panel.panelId}.",
                startMillis = startMillis,
                durationMillis = panel.displayDurationMillis,
            )
            subtitleCues += ComicVideoSubtitleCue(
                cueId = "subtitle-$sequence",
                panelId = panel.panelId,
                audioCueId = audioCueId,
                text = "Replace with subtitle text for ${panel.panelId}.",
                startMillis = startMillis,
                endMillis = endMillis,
            )
            cursorMillis = endMillis
        }

        return ComicVideoVoiceTimelineFill(
            fillId = fillId.ifBlank { "${packet.packetId}-voice-timeline-fill-template" },
            sourceComicVideoPacketId = packet.packetId,
            voiceCast = listOf(
                ComicVideoVoiceCast(
                    voiceId = voiceId,
                    characterId = characterId,
                    displayName = displayName,
                    voiceLabel = voiceLabel,
                    notes = "Editable local voice plan. Replace cue text before validation.",
                ),
            ),
            audioCues = audioCues,
            subtitleCues = subtitleCues,
            timing = ComicVideoTiming(
                totalDurationMillis = cursorMillis,
                frameRate = packet.timing.frameRate.ifBlank { "24" },
                resolution = packet.timing.resolution.ifBlank { "1920x1080" },
                backgroundPolicy = packet.timing.backgroundPolicy.ifBlank { "panel_track_only" },
            ),
            boundaryChecklist = listOf(
                "metadata_only",
                "voice_timeline_fill_template",
                "media_execution_absent",
                "remote_payload_absent",
            ),
            notes = listOf(
                "Template created from comic video packet panel tracks.",
                "Replace placeholder spoken/subtitle text before validation.",
            ),
        )
    }

    fun fill(
        packet: ComicVideoPacket,
        fill: ComicVideoVoiceTimelineFill,
    ): ComicVideoPacket {
        validatePacket(packet)
        validateFill(packet, fill)
        return packet.copy(
            voiceCast = fill.voiceCast,
            audioCues = fill.audioCues,
            subtitleCues = fill.subtitleCues,
            timing = fill.timing,
            boundaryChecklist = (
                packet.boundaryChecklist +
                    fill.boundaryChecklist +
                    listOf("voice_timeline_metadata_filled")
                ).filter { it.isNotBlank() }.distinct(),
            residualRisks = (
                packet.residualRisks +
                    fill.notes +
                    listOf("Filled voice timeline still requires validate-comic-video-packet before downstream use.")
                ).filter { it.isNotBlank() }.distinct(),
        )
    }

    private fun validatePacket(packet: ComicVideoPacket) {
        require(packet.packetId.isNotBlank()) {
            "Comic video voice timeline fill requires source packetId."
        }
        require(packet.packetStatus == requiredPacketStatus) {
            "Comic video voice timeline fill requires packetStatus=$requiredPacketStatus."
        }
        require(packet.downstreamBoundary == requiredPacketBoundary) {
            "Comic video voice timeline fill requires downstreamBoundary=$requiredPacketBoundary."
        }
        require(packet.nextAllowedDownstreamStep == requiredPacketNextStep) {
            "Comic video voice timeline fill requires nextAllowedDownstreamStep=$requiredPacketNextStep."
        }
        require(packet.panelTracks.isNotEmpty()) {
            "Comic video voice timeline fill requires at least one panel track."
        }
    }

    private fun validateFill(packet: ComicVideoPacket, fill: ComicVideoVoiceTimelineFill) {
        require(fill.sourceComicVideoPacketId == packet.packetId) {
            "Comic video voice timeline fill sourceComicVideoPacketId must match packetId."
        }
        require(fill.fillStatus == requiredFillStatus) {
            "Comic video voice timeline fill requires fillStatus=$requiredFillStatus."
        }
        require(fill.voiceCast.isNotEmpty()) {
            "Comic video voice timeline fill requires voiceCast entries."
        }
        require(fill.audioCues.isNotEmpty()) {
            "Comic video voice timeline fill requires audioCues entries."
        }
        require(fill.subtitleCues.isNotEmpty()) {
            "Comic video voice timeline fill requires subtitleCues entries."
        }
        require(fill.timing.timelineStatus == requiredTimelineStatus) {
            "Comic video voice timeline fill requires timing.timelineStatus=$requiredTimelineStatus."
        }
        require(fill.timing.outputVideoPath.isBlank() && fill.timing.renderedVideoPath.isBlank()) {
            "Comic video voice timeline fill cannot carry output or rendered video paths."
        }
        fill.audioCues.forEachIndexed { index, cue ->
            requireMediaFieldsBlank(index, cue)
        }
    }

    private fun requireMediaFieldsBlank(index: Int, cue: ComicVideoAudioCue) {
        val mediaFields = listOf(
            cue.audioAssetPath to "audioAssetPath",
            cue.remoteAudioUrl to "remoteAudioUrl",
            cue.providerJobId to "providerJobId",
            cue.generatedStatus to "generatedStatus",
        )
        mediaFields.forEach { (value, field) ->
            require(value.isBlank()) {
                "Comic video voice timeline fill cannot carry audio media/provider field audioCues[$index].$field."
            }
        }
    }
}
