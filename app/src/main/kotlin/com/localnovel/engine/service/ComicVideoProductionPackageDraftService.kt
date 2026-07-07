package com.localnovel.engine.service

import com.localnovel.engine.model.ComicVideoAssetPromptCard
import com.localnovel.engine.model.ComicVideoAudioCue
import com.localnovel.engine.model.ComicVideoPacket
import com.localnovel.engine.model.ComicVideoPacketValidationFinding
import com.localnovel.engine.model.ComicVideoProductionPackageDraft
import com.localnovel.engine.model.ComicVideoProductionPackageDraftEvidence
import com.localnovel.engine.model.ComicVideoProductionPackageDraftFinding
import com.localnovel.engine.model.ComicVideoShotPromptDraft
import com.localnovel.engine.model.ComicVideoStoryboardProductionRow
import com.localnovel.engine.model.ComicVideoSubtitleCue
import com.localnovel.engine.model.ComicVideoVoiceStyleCard
import com.localnovel.engine.model.ComicVideoVoiceTimelineFill
import com.localnovel.engine.model.ImageFacingDeliveryProof

class ComicVideoProductionPackageDraftService {
    private val defaultPanelDurationMillis = 3000
    private val packageStatus = "comic_video_production_package_candidate"
    private val nextAllowedDownstreamStep = "comic_video_production_package_review"
    private val embeddedUrlRegex = Regex("""(?i)\b[a-z][a-z0-9+.-]*://""")
    private val providerOrApiRegex = Regex("""(?i)\bprovider[-_\s]?job\b|\bprovider[-_\s]?id\b|\bapi[-_\s]?(response|result)\b|\bgeneration[-_\s]?api\b""")
    private val forbiddenStatusPatterns = listOf(
        Regex("""(?i)\bapproved\b"""),
        Regex("""(?i)\bregistered\b"""),
        Regex("""(?i)\bcanon\b"""),
        Regex("""(?i)\bfinal(?:[-_\s]?(asset|video|audio|image|media))?\b"""),
        Regex("""(?i)\bproduction[-_\s]?ready\b"""),
        Regex("""(?i)\bproduction[-_\s]?lock(?:ed)?\b"""),
        Regex("""(?i)\blocked\b"""),
        Regex("\u6838\u51c6|\u6279\u51c6|\u767b\u8a18\u5b8c\u6210|\u6b63\u5178|\u9396\u5b9a|\u6b63\u5f0f\u7d20\u6750"),
    )
    private val authorOnlyPatterns = listOf(
        Regex("""(?i)\bauthor[-_\s]?only\b"""),
        Regex("""(?i)\bdirector[-_\s]?only\b"""),
        Regex("""(?i)\bhidden\s+truth\b"""),
        Regex("""(?i)\bsecret\s+cause\b"""),
        Regex("""(?i)\bhidden\s+motive\b"""),
        Regex("""(?i)\bfuture\s+reveal\b"""),
        Regex("\u4f5c\u8005\u5c08\u7528|\u96b1\u85cf\u771f\u76f8|\u79d8\u5bc6\u539f\u56e0|\u96b1\u85cf\u52d5\u6a5f|\u672a\u4f86\u63ed\u9732"),
    )

