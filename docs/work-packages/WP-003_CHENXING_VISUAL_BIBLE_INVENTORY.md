# WP-003 — Chenxing Visual Bible Inventory

Status: `READY_FOR_CODEX`

Parent route:

- `docs/CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1.md`
- `docs/CODEX_GPT_IMAGE_PRODUCTION_EXECUTION_PLAN_V1.md`
- `docs/work-packages/WP-002A_CODEX_GPT_IMAGE_HANDOFF_FOUNDATION.md`

WP-002A accepted foundation commit:

- `e02700ecf370e53f289f5a77364119639ac4bb5f`

## 1. Model And Reasoning

Use the strongest currently available Codex coding model.

Reasoning effort: `HIGH`.

This package crosses author-locked visual authority, Repository file integrity, superseded/rejected assets, location geometry, prompt visibility, reference-upload requirements, and the first real image Pilot. Do not use low reasoning.

## 2. Goal

Create a verified inventory of the Chenxing visual Bible needed by the first GPT image Pilot.

The package must answer, using Repository evidence rather than guesses:

1. Which Lu Chenxing references are author-locked and usable for production?
2. Which Aijiang reference is authoritative?
3. Which opening costume and style references are current, and which are historical or phase-specific?
4. Which Daping Coast and ruin location-geometry assets already exist?
5. Which location assets are only text descriptions, missing, optional, superseded, or rejected drift?
6. Which props and continuity anchors are required for opening Shot 01–03?
7. Which exact files must later be attached to the ChatGPT conversation?
8. Can the WP-002A file protocol represent the real Pilot without unsafe placeholders or schema misuse?
9. Is WP-004 ready to create the first real handoff, or must a bounded location/schema gap be resolved first?

WP-003 inventories and classifies. It does not generate images, create the real Shot 01–03 handoff, approve an asset, edit Canon, or modify Runtime.

## 3. Required First Read

Read in this order:

1. `PROJECT_STATE.md`
2. `.codex/project_controller.md`
3. `docs/CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1.md`
4. `docs/CODEX_GPT_IMAGE_PRODUCTION_EXECUTION_PLAN_V1.md`
5. `docs/work-packages/WP-002A_CODEX_GPT_IMAGE_HANDOFF_FOUNDATION.md`
6. `docs/production/WP002A_EXISTING_IMAGE_PIPELINE_INVENTORY.md`
7. all six files under `schemas/production/`
8. `world/chenxing/production/README.md`
9. `AGENTS.md`
10. `world/chenxing/formal/23_character_visual_appearance_v1.json`
11. `world/chenxing/formal/24_series_cover_v1.json`
12. `world/chenxing/formal/25_lu_chenxing_opening_modern_costume_v1.json`
13. `world/chenxing/formal/26_volume_one_opening_storyboard_v1.json`
14. `world/chenxing/formal/27_opening_preview_visual_style_v1.json`
15. `world/chenxing/formal/28_volume_one_ruin_interior_storyboard_v1.json`
16. `world/chenxing/formal/32_crossing_experience_v1.json`
17. `world/chenxing/formal/33_ruin_interior_discoveries_v1.json`
18. `world/chenxing/formal/34_spatial_pocket_two_doors_v1.json`
19. `world/chenxing/preview/opening_v1/DRIFT_NOTICE.json`

Then inspect the actual files and directories referenced by those documents. Do not treat a path reference as proof that the file exists, is visually correct, is current, or is approved.

## 4. Branch And Base

Create a clean branch from the latest remote default branch after fetching:

```text
feature/wp003-chenxing-visual-bible-inventory
```

Required start checks:

```powershell
git fetch --prune origin
git status --short
git rev-parse HEAD
git rev-parse origin/codex/mvp6-checkpoint-branching-spec
```

Start only from a clean worktree. Do not copy the old dirty worktree into this branch.

## 5. Evidence Discipline

WP-003 must keep these evidence types separate.

### 5.1 Formal authority evidence

A formal JSON document may declare an asset `AUTHOR_LOCKED`, production-authoritative, superseded, phase-specific, or rejected. Record the exact source file and field.

### 5.2 File-integrity evidence

File existence, media type, byte size, SHA-256, and readable dimensions prove only that a file is present and identifiable. They do not prove likeness, quality, Canon, or human visual approval.

### 5.3 Visual approval evidence

Codex must not claim that a face, costume, dog marking, style, or location image is visually correct merely because it can read the file. Use the existing author-lock declarations and user decisions as authority. Anything lacking such evidence remains review-required.

### 5.4 ChatGPT attachment evidence

