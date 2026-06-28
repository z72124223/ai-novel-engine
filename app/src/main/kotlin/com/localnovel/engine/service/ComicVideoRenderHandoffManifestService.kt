package com.localnovel.engine.service

import com.localnovel.engine.model.ComicVideoPacketValidationReport
import com.localnovel.engine.model.ComicVideoRenderHandoffManifest
import com.localnovel.engine.model.ComicVideoRenderHandoffManifestEvidence
import com.localnovel.engine.model.ComicVideoRenderHandoffManifestFinding
import com.localnovel.engine.model.ComicVideoRenderPanelTimelineItem
import com.localnovel.engine.model.ComicVideoRenderSettings
import com.localnovel.engine.model.ComicVideoRenderSubtitleTimelineItem
import com.localnovel.engine.model.ComicVideoRenderVoiceTimelineItem

class ComicVideoRenderHandoffManifestService {
    private val requiredValidationNextStep = "comic_video_packet_validated"
    private val requiredValidationBoundary = "comic_video_packet_validation_only"
    private val manifestStatus = "comic_video_render_handoff_manifest_candidate"
    private val nextAllowedDownstreamStep = "external_manual_renderer_handoff"

    fun create(
        validationReport: ComicVideoPacketValidationReport,
        validationReportFile: String = "",
        manifestId: String = "",
    ): ComicVideoRenderHandoffManifest {
        val findings = mutableListOf<ComicVideoRenderHandoffManifestFinding>()
        val sourceFieldCount = validateSource(validationReport, validationReportFile, findings)
        val candidatePanelTimeline = if (findings.hasBlocking()) emptyList() else panelTimeline(validationReport)
        val candidateVoiceTimeline = if (findings.hasBlocking()) emptyList() else voiceTimeline(validationReport)
        val candidateSubtitleTimeline = if (findings.hasBlocking()) emptyList() else subtitleTimeline(validationReport)
        val mediaOutputFieldCount = validateNoMediaOutputFields(validationReport, findings)
        val verdict = if (findings.hasBlocking()) "FAIL" else "PASS"
        val panelTimeline = if (verdict == "PASS") candidatePanelTimeline else emptyList()
        val voiceTimeline = if (verdict == "PASS") candidateVoiceTimeline else emptyList()
        val subtitleTimeline = if (verdict == "PASS") candidateSubtitleTimeline else emptyList()
        val blockedReason = if (verdict == "FAIL") {
            findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId }
        } else {
            ""
        }
        val resolvedManifestId = manifestId.ifBlank {
            validationReport.packetId.takeIf { it.isNotBlank() }?.let { "$it-render-handoff-manifest" }.orEmpty()
        }

