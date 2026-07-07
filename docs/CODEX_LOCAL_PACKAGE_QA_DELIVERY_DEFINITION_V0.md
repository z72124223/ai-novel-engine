# Codex Local Package QA Delivery Definition V0

Status: `DRAFT_FOR_REVIEW`

Route label: `local_package_qa_delivery_definition_v0_docs_control`

This document defines a docs/control `LocalPackageQaDeliveryDefinition`. It is
not package generation, not renderer execution, not ffmpeg/TTS execution, not
image/audio/video media generation, not artifact creation, not a CLI command,
not runtime, not schema or migration, not provider/API, not release approval,
not final media readiness, not final approval, and not production lock.

## Purpose

`LocalPackageQaDeliveryDefinition` describes what a future reviewable local
delivery package could contain and what QA surfaces must be checked before any
later delivery-oriented implementation or execution task is considered.

The intended bridge is:

```text
ScriptReadyScenePacket evidence
-> StoryboardCameraPacket planning
-> AssetLifecycleRegistryDecisionPacket / asset route evidence
-> VoiceTimelineSubtitleSfxContract
-> LocalRendererExecutionReadinessChecklist
-> local package / QA / delivery definition
-> technical review only
-> future explicit route decision
```

This definition does not auto-start package generation, renderer/ffmpeg/TTS,
provider/API, schema/runtime implementation, artifact creation, media
generation, release, final media approval, or production lock.

## Reviewable Package Definition Shape

A future local delivery package definition may describe these sections without
creating the package:

### Package Metadata

- `packageDefinitionId`
- `status`
- `sourceRouteDecisionRef`
- `definitionScope`
- `reviewMetadata`
- `knownLimitations`
- `downstreamNote`

### Upstream Source References

Required upstream evidence references should be explicit when available:

- `ScriptReadyScenePacket`
- `StoryboardCameraPacket`
- `AssetLifecycleRegistryDecisionPacket`
- `VoiceTimelineSubtitleSfxContract`
- `LocalRendererExecutionReadinessChecklist`
- local render package planning
- relevant technical review PASS ids where available
- human approval records where required

Missing references must be reported as `BLOCKED_BY_MISSING_INPUTS`; they must
not be invented, inferred from silence, or replaced with unrelated old sample
evidence.

### Package Content Placeholders

A future reviewable local package may contain placeholders for:

- source packet references;
- storyboard/camera packet references;
- asset lifecycle decision references;
- voice timeline/subtitle/SFX references;
- renderer-readiness checklist references;
- QA report placeholders;
- artifact manifest placeholders;
- output evidence requirements;
- known limitations;
- reviewer notes.

These are placeholders only. They are not generated files, not package
contents, not a delivery artifact, and not release approval.

### Artifact Manifest Placeholders

Artifact manifest placeholders may describe future expected fields:

- artifact id;
- source ref;
- expected path policy;
- file type;
- generation provenance;
- verification requirement;
- cleanup rule;
- approval boundary.

They must not claim that an artifact exists, has passed QA, is approved, or is
ready for release.

## QA Categories

### Upstream Evidence Completeness

- Are all required upstream references present?
- Are review PASS ids cited only when available?
- Are missing story, storyboard, asset, voice, readiness, or QA inputs blocked
  instead of invented?

### File-Path Safety

- Are future package paths scoped to the canonical workspace?
- Are output directories, overwrite rules, and cleanup rules explicit?
- Are production paths, release paths, and registry paths excluded unless a
  future explicit decision approves them?

### Generated-Artifact Provenance

- Does every future artifact placeholder require a source and generation
  provenance?
- Does the definition forbid treating unknown or copied files as fresh package
  evidence?
- Does it keep local artifacts distinct from production deliverables?

### Duration / Codec / Audio Checks

If media exists in a future explicit task, QA may require:

- duration check;
- video codec check;
- resolution check;
- audio codec check;
- audio presence or silence check;
- nonblank frame check;
- report-path check.

This document does not run those checks and does not generate media.

### Prompt-Surface Leakage

- Do package and QA notes declare `visibility`, `allowedPromptSurface`, and
  `forbiddenPromptSurface` where they may feed prompt-facing or voice/audio
  planning surfaces?
- Are negative prompts treated as prompt surface?
- Are `protectedBoundaryRef` and generic `boundaryCategory` used for protected
  facts?

### Asset Approval Boundaries

- Are asset need refs candidate-only unless separate approval evidence exists?
- Are asset approval, registry approval, approved reference, and production
  lock claims excluded?
- Does the definition avoid converting prompt cards, reference cards, or
  composition recipes into registry entries?

