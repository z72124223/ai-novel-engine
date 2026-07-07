package com.localnovel.engine.service

import com.localnovel.engine.model.ComicVideoAssetPromptCard
import com.localnovel.engine.model.ComicVideoProductionPackageDraft
import com.localnovel.engine.model.ComicVideoProductionPackageValidationEvidence
import com.localnovel.engine.model.ComicVideoProductionPackageValidationFinding
import com.localnovel.engine.model.ComicVideoProductionPackageValidationReport
import com.localnovel.engine.model.ComicVideoShotPromptDraft
import com.localnovel.engine.model.ComicVideoStoryboardProductionRow
import com.localnovel.engine.model.ComicVideoVoiceStyleCard

class ComicVideoProductionPackageValidationService {
    private val requiredPackageStatus = "comic_video_production_package_candidate"
    private val requiredPackageBoundary = "comic_video_production_package_draft_only"
    private val requiredPackageNextStep = "comic_video_production_package_review"
    private val nextAllowedDownstreamStep = "comic_video_production_package_validated_for_review"
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
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
    private val forbiddenClaimPatterns = listOf(
        Regex("""(?i)\bprompt\s+submitted\b"""),
        Regex("""(?i)\bsubmitted\s+prompt\b"""),
        Regex("""(?i)\bimage\s+generated\b|\bgenerated\s+image\b"""),
        Regex("""(?i)\baudio\s+generated\b|\bgenerated\s+audio\b"""),
        Regex("""(?i)\bvideo\s+generated\b|\bgenerated\s+video\b"""),
        Regex("""(?i)\brendered\s+(video|audio|image|panel)\b"""),
        Regex("""(?i)\basset\s+approval\b|\basset\s+approved\b|\buser\s+approval\b|\buser\s+approved\b"""),
        Regex("""(?i)\bruntime\s+scene\b"""),
        Regex("\u63d0\u793a\u5df2\u63d0\u4ea4|\u5716\u7247\u5df2\u751f\u6210|\u5f71\u7247\u5df2\u751f\u6210|\u97f3\u8a0a\u5df2\u751f\u6210|\u7d20\u6750\u5df2\u6838\u51c6|\u88fd\u4f5c\u9396\u5b9a|\u6b63\u5178\u5df2\u5957\u7528"),
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

    fun validate(
        draft: ComicVideoProductionPackageDraft,
        draftFile: String = "",
        validationId: String = "",
    ): ComicVideoProductionPackageValidationReport {
        val findings = mutableListOf<ComicVideoProductionPackageValidationFinding>()
        validatePackageState(draft, findings)
        val sourceFieldCount = validateSourceFields(draft, draftFile, findings)
        val rowValidation = validateStoryboardRows(draft.storyboardProductionTable, findings)
        val assetCardIds = validateAssetCards(draft.assetPromptCards, rowValidation.panelIds, findings)
        val voiceById = validateVoiceCards(draft.voiceStyleCards, findings)
        val shotValidation = validateShotPrompts(draft.shotPrompts, rowValidation, assetCardIds, findings)
        val voiceMismatchCount = validateRowVoiceRefs(draft.storyboardProductionTable, voiceById, findings)
        val promptSafetyCounts = scanPromptSafeText(draft, findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") {
            findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId }
        } else {
            ""
        }
        val resolvedValidationId = validationId.ifBlank {
            draft.packageId.takeIf { it.isNotBlank() }?.let { "$it-validation" }.orEmpty()
        }

        return ComicVideoProductionPackageValidationReport(
            verdict = verdict,
            validationId = resolvedValidationId,
            productionPackageDraftFile = draftFile,
            packageId = draft.packageId,
            packageStatus = draft.packageStatus,
            sourceImageFacingDeliveryProofId = draft.sourceImageFacingDeliveryProofId,
            sourceReviewableStoryboardPacketId = draft.sourceReviewableStoryboardPacketId,
            acceptedScriptPacketId = draft.acceptedScriptPacketId,
            scriptPacketDraftFile = draft.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = draft.scriptPacketDraftContentSha256,
            sourceVerdicts = draft.sourceVerdicts,
            storyboardProductionTable = if (verdict == "PASS") draft.storyboardProductionTable else emptyList(),
            shotPromptPacketId = if (verdict == "PASS") draft.shotPromptPacketId else "",
            shotPrompts = if (verdict == "PASS") draft.shotPrompts else emptyList(),
            assetPromptCards = if (verdict == "PASS") draft.assetPromptCards else emptyList(),
            voiceStyleCards = if (verdict == "PASS") draft.voiceStyleCards else emptyList(),
            nextAllowedDownstreamStep = if (verdict == "PASS") nextAllowedDownstreamStep else "",
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = ComicVideoProductionPackageValidationEvidence(
                packageVerdict = draft.verdict,
                packageNextAllowedDownstreamStep = draft.nextAllowedDownstreamStep,
                sourceFieldCount = sourceFieldCount,
                storyboardRowCount = draft.storyboardProductionTable.size,
                uniqueShotIdCount = rowValidation.shotIds.size,
                shotPromptCount = draft.shotPrompts.size,
                assetCardCount = draft.assetPromptCards.size,
                voiceStyleCardCount = draft.voiceStyleCards.size,
                crossReferenceMismatchCount = shotValidation.crossReferenceMismatchCount + voiceMismatchCount,
                promptSafetyFindingCount = promptSafetyCounts.total,
                forbiddenClaimCount = promptSafetyCounts.forbiddenClaimCount,
                forbiddenStatusCount = promptSafetyCounts.forbiddenStatusCount,
                remoteOrProviderReferenceCount = promptSafetyCounts.remoteOrProviderCount,
                authorOnlyLeakCount = promptSafetyCounts.authorOnlyLeakCount,
                totalDurationMillis = draft.evidence.totalDurationMillis,
            ),
        )
    }

