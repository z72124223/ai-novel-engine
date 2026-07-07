package com.localnovel.engine.service

import com.localnovel.engine.model.LocalAssetPromptCardsDraft
import com.localnovel.engine.model.LocalAssetPromptCardsDraftFinding
import com.localnovel.engine.model.LocalRenderPackageDraft
import com.localnovel.engine.model.LocalRenderPackageDraftFinding
import com.localnovel.engine.model.LocalStoryInputPack
import com.localnovel.engine.model.LocalStoryInputPackValidationFinding
import com.localnovel.engine.model.LocalStoryInputPackValidationReport
import com.localnovel.engine.model.LocalStoryVideoChainBoundaries
import com.localnovel.engine.model.LocalStoryVideoChainCounts
import com.localnovel.engine.model.LocalStoryVideoChainFinding
import com.localnovel.engine.model.LocalStoryVideoChainGeneratedFiles
import com.localnovel.engine.model.LocalStoryVideoChainReport
import com.localnovel.engine.model.LocalStoryVideoChainStageResult
import com.localnovel.engine.model.LocalStoryVideoChainStageResults
import java.nio.file.Path

class LocalStoryVideoChainVerificationService {
    private val verifiedStatus = "LOCAL_STORY_VIDEO_CHAIN_VERIFIED"
    private val cardsDraftStatus = "LOCAL_ASSET_PROMPT_CARDS_DRAFT"
    private val renderPackageDraftStatus = "LOCAL_RENDER_PACKAGE_DRAFT"
    private val downstreamBoundary = "local_story_video_chain_verified_only_not_media_generation"
    private val nextAllowedDownstreamStep = "local_chain_review"

    fun verify(
        storyInputPack: LocalStoryInputPack,
        storyInputPackFile: String = "",
        outputDir: String = "",
        chainReportFile: String = "",
        chainId: String = "",
    ): LocalStoryVideoChainVerificationOutput {
        val resolvedChainId = chainId.ifBlank {
            storyInputPack.packId.takeIf { it.isNotBlank() }?.let { "$it-story-video-chain-v0" }.orEmpty()
        }
        val generatedFiles = generatedFiles(outputDir, chainReportFile)
        val findings = mutableListOf<LocalStoryVideoChainFinding>()

        val validation = LocalStoryInputPackValidationService().validate(
            pack = storyInputPack,
            packFile = storyInputPackFile,
            validationId = storyInputPack.packId.takeIf { it.isNotBlank() }?.let { "$it-chain-validation-v0" }.orEmpty(),
        )
        val storyStage = stage(
            stageName = "storyValidation",
            status = validation.verdict,
            outputFile = generatedFiles.storyValidationReportFile,
            summary = "story validation verdict=${validation.verdict}",
            findingCount = validation.findingCount,
        )
        if (validation.verdict != "PASS") {
            findings += finding(
                "LOCAL_CHAIN_STORY_VALIDATION_FAILED",
                "story_validation",
                "CRITICAL",
                "$.stageResults.storyValidation",
                "Local story video chain stops because story validation is not PASS.",
                listOf(validation.verdict),
                "Fix the story input pack and rerun the chain.",
            )
            findings += validation.findings.map(::fromStoryFinding)
            return output(
                storyInputPack = storyInputPack,
                chainId = resolvedChainId,
                storyInputPackFile = storyInputPackFile,
                outputDir = outputDir,
                generatedFiles = generatedFiles,
                storyStage = storyStage,
                cardsStage = skippedStage("assetPromptCards", generatedFiles.assetPromptCardsFile, "skipped because story validation failed"),
                renderStage = skippedStage("localRenderPackage", generatedFiles.localRenderPackageFile, "skipped because story validation failed"),
                validation = validation,
                cards = null,
                renderPackage = null,
                findings = findings,
            )
        }

        val cards = LocalAssetPromptCardDraftService().create(
            storyInputPack = storyInputPack,
            storyValidationReport = validation,
            storyInputPackFile = storyInputPackFile,
            storyValidationReportFile = generatedFiles.storyValidationReportFile,
            draftId = storyInputPack.packId.takeIf { it.isNotBlank() }?.let { "$it-chain-asset-prompt-cards-v0" }.orEmpty(),
        )
        val cardsStage = stage(
            stageName = "assetPromptCards",
            status = cards.status,
            outputFile = generatedFiles.assetPromptCardsFile,
            summary = "asset/prompt cards status=${cards.status}",
            findingCount = cards.findingCount,
        )
        if (cards.status != cardsDraftStatus) {
            findings += finding(
                "LOCAL_CHAIN_ASSET_PROMPT_CARDS_FAILED",
                "asset_prompt_cards",
                "CRITICAL",
                "$.stageResults.assetPromptCards",
                "Local story video chain stops because asset/prompt cards draft status is not LOCAL_ASSET_PROMPT_CARDS_DRAFT.",
                listOf(cards.status),
                "Fix the story input pack/card drafting findings and rerun the chain.",
            )
            findings += cards.findings.map(::fromCardsFinding)
            return output(
                storyInputPack = storyInputPack,
                chainId = resolvedChainId,
                storyInputPackFile = storyInputPackFile,
                outputDir = outputDir,
                generatedFiles = generatedFiles,
                storyStage = storyStage,
                cardsStage = cardsStage,
                renderStage = skippedStage("localRenderPackage", generatedFiles.localRenderPackageFile, "skipped because asset/prompt cards failed"),
                validation = validation,
                cards = cards,
                renderPackage = null,
                findings = findings,
            )
        }

        val renderPackage = LocalRenderPackageDraftService().create(
            storyInputPack = storyInputPack,
            assetPromptCards = cards,
            storyInputPackFile = storyInputPackFile,
            assetPromptCardsFile = generatedFiles.assetPromptCardsFile,
            packageId = storyInputPack.packId.takeIf { it.isNotBlank() }?.let { "$it-chain-local-render-package-v0" }.orEmpty(),
        )
        val renderStage = stage(
            stageName = "localRenderPackage",
            status = renderPackage.status,
            outputFile = generatedFiles.localRenderPackageFile,
            summary = "local render package status=${renderPackage.status}",
            findingCount = renderPackage.findingCount,
        )
        if (renderPackage.status != renderPackageDraftStatus) {
            findings += finding(
                "LOCAL_CHAIN_RENDER_PACKAGE_FAILED",
                "local_render_package",
                "CRITICAL",
                "$.stageResults.localRenderPackage",
                "Local story video chain stops because render package status is not LOCAL_RENDER_PACKAGE_DRAFT.",
                listOf(renderPackage.status),
                "Fix render package findings and rerun the chain.",
            )
            findings += renderPackage.findings.map(::fromRenderFinding)
        }

        return output(
            storyInputPack = storyInputPack,
            chainId = resolvedChainId,
            storyInputPackFile = storyInputPackFile,
            outputDir = outputDir,
            generatedFiles = generatedFiles,
            storyStage = storyStage,
            cardsStage = cardsStage,
            renderStage = renderStage,
            validation = validation,
            cards = cards,
            renderPackage = renderPackage,
            findings = findings,
        )
    }

