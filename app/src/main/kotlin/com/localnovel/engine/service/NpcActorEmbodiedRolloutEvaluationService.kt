package com.localnovel.engine.service

import com.localnovel.engine.model.NpcActorCalibrationPolicy
import com.localnovel.engine.model.NpcActorCalibrationScenario
import com.localnovel.engine.model.NpcActorCalibrationSuite
import com.localnovel.engine.model.NpcActorHardBlindScenario
import com.localnovel.engine.model.NpcActorHardBlindSuite
import com.localnovel.engine.model.NpcEmbodiedAnswer
import com.localnovel.engine.model.NpcEmbodiedAnswerKey
import com.localnovel.engine.model.NpcEmbodiedHumanReviewResult
import com.localnovel.engine.model.NpcEmbodiedReviewItem
import com.localnovel.engine.model.NpcEmbodiedReviewPacket
import com.localnovel.engine.model.NpcEmbodiedReviewStep
import com.localnovel.engine.model.NpcEmbodiedReviewTrajectory
import com.localnovel.engine.model.NpcEmbodiedRolloutScenario
import com.localnovel.engine.model.NpcEmbodiedRolloutSuite
import com.localnovel.engine.model.NpcEmbodiedTechnicalReport
import com.localnovel.engine.model.NpcEmbodiedTrajectory
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

data class NpcEmbodiedRolloutArtifacts(
    val report: NpcEmbodiedTechnicalReport,
    val blindReviewPacket: NpcEmbodiedReviewPacket,
    val blindReviewAnswerKey: NpcEmbodiedAnswerKey,
)