    private fun validatePackageState(
        draft: ComicVideoProductionPackageDraft,
        findings: MutableList<ComicVideoProductionPackageValidationFinding>,
    ) {
        if (draft.verdict.trim().uppercase() != "PASS") {
            findings += finding("PRODUCTION_PACKAGE_DRAFT_NOT_PASS", "upstream", "CRITICAL", "$.verdict", "Production package validation requires a PASS draft.", listOf(draft.verdict))
        }
        if (draft.packageStatus != requiredPackageStatus) {
            findings += finding("PRODUCTION_PACKAGE_STATUS_MISMATCH", "package_boundary", "CRITICAL", "$.packageStatus", "Production package must remain candidate status.", listOf(draft.packageStatus))
        }
        if (draft.downstreamBoundary != requiredPackageBoundary) {
            findings += finding("PRODUCTION_PACKAGE_BOUNDARY_MISMATCH", "package_boundary", "CRITICAL", "$.downstreamBoundary", "Production package must preserve draft-only boundary.", listOf(draft.downstreamBoundary))
        }
        if (draft.nextAllowedDownstreamStep != requiredPackageNextStep) {
            findings += finding("PRODUCTION_PACKAGE_NEXT_STEP_MISMATCH", "package_boundary", "CRITICAL", "$.nextAllowedDownstreamStep", "Production package validation must start from comic_video_production_package_review.", listOf(draft.nextAllowedDownstreamStep))
        }
        listOf("draft_only", "source_bound", "external_handoff_absent", "media_execution_absent", "review_required").forEach { required ->
            if (required !in draft.boundaryChecklist) {
                findings += finding("PRODUCTION_PACKAGE_BOUNDARY_CHECKLIST_MISSING", "package_boundary", "MAJOR", "$.boundaryChecklist", "Production package boundary checklist is missing $required.", listOf(required))
            }
        }
        if (draft.findingCount != draft.findings.size) {
            findings += finding("PRODUCTION_PACKAGE_FINDING_COUNT_MISMATCH", "package_structure", "MAJOR", "$.findingCount", "Draft findingCount must match findings size.", listOf("findingCount=${draft.findingCount}", "findings=${draft.findings.size}"))
        }
        if (draft.findingCount != 0 || draft.findings.isNotEmpty()) {
            findings += finding("PRODUCTION_PACKAGE_DRAFT_FINDINGS_PRESENT", "upstream", "MAJOR", "$.findings", "Production package validation expects a clean draft with no unresolved findings.", listOf(draft.findingCount.toString()))
        }
    }

