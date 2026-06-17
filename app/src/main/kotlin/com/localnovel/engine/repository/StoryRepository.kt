package com.localnovel.engine.repository

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterProfile
import com.localnovel.engine.model.CharacterMemory
import com.localnovel.engine.model.CharacterState
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DemoProjectSeed
import com.localnovel.engine.model.Event
import com.localnovel.engine.model.Fact
import com.localnovel.engine.model.Item
import com.localnovel.engine.model.Project
import com.localnovel.engine.model.Scene
import com.localnovel.engine.model.SceneContract
import com.localnovel.engine.model.Secret
import com.localnovel.engine.model.Task
import com.localnovel.engine.model.Technique
import com.localnovel.engine.model.WorldRule

interface StoryRepository {
    fun initialize()
    fun seedDemo(seed: DemoProjectSeed)

    fun getProject(projectId: String): Project?
    fun getActiveScene(projectId: String): Scene?
    fun getSceneContract(projectId: String, sceneId: String): SceneContract?
    fun getCharacterState(projectId: String, characterId: String): CharacterState?
    fun getTask(projectId: String, taskId: String): Task?

    fun listCharacters(projectId: String): List<CharacterProfile>
    fun listCharacterMemories(projectId: String): List<CharacterMemory>
    fun listCharacterStates(projectId: String): List<CharacterState>
    fun listWorldRules(projectId: String): List<WorldRule>
    fun listFacts(projectId: String): List<Fact>
    fun listSecrets(projectId: String): List<Secret>
    fun listRevealedSecrets(projectId: String): List<Secret>
    fun listItems(projectId: String): List<Item>
    fun listTechniques(projectId: String): List<Technique>
    fun listRecentEvents(projectId: String, limit: Int = 10): List<Event>
    fun listOpenTasks(projectId: String): List<Task>
    fun listTables(): Set<String>

    fun targetExists(projectId: String, targetType: String, targetId: String): Boolean
    fun lastTurnStateChangeFlags(projectId: String, limit: Int): List<Boolean>

    fun saveTurnLog(context: ContextPacket, result: AiTurnResult, hasStateChange: Boolean): String
    fun recordStateDelta(projectId: String, turnId: String, operationType: String, targetType: String?, targetId: String?, payloadJson: String)
    fun updateCharacterState(state: CharacterState)
    fun addFact(fact: Fact)
    fun revealSecret(projectId: String, secretId: String, revealedTurn: Int)
    fun addEvent(event: Event)
    fun upsertTask(task: Task)
    fun updateProjectTurn(projectId: String, turnNumber: Int)
}
