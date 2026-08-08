package com.localnovel.engine.service

import com.localnovel.engine.model.NovelizationWriterBrief
import com.localnovel.engine.model.NovelizationWriterBriefEvidence
import com.localnovel.engine.model.NovelizationWriterBriefFinding
import com.localnovel.engine.model.NovelizationWriterBriefValidationReport
import com.localnovel.engine.model.NovelizationPreflightReport

class NovelizationWriterBriefValidationService {
    private val knownPreflightVerdicts = setOf("PASS", "PASS_WITH_WARNINGS", "FAIL")

    fun validate(
        brief: NovelizationWriterBrief,
        briefFile: String = "",
        sourcePreflightReport: NovelizationPreflightReport? = null,
        sourcePreflightReportFile: String = "",
    ): NovelizationWriterBriefValidationReport {
        val findings = mutableListOf<NovelizationWriterBriefFinding>()

        requireNonBlank(findings, brief.briefId, "$.briefId", "BRIEF_ID_MISSING", "WARNING", "Writer brief should carry a stable briefId.")
        requireNonBlank(findings, brief.projectId, "$.projectId", "PROJECT_ID_MISSING", "WARNING", "Writer brief should identify the project.")
        requireNonBlank(findings, brief.targetChapter, "$.targetChapter", "TARGET_CHAPTER_MISSING", "MAJOR", "Writer brief must identify the target chapter.")
        requireNonBlank(
            findings,
            brief.sourcePreflightReport,
            "$.sourcePreflightReport",
            "SOURCE_PREFLIGHT_REPORT_MISSING",
            "MAJOR",
            "Writer brief must point to the preflight report that allowed drafting to start.",
        )
        requireNonEmpty(findings, brief.previousChapterRefs, "$.previousChapterRefs", "PREVIOUS_CHAPTER_REFS_EMPTY", "MAJOR", "Writer brief must reference accepted prior chapter material.")
        requireNonEmpty(findings, brief.acceptedChapterFacts, "$.acceptedChapterFacts", "ACCEPTED_CHAPTER_FACTS_EMPTY", "MAJOR", "Writer brief must list accepted facts that the next chapter cannot contradict.")
        requireNonEmpty(findings, brief.characterStates, "$.characterStates", "CHARACTER_STATES_EMPTY", "MAJOR", "Writer brief must list current character states before drafting.")
        requireNonEmpty(findings, brief.openThreads, "$.openThreads", "OPEN_THREADS_EMPTY", "WARNING", "Writer brief should list open threads or explicitly close that there are none.")
        requireNonEmpty(findings, brief.mustAdvance, "$.mustAdvance", "MUST_ADVANCE_EMPTY", "MAJOR", "Writer brief must state what the next chapter should advance.")
        requireNonEmpty(findings, brief.mustNotRepeat, "$.mustNotRepeat", "MUST_NOT_REPEAT_EMPTY", "MAJOR", "Writer brief must state what prior beats must not be repeated.")
        requireNonEmpty(findings, brief.allowedScenes, "$.allowedScenes", "ALLOWED_SCENES_EMPTY", "WARNING", "Writer brief should constrain where the draft may happen.")
        requireNonEmpty(findings, brief.knownUnknowns, "$.knownUnknowns", "KNOWN_UNKNOWNS_EMPTY", "WARNING", "Writer brief should mark facts the draft must not invent.")
        requireNonBlank(findings, brief.tonePacingTarget, "$.tonePacingTarget", "TONE_PACING_TARGET_MISSING", "MAJOR", "Writer brief must define tone and pacing target.")
        requireNonBlank(findings, brief.styleContract, "$.styleContract", "STYLE_CONTRACT_MISSING", "MAJOR", "Writer brief must include a prose style contract.")
        requireNonEmpty(findings, brief.characterNuwaReferences, "$.characterNuwaReferences", "CHARACTER_NUWA_REFERENCES_EMPTY", "MAJOR", "Writer brief must reference the Character Nuwa cards or gate report used for dialogue.")
        requireNonEmpty(findings, brief.antiAiProseConstraints, "$.antiAiProseConstraints", "ANTI_AI_PROSE_CONSTRAINTS_EMPTY", "MAJOR", "Writer brief must include anti-AI prose constraints.")

        if (!brief.authorOnlyExcluded) {
            findings.add(
                finding(
                    ruleId = "AUTHOR_ONLY_NOT_EXCLUDED",
                    category = "secrecy",
                    severity = "CRITICAL",
                    path = "$.authorOnlyExcluded",
                    message = "Writer brief must explicitly exclude author-only material from the drafting surface.",
                    suggestedFix = "Set authorOnlyExcluded=true after removing author-only secrets from user-facing draft instructions.",
                ),
            )
        }

        sourcePreflightReport?.let { preflight ->
            val preflightVerdict = preflight.verdict.trim().uppercase()
            when {
                preflightVerdict.isBlank() -> findings.add(
                    finding(
                        ruleId = "SOURCE_PREFLIGHT_VERDICT_MISSING",
                        category = "preflight",
                        severity = "MAJOR",
                        path = "$.sourcePreflightReport",
                        message = "Linked preflight report has no verdict.",
                        suggestedFix = "Regenerate the preflight report before validating this writer brief.",
                    ),
                )
                preflightVerdict !in knownPreflightVerdicts -> findings.add(
                    finding(
                        ruleId = "SOURCE_PREFLIGHT_VERDICT_UNKNOWN",
                        category = "preflight",
                        severity = "MAJOR",
                        path = "$.sourcePreflightReport",
                        message = "Linked preflight report has an unknown verdict.",
                        evidence = listOf(preflight.verdict),
                        suggestedFix = "Use a preflight report with verdict PASS, PASS_WITH_WARNINGS, or FAIL.",
                    ),
                )
                preflightVerdict == "FAIL" -> findings.add(
                    finding(
                        ruleId = "SOURCE_PREFLIGHT_FAILED",
                        category = "preflight",
                        severity = "CRITICAL",
                        path = "$.sourcePreflightReport",
                        message = "Writer brief cannot pass because the linked novelization preflight failed.",
                        evidence = listOf("preflightId=${preflight.preflightId}", "findings=${preflight.findingCount}"),
                        suggestedFix = "Fix the upstream preflight before using this writer brief for drafting.",
                    ),
                )
            }
            if (brief.sourcePreflightReport.isNotBlank() && sourcePreflightReportFile.isNotBlank()) {
                val declared = brief.sourcePreflightReport.replace('\\', '/')
                val actual = sourcePreflightReportFile.replace('\\', '/')
                if (!actual.endsWith(declared.substringAfterLast('/')) && declared != actual) {
                    findings.add(
                        finding(
                            ruleId = "SOURCE_PREFLIGHT_REPORT_PATH_MISMATCH",
                            category = "preflight",
                            severity = "WARNING",
                            path = "$.sourcePreflightReport",
                            message = "Writer brief points to a different preflight report path than the one provided for validation.",
                            evidence = listOf("declared=${brief.sourcePreflightReport}", "provided=$sourcePreflightReportFile"),
                            suggestedFix = "Update sourcePreflightReport or pass the intended preflight report file.",
                        ),
                    )
                }
            }
        }

        val verdict = when {
            findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
            findings.any { it.severity == "WARNING" } -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }

        return NovelizationWriterBriefValidationReport(
            verdict = verdict,
            briefFile = briefFile,
            sourcePreflightReportFile = sourcePreflightReportFile,
            briefId = brief.briefId,
            findingCount = findings.size,
            findings = findings,
            evidence = NovelizationWriterBriefEvidence(
                sourcePreflightVerdict = sourcePreflightReport?.verdict.orEmpty(),
                previousChapterRefCount = brief.previousChapterRefs.countNonBlank(),
                acceptedFactCount = brief.acceptedChapterFacts.countNonBlank(),
                characterStateCount = brief.characterStates.countNonBlank(),
                openThreadCount = brief.openThreads.countNonBlank(),
                mustAdvanceCount = brief.mustAdvance.countNonBlank(),
                mustNotRepeatCount = brief.mustNotRepeat.countNonBlank(),
                characterNuwaReferenceCount = brief.characterNuwaReferences.countNonBlank(),
                antiAiConstraintCount = brief.antiAiProseConstraints.countNonBlank(),
            ),
        )
    }

    private fun requireNonBlank(
        findings: MutableList<NovelizationWriterBriefFinding>,
        value: String,
        path: String,
        ruleId: String,
        severity: String,
        message: String,
        suggestedFix: String = "Fill this field before using the writer brief for drafting.",
    ) {
        if (value.isBlank()) {
            findings.add(finding(ruleId, "structure", severity, path, message, suggestedFix = suggestedFix))
        }
    }

    private fun requireNonEmpty(
        findings: MutableList<NovelizationWriterBriefFinding>,
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

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): NovelizationWriterBriefFinding =
        NovelizationWriterBriefFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            path = path,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
        )

    private fun List<String>.countNonBlank(): Int = count { it.isNotBlank() }
}
