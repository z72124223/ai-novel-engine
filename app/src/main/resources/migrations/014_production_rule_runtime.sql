CREATE TABLE IF NOT EXISTS production_rule_sets (
    rule_set_id TEXT NOT NULL,
    rule_set_version TEXT NOT NULL,
    content_sha256 TEXT NOT NULL CHECK(length(content_sha256) = 64),
    status TEXT NOT NULL CHECK(status IN ('ACTIVE', 'RETIRED')),
    rule_set_json TEXT NOT NULL,
    created_at TEXT NOT NULL,
    PRIMARY KEY (rule_set_id, rule_set_version)
);

CREATE TABLE IF NOT EXISTS production_rules (
    rule_set_id TEXT NOT NULL,
    rule_set_version TEXT NOT NULL,
    rule_code TEXT NOT NULL,
    category TEXT NOT NULL,
    default_severity TEXT NOT NULL CHECK(default_severity IN ('INFO', 'WARNING', 'BLOCKER')),
    hard_blocker INTEGER NOT NULL CHECK(hard_blocker IN (0, 1)),
    description TEXT NOT NULL,
    PRIMARY KEY (rule_set_id, rule_set_version, rule_code),
    FOREIGN KEY (rule_set_id, rule_set_version)
        REFERENCES production_rule_sets(rule_set_id, rule_set_version) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS character_voice_profiles (
    profile_id TEXT NOT NULL,
    profile_version TEXT NOT NULL,
    rule_set_id TEXT NOT NULL,
    rule_set_version TEXT NOT NULL,
    character_id TEXT NOT NULL,
    profile_sha256 TEXT NOT NULL CHECK(length(profile_sha256) = 64),
    profile_json TEXT NOT NULL,
    status TEXT NOT NULL CHECK(status IN ('ACTIVE', 'RETIRED')),
    created_at TEXT NOT NULL,
    PRIMARY KEY (profile_id, profile_version),
    FOREIGN KEY (rule_set_id, rule_set_version)
        REFERENCES production_rule_sets(rule_set_id, rule_set_version) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS language_production_profiles (
    profile_id TEXT NOT NULL,
    profile_version TEXT NOT NULL,
    rule_set_id TEXT NOT NULL,
    rule_set_version TEXT NOT NULL,
    profile_sha256 TEXT NOT NULL CHECK(length(profile_sha256) = 64),
    profile_json TEXT NOT NULL,
    status TEXT NOT NULL CHECK(status IN ('ACTIVE', 'RETIRED')),
    created_at TEXT NOT NULL,
    PRIMARY KEY (profile_id, profile_version),
    FOREIGN KEY (rule_set_id, rule_set_version)
        REFERENCES production_rule_sets(rule_set_id, rule_set_version) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS dialogue_artifacts (
    artifact_id TEXT PRIMARY KEY,
    production_id TEXT NOT NULL,
    artifact_kind TEXT NOT NULL CHECK(artifact_kind IN ('INITIAL', 'REVISED')),
    parent_artifact_sha256 TEXT NOT NULL DEFAULT '',
    content_sha256 TEXT NOT NULL CHECK(length(content_sha256) = 64),
    content_json TEXT NOT NULL,
    rule_set_id TEXT NOT NULL,
    rule_set_version TEXT NOT NULL,
    character_profile_refs_json TEXT NOT NULL,
    language_profile_ref TEXT NOT NULL,
    approval_state TEXT NOT NULL CHECK(approval_state IN (
        'DRAFT', 'REVIEW_PENDING', 'REVISE_REQUIRED', 'REVISED_PENDING_REVIEW',
        'SELECTABLE', 'HUMAN_DECISION_REQUIRED', 'SELECTED'
    )),
    auto_revision_count INTEGER NOT NULL CHECK(auto_revision_count BETWEEN 0 AND 1),
    boundary TEXT NOT NULL CHECK(boundary = 'NON_CANON_PILOT'),
    source_commit_sha TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE (artifact_id, content_sha256),
    FOREIGN KEY (rule_set_id, rule_set_version)
        REFERENCES production_rule_sets(rule_set_id, rule_set_version) ON DELETE RESTRICT,
    CHECK (
        (artifact_kind = 'INITIAL' AND parent_artifact_sha256 = '' AND auto_revision_count = 0) OR
        (artifact_kind = 'REVISED' AND length(parent_artifact_sha256) = 64 AND auto_revision_count = 1)
    )
);

CREATE TABLE IF NOT EXISTS dialogue_review_runs (
    review_id TEXT PRIMARY KEY,
    artifact_id TEXT NOT NULL,
    artifact_sha256 TEXT NOT NULL CHECK(length(artifact_sha256) = 64),
    rule_set_id TEXT NOT NULL,
    rule_set_version TEXT NOT NULL,
    reviewer_role TEXT NOT NULL CHECK(reviewer_role IN (
        'TAIWAN_SPOKEN_DIALOGUE', 'ACTOR_BREATH_AND_PERFORMANCE', 'PAN_CHINESE_COMPREHENSION'
    )),
    reviewer_instance_id TEXT NOT NULL,
    overall_score INTEGER NOT NULL CHECK(overall_score BETWEEN 0 AND 100),
    verdict TEXT NOT NULL CHECK(verdict IN ('PASS', 'REVISE', 'HUMAN_DECISION_REQUIRED')),
    review_json TEXT NOT NULL,
    source_commit_sha TEXT NOT NULL,
    created_at TEXT NOT NULL,
    UNIQUE (artifact_id, reviewer_role),
    UNIQUE (artifact_id, reviewer_instance_id),
    FOREIGN KEY (artifact_id, artifact_sha256)
        REFERENCES dialogue_artifacts(artifact_id, content_sha256) ON DELETE RESTRICT,
    FOREIGN KEY (rule_set_id, rule_set_version)
        REFERENCES production_rule_sets(rule_set_id, rule_set_version) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS dialogue_line_findings (
    finding_id TEXT PRIMARY KEY,
    review_id TEXT NOT NULL,
    line_id TEXT NOT NULL,
    exact_quote TEXT NOT NULL,
    rule_code TEXT NOT NULL,
    severity TEXT NOT NULL CHECK(severity IN ('INFO', 'WARNING', 'BLOCKER')),
    explanation TEXT NOT NULL,
    suggested_text TEXT NOT NULL DEFAULT '',
    requires_human_approval INTEGER NOT NULL CHECK(requires_human_approval IN (0, 1)),
    created_at TEXT NOT NULL,
    UNIQUE (review_id, finding_id),
    FOREIGN KEY (review_id) REFERENCES dialogue_review_runs(review_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS dialogue_selection_decisions (
    decision_id TEXT PRIMARY KEY,
    production_id TEXT NOT NULL,
    artifact_id TEXT NOT NULL,
    selected_artifact_sha256 TEXT NOT NULL CHECK(length(selected_artifact_sha256) = 64),
    rule_set_id TEXT NOT NULL,
    rule_set_version TEXT NOT NULL,
    decision_status TEXT NOT NULL CHECK(decision_status IN ('SELECTED', 'NOT_SELECTABLE', 'HUMAN_DECISION_REQUIRED')),
    blocker_count INTEGER NOT NULL CHECK(blocker_count >= 0),
    warning_count INTEGER NOT NULL CHECK(warning_count >= 0),
    reasons_json TEXT NOT NULL,
    decision_actor_id TEXT NOT NULL,
    source_commit_sha TEXT NOT NULL,
    decision_sha256 TEXT NOT NULL CHECK(length(decision_sha256) = 64),
    created_at TEXT NOT NULL,
    FOREIGN KEY (artifact_id, selected_artifact_sha256)
        REFERENCES dialogue_artifacts(artifact_id, content_sha256) ON DELETE RESTRICT,
    FOREIGN KEY (rule_set_id, rule_set_version)
        REFERENCES production_rule_sets(rule_set_id, rule_set_version) ON DELETE RESTRICT
);

CREATE TRIGGER IF NOT EXISTS trg_dialogue_selected_decision_guard
BEFORE INSERT ON dialogue_selection_decisions
WHEN NEW.decision_status = 'SELECTED'
BEGIN
    SELECT CASE WHEN NEW.blocker_count <> 0 THEN RAISE(ABORT, 'BLOCKER_PREVENTS_SELECTION') END;
    SELECT CASE WHEN (
        SELECT COUNT(DISTINCT reviewer_role) FROM dialogue_review_runs
        WHERE artifact_id = NEW.artifact_id
          AND artifact_sha256 = NEW.selected_artifact_sha256
          AND rule_set_id = NEW.rule_set_id
          AND rule_set_version = NEW.rule_set_version
    ) <> 3 THEN RAISE(ABORT, 'THREE_REVIEWS_REQUIRED') END;
    SELECT CASE WHEN (
        SELECT COUNT(DISTINCT reviewer_instance_id) FROM dialogue_review_runs
        WHERE artifact_id = NEW.artifact_id
    ) <> 3 THEN RAISE(ABORT, 'INDEPENDENT_REVIEWERS_REQUIRED') END;
    SELECT CASE WHEN EXISTS (
        SELECT 1 FROM dialogue_line_findings f
        JOIN dialogue_review_runs r ON r.review_id = f.review_id
        WHERE r.artifact_id = NEW.artifact_id AND f.severity = 'BLOCKER'
    ) THEN RAISE(ABORT, 'PERSISTED_BLOCKER_PREVENTS_SELECTION') END;
END;

CREATE TRIGGER IF NOT EXISTS trg_dialogue_artifact_selected_guard
BEFORE UPDATE OF approval_state ON dialogue_artifacts
WHEN NEW.approval_state = 'SELECTED'
BEGIN
    SELECT CASE WHEN NOT EXISTS (
        SELECT 1 FROM dialogue_selection_decisions d
        WHERE d.artifact_id = NEW.artifact_id
          AND d.selected_artifact_sha256 = NEW.content_sha256
          AND d.decision_status = 'SELECTED'
    ) THEN RAISE(ABORT, 'SELECTION_DECISION_REQUIRED') END;
END;

CREATE INDEX IF NOT EXISTS idx_dialogue_artifacts_production
    ON dialogue_artifacts(production_id, created_at);
CREATE INDEX IF NOT EXISTS idx_dialogue_review_runs_artifact
    ON dialogue_review_runs(artifact_id, reviewer_role);
CREATE INDEX IF NOT EXISTS idx_dialogue_line_findings_review
    ON dialogue_line_findings(review_id, line_id, severity);
CREATE INDEX IF NOT EXISTS idx_dialogue_selection_production
    ON dialogue_selection_decisions(production_id, created_at);
