package com.localnovel.engine.cli

import com.localnovel.engine.model.NpcActorEvaluationSuite
import com.localnovel.engine.model.NpcActorCalibrationHumanReviewResult
import com.localnovel.engine.model.NpcActorCalibrationPolicy
import com.localnovel.engine.model.NpcActorCalibrationReviewPacket
import com.localnovel.engine.model.NpcActorCalibrationSuite
import com.localnovel.engine.model.NpcActorHardBlindSuite
import com.localnovel.engine.model.NpcActorHardBlindHumanReviewResult
import com.localnovel.engine.model.NpcActorHardBlindAnswerKey
import com.localnovel.engine.model.NpcActorHardBlindReviewPacket
import com.localnovel.engine.model.NpcEmbodiedHumanReviewResult
import com.localnovel.engine.model.NpcEmbodiedAnswerKey
import com.localnovel.engine.model.NpcEmbodiedInternalReviewResult
import com.localnovel.engine.model.NpcEmbodiedReviewPacket
import com.localnovel.engine.model.NpcEmbodiedRolloutSuite
import com.localnovel.engine.service.NpcActorEvaluationFixtureFactory
import com.localnovel.engine.service.NpcActorEvaluationService
import com.localnovel.engine.service.NpcActorCalibrationEvaluationService
import com.localnovel.engine.service.NpcActorCalibrationFixtureFactory
import com.localnovel.engine.service.NpcActorHardBlindEvaluationService
import com.localnovel.engine.service.NpcActorHardBlindFixtureFactory
import com.localnovel.engine.service.NpcActorEmbodiedRolloutEvaluationService
import com.localnovel.engine.service.NpcActorEmbodiedRolloutFixtureFactory
import com.localnovel.engine.service.NpcActorInternalReviewService
import com.localnovel.engine.service.NpcActorJson
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

internal object NpcActorCli {
    fun createEmbodiedRolloutSuite(options: Map<String, String>): Int {
        val policy = NpcActorJson.strict.decodeFromString<NpcActorCalibrationPolicy>(
            Files.readString(Path.of(options.requiredCliOption("policy"))).removePrefix("\uFEFF"),
        )
        require(policy.policyId == NpcActorCalibrationFixtureFactory.POLICY_ID && policy.revisionNumber == 1) {
            "NPC-ACTOR-04 requires the frozen revision-1 calibration policy."
        }
        val output = Path.of(options.requiredCliOption("out"))
        writeStrictJson(output, NpcActorEmbodiedRolloutFixtureFactory.create())
        println("scenarios=4")
        println("profiles=4")
        println("beats=12")
        println("boundary=NON_CANON_EVALUATION")
        println("fixtureOrigin=MANUALLY_AUTHORED_OFFLINE_EMBODIED_FIXTURE")
        println("wrote=$output")
        return 0
    }

    fun evaluateEmbodiedRollout(options: Map<String, String>): Int {
        val policy = NpcActorJson.strict.decodeFromString<NpcActorCalibrationPolicy>(
            Files.readString(Path.of(options.requiredCliOption("policy"))).removePrefix("\uFEFF"),
        )
        val suite = NpcActorJson.strict.decodeFromString<NpcEmbodiedRolloutSuite>(
            Files.readString(Path.of(options.requiredCliOption("suite"))).removePrefix("\uFEFF"),
        )
        val actor02 = NpcActorJson.strict.decodeFromString<NpcActorHardBlindSuite>(
            Files.readString(Path.of(options.requiredCliOption("actor02-suite"))).removePrefix("\uFEFF"),
        )
        val actor03 = NpcActorJson.strict.decodeFromString<NpcActorCalibrationSuite>(
            Files.readString(Path.of(options.requiredCliOption("actor03-suite"))).removePrefix("\uFEFF"),
        )
        val artifacts = NpcActorEmbodiedRolloutEvaluationService().evaluate(policy, suite, actor02, actor03)
        val report = Path.of(options.requiredCliOption("report"))
        val blind = Path.of(options.requiredCliOption("blind"))
        val answerKey = Path.of(options.requiredCliOption("answer-key"))
        writeStrictJson(report, artifacts.report)
        writeStrictJson(blind, artifacts.blindReviewPacket)
        writeStrictJson(answerKey, artifacts.blindReviewAnswerKey)
        println("technicalDecision=${artifacts.report.technicalDecision}")
        println("humanReviewStatus=${artifacts.report.humanReviewStatus}")
        println("scenarioCount=${artifacts.report.scenarioCount}")
        println("beatCount=${artifacts.report.beatCount}")
        println("embodiedOptionBalance=${artifacts.report.embodiedOptionACount}/${artifacts.report.embodiedOptionBCount}")
        println("report=$report")
        println("blind=$blind")
        println("answerKey=$answerKey")
        return if (artifacts.report.technicalDecision == "TECHNICAL_PASS_HUMAN_REVIEW_PENDING") 0 else 1
    }

