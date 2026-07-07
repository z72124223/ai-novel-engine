# Script-Ready Scene Packet Candidate Source Basis Selection Record V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Selection record id:
`script-ready-scene-packet-candidate-source-basis-selection-v0-001`

## Purpose

`ScriptReadyScenePacketCandidateSourceBasisSelectionRecord` is a docs/control
selection record that attempts to select one source-basis bundle from existing
reviewed docs/control evidence for a future first `ScriptReadyScenePacket`
candidate.

This record references the reviewed prior outcome
`SOURCE_BASIS_MISSING_AI_RESOLVABLE_PLANNING_GAP` from
`docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_SOURCE_BASIS_DECISION_BRIEF_V0.md`.

This record is not an actual `ScriptReadyScenePacket`, not an actual
`ScriptReadyScenePacketCandidate` instance, not sample JSON, not story
generation, not runtime/CLI/schema implementation, not validator
implementation, not route execution, not final approval, and not production
lock.

## Selection Outcome

Outcome: `SOURCE_BASIS_SELECTION_BLOCKED_BY_MISSING_REVIEWED_EVIDENCE`

No safe concrete source-basis bundle can be selected from existing reviewed
docs/control evidence in this slice.

This is not classified as
`SOURCE_BASIS_MISSING_REQUIRES_USER_WORLDBUILDING`, because the project may
have enough worldbuilding or sample planning material somewhere. The blocker is
that the reviewed docs/control evidence does not cite a concrete selectable
bundle that ties together source scene id, zone id, beat id, character refs,
asset need refs, and query evidence refs for the future first packet.

This is not `BLOCKED_BY_BOUNDARY`, because the safe response is to stop at a
docs/control blocked selection record. No packet, sample, runtime, provider,
media, approval, canon, registry, release, or production-lock boundary is
crossed.

## Selection Attempt

| Required selection field | Evidence inspected | Selection result |
| --- | --- | --- |
| selected source scene id / scene reference | `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md` requires `sceneReference`; prior preflight and decision brief found no selected source scene id. Query sample values include `rain-gate-scene-placeholder`, which is explicitly placeholder evidence. | Not selected |
| selected zone id / location reference | The packet spec requires `locationZone`; query sample values include `rain-gate-threshold-placeholder`, which is placeholder evidence. | Not selected |
| selected beat id / beat reference | The packet spec requires `beatList`; query sample values include `beat-approach-gate-placeholder`, which is placeholder evidence. | Not selected |
| selected relevant character refs | Package-definition and preflight docs reserve character placeholders only. No reviewed concrete first-packet character refs were found. | Not selected |
| selected asset need refs | Demand-driven query sample includes asset query surfaces, but no reviewed first-packet asset need bundle is selected, and asset approval / registry approval remain separate. | Not selected |
| selected `QueryRequest` refs | Query contract/spec and sample define request surfaces. The sample request ids are useful evidence that the shape exists, but they are tied to placeholder scene/zone/beat/source-packet values. | Not selected |
| selected `QueryResponse` refs | Query sample defines response surfaces, but no reviewed selection record ties them to a concrete first-packet source basis. | Not selected |
| selected `QueryScopeReviewReport` refs | Query template defines the review report surface. No selected first-packet report refs were found. | Not selected |
| continuity constraints and visibility boundaries | `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md` and `docs/CODEX_SCRIPT_READY_SCENE_PACKET_ALIGNMENT_V0.md` define rules. | Available as rules only |
| `allowedPromptSurface` and forbidden prompt-surface rules | `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md` defines deny-by-default prompt surface controls. | Available as rules only |
| reviewer evidence refs and review gates | Route decision, package definition, preflight, and source-basis decision brief define review gates. | Available as gates only |

## Exact Missing Reviewed Evidence

The missing evidence is a reviewed source-basis bundle that explicitly selects
and ties together:

- source scene id / scene reference;
- zone id / location reference;
- beat id / beat reference;
- relevant character refs;
- asset need refs;
- `QueryRequest` refs, if scoped retrieval is needed;
- `QueryResponse` refs, if scoped retrieval is used;
- `QueryScopeReviewReport` refs, if scoped retrieval is reviewed;
- continuity constraints;
- visibility boundaries;
- `allowedPromptSurface` and forbidden prompt-surface rules;
- reviewer evidence refs and review gates.

Placeholder values such as `rain-gate-scene-placeholder`,
`rain-gate-threshold-placeholder`, `beat-approach-gate-placeholder`, and
`rain-gate-script-ready-scene-packet-draft-placeholder` are not enough to
select `SOURCE_BASIS_SELECTED_FOR_FUTURE_CANDIDATE`.

