# Codex Asset Lifecycle Registry Decision Packet V0

Status: `DRAFT_FOR_REVIEW`

Route label: `asset_lifecycle_registry_decision_packet_v0_docs_control`

This document defines an `AssetLifecycleRegistryDecisionPacket` for docs/control
review only. It is not implementation, not a final decision, not schema, not a
migration, not runtime data, not direct SQLite writes, not an asset registry,
not asset approval, not registry approval, not provider/API, not media
generation, not canon, not release, not final approval, and not production
lock.

## Purpose

`AssetLifecycleRegistryDecisionPacket` prepares a reviewable decision surface
for asset lifecycle and registry storage. It does not choose an option.

The decision surface is for B組01/A組01/00/user where required. A technical
review PASS only means the packet is complete enough to support a later
decision. PASS is not user approval, not asset approval, not registry approval,
not canon, not release, not final media approval, and not production lock.

## Decision Options

The packet compares only options and boundaries.

### `draft_files_only`（只保留草稿檔案）

Meaning:

- keep asset candidates as local draft files or docs/control references;
- avoid formal registry tables;
- keep exploration cheap while formats are still changing.

Possible strengths:

- lowest implementation risk;
- no schema or migration;
- easier to revise candidate formats;
- safer before user approval and asset approval exist.

Possible risks:

- weak querying across scenes, panels, variants, and storyboard packets;
- harder to audit duplicate or superseded assets;
- file naming and folder policy must still be controlled.

Boundary:

- no approved assets;
- no registry records;
- no production lock;
- no direct SQLite writes;
- no media generation.

### `same_runtime_sqlite_db`（同一 runtime SQLite 資料庫）

Meaning:

- store future formal production asset metadata in the same SQLite database as
  runtime state, if a later explicit schema milestone approves it.

Possible strengths:

- easier relation between scenes, packets, asset ids, and runtime evidence;
- one backup and export surface;
- one CLI configuration path.

Possible risks:

- production asset data may bloat or confuse runtime state;
- authoring assets may be mistaken for character knowledge or gameplay facts;
- schema and migration discipline becomes stricter.

Boundary:

- option comparison only;
- no schema/migration approval;
- no runtime write path;
- no asset approval;
- no production lock.

### `sidecar_production_asset_db`（旁掛式 production asset 資料庫）

Meaning:

- store future formal production asset metadata in a separate SQLite database or
  sidecar store, if a later explicit architecture decision approves it.

Possible strengths:

- cleaner separation between gameplay runtime and production assets;
- easier to package, back up, or share production asset data separately;
- lower risk of asset entries becoming character knowledge.

Possible risks:

- needs cross-db or file-protocol references;
- more CLI configuration and validation paths;
- import/export discipline must prevent broken ids.

Boundary:

- option comparison only;
- no sidecar database creation;
- no schema/migration approval;
- no registry import implementation;
- no production lock.

### `deferred_decision`（延後決策）

Meaning:

- keep the decision explicitly open until storyboard, asset, prompt-surface, and
  approval routes are clearer.

Possible strengths:

- avoids premature storage architecture;
- keeps current route docs/control only;
- gives user/00 more time to decide final product and funding constraints.

Possible risks:

- may slow future asset tooling;
- may require repeated handoff notes so downstream windows do not assume a
  registry exists.

Boundary:

- no storage option is selected;
- future tasks must keep using candidate-only references until a real decision
  is made.

## Docs-Control Lifecycle Labels

These labels are planning vocabulary only. They are not schema values, not
registry records, and not approved assets.

- `candidate`: proposed asset need or draft reference.
- `review_needed`: candidate needs technical or human review.
- `technically_reviewed`: technical review has checked shape and boundaries.
- `user_approved_reference`: user has explicitly approved use as a reference,
  but this still requires separate registry/storage handling if needed.
- `rejected`: should not be used downstream.
- `deprecated`: older reference kept only for history.
- `superseded`: replaced by a newer reference or variant, with migration notes
  still needed before any production use.

No lifecycle label in this packet grants asset approval, registry approval,
canon, release, final media approval, or production lock.

## Relationship To Upstream Packets And Cards

`StoryboardCameraPacket` may include `assetNeedRefs`, but those references are
candidate needs only. They do not become approved assets or registry entries.

Prompt cards, `ReferenceCard`, `PromptIntentCard`, `NegativeBoundaryCard`, and
`CompositionRecipe` may describe prompt-facing intent or composition rules, but
they remain separate from storage approval. They must not be converted into
formal registry entries without a future explicit route decision, review, and
approval path.

