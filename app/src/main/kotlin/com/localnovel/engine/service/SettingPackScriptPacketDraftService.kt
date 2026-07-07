package com.localnovel.engine.service

import com.localnovel.engine.model.ScriptPacketActionLine
import com.localnovel.engine.model.ScriptPacketAssetNeed
import com.localnovel.engine.model.ScriptPacketBeat
import com.localnovel.engine.model.ScriptPacketDialogueLine
import com.localnovel.engine.model.ScriptPacketDraft
import com.localnovel.engine.model.ScriptPacketFrameCandidate
import com.localnovel.engine.model.ScriptPacketInteractionChoice
import com.localnovel.engine.model.ScriptPacketKnowledgePass
import com.localnovel.engine.model.ScriptPacketLocationZone
import com.localnovel.engine.model.ScriptPacketReviewSummary
import com.localnovel.engine.model.ScriptPacketSceneReference
import com.localnovel.engine.model.ScriptPacketSecrecyKnowledgeNotes
import com.localnovel.engine.model.ScriptPacketSegment
import com.localnovel.engine.model.ScriptPacketSource
import com.localnovel.engine.model.ScriptPacketStoryboardNotes
import com.localnovel.engine.model.ScriptPacketVisualSetup
import com.localnovel.engine.model.SettingPack
import com.localnovel.engine.model.SettingPackEntity
import com.localnovel.engine.model.SettingPackScriptPacketDraftBuildEvidence
import com.localnovel.engine.model.SettingPackScriptPacketDraftBuildFinding

