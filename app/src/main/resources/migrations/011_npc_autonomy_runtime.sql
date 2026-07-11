CREATE TABLE IF NOT EXISTS npc_decision_traces (
    trace_id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    session_id TEXT NOT NULL,
    turn_number INTEGER NOT NULL,
    npc_id TEXT NOT NULL,
    scenario_seed TEXT NOT NULL,
    decision_trace_json TEXT NOT NULL,
    proposal_json TEXT NOT NULL,
    resolution_json TEXT NOT NULL,
    status TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_npc_decision_traces_session_turn
    ON npc_decision_traces(session_id, turn_number, npc_id);

CREATE TABLE IF NOT EXISTS sandbox_world_patch_candidates (
    patch_id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    scenario_seed TEXT NOT NULL,
    candidate_json TEXT NOT NULL,
    status TEXT NOT NULL CHECK(status IN ('SANDBOX_CANDIDATE', 'SANDBOX_APPLIED', 'REJECTED')),
    canon_mutation_allowed INTEGER NOT NULL DEFAULT 0 CHECK(canon_mutation_allowed = 0),
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);
