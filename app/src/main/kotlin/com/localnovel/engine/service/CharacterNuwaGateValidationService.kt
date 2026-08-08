package com.localnovel.engine.service

import com.localnovel.engine.model.CharacterNuwaCard
import com.localnovel.engine.model.CharacterNuwaGateEvidence
import com.localnovel.engine.model.CharacterNuwaGateFile
import com.localnovel.engine.model.CharacterNuwaGateFinding
import com.localnovel.engine.model.CharacterNuwaGateValidationReport
import com.localnovel.engine.model.CharacterNuwaRequiredSpeaker

class CharacterNuwaGateValidationService {
    private val allowedDepths = setOf("full_nuwa", "light_voice")
    private val allowedStatuses = setOf("draft", "ready", "blocked")
    private val hardBannedStructures = listOf(
        BannedStructure("CALIBRATION_ZH_THIS_NOT_THIS_IS_COMPACT", Regex("""這不是\S+\s*[，, ]\s*這是\S+""")),
        BannedStructure("CALIBRATION_ZH_THIS_NOT_THIS_IS_SPACED", Regex("""這不是\s+.+?[，, ]\s*這是\s+.+""")),
        BannedStructure("CALIBRATION_ZH_NOT_BUT_COMPACT", Regex("""不是\S+\s*[，, ]?\s*而是\S+""")),
        BannedStructure("CALIBRATION_ZH_NOT_BUT_SPACED", Regex("""不是\s+.+?[，, ]?\s*而是\s+.+""")),
        BannedStructure("CALIBRATION_EN_NOT_BUT", Regex("""(?i)\bnot\s+.+?\s*,?\s+but\s+.+""")),
        BannedStructure("CALIBRATION_EN_NOT_ONLY", Regex("""(?i)\bnot\s+.+?\s*,?\s+only\s+.+""")),
    )

