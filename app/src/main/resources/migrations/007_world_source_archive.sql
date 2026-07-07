CREATE TABLE IF NOT EXISTS world_source_archives (
    archive_id TEXT PRIMARY KEY,
    project_label TEXT NOT NULL,
    provided_source_title TEXT NOT NULL,
    title_relationship_status TEXT NOT NULL,
    status TEXT NOT NULL,
    locale TEXT NOT NULL,
    manifest_path TEXT NOT NULL,
    manifest_json TEXT NOT NULL,
    raw_text_unmodified INTEGER NOT NULL,
    classification_only INTEGER NOT NULL,
    content_rewrite_allowed INTEGER NOT NULL,
    content_summary_allowed INTEGER NOT NULL,
    official_source_data INTEGER NOT NULL,
    source_authority_status TEXT NOT NULL,
    canon_approved INTEGER NOT NULL,
    setting_pack_data INTEGER NOT NULL,
    runtime_sqlite_data INTEGER NOT NULL,
    approved_asset_data INTEGER NOT NULL,
    source_count INTEGER NOT NULL,
    total_byte_size INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS world_source_documents (
    document_id TEXT PRIMARY KEY,
    archive_id TEXT NOT NULL,
    project_label TEXT NOT NULL,
    source_id TEXT NOT NULL,
    document_order INTEGER NOT NULL,
    classification TEXT NOT NULL,
    title TEXT NOT NULL,
    raw_path TEXT NOT NULL,
    original_attachment_path TEXT,
    source_kind TEXT NOT NULL,
    copy_mode TEXT NOT NULL,
    byte_size INTEGER NOT NULL,
    sha256 TEXT NOT NULL,
    raw_text TEXT NOT NULL,
    raw_text_unmodified INTEGER NOT NULL,
    official_source_data INTEGER NOT NULL,
    source_authority_status TEXT NOT NULL,
    metadata_json TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (archive_id) REFERENCES world_source_archives(archive_id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_world_source_documents_archive_source
    ON world_source_documents(archive_id, source_id);

CREATE INDEX IF NOT EXISTS idx_world_source_documents_project_classification
    ON world_source_documents(project_label, classification, document_order);

CREATE TABLE IF NOT EXISTS world_source_chunks (
    chunk_id TEXT PRIMARY KEY,
    document_id TEXT NOT NULL,
    archive_id TEXT NOT NULL,
    project_label TEXT NOT NULL,
    source_id TEXT NOT NULL,
    chunk_order INTEGER NOT NULL,
    classification TEXT NOT NULL,
    title TEXT NOT NULL,
    heading TEXT NOT NULL,
    content TEXT NOT NULL,
    search_text TEXT NOT NULL,
    byte_size INTEGER NOT NULL,
    sha256 TEXT NOT NULL,
    official_source_data INTEGER NOT NULL,
    source_authority_status TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (document_id) REFERENCES world_source_documents(document_id) ON DELETE CASCADE,
    FOREIGN KEY (archive_id) REFERENCES world_source_archives(archive_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_world_source_chunks_project_classification
    ON world_source_chunks(project_label, classification, source_id, chunk_order);

CREATE INDEX IF NOT EXISTS idx_world_source_chunks_source
    ON world_source_chunks(archive_id, source_id, chunk_order);
