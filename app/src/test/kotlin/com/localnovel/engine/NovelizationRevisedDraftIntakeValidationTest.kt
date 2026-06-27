package com.localnovel.engine

import com.localnovel.engine.model.NovelizationRevisedDraftIntakeReport
import com.localnovel.engine.model.NovelizationRevisionBriefReport
import com.localnovel.engine.model.NovelizationRevisionFixHandling
import com.localnovel.engine.model.NovelizationRevisionHandlingDeclaration
import com.localnovel.engine.model.NovelizationRevisionItem
import com.localnovel.engine.model.NovelizationRevisionChecklistHandling
import com.localnovel.engine.service.NovelizationRevisedDraftIntakeValidationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NovelizationRevisedDraftIntakeValidationTest {
    @Test
    fun handledRevisedDraftPassesIntake() {
        val report = NovelizationRevisedDraftIntakeValidationService().validate(
            originalDraftText = originalDraft(),
            revisedDraftText = revisedDraft(),
            revisionBriefReport = revisionBrief(),
            declaredHandling = handlingDeclaration(),
        )

        assertEquals("PASS", report.verdict)
        assertEquals(0, report.evidence.bannedResidualCount)
        assertEquals(2, report.evidence.continuityAnchorPreservedCount)
        assertEquals(6, report.evidence.declaredChecklistCount)
        assertEquals(1, report.evidence.declaredFixCount)
    }

    @Test
    fun unchangedRevisedDraftFailsIntake() {
        val report = NovelizationRevisedDraftIntakeValidationService().validate(
            originalDraftText = originalDraft(),
            revisedDraftText = originalDraft(),
            revisionBriefReport = revisionBrief(),
            declaredHandling = handlingDeclaration(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "REVISED_DRAFT_UNCHANGED" && it.severity == "CRITICAL" })
    }

    @Test
    fun missingDeclaredHandlingFailsWhenBriefRequiresFixes() {
        val report = NovelizationRevisedDraftIntakeValidationService().validate(
            originalDraftText = originalDraft(),
            revisedDraftText = revisedDraft(),
            revisionBriefReport = revisionBrief(),
            declaredHandling = NovelizationRevisionHandlingDeclaration(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "DECLARED_HANDLING_FILE_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "MANDATORY_CHECKLIST_HANDLING_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "REQUIRED_FIX_HANDLING_MISSING" })
    }

    @Test
    fun removedContinuityAnchorFailsIntake() {
        val report = NovelizationRevisedDraftIntakeValidationService().validate(
            originalDraftText = originalDraft(),
            revisedDraftText = "Mira waited under the rain until someone opened a side gate.",
            revisionBriefReport = revisionBrief(),
            declaredHandling = handlingDeclaration(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "CONTINUITY_ANCHOR_NOT_PRESERVED" })
    }

    @Test
    fun damagedPaidContinuityAnchorFailsEvenWithTermOverlap() {
        val report = NovelizationRevisedDraftIntakeValidationService().validate(
            originalDraftText = originalDraft(),
            revisedDraftText = """
                Mira had not paid the door toll before reaching the checkpoint.
                The guard requires a visible mark before answering questions.
                She asked what the delay would cost.
            """.trimIndent(),
            revisionBriefReport = revisionBrief(),
            declaredHandling = handlingDeclaration(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "CONTINUITY_ANCHOR_DAMAGED" })
    }

    @Test
    fun notOnlyBannedResidualFailsIntake() {
        val report = NovelizationRevisedDraftIntakeValidationService().validate(
            originalDraftText = originalDraft(),
            revisedDraftText = """
                Mira pressed the paid door toll receipt flat against the checkpoint board.
                The guard kept his hand on the visible mark slot before answering questions.
                This was not only a delay, but also destiny testing her resolve.
            """.trimIndent(),
            revisionBriefReport = revisionBrief(),
            declaredHandling = handlingDeclaration(),
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "BANNED_PHRASE_RESIDUAL_CALIBRATION_EN_NOT_ONLY" })
    }

    @Test
    fun cliWritesRevisedDraftIntakeReportWithoutDatabase() {
        val tempDir = Files.createTempDirectory("revised-draft-intake")
        val originalPath = tempDir.resolve("original.md")
        val revisedPath = tempDir.resolve("revised.md")
        val briefPath = tempDir.resolve("revision_brief_report.json")
        val out = tempDir.resolve("revised_draft_intake_report.json")
        Files.writeString(originalPath, originalDraft())
        Files.writeString(revisedPath, revisedDraft())
        writeJson(briefPath, revisionBrief())

        val exit = Cli().run(
            listOf(
                "validate-novelization-revised-draft-intake",
                "--original-draft",
                originalPath.toString(),
                "--revised-draft",
                revisedPath.toString(),
                "--revision-brief-report",
                briefPath.toString(),
                "--declared-handling",
                samplePath("novelization_revision_handling_declaration_sample.json").toString(),
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<NovelizationRevisedDraftIntakeReport>(out)
        assertEquals("PASS", report.verdict)
        assertEquals("revision-brief-sample-001", report.revisionBriefId)
        assertEquals(0, report.evidence.bannedResidualCount)
        assertEquals(2, report.evidence.continuityAnchorPreservedCount)
    }

    private fun originalDraft(): String =
        """
        Mira already paid the door toll before reaching the checkpoint.
        This is not a setback, but destiny testing her.
        The guard requires a visible mark before answering questions.
        """.trimIndent()

    private fun revisedDraft(): String =
        """
        Mira pressed the paid door toll receipt flat against the checkpoint board.
        The guard kept his hand on the visible mark slot before answering questions.
        She did not argue about destiny. She asked what the delay would cost.
        """.trimIndent()

    private fun revisionBrief(): NovelizationRevisionBriefReport =
        NovelizationRevisionBriefReport(
            verdict = "PASS",
            briefId = "revision-brief-sample-001",
            revisionRequired = true,
            mandatoryChecklist = mandatoryChecklist(),
            requiredFixes = listOf(
                NovelizationRevisionItem(
                    itemId = "revision-item-001",
                    fixType = "banned_phrase_removal",
                    sourceRuleId = "BANNED_CALIBRATION_STRUCTURE_CALIBRATION_EN_NOT_BUT",
                    category = "anti_ai_prose",
                    severity = "CRITICAL",
                    instruction = "Remove banned phrasing.",
                    evidence = listOf("This is not a setback, but destiny testing her."),
                    suggestedFix = "Use concrete pressure instead.",
                ),
            ),
            continuityAnchors = listOf(
                "Mira already paid the door toll before reaching the checkpoint.",
                "The guard requires a visible mark before answering questions.",
            ),
            doNotChange = listOf("Do not reset the toll payment as unpaid."),
        )

    private fun handlingDeclaration(): NovelizationRevisionHandlingDeclaration =
        NovelizationRevisionHandlingDeclaration(
            declarationId = "novelization-revision-handling-declaration-test-001",
            revisionBriefId = "revision-brief-sample-001",
            handledChecklist = mandatoryChecklist().map {
                NovelizationRevisionChecklistHandling(
                    checklistItem = it,
                    status = "HANDLED",
                    note = "Handled in revised draft.",
                )
            },
            handledFixes = listOf(
                NovelizationRevisionFixHandling(
                    itemId = "revision-item-001",
                    status = "HANDLED",
                    note = "Removed banned calibration structure.",
                ),
            ),
        )

    private fun mandatoryChecklist(): List<String> =
        listOf(
            "ai_taste_revision",
            "dialogue_revision",
            "character_voice_revision",
            "scene_pressure_reinforcement",
            "banned_phrase_removal",
            "continuity_anchor_protection",
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
