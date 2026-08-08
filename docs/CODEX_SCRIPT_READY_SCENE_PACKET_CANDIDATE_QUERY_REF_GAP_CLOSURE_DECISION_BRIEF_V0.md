# Script-Ready Scene Packet Candidate Query Ref Gap Closure Decision Brief V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`ScriptReadyScenePacketCandidateQueryRefGapClosureDecisionBrief`

This document is docs/control only. It is not actual `QueryRequest`,
`QueryResponse`, or `QueryScopeReviewReport` instance creation, not
source/story invention, not promotion of Qingli candidates, not source-basis
selection, not reviewed evidence bundle creation, not an actual
`ScriptReadyScenePacket`, not an actual `ScriptReadyScenePacketCandidate`, not
sample JSON, not packet candidate generation, not route execution, not
runtime/CLI/schema/validator work, not provider/API work, not media generation,
and not production lock.

## Current Catalog Result

The current input is
`docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_UNRESOLVED_REF_SOURCE_CATALOG_V0.md`.

That reviewed catalog result is:

- partial Qingli-derived refs are only
  `CANDIDATE_EXISTING_SOURCE_REF_NEEDS_REVIEW`;
- `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport` refs remain
  `NO_EXISTING_SOURCE_REF_FOUND_AI_PLANNING_GAP`;
- no coherent source/ref set exists;
- no next future package candidate was named by the catalog.

This brief does not promote the partial Qingli-derived candidate refs, does not
select source basis, does not assemble an evidence bundle, and does not treat
any partial source candidates as sufficient for actual packet work.

## Reviewed Evidence Inputs

This decision brief cites only reviewed docs/control surfaces:

- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_UNRESOLVED_REF_SOURCE_CATALOG_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_REVIEWED_EVIDENCE_BUNDLE_REF_RESOLUTION_PACKET_V0.md`
- `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md`
- `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_ALIGNMENT_V0.md`
- `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

The demand-driven query contract/spec and templates define query surfaces and
minimum-scope review patterns. They do not provide concrete first-packet query
refs.

## Decision Outcomes

Allowed decision outcome labels are:

- `QUERY_REF_GAP_AI_RESOLVABLE_DOCS_CONTROL_ROUTE_AVAILABLE`
- `QUERY_REF_GAP_REQUIRES_USER_WORLDBUILDING`
- `QUERY_REF_GAP_BLOCKED_BY_BOUNDARY`
- `QUERY_REF_GAP_REMAINS_UNRESOLVED_AMBIGUOUS`

Selected decision outcome:

`QUERY_REF_GAP_AI_RESOLVABLE_DOCS_CONTROL_ROUTE_AVAILABLE`

Rationale: existing reviewed query docs define the required `QueryRequest`,
`QueryResponse`, and `QueryScopeReviewReport` surfaces well enough for a future
docs/control package to define reviewable query-ref evidence requirements. The
gap is still not resolved: no actual query instances, no concrete query refs,
no source-basis selection, and no evidence bundle are created here.

## Minimum Reviewed Query-Ref Evidence

A future query-ref evidence surface must provide all fields below before any
future source-basis selection can treat query refs as reviewed evidence.

### `QueryRequest`

Minimum reviewed evidence for a future `QueryRequest` slot:

- stable request id;
- route family and requesting packet surface;
- narrow information need, such as source scene id, zone id, beat id,
  character refs, or asset need refs;
- scope bounds and disallowed sources;
- cited reviewed docs/control source paths;
- visibility classification;
- `allowedPromptSurface` and forbidden prompt-surface fields;
- `protectedBoundaryRef` and generic `boundaryCategory` when hidden causality,
  culprit identity, sealed motive, director goal, future reveal, or unavailable
  actor knowledge must be protected;
- review status and reviewer evidence refs;
- explicit non-approval flags: no user approval, no asset approval, no registry
  approval, no canon, no release, no final media approval, no provider/media
  handoff, and no production lock.

### `QueryResponse`

Minimum reviewed evidence for a future `QueryResponse` slot:

- stable response id;
- matched `QueryRequest` id;
- returned refs or explicit missing-ref entries;
- per-ref citation paths;
- per-ref visibility and prompt-surface status;
- confirmation that placeholders, unpromoted samples, inferred story/source
  content, hidden author-only/review-only material, runtime database
  inspection, and direct SQLite writes were not used;
- `protectedBoundaryRef` and generic `boundaryCategory` where protected content
  is involved;
- review status and reviewer evidence refs;
- explicit non-approval flags: no source-basis selection, no evidence bundle
  creation, no actual packet generation, no media generation, no provider/API,
  no final approval, and no production lock.

