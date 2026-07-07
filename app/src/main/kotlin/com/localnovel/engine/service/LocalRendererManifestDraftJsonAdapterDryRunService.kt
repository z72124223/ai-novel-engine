package com.localnovel.engine.service

import com.localnovel.engine.model.LocalRendererDraftPanelManifestEntry
import com.localnovel.engine.model.LocalRendererManifestDraftJsonAdapterDryRunBoundarySummary
import com.localnovel.engine.model.LocalRendererManifestDraftJsonAdapterDryRunBoundaries
import com.localnovel.engine.model.LocalRendererManifestDraftJsonAdapterDryRunCheck
import com.localnovel.engine.model.LocalRendererManifestDraftJsonAdapterDryRunFinding
import com.localnovel.engine.model.LocalRendererManifestDraftJsonAdapterDryRunReport
import com.localnovel.engine.model.LocalRendererManifestDraftJsonAdapterDryRunSourceAlignmentSummary
import com.localnovel.engine.model.LocalRendererManifestDraftJsonAdapterDryRunSummary
import com.localnovel.engine.model.LocalRendererManifestDraftJsonAdapterFieldCoverageSummary
import com.localnovel.engine.model.LocalRendererManifestDraftJsonAdapterPanelMappingEntry
import com.localnovel.engine.model.LocalRendererManifestDraftJsonAdapterPanelMappingSummary
import com.localnovel.engine.model.LocalRendererManifestDraftJsonDraft
import com.localnovel.engine.model.LocalRendererManifestDraftJsonDraftBoundaries
import com.localnovel.engine.model.LocalRendererManifestDraftJsonField
import com.localnovel.engine.model.LocalRendererManifestDraftJsonReviewBoundaries
import com.localnovel.engine.model.LocalRendererManifestDraftJsonReviewReport
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

class LocalRendererManifestDraftJsonAdapterDryRunService {
    private val requiredDraftStatus = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT"
    private val requiredDraftBoundary = "local_renderer_manifest_draft_json_draft_only_not_production_manifest"
    private val requiredDraftNextStep = "local_renderer_manifest_draft_json_review"
    private val requiredReviewStatus = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_PASS"
    private val requiredReviewBoundary = "local_renderer_manifest_draft_json_review_only_not_production_approval"
    private val requiredReviewNextStep = "local_renderer_manifest_draft_json_adapter_dry_run"
    private val passStatus = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_PASS"
    private val failStatus = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_FAIL"
    private val downstreamBoundary = "local_renderer_manifest_draft_json_adapter_dry_run_only_not_renderer_execution"
    private val nextAllowedDownstreamStep = "local_renderer_manifest_adapter_contract_plan"
    private val requiredBoundaryFlags = listOf(
        "mediaGenerated=false",
        "rendererInvocation=none",
        "providerInvocation=none",
        "ttsInvocation=none",
        "adapterImplementationExecuted=false",
        "adapterExecuted=false",
        "noExternalPaidProviderTools=true",
        "rendererReady=false",
        "mp4ReadinessClaimed=false",
        "draftManifestGenerated=true",
        "productionManifestGenerated=false",
        "productionReady=false",
        "productionApproved=false",
    )

    fun dryRunFile(
        draftPath: Path,
        reviewPath: Path,
        outPath: Path? = null,
        dryRunId: String = "",
    ): LocalRendererManifestDraftJsonAdapterDryRunReport {
        val rawDraft = Files.readString(draftPath).removePrefix("\uFEFF")
        val rawReview = Files.readString(reviewPath).removePrefix("\uFEFF")
        val draft = JsonSupport.json.decodeFromString<LocalRendererManifestDraftJsonDraft>(rawDraft)
        val review = JsonSupport.json.decodeFromString<LocalRendererManifestDraftJsonReviewReport>(rawReview)
        val report = dryRun(
            draft = draft,
            review = review,
            draftFile = draftPath.toString(),
            reviewFile = reviewPath.toString(),
            dryRunId = dryRunId,
            sourceProductionApprovedClaim = rawProductionApprovedClaim(rawDraft) ||
                rawProductionApprovedClaim(rawReview),
        )
        if (outPath != null) {
            outPath.parent?.let { Files.createDirectories(it) }
            Files.writeString(outPath, JsonSupport.json.encodeToString(report))
        }
        return report
    }

