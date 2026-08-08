package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.NarrativeQualityFinding
import com.localnovel.engine.model.ComicVideoVoiceTimelineFill
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

@Serializable
data class AudioNovelPublishAuditReport(
    val schemaVersion: String = "audio-novel-publish-audit-v1",
    val sessionId: String = "",
    val fromTurn: Int = 0,
    val toTurn: Int = 0,
    val verdict: String = "PASS",
    val majorFindingCount: Int = 0,
    val warningFindingCount: Int = 0,
    val turnReports: List<AudioNovelPublishTurnAudit> = emptyList(),
    val subtitleFindings: List<NarrativeQualityFinding> = emptyList(),
    val notes: List<String> = emptyList(),
)

@Serializable
data class AudioNovelPublishTurnAudit(
    val turnNumber: Int,
    val turnId: String = "",
    val verdict: String = "PASS",
    val findings: List<NarrativeQualityFinding> = emptyList(),
)

class AudioNovelPublishAuditService {
    fun audit(
        repository: SqliteStoryRepository,
        sessionId: String,
        fromTurn: Int = 1,
        toTurn: Int = Int.MAX_VALUE,
        episodeDir: Path? = null,
        voiceTimeline: ComicVideoVoiceTimelineFill? = null,
    ): AudioNovelPublishAuditReport {
        val auditService = NarrativeQualityAuditService()
        val subtitleAuditService = AudioNovelSubtitleAuditService()
        val turnLogs = repository.tableRows("turn_logs")
            .filter {
                it["session_id"] == sessionId &&
                    it["validation_status"] == "applied" &&
                    (it["turn_number"]?.toIntOrNull() ?: 0) in fromTurn..toTurn
            }
            .sortedBy { it["turn_number"]?.toIntOrNull() ?: Int.MAX_VALUE }

        val turnReports = turnLogs.map { row ->
            val turnNumber = row["turn_number"]?.toIntOrNull() ?: 0
            val context = JsonSupport.json.decodeFromString<ContextPacket>(row["context_json"].orEmpty())
            val result = JsonSupport.json.decodeFromString<AiTurnResult>(row["result_json"].orEmpty())
            val input = NarrativeQualityAuditInput(
                contextFile = "turn_logs:$sessionId:$turnNumber:context",
                resultFile = "turn_logs:$sessionId:$turnNumber:result",
            )
            val findings = auditService.audit(context, result, input).findings
            val major = findings.count { it.severity == "MAJOR" }
            AudioNovelPublishTurnAudit(
                turnNumber = turnNumber,
                turnId = result.turnId,
                verdict = if (major > 0) "FAIL" else "PASS",
                findings = findings,
            )
        }

        val subtitleFindings = buildList {
            voiceTimeline?.let { timeline ->
                addAll(subtitleAuditService.auditVoiceTimeline(timeline))
            }
            episodeDir?.let { dir ->
                val srtPath = dir.resolve("episode_subtitles.srt")
                if (Files.exists(srtPath)) {
                    addAll(subtitleAuditService.auditSrt(srtPath))
                }
            }
        }.distinctBy { "${it.ruleId}:${it.affectedFileOrTurn}:${it.evidence.joinToString("|")}" }

        val narrativeMajor = turnReports.sumOf { report -> report.findings.count { it.severity == "MAJOR" } }
        val subtitleMajor = subtitleFindings.count { it.severity == "MAJOR" }
        val majorCount = narrativeMajor + subtitleMajor
        val warningCount = turnReports.sumOf { report -> report.findings.count { it.severity == "WARNING" } } +
            subtitleFindings.count { it.severity == "WARNING" }
        return AudioNovelPublishAuditReport(
            sessionId = sessionId,
            fromTurn = turnReports.firstOrNull()?.turnNumber ?: fromTurn,
            toTurn = turnReports.lastOrNull()?.turnNumber ?: toTurn,
            verdict = if (majorCount > 0) "FAIL" else "PASS",
            majorFindingCount = majorCount,
            warningFindingCount = warningCount,
            turnReports = turnReports,
            subtitleFindings = subtitleFindings,
            notes = listOf(
                "Run before muxing/publishing audio-novel MP4.",
                "FAIL blocks YouTube-ready export until narratives are corrected and turns re-resolved.",
                "Subtitle rules: SUBTITLE_DWELL_TOO_LONG, SUBTITLE_GAP_TOO_LONG, SUBTITLE_DOUBLE_TURN_GAP, SUBTITLE_BROKEN_EM_DASH, SUBTITLE_ORPHAN_PUNCTUATION, SIMPLIFIED_CHINESE_IN_PUBLISH_TEXT.",
            ),
        )
    }
}