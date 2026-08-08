package com.localnovel.engine.cli

import com.localnovel.engine.model.DialogueEvaluationRequest
import com.localnovel.engine.model.DialogueFinalizeRequest
import com.localnovel.engine.model.DialogueSelectionStatus
import com.localnovel.engine.repository.DialogueProductionRepository
import com.localnovel.engine.service.DialogueFinalizationService
import com.localnovel.engine.service.DialogueReviewOrchestrator
import com.localnovel.engine.service.DialogueSelectionGateService
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

internal object DialogueProductionCli {
    private val strictInputJson = Json {
        ignoreUnknownKeys = false
        explicitNulls = false
    }

    fun evaluate(options: Map<String, String>): Int {
        val inputPath = Path.of(options.requiredCliOption("input"))
        requireTopLevelKeys(inputPath, setOf("artifact"))
        val request = readStrictCliJson<DialogueEvaluationRequest>(inputPath)
        val repository = dialogueRepository(options)
        val handoff = DialogueReviewOrchestrator(repository).evaluate(request.artifact)
        options["out"]?.let { writeCliJson(Path.of(it), handoff) }
        println("status=REVIEW_PENDING")
        println("artifactId=${handoff.artifact.artifactId}")
        println("artifactSha256=${handoff.artifact.contentSha256}")
        println("ruleSet=${handoff.artifact.ruleSetId}@${handoff.artifact.ruleSetVersion}")
        println("deterministicBlockers=${handoff.deterministicFindings.count { it.severity.name == "BLOCKER" }}")
        return 0
    }

    fun showFindings(options: Map<String, String>): Int {
        val view = DialogueReviewOrchestrator(dialogueRepository(options)).show(options.requiredCliOption("artifact"))
        options["out"]?.let { writeCliJson(Path.of(it), view) }
        println("artifactId=${view.artifact.artifactId}")
        println("approvalState=${view.approvalState}")
        println("reviewCount=${view.reviews.size}")
        println("findingCount=${view.findings.size}")
        println("blockerCount=${view.findings.count { it.severity.name == "BLOCKER" }}")
        return 0
    }

    fun finalize(options: Map<String, String>): Int {
        val inputPath = Path.of(options.requiredCliOption("input"))
        requireTopLevelKeys(inputPath, setOf("reviewBatch", "selection"))
        val request = readStrictCliJson<DialogueFinalizeRequest>(inputPath)
        val decision = DialogueFinalizationService(dialogueRepository(options)).finalize(request)
        options["out"]?.let { writeCliJson(Path.of(it), decision) }
        println("artifactId=${decision.artifactId}")
        println("status=${decision.status}")
        println("blockerCount=${decision.blockerCount}")
        println("warningCount=${decision.warningCount}")
        println("decisionSha256=${decision.decisionSha256}")
        return if (decision.status == DialogueSelectionStatus.SELECTED) 0 else 2
    }

    fun showSelection(options: Map<String, String>): Int {
        val view = DialogueSelectionGateService(dialogueRepository(options)).show(options.requiredCliOption("artifact"))
        options["out"]?.let { writeCliJson(Path.of(it), view) }
        val decision = view.decisions.lastOrNull()
        println("artifactId=${view.artifact.artifactId}")
        println("approvalState=${view.approvalState}")
        println("selectionStatus=${decision?.status ?: "NONE"}")
        println("selectedArtifactSha256=${decision?.takeIf { it.status == DialogueSelectionStatus.SELECTED }?.selectedArtifactSha256.orEmpty()}")
        return 0
    }

    private fun dialogueRepository(options: Map<String, String>): DialogueProductionRepository {
        val sqlite = cliRepository(options).also { it.initialize() }
        return DialogueProductionRepository(sqlite).also { it.bootstrap() }
    }

    private fun requireTopLevelKeys(path: Path, allowed: Set<String>) {
        val keys = JsonSupport.json.parseToJsonElement(Files.readString(path).removePrefix("\uFEFF")).jsonObject.keys
        require(keys == allowed) { "Unexpected or missing top-level fields: expected $allowed, got $keys" }
    }

    private inline fun <reified T> readStrictCliJson(path: Path): T =
        strictInputJson.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
}
