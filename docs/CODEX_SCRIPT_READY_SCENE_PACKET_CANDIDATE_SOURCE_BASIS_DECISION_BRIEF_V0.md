# Script-Ready Scene Packet Candidate Source Basis Decision Brief V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

## Purpose

`ScriptReadyScenePacketCandidateSourceBasisDecisionBrief` is a docs/control
decision brief for the source-basis question raised by
`docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_PREFLIGHT_V0.md`.

This brief identifies whether existing reviewed docs contain a selectable
concrete source basis for a future first `ScriptReadyScenePacket` candidate.
It is not an actual `ScriptReadyScenePacket`, not a
`ScriptReadyScenePacketCandidate` instance, not sample JSON, not story
generation, not runtime/CLI/schema implementation, not validator
implementation, not route execution, not final approval, and not production
lock.

## Decision Outcome

Outcome: `SOURCE_BASIS_MISSING_AI_RESOLVABLE_PLANNING_GAP`

Existing reviewed docs do not yet contain one clearly selectable concrete
source basis for a future first `ScriptReadyScenePacket` candidate.

The missing inputs are:

- selected source scene id / scene reference;
- selected zone id / location reference;
- selected beat id / beat reference;
- selected relevant character references;
- selected asset need references;
- selected `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport` refs,
  if scoped retrieval evidence is needed for the first packet;
- one explicit source-basis selection record tying those refs together.

This is classified as an AI-resolvable planning gap, not a proven
`SOURCE_BASIS_MISSING_REQUIRES_USER_WORLDBUILDING` blocker, because reviewed
docs/control evidence already defines the needed surfaces, review rules, and
source-reference placeholders. What is missing is an explicit future
classification/selection task that chooses or confirms one source basis without
inventing story content.

If a future source-basis selection task cannot identify a candidate from
reviewed evidence without inventing user worldbuilding, then that later task
must stop and reclassify the blocker as
`SOURCE_BASIS_MISSING_REQUIRES_USER_WORLDBUILDING`.

This decision does not authorize actual packet creation.

## Source Basis Evidence Check

| Required basis | Existing reviewed evidence | Decision |
| --- | --- | --- |
| source scene id / scene reference | `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md` requires `sceneReference`; the preflight found no selected source scene id. Query examples observed during evidence review use placeholder scene ids, not a selected first-packet source basis. | Missing AI-resolvable planning gap |
| zone id / location reference | The packet spec requires `locationZone`; alignment docs allow `zoneId` in query evidence when known. No selected first-packet zone id is present. | Missing AI-resolvable planning gap |
| beat id / beat reference | The packet spec requires `beatList`; alignment docs allow `beatId` in `QueryRequest`. No selected first-packet beat id is present. | Missing AI-resolvable planning gap |
| relevant character references | The packet spec requires `charactersPresent` and `characterState`; package-definition docs reserve placeholders only. | Missing AI-resolvable planning gap |
| asset need references | The packet spec has `approvedProps` and `forbiddenProps` field labels, but asset approval and registry approval remain separate. No selected first-packet asset need refs are present. | Missing AI-resolvable planning gap |
| query evidence refs | `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md` and `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md` define `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport` surfaces. No concrete selected query evidence refs are attached to the first packet candidate. | Missing AI-resolvable planning gap |
| continuity and visibility boundaries | `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md` and `docs/CODEX_SCRIPT_READY_SCENE_PACKET_ALIGNMENT_V0.md` define continuity and visibility rules. | Available as rules only |
| `allowedPromptSurface` and forbidden prompt-surface rules | `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md` defines deny-by-default prompt surface rules. | Available as rules only |
| review gates and reviewer evidence | Package definition and preflight docs define technical review surfaces and non-approval boundaries. | Available as review rules only |

## Required Upstream Evidence References