    fun validate(gate: CharacterNuwaGateFile, gateFile: String = ""): CharacterNuwaGateValidationReport {
        val findings = mutableListOf<CharacterNuwaGateFinding>()

        requireNonBlank(findings, gate.gateId, "$.gateId", "GATE_ID_MISSING", "WARNING", "Character Nuwa gate file should carry a stable gateId.")
        if (gate.requiredSpeakingCharacters.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "REQUIRED_SPEAKERS_EMPTY",
                    category = "structure",
                    severity = "MAJOR",
                    path = "$.requiredSpeakingCharacters",
                    message = "The gate file does not list any speaking characters.",
                    suggestedFix = "List every character who may speak in this scene or chapter before drafting dialogue.",
                ),
            )
        }
        if (gate.characterCards.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "CHARACTER_CARDS_EMPTY",
                    category = "structure",
                    severity = "CRITICAL",
                    path = "$.characterCards",
                    message = "No Character Nuwa cards are present.",
                    suggestedFix = "Create full_nuwa cards for important speakers and light_voice cards for one-off background speakers.",
                ),
            )
        }

        val cardsByCharacter = gate.characterCards
            .mapIndexed { index, card -> index to card }
            .groupBy { it.second.characterId.trim() }

        cardsByCharacter
            .filterKeys { it.isNotBlank() }
            .filterValues { it.size > 1 }
            .forEach { (characterId, entries) ->
                findings.add(
                    finding(
                        ruleId = "CHARACTER_CARD_DUPLICATE",
                        category = "structure",
                        severity = "MAJOR",
                        path = "$.characterCards",
                        message = "A character has more than one card in the same gate file.",
                        evidence = listOf(characterId) + entries.map { "index=${it.first}" },
                        suggestedFix = "Keep one current card per character or split variants into a later approved format.",
                    ),
                )
            }

        gate.requiredSpeakingCharacters.forEachIndexed { index, speaker ->
            validateRequiredSpeaker(speaker, "$.requiredSpeakingCharacters[$index]", findings)
            val characterId = speaker.characterId.trim()
            if (characterId.isBlank()) return@forEachIndexed
            val cardEntry = cardsByCharacter[characterId]?.firstOrNull()
            if (cardEntry == null) {
                findings.add(
                    finding(
                        ruleId = "REQUIRED_SPEAKER_CARD_MISSING",
                        category = "gate",
                        severity = "CRITICAL",
                        path = "$.requiredSpeakingCharacters[$index]",
                        message = "A required speaking character has no Character Nuwa card.",
                        evidence = listOf(characterId, speaker.displayName),
                        suggestedFix = "Add a full_nuwa card for important speakers or an approved light_voice card for one-off speakers.",
                    ),
                )
            } else {
                validateCardAgainstRequirement(
                    card = cardEntry.second,
                    cardPath = "$.characterCards[${cardEntry.first}]",
                    speaker = speaker,
                    speakerPath = "$.requiredSpeakingCharacters[$index]",
                    findings = findings,
                )
            }
        }

        gate.characterCards.forEachIndexed { index, card ->
            validateCard(card, "$.characterCards[$index]", findings)
        }

        val bannedCalibrationHitCount = findings.count { it.ruleId.startsWith("BANNED_CALIBRATION_STRUCTURE") }
        val verdict = when {
            findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
            findings.any { it.severity == "WARNING" } -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }

        return CharacterNuwaGateValidationReport(
            verdict = verdict,
            gateFile = gateFile,
            gateId = gate.gateId,
            requiredSpeakingCharacterCount = gate.requiredSpeakingCharacters.size,
            cardCount = gate.characterCards.size,
            findingCount = findings.size,
            findings = findings,
            evidence = CharacterNuwaGateEvidence(
                requiredFullCardCount = gate.requiredSpeakingCharacters.count { it.requiredDepth.normalized() == "full_nuwa" },
                requiredLightCardCount = gate.requiredSpeakingCharacters.count { it.requiredDepth.normalized() == "light_voice" },
                readyCardCount = gate.characterCards.count { it.gateStatus.normalized() == "ready" },
                bannedCalibrationHitCount = bannedCalibrationHitCount,
            ),
        )
    }

    private fun validateRequiredSpeaker(
        speaker: CharacterNuwaRequiredSpeaker,
        path: String,
        findings: MutableList<CharacterNuwaGateFinding>,
    ) {
        requireNonBlank(findings, speaker.characterId, "$path.characterId", "REQUIRED_SPEAKER_ID_MISSING", "MAJOR", "Required speaker is missing characterId.")
        requireNonBlank(findings, speaker.displayName, "$path.displayName", "REQUIRED_SPEAKER_NAME_MISSING", "WARNING", "Required speaker is missing displayName.")
        val requiredDepth = speaker.requiredDepth.normalized()
        if (requiredDepth !in allowedDepths) {
            findings.add(
                finding(
                    ruleId = "REQUIRED_SPEAKER_DEPTH_UNKNOWN",
                    category = "structure",
                    severity = "MAJOR",
                    path = "$path.requiredDepth",
                    message = "Required speaker depth must be full_nuwa or light_voice.",
                    evidence = listOf(speaker.requiredDepth),
                    suggestedFix = "Use full_nuwa for important speakers and light_voice only for one-off background speakers.",
                ),
            )
        }
        requireNonBlank(findings, speaker.reason, "$path.reason", "REQUIRED_SPEAKER_REASON_MISSING", "WARNING", "Required speaker should explain why they may speak.")
    }

    private fun validateCardAgainstRequirement(
        card: CharacterNuwaCard,
        cardPath: String,
        speaker: CharacterNuwaRequiredSpeaker,
        speakerPath: String,
        findings: MutableList<CharacterNuwaGateFinding>,
    ) {
        val requiredDepth = speaker.requiredDepth.normalized()
        val cardDepth = card.cardDepth.normalized()
        if (requiredDepth == "full_nuwa" && cardDepth != "full_nuwa") {
            findings.add(
                finding(
                    ruleId = "IMPORTANT_SPEAKER_FULL_CARD_REQUIRED",
                    category = "gate",
                    severity = "CRITICAL",
                    path = cardPath,
                    message = "An important speaking character requires a full_nuwa card, but the card is not full depth.",
                    evidence = listOf("required=$requiredDepth", "actual=${card.cardDepth}", speakerPath),
                    suggestedFix = "Upgrade this card to full_nuwa before drafting accepted dialogue.",
                ),
            )
        }
        if (card.gateStatus.normalized() != "ready") {
            findings.add(
                finding(
                    ruleId = if (card.gateStatus.normalized() == "blocked") "CHARACTER_CARD_BLOCKED" else "CHARACTER_CARD_NOT_READY",
                    category = "gate",
                    severity = if (card.gateStatus.normalized() == "blocked") "CRITICAL" else "MAJOR",
                    path = "$cardPath.gateStatus",
                    message = "A required speaker card is not ready for drafting.",
                    evidence = listOf(card.gateStatus, speaker.characterId),
                    suggestedFix = "Set gateStatus=ready only after the card has enough voice, pressure, knowledge, and sample-line data.",
                ),
            )
        }
    }

    private fun validateCard(
        card: CharacterNuwaCard,
        path: String,
        findings: MutableList<CharacterNuwaGateFinding>,
    ) {
        requireNonBlank(findings, card.characterId, "$path.characterId", "CHARACTER_ID_MISSING", "MAJOR", "Character card is missing characterId.")
        requireNonBlank(findings, card.displayName, "$path.displayName", "CHARACTER_NAME_MISSING", "MAJOR", "Character card is missing displayName.")

        val cardDepth = card.cardDepth.normalized()
        if (cardDepth !in allowedDepths) {
            findings.add(
                finding(
                    ruleId = "CHARACTER_CARD_DEPTH_UNKNOWN",
                    category = "structure",
                    severity = "MAJOR",
                    path = "$path.cardDepth",
                    message = "Card depth must be full_nuwa or light_voice.",
                    evidence = listOf(card.cardDepth),
                    suggestedFix = "Use full_nuwa for important speakers and light_voice for one-off background speakers.",
                ),
            )
        }
        val status = card.gateStatus.normalized()
        if (status !in allowedStatuses) {
            findings.add(
                finding(
                    ruleId = "CHARACTER_CARD_STATUS_UNKNOWN",
                    category = "structure",
                    severity = "MAJOR",
                    path = "$path.gateStatus",
                    message = "Card gateStatus must be draft, ready, or blocked.",
                    evidence = listOf(card.gateStatus),
                    suggestedFix = "Use draft while preparing the card, ready when approved for drafting, or blocked when it must not be used.",
                ),
            )
        }

        validateCommonCardFields(card, path, findings)
        if (cardDepth == "full_nuwa") {
            validateFullCard(card, path, findings)
        } else {
            validateLightCard(card, path, findings)
        }
        scanAcceptedLinesForBans(card, path, findings)
    }

    private fun validateCommonCardFields(
        card: CharacterNuwaCard,
        path: String,
        findings: MutableList<CharacterNuwaGateFinding>,
    ) {
        requireNonBlank(findings, card.narrativeFunction, "$path.narrativeFunction", "NARRATIVE_FUNCTION_MISSING", "MAJOR", "Character card is missing narrativeFunction.")
        requireNonBlank(findings, card.currentPressure, "$path.currentPressure", "CURRENT_PRESSURE_MISSING", "MAJOR", "Character card must define the pressure acting on this speaker.")
        requireNonEmpty(findings, card.knowledgeBoundary, "$path.knowledgeBoundary", "KNOWLEDGE_BOUNDARY_EMPTY", "MAJOR", "Character card must define what this speaker is allowed to know.")
        requireNonBlank(findings, card.speechRegister, "$path.speechRegister", "SPEECH_REGISTER_MISSING", "MAJOR", "Character card is missing speechRegister.")
        requireNonBlank(findings, card.directness, "$path.directness", "DIRECTNESS_MISSING", "WARNING", "Character card should define directness.")
        requireNonBlank(findings, card.pressureResponse, "$path.pressureResponse", "PRESSURE_RESPONSE_MISSING", "MAJOR", "Character card must define how pressure changes speech.")
        requireNonEmpty(findings, card.neverDo, "$path.neverDo", "NEVER_DO_EMPTY", "MAJOR", "Character card must define speech behaviors this character must not do.")
        requireMinimumLines(findings, card.sampleLines.accepted, 1, "$path.sampleLines.accepted", "ACCEPTED_SAMPLE_LINES_EMPTY", "MAJOR", "Character card needs at least one accepted sample line.")
    }

    private fun validateFullCard(
        card: CharacterNuwaCard,
        path: String,
        findings: MutableList<CharacterNuwaGateFinding>,
    ) {
        requireNonBlank(findings, card.socialPosition, "$path.socialPosition", "SOCIAL_POSITION_MISSING", "MAJOR", "Full Nuwa card is missing socialPosition.")
        requireNonBlank(findings, card.relationshipToProtagonist, "$path.relationshipToProtagonist", "RELATIONSHIP_TO_PROTAGONIST_MISSING", "MAJOR", "Full Nuwa card is missing relationshipToProtagonist.")
        requireNonBlank(findings, card.wantsFromScene, "$path.wantsFromScene", "WANTS_FROM_SCENE_MISSING", "MAJOR", "Full Nuwa card must define what the character wants from the scene.")
        requireNonBlank(findings, card.fearsInScene, "$path.fearsInScene", "FEARS_IN_SCENE_MISSING", "WARNING", "Full Nuwa card should define what the character fears in the scene.")
        requireNonBlank(findings, card.sentenceLengthBias, "$path.sentenceLengthBias", "SENTENCE_LENGTH_BIAS_MISSING", "WARNING", "Full Nuwa card should define sentenceLengthBias.")
        requireNonBlank(findings, card.silencePattern, "$path.silencePattern", "SILENCE_PATTERN_MISSING", "WARNING", "Full Nuwa card should define when the character goes silent.")
        requireNonEmpty(findings, card.allowedTells, "$path.allowedTells", "ALLOWED_TELLS_EMPTY", "WARNING", "Full Nuwa card should define visible or verbal tells that are allowed.")
        requireNonEmpty(findings, card.forbiddenTells, "$path.forbiddenTells", "FORBIDDEN_TELLS_EMPTY", "WARNING", "Full Nuwa card should define forbidden tells.")
        requireNonEmpty(findings, card.favoriteMoves, "$path.favoriteMoves", "FAVORITE_MOVES_EMPTY", "WARNING", "Full Nuwa card should define favorite dialogue moves.")
        requireMinimumLines(findings, card.sampleLines.accepted, 3, "$path.sampleLines.accepted", "FULL_CARD_ACCEPTED_SAMPLES_TOO_FEW", "MAJOR", "Full Nuwa card needs at least three accepted sample lines.")
        requireMinimumLines(findings, card.sampleLines.rejected, 3, "$path.sampleLines.rejected", "FULL_CARD_REJECTED_SAMPLES_TOO_FEW", "MAJOR", "Full Nuwa card needs at least three rejected sample lines.")
    }

    private fun validateLightCard(
        card: CharacterNuwaCard,
        path: String,
        findings: MutableList<CharacterNuwaGateFinding>,
    ) {
        if (card.relationshipToProtagonist.isBlank()) {
            findings.add(
                finding(
                    ruleId = "LIGHT_CARD_RELATIONSHIP_MISSING",
                    category = "voice",
                    severity = "WARNING",
                    path = "$path.relationshipToProtagonist",
                    message = "Light voice card should still define the speaker's relationship to the protagonist or viewpoint.",
                    suggestedFix = "Add a short relationship such as stranger, guard, vendor, witness, or hostile passerby.",
                ),
            )
        }
    }

    private fun scanAcceptedLinesForBans(
        card: CharacterNuwaCard,
        path: String,
        findings: MutableList<CharacterNuwaGateFinding>,
    ) {
        card.sampleLines.accepted.forEachIndexed { index, line ->
            hardBannedStructures.forEach { banned ->
                if (banned.regex.containsMatchIn(line)) {
                    findings.add(
                        finding(
                            ruleId = "BANNED_CALIBRATION_STRUCTURE_${banned.id}",
                            category = "style",
                            severity = "CRITICAL",
                            path = "$path.sampleLines.accepted[$index]",
                            message = "Accepted sample line contains a hard-banned AI calibration structure.",
                            evidence = listOf(line),
                            suggestedFix = "Replace the slogan with concrete action, cost, refusal, pressure, or visible evidence.",
                        ),
                    )
                }
            }
        }
    }

    private fun requireNonBlank(
        findings: MutableList<CharacterNuwaGateFinding>,
        value: String,
        path: String,
        ruleId: String,
        severity: String,
        message: String,
        suggestedFix: String = "Fill this field before passing the Character Nuwa Gate.",
    ) {
        if (value.isBlank()) {
            findings.add(finding(ruleId, "structure", severity, path, message, suggestedFix = suggestedFix))
        }
    }

    private fun requireNonEmpty(
        findings: MutableList<CharacterNuwaGateFinding>,
        value: List<String>,
        path: String,
        ruleId: String,
        severity: String,
        message: String,
        suggestedFix: String = "Add at least one concrete entry.",
    ) {
        if (value.none { it.isNotBlank() }) {
            findings.add(finding(ruleId, "structure", severity, path, message, suggestedFix = suggestedFix))
        }
    }

    private fun requireMinimumLines(
        findings: MutableList<CharacterNuwaGateFinding>,
        value: List<String>,
        minimum: Int,
        path: String,
        ruleId: String,
        severity: String,
        message: String,
    ) {
        val count = value.count { it.isNotBlank() }
        if (count < minimum) {
            findings.add(
                finding(
                    ruleId = ruleId,
                    category = "samples",
                    severity = severity,
                    path = path,
                    message = message,
                    evidence = listOf("actual=$count", "minimum=$minimum"),
                    suggestedFix = "Add sample lines that prove the character can speak without generic AI cadence.",
                ),
            )
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
    ): CharacterNuwaGateFinding =
        CharacterNuwaGateFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            path = path,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
        )

    private fun String.normalized(): String = trim().lowercase()

    private data class BannedStructure(
        val id: String,
        val regex: Regex,
    )
}
