@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalStoryInputPack(
    val packId: String = "",
    val title: String = "",
    val locale: String = "",
    val worldPremise: String = "",
    val characters: List<LocalStoryInputCharacter> = emptyList(),
    val scenes: List<LocalStoryInputScene> = emptyList(),
    val beats: List<LocalStoryInputBeat> = emptyList(),
    val visualStyle: LocalStoryInputVisualStyle = LocalStoryInputVisualStyle(),
    val audioStyle: LocalStoryInputAudioStyle = LocalStoryInputAudioStyle(),
    val durationTargetMillis: Int = 0,
    val constraints: LocalStoryInputConstraints = LocalStoryInputConstraints(),
)

@Serializable
data class LocalStoryInputCharacter(
    val id: String = "",
    val name: String = "",
    val role: String = "",
    val visualCue: String = "",
    val voiceCue: String = "",
    val publicMotivation: String = "",
)

@Serializable
data class LocalStoryInputScene(
    val id: String = "",
    val title: String = "",
    val location: String = "",
    val mood: String = "",
    val visibleSituation: String = "",
)

@Serializable
data class LocalStoryInputBeat(
    val id: String = "",
    val sceneId: String = "",
    val characterIds: List<String> = emptyList(),
    val narrativeIntent: String = "",
    val dialogueIntent: String = "",
    val visualIntent: String = "",
    val audioIntent: String = "",
)

@Serializable
data class LocalStoryInputVisualStyle(
    val tone: String = "",
    val colorPalette: String = "",
    val cameraNotes: String = "",
    val subtitleStyle: String = "",
)

@Serializable
data class LocalStoryInputAudioStyle(
    val voiceDirection: String = "",
    val musicMood: String = "",
    val soundDesign: String = "",
)

@Serializable
data class LocalStoryInputConstraints(
    val localOnly: Boolean = false,
    val noExternalPaidProviderTools: Boolean = false,
    val fileProtocolJsonOnly: Boolean = true,
    val notes: List<String> = emptyList(),
)

@Serializable
data class LocalStoryInputPackValidationReport(
    val verdict: String,
    val validationId: String = "",
    val storyInputPackFile: String = "",
    val packId: String = "",
    val title: String = "",
    val locale: String = "",
    val downstreamBoundary: String = "local_story_input_pack_validation_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findings: List<LocalStoryInputPackValidationFinding> = emptyList(),
    val evidence: LocalStoryInputPackValidationEvidence = LocalStoryInputPackValidationEvidence(),
)

@Serializable
data class LocalStoryInputPackValidationFinding(
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

@Serializable
data class LocalStoryInputPackValidationEvidence(
    val characterCount: Int = 0,
    val sceneCount: Int = 0,
    val beatCount: Int = 0,
    val linkedBeatCount: Int = 0,
    val durationTargetMillis: Int = 0,
    val localOnlyDeclared: Boolean = false,
    val noExternalPaidProviderToolsDeclared: Boolean = false,
    val fileProtocolJsonOnlyDeclared: Boolean = false,
    val forbiddenBoundaryClaimCount: Int = 0,
    val crossReferenceMismatchCount: Int = 0,
)
