package com.localnovel.engine.service

import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.repository.StoryRepository

class ContextCompiler(private val repository: StoryRepository) {
    fun compile(projectId: String, playerInput: String): ContextPacket {
        val project = repository.getProject(projectId)
            ?: error("Project not found: $projectId")
        val activeScene = repository.getActiveScene(projectId)
        val sceneContract = activeScene?.let { repository.getSceneContract(projectId, it.id) }
        val turnNumber = project.currentTurn + 1

        return ContextPacket(
            project = project,
            turnId = "${project.id}-turn-$turnNumber",
            turnNumber = turnNumber,
            playerInput = playerInput,
            activeScene = activeScene,
            sceneContract = sceneContract,
            characters = repository.listCharacters(projectId),
            characterMemories = repository.listCharacterMemories(projectId),
            characterStates = repository.listCharacterStates(projectId),
            worldRules = repository.listWorldRules(projectId),
            facts = repository.listFacts(projectId),
            revealedSecrets = repository.listRevealedSecrets(projectId),
            items = repository.listItems(projectId),
            techniques = repository.listTechniques(projectId),
            recentEvents = repository.listRecentEvents(projectId),
            openTasks = repository.listOpenTasks(projectId),
        )
    }
}
