package com.localnovel.engine.service

import com.localnovel.engine.model.ComicVideoAudioCue
import com.localnovel.engine.model.ComicVideoPacket
import com.localnovel.engine.model.ComicVideoPacketValidationEvidence
import com.localnovel.engine.model.ComicVideoPacketValidationFinding
import com.localnovel.engine.model.ComicVideoPacketValidationReport
import com.localnovel.engine.model.ComicVideoSubtitleCue
import com.localnovel.engine.model.ComicVideoVoiceCast

class ComicVideoPacketValidationService {
    private val requiredPacketStatus = "comic_video_packet_candidate"
    private val requiredPacketNextStep = "comic_video_packet_validation"
    private val requiredPacketBoundary = "comic_video_packet_metadata_only"
    private val requiredImageFacingProofNextStep = "image_facing_delivery_proof_ready"
    private val nextAllowedDownstreamStep = "comic_video_packet_validated"
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
    private val embeddedUrlRegex = Regex("""(?i)\b[a-z][a-z0-9+.-]*://""")
    private val providerReferenceRegex = Regex("""(?i)\bprovider[-_\s]?job\b|\bprovider[-_\s]?id\b|\bapi[-_\s]?(response|result)\b|\bgeneration[-_\s]?api\b""")
    private val forbiddenStatusPatterns = listOf(
        Regex("""(?i)\bapproved\b"""),
        Regex("""(?i)\bregistered\b"""),
        Regex("""(?i)\bcanon\b"""),
        Regex("""(?i)\bproduction[-_\s]?ready\b"""),
        Regex("""(?i)\bproduction[-_\s]?lock(?:ed)?\b"""),
        Regex("""(?i)\blocked\b"""),
        Regex("""(?i)\bfinal(?:[-_\s]?(asset|video|audio|image|media))?\b"""),
        Regex("\u6838\u51c6|\u6279\u51c6|\u767b\u8a18\u5b8c\u6210|\u6b63\u5178|\u9396\u5b9a|\u6b63\u5f0f\u7d20\u6750"),
    )
    private val forbiddenClaimPatterns = listOf(
        Regex("""(?i)\bimage\s+generated\b|\bgenerated\s+image\b"""),
        Regex("""(?i)\baudio\s+generated\b|\bgenerated\s+audio\b"""),
        Regex("""(?i)\bvideo\s+generated\b|\bgenerated\s+video\b"""),
        Regex("""(?i)\brendered\s+(video|audio|image|panel)\b"""),
        Regex("""(?i)\bprovider[-_\s]?job\b"""),
        Regex("""(?i)\bapi[-_\s]?(response|result)\b"""),
        Regex("""(?i)\bruntime\s+scene\b"""),
        Regex("""(?i)\basset\s+approval\b|\basset\s+approved\b|\buser\s+approval\b|\buser\s+approved\b"""),
        Regex("""(?i)\bvideo[-_\s]?output[-_\s]?path\b|\baudio[-_\s]?output[-_\s]?path\b|\bimage[-_\s]?output[-_\s]?path\b"""),
        Regex("\u5df2\u751f\u6210\u5f71\u50cf|\u5df2\u751f\u6210\u97f3\u8a0a|\u5df2\u751f\u6210\u5716\u50cf|\u5df2\u5b8c\u6210\u5a92\u9ad4|\u88fd\u4f5c\u9396\u5b9a"),
    )