    fun validateEmbodiedHumanReview(options: Map<String, String>): Int {
        val packet = NpcActorJson.strict.decodeFromString<NpcEmbodiedReviewPacket>(
            Files.readString(Path.of(options.requiredCliOption("packet"))).removePrefix("\uFEFF"),
        )
        val result = NpcActorJson.strict.decodeFromString<NpcEmbodiedHumanReviewResult>(
            Files.readString(Path.of(options.requiredCliOption("result"))).removePrefix("\uFEFF"),
        )
        NpcActorEmbodiedRolloutEvaluationService().validateHumanReviewResult(packet, result)
        println("humanReviewValidation=PASS")
        println("packetId=${packet.packetId}")
        println("ratings=${result.ratings.size}")
        return 0
    }

    fun aggregateEmbodiedInternalReview(options: Map<String, String>): Int {
        val packet = NpcActorJson.strict.decodeFromString<NpcEmbodiedReviewPacket>(
            Files.readString(Path.of(options.requiredCliOption("packet"))).removePrefix("\uFEFF"),
        )
        val reviews = readInternalReviews(options)
        val answerKey = NpcActorJson.strict.decodeFromString<NpcEmbodiedAnswerKey>(
            Files.readString(Path.of(options.requiredCliOption("answer-key"))).removePrefix("\uFEFF"),
        )
        val aggregate = NpcActorInternalReviewService().aggregate(packet, reviews, answerKey)
        val output = Path.of(options.requiredCliOption("out"))
        writeStrictJson(output, aggregate)
        println("internalReviewDecision=${aggregate.overallDecision}")
        println("actorWins=${aggregate.actorWins}")
        println("controlWins=${aggregate.controlWins}")
        println("ties=${aggregate.ties}")
        println("bothFail=${aggregate.bothFail}")
        println("noConsensus=${aggregate.noConsensus}")
        println("wrote=$output")
        return 0
    }

    fun validateEmbodiedInternalReviews(options: Map<String, String>): Int {
        val packet = NpcActorJson.strict.decodeFromString<NpcEmbodiedReviewPacket>(
            Files.readString(Path.of(options.requiredCliOption("packet"))).removePrefix("\uFEFF"),
        )
        val reviews = readInternalReviews(options)
        NpcActorInternalReviewService().validateReviews(packet, reviews)
        println("internalReviewValidation=PASS")
        println("packetId=${packet.packetId}")
        println("reviewers=${reviews.size}")
        println("revisionNumber=${reviews.first().revisionNumber}")
        return 0
    }

    private fun readInternalReviews(options: Map<String, String>): List<NpcEmbodiedInternalReviewResult> =
        listOf("environment-review", "dialogue-review", "adversarial-review").map { option ->
            NpcActorJson.strict.decodeFromString(
                Files.readString(Path.of(options.requiredCliOption(option))).removePrefix("\uFEFF"),
            )
        }

    fun createCalibrationPolicy(options: Map<String, String>): Int {
        val output = Path.of(options.requiredCliOption("out"))
        writeStrictJson(output, NpcActorCalibrationFixtureFactory.createPolicy())
        println("policyId=${NpcActorCalibrationFixtureFactory.POLICY_ID}")
        println("revisionNumber=1")
        println("status=EVALUATION_ONLY")
        println("wrote=$output")
        return 0
    }

    fun createCalibrationSuite(options: Map<String, String>): Int {
        val policyPath = Path.of(options.requiredCliOption("policy"))
        val policy = NpcActorJson.strict.decodeFromString<NpcActorCalibrationPolicy>(
            Files.readString(policyPath).removePrefix("\uFEFF"),
        )
        val output = Path.of(options.requiredCliOption("out"))
        writeStrictJson(output, NpcActorCalibrationFixtureFactory.createSuite(policy.policyId))
        println("scenarios=8")
        println("profiles=4")
        println("boundary=NON_CANON_EVALUATION")
        println("fixtureOrigin=MANUALLY_AUTHORED_OFFLINE_HOLDOUT_FIXTURE")
        println("wrote=$output")
        return 0
    }