    fun create(
        imageFacingDeliveryProof: ImageFacingDeliveryProof,
        voiceTimelineFill: ComicVideoVoiceTimelineFill,
        imageFacingDeliveryProofFile: String = "",
        voiceTimelineFillFile: String = "",
        packageId: String = "",
        packetId: String = "",
        panelDurationMillis: Int = defaultPanelDurationMillis,
    ): ComicVideoProductionPackageDraft {
        val findings = mutableListOf<ComicVideoProductionPackageDraftFinding>()
        validateInputs(imageFacingDeliveryProofFile, voiceTimelineFillFile, panelDurationMillis, findings)

        val resolvedPackageId = packageId.ifBlank {
            imageFacingDeliveryProof.proofId
                .takeIf { it.isNotBlank() }
                ?.let { "$it-production-package-draft" }
                .orEmpty()
        }
        val resolvedPacketId = packetId.ifBlank {
            voiceTimelineFill.sourceComicVideoPacketId.ifBlank {
                imageFacingDeliveryProof.proofId
                    .takeIf { it.isNotBlank() }
                    ?.let { "$it-comic-video-packet" }
                    .orEmpty()
            }
        }

        val packetResult = runCatching {
            ComicVideoPacketService().create(
                imageFacingDeliveryProof = imageFacingDeliveryProof,
                imageFacingDeliveryProofFile = imageFacingDeliveryProofFile,
                packetId = resolvedPacketId,
                panelDurationMillis = panelDurationMillis,
            )
        }
        val packet = packetResult.getOrNull()
        if (packet == null) {
            findings += finding(
                "SOURCE_PACKET_CREATION_FAILED",
                "source_linkage",
                "CRITICAL",
                "$.imageFacingDeliveryProof",
                packetResult.exceptionOrNull()?.message ?: "Could not create source comic video packet.",
                suggestedFix = "Use a PASS image-facing proof that allows image_facing_delivery_proof_ready.",
            )
            return failureDraft(
                packageId = resolvedPackageId,
                imageFacingDeliveryProofFile = imageFacingDeliveryProofFile,
                voiceTimelineFillFile = voiceTimelineFillFile,
                packetId = resolvedPacketId,
                proof = imageFacingDeliveryProof,
                findings = findings,
            )
        }

        val filledPacketResult = runCatching {
            ComicVideoVoiceTimelineFillService().fill(packet = packet, fill = voiceTimelineFill)
        }
        val filledPacket = filledPacketResult.getOrNull()
        if (filledPacket == null) {
            findings += finding(
                "VOICE_TIMELINE_FILL_FAILED",
                "source_linkage",
                "CRITICAL",
                "$.voiceTimelineFill",
                filledPacketResult.exceptionOrNull()?.message ?: "Could not apply voice timeline fill.",
                suggestedFix = "Use a voice timeline fill whose sourceComicVideoPacketId matches the generated packet id.",
            )
            return failureDraft(
                packageId = resolvedPackageId,
                imageFacingDeliveryProofFile = imageFacingDeliveryProofFile,
                voiceTimelineFillFile = voiceTimelineFillFile,
                packetId = packet.packetId,
                proof = imageFacingDeliveryProof,
                findings = findings,
            )
        }

        val validation = ComicVideoPacketValidationService().validate(
            packet = filledPacket,
            packetFile = "generated:${filledPacket.packetId}",
            validationId = "$resolvedPackageId-source-validation",
        )
        if (validation.verdict != "PASS") {
            findings += validation.findings.map(::fromValidationFinding)
            return failureDraft(
                packageId = resolvedPackageId,
                imageFacingDeliveryProofFile = imageFacingDeliveryProofFile,
                voiceTimelineFillFile = voiceTimelineFillFile,
                packetId = filledPacket.packetId,
                validationId = validation.validationId,
                validationVerdict = validation.verdict,
                proof = imageFacingDeliveryProof,
                findings = findings,
                packet = filledPacket,
            )
        }

        val storyboardRows = buildStoryboardRows(filledPacket)
        val assetCards = buildAssetCards(filledPacket)
        val voiceCards = buildVoiceStyleCards(filledPacket)
        val shotPrompts = buildShotPrompts(filledPacket, storyboardRows)
        if (assetCards.isEmpty()) {
            findings += finding(
                "ASSET_PROMPT_CARDS_MISSING",
                "production_package",
                "MAJOR",
                "$.assetPromptCards",
                "Production package draft requires at least one candidate asset prompt card.",
                suggestedFix = "Use source proof frame cards with assetCandidateRefs.",
            )
        }
        if (shotPrompts.isEmpty()) {
            findings += finding(
                "SHOT_PROMPTS_MISSING",
                "production_package",
                "MAJOR",
                "$.shotPrompts",
                "Production package draft requires at least one shot prompt draft.",
            )
        }
        val forbiddenTextFindingCount = scanPromptSafeText(
            storyboardRows = storyboardRows,
            shotPrompts = shotPrompts,
            assetCards = assetCards,
            voiceCards = voiceCards,
            findings = findings,
        )
        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") {
            findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId }
        } else {
            ""
        }

        return ComicVideoProductionPackageDraft(
            verdict = verdict,
            packageId = resolvedPackageId,
            packageStatus = if (verdict == "PASS") packageStatus else "FAIL",
            imageFacingDeliveryProofFile = imageFacingDeliveryProofFile,
            voiceTimelineFillFile = voiceTimelineFillFile,
            generatedComicVideoPacketId = filledPacket.packetId,
            generatedPacketValidationId = validation.validationId,
            sourceImageFacingDeliveryProofId = filledPacket.sourceImageFacingDeliveryProofId,
            sourceReviewableStoryboardPacketId = filledPacket.sourceReviewableStoryboardPacketId,
            acceptedScriptPacketId = filledPacket.acceptedScriptPacketId,
            scriptPacketDraftFile = filledPacket.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = filledPacket.scriptPacketDraftContentSha256,
            sourceVerdicts = filledPacket.sourceVerdicts,
            storyboardProductionTable = if (verdict == "PASS") storyboardRows else emptyList(),
            shotPromptPacketId = if (verdict == "PASS") "$resolvedPackageId-shot-prompt-packet" else "",
            shotPrompts = if (verdict == "PASS") shotPrompts else emptyList(),
            assetPromptCards = if (verdict == "PASS") assetCards else emptyList(),
            voiceStyleCards = if (verdict == "PASS") voiceCards else emptyList(),
            nextAllowedDownstreamStep = if (verdict == "PASS") nextAllowedDownstreamStep else "",
            blockedReason = blockedReason,
            boundaryChecklist = listOf(
                "draft_only",
                "source_bound",
                "external_handoff_absent",
                "media_execution_absent",
                "review_required",
            ),
            findingCount = findings.size,
            findings = findings,
            evidence = ComicVideoProductionPackageDraftEvidence(
                proofVerdict = imageFacingDeliveryProof.verdict,
                packetValidationVerdict = validation.verdict,
                packetPanelCount = filledPacket.panelTracks.size,
                storyboardRowCount = storyboardRows.size,
                shotPromptCount = shotPrompts.size,
                assetCardCount = assetCards.size,
                voiceStyleCardCount = voiceCards.size,
                audioCueCount = filledPacket.audioCues.size,
                subtitleCueCount = filledPacket.subtitleCues.size,
                totalDurationMillis = filledPacket.timing.totalDurationMillis,
                forbiddenTextFindingCount = forbiddenTextFindingCount,
            ),
        )
    }

    private fun validateInputs(
        imageFacingDeliveryProofFile: String,
        voiceTimelineFillFile: String,
        panelDurationMillis: Int,
        findings: MutableList<ComicVideoProductionPackageDraftFinding>,
    ) {
        if (imageFacingDeliveryProofFile.isBlank()) {
            findings += finding("IMAGE_FACING_PROOF_FILE_MISSING", "source_linkage", "CRITICAL", "$.inputs.imageFacingDeliveryProofFile", "Production package draft requires the source image-facing delivery proof file path.")
        }
        if (voiceTimelineFillFile.isBlank()) {
            findings += finding("VOICE_TIMELINE_FILL_FILE_MISSING", "source_linkage", "CRITICAL", "$.inputs.voiceTimelineFillFile", "Production package draft requires the source voice timeline fill file path.")
        }
        if (panelDurationMillis <= 0) {
            findings += finding("PANEL_DURATION_INVALID", "production_package", "CRITICAL", "$.inputs.panelDurationMillis", "Production package draft requires positive panel duration.", listOf(panelDurationMillis.toString()))
        }
    }

    private fun buildStoryboardRows(packet: ComicVideoPacket): List<ComicVideoStoryboardProductionRow> {
        val audioByPanel = packet.audioCues.groupBy { it.panelId }
        val subtitleByAudio = packet.subtitleCues.associateBy { it.audioCueId }
        return packet.panelTracks.mapIndexed { index, panel ->
            val audio = audioByPanel[panel.panelId]?.firstOrNull()
            val subtitle = audio?.let { subtitleByAudio[it.cueId] }
                ?: packet.subtitleCues.firstOrNull { it.panelId == panel.panelId }
            ComicVideoStoryboardProductionRow(
                shotId = shotId(index),
                panelId = panel.panelId,
                sourceFrameId = panel.sourceFrameId,
                sceneReference = panel.sceneReference,
                beatReference = panel.beatReference,
                durationMillis = panel.displayDurationMillis,
                visualGoal = panel.visualIntent,
                camera = panel.cameraIntent,
                onScreenAssetCardIds = panel.assetCandidateRefs.map(::assetCardId).distinct(),
                voiceId = audio?.voiceId.orEmpty(),
                characterId = audio?.characterId.orEmpty(),
                spokenText = audio?.text.orEmpty(),
                subtitleText = subtitle?.text.orEmpty(),
                continuityMustShow = panel.mustShow,
                continuityMustNotShow = panel.doNotShow,
                productionNotes = listOf(
                    "Source-bound draft row.",
                    "Review before downstream use.",
                ),
            )
        }
    }

    private fun buildShotPrompts(
        packet: ComicVideoPacket,
        rows: List<ComicVideoStoryboardProductionRow>,
    ): List<ComicVideoShotPromptDraft> =
        packet.panelTracks.mapIndexed { index, panel ->
            val row = rows[index]
            ComicVideoShotPromptDraft(
                shotId = row.shotId,
                panelId = panel.panelId,
                sourceFrameId = panel.sourceFrameId,
                positivePromptDraft = listOf(
                    "comic video shot",
                    "scene: ${panel.sceneReference}",
                    "beat: ${panel.beatReference}",
                    "visual goal: ${panel.visualIntent}",
                    "camera: ${panel.cameraIntent}",
                    "must show: ${panel.mustShow.joinToString("; ")}",
                    "continuity: ${panel.continuityAnchors.joinToString("; ")}",
                ).filter { it.substringAfter(": ", it).isNotBlank() }.joinToString(". "),
                negativePromptDraft = (
                    panel.doNotShow +
                        listOf(
                            "non-public source material",
                            "remote media references",
                            "generated-output claims",
                            "decision-state claims",
                        )
                    ).filter { it.isNotBlank() }.distinct().joinToString("; "),
                referenceAssetCardIds = panel.assetCandidateRefs.map(::assetCardId).distinct(),
                durationMillis = panel.displayDurationMillis,
                mustPreserve = (panel.mustShow + panel.continuityAnchors).filter { it.isNotBlank() }.distinct(),
                forbiddenContent = panel.doNotShow.filter { it.isNotBlank() }.distinct(),
            )
        }

    private fun buildAssetCards(packet: ComicVideoPacket): List<ComicVideoAssetPromptCard> {
        val refs = packet.panelTracks
            .flatMap { panel -> panel.assetCandidateRefs.map { ref -> ref to panel.panelId } }
            .groupBy({ it.first }, { it.second })
        return refs.entries
            .filter { it.key.isNotBlank() }
            .sortedBy { it.key }
            .map { (ref, panelIds) ->
                val relatedPanels = packet.panelTracks.filter { ref in it.assetCandidateRefs }
                ComicVideoAssetPromptCard(
                    assetCardId = assetCardId(ref),
                    sourceAssetRef = ref,
                    assetType = inferAssetType(ref),
                    sourcePanelIds = panelIds.distinct(),
                    publicDescription = "Candidate visual asset referenced by validated comic video panels: $ref.",
                    mustRemainStable = relatedPanels.flatMap { it.continuityAnchors + it.mustShow }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .take(12),
                    allowedVariation = listOf(
                        "Camera angle and framing may vary only when source continuity remains readable.",
                        "Lighting and scale may vary only when the asset identity remains clear.",
                    ),
                    forbiddenDrift = relatedPanels.flatMap { it.doNotShow }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .take(12),
                    promptSafeDetails = listOf(
                        "Use visible source-bound details only.",
                        "Keep this card as candidate planning material.",
                    ),
                )
            }
    }

    private fun buildVoiceStyleCards(packet: ComicVideoPacket): List<ComicVideoVoiceStyleCard> =
        packet.voiceCast.map { voice ->
            val cueIds = packet.audioCues
                .filter { it.voiceId == voice.voiceId }
                .map { it.cueId }
                .filter { it.isNotBlank() }
            ComicVideoVoiceStyleCard(
                voiceCardId = "voice-card-${sanitizeId(voice.voiceId)}",
                voiceId = voice.voiceId,
                characterId = voice.characterId,
                displayName = voice.displayName,
                voiceLabel = voice.voiceLabel,
                sourceCueIds = cueIds,
                promptSafeNotes = listOf(
                    "Voice style card is planning metadata only.",
                    "Review character voice continuity before downstream use.",
                ),
            )
        }

    private fun scanPromptSafeText(
        storyboardRows: List<ComicVideoStoryboardProductionRow>,
        shotPrompts: List<ComicVideoShotPromptDraft>,
        assetCards: List<ComicVideoAssetPromptCard>,
        voiceCards: List<ComicVideoVoiceStyleCard>,
        findings: MutableList<ComicVideoProductionPackageDraftFinding>,
    ): Int {
        val fields = mutableListOf<TextField>()
        storyboardRows.forEachIndexed { index, row ->
            fields += TextField("$.storyboardProductionTable[$index].visualGoal", row.visualGoal)
            fields += TextField("$.storyboardProductionTable[$index].camera", row.camera)
            row.productionNotes.forEachIndexed { noteIndex, note ->
                fields += TextField("$.storyboardProductionTable[$index].productionNotes[$noteIndex]", note)
            }
        }
        shotPrompts.forEachIndexed { index, shot ->
            fields += TextField("$.shotPrompts[$index].positivePromptDraft", shot.positivePromptDraft)
            fields += TextField("$.shotPrompts[$index].promptBoundary", shot.promptBoundary)
            fields += TextField("$.shotPrompts[$index].reviewStatus", shot.reviewStatus)
        }
        assetCards.forEachIndexed { index, card ->
            fields += TextField("$.assetPromptCards[$index].publicDescription", card.publicDescription)
            card.promptSafeDetails.forEachIndexed { noteIndex, note ->
                fields += TextField("$.assetPromptCards[$index].promptSafeDetails[$noteIndex]", note)
            }
            fields += TextField("$.assetPromptCards[$index].reviewStatus", card.reviewStatus)
        }
        voiceCards.forEachIndexed { index, card ->
            fields += TextField("$.voiceStyleCards[$index].voiceBoundary", card.voiceBoundary)
            card.promptSafeNotes.forEachIndexed { noteIndex, note ->
                fields += TextField("$.voiceStyleCards[$index].promptSafeNotes[$noteIndex]", note)
            }
            fields += TextField("$.voiceStyleCards[$index].reviewStatus", card.reviewStatus)
        }
        var count = 0
        fields.filter { it.value.isNotBlank() }.forEach { field ->
            listOf(embeddedUrlRegex, providerOrApiRegex).forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings += finding("REMOTE_OR_API_TEXT_FORBIDDEN", "prompt_safety", "CRITICAL", field.path, "Production package draft cannot carry remote URLs, provider job references, provider ids, or API result text.", listOf(field.value), "Keep the package local and unsubmitted.")
                }
            }
            forbiddenStatusPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings += finding("PACKAGE_STATUS_UPGRADE_TEXT_FORBIDDEN", "safety_boundary", "CRITICAL", field.path, "Production package draft cannot carry approved, registered, canon, final, locked, or production-ready wording.", listOf(field.value), "Keep wording draft-only and review-required.")
                }
            }
            authorOnlyPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings += finding("AUTHOR_ONLY_PROMPT_TEXT_FORBIDDEN", "prompt_safety", "CRITICAL", field.path, "Prompt-facing text cannot contain author-only, hidden truth, secret cause, hidden motive, or future reveal text.", listOf(field.value), "Use visible public scene details only.")
                }
            }
        }
        return count
    }

    private fun failureDraft(
        packageId: String,
        imageFacingDeliveryProofFile: String,
        voiceTimelineFillFile: String,
        packetId: String,
        proof: ImageFacingDeliveryProof,
        findings: List<ComicVideoProductionPackageDraftFinding>,
        validationId: String = "",
        validationVerdict: String = "",
        packet: ComicVideoPacket? = null,
    ): ComicVideoProductionPackageDraft =
        ComicVideoProductionPackageDraft(
            verdict = "FAIL",
            packageId = packageId,
            packageStatus = "FAIL",
            imageFacingDeliveryProofFile = imageFacingDeliveryProofFile,
            voiceTimelineFillFile = voiceTimelineFillFile,
            generatedComicVideoPacketId = packetId,
            generatedPacketValidationId = validationId,
            sourceImageFacingDeliveryProofId = proof.proofId,
            sourceReviewableStoryboardPacketId = proof.reviewableStoryboardPacketId,
            acceptedScriptPacketId = proof.acceptedScriptPacketId,
            scriptPacketDraftFile = proof.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = proof.scriptPacketDraftContentSha256,
            sourceVerdicts = packet?.sourceVerdicts ?: com.localnovel.engine.model.ComicVideoPacketSourceVerdicts(
                reviewableStoryboardPacketVerdict = proof.sourceVerdict,
                imageFacingDeliveryProofVerdict = proof.verdict,
                imageFacingDeliveryProofNextAllowedDownstreamStep = proof.nextAllowedDownstreamStep,
            ),
            blockedReason = findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId },
            boundaryChecklist = listOf(
                "draft_only",
                "source_bound",
                "external_handoff_absent",
                "media_execution_absent",
                "review_required",
            ),
            findingCount = findings.size,
            findings = findings,
            evidence = ComicVideoProductionPackageDraftEvidence(
                proofVerdict = proof.verdict,
                packetValidationVerdict = validationVerdict,
                packetPanelCount = packet?.panelTracks?.size ?: 0,
                audioCueCount = packet?.audioCues?.size ?: 0,
                subtitleCueCount = packet?.subtitleCues?.size ?: 0,
                totalDurationMillis = packet?.timing?.totalDurationMillis ?: 0,
            ),
        )

    private fun fromValidationFinding(source: ComicVideoPacketValidationFinding): ComicVideoProductionPackageDraftFinding =
        finding(
            ruleId = "SOURCE_PACKET_${source.ruleId}",
            category = source.category,
            severity = source.severity,
            path = source.path,
            message = source.message,
            evidence = source.evidence,
            suggestedFix = source.suggestedFix,
        )

    private fun shotId(index: Int): String =
        "shot-${(index + 1).toString().padStart(3, '0')}"

    private fun assetCardId(sourceAssetRef: String): String =
        "asset-card-${sanitizeId(sourceAssetRef)}"

    private fun sanitizeId(value: String): String =
        value.trim()
            .lowercase()
            .replace(Regex("""[^a-z0-9._-]+"""), "-")
            .trim('-')
            .ifBlank { "unnamed" }

    private fun inferAssetType(ref: String): String {
        val value = ref.lowercase()
        return when {
            listOf("scene", "gate", "archive", "entrance", "room", "street", "hall").any { it in value } -> "scene"
            listOf("letter", "lantern", "prop", "seal", "document", "weapon").any { it in value } -> "prop"
            else -> "character_or_visual_asset"
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
    ): ComicVideoProductionPackageDraftFinding =
        ComicVideoProductionPackageDraftFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private data class TextField(val path: String, val value: String)
}
