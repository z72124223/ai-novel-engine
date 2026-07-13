package com.localnovel.engine.cli

import com.localnovel.engine.model.NpcActorEvaluationSuite
import com.localnovel.engine.model.NpcActorHardBlindSuite
import com.localnovel.engine.model.NpcActorHardBlindHumanReviewResult
import com.localnovel.engine.model.NpcActorHardBlindReviewPacket
import com.localnovel.engine.service.NpcActorEvaluationFixtureFactory
import com.localnovel.engine.service.NpcActorEvaluationService
import com.localnovel.engine.service.NpcActorHardBlindEvaluationService
import com.localnovel.engine.service.NpcActorHardBlindFixtureFactory
import com.localnovel.engine.service.NpcActorJson
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

internal object NpcActorCli {
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

    private inline fun <reified T> writeStrictJson(path: Path, value: T) {
        path.parent?.let(Files::createDirectories)
        Files.writeString(path, NpcActorJson.strict.encodeToString(value))
    }
}
