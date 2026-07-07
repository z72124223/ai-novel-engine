# Script-Ready Scene Packet Candidate Route Decision Brief V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

## Purpose

`ScriptReadyScenePacketCandidateRouteDecisionBrief` is a docs/control route
decision brief. It evaluates whether reviewed evidence is sufficient to define
the next bounded package for `script_ready_scene_packet_candidate`.

This brief is not a packet instance, not sample JSON, not implementation, not
final approval, not route execution, and not production lock. It does not
generate a `ScriptReadyScenePacket`, does not restart 04/05/06/07/08, does not
run provider/API, does not run renderer/ffmpeg/TTS, and does not generate
media.

## Evidence Review

The reviewed route evidence is sufficient to define a future package-definition
or review-surface task, but not sufficient to create an actual scene packet.

### 09 Foundation / Baseline Route Evidence

- `docs/CODEX_09_FOUNDATION_CARD_JSON_CONTRACT_V0.md` defines
  `FoundationCardPack` as candidate-only local JSON, not canon, not
  setting-pack data, not runtime SQLite data, and not approved assets.
- `docs/CODEX_09_FOUNDATION_CARD_PACK_REVIEW_V0.md` records a technical review
  gate whose PASS status allows only `demand_driven_query_contract_spec_v0`.
  It is not human approval, not canon approval, not registry approval, not
  provider/API, not renderer/ffmpeg/TTS/media generation, not final media
  approval, and not production lock.

### Demand-Driven Query / Retrieval Evidence

- `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md` defines future
  `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport` shapes for
  World Card Queryer, Asset Queryer, and Situation / Continuity / Pressure
  Queryer.
- `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md` provides template surfaces
  for the same contract/spec.
- Query evidence is scoped support material only. It is not queryer runtime,
  not SQLite, not provider/API, not full-world-bible intake, and not approval.

### Script-Ready Alignment Evidence

- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_ALIGNMENT_V0.md` explains how
  `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport` evidence may
  feed future `ScriptReadyScenePacket` sections.
- `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md` defines the packet as a product and
  authoring contract. It says 04/05/06/07/08 remain paused until explicit
  handoff and that the spec does not approve runtime enforcement, schema fields,
  UI work, or animation integration.
- The base spec requires visibility separation and keeps author-only causality,
  director goals, sealed answers, culprit identity, future plot answers, and
  unavailable actor knowledge out of user-facing packet sections.

### Prompt Surface Boundary Evidence

- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md` says prompt surface is
  deny-by-default.
- Fields not explicitly listed in `allowedPromptSurface` must not enter
  prompts, screen descriptions, negative prompts, prompt-safe asset notes, or
  voice/audio planning notes.
- Negative prompts are also prompt surface.
- Protected material must use `protectedBoundaryRef` plus generic
  `boundaryCategory`.

### Downstream Route Evidence

The following reviewed docs/control packets define downstream review surfaces,
but they do not approve packet generation, runtime work, media, or final
approval:

- `docs/CODEX_STORYBOARD_CAMERA_PACKET_TEMPLATE_REVIEW_V0.md`
- `docs/CODEX_ASSET_LIFECYCLE_REGISTRY_DECISION_PACKET_V0.md`
- `docs/CODEX_VOICE_TIMELINE_SUBTITLE_SFX_CONTRACT_V0.md`
- `docs/CODEX_LOCAL_RENDERER_EXECUTION_READINESS_CHECKLIST_V0.md`
- `docs/CODEX_LOCAL_PACKAGE_QA_DELIVERY_DEFINITION_V0.md`
- `docs/CODEX_BLUEPRINT_ROUTE_COMPLETION_STATUS_AND_NEXT_ROUTE_OPTIONS_V0.md`

These packets support route awareness only. They do not auto-start B組02
implementation, B組03 review, packet generation, renderer/ffmpeg/TTS, media
generation, provider/API work, schema/runtime work, final approval, or
production lock.

## Recommended Outcome

Exactly one safe next package candidate is recommended:

`script_ready_scene_packet_candidate_package_definition_v0_docs_control`

