# Script-Ready Scene Packet Candidate Package Definition V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

## Purpose

`ScriptReadyScenePacketCandidatePackageDefinition` is a docs/control definition
for a future reviewable `ScriptReadyScenePacketCandidate` package surface.

This definition is not an actual `ScriptReadyScenePacket`, not sample JSON, not
story generation, not runtime/CLI/schema implementation, not validator
implementation, not route execution, not final approval, and not production
lock. It defines placeholders and references only so a future explicit task can
be classified without inventing packet contents.

## Candidate Package Shape

A future reviewable `ScriptReadyScenePacketCandidate` package may contain these
sections as placeholders and source references only.

### Package Metadata

- `packageId`
- `packageVersion`
- `routeFamily`: `script_ready_scene_packet_candidate`
- `status`: one of `DRAFT_FOR_REVIEW`, `REVIEW_PASS_NOT_APPROVAL`,
  `NEEDS_REVISION`, `BLOCKED_BY_MISSING_INPUTS`,
  `BLOCKED_BY_HUMAN_DECISION`, or `BLOCKED_BY_BOUNDARY`
- `createdByRoute`
- `sourceDecisionBriefRef`
- `progressCaveat`: local baseline 100% is not true final product completion

### Review Metadata

- `reviewId`
- `reviewStatus`
- `reviewChecklistVersion`
- `reviewedEvidenceRefs`
- `unresolvedFindings`
- `technicalPassIsNotApproval`

### Source Scene / Beat / Request References

These fields are references only and must not invent source content:

- `sourceSceneRefs`
- `sourceBeatRefs`
- `sourceQueryRequestRefs`
- `sourceQueryResponseRefs`
- `sourceQueryScopeReviewReportRefs`
- `missingSourceRefs`
- `blockedBecause`

### Upstream Query Evidence References

The package may reference reviewed demand-driven query evidence:

- `QueryRequest`
- `QueryResponse`
- `QueryScopeReviewReport`
- `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md`
- `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md`

The references are scoped support material only. They are not queryer runtime,
not full-world-bible intake, not SQLite, not provider/API, and not approval.

### Script-Ready Alignment References

The package may cite:

