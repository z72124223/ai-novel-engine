package com.localnovel.engine

import com.localnovel.engine.model.DialogueArtifact
import com.localnovel.engine.model.DialogueArtifactKind
import com.localnovel.engine.model.DialogueEvaluationRequest
import com.localnovel.engine.model.DialogueFinalizeRequest
import com.localnovel.engine.model.DialogueLine
import com.localnovel.engine.model.DialogueLineReview
import com.localnovel.engine.model.DialogueReviewBatch
import com.localnovel.engine.model.DialogueReviewRun
import com.localnovel.engine.model.DialogueReviewerRole
import com.localnovel.engine.model.DialogueReviewVerdict
import com.localnovel.engine.model.DialogueSelectionDecision
import com.localnovel.engine.model.DialogueSelectionRequest
import com.localnovel.engine.model.DialogueSelectionStatus
import com.localnovel.engine.model.ProductionRuleSeverity
import com.localnovel.engine.service.ProductionRuleRegistry
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class EngineRulesDialogueCliTest {
    @Test
    fun cliAndServiceGateProduceSelectedHashBoundResult() {
        val root = Files.createTempDirectory("engine-rules-cli")
        val db = root.resolve("story.db")
        val artifact = artifact()
        val evaluation = write(root.resolve("evaluation.json"), DialogueEvaluationRequest(artifact))
        val handoff = root.resolve("handoff.json")

        assertEquals(0, Cli().run(listOf("evaluate-dialogue", "--db", db.toString(), "--input", evaluation.toString(), "--out", handoff.toString())))
        assertTrue(Files.readString(handoff).contains(ProductionRuleRegistry.RULE_SET_VERSION))

        val finalization = DialogueFinalizeRequest(
            reviewBatch = cleanReviews(artifact),
            selection = DialogueSelectionRequest("cli-decision", artifact.artifactId, "codex-cli", "cli-commit"),
        )
        val input = write(root.resolve("finalize.json"), finalization)
        val decisionPath = root.resolve("decision.json")
        assertEquals(0, Cli().run(listOf("finalize-dialogue", "--db", db.toString(), "--input", input.toString(), "--out", decisionPath.toString())))
        val decision = JsonSupport.json.decodeFromString<DialogueSelectionDecision>(Files.readString(decisionPath))
        assertEquals(DialogueSelectionStatus.SELECTED, decision.status)
        assertEquals(artifact.contentSha256, decision.selectedArtifactSha256)

        val viewPath = root.resolve("selection.json")
        assertEquals(0, Cli().run(listOf("show-dialogue-selection", "--db", db.toString(), "--artifact", artifact.artifactId, "--out", viewPath.toString())))
        assertTrue(Files.readString(viewPath).contains("SELECTED"))
    }

    @Test
    fun cliRejectsUnknownTopLevelFields() {
        val root = Files.createTempDirectory("engine-rules-cli-unknown")
        val db = root.resolve("story.db")
        val path = root.resolve("bad.json")
        Files.writeString(path, "{\"artifact\":{},\"bypass\":true}")
        assertEquals(1, Cli().run(listOf("evaluate-dialogue", "--db", db.toString(), "--input", path.toString())))
    }

    @Test
    fun cliRejectsUnknownNestedFields() {
        val root = Files.createTempDirectory("engine-rules-cli-nested-unknown")
        val db = root.resolve("story.db")
        val encoded = JsonSupport.json.encodeToString(DialogueEvaluationRequest(artifact()))
            .replace("\"productionId\"", "\"unexpectedNested\":true,\"productionId\"")
        val path = root.resolve("bad-nested.json")
        Files.writeString(path, encoded)

        assertEquals(1, Cli().run(listOf("evaluate-dialogue", "--db", db.toString(), "--input", path.toString())))
    }

    @Test
    fun blockedFinalizationReturnsNonZeroMachineStatus() {
        val root = Files.createTempDirectory("engine-rules-cli-blocked")
        val db = root.resolve("story.db")
        val clean = artifact()
        val badDraft = clean.copy(
            artifactId = "cli-blocked-rescue",
            contentSha256 = "",
            lines = clean.lines.map { line -> if (line.lineId == "dlg-012") line.copy(text = "好。先碰你。") else line },
        )
        val bad = badDraft.copy(contentSha256 = ProductionRuleRegistry.artifactHash(badDraft))
        val evaluation = write(root.resolve("evaluation.json"), DialogueEvaluationRequest(bad))
        assertEquals(0, Cli().run(listOf("evaluate-dialogue", "--db", db.toString(), "--input", evaluation.toString())))
        val blockers = ProductionRuleRegistry.canonicalFixtureFindings(bad)
        val reviews = cleanReviews(bad).let { batch ->
            batch.copy(
                reviews = batch.reviews.mapIndexed { index, review ->
                    if (index == 0) review.copy(
                        verdict = DialogueReviewVerdict.REVISE,
                        lineReviews = review.lineReviews.map { line ->
                            line.copy(findings = blockers.filter { it.lineId == line.lineId })
                        },
                    ) else review
                },
            )
        }
        assertTrue(blockers.all { it.severity == ProductionRuleSeverity.BLOCKER })
        val request = DialogueFinalizeRequest(
            reviewBatch = reviews,
            selection = DialogueSelectionRequest("cli-blocked-decision", bad.artifactId, "codex-cli", "cli-commit"),
        )
        val input = write(root.resolve("finalize.json"), request)
        assertEquals(2, Cli().run(listOf("finalize-dialogue", "--db", db.toString(), "--input", input.toString())))
    }

    private fun artifact(): DialogueArtifact {
        val ruleSet = ProductionRuleRegistry.activeRuleSet()
        val profileIds = listOf("voice-lu-chenxing-v1", "voice-tide-picker-v1")
        val profileHashes = ProductionRuleRegistry.characterProfiles()
            .filter { it.profileId in profileIds }
            .associate { it.profileId to it.profileSha256 }
        val language = ProductionRuleRegistry.languageProfile()
        val draft = DialogueArtifact(
            artifactId = "cli-natural-rescue",
            productionId = "dialogue02-cli",
            artifactKind = DialogueArtifactKind.INITIAL,
            contentSha256 = "",
            ruleSetId = ProductionRuleRegistry.RULE_SET_ID,
            ruleSetVersion = ProductionRuleRegistry.RULE_SET_VERSION,
            ruleSetContentSha256 = ruleSet.contentSha256,
            characterVoiceProfileIds = profileIds,
            characterVoiceProfileHashes = profileHashes,
            languageProfileId = ProductionRuleRegistry.LANGUAGE_PROFILE_ID,
            languageProfileSha256 = language.profileSha256,
            lines = listOf(
                DialogueLine("dlg-011", "scene-02", "character-tide-picker", "別碰箱子。", "HIGH", listOf("shot-013")),
                DialogueLine("dlg-012", "scene-02", "character-lu-chenxing", "先救你。", "HIGH", listOf("shot-013")),
            ),
            autoRevisionCount = 0,
            sourceCommitSha = "cli-source",
        )
        return draft.copy(contentSha256 = ProductionRuleRegistry.artifactHash(draft))
    }

    private fun cleanReviews(artifact: DialogueArtifact) = DialogueReviewBatch(
        artifactId = artifact.artifactId,
        reviews = DialogueReviewerRole.entries.map { role ->
            DialogueReviewRun(
                reviewId = "cli-${role.name.lowercase()}",
                artifactId = artifact.artifactId,
                artifactSha256 = artifact.contentSha256,
                ruleSetId = artifact.ruleSetId,
                ruleSetVersion = artifact.ruleSetVersion,
                reviewerRole = role,
                reviewerInstanceId = "cli-agent-${role.name.lowercase()}",
                lineReviews = artifact.lines.map { DialogueLineReview(it.lineId, it.text, note = "逐句確認") },
                overallScore = 90,
                verdict = DialogueReviewVerdict.PASS,
                summary = "自然救援短句可表演且無重大誤讀。",
                sourceCommitSha = "cli-review",
            )
        },
    )

    private inline fun <reified T> write(path: Path, value: T): Path {
        Files.writeString(path, JsonSupport.json.encodeToString(value))
        return path
    }
}
