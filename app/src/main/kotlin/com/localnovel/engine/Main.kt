package com.localnovel.engine

import com.localnovel.engine.cli.LocalStoryVideoCli
import com.localnovel.engine.cli.SettingPackCli
import com.localnovel.engine.cli.WorldSourceAndTaskQueueCli
import com.localnovel.engine.cli.cliRepository
import com.localnovel.engine.cli.cliRunner
import com.localnovel.engine.cli.parseCliOptions
import com.localnovel.engine.cli.printSettingPackValidation
import com.localnovel.engine.cli.readCliJson
import com.localnovel.engine.cli.requiredCliOption
import com.localnovel.engine.cli.writeCliJson
import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.AcceptedScriptPacketHandoffPacket
import com.localnovel.engine.model.AssetCandidateBrief
import com.localnovel.engine.model.AssetCandidateDraft
import com.localnovel.engine.model.AssetCandidateDraftValidationReport
import com.localnovel.engine.model.AssetCandidateHumanReviewDecision
import com.localnovel.engine.model.AssetCandidateHumanReviewDecisionIntakeReport
import com.localnovel.engine.model.AssetCandidatePreflightReport
import com.localnovel.engine.model.StoryboardPacketHumanReviewDecisionIntakeReport
import com.localnovel.engine.model.AssetCandidateReviewPreflightReport
import com.localnovel.engine.model.CandidateReferenceHumanAcceptanceDecision
import com.localnovel.engine.model.CandidateReferenceHumanAcceptanceDecisionIntakeReport
import com.localnovel.engine.model.CandidateReferenceAcceptancePacket
import com.localnovel.engine.model.CandidateReferenceRegistryHumanAcceptanceDecision
import com.localnovel.engine.model.CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport
import com.localnovel.engine.model.CandidateReferenceRegistryPacket
import com.localnovel.engine.model.CandidateReferenceRegistryPacketValidationReport
import com.localnovel.engine.model.CandidateReferenceRegistryPreflightReport
import com.localnovel.engine.model.ComicVideoAudioCue
import com.localnovel.engine.model.ComicVideoPacket
import com.localnovel.engine.model.ComicVideoPacketSourceVerdicts
import com.localnovel.engine.model.ComicVideoPacketValidationReport
import com.localnovel.engine.model.ComicVideoPanelTrack
import com.localnovel.engine.model.ComicVideoProductionPackageDraft
import com.localnovel.engine.model.ComicVideoLocalPreviewBuildEvidence
import com.localnovel.engine.model.ComicVideoLocalPreviewBuildFinding
import com.localnovel.engine.model.ComicVideoLocalPreviewBuildReport
import com.localnovel.engine.model.ComicVideoLocalRenderReport
import com.localnovel.engine.model.ComicVideoRenderHandoffManifest
import com.localnovel.engine.model.ComicVideoSubtitleCue
import com.localnovel.engine.model.ComicVideoTiming
import com.localnovel.engine.model.ComicVideoVoiceCast
import com.localnovel.engine.model.ComicVideoVoiceTimelineFill
import com.localnovel.engine.model.ImageCandidateBrief
import com.localnovel.engine.model.ImageCandidateExternalResult
import com.localnovel.engine.model.ImageCandidateExternalResultIntakeReport
import com.localnovel.engine.model.ImageCandidateReferencePacket
import com.localnovel.engine.model.ImageCandidateReferencePacketValidationReport
import com.localnovel.engine.model.ImageCandidateProviderHandoffDecision
import com.localnovel.engine.model.ImageCandidateProviderHandoffDecisionIntakeReport
import com.localnovel.engine.model.ImageCandidateProviderHandoffPacket
import com.localnovel.engine.model.ImageCandidateProviderHandoffPacketValidationReport
import com.localnovel.engine.model.ImageCandidateProviderHandoffPreflightReport
import com.localnovel.engine.model.ImageFacingDeliveryProof
import com.localnovel.engine.model.ImageCandidateResultReviewDecision
import com.localnovel.engine.model.ImageCandidateResultReviewDecisionIntakeReport
import com.localnovel.engine.model.ImageCandidateResultReviewPreflightReport
import com.localnovel.engine.model.ImageCandidatePromptPreparation
import com.localnovel.engine.model.ImageCandidatePromptValidationReport
import com.localnovel.engine.model.ImageCandidatePreflightReport
import com.localnovel.engine.model.CharacterNuwaCard
import com.localnovel.engine.model.CharacterNuwaGateFile
import com.localnovel.engine.model.CharacterNuwaGateValidationReport
import com.localnovel.engine.model.CharacterNuwaRequiredSpeaker
import com.localnovel.engine.model.CharacterNuwaSampleLines
import com.localnovel.engine.model.ChapterContinuityAnchorFile
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DemoProjectSeed
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
import com.localnovel.engine.model.NovelizationDraftIntakeReport
import com.localnovel.engine.model.NovelizationAcceptedDraftPacket
import com.localnovel.engine.model.NovelizationProseQualityAuditReport
import com.localnovel.engine.model.NovelizationRevisedDraftIntakeReport
import com.localnovel.engine.model.NovelizationRevisionBriefReport
import com.localnovel.engine.model.NovelizationRevisionContinuityAnchors
import com.localnovel.engine.model.NovelizationRevisionHandlingDeclaration
import com.localnovel.engine.model.NovelizationScriptPacketPreflightReport
import com.localnovel.engine.model.NovelizationWriterBriefValidationReport
import com.localnovel.engine.model.NovelizationPreflightReport
import com.localnovel.engine.model.NovelizationWriterBrief
import com.localnovel.engine.model.ScriptPacketDraft
import com.localnovel.engine.model.ScriptPacketDraftValidationReport
import com.localnovel.engine.model.ScriptPacketDraftStructureBridgeReport
import com.localnovel.engine.model.StoryboardPacketDraft
import com.localnovel.engine.model.StoryboardPacketDraftValidationReport
import com.localnovel.engine.model.StoryboardPacketHumanReviewDecision
import com.localnovel.engine.model.StoryboardPacketHumanReviewPreflightReport
import com.localnovel.engine.model.StoryboardPacketReviewPacket
import com.localnovel.engine.model.StoryboardStoryMovementAuditReport
import com.localnovel.engine.model.StoryboardPlanningPreflightReport
import com.localnovel.engine.model.StoryboardPlanningBrief
import com.localnovel.engine.model.ReviewableStoryboardPacket
import com.localnovel.engine.model.SettingPack
import com.localnovel.engine.model.SettingPackEntities
import com.localnovel.engine.model.SettingPackEntity
import com.localnovel.engine.model.SettingPackScriptPacketDraftBuildEvidence
import com.localnovel.engine.model.SettingPackScriptPacketDraftBuildFinding
import com.localnovel.engine.model.SettingPackScriptPacketDraftBuildReport
import com.localnovel.engine.model.SettingImageFacingProofPreviewBuildEvidence
import com.localnovel.engine.model.SettingImageFacingProofPreviewBuildFinding
import com.localnovel.engine.model.SettingImageFacingProofPreviewBuildReport
import com.localnovel.engine.model.SettingPackSource
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.AiTurnResultValidator
import com.localnovel.engine.service.AiTaskQueueService
import com.localnovel.engine.service.AcceptedScriptPacketHandoffPacketService
import com.localnovel.engine.service.AssetCandidateBriefService
import com.localnovel.engine.service.AssetCandidateDraftService
import com.localnovel.engine.service.AssetCandidateDraftValidationService
import com.localnovel.engine.service.AssetCandidateHumanReviewDecisionIntakeService
import com.localnovel.engine.service.AssetCandidatePreflightService
import com.localnovel.engine.service.AssetCandidateReviewPreflightService
import com.localnovel.engine.service.CandidateReferenceHumanAcceptanceDecisionIntakeService
import com.localnovel.engine.service.CandidateReferenceAcceptancePacketService
import com.localnovel.engine.service.CandidateReferenceAcceptancePacketValidationService
import com.localnovel.engine.service.CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService
import com.localnovel.engine.service.CandidateReferenceRegistryPacketService
import com.localnovel.engine.service.CandidateReferenceRegistryPacketValidationService
import com.localnovel.engine.service.CandidateReferenceRegistryPreflightService
import com.localnovel.engine.service.ImageCandidateBriefService
import com.localnovel.engine.service.ImageCandidatePreflightService
import com.localnovel.engine.service.ImageCandidatePromptPreparationService
import com.localnovel.engine.service.ImageCandidatePromptValidationService
import com.localnovel.engine.service.ImageCandidateProviderHandoffPreflightService
import com.localnovel.engine.service.ImageCandidateProviderHandoffDecisionIntakeService
import com.localnovel.engine.service.ImageCandidateProviderHandoffPacketService
import com.localnovel.engine.service.ImageCandidateProviderHandoffPacketValidationService
import com.localnovel.engine.service.ImageCandidateExternalResultIntakeService
import com.localnovel.engine.service.ImageCandidateReferencePacketService
import com.localnovel.engine.service.ImageCandidateReferencePacketValidationService
import com.localnovel.engine.service.ImageCandidateResultHumanReviewDecisionIntakeService
import com.localnovel.engine.service.ImageCandidateResultReviewPreflightService
import com.localnovel.engine.service.ImageFacingDeliveryProofService
import com.localnovel.engine.service.LocalAssetPromptCardDraftService
import com.localnovel.engine.service.LocalRendererAdapterDryRunService
import com.localnovel.engine.service.LocalRendererAdapterImplementationDraftReviewService
import com.localnovel.engine.service.LocalRendererAdapterImplementationDraftService
import com.localnovel.engine.service.LocalRendererAdapterImplementationPlanService
import com.localnovel.engine.service.LocalRendererAdapterImplementationPlanReviewService
import com.localnovel.engine.service.LocalRendererManifestDraftPlanService
import com.localnovel.engine.service.LocalRendererManifestDraftPlanReviewService
import com.localnovel.engine.service.LocalRendererManifestDraftJsonDraftService
import com.localnovel.engine.service.LocalRendererManifestDraftJsonReviewService
import com.localnovel.engine.service.LocalRendererManifestDraftJsonAdapterDryRunService
import com.localnovel.engine.service.LocalRendererManifestAdapterContractPlanService
import com.localnovel.engine.service.LocalRendererManifestAdapterContractPlanReviewService
import com.localnovel.engine.service.LocalRendererManifestAdapterContractDraftService
import com.localnovel.engine.service.LocalRenderPackageDraftService
import com.localnovel.engine.service.LocalStoryInputPackValidationService
import com.localnovel.engine.service.LocalStoryVideoChainVerificationService
import com.localnovel.engine.service.LocalStoryVideoPreviewHandoffDraftService
import com.localnovel.engine.service.LocalStoryVideoPreviewHandoffReviewService
import com.localnovel.engine.service.CanonConsistencyValidator
import com.localnovel.engine.service.CapabilityContractResolver
import com.localnovel.engine.service.CharacterNuwaGateValidationService
import com.localnovel.engine.service.ChapterContinuityAuditService
import com.localnovel.engine.service.CheckpointService
import com.localnovel.engine.service.ComicVideoPacketService
import com.localnovel.engine.service.ComicVideoPacketValidationService
import com.localnovel.engine.service.ComicVideoLocalRendererService
import com.localnovel.engine.service.ComicVideoProductionPackageDraftService
import com.localnovel.engine.service.ComicVideoProductionPackageValidationService
import com.localnovel.engine.service.ComicVideoRenderHandoffManifestService
import com.localnovel.engine.service.ComicVideoVoiceTimelineFillService
import com.localnovel.engine.service.ContextCompiler
import com.localnovel.engine.service.ContextAuditService
import com.localnovel.engine.service.ContextBudgetUnsatisfiableException
import com.localnovel.engine.service.DependencyGraphService
import com.localnovel.engine.service.FoundationCardPackReviewService
import com.localnovel.engine.service.FoundationCardPackValidationService
import com.localnovel.engine.service.ImpactAnalyzer
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.service.NarrativeQualityAuditInput
import com.localnovel.engine.service.NarrativeQualityAuditService
import com.localnovel.engine.service.MemoryRetriever
import com.localnovel.engine.service.NovelizationAcceptedDraftPacketService
import com.localnovel.engine.service.NovelizationDraftIntakeValidationService
import com.localnovel.engine.service.NovelizationPreflightValidationService
import com.localnovel.engine.service.NovelizationProseQualityAuditService
import com.localnovel.engine.service.NovelizationRevisedDraftIntakeValidationService
import com.localnovel.engine.service.NovelizationRevisionBriefService
import com.localnovel.engine.service.NovelizationScriptPacketPreflightService
import com.localnovel.engine.service.NovelizationWriterBriefValidationService
import com.localnovel.engine.service.PilotPreflightService
import com.localnovel.engine.service.SessionEvidenceExporter
import com.localnovel.engine.service.SettingPackDiffService
import com.localnovel.engine.service.SettingPackImportService
import com.localnovel.engine.service.SettingPackParser
import com.localnovel.engine.service.SettingPackSchemaValidator
import com.localnovel.engine.service.SettingPackScriptPacketDraftService
import com.localnovel.engine.service.SettingImageFacingProofPreviewBuildService
import com.localnovel.engine.service.StallDetector
import com.localnovel.engine.service.StateReducer
import com.localnovel.engine.service.StoryboardPacketDraftValidationService
import com.localnovel.engine.service.StoryboardPacketHumanReviewDecisionIntakeService
import com.localnovel.engine.service.StoryboardPacketHumanReviewPreflightService
import com.localnovel.engine.service.StoryboardPacketReviewService
import com.localnovel.engine.service.StoryboardStoryMovementAuditService
import com.localnovel.engine.service.ReviewableStoryboardPacketService
import com.localnovel.engine.service.StoryboardPlanningBriefService
import com.localnovel.engine.service.StoryboardPlanningPreflightService
import com.localnovel.engine.service.ReplayAuditService
import com.localnovel.engine.service.ScriptPacketDraftSourceBindingValidationService
import com.localnovel.engine.service.ScriptPacketDraftStructureBridgeService
import com.localnovel.engine.service.ScriptPacketDraftValidationService
import com.localnovel.engine.service.TimelineBranchService
import com.localnovel.engine.service.TurnRunner
import com.localnovel.engine.service.WorldSourceArchiveImportService
import com.localnovel.engine.service.WorldSourceQueryService
import com.localnovel.engine.service.canonicalHash
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    exitProcess(Cli().run(args.toList()))
}

class Cli {
    fun run(args: List<String>): Int {
        if (args.isEmpty() || args.first() in setOf("-h", "--help", "help")) {
            printHelp()
            return 0
        }

        return runCatching {
            val command = args.first()
            val options = parseCliOptions(args.drop(1))
            when (command) {
                "init-db" -> initDb(options)
                "seed-demo" -> seedDemo(options)
                "compile-context" -> compileContext(options)
                "validate-result" -> validateResult(options)
                "apply-result" -> applyResult(options)
                "run-manual-turn" -> runManualTurn(options)
                "show-state" -> showState(options)
                "start-session" -> startSession(options)
                "prepare-turn" -> prepareTurn(options)
                "resolve-turn" -> resolveTurn(options)
                "show-session" -> showSession(options)
                "abort-pending-turn" -> abortPendingTurn(options)
                "show-character-mind" -> showCharacterMind(options)
                "preview-recall" -> previewRecall(options)
                "explain-context" -> explainContext(options)
                "show-scene" -> showScene(options)
                "show-threads" -> showThreads(options)
                "preview-transitions" -> previewTransitions(options)
                "show-world-events" -> showWorldEvents(options)
                "explain-scene-state" -> explainSceneState(options)
                "create-setting-pack-template" -> SettingPackCli.createSettingPackTemplate(options)
                "validate-setting-pack" -> SettingPackCli.validateSettingPack(options)
                "preview-setting-import" -> SettingPackCli.previewSettingImport(options)
                "apply-setting-pack" -> SettingPackCli.applySettingPack(options)
                "validate-world-source-archive" -> WorldSourceAndTaskQueueCli.validateWorldSourceArchive(options)
                "apply-world-source-archive" -> WorldSourceAndTaskQueueCli.applyWorldSourceArchive(options)
                "query-world-source" -> WorldSourceAndTaskQueueCli.queryWorldSource(options)
                "upsert-ai-task" -> WorldSourceAndTaskQueueCli.upsertAiTask(options)
                "claim-ai-task" -> WorldSourceAndTaskQueueCli.claimAiTask(options)
                "complete-ai-task" -> WorldSourceAndTaskQueueCli.completeAiTask(options)
                "list-ai-tasks" -> WorldSourceAndTaskQueueCli.listAiTasks(options)
                "export-setting-pack" -> SettingPackCli.exportSettingPack(options)
                "diff-setting-pack" -> SettingPackCli.diffSettingPack(options)
                "impact-report" -> SettingPackCli.impactReport(options)
                "show-dependencies" -> SettingPackCli.showDependencies(options)
                "validate-project-canon" -> SettingPackCli.validateProjectCanon(options)
                "audit-context" -> auditContext(options)
                "audit-narrative-quality" -> auditNarrativeQuality(options)
                "audit-chapter-continuity" -> auditChapterContinuity(options)
                "create-setting-script-packet-draft" -> createSettingScriptPacketDraft(options)
                "build-setting-image-facing-proof-preview" -> buildSettingImageFacingProofPreview(options)
                "validate-script-packet-draft" -> validateScriptPacketDraft(options)
                "validate-script-packet-source-binding" -> validateScriptPacketSourceBinding(options)
                "validate-script-packet-structure-bridge" -> validateScriptPacketStructureBridge(options)
                "create-accepted-script-packet-handoff-packet" -> createAcceptedScriptPacketHandoffPacket(options)
                "validate-storyboard-planning-preflight" -> validateStoryboardPlanningPreflight(options)
                "create-storyboard-planning-brief" -> createStoryboardPlanningBrief(options)
                "validate-storyboard-packet-draft" -> validateStoryboardPacketDraft(options)
                "create-storyboard-packet-review" -> createStoryboardPacketReview(options)
                "validate-storyboard-packet-human-review-preflight" -> validateStoryboardPacketHumanReviewPreflight(options)
                "validate-storyboard-packet-human-review-decision" -> validateStoryboardPacketHumanReviewDecision(options)
                "audit-storyboard-story-movement" -> auditStoryboardStoryMovement(options)
                "create-reviewable-storyboard-packet" -> createReviewableStoryboardPacket(options)
                "create-image-facing-delivery-proof" -> createImageFacingDeliveryProof(options)
                "create-comic-video-packet" -> createComicVideoPacket(options)
                "create-comic-video-packet-template" -> createComicVideoPacketTemplate(options)
                "create-comic-video-voice-timeline-fill-template" -> createComicVideoVoiceTimelineFillTemplate(options)
                "fill-comic-video-voice-timeline" -> fillComicVideoVoiceTimeline(options)
                "validate-comic-video-packet" -> validateComicVideoPacket(options)
                "create-comic-video-render-handoff-manifest" -> createComicVideoRenderHandoffManifest(options)
                "create-comic-video-production-package-draft" -> createComicVideoProductionPackageDraft(options)
                "validate-comic-video-production-package" -> validateComicVideoProductionPackage(options)
                "validate-local-story-input-pack" -> LocalStoryVideoCli.validateLocalStoryInputPack(options)
                "validate-09-foundation-card-pack" -> LocalStoryVideoCli.validate09FoundationCardPack(options)
                "review-09-foundation-card-pack" -> LocalStoryVideoCli.review09FoundationCardPack(options)
                "draft-local-asset-prompt-cards" -> LocalStoryVideoCli.draftLocalAssetPromptCards(options)
                "draft-local-render-package" -> LocalStoryVideoCli.draftLocalRenderPackage(options)
                "verify-local-story-video-chain" -> LocalStoryVideoCli.verifyLocalStoryVideoChain(options)
                "draft-local-story-video-preview-handoff" -> LocalStoryVideoCli.draftLocalStoryVideoPreviewHandoff(options)
                "review-local-story-video-preview-handoff" -> LocalStoryVideoCli.reviewLocalStoryVideoPreviewHandoff(options)
                "dry-run-local-renderer-adapter" -> LocalStoryVideoCli.dryRunLocalRendererAdapter(options)
                "plan-local-renderer-adapter-implementation" -> LocalStoryVideoCli.planLocalRendererAdapterImplementation(options)
                "review-local-renderer-adapter-implementation-plan" -> LocalStoryVideoCli.reviewLocalRendererAdapterImplementationPlan(options)
                "draft-local-renderer-adapter-implementation" -> LocalStoryVideoCli.draftLocalRendererAdapterImplementation(options)
                "review-local-renderer-adapter-implementation-draft" -> LocalStoryVideoCli.reviewLocalRendererAdapterImplementationDraft(options)
                "plan-local-renderer-manifest-draft" -> LocalStoryVideoCli.planLocalRendererManifestDraft(options)
                "review-local-renderer-manifest-draft-plan" -> LocalStoryVideoCli.reviewLocalRendererManifestDraftPlan(options)
                "draft-local-renderer-manifest-json" -> LocalStoryVideoCli.draftLocalRendererManifestJson(options)
                "review-local-renderer-manifest-draft-json" -> LocalStoryVideoCli.reviewLocalRendererManifestDraftJson(options)
                "dry-run-local-renderer-manifest-draft-json-adapter" ->
                    LocalStoryVideoCli.dryRunLocalRendererManifestDraftJsonAdapter(options)
                "plan-local-renderer-manifest-adapter-contract" ->
                    LocalStoryVideoCli.planLocalRendererManifestAdapterContract(options)
                "review-local-renderer-manifest-adapter-contract-plan" ->
                    LocalStoryVideoCli.reviewLocalRendererManifestAdapterContractPlan(options)
                "draft-local-renderer-manifest-adapter-contract" ->
                    LocalStoryVideoCli.draftLocalRendererManifestAdapterContract(options)
                "render-comic-video-local" -> renderComicVideoLocal(options)
                "build-comic-video-local-preview" -> buildComicVideoLocalPreview(options)
                "build-setting-comic-video-local-preview" -> buildSettingComicVideoLocalPreview(options)
                "validate-asset-candidate-preflight" -> validateAssetCandidatePreflight(options)
                "create-asset-candidate-brief" -> createAssetCandidateBrief(options)
                "create-asset-candidate-draft" -> createAssetCandidateDraft(options)
                "validate-asset-candidate-draft" -> validateAssetCandidateDraft(options)
                "validate-asset-candidate-review-preflight" -> validateAssetCandidateReviewPreflight(options)
                "validate-asset-candidate-human-review-decision" -> validateAssetCandidateHumanReviewDecision(options)
                "validate-image-candidate-preflight" -> validateImageCandidatePreflight(options)
                "create-image-candidate-brief" -> createImageCandidateBrief(options)
                "create-image-candidate-prompt-preparation" -> createImageCandidatePromptPreparation(options)
                "validate-image-candidate-prompt-preparation" -> validateImageCandidatePromptPreparation(options)
                "validate-image-candidate-provider-handoff-preflight" -> validateImageCandidateProviderHandoffPreflight(options)
                "validate-image-candidate-provider-handoff-decision" -> validateImageCandidateProviderHandoffDecision(options)
                "create-image-candidate-provider-handoff-packet" -> createImageCandidateProviderHandoffPacket(options)
                "validate-image-candidate-provider-handoff-packet" -> validateImageCandidateProviderHandoffPacket(options)
                "validate-image-candidate-external-result-intake" -> validateImageCandidateExternalResultIntake(options)
                "validate-image-candidate-result-review-preflight" -> validateImageCandidateResultReviewPreflight(options)
                "validate-image-candidate-result-human-review-decision" -> validateImageCandidateResultHumanReviewDecision(options)
                "create-image-candidate-reference-packet" -> createImageCandidateReferencePacket(options)
                "validate-image-candidate-reference-packet" -> validateImageCandidateReferencePacket(options)
                "validate-candidate-reference-human-acceptance-decision" -> validateCandidateReferenceHumanAcceptanceDecision(options)
                "validate-candidate-reference-registry-preflight" -> validateCandidateReferenceRegistryPreflight(options)
                "create-candidate-reference-registry-packet" -> createCandidateReferenceRegistryPacket(options)
                "validate-candidate-reference-registry-packet" -> validateCandidateReferenceRegistryPacket(options)
                "validate-candidate-reference-registry-human-acceptance-decision" -> validateCandidateReferenceRegistryHumanAcceptanceDecision(options)
                "create-candidate-reference-acceptance-packet" -> createCandidateReferenceAcceptancePacket(options)
                "validate-candidate-reference-acceptance-packet" -> validateCandidateReferenceAcceptancePacket(options)
                "create-character-nuwa-card-template" -> createCharacterNuwaCardTemplate(options)
                "validate-character-nuwa-gate" -> validateCharacterNuwaGate(options)
                "validate-novelization-preflight" -> validateNovelizationPreflight(options)
                "create-novelization-writer-brief-template" -> createNovelizationWriterBriefTemplate(options)
                "validate-novelization-writer-brief" -> validateNovelizationWriterBrief(options)
                "validate-novelization-draft-intake" -> validateNovelizationDraftIntake(options)
                "audit-novelization-prose-quality" -> auditNovelizationProseQuality(options)
                "create-novelization-revision-brief" -> createNovelizationRevisionBrief(options)
                "validate-novelization-revised-draft-intake" -> validateNovelizationRevisedDraftIntake(options)
                "create-novelization-accepted-draft-packet" -> createNovelizationAcceptedDraftPacket(options)
                "validate-novelization-script-packet-preflight" -> validateNovelizationScriptPacketPreflight(options)
                "export-session-evidence" -> exportSessionEvidence(options)
                "pilot-preflight" -> pilotPreflight(options)
                "explain-context-budget" -> explainContextBudget(options)
                "explain-capability" -> explainCapability(options)
                "create-checkpoint" -> createCheckpoint(options)
                "list-checkpoints" -> listCheckpoints(options)
                "show-checkpoint" -> showCheckpoint(options)
                "fork-session" -> forkSession(options)
                "checkout-branch" -> checkoutBranch(options)
                "show-timeline" -> showTimeline(options)
                "verify-replay-chain" -> verifyReplayChain(options)
                "export-timeline" -> exportTimeline(options)
                else -> {
                    System.err.println("Unknown command: $command")
                    printHelp()
                    2
                }
            }
        }.getOrElse { error ->
            System.err.println(error.message ?: error.toString())
            1
        }
    }

