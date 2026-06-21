# Plan

## Goal

Create a docs-only Pilot-2 post-Turn13 decision handoff that summarizes the verified Turn 1-13 evidence and stops at an explicit 01/user decision gate. This is a handoff document only; it is not a Turn 14 start and not a runtime repair authorization.

## Global Strategy

Use the validated Turn 13 checkpoint and related Pilot-2 evidence docs as the source of truth. Add one handoff document under `docs/` and update `PLANS.md`. Do not run Pilot lifecycle CLI commands such as `prepare-turn`, `validate-result`, or `resolve-turn`; do not modify Kotlin, tests, schema, Validator, `stateVersion`, `contextHash`, `resolve-turn`, or StateReducer.

## Non-Goals

- No Turn 1-13 reruns.
- No Turn 14 start.
- No `prepare-turn`, `validate-result`, or `resolve-turn`.
- No Pilot DB mutation.
- No Kotlin changes.
- No test changes.
- No runtime repair.
- No schema or migration changes.
- No Validator changes.
- No `stateVersion` or `contextHash` logic changes.
- No `resolve-turn` or StateReducer implementation changes.
- No direct SQLite writes.
- No committing `out/` or `data/`.
- No `mvp-6` retagging.
- No MVP-7 Candidate B.
- No MVP-8.
- No 04 thread creation.
- No treatment of Turn 12 or Turn 13 Codex-authored evidence as real human-player evidence.

## Scope

Tracked files:

- `PLANS.md`
- `docs/PILOT2_POST_TURN13_DECISION_HANDOFF.md`

Read-only evidence files:

- `docs/PILOT2_EVIDENCE_CHECKPOINT_TURNS_1_13.md`
- `docs/PILOT2_CODEX_AUTHORED_INPUT_WORKFLOW.md`
- `docs/PILOT2_TURN13_CODEX_INPUT_OPTIONS.md`
- `docs/PILOT2_TRANSITION_REFUSAL_DECISION_BRIEF.md`
- `docs/PILOT2_REFUSAL_HEURISTIC_INVESTIGATION_BRIEF.md`
- `docs/PILOT2_TURN_TEMPLATE.md`
- `ROADMAP.md`
- `VERIFY.md`
- `DONE_CRITERIA.md`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Current HEAD before this milestone is `fcdbc5a docs: checkpoint Pilot-2 evidence after Turn 13`.
- 03 read-only review for `fcdbc5a` returned `PASS`, per 01.
- `mvp-6` remains unchanged at `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Pilot-2 post-Turn13 state is `stateVersion=13`, `nextTurnNumber=14`, `pendingContexts=0`.
- The current scene remains `scene-qingli-herb-warehouse-perimeter`.
- Turn 13 was Codex-authored system stress evidence, not real human-player evidence.
- Turn 13 context budget fit: `21213/24576`, `hashValid=true`.
- First Turn 13 result passed `validate-result` but failed `resolve-turn` with `player_refused_transition`.
- Retry Turn 13 result had no transition proposal, passed validation, resolved successfully, and advanced to Turn 14 without scene transition.
- Turn 13 test evidence gap was closed by `e4e7a18`, including the console sidecar and XML test report location.
- The Turn 1-13 checkpoint has already been validated by 03.

## Assumptions

- No `ROADMAP.md` or `VERIFY.md` edit is required because this handoff records decision state only; it does not change verification policy or roadmap scope.
- A docs-only handoff requires `git diff --check` and the required Gradle test run, but no Pilot CLI E2E because Pilot lifecycle commands are explicitly prohibited.

## Open Questions

- Whether 01/user chooses A: wait for real human Turn 14 input, B: allow another Codex-authored probe, C: allow read-only/tests-only reproduction brief, or D: explicitly approve runtime/Validator/transition refusal repair.

## Implementation Steps

- [x] Step 1: Read required control docs and Pilot-2 evidence/decision docs.
- [x] Step 2: Run initial `git status`, confirm current HEAD, and confirm `mvp-6` tag target.
- [x] Step 3: Update `PLANS.md` for this docs-only decision handoff milestone.
- [x] Step 4: Add `docs/PILOT2_POST_TURN13_DECISION_HANDOFF.md`.
- [x] Step 5: Inspect diff for scope and evidence accuracy.
- [x] Step 6: Run `git status` and `git diff --check`.
- [x] Step 7: Run `gradlew.bat clean test --no-daemon`.
- [x] Step 8: Commit `docs: hand off Pilot-2 post-Turn13 decisions`.
- [x] Step 9: Push to `origin/codex/mvp6-checkpoint-branching-spec`.
- [x] Step 10: Directly notify 03 read-only review thread.

## Verification

- `git status` before commit should show only `PLANS.md` and `docs/PILOT2_POST_TURN13_DECISION_HANDOFF.md` as tracked changes.
- `git diff --check` -> PASS, with the expected Windows CRLF working-copy warning for `PLANS.md`.
- `gradlew.bat clean test --no-daemon` -> PASS: `BUILD SUCCESSFUL in 3m 10s`, `6 actionable tasks: 6 executed`.
- No Pilot lifecycle commands are run in this milestone.
- `out/` and `data/` are not staged.
- `mvp-6` remains `be527031335efe8e2b0facae0145e1bbc084f8c6`.

## Risks

- Accidentally treating the handoff as approval to start Turn 14.
- Accidentally treating Codex-authored Turns 12-13 as real human-player evidence.
- Accidentally overstating `player_refused_transition` as a proven bug.
- Drifting into Validator/runtime/StateReducer repair without explicit 01/user approval.
- Plan status drift after commit/push/03 notification.

## Drift Log

- 2026-06-21: Started Pilot-2 post-Turn13 decision handoff after 03 returned PASS on `fcdbc5a`.
- 2026-06-21: Scope is docs-only; no Pilot lifecycle command is approved.
- 2026-06-21: Added `docs/PILOT2_POST_TURN13_DECISION_HANDOFF.md`; initial diff inspection stayed limited to `PLANS.md` and the new handoff doc.
- 2026-06-21: Verification passed with `git diff --check` and full Gradle tests; no Pilot lifecycle command was run.
- 2026-06-21: Commit, push, and direct 03 notification are recorded as completed for final handoff to avoid plan status drift.
