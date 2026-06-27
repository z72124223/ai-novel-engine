package com.localnovel.engine

import com.localnovel.engine.model.ChapterContinuityAnchorFile
import com.localnovel.engine.model.ChapterContinuityAuditReport
import com.localnovel.engine.service.ChapterContinuityAuditService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChapterContinuityAuditTest {
    @Test
    fun sampleContinuityAnchorsPassWhenDraftAdvancesState() {
        val anchors = readJson<ChapterContinuityAnchorFile>(samplePath("chapter_continuity_anchor_sample.json"))
        val previous = "The sealed door opened after Mira paid the toll."
        val draft = "Mira stepped through the opened door and marked the debt in her notebook."

        val report = ChapterContinuityAuditService().audit(previous, draft, anchors)

        assertEquals("PASS", report.verdict)
        assertTrue(report.findings.isEmpty())
    }

    @Test
    fun forbiddenRegressionPhraseFailsDraft() {
        val anchors = readJson<ChapterContinuityAnchorFile>(samplePath("chapter_continuity_anchor_sample.json"))
        val previous = "The sealed door opened after Mira paid the toll."
        val draft = """
            Mira found the sealed door for the first time.
            Mira stepped through the opened door and marked the debt in her notebook.
        """.trimIndent()

        val report = ChapterContinuityAuditService().audit(previous, draft, anchors)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "DRAFT_CONTAINS_FORBIDDEN_REGRESSION" })
    }

    @Test
    fun replacementCharacterFailsDraftIntegrity() {
        val anchors = readJson<ChapterContinuityAnchorFile>(samplePath("chapter_continuity_anchor_sample.json"))
        val previous = "The sealed door opened after Mira paid the toll."
        val draft = "Mira stepped through the opened door and touched the stone�ite mark."

        val report = ChapterContinuityAuditService().audit(previous, draft, anchors)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "DRAFT_CONTAINS_REPLACEMENT_CHARACTER" })
        assertEquals(1, report.evidence.replacementCharacterCount)
    }

    @Test
    fun cliWritesChapterContinuityReportWithoutDatabase() {
        val tempDir = Files.createTempDirectory("chapter-continuity-audit")
        val previous = tempDir.resolve("previous.txt")
        val draft = tempDir.resolve("draft.txt")
        val out = tempDir.resolve("report.json")
        Files.writeString(previous, "The sealed door opened after Mira paid the toll.")
        Files.writeString(draft, "Mira stepped through the opened door and marked the debt in her notebook.")

        val exit = Cli().run(
            listOf(
                "audit-chapter-continuity",
                "--previous",
                previous.toString(),
                "--draft",
                draft.toString(),
                "--anchors",
                samplePath("chapter_continuity_anchor_sample.json").toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<ChapterContinuityAuditReport>(out)
        assertEquals("PASS", report.verdict)
        assertTrue(report.previousFile.endsWith("previous.txt"))
        assertTrue(report.draftFile.endsWith("draft.txt"))
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private fun samplePath(fileName: String): Path {
        val candidates = listOf(
            Path.of("samples", fileName),
            Path.of("..", "samples", fileName),
        )
        return candidates.firstOrNull { Files.exists(it) } ?: error("Cannot find sample file: $fileName")
    }
}
