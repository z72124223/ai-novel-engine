# Plan

## Goal

Collect more real or near-real MVP-7 Candidate A.1 audit samples, run the existing read-only `audit-narrative-quality` command, and document how the audit behaves across a larger Pilot turn set.

## Global Strategy

Use existing Pilot artifacts only. Do not change Kotlin code, tests, audit rules, schema, Validator, `stateVersion`, `contextHash`, `resolve-turn`, `StateReducer`, or SQLite data. Generate ignored `out/` reports for evidence, then commit only documentation.

## Non-Goals

- No Kotlin code changes.
- No test changes.
- No audit rule changes.
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

- `PLANS.md`
- `docs/MVP7_AUDIT_SAMPLE_COLLECTION_REPORT.md`
- Ignored audit output under `out/mvp7_sample_collection/`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Local branch is synced with `origin/codex/mvp6-checkpoint-branching-spec` at `40032c042bdc885dc1bd0e8736597efaec1310e7`.
- `mvp-6` remains the old stable tag and points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Candidate A and Candidate A.1 are complete.
- Candidate A.1 docs checkpoint is complete and pushed.
- `out/pilot1/rerun/turns` contains context/result pairs for Pilot turns 1 through 12.
- Pilot turns 1 through 9 include per-turn evidence files.
- Pilot turns 10 through 12 do not include per-turn evidence files, but related audit/explain files and whole-session evidence exist.

## Assumptions

- A 10-turn sample set from Pilot turns 1 through 10 is enough to satisfy this milestone's 5-10 sample target while avoiding reusing every prior calibration choice.
- Whole-session evidence can be used only as supporting context in the report, not as a per-turn substitute unless explicitly noted.
- Generated audit reports under `out/` remain ignored and should not be committed.

## Open Questions

- Whether future Candidate B should use this audit as a pre-resolve lint remains undecided and out of scope.
- Whether more deliberately flawed samples are needed should be decided after this collection report.

## Implementation Steps

- [x] Step 1: Confirm git status, recent log, remote, and tags.
- [x] Step 2: Read MVP-7 and control documentation.
- [x] Step 3: Inventory available context/result/evidence samples.
- [x] Step 4: Run `audit-narrative-quality` on 5-10 selected samples.
- [x] Step 5: Write `docs/MVP7_AUDIT_SAMPLE_COLLECTION_REPORT.md`.
- [x] Step 6: Verify with `git diff --check` and `gradlew.bat clean test --no-daemon`.
- [x] Step 7: Commit `docs: collect MVP-7 audit samples`.

## Verification

- Run the existing audit CLI against selected sample pairs.
- Inspect generated audit verdicts, scores, finding counts, and rule IDs.
- Run `git diff --check`.
- Run `gradlew.bat clean test --no-daemon`.

## Risks

- Accidentally treating ignored `out/` files as committable evidence.
- Overstating audit precision from a small sample set.
- Using whole-session evidence as if it were per-turn evidence.
- Letting a documentation-only milestone drift into audit rule changes.

## Drift Log

- 2026-06-20: Started Candidate A.1 sample collection after explicit approval. Scope is read-only audit execution plus documentation.
- 2026-06-20: Confirmed 12 Pilot rerun context/result pairs exist; selected a 10-turn sample target for this milestone.
- 2026-06-20: Ran audit on Pilot rerun Turns 1 through 10. Generated ignored reports under `out/mvp7_sample_collection/`.
- 2026-06-20: Added `docs/MVP7_AUDIT_SAMPLE_COLLECTION_REPORT.md` summarizing verdicts, warning distribution, useful findings, noisy findings, suspected misses, and Candidate B readiness.
- 2026-06-20: Verification passed: `git diff --check` and `gradlew.bat clean test --no-daemon` with 227 tests, 0 failures, 0 errors, 0 skipped.
- 2026-06-20: Committed `docs: collect MVP-7 audit samples`.