        return ComicVideoRenderHandoffManifest(
            verdict = verdict,
            manifestId = resolvedManifestId,
            manifestStatus = if (verdict == "PASS") manifestStatus else "FAIL",
            comicVideoPacketValidationReportFile = validationReportFile,
            validationId = validationReport.validationId,
            packetId = validationReport.packetId,
            acceptedScriptPacketId = validationReport.acceptedScriptPacketId,
            scriptPacketDraftFile = validationReport.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = validationReport.scriptPacketDraftContentSha256,
            panelTimeline = panelTimeline,
            voiceTimeline = voiceTimeline,
            subtitleTimeline = subtitleTimeline,
            renderSettings = if (verdict == "PASS") {
                ComicVideoRenderSettings(
                    timelineStatus = validationReport.timing.timelineStatus,
                    totalDurationMillis = validationReport.timing.totalDurationMillis,
                    frameRate = validationReport.timing.frameRate,
                    resolution = validationReport.timing.resolution,
                    backgroundPolicy = validationReport.timing.backgroundPolicy,
                )
            } else {
                ComicVideoRenderSettings()
            },
            nextAllowedDownstreamStep = if (verdict == "PASS") nextAllowedDownstreamStep else "",
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = ComicVideoRenderHandoffManifestEvidence(
                validationVerdict = validationReport.verdict,
                validationNextAllowedDownstreamStep = validationReport.nextAllowedDownstreamStep,
                sourceFieldCount = sourceFieldCount,
                panelTimelineItemCount = panelTimeline.size,
                voiceTimelineItemCount = voiceTimeline.size,
                subtitleTimelineItemCount = subtitleTimeline.size,
                totalDurationMillis = validationReport.timing.totalDurationMillis,
                mediaOutputFieldCount = mediaOutputFieldCount,
            ),
        )
    }

    private fun validateSource(
        report: ComicVideoPacketValidationReport,
        reportFile: String,
        findings: MutableList<ComicVideoRenderHandoffManifestFinding>,
    ): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) {
                findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Render handoff manifest requires nonblank $label."))
            } else {
                count += 1
            }
        }
        if (report.verdict.trim().uppercase() != "PASS") {
            findings.add(finding("COMIC_VIDEO_VALIDATION_NOT_PASS", "upstream", "CRITICAL", "$.verdict", "Render handoff manifest requires PASS comic video packet validation.", listOf(report.verdict)))
        }
        if (report.nextAllowedDownstreamStep != requiredValidationNextStep) {
            findings.add(finding("COMIC_VIDEO_VALIDATION_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.nextAllowedDownstreamStep", "Render handoff manifest requires nextAllowedDownstreamStep=$requiredValidationNextStep.", listOf(report.nextAllowedDownstreamStep)))
        }
        if (report.downstreamBoundary != requiredValidationBoundary) {
            findings.add(finding("COMIC_VIDEO_VALIDATION_BOUNDARY_MISMATCH", "upstream", "CRITICAL", "$.downstreamBoundary", "Render handoff manifest requires downstreamBoundary=$requiredValidationBoundary.", listOf(report.downstreamBoundary)))
        }
        requireNonBlank(reportFile, "$.inputs.comicVideoPacketValidationReportFile", "VALIDATION_REPORT_FILE_MISSING", "validation report file")
        requireNonBlank(report.validationId, "$.validationId", "VALIDATION_ID_MISSING", "validation id")
        requireNonBlank(report.packetId, "$.packetId", "PACKET_ID_MISSING", "packet id")
        requireNonBlank(report.acceptedScriptPacketId, "$.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(report.scriptPacketDraftFile, "$.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(report.scriptPacketDraftContentSha256, "$.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        if (report.panelTracks.isEmpty()) findings.add(finding("PANEL_TIMELINE_SOURCE_MISSING", "source_linkage", "CRITICAL", "$.panelTracks", "Render handoff manifest requires panel tracks from PASS validation."))
        if (report.voiceCast.isEmpty()) findings.add(finding("VOICE_CAST_SOURCE_MISSING", "source_linkage", "CRITICAL", "$.voiceCast", "Render handoff manifest requires voice cast from PASS validation."))
        if (report.audioCues.isEmpty()) findings.add(finding("AUDIO_TIMELINE_SOURCE_MISSING", "source_linkage", "CRITICAL", "$.audioCues", "Render handoff manifest requires audio cues from PASS validation."))
        if (report.subtitleCues.isEmpty()) findings.add(finding("SUBTITLE_TIMELINE_SOURCE_MISSING", "source_linkage", "CRITICAL", "$.subtitleCues", "Render handoff manifest requires subtitle cues from PASS validation."))
        return count
    }

    private fun validateNoMediaOutputFields(
        report: ComicVideoPacketValidationReport,
        findings: MutableList<ComicVideoRenderHandoffManifestFinding>,
    ): Int {
        var count = 0
        report.panelTracks.forEachIndexed { index, panel ->
            listOf(
                "localImagePath" to panel.localImagePath,
                "renderedPanelPath" to panel.renderedPanelPath,
                "remoteImageUrl" to panel.remoteImageUrl,
                "providerJobId" to panel.providerJobId,
            ).forEach { (field, value) ->
                if (value.isNotBlank()) {
                    count += 1
                    findings.add(finding("MEDIA_OUTPUT_FIELD_FORBIDDEN", "safety_boundary", "CRITICAL", "$.panelTracks[$index].$field", "Render handoff manifest cannot carry media output paths, remote URLs, or provider jobs.", listOf(value)))
                }
            }
        }
        report.audioCues.forEachIndexed { index, cue ->
            listOf(
                "audioAssetPath" to cue.audioAssetPath,
                "remoteAudioUrl" to cue.remoteAudioUrl,
                "providerJobId" to cue.providerJobId,
                "generatedStatus" to cue.generatedStatus,
            ).forEach { (field, value) ->
                if (value.isNotBlank()) {
                    count += 1
                    findings.add(finding("MEDIA_OUTPUT_FIELD_FORBIDDEN", "safety_boundary", "CRITICAL", "$.audioCues[$index].$field", "Render handoff manifest cannot carry audio output paths, remote URLs, provider jobs, or generated status.", listOf(value)))
                }
            }
        }
        listOf(
            "outputVideoPath" to report.timing.outputVideoPath,
            "renderedVideoPath" to report.timing.renderedVideoPath,
        ).forEach { (field, value) ->
            if (value.isNotBlank()) {
                count += 1
                findings.add(finding("MEDIA_OUTPUT_FIELD_FORBIDDEN", "safety_boundary", "CRITICAL", "$.timing.$field", "Render handoff manifest cannot carry output video paths.", listOf(value)))
            }
        }
        return count
    }

    private fun panelTimeline(report: ComicVideoPacketValidationReport): List<ComicVideoRenderPanelTimelineItem> {
        var cursor = 0
        return report.panelTracks.map { panel ->
            val start = cursor
            val end = start + panel.displayDurationMillis
            cursor = end
            ComicVideoRenderPanelTimelineItem(
                panelId = panel.panelId,
                sourceFrameId = panel.sourceFrameId,
                sceneReference = panel.sceneReference,
                beatReference = panel.beatReference,
                visualIntent = panel.visualIntent,
                cameraIntent = panel.cameraIntent,
                startMillis = start,
                endMillis = end,
                displayDurationMillis = panel.displayDurationMillis,
                transition = panel.transition,
                mustShow = panel.mustShow,
                doNotShow = panel.doNotShow,
                continuityAnchors = panel.continuityAnchors,
                assetCandidateRefs = panel.assetCandidateRefs,
            )
        }
    }

    private fun voiceTimeline(report: ComicVideoPacketValidationReport): List<ComicVideoRenderVoiceTimelineItem> {
        val voiceById = report.voiceCast.associateBy { it.voiceId.trim() }
        return report.audioCues.map { cue ->
            val voice = voiceById[cue.voiceId.trim()]
            ComicVideoRenderVoiceTimelineItem(
                cueId = cue.cueId,
                panelId = cue.panelId,
                voiceId = cue.voiceId,
                characterId = cue.characterId,
                displayName = voice?.displayName.orEmpty(),
                voiceLabel = voice?.voiceLabel.orEmpty(),
                text = cue.text,
                startMillis = cue.startMillis,
                endMillis = cue.startMillis + cue.durationMillis,
                durationMillis = cue.durationMillis,
            )
        }
    }

    private fun subtitleTimeline(report: ComicVideoPacketValidationReport): List<ComicVideoRenderSubtitleTimelineItem> =
        report.subtitleCues.map { cue ->
            ComicVideoRenderSubtitleTimelineItem(
                cueId = cue.cueId,
                panelId = cue.panelId,
                audioCueId = cue.audioCueId,
                text = cue.text,
                startMillis = cue.startMillis,
                endMillis = cue.endMillis,
            )
        }

    private fun MutableList<ComicVideoRenderHandoffManifestFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): ComicVideoRenderHandoffManifestFinding =
        ComicVideoRenderHandoffManifestFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
}
