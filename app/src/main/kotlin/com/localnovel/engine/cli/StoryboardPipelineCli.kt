package com.localnovel.engine.cli

import com.localnovel.engine.cli.readCliJson
import com.localnovel.engine.cli.requiredCliOption
import com.localnovel.engine.cli.writeCliJson
import com.localnovel.engine.model.AcceptedScriptPacketHandoffPacket
import com.localnovel.engine.model.ImageFacingDeliveryProof
import com.localnovel.engine.model.NovelizationAcceptedDraftPacket
import com.localnovel.engine.model.NovelizationScriptPacketPreflightReport
import com.localnovel.engine.model.ReviewableStoryboardPacket
import com.localnovel.engine.model.ScriptPacketDraft
import com.localnovel.engine.model.ScriptPacketDraftStructureBridgeReport
import com.localnovel.engine.model.ScriptPacketDraftValidationReport
import com.localnovel.engine.model.SettingImageFacingProofPreviewBuildEvidence
import com.localnovel.engine.model.SettingImageFacingProofPreviewBuildFinding
import com.localnovel.engine.model.SettingImageFacingProofPreviewBuildReport
import com.localnovel.engine.model.SettingPack
import com.localnovel.engine.model.SettingPackScriptPacketDraftBuildEvidence
import com.localnovel.engine.model.SettingPackScriptPacketDraftBuildFinding
import com.localnovel.engine.model.SettingPackScriptPacketDraftBuildReport
import com.localnovel.engine.model.StoryboardPacketDraft
import com.localnovel.engine.model.StoryboardPacketDraftValidationReport
import com.localnovel.engine.model.StoryboardPacketHumanReviewDecision
import com.localnovel.engine.model.StoryboardPacketHumanReviewPreflightReport
import com.localnovel.engine.model.StoryboardPacketReviewPacket
import com.localnovel.engine.model.StoryboardPlanningBrief
import com.localnovel.engine.model.StoryboardPlanningPreflightReport
import com.localnovel.engine.model.StoryboardStoryMovementAuditReport
import com.localnovel.engine.service.AcceptedScriptPacketHandoffPacketService
import com.localnovel.engine.service.ImageFacingDeliveryProofService
import com.localnovel.engine.service.ReviewableStoryboardPacketService
import com.localnovel.engine.service.ScriptPacketDraftSourceBindingValidationService
import com.localnovel.engine.service.ScriptPacketDraftStructureBridgeService
import com.localnovel.engine.service.ScriptPacketDraftValidationService
import com.localnovel.engine.service.SettingImageFacingProofPreviewBuildService
import com.localnovel.engine.service.SettingPackScriptPacketDraftService
import com.localnovel.engine.service.StoryboardPacketDraftValidationService
import com.localnovel.engine.service.StoryboardPacketHumanReviewDecisionIntakeService
import com.localnovel.engine.service.StoryboardPacketHumanReviewPreflightService
import com.localnovel.engine.service.StoryboardPacketReviewService
import com.localnovel.engine.service.StoryboardPlanningBriefService
import com.localnovel.engine.service.StoryboardPlanningPreflightService
import com.localnovel.engine.service.StoryboardStoryMovementAuditService
import java.nio.file.Files
import java.nio.file.Path

