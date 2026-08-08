# WP-003A — Daping Coast Location Bible Foundation Handoff

Status: `READY_FOR_CODEX`

Parent route:

- `docs/CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1.md`
- `docs/CODEX_GPT_IMAGE_PRODUCTION_EXECUTION_PLAN_V1.md`
- `docs/work-packages/WP-002A_CODEX_GPT_IMAGE_HANDOFF_FOUNDATION.md`
- `docs/work-packages/WP-003_CHENXING_VISUAL_BIBLE_INVENTORY.md`

Accepted WP-003 evidence:

- Commit: `7cd3f82f49d05703c95c1b58e313a03d6967d6f4`
- Result: `WP003_SCHEMA_GAP_FOUND`
- Tier-1 blocker: Daping Coast has textual authority but no approved Location Bible images.

## 1. Model And Reasoning

Use the strongest currently available Codex coding model.

Reasoning effort: `HIGH`.

This package defines a new production file protocol and the first real location-foundation handoff. It crosses schema compatibility, source authority, location geometry, reference attachment, candidate-only approval, Git identity, future image intake, and the Shot 01–03 continuity boundary.

## 2. Controller Decision

The controller selects this architecture:

```text
separate preliminary Location Bible handoff
```

Do **not** weaken or misuse `shot_queue_v1`.

Reason:

- a Location Bible foundation task is not a story Shot;
- it may correctly contain zero visible characters;
- a master panorama or top-down plan must not require a fake character binding;
- story Shots require a locked Location Bible, while the Location Bible cannot require itself as an input;
- a distinct preliminary contract breaks that circular dependency without weakening normal Shot validation;
- later WP-004 continues to use the strict story-shot schemas.

Therefore:

- keep `schemas/production/shot_queue_v1.schema.json` unchanged;
- keep its `characters.minItems = 1` rule for real story Shots;
- add a bounded location-foundation protocol instead of turning `shotId` into a generic catch-all field;
- do not start Kotlin WP-002B.

## 3. Goal

Create and validate a provider-neutral file protocol that allows Codex to commit a real Daping Coast Location Bible candidate handoff for ChatGPT.

The handoff must support this route:

```text
formal Chenxing text authority + locked style/scale references
→ location-foundation tasks
→ Git Commit and exact handoff path
→ GPT reads one task at a time
→ GPT requests only task-relevant reference uploads
→ GPT generates Location Bible image candidates
→ user approves / rejects / requests variation
→ candidate files return to Codex
→ Codex computes SHA-256 and runs the existing image review/reference chain
→ accepted Location Bible references become inputs to WP-004 Shot 01–03
```

This package creates schemas and a real handoff. Codex does not generate images.

## 4. Required First Read

Read in this order:

1. `PROJECT_STATE.md`
2. `.codex/project_controller.md`
3. `docs/CURRENT_ROUTE_GATE_V1.md`
4. `docs/CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1.md`
5. `docs/CODEX_GPT_IMAGE_PRODUCTION_EXECUTION_PLAN_V1.md`
6. `docs/production/WP002A_EXISTING_IMAGE_PIPELINE_INVENTORY.md`
7. `docs/production/WP003_CHENXING_VISUAL_BIBLE_INVENTORY_REPORT.md`
8. `world/chenxing/production/visual_bible/chenxing_location_bible_gap_inventory_v1.json`
9. `world/chenxing/production/visual_bible/opening_shots_01_03_readiness_v1.json`
10. all six existing WP-002A schemas
11. `world/chenxing/formal/21_volume_one_opening_arc_v1.json`
12. `world/chenxing/formal/22_volume_one_opening_prologue_v1.json`
13. `world/chenxing/formal/23_character_visual_appearance_v1.json`
14. `world/chenxing/formal/25_lu_chenxing_opening_modern_costume_v1.json`
15. `world/chenxing/formal/26_volume_one_opening_storyboard_v1.json`
16. `world/chenxing/formal/27_opening_preview_visual_style_v1.json`
17. `world/chenxing/preview/opening_v1/DRIFT_NOTICE.json`
18. `AGENTS.md`

