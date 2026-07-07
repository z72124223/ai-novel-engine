package com.localnovel.engine.service

import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffDraft
import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffReviewCheck
import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffReviewFinding
import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffReviewReadinessSummary
import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffReviewRendererCompatibilitySummary
import com.localnovel.engine.model.LocalStoryVideoPreviewHandoffReviewReport

class LocalStoryVideoPreviewHandoffReviewService {
    private val requiredHandoffStatus = "LOCAL_STORY_VIDEO_PREVIEW_HANDOFF_DRAFT"
    private val requiredDraftBoundary = "local_story_video_preview_handoff_draft_only_not_media_generation"
    private val requiredDraftNextStep = "local_preview_handoff_review"
    private val reviewBoundary = "local_story_video_preview_handoff_review_only_not_media_generation"
    private val nextAllowedDownstreamStep = "local_renderer_adapter_dry_run"

    fun review(
        handoff: LocalStoryVideoPreviewHandoffDraft,
        handoffFile: String = "",
        reviewId: String = "",
    ): LocalStoryVideoPreviewHandoffReviewReport {
        val findings = mutableListOf<LocalStoryVideoPreviewHandoffReviewFinding>()
        val checks = buildChecks(handoff, handoffFile, findings)
        val verdict = if (findings.hasBlocking()) "FAIL" else "PASS"
        val resolvedReviewId = reviewId.ifBlank {
            handoff.handoffId.takeIf { it.isNotBlank() }?.let { "$it-review-v0" }.orEmpty()
        }

        return LocalStoryVideoPreviewHandoffReviewReport(
            verdict = verdict,
            reviewId = resolvedReviewId,
            sourceHandoffFile = handoffFile,
            sourceHandoffId = handoff.handoffId,
            sourceStoryInputPackId = handoff.sourceStoryInputPackId,
            handoffStatus = handoff.status,
            checks = checks,
            readinessSummary = readinessSummary(handoff),
            rendererCompatibilitySummary = rendererCompatibilitySummary(handoff),
            boundaries = handoff.boundaries,
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
        handoff: LocalStoryVideoPreviewHandoffDraft,
        handoffFile: String,
        findings: MutableList<LocalStoryVideoPreviewHandoffReviewFinding>,
    ): List<LocalStoryVideoPreviewHandoffReviewCheck> =
        listOf(
            check(
                checkId = "source-handoff-present",
                category = "source_validation",
                passed = handoffFile.isNotBlank() && handoff.handoffId.isNotBlank() && handoff.sourceStoryInputPackId.isNotBlank(),
                message = "Review requires an explicit handoff file, handoff id, and source story input pack id.",
                evidence = listOf(
                    "handoffFile=$handoffFile",
                    "handoffId=${handoff.handoffId}",
                    "sourceStoryInputPackId=${handoff.sourceStoryInputPackId}",
                ),
                findings = findings,
                ruleId = "LOCAL_PREVIEW_HANDOFF_REVIEW_SOURCE_MISSING",
                path = "$.sourceHandoff",
                suggestedFix = "Use a complete Phase 5 preview handoff draft JSON and pass it with --handoff.",
            ),
            check(
                checkId = "handoff-status",
                category = "source_validation",
                passed = handoff.status == requiredHandoffStatus,
                message = "Preview handoff review requires status LOCAL_STORY_VIDEO_PREVIEW_HANDOFF_DRAFT.",
                evidence = listOf(handoff.status),
                findings = findings,
                ruleId = "LOCAL_PREVIEW_HANDOFF_REVIEW_STATUS_NOT_DRAFT",
                path = "$.handoff.status",
                suggestedFix = "Use the PASS Phase 5 preview handoff draft.",
            ),
            check(
                checkId = "handoff-downstream-boundary",
                category = "boundary",
                passed = handoff.downstreamBoundary == requiredDraftBoundary,
                message = "Preview handoff review requires the Phase 5 no-media downstream boundary.",
                evidence = listOf(handoff.downstreamBoundary),
                findings = findings,
                ruleId = "LOCAL_PREVIEW_HANDOFF_REVIEW_BOUNDARY_MISMATCH",
                path = "$.handoff.downstreamBoundary",
                suggestedFix = "Use a Phase 5 handoff with local_story_video_preview_handoff_draft_only_not_media_generation.",
            ),
            check(
                checkId = "handoff-next-step",
                category = "boundary",
                passed = handoff.nextAllowedDownstreamStep == requiredDraftNextStep,
                message = "Preview handoff review requires nextAllowedDownstreamStep=local_preview_handoff_review.",
                evidence = listOf(handoff.nextAllowedDownstreamStep),
                findings = findings,
                ruleId = "LOCAL_PREVIEW_HANDOFF_REVIEW_NEXT_STEP_MISMATCH",
                path = "$.handoff.nextAllowedDownstreamStep",
                suggestedFix = "Use the Phase 5 handoff that explicitly allows local_preview_handoff_review.",
            ),
            check(
                checkId = "required-readiness-pass",
                category = "readiness",
                passed = requiredReadinessPassed(handoff),
                message = "All readiness checks required for the Phase 5 draft must be PASS.",
                evidence = listOf(
                    "required=${handoff.readinessChecks.count { it.requiredForDraft }}",
                    "requiredPass=${handoff.readinessChecks.count { it.requiredForDraft && it.status == "PASS" }}",
                    "requiredFail=${handoff.readinessChecks.count { it.requiredForDraft && it.status != "PASS" }}",
                ),
                findings = findings,
                ruleId = "LOCAL_PREVIEW_HANDOFF_REVIEW_REQUIRED_READINESS_FAIL",
                path = "$.handoff.readinessChecks",
                suggestedFix = "Regenerate or repair the Phase 5 handoff so all required readiness checks pass.",
            ),
            check(
                checkId = "preview-plan-present",
                category = "preview_plan",
                passed = previewPlanReady(handoff),
                message = "Preview handoff review requires at least one panel and positive target/total duration.",
                evidence = listOf(
                    "panels=${handoff.previewPlan.panelOrdering.size}",
                    "targetDurationMillis=${handoff.previewPlan.targetDurationMillis}",
                    "panelDurationMillis=${handoff.previewPlan.panelOrdering.sumOf { it.durationMillis }}",
                ),
                findings = findings,
                ruleId = "LOCAL_PREVIEW_HANDOFF_REVIEW_PREVIEW_PLAN_NOT_READY",
                path = "$.handoff.previewPlan",
                suggestedFix = "Use a Phase 5 handoff with a non-empty positive-duration preview plan.",
            ),
            check(
                checkId = "qa-summary-pass",
                category = "qa",
                passed = qaSummaryReady(handoff),
                message = "Preview handoff review requires QA summary with no failed checks.",
                evidence = listOf(
                    "qaChecks=${handoff.previewPlan.qaSummary.qaChecks}",
                    "qaPass=${handoff.previewPlan.qaSummary.qaPass}",
                    "qaFail=${handoff.previewPlan.qaSummary.qaFail}",
                ),
                findings = findings,
                ruleId = "LOCAL_PREVIEW_HANDOFF_REVIEW_QA_NOT_PASS",
                path = "$.handoff.previewPlan.qaSummary",
                suggestedFix = "Use a Phase 5 handoff whose source render package QA summary has zero failures.",
            ),
            check(
                checkId = "local-boundaries",
                category = "boundary",
                passed = localBoundariesOk(handoff),
                message = "Preview handoff review requires local-only boundaries with no provider/API, renderer, TTS, media generation, release, or production lock.",
                evidence = listOf(
                    "fileProtocolJsonOnly=${handoff.boundaries.fileProtocolJsonOnly}",
                    "noExternalPaidProviderTools=${handoff.boundaries.noExternalPaidProviderTools}",
                    "providerInvocation=${handoff.boundaries.providerInvocation}",
                    "ttsInvocation=${handoff.boundaries.ttsInvocation}",
                    "mediaGenerated=${handoff.boundaries.mediaGenerated}",
                    "rendererInvocation=${handoff.boundaries.rendererInvocation}",
                    "directSqliteWrites=${handoff.boundaries.directSqliteWrites}",
                    "providerApiCalls=${handoff.boundaries.providerApiCalls}",
                    "finalMediaApproval=${handoff.boundaries.finalMediaApproval}",
                    "release=${handoff.boundaries.release}",
                    "canonRegistryOrProductionLock=${handoff.boundaries.canonRegistryOrProductionLock}",
                ),
                findings = findings,
                ruleId = "LOCAL_PREVIEW_HANDOFF_REVIEW_LOCAL_BOUNDARY_FAIL",
                path = "$.handoff.boundaries",
                suggestedFix = "Use a local-only Phase 5 handoff with no provider/API, renderer, TTS, media, release, or production lock claims.",
            ),
            check(
                checkId = "renderer-compatibility-not-ready",
                category = "renderer_compatibility",
                passed = rendererCompatibilityHonest(handoff),
                message = "localRendererCompatibility.ready=false is preserved as the honest not-yet-direct-renderable signal.",
                evidence = listOf(
                    "ready=${handoff.localRendererCompatibility.ready}",
                    "missingFields=${handoff.localRendererCompatibility.missingFields.size}",
                    "requiredChecksBeforeMp4=${handoff.localRendererCompatibility.requiredChecksBeforeMp4.size}",
                ),
                findings = findings,
                ruleId = "LOCAL_PREVIEW_HANDOFF_REVIEW_RENDERER_COMPATIBILITY_UNSAFE",
                path = "$.handoff.localRendererCompatibility",
                suggestedFix = "Keep ready=false and carry adapter/dry-run work before any local MP4 generation.",
            ),
            check(
                checkId = "handoff-findings-empty",
                category = "source_validation",
                passed = handoff.findingCount == 0 && handoff.findings.isEmpty(),
                message = "Preview handoff review requires a clean Phase 5 handoff with no carried findings.",
                evidence = listOf("findingCount=${handoff.findingCount}"),
                findings = findings,
                ruleId = "LOCAL_PREVIEW_HANDOFF_REVIEW_UPSTREAM_FINDINGS_PRESENT",
                path = "$.handoff.findings",
                suggestedFix = "Resolve Phase 5 handoff findings before review.",
            ),
        )

    private fun requiredReadinessPassed(handoff: LocalStoryVideoPreviewHandoffDraft): Boolean {
        val required = handoff.readinessChecks.filter { it.requiredForDraft }
        return required.isNotEmpty() && required.all { it.status == "PASS" }
    }

    private fun previewPlanReady(handoff: LocalStoryVideoPreviewHandoffDraft): Boolean {
        val panels = handoff.previewPlan.panelOrdering
        return panels.isNotEmpty() &&
            handoff.previewPlan.targetDurationMillis > 0 &&
            panels.sumOf { it.durationMillis } > 0 &&
            panels.all { it.durationMillis > 0 && it.endMillis > it.startMillis }
    }

    private fun qaSummaryReady(handoff: LocalStoryVideoPreviewHandoffDraft): Boolean =
        handoff.previewPlan.qaSummary.qaChecks > 0 &&
            handoff.previewPlan.qaSummary.qaPass == handoff.previewPlan.qaSummary.qaChecks &&
            handoff.previewPlan.qaSummary.qaFail == 0

    private fun localBoundariesOk(handoff: LocalStoryVideoPreviewHandoffDraft): Boolean =
        handoff.boundaries.fileProtocolJsonOnly &&
            handoff.boundaries.noExternalPaidProviderTools &&
            handoff.boundaries.providerInvocation == "none" &&
            handoff.boundaries.ttsInvocation == "none" &&
            !handoff.boundaries.mediaGenerated &&
            handoff.boundaries.rendererInvocation == "none" &&
            !handoff.boundaries.directSqliteWrites &&
            !handoff.boundaries.providerApiCalls &&
            !handoff.boundaries.finalMediaApproval &&
            !handoff.boundaries.release &&
            !handoff.boundaries.canonRegistryOrProductionLock

    private fun rendererCompatibilityHonest(handoff: LocalStoryVideoPreviewHandoffDraft): Boolean {
        val compatibility = handoff.localRendererCompatibility
        val adapterMentioned = (compatibility.missingFields + compatibility.requiredChecksBeforeMp4)
            .any { it.contains("adapter", ignoreCase = true) || it.contains("ComicVideoRenderHandoffManifest") }
        val dryRunMentioned = (compatibility.missingFields + compatibility.requiredChecksBeforeMp4)
            .any { it.contains("dry-run", ignoreCase = true) || it.contains("dry run", ignoreCase = true) }
        return !compatibility.ready &&
            compatibility.missingFields.isNotEmpty() &&
            compatibility.requiredChecksBeforeMp4.isNotEmpty() &&
            adapterMentioned &&
            dryRunMentioned
    }

    private fun readinessSummary(handoff: LocalStoryVideoPreviewHandoffDraft): LocalStoryVideoPreviewHandoffReviewReadinessSummary {
        val required = handoff.readinessChecks.filter { it.requiredForDraft }
        return LocalStoryVideoPreviewHandoffReviewReadinessSummary(
            readinessChecks = handoff.readinessChecks.size,
            readinessPass = handoff.readinessChecks.count { it.status == "PASS" },
            readinessFail = handoff.readinessChecks.count { it.status != "PASS" },
            requiredReadinessChecks = required.size,
            requiredReadinessPass = required.count { it.status == "PASS" },
            requiredReadinessFail = required.count { it.status != "PASS" },
            previewPanels = handoff.previewPlan.panelOrdering.size,
            previewTargetDurationMillis = handoff.previewPlan.targetDurationMillis,
            previewPanelDurationMillis = handoff.previewPlan.panelOrdering.sumOf { it.durationMillis },
            qaChecks = handoff.previewPlan.qaSummary.qaChecks,
            qaPass = handoff.previewPlan.qaSummary.qaPass,
            qaFail = handoff.previewPlan.qaSummary.qaFail,
        )
    }

    private fun rendererCompatibilitySummary(
        handoff: LocalStoryVideoPreviewHandoffDraft,
    ): LocalStoryVideoPreviewHandoffReviewRendererCompatibilitySummary {
        val compatibility = handoff.localRendererCompatibility
        return LocalStoryVideoPreviewHandoffReviewRendererCompatibilitySummary(
            ready = compatibility.ready,
            currentRendererExpectedInput = compatibility.currentRendererExpectedInput,
            currentDraftInput = compatibility.currentDraftInput,
            readyFields = compatibility.readyFields,
            missingFields = compatibility.missingFields,
            requiredChecksBeforeMp4 = compatibility.requiredChecksBeforeMp4,
            readyFalseAccepted = !compatibility.ready,
            summary = if (compatibility.ready) {
                "Unexpected ready=true; this review gate is not MP4 readiness."
            } else {
                "ready=false accepted: adapter and dry-run renderer work remain required before any local MP4 generation."
            },
        )
    }

    private fun check(
        checkId: String,
        category: String,
        passed: Boolean,
        message: String,
        evidence: List<String>,
        findings: MutableList<LocalStoryVideoPreviewHandoffReviewFinding>,
        ruleId: String,
        path: String,
        suggestedFix: String,
    ): LocalStoryVideoPreviewHandoffReviewCheck {
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
        return LocalStoryVideoPreviewHandoffReviewCheck(
            checkId = checkId,
            status = if (passed) "PASS" else "FAIL",
            category = category,
            message = message,
            evidence = evidence,
        )
    }

    private fun List<LocalStoryVideoPreviewHandoffReviewFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): LocalStoryVideoPreviewHandoffReviewFinding =
        LocalStoryVideoPreviewHandoffReviewFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
}
