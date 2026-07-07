package com.localnovel.engine.service

import com.localnovel.engine.model.LocalStoryInputAudioStyle
import com.localnovel.engine.model.LocalStoryInputBeat
import com.localnovel.engine.model.LocalStoryInputCharacter
import com.localnovel.engine.model.LocalStoryInputConstraints
import com.localnovel.engine.model.LocalStoryInputPack
import com.localnovel.engine.model.LocalStoryInputPackValidationEvidence
import com.localnovel.engine.model.LocalStoryInputPackValidationFinding
import com.localnovel.engine.model.LocalStoryInputPackValidationReport
import com.localnovel.engine.model.LocalStoryInputScene
import com.localnovel.engine.model.LocalStoryInputVisualStyle

class LocalStoryInputPackValidationService {
    private val maxLocalDemoDurationMillis = 60_000
    private val nextAllowedDownstreamStep = "local_story_input_pack_validated_for_review"
    private val forbiddenBoundaryClaimPatterns = listOf(
        Regex("""(?i)\bfinal\s+media\s+approval\b"""),
        Regex("""(?i)\bfinal\s+(video|media|audio|image)\b"""),
        Regex("""(?i)\brelease(?:d)?\b"""),
        Regex("""(?i)\bcanon(?:ical)?\b"""),
        Regex("""(?i)\bregistry\b|\bregistered\b"""),
        Regex("""(?i)\bproduction[-_\s]?lock(?:ed)?\b"""),
        Regex("\u6700\u7d42\u5a92\u9ad4\u6838\u51c6|\u6b63\u5f0f\u91cb\u51fa|\u6b63\u5178|\u767b\u9304|\u767b\u8a18|\u751f\u7522\u9396\u5b9a"),
    )
    private val downstreamExecutionClaimPatterns = listOf(
        Regex("""(?i)\bprovider\s+job\b"""),
        Regex("""(?i)\bapi\s+(response|result)\b"""),
        Regex("""(?i)\bsubmitted\s+prompt\b|\bprompt\s+submitted\b"""),
        Regex("""(?i)\bgenerated\s+(video|audio|image)\b|\b(video|audio|image)\s+generated\b"""),
    )

