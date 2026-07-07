# Codex World Source Layering Blueprint Gap Audit V0

Task: `world_source_layering_blueprint_gap_audit_v0_docs_control`

Status: `DRAFT_FOR_REVIEW`

Selected outcome:
`WORLD_SOURCE_LAYERING_BLUEPRINT_GAP_AUDIT_ALIGNED_FOR_DISCUSSION_AND_FUTURE_CONVERSION`

This is a docs/control blueprint-control audit. It records the verified
world-source layer state for Chenxing and adds a first-read pointer target for
future A01 discussion. It is not setting-pack apply, canon/entity conversion,
runtime writes, source-basis selection, packet creation, media work, approval,
release, or production lock.

## Corrected Data-Layer Classification

The current data classification is layered:

- raw source archive exists: `world/chenxing/raw/` contains 9 Chenxing raw
  worldbuilding source files.
- source manifest exists:
  `memory/chenxing_world_source_manifest.json` lists 9 source sets and keeps
  raw-text integrity, classification-only, and no-rewrite boundaries.
- official source SQL archive exists:
  `memory/chenxing_world_source_sql_import_result.json` reports
  `WORLD_SOURCE_ARCHIVE_SQL_IMPORT_PASS`.
- official source SQL archive/query index exists in `data/story.db` through
  the controlled `query-world-source --db data/story.db --q <text>` entrypoint.
- SQL source archive tables are defined by
  `app/src/main/resources/migrations/007_world_source_archive.sql`:
  `world_source_archives`, `world_source_documents`, and
  `world_source_chunks`.
- setting-pack/canon/runtime layer not yet converted.
- final-product layer not yet generated.

The official source SQL archive/query layer is SQL-backed source evidence only.
It is not setting-pack/canon/runtime SQL, not canon approval, not runtime
character/location/world-rule state, not approved asset data, not a final
product artifact, and not permission for direct SQLite mutation.

## Source Layer Ladder

`source layer ladder`:

`raw user source` -> `source manifest` -> `official source SQL archive/query index` -> `reviewed derived source cards / requirement matrices` -> `future evidence/source-basis planning` -> `future ScriptReadyScenePacket` -> `future storyboard/camera` -> `future prompt/asset cards` -> `future voice/subtitle/SFX` -> `future renderer/package QA` -> `human approval gates`

This ladder reduces scene packet uncertainty by making future work name which
layer it is using. A future packet may depend on the ladder, but this document
does not create a packet, query, evidence bundle, concrete refs, source-basis
selection, prompt card, storyboard, renderer package, media, or sample JSON.

## Current-Worldview Discussion Entrypoint

`current-worldview discussion entrypoint` for A01 should point to:

- `memory/active_world_source.json`
- `memory/chenxing_world_source_manifest.json`
- `data/story.db` via `query-world-source --db data/story.db --q <text>`
- `world/chenxing/raw/`
- `docs/CODEX_CHENXING_09_FOUNDATION_CARD_PACK_CONTRACT_SAMPLE_V0.md`
- `docs/CODEX_CHENXING_09_TO_SCENE_PACKET_ALIGNMENT_V0.md`
- `docs/CODEX_CHENXING_09_SCENE_PACKET_SHAPE_READINESS_V0.md`
- `docs/CODEX_CHENXING_09_SCOPED_QUERY_SURFACE_V0.md`
- `docs/CODEX_CHENXING_09_SCENE_PACKET_CANDIDATE_SKELETON_V0.md`
- `docs/CODEX_CHENXING_09_SCENE_PACKET_SOURCE_REQUIREMENT_MATRIX_V0.md`

The next safe A01 discussion surface is worldview, characters, and items
discussion. It is not B02 implementation unless B01 later dispatches a bounded
implementation package.

## Current Worldview Guards

- Current-facing protagonist name is only `陸知行`; `衛昭` is superseded and
  must not be used as protagonist name in derived/current-facing docs/control
  or future planning surfaces.
- 愛醬 has her own story line and must not be reduced to pet, tool, clue
  machine, mascot, combat asset, or mount-only growth.
- Living world first; final-product chains serve the world later.
- Old opening/mainline material is discussion evidence only, not locked plot,
  canon, or final-product content.
