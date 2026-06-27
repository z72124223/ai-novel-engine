package com.localnovel.engine

import com.localnovel.engine.model.NovelizationWriterBrief
import com.localnovel.engine.model.NovelizationWriterBriefValidationReport
import com.localnovel.engine.model.NovelizationPreflightReport
import com.localnovel.engine.service.NovelizationWriterBriefValidationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NovelizationWriterBriefValidationTest {
    @Test
    fun sampleWriterBriefPassesValidation() {
        val brief = readJson<NovelizationWriterBrief>(samplePath("novelization_writer_brief_sample.json"))

        val report = NovelizationWriterBriefValidationService().validate(brief)

        assertEquals("PASS", report.verdict)
        assertEquals("novelization-writer-brief-sample-001", report.briefId)
        assertTrue(report.findings.isEmpty())
    }

    @Test
    fun missingMustNotRepeatFailsValidation() {
        val brief = readJson<NovelizationWriterBrief>(samplePath("novelization_writer_brief_sample.json"))
        val broken = brief.copy(mustNotRepeat = emptyList())

        val report = NovelizationWriterBriefValidationService().validate(broken)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "MUST_NOT_REPEAT_EMPTY" && it.severity == "MAJOR" })
    }

    @Test
    fun cliValidatesWriterBriefWithoutDatabase() {
        val out = Files.createTempDirectory("writer-brief-validation").resolve("report.json")

        val exit = Cli().run(
            listOf(
                "validate-novelization-writer-brief",
                "--brief",
                samplePath("novelization_writer_brief_sample.json").toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<NovelizationWriterBriefValidationReport>(out)
        assertEquals("PASS", report.verdict)
        assertTrue(report.briefFile.endsWith("novelization_writer_brief_sample.json"))
    }

    @Test
    fun cliCreatesWriterBriefTemplateWithoutDatabase() {
        val out = Files.createTempDirectory("writer-brief-template").resolve("writer_brief.json")

        val exit = Cli().run(
            listOf(
                "create-novelization-writer-brief-template",
                "--project",
                "demo-project",
                "--chapter",
                "chapter-003",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val brief = readJson<NovelizationWriterBrief>(out)
        assertEquals("demo-project", brief.projectId)
        assertEquals("chapter-003", brief.targetChapter)
        assertTrue(brief.mustNotRepeat.isNotEmpty())
    }

    @Test
    fun cliFailsWriterBriefWhenLinkedPreflightFailed() {
        val tempDir = Files.createTempDirectory("writer-brief-preflight-link")
        val briefPath = tempDir.resolve("writer_brief.json")
        val preflightPath = tempDir.resolve("preflight_report.json")
        val out = tempDir.resolve("writer_brief_report.json")
        val brief = readJson<NovelizationWriterBrief>(samplePath("novelization_writer_brief_sample.json")).copy(
            sourcePreflightReport = preflightPath.toString(),
        )
        writeJson(briefPath, brief)
        writeJson(
            preflightPath,
            NovelizationPreflightReport(
                verdict = "FAIL",
                preflightId = "failed-preflight",
                findingCount = 1,
            ),
        )

        val exit = Cli().run(
            listOf(
                "validate-novelization-writer-brief",
                "--brief",
                briefPath.toString(),
                "--preflight-report",
                preflightPath.toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(1, exit)
        val report = readJson<NovelizationWriterBriefValidationReport>(out)
        assertEquals("FAIL", report.verdict)
        assertEquals("FAIL", report.evidence.sourcePreflightVerdict)
        assertTrue(report.findings.any { it.ruleId == "SOURCE_PREFLIGHT_FAILED" && it.severity == "CRITICAL" })
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
