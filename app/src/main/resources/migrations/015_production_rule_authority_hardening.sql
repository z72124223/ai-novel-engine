CREATE TABLE IF NOT EXISTS dialogue_artifact_lines (
    artifact_id TEXT NOT NULL,
    line_id TEXT NOT NULL,
    exact_quote TEXT NOT NULL,
    PRIMARY KEY (artifact_id, line_id),
    FOREIGN KEY (artifact_id) REFERENCES dialogue_artifacts(artifact_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS dialogue_review_line_coverage (
    review_id TEXT NOT NULL,
    line_id TEXT NOT NULL,
    exact_quote TEXT NOT NULL,
    PRIMARY KEY (review_id, line_id),
    FOREIGN KEY (review_id) REFERENCES dialogue_review_runs(review_id) ON DELETE RESTRICT
);

ALTER TABLE dialogue_artifacts
    ADD COLUMN hash_schema_version INTEGER NOT NULL DEFAULT 1;

CREATE TABLE IF NOT EXISTS dialogue_legacy_artifact_audit (
    artifact_id TEXT PRIMARY KEY,
    legacy_content_sha256 TEXT NOT NULL,
    previous_approval_state TEXT NOT NULL,
    audit_status TEXT NOT NULL CHECK(audit_status IN ('QUARANTINED_HASH_SCHEMA_V1', 'HISTORICAL_SELECTED_HASH_SCHEMA_V1')),
    recorded_at TEXT NOT NULL,
    FOREIGN KEY (artifact_id) REFERENCES dialogue_artifacts(artifact_id) ON DELETE RESTRICT
);

INSERT OR IGNORE INTO dialogue_legacy_artifact_audit(
    artifact_id, legacy_content_sha256, previous_approval_state, audit_status, recorded_at
)
SELECT artifact_id, content_sha256, approval_state,
       CASE WHEN approval_state = 'SELECTED'
            THEN 'HISTORICAL_SELECTED_HASH_SCHEMA_V1'
            ELSE 'QUARANTINED_HASH_SCHEMA_V1'
       END,
       CURRENT_TIMESTAMP
FROM dialogue_artifacts
WHERE hash_schema_version = 1;

DROP TRIGGER IF EXISTS trg_dialogue_artifact_line_insert_guard;
DROP TRIGGER IF EXISTS trg_dialogue_review_run_insert_guard;
DROP TRIGGER IF EXISTS trg_dialogue_review_line_insert_guard;
DROP TRIGGER IF EXISTS trg_dialogue_finding_insert_guard;

INSERT OR IGNORE INTO dialogue_artifact_lines(artifact_id, line_id, exact_quote)
SELECT a.artifact_id, json_extract(line.value, '$.lineId'), json_extract(line.value, '$.text')
FROM dialogue_artifacts a, json_each(a.content_json, '$.lines') AS line;

INSERT OR IGNORE INTO dialogue_review_line_coverage(review_id, line_id, exact_quote)
SELECT r.review_id, json_extract(line.value, '$.lineId'), json_extract(line.value, '$.exactQuote')
FROM dialogue_review_runs r, json_each(r.review_json, '$.lineReviews') AS line;

DROP TRIGGER IF EXISTS trg_dialogue_selected_decision_guard;
DROP TRIGGER IF EXISTS trg_dialogue_selection_decision_update_forbidden;
DROP TRIGGER IF EXISTS trg_dialogue_selection_decision_delete_forbidden;
DROP TRIGGER IF EXISTS trg_dialogue_artifact_selected_guard;
DROP TRIGGER IF EXISTS trg_dialogue_review_run_update_forbidden;
DROP TRIGGER IF EXISTS trg_dialogue_review_run_delete_forbidden;
DROP TRIGGER IF EXISTS trg_dialogue_finding_update_forbidden;
DROP TRIGGER IF EXISTS trg_dialogue_finding_delete_forbidden;
DROP TRIGGER IF EXISTS trg_dialogue_artifact_identity_update_forbidden;
DROP TRIGGER IF EXISTS trg_dialogue_artifact_insert_state_guard;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_artifact_insert_state_guard
BEFORE INSERT ON dialogue_artifacts
WHEN NEW.approval_state NOT IN ('REVIEW_PENDING', 'REVISED_PENDING_REVIEW')
     OR NEW.hash_schema_version <> 2
BEGIN
    SELECT RAISE(ABORT, 'DIALOGUE_ARTIFACT_MUST_ENTER_CURRENT_PENDING_STATE');
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_artifact_state_transition_guard
BEFORE UPDATE OF approval_state ON dialogue_artifacts
WHEN NEW.approval_state <> OLD.approval_state
BEGIN
    SELECT CASE WHEN NOT (
        (OLD.approval_state = 'REVIEW_PENDING' AND NEW.approval_state IN ('SELECTABLE', 'REVISE_REQUIRED', 'HUMAN_DECISION_REQUIRED'))
        OR (OLD.approval_state = 'REVISED_PENDING_REVIEW' AND NEW.approval_state IN ('SELECTABLE', 'HUMAN_DECISION_REQUIRED'))
        OR (OLD.approval_state = 'SELECTABLE' AND NEW.approval_state = 'SELECTED')
    ) THEN RAISE(ABORT, 'INVALID_DIALOGUE_APPROVAL_TRANSITION') END;
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_artifact_selectable_guard
BEFORE UPDATE OF approval_state ON dialogue_artifacts
WHEN NEW.approval_state = 'SELECTABLE' AND OLD.approval_state <> 'SELECTABLE'
BEGIN
    SELECT CASE WHEN NEW.hash_schema_version <> 2 THEN RAISE(ABORT, 'LEGACY_ARTIFACT_QUARANTINED') END;
    SELECT CASE WHEN COALESCE(json_type(NEW.content_json, '$.lines'), 'missing') <> 'array'
        THEN RAISE(ABORT, 'ARTIFACT_LINES_JSON_ARRAY_REQUIRED') END;
    SELECT CASE WHEN (
        SELECT COUNT(*) FROM dialogue_artifact_lines l WHERE l.artifact_id = NEW.artifact_id
    ) <= 0 THEN RAISE(ABORT, 'ARTIFACT_LINES_REQUIRED') END;
    SELECT CASE WHEN (
        SELECT COUNT(*) FROM dialogue_artifact_lines l WHERE l.artifact_id = NEW.artifact_id
    ) <> json_array_length(NEW.content_json, '$.lines') THEN RAISE(ABORT, 'ARTIFACT_LINE_JSON_COUNT_MISMATCH') END;
    SELECT CASE WHEN EXISTS (
        SELECT 1 FROM json_each(NEW.content_json, '$.lines') line
        WHERE NOT EXISTS (
            SELECT 1 FROM dialogue_artifact_lines l
            WHERE l.artifact_id = NEW.artifact_id
              AND l.line_id = json_extract(line.value, '$.lineId')
              AND l.exact_quote = json_extract(line.value, '$.text')
        )
    ) THEN RAISE(ABORT, 'ARTIFACT_LINE_JSON_MISMATCH') END;
    SELECT CASE WHEN (
        SELECT COUNT(DISTINCT r.reviewer_role)
        FROM dialogue_review_runs r
        WHERE r.artifact_id = NEW.artifact_id
          AND r.artifact_sha256 = NEW.content_sha256
          AND r.rule_set_id = NEW.rule_set_id
          AND r.rule_set_version = NEW.rule_set_version
          AND r.verdict = 'PASS'
    ) <> 3 THEN RAISE(ABORT, 'THREE_PASS_REVIEWS_REQUIRED') END;
    SELECT CASE WHEN (
        SELECT COUNT(DISTINCT r.reviewer_instance_id)
        FROM dialogue_review_runs r
        WHERE r.artifact_id = NEW.artifact_id
    ) <> 3 THEN RAISE(ABORT, 'INDEPENDENT_REVIEWERS_REQUIRED') END;
    SELECT CASE WHEN EXISTS (
        SELECT 1
        FROM dialogue_review_runs r
        WHERE r.artifact_id = NEW.artifact_id
          AND (
              COALESCE(json_type(r.review_json, '$.lineReviews'), 'missing') <> 'array'
              OR
              (SELECT COUNT(*) FROM dialogue_review_line_coverage c WHERE c.review_id = r.review_id)
                  <> (SELECT COUNT(*) FROM dialogue_artifact_lines l WHERE l.artifact_id = NEW.artifact_id)
              OR json_array_length(r.review_json, '$.lineReviews')
                  <> (SELECT COUNT(*) FROM dialogue_artifact_lines l WHERE l.artifact_id = NEW.artifact_id)
              OR EXISTS (
                  SELECT 1 FROM dialogue_artifact_lines l
                  WHERE l.artifact_id = NEW.artifact_id
                    AND NOT EXISTS (
                        SELECT 1 FROM dialogue_review_line_coverage c
                        WHERE c.review_id = r.review_id
                          AND c.line_id = l.line_id
                          AND c.exact_quote = l.exact_quote
                    )
              )
              OR EXISTS (
                  SELECT 1 FROM json_each(r.review_json, '$.lineReviews') line
                  WHERE NOT EXISTS (
                      SELECT 1 FROM dialogue_review_line_coverage c
                      WHERE c.review_id = r.review_id
                        AND c.line_id = json_extract(line.value, '$.lineId')
                        AND c.exact_quote = json_extract(line.value, '$.exactQuote')
                  )
              )
          )
    ) THEN RAISE(ABORT, 'REVIEW_LINE_COVERAGE_OR_QUOTE_INVALID') END;
    SELECT CASE WHEN EXISTS (
        SELECT 1 FROM dialogue_line_findings f
        JOIN dialogue_review_runs r ON r.review_id = f.review_id
        WHERE r.artifact_id = NEW.artifact_id
          AND (f.severity = 'BLOCKER' OR f.requires_human_approval = 1)
    ) THEN RAISE(ABORT, 'DIALOGUE_FINDING_PREVENTS_SELECTABLE') END;
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_selected_decision_guard
BEFORE INSERT ON dialogue_selection_decisions
WHEN NEW.decision_status = 'SELECTED'
BEGIN
    SELECT CASE WHEN NEW.blocker_count <> 0 THEN RAISE(ABORT, 'BLOCKER_PREVENTS_SELECTION') END;
    SELECT CASE WHEN NOT EXISTS (
        SELECT 1 FROM dialogue_artifacts a
        WHERE a.artifact_id = NEW.artifact_id
          AND a.content_sha256 = NEW.selected_artifact_sha256
          AND a.production_id = NEW.production_id
          AND a.rule_set_id = NEW.rule_set_id
          AND a.rule_set_version = NEW.rule_set_version
          AND a.approval_state = 'SELECTABLE'
          AND a.hash_schema_version = 2
    ) THEN RAISE(ABORT, 'ARTIFACT_NOT_SELECTABLE') END;
    SELECT CASE WHEN (
        SELECT COUNT(DISTINCT r.reviewer_role)
        FROM dialogue_review_runs r
        WHERE r.artifact_id = NEW.artifact_id
          AND r.artifact_sha256 = NEW.selected_artifact_sha256
          AND r.rule_set_id = NEW.rule_set_id
          AND r.rule_set_version = NEW.rule_set_version
          AND r.verdict = 'PASS'
    ) <> 3 THEN RAISE(ABORT, 'THREE_PASS_REVIEWS_REQUIRED') END;
    SELECT CASE WHEN (
        SELECT COUNT(DISTINCT r.reviewer_instance_id)
        FROM dialogue_review_runs r
        WHERE r.artifact_id = NEW.artifact_id
    ) <> 3 THEN RAISE(ABORT, 'INDEPENDENT_REVIEWERS_REQUIRED') END;
    SELECT CASE WHEN EXISTS (
        SELECT 1
        FROM dialogue_review_runs r
        WHERE r.artifact_id = NEW.artifact_id
          AND (
              COALESCE(json_type(r.review_json, '$.lineReviews'), 'missing') <> 'array'
              OR
              (SELECT COUNT(*) FROM dialogue_review_line_coverage c WHERE c.review_id = r.review_id)
                  <> (SELECT COUNT(*) FROM dialogue_artifact_lines l WHERE l.artifact_id = NEW.artifact_id)
              OR json_array_length(r.review_json, '$.lineReviews')
                  <> (SELECT COUNT(*) FROM dialogue_artifact_lines l WHERE l.artifact_id = NEW.artifact_id)
              OR EXISTS (
                  SELECT 1 FROM dialogue_artifact_lines l
                  WHERE l.artifact_id = NEW.artifact_id
                    AND NOT EXISTS (
                        SELECT 1 FROM dialogue_review_line_coverage c
                        WHERE c.review_id = r.review_id
                          AND c.line_id = l.line_id
                          AND c.exact_quote = l.exact_quote
                    )
              )
              OR EXISTS (
                  SELECT 1 FROM json_each(r.review_json, '$.lineReviews') line
                  WHERE NOT EXISTS (
                      SELECT 1 FROM dialogue_review_line_coverage c
                      WHERE c.review_id = r.review_id
                        AND c.line_id = json_extract(line.value, '$.lineId')
                        AND c.exact_quote = json_extract(line.value, '$.exactQuote')
                  )
              )
          )
    ) THEN RAISE(ABORT, 'REVIEW_LINE_COVERAGE_OR_QUOTE_INVALID') END;
    SELECT CASE WHEN EXISTS (
        SELECT 1 FROM dialogue_line_findings f
        JOIN dialogue_review_runs r ON r.review_id = f.review_id
        WHERE r.artifact_id = NEW.artifact_id
          AND (f.severity = 'BLOCKER' OR f.requires_human_approval = 1)
    ) THEN RAISE(ABORT, 'PERSISTED_FINDING_PREVENTS_SELECTION') END;
    SELECT CASE WHEN NEW.warning_count <> (
        SELECT COUNT(*) FROM dialogue_line_findings f
        JOIN dialogue_review_runs r ON r.review_id = f.review_id
        WHERE r.artifact_id = NEW.artifact_id AND f.severity = 'WARNING'
    ) THEN RAISE(ABORT, 'WARNING_COUNT_MISMATCH') END;
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_artifact_selected_guard
BEFORE UPDATE OF approval_state ON dialogue_artifacts
WHEN NEW.approval_state = 'SELECTED'
BEGIN
    SELECT CASE WHEN OLD.approval_state <> 'SELECTABLE' THEN RAISE(ABORT, 'ARTIFACT_NOT_SELECTABLE') END;
    SELECT CASE WHEN NOT EXISTS (
        SELECT 1 FROM dialogue_selection_decisions d
        WHERE d.artifact_id = NEW.artifact_id
          AND d.selected_artifact_sha256 = NEW.content_sha256
          AND d.decision_status = 'SELECTED'
    ) THEN RAISE(ABORT, 'SELECTION_DECISION_REQUIRED') END;
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_selection_decision_update_forbidden
BEFORE UPDATE ON dialogue_selection_decisions
BEGIN
    SELECT RAISE(ABORT, 'SELECTION_DECISION_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_selection_decision_delete_forbidden
BEFORE DELETE ON dialogue_selection_decisions
BEGIN
    SELECT RAISE(ABORT, 'SELECTION_DECISION_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_review_run_update_forbidden
BEFORE UPDATE ON dialogue_review_runs
BEGIN
    SELECT RAISE(ABORT, 'DIALOGUE_REVIEW_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_review_run_insert_guard
BEFORE INSERT ON dialogue_review_runs
BEGIN
    SELECT CASE WHEN NOT EXISTS (
        SELECT 1 FROM dialogue_artifacts a
        WHERE a.artifact_id = NEW.artifact_id
          AND a.hash_schema_version = 2
          AND a.approval_state IN ('REVIEW_PENDING', 'REVISED_PENDING_REVIEW')
          AND NOT EXISTS (
              SELECT 1 FROM dialogue_selection_decisions d
              WHERE d.artifact_id = a.artifact_id AND d.decision_status = 'SELECTED'
          )
    ) THEN RAISE(ABORT, 'REVIEW_INSERT_REQUIRES_PENDING_CURRENT_ARTIFACT') END;
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_review_run_delete_forbidden
BEFORE DELETE ON dialogue_review_runs
BEGIN
    SELECT RAISE(ABORT, 'DIALOGUE_REVIEW_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_finding_update_forbidden
BEFORE UPDATE ON dialogue_line_findings
BEGIN
    SELECT RAISE(ABORT, 'DIALOGUE_FINDING_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_finding_insert_guard
BEFORE INSERT ON dialogue_line_findings
BEGIN
    SELECT CASE WHEN NOT EXISTS (
        SELECT 1 FROM dialogue_review_runs r
        JOIN dialogue_artifacts a ON a.artifact_id = r.artifact_id
        WHERE r.review_id = NEW.review_id
          AND a.hash_schema_version = 2
          AND a.approval_state IN ('REVIEW_PENDING', 'REVISED_PENDING_REVIEW')
    ) THEN RAISE(ABORT, 'FINDING_INSERT_REQUIRES_PENDING_CURRENT_ARTIFACT') END;
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_finding_delete_forbidden
BEFORE DELETE ON dialogue_line_findings
BEGIN
    SELECT RAISE(ABORT, 'DIALOGUE_FINDING_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_artifact_line_update_forbidden
BEFORE UPDATE ON dialogue_artifact_lines
BEGIN
    SELECT RAISE(ABORT, 'DIALOGUE_ARTIFACT_LINE_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_artifact_line_insert_guard
BEFORE INSERT ON dialogue_artifact_lines
BEGIN
    SELECT CASE WHEN NOT EXISTS (
        SELECT 1 FROM dialogue_artifacts a
        WHERE a.artifact_id = NEW.artifact_id
          AND a.hash_schema_version = 2
          AND a.approval_state IN ('REVIEW_PENDING', 'REVISED_PENDING_REVIEW')
          AND NOT EXISTS (
              SELECT 1 FROM dialogue_review_runs r WHERE r.artifact_id = a.artifact_id
          )
    ) THEN RAISE(ABORT, 'ARTIFACT_LINE_INSERT_REQUIRES_UNREVIEWED_CURRENT_ARTIFACT') END;
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_artifact_line_delete_forbidden
BEFORE DELETE ON dialogue_artifact_lines
BEGIN
    SELECT RAISE(ABORT, 'DIALOGUE_ARTIFACT_LINE_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_review_line_update_forbidden
BEFORE UPDATE ON dialogue_review_line_coverage
BEGIN
    SELECT RAISE(ABORT, 'DIALOGUE_REVIEW_LINE_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_review_line_insert_guard
BEFORE INSERT ON dialogue_review_line_coverage
BEGIN
    SELECT CASE WHEN NOT EXISTS (
        SELECT 1 FROM dialogue_review_runs r
        JOIN dialogue_artifacts a ON a.artifact_id = r.artifact_id
        WHERE r.review_id = NEW.review_id
          AND a.hash_schema_version = 2
          AND a.approval_state IN ('REVIEW_PENDING', 'REVISED_PENDING_REVIEW')
    ) THEN RAISE(ABORT, 'REVIEW_LINE_INSERT_REQUIRES_PENDING_CURRENT_ARTIFACT') END;
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_review_line_delete_forbidden
BEFORE DELETE ON dialogue_review_line_coverage
BEGIN
    SELECT RAISE(ABORT, 'DIALOGUE_REVIEW_LINE_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_artifact_identity_update_forbidden
BEFORE UPDATE OF
    artifact_id, production_id, artifact_kind, parent_artifact_sha256, content_sha256,
    content_json, rule_set_id, rule_set_version, character_profile_refs_json,
    language_profile_ref, auto_revision_count, boundary, source_commit_sha, hash_schema_version
ON dialogue_artifacts
BEGIN
    SELECT RAISE(ABORT, 'DIALOGUE_ARTIFACT_IDENTITY_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_production_rule_set_update_forbidden
BEFORE UPDATE ON production_rule_sets
BEGIN
    SELECT RAISE(ABORT, 'PRODUCTION_RULE_SET_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_production_rule_set_delete_forbidden
BEFORE DELETE ON production_rule_sets
BEGIN
    SELECT RAISE(ABORT, 'PRODUCTION_RULE_SET_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_production_rule_update_forbidden
BEFORE UPDATE ON production_rules
BEGIN
    SELECT RAISE(ABORT, 'PRODUCTION_RULE_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_production_rule_delete_forbidden
BEFORE DELETE ON production_rules
BEGIN
    SELECT RAISE(ABORT, 'PRODUCTION_RULE_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_character_voice_profile_update_forbidden
BEFORE UPDATE ON character_voice_profiles
BEGIN
    SELECT RAISE(ABORT, 'CHARACTER_VOICE_PROFILE_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_character_voice_profile_delete_forbidden
BEFORE DELETE ON character_voice_profiles
BEGIN
    SELECT RAISE(ABORT, 'CHARACTER_VOICE_PROFILE_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_language_profile_update_forbidden
BEFORE UPDATE ON language_production_profiles
BEGIN
    SELECT RAISE(ABORT, 'LANGUAGE_PROFILE_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_language_profile_delete_forbidden
BEFORE DELETE ON language_production_profiles
BEGIN
    SELECT RAISE(ABORT, 'LANGUAGE_PROFILE_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_legacy_audit_update_forbidden
BEFORE UPDATE ON dialogue_legacy_artifact_audit
BEGIN
    SELECT RAISE(ABORT, 'DIALOGUE_LEGACY_AUDIT_IMMUTABLE');
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_legacy_audit_delete_forbidden
BEFORE DELETE ON dialogue_legacy_artifact_audit
BEGIN
    SELECT RAISE(ABORT, 'DIALOGUE_LEGACY_AUDIT_IMMUTABLE');
END;

CREATE INDEX IF NOT EXISTS idx_dialogue_artifact_lines_artifact
    ON dialogue_artifact_lines(artifact_id, line_id);
CREATE INDEX IF NOT EXISTS idx_dialogue_review_line_coverage_review
    ON dialogue_review_line_coverage(review_id, line_id);