Use Repository evidence only. Do not browse or invent a real-world Daping Coast layout unless the package explicitly records an external user-supplied reference in a later revision.

## 5. Branch And Base

After fetching, create a clean branch from the latest remote default branch:

```text
feature/wp003a-daping-location-bible-handoff
```

Required start checks:

```powershell
git fetch --prune origin
git status --short
git rev-parse HEAD
git rev-parse origin/codex/mvp6-checkpoint-branching-spec
```

Start only from a clean worktree. Do not copy the old dirty worktree.

## 6. Required Outputs

Create exactly these primary artifacts:

```text
schemas/production/location_bible_handoff_v1.schema.json
schemas/production/location_bible_return_to_codex_v1.schema.json

samples/production_handoff/location_bible_handoff_v1.example.json
samples/production_handoff/location_bible_return_to_codex_v1.example.json

world/chenxing/production/location_bible_handoffs/daping_coast_v1/README.md
world/chenxing/production/location_bible_handoffs/daping_coast_v1/location_bible_handoff.json
world/chenxing/production/location_bible_handoffs/daping_coast_v1/reference_upload_checklist.json
world/chenxing/production/location_bible_handoffs/daping_coast_v1/location_bible_return_template.json

docs/production/WP003A_DAPING_LOCATION_BIBLE_HANDOFF_REPORT.md
```

At completion update only the current-package sections of:

```text
PLANS.md
PROJECT_STATE.md
```

Do not create any image file.

## 7. `LocationBibleHandoff` Contract

The new schema must be JSON Schema Draft 2020-12 and must require at least:

```text
schemaVersion
handoffId
status
createdAt
projectId
worldId
branch
sourceCommitSha
handoffCommitSha
handoffContentSha256
locationId
sceneIds
storyPhase
sourceArtifacts
foundationGenerationRule
referenceUploadChecklistFile
tasks
sharedSpatialRules
sharedNegativeConstraints
visibilityAndSecretBoundary
reviewContract
returnToCodexContract
validationSummary
```

### 7.1 Status values

At minimum:

```text
DRAFT
VALIDATED
READY_FOR_COMMIT
READY_FOR_GPT
REFERENCE_UPLOAD_REQUIRED
GENERATION_IN_PROGRESS
CANDIDATE_REVIEW_REQUIRED
CODEX_INTAKE_READY
LOCATION_REFERENCE_ACCEPTED
BLOCKED
```

A status is descriptive only. It cannot create approval, Canon, registry, or production lock.

### 7.2 Git identity

Use the WP-002A non-self-referential rule:

- `sourceCommitSha`: the Commit containing formal sources and WP-003 evidence;
- `handoffContentSha256`: canonical normalized JSON hash with self-referential hash and optional containing-Commit metadata set to `null` before hashing;
- `handoffCommitSha`: nullable, caller-supplied metadata after Commit;
- ChatGPT invocation supplies the containing Commit SHA and handoff path.

### 7.3 Foundation generation rule

The handoff must state explicitly:

```text
priorLocationReferenceRequired = false
outputIsFoundationCandidate = true
storyShotGenerationAllowed = false
userVisualApprovalRequired = true
codexHashAndReferenceIntakeRequired = true
```

This rule is the bounded exception that breaks the circular dependency:

- a Location Bible candidate may be generated from formal textual authority plus approved style/scale references;
- that candidate is not yet an approved location reference;
- no Shot 01–03 formal generation may proceed until the required Location Bible components are approved and returned through Codex intake.

### 7.4 Task record

Each task must support at least:

```text
taskId
componentType
phase
status
dependsOn
locationId
sceneIds
sourceArtifacts
textualAuthority
requiredReferenceIds
optionalReferenceIds
projectionOrCamera
aspectRatio
spatialAnchors
fixedGeometryRules
scaleEntities
weatherAndLighting
allowedCreativeFreedom
negativeConstraints
authorOnlyExclusions
expectedFileName
candidateOnly
reviewState
```

Allowed `componentType` values:

```text
MASTER_PANORAMA
TOP_DOWN_ZONE_PLAN
DIRECTIONAL_VIEW
ANCHOR_DETAIL
SCALE_REFERENCE
WEATHER_LIGHTING_REFERENCE
OPTIONAL_360_PANORAMA
```

Allowed task statuses:

```text
READY_FOR_GPT
REFERENCE_UPLOAD_REQUIRED
BLOCKED_BY_PRIOR_TASK_APPROVAL
CANDIDATE_GENERATED
USER_APPROVED_CANDIDATE
REJECTED_DRIFT
VARIATION_REQUESTED
CODEX_INTAKE_READY
```

No task requires a fake character. `scaleEntities` may be empty except for a real scale-reference task.

## 8. `LocationBibleReturnToCodex` Contract

The return schema must support exact candidate identity without using a fake `shotId`.

Required top-level fields:

```text
schemaVersion
handoffId
returnStatus
locationId
candidates
intakeBoundary
```

Each candidate must support:

```text
taskId
componentType
candidateId
expectedFileName
localFilePath
userDecision
userDecisionReason
driftFindings
sourceConversationNote
sourceFileSha256
intakeStatus
nextRequiredCliStages
```

Allowed user decisions:

```text
PENDING_USER_REVIEW
APPROVE_CANDIDATE
REJECT_DRIFT
REQUEST_VARIATION
CHANGE_VISUAL_DIRECTION
```

Use the existing standardized drift codes where applicable, including:

```text
SCENE_GEOMETRY_DRIFT
SCALE_DRIFT
PROP_DRIFT
WEATHER_OR_TIME_DRIFT
STYLE_DRIFT
SHOT_CONTINUITY_DRIFT
EXTRA_CHARACTER_OR_OBJECT
OTHER
```

The return contract must state:

- user approval applies to the exact candidate only;
- SHA-256 is required after Codex receives the file;
- existing External Result / Human Review / Candidate Reference stages still run;
- approval does not write Canon or SQLite;
- location-reference acceptance does not approve later story Shots or final media.

## 9. Real Daping Coast Handoff Tasks

The real handoff must include these tasks and dependencies.

### Phase 1 — spatial foundation

#### `daping-lb-01-master-panorama`

- component: `MASTER_PANORAMA`
- initial status: `READY_FOR_GPT`
- aspect ratio: `16:9`
- no visible characters required;
- formal source: opening arc/prologue/storyboard and WP-003 textual anchors;
- style: opening `cinematic_misty`, used only for atmosphere/rendering;
- establish rock platform, tide pools, unnatural seam area, sea-facing direction, safe/return route relationship, and plausible July 2026 coastal scale;
- must not invent fantasy architecture, ancient clothing, post-crossing structures, gold, specimen bottles, or hidden causal answers.

#### `daping-lb-02-top-down-zone-plan`

- component: `TOP_DOWN_ZONE_PLAN`
- initial status: `BLOCKED_BY_PRIOR_TASK_APPROVAL`
- dependency: approved candidate from task 01;
- aspect ratio: `4:3` or `1:1`, chosen consistently and documented;
- production diagram rather than cinematic illustration;
- define zones and relative orientation: sea, rock platform, tide pools, seam/detail zone, Shot 01 position, Shot 02/03 continuity area, safe/return route, later rising-water direction;
- no decorative room invention.

### Phase 2 — anchor and scale

#### `daping-lb-03-seam-tidepool-detail`

- component: `ANCHOR_DETAIL`
- initial status: `BLOCKED_BY_PRIOR_TASK_APPROVAL`
- dependencies: approved tasks 01 and 02;
- aspect ratio: `16:9` or `3:2`;
- establish the unnatural seam, artificial edge, adjacent tide-pool rock texture, hand-comparison scale, and orientation toward sea;
- no unreadable pseudo-text, machinery, doorway, portal glow, or supernatural effect.

