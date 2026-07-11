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
import kotlinx.serialization.json.jsonObject

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
    val fairSurprise: Double,
    val readerAttachment: Double,
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

data class RebootReplayAdoptionDecision(
    val decision: String,
    val equivalent: Boolean,
    val noRegression: Boolean,
    val retryImproved: Boolean,
    val reasons: List<String>,
)

object RebootReplayAdoptionGate {
    fun evaluate(baseline: RebootReplayMetrics, evolved: RebootReplayMetrics): RebootReplayAdoptionDecision {
        val equivalent = baseline.normalizedReplayFingerprint == evolved.normalizedReplayFingerprint
        val noRegression = evolved.legalApplyRate == baseline.legalApplyRate &&
            evolved.stallCount <= baseline.stallCount &&
            evolved.autonomyScore >= baseline.autonomyScore &&
            evolved.emotionalWaveCoverage >= baseline.emotionalWaveCoverage &&
            evolved.repetitionAiProseRisk <= baseline.repetitionAiProseRisk &&
            evolved.overallDramaScore >= baseline.overallDramaScore &&
            evolved.fairSurprise >= baseline.fairSurprise &&
            evolved.readerAttachment >= baseline.readerAttachment &&
            evolved.irreversibleChangeCount == baseline.irreversibleChangeCount &&
            evolved.elapsedWorldMinutes == baseline.elapsedWorldMinutes
        val retryImproved = evolved.retryCount < baseline.retryCount
        return RebootReplayAdoptionDecision(
            decision = if (equivalent && noRegression && retryImproved) "ADOPT" else "REJECT",
            equivalent = equivalent,
            noRegression = noRegression,
            retryImproved = retryImproved,
            reasons = listOf(
                "normalized replay fingerprint equivalent=$equivalent",
                "quality, legality, approval, and elapsed-state no-regression=$noRegression",
                "retry count ${baseline.retryCount} -> ${evolved.retryCount}",
            ),
        )
    }
}

