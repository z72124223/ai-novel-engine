# WP-002A — Codex/GPT Image Handoff Foundation

Status: `READY_FOR_CODEX`

Parent blueprint:

- `docs/CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1.md`
- `docs/CODEX_GPT_IMAGE_PRODUCTION_EXECUTION_PLAN_V1.md`

## 1. Model And Reasoning

Use the strongest currently available Codex coding model.

Reasoning effort: `HIGH`.

This package spans file-protocol design, existing CLI inventory, security boundaries, Git identity, JSON Schema, examples, and future Kotlin compatibility. Do not use low reasoning.

## 2. Goal

Create the first executable file-protocol foundation for this route:

```text
Codex runs engine/world data
→ Script / Shot package is committed
→ Codex creates a validated GPT image handoff
→ User gives ChatGPT Repository + Commit + Handoff path
→ GPT reads the package and either requests reference uploads or generates image candidates
→ Approved candidates return to Codex Intake
```

This Work Package defines and validates the files. It does **not** generate images, call external APIs, modify Runtime, or implement a Kotlin compiler yet.

## 3. Required First Read

Read in this order:

1. `PROJECT_STATE.md`
2. `.codex/project_controller.md`
3. `docs/CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1.md`
4. `docs/CODEX_GPT_IMAGE_PRODUCTION_EXECUTION_PLAN_V1.md`
5. This file
6. `AGENTS.md`
7. `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`
8. `docs/CODEX_GROK_BUILD_HANDOFF_AND_IMAGE_PRODUCTION_LANE_V0.md`
9. `world/chenxing/formal/23_character_visual_appearance_v1.json`
10. `world/chenxing/formal/25_lu_chenxing_opening_modern_costume_v1.json`
11. `world/chenxing/formal/26_volume_one_opening_storyboard_v1.json`
12. `world/chenxing/formal/27_opening_preview_visual_style_v1.json`
13. `world/chenxing/formal/28_volume_one_ruin_interior_storyboard_v1.json`

Then inspect existing image candidate, provider handoff, external result, review, reference, registry, and acceptance Models/Services/CLI/Tests before designing new shapes.

## 4. Repository Branch

Create a clean branch from the latest remote default branch:

```text
feature/wp002a-gpt-image-handoff-foundation
```

Do not work in a dirty tree. Do not copy an old dirty worktree into the clean branch.

## 5. Existing Pipeline Inventory

Before adding schemas, document the existing chain and identify reusable fields:

```text
Storyboard Review
→ Asset Candidate
→ Image Candidate Preflight
→ Image Candidate Brief
→ Prompt Preparation
→ Provider Handoff Preflight / Decision / Packet
→ External Result Intake
→ Result Human Review
→ Image Candidate Reference Packet
→ Candidate Reference Registry
→ Candidate Reference Acceptance
```

Inspect actual code; do not infer only from command names.

Create:

```text
docs/production/WP002A_EXISTING_IMAGE_PIPELINE_INVENTORY.md
```

For each stage record:

- CLI command
- Model types
- Service types
- Input files
- Output files
- Status/verdict fields
- Hash/source identity fields
- Human approval boundary
- Canon/SQLite write behavior
- Reusable fields for ChatGPT handoff
- Missing fields

Explicitly confirm that the new ChatGPT handoff does not bypass the existing provider/external-result/human-review/reference chain.

## 6. File Layout To Establish

Preferred paths, unless an existing stronger Repository convention is found:

```text
schemas/production/
├── chatgpt_image_handoff_v1.schema.json
├── reference_upload_checklist_v1.schema.json
├── shot_queue_v1.schema.json
├── prompt_manifest_v1.schema.json
├── image_review_log_v1.schema.json
└── image_return_to_codex_v1.schema.json

samples/production_handoff/
├── chatgpt_image_handoff_v1.example.json
├── reference_upload_checklist_v1.example.json
├── shot_queue_v1.example.json
├── prompt_manifest_v1.example.json
├── image_review_log_v1.example.json
└── image_return_to_codex_v1.example.json

world/chenxing/production/
└── README.md
```

