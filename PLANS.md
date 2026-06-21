# Plan

## Goal

Prepare and implement the smallest approved repair for the Pilot-2 transition-refusal false-positive path, using tests first. User/01 approved path D: a minimal transition refusal / Validator / runtime repair. The earlier B probe authorization is narrowed: formal Codex-authored probe content must be produced by 01, not by 02.

## Global Strategy

Use source/test investigation to isolate the smallest failure condition behind `player_refused_transition`: a legal transition proposal with explicit movement consent should not be rejected merely because the player input contains boundaries against other actions. First add or extend regression tests. Only after the test captures the problem, make the narrowest repair in transition-refusal heuristic / `AiTurnResultValidator` / related runtime validation code. Do not change schema, migrations, `stateVersion`, `contextHash`, `resolve-turn`, or StateReducer.

## Non-Goals

- No formal Pilot-2 Turn 14 lifecycle in this milestone.
- No 02-authored Codex probe player input, result JSON, probe narrative, or probe decision content.
- No `prepare-turn`, `validate-result`, or `resolve-turn` against the formal Pilot-2 DB unless 01 later supplies explicit probe content in a new milestone.
- No direct SQLite writes.
- No schema or migration changes.
- No `stateVersion` or `contextHash` calculation changes.
- No `resolve-turn` or StateReducer implementation changes without stopping for 01 approval.
- No Candidate B or MVP-8.
- No server/API/Docker/Android/Rust/embedding/vector DB.
- No deleting `data/`, `out/`, or `backups/`.
- No committing ignored `out/` or `data/` artifacts.
- No `mvp-6` retagging.
- No treating Codex-authored tests or future probes as human-player evidence.

## Scope

Allowed tracked files:

- `PLANS.md`
- `docs/PILOT2_TRANSITION_REFUSAL_MINIMAL_REPAIR_REPORT.md`
- focused Kotlin runtime/Validator files directly related to transition refusal
- focused tests that reproduce and guard the behavior

Likely source/test areas to inspect:

- `app/src/main/kotlin/com/localnovel/engine/service/SceneThreadServices.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/MultiTurnRuntime.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/AiTurnResultValidator.kt`
- `app/src/test/kotlin/com/localnovel/engine/Mvp4SceneThreadRuntimeTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/Pilot1bCoverageRepairTest.kt`

Read-only evidence docs:

- `docs/PILOT2_POST_TURN13_DECISION_HANDOFF.md`
- `docs/PILOT2_EVIDENCE_CHECKPOINT_TURNS_1_13.md`
- `docs/PILOT2_REFUSAL_HEURISTIC_INVESTIGATION_BRIEF.md`
- `docs/PILOT2_TRANSITION_REFUSAL_DECISION_BRIEF.md`
- `docs/PILOT2_CODEX_AUTHORED_INPUT_WORKFLOW.md`
- `docs/PILOT2_TURN13_CODEX_INPUT_OPTIONS.md`
- `docs/PILOT2_TURN_TEMPLATE.md`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Current HEAD before this milestone is `69e7f5c docs: hand off Pilot-2 post-Turn13 decisions`.
- Working tree was clean at milestone start.
- User/01 explicitly selected B + D, then narrowed B: 01 will author any formal Codex-authored probe content.
- 02 is authorized to do D: source/test investigation, focused tests, minimal transition refusal / Validator / runtime repair, docs, verification, commit, and push.
- 02 is not authorized to create formal Pilot-2 probe player input, result JSON, probe narrative, or formal probe decision content in this milestone.
- Prior read-only investigation identified a likely broad full-input refusal scan in `TransitionValidator`.
- Existing evidence says Turn 12 and Turn 13 first results passed result-file validation but failed `resolve-turn` with `player_refused_transition`.
- `mvp-6` must remain `be527031335efe8e2b0facae0145e1bbc084f8c6`.

## Assumptions

- The smallest repair will likely be in transition refusal detection, not in state application.
- A regression test can be added without using the formal Pilot-2 DB or ignored Pilot artifacts.
- If the repair requires changing `resolve-turn` orchestration or StateReducer, the plan becomes invalid and 02 must stop for 01 approval.

## Open Questions

- Which exact source function should own a narrower refusal heuristic: `TransitionValidator` directly or a small helper near it?
- Whether existing tests already provide enough fixture setup to express "movement consent plus non-movement boundaries".
- Whether a CLI E2E is necessary after tests; if only unit-level Validator behavior changes, full Gradle tests may be sufficient, but this will be revisited after source inspection.
- Formal B probe remains pending until 01 supplies probe content and a separate execution instruction.

## Implementation Steps

- [x] Step 1: Read required control docs and Pilot-2 decision/evidence docs.
- [x] Step 2: Run initial `git status` and confirm current HEAD.
- [x] Step 3: Update `PLANS.md` with the corrected boundary: 01 authors formal probe content; 02 only repairs/tests now.
- [x] Step 4: Inspect source/tests for `player_refused_transition`, transition proposal validation, and existing transition refusal tests.
- [x] Step 5: Add a failing regression test for legal transition consent plus unrelated boundaries.
- [x] Step 6: Implement the smallest refusal heuristic repair that makes the new test pass while preserving direct refusal blocking.
- [x] Step 7: Run targeted tests if practical, then `git diff --check` and `gradlew.bat clean test --no-daemon`.
- [x] Step 8: Decide whether a CLI E2E is required; run it only if it can stay within approved boundaries and does not require formal probe content.
- [x] Step 9: Update docs/report notes for repair behavior and the deferred formal B probe.
- [x] Step 10: Commit and push if verification passes.
- [x] Step 11: Directly notify 03 read-only review thread.

## Verification

Required:

- `git status`
- `git diff --check` -> PASS, with expected Windows CRLF working-copy warnings.
- `gradlew.bat clean test --no-daemon` -> PASS: `BUILD SUCCESSFUL in 3m 37s`, `6 actionable tasks: 6 executed`; XML aggregate `240` tests, `0` failures, `0` errors, `0` skipped.

Additional verification:

- Focused regression run -> PASS after repair.
- Related class XML evidence after repair: `Mvp4SceneThreadRuntimeTest` `31/0/0/0`, `Pilot1bCoverageRepairTest` `7/0/0/0`.
- CLI E2E not run in this milestone because 01 withdrew 02 permission to author or execute formal probe content; runtime behavior is covered by temporary-DB tests that call `validate` and `resolveTurn`.
- Confirm no ignored `out/` or `data/` artifacts are staged.
- Confirm `mvp-6` tag remains unchanged.

## Risks

- Weakening player-consent protection by allowing a transition when the player actually refused movement.
- Overfitting to Pilot-2 wording instead of a general distinction between movement refusal and non-movement boundaries.
- Accidentally editing `resolve-turn` or StateReducer, which is not approved.
- Accidentally performing the formal B probe that 01 has reserved for itself.
- Plan status drift after commit/push/03 notification.

## Drift Log

- 2026-06-21: Started Pilot-2 transition refusal minimal repair preparation after 01/user approved D.
- 2026-06-21: User/01 narrowed B: 02 must not author or execute formal Codex-authored probe content in this milestone; formal probe waits for 01-supplied content.
- 2026-06-21: Added regression coverage for explicit transition consent plus unrelated action boundaries; the test failed first with `player_refused_transition`.
- 2026-06-21: Narrowed transition refusal detection to explicit movement/transition refusal phrases and preserved direct-refusal coverage.
- 2026-06-21: Full verification passed; formal B probe remains deferred until 01 supplies probe content.
- 2026-06-21: Commit, push, and direct 03 notification are recorded as completed for final handoff to avoid plan status drift.
