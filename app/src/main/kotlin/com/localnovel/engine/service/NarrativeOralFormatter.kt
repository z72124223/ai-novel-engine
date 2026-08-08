package com.localnovel.engine.service

import com.localnovel.engine.model.OralLineCandidate

enum class OralAudioSegmentKind {
    NARRATION,
    DIALOGUE,
}

data class OralAudioSegment(
    val kind: OralAudioSegmentKind,
    val text: String,
    val displayText: String = text,
    val speakerName: String = "",
    val attribution: String = "",
)

object NarrativeOralFormatter {
    private val dialoguePattern = Regex("""「([^」]+)」""")
    private val attributionBeforeQuotePattern = Regex(
        """([\u4E00-\u9FFF\u3400-\u4DBF·]{2,12})(低聲|沉聲|輕聲|高聲|急聲|冷聲|淡聲|緩聲|壓低聲音)?(說|道|問|答|喊|應|開口|笑道|沉聲道|喃喃道|接口|附和|叮囑|吩咐|囑咐)([：:])?\s*$""",
    )
    private val pronounAttributionPattern = Regex(
        """(他|她)(低聲|沉聲|輕聲|高聲|急聲|冷聲|淡聲|緩聲)?(說|道|問|答|喊|應|開口|笑道|喃喃道|接口|附和|叮囑|吩咐|囑咐)([：:])?\s*$""",
    )
    private val sentenceSplitPattern = Regex("""(?<=[。！？；;])""")
    private val deliveryAdverbs =
        "低聲|沉聲|輕聲|高聲|急聲|冷聲|淡聲|緩聲|喘而穩|皺眉|壓低聲音"
    private val attributionVerbs =
        "說|道|問|答|喊|應|開口|笑道|喃喃道|接口|附和|叮囑|吩咐|囑咐|沉聲道"
    val dialogueAttributionVerbs = attributionVerbs.split("|")

    fun splitIntoOralSegments(
        narrative: String,
        oralLineCandidates: List<OralLineCandidate> = emptyList(),
        characterNames: List<String> = emptyList(),
    ): List<OralAudioSegment> {
        if (narrative.isBlank()) return emptyList()
        val matches = dialoguePattern.findAll(narrative).toList()
        if (matches.isEmpty()) {
            return splitNarrationSentences(narrative.trim()).map { sentence ->
                OralAudioSegment(
                    kind = OralAudioSegmentKind.NARRATION,
                    text = sentence,
                    displayText = sentence,
                )
            }
        }

        val dialogueCandidates = oralLineCandidates.filter { it.lineType == "dialogue" && it.line.isNotBlank() }
        val segments = mutableListOf<OralAudioSegment>()
        var lastEnd = 0

        matches.forEach { match ->
            val before = narrative.substring(lastEnd, match.range.first)
            val (narrationParts, inlineAttribution) = splitNarrationBeforeQuote(before, characterNames)
            narrationParts.forEach { sentence ->
                if (sentence.isNotBlank()) {
                    segments += OralAudioSegment(
                        kind = OralAudioSegmentKind.NARRATION,
                        text = sentence,
                        displayText = sentence,
                    )
                }
            }

            val dialogueLine = match.groupValues[1].trim()
            if (dialogueLine.isNotBlank()) {
                val attribution = inlineAttribution ?: resolveAttribution(
                    narrative = narrative,
                    quoteStart = match.range.first,
                    dialogueLine = dialogueLine,
                    dialogueCandidates = dialogueCandidates,
                    characterNames = characterNames,
                )
                val speaker = resolveSpeakerName(
                    speaker = attribution.speaker,
                    dialogueLine = dialogueLine,
                    dialogueCandidates = dialogueCandidates,
                    characterNames = characterNames,
                )
                val formatted = formatDialogueAttribution(
                    speaker = speaker,
                    delivery = attribution.delivery,
                    tonalParticle = attribution.tonalParticle,
                    verb = attribution.verb,
                    line = dialogueLine,
                )
                segments += OralAudioSegment(
                    kind = OralAudioSegmentKind.DIALOGUE,
                    text = formatted,
                    displayText = formatted,
                    speakerName = attribution.speaker,
                    attribution = buildAttributionLabel(attribution.delivery, attribution.verb),
                )
            }
            lastEnd = match.range.last + 1
        }

        val tail = narrative.substring(lastEnd).trim()
        splitNarrationSentences(tail).forEach { sentence ->
            if (sentence.isNotBlank()) {
                segments += OralAudioSegment(
                    kind = OralAudioSegmentKind.NARRATION,
                    text = sentence,
                    displayText = sentence,
                )
            }
        }
        return segments
    }

    fun splitNarrationSentences(text: String): List<String> =
        text.trim()
            .split(sentenceSplitPattern)
            .map { it.trim() }
            .filter { it.isNotBlank() }

    fun formatDialogueAttribution(
        speaker: String,
        delivery: String,
        tonalParticle: String = "",
        verb: String,
        line: String,
    ): String {
        val safeSpeaker = speaker.ifBlank { "他" }
        val safeVerb = verb.ifBlank { "道" }
        val deliveryPrefix = delivery.trim()
        val middle = when {
            deliveryPrefix.isBlank() -> safeVerb
            tonalParticle.isNotBlank() -> "$deliveryPrefix$tonalParticle$safeVerb"
            else -> "$deliveryPrefix$safeVerb"
        }
        return "$safeSpeaker$middle：「$line」"
    }

