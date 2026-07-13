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
import com.localnovel.engine.service.NpcActorEvaluationFixtureFactory
import com.localnovel.engine.service.NpcActorEvaluationService
import com.localnovel.engine.service.NpcActorCalibrationEvaluationService
import com.localnovel.engine.service.NpcActorCalibrationFixtureFactory
import com.localnovel.engine.service.NpcActorHardBlindEvaluationService
import com.localnovel.engine.service.NpcActorHardBlindFixtureFactory
import com.localnovel.engine.service.NpcActorJson
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

internal object NpcActorCli {
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
