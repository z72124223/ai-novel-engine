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
        val hashBoundArtifact = if (artifact.contentSha256.isBlank()) {
            artifact.copy(contentSha256 = ProductionRuleRegistry.artifactHash(artifact))
        } else {
            artifact
        }
        repository.ingestArtifact(hashBoundArtifact)
        val ruleSet = ProductionRuleRegistry.activeRuleSet()
        val profiles = ProductionRuleRegistry.characterProfiles()
        val language = ProductionRuleRegistry.languageProfile()
        return DialogueReviewHandoff(
            artifact = hashBoundArtifact,
            ruleSet = ruleSet,
            characterVoiceProfiles = profiles.filter { it.profileId in hashBoundArtifact.characterVoiceProfileIds },
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
    fun finalize(request: DialogueFinalizeRequest): DialogueSelectionDecision {
        repository.recordReviewBatch(request.reviewBatch)
        return repository.select(request.selection)
    }
}