## Missing Input Classification

Current missing inputs are reviewed-evidence gaps:

- The project may have enough worldbuilding somewhere, but this slice did not
  find a reviewed source-basis bundle.
- The blocker should be classified as
  `SOURCE_BASIS_SELECTION_BLOCKED_BY_MISSING_REVIEWED_EVIDENCE`.
- It should not yet be escalated to
  `SOURCE_BASIS_MISSING_REQUIRES_USER_WORLDBUILDING`.

If a future selection task inspects the allowed evidence pool and still cannot
cite any concrete source scene, zone, beat, character, asset, or query evidence
without inventing content, that later task should stop and reclassify as
`SOURCE_BASIS_MISSING_REQUIRES_USER_WORLDBUILDING`.

## Required Upstream Evidence References

A future selection attempt may cite these reviewed references where available,
without inventing contents:

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
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_SOURCE_BASIS_DECISION_BRIEF_V0.md`;
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`;
- `docs/CODEX_STORYBOARD_CAMERA_PACKET_TEMPLATE_REVIEW_V0.md`;
- `docs/CODEX_ASSET_LIFECYCLE_REGISTRY_DECISION_PACKET_V0.md`;
- `docs/CODEX_VOICE_TIMELINE_SUBTITLE_SFX_CONTRACT_V0.md`;
- `docs/CODEX_LOCAL_RENDERER_EXECUTION_READINESS_CHECKLIST_V0.md`;
- `docs/CODEX_LOCAL_PACKAGE_QA_DELIVERY_DEFINITION_V0.md`;
- `docs/CODEX_BLUEPRINT_ROUTE_COMPLETION_STATUS_AND_NEXT_ROUTE_OPTIONS_V0.md`.

These references remain evidence surfaces only. They do not become packet
content by being listed here.

## Prompt Surface And Visibility Rules

Any source-basis or selection field that may later feed prompts, screen
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

Use `protectedBoundaryRef` plus generic `boundaryCategory` where selection
notes must avoid leaking:

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
- A PASS on this selection record does not generate an actual
  `ScriptReadyScenePacket`, create an actual `ScriptReadyScenePacketCandidate`
  instance, create sample JSON, start story content generation, start
  runtime/CLI/schema/validator work, resume 04/05/06/07/08, run
  renderer/ffmpeg/TTS, call provider/API, generate media, grant approval, create
  canon, create registry approval, release, or create production lock.

## Review Status Terms

- `DRAFT_FOR_REVIEW`: selection record is ready for technical review.
- `REVIEW_PASS_NOT_APPROVAL`: record is technically coherent, but grants no
  approval and starts no work.
- `NEEDS_REVISION`: evidence mapping, selection outcome, source-basis
  classification, prompt-surface safety, visibility, or boundary wording needs
  correction.
- `SOURCE_BASIS_SELECTED_FOR_FUTURE_CANDIDATE`: one source basis is selected as
  a future package candidate only, not an actual packet.
- `SOURCE_BASIS_SELECTION_BLOCKED_BY_MISSING_REVIEWED_EVIDENCE`: reviewed docs
  do not cite a concrete selectable source-basis bundle.
- `SOURCE_BASIS_MISSING_REQUIRES_USER_WORLDBUILDING`: required source material
  cannot be selected from reviewed evidence and must return to 00/user.
- `BLOCKED_BY_HUMAN_DECISION`: user/00/A組01 decision is required before future
  task classification.
- `BLOCKED_BY_BOUNDARY`: the proposed next step crosses runtime/schema,
  provider/API, renderer/ffmpeg/TTS, media, approval, canon, registry, release,
  or production lock boundaries.

## Downstream Allowance

On B組03 review PASS, B組01/A組01/00 may use this selection record to classify a
future explicit task.

Because the current outcome is
`SOURCE_BASIS_SELECTION_BLOCKED_BY_MISSING_REVIEWED_EVIDENCE`, the next safe
candidate is not actual packet work. A future task should first supply or cite a
reviewed source-basis bundle, or explicitly determine that user/worldbuilding
input is required.

PASS must not auto-start:

- actual packet generation;
- actual `ScriptReadyScenePacketCandidate` instance creation;
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
- selection record only
- no selected concrete source-basis bundle in this slice
- no actual `ScriptReadyScenePacket`
- no actual `ScriptReadyScenePacketCandidate` instance
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
