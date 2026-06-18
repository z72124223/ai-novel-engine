package com.localnovel.engine.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Project(
    val id: String,
    val name: String,
    val summary: String,
    val activeSceneId: String? = null,
    val currentTurn: Int = 0,
)

@Serializable
data class CharacterProfile(
    val id: String,
    val projectId: String,
    val name: String,
    val role: String,
    val description: String,
    val isPlayer: Boolean = false,
    val profile: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class CharacterState(
    val characterId: String,
    val projectId: String,
    val locationId: String? = null,
    val mood: String,
    val health: String,
    val inventory: List<String> = emptyList(),
    val flags: Map<String, String> = emptyMap(),
)

@Serializable
data class CharacterMemory(
    val id: String,
    val projectId: String,
    val characterId: String,
    val ownerCharacterId: String = characterId,
    val memoryType: String = "EPISODIC",
    val summary: String,
    val details: JsonObject = JsonObject(emptyMap()),
    val sourceType: String = "seed",
    val sourceId: String? = null,
    val subjectType: String? = null,
    val subjectId: String? = null,
    val tags: List<String> = emptyList(),
    val triggerTags: List<String> = emptyList(),
    val emotionalSignature: JsonObject = JsonObject(emptyMap()),
    val salience: Int = 1,
    val confidence: Int = 100,
    val reliability: Int = 100,
    val visibility: String = "private",
    val createdTurn: Int = 0,
    val lastRecalledTurnNumber: Int? = null,
    val recallCount: Int = 0,
    val decayRate: Double = 0.0,
    val status: String = "active",
    val isCanon: Boolean = false,
    val supersedesMemoryId: String? = null,
)

@Serializable
data class WorldRule(
    val id: String,
    val projectId: String,
    val title: String,
    val content: String,
    val priority: Int = 0,
)

@Serializable
data class Faction(
    val id: String,
    val projectId: String,
    val name: String,
    val description: String,
    val stance: String = "neutral",
)

@Serializable
data class Location(
    val id: String,
    val projectId: String,
    val name: String,
    val description: String,
    val parentLocationId: String? = null,
)

@Serializable
data class Item(
    val id: String,
    val projectId: String,
    val name: String,
    val description: String,
    val ownerCharacterId: String? = null,
    val locationId: String? = null,
    val tags: List<String> = emptyList(),
)

@Serializable
data class Technique(
    val id: String,
    val projectId: String,
    val name: String,
    val description: String,
    val ownerCharacterId: String? = null,
    val tags: List<String> = emptyList(),
)

@Serializable
data class Scene(
    val id: String,
    val projectId: String,
    val title: String,
    val summary: String,
    val locationId: String? = null,
    val status: String = "active",
    val progress: JsonObject = JsonObject(emptyMap()),
    val completedRequirements: List<String> = emptyList(),
)

@Serializable
data class SceneContract(
    val id: String,
    val projectId: String,
    val sceneId: String,
    val allowedSecretIds: List<String> = emptyList(),
    val requiredBeats: List<String> = emptyList(),
    val forbiddenReveals: List<String> = emptyList(),
)

@Serializable
data class Fact(
    val id: String,
    val projectId: String,
    val content: String,
    val visibility: String = "public",
    val source: String = "system",
    val createdTurn: Int = 0,
)

@Serializable
data class Secret(
    val id: String,
    val projectId: String,
    val content: String,
    val revealed: Boolean = false,
    val revealCondition: String = "",
    val createdTurn: Int = 0,
    val revealedTurn: Int? = null,
)

@Serializable
data class Event(
    val id: String,
    val projectId: String,
    val turnId: String,
    val summary: String,
    val payload: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class StateDelta(
    val summary: String = "",
    val operations: List<DeltaOperation> = emptyList(),
)

@Serializable
data class DeltaOperation(
    val type: DeltaOperationType,
    val targetType: String? = null,
    val targetId: String? = null,
    val payload: JsonObject = JsonObject(emptyMap()),
)

@Serializable
enum class DeltaOperationType {
    @SerialName("update_character_state")
    UPDATE_CHARACTER_STATE,

    @SerialName("add_fact")
    ADD_FACT,

    @SerialName("reveal_secret")
    REVEAL_SECRET,

    @SerialName("add_event")
    ADD_EVENT,

    @SerialName("upsert_task")
    UPSERT_TASK,

    @SerialName("noop")
    NOOP,
}

@Serializable
data class DependencyEdge(
    val id: String,
    val projectId: String,
    val fromType: String,
    val fromId: String,
    val toType: String,
    val toId: String,
    val relation: String,
)

@Serializable
data class Task(
    val id: String,
    val projectId: String,
    val title: String,
    val status: String = "open",
    val payload: JsonObject = JsonObject(emptyMap()),
    val createdTurn: Int = 0,
    val updatedTurn: Int = 0,
)

@Serializable
data class CharacterKnowledgeClaim(
    val id: String,
    val projectId: String,
    val characterId: String,
    val propositionKey: String,
    val statement: String,
    val epistemicState: String = "UNKNOWN",
    val confidence: Int = 0,
    val sourceFactId: String? = null,
    val sourceMemoryId: String? = null,
    val acquiredTurnNumber: Int = 0,
    val updatedTurnNumber: Int = 0,
    val visibility: String = "private",
    val status: String = "active",
)

@Serializable
data class RetrievedMemory(
    val memoryId: String,
    val ownerCharacterId: String,
    val type: String,
    val summary: String,
    val emotionalSignature: JsonObject = JsonObject(emptyMap()),
    val confidence: Int,
    val salience: Int,
    val retrievalScore: Int,
    val retrievalReasons: List<String> = emptyList(),
    val allowedUses: List<String> = listOf("character_action", "emotion_trigger", "dialogue_subtext"),
)

@Serializable
data class ContextKnowledgeClaim(
    val claimId: String,
    val characterId: String,
    val propositionKey: String,
    val statement: String,
    val epistemicState: String,
    val confidence: Int,
    val sourceType: String,
    val sourceFactId: String? = null,
    val sourceMemoryId: String? = null,
)

@Serializable
data class ContextBudget(
    val maxBytes: Int = 24576,
    val actualBytes: Int = 0,
    val includedMemoryCount: Int = 0,
    val omittedMemoryCount: Int = 0,
    val includedKnowledgeCount: Int = 0,
    val omittedKnowledgeCount: Int = 0,
    val truncationApplied: Boolean = false,
    val truncationReasons: List<String> = emptyList(),
)

@Serializable
data class ContextPacket(
    val protocolVersion: String = "2",
    val project: Project,
    val turnId: String,
    val turnNumber: Int,
    val playerInput: String,
    val activeScene: Scene?,
    val sceneContract: SceneContract?,
    val characters: List<CharacterProfile> = emptyList(),
    val characterMemories: List<CharacterMemory> = emptyList(),
    val characterStates: List<CharacterState> = emptyList(),
    val worldRules: List<WorldRule> = emptyList(),
    val facts: List<Fact> = emptyList(),
    val revealedSecrets: List<Secret> = emptyList(),
    val items: List<Item> = emptyList(),
    val techniques: List<Technique> = emptyList(),
    val recentEvents: List<Event> = emptyList(),
    val openTasks: List<Task> = emptyList(),
    val contextId: String = "",
    val sessionId: String = "",
    val baseStateVersion: Int = 0,
    val contextHash: String = "",
    val generatedAt: String = "",
    val recentTurnSummaries: List<TurnSummary> = emptyList(),
    val sceneProgress: JsonObject = JsonObject(emptyMap()),
    val directorPressure: DirectorPressure? = null,
    val outputRequirements: OutputRequirements = OutputRequirements(),
    val retrievedMemories: List<RetrievedMemory> = emptyList(),
    val knowledgeClaims: List<ContextKnowledgeClaim> = emptyList(),
    val contextBudget: ContextBudget = ContextBudget(),
)

@Serializable
data class AiTurnResult(
    val protocolVersion: String = "2",
    val projectId: String,
    val turnId: String,
    val narrative: String,
    val stateDelta: StateDelta = StateDelta(),
    val resultId: String = "",
    val contextId: String = "",
    val sessionId: String = "",
    val turnNumber: Int = 0,
    val baseStateVersion: Int = 0,
    val contextHash: String = "",
    val turnSummary: String = "",
    val characterActions: List<CharacterAction> = emptyList(),
    val stateDeltas: List<DeltaOperation> = emptyList(),
    val revealedFacts: List<RevealedFact> = emptyList(),
    val memoryEffects: List<MemoryEffect> = emptyList(),
    val sceneProgressUpdates: List<SceneProgressUpdate> = emptyList(),
    val newTasks: List<Task> = emptyList(),
    val memoryCandidates: List<MemoryCandidate> = emptyList(),
    val knowledgeUpdates: List<KnowledgeUpdate> = emptyList(),
    val validationNotes: List<String> = emptyList(),
    val notes: String = "",
)

@Serializable
data class DemoProjectSeed(
    val project: Project,
    val characters: List<CharacterProfile> = emptyList(),
    val characterStates: List<CharacterState> = emptyList(),
    val characterMemories: List<CharacterMemory> = emptyList(),
    val worldRules: List<WorldRule> = emptyList(),
    val factions: List<Faction> = emptyList(),
    val locations: List<Location> = emptyList(),
    val items: List<Item> = emptyList(),
    val techniques: List<Technique> = emptyList(),
    val scenes: List<Scene> = emptyList(),
    val sceneContracts: List<SceneContract> = emptyList(),
    val facts: List<Fact> = emptyList(),
    val secrets: List<Secret> = emptyList(),
    val events: List<Event> = emptyList(),
    val dependencyEdges: List<DependencyEdge> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val knowledgeClaims: List<CharacterKnowledgeClaim> = emptyList(),
)

@Serializable
data class StorySession(
    val id: String,
    val projectId: String,
    val currentSceneId: String,
    val nextTurnNumber: Int,
    val stateVersion: Int,
    val status: String = "active",
)

@Serializable
data class TurnSummary(
    val turnNumber: Int,
    val resultId: String = "",
    val summary: String,
)

@Serializable
data class DirectorPressure(
    val enabled: Boolean = false,
    val allowedPressureTypes: List<String> = emptyList(),
    val mustProduceMeaningfulStateChange: Boolean = false,
)

@Serializable
data class OutputRequirements(
    val mustIncludeTurnSummary: Boolean = true,
    val mustIncludeCharacterActions: Boolean = true,
    val mustProduceMeaningfulStateChange: Boolean = false,
)

@Serializable
data class CharacterAction(
    val characterId: String,
    val intent: String,
    val visibleAction: String,
    val hiddenMotive: String,
    val triggeringConditions: List<String> = emptyList(),
    val usedMemoryIds: List<String> = emptyList(),
    val usedFactIds: List<String> = emptyList(),
    val usedKnowledgeClaimIds: List<String> = emptyList(),
    val usedItemIds: List<String> = emptyList(),
    val usedTechniqueIds: List<String> = emptyList(),
    val characterFitScore: Int,
    val storyProgressScore: Int,
)

@Serializable
data class RevealedFact(
    val factId: String,
    val content: String,
)

@Serializable
data class MemoryEffect(
    val characterId: String,
    val memoryId: String,
    val effect: String,
)

@Serializable
data class SceneProgressUpdate(
    val requirement: String,
    val status: String,
    val evidence: String,
)

@Serializable
data class MemoryCandidate(
    val candidateId: String,
    val ownerCharacterId: String,
    val memoryType: String,
    val summary: String,
    val sourceEventIds: List<String> = emptyList(),
    val sourceFactIds: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val emotionalSignature: JsonObject = JsonObject(emptyMap()),
    val salience: Int = 50,
    val confidence: Int = 50,
    val reason: String,
    val lifecycleAction: String = "create",
    val supersedesMemoryId: String? = null,
)

@Serializable
data class KnowledgeUpdate(
    val characterId: String,
    val propositionKey: String,
    val requestedState: String,
    val confidenceDelta: Int = 0,
    val sourceEventIds: List<String> = emptyList(),
    val sourceFactIds: List<String> = emptyList(),
    val sourceMemoryIds: List<String> = emptyList(),
    val reason: String,
)

data class ValidationError(
    val code: String,
    val message: String,
)

data class ValidationReport(
    val errors: List<ValidationError>,
) {
    val isValid: Boolean = errors.isEmpty()
}

data class ApplyResult(
    val turnLogId: String,
    val narrative: String,
    val hasStateChange: Boolean,
)

fun AiTurnResult.allDeltaOperations(): List<DeltaOperation> =
    stateDeltas.ifEmpty { stateDelta.operations }
