PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS setting_packs (
    pack_id TEXT NOT NULL,
    pack_version INTEGER NOT NULL,
    project_id TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    mode TEXT NOT NULL,
    canonical_hash TEXT NOT NULL,
    pack_json TEXT NOT NULL DEFAULT '{}',
    source_json TEXT NOT NULL DEFAULT '{}',
    metadata_json TEXT NOT NULL DEFAULT '{}',
    created_at TEXT NOT NULL,
    PRIMARY KEY(pack_id, pack_version),
    FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS setting_pack_imports (
    id TEXT PRIMARY KEY,
    pack_id TEXT NOT NULL,
    pack_version INTEGER NOT NULL,
    project_id TEXT NOT NULL,
    mode TEXT NOT NULL,
    canonical_hash TEXT NOT NULL,
    source_path TEXT NOT NULL DEFAULT '',
    status TEXT NOT NULL,
    validation_json TEXT NOT NULL DEFAULT '{}',
    diff_json TEXT NOT NULL DEFAULT '{}',
    impact_json TEXT NOT NULL DEFAULT '{}',
    created_at TEXT NOT NULL,
    applied_at TEXT,
    FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS entity_revisions (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    entity_version INTEGER NOT NULL,
    source_pack_id TEXT NOT NULL,
    source_pack_version INTEGER NOT NULL,
    canonical_hash TEXT NOT NULL,
    snapshot_json TEXT NOT NULL,
    visibility TEXT NOT NULL DEFAULT 'public',
    status TEXT NOT NULL DEFAULT 'active',
    created_at TEXT NOT NULL,
    FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY(source_pack_id, source_pack_version) REFERENCES setting_packs(pack_id, pack_version) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS canon_validation_runs (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    pack_id TEXT,
    pack_version INTEGER,
    status TEXT NOT NULL,
    report_json TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS impact_analysis_runs (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    pack_id TEXT,
    pack_version INTEGER,
    status TEXT NOT NULL,
    report_json TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE
);

ALTER TABLE dependency_edges ADD COLUMN source_pack_id TEXT;
ALTER TABLE dependency_edges ADD COLUMN source_pack_version INTEGER;
ALTER TABLE dependency_edges ADD COLUMN visibility TEXT NOT NULL DEFAULT 'public';
ALTER TABLE dependency_edges ADD COLUMN runtime_relevant INTEGER NOT NULL DEFAULT 1;
ALTER TABLE dependency_edges ADD COLUMN strength INTEGER NOT NULL DEFAULT 1;
ALTER TABLE dependency_edges ADD COLUMN reason TEXT NOT NULL DEFAULT '';
ALTER TABLE dependency_edges ADD COLUMN metadata_json TEXT NOT NULL DEFAULT '{}';
ALTER TABLE dependency_edges ADD COLUMN canonical_hash TEXT NOT NULL DEFAULT '';
ALTER TABLE dependency_edges ADD COLUMN status TEXT NOT NULL DEFAULT 'active';

CREATE UNIQUE INDEX IF NOT EXISTS idx_setting_packs_hash
ON setting_packs(pack_id, pack_version, canonical_hash);

CREATE INDEX IF NOT EXISTS idx_setting_pack_imports_project
ON setting_pack_imports(project_id, created_at);

CREATE UNIQUE INDEX IF NOT EXISTS idx_entity_revisions_unique_version
ON entity_revisions(project_id, entity_type, entity_id, entity_version);

CREATE INDEX IF NOT EXISTS idx_entity_revisions_latest
ON entity_revisions(project_id, entity_type, entity_id, entity_version DESC);

CREATE INDEX IF NOT EXISTS idx_dependency_edges_runtime
ON dependency_edges(project_id, from_type, from_id, runtime_relevant, status);

CREATE INDEX IF NOT EXISTS idx_dependency_edges_reverse
ON dependency_edges(project_id, to_type, to_id, runtime_relevant, status);
