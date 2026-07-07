package com.localnovel.engine.cli

import com.localnovel.engine.cli.readCliJson
import com.localnovel.engine.cli.requiredCliOption
import com.localnovel.engine.cli.writeCliJson
import com.localnovel.engine.model.AssetCandidateBrief
import com.localnovel.engine.model.AssetCandidateDraft
import com.localnovel.engine.model.AssetCandidateDraftValidationReport
import com.localnovel.engine.model.AssetCandidateHumanReviewDecision
import com.localnovel.engine.model.AssetCandidateHumanReviewDecisionIntakeReport
import com.localnovel.engine.model.AssetCandidatePreflightReport
import com.localnovel.engine.model.AssetCandidateReviewPreflightReport
import com.localnovel.engine.model.CandidateReferenceAcceptancePacket
import com.localnovel.engine.model.CandidateReferenceHumanAcceptanceDecision
import com.localnovel.engine.model.CandidateReferenceHumanAcceptanceDecisionIntakeReport
import com.localnovel.engine.model.CandidateReferenceRegistryHumanAcceptanceDecision
import com.localnovel.engine.model.CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport
import com.localnovel.engine.model.CandidateReferenceRegistryPacket
import com.localnovel.engine.model.CandidateReferenceRegistryPacketValidationReport
import com.localnovel.engine.model.CandidateReferenceRegistryPreflightReport
import com.localnovel.engine.model.CharacterNuwaCard
import com.localnovel.engine.model.CharacterNuwaGateFile
import com.localnovel.engine.model.CharacterNuwaRequiredSpeaker
import com.localnovel.engine.model.CharacterNuwaSampleLines
import com.localnovel.engine.model.ImageCandidateBrief
import com.localnovel.engine.model.ImageCandidateExternalResult
import com.localnovel.engine.model.ImageCandidateExternalResultIntakeReport
import com.localnovel.engine.model.ImageCandidatePreflightReport
import com.localnovel.engine.model.ImageCandidatePromptPreparation
import com.localnovel.engine.model.ImageCandidatePromptValidationReport
import com.localnovel.engine.model.ImageCandidateProviderHandoffDecision
import com.localnovel.engine.model.ImageCandidateProviderHandoffDecisionIntakeReport
import com.localnovel.engine.model.ImageCandidateProviderHandoffPacket
import com.localnovel.engine.model.ImageCandidateProviderHandoffPacketValidationReport
import com.localnovel.engine.model.ImageCandidateProviderHandoffPreflightReport
import com.localnovel.engine.model.ImageCandidateReferencePacket
import com.localnovel.engine.model.ImageCandidateReferencePacketValidationReport
import com.localnovel.engine.model.ImageCandidateResultReviewDecision
import com.localnovel.engine.model.ImageCandidateResultReviewDecisionIntakeReport
import com.localnovel.engine.model.ImageCandidateResultReviewPreflightReport
import com.localnovel.engine.model.StoryboardPacketHumanReviewDecision
import com.localnovel.engine.model.StoryboardPacketHumanReviewDecisionIntakeReport
import com.localnovel.engine.model.StoryboardPacketReviewPacket
import com.localnovel.engine.service.AssetCandidateBriefService
import com.localnovel.engine.service.AssetCandidateDraftService
import com.localnovel.engine.service.AssetCandidateDraftValidationService
import com.localnovel.engine.service.AssetCandidateHumanReviewDecisionIntakeService
import com.localnovel.engine.service.AssetCandidatePreflightService
import com.localnovel.engine.service.AssetCandidateReviewPreflightService
import com.localnovel.engine.service.CandidateReferenceAcceptancePacketService
import com.localnovel.engine.service.CandidateReferenceAcceptancePacketValidationService
import com.localnovel.engine.service.CandidateReferenceHumanAcceptanceDecisionIntakeService
import com.localnovel.engine.service.CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService
import com.localnovel.engine.service.CandidateReferenceRegistryPacketService
import com.localnovel.engine.service.CandidateReferenceRegistryPacketValidationService
import com.localnovel.engine.service.CandidateReferenceRegistryPreflightService
import com.localnovel.engine.service.CharacterNuwaGateValidationService
import com.localnovel.engine.service.ImageCandidateBriefService
import com.localnovel.engine.service.ImageCandidateExternalResultIntakeService
import com.localnovel.engine.service.ImageCandidatePreflightService
import com.localnovel.engine.service.ImageCandidatePromptPreparationService
import com.localnovel.engine.service.ImageCandidatePromptValidationService
import com.localnovel.engine.service.ImageCandidateProviderHandoffDecisionIntakeService
import com.localnovel.engine.service.ImageCandidateProviderHandoffPacketService
import com.localnovel.engine.service.ImageCandidateProviderHandoffPacketValidationService
import com.localnovel.engine.service.ImageCandidateProviderHandoffPreflightService
import com.localnovel.engine.service.ImageCandidateReferencePacketService
import com.localnovel.engine.service.ImageCandidateReferencePacketValidationService
import com.localnovel.engine.service.ImageCandidateResultHumanReviewDecisionIntakeService
import com.localnovel.engine.service.ImageCandidateResultReviewPreflightService
import java.nio.file.Path

