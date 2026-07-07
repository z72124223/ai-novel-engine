@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalStoryVideoChainReport(
    val status: String,
    val chainId: String = "",
    val sourceStoryInputPackFile: String = "",
    val outputDir: String = "",
    val sourceStoryInputPackId: String = "",
    val stageResults: LocalStoryVideoChainStageResults = LocalStoryVideoChainStageResults(),
    val generatedFiles: LocalStoryVideoChainGeneratedFiles = LocalStoryVideoChainGeneratedFiles(),
    val counts: LocalStoryVideoChainCounts = LocalStoryVideoChainCounts(),
    val boundaries: LocalStoryVideoChainBoundaries = LocalStoryVideoChainBoundaries(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val downstreamBoundary: String = "local_story_video_chain_verified_only_not_media_generation",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findings: List<LocalStoryVideoChainFinding> = emptyList(),
)

@Serializable
data class LocalStoryVideoChainStageResults(
    val storyValidation: LocalStoryVideoChainStageResult = LocalStoryVideoChainStageResult(stageName = "storyValidation"),
    val assetPromptCards: LocalStoryVideoChainStageResult = LocalStoryVideoChainStageResult(stageName = "assetPromptCards"),
    val localRenderPackage: LocalStoryVideoChainStageResult = LocalStoryVideoChainStageResult(stageName = "localRenderPackage"),
)

@Serializable
data class LocalStoryVideoChainStageResult(
    val stageName: String = "",
    val status: String = "SKIPPED",
    val outputFile: String = "",
    val summary: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
)

@Serializable
data class LocalStoryVideoChainGeneratedFiles(
    val storyValidationReportFile: String = "",
    val assetPromptCardsFile: String = "",
    val localRenderPackageFile: String = "",
    val chainReportFile: String = "",
)

@Serializable
data class LocalStoryVideoChainCounts(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val characters: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val scenes: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val beats: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val visualPromptCards: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val audioCueCards: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val panelPlans: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val qaChecks: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val qaPass: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val qaFail: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val targetDurationMillis: Int = 0,
)

@Serializable
data class LocalStoryVideoChainBoundaries(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val fileProtocolJsonOnly: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val noExternalPaidProviderTools: Boolean = false,
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
    val finalMediaApproval: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val release: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val canonRegistryOrProductionLock: Boolean = false,
)

@Serializable
data class LocalStoryVideoChainFinding(
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
