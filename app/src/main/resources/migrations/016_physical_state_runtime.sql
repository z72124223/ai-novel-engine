CREATE TABLE IF NOT EXISTS physical_archetype_registries (
    registry_sha256 TEXT PRIMARY KEY CHECK(length(registry_sha256) = 64),
    schema_version TEXT NOT NULL,
    archetypes_json TEXT NOT NULL CHECK(json_valid(archetypes_json)),
    authority_boundary TEXT NOT NULL CHECK(authority_boundary = 'FORMAL_TURN_RUNTIME_NON_CANON'),
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS formal_physical_states (
    session_id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    physical_state_version INTEGER NOT NULL CHECK(physical_state_version >= 1),
    registry_sha256 TEXT NOT NULL,
    snapshot_sha256 TEXT NOT NULL CHECK(length(snapshot_sha256) = 64),
    snapshot_json TEXT NOT NULL CHECK(json_valid(snapshot_json)),
    authority_boundary TEXT NOT NULL CHECK(authority_boundary = 'FORMAL_TURN_RUNTIME_NON_CANON'),
    source_context_id TEXT NOT NULL,
    last_turn_id TEXT NOT NULL,
    last_result_id TEXT NOT NULL UNIQUE,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (session_id) REFERENCES story_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (registry_sha256) REFERENCES physical_archetype_registries(registry_sha256) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS formal_physical_state_transitions (
    transition_id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    session_id TEXT NOT NULL,
    turn_id TEXT NOT NULL,
    result_id TEXT NOT NULL,
    context_id TEXT NOT NULL,
    action_id TEXT NOT NULL,
    action_type TEXT NOT NULL CHECK(action_type IN ('LIFT', 'PUSH', 'PLACE_ON', 'DROP', 'ENTER_CONTAINER', 'PASS_THROUGH')),
    before_state_version INTEGER NOT NULL CHECK(before_state_version >= 0),
    after_state_version INTEGER NOT NULL CHECK(after_state_version = before_state_version + 1),
    before_snapshot_sha256 TEXT NOT NULL CHECK(length(before_snapshot_sha256) = 64),
    after_snapshot_sha256 TEXT NOT NULL CHECK(length(after_snapshot_sha256) = 64),
    proposal_json TEXT NOT NULL CHECK(json_valid(proposal_json)),
    applied_effects_json TEXT NOT NULL CHECK(json_valid(applied_effects_json)),
    authority_boundary TEXT NOT NULL CHECK(authority_boundary = 'FORMAL_TURN_RUNTIME_NON_CANON'),
    created_at TEXT NOT NULL,
    UNIQUE(session_id, result_id, action_id),
    UNIQUE(session_id, after_state_version),
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (session_id) REFERENCES story_sessions(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_formal_physical_states_project
    ON formal_physical_states(project_id, session_id);

CREATE INDEX IF NOT EXISTS idx_formal_physical_transitions_turn
    ON formal_physical_state_transitions(session_id, turn_id, after_state_version);
