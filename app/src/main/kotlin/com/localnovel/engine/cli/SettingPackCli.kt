package com.localnovel.engine.cli

import com.localnovel.engine.model.SettingPack
import com.localnovel.engine.model.SettingPackEntities
import com.localnovel.engine.model.SettingPackEntity
import com.localnovel.engine.model.SettingPackSource
import com.localnovel.engine.service.CanonConsistencyValidator
import com.localnovel.engine.service.DependencyGraphService
import com.localnovel.engine.service.ImpactAnalyzer
import com.localnovel.engine.service.SettingPackDiffService
import com.localnovel.engine.service.SettingPackImportService
import com.localnovel.engine.service.SettingPackParser
import com.localnovel.engine.service.canonicalHash
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.encodeToString

internal object SettingPackCli {
    fun createSettingPackTemplate(options: Map<String, String>): Int {
        val projectId = options.requiredCliOption("project")
        val out = Path.of(options.requiredCliOption("out"))
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

    fun validateSettingPack(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val pack = SettingPackParser().parse(Path.of(options.requiredCliOption("file")))
        val preview = SettingPackImportService(repository).preview(pack)
        printSettingPackValidation(preview.validation)
        options["out"]?.let { writeCliJson(Path.of(it), preview.validation) }
        return if (preview.validation.isValid) 0 else 1
    }

    fun previewSettingImport(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val pack = SettingPackParser().parse(Path.of(options.requiredCliOption("file")))
        val preview = SettingPackImportService(repository).preview(pack)
        printSettingPackValidation(preview.validation)
        println("canonicalHash=${preview.canonicalHash}")
        println("diffEntries=${preview.diff.entries.size}")
        preview.diff.entries.forEach { println("- ${it.action} ${it.entityType}:${it.entityId} v${it.previousVersion ?: "-"} -> ${it.newVersion}") }
        println("impactPaths=${preview.impact.paths.size}")
        println("impactedSessions=${preview.impact.impactedSessions.joinToString(",")}")
        preview.impact.blockers.forEach { println("blocker=$it") }
        options["out"]?.let { writeCliJson(Path.of(it), preview) }
        return if (preview.validation.isValid) 0 else 1
    }

    fun applySettingPack(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val file = Path.of(options.requiredCliOption("file"))
        val pack = SettingPackParser().parse(file)
        val result = SettingPackImportService(repository).apply(pack, file.toString())
        println("applied=${result.applied}")
        println("importId=${result.importId}")
        println("pack=${result.packId}@${result.packVersion}")
        println("canonicalHash=${result.canonicalHash}")
        println("diffEntries=${result.diff.entries.size}")
        println("impactPaths=${result.impact.paths.size}")
        options["out"]?.let { writeCliJson(Path.of(it), result) }
        return 0
    }

    fun exportSettingPack(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val pack = SettingPackImportService(repository).export(
            projectId = options.requiredCliOption("project"),
            packId = options.requiredCliOption("pack"),
            packVersion = options.requiredCliOption("version").toInt(),
        )
        val out = Path.of(options.requiredCliOption("out"))
        writeCliJson(out, pack)
        println("wrote=$out")
        return 0
    }

    fun diffSettingPack(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val pack = SettingPackParser().parse(Path.of(options.requiredCliOption("file")))
        val diff = SettingPackDiffService(repository).diff(pack, canonicalHash(pack))
        diff.entries.forEach { println("${it.action} ${it.entityType}:${it.entityId} v${it.previousVersion ?: "-"} -> ${it.newVersion}") }
        options["out"]?.let { writeCliJson(Path.of(it), diff) }
        return 0
    }

    fun impactReport(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val pack = SettingPackParser().parse(Path.of(options.requiredCliOption("file")))
        val diff = SettingPackDiffService(repository).diff(pack, canonicalHash(pack))
        val report = ImpactAnalyzer(repository).analyze(pack, diff)
        println("changedRoots=${report.changedRoots.joinToString(",")}")
        println("impactedSessions=${report.impactedSessions.joinToString(",")}")
        println("impactedCharacters=${report.impactedCharacters.joinToString(",")}")
        println("impactedScenes=${report.impactedScenes.joinToString(",")}")
        println("impactedThreads=${report.impactedThreads.joinToString(",")}")
        report.blockers.forEach { println("blocker=$it") }
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        return 0
    }

    fun showDependencies(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        DependencyGraphService(repository).show(
            projectId = options.requiredCliOption("project"),
            entityType = options["entity-type"],
            entityId = options["entity-id"],
            depth = options["depth"]?.toIntOrNull() ?: 2,
        ).forEach(::println)
        return 0
    }

    fun validateProjectCanon(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val report = CanonConsistencyValidator(repository).validate(options.requiredCliOption("project"))
        printSettingPackValidation(report)
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        return if (report.isValid) 0 else 1
    }
}
