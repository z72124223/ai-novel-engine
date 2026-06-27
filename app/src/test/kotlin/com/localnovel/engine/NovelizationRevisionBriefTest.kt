package com.localnovel.engine

import com.localnovel.engine.model.NovelizationDraftIntakeReport
import com.localnovel.engine.model.NovelizationProseQualityAuditReport
import com.localnovel.engine.model.NovelizationProseQualityFinding
import com.localnovel.engine.model.NovelizationRevisionBriefReport
import com.localnovel.engine.model.NovelizationRevisionContinuityAnchors
import com.localnovel.engine.service.NovelizationRevisionBriefService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NovelizationRevisionBriefTest {
    @Test
    fun proseQualityFindingsBecomeRequiredRevisionItems() {
        val report = NovelizationRevisionBriefService().build(
            draftText = "Draft text.",
            draftIntakeReport = NovelizationDraftIntakeReport(verdict = "PASS"),
            proseQualityReport = proseQualityReport(
                verdict = "FAIL",
                findings = listOf(
                    proseFinding("BANNED_CALIBRATION_STRUCTURE_CALIBRATION_EN_NOT_BUT", "anti_ai_prose", "CRITICAL"),
                    proseFinding("DIALOGUE_EXPOSITION_DUMP", "dialogue", "MAJOR"),
                    proseFinding("SCENE_PRESSURE_MISSING", "scene_pressure", "MAJOR"),
                ),
            ),
            continuityAnchors = sampleAnchors(),
            briefId = "revision-brief-sample-001",
        )

        assertEquals("PASS", report.verdict)
        assertTrue(report.revisionRequired)
        assertEquals(3, report.requiredFixes.size)
        assertTrue(report.requiredFixes.any { it.fixType == "banned_phrase_removal" })
        assertTrue(report.requiredFixes.any { it.fixType == "dialogue_revision" })
        assertTrue(report.requiredFixes.any { it.fixType == "scene_pressure_reinforcement" })
        assertTrue(report.mandatoryChecklist.contains("character_voice_revision"))
        assertEquals(2, report.continuityAnchors.size)
    }

    @Test
    fun missingContinuityAnchorsBlocksRevisionBrief() {
        val report = NovelizationRevisionBriefService().build(
            draftText = "Draft text.",
            draftIntakeReport = NovelizationDraftIntakeReport(verdict = "PASS"),
            proseQualityReport = proseQualityReport(verdict = "PASS"),
            continuityAnchors = NovelizationRevisionContinuityAnchors(),
            briefId = "revision-brief-sample-001",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "CONTINUITY_ANCHORS_EMPTY" && it.severity == "CRITICAL" })
    }

    @Test
    fun cliWritesRevisionBriefReportWithoutDatabase() {
        val tempDir = Files.createTempDirectory("revision-brief")
        val draftPath = samplePath("novelization_draft_intake_sample.md")
        val draftIntakePath = tempDir.resolve("draft_intake_report.json")
        val proseQualityPath = tempDir.resolve("prose_quality_report.json")
        val out = tempDir.resolve("revision_brief_report.json")
        writeJson(draftIntakePath, NovelizationDraftIntakeReport(verdict = "PASS"))
        writeJson(
            proseQualityPath,
            proseQualityReport(
                verdict = "FAIL",
                findings = listOf(proseFinding("DIALOGUE_EXPOSITION_DUMP", "dialogue", "MAJOR")),
            ),
        )

        val exit = Cli().run(
            listOf(
                "create-novelization-revision-brief",
                "--draft",
                draftPath.toString(),
                "--draft-intake-report",
                draftIntakePath.toString(),
                "--prose-quality-report",
                proseQualityPath.toString(),
                "--continuity-anchors",
                samplePath("novelization_revision_continuity_anchors_sample.json").toString(),
                "--brief",
                "revision-brief-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<NovelizationRevisionBriefReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("revision-brief-cli-001", report.briefId)
        assertTrue(report.revisionRequired)
        assertTrue(report.requiredFixes.any { it.fixType == "dialogue_revision" })
        assertTrue(report.continuityAnchors.isNotEmpty())
    }

    private fun proseQualityReport(
        verdict: String,
        findings: List<NovelizationProseQualityFinding> = emptyList(),
    ): NovelizationProseQualityAuditReport =
        NovelizationProseQualityAuditReport(
            verdict = verdict,
            findingCount = findings.size,
            findings = findings,
        )

    private fun proseFinding(ruleId: String, category: String, severity: String): NovelizationProseQualityFinding =
        NovelizationProseQualityFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            path = "$.draft",
            message = "Sample finding.",
            evidence = listOf("sample evidence"),
            suggestedFix = "Sample suggested fix.",
        )

    private fun sampleAnchors(): NovelizationRevisionContinuityAnchors =
        NovelizationRevisionContinuityAnchors(
            anchors = listOf(
                "Mira already paid the door toll before reaching the checkpoint.",
                "The guard requires a visible mark before answering questions.",
            ),
            doNotChange = listOf("Do not reset the toll payment as unpaid."),
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }

    private fun samplePath(fileName: String): Path {
        val candidates = listOf(
            Path.of("samples", fileName),
            Path.of("..", "samples", fileName),
        )
        return candidates.firstOrNull { Files.exists(it) } ?: error("Cannot find sample file: $fileName")
    }
}
