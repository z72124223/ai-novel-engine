# Script-Ready Scene Packet Candidate Query Ref Source Doc Reviewed Input Gap Closure Plan V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`ScriptReadyScenePacketCandidateQueryRefSourceDocReviewedInputGapClosurePlan`

This document is docs/control only. It defines a closure plan for
AI-resolvable reviewed-input gaps before future reviewed query-ref source docs.
It does not create actual `QueryRequest`, `QueryResponse`, or
`QueryScopeReviewReport` instances, does not execute queries, does not
implement a queryer, does not resolve concrete refs, does not select source
basis, does not create evidence bundles, does not create an actual
`ScriptReadyScenePacket`, does not create an actual
`ScriptReadyScenePacketCandidate`, does not generate sample JSON, does not
invent story/source/query content, does not promote Qingli candidates, does not
start runtime/schema/provider/API/media work, does not run renderer/ffmpeg/TTS,
does not grant approval, and does not create production lock.

## Upstream Reviewed Outcome

This closure plan follows:
`docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_MATRIX_V0.md`.

Upstream reviewed outcome:
`QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_MATRIX_READY_FOR_REVIEW`.

Upstream route/context labels preserved here:

- `QUERY_REF_SOURCE_DOC_PLACEHOLDER_PACKET_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_DEFINITION_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_GAP_CLOSURE_ROUTE_DEFINED_FOR_FUTURE_SOURCE_DOCS`
- `BLOCKED_BY_MISSING_REVIEWED_QUERY_REF_SOURCE_DOCS`
- `QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_BLOCKED_BY_MISSING_REVIEWED_REFS`
- `NO_REVIEWED_SOURCE_FOUND_AI_PLANNING_GAP`
- `REVIEWED_INPUTS_MISSING_AI_PLANNING_GAP`

The prior matrix classifies all three future source-doc slots as reviewed-input
gaps. This closure plan does not resolve those gaps; it only defines how a
future docs/control task may close them safely.

## Referenced Reviewed Inputs

This closure plan relies on these reviewed docs/control inputs:

- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_MATRIX_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_PLACEHOLDER_PACKET_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_DEFINITION_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_GAP_CLOSURE_ROUTE_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_REVIEWED_REF_DISCOVERY_MATRIX_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_V0.md`
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

Citation rule: every future closure step must cite exact reviewed docs/control
paths. Templates, placeholders, and route labels may define shape, but they are
not concrete query-ref source docs by themselves.

## Closure Status Vocabulary

Each future source-doc slot keeps the prior classification and receives one
closure-plan status:

- `CLOSURE_PLAN_DEFINED_FOR_AI_RESOLVABLE_GAP`: the missing reviewed inputs can
  be addressed by a future docs/control planning task, without inventing
  source/story/query content.
- `CLOSURE_PLAN_REQUIRES_USER_WORLDBUILDING`: reviewed evidence proves user
  worldbuilding is required before the slot can proceed.
- `CLOSURE_PLAN_BOUNDARY_BLOCKED`: continuing would cross forbidden boundaries
  such as actual query instance creation, query execution, concrete ref
  resolution, source-basis selection, evidence bundle creation, actual packet
  generation, sample JSON/story content, runtime/schema, provider/API,
  renderer/ffmpeg/TTS, media generation, approval, canon, registry, release,
  final approval, or production lock.

The current evidence supports only `CLOSURE_PLAN_DEFINED_FOR_AI_RESOLVABLE_GAP`
for all three slots.

## Current Closure-Plan Outcome

Allowed current closure-plan outcome labels are:

- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PLAN_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PLAN_BLOCKED_BY_MISSING_REVIEWED_INPUTS`
- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PLAN_BLOCKED_BY_BOUNDARY`
- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PLAN_REQUIRES_USER_WORLDBUILDING`

Selected current closure-plan outcome:

`QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PLAN_READY_FOR_REVIEW`

Rationale: the plan is reviewable because it only describes procedural
docs/control closure steps. It does not close the gaps, resolve concrete refs,
select source basis, create query evidence, or create actual packet content.

The single future bounded package candidate is
`script_ready_scene_packet_candidate_query_ref_source_doc_reviewed_input_gap_closure_packet_v0_docs_control`.

That candidate is classification-only. It must not auto-start and must not be
treated as approval.

## Per-Slot Closure Statuses

| Future source-doc slot | Prior reviewed-input gap classification | Closure-plan status | Missing parts kept unresolved |
| --- | --- | --- | --- |
| `QueryRequest` source-doc reviewed-input gap | `REVIEWED_INPUTS_MISSING_AI_PLANNING_GAP` | `CLOSURE_PLAN_DEFINED_FOR_AI_RESOLVABLE_GAP` | Future source basis decision surface, reviewed docs/control citation, concrete visibility classification, concrete `allowedPromptSurface` eligibility, and review evidence remain future-only. |
| `QueryResponse` source-doc reviewed-input gap | `REVIEWED_INPUTS_MISSING_AI_PLANNING_GAP` | `CLOSURE_PLAN_DEFINED_FOR_AI_RESOLVABLE_GAP` | Reviewed response source linkage, reviewed docs/control citation, concrete visibility classification, concrete `allowedPromptSurface` eligibility, and review evidence remain future-only. |
| `QueryScopeReviewReport` source-doc reviewed-input gap | `REVIEWED_INPUTS_MISSING_AI_PLANNING_GAP` | `CLOSURE_PLAN_DEFINED_FOR_AI_RESOLVABLE_GAP` | Reviewed request/response linkage, reviewed docs/control citation, concrete review scope evidence, visibility classification, and prompt-surface review evidence remain future-only. |

