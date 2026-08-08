package com.localnovel.engine.cli

import com.localnovel.engine.cli.cliRepository
import com.localnovel.engine.cli.requiredCliOption
import com.localnovel.engine.cli.writeCliJson
import com.localnovel.engine.service.CheckpointService
import com.localnovel.engine.service.ReplayAuditService
import com.localnovel.engine.service.TimelineBranchService
import java.nio.file.Path

internal object CheckpointCli {
    fun createCheckpoint(options: Map<String, String>): Int {
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

    fun listCheckpoints(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        CheckpointService(repository).listCheckpoints(options.requiredCliOption("session")).forEach {
            println("${it.id} branch=${it.branchId ?: ""} turn=${it.turnNumber} stateVersion=${it.stateVersion} kind=${it.kind} label=${it.label} createdAt=${it.createdAt}")
        }
        return 0
    }

    fun showCheckpoint(options: Map<String, String>): Int {
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

    fun forkSession(options: Map<String, String>): Int {
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

    fun checkoutBranch(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val branch = TimelineBranchService(repository).checkoutBranch(options.requiredCliOption("branch"))
        println("branchId=${branch.id}")
        println("status=${branch.status}")
        println("activeSessionId=${branch.activeSessionId ?: ""}")
        println("sourceCheckpointId=${branch.sourceCheckpointId ?: ""}")
        return 0
    }

    fun showTimeline(options: Map<String, String>): Int {
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

    fun verifyReplayChain(options: Map<String, String>): Int {
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

    fun exportTimeline(options: Map<String, String>): Int {
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
}
