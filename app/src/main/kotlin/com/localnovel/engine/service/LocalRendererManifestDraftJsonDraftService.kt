package com.localnovel.engine.service

import com.localnovel.engine.model.LocalRendererAdapterImplementationDraftBoundaries
import com.localnovel.engine.model.LocalRendererDraftPanelManifestEntry
import com.localnovel.engine.model.LocalRendererManifestDraftJsonDraft
import com.localnovel.engine.model.LocalRendererManifestDraftJsonDraftBoundaries
import com.localnovel.engine.model.LocalRendererManifestDraftJsonDraftBoundarySummary
import com.localnovel.engine.model.LocalRendererManifestDraftJsonDraftCheck
import com.localnovel.engine.model.LocalRendererManifestDraftJsonDraftFinding
import com.localnovel.engine.model.LocalRendererManifestDraftJsonDraftSourceAlignmentSummary
import com.localnovel.engine.model.LocalRendererManifestDraftJsonDraftSummary
import com.localnovel.engine.model.LocalRendererManifestDraftJsonField
import com.localnovel.engine.model.LocalRendererManifestDraftJsonFieldCoverageSummary
import com.localnovel.engine.model.LocalRendererManifestDraftPlan
import com.localnovel.engine.model.LocalRendererManifestDraftPlanReviewReport
import com.localnovel.engine.model.LocalRendererManifestFieldMappingPlan
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class LocalRendererManifestDraftJsonDraftService {
    private val requiredPlanStatus = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN"
    private val requiredPlanBoundary = "local_renderer_manifest_draft_plan_only_not_manifest_generation"
    private val requiredPlanNextStep = "local_renderer_manifest_draft_plan_review"
    private val requiredReviewStatus = "LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_PASS"
    private val requiredReviewBoundary = "local_renderer_manifest_draft_plan_review_only_not_manifest_generation"
    private val requiredReviewNextStep = "local_renderer_manifest_draft_json_draft"
    private val passStatus = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT"
    private val failStatus = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT_FAIL"
    private val downstreamBoundary = "local_renderer_manifest_draft_json_draft_only_not_production_manifest"
    private val nextAllowedDownstreamStep = "local_renderer_manifest_draft_json_review"
    private val requiredBoundaryFlags = listOf(
        "mediaGenerated=false",
        "rendererInvocation=none",
        "providerInvocation=none",
        "ttsInvocation=none",
        "adapterImplementationExecuted=false",
        "noExternalPaidProviderTools=true",
        "rendererReady=false",
        "mp4ReadinessClaimed=false",
        "productionManifestGenerated=false",
        "productionReady=false",
    )

    fun draftFile(
        planPath: Path,
        planReviewPath: Path,
        outPath: Path? = null,
        draftId: String = "",
    ): LocalRendererManifestDraftJsonDraft {
        val plan = JsonSupport.json.decodeFromString<LocalRendererManifestDraftPlan>(
            Files.readString(planPath).removePrefix("\uFEFF"),
        )
        val review = JsonSupport.json.decodeFromString<LocalRendererManifestDraftPlanReviewReport>(
            Files.readString(planReviewPath).removePrefix("\uFEFF"),
        )
        val draft = draft(
            plan = plan,
            review = review,
            planFile = planPath.toString(),
            planReviewFile = planReviewPath.toString(),
            draftId = draftId,
        )
        if (outPath != null) {
            outPath.parent?.let { Files.createDirectories(it) }
            Files.writeString(outPath, JsonSupport.json.encodeToString(draft))
        }
        return draft
    }

    fun draft(
        plan: LocalRendererManifestDraftPlan,
        review: LocalRendererManifestDraftPlanReviewReport,
        planFile: String = "",
        planReviewFile: String = "",
        draftId: String = "",
    ): LocalRendererManifestDraftJsonDraft {
        val findings = mutableListOf<LocalRendererManifestDraftJsonDraftFinding>()
        val alignment = sourceAlignmentSummary(plan, review)
        val coverage = fieldCoverageSummary(plan)
        val boundarySummary = boundarySummary(plan, review)
        val preSummary = draftManifestSummary(plan, review, coverage, draftManifestGenerated = true)
        val checks = buildChecks(
            plan = plan,
            review = review,
            planFile = planFile,
            planReviewFile = planReviewFile,
            alignment = alignment,
            coverage = coverage,
            boundarySummary = boundarySummary,
            findings = findings,
        )
        val verdict = if (findings.hasBlocking()) "FAIL" else "PASS"
        val resolvedDraftId = draftId.ifBlank {
            review.reviewId.takeIf { it.isNotBlank() }?.let { "$it-draft-json-v0" }
                ?: plan.planId.takeIf { it.isNotBlank() }?.let { "$it-draft-json-v0" }
                ?: ""
        }
        val draftGenerated = verdict == "PASS"

        return LocalRendererManifestDraftJsonDraft(
            verdict = verdict,
            status = if (verdict == "PASS") passStatus else failStatus,
            draftId = resolvedDraftId,
            sourcePlanFile = planFile,
            sourcePlanReviewFile = planReviewFile,
            sourceManifestDraftPlanId = plan.planId,
            sourceManifestDraftPlanReviewId = review.reviewId,
            sourceImplementationDraftId = plan.sourceDraftId,
            sourceImplementationDraftReviewId = plan.sourceDraftReviewId,
            sourcePlanId = plan.sourcePlanId,
            sourcePlanReviewId = plan.sourcePlanReviewId,
            sourceDryRunId = plan.sourceDryRunId,
            sourceHandoffId = plan.sourceHandoffId,
            sourceHandoffReviewId = plan.sourceHandoffReviewId,
            sourceStoryInputPackId = plan.sourceStoryInputPackId,
            checks = checks,
            sourceAlignmentSummary = alignment,
            draftManifestSummary = preSummary.copy(draftManifestGenerated = draftGenerated),
            draftPanelManifestEntries = if (draftGenerated) draftPanelEntries(plan) else emptyList(),
            draftFieldCoverageSummary = coverage.copy(
                draftOnly = true,
                productionManifestGenerated = false,
                productionReady = false,
            ),
            boundarySummary = boundarySummary,
            boundaries = outputBoundaries(draftGenerated),
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
        plan: LocalRendererManifestDraftPlan,
        review: LocalRendererManifestDraftPlanReviewReport,
        planFile: String,
        planReviewFile: String,
        alignment: LocalRendererManifestDraftJsonDraftSourceAlignmentSummary,
        coverage: LocalRendererManifestDraftJsonFieldCoverageSummary,
        boundarySummary: LocalRendererManifestDraftJsonDraftBoundarySummary,
        findings: MutableList<LocalRendererManifestDraftJsonDraftFinding>,
    ): List<LocalRendererManifestDraftJsonDraftCheck> =
        listOf(
            check(
                checkId = "explicit-source-files",
                category = "source_validation",
                passed = planFile.isNotBlank() &&
                    planReviewFile.isNotBlank() &&
                    plan.planId.isNotBlank() &&
                    review.reviewId.isNotBlank() &&
                    plan.sourceStoryInputPackId.isNotBlank(),
                message = "Manifest draft JSON draft requires explicit Phase 12 plan and Phase 13 review files.",
                evidence = listOf(
                    "planFile=$planFile",
                    "planReviewFile=$planReviewFile",
                    "planId=${plan.planId}",
                    "reviewId=${review.reviewId}",
                    "sourceStoryInputPackId=${plan.sourceStoryInputPackId}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT_SOURCE_FILE_MISSING",
                path = "$.inputs",
                suggestedFix = "Pass --plan, --plan-review, and --out explicitly.",
            ),
            check(
                checkId = "plan-state",
                category = "source_validation",
                passed = plan.verdict == "PASS" &&
                    plan.status == requiredPlanStatus &&
                    plan.downstreamBoundary == requiredPlanBoundary &&
                    plan.nextAllowedDownstreamStep == requiredPlanNextStep,
                message = "Manifest draft JSON draft requires PASS Phase 12 manifest draft plan state.",
                evidence = listOf(
                    "verdict=${plan.verdict}",
                    "status=${plan.status}",
                    "downstreamBoundary=${plan.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${plan.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT_PLAN_STATE_INVALID",
                path = "$.plan",
                suggestedFix = "Use the accepted PASS Phase 12 manifest draft plan.",
            ),
            check(
                checkId = "plan-review-state",
                category = "source_validation",
                passed = review.verdict == "PASS" &&
                    review.status == requiredReviewStatus &&
                    review.downstreamBoundary == requiredReviewBoundary &&
                    review.nextAllowedDownstreamStep == requiredReviewNextStep,
                message = "Manifest draft JSON draft requires PASS Phase 13 manifest draft plan review state.",
                evidence = listOf(
                    "verdict=${review.verdict}",
                    "status=${review.status}",
                    "downstreamBoundary=${review.downstreamBoundary}",
                    "nextAllowedDownstreamStep=${review.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT_REVIEW_STATE_INVALID",
                path = "$.planReview",
                suggestedFix = "Use the accepted PASS Phase 13 manifest draft plan review report.",
            ),
            check(
                checkId = "source-ids-align",
                category = "source_linkage",
                passed = alignment.manifestDraftPlanIdAligned &&
                    alignment.sourceImplementationDraftIdAligned &&
                    alignment.sourceImplementationDraftReviewIdAligned &&
                    alignment.sourcePlanIdAligned &&
                    alignment.sourcePlanReviewIdAligned &&
                    alignment.sourceDryRunIdAligned &&
                    alignment.sourceHandoffIdAligned &&
                    alignment.sourceHandoffReviewIdAligned &&
                    alignment.sourceStoryInputPackIdAligned,
                message = "Manifest draft JSON draft requires Phase 12 and Phase 13 source ids to align.",
                evidence = listOf(
                    "manifestDraftPlanIdAligned=${alignment.manifestDraftPlanIdAligned}",
                    "sourceImplementationDraftIdAligned=${alignment.sourceImplementationDraftIdAligned}",
                    "sourceImplementationDraftReviewIdAligned=${alignment.sourceImplementationDraftReviewIdAligned}",
                    "sourcePlanIdAligned=${alignment.sourcePlanIdAligned}",
                    "sourcePlanReviewIdAligned=${alignment.sourcePlanReviewIdAligned}",
                    "sourceDryRunIdAligned=${alignment.sourceDryRunIdAligned}",
                    "sourceHandoffIdAligned=${alignment.sourceHandoffIdAligned}",
                    "sourceHandoffReviewIdAligned=${alignment.sourceHandoffReviewIdAligned}",
                    "sourceStoryInputPackIdAligned=${alignment.sourceStoryInputPackIdAligned}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT_SOURCE_ID_MISMATCH",
                path = "$.sourceAlignmentSummary",
                suggestedFix = "Use Phase 12 and Phase 13 JSON files produced from the same story-video chain.",
            ),
            check(
                checkId = "local-boundaries",
                category = "boundary",
                passed = boundarySummary.planLocalOnly &&
                    boundarySummary.reviewLocalOnly &&
                    !boundarySummary.mediaGenerated &&
                    boundarySummary.rendererInvocation == "none" &&
                    boundarySummary.providerInvocation == "none" &&
                    boundarySummary.ttsInvocation == "none" &&
                    !boundarySummary.adapterImplementationExecuted &&
                    !boundarySummary.productionManifestGenerated &&
                    !boundarySummary.productionReady &&
                    !boundarySummary.finalMediaApproval &&
                    !boundarySummary.providerMediaHandoff &&
                    !boundarySummary.canonRegistryOrProductionLock &&
                    !boundarySummary.rendererReady &&
                    !boundarySummary.mp4ReadinessClaimed,
                message = "Manifest draft JSON draft requires local-only no-media and no-production-manifest boundaries.",
                evidence = listOf(
                    "planLocalOnly=${boundarySummary.planLocalOnly}",
                    "reviewLocalOnly=${boundarySummary.reviewLocalOnly}",
                    "mediaGenerated=${boundarySummary.mediaGenerated}",
                    "rendererInvocation=${boundarySummary.rendererInvocation}",
                    "providerInvocation=${boundarySummary.providerInvocation}",
                    "ttsInvocation=${boundarySummary.ttsInvocation}",
                    "adapterImplementationExecuted=${boundarySummary.adapterImplementationExecuted}",
                    "productionManifestGenerated=${boundarySummary.productionManifestGenerated}",
                    "productionReady=${boundarySummary.productionReady}",
                    "rendererReady=${boundarySummary.rendererReady}",
                    "mp4ReadinessClaimed=${boundarySummary.mp4ReadinessClaimed}",
                ) + requiredBoundaryFlags,
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT_BOUNDARY_FAIL",
                path = "$.boundaries",
                suggestedFix = "Use local-only JSON with no provider/API, renderer, TTS, media, adapter execution, production manifest, readiness claim, release, or production lock.",
            ),
            check(
                checkId = "no-findings-in-sources",
                category = "review_gate",
                passed = plan.findingCount == 0 &&
                    plan.findings.isEmpty() &&
                    review.findingCount == 0 &&
                    review.findings.isEmpty(),
                message = "Manifest draft JSON draft requires no findings in Phase 12 plan or Phase 13 review.",
                evidence = listOf(
                    "planFindingCount=${plan.findingCount}",
                    "reviewFindingCount=${review.findingCount}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT_SOURCE_FINDINGS_PRESENT",
                path = "$.sourceFindings",
                suggestedFix = "Resolve Phase 12/13 findings before drafting manifest JSON.",
            ),
            check(
                checkId = "source-checks-pass",
                category = "review_gate",
                passed = plan.checks.isNotEmpty() &&
                    plan.checks.filter { it.requiredForPlan }.all { it.status == "PASS" } &&
                    review.checks.isNotEmpty() &&
                    review.checks.filter { it.requiredForReview }.all { it.status == "PASS" },
                message = "Manifest draft JSON draft requires non-empty Phase 12/13 checks with all required checks PASS.",
                evidence = listOf(
                    "planChecks=${plan.checks.size}",
                    "planRequiredFail=${plan.checks.count { it.requiredForPlan && it.status != "PASS" }}",
                    "reviewChecks=${review.checks.size}",
                    "reviewRequiredFail=${review.checks.count { it.requiredForReview && it.status != "PASS" }}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT_SOURCE_CHECKS_NOT_PASS",
                path = "$.sourceChecks",
                suggestedFix = "Resolve Phase 12/13 required checks before drafting manifest JSON.",
            ),
            check(
                checkId = "draft-content-present",
                category = "draft_completeness",
                passed = coverage.panelEntries > 0 &&
                    coverage.totalDraftFields > 0 &&
                    coverage.missingRequiredDraftFields == 0 &&
                    plan.requiredManifestDraftWorkItems.isNotEmpty() &&
                    plan.verificationPlan.isNotEmpty() &&
                    plan.manifestFieldPlan.renderSettingsPlan.totalDurationMillis > 0,
                message = "Manifest draft JSON draft requires draft panels, draft fields, work items, verification plan, and positive duration.",
                evidence = listOf(
                    "panelEntries=${coverage.panelEntries}",
                    "totalDraftFields=${coverage.totalDraftFields}",
                    "missingRequiredDraftFields=${coverage.missingRequiredDraftFields}",
                    "requiredManifestDraftWorkItems=${plan.requiredManifestDraftWorkItems.size}",
                    "verificationSteps=${plan.verificationPlan.size}",
                    "targetDurationMillis=${plan.manifestFieldPlan.renderSettingsPlan.totalDurationMillis}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT_CONTENT_MISSING",
                path = "$.draftManifestSummary",
                suggestedFix = "Use a complete Phase 12 plan with panel field plans, work items, and verification plan.",
            ),
            check(
                checkId = "draft-only-not-production-manifest",
                category = "manifest_boundary",
                passed = plan.manifestFieldPlan.planningOnly &&
                    !plan.manifestFieldPlan.manifestGenerated &&
                    plan.manifestFieldPlan.panelManifestPlans.all { !it.manifestGenerated && !it.rendererReady } &&
                    manifestFieldMappings(plan).all { !it.generatedNow } &&
                    !plan.manifestFieldPlan.renderSettingsPlan.generatedNow &&
                    !review.manifestDraftPlanReadinessSummary.realManifestGenerated,
                message = "Manifest draft JSON draft may create draft-only JSON, not a real production ComicVideoRenderHandoffManifest.",
                evidence = listOf(
                    "planningOnly=${plan.manifestFieldPlan.planningOnly}",
                    "sourcePlanManifestGenerated=${plan.manifestFieldPlan.manifestGenerated}",
                    "sourcePanelManifestGenerated=${plan.manifestFieldPlan.panelManifestPlans.count { it.manifestGenerated }}",
                    "sourcePanelRendererReady=${plan.manifestFieldPlan.panelManifestPlans.count { it.rendererReady }}",
                    "generatedNowFields=${manifestFieldMappings(plan).count { it.generatedNow }}",
                    "renderSettingsGeneratedNow=${plan.manifestFieldPlan.renderSettingsPlan.generatedNow}",
                    "reviewRealManifestGenerated=${review.manifestDraftPlanReadinessSummary.realManifestGenerated}",
                    "draft-only=true",
                    "productionManifestGenerated=false",
                    "productionReady=false",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT_PRODUCTION_MANIFEST_FORBIDDEN",
                path = "$.draftManifest",
                suggestedFix = "Keep Phase 14 draft-only and defer any real production ComicVideoRenderHandoffManifest to a future explicit task.",
            ),
            check(
                checkId = "no-readiness-overclaim",
                category = "renderer_compatibility",
                passed = !plan.boundaries.rendererReady &&
                    !plan.boundaries.mp4ReadinessClaimed &&
                    !review.boundaries.rendererReady &&
                    !review.boundaries.mp4ReadinessClaimed &&
                    !review.manifestDraftPlanReadinessSummary.rendererReady &&
                    !review.manifestDraftPlanReadinessSummary.mp4ReadinessClaimed,
                message = "Manifest draft JSON draft must preserve rendererReady=false and mp4ReadinessClaimed=false.",
                evidence = listOf(
                    "plan.rendererReady=${plan.boundaries.rendererReady}",
                    "plan.mp4ReadinessClaimed=${plan.boundaries.mp4ReadinessClaimed}",
                    "review.rendererReady=${review.boundaries.rendererReady}",
                    "review.mp4ReadinessClaimed=${review.boundaries.mp4ReadinessClaimed}",
                    "reviewReadiness.rendererReady=${review.manifestDraftPlanReadinessSummary.rendererReady}",
                    "reviewReadiness.mp4ReadinessClaimed=${review.manifestDraftPlanReadinessSummary.mp4ReadinessClaimed}",
                ),
                findings = findings,
                ruleId = "LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT_READY_OVERCLAIM",
                path = "$.rendererCompatibility",
                suggestedFix = "Keep rendererReady=false and mp4ReadinessClaimed=false until future explicit renderer/MP4 verification.",
            ),
        )

    private fun sourceAlignmentSummary(
        plan: LocalRendererManifestDraftPlan,
        review: LocalRendererManifestDraftPlanReviewReport,
    ): LocalRendererManifestDraftJsonDraftSourceAlignmentSummary =
        LocalRendererManifestDraftJsonDraftSourceAlignmentSummary(
            manifestDraftPlanIdAligned = plan.planId.isNotBlank() &&
                plan.planId == review.sourceManifestDraftPlanId,
            sourceImplementationDraftIdAligned = plan.sourceDraftId.isNotBlank() &&
                plan.sourceDraftId == review.sourceImplementationDraftId,
            sourceImplementationDraftReviewIdAligned = plan.sourceDraftReviewId.isNotBlank() &&
                plan.sourceDraftReviewId == review.sourceImplementationDraftReviewId,
            sourcePlanIdAligned = plan.sourcePlanId.isNotBlank() &&
                plan.sourcePlanId == review.sourcePlanId,
            sourcePlanReviewIdAligned = plan.sourcePlanReviewId.isNotBlank() &&
                plan.sourcePlanReviewId == review.sourcePlanReviewId,
            sourceDryRunIdAligned = plan.sourceDryRunId.isNotBlank() &&
                plan.sourceDryRunId == review.sourceDryRunId,
            sourceHandoffIdAligned = plan.sourceHandoffId.isNotBlank() &&
                plan.sourceHandoffId == review.sourceHandoffId,
            sourceHandoffReviewIdAligned = plan.sourceHandoffReviewId.isNotBlank() &&
                plan.sourceHandoffReviewId == review.sourceHandoffReviewId,
            sourceStoryInputPackIdAligned = plan.sourceStoryInputPackId.isNotBlank() &&
                plan.sourceStoryInputPackId == review.sourceStoryInputPackId,
            manifestDraftPlanId = plan.planId,
            reviewSourceManifestDraftPlanId = review.sourceManifestDraftPlanId,
            planSourceImplementationDraftId = plan.sourceDraftId,
            reviewSourceImplementationDraftId = review.sourceImplementationDraftId,
            planSourceImplementationDraftReviewId = plan.sourceDraftReviewId,
            reviewSourceImplementationDraftReviewId = review.sourceImplementationDraftReviewId,
            planSourcePlanId = plan.sourcePlanId,
            reviewSourcePlanId = review.sourcePlanId,
            planSourcePlanReviewId = plan.sourcePlanReviewId,
            reviewSourcePlanReviewId = review.sourcePlanReviewId,
            planSourceDryRunId = plan.sourceDryRunId,
            reviewSourceDryRunId = review.sourceDryRunId,
            planSourceHandoffId = plan.sourceHandoffId,
            reviewSourceHandoffId = review.sourceHandoffId,
            planSourceHandoffReviewId = plan.sourceHandoffReviewId,
            reviewSourceHandoffReviewId = review.sourceHandoffReviewId,
            planSourceStoryInputPackId = plan.sourceStoryInputPackId,
            reviewSourceStoryInputPackId = review.sourceStoryInputPackId,
        )

    private fun draftManifestSummary(
        plan: LocalRendererManifestDraftPlan,
        review: LocalRendererManifestDraftPlanReviewReport,
        coverage: LocalRendererManifestDraftJsonFieldCoverageSummary,
        draftManifestGenerated: Boolean,
    ): LocalRendererManifestDraftJsonDraftSummary =
        LocalRendererManifestDraftJsonDraftSummary(
            sourcePlanChecks = plan.checks.size,
            sourcePlanReviewChecks = review.checks.size,
            sourcePlanReviewChecksPass = review.checks.count { it.status == "PASS" },
            sourcePlanReviewChecksFail = review.checks.count { it.status != "PASS" },
            draftPanelEntries = coverage.panelEntries,
            draftManifestFields = coverage.totalDraftFields,
            requiredManifestDraftWorkItems = plan.requiredManifestDraftWorkItems.size,
            verificationSteps = plan.verificationPlan.size,
            targetDurationMillis = plan.manifestFieldPlan.renderSettingsPlan.totalDurationMillis,
            findingCount = plan.findingCount + review.findingCount,
            draftOnly = true,
            draftManifestGenerated = draftManifestGenerated,
            productionManifestGenerated = false,
            productionReady = false,
            adapterImplementationExecuted = false,
            rendererReady = false,
            mp4ReadinessClaimed = false,
        )

    private fun draftPanelEntries(plan: LocalRendererManifestDraftPlan): List<LocalRendererDraftPanelManifestEntry> =
        plan.manifestFieldPlan.panelManifestPlans.map { panel ->
            LocalRendererDraftPanelManifestEntry(
                order = panel.order,
                panelId = panel.panelId,
                sourceSceneId = panel.sourceSceneId,
                sourceBeatId = panel.sourceBeatId,
                startMillis = panel.startMillis,
                endMillis = panel.endMillis,
                durationMillis = panel.durationMillis,
                draftPanelTimelineFields = draftFields(
                    panelId = panel.panelId,
                    sourceList = "plannedPanelTimelineFields",
                    fields = panel.plannedPanelTimelineFields,
                ),
                draftVoiceTimelineFields = draftFields(
                    panelId = panel.panelId,
                    sourceList = "plannedVoiceTimelineFields",
                    fields = panel.plannedVoiceTimelineFields,
                ),
                draftSubtitleTimelineFields = draftFields(
                    panelId = panel.panelId,
                    sourceList = "plannedSubtitleTimelineFields",
                    fields = panel.plannedSubtitleTimelineFields,
                ),
                draftOnly = true,
                draftManifestGenerated = true,
                productionManifestGenerated = false,
                productionReady = false,
                rendererReady = false,
                note = "Draft-only local JSON panel entry; not a production ComicVideoRenderHandoffManifest entry.",
            )
        }

    private fun draftFields(
        panelId: String,
        sourceList: String,
        fields: List<LocalRendererManifestFieldMappingPlan>,
    ): List<LocalRendererManifestDraftJsonField> =
        fields.map { field ->
            LocalRendererManifestDraftJsonField(
                targetSection = field.targetSection,
                targetField = field.targetField,
                sourcePlanPath = field.sourceDraftPath.ifBlank {
                    "$.manifestFieldPlan.panelManifestPlans[panelId=$panelId].$sourceList[targetField=${field.targetField}]"
                },
                sourcePlanReviewPath = "$.checks[checkId=manifest-remains-plan-only]",
                draftValueHint = field.plannedValueHint,
                implementationNote = field.implementationNote,
                required = field.required,
                draftOnly = true,
                productionReady = false,
            )
        }

    private fun fieldCoverageSummary(
        plan: LocalRendererManifestDraftPlan,
    ): LocalRendererManifestDraftJsonFieldCoverageSummary {
        val panelTimelineFields = plan.manifestFieldPlan.panelManifestPlans.sumOf {
            it.plannedPanelTimelineFields.size
        }
        val voiceTimelineFields = plan.manifestFieldPlan.panelManifestPlans.sumOf {
            it.plannedVoiceTimelineFields.size
        }
        val subtitleTimelineFields = plan.manifestFieldPlan.panelManifestPlans.sumOf {
            it.plannedSubtitleTimelineFields.size
        }
        val missingRequired = manifestFieldMappings(plan).count { it.required && it.plannedValueHint.isBlank() }
        return LocalRendererManifestDraftJsonFieldCoverageSummary(
            panelEntries = plan.manifestFieldPlan.panelManifestPlans.size,
            panelTimelineFields = panelTimelineFields,
            voiceTimelineFields = voiceTimelineFields,
            subtitleTimelineFields = subtitleTimelineFields,
            totalDraftFields = panelTimelineFields + voiceTimelineFields + subtitleTimelineFields,
            missingRequiredDraftFields = missingRequired,
            draftOnly = true,
            productionManifestGenerated = false,
            productionReady = false,
        )
    }

    private fun boundarySummary(
        plan: LocalRendererManifestDraftPlan,
        review: LocalRendererManifestDraftPlanReviewReport,
    ): LocalRendererManifestDraftJsonDraftBoundarySummary =
        LocalRendererManifestDraftJsonDraftBoundarySummary(
            planLocalOnly = localBoundariesOk(plan.boundaries),
            reviewLocalOnly = localBoundariesOk(review.boundaries) &&
                !review.manifestDraftPlanReadinessSummary.realManifestGenerated,
            mediaGenerated = plan.boundaries.mediaGenerated || review.boundaries.mediaGenerated,
            rendererInvocation = firstUnsafeInvocation(
                plan.boundaries.rendererInvocation,
                review.boundaries.rendererInvocation,
            ),
            providerInvocation = firstUnsafeInvocation(
                plan.boundaries.providerInvocation,
                review.boundaries.providerInvocation,
            ),
            ttsInvocation = firstUnsafeInvocation(
                plan.boundaries.ttsInvocation,
                review.boundaries.ttsInvocation,
            ),
            adapterImplementationExecuted = plan.boundaries.adapterImplementationExecuted ||
                review.boundaries.adapterImplementationExecuted,
            productionManifestGenerated = plan.boundaries.manifestGenerated ||
                plan.manifestFieldPlan.manifestGenerated ||
                plan.manifestFieldPlan.panelManifestPlans.any { it.manifestGenerated } ||
                review.boundaries.manifestGenerated ||
                review.manifestDraftPlanReadinessSummary.realManifestGenerated,
            productionReady = plan.boundaries.rendererReady ||
                review.boundaries.rendererReady ||
                review.manifestDraftPlanReadinessSummary.rendererReady ||
                plan.manifestFieldPlan.panelManifestPlans.any { it.rendererReady },
            finalMediaApproval = plan.boundaries.finalMediaApproval || review.boundaries.finalMediaApproval,
            providerMediaHandoff = plan.boundaries.providerMediaHandoff || review.boundaries.providerMediaHandoff,
            canonRegistryOrProductionLock = plan.boundaries.canonRegistryOrProductionLock ||
                review.boundaries.canonRegistryOrProductionLock,
            rendererReady = plan.boundaries.rendererReady ||
                review.boundaries.rendererReady ||
                review.manifestDraftPlanReadinessSummary.rendererReady,
            mp4ReadinessClaimed = plan.boundaries.mp4ReadinessClaimed ||
                review.boundaries.mp4ReadinessClaimed ||
                review.manifestDraftPlanReadinessSummary.mp4ReadinessClaimed,
        )

    private fun outputBoundaries(draftManifestGenerated: Boolean): LocalRendererManifestDraftJsonDraftBoundaries =
        LocalRendererManifestDraftJsonDraftBoundaries(
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

    private fun manifestFieldMappings(plan: LocalRendererManifestDraftPlan): List<LocalRendererManifestFieldMappingPlan> =
        plan.manifestFieldPlan.panelManifestPlans.flatMap {
            it.plannedPanelTimelineFields + it.plannedVoiceTimelineFields + it.plannedSubtitleTimelineFields
        }

    private fun firstUnsafeInvocation(vararg values: String): String =
        values.firstOrNull { it != "none" } ?: "none"

    private fun check(
        checkId: String,
        category: String,
        passed: Boolean,
        message: String,
        evidence: List<String>,
        findings: MutableList<LocalRendererManifestDraftJsonDraftFinding>,
        ruleId: String,
        path: String,
        suggestedFix: String,
    ): LocalRendererManifestDraftJsonDraftCheck {
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
        return LocalRendererManifestDraftJsonDraftCheck(
            checkId = checkId,
            status = if (passed) "PASS" else "FAIL",
            category = category,
            message = message,
            evidence = evidence,
        )
    }

    private fun List<LocalRendererManifestDraftJsonDraftFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): LocalRendererManifestDraftJsonDraftFinding =
        LocalRendererManifestDraftJsonDraftFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
}
