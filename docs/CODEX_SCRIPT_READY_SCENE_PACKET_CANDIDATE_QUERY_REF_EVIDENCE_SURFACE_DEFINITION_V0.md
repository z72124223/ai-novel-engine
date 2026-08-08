# Script-Ready Scene Packet Candidate Query Ref Evidence Surface Definition V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`ScriptReadyScenePacketCandidateQueryRefEvidenceSurfaceDefinition`

This document is docs/control only. It defines a reviewable evidence surface,
not actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
instances. It is not query execution, not queryer implementation, not
source/story invention, not promotion of Qingli candidates, not source-basis
selection, not reviewed evidence bundle creation, not an actual
`ScriptReadyScenePacket`, not an actual `ScriptReadyScenePacketCandidate`, not
sample JSON, not packet candidate generation, not route execution, not
runtime/CLI/schema/validator work, not provider/API work, not media generation,
and not production lock.

## Upstream Reviewed Outcomes

This surface definition follows the reviewed decision brief:
`docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_GAP_CLOSURE_DECISION_BRIEF_V0.md`.

Upstream outcomes preserved here:

- `QUERY_REF_GAP_AI_RESOLVABLE_DOCS_CONTROL_ROUTE_AVAILABLE`
- `CANDIDATE_EXISTING_SOURCE_REF_NEEDS_REVIEW`
- `NO_EXISTING_SOURCE_REF_FOUND_AI_PLANNING_GAP`

Qingli-derived refs remain unpromoted candidates. Query refs remain missing
until a future explicit task produces reviewed placeholder/reference-only
evidence for them. This definition does not promote candidates, select source
basis, assemble an evidence bundle, or treat partial source candidates as
sufficient for actual packet work.

## Reviewed Evidence Inputs

This definition cites only reviewed docs/control surfaces:

- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_GAP_CLOSURE_DECISION_BRIEF_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_UNRESOLVED_REF_SOURCE_CATALOG_V0.md`
- `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md`
- `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_ALIGNMENT_V0.md`
- `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

These sources define rules, templates, and prompt/protected-boundary
constraints. They do not supply concrete first-packet query refs.

## Definition Outcome

Allowed current definition outcome labels are:

- `QUERY_REF_EVIDENCE_SURFACE_DEFINED_FOR_FUTURE_REVIEW`
- `QUERY_REF_EVIDENCE_SURFACE_BLOCKED_BY_MISSING_RULES`
- `QUERY_REF_EVIDENCE_SURFACE_BLOCKED_BY_BOUNDARY`
- `QUERY_REF_EVIDENCE_SURFACE_REQUIRES_USER_WORLDBUILDING`

Selected current definition outcome:

`QUERY_REF_EVIDENCE_SURFACE_DEFINED_FOR_FUTURE_REVIEW`

Rationale: reviewed query contract/spec and template docs already define the
minimum fields, review posture, and boundary rules needed to describe a future
query-ref evidence surface. The concrete refs remain unresolved, and this
definition does not create actual query evidence instances.

## Surface-Level Metadata

Every future query-ref evidence surface must include placeholder/reference-only
metadata:

- `surfaceId`: stable id for the surface under review;
- `surfaceVersion`: version label for this docs/control surface;
- `routeFamily`: expected value `script_ready_scene_packet_candidate`;
- `upstreamReviewedOutcomes`: references to the reviewed outcomes above;
- `sourceDocPath`: reviewed docs/control source path for each cited field;
- `reviewedStatus`: current status such as `DRAFT_FOR_REVIEW` or
  `REVIEW_PASS_NOT_APPROVAL`;
- `reviewGate`: expected technical review gate;
- `slotName`: one of `QueryRequest refs`, `QueryResponse refs`, or
  `QueryScopeReviewReport refs`;
- `intendedUse`: why the future script-ready scene packet candidate route needs
  the slot;
- `visibility`: whether the field is control-only, review-only, or potentially
  prompt-facing;
- `allowedPromptSurface`: explicit prompt-surface allowance if the field may
  later feed prompts, screen descriptions, negative prompts, prompt-safe asset
  notes, or voice/audio planning notes;
- `forbiddenPromptSurface`: fields that must not enter prompt-facing surfaces;
- `protectedBoundaryRef`: reference to protected boundary guidance when needed;
- `boundaryCategory`: generic category for protected hidden or unavailable
  information;
- `nonApprovalStatus`: explicit non-approval flags.

These fields are metadata about future query-ref evidence. They are not
`QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport` instances.

## `QueryRequest` Ref Surface

Future `QueryRequest` refs must provide placeholder/reference-only metadata for:

- request placeholder id, not an actual query instance id;
- source doc path or reviewed docs/control source path;
- reviewed status / review gate;
- slot name and intended use;
- requested information need, bounded to source scene id, zone id, beat id,
  character refs, asset need refs, or query evidence linkage;
- allowed evidence source categories;
- disallowed evidence source categories;
- visibility and prompt-surface eligibility via `allowedPromptSurface` where
  relevant;
- `protectedBoundaryRef` plus generic `boundaryCategory` when the request must
  avoid leaking hidden causality, culprit identity, sealed motive, director
  goal, future reveal, or unavailable actor knowledge;
- explicit non-approval status such as `DRAFT_FOR_REVIEW` and
  `REVIEW_PASS_NOT_APPROVAL`.

## `QueryResponse` Ref Surface