### `QueryScopeReviewReport`

Minimum reviewed evidence for a future `QueryScopeReviewReport` slot:

- stable review report id;
- covered `QueryRequest` and `QueryResponse` ids;
- scope-safety checks;
- source-citation checks;
- missing-ref checks;
- prompt-surface leakage checks;
- protected-boundary checks;
- no invented id, fact, character, asset, or story-content checks;
- no runtime/schema/provider/media boundary checks;
- outcome and reviewer evidence refs;
- explicit non-approval flags: `REVIEW_PASS_NOT_APPROVAL` is technical only and
  is not user approval, asset approval, registry approval, canon, release,
  final media approval, provider/media handoff, final approval, or production
  lock.

## Future Package Candidate

Because the selected outcome permits a docs/control route, this brief names one
future classification-only package candidate:
`script_ready_scene_packet_candidate_query_ref_evidence_surface_definition_v0_docs_control`

That candidate would be allowed to define a future review surface for
query-ref evidence. It must not create actual `QueryRequest`, `QueryResponse`,
or `QueryScopeReviewReport` instances; must not resolve concrete refs; must not
select source basis; must not assemble an evidence bundle; must not create an
actual `ScriptReadyScenePacket` or `ScriptReadyScenePacketCandidate`; and must
not auto-start after this brief.

## Prompt Surface And Visibility Rules

Prompt surface is deny-by-default. Fields not explicitly listed in
`allowedPromptSurface` must not enter prompts, screen descriptions, negative
prompts, prompt-safe asset notes, or voice/audio planning notes.

Negative prompts are also prompt surface.

Any future query-ref evidence surface must define visibility for fields that
may later feed prompts, screen descriptions, negative prompts, prompt-safe asset
notes, or voice/audio planning notes. `author_only` and `review_only` material
must not be copied, inverted, summarized, hinted, or paraphrased into
prompt-facing fields.

Where notes must avoid leaking hidden causality, culprit identity, sealed
motive, director goal, future reveal, or unavailable actor knowledge, they must
use `protectedBoundaryRef` plus a generic `boundaryCategory` instead of copying
or paraphrasing protected content.

## Review Status Terms

- `DRAFT_FOR_REVIEW`: this decision brief is ready for technical review.
- `REVIEW_PASS_NOT_APPROVAL`: technical review passed, but this is not user
  approval, asset approval, registry approval, canon approval, release approval,
  final media approval, provider/media handoff, final approval, or production
  lock.
- `NEEDS_REVISION`: decision outcome, query-ref evidence minimums,
  prompt-surface rules, or boundary wording needs correction.
- `BLOCKED_BY_HUMAN_DECISION`: a human/user decision is required before future
  task classification.
- `BLOCKED_BY_BOUNDARY`: continuing would cross actual query instance
  creation, concrete ref resolution, source-basis selection, evidence bundle
  creation, actual packet generation, sample JSON/story content,
  runtime/schema, provider/API, media, approval, canon, registry, release, final
  approval, or production lock.

## Technical PASS Is Not Approval

`B03 PASS`, `03 PASS`, `QA_PASS_NOT_APPROVAL`, and
`REVIEW_PASS_NOT_APPROVAL` do not equal user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

A technical PASS on this brief does not create `QueryRequest`, `QueryResponse`,
or `QueryScopeReviewReport` instances, does not promote Qingli candidates, does
not resolve concrete refs, does not select source basis, does not create an
evidence bundle, does not create an actual `ScriptReadyScenePacket`, does not
create an actual `ScriptReadyScenePacketCandidate`, does not create sample JSON,
does not start story generation, does not start runtime/CLI/schema/validator
work, does not resume 04/05/06/07/08, does not run renderer/ffmpeg/TTS, does
not call provider/API, does not generate media, does not grant approval, does
not create canon, does not create registry approval, does not release, and does
not create production lock.

## Downstream Allowance

On B03 review PASS, B01/A01/00 may use this decision brief to classify a future
explicit task. PASS must not auto-start query instance creation, concrete ref
resolution, source-basis selection, evidence bundle creation, actual packet
generation, sample JSON creation, story content generation,
runtime/CLI/schema/validator work, 04/05/06/07/08 restart,
renderer/ffmpeg/TTS execution, provider/API work, media generation, approval,
canon, registry, release, or production lock.

## Boundary Summary

- docs/control only
- query-ref gap decision brief only
- no actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
  instance creation
- no promotion of Qingli candidates
- no concrete ref resolution
- no source-basis selection
- no reviewed evidence bundle creation
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
