# Plan

## Goal

Create a fresh Pilot-2 DB and session for future real story-arc audit sample collection, then document the exact bootstrap state. Do not start Pilot-2 Turn 1 and do not create any formal story result.

## Global Strategy

Use only existing local CLI commands to initialize an ignored DB, apply a dedicated Pilot-2 setting pack through the formal setting-pack path, start a session, and verify it with read-only inspection. Commit only documentation that records the bootstrap state; generated DB and output artifacts stay ignored and untracked.

## Non-Goals

- No Pilot-2 Turn 1 execution.
- No `prepare-turn`.
- No `resolve-turn`.
- No AiTurnResult generation.
- No Kotlin code changes.
- No test changes.
- No audit rule changes.
- No schema or migration changes.
- No Validator changes.
- No `stateVersion` or `contextHash` changes.
- No `resolve-turn` or `StateReducer` changes.
- No direct SQLite writes.
- No Candidate B.
- No MVP-8.
- No server, API, Docker, Android UI, Rust, embedding, vector DB, or multi-agent runtime.
- No retagging `mvp-6`.
- No push unless the user explicitly approves later.

## Scope

- `PLANS.md`
- `docs/PILOT2_SESSION_BOOTSTRAP.md`
- ignored generated artifacts under `data/` and `out/`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Local `HEAD` is `db9754ce2ac22e75178d653d5df563723eadac78`.
- Local branch is synchronized with `origin/codex/mvp6-checkpoint-branching-spec`.
- `mvp-6` points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Working tree was clean before this milestone.
- `docs/PILOT2_SCENARIO_DESIGN.md` recommends the Pilot-2 scenario `青犁鎮藥市封倉`.
- `docs/PILOT2_TURN_TEMPLATE.md` says that if the Pilot-2 DB/session has not been created yet, a fresh Pilot-2 DB/session should be prepared as a separate milestone.
- `README.md` documents `init-db`, `apply-setting-pack`, `start-session`, and `show-session`.
- `VERIFY.md` requires `git diff --check` and `gradlew.bat clean test --no-daemon` before commit.
- `samples/` does not contain a committed Pilot-2 setting pack; a temporary bootstrap pack must therefore stay under ignored `out/`.

## Assumptions

- A dedicated `data/pilot2.db` is acceptable because `data/*.db` is intended for ignored local DB artifacts.
- A temporary `out/pilot2/bootstrap/pilot2_setting_pack.json` is acceptable because generated `out/` artifacts are ignored and not committed.
- Applying a setting pack through `apply-setting-pack` is the correct formal way to seed project data; it is not a direct SQLite write.

## Open Questions

- The user still needs to provide the actual Pilot-2 Turn 1 player input after this bootstrap.
- If the user wants a different Pilot-2 scenario premise later, a future milestone may need a new setting pack and fresh DB.

## Implementation Steps

- [x] Step 1: Read required Pilot-2 docs, control docs, README, and sample inventory.
- [x] Step 2: Update `PLANS.md` for this bootstrap milestone.
- [x] Step 3: Create ignored Pilot-2 bootstrap setting pack under `out/pilot2/bootstrap/`.
- [x] Step 4: Initialize fresh `data/pilot2.db` with `init-db`.
- [x] Step 5: Validate, preview, and apply the Pilot-2 setting pack.
- [x] Step 6: Start Pilot-2 session and confirm it with read-only `show-session`.
- [x] Step 7: Create `docs/PILOT2_SESSION_BOOTSTRAP.md`.
- [x] Step 8: Verify with `git diff --check`.
- [x] Step 9: Verify with `gradlew.bat clean test --no-daemon`.
- [x] Step 10: Commit `docs: bootstrap Pilot-2 session`.

## Verification

- `git status`
- `git check-ignore -v data/pilot2.db out/pilot2/bootstrap/pilot2_setting_pack.json`
- `init-db`
- `validate-setting-pack`
- `preview-setting-import`
- `apply-setting-pack`
- `start-session`
- read-only `show-session`
- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- confirm no Kotlin, test, schema, migration, Validator, `resolve-turn`, `StateReducer`, or non-ignored generated artifacts changed

## Risks

- The temporary setting pack may fail validation and require adjustment before apply.
- Starting a session writes legitimate session/runtime rows through the formal CLI; this is expected for this milestone but must not proceed to `prepare-turn`.
- Accidentally committing generated DB or output artifacts would violate the milestone.
- A session based on the bootstrap setting pack is ready for future sample collection but is not itself a completed story turn.

## Drift Log

- 2026-06-20: Started fresh Pilot-2 DB/session bootstrap after the user explicitly approved a separate initialization milestone and prohibited Turn 1 execution.
- 2026-06-20: Created ignored bootstrap pack `out/pilot2/bootstrap/pilot2_setting_pack.json`; `git check-ignore` confirmed both `data/pilot2.db` and the pack path are ignored.
- 2026-06-20: Ran formal CLI bootstrap: `init-db`, `validate-setting-pack`, `preview-setting-import`, `apply-setting-pack`, `start-session`, and read-only `show-session`. Created session `session-ea110b6c-7509-40e6-a786-99cf00d8fc66` for project `pilot2`, opening scene `scene-qingli-herb-warehouse-perimeter`, next turn `1`, state version `0`.
- 2026-06-20: Added `docs/PILOT2_SESSION_BOOTSTRAP.md` documenting DB path, project id, session id, CLI commands, output paths, ignored artifacts, and stop conditions. No `prepare-turn` or `resolve-turn` was executed.
- 2026-06-20: `git diff --check` passed, and `gradlew.bat clean test --no-daemon` passed with 233 tests, 0 failures, 0 errors, and 0 skipped.
- 2026-06-20: Sealing this bootstrap milestone in local commit `docs: bootstrap Pilot-2 session`; no push is performed in this milestone.
