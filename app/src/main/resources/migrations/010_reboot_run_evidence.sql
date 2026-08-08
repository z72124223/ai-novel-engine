CREATE TABLE IF NOT EXISTS reboot_run_evidence (
    run_id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    session_id TEXT NOT NULL,
    turn_number INTEGER NOT NULL,
    scenario_seed TEXT NOT NULL DEFAULT '',
    context_id TEXT NOT NULL,
    context_hash TEXT NOT NULL,
    player_input TEXT NOT NULL,
    context_snapshot_json TEXT NOT NULL,
    result_id TEXT NOT NULL,
    raw_draft TEXT NOT NULL,
    raw_result_json TEXT NOT NULL,
    critic_scores_json TEXT NOT NULL,
    revision_suggestion TEXT NOT NULL,
    revised_draft TEXT NOT NULL,
    validation_findings_json TEXT NOT NULL,
    validation_classification TEXT NOT NULL,
    state_delta_json TEXT NOT NULL,
    irreversible_change_json TEXT NOT NULL,
    irreversible_approval_json TEXT NOT NULL,
    irreversible_approval_actor TEXT NOT NULL,
    irreversible_approval_granted INTEGER NOT NULL DEFAULT 0,
    apply_result_json TEXT NOT NULL,
    model_metadata_json TEXT NOT NULL,
    elapsed_time_ms INTEGER NOT NULL DEFAULT 0,
    status TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_reboot_run_evidence_session_turn
    ON reboot_run_evidence(session_id, turn_number, created_at);

CREATE INDEX IF NOT EXISTS idx_reboot_run_evidence_result
    ON reboot_run_evidence(result_id, created_at);

CREATE INDEX IF NOT EXISTS idx_reboot_run_evidence_classification
    ON reboot_run_evidence(validation_classification, status);