internal object AssetImageCandidateCli {
    fun validateAssetCandidatePreflight(options: Map<String, String>): Int {
        val intakePath = Path.of(options.requiredCliOption("human-review-decision-intake"))
        val reviewPath = Path.of(options.requiredCliOption("storyboard-packet-review"))
        val report = AssetCandidatePreflightService().validate(
            humanReviewDecisionIntake = readCliJson<StoryboardPacketHumanReviewDecisionIntakeReport>(intakePath),
            storyboardPacketReview = readCliJson<StoryboardPacketReviewPacket>(reviewPath),
            preflightId = options["preflight"] ?: "",
            humanReviewDecisionIntakeFile = intakePath.toString(),
            storyboardPacketReviewFile = reviewPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("decisionId=${report.decisionId}")
        println("humanReviewPreflightId=${report.humanReviewPreflightId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("assetCandidateRequirements=${report.evidence.assetCandidateRequirementCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun createAssetCandidateBrief(options: Map<String, String>): Int {
        val preflightPath = Path.of(options.requiredCliOption("asset-candidate-preflight"))
        val brief = AssetCandidateBriefService().create(
            assetCandidatePreflight = readCliJson<AssetCandidatePreflightReport>(preflightPath),
            briefId = options["brief"] ?: "",
            assetCandidatePreflightFile = preflightPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), brief) }
        println("verdict=${brief.verdict}")
        println("briefId=${brief.briefId}")
        println("assetCandidatePreflightId=${brief.assetCandidatePreflightId}")
        println("decisionId=${brief.humanReviewDecisionId}")
        println("storyboardPacketReviewId=${brief.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${brief.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${brief.scriptPacketDraftFile}")
        println("scriptPacketSha256=${brief.scriptPacketDraftContentSha256}")
        println("assetCandidateRequirements=${brief.evidence.assetCandidateRequirementCount}")
        println("preparationChecklist=${brief.evidence.preparationChecklistCount}")
        println("forbiddenClaims=${brief.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${brief.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${brief.nextAllowedDownstreamStep}")
        println("blockedReason=${brief.blockedReason}")
        println("findings=${brief.findingCount}")
        brief.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (brief.verdict == "FAIL") 1 else 0
    }

    fun createAssetCandidateDraft(options: Map<String, String>): Int {
        val briefPath = Path.of(options.requiredCliOption("asset-candidate-brief"))
        val draft = AssetCandidateDraftService().create(
            assetCandidateBrief = readCliJson<AssetCandidateBrief>(briefPath),
            draftId = options["draft"] ?: "",
            assetCandidateBriefFile = briefPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), draft) }
        println("verdict=${draft.verdict}")
        println("draftId=${draft.draftId}")
        println("assetCandidateBriefId=${draft.assetCandidateBriefId}")
        println("assetCandidatePreflightId=${draft.assetCandidatePreflightId}")
        println("decisionId=${draft.humanReviewDecisionId}")
        println("storyboardPacketReviewId=${draft.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${draft.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${draft.scriptPacketDraftFile}")
        println("scriptPacketSha256=${draft.scriptPacketDraftContentSha256}")
        println("candidateItems=${draft.evidence.candidateItemCount}")
        println("forbiddenClaims=${draft.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${draft.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${draft.nextAllowedDownstreamStep}")
        println("blockedReason=${draft.blockedReason}")
        println("findings=${draft.findingCount}")
        draft.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (draft.verdict == "FAIL") 1 else 0
    }

