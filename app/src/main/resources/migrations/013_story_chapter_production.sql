CREATE TABLE IF NOT EXISTS story_chapter_productions (
    production_id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    session_id TEXT NOT NULL,
    scenario_seed TEXT NOT NULL,
    source_commit_sha TEXT NOT NULL,
    source_packet_json TEXT NOT NULL,
    source_packet_sha256 TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN (
        'DRAFT_REQUESTED',
        'DRAFT_INGESTED',
        'REVIEWS_COMPLETE',
        'REVISION_INGESTED',
        'SELECTED_PILOT_DRAFT',
        'HUMAN_DECISION_REQUIRED',
        'REJECTED'
    )),
    initial_draft TEXT NOT NULL DEFAULT '',
    initial_draft_sha256 TEXT NOT NULL DEFAULT '',
    initial_model_metadata_json TEXT NOT NULL DEFAULT '{}',
    revised_draft TEXT NOT NULL DEFAULT '',
    revised_draft_sha256 TEXT NOT NULL DEFAULT '',
    revision_model_metadata_json TEXT NOT NULL DEFAULT '{}',
    selected_draft TEXT NOT NULL DEFAULT '',
    selected_draft_sha256 TEXT NOT NULL DEFAULT '',
    revision_count INTEGER NOT NULL DEFAULT 0 CHECK (revision_count BETWEEN 0 AND 1),
    human_decision TEXT NOT NULL DEFAULT '',
    human_actor_id TEXT NOT NULL DEFAULT '',
    canon_status TEXT NOT NULL DEFAULT 'NON_CANON_PILOT' CHECK (canon_status = 'NON_CANON_PILOT'),
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS story_chapter_reviews (
    review_id TEXT PRIMARY KEY,
    production_id TEXT NOT NULL,
    reviewer_role TEXT NOT NULL CHECK (reviewer_role IN ('LOGIC', 'CHARACTER', 'READER')),
    reviewer_instance_id TEXT NOT NULL,
    source_draft_sha256 TEXT NOT NULL,
    verdict TEXT NOT NULL CHECK (verdict IN ('PASS', 'REVISE', 'HUMAN_DECISION_REQUIRED')),
    review_json TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (production_id) REFERENCES story_chapter_productions(production_id) ON DELETE RESTRICT,
    UNIQUE (production_id, reviewer_role),
    UNIQUE (production_id, reviewer_instance_id)
);

CREATE INDEX IF NOT EXISTS idx_story_chapter_productions_project
    ON story_chapter_productions(project_id, created_at);

CREATE INDEX IF NOT EXISTS idx_story_chapter_reviews_production
    ON story_chapter_reviews(production_id, reviewer_role);
