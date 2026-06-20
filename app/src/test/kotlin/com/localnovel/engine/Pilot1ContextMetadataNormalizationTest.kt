package com.localnovel.engine

import com.localnovel.engine.model.CharacterKnowledgeClaim
import com.localnovel.engine.model.CharacterProfile
import com.localnovel.engine.model.CharacterState
import com.localnovel.engine.model.AvailableTransition
import com.localnovel.engine.model.ContextBudget
import com.localnovel.engine.model.ContextKnowledgeClaim
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.ContinuityConstraints
import com.localnovel.engine.model.Event
import com.localnovel.engine.model.Fact
import com.localnovel.engine.model.Item
import com.localnovel.engine.model.Project
import com.localnovel.engine.model.RelevantSetting
import com.localnovel.engine.model.RetrievedMemory
import com.localnovel.engine.model.Scene
import com.localnovel.engine.model.SceneContract
import com.localnovel.engine.model.SceneLifecycle
import com.localnovel.engine.model.SettingContextBudget
import com.localnovel.engine.model.Task
import com.localnovel.engine.model.Technique
import com.localnovel.engine.model.WorldRule
import com.localnovel.engine.service.ContextAuditService
import com.localnovel.engine.service.ContextBudgetForensicsService
import com.localnovel.engine.service.ContextBudgetUnsatisfiableException
import com.localnovel.engine.service.ContextBudgeter
import com.localnovel.engine.service.ContextPacketFinalizer
import com.localnovel.engine.service.ContextPacketNormalizer
import com.localnovel.engine.service.ContextPacketTopLevelAllowlist
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.nio.file.Files
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class Pilot1ContextMetadataNormalizationTest {
    @Test
    fun finalContextOnlySerializesAllowlistedTopLevelFields() {
        val finalized = finalize(baseContext())

        val keys = ContextPacketNormalizer.serializedTopLevelKeys(finalized)

        assertTrue(keys.all { it in ContextPacketTopLevelAllowlist.keys }, keys.joinToString())
    }

    @Test
    fun unknownTopLevelFieldFailsAuditInsteadOfBecomingOtherMetadata() {
        val finalized = finalize(baseContext())
        val raw = JsonSupport.json.encodeToString(finalized).replaceFirst("{", "{\"unexpectedDiagnostics\":true,")
        val file = Files.createTempDirectory("metadata-normalization-unknown").resolve("context.json")
        Files.writeString(file, raw)

        val report = ContextAuditService().auditFile(file)

        assertFalse(report.valid)
        assertTrue(report.violations.any { it == "unknown_top_level_field:unexpectedDiagnostics" })
    }

    @Test
    fun diagnosticsAreNotSerializedInFinalContext() {
        val text = JsonSupport.json.encodeToString(finalize(baseContext()))

        assertFalse(text.contains("perSectionBytesBefore"))
        assertFalse(text.contains("topLargestJsonPaths"))
        assertFalse(text.contains("duplicateContentGroups"))
        assertFalse(text.contains("otherMetadata"))
    }

    @Test
    fun auditSidecarStillIncludesFullForensics() {
        val finalized = finalize(baseContext())
        val report = ContextAuditService().audit(finalized)

        assertTrue(report.valid, report.violations.joinToString())
        assertTrue(report.forensics?.topLargestJsonPaths?.isNotEmpty() == true)
        assertTrue(report.forensics?.metadataPathBreakdown?.isNotEmpty() == true)
    }

    @Test
    fun legacyCharacterMemoryAliasIsNotSerializedAlongsideRetrievedMemories() {
        val text = JsonSupport.json.encodeToString(finalize(baseContext()))

        assertTrue(text.contains("retrievedMemories"))
        assertFalse(text.contains("characterMemories"))
    }

    @Test
    fun activeCharacterRuntimeTemplateIsRemovedAfterRuntimeStateExists() {
        val text = JsonSupport.json.encodeToString(finalize(baseContext()))

        assertFalse(text.contains("initialRuntimeTemplate"))
        assertTrue(text.contains("current_goal"))
    }

    @Test
    fun characterStateFlagsDropHistoricalDiagnosticsButKeepRuntimeMind() {
        val finalized = finalize(baseContext())
        val state = finalized.characterStates.single { it.characterId == "char-warden" }

        assertEquals("Keep everyone away from the shadow.", state.flags["current_goal"])
        assertEquals("alert", state.flags["dominant_emotion"])
        assertEquals("70", state.flags["emotion.alert"])
        assertFalse(state.flags.keys.any { it.contains("last_trigger") })
    }

    @Test
    fun eventPayloadSummaryDuplicateIsRemoved() {
        val finalized = finalize(baseContext())
        val event = finalized.recentEvents.single()

        assertEquals("The shadow twitched toward the last voice.", event.summary)
        assertFalse(event.payload.containsKey("summary"))
        assertFalse(event.payload.containsKey("diagnostics"))
    }

    @Test
    fun emptyAndDefaultOptionalFieldsAreNotSerialized() {
        val text = JsonSupport.json.encodeToString(finalize(baseContext()))

        assertFalse(text.contains("revealedSecrets"))
        assertFalse(text.contains("openTasks"))
        assertFalse(text.contains("directorPressure"))
    }

    @Test
    fun contextBudgetUsesCompactReasonCodesOnly() {
        val finalized = finalize(largeContext(), 6000)
        val text = JsonSupport.json.encodeToString(finalized)

        assertTrue(text.contains("truncationReasonCodes"))
        assertFalse(text.contains("truncationReasons"))
        val budgetBytes = ContextBudgetForensicsService(6000).sectionReports(finalized).single { it.section == "contextBudgetMetadata" }.utf8Bytes
        assertTrue(budgetBytes < 1200, "contextBudgetMetadata=$budgetBytes")
    }

    @Test
    fun largeTurnEightLikeInputFitsHardBudgetAfterNormalization() {
        val finalized = finalize(largeContext(playerInput = turnEightInput()), 24576)

        assertTrue(finalized.contextBudget.actualBytes <= 24576)
    }

    @Test
    fun otherMetadataSectionIsEmptyAfterNormalization() {
        val report = ContextBudgetForensicsService().report(baseContext(), finalize(baseContext()))
        val other = report.perSectionBytesAfter.single { it.section == "otherMetadata" }

        assertTrue(other.utf8Bytes <= 2)
        assertEquals(0, other.itemCount)
    }

    @Test
    fun mandatorySafetyFieldsRemainPresent() {
        val audit = ContextAuditService().audit(finalize(baseContext()))

        assertTrue(audit.mandatoryFieldsPresent)
        assertTrue(audit.forensics?.safetyFieldPresence?.forbiddenReveals == true)
        assertTrue(audit.forensics?.safetyFieldPresence?.characterStates == true)
    }

    @Test
    fun forbiddenSecretContentDoesNotEnterFinalContext() {
        val text = JsonSupport.json.encodeToString(finalize(baseContext()))

        assertFalse(text.contains("author-only murder truth"))
    }

    @Test
    fun itemAndTechniqueRequirementsCostsAndLimitationsStayInRelevantSettings() {
        val finalized = finalize(baseContext())
        val settingText = JsonSupport.json.encodeToString(finalized.relevantSettings)

        assertTrue(settingText.contains("costs"))
        assertTrue(settingText.contains("limitations"))
        assertTrue(settingText.contains("cannot identify silent targets"))
    }

    @Test
    fun contextHashUsesFinalNormalizedPayload() {
        val finalized = finalize(baseContext())

        assertEquals(hash(finalized), finalized.contextHash)
    }

    @Test
    fun actualBytesEqualsSerializedUtf8Bytes() {
        val finalized = finalize(baseContext())

        assertEquals(JsonSupport.json.encodeToString(finalized).toByteArray(Charsets.UTF_8).size, finalized.contextBudget.actualBytes)
    }

    @Test
    fun normalizationIsDeterministic() {
        val first = JsonSupport.json.encodeToString(finalize(baseContext()))
        val second = JsonSupport.json.encodeToString(finalize(baseContext()))

        assertEquals(first, second)
    }

    @Test
    fun forensicsReportsNoDanglingReferencesAfterNormalization() {
        val report = ContextBudgetForensicsService().report(baseContext(), finalize(baseContext()))

        assertTrue(report.danglingReferenceCheck.valid, report.danglingReferenceCheck.missingReferences.joinToString())
    }

    @Test
    fun transitionSettingMetadataIsDeduplicatedAgainstTransitionPreview() {
        val unmet = "Need patient name, medicine, package number, and witness before opening the seal."
        val context = baseContext(playerInput = "I ask whether we can move toward the awning after checking the package number.").copy(
            availableTransitions = listOf(
                AvailableTransition(
                    transitionId = "transition-gate-to-awning",
                    toSceneId = "scene-awning",
                    targetLocation = "loc-awning",
                    playerIntentRequired = "move to awning to register responsibility",
                    prerequisitesSatisfied = false,
                    unmetPrerequisites = listOf(unmet, unmet),
                    travelMinutes = 1,
                    forcedAllowed = false,
                    visibleReason = unmet,
                ),
            ),
            relevantSettings = baseContext().relevantSettings + RelevantSetting(
                "scene_transition",
                "transition-gate-to-awning",
                "Move to awning",
                "Transition to the awning. $unmet",
                requiredRuntimeUse = true,
                relationPath = listOf(
                    "scene:scene-gate-HAS_TRANSITION->scene_transition:transition-gate-to-awning",
                    "scene_transition:transition-gate-to-awning-TARGETS->scene:scene-awning",
                ),
                metadata = JsonObject(
                    mapOf(
                        "targetSceneId" to JsonPrimitive("scene-awning"),
                        "targetLocationId" to JsonPrimitive("loc-awning"),
                        "playerIntentRequired" to JsonPrimitive("move to awning to register responsibility"),
                        "prerequisitesSatisfied" to JsonPrimitive(false),
                        "unmetPrerequisites" to JsonArray(listOf(JsonPrimitive(unmet))),
                        "travelMinutes" to JsonPrimitive(1),
                        "forcedAllowed" to JsonPrimitive(false),
                    ),
                ),
            ),
        )

        val finalized = finalize(context)
        val transitionSetting = finalized.relevantSettings.single { it.entityType == "scene_transition" }

        assertEquals("", transitionSetting.summary)
        assertEquals(emptyList(), transitionSetting.relationPath)
        assertFalse(transitionSetting.metadata.containsKey("unmetPrerequisites"))
        assertFalse(transitionSetting.metadata.containsKey("prerequisitesSatisfied"))
        assertEquals("", finalized.availableTransitions.single().visibleReason)
        assertEquals(listOf(unmet), finalized.availableTransitions.single().unmetPrerequisites)
        assertTrue(finalized.contextBudget.actualBytes <= 24576)
    }

    @Test
    fun relationshipDefaultsAreRemovedFromCharacterStateFlags() {
        val context = baseContext().copy(
            characterStates = baseContext().characterStates.map { state ->
                if (state.characterId == "char-warden") {
                    state.copy(
                        flags = state.flags + mapOf(
                            "relationship_defaults" to "A long default relationship matrix that belongs in canon, not final context.",
                            "available_actions" to "[\"control perimeter\",\"answer partially\",\"test safely\",\"repeat procedure\"]",
                        ),
                    )
                } else {
                    state
                }
            },
        )

        val state = finalize(context).characterStates.single { it.characterId == "char-warden" }

        assertFalse(state.flags.containsKey("relationship_defaults"))
        assertEquals("control perimeter; answer partially; test safely; repeat procedure", state.flags["available_actions"])
    }

    @Test
    fun missingKnowledgeSourceReferencesAreDroppedDuringNormalization() {
        val context = baseContext().copy(
            facts = emptyList(),
            retrievedMemories = emptyList(),
            knowledgeClaims = listOf(
                ContextKnowledgeClaim(
                    "claim-missing-source",
                    "char-warden",
                    "missing_source",
                    "A belief that used to point at a source removed by final context trimming.",
                    "BELIEVES",
                    50,
                    "fact",
                    sourceFactId = "fact-not-retained",
                ),
            ),
        )

        val claim = finalize(context).knowledgeClaims.single()

        assertEquals("belief", claim.sourceType)
        assertNull(claim.sourceFactId)
        assertNull(claim.sourceMemoryId)
    }

    @Test
    fun openTaskPayloadIsCompactedAndDeduplicatedDuringNormalization() {
        val title = "Confirm the limited water-trace witness path before anyone can turn it into cargo responsibility."
        val context = baseContext().copy(
            openTasks = listOf(
                Task(
                    "task-water-witness",
                    "metadata-normalization",
                    title,
                    status = "open",
                    payload = JsonObject(
                        mapOf(
                            "title" to JsonPrimitive(title),
                            "status" to JsonPrimitive("open"),
                            "currentObjective" to JsonPrimitive("Record water trace, wheel rut, and low blocks without opening cargo. ".repeat(4)),
                            "nextOptions" to JsonArray(
                                (1..6).map { JsonPrimitive("option-$it: verify one narrow procedural step without touching the warehouse seal") },
                            ),
                            "diagnostics" to JsonPrimitive("debug payload that should not enter final context"),
                            "fullHistory" to JsonPrimitive("long task history ".repeat(50)),
                        ),
                    ),
                    createdTurn = 3,
                    updatedTurn = 10,
                ),
            ),
        )

        val task = finalize(context).openTasks.single()

        assertEquals("task-water-witness", task.id)
        assertFalse(task.payload.containsKey("title"))
        assertFalse(task.payload.containsKey("status"))
        assertFalse(task.payload.containsKey("diagnostics"))
        assertFalse(task.payload.containsKey("fullHistory"))
        assertTrue(task.title.length <= 120)
        assertTrue(task.payload["currentObjective"].toString().length < 140)
        assertEquals(3, (task.payload["nextOptions"] as JsonArray).size)
    }

    @Test
    fun retrievedMemoryCardsAreCompactedButKeepIdentityAndUse() {
        val context = baseContext().copy(
            retrievedMemories = listOf(
                RetrievedMemory(
                    "memory-turn-009-limited-witness",
                    "char-warden",
                    "EPISODIC",
                    "The stranger accepted limited witness responsibility for checking water traces but refused cargo opening responsibility. ".repeat(4),
                    emotionalSignature = JsonObject(
                        mapOf(
                            "trust_shift" to JsonPrimitive("cautious but procedurally useful"),
                            "risk" to JsonPrimitive("may draw buyer and merchant hostility"),
                            "debug_source" to JsonPrimitive("internal retrieval diagnostics"),
                            "long_note" to JsonPrimitive("detailed emotional analysis ".repeat(20)),
                        ),
                    ),
                    confidence = 90,
                    salience = 88,
                    retrievalScore = 160,
                    retrievalReasons = listOf(
                        "explicitly mentions limited responsibility and water-trace witness",
                        "explicitly mentions limited responsibility and water-trace witness",
                        "connects to awning transition intent",
                    ),
                    allowedUses = listOf("character_action", "dialogue_subtext", "emotion_trigger", "long_form_summary"),
                ),
            ),
        )

        val memory = finalize(context).retrievedMemories.single()

        assertEquals("memory-turn-009-limited-witness", memory.memoryId)
        assertTrue(memory.summary.length <= 140)
        assertEquals(2, memory.retrievalReasons.size)
        assertTrue(memory.retrievalReasons.all { it.length <= 48 })
        assertEquals(2, memory.allowedUses.size)
        assertFalse(memory.emotionalSignature.containsKey("debug_source"))
        assertTrue(memory.emotionalSignature.size <= 3)
    }

    @Test
    fun turnTenLikePressureFitsAfterTaskAndMemoryCompaction() {
        val playerInput = """
            I formally accept limited witness responsibility and go to the awning with witness, but only for checking water.
            I do not cross the chalk line, touch the seal, touch the cargo, open the warehouse, or accept cargo responsibility.
            Record water trace, wheel rut, and low blocks as separate from opening, taking cargo, or assigning guilt.
        """.trimIndent().repeat(4)
        val base = largeContext(playerInput)
        val context = base.copy(
            facts = base.facts + (1..24).map {
                Fact(
                    "fact-water-$it",
                    "metadata-normalization",
                    "Water-trace procedural fact $it: right drain, wheel rut, low blocks, buyer pressure, clerk record, and witness boundary remain separate from opening cargo.",
                    "public",
                    "turn",
                    it,
                )
            },
            retrievedMemories = base.retrievedMemories + (1..16).map {
                RetrievedMemory(
                    "memory-water-$it",
                    "char-warden",
                    "EPISODIC",
                    "Earlier turn $it kept the witness boundary narrow: water trace, wheel rut, low blocks, no cargo opening, and no blame assignment. ".repeat(3),
                    confidence = 85,
                    salience = 80,
                    retrievalScore = 150 - it,
                    retrievalReasons = listOf("limited witness boundary repeated in player intent", "transition to awning with witness"),
                )
            },
            openTasks = listOf(
                Task(
                    "task-pilot2-legal-opening-path",
                    "metadata-normalization",
                    "Resolve the limited legal opening path without turning water witness into cargo responsibility.",
                    payload = JsonObject(
                        mapOf(
                            "currentObjective" to JsonPrimitive("Keep check-water-only responsibility distinct from opening, taking cargo, or assigning guilt. ".repeat(5)),
                            "nextOptions" to JsonArray((1..8).map { JsonPrimitive("option-$it: one procedural next step after awning witness") }),
                            "knownFacts" to JsonArray((1..8).map { JsonPrimitive("known fact $it: water trace and witness boundary remain relevant") }),
                        ),
                    ),
                    createdTurn = 3,
                    updatedTurn = 10,
                ),
            ),
            relevantSettings = base.relevantSettings + (1..16).map {
                RelevantSetting(
                    "scene_transition",
                    "transition-awning-$it",
                    "Awning witness transition $it",
                    "Move to the awning for a limited witness record without opening cargo.",
                    requiredRuntimeUse = true,
                    metadata = JsonObject(
                        mapOf(
                            "targetSceneId" to JsonPrimitive("scene-awning"),
                            "targetLocationId" to JsonPrimitive("loc-awning"),
                            "playerIntentRequired" to JsonPrimitive("go to awning with witness"),
                            "travelMinutes" to JsonPrimitive(1),
                            "forcedAllowed" to JsonPrimitive(false),
                        ),
                    ),
                )
            },
        )

        val finalized = finalize(context, 24576)

        assertTrue(finalized.contextBudget.actualBytes <= 24576)
        assertTrue(finalized.openTasks.any { it.id == "task-pilot2-legal-opening-path" })
    }

    @Test
    fun auditContextDoesNotRewriteContextFile() {
        val finalized = finalize(baseContext())
        val file = Files.createTempDirectory("metadata-normalization-audit").resolve("context.json")
        val before = JsonSupport.json.encodeToString(finalized)
        Files.writeString(file, before)

        ContextAuditService().auditFile(file)

        assertEquals(before, Files.readString(file))
    }

    @Test
    fun unsatisfiablePreviewDoesNotMutateInputContext() {
        val context = baseContext(playerInput = "界".repeat(3000)).copy(contextBudget = ContextBudget(maxBytes = 1000))
        val before = JsonSupport.json.encodeToString(context)

        assertFailsWith<ContextBudgetUnsatisfiableException> { finalize(context, 1000) }

        assertEquals(before, JsonSupport.json.encodeToString(context))
    }

    @Test
    fun utf8ChineseRoundTripPreservesPlayerInputAndByteAccounting() {
        val finalized = finalize(baseContext(playerInput = "我摸了摸愛醬的頸側。"))
        val decoded = JsonSupport.json.decodeFromString<ContextPacket>(JsonSupport.json.encodeToString(finalized))

        assertEquals("我摸了摸愛醬的頸側。", decoded.playerInput)
        assertTrue(decoded.playerInput.toByteArray(Charsets.UTF_8).size > decoded.playerInput.length)
        assertEquals(finalized.contextBudget.actualBytes, decoded.contextBudget.actualBytes)
    }

    private fun finalize(context: ContextPacket, maxBytes: Int = 24576): ContextPacket {
        val budgeted = ContextBudgeter(maxBytes).applyBudget(context, omittedMemoryCount = 0, omittedKnowledgeCount = 0)
        return ContextPacketFinalizer(maxBytes).finalize(budgeted, ::hash)
    }

    private fun baseContext(playerInput: String = "I watch the shadow."): ContextPacket {
        val repeated = "Names and papers have legal force."
        return ContextPacket(
            project = Project("metadata-normalization", "Metadata Normalization", repeated, "scene-gate"),
            turnId = "session-meta-turn-8",
            turnNumber = 8,
            playerInput = playerInput,
            activeScene = Scene("scene-gate", "metadata-normalization", "Gate", "A rain-wet gate with a prone officer.", "loc-gate"),
            sceneContract = SceneContract("contract-gate", "metadata-normalization", "scene-gate", forbiddenReveals = listOf("hidden phrase")),
            characters = listOf(
                CharacterProfile(
                    "char-player",
                    "metadata-normalization",
                    "Player",
                    "player",
                    "A cautious stranger.",
                    isPlayer = true,
                ),
                CharacterProfile(
                    "char-warden",
                    "metadata-normalization",
                    "Zhao",
                    "officer",
                    "A cautious officer who follows procedure.",
                    profile = JsonObject(
                        mapOf(
                            "initialRuntimeTemplate" to JsonObject(mapOf("initialGoal" to JsonPrimitive("Keep everyone away from the shadow."))),
                            "history" to JsonPrimitive("A long diagnostic history that belongs in canon, not every turn context."),
                        ),
                    ),
                ),
            ),
            characterStates = listOf(
                CharacterState(
                    "char-player",
                    "metadata-normalization",
                    "loc-gate",
                    "calm",
                    "normal",
                    inventory = listOf("item-token"),
                    flags = mapOf(
                        "current_goal" to "Observe without touching.",
                        "dominant_emotion" to "calm",
                        "available_actions" to "wait; gesture; speak",
                        "emotion.calm" to "55",
                    ),
                ),
                CharacterState(
                    "char-warden",
                    "metadata-normalization",
                    "loc-gate",
                    "alert",
                    "normal",
                    inventory = listOf("item-token"),
                    flags = mapOf(
                        "current_goal" to "Keep everyone away from the shadow.",
                        "dominant_emotion" to "alert",
                        "available_actions" to "control perimeter; answer partially; test safely",
                        "emotion.alert" to "70",
                        "emotion.alert.last_trigger" to "Very long diagnostic trigger text that should not be in final context.",
                    ),
                ),
            ),
            worldRules = listOf(WorldRule("rule-identity", "metadata-normalization", "Identity Law", repeated, 10)),
            facts = listOf(Fact("fact-shadow-voice", "metadata-normalization", "The shadow twitched toward the last voice.", "public", "turn", 4)),
            items = listOf(Item("item-token", "metadata-normalization", "Cracked Token", "A cracked officer token.", "char-warden")),
            techniques = listOf(Technique("tech-identify", "metadata-normalization", "Quiet Identification", "A cautious method with costs.", "char-warden")),
            recentEvents = listOf(
                Event(
                    "event-shadow-voice",
                    "metadata-normalization",
                    "session-meta-turn-7",
                    "The shadow twitched toward the last voice.",
                    JsonObject(
                        mapOf(
                            "summary" to JsonPrimitive("The shadow twitched toward the last voice."),
                            "diagnostics" to JsonPrimitive("debug-only"),
                        ),
                    ),
                ),
            ),
            contextId = "ctx-meta",
            sessionId = "session-meta",
            baseStateVersion = 7,
            generatedAt = "2026-06-18T00:00:00Z",
            retrievedMemories = listOf(
                RetrievedMemory(
                    "memory-quiet-test",
                    "char-warden",
                    "EPISODIC",
                    "The stranger proposed a quiet test without stepping forward.",
                    confidence = 90,
                    salience = 80,
                    retrievalScore = 120,
                ),
            ),
            knowledgeClaims = listOf(
                ContextKnowledgeClaim(
                    "claim-shadow-voice",
                    "char-warden",
                    "shadow_tracks_voice",
                    "The shadow may be tracking sound direction.",
                    "BELIEVES",
                    65,
                    "fact",
                    sourceFactId = "fact-shadow-voice",
                ),
            ),
            contextBudget = ContextBudget(truncationReasons = listOf("trimmed character state flags aggressively")),
            sceneLifecycle = SceneLifecycle("scene-instance-1", "scene-gate", startedTurnNumber = 1),
            worldClock = com.localnovel.engine.model.WorldClock("rain morning", 7, 8),
            continuityConstraints = ContinuityConstraints("scene-instance-1", "loc-gate", mapOf("char-warden" to "loc-gate", "char-player" to "loc-gate"), mapOf("item-token" to "char-warden")),
            relevantSettings = listOf(
                RelevantSetting(
                    "world_rule",
                    "rule-identity",
                    "Identity Law",
                    repeated,
                    requiredRuntimeUse = true,
                    relationPath = listOf("world_rule:rule-identity-APPLIES_TO->scene:scene-gate"),
                ),
                RelevantSetting(
                    "technique",
                    "tech-identify",
                    "Quiet Identification",
                    "A cautious method with costs.",
                    requiredRuntimeUse = true,
                    metadata = JsonObject(
                        mapOf(
                            "costs" to JsonArray(listOf(JsonPrimitive("requires uninterrupted focus"))),
                            "limitations" to JsonArray(listOf(JsonPrimitive("cannot identify silent targets"))),
                        ),
                    ),
                ),
            ),
            settingContextBudget = SettingContextBudget(includedSettingCount = 2, reasons = listOf("setting context compacted")),
        )
    }

    private fun largeContext(playerInput: String = "I keep watching."): ContextPacket =
        baseContext(playerInput).copy(
            relevantSettings = baseContext(playerInput).relevantSettings + (1..80).map {
                RelevantSetting(
                    "item",
                    "extra-$it",
                    "Extra $it",
                    "Verbose setting excerpt ".repeat(40),
                    requiredRuntimeUse = false,
                    relationPath = listOf("item:extra-$it-RELATED_TO->scene:scene-gate"),
                )
            },
            retrievedMemories = baseContext(playerInput).retrievedMemories + (1..20).map {
                RetrievedMemory(
                    "memory-extra-$it",
                    "char-warden",
                    "EPISODIC",
                    "A previous observation about careful testing number $it.",
                    confidence = 80,
                    salience = 50,
                    retrievalScore = 80 - it,
                )
            },
        )

    private fun turnEightInput(): String =
        """
        我摸了摸愛醬的頸側，讓牠知道我記得牠剛才忍住低吼守著我。

        「趙無咎，既然安靜時它沒有追過來，我們能不能不碰它，慢慢退到驛亭門外？」

        我仍留在原地，等他回答。
        """.trimIndent()

    private fun hash(context: ContextPacket): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(JsonSupport.json.encodeToString(context.copy(contextHash = "")).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