    fun hasSpeakerAttributionBeforeQuote(narrative: String, quoteStart: Int, characterNames: List<String>): Boolean {
        val window = narrative.substring(0, quoteStart).takeLast(80).trim()
        return findInlineAttribution(window, characterNames) != null
    }

    fun findDialogueWithoutAttribution(narrative: String, characterNames: List<String>): List<String> {
        val quotes = dialoguePattern.findAll(narrative).toList()
        return quotes.filterNot { match ->
            hasSpeakerAttributionBeforeQuote(narrative, match.range.first, characterNames)
        }.map { it.groupValues[1].trim() }.filter { it.isNotBlank() }
    }

    private data class ResolvedAttribution(
        val speaker: String,
        val delivery: String,
        val tonalParticle: String = "",
        val verb: String,
    )

    private fun resolveAttribution(
        narrative: String,
        quoteStart: Int,
        dialogueLine: String,
        dialogueCandidates: List<OralLineCandidate>,
        characterNames: List<String>,
    ): ResolvedAttribution {
        val window = narrative.substring(0, quoteStart).takeLast(80).trim()
        findInlineAttribution(window, characterNames)?.let { return it }
        val candidate = dialogueCandidates.firstOrNull { OralLineMatcher.matchesSpokenLine(dialogueLine, it.line) }
        if (candidate != null) {
            val delivery = candidate.delivery.substringBefore("、").substringBefore("，").trim()
            return ResolvedAttribution(
                speaker = candidate.speaker.ifBlank { characterNames.firstOrNull().orEmpty() },
                delivery = delivery,
                verb = "道",
            )
        }
        return ResolvedAttribution(
            speaker = characterNames.firstOrNull().orEmpty(),
            delivery = "",
            verb = "道",
        )
    }

    private fun resolveSpeakerName(
        speaker: String,
        dialogueLine: String,
        dialogueCandidates: List<OralLineCandidate>,
        characterNames: List<String>,
    ): String {
        if (speaker.isNotBlank() && speaker !in listOf("他", "她")) return speaker
        val candidateSpeaker = dialogueCandidates
            .firstOrNull { OralLineMatcher.matchesSpokenLine(dialogueLine, it.line) }
            ?.speaker
            ?.takeIf { it.isNotBlank() }
        if (candidateSpeaker != null) return candidateSpeaker
        val namedCharacter = characterNames.firstOrNull().orEmpty()
        if (namedCharacter.isNotBlank()) return namedCharacter
        return speaker
    }

    private fun splitNarrationBeforeQuote(
        text: String,
        characterNames: List<String>,
    ): Pair<List<String>, ResolvedAttribution?> {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return emptyList<String>() to null

        val match = findAttributionMatch(trimmed, characterNames) ?: return splitNarrationSentences(trimmed) to null
        val narrationBody = trimmed.substring(0, match.range.first).trim()
        return splitNarrationSentences(narrationBody) to attributionFromMatch(match, characterNames)
    }

    private fun findInlineAttribution(text: String, characterNames: List<String>): ResolvedAttribution? =
        findAttributionMatch(text, characterNames)?.let { attributionFromMatch(it, characterNames) }

    private fun findAttributionMatch(text: String, characterNames: List<String>): MatchResult? {
        buildCharacterAttributionPattern(characterNames).find(text)?.let { return it }
        pronounAttributionPattern.find(text)?.let { match ->
            if (text.length - match.range.first <= 18) return match
        }
        return attributionBeforeQuotePattern.find(text)
    }

    private fun attributionFromMatch(match: MatchResult, @Suppress("UNUSED_PARAMETER") characterNames: List<String>): ResolvedAttribution {
        val fourth = match.groupValues.getOrNull(4).orEmpty().trim()
        if (fourth.isNotBlank() && dialogueAttributionVerbs.contains(fourth)) {
            return ResolvedAttribution(
                speaker = match.groupValues[1].trim(),
                delivery = match.groupValues[2].trim(),
                tonalParticle = match.groupValues[3].trim(),
                verb = fourth,
            )
        }
        return ResolvedAttribution(
            speaker = match.groupValues[1].trim(),
            delivery = match.groupValues[2].trim(),
            verb = match.groupValues[3].trim(),
        )
    }

    private fun buildCharacterAttributionPattern(characterNames: List<String>): Regex {
        val names = characterNames
            .filter { it.isNotBlank() && it !in listOf("他", "她") }
            .distinct()
            .sortedByDescending { it.length }
            .joinToString("|") { Regex.escape(it) }
        if (names.isBlank()) {
            return Regex("""$^""")
        }
        return Regex(
            """($names)(?:[^：:「。！？；;]{0,20}?)?(?:($deliveryAdverbs)(地)?)($attributionVerbs)[：:]\s*$""",
        )
    }

    private fun buildAttributionLabel(delivery: String, verb: String): String =
        if (delivery.isBlank()) verb else "$delivery$verb"
}