# Plan

## Goal

Complete Pilot-2 Turn 10 through the approved formal CLI path: use the existing pending context, produce a validated result JSON, resolve the turn, export evidence, run the read-only narrative audit, and stop because the next step requires real Turn 11 player input.

## Global Strategy

Keep the milestone inside the approved Pilot-2 turn-collection boundary. Use the existing `turn_010_context.json` and pending context, draft result JSON under ignored `out/`, validate before resolve, resolve only through `resolve-turn`, then export evidence and audit. Do not change Kotlin, tests, schema, Validator, `stateVersion` logic, `contextHash`, `StateReducer`, or Candidate B behavior.

## Non-Goals

- No Kotlin changes.
- No test changes.
- No schema or migration changes.
- No Validator changes.
- No `stateVersion` or `contextHash` logic changes.
- No `resolve-turn` or `StateReducer` implementation changes.
- No direct SQLite writes.
- No Candidate B.
- No MVP-8.
- No `mvp-6` retagging.
- No committing ignored `data/` or `out/` artifacts.

## Scope

- `PLANS.md`
- Ignored Pilot-2 runtime artifacts under `out/pilot2/pilot2-qingli-herb-warehouse/turns/`
- Formal `data/pilot2.db` only through approved CLI commands

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- `mvp-6^{}` points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Formal Pilot-2 Turn 10 already had one pending context before result work:
  - `contextId=ctx-b2359562-807e-4e97-bc4c-f808d2ad710b`
  - `contextHash=d1463cfa291509dabada86d9a199ac8eace36a460b94477639ff79f100751488`
  - `actualBytes=23906/24576`
  - `hashValid=true`
- `turn_010_result.json` was first drafted with a transition proposal, but `resolve-turn` rejected it with `player_refused_transition` because the runtime transition validator detected refusal wording in the player-input boundaries.
- The failed result was not applied; formal DB state did not advance from that failed attempt.
- `turn_010_result_retry_01.json` removed the transition proposal and represented the turn as procedural progress without actual scene transition.
- `validate-result` passed for `turn_010_result_retry_01.json`.
- `resolve-turn` passed for `turn_010_result_retry_01.json`.
- Formal Pilot-2 DB advanced to `stateVersion=10` and `nextTurnNumber=11`.
- `export-session-evidence` after Turn 10 reported `pendingContexts=0`.
- `audit-narrative-quality` for Turn 10 reported `verdict=PASS`, `score=92`, `severity=WARNING`, `findings=1`.
- The single audit warning was `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, consistent with prior Pilot-2 audit observations.

## Assumptions

- The next safe story step requires a real human Turn 11 player input from the user.
- The Turn 10 transition blocker is a runtime validator/string-matching limitation that should not be fixed inside this story-turn milestone.
- If future work wants the exact oil-cloth-awning transition to complete, it should be handled as a separate approved context/transition-intent repair or by a new player input that avoids refusal-boundary collision.

## Open Questions

- What should the player do for Pilot-2 Turn 11?
- Should a future small milestone investigate transition-refusal matching for inputs that include boundary refusals such as "do not touch goods" while still clearly accepting a movement?

## Implementation Steps

- [x] Step 1: Confirm repo, session, Turn 10 context, and pending context state.
- [x] Step 2: Read Turn 10 context, Turn 9 result format, result models, and runtime validation rules.
- [x] Step 3: Draft `turn_010_result.json`.
- [x] Step 4: Run `validate-result` for `turn_010_result.json`.
- [x] Step 5: Attempt `resolve-turn`; observe `player_refused_transition` rejection.
- [x] Step 6: Draft `turn_010_result_retry_01.json` without transition proposal.
- [x] Step 7: Run `validate-result` for `turn_010_result_retry_01.json`.
- [x] Step 8: Run `resolve-turn` for `turn_010_result_retry_01.json`.
- [x] Step 9: Export `turn_010_evidence.json`.
- [x] Step 10: Run `audit-narrative-quality` and save `turn_010_audit_report.json`.
- [ ] Step 11: Run `git diff --check`.
- [ ] Step 12: Run `gradlew.bat clean test --no-daemon`.
- [ ] Step 13: Commit and push the tracked checkpoint update.
- [ ] Step 14: Stop for real Turn 11 player input.

## Verification

- `show-session --db data/pilot2.db --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66`
- `validate-result --db data/pilot2.db --context out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_010_context.json --result out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_010_result_retry_01.json`
- `resolve-turn --db data/pilot2.db --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66 --result out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_010_result_retry_01.json`
- `export-session-evidence --db data/pilot2.db --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66 --out out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_010_evidence.json`
- `audit-narrative-quality --context out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_010_context.json --result out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_010_result_retry_01.json --evidence out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_010_evidence.json --out out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_010_audit_report.json`
- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- `git status`

## Risks

- The successful retry did not complete the oil-cloth-awning scene transition because the validator rejected the normal transition when boundary-refusal wording existed in the player input.
- Turn 10 still made meaningful progress through limited-liability wording, but the transition coverage remains incomplete for this specific turn.
- Pilot-2 still lacks scheduled world event pressure in context; the audit warning remains advisory and should not be treated as a hard failure.

## Drift Log

- 2026-06-21: High-autonomy mode selected Pilot-2 Turn 10 result/validate/resolve after the formal prepare checkpoint was already complete and synced.
- 2026-06-21: First Turn 10 result attempted the explicit awning transition, but `resolve-turn` rejected it with `player_refused_transition`. The result was not applied.
- 2026-06-21: Wrote `turn_010_result_retry_01.json` to preserve player boundaries and avoid forced transition. The retry passed `validate-result`, passed `resolve-turn`, advanced formal DB to `stateVersion=10` and `nextTurnNumber=11`, exported evidence, and produced a `PASS` narrative audit with score `92`.
- 2026-06-21: Stopping after verification will be required because Pilot-2 Turn 11 needs real player input.