A Repository path does not prove the reference is attached in the active ChatGPT conversation. WP-003 must prepare upload requirements, but every actual upload status remains `NOT_AVAILABLE_IN_CONVERSATION` or `UPLOAD_REQUIRED` until the later GPT session confirms otherwise.

## 6. Classification Values

Every inventoried visual record must use one primary authority status:

```text
AUTHOR_LOCKED_REFERENCE
APPROVED_REFERENCE
CANDIDATE_ONLY
STYLE_REFERENCE_ONLY
TEXTUAL_AUTHORITY_ONLY
SUPERSEDED_HISTORICAL
REJECTED_DRIFT
MISSING_REFERENCED_FILE
UNKNOWN_REVIEW_REQUIRED
```

Every physical file record must use one integrity status:

```text
EXISTS_HASHED
EXISTS_UNHASHED
MISSING
NOT_APPLICABLE
```

Every location component must use one readiness status:

```text
LOCKED_AND_PRESENT
PRESENT_CANDIDATE_ONLY
TEXT_ONLY
MISSING_REQUIRED
OPTIONAL_NOT_PRESENT
REJECTED_DRIFT
NOT_REQUIRED_FOR_SCOPE
```

Do not invent stronger labels such as `FINAL`, `CANON_IMAGE`, `PRODUCTION_LOCK`, or `GPT_UPLOADED`.

## 7. Required Outputs

Create exactly these primary artifacts:

```text
docs/production/WP003_CHENXING_VISUAL_BIBLE_INVENTORY_REPORT.md
world/chenxing/production/visual_bible/README.md
world/chenxing/production/visual_bible/chenxing_visual_asset_inventory_v1.json
world/chenxing/production/visual_bible/chenxing_location_bible_gap_inventory_v1.json
world/chenxing/production/visual_bible/opening_shots_01_03_readiness_v1.json
```

At completion, update only the current WP-003 section of:

```text
PLANS.md
PROJECT_STATE.md
```

The `PROJECT_STATE.md` update may record `WP-003_COMPLETED_AWAITING_CONTROLLER_REVIEW`; it must not activate WP-004 by itself.

Do not create actual `chatgpt_image_handoff.json`, `shot_queue.json`, or real Shot 01–03 prompt instances in WP-003. Those belong to WP-004.

## 8. Visual Asset Inventory Contract

`chenxing_visual_asset_inventory_v1.json` must include at least:

```text
schemaVersion
inventoryId
status
createdAt
worldId
sourceCommitSha
scope
characters
costumes
styles
props
rejectedOrSupersededEvidence
integritySummary
warnings
```

### 8.1 Asset record minimum fields

Each image, file group, or text-only authority record must support:

```text
recordId
entityId
assetRole
repositoryPath | repositoryPaths
formalAuthoritySources
authorityStatus
integrityStatus
version
storyPhase
mediaType
byteSize
sha256
width
height
aspectRatio
lockedTraits
forbiddenDrift
allowedUsage
forbiddenUsage
requiredForPilot
requirementCategory
chatGptUploadPolicy
supersedes
supersededBy
notes
```

Use Repository-relative paths only. Do not copy private `C:\...` source paths from formal JSON into new production artifacts.

If width/height cannot be read with already available local tools, record `null` and the exact reason. Do not install a package solely for dimensions.

### 8.2 Required character and animal coverage

Inventory at minimum:

- Lu Chenxing production body/likeness turnaround.
- Lu Chenxing face reference and `photo_set_123123` members.
- Any lean/opening-stocky/cinematic turnaround paths referenced by formal files.
- Aijiang production turnaround.
- Aijiang superseded historical concept sheet.
- Any missing or duplicated character reference paths.

For Lu Chenxing, record the locked opening traits without rewriting them:

- age 36;
- 174 cm / 80 kg opening baseline;
- stocky/slightly overweight opening body;
- buzz cut;
- glasses required;
- no facial hair;
- same face/bone structure across weight change.

For Aijiang, record the authoritative appearance fields and the no-collar/no-harness default policy.

### 8.3 Required costume coverage

Inventory the 2026 opening costume and all directly referenced production files:

- black tank undershirt;
- charcoal short-sleeve utility jacket;
- dark outdoor shorts;
- hiking boots;
- black crossbody bag;
- fixed glasses/earring/body traits;
- forbidden ancient/cultivation/dark-fantasy opening costume substitutions.

Record phase restrictions. Cover/post-crossing visual clothing must not be treated as the 2026 opening costume.

### 8.4 Required style coverage

Inventory:

- cinematic misty reference;
- black-and-white storyboard reference;
- dark-rain teal cinematic reference;
- formal cover reference where relevant;
- any candidate-v2 style files;
- explicit author warnings that style-reference characters are not likeness or costume authority.

Each style record must state the story phases in which it is permitted.

### 8.5 Required prop coverage for Shot 01–03

At minimum determine whether visual references exist for:

- phone;
- tide notes;
- glasses;
- crossbody bag;
- outdoor clothing/boots as visible costume elements;
- Daping Coast rock platform and the unnatural seam/edge used by Shot 02–03.

A story mention is not a prop image. Classify text-only anchors separately.

## 9. Location Bible Gap Inventory Contract

`chenxing_location_bible_gap_inventory_v1.json` must include:

```text
schemaVersion
inventoryId
status
sourceCommitSha
locations
pilotBlockingSummary
recommendedProductionOrder
schemaCompatibilityFindings
warnings
```

### 9.1 Location record minimum fields

Each location record must support:

```text
locationId
sceneIds
storyPhase
sourceArtifacts
textualVisualAnchors
masterPanorama
topDownPlan
directionalViews
spatialAnchorTable
detailViews
scaleReference
weatherAndLightingVariants
optional360Panorama
existingImageEvidence
rejectedDriftEvidence
readinessStatus
blockingGaps
recommendedNextAction
```

### 9.2 Required locations and priority

Inventory in this order:

#### Tier 1 — first Pilot

- `loc-daping-coast`
- `scene-daping-coast-opening`

Required for Shot 01–03:

- master panorama;
- top-down plan;
- shot-facing direction views;
- tide-pool / rock-platform / seam spatial anchors;
- Lu Chenxing / Aijiang scale reference;
- July 2026 coastal weather and lighting state.

#### Tier 2 — opening continuation

- coast retreat and route to `scene-hillside-ruin-retreat` for Shot 04–09.

#### Tier 3 — ruin interior

- ruin threshold;
- first shelter;
- service corridor;
- dead room;
- grey bottle storage;
- gold cache;
- any spatial-pocket/two-door geometry required by formal sources.

WP-003 does not produce these images. It identifies what exists and what must later be generated or supplied.

### 9.3 360-degree rule

Record a 360 panorama only as optional supplementary evidence. It cannot replace:

```text
master panorama + top-down plan + directional views + spatial anchors + scale reference
```

If a 360 asset exists but has seam/pole/geometry drift or lacks approval, classify it accordingly.

## 10. Opening Shot 01–03 Readiness Contract

`opening_shots_01_03_readiness_v1.json` must include one record for each source shot:

```text
shot-01-tide-check
shot-02-strange-find
shot-03-overfocus
```

Each record must support:

```text
shotId
sourceArtifact
sceneId
locationId
requiredCharacterReferences
requiredCostumeReferences
requiredAnimalReferences
requiredStyleReferences
requiredLocationReferences
requiredPropReferences
continuityDependencies
missingBindings
referenceUploadRequirements
schemaCompatibility
wp004Disposition
notes
```

Allowed `wp004Disposition` values:

```text
READY_FOR_WP004_HANDOFF
READY_WITH_REFERENCE_UPLOAD_REQUIRED
BLOCKED_BY_LOCATION_BIBLE
BLOCKED_BY_MISSING_LOCKED_ASSET
SCHEMA_ADJUSTMENT_REQUIRED
NEEDS_USER_VISUAL_DECISION
```

Do not mark a shot ready merely because its storyboard text exists.

## 11. WP-002A Schema Compatibility Check

Test the real inventory against the shape of the six WP-002A schemas without creating a real handoff.

At minimum answer:

1. Can a pure `LOCATION_BIBLE_CANDIDATE` task be represented without inventing a character binding?
2. Does `shot_queue_v1` require at least one character even when a location-only base image is needed?
3. Can location-bible generation be represented as a pre-shot task, or does WP-004 need a bounded schema amendment or separate preliminary handoff?
4. Can every required reference carry an exact Repository path, version, role, and SHA-256?
5. Can missing conversation attachments be represented honestly as `REFERENCE_UPLOAD_REQUIRED`?
6. Does any return/intake field require an absolute local path that the current Repository-relative schema cannot express?

Possible finding values:

```text
COMPATIBLE_AS_IS
COMPATIBLE_WITH_DOCUMENTED_CONVENTION
SCHEMA_ADJUSTMENT_REQUIRED_BEFORE_WP004
DEFER_TO_POST_PILOT
```

WP-003 records findings only. It must not modify the WP-002A schemas.

## 12. Existing Rejected Drift

The ten opening preview frames under the rejected preview directory must remain rejected evidence.

