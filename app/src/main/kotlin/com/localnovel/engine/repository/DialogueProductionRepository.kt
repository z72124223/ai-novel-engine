package com.localnovel.engine.repository

import com.localnovel.engine.model.CharacterVoiceProfile
import com.localnovel.engine.model.DialogueApprovalState
import com.localnovel.engine.model.DialogueArtifact
import com.localnovel.engine.model.DialogueArtifactKind
import com.localnovel.engine.model.DialogueArtifactView
import com.localnovel.engine.model.DialogueFinding
import com.localnovel.engine.model.DialogueReviewBatch
import com.localnovel.engine.model.DialogueReviewRun
import com.localnovel.engine.model.DialogueReviewerRole
import com.localnovel.engine.model.DialogueReviewVerdict
import com.localnovel.engine.model.DialogueSelectionDecision
import com.localnovel.engine.model.DialogueSelectionRequest
import com.localnovel.engine.model.DialogueSelectionStatus
import com.localnovel.engine.model.LanguageProductionProfile
import com.localnovel.engine.model.ProductionRuleSet
import com.localnovel.engine.model.ProductionRuleSeverity
import com.localnovel.engine.service.ProductionRuleRegistry
import com.localnovel.engine.support.JsonSupport
import java.sql.Connection
import java.time.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class DialogueProductionRepository(private val repository: SqliteStoryRepository) {
    fun bootstrap(
        ruleSet: ProductionRuleSet = ProductionRuleRegistry.activeRuleSet(),
        characterProfiles: List<CharacterVoiceProfile> = ProductionRuleRegistry.characterProfiles(),
        languageProfile: LanguageProductionProfile = ProductionRuleRegistry.languageProfile(),
    ) = repository.withTransaction { connection ->
        require(ruleSet.contentSha256 == ProductionRuleRegistry.sha256(JsonSupport.json.encodeToString(ruleSet.copy(contentSha256 = "")))) {
            "Rule Set content SHA-256 mismatch."
        }
        val timestamp = now()
        repository.executeUpdate(
            connection,
            """
            INSERT OR IGNORE INTO production_rule_sets(
                rule_set_id, rule_set_version, content_sha256, status, rule_set_json, created_at
            ) VALUES (?, ?, ?, 'ACTIVE', ?, ?)
            """.trimIndent(),
            ruleSet.ruleSetId,
            ruleSet.version,
            ruleSet.contentSha256,
            JsonSupport.json.encodeToString(ruleSet),
            timestamp,
        )
        val persistedHash = repository.queryRows(
            connection,
            "SELECT content_sha256 FROM production_rule_sets WHERE rule_set_id = ? AND rule_set_version = ?",
            ruleSet.ruleSetId,
            ruleSet.version,
        ).single().getValue("content_sha256")
        require(persistedHash == ruleSet.contentSha256) { "Rule Set identity already exists with different content." }

        ruleSet.rules.forEach { rule ->
            repository.executeUpdate(
                connection,
                """
                INSERT OR IGNORE INTO production_rules(
                    rule_set_id, rule_set_version, rule_code, category, default_severity,
                    hard_blocker, description
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                ruleSet.ruleSetId,
                ruleSet.version,
                rule.code.name,
                rule.category.name,
                rule.defaultSeverity.name,
                if (rule.hardBlocker) "1" else "0",
                rule.description,
            )
        }
        require(
            repository.queryRows(
                connection,
                "SELECT rule_code FROM production_rules WHERE rule_set_id = ? AND rule_set_version = ?",
                ruleSet.ruleSetId,
                ruleSet.version,
            ).map { it.getValue("rule_code") }.toSet() == ruleSet.rules.map { it.code.name }.toSet(),
        ) { "Persisted Rule Set is incomplete." }

        characterProfiles.forEach { profile ->
            require(profile.ruleSetId == ruleSet.ruleSetId && profile.ruleSetVersion == ruleSet.version)
            require(profile.profileSha256 == ProductionRuleRegistry.sha256(JsonSupport.json.encodeToString(profile.copy(profileSha256 = ""))))
            repository.executeUpdate(
                connection,
                """
                INSERT OR IGNORE INTO character_voice_profiles(
                    profile_id, profile_version, rule_set_id, rule_set_version, character_id,
                    profile_sha256, profile_json, status, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE', ?)
                """.trimIndent(),
                profile.profileId,
                profile.version,
                profile.ruleSetId,
                profile.ruleSetVersion,
                profile.characterId,
                profile.profileSha256,
                JsonSupport.json.encodeToString(profile),
                timestamp,
            )
        }
        require(languageProfile.ruleSetId == ruleSet.ruleSetId && languageProfile.ruleSetVersion == ruleSet.version)
        require(languageProfile.profileSha256 == ProductionRuleRegistry.sha256(JsonSupport.json.encodeToString(languageProfile.copy(profileSha256 = ""))))
        repository.executeUpdate(
            connection,
            """
            INSERT OR IGNORE INTO language_production_profiles(
                profile_id, profile_version, rule_set_id, rule_set_version,
                profile_sha256, profile_json, status, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE', ?)
            """.trimIndent(),
            languageProfile.profileId,
            languageProfile.version,
            languageProfile.ruleSetId,
            languageProfile.ruleSetVersion,
            languageProfile.profileSha256,
            JsonSupport.json.encodeToString(languageProfile),
            timestamp,
        )
    }

    fun ingestArtifact(artifact: DialogueArtifact) = repository.withTransaction { connection ->
        require(artifact.artifactId.isNotBlank() && artifact.productionId.isNotBlank())
        require(artifact.sourceCommitSha.isNotBlank())
        require(artifact.boundary == "NON_CANON_PILOT") { "Dialogue artifacts cannot escalate Canon authority." }
        require(artifact.lines.isNotEmpty()) { "Dialogue artifact requires at least one line." }
        require(artifact.lines.map { it.lineId }.toSet().size == artifact.lines.size) { "Dialogue line IDs must be unique." }
        require(artifact.lines.all { it.lineId.isNotBlank() && it.speakerId.isNotBlank() && it.text.isNotBlank() })
        require(artifact.contentSha256 == ProductionRuleRegistry.artifactHash(artifact)) { "Dialogue artifact SHA-256 mismatch." }
        require(ruleSetExists(connection, artifact.ruleSetId, artifact.ruleSetVersion)) { "Unknown Rule Set." }
        requireProfiles(connection, artifact)

        val initialState = when (artifact.artifactKind) {
            DialogueArtifactKind.INITIAL -> {
                require(artifact.parentArtifactSha256.isBlank() && artifact.autoRevisionCount == 0)
                DialogueApprovalState.REVIEW_PENDING
            }
            DialogueArtifactKind.REVISED -> {
                require(artifact.parentArtifactSha256.isNotBlank() && artifact.autoRevisionCount == 1)
                val parent = artifactRowByHash(connection, artifact.productionId, artifact.parentArtifactSha256)
                    ?: error("Revised artifact parent is not persisted.")
                require(parent.getValue("artifact_kind") == DialogueArtifactKind.INITIAL.name)
                require(parent.getValue("approval_state") == DialogueApprovalState.REVISE_REQUIRED.name) {
                    "Revision requires a blocked and reviewed initial artifact."
                }
                require(parent.getValue("content_sha256") != artifact.contentSha256) { "Revision hash must differ from initial hash." }
                DialogueApprovalState.REVISED_PENDING_REVIEW
            }
        }
        val timestamp = now()
        repository.executeUpdate(
            connection,
            """
            INSERT INTO dialogue_artifacts(
                artifact_id, production_id, artifact_kind, parent_artifact_sha256, content_sha256,
                content_json, rule_set_id, rule_set_version, character_profile_refs_json,
                language_profile_ref, approval_state, auto_revision_count, boundary,
                source_commit_sha, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
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
            initialState.name,
            artifact.autoRevisionCount.toString(),
            artifact.boundary,
            artifact.sourceCommitSha,
            timestamp,
            timestamp,
        )
    }

    fun recordReviewBatch(batch: DialogueReviewBatch) = repository.withTransaction { connection ->
        val artifact = artifact(connection, batch.artifactId)
        val state = artifactState(connection, batch.artifactId)
        require(state in setOf(DialogueApprovalState.REVIEW_PENDING, DialogueApprovalState.REVISED_PENDING_REVIEW)) {
            "Artifact is not pending review."
        }
        require(batch.reviews.size == DialogueReviewerRole.entries.size) { "Exactly three reviews are required." }
        require(batch.reviews.map { it.reviewerRole }.toSet() == DialogueReviewerRole.entries.toSet()) {
            "All required reviewer roles are required exactly once."
        }
        require(batch.reviews.map { it.reviewerInstanceId }.toSet().size == batch.reviews.size) {
            "Reviewer instances must be independent."
        }
        require(repository.queryRows(connection, "SELECT review_id FROM dialogue_review_runs WHERE artifact_id = ?", artifact.artifactId).isEmpty()) {
            "Artifact already has reviews; reviews are immutable."
        }

        val lineById = artifact.lines.associateBy { it.lineId }
        val requiredFixtureFindings = ProductionRuleRegistry.canonicalFixtureFindings(artifact)
        val allSubmittedFindings = batch.reviews.flatMap { it.lineReviews }.flatMap { it.findings }
        requiredFixtureFindings.forEach { required ->
            require(allSubmittedFindings.any { it.lineId == required.lineId && it.ruleCode == required.ruleCode && it.severity == ProductionRuleSeverity.BLOCKER }) {
                "Canonical fixture blocker ${required.ruleCode} is missing."
            }
        }

        batch.reviews.forEach { review ->
            validateReview(artifact, review, lineById)
            repository.executeUpdate(
                connection,
                """
                INSERT INTO dialogue_review_runs(
                    review_id, artifact_id, artifact_sha256, rule_set_id, rule_set_version,
                    reviewer_role, reviewer_instance_id, overall_score, verdict, review_json,
                    source_commit_sha, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                review.reviewId,
                review.artifactId,
                review.artifactSha256,
                review.ruleSetId,
                review.ruleSetVersion,
                review.reviewerRole.name,
                review.reviewerInstanceId,
                review.overallScore.toString(),
                review.verdict.name,
                JsonSupport.json.encodeToString(review),
                review.sourceCommitSha,
                now(),
            )
            review.lineReviews.flatMap { it.findings }.forEach { finding ->
                repository.executeUpdate(
                    connection,
                    """
                    INSERT INTO dialogue_line_findings(
                        finding_id, review_id, line_id, exact_quote, rule_code, severity,
                        explanation, suggested_text, requires_human_approval, created_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent(),
                    finding.findingId,
                    review.reviewId,
                    finding.lineId,
                    finding.exactQuote,
                    finding.ruleCode.name,
                    finding.severity.name,
                    finding.explanation,
                    finding.suggestedText,
                    if (finding.requiresHumanApproval) "1" else "0",
                    now(),
                )
            }
        }
        val blockerCount = allSubmittedFindings.count { it.severity == ProductionRuleSeverity.BLOCKER }
        val nextState = when {
            blockerCount == 0 -> DialogueApprovalState.SELECTABLE
            artifact.artifactKind == DialogueArtifactKind.REVISED -> DialogueApprovalState.HUMAN_DECISION_REQUIRED
            else -> DialogueApprovalState.REVISE_REQUIRED
        }
        check(
            repository.executeUpdate(
                connection,
                "UPDATE dialogue_artifacts SET approval_state = ?, updated_at = ? WHERE artifact_id = ? AND approval_state = ?",
                nextState.name,
                now(),
                artifact.artifactId,
                state.name,
            ) == 1,
        ) { "Dialogue artifact review state changed concurrently." }
    }

    fun select(request: DialogueSelectionRequest): DialogueSelectionDecision = repository.withTransaction { connection ->
        require(request.decisionId.isNotBlank() && request.decisionActorId.isNotBlank() && request.sourceCommitSha.isNotBlank())
        val artifact = artifact(connection, request.artifactId)
        val state = artifactState(connection, request.artifactId)
        val reviews = reviewRows(connection, artifact.artifactId)
        val findings = findingRows(connection, artifact.artifactId)
        val reasons = mutableListOf<String>()
        if (reviews.map { it.reviewerRole }.toSet() != DialogueReviewerRole.entries.toSet()) reasons += "THREE_REQUIRED_REVIEW_ROLES_MISSING"
        if (reviews.map { it.reviewerInstanceId }.toSet().size != DialogueReviewerRole.entries.size) reasons += "REVIEWER_INDEPENDENCE_FAILED"
        if (reviews.any { it.artifactSha256 != artifact.contentSha256 }) reasons += "REVIEW_ARTIFACT_HASH_MISMATCH"
        if (reviews.any { it.ruleSetId != artifact.ruleSetId || it.ruleSetVersion != artifact.ruleSetVersion }) reasons += "REVIEW_RULE_SET_MISMATCH"
        val covered = reviews.all { review -> review.lineReviews.map { it.lineId }.toSet() == artifact.lines.map { it.lineId }.toSet() }
        if (!covered) reasons += "LINE_COVERAGE_INCOMPLETE"
        val blockerCount = findings.count { it.severity == ProductionRuleSeverity.BLOCKER }
        if (blockerCount > 0) reasons += "BLOCKER_PRESENT"
        if (state != DialogueApprovalState.SELECTABLE) reasons += "ARTIFACT_NOT_SELECTABLE"
        if (artifact.artifactKind == DialogueArtifactKind.REVISED) {
            if (artifact.autoRevisionCount != 1 || artifact.parentArtifactSha256.isBlank()) reasons += "REVISION_LIFECYCLE_INVALID"
        }
        val warningCount = findings.count { it.severity == ProductionRuleSeverity.WARNING }
        val status = when {
            reasons.isEmpty() -> DialogueSelectionStatus.SELECTED
            artifact.artifactKind == DialogueArtifactKind.REVISED && blockerCount > 0 -> DialogueSelectionStatus.HUMAN_DECISION_REQUIRED
            else -> DialogueSelectionStatus.NOT_SELECTABLE
        }
        val unsigned = DialogueSelectionDecision(
            decisionId = request.decisionId,
            productionId = artifact.productionId,
            artifactId = artifact.artifactId,
            selectedArtifactSha256 = artifact.contentSha256,
            ruleSetId = artifact.ruleSetId,
            ruleSetVersion = artifact.ruleSetVersion,
            status = status,
            blockerCount = blockerCount,
            warningCount = warningCount,
            reasons = reasons.ifEmpty { listOf("ALL_HARD_GATES_PASS") },
            decisionActorId = request.decisionActorId,
            sourceCommitSha = request.sourceCommitSha,
        )
        val decision = unsigned.copy(decisionSha256 = ProductionRuleRegistry.sha256(JsonSupport.json.encodeToString(unsigned)))
        repository.executeUpdate(
            connection,
            """
            INSERT INTO dialogue_selection_decisions(
                decision_id, production_id, artifact_id, selected_artifact_sha256,
                rule_set_id, rule_set_version, decision_status, blocker_count, warning_count,
                reasons_json, decision_actor_id, source_commit_sha, decision_sha256, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            decision.decisionId,
            decision.productionId,
            decision.artifactId,
            decision.selectedArtifactSha256,
            decision.ruleSetId,
            decision.ruleSetVersion,
            decision.status.name,
            decision.blockerCount.toString(),
            decision.warningCount.toString(),
            JsonSupport.json.encodeToString(decision.reasons),
            decision.decisionActorId,
            decision.sourceCommitSha,
            decision.decisionSha256,
            now(),
        )
        if (status == DialogueSelectionStatus.SELECTED) {
            check(
                repository.executeUpdate(
                    connection,
                    "UPDATE dialogue_artifacts SET approval_state = 'SELECTED', updated_at = ? WHERE artifact_id = ? AND approval_state = 'SELECTABLE'",
                    now(),
                    artifact.artifactId,
                ) == 1,
            ) { "Dialogue selection state changed concurrently." }
        }
        decision
    }

    fun get(artifactId: String): DialogueArtifactView = repository.withConnection { connection ->
        DialogueArtifactView(
            artifact = artifact(connection, artifactId),
            approvalState = artifactState(connection, artifactId),
            reviews = reviewRows(connection, artifactId),
            findings = findingRows(connection, artifactId),
            decisions = decisionRows(connection, artifactId),
        )
    }

    private fun validateReview(
        artifact: DialogueArtifact,
        review: DialogueReviewRun,
        lineById: Map<String, com.localnovel.engine.model.DialogueLine>,
    ) {
        require(review.reviewId.isNotBlank() && review.reviewerInstanceId.isNotBlank() && review.sourceCommitSha.isNotBlank())
        require(review.artifactId == artifact.artifactId && review.artifactSha256 == artifact.contentSha256)
        require(review.ruleSetId == artifact.ruleSetId && review.ruleSetVersion == artifact.ruleSetVersion)
        require(review.summary.isNotBlank())
        require(review.lineReviews.size == artifact.lines.size)
        require(review.lineReviews.map { it.lineId }.toSet() == lineById.keys) { "Review must cover every line exactly once." }
        review.lineReviews.forEach { lineReview ->
            val line = lineById.getValue(lineReview.lineId)
            require(lineReview.exactQuote == line.text) { "Review quote does not match ${line.lineId}." }
            lineReview.findings.forEach { finding ->
                require(finding.lineId == line.lineId && finding.exactQuote == line.text)
                require(finding.explanation.isNotBlank())
                if (ProductionRuleRegistry.isHardBlocker(finding.ruleCode)) {
                    require(finding.severity == ProductionRuleSeverity.BLOCKER) { "Hard blocker cannot be downgraded." }
                }
            }
        }
        val hasBlocker = review.lineReviews.flatMap { it.findings }.any { it.severity == ProductionRuleSeverity.BLOCKER }
        require(!hasBlocker || review.verdict != DialogueReviewVerdict.PASS) { "PASS cannot conceal a BLOCKER." }
    }

    private fun requireProfiles(connection: Connection, artifact: DialogueArtifact) {
        require(artifact.characterVoiceProfileIds.isNotEmpty())
        val persisted = repository.queryRows(
            connection,
            "SELECT profile_id FROM character_voice_profiles WHERE rule_set_id = ? AND rule_set_version = ? AND status = 'ACTIVE'",
            artifact.ruleSetId,
            artifact.ruleSetVersion,
        ).map { it.getValue("profile_id") }.toSet()
        require(persisted.containsAll(artifact.characterVoiceProfileIds)) { "Unknown character voice profile." }
        require(
            repository.queryRows(
                connection,
                "SELECT profile_id FROM language_production_profiles WHERE profile_id = ? AND rule_set_id = ? AND rule_set_version = ? AND status = 'ACTIVE'",
                artifact.languageProfileId,
                artifact.ruleSetId,
                artifact.ruleSetVersion,
            ).size == 1,
        ) { "Unknown language production profile." }
    }

    private fun ruleSetExists(connection: Connection, id: String, version: String): Boolean = repository.queryRows(
        connection,
        "SELECT rule_set_id FROM production_rule_sets WHERE rule_set_id = ? AND rule_set_version = ? AND status = 'ACTIVE'",
        id,
        version,
    ).size == 1

    private fun artifact(connection: Connection, artifactId: String): DialogueArtifact = repository.queryRows(
        connection,
        "SELECT content_json FROM dialogue_artifacts WHERE artifact_id = ?",
        artifactId,
    ).singleOrNull()?.getValue("content_json")?.let { JsonSupport.json.decodeFromString<DialogueArtifact>(it.orEmpty()) }
        ?: error("Unknown dialogue artifact: $artifactId")

    private fun artifactState(connection: Connection, artifactId: String): DialogueApprovalState = repository.queryRows(
        connection,
        "SELECT approval_state FROM dialogue_artifacts WHERE artifact_id = ?",
        artifactId,
    ).single().getValue("approval_state").orEmpty().let(DialogueApprovalState::valueOf)

    private fun artifactRowByHash(connection: Connection, productionId: String, hash: String): Map<String, String?>? = repository.queryRows(
        connection,
        "SELECT artifact_kind, approval_state, content_sha256 FROM dialogue_artifacts WHERE production_id = ? AND content_sha256 = ?",
        productionId,
        hash,
    ).singleOrNull()

    private fun reviewRows(connection: Connection, artifactId: String): List<DialogueReviewRun> = repository.queryRows(
        connection,
        "SELECT review_json FROM dialogue_review_runs WHERE artifact_id = ? ORDER BY reviewer_role",
        artifactId,
    ).map { JsonSupport.json.decodeFromString<DialogueReviewRun>(it.getValue("review_json").orEmpty()) }

    private fun findingRows(connection: Connection, artifactId: String): List<DialogueFinding> = repository.queryRows(
        connection,
        """
        SELECT f.finding_id, f.line_id, f.exact_quote, f.rule_code, f.severity,
               f.explanation, f.suggested_text, f.requires_human_approval
        FROM dialogue_line_findings f
        JOIN dialogue_review_runs r ON r.review_id = f.review_id
        WHERE r.artifact_id = ?
        ORDER BY f.line_id, f.finding_id
        """.trimIndent(),
        artifactId,
    ).map { row ->
        DialogueFinding(
            findingId = row.getValue("finding_id").orEmpty(),
            lineId = row.getValue("line_id").orEmpty(),
            exactQuote = row.getValue("exact_quote").orEmpty(),
            ruleCode = com.localnovel.engine.model.ProductionRuleCode.valueOf(row.getValue("rule_code").orEmpty()),
            severity = ProductionRuleSeverity.valueOf(row.getValue("severity").orEmpty()),
            explanation = row.getValue("explanation").orEmpty(),
            suggestedText = row.getValue("suggested_text").orEmpty(),
            requiresHumanApproval = row.getValue("requires_human_approval") == "1",
        )
    }

    private fun decisionRows(connection: Connection, artifactId: String): List<DialogueSelectionDecision> = repository.queryRows(
        connection,
        "SELECT decision_id, production_id, artifact_id, selected_artifact_sha256, rule_set_id, rule_set_version, decision_status, blocker_count, warning_count, reasons_json, decision_actor_id, source_commit_sha, decision_sha256 FROM dialogue_selection_decisions WHERE artifact_id = ? ORDER BY created_at",
        artifactId,
    ).map { row ->
        DialogueSelectionDecision(
            decisionId = row.getValue("decision_id").orEmpty(),
            productionId = row.getValue("production_id").orEmpty(),
            artifactId = row.getValue("artifact_id").orEmpty(),
            selectedArtifactSha256 = row.getValue("selected_artifact_sha256").orEmpty(),
            ruleSetId = row.getValue("rule_set_id").orEmpty(),
            ruleSetVersion = row.getValue("rule_set_version").orEmpty(),
            status = DialogueSelectionStatus.valueOf(row.getValue("decision_status").orEmpty()),
            blockerCount = row.getValue("blocker_count").orEmpty().toInt(),
            warningCount = row.getValue("warning_count").orEmpty().toInt(),
            reasons = JsonSupport.json.decodeFromString(row.getValue("reasons_json").orEmpty()),
            decisionActorId = row.getValue("decision_actor_id").orEmpty(),
            sourceCommitSha = row.getValue("source_commit_sha").orEmpty(),
            decisionSha256 = row.getValue("decision_sha256").orEmpty(),
        )
    }

    private fun now(): String = Instant.now().toString()
}