internal object StoryboardPipelineCli {
    fun createSettingScriptPacketDraft(options: Map<String, String>): Int {
        val settingPackPath = Path.of(options.requiredCliOption("setting-pack"))
        val draftPath = Path.of(options.requiredCliOption("out"))
        val validationPath = options["validation-out"]?.let { Path.of(it) } ?: derivedJsonPath(draftPath, "_validation")
        val reportPath = options["report-out"]?.let { Path.of(it) } ?: derivedJsonPath(draftPath, "_build_report")
        val pack = readCliJson<SettingPack>(settingPackPath)
        val result = SettingPackScriptPacketDraftService().create(
            settingPack = pack,
            settingPackFile = settingPackPath.toString(),
            requestedSceneId = options["scene"] ?: "",
            packetId = options["packet"] ?: "",
        )
        val findings = result.findings.toMutableList()
        var validationVerdict = ""
        var validationFindingCount = 0
        var outputPacketId = ""

        val draft = result.draft
        if (draft != null) {
            outputPacketId = draft.packetId
            writeCliJson(draftPath, draft)
            val validation = ScriptPacketDraftValidationService().validate(
                packet = draft,
                packetFile = draftPath.toString(),
            )
            validationVerdict = validation.verdict
            validationFindingCount = validation.findingCount
            writeCliJson(validationPath, validation)
            if (validation.verdict == "FAIL") {
                findings += settingScriptPacketDraftBuildFinding(
                    ruleId = "SCRIPT_PACKET_DRAFT_VALIDATION_FAILED",
                    category = "validation",
                    severity = "CRITICAL",
                    path = validationPath.toString(),
                    message = "Setting-pack script packet draft creation requires the generated draft to pass existing draft validation.",
                    evidence = validation.findings.map { it.ruleId },
                )
            }
        }

        val hasBlockingFinding = findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }
        val verdict = when {
            draft == null || hasBlockingFinding -> "FAIL"
            validationVerdict == "PASS_WITH_WARNINGS" || findings.isNotEmpty() -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }
        val buildId = (options["build"] ?: "").ifBlank {
            "setting-script-packet-draft-${pack.packId}-${result.selectedSceneId.ifBlank { "unselected" }}"
        }
        val report = SettingPackScriptPacketDraftBuildReport(
            verdict = verdict,
            buildId = buildId,
            settingPackFile = settingPackPath.toString(),
            scriptPacketDraftFile = if (draft != null) draftPath.toString() else "",
            scriptPacketValidationFile = if (validationVerdict.isNotBlank()) validationPath.toString() else "",
            packId = pack.packId,
            packVersion = pack.packVersion,
            projectId = pack.projectId,
            selectedSceneId = result.selectedSceneId,
            selectedLocationId = result.selectedLocationId,
            outputPacketId = outputPacketId,
            validationVerdict = validationVerdict,
            nextAllowedDownstreamStep = if (verdict != "FAIL") "script_packet_draft_review" else "",
            blockedReason = if (verdict == "FAIL") {
                findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId }
            } else {
                ""
            },
            findingCount = findings.size,
            findings = findings,
            evidence = result.evidence.withValidationFindingCount(validationFindingCount),
        )
        writeCliJson(reportPath, report)
        printSettingScriptPacketDraftBuildReport(report)
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun derivedJsonPath(base: Path, suffix: String): Path {
        val fileName = base.fileName?.toString() ?: "output.json"
        val stem = fileName.removeSuffix(".json")
        return base.parent?.resolve("$stem$suffix.json") ?: Path.of("$stem$suffix.json")
    }

    fun SettingPackScriptPacketDraftBuildEvidence.withValidationFindingCount(
        validationFindingCount: Int,
    ): SettingPackScriptPacketDraftBuildEvidence =
        copy(validationFindingCount = validationFindingCount)

    fun settingScriptPacketDraftBuildFinding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): SettingPackScriptPacketDraftBuildFinding =
        SettingPackScriptPacketDraftBuildFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    fun printSettingScriptPacketDraftBuildReport(report: SettingPackScriptPacketDraftBuildReport) {
        println("verdict=${report.verdict}")
        println("buildId=${report.buildId}")
        println("packId=${report.packId}")
        println("projectId=${report.projectId}")
        println("selectedSceneId=${report.selectedSceneId}")
        println("selectedLocationId=${report.selectedLocationId}")
        println("outputPacketId=${report.outputPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketValidationFile=${report.scriptPacketValidationFile}")
        println("validationVerdict=${report.validationVerdict}")
        println("generatedBeats=${report.evidence.generatedBeatCount}")
        println("generatedFrameCandidates=${report.evidence.generatedFrameCandidateCount}")
        println("excludedSecrets=${report.evidence.excludedSecretCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
    }

    fun buildSettingImageFacingProofPreview(options: Map<String, String>): Int {
        val settingPackPath = Path.of(options.requiredCliOption("setting-pack"))
        val outputDir = Path.of(options.requiredCliOption("output-dir"))
        Files.createDirectories(outputDir)

        val scriptDraftPath = outputDir.resolve("script_packet_draft.json")
        val scriptValidationPath = outputDir.resolve("script_packet_draft_validation.json")
        val settingBuildReportPath = outputDir.resolve("setting_script_packet_draft_build_report.json")
        val planningBriefPath = outputDir.resolve("storyboard_planning_brief.json")
        val storyboardDraftPath = outputDir.resolve("storyboard_packet_draft.json")
        val storyboardDraftValidationPath = outputDir.resolve("storyboard_packet_draft_validation.json")
        val storyboardReviewPath = outputDir.resolve("storyboard_packet_review.json")
        val humanReviewPreflightPath = outputDir.resolve("storyboard_human_review_preflight.json")
        val storyMovementAuditPath = outputDir.resolve("storyboard_story_movement_audit.json")
        val reviewableStoryboardPath = outputDir.resolve("reviewable_storyboard_packet.json")
        val proofPath = outputDir.resolve("image_facing_delivery_proof.json")
        val reportPath = options["report-out"]?.let { Path.of(it) }
            ?: outputDir.resolve("setting_image_facing_proof_preview_build_report.json")

        val buildId = (options["build"] ?: "").ifBlank { "setting-image-facing-proof-preview-build" }
        val findings = mutableListOf<SettingImageFacingProofPreviewBuildFinding>()
        val pack = readCliJson<SettingPack>(settingPackPath)
        val previewService = SettingImageFacingProofPreviewBuildService()

        var settingBuildReport: SettingPackScriptPacketDraftBuildReport? = null
        var scriptValidation: ScriptPacketDraftValidationReport? = null
        var planningBrief: StoryboardPlanningBrief? = null
        var storyboardDraft: StoryboardPacketDraft? = null
        var storyboardDraftValidation: StoryboardPacketDraftValidationReport? = null
        var storyboardReview: StoryboardPacketReviewPacket? = null
        var humanReviewPreflight: StoryboardPacketHumanReviewPreflightReport? = null
        var storyMovementAudit: StoryboardStoryMovementAuditReport? = null
        var reviewableStoryboard: com.localnovel.engine.model.ReviewableStoryboardPacket? = null
        var proof: ImageFacingDeliveryProof? = null

        try {
            val settingResult = SettingPackScriptPacketDraftService().create(
                settingPack = pack,
                settingPackFile = settingPackPath.toString(),
                requestedSceneId = options["scene"] ?: "",
                packetId = options["script-packet"] ?: "",
            )
            val scriptDraft = settingResult.draft
            if (scriptDraft == null) {
                settingBuildReport = SettingPackScriptPacketDraftBuildReport(
                    verdict = "FAIL",
                    buildId = "$buildId-setting-script-draft",
                    settingPackFile = settingPackPath.toString(),
                    packId = pack.packId,
                    packVersion = pack.packVersion,
                    projectId = pack.projectId,
                    selectedSceneId = settingResult.selectedSceneId,
                    selectedLocationId = settingResult.selectedLocationId,
                    findingCount = settingResult.findings.size,
                    findings = settingResult.findings,
                    evidence = settingResult.evidence,
                )
                writeCliJson(settingBuildReportPath, settingBuildReport)
                findings += previewBuildFinding(
                    ruleId = "SETTING_SCRIPT_PACKET_DRAFT_BUILD_FAILED",
                    category = "setting_script_draft",
                    severity = "CRITICAL",
                    path = settingBuildReportPath.toString(),
                    message = "Setting image-facing preview requires a generated setting script packet draft.",
                    evidence = settingResult.findings.map { it.ruleId },
                )
            } else {
                writeCliJson(scriptDraftPath, scriptDraft)
                val scriptText = Files.readString(scriptDraftPath).removePrefix("\uFEFF")
                scriptValidation = ScriptPacketDraftValidationService().validate(
                    packet = scriptDraft,
                    packetFile = scriptDraftPath.toString(),
                )
                writeCliJson(scriptValidationPath, scriptValidation)
                val settingBuildVerdict = when {
                    scriptValidation?.verdict == "FAIL" -> "FAIL"
                    settingResult.findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
                    scriptValidation?.verdict == "PASS_WITH_WARNINGS" || settingResult.findings.isNotEmpty() -> "PASS_WITH_WARNINGS"
                    else -> "PASS"
                }
                settingBuildReport = SettingPackScriptPacketDraftBuildReport(
                    verdict = settingBuildVerdict,
                    buildId = "$buildId-setting-script-draft",
                    settingPackFile = settingPackPath.toString(),
                    scriptPacketDraftFile = scriptDraftPath.toString(),
                    scriptPacketValidationFile = scriptValidationPath.toString(),
                    packId = pack.packId,
                    packVersion = pack.packVersion,
                    projectId = pack.projectId,
                    selectedSceneId = settingResult.selectedSceneId,
                    selectedLocationId = settingResult.selectedLocationId,
                    outputPacketId = scriptDraft.packetId,
                    validationVerdict = scriptValidation?.verdict.orEmpty(),
                    nextAllowedDownstreamStep = if (settingBuildVerdict == "FAIL") "" else "script_packet_draft_review",
                    blockedReason = if (settingBuildVerdict == "FAIL") {
                        (settingResult.findings.map { it.ruleId } + scriptValidation?.findings.orEmpty().map { it.ruleId })
                            .filter { it.isNotBlank() }
                            .distinct()
                            .joinToString("; ")
                    } else {
                        ""
                    },
                    findingCount = settingResult.findings.size,
                    findings = settingResult.findings,
                    evidence = settingResult.evidence.withValidationFindingCount(scriptValidation?.findingCount ?: 0),
                )
                writeCliJson(settingBuildReportPath, settingBuildReport)
                addStageFailure(
                    findings = findings,
                    verdict = settingBuildReport?.verdict.orEmpty(),
                    stageRuleId = "SETTING_SCRIPT_PACKET_DRAFT_BUILD_NOT_PASS",
                    category = "setting_script_draft",
                    path = settingBuildReportPath,
                    message = "Setting image-facing preview requires a non-failing setting script draft build.",
                    evidence = settingBuildReport?.findings.orEmpty().map { it.ruleId },
                )

                planningBrief = previewService.createPlanningBrief(
                    scriptPacketDraft = scriptDraft,
                    scriptPacketDraftText = scriptText,
                    scriptPacketDraftFile = scriptDraftPath.toString(),
                    scriptPacketValidation = scriptValidation ?: error("Missing script validation"),
                    settingBuildReport = settingBuildReport ?: error("Missing setting build report"),
                    settingBuildReportFile = settingBuildReportPath.toString(),
                    briefId = options["storyboard-brief"] ?: "",
                )
                writeCliJson(planningBriefPath, planningBrief)
                addStageFailure(
                    findings = findings,
                    verdict = planningBrief?.verdict.orEmpty(),
                    stageRuleId = "STORYBOARD_PLANNING_BRIEF_FAILED",
                    category = "storyboard_planning",
                    path = planningBriefPath,
                    message = "Setting image-facing preview requires PASS local preview storyboard planning brief.",
                    evidence = planningBrief?.findings.orEmpty().map { it.ruleId },
                )

                storyboardDraft = previewService.createStoryboardDraft(
                    scriptPacketDraft = scriptDraft,
                    planningBrief = planningBrief ?: error("Missing planning brief"),
                    planningBriefFile = planningBriefPath.toString(),
                    draftId = options["storyboard-draft"] ?: "",
                )
                writeCliJson(storyboardDraftPath, storyboardDraft)

                storyboardDraftValidation = StoryboardPacketDraftValidationService().validate(
                    storyboardPlanningBrief = planningBrief ?: error("Missing planning brief"),
                    storyboardPacketDraft = storyboardDraft ?: error("Missing storyboard draft"),
                    storyboardPlanningBriefFile = planningBriefPath.toString(),
                    storyboardPacketDraftFile = storyboardDraftPath.toString(),
                )
                writeCliJson(storyboardDraftValidationPath, storyboardDraftValidation)
                addStageFailure(
                    findings = findings,
                    verdict = storyboardDraftValidation?.verdict.orEmpty(),
                    stageRuleId = "STORYBOARD_PACKET_DRAFT_VALIDATION_FAILED",
                    category = "storyboard_draft",
                    path = storyboardDraftValidationPath,
                    message = "Setting image-facing preview requires PASS storyboard packet draft validation.",
                    evidence = storyboardDraftValidation?.findings.orEmpty().map { it.ruleId },
                )

                storyboardReview = StoryboardPacketReviewService().create(
                    storyboardPacketDraft = storyboardDraft ?: error("Missing storyboard draft"),
                    storyboardPacketDraftReport = storyboardDraftValidation ?: error("Missing storyboard draft validation"),
                    reviewId = options["storyboard-review"] ?: "",
                    storyboardPacketDraftFile = storyboardDraftPath.toString(),
                    storyboardPacketDraftReportFile = storyboardDraftValidationPath.toString(),
                )
                writeCliJson(storyboardReviewPath, storyboardReview)
                addStageFailure(
                    findings = findings,
                    verdict = storyboardReview?.verdict.orEmpty(),
                    stageRuleId = "STORYBOARD_PACKET_REVIEW_FAILED",
                    category = "storyboard_review",
                    path = storyboardReviewPath,
                    message = "Setting image-facing preview requires PASS storyboard packet review.",
                    evidence = storyboardReview?.findings.orEmpty().map { it.ruleId },
                )

                humanReviewPreflight = StoryboardPacketHumanReviewPreflightService().validate(
                    storyboardPacketReview = storyboardReview ?: error("Missing storyboard review"),
                    preflightId = options["human-review-preflight"] ?: "",
                    storyboardPacketReviewFile = storyboardReviewPath.toString(),
                )
                writeCliJson(humanReviewPreflightPath, humanReviewPreflight)
                addStageFailure(
                    findings = findings,
                    verdict = humanReviewPreflight?.verdict.orEmpty(),
                    stageRuleId = "STORYBOARD_HUMAN_REVIEW_PREFLIGHT_FAILED",
                    category = "storyboard_review",
                    path = humanReviewPreflightPath,
                    message = "Setting image-facing preview requires PASS human-review preflight readiness.",
                    evidence = humanReviewPreflight?.findings.orEmpty().map { it.ruleId },
                )

                storyMovementAudit = StoryboardStoryMovementAuditService().audit(
                    storyboardPacketDraft = storyboardDraft ?: error("Missing storyboard draft"),
                    storyboardPacketReview = storyboardReview ?: error("Missing storyboard review"),
                    storyboardHumanReviewPreflight = humanReviewPreflight ?: error("Missing human review preflight"),
                    storyboardPacketDraftFile = storyboardDraftPath.toString(),
                    storyboardPacketReviewFile = storyboardReviewPath.toString(),
                    storyboardHumanReviewPreflightFile = humanReviewPreflightPath.toString(),
                    auditId = options["story-movement-audit"] ?: "",
                )
                writeCliJson(storyMovementAuditPath, storyMovementAudit)
                addStageFailure(
                    findings = findings,
                    verdict = storyMovementAudit?.verdict.orEmpty(),
                    stageRuleId = "STORY_MOVEMENT_AUDIT_FAILED",
                    category = "story_movement",
                    path = storyMovementAuditPath,
                    message = "Setting image-facing preview requires PASS story movement audit.",
                    evidence = storyMovementAudit?.findings.orEmpty().map { it.ruleId },
                )

                reviewableStoryboard = ReviewableStoryboardPacketService().create(
                    storyboardPacketDraft = storyboardDraft ?: error("Missing storyboard draft"),
                    storyboardPacketReview = storyboardReview ?: error("Missing storyboard review"),
                    storyboardHumanReviewPreflight = humanReviewPreflight ?: error("Missing human review preflight"),
                    storyboardStoryMovementAudit = storyMovementAudit ?: error("Missing story movement audit"),
                    storyboardPacketDraftFile = storyboardDraftPath.toString(),
                    storyboardPacketReviewFile = storyboardReviewPath.toString(),
                    storyboardHumanReviewPreflightFile = humanReviewPreflightPath.toString(),
                    storyboardStoryMovementAuditFile = storyMovementAuditPath.toString(),
                    packetId = options["reviewable-storyboard"] ?: "",
                )
                writeCliJson(reviewableStoryboardPath, reviewableStoryboard)
                addStageFailure(
                    findings = findings,
                    verdict = reviewableStoryboard?.verdict.orEmpty(),
                    stageRuleId = "REVIEWABLE_STORYBOARD_PACKET_FAILED",
                    category = "reviewable_storyboard",
                    path = reviewableStoryboardPath,
                    message = "Setting image-facing preview requires PASS reviewable storyboard packet.",
                    evidence = reviewableStoryboard?.findings.orEmpty().map { it.ruleId },
                )

                proof = ImageFacingDeliveryProofService().create(
                    reviewableStoryboardPacket = reviewableStoryboard ?: error("Missing reviewable storyboard"),
                    reviewableStoryboardPacketFile = reviewableStoryboardPath.toString(),
                    proofId = options["proof"] ?: "",
                )
                writeCliJson(proofPath, proof)
                addStageFailure(
                    findings = findings,
                    verdict = proof?.verdict.orEmpty(),
                    stageRuleId = "IMAGE_FACING_DELIVERY_PROOF_FAILED",
                    category = "image_facing",
                    path = proofPath,
                    message = "Setting image-facing preview requires PASS image-facing delivery proof.",
                    evidence = proof?.findings.orEmpty().map { it.ruleId },
                )
            }
        } catch (exception: Exception) {
            findings += previewBuildFinding(
                ruleId = "SETTING_IMAGE_FACING_PREVIEW_BUILD_EXCEPTION",
                category = "execution",
                severity = "CRITICAL",
                path = "$.execution",
                message = "Setting image-facing preview build failed before completing the JSON chain.",
                evidence = listOf(exception::class.java.simpleName, exception.message.orEmpty()),
            )
        }

        val verdict = if (proof?.verdict == "PASS" && findings.none { it.severity == "CRITICAL" || it.severity == "MAJOR" }) {
            "PASS"
        } else {
            "FAIL"
        }
        val report = SettingImageFacingProofPreviewBuildReport(
            verdict = verdict,
            buildId = buildId,
            buildStatus = if (verdict == "PASS") "local_preview_image_facing_proof_built" else "FAIL",
            settingPackFile = settingPackPath.toString(),
            outputDir = outputDir.toString(),
            settingScriptPacketDraftBuildReportFile = settingBuildReportPath.toString(),
            scriptPacketDraftFile = if (Files.exists(scriptDraftPath)) scriptDraftPath.toString() else "",
            scriptPacketValidationFile = if (Files.exists(scriptValidationPath)) scriptValidationPath.toString() else "",
            storyboardPlanningBriefFile = if (Files.exists(planningBriefPath)) planningBriefPath.toString() else "",
            storyboardPacketDraftFile = if (Files.exists(storyboardDraftPath)) storyboardDraftPath.toString() else "",
            storyboardPacketDraftValidationFile = if (Files.exists(storyboardDraftValidationPath)) storyboardDraftValidationPath.toString() else "",
            storyboardPacketReviewFile = if (Files.exists(storyboardReviewPath)) storyboardReviewPath.toString() else "",
            storyboardHumanReviewPreflightFile = if (Files.exists(humanReviewPreflightPath)) humanReviewPreflightPath.toString() else "",
            storyboardStoryMovementAuditFile = if (Files.exists(storyMovementAuditPath)) storyMovementAuditPath.toString() else "",
            reviewableStoryboardPacketFile = if (Files.exists(reviewableStoryboardPath)) reviewableStoryboardPath.toString() else "",
            imageFacingDeliveryProofFile = if (Files.exists(proofPath)) proofPath.toString() else "",
            packId = pack.packId,
            projectId = pack.projectId,
            selectedSceneId = settingBuildReport?.selectedSceneId.orEmpty(),
            scriptPacketDraftId = settingBuildReport?.outputPacketId.orEmpty(),
            storyboardPlanningBriefId = planningBrief?.briefId.orEmpty(),
            storyboardPacketDraftId = storyboardDraft?.packetId.orEmpty(),
            storyboardPacketReviewId = storyboardReview?.reviewId.orEmpty(),
            storyboardHumanReviewPreflightId = humanReviewPreflight?.preflightId.orEmpty(),
            storyboardStoryMovementAuditId = storyMovementAudit?.auditId.orEmpty(),
            reviewableStoryboardPacketId = reviewableStoryboard?.packetId.orEmpty(),
            imageFacingDeliveryProofId = proof?.proofId.orEmpty(),
            nextAllowedDownstreamStep = if (verdict == "PASS") proof?.nextAllowedDownstreamStep.orEmpty() else "",
            blockedReason = if (verdict == "FAIL") findings.joinToString("; ") { it.ruleId } else "",
            findingCount = findings.size,
            findings = findings,
            evidence = SettingImageFacingProofPreviewBuildEvidence(
                settingScriptDraftBuildVerdict = settingBuildReport?.verdict.orEmpty(),
                scriptPacketValidationVerdict = scriptValidation?.verdict.orEmpty(),
                storyboardPlanningBriefVerdict = planningBrief?.verdict.orEmpty(),
                storyboardPacketDraftValidationVerdict = storyboardDraftValidation?.verdict.orEmpty(),
                storyboardPacketReviewVerdict = storyboardReview?.verdict.orEmpty(),
                storyboardHumanReviewPreflightVerdict = humanReviewPreflight?.verdict.orEmpty(),
                storyboardStoryMovementAuditVerdict = storyMovementAudit?.verdict.orEmpty(),
                reviewableStoryboardPacketVerdict = reviewableStoryboard?.verdict.orEmpty(),
                imageFacingDeliveryProofVerdict = proof?.verdict.orEmpty(),
                generatedScriptSegmentCount = scriptValidation?.segmentCount ?: 0,
                generatedStoryboardFrameCount = storyboardDraft?.frames?.size ?: 0,
                generatedImageFrameCardCount = proof?.frameDeliveryCards?.size ?: 0,
                excludedSecretCount = settingBuildReport?.evidence?.excludedSecretCount ?: 0,
                findingCountAcrossStages = listOf(
                    settingBuildReport?.findingCount ?: 0,
                    scriptValidation?.findingCount ?: 0,
                    planningBrief?.findingCount ?: 0,
                    storyboardDraftValidation?.findingCount ?: 0,
                    storyboardReview?.findingCount ?: 0,
                    humanReviewPreflight?.findingCount ?: 0,
                    storyMovementAudit?.findingCount ?: 0,
                    reviewableStoryboard?.findingCount ?: 0,
                    proof?.findingCount ?: 0,
                ).sum(),
            ),
        )
        writeCliJson(reportPath, report)
        printSettingImageFacingProofPreviewBuildReport(report)
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun addStageFailure(
        findings: MutableList<SettingImageFacingProofPreviewBuildFinding>,
        verdict: String,
        stageRuleId: String,
        category: String,
        path: Path,
        message: String,
        evidence: List<String>,
    ) {
        if (verdict.trim().uppercase() == "PASS") return
        findings += previewBuildFinding(
            ruleId = stageRuleId,
            category = category,
            severity = "CRITICAL",
            path = path.toString(),
            message = message,
            evidence = evidence,
        )
    }

    fun previewBuildFinding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): SettingImageFacingProofPreviewBuildFinding =
        SettingImageFacingProofPreviewBuildFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    fun printSettingImageFacingProofPreviewBuildReport(report: SettingImageFacingProofPreviewBuildReport) {
        println("verdict=${report.verdict}")
        println("buildId=${report.buildId}")
        println("buildStatus=${report.buildStatus}")
        println("packId=${report.packId}")
        println("projectId=${report.projectId}")
        println("selectedSceneId=${report.selectedSceneId}")
        println("scriptPacketDraftId=${report.scriptPacketDraftId}")
        println("storyboardPacketDraftId=${report.storyboardPacketDraftId}")
        println("reviewableStoryboardPacketId=${report.reviewableStoryboardPacketId}")
        println("imageFacingDeliveryProofId=${report.imageFacingDeliveryProofId}")
        println("imageFacingDeliveryProofFile=${report.imageFacingDeliveryProofFile}")
        println("imageFacingDeliveryProofVerdict=${report.evidence.imageFacingDeliveryProofVerdict}")
        println("generatedStoryboardFrames=${report.evidence.generatedStoryboardFrameCount}")
        println("generatedImageFrameCards=${report.evidence.generatedImageFrameCardCount}")
        println("excludedSecrets=${report.evidence.excludedSecretCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
    }

    fun validateScriptPacketDraft(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("packet"))
        val packet = readCliJson<ScriptPacketDraft>(packetPath)
        val report = ScriptPacketDraftValidationService().validate(
            packet = packet,
            packetFile = packetPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("packetId=${report.packetId}")
        println("segments=${report.segmentCount}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun validateScriptPacketSourceBinding(options: Map<String, String>): Int {
        val scriptPacketPath = Path.of(options.requiredCliOption("script-packet-draft"))
        val preflightPath = Path.of(options.requiredCliOption("script-packet-preflight"))
        val acceptedPacketPath = Path.of(options.requiredCliOption("accepted-draft-packet"))
        val report = ScriptPacketDraftSourceBindingValidationService().validate(
            scriptPacketDraft = readCliJson<ScriptPacketDraft>(scriptPacketPath),
            scriptPacketPreflight = readCliJson<NovelizationScriptPacketPreflightReport>(preflightPath),
            acceptedDraftPacket = readCliJson<NovelizationAcceptedDraftPacket>(acceptedPacketPath),
            scriptPacketDraftFile = scriptPacketPath.toString(),
            scriptPacketPreflightFile = preflightPath.toString(),
            acceptedDraftPacketFile = acceptedPacketPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("scriptPacketId=${report.scriptPacketId}")
        println("acceptedDraftPacketId=${report.acceptedDraftPacketId}")
        println("bindingMatchedBy=${report.evidence.bindingMatchedBy.joinToString(",")}")
        println("continuityAnchors=${report.evidence.referencedContinuityAnchorCount}/${report.evidence.protectedContinuityAnchorCount}")
        println("boundaryClaims=${report.evidence.boundaryClaimCount}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun validateScriptPacketStructureBridge(options: Map<String, String>): Int {
        val scriptPacketPath = Path.of(options.requiredCliOption("script-packet-draft"))
        val preflightPath = Path.of(options.requiredCliOption("script-packet-preflight"))
        val acceptedPacketPath = Path.of(options.requiredCliOption("accepted-draft-packet"))
        val report = ScriptPacketDraftStructureBridgeService().validate(
            scriptPacketDraft = readCliJson<ScriptPacketDraft>(scriptPacketPath),
            scriptPacketPreflight = readCliJson<NovelizationScriptPacketPreflightReport>(preflightPath),
            acceptedDraftPacket = readCliJson<NovelizationAcceptedDraftPacket>(acceptedPacketPath),
            scriptPacketDraftFile = scriptPacketPath.toString(),
            scriptPacketPreflightFile = preflightPath.toString(),
            acceptedDraftPacketFile = acceptedPacketPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("scriptPacketId=${report.scriptPacketId}")
        println("sourceBindingVerdict=${report.sourceBindingVerdict}")
        println("structureVerdict=${report.structureVerdict}")
        println("downstreamAllowed=${report.downstreamAllowed}")
        println("blockedReason=${report.blockedReason}")
        println("sourceBindingFindings=${report.sourceBindingFindingCount}")
        println("structureFindings=${report.structureFindingCount}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun createAcceptedScriptPacketHandoffPacket(options: Map<String, String>): Int {
        val scriptPacketPath = Path.of(options.requiredCliOption("script-packet-draft"))
        val bridgeReportPath = Path.of(options.requiredCliOption("structure-bridge-report"))
        val scriptPacketText = Files.readString(scriptPacketPath).removePrefix("\uFEFF")
        val report = AcceptedScriptPacketHandoffPacketService().build(
            scriptPacketDraftText = scriptPacketText,
            scriptPacketDraft = readCliJson<ScriptPacketDraft>(scriptPacketPath),
            structureBridgeReport = readCliJson<ScriptPacketDraftStructureBridgeReport>(bridgeReportPath),
            scriptPacketDraftFile = scriptPacketPath.toString(),
            structureBridgeReportFile = bridgeReportPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("packetId=${report.packetId}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("bridgeVerdict=${report.sourceReports.structureBridgeVerdict}")
        println("sourceBindingVerdict=${report.sourceReports.sourceBindingVerdict}")
        println("structureVerdict=${report.sourceReports.structureVerdict}")
        println("protectedContinuityAnchors=${report.protectedContinuityAnchorsSummary.protectedContinuityAnchorCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun validateStoryboardPlanningPreflight(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("accepted-script-packet"))
        val report = StoryboardPlanningPreflightService().validate(
            acceptedScriptPacket = readCliJson<AcceptedScriptPacketHandoffPacket>(packetPath),
            preflightId = options["preflight"] ?: "",
            acceptedScriptPacketFile = packetPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("packetVerdict=${report.evidence.packetVerdict}")
        println("sourceReportFiles=${report.evidence.sourceReportFileCount}")
        println("sourceReportVerdicts=${report.evidence.sourceReportVerdictCount}")
        println("protectedContinuityAnchors=${report.evidence.protectedContinuityAnchorCount}")
        println("residualRisks=${report.evidence.residualRiskCount}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun createStoryboardPlanningBrief(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("accepted-script-packet"))
        val preflightPath = Path.of(options.requiredCliOption("storyboard-planning-preflight"))
        val report = StoryboardPlanningBriefService().build(
            acceptedScriptPacket = readCliJson<AcceptedScriptPacketHandoffPacket>(packetPath),
            storyboardPlanningPreflight = readCliJson<StoryboardPlanningPreflightReport>(preflightPath),
            briefId = options["brief"] ?: "",
            acceptedScriptPacketFile = packetPath.toString(),
            storyboardPlanningPreflightFile = preflightPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("briefId=${report.briefId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("preflightVerdict=${report.sourceReports.storyboardPlanningPreflightVerdict}")
        println("protectedContinuityAnchors=${report.protectedContinuityAnchorsSummary.protectedContinuityAnchorCount}")
        println("residualRisks=${report.remainingResidualRisks.size}")
        println("reviewChecklist=${report.reviewChecklist.size}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun validateStoryboardPacketDraft(options: Map<String, String>): Int {
        val briefPath = Path.of(options.requiredCliOption("storyboard-planning-brief"))
        val draftPath = Path.of(options.requiredCliOption("storyboard-packet-draft"))
        val report = StoryboardPacketDraftValidationService().validate(
            storyboardPlanningBrief = readCliJson<StoryboardPlanningBrief>(briefPath),
            storyboardPacketDraft = readCliJson<StoryboardPacketDraft>(draftPath),
            storyboardPlanningBriefFile = briefPath.toString(),
            storyboardPacketDraftFile = draftPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("storyboardPlanningBriefId=${report.storyboardPlanningBriefId}")
        println("storyboardPacketDraftId=${report.storyboardPacketDraftId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("frames=${report.evidence.frameCount}")
        println("protectedContinuityAnchors=${report.evidence.protectedContinuityAnchorReferencedCount}/${report.evidence.protectedContinuityAnchorCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("forbiddenOutputPaths=${report.evidence.forbiddenOutputPathCount}")
        println("externalGenerationApiHints=${report.evidence.externalGenerationApiHitCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun createStoryboardPacketReview(options: Map<String, String>): Int {
        val draftPath = Path.of(options.requiredCliOption("storyboard-packet-draft"))
        val reportPath = Path.of(options.requiredCliOption("storyboard-packet-draft-report"))
        val review = StoryboardPacketReviewService().create(
            storyboardPacketDraft = readCliJson<StoryboardPacketDraft>(draftPath),
            storyboardPacketDraftReport = readCliJson<StoryboardPacketDraftValidationReport>(reportPath),
            reviewId = options["review"] ?: "",
            storyboardPacketDraftFile = draftPath.toString(),
            storyboardPacketDraftReportFile = reportPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), review) }
        println("verdict=${review.verdict}")
        println("reviewId=${review.reviewId}")
        println("reviewStatus=${review.reviewStatus}")
        println("storyboardPacketDraftId=${review.storyboardPacketDraftId}")
        println("storyboardPlanningBriefId=${review.storyboardPlanningBriefId}")
        println("acceptedScriptPacketId=${review.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${review.scriptPacketDraftFile}")
        println("scriptPacketSha256=${review.scriptPacketDraftContentSha256}")
        println("frames=${review.frameCount}")
        println("assetCandidateRequirements=${review.assetCandidateRequirements.size}")
        println("protectedContinuityAnchors=${review.protectedContinuityAnchorsSummary.protectedContinuityAnchorCount}")
        println("residualRisks=${review.remainingResidualRisks.size}")
        println("reviewChecklist=${review.reviewChecklist.size}")
        println("nextAllowedDownstreamStep=${review.nextAllowedDownstreamStep}")
        println("blockedReason=${review.blockedReason}")
        println("findings=${review.findingCount}")
        review.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (review.verdict == "FAIL") 1 else 0
    }

    fun validateStoryboardPacketHumanReviewPreflight(options: Map<String, String>): Int {
        val reviewPath = Path.of(options.requiredCliOption("storyboard-packet-review"))
        val report = StoryboardPacketHumanReviewPreflightService().validate(
            storyboardPacketReview = readCliJson<StoryboardPacketReviewPacket>(reviewPath),
            preflightId = options["preflight"] ?: "",
            storyboardPacketReviewFile = reviewPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("reviewStatus=${report.reviewStatus}")
        println("reviewVerdict=${report.evidence.reviewPacketVerdict}")
        println("frames=${report.evidence.frameIdCount}/${report.evidence.frameCount}")
        println("reviewChecklist=${report.evidence.reviewChecklistCount}")
        println("protectedContinuityAnchors=${report.evidence.protectedContinuityAnchorCount}")
        println("residualRisks=${report.evidence.residualRiskCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun auditStoryboardStoryMovement(options: Map<String, String>): Int {
        val draftPath = Path.of(options.requiredCliOption("storyboard-packet-draft"))
        val reviewPath = Path.of(options.requiredCliOption("storyboard-packet-review"))
        val preflightPath = Path.of(options.requiredCliOption("storyboard-human-review-preflight"))
        val report = StoryboardStoryMovementAuditService().audit(
            storyboardPacketDraft = readCliJson<StoryboardPacketDraft>(draftPath),
            storyboardPacketReview = readCliJson<StoryboardPacketReviewPacket>(reviewPath),
            storyboardHumanReviewPreflight = readCliJson<StoryboardPacketHumanReviewPreflightReport>(preflightPath),
            storyboardPacketDraftFile = draftPath.toString(),
            storyboardPacketReviewFile = reviewPath.toString(),
            storyboardHumanReviewPreflightFile = preflightPath.toString(),
            auditId = options["audit"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("auditId=${report.auditId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("storyboardPacketDraftId=${report.storyboardPacketDraftId}")
        println("acceptedScriptPacketId=${report.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${report.scriptPacketDraftFile}")
        println("scriptPacketSha256=${report.scriptPacketDraftContentSha256}")
        println("frames=${report.evidence.uniqueFrameIdCount}/${report.evidence.frameCount}")
        println("movementEvidence=${report.evidence.movementEvidenceCount}")
        println("openingPressureEvidence=${report.evidence.openingPressureEvidenceCount}")
        println("actionEvidence=${report.evidence.actionEvidenceCount}")
        println("consequenceOrChoiceEvidence=${report.evidence.consequenceOrChoiceEvidenceCount}")
        println("protectedContinuityAnchors=${report.evidence.coveredContinuityAnchorCount}/${report.evidence.protectedContinuityAnchorCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("downstreamBoundary=${report.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun createReviewableStoryboardPacket(options: Map<String, String>): Int {
        val draftPath = Path.of(options.requiredCliOption("storyboard-packet-draft"))
        val reviewPath = Path.of(options.requiredCliOption("storyboard-packet-review"))
        val preflightPath = Path.of(options.requiredCliOption("storyboard-human-review-preflight"))
        val auditPath = Path.of(options.requiredCliOption("storyboard-story-movement-audit"))
        val packet = ReviewableStoryboardPacketService().create(
            storyboardPacketDraft = readCliJson<StoryboardPacketDraft>(draftPath),
            storyboardPacketReview = readCliJson<StoryboardPacketReviewPacket>(reviewPath),
            storyboardHumanReviewPreflight = readCliJson<StoryboardPacketHumanReviewPreflightReport>(preflightPath),
            storyboardStoryMovementAudit = readCliJson<StoryboardStoryMovementAuditReport>(auditPath),
            storyboardPacketDraftFile = draftPath.toString(),
            storyboardPacketReviewFile = reviewPath.toString(),
            storyboardHumanReviewPreflightFile = preflightPath.toString(),
            storyboardStoryMovementAuditFile = auditPath.toString(),
            packetId = options["packet"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), packet) }
        println("verdict=${packet.verdict}")
        println("packetId=${packet.packetId}")
        println("packetStatus=${packet.packetStatus}")
        println("storyboardPacketReviewId=${packet.storyboardPacketReviewId}")
        println("storyboardStoryMovementAuditId=${packet.storyboardStoryMovementAuditId}")
        println("storyboardPacketDraftId=${packet.storyboardPacketDraftId}")
        println("acceptedScriptPacketId=${packet.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${packet.scriptPacketDraftFile}")
        println("scriptPacketSha256=${packet.scriptPacketDraftContentSha256}")
        println("frames=${packet.evidence.uniqueFrameIdCount}/${packet.evidence.frameCount}")
        println("movementEvidence=${packet.evidence.movementEvidenceCount}")
        println("protectedContinuityAnchors=${packet.evidence.coveredContinuityAnchorCount}/${packet.evidence.protectedContinuityAnchorCount}")
        println("residualRisks=${packet.evidence.residualRiskCount}")
        println("reviewChecklist=${packet.evidence.reviewChecklistCount}")
        println("forbiddenClaims=${packet.evidence.forbiddenClaimCount}")
        println("downstreamBoundary=${packet.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${packet.nextAllowedDownstreamStep}")
        println("blockedReason=${packet.blockedReason}")
        println("findings=${packet.findingCount}")
        packet.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (packet.verdict == "FAIL") 1 else 0
    }

    fun createImageFacingDeliveryProof(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("reviewable-storyboard-packet"))
        val proof = ImageFacingDeliveryProofService().create(
            reviewableStoryboardPacket = readCliJson<ReviewableStoryboardPacket>(packetPath),
            reviewableStoryboardPacketFile = packetPath.toString(),
            proofId = options["proof"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), proof) }
        println("verdict=${proof.verdict}")
        println("proofId=${proof.proofId}")
        println("proofStatus=${proof.proofStatus}")
        println("reviewableStoryboardPacketId=${proof.reviewableStoryboardPacketId}")
        println("acceptedScriptPacketId=${proof.acceptedScriptPacketId}")
        println("scriptPacketDraftFile=${proof.scriptPacketDraftFile}")
        println("scriptPacketSha256=${proof.scriptPacketDraftContentSha256}")
        println("frames=${proof.evidence.uniqueFrameIdCount}/${proof.evidence.frameCount}")
        println("visualIntentFrames=${proof.evidence.frameWithVisualIntentCount}")
        println("cameraIntentFrames=${proof.evidence.frameWithCameraIntentCount}")
        println("mustShowFrames=${proof.evidence.frameWithMustShowCount}")
        println("doNotShowFrames=${proof.evidence.frameWithDoNotShowCount}")
        println("continuityAnchorFrames=${proof.evidence.frameWithContinuityAnchorCount}")
        println("assetCandidateRefs=${proof.evidence.assetCandidateRefCount}")
        println("forbiddenClaims=${proof.evidence.forbiddenClaimCount}")
        println("remoteReferences=${proof.evidence.remoteReferenceCount}")
        println("downstreamBoundary=${proof.downstreamBoundary}")
        println("nextAllowedDownstreamStep=${proof.nextAllowedDownstreamStep}")
        println("blockedReason=${proof.blockedReason}")
        println("findings=${proof.findingCount}")
        proof.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (proof.verdict == "FAIL") 1 else 0
    }

    fun validateStoryboardPacketHumanReviewDecision(options: Map<String, String>): Int {
        val preflightPath = Path.of(options.requiredCliOption("human-review-preflight"))
        val decisionPath = Path.of(options.requiredCliOption("human-review-decision"))
        val report = StoryboardPacketHumanReviewDecisionIntakeService().validate(
            humanReviewPreflight = readCliJson<StoryboardPacketHumanReviewPreflightReport>(preflightPath),
            humanReviewDecision = readCliJson<StoryboardPacketHumanReviewDecision>(decisionPath),
            humanReviewPreflightFile = preflightPath.toString(),
            humanReviewDecisionFile = decisionPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("decisionId=${report.decisionId}")
        println("preflightId=${report.preflightId}")
        println("storyboardPacketReviewId=${report.storyboardPacketReviewId}")
        println("decisionStatus=${report.decisionStatus}")
        println("decisionAllowedDownstream=${report.decisionAllowedDownstream}")
        println("decisionSource=${report.evidence.decisionSource}")
        println("checklist=${report.evidence.checklistPassedCount}/${report.evidence.checklistItemCount}")
        println("unresolvedBlockingIssues=${report.evidence.unresolvedBlockingIssueCount}")
        println("forbiddenClaims=${report.evidence.forbiddenClaimCount}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("blockedReason=${report.blockedReason}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }
}
