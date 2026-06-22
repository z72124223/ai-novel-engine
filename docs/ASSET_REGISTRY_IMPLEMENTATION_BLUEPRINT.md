# Asset Registry Implementation Blueprint

Status: `FUTURE_BLUEPRINT_NOT_IMPLEMENTED`

Date: 2026-06-22

This blueprint describes a future implementation path for production asset registration that supports storyboard asset consistency first. It does not implement schema, migrations, Kotlin code, CLI commands, Validator rules, StateReducer behavior, file storage, GPT browser automation, Seedance integration, or Oiioii integration.

## 1. Verdict

The long-term project should use two layers:

1. Draft asset files for unapproved candidate work.
2. SQLite-backed formal asset registry for approved production assets, if a later milestone explicitly approves it.

The draft layer keeps exploration cheap and flexible. The SQLite layer keeps large future data searchable, versioned, auditable, and stable.

Near-term goal: keep asset planning as docs and draft candidates that help 08 produce consistent storyboard images. Do not write directly to SQLite. Any future formal asset registry must follow an explicit validation / preview / apply flow.

The demand-driven Asset Queryer described in `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md` is a future product-layer retrieval contract over these asset records or draft candidates. It is not implemented by this blueprint, but it defines how 04 / 08 should eventually request scoped asset ids, scene ids, zone ids, variants, and prompt-safe notes without redesigning assets.

## 2. Why Not Only Files

Files are useful while the format is changing, but future production data will become too large and relational for plain JSON / YAML alone.

The project will need queries such as:

- which scenes use this character asset;
- which storyboard frames use this weapon variant;
- which assets remain `candidate`;
- which assets are safe for final storyboard delivery or optional downstream video production;
- which packet references an unknown asset id;
- which variants descend from a broken or repaired object;
- which GPT outputs have not passed review;
- which references become invalid after redesign.

Those are database-shaped questions.

## 3. Layered Architecture

```text
04 / 07 / user / GPT produces candidate
        |
        v
Draft asset file
        |
        v
05 review + 06 human approval
        |
        v
08 uses reviewed references for storyboard pickup / verification
        |
        v
optional future 01-approved formal import
        |
        v
01 assigns formal import milestone
        |
        v
02 runs validate -> preview -> apply
        |
        v
SQLite formal asset registry
        |
        v
04 / 05 / 06 / 07 / 08 / optional video workflows reference assetId
```

This mirrors the existing setting-pack discipline: parse, validate, diff / preview, transaction apply, export / audit.

The formal SQLite portion is a future phase only. This milestone does not approve a migration, CLI, asset DB, import command, or runtime gate.

## 4. Draft Asset Files

Draft files are for candidates only.

Use draft files when:

- GPT proposes a character design;
- 04 proposes a new prop needed by a packet;
- 07 prepares an asset candidate needed for storyboard consistency;
- 08 retrieves a storyboard image candidate;
- the user is still deciding;
- required reference views are incomplete;
- a variant has not passed review.

Draft files should not be treated as production truth.

Suggested future location:

```text
assets_drafts/
  characters/
  animals/
  scenes/
  props/
  weapons/
  costumes/
  variants/
  storyboard_frames/
```

This location is not created by this document. A later milestone must decide whether these files are tracked, ignored, or partially tracked.

## 5. Formal SQLite Asset Registry

Approved assets should eventually move into SQLite because:

- asset data will grow;
- packets need stable references;
- variants need parent / child relations;
- review status needs history;
- storyboards need asset usage checks;
- storyboard handoff needs production-safe asset queries;
- optional Seedance / Oiioii handoff may later need production-safe asset queries.

Future tables may include:

- `production_assets`;
- `asset_references`;
- `asset_variants`;
- `asset_usage`;
- `asset_review_events`;
- `asset_prompt_safety_notes`;
- `asset_exports`.

This is a conceptual table list only. It does not approve a migration.

## 6. Future Validation Flow

Potential future commands:

```text
validate-asset-draft
preview-asset-import
apply-asset-import
export-asset-registry
audit-packet-assets
audit-storyboard-assets
```

The expected lifecycle:

1. `validate-asset-draft`
   - checks required fields;
   - checks `assetId` naming;
   - checks required reference views;
   - checks parent variant relationships;
   - checks forbidden drift notes;
   - checks prompt safety notes.

2. `preview-asset-import`
   - compares draft asset to formal registry;
   - reports new assets, variants, supersessions, conflicts, and missing references;
   - reports downstream packet / storyboard impact.

3. `apply-asset-import`
   - writes only after validation passes;
   - uses a transaction;
   - records review event and source;
   - never imports author-only secrets into prompt-facing fields.

4. `export-asset-registry`
   - emits human-readable approved asset summaries;
   - can prepare GPT storyboard-safe and optional video-safe asset sheets without secrets.

5. `audit-packet-assets`
   - checks packet `usedAssets`;
   - fails unknown asset ids;
   - flags candidate-only assets used in production context;
   - flags missing variants.

6. `audit-storyboard-assets`
   - checks each storyboard frame references approved assets;
   - flags visual drift and forbidden additions;
   - checks scene and asset compatibility.

## 7. Future Data Model Questions