#### `daping-lb-04-lu-aijiang-scale-reference`

- component: `SCALE_REFERENCE`
- initial status: `REFERENCE_UPLOAD_REQUIRED` or `BLOCKED_BY_PRIOR_TASK_APPROVAL` according to exact dependency state;
- requires Lu production body turnaround, opening costume, and Aijiang authoritative turnaround;
- use 174 cm Lu baseline and locked Aijiang build;
- the output is a spatial scale/reference candidate, not a story frame;
- face detail may be simplified, but body/costume/dog scale must not drift.

### Phase 3 — shot-facing directions

#### `daping-lb-05-shot01-facing-view`
#### `daping-lb-06-shot02-seam-facing-view`
#### `daping-lb-07-shot03-sea-reaction-view`

- component: `DIRECTIONAL_VIEW`
- initial status: `BLOCKED_BY_PRIOR_TASK_APPROVAL`;
- depend on approved master panorama, plan, detail, and scale components;
- preserve one shared geometry;
- these are empty or minimally marked location-reference views, not final story Shot images;
- no new room, coastline, prop, weather phase, or seam position may appear.

### Optional 360 rule

Do not include an `OPTIONAL_360_PANORAMA` task in the initial ready queue.

The real handoff may record it as deferred optional work only. It cannot replace the panorama, plan, directional views, anchors, detail, or scale reference.

## 10. Reference Upload Checklist

Reuse `schemas/production/reference_upload_checklist_v1.schema.json` when compatible.

The real checklist must be task-aware through handoff task bindings. It must not claim any file is already attached to ChatGPT.

Minimum references:

- opening cinematic-misty style reference for task 01;
- Lu body turnaround for task 04;
- Lu opening costume turnaround for task 04;
- Aijiang authoritative turnaround for task 04.

The six Lu face photos are **not required** for the initial empty-location panorama/plan tasks. Do not burden the user with all ten Shot-generation uploads before they are needed.

If the existing checklist schema cannot represent task-scoped necessity directly:

- keep each item’s global `required` value honest;
- bind task-specific requirements in `LocationBibleHandoff.tasks[].requiredReferenceIds`;
- document this convention in the report;
- do not modify the existing checklist schema unless a genuine validation impossibility is demonstrated.

## 11. Prompt And Secret Boundaries

Prompts must use:

- public/production-visible setting facts only;
- exact location anchors from WP-003 and formal opening sources;
- phase-appropriate 2026 Taiwan coastal material culture;
- style references only as rendering guidance;
- no author-only cause of the ruin, crossing mechanics, future plot answers, specimen/gold discoveries, or post-crossing imagery.

A Location Bible task may define allowed creative freedom only for non-causal visual details such as ordinary rock variation, puddle shape, worn concrete texture, cloud pattern, and neutral vegetation, provided fixed anchors remain stable.

## 12. Real Handoff Readiness

The actual `location_bible_handoff.json` may be marked `READY_FOR_GPT` only when:

- task 01 is structurally complete;
- source paths and hashes pass;
- task 01’s required style reference exists and is listed for upload;
- no task claims an image has already been generated or approved;
- later tasks are correctly blocked by prior approval or upload requirements;
- no private absolute path appears;
- normalized handoff hash passes;
- all JSON parses;
- any unavailable JSON Schema meta-validation is recorded honestly.

## 13. ChatGPT Invocation Contract

The package README must give the user this exact pattern:

```text
讀取 z72124223/ai-novel-engine，
Commit <handoff-commit-sha>，
交接包 world/chenxing/production/location_bible_handoffs/daping_coast_v1/location_bible_handoff.json，
依 location-bible-handoff-v1 執行 daping-lb-01-master-panorama。
```