class SettingPackScriptPacketDraftService {
    fun create(
        settingPack: SettingPack,
        settingPackFile: String = "",
        requestedSceneId: String = "",
        packetId: String = "",
    ): SettingPackScriptPacketDraftBuildResult {
        val findings = mutableListOf<SettingPackScriptPacketDraftBuildFinding>()
        val publicScenes = settingPack.entities.scenes.filterPublic()
        val publicLocations = settingPack.entities.locations.filterPublic()
        val publicCharacters = settingPack.entities.characters.filterPublic()
        val publicItems = settingPack.entities.items.filterPublic()
        val publicFacts = settingPack.entities.facts.filterPublic()
        val publicWorldRules = settingPack.entities.worldRules.filterPublic()
        val excludedSecretCount = settingPack.entities.secrets.count { !it.isPublic() || !it.revealed }

        val scene = selectScene(publicScenes, requestedSceneId, findings)
        if (scene == null) {
            return SettingPackScriptPacketDraftBuildResult(
                draft = null,
                selectedSceneId = "",
                selectedLocationId = "",
                findings = findings,
                evidence = evidence(
                    publicScenes = publicScenes,
                    publicLocations = publicLocations,
                    publicCharacters = publicCharacters,
                    publicItems = publicItems,
                    publicFacts = publicFacts,
                    publicWorldRules = publicWorldRules,
                    selectedCharacters = emptyList(),
                    selectedItems = emptyList(),
                    sceneContractCount = 0,
                    excludedSecretCount = excludedSecretCount,
                    generatedBeatCount = 0,
                    generatedFrameCandidateCount = 0,
                ),
            )
        }

        val location = selectLocation(settingPack, publicLocations, scene)
        val characters = selectCharacters(settingPack, publicCharacters, location, scene).ifEmpty {
            publicCharacters.take(1)
        }
        val items = selectItems(settingPack, publicItems, location, characters)
        val sceneContracts = settingPack.entities.sceneContracts.filter {
            it.sceneId == scene.id && !it.isPublic()
        }
        val actorId = characters.firstOrNull()?.id ?: "setting-observer"
        val actorLabel = characters.firstOrNull()?.label() ?: "visible observer"
        val locationLabel = location?.label() ?: "visible setting area"
        val sceneLabel = scene.label()
        val sceneText = scene.bodyText().ifBlank { "The public scene state is visible." }
        val locationText = location?.bodyText().orEmpty()
        val itemLabel = items.firstOrNull()?.label().orEmpty()
        val selectedPacketId = packetId.ifBlank {
            "script-packet-draft-${slug(settingPack.packId)}-${slug(scene.id)}"
        }

        val continuityLocks = buildList {
            add("Scene remains tied to setting pack ${settingPack.packId} and public scene ${scene.id}.")
            add("Visible location stays ${locationLabel}.")
            add("Visible actor focus stays ${actorLabel}.")
            if (itemLabel.isNotBlank()) add("Visible prop focus stays ${itemLabel}.")
        }.distinct()
        val mustShow = buildList {
            add(locationLabel)
            add(sceneLabel)
            add(actorLabel)
            if (itemLabel.isNotBlank()) add(itemLabel)
        }.filter { it.isNotBlank() }.distinct()
        val mustNotShow = buildList {
            add("non-public reveal material")
            add("remote media references")
            if (sceneContracts.isNotEmpty() || excludedSecretCount > 0) {
                add("sealed source-only details")
            }
        }.distinct()
        val publicFactTexts = publicFacts.take(3).map { it.bodyText() }.filter { it.isNotBlank() }
        val worldRuleTexts = publicWorldRules.take(2).map { it.bodyText() }.filter { it.isNotBlank() }
        val characterKnowledge = buildList {
            characters.take(2).forEach { character ->
                add("${character.id} can act only from visible public setting information in ${locationLabel}.")
            }
            if (characters.isEmpty()) {
                add("The scene can use only visible public setting information in ${locationLabel}.")
            }
        }

        val beats = settingBeats(scene, locationLabel, sceneLabel, actorLabel, itemLabel, characters)
        val zoneId = location?.id?.let { "zone-${slug(it)}" } ?: "zone-${slug(scene.id)}"
        val draft = ScriptPacketDraft(
            packetId = selectedPacketId,
            source = ScriptPacketSource(
                inputType = "settingPack",
                sourceId = "${settingPack.packId}:${scene.id}",
                sourceTitle = "${settingPack.name} / ${sceneLabel}",
                excerptSummary = listOf(
                    "Setting pack local preview source for ${sceneLabel}.",
                    sceneText,
                    locationText,
                ).filter { it.isNotBlank() }.joinToString(" "),
                transformationStatus = "converted_draft",
            ),
            segments = listOf(
                ScriptPacketSegment(
                    segmentId = "segment-${slug(scene.id)}",
                    sceneReference = ScriptPacketSceneReference(
                        type = "sceneCandidate",
                        candidateId = scene.id,
                        notes = "Derived from setting pack JSON for local preview draft review.",
                    ),
                    scenePurpose = "Present the public scene state at ${locationLabel} and the immediate pressure around ${sceneLabel}.",
                    playerObjective = "Observe the visible constraints before choosing a cautious next action.",
                    locationZone = ScriptPacketLocationZone(
                        zoneId = zoneId,
                        publicName = locationLabel,
                        constraints = locationText.ifBlank { "Only public setting information is available to this draft." },
                    ),
                    visualSetup = ScriptPacketVisualSetup(
                        publicDescription = buildPublicDescription(sceneText, locationText, actorLabel, itemLabel),
                        cameraIntent = "Use readable comic framing that keeps the location, visible actor, and main constraint in one panel.",
                        mustShow = mustShow,
                        mustNotShow = mustNotShow,
                    ),
                    knowledgePass = ScriptPacketKnowledgePass(
                        publicFacts = (publicFactTexts + worldRuleTexts).ifEmpty {
                            listOf("Only public setting details are available for this draft.")
                        },
                        characterKnowledgeClaims = characterKnowledge,
                        authorOnlyExtracted = emptyList(),
                    ),
                    beatList = beats,
                    actionLines = beats.mapIndexed { index, beat ->
                        ScriptPacketActionLine(
                            lineId = "action-${index + 1}",
                            actorId = actorId,
                            line = beat.visibleAction,
                            blockingZone = zoneId,
                            visibleCost = "The action stays within public visible constraints from the setting pack.",
                        )
                    },
                    dialogueLines = listOf(
                        ScriptPacketDialogueLine(
                            lineId = "dialogue-${slug(scene.id)}-public",
                            speakerId = actorId,
                            line = "We can only act on what is visible here.",
                            knowledgeBoundary = "The line is limited to public visible setting information.",
                            subtext = "Caution without exposing non-public source material.",
                        ),
                    ),
                    interactionChoices = listOf(
                        ScriptPacketInteractionChoice(
                            choiceId = "choice-observe-${slug(scene.id)}",
                            label = "Observe the visible constraint.",
                            allowedBecause = "The visible location, actor focus, and public facts are already present in the scene.",
                            likelyStateEffect = "The next beat can preserve continuity while keeping non-public material outside the visible draft.",
                        ),
                    ),
                ),
            ),
            assetNeeds = buildAssetNeeds(location, characters, items),
            storyboardNotes = ScriptPacketStoryboardNotes(
                cameraLanguage = "Keep each frame readable for later comic-video panel planning.",
                continuityLocks = continuityLocks,
                frameCandidates = beats.mapIndexed { index, beat ->
                    ScriptPacketFrameCandidate(
                        frameId = "frame-${slug(scene.id)}-${(index + 1).toString().padStart(2, '0')}",
                        beatId = beat.beatId,
                        framePurpose = beat.outcome,
                        cameraPurpose = "Make the beat readable as a single comic panel.",
                        visibleSubject = mustShow.joinToString(", "),
                        doNotShow = mustNotShow,
                    )
                },
            ),
            secrecyKnowledgeNotes = ScriptPacketSecrecyKnowledgeNotes(
                authorOnlyExcluded = true,
                reviewOnlyNotes = "Non-public source material was excluded from visible fields.",
            ),
            reviewSummary = ScriptPacketReviewSummary(
                verdict = "unreviewed",
                reviewerNotes = "Deterministic setting-pack draft for local review gates.",
            ),
        )

        return SettingPackScriptPacketDraftBuildResult(
            draft = draft,
            selectedSceneId = scene.id,
            selectedLocationId = location?.id.orEmpty(),
            findings = findings,
            evidence = evidence(
                publicScenes = publicScenes,
                publicLocations = publicLocations,
                publicCharacters = publicCharacters,
                publicItems = publicItems,
                publicFacts = publicFacts,
                publicWorldRules = publicWorldRules,
                selectedCharacters = characters,
                selectedItems = items,
                sceneContractCount = sceneContracts.size,
                excludedSecretCount = excludedSecretCount,
                generatedBeatCount = beats.size,
                generatedFrameCandidateCount = beats.size,
            ),
        )
    }