class NpcActorEmbodiedRolloutEvaluationService {
    fun evaluate(
        frozenPolicy: NpcActorCalibrationPolicy,
        suite: NpcEmbodiedRolloutSuite,
        actor02Development: NpcActorHardBlindSuite,
        actor03Development: NpcActorCalibrationSuite,
    ): NpcEmbodiedRolloutArtifacts {
        require(frozenPolicy.policyId == NpcActorCalibrationFixtureFactory.POLICY_ID) {
            "NPC-ACTOR-04 must evaluate the frozen NPC-ACTOR-03 policy."
        }
        require(frozenPolicy.revisionNumber == 1 && frozenPolicy.status == "EVALUATION_ONLY") {
            "NPC-ACTOR-04 cannot create or evaluate a second calibration revision."
        }
        require(suite.frozenPolicyId == frozenPolicy.policyId) { "Suite policy id does not match the frozen policy." }
        require(suite.status == "NON_CANON_EMBODIED_HOLDOUT_READY") { "Embodied suite status is invalid." }
        require(suite.fixtureOrigin == "MANUALLY_AUTHORED_OFFLINE_EMBODIED_FIXTURE") {
            "Embodied fixtures must remain labeled as manually authored offline material."
        }
        require(suite.scenarios.size == 4) { "NPC-ACTOR-04 requires exactly four embodied scenes." }
        require(suite.scenarios.map { it.scenarioId }.distinct().size == 4) { "Scenario ids must be unique." }
        require(suite.scenarios.map { it.actorId }.distinct().size == 4) { "Each recurring profile must appear once." }
        require(suite.scenarios.map { it.actorId }.toSet() == expectedActorIds) {
            "NPC-ACTOR-04 must evaluate each recurring profile exactly once."
        }
        require(suite.scenarios.all { it.boundary == "NON_CANON_EVALUATION" }) {
            "Embodied scenes must remain non-Canon."
        }

        val priorIds = actor02Development.scenarios.map { it.scenarioId }.toSet() +
            actor03Development.scenarios.map { it.scenarioId }.toSet()
        val priorIdOverlap = suite.scenarios.count { it.scenarioId in priorIds }
        val priorFingerprints = actor02Development.scenarios.map(::fingerprint).toSet() +
            actor03Development.scenarios.map(::fingerprint).toSet()
        val priorContentOverlap = suite.scenarios.count { fingerprint(it) in priorFingerprints }
        require(priorIdOverlap == 0 && priorContentOverlap == 0) {
            "NPC-ACTOR-04 scenes must be new relative to NPC-ACTOR-02/03."
        }

        val findings = suite.scenarios.map(::validateScenario)
        val unknownRefs = findings.sumOf { it.unknownReferenceViolations }
        val visibility = findings.sumOf { it.visibilityViolations }
        val adjacency = findings.sumOf { it.adjacencyViolations }
        val timing = findings.sumOf { it.timingViolations }
        val continuity = findings.sumOf { it.stateContinuityViolations }
        val behavior = findings.sumOf { it.requiredBehaviorViolations }
        val forbidden = findings.sumOf { it.forbiddenInformationViolations }
        val lengthRatios = suite.scenarios.map(::spokenLengthRatio)
        val lengthViolations = lengthRatios.count { it > 1.25 }

        require(unknownRefs == 0) { "Embodied trajectories contain unknown entity, zone, beat or target references." }
        require(visibility == 0) { "Embodied trajectories perceive entities outside the actor-visible scene." }
        require(adjacency == 0) { "Embodied trajectories contain non-adjacent movement." }
        require(timing == 0) { "Embodied scene clocks or beat order are invalid." }
        require(continuity == 0) { "Embodied state changes do not continue from prior state." }
        require(behavior == 0) { "Each trajectory needs action-first, silence, multi-agent reaction and environment dependence." }
        require(forbidden == 0) { "Embodied trajectories referenced author-only information." }
        require(lengthViolations == 0) { "Embodied candidate spoken lengths must remain within 25 percent." }

        val packetId = "npc-actor04-embodied-${sha256("${suite.suiteId}:${suite.randomizationSeed}").take(12)}"
        val embodiedAIds = suite.scenarios
            .sortedBy { sha256("${suite.randomizationSeed}:${it.scenarioId}") }
            .take(2)
            .map { it.scenarioId }
            .toSet()
        val reviewItems = mutableListOf<NpcEmbodiedReviewItem>()
        val answers = mutableListOf<NpcEmbodiedAnswer>()
        suite.scenarios.forEach { scenario ->
            val reviewId = "review-${scenario.scenarioId}"
            val embodiedIsA = scenario.scenarioId in embodiedAIds
            val control = toReviewTrajectory(scenario, scenario.naturalControlTrajectory)
            val embodied = toReviewTrajectory(scenario, scenario.embodiedActorTrajectory)
            reviewItems += NpcEmbodiedReviewItem(
                reviewId = reviewId,
                scenarioId = scenario.scenarioId,
                title = scenario.title,
                characterBrief = scenario.reviewerCharacterBrief,
                worldBrief = scenario.worldBrief,
                zoneMap = scenario.zones.map { zone ->
                    "${zone.label}：相鄰 ${zone.adjacentZoneIds.map { labelOf(scenario, it) }.joinToString("、")}；可見 ${zone.visibleZoneIds.map { labelOf(scenario, it) }.joinToString("、")}"
                },
                initialState = scenario.initialEntities.map { "${it.label}｜${it.entityType}｜${labelOf(scenario, it.zoneId)}｜${it.state}" },
                sharedEnvironmentBeats = scenario.environmentBeats,
                optionA = if (embodiedIsA) embodied else control,
                optionB = if (embodiedIsA) control else embodied,
                ratingDimensions = scenario.ratingDimensions,
                ratingPrompt = "請比較整段軌跡的環境感知、空間、優先順序、多人反應、角色自然度與狀態連續性；選 A、B、TIE 或 BOTH_FAIL。",
            )
            answers += NpcEmbodiedAnswer(reviewId, if (embodiedIsA) "A" else "B")
        }
        val embodiedACount = answers.count { it.embodiedActorOption == "A" }
        require(embodiedACount == 2) { "Embodied A/B identity must be balanced 2/2." }

        return NpcEmbodiedRolloutArtifacts(
            report = NpcEmbodiedTechnicalReport(
                suiteId = suite.suiteId,
                scenarioCount = suite.scenarios.size,
                profileCount = suite.scenarios.map { it.actorId }.distinct().size,
                beatCount = suite.scenarios.sumOf { it.environmentBeats.size },
                embodiedOptionACount = embodiedACount,
                embodiedOptionBCount = answers.size - embodiedACount,
                priorScenarioIdOverlapCount = priorIdOverlap,
                priorContentFingerprintOverlapCount = priorContentOverlap,
                unknownReferenceViolationCount = unknownRefs,
                visibilityViolationCount = visibility,
                adjacencyViolationCount = adjacency,
                timingViolationCount = timing,
                stateContinuityViolationCount = continuity,
                requiredBehaviorViolationCount = behavior,
                forbiddenInformationViolationCount = forbidden,
                lengthBalanceViolationCount = lengthViolations,
                maximumSpokenLengthRatio = lengthRatios.maxOrNull() ?: 1.0,
                technicalDecision = "TECHNICAL_PASS_HUMAN_REVIEW_PENDING",
                humanReviewStatus = "HUMAN_EMBODIED_TRAJECTORY_REVIEW_PENDING",
                claimBoundary = listOf(
                    "These are manually authored offline text trajectories, not live model outputs or a physics simulation.",
                    "The wider scene state tests environmental coherence but does not prove general human likeness.",
                    "No live API, production rollout, Canon promotion or SQLite state write is authorized.",
                ),
            ),
            blindReviewPacket = NpcEmbodiedReviewPacket(packetId = packetId, items = reviewItems),
            blindReviewAnswerKey = NpcEmbodiedAnswerKey(
                packetId = packetId,
                randomizationSeed = suite.randomizationSeed,
                answers = answers,
            ),
        )
    }