    private fun output(
        storyInputPack: LocalStoryInputPack,
        chainId: String,
        storyInputPackFile: String,
        outputDir: String,
        generatedFiles: LocalStoryVideoChainGeneratedFiles,
        storyStage: LocalStoryVideoChainStageResult,
        cardsStage: LocalStoryVideoChainStageResult,
        renderStage: LocalStoryVideoChainStageResult,
        validation: LocalStoryInputPackValidationReport,
        cards: LocalAssetPromptCardsDraft?,
        renderPackage: LocalRenderPackageDraft?,
        findings: List<LocalStoryVideoChainFinding>,
    ): LocalStoryVideoChainVerificationOutput {
        val success = validation.verdict == "PASS" &&
            cards?.status == cardsDraftStatus &&
            renderPackage?.status == renderPackageDraftStatus &&
            findings.none { it.severity == "CRITICAL" || it.severity == "MAJOR" }
        val report = LocalStoryVideoChainReport(
            status = if (success) verifiedStatus else "FAIL",
            chainId = chainId,
            sourceStoryInputPackFile = storyInputPackFile,
            outputDir = outputDir,
            sourceStoryInputPackId = storyInputPack.packId,
            stageResults = LocalStoryVideoChainStageResults(
                storyValidation = storyStage,
                assetPromptCards = cardsStage,
                localRenderPackage = renderStage,
            ),
            generatedFiles = generatedFiles,
            counts = counts(storyInputPack, cards, renderPackage),
            boundaries = boundaries(storyInputPack, cards),
            downstreamBoundary = downstreamBoundary,
            nextAllowedDownstreamStep = if (success) nextAllowedDownstreamStep else "",
            blockedReason = if (success) "" else findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId },
            findingCount = findings.size,
            findings = findings,
        )
        return LocalStoryVideoChainVerificationOutput(
            report = report,
            storyValidationReport = validation,
            assetPromptCardsDraft = cards,
            localRenderPackageDraft = renderPackage,
        )
    }

    private fun generatedFiles(outputDir: String, chainReportFile: String): LocalStoryVideoChainGeneratedFiles {
        val dir = Path.of(outputDir)
        return LocalStoryVideoChainGeneratedFiles(
            storyValidationReportFile = dir.resolve("local_story_input_pack_validation_report.json").toString(),
            assetPromptCardsFile = dir.resolve("local_asset_prompt_cards_draft.json").toString(),
            localRenderPackageFile = dir.resolve("local_render_package_draft.json").toString(),
            chainReportFile = chainReportFile,
        )
    }

    private fun counts(
        storyInputPack: LocalStoryInputPack,
        cards: LocalAssetPromptCardsDraft?,
        renderPackage: LocalRenderPackageDraft?,
    ): LocalStoryVideoChainCounts =
        LocalStoryVideoChainCounts(
            characters = storyInputPack.characters.size,
            scenes = storyInputPack.scenes.size,
            beats = storyInputPack.beats.size,
            visualPromptCards = cards?.visualPromptCards?.size ?: 0,
            audioCueCards = cards?.audioCueCards?.size ?: 0,
            panelPlans = renderPackage?.panelPlan?.size ?: 0,
            qaChecks = renderPackage?.qaChecklist?.size ?: 0,
            qaPass = renderPackage?.evidence?.qaPassCount ?: 0,
            qaFail = renderPackage?.evidence?.qaFailCount ?: 0,
            targetDurationMillis = storyInputPack.durationTargetMillis,
        )

    private fun boundaries(
        storyInputPack: LocalStoryInputPack,
        cards: LocalAssetPromptCardsDraft?,
    ): LocalStoryVideoChainBoundaries =
        LocalStoryVideoChainBoundaries(
            fileProtocolJsonOnly = storyInputPack.constraints.fileProtocolJsonOnly &&
                (cards?.localOnlyConstraints?.fileProtocolJsonOnly ?: true),
            noExternalPaidProviderTools = storyInputPack.constraints.noExternalPaidProviderTools &&
                (cards?.localOnlyConstraints?.noExternalPaidProviderTools ?: true),
            providerInvocation = if (cards == null || cards.visualPromptCards.all { it.providerInvocation == "none" }) "none" else "present",
            ttsInvocation = if (cards == null || cards.audioCueCards.all { it.ttsInvocation == "none" }) "none" else "present",
            mediaGenerated = false,
            rendererInvocation = "none",
            directSqliteWrites = false,
            providerApiCalls = false,
            finalMediaApproval = false,
            release = false,
            canonRegistryOrProductionLock = false,
        )

    private fun stage(
        stageName: String,
        status: String,
        outputFile: String,
        summary: String,
        findingCount: Int,
    ): LocalStoryVideoChainStageResult =
        LocalStoryVideoChainStageResult(stageName, status, outputFile, summary, findingCount)

    private fun skippedStage(stageName: String, outputFile: String, summary: String): LocalStoryVideoChainStageResult =
        LocalStoryVideoChainStageResult(stageName = stageName, status = "SKIPPED", outputFile = outputFile, summary = summary)

    private fun fromStoryFinding(finding: LocalStoryInputPackValidationFinding): LocalStoryVideoChainFinding =
        finding(
            ruleId = "STORY_${finding.ruleId}",
            category = finding.category,
            severity = finding.severity,
            path = finding.path,
            message = finding.message,
            evidence = finding.evidence,
            suggestedFix = finding.suggestedFix,
        )

    private fun fromCardsFinding(finding: LocalAssetPromptCardsDraftFinding): LocalStoryVideoChainFinding =
        finding(
            ruleId = "CARDS_${finding.ruleId}",
            category = finding.category,
            severity = finding.severity,
            path = finding.path,
            message = finding.message,
            evidence = finding.evidence,
            suggestedFix = finding.suggestedFix,
        )

    private fun fromRenderFinding(finding: LocalRenderPackageDraftFinding): LocalStoryVideoChainFinding =
        finding(
            ruleId = "RENDER_${finding.ruleId}",
            category = finding.category,
            severity = finding.severity,
            path = finding.path,
            message = finding.message,
            evidence = finding.evidence,
            suggestedFix = finding.suggestedFix,
        )

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): LocalStoryVideoChainFinding =
        LocalStoryVideoChainFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
}

data class LocalStoryVideoChainVerificationOutput(
    val report: LocalStoryVideoChainReport,
    val storyValidationReport: LocalStoryInputPackValidationReport,
    val assetPromptCardsDraft: LocalAssetPromptCardsDraft?,
    val localRenderPackageDraft: LocalRenderPackageDraft?,
)
