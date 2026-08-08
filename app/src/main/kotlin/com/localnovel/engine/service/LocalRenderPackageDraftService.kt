package com.localnovel.engine.service

import com.localnovel.engine.model.LocalAssetPromptCardsDraft
import com.localnovel.engine.model.LocalRenderAssetPromptReference
import com.localnovel.engine.model.LocalRenderPackageConstraints
import com.localnovel.engine.model.LocalRenderPackageDraft
import com.localnovel.engine.model.LocalRenderPackageDraftEvidence
import com.localnovel.engine.model.LocalRenderPackageDraftFinding
import com.localnovel.engine.model.LocalRenderPanelPlan
import com.localnovel.engine.model.LocalRenderQaCheck
import com.localnovel.engine.model.LocalRenderScenePlan
import com.localnovel.engine.model.LocalRenderTimelineItem
import com.localnovel.engine.model.LocalStoryInputBeat
import com.localnovel.engine.model.LocalStoryInputConstraints
import com.localnovel.engine.model.LocalStoryInputPack

class LocalRenderPackageDraftService {
    private val draftStatus = "LOCAL_RENDER_PACKAGE_DRAFT"
    private val requiredCardsStatus = "LOCAL_ASSET_PROMPT_CARDS_DRAFT"
    private val nextAllowedDownstreamStep = "local_render_package_review"
    private val renderBoundary = "local_render_package_draft_only_not_media_generation"
    private val downstreamBoundary = "local_render_package_draft_only"

    fun create(
        storyInputPack: LocalStoryInputPack,
        assetPromptCards: LocalAssetPromptCardsDraft,
        storyInputPackFile: String = "",
        assetPromptCardsFile: String = "",
        packageId: String = "",
    ): LocalRenderPackageDraft {
        val findings = mutableListOf<LocalRenderPackageDraftFinding>()
        validatePreconditions(storyInputPack, assetPromptCards, findings)

        val resolvedPackageId = packageId.ifBlank {
            storyInputPack.packId.takeIf { it.isNotBlank() }?.let { "$it-local-render-package-v0" }.orEmpty()
        }
        val hasBlockingFindings = findings.hasBlocking()
        val timelinePlan = if (hasBlockingFindings) emptyList() else buildTimeline(storyInputPack)
        val assetReferences = if (hasBlockingFindings) emptyList() else buildAssetPromptReferences(storyInputPack, assetPromptCards)
        val panelPlan = if (hasBlockingFindings) emptyList() else buildPanelPlan(storyInputPack, assetPromptCards, timelinePlan, assetReferences)
        val scenePlan = if (hasBlockingFindings) emptyList() else buildScenePlan(storyInputPack, assetPromptCards, panelPlan)
        val qaChecklist = buildQaChecklist(
            storyInputPack = storyInputPack,
            assetPromptCards = assetPromptCards,
            timelinePlan = timelinePlan,
        )
        val qaHasBlockingFailure = qaChecklist.any { it.requiredForPass && it.status != "PASS" }
        val status = if (!hasBlockingFindings && !qaHasBlockingFailure) draftStatus else "FAIL"
        val blockingReasons = findings
            .filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }
            .map { it.ruleId }
            .plus(qaChecklist.filter { it.requiredForPass && it.status != "PASS" }.map { it.checkId })