    private fun settingBeats(
        scene: SettingPackEntity,
        locationLabel: String,
        sceneLabel: String,
        actorLabel: String,
        itemLabel: String,
        characters: List<SettingPackEntity>,
    ): List<ScriptPacketBeat> =
        listOf(
            ScriptPacketBeat(
                beatId = "beat-${slug(scene.id)}-establish",
                visibleAction = "The frame establishes ${locationLabel} while ${actorLabel} remains within the visible scene boundary.",
                causalLink = "The setting source names ${sceneLabel} as the public scene focus.",
                characterIds = characters.map { it.id },
                requiredKnowledgeClaimIds = listOf("public-setting-visible-state"),
                outcome = "The viewer can read the location, actor focus, and scene pressure before any choice is implied.",
            ),
            ScriptPacketBeat(
                beatId = "beat-${slug(scene.id)}-constraint",
                visibleAction = if (itemLabel.isNotBlank()) {
                    "${actorLabel} stays near ${itemLabel}, keeping the visible constraint readable."
                } else {
                    "${actorLabel} checks the visible surroundings without exposing non-public material."
                },
                causalLink = "The generated draft carries only public facts and world rules from the setting pack.",
                characterIds = characters.map { it.id },
                requiredKnowledgeClaimIds = listOf("public-setting-visible-state"),
                outcome = "The scene pressure is concrete enough for storyboard framing.",
            ),
            ScriptPacketBeat(
                beatId = "beat-${slug(scene.id)}-choice",
                visibleAction = "${actorLabel} pauses, leaving room for a cautious next action inside ${locationLabel}.",
                causalLink = "A script packet draft must preserve player-facing agency instead of forcing an unearned answer.",
                characterIds = characters.map { it.id },
                requiredKnowledgeClaimIds = listOf("public-setting-visible-state"),
                outcome = "The draft can proceed to review as a candidate scene packet.",
            ),
        )

