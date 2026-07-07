# Script-Ready Scene Packet Candidate Preflight V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

## Purpose

`ScriptReadyScenePacketCandidatePreflight` is a docs/control preflight for a
future first `ScriptReadyScenePacket` candidate. It checks whether reviewed
route evidence is sufficient to enter actual packet work.

This preflight is not an actual `ScriptReadyScenePacket`, not a
`ScriptReadyScenePacketCandidate` instance, not sample JSON, not story
generation, not runtime/CLI/schema implementation, not validator
implementation, not route execution, not final approval, and not production
lock.

## Prerequisites For A Future First Packet Candidate

A future first `ScriptReadyScenePacket` candidate needs concrete upstream
references before any packet content can be drafted:

- source scene id or scene reference;
- zone id or location reference;
- beat id or beat reference;
- relevant character references;
- asset need references;
- continuity boundaries;
- visibility boundaries;
- `allowedPromptSurface` and forbidden prompt-surface rules;
- review gates and required reviewer evidence;
- unresolved user/worldbuilding inputs, if any;
- downstream storyboard/camera placeholders;
- downstream asset lifecycle / registry placeholders;
- downstream voice timeline / subtitle / SFX placeholders;
- downstream renderer-readiness placeholders;
- downstream local package / QA placeholders.

These prerequisites are references and blockers only. They are not permission
to invent scene, zone, beat, character, asset, dialogue, action, continuity, or
story content.

## Existing Evidence Check

| Requirement | Existing reviewed docs/control evidence | Preflight result |
| --- | --- | --- |
| source scene id / scene reference | `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md` requires `sceneReference`, but no reviewed current source scene id is selected by the package definition. | Missing prerequisite |
| zone id / location reference | `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md` requires `locationZone`, but no reviewed current zone id is selected by the package definition. | Missing prerequisite |
| beat id / beat reference | `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md` requires `beatList`, and alignment docs allow `beatId` refs, but no reviewed current beat id is selected by the package definition. | Missing prerequisite |
| character references | The spec requires `charactersPresent` and `characterState`; no concrete reviewed first-packet character refs are selected by the package definition. | Missing prerequisite |
| asset need references | The spec includes `approvedProps`, `forbiddenProps`, and storyboard/asset notes, but asset approval and registry approval remain separate. No reviewed first-packet asset need refs are selected. | Missing prerequisite |
| continuity and visibility boundaries | `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md` and `docs/CODEX_SCRIPT_READY_SCENE_PACKET_ALIGNMENT_V0.md` define visibility and continuity rules. | Sufficient as rules only |
| prompt-surface rules | `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md` defines `allowedPromptSurface`, negative prompt, and `protectedBoundaryRef` rules. | Sufficient as rules only |
| review gates and reviewer evidence | `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_PACKAGE_DEFINITION_V0.md` defines review metadata and checklist placeholders. | Sufficient as placeholders only |
| downstream references | Storyboard/camera, asset lifecycle, voice timeline, renderer readiness, and local package/QA docs exist as control references. | Sufficient as placeholders only |

## Preflight Outcome

Outcome: `BLOCKED_BY_MISSING_INPUTS`

The project is not yet safe to enter actual first `ScriptReadyScenePacket`
candidate generation. The missing prerequisite is a reviewed, explicitly
selected source basis for the first packet:

- source scene id / scene reference;
- zone id / location reference;
- beat id / beat reference;
- relevant character references;
- asset need references;
- query evidence refs, if the first packet needs scoped retrieval;
- unresolved user/worldbuilding inputs marked as blockers instead of inferred.

This is a missing prerequisite, not a stop-condition blocker. It does not mean
the route is unsafe forever. It means a future explicit task must first classify
or provide the concrete source basis before actual packet generation can be
considered.

## Required Upstream Evidence References

A future task may cite these reviewed references where available, without
inventing contents:

