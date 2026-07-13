package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.CharacterProfile
import com.localnovel.engine.model.CharacterState
import com.localnovel.engine.model.DemoProjectSeed
import com.localnovel.engine.model.Fact
import com.localnovel.engine.model.FormalTurnPhysicalValidationContext
import com.localnovel.engine.model.Item
import com.localnovel.engine.model.Location
import com.localnovel.engine.model.PhysicalActionProposal
import com.localnovel.engine.model.PhysicalActionType
import com.localnovel.engine.model.PhysicalArchetype
import com.localnovel.engine.model.PhysicalDimensionsCm
import com.localnovel.engine.model.PhysicalEntityInstance
import com.localnovel.engine.model.PhysicalSupportSurface
import com.localnovel.engine.model.PhysicalSupportUse
import com.localnovel.engine.model.PhysicalVector3Cm
import com.localnovel.engine.model.PhysicalWorldSnapshot
import com.localnovel.engine.model.Project
import com.localnovel.engine.model.Scene
import com.localnovel.engine.model.SceneContract
import com.localnovel.engine.model.WorldPhysics05EntityEvidence
import com.localnovel.engine.model.WorldPhysics05PilotChecks
import com.localnovel.engine.model.WorldPhysics05PilotReport
import com.localnovel.engine.model.WorldPhysics05SourceEvidence
import com.localnovel.engine.model.WorldPhysics05TurnEvidence
import com.localnovel.engine.model.WorldRule
import com.localnovel.engine.repository.SqliteStoryRepository
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class WorldPhysics05EmbodiedScenePilotService {
    fun run(repositoryRoot: Path): WorldPhysics05PilotReport {
        val sourceFiles = sourcePaths.map { relative -> sourceEvidence(repositoryRoot, relative) }
        val source = verifyScreen01Source(repositoryRoot)
        val repository = SqliteStoryRepository(
            Files.createTempDirectory("world-physics05-pilot-").resolve("story.db"),
        )
        repository.initialize()
        repository.seedDemo(seed())
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession(PROJECT_ID, SCENE_ID)
        val archetypes = archetypes()
        val initialSnapshot = initialSnapshot()
        val turns = mutableListOf<WorldPhysics05TurnEvidence>()

        val firstContext = runtime.prepareTurn(session.id, "起身前先找安全支點，不把重量壓在愛醬身上。")
        val firstBeforeSession = requireNotNull(repository.getSession(session.id))
        val invalidAction = PhysicalActionProposal(
            actionId = "wp05-turn1-dog-shoulder-rejected",
            actionType = PhysicalActionType.USE_AS_SUPPORT,
            actorId = ACTOR_ID,
            targetId = DOG_ID,
            supportSurfaceId = "shoulder",
            supportUse = PhysicalSupportUse.STAND_UP_ASSIST,
        )
        val invalidResult = result(firstContext, "wp05-turn1-invalid", listOf(invalidAction), "陸塵行沒有把重量壓到愛醬身上。")
        val invalidPhysicalContext = physicalContext(firstContext, invalidResult, archetypes, initialSnapshot, null)
        val invalidCodes = runtime.validate(firstContext, invalidResult, invalidPhysicalContext).errors.map { it.code }.distinct().sorted()
        require(FormalTurnPhysicalGateValidator.PHYSICAL_AFFORDANCE_REJECTED in invalidCodes) {
            "The dog-support regression was not rejected: $invalidCodes"
        }
        val rejectedStateUnchanged = repository.getSession(session.id) == firstBeforeSession &&
            repository.getFormalPhysicalState(session.id) == null &&
            repository.formalPhysicalTransitionCount(session.id) == 0 &&
            repository.getPendingContext(session.id, firstContext.contextId) != null

        val safeSupport = PhysicalActionProposal(
            actionId = "wp05-turn1-stable-rock-support",
            actionType = PhysicalActionType.USE_AS_SUPPORT,
            actorId = ACTOR_ID,
            targetId = ROCK_ID,
            supportSurfaceId = "fulcrum",
            supportUse = PhysicalSupportUse.STAND_UP_ASSIST,
        )
        val repairedResult = result(firstContext, "wp05-turn1-repaired", listOf(safeSupport), "陸塵行以穩定岩面借力起身，愛醬留在高台警戒。")
        runtime.resolveTurn(
            session.id,
            repairedResult,
            ResolveTurnOptions(formalTurnPhysicalContext = physicalContext(firstContext, repairedResult, archetypes, initialSnapshot, null)),
        )
        val firstAfterSession = requireNotNull(repository.getSession(session.id))
        turns += turnEvidence(
            turnNumber = 1,
            playerInput = firstContext.playerInput,
            rejectedActionId = invalidAction.actionId,
            rejectionCodes = invalidCodes,
            resolvedActions = listOf(safeSupport),
            storyBefore = firstBeforeSession.stateVersion,
            storyAfter = firstAfterSession.stateVersion,
            physicalBefore = 0,
            physicalAfter = 0,
            transitionsBefore = 0,
            transitionsAfter = 0,
            snapshot = initialSnapshot,
        )

        turns += resolveStatefulTurn(
            runtime = runtime,
            repository = repository,
            sessionId = session.id,
            archetypes = archetypes,
            initialSnapshot = initialSnapshot,
            playerInput = "拿起身旁的短撬桿，準備處理受困者周圍的落石。",
            resultId = "wp05-turn2-lift",
            narrative = "陸塵行確認距離後拿起短撬桿。",
            actions = listOf(
                PhysicalActionProposal("wp05-turn2-lift-pry-bar", PhysicalActionType.LIFT, ACTOR_ID, TOOL_ID),
            ),
        )
        turns += resolveStatefulTurn(
            runtime = runtime,
            repository = repository,
            sessionId = session.id,
            archetypes = archetypes,
            initialSnapshot = initialSnapshot,
            playerInput = "握著短撬桿穿過凹壁入口，移到較安全的內側位置。",
            resultId = "wp05-turn3-pass",
            narrative = "陸塵行帶著短撬桿穿過凹壁入口。",
            actions = listOf(
                PhysicalActionProposal(
                    "wp05-turn3-pass-with-tool",
                    PhysicalActionType.PASS_THROUGH,
                    ACTOR_ID,
                    OPENING_ID,
                    destinationCenterCm = SAFE_INSIDE_POSITION,
                ),
            ),
        )
        turns += resolveStatefulTurn(
            runtime = runtime,
            repository = repository,
            sessionId = session.id,
            archetypes = archetypes,
            initialSnapshot = initialSnapshot,
            playerInput = "把短撬桿放到已確認穩定的岩面撬點。",
            resultId = "wp05-turn4-place",
            narrative = "陸塵行把短撬桿放上穩定岩面，沒有碰動金屬箱。",
            actions = listOf(
                PhysicalActionProposal(
                    "wp05-turn4-place-pry-bar",
                    PhysicalActionType.PLACE_ON,
                    ACTOR_ID,
                    TOOL_ID,
                    secondaryTargetId = ROCK_ID,
                    supportSurfaceId = "fulcrum",
                ),
            ),
        )
        turns += resolveStatefulTurn(
            runtime = runtime,
            repository = repository,
            sessionId = session.id,
            archetypes = archetypes,
            initialSnapshot = initialSnapshot,
            playerInput = "救援停止後重新拿起短撬桿，再放到安全地面。",
            resultId = "wp05-turn5-release",
            narrative = "陸塵行收回短撬桿，確認腳邊安全後放下。",
            actions = listOf(
                PhysicalActionProposal("wp05-turn5-lift-pry-bar", PhysicalActionType.LIFT, ACTOR_ID, TOOL_ID),
                PhysicalActionProposal("wp05-turn5-drop-pry-bar", PhysicalActionType.DROP, ACTOR_ID, TOOL_ID),
            ),
        )

        val finalSession = requireNotNull(repository.getSession(session.id))
        val finalState = requireNotNull(repository.getFormalPhysicalState(session.id))
        val third = turns.single { it.turnNumber == 3 }
        val fourth = turns.single { it.turnNumber == 4 }
        val fifth = turns.single { it.turnNumber == 5 }
        val checks = WorldPhysics05PilotChecks(
            sourceHashesVerified = sourceFiles.size == sourcePaths.size,
            dogSupportRejected = FormalTurnPhysicalGateValidator.PHYSICAL_AFFORDANCE_REJECTED in invalidCodes,
            rejectedAttemptStateUnchanged = rejectedStateUnchanged,
            samePendingTurnRepairApplied = firstAfterSession.nextTurnNumber == 2 &&
                repository.getPendingContext(session.id, firstContext.contextId) == null,
            heldToolFollowedHolder = third.toolAfter.heldByEntityId == ACTOR_ID &&
                third.toolAfter.centerCm == third.actorAfter.centerCm,
            placedToolRelationPersisted = fourth.toolAfter.supportedByEntityId == ROCK_ID &&
                fourth.toolAfter.supportedBySurfaceId == "fulcrum",
            droppedToolRelationsCleared = fifth.toolAfter.heldByEntityId == null &&
                fifth.toolAfter.supportedByEntityId == null &&
                fifth.toolAfter.containedByEntityId == null &&
                fifth.toolAfter.centerCm.z == TOOL_HEIGHT_CM / 2.0,
            formalRuntimeTurnCountVerified = turns.size == 5 &&
                finalSession.nextTurnNumber == 6 &&
                finalSession.stateVersion == 5 &&
                finalState.physicalStateVersion == 5 &&
                repository.formalPhysicalTransitionCount(session.id) == 5,
        )
        require(checks.asList().all { it }) { "WORLD-PHYSICS-05 checks did not all pass: $checks" }

        return WorldPhysics05PilotReport(
            storyTitle = source.storyTitle,
            sceneId = source.sceneId,
            locationId = source.locationId,
            sourceFiles = sourceFiles,
            resolvedTurnCount = turns.size,
            rejectedAttemptCount = 1,
            physicalTransitionCount = repository.formalPhysicalTransitionCount(session.id),
            turns = turns,
            checks = checks,
            technicalDecision = "WORLD_PHYSICS05_EMBODIED_SCENE_PILOT_PASS",
            claimBoundary = listOf(
                "One fixed non-Canon SCREEN-01 scene trajectory only.",
                "No free-text action extraction or complete real-world simulation is proven.",
                "All metric overlay values remain NON_CANON_EVALUATION.",
                "No live model, Provider, media generation or Canon promotion was used.",
            ),
        )
    }

    private fun resolveStatefulTurn(
        runtime: MultiTurnRuntime,
        repository: SqliteStoryRepository,
        sessionId: String,
        archetypes: List<PhysicalArchetype>,
        initialSnapshot: PhysicalWorldSnapshot,
        playerInput: String,
        resultId: String,
        narrative: String,
        actions: List<PhysicalActionProposal>,
    ): WorldPhysics05TurnEvidence {
        val context = runtime.prepareTurn(sessionId, playerInput)
        val beforeSession = requireNotNull(repository.getSession(sessionId))
        val beforeState = repository.getFormalPhysicalState(sessionId)
        val beforePhysicalVersion = beforeState?.physicalStateVersion ?: 0
        val beforeTransitionCount = repository.formalPhysicalTransitionCount(sessionId)
        val snapshot = beforeState?.physicalSnapshot ?: initialSnapshot
        val turnResult = result(context, resultId, actions, narrative)
        val boundContext = physicalContext(context, turnResult, archetypes, snapshot, beforePhysicalVersion)
        val validation = runtime.validate(context, turnResult, boundContext)
        require(validation.isValid) { validation.errors.joinToString { "${it.code}:${it.message}" } }
        runtime.resolveTurn(sessionId, turnResult, ResolveTurnOptions(formalTurnPhysicalContext = boundContext))
        val afterSession = requireNotNull(repository.getSession(sessionId))
        val afterState = requireNotNull(repository.getFormalPhysicalState(sessionId))
        return turnEvidence(
            turnNumber = context.turnNumber,
            playerInput = playerInput,
            resolvedActions = actions,
            storyBefore = beforeSession.stateVersion,
            storyAfter = afterSession.stateVersion,
            physicalBefore = beforePhysicalVersion,
            physicalAfter = afterState.physicalStateVersion,
            transitionsBefore = beforeTransitionCount,
            transitionsAfter = repository.formalPhysicalTransitionCount(sessionId),
            snapshot = afterState.physicalSnapshot,
        )
    }

    private fun turnEvidence(
        turnNumber: Int,
        playerInput: String,
        rejectedActionId: String? = null,
        rejectionCodes: List<String> = emptyList(),
        resolvedActions: List<PhysicalActionProposal>,
        storyBefore: Int,
        storyAfter: Int,
        physicalBefore: Int,
        physicalAfter: Int,
        transitionsBefore: Int,
        transitionsAfter: Int,
        snapshot: PhysicalWorldSnapshot,
    ): WorldPhysics05TurnEvidence = WorldPhysics05TurnEvidence(
        turnNumber = turnNumber,
        playerInput = playerInput,
        rejectedActionId = rejectedActionId,
        rejectionCodes = rejectionCodes,
        resolvedActionIds = resolvedActions.map { it.actionId },
        resolvedActionTypes = resolvedActions.map { it.actionType },
        storyStateVersionBefore = storyBefore,
        storyStateVersionAfter = storyAfter,
        physicalStateVersionBefore = physicalBefore,
        physicalStateVersionAfter = physicalAfter,
        transitionCountBefore = transitionsBefore,
        transitionCountAfter = transitionsAfter,
        actorAfter = snapshot.entity(ACTOR_ID).evidence(),
        toolAfter = snapshot.entity(TOOL_ID).evidence(),
    )

    private fun result(
        context: com.localnovel.engine.model.ContextPacket,
        resultId: String,
        actions: List<PhysicalActionProposal>,
        narrative: String,
    ): AiTurnResult = AiTurnResult(
        protocolVersion = "2",
        projectId = context.project.id,
        turnId = context.turnId,
        resultId = resultId,
        contextId = context.contextId,
        sessionId = context.sessionId,
        turnNumber = context.turnNumber,
        baseStateVersion = context.baseStateVersion,
        contextHash = context.contextHash,
        narrative = narrative,
        turnSummary = "SCREEN-01 scene-03 bounded physical turn ${context.turnNumber}.",
        characterActions = listOf(
            CharacterAction(
                characterId = ACTOR_ID,
                intent = "在凹壁救援中先確認支點、距離與工具狀態。",
                visibleAction = narrative,
                hiddenMotive = "保護愛醬與受困者，不讓證物優先於生命安全。",
                characterFitScore = 94,
                storyProgressScore = 78,
            ),
        ),
        physicalActionProposals = actions,
    )

    private fun physicalContext(
        context: com.localnovel.engine.model.ContextPacket,
        result: AiTurnResult,
        archetypes: List<PhysicalArchetype>,
        snapshot: PhysicalWorldSnapshot,
        physicalStateVersion: Int?,
    ): FormalTurnPhysicalValidationContext {
        val isV2 = physicalStateVersion != null
        return FormalTurnPhysicalValidationContext(
            schemaVersion = if (isV2) PhysicalStateReducer.SCHEMA_VERSION else FormalTurnPhysicalGateValidator.SCHEMA_VERSION,
            projectId = context.project.id,
            sessionId = context.sessionId,
            turnId = context.turnId,
            contextId = context.contextId,
            contextHash = context.contextHash,
            turnNumber = context.turnNumber,
            baseStateVersion = context.baseStateVersion,
            resultId = result.resultId,
            resultCanonicalSha256 = FormalTurnPhysicalGateValidator().canonicalResultSha256(result),
            physicalStateVersion = physicalStateVersion,
            physicalSnapshotCanonicalSha256 = if (isV2) FormalPhysicalStateHash.snapshot(snapshot) else null,
            physicalArchetypeRegistryCanonicalSha256 = if (isV2) FormalPhysicalStateHash.archetypeRegistry(archetypes) else null,
            physicalArchetypes = archetypes,
            physicalSnapshot = snapshot,
            allowedPhysicalActionTypes = result.physicalActionProposals.map { it.actionType }.distinct(),
            allowedPhysicalActorIds = result.physicalActionProposals.map { it.actorId }.distinct(),
            allowedPhysicalTargetIds = result.physicalActionProposals.flatMap {
                listOfNotNull(it.targetId, it.secondaryTargetId)
            }.distinct(),
        )
    }

    private fun seed(): DemoProjectSeed = DemoProjectSeed(
        project = Project(PROJECT_ID, "潮線之外物理試演", "SCREEN-01 scene-03 non-Canon physical continuity pilot.", SCENE_ID),
        characters = listOf(
            CharacterProfile(ACTOR_ID, PROJECT_ID, "陸塵行", "player", "現場判斷與救援。", true),
            CharacterProfile(DOG_ID, PROJECT_ID, "愛醬", "companion", "主動偵察、危險預警與情緒支點。"),
            CharacterProfile("character-zhou-ke", PROJECT_ID, "周克", "rescuer", "撤退與繩索協助者。"),
            CharacterProfile("character-tide-picker", PROJECT_ID, "拾潮者", "injured", "受困傷者。"),
        ),
        characterStates = listOf(
            CharacterState(ACTOR_ID, PROJECT_ID, LOCATION_ID, "alert", "steady"),
            CharacterState(DOG_ID, PROJECT_ID, LOCATION_ID, "watchful", "steady"),
            CharacterState("character-zhou-ke", PROJECT_ID, LOCATION_ID, "focused", "steady"),
            CharacterState("character-tide-picker", PROJECT_ID, LOCATION_ID, "guarded", "injured"),
        ),
        worldRules = listOf(
            WorldRule("wp05-rule-safety", PROJECT_ID, "救援安全", "物理動作必須先通過引擎驗證；生命安全優先。", 10),
        ),
        locations = listOf(Location(LOCATION_ID, PROJECT_ID, "大坪海岸低處凹壁", "SCREEN-01 scene-03 非 Canon 試演場景。")),
        items = listOf(Item(TOOL_ID, PROJECT_ID, "短撬桿", "救援工具。", locationId = LOCATION_ID, tags = listOf("rescue-tool"))),
        scenes = listOf(Scene(SCENE_ID, PROJECT_ID, "凹壁救援", "保護愛醬並救出受困者。", LOCATION_ID)),
        sceneContracts = listOf(
            SceneContract(
                "wp05-scene03-contract",
                PROJECT_ID,
                SCENE_ID,
                forbiddenReveals = listOf("箱內內容", "第二名行動者身分", "新世界力量答案"),
            ),
        ),
        facts = listOf(Fact("wp05-fact-rescue", PROJECT_ID, "陸塵行、愛醬、周克與拾潮者位於凹壁救援現場。")),
    )

    private fun archetypes(): List<PhysicalArchetype> {
        val base = PhysicalAffordanceFixtureFactory.createSuite().archetypes.associateBy { it.archetypeId }
        return listOf(
            requireNotNull(base["adult-human"]),
            requireNotNull(base["medium-dog"]),
            PhysicalArchetype(
                archetypeId = "rescue-pry-bar",
                entityKind = "RIGID_TOOL",
                bodyPlan = "RIGID_TOOL",
                massKg = 2.0,
                dimensionsCm = PhysicalDimensionsCm(60.0, 5.0, TOOL_HEIGHT_CM),
                movable = true,
            ),
            PhysicalArchetype(
                archetypeId = "stable-rock-fulcrum",
                entityKind = "FIXED_STRUCTURE",
                bodyPlan = "FIXED_STRUCTURE",
                massKg = 1000.0,
                dimensionsCm = PhysicalDimensionsCm(120.0, 80.0, 80.0),
                movable = false,
                supportSurfaces = listOf(
                    PhysicalSupportSurface(
                        surfaceId = "fulcrum",
                        stable = true,
                        maxLoadKg = 200.0,
                        widthCm = 80.0,
                        depthCm = 40.0,
                        allowedUses = listOf(PhysicalSupportUse.STAND_UP_ASSIST, PhysicalSupportUse.OBJECT_PLACEMENT),
                        allowedBodyPlans = listOf("ADULT_HUMANOID"),
                    ),
                ),
            ),
            requireNotNull(base["wide-door"]).copy(archetypeId = "recess-opening"),
        )
    }

    private fun initialSnapshot(): PhysicalWorldSnapshot = PhysicalWorldSnapshot(
        snapshotId = "wp05-screen01-scene03-physical-v0",
        boundary = FormalTurnPhysicalGateValidator.FORMAL_TURN_BOUNDARY,
        entities = listOf(
            PhysicalEntityInstance(ACTOR_ID, "陸塵行", "adult-human", PhysicalVector3Cm(0.0, 0.0, 87.5)),
            PhysicalEntityInstance(DOG_ID, "愛醬", "medium-dog", PhysicalVector3Cm(50.0, 0.0, 27.5)),
            PhysicalEntityInstance(TOOL_ID, "短撬桿", "rescue-pry-bar", PhysicalVector3Cm(40.0, 20.0, TOOL_HEIGHT_CM / 2.0)),
            PhysicalEntityInstance(ROCK_ID, "穩定岩面", "stable-rock-fulcrum", PhysicalVector3Cm(50.0, -20.0, 40.0)),
            PhysicalEntityInstance(OPENING_ID, "凹壁入口", "recess-opening", PhysicalVector3Cm(65.0, 0.0, 105.0)),
        ),
    )

    private fun verifyScreen01Source(repositoryRoot: Path): Screen01Source {
        val master = Json.parseToJsonElement(Files.readString(repositoryRoot.resolve(sourcePaths[0])).removePrefix("\uFEFF")).jsonObject
        require(master["title"]?.jsonPrimitive?.content == "潮線之外")
        require(master["canonBoundary"]?.jsonPrimitive?.content == "NON_CANON_PILOT")
        require(master["charactersPresent"]?.jsonArray?.any { it.jsonObject["characterId"]?.jsonPrimitive?.content == ACTOR_ID } == true)
        require(master["charactersPresent"]?.jsonArray?.any { it.jsonObject["characterId"]?.jsonPrimitive?.content == DOG_ID } == true)
        require(master["props"]?.jsonArray?.any { it.jsonObject["propId"]?.jsonPrimitive?.content == TOOL_ID } == true)
        val sceneList = Json.parseToJsonElement(Files.readString(repositoryRoot.resolve(sourcePaths[1])).removePrefix("\uFEFF")).jsonObject
        val scene = requireNotNull(sceneList["scenes"]?.jsonArray?.map { it.jsonObject }?.singleOrNull {
            it["sceneId"]?.jsonPrimitive?.content == SCENE_ID
        })
        require(scene["locationId"]?.jsonPrimitive?.content == LOCATION_ID)
        require(scene["characters"]?.jsonArray?.any { it.jsonPrimitive.content == DOG_ID } == true)
        require(scene["props"]?.jsonArray?.any { it.jsonPrimitive.content == TOOL_ID } == true)
        return Screen01Source("潮線之外", SCENE_ID, LOCATION_ID)
    }

    private fun sourceEvidence(repositoryRoot: Path, relative: String): WorldPhysics05SourceEvidence {
        val path = repositoryRoot.resolve(relative)
        require(Files.isRegularFile(path)) { "Missing SCREEN-01 source: $relative" }
        return WorldPhysics05SourceEvidence(relative, sha256(path), Files.size(path))
    }

    private fun sha256(path: Path): String = MessageDigest.getInstance("SHA-256")
        .digest(Files.readAllBytes(path))
        .joinToString("") { "%02x".format(it) }

    private fun PhysicalWorldSnapshot.entity(entityId: String): PhysicalEntityInstance =
        entities.single { it.entityId == entityId }

    private fun PhysicalEntityInstance.evidence(): WorldPhysics05EntityEvidence = WorldPhysics05EntityEvidence(
        entityId = entityId,
        centerCm = centerCm,
        heldByEntityId = heldByEntityId,
        supportedByEntityId = supportedByEntityId,
        supportedBySurfaceId = supportedBySurfaceId,
        containedByEntityId = containedByEntityId,
    )

    private fun WorldPhysics05PilotChecks.asList(): List<Boolean> = listOf(
        sourceHashesVerified,
        dogSupportRejected,
        rejectedAttemptStateUnchanged,
        samePendingTurnRepairApplied,
        heldToolFollowedHolder,
        placedToolRelationPersisted,
        droppedToolRelationsCleared,
        formalRuntimeTurnCountVerified,
    )

    private data class Screen01Source(val storyTitle: String, val sceneId: String, val locationId: String)

    companion object {
        const val PROJECT_ID = "world-physics05-screen01"
        const val SCENE_ID = "scene-03"
        const val LOCATION_ID = "loc-daping-coast-recess"
        const val ACTOR_ID = "character-lu-chenxing"
        const val DOG_ID = "character-aijiang"
        const val TOOL_ID = "prop-pry-bar"
        const val ROCK_ID = "physical-stable-rock-fulcrum"
        const val OPENING_ID = "physical-recess-opening"
        const val TOOL_HEIGHT_CM = 5.0
        val SAFE_INSIDE_POSITION = PhysicalVector3Cm(100.0, 0.0, 87.5)
        val sourcePaths = listOf(
            "samples/screen01/screen01_story_production_master.json",
            "samples/screen01/screen01_scene_list.json",
        )
    }
}