    fun validateHumanReviewResult(packet: NpcEmbodiedReviewPacket, result: NpcEmbodiedHumanReviewResult) {
        require(result.packetId == packet.packetId) { "Human review result packet id does not match." }
        require(result.status == "COMPLETE_BEFORE_ANSWER_KEY_DISCLOSURE") {
            "Human review result must be complete before answer-key disclosure."
        }
        require(result.reviewerRole == "PRODUCT_OWNER") { "NPC-ACTOR-04 requires Product Owner review." }
        require(!result.answerKeyOpenedBeforeCompletion) { "Answer key was opened before review completion." }
        require(result.ratings.size == packet.items.size) { "Every embodied review scene requires one rating." }
        require(result.ratings.map { it.reviewId }.distinct().size == result.ratings.size) {
            "Human review result contains duplicate review ids."
        }
        require(result.ratings.map { it.reviewId }.toSet() == packet.items.map { it.reviewId }.toSet()) {
            "Human review result ids do not match the packet."
        }
        require(result.ratings.all { it.selection in allowedSelections }) { "Human review selection is invalid." }
        require(result.ratings.all { rating ->
            rating.flaggedDimensions.distinct().size == rating.flaggedDimensions.size &&
                rating.flaggedDimensions.all { it in allowedDimensions }
        }) { "Human review dimensions are invalid." }
    }

    private fun validateScenario(scenario: NpcEmbodiedRolloutScenario): ScenarioFindings {
        val zoneIds = scenario.zones.map { it.zoneId }.toSet()
        val entityIds = scenario.initialEntities.map { it.entityId }.toSet()
        val beatIds = scenario.environmentBeats.map { it.beatId }.toSet()
        var unknown = 0
        unknown += scenario.zones.sumOf { zone ->
            (zone.adjacentZoneIds + zone.visibleZoneIds).count { it !in zoneIds }
        }
        unknown += scenario.initialEntities.count { it.zoneId !in zoneIds }
        unknown += scenario.environmentBeats.sumOf { beat -> beat.visibleEntityIdsAdded.count { it !in entityIds } }
        if (scenario.initialEntities.none { it.entityId == scenario.actorId && it.entityType == "ACTOR" }) unknown++
        if (scenario.initialEntities.count { it.entityType == "ACTOR" } < 4) unknown++
        if (scenario.initialEntities.count { it.entityType == "OBJECT" } < 4) unknown++
        if (scenario.initialEntities.count { it.entityType == "HAZARD" } < 2) unknown++
        val zonesById = scenario.zones.associateBy { it.zoneId }
        if (scenario.zones.any { zone ->
                zone.adjacentZoneIds.any { neighbor -> zonesById[neighbor]?.adjacentZoneIds?.contains(zone.zoneId) != true }
            }
        ) unknown++
        if (scenario.zones.map { it.zoneId }.distinct().size != scenario.zones.size) unknown++
        if (scenario.initialEntities.map { it.entityId }.distinct().size != scenario.initialEntities.size) unknown++
        if (scenario.environmentBeats.map { it.beatId }.distinct().size != scenario.environmentBeats.size) unknown++

        var timing = if (
            scenario.environmentBeats.size != 3 ||
            scenario.environmentBeats.zipWithNext().any { (left, right) -> right.tick <= left.tick }
        ) 1 else 0
        val control = validateTrajectory(scenario, scenario.naturalControlTrajectory, "CONCISE_CONTROL_TRAJECTORY")
        val embodied = validateTrajectory(scenario, scenario.embodiedActorTrajectory, "EMBODIED_ACTOR_TRAJECTORY")
        unknown += control.unknownReferenceViolations + embodied.unknownReferenceViolations
        timing += control.timingViolations + embodied.timingViolations
        return ScenarioFindings(
            unknownReferenceViolations = unknown,
            visibilityViolations = control.visibilityViolations + embodied.visibilityViolations,
            adjacencyViolations = control.adjacencyViolations + embodied.adjacencyViolations,
            timingViolations = timing,
            stateContinuityViolations = control.stateContinuityViolations + embodied.stateContinuityViolations,
            requiredBehaviorViolations = control.requiredBehaviorViolations + embodied.requiredBehaviorViolations,
            forbiddenInformationViolations = forbiddenInformationViolationCount(scenario),
        )
    }

