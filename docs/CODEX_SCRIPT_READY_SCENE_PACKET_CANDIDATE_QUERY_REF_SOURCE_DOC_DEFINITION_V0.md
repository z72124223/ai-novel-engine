# Script-Ready Scene Packet Candidate Query Ref Source Doc Definition V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`ScriptReadyScenePacketCandidateQueryRefSourceDocDefinition`

This document is docs/control only. It defines placeholder/reference-only
source-doc surfaces for future reviewed query-ref source docs. It does not
create actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
instances, does not execute queries, does not implement queryers, does not
resolve concrete query refs, does not invent source/story content, does not
promote Qingli candidates, does not select source basis, does not create
evidence bundles, does not create an actual `ScriptReadyScenePacket`, does not
create an actual `ScriptReadyScenePacketCandidate`, does not create sample JSON,
does not perform runtime/CLI/schema/validator work, grants no provider/API
work, does not run renderer/ffmpeg/TTS, does not generate media, and does not
create production lock.

## Upstream Route Outcome

This definition follows:
`docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_GAP_CLOSURE_ROUTE_V0.md`.

Upstream route outcome:
`QUERY_REF_SOURCE_DOC_GAP_CLOSURE_ROUTE_DEFINED_FOR_FUTURE_SOURCE_DOCS`.

Unresolved source-doc blocker/context preserved here:

- `BLOCKED_BY_MISSING_REVIEWED_QUERY_REF_SOURCE_DOCS`
- `QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_BLOCKED_BY_MISSING_REVIEWED_REFS`
- `NO_REVIEWED_SOURCE_FOUND_AI_PLANNING_GAP`

The current project has reviewed rules and placeholder surfaces, but it still
does not have concrete reviewed source docs for future `QueryRequest`,
`QueryResponse`, or `QueryScopeReviewReport` refs.

## Referenced Reviewed Inputs

This definition relies on these reviewed docs/control inputs:

- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_GAP_CLOSURE_ROUTE_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_REVIEWED_REF_DISCOVERY_MATRIX_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_EVIDENCE_SURFACE_DEFINITION_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_GAP_CLOSURE_DECISION_BRIEF_V0.md`
- `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md`
- `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md`
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

Citation rule: a future source doc must cite reviewed docs/control paths. A
template path may support field shape, but it is not a concrete reviewed
query-ref source doc by itself.

## Current Definition Outcome

Allowed current definition outcome labels are:

- `QUERY_REF_SOURCE_DOC_DEFINITION_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_DEFINITION_BLOCKED_BY_MISSING_REVIEWED_INPUTS`
- `QUERY_REF_SOURCE_DOC_DEFINITION_BLOCKED_BY_BOUNDARY`
- `QUERY_REF_SOURCE_DOC_DEFINITION_REQUIRES_USER_WORLDBUILDING`

Selected current definition outcome:

`QUERY_REF_SOURCE_DOC_DEFINITION_READY_FOR_REVIEW`

Rationale: the reviewed route already defined the missing source-doc gap as an
AI-governed docs/control route. This document defines source-doc surfaces and
qualification rules only, so it can proceed to technical review without
creating actual query instances or resolving concrete refs.

The single future bounded package candidate is
`script_ready_scene_packet_candidate_query_ref_source_doc_placeholder_packet_v0_docs_control`.

That candidate remains classification-only. It must not auto-start and must not
be treated as approval.

## Source-Doc Surface Model

Each future reviewed query-ref source doc must be a docs/control artifact with
placeholder/reference-only fields. It must not be an actual query instance.

Common placeholder/reference-only fields:

- `surfaceId`: placeholder id for the source-doc surface;
- `slotName`: one of `QueryRequest`, `QueryResponse`, or
  `QueryScopeReviewReport`;
- `intendedFutureUse`: why a future packet may need this source-doc surface;
- `requiredSourceBasisPlaceholder`: placeholder for the future source basis
  citation, not a selected source basis;
- `reviewedDocsControlCitationPlaceholder`: placeholder for exact reviewed
  docs/control path citations;
- `sourceVisibilityClassification`: `author_only`, `review_only`,
  `production_only`, or `user_facing`;
- `allowedPromptSurfaceEligibilityPlaceholder`: whether the field may later
  enter prompts, screen descriptions, negative prompts, prompt-safe asset
  notes, or voice/audio planning notes;
- `protectedBoundaryRef`: placeholder ref when hidden or unavailable knowledge
  must be protected;
- `boundaryCategory`: generic category for hidden causality, culprit identity,
  sealed motive, director goal, future reveal, or unavailable actor knowledge;
- `reviewStatusGate`: `DRAFT_FOR_REVIEW` before review and
  `REVIEW_PASS_NOT_APPROVAL` only after technical review;
- `unresolvedReason`: required when no concrete reviewed citation exists.

## `QueryRequest` Source Doc Surface

The `QueryRequest` source doc surface defines a future request-source shape
without creating an actual `QueryRequest`.

Required placeholder/reference-only fields:

- `surfaceId`
- `slotName`: `QueryRequest`
- `intendedFutureUse`
- `requiredSourceBasisPlaceholder`
- `reviewedDocsControlCitationPlaceholder`
- `narrowInformationNeedPlaceholder`
- `allowedSourceCategoryPlaceholder`
- `forbiddenSourceCategoryPlaceholder`
- `sourceVisibilityClassification`
- `allowedPromptSurface`
- `forbiddenPromptSurface`
- `protectedBoundaryRef`
- generic `boundaryCategory`
- `reviewStatusGate`: `DRAFT_FOR_REVIEW` / `REVIEW_PASS_NOT_APPROVAL`
- `unresolvedReason`

Qualification rules:

- It must cite reviewed docs/control paths before it can be treated as a
  reviewed query-ref source doc.
- It must explain why the future information need is narrow enough.
- It must keep hidden author/control material out of prompt-facing fields.
- It must not include executable query request data, query runtime calls,
  provider/API output, runtime database inspection, direct SQLite writes, or
  selected source basis.

## `QueryResponse` Source Doc Surface

The `QueryResponse` source doc surface defines a future response-source shape
without creating an actual `QueryResponse`.

Required placeholder/reference-only fields:

- `surfaceId`
- `slotName`: `QueryResponse`
- `intendedFutureUse`
- `sourceRequestSurfaceRefPlaceholder`
- `requiredSourceBasisPlaceholder`
- `reviewedDocsControlCitationPlaceholder`
- `candidateEvidencePlaceholder`
- `missingEvidencePlaceholder`
- `rejectedEvidencePlaceholder`
- `sourceVisibilityClassification`
- `allowedPromptSurface`
- `forbiddenPromptSurface`
- `protectedBoundaryRef`
- generic `boundaryCategory`
- `reviewStatusGate`: `DRAFT_FOR_REVIEW` / `REVIEW_PASS_NOT_APPROVAL`
- `unresolvedReason`

Qualification rules:

- It must cite reviewed docs/control paths for any future response evidence.
- It must distinguish candidate evidence, missing evidence, and rejected
  evidence.
- It must preserve the requested visibility target or stricter visibility.
- It must not invent returned evidence, facts, ids, assets, approvals,
  provider/API output, media artifacts, direct SQLite data, or selected source
  basis.

## `QueryScopeReviewReport` Source Doc Surface

The `QueryScopeReviewReport` source doc surface defines a future
scope-review-source shape without creating an actual `QueryScopeReviewReport`.

Required placeholder/reference-only fields:

- `surfaceId`
- `slotName`: `QueryScopeReviewReport`
- `intendedFutureUse`
- `sourceRequestSurfaceRefPlaceholder`
- `sourceResponseSurfaceRefPlaceholder`
- `reviewedDocsControlCitationPlaceholder`
- `citationCompletenessCheckPlaceholder`
- `missingRefCheckPlaceholder`
- `promptSurfaceLeakageCheckPlaceholder`
- `protectedBoundaryLeakageCheckPlaceholder`
- `sourceStoryInventionCheckPlaceholder`
- `approvalOverclaimCheckPlaceholder`
- `runtimeProviderMediaBoundaryCheckPlaceholder`
- `sourceVisibilityClassification`
- `allowedPromptSurface`
- `forbiddenPromptSurface`
- `protectedBoundaryRef`
- generic `boundaryCategory`
- `reviewStatusGate`: `DRAFT_FOR_REVIEW` / `REVIEW_PASS_NOT_APPROVAL`
- `unresolvedReason`

Qualification rules:

- It must cite reviewed docs/control paths for request and response source
  surfaces before it can be treated as a reviewed query-ref source doc.
- It must check citation completeness, missing refs, prompt-surface leakage,
  protected-boundary leakage, source/story invention, approval overclaim,
  runtime/schema overreach, provider/API use, renderer/ffmpeg/TTS output, and
  media artifacts.
- It must not be treated as a real scope review report for executed queries.

## Prompt Surface And Visibility Rules

Prompt surface is deny-by-default. Fields not explicitly listed in
`allowedPromptSurface` must not enter prompts, screen descriptions, negative
prompts, prompt-safe asset notes, or voice/audio planning notes.

Negative prompts are also prompt surface.

Every future source-doc surface must define visibility for fields that may
later feed prompts, screen descriptions, negative prompts, prompt-safe asset
notes, or voice/audio planning notes.

`author_only` and `review_only` material must not be copied, inverted,
summarized, hinted, or paraphrased into prompt-facing fields. Where a source
doc must avoid hidden causality, culprit identity, sealed motive, director
goal, future reveal, or unavailable actor knowledge, it must use
`protectedBoundaryRef` plus generic `boundaryCategory` instead of exposing the
protected fact.

## Qualification Rules For Reviewed Source Docs

A future source doc can be treated as reviewed only when all of these are true:

- it is a docs/control artifact;
- it cites exact reviewed docs/control paths;
- it distinguishes placeholder fields from concrete reviewed citations;
- it identifies unresolved reasons when no concrete reviewed citation exists;
- it declares visibility and prompt-surface eligibility;
- it uses `protectedBoundaryRef` plus generic `boundaryCategory` for protected
  hidden or unavailable knowledge;
- it passes technical review as `REVIEW_PASS_NOT_APPROVAL`;
- it makes no approval, canon, registry, release, final media approval,
  provider/media handoff, final approval, or production lock claim;
- it does not create actual `QueryRequest`, `QueryResponse`, or
  `QueryScopeReviewReport` instances.

## Review Status Terms

- `DRAFT_FOR_REVIEW`: this definition is ready for technical review.
- `REVIEW_PASS_NOT_APPROVAL`: technical review passed, but this is not user
  approval, asset approval, registry approval, canon approval, release approval,
  final media approval, provider/media handoff, final approval, or production
  lock.
- `NEEDS_REVISION`: source-doc surface fields, citation rules,
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

A technical PASS on this definition does not create `QueryRequest`,
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

On B03 review PASS, B01/A01/00 may use this definition to classify or dispatch
a future explicit docs/control task. PASS must not auto-start actual query
instance creation, query execution, concrete ref resolution, source-basis
selection, evidence bundle creation, actual packet generation, sample JSON
creation, story content generation, runtime/CLI/schema/validator work,
04/05/06/07/08 restart, renderer/ffmpeg/TTS execution, provider/API work, media
generation, approval, canon, registry, release, final approval, or production
lock.

## Boundary Summary

- docs/control only
- source-doc surface definition only
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