    fun validateAssetCandidateDraft(options: Map<String, String>): Int {
        val draftPath = Path.of(options.requiredCliOption("asset-candidate-draft"))
        val report = AssetCandidateDraftValidationService().validate(
            assetCandidateDraft = readCliJson<AssetCandidateDraft>(draftPath),
            assetCandidateDraftFile = draftPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("assetCandidateDraftId=${report.assetCandidateDraftId}")
        println("assetCandidateBriefId=${report.assetCandidateBriefId}")
        println("assetCandidatePreflightId=${report.assetCandidatePreflightId}")
        println("decisionId=${report.humanReviewDecisionId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("candidateItems=${report.evidence.candidateItemCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun validateAssetCandidateReviewPreflight(options: Map<String, String>): Int {
        val validationPath = Path.of(options.requiredCliOption("asset-candidate-draft-validation"))
        val report = AssetCandidateReviewPreflightService().validate(
            assetCandidateDraftValidation = readCliJson<AssetCandidateDraftValidationReport>(validationPath),
            preflightId = options["preflight"] ?: "",
            assetCandidateDraftValidationReportFile = validationPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("assetCandidateDraftId=${report.assetCandidateDraftId}")
        println("assetCandidateBriefId=${report.assetCandidateBriefId}")
        println("assetCandidatePreflightId=${report.assetCandidatePreflightId}")
        println("decisionId=${report.humanReviewDecisionId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("candidateItems=${report.evidence.candidateItemCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun validateAssetCandidateHumanReviewDecision(options: Map<String, String>): Int {
        val preflightPath = Path.of(options.requiredCliOption("asset-candidate-review-preflight"))
        val decisionPath = Path.of(options.requiredCliOption("asset-candidate-human-review-decision"))
        val report = AssetCandidateHumanReviewDecisionIntakeService().validate(
            assetCandidateReviewPreflight = readCliJson<AssetCandidateReviewPreflightReport>(preflightPath),
            assetCandidateHumanReviewDecision = readCliJson<AssetCandidateHumanReviewDecision>(decisionPath),
            assetCandidateReviewPreflightFile = preflightPath.toString(),
            assetCandidateHumanReviewDecisionFile = decisionPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("decisionId=${report.decisionId}")
        println("preflightId=${report.preflightId}")
        println("decisionStatus=${report.decisionStatus}")
        println("decisionAllowedDownstream=${report.decisionAllowedDownstream}")
        println("assetCandidateDraftId=${report.assetCandidateDraftId}")
        println("assetCandidateBriefId=${report.assetCandidateBriefId}")
        println("assetCandidatePreflightId=${report.assetCandidatePreflightId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("imageCandidateRequirements=${report.evidence.imageCandidateRequirementCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("forbiddenOutputFields=${report.evidence.forbiddenOutputFieldCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun validateImageCandidatePreflight(options: Map<String, String>): Int {
        val intakePath = Path.of(options.requiredCliOption("asset-candidate-human-review-decision-intake"))
        val report = ImageCandidatePreflightService().validate(
            decisionIntake = readCliJson<AssetCandidateHumanReviewDecisionIntakeReport>(intakePath),
            preflightId = options["preflight"] ?: "",
            decisionIntakeFile = intakePath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("decisionId=${report.decisionId}")
        println("assetCandidateReviewPreflightId=${report.assetCandidateReviewPreflightId}")
        println("assetCandidateDraftId=${report.assetCandidateDraftId}")
        println("assetCandidateBriefId=${report.assetCandidateBriefId}")
        println("assetCandidatePreflightId=${report.assetCandidatePreflightId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("imageCandidateRequirements=${report.evidence.imageCandidateRequirementCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("forbiddenOutputFields=${report.evidence.forbiddenOutputFieldCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun createImageCandidateBrief(options: Map<String, String>): Int {
        val preflightPath = Path.of(options.requiredCliOption("image-candidate-preflight"))
        val brief = ImageCandidateBriefService().create(
            imageCandidatePreflight = readCliJson<ImageCandidatePreflightReport>(preflightPath),
            briefId = options["brief"] ?: "",
            imageCandidatePreflightFile = preflightPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), brief) }
        println("verdict=${brief.verdict}")
        println("briefId=${brief.briefId}")
        println("imageCandidatePreflightId=${brief.imageCandidatePreflightId}")
        println("decisionId=${brief.decisionId}")
        println("assetCandidateReviewPreflightId=${brief.assetCandidateReviewPreflightId}")
        println("assetCandidateDraftId=${brief.assetCandidateDraftId}")
        println("assetCandidateBriefId=${brief.assetCandidateBriefId}")
        println("assetCandidatePreflightId=${brief.assetCandidatePreflightId}")
        println("storyboardPacketReviewId=${brief.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${brief.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${brief.scriptPacketDraftFile}")
        println("scriptPacketSha256=${brief.scriptPacketDraftContentSha256}")
        println("imageCandidateRequirements=${brief.evidence.imageCandidateRequirementCount}")
        println("preparationChecklist=${brief.evidence.preparationChecklistCount}")
        println("forbiddenClaims=${brief.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${brief.evidence.forbiddenStatusCount}")
        println("forbiddenOutputFields=${brief.evidence.forbiddenOutputFieldCount}")
        println("nextAllowedDownstreamStep=${brief.nextAllowedDownstreamStep}")
        println("blockedReason=${brief.blockedReason}")
        println("findings=${brief.findingCount}")
        brief.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (brief.verdict == "FAIL") 1 else 0
    }

    fun createImageCandidatePromptPreparation(options: Map<String, String>): Int {
        val briefPath = Path.of(options.requiredCliOption("image-candidate-brief"))
        val preparation = ImageCandidatePromptPreparationService().create(
            imageCandidateBrief = readCliJson<ImageCandidateBrief>(briefPath),
            preparationId = options["preparation"] ?: "",
            imageCandidateBriefFile = briefPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), preparation) }
        println("verdict=${preparation.verdict}")
        println("preparationId=${preparation.preparationId}")
        println("imageCandidateBriefId=${preparation.imageCandidateBriefId}")
        println("imageCandidatePreflightId=${preparation.imageCandidatePreflightId}")
        println("decisionId=${preparation.decisionId}")
        println("assetCandidateReviewPreflightId=${preparation.assetCandidateReviewPreflightId}")
        println("assetCandidateDraftId=${preparation.assetCandidateDraftId}")
        println("assetCandidateBriefId=${preparation.assetCandidateBriefId}")
        println("assetCandidatePreflightId=${preparation.assetCandidatePreflightId}")
        println("storyboardPacketReviewId=${preparation.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${preparation.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${preparation.scriptPacketDraftFile}")
        println("scriptPacketSha256=${preparation.scriptPacketDraftContentSha256}")
        println("promptItems=${preparation.evidence.promptItemCount}")
        println("forbiddenClaims=${preparation.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${preparation.evidence.forbiddenStatusCount}")
        println("forbiddenOutputFields=${preparation.evidence.forbiddenOutputFieldCount}")
        println("nextAllowedDownstreamStep=${preparation.nextAllowedDownstreamStep}")
        println("blockedReason=${preparation.blockedReason}")
        println("findings=${preparation.findingCount}")
        preparation.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (preparation.verdict == "FAIL") 1 else 0
    }

    fun validateImageCandidatePromptPreparation(options: Map<String, String>): Int {
        val preparationPath = Path.of(options.requiredCliOption("image-candidate-prompt-preparation"))
        val report = ImageCandidatePromptValidationService().validate(
            promptPreparation = readCliJson<ImageCandidatePromptPreparation>(preparationPath),
            promptPreparationFile = preparationPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preparationId=${report.preparationId}")
        println("imageCandidateBriefId=${report.imageCandidateBriefId}")
        println("imageCandidatePreflightId=${report.imageCandidatePreflightId}")
        println("decisionId=${report.decisionId}")
        println("assetCandidateReviewPreflightId=${report.assetCandidateReviewPreflightId}")
        println("assetCandidateDraftId=${report.assetCandidateDraftId}")
        println("assetCandidateBriefId=${report.assetCandidateBriefId}")
        println("assetCandidatePreflightId=${report.assetCandidatePreflightId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("promptItems=${report.evidence.promptItemCount}")
        println("authorOnlyLeaks=${report.evidence.authorOnlyLeakCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("forbiddenOutputFields=${report.evidence.forbiddenOutputFieldCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun validateImageCandidateProviderHandoffPreflight(options: Map<String, String>): Int {
        val validationPath = Path.of(options.requiredCliOption("image-candidate-prompt-validation"))
        val report = ImageCandidateProviderHandoffPreflightService().validate(
            promptValidationReport = readCliJson<ImageCandidatePromptValidationReport>(validationPath),
            preflightId = options["preflight"] ?: "",
            promptValidationReportFile = validationPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("preparationId=${report.preparationId}")
        println("imageCandidateBriefId=${report.imageCandidateBriefId}")
        println("imageCandidatePreflightId=${report.imageCandidatePreflightId}")
        println("decisionId=${report.decisionId}")
        println("assetCandidateReviewPreflightId=${report.assetCandidateReviewPreflightId}")
        println("assetCandidateDraftId=${report.assetCandidateDraftId}")
        println("assetCandidateBriefId=${report.assetCandidateBriefId}")
        println("assetCandidatePreflightId=${report.assetCandidatePreflightId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("promptItems=${report.evidence.promptItemCount}")
        println("authorOnlyLeaks=${report.evidence.authorOnlyLeakCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("forbiddenOutputFields=${report.evidence.forbiddenOutputFieldCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun validateImageCandidateProviderHandoffDecision(options: Map<String, String>): Int {
        val preflightPath = Path.of(options.requiredCliOption("provider-handoff-preflight"))
        val decisionPath = Path.of(options.requiredCliOption("provider-handoff-decision"))
        val report = ImageCandidateProviderHandoffDecisionIntakeService().validate(
            providerHandoffPreflight = readCliJson<ImageCandidateProviderHandoffPreflightReport>(preflightPath),
            providerHandoffDecision = readCliJson<ImageCandidateProviderHandoffDecision>(decisionPath),
            providerHandoffPreflightFile = preflightPath.toString(),
            providerHandoffDecisionFile = decisionPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("decisionId=${report.decisionId}")
        println("preflightId=${report.preflightId}")
        println("decisionStatus=${report.decisionStatus}")
        println("decisionAllowedDownstream=${report.decisionAllowedDownstream}")
        println("preparationId=${report.preparationId}")
        println("imageCandidateBriefId=${report.imageCandidateBriefId}")
        println("imageCandidatePreflightId=${report.imageCandidatePreflightId}")
        println("promptItems=${report.evidence.promptItemCount}")
        println("checklist=${report.evidence.checklistPassedCount}/${report.evidence.checklistItemCount}")
        println("unresolvedBlockingIssues=${report.evidence.unresolvedBlockingIssueCount}")
        println("authorOnlyLeaks=${report.evidence.authorOnlyLeakCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("forbiddenOutputFields=${report.evidence.forbiddenOutputFieldCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun createImageCandidateProviderHandoffPacket(options: Map<String, String>): Int {
        val preflightPath = Path.of(options.requiredCliOption("provider-handoff-preflight"))
        val intakePath = Path.of(options.requiredCliOption("provider-handoff-decision-intake"))
        val packet = ImageCandidateProviderHandoffPacketService().create(
            providerHandoffPreflight = readCliJson<ImageCandidateProviderHandoffPreflightReport>(preflightPath),
            decisionIntake = readCliJson<ImageCandidateProviderHandoffDecisionIntakeReport>(intakePath),
            packetId = options["packet"] ?: "",
            providerHandoffPreflightFile = preflightPath.toString(),
            providerHandoffDecisionIntakeFile = intakePath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), packet) }
        println("verdict=${packet.verdict}")
        println("packetId=${packet.packetId}")
        println("decisionId=${packet.decisionId}")
        println("preflightId=${packet.preflightId}")
        println("preparationId=${packet.preparationId}")
        println("imageCandidateBriefId=${packet.imageCandidateBriefId}")
        println("imageCandidatePreflightId=${packet.imageCandidatePreflightId}")
        println("promptItems=${packet.evidence.promptItemCount}")
        println("manualInstructions=${packet.evidence.manualInstructionCount}")
        println("authorOnlyLeaks=${packet.evidence.authorOnlyLeakCount}")
        println("forbiddenClaims=${packet.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${packet.evidence.forbiddenStatusCount}")
        println("forbiddenOutputFields=${packet.evidence.forbiddenOutputFieldCount}")
        println("nextAllowedDownstreamStep=${packet.nextAllowedDownstreamStep}")
        println("blockedReason=${packet.blockedReason}")
        println("findings=${packet.findingCount}")
        packet.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (packet.verdict == "FAIL") 1 else 0
    }

    fun validateImageCandidateProviderHandoffPacket(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("provider-handoff-packet"))
        val report = ImageCandidateProviderHandoffPacketValidationService().validate(
            providerHandoffPacket = readCliJson<ImageCandidateProviderHandoffPacket>(packetPath),
            providerHandoffPacketFile = packetPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("packetId=${report.packetId}")
        println("decisionId=${report.decisionId}")
        println("preflightId=${report.preflightId}")
        println("preparationId=${report.preparationId}")
        println("promptItems=${report.evidence.promptItemCount}")
        println("manualInstructions=${report.evidence.manualInstructionCount}")
        println("authorOnlyLeaks=${report.evidence.authorOnlyLeakCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("forbiddenOutputFields=${report.evidence.forbiddenOutputFieldCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun validateImageCandidateExternalResultIntake(options: Map<String, String>): Int {
        val validationPath = Path.of(options.requiredCliOption("provider-handoff-packet-validation"))
        val externalResultPath = Path.of(options.requiredCliOption("external-result"))
        val report = ImageCandidateExternalResultIntakeService().validate(
            providerHandoffPacketValidation = readCliJson<ImageCandidateProviderHandoffPacketValidationReport>(validationPath),
            externalResult = readCliJson<ImageCandidateExternalResult>(externalResultPath),
            providerHandoffPacketValidationReportFile = validationPath.toString(),
            externalResultFile = externalResultPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("resultId=${report.resultId}")
        println("packetId=${report.packetId}")
        println("decisionId=${report.decisionId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("resultStatus=${report.resultStatus}")
        println("imageResults=${report.evidence.imageResultCount}")
        println("localImageReferences=${report.evidence.localImageReferenceCount}")
        println("metadataOnlyReferences=${report.evidence.metadataOnlyReferenceCount}")
        println("forbiddenRemoteOrProviderFields=${report.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun validateImageCandidateResultReviewPreflight(options: Map<String, String>): Int {
        val intakePath = Path.of(options.requiredCliOption("external-result-intake"))
        val report = ImageCandidateResultReviewPreflightService().validate(
            externalResultIntake = readCliJson<ImageCandidateExternalResultIntakeReport>(intakePath),
            preflightId = options["preflight"] ?: "",
            externalResultIntakeFile = intakePath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("resultId=${report.resultId}")
        println("packetId=${report.packetId}")
        println("decisionId=${report.decisionId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("imageResults=${report.evidence.imageResultCount}")
        println("localImageReferences=${report.evidence.localImageReferenceCount}")
        println("metadataOnlyReferences=${report.evidence.metadataOnlyReferenceCount}")
        println("forbiddenRemoteOrProviderFields=${report.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun validateImageCandidateResultHumanReviewDecision(options: Map<String, String>): Int {
        val preflightPath = Path.of(options.requiredCliOption("result-review-preflight"))
        val decisionPath = Path.of(options.requiredCliOption("result-review-decision"))
        val report = ImageCandidateResultHumanReviewDecisionIntakeService().validate(
            resultReviewPreflight = readCliJson<ImageCandidateResultReviewPreflightReport>(preflightPath),
            resultReviewDecision = readCliJson<ImageCandidateResultReviewDecision>(decisionPath),
            resultReviewPreflightFile = preflightPath.toString(),
            resultReviewDecisionFile = decisionPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("decisionId=${report.decisionId}")
        println("resultReviewPreflightId=${report.resultReviewPreflightId}")
        println("resultId=${report.resultId}")
        println("packetId=${report.packetId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("decisionStatus=${report.decisionStatus}")
        println("decisionAllowedDownstream=${report.decisionAllowedDownstream}")
        println("imageResults=${report.evidence.imageResultCount}")
        println("checklistItems=${report.evidence.checklistItemCount}")
        println("checklistPassed=${report.evidence.checklistPassedCount}")
        println("unresolvedBlockingIssues=${report.evidence.unresolvedBlockingIssueCount}")
        println("forbiddenRemoteOrProviderFields=${report.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun createImageCandidateReferencePacket(options: Map<String, String>): Int {
        val preflightPath = Path.of(options.requiredCliOption("result-review-preflight"))
        val intakePath = Path.of(options.requiredCliOption("result-review-decision-intake"))
        val packet = ImageCandidateReferencePacketService().create(
            resultReviewPreflight = readCliJson<ImageCandidateResultReviewPreflightReport>(preflightPath),
            decisionIntake = readCliJson<ImageCandidateResultReviewDecisionIntakeReport>(intakePath),
            packetId = options["packet"] ?: "",
            resultReviewPreflightFile = preflightPath.toString(),
            resultReviewDecisionIntakeFile = intakePath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), packet) }
        println("verdict=${packet.verdict}")
        println("packetId=${packet.packetId}")
        println("packetStatus=${packet.packetStatus}")
        println("decisionId=${packet.decisionId}")
        println("resultReviewPreflightId=${packet.resultReviewPreflightId}")
        println("resultId=${packet.resultId}")
        println("providerHandoffPacketId=${packet.providerHandoffPacketId}")
        println("acceptedScriptPacketId=${packet.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${packet.scriptPacketDraftFile}")
        println("scriptPacketSha256=${packet.scriptPacketDraftContentSha256}")
        println("imageResults=${packet.evidence.imageResultCount}")
        println("forbiddenRemoteOrProviderFields=${packet.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${packet.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${packet.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${packet.nextAllowedDownstreamStep}")
        println("blockedReason=${packet.blockedReason}")
        println("findings=${packet.findingCount}")
        packet.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (packet.verdict == "FAIL") 1 else 0
    }

    fun validateImageCandidateReferencePacket(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("candidate-reference-packet"))
        val report = ImageCandidateReferencePacketValidationService().validate(
            referencePacket = readCliJson<ImageCandidateReferencePacket>(packetPath),
            referencePacketFile = packetPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("packetId=${report.packetId}")
        println("packetStatus=${report.packetStatus}")
        println("decisionId=${report.decisionId}")
        println("resultReviewPreflightId=${report.resultReviewPreflightId}")
        println("resultId=${report.resultId}")
        println("providerHandoffPacketId=${report.providerHandoffPacketId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("imageResults=${report.evidence.imageResultCount}")
        println("forbiddenRemoteOrProviderFields=${report.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun validateCandidateReferenceHumanAcceptanceDecision(options: Map<String, String>): Int {
        val validationPath = Path.of(options.requiredCliOption("candidate-reference-packet-validation"))
        val decisionPath = Path.of(options.requiredCliOption("human-acceptance-decision"))
        val report = CandidateReferenceHumanAcceptanceDecisionIntakeService().validate(
            candidateReferencePacketValidation = readCliJson<ImageCandidateReferencePacketValidationReport>(validationPath),
            humanAcceptanceDecision = readCliJson<CandidateReferenceHumanAcceptanceDecision>(decisionPath),
            candidateReferencePacketValidationReportFile = validationPath.toString(),
            humanAcceptanceDecisionFile = decisionPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("decisionId=${report.decisionId}")
        println("packetId=${report.packetId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("decisionStatus=${report.decisionStatus}")
        println("decisionAllowedDownstream=${report.decisionAllowedDownstream}")
        println("imageResults=${report.evidence.imageResultCount}")
        println("checklistItems=${report.evidence.checklistItemCount}")
        println("checklistPassed=${report.evidence.checklistPassedCount}")
        println("forbiddenRemoteOrProviderFields=${report.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach { println("${it.ruleId}:${it.severity}:${it.path}:${it.message}") }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun validateCandidateReferenceRegistryPreflight(options: Map<String, String>): Int {
        val intakePath = Path.of(options.requiredCliOption("human-acceptance-decision-intake"))
        val validationPath = Path.of(options.requiredCliOption("candidate-reference-packet-validation"))
        val report = CandidateReferenceRegistryPreflightService().validate(
            humanAcceptanceDecisionIntake = readCliJson<CandidateReferenceHumanAcceptanceDecisionIntakeReport>(intakePath),
            candidateReferencePacketValidation = readCliJson<ImageCandidateReferencePacketValidationReport>(validationPath),
            preflightId = options["preflight"] ?: "",
            humanAcceptanceDecisionIntakeFile = intakePath.toString(),
            candidateReferencePacketValidationReportFile = validationPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("decisionId=${report.decisionId}")
        println("packetId=${report.packetId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("imageResults=${report.evidence.imageResultCount}")
        println("forbiddenRemoteOrProviderFields=${report.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach { println("${it.ruleId}:${it.severity}:${it.path}:${it.message}") }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun createCandidateReferenceRegistryPacket(options: Map<String, String>): Int {
        val preflightPath = Path.of(options.requiredCliOption("registry-preflight"))
        val packet = CandidateReferenceRegistryPacketService().create(
            registryPreflight = readCliJson<CandidateReferenceRegistryPreflightReport>(preflightPath),
            packetId = options["packet"] ?: "",
            registryPreflightFile = preflightPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), packet) }
        println("verdict=${packet.verdict}")
        println("packetId=${packet.packetId}")
        println("packetStatus=${packet.packetStatus}")
        println("preflightId=${packet.preflightId}")
        println("decisionId=${packet.decisionId}")
        println("referencePacketId=${packet.referencePacketId}")
        println("acceptedScriptPacketId=${packet.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${packet.scriptPacketDraftFile}")
        println("scriptPacketSha256=${packet.scriptPacketDraftContentSha256}")
        println("imageResults=${packet.evidence.imageResultCount}")
        println("forbiddenRemoteOrProviderFields=${packet.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${packet.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${packet.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${packet.nextAllowedDownstreamStep}")
        println("blockedReason=${packet.blockedReason}")
        println("findings=${packet.findingCount}")
        packet.findings.forEach { println("${it.ruleId}:${it.severity}:${it.path}:${it.message}") }
        return if (packet.verdict == "FAIL") 1 else 0
    }

    fun validateCandidateReferenceRegistryPacket(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("candidate-reference-registry-packet"))
        val report = CandidateReferenceRegistryPacketValidationService().validate(
            registryPacket = readCliJson<CandidateReferenceRegistryPacket>(packetPath),
            registryPacketFile = packetPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("packetId=${report.packetId}")
        println("packetStatus=${report.packetStatus}")
        println("preflightId=${report.preflightId}")
        println("decisionId=${report.decisionId}")
        println("referencePacketId=${report.referencePacketId}")
        println("resultReviewPreflightId=${report.resultReviewPreflightId}")
        println("resultReviewDecisionId=${report.resultReviewDecisionId}")
        println("providerHandoffPacketId=${report.providerHandoffPacketId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("imageResults=${report.evidence.imageResultCount}")
        println("forbiddenRemoteOrProviderFields=${report.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach { println("${it.ruleId}:${it.severity}:${it.path}:${it.message}") }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun validateCandidateReferenceRegistryHumanAcceptanceDecision(options: Map<String, String>): Int {
        val validationPath = Path.of(options.requiredCliOption("registry-packet-validation"))
        val decisionPath = Path.of(options.requiredCliOption("human-acceptance-decision"))
        val report = CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService().validate(
            registryPacketValidation = readCliJson<CandidateReferenceRegistryPacketValidationReport>(validationPath),
            humanAcceptanceDecision = readCliJson<CandidateReferenceRegistryHumanAcceptanceDecision>(decisionPath),
            registryPacketValidationReportFile = validationPath.toString(),
            humanAcceptanceDecisionFile = decisionPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("decisionId=${report.decisionId}")
        println("registryPacketId=${report.registryPacketId}")
        println("registryPreflightId=${report.registryPreflightId}")
        println("priorHumanAcceptanceDecisionId=${report.priorHumanAcceptanceDecisionId}")
        println("referencePacketId=${report.referencePacketId}")
        println("resultReviewPreflightId=${report.resultReviewPreflightId}")
        println("resultReviewDecisionId=${report.resultReviewDecisionId}")
        println("providerHandoffPacketId=${report.providerHandoffPacketId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("decisionStatus=${report.decisionStatus}")
        println("decisionAllowedDownstream=${report.decisionAllowedDownstream}")
        println("imageResults=${report.evidence.imageResultCount}")
        println("checklistItems=${report.evidence.checklistItemCount}")
        println("checklistPassed=${report.evidence.checklistPassedCount}")
        println("forbiddenRemoteOrProviderFields=${report.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach { println("${it.ruleId}:${it.severity}:${it.path}:${it.message}") }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun createCandidateReferenceAcceptancePacket(options: Map<String, String>): Int {
        val validationPath = Path.of(options.requiredCliOption("registry-packet-validation"))
        val intakePath = Path.of(options.requiredCliOption("human-acceptance-decision-intake"))
        val packet = CandidateReferenceAcceptancePacketService().create(
            registryPacketValidation = readCliJson<CandidateReferenceRegistryPacketValidationReport>(validationPath),
            decisionIntake = readCliJson<CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport>(intakePath),
            packetId = options["packet"] ?: "",
            registryPacketValidationReportFile = validationPath.toString(),
            humanAcceptanceDecisionIntakeFile = intakePath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), packet) }
        println("verdict=${packet.verdict}")
        println("packetId=${packet.packetId}")
        println("packetStatus=${packet.packetStatus}")
        println("decisionId=${packet.decisionId}")
        println("registryPacketId=${packet.registryPacketId}")
        println("registryPreflightId=${packet.registryPreflightId}")
        println("referencePacketId=${packet.referencePacketId}")
        println("resultReviewPreflightId=${packet.resultReviewPreflightId}")
        println("resultReviewDecisionId=${packet.resultReviewDecisionId}")
        println("providerHandoffPacketId=${packet.providerHandoffPacketId}")
        println("acceptedScriptPacketId=${packet.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${packet.scriptPacketDraftFile}")
        println("scriptPacketSha256=${packet.scriptPacketDraftContentSha256}")
        println("imageResults=${packet.evidence.imageResultCount}")
        println("forbiddenRemoteOrProviderFields=${packet.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${packet.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${packet.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${packet.nextAllowedDownstreamStep}")
        println("blockedReason=${packet.blockedReason}")
        println("findings=${packet.findingCount}")
        packet.findings.forEach { println("${it.ruleId}:${it.severity}:${it.path}:${it.message}") }
        return if (packet.verdict == "FAIL") 1 else 0
    }

    fun validateCandidateReferenceAcceptancePacket(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("candidate-reference-acceptance-packet"))
        val report = CandidateReferenceAcceptancePacketValidationService().validate(
            packet = readCliJson<CandidateReferenceAcceptancePacket>(packetPath),
            acceptancePacketFile = packetPath.toString(),
            validationId = options["validation"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("validationId=${report.validationId}")
        println("packetId=${report.packetId}")
        println("packetStatus=${report.packetStatus}")
        println("decisionId=${report.decisionId}")
        println("humanAcceptanceDecisionSource=${report.humanAcceptanceDecisionSource}")
        println("registryPacketId=${report.registryPacketId}")
        println("registryPreflightId=${report.registryPreflightId}")
        println("referencePacketId=${report.referencePacketId}")
        println("resultReviewPreflightId=${report.resultReviewPreflightId}")
        println("resultReviewDecisionId=${report.resultReviewDecisionId}")
        println("providerHandoffPacketId=${report.providerHandoffPacketId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("imageResults=${report.evidence.imageResultCount}")
        println("forbiddenRemoteOrProviderFields=${report.evidence.forbiddenRemoteOrProviderFieldCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenStatuses=${report.evidence.forbiddenStatusCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach { println("${it.ruleId}:${it.severity}:${it.path}:${it.message}") }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun createCharacterNuwaCardTemplate(options: Map<String, String>): Int {
        val projectId = options["project"] ?: "project-id"
        val gateId = options["gate"] ?: "$projectId-character-nuwa-gate"
        val out = Path.of(options.requiredCliOption("out"))
        val template = CharacterNuwaGateFile(
            gateId = gateId,
            projectId = projectId,
            sourceChapter = options["chapter"] ?: "",
            requiredSpeakingCharacters = listOf(
                CharacterNuwaRequiredSpeaker(
                    characterId = "char-protagonist",
                    displayName = "Protagonist",
                    requiredDepth = "full_nuwa",
                    reason = "Point-of-view or emotionally important speaker.",
                ),
                CharacterNuwaRequiredSpeaker(
                    characterId = "char-background-speaker",
                    displayName = "Background speaker",
                    requiredDepth = "light_voice",
                    reason = "One-off speaker with a line in this scene.",
                ),
            ),
            characterCards = listOf(
                CharacterNuwaCard(
                    cardId = "card-char-protagonist",
                    characterId = "char-protagonist",
                    displayName = "Protagonist",
                    cardDepth = "full_nuwa",
                    gateStatus = "draft",
                    narrativeFunction = "pov",
                    socialPosition = "Replace with social position.",
                    currentPressure = "Replace with the pressure acting on this character now.",
                    relationshipToProtagonist = "self",
                    knowledgeBoundary = listOf("Replace with what this character can observe or infer."),
                    wantsFromScene = "Replace with what this character wants from this scene.",
                    fearsInScene = "Replace with what this character is avoiding.",
                    speechRegister = "Replace with speech register.",
                    sentenceLengthBias = "mixed",
                    directness = "restrained",
                    silencePattern = "Replace with when this character goes silent.",
                    pressureResponse = "Replace with how pressure changes this character's speech.",
                    allowedTells = listOf("Replace with allowed visible or verbal tell."),
                    forbiddenTells = listOf("Replace with forbidden tell."),
                    favoriteMoves = listOf("asks_back", "deflects"),
                    neverDo = listOf("recite_world_lore", "reveal_author_only_truth", "use_calibration_slogan"),
                    sampleLines = CharacterNuwaSampleLines(
                        accepted = listOf(
                            "Replace with accepted line 1.",
                            "Replace with accepted line 2.",
                            "Replace with accepted line 3.",
                        ),
                        rejected = listOf(
                            "Replace with rejected generic line 1.",
                            "Replace with rejected generic line 2.",
                            "Replace with rejected generic line 3.",
                        ),
                    ),
                    bannedStructures = listOf(
                        "???狷X嚗XXX",
                        "????X嚗 Y",
                        "銝XX嚗XXX",
                        "銝 X嚗 Y",
                        "not X, but Y",
                    ),
                ),
                CharacterNuwaCard(
                    cardId = "card-char-background-speaker",
                    characterId = "char-background-speaker",
                    displayName = "Background speaker",
                    cardDepth = "light_voice",
                    gateStatus = "draft",
                    narrativeFunction = "passerby",
                    currentPressure = "Replace with immediate pressure.",
                    relationshipToProtagonist = "stranger",
                    knowledgeBoundary = listOf("Replace with what this one-off speaker can know."),
                    speechRegister = "plain",
                    directness = "blunt",
                    pressureResponse = "Replace with how pressure shapes the line.",
                    neverDo = listOf("recite_world_lore", "use_calibration_slogan"),
                    sampleLines = CharacterNuwaSampleLines(
                        accepted = listOf("Replace with one acceptable line."),
                        rejected = listOf("Replace with one rejected line."),
                    ),
                ),
            ),
            bannedCalibrationStructures = listOf(
                "???狷X嚗XXX",
                "????X嚗 Y",
                "銝XX嚗XXX",
                "銝 X嚗 Y",
                "not X, but Y",
                "not X, only Y",
            ),
            reviewerNotes = "Fill this template before drafting accepted dialogue. No character card means no accepted dialogue.",
        )
        writeCliJson(out, template)
        println("wrote=$out")
        println("gateId=${template.gateId}")
        println("cards=${template.characterCards.size}")
        return 0
    }

    fun validateCharacterNuwaGate(options: Map<String, String>): Int {
        val gatePath = options["file"]?.let { Path.of(it) }
            ?: options["character-dir"]?.let { Path.of(it).resolve("character_nuwa_gate.json") }
            ?: error("Missing required option --file or --character-dir")
        val gate = readCliJson<CharacterNuwaGateFile>(gatePath)
        val report = CharacterNuwaGateValidationService().validate(
            gate = gate,
            gateFile = gatePath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("gateId=${report.gateId}")
        println("requiredSpeakingCharacters=${report.requiredSpeakingCharacterCount}")
        println("cards=${report.cardCount}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }
}
