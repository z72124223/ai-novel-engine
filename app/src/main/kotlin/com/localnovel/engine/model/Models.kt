@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.localnovel.engine.model

import kotlinx.serialization.EncodeDefault
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
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val maxBytes: Int = 24576,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val actualBytes: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val includedMemoryCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val omittedMemoryCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val includedKnowledgeCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val omittedKnowledgeCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val includedSettingCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val omittedSettingCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val truncationApplied: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val truncationReasonCodes: List<String> = emptyList(),
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
data class CapabilityContract(
    val entityType: String,
    val entityId: String,
    val name: String = "",
    val authorizedActorIds: List<String> = emptyList(),
    val intendedUse: String = "",
    val effects: List<String> = emptyList(),
    val prerequisites: List<String> = emptyList(),
    val requirements: List<String> = emptyList(),
    val costs: List<String> = emptyList(),
    val limitations: List<String> = emptyList(),
    val forbiddenUses: List<String> = emptyList(),
    val currentEligibility: String = "UNCERTAIN",
    val eligibilityReasons: List<String> = emptyList(),
    val allowedNarrativeClaims: List<String> = emptyList(),
    val forbiddenNarrativeClaims: List<String> = emptyList(),
    val sourceRevision: String = "",
)

@Serializable
data class CapabilityUseCheck(
    val actorId: String,
    val entityType: String,
    val entityId: String,
    val intendedUse: String,
    val eligibilityClaim: String,
    val satisfiedPrerequisites: List<String> = emptyList(),
    val unsatisfiedPrerequisites: List<String> = emptyList(),
    val appliedCosts: List<String> = emptyList(),
    val observedLimitations: List<String> = emptyList(),
    val resultScope: String = "",
    val evidenceStateDeltaIds: List<String> = emptyList(),
    val reason: String,
)

@Serializable
data class CapabilityCoverageReport(
    val referencedCapabilities: List<String> = emptyList(),
    val selectedContracts: List<CapabilityCoverageSelection> = emptyList(),
    val missingContracts: List<String> = emptyList(),
    val authorizationChecks: List<String> = emptyList(),
    val prerequisiteChecks: List<String> = emptyList(),
    val costCoverage: List<String> = emptyList(),
    val limitationCoverage: List<String> = emptyList(),
    val qualityStatus: String = "PASS",
)

@Serializable
data class CapabilityCoverageSelection(
    val entityId: String,
    val actorId: String = "",
    val selectedContextPath: String = "",
    val bytes: Int = 0,
    val sourceProvenance: String = "",
    val requiredFieldsPresent: List<String> = emptyList(),
    val currentEligibility: String = "UNCERTAIN",
    val omittedAlternatives: List<String> = emptyList(),
    val visibilityStatus: String = "public",
)

@Serializable
data class CapabilityProvenanceReport(
    val projectId: String,
    val sessionId: String = "",
    val entityType: String,
    val entityId: String,
    val canonicalEntitySource: String = "",
    val tableRow: JsonObject = JsonObject(emptyMap()),
    val entityRevisionId: String = "",
    val sourcePackId: String? = null,
    val sourcePackVersion: Int? = null,
    val sourceRevision: String = "",
    val dependencyRelations: List<String> = emptyList(),
    val owner: String? = null,
    val authorizedActorIds: List<String> = emptyList(),
    val effects: List<String> = emptyList(),
    val prerequisites: List<String> = emptyList(),
    val requirements: List<String> = emptyList(),
    val costs: List<String> = emptyList(),
    val limitations: List<String> = emptyList(),
    val forbiddenUses: List<String> = emptyList(),
    val visibility: String = "public",
    val currentSessionAccessibility: String = "UNKNOWN",
    val currentEligibility: String = "UNCERTAIN",
    val eligibilityReasons: List<String> = emptyList(),
    val status: String = "OK",
    val missingFields: List<String> = emptyList(),
)