    fun validate(
        pack: LocalStoryInputPack,
        packFile: String = "",
        validationId: String = "",
    ): LocalStoryInputPackValidationReport {
        val findings = mutableListOf<LocalStoryInputPackValidationFinding>()
        validateMetadata(pack, findings)
        val characterIds = validateCharacters(pack.characters, findings)
        val sceneIds = validateScenes(pack.scenes, findings)
        val beatValidation = validateBeats(pack.beats, sceneIds, characterIds, findings)
        validateDuration(pack.durationTargetMillis, findings)
        validateConstraints(pack.constraints, findings)
        val forbiddenBoundaryClaimCount = validateBoundaryText(pack, findings)

        val failureFindings = findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }
        val verdict = if (failureFindings.isEmpty()) "PASS" else "FAIL"
        val resolvedValidationId = validationId.ifBlank {
            pack.packId.takeIf { it.isNotBlank() }?.let { "$it-validation-v0" }.orEmpty()
        }
        return LocalStoryInputPackValidationReport(
            verdict = verdict,
            validationId = resolvedValidationId,
            storyInputPackFile = packFile,
            packId = pack.packId,
            title = pack.title,
            locale = pack.locale,
            nextAllowedDownstreamStep = if (verdict == "PASS") nextAllowedDownstreamStep else "",
            blockedReason = if (verdict == "FAIL") failureFindings.joinToString("; ") { it.ruleId } else "",
            findingCount = findings.size,
            findings = findings,
            evidence = LocalStoryInputPackValidationEvidence(
                characterCount = pack.characters.size,
                sceneCount = pack.scenes.size,
                beatCount = pack.beats.size,
                linkedBeatCount = beatValidation.linkedBeatCount,
                durationTargetMillis = pack.durationTargetMillis,
                localOnlyDeclared = pack.constraints.localOnly,
                noExternalPaidProviderToolsDeclared = pack.constraints.noExternalPaidProviderTools,
                fileProtocolJsonOnlyDeclared = pack.constraints.fileProtocolJsonOnly,
                forbiddenBoundaryClaimCount = forbiddenBoundaryClaimCount,
                crossReferenceMismatchCount = beatValidation.crossReferenceMismatchCount,
            ),
        )
    }

    private fun validateMetadata(
        pack: LocalStoryInputPack,
        findings: MutableList<LocalStoryInputPackValidationFinding>,
    ) {
        requireField(pack.packId, "$.packId", "LOCAL_STORY_PACK_ID_MISSING", "pack id", findings)
        requireField(pack.title, "$.title", "LOCAL_STORY_TITLE_MISSING", "title", findings)
        requireField(pack.locale, "$.locale", "LOCAL_STORY_LOCALE_MISSING", "locale", findings)
    }

    private fun validateCharacters(
        characters: List<LocalStoryInputCharacter>,
        findings: MutableList<LocalStoryInputPackValidationFinding>,
    ): Set<String> {
        if (characters.isEmpty()) {
            findings += finding("LOCAL_STORY_CHARACTERS_MISSING", "structure", "CRITICAL", "$.characters", "Local story input pack requires at least one character.")
            return emptySet()
        }
        val ids = characters.map { it.id.trim() }.filter { it.isNotBlank() }
        duplicateValues(ids).forEach { duplicate ->
            findings += finding("LOCAL_STORY_CHARACTER_IDS_NOT_UNIQUE", "structure", "CRITICAL", "$.characters", "Character ids must be unique.", listOf(duplicate))
        }
        characters.forEachIndexed { index, character ->
            val path = "$.characters[$index]"
            requireField(character.id, "$path.id", "LOCAL_STORY_CHARACTER_ID_MISSING", "character id", findings)
            requireField(character.name, "$path.name", "LOCAL_STORY_CHARACTER_NAME_MISSING", "character name", findings)
            requireField(character.role, "$path.role", "LOCAL_STORY_CHARACTER_ROLE_MISSING", "character role", findings)
            requireField(character.visualCue, "$path.visualCue", "LOCAL_STORY_CHARACTER_VISUAL_CUE_MISSING", "character visual cue", findings)
            requireField(character.voiceCue, "$path.voiceCue", "LOCAL_STORY_CHARACTER_VOICE_CUE_MISSING", "character voice cue", findings)
        }
        return ids.toSet()
    }

    private fun validateScenes(
        scenes: List<LocalStoryInputScene>,
        findings: MutableList<LocalStoryInputPackValidationFinding>,
    ): Set<String> {
        if (scenes.isEmpty()) {
            findings += finding("LOCAL_STORY_SCENES_MISSING", "structure", "CRITICAL", "$.scenes", "Local story input pack requires at least one scene.")
            return emptySet()
        }
        val ids = scenes.map { it.id.trim() }.filter { it.isNotBlank() }
        duplicateValues(ids).forEach { duplicate ->
            findings += finding("LOCAL_STORY_SCENE_IDS_NOT_UNIQUE", "structure", "CRITICAL", "$.scenes", "Scene ids must be unique.", listOf(duplicate))
        }
        scenes.forEachIndexed { index, scene ->
            val path = "$.scenes[$index]"
            requireField(scene.id, "$path.id", "LOCAL_STORY_SCENE_ID_MISSING", "scene id", findings)
            requireField(scene.title, "$path.title", "LOCAL_STORY_SCENE_TITLE_MISSING", "scene title", findings)
            requireField(scene.location, "$path.location", "LOCAL_STORY_SCENE_LOCATION_MISSING", "scene location", findings)
            requireField(scene.mood, "$path.mood", "LOCAL_STORY_SCENE_MOOD_MISSING", "scene mood", findings)
        }
        return ids.toSet()
    }

    private fun validateBeats(
        beats: List<LocalStoryInputBeat>,
        sceneIds: Set<String>,
        characterIds: Set<String>,
        findings: MutableList<LocalStoryInputPackValidationFinding>,
    ): BeatValidation {
        if (beats.isEmpty()) {
            findings += finding("LOCAL_STORY_BEATS_MISSING", "structure", "CRITICAL", "$.beats", "Local story input pack requires at least one beat.")
            return BeatValidation()
        }
        var linkedBeatCount = 0
        var crossReferenceMismatchCount = 0
        val ids = beats.map { it.id.trim() }.filter { it.isNotBlank() }
        duplicateValues(ids).forEach { duplicate ->
            findings += finding("LOCAL_STORY_BEAT_IDS_NOT_UNIQUE", "structure", "CRITICAL", "$.beats", "Beat ids must be unique.", listOf(duplicate))
        }
        beats.forEachIndexed { index, beat ->
            val path = "$.beats[$index]"
            requireField(beat.id, "$path.id", "LOCAL_STORY_BEAT_ID_MISSING", "beat id", findings)
            requireField(beat.sceneId, "$path.sceneId", "LOCAL_STORY_BEAT_SCENE_ID_MISSING", "beat scene id", findings)
            requireField(beat.narrativeIntent, "$path.narrativeIntent", "LOCAL_STORY_BEAT_NARRATIVE_INTENT_MISSING", "beat narrative intent", findings)
            requireField(beat.dialogueIntent, "$path.dialogueIntent", "LOCAL_STORY_BEAT_DIALOGUE_INTENT_MISSING", "beat dialogue intent", findings)
            if (beat.sceneId.isNotBlank() && beat.sceneId !in sceneIds) {
                crossReferenceMismatchCount += 1
                findings += finding("LOCAL_STORY_BEAT_SCENE_REF_MISSING", "cross_reference", "CRITICAL", "$path.sceneId", "Beat sceneId must reference an existing scene.", listOf(beat.sceneId))
            } else if (beat.sceneId.isNotBlank()) {
                linkedBeatCount += 1
            }
            beat.characterIds.filter { it.isNotBlank() }.forEach { characterId ->
                if (characterId !in characterIds) {
                    crossReferenceMismatchCount += 1
                    findings += finding("LOCAL_STORY_BEAT_CHARACTER_REF_MISSING", "cross_reference", "CRITICAL", "$path.characterIds", "Beat characterIds must reference existing characters.", listOf(characterId))
                }
            }
        }
        return BeatValidation(
            linkedBeatCount = linkedBeatCount,
            crossReferenceMismatchCount = crossReferenceMismatchCount,
        )
    }

    private fun validateDuration(
        durationTargetMillis: Int,
        findings: MutableList<LocalStoryInputPackValidationFinding>,
    ) {
        if (durationTargetMillis <= 0) {
            findings += finding("LOCAL_STORY_DURATION_TARGET_INVALID", "timing", "CRITICAL", "$.durationTargetMillis", "Duration target must be positive.", listOf(durationTargetMillis.toString()))
        }
        if (durationTargetMillis > maxLocalDemoDurationMillis) {
            findings += finding("LOCAL_STORY_DURATION_TARGET_TOO_LONG", "timing", "CRITICAL", "$.durationTargetMillis", "Duration target must stay within the local demo bound of $maxLocalDemoDurationMillis ms.", listOf(durationTargetMillis.toString()))
        }
    }

    private fun validateConstraints(
        constraints: LocalStoryInputConstraints,
        findings: MutableList<LocalStoryInputPackValidationFinding>,
    ) {
        if (!constraints.localOnly) {
            findings += finding("LOCAL_STORY_LOCAL_ONLY_NOT_DECLARED", "boundary", "CRITICAL", "$.constraints.localOnly", "Local story input pack must explicitly declare local-only handling.")
        }
        if (!constraints.noExternalPaidProviderTools) {
            findings += finding("LOCAL_STORY_NO_EXTERNAL_TOOLS_NOT_DECLARED", "boundary", "CRITICAL", "$.constraints.noExternalPaidProviderTools", "Local story input pack must explicitly declare no external paid/provider tools.")
        }
        if (!constraints.fileProtocolJsonOnly) {
            findings += finding("LOCAL_STORY_FILE_PROTOCOL_JSON_NOT_DECLARED", "boundary", "CRITICAL", "$.constraints.fileProtocolJsonOnly", "Local story input pack must remain file-protocol JSON only.")
        }
        if (constraints.notes.none { it.isNotBlank() }) {
            findings += finding("LOCAL_STORY_CONSTRAINT_NOTES_MISSING", "boundary", "MAJOR", "$.constraints.notes", "Local story input pack should include explicit local-only constraint notes.")
        }
    }

    private fun validateBoundaryText(
        pack: LocalStoryInputPack,
        findings: MutableList<LocalStoryInputPackValidationFinding>,
    ): Int {
        var boundaryClaimCount = 0
        collectText(pack).filter { it.value.isNotBlank() }.forEach { field ->
            forbiddenBoundaryClaimPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    boundaryClaimCount += 1
                    findings += finding("LOCAL_STORY_FORBIDDEN_BOUNDARY_CLAIM", "boundary", "CRITICAL", field.path, "Local story input pack cannot claim final media approval, release, canon, registry, or production lock.", listOf(field.value), "Keep the pack as local input only.")
                }
            }
            downstreamExecutionClaimPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    boundaryClaimCount += 1
                    findings += finding("LOCAL_STORY_DOWNSTREAM_EXECUTION_CLAIM", "boundary", "CRITICAL", field.path, "Local story input pack cannot claim provider/API execution, prompt submission, or generated media.", listOf(field.value), "Keep execution claims out of the input pack.")
                }
            }
        }
        return boundaryClaimCount
    }

    private fun collectText(pack: LocalStoryInputPack): List<TextField> {
        val fields = mutableListOf(
            TextField("$.packId", pack.packId),
            TextField("$.title", pack.title),
            TextField("$.locale", pack.locale),
            TextField("$.worldPremise", pack.worldPremise),
        )
        pack.characters.forEachIndexed { index, character ->
            val path = "$.characters[$index]"
            fields += collectCharacterText(path, character)
        }
        pack.scenes.forEachIndexed { index, scene ->
            val path = "$.scenes[$index]"
            fields += collectSceneText(path, scene)
        }
        pack.beats.forEachIndexed { index, beat ->
            val path = "$.beats[$index]"
            fields += collectBeatText(path, beat)
        }
        fields += collectVisualStyleText("$.visualStyle", pack.visualStyle)
        fields += collectAudioStyleText("$.audioStyle", pack.audioStyle)
        pack.constraints.notes.forEachIndexed { index, note ->
            fields += TextField("$.constraints.notes[$index]", note)
        }
        return fields
    }

    private fun collectCharacterText(path: String, character: LocalStoryInputCharacter): List<TextField> =
        listOf(
            TextField("$path.id", character.id),
            TextField("$path.name", character.name),
            TextField("$path.role", character.role),
            TextField("$path.visualCue", character.visualCue),
            TextField("$path.voiceCue", character.voiceCue),
            TextField("$path.publicMotivation", character.publicMotivation),
        )

    private fun collectSceneText(path: String, scene: LocalStoryInputScene): List<TextField> =
        listOf(
            TextField("$path.id", scene.id),
            TextField("$path.title", scene.title),
            TextField("$path.location", scene.location),
            TextField("$path.mood", scene.mood),
            TextField("$path.visibleSituation", scene.visibleSituation),
        )

    private fun collectBeatText(path: String, beat: LocalStoryInputBeat): List<TextField> {
        val fields = mutableListOf(
            TextField("$path.id", beat.id),
            TextField("$path.sceneId", beat.sceneId),
            TextField("$path.narrativeIntent", beat.narrativeIntent),
            TextField("$path.dialogueIntent", beat.dialogueIntent),
            TextField("$path.visualIntent", beat.visualIntent),
            TextField("$path.audioIntent", beat.audioIntent),
        )
        beat.characterIds.forEachIndexed { index, characterId ->
            fields += TextField("$path.characterIds[$index]", characterId)
        }
        return fields
    }

    private fun collectVisualStyleText(path: String, visualStyle: LocalStoryInputVisualStyle): List<TextField> =
        listOf(
            TextField("$path.tone", visualStyle.tone),
            TextField("$path.colorPalette", visualStyle.colorPalette),
            TextField("$path.cameraNotes", visualStyle.cameraNotes),
            TextField("$path.subtitleStyle", visualStyle.subtitleStyle),
        )

    private fun collectAudioStyleText(path: String, audioStyle: LocalStoryInputAudioStyle): List<TextField> =
        listOf(
            TextField("$path.voiceDirection", audioStyle.voiceDirection),
            TextField("$path.musicMood", audioStyle.musicMood),
            TextField("$path.soundDesign", audioStyle.soundDesign),
        )

    private fun requireField(
        value: String,
        path: String,
        ruleId: String,
        label: String,
        findings: MutableList<LocalStoryInputPackValidationFinding>,
    ) {
        if (value.isBlank()) {
            findings += finding(ruleId, "structure", "CRITICAL", path, "Local story input pack requires nonblank $label.")
        }
    }

    private fun duplicateValues(values: List<String>): List<String> =
        values.groupingBy { it }.eachCount().filter { it.key.isNotBlank() && it.value > 1 }.keys.toList()

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): LocalStoryInputPackValidationFinding =
        LocalStoryInputPackValidationFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private data class BeatValidation(
        val linkedBeatCount: Int = 0,
        val crossReferenceMismatchCount: Int = 0,
    )

    private data class TextField(val path: String, val value: String)
}
