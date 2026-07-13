package com.localnovel.engine.cli

import com.localnovel.engine.service.ProjectDataRebuildService
import java.nio.file.Path

internal object DataFoundationCli {
    fun rebuildProjectData(options: Map<String, String>): Int {
        val repositoryRoot = Path.of(options["repo"] ?: ".")
        val manifestPath = Path.of(options.requiredCliOption("manifest"))
        val databasePath = Path.of(options.requiredCliOption("db"))
        val outputPath = Path.of(options.requiredCliOption("out"))
        val report = ProjectDataRebuildService().rebuild(
            repositoryRoot = repositoryRoot,
            manifestPath = manifestPath,
            databasePath = databasePath,
        )
        writeCliJson(outputPath, report)
        println("verdict=${report.verdict}")
        println("status=${report.status}")
        println("database=${report.databaseFileName}")
        println("verifiedInputs=${report.verifiedInputs.count { it.status == "PASS" }}")
        println("logicalContentHash=${report.logicalContent.rootSha256}")
        println("worldSourceDocuments=${report.worldSourceDocumentCount}")
        println("worldSourceChunks=${report.worldSourceChunkCount}")
        println("wrote=$outputPath")
        return if (report.verdict == "PASS") 0 else 1
    }
}