If an existing schema/sample convention is clearly established, use it and explain the mapping in the inventory report. Do not create two competing conventions.

Do not create actual Shot 01–03 handoff instances in WP-002A; those belong to WP-004.

## 7. `ChatGptImageHandoff` Minimum Contract

The schema must support at least:

```text
schemaVersion
handoffId
status
createdAt
projectId
worldId
branch
sourceCommitSha
handoffContentSha256
sourceArtifacts
shotScope
referenceBindings
referenceUploadChecklistFile
shotQueueFile
promptManifestFile
locationBibleRequirements
continuityRules
negativeConstraints
outputContract
reviewContract
returnToCodexContract
visibilityAndSecretBoundary
validationSummary
```

### 7.1 Status Values

At minimum:

```text
DRAFT
VALIDATED
READY_FOR_COMMIT
READY_FOR_GPT
REFERENCE_UPLOAD_REQUIRED
GENERATION_READY
CANDIDATE_GENERATED
USER_APPROVED
REJECTED_DRIFT
VARIATION_REQUESTED
CODEX_INTAKE_READY
INTAKE_VALIDATED
REFERENCE_ACCEPTED
VIDEO_HANDOFF_READY
```

Schema status is descriptive. It must not create approval by itself.

### 7.2 Git Identity

Do not require a package to embed the Commit SHA that contains itself.

Required fields:

- `sourceCommitSha`: commit containing source script/canon/reference state.
- `handoffContentSha256`: hash of normalized handoff content.
- `branch`: branch expected by caller.

The caller supplies:

- `handoffCommitSha`
- `handoffPath`

when asking GPT to read the package.

The schema may allow `handoffCommitSha` only as externally filled metadata or `null`; it must not be required for the commit that creates the file.

## 8. Reference Upload Checklist Contract

Each item must support:

```text
referenceId
repositoryPath
assetRole
entityId
version
required
requirementCategory
expectedSha256
whyNeeded
canProceedWithoutIt
degradedMode
uploadStatus
```

Requirement categories:

```text
LIKENESS
BODY
COSTUME
ANIMAL_APPEARANCE
LOCATION_GEOMETRY
PROP_GEOMETRY
STYLE_ONLY
CONTINUITY_FRAME
OPTIONAL_CONTEXT
```

If required likeness, costume, animal appearance, or location geometry references are unavailable in the current ChatGPT conversation, the formal output is `REFERENCE_UPLOAD_REQUIRED`, not text-only generation.

## 9. Shot Queue Contract

Each shot must support:

```text
shotId
sequenceNumber
sourceArtifact
sceneId
locationId
locationZone
characters
props
camera
composition
action
emotion
weatherAndLighting
continuityFrom
continuityTo
firstFrameRef
lastFrameRef
requiredReferences
forbiddenElements
outputFileName
reviewStatus
```

Each character entry must identify:

```text
characterId
appearanceVersion
costumeVersion
position
scale
pose
emotion
visibleProps
```

Names alone are invalid references.

## 10. Prompt Manifest Contract

Each prompt item must separate:

- factual scene content
- visual style
- character/reference bindings
- camera/composition
- allowed creative freedom
- negative constraints
- author-only exclusions
- output aspect ratio and use

Prompt text must not contain:

- author-only secrets not visible in the shot
- later plot answers
- unapproved appearance details
- false approval/canon claims
- provider API credentials or remote job data

## 11. Image Review Log Contract

Review states:

```text
PENDING_USER_REVIEW
APPROVE_CANDIDATE
REJECT_DRIFT
REQUEST_VARIATION
CHANGE_VISUAL_DIRECTION
```

Drift codes must include:

```text
FACE_DRIFT
BODY_DRIFT
AGE_DRIFT
HAIR_OR_GLASSES_DRIFT
COSTUME_DRIFT
AIJIANG_MARKING_DRIFT
SCALE_DRIFT
SCENE_GEOMETRY_DRIFT
PROP_DRIFT
WEATHER_OR_TIME_DRIFT
STYLE_DRIFT
SHOT_CONTINUITY_DRIFT
EXTRA_CHARACTER_OR_OBJECT
OTHER
```

