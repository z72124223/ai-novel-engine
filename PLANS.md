# Plan

## Goal

Create a docs-only Pilot-2 Turn 13 Codex-authored player input options brief（Codex 代寫玩家輸入選項簡報） so 01 can later choose whether to wait for real human input（真人輸入） or explicitly adopt one non-human system-probe input（系統壓測輸入） for Turn 13.

## Global Strategy

Use existing Pilot-2 evidence through Turn 12, the Codex-authored input workflow, and the transition/refusal heuristic findings as the evidence base. Draft candidate inputs only as options. Do not begin Turn 13, do not create 04, and do not run lifecycle CLI（命令列介面） commands such as `prepare-turn`, `validate-result`, or `resolve-turn`.

## Non-Goals

- No new chat window / thread creation.
- No optional 04 player-input simulation window creation.
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
- No MVP-7 Candidate B.
- No MVP-8.
- No server, API, Docker, Android, Rust, embedding, vector DB, or unrelated technology.
- No `mvp-6` retagging.
- No committing ignored `data/` or `out/` artifacts.

## Scope

- `PLANS.md`
- `docs/PILOT2_TURN13_CODEX_INPUT_OPTIONS.md`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Current HEAD before this milestone is `af328fa docs: align Pilot-2 Codex-authored input plan status`.
- 03 read-only review for `af328fa` returned `PASS`, per 01.
- `mvp-6` remains unchanged at `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- `docs/PILOT2_CODEX_AUTHORED_INPUT_WORKFLOW.md` allows Codex-authored player input only when 01/user explicitly approves it, and requires it to be labeled as non-human evidence.
- `docs/PILOT2_EVIDENCE_CHECKPOINT_TURNS_1_12.md` records the formal post-Turn 12 state as `stateVersion=12`, `nextTurnNumber=13`, current scene `scene-qingli-herb-warehouse-perimeter`, and `pendingContexts=0`.
- Turn 12 was Codex-authored player input and cannot count as real human-player evidence.
- Turn 12 did not complete a formal scene transition.
- `docs/PILOT2_TRANSITION_REFUSAL_DECISION_BRIEF.md` and `docs/PILOT2_REFUSAL_HEURISTIC_INVESTIGATION_BRIEF.md` record the observed difference between context-layer transition intent recognition and resolve-layer `player_refused_transition`.
- The user request explicitly forbids starting Turn 13, creating 04, or running prepare/validate/resolve in this milestone.

## Assumptions

- No `ROADMAP.md` edit is required because this is an options brief, not a roadmap status change.
- The candidate inputs may include actionable text, but none of them becomes official Turn 13 input until 01/user explicitly chooses and labels one in a later milestone.
- Because this milestone only changes documentation, full Gradle tests are still required by project workflow, but no CLI E2E（端到端命令列驗證） is necessary unless the docs accidentally imply executable behavior.

## Open Questions

- Whether 01 wants Turn 13 to wait for real human input.
- Whether 01 wants to explicitly choose one Codex-authored option as non-human system-probe evidence.
- Whether 01 wants to create or designate a separate 04 input-simulation window later.

## Implementation Steps

- [x] Step 1: Read control docs and relevant Pilot-2 workflow/evidence/refusal docs.
- [x] Step 2: Run initial `git status` and confirm branch/head context.
- [x] Step 3: Update `PLANS.md` for this docs-only options brief milestone.
- [x] Step 4: Add `docs/PILOT2_TURN13_CODEX_INPUT_OPTIONS.md`.
- [x] Step 5: Inspect diff for scope drift and evidence accuracy.
- [x] Step 6: Run `git status`.
- [x] Step 7: Run `git diff --check`.
- [x] Step 8: Run `gradlew.bat clean test --no-daemon`.
- [x] Step 9: Commit `docs: draft Pilot-2 Turn 13 Codex input options`.
- [x] Step 10: Push to `origin/codex/mvp6-checkpoint-branching-spec`.
- [x] Step 11: Directly notify 03 read-only review thread with milestone summary and safety boundaries.

## Verification

- `git status` -> only `PLANS.md` and `docs/PILOT2_TURN13_CODEX_INPUT_OPTIONS.md` changed before commit.
- `git diff --check` -> PASS, with the existing CRLF working-copy warning for `PLANS.md`.
- `gradlew.bat clean test --no-daemon` -> PASS after clearing a transient lock left by the first timeout attempt.
- No `prepare-turn`, `validate-result`, or `resolve-turn` was run.
- No `data/` or `out/` artifacts were staged.

## Risks

- Accidentally treating candidate Codex-authored input as official Turn 13 input.
- Accidentally treating Codex-authored options as real human-player evidence.
- Creating a 04 workflow window or beginning Turn 13 without explicit 01/user approval.
- Recommending a runtime, Validator, StateReducer, schema, or MVP-7 Candidate B change from a docs-only milestone.
- Retaining ambiguity around Turn 12's `player_refused_transition` finding.

## Drift Log

- 2026-06-21: Started Pilot-2 Turn 13 Codex-authored input options brief after 01 reported 03 PASS on `af328fa`.
- 2026-06-21: Updated plan to docs-only scope; no Turn 13 execution, no 04 creation, and no lifecycle CLI commands are approved.
- 2026-06-21: Added `docs/PILOT2_TURN13_CODEX_INPUT_OPTIONS.md` with A/B/C candidate inputs, evidence classification, adoption instructions, and prohibitions.
- 2026-06-21: Verification passed with `git diff --check` and full Gradle tests; commit/push and direct 03 notification are recorded as part of final handoff.