This future package should define a reviewable package-definition and review
surface for a `ScriptReadyScenePacketCandidate`. It may specify required source
refs, candidate field groups, review checklist, prompt-surface controls,
visibility rules, and stop conditions.

It must remain candidate-only. It must not:

- create the actual `ScriptReadyScenePacket`;
- create sample JSON;
- generate story content;
- resume 04/05/06/07/08;
- implement runtime, CLI, schema, SQLite, or queryers;
- run provider/API;
- run renderer/ffmpeg/TTS;
- generate media;
- claim user approval, asset approval, registry approval, canon, release,
  final media approval, provider/media handoff, or production lock.

Actual packet generation remains blocked until a future explicit package is
classified and all required source inputs, review boundaries, and user/human
approval gates are present.

## Technical PASS Is Not Approval

All technical PASS statuses remain non-approval.

- `B組03 PASS` and `03 PASS` mean technical review for the declared scope only.
- `QA_PASS_NOT_APPROVAL` and `REVIEW_PASS_NOT_APPROVAL` do not equal user
  approval, asset approval, registry approval, canon, release, final media
  approval, provider/media handoff, or production lock.
- A PASS on this brief does not start B組02 implementation, B組03 review,
  packet generation, runtime/CLI/schema work, 04/05/06/07/08, renderer,
  ffmpeg, TTS, provider/API, media generation, approval, or production lock.

## Prompt Surface And Visibility Rules

Any candidate text that may later feed prompts, screen descriptions, negative
prompts, prompt-safe asset notes, or voice/audio planning notes must declare:

- `visibility`;
- `allowedPromptSurface`;
- `forbiddenPromptSurface`;
- review status.

Follow `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`:

- fields not explicitly listed in `allowedPromptSurface` must not enter
  prompts, screen descriptions, negative prompts, prompt-safe asset notes, or
  voice/audio planning notes;
- negative prompts are also prompt surface;
- `author_only` and `review_only` material must not be copied, hinted,
  inverted, summarized, or paraphrased into prompt-facing fields.

Use `protectedBoundaryRef` plus generic `boundaryCategory` when candidate route
notes must avoid leaking:

- hidden causality;
- culprit identity;
- sealed motive;
- director goal;
- future reveal;
- unavailable actor knowledge.

The generic category may identify the type of protected boundary, but must not
reveal the protected content.

## Review Statuses

- `DRAFT_FOR_REVIEW`: brief is ready for technical review.
- `REVIEW_PASS_NOT_APPROVAL`: route evidence is coherent enough for future
  package classification, but grants no approval and starts no work.
- `NEEDS_REVISION`: evidence mapping, scope, prompt-surface safety, or boundary
  wording is incomplete.
- `BLOCKED_BY_MISSING_INPUTS`: a future route cannot be defined because source
  scene, query, foundation, or review evidence is missing.
- `BLOCKED_BY_HUMAN_DECISION`: user/00/A組01 decision is required before route
  classification can proceed.
- `BLOCKED_BY_BOUNDARY`: the proposed route crosses provider/API, media,
  renderer/ffmpeg/TTS, SQLite/schema/runtime, approval, canon, registry,
  release, or production lock boundaries.

## Downstream Allowance

On B組03 review PASS, B組01/A組01/00 may use this brief for route awareness and
may classify a future explicit implementation package.

PASS must not auto-start:

- B組02 implementation;
- packet generation;
- B組03 review;
- runtime/CLI/schema work;
- direct SQLite writes;
- queryer implementation;
- 04/05/06/07/08 restart;
- renderer/ffmpeg/TTS execution;
- provider/API work;
- media generation;
- approval or production lock.

## Boundary Summary

- docs/control only
- no packet instance
- no sample JSON
- no Kotlin/JVM source
- no CLI command
- no runtime/schema/migration
- no direct SQLite writes
- no queryer implementation
- no provider/API
- no renderer/ffmpeg/TTS execution
- no media generation
- no package generation
- no QA execution against real media
- no artifact creation
- no story content generation
- no actual `ScriptReadyScenePacket`
- no 04/05/06/07/08 restart
- no Phase 20 / Phase 21 restart
- no canon
- no asset approval
- no registry approval
- no release
- no final media approval
- no provider/media handoff
- no production lock