        return LocalRenderPackageDraft(
            status = status,
            packageId = resolvedPackageId,
            sourceStoryInputPackFile = storyInputPackFile,
            sourceAssetPromptCardsFile = assetPromptCardsFile,
            sourceStoryInputPackId = storyInputPack.packId,
            sourceAssetPromptCardsStatus = assetPromptCards.status,
            sourceAssetPromptCardsDraftId = assetPromptCards.draftId,
            sourceTitle = storyInputPack.title,
            locale = storyInputPack.locale,
            localOnlyConstraints = copyConstraints(storyInputPack.constraints, assetPromptCards.localOnlyConstraints),
            timelinePlan = if (status == draftStatus) timelinePlan else emptyList(),
            scenePlan = if (status == draftStatus) scenePlan else emptyList(),
            panelPlan = if (status == draftStatus) panelPlan else emptyList(),
            assetPromptReferences = if (status == draftStatus) assetReferences else emptyList(),
            qaChecklist = qaChecklist,
            renderBoundary = renderBoundary,
            downstreamBoundary = downstreamBoundary,
            nextAllowedDownstreamStep = if (status == draftStatus) nextAllowedDownstreamStep else "",
            blockedReason = blockingReasons.joinToString("; "),
            findingCount = findings.size,
            findings = findings,
            evidence = evidence(
                storyInputPack = storyInputPack,
                assetPromptCards = assetPromptCards,
                timelinePlan = timelinePlan,
                scenePlan = scenePlan,
                panelPlan = panelPlan,
                assetReferences = assetReferences,
                qaChecklist = qaChecklist,
            ),
        )
    }

    private fun validatePreconditions(
        storyInputPack: LocalStoryInputPack,
        assetPromptCards: LocalAssetPromptCardsDraft,
        findings: MutableList<LocalRenderPackageDraftFinding>,
    ) {
        if (assetPromptCards.status != requiredCardsStatus) {
            findings += finding(
                "LOCAL_RENDER_ASSET_PROMPT_STATUS_NOT_DRAFT",
                "source_validation",
                "CRITICAL",
                "$.assetPromptCards.status",
                "Local render package draft requires asset/prompt cards status LOCAL_ASSET_PROMPT_CARDS_DRAFT.",
                listOf(assetPromptCards.status),
                "Run draft-local-asset-prompt-cards and use its draft output.",
            )
        }
        if (storyInputPack.packId.isBlank()) {
            findings += finding(
                "LOCAL_RENDER_STORY_PACK_ID_MISSING",
                "source_validation",
                "CRITICAL",
                "$.storyInput.packId",
                "Local render package draft requires a source story input pack id.",
            )
        }
        if (assetPromptCards.sourceStoryInputPackId != storyInputPack.packId) {
            findings += finding(
                "LOCAL_RENDER_STORY_PACK_ID_MISMATCH",
                "source_validation",
                "CRITICAL",
                "$.assetPromptCards.sourceStoryInputPackId",
                "Asset/prompt cards sourceStoryInputPackId must match the story input pack id.",
                listOf(
                    "storyInputPack.packId=${storyInputPack.packId}",
                    "assetPromptCards.sourceStoryInputPackId=${assetPromptCards.sourceStoryInputPackId}",
                ),
                "Use the asset/prompt cards generated from this exact story input pack.",
            )
        }
        if (storyInputPack.beats.isEmpty()) {
            findings += finding(
                "LOCAL_RENDER_STORY_BEATS_MISSING",
                "timeline",
                "CRITICAL",
                "$.storyInput.beats",
                "Local render package draft requires at least one story beat.",
            )
        }
        if (storyInputPack.durationTargetMillis <= 0) {
            findings += finding(
                "LOCAL_RENDER_DURATION_TARGET_INVALID",
                "timeline",
                "CRITICAL",
                "$.storyInput.durationTargetMillis",
                "Local render package draft requires a positive duration target.",
                listOf(storyInputPack.durationTargetMillis.toString()),
            )
        }
        if (storyInputPack.durationTargetMillis in 1 until storyInputPack.beats.size) {
            findings += finding(
                "LOCAL_RENDER_DURATION_TOO_SHORT_FOR_BEATS",
                "timeline",
                "CRITICAL",
                "$.storyInput.durationTargetMillis",
                "Duration target must allow at least 1 ms per beat in the deterministic v0 timeline.",
                listOf("durationTargetMillis=${storyInputPack.durationTargetMillis}", "beatCount=${storyInputPack.beats.size}"),
            )
        }
        validateLocalConstraints(storyInputPack.constraints, assetPromptCards, findings)
        validateCardReferences(storyInputPack, assetPromptCards, findings)
        validateNoInvocation(assetPromptCards, findings)
    }

    private fun validateLocalConstraints(
        storyConstraints: LocalStoryInputConstraints,
        assetPromptCards: LocalAssetPromptCardsDraft,
        findings: MutableList<LocalRenderPackageDraftFinding>,
    ) {
        if (!storyConstraints.localOnly || !assetPromptCards.localOnlyConstraints.localOnly) {
            findings += finding("LOCAL_RENDER_LOCAL_ONLY_NOT_DECLARED", "boundary", "CRITICAL", "$.localOnlyConstraints.localOnly", "Story input pack and asset/prompt cards must both declare local-only handling.")
        }
        if (!storyConstraints.noExternalPaidProviderTools || !assetPromptCards.localOnlyConstraints.noExternalPaidProviderTools) {
            findings += finding("LOCAL_RENDER_NO_EXTERNAL_TOOLS_NOT_DECLARED", "boundary", "CRITICAL", "$.localOnlyConstraints.noExternalPaidProviderTools", "Story input pack and asset/prompt cards must both declare no external paid/provider tools.")
        }
        if (!storyConstraints.fileProtocolJsonOnly || !assetPromptCards.localOnlyConstraints.fileProtocolJsonOnly) {
            findings += finding("LOCAL_RENDER_FILE_PROTOCOL_JSON_NOT_DECLARED", "boundary", "CRITICAL", "$.localOnlyConstraints.fileProtocolJsonOnly", "Story input pack and asset/prompt cards must both remain file-protocol JSON only.")
        }
    }

    private fun validateCardReferences(
        storyInputPack: LocalStoryInputPack,
        assetPromptCards: LocalAssetPromptCardsDraft,
        findings: MutableList<LocalRenderPackageDraftFinding>,
    ) {
        val beatCardIds = assetPromptCards.beatCards.map { it.sourceBeatId }.toSet()
        val visualBeatIds = assetPromptCards.visualPromptCards.map { it.sourceBeatId }.toSet()
        val audioBeatIds = assetPromptCards.audioCueCards.map { it.sourceBeatId }.toSet()
        val sceneCardIds = assetPromptCards.sceneCards.map { it.sourceSceneId }.toSet()
        storyInputPack.beats.forEachIndexed { index, beat ->
            val path = "$.storyInput.beats[$index]"
            if (beat.id !in beatCardIds) {
                findings += finding("LOCAL_RENDER_BEAT_CARD_MISSING", "source_linkage", "CRITICAL", "$path.id", "Every story beat needs a matching beat card.", listOf(beat.id))
            }
            if (beat.id !in visualBeatIds) {
                findings += finding("LOCAL_RENDER_VISUAL_PROMPT_CARD_MISSING", "source_linkage", "CRITICAL", "$path.id", "Every story beat needs a matching visual prompt card.", listOf(beat.id))
            }
            if (beat.id !in audioBeatIds) {
                findings += finding("LOCAL_RENDER_AUDIO_CUE_CARD_MISSING", "source_linkage", "CRITICAL", "$path.id", "Every story beat needs a matching audio cue card.", listOf(beat.id))
            }
            if (beat.sceneId !in sceneCardIds) {
                findings += finding("LOCAL_RENDER_SCENE_CARD_MISSING", "source_linkage", "CRITICAL", "$path.sceneId", "Every beat sceneId needs a matching scene card.", listOf(beat.sceneId))
            }
        }
    }

    private fun validateNoInvocation(
        assetPromptCards: LocalAssetPromptCardsDraft,
        findings: MutableList<LocalRenderPackageDraftFinding>,
    ) {
        assetPromptCards.visualPromptCards.forEachIndexed { index, card ->
            if (card.providerInvocation != "none") {
                findings += finding("LOCAL_RENDER_PROVIDER_INVOCATION_PRESENT", "boundary", "CRITICAL", "$.assetPromptCards.visualPromptCards[$index].providerInvocation", "Render package draft cannot consume visual cards with provider invocation.", listOf(card.providerInvocation))
            }
        }
        assetPromptCards.audioCueCards.forEachIndexed { index, card ->
            if (card.ttsInvocation != "none") {
                findings += finding("LOCAL_RENDER_TTS_INVOCATION_PRESENT", "boundary", "CRITICAL", "$.assetPromptCards.audioCueCards[$index].ttsInvocation", "Render package draft cannot consume audio cards with TTS invocation.", listOf(card.ttsInvocation))
            }
        }
    }

    private fun buildTimeline(storyInputPack: LocalStoryInputPack): List<LocalRenderTimelineItem> {
        val beatCount = storyInputPack.beats.size
        val baseDuration = storyInputPack.durationTargetMillis / beatCount
        val remainder = storyInputPack.durationTargetMillis % beatCount
        var startMillis = 0
        return storyInputPack.beats.mapIndexed { index, beat ->
            val duration = baseDuration + if (index < remainder) 1 else 0
            val item = LocalRenderTimelineItem(
                itemId = timelineItemId(beat.id),
                sourceBeatId = beat.id,
                sourceSceneId = beat.sceneId,
                startMillis = startMillis,
                endMillis = startMillis + duration,
                durationMillis = duration,
                narrativeIntent = beat.narrativeIntent,
                dialogueIntent = beat.dialogueIntent,
                audioIntent = beat.audioIntent,
            )
            startMillis += duration
            item
        }
    }

    private fun buildScenePlan(
        storyInputPack: LocalStoryInputPack,
        assetPromptCards: LocalAssetPromptCardsDraft,
        panelPlan: List<LocalRenderPanelPlan>,
    ): List<LocalRenderScenePlan> {
        val sceneCardsBySource = assetPromptCards.sceneCards.associateBy { it.sourceSceneId }
        val panelsByScene = panelPlan.groupBy { it.sourceSceneId }
        return storyInputPack.scenes.map { scene ->
            LocalRenderScenePlan(
                scenePlanId = scenePlanId(scene.id),
                sourceSceneId = scene.id,
                sceneCardId = sceneCardsBySource[scene.id]?.cardId.orEmpty(),
                title = scene.title,
                location = scene.location,
                mood = scene.mood,
                panelIds = panelsByScene[scene.id].orEmpty().map { it.panelId },
            )
        }
    }

    private fun buildPanelPlan(
        storyInputPack: LocalStoryInputPack,
        assetPromptCards: LocalAssetPromptCardsDraft,
        timelinePlan: List<LocalRenderTimelineItem>,
        assetReferences: List<LocalRenderAssetPromptReference>,
    ): List<LocalRenderPanelPlan> {
        val sceneTitleById = storyInputPack.scenes.associate { it.id to it.title }
        val beatById = storyInputPack.beats.associateBy { it.id }
        val timelineByBeat = timelinePlan.associateBy { it.sourceBeatId }
        val refsByBeat = assetReferences.associateBy { it.sourceBeatId }
        return assetPromptCards.beatCards.mapNotNull { beatCard ->
            val beat = beatById[beatCard.sourceBeatId] ?: return@mapNotNull null
            val timeline = timelineByBeat[beat.id] ?: return@mapNotNull null
            val refs = refsByBeat[beat.id] ?: return@mapNotNull null
            LocalRenderPanelPlan(
                panelId = panelId(beat.id),
                timelineItemId = timeline.itemId,
                sourceBeatId = beat.id,
                sourceSceneId = beat.sceneId,
                sceneTitle = sceneTitleById[beat.sceneId].orEmpty(),
                characterCardIds = refs.characterCardIds,
                visualPromptCardId = refs.visualPromptCardId,
                audioCueCardId = refs.audioCueCardId,
                durationMillis = timeline.durationMillis,
                visualIntent = beat.visualIntent,
                audioIntent = beat.audioIntent,
            )
        }
    }

    private fun buildAssetPromptReferences(
        storyInputPack: LocalStoryInputPack,
        assetPromptCards: LocalAssetPromptCardsDraft,
    ): List<LocalRenderAssetPromptReference> {
        val beatCardsByBeat = assetPromptCards.beatCards.associateBy { it.sourceBeatId }
        val sceneCardsByScene = assetPromptCards.sceneCards.associateBy { it.sourceSceneId }
        val visualCardsByBeat = assetPromptCards.visualPromptCards.associateBy { it.sourceBeatId }
        val audioCardsByBeat = assetPromptCards.audioCueCards.associateBy { it.sourceBeatId }
        return storyInputPack.beats.map { beat ->
            val beatCard = beatCardsByBeat[beat.id]
            LocalRenderAssetPromptReference(
                sourceBeatId = beat.id,
                sourceSceneId = beat.sceneId,
                characterCardIds = beatCard?.characterCardIds.orEmpty(),
                sceneCardId = sceneCardsByScene[beat.sceneId]?.cardId.orEmpty(),
                beatCardId = beatCard?.cardId.orEmpty(),
                visualPromptCardId = visualCardsByBeat[beat.id]?.cardId.orEmpty(),
                audioCueCardId = audioCardsByBeat[beat.id]?.cardId.orEmpty(),
            )
        }
    }

    private fun buildQaChecklist(
        storyInputPack: LocalStoryInputPack,
        assetPromptCards: LocalAssetPromptCardsDraft,
        timelinePlan: List<LocalRenderTimelineItem>,
    ): List<LocalRenderQaCheck> {
        val timelineTotal = timelinePlan.sumOf { it.durationMillis }
        return listOf(
            qa(
                checkId = "story-pack-match",
                category = "source_validation",
                passed = assetPromptCards.sourceStoryInputPackId == storyInputPack.packId,
                message = "Asset/prompt cards sourceStoryInputPackId matches story input pack id.",
                evidence = listOf("story=${storyInputPack.packId}", "cards=${assetPromptCards.sourceStoryInputPackId}"),
            ),
            qa(
                checkId = "card-status",
                category = "source_validation",
                passed = assetPromptCards.status == requiredCardsStatus,
                message = "Asset/prompt cards status is LOCAL_ASSET_PROMPT_CARDS_DRAFT.",
                evidence = listOf(assetPromptCards.status),
            ),
            qa(
                checkId = "duration-positive",
                category = "timeline",
                passed = storyInputPack.durationTargetMillis > 0 && timelineTotal == storyInputPack.durationTargetMillis,
                message = "Timeline duration is positive and equals the story duration target.",
                evidence = listOf("target=${storyInputPack.durationTargetMillis}", "timelineTotal=$timelineTotal"),
            ),
            qa(
                checkId = "local-only-constraints",
                category = "boundary",
                passed = storyInputPack.constraints.localOnly &&
                    storyInputPack.constraints.noExternalPaidProviderTools &&
                    storyInputPack.constraints.fileProtocolJsonOnly &&
                    assetPromptCards.localOnlyConstraints.localOnly &&
                    assetPromptCards.localOnlyConstraints.noExternalPaidProviderTools &&
                    assetPromptCards.localOnlyConstraints.fileProtocolJsonOnly,
                message = "Story input and card draft preserve local-only/no-external/file-protocol constraints.",
                evidence = listOf(
                    "story.localOnly=${storyInputPack.constraints.localOnly}",
                    "cards.localOnly=${assetPromptCards.localOnlyConstraints.localOnly}",
                    "story.noExternal=${storyInputPack.constraints.noExternalPaidProviderTools}",
                    "cards.noExternal=${assetPromptCards.localOnlyConstraints.noExternalPaidProviderTools}",
                    "story.fileProtocol=${storyInputPack.constraints.fileProtocolJsonOnly}",
                    "cards.fileProtocol=${assetPromptCards.localOnlyConstraints.fileProtocolJsonOnly}",
                ),
            ),
            qa(
                checkId = "no-provider-api",
                category = "boundary",
                passed = assetPromptCards.visualPromptCards.all { it.providerInvocation == "none" },
                message = "Visual prompt cards have no provider/API invocation.",
                evidence = assetPromptCards.visualPromptCards.map { "${it.cardId}:${it.providerInvocation}" },
            ),
            qa(
                checkId = "no-media-generation",
                category = "boundary",
                passed = assetPromptCards.audioCueCards.all { it.ttsInvocation == "none" },
                message = "Audio cue cards have no TTS/media generation invocation.",
                evidence = assetPromptCards.audioCueCards.map { "${it.cardId}:${it.ttsInvocation}" },
            ),
            qa(
                checkId = "no-final-approval-release-production-lock",
                category = "boundary",
                passed = true,
                message = "Render package is draft-only: no final approval, no release, no canon/registry, no production lock.",
                evidence = listOf(renderBoundary, downstreamBoundary, "no production lock"),
            ),
        )
    }

    private fun copyConstraints(
        storyConstraints: LocalStoryInputConstraints,
        cardConstraints: com.localnovel.engine.model.LocalAssetPromptCardsConstraints,
    ): LocalRenderPackageConstraints =
        LocalRenderPackageConstraints(
            localOnly = storyConstraints.localOnly && cardConstraints.localOnly,
            noExternalPaidProviderTools = storyConstraints.noExternalPaidProviderTools && cardConstraints.noExternalPaidProviderTools,
            fileProtocolJsonOnly = storyConstraints.fileProtocolJsonOnly && cardConstraints.fileProtocolJsonOnly,
            notes = (storyConstraints.notes + cardConstraints.notes + listOf("local render package draft only", "no media generation")).distinct(),
        )

    private fun evidence(
        storyInputPack: LocalStoryInputPack,
        assetPromptCards: LocalAssetPromptCardsDraft,
        timelinePlan: List<LocalRenderTimelineItem>,
        scenePlan: List<LocalRenderScenePlan>,
        panelPlan: List<LocalRenderPanelPlan>,
        assetReferences: List<LocalRenderAssetPromptReference>,
        qaChecklist: List<LocalRenderQaCheck>,
    ): LocalRenderPackageDraftEvidence =
        LocalRenderPackageDraftEvidence(
            assetPromptCardsStatus = assetPromptCards.status,
            timelineItemCount = timelinePlan.size,
            scenePlanCount = scenePlan.size,
            panelPlanCount = panelPlan.size,
            assetPromptReferenceCount = assetReferences.size,
            qaCheckCount = qaChecklist.size,
            qaPassCount = qaChecklist.count { it.status == "PASS" },
            qaFailCount = qaChecklist.count { it.status != "PASS" },
            targetDurationMillis = storyInputPack.durationTargetMillis,
            timelineTotalDurationMillis = timelinePlan.sumOf { it.durationMillis },
            localOnlyDeclared = storyInputPack.constraints.localOnly && assetPromptCards.localOnlyConstraints.localOnly,
            noExternalPaidProviderToolsDeclared = storyInputPack.constraints.noExternalPaidProviderTools && assetPromptCards.localOnlyConstraints.noExternalPaidProviderTools,
            fileProtocolJsonOnlyDeclared = storyInputPack.constraints.fileProtocolJsonOnly && assetPromptCards.localOnlyConstraints.fileProtocolJsonOnly,
        )

    private fun qa(
        checkId: String,
        category: String,
        passed: Boolean,
        message: String,
        evidence: List<String> = emptyList(),
    ): LocalRenderQaCheck =
        LocalRenderQaCheck(
            checkId = checkId,
            status = if (passed) "PASS" else "FAIL",
            category = category,
            message = message,
            evidence = evidence,
        )

    private fun MutableList<LocalRenderPackageDraftFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun timelineItemId(sourceBeatId: String): String =
        "local-render-timeline-$sourceBeatId"

    private fun panelId(sourceBeatId: String): String =
        "local-render-panel-$sourceBeatId"

    private fun scenePlanId(sourceSceneId: String): String =
        "local-render-scene-$sourceSceneId"

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): LocalRenderPackageDraftFinding =
        LocalRenderPackageDraftFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
}