    fun validate(
        packet: ComicVideoPacket,
        packetFile: String = "",
        validationId: String = "",
    ): ComicVideoPacketValidationReport {
        val findings = mutableListOf<ComicVideoPacketValidationFinding>()
        validatePacketState(packet, findings)
        val sourceFieldCount = validateSourceFields(packet, packetFile, findings)
        val panelDurationTotal = validatePanelTracks(packet, findings)
        val voiceById = validateVoiceCast(packet.voiceCast, findings)
        val panelIds = packet.panelTracks.map { it.panelId.trim() }.filter { it.isNotBlank() }.toSet()
        val audioIds = packet.audioCues.map { it.cueId.trim() }.filter { it.isNotBlank() }.toSet()
        val crossReferenceMismatchCount =
            validateAudioCues(packet.audioCues, panelIds, voiceById, findings) +
                validateSubtitleCues(packet.subtitleCues, panelIds, audioIds, findings)
        validateTiming(packet, panelDurationTotal, findings)
        val generatedMediaFieldCount = validateGeneratedMediaFields(packet, findings)
        val remoteOrProviderFieldCount = scanText(
            packet = packet,
            ruleId = "REMOTE_OR_PROVIDER_REFERENCE_FORBIDDEN",
            category = "safety_boundary",
            message = "Comic video packet validation cannot carry remote URLs, provider job references, provider ids, or API results.",
            patterns = listOf(embeddedUrlRegex, providerReferenceRegex),
            findings = findings,
        )
        val forbiddenStatusCount = scanText(
            packet = packet,
            ruleId = "COMIC_VIDEO_STATUS_FORBIDDEN",
            category = "comic_video_boundary",
            message = "Comic video packet validation cannot carry approved, registered, canon, final, locked, or production-ready wording.",
            patterns = forbiddenStatusPatterns,
            findings = findings,
        )
        val forbiddenClaimCount = scanText(
            packet = packet,
            ruleId = "DOWNSTREAM_MEDIA_COMPLETION_CLAIM_FORBIDDEN",
            category = "safety_boundary",
            message = "Comic video packet validation cannot carry generated media, rendered output, provider, API, approval, canon, or runtime scene claims.",
            patterns = forbiddenClaimPatterns,
            findings = findings,
        )

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") {
            findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId }
        } else {
            ""
        }
        val resolvedValidationId = validationId.ifBlank {
            packet.packetId.takeIf { it.isNotBlank() }?.let { "$it-validation" }.orEmpty()
        }

        return ComicVideoPacketValidationReport(
            verdict = verdict,
            validationId = resolvedValidationId,
            comicVideoPacketFile = packetFile,
            packetId = packet.packetId,
            packetStatus = packet.packetStatus,
            reviewableStoryboardPacketFile = packet.reviewableStoryboardPacketFile,
            imageFacingDeliveryProofFile = packet.imageFacingDeliveryProofFile,
            sourceReviewableStoryboardPacketId = packet.sourceReviewableStoryboardPacketId,
            sourceImageFacingDeliveryProofId = packet.sourceImageFacingDeliveryProofId,
            acceptedScriptPacketId = packet.acceptedScriptPacketId,
            scriptPacketDraftFile = packet.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = packet.scriptPacketDraftContentSha256,
            sourceVerdicts = packet.sourceVerdicts,
            panelTracks = if (verdict == "PASS") packet.panelTracks else emptyList(),
            voiceCast = if (verdict == "PASS") packet.voiceCast else emptyList(),
            audioCues = if (verdict == "PASS") packet.audioCues else emptyList(),
            subtitleCues = if (verdict == "PASS") packet.subtitleCues else emptyList(),
            timing = if (verdict == "PASS") packet.timing else ComicVideoPacket().timing,
            nextAllowedDownstreamStep = if (verdict == "PASS") nextAllowedDownstreamStep else "",
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = ComicVideoPacketValidationEvidence(
                sourceFieldCount = sourceFieldCount,
                panelTrackCount = packet.panelTracks.size,
                uniquePanelIdCount = panelIds.size,
                panelDurationMillisTotal = panelDurationTotal,
                voiceCastCount = packet.voiceCast.size,
                uniqueVoiceIdCount = packet.voiceCast.map { it.voiceId.trim() }.filter { it.isNotBlank() }.toSet().size,
                audioCueCount = packet.audioCues.size,
                subtitleCueCount = packet.subtitleCues.size,
                crossReferenceMismatchCount = crossReferenceMismatchCount,
                generatedMediaFieldCount = generatedMediaFieldCount,
                remoteOrProviderFieldCount = remoteOrProviderFieldCount,
                forbiddenClaimCount = forbiddenClaimCount,
                forbiddenStatusCount = forbiddenStatusCount,
            ),
        )
    }

    private fun validatePacketState(packet: ComicVideoPacket, findings: MutableList<ComicVideoPacketValidationFinding>) {
        if (packet.packetStatus != requiredPacketStatus) {
            findings.add(finding("COMIC_VIDEO_PACKET_STATUS_MISMATCH", "comic_video_boundary", "CRITICAL", "$.packetStatus", "Comic video packet must remain comic_video_packet_candidate.", listOf(packet.packetStatus)))
        }
        if (packet.nextAllowedDownstreamStep != requiredPacketNextStep) {
            findings.add(finding("COMIC_VIDEO_PACKET_NEXT_STEP_MISMATCH", "comic_video_boundary", "CRITICAL", "$.nextAllowedDownstreamStep", "Comic video packet must enter validation from comic_video_packet_validation.", listOf(packet.nextAllowedDownstreamStep)))
        }
        if (packet.downstreamBoundary != requiredPacketBoundary) {
            findings.add(finding("COMIC_VIDEO_PACKET_BOUNDARY_MISMATCH", "comic_video_boundary", "CRITICAL", "$.downstreamBoundary", "Comic video packet must preserve metadata-only boundary wording.", listOf(packet.downstreamBoundary)))
        }
    }

    private fun validateSourceFields(
        packet: ComicVideoPacket,
        packetFile: String,
        findings: MutableList<ComicVideoPacketValidationFinding>,
    ): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) {
                findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Comic video packet validation requires nonblank $label."))
            } else {
                count += 1
            }
        }
        requireNonBlank(packetFile, "$.inputs.comicVideoPacketFile", "COMIC_VIDEO_PACKET_FILE_MISSING", "comic video packet file")
        requireNonBlank(packet.packetId, "$.packetId", "COMIC_VIDEO_PACKET_ID_MISSING", "packet id")
        requireNonBlank(packet.reviewableStoryboardPacketFile, "$.reviewableStoryboardPacketFile", "REVIEWABLE_STORYBOARD_PACKET_FILE_MISSING", "reviewable storyboard packet file")
        requireNonBlank(packet.imageFacingDeliveryProofFile, "$.imageFacingDeliveryProofFile", "IMAGE_FACING_DELIVERY_PROOF_FILE_MISSING", "image-facing delivery proof file")
        requireNonBlank(packet.sourceReviewableStoryboardPacketId, "$.sourceReviewableStoryboardPacketId", "SOURCE_REVIEWABLE_STORYBOARD_PACKET_ID_MISSING", "source reviewable storyboard packet id")
        requireNonBlank(packet.sourceImageFacingDeliveryProofId, "$.sourceImageFacingDeliveryProofId", "SOURCE_IMAGE_FACING_DELIVERY_PROOF_ID_MISSING", "source image-facing delivery proof id")
        requireNonBlank(packet.acceptedScriptPacketId, "$.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(packet.scriptPacketDraftFile, "$.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(packet.scriptPacketDraftContentSha256, "$.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")

        if (packet.scriptPacketDraftContentSha256.isNotBlank() && !sha256Regex.matches(packet.scriptPacketDraftContentSha256.trim())) {
            findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.scriptPacketDraftContentSha256", "Comic video packet validation requires lowercase 64-character SHA-256.", listOf(packet.scriptPacketDraftContentSha256)))
        }
        if (packet.sourceVerdicts.reviewableStoryboardPacketVerdict.trim().uppercase() != "PASS") {
            findings.add(finding("SOURCE_REVIEWABLE_STORYBOARD_PACKET_NOT_PASS", "source_linkage", "CRITICAL", "$.sourceVerdicts.reviewableStoryboardPacketVerdict", "Comic video packet validation requires PASS reviewable storyboard packet evidence.", listOf(packet.sourceVerdicts.reviewableStoryboardPacketVerdict)))
        }
        if (packet.sourceVerdicts.imageFacingDeliveryProofVerdict.trim().uppercase() != "PASS") {
            findings.add(finding("SOURCE_IMAGE_FACING_DELIVERY_PROOF_NOT_PASS", "source_linkage", "CRITICAL", "$.sourceVerdicts.imageFacingDeliveryProofVerdict", "Comic video packet validation requires PASS image-facing delivery proof evidence.", listOf(packet.sourceVerdicts.imageFacingDeliveryProofVerdict)))
        }
        if (packet.sourceVerdicts.imageFacingDeliveryProofNextAllowedDownstreamStep != requiredImageFacingProofNextStep) {
            findings.add(finding("SOURCE_IMAGE_FACING_DELIVERY_PROOF_NEXT_STEP_MISMATCH", "source_linkage", "CRITICAL", "$.sourceVerdicts.imageFacingDeliveryProofNextAllowedDownstreamStep", "Comic video packet validation requires image_facing_delivery_proof_ready upstream permission.", listOf(packet.sourceVerdicts.imageFacingDeliveryProofNextAllowedDownstreamStep)))
        }
        return count
    }

    private fun validatePanelTracks(packet: ComicVideoPacket, findings: MutableList<ComicVideoPacketValidationFinding>): Int {
        if (packet.panelTracks.isEmpty()) {
            findings.add(finding("PANEL_TRACKS_MISSING", "comic_video_timeline", "CRITICAL", "$.panelTracks", "Comic video packet validation requires at least one panel track."))
            return 0
        }
        val ids = packet.panelTracks.map { it.panelId.trim() }
        val duplicates = ids.groupingBy { it }.eachCount().filter { it.key.isNotBlank() && it.value > 1 }.keys
        if (duplicates.isNotEmpty()) {
            findings.add(finding("PANEL_IDS_NOT_UNIQUE", "comic_video_timeline", "CRITICAL", "$.panelTracks", "Comic video panel ids must be unique.", duplicates.toList()))
        }
        packet.panelTracks.forEachIndexed { index, panel ->
            val path = "$.panelTracks[$index]"
            requireField(panel.panelId, "$path.panelId", "PANEL_ID_MISSING", "panel id", findings)
            requireField(panel.sourceFrameId, "$path.sourceFrameId", "SOURCE_FRAME_ID_MISSING", "source frame id", findings)
            requireField(panel.visualIntent, "$path.visualIntent", "PANEL_VISUAL_INTENT_MISSING", "visual intent", findings)
            requireField(panel.cameraIntent, "$path.cameraIntent", "PANEL_CAMERA_INTENT_MISSING", "camera intent", findings)
            if (panel.displayDurationMillis <= 0) {
                findings.add(finding("PANEL_DURATION_MISSING", "comic_video_timeline", "CRITICAL", "$path.displayDurationMillis", "Comic video panel requires positive displayDurationMillis.", listOf(panel.displayDurationMillis.toString())))
            }
            if (panel.mustShow.none { it.isNotBlank() }) {
                findings.add(finding("PANEL_MUST_SHOW_MISSING", "comic_video_timeline", "MAJOR", "$path.mustShow", "Comic video panel requires must-show constraints."))
            }
            if (panel.doNotShow.none { it.isNotBlank() }) {
                findings.add(finding("PANEL_DO_NOT_SHOW_MISSING", "comic_video_timeline", "MAJOR", "$path.doNotShow", "Comic video panel requires do-not-show constraints."))
            }
            if (panel.continuityAnchors.none { it.isNotBlank() }) {
                findings.add(finding("PANEL_CONTINUITY_ANCHOR_MISSING", "comic_video_timeline", "MAJOR", "$path.continuityAnchors", "Comic video panel requires continuity anchors."))
            }
        }
        return packet.panelTracks.sumOf { maxOf(0, it.displayDurationMillis) }
    }

    private fun validateVoiceCast(voiceCast: List<ComicVideoVoiceCast>, findings: MutableList<ComicVideoPacketValidationFinding>): Map<String, ComicVideoVoiceCast> {
        if (voiceCast.isEmpty()) {
            findings.add(finding("VOICE_CAST_MISSING", "comic_video_voice", "CRITICAL", "$.voiceCast", "Comic video packet validation requires voice cast entries."))
            return emptyMap()
        }
        val ids = voiceCast.map { it.voiceId.trim() }
        val duplicates = ids.groupingBy { it }.eachCount().filter { it.key.isNotBlank() && it.value > 1 }.keys
        if (duplicates.isNotEmpty()) {
            findings.add(finding("VOICE_IDS_NOT_UNIQUE", "comic_video_voice", "CRITICAL", "$.voiceCast", "Voice cast ids must be unique.", duplicates.toList()))
        }
        voiceCast.forEachIndexed { index, voice ->
            val path = "$.voiceCast[$index]"
            requireField(voice.voiceId, "$path.voiceId", "VOICE_ID_MISSING", "voice id", findings)
            requireField(voice.characterId, "$path.characterId", "VOICE_CHARACTER_ID_MISSING", "voice character id", findings)
            requireField(voice.displayName, "$path.displayName", "VOICE_DISPLAY_NAME_MISSING", "voice display name", findings)
            requireField(voice.voiceLabel, "$path.voiceLabel", "VOICE_LABEL_MISSING", "voice label", findings)
            if (voice.voiceBoundary != "voice_cast_plan_only") {
                findings.add(finding("VOICE_BOUNDARY_MISMATCH", "comic_video_voice", "CRITICAL", "$path.voiceBoundary", "Voice cast must remain a plan-only metadata boundary.", listOf(voice.voiceBoundary)))
            }
        }
        return voiceCast.filter { it.voiceId.isNotBlank() }.associateBy { it.voiceId.trim() }
    }

    private fun validateAudioCues(
        audioCues: List<ComicVideoAudioCue>,
        panelIds: Set<String>,
        voiceById: Map<String, ComicVideoVoiceCast>,
        findings: MutableList<ComicVideoPacketValidationFinding>,
    ): Int {
        if (audioCues.isEmpty()) {
            findings.add(finding("AUDIO_CUES_MISSING", "comic_video_voice", "CRITICAL", "$.audioCues", "Comic video packet validation requires audio cue intent entries."))
            return 0
        }
        var crossReferenceMismatchCount = 0
        val ids = audioCues.map { it.cueId.trim() }
        val duplicates = ids.groupingBy { it }.eachCount().filter { it.key.isNotBlank() && it.value > 1 }.keys
        if (duplicates.isNotEmpty()) {
            findings.add(finding("AUDIO_CUE_IDS_NOT_UNIQUE", "comic_video_voice", "CRITICAL", "$.audioCues", "Audio cue ids must be unique.", duplicates.toList()))
        }
        audioCues.forEachIndexed { index, cue ->
            val path = "$.audioCues[$index]"
            requireField(cue.cueId, "$path.cueId", "AUDIO_CUE_ID_MISSING", "audio cue id", findings)
            requireField(cue.panelId, "$path.panelId", "AUDIO_CUE_PANEL_ID_MISSING", "audio cue panel id", findings)
            requireField(cue.voiceId, "$path.voiceId", "AUDIO_CUE_VOICE_ID_MISSING", "audio cue voice id", findings)
            requireField(cue.characterId, "$path.characterId", "AUDIO_CUE_CHARACTER_ID_MISSING", "audio cue character id", findings)
            requireField(cue.text, "$path.text", "AUDIO_CUE_TEXT_MISSING", "audio cue text", findings)
            if (cue.startMillis < 0) {
                findings.add(finding("AUDIO_CUE_START_NEGATIVE", "comic_video_timing", "CRITICAL", "$path.startMillis", "Audio cue startMillis cannot be negative.", listOf(cue.startMillis.toString())))
            }
            if (cue.durationMillis <= 0) {
                findings.add(finding("AUDIO_CUE_DURATION_MISSING", "comic_video_timing", "CRITICAL", "$path.durationMillis", "Audio cue requires positive durationMillis.", listOf(cue.durationMillis.toString())))
            }
            if (cue.panelId.isNotBlank() && cue.panelId.trim() !in panelIds) {
                crossReferenceMismatchCount += 1
                findings.add(finding("AUDIO_CUE_PANEL_REF_MISSING", "cross_reference", "CRITICAL", "$path.panelId", "Audio cue panelId must reference an existing panel track.", listOf(cue.panelId)))
            }
            val voice = voiceById[cue.voiceId.trim()]
            if (cue.voiceId.isNotBlank() && voice == null) {
                crossReferenceMismatchCount += 1
                findings.add(finding("AUDIO_CUE_VOICE_REF_MISSING", "cross_reference", "CRITICAL", "$path.voiceId", "Audio cue voiceId must reference an existing voice cast entry.", listOf(cue.voiceId)))
            }
            if (voice != null && cue.characterId.isNotBlank() && cue.characterId.trim() != voice.characterId.trim()) {
                crossReferenceMismatchCount += 1
                findings.add(finding("AUDIO_CUE_CHARACTER_VOICE_MISMATCH", "cross_reference", "CRITICAL", "$path.characterId", "Audio cue characterId must match the referenced voice cast characterId.", listOf("cue=${cue.characterId}", "voice=${voice.characterId}")))
            }
        }
        return crossReferenceMismatchCount
    }

    private fun validateSubtitleCues(
        subtitleCues: List<ComicVideoSubtitleCue>,
        panelIds: Set<String>,
        audioIds: Set<String>,
        findings: MutableList<ComicVideoPacketValidationFinding>,
    ): Int {
        if (subtitleCues.isEmpty()) {
            findings.add(finding("SUBTITLE_CUES_MISSING", "comic_video_subtitle", "CRITICAL", "$.subtitleCues", "Comic video packet validation requires subtitle cue entries."))
            return 0
        }
        var crossReferenceMismatchCount = 0
        val ids = subtitleCues.map { it.cueId.trim() }
        val duplicates = ids.groupingBy { it }.eachCount().filter { it.key.isNotBlank() && it.value > 1 }.keys
        if (duplicates.isNotEmpty()) {
            findings.add(finding("SUBTITLE_CUE_IDS_NOT_UNIQUE", "comic_video_subtitle", "CRITICAL", "$.subtitleCues", "Subtitle cue ids must be unique.", duplicates.toList()))
        }
        subtitleCues.forEachIndexed { index, cue ->
            val path = "$.subtitleCues[$index]"
            requireField(cue.cueId, "$path.cueId", "SUBTITLE_CUE_ID_MISSING", "subtitle cue id", findings)
            requireField(cue.panelId, "$path.panelId", "SUBTITLE_PANEL_ID_MISSING", "subtitle panel id", findings)
            requireField(cue.audioCueId, "$path.audioCueId", "SUBTITLE_AUDIO_CUE_ID_MISSING", "subtitle audio cue id", findings)
            requireField(cue.text, "$path.text", "SUBTITLE_TEXT_MISSING", "subtitle text", findings)
            if (cue.startMillis < 0) {
                findings.add(finding("SUBTITLE_START_NEGATIVE", "comic_video_timing", "CRITICAL", "$path.startMillis", "Subtitle cue startMillis cannot be negative.", listOf(cue.startMillis.toString())))
            }
            if (cue.endMillis <= cue.startMillis) {
                findings.add(finding("SUBTITLE_END_NOT_AFTER_START", "comic_video_timing", "CRITICAL", "$path.endMillis", "Subtitle cue endMillis must be greater than startMillis.", listOf("start=${cue.startMillis}", "end=${cue.endMillis}")))
            }
            if (cue.panelId.isNotBlank() && cue.panelId.trim() !in panelIds) {
                crossReferenceMismatchCount += 1
                findings.add(finding("SUBTITLE_PANEL_REF_MISSING", "cross_reference", "CRITICAL", "$path.panelId", "Subtitle cue panelId must reference an existing panel track.", listOf(cue.panelId)))
            }
            if (cue.audioCueId.isNotBlank() && cue.audioCueId.trim() !in audioIds) {
                crossReferenceMismatchCount += 1
                findings.add(finding("SUBTITLE_AUDIO_CUE_REF_MISSING", "cross_reference", "CRITICAL", "$path.audioCueId", "Subtitle cue audioCueId must reference an existing audio cue.", listOf(cue.audioCueId)))
            }
        }
        return crossReferenceMismatchCount
    }

    private fun validateTiming(
        packet: ComicVideoPacket,
        panelDurationTotal: Int,
        findings: MutableList<ComicVideoPacketValidationFinding>,
    ) {
        if (packet.timing.timelineStatus != "metadata_only_timing_plan") {
            findings.add(finding("TIMELINE_STATUS_MISMATCH", "comic_video_timing", "CRITICAL", "$.timing.timelineStatus", "Comic video timeline must remain metadata_only_timing_plan.", listOf(packet.timing.timelineStatus)))
        }
        if (packet.timing.totalDurationMillis <= 0) {
            findings.add(finding("TIMELINE_DURATION_MISSING", "comic_video_timing", "CRITICAL", "$.timing.totalDurationMillis", "Comic video timing requires positive totalDurationMillis.", listOf(packet.timing.totalDurationMillis.toString())))
        }
        if (packet.timing.frameRate.isBlank()) {
            findings.add(finding("TIMELINE_FRAME_RATE_MISSING", "comic_video_timing", "CRITICAL", "$.timing.frameRate", "Comic video timing requires frameRate."))
        }
        if (packet.timing.resolution.isBlank()) {
            findings.add(finding("TIMELINE_RESOLUTION_MISSING", "comic_video_timing", "CRITICAL", "$.timing.resolution", "Comic video timing requires resolution."))
        }
        if (packet.timing.backgroundPolicy.isBlank()) {
            findings.add(finding("TIMELINE_BACKGROUND_POLICY_MISSING", "comic_video_timing", "CRITICAL", "$.timing.backgroundPolicy", "Comic video timing requires backgroundPolicy."))
        }
        val maxAudioEnd = packet.audioCues.maxOfOrNull { it.startMillis + maxOf(0, it.durationMillis) } ?: 0
        val maxSubtitleEnd = packet.subtitleCues.maxOfOrNull { it.endMillis } ?: 0
        val requiredDuration = maxOf(panelDurationTotal, maxAudioEnd, maxSubtitleEnd)
        if (packet.timing.totalDurationMillis > 0 && requiredDuration > packet.timing.totalDurationMillis) {
            findings.add(finding("TIMELINE_DURATION_TOO_SHORT", "comic_video_timing", "CRITICAL", "$.timing.totalDurationMillis", "Comic video totalDurationMillis must cover panel, audio, and subtitle timing.", listOf("total=${packet.timing.totalDurationMillis}", "required=$requiredDuration")))
        }
    }

    private fun validateGeneratedMediaFields(
        packet: ComicVideoPacket,
        findings: MutableList<ComicVideoPacketValidationFinding>,
    ): Int {
        var count = 0
        val fields = mutableListOf<TextField>()
        packet.panelTracks.forEachIndexed { index, panel ->
            val path = "$.panelTracks[$index]"
            fields += TextField("$path.localImagePath", panel.localImagePath)
            fields += TextField("$path.renderedPanelPath", panel.renderedPanelPath)
            fields += TextField("$path.remoteImageUrl", panel.remoteImageUrl)
            fields += TextField("$path.providerJobId", panel.providerJobId)
        }
        packet.audioCues.forEachIndexed { index, cue ->
            val path = "$.audioCues[$index]"
            fields += TextField("$path.audioAssetPath", cue.audioAssetPath)
            fields += TextField("$path.remoteAudioUrl", cue.remoteAudioUrl)
            fields += TextField("$path.providerJobId", cue.providerJobId)
            fields += TextField("$path.generatedStatus", cue.generatedStatus)
        }
        fields += TextField("$.timing.outputVideoPath", packet.timing.outputVideoPath)
        fields += TextField("$.timing.renderedVideoPath", packet.timing.renderedVideoPath)
        fields.filter { it.value.isNotBlank() }.forEach { field ->
            count += 1
            findings.add(finding("MEDIA_OUTPUT_FIELD_FORBIDDEN", "safety_boundary", "CRITICAL", field.path, "Comic video packet is metadata-only and cannot carry generated media paths, output paths, provider jobs, remote media URLs, or generated status.", listOf(field.value)))
        }
        return count
    }

    private fun scanText(
        packet: ComicVideoPacket,
        ruleId: String,
        category: String,
        message: String,
        patterns: List<Regex>,
        findings: MutableList<ComicVideoPacketValidationFinding>,
    ): Int {
        var count = 0
        collectText(packet).filter { it.value.isNotBlank() }.forEach { field ->
            patterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings.add(finding(ruleId, category, "CRITICAL", field.path, message, listOf(field.value), "Keep the comic video packet metadata-only and candidate-only."))
                }
            }
        }
        return count
    }

    private fun collectText(packet: ComicVideoPacket): List<TextField> {
        val fields = mutableListOf(
            TextField("$.packetId", packet.packetId),
            TextField("$.packetStatus", packet.packetStatus),
            TextField("$.reviewableStoryboardPacketFile", packet.reviewableStoryboardPacketFile),
            TextField("$.imageFacingDeliveryProofFile", packet.imageFacingDeliveryProofFile),
            TextField("$.sourceReviewableStoryboardPacketId", packet.sourceReviewableStoryboardPacketId),
            TextField("$.sourceImageFacingDeliveryProofId", packet.sourceImageFacingDeliveryProofId),
            TextField("$.acceptedScriptPacketId", packet.acceptedScriptPacketId),
            TextField("$.scriptPacketDraftFile", packet.scriptPacketDraftFile),
            TextField("$.sourceVerdicts.reviewableStoryboardPacketVerdict", packet.sourceVerdicts.reviewableStoryboardPacketVerdict),
            TextField("$.sourceVerdicts.imageFacingDeliveryProofVerdict", packet.sourceVerdicts.imageFacingDeliveryProofVerdict),
            TextField("$.sourceVerdicts.imageFacingDeliveryProofNextAllowedDownstreamStep", packet.sourceVerdicts.imageFacingDeliveryProofNextAllowedDownstreamStep),
            TextField("$.timing.timelineStatus", packet.timing.timelineStatus),
            TextField("$.timing.frameRate", packet.timing.frameRate),
            TextField("$.timing.resolution", packet.timing.resolution),
            TextField("$.timing.backgroundPolicy", packet.timing.backgroundPolicy),
            TextField("$.timing.outputVideoPath", packet.timing.outputVideoPath),
            TextField("$.timing.renderedVideoPath", packet.timing.renderedVideoPath),
            TextField("$.downstreamBoundary", packet.downstreamBoundary),
            TextField("$.nextAllowedDownstreamStep", packet.nextAllowedDownstreamStep),
        )
        packet.boundaryChecklist.forEachIndexed { index, value -> fields += TextField("$.boundaryChecklist[$index]", value) }
        packet.residualRisks.forEachIndexed { index, value -> fields += TextField("$.residualRisks[$index]", value) }
        packet.panelTracks.forEachIndexed { index, panel ->
            val path = "$.panelTracks[$index]"
            fields += TextField("$path.panelId", panel.panelId)
            fields += TextField("$path.sourceFrameId", panel.sourceFrameId)
            fields += TextField("$path.sceneReference", panel.sceneReference)
            fields += TextField("$path.beatReference", panel.beatReference)
            fields += TextField("$path.visualIntent", panel.visualIntent)
            fields += TextField("$path.cameraIntent", panel.cameraIntent)
            fields += TextField("$path.transition", panel.transition)
            fields += TextField("$path.localImagePath", panel.localImagePath)
            fields += TextField("$path.renderedPanelPath", panel.renderedPanelPath)
            fields += TextField("$path.remoteImageUrl", panel.remoteImageUrl)
            fields += TextField("$path.providerJobId", panel.providerJobId)
            panel.mustShow.forEachIndexed { itemIndex, value -> fields += TextField("$path.mustShow[$itemIndex]", value) }
            panel.doNotShow.forEachIndexed { itemIndex, value -> fields += TextField("$path.doNotShow[$itemIndex]", value) }
            panel.continuityAnchors.forEachIndexed { itemIndex, value -> fields += TextField("$path.continuityAnchors[$itemIndex]", value) }
            panel.assetCandidateRefs.forEachIndexed { itemIndex, value -> fields += TextField("$path.assetCandidateRefs[$itemIndex]", value) }
        }
        packet.voiceCast.forEachIndexed { index, voice ->
            val path = "$.voiceCast[$index]"
            fields += TextField("$path.voiceId", voice.voiceId)
            fields += TextField("$path.characterId", voice.characterId)
            fields += TextField("$path.displayName", voice.displayName)
            fields += TextField("$path.voiceLabel", voice.voiceLabel)
            fields += TextField("$path.voiceBoundary", voice.voiceBoundary)
            fields += TextField("$path.notes", voice.notes)
        }
        packet.audioCues.forEachIndexed { index, cue ->
            val path = "$.audioCues[$index]"
            fields += TextField("$path.cueId", cue.cueId)
            fields += TextField("$path.panelId", cue.panelId)
            fields += TextField("$path.voiceId", cue.voiceId)
            fields += TextField("$path.characterId", cue.characterId)
            fields += TextField("$path.text", cue.text)
            fields += TextField("$path.audioAssetPath", cue.audioAssetPath)
            fields += TextField("$path.remoteAudioUrl", cue.remoteAudioUrl)
            fields += TextField("$path.providerJobId", cue.providerJobId)
            fields += TextField("$path.generatedStatus", cue.generatedStatus)
        }
        packet.subtitleCues.forEachIndexed { index, cue ->
            val path = "$.subtitleCues[$index]"
            fields += TextField("$path.cueId", cue.cueId)
            fields += TextField("$path.panelId", cue.panelId)
            fields += TextField("$path.audioCueId", cue.audioCueId)
            fields += TextField("$path.text", cue.text)
        }
        return fields
    }

    private fun requireField(
        value: String,
        path: String,
        ruleId: String,
        label: String,
        findings: MutableList<ComicVideoPacketValidationFinding>,
    ) {
        if (value.isBlank()) {
            findings.add(finding(ruleId, "comic_video_structure", "CRITICAL", path, "Comic video packet validation requires nonblank $label."))
        }
    }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): ComicVideoPacketValidationFinding =
        ComicVideoPacketValidationFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private data class TextField(val path: String, val value: String)
}