These need explicit architecture decisions later.

### 7.1 Same DB Or Sidecar DB

Option A: same runtime SQLite DB.

Pros:

- easy turn / scene / asset relations;
- one backup surface;
- one CLI path.

Cons:

- production asset data may bloat runtime DB;
- image references and review artifacts may not belong in gameplay state;
- stronger risk of mixing authoring assets with runtime facts.

Option B: separate production asset SQLite DB.

Pros:

- cleaner separation from game state;
- easier to back up or share production package;
- safer for animation workflow.

Cons:

- needs cross-db references or export/import discipline;
- more CLI / project configuration.

This blueprint does not choose. It records the decision point.

### 7.2 Images In DB Or File Paths

Likely recommendation:

- store metadata and paths in SQLite;
- store actual images as files or external references;
- track only small approved references if the repo policy permits;
- keep large generated files ignored unless explicitly approved.

Do not commit large GPT / Oiioii outputs by default.

### 7.3 Runtime Knowledge Boundary

Production assets should not become character knowledge automatically.

If a prop exists in the asset registry, that does not mean a character has seen it, owns it, or can use it in the current scene.

Runtime knowledge, scene visibility, inventory, and capability rules still belong to the existing Validator / StateReducer flow.

## 8. Future Asset Id Rules

Suggested pattern:

```text
<type>-<slug>-v<number>
<type>-<slug>-<variant>-v<number>
```

Examples:

```text
char-protagonist-v1
animal-ai-chan-v1
scene-qingli-warehouse-perimeter-v1
prop-seal-strip-wet-v1
weapon-iron-sword-v1
weapon-iron-sword-broken-v1
costume-patrol-uniform-rain-v1
```

Rules:

- never translate ids casually;
- never reuse an id for a redesign;
- variant ids should point to a parent asset;
- damaged, repaired, wet, bloodied, burned, aged, or disguised forms should be variants;
- scene ids and production scene ids may need separate namespaces.

## 9. Future 04 / 05 / 06 / 07 Contract Impact

04 should eventually:

- list `usedAssets`;
- list `newAssetCandidates`;
- avoid redesigning registered assets in prose;
- request variants when story state changes an asset.

05 should eventually:

- fail unknown asset references;
- flag candidate assets used as if approved;
- flag asset drift;
- require variants for damage / repair / disguise / weathering.

06 should eventually:

- show the user which assets a packet uses;
- ask for explicit approval before a candidate asset can be imported;
- avoid presenting asset candidates as production-locked.

07 should eventually:

- use only approved asset references when asking GPT for storyboard images;
- return GPT images as candidate references;
- mark drift issues;
- never push GPT output directly to a video pipeline.

08 should eventually:

- cite the Asset Queryer response used for each storyboard frame;
- report missing asset ids, scene ids, zone ids, or variants instead of inventing them;
- treat `candidate` and `review_ready` assets as unstable unless 01 / 05 / 06 approval explicitly promotes them;
- preserve prompt-safe summaries and forbidden-drift notes returned by the query layer.

## 10. Future Engineering Phases

Recommended order:

### Phase A: Docs And Templates

- `ASSET_REGISTRY_SPEC.md`;
- `ASSET_ID_NAMING.md`;
- draft asset templates;
- review checklist;
- storage policy.

### Phase B: Draft File Validation

- validate asset draft files without SQLite writes;
- detect missing required views and forbidden drift fields;
- check variant parent references.

### Phase C: SQLite Formal Registry Design

- decision brief: same DB versus sidecar DB;
- additive migration only if approved;
- import transaction design;
- export format design.

### Phase D: Import / Export CLI

- validate;
- preview;
- apply;
- export.

### Phase E: Packet / Storyboard Audit

- audit packet asset references;
- audit storyboard frame asset references;
- report unknown or unstable assets.

### Phase F: GPT Storyboard And Optional Video Handoff

- only after assets, scenes, and storyboards are stable;
- GPT used for low-cost reference generation;
- 08 uses GPT / image-model output for storyboard pickup and verification;
- Seedance / Oiioii used only if a later optional downstream video milestone is approved.

## 11. Safety Boundaries

This blueprint does not approve:

- schema changes;
- migrations;
- direct SQLite writes;
- production asset tables;
- image storage;
- generated image commits;
- OpenAI API / GPT API integration;
- Oiioii integration;
- Seedance integration;
- browser automation implementation;
- Validator or StateReducer changes;
- runtime asset gates;
- 04 / 05 / 06 / 07 automatic execution.

Any future implementation must be a separate user / 01 approved milestone.

## 12. Recommended Next Milestone

The safest next milestone after this blueprint is still docs-only:

```text
NEW_WORLD_FOUNDATION_QUESTIONNAIRE
```

Scope:

- rebuild the non-Qingli world foundation before asset pickup resumes;
- keep protagonist and Ai-chan seeds while avoiding Qingli as the new base;
- define the initial world, era, material culture, geography, social structure, and scene set needed before 04 / 05 / 06 / 07 / 08 restart.

Only after the new world foundation is approved should the project consider a storyboard asset spec / templates milestone. Any SQLite formal registry, schema, import CLI, or optional video handoff remains a later separately approved phase.
