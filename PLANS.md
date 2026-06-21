# Plan

## Goal

Bootstrap an independent Pilot-2 playtest new-game session using the same setting pack, without continuing or modifying the formal `data/pilot2.db` Turn-15 engineering line. Create `data/pilot2_playtest.db`, import `out/pilot2/bootstrap/pilot2_setting_pack.json`, start a fresh session at `scene-qingli-herb-warehouse-perimeter`, and stop before Turn 1.

## Global Strategy

Use only existing CLI commands for all formal writes. First confirm that no `data/pilot2_playtest.db*` files exist. Then initialize the new DB, validate/preview/apply the existing setting pack, start a new session, run read-only confirmation commands, write a tracked bootstrap report, run verification, commit/push tracked docs only, and notify 03. Do not run `prepare-turn`, create context/result/narrative, or resolve any turn.

## Non-Goals

- No modification, deletion, reset, or overwrite of `data/pilot2.db`.
- No deletion of any `data/`, `out/`, or backup files.
- No overwrite of existing `data/pilot2_playtest.db*`; if present, stop.
- No `prepare-turn`.
- No context, result JSON, narrative, `validate-result`, or `resolve-turn`.
- No direct SQLite writes.
- No Kotlin/runtime/test/schema/migration/Validator changes.
- No `stateVersion`, `contextHash`, `resolve-turn`, or StateReducer changes.
- No `mvp-6` retagging.
- No MVP-8 or unapproved Candidate B follow-up scope.
- No server/API/Docker/Android/Rust/embedding/vector DB.
- No committing ignored `data/` or `out/` artifacts.

## Scope

Tracked files allowed:

- `PLANS.md`
- `docs/PILOT2_PLAYTEST_BOOTSTRAP_REPORT.md`

Ignored artifacts to create but not commit:

- `data/pilot2_playtest.db`
- `out/pilot2_playtest/bootstrap/02_validate_setting_pack.json`
- `out/pilot2_playtest/bootstrap/03_preview_setting_import.json`
- `out/pilot2_playtest/bootstrap/04_apply_setting_pack.json`
- `out/pilot2_playtest/qingli-herb-warehouse/session_show_after_start.txt`
- `out/pilot2_playtest/qingli-herb-warehouse/session_preflight.json`
- `out/pilot2_playtest/qingli-herb-warehouse/session_initial_evidence.json`

Setting pack source:

- `out/pilot2/bootstrap/pilot2_setting_pack.json`

Opening scene:

- `scene-qingli-herb-warehouse-perimeter`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Current HEAD before this milestone is `c39c660 docs: resolve Pilot-2 Turn 14 Codex probe`.
- Working tree was clean at milestone start.
- `mvp-6` was confirmed as `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- `out/pilot2/bootstrap/pilot2_setting_pack.json` exists.
- `data/pilot2_playtest.db*` did not exist in the initial preflight check.
- `data/pilot2_playtest.db` and `out/pilot2_playtest/**` are ignored by `.gitignore`.
- 2026-06-21 pause/resume note: 01 temporarily paused this milestone while checking the user's "original otherworld setting" intent. 01 then confirmed the user accepted the old world base; `pack-pilot2-qingli-herb-warehouse@1` remains the formal playtest world base.
- Mountain fog / hiking descent is only an 01/00/04 opening prologue candidate, not written to the formal setting pack or DB in this milestone.
- New Ai-chan details, including long-haired husky, sharp senses, and special scent/perfume tracking, are user-confirmed character-card candidates only; they are not canon until a future approved setting-pack patch.

## Assumptions

- The setting pack is still valid for a fresh DB because it was used for the existing Pilot-2 bootstrap.
- `pilot-preflight` and `export-session-evidence` should support the new session after `start-session`.
- A fresh session should start at `nextTurnNumber=1`, `stateVersion=0`, and no pending contexts.

## Open Questions

- New `sessionId` and `branchId` will be known after `start-session`.
- Whether `pilot-preflight` emits clean PASS signals for the new playtest DB.

## Implementation Steps

- [x] Step 1: Read AGENTS, current PLANS, Pilot-2 bootstrap docs, Turn template, and setting-pack rules.
- [x] Step 2: Confirm branch, git status, latest log, `mvp-6`, setting pack existence, ignore rules, and absence of `data/pilot2_playtest.db*`.
- [x] Step 3: Update `PLANS.md` for playtest new-game bootstrap.
- [x] Step 4: Run `init-db --db data/pilot2_playtest.db`.
- [x] Step 5: Run `validate-setting-pack`, `preview-setting-import`, and `apply-setting-pack` against the new DB.
- [x] Step 6: Start a fresh session at `scene-qingli-herb-warehouse-perimeter`.
- [x] Step 7: Run read-only `show-session`, `pilot-preflight`, and `export-session-evidence` if supported.
- [x] Step 8: Write `docs/PILOT2_PLAYTEST_BOOTSTRAP_REPORT.md`.
- [x] Step 9: Run `git diff --check` and `gradlew.bat clean test --no-daemon`.
- [x] Step 10: Commit `docs: bootstrap Pilot-2 playtest session` and push tracked docs only.
- [x] Step 11: Directly notify 03 read-only review thread.

## Verification

Required:

- `git status`
- `init-db --db data/pilot2_playtest.db`
- `validate-setting-pack`
- `preview-setting-import`
- `apply-setting-pack`
- `start-session`
- `show-session`
- `pilot-preflight` if supported
- `export-session-evidence` if supported
- `git diff --check`
- `gradlew.bat clean test --no-daemon`

Safety checks:

- Confirm no `prepare-turn` is run.
- Confirm no context/result/narrative/resolve artifacts are produced.
- Confirm `data/pilot2.db` is untouched by this milestone.
- Confirm ignored `data/` and `out/` artifacts are not staged.
- Confirm `mvp-6` remains unchanged.

## Risks

- Existing `data/pilot2_playtest.db*` files would require stopping instead of overwrite.
- Setting pack apply could fail in a fresh DB if the pack drifted.
- Console output may contain encoding artifacts; report stable IDs and JSON sidecars rather than relying only on localized text.
- Accidentally staging ignored DB/out artifacts.

## Drift Log

- 2026-06-21: Started Pilot-2 playtest new-game bootstrap milestone using the existing setting pack and a new dedicated DB.
- 2026-06-21: Milestone paused before commit/push while 01 checked the user's old-world/new-opening intent. Existing `data/pilot2_playtest.db` and ignored sidecars were retained without cleanup or overwrite.
- 2026-06-21: Milestone resumed after 01 confirmed the user accepts the old world base. The hiking/fog prologue and expanded Ai-chan sensory details remain opening/character-card candidates only and were not written to canon or DB.
- 2026-06-21: Fresh playtest session created as `session-3aad7bfe-fed8-421b-868d-0cf033c8339c`, `branch-103aab87-d3ad-43ed-9205-9ce0a84fc92b`, `nextTurnNumber=1`, `stateVersion=0`, `pendingContexts=0`; no Turn 1 lifecycle commands were run.
- 2026-06-21: `pilot-preflight` PASS with `contextBytes=16218/24576`, `hashValid=true`, and `pendingContextCount=0`; `export-session-evidence` reported `turnLogs=0`, `stateDeltas=0`, `pendingContexts=0`.
- 2026-06-21: `git diff --check` PASS. First full Gradle test attempt timed out and left a Java process holding test output files; after stopping the stale Java process, `gradlew.bat clean test --no-daemon` PASS.