Future `QueryResponse` refs must provide placeholder/reference-only metadata
for:

- response placeholder id, not an actual query instance id;
- linked request placeholder id;
- source doc path or reviewed docs/control source path;
- reviewed status / review gate;
- slot name and intended use;
- returned-ref placeholder list or unresolved-ref list;
- citation requirements for every returned or unresolved ref;
- per-ref visibility and prompt-surface eligibility via `allowedPromptSurface`
  where relevant;
- `protectedBoundaryRef` plus generic `boundaryCategory` when response notes
  must avoid hidden or unavailable information leakage;
- explicit non-approval status such as `DRAFT_FOR_REVIEW` and
  `REVIEW_PASS_NOT_APPROVAL`.

## `QueryScopeReviewReport` Ref Surface

Future `QueryScopeReviewReport` refs must provide placeholder/reference-only
metadata for:

- report placeholder id, not an actual query review instance id;
- linked request and response placeholder ids;
- source doc path or reviewed docs/control source path;
- reviewed status / review gate;
- slot name and intended use;
- scope-safety checks;
- source-citation checks;
- missing-ref checks;
- prompt-surface leakage checks;
- protected-boundary checks using `protectedBoundaryRef` and generic
  `boundaryCategory` where needed;
- no invented id/fact/character/asset/story-content checks;
- no runtime/schema/provider/media boundary checks;
- explicit non-approval status such as `DRAFT_FOR_REVIEW` and
  `REVIEW_PASS_NOT_APPROVAL`.

## Rejection Rules

A future evidence surface must reject or keep unresolved any query-ref material
that depends on:

- unreviewed placeholders;
- invented story/source content;
- hidden `author_only` or `review_only` material copied or paraphrased into
  prompt surface;
- runtime database inspection;
- direct SQLite writes;
- provider/API output;
- renderer/ffmpeg/TTS output;
- media artifacts;
- samples not explicitly reviewed and promoted by docs/control evidence;
- Qingli-derived candidates treated as selected source basis;
- any claim of approval, canon, asset approval, registry approval, release,
  final media approval, provider/media handoff, final approval, or production
  lock.

Rejected material must remain unresolved. It must not be silently promoted into
query refs.

## Future Package Candidate

Because the selected outcome permits future docs/control-only query-ref evidence
work, this definition names one future classification-only package candidate:
`script_ready_scene_packet_candidate_query_ref_evidence_placeholder_packet_v0_docs_control`

That candidate may define a reviewable placeholder packet for query-ref
evidence, but it must not create actual `QueryRequest`, `QueryResponse`, or
`QueryScopeReviewReport` instances; must not execute a query; must not resolve
concrete refs; must not promote Qingli candidates; must not select source
basis; must not assemble a reviewed evidence bundle; must not create an actual
`ScriptReadyScenePacket` or `ScriptReadyScenePacketCandidate`; and must not
auto-start after this definition.

## Prompt Surface And Visibility Rules

Prompt surface is deny-by-default. Fields not explicitly listed in
`allowedPromptSurface` must not enter prompts, screen descriptions, negative
prompts, prompt-safe asset notes, or voice/audio planning notes.

Negative prompts are also prompt surface.

Any future query-ref evidence packet must define visibility for fields that may
later feed prompts, screen descriptions, negative prompts, prompt-safe asset
notes, or voice/audio planning notes. `author_only` and `review_only` material
must not be copied, inverted, summarized, hinted, or paraphrased into
prompt-facing fields.

Where notes must avoid leaking hidden causality, culprit identity, sealed
motive, director goal, future reveal, or unavailable actor knowledge, they must
use `protectedBoundaryRef` plus a generic `boundaryCategory` instead of copying
or paraphrasing protected content.

## Review Status Terms

- `DRAFT_FOR_REVIEW`: this surface definition is ready for technical review.
- `REVIEW_PASS_NOT_APPROVAL`: technical review passed, but this is not user
  approval, asset approval, registry approval, canon approval, release approval,
  final media approval, provider/media handoff, final approval, or production
  lock.
- `NEEDS_REVISION`: surface fields, rejection rules, prompt-surface rules, or
  boundary wording needs correction.
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

A technical PASS on this definition does not create `QueryRequest`,
`QueryResponse`, or `QueryScopeReviewReport` instances, does not execute a
query, does not promote Qingli candidates, does not resolve concrete refs, does
not select source basis, does not create an evidence bundle, does not create an
actual `ScriptReadyScenePacket`, does not create an actual
`ScriptReadyScenePacketCandidate`, does not create sample JSON, does not start
story generation, does not start runtime/CLI/schema/validator work, does not
resume 04/05/06/07/08, does not run renderer/ffmpeg/TTS, does not call
provider/API, does not generate media, does not grant approval, does not create
canon, does not create registry approval, does not release, and does not create
production lock.

## Downstream Allowance

On B03 review PASS, B01/A01/00 may use this surface definition to classify a
future explicit task. PASS must not auto-start actual query instance creation,
concrete ref resolution, source-basis selection, evidence bundle creation,
actual packet generation, sample JSON creation, story content generation,
runtime/CLI/schema/validator work, 04/05/06/07/08 restart,
renderer/ffmpeg/TTS execution, provider/API work, media generation, approval,
canon, registry, release, or production lock.

## Boundary Summary

- docs/control only
- query-ref evidence surface definition only
- placeholder/reference-only metadata
- no actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
  instance creation
- no query execution or queryer implementation
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