    fun evaluateCalibration(options: Map<String, String>): Int {
        val policy = NpcActorJson.strict.decodeFromString<NpcActorCalibrationPolicy>(
            Files.readString(Path.of(options.requiredCliOption("policy"))).removePrefix("\uFEFF"),
        )
        val suite = NpcActorJson.strict.decodeFromString<NpcActorCalibrationSuite>(
            Files.readString(Path.of(options.requiredCliOption("suite"))).removePrefix("\uFEFF"),
        )
        val developmentSuite = NpcActorJson.strict.decodeFromString<NpcActorHardBlindSuite>(
            Files.readString(Path.of(options.requiredCliOption("development-suite"))).removePrefix("\uFEFF"),
        )
        val artifacts = NpcActorCalibrationEvaluationService().evaluate(policy, suite, developmentSuite)
        val report = Path.of(options.requiredCliOption("report"))
        val blind = Path.of(options.requiredCliOption("blind"))
        val answerKey = Path.of(options.requiredCliOption("answer-key"))
        writeStrictJson(report, artifacts.report)
        writeStrictJson(blind, artifacts.blindReviewPacket)
        writeStrictJson(answerKey, artifacts.blindReviewAnswerKey)
        println("technicalDecision=${artifacts.report.technicalDecision}")
        println("humanBlindReviewStatus=${artifacts.report.humanBlindReviewStatus}")
        println("scenarioCount=${artifacts.report.scenarioCount}")
        println("profileCount=${artifacts.report.profileCount}")
        println("adaptiveOptionBalance=${artifacts.report.adaptiveOptionACount}/${artifacts.report.adaptiveOptionBCount}")
        println("report=$report")
        println("blind=$blind")
        println("answerKey=$answerKey")
        return if (artifacts.report.technicalDecision == "TECHNICAL_PASS_HUMAN_REVIEW_PENDING") 0 else 1
    }

    fun validateCalibrationHumanReview(options: Map<String, String>): Int {
        val packet = NpcActorJson.strict.decodeFromString<NpcActorCalibrationReviewPacket>(
            Files.readString(Path.of(options.requiredCliOption("packet"))).removePrefix("\uFEFF"),
        )
        val result = NpcActorJson.strict.decodeFromString<NpcActorCalibrationHumanReviewResult>(
            Files.readString(Path.of(options.requiredCliOption("result"))).removePrefix("\uFEFF"),
        )
        NpcActorCalibrationEvaluationService().validateHumanReviewResult(packet, result)
        println("humanReviewValidation=PASS")
        println("packetId=${packet.packetId}")
        println("ratings=${result.ratings.size}")
        return 0
    }

    fun createEvaluationSuite(options: Map<String, String>): Int {
        val output = Path.of(options.requiredCliOption("out"))
        writeStrictJson(output, NpcActorEvaluationFixtureFactory.create())
        println("scenarios=20")
        println("boundary=NON_CANON_EVALUATION")
        println("wrote=$output")
        return 0
    }

    fun evaluate(options: Map<String, String>): Int {
        val suitePath = Path.of(options.requiredCliOption("suite"))
        val suite = NpcActorJson.strict.decodeFromString<NpcActorEvaluationSuite>(
            Files.readString(suitePath).removePrefix("\uFEFF"),
        )
        val artifacts = NpcActorEvaluationService().evaluate(suite)
        val report = Path.of(options.requiredCliOption("report"))
        val blind = Path.of(options.requiredCliOption("blind"))
        val answerKey = Path.of(options.requiredCliOption("answer-key"))
        writeStrictJson(report, artifacts.report)
        writeStrictJson(blind, artifacts.blindReviewPacket)
        writeStrictJson(answerKey, artifacts.blindReviewAnswerKey)
        println("technicalDecision=" + artifacts.report.technicalDecision)
        println("humanBlindReviewStatus=" + artifacts.report.humanBlindReviewStatus)
        println("scenarioCount=" + artifacts.report.scenarioCount)
        println("report=$report")
        println("blind=$blind")
        println("answerKey=$answerKey")
        return if (artifacts.report.technicalDecision == "TECHNICAL_PASS_HUMAN_REVIEW_PENDING") 0 else 1
    }

