@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalRendererAdapterImplementationPlanDraft(
    val verdict: String,
    val status: String = "",
    val planId: String = "",
    val sourceDryRunReportFile: String = "",
    val sourceDryRunId: String = "",
    val sourceHandoffId: String = "",
    val sourceReviewId: String = "",
    val sourceStoryInputPackId: String = "",
    val sourceValidationChecks: List<LocalRendererAdapterImplementationPlanCheck> = emptyList(),
    val requiredAdapterWorkItems: List<LocalRendererAdapterWorkItem> = emptyList(),
    val targetManifestFieldMappingPlan: LocalRendererAdapterTargetManifestFieldMappingPlan =
        LocalRendererAdapterTargetManifestFieldMappingPlan(),
    val implementationSteps: List<LocalRendererAdapterImplementationStep> = emptyList(),
    val riskChecklist: List<LocalRendererAdapterRiskCheck> = emptyList(),
    val verificationPlan: List<LocalRendererAdapterVerificationStep> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val boundaries: LocalRendererAdapterImplementationPlanBoundaries =
        LocalRendererAdapterImplementationPlanBoundaries(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String = "local_renderer_adapter_implementation_plan_only_not_implementation",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<LocalRendererAdapterImplementationPlanFinding> = emptyList(),
)

@Serializable
data class LocalRendererAdapterImplementationPlanCheck(
    val checkId: String = "",
    val status: String = "FAIL",
    val category: String = "",
    val message: String = "",
    val evidence: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredForPlan: Boolean = true,
)

@Serializable
data class LocalRendererAdapterWorkItem(
    val workItemId: String = "",
    val status: String = "PLANNED",
    val title: String = "",
    val description: String = "",
    val sourceEvidence: List<String> = emptyList(),
    val targetOutcome: String = "",
    val mustNotDo: List<String> = emptyList(),
)

@Serializable
data class LocalRendererAdapterTargetManifestFieldMappingPlan(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val planningOnly: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestGenerated: Boolean = false,
    val targetManifestType: String = "ComicVideoRenderHandoffManifest",
    val targetManifestFieldMappingPlanNote: String = "",
    val panelTimelineFields: List<LocalRendererAdapterTargetFieldMappingNote> = emptyList(),
    val voiceTimelineFields: List<LocalRendererAdapterTargetFieldMappingNote> = emptyList(),
    val subtitleTimelineFields: List<LocalRendererAdapterTargetFieldMappingNote> = emptyList(),
    val renderSettingsFields: List<LocalRendererAdapterTargetFieldMappingNote> = emptyList(),
    val perPanelMappingNotes: List<LocalRendererAdapterImplementationPanelPlan> = emptyList(),
)

@Serializable
data class LocalRendererAdapterTargetFieldMappingNote(
    val targetSection: String = "",
    val targetField: String = "",
    val sourcePath: String = "",
    val implementationNote: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val required: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val generatedNow: Boolean = false,
)

@Serializable
data class LocalRendererAdapterImplementationPanelPlan(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val order: Int = 0,
    val panelId: String = "",
    val sourceSceneId: String = "",
    val sourceBeatId: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val durationMillis: Int = 0,
    val plannedPanelTimelineMapping: List<LocalRendererAdapterTargetFieldMappingNote> = emptyList(),
    val plannedVoiceTimelineMapping: List<LocalRendererAdapterTargetFieldMappingNote> = emptyList(),
    val plannedSubtitleTimelineMapping: List<LocalRendererAdapterTargetFieldMappingNote> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestGenerated: Boolean = false,
    val note: String = "",
)

@Serializable
data class LocalRendererAdapterImplementationStep(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val order: Int = 0,
    val stepId: String = "",
    val status: String = "PLANNED",
    val description: String = "",
    val acceptance: List<String> = emptyList(),
    val failStop: List<String> = emptyList(),
)

@Serializable
data class LocalRendererAdapterRiskCheck(
    val riskId: String = "",
    val status: String = "OPEN",
    val risk: String = "",
    val mitigation: String = "",
    val failStop: String = "",
)

@Serializable
data class LocalRendererAdapterVerificationStep(
    val verificationId: String = "",
    val commandOrCheck: String = "",
    val expectedResult: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiresMediaGeneration: Boolean = false,
)

@Serializable
data class LocalRendererAdapterImplementationPlanBoundaries(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val fileProtocolJsonOnly: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val noExternalPaidProviderTools: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val providerInvocation: String = "none",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val ttsInvocation: String = "none",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mediaGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val rendererInvocation: String = "none",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val directSqliteWrites: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val providerApiCalls: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val adapterImplementationExecuted: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val manifestGenerated: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val finalMediaApproval: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val providerMediaHandoff: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val release: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val canonRegistryOrProductionLock: Boolean = false,
)

@Serializable
data class LocalRendererAdapterImplementationPlanFinding(
    @SerialName("rule_id")
    val ruleId: String,
    val category: String,
    val severity: String,
    val path: String,
    val message: String,
    val evidence: List<String> = emptyList(),
    @SerialName("suggested_fix")
    val suggestedFix: String = "",
)
