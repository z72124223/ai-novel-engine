PRAGMA foreign_keys = ON;

ALTER TABLE story_sessions ADD COLUMN branch_id TEXT;
ALTER TABLE turn_logs ADD COLUMN branch_id TEXT;

CREATE TABLE IF NOT EXISTS timeline_branches (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    label TEXT NOT NULL DEFAULT '',
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    parent_branch_id TEXT,
    source_checkpoint_id TEXT,
    active_session_id TEXT,
    created_from_session_id TEXT,
    created_from_turn_number INTEGER NOT NULL DEFAULT 0,
    created_from_state_version INTEGER NOT NULL DEFAULT 0,
    reason TEXT NOT NULL DEFAULT '',
    metadata_json TEXT NOT NULL DEFAULT '{}',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY(parent_branch_id) REFERENCES timeline_branches(id) ON DELETE SET NULL,
    FOREIGN KEY(active_session_id) REFERENCES story_sessions(id) ON DELETE SET NULL,
    FOREIGN KEY(created_from_session_id) REFERENCES story_sessions(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS session_checkpoints (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    session_id TEXT NOT NULL,
    branch_id TEXT,
    turn_number INTEGER NOT NULL,
    state_version INTEGER NOT NULL,
    source_turn_log_id TEXT,
    kind TEXT NOT NULL,
    label TEXT NOT NULL DEFAULT '',
    reason TEXT NOT NULL DEFAULT '',
    snapshot_hash TEXT NOT NULL,
    snapshot_json TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY(session_id) REFERENCES story_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY(branch_id) REFERENCES timeline_branches(id) ON DELETE SET NULL,
    FOREIGN KEY(source_turn_log_id) REFERENCES turn_logs(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS timeline_events (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    branch_id TEXT,
    session_id TEXT,
    event_type TEXT NOT NULL,
    source_checkpoint_id TEXT,
    details_json TEXT NOT NULL DEFAULT '{}',
    created_at TEXT NOT NULL,
    FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY(branch_id) REFERENCES timeline_branches(id) ON DELETE SET NULL,
    FOREIGN KEY(session_id) REFERENCES story_sessions(id) ON DELETE SET NULL,
    FOREIGN KEY(source_checkpoint_id) REFERENCES session_checkpoints(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS replay_audit_runs (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    session_id TEXT NOT NULL,
    branch_id TEXT,
    status TEXT NOT NULL,
    checked_turn_count INTEGER NOT NULL DEFAULT 0,
    report_json TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY(session_id) REFERENCES story_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY(branch_id) REFERENCES timeline_branches(id) ON DELETE SET NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_timeline_branches_one_active
ON timeline_branches(project_id)
WHERE status = 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_timeline_branches_project
ON timeline_branches(project_id, status, created_at);

CREATE INDEX IF NOT EXISTS idx_session_checkpoints_session
ON session_checkpoints(session_id, state_version, turn_number);

CREATE INDEX IF NOT EXISTS idx_session_checkpoints_branch
ON session_checkpoints(branch_id, created_at);

CREATE INDEX IF NOT EXISTS idx_timeline_events_branch
ON timeline_events(project_id, branch_id, created_at);

CREATE INDEX IF NOT EXISTS idx_replay_audit_runs_session
ON replay_audit_runs(session_id, created_at);
