# Script-Ready Scene Packet Candidate AI-Resolvable Gap Closure Path V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`ScriptReadyScenePacketCandidateAiResolvableGapClosurePath`

This document is docs/control only. It is not an actual
`ScriptReadyScenePacket`, not an actual `ScriptReadyScenePacketCandidate`, not a
sample JSON file, not source/story content invention, not source-basis
selection, not evidence bundle creation, not route execution, not
runtime/CLI/schema/validator work, not media generation, not provider/API work,
and not production lock.

## Current Reviewed Outcome

The current reviewed outcome is
`EXISTING_REVIEWED_EVIDENCE_BUNDLE_NOT_AVAILABLE` from
`docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_EXISTING_REVIEWED_EVIDENCE_BUNDLE_INVENTORY_V0.md`.

The reviewed inventory says existing reviewed docs provide rules, templates,
and prompt/protected-boundary constraints, but no reviewed evidence bundle ties
all required concrete refs together. The missing concrete refs are:

- source scene id / scene reference
- zone id / location reference
- beat id / beat reference
- character refs
- asset need refs
- `QueryRequest` refs
- `QueryResponse` refs
- `QueryScopeReviewReport` refs

The inventory classifies those missing concrete refs as AI-resolvable planning
gap items unless reviewed evidence later proves a user/worldbuilding blocker or
`BLOCKED_BY_BOUNDARY`.

## Gap Closure Outcome

Exact gap-closure outcome:
`AI_RESOLVABLE_GAP_CLOSURE_PATH_DEFINED_FOR_FUTURE_CLASSIFICATION`.

This outcome preserves the prior
`EXISTING_REVIEWED_EVIDENCE_BUNDLE_NOT_AVAILABLE` result and does not convert it
into source-basis selection, evidence bundle creation, packet generation, or
approval. It only defines how a future explicit package may close the missing
concrete-ref gap with reviewed citations.

## Reviewed Evidence Used

This path relies only on reviewed docs/control evidence. It does not promote
placeholder/sample values into concrete refs.

- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_EXISTING_REVIEWED_EVIDENCE_BUNDLE_INVENTORY_V0.md`
  records `EXISTING_REVIEWED_EVIDENCE_BUNDLE_NOT_AVAILABLE`.
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_REVIEWED_EVIDENCE_BUNDLE_SELECTION_V0.md`
  records
  `REVIEWED_EVIDENCE_BUNDLE_SELECTION_BLOCKED_BY_MISSING_REVIEWED_EVIDENCE`.
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_REVIEWED_EVIDENCE_BUNDLE_DEFINITION_V0.md`
  defines placeholder/reference fields only.
- `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md` defines the future packet surface.
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_ALIGNMENT_V0.md` maps query evidence to
  future packet sections without generating a packet.
