# Plan

## Goal

Create a docs-only Pilot-2 refusal heuristic investigation brief（拒絕啟發式調查簡報） so 01 and the user can understand why Turn 12 produced `validate-result` PASS but `resolve-turn` FAIL with `player_refused_transition`. This milestone is read-only investigation（只讀調查） plus documentation; it must not approve or perform Candidate B, Validator（驗證器）, StateReducer（狀態化約器）, `resolve-turn`, runtime（執行期）, schema（資料結構）, migration（資料庫遷移）, or test changes.

## Global Strategy

Use existing docs, Turn 12 artifacts（產物）, and read-only source/test inspection to document observable behavior. Separate confirmed facts from 推測. Produce one focused brief under `docs/` and update `PLANS.md`. Do not rerun Pilot-2 turns or lifecycle CLI（命令列介面） commands such as `prepare-turn`, `validate-result`, or `resolve-turn`.

## Non-Goals

- No Pilot-2 turn reruns.
- No `prepare-turn`, `validate-result`, or `resolve-turn`.
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
- No committing ignored `data/` or `out/` artifacts.

## Scope

- `PLANS.md`
- `docs/PILOT2_REFUSAL_HEURISTIC_INVESTIGATION_BRIEF.md`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Current HEAD before this milestone is `626a35d docs: draft Pilot-2 transition refusal decision brief`.
- 01 reports 03 read-only review passed for `docs: draft Pilot-2 transition refusal decision brief`.
- Investigation milestone commit `52e3218 docs: investigate Pilot-2 refusal heuristic` has been pushed to `origin/codex/mvp6-checkpoint-branching-spec`.
- 03 read-only review for `52e3218` returned `PASS_WITH_MINOR_PLAN_DRIFT`; the only reported drift is that Step 8 commit and Step 9 push were left unchecked in this file after the commit/push had completed.
- `mvp-6` remains unchanged at `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Turn 12 is Codex-authored player input（Codex 代寫玩家輸入） and must not count as real human-player evidence.
- Turn 12 context（上下文） has `contextId=ctx-04485d23-0ab5-470d-96d2-2af2df48a2af`, `baseStateVersion=11`, `contextHash=31198815d7c384a91de97c2369cc503075c25ed630a44cd4ce730cbe15848d39`, and `contextBudget.actualBytes=21366` of `24576`.
- Turn 12 target transition `transition-perimeter-to-awning-witnessed` required `go to awning with witness` and had no unmet prerequisites in the context artifact.
- Turn 12 first result included `transitionProposal`; prior recorded outcome says `validate-result` passed but `resolve-turn` failed with `player_refused_transition`.
- Turn 12 retry result had no `transitionProposal`, resolved successfully, and final evidence shows `stateVersion=12`, `nextTurnNumber=13`, `currentSceneId=scene-qingli-herb-warehouse-perimeter`, `pendingContexts=0`.
- `SqliteStoryRepository` builds available transitions by checking whether `playerInput.contains(required_player_intent, ignoreCase = true)`.
- `TransitionValidator` checks `context.playerInput` for refusal keywords including `拒絕`, `不走`, `留下`, `不離開`, `不要走`, `stay`, and `refuse` when a non-`WORLD_FORCED` transition proposal exists.
- `validate-result` CLI uses `AiTurnResultValidator`, while `resolve-turn` uses `MultiTurnRuntime.resolveTurn`, which calls `MultiTurnRuntime.validate` and includes `TransitionValidator`.

## Assumptions

- No `ROADMAP.md` edit is required because this is a narrow investigation brief, not a roadmap status change.
- The Turn 12 `player_refused_transition` was likely triggered by broad player-input refusal wording or the standard section heading `明確拒絕 / 邊界`, but this remains 推測 unless reproduced in an approved tests-only milestone.

## Open Questions

- Whether future `validate-result` should intentionally remain a legacy/basic result validator or be clarified/documented against `resolve-turn` validation.
- Whether the refusal heuristic should distinguish target-transition refusal from boundary refusals such as "do not enter the sealed warehouse".
- Whether the Pilot-2 player input template should avoid the exact word `拒絕` in transition-attempt turns, or whether runtime should become smarter. This milestone does not decide.

## Implementation Steps

- [x] Step 1: Read control docs, Pilot-2 decision/checkpoint/finding docs, Turn 12 artifacts, and source/test observations from `rg`.
- [x] Step 2: Update `PLANS.md` for this docs-only read-only investigation milestone.
- [x] Step 3: Add `docs/PILOT2_REFUSAL_HEURISTIC_INVESTIGATION_BRIEF.md`.
- [x] Step 4: Inspect diff for scope drift and evidence accuracy.
- [x] Step 5: Run `git status`.
- [x] Step 6: Run `git diff --check`.
- [x] Step 7: Run `gradlew.bat clean test --no-daemon`.
- [x] Step 8: Commit `docs: investigate Pilot-2 refusal heuristic`.
- [x] Step 9: Push to `origin/codex/mvp6-checkpoint-branching-spec`.

## Verification

- `git status` -> only `PLANS.md` and `docs/PILOT2_REFUSAL_HEURISTIC_INVESTIGATION_BRIEF.md` changed before commit.
- `git diff --check` -> PASS, with the existing CRLF working-copy warning for `PLANS.md`.
- `gradlew.bat clean test --no-daemon` -> PASS.

## Risks

- Overstating Turn 12 as real human evidence.
- Presenting source observations as a completed fix.
- Treating this investigation as approval to modify Validator, runtime, StateReducer, schema, or Candidate B.
- Committing ignored `out/` artifacts by mistake.
- Accidentally rerunning Pilot-2 lifecycle commands while only read-only investigation is approved.

## Drift Log

- 2026-06-21: Started Pilot-2 refusal heuristic investigation brief after 01 reported 03 read-only PASS on the transition refusal decision brief.
- 2026-06-21: Added `docs/PILOT2_REFUSAL_HEURISTIC_INVESTIGATION_BRIEF.md`; scope remains docs-only and no Pilot lifecycle commands were run.
- 2026-06-21: Verification passed with `git diff --check` and full Gradle tests.
- 2026-06-21: 03 returned `PASS_WITH_MINOR_PLAN_DRIFT` for `52e3218`; this status-alignment milestone marks Step 8/9 complete and records that `52e3218` was pushed without moving `mvp-6`.