    private fun validateTrajectory(
        scenario: NpcEmbodiedRolloutScenario,
        trajectory: NpcEmbodiedTrajectory,
        expectedSourceType: String,
    ): ScenarioFindings {
        val zones = scenario.zones.associateBy { it.zoneId }
        val entities = scenario.initialEntities.associate { it.entityId to MutableEntity(it.entityType, it.zoneId, it.state) }.toMutableMap()
        val beatById = scenario.environmentBeats.associateBy { it.beatId }
        var unknown = 0
        var visibility = 0
        var adjacency = 0
        var timing = 0
        var continuity = 0
        if (trajectory.sourceType != expectedSourceType) unknown++
        if (trajectory.steps.size != 3) timing++
        if (trajectory.steps.map { it.beatId } != scenario.environmentBeats.map { it.beatId }) timing++

        trajectory.steps.forEach stepLoop@{ step ->
            val beat = beatById[step.beatId]
            if (beat == null) {
                unknown++
                return@stepLoop
            }
            val actorState = entities[scenario.actorId]
            if (actorState == null) {
                unknown++
                return@stepLoop
            }
            val actorZone = zones[actorState.zoneId]
            if (actorZone == null) {
                unknown++
                return@stepLoop
            }
            val visibleZones = actorZone.visibleZoneIds.toSet() + actorZone.zoneId
            val visibleEntities = entities.filterValues { it.zoneId in visibleZones }.keys + beat.visibleEntityIdsAdded
            unknown += (step.perceivedEntityIds + step.targetEntityIds + step.stateChanges.map { it.entityId })
                .count { it !in entities }
            visibility += (step.perceivedEntityIds + step.targetEntityIds + step.stateChanges.map { it.entityId })
                .distinct()
                .count { it !in visibleEntities }
            val changedFields = mutableSetOf<String>()
            step.stateChanges.forEach changeLoop@{ change ->
                val current = entities[change.entityId]
                if (current == null) return@changeLoop
                val key = "${change.entityId}:${change.field}"
                if (!changedFields.add(key)) continuity++
                when (change.field) {
                    "zoneId" -> {
                        if (current.zoneId != change.before || change.after !in zones) continuity++
                        val fromZone = zones[current.zoneId]
                        if (fromZone == null || change.after !in fromZone.adjacentZoneIds) adjacency++
                        current.zoneId = change.after
                    }
                    "state" -> {
                        if (current.state != change.before || change.after.isBlank()) continuity++
                        current.state = change.after
                    }
                    else -> continuity++
                }
            }
        }
        var required = 0
        if (trajectory.steps.none { it.utterance.isBlank() }) required++
        if (trajectory.steps.any { it.actionDescription.isBlank() || it.stateChanges.isEmpty() || it.targetEntityIds.isEmpty() }) required++
        if (trajectory.steps.none { it.otherActorReactions.isNotEmpty() }) required++
        val environmentalIds = scenario.initialEntities.filter { it.entityType in setOf("OBJECT", "HAZARD") }.map { it.entityId }.toSet()
        if (trajectory.steps.any { step -> step.perceivedEntityIds.none { it in environmentalIds } }) required++
        return ScenarioFindings(
            unknownReferenceViolations = unknown,
            visibilityViolations = visibility,
            adjacencyViolations = adjacency,
            timingViolations = timing,
            stateContinuityViolations = continuity,
            requiredBehaviorViolations = required,
        )
    }

