package com.localnovel.engine.cli

import com.localnovel.engine.service.FormalTurnPhysicalJson
import com.localnovel.engine.service.WorldPhysics05EmbodiedScenePilotService
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.encodeToString

internal object WorldPhysicsCli {
    fun runEmbodiedScenePilot(options: Map<String, String>): Int {
        val repositoryRoot = Path.of(options.requiredCliOption("repo")).toAbsolutePath().normalize()
        val output = Path.of(options.requiredCliOption("out")).toAbsolutePath().normalize()
        val report = WorldPhysics05EmbodiedScenePilotService().run(repositoryRoot)
        output.parent?.let(Files::createDirectories)
        Files.writeString(output, FormalTurnPhysicalJson.strict.encodeToString(report))
        println("technicalDecision=${report.technicalDecision}")
        println("resolvedTurns=${report.resolvedTurnCount}")
        println("rejectedAttempts=${report.rejectedAttemptCount}")
        println("physicalTransitions=${report.physicalTransitionCount}")
        println("wrote=$output")
        return 0
    }
}
