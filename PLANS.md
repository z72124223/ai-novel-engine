# Plan

## Goal

Draft a new MVP-7 next-candidate decision brief that compares possible next directions without implementing any runtime, audit-rule, schema, or write-path changes.

## Global Strategy

Keep this milestone documentation-only. Use the completed Candidate A/A.1/A.2 reports and current project control docs to compare next-step options, make the safest recommendation explicit, and preserve the boundary that Candidate B is not approved. Verify, commit, and stop without pushing.

## Non-Goals

- No Kotlin code changes.
- No test changes.
- No audit rule changes.
- No schema or migration changes.
- No Validator changes.
- No `stateVersion` or `contextHash` changes.
- No `resolve-turn` or `StateReducer` changes.
- No SQLite writes.
- No Candidate B implementation.
- No Candidate A.3 implementation.
- No MVP-8.
- No UI implementation.
- No web server, API, Docker, Android UI, Rust, embedding, vector DB, or multi-agent runtime.
- No retagging `mvp-6`.
- No push unless the user explicitly approves later.

## Scope

- `PLANS.md`
- `docs/MVP7_NEXT_CANDIDATE_DECISION_BRIEF.md`
- `ROADMAP.md` only if needed; current plan is to avoid changing it unless the decision brief reveals a control-doc conflict.

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Local `HEAD` and `origin/codex/mvp6-checkpoint-branching-spec` both point to `77f5173c9eb00009bb58783293c8c8a7f7968d9e`.
- `mvp-6` remains the old stable tag and points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Working tree was clean before this milestone.
- MVP-7 Candidate A, A.1, and A.2 are complete.
- Candidate A.2 remains a read-only audit calibration and did not change `resolve-turn`, `StateReducer`, SQLite writes, Validator, `stateVersion`, or `contextHash`.
- `docs/PROJECT_COMPLETION_AUDIT.md` says remaining progress needs either real author-led story samples or an explicit new candidate decision.
- `ROADMAP.md` says Candidate B, Candidate A.3, and MVP-8 are not approved by default.
- `docs/MVP7_A2_REAL_SAMPLE_RERUN_REPORT.md` says A.2 stayed stable, but Candidate B is not ready.
- `docs/MVP7_FLAWED_AUDIT_SAMPLE_PACK_REPORT.md` says some deliberately flawed cases remain advisory and not strong enough for Candidate B gating.

## Assumptions

- A decision brief is the smallest safe step because it narrows the user's next choice without changing runtime behavior.
- No CLI E2E is needed because this milestone changes documentation only.

## Open Questions

- Which direction the user will choose after reading the brief remains intentionally open.

## Implementation Steps

- [x] Step 1: Read required control docs and MVP-7 evidence reports.
- [x] Step 2: Update `PLANS.md` for this decision-brief milestone.
- [x] Step 3: Create `docs/MVP7_NEXT_CANDIDATE_DECISION_BRIEF.md`.
- [x] Step 4: Decide whether `ROADMAP.md` needs a small status note; avoid changing it if not needed.
- [x] Step 5: Verify with `git diff --check`.
- [x] Step 6: Verify with `gradlew.bat clean test --no-daemon`.
- [x] Step 7: Commit `docs: draft MVP-7 next candidate decision brief`.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- `git status`
- confirm no Kotlin/test/schema/runtime files changed

## Risks

- Accidentally making Candidate B sound approved.
- Recommending UI/product work before the runtime and sample evidence justify it.
- Treating synthetic fixture audit results as enough for a gate.
- Writing a brief that is too abstract for the user to act on.

## Drift Log

- 2026-06-20: Started MVP-7 next-candidate decision brief after the automatic stable-completion loop stopped because remaining work required human direction.
- 2026-06-20: Created `docs/MVP7_NEXT_CANDIDATE_DECISION_BRIEF.md` and updated `ROADMAP.md` to point next work toward real samples, Pilot-2 design, or a focused Candidate A.3 brief while keeping Candidate B unapproved.
- 2026-06-20: Verification passed: `git diff --check` reported no whitespace errors, and `gradlew.bat clean test --no-daemon` completed with `BUILD SUCCESSFUL`.
- 2026-06-20: Sealed this decision brief in a local documentation-only commit. Per user instruction, it has not been pushed.
