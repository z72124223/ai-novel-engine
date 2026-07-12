package com.localnovel.engine.service

import com.localnovel.engine.model.StoryChapterHandoffArtifact
import com.localnovel.engine.model.StoryChapterHandoffRequest
import com.localnovel.engine.model.StoryChapterProductionSeed
import com.localnovel.engine.model.StoryChapterSourcePacket
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.repository.StoryChapterProductionRepository
import com.localnovel.engine.support.JsonSupport
import java.security.MessageDigest
import kotlinx.serialization.encodeToString

class StoryChapterProductionService(
    private val repository: SqliteStoryRepository,
) {
    fun createHandoff(request: StoryChapterHandoffRequest): StoryChapterHandoffArtifact {
        repository.initialize()
        require(request.productionId.isNotBlank() && request.runId.isNotBlank())
        require(request.worldQueries.isNotEmpty()) { "At least one SQL world-source query is required." }
        require(request.targetChineseCharactersMin >= 1500)
        require(request.targetChineseCharactersMax >= request.targetChineseCharactersMin)
        val runtime = repository.withConnection { connection ->
            repository.queryRows(
                connection,
                """
                SELECT project_id, session_id, scenario_seed, context_id, context_hash, context_snapshot_json
                FROM reboot_run_evidence WHERE run_id = ?
                """.trimIndent(),
                request.runId,
            ).singleOrNull()
        } ?: error("Unknown SQL runtime evidence run: ${request.runId}")
        require(runtime["project_id"] == request.projectId) { "Runtime project does not match handoff request." }
        require(runtime["session_id"] == request.sessionId) { "Runtime session does not match handoff request." }

        val worldReports = request.worldQueries.map { query ->
            WorldSourceQueryService(repository).query(query.text, query.classification, query.limit)
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