    private fun selectScene(
        publicScenes: List<SettingPackEntity>,
        requestedSceneId: String,
        findings: MutableList<SettingPackScriptPacketDraftBuildFinding>,
    ): SettingPackEntity? {
        if (requestedSceneId.isBlank()) {
            val scene = publicScenes.firstOrNull()
            if (scene == null) {
                findings += finding(
                    "PUBLIC_SCENE_MISSING",
                    "setting_pack",
                    "CRITICAL",
                    "$.entities.scenes",
                    "Setting-pack script packet draft creation requires at least one public scene.",
                )
            }
            return scene
        }
        val scene = publicScenes.firstOrNull { it.id == requestedSceneId }
        if (scene == null) {
            findings += finding(
                "REQUESTED_PUBLIC_SCENE_NOT_FOUND",
                "setting_pack",
                "CRITICAL",
                "$.entities.scenes",
                "Requested scene id was not found among public setting pack scenes.",
                evidence = listOf(requestedSceneId),
            )
        }
        return scene
    }

    private fun selectLocation(
        settingPack: SettingPack,
        publicLocations: List<SettingPackEntity>,
        scene: SettingPackEntity,
    ): SettingPackEntity? {
        val direct = scene.locationId?.takeIf { it.isNotBlank() }
        val relation = settingPack.relations.firstOrNull {
            it.visibility.isPublicVisibility() &&
                it.fromType.equals("scene", ignoreCase = true) &&
                it.fromId == scene.id &&
                it.toType.equals("location", ignoreCase = true)
        }?.toId
        val locationId = direct ?: relation
        return publicLocations.firstOrNull { it.id == locationId } ?: publicLocations.firstOrNull()
    }

    private fun selectCharacters(
        settingPack: SettingPack,
        publicCharacters: List<SettingPackEntity>,
        location: SettingPackEntity?,
        scene: SettingPackEntity,
    ): List<SettingPackEntity> {
        val ids = mutableSetOf<String>()
        val locationId = location?.id
        settingPack.relations.filter { it.visibility.isPublicVisibility() }.forEach { relation ->
            if (relation.fromType.equals("character", ignoreCase = true) &&
                relation.toType.equals("location", ignoreCase = true) &&
                relation.toId == locationId
            ) {
                ids += relation.fromId
            }
            if (relation.fromType.equals("character", ignoreCase = true) &&
                relation.toType.equals("scene", ignoreCase = true) &&
                relation.toId == scene.id
            ) {
                ids += relation.fromId
            }
        }
        return publicCharacters.filter { it.id in ids }.take(3)
    }

    private fun selectItems(
        settingPack: SettingPack,
        publicItems: List<SettingPackEntity>,
        location: SettingPackEntity?,
        characters: List<SettingPackEntity>,
    ): List<SettingPackEntity> {
        val characterIds = characters.map { it.id }.toSet()
        val locationId = location?.id
        val relationIds = settingPack.relations.filter { it.visibility.isPublicVisibility() }.mapNotNull { relation ->
            when {
                relation.fromType.equals("item", ignoreCase = true) &&
                    relation.toType.equals("location", ignoreCase = true) &&
                    relation.toId == locationId -> relation.fromId
                relation.fromType.equals("character", ignoreCase = true) &&
                    relation.fromId in characterIds &&
                    relation.toType.equals("item", ignoreCase = true) -> relation.toId
                else -> null
            }
        }.toSet()
        return publicItems.filter { item ->
            item.id in relationIds ||
                item.locationId == locationId ||
                item.initialLocationId == locationId ||
                item.initialOwnerId in characterIds ||
                item.ownerCharacterId in characterIds
        }.take(3)
    }

