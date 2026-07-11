package com.localnovel.engine

import com.localnovel.engine.cli.AssetImageCandidateCli
import com.localnovel.engine.cli.CheckpointCli
import com.localnovel.engine.cli.ComicVideoCli
import com.localnovel.engine.cli.GrokCodexHandoffCli
import com.localnovel.engine.cli.LocalStoryVideoCli
import com.localnovel.engine.cli.NovelizationCli
import com.localnovel.engine.cli.SettingPackCli
import com.localnovel.engine.cli.StorySessionCli
import com.localnovel.engine.cli.StoryboardPipelineCli
import com.localnovel.engine.cli.ProvisionalLayerCli
import com.localnovel.engine.cli.WorldSourceAndTaskQueueCli
import com.localnovel.engine.cli.parseCliOptions
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
                "init-db" -> StorySessionCli.initDb(options)
                "seed-demo" -> StorySessionCli.seedDemo(options)
                "compile-context" -> StorySessionCli.compileContext(options)
                "validate-result" -> StorySessionCli.validateResult(options)
                "apply-result" -> StorySessionCli.applyResult(options)
                "run-manual-turn" -> StorySessionCli.runManualTurn(options)
                "show-state" -> StorySessionCli.showState(options)
                "start-session" -> StorySessionCli.startSession(options)
                "prepare-turn" -> StorySessionCli.prepareTurn(options)
                "resolve-turn" -> StorySessionCli.resolveTurn(options)
                "run-offscreen-npc-tick" -> StorySessionCli.runOffscreenNpcTick(options)
                "evaluate-reboot-drama" -> StorySessionCli.evaluateRebootDrama(options)
                "run-reboot01-vertical-slice" -> StorySessionCli.runReboot01VerticalSlice(options)
                "benchmark-reboot01-evolution" -> StorySessionCli.benchmarkReboot01Evolution(options)
                "serve-reboot01-web" -> StorySessionCli.serveReboot01Web(options)
                "show-session" -> StorySessionCli.showSession(options)
                "abort-pending-turn" -> StorySessionCli.abortPendingTurn(options)
                "show-character-mind" -> StorySessionCli.showCharacterMind(options)
                "preview-recall" -> StorySessionCli.previewRecall(options)
                "explain-context" -> StorySessionCli.explainContext(options)
                "show-scene" -> StorySessionCli.showScene(options)
                "show-threads" -> StorySessionCli.showThreads(options)
                "preview-transitions" -> StorySessionCli.previewTransitions(options)
                "show-world-events" -> StorySessionCli.showWorldEvents(options)
                "explain-scene-state" -> StorySessionCli.explainSceneState(options)
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
                "create-grok-story-handoff-template" -> GrokCodexHandoffCli.createGrokStoryHandoffTemplate(options)
                "validate-grok-story-handoff" -> GrokCodexHandoffCli.validateGrokStoryHandoff(options)
                "draft-codex-production-handoff" -> GrokCodexHandoffCli.draftCodexProductionHandoff(options)
                "export-setting-pack" -> SettingPackCli.exportSettingPack(options)
                "diff-setting-pack" -> SettingPackCli.diffSettingPack(options)
                "impact-report" -> SettingPackCli.impactReport(options)
                "show-dependencies" -> SettingPackCli.showDependencies(options)
                "validate-project-canon" -> SettingPackCli.validateProjectCanon(options)
                "audit-context" -> StorySessionCli.auditContext(options)
                "audit-narrative-quality" -> StorySessionCli.auditNarrativeQuality(options)
                "audit-chapter-continuity" -> NovelizationCli.auditChapterContinuity(options)
                "create-setting-script-packet-draft" -> StoryboardPipelineCli.createSettingScriptPacketDraft(options)
                "build-setting-image-facing-proof-preview" -> StoryboardPipelineCli.buildSettingImageFacingProofPreview(options)
                "validate-script-packet-draft" -> StoryboardPipelineCli.validateScriptPacketDraft(options)
                "validate-script-packet-source-binding" -> StoryboardPipelineCli.validateScriptPacketSourceBinding(options)
                "validate-script-packet-structure-bridge" -> StoryboardPipelineCli.validateScriptPacketStructureBridge(options)
                "create-accepted-script-packet-handoff-packet" -> StoryboardPipelineCli.createAcceptedScriptPacketHandoffPacket(options)
                "validate-storyboard-planning-preflight" -> StoryboardPipelineCli.validateStoryboardPlanningPreflight(options)
                "create-storyboard-planning-brief" -> StoryboardPipelineCli.createStoryboardPlanningBrief(options)
                "validate-storyboard-packet-draft" -> StoryboardPipelineCli.validateStoryboardPacketDraft(options)
                "create-storyboard-packet-review" -> StoryboardPipelineCli.createStoryboardPacketReview(options)
                "validate-storyboard-packet-human-review-preflight" -> StoryboardPipelineCli.validateStoryboardPacketHumanReviewPreflight(options)
                "validate-storyboard-packet-human-review-decision" -> StoryboardPipelineCli.validateStoryboardPacketHumanReviewDecision(options)
                "audit-storyboard-story-movement" -> StoryboardPipelineCli.auditStoryboardStoryMovement(options)
                "create-reviewable-storyboard-packet" -> StoryboardPipelineCli.createReviewableStoryboardPacket(options)
                "create-image-facing-delivery-proof" -> StoryboardPipelineCli.createImageFacingDeliveryProof(options)
                "create-comic-video-packet" -> ComicVideoCli.createComicVideoPacket(options)
                "create-comic-video-packet-template" -> ComicVideoCli.createComicVideoPacketTemplate(options)
                "create-comic-video-voice-timeline-fill-template" -> ComicVideoCli.createComicVideoVoiceTimelineFillTemplate(options)
                "fill-comic-video-voice-timeline" -> ComicVideoCli.fillComicVideoVoiceTimeline(options)
                "validate-comic-video-packet" -> ComicVideoCli.validateComicVideoPacket(options)
                "create-comic-video-render-handoff-manifest" -> ComicVideoCli.createComicVideoRenderHandoffManifest(options)
                "create-comic-video-production-package-draft" -> ComicVideoCli.createComicVideoProductionPackageDraft(options)
                "validate-comic-video-production-package" -> ComicVideoCli.validateComicVideoProductionPackage(options)
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
                "render-comic-video-local" -> ComicVideoCli.renderComicVideoLocal(options)
                "build-comic-video-local-preview" -> ComicVideoCli.buildComicVideoLocalPreview(options)
                "build-setting-comic-video-local-preview" -> ComicVideoCli.buildSettingComicVideoLocalPreview(options)
                "validate-asset-candidate-preflight" -> AssetImageCandidateCli.validateAssetCandidatePreflight(options)
                "create-asset-candidate-brief" -> AssetImageCandidateCli.createAssetCandidateBrief(options)
                "create-asset-candidate-draft" -> AssetImageCandidateCli.createAssetCandidateDraft(options)
                "validate-asset-candidate-draft" -> AssetImageCandidateCli.validateAssetCandidateDraft(options)
                "validate-asset-candidate-review-preflight" -> AssetImageCandidateCli.validateAssetCandidateReviewPreflight(options)
                "validate-asset-candidate-human-review-decision" -> AssetImageCandidateCli.validateAssetCandidateHumanReviewDecision(options)
                "validate-image-candidate-preflight" -> AssetImageCandidateCli.validateImageCandidatePreflight(options)
                "create-image-candidate-brief" -> AssetImageCandidateCli.createImageCandidateBrief(options)
                "create-image-candidate-prompt-preparation" -> AssetImageCandidateCli.createImageCandidatePromptPreparation(options)
                "validate-image-candidate-prompt-preparation" -> AssetImageCandidateCli.validateImageCandidatePromptPreparation(options)
                "validate-image-candidate-provider-handoff-preflight" -> AssetImageCandidateCli.validateImageCandidateProviderHandoffPreflight(options)
                "validate-image-candidate-provider-handoff-decision" -> AssetImageCandidateCli.validateImageCandidateProviderHandoffDecision(options)
                "create-image-candidate-provider-handoff-packet" -> AssetImageCandidateCli.createImageCandidateProviderHandoffPacket(options)
                "validate-image-candidate-provider-handoff-packet" -> AssetImageCandidateCli.validateImageCandidateProviderHandoffPacket(options)
                "validate-image-candidate-external-result-intake" -> AssetImageCandidateCli.validateImageCandidateExternalResultIntake(options)
                "validate-image-candidate-result-review-preflight" -> AssetImageCandidateCli.validateImageCandidateResultReviewPreflight(options)
                "validate-image-candidate-result-human-review-decision" -> AssetImageCandidateCli.validateImageCandidateResultHumanReviewDecision(options)
                "create-image-candidate-reference-packet" -> AssetImageCandidateCli.createImageCandidateReferencePacket(options)
                "validate-image-candidate-reference-packet" -> AssetImageCandidateCli.validateImageCandidateReferencePacket(options)
                "validate-candidate-reference-human-acceptance-decision" -> AssetImageCandidateCli.validateCandidateReferenceHumanAcceptanceDecision(options)
                "validate-candidate-reference-registry-preflight" -> AssetImageCandidateCli.validateCandidateReferenceRegistryPreflight(options)
                "create-candidate-reference-registry-packet" -> AssetImageCandidateCli.createCandidateReferenceRegistryPacket(options)
                "validate-candidate-reference-registry-packet" -> AssetImageCandidateCli.validateCandidateReferenceRegistryPacket(options)
                "validate-candidate-reference-registry-human-acceptance-decision" -> AssetImageCandidateCli.validateCandidateReferenceRegistryHumanAcceptanceDecision(options)
                "create-candidate-reference-acceptance-packet" -> AssetImageCandidateCli.createCandidateReferenceAcceptancePacket(options)
                "validate-candidate-reference-acceptance-packet" -> AssetImageCandidateCli.validateCandidateReferenceAcceptancePacket(options)
                "create-character-nuwa-card-template" -> AssetImageCandidateCli.createCharacterNuwaCardTemplate(options)
                "validate-character-nuwa-gate" -> AssetImageCandidateCli.validateCharacterNuwaGate(options)
                "validate-novelization-preflight" -> NovelizationCli.validateNovelizationPreflight(options)
                "create-novelization-writer-brief-template" -> NovelizationCli.createNovelizationWriterBriefTemplate(options)
                "validate-novelization-writer-brief" -> NovelizationCli.validateNovelizationWriterBrief(options)
                "validate-novelization-draft-intake" -> NovelizationCli.validateNovelizationDraftIntake(options)
                "audit-novelization-prose-quality" -> NovelizationCli.auditNovelizationProseQuality(options)
                "create-novelization-revision-brief" -> NovelizationCli.createNovelizationRevisionBrief(options)
                "validate-novelization-revised-draft-intake" -> NovelizationCli.validateNovelizationRevisedDraftIntake(options)
                "create-novelization-accepted-draft-packet" -> NovelizationCli.createNovelizationAcceptedDraftPacket(options)
                "validate-novelization-script-packet-preflight" -> NovelizationCli.validateNovelizationScriptPacketPreflight(options)
                "export-session-evidence" -> StorySessionCli.exportSessionEvidence(options)
                "export-audio-novel-episode" -> StorySessionCli.exportAudioNovelEpisode(options)
                "audit-audio-novel-publish" -> StorySessionCli.auditAudioNovelPublish(options)
                "pilot-preflight" -> StorySessionCli.pilotPreflight(options)
                "explain-context-budget" -> StorySessionCli.explainContextBudget(options)
                "explain-capability" -> StorySessionCli.explainCapability(options)
                "create-checkpoint" -> CheckpointCli.createCheckpoint(options)
                "list-checkpoints" -> CheckpointCli.listCheckpoints(options)
                "show-checkpoint" -> CheckpointCli.showCheckpoint(options)
                "fork-session" -> CheckpointCli.forkSession(options)
                "checkout-branch" -> CheckpointCli.checkoutBranch(options)
                "show-timeline" -> CheckpointCli.showTimeline(options)
                "verify-replay-chain" -> CheckpointCli.verifyReplayChain(options)
                "export-timeline" -> CheckpointCli.exportTimeline(options)
                "import-canon-thin-zones" -> ProvisionalLayerCli.importCanonThinZones(options)
                "list-canon-thin-zones" -> ProvisionalLayerCli.listCanonThinZones(options)
                "list-provisional-entities" -> ProvisionalLayerCli.listProvisionalEntities(options)
                "export-provisional-backlog" -> ProvisionalLayerCli.exportProvisionalBacklog(options)
                "discard-provisional-entity" -> ProvisionalLayerCli.discardProvisionalEntity(options)
                "promote-provisional-entity" -> ProvisionalLayerCli.promoteProvisionalEntity(options)
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
    private fun printHelp() {
        println(
            """
            Local Interactive Fiction Engine

            Commands:
              init-db --db <path>
              seed-demo --db <path> [--seed samples/demo_project_seed.json]
              compile-context --db <path> --project <id> --player-input <text> --out <path>
              validate-result --db <path> --context <path> --result <path> [--evidence <path>] [--logic-audit <path>] [--sample-label <text>] [--strict-narrative-quality] [--narrative-quality-out <path>] [--promotion-out <path>] [--no-auto-promote-learned-rules]
              apply-result --db <path> --context <path> --result <path>
              run-manual-turn --db <path> --project <id> --player-input <text> --context-out <path> --result <path>
              show-state --db <path> --project <id>
              start-session [--db <path>] --project <id> --scene <id> [--reader-mode]
              prepare-turn [--db <path>] --session <id> (--input <text> | --input-file <path>) --out <path>
              resolve-turn [--db <path>] --session <id> --result <path> [--evidence <path>] [--logic-audit <path>] [--sample-label <text>] [--scenario-seed <seed>] [--revision-suggestion <text>] [--revised-draft-file <path>] [--irreversible-contract <path> --irreversible-approval <path>] [--strict-narrative-quality] [--narrative-quality-out <path>] [--promotion-out <path>] [--no-auto-promote-learned-rules]
              run-offscreen-npc-tick [--db <path>] --session <id> --profile <path> --scenario-seed <seed> [--out <path>]
              evaluate-reboot-drama [--db <path>] --input <path> [--out <path>]
              run-reboot01-vertical-slice --db <path> --out-dir <path> [--base-pack <path>] [--addon-pack <path>] [--npc-profile <path>]
              benchmark-reboot01-evolution --out-dir <path> [--base-pack <path>] [--addon-pack <path>] [--npc-profile <path>]
              serve-reboot01-web [--evidence-dir <path>] [--port <loopback-port>]
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
              create-grok-story-handoff-template --out <path> [--handoff <id>] [--project <id>] [--title <text>] [--locale <text>]
              validate-grok-story-handoff --input <path> [--validation <id>] --out <path>
              draft-codex-production-handoff --grok-handoff <path> --validation <path> [--draft <id>] --out <path>
              export-setting-pack [--db <path>] --project <id> --pack <id> --version <n> --out <path>
              diff-setting-pack [--db <path>] --file <path> [--out <path>]
              impact-report [--db <path>] --file <path> [--out <path>]
              show-dependencies [--db <path>] --project <id> [--entity-type <type> --entity-id <id>] [--depth <n>]
              validate-project-canon [--db <path>] --project <id> [--out <path>]
              audit-context --file <path> [--out <path>]
              audit-narrative-quality [--db <path>] --context <path> --result <path> [--evidence <path>] [--logic-audit <path>] [--sample-label <text>] [--out <path>] [--promotion-out <path>] [--no-auto-promote-learned-rules]
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
              export-audio-novel-episode [--db <path>] --session <id> --out-dir <path> [--episode-id <id>] [--title <text>] [--from-turn <n>] [--to-turn <n>] [--cover <path>] [--publish]
              audit-audio-novel-publish --episode-dir <path> [--db <path>] --session <id> [--from-turn <n>] [--to-turn <n>] [--out <path>]
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
              import-canon-thin-zones [--db <path>] --project <id> --file <path> [--out <path>]
              list-canon-thin-zones [--db <path>] --project <id> [--out <path>]
              list-provisional-entities [--db <path>] --session <id> [--out <path>]
              export-provisional-backlog [--db <path>] --session <id> --out <path>
              discard-provisional-entity [--db <path>] --session <id> --entity <id> [--actor <text>] [--note <text>]
              promote-provisional-entity [--db <path>] --session <id> --entity <id> --out <path>
            """.trimIndent(),
        )
    }
}
