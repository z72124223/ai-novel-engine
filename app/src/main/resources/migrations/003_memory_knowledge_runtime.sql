PRAGMA foreign_keys = ON;

ALTER TABLE character_memories ADD COLUMN owner_character_id TEXT;
ALTER TABLE character_memories ADD COLUMN memory_type TEXT NOT NULL DEFAULT 'EPISODIC';
ALTER TABLE character_memories ADD COLUMN details_json TEXT NOT NULL DEFAULT '{}';
ALTER TABLE character_memories ADD COLUMN source_type TEXT NOT NULL DEFAULT 'seed';
ALTER TABLE character_memories ADD COLUMN source_id TEXT;
ALTER TABLE character_memories ADD COLUMN subject_type TEXT;
ALTER TABLE character_memories ADD COLUMN subject_id TEXT;
ALTER TABLE character_memories ADD COLUMN tags_json TEXT NOT NULL DEFAULT '[]';
ALTER TABLE character_memories ADD COLUMN trigger_tags_json TEXT NOT NULL DEFAULT '[]';
ALTER TABLE character_memories ADD COLUMN emotional_signature_json TEXT NOT NULL DEFAULT '{}';
ALTER TABLE character_memories ADD COLUMN confidence INTEGER NOT NULL DEFAULT 100;
ALTER TABLE character_memories ADD COLUMN reliability INTEGER NOT NULL DEFAULT 100;
ALTER TABLE character_memories ADD COLUMN created_turn_number INTEGER NOT NULL DEFAULT 0;
ALTER TABLE character_memories ADD COLUMN last_recalled_turn_number INTEGER;
ALTER TABLE character_memories ADD COLUMN recall_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE character_memories ADD COLUMN decay_rate REAL NOT NULL DEFAULT 0.0;
ALTER TABLE character_memories ADD COLUMN status TEXT NOT NULL DEFAULT 'active';
ALTER TABLE character_memories ADD COLUMN is_canon INTEGER NOT NULL DEFAULT 0;
ALTER TABLE character_memories ADD COLUMN supersedes_memory_id TEXT;
ALTER TABLE character_memories ADD COLUMN created_at TEXT;
ALTER TABLE character_memories ADD COLUMN updated_at TEXT;

UPDATE character_memories
SET owner_character_id = COALESCE(owner_character_id, character_id),
    created_turn_number = CASE WHEN created_turn_number = 0 THEN created_turn ELSE created_turn_number END,
    created_at = COALESCE(created_at, datetime('now')),
    updated_at = COALESCE(updated_at, datetime('now'));

CREATE TABLE IF NOT EXISTS character_knowledge (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    character_id TEXT NOT NULL,
    proposition_key TEXT NOT NULL,
    statement TEXT NOT NULL,
    epistemic_state TEXT NOT NULL DEFAULT 'UNKNOWN',
    confidence INTEGER NOT NULL DEFAULT 0,
    source_fact_id TEXT,
    source_memory_id TEXT,
    acquired_turn_number INTEGER NOT NULL DEFAULT 0,
    updated_turn_number INTEGER NOT NULL DEFAULT 0,
    visibility TEXT NOT NULL DEFAULT 'private',
    status TEXT NOT NULL DEFAULT 'active',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY(character_id) REFERENCES characters(id) ON DELETE CASCADE,
    FOREIGN KEY(source_fact_id) REFERENCES facts(id) ON DELETE SET NULL,
    FOREIGN KEY(source_memory_id) REFERENCES character_memories(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS memory_recall_logs (
    id TEXT PRIMARY KEY,
    session_id TEXT NOT NULL,
    turn_number INTEGER NOT NULL,
    context_id TEXT NOT NULL,
    character_id TEXT NOT NULL,
    memory_id TEXT NOT NULL,
    retrieval_score INTEGER NOT NULL,
    retrieval_reasons_json TEXT NOT NULL DEFAULT '[]',
    included_in_context INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    FOREIGN KEY(session_id) REFERENCES story_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY(character_id) REFERENCES characters(id) ON DELETE CASCADE,
    FOREIGN KEY(memory_id) REFERENCES character_memories(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_character_memories_owner_status
ON character_memories(project_id, owner_character_id, status, visibility);

CREATE INDEX IF NOT EXISTS idx_character_memories_recall
ON character_memories(project_id, owner_character_id, salience, last_recalled_turn_number);

CREATE UNIQUE INDEX IF NOT EXISTS idx_character_memories_runtime_dedupe
ON character_memories(project_id, owner_character_id, source_type, COALESCE(source_id, ''), summary)
WHERE status != 'archived';

CREATE UNIQUE INDEX IF NOT EXISTS idx_character_knowledge_character_key
ON character_knowledge(project_id, character_id, proposition_key);

CREATE INDEX IF NOT EXISTS idx_character_knowledge_state
ON character_knowledge(project_id, character_id, epistemic_state, status);

CREATE INDEX IF NOT EXISTS idx_memory_recall_logs_context
ON memory_recall_logs(session_id, context_id, character_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_memory_recall_logs_once
ON memory_recall_logs(session_id, context_id, character_id, memory_id);
