# Script-Ready Scene Packet Candidate Query Ref Evidence Placeholder Packet V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`ScriptReadyScenePacketCandidateQueryRefEvidencePlaceholderPacket`

This document is docs/control only. It assembles a placeholder/reference-only
packet surface for future query-ref evidence review. It does not contain actual
`QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport` instances. It is
not query execution, not queryer implementation, not concrete query-ref
resolution, not source/story invention, not promotion of Qingli candidates, not
source-basis selection, not reviewed evidence bundle creation, not an actual
`ScriptReadyScenePacket`, not an actual `ScriptReadyScenePacketCandidate`, not
sample JSON, not packet candidate generation, not route execution, not
runtime/CLI/schema/validator work, not provider/API work, not media generation,
and not production lock.

## Upstream Reviewed Outcomes

This placeholder packet follows:
`docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_EVIDENCE_SURFACE_DEFINITION_V0.md`.

Upstream outcomes preserved here:

- `QUERY_REF_GAP_AI_RESOLVABLE_DOCS_CONTROL_ROUTE_AVAILABLE`
- `QUERY_REF_EVIDENCE_SURFACE_DEFINED_FOR_FUTURE_REVIEW`
- `CANDIDATE_EXISTING_SOURCE_REF_NEEDS_REVIEW`
- `NO_EXISTING_SOURCE_REF_FOUND_AI_PLANNING_GAP`

Qingli-derived refs remain unpromoted candidates. Query refs remain missing.
This packet does not promote candidates, select source basis, assemble an
evidence bundle, or treat partial source candidates as sufficient for actual
packet work.

## Reviewed Evidence Inputs

This packet cites only reviewed docs/control surfaces:

- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_EVIDENCE_SURFACE_DEFINITION_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_GAP_CLOSURE_DECISION_BRIEF_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_UNRESOLVED_REF_SOURCE_CATALOG_V0.md`
- `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md`
- `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_ALIGNMENT_V0.md`
- `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

These sources define rules, templates, and prompt/protected-boundary
constraints. They do not provide reviewed concrete `QueryRequest`,
`QueryResponse`, or `QueryScopeReviewReport` refs for the future first
script-ready scene packet candidate.

## Packet Outcome

Allowed placeholder-packet outcome labels are:

- `QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_READY_FOR_REVIEW`
- `QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_BLOCKED_BY_MISSING_REVIEWED_REFS`
- `QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_BLOCKED_BY_BOUNDARY`
- `QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_REQUIRES_USER_WORLDBUILDING`

Selected placeholder-packet outcome:

`QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_BLOCKED_BY_MISSING_REVIEWED_REFS`

Rationale: the packet surface can be described for technical review, but all
three required query-ref slots still lack concrete reviewed docs/control
citations. The missing refs are not resolved by this packet, and this packet
does not name a future package candidate because doing so would be speculative
while all query-ref slots remain unresolved.

## Packet Metadata Surface

The packet surface contains only placeholder/reference-only metadata:

- `packetId`: placeholder id for this docs/control packet surface;
- `packetVersion`: version label for this packet surface;
- `routeFamily`: expected value `script_ready_scene_packet_candidate`;
- `upstreamReviewedOutcomes`: the reviewed outcomes listed above;
- `packetStatus`: `DRAFT_FOR_REVIEW` until technical review;
- `packetOutcome`: selected outcome above;
- `nonApprovalStatus`: `REVIEW_PASS_NOT_APPROVAL` if technical review passes;
- `slotSurfaces`: placeholder surfaces for the three query-ref slots;
- `rejectionRules`: rules for keeping unsafe material unresolved;
- `boundarySummary`: explicit non-goals and stop conditions.

These fields are not actual query evidence instances and do not resolve any
query-ref slot.

## Query-Ref Slot Surfaces

Each slot below is explicitly unresolved because no reviewed concrete citation
exists in already reviewed docs/control evidence.

| Slot | Placeholder fields | Current slot state | Unresolved reason |
| --- | --- | --- | --- |
| `QueryRequest` refs | slot name; intended future use; required source doc path / reviewed docs-control path placeholder; reviewed status / review gate placeholder; `allowedPromptSurface` eligibility placeholder; `protectedBoundaryRef` placeholder; generic `boundaryCategory` placeholder; `DRAFT_FOR_REVIEW` / `REVIEW_PASS_NOT_APPROVAL` status placeholder | unresolved | Reviewed docs define `QueryRequest` rules and templates, but no reviewed concrete `QueryRequest` ref exists for the future first packet. |
| `QueryResponse` refs | slot name; intended future use; required source doc path / reviewed docs-control path placeholder; reviewed status / review gate placeholder; `allowedPromptSurface` eligibility placeholder; `protectedBoundaryRef` placeholder; generic `boundaryCategory` placeholder; `DRAFT_FOR_REVIEW` / `REVIEW_PASS_NOT_APPROVAL` status placeholder | unresolved | Reviewed docs define `QueryResponse` rules and templates, but no reviewed concrete `QueryResponse` ref exists for the future first packet. |
| `QueryScopeReviewReport` refs | slot name; intended future use; required source doc path / reviewed docs-control path placeholder; reviewed status / review gate placeholder; `allowedPromptSurface` eligibility placeholder; `protectedBoundaryRef` placeholder; generic `boundaryCategory` placeholder; `DRAFT_FOR_REVIEW` / `REVIEW_PASS_NOT_APPROVAL` status placeholder | unresolved | Reviewed docs define `QueryScopeReviewReport` rules and templates, but no reviewed concrete `QueryScopeReviewReport` ref exists for the future first packet. |

