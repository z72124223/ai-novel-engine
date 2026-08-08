package com.localnovel.engine

import com.localnovel.engine.model.CodexProductionHandoffDraft
import com.localnovel.engine.model.GrokStoryHandoff
import com.localnovel.engine.model.GrokStoryHandoffValidationReport
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GrokCodexHandoffCliTest {
    @Test
    fun cliCreatesValidatesAndDraftsCodexProductionHandoff() {
        val tempDir = Files.createTempDirectory("grok-codex-handoff-cli")
        val template = tempDir.resolve("grok_story_handoff.json")
        val validation = tempDir.resolve("grok_story_handoff_validation.json")
        val draft = tempDir.resolve("codex_production_handoff.json")

        val createExit = Cli().run(
            listOf(
                "create-grok-story-handoff-template",
                "--project",
                "chenxing",
                "--handoff",
                "grok-handoff-cli-test-001",
                "--out",
                template.toString(),
            ),
        )
        assertEquals(0, createExit)
        assertTrue(Files.exists(template))
        assertEquals("grok-handoff-cli-test-001", readJson<GrokStoryHandoff>(template).handoffId)

        val validateExit = Cli().run(
            listOf(
                "validate-grok-story-handoff",
                "--input",
                template.toString(),
                "--out",
                validation.toString(),
            ),
        )
        assertEquals(0, validateExit)
        val report = readJson<GrokStoryHandoffValidationReport>(validation)
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("codex_production_handoff_draft", report.nextAllowedDownstreamStep)

        val draftExit = Cli().run(
            listOf(
                "draft-codex-production-handoff",
                "--grok-handoff",
                template.toString(),
                "--validation",
                validation.toString(),
                "--out",
                draft.toString(),
            ),
        )
        assertEquals(0, draftExit)
        val handoffDraft = readJson<CodexProductionHandoffDraft>(draft)
        assertEquals("CODEX_PRODUCTION_HANDOFF_READY", handoffDraft.status)
        assertEquals(1, handoffDraft.sceneSummaries.size)
        assertEquals(1, handoffDraft.shotCandidates.size)
        assertEquals(1, handoffDraft.imagePromptDrafts.size)
        assertEquals(4, handoffDraft.referenceChecks.size)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
}
