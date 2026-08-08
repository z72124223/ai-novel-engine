package com.localnovel.engine

import com.localnovel.engine.model.NovelizationDraftIntakeReport
import com.localnovel.engine.model.NovelizationWriterBrief
import com.localnovel.engine.model.NovelizationWriterBriefValidationReport
import com.localnovel.engine.service.NovelizationDraftIntakeValidationService
import com.localnovel.engine.service.NovelizationWriterBriefValidationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NovelizationDraftIntakeValidationTest {
    @Test
    fun sampleDraftWithPassingWriterBriefPassesIntake() {
        val draftText = Files.readString(samplePath("novelization_draft_intake_sample.md"))
        val writerBriefReport = passingWriterBriefReport()

        val report = NovelizationDraftIntakeValidationService().validate(
            draftText = draftText,
            writerBriefReport = writerBriefReport,
            sourceType = "manual",
            sourceLabel = "sample-draft",
        )

        assertEquals("PASS", report.verdict)
        assertEquals("PASS", report.evidence.writerBriefVerdict)
        assertTrue(report.evidence.draftCharacters > 0)
        assertTrue(report.findings.isEmpty())
    }

    @Test
    fun failedWriterBriefBlocksDraftIntake() {
        val draftText = Files.readString(samplePath("novelization_draft_intake_sample.md"))
        val writerBriefReport = passingWriterBriefReport().copy(
            verdict = "FAIL",
            findingCount = 1,
        )

        val report = NovelizationDraftIntakeValidationService().validate(
            draftText = draftText,
            writerBriefReport = writerBriefReport,
            sourceType = "manual",
            sourceLabel = "sample-draft",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "WRITER_BRIEF_FAILED" && it.severity == "CRITICAL" })
    }

    @Test
    fun cliWritesDraftIntakeReportWithoutDatabase() {
        val tempDir = Files.createTempDirectory("draft-intake")
        val writerBriefReportPath = tempDir.resolve("writer_brief_report.json")
        val out = tempDir.resolve("draft_intake_report.json")
        writeJson(writerBriefReportPath, passingWriterBriefReport())

        val exit = Cli().run(
            listOf(
                "validate-novelization-draft-intake",
                "--draft",
                samplePath("novelization_draft_intake_sample.md").toString(),
                "--writer-brief-report",
                writerBriefReportPath.toString(),
                "--source-type",
                "manual",
                "--source-label",
                "sample-draft",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<NovelizationDraftIntakeReport>(out)
        assertEquals("PASS", report.verdict)
        assertTrue(report.draftFile.endsWith("novelization_draft_intake_sample.md"))
    }

    private fun passingWriterBriefReport(): NovelizationWriterBriefValidationReport {
        val brief = readJson<NovelizationWriterBrief>(samplePath("novelization_writer_brief_sample.json"))
        return NovelizationWriterBriefValidationService().validate(brief)
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
