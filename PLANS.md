# Plan

## Goal

Complete Pilot-2 Turn 11 from the already prepared official pending context. Do not rerun `prepare-turn`. Draft `turn_011_result.json`, run `validate-result`, resolve the pending turn through formal `resolve-turn`, export evidence, run the read-only narrative audit, update this plan with the 01/02 responsibility handoff, then verify, commit, and push.

## Global Strategy

Treat 01's Turn 11 `prepare-turn` / `audit-context` output as the formal starting point. Use only the existing pending context `ctx-d55d452c-72e7-431c-bf09-79caa803ebe8`. Because the context says `transition-perimeter-to-awning-witnessed` has `prerequisitesSatisfied=false`, do not force a scene transition. Make the result record procedure progress, limited-liability confirmation, NPC pressure, and consequences while keeping all formal state mutation behind `resolve-turn`.

## Non-Goals

- No Turn 11 `prepare-turn` rerun.
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
- No deleting `data/`, `out/`, or `backups/`.
- No committing ignored `data/` or `out/` artifacts.

## Scope

- `PLANS.md`
- Ignored Pilot-2 Turn 11 artifacts under `out/pilot2/pilot2-qingli-herb-warehouse/turns/`
- Formal `data/pilot2.db` only through approved CLI commands

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Current HEAD before this milestone is `0a23455 docs: record Pilot-2 Turn 10 resolve`.
- Formal Pilot-2 session is `session-ea110b6c-7509-40e6-a786-99cf00d8fc66`.
- Formal Pilot-2 state before resolving Turn 11 is `stateVersion=10` and `nextTurnNumber=11`.
- `pilot-preflight` reports `pendingContextCount=1`; this is expected because 01 already created the Turn 11 pending context.
- 01 already ran Turn 11 `prepare-turn` and `audit-context`; 02 must not rerun prepare.
- Turn 11 context path is `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_011_context.json`.
- Turn 11 player input path is `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_011_player_input.txt`.
- Turn 11 context audit path is `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_011_context_audit.json`.
- Turn 11 context has `contextId=ctx-d55d452c-72e7-431c-bf09-79caa803ebe8`, `baseStateVersion=10`, and `contextHash=499e20d753d4fcfa71d03862f9cee0676d26cb35200f44fc2a31994ec7376951`.
- `audit-context` reported `valid=true`, `actualBytes=22959/24576`, and `hashValid=true`.
- Context `availableTransitions` includes `transition-perimeter-to-awning-witnessed`, but `prerequisitesSatisfied=false` with unmet prerequisite `player intent 'go to awning with witness' not present`.
- Turn 11 player input asks to confirm the wording and formally move along the outer route to the oil-cloth awning, while keeping powder-line, warehouse, seal, door, goods, and no-immediate-open boundaries.

## Assumptions

- A result without `transitionProposal` is the safest valid Turn 11 result because the prepared context did not satisfy the awning transition prerequisite.
- The result should not claim the formal scene transition completed. It may record that NPCs prepare the route, confirm the wording, and create a next-step condition for movement.
- If the runtime rejects the result for the same transition boundary, repair only the result JSON and do not change runtime code.

## Open Questions

- Whether future Turn 12 input should use the exact required phrase `go to awning with witness` if the user still wants the formal awning transition.
- Whether the context-intent mismatch should later be investigated as a separate context-preparation milestone; it is not part of this milestone.

## Implementation Steps

- [x] Step 1: Read control docs, Turn 11 input/context/audit, and current repo/session state.
- [x] Step 2: Confirm Turn 11 pending context exists and do not rerun `prepare-turn`.
- [x] Step 3: Update `PLANS.md` for the 02 handoff and Turn 11后半流程.
- [x] Step 4: Draft `turn_011_result.json` from the existing pending context.
- [x] Step 5: Run `validate-result`.
- [x] Step 6: Run `resolve-turn` only after validation passes.
- [x] Step 7: Export `turn_011_evidence.json`.
- [x] Step 8: Run `audit-narrative-quality` and validate the report as UTF-8 JSON.
- [x] Step 9: Confirm formal state advanced to `stateVersion=11`, `nextTurnNumber=12`, and `pendingContextCount=0`.
- [x] Step 10: Update `PLANS.md` with final Turn 11 evidence and audit results.
- [x] Step 11: Run `git diff --check`.
- [x] Step 12: Run `gradlew.bat clean test --no-daemon`.
- [x] Step 13: Commit `docs: record Pilot-2 Turn 11`.
- [x] Step 14: Push to `origin/codex/mvp6-checkpoint-branching-spec`.

## Verification

- `show-session --db data/pilot2.db --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66`
- `pilot-preflight --db data/pilot2.db --project pilot2 --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66`
- `validate-result --db data/pilot2.db --context out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_011_context.json --result out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_011_result.json`
- `resolve-turn --db data/pilot2.db --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66 --result out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_011_result.json`
- `export-session-evidence --db data/pilot2.db --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66 --out out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_011_evidence.json`
- `audit-narrative-quality --context out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_011_context.json --result out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_011_result.json --evidence out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_011_evidence.json --out out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_011_audit_report.json`
- Explicit UTF-8 JSON parse for `turn_011_audit_report.json`.
- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- `git status`

## Risks

- The player wanted to complete the awning transition, but the prepared context says the transition prerequisite is unmet. Forcing `transitionProposal` would likely fail validation or resolve.
- A narrative that says the party entered `藥倉檐下查驗棚` without `transitionProposal` could trigger implicit-transition validation. Avoid target-scene title language unless the transition is valid.
- `pilot-preflight` will remain `valid=false` until the pending context is resolved; this is expected and should not be treated as a blocker before resolve.

## Drift Log

- 2026-06-21: 01 created the official Turn 11 context/audit before handoff. 02 accepted the pending context as formal state, explicitly removed any plan to rerun `prepare-turn`, and narrowed the milestone to result / validate / resolve / evidence / audit / verification / commit / push.
- 2026-06-21: 02 drafted `turn_011_result.json` without `transitionProposal` because the prepared context listed `transition-perimeter-to-awning-witnessed` as `prerequisitesSatisfied=false`. `validate-result` passed, `resolve-turn` passed, and formal DB advanced to `stateVersion=11`, `nextTurnNumber=12`, `pendingContextCount=0`.
- 2026-06-21: Exported `turn_011_evidence.json`; `audit-narrative-quality` wrote `turn_011_audit_report.json` with `verdict=PASS`, `score=92`, `severity=WARNING`, and one advisory `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE` finding. Post-resolve `pilot-preflight` passed with `valid=true`, `contextBytes=17448/24576`, and `contextHashValid=true`.
- 2026-06-21: `git diff --check` passed and `gradlew.bat clean test --no-daemon` passed before final checkpoint commit/push.