@Serializable
data class SettingContextBudget(
    val maxBytes: Int = 8192,
    val actualBytes: Int = 0,
    val includedSettingCount: Int = 0,
    val omittedSettingCount: Int = 0,
    val truncationApplied: Boolean = false,
    val reasonCodes: List<String> = emptyList(),
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
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
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
    val branchId: String? = null,
    val checkpointSummary: String? = null,
    val baseStateVersion: Int = 0,
    val contextHash: String = "",
    val generatedAt: String = "",
    val recentTurnSummaries: List<TurnSummary> = emptyList(),
    val sceneProgress: JsonObject = JsonObject(emptyMap()),
    val directorPressure: DirectorPressure? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
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
    val activeCapabilityContracts: List<CapabilityContract> = emptyList(),
    val settingContextBudget: SettingContextBudget = SettingContextBudget(),
    val canonRevisionSummary: CanonRevisionSummary = CanonRevisionSummary(),
    val canonThinZones: List<com.localnovel.engine.model.CanonThinZoneView> = emptyList(),
    val provisionalEntities: List<com.localnovel.engine.model.ProvisionalEntityView> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val readerMode: Boolean = false,
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
    val capabilityUseChecks: List<CapabilityUseCheck> = emptyList(),
    val entityProposals: List<com.localnovel.engine.model.ProvisionalEntityProposal> = emptyList(),
    val promotionRecommendations: List<com.localnovel.engine.model.ProvisionalPromotionRecommendation> = emptyList(),
    val creativeGapNotes: List<com.localnovel.engine.model.CreativeGapNote> = emptyList(),
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
    val branchId: String? = null,
    val currentSceneInstanceId: String? = null,
    val worldTime: String = "",
    val elapsedMinutes: Int = 0,
    val readerMode: Boolean = false,
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
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mustIncludeTurnSummary: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val mustIncludeCharacterActions: Boolean = true,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
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
    val locationConnections: List<LocationConnection> = emptyList(),
    val sceneTransitions: List<SceneTransitionDefinition> = emptyList(),
    val scheduledWorldEvents: List<ScheduledWorldEventSeed> = emptyList(),
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
data class SettingPackScriptPacketDraftBuildReport(
    val verdict: String,
    val buildId: String = "",
    val settingPackFile: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketValidationFile: String = "",
    val packId: String = "",
    val packVersion: Int = 0,
    val projectId: String = "",
    val selectedSceneId: String = "",
    val selectedLocationId: String = "",
    val outputPacketId: String = "",
    val validationVerdict: String = "",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<SettingPackScriptPacketDraftBuildFinding> = emptyList(),
    val evidence: SettingPackScriptPacketDraftBuildEvidence = SettingPackScriptPacketDraftBuildEvidence(),
)

@Serializable
data class SettingPackScriptPacketDraftBuildFinding(
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
data class SettingPackScriptPacketDraftBuildEvidence(
    val publicSceneCount: Int = 0,
    val publicLocationCount: Int = 0,
    val publicCharacterCount: Int = 0,
    val publicItemCount: Int = 0,
    val publicFactCount: Int = 0,
    val publicWorldRuleCount: Int = 0,
    val selectedCharacterCount: Int = 0,
    val selectedItemCount: Int = 0,
    val sceneContractCount: Int = 0,
    val excludedSecretCount: Int = 0,
    val generatedSegmentCount: Int = 0,
    val generatedBeatCount: Int = 0,
    val generatedFrameCandidateCount: Int = 0,
    val validationFindingCount: Int = 0,
)

@Serializable
data class SettingImageFacingProofPreviewBuildReport(
    val verdict: String,
    val buildId: String = "",
    val buildStatus: String = "FAIL",
    val settingPackFile: String = "",
    val outputDir: String = "",
    val settingScriptPacketDraftBuildReportFile: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketValidationFile: String = "",
    val storyboardPlanningBriefFile: String = "",
    val storyboardPacketDraftFile: String = "",
    val storyboardPacketDraftValidationFile: String = "",
    val storyboardPacketReviewFile: String = "",
    val storyboardHumanReviewPreflightFile: String = "",
    val storyboardStoryMovementAuditFile: String = "",
    val reviewableStoryboardPacketFile: String = "",
    val imageFacingDeliveryProofFile: String = "",
    val packId: String = "",
    val projectId: String = "",
    val selectedSceneId: String = "",
    val scriptPacketDraftId: String = "",
    val storyboardPlanningBriefId: String = "",
    val storyboardPacketDraftId: String = "",
    val storyboardPacketReviewId: String = "",
    val storyboardHumanReviewPreflightId: String = "",
    val storyboardStoryMovementAuditId: String = "",
    val reviewableStoryboardPacketId: String = "",
    val imageFacingDeliveryProofId: String = "",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<SettingImageFacingProofPreviewBuildFinding> = emptyList(),
    val evidence: SettingImageFacingProofPreviewBuildEvidence = SettingImageFacingProofPreviewBuildEvidence(),
)

@Serializable
data class SettingImageFacingProofPreviewBuildFinding(
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
data class SettingImageFacingProofPreviewBuildEvidence(
    val settingScriptDraftBuildVerdict: String = "",
    val scriptPacketValidationVerdict: String = "",
    val storyboardPlanningBriefVerdict: String = "",
    val storyboardPacketDraftValidationVerdict: String = "",
    val storyboardPacketReviewVerdict: String = "",
    val storyboardHumanReviewPreflightVerdict: String = "",
    val storyboardStoryMovementAuditVerdict: String = "",
    val reviewableStoryboardPacketVerdict: String = "",
    val imageFacingDeliveryProofVerdict: String = "",
    val generatedScriptSegmentCount: Int = 0,
    val generatedStoryboardFrameCount: Int = 0,
    val generatedImageFrameCardCount: Int = 0,
    val excludedSecretCount: Int = 0,
    val findingCountAcrossStages: Int = 0,
)

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
    val forensics: ContextBudgetForensicsReport? = null,
    val semanticCoverage: SemanticCoverageReport = SemanticCoverageReport(),
    val capabilityCoverage: CapabilityCoverageReport = CapabilityCoverageReport(),
)

@Serializable
data class ContextBudgetItemReport(
    val id: String = "",
    val section: String,
    val reason: String = "",
    val utf8Bytes: Int = 0,
)

@Serializable
data class ContextJsonPathByteReport(
    val path: String,
    val utf8Bytes: Int,
    val mandatory: Boolean = false,
    val deduplicatable: Boolean = false,
)

@Serializable
data class ContextMetadataPathReport(
    val path: String,
    val utf8Bytes: Int,
    val valueType: String,
    val semanticOwner: String,
    val mandatory: Boolean = false,
    val duplicatedWith: List<String> = emptyList(),
    val canCompact: Boolean = false,
    val canMoveToAuditSidecar: Boolean = false,
    val inclusionReason: String = "",
)

@Serializable
data class SemanticRequirement(
    val requirementId: String,
    val semanticType: String,
    val source: String,
    val actorId: String = "",
    val entityType: String = "",
    val entityId: String = "",
    val referenceReason: String = "",
    val requestedUse: String = "",
    val requiredContractFields: List<String> = emptyList(),
    val targetCharacterIds: List<String> = emptyList(),
    val referencedEntityIds: List<String> = emptyList(),
    val referencedMemoryCandidates: List<String> = emptyList(),
    val referencedKnowledgeCandidates: List<String> = emptyList(),
    val referencedSettingCandidates: List<String> = emptyList(),
    val priority: Int = 0,
    val reason: String = "",
    val mustBeSatisfied: Boolean = false,
    val acceptableContextPaths: List<String> = emptyList(),
)

@Serializable
data class SemanticCoverageItem(
    val requirementId: String,
    val semanticType: String,
    val reason: String = "",
    val candidateIds: List<String> = emptyList(),
    val selectedId: String = "",
    val selectedContextPath: String = "",
    val bytes: Int = 0,
    val omittedAlternatives: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val visibilityCheck: String = "PASS",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val qualityStatus: String = "PASS",
)

@Serializable
data class SemanticCoverageReport(
    val requirements: List<SemanticRequirement> = emptyList(),
    val satisfiedCount: Int = 0,
    val unsatisfiedCount: Int = 0,
    val satisfiedBy: List<SemanticCoverageItem> = emptyList(),
    val missingCriticalRequirements: List<SemanticRequirement> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val qualityStatus: String = "PASS",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val nearHardLimit: Boolean = false,
    val remainingBytes: Int = 0,
    val softTargetBytes: Int = 22528,
    val maxBytes: Int = 24576,
    val tierBytes: Map<String, Int> = emptyMap(),
)

@Serializable
data class ContextDuplicateContentGroup(
    val hash: String,
    val normalizedBytes: Int,
    val paths: List<String>,
    val sample: String = "",
)

@Serializable
data class ContextDanglingReferenceCheck(
    val valid: Boolean,
    val missingReferences: List<String> = emptyList(),
)

@Serializable
data class ContextSafetyFieldPresence(
    val protocolIdentity: Boolean,
    val sceneContract: Boolean,
    val forbiddenReveals: Boolean,
    val worldRules: Boolean,
    val activeCharacters: Boolean,
    val characterStates: Boolean,
    val continuityConstraints: Boolean,
    val contextHash: Boolean,
)

@Serializable
data class ContextBudgetForensicsReport(
    val sessionId: String,
    val turnNumber: Int,
    val baseStateVersion: Int,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val maxContextBytes: Int = 24576,
    val candidateBytesBeforeTrimming: Int,
    val candidateBytesAfterTrimming: Int,
    val bytesOverLimit: Int,
    val mandatoryBytes: Int,
    val optionalBytesRemaining: Int,
    val perSectionBytesBefore: List<ContextSectionByteReport> = emptyList(),
    val perSectionBytesAfter: List<ContextSectionByteReport> = emptyList(),
    val mandatorySections: List<String> = emptyList(),
    val optionalSections: List<String> = emptyList(),
    val removedItems: List<ContextBudgetItemReport> = emptyList(),
    val retainedItems: List<ContextBudgetItemReport> = emptyList(),
    val topLargestJsonPaths: List<ContextJsonPathByteReport> = emptyList(),
    val metadataPathBreakdown: List<ContextMetadataPathReport> = emptyList(),
    val duplicateContentGroups: List<ContextDuplicateContentGroup> = emptyList(),
    val semanticCoverage: SemanticCoverageReport = SemanticCoverageReport(),
    val capabilityCoverage: CapabilityCoverageReport = CapabilityCoverageReport(),
    val danglingReferenceCheck: ContextDanglingReferenceCheck = ContextDanglingReferenceCheck(true),
    val safetyFieldPresence: ContextSafetyFieldPresence,
    val unsatisfiedReason: String = "",
    val canFit: Boolean = false,
    val contextHash: String = "",
    val actualBytesMatchesSerializedPayload: Boolean = false,
    val hashValid: Boolean = false,
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
data class TimelineBranch(
    val id: String,
    val projectId: String,
    val label: String = "",
    val status: String = "ACTIVE",
    val parentBranchId: String? = null,
    val sourceCheckpointId: String? = null,
    val activeSessionId: String? = null,
    val createdFromSessionId: String? = null,
    val createdFromTurnNumber: Int = 0,
    val createdFromStateVersion: Int = 0,
    val reason: String = "",
    val metadata: JsonObject = JsonObject(emptyMap()),
    val createdAt: String = "",
    val updatedAt: String = "",
)

@Serializable
data class CheckpointSnapshot(
    val snapshotVersion: String = "1",
    val projectId: String,
    val sessionId: String,
    val branchId: String? = null,
    val turnNumber: Int,
    val stateVersion: Int,
    val worldTime: String = "",
    val elapsedMinutes: Int = 0,
    val activeSceneId: String? = null,
    val currentSceneInstanceId: String? = null,
    val tables: Map<String, List<Map<String, String?>>> = emptyMap(),
)

@Serializable
data class SessionCheckpoint(
    val id: String,
    val projectId: String,
    val sessionId: String,
    val branchId: String? = null,
    val turnNumber: Int,
    val stateVersion: Int,
    val sourceTurnLogId: String? = null,
    val kind: String,
    val label: String = "",
    val reason: String = "",
    val snapshotHash: String,
    val snapshot: CheckpointSnapshot? = null,
    val createdAt: String = "",
)

@Serializable
data class ForkSessionResult(
    val branchId: String,
    val sessionId: String,
    val sourceCheckpointId: String,
    val stateVersion: Int,
    val nextTurnNumber: Int,
)

@Serializable
data class ReplayAuditIssue(
    val code: String,
    val message: String,
    val turnNumber: Int? = null,
)

@Serializable
data class ReplayAuditReport(
    val projectId: String,
    val sessionId: String,
    val branchId: String? = null,
    val status: String,
    val checkedTurnCount: Int,
    val issues: List<ReplayAuditIssue> = emptyList(),
)

@Serializable
data class TimelineExport(
    val projectId: String,
    val activeBranchId: String? = null,
    val branches: List<TimelineBranch> = emptyList(),
    val checkpoints: List<SessionCheckpoint> = emptyList(),
    val replayAudits: List<ReplayAuditReport> = emptyList(),
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
    val stateVersion: Int? = null,
    val nextTurnNumber: Int? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val pendingContextCount: Int = 0,
)

@Serializable
data class NarrativeQualityAuditReport(
    val verdict: String,
    val score: Int,
    val severity: String,
    val findings: List<NarrativeQualityFinding> = emptyList(),
    @SerialName("finding_summary_by_category")
    val findingSummaryByCategory: Map<String, Int> = emptyMap(),
    val evidence: NarrativeQualityAuditEvidence = NarrativeQualityAuditEvidence(),
)

@Serializable
data class NarrativeQualityFinding(
    @SerialName("rule_id")
    val ruleId: String,
    val category: String,
    val severity: String,
    val message: String,
    val evidence: List<String> = emptyList(),
    @SerialName("suggested_fix")
    val suggestedFix: String,
    @SerialName("affected_file_or_turn")
    val affectedFileOrTurn: String = "",
)

@Serializable
data class NarrativeQualityAuditEvidence(
    val contextFile: String = "",
    val resultFile: String = "",
    val evidenceFile: String = "",
    @SerialName("sample_label")
    val sampleLabel: String = "",
    val projectId: String = "",
    val turnId: String = "",
    val contextId: String = "",
    val resultId: String = "",
    val turnNumber: Int = 0,
    val narrativeLength: Int = 0,
    val characterActionCount: Int = 0,
    val stateDeltaCount: Int = 0,
    val retrievedMemoryCount: Int = 0,
    val knowledgeClaimCount: Int = 0,
    val activeThreadCount: Int = 0,
    val pendingWorldEventCount: Int = 0,
)

@Serializable
data class ChapterContinuityAnchorFile(
    val auditId: String = "",
    val previousTitle: String = "",
    val draftTitle: String = "",
    val mustAppearInPrevious: List<ChapterContinuityCheck> = emptyList(),
    val mustAppearInDraft: List<ChapterContinuityCheck> = emptyList(),
    val mustNotAppearInDraft: List<ChapterContinuityCheck> = emptyList(),
    val reviewerNotes: String = "",
)

@Serializable
data class ChapterContinuityCheck(
    val checkId: String = "",
    val text: String = "",
    val severity: String = "CRITICAL",
    val description: String = "",
    val suggestedFix: String = "",
)

@Serializable
data class ChapterContinuityAuditReport(
    val verdict: String,
    val previousFile: String = "",
    val draftFile: String = "",
    val anchorFile: String = "",
    val auditId: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ChapterContinuityFinding> = emptyList(),
    val evidence: ChapterContinuityAuditEvidence = ChapterContinuityAuditEvidence(),
)

@Serializable
data class ChapterContinuityFinding(
    @SerialName("rule_id")
    val ruleId: String,
    val category: String,
    val severity: String,
    val checkId: String = "",
    val message: String,
    val evidence: List<String> = emptyList(),
    @SerialName("suggested_fix")
    val suggestedFix: String = "",
)

@Serializable
data class ChapterContinuityAuditEvidence(
    val previousCharacters: Int = 0,
    val draftCharacters: Int = 0,
    val mustAppearInPreviousCount: Int = 0,
    val mustAppearInDraftCount: Int = 0,
    val mustNotAppearInDraftCount: Int = 0,
    val replacementCharacterCount: Int = 0,
)

@Serializable
data class ScriptPacketDraft(
    val packetId: String = "",
    val source: ScriptPacketSource = ScriptPacketSource(),
    val segments: List<ScriptPacketSegment> = emptyList(),
    val assetNeeds: List<ScriptPacketAssetNeed> = emptyList(),
    val storyboardNotes: ScriptPacketStoryboardNotes = ScriptPacketStoryboardNotes(),
    val secrecyKnowledgeNotes: ScriptPacketSecrecyKnowledgeNotes = ScriptPacketSecrecyKnowledgeNotes(),
    val reviewSummary: ScriptPacketReviewSummary = ScriptPacketReviewSummary(),
)

@Serializable
data class ScriptPacketSource(
    val inputType: String = "",
    val sourceId: String = "",
    val sourceTitle: String = "",
    val excerptSummary: String = "",
    val transformationStatus: String = "draft",
    val acceptedDraftPacketId: String = "",
    val acceptedDraftContentSha256: String = "",
    val acceptedDraftFile: String = "",
)

@Serializable
data class ScriptPacketSegment(
    val segmentId: String = "",
    val sceneReference: ScriptPacketSceneReference = ScriptPacketSceneReference(),
    val scenePurpose: String = "",
    val playerObjective: String = "",
    val locationZone: ScriptPacketLocationZone = ScriptPacketLocationZone(),
    val visualSetup: ScriptPacketVisualSetup = ScriptPacketVisualSetup(),
    val knowledgePass: ScriptPacketKnowledgePass = ScriptPacketKnowledgePass(),
    val beatList: List<ScriptPacketBeat> = emptyList(),
    val actionLines: List<ScriptPacketActionLine> = emptyList(),
    val dialogueLines: List<ScriptPacketDialogueLine> = emptyList(),
    val interactionChoices: List<ScriptPacketInteractionChoice> = emptyList(),
)

@Serializable
data class ScriptPacketSceneReference(
    val type: String = "",
    val sceneId: String = "",
    val candidateId: String = "",
    val notes: String = "",
)

@Serializable
data class ScriptPacketLocationZone(
    val zoneId: String = "",
    val publicName: String = "",
    val constraints: String = "",
)

@Serializable
data class ScriptPacketVisualSetup(
    val publicDescription: String = "",
    val cameraIntent: String = "",
    val mustShow: List<String> = emptyList(),
    val mustNotShow: List<String> = emptyList(),
)

@Serializable
data class ScriptPacketKnowledgePass(
    val publicFacts: List<String> = emptyList(),
    val characterKnowledgeClaims: List<String> = emptyList(),
    val authorOnlyExtracted: List<String> = emptyList(),
)

@Serializable
data class ScriptPacketBeat(
    val beatId: String = "",
    val visibleAction: String = "",
    val causalLink: String = "",
    val characterIds: List<String> = emptyList(),
    val requiredKnowledgeClaimIds: List<String> = emptyList(),
    val outcome: String = "",
)

@Serializable
data class ScriptPacketActionLine(
    val lineId: String = "",
    val actorId: String = "",
    val line: String = "",
    val blockingZone: String = "",
    val visibleCost: String = "",
)

@Serializable
data class ScriptPacketDialogueLine(
    val lineId: String = "",
    val speakerId: String = "",
    val line: String = "",
    val knowledgeBoundary: String = "",
    val subtext: String = "",
)

@Serializable
data class ScriptPacketInteractionChoice(
    val choiceId: String = "",
    val label: String = "",
    val allowedBecause: String = "",
    val likelyStateEffect: String = "",
)

@Serializable
data class ScriptPacketAssetNeed(
    val assetType: String = "",
    val existingAssetId: String = "",
    val candidateName: String = "",
    val candidateReason: String = "",
    val lockStatus: String = "candidate",
)

@Serializable
data class ScriptPacketStoryboardNotes(
    val cameraLanguage: String = "",
    val continuityLocks: List<String> = emptyList(),
    val frameCandidates: List<ScriptPacketFrameCandidate> = emptyList(),
)

@Serializable
data class ScriptPacketFrameCandidate(
    val frameId: String = "",
    val beatId: String = "",
    val framePurpose: String = "",
    val cameraPurpose: String = "",
    val visibleSubject: String = "",
    val doNotShow: List<String> = emptyList(),
)

@Serializable
data class ScriptPacketSecrecyKnowledgeNotes(
    val authorOnlyExcluded: Boolean = false,
    val reviewOnlyNotes: String = "",
)

@Serializable
data class ScriptPacketReviewSummary(
    val verdict: String = "unreviewed",
    val reviewerNotes: String = "",
)

@Serializable
data class ScriptPacketDraftValidationReport(
    val verdict: String,
    val packetFile: String = "",
    val packetId: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val segmentCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ScriptPacketDraftFinding> = emptyList(),
)

@Serializable
data class ScriptPacketDraftFinding(
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
data class ScriptPacketDraftSourceBindingReport(
    val verdict: String,
    val scriptPacketDraftFile: String = "",
    val scriptPacketPreflightFile: String = "",
    val acceptedDraftPacketFile: String = "",
    val scriptPacketId: String = "",
    val acceptedDraftPacketId: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ScriptPacketDraftSourceBindingFinding> = emptyList(),
    val evidence: ScriptPacketDraftSourceBindingEvidence = ScriptPacketDraftSourceBindingEvidence(),
)

@Serializable
data class ScriptPacketDraftSourceBindingFinding(
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
data class ScriptPacketDraftSourceBindingEvidence(
    val preflightVerdict: String = "",
    val acceptedDraftPacketVerdict: String = "",
    val bindingMatchedBy: List<String> = emptyList(),
    val protectedContinuityAnchorCount: Int = 0,
    val referencedContinuityAnchorCount: Int = 0,
    val boundaryClaimCount: Int = 0,
)

@Serializable
data class ScriptPacketDraftStructureBridgeReport(
    val verdict: String,
    val scriptPacketDraftFile: String = "",
    val scriptPacketPreflightFile: String = "",
    val acceptedDraftPacketFile: String = "",
    val scriptPacketId: String = "",
    val sourceBindingVerdict: String = "",
    val structureVerdict: String = "",
    val downstreamAllowed: Boolean = false,
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val sourceBindingFindingCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val structureFindingCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val sourceBindingFindings: List<ScriptPacketDraftSourceBindingFinding> = emptyList(),
    val structureFindings: List<ScriptPacketDraftFinding> = emptyList(),
    val findings: List<ScriptPacketDraftStructureBridgeFinding> = emptyList(),
    val evidence: ScriptPacketDraftStructureBridgeEvidence = ScriptPacketDraftStructureBridgeEvidence(),
)

@Serializable
data class ScriptPacketDraftStructureBridgeFinding(
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
data class ScriptPacketDraftStructureBridgeEvidence(
    val sourceBindingPass: Boolean = false,
    val structureAcceptable: Boolean = false,
    val structureWarningAllowed: Boolean = false,
    val downstreamAllowedReason: String = "",
    val blockedReason: String = "",
)

@Serializable
data class AcceptedScriptPacketHandoffPacket(
    val verdict: String,
    val packetId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val sourceReports: AcceptedScriptPacketSourceReports = AcceptedScriptPacketSourceReports(),
    val protectedContinuityAnchorsSummary: AcceptedScriptPacketContinuityAnchorsSummary =
        AcceptedScriptPacketContinuityAnchorsSummary(),
    val remainingResidualRisks: List<String> = emptyList(),
    val nextAllowedDownstreamStep: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<AcceptedScriptPacketHandoffFinding> = emptyList(),
    val evidence: AcceptedScriptPacketHandoffEvidence = AcceptedScriptPacketHandoffEvidence(),
)

@Serializable
data class AcceptedScriptPacketSourceReports(
    val structureBridgeReportFile: String = "",
    val structureBridgeVerdict: String = "",
    val sourceBindingVerdict: String = "",
    val structureVerdict: String = "",
    val scriptPacketPreflightReportFile: String = "",
    val acceptedDraftPacketFile: String = "",
)

@Serializable
data class AcceptedScriptPacketContinuityAnchorsSummary(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val protectedContinuityAnchorCount: Int = 0,
    val protectedContinuityAnchors: List<String> = emptyList(),
)

@Serializable
data class AcceptedScriptPacketHandoffFinding(
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
data class AcceptedScriptPacketHandoffEvidence(
    val bridgeDownstreamAllowed: Boolean = false,
    val scriptPacketCharacters: Int = 0,
    val protectedContinuityAnchorCount: Int = 0,
    val sourceBindingFindingCount: Int = 0,
    val structureFindingCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val residualRiskCount: Int = 0,
)


@Serializable
data class ComicVideoPacket(
    val packetId: String = "",
    val packetStatus: String = "comic_video_packet_candidate",
    val reviewableStoryboardPacketFile: String = "",
    val imageFacingDeliveryProofFile: String = "",
    val sourceReviewableStoryboardPacketId: String = "",
    val sourceImageFacingDeliveryProofId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val sourceVerdicts: ComicVideoPacketSourceVerdicts = ComicVideoPacketSourceVerdicts(),
    val panelTracks: List<ComicVideoPanelTrack> = emptyList(),
    val voiceCast: List<ComicVideoVoiceCast> = emptyList(),
    val audioCues: List<ComicVideoAudioCue> = emptyList(),
    val subtitleCues: List<ComicVideoSubtitleCue> = emptyList(),
    val timing: ComicVideoTiming = ComicVideoTiming(),
    val downstreamBoundary: String = "comic_video_packet_metadata_only",
    val nextAllowedDownstreamStep: String = "comic_video_packet_validation",
    val boundaryChecklist: List<String> = emptyList(),
    val residualRisks: List<String> = emptyList(),
)

@Serializable
data class ComicVideoPacketSourceVerdicts(
    val reviewableStoryboardPacketVerdict: String = "",
    val imageFacingDeliveryProofVerdict: String = "",
    val imageFacingDeliveryProofNextAllowedDownstreamStep: String = "",
)

@Serializable
data class ComicVideoPanelTrack(
    val panelId: String = "",
    val sourceFrameId: String = "",
    val sceneReference: String = "",
    val beatReference: String = "",
    val visualIntent: String = "",
    val cameraIntent: String = "",
    val displayDurationMillis: Int = 0,
    val transition: String = "cut",
    val mustShow: List<String> = emptyList(),
    val doNotShow: List<String> = emptyList(),
    val continuityAnchors: List<String> = emptyList(),
    val assetCandidateRefs: List<String> = emptyList(),
    val localImagePath: String = "",
    val renderedPanelPath: String = "",
    val remoteImageUrl: String = "",
    val providerJobId: String = "",
)

@Serializable
data class ComicVideoVoiceCast(
    val voiceId: String = "",
    val characterId: String = "",
    val displayName: String = "",
    val voiceLabel: String = "",
    val voiceBoundary: String = "voice_cast_plan_only",
    val notes: String = "",
)

@Serializable
data class ComicVideoAudioCue(
    val cueId: String = "",
    val panelId: String = "",
    val voiceId: String = "",
    val characterId: String = "",
    val text: String = "",
    val startMillis: Int = 0,
    val durationMillis: Int = 0,
    val audioAssetPath: String = "",
    val remoteAudioUrl: String = "",
    val providerJobId: String = "",
    val generatedStatus: String = "",
)

@Serializable
data class ComicVideoSubtitleCue(
    val cueId: String = "",
    val panelId: String = "",
    val audioCueId: String = "",
    val text: String = "",
    val startMillis: Int = 0,
    val endMillis: Int = 0,
)

@Serializable
data class ComicVideoTiming(
    val timelineStatus: String = "metadata_only_timing_plan",
    val totalDurationMillis: Int = 0,
    val frameRate: String = "24",
    val resolution: String = "1920x1080",
    val backgroundPolicy: String = "panel_track_only",
    val outputVideoPath: String = "",
    val renderedVideoPath: String = "",
)

@Serializable
data class ComicVideoVoiceTimelineFill(
    val fillId: String = "",
    val sourceComicVideoPacketId: String = "",
    val fillStatus: String = "comic_video_voice_timeline_fill_candidate",
    val voiceCast: List<ComicVideoVoiceCast> = emptyList(),
    val audioCues: List<ComicVideoAudioCue> = emptyList(),
    val subtitleCues: List<ComicVideoSubtitleCue> = emptyList(),
    val timing: ComicVideoTiming = ComicVideoTiming(),
    val boundaryChecklist: List<String> = emptyList(),
    val notes: List<String> = emptyList(),
)

@Serializable
data class ComicVideoPacketValidationReport(
    val verdict: String,
    val validationId: String = "",
    val comicVideoPacketFile: String = "",
    val packetId: String = "",
    val packetStatus: String = "",
    val reviewableStoryboardPacketFile: String = "",
    val imageFacingDeliveryProofFile: String = "",
    val sourceReviewableStoryboardPacketId: String = "",
    val sourceImageFacingDeliveryProofId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val sourceVerdicts: ComicVideoPacketSourceVerdicts = ComicVideoPacketSourceVerdicts(),
    val panelTracks: List<ComicVideoPanelTrack> = emptyList(),
    val voiceCast: List<ComicVideoVoiceCast> = emptyList(),
    val audioCues: List<ComicVideoAudioCue> = emptyList(),
    val subtitleCues: List<ComicVideoSubtitleCue> = emptyList(),
    val timing: ComicVideoTiming = ComicVideoTiming(),
    val downstreamBoundary: String = "comic_video_packet_validation_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ComicVideoPacketValidationFinding> = emptyList(),
    val evidence: ComicVideoPacketValidationEvidence = ComicVideoPacketValidationEvidence(),
)

@Serializable
data class ComicVideoPacketValidationFinding(
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
data class ComicVideoPacketValidationEvidence(
    val sourceFieldCount: Int = 0,
    val panelTrackCount: Int = 0,
    val uniquePanelIdCount: Int = 0,
    val panelDurationMillisTotal: Int = 0,
    val voiceCastCount: Int = 0,
    val uniqueVoiceIdCount: Int = 0,
    val audioCueCount: Int = 0,
    val subtitleCueCount: Int = 0,
    val crossReferenceMismatchCount: Int = 0,
    val generatedMediaFieldCount: Int = 0,
    val remoteOrProviderFieldCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
)

@Serializable
data class ComicVideoRenderHandoffManifest(
    val verdict: String,
    val manifestId: String = "",
    val manifestStatus: String = "FAIL",
    val comicVideoPacketValidationReportFile: String = "",
    val validationId: String = "",
    val packetId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val panelTimeline: List<ComicVideoRenderPanelTimelineItem> = emptyList(),
    val voiceTimeline: List<ComicVideoRenderVoiceTimelineItem> = emptyList(),
    val subtitleTimeline: List<ComicVideoRenderSubtitleTimelineItem> = emptyList(),
    val renderSettings: ComicVideoRenderSettings = ComicVideoRenderSettings(),
    val downstreamBoundary: String = "comic_video_render_handoff_manifest_metadata_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ComicVideoRenderHandoffManifestFinding> = emptyList(),
    val evidence: ComicVideoRenderHandoffManifestEvidence = ComicVideoRenderHandoffManifestEvidence(),
)

@Serializable
data class ComicVideoRenderPanelTimelineItem(
    val panelId: String = "",
    val sourceFrameId: String = "",
    val sceneReference: String = "",
    val beatReference: String = "",
    val visualIntent: String = "",
    val cameraIntent: String = "",
    val startMillis: Int = 0,
    val endMillis: Int = 0,
    val displayDurationMillis: Int = 0,
    val transition: String = "cut",
    val mustShow: List<String> = emptyList(),
    val doNotShow: List<String> = emptyList(),
    val continuityAnchors: List<String> = emptyList(),
    val assetCandidateRefs: List<String> = emptyList(),
    val mediaSlotStatus: String = "external_panel_image_required",
)

@Serializable
data class ComicVideoRenderVoiceTimelineItem(
    val cueId: String = "",
    val panelId: String = "",
    val voiceId: String = "",
    val characterId: String = "",
    val displayName: String = "",
    val voiceLabel: String = "",
    val text: String = "",
    val startMillis: Int = 0,
    val endMillis: Int = 0,
    val durationMillis: Int = 0,
    val mediaSlotStatus: String = "external_audio_required",
)

@Serializable
data class ComicVideoRenderSubtitleTimelineItem(
    val cueId: String = "",
    val panelId: String = "",
    val audioCueId: String = "",
    val text: String = "",
    val startMillis: Int = 0,
    val endMillis: Int = 0,
)

@Serializable
data class ComicVideoRenderSettings(
    val timelineStatus: String = "metadata_only_timing_plan",
    val totalDurationMillis: Int = 0,
    val frameRate: String = "24",
    val resolution: String = "1920x1080",
    val backgroundPolicy: String = "panel_track_only",
    val rendererBoundary: String = "external_manual_renderer_only",
)

@Serializable
data class ComicVideoRenderHandoffManifestFinding(
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
data class ComicVideoRenderHandoffManifestEvidence(
    val validationVerdict: String = "",
    val validationNextAllowedDownstreamStep: String = "",
    val sourceFieldCount: Int = 0,
    val panelTimelineItemCount: Int = 0,
    val voiceTimelineItemCount: Int = 0,
    val subtitleTimelineItemCount: Int = 0,
    val totalDurationMillis: Int = 0,
    val mediaOutputFieldCount: Int = 0,
)

@Serializable
data class ComicVideoLocalRenderReport(
    val verdict: String,
    val renderId: String = "",
    val renderStatus: String = "FAIL",
    val renderHandoffManifestFile: String = "",
    val manifestId: String = "",
    val packetId: String = "",
    val outputVideoPath: String = "",
    val workDir: String = "",
    val voiceName: String = "",
    val dryRun: Boolean = false,
    val localRendererBoundary: String = "local_ffmpeg_windows_speech_preview_renderer_only",
    val mediaGenerated: Boolean = false,
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ComicVideoLocalRenderFinding> = emptyList(),
    val artifacts: List<ComicVideoLocalRenderArtifact> = emptyList(),
    val evidence: ComicVideoLocalRenderEvidence = ComicVideoLocalRenderEvidence(),
)

@Serializable
data class ComicVideoLocalRenderFinding(
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
data class ComicVideoLocalRenderArtifact(
    val artifactType: String = "",
    val path: String = "",
    val exists: Boolean = false,
    val bytes: Long = 0,
)

@Serializable
data class ComicVideoLocalRenderEvidence(
    val manifestVerdict: String = "",
    val manifestNextAllowedDownstreamStep: String = "",
    val panelCount: Int = 0,
    val voiceCueCount: Int = 0,
    val subtitleCueCount: Int = 0,
    val totalDurationMillis: Int = 0,
    val resolution: String = "",
    val frameRate: String = "",
    val ffmpegCommandCount: Int = 0,
    val speechCommandCount: Int = 0,
)

@Serializable
data class ComicVideoLocalPreviewBuildReport(
    val verdict: String,
    val buildId: String = "",
    val buildStatus: String = "FAIL",
    val imageFacingDeliveryProofFile: String = "",
    val voiceTimelineFillFile: String = "",
    val outputDir: String = "",
    val packetFile: String = "",
    val voiceTimelineFillTemplateFile: String = "",
    val filledPacketFile: String = "",
    val packetValidationFile: String = "",
    val renderHandoffManifestFile: String = "",
    val localRenderReportFile: String = "",
    val outputVideoPath: String = "",
    val packetId: String = "",
    val validationId: String = "",
    val manifestId: String = "",
    val renderId: String = "",
    val dryRun: Boolean = false,
    val mediaGenerated: Boolean = false,
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ComicVideoLocalPreviewBuildFinding> = emptyList(),
    val renderReport: ComicVideoLocalRenderReport? = null,
    val evidence: ComicVideoLocalPreviewBuildEvidence = ComicVideoLocalPreviewBuildEvidence(),
)

@Serializable
data class ComicVideoLocalPreviewBuildFinding(
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
data class ComicVideoLocalPreviewBuildEvidence(
    val proofVerdict: String = "",
    val packetPanelCount: Int = 0,
    val voiceCastCount: Int = 0,
    val audioCueCount: Int = 0,
    val subtitleCueCount: Int = 0,
    val packetValidationVerdict: String = "",
    val manifestVerdict: String = "",
    val localRenderVerdict: String = "",
    val totalDurationMillis: Int = 0,
)

@Serializable
data class StoryboardPacketHumanReviewDecision(
    val decisionId: String = "",
    val preflightId: String = "",
    val storyboardPacketReviewFile: String = "",
    val storyboardPacketReviewId: String = "",
    val storyboardPacketDraftFile: String = "",
    val storyboardPacketDraftReportFile: String = "",
    val storyboardPacketDraftId: String = "",
    val storyboardPlanningBriefId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val decisionSource: String = "",
    val reviewer: String = "",
    val reviewerRole: String = "",
    val decisionAt: String = "",
    val decisionStatus: String = "",
    val checklistItems: List<StoryboardPacketHumanReviewDecisionChecklistItem> = emptyList(),
    val blockingIssues: List<StoryboardPacketHumanReviewDecisionBlockingIssue> = emptyList(),
    val notes: String = "",
)

@Serializable
data class StoryboardPacketHumanReviewDecisionChecklistItem(
    val itemId: String = "",
    val category: String = "",
    val result: String = "",
    val note: String = "",
)

@Serializable
data class StoryboardPacketHumanReviewDecisionBlockingIssue(
    val issueId: String = "",
    val severity: String = "",
    val status: String = "",
    val description: String = "",
)

@Serializable
data class StoryboardPacketHumanReviewDecisionIntakeReport(
    val verdict: String,
    val decisionId: String = "",
    val preflightId: String = "",
    val humanReviewPreflightFile: String = "",
    val humanReviewDecisionFile: String = "",
    val storyboardPacketReviewFile: String = "",
    val storyboardPacketReviewId: String = "",
    val storyboardPacketDraftFile: String = "",
    val storyboardPacketDraftReportFile: String = "",
    val storyboardPacketDraftId: String = "",
    val storyboardPlanningBriefId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val decisionStatus: String = "",
    val decisionAllowedDownstream: Boolean = false,
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<StoryboardPacketHumanReviewDecisionIntakeFinding> = emptyList(),
    val evidence: StoryboardPacketHumanReviewDecisionIntakeEvidence =
        StoryboardPacketHumanReviewDecisionIntakeEvidence(),
)

@Serializable
data class StoryboardPacketHumanReviewDecisionIntakeFinding(
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
data class StoryboardPacketHumanReviewDecisionIntakeEvidence(
    val preflightVerdict: String = "",
    val preflightNextAllowedDownstreamStep: String = "",
    val sourceBindingFieldCount: Int = 0,
    val decisionSource: String = "",
    val decisionStatus: String = "",
    val checklistItemCount: Int = 0,
    val checklistPassedCount: Int = 0,
    val unresolvedBlockingIssueCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
)

@Serializable
data class AssetCandidatePreflightReport(
    val verdict: String,
    val preflightId: String = "",
    val humanReviewDecisionIntakeFile: String = "",
    val storyboardPacketReviewFile: String = "",
    val decisionId: String = "",
    val humanReviewPreflightId: String = "",
    val storyboardPacketReviewId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val assetCandidateRequirements: List<StoryboardPacketReviewAssetRequirement> = emptyList(),
    val downstreamBoundary: String = "asset_candidate_preparation_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<AssetCandidatePreflightFinding> = emptyList(),
    val evidence: AssetCandidatePreflightEvidence = AssetCandidatePreflightEvidence(),
)

@Serializable
data class AssetCandidatePreflightFinding(
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
data class AssetCandidatePreflightEvidence(
    val decisionIntakeVerdict: String = "",
    val decisionAllowedDownstream: Boolean = false,
    val decisionStatus: String = "",
    val decisionNextAllowedDownstreamStep: String = "",
    val sourceBindingFieldCount: Int = 0,
    val assetCandidateRequirementCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
)

@Serializable
data class AssetCandidateBrief(
    val verdict: String,
    val briefId: String = "",
    val assetCandidatePreflightFile: String = "",
    val assetCandidatePreflightId: String = "",
    val humanReviewDecisionIntakeFile: String = "",
    val humanReviewDecisionId: String = "",
    val storyboardPacketReviewFile: String = "",
    val storyboardPacketReviewId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val assetCandidateRequirements: List<AssetCandidateBriefRequirement> = emptyList(),
    val preparationChecklist: List<AssetCandidateBriefChecklistItem> = emptyList(),
    val downstreamBoundary: String = "asset_candidate_brief_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<AssetCandidateBriefFinding> = emptyList(),
    val evidence: AssetCandidateBriefEvidence = AssetCandidateBriefEvidence(),
)

@Serializable
data class AssetCandidateBriefRequirement(
    val candidateId: String = "",
    val requirement: String = "",
    val sourceFrameIds: List<String> = emptyList(),
    val candidateStatus: String = "candidate_only",
)

@Serializable
data class AssetCandidateBriefChecklistItem(
    val itemId: String = "",
    val category: String = "",
    val requiredCheck: String = "",
    val status: String = "required_for_asset_candidate_draft",
)

@Serializable
data class AssetCandidateBriefFinding(
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
data class AssetCandidateBriefEvidence(
    val preflightVerdict: String = "",
    val preflightNextAllowedDownstreamStep: String = "",
    val sourceFieldCount: Int = 0,
    val assetCandidateRequirementCount: Int = 0,
    val preparationChecklistCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
)

@Serializable
data class AssetCandidateDraft(
    val verdict: String,
    val draftId: String = "",
    val assetCandidateBriefFile: String = "",
    val assetCandidateBriefId: String = "",
    val assetCandidatePreflightFile: String = "",
    val assetCandidatePreflightId: String = "",
    val humanReviewDecisionIntakeFile: String = "",
    val humanReviewDecisionId: String = "",
    val storyboardPacketReviewFile: String = "",
    val storyboardPacketReviewId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val candidateItems: List<AssetCandidateDraftItem> = emptyList(),
    val downstreamBoundary: String = "asset_candidate_draft_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<AssetCandidateDraftFinding> = emptyList(),
    val evidence: AssetCandidateDraftEvidence = AssetCandidateDraftEvidence(),
)

@Serializable
data class AssetCandidateDraftItem(
    val candidateId: String = "",
    val assetType: String = "",
    val sourceRequirementId: String = "",
    val candidateStatus: String = "draft_candidate",
    val requirement: String = "",
    val publicDescription: String = "",
    val visualReferenceRequirement: String = "",
    val sourceFrameIds: List<String> = emptyList(),
)

@Serializable
data class AssetCandidateDraftFinding(
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
data class AssetCandidateDraftEvidence(
    val briefVerdict: String = "",
    val briefNextAllowedDownstreamStep: String = "",
    val sourceFieldCount: Int = 0,
    val candidateItemCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
)

@Serializable
data class AssetCandidateDraftValidationReport(
    val verdict: String,
    val assetCandidateDraftFile: String = "",
    val assetCandidateDraftId: String = "",
    val assetCandidateBriefFile: String = "",
    val assetCandidateBriefId: String = "",
    val assetCandidatePreflightFile: String = "",
    val assetCandidatePreflightId: String = "",
    val humanReviewDecisionIntakeFile: String = "",
    val humanReviewDecisionId: String = "",
    val storyboardPacketReviewFile: String = "",
    val storyboardPacketReviewId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val candidateItems: List<AssetCandidateDraftItem> = emptyList(),
    val downstreamBoundary: String = "asset_candidate_draft_validation_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<AssetCandidateDraftValidationFinding> = emptyList(),
    val evidence: AssetCandidateDraftValidationEvidence = AssetCandidateDraftValidationEvidence(),
)

@Serializable
data class AssetCandidateDraftValidationFinding(
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
data class AssetCandidateDraftValidationEvidence(
    val draftVerdict: String = "",
    val draftNextAllowedDownstreamStep: String = "",
    val sourceFieldCount: Int = 0,
    val candidateItemCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
)

@Serializable
data class AssetCandidateReviewPreflightReport(
    val verdict: String,
    val preflightId: String = "",
    val assetCandidateDraftValidationReportFile: String = "",
    val assetCandidateDraftFile: String = "",
    val assetCandidateDraftId: String = "",
    val assetCandidateBriefFile: String = "",
    val assetCandidateBriefId: String = "",
    val assetCandidatePreflightFile: String = "",
    val assetCandidatePreflightId: String = "",
    val humanReviewDecisionIntakeFile: String = "",
    val humanReviewDecisionId: String = "",
    val storyboardPacketReviewFile: String = "",
    val storyboardPacketReviewId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val candidateItemCount: Int = 0,
    val downstreamBoundary: String = "asset_candidate_human_review_preparation_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<AssetCandidateReviewPreflightFinding> = emptyList(),
    val evidence: AssetCandidateReviewPreflightEvidence = AssetCandidateReviewPreflightEvidence(),
)

@Serializable
data class AssetCandidateReviewPreflightFinding(
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
data class AssetCandidateReviewPreflightEvidence(
    val draftValidationVerdict: String = "",
    val draftValidationNextAllowedDownstreamStep: String = "",
    val sourceFieldCount: Int = 0,
    val candidateItemCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
)

@Serializable
data class AssetCandidateHumanReviewDecision(
    val decisionId: String = "",
    val preflightId: String = "",
    val assetCandidateReviewPreflightFile: String = "",
    val assetCandidateDraftValidationReportFile: String = "",
    val assetCandidateDraftFile: String = "",
    val assetCandidateDraftId: String = "",
    val assetCandidateBriefFile: String = "",
    val assetCandidateBriefId: String = "",
    val assetCandidatePreflightFile: String = "",
    val assetCandidatePreflightId: String = "",
    val humanReviewDecisionIntakeFile: String = "",
    val humanReviewDecisionId: String = "",
    val storyboardPacketReviewFile: String = "",
    val storyboardPacketReviewId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val decisionSource: String = "",
    val reviewer: String = "",
    val reviewerRole: String = "",
    val decisionAt: String = "",
    val decisionStatus: String = "",
    val checklistItems: List<AssetCandidateHumanReviewDecisionChecklistItem> = emptyList(),
    val blockingIssues: List<AssetCandidateHumanReviewDecisionBlockingIssue> = emptyList(),
    val imageCandidateRequirements: List<ImageCandidateRequirement> = emptyList(),
    val notes: String = "",
)

@Serializable
data class AssetCandidateHumanReviewDecisionChecklistItem(
    val itemId: String = "",
    val category: String = "",
    val result: String = "",
    val note: String = "",
)

@Serializable
data class AssetCandidateHumanReviewDecisionBlockingIssue(
    val issueId: String = "",
    val severity: String = "",
    val status: String = "",
    val description: String = "",
)

@Serializable
data class ImageCandidateRequirement(
    val requirementId: String = "",
    val candidateId: String = "",
    val assetType: String = "",
    val sourceRequirementId: String = "",
    val status: String = "candidate_only",
    val requirement: String = "",
    val referenceRequirements: List<String> = emptyList(),
    val doNotShow: List<String> = emptyList(),
    val promptSafetyNotes: List<String> = emptyList(),
    val imageOutputPath: String = "",
    val generationApi: String = "",
    val providerJobId: String = "",
)

@Serializable
data class AssetCandidateHumanReviewDecisionIntakeReport(
    val verdict: String,
    val decisionId: String = "",
    val preflightId: String = "",
    val assetCandidateReviewPreflightFile: String = "",
    val assetCandidateHumanReviewDecisionFile: String = "",
    val assetCandidateDraftValidationReportFile: String = "",
    val assetCandidateDraftFile: String = "",
    val assetCandidateDraftId: String = "",
    val assetCandidateBriefFile: String = "",
    val assetCandidateBriefId: String = "",
    val assetCandidatePreflightFile: String = "",
    val assetCandidatePreflightId: String = "",
    val humanReviewDecisionIntakeFile: String = "",
    val humanReviewDecisionId: String = "",
    val storyboardPacketReviewFile: String = "",
    val storyboardPacketReviewId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val decisionStatus: String = "",
    val decisionAllowedDownstream: Boolean = false,
    val imageCandidateRequirements: List<ImageCandidateRequirement> = emptyList(),
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<AssetCandidateHumanReviewDecisionIntakeFinding> = emptyList(),
    val evidence: AssetCandidateHumanReviewDecisionIntakeEvidence =
        AssetCandidateHumanReviewDecisionIntakeEvidence(),
)

@Serializable
data class AssetCandidateHumanReviewDecisionIntakeFinding(
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
data class AssetCandidateHumanReviewDecisionIntakeEvidence(
    val preflightVerdict: String = "",
    val preflightNextAllowedDownstreamStep: String = "",
    val sourceBindingFieldCount: Int = 0,
    val decisionSource: String = "",
    val decisionStatus: String = "",
    val checklistItemCount: Int = 0,
    val checklistPassedCount: Int = 0,
    val unresolvedBlockingIssueCount: Int = 0,
    val imageCandidateRequirementCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
    val forbiddenOutputFieldCount: Int = 0,
)

@Serializable
data class ImageCandidatePreflightReport(
    val verdict: String,
    val preflightId: String = "",
    val assetCandidateHumanReviewDecisionIntakeFile: String = "",
    val decisionId: String = "",
    val assetCandidateReviewPreflightFile: String = "",
    val assetCandidateReviewPreflightId: String = "",
    val assetCandidateDraftValidationReportFile: String = "",
    val assetCandidateDraftFile: String = "",
    val assetCandidateDraftId: String = "",
    val assetCandidateBriefFile: String = "",
    val assetCandidateBriefId: String = "",
    val assetCandidatePreflightFile: String = "",
    val assetCandidatePreflightId: String = "",
    val humanReviewDecisionIntakeFile: String = "",
    val humanReviewDecisionId: String = "",
    val storyboardPacketReviewFile: String = "",
    val storyboardPacketReviewId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val imageCandidateRequirements: List<ImageCandidateRequirement> = emptyList(),
    val downstreamBoundary: String = "image_candidate_preflight_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ImageCandidatePreflightFinding> = emptyList(),
    val evidence: ImageCandidatePreflightEvidence = ImageCandidatePreflightEvidence(),
)

@Serializable
data class ImageCandidatePreflightFinding(
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
data class ImageCandidatePreflightEvidence(
    val decisionIntakeVerdict: String = "",
    val decisionAllowedDownstream: Boolean = false,
    val decisionStatus: String = "",
    val decisionNextAllowedDownstreamStep: String = "",
    val sourceFieldCount: Int = 0,
    val imageCandidateRequirementCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
    val forbiddenOutputFieldCount: Int = 0,
)

@Serializable
data class ImageCandidateBrief(
    val verdict: String,
    val briefId: String = "",
    val imageCandidatePreflightFile: String = "",
    val imageCandidatePreflightId: String = "",
    val assetCandidateHumanReviewDecisionIntakeFile: String = "",
    val decisionId: String = "",
    val assetCandidateReviewPreflightFile: String = "",
    val assetCandidateReviewPreflightId: String = "",
    val assetCandidateDraftValidationReportFile: String = "",
    val assetCandidateDraftFile: String = "",
    val assetCandidateDraftId: String = "",
    val assetCandidateBriefFile: String = "",
    val assetCandidateBriefId: String = "",
    val assetCandidatePreflightFile: String = "",
    val assetCandidatePreflightId: String = "",
    val humanReviewDecisionIntakeFile: String = "",
    val humanReviewDecisionId: String = "",
    val storyboardPacketReviewFile: String = "",
    val storyboardPacketReviewId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val imageCandidateRequirements: List<ImageCandidateRequirement> = emptyList(),
    val preparationChecklist: List<ImageCandidateBriefChecklistItem> = emptyList(),
    val downstreamBoundary: String = "image_candidate_brief_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ImageCandidateBriefFinding> = emptyList(),
    val evidence: ImageCandidateBriefEvidence = ImageCandidateBriefEvidence(),
)

@Serializable
data class ImageCandidateBriefChecklistItem(
    val itemId: String = "",
    val category: String = "",
    val requiredCheck: String = "",
    val status: String = "required_for_prompt_preparation",
)

@Serializable
data class ImageCandidateBriefFinding(
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
data class ImageCandidateBriefEvidence(
    val imageCandidatePreflightVerdict: String = "",
    val imageCandidatePreflightNextAllowedDownstreamStep: String = "",
    val sourceFieldCount: Int = 0,
    val imageCandidateRequirementCount: Int = 0,
    val preparationChecklistCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
    val forbiddenOutputFieldCount: Int = 0,
)

@Serializable
data class ImageCandidatePromptPreparation(
    val verdict: String,
    val preparationId: String = "",
    val imageCandidateBriefFile: String = "",
    val imageCandidateBriefId: String = "",
    val imageCandidatePreflightFile: String = "",
    val imageCandidatePreflightId: String = "",
    val assetCandidateHumanReviewDecisionIntakeFile: String = "",
    val decisionId: String = "",
    val assetCandidateReviewPreflightFile: String = "",
    val assetCandidateReviewPreflightId: String = "",
    val assetCandidateDraftValidationReportFile: String = "",
    val assetCandidateDraftFile: String = "",
    val assetCandidateDraftId: String = "",
    val assetCandidateBriefFile: String = "",
    val assetCandidateBriefId: String = "",
    val assetCandidatePreflightFile: String = "",
    val assetCandidatePreflightId: String = "",
    val humanReviewDecisionIntakeFile: String = "",
    val humanReviewDecisionId: String = "",
    val storyboardPacketReviewFile: String = "",
    val storyboardPacketReviewId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val promptItems: List<ImageCandidatePromptItem> = emptyList(),
    val downstreamBoundary: String = "image_candidate_prompt_preparation_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ImageCandidatePromptPreparationFinding> = emptyList(),
    val evidence: ImageCandidatePromptPreparationEvidence =
        ImageCandidatePromptPreparationEvidence(),
)

@Serializable
data class ImageCandidatePromptItem(
    val promptItemId: String = "",
    val sourceRequirementId: String = "",
    val sourceCandidateId: String = "",
    val assetType: String = "",
    val positivePromptDraft: String = "",
    val negativePromptDraft: String = "",
    val doNotShow: List<String> = emptyList(),
    val promptSafetyNotes: List<String> = emptyList(),
    val publicVisualRequirements: List<String> = emptyList(),
    val providerName: String = "",
    val generationApi: String = "",
    val providerJobId: String = "",
    val imageOutputPath: String = "",
)

@Serializable
data class ImageCandidatePromptPreparationFinding(
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
data class ImageCandidatePromptPreparationEvidence(
    val imageCandidateBriefVerdict: String = "",
    val imageCandidateBriefNextAllowedDownstreamStep: String = "",
    val sourceFieldCount: Int = 0,
    val promptItemCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
    val forbiddenOutputFieldCount: Int = 0,
)

@Serializable
data class ImageCandidatePromptValidationReport(
    val verdict: String,
    val imageCandidatePromptPreparationFile: String = "",
    val preparationId: String = "",
    val imageCandidateBriefFile: String = "",
    val imageCandidateBriefId: String = "",
    val imageCandidatePreflightFile: String = "",
    val imageCandidatePreflightId: String = "",
    val assetCandidateHumanReviewDecisionIntakeFile: String = "",
    val decisionId: String = "",
    val assetCandidateReviewPreflightFile: String = "",
    val assetCandidateReviewPreflightId: String = "",
    val assetCandidateDraftValidationReportFile: String = "",
    val assetCandidateDraftFile: String = "",
    val assetCandidateDraftId: String = "",
    val assetCandidateBriefFile: String = "",
    val assetCandidateBriefId: String = "",
    val assetCandidatePreflightFile: String = "",
    val assetCandidatePreflightId: String = "",
    val humanReviewDecisionIntakeFile: String = "",
    val humanReviewDecisionId: String = "",
    val storyboardPacketReviewFile: String = "",
    val storyboardPacketReviewId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val promptItems: List<ImageCandidatePromptItem> = emptyList(),
    val downstreamBoundary: String = "image_candidate_prompt_validation_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ImageCandidatePromptValidationFinding> = emptyList(),
    val evidence: ImageCandidatePromptValidationEvidence =
        ImageCandidatePromptValidationEvidence(),
)

@Serializable
data class ImageCandidatePromptValidationFinding(
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
data class ImageCandidatePromptValidationEvidence(
    val promptPreparationVerdict: String = "",
    val promptPreparationNextAllowedDownstreamStep: String = "",
    val sourceFieldCount: Int = 0,
    val promptItemCount: Int = 0,
    val authorOnlyLeakCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
    val forbiddenOutputFieldCount: Int = 0,
)

@Serializable
data class ImageCandidateProviderHandoffPreflightReport(
    val verdict: String,
    val preflightId: String = "",
    val imageCandidatePromptValidationReportFile: String = "",
    val imageCandidatePromptPreparationFile: String = "",
    val preparationId: String = "",
    val imageCandidateBriefFile: String = "",
    val imageCandidateBriefId: String = "",
    val imageCandidatePreflightFile: String = "",
    val imageCandidatePreflightId: String = "",
    val assetCandidateHumanReviewDecisionIntakeFile: String = "",
    val decisionId: String = "",
    val assetCandidateReviewPreflightFile: String = "",
    val assetCandidateReviewPreflightId: String = "",
    val assetCandidateDraftValidationReportFile: String = "",
    val assetCandidateDraftFile: String = "",
    val assetCandidateDraftId: String = "",
    val assetCandidateBriefFile: String = "",
    val assetCandidateBriefId: String = "",
    val assetCandidatePreflightFile: String = "",
    val assetCandidatePreflightId: String = "",
    val humanReviewDecisionIntakeFile: String = "",
    val humanReviewDecisionId: String = "",
    val storyboardPacketReviewFile: String = "",
    val storyboardPacketReviewId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val promptItems: List<ImageCandidatePromptItem> = emptyList(),
    val downstreamBoundary: String = "image_candidate_manual_provider_handoff_preparation_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ImageCandidateProviderHandoffPreflightFinding> = emptyList(),
    val evidence: ImageCandidateProviderHandoffPreflightEvidence =
        ImageCandidateProviderHandoffPreflightEvidence(),
)

@Serializable
data class ImageCandidateProviderHandoffPreflightFinding(
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
data class ImageCandidateProviderHandoffPreflightEvidence(
    val promptValidationVerdict: String = "",
    val promptValidationNextAllowedDownstreamStep: String = "",
    val sourceFieldCount: Int = 0,
    val promptItemCount: Int = 0,
    val authorOnlyLeakCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
    val forbiddenOutputFieldCount: Int = 0,
)

@Serializable
data class ImageCandidateProviderHandoffDecision(
    val decisionId: String = "",
    val preflightId: String = "",
    val providerHandoffPreflightFile: String = "",
    val imageCandidatePromptValidationReportFile: String = "",
    val imageCandidatePromptPreparationFile: String = "",
    val preparationId: String = "",
    val imageCandidateBriefFile: String = "",
    val imageCandidateBriefId: String = "",
    val imageCandidatePreflightFile: String = "",
    val imageCandidatePreflightId: String = "",
    val assetCandidateHumanReviewDecisionIntakeFile: String = "",
    val decisionSource: String = "",
    val reviewer: String = "",
    val reviewerRole: String = "",
    val decisionAt: String = "",
    val decisionStatus: String = "",
    val checklistItems: List<ImageCandidateProviderHandoffDecisionChecklistItem> = emptyList(),
    val blockingIssues: List<ImageCandidateProviderHandoffDecisionBlockingIssue> = emptyList(),
    val notes: String = "",
)

@Serializable
data class ImageCandidateProviderHandoffDecisionChecklistItem(
    val itemId: String = "",
    val category: String = "",
    val result: String = "",
    val note: String = "",
)

@Serializable
data class ImageCandidateProviderHandoffDecisionBlockingIssue(
    val issueId: String = "",
    val severity: String = "",
    val status: String = "",
    val description: String = "",
)

@Serializable
data class ImageCandidateProviderHandoffDecisionIntakeReport(
    val verdict: String,
    val decisionId: String = "",
    val preflightId: String = "",
    val providerHandoffPreflightFile: String = "",
    val providerHandoffDecisionFile: String = "",
    val imageCandidatePromptValidationReportFile: String = "",
    val imageCandidatePromptPreparationFile: String = "",
    val preparationId: String = "",
    val imageCandidateBriefFile: String = "",
    val imageCandidateBriefId: String = "",
    val imageCandidatePreflightFile: String = "",
    val imageCandidatePreflightId: String = "",
    val assetCandidateHumanReviewDecisionIntakeFile: String = "",
    val assetDecisionId: String = "",
    val assetCandidateReviewPreflightFile: String = "",
    val assetCandidateReviewPreflightId: String = "",
    val assetCandidateDraftValidationReportFile: String = "",
    val assetCandidateDraftFile: String = "",
    val assetCandidateDraftId: String = "",
    val assetCandidateBriefFile: String = "",
    val assetCandidateBriefId: String = "",
    val assetCandidatePreflightFile: String = "",
    val assetCandidatePreflightId: String = "",
    val humanReviewDecisionIntakeFile: String = "",
    val humanReviewDecisionId: String = "",
    val storyboardPacketReviewFile: String = "",
    val storyboardPacketReviewId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val decisionStatus: String = "",
    val decisionAllowedDownstream: Boolean = false,
    val promptItems: List<ImageCandidatePromptItem> = emptyList(),
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ImageCandidateProviderHandoffDecisionIntakeFinding> = emptyList(),
    val evidence: ImageCandidateProviderHandoffDecisionIntakeEvidence =
        ImageCandidateProviderHandoffDecisionIntakeEvidence(),
)

@Serializable
data class ImageCandidateProviderHandoffDecisionIntakeFinding(
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
data class ImageCandidateProviderHandoffDecisionIntakeEvidence(
    val providerHandoffPreflightVerdict: String = "",
    val providerHandoffPreflightNextAllowedDownstreamStep: String = "",
    val sourceBindingFieldCount: Int = 0,
    val decisionSource: String = "",
    val decisionStatus: String = "",
    val checklistItemCount: Int = 0,
    val checklistPassedCount: Int = 0,
    val unresolvedBlockingIssueCount: Int = 0,
    val promptItemCount: Int = 0,
    val authorOnlyLeakCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
    val forbiddenOutputFieldCount: Int = 0,
)

@Serializable
data class ImageCandidateProviderHandoffPacket(
    val verdict: String,
    val packetId: String = "",
    val providerHandoffPreflightFile: String = "",
    val providerHandoffDecisionIntakeFile: String = "",
    val decisionId: String = "",
    val preflightId: String = "",
    val imageCandidatePromptValidationReportFile: String = "",
    val imageCandidatePromptPreparationFile: String = "",
    val preparationId: String = "",
    val imageCandidateBriefFile: String = "",
    val imageCandidateBriefId: String = "",
    val imageCandidatePreflightFile: String = "",
    val imageCandidatePreflightId: String = "",
    val assetCandidateHumanReviewDecisionIntakeFile: String = "",
    val assetDecisionId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val promptItems: List<ImageCandidatePromptItem> = emptyList(),
    val manualInstructions: List<String> = emptyList(),
    val providerApiEndpoint: String = "",
    val providerJobId: String = "",
    val submittedAt: String = "",
    val outputPath: String = "",
    val generatedStatus: String = "",
    val downstreamBoundary: String = "image_candidate_manual_provider_handoff_packet_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ImageCandidateProviderHandoffPacketFinding> = emptyList(),
    val evidence: ImageCandidateProviderHandoffPacketEvidence =
        ImageCandidateProviderHandoffPacketEvidence(),
)

@Serializable
data class ImageCandidateProviderHandoffPacketFinding(
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
data class ImageCandidateProviderHandoffPacketEvidence(
    val providerHandoffPreflightVerdict: String = "",
    val decisionIntakeVerdict: String = "",
    val decisionAllowedDownstream: Boolean = false,
    val sourceBindingFieldCount: Int = 0,
    val promptItemCount: Int = 0,
    val manualInstructionCount: Int = 0,
    val authorOnlyLeakCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
    val forbiddenOutputFieldCount: Int = 0,
)

@Serializable
data class ImageCandidateProviderHandoffPacketValidationReport(
    val verdict: String,
    val providerHandoffPacketFile: String = "",
    val packetId: String = "",
    val providerHandoffPreflightFile: String = "",
    val providerHandoffDecisionIntakeFile: String = "",
    val decisionId: String = "",
    val preflightId: String = "",
    val imageCandidatePromptValidationReportFile: String = "",
    val imageCandidatePromptPreparationFile: String = "",
    val preparationId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val promptItems: List<ImageCandidatePromptItem> = emptyList(),
    val manualInstructions: List<String> = emptyList(),
    val downstreamBoundary: String = "image_candidate_provider_handoff_packet_validation_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ImageCandidateProviderHandoffPacketValidationFinding> = emptyList(),
    val evidence: ImageCandidateProviderHandoffPacketValidationEvidence =
        ImageCandidateProviderHandoffPacketValidationEvidence(),
)

@Serializable
data class ImageCandidateProviderHandoffPacketValidationFinding(
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
data class ImageCandidateProviderHandoffPacketValidationEvidence(
    val packetVerdict: String = "",
    val packetNextAllowedDownstreamStep: String = "",
    val sourceFieldCount: Int = 0,
    val promptItemCount: Int = 0,
    val manualInstructionCount: Int = 0,
    val authorOnlyLeakCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
    val forbiddenOutputFieldCount: Int = 0,
)

@Serializable
data class ImageCandidateExternalResult(
    val resultId: String = "",
    val providerHandoffPacketValidationReportFile: String = "",
    val packetId: String = "",
    val preflightId: String = "",
    val decisionId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val resultSource: String = "",
    val recorder: String = "",
    val recorderRole: String = "",
    val resultAt: String = "",
    val resultStatus: String = "",
    val imageResults: List<ImageCandidateExternalResultItem> = emptyList(),
    val notes: String = "",
    val providerJobId: String = "",
    val apiResponse: String = "",
    val remoteUrl: String = "",
)

@Serializable
data class ImageCandidateExternalResultItem(
    val resultItemId: String = "",
    val promptItemId: String = "",
    val sourceCandidateId: String = "",
    val assetType: String = "",
    val candidateStatus: String = "external_result_candidate",
    val localImagePath: String = "",
    val metadataOnlyReference: String = "",
    val sourceFileSha256: String = "",
    val doNotShowChecklist: List<String> = emptyList(),
    val driftReviewChecklist: List<String> = emptyList(),
    val residualRisks: List<String> = emptyList(),
    val notes: String = "",
    val providerJobId: String = "",
    val apiResponse: String = "",
    val remoteUrl: String = "",
    val generatedStatus: String = "",
)

@Serializable
data class ImageCandidateExternalResultIntakeReport(
    val verdict: String,
    val resultId: String = "",
    val providerHandoffPacketValidationReportFile: String = "",
    val externalResultFile: String = "",
    val packetId: String = "",
    val preflightId: String = "",
    val decisionId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val resultStatus: String = "",
    val imageResults: List<ImageCandidateExternalResultItem> = emptyList(),
    val downstreamBoundary: String = "image_candidate_external_result_intake_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ImageCandidateExternalResultIntakeFinding> = emptyList(),
    val evidence: ImageCandidateExternalResultIntakeEvidence =
        ImageCandidateExternalResultIntakeEvidence(),
)

@Serializable
data class ImageCandidateExternalResultIntakeFinding(
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
data class ImageCandidateExternalResultIntakeEvidence(
    val providerHandoffPacketValidationVerdict: String = "",
    val providerHandoffPacketValidationNextAllowedDownstreamStep: String = "",
    val sourceBindingFieldCount: Int = 0,
    val resultSource: String = "",
    val resultStatus: String = "",
    val promptItemCount: Int = 0,
    val imageResultCount: Int = 0,
    val localImageReferenceCount: Int = 0,
    val metadataOnlyReferenceCount: Int = 0,
    val forbiddenRemoteOrProviderFieldCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
)

@Serializable
data class ImageCandidateResultReviewPreflightReport(
    val verdict: String,
    val preflightId: String = "",
    val externalResultIntakeFile: String = "",
    val resultId: String = "",
    val providerHandoffPacketValidationReportFile: String = "",
    val packetId: String = "",
    val preflightSourceId: String = "",
    val decisionId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val imageResults: List<ImageCandidateExternalResultItem> = emptyList(),
    val downstreamBoundary: String = "image_candidate_result_review_preflight_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ImageCandidateResultReviewPreflightFinding> = emptyList(),
    val evidence: ImageCandidateResultReviewPreflightEvidence =
        ImageCandidateResultReviewPreflightEvidence(),
)

@Serializable
data class ImageCandidateResultReviewPreflightFinding(
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
data class ImageCandidateResultReviewPreflightEvidence(
    val externalResultIntakeVerdict: String = "",
    val externalResultIntakeNextAllowedDownstreamStep: String = "",
    val sourceFieldCount: Int = 0,
    val imageResultCount: Int = 0,
    val localImageReferenceCount: Int = 0,
    val metadataOnlyReferenceCount: Int = 0,
    val forbiddenRemoteOrProviderFieldCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
)

@Serializable
data class ImageCandidateResultReviewDecision(
    val decisionId: String = "",
    val resultReviewPreflightFile: String = "",
    val resultReviewPreflightId: String = "",
    val externalResultIntakeFile: String = "",
    val resultId: String = "",
    val providerHandoffPacketValidationReportFile: String = "",
    val packetId: String = "",
    val preflightSourceId: String = "",
    val providerDecisionId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val decisionSource: String = "",
    val reviewer: String = "",
    val reviewerRole: String = "",
    val decisionAt: String = "",
    val decisionStatus: String = "",
    val checklistItems: List<ImageCandidateResultReviewDecisionChecklistItem> = emptyList(),
    val blockingIssues: List<ImageCandidateResultReviewDecisionBlockingIssue> = emptyList(),
    val notes: String = "",
)

@Serializable
data class ImageCandidateResultReviewDecisionChecklistItem(
    val itemId: String = "",
    val category: String = "",
    val result: String = "",
    val note: String = "",
)

@Serializable
data class ImageCandidateResultReviewDecisionBlockingIssue(
    val issueId: String = "",
    val severity: String = "",
    val status: String = "",
    val description: String = "",
)

@Serializable
data class ImageCandidateResultReviewDecisionIntakeReport(
    val verdict: String,
    val decisionId: String = "",
    val resultReviewPreflightFile: String = "",
    val resultReviewDecisionFile: String = "",
    val resultReviewPreflightId: String = "",
    val externalResultIntakeFile: String = "",
    val resultId: String = "",
    val providerHandoffPacketValidationReportFile: String = "",
    val packetId: String = "",
    val preflightSourceId: String = "",
    val providerDecisionId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val decisionStatus: String = "",
    val decisionAllowedDownstream: Boolean = false,
    val imageResults: List<ImageCandidateExternalResultItem> = emptyList(),
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ImageCandidateResultReviewDecisionIntakeFinding> = emptyList(),
    val evidence: ImageCandidateResultReviewDecisionIntakeEvidence =
        ImageCandidateResultReviewDecisionIntakeEvidence(),
)

@Serializable
data class ImageCandidateResultReviewDecisionIntakeFinding(
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
data class ImageCandidateResultReviewDecisionIntakeEvidence(
    val resultReviewPreflightVerdict: String = "",
    val resultReviewPreflightNextAllowedDownstreamStep: String = "",
    val sourceBindingFieldCount: Int = 0,
    val decisionSource: String = "",
    val decisionStatus: String = "",
    val checklistItemCount: Int = 0,
    val checklistPassedCount: Int = 0,
    val unresolvedBlockingIssueCount: Int = 0,
    val imageResultCount: Int = 0,
    val forbiddenRemoteOrProviderFieldCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
)

@Serializable
data class ImageCandidateReferencePacket(
    val verdict: String,
    val packetId: String = "",
    val packetStatus: String = "reference_candidate",
    val resultReviewPreflightFile: String = "",
    val resultReviewDecisionIntakeFile: String = "",
    val resultReviewPreflightId: String = "",
    val decisionId: String = "",
    val externalResultIntakeFile: String = "",
    val resultId: String = "",
    val providerHandoffPacketValidationReportFile: String = "",
    val providerHandoffPacketId: String = "",
    val providerHandoffPreflightId: String = "",
    val providerHandoffDecisionId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val imageResults: List<ImageCandidateExternalResultItem> = emptyList(),
    val reviewChecklistSummary: List<String> = emptyList(),
    val reviewNotes: List<String> = emptyList(),
    val downstreamBoundary: String = "image_candidate_reference_packet_candidate_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ImageCandidateReferencePacketFinding> = emptyList(),
    val evidence: ImageCandidateReferencePacketEvidence =
        ImageCandidateReferencePacketEvidence(),
)

@Serializable
data class ImageCandidateReferencePacketFinding(
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
data class ImageCandidateReferencePacketEvidence(
    val resultReviewPreflightVerdict: String = "",
    val decisionIntakeVerdict: String = "",
    val decisionAllowedDownstream: Boolean = false,
    val sourceBindingFieldCount: Int = 0,
    val imageResultCount: Int = 0,
    val checklistSummaryCount: Int = 0,
    val forbiddenRemoteOrProviderFieldCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
)

@Serializable
data class ImageCandidateReferencePacketValidationReport(
    val verdict: String,
    val referencePacketFile: String = "",
    val packetId: String = "",
    val packetStatus: String = "",
    val resultReviewPreflightFile: String = "",
    val resultReviewDecisionIntakeFile: String = "",
    val resultReviewPreflightId: String = "",
    val decisionId: String = "",
    val externalResultIntakeFile: String = "",
    val resultId: String = "",
    val providerHandoffPacketValidationReportFile: String = "",
    val providerHandoffPacketId: String = "",
    val providerHandoffPreflightId: String = "",
    val providerHandoffDecisionId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val imageResults: List<ImageCandidateExternalResultItem> = emptyList(),
    val downstreamBoundary: String = "image_candidate_reference_packet_validation_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<ImageCandidateReferencePacketValidationFinding> = emptyList(),
    val evidence: ImageCandidateReferencePacketValidationEvidence =
        ImageCandidateReferencePacketValidationEvidence(),
)

@Serializable
data class ImageCandidateReferencePacketValidationFinding(
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
data class ImageCandidateReferencePacketValidationEvidence(
    val referencePacketVerdict: String = "",
    val referencePacketNextAllowedDownstreamStep: String = "",
    val sourceFieldCount: Int = 0,
    val imageResultCount: Int = 0,
    val forbiddenRemoteOrProviderFieldCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
)

@Serializable
data class CandidateReferenceHumanAcceptanceDecision(
    val decisionId: String = "",
    val candidateReferencePacketValidationReportFile: String = "",
    val referencePacketFile: String = "",
    val packetId: String = "",
    val resultReviewPreflightId: String = "",
    val resultReviewDecisionIntakeFile: String = "",
    val resultReviewDecisionId: String = "",
    val externalResultIntakeFile: String = "",
    val resultId: String = "",
    val providerHandoffPacketValidationReportFile: String = "",
    val providerHandoffPacketId: String = "",
    val providerHandoffPreflightId: String = "",
    val providerHandoffDecisionId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val decisionSource: String = "",
    val reviewer: String = "",
    val reviewerRole: String = "",
    val decisionAt: String = "",
    val decisionStatus: String = "",
    val imageResults: List<ImageCandidateExternalResultItem> = emptyList(),
    val checklistItems: List<CandidateReferenceHumanAcceptanceChecklistItem> = emptyList(),
    val blockingIssues: List<CandidateReferenceHumanAcceptanceBlockingIssue> = emptyList(),
    val notes: String = "",
)

@Serializable
data class CandidateReferenceHumanAcceptanceChecklistItem(
    val itemId: String = "",
    val category: String = "",
    val result: String = "",
    val note: String = "",
)

@Serializable
data class CandidateReferenceHumanAcceptanceBlockingIssue(
    val issueId: String = "",
    val severity: String = "",
    val status: String = "",
    val description: String = "",
)

@Serializable
data class CandidateReferenceHumanAcceptanceDecisionIntakeReport(
    val verdict: String,
    val decisionId: String = "",
    val candidateReferencePacketValidationReportFile: String = "",
    val humanAcceptanceDecisionFile: String = "",
    val referencePacketFile: String = "",
    val packetId: String = "",
    val resultReviewPreflightId: String = "",
    val resultReviewDecisionIntakeFile: String = "",
    val resultReviewDecisionId: String = "",
    val externalResultIntakeFile: String = "",
    val resultId: String = "",
    val providerHandoffPacketValidationReportFile: String = "",
    val providerHandoffPacketId: String = "",
    val providerHandoffPreflightId: String = "",
    val providerHandoffDecisionId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val decisionStatus: String = "",
    val decisionAllowedDownstream: Boolean = false,
    val imageResults: List<ImageCandidateExternalResultItem> = emptyList(),
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<CandidateReferenceHumanAcceptanceDecisionIntakeFinding> = emptyList(),
    val evidence: CandidateReferenceHumanAcceptanceDecisionIntakeEvidence =
        CandidateReferenceHumanAcceptanceDecisionIntakeEvidence(),
)

@Serializable
data class CandidateReferenceHumanAcceptanceDecisionIntakeFinding(
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
data class CandidateReferenceHumanAcceptanceDecisionIntakeEvidence(
    val candidateReferencePacketValidationVerdict: String = "",
    val candidateReferencePacketValidationNextAllowedDownstreamStep: String = "",
    val sourceBindingFieldCount: Int = 0,
    val decisionSource: String = "",
    val decisionStatus: String = "",
    val checklistItemCount: Int = 0,
    val checklistPassedCount: Int = 0,
    val unresolvedBlockingIssueCount: Int = 0,
    val imageResultCount: Int = 0,
    val forbiddenRemoteOrProviderFieldCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
)

@Serializable
data class CandidateReferenceRegistryPreflightReport(
    val verdict: String,
    val preflightId: String = "",
    val humanAcceptanceDecisionIntakeFile: String = "",
    val candidateReferencePacketValidationReportFile: String = "",
    val decisionId: String = "",
    val referencePacketFile: String = "",
    val packetId: String = "",
    val resultReviewPreflightId: String = "",
    val resultReviewDecisionIntakeFile: String = "",
    val resultReviewDecisionId: String = "",
    val externalResultIntakeFile: String = "",
    val resultId: String = "",
    val providerHandoffPacketValidationReportFile: String = "",
    val providerHandoffPacketId: String = "",
    val providerHandoffPreflightId: String = "",
    val providerHandoffDecisionId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val imageResults: List<ImageCandidateExternalResultItem> = emptyList(),
    val downstreamBoundary: String = "candidate_reference_registry_preflight_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<CandidateReferenceRegistryPreflightFinding> = emptyList(),
    val evidence: CandidateReferenceRegistryPreflightEvidence =
        CandidateReferenceRegistryPreflightEvidence(),
)

@Serializable
data class CandidateReferenceRegistryPreflightFinding(
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
data class CandidateReferenceRegistryPreflightEvidence(
    val humanAcceptanceDecisionIntakeVerdict: String = "",
    val humanAcceptanceDecisionIntakeNextAllowedDownstreamStep: String = "",
    val candidateReferencePacketValidationVerdict: String = "",
    val candidateReferencePacketValidationNextAllowedDownstreamStep: String = "",
    val sourceBindingFieldCount: Int = 0,
    val imageResultCount: Int = 0,
    val forbiddenRemoteOrProviderFieldCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
)

@Serializable
data class CandidateReferenceRegistryPacket(
    val verdict: String,
    val packetId: String = "",
    val packetStatus: String = "candidate_reference_candidate",
    val registryPreflightFile: String = "",
    val preflightId: String = "",
    val humanAcceptanceDecisionIntakeFile: String = "",
    val decisionId: String = "",
    val candidateReferencePacketValidationReportFile: String = "",
    val referencePacketFile: String = "",
    val referencePacketId: String = "",
    val resultReviewPreflightId: String = "",
    val resultReviewDecisionIntakeFile: String = "",
    val resultReviewDecisionId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val providerHandoffPacketValidationReportFile: String = "",
    val providerHandoffPacketId: String = "",
    val providerHandoffPreflightId: String = "",
    val providerHandoffDecisionId: String = "",
    val externalResultIntakeFile: String = "",
    val resultId: String = "",
    val imageResults: List<ImageCandidateExternalResultItem> = emptyList(),
    val preparationChecklist: List<String> = emptyList(),
    val downstreamBoundary: String = "candidate_reference_registry_packet_preparation_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<CandidateReferenceRegistryPacketFinding> = emptyList(),
    val evidence: CandidateReferenceRegistryPacketEvidence =
        CandidateReferenceRegistryPacketEvidence(),
)

@Serializable
data class CandidateReferenceRegistryPacketFinding(
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
data class CandidateReferenceRegistryPacketEvidence(
    val registryPreflightVerdict: String = "",
    val registryPreflightNextAllowedDownstreamStep: String = "",
    val sourceFieldCount: Int = 0,
    val imageResultCount: Int = 0,
    val forbiddenRemoteOrProviderFieldCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
)

@Serializable
data class CandidateReferenceRegistryPacketValidationReport(
    val verdict: String,
    val registryPacketFile: String = "",
    val packetId: String = "",
    val packetStatus: String = "",
    val registryPreflightFile: String = "",
    val preflightId: String = "",
    val humanAcceptanceDecisionIntakeFile: String = "",
    val decisionId: String = "",
    val candidateReferencePacketValidationReportFile: String = "",
    val referencePacketFile: String = "",
    val referencePacketId: String = "",
    val resultReviewPreflightId: String = "",
    val resultReviewDecisionIntakeFile: String = "",
    val resultReviewDecisionId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val providerHandoffPacketValidationReportFile: String = "",
    val providerHandoffPacketId: String = "",
    val providerHandoffPreflightId: String = "",
    val providerHandoffDecisionId: String = "",
    val externalResultIntakeFile: String = "",
    val resultId: String = "",
    val imageResults: List<ImageCandidateExternalResultItem> = emptyList(),
    val downstreamBoundary: String = "candidate_reference_registry_packet_validation_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<CandidateReferenceRegistryPacketValidationFinding> = emptyList(),
    val evidence: CandidateReferenceRegistryPacketValidationEvidence =
        CandidateReferenceRegistryPacketValidationEvidence(),
)

@Serializable
data class CandidateReferenceRegistryPacketValidationFinding(
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
data class CandidateReferenceRegistryPacketValidationEvidence(
    val registryPacketVerdict: String = "",
    val registryPacketNextAllowedDownstreamStep: String = "",
    val sourceFieldCount: Int = 0,
    val imageResultCount: Int = 0,
    val forbiddenRemoteOrProviderFieldCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
)

@Serializable
data class CandidateReferenceRegistryHumanAcceptanceDecision(
    val decisionId: String = "",
    val registryPacketValidationReportFile: String = "",
    val registryPacketFile: String = "",
    val registryPacketId: String = "",
    val registryPreflightFile: String = "",
    val registryPreflightId: String = "",
    val humanAcceptanceDecisionIntakeFile: String = "",
    val priorHumanAcceptanceDecisionId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val referencePacketFile: String = "",
    val referencePacketId: String = "",
    val resultReviewPreflightId: String = "",
    val resultReviewDecisionIntakeFile: String = "",
    val resultReviewDecisionId: String = "",
    val providerHandoffPacketValidationReportFile: String = "",
    val providerHandoffPacketId: String = "",
    val providerHandoffPreflightId: String = "",
    val providerHandoffDecisionId: String = "",
    val externalResultIntakeFile: String = "",
    val resultId: String = "",
    val decisionSource: String = "",
    val reviewer: String = "",
    val reviewerRole: String = "",
    val decisionAt: String = "",
    val decisionStatus: String = "",
    val imageResults: List<ImageCandidateExternalResultItem> = emptyList(),
    val checklistItems: List<CandidateReferenceRegistryHumanAcceptanceChecklistItem> = emptyList(),
    val blockingIssues: List<CandidateReferenceRegistryHumanAcceptanceBlockingIssue> = emptyList(),
    val notes: String = "",
)

@Serializable
data class CandidateReferenceRegistryHumanAcceptanceChecklistItem(
    val itemId: String = "",
    val category: String = "",
    val result: String = "",
    val note: String = "",
)

@Serializable
data class CandidateReferenceRegistryHumanAcceptanceBlockingIssue(
    val issueId: String = "",
    val severity: String = "",
    val status: String = "",
    val description: String = "",
)

@Serializable
data class CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport(
    val verdict: String,
    val decisionId: String = "",
    val registryPacketValidationReportFile: String = "",
    val humanAcceptanceDecisionFile: String = "",
    val registryPacketFile: String = "",
    val registryPacketId: String = "",
    val registryPreflightFile: String = "",
    val registryPreflightId: String = "",
    val humanAcceptanceDecisionIntakeFile: String = "",
    val priorHumanAcceptanceDecisionId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val referencePacketFile: String = "",
    val referencePacketId: String = "",
    val resultReviewPreflightId: String = "",
    val resultReviewDecisionIntakeFile: String = "",
    val resultReviewDecisionId: String = "",
    val providerHandoffPacketValidationReportFile: String = "",
    val providerHandoffPacketId: String = "",
    val providerHandoffPreflightId: String = "",
    val providerHandoffDecisionId: String = "",
    val externalResultIntakeFile: String = "",
    val resultId: String = "",
    val decisionStatus: String = "",
    val decisionAllowedDownstream: Boolean = false,
    val imageResults: List<ImageCandidateExternalResultItem> = emptyList(),
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<CandidateReferenceRegistryHumanAcceptanceDecisionIntakeFinding> = emptyList(),
    val evidence: CandidateReferenceRegistryHumanAcceptanceDecisionIntakeEvidence =
        CandidateReferenceRegistryHumanAcceptanceDecisionIntakeEvidence(),
)

@Serializable
data class CandidateReferenceRegistryHumanAcceptanceDecisionIntakeFinding(
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
data class CandidateReferenceRegistryHumanAcceptanceDecisionIntakeEvidence(
    val registryPacketValidationVerdict: String = "",
    val registryPacketValidationNextAllowedDownstreamStep: String = "",
    val sourceBindingFieldCount: Int = 0,
    val decisionSource: String = "",
    val decisionStatus: String = "",
    val checklistItemCount: Int = 0,
    val checklistPassedCount: Int = 0,
    val unresolvedBlockingIssueCount: Int = 0,
    val imageResultCount: Int = 0,
    val forbiddenRemoteOrProviderFieldCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
)

@Serializable
data class CandidateReferenceAcceptancePacket(
    val verdict: String,
    val packetId: String = "",
    val packetStatus: String = "candidate_reference_acceptance_candidate",
    val registryPacketValidationReportFile: String = "",
    val humanAcceptanceDecisionIntakeFile: String = "",
    val decisionId: String = "",
    val humanAcceptanceDecisionSource: String = "",
    val registryPacketFile: String = "",
    val registryPacketId: String = "",
    val registryPreflightFile: String = "",
    val registryPreflightId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val referencePacketFile: String = "",
    val referencePacketId: String = "",
    val resultReviewPreflightId: String = "",
    val resultReviewDecisionIntakeFile: String = "",
    val resultReviewDecisionId: String = "",
    val providerHandoffPacketValidationReportFile: String = "",
    val providerHandoffPacketId: String = "",
    val providerHandoffPreflightId: String = "",
    val providerHandoffDecisionId: String = "",
    val externalResultIntakeFile: String = "",
    val resultId: String = "",
    val sourceImageResultIdentityDigest: String = "",
    val imageResults: List<ImageCandidateExternalResultItem> = emptyList(),
    val boundaryChecklist: List<String> = emptyList(),
    val downstreamBoundary: String = "candidate_reference_acceptance_packet_boundary_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<CandidateReferenceAcceptancePacketFinding> = emptyList(),
    val evidence: CandidateReferenceAcceptancePacketEvidence =
        CandidateReferenceAcceptancePacketEvidence(),
)

@Serializable
data class CandidateReferenceAcceptancePacketFinding(
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
data class CandidateReferenceAcceptancePacketEvidence(
    val registryPacketValidationVerdict: String = "",
    val decisionIntakeVerdict: String = "",
    val decisionAllowedDownstream: Boolean = false,
    val humanAcceptanceDecisionSource: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val imageResultIdentityDigest: String = "",
    val sourceFieldCount: Int = 0,
    val imageResultCount: Int = 0,
    val forbiddenRemoteOrProviderFieldCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
)

@Serializable
data class CandidateReferenceAcceptancePacketValidationReport(
    val verdict: String,
    val validationId: String = "",
    val acceptancePacketFile: String = "",
    val packetId: String = "",
    val packetStatus: String = "",
    val registryPacketValidationReportFile: String = "",
    val humanAcceptanceDecisionIntakeFile: String = "",
    val decisionId: String = "",
    val humanAcceptanceDecisionSource: String = "",
    val registryPacketFile: String = "",
    val registryPacketId: String = "",
    val registryPreflightFile: String = "",
    val registryPreflightId: String = "",
    val acceptedScriptPacketId: String = "",
    val scriptPacketDraftFile: String = "",
    val scriptPacketDraftContentSha256: String = "",
    val referencePacketFile: String = "",
    val referencePacketId: String = "",
    val resultReviewPreflightId: String = "",
    val resultReviewDecisionIntakeFile: String = "",
    val resultReviewDecisionId: String = "",
    val providerHandoffPacketValidationReportFile: String = "",
    val providerHandoffPacketId: String = "",
    val providerHandoffPreflightId: String = "",
    val providerHandoffDecisionId: String = "",
    val externalResultIntakeFile: String = "",
    val resultId: String = "",
    val sourceImageResultIdentityDigest: String = "",
    val imageResults: List<ImageCandidateExternalResultItem> = emptyList(),
    val downstreamBoundary: String = "candidate_reference_acceptance_packet_validation_only",
    val nextAllowedDownstreamStep: String = "",
    val blockedReason: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<CandidateReferenceAcceptancePacketValidationFinding> = emptyList(),
    val evidence: CandidateReferenceAcceptancePacketValidationEvidence =
        CandidateReferenceAcceptancePacketValidationEvidence(),
)

@Serializable
data class CandidateReferenceAcceptancePacketValidationFinding(
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
data class CandidateReferenceAcceptancePacketValidationEvidence(
    val acceptancePacketVerdict: String = "",
    val acceptancePacketNextAllowedDownstreamStep: String = "",
    val registryPacketValidationVerdict: String = "",
    val decisionIntakeVerdict: String = "",
    val decisionAllowedDownstream: Boolean = false,
    val humanAcceptanceDecisionSource: String = "",
    val sourceFieldCount: Int = 0,
    val imageResultCount: Int = 0,
    val forbiddenRemoteOrProviderFieldCount: Int = 0,
    val forbiddenClaimCount: Int = 0,
    val forbiddenStatusCount: Int = 0,
)

@Serializable
data class CharacterNuwaGateFile(
    val gateId: String = "",
    val projectId: String = "",
    val sourceChapter: String = "",
    val requiredSpeakingCharacters: List<CharacterNuwaRequiredSpeaker> = emptyList(),
    val characterCards: List<CharacterNuwaCard> = emptyList(),
    val bannedCalibrationStructures: List<String> = emptyList(),
    val reviewerNotes: String = "",
)

@Serializable
data class CharacterNuwaRequiredSpeaker(
    val characterId: String = "",
    val displayName: String = "",
    val requiredDepth: String = "full_nuwa",
    val reason: String = "",
)

@Serializable
data class CharacterNuwaCard(
    val cardId: String = "",
    val characterId: String = "",
    val displayName: String = "",
    val cardDepth: String = "full_nuwa",
    val gateStatus: String = "draft",
    val narrativeFunction: String = "",
    val socialPosition: String = "",
    val currentPressure: String = "",
    val relationshipToProtagonist: String = "",
    val knowledgeBoundary: List<String> = emptyList(),
    val wantsFromScene: String = "",
    val fearsInScene: String = "",
    val hiddenMotiveIfAny: String = "",
    val speechRegister: String = "",
    val sentenceLengthBias: String = "",
    val directness: String = "",
    val silencePattern: String = "",
    val pressureResponse: String = "",
    val allowedTells: List<String> = emptyList(),
    val forbiddenTells: List<String> = emptyList(),
    val favoriteMoves: List<String> = emptyList(),
    val neverDo: List<String> = emptyList(),
    val sampleLines: CharacterNuwaSampleLines = CharacterNuwaSampleLines(),
    val bannedStructures: List<String> = emptyList(),
)

@Serializable
data class CharacterNuwaSampleLines(
    val accepted: List<String> = emptyList(),
    val rejected: List<String> = emptyList(),
)

@Serializable
data class CharacterNuwaGateValidationReport(
    val verdict: String,
    val gateFile: String = "",
    val gateId: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val requiredSpeakingCharacterCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val cardCount: Int = 0,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<CharacterNuwaGateFinding> = emptyList(),
    val evidence: CharacterNuwaGateEvidence = CharacterNuwaGateEvidence(),
)

@Serializable
data class CharacterNuwaGateFinding(
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
data class CharacterNuwaGateEvidence(
    val requiredFullCardCount: Int = 0,
    val requiredLightCardCount: Int = 0,
    val readyCardCount: Int = 0,
    val bannedCalibrationHitCount: Int = 0,
)

@Serializable
data class NovelizationPreflightReport(
    val verdict: String,
    val preflightId: String = "",
    val characterNuwaReportFile: String = "",
    val characterNuwaGateFile: String = "",
    val characterNuwaGateId: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<NovelizationPreflightFinding> = emptyList(),
    val evidence: NovelizationPreflightEvidence = NovelizationPreflightEvidence(),
)

@Serializable
data class NovelizationPreflightFinding(
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
data class NovelizationPreflightEvidence(
    val characterNuwaVerdict: String = "",
    val requiredSpeakingCharacterCount: Int = 0,
    val characterCardCount: Int = 0,
    val readyCharacterCardCount: Int = 0,
    val upstreamFindingCount: Int = 0,
    val bannedCalibrationHitCount: Int = 0,
)

@Serializable
data class NovelizationWriterBrief(
    val briefId: String = "",
    val projectId: String = "",
    val targetChapter: String = "",
    val sourcePreflightReport: String = "",
    val previousChapterRefs: List<String> = emptyList(),
    val acceptedChapterFacts: List<String> = emptyList(),
    val characterStates: List<String> = emptyList(),
    val openThreads: List<String> = emptyList(),
    val mustAdvance: List<String> = emptyList(),
    val mustNotRepeat: List<String> = emptyList(),
    val allowedScenes: List<String> = emptyList(),
    val knownUnknowns: List<String> = emptyList(),
    val tonePacingTarget: String = "",
    val styleContract: String = "",
    val characterNuwaReferences: List<String> = emptyList(),
    val antiAiProseConstraints: List<String> = emptyList(),
    val authorOnlyExcluded: Boolean = false,
    val reviewerNotes: String = "",
)

@Serializable
data class NovelizationWriterBriefValidationReport(
    val verdict: String,
    val briefFile: String = "",
    val sourcePreflightReportFile: String = "",
    val briefId: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<NovelizationWriterBriefFinding> = emptyList(),
    val evidence: NovelizationWriterBriefEvidence = NovelizationWriterBriefEvidence(),
)

@Serializable
data class NovelizationWriterBriefFinding(
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
data class NovelizationWriterBriefEvidence(
    val sourcePreflightVerdict: String = "",
    val previousChapterRefCount: Int = 0,
    val acceptedFactCount: Int = 0,
    val characterStateCount: Int = 0,
    val openThreadCount: Int = 0,
    val mustAdvanceCount: Int = 0,
    val mustNotRepeatCount: Int = 0,
    val characterNuwaReferenceCount: Int = 0,
    val antiAiConstraintCount: Int = 0,
)

@Serializable
data class NovelizationDraftIntakeReport(
    val verdict: String,
    val draftFile: String = "",
    val writerBriefReportFile: String = "",
    val sourceType: String = "",
    val sourceLabel: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<NovelizationDraftIntakeFinding> = emptyList(),
    val evidence: NovelizationDraftIntakeEvidence = NovelizationDraftIntakeEvidence(),
)

@Serializable
data class NovelizationDraftIntakeFinding(
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
data class NovelizationDraftIntakeEvidence(
    val writerBriefVerdict: String = "",
    val draftCharacters: Int = 0,
    val draftLines: Int = 0,
    val replacementCharacterCount: Int = 0,
)

@Serializable
data class NovelizationProseQualityAuditReport(
    val verdict: String,
    val draftFile: String = "",
    val draftIntakeReportFile: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<NovelizationProseQualityFinding> = emptyList(),
    val evidence: NovelizationProseQualityEvidence = NovelizationProseQualityEvidence(),
)

@Serializable
data class NovelizationProseQualityFinding(
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
data class NovelizationProseQualityEvidence(
    val draftIntakeVerdict: String = "",
    val draftCharacters: Int = 0,
    val draftLines: Int = 0,
    val dialogueLineCount: Int = 0,
    val sentenceCount: Int = 0,
    val bannedCalibrationHitCount: Int = 0,
    val genericAbstractionHitCount: Int = 0,
    val expositionDialogueHitCount: Int = 0,
    val replacementCharacterCount: Int = 0,
)

@Serializable
data class NovelizationRevisionContinuityAnchors(
    val anchorFileId: String = "",
    val anchors: List<String> = emptyList(),
    val doNotChange: List<String> = emptyList(),
)

@Serializable
data class NovelizationRevisionBriefReport(
    val verdict: String,
    val briefId: String = "",
    val draftFile: String = "",
    val draftIntakeReportFile: String = "",
    val proseQualityAuditReportFile: String = "",
    val continuityAnchorsFile: String = "",
    val revisionRequired: Boolean = false,
    val mandatoryChecklist: List<String> = emptyList(),
    val requiredFixes: List<NovelizationRevisionItem> = emptyList(),
    val continuityAnchors: List<String> = emptyList(),
    val doNotChange: List<String> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<NovelizationRevisionBriefFinding> = emptyList(),
    val evidence: NovelizationRevisionBriefEvidence = NovelizationRevisionBriefEvidence(),
)

@Serializable
data class NovelizationRevisionItem(
    val itemId: String = "",
    val fixType: String = "",
    val sourceRuleId: String = "",
    val category: String = "",
    val severity: String = "",
    val instruction: String = "",
    val evidence: List<String> = emptyList(),
    val suggestedFix: String = "",
)

@Serializable
data class NovelizationRevisionBriefFinding(
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
data class NovelizationRevisionBriefEvidence(
    val draftIntakeVerdict: String = "",
    val proseQualityVerdict: String = "",
    val draftCharacters: Int = 0,
    val proseQualityFindingCount: Int = 0,
    val requiredFixCount: Int = 0,
    val continuityAnchorCount: Int = 0,
    val doNotChangeCount: Int = 0,
)

@Serializable
data class NovelizationRevisionHandlingDeclaration(
    val declarationId: String = "",
    val revisionBriefId: String = "",
    val handledChecklist: List<NovelizationRevisionChecklistHandling> = emptyList(),
    val handledFixes: List<NovelizationRevisionFixHandling> = emptyList(),
)

@Serializable
data class NovelizationRevisionChecklistHandling(
    val checklistItem: String = "",
    val status: String = "",
    val note: String = "",
)

@Serializable
data class NovelizationRevisionFixHandling(
    val itemId: String = "",
    val status: String = "",
    val note: String = "",
)

@Serializable
data class NovelizationRevisedDraftIntakeReport(
    val verdict: String,
    val originalDraftFile: String = "",
    val revisedDraftFile: String = "",
    val revisionBriefReportFile: String = "",
    val declaredHandlingFile: String = "",
    val revisionBriefId: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<NovelizationRevisedDraftIntakeFinding> = emptyList(),
    val evidence: NovelizationRevisedDraftIntakeEvidence = NovelizationRevisedDraftIntakeEvidence(),
)

@Serializable
data class NovelizationRevisedDraftIntakeFinding(
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
data class NovelizationRevisedDraftIntakeEvidence(
    val revisionBriefVerdict: String = "",
    val revisionRequired: Boolean = false,
    val originalCharacters: Int = 0,
    val revisedCharacters: Int = 0,
    val unchangedAfterNormalization: Boolean = false,
    val mandatoryChecklistCount: Int = 0,
    val requiredFixCount: Int = 0,
    val declaredChecklistCount: Int = 0,
    val declaredFixCount: Int = 0,
    val bannedResidualCount: Int = 0,
    val continuityAnchorCount: Int = 0,
    val continuityAnchorPreservedCount: Int = 0,
)

@Serializable
data class NovelizationAcceptedDraftPacket(
    val verdict: String,
    val packetId: String = "",
    val revisedDraftFile: String = "",
    val revisedDraftContentSha256: String = "",
    val sourceReports: NovelizationAcceptedDraftSourceReports = NovelizationAcceptedDraftSourceReports(),
    val requiredFixesHandledSummary: List<NovelizationAcceptedDraftHandledFix> = emptyList(),
    val protectedContinuityAnchors: List<String> = emptyList(),
    val doNotChange: List<String> = emptyList(),
    val remainingResidualRisks: List<String> = emptyList(),
    val nextAllowedDownstreamStep: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<NovelizationAcceptedDraftPacketFinding> = emptyList(),
    val evidence: NovelizationAcceptedDraftPacketEvidence = NovelizationAcceptedDraftPacketEvidence(),
)

@Serializable
data class NovelizationAcceptedDraftSourceReports(
    val revisionBriefReportFile: String = "",
    val revisionBriefVerdict: String = "",
    val revisedDraftIntakeReportFile: String = "",
    val revisedDraftIntakeVerdict: String = "",
    val continuityAnchorsFile: String = "",
    val continuityAnchorFileId: String = "",
)

@Serializable
data class NovelizationAcceptedDraftHandledFix(
    val itemId: String = "",
    val fixType: String = "",
    val sourceRuleId: String = "",
    val severity: String = "",
    val status: String = "accepted_by_revised_draft_intake",
)

@Serializable
data class NovelizationAcceptedDraftPacketFinding(
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
data class NovelizationAcceptedDraftPacketEvidence(
    val revisedDraftCharacters: Int = 0,
    val requiredFixCount: Int = 0,
    val protectedContinuityAnchorCount: Int = 0,
    val doNotChangeCount: Int = 0,
    val revisedDraftIntakeFindingCount: Int = 0,
    val revisionBriefFindingCount: Int = 0,
    val residualRiskCount: Int = 0,
)

@Serializable
data class NovelizationScriptPacketPreflightReport(
    val verdict: String,
    val preflightId: String = "",
    val acceptedDraftPacketFile: String = "",
    val acceptedDraftPacketId: String = "",
    val nextAllowedDownstreamStep: String = "",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val findingCount: Int = 0,
    val findings: List<NovelizationScriptPacketPreflightFinding> = emptyList(),
    val evidence: NovelizationScriptPacketPreflightEvidence = NovelizationScriptPacketPreflightEvidence(),
)

@Serializable
data class NovelizationScriptPacketPreflightFinding(
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
data class NovelizationScriptPacketPreflightEvidence(
    val packetVerdict: String = "",
    val revisedDraftContentHashPresent: Boolean = false,
    val sourceReportFileCount: Int = 0,
    val sourceReportVerdictCount: Int = 0,
    val protectedContinuityAnchorCount: Int = 0,
    val requiredFixHandledCount: Int = 0,
    val residualRiskCount: Int = 0,
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
