package com.localnovel.engine.service

import com.localnovel.engine.model.LocalRendererManifestDraftJsonDraft
import com.localnovel.engine.model.LocalRendererManifestDraftJsonDraftBoundaries
import com.localnovel.engine.model.LocalRendererManifestDraftJsonReviewBoundarySummary
import com.localnovel.engine.model.LocalRendererManifestDraftJsonReviewBoundaries
import com.localnovel.engine.model.LocalRendererManifestDraftJsonReviewCheck
import com.localnovel.engine.model.LocalRendererManifestDraftJsonReviewFinding
import com.localnovel.engine.model.LocalRendererManifestDraftJsonReviewReport
import com.localnovel.engine.model.LocalRendererManifestDraftJsonReviewSourceAlignmentSummary
import com.localnovel.engine.model.LocalRendererManifestDraftJsonReviewSummary
import com.localnovel.engine.model.LocalRendererManifestDraftJsonFieldCoverageReviewSummary
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

class LocalRendererManifestDraftJsonReviewService {
    private val requiredDraftStatus = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT"
    private val requiredDraftBoundary = "local_renderer_manifest_draft_json_draft_only_not_production_manifest"
    private val requiredDraftNextStep = "local_renderer_manifest_draft_json_review"
    private val passStatus = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_PASS"
    private val failStatus = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_FAIL"
    private val downstreamBoundary = "local_renderer_manifest_draft_json_review_only_not_production_approval"
    private val nextAllowedDownstreamStep = "local_renderer_manifest_draft_json_adapter_dry_run"
    private val requiredBoundaryFlags = listOf(
        "mediaGenerated=false",
        "rendererInvocation=none",
        "providerInvocation=none",
        "ttsInvocation=none",
        "adapterImplementationExecuted=false",
        "noExternalPaidProviderTools=true",
        "rendererReady=false",
        "mp4ReadinessClaimed=false",
        "draftManifestGenerated=true",
        "productionManifestGenerated=false",
        "productionReady=false",
        "productionApproved=false",
    )

    fun reviewFile(
        draftPath: Path,
        outPath: Path? = null,
        reviewId: String = "",
    ): LocalRendererManifestDraftJsonReviewReport {
        val raw = Files.readString(draftPath).removePrefix("\uFEFF")
        val draft = JsonSupport.json.decodeFromString<LocalRendererManifestDraftJsonDraft>(raw)
        val report = review(
            draft = draft,
            draftFile = draftPath.toString(),
            reviewId = reviewId,
            sourceProductionApprovedClaim = rawProductionApprovedClaim(raw),
        )
        if (outPath != null) {
            outPath.parent?.let { Files.createDirectories(it) }
            Files.writeString(outPath, JsonSupport.json.encodeToString(report))
        }
        return report
    }

