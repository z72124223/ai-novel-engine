# Script-Ready Scene Packet Candidate Query Ref Source Doc Reviewed Input Gap Matrix V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`ScriptReadyScenePacketCandidateQueryRefSourceDocReviewedInputGapMatrix`

This document is docs/control only. It classifies the reviewed input gap state
for future reviewed query-ref source docs. It does not create actual
`QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport` instances, does
not execute queries, does not implement a queryer, does not resolve concrete
refs, does not select source basis, does not create evidence bundles, does not
create an actual `ScriptReadyScenePacket`, does not create an actual
`ScriptReadyScenePacketCandidate`, does not generate sample JSON, does not
invent story/source content, does not promote Qingli candidates, does not start
runtime/schema/provider/API/media work, does not run renderer/ffmpeg/TTS, does
not grant approval, and does not create production lock.

## Upstream Reviewed Outcome

This matrix follows:
`docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_PLACEHOLDER_PACKET_V0.md`.

Upstream reviewed outcome:
`QUERY_REF_SOURCE_DOC_PLACEHOLDER_PACKET_READY_FOR_REVIEW`.

Upstream route/context labels preserved here:

- `QUERY_REF_SOURCE_DOC_DEFINITION_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_GAP_CLOSURE_ROUTE_DEFINED_FOR_FUTURE_SOURCE_DOCS`
- `BLOCKED_BY_MISSING_REVIEWED_QUERY_REF_SOURCE_DOCS`
- `QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_BLOCKED_BY_MISSING_REVIEWED_REFS`
- `NO_REVIEWED_SOURCE_FOUND_AI_PLANNING_GAP`

The placeholder packet provides reviewed placeholder/reference surfaces. It
does not provide concrete reviewed source basis, concrete reviewed
docs/control citations, actual query instances, or selected source basis.

## Referenced Reviewed Inputs

This matrix relies on these reviewed docs/control inputs:

- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_PLACEHOLDER_PACKET_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_DEFINITION_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_GAP_CLOSURE_ROUTE_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_REVIEWED_REF_DISCOVERY_MATRIX_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_EVIDENCE_SURFACE_DEFINITION_V0.md`
- `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md`
- `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md`
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

Citation rule: every future reviewed query-ref source doc must cite exact
docs/control paths. Template paths and placeholder packet sections may define
shape, but they are not concrete reviewed source-doc citations by themselves.

## Classification Vocabulary

Each future source-doc slot must have exactly one slot-level classification:

- `REVIEWED_INPUTS_DEFINED_FOR_FUTURE_DRAFT`: reviewed docs/control evidence
  defines all required input categories well enough for a future draft task,
  without filling concrete content.
- `REVIEWED_INPUTS_MISSING_AI_PLANNING_GAP`: required categories are known,
  but one or more concrete reviewed inputs remain missing and are still an
  AI-resolvable planning gap.
- `REVIEWED_INPUTS_REQUIRE_USER_WORLDBUILDING`: reviewed evidence proves user
  worldbuilding is required before this slot can proceed.
- `REVIEWED_INPUTS_BOUNDARY_BLOCKED`: continuing would cross forbidden
  boundaries such as actual query instance creation, query execution, concrete
  ref resolution, source-basis selection, evidence bundle creation, actual
  packet generation, sample JSON/story content, runtime/schema, provider/API,
  renderer/ffmpeg/TTS, media generation, approval, canon, registry, release,
  final approval, or production lock.

## Current Matrix Outcome

Allowed current reviewed-input-gap matrix outcome labels are:

- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_MATRIX_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_MATRIX_BLOCKED_BY_MISSING_REVIEWED_INPUTS`
- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_MATRIX_BLOCKED_BY_BOUNDARY`
- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_MATRIX_REQUIRES_USER_WORLDBUILDING`

Selected current matrix outcome:

`QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_MATRIX_READY_FOR_REVIEW`

Rationale: the matrix only classifies reviewed input gaps. It does not fill
the gaps. The reviewed placeholder packet provides enough surface structure to
review this matrix, while all three future source-doc slots still need future
AI-governed planning to close concrete reviewed-input gaps.

