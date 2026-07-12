# REBOOT-01 Stage 6 — Local Engine-Backed Web Slice

Date: 2026-07-12

Status: `STAGE_6_COMPLETION_AUDIT_PASS`

## Delivered behavior

The dependency-free Kotlin/JDK server binds only to literal loopback hosts. Each Web Session owns a temporary working directory. Every submit rebuilds a fresh isolated SQLite database and runs `RebootVerticalSliceRunner` through the requested turn, which uses `MultiTurnRuntime.prepareTurn` and `resolveTurn` with the normal Validator, state-version, Context-hash and transaction boundaries.

The page supports six actions, story text, world time, character and relationship summaries, save/load, and developer-mode Context, Critic, DecisionTrace and State Delta inspection.

## Honest product boundary

Mode is `LOCAL_ENGINE_BACKED_SCENARIO`. The result adapter is `DETERMINISTIC_SCENARIO_ADAPTER`. Player input enters Context and is persisted as a sandbox intent event, so different inputs produce different Context hashes and SQL evidence. Narrative resolution remains bounded; there is no external model or claim of free-form generation.

The browser never receives a SQLite path. There is no login, public deployment, cloud service, API key or provider call.

## Verification

`RebootLocalWebServerTest` verifies:

- loopback-only binding, literal Host allowlist and foreign-Origin rejection;
- six real engine-backed submissions;
- State Version / applied-turn progression;
- different player inputs produce different Context hashes and State Delta evidence;
- debug evidence, completion at `T+35m`, save and load by deterministic engine reconstruction;
- temporary Session data is removed when the server closes, with an explicit cleanup assertion.

No Canon, image, external provider or original dirty-worktree content is modified.