    private fun forbiddenInformationViolationCount(scenario: NpcEmbodiedRolloutScenario): Int {
        val forbidden = scenario.forbiddenEvidence.map { it.summary }.filter { it.isNotBlank() }
        return listOf(scenario.naturalControlTrajectory, scenario.embodiedActorTrajectory).sumOf { trajectory ->
            val text = trajectory.steps.joinToString(" ") { step ->
                "${step.chosenPriority} ${step.actionDescription} ${step.utterance} ${step.nonverbal} ${step.otherActorReactions.joinToString(" ")} ${step.resultingStateSummary}"
            }
            forbidden.count { token -> text.contains(token, ignoreCase = true) } +
                strawmanPatterns.count { token -> text.contains(token, ignoreCase = true) }
        }
    }

    private fun spokenLengthRatio(scenario: NpcEmbodiedRolloutScenario): Double {
        val control = scenario.naturalControlTrajectory.steps.sumOf { it.utterance.length }
        val embodied = scenario.embodiedActorTrajectory.steps.sumOf { it.utterance.length }
        if (control == 0 && embodied == 0) return 1.0
        if (min(control, embodied) == 0) return Double.POSITIVE_INFINITY
        return max(control, embodied).toDouble() / min(control, embodied).toDouble()
    }

    private fun toReviewTrajectory(
        scenario: NpcEmbodiedRolloutScenario,
        trajectory: NpcEmbodiedTrajectory,
    ): NpcEmbodiedReviewTrajectory = NpcEmbodiedReviewTrajectory(
        steps = trajectory.steps.map { step ->
            NpcEmbodiedReviewStep(
                beatId = step.beatId,
                perceived = step.perceivedEntityIds.map { labelOf(scenario, it) },
                chosenPriority = step.chosenPriority,
                action = step.actionDescription,
                utterance = step.utterance,
                nonverbal = step.nonverbal,
                otherActorReactions = step.otherActorReactions,
                resultingStateSummary = step.resultingStateSummary,
            )
        },
    )

    private fun labelOf(scenario: NpcEmbodiedRolloutScenario, id: String): String =
        scenario.zones.firstOrNull { it.zoneId == id }?.label
            ?: scenario.initialEntities.firstOrNull { it.entityId == id }?.label
            ?: id

    private fun fingerprint(scenario: NpcEmbodiedRolloutScenario): String = sha256(
        normalize(
            listOf(
                scenario.title,
                scenario.worldBrief,
            ).joinToString("||"),
        ),
    )

    private fun fingerprint(scenario: NpcActorHardBlindScenario): String = sha256(
        normalize(listOf(scenario.title, scenario.sceneContext).joinToString("||")),
    )

    private fun fingerprint(scenario: NpcActorCalibrationScenario): String = sha256(
        normalize(listOf(scenario.title, scenario.sceneContext).joinToString("||")),
    )

    private fun normalize(value: String): String = value.lowercase(Locale.ROOT).replace(Regex("\\s+"), "")

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte) }

    private data class MutableEntity(val entityType: String, var zoneId: String, var state: String)

    private data class ScenarioFindings(
        val unknownReferenceViolations: Int = 0,
        val visibilityViolations: Int = 0,
        val adjacencyViolations: Int = 0,
        val timingViolations: Int = 0,
        val stateContinuityViolations: Int = 0,
        val requiredBehaviorViolations: Int = 0,
        val forbiddenInformationViolations: Int = 0,
    )

    private companion object {
        val allowedSelections = setOf("A", "B", "TIE", "BOTH_FAIL")
        val expectedActorIds = setOf("npc-eval-mira", "npc-eval-yan", "npc-eval-lian", "npc-eval-ren")
        val allowedDimensions = setOf(
            "ENVIRONMENT_AWARENESS", "SPATIAL_COHERENCE", "PRIORITY_AND_TIMING",
            "SOCIAL_REALISM", "VOICE_NATURALNESS", "STATE_CONTINUITY", "KNOWLEDGE_BOUNDARY",
        )
        val strawmanPatterns = listOf(
            "身為一個", "我的人格設定", "根據我的設定", "作為一個AI", "身為一個AI", "我必須展現",
        )
    }
}
