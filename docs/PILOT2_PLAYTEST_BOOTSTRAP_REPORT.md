# Pilot-2 Playtest Bootstrap Report

## VERDICT

PASS for bootstrap only. A separate Pilot-2 playtest DB and fresh session were created from the existing Qingli herb warehouse setting pack, then stopped before Turn 1. No `prepare-turn`, result, narrative, `validate-result`, or `resolve-turn` was run.

## Scope And Provenance

This is a playtest new-game bootstrap, not a continuation of the formal `data/pilot2.db` engineering line.

User intent was clarified by 01: the old world base is acceptable for the playtest. Therefore `pack-pilot2-qingli-herb-warehouse@1` is the formal playtest world base for this checkpoint.

The mountain-hiking, dense-fog, and descent-into-a-different-scene idea is only an 01/00/04 opening prologue candidate. It was not written into the formal setting pack or into SQLite in this milestone.

The expanded Ai-chan details, including long-haired husky, sharp senses, and attraction to special scent / perfume trails, are user-confirmed character-card candidates only. They were not applied as canon because no approved setting-pack patch was part of this milestone.

## Inputs

- DB path: `data/pilot2_playtest.db`
- Setting pack source: `out/pilot2/bootstrap/pilot2_setting_pack.json`
- Setting pack: `pack-pilot2-qingli-herb-warehouse@1`
- Canonical hash: `cc2297f834c503c7290558385f86b6dcfb2a6e56b37ffd7d62f589b94547b1cd`
- Opening scene: `scene-qingli-herb-warehouse-perimeter`

Preflight safety check confirmed `data/pilot2_playtest.db*` did not exist before creation. The existing formal DB `data/pilot2.db` was not modified, reset, deleted, or used for this bootstrap.

## CLI Results

All formal writes used existing CLI commands.

- `init-db --db data/pilot2_playtest.db`: initialized SQLite database.
- `validate-setting-pack --db data/pilot2_playtest.db --file out/pilot2/bootstrap/pilot2_setting_pack.json --out out/pilot2_playtest/bootstrap/02_validate_setting_pack.json`: `valid=true`, `issueCount=0`.
- `preview-setting-import --db data/pilot2_playtest.db --file out/pilot2/bootstrap/pilot2_setting_pack.json --out out/pilot2_playtest/bootstrap/03_preview_setting_import.json`: `valid=true`, `issueCount=0`, `diffEntries=34`, `impactPaths=0`.
- `apply-setting-pack --db data/pilot2_playtest.db --file out/pilot2/bootstrap/pilot2_setting_pack.json --out out/pilot2_playtest/bootstrap/04_apply_setting_pack.json`: `applied=true`, importId `setting-import-651248f1-5991-4421-846e-4a389285f088`, `diffEntries=34`, `impactPaths=0`.
- `start-session --db data/pilot2_playtest.db --project pilot2 --scene scene-qingli-herb-warehouse-perimeter`: created the fresh playtest session.
- `show-session --db data/pilot2_playtest.db --session session-3aad7bfe-fed8-421b-868d-0cf033c8339c`: confirmed initial session state.
- `pilot-preflight --db data/pilot2_playtest.db --project pilot2 --session session-3aad7bfe-fed8-421b-868d-0cf033c8339c --out out/pilot2_playtest/qingli-herb-warehouse/session_preflight.json`: `valid=true`.
- `export-session-evidence --db data/pilot2_playtest.db --session session-3aad7bfe-fed8-421b-868d-0cf033c8339c --out out/pilot2_playtest/qingli-herb-warehouse/session_initial_evidence.json`: wrote initial evidence with zero turns.

## New Session

- sessionId: `session-3aad7bfe-fed8-421b-868d-0cf033c8339c`
- branchId: `branch-103aab87-d3ad-43ed-9205-9ce0a84fc92b`
- currentSceneId: `scene-qingli-herb-warehouse-perimeter`
- currentSceneInstanceId: `scene-instance-6fcaef39-18ae-4eb1-a2f8-691044439000`
- nextTurnNumber: `1`
- stateVersion: `0`
- pendingContexts: `0`
- activeSceneInstanceCount: `1`
- turnLogs: `0`
- stateDeltas: `0`

