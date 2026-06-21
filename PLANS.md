# Plan

## Goal

Execute Pilot-2 Turn 12 as a Codex-authored player-input transition intent retry. The single story purpose is to test whether the exact legal transition intent `go to awning with witness` can satisfy context / transition prerequisites and, if valid, resolve a formal transition from `scene-qingli-herb-warehouse-perimeter` toward the awning witness position. This input is AI-authored and must not be counted as real human-input evidence.

## Global Strategy

Run the full Pilot-2 per-turn CLI flow on formal `data/pilot2.db`: save the Codex-authored Turn 12 input, run context budget, `prepare-turn`, `audit-context`, draft result only after context audit passes, validate result, resolve through the formal CLI, export evidence, run read-only narrative audit, then verify, commit, and push. Do not modify runtime code, schema, migrations, Validator, `stateVersion`, `contextHash`, `resolve-turn`, or `StateReducer`. If context/transition intent still fails, document the failure and do not force a transition.

## Non-Goals

- No Turn 1-11 reruns.
- No overwriting Turn 1-11 artifacts.
- No Kotlin changes.
- No test changes.
- No schema or migration changes.
- No Validator changes.
- No `stateVersion` or `contextHash` logic changes.
- No `resolve-turn` or `StateReducer` implementation changes.
- No direct SQLite writes.
- No Candidate B.
- No MVP-8.
- No server, API, Docker, Android, Rust, embedding, vector DB, or unrelated technology.
- No `mvp-6` retagging.
- No deleting `data/`, `out/`, or `backups/`.
- No committing ignored `data/` or `out/` artifacts.

## Scope

- `PLANS.md`
- Ignored Pilot-2 Turn 12 artifacts under `out/pilot2/pilot2-qingli-herb-warehouse/turns/`
- Formal `data/pilot2.db` only through approved CLI commands

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Current HEAD before this milestone is `537118e docs: record Pilot-2 Turn 11`.
- Formal Pilot-2 session is `session-ea110b6c-7509-40e6-a786-99cf00d8fc66`.
- Formal Pilot-2 state before Turn 12 is `stateVersion=11`, `nextTurnNumber=12`, and `pendingContextCount=0`.
- Current scene before Turn 12 is `scene-qingli-herb-warehouse-perimeter`.
- Turn 11 was applied with result `result-pilot2-turn-011-ledger-wording-before-route`.
- Turn 11 evidence confirms the formal awning transition did not occur; current scene remained `scene-qingli-herb-warehouse-perimeter`.
- Turn 11 context showed `transition-perimeter-to-awning-witnessed` as `prerequisitesSatisfied=false` because player intent `go to awning with witness` was not present.
- The delegated Turn 12 input explicitly includes the required phrase `go to awning with witness`.
- The delegated Turn 12 input is Codex-authored and must not be described as a real human-input sample.
- Post-Turn 11 `pilot-preflight` passed with `valid=true`, `contextBytes=17448/24576`, and `contextHashValid=true`.

## Outcome

- `turn_012_player_input.txt` was saved as Codex-authored input.
- `explain-context-budget` passed with `candidateBytesAfterTrimming=21366/24576`.
- `prepare-turn` created `ctx-04485d23-0ab5-470d-96d2-2af2df48a2af` for Turn 12 at `baseStateVersion=11`.
- `audit-context` passed with `actualBytes=21366/24576` and `hashValid=true`.
- The Turn 12 context included `transition-perimeter-to-awning-witnessed` without `unmetPrerequisites`, so the exact `go to awning with witness` intent was recognized at the context/available-transition layer.
- First result `turn_012_result.json` included `transitionProposal` and passed `validate-result`, but `resolve-turn` rejected it with `player_refused_transition: Player input refuses transition; normal transition cannot be forced.`
- Retry result `turn_012_result_retry_01.json` removed `transitionProposal`, preserved the failed-transition evidence, and passed `validate-result`.
- `resolve-turn` with retry result passed, advancing the formal session to `stateVersion=12`, `nextTurnNumber=13`, with `pendingContexts=0`.
- No formal scene transition occurred; current scene remains `scene-qingli-herb-warehouse-perimeter`.
- `export-session-evidence` wrote `turn_012_evidence.json` with `turnLogs=12`, `stateDeltas=138`, and `pendingContexts=0`.
- `audit-narrative-quality` wrote `turn_012_audit_report.json` with `verdict=PASS`, `score=92`, `severity=WARNING`, `findings=1`.
- Post-Turn 12 `pilot-preflight` passed with `valid=true`, `stateVersion=12`, `nextTurnNumber=13`, `pendingContextCount=0`, `contextBytes=17645/24576`, and `contextHashValid=true`.

## Key Finding

