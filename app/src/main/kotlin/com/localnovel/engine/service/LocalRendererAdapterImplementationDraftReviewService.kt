package com.localnovel.engine.service

import com.localnovel.engine.model.LocalRendererAdapterImplementationDraft
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftBoundaries
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftReviewBoundarySummary
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftReviewCheck
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftReviewFinding
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftReviewRendererCompatibilitySummary
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftReviewReport
import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftReviewSummary
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class LocalRendererAdapterImplementationDraftReviewService {
    private val proposedCommand = "review-local-renderer-adapter-implementation-draft"
    private val requiredDraftStatus = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT"
    private val requiredDraftBoundary = "local_renderer_adapter_implementation_draft_only_not_manifest_generation"
    private val requiredDraftNextStep = "local_renderer_adapter_implementation_draft_review"
    private val passStatus = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_PASS"
    private val failStatus = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_FAIL"
    private val reviewBoundary = "local_renderer_adapter_implementation_draft_review_only_not_manifest_generation"
    private val nextAllowedDownstreamStep = "local_renderer_manifest_draft_plan"
    private val requiredBoundaryFlags = listOf(
        "mediaGenerated=false",
        "rendererInvocation=none",
        "providerInvocation=none",
        "ttsInvocation=none",
        "manifestGenerated=false",
        "adapterImplementationExecuted=false",
        "noExternalPaidProviderTools=true",
        "rendererReady=false",
        "mp4ReadinessClaimed=false",
    )

    fun reviewFile(
        draftPath: Path,
        outPath: Path? = null,
        reviewId: String = "",
    ): LocalRendererAdapterImplementationDraftReviewReport {
        val draft = JsonSupport.json.decodeFromString<LocalRendererAdapterImplementationDraft>(
            Files.readString(draftPath).removePrefix("\uFEFF"),
        )
        val report = review(
            draft = draft,
            draftFile = draftPath.toString(),
            reviewId = reviewId,
        )
        if (outPath != null) {
            outPath.parent?.let { Files.createDirectories(it) }
            Files.writeString(outPath, JsonSupport.json.encodeToString(report))
        }
        return report
    }

    fun review(
        draft: LocalRendererAdapterImplementationDraft,
        draftFile: String = "",
        reviewId: String = "",
    ): LocalRendererAdapterImplementationDraftReviewReport {
        val findings = mutableListOf<LocalRendererAdapterImplementationDraftReviewFinding>()
        val draftSummary = draftSummary(draft)
        val boundarySummary = boundarySummary(draft.boundaries)
        val checks = buildChecks(draft, draftFile, draftSummary, boundarySummary, findings)
        val verdict = if (findings.hasBlocking()) "FAIL" else "PASS"
        val resolvedReviewId = reviewId.ifBlank {
            draft.draftId.takeIf { it.isNotBlank() }?.let { "$it-review-v0" }.orEmpty()
        }

        return LocalRendererAdapterImplementationDraftReviewReport(
            verdict = verdict,
            status = if (verdict == "PASS") passStatus else failStatus,
            reviewId = resolvedReviewId,
            sourceDraftFile = draftFile,
            sourceDraftId = draft.draftId,
            sourcePlanId = draft.sourcePlanId,
            sourcePlanReviewId = draft.sourcePlanReviewId,
            sourceDryRunId = draft.sourceDryRunId,
            sourceHandoffId = draft.sourceHandoffId,
            sourceHandoffReviewId = draft.sourceHandoffReviewId,
            sourceStoryInputPackId = draft.sourceStoryInputPackId,
            checks = checks,
            draftSummary = draftSummary,
            boundarySummary = boundarySummary,
            rendererCompatibilitySummary = rendererCompatibilitySummary(),
            boundaries = outputBoundaries(),
            downstreamBoundary = reviewBoundary,
            nextAllowedDownstreamStep = if (verdict == "PASS") nextAllowedDownstreamStep else "",
            blockedReason = if (verdict == "PASS") "" else findings
                .filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }
                .joinToString("; ") { it.ruleId },
            findingCount = findings.size,
            findings = findings,
        )
    }

    private fun buildChecks(
        draft: LocalRendererAdapterImplementationDraft,
        draftFile: String,
        draftSummary: LocalRendererAdapterImplementationDraftReviewSummary,
        boundarySummary: LocalRendererAdapterImplementationDraftReviewBoundarySummary,
        findings: MutableList<LocalRendererAdapterImplementationDraftReviewFinding>,
    ): List<LocalRendererAdapterImplementationDraftReviewCheck> =
        listOf(
            check(
                checkId = "source-draft-present",
                category = "source_validation",
                passed = draftFile.isNotBlank() &&
                    draft.draftId.isNotBlank() &&
                    draft.sourceStoryInputPackId.isNotBlank() &&
                    draft.sourceHandoffId.isNotBlank() &&
                    draft.sourceHandoffReviewId.isNotBlank() &&
                    draft.sourceDryRunId.isNotBlank() &&
                    draft.sourcePlanId.isNotBlank() &&
                    draft.sourcePlanReviewId.isNotBlank(),
                message = "Implementation draft review requires an explicit Phase 10 draft file and non-empty source ids.",
                evidence = listOf(
                    "proposedCommand=$proposedCommand",
                    "draftFile=$draftFile",
                    "draftId=${draft.draftId}",
                    "sourceStoryInputPackId=${draft.sourceStoryInputPackId}",
                    "sourceHandoffId=${draft.sourceHandoffId}",
                    "sourceHandoffReviewId=${draft.sourceHandoffReviewId}",
                    "sourceDryRunId=${draft.sourceDryRunId}",
                    "sourcePlanId=${draft.sourcePlanId}",
                    "sourcePlanReviewId=${draft.sourcePlanReviewId}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_SOURCE_MISSING",
                path = "$.sourceDraft",
                suggestedFix = "Use a complete PASS Phase 10 implementation draft JSON and pass it explicitly.",
            ),
            check(
                checkId = "draft-state",
                category = "source_validation",
                passed = draft.verdict == "PASS" &&
                    draft.status == requiredDraftStatus &&
                    draft.downstreamBoundary == requiredDraftBoundary &&
                    draft.nextAllowedDownstreamStep == requiredDraftNextStep,
                message = "Implementation draft review requires PASS Phase 10 draft state and draft-review downstream step.",
                evidence = listOf(
                    "verdict=${draft.verdict}",
                    "status=${draft.status}",
                    "downstreamBoundary=${draft.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${draft.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_STATE_INVALID",
                path = "$.draftState",
                suggestedFix = "Use the accepted PASS Phase 10 implementation draft.",
            ),
            check(
                checkId = "local-boundaries",
                category = "boundary",
                passed = localBoundariesOk(draft.boundaries),
                message = "Implementation draft review requires local-only no-media boundaries with no adapter execution or manifest generation.",
                evidence = listOf(
                    "mediaGenerated=${draft.boundaries.mediaGenerated}",
                    "rendererInvocation=${draft.boundaries.rendererInvocation}",
                    "providerInvocation=${draft.boundaries.providerInvocation}",
                    "ttsInvocation=${draft.boundaries.ttsInvocation}",
                    "manifestGenerated=${draft.boundaries.manifestGenerated}",
                    "adapterImplementationExecuted=${draft.boundaries.adapterImplementationExecuted}",
                    "noExternalPaidProviderTools=${draft.boundaries.noExternalPaidProviderTools}",
                    "rendererReady=${draft.boundaries.rendererReady}",
                    "mp4ReadinessClaimed=${draft.boundaries.mp4ReadinessClaimed}",
                    "boundarySummary.mediaGenerated=${boundarySummary.mediaGenerated}",
                    "boundarySummary.manifestGenerated=${boundarySummary.manifestGenerated}",
                    "boundarySummary.adapterImplementationExecuted=${boundarySummary.adapterImplementationExecuted}",
                ) + requiredBoundaryFlags,
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_BOUNDARY_FAIL",
                path = "$.boundaries",
                suggestedFix = "Use a Phase 10 draft with no provider/API, renderer, TTS, media, adapter execution, manifest generation, release, readiness claim, or production lock.",
            ),
            check(
                checkId = "no-finalization-boundaries",
                category = "boundary",
                passed = !draft.boundaries.finalMediaApproval &&
                    !draft.boundaries.providerMediaHandoff &&
                    !draft.boundaries.canonRegistryOrProductionLock,
                message = "Implementation draft review is not final media approval, not provider/media handoff, and no production lock.",
                evidence = listOf(
                    "finalMediaApproval=${draft.boundaries.finalMediaApproval}",
                    "providerMediaHandoff=${draft.boundaries.providerMediaHandoff}",
                    "canonRegistryOrProductionLock=${draft.boundaries.canonRegistryOrProductionLock}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_FINALIZATION_FORBIDDEN",
                path = "$.boundaries.finalization",
                suggestedFix = "Keep Phase 11A as a local JSON review gate skeleton with no final media approval, no provider/media handoff, and no production lock.",
            ),
            check(
                checkId = "source-checks-pass",
                category = "review_gate",
                passed = draft.checks.isNotEmpty() &&
                    draft.checks.filter { it.requiredForDraft }.all { it.status == "PASS" },
                message = "Implementation draft review requires non-empty Phase 10 checks and all required checks PASS.",
                evidence = listOf(
                    "checks=${draft.checks.size}",
                    "requiredFail=${draft.checks.count { it.requiredForDraft && it.status != "PASS" }}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_CHECKS_NOT_PASS",
                path = "$.checks",
                suggestedFix = "Resolve Phase 10 draft checks before reviewing the draft.",
            ),
            check(
                checkId = "draft-sections-present",
                category = "draft_completeness",
                passed = draft.implementationTasks.isNotEmpty() &&
                    draft.adapterMappingDraft.panelDrafts.isNotEmpty() &&
                    draftSummary.mappingDraftFields > 0 &&
                    draft.verificationPlan.isNotEmpty(),
                message = "Implementation draft review requires implementation tasks, mapping draft, and verification plan.",
                evidence = listOf(
                    "implementationTasks=${draft.implementationTasks.size}",
                    "mappingDraftPanels=${draft.adapterMappingDraft.panelDrafts.size}",
                    "mappingDraftFields=${draftSummary.mappingDraftFields}",
                    "verificationSteps=${draft.verificationPlan.size}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_SECTIONS_MISSING",
                path = "$.implementationDraftSections",
                suggestedFix = "Regenerate Phase 10 draft with implementationTasks, adapterMappingDraft, and verificationPlan.",
            ),
            check(
                checkId = "mapping-remains-draft-only",
                category = "manifest_boundary",
                passed = draft.adapterMappingDraft.draftOnly &&
                    !draft.adapterMappingDraft.manifestGenerated &&
                    draft.adapterMappingDraft.panelDrafts.all { !it.manifestGenerated && !it.rendererReady } &&
                    draft.adapterMappingDraft.panelDrafts
                        .flatMap { it.targetPanelTimelineDraft + it.targetVoiceTimelineDraft + it.targetSubtitleTimelineDraft }
                        .all { !it.generatedNow } &&
                    !draft.adapterMappingDraft.renderSettingsDraft.generatedNow,
                message = "Implementation draft review allows ComicVideoRenderHandoffManifest only as draft mapping notes; no real manifest may be generated.",
                evidence = listOf(
                    "draftOnly=${draft.adapterMappingDraft.draftOnly}",
                    "mappingManifestGenerated=${draft.adapterMappingDraft.manifestGenerated}",
                    "panelManifestGenerated=${draft.adapterMappingDraft.panelDrafts.count { it.manifestGenerated }}",
                    "panelRendererReady=${draft.adapterMappingDraft.panelDrafts.count { it.rendererReady }}",
                    "generatedNowFields=${generatedNowFieldCount(draft)}",
                    "renderSettingsGeneratedNow=${draft.adapterMappingDraft.renderSettingsDraft.generatedNow}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_MANIFEST_GENERATED_FORBIDDEN",
                path = "$.adapterMappingDraft",
                suggestedFix = "Keep Phase 10 mapping as draft notes only and defer any ComicVideoRenderHandoffManifest generation to a future explicit task.",
            ),
            check(
                checkId = "no-findings",
                category = "review_gate",
                passed = draft.findingCount == 0 && draft.findings.isEmpty(),
                message = "Implementation draft review requires no Phase 10 findings.",
                evidence = listOf(
                    "findingCount=${draft.findingCount}",
                    "findings=${draft.findings.size}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_FINDINGS_PRESENT",
                path = "$.findings",
                suggestedFix = "Resolve Phase 10 draft findings before Phase 11 review.",
            ),
            check(
                checkId = "no-readiness-overclaim",
                category = "renderer_compatibility",
                passed = !draft.boundaries.rendererReady &&
                    !draft.boundaries.mp4ReadinessClaimed &&
                    !draftSummary.rendererReady &&
                    !draftSummary.mp4ReadinessClaimed &&
                    !draft.implementationDraftSummary.manifestGenerated &&
                    !draft.implementationDraftSummary.adapterImplementationExecuted,
                message = "Implementation draft review must preserve rendererReady=false, mp4ReadinessClaimed=false, manifestGenerated=false, adapterImplementationExecuted=false, and mediaGenerated=false.",
                evidence = listOf(
                    "rendererReady=${draft.boundaries.rendererReady}",
                    "mp4ReadinessClaimed=${draft.boundaries.mp4ReadinessClaimed}",
                    "manifestGenerated=${draft.implementationDraftSummary.manifestGenerated}",
                    "adapterImplementationExecuted=${draft.implementationDraftSummary.adapterImplementationExecuted}",
                    "mediaGenerated=${draft.boundaries.mediaGenerated}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_READY_OVERCLAIM",
                path = "$.rendererCompatibility",
                suggestedFix = "Keep rendererReady=false, mp4ReadinessClaimed=false, and all no-media/no-manifest flags false until future explicit verification.",
            ),
        )

    private fun draftSummary(
        draft: LocalRendererAdapterImplementationDraft,
    ): LocalRendererAdapterImplementationDraftReviewSummary =
        LocalRendererAdapterImplementationDraftReviewSummary(
            sourceChecks = draft.checks.size,
            sourceChecksPass = draft.checks.count { it.status == "PASS" },
            sourceChecksFail = draft.checks.count { it.status != "PASS" },
            implementationTasks = draft.implementationTasks.size,
            mappingDraftPanels = draft.adapterMappingDraft.panelDrafts.size,
            mappingDraftFields = mappingDraftFieldCount(draft),
            verificationSteps = draft.verificationPlan.size,
            findingCount = draft.findingCount,
            draftOnly = draft.adapterMappingDraft.draftOnly,
            manifestGenerated = draft.boundaries.manifestGenerated ||
                draft.implementationDraftSummary.manifestGenerated ||
                draft.adapterMappingDraft.manifestGenerated,
            adapterImplementationExecuted = draft.boundaries.adapterImplementationExecuted ||
                draft.implementationDraftSummary.adapterImplementationExecuted,
            rendererReady = draft.boundaries.rendererReady ||
                draft.adapterMappingDraft.panelDrafts.any { it.rendererReady },
            mp4ReadinessClaimed = draft.boundaries.mp4ReadinessClaimed,
        )

    private fun boundarySummary(
        boundaries: LocalRendererAdapterImplementationDraftBoundaries,
    ): LocalRendererAdapterImplementationDraftReviewBoundarySummary =
        LocalRendererAdapterImplementationDraftReviewBoundarySummary(
            fileProtocolJsonOnly = boundaries.fileProtocolJsonOnly,
            noExternalPaidProviderTools = boundaries.noExternalPaidProviderTools,
            providerInvocation = boundaries.providerInvocation,
            ttsInvocation = boundaries.ttsInvocation,
            mediaGenerated = boundaries.mediaGenerated,
            rendererInvocation = boundaries.rendererInvocation,
            directSqliteWrites = boundaries.directSqliteWrites,
            providerApiCalls = boundaries.providerApiCalls,
            adapterImplementationExecuted = boundaries.adapterImplementationExecuted,
            manifestGenerated = boundaries.manifestGenerated,
            finalMediaApproval = boundaries.finalMediaApproval,
            providerMediaHandoff = boundaries.providerMediaHandoff,
            release = boundaries.release,
            canonRegistryOrProductionLock = boundaries.canonRegistryOrProductionLock,
            rendererReady = boundaries.rendererReady,
            mp4ReadinessClaimed = boundaries.mp4ReadinessClaimed,
        )

    private fun rendererCompatibilitySummary(): LocalRendererAdapterImplementationDraftReviewRendererCompatibilitySummary =
        LocalRendererAdapterImplementationDraftReviewRendererCompatibilitySummary(
            ready = false,
            mp4ReadinessClaimed = false,
            adapterExecutionStillRequiresExplicitTask = true,
            manifestDraftPlanStillRequiresExplicitTask = true,
            requiredBeforeManifestDraftPlan = listOf(
                "Pass Phase 11 implementation draft review.",
                "Assign a separate local_renderer_manifest_draft_plan task.",
                "Do not generate ComicVideoRenderHandoffManifest in Phase 11A.",
                "Do not run renderer/ffmpeg/TTS or generate media.",
                "Boundary: no media generation, no adapter execution, no real ComicVideoRenderHandoffManifest.",
            ),
            summary = "Review gate skeleton only: Phase 10 draft can be reviewed for a future manifest draft plan, but no adapter execution, renderer readiness, MP4 readiness, or media generation is claimed.",
        )

    private fun outputBoundaries(): LocalRendererAdapterImplementationDraftBoundaries =
        LocalRendererAdapterImplementationDraftBoundaries(
            fileProtocolJsonOnly = true,
            noExternalPaidProviderTools = true,
            providerInvocation = "none",
            ttsInvocation = "none",
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
            rendererReady = false,
            mp4ReadinessClaimed = false,
        )

    private fun localBoundariesOk(boundaries: LocalRendererAdapterImplementationDraftBoundaries): Boolean =
        boundaries.fileProtocolJsonOnly &&
            boundaries.noExternalPaidProviderTools &&
            boundaries.providerInvocation == "none" &&
            boundaries.ttsInvocation == "none" &&
            !boundaries.mediaGenerated &&
            boundaries.rendererInvocation == "none" &&
            !boundaries.directSqliteWrites &&
            !boundaries.providerApiCalls &&
            !boundaries.adapterImplementationExecuted &&
            !boundaries.manifestGenerated &&
            !boundaries.finalMediaApproval &&
            !boundaries.providerMediaHandoff &&
            !boundaries.release &&
            !boundaries.canonRegistryOrProductionLock &&
            !boundaries.rendererReady &&
            !boundaries.mp4ReadinessClaimed

    private fun mappingDraftFieldCount(draft: LocalRendererAdapterImplementationDraft): Int =
        draft.adapterMappingDraft.panelDrafts.sumOf {
            it.targetPanelTimelineDraft.size + it.targetVoiceTimelineDraft.size + it.targetSubtitleTimelineDraft.size
        } + listOf(
            draft.adapterMappingDraft.renderSettingsDraft.targetSection,
            draft.adapterMappingDraft.renderSettingsDraft.timelineStatusHint,
            draft.adapterMappingDraft.renderSettingsDraft.frameRateHint,
            draft.adapterMappingDraft.renderSettingsDraft.resolutionHint,
        ).count { it.isNotBlank() }

    private fun generatedNowFieldCount(draft: LocalRendererAdapterImplementationDraft): Int =
        draft.adapterMappingDraft.panelDrafts
            .flatMap { it.targetPanelTimelineDraft + it.targetVoiceTimelineDraft + it.targetSubtitleTimelineDraft }
            .count { it.generatedNow }

    private fun check(
        checkId: String,
        category: String,
        passed: Boolean,
        message: String,
        evidence: List<String>,
        findings: MutableList<LocalRendererAdapterImplementationDraftReviewFinding>,
        ruleId: String,
        path: String,
        suggestedFix: String,
    ): LocalRendererAdapterImplementationDraftReviewCheck {
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
        return LocalRendererAdapterImplementationDraftReviewCheck(
            checkId = checkId,
            status = if (passed) "PASS" else "FAIL",
            category = category,
            message = message,
            evidence = evidence,
        )
    }

    private fun List<LocalRendererAdapterImplementationDraftReviewFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): LocalRendererAdapterImplementationDraftReviewFinding =
        LocalRendererAdapterImplementationDraftReviewFinding(
            ruleId,
            category,
            severity,
            path,
            message,
            evidence,
            suggestedFix,
        )
}
