package com.localnovel.engine.cli

import com.localnovel.engine.model.LocalAssetPromptCardsDraft
import com.localnovel.engine.model.LocalRenderPackageDraft
import com.localnovel.engine.model.LocalRendererAdapterDryRunReport
import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanDraft
import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanReviewReport
import com.localnovel.engine.model.LocalStoryInputPack
import com.localnovel.engine.model.LocalStoryInputPackValidationReport
import com.localnovel.engine.model.LocalStoryVideoChainReport
import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffDraft
import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffReviewReport
import com.localnovel.engine.service.FoundationCardPackReviewService
import com.localnovel.engine.service.FoundationCardPackValidationService
import com.localnovel.engine.service.LocalAssetPromptCardDraftService
import com.localnovel.engine.service.LocalRenderPackageDraftService
import com.localnovel.engine.service.LocalRendererAdapterDryRunService
import com.localnovel.engine.service.LocalRendererAdapterImplementationDraftReviewService
import com.localnovel.engine.service.LocalRendererAdapterImplementationDraftService
import com.localnovel.engine.service.LocalRendererAdapterImplementationPlanReviewService
import com.localnovel.engine.service.LocalRendererAdapterImplementationPlanService
import com.localnovel.engine.service.LocalRendererManifestAdapterContractDraftService
import com.localnovel.engine.service.LocalRendererManifestAdapterContractPlanReviewService
import com.localnovel.engine.service.LocalRendererManifestAdapterContractPlanService
import com.localnovel.engine.service.LocalRendererManifestDraftJsonAdapterDryRunService
import com.localnovel.engine.service.LocalRendererManifestDraftJsonDraftService
import com.localnovel.engine.service.LocalRendererManifestDraftJsonReviewService
import com.localnovel.engine.service.LocalRendererManifestDraftPlanReviewService
import com.localnovel.engine.service.LocalRendererManifestDraftPlanService
import com.localnovel.engine.service.LocalStoryInputPackValidationService
import com.localnovel.engine.service.LocalStoryVideoChainVerificationService
import com.localnovel.engine.service.LocalStoryVideoPreviewHandoffDraftService
import com.localnovel.engine.service.LocalStoryVideoPreviewHandoffReviewService
import java.nio.file.Path

