# Plan

## Goal

Create a docs-only Pilot-2 transition/refusal decision brief（決策簡報） after Turns 1-12 so 01 and the user can decide what to do about the observed gap between transition intent recognition（轉場意圖識別） and the `player_refused_transition` refusal heuristic（拒絕啟發式）. This milestone must not approve Candidate B, Validator（驗證器） changes, runtime（執行期） changes, schema（資料結構） changes, or StateReducer（狀態化約器） changes.

## Global Strategy

Use only existing docs and ignored `out/pilot2` artifacts（產物） as evidence（證據）. Summarize the Turn 12 finding, classify possible causes without changing code（程式碼）, compare next-step options, and recommend the smallest safe handoff for 01. Do not rerun any Pilot-2 turn or any turn lifecycle CLI（命令列介面） command such as `prepare-turn`, `validate-result`, or `resolve-turn`.

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
- `docs/PILOT2_TRANSITION_REFUSAL_DECISION_BRIEF.md`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Current HEAD before this milestone is `3dabae2 docs: checkpoint Pilot-2 evidence after Turn 12`.
- `docs/PILOT2_EVIDENCE_CHECKPOINT_TURNS_1_12.md` is already read-only reviewed by 03 with VERDICT=PASS, per 01.
- Turn 12 is Codex-authored player input（Codex 代寫玩家輸入） and must not count as real human-player evidence.
- Turn 12 context（上下文） has `contextId=ctx-04485d23-0ab5-470d-96d2-2af2df48a2af`, `baseStateVersion=11`, `contextHash=31198815d7c384a91de97c2369cc503075c25ed630a44cd4ce730cbe15848d39`, and `contextBudget.actualBytes=21366` of `24576`.
- Turn 12 context exposes `transition-perimeter-to-awning-witnessed` with required player intent `go to awning with witness` and no unmet prerequisites on that transition.
- First Turn 12 result `result-pilot2-turn-012-awning-transition-attempt` includes a transition proposal to `scene-qingli-herb-awning`; `validate-result` passed, but `resolve-turn` failed with `player_refused_transition`.
- Retry Turn 12 result `result-pilot2-turn-012-awning-transition-retry-no-transition` has no transition proposal; it validated and resolved, leaving the session at `stateVersion=12`, `nextTurnNumber=13`, `currentSceneId=scene-qingli-herb-warehouse-perimeter`, and `pendingContexts=0`.

## Assumptions

- No `ROADMAP.md` edit is required because this is a decision brief, not a roadmap status change.
- The current milestone should classify and recommend, not inspect or modify Validator/runtime implementation.

## Open Questions

- Whether 01 wants real human Turn 13 input before continuing Pilot-2.
- Whether 01 or the user wants a future read-only/tests-only investigation of the refusal heuristic.
- Whether 01 or the user later wants to explicitly authorize a runtime/Validator fix; this milestone does not authorize it.

## Implementation Steps

- [x] Step 1: Read control docs, Pilot-2 checkpoint docs, Turn 12 finding docs, and Turn 12 context/result/evidence artifacts.
- [x] Step 2: Update `PLANS.md` for this docs-only decision brief milestone.
- [x] Step 3: Add `docs/PILOT2_TRANSITION_REFUSAL_DECISION_BRIEF.md`.
- [x] Step 4: Inspect diff for scope drift and evidence accuracy.
- [x] Step 5: Run `git status`.
- [x] Step 6: Run `git diff --check`.
- [x] Step 7: Run `gradlew.bat clean test --no-daemon`.
- [x] Step 8: Commit `docs: draft Pilot-2 transition refusal decision brief`.
- [x] Step 9: Push to `origin/codex/mvp6-checkpoint-branching-spec`.

## Verification

- `git status` -> only `PLANS.md` and `docs/PILOT2_TRANSITION_REFUSAL_DECISION_BRIEF.md` changed before commit.
- `git diff --check` -> PASS, with the existing CRLF working-copy warning for `PLANS.md`.
- `gradlew.bat clean test --no-daemon` -> PASS after stopping a stale Gradle daemon left by an earlier command timeout.

## Risks

- Overstating Turn 12 as real human evidence.
- Treating the decision brief as approval to modify Validator, runtime, StateReducer, schema, or Candidate B.
- Committing ignored `out/` artifacts by mistake.
- Calling `player_refused_transition` a confirmed bug without a separately approved implementation investigation.
- Accidentally rerunning Pilot-2 lifecycle commands while only a document brief is approved.

## Drift Log

- 2026-06-21: Started Pilot-2 transition/refusal heuristic decision brief after 01 reported 03 read-only PASS on the Turns 1-12 evidence checkpoint.
- 2026-06-21: Added `docs/PILOT2_TRANSITION_REFUSAL_DECISION_BRIEF.md`; scope remains docs-only and no Pilot lifecycle commands were run.
- 2026-06-21: Verification passed with `git diff --check` and full Gradle tests.
- 2026-06-21: Prepared commit/push handoff for `docs: draft Pilot-2 transition refusal decision brief`.
