package com.localnovel.engine.cli

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterMemory
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DemoProjectSeed
import com.localnovel.engine.model.DramaEvaluationInput
import com.localnovel.engine.model.NpcAutonomyProfile
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.AiTurnResultValidator
import com.localnovel.engine.service.CapabilityContractResolver
import com.localnovel.engine.service.ContextAuditService
import com.localnovel.engine.service.ContextBudgetUnsatisfiableException
import com.localnovel.engine.service.ContextCompiler
import com.localnovel.engine.service.MemoryRetriever
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.service.OffscreenSimulationService
import com.localnovel.engine.service.NarrativeQualityAuditInput
import com.localnovel.engine.service.NarrativeQualityAuditRunner
import com.localnovel.engine.service.NarrativeQualityAuditService
import com.localnovel.engine.service.NarrativeQualityGateOptions
import com.localnovel.engine.service.NarrativeQualityGateService
import com.localnovel.engine.service.ResolveTurnOptions
import com.localnovel.engine.service.RebootRunEvidenceService
import com.localnovel.engine.service.RebootDramaLoopService
import com.localnovel.engine.service.RebootVerticalSliceRunner
import com.localnovel.engine.service.RebootReplayBenchmarkRunner
import com.localnovel.engine.service.PilotPreflightService
import com.localnovel.engine.service.AudioNovelEpisodeExportOptions
import com.localnovel.engine.service.AudioNovelEpisodeExporter
import com.localnovel.engine.service.AudioNovelPublishAuditReport
import com.localnovel.engine.service.AudioNovelPublishAuditService
import com.localnovel.engine.service.AudioNovelSubtitleAuditService
import com.localnovel.engine.model.ComicVideoVoiceTimelineFill
import com.localnovel.engine.service.SessionEvidenceExporter
import com.localnovel.engine.service.StallDetector
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

internal object StorySessionCli {
    fun initDb(options: Map<String, String>): Int {
        cliRepository(options).initialize()
        println("Initialized SQLite database: ${options.requiredCliOption("db")}")
        return 0
    }

    fun seedDemo(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val seedPath = Path.of(options["seed"] ?: "samples/demo_project_seed.json")
        val seed = JsonSupport.json.decodeFromString<DemoProjectSeed>(Files.readString(seedPath))
        repository.seedDemo(seed)
        println("Seeded demo project '${seed.project.id}' into ${options.requiredCliOption("db")}")
        return 0
    }

    fun runOffscreenNpcTick(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val profile = readCliJson<NpcAutonomyProfile>(Path.of(options.requiredCliOption("profile")))
        val result = OffscreenSimulationService(repository).run(
            sessionId = options.requiredCliOption("session"),
            profile = profile,
            scenarioSeed = options.requiredCliOption("scenario-seed"),
        )
        options["out"]?.let { outValue ->
            val out = Path.of(outValue)
            out.parent?.let { Files.createDirectories(it) }
            Files.writeString(out, JsonSupport.json.encodeToString(result))
        }
        println(JsonSupport.json.encodeToString(result))
        return if (result.accepted) 0 else 3
    }

    fun evaluateRebootDrama(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val input = readCliJson<DramaEvaluationInput>(Path.of(options.requiredCliOption("input")))
        val report = RebootDramaLoopService(repository).evaluateAndPersist(input)
        options["out"]?.let { outValue ->
            val out = Path.of(outValue)
            out.parent?.let { Files.createDirectories(it) }
            Files.writeString(out, JsonSupport.json.encodeToString(report))
        }
        println(JsonSupport.json.encodeToString(report))
        return 0
    }

    fun runReboot01VerticalSlice(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        Files.deleteIfExists(repository.databasePath())
        val summary = RebootVerticalSliceRunner(
            repository = repository,
            outputDir = Path.of(options.requiredCliOption("out-dir")),
        ).run(
            basePackPath = Path.of(options["base-pack"] ?: "world/chenxing/engine/chenxing_setting_pack_v1.json"),
            addonPackPath = Path.of(options["addon-pack"] ?: "samples/reboot01/chenxing_vertical_slice_addon.json"),
            npcProfilePath = Path.of(options["npc-profile"] ?: "samples/reboot01/lin_yao_offscreen_profile.json"),
        )
        println(JsonSupport.json.encodeToString(summary))
        return 0
    }