- `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md` and
  `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md` define `QueryRequest`,
  `QueryResponse`, and `QueryScopeReviewReport` surfaces.
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md` defines
  `allowedPromptSurface`, `protectedBoundaryRef`, and generic
  `boundaryCategory` rules.

## Gap Closure Classification

Each missing concrete ref remains an AI-resolvable planning gap at this layer.
No reviewed evidence proves that the missing refs are true user/worldbuilding
blockers, and no reviewed evidence requires crossing forbidden boundaries to
define a future closure path.

| Missing ref | Current classification | Closure-path requirement |
| --- | --- | --- |
| source scene id / scene reference | AI-resolvable planning gap | A future package must cite one reviewed source scene id or explicitly keep it missing; it must not invent scene content. |
| zone id / location reference | AI-resolvable planning gap | A future package must cite one reviewed zone id or location ref tied to the source scene; it must not infer a location from placeholders. |
| beat id / beat reference | AI-resolvable planning gap | A future package must cite one reviewed beat id or beat ref tied to the source scene; it must not create story beats. |
| character refs | AI-resolvable planning gap | A future package must cite reviewed character refs relevant to the scene and visibility boundaries; it must not invent character facts. |
| asset need refs | AI-resolvable planning gap | A future package must cite reviewed asset need refs as candidate/planning refs only; it must not create approved assets or registry entries. |
| `QueryRequest` refs | AI-resolvable planning gap | A future package must cite reviewed `QueryRequest` refs or mark retrieval evidence missing; placeholder query samples are insufficient. |
| `QueryResponse` refs | AI-resolvable planning gap | A future package must cite reviewed `QueryResponse` refs or mark response evidence missing; it must not fabricate retrieval output. |
| `QueryScopeReviewReport` refs | AI-resolvable planning gap | A future package must cite reviewed `QueryScopeReviewReport` refs or mark review evidence missing; technical PASS remains non-approval. |

## Acceptance Criteria For Future Evidence Production

A future reviewed-evidence-producing package is acceptable only if it remains
docs/control or otherwise receives explicit classification before dispatch. It
must cite, review, and withhold the right surfaces before any source basis can
be considered.

Required citations:

- One source scene id / scene reference, or an explicit missing-ref record.
- One zone id / location reference, or an explicit missing-ref record.
- One beat id / beat reference, or an explicit missing-ref record.
- Relevant character refs, or an explicit missing-ref record.
- Asset need refs, or an explicit missing-ref record.
- `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport` refs when
  scoped retrieval is needed or available, or explicit missing-ref records.
- Continuity constraints, visibility boundaries, and reviewer evidence refs.
- `allowedPromptSurface`, forbidden prompt-surface rules,
  `protectedBoundaryRef`, and generic `boundaryCategory` where applicable.

Required review behavior:

- The package must show which cited refs are reviewed evidence and which remain
  unresolved.
- The package must keep hidden causality, culprit identity, sealed motive,
  director goal, future reveal, and unavailable actor knowledge behind
  `protectedBoundaryRef` plus generic `boundaryCategory`.
- The package must withhold any field not explicitly listed in
  `allowedPromptSurface` from prompts, screen descriptions, negative prompts,
  prompt-safe asset notes, and voice/audio planning notes.
- Negative prompts are prompt surface.
- The package must keep source-basis selection, actual packet generation,
  sample JSON creation, story content generation, runtime/CLI/schema/validator
  work, media generation, provider/API work, approval, canon, registry, release,
  final approval, and production lock out of scope.

## Exactly One Future Package Candidate

The single future package candidate named by this path is:

`script_ready_scene_packet_candidate_reviewed_evidence_bundle_ref_resolution_plan_v0_docs_control`

That future package candidate is classification-only. It may define how the
missing concrete refs will be resolved, cited, reviewed, and withheld from
prompt surface. It must not select the source basis, create the evidence bundle,
create an actual `ScriptReadyScenePacketCandidate`, create sample JSON, generate
story/source content, start runtime/schema work, call provider/API tools, run
renderer/ffmpeg/TTS, generate media, grant approval, create canon or registry
state, or claim production lock.

## Prompt Surface And Visibility Rules

Prompt surface is deny-by-default. Fields not explicitly listed in
`allowedPromptSurface` must not enter prompts, screen descriptions, negative
prompts, prompt-safe asset notes, or voice/audio planning notes.

Future gap-closure and evidence-producing packages must define visibility for
each field that might later feed a prompt, screen description, negative prompt,
prompt-safe asset note, or voice/audio planning note. `author_only` and
`review_only` material must not be copied or paraphrased into prompt-facing
surfaces.

Where notes must avoid leaking hidden causality, culprit identity, sealed
motive, director goal, future reveal, or unavailable actor knowledge, they must
use `protectedBoundaryRef` plus a generic `boundaryCategory` instead of copying
or paraphrasing the protected content.

## Review Status Terms

- `DRAFT_FOR_REVIEW`: This control document or a future package is ready for
  technical review.
- `REVIEW_PASS_NOT_APPROVAL`: Technical review passed, but this is not user
  approval, asset approval, registry approval, canon approval, release approval,
  final media approval, provider/media handoff, final approval, or production
  lock.
- `NEEDS_REVISION`: The control surface is incomplete or internally
  inconsistent.
- `BLOCKED_BY_HUMAN_DECISION`: A true human/user decision is required before the
  route can continue.
- `BLOCKED_BY_BOUNDARY`: Continuing would cross source/story invention, actual
  packet generation, runtime/CLI/schema/validator work, provider/API, media,
  approval, canon, registry, release, final approval, or production lock
  boundaries.

## Downstream Allowance

On future B03 review PASS, B01/A01/00 may use this gap closure path to classify
a future explicit task. PASS must not auto-start source-basis selection, actual
packet generation, sample JSON creation, story content generation,
runtime/CLI/schema/validator work, 04/05/06/07/08 restart,
renderer/ffmpeg/TTS execution, provider/API work, media generation, approval,
canon, registry, release, or production lock.

## Boundary Summary

- Docs/control only.
- No source/story content invention.
- No actual `ScriptReadyScenePacket`.
- No actual `ScriptReadyScenePacketCandidate`.
- No sample JSON, packet candidate, source-basis selection, evidence bundle
  creation, route execution, or artifact creation.
- No Kotlin/JVM source, runtime/CLI/schema/validator work, migration,
  direct SQLite writes, queryer implementation, provider/API, web/server,
  renderer/ffmpeg/TTS, media/image/audio/video generation, package generation,
  or QA execution against real media.
- No 04/05/06/07/08 restart and no Phase 20 / Phase 21 restart.
- No approval, canon, asset approval, registry approval, release, final media
  readiness, final approval, provider/media handoff, or production lock.