The single future bounded package candidate is
`script_ready_scene_packet_candidate_query_ref_source_doc_reviewed_input_gap_closure_plan_v0_docs_control`.

That candidate is classification-only. It must not auto-start and must not be
treated as approval.

## Slot-Level Classifications

| Future source-doc slot | Slot-level classification | Reason |
| --- | --- | --- |
| `QueryRequest` source-doc reviewed-input gaps | `REVIEWED_INPUTS_MISSING_AI_PLANNING_GAP` | The placeholder packet defines a `QueryRequest` section, but concrete future source basis and concrete reviewed docs/control citations are still missing. |
| `QueryResponse` source-doc reviewed-input gaps | `REVIEWED_INPUTS_MISSING_AI_PLANNING_GAP` | The placeholder packet defines a `QueryResponse` section, but concrete response evidence citations and a reviewed request-source linkage are still missing. |
| `QueryScopeReviewReport` source-doc reviewed-input gaps | `REVIEWED_INPUTS_MISSING_AI_PLANNING_GAP` | The placeholder packet defines a `QueryScopeReviewReport` section, but reviewed request/response source links and concrete review evidence are still missing. |

Every query-ref source-doc slot above has exactly one slot-level
classification.

## Input Category Matrix

For each category, "current packet state" records whether the reviewed
placeholder packet already provides only a placeholder/reference surface, or
whether future reviewed input remains missing.

### `QueryRequest` Source-Doc Reviewed-Input Gaps

Slot-level classification:
`REVIEWED_INPUTS_MISSING_AI_PLANNING_GAP`.

| Required input category | Current packet state | Gap classification |
| --- | --- | --- |
| Future source basis requirement | Placeholder/reference surface exists; concrete reviewed source basis is missing. | AI-resolvable planning gap |
| Reviewed docs/control citation requirement | Placeholder exists; exact future citation remains missing. | AI-resolvable planning gap |
| Visibility classification requirement | Placeholder visibility choices exist. | Defined for future draft |
| `allowedPromptSurface` eligibility requirement | Placeholder eligibility exists; concrete allowed fields remain future-review material. | AI-resolvable planning gap |
| `protectedBoundaryRef` requirement | Placeholder exists. | Defined for future draft |
| Generic `boundaryCategory` requirement | Placeholder category set exists. | Defined for future draft |
| Review gate/status requirement (`DRAFT_FOR_REVIEW`, `REVIEW_PASS_NOT_APPROVAL`) | Status gate exists. | Defined for future draft |
| Rejection/blocker conditions (`BLOCKED_BY_HUMAN_DECISION`, `BLOCKED_BY_BOUNDARY`) | Rejection condition labels exist. | Defined for future draft |

### `QueryResponse` Source-Doc Reviewed-Input Gaps

Slot-level classification:
`REVIEWED_INPUTS_MISSING_AI_PLANNING_GAP`.

| Required input category | Current packet state | Gap classification |
| --- | --- | --- |
| Future source basis requirement | Placeholder/reference surface exists; concrete reviewed source basis is missing. | AI-resolvable planning gap |
| Reviewed docs/control citation requirement | Placeholder exists; exact future citation remains missing. | AI-resolvable planning gap |
| Visibility classification requirement | Placeholder visibility choices exist. | Defined for future draft |
| `allowedPromptSurface` eligibility requirement | Placeholder eligibility exists; concrete allowed fields remain future-review material. | AI-resolvable planning gap |
| `protectedBoundaryRef` requirement | Placeholder exists. | Defined for future draft |
| Generic `boundaryCategory` requirement | Placeholder category set exists. | Defined for future draft |
| Review gate/status requirement (`DRAFT_FOR_REVIEW`, `REVIEW_PASS_NOT_APPROVAL`) | Status gate exists. | Defined for future draft |
| Rejection/blocker conditions (`BLOCKED_BY_HUMAN_DECISION`, `BLOCKED_BY_BOUNDARY`) | Rejection condition labels exist. | Defined for future draft |

