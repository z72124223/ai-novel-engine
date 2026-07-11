package com.localnovel.engine.service

import com.localnovel.engine.model.DramaCriticScores
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.system.measureTimeMillis
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

@Serializable
data class RebootReplayMetrics(
    val profile: String,
    val scenarioSeed: String,
    val legalApplyRate: Double,
    val stallCount: Int,
    val autonomyScore: Int,
    val emotionalWaveCoverage: Double,
    val irreversibleChangeCount: Int,
    val repetitionAiProseRisk: Double,
    val overallDramaScore: Double,
    val elapsedWorldMinutes: Int,
    val executionMillis: Long,
    val model: String,
    val reasoning: String,
    val retryCount: Int,
    val normalizedReplayFingerprint: String,
)

@Serializable
data class RebootReplayBenchmarkReport(
    val scenarioSeed: String,
    val baseline: RebootReplayMetrics,
    val evolved: RebootReplayMetrics,
    val normalizedReplayEquivalent: Boolean,
    val adjustedRule: String,
    val decision: String,
    val decisionReasons: List<String>,
)

class RebootReplayBenchmarkRunner(private val benchmarkRoot: Path) {
    fun run(basePackPath: Path, addonPackPath: Path, npcProfilePath: Path): RebootReplayBenchmarkReport {
        Files.createDirectories(benchmarkRoot)
        val baseline = execute(
            label = "baseline",
            profile = RebootVerticalSliceRuleProfile.CURRENT,
            basePackPath = basePackPath,
            addonPackPath = addonPackPath,
            npcProfilePath = npcProfilePath,
        )
        val evolved = execute(
            label = "evolved",
            profile = RebootVerticalSliceRuleProfile.PREFLIGHT_RECOGNIZED_DANGER_DRAFT,
            basePackPath = basePackPath,
            addonPackPath = addonPackPath,
            npcProfilePath = npcProfilePath,
        )
        val equivalent = baseline.normalizedReplayFingerprint == evolved.normalizedReplayFingerprint
        val noRegression = evolved.legalApplyRate >= baseline.legalApplyRate &&
            evolved.stallCount <= baseline.stallCount &&
            evolved.autonomyScore >= baseline.autonomyScore &&
            evolved.emotionalWaveCoverage >= baseline.emotionalWaveCoverage &&
            evolved.repetitionAiProseRisk <= baseline.repetitionAiProseRisk &&
            evolved.overallDramaScore >= baseline.overallDramaScore
        val retryImproved = evolved.retryCount < baseline.retryCount
        val decision = if (equivalent && noRegression && retryImproved) "ADOPT" else "REJECT"
        val report = RebootReplayBenchmarkReport(
            scenarioSeed = baseline.scenarioSeed,
            baseline = baseline,
            evolved = evolved,
            normalizedReplayEquivalent = equivalent,
            adjustedRule = "Use the already validated danger-recognition draft as turn-one input before the revision loop.",
            decision = decision,
            decisionReasons = buildList {
                add("normalized replay fingerprint equivalent=$equivalent")
                add("quality and legality no-regression=$noRegression")
                add("retry count ${baseline.retryCount} -> ${evolved.retryCount}")
            },
        )
        Files.writeString(benchmarkRoot.resolve("benchmark-summary.json"), JsonSupport.json.encodeToString(report))
        return report
    }

    private fun execute(
        label: String,
        profile: RebootVerticalSliceRuleProfile,
        basePackPath: Path,
        addonPackPath: Path,
        npcProfilePath: Path,
    ): RebootReplayMetrics {
        val output = benchmarkRoot.resolve(label)
        Files.createDirectories(output)
        val database = output.resolve("story.db")
        Files.deleteIfExists(database)
        Files.deleteIfExists(output.resolve("narrative_learned_rules").resolve("chenxing.json"))
        val repository = SqliteStoryRepository(database)
        lateinit var summary: RebootVerticalSliceRunSummary
        val executionMillis = measureTimeMillis {
            summary = RebootVerticalSliceRunner(repository, output, profile).run(basePackPath, addonPackPath, npcProfilePath)
        }
        val evidence = repository.tableRows("reboot_run_evidence")
        val dramaRows = repository.tableRows("reboot_drama_evaluations")
        val selectedScores = dramaRows.map { row ->
            val json = if (row["revision_count"] == "1") row["revised_scores_json"].orEmpty() else row["raw_scores_json"].orEmpty()
            JsonSupport.json.decodeFromString<DramaCriticScores>(json)
        }
        return RebootReplayMetrics(
            profile = profile.name,
            scenarioSeed = summary.scenarioSeed,
            legalApplyRate = evidence.count { it["status"] == "APPLIED" }.toDouble() / evidence.size.coerceAtLeast(1),
            stallCount = dramaRows.count { it["stall_status"] == "STALL_DETECTED" },
            autonomyScore = if (repository.tableRows("npc_decision_traces").all { it["status"] == "APPLIED" }) 100 else 0,
            emotionalWaveCoverage = selectedScores.averageOf { it.emotionalWaveCoverage },
            irreversibleChangeCount = evidence.count { it["irreversible_approval_granted"] == "1" },
            repetitionAiProseRisk = selectedScores.averageOf { it.cheapTwistRisk },
            overallDramaScore = selectedScores.averageOf { it.overall },
            elapsedWorldMinutes = summary.elapsedMinutes,
            executionMillis = executionMillis,
            model = "deterministic-local",
            reasoning = "none",
            retryCount = dramaRows.sumOf { it["revision_count"].orEmpty().toInt() },
            normalizedReplayFingerprint = normalizedFingerprint(repository),
        )
    }

    private fun normalizedFingerprint(repository: SqliteStoryRepository): String {
        val canonical = buildList {
            addAll(select(repository, "turn_logs", "turn_number", "player_input", "narrative", "has_state_change", "turn_summary", "validation_status"))
            addAll(repository.tableRows("state_deltas").map { row ->
                val stableTargetId = row["target_id"].orEmpty().replace(Regex("^event-npc-trace-.+"), "event-npc-trace")
                "state_deltas|${row["operation_type"]}|${row["target_type"]}|$stableTargetId|${row["payload_json"].orEmpty()}"
            })
            addAll(select(repository, "events", "summary", "payload_json"))
            addAll(select(repository, "character_states", "character_id", "location_id", "mood", "health", "inventory_json", "flags_json"))
            addAll(select(repository, "npc_decision_traces", "turn_number", "npc_id", "scenario_seed", "status"))
            addAll(select(repository, "reboot_drama_evaluations", "turn_number", "scenario_seed", "selected_draft", "scene_frame_json", "stall_status"))
        }.sorted().joinToString("\n")
        return MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun select(repository: SqliteStoryRepository, table: String, vararg columns: String): List<String> =
        repository.tableRows(table).map { row -> "$table|" + columns.joinToString("|") { row[it].orEmpty() } }
}

private inline fun <T> Iterable<T>.averageOf(selector: (T) -> Int): Double {
    val values = map(selector)
    return if (values.isEmpty()) 0.0 else values.average()
}
