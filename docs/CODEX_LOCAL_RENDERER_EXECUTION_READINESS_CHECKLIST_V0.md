# Codex Local Renderer Execution Readiness Checklist V0

Status: `DRAFT_FOR_REVIEW`

Route label: `local_renderer_execution_readiness_checklist_v0_docs_control`

This document defines a docs/control
`LocalRendererExecutionReadinessChecklist`. It is not renderer execution, not
ffmpeg/TTS execution, not image/audio/video media generation, not a CLI command,
not runtime, not schema or migration, not provider/API, not final media
readiness, not renderer readiness approval, not canon, not asset approval, not
registry approval, not release, not final approval, and not production lock.

## Purpose

`LocalRendererExecutionReadinessChecklist` prepares a reviewable decision
surface that B組01/A組01/00 may inspect before any future explicit local
renderer execution task is assigned.

The intended bridge is:

```text
ScriptReadyScenePacket evidence
-> StoryboardCameraPacket planning
-> AssetLifecycleRegistryDecisionPacket / asset route evidence
-> VoiceTimelineSubtitleSfxContract
-> local render package / QA planning
-> LocalRendererExecutionReadinessChecklist
-> technical review only
-> future explicit route decision
```

This checklist does not auto-start renderer/ffmpeg/TTS, provider/API, schema or
runtime implementation, media generation, final media approval, or production
lock.

## Checklist Statuses

These statuses are checklist labels only. They are not runtime state, not human
approval, not renderer execution permission, and not final media readiness.

- `ready_for_future_decision`: evidence appears coherent enough for B組01/A組01
  /00 to consider a future explicit decision; it is not execution approval.
- `blocked_by_missing_inputs`: required upstream evidence is absent or
  incomplete.
- `blocked_by_human_decision`: a user, B組01, A組01, or 00 decision is required
  before readiness can be reviewed.
- `blocked_by_boundary`: the requested path would cross a forbidden boundary,
  such as provider/API, renderer execution, media generation, direct SQLite
  writes, final media approval, or production lock.
- `not_ready`: the checklist found technical, evidence, environment, output, or
  review-routing gaps.

## Required Upstream Evidence References

A future checklist instance should cite explicit evidence when available:

- `ScriptReadyScenePacket` or script-ready scene packet alignment evidence.
- `StoryboardCameraPacket` review evidence.
- `AssetLifecycleRegistryDecisionPacket` review evidence.
- `VoiceTimelineSubtitleSfxContract` review evidence.
- Local render package planning and QA evidence.
- Relevant technical review PASS ids where available.
- Human approval records where required.

Missing evidence must be marked as `blocked_by_missing_inputs`; it must not be
invented, inferred from silence, or replaced with unrelated local sample proof.

## Readiness Categories

### Source Packet Readiness

- Does the source scene/beat evidence exist?
- Is the source scoped rather than a full world bible import?
- Are author-only and review-only fields excluded from renderer-facing notes?
- Are unresolved source blockers listed?

### Storyboard / Camera Packet Readiness

- Is there a reviewed `StoryboardCameraPacket` or equivalent route evidence?
- Are panel refs, scene refs, composition recipe refs, and continuity notes
  scoped?
- Are storyboard/camera PASS results technical-only and not final visual
  approval?

### Asset Lifecycle / Approval Readiness

- Is there an `AssetLifecycleRegistryDecisionPacket` or current asset route
  decision evidence?
- Are asset need refs candidate-only unless separate user approval exists?
- Are asset approval, registry approval, approved reference, and production
  lock boundaries explicit?
- If storage architecture is undecided, is the checklist blocked by a human or
  route decision rather than silently choosing storage?

### Voice Timeline / Subtitle / SFX Readiness

- Is there a reviewed `VoiceTimelineSubtitleSfxContract` or equivalent route
  evidence?
- Are dialogue, subtitle, SFX, ambient, silence, and beat marker cues scoped?
- Are TTS, audio generation, sound-library selection, and audio export still
  forbidden unless a future explicit task authorizes them?

### Local Render Package / QA Readiness

- Is local render package planning present?
- Does QA evidence identify source alignment, panel/timeline coverage, duration
  assumptions, and failed checks?
- Does the readiness note avoid claiming renderer readiness, MP4 readiness, or
  final video readiness?

### Human Approval Boundaries

- Which items require user approval before execution can be considered?
- Which technical PASS results are not approval?
- Are final media approval, provider/media handoff, canon, registry, release,
  approval, and production lock excluded?

### Output Evidence Requirements

Before any future execution task, the route decision should define:

- allowed output directory pattern;
- expected report paths;
- whether generated artifacts would be disposable local evidence;
- required file-existence, size, duration, stream, and nonblank checks if media
  generation is later explicitly authorized;
- how to report failures without treating partial artifacts as approved media.

This section is a planning checklist only. It does not generate artifacts.

### Environment / Tool Availability

A future execution task may need to check local availability of renderer,
ffmpeg, TTS, fonts, codecs, or workspace dependencies. This checklist only
records that such checks may be required; it must not run tools or install
anything.

### Rollback / Cleanup Expectations

Before any future execution task, the route decision should define:

