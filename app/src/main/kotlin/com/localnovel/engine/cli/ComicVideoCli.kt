package com.localnovel.engine.cli

import com.localnovel.engine.model.ComicVideoAudioCue
import com.localnovel.engine.model.ComicVideoLocalPreviewBuildEvidence
import com.localnovel.engine.model.ComicVideoLocalPreviewBuildFinding
import com.localnovel.engine.model.ComicVideoLocalPreviewBuildReport
import com.localnovel.engine.model.ComicVideoLocalRenderReport
import com.localnovel.engine.model.ComicVideoPacket
import com.localnovel.engine.model.ComicVideoPacketSourceVerdicts
import com.localnovel.engine.model.ComicVideoPacketValidationReport
import com.localnovel.engine.model.ComicVideoPanelTrack
import com.localnovel.engine.model.ComicVideoProductionPackageDraft
import com.localnovel.engine.model.ComicVideoRenderHandoffManifest
import com.localnovel.engine.model.ComicVideoSubtitleCue
import com.localnovel.engine.model.ComicVideoTiming
import com.localnovel.engine.model.ComicVideoVoiceCast
import com.localnovel.engine.model.ComicVideoVoiceTimelineFill
import com.localnovel.engine.model.ImageFacingDeliveryProof
import com.localnovel.engine.service.ComicVideoLocalRendererService
import com.localnovel.engine.service.ComicVideoPacketService
import com.localnovel.engine.service.ComicVideoPacketValidationService
import com.localnovel.engine.service.ComicVideoProductionPackageDraftService
import com.localnovel.engine.service.ComicVideoProductionPackageValidationService
import com.localnovel.engine.service.ComicVideoRenderHandoffManifestService
import com.localnovel.engine.service.ComicVideoVoiceTimelineFillService
import java.nio.file.Files
import java.nio.file.Path

internal object ComicVideoCli {
    fun createComicVideoPacket(options: Map<String, String>): Int {
        val proofPath = Path.of(options.requiredCliOption("image-facing-delivery-proof"))
        val panelDurationMillis = options["panel-duration-ms"]?.toIntOrNull()
            ?: options["panel-duration-ms"]?.let { error("--panel-duration-ms must be an integer.") }
            ?: 3000
        val packet = ComicVideoPacketService().create(
            imageFacingDeliveryProof = readCliJson<ImageFacingDeliveryProof>(proofPath),
            imageFacingDeliveryProofFile = proofPath.toString(),
            packetId = options["packet"] ?: "",
            panelDurationMillis = panelDurationMillis,
        )
        options["out"]?.let { writeCliJson(Path.of(it), packet) }
        println("packetId=${packet.packetId}")
        println("packetStatus=${packet.packetStatus}")
        println("sourceImageFacingDeliveryProofId=${packet.sourceImageFacingDeliveryProofId}")
        println("acceptedScriptPacketId=${packet.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${packet.scriptPacketDraftFile}")
        println("scriptPacketSha256=${packet.scriptPacketDraftContentSha256}")
        println("panels=${packet.panelTracks.size}")
        println("voiceCast=${packet.voiceCast.size}")
        println("audioCues=${packet.audioCues.size}")
        println("subtitleCues=${packet.subtitleCues.size}")
        println("totalDurationMillis=${packet.timing.totalDurationMillis}")
        println("downstreamBoundary=${packet.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${packet.nextAllowedDownstreamStep}")
        println("validationNeeded=true")
        return 0
    }

