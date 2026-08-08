package com.localnovel.engine.cli

import com.localnovel.engine.service.AiTaskQueueService
import com.localnovel.engine.service.WorldSourceArchiveImportService
import com.localnovel.engine.service.WorldSourceQueryService
import java.nio.file.Files
import java.nio.file.Path

internal object WorldSourceAndTaskQueueCli {
    fun validateWorldSourceArchive(options: Map<String, String>): Int {
        val manifest = Path.of(options.requiredCliOption("manifest"))
        val report = WorldSourceArchiveImportService().validateManifestFile(manifest)
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("status=${report.status}")
        println("manifestId=${report.manifestId}")
        println("sourceCount=${report.evidence.sourceCount}")
        println("verifiedSourceCount=${report.evidence.verifiedSourceCount}")
        println("hashMismatchCount=${report.evidence.hashMismatchCount}")
        println("missingRawFileCount=${report.evidence.missingRawFileCount}")
        println("unsafeBoundaryFlagCount=${report.evidence.unsafeBoundaryFlagCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("findings=${report.findingCount}")
        return if (report.verdict == "PASS") 0 else 1
    }

    fun applyWorldSourceArchive(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val manifest = Path.of(options.requiredCliOption("manifest"))
        val databasePath = Path.of(options["db"] ?: "data/story.db")
        val result = WorldSourceArchiveImportService(repository).applyManifestFile(
            manifestPath = manifest,
            databasePath = databasePath,
        )
        options["out"]?.let { writeCliJson(Path.of(it), result) }
        println("verdict=${result.verdict}")
        println("status=${result.status}")
        println("archiveId=${result.archiveId}")
        println("archiveRowsWritten=${result.archiveRowsWritten}")
        println("documentRowsWritten=${result.documentRowsWritten}")
        println("chunkRowsWritten=${result.chunkRowsWritten}")
        println("sourceCount=${result.sourceCount}")
        println("totalByteSize=${result.totalByteSize}")
        println("downstreamBoundary=${result.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${result.nextAllowedDownstreamStep}")
        println("findings=${result.findingCount}")
        return if (result.verdict == "PASS") 0 else 1
    }

    fun queryWorldSource(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val report = WorldSourceQueryService(repository).query(
            text = options.requiredCliOption("q"),
            classification = options["classification"].orEmpty(),
            limit = options["limit"]?.toIntOrNull() ?: 10,
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("status=${report.status}")
        println("query=${report.query}")
        println("hitCount=${report.hitCount}")
        report.hits.forEach {
            println("- ${it.sourceId} ${it.title} [${it.heading}] ${it.snippet}")
        }
        return 0
    }

    fun upsertAiTask(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val payloadJson = options["payload"]?.let { Files.readString(Path.of(it)).removePrefix("\uFEFF") }
            ?: options["payload-json"]
            ?: "{}"
        val result = AiTaskQueueService(repository).upsertTask(
            taskId = options.requiredCliOption("task"),
            title = options.requiredCliOption("title"),
            payloadJson = payloadJson,
            taskType = options["type"] ?: "implementation",
            ownerRole = options["owner"] ?: "B組01",
            targetRole = options["target"] ?: "B組02",
            priority = options["priority"]?.toIntOrNull() ?: 100,
            status = options["status"] ?: "READY",
            source = options["source"] ?: "user",
            actor = options["actor"] ?: "user",
        )
        options["out"]?.let { writeCliJson(Path.of(it), result) }
        println("status=${result.status}")
        println("taskId=${result.task?.taskId.orEmpty()}")
        println("taskStatus=${result.task?.status.orEmpty()}")
        println("targetRole=${result.task?.targetRole.orEmpty()}")
        println("priority=${result.task?.priority ?: 0}")
        return 0
    }

    fun claimAiTask(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val result = AiTaskQueueService(repository).claimNextTask(
            worker = options.requiredCliOption("worker"),
            targetRole = options["target"].orEmpty(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), result) }
        println("status=${result.status}")
        println("taskId=${result.task?.taskId.orEmpty()}")
        println("taskStatus=${result.task?.status.orEmpty()}")
        println("title=${result.task?.title.orEmpty()}")
        println("nextAllowedStep=${result.nextAllowedStep}")
        return 0
    }

    fun completeAiTask(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val result = AiTaskQueueService(repository).completeTask(
            taskId = options.requiredCliOption("task"),
            worker = options.requiredCliOption("worker"),
            resultStatus = options.requiredCliOption("result"),
            summary = options["summary"].orEmpty(),
            reportPath = options["report"].orEmpty(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), result) }
        println("status=${result.status}")
        println("taskId=${result.task?.taskId.orEmpty()}")
        println("taskStatus=${result.task?.status.orEmpty()}")
        println("result=${result.task?.resultStatus.orEmpty()}")
        println("nextAllowedStep=${result.nextAllowedStep}")
        return 0
    }

    fun listAiTasks(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        val report = AiTaskQueueService(repository).listTasks(status = options["status"].orEmpty())
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("status=${report.status}")
        println("count=${report.count}")
        report.tasks.forEach {
            println("- ${it.taskId} ${it.status} priority=${it.priority} target=${it.targetRole} title=${it.title}")
        }
        return 0
    }
}