class RebootReplayBenchmarkRunner(private val benchmarkRoot: Path) {
    fun run(basePackPath: Path, addonPackPath: Path, npcProfilePath: Path, approvalGrantPath: Path): RebootReplayBenchmarkReport {
        Files.createDirectories(benchmarkRoot)
        val baseline = execute(
            label = "baseline",
            profile = RebootVerticalSliceRuleProfile.CURRENT,
            basePackPath = basePackPath,
            addonPackPath = addonPackPath,
            npcProfilePath = npcProfilePath,
            approvalGrantPath = approvalGrantPath,
        )
        val evolved = execute(
            label = "evolved",
            profile = RebootVerticalSliceRuleProfile.PREFLIGHT_RECOGNIZED_DANGER_DRAFT,
            basePackPath = basePackPath,
            addonPackPath = addonPackPath,
            npcProfilePath = npcProfilePath,
            approvalGrantPath = approvalGrantPath,
        )
        val gate = RebootReplayAdoptionGate.evaluate(baseline, evolved)
        val report = RebootReplayBenchmarkReport(
            scenarioSeed = baseline.scenarioSeed,
            baseline = baseline,
            evolved = evolved,
            normalizedReplayEquivalent = gate.equivalent,
            adjustedRule = "Use the already validated danger-recognition draft as turn-one input before the revision loop.",
            decision = gate.decision,
            decisionReasons = gate.reasons,
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
        approvalGrantPath: Path,
    ): RebootReplayMetrics {
        val output = benchmarkRoot.resolve(label)
        Files.createDirectories(output)
        val database = output.resolve("story.db")
        Files.deleteIfExists(database)
        Files.deleteIfExists(output.resolve("narrative_learned_rules").resolve("chenxing.json"))
        val repository = SqliteStoryRepository(database)
        lateinit var summary: RebootVerticalSliceRunSummary
        val executionMillis = measureTimeMillis {
            summary = RebootVerticalSliceRunner(repository, output, profile).run(basePackPath, addonPackPath, npcProfilePath, approvalGrantPath)
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
            fairSurprise = selectedScores.averageOf { it.fairSurprise },
            readerAttachment = selectedScores.averageOf { it.readerAttachment },
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
            addAll(select(repository, "story_sessions", "project_id", "current_scene_id", "next_turn_number", "state_version", "status", "world_time", "elapsed_minutes", "reader_mode"))
            addAll(select(repository, "turn_logs", "turn_number", "player_input", "narrative", "has_state_change", "turn_summary", "validation_status"))
            addAll(repository.tableRows("state_deltas").map { row ->
                val stableTargetId = row["target_id"].orEmpty().replace(Regex("^event-npc-trace-.+"), "event-npc-trace")
                "state_deltas|${row["operation_type"]}|${row["target_type"]}|$stableTargetId|${row["payload_json"].orEmpty()}"
            })
            addAll(select(repository, "events", "summary", "payload_json"))
            addAll(select(repository, "character_states", "character_id", "location_id", "mood", "health", "inventory_json", "flags_json"))
            addAll(repository.tableRows("npc_decision_traces").map { row ->
                val trace = JsonSupport.json.parseToJsonElement(row["decision_trace_json"].orEmpty()).jsonObject
                val proposal = JsonSupport.json.parseToJsonElement(row["proposal_json"].orEmpty()).jsonObject
                val resolution = JsonSupport.json.parseToJsonElement(row["resolution_json"].orEmpty()).jsonObject
                listOf(
                    "npc_decision_traces",
                    row["turn_number"].orEmpty(),
                    row["npc_id"].orEmpty(),
                    row["scenario_seed"].orEmpty(),
                    row["status"].orEmpty(),
                    trace.text("activeGoal"),
                    trace.text("perceivedSituation"),
                    trace.text("selectedAction"),
                    trace.text("predictedCost"),
                    proposal.text("actionType"),
                    proposal.text("intent"),
                    proposal.text("visibleAction"),
                    proposal.text("hiddenMotive"),
                    proposal.text("locationId"),
                    proposal.text("observableConsequence"),
                    resolution.text("status"),
                    resolution.text("elapsedMinutes"),
                    resolution.text("observableConsequence"),
                ).joinToString("|")
            })
            addAll(select(repository, "reboot_drama_evaluations", "turn_number", "scenario_seed", "selected_draft", "scene_frame_json", "stall_status"))
            addAll(select(repository, "facts", "project_id", "content", "visibility", "source", "created_turn"))
            addAll(select(repository, "character_memories", "project_id", "owner_character_id", "character_id", "summary", "salience", "visibility", "created_turn", "source_type", "source_id", "last_recalled_turn_number", "recall_count", "status"))
            addAll(select(repository, "character_knowledge", "project_id", "character_id", "proposition_key", "statement", "epistemic_state", "confidence", "source_fact_id", "acquired_turn_number", "updated_turn_number", "visibility", "status"))
            addAll(select(repository, "memory_recall_logs", "turn_number", "character_id", "retrieval_score", "retrieval_reasons_json", "included_in_context"))
            addAll(select(repository, "scene_instances", "scene_id", "visit_number", "status", "started_turn_number", "ended_turn_number", "entry_reason", "exit_reason", "summary"))
            addAll(select(repository, "session_scene_requirement_states", "requirement_id", "status", "progress", "evidence_json", "completed_turn_number"))
            addAll(select(repository, "session_thread_states", "thread_id", "status", "progress", "urgency", "blocked_reason", "last_advanced_turn", "evidence_json"))
            addAll(select(repository, "pending_scene_transitions", "to_scene_id", "proposal_type", "reason", "evidence_json", "status", "rejection_reason", "created_turn_number", "resolved_turn_number"))
            addAll(select(repository, "scheduled_world_events", "project_id", "event_type", "due_turn_number", "due_world_time", "trigger_conditions_json", "payload_json", "visibility", "status", "triggered_turn_number", "consumed_turn_number"))
            addAll(repository.tableRows("reboot_run_evidence").map { row ->
                val contract = optionalObject(row["irreversible_change_json"])
                val approval = optionalObject(row["irreversible_approval_json"])
                listOf(
                    "reboot_run_evidence",
                    row["turn_number"].orEmpty(),
                    row["scenario_seed"].orEmpty(),
                    row["validation_classification"].orEmpty(),
                    row["state_delta_json"].orEmpty().replace(Regex("event-npc-trace-[0-9a-f-]{36}"), "event-npc-trace"),
                    row["irreversible_approval_actor"].orEmpty(),
                    row["irreversible_approval_granted"].orEmpty(),
                    row["status"].orEmpty(),
                    contract.text("changeId"),
                    contract["changeTypes"].toString(),
                    contract.text("summary"),
                    contract["affectedEntityIds"].toString(),
                    contract["requiredStateDeltaTargetIds"].toString(),
                    approval.text("approvalId"),
                    approval.text("changeId"),
                    approval.text("humanActorId"),
                    approval.text("decision"),
                    approval.text("approvedAt"),
                ).joinToString("|")
            })
        }.sorted().joinToString("\n")
        return MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun select(repository: SqliteStoryRepository, table: String, vararg columns: String): List<String> =
        repository.tableRows(table).map { row -> "$table|" + columns.joinToString("|") { row[it].orEmpty() } }
}

private fun optionalObject(value: String?): kotlinx.serialization.json.JsonObject =
    value?.takeIf { it.isNotBlank() }?.let { JsonSupport.json.parseToJsonElement(it).jsonObject } ?: kotlinx.serialization.json.JsonObject(emptyMap())

private fun kotlinx.serialization.json.JsonObject.text(key: String): String = this[key]?.toString().orEmpty()

private inline fun <T> Iterable<T>.averageOf(selector: (T) -> Int): Double {
    val values = map(selector)
    return if (values.isEmpty()) 0.0 else values.average()
}
