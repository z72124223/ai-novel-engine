package com.localnovel.engine.cli

import com.localnovel.engine.model.LogicAuditReport
import com.localnovel.engine.model.NarrativeQualityFinding
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.TurnRunner
import com.localnovel.engine.service.ContextCompiler
import com.localnovel.engine.service.AiTurnResultValidator
import com.localnovel.engine.service.StateReducer
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

internal fun parseCliOptions(args: List<String>): Map<String, String> {
    val options = mutableMapOf<String, String>()
    var index = 0
    while (index < args.size) {
        val token = args[index]
        require(token.startsWith("--")) { "Expected option starting with --, got '$token'." }
        val key = token.removePrefix("--")
        if (
            key == "record" ||
            key == "dry-run" ||
            key == "reader-mode" ||
            key == "publish" ||
            key == "auto-promote-learned-rules" ||
            key == "no-auto-promote-learned-rules"
        ) {
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

internal fun Map<String, String>.requiredCliOption(key: String): String =
    this[key] ?: error("Missing required option --$key")

internal inline fun <reified T> writeCliJson(path: Path, value: T) {
    path.parent?.let { Files.createDirectories(it) }
    Files.writeString(path, JsonSupport.json.encodeToString(value))
}

internal inline fun <reified T> readCliJson(path: Path): T =
    JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

internal fun cliRepository(options: Map<String, String>): SqliteStoryRepository =
    SqliteStoryRepository(Path.of(options["db"] ?: "data/story.db"))

internal fun cliAutoPromoteLearnedRules(options: Map<String, String>): Boolean =
    !options.containsKey("no-auto-promote-learned-rules")

internal fun cliLogicAuditFindings(options: Map<String, String>): List<NarrativeQualityFinding> {
    val logicAuditPath = options["logic-audit"] ?: return emptyList()
    val report = readCliJson<LogicAuditReport>(Path.of(logicAuditPath))
    return report.findings
}

internal fun cliRunner(repository: SqliteStoryRepository): TurnRunner =
    TurnRunner(
        contextCompiler = ContextCompiler(repository),
        validator = AiTurnResultValidator(repository),
        stateReducer = StateReducer(repository),
    )

internal fun printSettingPackValidation(report: com.localnovel.engine.model.SettingPackValidationReport) {
    println("valid=${report.isValid}")
    println("issueCount=${report.issues.size}")
    report.issues.forEach { println("${it.severity}:${it.code}:${it.path}:${it.message}") }
}

internal fun Path.budgetFailurePath(): Path {
    val fileName = fileName.toString()
    val dot = fileName.lastIndexOf('.')
    val base = if (dot > 0) fileName.substring(0, dot) else fileName
    val extension = if (dot > 0) fileName.substring(dot) else ".json"
    return (parent ?: Path.of(".")).resolve("${base}_budget_failure$extension")
}