    fun createHardBlindSuite(options: Map<String, String>): Int {
        val output = Path.of(options.requiredCliOption("out"))
        writeStrictJson(output, NpcActorHardBlindFixtureFactory.create())
        println("scenarios=12")
        println("profiles=4")
        println("boundary=NON_CANON_EVALUATION")
        println("fixtureOrigin=MANUALLY_AUTHORED_OFFLINE_EVALUATION_FIXTURE")
        println("wrote=$output")
        return 0
    }

    fun evaluateHardBlind(options: Map<String, String>): Int {
        val suitePath = Path.of(options.requiredCliOption("suite"))
        val suite = NpcActorJson.strict.decodeFromString<NpcActorHardBlindSuite>(
            Files.readString(suitePath).removePrefix("\uFEFF"),
        )
        val artifacts = NpcActorHardBlindEvaluationService().evaluate(suite)
        val report = Path.of(options.requiredCliOption("report"))
        val blind = Path.of(options.requiredCliOption("blind"))
        val answerKey = Path.of(options.requiredCliOption("answer-key"))
        writeStrictJson(report, artifacts.report)
        writeStrictJson(blind, artifacts.blindReviewPacket)
        writeStrictJson(answerKey, artifacts.blindReviewAnswerKey)
        println("technicalDecision=" + artifacts.report.technicalDecision)
        println("humanBlindReviewStatus=" + artifacts.report.humanBlindReviewStatus)
        println("scenarioCount=" + artifacts.report.scenarioCount)
        println("profileCount=" + artifacts.report.profileCount)
        println("actorOptionBalance=" + artifacts.report.actorOptionACount + "/" + artifacts.report.actorOptionBCount)
        println("report=$report")
        println("blind=$blind")
        println("answerKey=$answerKey")
        return if (artifacts.report.technicalDecision == "TECHNICAL_PASS_HUMAN_REVIEW_PENDING") 0 else 1
    }

    fun validateHardBlindHumanReview(options: Map<String, String>): Int {
        val packet = NpcActorJson.strict.decodeFromString<NpcActorHardBlindReviewPacket>(
            Files.readString(Path.of(options.requiredCliOption("packet"))).removePrefix("\uFEFF"),
        )
        val result = NpcActorJson.strict.decodeFromString<NpcActorHardBlindHumanReviewResult>(
            Files.readString(Path.of(options.requiredCliOption("result"))).removePrefix("\uFEFF"),
        )
        NpcActorHardBlindEvaluationService().validateHumanReviewResult(packet, result)
        println("humanReviewValidation=PASS")
        println("packetId=${packet.packetId}")
        println("ratings=${result.ratings.size}")
        return 0
    }

    fun revealHardBlindHumanReview(options: Map<String, String>): Int {
        val packet = NpcActorJson.strict.decodeFromString<NpcActorHardBlindReviewPacket>(
            Files.readString(Path.of(options.requiredCliOption("packet"))).removePrefix("\uFEFF"),
        )
        val result = NpcActorJson.strict.decodeFromString<NpcActorHardBlindHumanReviewResult>(
            Files.readString(Path.of(options.requiredCliOption("result"))).removePrefix("\uFEFF"),
        )
        val answerKey = NpcActorJson.strict.decodeFromString<NpcActorHardBlindAnswerKey>(
            Files.readString(Path.of(options.requiredCliOption("answer-key"))).removePrefix("\uFEFF"),
        )
        val summary = NpcActorHardBlindEvaluationService().revealHumanReviewResult(packet, answerKey, result)
        val output = Path.of(options.requiredCliOption("out"))
        writeStrictJson(output, summary)
        println("overallDecision=${summary.overallDecision}")
        println("actorPreferred=${summary.actorPreferredCount}")
        println("controlPreferred=${summary.controlPreferredCount}")
        println("tie=${summary.tieCount}")
        println("bothFail=${summary.bothFailCount}")
        println("wrote=$output")
        return 0
    }

    private inline fun <reified T> writeStrictJson(path: Path, value: T) {
        path.parent?.let(Files::createDirectories)
        Files.writeString(path, NpcActorJson.strict.encodeToString(value))
    }
}
