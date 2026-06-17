PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS story_sessions (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    current_scene_id TEXT NOT NULL,
    next_turn_number INTEGER NOT NULL DEFAULT 1,
    state_version INTEGER NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'active',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY(current_scene_id) REFERENCES scenes(id) ON DELETE CASCADE
);

ALTER TABLE turn_logs ADD COLUMN session_id TEXT;
ALTER TABLE turn_logs ADD COLUMN context_id TEXT;
ALTER TABLE turn_logs ADD COLUMN result_id TEXT;
ALTER TABLE turn_logs ADD COLUMN base_state_version INTEGER;
ALTER TABLE turn_logs ADD COLUMN context_hash TEXT;
ALTER TABLE turn_logs ADD COLUMN turn_summary TEXT NOT NULL DEFAULT '';
ALTER TABLE turn_logs ADD COLUMN validation_status TEXT NOT NULL DEFAULT 'applied';

ALTER TABLE scenes ADD COLUMN progress_json TEXT NOT NULL DEFAULT '{}';
ALTER TABLE scenes ADD COLUMN completed_requirements_json TEXT NOT NULL DEFAULT '[]';

CREATE UNIQUE INDEX IF NOT EXISTS idx_turn_logs_result_id_unique
ON turn_logs(result_id)
WHERE result_id IS NOT NULL AND result_id != '';

CREATE UNIQUE INDEX IF NOT EXISTS idx_turn_logs_session_turn_applied_unique
ON turn_logs(session_id, turn_number)
WHERE session_id IS NOT NULL AND validation_status = 'applied';

CREATE UNIQUE INDEX IF NOT EXISTS idx_turn_logs_pending_context_unique
ON turn_logs(context_id)
WHERE context_id IS NOT NULL AND validation_status = 'pending';

CREATE INDEX IF NOT EXISTS idx_story_sessions_project
ON story_sessions(project_id);
