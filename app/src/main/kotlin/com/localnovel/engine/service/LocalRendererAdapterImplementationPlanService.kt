package com.localnovel.engine.service

import com.localnovel.engine.model.LocalRendererAdapterImplementationPanelPlan
import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanBoundaries
import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanCheck
import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanDraft
import com.localnovel.engine.model.LocalRendererAdapterImplementationPlanFinding
import com.localnovel.engine.model.LocalRendererAdapterImplementationStep
import com.localnovel.engine.model.LocalRendererAdapterRiskCheck
import com.localnovel.engine.model.LocalRendererAdapterTargetFieldMappingNote
import com.localnovel.engine.model.LocalRendererAdapterTargetManifestFieldMappingPlan
import com.localnovel.engine.model.LocalRendererAdapterVerificationStep
import com.localnovel.engine.model.LocalRendererAdapterWorkItem
import com.localnovel.engine.model.LocalRendererAdapterDryRunReport

class LocalRendererAdapterImplementationPlanService {
    private val requiredDryRunStatus = "LOCAL_RENDERER_ADAPTER_DRY_RUN_PASS"
    private val requiredDryRunBoundary = "local_renderer_adapter_dry_run_only_not_media_generation"
    private val requiredDryRunNextStep = "local_renderer_adapter_implementation_plan"
    private val passStatus = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_DRAFT"
    private val failStatus = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_FAIL"
    private val downstreamBoundary = "local_renderer_adapter_implementation_plan_only_not_implementation"
    private val nextAllowedDownstreamStep = "local_renderer_adapter_implementation_review"

