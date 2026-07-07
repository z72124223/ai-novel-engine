package com.localnovel.engine.service

import com.localnovel.engine.model.LocalAssetPromptCardsConstraints
import com.localnovel.engine.model.LocalAssetPromptCardsDraft
import com.localnovel.engine.model.LocalAssetPromptCardsDraftEvidence
import com.localnovel.engine.model.LocalAssetPromptCardsDraftFinding
import com.localnovel.engine.model.LocalAudioCueCard
import com.localnovel.engine.model.LocalBeatAssetCard
import com.localnovel.engine.model.LocalCharacterAssetCard
import com.localnovel.engine.model.LocalRenderIntent
import com.localnovel.engine.model.LocalSceneAssetCard
import com.localnovel.engine.model.LocalStoryInputBeat
import com.localnovel.engine.model.LocalStoryInputCharacter
import com.localnovel.engine.model.LocalStoryInputConstraints
import com.localnovel.engine.model.LocalStoryInputPack
import com.localnovel.engine.model.LocalStoryInputPackValidationReport
import com.localnovel.engine.model.LocalStoryInputScene
import com.localnovel.engine.model.LocalVisualPromptCard

class LocalAssetPromptCardDraftService {
    private val draftStatus = "LOCAL_ASSET_PROMPT_CARDS_DRAFT"
    private val nextAllowedDownstreamStep = "local_asset_prompt_cards_review"
    private val boundaries = listOf(
        "file-protocol JSON only",
        "no provider/API",
        "no media generation",
        "no final media approval",
        "no release",
        "no canon/registry",
        "no production lock",
    )

    fun create(
        storyInputPack: LocalStoryInputPack,
        storyValidationReport: LocalStoryInputPackValidationReport,
        storyInputPackFile: String = "",
        storyValidationReportFile: String = "",
        draftId: String = "",
    ): LocalAssetPromptCardsDraft {
        val findings = mutableListOf<LocalAssetPromptCardsDraftFinding>()
        validatePreconditions(storyInputPack, storyValidationReport, findings)

        val resolvedDraftId = draftId.ifBlank {
            storyInputPack.packId.takeIf { it.isNotBlank() }?.let { "$it-asset-prompt-cards-v0" }.orEmpty()
        }
        if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) {
            return failureDraft(
                storyInputPack = storyInputPack,
                storyValidationReport = storyValidationReport,
                storyInputPackFile = storyInputPackFile,
                storyValidationReportFile = storyValidationReportFile,
                draftId = resolvedDraftId,
                findings = findings,
            )
        }

        val sceneById = storyInputPack.scenes.associateBy { it.id }
        val characterById = storyInputPack.characters.associateBy { it.id }
        val characterCards = storyInputPack.characters.map(::buildCharacterCard)
        val sceneCards = storyInputPack.scenes.map { buildSceneCard(storyInputPack, it) }
        val beatCards = storyInputPack.beats.map { buildBeatCard(it, sceneById, characterById) }
        val visualPromptCards = storyInputPack.beats.map { buildVisualPromptCard(storyInputPack, it, sceneById) }
        val audioCueCards = storyInputPack.beats.map { buildAudioCueCard(storyInputPack, it, characterById) }
        val renderIntent = LocalRenderIntent(
            targetDurationMillis = storyInputPack.durationTargetMillis,
            estimatedBeatCount = storyInputPack.beats.size,
            suggestedPanelCount = storyInputPack.beats.size.coerceAtLeast(1),
            locale = storyInputPack.locale,
            summary = "Local render planning seed for ${storyInputPack.title}: ${storyInputPack.beats.size} beat(s), ${storyInputPack.durationTargetMillis} ms target.",
        )