- whether outputs are local temporary artifacts;
- what may be overwritten;
- what must be preserved;
- how failed output should be reported;
- who may approve cleanup.

This checklist must not delete, clean, move, or archive files.

### Review Routing

- B組02 may draft this docs/control checklist only when assigned.
- B組01/A組01/00 decide any future route step.
- B組03 technical PASS is review evidence only, not user approval and not
  execution permission.

## Visibility And Prompt Surface Rules

Every renderer-readiness note that may later feed prompts, screen descriptions,
negative prompts, prompt-safe asset notes, or voice/audio planning notes must
declare `visibility`, `allowedPromptSurface`, and `forbiddenPromptSurface`.

Prompt surface follows
`docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`:

- fields not explicitly listed in `allowedPromptSurface` must not enter
  prompts, screen descriptions, negative prompts, prompt-safe asset notes, or
  voice/audio planning notes;
- negative prompts are also prompt surface;
- `author_only` / `review_only` material must not be copied, hinted,
  inverted, or paraphrased into renderer-readiness notes;
- prompt-safe asset notes and voice/audio planning notes must not leak hidden
  causality.

Use `forbiddenPromptSurface` to name field families that must remain out of
prompt-facing, screen-facing, and voice/audio planning text.

## Protected Boundary Rule

When readiness notes must avoid leaking protected information, use
`protectedBoundaryRef` plus a generic `boundaryCategory`.

This is required for:

- hidden causality（隱藏因果）;
- culprit identity（犯人身分）;
- sealed motive（封存動機）;
- director goal（導演目標）;
- future reveal（未來揭露）;
- unavailable actor knowledge（角色不可取得知識）.

The generic `boundaryCategory` may identify the type of protected boundary, but
it must not reveal the protected fact.

## Review Checklist

A checklist draft should receive `REVIEW_PASS_NOT_APPROVAL` only when all
review areas pass.

### Upstream Evidence Completeness

- Are required source packet, storyboard/camera, asset lifecycle, voice
  timeline/subtitle/SFX, and local render package refs present?
- Are missing refs represented as blockers?
- Are PASS ids cited only when available?

### Asset / Registry / Approval Boundaries

- Are asset and registry approvals separate from technical review?
- Are candidate asset refs kept distinct from approved references?
- Are storage, schema, and registry decisions escalated when needed?

### Prompt-Surface Safety

- Are `visibility`, `allowedPromptSurface`, and `forbiddenPromptSurface`
  declared for readiness notes that may later face prompt or voice/audio
  planning surfaces?
- Are negative prompts treated as prompt surface?
- Are `protectedBoundaryRef` and generic `boundaryCategory` used for protected
  facts?

### Local Environment / Tool Preconditions

- Are future tool checks named without running tools?
- Are renderer/ffmpeg/TTS availability checks deferred to an explicit future
  execution-readiness decision?
- Are install, download, and provider/API assumptions excluded?

### Output-Path Safety

- Is there a future output-path policy before media generation?
- Are output artifacts described as local evidence only?
- Does the checklist forbid generated artifact claims in this docs/control
  slice?

### No Generated Artifact Claims

- Does the checklist avoid claiming a new MP4, audio file, image, renderer
  output, or final deliverable exists?
- Does it avoid treating the old Rain Gate preview artifact as proof of whole
  final-product readiness?

### No Media / Provider / Renderer Claims

- no provider/API
- no renderer/ffmpeg/TTS execution
- no image/audio/video media generation
- no final media approval
- no provider/media handoff
- no renderer readiness
- no MP4 readiness
- no production lock

## Review Statuses

- `DRAFT_FOR_REVIEW`: inspectable checklist draft only.
- `REVIEW_PASS_NOT_APPROVAL`: technically coherent for later route selection,
  but not execution permission, not human approval, and not final media
  approval.
- `NEEDS_REVISION`: blocked by missing evidence, unsafe prompt surface,
  boundary drift, environment ambiguity, output-path risk, or review-routing
  gap.
- `BLOCKED_BY_HUMAN_DECISION`: requires B組01/A組01/00/user decision before the
  checklist can pass.
- `BLOCKED_BY_BOUNDARY`: requested route would cross a forbidden boundary.

## Narrow Downstream Allowance

On `REVIEW_PASS_NOT_APPROVAL`, B組01/A組01/00 may consider another
route-selected docs/control task such as local package / QA / delivery
definition, or may ask for a future explicit decision about renderer execution
readiness.

PASS must not auto-start:

- renderer execution;
- ffmpeg execution;
- TTS execution;
- audio, video, or image generation;
- provider/API;
- schema/runtime implementation;
- direct SQLite writes;
- final media approval;
- provider/media handoff;
- release or production lock.

## Stop Conditions

Stop and return to B組01 / the current control lane if a future task requires:

- Kotlin/JVM source, CLI, runtime, schema, migration, or sample JSON;
- direct SQLite writes;
- provider/API calls;
- web/server/open ports;
- 04/05/06/07/08 restart;
- Phase 20 / Phase 21 restart;
- renderer/ffmpeg/TTS execution;
- media/image/audio/video generation;
- generated artifact claims;
- story content generation;
- canon, asset approval, registry approval, release, final approval, final
  media approval, provider/media handoff, or production lock.
