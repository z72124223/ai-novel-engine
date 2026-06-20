# Plan

## Goal

Align documentation after the MVP-7 Candidate A.1 post-push checkpoint. This milestone only records that Candidate A.1 is complete, pushed, and checkpointed.

## Global Strategy

Keep this as a documentation-only checkpoint. Update the few control and MVP-7 documents that still describe Candidate A.1 as pending or historically ambiguous, without changing Kotlin code, tests, runtime behavior, schema, Validator, `stateVersion`, `contextHash`, `resolve-turn`, or `StateReducer`.

## Non-Goals

- No Kotlin code changes.
- No test changes.
- No schema or migration changes.
- No Validator changes.
- No `stateVersion` or `contextHash` changes.
- No `resolve-turn` or `StateReducer` changes.
- No SQLite writes.
- No Candidate B.
- No Candidate A.2.
- No server, API, port, Web UI, Android UI, OpenAI API, Rust, Docker, embedding, vector DB, or multi-agent runtime.
- No retagging `mvp-6`.
- No push in this milestone.

## Scope

- `ROADMAP.md`
- `PLANS.md`
- `docs/MVP7_DECISION_BRIEF.md`
- `docs/MVP7_AUDIT_CALIBRATION_REPORT.md`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- The branch is synced with `origin/codex/mvp6-checkpoint-branching-spec` at `bc3f3843d64ba883890b79464db8785658732cda`.
- `bc3f3843d64ba883890b79464db8785658732cda` is `fix: calibrate narrative quality audit findings`.
- Candidate A is complete.
- Candidate A.1 is complete and has been pushed.
- The post-push checkpoint passed.
- `mvp-6` remains the old stable tag and still points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- At milestone start, `ROADMAP.md` still said the next milestone was to decide whether to push Candidate A.1.
- At milestone start, `docs/MVP7_DECISION_BRIEF.md` reflected the pre-Candidate A/A.1 decision state without a historical-status warning.
- At milestone start, `docs/MVP7_AUDIT_CALIBRATION_REPORT.md` contained both the initial calibration and the later Candidate A.1 result, but the initial status text could read like no audit rules changed.

## Assumptions

- The correct next milestone is to collect more real audit samples or define a new explicit candidate, not to enter Candidate B automatically.
- The docs can state Candidate A.1 post-push status without requiring another runtime verification beyond this milestone's required tests.

## Open Questions

- None blocking this documentation-only alignment.

## Implementation Steps

- [x] Step 1: Read `ROADMAP.md`, `PLANS.md`, `VERIFY.md`, `DONE_CRITERIA.md`, `docs/MVP7_DECISION_BRIEF.md`, `docs/MVP7_AUDIT_CALIBRATION_REPORT.md`, and `docs/MVP7_NARRATIVE_QUALITY_SPEC.md`.
- [x] Step 2: Update this plan for the docs checkpoint alignment.
- [x] Step 3: Align `ROADMAP.md` current/next milestone language and milestone table.
- [x] Step 4: Mark `docs/MVP7_DECISION_BRIEF.md` as a historical decision brief.
- [x] Step 5: Clarify initial calibration versus Candidate A.1 refinements in `docs/MVP7_AUDIT_CALIBRATION_REPORT.md`.
- [x] Step 6: Run verification.
- [x] Step 7: Commit `docs: align MVP-7 Candidate A.1 checkpoint status`.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`

## Risks

- Accidentally making the historical decision brief read like the current roadmap.
- Accidentally implying Candidate B is approved.
- Accidentally editing code or tests outside the approved documentation scope.

## Drift Log

- 2026-06-20: Started docs checkpoint alignment after explicit approval. Scope is limited to four documentation files and excludes all runtime changes.
- 2026-06-20: Aligned roadmap next milestone, marked the decision brief historical, and clarified calibration report timeline semantics.
- 2026-06-20: Verification passed: `git diff --check` and `gradlew.bat clean test --no-daemon` with 227 tests, 0 failures, 0 errors, 0 skipped.
- 2026-06-20: Ready to commit after verification; no runtime files changed.