    fun plan(
        dryRunReport: LocalRendererAdapterDryRunReport,
        dryRunReportFile: String = "",
        planId: String = "",
    ): LocalRendererAdapterImplementationPlanDraft {
        val findings = mutableListOf<LocalRendererAdapterImplementationPlanFinding>()
        val checks = buildChecks(dryRunReport, dryRunReportFile, findings)
        val verdict = if (findings.hasBlocking()) "FAIL" else "PASS"
        val resolvedPlanId = planId.ifBlank {
            dryRunReport.dryRunId.takeIf { it.isNotBlank() }?.let { "$it-implementation-plan-v0" }.orEmpty()
        }

        return LocalRendererAdapterImplementationPlanDraft(
            verdict = verdict,
            status = if (verdict == "PASS") passStatus else failStatus,
            planId = resolvedPlanId,
            sourceDryRunReportFile = dryRunReportFile,
            sourceDryRunId = dryRunReport.dryRunId,
            sourceHandoffId = dryRunReport.sourceHandoffId,
            sourceReviewId = dryRunReport.sourceReviewId,
            sourceStoryInputPackId = dryRunReport.sourceStoryInputPackId,
            sourceValidationChecks = checks,
            requiredAdapterWorkItems = if (verdict == "PASS") requiredWorkItems(dryRunReport) else emptyList(),
            targetManifestFieldMappingPlan = if (verdict == "PASS") {
                targetManifestFieldMappingPlan(dryRunReport)
            } else {
                LocalRendererAdapterTargetManifestFieldMappingPlan()
            },
            implementationSteps = if (verdict == "PASS") implementationSteps() else emptyList(),
            riskChecklist = if (verdict == "PASS") riskChecklist() else emptyList(),
            verificationPlan = if (verdict == "PASS") verificationPlan() else emptyList(),
            boundaries = outputBoundaries(dryRunReport),
            downstreamBoundary = downstreamBoundary,
            nextAllowedDownstreamStep = if (verdict == "PASS") nextAllowedDownstreamStep else "",
            blockedReason = if (verdict == "PASS") "" else findings
                .filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }
                .joinToString("; ") { it.ruleId },
            findingCount = findings.size,
            findings = findings,
        )
    }

    private fun buildChecks(
        report: LocalRendererAdapterDryRunReport,
        dryRunReportFile: String,
        findings: MutableList<LocalRendererAdapterImplementationPlanFinding>,
    ): List<LocalRendererAdapterImplementationPlanCheck> =
        listOf(
            check(
                checkId = "explicit-source-file",
                category = "source_validation",
                passed = dryRunReportFile.isNotBlank(),
                message = "Implementation plan requires an explicit Phase 7 dry-run report file.",
                evidence = listOf("dryRunReportFile=$dryRunReportFile"),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_SOURCE_FILE_MISSING",
                path = "$.inputs.dryRunReport",
                suggestedFix = "Pass --dry-run-report and --out explicitly.",
            ),
            check(
                checkId = "phase7-state",
                category = "source_validation",
                passed = report.verdict == "PASS" &&
                    report.status == requiredDryRunStatus &&
                    report.downstreamBoundary == requiredDryRunBoundary &&
                    report.nextAllowedDownstreamStep == requiredDryRunNextStep,
                message = "Implementation plan requires PASS Phase 7 dry-run state and the implementation-plan downstream step.",
                evidence = listOf(
                    "verdict=${report.verdict}",
                    "status=${report.status}",
                    "downstreamBoundary=${report.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_PHASE7_STATE_INVALID",
                path = "$.phase7",
                suggestedFix = "Use the accepted PASS Phase 7 local renderer adapter dry-run report.",
            ),
            check(
                checkId = "source-ids-present",
                category = "source_linkage",
                passed = report.dryRunId.isNotBlank() &&
                    report.sourceHandoffId.isNotBlank() &&
                    report.sourceReviewId.isNotBlank() &&
                    report.sourceStoryInputPackId.isNotBlank(),
                message = "Implementation plan requires dry-run, handoff, review, and story input source ids.",
                evidence = listOf(
                    "dryRunId=${report.dryRunId}",
                    "sourceHandoffId=${report.sourceHandoffId}",
                    "sourceReviewId=${report.sourceReviewId}",
                    "sourceStoryInputPackId=${report.sourceStoryInputPackId}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_SOURCE_ID_MISSING",
                path = "$.sourceIds",
                suggestedFix = "Regenerate the Phase 7 dry-run report from accepted Phase 5/6 sources.",
            ),
            check(
                checkId = "local-boundaries",
                category = "boundary",
                passed = localBoundariesOk(report),
                message = "Implementation plan requires local-only no-media boundaries and no manifest generation.",
                evidence = listOf(
                    "mediaGenerated=${report.boundaries.mediaGenerated}",
                    "rendererInvocation=${report.boundaries.rendererInvocation}",
                    "providerInvocation=${report.boundaries.providerInvocation}",
                    "ttsInvocation=${report.boundaries.ttsInvocation}",
                    "manifestGenerated=${report.rendererCompatibilitySummary.manifestGenerated}",
                    "noExternalPaidProviderTools=${report.boundaries.noExternalPaidProviderTools}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_BOUNDARY_FAIL",
                path = "$.boundaries",
                suggestedFix = "Use local-only Phase 7 JSON with no provider/API, renderer, TTS, media, release, production lock, or manifest generation.",
            ),
            check(
                checkId = "not-renderer-ready",
                category = "renderer_compatibility",
                passed = !report.rendererCompatibilitySummary.ready,
                message = "Implementation plan requires localRendererReady=false to remain preserved.",
                evidence = listOf("localRendererReady=${report.rendererCompatibilitySummary.ready}"),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_READY_TRUE_FORBIDDEN",
                path = "$.rendererCompatibilitySummary.ready",
                suggestedFix = "Keep renderer readiness false until a future explicit adapter implementation and renderer dry-run pass.",
            ),
            check(
                checkId = "phase7-checks-pass",
                category = "review_gate",
                passed = report.checks.isNotEmpty() &&
                    report.checks.filter { it.requiredForDryRun }.all { it.status == "PASS" } &&
                    report.findingCount == 0 &&
                    report.findings.isEmpty(),
                message = "Implementation plan requires all required Phase 7 checks to remain PASS with no findings.",
                evidence = listOf(
                    "checks=${report.checks.size}",
                    "requiredFail=${report.checks.count { it.requiredForDryRun && it.status != "PASS" }}",
                    "findingCount=${report.findingCount}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_PHASE7_CHECKS_NOT_PASS",
                path = "$.checks",
                suggestedFix = "Resolve Phase 7 dry-run findings before planning adapter implementation.",
            ),
            check(
                checkId = "mapping-summary-pass",
                category = "adapter_mapping",
                passed = report.adapterMappingSummary.mappedPanelCount > 0 &&
                    report.adapterMappingSummary.unmappedPanelCount == 0 &&
                    report.adapterMappingSummary.missingRequiredFieldCount == 0 &&
                    report.adapterMappingSummary.qaFail == 0,
                message = "Implementation plan requires mapped panels, zero missing required fields, and zero QA failures.",
                evidence = listOf(
                    "mappedPanelCount=${report.adapterMappingSummary.mappedPanelCount}",
                    "unmappedPanelCount=${report.adapterMappingSummary.unmappedPanelCount}",
                    "missingRequiredFieldCount=${report.adapterMappingSummary.missingRequiredFieldCount}",
                    "qaFail=${report.adapterMappingSummary.qaFail}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_MAPPING_NOT_READY",
                path = "$.adapterMappingSummary",
                suggestedFix = "Use a PASS Phase 7 dry-run report with mapped panels and no missing required fields.",
            ),
            check(
                checkId = "mapping-notes-planning-only",
                category = "manifest_boundary",
                passed = report.dryRunMappingPlan.isNotEmpty() &&
                    report.dryRunMappingPlan.all { !it.manifestGenerated },
                message = "Implementation plan requires Phase 7 mapping notes to be planning-only with no manifest generation.",
                evidence = listOf(
                    "mappingNotes=${report.dryRunMappingPlan.size}",
                    "manifestGeneratedNotes=${report.dryRunMappingPlan.count { it.manifestGenerated }}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_MANIFEST_GENERATED_FORBIDDEN",
                path = "$.dryRunMappingPlan",
                suggestedFix = "Use a dry-run report whose mapping notes do not generate ComicVideoRenderHandoffManifest.",
            ),
        )

    private fun localBoundariesOk(report: LocalRendererAdapterDryRunReport): Boolean =
        report.boundaries.fileProtocolJsonOnly &&
            report.boundaries.noExternalPaidProviderTools &&
            report.boundaries.providerInvocation == "none" &&
            report.boundaries.ttsInvocation == "none" &&
            !report.boundaries.mediaGenerated &&
            report.boundaries.rendererInvocation == "none" &&
            !report.boundaries.directSqliteWrites &&
            !report.boundaries.providerApiCalls &&
            !report.boundaries.finalMediaApproval &&
            !report.boundaries.release &&
            !report.boundaries.canonRegistryOrProductionLock &&
            !report.rendererCompatibilitySummary.manifestGenerated

    private fun outputBoundaries(report: LocalRendererAdapterDryRunReport): LocalRendererAdapterImplementationPlanBoundaries =
        LocalRendererAdapterImplementationPlanBoundaries(
            fileProtocolJsonOnly = report.boundaries.fileProtocolJsonOnly,
            noExternalPaidProviderTools = report.boundaries.noExternalPaidProviderTools,
            providerInvocation = if (report.boundaries.providerInvocation == "none") "none" else "present",
            ttsInvocation = if (report.boundaries.ttsInvocation == "none") "none" else "present",
            mediaGenerated = false,
            rendererInvocation = "none",
            directSqliteWrites = false,
            providerApiCalls = false,
            adapterImplementationExecuted = false,
            manifestGenerated = false,
            finalMediaApproval = false,
            providerMediaHandoff = false,
            release = false,
            canonRegistryOrProductionLock = false,
        )

    private fun requiredWorkItems(report: LocalRendererAdapterDryRunReport): List<LocalRendererAdapterWorkItem> =
        listOf(
            LocalRendererAdapterWorkItem(
                workItemId = "adapter-input-contract",
                title = "Define the local adapter input contract.",
                description = "Use the Phase 7 dry-run report as the only accepted source for a future adapter implementation.",
                sourceEvidence = listOf(
                    "sourceDryRunId=${report.dryRunId}",
                    "sourceHandoffId=${report.sourceHandoffId}",
                    "sourceReviewId=${report.sourceReviewId}",
                ),
                targetOutcome = "A future adapter implementation can verify source ids and dry-run boundaries before mapping.",
                mustNotDo = commonMustNotDo(),
            ),
            LocalRendererAdapterWorkItem(
                workItemId = "panel-timeline-mapper",
                title = "Plan panel timeline field mapping.",
                description = "Map Phase 7 panel notes to future ComicVideoRenderPanelTimelineItem fields.",
                sourceEvidence = listOf(
                    "mappedPanelCount=${report.adapterMappingSummary.mappedPanelCount}",
                    "targetDurationMillis=${report.adapterMappingSummary.targetDurationMillis}",
                ),
                targetOutcome = "Future implementation has explicit panelTimeline mapping rules before any manifest is generated.",
                mustNotDo = commonMustNotDo(),
            ),
            LocalRendererAdapterWorkItem(
                workItemId = "voice-subtitle-mapper",
                title = "Plan voice and subtitle timeline field mapping.",
                description = "Map Phase 7 audio cue and caption/dialogue notes to future voiceTimeline and subtitleTimeline fields.",
                sourceEvidence = listOf(
                    "audioCueReferenceCount=${report.adapterMappingSummary.audioCueReferenceCount}",
                    "panelCount=${report.adapterMappingSummary.panelCount}",
                ),
                targetOutcome = "Future implementation can create deterministic local voice/subtitle metadata without TTS.",
                mustNotDo = commonMustNotDo(),
            ),
            LocalRendererAdapterWorkItem(
                workItemId = "render-settings-policy",
                title = "Plan render settings defaults.",
                description = "Choose deterministic metadata-only render settings for a future adapter implementation.",
                sourceEvidence = listOf("rendererReady=${report.rendererCompatibilitySummary.ready}"),
                targetOutcome = "Future implementation has explicit frame rate, resolution, and renderer boundary policy.",
                mustNotDo = commonMustNotDo(),
            ),
            LocalRendererAdapterWorkItem(
                workItemId = "fail-stop-verification",
                title = "Plan fail-stop verification before any media step.",
                description = "Require a later adapter implementation review and renderer dry-run before media generation can be considered.",
                sourceEvidence = listOf("nextAllowedDownstreamStep=$requiredDryRunNextStep"),
                targetOutcome = "The future adapter path remains governed and cannot jump directly to MP4 generation.",
                mustNotDo = commonMustNotDo(),
            ),
        )

    private fun targetManifestFieldMappingPlan(
        report: LocalRendererAdapterDryRunReport,
    ): LocalRendererAdapterTargetManifestFieldMappingPlan =
        LocalRendererAdapterTargetManifestFieldMappingPlan(
            planningOnly = true,
            manifestGenerated = false,
            targetManifestType = "ComicVideoRenderHandoffManifest",
            targetManifestFieldMappingPlanNote =
                "Planning notes only. ComicVideoRenderHandoffManifest is not generated in Phase 8.",
            panelTimelineFields = panelTimelineFields(),
            voiceTimelineFields = voiceTimelineFields(),
            subtitleTimelineFields = subtitleTimelineFields(),
            renderSettingsFields = renderSettingsFields(report),
            perPanelMappingNotes = report.dryRunMappingPlan.map { note ->
                LocalRendererAdapterImplementationPanelPlan(
                    order = note.order,
                    panelId = note.panelId,
                    sourceSceneId = note.sourceSceneId,
                    sourceBeatId = note.sourceBeatId,
                    durationMillis = note.durationMillis,
                    plannedPanelTimelineMapping = panelTimelineFields().map {
                        it.copy(
                            sourcePath = it.sourcePath.replace("{order}", note.order.toString()),
                        )
                    },
                    plannedVoiceTimelineMapping = voiceTimelineFields().map {
                        it.copy(
                            sourcePath = it.sourcePath.replace("{order}", note.order.toString()),
                        )
                    },
                    plannedSubtitleTimelineMapping = subtitleTimelineFields().map {
                        it.copy(
                            sourcePath = it.sourcePath.replace("{order}", note.order.toString()),
                        )
                    },
                    manifestGenerated = false,
                    note = "Per-panel implementation plan only; ComicVideoRenderHandoffManifest is not generated.",
                )
            },
        )

    private fun panelTimelineFields(): List<LocalRendererAdapterTargetFieldMappingNote> =
        listOf(
            targetField("panelTimeline", "panelId", "$.dryRunMappingPlan[{order}].panelId", "Carry the stable panel id."),
            targetField("panelTimeline", "sourceFrameId", "$.dryRunMappingPlan[{order}].proposedPanelTimelineFields.wouldMapSourceFrameIdFrom", "Resolve from local visual prompt card id in a future adapter."),
            targetField("panelTimeline", "sceneReference", "$.dryRunMappingPlan[{order}].sourceSceneId", "Carry local source scene id."),
            targetField("panelTimeline", "beatReference", "$.dryRunMappingPlan[{order}].sourceBeatId", "Carry local source beat id."),
            targetField("panelTimeline", "visualIntent", "$.dryRunMappingPlan[{order}].proposedPanelTimelineFields.wouldMapVisualIntentFrom", "Map visual intent text only; do not call an image provider."),
            targetField("panelTimeline", "cameraIntent", "$.dryRunMappingPlan[{order}].proposedPanelTimelineFields.wouldMapCameraIntentFrom", "Choose local deterministic fallback camera intent in a future adapter."),
            targetField("panelTimeline", "startMillis", "$.dryRunMappingPlan[{order}].startMillis", "Carry timing from Phase 7 notes."),
            targetField("panelTimeline", "endMillis", "$.dryRunMappingPlan[{order}].endMillis", "Carry timing from Phase 7 notes."),
            targetField("panelTimeline", "displayDurationMillis", "$.dryRunMappingPlan[{order}].durationMillis", "Carry duration from Phase 7 notes."),
            targetField("panelTimeline", "assetCandidateRefs", "$.dryRunMappingPlan[{order}].proposedPanelTimelineFields.wouldMapAssetCandidateRefsFrom", "Resolve local asset references only in a future adapter."),
        )

    private fun voiceTimelineFields(): List<LocalRendererAdapterTargetFieldMappingNote> =
        listOf(
            targetField("voiceTimeline", "cueId", "$.dryRunMappingPlan[{order}].proposedVoiceTimelineFields.wouldMapCueIdFrom", "Carry local audio cue card id."),
            targetField("voiceTimeline", "panelId", "$.dryRunMappingPlan[{order}].panelId", "Link voice cue to panel id."),
            targetField("voiceTimeline", "voiceLabel", "$.dryRunMappingPlan[{order}].proposedVoiceTimelineFields.wouldNeedVoiceLabelsFrom", "Choose deterministic local voice label policy in a future adapter; do not invoke TTS."),
            targetField("voiceTimeline", "text", "$.dryRunMappingPlan[{order}].proposedVoiceTimelineFields.wouldMapTextFrom", "Carry dialogue/audio cue text from local JSON."),
            targetField("voiceTimeline", "startMillis", "$.dryRunMappingPlan[{order}].startMillis", "Carry timing from Phase 7 notes."),
            targetField("voiceTimeline", "endMillis", "$.dryRunMappingPlan[{order}].endMillis", "Carry timing from Phase 7 notes."),
            targetField("voiceTimeline", "durationMillis", "$.dryRunMappingPlan[{order}].durationMillis", "Carry duration from Phase 7 notes."),
        )

    private fun subtitleTimelineFields(): List<LocalRendererAdapterTargetFieldMappingNote> =
        listOf(
            targetField("subtitleTimeline", "cueId", "$.dryRunMappingPlan[{order}].proposedSubtitleTimelineFields.wouldMapCueIdFrom", "Create deterministic local subtitle cue id in a future adapter."),
            targetField("subtitleTimeline", "panelId", "$.dryRunMappingPlan[{order}].panelId", "Link subtitle cue to panel id."),
            targetField("subtitleTimeline", "audioCueId", "$.dryRunMappingPlan[{order}].proposedSubtitleTimelineFields.wouldMapAudioCueIdFrom", "Carry local audio cue id."),
            targetField("subtitleTimeline", "text", "$.dryRunMappingPlan[{order}].proposedSubtitleTimelineFields.wouldMapTextFrom", "Carry caption/dialogue text from local JSON."),
            targetField("subtitleTimeline", "startMillis", "$.dryRunMappingPlan[{order}].startMillis", "Carry timing from Phase 7 notes."),
            targetField("subtitleTimeline", "endMillis", "$.dryRunMappingPlan[{order}].endMillis", "Carry timing from Phase 7 notes."),
        )

    private fun renderSettingsFields(
        report: LocalRendererAdapterDryRunReport,
    ): List<LocalRendererAdapterTargetFieldMappingNote> =
        listOf(
            targetField("renderSettings", "timelineStatus", "$.adapterMappingSummary", "Future adapter should use metadata_only_timing_plan until media generation is explicitly assigned."),
            targetField("renderSettings", "totalDurationMillis", "$.adapterMappingSummary.targetDurationMillis", "Use ${report.adapterMappingSummary.targetDurationMillis} ms from Phase 7."),
            targetField("renderSettings", "frameRate", "constant:24", "Use local deterministic default unless 01/user assigns a different local renderer policy."),
            targetField("renderSettings", "resolution", "constant:1920x1080", "Use local deterministic default matching current local preview evidence."),
            targetField("renderSettings", "rendererBoundary", "constant:local_renderer_adapter_planned_only", "Planning-only boundary; do not run renderer."),
        )

    private fun implementationSteps(): List<LocalRendererAdapterImplementationStep> =
        listOf(
            LocalRendererAdapterImplementationStep(
                order = 1,
                stepId = "define-adapter-model",
                description = "Future task defines adapter output models and validation checks, still file-protocol JSON only.",
                acceptance = listOf("No renderer execution.", "No media generation.", "No provider/API calls."),
                failStop = listOf("Stop if the model would bypass Phase 8 review.", "Stop if it claims final media approval."),
            ),
            LocalRendererAdapterImplementationStep(
                order = 2,
                stepId = "implement-mapping-service",
                description = "Future task maps Phase 8-approved plan data into adapter metadata with strict fail-stop checks.",
                acceptance = listOf("All source ids preserved.", "All boundary fields remain local-only.", "Manifest generation only if explicitly assigned later."),
                failStop = listOf("Stop on missing panel/audio/subtitle mapping.", "Stop on any media/provider/renderer invocation."),
            ),
            LocalRendererAdapterImplementationStep(
                order = 3,
                stepId = "add-focused-tests",
                description = "Future task adds happy-path and fail-stop tests for adapter implementation.",
                acceptance = listOf("Tests prove no media generation.", "Tests prove manifest boundary is explicit."),
                failStop = listOf("Stop if tests require ffmpeg/TTS/provider tools."),
            ),
            LocalRendererAdapterImplementationStep(
                order = 4,
                stepId = "run-review-gate",
                description = "Future task sends adapter implementation result to a local JSON review gate before renderer dry-run.",
                acceptance = listOf("Next downstream step is review-only.", "No MP4 readiness claim."),
                failStop = listOf("Stop if review is skipped.", "Stop if output jumps to renderer execution."),
            ),
        )

    private fun riskChecklist(): List<LocalRendererAdapterRiskCheck> =
        listOf(
            LocalRendererAdapterRiskCheck(
                riskId = "manifest-generation-drift",
                risk = "Planning notes could be mistaken for an actual ComicVideoRenderHandoffManifest.",
                mitigation = "Keep manifestGenerated=false and planningOnly=true in every Phase 8 output.",
                failStop = "Stop if a future slice generates manifest JSON without explicit 01/user assignment.",
            ),
            LocalRendererAdapterRiskCheck(
                riskId = "renderer-readiness-overclaim",
                risk = "A PASS plan could be misread as renderer readiness or MP4 readiness.",
                mitigation = "Keep downstream boundary to implementation review only and state no media generation.",
                failStop = "Stop if docs or output claim final video, MP4 readiness, or renderer readiness.",
            ),
            LocalRendererAdapterRiskCheck(
                riskId = "provider-or-tts-drift",
                risk = "Voice/prompt planning could drift into external provider or TTS invocation.",
                mitigation = "Keep providerInvocation=none, ttsInvocation=none, and noExternalPaidProviderTools=true.",
                failStop = "Stop if any provider/API/TTS command is introduced.",
            ),
        )

    private fun verificationPlan(): List<LocalRendererAdapterVerificationStep> =
        listOf(
            LocalRendererAdapterVerificationStep(
                verificationId = "focused-tests",
                commandOrCheck = "./gradlew.bat test --tests \"com.localnovel.engine.LocalRendererAdapterImplementationPlan*\"",
                expectedResult = "PASS for service and CLI focused tests.",
            ),
            LocalRendererAdapterVerificationStep(
                verificationId = "sample-cli",
                commandOrCheck = "plan-local-renderer-adapter-implementation --dry-run-report <phase7-report.json> --plan <id> --out <plan.json>",
                expectedResult = "Sample plan JSON has LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_DRAFT and PASS.",
            ),
            LocalRendererAdapterVerificationStep(
                verificationId = "boundary-parse",
                commandOrCheck = "Parse sample JSON and verify mediaGenerated=false, rendererInvocation=none, providerInvocation=none, ttsInvocation=none, manifestGenerated=false.",
                expectedResult = "All local-only boundary fields remain safe.",
            ),
        )

    private fun targetField(
        section: String,
        field: String,
        sourcePath: String,
        note: String,
    ): LocalRendererAdapterTargetFieldMappingNote =
        LocalRendererAdapterTargetFieldMappingNote(
            targetSection = section,
            targetField = field,
            sourcePath = sourcePath,
            implementationNote = note,
            required = true,
            generatedNow = false,
        )

    private fun commonMustNotDo(): List<String> =
        listOf(
            "Do not implement adapter execution in Phase 8.",
            "Do not generate ComicVideoRenderHandoffManifest in Phase 8.",
            "Do not run renderer/ffmpeg/TTS.",
            "Do not generate media or claim final media approval.",
            "Do not call provider/API or create provider/media handoff.",
            "Do not claim canon, registry, release, approval, or production lock.",
        )

    private fun check(
        checkId: String,
        category: String,
        passed: Boolean,
        message: String,
        evidence: List<String>,
        findings: MutableList<LocalRendererAdapterImplementationPlanFinding>,
        ruleId: String,
        path: String,
        suggestedFix: String,
    ): LocalRendererAdapterImplementationPlanCheck {
        if (!passed) {
            findings += finding(
                ruleId = ruleId,
                category = category,
                severity = "CRITICAL",
                path = path,
                message = message,
                evidence = evidence,
                suggestedFix = suggestedFix,
            )
        }
        return LocalRendererAdapterImplementationPlanCheck(
            checkId = checkId,
            status = if (passed) "PASS" else "FAIL",
            category = category,
            message = message,
            evidence = evidence,
        )
    }

    private fun List<LocalRendererAdapterImplementationPlanFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): LocalRendererAdapterImplementationPlanFinding =
        LocalRendererAdapterImplementationPlanFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
}