A future source-basis selection task may cite these reviewed references where
available, without inventing source contents:

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
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_PREFLIGHT_V0.md`;
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`;
- `docs/CODEX_STORYBOARD_CAMERA_PACKET_TEMPLATE_REVIEW_V0.md`;
- `docs/CODEX_ASSET_LIFECYCLE_REGISTRY_DECISION_PACKET_V0.md`;
- `docs/CODEX_VOICE_TIMELINE_SUBTITLE_SFX_CONTRACT_V0.md`;
- `docs/CODEX_LOCAL_RENDERER_EXECUTION_READINESS_CHECKLIST_V0.md`;
- `docs/CODEX_LOCAL_PACKAGE_QA_DELIVERY_DEFINITION_V0.md`;
- `docs/CODEX_BLUEPRINT_ROUTE_COMPLETION_STATUS_AND_NEXT_ROUTE_OPTIONS_V0.md`.

These references are evidence surfaces only. They do not become packet content
by being listed here.

## Missing Inputs Classification

Current missing inputs are planning-selection gaps:

- choose one source scene id / scene reference from reviewed evidence;
- choose one zone id / location reference from reviewed evidence;
- choose one beat id / beat reference from reviewed evidence;
- choose relevant character references from reviewed evidence;
- choose asset need references from reviewed evidence;
- choose query evidence refs, if needed;
- record why the selected set is minimum-scope and safe.

They are not currently proven true 00/user worldbuilding blockers because this
brief has not found evidence that the project lacks all candidate source
material. The blocker is that no reviewed route doc has selected one concrete
source-basis bundle yet.

The future selection task must stop instead of inventing content if it cannot
cite reviewed source evidence for any required field.

## Prompt Surface And Visibility Rules

Any source-basis or decision field that may later feed prompts, screen
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

Use `protectedBoundaryRef` plus generic `boundaryCategory` where decision notes
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
- A PASS on this brief does not generate an actual `ScriptReadyScenePacket`,
  create a `ScriptReadyScenePacketCandidate` instance, create sample JSON,
  start story content generation, start runtime/CLI/schema/validator work,
  resume 04/05/06/07/08, run renderer/ffmpeg/TTS, call provider/API, generate
  media, grant approval, create canon, create registry approval, release, or
  create production lock.

## Review Statuses

- `DRAFT_FOR_REVIEW`: brief is ready for technical review.
- `REVIEW_PASS_NOT_APPROVAL`: brief is technically coherent, but grants no
  approval and starts no work.
- `NEEDS_REVISION`: evidence mapping, source-basis classification,
  prompt-surface safety, visibility, or boundary wording needs correction.
- `SOURCE_BASIS_AVAILABLE_AS_FUTURE_CANDIDATE`: one source basis is clearly
  available as a future candidate only, not an actual packet.
- `SOURCE_BASIS_MISSING_AI_RESOLVABLE_PLANNING_GAP`: source-basis selection is
  missing, but can be attempted through a future docs/control planning task
  without inventing content.
- `SOURCE_BASIS_MISSING_REQUIRES_USER_WORLDBUILDING`: required source material
  cannot be selected from reviewed evidence and must return to 00/user.
- `BLOCKED_BY_HUMAN_DECISION`: user/00/A組01 decision is required before future
  task classification.
- `BLOCKED_BY_BOUNDARY`: the proposed next step crosses runtime/schema,
  provider/API, renderer/ffmpeg/TTS, media, approval, canon, registry, release,
  or production lock boundaries.

## Downstream Allowance

On B組03 review PASS, B組01/A組01/00 may use this brief to classify a future
explicit source-basis selection task.

Because the current outcome is
`SOURCE_BASIS_MISSING_AI_RESOLVABLE_PLANNING_GAP`, the next safe candidate is a
docs/control source-basis selection/classification package. It should choose or
confirm source scene id, zone id, beat id, character refs, asset need refs, and
query evidence refs from reviewed evidence only.

PASS must not auto-start:

- actual packet generation;
- `ScriptReadyScenePacketCandidate` instance creation;
- sample JSON creation;
- story content generation;
- B組02 implementation beyond an explicitly classified future task;
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
- decision brief only
- no actual `ScriptReadyScenePacket`
- no `ScriptReadyScenePacketCandidate` instance
- no sample JSON
- no story generation
- no source content invention
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