    private fun buildPublicDescription(
        sceneText: String,
        locationText: String,
        actorLabel: String,
        itemLabel: String,
    ): String {
        val parts = mutableListOf<String>()
        parts += sceneText
        if (locationText.isNotBlank()) parts += locationText
        parts += "${actorLabel} is visible in the frame."
        if (itemLabel.isNotBlank()) parts += "${itemLabel} remains readable as a local prop."
        return parts.joinToString(" ")
    }

    private fun buildAssetNeeds(
        location: SettingPackEntity?,
        characters: List<SettingPackEntity>,
        items: List<SettingPackEntity>,
    ): List<ScriptPacketAssetNeed> {
        val needs = mutableListOf<ScriptPacketAssetNeed>()
        location?.let {
            needs += ScriptPacketAssetNeed(
                assetType = "location",
                candidateName = it.label(),
                candidateReason = "Needed to frame the setting-derived scene location.",
                lockStatus = "candidate",
            )
        }
        characters.take(2).forEach {
            needs += ScriptPacketAssetNeed(
                assetType = "character",
                candidateName = it.label(),
                candidateReason = "Needed as visible actor focus for the setting-derived scene.",
                lockStatus = "candidate",
            )
        }
        items.take(2).forEach {
            needs += ScriptPacketAssetNeed(
                assetType = "prop",
                candidateName = it.label(),
                candidateReason = "Needed as visible prop or constraint from the setting pack.",
                lockStatus = "candidate",
            )
        }
        return needs.distinctBy { "${it.assetType}:${it.candidateName}" }
    }

    private fun evidence(
        publicScenes: List<SettingPackEntity>,
        publicLocations: List<SettingPackEntity>,
        publicCharacters: List<SettingPackEntity>,
        publicItems: List<SettingPackEntity>,
        publicFacts: List<SettingPackEntity>,
        publicWorldRules: List<SettingPackEntity>,
        selectedCharacters: List<SettingPackEntity>,
        selectedItems: List<SettingPackEntity>,
        sceneContractCount: Int,
        excludedSecretCount: Int,
        generatedBeatCount: Int,
        generatedFrameCandidateCount: Int,
    ): SettingPackScriptPacketDraftBuildEvidence =
        SettingPackScriptPacketDraftBuildEvidence(
            publicSceneCount = publicScenes.size,
            publicLocationCount = publicLocations.size,
            publicCharacterCount = publicCharacters.size,
            publicItemCount = publicItems.size,
            publicFactCount = publicFacts.size,
            publicWorldRuleCount = publicWorldRules.size,
            selectedCharacterCount = selectedCharacters.size,
            selectedItemCount = selectedItems.size,
            sceneContractCount = sceneContractCount,
            excludedSecretCount = excludedSecretCount,
            generatedSegmentCount = if (generatedBeatCount > 0) 1 else 0,
            generatedBeatCount = generatedBeatCount,
            generatedFrameCandidateCount = generatedFrameCandidateCount,
        )

    private fun List<SettingPackEntity>.filterPublic(): List<SettingPackEntity> =
        filter { it.isPublic() }

    private fun SettingPackEntity.isPublic(): Boolean =
        visibility.isPublicVisibility()

    private fun String.isPublicVisibility(): Boolean =
        isBlank() || equals("public", ignoreCase = true)

    private fun SettingPackEntity.label(): String =
        listOf(name, title, id).firstOrNull { it.isNotBlank() }.orEmpty()

    private fun SettingPackEntity.bodyText(): String =
        listOf(summary, description, content, publicSummary, statement, title, name).firstOrNull { it.isNotBlank() }.orEmpty()

    private fun slug(value: String): String =
        value.lowercase()
            .replace(Regex("""[^a-z0-9]+"""), "-")
            .trim('-')
            .ifBlank { "setting" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): SettingPackScriptPacketDraftBuildFinding =
        SettingPackScriptPacketDraftBuildFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
}

data class SettingPackScriptPacketDraftBuildResult(
    val draft: ScriptPacketDraft?,
    val selectedSceneId: String,
    val selectedLocationId: String,
    val findings: List<SettingPackScriptPacketDraftBuildFinding>,
    val evidence: SettingPackScriptPacketDraftBuildEvidence,
)
