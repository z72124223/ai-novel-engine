package com.localnovel.engine.service

import com.localnovel.engine.model.LocalAssetPromptCardsDraft
import com.localnovel.engine.model.LocalRenderPackageDraft
import com.localnovel.engine.model.LocalStoryInputPack
import com.localnovel.engine.model.LocalStoryVideoChainReport
import com.localnovel.engine.model.LocalStoryVideoLocalRendererCompatibility
import com.localnovel.engine.model.LocalStoryVideoPreviewAssetPromptReference
import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffBoundaries
import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffDraft
import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffEvidence
import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffFinding
import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffReadinessCheck
import com.localnovel.engine.model.LocalStoryVideoPreviewPanelPlan
import com.localnovel.engine.model.LocalStoryVideoPreviewPlan
import com.localnovel.engine.model.LocalStoryVideoPreviewQaSummary
import com.localnovel.engine.model.LocalStoryVideoPreviewSceneOrder

class LocalStoryVideoPreviewHandoffDraftService {
    private val draftStatus = "LOCAL_STORY_VIDEO_PREVIEW_HANDOFF_DRAFT"
    private val requiredChainStatus = "LOCAL_STORY_VIDEO_CHAIN_VERIFIED"
    private val requiredRenderPackageStatus = "LOCAL_RENDER_PACKAGE_DRAFT"
    private val requiredAssetPromptCardsStatus = "LOCAL_ASSET_PROMPT_CARDS_DRAFT"
    private val downstreamBoundary = "local_story_video_preview_handoff_draft_only_not_media_generation"
    private val nextAllowedDownstreamStep = "local_preview_handoff_review"

