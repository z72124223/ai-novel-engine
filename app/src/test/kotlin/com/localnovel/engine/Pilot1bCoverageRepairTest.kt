package com.localnovel.engine

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.InitialRuntimeTemplate
import com.localnovel.engine.model.LocationConnection
import com.localnovel.engine.model.SceneTransitionDefinition
import com.localnovel.engine.model.ScheduledWorldEventSeed
import com.localnovel.engine.model.SettingPack
import com.localnovel.engine.model.SettingPackEntities
import com.localnovel.engine.model.SettingPackEntity
import com.localnovel.engine.model.SettingPackRelation
import com.localnovel.engine.model.SettingPackSource
import com.localnovel.engine.model.TimeAdvanceProposal
import com.localnovel.engine.model.TransitionProposal
import com.localnovel.engine.model.WorldEventAcknowledgement
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.service.SettingPackImportService
import com.localnovel.engine.service.SettingPackParser
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Pilot1bCoverageRepairTest {
    @Test
    fun settingPackImportsRuntimeTopology() {
        val repository = newRepository()
        val service = SettingPackImportService(repository)
        repository.initialize()
        val file = Files.createTempDirectory("pilot1b-pack").resolve("pack.json")
        Files.writeString(file, JsonSupport.json.encodeToString(pilot1bPack()))
        val parsed = SettingPackParser().parse(file)

        val preview = service.preview(parsed)
        val result = service.apply(parsed)

        assertTrue(preview.validation.isValid, preview.validation.issues.joinToString { it.message })
        assertTrue(preview.diff.entries.any { it.entityType == "location_connection" && it.action == "ADD" })
        assertTrue(preview.diff.entries.any { it.entityType == "scene_transition" && it.action == "ADD" })
        assertTrue(preview.diff.entries.any { it.entityType == "world_event" && it.action == "ADD" })
        assertTrue(result.applied)
        assertTrue(repository.targetExists("pilot1b", "location_connection", "conn-abandoned-post-to-perimeter"))
        assertTrue(repository.targetExists("pilot1b", "scene_transition", "transition-abandoned-post-to-perimeter-hold"))
        assertTrue(repository.targetExists("pilot1b", "world_event", "world-event-verifier-arrival-pressure"))
    }

    @Test
    fun validatorRejectsBrokenRuntimeTopology() {
        val repository = newRepository()
        repository.initialize()
        val brokenTransition = pilot1bPack().copy(
            locationConnections = emptyList(),
            sceneTransitions = listOf(
                pilot1bPack().sceneTransitions.first().copy(requiredPlayerIntent = ""),
            ),
        )
        val brokenEvent = pilot1bPack().copy(
            scheduledWorldEvents = listOf(
                pilot1bPack().scheduledWorldEvents.first().copy(payload = JsonObject(emptyMap())),
            ),
        )

        val transitionReport = SettingPackImportService(repository).preview(brokenTransition).validation
        val eventReport = SettingPackImportService(repository).preview(brokenEvent).validation

        assertFalse(transitionReport.isValid)
        assertTrue(transitionReport.issues.any { it.code == "missing_transition_intent" })
        assertTrue(transitionReport.issues.any { it.code == "missing_location_connection" })
        assertFalse(eventReport.isValid)
        assertTrue(eventReport.issues.any { it.code == "missing_public_effect" })
    }

    @Test
    fun activeSessionPatchBlocksRuntimeTopologyMutation() {
        val repository = newRepository()
        repository.initialize()
        val service = SettingPackImportService(repository)
        service.apply(pilot1bPack())
        MultiTurnRuntime(repository).startSession("pilot1b", "scene-abandoned-post")

        val patch = pilot1bPack(packId = "pack-pilot1b-patch", mode = "PATCH_CANON").copy(
            entities = SettingPackEntities(),
            locationConnections = emptyList(),
            sceneTransitions = listOf(pilot1bPack().sceneTransitions.first()),
            scheduledWorldEvents = emptyList(),
            relations = emptyList(),
        )
        val preview = service.preview(patch)

        assertFalse(preview.validation.isValid)
        assertTrue(preview.validation.issues.any { it.code == "active_session_patch_blocked" })
        assertTrue(preview.impact.blockers.contains("active_session_patch_blocked"))
    }

    @Test
    fun prepareTurnKeepsTransitionAndWorldEventSettingContext() {
        val repository = seededPilot1bRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("pilot1b", "scene-abandoned-post")

        val context = runtime.prepareTurn(session.id, "I accept controlled handling and move to perimeter.")

        assertTrue(context.availableTransitions.any { it.transitionId == "transition-abandoned-post-to-perimeter-hold" && it.prerequisitesSatisfied })
        assertTrue(context.pendingWorldEvents.any { it.eventId == "world-event-verifier-arrival-pressure" })
        assertTrue(context.relevantSettings.any { it.entityType == "scene_transition" && it.entityId == "transition-abandoned-post-to-perimeter-hold" })
        assertTrue(context.relevantSettings.any { it.entityType == "location_connection" && it.entityId == "conn-abandoned-post-to-perimeter" })
        assertTrue(context.relevantSettings.any { it.entityType == "world_event" && it.entityId == "world-event-verifier-arrival-pressure" })
        assertTrue(context.settingContextBudget.actualBytes > 2)
        assertTrue(context.contextBudget.actualBytes <= context.contextBudget.maxBytes)
        assertFalse(context.relevantSettings.any { it.visibility == "author_only" })
    }

    @Test
    fun dueWorldEventCanApplyPressureAndConsumeOnce() {
        val repository = seededPilot1bRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("pilot1b", "scene-abandoned-post")
        val context = runtime.prepareTurn(session.id, "I hold position and wait for the verifier pressure.")

        runtime.resolveTurn(
            session.id,
            resultFor(context, "pilot1b-world-event").copy(
                worldEventAcknowledgements = listOf(worldAck()),
            ),
        )

        val event = repository.tableRows("scheduled_world_events").single { it["id"] == "world-event-verifier-arrival-pressure" }
        assertEquals("consumed", event["status"])
        assertEquals("2", repository.getSession(session.id)?.nextTurnNumber?.toString())
    }

    @Test
    fun legalSceneTransitionClosesOldSceneAndActivatesNewScene() {
        val repository = seededPilot1bRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("pilot1b", "scene-abandoned-post")
        val context = runtime.prepareTurn(session.id, "I accept controlled handling and move to perimeter.")
        val transition = context.availableTransitions.single { it.transitionId == "transition-abandoned-post-to-perimeter-hold" }

        runtime.resolveTurn(
            session.id,
            resultFor(context, "pilot1b-transition").copy(
                transitionProposal = transitionToPerimeter(context),
            ),
        )

        assertTrue(transition.prerequisitesSatisfied)
        assertEquals("scene-abandoned-post-perimeter", repository.getSession(session.id)?.currentSceneId)
        assertEquals(1, repository.activeSceneInstanceCount(session.id))
        assertTrue(repository.tableRows("scene_instances").any { it["scene_id"] == "scene-abandoned-post" && it["status"] == "CLOSED" })
        assertTrue(repository.tableRows("character_states").any { it["character_id"] == "char-player" && it["location_id"] == "loc-abandoned-post-perimeter" })
    }

    @Test
    fun playerRefusalCannotBeResolvedAsNormalTransition() {
        val repository = seededPilot1bRepository()
        val runtime = MultiTurnRuntime(repository)
        val session = runtime.startSession("pilot1b", "scene-abandoned-post")
        val context = runtime.prepareTurn(session.id, "I refuse to leave and stay exactly where I am.")

        val report = runtime.validate(
            context,
            resultFor(context, "pilot1b-refusal").copy(
                transitionProposal = transitionToPerimeter(context),
            ),
        )

        assertFalse(report.isValid)
        assertTrue(report.errors.any { it.code == "transition_prerequisite_unmet" || it.code == "player_refused_transition" })
        assertEquals("scene-abandoned-post", repository.getSession(session.id)?.currentSceneId)
    }

    private fun newRepository(): SqliteStoryRepository =
        SqliteStoryRepository(Files.createTempDirectory("pilot1b-coverage-test").resolve("story.db"))

    private fun seededPilot1bRepository(): SqliteStoryRepository {
        val repository = newRepository()
        repository.initialize()
        SettingPackImportService(repository).apply(pilot1bPack())
        return repository
    }

    private fun pilot1bPack(
        packId: String = "pack-pilot1b-coverage",
        mode: String = "BASE",
    ): SettingPack =
        SettingPack(
            schemaVersion = "1.0",
            packId = packId,
            packVersion = 1,
            projectId = "pilot1b",
            name = "Pilot-1b Coverage Pack",
            description = "A narrow topology and pressure fixture for Pilot-1b coverage repair.",
            mode = mode,
            source = SettingPackSource(author = "local-author", license = "CC0"),
            entities = SettingPackEntities(
                projects = listOf(
                    SettingPackEntity(
                        id = "pilot1b",
                        name = "Pilot-1b",
                        summary = "Coverage repair fixture.",
                        metadata = JsonObject(mapOf("activeSceneId" to JsonPrimitive("scene-abandoned-post"))),
                    ),
                ),
                locations = listOf(
                    SettingPackEntity("loc-abandoned-post", name = "Abandoned post", description = "The active investigation scene."),
                    SettingPackEntity("loc-abandoned-post-perimeter", name = "Outer control perimeter", description = "A marked holding area outside the post."),
                ),
                characters = listOf(
                    character("char-player", "Player", true),
                    character("char-ai-chan", "Ai-chan", false),
                    character("char-zhao-wu-jiu", "Zhao Wujiu", false),
                ),
                worldRules = listOf(
                    SettingPackEntity(
                        id = "rule-identity-has-force",
                        title = "Identity has legal force",
                        content = "Unregistered people and unregistered snow dogs cannot be freely released into town.",
                        priority = 10,
                        metadata = JsonObject(mapOf("severity" to JsonPrimitive("hard"), "conflictKey" to JsonPrimitive("identity-law"))),
                    ),
                ),
                scenes = listOf(
                    SettingPackEntity("scene-abandoned-post", title = "Abandoned post", summary = "The investigation is still active.", locationId = "loc-abandoned-post"),
                    SettingPackEntity("scene-abandoned-post-perimeter", title = "Outer perimeter hold", summary = "A controlled holding area outside the post.", locationId = "loc-abandoned-post-perimeter"),
                ),
                sceneContracts = listOf(
                    SettingPackEntity("contract-abandoned-post", sceneId = "scene-abandoned-post", allowedSecretIds = emptyList(), forbiddenReveals = listOf("author-only truth")),
                    SettingPackEntity("contract-perimeter", sceneId = "scene-abandoned-post-perimeter", allowedSecretIds = emptyList(), forbiddenReveals = listOf("author-only truth")),
                ),
                storyThreads = listOf(
                    SettingPackEntity(
                        id = "thread-identity-and-shelter",
                        title = "Identity and shelter",
                        threadType = "plot",
                        publicSummary = "The player needs lawful temporary handling without entering town.",
                        directorGoal = "Test whether procedure can create bounded safety.",
                        visibility = "director_only",
                    ),
                ),
                secrets = listOf(
                    SettingPackEntity(
                        id = "secret-author-only",
                        content = "author-only truth",
                        visibility = "author_only",
                        revealCondition = "never in Pilot-1b",
                    ),
                ),
            ),
            locationConnections = listOf(
                LocationConnection(
                    id = "conn-abandoned-post-to-perimeter",
                    projectId = "pilot1b",
                    fromLocationId = "loc-abandoned-post",
                    toLocationId = "loc-abandoned-post-perimeter",
                    connectionType = "walk",
                    bidirectional = true,
                    travelMinutes = 1,
                ),
            ),
            sceneTransitions = listOf(
                SceneTransitionDefinition(
                    id = "transition-abandoned-post-to-perimeter-hold",
                    projectId = "pilot1b",
                    fromSceneId = "scene-abandoned-post",
                    toSceneId = "scene-abandoned-post-perimeter",
                    transitionType = "controlled_withdrawal",
                    requiredPlayerIntent = "move to perimeter",
                    forcedTransitionAllowed = false,
                ),
            ),
            scheduledWorldEvents = listOf(
                ScheduledWorldEventSeed(
                    id = "world-event-verifier-arrival-pressure",
                    projectId = "pilot1b",
                    eventType = "verifier_arrival_pressure",
                    dueTurnNumber = 1,
                    payload = JsonObject(
                        mapOf(
                            "publicEffect" to JsonPrimitive("A verifier is approaching, forcing Zhao to tighten the holding procedure."),
                            "allowedNarrativeUses" to JsonArray(listOf(JsonPrimitive("verifier pressure"))),
                            "forbiddenCharacterUses" to JsonArray(listOf(JsonPrimitive("force imprint"), JsonPrimitive("separate Ai-chan"))),
                        ),
                    ),
                ),
            ),
            relations = listOf(
                SettingPackRelation("rel-player-located", "character", "char-player", "location", "loc-abandoned-post", "LOCATED_IN"),
                SettingPackRelation("rel-ai-located", "character", "char-ai-chan", "location", "loc-abandoned-post", "LOCATED_IN"),
                SettingPackRelation("rel-zhao-located", "character", "char-zhao-wu-jiu", "location", "loc-abandoned-post", "LOCATED_IN"),
                SettingPackRelation("rel-scene-location", "scene", "scene-abandoned-post", "location", "loc-abandoned-post", "LOCATED_IN"),
                SettingPackRelation("rel-perimeter-location", "scene", "scene-abandoned-post-perimeter", "location", "loc-abandoned-post-perimeter", "LOCATED_IN"),
                SettingPackRelation("rel-thread-player", "story_thread", "thread-identity-and-shelter", "character", "char-player", "AFFECTS"),
            ),
            metadata = JsonObject(mapOf("canonicalHashPreview" to JsonPrimitive(canonicalHashPreviewMarker))),
        )

    private fun character(id: String, name: String, isPlayer: Boolean): SettingPackEntity =
        SettingPackEntity(
            id = id,
            name = name,
            role = if (isPlayer) "player" else "npc",
            description = "$name is present at the abandoned post.",
            metadata = JsonObject(mapOf("isPlayer" to JsonPrimitive(isPlayer.toString()))),
            initialRuntimeTemplate = InitialRuntimeTemplate(
                initialLocationId = "loc-abandoned-post",
                initialGoal = if (isPlayer) "Protect Ai-chan and avoid unlawful handling." else "Maintain safety and procedure from visible evidence.",
                dominantEmotion = "alert",
                emotionState = mapOf("alert" to 60),
                availableActions = listOf("wait", "speak", "observe"),
                relationshipDefaults = mapOf("char-player" to "unknown"),
                initiativeLevel = if (isPlayer) 40 else 55,
            ),
        )

    private fun resultFor(context: ContextPacket, resultId: String): AiTurnResult =
        AiTurnResult(
            projectId = context.project.id,
            turnId = context.turnId,
            resultId = resultId,
            contextId = context.contextId,
            sessionId = context.sessionId,
            turnNumber = context.turnNumber,
            baseStateVersion = context.baseStateVersion,
            contextHash = context.contextHash,
            narrative = "Zhao keeps the site controlled while the player and Ai-chan remain within visible limits.",
            turnSummary = "A controlled Pilot-1b coverage turn records bounded pressure without revealing secrets.",
            characterActions = listOf(
                CharacterAction(
                    characterId = "char-zhao-wu-jiu",
                    intent = "Maintain lawful control without overpromising.",
                    visibleAction = "Zhao marks the boundary and watches the player comply.",
                    hiddenMotive = "He wants evidence before release.",
                    triggeringConditions = listOf(context.playerInput.take(80)),
                    characterFitScore = 90,
                    storyProgressScore = 70,
                ),
            ),
            stateDeltas = listOf(
                DeltaOperation(
                    type = DeltaOperationType.ADD_EVENT,
                    targetType = "event",
                    targetId = "event-$resultId",
                    payload = JsonObject(mapOf("summary" to JsonPrimitive("Pilot-1b coverage event $resultId"))),
                ),
            ),
            timeAdvanceProposal = TimeAdvanceProposal(1, "turn", context.turnId, "Pilot-1b coverage time advance."),
        )

    private fun worldAck(): WorldEventAcknowledgement =
        WorldEventAcknowledgement(
            eventId = "world-event-verifier-arrival-pressure",
            narrativeUse = "verifier pressure",
            perceivedByCharacterIds = listOf("char-zhao-wu-jiu", "char-player"),
            reason = "The due event applies public procedural pressure.",
        )

    private fun transitionToPerimeter(context: ContextPacket): TransitionProposal =
        TransitionProposal(
            fromSceneInstanceId = context.sceneLifecycle.sceneInstanceId,
            transitionId = "transition-abandoned-post-to-perimeter-hold",
            toSceneId = "scene-abandoned-post-perimeter",
            proposalType = "PLAYER_INITIATED",
            playerIntentEvidence = context.playerInput,
            participatingCharacterIds = listOf("char-player", "char-ai-chan", "char-zhao-wu-jiu"),
            reason = "The player explicitly accepts moving to the outer control perimeter.",
        )

    private companion object {
        const val canonicalHashPreviewMarker = "pilot1b"
    }
}
