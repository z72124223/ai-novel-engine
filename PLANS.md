# Plan

## Goal

Align control documentation after the automatic loop bootstrap push so future work starts from the correct remote-synced state and does not reopen completed MVP-7 Candidate A.2 follow-up milestones.

## Global Strategy

Keep this milestone documentation-only. Update roadmap/control status to match verified Git evidence, then verify, commit, and push the alignment. Do not change runtime behavior or enter any new product candidate.

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
- No MVP-8.
- No web server, API, Docker, Android UI, Rust, embedding, vector DB, or multi-agent runtime.
- No retagging `mvp-6`.

## Scope

- `ROADMAP.md`
- `PLANS.md`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Local `HEAD` and `origin/codex/mvp6-checkpoint-branching-spec` both point to `fed20562015d536c4f3a356854c7e78c17470df3`.
- The previous push included `737ea09 docs: harden audit JSON verification instructions` and `fed2056 docs: record automatic stability loop start`.
- Working tree was clean after the push.
- `mvp-6` remains the old stable tag and points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- `ROADMAP.md` previously still listed A.2 real sample rerun as a future option even though `4786d25 docs: rerun MVP-7 A.2 audit on real samples` exists in history.
- `ROADMAP.md` did not yet list the audit JSON robustness investigation, JSON verification docs hardening, or automatic loop bootstrap.

## Assumptions

- Updating `ROADMAP.md` and `PLANS.md` is sufficient for this checkpoint; `VERIFY.md` already contains the hardened JSON verification instructions.
- The next safest substantive milestone after this alignment is more diverse read-only audit sample collection, unless a new candidate brief is explicitly selected.

## Open Questions

- None blocking this milestone.

## Implementation Steps

- [x] Step 1: Read current Git state and control docs.
- [x] Step 2: Update `ROADMAP.md` with completed A.2 rerun, JSON robustness, JSON verification hardening, and automatic loop bootstrap status.
- [x] Step 3: Update `PLANS.md` for this docs alignment milestone.
- [x] Step 4: Verify with `git diff --check`.
- [x] Step 5: Verify with `gradlew.bat clean test --no-daemon`.
- [x] Step 6: Commit `docs: align automatic loop checkpoint status`.
- [ ] Step 7: Push `codex/mvp6-checkpoint-branching-spec` to origin and confirm remote state.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- `git status`
- `git ls-remote --heads origin codex/mvp6-checkpoint-branching-spec`
- `git rev-parse "mvp-6^{}"`

## Risks

- Accidentally implying Candidate B is approved.
- Rewriting historical roadmap milestones instead of appending current status.
- Creating documentation churn without improving restartability.

## Drift Log

- 2026-06-20: Started control docs alignment after successful automatic loop bootstrap push. Scope is documentation-only.
- 2026-06-20: Verification passed before commit. `git diff --check` reported no whitespace errors. `gradlew.bat clean test --no-daemon` exited `0` with Temurin JDK 17.0.19 and `233 tests / 0 failures / 0 errors / 0 skipped`.
- 2026-06-20: Created docs-only commit `docs: align automatic loop checkpoint status`.