GPT must first inspect the exact Commit and task. If the style reference is not attached in the current conversation, it returns `REFERENCE_UPLOAD_REQUIRED` with the exact Repository path.

Do not instruct GPT to execute all seven tasks at once.

## 14. Allowed Changes

Allowed paths:

```text
schemas/production/location_bible_handoff_v1.schema.json
schemas/production/location_bible_return_to_codex_v1.schema.json
samples/production_handoff/location_bible_handoff_v1.example.json
samples/production_handoff/location_bible_return_to_codex_v1.example.json
world/chenxing/production/location_bible_handoffs/daping_coast_v1/**
docs/production/WP003A_DAPING_LOCATION_BIBLE_HANDOFF_REPORT.md
PLANS.md
PROJECT_STATE.md
```

No other paths may change without stopping for a new instruction.

## 15. Forbidden Changes

Do not modify:

```text
app/src/main/**
app/src/test/**
app/src/main/resources/migrations/**
schemas/production/shot_queue_v1.schema.json
other existing WP-002A schemas or examples
world/chenxing/formal/**
world/chenxing/assets/**
world/chenxing/preview/**
world/chenxing/production/visual_bible/**
memory/**
tools/**
package.json
package-lock.json
```

Do not:

- generate or edit images;
- install a dependency;
- call an image/video provider or API;
- make a fake story Shot or fake character binding;
- create the final Shot 01–03 handoff;
- approve a location image;
- change Canon or Runtime;
- start WP-004, WP-005, WP-006, WP-002B, or WP-007;
- use `git add .`, `git add -A`, `git commit -a`, or force push.

## 16. Verification

Required:

1. All new JSON and schemas parse.
2. If a compatible JSON Schema validator is preinstalled, validate examples and the real handoff; otherwise record `SCHEMA_META_VALIDATION_NOT_AVAILABLE` without installing a dependency.
3. All local `$ref` pointers resolve.
4. All schema regex patterns compile.
5. Every Repository path is present or explicitly documented as a future output path.
6. Every bound present file has SHA-256 and byte size where the contract requires it.
7. Canonical normalized handoff SHA-256 matches.
8. Task dependencies form an acyclic graph.
9. Exactly one initial task is `READY_FOR_GPT`: `daping-lb-01-master-panorama`.
10. No task contains a fake character.
11. No actual image/approval/Canon/final-media/production-lock claim exists.
12. No private absolute path exists.
13. `git diff --check` passes.
14. `git diff --name-only` contains only allowed paths.

Gradle is not required because Kotlin changes are forbidden.

## 17. Completion Decision

The report must end with one primary result:

```text
WP003A_READY_FOR_GPT_LOCATION_TASK_01
WP003A_BLOCKED_BY_SCHEMA_VALIDATION
WP003A_BLOCKED_BY_SOURCE_CONFLICT
WP003A_BLOCKED_BY_MISSING_STYLE_REFERENCE
WP003A_NEEDS_CONTROLLER_DECISION
```

Do not label WP-004 ready. Even a successful WP-003A only enables the first GPT Location Bible task.

## 18. Commit And Push

Recommended Commit:

```text
docs(production): add Daping Coast location Bible handoff
```

Push only to:

```text
origin/feature/wp003a-daping-location-bible-handoff
```

Do not push directly to the default branch.

## 19. Completion Report

Report:

```text
Base SHA:
Branch:
Commit SHA:
Push result:
New schemas:
Example-only files:
Real handoff path:
Real return template path:
Initial READY_FOR_GPT task:
Blocked dependent tasks:
Required initial ChatGPT uploads:
Task dependency validation:
Normalized handoff SHA-256:
Schema validation result:
JSON/path/hash verification:
git diff --check:
Primary result:
Modified files:
Kotlin/Test/Migration/Canon/Image changes:
Original unrelated worktree status:
```

Stop after Push and wait for controller review. Do not start image generation or WP-004 automatically.