internal object LocalStoryVideoCli {
    fun validateLocalStoryInputPack(options: Map<String, String>): Int {
        val inputPath = Path.of(options.requiredCliOption("input"))
        val report = LocalStoryInputPackValidationService().validate(
            pack = readCliJson<LocalStoryInputPack>(inputPath),
            packFile = inputPath.toString(),
            validationId = options["validation"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("validationId=${report.validationId}")
        println("packId=${report.packId}")
        println("title=${report.title}")
        println("locale=${report.locale}")
        println("characters=${report.evidence.characterCount}")
        println("scenes=${report.evidence.sceneCount}")
        println("beats=${report.evidence.beatCount}")
        println("linkedBeats=${report.evidence.linkedBeatCount}")
        println("durationTargetMillis=${report.evidence.durationTargetMillis}")
        println("localOnly=${report.evidence.localOnlyDeclared}")
        println("noExternalPaidProviderTools=${report.evidence.noExternalPaidProviderToolsDeclared}")
        println("fileProtocolJsonOnly=${report.evidence.fileProtocolJsonOnlyDeclared}")
        println("downstreamBoundary=${report.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun validate09FoundationCardPack(options: Map<String, String>): Int {
        val inputPath = Path.of(options.requiredCliOption("input"))
        val outPath = Path.of(options.requiredCliOption("out"))
        val report = FoundationCardPackValidationService().validateFile(
            inputPath = inputPath,
            outPath = outPath,
            validationId = options["validation"] ?: "",
        )
        val checksPass = report.checks.count { it.status == "PASS" }
        val checksFail = report.checks.count { it.status != "PASS" }
        println("verdict=${report.verdict}")
        println("status=${report.status}")
        println("validationId=${report.validationId}")
        println("packId=${report.packId}")
        println("title=${report.title}")
        println("locale=${report.locale}")
        println("cards=${report.evidence.cardCount}")
        println("requiredFamilies=${report.evidence.requiredFamilyCount}")
        println("presentRequiredFamilies=${report.evidence.presentRequiredFamilyCount}")
        println("missingFamilies=${report.evidence.missingFamilies.joinToString(",")}")
        println("duplicateCardIds=${report.evidence.duplicateCardIdCount}")
        println("missingSourceNotes=${report.evidence.missingSourceNotesCount}")
        println("missingRetrievalTags=${report.evidence.missingRetrievalTagsCount}")
        println("missingDownstreamUse=${report.evidence.missingDownstreamUseCount}")
        println("unsafeBoundaryFlags=${report.evidence.unsafeBoundaryFlagCount}")
        println("readinessClaims=${report.evidence.readinessClaimCount}")
        println("eligibilityClaims=${report.evidence.eligibilityClaimCount}")
        println("checks=${report.checks.size}")
        println("checksPass=$checksPass")
        println("checksFail=$checksFail")
        println("candidateOnly=${report.boundarySummary.candidateOnly}")
        println("canonApproved=${report.boundarySummary.canonApproved}")
        println("settingPackData=${report.boundarySummary.settingPackData}")
        println("runtimeSqliteData=${report.boundarySummary.runtimeSqliteData}")
        println("approvedAssetData=${report.boundarySummary.approvedAssetData}")
        println("providerApiCalls=${report.boundarySummary.providerApiCalls}")
        println("mediaGenerated=${report.boundarySummary.mediaGenerated}")
        println("finalMediaApproval=${report.boundarySummary.finalMediaApproval}")
        println("productionLock=${report.boundarySummary.productionLock}")
        println("allRendererInvocationNone=${report.boundarySummary.allRendererInvocationNone}")
        println("allFfmpegInvocationNone=${report.boundarySummary.allFfmpegInvocationNone}")
        println("allTtsInvocationNone=${report.boundarySummary.allTtsInvocationNone}")
        println("downstreamBoundary=${report.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("progressCaveat=${report.progressCaveat}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun review09FoundationCardPack(options: Map<String, String>): Int {
        val packPath = Path.of(options.requiredCliOption("pack"))
        val validationPath = Path.of(options.requiredCliOption("validation"))
        val outPath = Path.of(options.requiredCliOption("out"))
        val report = FoundationCardPackReviewService().reviewFile(
            packPath = packPath,
            validationPath = validationPath,
            outPath = outPath,
            reviewId = options["review"] ?: "",
        )
        val checksPass = report.checks.count { it.status == "PASS" }
        val checksFail = report.checks.count { it.status != "PASS" }
        println("verdict=${report.verdict}")
        println("status=${report.status}")
        println("reviewId=${report.reviewId}")
        println("sourceValidationId=${report.sourceValidationId}")
        println("packId=${report.packId}")
        println("title=${report.title}")
        println("locale=${report.locale}")
        println("sourceAligned=${report.evidence.sourceAligned}")
        println("sourcePackFileMatches=${report.evidence.sourcePackFileMatches}")
        println("packIdAligned=${report.evidence.packIdAligned}")
        println("titleAligned=${report.evidence.titleAligned}")
        println("localeAligned=${report.evidence.localeAligned}")
        println("cards=${report.evidence.cardCount}")
        println("requiredFamilies=${report.evidence.requiredFamilyCount}")
        println("presentRequiredFamilies=${report.evidence.presentRequiredFamilyCount}")
        println("missingFamilies=${report.evidence.missingFamilies.joinToString(",")}")
        println("validationFailedChecks=${report.evidence.validationFailedCheckCount}")
        println("validationFindings=${report.evidence.validationFindingCount}")
        println("reviewFailedChecks=${report.evidence.reviewFailedCheckCount}")
        println("duplicateCardIds=${report.evidence.duplicateCardIdCount}")
        println("missingTraceability=${report.evidence.missingTraceabilityCount}")
        println("missingDownstreamUse=${report.evidence.missingDownstreamUseCount}")
        println("unsafeBoundaryFlags=${report.evidence.unsafeBoundaryFlagCount}")
        println("readinessClaims=${report.evidence.readinessClaimCount}")
        println("eligibilityClaims=${report.evidence.eligibilityClaimCount}")
        println("checks=${report.checks.size}")
        println("checksPass=$checksPass")
        println("checksFail=$checksFail")
        println("technicalReviewOnly=${report.boundarySummary.technicalReviewOnly}")
        println("candidateOnly=${report.boundarySummary.candidateOnly}")
        println("validationPass=${report.boundarySummary.validationPass}")
        println("humanApproval=${report.boundarySummary.humanApproval}")
        println("canonApproval=${report.boundarySummary.canonApproval}")
        println("settingPackConversion=${report.boundarySummary.settingPackConversion}")
        println("runtimeSqliteData=${report.boundarySummary.runtimeSqliteData}")
        println("assetRegistryApproval=${report.boundarySummary.assetRegistryApproval}")
        println("providerApiCalls=${report.boundarySummary.providerApiCalls}")
        println("mediaGenerated=${report.boundarySummary.mediaGenerated}")
        println("finalMediaApproval=${report.boundarySummary.finalMediaApproval}")
        println("productionLock=${report.boundarySummary.productionLock}")
        println("demandDrivenQueryImplementation=${report.boundarySummary.demandDrivenQueryImplementation}")
        println("allRendererInvocationNone=${report.boundarySummary.allRendererInvocationNone}")
        println("allFfmpegInvocationNone=${report.boundarySummary.allFfmpegInvocationNone}")
        println("allTtsInvocationNone=${report.boundarySummary.allTtsInvocationNone}")
        println("downstreamBoundary=${report.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("progressCaveat=${report.progressCaveat}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun draftLocalAssetPromptCards(options: Map<String, String>): Int {
        val storyInputPath = Path.of(options.requiredCliOption("story-input"))
        val storyValidationPath = Path.of(options.requiredCliOption("story-validation"))
        val draft = LocalAssetPromptCardDraftService().create(
            storyInputPack = readCliJson<LocalStoryInputPack>(storyInputPath),
            storyValidationReport = readCliJson<LocalStoryInputPackValidationReport>(storyValidationPath),
            storyInputPackFile = storyInputPath.toString(),
            storyValidationReportFile = storyValidationPath.toString(),
            draftId = options["draft"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), draft) }
        println("status=${draft.status}")
        println("draftId=${draft.draftId}")
        println("sourceStoryInputPackId=${draft.sourceStoryInputPackId}")
        println("sourceStoryValidationId=${draft.sourceStoryValidationId}")
        println("characters=${draft.characterCards.size}")
        println("scenes=${draft.sceneCards.size}")
        println("beats=${draft.beatCards.size}")
        println("visualPromptCards=${draft.visualPromptCards.size}")
        println("audioCueCards=${draft.audioCueCards.size}")
        println("targetDurationMillis=${draft.renderIntent.targetDurationMillis}")
        println("localOnly=${draft.localOnlyConstraints.localOnly}")
        println("noExternalPaidProviderTools=${draft.localOnlyConstraints.noExternalPaidProviderTools}")
        println("fileProtocolJsonOnly=${draft.localOnlyConstraints.fileProtocolJsonOnly}")
        println("downstreamBoundary=${draft.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${draft.nextAllowedDownstreamStep}")
        println("blockedReason=${draft.blockedReason}")
        println("findings=${draft.findingCount}")
        draft.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (draft.status == "FAIL") 1 else 0
    }

    fun draftLocalRenderPackage(options: Map<String, String>): Int {
        val storyInputPath = Path.of(options.requiredCliOption("story-input"))
        val assetPromptCardsPath = Path.of(options.requiredCliOption("asset-prompt-cards"))
        val draft = LocalRenderPackageDraftService().create(
            storyInputPack = readCliJson<LocalStoryInputPack>(storyInputPath),
            assetPromptCards = readCliJson<LocalAssetPromptCardsDraft>(assetPromptCardsPath),
            storyInputPackFile = storyInputPath.toString(),
            assetPromptCardsFile = assetPromptCardsPath.toString(),
            packageId = options["package"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), draft) }
        println("status=${draft.status}")
        println("packageId=${draft.packageId}")
        println("sourceStoryInputPackId=${draft.sourceStoryInputPackId}")
        println("sourceAssetPromptCardsStatus=${draft.sourceAssetPromptCardsStatus}")
        println("timelineItems=${draft.timelinePlan.size}")
        println("scenePlans=${draft.scenePlan.size}")
        println("panelPlans=${draft.panelPlan.size}")
        println("assetPromptReferences=${draft.assetPromptReferences.size}")
        println("qaChecks=${draft.qaChecklist.size}")
        println("qaPass=${draft.evidence.qaPassCount}")
        println("qaFail=${draft.evidence.qaFailCount}")
        println("targetDurationMillis=${draft.evidence.targetDurationMillis}")
        println("timelineTotalDurationMillis=${draft.evidence.timelineTotalDurationMillis}")
        println("localOnly=${draft.localOnlyConstraints.localOnly}")
        println("noExternalPaidProviderTools=${draft.localOnlyConstraints.noExternalPaidProviderTools}")
        println("fileProtocolJsonOnly=${draft.localOnlyConstraints.fileProtocolJsonOnly}")
        println("renderBoundary=${draft.renderBoundary}")
        println("downstreamBoundary=${draft.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${draft.nextAllowedDownstreamStep}")
        println("blockedReason=${draft.blockedReason}")
        println("findings=${draft.findingCount}")
        draft.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (draft.status == "FAIL") 1 else 0
    }

    fun verifyLocalStoryVideoChain(options: Map<String, String>): Int {
        val storyInputPath = Path.of(options.requiredCliOption("story-input"))
        val outputDir = Path.of(options.requiredCliOption("output-dir"))
        val reportPath = Path.of(options.requiredCliOption("out"))
        val result = LocalStoryVideoChainVerificationService().verify(
            storyInputPack = readCliJson<LocalStoryInputPack>(storyInputPath),
            storyInputPackFile = storyInputPath.toString(),
            outputDir = outputDir.toString(),
            chainReportFile = reportPath.toString(),
            chainId = options["chain"] ?: "",
        )
        writeCliJson(Path.of(result.report.generatedFiles.storyValidationReportFile), result.storyValidationReport)
        result.assetPromptCardsDraft?.let {
            writeCliJson(Path.of(result.report.generatedFiles.assetPromptCardsFile), it)
        }
        result.localRenderPackageDraft?.let {
            writeCliJson(Path.of(result.report.generatedFiles.localRenderPackageFile), it)
        }
        writeCliJson(reportPath, result.report)
        println("status=${result.report.status}")
        println("chainId=${result.report.chainId}")
        println("sourceStoryInputPackId=${result.report.sourceStoryInputPackId}")
        println("storyValidation=${result.report.stageResults.storyValidation.status}")
        println("assetPromptCards=${result.report.stageResults.assetPromptCards.status}")
        println("localRenderPackage=${result.report.stageResults.localRenderPackage.status}")
        println("validationReportFile=${result.report.generatedFiles.storyValidationReportFile}")
        println("assetPromptCardsFile=${result.report.generatedFiles.assetPromptCardsFile}")
        println("localRenderPackageFile=${result.report.generatedFiles.localRenderPackageFile}")
        println("chainReportFile=${result.report.generatedFiles.chainReportFile}")
        println("characters=${result.report.counts.characters}")
        println("scenes=${result.report.counts.scenes}")
        println("beats=${result.report.counts.beats}")
        println("visualPromptCards=${result.report.counts.visualPromptCards}")
        println("audioCueCards=${result.report.counts.audioCueCards}")
        println("panelPlans=${result.report.counts.panelPlans}")
        println("qaChecks=${result.report.counts.qaChecks}")
        println("qaPass=${result.report.counts.qaPass}")
        println("qaFail=${result.report.counts.qaFail}")
        println("targetDurationMillis=${result.report.counts.targetDurationMillis}")
        println("mediaGenerated=${result.report.boundaries.mediaGenerated}")
        println("rendererInvocation=${result.report.boundaries.rendererInvocation}")
        println("providerInvocation=${result.report.boundaries.providerInvocation}")
        println("ttsInvocation=${result.report.boundaries.ttsInvocation}")
        println("noExternalPaidProviderTools=${result.report.boundaries.noExternalPaidProviderTools}")
        println("downstreamBoundary=${result.report.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${result.report.nextAllowedDownstreamStep}")
        println("blockedReason=${result.report.blockedReason}")
        println("findings=${result.report.findingCount}")
        result.report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (result.report.status == "FAIL") 1 else 0
    }

    fun draftLocalStoryVideoPreviewHandoff(options: Map<String, String>): Int {
        val chainReportPath = Path.of(options.requiredCliOption("chain-report"))
        val renderPackagePath = Path.of(options.requiredCliOption("render-package"))
        val assetPromptCardsPath = Path.of(options.requiredCliOption("asset-prompt-cards"))
        val storyInputPath = Path.of(options.requiredCliOption("story-input"))
        val outPath = Path.of(options.requiredCliOption("out"))
        val draft = LocalStoryVideoPreviewHandoffDraftService().create(
            chainReport = readCliJson<LocalStoryVideoChainReport>(chainReportPath),
            renderPackage = readCliJson<LocalRenderPackageDraft>(renderPackagePath),
            assetPromptCards = readCliJson<LocalAssetPromptCardsDraft>(assetPromptCardsPath),
            storyInputPack = readCliJson<LocalStoryInputPack>(storyInputPath),
            chainReportFile = chainReportPath.toString(),
            renderPackageFile = renderPackagePath.toString(),
            assetPromptCardsFile = assetPromptCardsPath.toString(),
            storyInputFile = storyInputPath.toString(),
            handoffId = options["handoff"] ?: "",
        )
        writeCliJson(outPath, draft)
        println("status=${draft.status}")
        println("handoffId=${draft.handoffId}")
        println("sourceStoryInputPackId=${draft.sourceStoryInputPackId}")
        println("sourceChainStatus=${draft.sourceChainStatus}")
        println("sourceRenderPackageStatus=${draft.sourceRenderPackageStatus}")
        println("sourceAssetPromptCardsStatus=${draft.sourceAssetPromptCardsStatus}")
        println("readinessChecks=${draft.evidence.readinessCheckCount}")
        println("readinessPass=${draft.evidence.readinessPassCount}")
        println("readinessFail=${draft.evidence.readinessFailCount}")
        println("scenes=${draft.evidence.sceneCount}")
        println("panels=${draft.evidence.panelCount}")
        println("visualPromptReferences=${draft.evidence.visualPromptReferenceCount}")
        println("audioCueReferences=${draft.evidence.audioCueReferenceCount}")
        println("targetDurationMillis=${draft.evidence.targetDurationMillis}")
        println("localRendererReady=${draft.localRendererCompatibility.ready}")
        println("mediaGenerated=${draft.boundaries.mediaGenerated}")
        println("rendererInvocation=${draft.boundaries.rendererInvocation}")
        println("providerInvocation=${draft.boundaries.providerInvocation}")
        println("ttsInvocation=${draft.boundaries.ttsInvocation}")
        println("noExternalPaidProviderTools=${draft.boundaries.noExternalPaidProviderTools}")
        println("downstreamBoundary=${draft.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${draft.nextAllowedDownstreamStep}")
        println("blockedReason=${draft.blockedReason}")
        println("findings=${draft.findingCount}")
        draft.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (draft.status == "FAIL") 1 else 0
    }

    fun reviewLocalStoryVideoPreviewHandoff(options: Map<String, String>): Int {
        val handoffPath = Path.of(options.requiredCliOption("handoff"))
        val outPath = Path.of(options.requiredCliOption("out"))
        val report = LocalStoryVideoPreviewHandoffReviewService().review(
            handoff = readCliJson<LocalStoryVideoPreviewHandoffDraft>(handoffPath),
            handoffFile = handoffPath.toString(),
            reviewId = options["review"] ?: "",
        )
        writeCliJson(outPath, report)
        val checksPass = report.checks.count { it.status == "PASS" }
        val checksFail = report.checks.count { it.status != "PASS" }
        println("verdict=${report.verdict}")
        println("reviewId=${report.reviewId}")
        println("sourceHandoffId=${report.sourceHandoffId}")
        println("sourceStoryInputPackId=${report.sourceStoryInputPackId}")
        println("handoffStatus=${report.handoffStatus}")
        println("checks=${report.checks.size}")
        println("checksPass=$checksPass")
        println("checksFail=$checksFail")
        println("readinessChecks=${report.readinessSummary.readinessChecks}")
        println("requiredReadinessPass=${report.readinessSummary.requiredReadinessPass}")
        println("requiredReadinessFail=${report.readinessSummary.requiredReadinessFail}")
        println("previewPanels=${report.readinessSummary.previewPanels}")
        println("targetDurationMillis=${report.readinessSummary.previewTargetDurationMillis}")
        println("qaFail=${report.readinessSummary.qaFail}")
        println("localRendererReady=${report.rendererCompatibilitySummary.ready}")
        println("mediaGenerated=${report.boundaries.mediaGenerated}")
        println("rendererInvocation=${report.boundaries.rendererInvocation}")
        println("providerInvocation=${report.boundaries.providerInvocation}")
        println("ttsInvocation=${report.boundaries.ttsInvocation}")
        println("noExternalPaidProviderTools=${report.boundaries.noExternalPaidProviderTools}")
        println("downstreamBoundary=${report.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun dryRunLocalRendererAdapter(options: Map<String, String>): Int {
        val handoffPath = Path.of(options.requiredCliOption("handoff"))
        val reviewPath = Path.of(options.requiredCliOption("review"))
        val outPath = Path.of(options.requiredCliOption("out"))
        val report = LocalRendererAdapterDryRunService().dryRun(
            handoff = readCliJson<LocalStoryVideoPreviewHandoffDraft>(handoffPath),
            review = readCliJson<LocalStoryVideoPreviewHandoffReviewReport>(reviewPath),
            handoffFile = handoffPath.toString(),
            reviewFile = reviewPath.toString(),
            dryRunId = options["dry-run-id"] ?: "",
        )
        writeCliJson(outPath, report)
        val checksPass = report.checks.count { it.status == "PASS" }
        val checksFail = report.checks.count { it.status != "PASS" }
        println("verdict=${report.verdict}")
        println("status=${report.status}")
        println("dryRunId=${report.dryRunId}")
        println("sourceHandoffId=${report.sourceHandoffId}")
        println("sourceReviewId=${report.sourceReviewId}")
        println("sourceStoryInputPackId=${report.sourceStoryInputPackId}")
        println("checks=${report.checks.size}")
        println("checksPass=$checksPass")
        println("checksFail=$checksFail")
        println("sceneCount=${report.adapterMappingSummary.sceneCount}")
        println("panelCount=${report.adapterMappingSummary.panelCount}")
        println("mappedPanelCount=${report.adapterMappingSummary.mappedPanelCount}")
        println("unmappedPanelCount=${report.adapterMappingSummary.unmappedPanelCount}")
        println("missingRequiredFieldCount=${report.adapterMappingSummary.missingRequiredFieldCount}")
        println("targetDurationMillis=${report.adapterMappingSummary.targetDurationMillis}")
        println("qaFail=${report.adapterMappingSummary.qaFail}")
        println("localRendererReady=${report.rendererCompatibilitySummary.ready}")
        println("manifestGenerated=${report.rendererCompatibilitySummary.manifestGenerated}")
        println("mediaGenerated=${report.boundaries.mediaGenerated}")
        println("rendererInvocation=${report.boundaries.rendererInvocation}")
        println("providerInvocation=${report.boundaries.providerInvocation}")
        println("ttsInvocation=${report.boundaries.ttsInvocation}")
        println("noExternalPaidProviderTools=${report.boundaries.noExternalPaidProviderTools}")
        println("downstreamBoundary=${report.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun planLocalRendererAdapterImplementation(options: Map<String, String>): Int {
        val dryRunReportPath = Path.of(options.requiredCliOption("dry-run-report"))
        val outPath = Path.of(options.requiredCliOption("out"))
        val plan = LocalRendererAdapterImplementationPlanService().plan(
            dryRunReport = readCliJson<LocalRendererAdapterDryRunReport>(dryRunReportPath),
            dryRunReportFile = dryRunReportPath.toString(),
            planId = options["plan"] ?: "",
        )
        writeCliJson(outPath, plan)
        val checksPass = plan.sourceValidationChecks.count { it.status == "PASS" }
        val checksFail = plan.sourceValidationChecks.count { it.status != "PASS" }
        println("verdict=${plan.verdict}")
        println("status=${plan.status}")
        println("planId=${plan.planId}")
        println("sourceDryRunId=${plan.sourceDryRunId}")
        println("sourceHandoffId=${plan.sourceHandoffId}")
        println("sourceReviewId=${plan.sourceReviewId}")
        println("sourceStoryInputPackId=${plan.sourceStoryInputPackId}")
        println("checks=${plan.sourceValidationChecks.size}")
        println("checksPass=$checksPass")
        println("checksFail=$checksFail")
        println("requiredAdapterWorkItems=${plan.requiredAdapterWorkItems.size}")
        println("implementationSteps=${plan.implementationSteps.size}")
        println("riskChecks=${plan.riskChecklist.size}")
        println("verificationSteps=${plan.verificationPlan.size}")
        println("planningOnly=${plan.targetManifestFieldMappingPlan.planningOnly}")
        println("manifestGenerated=${plan.boundaries.manifestGenerated}")
        println("mediaGenerated=${plan.boundaries.mediaGenerated}")
        println("rendererInvocation=${plan.boundaries.rendererInvocation}")
        println("providerInvocation=${plan.boundaries.providerInvocation}")
        println("ttsInvocation=${plan.boundaries.ttsInvocation}")
        println("noExternalPaidProviderTools=${plan.boundaries.noExternalPaidProviderTools}")
        println("adapterImplementationExecuted=${plan.boundaries.adapterImplementationExecuted}")
        println("downstreamBoundary=${plan.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${plan.nextAllowedDownstreamStep}")
        println("blockedReason=${plan.blockedReason}")
        println("findings=${plan.findingCount}")
        plan.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (plan.verdict == "FAIL") 1 else 0
    }

    fun reviewLocalRendererAdapterImplementationPlan(options: Map<String, String>): Int {
        val planPath = Path.of(options.requiredCliOption("plan"))
        val outPath = Path.of(options.requiredCliOption("out"))
        val report = LocalRendererAdapterImplementationPlanReviewService().review(
            plan = readCliJson<LocalRendererAdapterImplementationPlanDraft>(planPath),
            planFile = planPath.toString(),
            reviewId = options["review"] ?: "",
        )
        writeCliJson(outPath, report)
        val checksPass = report.checks.count { it.status == "PASS" }
        val checksFail = report.checks.count { it.status != "PASS" }
        println("verdict=${report.verdict}")
        println("status=${report.status}")
        println("reviewId=${report.reviewId}")
        println("sourcePlanId=${report.sourcePlanId}")
        println("sourceDryRunId=${report.sourceDryRunId}")
        println("sourceHandoffId=${report.sourceHandoffId}")
        println("sourceReviewId=${report.sourceReviewId}")
        println("sourceStoryInputPackId=${report.sourceStoryInputPackId}")
        println("checks=${report.checks.size}")
        println("checksPass=$checksPass")
        println("checksFail=$checksFail")
        println("requiredAdapterWorkItems=${report.planSummary.requiredAdapterWorkItems}")
        println("targetMappingNoteCount=${report.planSummary.targetMappingNoteCount}")
        println("implementationSteps=${report.planSummary.implementationSteps}")
        println("riskChecks=${report.planSummary.riskChecks}")
        println("verificationSteps=${report.planSummary.verificationSteps}")
        println("planningOnly=${report.planSummary.planningOnly}")
        println("manifestGenerated=${report.boundaries.manifestGenerated}")
        println("adapterImplementationExecuted=${report.boundaries.adapterImplementationExecuted}")
        println("mediaGenerated=${report.boundaries.mediaGenerated}")
        println("rendererInvocation=${report.boundaries.rendererInvocation}")
        println("providerInvocation=${report.boundaries.providerInvocation}")
        println("ttsInvocation=${report.boundaries.ttsInvocation}")
        println("noExternalPaidProviderTools=${report.boundaries.noExternalPaidProviderTools}")
        println("rendererReady=${report.rendererCompatibilitySummary.ready}")
        println("mp4ReadinessClaimed=${report.rendererCompatibilitySummary.mp4ReadinessClaimed}")
        println("downstreamBoundary=${report.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun draftLocalRendererAdapterImplementation(options: Map<String, String>): Int {
        val handoffPath = Path.of(options.requiredCliOption("handoff"))
        val planPath = Path.of(options.requiredCliOption("plan"))
        val planReviewPath = Path.of(options.requiredCliOption("plan-review"))
        val outPath = Path.of(options.requiredCliOption("out"))
        val draft = LocalRendererAdapterImplementationDraftService().draft(
            handoff = readCliJson<LocalStoryVideoPreviewHandoffDraft>(handoffPath),
            plan = readCliJson<LocalRendererAdapterImplementationPlanDraft>(planPath),
            planReview = readCliJson<LocalRendererAdapterImplementationPlanReviewReport>(planReviewPath),
            handoffFile = handoffPath.toString(),
            planFile = planPath.toString(),
            planReviewFile = planReviewPath.toString(),
            draftId = options["draft"] ?: "",
        )
        writeCliJson(outPath, draft)
        val checksPass = draft.checks.count { it.status == "PASS" }
        val checksFail = draft.checks.count { it.status != "PASS" }
        println("verdict=${draft.verdict}")
        println("status=${draft.status}")
        println("draftId=${draft.draftId}")
        println("sourcePlanId=${draft.sourcePlanId}")
        println("sourcePlanReviewId=${draft.sourcePlanReviewId}")
        println("sourceDryRunId=${draft.sourceDryRunId}")
        println("sourceHandoffId=${draft.sourceHandoffId}")
        println("sourceHandoffReviewId=${draft.sourceHandoffReviewId}")
        println("sourceStoryInputPackId=${draft.sourceStoryInputPackId}")
        println("checks=${draft.checks.size}")
        println("checksPass=$checksPass")
        println("checksFail=$checksFail")
        println("previewPanelCount=${draft.implementationDraftSummary.previewPanelCount}")
        println("mappingDraftPanelCount=${draft.implementationDraftSummary.mappingDraftPanelCount}")
        println("requiredAdapterWorkItems=${draft.implementationDraftSummary.requiredAdapterWorkItems}")
        println("implementationTasks=${draft.implementationTasks.size}")
        println("verificationSteps=${draft.verificationPlan.size}")
        println("targetMappingNoteCount=${draft.implementationDraftSummary.targetMappingNoteCount}")
        println("missingMappingNoteCount=${draft.implementationDraftSummary.missingMappingNoteCount}")
        println("mediaGenerated=${draft.boundaries.mediaGenerated}")
        println("rendererInvocation=${draft.boundaries.rendererInvocation}")
        println("providerInvocation=${draft.boundaries.providerInvocation}")
        println("ttsInvocation=${draft.boundaries.ttsInvocation}")
        println("manifestGenerated=${draft.boundaries.manifestGenerated}")
        println("adapterImplementationExecuted=${draft.boundaries.adapterImplementationExecuted}")
        println("noExternalPaidProviderTools=${draft.boundaries.noExternalPaidProviderTools}")
        println("rendererReady=${draft.boundaries.rendererReady}")
        println("mp4ReadinessClaimed=${draft.boundaries.mp4ReadinessClaimed}")
        println("downstreamBoundary=${draft.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${draft.nextAllowedDownstreamStep}")
        println("blockedReason=${draft.blockedReason}")
        println("findings=${draft.findingCount}")
        draft.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (draft.verdict == "FAIL") 1 else 0
    }

    fun reviewLocalRendererAdapterImplementationDraft(options: Map<String, String>): Int {
        val draftPath = Path.of(options.requiredCliOption("draft"))
        val outPath = Path.of(options.requiredCliOption("out"))
        val report = LocalRendererAdapterImplementationDraftReviewService().reviewFile(
            draftPath = draftPath,
            outPath = outPath,
            reviewId = options["review"] ?: "",
        )
        val checksPass = report.checks.count { it.status == "PASS" }
        val checksFail = report.checks.count { it.status != "PASS" }
        println("verdict=${report.verdict}")
        println("status=${report.status}")
        println("reviewId=${report.reviewId}")
        println("sourceDraftId=${report.sourceDraftId}")
        println("sourceStoryInputPackId=${report.sourceStoryInputPackId}")
        println("checks=${report.checks.size}")
        println("checksPass=$checksPass")
        println("checksFail=$checksFail")
        println("statusMatchesExpected=${report.status == "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_PASS"}")
        println("downstreamBoundary=${report.downstreamBoundary}")
        println("downstreamBoundaryMatchesExpected=${report.downstreamBoundary == "local_renderer_adapter_implementation_draft_review_only_not_manifest_generation"}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("nextStepMatchesExpected=${report.nextAllowedDownstreamStep == "local_renderer_manifest_draft_plan"}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun planLocalRendererManifestDraft(options: Map<String, String>): Int {
        val implementationDraftPath = Path.of(options.requiredCliOption("implementation-draft"))
        val draftReviewPath = Path.of(options.requiredCliOption("draft-review"))
        val outPath = Path.of(options.requiredCliOption("out"))
        val plan = LocalRendererManifestDraftPlanService().planFile(
            implementationDraftPath = implementationDraftPath,
            draftReviewPath = draftReviewPath,
            outPath = outPath,
            planId = options["plan"] ?: "",
        )
        val checksPass = plan.checks.count { it.status == "PASS" }
        val checksFail = plan.checks.count { it.status != "PASS" }
        println("verdict=${plan.verdict}")
        println("status=${plan.status}")
        println("planId=${plan.planId}")
        println("sourceDraftId=${plan.sourceDraftId}")
        println("sourceDraftReviewId=${plan.sourceDraftReviewId}")
        println("sourcePlanId=${plan.sourcePlanId}")
        println("sourcePlanReviewId=${plan.sourcePlanReviewId}")
        println("sourceDryRunId=${plan.sourceDryRunId}")
        println("sourceHandoffId=${plan.sourceHandoffId}")
        println("sourceHandoffReviewId=${plan.sourceHandoffReviewId}")
        println("sourceStoryInputPackId=${plan.sourceStoryInputPackId}")
        println("checks=${plan.checks.size}")
        println("checksPass=$checksPass")
        println("checksFail=$checksFail")
        println("manifestPanels=${plan.manifestFieldPlan.panelManifestPlans.size}")
        println("manifestFields=${plan.manifestDraftPlanSummary.mappingDraftFields}")
        println("requiredManifestDraftWorkItems=${plan.requiredManifestDraftWorkItems.size}")
        println("verificationSteps=${plan.verificationPlan.size}")
        println("planningOnly=${plan.manifestFieldPlan.planningOnly}")
        println("manifestGenerated=${plan.boundaries.manifestGenerated}")
        println("adapterImplementationExecuted=${plan.boundaries.adapterImplementationExecuted}")
        println("mediaGenerated=${plan.boundaries.mediaGenerated}")
        println("rendererInvocation=${plan.boundaries.rendererInvocation}")
        println("providerInvocation=${plan.boundaries.providerInvocation}")
        println("ttsInvocation=${plan.boundaries.ttsInvocation}")
        println("noExternalPaidProviderTools=${plan.boundaries.noExternalPaidProviderTools}")
        println("rendererReady=${plan.boundaries.rendererReady}")
        println("mp4ReadinessClaimed=${plan.boundaries.mp4ReadinessClaimed}")
        println("downstreamBoundary=${plan.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${plan.nextAllowedDownstreamStep}")
        println("blockedReason=${plan.blockedReason}")
        println("findings=${plan.findingCount}")
        plan.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (plan.verdict == "FAIL") 1 else 0
    }

    fun reviewLocalRendererManifestDraftPlan(options: Map<String, String>): Int {
        val planPath = Path.of(options.requiredCliOption("plan"))
        val outPath = Path.of(options.requiredCliOption("out"))
        val report = LocalRendererManifestDraftPlanReviewService().reviewFile(
            planPath = planPath,
            outPath = outPath,
            reviewId = options["review"] ?: "",
        )
        val checksPass = report.checks.count { it.status == "PASS" }
        val checksFail = report.checks.count { it.status != "PASS" }
        println("verdict=${report.verdict}")
        println("status=${report.status}")
        println("reviewId=${report.reviewId}")
        println("sourceManifestDraftPlanId=${report.sourceManifestDraftPlanId}")
        println("sourceImplementationDraftId=${report.sourceImplementationDraftId}")
        println("sourceImplementationDraftReviewId=${report.sourceImplementationDraftReviewId}")
        println("sourceStoryInputPackId=${report.sourceStoryInputPackId}")
        println("checks=${report.checks.size}")
        println("checksPass=$checksPass")
        println("checksFail=$checksFail")
        println("manifestPanels=${report.planReviewSummary.manifestPanels}")
        println("manifestFields=${report.planReviewSummary.manifestFields}")
        println("requiredManifestDraftWorkItems=${report.planReviewSummary.requiredManifestDraftWorkItems}")
        println("verificationSteps=${report.planReviewSummary.verificationSteps}")
        println("readyForFutureManifestJsonDraft=${report.manifestDraftPlanReadinessSummary.readyForFutureManifestJsonDraft}")
        println("manifestGenerated=${report.boundaries.manifestGenerated}")
        println("adapterImplementationExecuted=${report.boundaries.adapterImplementationExecuted}")
        println("mediaGenerated=${report.boundaries.mediaGenerated}")
        println("rendererInvocation=${report.boundaries.rendererInvocation}")
        println("providerInvocation=${report.boundaries.providerInvocation}")
        println("ttsInvocation=${report.boundaries.ttsInvocation}")
        println("noExternalPaidProviderTools=${report.boundaries.noExternalPaidProviderTools}")
        println("rendererReady=${report.boundaries.rendererReady}")
        println("mp4ReadinessClaimed=${report.boundaries.mp4ReadinessClaimed}")
        println("downstreamBoundary=${report.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun draftLocalRendererManifestJson(options: Map<String, String>): Int {
        val planPath = Path.of(options.requiredCliOption("plan"))
        val planReviewPath = Path.of(options.requiredCliOption("plan-review"))
        val outPath = Path.of(options.requiredCliOption("out"))
        val draft = LocalRendererManifestDraftJsonDraftService().draftFile(
            planPath = planPath,
            planReviewPath = planReviewPath,
            outPath = outPath,
            draftId = options["draft"] ?: "",
        )
        val checksPass = draft.checks.count { it.status == "PASS" }
        val checksFail = draft.checks.count { it.status != "PASS" }
        println("verdict=${draft.verdict}")
        println("status=${draft.status}")
        println("draftId=${draft.draftId}")
        println("sourceManifestDraftPlanId=${draft.sourceManifestDraftPlanId}")
        println("sourceManifestDraftPlanReviewId=${draft.sourceManifestDraftPlanReviewId}")
        println("sourceImplementationDraftId=${draft.sourceImplementationDraftId}")
        println("sourceImplementationDraftReviewId=${draft.sourceImplementationDraftReviewId}")
        println("sourceStoryInputPackId=${draft.sourceStoryInputPackId}")
        println("checks=${draft.checks.size}")
        println("checksPass=$checksPass")
        println("checksFail=$checksFail")
        println("draftPanelEntries=${draft.draftPanelManifestEntries.size}")
        println("draftManifestFields=${draft.draftFieldCoverageSummary.totalDraftFields}")
        println("missingRequiredDraftFields=${draft.draftFieldCoverageSummary.missingRequiredDraftFields}")
        println("draftOnly=${draft.draftManifestSummary.draftOnly}")
        println("draftManifestGenerated=${draft.boundaries.draftManifestGenerated}")
        println("productionManifestGenerated=${draft.boundaries.productionManifestGenerated}")
        println("productionReady=${draft.boundaries.productionReady}")
        println("adapterImplementationExecuted=${draft.boundaries.adapterImplementationExecuted}")
        println("mediaGenerated=${draft.boundaries.mediaGenerated}")
        println("rendererInvocation=${draft.boundaries.rendererInvocation}")
        println("providerInvocation=${draft.boundaries.providerInvocation}")
        println("ttsInvocation=${draft.boundaries.ttsInvocation}")
        println("noExternalPaidProviderTools=${draft.boundaries.noExternalPaidProviderTools}")
        println("rendererReady=${draft.boundaries.rendererReady}")
        println("mp4ReadinessClaimed=${draft.boundaries.mp4ReadinessClaimed}")
        println("downstreamBoundary=${draft.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${draft.nextAllowedDownstreamStep}")
        println("blockedReason=${draft.blockedReason}")
        println("findings=${draft.findingCount}")
        draft.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (draft.verdict == "FAIL") 1 else 0
    }

    fun reviewLocalRendererManifestDraftJson(options: Map<String, String>): Int {
        val draftPath = Path.of(options.requiredCliOption("draft"))
        val outPath = Path.of(options.requiredCliOption("out"))
        val report = LocalRendererManifestDraftJsonReviewService().reviewFile(
            draftPath = draftPath,
            outPath = outPath,
            reviewId = options["review"] ?: "",
        )
        val checksPass = report.checks.count { it.status == "PASS" }
        val checksFail = report.checks.count { it.status != "PASS" }
        println("verdict=${report.verdict}")
        println("status=${report.status}")
        println("reviewId=${report.reviewId}")
        println("sourceDraftId=${report.sourceDraftId}")
        println("sourceManifestDraftPlanId=${report.sourceManifestDraftPlanId}")
        println("sourceManifestDraftPlanReviewId=${report.sourceManifestDraftPlanReviewId}")
        println("sourceStoryInputPackId=${report.sourceStoryInputPackId}")
        println("checks=${report.checks.size}")
        println("checksPass=$checksPass")
        println("checksFail=$checksFail")
        println("draftPanelEntries=${report.draftManifestReviewSummary.draftPanelEntries}")
        println("draftManifestFields=${report.draftManifestReviewSummary.draftManifestFields}")
        println("missingRequiredDraftFields=${report.draftFieldCoverageReviewSummary.missingRequiredDraftFields}")
        println("draftManifestGenerated=${report.boundaries.draftManifestGenerated}")
        println("productionManifestGenerated=${report.boundaries.productionManifestGenerated}")
        println("productionReady=${report.boundaries.productionReady}")
        println("productionApproved=${report.boundaries.productionApproved}")
        println("adapterImplementationExecuted=${report.boundaries.adapterImplementationExecuted}")
        println("mediaGenerated=${report.boundaries.mediaGenerated}")
        println("rendererInvocation=${report.boundaries.rendererInvocation}")
        println("providerInvocation=${report.boundaries.providerInvocation}")
        println("ttsInvocation=${report.boundaries.ttsInvocation}")
        println("noExternalPaidProviderTools=${report.boundaries.noExternalPaidProviderTools}")
        println("rendererReady=${report.boundaries.rendererReady}")
        println("mp4ReadinessClaimed=${report.boundaries.mp4ReadinessClaimed}")
        println("downstreamBoundary=${report.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun dryRunLocalRendererManifestDraftJsonAdapter(options: Map<String, String>): Int {
        val draftPath = Path.of(options.requiredCliOption("draft"))
        val reviewPath = Path.of(options.requiredCliOption("review"))
        val outPath = Path.of(options.requiredCliOption("out"))
        val report = LocalRendererManifestDraftJsonAdapterDryRunService().dryRunFile(
            draftPath = draftPath,
            reviewPath = reviewPath,
            outPath = outPath,
            dryRunId = options["dry-run"] ?: options["dry-run-id"] ?: "",
        )
        val checksPass = report.checks.count { it.status == "PASS" }
        val checksFail = report.checks.count { it.status != "PASS" }
        println("verdict=${report.verdict}")
        println("status=${report.status}")
        println("dryRunId=${report.dryRunId}")
        println("sourceDraftId=${report.sourceDraftId}")
        println("sourceReviewId=${report.sourceReviewId}")
        println("sourceManifestDraftPlanId=${report.sourceManifestDraftPlanId}")
        println("sourceManifestDraftPlanReviewId=${report.sourceManifestDraftPlanReviewId}")
        println("sourceStoryInputPackId=${report.sourceStoryInputPackId}")
        println("checks=${report.checks.size}")
        println("checksPass=$checksPass")
        println("checksFail=$checksFail")
        println("draftPanelCount=${report.panelMappingSummary.draftPanelCount}")
        println("adapterDryRunMappingEntries=${report.panelMappingSummary.adapterDryRunMappingEntries}")
        println("mappedPanelCount=${report.panelMappingSummary.mappedPanelCount}")
        println("unmappedPanelCount=${report.panelMappingSummary.unmappedPanelCount}")
        println("totalDraftFields=${report.draftFieldCoverageSummary.totalDraftFields}")
        println("mappedDraftFields=${report.draftFieldCoverageSummary.mappedDraftFields}")
        println("missingRequiredFieldCount=${report.panelMappingSummary.missingRequiredFieldCount}")
        println("draftManifestGenerated=${report.boundaries.draftManifestGenerated}")
        println("productionManifestGenerated=${report.boundaries.productionManifestGenerated}")
        println("productionReady=${report.boundaries.productionReady}")
        println("productionApproved=${report.boundaries.productionApproved}")
        println("adapterImplementationExecuted=${report.boundaries.adapterImplementationExecuted}")
        println("adapterExecuted=${report.boundaries.adapterExecuted}")
        println("mediaGenerated=${report.boundaries.mediaGenerated}")
        println("rendererInvocation=${report.boundaries.rendererInvocation}")
        println("providerInvocation=${report.boundaries.providerInvocation}")
        println("ttsInvocation=${report.boundaries.ttsInvocation}")
        println("noExternalPaidProviderTools=${report.boundaries.noExternalPaidProviderTools}")
        println("rendererReady=${report.boundaries.rendererReady}")
        println("mp4ReadinessClaimed=${report.boundaries.mp4ReadinessClaimed}")
        println("downstreamBoundary=${report.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun planLocalRendererManifestAdapterContract(options: Map<String, String>): Int {
        val dryRunReportPath = Path.of(options.requiredCliOption("dry-run-report"))
        val outPath = Path.of(options.requiredCliOption("out"))
        val plan = LocalRendererManifestAdapterContractPlanService().planFile(
            dryRunReportPath = dryRunReportPath,
            outPath = outPath,
            planId = options["plan"] ?: "",
        )
        val checksPass = plan.checks.count { it.status == "PASS" }
        val checksFail = plan.checks.count { it.status != "PASS" }
        println("verdict=${plan.verdict}")
        println("status=${plan.status}")
        println("planId=${plan.planId}")
        println("sourcePhase16DryRunId=${plan.sourcePhase16DryRunId}")
        println("sourceDraftId=${plan.sourceDraftId}")
        println("sourceReviewId=${plan.sourceReviewId}")
        println("sourceManifestDraftPlanId=${plan.sourceManifestDraftPlanId}")
        println("sourceStoryInputPackId=${plan.sourceStoryInputPackId}")
        println("checks=${plan.checks.size}")
        println("checksPass=$checksPass")
        println("checksFail=$checksFail")
        println("adapterContractRequirements=${plan.adapterContractRequirements.size}")
        println("adapterInputMappingEntries=${plan.adapterInputMappingPlan.entries.size}")
        println("requiredAdapterContractWorkItems=${plan.requiredAdapterContractWorkItems.size}")
        println("verificationSteps=${plan.verificationPlan.size}")
        println("draftPanelCount=${plan.adapterContractPlanSummary.draftPanelCount}")
        println("mappedPanelCount=${plan.adapterContractPlanSummary.mappedPanelCount}")
        println("unmappedPanelCount=${plan.adapterContractPlanSummary.unmappedPanelCount}")
        println("missingRequiredFieldCount=${plan.adapterContractPlanSummary.missingRequiredFieldCount}")
        println("draftManifestGenerated=${plan.boundaries.draftManifestGenerated}")
        println("productionManifestGenerated=${plan.boundaries.productionManifestGenerated}")
        println("productionReady=${plan.boundaries.productionReady}")
        println("productionApproved=${plan.boundaries.productionApproved}")
        println("adapterImplementationExecuted=${plan.boundaries.adapterImplementationExecuted}")
        println("adapterExecuted=${plan.boundaries.adapterExecuted}")
        println("mediaGenerated=${plan.boundaries.mediaGenerated}")
        println("rendererInvocation=${plan.boundaries.rendererInvocation}")
        println("providerInvocation=${plan.boundaries.providerInvocation}")
        println("ttsInvocation=${plan.boundaries.ttsInvocation}")
        println("noExternalPaidProviderTools=${plan.boundaries.noExternalPaidProviderTools}")
        println("rendererReady=${plan.boundaries.rendererReady}")
        println("mp4ReadinessClaimed=${plan.boundaries.mp4ReadinessClaimed}")
        println("downstreamBoundary=${plan.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${plan.nextAllowedDownstreamStep}")
        println("blockedReason=${plan.blockedReason}")
        println("findings=${plan.findingCount}")
        plan.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (plan.verdict == "FAIL") 1 else 0
    }

    fun reviewLocalRendererManifestAdapterContractPlan(options: Map<String, String>): Int {
        val planPath = Path.of(options.requiredCliOption("plan"))
        val outPath = Path.of(options.requiredCliOption("out"))
        val report = LocalRendererManifestAdapterContractPlanReviewService().reviewFile(
            planPath = planPath,
            outPath = outPath,
            reviewId = options["review"] ?: "",
        )
        val checksPass = report.checks.count { it.status == "PASS" }
        val checksFail = report.checks.count { it.status != "PASS" }
        println("verdict=${report.verdict}")
        println("status=${report.status}")
        println("reviewId=${report.reviewId}")
        println("sourcePlanId=${report.sourcePlanId}")
        println("sourcePhase16DryRunId=${report.sourcePhase16DryRunId}")
        println("sourceDraftId=${report.sourceDraftId}")
        println("sourceReviewId=${report.sourceReviewId}")
        println("sourceStoryInputPackId=${report.sourceStoryInputPackId}")
        println("checks=${report.checks.size}")
        println("checksPass=$checksPass")
        println("checksFail=$checksFail")
        println("adapterContractRequirements=${report.reviewedContractRequirementsSummary.requirementCount}")
        println("adapterInputMappingEntries=${report.reviewedInputMappingSummary.mappingEntries}")
        println("requiredAdapterContractWorkItems=${report.reviewedWorkItemsSummary.workItemCount}")
        println("verificationSteps=${report.verificationReviewSummary.verificationStepCount}")
        println("mappedPanelCount=${report.adapterContractPlanReviewSummary.mappedPanelCount}")
        println("unmappedPanelCount=${report.adapterContractPlanReviewSummary.unmappedPanelCount}")
        println("missingRequiredFieldCount=${report.adapterContractPlanReviewSummary.missingRequiredFieldCount}")
        println("draftManifestGenerated=${report.boundaries.draftManifestGenerated}")
        println("productionManifestGenerated=${report.boundaries.productionManifestGenerated}")
        println("productionReady=${report.boundaries.productionReady}")
        println("productionApproved=${report.boundaries.productionApproved}")
        println("adapterImplementationExecuted=${report.boundaries.adapterImplementationExecuted}")
        println("adapterExecuted=${report.boundaries.adapterExecuted}")
        println("mediaGenerated=${report.boundaries.mediaGenerated}")
        println("rendererInvocation=${report.boundaries.rendererInvocation}")
        println("providerInvocation=${report.boundaries.providerInvocation}")
        println("ttsInvocation=${report.boundaries.ttsInvocation}")
        println("noExternalPaidProviderTools=${report.boundaries.noExternalPaidProviderTools}")
        println("rendererReady=${report.boundaries.rendererReady}")
        println("mp4ReadinessClaimed=${report.boundaries.mp4ReadinessClaimed}")
        println("downstreamBoundary=${report.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun draftLocalRendererManifestAdapterContract(options: Map<String, String>): Int {
        val planPath = Path.of(options.requiredCliOption("plan"))
        val planReviewPath = Path.of(options.requiredCliOption("plan-review"))
        val outPath = Path.of(options.requiredCliOption("out"))
        val draft = LocalRendererManifestAdapterContractDraftService().draftFile(
            planPath = planPath,
            planReviewPath = planReviewPath,
            outPath = outPath,
            draftId = options["draft"] ?: "",
        )
        val checksPass = draft.checks.count { it.status == "PASS" }
        val checksFail = draft.checks.count { it.status != "PASS" }
        println("verdict=${draft.verdict}")
        println("status=${draft.status}")
        println("draftId=${draft.draftId}")
        println("sourcePlanId=${draft.sourcePlanId}")
        println("sourcePlanReviewId=${draft.sourcePlanReviewId}")
        println("sourcePhase16DryRunId=${draft.sourcePhase16DryRunId}")
        println("sourceDraftId=${draft.sourceDraftId}")
        println("sourceReviewId=${draft.sourceReviewId}")
        println("sourceStoryInputPackId=${draft.sourceStoryInputPackId}")
        println("checks=${draft.checks.size}")
        println("checksPass=$checksPass")
        println("checksFail=$checksFail")
        println("requiredInputs=${draft.requiredInputs.size}")
        println("normalizedFieldMappings=${draft.normalizedFieldMapping.size}")
        println("adapterObligations=${draft.adapterObligations.size}")
        println("failStopRules=${draft.failStopRules.size}")
        println("verificationRequirements=${draft.verificationRequirements.size}")
        println("knownNonGoals=${draft.knownNonGoals.size}")
        println("mappedPanelCount=${draft.contractDraftSummary.mappedPanelCount}")
        println("unmappedPanelCount=${draft.contractDraftSummary.unmappedPanelCount}")
        println("missingRequiredFieldCount=${draft.contractDraftSummary.missingRequiredFieldCount}")
        println("contractDraftGenerated=${draft.boundaries.contractDraftGenerated}")
        println("productionManifestGenerated=${draft.boundaries.productionManifestGenerated}")
        println("productionReady=${draft.boundaries.productionReady}")
        println("productionApproved=${draft.boundaries.productionApproved}")
        println("adapterImplementationExecuted=${draft.boundaries.adapterImplementationExecuted}")
        println("adapterExecuted=${draft.boundaries.adapterExecuted}")
        println("mediaGenerated=${draft.boundaries.mediaGenerated}")
        println("rendererInvocation=${draft.boundaries.rendererInvocation}")
        println("providerInvocation=${draft.boundaries.providerInvocation}")
        println("ttsInvocation=${draft.boundaries.ttsInvocation}")
        println("noExternalPaidProviderTools=${draft.boundaries.noExternalPaidProviderTools}")
        println("rendererReady=${draft.boundaries.rendererReady}")
        println("mp4ReadinessClaimed=${draft.boundaries.mp4ReadinessClaimed}")
        println("downstreamBoundary=${draft.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${draft.nextAllowedDownstreamStep}")
        println("blockedReason=${draft.blockedReason}")
        println("findings=${draft.findingCount}")
        draft.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (draft.verdict == "FAIL") 1 else 0
    }
}
