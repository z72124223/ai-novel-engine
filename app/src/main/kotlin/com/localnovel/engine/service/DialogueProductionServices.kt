package com.localnovel.engine.service

import com.localnovel.engine.model.DialogueArtifact
import com.localnovel.engine.model.DialogueArtifactView
import com.localnovel.engine.model.DialogueFinalizeRequest
import com.localnovel.engine.model.DialogueReviewHandoff
import com.localnovel.engine.model.DialogueSelectionDecision
import com.localnovel.engine.model.DialogueSelectionRequest
import com.localnovel.engine.repository.DialogueProductionRepository

class DialogueReviewOrchestrator(private val repository: DialogueProductionRepository) {
    fun evaluate(artifact: DialogueArtifact): DialogueReviewHandoff {
        repository.bootstrap()
        val ruleSet = ProductionRuleRegistry.activeRuleSet()
        val profiles = ProductionRuleRegistry.characterProfiles()
        val language = ProductionRuleRegistry.languageProfile()
        val referencedProfiles = profiles.filter { it.profileId in artifact.characterVoiceProfileIds }
        val identityBoundArtifact = artifact.copy(
            ruleSetContentSha256 = artifact.ruleSetContentSha256.ifBlank { ruleSet.contentSha256 },
            characterVoiceProfileHashes = artifact.characterVoiceProfileHashes.ifEmpty {
                referencedProfiles.associate { it.profileId to it.profileSha256 }
            },
            languageProfileSha256 = artifact.languageProfileSha256.ifBlank { language.profileSha256 },
        )
        val hashBoundArtifact = if (identityBoundArtifact.contentSha256.isBlank()) {
            identityBoundArtifact.copy(contentSha256 = ProductionRuleRegistry.artifactHash(identityBoundArtifact))
        } else {
            identityBoundArtifact
        }
        repository.ingestArtifact(hashBoundArtifact)
        return DialogueReviewHandoff(
            artifact = hashBoundArtifact,
            ruleSet = ruleSet,
            characterVoiceProfiles = referencedProfiles,
            languageProfile = language,
            requiredReviewerRoles = ruleSet.requiredReviewerRoles,
            hardBlockerCodes = ruleSet.hardBlockerCodes,
            deterministicFindings = ProductionRuleRegistry.canonicalFixtureFindings(hashBoundArtifact),
            instruction = "三位獨立 reviewer 必須逐句覆蓋、引用 exact quote、綁定 Artifact SHA 與 Rule Set；任何 BLOCKER 禁止選定。",
        )
    }

    fun show(artifactId: String): DialogueArtifactView = repository.get(artifactId)
}

class DialogueSelectionGateService(private val repository: DialogueProductionRepository) {
    fun select(request: DialogueSelectionRequest): DialogueSelectionDecision = repository.select(request)
    fun show(artifactId: String): DialogueArtifactView = repository.get(artifactId)
}

class DialogueFinalizationService(private val repository: DialogueProductionRepository) {
    fun finalize(request: DialogueFinalizeRequest): DialogueSelectionDecision = repository.finalize(request)
}