### `QueryScopeReviewReport` Source-Doc Reviewed-Input Gaps

Slot-level classification:
`REVIEWED_INPUTS_MISSING_AI_PLANNING_GAP`.

| Required input category | Current packet state | Gap classification |
| --- | --- | --- |
| Future source basis requirement | Placeholder/reference surface exists; concrete reviewed source basis is missing. | AI-resolvable planning gap |
| Reviewed docs/control citation requirement | Placeholder exists; exact future citation remains missing. | AI-resolvable planning gap |
| Visibility classification requirement | Placeholder visibility choices exist. | Defined for future draft |
| `allowedPromptSurface` eligibility requirement | Placeholder eligibility exists; concrete allowed fields remain future-review material. | AI-resolvable planning gap |
| `protectedBoundaryRef` requirement | Placeholder exists. | Defined for future draft |
| Generic `boundaryCategory` requirement | Placeholder category set exists. | Defined for future draft |
| Review gate/status requirement (`DRAFT_FOR_REVIEW`, `REVIEW_PASS_NOT_APPROVAL`) | Status gate exists. | Defined for future draft |
| Rejection/blocker conditions (`BLOCKED_BY_HUMAN_DECISION`, `BLOCKED_BY_BOUNDARY`) | Rejection condition labels exist. | Defined for future draft |

## Actual Instance Exclusion

This reviewed input gap matrix does not create actual `QueryRequest`,
`QueryResponse`, or `QueryScopeReviewReport` instances.

It also does not execute queries, implement a queryer, resolve concrete refs,
select source basis, create evidence bundles, create actual
`ScriptReadyScenePacket` or `ScriptReadyScenePacketCandidate` content, generate
sample JSON, invent story/source content, promote Qingli candidates, or start
runtime/schema/provider/API/media work.

## Prompt Surface And Visibility Rules

Prompt surface is deny-by-default. Fields not explicitly listed in
`allowedPromptSurface` must not enter prompts, screen descriptions, negative
prompts, prompt-safe asset notes, voice/audio planning notes, or media planning
notes.

Negative prompts are also prompt surface.

Every future reviewed input field must define visibility before it can feed
prompts, screen descriptions, negative prompts, prompt-safe asset notes,
voice/audio planning notes, or media planning notes.

`author_only` and `review_only` material must not be copied, inverted,
summarized, hinted, or paraphrased into prompt-facing fields. Where a future
field must avoid hidden causality, culprit identity, sealed motive, director
goal, future reveal, or unavailable actor knowledge, it must use
`protectedBoundaryRef` plus generic `boundaryCategory` instead of exposing the
protected fact.

## Technical PASS Is Not Approval

`B03 PASS`, `03 PASS`, `QA_PASS_NOT_APPROVAL`, and
`REVIEW_PASS_NOT_APPROVAL` do not equal user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

A technical PASS on this matrix does not create `QueryRequest`,
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

On B03 review PASS, B01/A01/00 may use this matrix to classify or dispatch a
future explicit docs/control task. PASS must not auto-start actual query
instance creation, query execution, concrete ref resolution, source-basis
selection, evidence bundle creation, actual packet generation, sample JSON
creation, story content generation, runtime/CLI/schema/validator work,
04/05/06/07/08 restart, renderer/ffmpeg/TTS execution, provider/API work, media
generation, approval, canon, registry, release, final approval, or production
lock.

## Boundary Summary

- docs/control only
- reviewed input gap matrix only
- no actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
  instance creation
- no query execution or queryer implementation
- no concrete query-ref resolution
- no source/story invention
- no Qingli promotion
- no source-basis selection
- no evidence bundle creation
- no actual `ScriptReadyScenePacket`
- no actual `ScriptReadyScenePacketCandidate`
- no sample JSON
- no packet candidate generation
- no route execution
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
- no approval
- no canon
- no asset approval
- no registry approval
- no release
- no final media readiness
- no final approval
- no production lock