- `script_ready_scene_packet_alignment_v0`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_ALIGNMENT_V0.md`
- `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`

These references define alignment and packet expectations only. They do not
generate a `ScriptReadyScenePacket`, sample JSON, scene prose, runtime state,
or downstream handoff.

### Character / Location / Continuity Placeholders

The future candidate package may include placeholders for:

- character reference ids;
- location or zone reference ids;
- visible state and continuity checks;
- unresolved continuity risks;
- missing worldbuilding inputs;
- review-only knowledge boundaries.

Placeholders must cite upstream evidence where available and must leave missing
inputs as blockers. Do not infer missing user worldbuilding beyond reviewed
docs.

### Visibility And Prompt Surface Fields

Any field that may later feed prompts, screen descriptions, negative prompts,
prompt-safe asset notes, or voice/audio planning notes must declare:

- `visibility`
- `allowedPromptSurface`
- `forbiddenPromptSurface`
- `protectedBoundaryRef`
- `boundaryCategory`
- `reviewStatus`

### Downstream Reference Placeholders

The future candidate package may reserve references for downstream review
surfaces, without starting them:

- storyboard/camera packet template review refs;
- asset lifecycle / registry decision packet refs;
- voice timeline / subtitle / SFX contract refs;
- local renderer execution readiness checklist refs;
- local package / QA / delivery definition refs;
- unresolved downstream blockers.

## Required Upstream Evidence

A future package-definition review should check for these references where
available:

- reviewed 09 foundation / baseline evidence;
- `FoundationCardPack` review evidence;
- `QueryRequest`;
- `QueryResponse`;
- `QueryScopeReviewReport`;
- `script_ready_scene_packet_alignment_v0`;
- `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`;
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`;
- `docs/CODEX_STORYBOARD_CAMERA_PACKET_TEMPLATE_REVIEW_V0.md`;
- `docs/CODEX_ASSET_LIFECYCLE_REGISTRY_DECISION_PACKET_V0.md`;
- `docs/CODEX_VOICE_TIMELINE_SUBTITLE_SFX_CONTRACT_V0.md`;
- `docs/CODEX_LOCAL_RENDERER_EXECUTION_READINESS_CHECKLIST_V0.md`;
- `docs/CODEX_LOCAL_PACKAGE_QA_DELIVERY_DEFINITION_V0.md`;
- `docs/CODEX_BLUEPRINT_ROUTE_COMPLETION_STATUS_AND_NEXT_ROUTE_OPTIONS_V0.md`;
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_ROUTE_DECISION_BRIEF_V0.md`.

Missing references are blockers or placeholders only. They are not permission to
invent packet contents.

## Review Checklist

A future package-definition review should verify:

- Source evidence completeness: are the cited 09, query, alignment, and packet
  spec references present?
- Minimum necessary evidence: does the package avoid full-world-bible intake
  and broad retrieval?
- Source alignment: do scene, beat, request, response, and review-report refs
  match the declared candidate scope?
- Visibility boundary: are `author_only`, `review_only`, `production_only`, and
  `user_facing` fields separated?
- Prompt-surface leakage: are all prompt-relevant fields governed by
  `allowedPromptSurface`, `forbiddenPromptSurface`, `protectedBoundaryRef`, and
  `boundaryCategory`?
- Missing inputs: are unresolved user/worldbuilding inputs listed as blockers
  instead of inferred?
- Continuity risks: are character knowledge, location, beat causality, and
  state continuity risks marked for review?
- Downstream readiness references: are storyboard/camera, asset lifecycle,
  voice timeline, renderer-readiness, and local package/QA refs placeholders
  only?
- Runtime/schema/media claims: does the package avoid runtime, CLI, schema,
  validator, SQLite, provider/API, renderer/ffmpeg/TTS, and media claims?
- Approval/production-lock claims: does the package avoid canon, asset
  approval, registry approval, release, final media approval, provider/media
  handoff, and production lock claims?

## Prompt Surface And Visibility Rules

Follow `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`.

Fields not explicitly listed in `allowedPromptSurface` must not enter:

- prompts;
- screen descriptions;
- negative prompts;
- prompt-safe asset notes;
- voice/audio planning notes.

Negative prompts are also prompt surface. They must not copy, summarize, hint,
invert, or paraphrase hidden material.

Use `protectedBoundaryRef` plus generic `boundaryCategory` where candidate
package fields must avoid leaking:

- hidden causality;
- culprit identity;
- sealed motive;
- director goal;
- future reveal;
- unavailable actor knowledge.

The generic `boundaryCategory` may identify the kind of protected boundary, but
must not reveal the protected content.

## Technical PASS Is Not Approval

All technical PASS statuses remain non-approval.

- `B組03 PASS` and `03 PASS` are technical review for the declared scope only.
- `QA_PASS_NOT_APPROVAL` and `REVIEW_PASS_NOT_APPROVAL` do not equal user
  approval, asset approval, registry approval, canon, release, final media
  approval, provider/media handoff, or production lock.
- A PASS on this definition does not generate an actual
  `ScriptReadyScenePacket`, create sample JSON, start runtime/CLI/schema work,
  resume 04/05/06/07/08, run renderer/ffmpeg/TTS, call provider/API, generate
  media, grant final approval, or create production lock.

## Review Statuses

- `DRAFT_FOR_REVIEW`: package definition is ready for technical review.
- `REVIEW_PASS_NOT_APPROVAL`: definition is coherent enough for future task
  classification, but grants no approval and starts no work.
- `NEEDS_REVISION`: source evidence, placeholders, review checklist, prompt
  surface, visibility, or boundaries need correction.
- `BLOCKED_BY_MISSING_INPUTS`: required source scene, beat, query, foundation,
  or review evidence is missing.
- `BLOCKED_BY_HUMAN_DECISION`: user/00/A組01 decision is required before future
  package classification.
- `BLOCKED_BY_BOUNDARY`: the proposed package crosses runtime/schema,
  provider/API, renderer/ffmpeg/TTS, media, approval, canon, registry, release,
  or production lock boundaries.

## Downstream Allowance

On B組03 review PASS, B組01/A組01/00 may use this definition to classify a
future explicit task.

PASS must not auto-start:

- actual packet generation;
- sample JSON creation;
- story content generation;
- B組02 implementation;
- B組03 review;
- runtime/CLI/schema work;
- validator implementation;
- direct SQLite writes;
- queryer implementation;
- 04/05/06/07/08 restart;
- renderer/ffmpeg/TTS execution;
- provider/API work;
- media generation;
- approval or production lock.

## Boundary Summary

- docs/control only
- placeholders and references only
- no actual `ScriptReadyScenePacket`
- no sample JSON
- no story generation
- no Kotlin/JVM source
- no CLI command
- no runtime/schema/migration
- no validator implementation
- no direct SQLite writes
- no queryer implementation
- no provider/API
- no renderer/ffmpeg/TTS execution
- no media generation
- no package generation
- no QA execution against real media
- no artifact creation
- no actual candidate packet content
- no 04/05/06/07/08 restart
- no Phase 20 / Phase 21 restart
- no canon
- no asset approval
- no registry approval
- no release
- no final media approval
- no provider/media handoff
- no production lock
