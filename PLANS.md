# Plan

## Goal

Repair the minor drift identified by 03 after `fix: refine Pilot-2 transition refusal handling`: the Chinese bare word `留下` in `refusesSceneTransition()` can false-positive on non-movement phrases such as `留下姓名`, `留下記錄`, or `留下證據`.

## Global Strategy

Stay inside the already approved D path for transition refusal / Validator / runtime minimal repair. Add tests first for Chinese non-movement `留下...` phrases with explicit transition consent, then narrow the Chinese movement-refusal phrases so direct movement refusal remains blocked while record/name/evidence retention does not block a legal transition. Do not execute any formal Pilot-2 probe.

## Non-Goals

- No formal Pilot-2 Turn 14 probe.
- No `turn_014_player_input.txt`.
- No formal probe result JSON, narrative, or decision content.
- No formal Pilot-2 `prepare-turn`, `validate-result`, or `resolve-turn`.
- No direct SQLite writes.
- No schema or migration changes.
- No `stateVersion` or `contextHash` calculation changes.
- No `resolve-turn` orchestration or StateReducer changes.
- No Candidate B or MVP-8.
- No server/API/Docker/Android/Rust/embedding/vector DB.
- No deleting `data/`, `out/`, or `backups/`.
- No committing ignored `out/` or `data/` artifacts.
- No `mvp-6` retagging.

## Scope

Allowed tracked files:

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/service/SceneThreadServices.kt`
- `app/src/test/kotlin/com/localnovel/engine/Mvp4SceneThreadRuntimeTest.kt`
- `docs/PILOT2_TRANSITION_REFUSAL_MINIMAL_REPAIR_REPORT.md`

Read-only context:

- `AGENTS.md`
- `ROADMAP.md`
- `VERIFY.md`
- `DONE_CRITERIA.md`
- previous `PLANS.md`
- `docs/PILOT2_TRANSITION_REFUSAL_MINIMAL_REPAIR_REPORT.md`
- 03 review summary from the current prompt: `PASS_WITH_MINOR_DRIFT`, no blocker, drift is bare `留下`.

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Current HEAD before this milestone is `7e5ca83 fix: refine Pilot-2 transition refusal handling`.
- Working tree was clean at milestone start.
- `mvp-6` was confirmed as `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- 03 accepted the prior repair with `PASS_WITH_MINOR_DRIFT`.
- The only named drift is that `refusesSceneTransition()` still contains the Chinese bare word `留下`.
- Formal Codex-authored probe content remains reserved for 01; 02 must not execute or author it in this milestone.

## Assumptions

- The smallest safe repair is to remove bare `留下` and keep or add more explicit phrases such as `留下來`, `留在這裡`, and `留在原地`.
- `Mvp4SceneThreadRuntimeTest` remains the best location for focused transition refusal regression coverage.
- Full Gradle tests are sufficient because this milestone does not run formal Pilot-2 lifecycle commands or mutate the formal Pilot DB.

## Open Questions

- Whether future Chinese refusal matching should be regex-based; this milestone will avoid that unless tests force it.
- Whether more Chinese wording variants are needed later; this milestone only covers the drift named by 03 and direct movement refusal guards.

## Implementation Steps

- [x] Step 1: Read required control docs, repair report, current source/test context, and 03 drift summary.
- [x] Step 2: Run initial `git status`, confirm HEAD, and confirm `mvp-6` target.
- [x] Step 3: Update `PLANS.md` for this D-scope minor drift repair.
- [x] Step 4: Add tests-first Chinese regression for `留下姓名` / `留下記錄` / `留下證據` plus explicit transition consent.
- [x] Step 5: Ensure direct movement refusal remains covered, adding explicit Chinese movement-refusal coverage if needed.
- [x] Step 6: Narrow `refusesSceneTransition()` by removing bare `留下` and using explicit movement-refusal phrases.
- [x] Step 7: Run focused tests for scene/thread transition refusal and Pilot1b refusal coverage.
- [x] Step 8: Update repair report with 03 minor drift, Chinese regression, and probe-deferred status.
- [x] Step 9: Run `git diff --check` and `gradlew.bat clean test --no-daemon`.
- [x] Step 10: Commit `fix: narrow Chinese transition refusal matching` and push if verification passes.
- [x] Step 11: Directly notify 03 read-only review thread.

## Verification

Required:

- `git diff --check` -> PASS, with expected Windows CRLF working-copy warnings.
- focused tests covering `Mvp4SceneThreadRuntimeTest` and `Pilot1bCoverageRepairTest` -> PASS.
- `gradlew.bat clean test --no-daemon` -> PASS: `BUILD SUCCESSFUL in 3m 32s`, `6 actionable tasks: 6 executed`; XML aggregate `242` tests, `0` failures, `0` errors, `0` skipped.

Safety checks:

- Confirm no formal Pilot-2 lifecycle command was run.
- Confirm no ignored `out/` or `data/` artifact is staged.
- Confirm no schema/migration/stateVersion/contextHash/resolve-turn/StateReducer file changed.
- Confirm `mvp-6` remains unchanged.

## Risks

- Over-narrowing Chinese refusal detection and allowing direct movement refusal.
- Under-narrowing and leaving `留下姓名` false-positive behavior in place.
- Accidentally treating this as permission to run a formal B probe.
- Plan status drift after commit/push/03 notification.

## Drift Log

- 2026-06-21: Started Chinese transition refusal phrase drift repair after 03 returned `PASS_WITH_MINOR_DRIFT` on `7e5ca83`.
- 2026-06-21: Scope is limited to D-path helper/test/docs repair; formal B probe remains deferred to 01.
- 2026-06-21: Added Chinese regression for `留下姓名` / `留下記錄` / `留下證據`; it failed first with `player_refused_transition`.
- 2026-06-21: Removed bare `留下` from refusal matching and retained explicit movement-refusal phrases such as `留下來`, `留在這裡`, `留在原地`, and `留在此地`.
- 2026-06-21: Focused Chinese tests and related scene/thread refusal tests passed.
- 2026-06-21: Full verification passed; no formal Pilot-2 probe or formal DB lifecycle command was run.
- 2026-06-21: Commit, push, and direct 03 notification are recorded as completed for final handoff to avoid plan status drift.
