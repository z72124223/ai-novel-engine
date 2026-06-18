PRAGMA foreign_keys = ON;

ALTER TABLE story_sessions ADD COLUMN current_scene_instance_id TEXT;
ALTER TABLE story_sessions ADD COLUMN world_time TEXT NOT NULL DEFAULT '0001-01-01T00:00:00Z';
ALTER TABLE story_sessions ADD COLUMN elapsed_minutes INTEGER NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS story_threads (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    thread_type TEXT NOT NULL,
    title TEXT NOT NULL,
    director_goal TEXT NOT NULL DEFAULT '',
    public_summary TEXT NOT NULL,
    priority INTEGER NOT NULL DEFAULT 0,
    visibility TEXT NOT NULL DEFAULT 'director_only',
    parent_thread_id TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY(parent_thread_id) REFERENCES story_threads(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS session_thread_states (
    session_id TEXT NOT NULL,
    thread_id TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'OPEN',
    progress INTEGER NOT NULL DEFAULT 0,
    urgency INTEGER NOT NULL DEFAULT 0,
    blocked_reason TEXT NOT NULL DEFAULT '',
    last_advanced_turn INTEGER NOT NULL DEFAULT 0,
    evidence_json TEXT NOT NULL DEFAULT '{}',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    PRIMARY KEY(session_id, thread_id),
    FOREIGN KEY(session_id) REFERENCES story_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY(thread_id) REFERENCES story_threads(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS scene_requirements (
    id TEXT PRIMARY KEY,
    scene_id TEXT NOT NULL,
    requirement_type TEXT NOT NULL,
    description TEXT NOT NULL,
    completion_condition_json TEXT NOT NULL DEFAULT '{}',
    failure_condition_json TEXT NOT NULL DEFAULT '{}',
    priority INTEGER NOT NULL DEFAULT 0,
    visibility TEXT NOT NULL DEFAULT 'director_only',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY(scene_id) REFERENCES scenes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS scene_instances (
    id TEXT PRIMARY KEY,
    session_id TEXT NOT NULL,
    scene_id TEXT NOT NULL,
    visit_number INTEGER NOT NULL DEFAULT 1,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    started_turn_number INTEGER NOT NULL,
    ended_turn_number INTEGER,
    entry_reason TEXT NOT NULL DEFAULT '',
    exit_reason TEXT NOT NULL DEFAULT '',
    summary TEXT NOT NULL DEFAULT '',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY(session_id) REFERENCES story_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY(scene_id) REFERENCES scenes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS session_scene_requirement_states (
    session_id TEXT NOT NULL,
    scene_instance_id TEXT NOT NULL,
    requirement_id TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'OPEN',
    progress INTEGER NOT NULL DEFAULT 0,
    evidence_json TEXT NOT NULL DEFAULT '{}',
    completed_turn_number INTEGER,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    PRIMARY KEY(session_id, scene_instance_id, requirement_id),
    FOREIGN KEY(session_id) REFERENCES story_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY(scene_instance_id) REFERENCES scene_instances(id) ON DELETE CASCADE,
    FOREIGN KEY(requirement_id) REFERENCES scene_requirements(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS location_connections (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    from_location_id TEXT NOT NULL,
    to_location_id TEXT NOT NULL,
    connection_type TEXT NOT NULL DEFAULT 'walk',
    bidirectional INTEGER NOT NULL DEFAULT 1,
    travel_minutes INTEGER NOT NULL DEFAULT 0,
    prerequisites_json TEXT NOT NULL DEFAULT '{}',
    restrictions_json TEXT NOT NULL DEFAULT '{}',
    visibility TEXT NOT NULL DEFAULT 'public',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY(from_location_id) REFERENCES locations(id) ON DELETE CASCADE,
    FOREIGN KEY(to_location_id) REFERENCES locations(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS scene_transitions (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    from_scene_id TEXT NOT NULL,
    to_scene_id TEXT NOT NULL,
    transition_type TEXT NOT NULL DEFAULT 'walk',
    prerequisites_json TEXT NOT NULL DEFAULT '{}',
    costs_json TEXT NOT NULL DEFAULT '{}',
    required_player_intent TEXT NOT NULL DEFAULT '',
    forced_transition_allowed INTEGER NOT NULL DEFAULT 0,
    visibility TEXT NOT NULL DEFAULT 'public',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY(from_scene_id) REFERENCES scenes(id) ON DELETE CASCADE,
    FOREIGN KEY(to_scene_id) REFERENCES scenes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS pending_scene_transitions (
    id TEXT PRIMARY KEY,
    session_id TEXT NOT NULL,
    result_id TEXT NOT NULL,
    from_scene_instance_id TEXT NOT NULL,
    to_scene_id TEXT NOT NULL,
    proposal_type TEXT NOT NULL,
    reason TEXT NOT NULL,
    evidence_json TEXT NOT NULL DEFAULT '{}',
    status TEXT NOT NULL DEFAULT 'pending',
    rejection_reason TEXT NOT NULL DEFAULT '',
    created_turn_number INTEGER NOT NULL,
    resolved_turn_number INTEGER,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY(session_id) REFERENCES story_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY(from_scene_instance_id) REFERENCES scene_instances(id) ON DELETE CASCADE,
    FOREIGN KEY(to_scene_id) REFERENCES scenes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS scheduled_world_events (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    session_id TEXT,
    event_type TEXT NOT NULL,
    due_turn_number INTEGER NOT NULL,
    due_world_time TEXT,
    trigger_conditions_json TEXT NOT NULL DEFAULT '{}',
    payload_json TEXT NOT NULL DEFAULT '{}',
    visibility TEXT NOT NULL DEFAULT 'public',
    status TEXT NOT NULL DEFAULT 'scheduled',
    triggered_turn_number INTEGER,
    consumed_turn_number INTEGER,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY(session_id) REFERENCES story_sessions(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_scene_instances_one_active
ON scene_instances(session_id)
WHERE status = 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_scene_instances_session_scene
ON scene_instances(session_id, scene_id, visit_number);

CREATE INDEX IF NOT EXISTS idx_scene_requirements_scene
ON scene_requirements(scene_id, priority);

CREATE INDEX IF NOT EXISTS idx_session_requirement_states_instance
ON session_scene_requirement_states(scene_instance_id, status);

CREATE INDEX IF NOT EXISTS idx_threads_project_priority
ON story_threads(project_id, priority);

CREATE INDEX IF NOT EXISTS idx_scene_transitions_from
ON scene_transitions(project_id, from_scene_id);

CREATE INDEX IF NOT EXISTS idx_world_events_due
ON scheduled_world_events(project_id, session_id, status, due_turn_number);