    fun createComicVideoPacketTemplate(options: Map<String, String>): Int {
        val packetId = options["packet"] ?: "comic-video-packet-template"
        val out = Path.of(options.requiredCliOption("out"))
        val template = ComicVideoPacket(
            packetId = packetId,
            reviewableStoryboardPacketFile = options["reviewable-storyboard-packet"] ?: "",
            imageFacingDeliveryProofFile = options["image-facing-delivery-proof"] ?: "",
            acceptedScriptPacketId = options["accepted-script-packet"] ?: "",
            scriptPacketDraftFile = options["script-packet-draft"] ?: "",
            scriptPacketDraftContentSha256 = options["script-packet-sha256"] ?: "",
            sourceVerdicts = ComicVideoPacketSourceVerdicts(),
            panelTracks = listOf(
                ComicVideoPanelTrack(
                    panelId = "panel-001",
                    sourceFrameId = "frame-001",
                    sceneReference = "replace-with-scene-reference",
                    beatReference = "replace-with-beat-reference",
                    visualIntent = "Replace with visible panel intent from the reviewable storyboard packet.",
                    cameraIntent = "Replace with camera intent from the reviewable storyboard packet.",
                    displayDurationMillis = 3000,
                    mustShow = listOf("Replace with must-show visual constraint."),
                    doNotShow = listOf("Replace with do-not-show visual constraint."),
                    continuityAnchors = listOf("Replace with protected continuity anchor."),
                    assetCandidateRefs = listOf("replace-with-asset-candidate-ref"),
                ),
            ),
            voiceCast = listOf(
                ComicVideoVoiceCast(
                    voiceId = "voice-protagonist",
                    characterId = "char-protagonist",
                    displayName = "Protagonist",
                    voiceLabel = "Replace with local/manual voice label.",
                    notes = "Plan-only voice casting. Keep media fields blank.",
                ),
            ),
            audioCues = listOf(
                ComicVideoAudioCue(
                    cueId = "audio-001",
                    panelId = "panel-001",
                    voiceId = "voice-protagonist",
                    characterId = "char-protagonist",
                    text = "Replace with spoken Traditional Chinese line.",
                    startMillis = 0,
                    durationMillis = 3000,
                ),
            ),
            subtitleCues = listOf(
                ComicVideoSubtitleCue(
                    cueId = "subtitle-001",
                    panelId = "panel-001",
                    audioCueId = "audio-001",
                    text = "Replace with subtitle text.",
                    startMillis = 0,
                    endMillis = 3000,
                ),
            ),
            timing = ComicVideoTiming(totalDurationMillis = 3000),
            boundaryChecklist = listOf(
                "metadata_only",
                "media_execution_absent",
                "remote_payload_absent",
                "candidate_only_no_lock",
            ),
            residualRisks = listOf(
                "Template requires source ids, PASS source verdicts, and author-filled timeline details before validation can pass.",
            ),
        )
        writeCliJson(out, template)
        println("wrote=$out")
        println("packetId=${template.packetId}")
        println("packetStatus=${template.packetStatus}")
        println("panels=${template.panelTracks.size}")
        println("voiceCast=${template.voiceCast.size}")
        println("audioCues=${template.audioCues.size}")
        println("subtitleCues=${template.subtitleCues.size}")
        println("downstreamBoundary=${template.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${template.nextAllowedDownstreamStep}")
        return 0
    }