    private fun initDb(options: Map<String, String>): Int {
        cliRepository(options).initialize()
        println("Initialized SQLite database: ${options.requiredCliOption("db")}")
        return 0
    }

    private fun seedDemo(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val seedPath = Path.of(options["seed"] ?: "samples/demo_project_seed.json")
        val seed = JsonSupport.json.decodeFromString<DemoProjectSeed>(Files.readString(seedPath))
        repository.seedDemo(seed)
        println("Seeded demo project '${seed.project.id}' into ${options.requiredCliOption("db")}")
        return 0
    }

    private fun compileContext(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val context = ContextCompiler(repository).compile(
            projectId = options.requiredCliOption("project"),
            playerInput = options.requiredCliOption("player-input"),
        )
        val out = Path.of(options.requiredCliOption("out"))
        out.parent?.let { Files.createDirectories(it) }
        Files.writeString(out, JsonSupport.json.encodeToString(context))
        println("Wrote context packet: $out")
        return 0
    }

    private fun validateResult(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val context = readCliJson<ContextPacket>(Path.of(options.requiredCliOption("context")))
        val result = readCliJson<AiTurnResult>(Path.of(options.requiredCliOption("result")))
        val report = AiTurnResultValidator(repository).validate(context, result)
        return if (report.isValid) {
            println("Result is valid.")
            0
        } else {
            report.errors.forEach { System.err.println("${it.code}: ${it.message}") }
            1
        }
    }

    private fun applyResult(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val runner = cliRunner(repository)
        val context = readCliJson<ContextPacket>(Path.of(options.requiredCliOption("context")))
        val result = readCliJson<AiTurnResult>(Path.of(options.requiredCliOption("result")))
        val applyResult = runner.apply(context, result)
        println(applyResult.narrative)
        if (StallDetector(repository).hasStalled(context.project.id)) {
            println()
            println("[stall] Consecutive turns without state change detected.")
        }
        return 0
    }

    private fun runManualTurn(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val runner = cliRunner(repository)
        val context = runner.compile(
            projectId = options.requiredCliOption("project"),
            playerInput = options.requiredCliOption("player-input"),
        )
        val contextOut = Path.of(options.requiredCliOption("context-out"))
        contextOut.parent?.let { Files.createDirectories(it) }
        Files.writeString(contextOut, JsonSupport.json.encodeToString(context))

        val resultPath = Path.of(options.requiredCliOption("result"))
        if (!Files.exists(resultPath)) {
            println("Wrote context packet: $contextOut")
            println("Create ai_turn_result JSON at: $resultPath")
            return 0
        }

        val result = readCliJson<AiTurnResult>(resultPath)
        val applyResult = runner.apply(context, result)
        println(applyResult.narrative)
        return 0
    }

    private fun showState(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val projectId = options.requiredCliOption("project")
        val project = repository.getProject(projectId) ?: error("Project not found: $projectId")
        println("${project.name} (${project.id}) turn=${project.currentTurn}")
        println(project.summary)
        println()
        println("Active scene:")
        println(repository.getActiveScene(projectId)?.let { "${it.title}: ${it.summary}" } ?: "(none)")
        println()
        println("Characters:")
        repository.listCharacters(projectId).forEach { character ->
            val state = repository.getCharacterState(projectId, character.id)
            println("- ${character.name} [${character.role}] mood=${state?.mood ?: "?"} health=${state?.health ?: "?"}")
        }
        println()
        println("Facts:")
        repository.listFacts(projectId).forEach { println("- ${it.content}") }
        println()
        println("Open tasks:")
        repository.listOpenTasks(projectId).forEach { println("- [${it.status}] ${it.title}") }
        return 0
    }

    private fun startSession(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val session = MultiTurnRuntime(repository).startSession(
            projectId = options.requiredCliOption("project"),
            sceneId = options.requiredCliOption("scene"),
        )
        println("sessionId=${session.id}")
        println("branchId=${session.branchId ?: ""}")
        println("currentSceneId=${session.currentSceneId}")
        println("nextTurnNumber=${session.nextTurnNumber}")
        println("stateVersion=${session.stateVersion}")
        return 0
    }

    private fun prepareTurn(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val sessionId = options.requiredCliOption("session")
        val playerInput = options["input"]
            ?: options["input-file"]?.let { Files.readString(Path.of(it)).removePrefix("\uFEFF") }
            ?: error("Missing required option --input or --input-file")
        val out = Path.of(options.requiredCliOption("out"))
        val context = try {
            MultiTurnRuntime(repository).prepareTurn(
                sessionId = sessionId,
                playerInput = playerInput,
            )
        } catch (error: ContextBudgetUnsatisfiableException) {
            val report = error.report ?: MultiTurnRuntime(repository).explainContextBudget(sessionId, playerInput)
            val reportPath = options["budget-report"]?.let { Path.of(it) } ?: out.budgetFailurePath()
            writeCliJson(reportPath, report)
            println("budgetFailureReport=$reportPath")
            throw error
        }
        out.parent?.let { Files.createDirectories(it) }
        Files.writeString(out, JsonSupport.json.encodeToString(context))
        println("contextId=${context.contextId}")
        println("contextHash=${context.contextHash}")
        println("turnNumber=${context.turnNumber}")
        println("baseStateVersion=${context.baseStateVersion}")
        println("wrote=$out")
        return 0
    }

    private fun resolveTurn(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val sessionId = options.requiredCliOption("session")
        val result = readCliJson<AiTurnResult>(Path.of(options.requiredCliOption("result")))
        val applied = MultiTurnRuntime(repository).resolveTurn(sessionId, result)
        val session = repository.getSession(sessionId)
        println(applied.narrative)
        println()
        println("turnLogId=${applied.turnLogId}")
        println("stateVersion=${session?.stateVersion}")
        println("nextTurnNumber=${session?.nextTurnNumber}")
        return 0
    }

    private fun showSession(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val sessionId = options.requiredCliOption("session")
        val session = repository.getSession(sessionId) ?: error("Session not found: $sessionId")
        val project = repository.getProject(session.projectId) ?: error("Project not found: ${session.projectId}")
        val scene = repository.getActiveScene(session.projectId)
        val shen = repository.getCharacterState(session.projectId, "char-shen-shuang")
        println("sessionId=${session.id}")
        println("branchId=${session.branchId ?: ""}")
        println("project=${project.name}")
        println("currentScene=${scene?.title ?: session.currentSceneId}")
        println("nextTurnNumber=${session.nextTurnNumber}")
        println("stateVersion=${session.stateVersion}")
        println("recentTurnSummaries:")
        repository.listRecentTurnSummaries(session.id, 3).forEach { println("- ${it.turnNumber}: ${it.summary}") }
        println("shenShuangMood=${shen?.mood ?: "?"}")
        println("shenShuangGoal=${shen?.flags?.get("current_goal") ?: "?"}")
        println("idleTurnCount=${shen?.flags?.get("idle_turn_count") ?: "0"}")
        println("revealedFacts:")
        repository.listFacts(session.projectId).forEach { println("- ${it.content}") }
        println("unrevealedSecrets=${repository.listSecrets(session.projectId).count { !it.revealed }}")
        println("sceneProgress=${scene?.progress ?: "{}"}")
        println("completedRequirements=${scene?.completedRequirements?.joinToString() ?: ""}")
        return 0
    }

    private fun abortPendingTurn(options: Map<String, String>): Int {
        val count = cliRepository(options).abortPendingContext(
            sessionId = options.requiredCliOption("session"),
            contextId = options["context"],
        )
        println("aborted=$count")
        return 0
    }

    private fun showScene(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val session = repository.getSession(options.requiredCliOption("session")) ?: error("Session not found: ${options.requiredCliOption("session")}")
        val scene = repository.getActiveScene(session.projectId)
        val lifecycle = repository.getSceneLifecycle(session.id)
        println("sceneInstanceId=${lifecycle.sceneInstanceId}")
        println("sceneId=${lifecycle.sceneId}")
        println("sceneTitle=${scene?.title ?: lifecycle.sceneId}")
        println("status=${lifecycle.status}")
        println("visitNumber=${lifecycle.visitNumber}")
        println("readyToClose=${lifecycle.readyToClose}")
        println("worldTime=${session.worldTime}")
        println("elapsedMinutes=${session.elapsedMinutes}")
        println("requirements:")
        repository.listSceneRequirementViews(session.id).forEach {
            println("- ${it.requirementId} [${it.type}] status=${it.status} progress=${it.progress}: ${it.description}")
        }
        println("availableTransitions:")
        repository.listAvailableTransitions(session.id, options["input"].orEmpty()).forEach {
            println("- ${it.transitionId} -> ${it.toSceneId} ok=${it.prerequisitesSatisfied} travel=${it.travelMinutes} forced=${it.forcedAllowed} reason=${it.visibleReason}")
        }
        println("activeThreads:")
        repository.listActiveStoryThreadViews(session.id).forEach {
            println("- ${it.threadId} [${it.type}] status=${it.status} progress=${it.progress} urgency=${it.urgency}: ${it.publicSummary}")
        }
        println("pendingWorldEvents:")
        repository.listPendingWorldEventViews(session.id).forEach {
            println("- ${it.eventId} [${it.type}] ${it.publicEffect}")
        }
        return 0
    }

    private fun showThreads(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val session = repository.getSession(options.requiredCliOption("session")) ?: error("Session not found: ${options.requiredCliOption("session")}")
        repository.listActiveStoryThreadViews(session.id).forEach {
            println("${it.threadId} [${it.type}] status=${it.status} progress=${it.progress} urgency=${it.urgency}")
            println("  publicSummary=${it.publicSummary}")
            println("  directorOnlyGuidance=${it.directorGuidance}")
        }
        return 0
    }

    private fun previewTransitions(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val sessionId = options.requiredCliOption("session")
        val input = options.requiredCliOption("input")
        repository.listAvailableTransitions(sessionId, input).forEach {
            println("${it.transitionId} -> ${it.toSceneId}")
            println("  ok=${it.prerequisitesSatisfied}")
            println("  requiredIntent=${it.playerIntentRequired}")
            println("  unmet=${it.unmetPrerequisites.joinToString("; ")}")
            println("  travelMinutes=${it.travelMinutes}")
            println("  forcedAllowed=${it.forcedAllowed}")
        }
        return 0
    }

    private fun showWorldEvents(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val session = repository.getSession(options.requiredCliOption("session")) ?: error("Session not found: ${options.requiredCliOption("session")}")
        repository.tableRows("scheduled_world_events")
            .filter { it["project_id"] == session.projectId && (it["session_id"].isNullOrBlank() || it["session_id"] == session.id) }
            .sortedWith(compareBy<Map<String, String>> { it["due_turn_number"]?.toIntOrNull() ?: Int.MAX_VALUE }.thenBy { it["id"] })
            .forEach {
                println("${it["id"]} [${it["event_type"]}] status=${it["status"]} dueTurn=${it["due_turn_number"]} triggered=${it["triggered_turn_number"]} consumed=${it["consumed_turn_number"]}")
            }
        return 0
    }

    private fun explainSceneState(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val session = repository.getSession(options.requiredCliOption("session")) ?: error("Session not found: ${options.requiredCliOption("session")}")
        val lifecycle = repository.getSceneLifecycle(session.id)
        println("sceneInstanceId=${lifecycle.sceneInstanceId}")
        println("status=${lifecycle.status}")
        println("readyToClose=${lifecycle.readyToClose}")
        println("closureReasons=${lifecycle.closureReasons.joinToString("; ")}")
        repository.listSceneRequirementViews(session.id).forEach {
            val blocking = it.type == "REQUIRED" && it.status != "COMPLETED"
            println("- ${it.requirementId} type=${it.type} status=${it.status} progress=${it.progress} blocking=$blocking")
        }
        return 0
    }

    private fun showCharacterMind(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val session = repository.getSession(options.requiredCliOption("session")) ?: error("Session not found: ${options.requiredCliOption("session")}")
        val characterId = options.requiredCliOption("character")
        val state = repository.getCharacterState(session.projectId, characterId) ?: error("Character state not found: $characterId")
        val memories = repository.listAllCharacterMemories(session.projectId).filter { it.ownerCharacterId == characterId || it.characterId == characterId }
        val activeMemories = memories
            .filter { it.status == "active" && it.visibility != "author_only" && it.visibility != "inaccessible" }
            .sortedWith(compareByDescending<com.localnovel.engine.model.CharacterMemory> { it.salience }.thenBy { it.id })
            .take(5)
        val claims = repository.listKnowledgeClaims(session.projectId, characterId)
        println("characterId=$characterId")
        println("currentGoal=${state.flags["current_goal"] ?: "?"}")
        println("mood=${state.mood}")
        println("dominantEmotions:")
        state.flags.filterKeys { it.startsWith("emotion.") && !it.contains(".last_") }.forEach { (key, value) -> println("- ${key.removePrefix("emotion.")}=$value") }
        println("topActiveMemories:")
        activeMemories.forEach { println("- ${it.id} [${it.memoryType}] salience=${it.salience} confidence=${it.confidence}: ${it.summary}") }
        println("knowledgeClaims:")
        claims.forEach { println("- ${it.id} ${it.epistemicState}/${it.confidence}: ${it.statement}") }
        println("suspectedOrDoubted:")
        claims.filter { it.epistemicState in setOf("SUSPECTED", "DOUBTED", "BELIEVED", "MISREMEMBERED") }
            .forEach { println("- ${it.propositionKey}: ${it.epistemicState}") }
        println("relationshipState=${state.flags["relationship_state"] ?: "unknown"}")
        println("inaccessibleMemoryCount=${memories.count { it.visibility in setOf("author_only", "inaccessible") || it.status != "active" }}")
        return 0
    }

    private fun previewRecall(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val sessionId = options.requiredCliOption("session")
        val characterId = options.requiredCliOption("character")
        val input = options.requiredCliOption("input")
        val memories = MemoryRetriever(repository).preview(sessionId, characterId, input)
        println("candidateMemories:")
        memories.forEachIndexed { index, memory ->
            println("- ${memory.memoryId} score=${memory.retrievalScore} included=${index < 5}")
            println("  reasons=${memory.retrievalReasons.joinToString("; ")}")
        }
        println("budgetImpact=included=${memories.take(5).size}, omitted=${(memories.size - 5).coerceAtLeast(0)}")
        return 0
    }

    private fun explainContext(options: Map<String, String>): Int {
        val context = readCliJson<ContextPacket>(Path.of(options.requiredCliOption("file")))
        println("contextId=${context.contextId}")
        println("contextBytes=${context.contextBudget.actualBytes}/${context.contextBudget.maxBytes}")
        println("safetyFieldsComplete=${context.sceneContract?.forbiddenReveals != null && context.worldRules.isNotEmpty()}")
        println("forbiddenReveals=${context.sceneContract?.forbiddenReveals?.size ?: 0}")
        println("retrievedMemories:")
        context.retrievedMemories.forEach {
            println("- ${it.memoryId} owner=${it.ownerCharacterId} score=${it.retrievalScore} reasons=${it.retrievalReasons.joinToString("; ")}")
        }
        println("knowledgeClaims:")
        context.knowledgeClaims.forEach {
            println("- ${it.claimId} character=${it.characterId} state=${it.epistemicState} confidence=${it.confidence}")
        }
        println("omittedMemories=${context.contextBudget.omittedMemoryCount}")
        println("omittedKnowledge=${context.contextBudget.omittedKnowledgeCount}")
        println("truncationApplied=${context.contextBudget.truncationApplied}")
        context.contextBudget.truncationReasonCodes.forEach { println("- $it") }
        return 0
    }

    private fun auditContext(options: Map<String, String>): Int {
        val file = Path.of(options.requiredCliOption("file"))
        val report = ContextAuditService().auditFile(file)
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("valid=${report.valid}")
        println("actualBytes=${report.actualBytes}/${report.maxBytes}")
        println("declaredActualBytes=${report.declaredActualBytes}")
        println("hashValid=${report.hashValid}")
        report.violations.forEach { println("violation=$it") }
        return if (report.valid) 0 else 1
    }

    private fun auditNarrativeQuality(options: Map<String, String>): Int {
        val contextPath = Path.of(options.requiredCliOption("context"))
        val resultPath = Path.of(options.requiredCliOption("result"))
        val evidencePath = options["evidence"]?.let { Path.of(it) }
        val evidenceText = evidencePath?.let { Files.readString(it).removePrefix("\uFEFF") }.orEmpty()
        val report = NarrativeQualityAuditService().audit(
            context = readCliJson<ContextPacket>(contextPath),
            result = readCliJson<AiTurnResult>(resultPath),
            input = NarrativeQualityAuditInput(
                contextFile = contextPath.toString(),
                resultFile = resultPath.toString(),
                evidenceFile = evidencePath?.toString().orEmpty(),
                evidenceText = evidenceText,
            ),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("score=${report.score}")
        println("severity=${report.severity}")
        println("findings=${report.findings.size}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.message}")
        }
        return 0
    }