    fun review(
        draft: LocalRendererManifestDraftJsonDraft,
        draftFile: String = "",
        reviewId: String = "",
        sourceProductionApprovedClaim: Boolean = false,
    ): LocalRendererManifestDraftJsonReviewReport {
        val findings = mutableListOf<LocalRendererManifestDraftJsonReviewFinding>()
        val alignment = sourceAlignmentSummary(draft)
        val fieldCoverage = fieldCoverageSummary(draft)
        val boundarySummary = boundarySummary(draft, sourceProductionApprovedClaim)
        val reviewSummary = draftManifestReviewSummary(draft, fieldCoverage, sourceProductionApprovedClaim)
        val checks = buildChecks(
            draft = draft,
            draftFile = draftFile,
            alignment = alignment,
            fieldCoverage = fieldCoverage,
            boundarySummary = boundarySummary,
            reviewSummary = reviewSummary,
            sourceProductionApprovedClaim = sourceProductionApprovedClaim,
            findings = findings,
        )
        val verdict = if (findings.hasBlocking()) "FAIL" else "PASS"
        val resolvedReviewId = reviewId.ifBlank {
            draft.draftId.takeIf { it.isNotBlank() }?.let { "$it-review-v0" }.orEmpty()
        }

        return LocalRendererManifestDraftJsonReviewReport(
            verdict = verdict,
            status = if (verdict == "PASS") passStatus else failStatus,
            reviewId = resolvedReviewId,
            sourceDraftFile = draftFile,
            sourceDraftId = draft.draftId,
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
            draftManifestReviewSummary = reviewSummary,
            draftFieldCoverageReviewSummary = fieldCoverage,
            boundarySummary = boundarySummary,
            boundaries = outputBoundaries(draft.boundaries.draftManifestGenerated),
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
        draftFile: String,
        alignment: LocalRendererManifestDraftJsonReviewSourceAlignmentSummary,
        fieldCoverage: LocalRendererManifestDraftJsonFieldCoverageReviewSummary,
        boundarySummary: LocalRendererManifestDraftJsonReviewBoundarySummary,
        reviewSummary: LocalRendererManifestDraftJsonReviewSummary,
        sourceProductionApprovedClaim: Boolean,
        findings: MutableList<LocalRendererManifestDraftJsonReviewFinding>,
    ): List<LocalRendererManifestDraftJsonReviewCheck> =
        listOf(
            check(
                checkId = "source-draft-present",
                category = "source_validation",
                passed = draftFile.isNotBlank() &&
                    draft.draftId.isNotBlank() &&
                    draft.sourceManifestDraftPlanId.isNotBlank() &&
                    draft.sourceManifestDraftPlanReviewId.isNotBlank() &&
                    draft.sourceStoryInputPackId.isNotBlank(),
                message = "Manifest draft JSON review requires an explicit Phase 14 draft file and non-empty source ids.",
                evidence = listOf(
                    "draftFile=$draftFile",
                    "draftId=${draft.draftId}",
                    "sourceManifestDraftPlanId=${draft.sourceManifestDraftPlanId}",
                    "sourceManifestDraftPlanReviewId=${draft.sourceManifestDraftPlanReviewId}",
                    "sourceStoryInputPackId=${draft.sourceStoryInputPackId}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_SOURCE_MISSING",
                path = "$.sourceDraft",
                suggestedFix = "Use a complete PASS Phase 14 draft JSON file and pass it explicitly.",
            ),
            check(
                checkId = "draft-state",
                category = "source_validation",
                passed = draft.verdict == "PASS" &&
                    draft.status == requiredDraftStatus &&
                    draft.downstreamBoundary == requiredDraftBoundary &&
                    draft.nextAllowedDownstreamStep == requiredDraftNextStep,
                message = "Manifest draft JSON review requires PASS Phase 14 draft state and draft-review downstream step.",
                evidence = listOf(
                    "verdict=${draft.verdict}",
                    "status=${draft.status}",
                    "downstreamBoundary=${draft.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${draft.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_STATE_INVALID",
                path = "$.draftState",
                suggestedFix = "Use the accepted PASS Phase 14 manifest draft JSON.",
            ),
            check(
                checkId = "source-ids-present-and-aligned",
                category = "source_linkage",
                passed = alignment.draftIdPresent &&
                    alignment.manifestDraftPlanIdPresent &&
                    alignment.manifestDraftPlanReviewIdPresent &&
                    alignment.sourceImplementationDraftIdPresent &&
                    alignment.sourceImplementationDraftReviewIdPresent &&
                    alignment.sourceStoryInputPackIdPresent &&
                    alignment.sourceAlignmentFromDraftPass,
                message = "Manifest draft JSON review requires non-empty source ids and PASS alignment evidence from Phase 14.",
                evidence = listOf(
                    "draftIdPresent=${alignment.draftIdPresent}",
                    "manifestDraftPlanIdPresent=${alignment.manifestDraftPlanIdPresent}",
                    "manifestDraftPlanReviewIdPresent=${alignment.manifestDraftPlanReviewIdPresent}",
                    "sourceImplementationDraftIdPresent=${alignment.sourceImplementationDraftIdPresent}",
                    "sourceImplementationDraftReviewIdPresent=${alignment.sourceImplementationDraftReviewIdPresent}",
                    "sourceStoryInputPackIdPresent=${alignment.sourceStoryInputPackIdPresent}",
                    "sourceAlignmentFromDraftPass=${alignment.sourceAlignmentFromDraftPass}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_SOURCE_ID_MISSING",
                path = "$.sourceAlignmentSummary",
                suggestedFix = "Use a Phase 14 draft generated from aligned Phase 12/13 inputs.",
            ),
            check(
                checkId = "local-boundaries",
                category = "boundary",
                passed = localBoundariesOk(draft.boundaries) &&
                    !sourceProductionApprovedClaim,
                message = "Manifest draft JSON review requires local-only no-media boundaries and no production approval claim.",
                evidence = listOf(
                    "mediaGenerated=${draft.boundaries.mediaGenerated}",
                    "rendererInvocation=${draft.boundaries.rendererInvocation}",
                    "providerInvocation=${draft.boundaries.providerInvocation}",
                    "ttsInvocation=${draft.boundaries.ttsInvocation}",
                    "adapterImplementationExecuted=${draft.boundaries.adapterImplementationExecuted}",
                    "noExternalPaidProviderTools=${draft.boundaries.noExternalPaidProviderTools}",
                    "draftManifestGenerated=${draft.boundaries.draftManifestGenerated}",
                    "productionManifestGenerated=${draft.boundaries.productionManifestGenerated}",
                    "productionReady=${draft.boundaries.productionReady}",
                    "productionApproved=$sourceProductionApprovedClaim",
                    "rendererReady=${draft.boundaries.rendererReady}",
                    "mp4ReadinessClaimed=${draft.boundaries.mp4ReadinessClaimed}",
                ) + requiredBoundaryFlags,
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_BOUNDARY_FAIL",
                path = "$.boundaries",
                suggestedFix = "Use local-only JSON with no provider/API, renderer, TTS, media, adapter execution, production manifest, production approval, readiness claim, release, or production lock.",
            ),
            check(
                checkId = "no-production-approval",
                category = "production_boundary",
                passed = !sourceProductionApprovedClaim &&
                    !draft.boundaries.productionManifestGenerated &&
                    !draft.boundaries.productionReady &&
                    !draft.boundaries.finalMediaApproval &&
                    !draft.boundaries.providerMediaHandoff &&
                    !draft.boundaries.release &&
                    !draft.boundaries.canonRegistryOrProductionLock &&
                    !reviewSummary.productionApproved,
                message = "Manifest draft JSON review is not production approval, not final media approval, and not provider/media handoff.",
                evidence = listOf(
                    "productionApproved=$sourceProductionApprovedClaim",
                    "productionManifestGenerated=${draft.boundaries.productionManifestGenerated}",
                    "productionReady=${draft.boundaries.productionReady}",
                    "finalMediaApproval=${draft.boundaries.finalMediaApproval}",
                    "providerMediaHandoff=${draft.boundaries.providerMediaHandoff}",
                    "release=${draft.boundaries.release}",
                    "canonRegistryOrProductionLock=${draft.boundaries.canonRegistryOrProductionLock}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_PRODUCTION_APPROVAL_FORBIDDEN",
                path = "$.productionApproval",
                suggestedFix = "Keep Phase 15 review-only; do not use production approval or final media approval wording.",
            ),
            check(
                checkId = "source-checks-pass",
                category = "review_gate",
                passed = draft.checks.isNotEmpty() &&
                    draft.checks.filter { it.requiredForDraft }.all { it.status == "PASS" },
                message = "Manifest draft JSON review requires non-empty Phase 14 checks and all required checks PASS.",
                evidence = listOf(
                    "checks=${draft.checks.size}",
                    "requiredFail=${draft.checks.count { it.requiredForDraft && it.status != "PASS" }}",
                    "summaryChecksFail=${reviewSummary.sourceDraftChecksFail}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_CHECKS_NOT_PASS",
                path = "$.checks",
                suggestedFix = "Resolve Phase 14 draft checks before reviewing the draft JSON.",
            ),
            check(
                checkId = "no-findings",
                category = "review_gate",
                passed = draft.findingCount == 0 && draft.findings.isEmpty(),
                message = "Manifest draft JSON review requires no Phase 14 findings.",
                evidence = listOf(
                    "findingCount=${draft.findingCount}",
                    "findings=${draft.findings.size}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_FINDINGS_PRESENT",
                path = "$.findings",
                suggestedFix = "Resolve Phase 14 draft findings before Phase 15 review.",
            ),
            check(
                checkId = "draft-content-present",
                category = "draft_completeness",
                passed = draft.draftPanelManifestEntries.isNotEmpty() &&
                    fieldCoverage.totalDraftFields > 0 &&
                    fieldCoverage.missingRequiredDraftFields == 0 &&
                    draft.draftManifestSummary.draftPanelEntries > 0 &&
                    draft.draftManifestSummary.draftManifestFields > 0,
                message = "Manifest draft JSON review requires draft panel entries and non-empty draft field coverage.",
                evidence = listOf(
                    "draftPanelManifestEntries=${draft.draftPanelManifestEntries.size}",
                    "summaryDraftPanelEntries=${draft.draftManifestSummary.draftPanelEntries}",
                    "totalDraftFields=${fieldCoverage.totalDraftFields}",
                    "missingRequiredDraftFields=${fieldCoverage.missingRequiredDraftFields}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_CONTENT_MISSING",
                path = "$.draftFieldCoverageReviewSummary",
                suggestedFix = "Use a complete Phase 14 draft with panel entries and draft field coverage.",
            ),
            check(
                checkId = "draft-only-not-production-manifest",
                category = "manifest_boundary",
                passed = draft.draftManifestSummary.draftOnly &&
                    draft.draftManifestSummary.draftManifestGenerated &&
                    !draft.draftManifestSummary.productionManifestGenerated &&
                    !draft.draftManifestSummary.productionReady &&
                    draft.draftFieldCoverageSummary.draftOnly &&
                    !draft.draftFieldCoverageSummary.productionManifestGenerated &&
                    !draft.draftFieldCoverageSummary.productionReady &&
                    draft.draftPanelManifestEntries.all {
                        it.draftOnly &&
                            it.draftManifestGenerated &&
                            !it.productionManifestGenerated &&
                            !it.productionReady &&
                            !it.rendererReady
                    },
                message = "Manifest draft JSON review may approve review readiness only; it must not approve a real production ComicVideoRenderHandoffManifest.",
                evidence = listOf(
                    "draftOnly=${draft.draftManifestSummary.draftOnly}",
                    "draftManifestGenerated=${draft.draftManifestSummary.draftManifestGenerated}",
                    "productionManifestGenerated=${draft.draftManifestSummary.productionManifestGenerated}",
                    "productionReady=${draft.draftManifestSummary.productionReady}",
                    "panelProductionReady=${draft.draftPanelManifestEntries.count { it.productionReady }}",
                    "panelProductionManifestGenerated=${draft.draftPanelManifestEntries.count { it.productionManifestGenerated }}",
                    "draft-only=true",
                    "productionApproved=false",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_PRODUCTION_MANIFEST_FORBIDDEN",
                path = "$.draftManifest",
                suggestedFix = "Keep Phase 15 as review-only and defer production manifest approval to a future explicit decision.",
            ),
            check(
                checkId = "no-readiness-overclaim",
                category = "renderer_compatibility",
                passed = !draft.boundaries.rendererReady &&
                    !draft.boundaries.mp4ReadinessClaimed &&
                    !draft.draftManifestSummary.rendererReady &&
                    !draft.draftManifestSummary.mp4ReadinessClaimed,
                message = "Manifest draft JSON review must preserve rendererReady=false and mp4ReadinessClaimed=false.",
                evidence = listOf(
                    "boundaries.rendererReady=${draft.boundaries.rendererReady}",
                    "boundaries.mp4ReadinessClaimed=${draft.boundaries.mp4ReadinessClaimed}",
                    "summary.rendererReady=${draft.draftManifestSummary.rendererReady}",
                    "summary.mp4ReadinessClaimed=${draft.draftManifestSummary.mp4ReadinessClaimed}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_READY_OVERCLAIM",
                path = "$.rendererCompatibility",
                suggestedFix = "Keep rendererReady=false and mp4ReadinessClaimed=false until future explicit renderer/MP4 verification.",
            ),
        )

    private fun sourceAlignmentSummary(
        draft: LocalRendererManifestDraftJsonDraft,
    ): LocalRendererManifestDraftJsonReviewSourceAlignmentSummary =
        LocalRendererManifestDraftJsonReviewSourceAlignmentSummary(
            draftIdPresent = draft.draftId.isNotBlank(),
            manifestDraftPlanIdPresent = draft.sourceManifestDraftPlanId.isNotBlank(),
            manifestDraftPlanReviewIdPresent = draft.sourceManifestDraftPlanReviewId.isNotBlank(),
            sourceImplementationDraftIdPresent = draft.sourceImplementationDraftId.isNotBlank(),
            sourceImplementationDraftReviewIdPresent = draft.sourceImplementationDraftReviewId.isNotBlank(),
            sourceStoryInputPackIdPresent = draft.sourceStoryInputPackId.isNotBlank(),
            sourceAlignmentFromDraftPass = draft.sourceAlignmentSummary.manifestDraftPlanIdAligned &&
                draft.sourceAlignmentSummary.sourceImplementationDraftIdAligned &&
                draft.sourceAlignmentSummary.sourceImplementationDraftReviewIdAligned &&
                draft.sourceAlignmentSummary.sourcePlanIdAligned &&
                draft.sourceAlignmentSummary.sourcePlanReviewIdAligned &&
                draft.sourceAlignmentSummary.sourceDryRunIdAligned &&
                draft.sourceAlignmentSummary.sourceHandoffIdAligned &&
                draft.sourceAlignmentSummary.sourceHandoffReviewIdAligned &&
                draft.sourceAlignmentSummary.sourceStoryInputPackIdAligned,
            draftId = draft.draftId,
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
        )

    private fun draftManifestReviewSummary(
        draft: LocalRendererManifestDraftJsonDraft,
        fieldCoverage: LocalRendererManifestDraftJsonFieldCoverageReviewSummary,
        sourceProductionApprovedClaim: Boolean,
    ): LocalRendererManifestDraftJsonReviewSummary =
        LocalRendererManifestDraftJsonReviewSummary(
            sourceDraftChecks = draft.checks.size,
            sourceDraftChecksPass = draft.checks.count { it.status == "PASS" },
            sourceDraftChecksFail = draft.checks.count { it.status != "PASS" },
            draftPanelEntries = draft.draftPanelManifestEntries.size,
            draftManifestFields = fieldCoverage.totalDraftFields,
            targetDurationMillis = draft.draftManifestSummary.targetDurationMillis,
            sourceFindingCount = draft.findingCount,
            draftOnly = draft.draftManifestSummary.draftOnly,
            draftManifestGenerated = draft.boundaries.draftManifestGenerated &&
                draft.draftManifestSummary.draftManifestGenerated,
            productionManifestGenerated = draft.boundaries.productionManifestGenerated ||
                draft.draftManifestSummary.productionManifestGenerated,
            productionReady = draft.boundaries.productionReady || draft.draftManifestSummary.productionReady,
            productionApproved = sourceProductionApprovedClaim,
            adapterImplementationExecuted = draft.boundaries.adapterImplementationExecuted ||
                draft.draftManifestSummary.adapterImplementationExecuted,
            rendererReady = draft.boundaries.rendererReady || draft.draftManifestSummary.rendererReady,
            mp4ReadinessClaimed = draft.boundaries.mp4ReadinessClaimed ||
                draft.draftManifestSummary.mp4ReadinessClaimed,
        )

    private fun fieldCoverageSummary(
        draft: LocalRendererManifestDraftJsonDraft,
    ): LocalRendererManifestDraftJsonFieldCoverageReviewSummary =
        LocalRendererManifestDraftJsonFieldCoverageReviewSummary(
            panelEntries = draft.draftFieldCoverageSummary.panelEntries,
            panelTimelineFields = draft.draftFieldCoverageSummary.panelTimelineFields,
            voiceTimelineFields = draft.draftFieldCoverageSummary.voiceTimelineFields,
            subtitleTimelineFields = draft.draftFieldCoverageSummary.subtitleTimelineFields,
            totalDraftFields = draft.draftFieldCoverageSummary.totalDraftFields,
            missingRequiredDraftFields = draft.draftFieldCoverageSummary.missingRequiredDraftFields,
            draftOnly = draft.draftFieldCoverageSummary.draftOnly,
            productionManifestGenerated = draft.draftFieldCoverageSummary.productionManifestGenerated,
            productionReady = draft.draftFieldCoverageSummary.productionReady,
        )

    private fun boundarySummary(
        draft: LocalRendererManifestDraftJsonDraft,
        sourceProductionApprovedClaim: Boolean,
    ): LocalRendererManifestDraftJsonReviewBoundarySummary =
        LocalRendererManifestDraftJsonReviewBoundarySummary(
            fileProtocolJsonOnly = draft.boundaries.fileProtocolJsonOnly,
            noExternalPaidProviderTools = draft.boundaries.noExternalPaidProviderTools,
            providerInvocation = draft.boundaries.providerInvocation,
            ttsInvocation = draft.boundaries.ttsInvocation,
            mediaGenerated = draft.boundaries.mediaGenerated,
            rendererInvocation = draft.boundaries.rendererInvocation,
            directSqliteWrites = draft.boundaries.directSqliteWrites,
            providerApiCalls = draft.boundaries.providerApiCalls,
            adapterImplementationExecuted = draft.boundaries.adapterImplementationExecuted,
            draftManifestGenerated = draft.boundaries.draftManifestGenerated,
            productionManifestGenerated = draft.boundaries.productionManifestGenerated,
            productionReady = draft.boundaries.productionReady,
            productionApproved = sourceProductionApprovedClaim,
            finalMediaApproval = draft.boundaries.finalMediaApproval,
            providerMediaHandoff = draft.boundaries.providerMediaHandoff,
            release = draft.boundaries.release,
            canonRegistryOrProductionLock = draft.boundaries.canonRegistryOrProductionLock,
            rendererReady = draft.boundaries.rendererReady,
            mp4ReadinessClaimed = draft.boundaries.mp4ReadinessClaimed,
        )

    private fun outputBoundaries(draftManifestGenerated: Boolean): LocalRendererManifestDraftJsonReviewBoundaries =
        LocalRendererManifestDraftJsonReviewBoundaries(
            fileProtocolJsonOnly = true,
            noExternalPaidProviderTools = true,
            providerInvocation = "none",
            ttsInvocation = "none",
            mediaGenerated = false,
            rendererInvocation = "none",
            directSqliteWrites = false,
            providerApiCalls = false,
            adapterImplementationExecuted = false,
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

    private fun localBoundariesOk(boundaries: LocalRendererManifestDraftJsonDraftBoundaries): Boolean =
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

    private fun rawProductionApprovedClaim(raw: String): Boolean =
        runCatching {
            containsTrueProductionApproved(JsonSupport.json.parseToJsonElement(raw))
        }.getOrDefault(false)

    private fun containsTrueProductionApproved(element: JsonElement): Boolean =
        when (element) {
            is JsonObject -> element.any { (key, value) ->
                key == "productionApproved" &&
                    (value as? JsonPrimitive)?.booleanOrNull == true ||
                    containsTrueProductionApproved(value)
            }
            else -> false
        }

    private fun check(
        checkId: String,
        category: String,
        passed: Boolean,
        message: String,
        evidence: List<String>,
        findings: MutableList<LocalRendererManifestDraftJsonReviewFinding>,
        ruleId: String,
        path: String,
        suggestedFix: String,
    ): LocalRendererManifestDraftJsonReviewCheck {
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
        return LocalRendererManifestDraftJsonReviewCheck(
            checkId = checkId,
            status = if (passed) "PASS" else "FAIL",
            category = category,
            message = message,
            evidence = evidence,
        )
    }

    private fun List<LocalRendererManifestDraftJsonReviewFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): LocalRendererManifestDraftJsonReviewFinding =
        LocalRendererManifestDraftJsonReviewFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
}