    fun fillComicVideoVoiceTimeline(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("comic-video-packet"))
        val fillPath = Path.of(options.requiredCliOption("voice-timeline-fill"))
        val packet = ComicVideoVoiceTimelineFillService().fill(
            packet = readCliJson<ComicVideoPacket>(packetPath),
            fill = readCliJson<ComicVideoVoiceTimelineFill>(fillPath),
        )
        options["out"]?.let { writeCliJson(Path.of(it), packet) }
        println("packetId=${packet.packetId}")
        println("packetStatus=${packet.packetStatus}")
        println("sourcePacketFile=$packetPath")
        println("voiceTimelineFillFile=$fillPath")
        println("panels=${packet.panelTracks.size}")
        println("voiceCast=${packet.voiceCast.size}")
        println("audioCues=${packet.audioCues.size}")
        println("subtitleCues=${packet.subtitleCues.size}")
        println("totalDurationMillis=${packet.timing.totalDurationMillis}")
        println("downstreamBoundary=${packet.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${packet.nextAllowedDownstreamStep}")
        println("validationNeeded=true")
        return 0
    }

    fun createComicVideoVoiceTimelineFillTemplate(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("comic-video-packet"))
        val out = Path.of(options.requiredCliOption("out"))
        val fill = ComicVideoVoiceTimelineFillService().createTemplate(
            packet = readCliJson<ComicVideoPacket>(packetPath),
            fillId = options["fill"] ?: "",
            voiceId = options["voice-id"] ?: "voice-manual-001",
            characterId = options["character-id"] ?: "char-manual-001",
            displayName = options["display-name"] ?: "Manual Voice",
            voiceLabel = options["voice-label"] ?: "local-manual-voice",
        )
        writeCliJson(out, fill)
        println("wrote=$out")
        println("fillId=${fill.fillId}")
        println("sourceComicVideoPacketId=${fill.sourceComicVideoPacketId}")
        println("fillStatus=${fill.fillStatus}")
        println("voiceCast=${fill.voiceCast.size}")
        println("audioCues=${fill.audioCues.size}")
        println("subtitleCues=${fill.subtitleCues.size}")
        println("totalDurationMillis=${fill.timing.totalDurationMillis}")
        println("editRequired=true")
        println("mediaGenerated=false")
        return 0
    }

    fun validateComicVideoPacket(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("comic-video-packet"))
        val report = ComicVideoPacketValidationService().validate(
            packet = readCliJson<ComicVideoPacket>(packetPath),
            packetFile = packetPath.toString(),
            validationId = options["validation"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("validationId=${report.validationId}")
        println("packetId=${report.packetId}")
        println("packetStatus=${report.packetStatus}")
        println("reviewableStoryboardPacketFile=${report.reviewableStoryboardPacketFile}")
        println("imageFacingDeliveryProofFile=${report.imageFacingDeliveryProofFile}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("panels=${report.evidence.uniquePanelIdCount}/${report.evidence.panelTrackCount}")
        println("voiceCast=${report.evidence.uniqueVoiceIdCount}/${report.evidence.voiceCastCount}")
        println("audioCues=${report.evidence.audioCueCount}")
        println("subtitleCues=${report.evidence.subtitleCueCount}")
        println("crossReferenceMismatches=${report.evidence.crossReferenceMismatchCount}")
        println("generatedMediaFields=${report.evidence.generatedMediaFieldCount}")
        println("remoteOrProviderFields=${report.evidence.remoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun createComicVideoRenderHandoffManifest(options: Map<String, String>): Int {
        val validationPath = Path.of(options.requiredCliOption("comic-video-packet-validation"))
        val manifest = ComicVideoRenderHandoffManifestService().create(
            validationReport = readCliJson<ComicVideoPacketValidationReport>(validationPath),
            validationReportFile = validationPath.toString(),
            manifestId = options["manifest"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), manifest) }
        println("verdict=${manifest.verdict}")
        println("manifestId=${manifest.manifestId}")
        println("manifestStatus=${manifest.manifestStatus}")
        println("validationId=${manifest.validationId}")
        println("packetId=${manifest.packetId}")
        println("acceptedScriptPacketId=${manifest.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${manifest.scriptPacketDraftFile}")
        println("scriptPacketSha256=${manifest.scriptPacketDraftContentSha256}")
        println("panelTimeline=${manifest.panelTimeline.size}")
        println("voiceTimeline=${manifest.voiceTimeline.size}")
        println("subtitleTimeline=${manifest.subtitleTimeline.size}")
        println("totalDurationMillis=${manifest.renderSettings.totalDurationMillis}")
        println("downstreamBoundary=${manifest.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${manifest.nextAllowedDownstreamStep}")
        println("blockedReason=${manifest.blockedReason}")
        println("findings=${manifest.findingCount}")
        manifest.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (manifest.verdict == "FAIL") 1 else 0
    }

    fun createComicVideoProductionPackageDraft(options: Map<String, String>): Int {
        val proofPath = Path.of(options.requiredCliOption("image-facing-delivery-proof"))
        val fillPath = Path.of(options.requiredCliOption("voice-timeline-fill"))
        val panelDurationMillis = options["panel-duration-ms"]?.toIntOrNull()
            ?: options["panel-duration-ms"]?.let { error("--panel-duration-ms must be an integer.") }
            ?: 3000
        val draft = ComicVideoProductionPackageDraftService().create(
            imageFacingDeliveryProof = readCliJson<ImageFacingDeliveryProof>(proofPath),
            voiceTimelineFill = readCliJson<ComicVideoVoiceTimelineFill>(fillPath),
            imageFacingDeliveryProofFile = proofPath.toString(),
            voiceTimelineFillFile = fillPath.toString(),
            packageId = options["package"] ?: "",
            packetId = options["packet"] ?: "",
            panelDurationMillis = panelDurationMillis,
        )
        options["out"]?.let { writeCliJson(Path.of(it), draft) }
        println("verdict=${draft.verdict}")
        println("packageId=${draft.packageId}")
        println("packageStatus=${draft.packageStatus}")
        println("generatedComicVideoPacketId=${draft.generatedComicVideoPacketId}")
        println("generatedPacketValidationId=${draft.generatedPacketValidationId}")
        println("sourceImageFacingDeliveryProofId=${draft.sourceImageFacingDeliveryProofId}")
        println("acceptedScriptPacketId=${draft.acceptedScriptPacketId}")
        println("storyboardRows=${draft.storyboardProductionTable.size}")
        println("shotPrompts=${draft.shotPrompts.size}")
        println("assetPromptCards=${draft.assetPromptCards.size}")
        println("voiceStyleCards=${draft.voiceStyleCards.size}")
        println("totalDurationMillis=${draft.evidence.totalDurationMillis}")
        println("downstreamBoundary=${draft.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${draft.nextAllowedDownstreamStep}")
        println("blockedReason=${draft.blockedReason}")
        println("findings=${draft.findingCount}")
        draft.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (draft.verdict == "FAIL") 1 else 0
    }

    fun validateComicVideoProductionPackage(options: Map<String, String>): Int {
        val draftPath = Path.of(options.requiredCliOption("production-package-draft"))
        val report = ComicVideoProductionPackageValidationService().validate(
            draft = readCliJson<ComicVideoProductionPackageDraft>(draftPath),
            draftFile = draftPath.toString(),
            validationId = options["validation"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("validationId=${report.validationId}")
        println("packageId=${report.packageId}")
        println("packageStatus=${report.packageStatus}")
        println("sourceImageFacingDeliveryProofId=${report.sourceImageFacingDeliveryProofId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("storyboardRows=${report.evidence.storyboardRowCount}")
        println("shotPrompts=${report.evidence.shotPromptCount}")
        println("assetPromptCards=${report.evidence.assetCardCount}")
        println("voiceStyleCards=${report.evidence.voiceStyleCardCount}")
        println("crossReferenceMismatches=${report.evidence.crossReferenceMismatchCount}")
        println("promptSafetyFindings=${report.evidence.promptSafetyFindingCount}")
        println("remoteOrProviderReferences=${report.evidence.remoteOrProviderReferenceCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("authorOnlyLeaks=${report.evidence.authorOnlyLeakCount}")
        println("totalDurationMillis=${report.evidence.totalDurationMillis}")
        println("downstreamBoundary=${report.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun renderComicVideoLocal(options: Map<String, String>): Int {
        val manifestPath = Path.of(options.requiredCliOption("render-handoff-manifest"))
        val outputVideoPath = Path.of(options.requiredCliOption("out-video"))
        val defaultWorkDir = outputVideoPath
            .toAbsolutePath()
            .normalize()
            .parent
            ?.resolve(outputVideoPath.fileName.toString().removeSuffix(".mp4") + "_work")
            ?: Path.of("out", "comic_video_local_render_work")
        val report = ComicVideoLocalRendererService().render(
            manifest = readCliJson<ComicVideoRenderHandoffManifest>(manifestPath),
            manifestFile = manifestPath.toString(),
            outputVideoPath = outputVideoPath,
            workDir = Path.of(options["work-dir"] ?: defaultWorkDir.toString()),
            voiceName = options["voice-name"] ?: "Microsoft Hanhan Desktop",
            dryRun = options["dry-run"] == "true",
            renderId = options["render"] ?: "",
        )
        options["report-out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("renderId=${report.renderId}")
        println("renderStatus=${report.renderStatus}")
        println("manifestId=${report.manifestId}")
        println("packetId=${report.packetId}")
        println("outputVideoPath=${report.outputVideoPath}")
        println("workDir=${report.workDir}")
        println("dryRun=${report.dryRun}")
        println("mediaGenerated=${report.mediaGenerated}")
        println("localRendererBoundary=${report.localRendererBoundary}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun buildComicVideoLocalPreview(options: Map<String, String>): Int {
        val proofPath = Path.of(options.requiredCliOption("image-facing-delivery-proof"))
        val fillPath = Path.of(options.requiredCliOption("voice-timeline-fill"))
        val outputDir = Path.of(options.requiredCliOption("output-dir")).toAbsolutePath().normalize()
        val buildId = options["build"] ?: "comic-video-local-preview-build"
        val packetId = options["packet"] ?: "$buildId-packet"
        val fillTemplateId = options["fill-template"] ?: "$buildId-voice-timeline-fill-template"
        val validationId = options["validation"] ?: "$buildId-validation"
        val manifestId = options["manifest"] ?: "$buildId-render-handoff-manifest"
        val renderId = options["render"] ?: "$buildId-local-render"
        val panelDurationMillis = options["panel-duration-ms"]?.toIntOrNull()
            ?: options["panel-duration-ms"]?.let { error("--panel-duration-ms must be an integer.") }
            ?: 3000
        val packetPath = outputDir.resolve("comic_video_packet.json")
        val fillTemplatePath = outputDir.resolve("voice_timeline_fill_template.json")
        val filledPacketPath = outputDir.resolve("comic_video_packet_filled.json")
        val validationPath = outputDir.resolve("comic_video_packet_validation.json")
        val manifestPath = outputDir.resolve("comic_video_render_handoff_manifest.json")
        val renderReportPath = outputDir.resolve("comic_video_local_render_report.json")
        val buildReportPath = Path.of(
            options["report-out"] ?: outputDir.resolve("comic_video_local_preview_build_report.json").toString(),
        )
        val outputVideoPath = Path.of(
            options["out-video"] ?: outputDir.resolve("comic_video_local_preview.mp4").toString(),
        )
        val workDir = Path.of(options["work-dir"] ?: outputDir.resolve("local_render_work").toString())
        val dryRun = options["dry-run"] == "true"
        val findings = mutableListOf<ComicVideoLocalPreviewBuildFinding>()
        var proofVerdict = ""
        var packetPanelCount = 0
        var voiceCastCount = 0
        var audioCueCount = 0
        var subtitleCueCount = 0
        var packetValidationVerdict = ""
        var manifestVerdict = ""
        var localRenderVerdict = ""
        var totalDurationMillis = 0
        var renderReport: ComicVideoLocalRenderReport? = null

        val report = try {
            Files.createDirectories(outputDir)
            val proof = readCliJson<ImageFacingDeliveryProof>(proofPath)
            proofVerdict = proof.verdict
            val packet = ComicVideoPacketService().create(
                imageFacingDeliveryProof = proof,
                imageFacingDeliveryProofFile = proofPath.toString(),
                packetId = packetId,
                panelDurationMillis = panelDurationMillis,
            )
            packetPanelCount = packet.panelTracks.size
            writeCliJson(packetPath, packet)

            val fillTemplate = ComicVideoVoiceTimelineFillService().createTemplate(
                packet = packet,
                fillId = fillTemplateId,
                voiceId = options["voice-id"] ?: "voice-manual-001",
                characterId = options["character-id"] ?: "char-manual-001",
                displayName = options["display-name"] ?: "Manual Voice",
                voiceLabel = options["voice-label"] ?: "local-manual-voice",
            )
            writeCliJson(fillTemplatePath, fillTemplate)

            val filledPacket = ComicVideoVoiceTimelineFillService().fill(
                packet = packet,
                fill = readCliJson<ComicVideoVoiceTimelineFill>(fillPath),
            )
            voiceCastCount = filledPacket.voiceCast.size
            audioCueCount = filledPacket.audioCues.size
            subtitleCueCount = filledPacket.subtitleCues.size
            totalDurationMillis = filledPacket.timing.totalDurationMillis
            writeCliJson(filledPacketPath, filledPacket)

            val validation = ComicVideoPacketValidationService().validate(
                packet = filledPacket,
                packetFile = filledPacketPath.toString(),
                validationId = validationId,
            )
            packetValidationVerdict = validation.verdict
            writeCliJson(validationPath, validation)
            if (validation.verdict == "FAIL") {
                findings += buildFinding(
                    ruleId = "COMIC_VIDEO_PACKET_VALIDATION_FAILED",
                    category = "validation",
                    severity = "CRITICAL",
                    path = validationPath.toString(),
                    message = "End-to-end local preview build requires PASS comic video packet validation.",
                    evidence = validation.findings.map { it.ruleId },
                )
            }

            val manifest = ComicVideoRenderHandoffManifestService().create(
                validationReport = validation,
                validationReportFile = validationPath.toString(),
                manifestId = manifestId,
            )
            manifestVerdict = manifest.verdict
            writeCliJson(manifestPath, manifest)
            if (manifest.verdict == "FAIL") {
                findings += buildFinding(
                    ruleId = "COMIC_VIDEO_RENDER_HANDOFF_FAILED",
                    category = "render_handoff",
                    severity = "CRITICAL",
                    path = manifestPath.toString(),
                    message = "End-to-end local preview build requires PASS render handoff manifest.",
                    evidence = manifest.findings.map { it.ruleId },
                )
            }

            renderReport = ComicVideoLocalRendererService().render(
                manifest = manifest,
                manifestFile = manifestPath.toString(),
                outputVideoPath = outputVideoPath,
                workDir = workDir,
                voiceName = options["voice-name"] ?: "Microsoft Hanhan Desktop",
                dryRun = dryRun,
                renderId = renderId,
            )
            localRenderVerdict = renderReport?.verdict.orEmpty()
            writeCliJson(renderReportPath, renderReport)
            if (renderReport?.verdict == "FAIL") {
                findings += buildFinding(
                    ruleId = "COMIC_VIDEO_LOCAL_RENDER_FAILED",
                    category = "local_render",
                    severity = "CRITICAL",
                    path = renderReportPath.toString(),
                    message = "End-to-end local preview build requires PASS local render report.",
                    evidence = renderReport?.findings.orEmpty().map { it.ruleId },
                )
            }

            val verdict = if (validation.verdict == "PASS" && manifest.verdict == "PASS" && renderReport?.verdict == "PASS") {
                "PASS"
            } else {
                "FAIL"
            }
            comicVideoLocalPreviewBuildReport(
                verdict = verdict,
                buildStatus = when {
                    verdict == "FAIL" -> "FAIL"
                    dryRun -> "DRY_RUN"
                    else -> "local_preview_video_built"
                },
                buildId = buildId,
                proofPath = proofPath,
                fillPath = fillPath,
                outputDir = outputDir,
                packetPath = packetPath,
                fillTemplatePath = fillTemplatePath,
                filledPacketPath = filledPacketPath,
                validationPath = validationPath,
                manifestPath = manifestPath,
                renderReportPath = renderReportPath,
                outputVideoPath = outputVideoPath,
                packetId = packet.packetId,
                validationId = validation.validationId,
                manifestId = manifest.manifestId,
                renderId = renderReport?.renderId.orEmpty(),
                dryRun = dryRun,
                mediaGenerated = renderReport?.mediaGenerated == true,
                nextAllowedDownstreamStep = if (verdict == "PASS") renderReport?.nextAllowedDownstreamStep.orEmpty() else "",
                findings = findings,
                renderReport = renderReport,
                proofVerdict = proofVerdict,
                packetPanelCount = packetPanelCount,
                voiceCastCount = voiceCastCount,
                audioCueCount = audioCueCount,
                subtitleCueCount = subtitleCueCount,
                packetValidationVerdict = packetValidationVerdict,
                manifestVerdict = manifestVerdict,
                localRenderVerdict = localRenderVerdict,
                totalDurationMillis = totalDurationMillis,
            )
        } catch (exception: Exception) {
            findings += buildFinding(
                ruleId = "COMIC_VIDEO_LOCAL_PREVIEW_BUILD_EXCEPTION",
                category = "execution",
                severity = "CRITICAL",
                path = "$.execution",
                message = "End-to-end local preview build failed before completing the pipeline.",
                evidence = listOf(exception::class.java.simpleName, exception.message.orEmpty()),
            )
            comicVideoLocalPreviewBuildReport(
                verdict = "FAIL",
                buildStatus = "FAIL",
                buildId = buildId,
                proofPath = proofPath,
                fillPath = fillPath,
                outputDir = outputDir,
                packetPath = packetPath,
                fillTemplatePath = fillTemplatePath,
                filledPacketPath = filledPacketPath,
                validationPath = validationPath,
                manifestPath = manifestPath,
                renderReportPath = renderReportPath,
                outputVideoPath = outputVideoPath,
                packetId = packetId,
                validationId = validationId,
                manifestId = manifestId,
                renderId = renderId,
                dryRun = dryRun,
                mediaGenerated = false,
                nextAllowedDownstreamStep = "",
                findings = findings,
                renderReport = renderReport,
                proofVerdict = proofVerdict,
                packetPanelCount = packetPanelCount,
                voiceCastCount = voiceCastCount,
                audioCueCount = audioCueCount,
                subtitleCueCount = subtitleCueCount,
                packetValidationVerdict = packetValidationVerdict,
                manifestVerdict = manifestVerdict,
                localRenderVerdict = localRenderVerdict,
                totalDurationMillis = totalDurationMillis,
            )
        }

        writeCliJson(buildReportPath, report)
        printComicVideoLocalPreviewBuildReport(report)
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun buildSettingComicVideoLocalPreview(options: Map<String, String>): Int {
        val settingPackPath = Path.of(options.requiredCliOption("setting-pack"))
        val fillPath = Path.of(options.requiredCliOption("voice-timeline-fill"))
        val outputDir = Path.of(options.requiredCliOption("output-dir")).toAbsolutePath().normalize()
        val buildId = options["build"] ?: "setting-comic-video-local-preview-build"
        val settingOutputDir = outputDir.resolve("setting_image_facing_preview")
        val videoOutputDir = outputDir.resolve("comic_video_local_preview")
        val proofPath = settingOutputDir.resolve("image_facing_delivery_proof.json")
        val fill = readCliJson<ComicVideoVoiceTimelineFill>(fillPath)
        val packetId = options["packet"]
            ?: fill.sourceComicVideoPacketId.ifBlank { "$buildId-packet" }

        val proofOptions = mutableMapOf(
            "setting-pack" to settingPackPath.toString(),
            "output-dir" to settingOutputDir.toString(),
            "build" to (options["setting-proof-build"] ?: "$buildId-setting-image-facing-proof"),
            "script-packet" to (options["script-packet"] ?: "$buildId-script-packet-draft"),
            "storyboard-brief" to (options["storyboard-brief"] ?: "$buildId-storyboard-brief"),
            "storyboard-draft" to (options["storyboard-draft"] ?: "$buildId-storyboard-draft"),
            "storyboard-review" to (options["storyboard-review"] ?: "$buildId-storyboard-review"),
            "human-review-preflight" to (options["human-review-preflight"] ?: "$buildId-human-review-preflight"),
            "story-movement-audit" to (options["story-movement-audit"] ?: "$buildId-story-movement-audit"),
            "reviewable-storyboard" to (options["reviewable-storyboard"] ?: "$buildId-reviewable-storyboard"),
            "proof" to (options["proof"] ?: "$buildId-image-facing-proof"),
        )
        options["scene"]?.let { proofOptions["scene"] = it }

        println("settingImageFacingPreviewDir=$settingOutputDir")
        val proofExit = StoryboardPipelineCli.buildSettingImageFacingProofPreview(proofOptions)
        if (proofExit != 0) {
            println("settingComicVideoPreviewStatus=FAILED_AT_SETTING_IMAGE_FACING_PROOF")
            return proofExit
        }

        val videoOptions = mutableMapOf(
            "image-facing-delivery-proof" to proofPath.toString(),
            "voice-timeline-fill" to fillPath.toString(),
            "output-dir" to videoOutputDir.toString(),
            "out-video" to (options["out-video"] ?: outputDir.resolve("setting_comic_video_local_preview.mp4").toString()),
            "build" to (options["comic-video-build"] ?: "$buildId-comic-video"),
            "packet" to packetId,
            "validation" to (options["validation"] ?: "$buildId-comic-video-validation"),
            "manifest" to (options["manifest"] ?: "$buildId-render-handoff-manifest"),
            "render" to (options["render"] ?: "$buildId-local-render"),
        )
        listOf(
            "panel-duration-ms",
            "voice-name",
            "work-dir",
            "report-out",
            "voice-id",
            "character-id",
            "display-name",
            "voice-label",
        ).forEach { key ->
            options[key]?.let { videoOptions[key] = it }
        }
        if (options["dry-run"] == "true") {
            videoOptions["dry-run"] = "true"
        }

        println("comicVideoLocalPreviewDir=$videoOutputDir")
        val videoExit = buildComicVideoLocalPreview(videoOptions)
        println("settingComicVideoPreviewStatus=${if (videoExit == 0) "PASS" else "FAILED_AT_COMIC_VIDEO_LOCAL_PREVIEW"}")
        return videoExit
    }

    private fun comicVideoLocalPreviewBuildReport(
        verdict: String,
        buildStatus: String,
        buildId: String,
        proofPath: Path,
        fillPath: Path,
        outputDir: Path,
        packetPath: Path,
        fillTemplatePath: Path,
        filledPacketPath: Path,
        validationPath: Path,
        manifestPath: Path,
        renderReportPath: Path,
        outputVideoPath: Path,
        packetId: String,
        validationId: String,
        manifestId: String,
        renderId: String,
        dryRun: Boolean,
        mediaGenerated: Boolean,
        nextAllowedDownstreamStep: String,
        findings: List<ComicVideoLocalPreviewBuildFinding>,
        renderReport: ComicVideoLocalRenderReport?,
        proofVerdict: String,
        packetPanelCount: Int,
        voiceCastCount: Int,
        audioCueCount: Int,
        subtitleCueCount: Int,
        packetValidationVerdict: String,
        manifestVerdict: String,
        localRenderVerdict: String,
        totalDurationMillis: Int,
    ): ComicVideoLocalPreviewBuildReport =
        ComicVideoLocalPreviewBuildReport(
            verdict = verdict,
            buildId = buildId,
            buildStatus = buildStatus,
            imageFacingDeliveryProofFile = proofPath.toString(),
            voiceTimelineFillFile = fillPath.toString(),
            outputDir = outputDir.toString(),
            packetFile = packetPath.toString(),
            voiceTimelineFillTemplateFile = fillTemplatePath.toString(),
            filledPacketFile = filledPacketPath.toString(),
            packetValidationFile = validationPath.toString(),
            renderHandoffManifestFile = manifestPath.toString(),
            localRenderReportFile = renderReportPath.toString(),
            outputVideoPath = outputVideoPath.toString(),
            packetId = packetId,
            validationId = validationId,
            manifestId = manifestId,
            renderId = renderId,
            dryRun = dryRun,
            mediaGenerated = mediaGenerated,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
            blockedReason = if (verdict == "FAIL") {
                findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId }
            } else {
                ""
            },
            findingCount = findings.size,
            findings = findings,
            renderReport = renderReport,
            evidence = ComicVideoLocalPreviewBuildEvidence(
                proofVerdict = proofVerdict,
                packetPanelCount = packetPanelCount,
                voiceCastCount = voiceCastCount,
                audioCueCount = audioCueCount,
                subtitleCueCount = subtitleCueCount,
                packetValidationVerdict = packetValidationVerdict,
                manifestVerdict = manifestVerdict,
                localRenderVerdict = localRenderVerdict,
                totalDurationMillis = totalDurationMillis,
            ),
        )

    private fun buildFinding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): ComicVideoLocalPreviewBuildFinding =
        ComicVideoLocalPreviewBuildFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private fun printComicVideoLocalPreviewBuildReport(report: ComicVideoLocalPreviewBuildReport) {
        println("verdict=${report.verdict}")
        println("buildId=${report.buildId}")
        println("buildStatus=${report.buildStatus}")
        println("packetId=${report.packetId}")
        println("validationId=${report.validationId}")
        println("manifestId=${report.manifestId}")
        println("renderId=${report.renderId}")
        println("outputDir=${report.outputDir}")
        println("outputVideoPath=${report.outputVideoPath}")
        println("dryRun=${report.dryRun}")
        println("mediaGenerated=${report.mediaGenerated}")
        println("packetValidationVerdict=${report.evidence.packetValidationVerdict}")
        println("manifestVerdict=${report.evidence.manifestVerdict}")
        println("localRenderVerdict=${report.evidence.localRenderVerdict}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
    }
}