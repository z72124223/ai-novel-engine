# Codex 09 Foundation Card Execution Preflight V0

Status: `GAME_MODE_LOW_INTERFERENCE_PREFLIGHT_DRAFT`

Date: 2026-07-02

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## Purpose

This is a GAME_MODE_LOW_INTERFERENCE docs/control packet. It prepares the next
safe route after the whole-project blueprint control layer: 09 foundation card
execution packet planning.

This packet does not create card JSON, runtime data, CLI commands, SQLite
schema, canon, setting-pack content, asset registry entries, storyboard output,
or media. It only explains how a later non-game-mode task could turn the
accepted `docs/CODEX_09_FOUNDATION_CARD_ORGANIZATION_V0.md` planning layer into
a bounded implementation plan.

## 09 Foundation Route Purpose

The 09 foundation route organizes the first reviewable planning layer before
demand-driven query, script-ready scene packets, 04/05/06/07/08, storyboard,
asset registry, voice timeline, renderer execution, or external provider work.

It keeps these card families separate and non-canon until later approval:

- world premise cards
- character foundation cards
- location foundation cards
- system foundation cards
- constraint cards
- asset seed cards
- situation seed cards

The route purpose is organization, not approval. These cards are planning
surfaces for future query and scene-packet work; they are not setting packs,
runtime truth, approved assets, or production data.

## Next Non-Game-Mode Implementation Candidate

A later explicit 01/user task could implement a small local file-protocol JSON
packet for 09 foundation card drafting. A safe first implementation could be:

- add draft-only `FoundationCard` JSON models or templates;
- add local validation rules for required fields, family names, status, source
  references, visibility, constraints, forbidden implications, and promotion
  flags;
- add one sample non-canon card packet only if user/01 supplies or approves the
  source worldbuilding;
- add focused tests only outside GAME_MODE_LOW_INTERFERENCE;
- keep every output `draft_non_canon`, `settingPackReady=false`,
  `assetRegistryReady=false`, and `queryEligible=false` unless a later explicit
  review task changes that boundary.

This preflight does not start that work.

## Stop Conditions

Stop before implementation if any of these are true:

- user worldbuilding missing;
- canon approval is needed or implied;
- setting-pack conversion is requested or implied;
- SQLite/schema/direct writes would be needed;
- 04/05/06/07/08 resume would be needed;
- asset approval/registry work would be needed;
- provider/API calls would be needed;
- renderer/ffmpeg/TTS/media generation would be needed;
- final media approval would be needed or implied;
- release/production lock would be needed or implied;
- the task would treat the old local baseline 100% as true final completion.

## Proposed Follow-Up Tasks For 01

01 can later choose exactly one of these after the user is no longer in game
mode:

1. Small docs slice: turn this preflight into a detailed 09 foundation card
   JSON contract spec, still docs-only.
2. Medium implementation slice: add draft-only 09 foundation card local JSON
   models, validator, CLI, tests, and one non-canon fixture if source
   worldbuilding is available.
3. Small decision slice: ask the user for the missing worldbuilding needed to
   draft the first 09 foundation card packet.
4. Small control slice: review whether the default route should remain 09
   foundation or switch to user / 00 input first.

## Boundary

The older local product-delivery baseline showing 100% only meant a previous
local baseline had passed. It is not true final project completion, not final
video readiness, not renderer readiness, not provider/media handoff, not canon,
not registry approval, not release, not final media approval, and not production
lock.

This preflight is docs/control only:

- no Kotlin source or test edits;
- no Gradle;
- no runtime/CLI/schema implementation;
- no SQLite direct writes;
- no provider/API;
- no web/server;
- no renderer/ffmpeg/TTS/media generation;
- no 04/05/06/07/08 resume;
- no asset approval/registry;
- no canon, registry, release, approval, final media approval, or production
  lock;
- no staging, commit, or push;
- no 03 contact.

## Verification

Allowed verification for this GAME_MODE_LOW_INTERFERENCE slice:

- `rg` string checks;
- trailing-whitespace scan;
- `git diff --check` for touched tracked files;
- `git status --short --branch`.

Gradle, Kotlin compile/test, renderer, ffmpeg, TTS, media generation, provider
calls, GUI inspection, screenshots, quota-panel inspection, staging, commit, and
push are not allowed in this slice.