Asset notes should preserve their source:

- storyboard/camera panel need;
- prompt card candidate;
- reference card candidate;
- composition recipe candidate;
- user-approved reference, only if explicit evidence exists.

## Prompt Surface Safety

This packet follows `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`.

Registry or asset notes must not leak `author_only` / `review_only` material
into prompts, screen descriptions, negative prompts, or prompt-safe asset notes
unless the exact field is explicitly listed in `allowedPromptSurface`.

Negative prompts are also prompt surface. Forbidden details, drift notes, and
registry comments must not become a hidden channel for culprit identity, sealed
motive, hidden causality, director goal, future reveal, or unavailable actor
knowledge.

When protected information is relevant, use `protectedBoundaryRef` plus generic
`boundaryCategory`, not the protected fact.

## Visibility And Approval Boundary Rules

Every future packet or draft using this decision surface should declare:

- `visibility`;
- `allowedPromptSurface`;
- `forbiddenPromptSurface`;
- source evidence;
- approval status;
- review status.

Approval boundaries remain separate:

- 03 PASS is technical only.
- User approval is separate.
- Asset approval is separate.
- Registry approval is separate.
- Canon approval is separate.
- Release approval is separate.
- Final media approval is separate.
- Production lock is separate.

No route may infer approval from a docs/control PASS.

## Review Checklist

A review should check these areas.

### Option Completeness

- Are `draft_files_only`, `same_runtime_sqlite_db`,
  `sidecar_production_asset_db`, and any `deferred_decision` path described?
- Does the packet avoid choosing an option?
- Are required human/user decisions marked when needed?

### Data Ownership

- Does the packet keep runtime state separate from production asset planning?
- Does it avoid turning assets into character knowledge?
- Does it identify who may approve asset/reference promotion later?

### Storage Risk

- Are same-DB and sidecar risks stated?
- Are draft-file audit risks stated?
- Is large media storage explicitly out of scope?

### Migration / Schema Implications

- Does the packet avoid approving schema, migration, tables, import CLI, export
  CLI, or direct SQLite writes?
- Does it require a future explicit task before any storage implementation?

### Prompt-Surface Leakage

- Does every prompt-relevant note obey `allowedPromptSurface`?
- Are negative prompts treated as prompt surface?
- Are `author_only` / `review_only` materials kept out of prompt-facing notes?
- Are protected boundaries referenced generically?

### Asset Approval Boundaries

- Does the packet avoid treating `candidate`, `technically_reviewed`, or
  `user_approved_reference` as formal registry approval?
- Does it keep asset approval, registry approval, canon, release, final media
  approval, and production lock separate?

### Downstream Route Safety

- Does PASS avoid auto-starting implementation?
- Does PASS avoid resuming 04/05/06/07/08?
- Does PASS avoid provider/API, renderer/ffmpeg/TTS, and media generation?

## Review Statuses

- `DRAFT_FOR_REVIEW`: decision packet is inspectable as a candidate only.
- `REVIEW_PASS_NOT_APPROVAL`: packet is technically complete enough to support
  a future decision, but no storage option is chosen and no approval is granted.
- `NEEDS_REVISION`: packet is incomplete, ambiguous, or leaks approval/storage
  assumptions.
- `BLOCKED_BY_HUMAN_DECISION`: packet cannot proceed because user/00/A組01 must
  choose or defer a storage/approval direction.

## Narrow Downstream Allowance

On `REVIEW_PASS_NOT_APPROVAL`, B組01/A組01/00 may:

- ask for the required decision;
- choose to defer the decision;
- assign another route-selected docs/control task.

PASS must not auto-start schema/runtime/SQLite/storage/import implementation.
PASS must not create files as registry records, create database tables, write
SQLite, import assets, approve assets, approve registry data, run provider/API,
run renderer/ffmpeg/TTS, generate media, or create production lock.

## Stop Conditions

Stop and return to the control lane if a future task requires:

- Kotlin/JVM source or CLI command changes;
- runtime/schema/migration work;
- direct SQLite writes;
- import/export implementation;
- sample JSON that can be mistaken for registry data;
- provider/API calls;
- web/server/open ports;
- renderer/ffmpeg/TTS;
- image/audio/video media generation;
- queryer implementation;
- story content generation;
- script packet generation;
- 04/05/06/07/08 restart;
- Phase 20 / Phase 21 restart;
- canon, asset approval, registry approval, release, final approval, final media
  approval, provider/media handoff, approval, or production lock.