Turn 12 split the transition behavior into two layers. The context / available-transition layer recognized the exact legal intent, but formal `resolve-turn` still treated the player input's broad refusal/boundary wording as `player_refused_transition`. This is useful Pilot-2 evidence and should be reviewed by 03 before any product or runtime decision. No runtime or Validator change was made.

## Implementation Steps

- [x] Step 1: Read control docs, Pilot-2 docs, Turn 11 evidence/context, and current repo/session state.
- [x] Step 2: Update `PLANS.md` for Codex-authored Turn 12 transition intent retry.
- [x] Step 3: Save `turn_012_player_input.txt`.
- [x] Step 4: Run `explain-context-budget`.
- [x] Step 5: Run `prepare-turn` and save `turn_012_context.json`.
- [x] Step 6: Run `audit-context` and confirm context is valid and `<=24576` bytes.
- [x] Step 7: Inspect Turn 12 available transitions and draft `turn_012_result.json`.
- [x] Step 8: Run `validate-result`.
- [x] Step 9: Run `resolve-turn` after validation passes; first transition result failed at resolve with `player_refused_transition`, then retry result resolved successfully without transition.
- [x] Step 10: Export `turn_012_evidence.json`.
- [x] Step 11: Run `audit-narrative-quality` and validate audit JSON as UTF-8.
- [x] Step 12: Confirm formal state advanced and pending contexts are cleared.
- [x] Step 13: Update `PLANS.md` with final Turn 12 evidence and audit results.
- [x] Step 14: Run `git diff --check`.
- [x] Step 15: Run `gradlew.bat clean test --no-daemon`.
- [x] Step 16: Decide whether installDist / CLI audit smoke is necessary and record rationale.
- [x] Step 17: Commit `docs: record Pilot-2 Turn 12`.
- [x] Step 18: Push to `origin/codex/mvp6-checkpoint-branching-spec`.

## Verification

- `show-session --db data/pilot2.db --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66`
- `pilot-preflight --db data/pilot2.db --project pilot2 --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66`
- `explain-context-budget --db data/pilot2.db --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66 --input-file out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_player_input.txt --out out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_context_budget.json`
- `prepare-turn --db data/pilot2.db --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66 --input-file out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_player_input.txt --out out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_context.json`
- `audit-context --file out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_context.json --out out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_context_audit.json`
- `validate-result --db data/pilot2.db --context out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_context.json --result out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_result.json` -> PASS.
- `resolve-turn --db data/pilot2.db --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66 --result out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_result.json` -> FAIL with `player_refused_transition`.
- `validate-result --db data/pilot2.db --context out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_context.json --result out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_result_retry_01.json` -> PASS.
- `resolve-turn --db data/pilot2.db --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66 --result out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_result_retry_01.json` -> PASS, `stateVersion=12`, `nextTurnNumber=13`.
- `export-session-evidence --db data/pilot2.db --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66 --out out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_evidence.json`
- `audit-narrative-quality --context out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_context.json --result out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_result_retry_01.json --evidence out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_evidence.json --out out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_audit_report.json` -> PASS, `score=92`, `severity=WARNING`, `findings=1`.
- Explicit UTF-8 JSON parse for `turn_012_audit_report.json` -> PASS.
- Post-turn `pilot-preflight --db data/pilot2.db --project pilot2 --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66 --out out/pilot2/pilot2-qingli-herb-warehouse/session_preflight_after_turn_012.json` -> PASS.
- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- `installDist` was not run because this milestone changed no build, packaging, Kotlin runtime, or distribution code; the relevant CLI paths were already exercised by `show-session`, `pilot-preflight`, `explain-context-budget`, `prepare-turn`, `audit-context`, `validate-result`, `resolve-turn`, `export-session-evidence`, and `audit-narrative-quality`.
- `git status`

## Risks

- The explicit transition intent may still not satisfy `prepare-turn`; if so, do not forge the transition.
- Boundary refusals may still trigger `player_refused_transition` during `resolve-turn` even if the prerequisite is satisfied.
- If a transition is valid, the result must avoid accidental warehouse entry, powder-line crossing, seal/door/goods touch, hidden truth reveal, or unconditional NPC compliance.
- This is Codex-authored input and therefore weaker than real human-input evidence.

## Drift Log

- 2026-06-21: Started Turn 12 legal transition intent retry after delegated 01主控 instructions and Turn 11 03驗收 PASS. This milestone explicitly labels the player input as Codex-authored and keeps Candidate B / runtime changes out of scope.
- 2026-06-21: Completed Turn 12 with retry. The exact transition intent was recognized in context, but formal transition was blocked during `resolve-turn` by `player_refused_transition`; retry result resolved without transition and advanced the session to Turn 13.
- 2026-06-21: Final checkpoint verification passed and this plan update is intended to be committed as `docs: record Pilot-2 Turn 12` and pushed to `origin/codex/mvp6-checkpoint-branching-spec`.
