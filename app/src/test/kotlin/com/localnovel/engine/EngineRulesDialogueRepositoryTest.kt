package com.localnovel.engine

import com.localnovel.engine.model.DialogueApprovalState
import com.localnovel.engine.model.DialogueArtifact
import com.localnovel.engine.model.DialogueArtifactKind
import com.localnovel.engine.model.DialogueFinding
import com.localnovel.engine.model.DialogueFinalizeRequest
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
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.serialization.encodeToString

class EngineRulesDialogueRepositoryTest {
    @Test
    fun migrationIsIdempotentAndInstallsAuthorityTablesAndImmutableGuards() {
        val repository = SqliteStoryRepository(Files.createTempDirectory("engine-rules-migration").resolve("story.db"))
        repository.initialize()
        repository.initialize()
        val tables = repository.withConnection { connection ->
            repository.queryRows(
                connection,
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name IN ('production_rule_sets','production_rules','character_voice_profiles','language_production_profiles','dialogue_artifacts','dialogue_artifact_lines','dialogue_review_runs','dialogue_review_line_coverage','dialogue_line_findings','dialogue_selection_decisions','dialogue_legacy_artifact_audit')",
            ).map { it.getValue("name") }.toSet()
        }
        val triggers = repository.withConnection { connection ->
            repository.queryRows(
                connection,
                "SELECT name FROM sqlite_master WHERE type = 'trigger' AND name LIKE 'trg_dialogue_%'",
            ).map { it.getValue("name") }.toSet()
        }
        assertEquals(11, tables.size)
        assertEquals(
            setOf(
                "trg_dialogue_artifact_state_transition_guard",
                "trg_dialogue_artifact_selectable_guard",
                "trg_dialogue_artifact_insert_state_guard",
                "trg_dialogue_selected_decision_guard",
                "trg_dialogue_selection_decision_update_forbidden",
                "trg_dialogue_selection_decision_delete_forbidden",
                "trg_dialogue_artifact_selected_guard",
                "trg_dialogue_review_run_update_forbidden",
                "trg_dialogue_review_run_delete_forbidden",
                "trg_dialogue_review_run_insert_guard",
                "trg_dialogue_finding_update_forbidden",
                "trg_dialogue_finding_delete_forbidden",
                "trg_dialogue_finding_insert_guard",
                "trg_dialogue_artifact_line_update_forbidden",
                "trg_dialogue_artifact_line_delete_forbidden",
                "trg_dialogue_artifact_line_insert_guard",
                "trg_dialogue_review_line_update_forbidden",
                "trg_dialogue_review_line_delete_forbidden",
                "trg_dialogue_review_line_insert_guard",
                "trg_dialogue_artifact_identity_update_forbidden",
                "trg_dialogue_legacy_audit_update_forbidden",
                "trg_dialogue_legacy_audit_delete_forbidden",
            ),
            triggers,
        )
    }

