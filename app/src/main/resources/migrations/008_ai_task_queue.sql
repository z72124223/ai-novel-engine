CREATE TABLE IF NOT EXISTS ai_task_queue (
    task_id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    task_type TEXT NOT NULL,
    owner_role TEXT NOT NULL,
    target_role TEXT NOT NULL,
    priority INTEGER NOT NULL,
    status TEXT NOT NULL,
    source TEXT NOT NULL,
    payload_json TEXT NOT NULL,
    claimed_by TEXT,
    claimed_at TEXT,
    completed_at TEXT,
    result_status TEXT,
    result_summary TEXT,
    result_report_path TEXT,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_ai_task_queue_ready
    ON ai_task_queue(status, priority, created_at);

CREATE INDEX IF NOT EXISTS idx_ai_task_queue_owner_target
    ON ai_task_queue(owner_role, target_role, status, priority);

CREATE TABLE IF NOT EXISTS ai_task_events (
    event_id TEXT PRIMARY KEY,
    task_id TEXT NOT NULL,
    event_type TEXT NOT NULL,
    actor TEXT NOT NULL,
    note TEXT NOT NULL,
    payload_json TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (task_id) REFERENCES ai_task_queue(task_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_ai_task_events_task_time
    ON ai_task_events(task_id, created_at);