## Actual Instance Exclusion

This packet does not contain actual `QueryRequest`, `QueryResponse`, or
`QueryScopeReviewReport` instances.

Placeholder fields must not be interpreted as:

- query instance ids;
- executable query requests;
- returned query responses;
- query scope review reports;
- concrete source-basis refs;
- reviewed evidence bundle entries;
- actual `ScriptReadyScenePacket` content;
- actual `ScriptReadyScenePacketCandidate` content.

## Rejection Rules

This packet must reject or keep unresolved any query-ref material depending on:

- unreviewed placeholders;
- invented story/source content;
- hidden `author_only` or `review_only` material copied or paraphrased into
  prompt surface;
- runtime database inspection;
- direct SQLite writes;
- provider/API output;
- renderer/ffmpeg/TTS output;
- media artifacts;
- Qingli candidates not explicitly reviewed/promoted;
- samples not explicitly reviewed and promoted by docs/control evidence;
- any claim of approval, canon, asset approval, registry approval, release,
  final media approval, provider/media handoff, final approval, or production
  lock.

Rejected material remains unresolved. It must not be silently promoted into
query refs.

## Future Package Candidate

No future package candidate is named because the selected packet outcome is
`QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_BLOCKED_BY_MISSING_REVIEWED_REFS`.

B01/A01/00 may use this packet after B03 review to decide whether the next
route should request human/user input, define another docs/control closure path,
or stop on the missing reviewed refs. This packet itself does not make that
decision and must not auto-start any follow-up task.

## Prompt Surface And Visibility Rules

Prompt surface is deny-by-default. Fields not explicitly listed in
`allowedPromptSurface` must not enter prompts, screen descriptions, negative
prompts, prompt-safe asset notes, or voice/audio planning notes.

Negative prompts are also prompt surface.

Any future query-ref packet or downstream route must define visibility for
fields that may later feed prompts, screen descriptions, negative prompts,
prompt-safe asset notes, or voice/audio planning notes. `author_only` and
`review_only` material must not be copied, inverted, summarized, hinted, or
paraphrased into prompt-facing fields.

Where notes must avoid leaking hidden causality, culprit identity, sealed
motive, director goal, future reveal, or unavailable actor knowledge, they must
use `protectedBoundaryRef` plus a generic `boundaryCategory` instead of copying
or paraphrasing protected content.

## Review Status Terms

- `DRAFT_FOR_REVIEW`: this placeholder packet is ready for technical review.
- `REVIEW_PASS_NOT_APPROVAL`: technical review passed, but this is not user
  approval, asset approval, registry approval, canon approval, release approval,
  final media approval, provider/media handoff, final approval, or production
  lock.
- `NEEDS_REVISION`: placeholder fields, unresolved reasons, rejection rules,
  prompt-surface rules, or boundary wording needs correction.
- `BLOCKED_BY_HUMAN_DECISION`: a human/user decision is required before future
  task classification.
- `BLOCKED_BY_BOUNDARY`: continuing would cross actual query instance
  creation, query execution, concrete ref resolution, source-basis selection,
  evidence bundle creation, actual packet generation, sample JSON/story
  content, runtime/schema, provider/API, media, approval, canon, registry,
  release, final approval, or production lock.

## Technical PASS Is Not Approval

`B03 PASS`, `03 PASS`, `QA_PASS_NOT_APPROVAL`, and
`REVIEW_PASS_NOT_APPROVAL` do not equal user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

A technical PASS on this packet does not create `QueryRequest`,
`QueryResponse`, or `QueryScopeReviewReport` instances, does not execute a
query, does not implement a queryer, does not promote Qingli candidates, does
not resolve concrete refs, does not select source basis, does not create an
evidence bundle, does not create an actual `ScriptReadyScenePacket`, does not
create an actual `ScriptReadyScenePacketCandidate`, does not create sample JSON,
does not start story generation, does not start runtime/CLI/schema/validator
work, does not resume 04/05/06/07/08, does not run renderer/ffmpeg/TTS, does
not call provider/API, does not generate media, does not grant approval, does
not create canon, does not create registry approval, does not release, and does
not create production lock.

## Downstream Allowance

On B03 review PASS, B01/A01/00 may use this placeholder packet to classify a
future explicit task or blocker. PASS must not auto-start actual query instance
creation, query execution, concrete ref resolution, source-basis selection,
evidence bundle creation, actual packet generation, sample JSON creation, story
content generation, runtime/CLI/schema/validator work, 04/05/06/07/08 restart,
renderer/ffmpeg/TTS execution, provider/API work, media generation, approval,
canon, registry, release, or production lock.

## Boundary Summary

- docs/control only
- query-ref evidence placeholder packet only
- placeholder/reference-only metadata
- no actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
  instance creation
- no query execution or queryer implementation
- no concrete query-ref resolution
- no promotion of Qingli candidates
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