    @Test
    fun migrationUpgradesAThirteenSchemaAndPreservesExistingData() {
        val db = Files.createTempDirectory("engine-rules-upgrade").resolve("story.db")
        applyMigrationsThrough(db, 13)
        DriverManager.getConnection("jdbc:sqlite:$db").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("CREATE TABLE upgrade_sentinel(value TEXT NOT NULL)")
                statement.executeUpdate("INSERT INTO upgrade_sentinel(value) VALUES ('preserved')")
            }
        }

        val repository = SqliteStoryRepository(db)
        repository.initialize()

        val sentinel = repository.withConnection { connection ->
            repository.queryRows(connection, "SELECT value FROM upgrade_sentinel").single().getValue("value")
        }
        assertEquals("preserved", sentinel)
        assertTrue(tableExists(repository, "production_rule_sets"))
        assertTrue(tableExists(repository, "dialogue_selection_decisions"))
    }

    @Test
    fun migrationFifteenReplacesAnInstalledFourteenSelectionGuard() {
        val db = Files.createTempDirectory("engine-rules-014-to-015").resolve("story.db")
        applyMigrationsThrough(db, 14)
        val legacy = artifact("legacy-pending", DialogueArtifactKind.INITIAL, goodLines())
        val legacyHash = ProductionRuleRegistry.sha256(JsonSupport.json.encodeToString(legacy.lines))
        val legacyArtifact = legacy.copy(contentSha256 = legacyHash)
        insertLegacyArtifact(db, legacyArtifact, "REVIEW_PENDING")
        val before = triggerSql(db, "trg_dialogue_selected_decision_guard")
        assertTrue("dialogue_review_line_coverage" !in before)

        val repository = SqliteStoryRepository(db)
        repository.initialize()

        val after = triggerSql(db, "trg_dialogue_selected_decision_guard")
        assertTrue("dialogue_review_line_coverage" in after)
        assertTrue(tableExists(repository, "dialogue_artifact_lines"))
        assertTrue(tableExists(repository, "dialogue_review_line_coverage"))
        val legacyAudit = repository.withConnection { connection ->
            repository.queryRows(
                connection,
                "SELECT audit_status FROM dialogue_legacy_artifact_audit WHERE artifact_id = ?",
                legacyArtifact.artifactId,
            ).single().getValue("audit_status")
        }
        assertEquals("QUARANTINED_HASH_SCHEMA_V1", legacyAudit)
        val dialogue = DialogueProductionRepository(repository).also { it.bootstrap() }
        assertFailsWith<IllegalArgumentException> { dialogue.recordReviewBatch(reviews(legacyArtifact)) }
    }

    @Test
    fun migrationFailureRollsBackTheEntireFourteenUpgrade() {
        val db = Files.createTempDirectory("engine-rules-migration-rollback").resolve("story.db")
        applyMigrationsThrough(db, 13)
        DriverManager.getConnection("jdbc:sqlite:$db").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("CREATE TABLE idx_dialogue_artifacts_production(value TEXT)")
            }
        }
        val repository = SqliteStoryRepository(db)

        assertFailsWith<Exception> { repository.initialize() }
        assertTrue(!tableExists(repository, "production_rule_sets"))
        assertTrue(!tableExists(repository, "dialogue_selection_decisions"))
    }

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
    fun earlyNotSelectableDecisionDoesNotPoisonLaterValidReview() {
        val fixture = fixture()
        val artifact = artifact("early-selection", DialogueArtifactKind.INITIAL, goodLines())
        fixture.dialogue.ingestArtifact(artifact)

        val early = fixture.dialogue.select(selection(artifact, "early-not-selectable"))
        assertEquals(DialogueSelectionStatus.NOT_SELECTABLE, early.status)
        assertEquals(DialogueApprovalState.REVIEW_PENDING, fixture.dialogue.get(artifact.artifactId).approvalState)

        fixture.dialogue.recordReviewBatch(reviews(artifact))
        val selected = fixture.dialogue.select(selection(artifact, "later-selected"))
        assertEquals(DialogueSelectionStatus.SELECTED, selected.status)
        assertEquals(DialogueApprovalState.SELECTED, fixture.dialogue.get(artifact.artifactId).approvalState)
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
        assertNotEquals(initial.contentSha256, sameContentRevision.contentSha256)
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
        fixture.repository.withTransaction { connection ->
            fixture.repository.executeUpdate(
                connection,
                """
                INSERT INTO dialogue_selection_decisions(
                    decision_id, production_id, artifact_id, selected_artifact_sha256,
                    rule_set_id, rule_set_version, decision_status, blocker_count, warning_count,
                    reasons_json, decision_actor_id, source_commit_sha, decision_sha256, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, 'NOT_SELECTABLE', 0, 0, '[]', ?, ?, ?, ?)
                """.trimIndent(),
                "raw-update-bypass", artifact.productionId, artifact.artifactId, artifact.contentSha256,
                artifact.ruleSetId, artifact.ruleSetVersion, "attacker", "bad-commit", "1".repeat(64), "now",
            )
        }
        assertFailsWith<Exception> {
            fixture.repository.withTransaction { connection ->
                fixture.repository.executeUpdate(
                    connection,
                    "UPDATE dialogue_selection_decisions SET decision_status = 'SELECTED' WHERE decision_id = 'raw-update-bypass'",
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

    @Test
    fun databaseRejectsFakeReviewsWithoutNormalizedCoverage() {
        val fixture = fixture()
        val artifact = artifact("fake-review-bypass", DialogueArtifactKind.INITIAL, goodLines())
        fixture.dialogue.ingestArtifact(artifact)
        val fakeReviews = reviews(artifact).reviews
        fixture.repository.withTransaction { connection ->
            fakeReviews.forEachIndexed { index, review ->
                fixture.repository.executeUpdate(
                    connection,
                    """
                    INSERT INTO dialogue_review_runs(
                        review_id, artifact_id, artifact_sha256, rule_set_id, rule_set_version,
                        reviewer_role, reviewer_instance_id, overall_score, verdict, review_json,
                        source_commit_sha, created_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, 100, 'PASS', ?, 'attacker', 'now')
                    """.trimIndent(),
                    "fake-review-$index",
                    artifact.artifactId,
                    artifact.contentSha256,
                    artifact.ruleSetId,
                    artifact.ruleSetVersion,
                    review.reviewerRole.name,
                    "fake-instance-$index",
                    JsonSupport.json.encodeToString(review),
                )
            }
        }

        assertFailsWith<Exception> {
            fixture.repository.withTransaction { connection ->
                fixture.repository.executeUpdate(
                    connection,
                    "UPDATE dialogue_artifacts SET approval_state = 'SELECTABLE' WHERE artifact_id = ?",
                    artifact.artifactId,
                )
            }
        }
        assertEquals(DialogueApprovalState.REVIEW_PENDING, fixture.dialogue.get(artifact.artifactId).approvalState)
    }

    @Test
    fun databaseRejectsVacuousZeroLineCoverageBypass() {
        val fixture = fixture()
        fixture.repository.withTransaction { connection ->
            fixture.repository.executeUpdate(
                connection,
                """
                INSERT INTO dialogue_artifacts(
                    artifact_id, production_id, artifact_kind, parent_artifact_sha256, content_sha256,
                    content_json, rule_set_id, rule_set_version, character_profile_refs_json,
                    language_profile_ref, approval_state, auto_revision_count, boundary,
                    source_commit_sha, created_at, updated_at, hash_schema_version
                ) VALUES ('zero-lines', 'dialogue02-test', 'INITIAL', '', ?, ?, ?, ?, '[]', ?,
                          'REVIEW_PENDING', 0, 'NON_CANON_PILOT', 'attacker', 'now', 'now', 2)
                """.trimIndent(),
                "9".repeat(64),
                "{\"lines\":[]}",
                ProductionRuleRegistry.RULE_SET_ID,
                ProductionRuleRegistry.RULE_SET_VERSION,
                ProductionRuleRegistry.LANGUAGE_PROFILE_ID,
            )
            DialogueReviewerRole.entries.forEachIndexed { index, role ->
                fixture.repository.executeUpdate(
                    connection,
                    """
                    INSERT INTO dialogue_review_runs(
                        review_id, artifact_id, artifact_sha256, rule_set_id, rule_set_version,
                        reviewer_role, reviewer_instance_id, overall_score, verdict, review_json,
                        source_commit_sha, created_at
                    ) VALUES (?, 'zero-lines', ?, ?, ?, ?, ?, 100, 'PASS', '{\"lineReviews\":[]}', 'attacker', 'now')
                    """.trimIndent(),
                    "zero-review-$index",
                    "9".repeat(64),
                    ProductionRuleRegistry.RULE_SET_ID,
                    ProductionRuleRegistry.RULE_SET_VERSION,
                    role.name,
                    "zero-instance-$index",
                )
            }
        }

        assertFailsWith<Exception> {
            fixture.repository.withTransaction { connection ->
                fixture.repository.executeUpdate(
                    connection,
                    "UPDATE dialogue_artifacts SET approval_state = 'SELECTABLE' WHERE artifact_id = 'zero-lines'",
                )
            }
        }
    }

    @Test
    fun databaseRejectsMissingJsonArrayPathsEvenWithNormalizedRows() {
        val fixture = fixture()
        insertRawCurrentArtifact(fixture, "missing-artifact-lines-json", "{}")
        fixture.repository.withTransaction { connection ->
            fixture.repository.executeUpdate(
                connection,
                "INSERT INTO dialogue_artifact_lines(artifact_id, line_id, exact_quote) VALUES ('missing-artifact-lines-json', 'line-1', '一句。')",
            )
        }
        assertFailsWith<Exception> {
            fixture.repository.withTransaction { connection ->
                fixture.repository.executeUpdate(
                    connection,
                    "UPDATE dialogue_artifacts SET approval_state = 'SELECTABLE' WHERE artifact_id = 'missing-artifact-lines-json'",
                )
            }
        }

        insertRawCurrentArtifact(
            fixture,
            "missing-review-lines-json",
            "{\"lines\":[{\"lineId\":\"line-1\",\"text\":\"一句。\"}]}",
        )
        fixture.repository.withTransaction { connection ->
            fixture.repository.executeUpdate(
                connection,
                "INSERT INTO dialogue_artifact_lines(artifact_id, line_id, exact_quote) VALUES ('missing-review-lines-json', 'line-1', '一句。')",
            )
            DialogueReviewerRole.entries.forEachIndexed { index, role ->
                val reviewId = "missing-json-review-$index"
                fixture.repository.executeUpdate(
                    connection,
                    """
                    INSERT INTO dialogue_review_runs(
                        review_id, artifact_id, artifact_sha256, rule_set_id, rule_set_version,
                        reviewer_role, reviewer_instance_id, overall_score, verdict, review_json,
                        source_commit_sha, created_at
                    ) VALUES (?, 'missing-review-lines-json', ?, ?, ?, ?, ?, 100, 'PASS', '{}', 'attacker', 'now')
                    """.trimIndent(),
                    reviewId,
                    "8".repeat(64),
                    ProductionRuleRegistry.RULE_SET_ID,
                    ProductionRuleRegistry.RULE_SET_VERSION,
                    role.name,
                    "missing-json-instance-$index",
                )
                fixture.repository.executeUpdate(
                    connection,
                    "INSERT INTO dialogue_review_line_coverage(review_id, line_id, exact_quote) VALUES (?, 'line-1', '一句。')",
                    reviewId,
                )
            }
        }
        assertFailsWith<Exception> {
            fixture.repository.withTransaction { connection ->
                fixture.repository.executeUpdate(
                    connection,
                    "UPDATE dialogue_artifacts SET approval_state = 'SELECTABLE' WHERE artifact_id = 'missing-review-lines-json'",
                )
            }
        }
    }

    @Test
    fun selectedEvidenceRejectsAllLateInsertions() {
        val fixture = fixture()
        val artifact = artifact("selected-immutable", DialogueArtifactKind.INITIAL, goodLines())
        fixture.dialogue.ingestArtifact(artifact)
        fixture.dialogue.recordReviewBatch(reviews(artifact))
        assertEquals(DialogueSelectionStatus.SELECTED, fixture.dialogue.select(selection(artifact, "selected-immutable-decision")).status)
        val reviewId = reviews(artifact).reviews.first().reviewId

        assertFailsWith<Exception> {
            fixture.repository.withTransaction { connection ->
                fixture.repository.executeUpdate(
                    connection,
                    "INSERT INTO dialogue_artifact_lines(artifact_id, line_id, exact_quote) VALUES (?, 'late-line', '晚到。')",
                    artifact.artifactId,
                )
            }
        }
        assertFailsWith<Exception> {
            fixture.repository.withTransaction { connection ->
                fixture.repository.executeUpdate(
                    connection,
                    "INSERT INTO dialogue_review_line_coverage(review_id, line_id, exact_quote) VALUES (?, 'late-line', '晚到。')",
                    reviewId,
                )
            }
        }
        assertFailsWith<Exception> {
            fixture.repository.withTransaction { connection ->
                fixture.repository.executeUpdate(
                    connection,
                    """
                    INSERT INTO dialogue_line_findings(
                        finding_id, review_id, line_id, exact_quote, rule_code, severity,
                        explanation, suggested_text, requires_human_approval, created_at
                    ) VALUES ('late-blocker', ?, 'dlg-012', '先救你。', 'FORCED_WIT_UNDER_DANGER',
                              'BLOCKER', 'late mutation', '', 0, 'now')
                    """.trimIndent(),
                    reviewId,
                )
            }
        }
    }

    @Test
    fun reviewVerdictAndHumanApprovalCannotBeIgnored() {
        val revisionFixture = fixture()
        val reviseArtifact = artifact("verdict-revise", DialogueArtifactKind.INITIAL, goodLines())
        revisionFixture.dialogue.ingestArtifact(reviseArtifact)
        val reviseBatch = reviews(reviseArtifact).let { batch ->
            batch.copy(reviews = batch.reviews.map { it.copy(verdict = DialogueReviewVerdict.REVISE) })
        }
        revisionFixture.dialogue.recordReviewBatch(reviseBatch)
        val reviseDecision = revisionFixture.dialogue.select(selection(reviseArtifact, "verdict-revise-decision"))
        assertEquals(DialogueSelectionStatus.NOT_SELECTABLE, reviseDecision.status)
        assertTrue("REVIEW_REQUIRES_REVISION" in reviseDecision.reasons)
        assertEquals(DialogueApprovalState.REVISE_REQUIRED, revisionFixture.dialogue.get(reviseArtifact.artifactId).approvalState)

        val humanFixture = fixture()
        val humanArtifact = artifact("human-approval", DialogueArtifactKind.INITIAL, goodLines())
        humanFixture.dialogue.ingestArtifact(humanArtifact)
        val humanFinding = DialogueFinding(
            findingId = "human-warning",
            lineId = humanArtifact.lines.first().lineId,
            exactQuote = humanArtifact.lines.first().text,
            ruleCode = ProductionRuleCode.UNSUPPORTED_REGIONAL_TERM,
            severity = ProductionRuleSeverity.WARNING,
            explanation = "此詞必須由人類決定是否保留。",
            requiresHumanApproval = true,
        )
        val humanBatch = reviews(humanArtifact).let { batch ->
            batch.copy(
                reviews = batch.reviews.mapIndexed { index, review ->
                    if (index == 0) review.copy(
                        verdict = DialogueReviewVerdict.HUMAN_DECISION_REQUIRED,
                        lineReviews = review.lineReviews.mapIndexed { lineIndex, line ->
                            if (lineIndex == 0) line.copy(findings = listOf(humanFinding)) else line
                        },
                    ) else review
                },
            )
        }
        humanFixture.dialogue.recordReviewBatch(humanBatch)
        val humanDecision = humanFixture.dialogue.select(selection(humanArtifact, "human-approval-decision"))
        assertEquals(DialogueSelectionStatus.HUMAN_DECISION_REQUIRED, humanDecision.status)
        assertTrue("FINDING_REQUIRES_HUMAN_APPROVAL" in humanDecision.reasons)
    }

    @Test
    fun finalizationRollsBackReviewsWhenSelectionRequestIsInvalid() {
        val fixture = fixture()
        val artifact = artifact("atomic-finalize", DialogueArtifactKind.INITIAL, goodLines())
        fixture.dialogue.ingestArtifact(artifact)
        val invalid = DialogueFinalizeRequest(
            reviewBatch = reviews(artifact),
            selection = selection(artifact, "atomic-decision").copy(decisionActorId = ""),
        )

        assertFailsWith<IllegalArgumentException> { fixture.dialogue.finalize(invalid) }
        assertEquals(0, tableCount(fixture.repository, "dialogue_review_runs", "artifact_id", artifact.artifactId))
        assertEquals(DialogueApprovalState.REVIEW_PENDING, fixture.dialogue.get(artifact.artifactId).approvalState)
    }

    @Test
    fun artifactHashBindsIdentityLifecycleAndSourceMetadata() {
        val artifact = artifact("hash-identity", DialogueArtifactKind.INITIAL, goodLines())
        val changedSource = artifact.copy(contentSha256 = "", sourceCommitSha = "different-source")
        val changedProfiles = artifact.copy(
            contentSha256 = "",
            characterVoiceProfileIds = artifact.characterVoiceProfileIds.reversed(),
        )

        assertNotEquals(artifact.contentSha256, ProductionRuleRegistry.artifactHash(changedSource))
        assertEquals(artifact.contentSha256, ProductionRuleRegistry.artifactHash(changedProfiles))
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
        val ruleSet = ProductionRuleRegistry.activeRuleSet()
        val profileIds = listOf("voice-lu-chenxing-v1", "voice-tide-picker-v1")
        val profileHashes = ProductionRuleRegistry.characterProfiles()
            .filter { it.profileId in profileIds }
            .associate { it.profileId to it.profileSha256 }
        val language = ProductionRuleRegistry.languageProfile()
        val draft = DialogueArtifact(
            artifactId = id,
            productionId = "dialogue02-test",
            artifactKind = kind,
            parentArtifactSha256 = parentHash,
            contentSha256 = "",
            ruleSetId = ProductionRuleRegistry.RULE_SET_ID,
            ruleSetVersion = ProductionRuleRegistry.RULE_SET_VERSION,
            ruleSetContentSha256 = ruleSet.contentSha256,
            characterVoiceProfileIds = profileIds,
            characterVoiceProfileHashes = profileHashes,
            languageProfileId = ProductionRuleRegistry.LANGUAGE_PROFILE_ID,
            languageProfileSha256 = language.profileSha256,
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

    private fun tableExists(repository: SqliteStoryRepository, table: String): Boolean = repository.withConnection { connection ->
        repository.queryRows(
            connection,
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
            table,
        ).isNotEmpty()
    }

    private fun applyMigrationsThrough(db: Path, maximum: Int) {
        DriverManager.getConnection("jdbc:sqlite:$db").use { connection ->
            connection.autoCommit = false
            try {
                connection.createStatement().use { statement ->
                    (1..maximum).forEach { number ->
                        val fileName = "%03d".format(number) + when (number) {
                            1 -> "_init.sql"
                            2 -> "_multi_turn_runtime.sql"
                            3 -> "_memory_knowledge_runtime.sql"
                            4 -> "_scene_thread_runtime.sql"
                            5 -> "_setting_pack_runtime.sql"
                            6 -> "_checkpoint_branching_runtime.sql"
                            7 -> "_world_source_archive.sql"
                            8 -> "_ai_task_queue.sql"
                            9 -> "_provisional_layer.sql"
                            10 -> "_reboot_run_evidence.sql"
                            11 -> "_npc_autonomy_runtime.sql"
                            12 -> "_reboot_drama_evaluation.sql"
                            13 -> "_story_chapter_production.sql"
                            14 -> "_production_rule_runtime.sql"
                            else -> error("Unsupported migration fixture: $number")
                        }
                        val text = checkNotNull(javaClass.classLoader.getResource("migrations/$fileName")).readText()
                        splitMigrationStatements(text).forEach(statement::executeUpdate)
                    }
                }
                connection.commit()
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            }
        }
    }

    private fun triggerSql(db: Path, triggerName: String): String = DriverManager.getConnection("jdbc:sqlite:$db").use { connection ->
        connection.prepareStatement("SELECT sql FROM sqlite_master WHERE type = 'trigger' AND name = ?").use { statement ->
            statement.setString(1, triggerName)
            statement.executeQuery().use { rows ->
                check(rows.next()) { "Missing trigger: $triggerName" }
                rows.getString("sql")
            }
        }
    }

    private fun insertLegacyArtifact(db: Path, artifact: DialogueArtifact, state: String) {
        DriverManager.getConnection("jdbc:sqlite:$db").use { connection ->
            connection.prepareStatement(
                """
                INSERT INTO dialogue_artifacts(
                    artifact_id, production_id, artifact_kind, parent_artifact_sha256, content_sha256,
                    content_json, rule_set_id, rule_set_version, character_profile_refs_json,
                    language_profile_ref, approval_state, auto_revision_count, boundary,
                    source_commit_sha, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'legacy', 'legacy')
                """.trimIndent(),
            ).use { statement ->
                val values = listOf(
                    artifact.artifactId,
                    artifact.productionId,
                    artifact.artifactKind.name,
                    artifact.parentArtifactSha256,
                    artifact.contentSha256,
                    JsonSupport.json.encodeToString(artifact),
                    artifact.ruleSetId,
                    artifact.ruleSetVersion,
                    JsonSupport.json.encodeToString(artifact.characterVoiceProfileIds),
                    artifact.languageProfileId,
                    state,
                    artifact.autoRevisionCount.toString(),
                    artifact.boundary,
                    artifact.sourceCommitSha,
                )
                values.forEachIndexed { index, value -> statement.setString(index + 1, value) }
                statement.executeUpdate()
            }
        }
    }

    private fun insertRawCurrentArtifact(fixture: Fixture, artifactId: String, contentJson: String) {
        fixture.repository.withTransaction { connection ->
            fixture.repository.executeUpdate(
                connection,
                """
                INSERT INTO dialogue_artifacts(
                    artifact_id, production_id, artifact_kind, parent_artifact_sha256, content_sha256,
                    content_json, rule_set_id, rule_set_version, character_profile_refs_json,
                    language_profile_ref, approval_state, auto_revision_count, boundary,
                    source_commit_sha, created_at, updated_at, hash_schema_version
                ) VALUES (?, 'dialogue02-test', 'INITIAL', '', ?, ?, ?, ?, '[]', ?,
                          'REVIEW_PENDING', 0, 'NON_CANON_PILOT', 'attacker', 'now', 'now', 2)
                """.trimIndent(),
                artifactId,
                if (artifactId.contains("review")) "8".repeat(64) else "7".repeat(64),
                contentJson,
                ProductionRuleRegistry.RULE_SET_ID,
                ProductionRuleRegistry.RULE_SET_VERSION,
                ProductionRuleRegistry.LANGUAGE_PROFILE_ID,
            )
        }
    }

    private fun splitMigrationStatements(sql: String): List<String> {
        val statements = mutableListOf<String>()
        val buffer = StringBuilder()
        var trigger = false
        sql.lineSequence().filterNot { it.trimStart().startsWith("--") }.forEach { line ->
            if (buffer.isNotEmpty()) buffer.append('\n')
            buffer.append(line)
            if (!trigger && buffer.toString().trimStart().startsWith("CREATE TRIGGER", ignoreCase = true)) trigger = true
            if (trigger && line.trim().equals("END;", ignoreCase = true)) {
                statements += buffer.toString().trim().removeSuffix(";")
                buffer.clear()
                trigger = false
            } else if (!trigger) {
                while (';' in buffer) {
                    val statement = buffer.substring(0, buffer.indexOf(";")).trim()
                    if (statement.isNotEmpty()) statements += statement
                    buffer.delete(0, buffer.indexOf(";") + 1)
                }
            }
        }
        buffer.toString().trim().takeIf(String::isNotEmpty)?.let(statements::add)
        return statements
    }

    private data class Fixture(val repository: SqliteStoryRepository, val dialogue: DialogueProductionRepository)
}
