# Script-Ready Scene Packet Candidate Reviewed Evidence Bundle Ref Resolution Packet V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`ScriptReadyScenePacketCandidateReviewedEvidenceBundleRefResolutionPacket`

This document is docs/control only. It is not an actual
`ScriptReadyScenePacket`, not an actual `ScriptReadyScenePacketCandidate`, not a
sample JSON file, not packet candidate generation, not source/story content
invention, not source-basis selection, not evidence bundle creation, not route
execution, not concrete story/source content resolution, not
runtime/CLI/schema/validator work, not provider/API work, not media generation,
and not production lock.

## Upstream Reviewed Outcomes

This packet is downstream of these reviewed outcomes:

- `EXISTING_REVIEWED_EVIDENCE_BUNDLE_NOT_AVAILABLE`
- `AI_RESOLVABLE_GAP_CLOSURE_PATH_DEFINED_FOR_FUTURE_CLASSIFICATION`
- `REF_RESOLUTION_PLAN_DEFINED_FOR_FUTURE_PACKET_CLASSIFICATION`

The upstream reviewed docs show that concrete reviewed refs are still missing
for the first future `ScriptReadyScenePacket` candidate route.

## Packet Metadata Surface

A future reviewed evidence bundle ref-resolution packet should expose these
metadata fields without becoming runtime data or schema:

- `packetId`: stable docs/control packet id.
- `routeFamily`: expected value `script_ready_scene_packet_candidate`.
- `sourcePlanRef`: citation to the reviewed ref-resolution plan.
- `upstreamEvidenceRefs`: citations to upstream reviewed docs/control evidence.
- `reviewStatus`: one of the review status terms below.
- `resultStatus`: one of the packet-level outcome labels below.
- `createdFor`: future classification only.
- `visibility`: field-level visibility classification.
- `allowedPromptSurface`: explicit prompt-surface allowlist for any field that
  may later feed prompts, screen descriptions, negative prompts, prompt-safe
  asset notes, or voice/audio planning notes.
- `forbiddenPromptSurface`: explicit forbidden prompt-surface notes.
- `protectedBoundaryRef`: protected-boundary reference where needed.
- `boundaryCategory`: generic protected-boundary category where needed.

## Upstream Evidence Refs Surface

The packet surface may cite these reviewed docs/control surfaces:

- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_EXISTING_REVIEWED_EVIDENCE_BUNDLE_INVENTORY_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_AI_RESOLVABLE_GAP_CLOSURE_PATH_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_REVIEWED_EVIDENCE_BUNDLE_REF_RESOLUTION_PLAN_V0.md`
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

These citations provide rules, constraints, and reviewed outcomes. They do not
provide concrete source scene id, zone id, beat id, character refs, asset need
refs, `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport` refs for a
future first packet.

## Ref Slot Status Vocabulary

Each required concrete ref slot must be classified as exactly one of:

- `REF_SLOT_CITED_FROM_REVIEWED_EVIDENCE`: the slot cites a concrete reviewed
  docs/control ref.
- `REF_SLOT_UNRESOLVED_AI_RESOLVABLE_PLANNING_GAP`: the slot is unresolved, but
  reviewed evidence does not prove user/worldbuilding blocker or boundary
  blocker.
- `REF_SLOT_BLOCKED_BY_USER_WORLDBUILDING`: the slot requires new user or 00
  worldbuilding input that cannot be safely derived from reviewed project
  evidence.
- `REF_SLOT_BLOCKED_BY_BOUNDARY`: resolving the slot would cross forbidden
  boundaries such as story/source invention, actual packet generation, runtime
  work, provider/API, media generation, approval, canon, registry, release,
  final approval, or production lock.

## Ref Slot Classification

No concrete refs are resolved by this packet. Each required slot remains
unresolved because reviewed docs/control evidence does not cite a concrete
value.

| Required concrete ref slot | Slot classification | Reason |
| --- | --- | --- |
| source scene id | `REF_SLOT_UNRESOLVED_AI_RESOLVABLE_PLANNING_GAP` | Upstream reviewed docs define the need for a source scene id but do not cite one concrete reviewed source scene id. |
| zone id | `REF_SLOT_UNRESOLVED_AI_RESOLVABLE_PLANNING_GAP` | Upstream reviewed docs define the need for a zone id / location reference but do not cite one concrete reviewed zone id. |
| beat id | `REF_SLOT_UNRESOLVED_AI_RESOLVABLE_PLANNING_GAP` | Upstream reviewed docs define the need for a beat id / beat reference but do not cite one concrete reviewed beat id. |
| character refs | `REF_SLOT_UNRESOLVED_AI_RESOLVABLE_PLANNING_GAP` | Upstream reviewed docs define the need for character refs but do not cite concrete reviewed character refs for this route. |
| asset need refs | `REF_SLOT_UNRESOLVED_AI_RESOLVABLE_PLANNING_GAP` | Upstream reviewed docs define asset need refs as future planning refs but do not cite concrete reviewed refs. |
| `QueryRequest` refs | `REF_SLOT_UNRESOLVED_AI_RESOLVABLE_PLANNING_GAP` | Upstream reviewed docs define `QueryRequest` surfaces but do not cite concrete reviewed request refs. |
| `QueryResponse` refs | `REF_SLOT_UNRESOLVED_AI_RESOLVABLE_PLANNING_GAP` | Upstream reviewed docs define `QueryResponse` surfaces but do not cite concrete reviewed response refs. |
| `QueryScopeReviewReport` refs | `REF_SLOT_UNRESOLVED_AI_RESOLVABLE_PLANNING_GAP` | Upstream reviewed docs define `QueryScopeReviewReport` surfaces but do not cite concrete reviewed report refs. |

If future reviewed evidence proves a different status for a slot, it must
change exactly one slot classification and cite the evidence that justifies the
change. Until then, unresolved slots must not be inferred from placeholders,
samples not explicitly reviewed/promoted, inferred story/source content, hidden
author-only or review-only material, runtime database inspection, or direct
SQLite writes.

## Packet-Level Outcome Labels

Packet-level result status must be one of:

- `REF_RESOLUTION_PACKET_READY_FOR_REVIEW`
- `REF_RESOLUTION_PACKET_BLOCKED_BY_UNRESOLVED_REVIEWED_REFS`
- `REF_RESOLUTION_PACKET_BLOCKED_BY_USER_WORLDBUILDING`
- `REF_RESOLUTION_PACKET_BLOCKED_BY_BOUNDARY`

Selected packet-level outcome:
`REF_RESOLUTION_PACKET_BLOCKED_BY_UNRESOLVED_REVIEWED_REFS`.

The packet is blocked because every required concrete ref slot remains
unresolved from reviewed docs/control evidence. This is not a user/worldbuilding
blocker and not a boundary blocker unless future reviewed evidence proves one of
those classifications.

No next future package candidate is named in this packet because the selected
packet-level outcome is blocked by unresolved reviewed refs.

## Prompt Surface And Visibility Rules

Prompt surface is deny-by-default. Fields not explicitly listed in
`allowedPromptSurface` must not enter prompts, screen descriptions, negative
prompts, prompt-safe asset notes, or voice/audio planning notes.

Negative prompts are also prompt surface.

Any future ref-resolution packet or downstream packet must define visibility for
each field that could later feed prompts, screen descriptions, negative
prompts, prompt-safe asset notes, or voice/audio planning notes. `author_only`
and `review_only` material must not be copied, inverted, summarized, hinted, or
paraphrased into prompt-facing fields.

Where notes must avoid leaking hidden causality, culprit identity, sealed
motive, director goal, future reveal, or unavailable actor knowledge, they must
use `protectedBoundaryRef` plus a generic `boundaryCategory` instead of copying
or paraphrasing protected content.

## Review Status Terms

- `DRAFT_FOR_REVIEW`: this ref-resolution packet is ready for technical review.
- `REVIEW_PASS_NOT_APPROVAL`: technical review passed, but this is not user
  approval, asset approval, registry approval, canon approval, release approval,
  final media approval, provider/media handoff, final approval, or production
  lock.
- `NEEDS_REVISION`: the packet lacks required metadata, upstream evidence refs,
  ref slot status, packet-level outcome, prompt-surface rules, visibility rules,
  or boundary wording.
- `BLOCKED_BY_HUMAN_DECISION`: a human/user decision is required before future
  task classification.
- `BLOCKED_BY_BOUNDARY`: continuing would cross source/story invention, actual
  packet generation, sample JSON/story content, runtime/schema, provider/API,
  media, approval, canon, registry, release, final approval, or production lock.

## Technical PASS Is Not Approval

`B03 PASS`, `03 PASS`, `QA_PASS_NOT_APPROVAL`, and
`REVIEW_PASS_NOT_APPROVAL` do not equal user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

A technical PASS on this packet does not select source basis, resolve concrete
refs, create an evidence bundle, create an actual `ScriptReadyScenePacket`,
create an actual `ScriptReadyScenePacketCandidate`, create sample JSON, start
story generation, start runtime/CLI/schema/validator work, resume
04/05/06/07/08, run renderer/ffmpeg/TTS, call provider/API, generate media,
grant approval, create canon, create registry approval, release, or create
production lock.

## Downstream Allowance

On B03 review PASS, B01/A01/00 may use this packet result to classify a future
explicit task. PASS must not auto-start source-basis selection, evidence bundle
creation, actual packet generation, sample JSON creation, story content
generation, runtime/CLI/schema/validator work, 04/05/06/07/08 restart,
renderer/ffmpeg/TTS execution, provider/API work, media generation, approval,
canon, registry, release, or production lock.

## Boundary Summary

- docs/control only
- no concrete ref resolution
- no source-basis selection
- no evidence bundle creation
- no actual `ScriptReadyScenePacket`
- no actual `ScriptReadyScenePacketCandidate`
- no packet candidate generation
- no source/story content invention
- no sample JSON
- no Kotlin/JVM source
- no runtime/CLI/schema/validator work
- no migration
- no direct SQLite writes
- no queryer implementation
- no provider/API
- no web/server
- no renderer/ffmpeg/TTS execution
- no media generation
- no package generation
- no QA execution against real media
- no artifact creation
- no 04/05/06/07/08 restart
- no Phase 20 / Phase 21 restart
- no approval, canon, asset approval, registry approval, release, final media
  readiness, final approval, provider/media handoff, or production lock
