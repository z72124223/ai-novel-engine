# Plan

## Goal

Create a reusable Pilot-2 turn collection template that tells the user and future Codex sessions how to provide player input, save per-turn artifacts, run read-only narrative audit, and decide when to stop, without starting Pilot-2 Turn 1.

## Global Strategy

Keep this milestone documentation-only. Use `docs/PILOT2_SCENARIO_DESIGN.md`, `VERIFY.md`, `README.md`, and the current roadmap to turn the Pilot-2 scenario design into an operational template. Do not execute story turns, do not create result JSON, do not write SQLite, and do not change runtime behavior.

## Non-Goals

- No Pilot-2 Turn 1 execution.
- No Kotlin code changes.
- No test changes.
- No audit rule changes.
- No schema or migration changes.
- No Validator changes.
- No `stateVersion` or `contextHash` changes.
- No `resolve-turn` or `StateReducer` changes.
- No SQLite writes.
- No Candidate B.
- No MVP-8.
- No server, API, Docker, Android UI, Rust, embedding, vector DB, or multi-agent runtime.
- No retagging `mvp-6`.
- No push unless the user explicitly approves later.

## Scope

- `PLANS.md`
- `docs/PILOT2_TURN_TEMPLATE.md`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Local `HEAD` is `fa1d15bc7ee9ce2539b0c62f12e4d2e0ada160c5`.
- Local branch is synchronized with `origin/codex/mvp6-checkpoint-branching-spec`.
- `mvp-6` points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Working tree was clean before this milestone.
- `docs/PILOT2_SCENARIO_DESIGN.md` defines the Pilot-2 scenario, turn data to collect, naming rules, audit execution, success conditions, and failure conditions.
- `VERIFY.md` defines `audit-narrative-quality` as read-only and documents UTF-8 JSON report verification.
- `README.md` lists the relevant CLI commands: `prepare-turn`, `resolve-turn`, `audit-context`, `export-session-evidence`, and `audit-narrative-quality`.
- `ROADMAP.md` says next options after Pilot-2 scenario design include Pilot-2 human turn collection and explicitly says not to enter Candidate B directly.

## Assumptions

- A template document is the smallest safe next step because it reduces future operator confusion without touching the engine.
- No CLI E2E is needed because this milestone changes only documentation and does not alter commands or runtime behavior.

## Open Questions

- The user has not yet provided the actual Pilot-2 Turn 1 player input.
- The actual Pilot-2 DB path, project id, and session id remain future execution details.

## Implementation Steps

- [x] Step 1: Read requested docs and current repo state.
- [x] Step 2: Update `PLANS.md` for the Pilot-2 turn template milestone.
- [x] Step 3: Create `docs/PILOT2_TURN_TEMPLATE.md`.
- [x] Step 4: Verify with `git diff --check`.
- [x] Step 5: Verify with `gradlew.bat clean test --no-daemon`.
- [x] Step 6: Commit `docs: add Pilot-2 turn collection template`.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- `git status`
- confirm no Kotlin, test, schema, migration, Validator, `resolve-turn`, `StateReducer`, or SQLite files changed

## Risks

- Accidentally writing the template as if Pilot-2 Turn 1 has already started.
- Accidentally making Candidate B sound approved.
- Making the template too vague for the user to provide a valid turn.
- Including commands that imply direct SQLite writes or bypass Validator.

## Drift Log

- 2026-06-20: Started Pilot-2 turn collection template milestone after the user approved a documentation-only template and explicitly prohibited starting Turn 1.
- 2026-06-20: Created `docs/PILOT2_TURN_TEMPLATE.md` with player input format, Turn 1 example, artifact naming, per-turn audit commands, quality checklists, stop conditions, and Candidate B reminder.
- 2026-06-20: `git diff --check` passed, and `gradlew.bat clean test --no-daemon` passed with 233 tests, 0 failures, 0 errors, and 0 skipped.
- 2026-06-20: Sealing this documentation-only milestone in local commit `docs: add Pilot-2 turn collection template`; no push is performed in this milestone.
