# Plan

## Goal

Create a docs-only Pilot-2 Codex-authored player input workflow（Codex 代寫玩家輸入流程） alignment document so future Pilot-2 operator-authored inputs are clearly labeled, separated from real human-player evidence（真人玩家證據）, and assigned to clean 01/02/03/optional 04 roles without context pollution（上下文污染）.

## Global Strategy

Use existing Pilot-2 scenario, template, evidence checkpoint, and Turn 12 transition/refusal findings as the evidence base. Add one focused workflow document under `docs/` and update `PLANS.md`. Do not create a new thread（聊天窗口）, do not begin Turn 13, and do not run lifecycle CLI（命令列介面） commands such as `prepare-turn`, `validate-result`, or `resolve-turn`.

## Non-Goals

- No new chat window / thread creation.
- No Pilot-2 Turn 13 start.
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
- `docs/PILOT2_CODEX_AUTHORED_INPUT_WORKFLOW.md`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Current HEAD before this milestone is `7222ef2 docs: align Pilot-2 refusal heuristic plan status`.
- 03 read-only review for `7222ef2` returned `PASS`, per 01.
- Codex-authored workflow milestone commit `35e45e9 docs: define Pilot-2 Codex-authored input workflow` has been pushed to `origin/codex/mvp6-checkpoint-branching-spec`.
- 03 read-only review for `35e45e9` returned `PASS_WITH_MINOR_PLAN_DRIFT`; the only reported drift is that Step 8 commit and Step 9 push were left unchecked in this file after the commit/push had completed.
- `mvp-6` remains unchanged at `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- `docs/PILOT2_SCENARIO_DESIGN.md` defines Pilot-2 as evidence collection and requires real human-input turns for human evidence success.
- `docs/PILOT2_EVIDENCE_CHECKPOINT_TURNS_1_12.md` records Turn 12 as Codex-authored player input and excludes it from human-player evidence.
- `docs/PILOT2_TRANSITION_REFUSAL_DECISION_BRIEF.md` lists Codex-authored Turn 13 as an optional system probe only, explicitly labeled non-human evidence.
- `docs/PILOT2_REFUSAL_HEURISTIC_INVESTIGATION_BRIEF.md` recommends waiting for real Turn 13, or explicitly approving a Codex-authored Turn 13 as non-human evidence.
- The user request explicitly forbids creating a new thread and starting Turn 13 in this milestone.

## Assumptions

- No `ROADMAP.md` edit is required because this is an operational workflow note, not a roadmap status change.
- A future 04 player-input simulation window is optional and requires explicit 01/user approval before creation or assignment.

## Open Questions

- Whether 01 wants to create or designate a 04 player-input simulation window later.
- Whether Turn 13 should wait for real human input or use a clearly labeled Codex-authored input as system-probe evidence only.

## Implementation Steps

- [x] Step 1: Read control docs and relevant Pilot-2 evidence/workflow/finding docs.
- [x] Step 2: Update `PLANS.md` for this docs-only workflow alignment milestone.
- [x] Step 3: Add `docs/PILOT2_CODEX_AUTHORED_INPUT_WORKFLOW.md`.
- [x] Step 4: Inspect diff for scope drift and evidence accuracy.
- [x] Step 5: Run `git status`.
- [x] Step 6: Run `git diff --check`.
- [x] Step 7: Run `gradlew.bat clean test --no-daemon`.
- [x] Step 8: Commit `docs: define Pilot-2 Codex-authored input workflow`.
- [x] Step 9: Push to `origin/codex/mvp6-checkpoint-branching-spec`.

## Verification

- `git status` -> only `PLANS.md` and `docs/PILOT2_CODEX_AUTHORED_INPUT_WORKFLOW.md` changed before commit.
- `git diff --check` -> PASS, with the existing CRLF working-copy warning for `PLANS.md`.
- `gradlew.bat clean test --no-daemon` -> PASS.

## Risks

- Accidentally treating Codex-authored player input as real human-player evidence.
- Blurring 02 execution duties with player-input authorship or 03 acceptance review.
- Accidentally creating a new thread or starting Turn 13 while only documentation is approved.
- Treating this workflow note as approval for Candidate B, Validator, runtime, StateReducer, schema, or resolve changes.

## Drift Log

- 2026-06-21: Started Pilot-2 Codex-authored input workflow alignment after 01 reported 03 PASS on the refusal heuristic plan status alignment.
- 2026-06-21: Added `docs/PILOT2_CODEX_AUTHORED_INPUT_WORKFLOW.md`; scope remains docs-only, no thread was created, and Turn 13 was not started.
- 2026-06-21: Verification passed with `git diff --check` and full Gradle tests.
- 2026-06-21: 03 returned `PASS_WITH_MINOR_PLAN_DRIFT` for `35e45e9`; this status-alignment milestone marks Step 8/9 complete and records that `35e45e9` was pushed without moving `mvp-6`.
