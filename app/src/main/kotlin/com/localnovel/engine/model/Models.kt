package com.localnovel.engine.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

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
    val sourcePackId: String? = null,
    val sourcePackVersion: Int? = null,
    val visibility: String = "public",
    val runtimeRelevant: Boolean = true,
    val strength: Int = 1,
    val reason: String = "",
    val metadata: JsonObject = JsonObject(emptyMap()),
    val canonicalHash: String = "",
    val status: String = "active",
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
data class StoryThread(
    val id: String,
    val projectId: String,
    val threadType: String,
    val title: String,
    val directorGoal: String = "",
    val publicSummary: String,
    val priority: Int = 0,
    val visibility: String = "director_only",
    val parentThreadId: String? = null,
)

@Serializable
data class SessionThreadState(
    val sessionId: String,
    val threadId: String,
    val status: String = "OPEN",
    val progress: Int = 0,
    val urgency: Int = 0,
    val blockedReason: String = "",
    val lastAdvancedTurn: Int = 0,
    val evidence: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class SceneRequirementDefinition(
    val id: String,
    val sceneId: String,
    val requirementType: String,
    val description: String,
    val completionCondition: JsonObject = JsonObject(emptyMap()),
    val failureCondition: JsonObject = JsonObject(emptyMap()),
    val priority: Int = 0,
    val visibility: String = "director_only",
)

@Serializable
data class SceneRequirementView(
    val requirementId: String,
    val type: String,
    val description: String,
    val status: String = "OPEN",
    val progress: Int = 0,
    val remainingCondition: String = "",
    val directorOnly: Boolean = true,
)

@Serializable
data class SceneLifecycle(
    val sceneInstanceId: String = "",
    val sceneId: String = "",
    val visitNumber: Int = 1,
    val status: String = "ACTIVE",
    val startedTurnNumber: Int = 1,
    val readyToClose: Boolean = false,
    val closureReasons: List<String> = emptyList(),
)

@Serializable
data class ActiveStoryThreadView(
    val threadId: String,
    val type: String,
    val publicSummary: String,
    val status: String,
    val progress: Int,
    val urgency: Int,
    val directorGuidance: String = "",
)

@Serializable
data class AvailableTransition(
    val transitionId: String,
    val toSceneId: String,
    val targetLocation: String? = null,
    val playerIntentRequired: String = "",
    val prerequisitesSatisfied: Boolean = true,
    val unmetPrerequisites: List<String> = emptyList(),
    val travelMinutes: Int = 0,
    val forcedAllowed: Boolean = false,
    val visibleReason: String = "",
)

@Serializable
data class PendingWorldEventView(
    val eventId: String,
    val type: String,
    val publicEffect: String,
    val dueReason: String,
    val allowedNarrativeUses: List<String> = emptyList(),
    val forbiddenCharacterUses: List<String> = emptyList(),
)

@Serializable
data class WorldClock(
    val worldTime: String = "",
    val elapsedMinutes: Int = 0,
    val turnNumber: Int = 0,
)

@Serializable
data class ContinuityConstraints(
    val currentSceneInstanceId: String = "",
    val currentSceneLocationId: String? = null,
    val characterLocations: Map<String, String?> = emptyMap(),
    val itemLocations: Map<String, String?> = emptyMap(),
    val closedSceneSummaries: List<String> = emptyList(),
)

@Serializable
data class RelevantSetting(
    val entityType: String,
    val entityId: String,
    val name: String = "",
    val summary: String = "",
    val visibility: String = "public",
    val sourcePackId: String? = null,
    val sourcePackVersion: Int? = null,
    val relationPath: List<String> = emptyList(),
    val requiredRuntimeUse: Boolean = false,
    val metadata: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class SettingContextBudget(
    val maxBytes: Int = 8192,
    val actualBytes: Int = 0,
    val includedSettingCount: Int = 0,
    val omittedSettingCount: Int = 0,
    val truncationApplied: Boolean = false,
    val reasons: List<String> = emptyList(),
)

@Serializable
data class CanonRevisionSummary(
    val importedPacks: List<String> = emptyList(),
    val relevantRevisionIds: List<String> = emptyList(),
    val latestImportId: String? = null,
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
    val sceneLifecycle: SceneLifecycle = SceneLifecycle(),
    val sceneRequirements: List<SceneRequirementView> = emptyList(),
    val activeStoryThreads: List<ActiveStoryThreadView> = emptyList(),
    val availableTransitions: List<AvailableTransition> = emptyList(),
    val pendingWorldEvents: List<PendingWorldEventView> = emptyList(),
    val worldClock: WorldClock = WorldClock(),
    val continuityConstraints: ContinuityConstraints = ContinuityConstraints(),
    val relevantSettings: List<RelevantSetting> = emptyList(),
    val settingContextBudget: SettingContextBudget = SettingContextBudget(),
    val canonRevisionSummary: CanonRevisionSummary = CanonRevisionSummary(),
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
    val sceneRequirementUpdates: List<SceneRequirementUpdate> = emptyList(),
    val threadProgressUpdates: List<ThreadProgressUpdate> = emptyList(),
    val transitionProposal: TransitionProposal? = null,
    val worldEventAcknowledgements: List<WorldEventAcknowledgement> = emptyList(),
    val timeAdvanceProposal: TimeAdvanceProposal? = null,
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
    val storyThreads: List<StoryThread> = emptyList(),
    val sceneRequirements: List<SceneRequirementDefinition> = emptyList(),
    val locationConnections: List<LocationConnection> = emptyList(),
    val sceneTransitions: List<SceneTransitionDefinition> = emptyList(),
    val scheduledWorldEvents: List<ScheduledWorldEventSeed> = emptyList(),
)

@Serializable
data class StorySession(
    val id: String,
    val projectId: String,
    val currentSceneId: String,
    val nextTurnNumber: Int,
    val stateVersion: Int,
    val status: String = "active",
    val currentSceneInstanceId: String? = null,
    val worldTime: String = "",
    val elapsedMinutes: Int = 0,
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

@Serializable
data class SceneRequirementUpdate(
    val requirementId: String,
    val requestedStatus: String,
    val progressDelta: Int = 0,
    val evidenceEventIds: List<String> = emptyList(),
    val evidenceFactIds: List<String> = emptyList(),
    val evidenceStateDeltaIds: List<String> = emptyList(),
    val reason: String,
)

@Serializable
data class ThreadProgressUpdate(
    val threadId: String,
    val progressDelta: Int = 0,
    val requestedStatus: String = "OPEN",
    val evidenceEventIds: List<String> = emptyList(),
    val evidenceFactIds: List<String> = emptyList(),
    val reason: String,
)

@Serializable
data class TransitionProposal(
    val fromSceneInstanceId: String,
    val transitionId: String,
    val toSceneId: String,
    val proposalType: String,
    val playerIntentEvidence: String = "",
    val worldEventEvidence: String? = null,
    val participatingCharacterIds: List<String> = emptyList(),
    val reason: String,
)

@Serializable
data class WorldEventAcknowledgement(
    val eventId: String,
    val narrativeUse: String,
    val perceivedByCharacterIds: List<String> = emptyList(),
    val resultingEventIds: List<String> = emptyList(),
    val reason: String,
)

@Serializable
data class TimeAdvanceProposal(
    val minutes: Int,
    val sourceType: String,
    val sourceId: String,
    val reason: String,
)

@Serializable
data class LocationConnection(
    val id: String,
    val projectId: String,
    val fromLocationId: String,
    val toLocationId: String,
    val connectionType: String = "walk",
    val bidirectional: Boolean = true,
    val travelMinutes: Int = 0,
    val prerequisites: JsonObject = JsonObject(emptyMap()),
    val restrictions: JsonObject = JsonObject(emptyMap()),
    val visibility: String = "public",
)

@Serializable
data class SceneTransitionDefinition(
    val id: String,
    val projectId: String,
    val fromSceneId: String,
    val toSceneId: String,
    val transitionType: String = "walk",
    val prerequisites: JsonObject = JsonObject(emptyMap()),
    val costs: JsonObject = JsonObject(emptyMap()),
    val requiredPlayerIntent: String = "",
    val forcedTransitionAllowed: Boolean = false,
    val visibility: String = "public",
)

@Serializable
data class ScheduledWorldEventSeed(
    val id: String,
    val projectId: String,
    val sessionId: String? = null,
    val eventType: String,
    val dueTurnNumber: Int,
    val dueWorldTime: String? = null,
    val triggerConditions: JsonObject = JsonObject(emptyMap()),
    val payload: JsonObject = JsonObject(emptyMap()),
    val visibility: String = "public",
    val status: String = "scheduled",
)

@Serializable
data class SettingPackSource(
    val author: String = "",
    val license: String = "",
    val notes: String = "",
)

@Serializable
data class SettingPackDependency(
    val packId: String,
    val minVersion: Int = 1,
)

@Serializable
data class InitialRuntimeTemplate(
    val initialLocationId: String = "",
    val initialGoal: String = "",
    val dominantEmotion: String = "",
    val emotionState: Map<String, Int> = emptyMap(),
    val availableActions: List<String> = emptyList(),
    val relationshipDefaults: Map<String, String> = emptyMap(),
    val initiativeLevel: Int = 0,
)

@Serializable
data class SettingPackEntity(
    val id: String,
    val entityVersion: Int = 1,
    val name: String = "",
    val title: String = "",
    val role: String = "",
    val description: String = "",
    val summary: String = "",
    val content: String = "",
    val statement: String = "",
    val type: String = "",
    val subtype: String = "",
    val priority: Int = 0,
    val visibility: String = "public",
    val status: String = "active",
    val tags: List<String> = emptyList(),
    val sourcePackId: String? = null,
    val sceneId: String? = null,
    val locationId: String? = null,
    val parentLocationId: String? = null,
    val ownerCharacterId: String? = null,
    val initialOwnerId: String? = null,
    val initialLocationId: String? = null,
    val factionId: String? = null,
    val parentThreadId: String? = null,
    val threadType: String = "",
    val directorGoal: String = "",
    val publicSummary: String = "",
    val knownBy: List<String> = emptyList(),
    val startingKnowledge: List<String> = emptyList(),
    val startingItems: List<String> = emptyList(),
    val startingTechniques: List<String> = emptyList(),
    val effects: List<String> = emptyList(),
    val costs: List<String> = emptyList(),
    val limitations: List<String> = emptyList(),
    val prerequisites: List<String> = emptyList(),
    val forbiddenUses: List<String> = emptyList(),
    val allowedSecretIds: List<String> = emptyList(),
    val forbiddenReveals: List<String> = emptyList(),
    val requiredBeats: List<String> = emptyList(),
    val unique: Boolean = false,
    val revealed: Boolean = false,
    val revealCondition: String = "",
    val initialRuntimeTemplate: InitialRuntimeTemplate? = null,
    val metadata: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class SettingPackRelation(
    val id: String,
    val fromType: String,
    val fromId: String,
    val toType: String,
    val toId: String,
    val relation: String,
    val visibility: String = "public",
    val runtimeRelevant: Boolean = true,
    val strength: Int = 1,
    val reason: String = "",
    val metadata: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class SettingPackAssertion(
    val id: String,
    val statement: String,
    val severity: String = "soft",
    val entityRefs: List<String> = emptyList(),
    val metadata: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class SettingPackEntities(
    val projects: List<SettingPackEntity> = emptyList(),
    val characters: List<SettingPackEntity> = emptyList(),
    val worldRules: List<SettingPackEntity> = emptyList(),
    val factions: List<SettingPackEntity> = emptyList(),
    val locations: List<SettingPackEntity> = emptyList(),
    val items: List<SettingPackEntity> = emptyList(),
    val techniques: List<SettingPackEntity> = emptyList(),
    val facts: List<SettingPackEntity> = emptyList(),
    val secrets: List<SettingPackEntity> = emptyList(),
    val scenes: List<SettingPackEntity> = emptyList(),
    val sceneContracts: List<SettingPackEntity> = emptyList(),
    val storyThreads: List<SettingPackEntity> = emptyList(),
    val characterMemories: List<SettingPackEntity> = emptyList(),
    val characterKnowledge: List<SettingPackEntity> = emptyList(),
)

@Serializable
data class SettingPack(
    val schemaVersion: String = "1.0",
    val packId: String,
    val packVersion: Int = 1,
    val projectId: String,
    val name: String,
    val description: String = "",
    val mode: String = "BASE",
    val source: SettingPackSource = SettingPackSource(),
    val dependencies: List<SettingPackDependency> = emptyList(),
    val entities: SettingPackEntities = SettingPackEntities(),
    val relations: List<SettingPackRelation> = emptyList(),
    val assertions: List<SettingPackAssertion> = emptyList(),
    val metadata: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class SettingPackValidationIssue(
    val code: String,
    val message: String,
    val path: String = "",
    val severity: String = "error",
)

@Serializable
data class SettingPackValidationReport(
    val packId: String = "",
    val packVersion: Int = 0,
    val valid: Boolean = false,
    val issues: List<SettingPackValidationIssue> = emptyList(),
) {
    val isValid: Boolean get() = valid && issues.none { it.severity == "error" }
}

@Serializable
data class SettingPackDiffEntry(
    val action: String,
    val entityType: String,
    val entityId: String,
    val previousVersion: Int? = null,
    val newVersion: Int? = null,
    val fieldChanges: Map<String, String> = emptyMap(),
    val sensitive: Boolean = false,
)

@Serializable
data class SettingPackDiff(
    val packId: String,
    val packVersion: Int,
    val entries: List<SettingPackDiffEntry> = emptyList(),
    val canonicalHash: String = "",
)

@Serializable
data class ImpactPath(
    val rootType: String,
    val rootId: String,
    val impactedType: String,
    val impactedId: String,
    val relationPath: List<String> = emptyList(),
    val severity: String = "LOW",
    val reason: String = "",
)

@Serializable
data class SettingPackImpactReport(
    val packId: String,
    val packVersion: Int,
    val changedRoots: List<String> = emptyList(),
    val impactedSessions: List<String> = emptyList(),
    val impactedCharacters: List<String> = emptyList(),
    val impactedScenes: List<String> = emptyList(),
    val impactedThreads: List<String> = emptyList(),
    val paths: List<ImpactPath> = emptyList(),
    val blockers: List<String> = emptyList(),
)

@Serializable
data class SettingPackPreview(
    val pack: SettingPack,
    val validation: SettingPackValidationReport,
    val diff: SettingPackDiff,
    val impact: SettingPackImpactReport,
    val canonicalHash: String,
)

@Serializable
data class SettingPackApplyResult(
    val applied: Boolean,
    val importId: String,
    val packId: String,
    val packVersion: Int,
    val canonicalHash: String,
    val diff: SettingPackDiff,
    val impact: SettingPackImpactReport,
)

@Serializable
data class ContextSectionByteReport(
    val section: String,
    val utf8Bytes: Int,
    val itemCount: Int = 0,
)

@Serializable
data class ContextAuditReport(
    val valid: Boolean,
    val file: String = "",
    val maxBytes: Int = 24576,
    val actualBytes: Int = 0,
    val canonicalBytes: Int = 0,
    val declaredActualBytes: Int = 0,
    val contextHash: String = "",
    val recomputedContextHash: String = "",
    val hashValid: Boolean = false,
    val mandatoryFieldsPresent: Boolean = false,
    val violations: List<String> = emptyList(),
    val sections: List<ContextSectionByteReport> = emptyList(),
)

@Serializable
data class SessionEvidenceExport(
    val session: StorySession,
    val activeSceneInstanceCount: Int,
    val characterStates: List<CharacterState> = emptyList(),
    val turnLogs: List<Map<String, String>> = emptyList(),
    val stateDeltas: List<Map<String, String>> = emptyList(),
    val facts: List<Fact> = emptyList(),
    val revealedSecrets: List<Secret> = emptyList(),
    val pendingContextCount: Int = 0,
)

@Serializable
data class PilotPreflightCheck(
    val name: String,
    val passed: Boolean,
    val details: String = "",
)

@Serializable
data class PilotPreflightReport(
    val valid: Boolean,
    val projectId: String,
    val sessionId: String,
    val checks: List<PilotPreflightCheck>,
    val contextAudit: ContextAuditReport? = null,
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
