package com.localnovel.engine.service

import com.localnovel.engine.model.StoryChapterHandoffArtifact
import com.localnovel.engine.model.StoryChapterHandoffRequest
import com.localnovel.engine.model.StoryChapterProductionSeed
import com.localnovel.engine.model.StoryChapterSourcePacket
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.repository.StoryChapterProductionRepository
import com.localnovel.engine.support.JsonSupport
import java.security.MessageDigest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class StoryChapterProductionService(
    private val repository: SqliteStoryRepository,
) {
    fun createHandoff(request: StoryChapterHandoffRequest): StoryChapterHandoffArtifact {
        repository.initialize()
        require(request.productionId.isNotBlank() && request.runId.isNotBlank())
        require(request.worldSourceProjectLabel.isNotBlank()) { "World-source project label is required." }
        require(request.worldQueries.isNotEmpty()) { "At least one SQL world-source query is required." }
        require(request.targetChineseCharactersMin >= 1500)
        require(request.targetChineseCharactersMax >= request.targetChineseCharactersMin)
        val runtime = repository.withConnection { connection ->
            repository.queryRows(
                connection,
                """
                SELECT project_id, session_id, scenario_seed, context_id, context_hash, context_snapshot_json,
                       validation_classification, status
                FROM reboot_run_evidence WHERE run_id = ?
                """.trimIndent(),
                request.runId,
            ).singleOrNull()
        } ?: error("Unknown SQL runtime evidence run: ${request.runId}")
        require(runtime["project_id"] == request.projectId) { "Runtime project does not match handoff request." }
        require(runtime["session_id"] == request.sessionId) { "Runtime session does not match handoff request." }
        require(runtime["scenario_seed"] == request.runtimeScenarioSeed) { "Runtime scenario seed does not match handoff request." }
        require(runtime["status"] == "APPLIED") { "Only APPLIED runtime evidence may enter a chapter handoff." }
        require(runtime["validation_classification"] in setOf("PASS", "QUALITY_WARNING")) {
            "Hard-failed runtime evidence cannot enter a chapter handoff."
        }
        val runtimeContext = JsonSupport.json.decodeFromString<ContextPacket>(runtime["context_snapshot_json"].orEmpty())
        require(runtimeContext.project.id == request.projectId && runtimeContext.sessionId == request.sessionId)
        require(runtimeContext.contextId == runtime["context_id"] && runtimeContext.contextHash == runtime["context_hash"])
        val recomputedContextHash = sha256(JsonSupport.json.encodeToString(runtimeContext.copy(contextHash = "")))
        require(recomputedContextHash == runtimeContext.contextHash) { "Runtime context hash verification failed." }

        val worldReports = request.worldQueries.map { query ->
            require(query.allowedSourceIds.isNotEmpty()) { "Every chapter query requires an explicit source allowlist." }
            WorldSourceQueryService(repository).query(
                text = query.text,
                classification = query.classification,
                limit = query.limit,
                projectLabel = request.worldSourceProjectLabel,
                sourceIds = query.allowedSourceIds,
                officialOnly = true,
            )
                .also { require(it.hitCount > 0) { "World-source query returned no SQL evidence: ${query.text}" } }
        }
        val packet = StoryChapterSourcePacket(
            productionId = request.productionId,
            projectId = request.projectId,
            sessionId = request.sessionId,
            scenarioSeed = request.scenarioSeed,
            sourceCommitSha = request.sourceCommitSha,
            chapterTitle = request.chapterTitle,
            premise = request.premise,
            targetChineseCharactersMin = request.targetChineseCharactersMin,
            targetChineseCharactersMax = request.targetChineseCharactersMax,
            requiredBeats = request.requiredBeats,
            forbiddenOutcomes = request.forbiddenOutcomes,
            voiceRequirements = request.voiceRequirements,
            runtimeContextId = runtime["context_id"].orEmpty(),
            runtimeContextHash = runtime["context_hash"].orEmpty(),
            runtimeContextJson = runtime["context_snapshot_json"].orEmpty(),
            worldSourceQueries = worldReports,
        )
        val packetJson = JsonSupport.json.encodeToString(packet)
        val packetHash = sha256(packetJson)
        StoryChapterProductionRepository(repository).create(
            StoryChapterProductionSeed(
                productionId = request.productionId,
                projectId = request.projectId,
                sessionId = request.sessionId,
                scenarioSeed = request.scenarioSeed,
                sourceCommitSha = request.sourceCommitSha,
                sourcePacketJson = packetJson,
                sourcePacketSha256 = packetHash,
            ),
        )
        return StoryChapterHandoffArtifact(packet, packetHash)
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}
