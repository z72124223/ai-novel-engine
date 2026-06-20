# Plan

## Goal

Align the secondary roadmap document with the latest remote restore smoke evidence so it no longer reports the old 220-test restore baseline.

## Global Strategy

Keep this milestone documentation-only. Use the current Git evidence and prior verified remote-restore command output to update the stale `docs/ROADMAP.md` line, then verify, commit, and push. Do not change runtime behavior or start any new product candidate.

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
- No Candidate A.3 implementation.
- No MVP-8.
- No web server, API, Docker, Android UI, Rust, embedding, vector DB, or multi-agent runtime.
- No retagging `mvp-6`.
- No edits to `data/`, `out/`, `backups/`, or generated build artifacts.

## Scope

- `PLANS.md`
- `docs/ROADMAP.md`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Local `HEAD` and `origin/codex/mvp6-checkpoint-branching-spec` both point to `4c47c3438dec86723f0abb21db38e090282ceae7`.
- `mvp-6` remains the old stable tag and points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Working tree was clean before this milestone.
- Root `ROADMAP.md`, `docs/HANDOFF.md`, and `docs/PROJECT_COMPLETION_AUDIT.md` record the current 233-test baseline.
- Before this milestone, `docs/ROADMAP.md` still reported the old 220-test remote restore baseline instead of the current 233-test baseline.
- The latest remote restore smoke milestone verified a temporary clone from `origin/codex/mvp6-checkpoint-branching-spec` at `0019da552f482898956715f239938d3a4e1b2608`, and the current branch has since advanced to `4c47c3438dec86723f0abb21db38e090282ceae7` with only `PLANS.md` evidence changes.

## Assumptions

- Updating the stale `docs/ROADMAP.md` test baseline improves restartability without needing a new CLI E2E.
- No runtime CLI E2E is needed because this milestone changes documentation only.

## Open Questions

- None blocking this milestone.

## Implementation Steps

- [x] Step 1: Read `ROADMAP.md`, `VERIFY.md`, `docs/ROADMAP.md`, and Git evidence.
- [x] Step 2: Update `PLANS.md` for this secondary-roadmap alignment milestone.
- [x] Step 3: Update `docs/ROADMAP.md` remote restore test baseline from the old 220-test baseline to the verified 233-test baseline.
- [x] Step 4: Verify with `git diff --check`.
- [x] Step 5: Verify with `gradlew.bat clean test --no-daemon`.
- [x] Step 6: Commit `docs: align secondary roadmap restore baseline`.
- [x] Step 7: Push `codex/mvp6-checkpoint-branching-spec` to origin and confirm remote state.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- `git status`
- `git ls-remote --heads origin codex/mvp6-checkpoint-branching-spec`
- `git rev-parse "mvp-6^{}"`

## Risks

- Overstating remote restore evidence beyond what was actually verified.
- Accidentally treating this documentation alignment as Candidate B approval.
- Creating churn in historical documents that are better left unchanged.

## Drift Log

- 2026-06-20: Started secondary roadmap alignment after `git grep` found `docs/ROADMAP.md` still reporting the old 220-test remote restore baseline while current control docs record 233 tests.
- 2026-06-20: Updated `docs/ROADMAP.md` to reference the latest verified 233-test restore baseline.
- 2026-06-20: Verification passed: `git diff --check` reported no whitespace errors, and `gradlew.bat clean test --no-daemon` completed with `BUILD SUCCESSFUL`.
- 2026-06-20: Sealed this secondary-roadmap alignment as a documentation-only commit and prepared it for push to `origin/codex/mvp6-checkpoint-branching-spec`.
