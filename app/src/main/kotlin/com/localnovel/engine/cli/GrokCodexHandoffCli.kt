package com.localnovel.engine.cli

import com.localnovel.engine.model.CodexProductionHandoffDraft
import com.localnovel.engine.model.GrokStoryHandoff
import com.localnovel.engine.model.GrokStoryHandoffValidationReport
import com.localnovel.engine.service.CodexProductionHandoffDraftService
import com.localnovel.engine.service.GrokStoryHandoffTemplateService
import com.localnovel.engine.service.GrokStoryHandoffValidationService
import java.nio.file.Path

internal object GrokCodexHandoffCli {
    fun createGrokStoryHandoffTemplate(options: Map<String, String>): Int {
        val outPath = Path.of(options.requiredCliOption("out"))
        val handoff = GrokStoryHandoffTemplateService().create(
            handoffId = options["handoff"] ?: "",
            projectId = options["project"] ?: "chenxing",
            title = options["title"] ?: "Grok story handoff template",
            locale = options["locale"] ?: "zh-TW",
        )
        writeCliJson(outPath, handoff)
        println("handoffId=${handoff.handoffId}")
        println("projectId=${handoff.projectId}")
        println("title=${handoff.title}")
        println("storyChunks=${handoff.storyChunks.size}")
        println("productionRequests=${handoff.productionRequests.size}")
        println("referenceRequirements=${handoff.referenceRequirements.size}")
        println("returnToGrok=${handoff.returnToGrok.size}")
        println("out=$outPath")
        return 0
    }

    fun validateGrokStoryHandoff(options: Map<String, String>): Int {
        val inputPath = Path.of(options.requiredCliOption("input"))
        val outPath = Path.of(options.requiredCliOption("out"))
        val report = GrokStoryHandoffValidationService().validate(
            handoff = readCliJson<GrokStoryHandoff>(inputPath),
            handoffFile = inputPath.toString(),
            validationId = options["validation"] ?: "",
        )
        writeCliJson(outPath, report)
        println("verdict=${report.verdict}")
        println("validationId=${report.validationId}")
        println("handoffId=${report.handoffId}")
        println("projectId=${report.projectId}")
        println("storyChunks=${report.evidence.storyChunkCount}")
        println("productionRequests=${report.evidence.productionRequestCount}")
        println("referenceRequirements=${report.evidence.referenceRequirementCount}")
        println("returnToGrok=${report.evidence.returnToGrokCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun draftCodexProductionHandoff(options: Map<String, String>): Int {
        val grokHandoffPath = Path.of(options.requiredCliOption("grok-handoff"))
        val validationPath = Path.of(options.requiredCliOption("validation"))
        val outPath = Path.of(options.requiredCliOption("out"))
        val draft = CodexProductionHandoffDraftService().create(
            grokHandoff = readCliJson<GrokStoryHandoff>(grokHandoffPath),
            validationReport = readCliJson<GrokStoryHandoffValidationReport>(validationPath),
            grokHandoffFile = grokHandoffPath.toString(),
            validationReportFile = validationPath.toString(),
            draftId = options["draft"] ?: "",
        )
        writeCliJson(outPath, draft)
        printDraft(draft)
        return if (draft.status == "FAIL") 1 else 0
    }

    private fun printDraft(draft: CodexProductionHandoffDraft) {
        println("status=${draft.status}")
        println("draftId=${draft.draftId}")
        println("sourceHandoffId=${draft.sourceHandoffId}")
        println("projectId=${draft.projectId}")
        println("sceneSummaries=${draft.sceneSummaries.size}")
        println("shotCandidates=${draft.shotCandidates.size}")
        println("imagePromptDrafts=${draft.imagePromptDrafts.size}")
        println("referenceChecks=${draft.referenceChecks.size}")
        println("returnToGrok=${draft.returnToGrok.size}")
        println("nextAllowedDownstreamStep=${draft.nextAllowedDownstreamStep}")
        println("blockedReason=${draft.blockedReason}")
        println("findings=${draft.findingCount}")
        draft.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
    }
}
