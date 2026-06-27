package com.localnovel.engine

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
import com.localnovel.engine.model.ScriptPacketDraftStructureBridgeReport
import com.localnovel.engine.model.StoryboardPacketDraft
import com.localnovel.engine.model.StoryboardPacketDraftValidationReport
import com.localnovel.engine.model.StoryboardPacketHumanReviewDecision
import com.localnovel.engine.model.StoryboardPacketHumanReviewPreflightReport
import com.localnovel.engine.model.StoryboardPacketReviewPacket
import com.localnovel.engine.model.StoryboardStoryMovementAuditReport
import com.localnovel.engine.model.StoryboardPlanningPreflightReport
import com.localnovel.engine.model.StoryboardPlanningBrief
import com.localnovel.engine.model.SettingPack
import com.localnovel.engine.model.SettingPackEntities
import com.localnovel.engine.model.SettingPackEntity
import com.localnovel.engine.model.SettingPackSource
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.AiTurnResultValidator
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
import com.localnovel.engine.service.CanonConsistencyValidator
import com.localnovel.engine.service.CapabilityContractResolver
import com.localnovel.engine.service.CharacterNuwaGateValidationService
import com.localnovel.engine.service.ChapterContinuityAuditService
import com.localnovel.engine.service.CheckpointService
import com.localnovel.engine.service.ContextCompiler
import com.localnovel.engine.service.ContextAuditService
import com.localnovel.engine.service.ContextBudgetUnsatisfiableException
import com.localnovel.engine.service.DependencyGraphService
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
            val options = parseOptions(args.drop(1))
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
                "create-setting-pack-template" -> createSettingPackTemplate(options)
                "validate-setting-pack" -> validateSettingPack(options)
                "preview-setting-import" -> previewSettingImport(options)
                "apply-setting-pack" -> applySettingPack(options)
                "export-setting-pack" -> exportSettingPack(options)
                "diff-setting-pack" -> diffSettingPack(options)
                "impact-report" -> impactReport(options)
                "show-dependencies" -> showDependencies(options)
                "validate-project-canon" -> validateProjectCanon(options)
                "audit-context" -> auditContext(options)
                "audit-narrative-quality" -> auditNarrativeQuality(options)
                "audit-chapter-continuity" -> auditChapterContinuity(options)
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
        repository(options).initialize()
        println("Initialized SQLite database: ${options.required("db")}")
        return 0
    }

    private fun seedDemo(options: Map<String, String>): Int {
        val repository = repository(options)
        repository.initialize()
        val seedPath = Path.of(options["seed"] ?: "samples/demo_project_seed.json")
        val seed = JsonSupport.json.decodeFromString<DemoProjectSeed>(Files.readString(seedPath))
        repository.seedDemo(seed)
        println("Seeded demo project '${seed.project.id}' into ${options.required("db")}")
        return 0
    }

    private fun compileContext(options: Map<String, String>): Int {
        val repository = repository(options)
        val context = ContextCompiler(repository).compile(
            projectId = options.required("project"),
            playerInput = options.required("player-input"),
        )
        val out = Path.of(options.required("out"))
        out.parent?.let { Files.createDirectories(it) }
        Files.writeString(out, JsonSupport.json.encodeToString(context))
        println("Wrote context packet: $out")
        return 0
    }

    private fun validateResult(options: Map<String, String>): Int {
        val repository = repository(options)
        val context = readJson<ContextPacket>(Path.of(options.required("context")))
        val result = readJson<AiTurnResult>(Path.of(options.required("result")))
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
        val repository = repository(options)
        val runner = runner(repository)
        val context = readJson<ContextPacket>(Path.of(options.required("context")))
        val result = readJson<AiTurnResult>(Path.of(options.required("result")))
        val applyResult = runner.apply(context, result)
        println(applyResult.narrative)
        if (StallDetector(repository).hasStalled(context.project.id)) {
            println()
            println("[stall] Consecutive turns without state change detected.")
        }
        return 0
    }

    private fun runManualTurn(options: Map<String, String>): Int {
        val repository = repository(options)
        val runner = runner(repository)
        val context = runner.compile(
            projectId = options.required("project"),
            playerInput = options.required("player-input"),
        )
        val contextOut = Path.of(options.required("context-out"))
        contextOut.parent?.let { Files.createDirectories(it) }
        Files.writeString(contextOut, JsonSupport.json.encodeToString(context))

        val resultPath = Path.of(options.required("result"))
        if (!Files.exists(resultPath)) {
            println("Wrote context packet: $contextOut")
            println("Create ai_turn_result JSON at: $resultPath")
            return 0
        }

        val result = readJson<AiTurnResult>(resultPath)
        val applyResult = runner.apply(context, result)
        println(applyResult.narrative)
        return 0
    }

    private fun showState(options: Map<String, String>): Int {
        val repository = repository(options)
        val projectId = options.required("project")
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
        val repository = repository(options)
        repository.initialize()
        val session = MultiTurnRuntime(repository).startSession(
            projectId = options.required("project"),
            sceneId = options.required("scene"),
        )
        println("sessionId=${session.id}")
        println("branchId=${session.branchId ?: ""}")
        println("currentSceneId=${session.currentSceneId}")
        println("nextTurnNumber=${session.nextTurnNumber}")
        println("stateVersion=${session.stateVersion}")
        return 0
    }

    private fun prepareTurn(options: Map<String, String>): Int {
        val repository = repository(options)
        val sessionId = options.required("session")
        val playerInput = options["input"]
            ?: options["input-file"]?.let { Files.readString(Path.of(it)).removePrefix("\uFEFF") }
            ?: error("Missing required option --input or --input-file")
        val out = Path.of(options.required("out"))
        val context = try {
            MultiTurnRuntime(repository).prepareTurn(
                sessionId = sessionId,
                playerInput = playerInput,
            )
        } catch (error: ContextBudgetUnsatisfiableException) {
            val report = error.report ?: MultiTurnRuntime(repository).explainContextBudget(sessionId, playerInput)
            val reportPath = options["budget-report"]?.let { Path.of(it) } ?: out.budgetFailurePath()
            writeJson(reportPath, report)
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
        val repository = repository(options)
        val sessionId = options.required("session")
        val result = readJson<AiTurnResult>(Path.of(options.required("result")))
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
        val repository = repository(options)
        val sessionId = options.required("session")
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
        val count = repository(options).abortPendingContext(
            sessionId = options.required("session"),
            contextId = options["context"],
        )
        println("aborted=$count")
        return 0
    }

    private fun showScene(options: Map<String, String>): Int {
        val repository = repository(options)
        val session = repository.getSession(options.required("session")) ?: error("Session not found: ${options.required("session")}")
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
        val repository = repository(options)
        val session = repository.getSession(options.required("session")) ?: error("Session not found: ${options.required("session")}")
        repository.listActiveStoryThreadViews(session.id).forEach {
            println("${it.threadId} [${it.type}] status=${it.status} progress=${it.progress} urgency=${it.urgency}")
            println("  publicSummary=${it.publicSummary}")
            println("  directorOnlyGuidance=${it.directorGuidance}")
        }
        return 0
    }

    private fun previewTransitions(options: Map<String, String>): Int {
        val repository = repository(options)
        val sessionId = options.required("session")
        val input = options.required("input")
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
        val repository = repository(options)
        val session = repository.getSession(options.required("session")) ?: error("Session not found: ${options.required("session")}")
        repository.tableRows("scheduled_world_events")
            .filter { it["project_id"] == session.projectId && (it["session_id"].isNullOrBlank() || it["session_id"] == session.id) }
            .sortedWith(compareBy<Map<String, String>> { it["due_turn_number"]?.toIntOrNull() ?: Int.MAX_VALUE }.thenBy { it["id"] })
            .forEach {
                println("${it["id"]} [${it["event_type"]}] status=${it["status"]} dueTurn=${it["due_turn_number"]} triggered=${it["triggered_turn_number"]} consumed=${it["consumed_turn_number"]}")
            }
        return 0
    }

    private fun explainSceneState(options: Map<String, String>): Int {
        val repository = repository(options)
        val session = repository.getSession(options.required("session")) ?: error("Session not found: ${options.required("session")}")
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
        val repository = repository(options)
        val session = repository.getSession(options.required("session")) ?: error("Session not found: ${options.required("session")}")
        val characterId = options.required("character")
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
        val repository = repository(options)
        val sessionId = options.required("session")
        val characterId = options.required("character")
        val input = options.required("input")
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
        val context = readJson<ContextPacket>(Path.of(options.required("file")))
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
        val file = Path.of(options.required("file"))
        val report = ContextAuditService().auditFile(file)
        options["out"]?.let { writeJson(Path.of(it), report) }
        println("valid=${report.valid}")
        println("actualBytes=${report.actualBytes}/${report.maxBytes}")
        println("declaredActualBytes=${report.declaredActualBytes}")
        println("hashValid=${report.hashValid}")
        report.violations.forEach { println("violation=$it") }
        return if (report.valid) 0 else 1
    }

    private fun auditNarrativeQuality(options: Map<String, String>): Int {
        val contextPath = Path.of(options.required("context"))
        val resultPath = Path.of(options.required("result"))
        val evidencePath = options["evidence"]?.let { Path.of(it) }
        val evidenceText = evidencePath?.let { Files.readString(it).removePrefix("\uFEFF") }.orEmpty()
        val report = NarrativeQualityAuditService().audit(
            context = readJson(contextPath),
            result = readJson(resultPath),
            input = NarrativeQualityAuditInput(
                contextFile = contextPath.toString(),
                resultFile = resultPath.toString(),
                evidenceFile = evidencePath?.toString().orEmpty(),
                evidenceText = evidenceText,
            ),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("score=${report.score}")
        println("severity=${report.severity}")
        println("findings=${report.findings.size}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.message}")
        }
        return 0
    }

    private fun validateScriptPacketDraft(options: Map<String, String>): Int {
        val packetPath = Path.of(options.required("packet"))
        val packet = readJson<ScriptPacketDraft>(packetPath)
        val report = ScriptPacketDraftValidationService().validate(
            packet = packet,
            packetFile = packetPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val scriptPacketPath = Path.of(options.required("script-packet-draft"))
        val preflightPath = Path.of(options.required("script-packet-preflight"))
        val acceptedPacketPath = Path.of(options.required("accepted-draft-packet"))
        val report = ScriptPacketDraftSourceBindingValidationService().validate(
            scriptPacketDraft = readJson<ScriptPacketDraft>(scriptPacketPath),
            scriptPacketPreflight = readJson<NovelizationScriptPacketPreflightReport>(preflightPath),
            acceptedDraftPacket = readJson<NovelizationAcceptedDraftPacket>(acceptedPacketPath),
            scriptPacketDraftFile = scriptPacketPath.toString(),
            scriptPacketPreflightFile = preflightPath.toString(),
            acceptedDraftPacketFile = acceptedPacketPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val scriptPacketPath = Path.of(options.required("script-packet-draft"))
        val preflightPath = Path.of(options.required("script-packet-preflight"))
        val acceptedPacketPath = Path.of(options.required("accepted-draft-packet"))
        val report = ScriptPacketDraftStructureBridgeService().validate(
            scriptPacketDraft = readJson<ScriptPacketDraft>(scriptPacketPath),
            scriptPacketPreflight = readJson<NovelizationScriptPacketPreflightReport>(preflightPath),
            acceptedDraftPacket = readJson<NovelizationAcceptedDraftPacket>(acceptedPacketPath),
            scriptPacketDraftFile = scriptPacketPath.toString(),
            scriptPacketPreflightFile = preflightPath.toString(),
            acceptedDraftPacketFile = acceptedPacketPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val scriptPacketPath = Path.of(options.required("script-packet-draft"))
        val bridgeReportPath = Path.of(options.required("structure-bridge-report"))
        val scriptPacketText = Files.readString(scriptPacketPath).removePrefix("\uFEFF")
        val report = AcceptedScriptPacketHandoffPacketService().build(
            scriptPacketDraftText = scriptPacketText,
            scriptPacketDraft = readJson<ScriptPacketDraft>(scriptPacketPath),
            structureBridgeReport = readJson<ScriptPacketDraftStructureBridgeReport>(bridgeReportPath),
            scriptPacketDraftFile = scriptPacketPath.toString(),
            structureBridgeReportFile = bridgeReportPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val packetPath = Path.of(options.required("accepted-script-packet"))
        val report = StoryboardPlanningPreflightService().validate(
            acceptedScriptPacket = readJson<AcceptedScriptPacketHandoffPacket>(packetPath),
            preflightId = options["preflight"] ?: "",
            acceptedScriptPacketFile = packetPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val packetPath = Path.of(options.required("accepted-script-packet"))
        val preflightPath = Path.of(options.required("storyboard-planning-preflight"))
        val report = StoryboardPlanningBriefService().build(
            acceptedScriptPacket = readJson<AcceptedScriptPacketHandoffPacket>(packetPath),
            storyboardPlanningPreflight = readJson<StoryboardPlanningPreflightReport>(preflightPath),
            briefId = options["brief"] ?: "",
            acceptedScriptPacketFile = packetPath.toString(),
            storyboardPlanningPreflightFile = preflightPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val briefPath = Path.of(options.required("storyboard-planning-brief"))
        val draftPath = Path.of(options.required("storyboard-packet-draft"))
        val report = StoryboardPacketDraftValidationService().validate(
            storyboardPlanningBrief = readJson<StoryboardPlanningBrief>(briefPath),
            storyboardPacketDraft = readJson<StoryboardPacketDraft>(draftPath),
            storyboardPlanningBriefFile = briefPath.toString(),
            storyboardPacketDraftFile = draftPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val draftPath = Path.of(options.required("storyboard-packet-draft"))
        val reportPath = Path.of(options.required("storyboard-packet-draft-report"))
        val review = StoryboardPacketReviewService().create(
            storyboardPacketDraft = readJson<StoryboardPacketDraft>(draftPath),
            storyboardPacketDraftReport = readJson<StoryboardPacketDraftValidationReport>(reportPath),
            reviewId = options["review"] ?: "",
            storyboardPacketDraftFile = draftPath.toString(),
            storyboardPacketDraftReportFile = reportPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), review) }
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
        val reviewPath = Path.of(options.required("storyboard-packet-review"))
        val report = StoryboardPacketHumanReviewPreflightService().validate(
            storyboardPacketReview = readJson<StoryboardPacketReviewPacket>(reviewPath),
            preflightId = options["preflight"] ?: "",
            storyboardPacketReviewFile = reviewPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val draftPath = Path.of(options.required("storyboard-packet-draft"))
        val reviewPath = Path.of(options.required("storyboard-packet-review"))
        val preflightPath = Path.of(options.required("storyboard-human-review-preflight"))
        val report = StoryboardStoryMovementAuditService().audit(
            storyboardPacketDraft = readJson<StoryboardPacketDraft>(draftPath),
            storyboardPacketReview = readJson<StoryboardPacketReviewPacket>(reviewPath),
            storyboardHumanReviewPreflight = readJson<StoryboardPacketHumanReviewPreflightReport>(preflightPath),
            storyboardPacketDraftFile = draftPath.toString(),
            storyboardPacketReviewFile = reviewPath.toString(),
            storyboardHumanReviewPreflightFile = preflightPath.toString(),
            auditId = options["audit"] ?: "",
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val draftPath = Path.of(options.required("storyboard-packet-draft"))
        val reviewPath = Path.of(options.required("storyboard-packet-review"))
        val preflightPath = Path.of(options.required("storyboard-human-review-preflight"))
        val auditPath = Path.of(options.required("storyboard-story-movement-audit"))
        val packet = ReviewableStoryboardPacketService().create(
            storyboardPacketDraft = readJson<StoryboardPacketDraft>(draftPath),
            storyboardPacketReview = readJson<StoryboardPacketReviewPacket>(reviewPath),
            storyboardHumanReviewPreflight = readJson<StoryboardPacketHumanReviewPreflightReport>(preflightPath),
            storyboardStoryMovementAudit = readJson<StoryboardStoryMovementAuditReport>(auditPath),
            storyboardPacketDraftFile = draftPath.toString(),
            storyboardPacketReviewFile = reviewPath.toString(),
            storyboardHumanReviewPreflightFile = preflightPath.toString(),
            storyboardStoryMovementAuditFile = auditPath.toString(),
            packetId = options["packet"] ?: "",
        )
        options["out"]?.let { writeJson(Path.of(it), packet) }
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

    private fun validateStoryboardPacketHumanReviewDecision(options: Map<String, String>): Int {
        val preflightPath = Path.of(options.required("human-review-preflight"))
        val decisionPath = Path.of(options.required("human-review-decision"))
        val report = StoryboardPacketHumanReviewDecisionIntakeService().validate(
            humanReviewPreflight = readJson<StoryboardPacketHumanReviewPreflightReport>(preflightPath),
            humanReviewDecision = readJson<StoryboardPacketHumanReviewDecision>(decisionPath),
            humanReviewPreflightFile = preflightPath.toString(),
            humanReviewDecisionFile = decisionPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val intakePath = Path.of(options.required("human-review-decision-intake"))
        val reviewPath = Path.of(options.required("storyboard-packet-review"))
        val report = AssetCandidatePreflightService().validate(
            humanReviewDecisionIntake = readJson<StoryboardPacketHumanReviewDecisionIntakeReport>(intakePath),
            storyboardPacketReview = readJson<StoryboardPacketReviewPacket>(reviewPath),
            preflightId = options["preflight"] ?: "",
            humanReviewDecisionIntakeFile = intakePath.toString(),
            storyboardPacketReviewFile = reviewPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val preflightPath = Path.of(options.required("asset-candidate-preflight"))
        val brief = AssetCandidateBriefService().create(
            assetCandidatePreflight = readJson<AssetCandidatePreflightReport>(preflightPath),
            briefId = options["brief"] ?: "",
            assetCandidatePreflightFile = preflightPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), brief) }
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
        val briefPath = Path.of(options.required("asset-candidate-brief"))
        val draft = AssetCandidateDraftService().create(
            assetCandidateBrief = readJson<AssetCandidateBrief>(briefPath),
            draftId = options["draft"] ?: "",
            assetCandidateBriefFile = briefPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), draft) }
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
        val draftPath = Path.of(options.required("asset-candidate-draft"))
        val report = AssetCandidateDraftValidationService().validate(
            assetCandidateDraft = readJson<AssetCandidateDraft>(draftPath),
            assetCandidateDraftFile = draftPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val validationPath = Path.of(options.required("asset-candidate-draft-validation"))
        val report = AssetCandidateReviewPreflightService().validate(
            assetCandidateDraftValidation = readJson<AssetCandidateDraftValidationReport>(validationPath),
            preflightId = options["preflight"] ?: "",
            assetCandidateDraftValidationReportFile = validationPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val preflightPath = Path.of(options.required("asset-candidate-review-preflight"))
        val decisionPath = Path.of(options.required("asset-candidate-human-review-decision"))
        val report = AssetCandidateHumanReviewDecisionIntakeService().validate(
            assetCandidateReviewPreflight = readJson<AssetCandidateReviewPreflightReport>(preflightPath),
            assetCandidateHumanReviewDecision = readJson<AssetCandidateHumanReviewDecision>(decisionPath),
            assetCandidateReviewPreflightFile = preflightPath.toString(),
            assetCandidateHumanReviewDecisionFile = decisionPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val intakePath = Path.of(options.required("asset-candidate-human-review-decision-intake"))
        val report = ImageCandidatePreflightService().validate(
            decisionIntake = readJson<AssetCandidateHumanReviewDecisionIntakeReport>(intakePath),
            preflightId = options["preflight"] ?: "",
            decisionIntakeFile = intakePath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val preflightPath = Path.of(options.required("image-candidate-preflight"))
        val brief = ImageCandidateBriefService().create(
            imageCandidatePreflight = readJson<ImageCandidatePreflightReport>(preflightPath),
            briefId = options["brief"] ?: "",
            imageCandidatePreflightFile = preflightPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), brief) }
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
        val briefPath = Path.of(options.required("image-candidate-brief"))
        val preparation = ImageCandidatePromptPreparationService().create(
            imageCandidateBrief = readJson<ImageCandidateBrief>(briefPath),
            preparationId = options["preparation"] ?: "",
            imageCandidateBriefFile = briefPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), preparation) }
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
        val preparationPath = Path.of(options.required("image-candidate-prompt-preparation"))
        val report = ImageCandidatePromptValidationService().validate(
            promptPreparation = readJson<ImageCandidatePromptPreparation>(preparationPath),
            promptPreparationFile = preparationPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val validationPath = Path.of(options.required("image-candidate-prompt-validation"))
        val report = ImageCandidateProviderHandoffPreflightService().validate(
            promptValidationReport = readJson<ImageCandidatePromptValidationReport>(validationPath),
            preflightId = options["preflight"] ?: "",
            promptValidationReportFile = validationPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val preflightPath = Path.of(options.required("provider-handoff-preflight"))
        val decisionPath = Path.of(options.required("provider-handoff-decision"))
        val report = ImageCandidateProviderHandoffDecisionIntakeService().validate(
            providerHandoffPreflight = readJson<ImageCandidateProviderHandoffPreflightReport>(preflightPath),
            providerHandoffDecision = readJson<ImageCandidateProviderHandoffDecision>(decisionPath),
            providerHandoffPreflightFile = preflightPath.toString(),
            providerHandoffDecisionFile = decisionPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val preflightPath = Path.of(options.required("provider-handoff-preflight"))
        val intakePath = Path.of(options.required("provider-handoff-decision-intake"))
        val packet = ImageCandidateProviderHandoffPacketService().create(
            providerHandoffPreflight = readJson<ImageCandidateProviderHandoffPreflightReport>(preflightPath),
            decisionIntake = readJson<ImageCandidateProviderHandoffDecisionIntakeReport>(intakePath),
            packetId = options["packet"] ?: "",
            providerHandoffPreflightFile = preflightPath.toString(),
            providerHandoffDecisionIntakeFile = intakePath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), packet) }
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
        val packetPath = Path.of(options.required("provider-handoff-packet"))
        val report = ImageCandidateProviderHandoffPacketValidationService().validate(
            providerHandoffPacket = readJson<ImageCandidateProviderHandoffPacket>(packetPath),
            providerHandoffPacketFile = packetPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val validationPath = Path.of(options.required("provider-handoff-packet-validation"))
        val externalResultPath = Path.of(options.required("external-result"))
        val report = ImageCandidateExternalResultIntakeService().validate(
            providerHandoffPacketValidation = readJson<ImageCandidateProviderHandoffPacketValidationReport>(validationPath),
            externalResult = readJson<ImageCandidateExternalResult>(externalResultPath),
            providerHandoffPacketValidationReportFile = validationPath.toString(),
            externalResultFile = externalResultPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val intakePath = Path.of(options.required("external-result-intake"))
        val report = ImageCandidateResultReviewPreflightService().validate(
            externalResultIntake = readJson<ImageCandidateExternalResultIntakeReport>(intakePath),
            preflightId = options["preflight"] ?: "",
            externalResultIntakeFile = intakePath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val preflightPath = Path.of(options.required("result-review-preflight"))
        val decisionPath = Path.of(options.required("result-review-decision"))
        val report = ImageCandidateResultHumanReviewDecisionIntakeService().validate(
            resultReviewPreflight = readJson<ImageCandidateResultReviewPreflightReport>(preflightPath),
            resultReviewDecision = readJson<ImageCandidateResultReviewDecision>(decisionPath),
            resultReviewPreflightFile = preflightPath.toString(),
            resultReviewDecisionFile = decisionPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val preflightPath = Path.of(options.required("result-review-preflight"))
        val intakePath = Path.of(options.required("result-review-decision-intake"))
        val packet = ImageCandidateReferencePacketService().create(
            resultReviewPreflight = readJson<ImageCandidateResultReviewPreflightReport>(preflightPath),
            decisionIntake = readJson<ImageCandidateResultReviewDecisionIntakeReport>(intakePath),
            packetId = options["packet"] ?: "",
            resultReviewPreflightFile = preflightPath.toString(),
            resultReviewDecisionIntakeFile = intakePath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), packet) }
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
        val packetPath = Path.of(options.required("candidate-reference-packet"))
        val report = ImageCandidateReferencePacketValidationService().validate(
            referencePacket = readJson<ImageCandidateReferencePacket>(packetPath),
            referencePacketFile = packetPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val validationPath = Path.of(options.required("candidate-reference-packet-validation"))
        val decisionPath = Path.of(options.required("human-acceptance-decision"))
        val report = CandidateReferenceHumanAcceptanceDecisionIntakeService().validate(
            candidateReferencePacketValidation = readJson<ImageCandidateReferencePacketValidationReport>(validationPath),
            humanAcceptanceDecision = readJson<CandidateReferenceHumanAcceptanceDecision>(decisionPath),
            candidateReferencePacketValidationReportFile = validationPath.toString(),
            humanAcceptanceDecisionFile = decisionPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val intakePath = Path.of(options.required("human-acceptance-decision-intake"))
        val validationPath = Path.of(options.required("candidate-reference-packet-validation"))
        val report = CandidateReferenceRegistryPreflightService().validate(
            humanAcceptanceDecisionIntake = readJson<CandidateReferenceHumanAcceptanceDecisionIntakeReport>(intakePath),
            candidateReferencePacketValidation = readJson<ImageCandidateReferencePacketValidationReport>(validationPath),
            preflightId = options["preflight"] ?: "",
            humanAcceptanceDecisionIntakeFile = intakePath.toString(),
            candidateReferencePacketValidationReportFile = validationPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val preflightPath = Path.of(options.required("registry-preflight"))
        val packet = CandidateReferenceRegistryPacketService().create(
            registryPreflight = readJson<CandidateReferenceRegistryPreflightReport>(preflightPath),
            packetId = options["packet"] ?: "",
            registryPreflightFile = preflightPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), packet) }
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
        val packetPath = Path.of(options.required("candidate-reference-registry-packet"))
        val report = CandidateReferenceRegistryPacketValidationService().validate(
            registryPacket = readJson<CandidateReferenceRegistryPacket>(packetPath),
            registryPacketFile = packetPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val validationPath = Path.of(options.required("registry-packet-validation"))
        val decisionPath = Path.of(options.required("human-acceptance-decision"))
        val report = CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService().validate(
            registryPacketValidation = readJson<CandidateReferenceRegistryPacketValidationReport>(validationPath),
            humanAcceptanceDecision = readJson<CandidateReferenceRegistryHumanAcceptanceDecision>(decisionPath),
            registryPacketValidationReportFile = validationPath.toString(),
            humanAcceptanceDecisionFile = decisionPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val validationPath = Path.of(options.required("registry-packet-validation"))
        val intakePath = Path.of(options.required("human-acceptance-decision-intake"))
        val packet = CandidateReferenceAcceptancePacketService().create(
            registryPacketValidation = readJson<CandidateReferenceRegistryPacketValidationReport>(validationPath),
            decisionIntake = readJson<CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport>(intakePath),
            packetId = options["packet"] ?: "",
            registryPacketValidationReportFile = validationPath.toString(),
            humanAcceptanceDecisionIntakeFile = intakePath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), packet) }
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
        val packetPath = Path.of(options.required("candidate-reference-acceptance-packet"))
        val report = CandidateReferenceAcceptancePacketValidationService().validate(
            packet = readJson<CandidateReferenceAcceptancePacket>(packetPath),
            acceptancePacketFile = packetPath.toString(),
            validationId = options["validation"] ?: "",
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val out = Path.of(options.required("out"))
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
                        "這不是XX，這是XXX",
                        "這不是 X，這是 Y",
                        "不是XX，而是XXX",
                        "不是 X，而是 Y",
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
                "這不是XX，這是XXX",
                "這不是 X，這是 Y",
                "不是XX，而是XXX",
                "不是 X，而是 Y",
                "not X, but Y",
                "not X, only Y",
            ),
            reviewerNotes = "Fill this template before drafting accepted dialogue. No character card means no accepted dialogue.",
        )
        writeJson(out, template)
        println("wrote=$out")
        println("gateId=${template.gateId}")
        println("cards=${template.characterCards.size}")
        return 0
    }

    private fun validateCharacterNuwaGate(options: Map<String, String>): Int {
        val gatePath = options["file"]?.let { Path.of(it) }
            ?: options["character-dir"]?.let { Path.of(it).resolve("character_nuwa_gate.json") }
            ?: error("Missing required option --file or --character-dir")
        val gate = readJson<CharacterNuwaGateFile>(gatePath)
        val report = CharacterNuwaGateValidationService().validate(
            gate = gate,
            gateFile = gatePath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
            readJson<CharacterNuwaGateValidationReport>(it)
        } ?: CharacterNuwaGateValidationService().validate(
            gate = readJson<CharacterNuwaGateFile>(gatePath!!),
            gateFile = gatePath.toString(),
        )
        val report = NovelizationPreflightValidationService().validate(
            characterNuwaReport = characterNuwaReport,
            characterNuwaReportFile = reportPath?.toString().orEmpty(),
            characterNuwaGateFile = gatePath?.toString().orEmpty(),
            preflightId = options["preflight"] ?: "",
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val out = Path.of(options.required("out"))
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
        writeJson(out, template)
        println("wrote=$out")
        println("briefId=${template.briefId}")
        println("targetChapter=${template.targetChapter}")
        return 0
    }

    private fun validateNovelizationWriterBrief(options: Map<String, String>): Int {
        val briefPath = Path.of(options.required("brief"))
        val brief = readJson<NovelizationWriterBrief>(briefPath)
        val preflightReportPath = options["preflight-report"]?.let { Path.of(it) }
        val report = NovelizationWriterBriefValidationService().validate(
            brief = brief,
            briefFile = briefPath.toString(),
            sourcePreflightReport = preflightReportPath?.let { readJson<NovelizationPreflightReport>(it) },
            sourcePreflightReportFile = preflightReportPath?.toString().orEmpty(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("briefId=${report.briefId}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    private fun validateNovelizationDraftIntake(options: Map<String, String>): Int {
        val draftPath = Path.of(options.required("draft"))
        val writerBriefReportPath = Path.of(options.required("writer-brief-report"))
        val report = NovelizationDraftIntakeValidationService().validate(
            draftText = Files.readString(draftPath).removePrefix("\uFEFF"),
            writerBriefReport = readJson<NovelizationWriterBriefValidationReport>(writerBriefReportPath),
            draftFile = draftPath.toString(),
            writerBriefReportFile = writerBriefReportPath.toString(),
            sourceType = options.required("source-type"),
            sourceLabel = options["source-label"] ?: "",
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val draftPath = Path.of(options.required("draft"))
        val draftIntakeReportPath = Path.of(options.required("draft-intake-report"))
        val report = NovelizationProseQualityAuditService().audit(
            draftText = Files.readString(draftPath).removePrefix("\uFEFF"),
            draftIntakeReport = readJson<NovelizationDraftIntakeReport>(draftIntakeReportPath),
            draftFile = draftPath.toString(),
            draftIntakeReportFile = draftIntakeReportPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val draftPath = Path.of(options.required("draft"))
        val draftIntakeReportPath = Path.of(options.required("draft-intake-report"))
        val proseQualityReportPath = Path.of(options.required("prose-quality-report"))
        val continuityAnchorsPath = Path.of(options.required("continuity-anchors"))
        val report = NovelizationRevisionBriefService().build(
            draftText = Files.readString(draftPath).removePrefix("\uFEFF"),
            draftIntakeReport = readJson<NovelizationDraftIntakeReport>(draftIntakeReportPath),
            proseQualityReport = readJson<NovelizationProseQualityAuditReport>(proseQualityReportPath),
            continuityAnchors = readJson<NovelizationRevisionContinuityAnchors>(continuityAnchorsPath),
            briefId = options["brief"] ?: "",
            draftFile = draftPath.toString(),
            draftIntakeReportFile = draftIntakeReportPath.toString(),
            proseQualityAuditReportFile = proseQualityReportPath.toString(),
            continuityAnchorsFile = continuityAnchorsPath.toString(),
        )
        val out = Path.of(options.required("out"))
        writeJson(out, report)
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
        val originalDraftPath = Path.of(options.required("original-draft"))
        val revisedDraftPath = Path.of(options.required("revised-draft"))
        val revisionBriefReportPath = Path.of(options.required("revision-brief-report"))
        val declaredHandlingPath = options["declared-handling"]?.let { Path.of(it) }
        val report = NovelizationRevisedDraftIntakeValidationService().validate(
            originalDraftText = Files.readString(originalDraftPath).removePrefix("\uFEFF"),
            revisedDraftText = Files.readString(revisedDraftPath).removePrefix("\uFEFF"),
            revisionBriefReport = readJson<NovelizationRevisionBriefReport>(revisionBriefReportPath),
            declaredHandling = declaredHandlingPath?.let { readJson<NovelizationRevisionHandlingDeclaration>(it) }
                ?: NovelizationRevisionHandlingDeclaration(),
            originalDraftFile = originalDraftPath.toString(),
            revisedDraftFile = revisedDraftPath.toString(),
            revisionBriefReportFile = revisionBriefReportPath.toString(),
            declaredHandlingFile = declaredHandlingPath?.toString().orEmpty(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val revisedDraftPath = Path.of(options.required("revised-draft"))
        val revisedDraftIntakeReportPath = Path.of(options.required("revised-draft-intake-report"))
        val revisionBriefReportPath = Path.of(options.required("revision-brief-report"))
        val continuityAnchorsPath = Path.of(options.required("continuity-anchors"))
        val report = NovelizationAcceptedDraftPacketService().build(
            revisedDraftText = Files.readString(revisedDraftPath).removePrefix("\uFEFF"),
            revisedDraftIntakeReport = readJson<NovelizationRevisedDraftIntakeReport>(revisedDraftIntakeReportPath),
            revisionBriefReport = readJson<NovelizationRevisionBriefReport>(revisionBriefReportPath),
            continuityAnchors = readJson<NovelizationRevisionContinuityAnchors>(continuityAnchorsPath),
            packetId = options["packet"] ?: "",
            revisedDraftFile = revisedDraftPath.toString(),
            revisedDraftIntakeReportFile = revisedDraftIntakeReportPath.toString(),
            revisionBriefReportFile = revisionBriefReportPath.toString(),
            continuityAnchorsFile = continuityAnchorsPath.toString(),
        )
        val out = Path.of(options.required("out"))
        writeJson(out, report)
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
        val packetPath = Path.of(options.required("accepted-draft-packet"))
        val report = NovelizationScriptPacketPreflightService().validate(
            acceptedDraftPacket = readJson<NovelizationAcceptedDraftPacket>(packetPath),
            preflightId = options["preflight"] ?: "",
            acceptedDraftPacketFile = packetPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val previousPath = Path.of(options.required("previous"))
        val draftPath = Path.of(options.required("draft"))
        val anchorPath = Path.of(options.required("anchors"))
        val report = ChapterContinuityAuditService().audit(
            previousText = Files.readString(previousPath).removePrefix("\uFEFF"),
            draftText = Files.readString(draftPath).removePrefix("\uFEFF"),
            anchors = readJson<ChapterContinuityAnchorFile>(anchorPath),
            previousFile = previousPath.toString(),
            draftFile = draftPath.toString(),
            anchorFile = anchorPath.toString(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val repository = repository(options)
        val input = options["input"]
            ?: options["input-file"]?.let { Files.readString(Path.of(it)).removePrefix("\uFEFF") }
            ?: error("Missing required option --input or --input-file")
        val report = MultiTurnRuntime(repository).explainContextBudget(
            sessionId = options.required("session"),
            playerInput = input,
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val repository = repository(options)
        val sessionId = options.required("session")
        val session = repository.getSession(sessionId) ?: error("Session not found: $sessionId")
        val report = CapabilityContractResolver(repository).explain(
            projectId = session.projectId,
            sessionId = sessionId,
            entityType = options.required("entity-type"),
            entityId = options.required("entity-id"),
            actorId = options["actor"],
            intendedUse = options["intended-use"].orEmpty(),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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
        val repository = repository(options)
        repository.initialize()
        val checkpoint = CheckpointService(repository).createCheckpoint(
            sessionId = options.required("session"),
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
        val repository = repository(options)
        repository.initialize()
        CheckpointService(repository).listCheckpoints(options.required("session")).forEach {
            println("${it.id} branch=${it.branchId ?: ""} turn=${it.turnNumber} stateVersion=${it.stateVersion} kind=${it.kind} label=${it.label} createdAt=${it.createdAt}")
        }
        return 0
    }

    private fun showCheckpoint(options: Map<String, String>): Int {
        val repository = repository(options)
        repository.initialize()
        val checkpoint = CheckpointService(repository).showCheckpoint(options.required("checkpoint"))
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
        val repository = repository(options)
        repository.initialize()
        val result = TimelineBranchService(repository).forkSession(
            checkpointId = options.required("checkpoint"),
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
        val repository = repository(options)
        repository.initialize()
        val branch = TimelineBranchService(repository).checkoutBranch(options.required("branch"))
        println("branchId=${branch.id}")
        println("status=${branch.status}")
        println("activeSessionId=${branch.activeSessionId ?: ""}")
        println("sourceCheckpointId=${branch.sourceCheckpointId ?: ""}")
        return 0
    }

    private fun showTimeline(options: Map<String, String>): Int {
        val repository = repository(options)
        repository.initialize()
        val export = TimelineBranchService(repository).showTimeline(options.required("project"))
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
        val repository = repository(options)
        repository.initialize()
        val report = ReplayAuditService(repository).verify(
            sessionId = options.required("session"),
            record = options["record"] == "true",
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
        println("status=${report.status}")
        println("checkedTurnCount=${report.checkedTurnCount}")
        report.issues.forEach { println("${it.code}: turn=${it.turnNumber ?: ""} ${it.message}") }
        return if (report.status == "PASS") 0 else 1
    }

    private fun exportTimeline(options: Map<String, String>): Int {
        val repository = repository(options)
        repository.initialize()
        val export = TimelineBranchService(repository).exportTimeline(options.required("project"))
        writeJson(Path.of(options.required("out")), export)
        println("wrote=${options.required("out")}")
        println("branches=${export.branches.size}")
        println("checkpoints=${export.checkpoints.size}")
        println("activeBranchId=${export.activeBranchId ?: ""}")
        return 0
    }

    private fun exportSessionEvidence(options: Map<String, String>): Int {
        val repository = repository(options)
        val export = SessionEvidenceExporter(repository).export(options.required("session"))
        writeJson(Path.of(options.required("out")), export)
        println("wrote=${options.required("out")}")
        println("turnLogs=${export.turnLogs.size}")
        println("stateDeltas=${export.stateDeltas.size}")
        println("pendingContexts=${export.pendingContextCount}")
        return 0
    }

    private fun pilotPreflight(options: Map<String, String>): Int {
        val repository = repository(options)
        val report = PilotPreflightService(repository).run(
            projectId = options.required("project"),
            sessionId = options.required("session"),
        )
        options["out"]?.let { writeJson(Path.of(it), report) }
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

    private fun createSettingPackTemplate(options: Map<String, String>): Int {
        val projectId = options.required("project")
        val out = Path.of(options.required("out"))
        val pack = SettingPack(
            packId = options["pack"] ?: "$projectId-setting-pack",
            packVersion = options["version"]?.toIntOrNull() ?: 1,
            projectId = projectId,
            name = "Setting Pack Template",
            description = "Template for local canon import.",
            mode = options["mode"] ?: "ADD_ON",
            source = SettingPackSource(author = options["author"] ?: "local-author"),
            entities = SettingPackEntities(
                worldRules = listOf(
                    SettingPackEntity(
                        id = "rule-template",
                        title = "Template rule",
                        content = "Replace this rule with canon text.",
                        visibility = "public",
                    ),
                ),
            ),
        )
        out.parent?.let { Files.createDirectories(it) }
        Files.writeString(out, JsonSupport.json.encodeToString(pack))
        println("wrote=$out")
        return 0
    }

    private fun validateSettingPack(options: Map<String, String>): Int {
        val repository = repository(options)
        repository.initialize()
        val pack = SettingPackParser().parse(Path.of(options.required("file")))
        val preview = SettingPackImportService(repository).preview(pack)
        printValidation(preview.validation)
        options["out"]?.let { writeJson(Path.of(it), preview.validation) }
        return if (preview.validation.isValid) 0 else 1
    }

    private fun previewSettingImport(options: Map<String, String>): Int {
        val repository = repository(options)
        repository.initialize()
        val pack = SettingPackParser().parse(Path.of(options.required("file")))
        val preview = SettingPackImportService(repository).preview(pack)
        printValidation(preview.validation)
        println("canonicalHash=${preview.canonicalHash}")
        println("diffEntries=${preview.diff.entries.size}")
        preview.diff.entries.forEach { println("- ${it.action} ${it.entityType}:${it.entityId} v${it.previousVersion ?: "-"} -> ${it.newVersion}") }
        println("impactPaths=${preview.impact.paths.size}")
        println("impactedSessions=${preview.impact.impactedSessions.joinToString(",")}")
        preview.impact.blockers.forEach { println("blocker=$it") }
        options["out"]?.let { writeJson(Path.of(it), preview) }
        return if (preview.validation.isValid) 0 else 1
    }

    private fun applySettingPack(options: Map<String, String>): Int {
        val repository = repository(options)
        repository.initialize()
        val file = Path.of(options.required("file"))
        val pack = SettingPackParser().parse(file)
        val result = SettingPackImportService(repository).apply(pack, file.toString())
        println("applied=${result.applied}")
        println("importId=${result.importId}")
        println("pack=${result.packId}@${result.packVersion}")
        println("canonicalHash=${result.canonicalHash}")
        println("diffEntries=${result.diff.entries.size}")
        println("impactPaths=${result.impact.paths.size}")
        options["out"]?.let { writeJson(Path.of(it), result) }
        return 0
    }

    private fun exportSettingPack(options: Map<String, String>): Int {
        val repository = repository(options)
        repository.initialize()
        val pack = SettingPackImportService(repository).export(
            projectId = options.required("project"),
            packId = options.required("pack"),
            packVersion = options.required("version").toInt(),
        )
        val out = Path.of(options.required("out"))
        writeJson(out, pack)
        println("wrote=$out")
        return 0
    }

    private fun diffSettingPack(options: Map<String, String>): Int {
        val repository = repository(options)
        repository.initialize()
        val pack = SettingPackParser().parse(Path.of(options.required("file")))
        val diff = SettingPackDiffService(repository).diff(pack, canonicalHash(pack))
        diff.entries.forEach { println("${it.action} ${it.entityType}:${it.entityId} v${it.previousVersion ?: "-"} -> ${it.newVersion}") }
        options["out"]?.let { writeJson(Path.of(it), diff) }
        return 0
    }

    private fun impactReport(options: Map<String, String>): Int {
        val repository = repository(options)
        repository.initialize()
        val pack = SettingPackParser().parse(Path.of(options.required("file")))
        val diff = SettingPackDiffService(repository).diff(pack, canonicalHash(pack))
        val report = ImpactAnalyzer(repository).analyze(pack, diff)
        println("changedRoots=${report.changedRoots.joinToString(",")}")
        println("impactedSessions=${report.impactedSessions.joinToString(",")}")
        println("impactedCharacters=${report.impactedCharacters.joinToString(",")}")
        println("impactedScenes=${report.impactedScenes.joinToString(",")}")
        println("impactedThreads=${report.impactedThreads.joinToString(",")}")
        report.blockers.forEach { println("blocker=$it") }
        options["out"]?.let { writeJson(Path.of(it), report) }
        return 0
    }

    private fun showDependencies(options: Map<String, String>): Int {
        val repository = repository(options)
        repository.initialize()
        DependencyGraphService(repository).show(
            projectId = options.required("project"),
            entityType = options["entity-type"],
            entityId = options["entity-id"],
            depth = options["depth"]?.toIntOrNull() ?: 2,
        ).forEach(::println)
        return 0
    }

    private fun validateProjectCanon(options: Map<String, String>): Int {
        val repository = repository(options)
        repository.initialize()
        val report = CanonConsistencyValidator(repository).validate(options.required("project"))
        printValidation(report)
        options["out"]?.let { writeJson(Path.of(it), report) }
        return if (report.isValid) 0 else 1
    }

    private fun printValidation(report: com.localnovel.engine.model.SettingPackValidationReport) {
        println("valid=${report.isValid}")
        println("issueCount=${report.issues.size}")
        report.issues.forEach { println("${it.severity}:${it.code}:${it.path}:${it.message}") }
    }

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }

    private fun repository(options: Map<String, String>): SqliteStoryRepository =
        SqliteStoryRepository(Path.of(options["db"] ?: "data/story.db"))

    private fun runner(repository: SqliteStoryRepository): TurnRunner =
        TurnRunner(
            contextCompiler = ContextCompiler(repository),
            validator = AiTurnResultValidator(repository),
            stateReducer = StateReducer(repository),
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private fun parseOptions(args: List<String>): Map<String, String> {
        val options = mutableMapOf<String, String>()
        var index = 0
        while (index < args.size) {
            val token = args[index]
            require(token.startsWith("--")) { "Expected option starting with --, got '$token'." }
            val key = token.removePrefix("--")
            if (key == "record") {
                options[key] = "true"
                index += 1
                continue
            }
            val value = args.getOrNull(index + 1)
            require(value != null && !value.startsWith("--")) { "Missing value for option --$key." }
            options[key] = value
            index += 2
        }
        return options
    }

    private fun Map<String, String>.required(key: String): String =
        this[key] ?: error("Missing required option --$key")

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
              export-setting-pack [--db <path>] --project <id> --pack <id> --version <n> --out <path>
              diff-setting-pack [--db <path>] --file <path> [--out <path>]
              impact-report [--db <path>] --file <path> [--out <path>]
              show-dependencies [--db <path>] --project <id> [--entity-type <type> --entity-id <id>] [--depth <n>]
              validate-project-canon [--db <path>] --project <id> [--out <path>]
              audit-context --file <path> [--out <path>]
              audit-narrative-quality --context <path> --result <path> [--evidence <path>] [--out <path>]
              audit-chapter-continuity --previous <path> --draft <path> --anchors <path> [--out <path>]
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