        return LocalAssetPromptCardsDraft(
            status = draftStatus,
            draftId = resolvedDraftId,
            sourceStoryInputPackFile = storyInputPackFile,
            sourceStoryValidationReportFile = storyValidationReportFile,
            sourceStoryInputPackId = storyInputPack.packId,
            sourceStoryValidationId = storyValidationReport.validationId,
            sourceTitle = storyInputPack.title,
            locale = storyInputPack.locale,
            localOnlyConstraints = copyConstraints(storyInputPack.constraints),
            characterCards = characterCards,
            sceneCards = sceneCards,
            beatCards = beatCards,
            visualPromptCards = visualPromptCards,
            audioCueCards = audioCueCards,
            renderIntent = renderIntent,
            boundaries = boundaries,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
            findingCount = findings.size,
            findings = findings,
            evidence = evidence(
                storyInputPack = storyInputPack,
                storyValidationReport = storyValidationReport,
                characterCardCount = characterCards.size,
                sceneCardCount = sceneCards.size,
                beatCardCount = beatCards.size,
                visualPromptCardCount = visualPromptCards.size,
                audioCueCardCount = audioCueCards.size,
            ),
        )
    }

    private fun validatePreconditions(
        storyInputPack: LocalStoryInputPack,
        storyValidationReport: LocalStoryInputPackValidationReport,
        findings: MutableList<LocalAssetPromptCardsDraftFinding>,
    ) {
        if (storyValidationReport.verdict != "PASS") {
            findings += finding(
                "LOCAL_ASSET_PROMPT_STORY_VALIDATION_NOT_PASS",
                "source_validation",
                "CRITICAL",
                "$.storyValidation.verdict",
                "Local asset/prompt cards require a PASS local story input pack validation report.",
                listOf(storyValidationReport.verdict),
                "Run validate-local-story-input-pack and use its PASS report.",
            )
        }
        if (storyInputPack.packId.isBlank()) {
            findings += finding(
                "LOCAL_ASSET_PROMPT_STORY_PACK_ID_MISSING",
                "source_validation",
                "CRITICAL",
                "$.storyInput.packId",
                "Local asset/prompt cards require a source story input pack id.",
            )
        }
        if (storyValidationReport.packId != storyInputPack.packId) {
            findings += finding(
                "LOCAL_ASSET_PROMPT_STORY_PACK_ID_MISMATCH",
                "source_validation",
                "CRITICAL",
                "$.storyValidation.packId",
                "Story validation report packId must match the story input pack id.",
                listOf(
                    "storyInputPack.packId=${storyInputPack.packId}",
                    "storyValidationReport.packId=${storyValidationReport.packId}",
                ),
                "Use the validation report generated from this exact story input pack.",
            )
        }
        validateLocalConstraints(storyInputPack.constraints, findings)
    }

    private fun validateLocalConstraints(
        constraints: LocalStoryInputConstraints,
        findings: MutableList<LocalAssetPromptCardsDraftFinding>,
    ) {
        if (!constraints.localOnly) {
            findings += finding("LOCAL_ASSET_PROMPT_LOCAL_ONLY_NOT_DECLARED", "boundary", "CRITICAL", "$.storyInput.constraints.localOnly", "Source story input pack must declare local-only handling.")
        }
        if (!constraints.noExternalPaidProviderTools) {
            findings += finding("LOCAL_ASSET_PROMPT_NO_EXTERNAL_TOOLS_NOT_DECLARED", "boundary", "CRITICAL", "$.storyInput.constraints.noExternalPaidProviderTools", "Source story input pack must declare no external paid/provider tools.")
        }
        if (!constraints.fileProtocolJsonOnly) {
            findings += finding("LOCAL_ASSET_PROMPT_FILE_PROTOCOL_JSON_NOT_DECLARED", "boundary", "CRITICAL", "$.storyInput.constraints.fileProtocolJsonOnly", "Source story input pack must remain file-protocol JSON only.")
        }
    }

    private fun buildCharacterCard(character: LocalStoryInputCharacter): LocalCharacterAssetCard =
        LocalCharacterAssetCard(
            cardId = characterCardId(character.id),
            sourceCharacterId = character.id,
            name = character.name,
            role = character.role,
            visualCue = character.visualCue,
            voiceCue = character.voiceCue,
            publicMotivation = character.publicMotivation,
        )

    private fun buildSceneCard(storyInputPack: LocalStoryInputPack, scene: LocalStoryInputScene): LocalSceneAssetCard =
        LocalSceneAssetCard(
            cardId = sceneCardId(scene.id),
            sourceSceneId = scene.id,
            title = scene.title,
            location = scene.location,
            mood = scene.mood,
            visibleSituation = scene.visibleSituation,
            visualStyleSeed = listOf(
                storyInputPack.visualStyle.tone,
                storyInputPack.visualStyle.colorPalette,
                storyInputPack.visualStyle.cameraNotes,
                storyInputPack.visualStyle.subtitleStyle,
            ).filter { it.isNotBlank() }.joinToString(" | "),
        )

    private fun buildBeatCard(
        beat: LocalStoryInputBeat,
        sceneById: Map<String, LocalStoryInputScene>,
        characterById: Map<String, LocalStoryInputCharacter>,
    ): LocalBeatAssetCard =
        LocalBeatAssetCard(
            cardId = beatCardId(beat.id),
            sourceBeatId = beat.id,
            sourceSceneId = beat.sceneId,
            sceneTitle = sceneById[beat.sceneId]?.title.orEmpty(),
            characterIds = beat.characterIds,
            characterCardIds = beat.characterIds.filter { it in characterById }.map(::characterCardId),
            narrativeIntent = beat.narrativeIntent,
            dialogueIntent = beat.dialogueIntent,
            visualIntent = beat.visualIntent,
            audioIntent = beat.audioIntent,
        )

    private fun buildVisualPromptCard(
        storyInputPack: LocalStoryInputPack,
        beat: LocalStoryInputBeat,
        sceneById: Map<String, LocalStoryInputScene>,
    ): LocalVisualPromptCard {
        val scene = sceneById[beat.sceneId]
        val promptSeed = listOf(
            "Story: ${storyInputPack.title}",
            "Scene: ${scene?.title.orEmpty()} at ${scene?.location.orEmpty()}",
            "Mood: ${scene?.mood.orEmpty()}",
            "Beat: ${beat.narrativeIntent}",
            "Visual intent: ${beat.visualIntent}",
            "Style: ${storyInputPack.visualStyle.tone}",
            "Palette: ${storyInputPack.visualStyle.colorPalette}",
            "Camera: ${storyInputPack.visualStyle.cameraNotes}",
            "Subtitles: ${storyInputPack.visualStyle.subtitleStyle}",
        ).filter { !it.endsWith(": ") && it.substringAfter(": ").isNotBlank() }.joinToString(" | ")
        return LocalVisualPromptCard(
            cardId = visualPromptCardId(beat.id),
            sourceBeatId = beat.id,
            sourceSceneId = beat.sceneId,
            promptSeed = promptSeed,
            negativeBoundaryNotes = listOf(
                "No provider/API invocation.",
                "No media generation.",
                "No final approval, release, canon, registry, or production lock claim.",
                "No generated media path or provider job id.",
            ),
        )
    }

    private fun buildAudioCueCard(
        storyInputPack: LocalStoryInputPack,
        beat: LocalStoryInputBeat,
        characterById: Map<String, LocalStoryInputCharacter>,
    ): LocalAudioCueCard {
        val voiceCueSummary = beat.characterIds
            .mapNotNull { characterById[it] }
            .joinToString(" | ") { "${it.name}: ${it.voiceCue}" }
            .ifBlank { storyInputPack.audioStyle.voiceDirection }
        val audioStyleSeed = listOf(
            storyInputPack.audioStyle.voiceDirection,
            storyInputPack.audioStyle.musicMood,
            storyInputPack.audioStyle.soundDesign,
        ).filter { it.isNotBlank() }.joinToString(" | ")
        return LocalAudioCueCard(
            cardId = audioCueCardId(beat.id),
            sourceBeatId = beat.id,
            sourceSceneId = beat.sceneId,
            characterIds = beat.characterIds,
            voiceCueSummary = voiceCueSummary,
            audioIntent = beat.audioIntent,
            audioStyleSeed = audioStyleSeed,
        )
    }

    private fun failureDraft(
        storyInputPack: LocalStoryInputPack,
        storyValidationReport: LocalStoryInputPackValidationReport,
        storyInputPackFile: String,
        storyValidationReportFile: String,
        draftId: String,
        findings: List<LocalAssetPromptCardsDraftFinding>,
    ): LocalAssetPromptCardsDraft =
        LocalAssetPromptCardsDraft(
            status = "FAIL",
            draftId = draftId,
            sourceStoryInputPackFile = storyInputPackFile,
            sourceStoryValidationReportFile = storyValidationReportFile,
            sourceStoryInputPackId = storyInputPack.packId,
            sourceStoryValidationId = storyValidationReport.validationId,
            sourceTitle = storyInputPack.title,
            locale = storyInputPack.locale,
            localOnlyConstraints = copyConstraints(storyInputPack.constraints),
            boundaries = boundaries,
            blockedReason = findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId },
            findingCount = findings.size,
            findings = findings,
            evidence = evidence(storyInputPack, storyValidationReport),
        )

    private fun copyConstraints(constraints: LocalStoryInputConstraints): LocalAssetPromptCardsConstraints =
        LocalAssetPromptCardsConstraints(
            localOnly = constraints.localOnly,
            noExternalPaidProviderTools = constraints.noExternalPaidProviderTools,
            fileProtocolJsonOnly = constraints.fileProtocolJsonOnly,
            notes = constraints.notes,
        )

    private fun evidence(
        storyInputPack: LocalStoryInputPack,
        storyValidationReport: LocalStoryInputPackValidationReport,
        characterCardCount: Int = 0,
        sceneCardCount: Int = 0,
        beatCardCount: Int = 0,
        visualPromptCardCount: Int = 0,
        audioCueCardCount: Int = 0,
    ): LocalAssetPromptCardsDraftEvidence =
        LocalAssetPromptCardsDraftEvidence(
            storyValidationVerdict = storyValidationReport.verdict,
            characterCardCount = characterCardCount,
            sceneCardCount = sceneCardCount,
            beatCardCount = beatCardCount,
            visualPromptCardCount = visualPromptCardCount,
            audioCueCardCount = audioCueCardCount,
            targetDurationMillis = storyInputPack.durationTargetMillis,
            localOnlyDeclared = storyInputPack.constraints.localOnly,
            noExternalPaidProviderToolsDeclared = storyInputPack.constraints.noExternalPaidProviderTools,
            fileProtocolJsonOnlyDeclared = storyInputPack.constraints.fileProtocolJsonOnly,
        )

    private fun characterCardId(sourceCharacterId: String): String =
        "local-character-card-$sourceCharacterId"

    private fun sceneCardId(sourceSceneId: String): String =
        "local-scene-card-$sourceSceneId"

    private fun beatCardId(sourceBeatId: String): String =
        "local-beat-card-$sourceBeatId"

    private fun visualPromptCardId(sourceBeatId: String): String =
        "local-visual-prompt-$sourceBeatId"

    private fun audioCueCardId(sourceBeatId: String): String =
        "local-audio-cue-$sourceBeatId"

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): LocalAssetPromptCardsDraftFinding =
        LocalAssetPromptCardsDraftFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
}
