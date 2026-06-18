package com.localnovel.engine

import com.localnovel.engine.model.CharacterKnowledgeClaim
import com.localnovel.engine.model.CharacterProfile
import com.localnovel.engine.model.CharacterState
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
