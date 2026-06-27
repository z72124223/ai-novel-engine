package com.localnovel.engine.service

import com.localnovel.engine.model.CharacterNuwaGateValidationReport
import com.localnovel.engine.model.NovelizationPreflightEvidence
import com.localnovel.engine.model.NovelizationPreflightFinding
import com.localnovel.engine.model.NovelizationPreflightReport

class NovelizationPreflightValidationService {
    private val knownVerdicts = setOf("PASS", "PASS_WITH_WARNINGS", "FAIL")

    fun validate(
        characterNuwaReport: CharacterNuwaGateValidationReport,
        characterNuwaReportFile: String = "",
        characterNuwaGateFile: String = "",
        preflightId: String = "",
    ): NovelizationPreflightReport {
        val findings = mutableListOf<NovelizationPreflightFinding>()
        val upstreamVerdict = characterNuwaReport.verdict.trim().uppercase()

        if (preflightId.isBlank()) {
            findings.add(
                finding(
                    ruleId = "PREFLIGHT_ID_MISSING",
                    category = "structure",
                    severity = "WARNING",
                    path = "$.preflightId",
                    message = "Novelization preflight should carry a stable preflightId for review handoff.",
                    suggestedFix = "Pass --preflight or store a stable id in the calling workflow.",
                ),
            )
        }

        when {
            upstreamVerdict.isBlank() -> findings.add(
                finding(
                    ruleId = "CHARACTER_NUWA_VERDICT_MISSING",
                    category = "character_nuwa",
                    severity = "MAJOR",
                    path = "$.characterNuwa.verdict",
                    message = "Character Nuwa Gate report has no verdict.",
                    suggestedFix = "Run validate-character-nuwa-gate and pass its report into this preflight.",
                ),
            )
            upstreamVerdict !in knownVerdicts -> findings.add(
                finding(
                    ruleId = "CHARACTER_NUWA_VERDICT_UNKNOWN",
                    category = "character_nuwa",
                    severity = "MAJOR",
                    path = "$.characterNuwa.verdict",
                    message = "Character Nuwa Gate report has an unknown verdict.",
                    evidence = listOf(characterNuwaReport.verdict),
                    suggestedFix = "Use a Character Nuwa report with verdict PASS, PASS_WITH_WARNINGS, or FAIL.",
                ),
            )
            upstreamVerdict == "FAIL" -> findings.add(
                finding(
                    ruleId = "CHARACTER_NUWA_GATE_FAILED",
                    category = "character_nuwa",
                    severity = "CRITICAL",
                    path = "$.characterNuwa",
                    message = "Next-chapter drafting cannot start because Character Nuwa Gate failed.",
                    evidence = listOf("upstreamFindings=${characterNuwaReport.findingCount}"),
                    suggestedFix = "Fix the Character Nuwa Gate report before drafting accepted dialogue.",
                ),
            )
        }

        if (characterNuwaReport.requiredSpeakingCharacterCount <= 0) {
            findings.add(
                finding(
                    ruleId = "NO_REQUIRED_SPEAKERS_DECLARED",
                    category = "character_nuwa",
                    severity = "MAJOR",
                    path = "$.characterNuwa.requiredSpeakingCharacterCount",
                    message = "The upstream gate does not declare any speaking characters.",
                    suggestedFix = "Declare every character who may speak before starting the next chapter draft.",
                ),
            )
        }

        if (characterNuwaReport.cardCount <= 0) {
            findings.add(
                finding(
                    ruleId = "NO_CHARACTER_CARDS_PRESENT",
                    category = "character_nuwa",
                    severity = "CRITICAL",
                    path = "$.characterNuwa.cardCount",
                    message = "The upstream gate has no Character Nuwa cards.",
                    suggestedFix = "Create Character Nuwa cards before allowing the draft workflow to start.",
                ),
            )
        }

        if (
            characterNuwaReport.requiredSpeakingCharacterCount > 0 &&
            characterNuwaReport.evidence.readyCardCount < characterNuwaReport.requiredSpeakingCharacterCount
        ) {
            findings.add(
                finding(
                    ruleId = "READY_CARD_COVERAGE_INCOMPLETE",
                    category = "character_nuwa",
                    severity = "MAJOR",
                    path = "$.characterNuwa.evidence.readyCardCount",
                    message = "Not every required speaking character has a ready card.",
                    evidence = listOf(
                        "ready=${characterNuwaReport.evidence.readyCardCount}",
                        "required=${characterNuwaReport.requiredSpeakingCharacterCount}",
                    ),
                    suggestedFix = "Set cards to ready only after their voice, pressure, knowledge, and sample lines are complete.",
                ),
            )
        }

        if (characterNuwaReport.evidence.bannedCalibrationHitCount > 0) {
            findings.add(
                finding(
                    ruleId = "BANNED_CALIBRATION_PRESENT_UPSTREAM",
                    category = "style",
                    severity = "CRITICAL",
                    path = "$.characterNuwa.evidence.bannedCalibrationHitCount",
                    message = "The upstream gate found hard-banned calibration phrasing in accepted sample lines.",
                    evidence = listOf("hits=${characterNuwaReport.evidence.bannedCalibrationHitCount}"),
                    suggestedFix = "Replace slogan-like calibration with concrete action, pressure, cost, or refusal.",
                ),
            )
        }

        val verdict = when {
            findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
            findings.any { it.severity == "WARNING" } -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }

        return NovelizationPreflightReport(
            verdict = verdict,
            preflightId = preflightId,
            characterNuwaReportFile = characterNuwaReportFile,
            characterNuwaGateFile = characterNuwaGateFile,
            characterNuwaGateId = characterNuwaReport.gateId,
            findingCount = findings.size,
            findings = findings,
            evidence = NovelizationPreflightEvidence(
                characterNuwaVerdict = characterNuwaReport.verdict,
                requiredSpeakingCharacterCount = characterNuwaReport.requiredSpeakingCharacterCount,
                characterCardCount = characterNuwaReport.cardCount,
                readyCharacterCardCount = characterNuwaReport.evidence.readyCardCount,
                upstreamFindingCount = characterNuwaReport.findingCount,
                bannedCalibrationHitCount = characterNuwaReport.evidence.bannedCalibrationHitCount,
            ),
        )
    }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): NovelizationPreflightFinding =
        NovelizationPreflightFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            path = path,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
        )
}
