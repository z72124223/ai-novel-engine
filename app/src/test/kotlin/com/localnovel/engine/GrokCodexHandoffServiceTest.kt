package com.localnovel.engine

import com.localnovel.engine.model.GrokCodexBoundaryFlags
import com.localnovel.engine.model.ReturnToGrokRequest
import com.localnovel.engine.service.CodexProductionHandoffDraftService
import com.localnovel.engine.service.GrokStoryHandoffTemplateService
import com.localnovel.engine.service.GrokStoryHandoffValidationService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GrokCodexHandoffServiceTest {
    @Test
    fun validatesTemplateHandoff() {
        val handoff = GrokStoryHandoffTemplateService().create(projectId = "chenxing")
        val report = GrokStoryHandoffValidationService().validate(
            handoff = handoff,
            handoffFile = "samples/grok_codex_handoff/template.json",
        )

        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("codex_production_handoff_draft", report.nextAllowedDownstreamStep)
        assertEquals(1, report.evidence.storyChunkCount)
        assertEquals(2, report.evidence.productionRequestCount)
        assertEquals(4, report.evidence.referenceRequirementCount)
    }

    @Test
    fun productionDraftCarriesReturnToGrokWithoutRewritingStory() {
        val handoff = GrokStoryHandoffTemplateService().create(projectId = "chenxing").copy(
            returnToGrok = listOf(
                ReturnToGrokRequest(
                    requestId = "return-001",
                    source = "grok-output-001",
                    location = "scene 2 / paragraph 4",
                    reason = "Codex cannot process this story section.",
                    neededFromGrok = "Rewrite or complete the section in Grok before Codex continues.",
                    codexCanContinueAfter = "Grok provides a safe scene summary and visual intent.",
                ),
            ),
        )
        val report = GrokStoryHandoffValidationService().validate(handoff)
        val draft = CodexProductionHandoffDraftService().create(
            grokHandoff = handoff,
            validationReport = report,
            grokHandoffFile = "grok.json",
            validationReportFile = "validation.json",
        )

        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("CODEX_PRODUCTION_HANDOFF_READY_WITH_RETURN_TO_GROK", draft.status)
        assertEquals(1, draft.returnToGrok.size)
        assertTrue(draft.returnToGrok.first().formattedBlock.contains("[RETURN_TO_GROK]"))
        assertTrue(draft.returnToGrok.first().formattedBlock.contains("Needed from Grok"))
        assertEquals(1, draft.sceneSummaries.size)
        assertEquals(1, draft.imagePromptDrafts.size)
    }

    @Test
    fun unsafeBoundaryFailsValidation() {
        val handoff = GrokStoryHandoffTemplateService().create(projectId = "chenxing").copy(
            boundaries = GrokCodexBoundaryFlags(noProviderApiCalls = false),
        )
        val report = GrokStoryHandoffValidationService().validate(handoff)

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "GROK_BOUNDARY_PROVIDER_API_FALSE" })
        assertEquals("", report.nextAllowedDownstreamStep)
    }
}
