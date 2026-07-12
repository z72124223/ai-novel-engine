package com.localnovel.engine

import com.localnovel.engine.model.DialogueApprovalState
import com.localnovel.engine.model.DialogueArtifact
import com.localnovel.engine.model.DialogueArtifactKind
import com.localnovel.engine.model.DialogueFinding
import com.localnovel.engine.model.DialogueLine
import com.localnovel.engine.model.DialogueLineReview
import com.localnovel.engine.model.DialogueReviewBatch
import com.localnovel.engine.model.DialogueReviewRun
import com.localnovel.engine.model.DialogueReviewerRole
import com.localnovel.engine.model.DialogueReviewVerdict
import com.localnovel.engine.model.DialogueSelectionRequest
import com.localnovel.engine.model.DialogueSelectionStatus
import com.localnovel.engine.model.ProductionRuleCode
import com.localnovel.engine.model.ProductionRuleSeverity
import com.localnovel.engine.repository.DialogueProductionRepository
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.ProductionRuleRegistry
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EngineRulesDialogueRepositoryTest {
    @Test
    fun blockerCannotBeHiddenByHighAverageScore() {
        val fixture = fixture()
        val artifact = artifact("initial-bad", DialogueArtifactKind.INITIAL, badLines())
        fixture.dialogue.ingestArtifact(artifact)
        fixture.dialogue.recordReviewBatch(reviews(artifact, includeCanonicalBlockers = true, score = 95))

        val decision = fixture.dialogue.select(selection(artifact, "decision-bad"))

        assertEquals(DialogueSelectionStatus.NOT_SELECTABLE, decision.status)
        assertEquals(3, decision.blockerCount)
        assertTrue("BLOCKER_PRESENT" in decision.reasons)
        assertEquals(DialogueApprovalState.REVISE_REQUIRED, fixture.dialogue.get(artifact.artifactId).approvalState)
    }

    @Test
    fun revisedArtifactRequiresFreshReviewsAndCanBeSelectedWhenClean() {
        val fixture = fixture()
        val initial = artifact("initial", DialogueArtifactKind.INITIAL, badLines())
        fixture.dialogue.ingestArtifact(initial)
        fixture.dialogue.recordReviewBatch(reviews(initial, includeCanonicalBlockers = true))
        val revised = artifact(
            id = "revised",
            kind = DialogueArtifactKind.REVISED,
            lines = goodLines(),
            parentHash = initial.contentSha256,
            revisionCount = 1,
        )
        fixture.dialogue.ingestArtifact(revised)

        assertFailsWith<IllegalArgumentException> {
            fixture.dialogue.recordReviewBatch(reviews(initial, includeCanonicalBlockers = true).copy(artifactId = revised.artifactId))
        }
        assertEquals(0, tableCount(fixture.repository, "dialogue_review_runs", "artifact_id", revised.artifactId))

        fixture.dialogue.recordReviewBatch(reviews(revised))
        val decision = fixture.dialogue.select(selection(revised, "decision-good"))
        assertEquals(DialogueSelectionStatus.SELECTED, decision.status)
        assertEquals(DialogueApprovalState.SELECTED, fixture.dialogue.get(revised.artifactId).approvalState)
    }

    @Test
    fun reviewBatchFailsClosedAndRollsBack() {
        val fixture = fixture()
        val artifact = artifact("rollback", DialogueArtifactKind.INITIAL, goodLines())
        fixture.dialogue.ingestArtifact(artifact)
        val valid = reviews(artifact)

        val missingReviewer = valid.copy(reviews = valid.reviews.dropLast(1))
        assertFailsWith<IllegalArgumentException> { fixture.dialogue.recordReviewBatch(missingReviewer) }
        assertEquals(0, tableCount(fixture.repository, "dialogue_review_runs", "artifact_id", artifact.artifactId))

        val duplicateInstance = valid.copy(
            reviews = valid.reviews.map { it.copy(reviewerInstanceId = "same-agent") },
        )
        assertFailsWith<IllegalArgumentException> { fixture.dialogue.recordReviewBatch(duplicateInstance) }
        assertEquals(0, tableCount(fixture.repository, "dialogue_review_runs", "artifact_id", artifact.artifactId))

        val badQuote = valid.copy(
            reviews = valid.reviews.mapIndexed { index, review ->
                if (index == 1) review.copy(
                    lineReviews = review.lineReviews.mapIndexed { lineIndex, line ->
                        if (lineIndex == 0) line.copy(exactQuote = "不存在的引句") else line
                    },
                ) else review
            },
        )
        assertFailsWith<IllegalArgumentException> { fixture.dialogue.recordReviewBatch(badQuote) }
        assertEquals(0, tableCount(fixture.repository, "dialogue_review_runs", "artifact_id", artifact.artifactId))
    }

    @Test
    fun hardBlockerCannotBeDowngradedAndFixtureCannotBeOmitted() {
        val fixture = fixture()
        val artifact = artifact("hard", DialogueArtifactKind.INITIAL, badLines())
        fixture.dialogue.ingestArtifact(artifact)
        assertFailsWith<IllegalArgumentException> { fixture.dialogue.recordReviewBatch(reviews(artifact)) }

        val downgraded = reviews(artifact, includeCanonicalBlockers = true).let { batch ->
            batch.copy(
                reviews = batch.reviews.map { review ->
                    review.copy(
                        lineReviews = review.lineReviews.map { line ->
                            line.copy(findings = line.findings.map { it.copy(severity = ProductionRuleSeverity.WARNING) })
                        },
                    )
                },
            )
        }
        assertFailsWith<IllegalArgumentException> { fixture.dialogue.recordReviewBatch(downgraded) }
        assertEquals(0, tableCount(fixture.repository, "dialogue_review_runs", "artifact_id", artifact.artifactId))
    }

    @Test
    fun rejectsSameContentRevisionAndWrongRuleSetReviews() {
        val fixture = fixture()
        val initial = artifact("same-initial", DialogueArtifactKind.INITIAL, badLines())
        fixture.dialogue.ingestArtifact(initial)
        fixture.dialogue.recordReviewBatch(reviews(initial, includeCanonicalBlockers = true))
        val sameContentRevision = artifact(
            "same-revision",
            DialogueArtifactKind.REVISED,
            badLines(),
            parentHash = initial.contentSha256,
            revisionCount = 1,
        )
        assertEquals(initial.contentSha256, sameContentRevision.contentSha256)
        assertFailsWith<IllegalArgumentException> { fixture.dialogue.ingestArtifact(sameContentRevision) }

        val clean = artifact("wrong-ruleset", DialogueArtifactKind.INITIAL, goodLines())
        fixture.dialogue.ingestArtifact(clean)
        val wrong = reviews(clean).let { batch ->
            batch.copy(reviews = batch.reviews.mapIndexed { index, review -> if (index == 0) review.copy(ruleSetVersion = "9.9.9") else review })
        }
        assertFailsWith<IllegalArgumentException> { fixture.dialogue.recordReviewBatch(wrong) }
        assertEquals(0, tableCount(fixture.repository, "dialogue_review_runs", "artifact_id", clean.artifactId))
    }

    @Test
    fun warningOnlyRegionalTermRemainsSelectable() {
        val fixture = fixture()
        val artifact = artifact("regional-warning", DialogueArtifactKind.INITIAL, goodLines())
        fixture.dialogue.ingestArtifact(artifact)
        val warning = DialogueFinding(
            findingId = "regional-warning-119",
            lineId = artifact.lines.first().lineId,
            exactQuote = artifact.lines.first().text,
            ruleCode = ProductionRuleCode.UNSUPPORTED_REGIONAL_TERM,
            severity = ProductionRuleSeverity.WARNING,
            explanation = "台灣地域詞需要在地化 metadata，但不造成母稿阻斷。",
        )
        val batch = reviews(artifact).let { clean ->
            clean.copy(
                reviews = clean.reviews.mapIndexed { index, review ->
                    if (index == 2) review.copy(
                        lineReviews = review.lineReviews.mapIndexed { lineIndex, line ->
                            if (lineIndex == 0) line.copy(findings = listOf(warning)) else line
                        },
                    ) else review
                },
            )
        }
        fixture.dialogue.recordReviewBatch(batch)
        val decision = fixture.dialogue.select(selection(artifact, "regional-decision"))
        assertEquals(DialogueSelectionStatus.SELECTED, decision.status)
        assertEquals(1, decision.warningCount)
    }

    @Test
    fun databaseTriggersRejectDirectSelectionBypass() {
        val fixture = fixture()
        val artifact = artifact("bypass", DialogueArtifactKind.INITIAL, goodLines())
        fixture.dialogue.ingestArtifact(artifact)

        assertFailsWith<Exception> {
            fixture.repository.withTransaction { connection ->
                fixture.repository.executeUpdate(
                    connection,
                    """
                    INSERT INTO dialogue_selection_decisions(
                        decision_id, production_id, artifact_id, selected_artifact_sha256,
                        rule_set_id, rule_set_version, decision_status, blocker_count, warning_count,
                        reasons_json, decision_actor_id, source_commit_sha, decision_sha256, created_at
                    ) VALUES (?, ?, ?, ?, ?, ?, 'SELECTED', 0, 0, '[]', ?, ?, ?, ?)
                    """.trimIndent(),
                    "raw-bypass", artifact.productionId, artifact.artifactId, artifact.contentSha256,
                    artifact.ruleSetId, artifact.ruleSetVersion, "attacker", "bad-commit", "0".repeat(64), "now",
                )
            }
        }
        assertFailsWith<Exception> {
            fixture.repository.withTransaction { connection ->
                fixture.repository.executeUpdate(
                    connection,
                    "UPDATE dialogue_artifacts SET approval_state = 'SELECTED' WHERE artifact_id = ?",
                    artifact.artifactId,
                )
            }
        }
        assertEquals(DialogueApprovalState.REVIEW_PENDING, fixture.dialogue.get(artifact.artifactId).approvalState)
    }

    private fun fixture(): Fixture {
        val repository = SqliteStoryRepository(Files.createTempDirectory("engine-rules01").resolve("story.db"))
        repository.initialize()
        val dialogue = DialogueProductionRepository(repository)
        dialogue.bootstrap()
        return Fixture(repository, dialogue)
    }

    private fun artifact(
        id: String,
        kind: DialogueArtifactKind,
        lines: List<DialogueLine>,
        parentHash: String = "",
        revisionCount: Int = 0,
    ): DialogueArtifact {
        val draft = DialogueArtifact(
            artifactId = id,
            productionId = "dialogue02-test",
            artifactKind = kind,
            parentArtifactSha256 = parentHash,
            contentSha256 = "",
            ruleSetId = ProductionRuleRegistry.RULE_SET_ID,
            ruleSetVersion = ProductionRuleRegistry.RULE_SET_VERSION,
            characterVoiceProfileIds = listOf("voice-lu-chenxing-v1", "voice-tide-picker-v1"),
            languageProfileId = ProductionRuleRegistry.LANGUAGE_PROFILE_ID,
            lines = lines,
            autoRevisionCount = revisionCount,
            sourceCommitSha = "test-commit",
        )
        return draft.copy(contentSha256 = ProductionRuleRegistry.artifactHash(draft))
    }

    private fun badLines() = listOf(
        DialogueLine("dlg-011", "scene-02", "character-tide-picker", "別碰箱子。", "HIGH"),
        DialogueLine("dlg-012", "scene-02", "character-lu-chenxing", "好。先碰你。", "HIGH"),
    )

    private fun goodLines() = listOf(
        DialogueLine("dlg-011", "scene-02", "character-tide-picker", "別碰箱子。", "HIGH"),
        DialogueLine("dlg-012", "scene-02", "character-lu-chenxing", "先救你。", "HIGH"),
    )

    private fun reviews(
        artifact: DialogueArtifact,
        includeCanonicalBlockers: Boolean = false,
        score: Int = 90,
    ): DialogueReviewBatch {
        val blockers = if (includeCanonicalBlockers) ProductionRuleRegistry.canonicalFixtureFindings(artifact) else emptyList()
        return DialogueReviewBatch(
            artifactId = artifact.artifactId,
            reviews = DialogueReviewerRole.entries.mapIndexed { index, role ->
                val lineReviews = artifact.lines.map { line ->
                    DialogueLineReview(
                        lineId = line.lineId,
                        exactQuote = line.text,
                        findings = if (index == 0) blockers.filter { it.lineId == line.lineId } else emptyList(),
                        note = "完整逐句覆蓋",
                    )
                }
                DialogueReviewRun(
                    reviewId = "${artifact.artifactId}-${role.name.lowercase()}",
                    artifactId = artifact.artifactId,
                    artifactSha256 = artifact.contentSha256,
                    ruleSetId = artifact.ruleSetId,
                    ruleSetVersion = artifact.ruleSetVersion,
                    reviewerRole = role,
                    reviewerInstanceId = "agent-${role.name.lowercase()}",
                    lineReviews = lineReviews,
                    overallScore = score,
                    verdict = if (lineReviews.flatMap { it.findings }.any { it.severity == ProductionRuleSeverity.BLOCKER }) DialogueReviewVerdict.REVISE else DialogueReviewVerdict.PASS,
                    summary = "獨立完整評審",
                    sourceCommitSha = "review-commit",
                )
            },
        )
    }

    private fun selection(artifact: DialogueArtifact, decisionId: String) = DialogueSelectionRequest(
        decisionId = decisionId,
        artifactId = artifact.artifactId,
        decisionActorId = "codex-engine-rules01",
        sourceCommitSha = "selection-commit",
    )

    private fun tableCount(repository: SqliteStoryRepository, table: String, column: String, value: String): Int =
        repository.withConnection { connection ->
            repository.queryRows(connection, "SELECT COUNT(*) AS count FROM $table WHERE $column = ?", value)
                .single().getValue("count").orEmpty().toInt()
        }

    private data class Fixture(val repository: SqliteStoryRepository, val dialogue: DialogueProductionRepository)
}