    fun create(
        chainReport: LocalStoryVideoChainReport,
        renderPackage: LocalRenderPackageDraft,
        assetPromptCards: LocalAssetPromptCardsDraft,
        storyInputPack: LocalStoryInputPack,
        chainReportFile: String = "",
        renderPackageFile: String = "",
        assetPromptCardsFile: String = "",
        storyInputFile: String = "",
        handoffId: String = "",
    ): LocalStoryVideoPreviewHandoffDraft {
        val findings = mutableListOf<LocalStoryVideoPreviewHandoffFinding>()
        val readinessChecks = buildReadinessChecks(
            chainReport = chainReport,
            renderPackage = renderPackage,
            assetPromptCards = assetPromptCards,
            storyInputPack = storyInputPack,
            chainReportFile = chainReportFile,
            renderPackageFile = renderPackageFile,
            assetPromptCardsFile = assetPromptCardsFile,
            storyInputFile = storyInputFile,
            findings = findings,
        )
        val hasBlocking = findings.hasBlocking()
        val previewPlan = if (hasBlocking) LocalStoryVideoPreviewPlan() else buildPreviewPlan(storyInputPack, renderPackage, assetPromptCards)
        val compatibility = buildCompatibility(previewPlan)
        val status = if (!hasBlocking) draftStatus else "FAIL"
        val resolvedHandoffId = handoffId.ifBlank {
            storyInputPack.packId.takeIf { it.isNotBlank() }?.let { "$it-preview-handoff-v0" }.orEmpty()
        }

        return LocalStoryVideoPreviewHandoffDraft(
            status = status,
            handoffId = resolvedHandoffId,
            sourceStoryInputPackId = storyInputPack.packId,
            sourceChainReportFile = chainReportFile,
            sourceRenderPackageFile = renderPackageFile,
            sourceAssetPromptCardsFile = assetPromptCardsFile,
            sourceStoryInputFile = storyInputFile,
            sourceChainStatus = chainReport.status,
            sourceRenderPackageStatus = renderPackage.status,
            sourceAssetPromptCardsStatus = assetPromptCards.status,
            readinessChecks = readinessChecks,
            previewPlan = previewPlan,
            localRendererCompatibility = compatibility,
            boundaries = boundaries(chainReport, renderPackage, assetPromptCards, storyInputPack),
            downstreamBoundary = downstreamBoundary,
            nextAllowedDownstreamStep = if (status == draftStatus) nextAllowedDownstreamStep else "",
            blockedReason = if (status == draftStatus) "" else findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId },
            findingCount = findings.size,
            findings = findings,
            evidence = evidence(readinessChecks, previewPlan, compatibility),
        )
    }

    private fun buildReadinessChecks(
        chainReport: LocalStoryVideoChainReport,
        renderPackage: LocalRenderPackageDraft,
        assetPromptCards: LocalAssetPromptCardsDraft,
        storyInputPack: LocalStoryInputPack,
        chainReportFile: String,
        renderPackageFile: String,
        assetPromptCardsFile: String,
        storyInputFile: String,
        findings: MutableList<LocalStoryVideoPreviewHandoffFinding>,
    ): List<LocalStoryVideoPreviewHandoffReadinessCheck> {
        val storyId = storyInputPack.packId
        return listOf(
            check(
                "explicit-source-files",
                "source_validation",
                listOf(chainReportFile, renderPackageFile, assetPromptCardsFile, storyInputFile).all { it.isNotBlank() },
                "All Phase 5 source files are explicit; the handoff does not guess paths.",
                listOf(
                    "chainReportFile=$chainReportFile",
                    "renderPackageFile=$renderPackageFile",
                    "assetPromptCardsFile=$assetPromptCardsFile",
                    "storyInputFile=$storyInputFile",
                ),
                findings,
                "LOCAL_PREVIEW_HANDOFF_SOURCE_FILE_MISSING",
                "$.sourceFiles",
                "Pass --chain-report, --render-package, --asset-prompt-cards, --story-input, and --out.",
            ),
            check(
                "chain-status",
                "source_validation",
                chainReport.status == requiredChainStatus,
                "Chain report status is LOCAL_STORY_VIDEO_CHAIN_VERIFIED.",
                listOf(chainReport.status),
                findings,
                "LOCAL_PREVIEW_HANDOFF_CHAIN_STATUS_NOT_VERIFIED",
                "$.chainReport.status",
                "Rerun verify-local-story-video-chain and use its verified report.",
            ),
            check(
                "render-package-status",
                "source_validation",
                renderPackage.status == requiredRenderPackageStatus,
                "Render package status is LOCAL_RENDER_PACKAGE_DRAFT.",
                listOf(renderPackage.status),
                findings,
                "LOCAL_PREVIEW_HANDOFF_RENDER_PACKAGE_STATUS_NOT_DRAFT",
                "$.renderPackage.status",
                "Use a PASS local render package draft.",
            ),
            check(
                "asset-prompt-cards-status",
                "source_validation",
                assetPromptCards.status == requiredAssetPromptCardsStatus,
                "Asset/prompt cards status is LOCAL_ASSET_PROMPT_CARDS_DRAFT.",
                listOf(assetPromptCards.status),
                findings,
                "LOCAL_PREVIEW_HANDOFF_ASSET_PROMPT_STATUS_NOT_DRAFT",
                "$.assetPromptCards.status",
                "Use a PASS local asset/prompt cards draft.",
            ),
            check(
                "source-story-pack-id-match",
                "source_linkage",
                storyId.isNotBlank() &&
                    chainReport.sourceStoryInputPackId == storyId &&
                    renderPackage.sourceStoryInputPackId == storyId &&
                    assetPromptCards.sourceStoryInputPackId == storyId,
                "Story/source ids match across story input, chain report, render package, and asset/prompt cards.",
                listOf(
                    "story=$storyId",
                    "chain=${chainReport.sourceStoryInputPackId}",
                    "render=${renderPackage.sourceStoryInputPackId}",
                    "cards=${assetPromptCards.sourceStoryInputPackId}",
                ),
                findings,
                "LOCAL_PREVIEW_HANDOFF_STORY_ID_MISMATCH",
                "$.sourceStoryInputPackId",
                "Use Phase 1-4 outputs generated from the same story input pack.",
            ),
            check(
                "asset-prompt-source-match",
                "source_linkage",
                renderPackage.sourceAssetPromptCardsStatus == assetPromptCards.status &&
                    renderPackage.sourceAssetPromptCardsDraftId == assetPromptCards.draftId,
                "Render package source asset/prompt card metadata matches the supplied cards draft.",
                listOf(
                    "render.sourceAssetPromptCardsStatus=${renderPackage.sourceAssetPromptCardsStatus}",
                    "cards.status=${assetPromptCards.status}",
                    "render.sourceAssetPromptCardsDraftId=${renderPackage.sourceAssetPromptCardsDraftId}",
                    "cards.draftId=${assetPromptCards.draftId}",
                ),
                findings,
                "LOCAL_PREVIEW_HANDOFF_ASSET_PROMPT_SOURCE_MISMATCH",
                "$.renderPackage.sourceAssetPromptCardsDraftId",
                "Use the render package generated from the supplied asset/prompt cards draft.",
            ),
            check(
                "chain-stage-statuses",
                "source_validation",
                chainReport.stageResults.storyValidation.status == "PASS" &&
                    chainReport.stageResults.assetPromptCards.status == requiredAssetPromptCardsStatus &&
                    chainReport.stageResults.localRenderPackage.status == requiredRenderPackageStatus,
                "Chain stage results show PASS story validation, draft asset/prompt cards, and draft render package.",
                listOf(
                    "storyValidation=${chainReport.stageResults.storyValidation.status}",
                    "assetPromptCards=${chainReport.stageResults.assetPromptCards.status}",
                    "localRenderPackage=${chainReport.stageResults.localRenderPackage.status}",
                ),
                findings,
                "LOCAL_PREVIEW_HANDOFF_CHAIN_STAGE_STATUS_MISMATCH",
                "$.chainReport.stageResults",
                "Use a complete Phase 4 chain report.",
            ),
            check(
                "render-plan-present",
                "preview_plan",
                renderPackage.timelinePlan.isNotEmpty() &&
                    renderPackage.panelPlan.isNotEmpty() &&
                    renderPackage.evidence.qaFailCount == 0,
                "Render package includes timeline/panel plans and has zero QA failures.",
                listOf(
                    "timeline=${renderPackage.timelinePlan.size}",
                    "panels=${renderPackage.panelPlan.size}",
                    "qaFail=${renderPackage.evidence.qaFailCount}",
                ),
                findings,
                "LOCAL_PREVIEW_HANDOFF_RENDER_PLAN_NOT_READY",
                "$.renderPackage",
                "Fix the local render package draft before preview handoff.",
            ),
            check(
                "local-boundaries",
                "boundary",
                boundariesOk(chainReport, renderPackage, assetPromptCards, storyInputPack),
                "All sources preserve file-protocol/no-external/no-renderer/no-media-generation boundaries.",
                listOf(
                    "chain.mediaGenerated=${chainReport.boundaries.mediaGenerated}",
                    "chain.rendererInvocation=${chainReport.boundaries.rendererInvocation}",
                    "chain.providerInvocation=${chainReport.boundaries.providerInvocation}",
                    "chain.ttsInvocation=${chainReport.boundaries.ttsInvocation}",
                    "story.noExternal=${storyInputPack.constraints.noExternalPaidProviderTools}",
                    "cards.noExternal=${assetPromptCards.localOnlyConstraints.noExternalPaidProviderTools}",
                    "render.noExternal=${renderPackage.localOnlyConstraints.noExternalPaidProviderTools}",
                ),
                findings,
                "LOCAL_PREVIEW_HANDOFF_BOUNDARY_MISMATCH",
                "$.boundaries",
                "Use local-only Phase 1-4 outputs with no provider/API, renderer, TTS, or media generation.",
            ),
        )
    }

    private fun buildPreviewPlan(
        storyInputPack: LocalStoryInputPack,
        renderPackage: LocalRenderPackageDraft,
        assetPromptCards: LocalAssetPromptCardsDraft,
    ): LocalStoryVideoPreviewPlan {
        val beatsById = storyInputPack.beats.associateBy { it.id }
        val timelineByBeat = renderPackage.timelinePlan.associateBy { it.sourceBeatId }
        val visualById = assetPromptCards.visualPromptCards.associateBy { it.cardId }
        val audioById = assetPromptCards.audioCueCards.associateBy { it.cardId }
        val assetRefsByBeat = renderPackage.assetPromptReferences.associateBy { it.sourceBeatId }
        val panels = renderPackage.panelPlan
            .sortedBy { timelineByBeat[it.sourceBeatId]?.startMillis ?: Int.MAX_VALUE }
            .mapIndexed { index, panel ->
                val timeline = timelineByBeat[panel.sourceBeatId]
                val beat = beatsById[panel.sourceBeatId]
                val assetRef = assetRefsByBeat[panel.sourceBeatId]
                val visual = visualById[panel.visualPromptCardId]
                val audio = audioById[panel.audioCueCardId]
                LocalStoryVideoPreviewPanelPlan(
                    order = index + 1,
                    panelId = panel.panelId,
                    sourceBeatId = panel.sourceBeatId,
                    sourceSceneId = panel.sourceSceneId,
                    sceneTitle = panel.sceneTitle,
                    startMillis = timeline?.startMillis ?: 0,
                    endMillis = timeline?.endMillis ?: 0,
                    durationMillis = panel.durationMillis,
                    captionCue = beat?.narrativeIntent.orEmpty(),
                    dialogueCue = beat?.dialogueIntent.orEmpty(),
                    visualIntent = panel.visualIntent,
                    audioIntent = panel.audioIntent,
                    characterCardIds = panel.characterCardIds,
                    visualPromptCardId = panel.visualPromptCardId,
                    visualPromptSeed = visual?.promptSeed.orEmpty(),
                    audioCueCardId = panel.audioCueCardId,
                    audioCueSummary = listOf(audio?.voiceCueSummary.orEmpty(), audio?.audioIntent.orEmpty())
                        .filter { it.isNotBlank() }
                        .joinToString(" | "),
                    assetPromptReference = LocalStoryVideoPreviewAssetPromptReference(
                        sourceBeatId = assetRef?.sourceBeatId.orEmpty(),
                        sourceSceneId = assetRef?.sourceSceneId.orEmpty(),
                        beatCardId = assetRef?.beatCardId.orEmpty(),
                        sceneCardId = assetRef?.sceneCardId.orEmpty(),
                        visualPromptCardId = assetRef?.visualPromptCardId.orEmpty(),
                        audioCueCardId = assetRef?.audioCueCardId.orEmpty(),
                        characterCardIds = assetRef?.characterCardIds.orEmpty(),
                    ),
                )
            }
        return LocalStoryVideoPreviewPlan(
            sceneOrdering = renderPackage.scenePlan.map {
                LocalStoryVideoPreviewSceneOrder(
                    scenePlanId = it.scenePlanId,
                    sourceSceneId = it.sourceSceneId,
                    title = it.title,
                    location = it.location,
                    mood = it.mood,
                    panelIds = it.panelIds,
                )
            },
            panelOrdering = panels,
            targetDurationMillis = renderPackage.evidence.targetDurationMillis,
            qaSummary = LocalStoryVideoPreviewQaSummary(
                qaChecks = renderPackage.evidence.qaCheckCount,
                qaPass = renderPackage.evidence.qaPassCount,
                qaFail = renderPackage.evidence.qaFailCount,
                sourceQaCheckIds = renderPackage.qaChecklist.map { it.checkId },
            ),
        )
    }

    private fun buildCompatibility(previewPlan: LocalStoryVideoPreviewPlan): LocalStoryVideoLocalRendererCompatibility =
        LocalStoryVideoLocalRendererCompatibility(
            ready = false,
            readyFields = listOf(
                "scene/panel ordering",
                "target durations",
                "caption/dialogue cues",
                "visual prompt references",
                "audio cue references",
                "QA summary",
            ).filter { previewPlan.panelOrdering.isNotEmpty() || it == "QA summary" },
            missingFields = listOf(
                "adapter to ComicVideoRenderHandoffManifest",
                "reviewed ComicVideoRenderPanelTimelineItem sourceFrameId/mustShow/doNotShow/continuityAnchors",
                "reviewed ComicVideoRenderVoiceTimelineItem spoken text and voice labels",
                "reviewed ComicVideoRenderSubtitleTimelineItem subtitle text",
                "local renderer dry-run PASS before actual MP4 generation",
            ),
            requiredChecksBeforeMp4 = listOf(
                "Review this preview handoff draft.",
                "Create or choose a local adapter from LocalStoryVideoPreviewHandoffDraft to ComicVideoRenderHandoffManifest.",
                "Run a dry-run renderer/preflight command before any media generation.",
                "Keep provider/API, TTS, final approval, provider/media handoff, canon, registry, release, and production lock boundaries explicit.",
            ),
        )

    private fun boundariesOk(
        chainReport: LocalStoryVideoChainReport,
        renderPackage: LocalRenderPackageDraft,
        assetPromptCards: LocalAssetPromptCardsDraft,
        storyInputPack: LocalStoryInputPack,
    ): Boolean =
        storyInputPack.constraints.localOnly &&
            storyInputPack.constraints.noExternalPaidProviderTools &&
            storyInputPack.constraints.fileProtocolJsonOnly &&
            assetPromptCards.localOnlyConstraints.localOnly &&
            assetPromptCards.localOnlyConstraints.noExternalPaidProviderTools &&
            assetPromptCards.localOnlyConstraints.fileProtocolJsonOnly &&
            renderPackage.localOnlyConstraints.localOnly &&
            renderPackage.localOnlyConstraints.noExternalPaidProviderTools &&
            renderPackage.localOnlyConstraints.fileProtocolJsonOnly &&
            chainReport.boundaries.fileProtocolJsonOnly &&
            chainReport.boundaries.noExternalPaidProviderTools &&
            chainReport.boundaries.providerInvocation == "none" &&
            chainReport.boundaries.ttsInvocation == "none" &&
            !chainReport.boundaries.mediaGenerated &&
            chainReport.boundaries.rendererInvocation == "none" &&
            !chainReport.boundaries.directSqliteWrites &&
            !chainReport.boundaries.providerApiCalls &&
            !chainReport.boundaries.finalMediaApproval &&
            !chainReport.boundaries.release &&
            !chainReport.boundaries.canonRegistryOrProductionLock &&
            assetPromptCards.visualPromptCards.all { it.providerInvocation == "none" } &&
            assetPromptCards.audioCueCards.all { it.ttsInvocation == "none" }

    private fun boundaries(
        chainReport: LocalStoryVideoChainReport,
        renderPackage: LocalRenderPackageDraft,
        assetPromptCards: LocalAssetPromptCardsDraft,
        storyInputPack: LocalStoryInputPack,
    ): LocalStoryVideoPreviewHandoffBoundaries =
        LocalStoryVideoPreviewHandoffBoundaries(
            fileProtocolJsonOnly = storyInputPack.constraints.fileProtocolJsonOnly &&
                assetPromptCards.localOnlyConstraints.fileProtocolJsonOnly &&
                renderPackage.localOnlyConstraints.fileProtocolJsonOnly &&
                chainReport.boundaries.fileProtocolJsonOnly,
            noExternalPaidProviderTools = storyInputPack.constraints.noExternalPaidProviderTools &&
                assetPromptCards.localOnlyConstraints.noExternalPaidProviderTools &&
                renderPackage.localOnlyConstraints.noExternalPaidProviderTools &&
                chainReport.boundaries.noExternalPaidProviderTools,
            providerInvocation = if (assetPromptCards.visualPromptCards.all { it.providerInvocation == "none" } &&
                chainReport.boundaries.providerInvocation == "none"
            ) "none" else "present",
            ttsInvocation = if (assetPromptCards.audioCueCards.all { it.ttsInvocation == "none" } &&
                chainReport.boundaries.ttsInvocation == "none"
            ) "none" else "present",
            mediaGenerated = false,
            rendererInvocation = "none",
            directSqliteWrites = false,
            providerApiCalls = false,
            finalMediaApproval = false,
            release = false,
            canonRegistryOrProductionLock = false,
        )

    private fun evidence(
        readinessChecks: List<LocalStoryVideoPreviewHandoffReadinessCheck>,
        previewPlan: LocalStoryVideoPreviewPlan,
        compatibility: LocalStoryVideoLocalRendererCompatibility,
    ): LocalStoryVideoPreviewHandoffEvidence =
        LocalStoryVideoPreviewHandoffEvidence(
            readinessCheckCount = readinessChecks.size,
            readinessPassCount = readinessChecks.count { it.status == "PASS" },
            readinessFailCount = readinessChecks.count { it.status != "PASS" },
            sceneCount = previewPlan.sceneOrdering.size,
            panelCount = previewPlan.panelOrdering.size,
            visualPromptReferenceCount = previewPlan.panelOrdering.count { it.visualPromptCardId.isNotBlank() },
            audioCueReferenceCount = previewPlan.panelOrdering.count { it.audioCueCardId.isNotBlank() },
            targetDurationMillis = previewPlan.targetDurationMillis,
            localRendererReady = compatibility.ready,
        )

    private fun check(
        checkId: String,
        category: String,
        passed: Boolean,
        message: String,
        evidence: List<String>,
        findings: MutableList<LocalStoryVideoPreviewHandoffFinding>,
        ruleId: String,
        path: String,
        suggestedFix: String,
    ): LocalStoryVideoPreviewHandoffReadinessCheck {
        if (!passed) {
            findings += finding(
                ruleId = ruleId,
                category = category,
                severity = "CRITICAL",
                path = path,
                message = message,
                evidence = evidence,
                suggestedFix = suggestedFix,
            )
        }
        return LocalStoryVideoPreviewHandoffReadinessCheck(
            checkId = checkId,
            status = if (passed) "PASS" else "FAIL",
            category = category,
            message = message,
            evidence = evidence,
        )
    }

    private fun List<LocalStoryVideoPreviewHandoffFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): LocalStoryVideoPreviewHandoffFinding =
        LocalStoryVideoPreviewHandoffFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
}