### Human Approval Boundaries

- Does the definition separate technical PASS from user approval?
- Does it avoid release approval, final media approval, provider/media handoff,
  canon, registry, approval, and production lock?
- Are unresolved human decisions represented as blockers?

### Reproducibility Notes

- Are future commands, inputs, environment assumptions, and tool versions
  recorded only as expected evidence?
- Are provider/API, web/server, direct SQLite writes, and external paid tools
  excluded unless a later explicit task changes the boundary?

### Rollback / Cleanup Notes

- Are temporary-output expectations clear?
- Are cleanup authority and preservation rules explicit?
- Does the definition avoid deleting, moving, cleaning, or archiving files?

### Delivery-Readiness Notes

- Are delivery notes limited to local reviewability?
- Does the definition avoid final media readiness, release readiness, and
  production lock claims?
- Are limitations and unresolved blockers visible to reviewers?

## Review Statuses

These statuses are package/QA review statuses only. They are not release,
approval, production lock, final media readiness, runtime state, or delivery
execution permission.

- `DRAFT_FOR_REVIEW`: inspectable candidate definition only.
- `QA_PASS_NOT_APPROVAL`: QA surface appears coherent for technical review, but
  not release, final media approval, or delivery approval.
- `REVIEW_PASS_NOT_APPROVAL`: technical review can pass the definition for
  future route selection, but not for package generation or release.
- `NEEDS_REVISION`: blocked by missing evidence, unclear QA scope,
  prompt-surface risk, file-path risk, or boundary drift.
- `BLOCKED_BY_MISSING_INPUTS`: required upstream source, review, package, or QA
  evidence is absent.
- `BLOCKED_BY_HUMAN_DECISION`: B組01/A組01/00/user decision is required before
  the definition can pass.
- `BLOCKED_BY_BOUNDARY`: requested route would cross a forbidden boundary.

## Visibility And Prompt Surface Rules

Every package or QA note that may later feed prompts, screen descriptions,
negative prompts, prompt-safe asset notes, or voice/audio planning notes must
declare `visibility`, `allowedPromptSurface`, and `forbiddenPromptSurface`.

Prompt surface follows
`docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`:

- fields not explicitly listed in `allowedPromptSurface` must not enter
  prompts, screen descriptions, negative prompts, prompt-safe asset notes, or
  voice/audio planning notes;
- negative prompts are also prompt surface;
- `author_only` / `review_only` material must not be copied, hinted,
  inverted, or paraphrased into package, QA, prompt-facing, screen-facing, or
  voice/audio planning fields;
- prompt-safe asset notes and delivery notes must not leak hidden causality.

Use `forbiddenPromptSurface` to name field families that must remain out of
prompt-facing, screen-facing, and voice/audio planning text.

## Protected Boundary Rule

When package or QA notes must avoid leaking protected information, use
`protectedBoundaryRef` plus a generic `boundaryCategory`.

This is required for:

- hidden causality（隱藏因果）;
- culprit identity（犯人身分）;
- sealed motive（封存動機）;
- director goal（導演目標）;
- future reveal（未來揭露）;
- unavailable actor knowledge（角色不可取得知識）.

The generic `boundaryCategory` may identify the protected boundary type, but it
must not reveal the protected fact.

## Narrow Downstream Allowance

On `REVIEW_PASS_NOT_APPROVAL`, B組01/A組01/00 may consider:

- a future explicit route decision;
- a status/progress update;
- a future bounded implementation package only if it does not cross stop
  conditions.

PASS must not auto-start:

- renderer execution;
- ffmpeg execution;
- TTS execution;
- audio, video, or image generation;
- provider/API;
- schema/runtime implementation;
- package generation;
- artifact creation;
- QA execution against real media;
- release;
- final media approval;
- provider/media handoff;
- production lock.

## Stop Conditions

Stop and return to B組01 / the current control lane if a future task requires:

- Kotlin/JVM source, CLI, runtime, schema, migration, or sample JSON;
- direct SQLite writes;
- provider/API calls;
- web/server/open ports;
- 04/05/06/07/08 restart;
- Phase 20 / Phase 21 restart;
- package generation;
- artifact creation;
- QA execution against real media;
- renderer/ffmpeg/TTS execution;
- media/image/audio/video generation;
- story content generation;
- canon, asset approval, registry approval, release, final approval, final
  media approval, provider/media handoff, or production lock.

## Explicit Non-Claims

- no provider/API
- no renderer/ffmpeg/TTS execution
- no media generation
- no package generation
- no artifact creation
- no release approval
- no final media readiness
- no production lock