- reviewed 09 foundation / baseline evidence;
- `docs/CODEX_09_FOUNDATION_CARD_PACK_REVIEW_V0.md`;
- `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md`;
- `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md`;
- `QueryRequest`;
- `QueryResponse`;
- `QueryScopeReviewReport`;
- `script_ready_scene_packet_alignment_v0`;
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_ALIGNMENT_V0.md`;
- `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`;
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_ROUTE_DECISION_BRIEF_V0.md`;
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_PACKAGE_DEFINITION_V0.md`;
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`;
- `docs/CODEX_STORYBOARD_CAMERA_PACKET_TEMPLATE_REVIEW_V0.md`;
- `docs/CODEX_ASSET_LIFECYCLE_REGISTRY_DECISION_PACKET_V0.md`;
- `docs/CODEX_VOICE_TIMELINE_SUBTITLE_SFX_CONTRACT_V0.md`;
- `docs/CODEX_LOCAL_RENDERER_EXECUTION_READINESS_CHECKLIST_V0.md`;
- `docs/CODEX_LOCAL_PACKAGE_QA_DELIVERY_DEFINITION_V0.md`;
- `docs/CODEX_BLUEPRINT_ROUTE_COMPLETION_STATUS_AND_NEXT_ROUTE_OPTIONS_V0.md`.

The preflight does not authorize any of those references to become packet
content by themselves.

## Prompt Surface And Visibility Rules

Any future candidate or preflight field that may later feed prompts, screen
descriptions, negative prompts, prompt-safe asset notes, or voice/audio planning
notes must declare:

- `visibility`;
- `allowedPromptSurface`;
- `forbiddenPromptSurface`;
- `protectedBoundaryRef`;
- `boundaryCategory`;
- review status.

Follow `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`:

- fields not explicitly listed in `allowedPromptSurface` must not enter
  prompts, screen descriptions, negative prompts, prompt-safe asset notes, or
  voice/audio planning notes;
- negative prompts are also prompt surface;
- `author_only` and `review_only` material must not be copied, hinted,
  inverted, summarized, or paraphrased into prompt-facing fields.

Use `protectedBoundaryRef` plus generic `boundaryCategory` where preflight notes
must avoid leaking:

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
- A PASS on this preflight does not generate an actual `ScriptReadyScenePacket`,
  create a `ScriptReadyScenePacketCandidate` instance, create sample JSON,
  start story content generation, start runtime/CLI/schema/validator work,
  resume 04/05/06/07/08, run renderer/ffmpeg/TTS, call provider/API, generate
  media, grant approval, create canon, create registry approval, release, or
  create production lock.

## Review Statuses

- `DRAFT_FOR_REVIEW`: preflight is ready for technical review.
- `REVIEW_PASS_NOT_APPROVAL`: preflight is technically coherent, but grants no
  approval and starts no work.
- `NEEDS_REVISION`: evidence mapping, preflight outcome, prerequisites,
  prompt-surface safety, visibility, or boundary wording needs correction.
- `BLOCKED_BY_MISSING_INPUTS`: required source scene, zone, beat, character,
  asset, query, foundation, or review evidence is missing.
- `BLOCKED_BY_HUMAN_DECISION`: user/00/A組01 decision is required before future
  task classification.
- `BLOCKED_BY_BOUNDARY`: the proposed next step crosses runtime/schema,
  provider/API, renderer/ffmpeg/TTS, media, approval, canon, registry, release,
  or production lock boundaries.

## Downstream Allowance

On B組03 review PASS, B組01/A組01/00 may use this preflight to classify a future
explicit task.

Because the current outcome is `BLOCKED_BY_MISSING_INPUTS`, a future task should
first select or provide the concrete source basis for a first packet candidate.
That future task must still be explicitly classified before dispatch.

PASS must not auto-start:

- actual packet generation;
- `ScriptReadyScenePacketCandidate` instance creation;
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
- approval, canon, registry, release, or production lock.

## Boundary Summary

- docs/control only
- preflight only
- no actual `ScriptReadyScenePacket`
- no `ScriptReadyScenePacketCandidate` instance
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
- no actual first packet generation
- no 04/05/06/07/08 restart
- no Phase 20 / Phase 21 restart
- no canon
- no asset approval
- no registry approval
- no release
- no final media approval
- no provider/media handoff
- no production lock