    fun dryRun(
        draft: LocalRendererManifestDraftJsonDraft,
        review: LocalRendererManifestDraftJsonReviewReport,
        draftFile: String = "",
        reviewFile: String = "",
        dryRunId: String = "",
        sourceProductionApprovedClaim: Boolean = false,
    ): LocalRendererManifestDraftJsonAdapterDryRunReport {
        val findings = mutableListOf<LocalRendererManifestDraftJsonAdapterDryRunFinding>()
        val alignment = sourceAlignmentSummary(draft, review)
        val mappingEntries = buildMappingEntries(draft)
        val fieldCoverage = fieldCoverageSummary(draft, mappingEntries)
        val panelMappingSummary = panelMappingSummary(draft, mappingEntries, fieldCoverage)
        val boundarySummary = boundarySummary(draft, review, sourceProductionApprovedClaim)
        val dryRunSummary = adapterDryRunSummary(
            draft = draft,
            review = review,
            mappingEntries = mappingEntries,
            fieldCoverage = fieldCoverage,
            panelMappingSummary = panelMappingSummary,
            sourceProductionApprovedClaim = sourceProductionApprovedClaim,
        )
        val checks = buildChecks(
            draft = draft,
            review = review,
            draftFile = draftFile,
            reviewFile = reviewFile,
            alignment = alignment,
            fieldCoverage = fieldCoverage,
            panelMappingSummary = panelMappingSummary,
            boundarySummary = boundarySummary,
            sourceProductionApprovedClaim = sourceProductionApprovedClaim,
            findings = findings,
        )
        val verdict = if (findings.hasBlocking()) "FAIL" else "PASS"
        val resolvedDryRunId = dryRunId.ifBlank {
            review.reviewId.takeIf { it.isNotBlank() }?.let { "$it-adapter-dry-run-v0" }
                ?: draft.draftId.takeIf { it.isNotBlank() }?.let { "$it-adapter-dry-run-v0" }
                ?: ""
        }

        return LocalRendererManifestDraftJsonAdapterDryRunReport(
            verdict = verdict,
            status = if (verdict == "PASS") passStatus else failStatus,
            dryRunId = resolvedDryRunId,
            sourceDraftFile = draftFile,
            sourceReviewFile = reviewFile,
            sourceDraftId = draft.draftId,
            sourceReviewId = review.reviewId,
            sourceManifestDraftPlanId = draft.sourceManifestDraftPlanId,
            sourceManifestDraftPlanReviewId = draft.sourceManifestDraftPlanReviewId,
            sourceImplementationDraftId = draft.sourceImplementationDraftId,
            sourceImplementationDraftReviewId = draft.sourceImplementationDraftReviewId,
            sourcePlanId = draft.sourcePlanId,
            sourcePlanReviewId = draft.sourcePlanReviewId,
            sourceDryRunId = draft.sourceDryRunId,
            sourceHandoffId = draft.sourceHandoffId,
            sourceHandoffReviewId = draft.sourceHandoffReviewId,
            sourceStoryInputPackId = draft.sourceStoryInputPackId,
            checks = checks,
            sourceAlignmentSummary = alignment,
            adapterDryRunSummary = dryRunSummary,
            panelMappingSummary = panelMappingSummary,
            draftFieldCoverageSummary = fieldCoverage,
            boundarySummary = boundarySummary,
            adapterDryRunMappingEntries = if (verdict == "PASS") mappingEntries else emptyList(),
            boundaries = outputBoundaries(
                draftManifestGenerated = draft.boundaries.draftManifestGenerated &&
                    review.boundaries.draftManifestGenerated,
            ),
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
        draft: LocalRendererManifestDraftJsonDraft,
        review: LocalRendererManifestDraftJsonReviewReport,
        draftFile: String,
        reviewFile: String,
        alignment: LocalRendererManifestDraftJsonAdapterDryRunSourceAlignmentSummary,
        fieldCoverage: LocalRendererManifestDraftJsonAdapterFieldCoverageSummary,
        panelMappingSummary: LocalRendererManifestDraftJsonAdapterPanelMappingSummary,
        boundarySummary: LocalRendererManifestDraftJsonAdapterDryRunBoundarySummary,
        sourceProductionApprovedClaim: Boolean,
        findings: MutableList<LocalRendererManifestDraftJsonAdapterDryRunFinding>,
    ): List<LocalRendererManifestDraftJsonAdapterDryRunCheck> =
        listOf(
            check(
                checkId = "explicit-source-files",
                category = "source_validation",
                passed = draftFile.isNotBlank() &&
                    reviewFile.isNotBlank() &&
                    draft.draftId.isNotBlank() &&
                    review.reviewId.isNotBlank() &&
                    draft.sourceStoryInputPackId.isNotBlank(),
                message = "Adapter dry-run requires explicit Phase 14 draft and Phase 15 review files.",
                evidence = listOf(
                    "draftFile=$draftFile",
                    "reviewFile=$reviewFile",
                    "draftId=${draft.draftId}",
                    "reviewId=${review.reviewId}",
                    "sourceStoryInputPackId=${draft.sourceStoryInputPackId}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_SOURCE_FILE_MISSING",
                path = "$.inputs",
                suggestedFix = "Pass --draft, --review, and --out explicitly.",
            ),
            check(
                checkId = "draft-state",
                category = "source_validation",
                passed = draft.verdict == "PASS" &&
                    draft.status == requiredDraftStatus &&
                    draft.downstreamBoundary == requiredDraftBoundary &&
                    draft.nextAllowedDownstreamStep == requiredDraftNextStep,
                message = "Adapter dry-run requires PASS Phase 14 draft JSON state.",
                evidence = listOf(
                    "verdict=${draft.verdict}",
                    "status=${draft.status}",
                    "downstreamBoundary=${draft.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${draft.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_DRAFT_STATE_INVALID",
                path = "$.draftState",
                suggestedFix = "Use the accepted PASS Phase 14 manifest draft JSON.",
            ),
            check(
                checkId = "review-state",
                category = "source_validation",
                passed = review.verdict == "PASS" &&
                    review.status == requiredReviewStatus &&
                    review.downstreamBoundary == requiredReviewBoundary &&
                    review.nextAllowedDownstreamStep == requiredReviewNextStep,
                message = "Adapter dry-run requires PASS Phase 15 draft JSON review state.",
                evidence = listOf(
                    "verdict=${review.verdict}",
                    "status=${review.status}",
                    "downstreamBoundary=${review.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${review.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_REVIEW_STATE_INVALID",
                path = "$.reviewState",
                suggestedFix = "Use the accepted PASS Phase 15 manifest draft JSON review report.",
            ),
            check(
                checkId = "source-ids-align",
                category = "source_linkage",
                passed = alignment.draftIdAligned &&
                    alignment.manifestDraftPlanIdAligned &&
                    alignment.manifestDraftPlanReviewIdAligned &&
                    alignment.sourceImplementationDraftIdAligned &&
                    alignment.sourceImplementationDraftReviewIdAligned &&
                    alignment.sourcePlanIdAligned &&
                    alignment.sourcePlanReviewIdAligned &&
                    alignment.sourceDryRunIdAligned &&
                    alignment.sourceHandoffIdAligned &&
                    alignment.sourceHandoffReviewIdAligned &&
                    alignment.sourceStoryInputPackIdAligned,
                message = "Adapter dry-run requires Phase 14 and Phase 15 source ids to align.",
                evidence = listOf(
                    "draftIdAligned=${alignment.draftIdAligned}",
                    "manifestDraftPlanIdAligned=${alignment.manifestDraftPlanIdAligned}",
                    "manifestDraftPlanReviewIdAligned=${alignment.manifestDraftPlanReviewIdAligned}",
                    "sourceImplementationDraftIdAligned=${alignment.sourceImplementationDraftIdAligned}",
                    "sourceImplementationDraftReviewIdAligned=${alignment.sourceImplementationDraftReviewIdAligned}",
                    "sourceStoryInputPackIdAligned=${alignment.sourceStoryInputPackIdAligned}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_SOURCE_ID_MISMATCH",
                path = "$.sourceAlignmentSummary",
                suggestedFix = "Use the Phase 15 review report generated from the supplied Phase 14 draft JSON.",
            ),
            check(
                checkId = "local-boundaries",
                category = "boundary",
                passed = boundarySummary.draftLocalOnly &&
                    boundarySummary.reviewLocalOnly &&
                    !boundarySummary.mediaGenerated &&
                    boundarySummary.rendererInvocation == "none" &&
                    boundarySummary.providerInvocation == "none" &&
                    boundarySummary.ttsInvocation == "none" &&
                    !boundarySummary.adapterImplementationExecuted &&
                    !boundarySummary.adapterExecuted &&
                    boundarySummary.draftManifestGenerated &&
                    !boundarySummary.productionManifestGenerated &&
                    !boundarySummary.productionReady &&
                    !boundarySummary.productionApproved &&
                    !boundarySummary.finalMediaApproval &&
                    !boundarySummary.providerMediaHandoff &&
                    !boundarySummary.release &&
                    !boundarySummary.canonRegistryOrProductionLock &&
                    !boundarySummary.rendererReady &&
                    !boundarySummary.mp4ReadinessClaimed &&
                    !sourceProductionApprovedClaim,
                message = "Adapter dry-run requires local-only no-media boundaries and no production approval claim.",
                evidence = listOf(
                    "draftLocalOnly=${boundarySummary.draftLocalOnly}",
                    "reviewLocalOnly=${boundarySummary.reviewLocalOnly}",
                    "mediaGenerated=${boundarySummary.mediaGenerated}",
                    "rendererInvocation=${boundarySummary.rendererInvocation}",
                    "providerInvocation=${boundarySummary.providerInvocation}",
                    "ttsInvocation=${boundarySummary.ttsInvocation}",
                    "adapterImplementationExecuted=${boundarySummary.adapterImplementationExecuted}",
                    "adapterExecuted=${boundarySummary.adapterExecuted}",
                    "draftManifestGenerated=${boundarySummary.draftManifestGenerated}",
                    "productionManifestGenerated=${boundarySummary.productionManifestGenerated}",
                    "productionReady=${boundarySummary.productionReady}",
                    "productionApproved=${boundarySummary.productionApproved}",
                    "rendererReady=${boundarySummary.rendererReady}",
                    "mp4ReadinessClaimed=${boundarySummary.mp4ReadinessClaimed}",
                ) + requiredBoundaryFlags,
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_BOUNDARY_FAIL",
                path = "$.boundaries",
                suggestedFix = "Use local-only JSON with no provider/API, renderer, TTS, media, adapter execution, production manifest, production approval, readiness claim, release, or production lock.",
            ),
            check(
                checkId = "source-checks-pass",
                category = "review_gate",
                passed = draft.checks.isNotEmpty() &&
                    draft.checks.filter { it.requiredForDraft }.all { it.status == "PASS" } &&
                    review.checks.isNotEmpty() &&
                    review.checks.filter { it.requiredForReview }.all { it.status == "PASS" },
                message = "Adapter dry-run requires non-empty Phase 14/15 checks and all required checks PASS.",
                evidence = listOf(
                    "draftChecks=${draft.checks.size}",
                    "draftRequiredFail=${draft.checks.count { it.requiredForDraft && it.status != "PASS" }}",
                    "reviewChecks=${review.checks.size}",
                    "reviewRequiredFail=${review.checks.count { it.requiredForReview && it.status != "PASS" }}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_SOURCE_CHECKS_NOT_PASS",
                path = "$.sourceChecks",
                suggestedFix = "Resolve Phase 14/15 required checks before adapter dry-run.",
            ),
            check(
                checkId = "no-findings-in-sources",
                category = "review_gate",
                passed = draft.findingCount == 0 &&
                    draft.findings.isEmpty() &&
                    review.findingCount == 0 &&
                    review.findings.isEmpty(),
                message = "Adapter dry-run requires no findings in Phase 14 draft or Phase 15 review.",
                evidence = listOf(
                    "draftFindingCount=${draft.findingCount}",
                    "draftFindings=${draft.findings.size}",
                    "reviewFindingCount=${review.findingCount}",
                    "reviewFindings=${review.findings.size}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_SOURCE_FINDINGS_PRESENT",
                path = "$.sourceFindings",
                suggestedFix = "Resolve Phase 14/15 findings before adapter dry-run.",
            ),
            check(
                checkId = "draft-content-present",
                category = "draft_completeness",
                passed = draft.draftPanelManifestEntries.isNotEmpty() &&
                    fieldCoverage.panelEntries > 0 &&
                    fieldCoverage.totalDraftFields > 0 &&
                    fieldCoverage.missingRequiredDraftFields == 0 &&
                    draft.draftManifestSummary.targetDurationMillis > 0 &&
                    review.draftFieldCoverageReviewSummary.totalDraftFields > 0,
                message = "Adapter dry-run requires draft panel entries, draft field coverage, and positive duration.",
                evidence = listOf(
                    "draftPanelManifestEntries=${draft.draftPanelManifestEntries.size}",
                    "panelEntries=${fieldCoverage.panelEntries}",
                    "totalDraftFields=${fieldCoverage.totalDraftFields}",
                    "missingRequiredDraftFields=${fieldCoverage.missingRequiredDraftFields}",
                    "targetDurationMillis=${draft.draftManifestSummary.targetDurationMillis}",
                    "reviewTotalDraftFields=${review.draftFieldCoverageReviewSummary.totalDraftFields}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_CONTENT_MISSING",
                path = "$.draftFieldCoverageSummary",
                suggestedFix = "Use a complete Phase 14/15 pair with draft panels and field coverage.",
            ),
            check(
                checkId = "adapter-mapping-complete",
                category = "adapter_mapping",
                passed = panelMappingSummary.draftPanelCount > 0 &&
                    panelMappingSummary.adapterDryRunMappingEntries == panelMappingSummary.draftPanelCount &&
                    panelMappingSummary.mappedPanelCount == panelMappingSummary.draftPanelCount &&
                    panelMappingSummary.unmappedPanelCount == 0 &&
                    panelMappingSummary.missingRequiredFieldCount == 0 &&
                    panelMappingSummary.targetDurationMillis > 0,
                message = "Adapter dry-run requires one mappable dry-run mapping entry for every draft panel.",
                evidence = listOf(
                    "draftPanelCount=${panelMappingSummary.draftPanelCount}",
                    "adapterDryRunMappingEntries=${panelMappingSummary.adapterDryRunMappingEntries}",
                    "mappedPanelCount=${panelMappingSummary.mappedPanelCount}",
                    "unmappedPanelCount=${panelMappingSummary.unmappedPanelCount}",
                    "missingRequiredFieldCount=${panelMappingSummary.missingRequiredFieldCount}",
                    "targetDurationMillis=${panelMappingSummary.targetDurationMillis}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_UNMAPPED_PANELS",
                path = "$.adapterDryRunMappingEntries",
                suggestedFix = "Use draft JSON with complete panel ids, source ids, timing, and draft field hints.",
            ),
            check(
                checkId = "no-production-or-readiness-overclaim",
                category = "production_boundary",
                passed = draft.draftManifestSummary.draftOnly &&
                    draft.boundaries.draftManifestGenerated &&
                    review.boundaries.draftManifestGenerated &&
                    !draft.boundaries.productionManifestGenerated &&
                    !review.boundaries.productionManifestGenerated &&
                    !draft.boundaries.productionReady &&
                    !review.boundaries.productionReady &&
                    !review.boundaries.productionApproved &&
                    !draft.boundaries.finalMediaApproval &&
                    !review.boundaries.finalMediaApproval &&
                    !draft.boundaries.providerMediaHandoff &&
                    !review.boundaries.providerMediaHandoff &&
                    !draft.boundaries.rendererReady &&
                    !review.boundaries.rendererReady &&
                    !draft.boundaries.mp4ReadinessClaimed &&
                    !review.boundaries.mp4ReadinessClaimed,
                message = "Adapter dry-run must preserve draft readiness only, not production/renderer/MP4 readiness.",
                evidence = listOf(
                    "draftManifestGenerated=${draft.boundaries.draftManifestGenerated}",
                    "reviewDraftManifestGenerated=${review.boundaries.draftManifestGenerated}",
                    "draftProductionManifestGenerated=${draft.boundaries.productionManifestGenerated}",
                    "reviewProductionManifestGenerated=${review.boundaries.productionManifestGenerated}",
                    "reviewProductionApproved=${review.boundaries.productionApproved}",
                    "draftRendererReady=${draft.boundaries.rendererReady}",
                    "reviewRendererReady=${review.boundaries.rendererReady}",
                    "draftMp4ReadinessClaimed=${draft.boundaries.mp4ReadinessClaimed}",
                    "reviewMp4ReadinessClaimed=${review.boundaries.mp4ReadinessClaimed}",
                    "adapter dry-run",
                    "no media generation",
                    "production lock=false",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_PRODUCTION_OR_READY_OVERCLAIM",
                path = "$.productionBoundary",
                suggestedFix = "Keep Phase 16 adapter dry-run only; defer production manifest approval, renderer readiness, and MP4 readiness to future explicit tasks.",
            ),
        )

    private fun sourceAlignmentSummary(
        draft: LocalRendererManifestDraftJsonDraft,
        review: LocalRendererManifestDraftJsonReviewReport,
    ): LocalRendererManifestDraftJsonAdapterDryRunSourceAlignmentSummary =
        LocalRendererManifestDraftJsonAdapterDryRunSourceAlignmentSummary(
            draftIdAligned = draft.draftId.isNotBlank() && draft.draftId == review.sourceDraftId,
            manifestDraftPlanIdAligned = draft.sourceManifestDraftPlanId.isNotBlank() &&
                draft.sourceManifestDraftPlanId == review.sourceManifestDraftPlanId,
            manifestDraftPlanReviewIdAligned = draft.sourceManifestDraftPlanReviewId.isNotBlank() &&
                draft.sourceManifestDraftPlanReviewId == review.sourceManifestDraftPlanReviewId,
            sourceImplementationDraftIdAligned = draft.sourceImplementationDraftId.isNotBlank() &&
                draft.sourceImplementationDraftId == review.sourceImplementationDraftId,
            sourceImplementationDraftReviewIdAligned = draft.sourceImplementationDraftReviewId.isNotBlank() &&
                draft.sourceImplementationDraftReviewId == review.sourceImplementationDraftReviewId,
            sourcePlanIdAligned = draft.sourcePlanId.isNotBlank() &&
                draft.sourcePlanId == review.sourcePlanId,
            sourcePlanReviewIdAligned = draft.sourcePlanReviewId.isNotBlank() &&
                draft.sourcePlanReviewId == review.sourcePlanReviewId,
            sourceDryRunIdAligned = draft.sourceDryRunId.isNotBlank() &&
                draft.sourceDryRunId == review.sourceDryRunId,
            sourceHandoffIdAligned = draft.sourceHandoffId.isNotBlank() &&
                draft.sourceHandoffId == review.sourceHandoffId,
            sourceHandoffReviewIdAligned = draft.sourceHandoffReviewId.isNotBlank() &&
                draft.sourceHandoffReviewId == review.sourceHandoffReviewId,
            sourceStoryInputPackIdAligned = draft.sourceStoryInputPackId.isNotBlank() &&
                draft.sourceStoryInputPackId == review.sourceStoryInputPackId,
            draftId = draft.draftId,
            reviewSourceDraftId = review.sourceDraftId,
            draftSourceManifestDraftPlanId = draft.sourceManifestDraftPlanId,
            reviewSourceManifestDraftPlanId = review.sourceManifestDraftPlanId,
            draftSourceManifestDraftPlanReviewId = draft.sourceManifestDraftPlanReviewId,
            reviewSourceManifestDraftPlanReviewId = review.sourceManifestDraftPlanReviewId,
            draftSourceImplementationDraftId = draft.sourceImplementationDraftId,
            reviewSourceImplementationDraftId = review.sourceImplementationDraftId,
            draftSourceImplementationDraftReviewId = draft.sourceImplementationDraftReviewId,
            reviewSourceImplementationDraftReviewId = review.sourceImplementationDraftReviewId,
            draftSourcePlanId = draft.sourcePlanId,
            reviewSourcePlanId = review.sourcePlanId,
            draftSourcePlanReviewId = draft.sourcePlanReviewId,
            reviewSourcePlanReviewId = review.sourcePlanReviewId,
            draftSourceDryRunId = draft.sourceDryRunId,
            reviewSourceDryRunId = review.sourceDryRunId,
            draftSourceHandoffId = draft.sourceHandoffId,
            reviewSourceHandoffId = review.sourceHandoffId,
            draftSourceHandoffReviewId = draft.sourceHandoffReviewId,
            reviewSourceHandoffReviewId = review.sourceHandoffReviewId,
            draftSourceStoryInputPackId = draft.sourceStoryInputPackId,
            reviewSourceStoryInputPackId = review.sourceStoryInputPackId,
        )

    private fun buildMappingEntries(
        draft: LocalRendererManifestDraftJsonDraft,
    ): List<LocalRendererManifestDraftJsonAdapterPanelMappingEntry> =
        draft.draftPanelManifestEntries
            .sortedWith(compareBy<LocalRendererDraftPanelManifestEntry> { it.order }.thenBy { it.startMillis })
            .map { panel ->
                val fields = allDraftFields(panel)
                val missing = missingPanelFields(panel, fields)
                LocalRendererManifestDraftJsonAdapterPanelMappingEntry(
                    order = panel.order,
                    panelId = panel.panelId,
                    sourceSceneId = panel.sourceSceneId,
                    sourceBeatId = panel.sourceBeatId,
                    startMillis = panel.startMillis,
                    endMillis = panel.endMillis,
                    durationMillis = panel.durationMillis,
                    panelTimelineFieldCount = panel.draftPanelTimelineFields.size,
                    voiceTimelineFieldCount = panel.draftVoiceTimelineFields.size,
                    subtitleTimelineFieldCount = panel.draftSubtitleTimelineFields.size,
                    mappedDraftFieldCount = fields.size,
                    mappedDraftFieldTargets = fields.map { "${it.targetSection}.${it.targetField}" },
                    missingRequiredFields = missing,
                    canMapForFutureAdapter = missing.isEmpty(),
                    adapterExecuted = false,
                    rendererInvocation = "none",
                    mediaGenerated = false,
                    productionManifestGenerated = false,
                    note = "Adapter dry-run mapping only; no ComicVideoRenderHandoffManifest generation, renderer execution, or media generation.",
                )
            }

    private fun missingPanelFields(
        panel: LocalRendererDraftPanelManifestEntry,
        fields: List<LocalRendererManifestDraftJsonField>,
    ): List<String> {
        val missing = mutableListOf<String>()
        fun require(value: Boolean, field: String) {
            if (!value) missing += field
        }
        require(panel.panelId.isNotBlank(), "panelId")
        require(panel.sourceSceneId.isNotBlank(), "sourceSceneId")
        require(panel.sourceBeatId.isNotBlank(), "sourceBeatId")
        require(panel.durationMillis > 0, "durationMillis")
        require(panel.endMillis > panel.startMillis, "endMillis")
        require(fields.isNotEmpty(), "draftFields")
        fields.filter { it.required }.forEachIndexed { index, field ->
            require(field.targetSection.isNotBlank(), "requiredField[$index].targetSection")
            require(field.targetField.isNotBlank(), "requiredField[$index].targetField")
            require(field.draftValueHint.isNotBlank(), "requiredField[$index].draftValueHint")
            require(field.draftOnly, "requiredField[$index].draftOnly")
            require(!field.productionReady, "requiredField[$index].productionReadyFalse")
        }
        return missing
    }

    private fun allDraftFields(panel: LocalRendererDraftPanelManifestEntry): List<LocalRendererManifestDraftJsonField> =
        panel.draftPanelTimelineFields + panel.draftVoiceTimelineFields + panel.draftSubtitleTimelineFields

    private fun fieldCoverageSummary(
        draft: LocalRendererManifestDraftJsonDraft,
        mappingEntries: List<LocalRendererManifestDraftJsonAdapterPanelMappingEntry>,
    ): LocalRendererManifestDraftJsonAdapterFieldCoverageSummary {
        val panelTimelineFields = draft.draftPanelManifestEntries.sumOf { it.draftPanelTimelineFields.size }
        val voiceTimelineFields = draft.draftPanelManifestEntries.sumOf { it.draftVoiceTimelineFields.size }
        val subtitleTimelineFields = draft.draftPanelManifestEntries.sumOf { it.draftSubtitleTimelineFields.size }
        val totalDraftFields = panelTimelineFields + voiceTimelineFields + subtitleTimelineFields
        return LocalRendererManifestDraftJsonAdapterFieldCoverageSummary(
            panelEntries = draft.draftPanelManifestEntries.size,
            panelTimelineFields = panelTimelineFields,
            voiceTimelineFields = voiceTimelineFields,
            subtitleTimelineFields = subtitleTimelineFields,
            totalDraftFields = totalDraftFields,
            mappedDraftFields = mappingEntries.sumOf { it.mappedDraftFieldCount },
            missingRequiredDraftFields = mappingEntries.sumOf { it.missingRequiredFields.size },
            draftOnly = true,
            productionManifestGenerated = false,
            productionReady = false,
        )
    }

    private fun panelMappingSummary(
        draft: LocalRendererManifestDraftJsonDraft,
        mappingEntries: List<LocalRendererManifestDraftJsonAdapterPanelMappingEntry>,
        fieldCoverage: LocalRendererManifestDraftJsonAdapterFieldCoverageSummary,
    ): LocalRendererManifestDraftJsonAdapterPanelMappingSummary =
        LocalRendererManifestDraftJsonAdapterPanelMappingSummary(
            draftPanelCount = draft.draftPanelManifestEntries.size,
            adapterDryRunMappingEntries = mappingEntries.size,
            mappedPanelCount = mappingEntries.count { it.canMapForFutureAdapter },
            unmappedPanelCount = mappingEntries.count { !it.canMapForFutureAdapter },
            missingRequiredFieldCount = fieldCoverage.missingRequiredDraftFields,
            targetDurationMillis = draft.draftManifestSummary.targetDurationMillis,
        )

    private fun adapterDryRunSummary(
        draft: LocalRendererManifestDraftJsonDraft,
        review: LocalRendererManifestDraftJsonReviewReport,
        mappingEntries: List<LocalRendererManifestDraftJsonAdapterPanelMappingEntry>,
        fieldCoverage: LocalRendererManifestDraftJsonAdapterFieldCoverageSummary,
        panelMappingSummary: LocalRendererManifestDraftJsonAdapterPanelMappingSummary,
        sourceProductionApprovedClaim: Boolean,
    ): LocalRendererManifestDraftJsonAdapterDryRunSummary =
        LocalRendererManifestDraftJsonAdapterDryRunSummary(
            sourceDraftChecks = draft.checks.size,
            sourceReviewChecks = review.checks.size,
            sourceReviewChecksPass = review.checks.count { it.status == "PASS" },
            sourceReviewChecksFail = review.checks.count { it.status != "PASS" },
            sourceDraftFindings = draft.findingCount,
            sourceReviewFindings = review.findingCount,
            targetDurationMillis = draft.draftManifestSummary.targetDurationMillis,
            draftPanelEntries = draft.draftPanelManifestEntries.size,
            adapterDryRunMappingEntries = mappingEntries.size,
            mappedPanelCount = panelMappingSummary.mappedPanelCount,
            unmappedPanelCount = panelMappingSummary.unmappedPanelCount,
            totalDraftFields = fieldCoverage.totalDraftFields,
            mappedDraftFields = fieldCoverage.mappedDraftFields,
            missingRequiredFieldCount = fieldCoverage.missingRequiredDraftFields,
            draftManifestGenerated = draft.boundaries.draftManifestGenerated &&
                review.boundaries.draftManifestGenerated,
            productionManifestGenerated = draft.boundaries.productionManifestGenerated ||
                review.boundaries.productionManifestGenerated,
            productionReady = draft.boundaries.productionReady || review.boundaries.productionReady,
            productionApproved = sourceProductionApprovedClaim || review.boundaries.productionApproved,
            adapterImplementationExecuted = draft.boundaries.adapterImplementationExecuted ||
                review.boundaries.adapterImplementationExecuted,
            adapterExecuted = false,
            rendererReady = draft.boundaries.rendererReady || review.boundaries.rendererReady,
            mp4ReadinessClaimed = draft.boundaries.mp4ReadinessClaimed ||
                review.boundaries.mp4ReadinessClaimed,
        )

    private fun boundarySummary(
        draft: LocalRendererManifestDraftJsonDraft,
        review: LocalRendererManifestDraftJsonReviewReport,
        sourceProductionApprovedClaim: Boolean,
    ): LocalRendererManifestDraftJsonAdapterDryRunBoundarySummary =
        LocalRendererManifestDraftJsonAdapterDryRunBoundarySummary(
            draftLocalOnly = localDraftBoundariesOk(draft.boundaries),
            reviewLocalOnly = localReviewBoundariesOk(review.boundaries),
            mediaGenerated = draft.boundaries.mediaGenerated || review.boundaries.mediaGenerated,
            rendererInvocation = firstUnsafeInvocation(
                draft.boundaries.rendererInvocation,
                review.boundaries.rendererInvocation,
            ),
            providerInvocation = firstUnsafeInvocation(
                draft.boundaries.providerInvocation,
                review.boundaries.providerInvocation,
            ),
            ttsInvocation = firstUnsafeInvocation(
                draft.boundaries.ttsInvocation,
                review.boundaries.ttsInvocation,
            ),
            adapterImplementationExecuted = draft.boundaries.adapterImplementationExecuted ||
                review.boundaries.adapterImplementationExecuted,
            adapterExecuted = false,
            draftManifestGenerated = draft.boundaries.draftManifestGenerated &&
                review.boundaries.draftManifestGenerated,
            productionManifestGenerated = draft.boundaries.productionManifestGenerated ||
                review.boundaries.productionManifestGenerated,
            productionReady = draft.boundaries.productionReady || review.boundaries.productionReady,
            productionApproved = sourceProductionApprovedClaim || review.boundaries.productionApproved,
            finalMediaApproval = draft.boundaries.finalMediaApproval || review.boundaries.finalMediaApproval,
            providerMediaHandoff = draft.boundaries.providerMediaHandoff || review.boundaries.providerMediaHandoff,
            release = draft.boundaries.release || review.boundaries.release,
            canonRegistryOrProductionLock = draft.boundaries.canonRegistryOrProductionLock ||
                review.boundaries.canonRegistryOrProductionLock,
            rendererReady = draft.boundaries.rendererReady || review.boundaries.rendererReady,
            mp4ReadinessClaimed = draft.boundaries.mp4ReadinessClaimed ||
                review.boundaries.mp4ReadinessClaimed,
        )

    private fun outputBoundaries(draftManifestGenerated: Boolean): LocalRendererManifestDraftJsonAdapterDryRunBoundaries =
        LocalRendererManifestDraftJsonAdapterDryRunBoundaries(
            fileProtocolJsonOnly = true,
            noExternalPaidProviderTools = true,
            providerInvocation = "none",
            ttsInvocation = "none",
            mediaGenerated = false,
            rendererInvocation = "none",
            directSqliteWrites = false,
            providerApiCalls = false,
            adapterImplementationExecuted = false,
            adapterExecuted = false,
            draftManifestGenerated = draftManifestGenerated,
            productionManifestGenerated = false,
            productionReady = false,
            productionApproved = false,
            finalMediaApproval = false,
            providerMediaHandoff = false,
            release = false,
            canonRegistryOrProductionLock = false,
            rendererReady = false,
            mp4ReadinessClaimed = false,
        )

    private fun localDraftBoundariesOk(boundaries: LocalRendererManifestDraftJsonDraftBoundaries): Boolean =
        boundaries.fileProtocolJsonOnly &&
            boundaries.noExternalPaidProviderTools &&
            boundaries.providerInvocation == "none" &&
            boundaries.ttsInvocation == "none" &&
            !boundaries.mediaGenerated &&
            boundaries.rendererInvocation == "none" &&
            !boundaries.directSqliteWrites &&
            !boundaries.providerApiCalls &&
            !boundaries.adapterImplementationExecuted &&
            boundaries.draftManifestGenerated &&
            !boundaries.productionManifestGenerated &&
            !boundaries.productionReady &&
            !boundaries.finalMediaApproval &&
            !boundaries.providerMediaHandoff &&
            !boundaries.release &&
            !boundaries.canonRegistryOrProductionLock &&
            !boundaries.rendererReady &&
            !boundaries.mp4ReadinessClaimed

    private fun localReviewBoundariesOk(boundaries: LocalRendererManifestDraftJsonReviewBoundaries): Boolean =
        boundaries.fileProtocolJsonOnly &&
            boundaries.noExternalPaidProviderTools &&
            boundaries.providerInvocation == "none" &&
            boundaries.ttsInvocation == "none" &&
            !boundaries.mediaGenerated &&
            boundaries.rendererInvocation == "none" &&
            !boundaries.directSqliteWrites &&
            !boundaries.providerApiCalls &&
            !boundaries.adapterImplementationExecuted &&
            boundaries.draftManifestGenerated &&
            !boundaries.productionManifestGenerated &&
            !boundaries.productionReady &&
            !boundaries.productionApproved &&
            !boundaries.finalMediaApproval &&
            !boundaries.providerMediaHandoff &&
            !boundaries.release &&
            !boundaries.canonRegistryOrProductionLock &&
            !boundaries.rendererReady &&
            !boundaries.mp4ReadinessClaimed

    private fun firstUnsafeInvocation(vararg values: String): String =
        values.firstOrNull { it != "none" } ?: "none"

    private fun rawProductionApprovedClaim(raw: String): Boolean =
        runCatching {
            containsTrueProductionApproved(JsonSupport.json.parseToJsonElement(raw))
        }.getOrDefault(false)

    private fun containsTrueProductionApproved(element: JsonElement): Boolean =
        when (element) {
            is JsonObject -> element.any { (key, value) ->
                (key == "productionApproved" &&
                    (value as? JsonPrimitive)?.booleanOrNull == true) ||
                    containsTrueProductionApproved(value)
            }
            is JsonArray -> element.any { containsTrueProductionApproved(it) }
            else -> false
        }

    private fun check(
        checkId: String,
        category: String,
        passed: Boolean,
        message: String,
        evidence: List<String>,
        findings: MutableList<LocalRendererManifestDraftJsonAdapterDryRunFinding>,
        ruleId: String,
        path: String,
        suggestedFix: String,
    ): LocalRendererManifestDraftJsonAdapterDryRunCheck {
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
        return LocalRendererManifestDraftJsonAdapterDryRunCheck(
            checkId = checkId,
            status = if (passed) "PASS" else "FAIL",
            category = category,
            message = message,
            evidence = evidence,
        )
    }

    private fun List<LocalRendererManifestDraftJsonAdapterDryRunFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): LocalRendererManifestDraftJsonAdapterDryRunFinding =
        LocalRendererManifestDraftJsonAdapterDryRunFinding(
            ruleId,
            category,
            severity,
            path,
            message,
            evidence,
            suggestedFix,
        )
}
