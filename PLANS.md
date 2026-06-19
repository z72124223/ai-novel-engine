# Plan

## Goal

Calibrate MVP-7 Candidate A by running the existing read-only `audit-narrative-quality` command against several real Pilot turn context/result/evidence files, then write a report about warning usefulness, noise, missed issues, and next-step readiness.

## Global Strategy

Use existing artifacts only. Do not adjust audit rules, runtime code, schema, Validator, `resolve-turn`, `StateReducer`, `stateVersion`, or `contextHash`. Treat audit output as evidence for a documentation-only calibration report.

## Non-Goals

- No code changes.
- No audit rule changes.
- No SQLite writes.
- No schema or migration changes.
- No `resolve-turn` or `StateReducer` changes.
- No Candidate B.
- No server, API, port, Web UI, Android UI, OpenAI API, Rust, Docker, embedding, vector DB, or multi-agent runtime.
- No retagging, merge, push, or branch changes.

## Scope

- Read current docs and available Pilot artifacts.
- Select 3 to 5 existing Pilot turn context/result/evidence groups.
- Run `audit-narrative-quality` on each group.
- Add `docs/MVP7_AUDIT_CALIBRATION_REPORT.md`.
- Run required verification and commit documentation-only changes.

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- The branch is synced with `origin/codex/mvp6-checkpoint-branching-spec` at `8bdb9f5` before this milestone.
- `mvp-6` exists and points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- `audit-narrative-quality` is implemented as a read-only CLI command that does not require `--db`.
- `docs/MVP7_NARRATIVE_QUALITY_SPEC.md` says Candidate A is implemented and Candidate B is not approved.
- `out/pilot1/rerun/turns` contains formal Pilot context/result files for Turns 1 through 12.
- Per-turn evidence files exist for several Pilot turns, including Turns 4, 8, and 9.
- Final/full evidence artifacts exist under `out/pilot1/rerun` and `out/pilot1/capability_contract`.

## Assumptions

- Turns 4, 8, 9, 10, and 12 provide a useful calibration spread: NPC refusal, memory/transition question, attempted transition, capability use, and final boundary negotiation.
- Generated audit reports under `out/` remain ignored and should not be committed.

## Open Questions

- Whether the current heuristics are useful enough to feed a future Candidate B gate.
- Whether evidence-file repetition warnings are meaningful when the evidence file contains a whole-session export rather than one turn only.

## Implementation Steps

- [x] Step 1: Read control docs, MVP-7 spec, Pilot findings/log, samples, and available `out/` artifacts.
- [x] Step 2: Update this plan for the calibration-only milestone.
- [x] Step 3: Run audit on selected Pilot turn groups and capture stable report JSON under `out/mvp7_calibration/`.
- [x] Step 4: Add `docs/MVP7_AUDIT_CALIBRATION_REPORT.md`.
- [x] Step 5: Verify with `git diff --check`, `gradlew.bat clean test --no-daemon`, and `gradlew.bat :app:installDist`.
- [x] Step 6: Commit documentation-only calibration changes.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- `gradlew.bat :app:installDist`
- Manual CLI audit runs for selected Pilot turns.

## Risks

- The report may overinterpret heuristic warnings as objective literary failure.
- Whole-session evidence can create repeated-narrative warnings that may not belong to one turn.
- If only `PLANS.md` and the calibration report change, the milestone stays documentation-only despite generated ignored reports under `out/`.

## Drift Log

- 2026-06-20: Started calibration-only milestone after explicit approval. Scope is documentation plus ignored audit outputs; no runtime edits.
- 2026-06-20: Audited Pilot Turns 4, 8, 9, 10, and 12 using the installed distribution CLI. Reports were written to ignored `out/mvp7_calibration/`.
- 2026-06-20: Verification passed: `git diff --check`, 225 tests with 0 failures/errors/skips, and `gradlew.bat :app:installDist`.
- 2026-06-20: Prepared documentation-only commit for the calibration report.
