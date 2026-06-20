# Plan

## Goal

Refresh stale project structure and historical MVP-7/blueprint documents so they no longer describe MVP-6 as uncommitted or MVP-7 Candidate A/A.2 as not yet completed.

## Global Strategy

Keep this milestone documentation-only. Update current-state claims where they are misleading, preserve historical context where useful, verify, commit, and push. Do not change runtime behavior or open a new product candidate.

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

## Scope

- `PLANS.md`
- `docs/PROJECT_STRUCTURE_REPORT.md`
- `docs/BLUEPRINT_ALIGNMENT.md`
- `docs/MVP7_DECISION_BRIEF.md`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Local `HEAD` and `origin/codex/mvp6-checkpoint-branching-spec` both point to `3cf9a8081bb05a8e68bb85060e71d663d1d527bc`.
- `mvp-6` remains the old stable tag and points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Working tree was clean before this milestone.
- `docs/PROJECT_STRUCTURE_REPORT.md` still says MVP-6 is not committed, `HEAD` is `a8131a2`, and tests are 220.
- `docs/BLUEPRINT_ALIGNMENT.md` says `ROADMAP.md` says MVP-7 is not approved, but current roadmap records approved read-only MVP-7 Candidate A/A.1/A.2 work as completed while Candidate B remains unapproved.
- `docs/MVP7_DECISION_BRIEF.md` is intentionally historical but its current-status note only mentions Candidate A.1.

## Assumptions

- Updating these docs improves restartability and prevents a future Codex window from following stale MVP-6 instructions.
- No CLI E2E is needed because this milestone changes documentation only.

## Open Questions

- None blocking this milestone.

## Implementation Steps

- [x] Step 1: Read stale documents and current Git evidence.
- [x] Step 2: Update `PLANS.md` for this stale-doc refresh milestone.
- [x] Step 3: Refresh `docs/PROJECT_STRUCTURE_REPORT.md`.
- [x] Step 4: Update `docs/BLUEPRINT_ALIGNMENT.md` current anchors.
- [x] Step 5: Update `docs/MVP7_DECISION_BRIEF.md` historical status note.
- [x] Step 6: Verify with `git diff --check`.
- [x] Step 7: Verify with `gradlew.bat clean test --no-daemon`.
- [x] Step 8: Commit `docs: refresh project structure and MVP-7 status notes`.
- [x] Step 9: Push `codex/mvp6-checkpoint-branching-spec` to origin and confirm remote state.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- `git status`
- `git ls-remote --heads origin codex/mvp6-checkpoint-branching-spec`
- `git rev-parse "mvp-6^{}"`

## Risks

- Erasing useful historical context in `docs/MVP7_DECISION_BRIEF.md`.
- Implying Candidate B is approved.
- Treating synthetic fixture audit results as product-complete proof.

## Drift Log

- 2026-06-20: Started stale-doc refresh after search found outdated MVP-6 and MVP-7 current-state claims in project structure and historical alignment docs.
- 2026-06-20: Updated `docs/PROJECT_STRUCTURE_REPORT.md`, `docs/BLUEPRINT_ALIGNMENT.md`, and `docs/MVP7_DECISION_BRIEF.md` so current-state notes reflect completed MVP-7 read-only audit work while Candidate B remains unapproved.
- 2026-06-20: Verification passed: `git diff --check` reported no whitespace errors, and `gradlew.bat clean test --no-daemon` passed with 233 tests, 0 failures, 0 errors, 0 skipped on Temurin JDK 17.0.19.
- 2026-06-20: Sealed this stale-document refresh as a documentation-only commit and prepared it for push to `origin/codex/mvp6-checkpoint-branching-spec`.
