# Plan

## Goal

Design a Pilot-2 scenario document for collecting more real story-arc audit samples, without changing Kotlin code, audit rules, schema, runtime writes, or Candidate B boundaries.

## Global Strategy

Keep this milestone documentation-only. Use the MVP-7 next-candidate decision brief and A.2 audit reports to define a practical Pilot-2 scenario, per-turn evidence format, naming convention, audit flow, and success/failure criteria. Verify with standard repository checks, commit, and stop without pushing.

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
- `docs/PILOT2_SCENARIO_DESIGN.md`
- `ROADMAP.md` only for a small status update after the design exists.

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Local `HEAD` is `b94caf81f07eade476c37305e5f63f6606bbee31`.
- `mvp-6` remains the old stable tag and points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Working tree was clean before this milestone.
- `docs/MVP7_NEXT_CANDIDATE_DECISION_BRIEF.md` ranks more real audit samples and Pilot-2 scenario design as the safest next directions.
- `docs/MVP7_A2_REAL_SAMPLE_RERUN_REPORT.md` says the current audit is stable but still mostly tested on Pilot-1, Pilot-1b, MVP-6, and fixture families.
- `docs/MVP7_FLAWED_AUDIT_SAMPLE_PACK_REPORT.md` says flawed fixtures are useful but are not enough to proceed to Candidate B.
- `VERIFY.md` defines `audit-narrative-quality` as a read-only JSON audit and documents UTF-8 report validation.
- `DONE_CRITERIA.md` requires `git diff --check` and `gradlew.bat clean test --no-daemon` before commit.
- Existing committed sample families include demo seed/context/result, Pilot-1b coverage pack, setting packs, and the MVP-7 flawed audit sample pack.

## Assumptions

- A documentation-only Pilot-2 scenario design is the smallest safe step because it lets the user provide better real story evidence later without changing runtime behavior now.
- No CLI E2E is needed for this milestone because it changes only documentation and does not alter commands or runtime behavior.

## Open Questions

- The exact Pilot-2 story premise still needs user approval or future author input before formal turns are run.
- Whether Pilot-2 will use the existing snow-dog setting or a new setting pack remains a future decision; this document can propose a recommended scenario without applying it.

## Implementation Steps

- [x] Step 1: Read required control docs, MVP-7 reports, README, and sample inventory.
- [x] Step 2: Update `PLANS.md` for this Pilot-2 design milestone.
- [x] Step 3: Create `docs/PILOT2_SCENARIO_DESIGN.md`.
- [x] Step 4: Update `ROADMAP.md` with a small Pilot-2 design status note.
- [x] Step 5: Verify with `git diff --check`.
- [x] Step 6: Verify with `gradlew.bat clean test --no-daemon`.
- [x] Step 7: Commit `docs: design Pilot-2 scenario`.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- `git status`
- confirm no Kotlin, test, schema, migration, Validator, `resolve-turn`, `StateReducer`, or SQLite files changed

## Risks

- Accidentally making Pilot-2 sound like an approved runtime implementation.
- Accidentally implying Candidate B is approved.
- Designing a scenario so specific that it becomes hardcoded test data rather than a flexible evidence-collection plan.
- Producing a format too vague for the user to provide real turns.

## Drift Log

- 2026-06-20: Started Pilot-2 scenario design after the user approved a documentation-only milestone for settings, acceptance items, and human turn collection format.
- 2026-06-20: Created `docs/PILOT2_SCENARIO_DESIGN.md` with scenario premise, human input format, evidence naming rules, audit commands, and pass/fail criteria. Updated `ROADMAP.md` to list Pilot-2 scenario design as completed locally and to keep Candidate B unapproved.
- 2026-06-20: `git diff --check` passed. The first `clean test` attempt was cut off by tool timeout and left a temporary Java lock outside the repo; after the Java process exited, `gradlew.bat clean test --no-daemon` passed with 233 tests, 0 failures, 0 errors, and 0 skipped.
- 2026-06-20: Sealing this documentation-only milestone in local commit `docs: design Pilot-2 scenario`; no push is performed in this milestone.
