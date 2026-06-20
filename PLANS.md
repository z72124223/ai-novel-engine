# Plan

## Goal

Investigate the Pilot-2 Turn 4 context budget failure using existing ignored forensics artifacts, then document the cause and recommended next repair scope. This milestone is read-only with respect to runtime state: do not rerun Turn 4, do not create a result, do not resolve the turn, and do not modify SQLite.

## Global Strategy

Use the already generated `turn_004_context_budget*.json` and failed-prepare evidence to identify which ContextPacket sections remain too large after trimming. Record confirmed facts only; mark any repair direction as a recommendation for a future milestone, not an implementation in this one.

## Non-Goals

- No Turn 4 rerun.
- No `turn_004_context.json` generation.
- No `turn_004_result.json` generation.
- No `validate-result`.
- No `resolve-turn`.
- No SQLite writes.
- No Kotlin changes.
- No test changes.
- No schema or migration changes.
- No Validator changes.
- No `stateVersion` or `contextHash` changes.
- No `resolve-turn` or `StateReducer` changes.
- No Candidate B.
- No push in this milestone.

## Scope

- `PLANS.md`
- `docs/PILOT2_TURN4_CONTEXT_BUDGET_FORENSICS.md`

Ignored inputs used for evidence:

- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_004_player_input.txt`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_004_context_budget.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_004_context_budget_failure.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_004_evidence_after_failed_prepare.json`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Working tree was clean before this milestone.
- Pilot-2 Turn 4 did not formally execute.
- `prepare-turn` failed with `CONTEXT_QUALITY_UNSATISFIABLE`.
- Pilot-2 session remains at `stateVersion=3` and `nextTurnNumber=4`.
- No pending context remained after the failed prepare attempt.
- The failed candidate was `53956` UTF-8 bytes before trimming and `26885` UTF-8 bytes after trimming against a hard max of `24576`, leaving `2309` bytes over.
- The main retained byte pressure after trimming is in `relevantSettings`, `characterRuntimeStates`, `facts`, `characterKnowledge`, `playerInput`, and `availableTransitions`.

## Assumptions

- The ignored forensics JSON files are sufficient for a documentation-only diagnosis.
- Because this milestone is read-only, any proposed repair must be deferred to a separate approved milestone.
- The correct fix should preserve the `24576` byte hard max and semantic floor rather than delete safety-critical context.

## Open Questions

- Whether the next milestone should implement compact transition/setting cards, compact runtime state flags, or both.
- Whether Turn 4's movement wording should trigger all transition constraints, or whether the semantic planner can distinguish "step back within the same scene" from "request a scene transition" without weakening safety.

## Implementation Steps

- [x] Step 1: Read required Pilot-2 turn artifacts and current session evidence.
- [x] Step 2: Read Pilot-2 scenario/template docs and context budget/relevance/normalization implementation.
- [x] Step 3: Update `PLANS.md` for this read-only forensics milestone.
- [x] Step 4: Create `docs/PILOT2_TURN4_CONTEXT_BUDGET_FORENSICS.md`.
- [x] Step 5: Verify with `git diff --check`.
- [x] Step 6: Verify with `gradlew.bat clean test --no-daemon`.
- [x] Step 7: Commit `docs: investigate Pilot-2 Turn 4 context budget failure`.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- confirm only documentation files are tracked as changed
- confirm no Turn 4 context/result was generated
- confirm no DB, schema, Kotlin, tests, Validator, `resolve-turn`, `StateReducer`, `stateVersion`, or `contextHash` changes

## Risks

- Overstating a recommendation as a confirmed root cause. Mitigation: separate confirmed byte evidence from inferred repair direction.
- Accidentally implying that raising the hard byte limit is acceptable. Mitigation: explicitly list it as not recommended.
- Treating missing retrieved memories as the cause. Mitigation: report that retrieved memories were already fully trimmed after budget fitting.

## Drift Log

- 2026-06-21: Started Pilot-2 Turn 4 context budget forensics after user approved read-only analysis. No Turn 4 rerun, result generation, validation, resolve, or DB mutation is allowed in this milestone.
- 2026-06-21: Added `docs/PILOT2_TURN4_CONTEXT_BUDGET_FORENSICS.md`; `git diff --check` passed, and `gradlew.bat clean test --no-daemon` passed with 233 tests, 0 failures, 0 errors, and 0 skipped using a per-command temporary JDK 17 `JAVA_HOME`.
- 2026-06-21: Sealed the milestone in commit `docs: investigate Pilot-2 Turn 4 context budget failure`.