    private fun createSettingScriptPacketDraft(options: Map<String, String>): Int {
        val settingPackPath = Path.of(options.requiredCliOption("setting-pack"))
        val draftPath = Path.of(options.requiredCliOption("out"))
        val validationPath = options["validation-out"]?.let { Path.of(it) } ?: derivedJsonPath(draftPath, "_validation")
        val reportPath = options["report-out"]?.let { Path.of(it) } ?: derivedJsonPath(draftPath, "_build_report")
        val pack = readCliJson<SettingPack>(settingPackPath)
        val result = SettingPackScriptPacketDraftService().create(
            settingPack = pack,
            settingPackFile = settingPackPath.toString(),
            requestedSceneId = options["scene"] ?: "",
            packetId = options["packet"] ?: "",
        )
        val findings = result.findings.toMutableList()
        var validationVerdict = ""
        var validationFindingCount = 0
        var outputPacketId = ""

        val draft = result.draft
        if (draft != null) {
            outputPacketId = draft.packetId
            writeCliJson(draftPath, draft)
            val validation = ScriptPacketDraftValidationService().validate(
                packet = draft,
                packetFile = draftPath.toString(),
            )
            validationVerdict = validation.verdict
            validationFindingCount = validation.findingCount
            writeCliJson(validationPath, validation)
            if (validation.verdict == "FAIL") {
                findings += settingScriptPacketDraftBuildFinding(
                    ruleId = "SCRIPT_PACKET_DRAFT_VALIDATION_FAILED",
                    category = "validation",
                    severity = "CRITICAL",
                    path = validationPath.toString(),
                    message = "Setting-pack script packet draft creation requires the generated draft to pass existing draft validation.",
                    evidence = validation.findings.map { it.ruleId },
                )
            }
        }

        val hasBlockingFinding = findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }
        val verdict = when {
            draft == null || hasBlockingFinding -> "FAIL"
            validationVerdict == "PASS_WITH_WARNINGS" || findings.isNotEmpty() -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }
        val buildId = (options["build"] ?: "").ifBlank {
            "setting-script-packet-draft-${pack.packId}-${result.selectedSceneId.ifBlank { "unselected" }}"
        }
        val report = SettingPackScriptPacketDraftBuildReport(
            verdict = verdict,
            buildId = buildId,
            settingPackFile = settingPackPath.toString(),
            scriptPacketDraftFile = if (draft != null) draftPath.toString() else "",
            scriptPacketValidationFile = if (validationVerdict.isNotBlank()) validationPath.toString() else "",
            packId = pack.packId,
            packVersion = pack.packVersion,
            projectId = pack.projectId,
            selectedSceneId = result.selectedSceneId,
            selectedLocationId = result.selectedLocationId,
            outputPacketId = outputPacketId,
            validationVerdict = validationVerdict,
            nextAllowedDownstreamStep = if (verdict != "FAIL") "script_packet_draft_review" else "",
            blockedReason = if (verdict == "FAIL") {
                findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId }
            } else {
                ""
            },
            findingCount = findings.size,
            findings = findings,
            evidence = result.evidence.withValidationFindingCount(validationFindingCount),
        )
        writeCliJson(reportPath, report)
        printSettingScriptPacketDraftBuildReport(report)
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun derivedJsonPath(base: Path, suffix: String): Path {
        val fileName = base.fileName?.toString() ?: "output.json"
        val stem = fileName.removeSuffix(".json")
        return base.parent?.resolve("$stem$suffix.json") ?: Path.of("$stem$suffix.json")
    }

    private fun SettingPackScriptPacketDraftBuildEvidence.withValidationFindingCount(
        validationFindingCount: Int,
    ): SettingPackScriptPacketDraftBuildEvidence =
        copy(validationFindingCount = validationFindingCount)

    private fun settingScriptPacketDraftBuildFinding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): SettingPackScriptPacketDraftBuildFinding =
        SettingPackScriptPacketDraftBuildFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private fun printSettingScriptPacketDraftBuildReport(report: SettingPackScriptPacketDraftBuildReport) {
        println("verdict=${report.verdict}")
        println("buildId=${report.buildId}")
        println("packId=${report.packId}")
        println("projectId=${report.projectId}")
        println("selectedSceneId=${report.selectedSceneId}")
        println("selectedLocationId=${report.selectedLocationId}")
        println("outputPacketId=${report.outputPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketValidationFile=${report.scriptPacketValidationFile}")
        println("validationVerdict=${report.validationVerdict}")
        println("generatedBeats=${report.evidence.generatedBeatCount}")
        println("generatedFrameCandidates=${report.evidence.generatedFrameCandidateCount}")
        println("excludedSecrets=${report.evidence.excludedSecretCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
    }

    private fun buildSettingImageFacingProofPreview(options: Map<String, String>): Int {
        val settingPackPath = Path.of(options.requiredCliOption("setting-pack"))
        val outputDir = Path.of(options.requiredCliOption("output-dir"))
        Files.createDirectories(outputDir)

        val scriptDraftPath = outputDir.resolve("script_packet_draft.json")
        val scriptValidationPath = outputDir.resolve("script_packet_draft_validation.json")
        val settingBuildReportPath = outputDir.resolve("setting_script_packet_draft_build_report.json")
        val planningBriefPath = outputDir.resolve("storyboard_planning_brief.json")
        val storyboardDraftPath = outputDir.resolve("storyboard_packet_draft.json")
        val storyboardDraftValidationPath = outputDir.resolve("storyboard_packet_draft_validation.json")
        val storyboardReviewPath = outputDir.resolve("storyboard_packet_review.json")
        val humanReviewPreflightPath = outputDir.resolve("storyboard_human_review_preflight.json")
        val storyMovementAuditPath = outputDir.resolve("storyboard_story_movement_audit.json")
        val reviewableStoryboardPath = outputDir.resolve("reviewable_storyboard_packet.json")
        val proofPath = outputDir.resolve("image_facing_delivery_proof.json")
        val reportPath = options["report-out"]?.let { Path.of(it) }
            ?: outputDir.resolve("setting_image_facing_proof_preview_build_report.json")

        val buildId = (options["build"] ?: "").ifBlank { "setting-image-facing-proof-preview-build" }
        val findings = mutableListOf<SettingImageFacingProofPreviewBuildFinding>()
        val pack = readCliJson<SettingPack>(settingPackPath)
        val previewService = SettingImageFacingProofPreviewBuildService()

        var settingBuildReport: SettingPackScriptPacketDraftBuildReport? = null
        var scriptValidation: ScriptPacketDraftValidationReport? = null
        var planningBrief: StoryboardPlanningBrief? = null
        var storyboardDraft: StoryboardPacketDraft? = null
        var storyboardDraftValidation: StoryboardPacketDraftValidationReport? = null
        var storyboardReview: StoryboardPacketReviewPacket? = null
        var humanReviewPreflight: StoryboardPacketHumanReviewPreflightReport? = null
        var storyMovementAudit: StoryboardStoryMovementAuditReport? = null
        var reviewableStoryboard: com.localnovel.engine.model.ReviewableStoryboardPacket? = null
        var proof: ImageFacingDeliveryProof? = null

        try {
            val settingResult = SettingPackScriptPacketDraftService().create(
                settingPack = pack,
                settingPackFile = settingPackPath.toString(),
                requestedSceneId = options["scene"] ?: "",
                packetId = options["script-packet"] ?: "",
            )
            val scriptDraft = settingResult.draft
            if (scriptDraft == null) {
                settingBuildReport = SettingPackScriptPacketDraftBuildReport(
                    verdict = "FAIL",
                    buildId = "$buildId-setting-script-draft",
                    settingPackFile = settingPackPath.toString(),
                    packId = pack.packId,
                    packVersion = pack.packVersion,
                    projectId = pack.projectId,
                    selectedSceneId = settingResult.selectedSceneId,
                    selectedLocationId = settingResult.selectedLocationId,
                    findingCount = settingResult.findings.size,
                    findings = settingResult.findings,
                    evidence = settingResult.evidence,
                )
                writeCliJson(settingBuildReportPath, settingBuildReport)
                findings += previewBuildFinding(
                    ruleId = "SETTING_SCRIPT_PACKET_DRAFT_BUILD_FAILED",
                    category = "setting_script_draft",
                    severity = "CRITICAL",
                    path = settingBuildReportPath.toString(),
                    message = "Setting image-facing preview requires a generated setting script packet draft.",
                    evidence = settingResult.findings.map { it.ruleId },
                )
            } else {
                writeCliJson(scriptDraftPath, scriptDraft)
                val scriptText = Files.readString(scriptDraftPath).removePrefix("\uFEFF")
                scriptValidation = ScriptPacketDraftValidationService().validate(
                    packet = scriptDraft,
                    packetFile = scriptDraftPath.toString(),
                )
                writeCliJson(scriptValidationPath, scriptValidation)
                val settingBuildVerdict = when {
                    scriptValidation?.verdict == "FAIL" -> "FAIL"
                    settingResult.findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
                    scriptValidation?.verdict == "PASS_WITH_WARNINGS" || settingResult.findings.isNotEmpty() -> "PASS_WITH_WARNINGS"
                    else -> "PASS"
                }
                settingBuildReport = SettingPackScriptPacketDraftBuildReport(
                    verdict = settingBuildVerdict,
                    buildId = "$buildId-setting-script-draft",
                    settingPackFile = settingPackPath.toString(),
                    scriptPacketDraftFile = scriptDraftPath.toString(),
                    scriptPacketValidationFile = scriptValidationPath.toString(),
                    packId = pack.packId,
                    packVersion = pack.packVersion,
                    projectId = pack.projectId,
                    selectedSceneId = settingResult.selectedSceneId,
                    selectedLocationId = settingResult.selectedLocationId,
                    outputPacketId = scriptDraft.packetId,
                    validationVerdict = scriptValidation?.verdict.orEmpty(),
                    nextAllowedDownstreamStep = if (settingBuildVerdict == "FAIL") "" else "script_packet_draft_review",
                    blockedReason = if (settingBuildVerdict == "FAIL") {
                        (settingResult.findings.map { it.ruleId } + scriptValidation?.findings.orEmpty().map { it.ruleId })
                            .filter { it.isNotBlank() }
                            .distinct()
                            .joinToString("; ")
                    } else {
                        ""
                    },
                    findingCount = settingResult.findings.size,
                    findings = settingResult.findings,
                    evidence = settingResult.evidence.withValidationFindingCount(scriptValidation?.findingCount ?: 0),
                )
                writeCliJson(settingBuildReportPath, settingBuildReport)
                addStageFailure(
                    findings = findings,
                    verdict = settingBuildReport?.verdict.orEmpty(),
                    stageRuleId = "SETTING_SCRIPT_PACKET_DRAFT_BUILD_NOT_PASS",
                    category = "setting_script_draft",
                    path = settingBuildReportPath,
                    message = "Setting image-facing preview requires a non-failing setting script draft build.",
                    evidence = settingBuildReport?.findings.orEmpty().map { it.ruleId },
                )

                planningBrief = previewService.createPlanningBrief(
                    scriptPacketDraft = scriptDraft,
                    scriptPacketDraftText = scriptText,
                    scriptPacketDraftFile = scriptDraftPath.toString(),
                    scriptPacketValidation = scriptValidation ?: error("Missing script validation"),
                    settingBuildReport = settingBuildReport ?: error("Missing setting build report"),
                    settingBuildReportFile = settingBuildReportPath.toString(),
                    briefId = options["storyboard-brief"] ?: "",
                )
                writeCliJson(planningBriefPath, planningBrief)
                addStageFailure(
                    findings = findings,
                    verdict = planningBrief?.verdict.orEmpty(),
                    stageRuleId = "STORYBOARD_PLANNING_BRIEF_FAILED",
                    category = "storyboard_planning",
                    path = planningBriefPath,
                    message = "Setting image-facing preview requires PASS local preview storyboard planning brief.",
                    evidence = planningBrief?.findings.orEmpty().map { it.ruleId },
                )

                storyboardDraft = previewService.createStoryboardDraft(
                    scriptPacketDraft = scriptDraft,
                    planningBrief = planningBrief ?: error("Missing planning brief"),
                    planningBriefFile = planningBriefPath.toString(),
                    draftId = options["storyboard-draft"] ?: "",
                )
                writeCliJson(storyboardDraftPath, storyboardDraft)

                storyboardDraftValidation = StoryboardPacketDraftValidationService().validate(
                    storyboardPlanningBrief = planningBrief ?: error("Missing planning brief"),
                    storyboardPacketDraft = storyboardDraft ?: error("Missing storyboard draft"),
                    storyboardPlanningBriefFile = planningBriefPath.toString(),
                    storyboardPacketDraftFile = storyboardDraftPath.toString(),
                )
                writeCliJson(storyboardDraftValidationPath, storyboardDraftValidation)
                addStageFailure(
                    findings = findings,
                    verdict = storyboardDraftValidation?.verdict.orEmpty(),
                    stageRuleId = "STORYBOARD_PACKET_DRAFT_VALIDATION_FAILED",
                    category = "storyboard_draft",
                    path = storyboardDraftValidationPath,
                    message = "Setting image-facing preview requires PASS storyboard packet draft validation.",
                    evidence = storyboardDraftValidation?.findings.orEmpty().map { it.ruleId },
                )

                storyboardReview = StoryboardPacketReviewService().create(
                    storyboardPacketDraft = storyboardDraft ?: error("Missing storyboard draft"),
                    storyboardPacketDraftReport = storyboardDraftValidation ?: error("Missing storyboard draft validation"),
                    reviewId = options["storyboard-review"] ?: "",
                    storyboardPacketDraftFile = storyboardDraftPath.toString(),
                    storyboardPacketDraftReportFile = storyboardDraftValidationPath.toString(),
                )
                writeCliJson(storyboardReviewPath, storyboardReview)
                addStageFailure(
                    findings = findings,
                    verdict = storyboardReview?.verdict.orEmpty(),
                    stageRuleId = "STORYBOARD_PACKET_REVIEW_FAILED",
                    category = "storyboard_review",
                    path = storyboardReviewPath,
                    message = "Setting image-facing preview requires PASS storyboard packet review.",
                    evidence = storyboardReview?.findings.orEmpty().map { it.ruleId },
                )

                humanReviewPreflight = StoryboardPacketHumanReviewPreflightService().validate(
                    storyboardPacketReview = storyboardReview ?: error("Missing storyboard review"),
                    preflightId = options["human-review-preflight"] ?: "",
                    storyboardPacketReviewFile = storyboardReviewPath.toString(),
                )
                writeCliJson(humanReviewPreflightPath, humanReviewPreflight)
                addStageFailure(
                    findings = findings,
                    verdict = humanReviewPreflight?.verdict.orEmpty(),
                    stageRuleId = "STORYBOARD_HUMAN_REVIEW_PREFLIGHT_FAILED",
                    category = "storyboard_review",
                    path = humanReviewPreflightPath,
                    message = "Setting image-facing preview requires PASS human-review preflight readiness.",
                    evidence = humanReviewPreflight?.findings.orEmpty().map { it.ruleId },
                )

                storyMovementAudit = StoryboardStoryMovementAuditService().audit(
                    storyboardPacketDraft = storyboardDraft ?: error("Missing storyboard draft"),
                    storyboardPacketReview = storyboardReview ?: error("Missing storyboard review"),
                    storyboardHumanReviewPreflight = humanReviewPreflight ?: error("Missing human review preflight"),
                    storyboardPacketDraftFile = storyboardDraftPath.toString(),
                    storyboardPacketReviewFile = storyboardReviewPath.toString(),
                    storyboardHumanReviewPreflightFile = humanReviewPreflightPath.toString(),
                    auditId = options["story-movement-audit"] ?: "",
                )
                writeCliJson(storyMovementAuditPath, storyMovementAudit)
                addStageFailure(
                    findings = findings,
                    verdict = storyMovementAudit?.verdict.orEmpty(),
                    stageRuleId = "STORY_MOVEMENT_AUDIT_FAILED",
                    category = "story_movement",
                    path = storyMovementAuditPath,
                    message = "Setting image-facing preview requires PASS story movement audit.",
                    evidence = storyMovementAudit?.findings.orEmpty().map { it.ruleId },
                )

                reviewableStoryboard = ReviewableStoryboardPacketService().create(
                    storyboardPacketDraft = storyboardDraft ?: error("Missing storyboard draft"),
                    storyboardPacketReview = storyboardReview ?: error("Missing storyboard review"),
                    storyboardHumanReviewPreflight = humanReviewPreflight ?: error("Missing human review preflight"),
                    storyboardStoryMovementAudit = storyMovementAudit ?: error("Missing story movement audit"),
                    storyboardPacketDraftFile = storyboardDraftPath.toString(),
                    storyboardPacketReviewFile = storyboardReviewPath.toString(),
                    storyboardHumanReviewPreflightFile = humanReviewPreflightPath.toString(),
                    storyboardStoryMovementAuditFile = storyMovementAuditPath.toString(),
                    packetId = options["reviewable-storyboard"] ?: "",
                )
                writeCliJson(reviewableStoryboardPath, reviewableStoryboard)
                addStageFailure(
                    findings = findings,
                    verdict = reviewableStoryboard?.verdict.orEmpty(),
                    stageRuleId = "REVIEWABLE_STORYBOARD_PACKET_FAILED",
                    category = "reviewable_storyboard",
                    path = reviewableStoryboardPath,
                    message = "Setting image-facing preview requires PASS reviewable storyboard packet.",
                    evidence = reviewableStoryboard?.findings.orEmpty().map { it.ruleId },
                )

                proof = ImageFacingDeliveryProofService().create(
                    reviewableStoryboardPacket = reviewableStoryboard ?: error("Missing reviewable storyboard"),
                    reviewableStoryboardPacketFile = reviewableStoryboardPath.toString(),
                    proofId = options["proof"] ?: "",
                )
                writeCliJson(proofPath, proof)
                addStageFailure(
                    findings = findings,
                    verdict = proof?.verdict.orEmpty(),
                    stageRuleId = "IMAGE_FACING_DELIVERY_PROOF_FAILED",
                    category = "image_facing",
                    path = proofPath,
                    message = "Setting image-facing preview requires PASS image-facing delivery proof.",
                    evidence = proof?.findings.orEmpty().map { it.ruleId },
                )
            }
        } catch (exception: Exception) {
            findings += previewBuildFinding(
                ruleId = "SETTING_IMAGE_FACING_PREVIEW_BUILD_EXCEPTION",
                category = "execution",
                severity = "CRITICAL",
                path = "$.execution",
                message = "Setting image-facing preview build failed before completing the JSON chain.",
                evidence = listOf(exception::class.java.simpleName, exception.message.orEmpty()),
            )
        }

        val verdict = if (proof?.verdict == "PASS" && findings.none { it.severity == "CRITICAL" || it.severity == "MAJOR" }) {
            "PASS"
        } else {
            "FAIL"
        }
        val report = SettingImageFacingProofPreviewBuildReport(
            verdict = verdict,
            buildId = buildId,
            buildStatus = if (verdict == "PASS") "local_preview_image_facing_proof_built" else "FAIL",
            settingPackFile = settingPackPath.toString(),
            outputDir = outputDir.toString(),
            settingScriptPacketDraftBuildReportFile = settingBuildReportPath.toString(),
            scriptPacketDraftFile = if (Files.exists(scriptDraftPath)) scriptDraftPath.toString() else "",
            scriptPacketValidationFile = if (Files.exists(scriptValidationPath)) scriptValidationPath.toString() else "",
            storyboardPlanningBriefFile = if (Files.exists(planningBriefPath)) planningBriefPath.toString() else "",
            storyboardPacketDraftFile = if (Files.exists(storyboardDraftPath)) storyboardDraftPath.toString() else "",
            storyboardPacketDraftValidationFile = if (Files.exists(storyboardDraftValidationPath)) storyboardDraftValidationPath.toString() else "",
            storyboardPacketReviewFile = if (Files.exists(storyboardReviewPath)) storyboardReviewPath.toString() else "",
            storyboardHumanReviewPreflightFile = if (Files.exists(humanReviewPreflightPath)) humanReviewPreflightPath.toString() else "",
            storyboardStoryMovementAuditFile = if (Files.exists(storyMovementAuditPath)) storyMovementAuditPath.toString() else "",
            reviewableStoryboardPacketFile = if (Files.exists(reviewableStoryboardPath)) reviewableStoryboardPath.toString() else "",
            imageFacingDeliveryProofFile = if (Files.exists(proofPath)) proofPath.toString() else "",
            packId = pack.packId,
            projectId = pack.projectId,
            selectedSceneId = settingBuildReport?.selectedSceneId.orEmpty(),
            scriptPacketDraftId = settingBuildReport?.outputPacketId.orEmpty(),
            storyboardPlanningBriefId = planningBrief?.briefId.orEmpty(),
            storyboardPacketDraftId = storyboardDraft?.packetId.orEmpty(),
            storyboardPacketReviewId = storyboardReview?.reviewId.orEmpty(),
            storyboardHumanReviewPreflightId = humanReviewPreflight?.preflightId.orEmpty(),
            storyboardStoryMovementAuditId = storyMovementAudit?.auditId.orEmpty(),
            reviewableStoryboardPacketId = reviewableStoryboard?.packetId.orEmpty(),
            imageFacingDeliveryProofId = proof?.proofId.orEmpty(),
            nextAllowedDownstreamStep = if (verdict == "PASS") proof?.nextAllowedDownstreamStep.orEmpty() else "",
            blockedReason = if (verdict == "FAIL") findings.joinToString("; ") { it.ruleId } else "",
            findingCount = findings.size,
            findings = findings,
            evidence = SettingImageFacingProofPreviewBuildEvidence(
                settingScriptDraftBuildVerdict = settingBuildReport?.verdict.orEmpty(),
                scriptPacketValidationVerdict = scriptValidation?.verdict.orEmpty(),
                storyboardPlanningBriefVerdict = planningBrief?.verdict.orEmpty(),
                storyboardPacketDraftValidationVerdict = storyboardDraftValidation?.verdict.orEmpty(),
                storyboardPacketReviewVerdict = storyboardReview?.verdict.orEmpty(),
                storyboardHumanReviewPreflightVerdict = humanReviewPreflight?.verdict.orEmpty(),
                storyboardStoryMovementAuditVerdict = storyMovementAudit?.verdict.orEmpty(),
                reviewableStoryboardPacketVerdict = reviewableStoryboard?.verdict.orEmpty(),
                imageFacingDeliveryProofVerdict = proof?.verdict.orEmpty(),
                generatedScriptSegmentCount = scriptValidation?.segmentCount ?: 0,
                generatedStoryboardFrameCount = storyboardDraft?.frames?.size ?: 0,
                generatedImageFrameCardCount = proof?.frameDeliveryCards?.size ?: 0,
                excludedSecretCount = settingBuildReport?.evidence?.excludedSecretCount ?: 0,
                findingCountAcrossStages = listOf(
                    settingBuildReport?.findingCount ?: 0,
                    scriptValidation?.findingCount ?: 0,
                    planningBrief?.findingCount ?: 0,
                    storyboardDraftValidation?.findingCount ?: 0,
                    storyboardReview?.findingCount ?: 0,
                    humanReviewPreflight?.findingCount ?: 0,
                    storyMovementAudit?.findingCount ?: 0,
                    reviewableStoryboard?.findingCount ?: 0,
                    proof?.findingCount ?: 0,
                ).sum(),
            ),
        )
        writeCliJson(reportPath, report)
        printSettingImageFacingProofPreviewBuildReport(report)
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun addStageFailure(
        findings: MutableList<SettingImageFacingProofPreviewBuildFinding>,
        verdict: String,
        stageRuleId: String,
        category: String,
        path: Path,
        message: String,
        evidence: List<String>,
    ) {
        if (verdict.trim().uppercase() == "PASS") return
        findings += previewBuildFinding(
            ruleId = stageRuleId,
            category = category,
            severity = "CRITICAL",
            path = path.toString(),
            message = message,
            evidence = evidence,
        )
    }

    private fun previewBuildFinding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): SettingImageFacingProofPreviewBuildFinding =
        SettingImageFacingProofPreviewBuildFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private fun printSettingImageFacingProofPreviewBuildReport(report: SettingImageFacingProofPreviewBuildReport) {
        println("verdict=${report.verdict}")
        println("buildId=${report.buildId}")
        println("buildStatus=${report.buildStatus}")
        println("packId=${report.packId}")
        println("projectId=${report.projectId}")
        println("selectedSceneId=${report.selectedSceneId}")
        println("scriptPacketDraftId=${report.scriptPacketDraftId}")
        println("storyboardPacketDraftId=${report.storyboardPacketDraftId}")
        println("reviewableStoryboardPacketId=${report.reviewableStoryboardPacketId}")
        println("imageFacingDeliveryProofId=${report.imageFacingDeliveryProofId}")
        println("imageFacingDeliveryProofFile=${report.imageFacingDeliveryProofFile}")
        println("imageFacingDeliveryProofVerdict=${report.evidence.imageFacingDeliveryProofVerdict}")
        println("generatedStoryboardFrames=${report.evidence.generatedStoryboardFrameCount}")
        println("generatedImageFrameCards=${report.evidence.generatedImageFrameCardCount}")
        println("excludedSecrets=${report.evidence.excludedSecretCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
    }

    private fun validateScriptPacketDraft(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("packet"))
        val packet = readCliJson<ScriptPacketDraft>(packetPath)
        val report = ScriptPacketDraftValidationService().validate(
            packet = packet,
            packetFile = packetPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("packetId=${report.packetId}")
        println("segments=${report.segmentCount}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateScriptPacketSourceBinding(options: Map<String, String>): Int {
        val scriptPacketPath = Path.of(options.requiredCliOption("script-packet-draft"))
        val preflightPath = Path.of(options.requiredCliOption("script-packet-preflight"))
        val acceptedPacketPath = Path.of(options.requiredCliOption("accepted-draft-packet"))
        val report = ScriptPacketDraftSourceBindingValidationService().validate(
            scriptPacketDraft = readCliJson<ScriptPacketDraft>(scriptPacketPath),
            scriptPacketPreflight = readCliJson<NovelizationScriptPacketPreflightReport>(preflightPath),
            acceptedDraftPacket = readCliJson<NovelizationAcceptedDraftPacket>(acceptedPacketPath),
            scriptPacketDraftFile = scriptPacketPath.toString(),
            scriptPacketPreflightFile = preflightPath.toString(),
            acceptedDraftPacketFile = acceptedPacketPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("scriptPacketId=${report.scriptPacketId}")
        println("acceptedDraftPacketId=${report.acceptedDraftPacketId}")
        println("bindingMatchedBy=${report.evidence.bindingMatchedBy.joinToString(",")}")
        println("continuityAnchors=${report.evidence.referencedContinuityAnchorCount}/${report.evidence.protectedContinuityAnchorCount}")
        println("boundaryClaims=${report.evidence.boundaryClaimCount}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateScriptPacketStructureBridge(options: Map<String, String>): Int {
        val scriptPacketPath = Path.of(options.requiredCliOption("script-packet-draft"))
        val preflightPath = Path.of(options.requiredCliOption("script-packet-preflight"))
        val acceptedPacketPath = Path.of(options.requiredCliOption("accepted-draft-packet"))
        val report = ScriptPacketDraftStructureBridgeService().validate(
            scriptPacketDraft = readCliJson<ScriptPacketDraft>(scriptPacketPath),
            scriptPacketPreflight = readCliJson<NovelizationScriptPacketPreflightReport>(preflightPath),
            acceptedDraftPacket = readCliJson<NovelizationAcceptedDraftPacket>(acceptedPacketPath),
            scriptPacketDraftFile = scriptPacketPath.toString(),
            scriptPacketPreflightFile = preflightPath.toString(),
            acceptedDraftPacketFile = acceptedPacketPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("scriptPacketId=${report.scriptPacketId}")
        println("sourceBindingVerdict=${report.sourceBindingVerdict}")
        println("structureVerdict=${report.structureVerdict}")
        println("downstreamAllowed=${report.downstreamAllowed}")
        println("blockedReason=${report.blockedReason}")
        println("sourceBindingFindings=${report.sourceBindingFindingCount}")
        println("structureFindings=${report.structureFindingCount}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun createAcceptedScriptPacketHandoffPacket(options: Map<String, String>): Int {
        val scriptPacketPath = Path.of(options.requiredCliOption("script-packet-draft"))
        val bridgeReportPath = Path.of(options.requiredCliOption("structure-bridge-report"))
        val scriptPacketText = Files.readString(scriptPacketPath).removePrefix("\uFEFF")
        val report = AcceptedScriptPacketHandoffPacketService().build(
            scriptPacketDraftText = scriptPacketText,
            scriptPacketDraft = readCliJson<ScriptPacketDraft>(scriptPacketPath),
            structureBridgeReport = readCliJson<ScriptPacketDraftStructureBridgeReport>(bridgeReportPath),
            scriptPacketDraftFile = scriptPacketPath.toString(),
            structureBridgeReportFile = bridgeReportPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("packetId=${report.packetId}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("bridgeVerdict=${report.sourceReports.structureBridgeVerdict}")
        println("sourceBindingVerdict=${report.sourceReports.sourceBindingVerdict}")
        println("structureVerdict=${report.sourceReports.structureVerdict}")
        println("protectedContinuityAnchors=${report.protectedContinuityAnchorsSummary.protectedContinuityAnchorCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateStoryboardPlanningPreflight(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("accepted-script-packet"))
        val report = StoryboardPlanningPreflightService().validate(
            acceptedScriptPacket = readCliJson<AcceptedScriptPacketHandoffPacket>(packetPath),
            preflightId = options["preflight"] ?: "",
            acceptedScriptPacketFile = packetPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("packetVerdict=${report.evidence.packetVerdict}")
        println("sourceReportFiles=${report.evidence.sourceReportFileCount}")
        println("sourceReportVerdicts=${report.evidence.sourceReportVerdictCount}")
        println("protectedContinuityAnchors=${report.evidence.protectedContinuityAnchorCount}")
        println("residualRisks=${report.evidence.residualRiskCount}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun createStoryboardPlanningBrief(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("accepted-script-packet"))
        val preflightPath = Path.of(options.requiredCliOption("storyboard-planning-preflight"))
        val report = StoryboardPlanningBriefService().build(
            acceptedScriptPacket = readCliJson<AcceptedScriptPacketHandoffPacket>(packetPath),
            storyboardPlanningPreflight = readCliJson<StoryboardPlanningPreflightReport>(preflightPath),
            briefId = options["brief"] ?: "",
            acceptedScriptPacketFile = packetPath.toString(),
            storyboardPlanningPreflightFile = preflightPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("briefId=${report.briefId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("preflightVerdict=${report.sourceReports.storyboardPlanningPreflightVerdict}")
        println("protectedContinuityAnchors=${report.protectedContinuityAnchorsSummary.protectedContinuityAnchorCount}")
        println("residualRisks=${report.remainingResidualRisks.size}")
        println("reviewChecklist=${report.reviewChecklist.size}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateStoryboardPacketDraft(options: Map<String, String>): Int {
        val briefPath = Path.of(options.requiredCliOption("storyboard-planning-brief"))
        val draftPath = Path.of(options.requiredCliOption("storyboard-packet-draft"))
        val report = StoryboardPacketDraftValidationService().validate(
            storyboardPlanningBrief = readCliJson<StoryboardPlanningBrief>(briefPath),
            storyboardPacketDraft = readCliJson<StoryboardPacketDraft>(draftPath),
            storyboardPlanningBriefFile = briefPath.toString(),
            storyboardPacketDraftFile = draftPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("storyboardPlanningBriefId=${report.storyboardPlanningBriefId}")
        println("storyboardPacketDraftId=${report.storyboardPacketDraftId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("frames=${report.evidence.frameCount}")
        println("protectedContinuityAnchors=${report.evidence.protectedContinuityAnchorReferencedCount}/${report.evidence.protectedContinuityAnchorCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenOutputPaths=${report.evidence.forbiddenOutputPathCount}")
        println("externalGenerationApiHints=${report.evidence.externalGenerationApiHitCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun createStoryboardPacketReview(options: Map<String, String>): Int {
        val draftPath = Path.of(options.requiredCliOption("storyboard-packet-draft"))
        val reportPath = Path.of(options.requiredCliOption("storyboard-packet-draft-report"))
        val review = StoryboardPacketReviewService().create(
            storyboardPacketDraft = readCliJson<StoryboardPacketDraft>(draftPath),
            storyboardPacketDraftReport = readCliJson<StoryboardPacketDraftValidationReport>(reportPath),
            reviewId = options["review"] ?: "",
            storyboardPacketDraftFile = draftPath.toString(),
            storyboardPacketDraftReportFile = reportPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), review) }
        println("verdict=${review.verdict}")
        println("reviewId=${review.reviewId}")
        println("reviewStatus=${review.reviewStatus}")
        println("storyboardPacketDraftId=${review.storyboardPacketDraftId}")
        println("storyboardPlanningBriefId=${review.storyboardPlanningBriefId}")
        println("acceptedScriptPacketId=${review.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${review.scriptPacketDraftFile}")
        println("scriptPacketSha256=${review.scriptPacketDraftContentSha256}")
        println("frames=${review.frameCount}")
        println("assetCandidateRequirements=${review.assetCandidateRequirements.size}")
        println("protectedContinuityAnchors=${review.protectedContinuityAnchorsSummary.protectedContinuityAnchorCount}")
        println("residualRisks=${review.remainingResidualRisks.size}")
        println("reviewChecklist=${review.reviewChecklist.size}")
        println("nextAllowedDownstreamStep=${review.nextAllowedDownstreamStep}")
        println("blockedReason=${review.blockedReason}")
        println("findings=${review.findingCount}")
        review.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (review.verdict == "FAIL") 1 else 0
    }

    private fun validateStoryboardPacketHumanReviewPreflight(options: Map<String, String>): Int {
        val reviewPath = Path.of(options.requiredCliOption("storyboard-packet-review"))
        val report = StoryboardPacketHumanReviewPreflightService().validate(
            storyboardPacketReview = readCliJson<StoryboardPacketReviewPacket>(reviewPath),
            preflightId = options["preflight"] ?: "",
            storyboardPacketReviewFile = reviewPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("reviewStatus=${report.reviewStatus}")
        println("reviewVerdict=${report.evidence.reviewPacketVerdict}")
        println("frames=${report.evidence.frameIdCount}/${report.evidence.frameCount}")
        println("reviewChecklist=${report.evidence.reviewChecklistCount}")
        println("protectedContinuityAnchors=${report.evidence.protectedContinuityAnchorCount}")
        println("residualRisks=${report.evidence.residualRiskCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun auditStoryboardStoryMovement(options: Map<String, String>): Int {
        val draftPath = Path.of(options.requiredCliOption("storyboard-packet-draft"))
        val reviewPath = Path.of(options.requiredCliOption("storyboard-packet-review"))
        val preflightPath = Path.of(options.requiredCliOption("storyboard-human-review-preflight"))
        val report = StoryboardStoryMovementAuditService().audit(
            storyboardPacketDraft = readCliJson<StoryboardPacketDraft>(draftPath),
            storyboardPacketReview = readCliJson<StoryboardPacketReviewPacket>(reviewPath),
            storyboardHumanReviewPreflight = readCliJson<StoryboardPacketHumanReviewPreflightReport>(preflightPath),
            storyboardPacketDraftFile = draftPath.toString(),
            storyboardPacketReviewFile = reviewPath.toString(),
            storyboardHumanReviewPreflightFile = preflightPath.toString(),
            auditId = options["audit"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("auditId=${report.auditId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("storyboardPacketDraftId=${report.storyboardPacketDraftId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("frames=${report.evidence.uniqueFrameIdCount}/${report.evidence.frameCount}")
        println("movementEvidence=${report.evidence.movementEvidenceCount}")
        println("openingPressureEvidence=${report.evidence.openingPressureEvidenceCount}")
        println("actionEvidence=${report.evidence.actionEvidenceCount}")
        println("consequenceOrChoiceEvidence=${report.evidence.consequenceOrChoiceEvidenceCount}")
        println("protectedContinuityAnchors=${report.evidence.coveredContinuityAnchorCount}/${report.evidence.protectedContinuityAnchorCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("downstreamBoundary=${report.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun createReviewableStoryboardPacket(options: Map<String, String>): Int {
        val draftPath = Path.of(options.requiredCliOption("storyboard-packet-draft"))
        val reviewPath = Path.of(options.requiredCliOption("storyboard-packet-review"))
        val preflightPath = Path.of(options.requiredCliOption("storyboard-human-review-preflight"))
        val auditPath = Path.of(options.requiredCliOption("storyboard-story-movement-audit"))
        val packet = ReviewableStoryboardPacketService().create(
            storyboardPacketDraft = readCliJson<StoryboardPacketDraft>(draftPath),
            storyboardPacketReview = readCliJson<StoryboardPacketReviewPacket>(reviewPath),
            storyboardHumanReviewPreflight = readCliJson<StoryboardPacketHumanReviewPreflightReport>(preflightPath),
            storyboardStoryMovementAudit = readCliJson<StoryboardStoryMovementAuditReport>(auditPath),
            storyboardPacketDraftFile = draftPath.toString(),
            storyboardPacketReviewFile = reviewPath.toString(),
            storyboardHumanReviewPreflightFile = preflightPath.toString(),
            storyboardStoryMovementAuditFile = auditPath.toString(),
            packetId = options["packet"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), packet) }
        println("verdict=${packet.verdict}")
        println("packetId=${packet.packetId}")
        println("packetStatus=${packet.packetStatus}")
        println("storyboardPacketReviewId=${packet.storyboardPacketReviewId}")
        println("storyboardStoryMovementAuditId=${packet.storyboardStoryMovementAuditId}")
        println("storyboardPacketDraftId=${packet.storyboardPacketDraftId}")
        println("acceptedScriptPacketId=${packet.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${packet.scriptPacketDraftFile}")
        println("scriptPacketSha256=${packet.scriptPacketDraftContentSha256}")
        println("frames=${packet.evidence.uniqueFrameIdCount}/${packet.evidence.frameCount}")
        println("movementEvidence=${packet.evidence.movementEvidenceCount}")
        println("protectedContinuityAnchors=${packet.evidence.coveredContinuityAnchorCount}/${packet.evidence.protectedContinuityAnchorCount}")
        println("residualRisks=${packet.evidence.residualRiskCount}")
        println("reviewChecklist=${packet.evidence.reviewChecklistCount}")
        println("forbiddenClaims=${packet.evidence.forbiddenClaimCount}")
        println("downstreamBoundary=${packet.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${packet.nextAllowedDownstreamStep}")
        println("blockedReason=${packet.blockedReason}")
        println("findings=${packet.findingCount}")
        packet.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (packet.verdict == "FAIL") 1 else 0
    }

    private fun createImageFacingDeliveryProof(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("reviewable-storyboard-packet"))
        val proof = ImageFacingDeliveryProofService().create(
            reviewableStoryboardPacket = readCliJson<ReviewableStoryboardPacket>(packetPath),
            reviewableStoryboardPacketFile = packetPath.toString(),
            proofId = options["proof"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), proof) }
        println("verdict=${proof.verdict}")
        println("proofId=${proof.proofId}")
        println("proofStatus=${proof.proofStatus}")
        println("reviewableStoryboardPacketId=${proof.reviewableStoryboardPacketId}")
        println("acceptedScriptPacketId=${proof.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${proof.scriptPacketDraftFile}")
        println("scriptPacketSha256=${proof.scriptPacketDraftContentSha256}")
        println("frames=${proof.evidence.uniqueFrameIdCount}/${proof.evidence.frameCount}")
        println("visualIntentFrames=${proof.evidence.frameWithVisualIntentCount}")
        println("cameraIntentFrames=${proof.evidence.frameWithCameraIntentCount}")
        println("mustShowFrames=${proof.evidence.frameWithMustShowCount}")
        println("doNotShowFrames=${proof.evidence.frameWithDoNotShowCount}")
        println("continuityAnchorFrames=${proof.evidence.frameWithContinuityAnchorCount}")
        println("assetCandidateRefs=${proof.evidence.assetCandidateRefCount}")
        println("forbiddenClaims=${proof.evidence.forbiddenClaimCount}")
        println("remoteReferences=${proof.evidence.remoteReferenceCount}")
        println("downstreamBoundary=${proof.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${proof.nextAllowedDownstreamStep}")
        println("blockedReason=${proof.blockedReason}")
        println("findings=${proof.findingCount}")
        proof.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (proof.verdict == "FAIL") 1 else 0
    }

    private fun createComicVideoPacket(options: Map<String, String>): Int {
        val proofPath = Path.of(options.requiredCliOption("image-facing-delivery-proof"))
        val panelDurationMillis = options["panel-duration-ms"]?.toIntOrNull()
            ?: options["panel-duration-ms"]?.let { error("--panel-duration-ms must be an integer.") }
            ?: 3000
        val packet = ComicVideoPacketService().create(
            imageFacingDeliveryProof = readCliJson<ImageFacingDeliveryProof>(proofPath),
            imageFacingDeliveryProofFile = proofPath.toString(),
            packetId = options["packet"] ?: "",
            panelDurationMillis = panelDurationMillis,
        )
        options["out"]?.let { writeCliJson(Path.of(it), packet) }
        println("packetId=${packet.packetId}")
        println("packetStatus=${packet.packetStatus}")
        println("sourceImageFacingDeliveryProofId=${packet.sourceImageFacingDeliveryProofId}")
        println("acceptedScriptPacketId=${packet.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${packet.scriptPacketDraftFile}")
        println("scriptPacketSha256=${packet.scriptPacketDraftContentSha256}")
        println("panels=${packet.panelTracks.size}")
        println("voiceCast=${packet.voiceCast.size}")
        println("audioCues=${packet.audioCues.size}")
        println("subtitleCues=${packet.subtitleCues.size}")
        println("totalDurationMillis=${packet.timing.totalDurationMillis}")
        println("downstreamBoundary=${packet.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${packet.nextAllowedDownstreamStep}")
        println("validationNeeded=true")
        return 0
    }

    private fun createComicVideoPacketTemplate(options: Map<String, String>): Int {
        val packetId = options["packet"] ?: "comic-video-packet-template"
        val out = Path.of(options.requiredCliOption("out"))
        val template = ComicVideoPacket(
            packetId = packetId,
            reviewableStoryboardPacketFile = options["reviewable-storyboard-packet"] ?: "",
            imageFacingDeliveryProofFile = options["image-facing-delivery-proof"] ?: "",
            acceptedScriptPacketId = options["accepted-script-packet"] ?: "",
            scriptPacketDraftFile = options["script-packet-draft"] ?: "",
            scriptPacketDraftContentSha256 = options["script-packet-sha256"] ?: "",
            sourceVerdicts = ComicVideoPacketSourceVerdicts(),
            panelTracks = listOf(
                ComicVideoPanelTrack(
                    panelId = "panel-001",
                    sourceFrameId = "frame-001",
                    sceneReference = "replace-with-scene-reference",
                    beatReference = "replace-with-beat-reference",
                    visualIntent = "Replace with visible panel intent from the reviewable storyboard packet.",
                    cameraIntent = "Replace with camera intent from the reviewable storyboard packet.",
                    displayDurationMillis = 3000,
                    mustShow = listOf("Replace with must-show visual constraint."),
                    doNotShow = listOf("Replace with do-not-show visual constraint."),
                    continuityAnchors = listOf("Replace with protected continuity anchor."),
                    assetCandidateRefs = listOf("replace-with-asset-candidate-ref"),
                ),
            ),
            voiceCast = listOf(
                ComicVideoVoiceCast(
                    voiceId = "voice-protagonist",
                    characterId = "char-protagonist",
                    displayName = "Protagonist",
                    voiceLabel = "Replace with local/manual voice label.",
                    notes = "Plan-only voice casting. Keep media fields blank.",
                ),
            ),
            audioCues = listOf(
                ComicVideoAudioCue(
                    cueId = "audio-001",
                    panelId = "panel-001",
                    voiceId = "voice-protagonist",
                    characterId = "char-protagonist",
                    text = "Replace with spoken Traditional Chinese line.",
                    startMillis = 0,
                    durationMillis = 3000,
                ),
            ),
            subtitleCues = listOf(
                ComicVideoSubtitleCue(
                    cueId = "subtitle-001",
                    panelId = "panel-001",
                    audioCueId = "audio-001",
                    text = "Replace with subtitle text.",
                    startMillis = 0,
                    endMillis = 3000,
                ),
            ),
            timing = ComicVideoTiming(totalDurationMillis = 3000),
            boundaryChecklist = listOf(
                "metadata_only",
                "media_execution_absent",
                "remote_payload_absent",
                "candidate_only_no_lock",
            ),
            residualRisks = listOf(
                "Template requires source ids, PASS source verdicts, and author-filled timeline details before validation can pass.",
            ),
        )
        writeCliJson(out, template)
        println("wrote=$out")
        println("packetId=${template.packetId}")
        println("packetStatus=${template.packetStatus}")
        println("panels=${template.panelTracks.size}")
        println("voiceCast=${template.voiceCast.size}")
        println("audioCues=${template.audioCues.size}")
        println("subtitleCues=${template.subtitleCues.size}")
        println("downstreamBoundary=${template.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${template.nextAllowedDownstreamStep}")
        return 0
    }

    private fun fillComicVideoVoiceTimeline(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("comic-video-packet"))
        val fillPath = Path.of(options.requiredCliOption("voice-timeline-fill"))
        val packet = ComicVideoVoiceTimelineFillService().fill(
            packet = readCliJson<ComicVideoPacket>(packetPath),
            fill = readCliJson<ComicVideoVoiceTimelineFill>(fillPath),
        )
        options["out"]?.let { writeCliJson(Path.of(it), packet) }
        println("packetId=${packet.packetId}")
        println("packetStatus=${packet.packetStatus}")
        println("sourcePacketFile=$packetPath")
        println("voiceTimelineFillFile=$fillPath")
        println("panels=${packet.panelTracks.size}")
        println("voiceCast=${packet.voiceCast.size}")
        println("audioCues=${packet.audioCues.size}")
        println("subtitleCues=${packet.subtitleCues.size}")
        println("totalDurationMillis=${packet.timing.totalDurationMillis}")
        println("downstreamBoundary=${packet.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${packet.nextAllowedDownstreamStep}")
        println("validationNeeded=true")
        return 0
    }

    private fun createComicVideoVoiceTimelineFillTemplate(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("comic-video-packet"))
        val out = Path.of(options.requiredCliOption("out"))
        val fill = ComicVideoVoiceTimelineFillService().createTemplate(
            packet = readCliJson<ComicVideoPacket>(packetPath),
            fillId = options["fill"] ?: "",
            voiceId = options["voice-id"] ?: "voice-manual-001",
            characterId = options["character-id"] ?: "char-manual-001",
            displayName = options["display-name"] ?: "Manual Voice",
            voiceLabel = options["voice-label"] ?: "local-manual-voice",
        )
        writeCliJson(out, fill)
        println("wrote=$out")
        println("fillId=${fill.fillId}")
        println("sourceComicVideoPacketId=${fill.sourceComicVideoPacketId}")
        println("fillStatus=${fill.fillStatus}")
        println("voiceCast=${fill.voiceCast.size}")
        println("audioCues=${fill.audioCues.size}")
        println("subtitleCues=${fill.subtitleCues.size}")
        println("totalDurationMillis=${fill.timing.totalDurationMillis}")
        println("editRequired=true")
        println("mediaGenerated=false")
        return 0
    }

    private fun validateComicVideoPacket(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("comic-video-packet"))
        val report = ComicVideoPacketValidationService().validate(
            packet = readCliJson<ComicVideoPacket>(packetPath),
            packetFile = packetPath.toString(),
            validationId = options["validation"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("validationId=${report.validationId}")
        println("packetId=${report.packetId}")
        println("packetStatus=${report.packetStatus}")
        println("reviewableStoryboardPacketFile=${report.reviewableStoryboardPacketFile}")
        println("imageFacingDeliveryProofFile=${report.imageFacingDeliveryProofFile}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("panels=${report.evidence.uniquePanelIdCount}/${report.evidence.panelTrackCount}")
        println("voiceCast=${report.evidence.uniqueVoiceIdCount}/${report.evidence.voiceCastCount}")
        println("audioCues=${report.evidence.audioCueCount}")
        println("subtitleCues=${report.evidence.subtitleCueCount}")
        println("crossReferenceMismatches=${report.evidence.crossReferenceMismatchCount}")
        println("generatedMediaFields=${report.evidence.generatedMediaFieldCount}")
        println("remoteOrProviderFields=${report.evidence.remoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun createComicVideoRenderHandoffManifest(options: Map<String, String>): Int {
        val validationPath = Path.of(options.requiredCliOption("comic-video-packet-validation"))
        val manifest = ComicVideoRenderHandoffManifestService().create(
            validationReport = readCliJson<ComicVideoPacketValidationReport>(validationPath),
            validationReportFile = validationPath.toString(),
            manifestId = options["manifest"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), manifest) }
        println("verdict=${manifest.verdict}")
        println("manifestId=${manifest.manifestId}")
        println("manifestStatus=${manifest.manifestStatus}")
        println("validationId=${manifest.validationId}")
        println("packetId=${manifest.packetId}")
        println("acceptedScriptPacketId=${manifest.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${manifest.scriptPacketDraftFile}")
        println("scriptPacketSha256=${manifest.scriptPacketDraftContentSha256}")
        println("panelTimeline=${manifest.panelTimeline.size}")
        println("voiceTimeline=${manifest.voiceTimeline.size}")
        println("subtitleTimeline=${manifest.subtitleTimeline.size}")
        println("totalDurationMillis=${manifest.renderSettings.totalDurationMillis}")
        println("downstreamBoundary=${manifest.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${manifest.nextAllowedDownstreamStep}")
        println("blockedReason=${manifest.blockedReason}")
        println("findings=${manifest.findingCount}")
        manifest.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (manifest.verdict == "FAIL") 1 else 0
    }

    private fun createComicVideoProductionPackageDraft(options: Map<String, String>): Int {
        val proofPath = Path.of(options.requiredCliOption("image-facing-delivery-proof"))
        val fillPath = Path.of(options.requiredCliOption("voice-timeline-fill"))
        val panelDurationMillis = options["panel-duration-ms"]?.toIntOrNull()
            ?: options["panel-duration-ms"]?.let { error("--panel-duration-ms must be an integer.") }
            ?: 3000
        val draft = ComicVideoProductionPackageDraftService().create(
            imageFacingDeliveryProof = readCliJson<ImageFacingDeliveryProof>(proofPath),
            voiceTimelineFill = readCliJson<ComicVideoVoiceTimelineFill>(fillPath),
            imageFacingDeliveryProofFile = proofPath.toString(),
            voiceTimelineFillFile = fillPath.toString(),
            packageId = options["package"] ?: "",
            packetId = options["packet"] ?: "",
            panelDurationMillis = panelDurationMillis,
        )
        options["out"]?.let { writeCliJson(Path.of(it), draft) }
        println("verdict=${draft.verdict}")
        println("packageId=${draft.packageId}")
        println("packageStatus=${draft.packageStatus}")
        println("generatedComicVideoPacketId=${draft.generatedComicVideoPacketId}")
        println("generatedPacketValidationId=${draft.generatedPacketValidationId}")
        println("sourceImageFacingDeliveryProofId=${draft.sourceImageFacingDeliveryProofId}")
        println("acceptedScriptPacketId=${draft.acceptedScriptPacketId}")
        println("storyboardRows=${draft.storyboardProductionTable.size}")
        println("shotPrompts=${draft.shotPrompts.size}")
        println("assetPromptCards=${draft.assetPromptCards.size}")
        println("voiceStyleCards=${draft.voiceStyleCards.size}")
        println("totalDurationMillis=${draft.evidence.totalDurationMillis}")
        println("downstreamBoundary=${draft.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${draft.nextAllowedDownstreamStep}")
        println("blockedReason=${draft.blockedReason}")
        println("findings=${draft.findingCount}")
        draft.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (draft.verdict == "FAIL") 1 else 0
    }

    private fun validateComicVideoProductionPackage(options: Map<String, String>): Int {
        val draftPath = Path.of(options.requiredCliOption("production-package-draft"))
        val report = ComicVideoProductionPackageValidationService().validate(
            draft = readCliJson<ComicVideoProductionPackageDraft>(draftPath),
            draftFile = draftPath.toString(),
            validationId = options["validation"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("validationId=${report.validationId}")
        println("packageId=${report.packageId}")
        println("packageStatus=${report.packageStatus}")
        println("sourceImageFacingDeliveryProofId=${report.sourceImageFacingDeliveryProofId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("storyboardRows=${report.evidence.storyboardRowCount}")
        println("shotPrompts=${report.evidence.shotPromptCount}")
        println("assetPromptCards=${report.evidence.assetCardCount}")
        println("voiceStyleCards=${report.evidence.voiceStyleCardCount}")
        println("crossReferenceMismatches=${report.evidence.crossReferenceMismatchCount}")
        println("promptSafetyFindings=${report.evidence.promptSafetyFindingCount}")
        println("remoteOrProviderReferences=${report.evidence.remoteOrProviderReferenceCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("authorOnlyLeaks=${report.evidence.authorOnlyLeakCount}")
        println("totalDurationMillis=${report.evidence.totalDurationMillis}")
        println("downstreamBoundary=${report.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }


    private fun renderComicVideoLocal(options: Map<String, String>): Int {
        val manifestPath = Path.of(options.requiredCliOption("render-handoff-manifest"))
        val outputVideoPath = Path.of(options.requiredCliOption("out-video"))
        val defaultWorkDir = outputVideoPath
            .toAbsolutePath()
            .normalize()
            .parent
            ?.resolve(outputVideoPath.fileName.toString().removeSuffix(".mp4") + "_work")
            ?: Path.of("out", "comic_video_local_render_work")
        val report = ComicVideoLocalRendererService().render(
            manifest = readCliJson<ComicVideoRenderHandoffManifest>(manifestPath),
            manifestFile = manifestPath.toString(),
            outputVideoPath = outputVideoPath,
            workDir = Path.of(options["work-dir"] ?: defaultWorkDir.toString()),
            voiceName = options["voice-name"] ?: "Microsoft Hanhan Desktop",
            dryRun = options["dry-run"] == "true",
            renderId = options["render"] ?: "",
        )
        options["report-out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("renderId=${report.renderId}")
        println("renderStatus=${report.renderStatus}")
        println("manifestId=${report.manifestId}")
        println("packetId=${report.packetId}")
        println("outputVideoPath=${report.outputVideoPath}")
        println("workDir=${report.workDir}")
        println("dryRun=${report.dryRun}")
        println("mediaGenerated=${report.mediaGenerated}")
        println("localRendererBoundary=${report.localRendererBoundary}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun buildComicVideoLocalPreview(options: Map<String, String>): Int {
        val proofPath = Path.of(options.requiredCliOption("image-facing-delivery-proof"))
        val fillPath = Path.of(options.requiredCliOption("voice-timeline-fill"))
        val outputDir = Path.of(options.requiredCliOption("output-dir")).toAbsolutePath().normalize()
        val buildId = options["build"] ?: "comic-video-local-preview-build"
        val packetId = options["packet"] ?: "$buildId-packet"
        val fillTemplateId = options["fill-template"] ?: "$buildId-voice-timeline-fill-template"
        val validationId = options["validation"] ?: "$buildId-validation"
        val manifestId = options["manifest"] ?: "$buildId-render-handoff-manifest"
        val renderId = options["render"] ?: "$buildId-local-render"
        val panelDurationMillis = options["panel-duration-ms"]?.toIntOrNull()
            ?: options["panel-duration-ms"]?.let { error("--panel-duration-ms must be an integer.") }
            ?: 3000
        val packetPath = outputDir.resolve("comic_video_packet.json")
        val fillTemplatePath = outputDir.resolve("voice_timeline_fill_template.json")
        val filledPacketPath = outputDir.resolve("comic_video_packet_filled.json")
        val validationPath = outputDir.resolve("comic_video_packet_validation.json")
        val manifestPath = outputDir.resolve("comic_video_render_handoff_manifest.json")
        val renderReportPath = outputDir.resolve("comic_video_local_render_report.json")
        val buildReportPath = Path.of(
            options["report-out"] ?: outputDir.resolve("comic_video_local_preview_build_report.json").toString(),
        )
        val outputVideoPath = Path.of(
            options["out-video"] ?: outputDir.resolve("comic_video_local_preview.mp4").toString(),
        )
        val workDir = Path.of(options["work-dir"] ?: outputDir.resolve("local_render_work").toString())
        val dryRun = options["dry-run"] == "true"
        val findings = mutableListOf<ComicVideoLocalPreviewBuildFinding>()
        var proofVerdict = ""
        var packetPanelCount = 0
        var voiceCastCount = 0
        var audioCueCount = 0
        var subtitleCueCount = 0
        var packetValidationVerdict = ""
        var manifestVerdict = ""
        var localRenderVerdict = ""
        var totalDurationMillis = 0
        var renderReport: ComicVideoLocalRenderReport? = null

        val report = try {
            Files.createDirectories(outputDir)
            val proof = readCliJson<ImageFacingDeliveryProof>(proofPath)
            proofVerdict = proof.verdict
            val packet = ComicVideoPacketService().create(
                imageFacingDeliveryProof = proof,
                imageFacingDeliveryProofFile = proofPath.toString(),
                packetId = packetId,
                panelDurationMillis = panelDurationMillis,
            )
            packetPanelCount = packet.panelTracks.size
            writeCliJson(packetPath, packet)

            val fillTemplate = ComicVideoVoiceTimelineFillService().createTemplate(
                packet = packet,
                fillId = fillTemplateId,
                voiceId = options["voice-id"] ?: "voice-manual-001",
                characterId = options["character-id"] ?: "char-manual-001",
                displayName = options["display-name"] ?: "Manual Voice",
                voiceLabel = options["voice-label"] ?: "local-manual-voice",
            )
            writeCliJson(fillTemplatePath, fillTemplate)

            val filledPacket = ComicVideoVoiceTimelineFillService().fill(
                packet = packet,
                fill = readCliJson<ComicVideoVoiceTimelineFill>(fillPath),
            )
            voiceCastCount = filledPacket.voiceCast.size
            audioCueCount = filledPacket.audioCues.size
            subtitleCueCount = filledPacket.subtitleCues.size
            totalDurationMillis = filledPacket.timing.totalDurationMillis
            writeCliJson(filledPacketPath, filledPacket)

            val validation = ComicVideoPacketValidationService().validate(
                packet = filledPacket,
                packetFile = filledPacketPath.toString(),
                validationId = validationId,
            )
            packetValidationVerdict = validation.verdict
            writeCliJson(validationPath, validation)
            if (validation.verdict == "FAIL") {
                findings += buildFinding(
                    ruleId = "COMIC_VIDEO_PACKET_VALIDATION_FAILED",
                    category = "validation",
                    severity = "CRITICAL",
                    path = validationPath.toString(),
                    message = "End-to-end local preview build requires PASS comic video packet validation.",
                    evidence = validation.findings.map { it.ruleId },
                )
            }

            val manifest = ComicVideoRenderHandoffManifestService().create(
                validationReport = validation,
                validationReportFile = validationPath.toString(),
                manifestId = manifestId,
            )
            manifestVerdict = manifest.verdict
            writeCliJson(manifestPath, manifest)
            if (manifest.verdict == "FAIL") {
                findings += buildFinding(
                    ruleId = "COMIC_VIDEO_RENDER_HANDOFF_FAILED",
                    category = "render_handoff",
                    severity = "CRITICAL",
                    path = manifestPath.toString(),
                    message = "End-to-end local preview build requires PASS render handoff manifest.",
                    evidence = manifest.findings.map { it.ruleId },
                )
            }

            renderReport = ComicVideoLocalRendererService().render(
                manifest = manifest,
                manifestFile = manifestPath.toString(),
                outputVideoPath = outputVideoPath,
                workDir = workDir,
                voiceName = options["voice-name"] ?: "Microsoft Hanhan Desktop",
                dryRun = dryRun,
                renderId = renderId,
            )
            localRenderVerdict = renderReport?.verdict.orEmpty()
            writeCliJson(renderReportPath, renderReport)
            if (renderReport?.verdict == "FAIL") {
                findings += buildFinding(
                    ruleId = "COMIC_VIDEO_LOCAL_RENDER_FAILED",
                    category = "local_render",
                    severity = "CRITICAL",
                    path = renderReportPath.toString(),
                    message = "End-to-end local preview build requires PASS local render report.",
                    evidence = renderReport?.findings.orEmpty().map { it.ruleId },
                )
            }

            val verdict = if (validation.verdict == "PASS" && manifest.verdict == "PASS" && renderReport?.verdict == "PASS") {
                "PASS"
            } else {
                "FAIL"
            }
            comicVideoLocalPreviewBuildReport(
                verdict = verdict,
                buildStatus = when {
                    verdict == "FAIL" -> "FAIL"
                    dryRun -> "DRY_RUN"
                    else -> "local_preview_video_built"
                },
                buildId = buildId,
                proofPath = proofPath,
                fillPath = fillPath,
                outputDir = outputDir,
                packetPath = packetPath,
                fillTemplatePath = fillTemplatePath,
                filledPacketPath = filledPacketPath,
                validationPath = validationPath,
                manifestPath = manifestPath,
                renderReportPath = renderReportPath,
                outputVideoPath = outputVideoPath,
                packetId = packet.packetId,
                validationId = validation.validationId,
                manifestId = manifest.manifestId,
                renderId = renderReport?.renderId.orEmpty(),
                dryRun = dryRun,
                mediaGenerated = renderReport?.mediaGenerated == true,
                nextAllowedDownstreamStep = if (verdict == "PASS") renderReport?.nextAllowedDownstreamStep.orEmpty() else "",
                findings = findings,
                renderReport = renderReport,
                proofVerdict = proofVerdict,
                packetPanelCount = packetPanelCount,
                voiceCastCount = voiceCastCount,
                audioCueCount = audioCueCount,
                subtitleCueCount = subtitleCueCount,
                packetValidationVerdict = packetValidationVerdict,
                manifestVerdict = manifestVerdict,
                localRenderVerdict = localRenderVerdict,
                totalDurationMillis = totalDurationMillis,
            )
        } catch (exception: Exception) {
            findings += buildFinding(
                ruleId = "COMIC_VIDEO_LOCAL_PREVIEW_BUILD_EXCEPTION",
                category = "execution",
                severity = "CRITICAL",
                path = "$.execution",
                message = "End-to-end local preview build failed before completing the pipeline.",
                evidence = listOf(exception::class.java.simpleName, exception.message.orEmpty()),
            )
            comicVideoLocalPreviewBuildReport(
                verdict = "FAIL",
                buildStatus = "FAIL",
                buildId = buildId,
                proofPath = proofPath,
                fillPath = fillPath,
                outputDir = outputDir,
                packetPath = packetPath,
                fillTemplatePath = fillTemplatePath,
                filledPacketPath = filledPacketPath,
                validationPath = validationPath,
                manifestPath = manifestPath,
                renderReportPath = renderReportPath,
                outputVideoPath = outputVideoPath,
                packetId = packetId,
                validationId = validationId,
                manifestId = manifestId,
                renderId = renderId,
                dryRun = dryRun,
                mediaGenerated = false,
                nextAllowedDownstreamStep = "",
                findings = findings,
                renderReport = renderReport,
                proofVerdict = proofVerdict,
                packetPanelCount = packetPanelCount,
                voiceCastCount = voiceCastCount,
                audioCueCount = audioCueCount,
                subtitleCueCount = subtitleCueCount,
                packetValidationVerdict = packetValidationVerdict,
                manifestVerdict = manifestVerdict,
                localRenderVerdict = localRenderVerdict,
                totalDurationMillis = totalDurationMillis,
            )
        }

        writeCliJson(buildReportPath, report)
        printComicVideoLocalPreviewBuildReport(report)
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun comicVideoLocalPreviewBuildReport(
        verdict: String,
        buildStatus: String,
        buildId: String,
        proofPath: Path,
        fillPath: Path,
        outputDir: Path,
        packetPath: Path,
        fillTemplatePath: Path,
        filledPacketPath: Path,
        validationPath: Path,
        manifestPath: Path,
        renderReportPath: Path,
        outputVideoPath: Path,
        packetId: String,
        validationId: String,
        manifestId: String,
        renderId: String,
        dryRun: Boolean,
        mediaGenerated: Boolean,
        nextAllowedDownstreamStep: String,
        findings: List<ComicVideoLocalPreviewBuildFinding>,
        renderReport: ComicVideoLocalRenderReport?,
        proofVerdict: String,
        packetPanelCount: Int,
        voiceCastCount: Int,
        audioCueCount: Int,
        subtitleCueCount: Int,
        packetValidationVerdict: String,
        manifestVerdict: String,
        localRenderVerdict: String,
        totalDurationMillis: Int,
    ): ComicVideoLocalPreviewBuildReport =
        ComicVideoLocalPreviewBuildReport(
            verdict = verdict,
            buildId = buildId,
            buildStatus = buildStatus,
            imageFacingDeliveryProofFile = proofPath.toString(),
            voiceTimelineFillFile = fillPath.toString(),
            outputDir = outputDir.toString(),
            packetFile = packetPath.toString(),
            voiceTimelineFillTemplateFile = fillTemplatePath.toString(),
            filledPacketFile = filledPacketPath.toString(),
            packetValidationFile = validationPath.toString(),
            renderHandoffManifestFile = manifestPath.toString(),
            localRenderReportFile = renderReportPath.toString(),
            outputVideoPath = outputVideoPath.toString(),
            packetId = packetId,
            validationId = validationId,
            manifestId = manifestId,
            renderId = renderId,
            dryRun = dryRun,
            mediaGenerated = mediaGenerated,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
            blockedReason = if (verdict == "FAIL") {
                findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId }
            } else {
                ""
            },
            findingCount = findings.size,
            findings = findings,
            renderReport = renderReport,
            evidence = ComicVideoLocalPreviewBuildEvidence(
                proofVerdict = proofVerdict,
                packetPanelCount = packetPanelCount,
                voiceCastCount = voiceCastCount,
                audioCueCount = audioCueCount,
                subtitleCueCount = subtitleCueCount,
                packetValidationVerdict = packetValidationVerdict,
                manifestVerdict = manifestVerdict,
                localRenderVerdict = localRenderVerdict,
                totalDurationMillis = totalDurationMillis,
            ),
        )

    private fun buildFinding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): ComicVideoLocalPreviewBuildFinding =
        ComicVideoLocalPreviewBuildFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private fun printComicVideoLocalPreviewBuildReport(report: ComicVideoLocalPreviewBuildReport) {
        println("verdict=${report.verdict}")
        println("buildId=${report.buildId}")
        println("buildStatus=${report.buildStatus}")
        println("packetId=${report.packetId}")
        println("validationId=${report.validationId}")
        println("manifestId=${report.manifestId}")
        println("renderId=${report.renderId}")
        println("outputDir=${report.outputDir}")
        println("outputVideoPath=${report.outputVideoPath}")
        println("dryRun=${report.dryRun}")
        println("mediaGenerated=${report.mediaGenerated}")
        println("packetValidationVerdict=${report.evidence.packetValidationVerdict}")
        println("manifestVerdict=${report.evidence.manifestVerdict}")
        println("localRenderVerdict=${report.evidence.localRenderVerdict}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
    }

    private fun buildSettingComicVideoLocalPreview(options: Map<String, String>): Int {
        val settingPackPath = Path.of(options.requiredCliOption("setting-pack"))
        val fillPath = Path.of(options.requiredCliOption("voice-timeline-fill"))
        val outputDir = Path.of(options.requiredCliOption("output-dir")).toAbsolutePath().normalize()
        val buildId = options["build"] ?: "setting-comic-video-local-preview-build"
        val settingOutputDir = outputDir.resolve("setting_image_facing_preview")
        val videoOutputDir = outputDir.resolve("comic_video_local_preview")
        val proofPath = settingOutputDir.resolve("image_facing_delivery_proof.json")
        val fill = readCliJson<ComicVideoVoiceTimelineFill>(fillPath)
        val packetId = options["packet"]
            ?: fill.sourceComicVideoPacketId.ifBlank { "$buildId-packet" }

        val proofOptions = mutableMapOf(
            "setting-pack" to settingPackPath.toString(),
            "output-dir" to settingOutputDir.toString(),
            "build" to (options["setting-proof-build"] ?: "$buildId-setting-image-facing-proof"),
            "script-packet" to (options["script-packet"] ?: "$buildId-script-packet-draft"),
            "storyboard-brief" to (options["storyboard-brief"] ?: "$buildId-storyboard-brief"),
            "storyboard-draft" to (options["storyboard-draft"] ?: "$buildId-storyboard-draft"),
            "storyboard-review" to (options["storyboard-review"] ?: "$buildId-storyboard-review"),
            "human-review-preflight" to (options["human-review-preflight"] ?: "$buildId-human-review-preflight"),
            "story-movement-audit" to (options["story-movement-audit"] ?: "$buildId-story-movement-audit"),
            "reviewable-storyboard" to (options["reviewable-storyboard"] ?: "$buildId-reviewable-storyboard"),
            "proof" to (options["proof"] ?: "$buildId-image-facing-proof"),
        )
        options["scene"]?.let { proofOptions["scene"] = it }

        println("settingImageFacingPreviewDir=$settingOutputDir")
        val proofExit = buildSettingImageFacingProofPreview(proofOptions)
        if (proofExit != 0) {
            println("settingComicVideoPreviewStatus=FAILED_AT_SETTING_IMAGE_FACING_PROOF")
            return proofExit
        }

        val videoOptions = mutableMapOf(
            "image-facing-delivery-proof" to proofPath.toString(),
            "voice-timeline-fill" to fillPath.toString(),
            "output-dir" to videoOutputDir.toString(),
            "out-video" to (options["out-video"] ?: outputDir.resolve("setting_comic_video_local_preview.mp4").toString()),
            "build" to (options["comic-video-build"] ?: "$buildId-comic-video"),
            "packet" to packetId,
            "validation" to (options["validation"] ?: "$buildId-comic-video-validation"),
            "manifest" to (options["manifest"] ?: "$buildId-render-handoff-manifest"),
            "render" to (options["render"] ?: "$buildId-local-render"),
        )
        listOf(
            "panel-duration-ms",
            "voice-name",
            "work-dir",
            "report-out",
            "voice-id",
            "character-id",
            "display-name",
            "voice-label",
        ).forEach { key ->
            options[key]?.let { videoOptions[key] = it }
        }
        if (options["dry-run"] == "true") {
            videoOptions["dry-run"] = "true"
        }

        println("comicVideoLocalPreviewDir=$videoOutputDir")
        val videoExit = buildComicVideoLocalPreview(videoOptions)
        println("settingComicVideoPreviewStatus=${if (videoExit == 0) "PASS" else "FAILED_AT_COMIC_VIDEO_LOCAL_PREVIEW"}")
        return videoExit
    }

    private fun validateStoryboardPacketHumanReviewDecision(options: Map<String, String>): Int {
        val preflightPath = Path.of(options.requiredCliOption("human-review-preflight"))
        val decisionPath = Path.of(options.requiredCliOption("human-review-decision"))
        val report = StoryboardPacketHumanReviewDecisionIntakeService().validate(
            humanReviewPreflight = readCliJson<StoryboardPacketHumanReviewPreflightReport>(preflightPath),
            humanReviewDecision = readCliJson<StoryboardPacketHumanReviewDecision>(decisionPath),
            humanReviewPreflightFile = preflightPath.toString(),
            humanReviewDecisionFile = decisionPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("decisionId=${report.decisionId}")
        println("preflightId=${report.preflightId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("decisionStatus=${report.decisionStatus}")
        println("decisionAllowedDownstream=${report.decisionAllowedDownstream}")
        println("decisionSource=${report.evidence.decisionSource}")
        println("checklist=${report.evidence.checklistPassedCount}/${report.evidence.checklistItemCount}")
        println("unresolvedBlockingIssues=${report.evidence.unresolvedBlockingIssueCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateAssetCandidatePreflight(options: Map<String, String>): Int {
        val intakePath = Path.of(options.requiredCliOption("human-review-decision-intake"))
        val reviewPath = Path.of(options.requiredCliOption("storyboard-packet-review"))
        val report = AssetCandidatePreflightService().validate(
            humanReviewDecisionIntake = readCliJson<StoryboardPacketHumanReviewDecisionIntakeReport>(intakePath),
            storyboardPacketReview = readCliJson<StoryboardPacketReviewPacket>(reviewPath),
            preflightId = options["preflight"] ?: "",
            humanReviewDecisionIntakeFile = intakePath.toString(),
            storyboardPacketReviewFile = reviewPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("decisionId=${report.decisionId}")
        println("humanReviewPreflightId=${report.humanReviewPreflightId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("assetCandidateRequirements=${report.evidence.assetCandidateRequirementCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun createAssetCandidateBrief(options: Map<String, String>): Int {
        val preflightPath = Path.of(options.requiredCliOption("asset-candidate-preflight"))
        val brief = AssetCandidateBriefService().create(
            assetCandidatePreflight = readCliJson<AssetCandidatePreflightReport>(preflightPath),
            briefId = options["brief"] ?: "",
            assetCandidatePreflightFile = preflightPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), brief) }
        println("verdict=${brief.verdict}")
        println("briefId=${brief.briefId}")
        println("assetCandidatePreflightId=${brief.assetCandidatePreflightId}")
        println("decisionId=${brief.humanReviewDecisionId}")
        println("storyboardPacketReviewId=${brief.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${brief.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${brief.scriptPacketDraftFile}")
        println("scriptPacketSha256=${brief.scriptPacketDraftContentSha256}")
        println("assetCandidateRequirements=${brief.evidence.assetCandidateRequirementCount}")
        println("preparationChecklist=${brief.evidence.preparationChecklistCount}")
        println("forbiddenClaims=${brief.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${brief.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${brief.nextAllowedDownstreamStep}")
        println("blockedReason=${brief.blockedReason}")
        println("findings=${brief.findingCount}")
        brief.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (brief.verdict == "FAIL") 1 else 0
    }

    private fun createAssetCandidateDraft(options: Map<String, String>): Int {
        val briefPath = Path.of(options.requiredCliOption("asset-candidate-brief"))
        val draft = AssetCandidateDraftService().create(
            assetCandidateBrief = readCliJson<AssetCandidateBrief>(briefPath),
            draftId = options["draft"] ?: "",
            assetCandidateBriefFile = briefPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), draft) }
        println("verdict=${draft.verdict}")
        println("draftId=${draft.draftId}")
        println("assetCandidateBriefId=${draft.assetCandidateBriefId}")
        println("assetCandidatePreflightId=${draft.assetCandidatePreflightId}")
        println("decisionId=${draft.humanReviewDecisionId}")
        println("storyboardPacketReviewId=${draft.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${draft.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${draft.scriptPacketDraftFile}")
        println("scriptPacketSha256=${draft.scriptPacketDraftContentSha256}")
        println("candidateItems=${draft.evidence.candidateItemCount}")
        println("forbiddenClaims=${draft.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${draft.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${draft.nextAllowedDownstreamStep}")
        println("blockedReason=${draft.blockedReason}")
        println("findings=${draft.findingCount}")
        draft.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (draft.verdict == "FAIL") 1 else 0
    }

    private fun validateAssetCandidateDraft(options: Map<String, String>): Int {
        val draftPath = Path.of(options.requiredCliOption("asset-candidate-draft"))
        val report = AssetCandidateDraftValidationService().validate(
            assetCandidateDraft = readCliJson<AssetCandidateDraft>(draftPath),
            assetCandidateDraftFile = draftPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("assetCandidateDraftId=${report.assetCandidateDraftId}")
        println("assetCandidateBriefId=${report.assetCandidateBriefId}")
        println("assetCandidatePreflightId=${report.assetCandidatePreflightId}")
        println("decisionId=${report.humanReviewDecisionId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("candidateItems=${report.evidence.candidateItemCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateAssetCandidateReviewPreflight(options: Map<String, String>): Int {
        val validationPath = Path.of(options.requiredCliOption("asset-candidate-draft-validation"))
        val report = AssetCandidateReviewPreflightService().validate(
            assetCandidateDraftValidation = readCliJson<AssetCandidateDraftValidationReport>(validationPath),
            preflightId = options["preflight"] ?: "",
            assetCandidateDraftValidationReportFile = validationPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("assetCandidateDraftId=${report.assetCandidateDraftId}")
        println("assetCandidateBriefId=${report.assetCandidateBriefId}")
        println("assetCandidatePreflightId=${report.assetCandidatePreflightId}")
        println("decisionId=${report.humanReviewDecisionId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("candidateItems=${report.evidence.candidateItemCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateAssetCandidateHumanReviewDecision(options: Map<String, String>): Int {
        val preflightPath = Path.of(options.requiredCliOption("asset-candidate-review-preflight"))
        val decisionPath = Path.of(options.requiredCliOption("asset-candidate-human-review-decision"))
        val report = AssetCandidateHumanReviewDecisionIntakeService().validate(
            assetCandidateReviewPreflight = readCliJson<AssetCandidateReviewPreflightReport>(preflightPath),
            assetCandidateHumanReviewDecision = readCliJson<AssetCandidateHumanReviewDecision>(decisionPath),
            assetCandidateReviewPreflightFile = preflightPath.toString(),
            assetCandidateHumanReviewDecisionFile = decisionPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("decisionId=${report.decisionId}")
        println("preflightId=${report.preflightId}")
        println("decisionStatus=${report.decisionStatus}")
        println("decisionAllowedDownstream=${report.decisionAllowedDownstream}")
        println("assetCandidateDraftId=${report.assetCandidateDraftId}")
        println("assetCandidateBriefId=${report.assetCandidateBriefId}")
        println("assetCandidatePreflightId=${report.assetCandidatePreflightId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("imageCandidateRequirements=${report.evidence.imageCandidateRequirementCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("forbiddenOutputFields=${report.evidence.forbiddenOutputFieldCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateImageCandidatePreflight(options: Map<String, String>): Int {
        val intakePath = Path.of(options.requiredCliOption("asset-candidate-human-review-decision-intake"))
        val report = ImageCandidatePreflightService().validate(
            decisionIntake = readCliJson<AssetCandidateHumanReviewDecisionIntakeReport>(intakePath),
            preflightId = options["preflight"] ?: "",
            decisionIntakeFile = intakePath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("decisionId=${report.decisionId}")
        println("assetCandidateReviewPreflightId=${report.assetCandidateReviewPreflightId}")
        println("assetCandidateDraftId=${report.assetCandidateDraftId}")
        println("assetCandidateBriefId=${report.assetCandidateBriefId}")
        println("assetCandidatePreflightId=${report.assetCandidatePreflightId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("imageCandidateRequirements=${report.evidence.imageCandidateRequirementCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("forbiddenOutputFields=${report.evidence.forbiddenOutputFieldCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun createImageCandidateBrief(options: Map<String, String>): Int {
        val preflightPath = Path.of(options.requiredCliOption("image-candidate-preflight"))
        val brief = ImageCandidateBriefService().create(
            imageCandidatePreflight = readCliJson<ImageCandidatePreflightReport>(preflightPath),
            briefId = options["brief"] ?: "",
            imageCandidatePreflightFile = preflightPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), brief) }
        println("verdict=${brief.verdict}")
        println("briefId=${brief.briefId}")
        println("imageCandidatePreflightId=${brief.imageCandidatePreflightId}")
        println("decisionId=${brief.decisionId}")
        println("assetCandidateReviewPreflightId=${brief.assetCandidateReviewPreflightId}")
        println("assetCandidateDraftId=${brief.assetCandidateDraftId}")
        println("assetCandidateBriefId=${brief.assetCandidateBriefId}")
        println("assetCandidatePreflightId=${brief.assetCandidatePreflightId}")
        println("storyboardPacketReviewId=${brief.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${brief.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${brief.scriptPacketDraftFile}")
        println("scriptPacketSha256=${brief.scriptPacketDraftContentSha256}")
        println("imageCandidateRequirements=${brief.evidence.imageCandidateRequirementCount}")
        println("preparationChecklist=${brief.evidence.preparationChecklistCount}")
        println("forbiddenClaims=${brief.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${brief.evidence.forbiddenStatusCount}")
        println("forbiddenOutputFields=${brief.evidence.forbiddenOutputFieldCount}")
        println("nextAllowedDownstreamStep=${brief.nextAllowedDownstreamStep}")
        println("blockedReason=${brief.blockedReason}")
        println("findings=${brief.findingCount}")
        brief.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (brief.verdict == "FAIL") 1 else 0
    }

    private fun createImageCandidatePromptPreparation(options: Map<String, String>): Int {
        val briefPath = Path.of(options.requiredCliOption("image-candidate-brief"))
        val preparation = ImageCandidatePromptPreparationService().create(
            imageCandidateBrief = readCliJson<ImageCandidateBrief>(briefPath),
            preparationId = options["preparation"] ?: "",
            imageCandidateBriefFile = briefPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), preparation) }
        println("verdict=${preparation.verdict}")
        println("preparationId=${preparation.preparationId}")
        println("imageCandidateBriefId=${preparation.imageCandidateBriefId}")
        println("imageCandidatePreflightId=${preparation.imageCandidatePreflightId}")
        println("decisionId=${preparation.decisionId}")
        println("assetCandidateReviewPreflightId=${preparation.assetCandidateReviewPreflightId}")
        println("assetCandidateDraftId=${preparation.assetCandidateDraftId}")
        println("assetCandidateBriefId=${preparation.assetCandidateBriefId}")
        println("assetCandidatePreflightId=${preparation.assetCandidatePreflightId}")
        println("storyboardPacketReviewId=${preparation.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${preparation.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${preparation.scriptPacketDraftFile}")
        println("scriptPacketSha256=${preparation.scriptPacketDraftContentSha256}")
        println("promptItems=${preparation.evidence.promptItemCount}")
        println("forbiddenClaims=${preparation.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${preparation.evidence.forbiddenStatusCount}")
        println("forbiddenOutputFields=${preparation.evidence.forbiddenOutputFieldCount}")
        println("nextAllowedDownstreamStep=${preparation.nextAllowedDownstreamStep}")
        println("blockedReason=${preparation.blockedReason}")
        println("findings=${preparation.findingCount}")
        preparation.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (preparation.verdict == "FAIL") 1 else 0
    }

    private fun validateImageCandidatePromptPreparation(options: Map<String, String>): Int {
        val preparationPath = Path.of(options.requiredCliOption("image-candidate-prompt-preparation"))
        val report = ImageCandidatePromptValidationService().validate(
            promptPreparation = readCliJson<ImageCandidatePromptPreparation>(preparationPath),
            promptPreparationFile = preparationPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preparationId=${report.preparationId}")
        println("imageCandidateBriefId=${report.imageCandidateBriefId}")
        println("imageCandidatePreflightId=${report.imageCandidatePreflightId}")
        println("decisionId=${report.decisionId}")
        println("assetCandidateReviewPreflightId=${report.assetCandidateReviewPreflightId}")
        println("assetCandidateDraftId=${report.assetCandidateDraftId}")
        println("assetCandidateBriefId=${report.assetCandidateBriefId}")
        println("assetCandidatePreflightId=${report.assetCandidatePreflightId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("promptItems=${report.evidence.promptItemCount}")
        println("authorOnlyLeaks=${report.evidence.authorOnlyLeakCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("forbiddenOutputFields=${report.evidence.forbiddenOutputFieldCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateImageCandidateProviderHandoffPreflight(options: Map<String, String>): Int {
        val validationPath = Path.of(options.requiredCliOption("image-candidate-prompt-validation"))
        val report = ImageCandidateProviderHandoffPreflightService().validate(
            promptValidationReport = readCliJson<ImageCandidatePromptValidationReport>(validationPath),
            preflightId = options["preflight"] ?: "",
            promptValidationReportFile = validationPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("preparationId=${report.preparationId}")
        println("imageCandidateBriefId=${report.imageCandidateBriefId}")
        println("imageCandidatePreflightId=${report.imageCandidatePreflightId}")
        println("decisionId=${report.decisionId}")
        println("assetCandidateReviewPreflightId=${report.assetCandidateReviewPreflightId}")
        println("assetCandidateDraftId=${report.assetCandidateDraftId}")
        println("assetCandidateBriefId=${report.assetCandidateBriefId}")
        println("assetCandidatePreflightId=${report.assetCandidatePreflightId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("promptItems=${report.evidence.promptItemCount}")
        println("authorOnlyLeaks=${report.evidence.authorOnlyLeakCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("forbiddenOutputFields=${report.evidence.forbiddenOutputFieldCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateImageCandidateProviderHandoffDecision(options: Map<String, String>): Int {
        val preflightPath = Path.of(options.requiredCliOption("provider-handoff-preflight"))
        val decisionPath = Path.of(options.requiredCliOption("provider-handoff-decision"))
        val report = ImageCandidateProviderHandoffDecisionIntakeService().validate(
            providerHandoffPreflight = readCliJson<ImageCandidateProviderHandoffPreflightReport>(preflightPath),
            providerHandoffDecision = readCliJson<ImageCandidateProviderHandoffDecision>(decisionPath),
            providerHandoffPreflightFile = preflightPath.toString(),
            providerHandoffDecisionFile = decisionPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("decisionId=${report.decisionId}")
        println("preflightId=${report.preflightId}")
        println("decisionStatus=${report.decisionStatus}")
        println("decisionAllowedDownstream=${report.decisionAllowedDownstream}")
        println("preparationId=${report.preparationId}")
        println("imageCandidateBriefId=${report.imageCandidateBriefId}")
        println("imageCandidatePreflightId=${report.imageCandidatePreflightId}")
        println("promptItems=${report.evidence.promptItemCount}")
        println("checklist=${report.evidence.checklistPassedCount}/${report.evidence.checklistItemCount}")
        println("unresolvedBlockingIssues=${report.evidence.unresolvedBlockingIssueCount}")
        println("authorOnlyLeaks=${report.evidence.authorOnlyLeakCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("forbiddenOutputFields=${report.evidence.forbiddenOutputFieldCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun createImageCandidateProviderHandoffPacket(options: Map<String, String>): Int {
        val preflightPath = Path.of(options.requiredCliOption("provider-handoff-preflight"))
        val intakePath = Path.of(options.requiredCliOption("provider-handoff-decision-intake"))
        val packet = ImageCandidateProviderHandoffPacketService().create(
            providerHandoffPreflight = readCliJson<ImageCandidateProviderHandoffPreflightReport>(preflightPath),
            decisionIntake = readCliJson<ImageCandidateProviderHandoffDecisionIntakeReport>(intakePath),
            packetId = options["packet"] ?: "",
            providerHandoffPreflightFile = preflightPath.toString(),
            providerHandoffDecisionIntakeFile = intakePath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), packet) }
        println("verdict=${packet.verdict}")
        println("packetId=${packet.packetId}")
        println("decisionId=${packet.decisionId}")
        println("preflightId=${packet.preflightId}")
        println("preparationId=${packet.preparationId}")
        println("imageCandidateBriefId=${packet.imageCandidateBriefId}")
        println("imageCandidatePreflightId=${packet.imageCandidatePreflightId}")
        println("promptItems=${packet.evidence.promptItemCount}")
        println("manualInstructions=${packet.evidence.manualInstructionCount}")
        println("authorOnlyLeaks=${packet.evidence.authorOnlyLeakCount}")
        println("forbiddenClaims=${packet.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${packet.evidence.forbiddenStatusCount}")
        println("forbiddenOutputFields=${packet.evidence.forbiddenOutputFieldCount}")
        println("nextAllowedDownstreamStep=${packet.nextAllowedDownstreamStep}")
        println("blockedReason=${packet.blockedReason}")
        println("findings=${packet.findingCount}")
        packet.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (packet.verdict == "FAIL") 1 else 0
    }

    private fun validateImageCandidateProviderHandoffPacket(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("provider-handoff-packet"))
        val report = ImageCandidateProviderHandoffPacketValidationService().validate(
            providerHandoffPacket = readCliJson<ImageCandidateProviderHandoffPacket>(packetPath),
            providerHandoffPacketFile = packetPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("packetId=${report.packetId}")
        println("decisionId=${report.decisionId}")
        println("preflightId=${report.preflightId}")
        println("preparationId=${report.preparationId}")
        println("promptItems=${report.evidence.promptItemCount}")
        println("manualInstructions=${report.evidence.manualInstructionCount}")
        println("authorOnlyLeaks=${report.evidence.authorOnlyLeakCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("forbiddenOutputFields=${report.evidence.forbiddenOutputFieldCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateImageCandidateExternalResultIntake(options: Map<String, String>): Int {
        val validationPath = Path.of(options.requiredCliOption("provider-handoff-packet-validation"))
        val externalResultPath = Path.of(options.requiredCliOption("external-result"))
        val report = ImageCandidateExternalResultIntakeService().validate(
            providerHandoffPacketValidation = readCliJson<ImageCandidateProviderHandoffPacketValidationReport>(validationPath),
            externalResult = readCliJson<ImageCandidateExternalResult>(externalResultPath),
            providerHandoffPacketValidationReportFile = validationPath.toString(),
            externalResultFile = externalResultPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("resultId=${report.resultId}")
        println("packetId=${report.packetId}")
        println("decisionId=${report.decisionId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("resultStatus=${report.resultStatus}")
        println("imageResults=${report.evidence.imageResultCount}")
        println("localImageReferences=${report.evidence.localImageReferenceCount}")
        println("metadataOnlyReferences=${report.evidence.metadataOnlyReferenceCount}")
        println("forbiddenRemoteOrProviderFields=${report.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateImageCandidateResultReviewPreflight(options: Map<String, String>): Int {
        val intakePath = Path.of(options.requiredCliOption("external-result-intake"))
        val report = ImageCandidateResultReviewPreflightService().validate(
            externalResultIntake = readCliJson<ImageCandidateExternalResultIntakeReport>(intakePath),
            preflightId = options["preflight"] ?: "",
            externalResultIntakeFile = intakePath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("resultId=${report.resultId}")
        println("packetId=${report.packetId}")
        println("decisionId=${report.decisionId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("imageResults=${report.evidence.imageResultCount}")
        println("localImageReferences=${report.evidence.localImageReferenceCount}")
        println("metadataOnlyReferences=${report.evidence.metadataOnlyReferenceCount}")
        println("forbiddenRemoteOrProviderFields=${report.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateImageCandidateResultHumanReviewDecision(options: Map<String, String>): Int {
        val preflightPath = Path.of(options.requiredCliOption("result-review-preflight"))
        val decisionPath = Path.of(options.requiredCliOption("result-review-decision"))
        val report = ImageCandidateResultHumanReviewDecisionIntakeService().validate(
            resultReviewPreflight = readCliJson<ImageCandidateResultReviewPreflightReport>(preflightPath),
            resultReviewDecision = readCliJson<ImageCandidateResultReviewDecision>(decisionPath),
            resultReviewPreflightFile = preflightPath.toString(),
            resultReviewDecisionFile = decisionPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("decisionId=${report.decisionId}")
        println("resultReviewPreflightId=${report.resultReviewPreflightId}")
        println("resultId=${report.resultId}")
        println("packetId=${report.packetId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("decisionStatus=${report.decisionStatus}")
        println("decisionAllowedDownstream=${report.decisionAllowedDownstream}")
        println("imageResults=${report.evidence.imageResultCount}")
        println("checklistItems=${report.evidence.checklistItemCount}")
        println("checklistPassed=${report.evidence.checklistPassedCount}")
        println("unresolvedBlockingIssues=${report.evidence.unresolvedBlockingIssueCount}")
        println("forbiddenRemoteOrProviderFields=${report.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun createImageCandidateReferencePacket(options: Map<String, String>): Int {
        val preflightPath = Path.of(options.requiredCliOption("result-review-preflight"))
        val intakePath = Path.of(options.requiredCliOption("result-review-decision-intake"))
        val packet = ImageCandidateReferencePacketService().create(
            resultReviewPreflight = readCliJson<ImageCandidateResultReviewPreflightReport>(preflightPath),
            decisionIntake = readCliJson<ImageCandidateResultReviewDecisionIntakeReport>(intakePath),
            packetId = options["packet"] ?: "",
            resultReviewPreflightFile = preflightPath.toString(),
            resultReviewDecisionIntakeFile = intakePath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), packet) }
        println("verdict=${packet.verdict}")
        println("packetId=${packet.packetId}")
        println("packetStatus=${packet.packetStatus}")
        println("decisionId=${packet.decisionId}")
        println("resultReviewPreflightId=${packet.resultReviewPreflightId}")
        println("resultId=${packet.resultId}")
        println("providerHandoffPacketId=${packet.providerHandoffPacketId}")
        println("acceptedScriptPacketId=${packet.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${packet.scriptPacketDraftFile}")
        println("scriptPacketSha256=${packet.scriptPacketDraftContentSha256}")
        println("imageResults=${packet.evidence.imageResultCount}")
        println("forbiddenRemoteOrProviderFields=${packet.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${packet.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${packet.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${packet.nextAllowedDownstreamStep}")
        println("blockedReason=${packet.blockedReason}")
        println("findings=${packet.findingCount}")
        packet.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (packet.verdict == "FAIL") 1 else 0
    }

    private fun validateImageCandidateReferencePacket(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("candidate-reference-packet"))
        val report = ImageCandidateReferencePacketValidationService().validate(
            referencePacket = readCliJson<ImageCandidateReferencePacket>(packetPath),
            referencePacketFile = packetPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("packetId=${report.packetId}")
        println("packetStatus=${report.packetStatus}")
        println("decisionId=${report.decisionId}")
        println("resultReviewPreflightId=${report.resultReviewPreflightId}")
        println("resultId=${report.resultId}")
        println("providerHandoffPacketId=${report.providerHandoffPacketId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("imageResults=${report.evidence.imageResultCount}")
        println("forbiddenRemoteOrProviderFields=${report.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateCandidateReferenceHumanAcceptanceDecision(options: Map<String, String>): Int {
        val validationPath = Path.of(options.requiredCliOption("candidate-reference-packet-validation"))
        val decisionPath = Path.of(options.requiredCliOption("human-acceptance-decision"))
        val report = CandidateReferenceHumanAcceptanceDecisionIntakeService().validate(
            candidateReferencePacketValidation = readCliJson<ImageCandidateReferencePacketValidationReport>(validationPath),
            humanAcceptanceDecision = readCliJson<CandidateReferenceHumanAcceptanceDecision>(decisionPath),
            candidateReferencePacketValidationReportFile = validationPath.toString(),
            humanAcceptanceDecisionFile = decisionPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("decisionId=${report.decisionId}")
        println("packetId=${report.packetId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("decisionStatus=${report.decisionStatus}")
        println("decisionAllowedDownstream=${report.decisionAllowedDownstream}")
        println("imageResults=${report.evidence.imageResultCount}")
        println("checklistItems=${report.evidence.checklistItemCount}")
        println("checklistPassed=${report.evidence.checklistPassedCount}")
        println("forbiddenRemoteOrProviderFields=${report.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach { println("${it.ruleId}:${it.severity}:${it.path}:${it.message}") }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateCandidateReferenceRegistryPreflight(options: Map<String, String>): Int {
        val intakePath = Path.of(options.requiredCliOption("human-acceptance-decision-intake"))
        val validationPath = Path.of(options.requiredCliOption("candidate-reference-packet-validation"))
        val report = CandidateReferenceRegistryPreflightService().validate(
            humanAcceptanceDecisionIntake = readCliJson<CandidateReferenceHumanAcceptanceDecisionIntakeReport>(intakePath),
            candidateReferencePacketValidation = readCliJson<ImageCandidateReferencePacketValidationReport>(validationPath),
            preflightId = options["preflight"] ?: "",
            humanAcceptanceDecisionIntakeFile = intakePath.toString(),
            candidateReferencePacketValidationReportFile = validationPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("decisionId=${report.decisionId}")
        println("packetId=${report.packetId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("imageResults=${report.evidence.imageResultCount}")
        println("forbiddenRemoteOrProviderFields=${report.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach { println("${it.ruleId}:${it.severity}:${it.path}:${it.message}") }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun createCandidateReferenceRegistryPacket(options: Map<String, String>): Int {
        val preflightPath = Path.of(options.requiredCliOption("registry-preflight"))
        val packet = CandidateReferenceRegistryPacketService().create(
            registryPreflight = readCliJson<CandidateReferenceRegistryPreflightReport>(preflightPath),
            packetId = options["packet"] ?: "",
            registryPreflightFile = preflightPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), packet) }
        println("verdict=${packet.verdict}")
        println("packetId=${packet.packetId}")
        println("packetStatus=${packet.packetStatus}")
        println("preflightId=${packet.preflightId}")
        println("decisionId=${packet.decisionId}")
        println("referencePacketId=${packet.referencePacketId}")
        println("acceptedScriptPacketId=${packet.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${packet.scriptPacketDraftFile}")
        println("scriptPacketSha256=${packet.scriptPacketDraftContentSha256}")
        println("imageResults=${packet.evidence.imageResultCount}")
        println("forbiddenRemoteOrProviderFields=${packet.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${packet.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${packet.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${packet.nextAllowedDownstreamStep}")
        println("blockedReason=${packet.blockedReason}")
        println("findings=${packet.findingCount}")
        packet.findings.forEach { println("${it.ruleId}:${it.severity}:${it.path}:${it.message}") }
        return if (packet.verdict == "FAIL") 1 else 0
    }

    private fun validateCandidateReferenceRegistryPacket(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("candidate-reference-registry-packet"))
        val report = CandidateReferenceRegistryPacketValidationService().validate(
            registryPacket = readCliJson<CandidateReferenceRegistryPacket>(packetPath),
            registryPacketFile = packetPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("packetId=${report.packetId}")
        println("packetStatus=${report.packetStatus}")
        println("preflightId=${report.preflightId}")
        println("decisionId=${report.decisionId}")
        println("referencePacketId=${report.referencePacketId}")
        println("resultReviewPreflightId=${report.resultReviewPreflightId}")
        println("resultReviewDecisionId=${report.resultReviewDecisionId}")
        println("providerHandoffPacketId=${report.providerHandoffPacketId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("imageResults=${report.evidence.imageResultCount}")
        println("forbiddenRemoteOrProviderFields=${report.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach { println("${it.ruleId}:${it.severity}:${it.path}:${it.message}") }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateCandidateReferenceRegistryHumanAcceptanceDecision(options: Map<String, String>): Int {
        val validationPath = Path.of(options.requiredCliOption("registry-packet-validation"))
        val decisionPath = Path.of(options.requiredCliOption("human-acceptance-decision"))
        val report = CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService().validate(
            registryPacketValidation = readCliJson<CandidateReferenceRegistryPacketValidationReport>(validationPath),
            humanAcceptanceDecision = readCliJson<CandidateReferenceRegistryHumanAcceptanceDecision>(decisionPath),
            registryPacketValidationReportFile = validationPath.toString(),
            humanAcceptanceDecisionFile = decisionPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("decisionId=${report.decisionId}")
        println("registryPacketId=${report.registryPacketId}")
        println("registryPreflightId=${report.registryPreflightId}")
        println("priorHumanAcceptanceDecisionId=${report.priorHumanAcceptanceDecisionId}")
        println("referencePacketId=${report.referencePacketId}")
        println("resultReviewPreflightId=${report.resultReviewPreflightId}")
        println("resultReviewDecisionId=${report.resultReviewDecisionId}")
        println("providerHandoffPacketId=${report.providerHandoffPacketId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("decisionStatus=${report.decisionStatus}")
        println("decisionAllowedDownstream=${report.decisionAllowedDownstream}")
        println("imageResults=${report.evidence.imageResultCount}")
        println("checklistItems=${report.evidence.checklistItemCount}")
        println("checklistPassed=${report.evidence.checklistPassedCount}")
        println("forbiddenRemoteOrProviderFields=${report.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach { println("${it.ruleId}:${it.severity}:${it.path}:${it.message}") }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun createCandidateReferenceAcceptancePacket(options: Map<String, String>): Int {
        val validationPath = Path.of(options.requiredCliOption("registry-packet-validation"))
        val intakePath = Path.of(options.requiredCliOption("human-acceptance-decision-intake"))
        val packet = CandidateReferenceAcceptancePacketService().create(
            registryPacketValidation = readCliJson<CandidateReferenceRegistryPacketValidationReport>(validationPath),
            decisionIntake = readCliJson<CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport>(intakePath),
            packetId = options["packet"] ?: "",
            registryPacketValidationReportFile = validationPath.toString(),
            humanAcceptanceDecisionIntakeFile = intakePath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), packet) }
        println("verdict=${packet.verdict}")
        println("packetId=${packet.packetId}")
        println("packetStatus=${packet.packetStatus}")
        println("decisionId=${packet.decisionId}")
        println("registryPacketId=${packet.registryPacketId}")
        println("registryPreflightId=${packet.registryPreflightId}")
        println("referencePacketId=${packet.referencePacketId}")
        println("resultReviewPreflightId=${packet.resultReviewPreflightId}")
        println("resultReviewDecisionId=${packet.resultReviewDecisionId}")
        println("providerHandoffPacketId=${packet.providerHandoffPacketId}")
        println("acceptedScriptPacketId=${packet.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${packet.scriptPacketDraftFile}")
        println("scriptPacketSha256=${packet.scriptPacketDraftContentSha256}")
        println("imageResults=${packet.evidence.imageResultCount}")
        println("forbiddenRemoteOrProviderFields=${packet.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${packet.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${packet.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${packet.nextAllowedDownstreamStep}")
        println("blockedReason=${packet.blockedReason}")
        println("findings=${packet.findingCount}")
        packet.findings.forEach { println("${it.ruleId}:${it.severity}:${it.path}:${it.message}") }
        return if (packet.verdict == "FAIL") 1 else 0
    }

    private fun validateCandidateReferenceAcceptancePacket(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("candidate-reference-acceptance-packet"))
        val report = CandidateReferenceAcceptancePacketValidationService().validate(
            packet = readCliJson<CandidateReferenceAcceptancePacket>(packetPath),
            acceptancePacketFile = packetPath.toString(),
            validationId = options["validation"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("validationId=${report.validationId}")
        println("packetId=${report.packetId}")
        println("packetStatus=${report.packetStatus}")
        println("decisionId=${report.decisionId}")
        println("humanAcceptanceDecisionSource=${report.humanAcceptanceDecisionSource}")
        println("registryPacketId=${report.registryPacketId}")
        println("registryPreflightId=${report.registryPreflightId}")
        println("referencePacketId=${report.referencePacketId}")
        println("resultReviewPreflightId=${report.resultReviewPreflightId}")
        println("resultReviewDecisionId=${report.resultReviewDecisionId}")
        println("providerHandoffPacketId=${report.providerHandoffPacketId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("imageResults=${report.evidence.imageResultCount}")
        println("forbiddenRemoteOrProviderFields=${report.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach { println("${it.ruleId}:${it.severity}:${it.path}:${it.message}") }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun createCharacterNuwaCardTemplate(options: Map<String, String>): Int {
        val projectId = options["project"] ?: "project-id"
        val gateId = options["gate"] ?: "$projectId-character-nuwa-gate"
        val out = Path.of(options.requiredCliOption("out"))
        val template = CharacterNuwaGateFile(
            gateId = gateId,
            projectId = projectId,
            sourceChapter = options["chapter"] ?: "",
            requiredSpeakingCharacters = listOf(
                CharacterNuwaRequiredSpeaker(
                    characterId = "char-protagonist",
                    displayName = "Protagonist",
                    requiredDepth = "full_nuwa",
                    reason = "Point-of-view or emotionally important speaker.",
                ),
                CharacterNuwaRequiredSpeaker(
                    characterId = "char-background-speaker",
                    displayName = "Background speaker",
                    requiredDepth = "light_voice",
                    reason = "One-off speaker with a line in this scene.",
                ),
            ),
            characterCards = listOf(
                CharacterNuwaCard(
                    cardId = "card-char-protagonist",
                    characterId = "char-protagonist",
                    displayName = "Protagonist",
                    cardDepth = "full_nuwa",
                    gateStatus = "draft",
                    narrativeFunction = "pov",
                    socialPosition = "Replace with social position.",
                    currentPressure = "Replace with the pressure acting on this character now.",
                    relationshipToProtagonist = "self",
                    knowledgeBoundary = listOf("Replace with what this character can observe or infer."),
                    wantsFromScene = "Replace with what this character wants from this scene.",
                    fearsInScene = "Replace with what this character is avoiding.",
                    speechRegister = "Replace with speech register.",
                    sentenceLengthBias = "mixed",
                    directness = "restrained",
                    silencePattern = "Replace with when this character goes silent.",
                    pressureResponse = "Replace with how pressure changes this character's speech.",
                    allowedTells = listOf("Replace with allowed visible or verbal tell."),
                    forbiddenTells = listOf("Replace with forbidden tell."),
                    favoriteMoves = listOf("asks_back", "deflects"),
                    neverDo = listOf("recite_world_lore", "reveal_author_only_truth", "use_calibration_slogan"),
                    sampleLines = CharacterNuwaSampleLines(
                        accepted = listOf(
                            "Replace with accepted line 1.",
                            "Replace with accepted line 2.",
                            "Replace with accepted line 3.",
                        ),
                        rejected = listOf(
                            "Replace with rejected generic line 1.",
                            "Replace with rejected generic line 2.",
                            "Replace with rejected generic line 3.",
                        ),
                    ),
                    bannedStructures = listOf(
                        "???狷X嚗XXX",
                        "????X嚗 Y",
                        "銝XX嚗XXX",
                        "銝 X嚗 Y",
                        "not X, but Y",
                    ),
                ),
                CharacterNuwaCard(
                    cardId = "card-char-background-speaker",
                    characterId = "char-background-speaker",
                    displayName = "Background speaker",
                    cardDepth = "light_voice",
                    gateStatus = "draft",
                    narrativeFunction = "passerby",
                    currentPressure = "Replace with immediate pressure.",
                    relationshipToProtagonist = "stranger",
                    knowledgeBoundary = listOf("Replace with what this one-off speaker can know."),
                    speechRegister = "plain",
                    directness = "blunt",
                    pressureResponse = "Replace with how pressure shapes the line.",
                    neverDo = listOf("recite_world_lore", "use_calibration_slogan"),
                    sampleLines = CharacterNuwaSampleLines(
                        accepted = listOf("Replace with one acceptable line."),
                        rejected = listOf("Replace with one rejected line."),
                    ),
                ),
            ),
            bannedCalibrationStructures = listOf(
                "???狷X嚗XXX",
                "????X嚗 Y",
                "銝XX嚗XXX",
                "銝 X嚗 Y",
                "not X, but Y",
                "not X, only Y",
            ),
            reviewerNotes = "Fill this template before drafting accepted dialogue. No character card means no accepted dialogue.",
        )
        writeCliJson(out, template)
        println("wrote=$out")
        println("gateId=${template.gateId}")
        println("cards=${template.characterCards.size}")
        return 0
    }

    private fun validateCharacterNuwaGate(options: Map<String, String>): Int {
        val gatePath = options["file"]?.let { Path.of(it) }
            ?: options["character-dir"]?.let { Path.of(it).resolve("character_nuwa_gate.json") }
            ?: error("Missing required option --file or --character-dir")
        val gate = readCliJson<CharacterNuwaGateFile>(gatePath)
        val report = CharacterNuwaGateValidationService().validate(
            gate = gate,
            gateFile = gatePath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("gateId=${report.gateId}")
        println("requiredSpeakingCharacters=${report.requiredSpeakingCharacterCount}")
        println("cards=${report.cardCount}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateNovelizationPreflight(options: Map<String, String>): Int {
        val reportPath = options["character-nuwa-report"]?.let { Path.of(it) }
        val gatePath = options["character-nuwa-gate"]?.let { Path.of(it) }
        if ((reportPath == null) == (gatePath == null)) {
            error("Provide exactly one of --character-nuwa-report or --character-nuwa-gate")
        }
        val characterNuwaReport = reportPath?.let {
            readCliJson<CharacterNuwaGateValidationReport>(it)
        } ?: CharacterNuwaGateValidationService().validate(
            gate = readCliJson<CharacterNuwaGateFile>(gatePath!!),
            gateFile = gatePath.toString(),
        )
        val report = NovelizationPreflightValidationService().validate(
            characterNuwaReport = characterNuwaReport,
            characterNuwaReportFile = reportPath?.toString().orEmpty(),
            characterNuwaGateFile = gatePath?.toString().orEmpty(),
            preflightId = options["preflight"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("characterNuwaGateId=${report.characterNuwaGateId}")
        println("characterNuwaVerdict=${report.evidence.characterNuwaVerdict}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun createNovelizationWriterBriefTemplate(options: Map<String, String>): Int {
        val projectId = options["project"] ?: "project-id"
        val out = Path.of(options.requiredCliOption("out"))
        val template = NovelizationWriterBrief(
            briefId = options["brief"] ?: "$projectId-writer-brief",
            projectId = projectId,
            targetChapter = options["chapter"] ?: "chapter-id",
            sourcePreflightReport = options["preflight-report"] ?: "path/to/novelization_preflight_report.json",
            previousChapterRefs = listOf("path/to/accepted_previous_chapter.md"),
            acceptedChapterFacts = listOf("Replace with a fact that the next chapter must not contradict."),
            characterStates = listOf("Replace with a current character state that affects the next chapter."),
            openThreads = listOf("Replace with an unresolved thread the next chapter must respect."),
            mustAdvance = listOf("Replace with one concrete progression target."),
            mustNotRepeat = listOf("Replace with one completed beat that must not be replayed."),
            allowedScenes = listOf("Replace with allowed scene or location id."),
            knownUnknowns = listOf("Replace with a fact the draft must not invent."),
            tonePacingTarget = "Replace with tone and pacing target.",
            styleContract = "Replace with style contract or expression DNA reference.",
            characterNuwaReferences = listOf("Replace with Character Nuwa gate report or card id."),
            antiAiProseConstraints = listOf("No generic calibration slogans; use concrete pressure, action, cost, or refusal."),
            authorOnlyExcluded = true,
            reviewerNotes = "Draft planning only. This brief is not accepted canon and does not generate prose.",
        )
        writeCliJson(out, template)
        println("wrote=$out")
        println("briefId=${template.briefId}")
        println("targetChapter=${template.targetChapter}")
        return 0
    }

    private fun validateNovelizationWriterBrief(options: Map<String, String>): Int {
        val briefPath = Path.of(options.requiredCliOption("brief"))
        val brief = readCliJson<NovelizationWriterBrief>(briefPath)
        val preflightReportPath = options["preflight-report"]?.let { Path.of(it) }
        val report = NovelizationWriterBriefValidationService().validate(
            brief = brief,
            briefFile = briefPath.toString(),
            sourcePreflightReport = preflightReportPath?.let { readCliJson<NovelizationPreflightReport>(it) },
            sourcePreflightReportFile = preflightReportPath?.toString().orEmpty(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("briefId=${report.briefId}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateNovelizationDraftIntake(options: Map<String, String>): Int {
        val draftPath = Path.of(options.requiredCliOption("draft"))
        val writerBriefReportPath = Path.of(options.requiredCliOption("writer-brief-report"))
        val report = NovelizationDraftIntakeValidationService().validate(
            draftText = Files.readString(draftPath).removePrefix("\uFEFF"),
            writerBriefReport = readCliJson<NovelizationWriterBriefValidationReport>(writerBriefReportPath),
            draftFile = draftPath.toString(),
            writerBriefReportFile = writerBriefReportPath.toString(),
            sourceType = options.requiredCliOption("source-type"),
            sourceLabel = options["source-label"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("sourceType=${report.sourceType}")
        println("sourceLabel=${report.sourceLabel}")
        println("writerBriefVerdict=${report.evidence.writerBriefVerdict}")
        println("draftCharacters=${report.evidence.draftCharacters}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun auditNovelizationProseQuality(options: Map<String, String>): Int {
        val draftPath = Path.of(options.requiredCliOption("draft"))
        val draftIntakeReportPath = Path.of(options.requiredCliOption("draft-intake-report"))
        val report = NovelizationProseQualityAuditService().audit(
            draftText = Files.readString(draftPath).removePrefix("\uFEFF"),
            draftIntakeReport = readCliJson<NovelizationDraftIntakeReport>(draftIntakeReportPath),
            draftFile = draftPath.toString(),
            draftIntakeReportFile = draftIntakeReportPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("draftIntakeVerdict=${report.evidence.draftIntakeVerdict}")
        println("draftCharacters=${report.evidence.draftCharacters}")
        println("dialogueLines=${report.evidence.dialogueLineCount}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun createNovelizationRevisionBrief(options: Map<String, String>): Int {
        val draftPath = Path.of(options.requiredCliOption("draft"))
        val draftIntakeReportPath = Path.of(options.requiredCliOption("draft-intake-report"))
        val proseQualityReportPath = Path.of(options.requiredCliOption("prose-quality-report"))
        val continuityAnchorsPath = Path.of(options.requiredCliOption("continuity-anchors"))
        val report = NovelizationRevisionBriefService().build(
            draftText = Files.readString(draftPath).removePrefix("\uFEFF"),
            draftIntakeReport = readCliJson<NovelizationDraftIntakeReport>(draftIntakeReportPath),
            proseQualityReport = readCliJson<NovelizationProseQualityAuditReport>(proseQualityReportPath),
            continuityAnchors = readCliJson<NovelizationRevisionContinuityAnchors>(continuityAnchorsPath),
            briefId = options["brief"] ?: "",
            draftFile = draftPath.toString(),
            draftIntakeReportFile = draftIntakeReportPath.toString(),
            proseQualityAuditReportFile = proseQualityReportPath.toString(),
            continuityAnchorsFile = continuityAnchorsPath.toString(),
        )
        val out = Path.of(options.requiredCliOption("out"))
        writeCliJson(out, report)
        println("verdict=${report.verdict}")
        println("briefId=${report.briefId}")
        println("revisionRequired=${report.revisionRequired}")
        println("requiredFixes=${report.requiredFixes.size}")
        println("continuityAnchors=${report.continuityAnchors.size}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateNovelizationRevisedDraftIntake(options: Map<String, String>): Int {
        val originalDraftPath = Path.of(options.requiredCliOption("original-draft"))
        val revisedDraftPath = Path.of(options.requiredCliOption("revised-draft"))
        val revisionBriefReportPath = Path.of(options.requiredCliOption("revision-brief-report"))
        val declaredHandlingPath = options["declared-handling"]?.let { Path.of(it) }
        val report = NovelizationRevisedDraftIntakeValidationService().validate(
            originalDraftText = Files.readString(originalDraftPath).removePrefix("\uFEFF"),
            revisedDraftText = Files.readString(revisedDraftPath).removePrefix("\uFEFF"),
            revisionBriefReport = readCliJson<NovelizationRevisionBriefReport>(revisionBriefReportPath),
            declaredHandling = declaredHandlingPath?.let { readCliJson<NovelizationRevisionHandlingDeclaration>(it) }
                ?: NovelizationRevisionHandlingDeclaration(),
            originalDraftFile = originalDraftPath.toString(),
            revisedDraftFile = revisedDraftPath.toString(),
            revisionBriefReportFile = revisionBriefReportPath.toString(),
            declaredHandlingFile = declaredHandlingPath?.toString().orEmpty(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("revisionBriefId=${report.revisionBriefId}")
        println("revisionBriefVerdict=${report.evidence.revisionBriefVerdict}")
        println("originalCharacters=${report.evidence.originalCharacters}")
        println("revisedCharacters=${report.evidence.revisedCharacters}")
        println("declaredChecklist=${report.evidence.declaredChecklistCount}/${report.evidence.mandatoryChecklistCount}")
        println("declaredFixes=${report.evidence.declaredFixCount}/${report.evidence.requiredFixCount}")
        println("bannedResiduals=${report.evidence.bannedResidualCount}")
        println("continuityAnchors=${report.evidence.continuityAnchorPreservedCount}/${report.evidence.continuityAnchorCount}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun createNovelizationAcceptedDraftPacket(options: Map<String, String>): Int {
        val revisedDraftPath = Path.of(options.requiredCliOption("revised-draft"))
        val revisedDraftIntakeReportPath = Path.of(options.requiredCliOption("revised-draft-intake-report"))
        val revisionBriefReportPath = Path.of(options.requiredCliOption("revision-brief-report"))
        val continuityAnchorsPath = Path.of(options.requiredCliOption("continuity-anchors"))
        val report = NovelizationAcceptedDraftPacketService().build(
            revisedDraftText = Files.readString(revisedDraftPath).removePrefix("\uFEFF"),
            revisedDraftIntakeReport = readCliJson<NovelizationRevisedDraftIntakeReport>(revisedDraftIntakeReportPath),
            revisionBriefReport = readCliJson<NovelizationRevisionBriefReport>(revisionBriefReportPath),
            continuityAnchors = readCliJson<NovelizationRevisionContinuityAnchors>(continuityAnchorsPath),
            packetId = options["packet"] ?: "",
            revisedDraftFile = revisedDraftPath.toString(),
            revisedDraftIntakeReportFile = revisedDraftIntakeReportPath.toString(),
            revisionBriefReportFile = revisionBriefReportPath.toString(),
            continuityAnchorsFile = continuityAnchorsPath.toString(),
        )
        val out = Path.of(options.requiredCliOption("out"))
        writeCliJson(out, report)
        println("verdict=${report.verdict}")
        println("packetId=${report.packetId}")
        println("revisedDraftSha256=${report.revisedDraftContentSha256}")
        println("revisedDraftIntakeVerdict=${report.sourceReports.revisedDraftIntakeVerdict}")
        println("revisionBriefVerdict=${report.sourceReports.revisionBriefVerdict}")
        println("requiredFixesHandled=${report.requiredFixesHandledSummary.size}")
        println("protectedContinuityAnchors=${report.protectedContinuityAnchors.size}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateNovelizationScriptPacketPreflight(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("accepted-draft-packet"))
        val report = NovelizationScriptPacketPreflightService().validate(
            acceptedDraftPacket = readCliJson<NovelizationAcceptedDraftPacket>(packetPath),
            preflightId = options["preflight"] ?: "",
            acceptedDraftPacketFile = packetPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("acceptedDraftPacketId=${report.acceptedDraftPacketId}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("packetVerdict=${report.evidence.packetVerdict}")
        println("sourceReportFiles=${report.evidence.sourceReportFileCount}")
        println("sourceReportVerdicts=${report.evidence.sourceReportVerdictCount}")
        println("protectedContinuityAnchors=${report.evidence.protectedContinuityAnchorCount}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun auditChapterContinuity(options: Map<String, String>): Int {
        val previousPath = Path.of(options.requiredCliOption("previous"))
        val draftPath = Path.of(options.requiredCliOption("draft"))
        val anchorPath = Path.of(options.requiredCliOption("anchors"))
        val report = ChapterContinuityAuditService().audit(
            previousText = Files.readString(previousPath).removePrefix("\uFEFF"),
            draftText = Files.readString(draftPath).removePrefix("\uFEFF"),
            anchors = readCliJson<ChapterContinuityAnchorFile>(anchorPath),
            previousFile = previousPath.toString(),
            draftFile = draftPath.toString(),
            anchorFile = anchorPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("auditId=${report.auditId}")
        println("previous=${report.previousFile}")
        println("draft=${report.draftFile}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.checkId}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun explainContextBudget(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val input = options["input"]
            ?: options["input-file"]?.let { Files.readString(Path.of(it)).removePrefix("\uFEFF") }
            ?: error("Missing required option --input or --input-file")
        val report = MultiTurnRuntime(repository).explainContextBudget(
            sessionId = options.requiredCliOption("session"),
            playerInput = input,
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("canFit=${report.canFit}")
        println("candidateBytesBeforeTrimming=${report.candidateBytesBeforeTrimming}")
        println("candidateBytesAfterTrimming=${report.candidateBytesAfterTrimming}/${report.maxContextBytes}")
        println("bytesOverLimit=${report.bytesOverLimit}")
        println("mandatoryBytes=${report.mandatoryBytes}")
        println("optionalBytesRemaining=${report.optionalBytesRemaining}")
        println("duplicateGroups=${report.duplicateContentGroups.size}")
        if (report.unsatisfiedReason.isNotBlank()) println("unsatisfiedReason=${report.unsatisfiedReason}")
        return if (report.canFit) 0 else 1
    }

    private fun explainCapability(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val sessionId = options.requiredCliOption("session")
        val session = repository.getSession(sessionId) ?: error("Session not found: $sessionId")
        val report = CapabilityContractResolver(repository).explain(
            projectId = session.projectId,
            sessionId = sessionId,
            entityType = options.requiredCliOption("entity-type"),
            entityId = options.requiredCliOption("entity-id"),
            actorId = options["actor"],
            intendedUse = options["intended-use"].orEmpty(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("status=${report.status}")
        println("entity=${report.entityType}:${report.entityId}")
        println("canonicalEntitySource=${report.canonicalEntitySource}")
        println("entityRevisionId=${report.entityRevisionId}")
        println("sourcePack=${report.sourcePackId}@${report.sourcePackVersion ?: 0}")
        println("owner=${report.owner ?: ""}")
        println("authorizedActorIds=${report.authorizedActorIds.joinToString(",")}")
        println("effects=${report.effects.joinToString("; ")}")
        println("prerequisites=${report.prerequisites.joinToString("; ")}")
        println("requirements=${report.requirements.joinToString("; ")}")
        println("costs=${report.costs.joinToString("; ")}")
        println("limitations=${report.limitations.joinToString("; ")}")
        println("forbiddenUses=${report.forbiddenUses.joinToString("; ")}")
        println("currentEligibility=${report.currentEligibility}")
        println("eligibilityReasons=${report.eligibilityReasons.joinToString("; ")}")
        report.missingFields.forEach { println("missing=$it") }
        return if (report.status == "OK") 0 else 1
    }

    private fun createCheckpoint(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val checkpoint = CheckpointService(repository).createCheckpoint(
            sessionId = options.requiredCliOption("session"),
            label = options["label"].orEmpty(),
            kind = options["kind"] ?: "MANUAL",
            reason = options["reason"].orEmpty(),
        )
        println("checkpointId=${checkpoint.id}")
        println("branchId=${checkpoint.branchId ?: ""}")
        println("sessionId=${checkpoint.sessionId}")
        println("turnNumber=${checkpoint.turnNumber}")
        println("stateVersion=${checkpoint.stateVersion}")
        println("snapshotHash=${checkpoint.snapshotHash}")
        return 0
    }

    private fun listCheckpoints(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        CheckpointService(repository).listCheckpoints(options.requiredCliOption("session")).forEach {
            println("${it.id} branch=${it.branchId ?: ""} turn=${it.turnNumber} stateVersion=${it.stateVersion} kind=${it.kind} label=${it.label} createdAt=${it.createdAt}")
        }
        return 0
    }

    private fun showCheckpoint(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val checkpoint = CheckpointService(repository).showCheckpoint(options.requiredCliOption("checkpoint"))
        println("checkpointId=${checkpoint.id}")
        println("projectId=${checkpoint.projectId}")
        println("sessionId=${checkpoint.sessionId}")
        println("branchId=${checkpoint.branchId ?: ""}")
        println("turnNumber=${checkpoint.turnNumber}")
        println("stateVersion=${checkpoint.stateVersion}")
        println("kind=${checkpoint.kind}")
        println("label=${checkpoint.label}")
        println("snapshotHash=${checkpoint.snapshotHash}")
        println("createdAt=${checkpoint.createdAt}")
        return 0
    }

    private fun forkSession(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val result = TimelineBranchService(repository).forkSession(
            checkpointId = options.requiredCliOption("checkpoint"),
            label = options["label"] ?: "fork",
            reason = options["reason"].orEmpty(),
        )
        println("branchId=${result.branchId}")
        println("sessionId=${result.sessionId}")
        println("sourceCheckpointId=${result.sourceCheckpointId}")
        println("stateVersion=${result.stateVersion}")
        println("nextTurnNumber=${result.nextTurnNumber}")
        return 0
    }

    private fun checkoutBranch(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val branch = TimelineBranchService(repository).checkoutBranch(options.requiredCliOption("branch"))
        println("branchId=${branch.id}")
        println("status=${branch.status}")
        println("activeSessionId=${branch.activeSessionId ?: ""}")
        println("sourceCheckpointId=${branch.sourceCheckpointId ?: ""}")
        return 0
    }

    private fun showTimeline(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val export = TimelineBranchService(repository).showTimeline(options.requiredCliOption("project"))
        println("projectId=${export.projectId}")
        println("activeBranchId=${export.activeBranchId ?: ""}")
        println("branches:")
        export.branches.forEach {
            println("- ${it.id} status=${it.status} session=${it.activeSessionId ?: ""} parent=${it.parentBranchId ?: ""} label=${it.label}")
        }
        println("checkpoints:")
        export.checkpoints.forEach {
            println("- ${it.id} branch=${it.branchId ?: ""} turn=${it.turnNumber} stateVersion=${it.stateVersion} kind=${it.kind} label=${it.label}")
        }
        return 0
    }

    private fun verifyReplayChain(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val report = ReplayAuditService(repository).verify(
            sessionId = options.requiredCliOption("session"),
            record = options["record"] == "true",
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("status=${report.status}")
        println("checkedTurnCount=${report.checkedTurnCount}")
        report.issues.forEach { println("${it.code}: turn=${it.turnNumber ?: ""} ${it.message}") }
        return if (report.status == "PASS") 0 else 1
    }

    private fun exportTimeline(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val export = TimelineBranchService(repository).exportTimeline(options.requiredCliOption("project"))
        writeCliJson(Path.of(options.requiredCliOption("out")), export)
        println("wrote=${options.requiredCliOption("out")}")
        println("branches=${export.branches.size}")
        println("checkpoints=${export.checkpoints.size}")
        println("activeBranchId=${export.activeBranchId ?: ""}")
        return 0
    }

    private fun exportSessionEvidence(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val export = SessionEvidenceExporter(repository).export(options.requiredCliOption("session"))
        writeCliJson(Path.of(options.requiredCliOption("out")), export)
        println("wrote=${options.requiredCliOption("out")}")
        println("turnLogs=${export.turnLogs.size}")
        println("stateDeltas=${export.stateDeltas.size}")
        println("pendingContexts=${export.pendingContextCount}")
        return 0
    }

    private fun pilotPreflight(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val report = PilotPreflightService(repository).run(
            projectId = options.requiredCliOption("project"),
            sessionId = options.requiredCliOption("session"),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("valid=${report.valid}")
        println("stateVersion=${report.stateVersion}")
        println("nextTurnNumber=${report.nextTurnNumber}")
        println("pendingContextCount=${report.pendingContextCount}")
        report.checks.forEach { println("${it.name}=${it.passed} ${it.details}") }
        report.contextAudit?.let {
            println("contextBytes=${it.actualBytes}/${it.maxBytes}")
            println("contextHashValid=${it.hashValid}")
        }
        return if (report.valid) 0 else 1
    }


    private fun printHelp() {
        println(
            """
            Local Interactive Fiction Engine

            Commands:
              init-db --db <path>
              seed-demo --db <path> [--seed samples/demo_project_seed.json]
              compile-context --db <path> --project <id> --player-input <text> --out <path>
              validate-result --db <path> --context <path> --result <path>
              apply-result --db <path> --context <path> --result <path>
              run-manual-turn --db <path> --project <id> --player-input <text> --context-out <path> --result <path>
              show-state --db <path> --project <id>
              start-session [--db <path>] --project <id> --scene <id>
              prepare-turn [--db <path>] --session <id> (--input <text> | --input-file <path>) --out <path>
              resolve-turn [--db <path>] --session <id> --result <path>
              show-session [--db <path>] --session <id>
              abort-pending-turn [--db <path>] --session <id> [--context <id>]
              show-character-mind [--db <path>] --session <id> --character <id>
              preview-recall [--db <path>] --session <id> --character <id> --input <text>
              explain-context --file <path>
              show-scene [--db <path>] --session <id> [--input <text>]
              show-threads [--db <path>] --session <id>
              preview-transitions [--db <path>] --session <id> --input <text>
              show-world-events [--db <path>] --session <id>
              explain-scene-state [--db <path>] --session <id>
              create-setting-pack-template --project <id> --out <path> [--pack <id>] [--version <n>] [--mode ADD_ON]
              validate-setting-pack [--db <path>] --file <path> [--out <path>]
              preview-setting-import [--db <path>] --file <path> [--out <path>]
              apply-setting-pack [--db <path>] --file <path> [--out <path>]
              validate-world-source-archive --manifest <path> [--out <path>]
              apply-world-source-archive [--db <path>] --manifest <path> [--out <path>]
              query-world-source [--db <path>] --q <text> [--classification <id>] [--limit <n>] [--out <path>]
              upsert-ai-task [--db <path>] --task <id> --title <text> [--payload <json-file> | --payload-json <json>] [--type <text>] [--owner <role>] [--target <role>] [--priority <n>] [--status READY] [--source <text>] [--actor <text>] [--out <path>]
              claim-ai-task [--db <path>] --worker <id> [--target <role>] [--out <path>]
              complete-ai-task [--db <path>] --task <id> --worker <id> --result <PASS|FAIL|NEEDS_REVISION|BLOCKED> [--summary <text>] [--report <path>] [--out <path>]
              list-ai-tasks [--db <path>] [--status <READY|IN_PROGRESS|DONE|FAIL|NEEDS_REVISION|BLOCKED>] [--out <path>]
              export-setting-pack [--db <path>] --project <id> --pack <id> --version <n> --out <path>
              diff-setting-pack [--db <path>] --file <path> [--out <path>]
              impact-report [--db <path>] --file <path> [--out <path>]
              show-dependencies [--db <path>] --project <id> [--entity-type <type> --entity-id <id>] [--depth <n>]
              validate-project-canon [--db <path>] --project <id> [--out <path>]
              audit-context --file <path> [--out <path>]
              audit-narrative-quality --context <path> --result <path> [--evidence <path>] [--out <path>]
              audit-chapter-continuity --previous <path> --draft <path> --anchors <path> [--out <path>]
              create-setting-script-packet-draft --setting-pack <path> [--scene <id>] [--packet <id>] [--build <id>] --out <path> [--validation-out <path>] [--report-out <path>]
              build-setting-image-facing-proof-preview --setting-pack <path> --output-dir <path> [--scene <id>] [--build <id>] [--script-packet <id>] [--storyboard-brief <id>] [--storyboard-draft <id>] [--storyboard-review <id>] [--human-review-preflight <id>] [--story-movement-audit <id>] [--reviewable-storyboard <id>] [--proof <id>] [--report-out <path>]
              validate-script-packet-draft --packet <path> [--out <path>]
              validate-script-packet-source-binding --script-packet-draft <path> --script-packet-preflight <path> --accepted-draft-packet <path> [--out <path>]
              validate-script-packet-structure-bridge --script-packet-draft <path> --script-packet-preflight <path> --accepted-draft-packet <path> [--out <path>]
              create-accepted-script-packet-handoff-packet --script-packet-draft <path> --structure-bridge-report <path> [--out <path>]
              validate-storyboard-planning-preflight --accepted-script-packet <path> [--preflight <id>] [--out <path>]
              create-storyboard-planning-brief --accepted-script-packet <path> --storyboard-planning-preflight <path> [--brief <id>] [--out <path>]
              validate-storyboard-packet-draft --storyboard-planning-brief <path> --storyboard-packet-draft <path> [--out <path>]
              create-storyboard-packet-review --storyboard-packet-draft <path> --storyboard-packet-draft-report <path> [--review <id>] [--out <path>]
              validate-storyboard-packet-human-review-preflight --storyboard-packet-review <path> [--preflight <id>] [--out <path>]
              validate-storyboard-packet-human-review-decision --human-review-preflight <path> --human-review-decision <path> [--out <path>]
              audit-storyboard-story-movement --storyboard-packet-draft <path> --storyboard-packet-review <path> --storyboard-human-review-preflight <path> [--audit <id>] [--out <path>]
              create-reviewable-storyboard-packet --storyboard-packet-draft <path> --storyboard-packet-review <path> --storyboard-human-review-preflight <path> --storyboard-story-movement-audit <path> [--packet <id>] [--out <path>]
              create-image-facing-delivery-proof --reviewable-storyboard-packet <path> [--proof <id>] [--out <path>]
              create-comic-video-packet --image-facing-delivery-proof <path> [--packet <id>] [--panel-duration-ms <n>] [--out <path>]
              create-comic-video-packet-template [--packet <id>] [--reviewable-storyboard-packet <path>] [--image-facing-delivery-proof <path>] [--accepted-script-packet <id>] [--script-packet-draft <path>] [--script-packet-sha256 <sha256>] --out <path>
              create-comic-video-voice-timeline-fill-template --comic-video-packet <path> [--fill <id>] [--voice-id <id>] [--character-id <id>] [--display-name <name>] [--voice-label <label>] --out <path>
              fill-comic-video-voice-timeline --comic-video-packet <path> --voice-timeline-fill <path> [--out <path>]
              validate-comic-video-packet --comic-video-packet <path> [--validation <id>] [--out <path>]
              create-comic-video-render-handoff-manifest --comic-video-packet-validation <path> [--manifest <id>] [--out <path>]
              create-comic-video-production-package-draft --image-facing-delivery-proof <path> --voice-timeline-fill <path> [--package <id>] [--packet <id>] [--panel-duration-ms <n>] [--out <path>]
              validate-comic-video-production-package --production-package-draft <path> [--validation <id>] [--out <path>]
              validate-local-story-input-pack --input <path> [--validation <id>] --out <path>
              validate-09-foundation-card-pack --input <path> --out <path> [--validation <id>]
              review-09-foundation-card-pack --pack <path> --validation <path> --out <path> [--review <id>]
              draft-local-asset-prompt-cards --story-input <path> --story-validation <path> [--draft <id>] --out <path>
              draft-local-render-package --story-input <path> --asset-prompt-cards <path> [--package <id>] --out <path>
              verify-local-story-video-chain --story-input <path> --output-dir <dir> [--chain <id>] --out <path>
              draft-local-story-video-preview-handoff --chain-report <path> --render-package <path> --asset-prompt-cards <path> --story-input <path> [--handoff <id>] --out <path>
              review-local-story-video-preview-handoff --handoff <path> [--review <id>] --out <path>
              dry-run-local-renderer-adapter --handoff <path> --review <path> [--dry-run-id <id>] --out <path>
              plan-local-renderer-adapter-implementation --dry-run-report <path> [--plan <id>] --out <path>
              review-local-renderer-adapter-implementation-plan --plan <path> [--review <id>] --out <path>
              draft-local-renderer-adapter-implementation --handoff <path> --plan <path> --plan-review <path> [--draft <id>] --out <path>
              review-local-renderer-adapter-implementation-draft --draft <path> [--review <id>] --out <path>
              plan-local-renderer-manifest-draft --implementation-draft <path> --draft-review <path> [--plan <id>] --out <path>
              review-local-renderer-manifest-draft-plan --plan <path> [--review <id>] --out <path>
              draft-local-renderer-manifest-json --plan <path> --plan-review <path> [--draft <id>] --out <path>
              review-local-renderer-manifest-draft-json --draft <path> [--review <id>] --out <path>
              dry-run-local-renderer-manifest-draft-json-adapter --draft <path> --review <path> [--dry-run-id <id>] --out <path>
              plan-local-renderer-manifest-adapter-contract --dry-run-report <path> [--plan <id>] --out <path>
              review-local-renderer-manifest-adapter-contract-plan --plan <path> [--review <id>] --out <path>
              draft-local-renderer-manifest-adapter-contract --plan <path> --plan-review <path> [--draft <id>] --out <path>
              render-comic-video-local --render-handoff-manifest <path> --out-video <path> [--work-dir <path>] [--voice-name <name>] [--render <id>] [--report-out <path>] [--dry-run]
              build-comic-video-local-preview --image-facing-delivery-proof <path> --voice-timeline-fill <path> --output-dir <path> [--out-video <path>] [--work-dir <path>] [--build <id>] [--packet <id>] [--validation <id>] [--manifest <id>] [--render <id>] [--panel-duration-ms <n>] [--voice-name <name>] [--report-out <path>] [--dry-run]
              build-setting-comic-video-local-preview --setting-pack <path> --voice-timeline-fill <path> --output-dir <path> [--scene <id>] [--out-video <path>] [--build <id>] [--packet <id>] [--panel-duration-ms <n>] [--voice-name <name>] [--dry-run]
              validate-asset-candidate-preflight --human-review-decision-intake <path> --storyboard-packet-review <path> [--preflight <id>] [--out <path>]
              create-asset-candidate-brief --asset-candidate-preflight <path> [--brief <id>] [--out <path>]
              create-asset-candidate-draft --asset-candidate-brief <path> [--draft <id>] [--out <path>]
              validate-asset-candidate-draft --asset-candidate-draft <path> [--out <path>]
              validate-asset-candidate-review-preflight --asset-candidate-draft-validation <path> [--preflight <id>] [--out <path>]
              validate-asset-candidate-human-review-decision --asset-candidate-review-preflight <path> --asset-candidate-human-review-decision <path> [--out <path>]
              validate-image-candidate-preflight --asset-candidate-human-review-decision-intake <path> [--preflight <id>] [--out <path>]
              create-image-candidate-brief --image-candidate-preflight <path> [--brief <id>] [--out <path>]
              create-image-candidate-prompt-preparation --image-candidate-brief <path> [--preparation <id>] [--out <path>]
              validate-image-candidate-prompt-preparation --image-candidate-prompt-preparation <path> [--out <path>]
              validate-image-candidate-provider-handoff-preflight --image-candidate-prompt-validation <path> [--preflight <id>] [--out <path>]
              validate-image-candidate-provider-handoff-decision --provider-handoff-preflight <path> --provider-handoff-decision <path> [--out <path>]
              create-image-candidate-provider-handoff-packet --provider-handoff-preflight <path> --provider-handoff-decision-intake <path> [--packet <id>] [--out <path>]
              validate-image-candidate-provider-handoff-packet --provider-handoff-packet <path> [--out <path>]
              validate-image-candidate-external-result-intake --provider-handoff-packet-validation <path> --external-result <path> [--out <path>]
              validate-image-candidate-result-review-preflight --external-result-intake <path> [--preflight <id>] [--out <path>]
              validate-image-candidate-result-human-review-decision --result-review-preflight <path> --result-review-decision <path> [--out <path>]
              create-image-candidate-reference-packet --result-review-preflight <path> --result-review-decision-intake <path> [--packet <id>] [--out <path>]
              validate-image-candidate-reference-packet --candidate-reference-packet <path> [--out <path>]
              validate-candidate-reference-human-acceptance-decision --candidate-reference-packet-validation <path> --human-acceptance-decision <path> [--out <path>]
              validate-candidate-reference-registry-preflight --human-acceptance-decision-intake <path> --candidate-reference-packet-validation <path> [--preflight <id>] [--out <path>]
              create-candidate-reference-registry-packet --registry-preflight <path> [--packet <id>] [--out <path>]
              validate-candidate-reference-registry-packet --candidate-reference-registry-packet <path> [--out <path>]
              validate-candidate-reference-registry-human-acceptance-decision --registry-packet-validation <path> --human-acceptance-decision <path> [--out <path>]
              create-candidate-reference-acceptance-packet --registry-packet-validation <path> --human-acceptance-decision-intake <path> [--packet <id>] [--out <path>]
              validate-candidate-reference-acceptance-packet --candidate-reference-acceptance-packet <path> [--validation <id>] [--out <path>]
              create-character-nuwa-card-template [--project <id>] [--gate <id>] [--chapter <path>] --out <path>
              validate-character-nuwa-gate (--file <path> | --character-dir <dir>) [--out <path>]
              validate-novelization-preflight (--character-nuwa-report <path> | --character-nuwa-gate <path>) [--preflight <id>] [--out <path>]
              create-novelization-writer-brief-template [--project <id>] [--brief <id>] [--chapter <id>] [--preflight-report <path>] --out <path>
              validate-novelization-writer-brief --brief <path> [--preflight-report <path>] [--out <path>]
              validate-novelization-draft-intake --draft <path> --writer-brief-report <path> --source-type <codex|gpt|quarterfull|manual|mixed|other> [--source-label <text>] [--out <path>]
              audit-novelization-prose-quality --draft <path> --draft-intake-report <path> [--out <path>]
              create-novelization-revision-brief --draft <path> --draft-intake-report <path> --prose-quality-report <path> --continuity-anchors <path> [--brief <id>] --out <path>
              validate-novelization-revised-draft-intake --original-draft <path> --revised-draft <path> --revision-brief-report <path> [--declared-handling <path>] [--out <path>]
              create-novelization-accepted-draft-packet --revised-draft <path> --revised-draft-intake-report <path> --revision-brief-report <path> --continuity-anchors <path> [--packet <id>] --out <path>
              validate-novelization-script-packet-preflight --accepted-draft-packet <path> [--preflight <id>] [--out <path>]
              export-session-evidence [--db <path>] --session <id> --out <path>
              pilot-preflight [--db <path>] --project <id> --session <id> [--out <path>]
              explain-context-budget [--db <path>] --session <id> (--input <text> | --input-file <path>) [--out <path>]
              explain-capability [--db <path>] --session <id> --entity-type <technique|item|weapon> --entity-id <id> [--actor <character-id>] [--intended-use <text>] [--out <path>]
              create-checkpoint [--db <path>] --session <id> [--label <text>] [--kind MANUAL] [--reason <text>]
              list-checkpoints [--db <path>] --session <id>
              show-checkpoint [--db <path>] --checkpoint <id>
              fork-session [--db <path>] --checkpoint <id> [--label <text>] [--reason <text>]
              checkout-branch [--db <path>] --branch <id>
              show-timeline [--db <path>] --project <id>
              verify-replay-chain [--db <path>] --session <id> [--out <path>] [--record]
              export-timeline [--db <path>] --project <id> --out <path>
            """.trimIndent(),
        )
    }
}

private fun Path.budgetFailurePath(): Path {
    val fileName = fileName.toString()
    val dot = fileName.lastIndexOf('.')
    val base = if (dot > 0) fileName.substring(0, dot) else fileName
    val extension = if (dot > 0) fileName.substring(dot) else ".json"
    return (parent ?: Path.of(".")).resolve("${base}_budget_failure$extension")
}