Record:

- `world/chenxing/preview/opening_v1/DRIFT_NOTICE.json` as the decision authority;
- the rejected directory as evidence, not as a formal reference source;
- the failure reason: text-only generation without locked turnaround/costume/animal references;
- the rule that those frames cannot seed a future formal shot unless the user explicitly reverses the decision.

Do not copy the rejected frames into `production/visual_bible/`.

## 13. ChatGPT Upload Requirement Preparation

WP-003 must produce an exact future upload-source list inside the readiness JSON, but it must not claim anything is uploaded.

For each required visual file, record:

```text
referenceId
repositoryPath
assetRole
entityId
version
expectedSha256
requirementCategory
requiredForFormalGeneration
canProceedWithoutIt
degradedMode
whyNeeded
currentConversationStatus
```

`currentConversationStatus` must be one of:

```text
NOT_CHECKED
NOT_AVAILABLE_IN_CONVERSATION
UPLOAD_REQUIRED
```

Do not use `UPLOADED_VERIFIED` in WP-003.

## 14. Allowed Changes

Allowed primary paths:

```text
docs/production/WP003_CHENXING_VISUAL_BIBLE_INVENTORY_REPORT.md
world/chenxing/production/visual_bible/**
PLANS.md
PROJECT_STATE.md
```

No other path may be changed unless the package stops and receives a new instruction.

## 15. Forbidden Changes

Do not modify:

```text
app/src/main/**
app/src/test/**
app/src/main/resources/migrations/**
schemas/production/**
samples/production_handoff/**
world/chenxing/formal/**
world/chenxing/assets/**
world/chenxing/preview/**
memory/**
tools/**
package.json
package-lock.json
```

Do not:

- generate, edit, crop, convert, compress, rename, move, or delete an image;
- create a real Shot 01–03 handoff;
- call an external image/video provider or API;
- install a new dependency;
- visually approve an asset on behalf of the user;
- change `AUTHOR_LOCKED`, Canon, registry, candidate, or approval status;
- treat file existence as visual approval;
- copy private absolute source paths into new output;
- start WP-004, WP-005, WP-006, WP-002B, or WP-007;
- use `git add .`, `git add -A`, `git commit -a`, or any force push.

## 16. Verification

Required checks:

1. All new JSON parses with an installed standard parser.
2. Every Repository path in the inventory is either verified present or explicitly marked missing.
3. Every present bound image has SHA-256 and byte size.
4. Image dimensions are recorded when safely readable without new dependencies.
5. Every authority status cites at least one exact Repository evidence path.
6. Rejected/superseded files are not listed as formal generation references.
7. No private absolute path appears in new files.
8. No `APPROVED`, `FINAL`, `CANON_IMAGE`, `PRODUCTION_LOCK`, or `UPLOADED_VERIFIED` claim is introduced without existing evidence.
9. `git diff --check` passes.
10. `git diff --name-only` contains only allowed paths.

Because this package is docs/data inventory only, Gradle is not required unless Codex accidentally touches code, which is forbidden.

## 17. Completion Decision

The report must end with one primary result:

```text
WP003_READY_FOR_WP004
WP003_READY_FOR_WP004_WITH_REFERENCE_UPLOAD_REQUIRED
WP003_BLOCKED_BY_LOCATION_BIBLE
WP003_BLOCKED_BY_MISSING_FORMAL_REFERENCE
WP003_SCHEMA_GAP_FOUND
```

A schema gap does not authorize WP-002B. It identifies a bounded decision for the controller.

## 18. Commit And Push

Stage only allowed paths. Verify staged scope before committing.

Recommended commit:

```text
docs(production): inventory Chenxing visual bible and Pilot gaps
```

Push only to:

```text
origin/feature/wp003-chenxing-visual-bible-inventory
```

Do not push directly to the default branch.

## 19. Completion Report To Controller

Report exactly:

```text
Base SHA:
Branch:
Commit SHA:
Push result:
Author-locked character references found:
Author-locked costume references found:
Authoritative Aijiang reference:
Style references by phase:
Rejected/superseded assets:
Daping Coast Location Bible readiness:
Ruin Location Bible readiness:
Shot 01 disposition:
Shot 02 disposition:
Shot 03 disposition:
Missing required files:
Future ChatGPT upload file count:
WP-002A schema compatibility finding:
Primary WP-003 result:
JSON/path/hash verification:
git diff --check:
Modified files:
Kotlin/Test/Migration/Canon/Image changes:
Original unrelated worktree status:
```

Stop after push and wait for controller review. Do not start WP-004 automatically.
