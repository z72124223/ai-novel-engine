# Script-Ready Scene Packet Candidate Query Ref Source Doc Gap Closure Route V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`ScriptReadyScenePacketCandidateQueryRefSourceDocGapClosureRoute`

This document is docs/control only. It defines an AI-governed route for closing
the reviewed query-ref source-doc gap, but it does not create actual
`QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport` instances. It is
not query execution, not queryer implementation, not source/story invention,
not Qingli promotion, not source-basis selection, not evidence bundle creation,
not an actual `ScriptReadyScenePacket`, not an actual
`ScriptReadyScenePacketCandidate`, not sample JSON, not packet candidate
generation, not route execution, not runtime/CLI/schema/validator work, not
provider/API work, not media generation, and not production lock.

## Current Blocker

Current blocker:
`BLOCKED_BY_MISSING_REVIEWED_QUERY_REF_SOURCE_DOCS`.

Upstream reviewed/query-ref state:

- `QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_BLOCKED_BY_MISSING_REVIEWED_REFS`
- `NO_REVIEWED_SOURCE_FOUND_AI_PLANNING_GAP`
- `QueryRequest` refs are missing reviewed source docs.
- `QueryResponse` refs are missing reviewed source docs.
- `QueryScopeReviewReport` refs are missing reviewed source docs.

The prior discovery matrix found no reviewed source document that can be cited
as a concrete query-ref source for the future first script-ready scene packet
candidate. User/00 clarification says the broader blueprint route may continue
when A01 can provide an AI-governed technical route, so this document defines
that route without resolving the concrete refs.

## Route Outcome

Selected route outcome:

`QUERY_REF_SOURCE_DOC_GAP_CLOSURE_ROUTE_DEFINED_FOR_FUTURE_SOURCE_DOCS`

This outcome reclassifies the blocker as an AI-resolvable docs/control route
gap. It does not promote placeholders, templates, Qingli-derived candidates,
or partial source candidates into reviewed refs. It only defines what future
reviewed source docs must prove before any later task may use them.

Because the route is safe and remains docs/control only, the single future
bounded package candidate is
`script_ready_scene_packet_candidate_query_ref_source_doc_definition_v0_docs_control`.

That candidate is classification-only until B01/A01/00 explicitly dispatches
it after review. This route must not auto-start it.

## Referenced Reviewed Inputs

This route relies on these reviewed docs/control inputs:

- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_REVIEWED_REF_DISCOVERY_MATRIX_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_EVIDENCE_SURFACE_DEFINITION_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_GAP_CLOSURE_DECISION_BRIEF_V0.md`
- `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md`
- `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md`
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

Citation rule: every future reviewed query-ref source doc must cite exact
docs/control source paths and must distinguish rule/template evidence from
concrete reviewed source-doc evidence. If a concrete citation cannot be made,
the slot remains unresolved.

## What Qualifies As A Reviewed Query-Ref Source Doc

A reviewed query-ref source doc is a docs/control artifact that has passed
technical review as `REVIEW_PASS_NOT_APPROVAL`, cites exact reviewed source
paths, preserves prompt-surface and protected-boundary rules, and explicitly
states that it is not an actual query instance.

### `QueryRequest` Source Doc

A `QueryRequest` reviewed query-ref source doc qualifies only if it contains:

- a source-doc id and route family;
- the slot name `QueryRequest`;
- a narrow information need for a future packet;
- allowed source categories and forbidden source categories;
- required citation paths for any future concrete refs;
- visibility rules for every field;
- `allowedPromptSurface` and forbidden prompt-surface rules;
- `protectedBoundaryRef` plus generic `boundaryCategory` when protected
  hidden, causal, reveal, or unavailable-knowledge boundaries are relevant;
- `DRAFT_FOR_REVIEW` before technical review and `REVIEW_PASS_NOT_APPROVAL`
  after technical review;
- explicit non-approval language.

It must not contain an executable request, a query runtime call, provider/API
output, inferred story/source content, direct SQLite data, or a selected source
basis.

### `QueryResponse` Source Doc

A `QueryResponse` reviewed query-ref source doc qualifies only if it contains:

- a source-doc id and route family;
- the slot name `QueryResponse`;
- a link to the relevant `QueryRequest` source doc if one exists;
- cited source paths for any future response evidence;
- a clear distinction between candidate evidence, missing evidence, and
  rejected evidence;
- visibility rules for every response-facing field;
- `allowedPromptSurface` and forbidden prompt-surface rules;
- `protectedBoundaryRef` plus generic `boundaryCategory` for protected
  boundaries;
- `DRAFT_FOR_REVIEW` before technical review and `REVIEW_PASS_NOT_APPROVAL`
  after technical review;
- explicit non-approval language.

It must not contain returned query results, query execution output, provider/API
output, invented story/source content, runtime database inspection, direct
SQLite writes, media artifacts, or source-basis selection.

### `QueryScopeReviewReport` Source Doc

A `QueryScopeReviewReport` reviewed query-ref source doc qualifies only if it
contains:

- a source-doc id and route family;
- the slot name `QueryScopeReviewReport`;
- references to the relevant `QueryRequest` and `QueryResponse` source docs if
  they exist;
- checks for citation completeness, missing refs, prompt-surface leakage,
  protected-boundary leakage, source/story invention, approval overclaim,
  runtime/schema overreach, provider/API use, renderer/ffmpeg/TTS output, and
  media artifacts;
- review gate and reviewer evidence refs;
- `DRAFT_FOR_REVIEW` before technical review and `REVIEW_PASS_NOT_APPROVAL`
  after technical review;
- explicit non-approval language.

It must not be treated as a real scope review report for executed queries. It
is a reviewed source-doc surface for future classification only.

## AI-Governed Gap Closure Path

Future work may close this gap only through this sequence:

1. B01/A01/00 explicitly classify a bounded docs/control task.
2. B02 defines reviewed query-ref source docs for the three slots above,
   without creating actual query instances.
3. B03 reviews the source docs read-only.
4. Only after technical review PASS may B01/A01/00 classify whether a later
   task can use those source docs as reviewed refs.
5. Any user/worldbuilding blocker, boundary blocker, or missing citation keeps
   the affected slot unresolved.

This path may produce reviewed source docs later. It must not produce actual
`QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport` instances now.

## Citation, Visibility, Prompt Surface, And Protected Boundary Rules

Every future reviewed query-ref source doc must state:

- cited reviewed docs/control paths;
- citation purpose and slot name;
- visibility rules;
- `allowedPromptSurface`;
- forbidden prompt-surface rules;
- `protectedBoundaryRef`;
- generic `boundaryCategory`;
- review status and review gate;
- unresolved or rejected evidence status when citation is missing.

Prompt surface is deny-by-default. Fields not explicitly listed in
`allowedPromptSurface` must not enter prompts, screen descriptions, negative
prompts, prompt-safe asset notes, or voice/audio planning notes.

Negative prompts are also prompt surface.

`author_only` and `review_only` material must not be copied, inverted,
summarized, hinted, or paraphrased into prompt-facing fields. Where a source doc
must avoid hidden causality, culprit identity, sealed motive, director goal,
future reveal, or unavailable actor knowledge, it must use
`protectedBoundaryRef` plus generic `boundaryCategory` instead of exposing the
protected fact.

## Review Status Terms

- `DRAFT_FOR_REVIEW`: the route/source-doc definition is ready for technical
  review.
- `REVIEW_PASS_NOT_APPROVAL`: technical review passed, but this is not user
  approval, asset approval, registry approval, canon approval, release approval,
  final media approval, provider/media handoff, final approval, or production
  lock.
- `NEEDS_REVISION`: citation rules, source-doc qualifications,
  prompt-surface rules, protected-boundary rules, or boundary wording needs
  correction.
- `BLOCKED_BY_HUMAN_DECISION`: a human/user decision is required before future
  task classification.
- `BLOCKED_BY_BOUNDARY`: continuing would cross actual query instance
  creation, query execution, concrete ref resolution, source-basis selection,
  evidence bundle creation, actual packet generation, sample JSON/story
  content, runtime/schema, provider/API, renderer/ffmpeg/TTS, media generation,
  approval, canon, registry, release, final approval, or production lock.

## Technical PASS Is Not Approval

`B03 PASS`, `03 PASS`, `QA_PASS_NOT_APPROVAL`, and
`REVIEW_PASS_NOT_APPROVAL` do not equal user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

A technical PASS on this route does not create `QueryRequest`, `QueryResponse`,
or `QueryScopeReviewReport` instances, does not execute a query, does not
implement a queryer, does not promote Qingli candidates, does not resolve
concrete refs, does not select source basis, does not create an evidence
bundle, does not create an actual `ScriptReadyScenePacket`, does not create an
actual `ScriptReadyScenePacketCandidate`, does not create sample JSON, does not
start story generation, does not start runtime/CLI/schema/validator work, does
not resume 04/05/06/07/08, does not run renderer/ffmpeg/TTS, does not call
provider/API, does not generate media, does not grant approval, does not create
canon, does not create registry approval, does not release, and does not create
production lock.

## Downstream Allowance

On B03 review PASS, B01/A01/00 may use this route to classify or dispatch a
future explicit docs/control task. PASS must not auto-start actual query
instance creation, query execution, concrete ref resolution, source-basis
selection, evidence bundle creation, actual packet generation, sample JSON
creation, story content generation, runtime/CLI/schema/validator work,
04/05/06/07/08 restart, renderer/ffmpeg/TTS execution, provider/API work, media
generation, approval, canon, registry, release, final approval, or production
lock.

## Boundary Summary

- docs/control only
- reviewed query-ref source doc gap closure route only
- no actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
  instance creation
- no query execution or queryer implementation
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
