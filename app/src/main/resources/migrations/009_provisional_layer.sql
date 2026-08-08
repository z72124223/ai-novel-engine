ALTER TABLE story_sessions ADD COLUMN reader_mode INTEGER NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS canon_thin_zones (
    thin_zone_id TEXT NOT NULL,
    project_id TEXT NOT NULL,
    label TEXT NOT NULL,
    reason TEXT NOT NULL,
    allowed_proposal_types_json TEXT NOT NULL,
    status TEXT NOT NULL,
    source_path TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    PRIMARY KEY (project_id, thin_zone_id)
);

CREATE INDEX IF NOT EXISTS idx_canon_thin_zones_project_status
    ON canon_thin_zones(project_id, status);

CREATE TABLE IF NOT EXISTS provisional_entities (
    entity_id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    session_id TEXT NOT NULL,
    proposal_id TEXT NOT NULL,
    entity_type TEXT NOT NULL,
    display_name TEXT NOT NULL,
    thin_zone_id TEXT NOT NULL,
    lifespan TEXT NOT NULL,
    introduction_beat TEXT NOT NULL,
    agency_profile_json TEXT NOT NULL,
    narrative_role TEXT NOT NULL,
    evidence_hooks_json TEXT NOT NULL,
    perceived_by_character_ids_json TEXT NOT NULL,
    visibility TEXT NOT NULL DEFAULT 'session_provisional',
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    current_scene_id TEXT,
    created_turn_number INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (session_id) REFERENCES story_sessions(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_provisional_entities_session_proposal
    ON provisional_entities(session_id, proposal_id);

CREATE INDEX IF NOT EXISTS idx_provisional_entities_session_status
    ON provisional_entities(session_id, status);

CREATE TABLE IF NOT EXISTS provisional_entity_evidence (
    evidence_id TEXT PRIMARY KEY,
    entity_id TEXT NOT NULL,
    turn_id TEXT NOT NULL,
    turn_number INTEGER NOT NULL,
    evidence_type TEXT NOT NULL,
    evidence_ref TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (entity_id) REFERENCES provisional_entities(entity_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS provisional_promotion_queue (
    queue_id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    session_id TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    source TEXT NOT NULL,
    status TEXT NOT NULL,
    recommendation_json TEXT NOT NULL,
    created_turn_number INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (entity_id) REFERENCES provisional_entities(entity_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_provisional_promotion_queue_session_status
    ON provisional_promotion_queue(session_id, status);

CREATE TABLE IF NOT EXISTS provisional_promotion_records (
    record_id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    session_id TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    queue_id TEXT,
    action TEXT NOT NULL,
    actor TEXT NOT NULL,
    note TEXT NOT NULL,
    patch_path TEXT,
    created_at TEXT NOT NULL
);