An approval entry must identify the exact candidate file/hash or a placeholder indicating that Hash will be completed by Codex Intake.

## 12. Return-To-Codex Contract

Support:

```text
handoffId
shotId
candidateId
localFilePath
expectedFileName
userDecision
userDecisionReason
sourceConversationNote
sourceFileSha256
intakeStatus
nextRequiredCliStages
```

`sourceFileSha256` may be `null` before Codex receives the file. It becomes required before External Result / Candidate Reference acceptance.

## 13. Chenxing Production README

Create `world/chenxing/production/README.md` documenting:

- this directory stores reproducible text/JSON production handoffs
- it does not replace `world/chenxing/formal/`
- it does not replace SQLite Runtime
- file existence is not approval
- actual generated images remain candidate-only
- large video files and rejected bulk renders are not committed by default
- locked turnaround/costume/style assets remain in existing paths
- current first Pilot is opening shots 01–03, but no actual handoff is created in this package

## 14. Validation

All JSON Schemas and examples must parse as valid JSON.

Validate with existing Repository tools when available; otherwise use standard local tools without adding dependencies.

Minimum checks:

```text
- every schema is valid JSON
- every example is valid JSON
- every example declares the correct schemaVersion
- every referenced example file exists
- every source repository path used by examples exists, or is explicitly marked placeholder
- no example claims APPROVED_REFERENCE, Canon, Final Media, Release, or Production Lock
- no actual private/local absolute path is committed in examples
- git diff --check passes
```

Add a small no-dependency validation script only if Repository conventions support it and it is clearly useful. Otherwise document reproducible commands.

## 15. No Kotlin In This Package

Do not modify:

```text
app/src/main/**
app/src/test/**
app/src/main/resources/migrations/**
```

Do not implement a new CLI, Model, Service, Repository method, DB table, API integration, renderer, TTS, FFmpeg, Web UI, or Android UI.

Record potential WP-002B Kotlin integration points in the inventory report only.

## 16. Allowed Files

Allowed additions/changes:

```text
docs/production/WP002A_EXISTING_IMAGE_PIPELINE_INVENTORY.md
schemas/production/**
samples/production_handoff/**
world/chenxing/production/README.md
README.md                         # only a concise active-route pointer if needed
docs/HANDOFF.md                   # only a concise current override/pointer if needed
PROJECT_STATE.md                  # only completion/next-step update
PLANS.md                          # only current work-package status, no historical rewrite
```

Do not edit Canon JSON, locked assets, storyboards, Kotlin, tests, migrations, Memory, or candidate images.

## 17. Tests And Completion Evidence

Run:

```text
git diff --check
```

Run JSON parse validation for every added `.json` file.

If a JSON Schema validator already exists without new dependencies, validate all examples against their schemas. If not, record `SCHEMA_META_VALIDATION_NOT_AVAILABLE` rather than falsely claiming Schema validation.

Completion report must include:

```text
Branch
Base SHA
Changed files
Existing pipeline stages inventoried
Schemas added
Examples added
JSON parse result
Schema validation result
Path existence result
Secret/approval boundary review
Git diff check
Commit SHA
Push result
Known gaps for WP-003/WP-004
Whether WP-002B is currently necessary
```

## 18. Commit And Push

Use an isolated commit, suggested message:

```text
docs(production): establish Codex GPT image handoff foundation
```

Push only the feature branch:

```text
feature/wp002a-gpt-image-handoff-foundation
```

Do not force push. Do not push directly to the default branch.

## 19. Completion Decision

At completion classify:

```text
WP002A_PASS
WP002A_PASS_WITH_SCHEMA_VALIDATOR_GAP
WP002A_NEEDS_REVISION
WP002A_BLOCKED
```

After a PASS, the next package is `WP-003 Chenxing Visual Bible Inventory`, not automatic Kotlin implementation.

`WP-002B Handoff Compiler And Validator` remains conditional on evidence from the first real handoff Pilot.
