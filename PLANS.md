# Plan

## Goal

Align documentation after MVP-7 Candidate A.2 implementation was completed, pushed, and post-push checkpointed.

## Global Strategy

Only update status and verification documentation. Do not change Kotlin code, tests, audit rules, schemas, runtime write paths, or Candidate B planning. Preserve the current Git stable point and keep `mvp-6` as the old MVP-6 release tag.

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
- No Candidate A.3.
- No retagging `mvp-6`.
- No push in this milestone.

## Scope

- `PLANS.md`
- `ROADMAP.md`
- `VERIFY.md`
- `docs/MVP7_CANDIDATE_A2_CALIBRATION_PLAN.md`
- `docs/MVP7_FLAWED_AUDIT_SAMPLE_PACK_REPORT.md`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Local `HEAD` and `origin/codex/mvp6-checkpoint-branching-spec` are synced at `e6f56006f90ba2bd419defe3e14041e46525a9e3`.
- Working tree was clean before this documentation alignment milestone.
- `mvp-6` remains the old stable tag and points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Candidate A.2 implementation was completed and pushed.
- Candidate A.2 latest verification baseline was `233 tests / 0 failures / 0 errors / 0 skipped`.
- `gradlew.bat :app:installDist --no-daemon` passed for Candidate A.2.
- Post-push checkpoint verdict was `PASS_WITH_DOC_GAPS`.
- The doc gaps were status drift only: `ROADMAP.md`, `PLANS.md`, and historical MVP-7 reports still described pre-A.2 or planning state.

## Assumptions

- `VERIFY.md` should include a compact Candidate A.2 audit calibration smoke because A.2 added refined sample/flawed-case expectations.
- Historical reports should keep old result tables intact and add post-A.2 notes instead of rewriting history.

## Open Questions

- Whether to run a fresh A.2 real sample rerun as the next milestone.
- Whether to collect more diverse non-Pilot-1 audit samples before any Candidate B decision brief.

## Implementation Steps

- [x] Step 1: Read `strata-agent-loop`, Git status, remote head, tag state, and the requested docs.
- [x] Step 2: Update `PLANS.md` for this docs checkpoint alignment milestone.
- [x] Step 3: Update `ROADMAP.md` to show Candidate A.2 implementation is complete and pushed.
- [x] Step 4: Update `docs/MVP7_CANDIDATE_A2_CALIBRATION_PLAN.md` with implementation status.
- [x] Step 5: Update `docs/MVP7_FLAWED_AUDIT_SAMPLE_PACK_REPORT.md` with a post-A.2 historical note.
- [x] Step 6: Update `VERIFY.md` with Candidate A.2 flawed sample and installDist audit verification details.
- [x] Step 7: Verify with `git diff --check`.
- [x] Step 8: Verify with `gradlew.bat clean test --no-daemon`.
- [x] Step 9: Commit `docs: align MVP-7 Candidate A.2 checkpoint status`.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- Manual diff review to confirm only allowed documentation files changed.

## Risks

- Accidentally implying Candidate B approval.
- Rewriting historical report results instead of annotating them.
- Letting status docs claim fresh test execution that belongs to an earlier milestone.
- Touching files outside the approved documentation scope.

## Drift Log

- 2026-06-20: Started MVP-7 Candidate A.2 docs checkpoint alignment after explicit approval. Scope is documentation only.
- 2026-06-20: Evidence pass confirmed A.2 implementation is pushed at `e6f56006f90ba2bd419defe3e14041e46525a9e3`, while several docs still reflected pre-A.2 or planning state.
- 2026-06-20: Verification passed: `git diff --check` reported no whitespace errors, and `gradlew.bat clean test --no-daemon` completed with `233 tests / 0 failures / 0 errors / 0 skipped`.
- 2026-06-20: Created docs-only commit `docs: align MVP-7 Candidate A.2 checkpoint status`; no push performed.
