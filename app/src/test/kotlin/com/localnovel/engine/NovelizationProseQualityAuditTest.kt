package com.localnovel.engine

import com.localnovel.engine.model.NovelizationDraftIntakeReport
import com.localnovel.engine.model.NovelizationProseQualityAuditReport
import com.localnovel.engine.service.NovelizationProseQualityAuditService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NovelizationProseQualityAuditTest {
    @Test
    fun sampleDraftPassesProseQualityAudit() {
        val draftText = Files.readString(samplePath("novelization_draft_intake_sample.md"))

        val report = NovelizationProseQualityAuditService().audit(
            draftText = draftText,
            draftIntakeReport = NovelizationDraftIntakeReport(verdict = "PASS"),
        )

        assertEquals("PASS", report.verdict)
        assertEquals("PASS", report.evidence.draftIntakeVerdict)
        assertTrue(report.evidence.dialogueLineCount > 0)
        assertTrue(report.findings.isEmpty())
    }

    @Test
    fun failedDraftIntakeBlocksProseQualityAudit() {
        val draftText = Files.readString(samplePath("novelization_draft_intake_sample.md"))

        val report = NovelizationProseQualityAuditService().audit(
            draftText = draftText,
            draftIntakeReport = NovelizationDraftIntakeReport(verdict = "FAIL", findingCount = 1),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "DRAFT_INTAKE_FAILED" && it.severity == "CRITICAL" })
    }

    @Test
    fun bannedCalibrationAndExpositionDialogueFailAudit() {
        val draftText = """
            The place was full of ancient secrets and destiny.
            "Please explain the whole system. This is not survival, but destiny."
        """.trimIndent()

        val report = NovelizationProseQualityAuditService().audit(
            draftText = draftText,
            draftIntakeReport = NovelizationDraftIntakeReport(verdict = "PASS"),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId.startsWith("BANNED_CALIBRATION_STRUCTURE") && it.severity == "CRITICAL" })
        assertTrue(report.findings.any { it.ruleId == "DIALOGUE_EXPOSITION_DUMP" && it.severity == "MAJOR" })
        assertTrue(report.findings.any { it.ruleId == "GENERIC_ABSTRACTION_CLUSTER" && it.severity == "MAJOR" })
    }

    @Test
    fun chinesePressureScenePassesWithoutFalsePressureFailure() {
        val draftText = """
            米拉停在內門前，隊伍從背後推上來。守衛沒有抬頭，只把木牌敲了兩下。

            「先看標記，沒有標記不能過門。」

            她把手停在錢袋外側。「我在外門付過。你只要告訴我，這裡還算不算。」
        """.trimIndent()

        val report = NovelizationProseQualityAuditService().audit(
            draftText = draftText,
            draftIntakeReport = NovelizationDraftIntakeReport(verdict = "PASS"),
        )

        assertEquals("PASS", report.verdict)
        assertTrue(report.findings.none { it.ruleId == "SCENE_PRESSURE_MISSING" })
        assertTrue(report.evidence.sentenceCount >= 6)
        assertTrue(report.evidence.dialogueLineCount >= 2)
    }

    @Test
    fun chineseAiStyleExpositionSceneFailsAudit() {
        val draftText = """
            這座城藏著古老秘密，命運的真相在不可名狀的神秘力量中翻湧。

            「請解釋整個系統，告訴我這個世界的真相和真正的意義。」
        """.trimIndent()

        val report = NovelizationProseQualityAuditService().audit(
            draftText = draftText,
            draftIntakeReport = NovelizationDraftIntakeReport(verdict = "PASS"),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "GENERIC_ABSTRACTION_CLUSTER" && it.severity == "MAJOR" })
        assertTrue(report.findings.any { it.ruleId == "DIALOGUE_EXPOSITION_DUMP" && it.severity == "MAJOR" })
        assertTrue(report.findings.any { it.ruleId == "SCENE_PRESSURE_MISSING" && it.severity == "MAJOR" })
        assertTrue(report.evidence.sentenceCount >= 2)
    }

    @Test
    fun cliWritesProseQualityAuditReportWithoutDatabase() {
        val tempDir = Files.createTempDirectory("prose-quality-audit")
        val draftIntakeReportPath = tempDir.resolve("draft_intake_report.json")
        val out = tempDir.resolve("prose_quality_report.json")
        writeJson(draftIntakeReportPath, NovelizationDraftIntakeReport(verdict = "PASS"))

        val exit = Cli().run(
            listOf(
                "audit-novelization-prose-quality",
                "--draft",
                samplePath("novelization_draft_intake_sample.md").toString(),
                "--draft-intake-report",
                draftIntakeReportPath.toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<NovelizationProseQualityAuditReport>(out)
        assertEquals("PASS", report.verdict)
        assertTrue(report.draftFile.endsWith("novelization_draft_intake_sample.md"))
    }

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