The table above is the only per-slot closure-status assignment in this
document. It restates the gaps without resolving them.

## Future Closure Plan Steps

The closure plan below is procedural. It defines what a future bounded
docs/control task may do. It does not perform those steps now.

### Future Source Basis Decision Surface Requirement

A future closure packet must create or cite a reviewed docs/control decision
surface for the intended query-ref source basis. The surface must distinguish
candidate planning material from actual query instances and must keep all
unresolved source/story/query content out of prompts, screen descriptions,
negative prompts, prompt-safe asset notes, voice/audio planning notes, and
media planning notes unless explicitly listed in `allowedPromptSurface`.

Current state: missing reviewed input, classified as an AI-resolvable planning
gap.

### Reviewed Docs/Control Citation Requirement

A future closure packet must cite exact reviewed docs/control paths for each
source-doc slot. A placeholder section, template field, sample value, Qingli
candidate, hidden note, runtime database inspection, direct SQLite write,
provider/API output, renderer/ffmpeg/TTS output, or media artifact is not a
valid citation.

Current state: missing reviewed input, classified as an AI-resolvable planning
gap.

### Visibility Classification Requirement

A future closure packet must assign visibility for any field that may later
feed prompts, screen descriptions, negative prompts, prompt-safe asset notes,
voice/audio planning notes, or media planning notes. Visibility must separate
review-only, author-only, internal planning, and prompt-eligible surfaces.

Current state: placeholder surface exists, but concrete reviewed visibility
classification remains future-review material.

### `allowedPromptSurface` Eligibility Requirement

A future closure packet must explicitly list any field eligible for prompt
surface use through `allowedPromptSurface`. Fields not explicitly listed must
not enter prompts, screen descriptions, negative prompts, prompt-safe asset
notes, voice/audio planning notes, or media planning notes.

Current state: placeholder eligibility exists, but concrete allowed fields
remain future-review material.

### `protectedBoundaryRef` Requirement

A future closure packet must require `protectedBoundaryRef` when any field must
avoid leaking hidden causality, culprit identity, sealed motive, director goal,
future reveal, or unavailable actor knowledge.

Current state: placeholder exists; concrete protected refs remain future-review
material.

### Generic `boundaryCategory` Requirement

A future closure packet must pair each `protectedBoundaryRef` with generic
`boundaryCategory` so the prompt surface can enforce a class of restriction
without exposing the protected fact.

Current state: placeholder exists; concrete categories remain future-review
material.

### Review Gate/Status Requirement

A future closure packet must keep review status explicit. It may use
`DRAFT_FOR_REVIEW` while unresolved and `REVIEW_PASS_NOT_APPROVAL` only after a
technical review gate passes. `REVIEW_PASS_NOT_APPROVAL` is not user approval,
asset approval, registry approval, canon, release, final media approval,
provider/media handoff, final approval, or production lock.

Current state: status labels exist; concrete review evidence remains
future-review material.

### Blocker Handling

A future closure packet must stop or keep a slot unresolved if it encounters:

- `BLOCKED_BY_HUMAN_DECISION`
- `BLOCKED_BY_BOUNDARY`
- missing reviewed docs/control citation
- need for new user/worldbuilding content proven by reviewed evidence
- any request to create actual query instances, execute queries, resolve
  concrete refs, select source basis, create evidence bundles, generate sample
  JSON, invent story/source/query content, promote Qingli candidates, or start
  runtime/schema/provider/API/media work

Current state: no reviewed evidence proves user/worldbuilding requirement or
boundary block, so all three slots remain AI-resolvable planning gaps.

## Actual Instance Exclusion

This closure plan does not create actual `QueryRequest`, `QueryResponse`, or
`QueryScopeReviewReport` instances.

It also does not execute queries, implement a queryer, resolve concrete refs,
select source basis, create evidence bundles, create actual
`ScriptReadyScenePacket` or `ScriptReadyScenePacketCandidate` content, generate
sample JSON, invent story/source/query content, promote Qingli candidates, or
start runtime/schema/provider/API/media work.

## Prompt Surface And Visibility Rules

Prompt surface is deny-by-default. Fields not explicitly listed in
`allowedPromptSurface` must not enter prompts, screen descriptions, negative
prompts, prompt-safe asset notes, voice/audio planning notes, or media planning
notes.

Negative prompts are also prompt surface.

Every future closure-plan field must define visibility before it can feed
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

A technical PASS on this closure plan does not create `QueryRequest`,
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

On B03 review PASS, B01/A01/00 may use this closure plan to classify or
dispatch a future explicit docs/control task. PASS must not auto-start actual
query instance creation, query execution, concrete ref resolution,
source-basis selection, evidence bundle creation, actual packet generation,
sample JSON creation, story content generation, runtime/CLI/schema/validator
work, 04/05/06/07/08 restart, renderer/ffmpeg/TTS execution, provider/API
work, media generation, approval, canon, registry, release, final approval, or
production lock.

## Boundary Summary

- docs/control only
- closure plan only
- no actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
  instance creation
- no query execution or queryer implementation
- no concrete query-ref resolution
- no source/story/query invention
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