- 陸知行 core desire is curiosity, logic, clue assembly, and learning the world.
- World pressure includes outside-city ecology, exhaustion, waves, vertical
  danger, and inside-city identity, class, and profession pressure.
- Profession dignity remains required. If the cook example appears, preserve
  survival, restoration, and difficult monster-ingredient handling logic.

## A-Group Proposal Blueprint Reflection Audit

`A-group proposal blueprint reflection audit`:

| Entrypoint | Reflection state | Audit result |
| --- | --- | --- |
| `docs/CODEX_WHOLE_PROJECT_BLUEPRINT_V0.md` | A-group evolution and optimization are visible; source layer ladder was not visible from the top blueprint. | Minimal source-layer pointer added by this slice. |
| `docs/CODEX_BLUEPRINT_EXECUTION_CONTROL_INDEX_V0.md` | A-group evolution, optimization, and route-loop convergence are visible; source SQL layer was not a route-by-need entry. | Minimal route-by-need pointer added by this slice. |
| `docs/CODEX_CURRENT_ROUTE_CARD_V0.md` | A06 same-family convergence, route-family convergence, A-group evolution, heartbeat, and route-loop convergence are visible; source layer ladder was not visible in current route digest. | Minimal current route pointer added by this slice. |
| `docs/HANDOFF.md` | Already records `WORLD_SOURCE_ARCHIVE_SQL_IMPORT_PASS`, SQL tables, `query-world-source`, and active Chenxing source direction. | Reflected; not touched because it is dirty and no current-slice edit is needed. |
| `docs/CODEX_A_GROUP_EVOLUTION_SYSTEM_V0.md` | Dedicated A-group evolution route exists and is cited by first-read entrypoints. | Reflected; no correction needed. |
| `docs/CODEX_A_GROUP_PROACTIVE_EVOLUTION_HEARTBEAT_RULE_SET_V0.md` | Dedicated heartbeat rule set exists and is visible from current route card. | Reflected; no correction needed. |
| `docs/CODEX_ROUTE_LOOP_CONVERGENCE_RULE_SET_V0.md` | Dedicated route-loop and same-family convergence rule set exists and is visible from first-read entrypoints. | Reflected; no correction needed. |
| `docs/CODEX_A_GROUP_OPTIMIZATION_AUDIT_LANE_V0.md` | Dedicated optimization lane exists and is visible from blueprint/index entrypoints. | Reflected; no correction needed. |

Missing or stale blueprint/control gap found: the Chenxing source ladder was
not visible enough from first-read blueprint/control surfaces. Correction:
point the top blueprint, execution-control index, and current route card to this
audit instead of duplicating long history.

## Future Conversion Gap Categories

- source archive to reviewed derived source cards remains a review/curation
  conversion gap, not content approval.
- reviewed derived source cards to future evidence/source-basis planning remains
  a planning gap, not source-basis selection.
- future evidence/source-basis planning to future `ScriptReadyScenePacket`
  remains packet planning, not actual packet creation.
- downstream storyboard/camera, prompt/asset cards, voice/subtitle/SFX,
  renderer/package QA, and human approval gates remain future layers.

## Boundary Rules

- no setting-pack apply.
- no canon/entity conversion.
- no runtime writes.
- no direct ad hoc SQL writes.
- no SQLite mutation.
- no provider/API.
- no sample JSON.
- no raw archive mutation.
- no actual `ScriptReadyScenePacket`, `ScriptReadyScenePacketCandidate`,
  `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`.
- no source-basis selection, evidence bundle, concrete refs, story prose, scene
  prose, prompt card body, storyboard, renderer package, generated media,
  approval, canon, registry, release, final approval, or production lock.

## Review Checklist

- Verify `WORLD_SOURCE_ARCHIVE_SQL_IMPORT_PASS` remains evidence for source SQL
  archive/query availability only.
- Verify `world_source_archives`, `world_source_documents`, and
  `world_source_chunks` are classified as official source archive/index tables,
  not setting-pack/canon/runtime tables.
- Verify current-worldview discussion uses source ids, paths, and reviewed
  docs/control surfaces rather than copied raw archive prose.
- Verify A01 discussion stays at worldview, characters, and items unless a
  future B01-dispatched package starts implementation.
- Verify route-loop convergence is preserved: this audit clarifies source
  layers and first-read pointers rather than adding another same-family status
  layer.