## Preflight And Evidence

`pilot-preflight` result:

- valid: `true`
- session status: active
- project matches: `pilot2`
- canon valid: `true`
- single active scene instance: `true`
- opening characters have runtime states: `true`
- opening character mind ready: `true`
- pending context conflict: none
- hidden secret leak count: `0`
- dry-run context: `true`
- context bytes: `16218 / 24576`
- context hash: `d75451fd2d6808e95795bf616649117f80520b3894ce51d7adb7967942970ea1`
- context hash valid: `true`
- semantic coverage: `PASS`

`export-session-evidence` result:

- turnLogs: `0`
- stateDeltas: `0`
- pendingContexts: `0`

## Artifacts

Ignored artifacts retained for 03 read-only verification:

- `data/pilot2_playtest.db`
- `out/pilot2_playtest/bootstrap/01_init_db_output.txt`
- `out/pilot2_playtest/bootstrap/02_validate_setting_pack.json`
- `out/pilot2_playtest/bootstrap/03_preview_setting_import.json`
- `out/pilot2_playtest/bootstrap/04_apply_setting_pack.json`
- `out/pilot2_playtest/qingli-herb-warehouse/start_session_output.txt`
- `out/pilot2_playtest/qingli-herb-warehouse/session_show_after_start.txt`
- `out/pilot2_playtest/qingli-herb-warehouse/session_preflight.json`
- `out/pilot2_playtest/qingli-herb-warehouse/session_initial_evidence.json`

Tracked files for this milestone:

- `PLANS.md`
- `docs/PILOT2_PLAYTEST_BOOTSTRAP_REPORT.md`

## Safety Boundaries

Confirmed:

- Did not start Turn 1.
- Did not run `prepare-turn`.
- Did not create a formal turn context.
- Did not create result JSON or narrative.
- Did not run `validate-result`.
- Did not run `resolve-turn`.
- Did not directly write SQLite.
- Did not modify `data/pilot2.db`.
- Did not modify Kotlin, tests, schema, migrations, Validator, StateReducer, `stateVersion`, `contextHash`, or resolve orchestration.
- Did not retag `mvp-6`.
- Did not commit ignored `data/` or `out/` artifacts.

## Verification

- `git diff --check`: PASS.
- `gradlew.bat clean test --no-daemon`: PASS after clearing one stale Java process left by an earlier timeout. The failed intermediate run stopped at Gradle `clean` because test output files were locked; no test assertion failure was observed. The final rerun completed with `BUILD SUCCESSFUL`.
- `mvp-6`: `be527031335efe8e2b0facae0145e1bbc084f8c6`.

## Handoff To 04

04 / Playtest UI can use the fresh playtest DB and session once 01/03 accept this bootstrap:

- DB: `data/pilot2_playtest.db`
- sessionId: `session-3aad7bfe-fed8-421b-868d-0cf033c8339c`
- first playable turn: Turn 1

04 should provide or collect Turn 1 player input for this playtest line. Any hiking / fog prologue or expanded Ai-chan sensory detail should remain an opening/player-input or character-card candidate until 01 explicitly approves a formal setting-pack patch.

## 03 Review Checklist

- Confirm tracked changes are limited to `PLANS.md` and this report.
- Confirm ignored artifacts exist and are not staged.
- Confirm `data/pilot2.db` was not modified.
- Confirm `data/pilot2_playtest.db` contains the fresh session above.
- Confirm setting pack validation/preview/apply sidecars match the reported canonical hash and import result.
- Confirm preflight is valid, context bytes are under `24576`, and `pendingContexts=0`.
- Confirm no Turn 1 lifecycle command was run.
- Confirm `mvp-6` still points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
