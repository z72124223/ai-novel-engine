CREATE TABLE IF NOT EXISTS reboot_drama_evaluations (
    evaluation_id TEXT PRIMARY KEY,
    run_id TEXT NOT NULL,
    project_id TEXT NOT NULL,
    session_id TEXT NOT NULL,
    turn_number INTEGER NOT NULL,
    scenario_seed TEXT NOT NULL,
    raw_draft TEXT NOT NULL,
    raw_scores_json TEXT NOT NULL,
    revision_suggestion TEXT NOT NULL,
    revised_draft TEXT NOT NULL,
    revised_scores_json TEXT NOT NULL,
    revision_count INTEGER NOT NULL CHECK(revision_count BETWEEN 0 AND 1),
    selected_draft TEXT NOT NULL,
    scene_frame_json TEXT NOT NULL,
    expectation_ledger_json TEXT NOT NULL,
    findings_json TEXT NOT NULL,
    stall_status TEXT NOT NULL,
    created_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_reboot_drama_evaluations_run_turn
    ON reboot_drama_evaluations(run_id, turn_number);