    private fun validateSourceFields(
        draft: ComicVideoProductionPackageDraft,
        draftFile: String,
        findings: MutableList<ComicVideoProductionPackageValidationFinding>,
    ): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) {
                findings += finding(ruleId, "source_linkage", "CRITICAL", path, "Production package validation requires nonblank $label.")
            } else {
                count += 1
            }
        }
        requireNonBlank(draftFile, "$.inputs.productionPackageDraftFile", "PRODUCTION_PACKAGE_DRAFT_FILE_MISSING", "production package draft file")
        requireNonBlank(draft.packageId, "$.packageId", "PRODUCTION_PACKAGE_ID_MISSING", "package id")
        requireNonBlank(draft.imageFacingDeliveryProofFile, "$.imageFacingDeliveryProofFile", "IMAGE_FACING_DELIVERY_PROOF_FILE_MISSING", "image-facing delivery proof file")
        requireNonBlank(draft.voiceTimelineFillFile, "$.voiceTimelineFillFile", "VOICE_TIMELINE_FILL_FILE_MISSING", "voice timeline fill file")
        requireNonBlank(draft.generatedComicVideoPacketId, "$.generatedComicVideoPacketId", "GENERATED_PACKET_ID_MISSING", "generated comic video packet id")
        requireNonBlank(draft.generatedPacketValidationId, "$.generatedPacketValidationId", "GENERATED_PACKET_VALIDATION_ID_MISSING", "generated packet validation id")
        requireNonBlank(draft.sourceImageFacingDeliveryProofId, "$.sourceImageFacingDeliveryProofId", "SOURCE_IMAGE_FACING_PROOF_ID_MISSING", "source image-facing delivery proof id")
        requireNonBlank(draft.sourceReviewableStoryboardPacketId, "$.sourceReviewableStoryboardPacketId", "SOURCE_REVIEWABLE_STORYBOARD_ID_MISSING", "source reviewable storyboard packet id")
        requireNonBlank(draft.acceptedScriptPacketId, "$.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(draft.scriptPacketDraftFile, "$.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(draft.scriptPacketDraftContentSha256, "$.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        if (draft.scriptPacketDraftContentSha256.isNotBlank() && !sha256Regex.matches(draft.scriptPacketDraftContentSha256.trim())) {
            findings += finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.scriptPacketDraftContentSha256", "Production package validation requires lowercase 64-character SHA-256.", listOf(draft.scriptPacketDraftContentSha256))
        }
        if (draft.sourceVerdicts.reviewableStoryboardPacketVerdict.trim().uppercase() != "PASS") {
            findings += finding("SOURCE_REVIEWABLE_STORYBOARD_NOT_PASS", "source_linkage", "CRITICAL", "$.sourceVerdicts.reviewableStoryboardPacketVerdict", "Production package validation requires PASS reviewable storyboard source verdict.", listOf(draft.sourceVerdicts.reviewableStoryboardPacketVerdict))
        }
        if (draft.sourceVerdicts.imageFacingDeliveryProofVerdict.trim().uppercase() != "PASS") {
            findings += finding("SOURCE_IMAGE_FACING_PROOF_NOT_PASS", "source_linkage", "CRITICAL", "$.sourceVerdicts.imageFacingDeliveryProofVerdict", "Production package validation requires PASS image-facing proof verdict.", listOf(draft.sourceVerdicts.imageFacingDeliveryProofVerdict))
        }
        if (draft.sourceVerdicts.imageFacingDeliveryProofNextAllowedDownstreamStep != "image_facing_delivery_proof_ready") {
            findings += finding("SOURCE_IMAGE_FACING_PROOF_NEXT_STEP_MISMATCH", "source_linkage", "CRITICAL", "$.sourceVerdicts.imageFacingDeliveryProofNextAllowedDownstreamStep", "Production package validation requires image_facing_delivery_proof_ready source permission.", listOf(draft.sourceVerdicts.imageFacingDeliveryProofNextAllowedDownstreamStep))
        }
        return count
    }

    private fun validateStoryboardRows(
        rows: List<ComicVideoStoryboardProductionRow>,
        findings: MutableList<ComicVideoProductionPackageValidationFinding>,
    ): RowValidation {
        if (rows.isEmpty()) {
            findings += finding("STORYBOARD_ROWS_MISSING", "package_structure", "CRITICAL", "$.storyboardProductionTable", "Production package validation requires storyboard rows.")
            return RowValidation()
        }
        val shotIds = rows.map { it.shotId.trim() }.filter { it.isNotBlank() }
        val panelIds = rows.map { it.panelId.trim() }.filter { it.isNotBlank() }
        duplicateValues(shotIds).forEach { duplicate ->
            findings += finding("STORYBOARD_SHOT_IDS_NOT_UNIQUE", "package_structure", "CRITICAL", "$.storyboardProductionTable", "Storyboard row shot ids must be unique.", listOf(duplicate))
        }
        duplicateValues(panelIds).forEach { duplicate ->
            findings += finding("STORYBOARD_PANEL_IDS_NOT_UNIQUE", "package_structure", "CRITICAL", "$.storyboardProductionTable", "Storyboard row panel ids must be unique.", listOf(duplicate))
        }
        rows.forEachIndexed { index, row ->
            val path = "$.storyboardProductionTable[$index]"
            requireField(row.shotId, "$path.shotId", "STORYBOARD_SHOT_ID_MISSING", "shot id", findings)
            requireField(row.panelId, "$path.panelId", "STORYBOARD_PANEL_ID_MISSING", "panel id", findings)
            requireField(row.sourceFrameId, "$path.sourceFrameId", "STORYBOARD_SOURCE_FRAME_ID_MISSING", "source frame id", findings)
            requireField(row.sceneReference, "$path.sceneReference", "STORYBOARD_SCENE_REFERENCE_MISSING", "scene reference", findings)
            requireField(row.beatReference, "$path.beatReference", "STORYBOARD_BEAT_REFERENCE_MISSING", "beat reference", findings)
            requireField(row.visualGoal, "$path.visualGoal", "STORYBOARD_VISUAL_GOAL_MISSING", "visual goal", findings)
            requireField(row.camera, "$path.camera", "STORYBOARD_CAMERA_MISSING", "camera", findings)
            if (row.durationMillis <= 0) {
                findings += finding("STORYBOARD_DURATION_INVALID", "package_timing", "CRITICAL", "$path.durationMillis", "Storyboard row requires positive duration.", listOf(row.durationMillis.toString()))
            }
            if (row.continuityMustShow.none { it.isNotBlank() }) {
                findings += finding("STORYBOARD_MUST_SHOW_MISSING", "package_structure", "MAJOR", "$path.continuityMustShow", "Storyboard row requires must-show continuity.")
            }
            if (row.continuityMustNotShow.none { it.isNotBlank() }) {
                findings += finding("STORYBOARD_MUST_NOT_SHOW_MISSING", "package_structure", "MAJOR", "$path.continuityMustNotShow", "Storyboard row requires must-not-show continuity.")
            }
            if (row.onScreenAssetCardIds.none { it.isNotBlank() }) {
                findings += finding("STORYBOARD_ASSET_REFS_MISSING", "cross_reference", "MAJOR", "$path.onScreenAssetCardIds", "Storyboard row requires referenced asset card ids.")
            }
        }
        return RowValidation(
            shotIds = shotIds.toSet(),
            panelIds = panelIds.toSet(),
            rowByShotId = rows.filter { it.shotId.isNotBlank() }.associateBy { it.shotId },
            rowByPanelId = rows.filter { it.panelId.isNotBlank() }.associateBy { it.panelId },
        )
    }

    private fun validateShotPrompts(
        prompts: List<ComicVideoShotPromptDraft>,
        rowValidation: RowValidation,
        assetCardIds: Set<String>,
        findings: MutableList<ComicVideoProductionPackageValidationFinding>,
    ): ShotValidation {
        if (prompts.isEmpty()) {
            findings += finding("SHOT_PROMPTS_MISSING", "package_structure", "CRITICAL", "$.shotPrompts", "Production package validation requires shot prompt drafts.")
            return ShotValidation()
        }
        var mismatchCount = 0
        val promptShotIds = prompts.map { it.shotId.trim() }.filter { it.isNotBlank() }
        duplicateValues(promptShotIds).forEach { duplicate ->
            findings += finding("SHOT_PROMPT_IDS_NOT_UNIQUE", "package_structure", "CRITICAL", "$.shotPrompts", "Shot prompt ids must be unique.", listOf(duplicate))
        }
        prompts.forEachIndexed { index, prompt ->
            val path = "$.shotPrompts[$index]"
            requireField(prompt.shotId, "$path.shotId", "SHOT_PROMPT_SHOT_ID_MISSING", "shot id", findings)
            requireField(prompt.panelId, "$path.panelId", "SHOT_PROMPT_PANEL_ID_MISSING", "panel id", findings)
            requireField(prompt.sourceFrameId, "$path.sourceFrameId", "SHOT_PROMPT_SOURCE_FRAME_ID_MISSING", "source frame id", findings)
            requireField(prompt.positivePromptDraft, "$path.positivePromptDraft", "POSITIVE_PROMPT_DRAFT_MISSING", "positive prompt draft", findings)
            if (prompt.negativePromptDraft.isBlank() && prompt.forbiddenContent.isEmpty()) {
                findings += finding("NEGATIVE_PROMPT_OR_FORBIDDEN_CONTENT_MISSING", "prompt_safety", "CRITICAL", path, "Shot prompt requires negative prompt text or forbidden content entries.")
            }
            if (prompt.promptBoundary != "draft_prompt_not_submitted") {
                findings += finding("SHOT_PROMPT_BOUNDARY_MISMATCH", "prompt_safety", "CRITICAL", "$path.promptBoundary", "Shot prompt must remain draft_prompt_not_submitted.", listOf(prompt.promptBoundary))
            }
            if (prompt.reviewStatus != "draft_review_required") {
                findings += finding("SHOT_PROMPT_REVIEW_STATUS_MISMATCH", "prompt_safety", "CRITICAL", "$path.reviewStatus", "Shot prompt must remain draft_review_required.", listOf(prompt.reviewStatus))
            }
            if (prompt.durationMillis <= 0) {
                findings += finding("SHOT_PROMPT_DURATION_INVALID", "package_timing", "CRITICAL", "$path.durationMillis", "Shot prompt requires positive duration.", listOf(prompt.durationMillis.toString()))
            }
            val row = rowValidation.rowByShotId[prompt.shotId]
            if (prompt.shotId.isNotBlank() && row == null) {
                mismatchCount += 1
                findings += finding("SHOT_PROMPT_ROW_REF_MISSING", "cross_reference", "CRITICAL", "$path.shotId", "Shot prompt shotId must match a storyboard row.", listOf(prompt.shotId))
            }
            if (row != null && prompt.panelId != row.panelId) {
                mismatchCount += 1
                findings += finding("SHOT_PROMPT_PANEL_MISMATCH", "cross_reference", "CRITICAL", "$path.panelId", "Shot prompt panelId must match the storyboard row panelId.", listOf("prompt=${prompt.panelId}", "row=${row.panelId}"))
            }
            prompt.referenceAssetCardIds.filter { it.isNotBlank() }.forEach { ref ->
                if (ref !in assetCardIds) {
                    mismatchCount += 1
                    findings += finding("SHOT_PROMPT_ASSET_CARD_REF_MISSING", "cross_reference", "CRITICAL", "$path.referenceAssetCardIds", "Shot prompt referenceAssetCardIds must reference existing asset prompt cards.", listOf(ref))
                }
            }
        }
        rowValidation.shotIds.filter { it !in promptShotIds.toSet() }.forEach { missing ->
            mismatchCount += 1
            findings += finding("STORYBOARD_ROW_WITHOUT_SHOT_PROMPT", "cross_reference", "CRITICAL", "$.storyboardProductionTable", "Every storyboard row requires a matching shot prompt.", listOf(missing))
        }
        return ShotValidation(crossReferenceMismatchCount = mismatchCount)
    }

    private fun validateAssetCards(
        cards: List<ComicVideoAssetPromptCard>,
        panelIds: Set<String>,
        findings: MutableList<ComicVideoProductionPackageValidationFinding>,
    ): Set<String> {
        if (cards.isEmpty()) {
            findings += finding("ASSET_PROMPT_CARDS_MISSING", "package_structure", "CRITICAL", "$.assetPromptCards", "Production package validation requires asset prompt cards.")
            return emptySet()
        }
        val ids = cards.map { it.assetCardId.trim() }.filter { it.isNotBlank() }
        duplicateValues(ids).forEach { duplicate ->
            findings += finding("ASSET_CARD_IDS_NOT_UNIQUE", "package_structure", "CRITICAL", "$.assetPromptCards", "Asset prompt card ids must be unique.", listOf(duplicate))
        }
        cards.forEachIndexed { index, card ->
            val path = "$.assetPromptCards[$index]"
            requireField(card.assetCardId, "$path.assetCardId", "ASSET_CARD_ID_MISSING", "asset card id", findings)
            requireField(card.sourceAssetRef, "$path.sourceAssetRef", "ASSET_SOURCE_REF_MISSING", "source asset ref", findings)
            requireField(card.assetType, "$path.assetType", "ASSET_TYPE_MISSING", "asset type", findings)
            requireField(card.publicDescription, "$path.publicDescription", "ASSET_PUBLIC_DESCRIPTION_MISSING", "public description", findings)
            if (card.sourcePanelIds.none { it.isNotBlank() }) {
                findings += finding("ASSET_SOURCE_PANEL_IDS_MISSING", "cross_reference", "MAJOR", "$path.sourcePanelIds", "Asset prompt card requires source panel ids.")
            }
            card.sourcePanelIds.filter { it.isNotBlank() }.forEach { panelId ->
                if (panelId !in panelIds) {
                    findings += finding("ASSET_SOURCE_PANEL_REF_MISSING", "cross_reference", "CRITICAL", "$path.sourcePanelIds", "Asset prompt card sourcePanelIds must reference storyboard row panel ids.", listOf(panelId))
                }
            }
            if (card.mustRemainStable.none { it.isNotBlank() }) {
                findings += finding("ASSET_STABILITY_NOTES_MISSING", "package_structure", "MAJOR", "$path.mustRemainStable", "Asset prompt card requires stability notes.")
            }
            if (card.forbiddenDrift.none { it.isNotBlank() }) {
                findings += finding("ASSET_FORBIDDEN_DRIFT_MISSING", "prompt_safety", "MAJOR", "$path.forbiddenDrift", "Asset prompt card requires forbidden drift notes.")
            }
            if (card.promptSafeDetails.none { it.isNotBlank() }) {
                findings += finding("ASSET_PROMPT_SAFE_DETAILS_MISSING", "prompt_safety", "MAJOR", "$path.promptSafeDetails", "Asset prompt card requires prompt-safe details.")
            }
            if (card.authorOnlyExcludedCount < 0) {
                findings += finding("ASSET_AUTHOR_ONLY_COUNT_INVALID", "prompt_safety", "CRITICAL", "$path.authorOnlyExcludedCount", "Author-only excluded count cannot be negative.", listOf(card.authorOnlyExcludedCount.toString()))
            }
            if (card.reviewStatus != "draft_review_required") {
                findings += finding("ASSET_REVIEW_STATUS_MISMATCH", "package_boundary", "CRITICAL", "$path.reviewStatus", "Asset prompt card must remain draft_review_required.", listOf(card.reviewStatus))
            }
        }
        return ids.toSet()
    }

    private fun validateVoiceCards(
        cards: List<ComicVideoVoiceStyleCard>,
        findings: MutableList<ComicVideoProductionPackageValidationFinding>,
    ): Map<String, ComicVideoVoiceStyleCard> {
        if (cards.isEmpty()) {
            findings += finding("VOICE_STYLE_CARDS_MISSING", "package_structure", "CRITICAL", "$.voiceStyleCards", "Production package validation requires voice style cards.")
            return emptyMap()
        }
        val ids = cards.map { it.voiceCardId.trim() }.filter { it.isNotBlank() }
        val voiceIds = cards.map { it.voiceId.trim() }.filter { it.isNotBlank() }
        duplicateValues(ids).forEach { duplicate ->
            findings += finding("VOICE_CARD_IDS_NOT_UNIQUE", "package_structure", "CRITICAL", "$.voiceStyleCards", "Voice style card ids must be unique.", listOf(duplicate))
        }
        duplicateValues(voiceIds).forEach { duplicate ->
            findings += finding("VOICE_IDS_NOT_UNIQUE", "package_structure", "CRITICAL", "$.voiceStyleCards", "Voice ids must be unique in voice style cards.", listOf(duplicate))
        }
        cards.forEachIndexed { index, card ->
            val path = "$.voiceStyleCards[$index]"
            requireField(card.voiceCardId, "$path.voiceCardId", "VOICE_CARD_ID_MISSING", "voice card id", findings)
            requireField(card.voiceId, "$path.voiceId", "VOICE_ID_MISSING", "voice id", findings)
            requireField(card.characterId, "$path.characterId", "VOICE_CHARACTER_ID_MISSING", "character id", findings)
            requireField(card.displayName, "$path.displayName", "VOICE_DISPLAY_NAME_MISSING", "display name", findings)
            requireField(card.voiceLabel, "$path.voiceLabel", "VOICE_LABEL_MISSING", "voice label", findings)
            if (card.voiceBoundary != "voice_style_card_plan_only") {
                findings += finding("VOICE_CARD_BOUNDARY_MISMATCH", "package_boundary", "CRITICAL", "$path.voiceBoundary", "Voice style card must remain plan-only.", listOf(card.voiceBoundary))
            }
            if (card.sourceCueIds.none { it.isNotBlank() }) {
                findings += finding("VOICE_SOURCE_CUE_IDS_MISSING", "cross_reference", "MAJOR", "$path.sourceCueIds", "Voice style card requires source cue ids.")
            }
            if (card.promptSafeNotes.none { it.isNotBlank() }) {
                findings += finding("VOICE_PROMPT_SAFE_NOTES_MISSING", "prompt_safety", "MAJOR", "$path.promptSafeNotes", "Voice style card requires prompt-safe notes.")
            }
            if (card.reviewStatus != "draft_review_required") {
                findings += finding("VOICE_REVIEW_STATUS_MISMATCH", "package_boundary", "CRITICAL", "$path.reviewStatus", "Voice style card must remain draft_review_required.", listOf(card.reviewStatus))
            }
        }
        return cards.filter { it.voiceId.isNotBlank() }.associateBy { it.voiceId }
    }

    private fun validateRowVoiceRefs(
        rows: List<ComicVideoStoryboardProductionRow>,
        voiceById: Map<String, ComicVideoVoiceStyleCard>,
        findings: MutableList<ComicVideoProductionPackageValidationFinding>,
    ): Int {
        var mismatchCount = 0
        rows.forEachIndexed { index, row ->
            if (row.voiceId.isNotBlank() && row.voiceId !in voiceById) {
                mismatchCount += 1
                findings += finding("STORYBOARD_ROW_VOICE_REF_MISSING", "cross_reference", "CRITICAL", "$.storyboardProductionTable[$index].voiceId", "Storyboard row voiceId must reference a voice style card.", listOf(row.voiceId))
            }
            val voice = voiceById[row.voiceId]
            if (voice != null && row.characterId.isNotBlank() && row.characterId != voice.characterId) {
                mismatchCount += 1
                findings += finding("STORYBOARD_ROW_CHARACTER_VOICE_MISMATCH", "cross_reference", "CRITICAL", "$.storyboardProductionTable[$index].characterId", "Storyboard row characterId must match the referenced voice style card.", listOf("row=${row.characterId}", "voice=${voice.characterId}"))
            }
        }
        return mismatchCount
    }

    private fun scanPromptSafeText(
        draft: ComicVideoProductionPackageDraft,
        findings: MutableList<ComicVideoProductionPackageValidationFinding>,
    ): PromptSafetyCounts {
        val fields = collectText(draft)
        var remoteOrProviderCount = 0
        var forbiddenStatusCount = 0
        var forbiddenClaimCount = 0
        var authorOnlyLeakCount = 0
        fields.filter { it.value.isNotBlank() }.forEach { field ->
            listOf(embeddedUrlRegex, providerOrApiRegex).forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    remoteOrProviderCount += 1
                    findings += finding("REMOTE_OR_PROVIDER_TEXT_FORBIDDEN", "prompt_safety", "CRITICAL", field.path, "Production package validation cannot allow remote URLs, provider job references, provider ids, or API result text.", listOf(field.value), "Keep the package local and unsubmitted.")
                }
            }
            forbiddenStatusPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    forbiddenStatusCount += 1
                    findings += finding("PACKAGE_STATUS_UPGRADE_TEXT_FORBIDDEN", "safety_boundary", "CRITICAL", field.path, "Production package validation cannot allow approved, registered, canon, final, locked, or production-ready wording.", listOf(field.value), "Keep wording draft-only and review-required.")
                }
            }
            forbiddenClaimPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    forbiddenClaimCount += 1
                    findings += finding("DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN", "safety_boundary", "CRITICAL", field.path, "Production package validation cannot allow submitted prompts, generated media, approval, canon, runtime scene, or production-lock claims.", listOf(field.value), "Keep the package as a local review artifact.")
                }
            }
            authorOnlyPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    authorOnlyLeakCount += 1
                    findings += finding("AUTHOR_ONLY_PROMPT_TEXT_FORBIDDEN", "prompt_safety", "CRITICAL", field.path, "Prompt-facing text cannot contain author-only, hidden truth, secret cause, hidden motive, or future reveal text.", listOf(field.value), "Use visible public scene details only.")
                }
            }
        }
        return PromptSafetyCounts(
            remoteOrProviderCount = remoteOrProviderCount,
            forbiddenStatusCount = forbiddenStatusCount,
            forbiddenClaimCount = forbiddenClaimCount,
            authorOnlyLeakCount = authorOnlyLeakCount,
        )
    }

    private fun collectText(draft: ComicVideoProductionPackageDraft): List<TextField> {
        val fields = mutableListOf<TextField>(
            TextField("$.packageStatus", draft.packageStatus),
            TextField("$.downstreamBoundary", draft.downstreamBoundary),
            TextField("$.nextAllowedDownstreamStep", draft.nextAllowedDownstreamStep),
        )
        draft.storyboardProductionTable.forEachIndexed { index, row ->
            val path = "$.storyboardProductionTable[$index]"
            fields += TextField("$path.visualGoal", row.visualGoal)
            fields += TextField("$path.camera", row.camera)
            fields += TextField("$path.spokenText", row.spokenText)
            fields += TextField("$path.subtitleText", row.subtitleText)
            row.continuityMustShow.forEachIndexed { itemIndex, value -> fields += TextField("$path.continuityMustShow[$itemIndex]", value) }
            row.continuityMustNotShow.forEachIndexed { itemIndex, value -> fields += TextField("$path.continuityMustNotShow[$itemIndex]", value) }
            row.productionNotes.forEachIndexed { itemIndex, value -> fields += TextField("$path.productionNotes[$itemIndex]", value) }
        }
        draft.shotPrompts.forEachIndexed { index, prompt ->
            val path = "$.shotPrompts[$index]"
            fields += TextField("$path.promptBoundary", prompt.promptBoundary)
            fields += TextField("$path.positivePromptDraft", prompt.positivePromptDraft)
            fields += TextField("$path.negativePromptDraft", prompt.negativePromptDraft)
            fields += TextField("$path.reviewStatus", prompt.reviewStatus)
            prompt.mustPreserve.forEachIndexed { itemIndex, value -> fields += TextField("$path.mustPreserve[$itemIndex]", value) }
            prompt.forbiddenContent.forEachIndexed { itemIndex, value -> fields += TextField("$path.forbiddenContent[$itemIndex]", value) }
        }
        draft.assetPromptCards.forEachIndexed { index, card ->
            val path = "$.assetPromptCards[$index]"
            fields += TextField("$path.assetType", card.assetType)
            fields += TextField("$path.publicDescription", card.publicDescription)
            fields += TextField("$path.reviewStatus", card.reviewStatus)
            card.mustRemainStable.forEachIndexed { itemIndex, value -> fields += TextField("$path.mustRemainStable[$itemIndex]", value) }
            card.allowedVariation.forEachIndexed { itemIndex, value -> fields += TextField("$path.allowedVariation[$itemIndex]", value) }
            card.forbiddenDrift.forEachIndexed { itemIndex, value -> fields += TextField("$path.forbiddenDrift[$itemIndex]", value) }
            card.promptSafeDetails.forEachIndexed { itemIndex, value -> fields += TextField("$path.promptSafeDetails[$itemIndex]", value) }
        }
        draft.voiceStyleCards.forEachIndexed { index, card ->
            val path = "$.voiceStyleCards[$index]"
            fields += TextField("$path.displayName", card.displayName)
            fields += TextField("$path.voiceLabel", card.voiceLabel)
            fields += TextField("$path.voiceBoundary", card.voiceBoundary)
            fields += TextField("$path.reviewStatus", card.reviewStatus)
            card.promptSafeNotes.forEachIndexed { itemIndex, value -> fields += TextField("$path.promptSafeNotes[$itemIndex]", value) }
        }
        return fields
    }

    private fun duplicateValues(values: List<String>): List<String> =
        values.groupingBy { it }.eachCount().filter { it.key.isNotBlank() && it.value > 1 }.keys.toList()

    private fun requireField(
        value: String,
        path: String,
        ruleId: String,
        label: String,
        findings: MutableList<ComicVideoProductionPackageValidationFinding>,
    ) {
        if (value.isBlank()) {
            findings += finding(ruleId, "package_structure", "CRITICAL", path, "Production package validation requires nonblank $label.")
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
    ): ComicVideoProductionPackageValidationFinding =
        ComicVideoProductionPackageValidationFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private data class RowValidation(
        val shotIds: Set<String> = emptySet(),
        val panelIds: Set<String> = emptySet(),
        val rowByShotId: Map<String, ComicVideoStoryboardProductionRow> = emptyMap(),
        val rowByPanelId: Map<String, ComicVideoStoryboardProductionRow> = emptyMap(),
    )

    private data class ShotValidation(val crossReferenceMismatchCount: Int = 0)

    private data class PromptSafetyCounts(
        val remoteOrProviderCount: Int = 0,
        val forbiddenStatusCount: Int = 0,
        val forbiddenClaimCount: Int = 0,
        val authorOnlyLeakCount: Int = 0,
    ) {
        val total: Int
            get() = remoteOrProviderCount + forbiddenStatusCount + forbiddenClaimCount + authorOnlyLeakCount
    }

    private data class TextField(val path: String, val value: String)
}