    fun benchmarkReboot01Evolution(options: Map<String, String>): Int {
        val report = RebootReplayBenchmarkRunner(Path.of(options.requiredCliOption("out-dir"))).run(
            basePackPath = Path.of(options["base-pack"] ?: "world/chenxing/engine/chenxing_setting_pack_v1.json"),
            addonPackPath = Path.of(options["addon-pack"] ?: "samples/reboot01/chenxing_vertical_slice_addon.json"),
            npcProfilePath = Path.of(options["npc-profile"] ?: "samples/reboot01/lin_yao_offscreen_profile.json"),
        )
        println(JsonSupport.json.encodeToString(report))
        return if (report.decision == "ADOPT") 0 else 3
    }

    fun compileContext(options: Map<String, String>): Int {
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

    fun validateResult(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val contextPath = Path.of(options.requiredCliOption("context"))
        val resultPath = Path.of(options.requiredCliOption("result"))
        val context = readCliJson<ContextPacket>(contextPath)
        val result = readCliJson<AiTurnResult>(resultPath)
        val report = AiTurnResultValidator(repository).validate(context, result)
        if (!report.isValid) {
            report.errors.forEach { System.err.println("${it.code}: ${it.message}") }
            return 1
        }

        val evidencePath = options["evidence"]?.let { Path.of(it) }
        val evidenceText = evidencePath?.let { Files.readString(it).removePrefix("\uFEFF") }.orEmpty()
        val auditRun = NarrativeQualityGateService().evaluateRun(
            context = context,
            result = result,
            options = NarrativeQualityGateOptions(
                strictMode = options.containsKey("strict-narrative-quality"),
                enforceSceneContextFloor = repository.settingPackRows(context.project.id).isNotEmpty(),
                sampleLabel = options["sample-label"].orEmpty(),
                evidenceText = evidenceText,
                autoPromoteLearnedRules = cliAutoPromoteLearnedRules(options),
            ),
            input = narrativeQualityAuditInput(
                options = options,
                repository = repository,
                contextFile = contextPath.toString(),
                resultFile = resultPath.toString(),
                evidenceFile = evidencePath?.toString().orEmpty(),
                evidenceText = evidenceText,
            ),
        )
        val gateResult = auditRun.gateResult ?: error("Narrative quality gate result missing.")
        options["narrative-quality-out"]?.let { writeCliJson(Path.of(it), gateResult.auditReport) }
        options["promotion-out"]?.let { outPath ->
            auditRun.promotionReport?.let { writeCliJson(Path.of(outPath), it) }
        }
        auditRun.promotionReport?.let { promotion ->
            if (promotion.promotedCount > 0) {
                println("learnedRulesPromoted=${promotion.promotedCount}")
                promotion.promotedRuleIds.forEach { println("learned_rule_promoted=$it") }
                println("learnedRulesStore=${promotion.storePath}")
            }
        }
        val gateErrors = NarrativeQualityGateService().toValidationErrors(gateResult)
        if (gateErrors.isNotEmpty()) {
            gateErrors.forEach { System.err.println("${it.code}: ${it.message}") }
            return 1
        }
        if (gateResult.hasAdvisoryFindings) {
            println("Result is valid with narrative quality advisories.")
            gateResult.advisoryFindings.forEach {
                println("narrative_quality_advisory:${it.ruleId}:${it.severity}:${it.message}")
            }
        } else {
            println("Result is valid.")
        }
        return 0
    }

    fun applyResult(options: Map<String, String>): Int {
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

    fun runManualTurn(options: Map<String, String>): Int {
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

    fun showState(options: Map<String, String>): Int {
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

    fun startSession(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val readerMode = options.containsKey("reader-mode")
        val session = MultiTurnRuntime(repository).startSession(
            projectId = options.requiredCliOption("project"),
            sceneId = options.requiredCliOption("scene"),
            readerMode = readerMode,
        )
        println("sessionId=${session.id}")
        println("readerMode=$readerMode")
        println("branchId=${session.branchId ?: ""}")
        println("currentSceneId=${session.currentSceneId}")
        println("nextTurnNumber=${session.nextTurnNumber}")
        println("stateVersion=${session.stateVersion}")
        return 0
    }

    fun prepareTurn(options: Map<String, String>): Int {
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

    fun resolveTurn(options: Map<String, String>): Int {
        val startedAtNanos = System.nanoTime()
        val repository = cliRepository(options)
        repository.initialize()
        val sessionId = options.requiredCliOption("session")
        val resultPath = Path.of(options.requiredCliOption("result"))
        val rawResultText = Files.readString(resultPath).removePrefix("\uFEFF")
        val evidencePath = options["evidence"]?.let { Path.of(it) }
        val evidenceText = evidencePath?.let { Files.readString(it).removePrefix("\uFEFF") }.orEmpty()
        val baseResolveOptions = ResolveTurnOptions(
            strictNarrativeQuality = options.containsKey("strict-narrative-quality"),
            narrativeQualityEvidenceText = evidenceText,
            narrativeQualitySampleLabel = options["sample-label"].orEmpty(),
            autoPromoteLearnedRules = cliAutoPromoteLearnedRules(options),
            logicAuditFile = options["logic-audit"].orEmpty(),
            dbPath = repository.databasePath().toString(),
            scenarioSeed = options["scenario-seed"].orEmpty(),
            revisionSuggestion = options["revision-suggestion"].orEmpty(),
            revisedDraft = options["revised-draft-file"]
                ?.let { Files.readString(Path.of(it)).removePrefix("\uFEFF") }
                .orEmpty(),
        )
        val result = try {
            JsonSupport.json.decodeFromString<AiTurnResult>(rawResultText)
        } catch (error: Throwable) {
            RebootRunEvidenceService(repository).persistProtocolEnvelope(
                cliSessionId = sessionId,
                rawResultText = rawResultText,
                parsedResult = null,
                options = baseResolveOptions,
                code = "malformed_result_json",
                message = error.message.orEmpty(),
                startedAtNanos = startedAtNanos,
            )
            throw error
        }
        val irreversibleContract = try {
            options["irreversible-contract"]
                ?.let { readCliJson<com.localnovel.engine.model.IrreversibleChangeContract>(Path.of(it)) }
        } catch (error: Throwable) {
            RebootRunEvidenceService(repository).persistProtocolEnvelope(
                cliSessionId = sessionId,
                rawResultText = rawResultText,
                parsedResult = result,
                options = baseResolveOptions,
                code = "malformed_irreversible_contract",
                message = error.message.orEmpty(),
                startedAtNanos = startedAtNanos,
            )
            throw error
        }
        val irreversibleApproval = try {
            options["irreversible-approval"]
                ?.let { readCliJson<com.localnovel.engine.model.IrreversibleApprovalArtifact>(Path.of(it)) }
        } catch (error: Throwable) {
            RebootRunEvidenceService(repository).persistProtocolEnvelope(
                cliSessionId = sessionId,
                rawResultText = rawResultText,
                parsedResult = result,
                options = baseResolveOptions.copy(irreversibleChangeContract = irreversibleContract),
                code = "malformed_irreversible_approval",
                message = error.message.orEmpty(),
                startedAtNanos = startedAtNanos,
            )
            throw error
        }
        val resolveOptions = baseResolveOptions.copy(
            irreversibleChangeContract = irreversibleContract,
            irreversibleApprovalArtifact = irreversibleApproval,
        )
        options["narrative-quality-out"]?.let { outPath ->
            val context = repository.getPendingContext(sessionId, result.contextId)
            if (context == null) {
                RebootRunEvidenceService(repository).persistProtocolEnvelope(
                    cliSessionId = sessionId,
                    rawResultText = rawResultText,
                    parsedResult = result,
                    options = resolveOptions,
                    code = "pending_context_not_found",
                    message = "Pending context not found for session '$sessionId' and context '${result.contextId}'.",
                    startedAtNanos = startedAtNanos,
                )
                error("Pending context not found for session '$sessionId' and context '${result.contextId}'.")
            }
            val auditRun = NarrativeQualityGateService().evaluateRun(
                context = context,
                result = result,
                options = NarrativeQualityGateOptions(
                    strictMode = resolveOptions.strictNarrativeQuality,
                    enforceSceneContextFloor = repository.settingPackRows(context.project.id).isNotEmpty(),
                    sampleLabel = resolveOptions.narrativeQualitySampleLabel,
                    evidenceText = resolveOptions.narrativeQualityEvidenceText,
                    autoPromoteLearnedRules = resolveOptions.autoPromoteLearnedRules,
                ),
                input = narrativeQualityAuditInput(
                    options = options,
                    repository = repository,
                    contextFile = "pending://${context.contextId}",
                    resultFile = resultPath.toString(),
                    evidenceFile = evidencePath?.toString().orEmpty(),
                    evidenceText = evidenceText,
                ),
            )
            val gateResult = auditRun.gateResult ?: error("Narrative quality gate result missing.")
            writeCliJson(Path.of(outPath), gateResult.auditReport)
            options["promotion-out"]?.let { promotionOut ->
                auditRun.promotionReport?.let { writeCliJson(Path.of(promotionOut), it) }
            }
            auditRun.promotionReport?.let { promotion ->
                if (promotion.promotedCount > 0) {
                    println("learnedRulesPromoted=${promotion.promotedCount}")
                    promotion.promotedRuleIds.forEach { println("learned_rule_promoted=$it") }
                    println("learnedRulesStore=${promotion.storePath}")
                }
            }
        }
        val applied = MultiTurnRuntime(repository).resolveTurn(sessionId, result, resolveOptions)
        val session = repository.getSession(sessionId)
        println(applied.narrative)
        println()
        println("turnLogId=${applied.turnLogId}")
        println("stateVersion=${session?.stateVersion}")
        println("nextTurnNumber=${session?.nextTurnNumber}")
        return 0
    }

    fun showSession(options: Map<String, String>): Int {
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

    fun abortPendingTurn(options: Map<String, String>): Int {
        val count = cliRepository(options).abortPendingContext(
            sessionId = options.requiredCliOption("session"),
            contextId = options["context"],
        )
        println("aborted=$count")
        return 0
    }

    fun showScene(options: Map<String, String>): Int {
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

    fun showThreads(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val session = repository.getSession(options.requiredCliOption("session")) ?: error("Session not found: ${options.requiredCliOption("session")}")
        repository.listActiveStoryThreadViews(session.id).forEach {
            println("${it.threadId} [${it.type}] status=${it.status} progress=${it.progress} urgency=${it.urgency}")
            println("  publicSummary=${it.publicSummary}")
            println("  directorOnlyGuidance=${it.directorGuidance}")
        }
        return 0
    }

    fun previewTransitions(options: Map<String, String>): Int {
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

    fun showWorldEvents(options: Map<String, String>): Int {
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

    fun explainSceneState(options: Map<String, String>): Int {
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

    fun showCharacterMind(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val session = repository.getSession(options.requiredCliOption("session")) ?: error("Session not found: ${options.requiredCliOption("session")}")
        val characterId = options.requiredCliOption("character")
        val state = repository.getCharacterState(session.projectId, characterId) ?: error("Character state not found: $characterId")
        val memories = repository.listAllCharacterMemories(session.projectId).filter { it.ownerCharacterId == characterId || it.characterId == characterId }
        val activeMemories = memories
            .filter { it.status == "active" && it.visibility != "author_only" && it.visibility != "inaccessible" }
            .sortedWith(compareByDescending<CharacterMemory> { it.salience }.thenBy { it.id })
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

    fun previewRecall(options: Map<String, String>): Int {
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

    fun explainContext(options: Map<String, String>): Int {
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

    fun auditContext(options: Map<String, String>): Int {
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

    fun auditNarrativeQuality(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val contextPath = Path.of(options.requiredCliOption("context"))
        val resultPath = Path.of(options.requiredCliOption("result"))
        val evidencePath = options["evidence"]?.let { Path.of(it) }
        val evidenceText = evidencePath?.let { Files.readString(it).removePrefix("\uFEFF") }.orEmpty()
        val context = readCliJson<ContextPacket>(contextPath)
        val result = readCliJson<AiTurnResult>(resultPath)
        val auditRun = NarrativeQualityAuditRunner.audit(
            context = context,
            result = result,
            input = narrativeQualityAuditInput(
                options = options,
                repository = repository,
                contextFile = contextPath.toString(),
                resultFile = resultPath.toString(),
                evidenceFile = evidencePath?.toString().orEmpty(),
                evidenceText = evidenceText,
                sampleLabel = options["sample-label"].orEmpty(),
            ),
            autoPromoteLearnedRules = cliAutoPromoteLearnedRules(options),
        )
        val report = auditRun.auditReport
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        options["promotion-out"]?.let { outPath ->
            auditRun.promotionReport?.let { writeCliJson(Path.of(outPath), it) }
        }
        auditRun.promotionReport?.let { promotion ->
            if (promotion.promotedCount > 0) {
                println("learnedRulesPromoted=${promotion.promotedCount}")
                promotion.promotedRuleIds.forEach { println("learned_rule_promoted=$it") }
                println("learnedRulesStore=${promotion.storePath}")
            }
        }
        println("verdict=${report.verdict}")
        println("score=${report.score}")
        println("severity=${report.severity}")
        println("findings=${report.findings.size}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.message}")
        }
        return 0
    }

    private fun narrativeQualityAuditInput(
        options: Map<String, String>,
        repository: SqliteStoryRepository,
        contextFile: String,
        resultFile: String,
        evidenceFile: String = "",
        evidenceText: String = "",
        sampleLabel: String = "",
    ): NarrativeQualityAuditInput =
        NarrativeQualityAuditInput(
            contextFile = contextFile,
            resultFile = resultFile,
            evidenceFile = evidenceFile,
            evidenceText = evidenceText,
            sampleLabel = sampleLabel,
            dbPath = repository.databasePath().toString(),
            logicAuditFindings = cliLogicAuditFindings(options),
        )

    fun explainContextBudget(options: Map<String, String>): Int {
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

    fun explainCapability(options: Map<String, String>): Int {
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

    fun exportAudioNovelEpisode(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val sessionId = options.requiredCliOption("session")
        val outDir = Path.of(options.requiredCliOption("out-dir"))
        val fromTurn = options["from-turn"]?.toIntOrNull() ?: 1
        val toTurn = options["to-turn"]?.toIntOrNull() ?: Int.MAX_VALUE
        val publishMode = options.containsKey("publish")
        if (publishMode) {
            val preExportAudit = AudioNovelPublishAuditService().audit(
                repository = repository,
                sessionId = sessionId,
                fromTurn = fromTurn,
                toTurn = toTurn,
            )
            if (preExportAudit.verdict != "PASS") {
                reportPublishBlockers(preExportAudit)
                return 1
            }
        }
        val export = AudioNovelEpisodeExporter(repository).export(
            sessionId = sessionId,
            options = AudioNovelEpisodeExportOptions(
                episodeId = options["episode-id"].orEmpty(),
                title = options["title"] ?: "第一回　塵",
                fromTurn = fromTurn,
                toTurn = toTurn,
                coverImagePath = options["cover"].orEmpty(),
            ),
        )
        Files.createDirectories(outDir)
        val exportPath = outDir.resolve("episode_export.json")
        val scriptPath = outDir.resolve("episode_script.md")
        val timelinePath = outDir.resolve("voice_timeline.json")
        writeCliJson(exportPath, export)
        Files.writeString(scriptPath, export.scriptMarkdown)
        writeCliJson(timelinePath, export.voiceTimeline)
        if (publishMode) {
            val publishAudit = AudioNovelPublishAuditService().audit(
                repository = repository,
                sessionId = sessionId,
                fromTurn = fromTurn,
                toTurn = toTurn,
                episodeDir = outDir,
                voiceTimeline = export.voiceTimeline,
            )
            val publishAuditPath = outDir.resolve("publish_audit.json")
            writeCliJson(publishAuditPath, publishAudit)
            println("publishAuditVerdict=${publishAudit.verdict}")
            println("publishAuditMajorFindings=${publishAudit.majorFindingCount}")
            println("publishAuditWarningFindings=${publishAudit.warningFindingCount}")
            println("wrote=$publishAuditPath")
            if (publishAudit.verdict != "PASS") {
                reportPublishBlockers(publishAudit)
                return 1
            }
        }
        println("episodeId=${export.episodeId}")
        println("turnCount=${export.turnCount}")
        println("fromTurn=${export.fromTurn}")
        println("toTurn=${export.toTurn}")
        println("audioCueCount=${export.voiceTimeline.audioCues.size}")
        println("totalDurationMillis=${export.voiceTimeline.timing.totalDurationMillis}")
        println("coverImagePath=${export.coverImagePath}")
        println("wrote=$exportPath")
        println("wrote=$scriptPath")
        println("wrote=$timelinePath")
        return 0
    }

    fun exportSessionEvidence(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val export = SessionEvidenceExporter(repository).export(options.requiredCliOption("session"))
        writeCliJson(Path.of(options.requiredCliOption("out")), export)
        println("wrote=${options.requiredCliOption("out")}")
        println("turnLogs=${export.turnLogs.size}")
        println("stateDeltas=${export.stateDeltas.size}")
        println("pendingContexts=${export.pendingContextCount}")
        return 0
    }

    fun pilotPreflight(options: Map<String, String>): Int {
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

    fun auditAudioNovelPublish(options: Map<String, String>): Int {
        val episodeDir = Path.of(options.requiredCliOption("episode-dir"))
        Files.createDirectories(episodeDir)
        val voiceTimelinePath = episodeDir.resolve("voice_timeline.json")
        val voiceTimeline = if (Files.exists(voiceTimelinePath)) {
            JsonSupport.json.decodeFromString<ComicVideoVoiceTimelineFill>(Files.readString(voiceTimelinePath))
        } else {
            null
        }
        val publishAudit = if (options.containsKey("session")) {
            AudioNovelPublishAuditService().audit(
                repository = cliRepository(options),
                sessionId = options.requiredCliOption("session"),
                fromTurn = options["from-turn"]?.toIntOrNull() ?: 1,
                toTurn = options["to-turn"]?.toIntOrNull() ?: Int.MAX_VALUE,
                episodeDir = episodeDir,
                voiceTimeline = voiceTimeline,
            )
        } else {
            val subtitleFindings = buildList {
                voiceTimeline?.let { timeline ->
                    addAll(AudioNovelSubtitleAuditService().auditVoiceTimeline(timeline))
                }
                addAll(AudioNovelSubtitleAuditService().auditSrt(episodeDir.resolve("episode_subtitles.srt")))
            }
            val majorCount = subtitleFindings.count { it.severity == "MAJOR" }
            AudioNovelPublishAuditReport(
                subtitleFindings = subtitleFindings,
                verdict = if (majorCount > 0) "FAIL" else "PASS",
                majorFindingCount = majorCount,
                warningFindingCount = subtitleFindings.count { it.severity == "WARNING" },
            )
        }
        val outPath = options["out"]?.let { Path.of(it) } ?: episodeDir.resolve("publish_audit.json")
        writeCliJson(outPath, publishAudit)
        println("publishAuditVerdict=${publishAudit.verdict}")
        println("publishAuditMajorFindings=${publishAudit.majorFindingCount}")
        println("publishAuditWarningFindings=${publishAudit.warningFindingCount}")
        println("wrote=$outPath")
        if (publishAudit.verdict != "PASS") {
            reportPublishBlockers(publishAudit)
            return 1
        }
        return 0
    }

    private fun reportPublishBlockers(publishAudit: AudioNovelPublishAuditReport) {
        publishAudit.turnReports
            .flatMap { report -> report.findings.filter { it.severity == "MAJOR" } }
            .plus(publishAudit.subtitleFindings.filter { it.severity == "MAJOR" })
            .take(8)
            .forEach { finding ->
                println("publishBlocker=${finding.ruleId} ${finding.affectedFileOrTurn} ${finding.message}")
            }
    }
}
