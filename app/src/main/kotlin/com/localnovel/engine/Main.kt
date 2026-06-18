package com.localnovel.engine

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DemoProjectSeed
import com.localnovel.engine.model.SettingPack
import com.localnovel.engine.model.SettingPackEntities
import com.localnovel.engine.model.SettingPackEntity
import com.localnovel.engine.model.SettingPackSource
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.AiTurnResultValidator
import com.localnovel.engine.service.CanonConsistencyValidator
import com.localnovel.engine.service.ContextCompiler
import com.localnovel.engine.service.DependencyGraphService
import com.localnovel.engine.service.ImpactAnalyzer
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.service.MemoryRetriever
import com.localnovel.engine.service.SettingPackDiffService
import com.localnovel.engine.service.SettingPackImportService
import com.localnovel.engine.service.SettingPackParser
import com.localnovel.engine.service.SettingPackSchemaValidator
import com.localnovel.engine.service.StallDetector
import com.localnovel.engine.service.StateReducer
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
        println("currentSceneId=${session.currentSceneId}")
        println("nextTurnNumber=${session.nextTurnNumber}")
        println("stateVersion=${session.stateVersion}")
        return 0
    }

    private fun prepareTurn(options: Map<String, String>): Int {
        val repository = repository(options)
        val context = MultiTurnRuntime(repository).prepareTurn(
            sessionId = options.required("session"),
            playerInput = options.required("input"),
        )
        val out = Path.of(options.required("out"))
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
        context.contextBudget.truncationReasons.forEach { println("- $it") }
        return 0
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
              prepare-turn [--db <path>] --session <id> --input <text> --out <path>
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
            """.trimIndent(),
        )
    }
}
