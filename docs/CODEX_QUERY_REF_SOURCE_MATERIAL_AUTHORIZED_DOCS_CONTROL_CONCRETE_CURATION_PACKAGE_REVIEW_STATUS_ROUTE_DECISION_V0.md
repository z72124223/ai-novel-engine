# Query Ref Source Material Authorized Docs Control Concrete Curation Package Review Status Route Decision V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`QueryRefSourceMaterialAuthorizedDocsControlConcreteCurationPackageReviewStatusRouteDecision`

This document is docs/control only. It decides whether the concrete
docs/control curation package review-status route can advance beyond
review/status surfaces or must block, across `QueryRequest`, `QueryResponse`,
and `QueryScopeReviewReport`. In this route-decision surface, "advance" means
advance only to another docs/control curation package. It does not create
actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport` instances,
does not execute queries, does not implement a queryer, does not resolve
concrete runtime query refs, does not create concrete refs, does not invent
source/story/query content, does not promote Qingli candidates, does not select
source basis for actual packet work, does not create evidence bundles, does
not create an actual `ScriptReadyScenePacket`, does not create an actual
`ScriptReadyScenePacketCandidate`, does not generate sample JSON, does not
start runtime/CLI/schema/validator work, has no provider/API work, does not run
renderer/ffmpeg/TTS, does not generate media, does not grant approval, and does
not create production lock.

## Upstream Review-Status Outcome

Reviewed review-status outcome:
`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CONCRETE_CURATION_PACKAGE_REVIEW_STATUS_READY_FOR_ROUTE_DECISION`.

This route-decision surface follows:
`docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CONCRETE_CURATION_PACKAGE_REVIEW_STATUS_V0.md`.

The reviewed review-status surface records exactly one review-status row each
for `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport`. Each row
uses `CONCRETE_PACKAGE_REVIEW_STATUS_PASS_NOT_APPROVAL_FOR_FUTURE_ROUTE_DECISION`,
supports future docs/control route-decision classification only, keeps
concrete refs unresolved/absent, keeps "concrete" limited to package structure
and review surfaces, and preserves citation, visibility, `allowedPromptSurface`,
`protectedBoundaryRef`, generic `boundaryCategory`, review gates,
`DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`.

B03 PASS for the concrete package review-status surface is technical review
evidence only. `REVIEW_PASS_NOT_APPROVAL` is not user approval, asset approval,
registry approval, canon, release, final media approval, provider/media
handoff, final approval, or production lock.

## Referenced Reviewed Inputs

This concrete package review-status route-decision surface cites these
reviewed docs/control inputs:

- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CONCRETE_CURATION_PACKAGE_REVIEW_STATUS_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CONCRETE_CURATION_PACKAGE_REVIEW_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CONCRETE_CURATION_PACKAGE_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_PACKAGE_REVIEW_STATUS_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_PACKAGE_REVIEW_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_PACKAGE_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_ROUTE_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_ROUTE_PACKAGE_REVIEW_STATUS_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_ROUTE_PACKAGE_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_ROUTE_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKAGE_REVIEW_STATUS_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKAGE_REVIEW_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKAGE_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PLAN_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORITY_CLASSIFICATION_V0.md`
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

Citation rule: every route-decision row must cite an exact reviewed
docs/control source path, the reviewed concrete-package review-status outcome
it relies on, and the query-ref slot it classifies. Concrete refs must stay
unresolved/absent unless future reviewed docs/control citations explicitly
support resolution.

## Query-Ref Route-Decision Rows

Each query-ref source-material route-decision row appears exactly once below.
The rows decide route advancement only; they do not resolve concrete refs.

| Route-decision row | Query-ref slot | Prior review-status row | Row decision | Preserved guardrails | Concrete-ref status |
| --- | --- | --- | --- | --- | --- |
| `CONCRETE_CURATION_REVIEW_STATUS_ROUTE_DECISION_ROW_QueryRequest` | `QueryRequest` | `CONCRETE_CURATION_PACKAGE_REVIEW_STATUS_ROW_QueryRequest` | `ROUTE_ROW_CAN_ADVANCE_TO_DOCS_CONTROL_CURATION_ONLY` because the row is non-approval, keeps concrete refs absent, and supports route-decision classification only. | Preserves citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gate, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved/absent: no actual request content, no runtime query parameters, no concrete source basis, no concrete refs, and no actual `QueryRequest` instance. |
| `CONCRETE_CURATION_REVIEW_STATUS_ROUTE_DECISION_ROW_QueryResponse` | `QueryResponse` | `CONCRETE_CURATION_PACKAGE_REVIEW_STATUS_ROW_QueryResponse` | `ROUTE_ROW_CAN_ADVANCE_TO_DOCS_CONTROL_CURATION_ONLY` because the row is non-approval, keeps concrete refs absent, and supports route-decision classification only. | Preserves citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gate, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved/absent: no actual query output, no provider/API output, no retrieved fact content, no concrete refs, and no actual `QueryResponse` instance. |
| `CONCRETE_CURATION_REVIEW_STATUS_ROUTE_DECISION_ROW_QueryScopeReviewReport` | `QueryScopeReviewReport` | `CONCRETE_CURATION_PACKAGE_REVIEW_STATUS_ROW_QueryScopeReviewReport` | `ROUTE_ROW_CAN_ADVANCE_TO_DOCS_CONTROL_CURATION_ONLY` because the row is non-approval, keeps concrete refs absent, and supports route-decision classification only. | Preserves citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gate, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved/absent: no actual scope-review verdict, no concrete query linkage, no evidence bundle, no source-basis selection, no concrete refs, and no actual `QueryScopeReviewReport` instance. |

## Aggregate Route Decision

The route can advance to another docs/control curation package because all
three route-decision rows:

- preserve the upstream review-status outcome
  `QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CONCRETE_CURATION_PACKAGE_REVIEW_STATUS_READY_FOR_ROUTE_DECISION`;
- preserve `CONCRETE_PACKAGE_REVIEW_STATUS_PASS_NOT_APPROVAL_FOR_FUTURE_ROUTE_DECISION`;
- preserve non-approval status through `DRAFT_FOR_REVIEW` and
  `REVIEW_PASS_NOT_APPROVAL`;
- confirm "concrete" remains limited to package structure and review surfaces;
- keep concrete query refs unresolved/absent unless future reviewed
  docs/control citations explicitly support resolution;
- preserve citation, visibility, `allowedPromptSurface`,
  `protectedBoundaryRef`, generic `boundaryCategory`, and review gate rules;
- reject forbidden claims, including actual query instances, query execution,
  queryer implementation, concrete runtime query-ref resolution, concrete refs,
  source/story/query invention, provider/API output, media output, approval,
  canon, release, final approval, and production lock.

This route decision is non-approval. It is not source-basis selection,
concrete query-ref resolution, evidence bundle creation, actual packet
generation, canon, release, final approval, or production lock.

## Prompt Surface And Visibility Guardrails

This route-decision surface follows
`docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`.

Fields not explicitly listed in `allowedPromptSurface` must not enter prompts,
screen descriptions, negative prompts, prompt-safe asset notes,
voice/audio planning notes, or media planning notes. Negative prompts are also
prompt surface.

Future docs/control curation work must distinguish review-only, author-only,
production-only, and future prompt-facing material. Review-only or author-only
material must not be copied, paraphrased, inverted, or hinted into
prompt-facing fields.

When future source-material notes must avoid leaking hidden causality, culprit
identity, sealed motive, director goal, future reveal, or unavailable actor
knowledge, the curation item must use `protectedBoundaryRef` plus generic
`boundaryCategory` instead of exposing protected content.

## Forbidden Claims

The forbidden claims below must be rejected or kept unresolved:

- actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
  instances;
- plain forbidden-claim aliases for verification: actual QueryRequest,
  actual QueryResponse, actual QueryScopeReviewReport, no provider/API,
  renderer/ffmpeg/TTS, production lock;
- query execution or queryer implementation;
- concrete runtime query-ref resolution;
- concrete refs;
- source/story/query invention;
- Qingli promotion;
- source-basis selection for actual packet work;
- evidence bundle creation;
- actual `ScriptReadyScenePacket` or `ScriptReadyScenePacketCandidate`
  content;
- sample JSON;
- runtime/CLI/schema/validator work;
- provider/API, web/server, direct SQLite writes, renderer/ffmpeg/TTS, or media
  generation;
- hidden author-only or review-only material copied, paraphrased, inverted, or
  hinted into prompt surface;
- 04/05/06/07/08 restart or Phase 20/21 restart;
- approval, canon, registry, release, final approval, or production lock.

## Selected Aggregate Outcome

Selected aggregate outcome:

`CONCRETE_CURATION_REVIEW_STATUS_ROUTE_DECISION_CAN_ADVANCE_TO_DOCS_CONTROL_CURATION`

Rationale: all three query-ref rows preserve non-approval review-status
classification, keep concrete refs unresolved/absent, and preserve citation,
visibility, `allowedPromptSurface`, `protectedBoundaryRef`,
`boundaryCategory`, review gates, and `REVIEW_PASS_NOT_APPROVAL`. The route can
advance beyond review/status surfaces only to another explicit docs/control
curation package, without auto-starting it or implying approval.

Future bounded docs/control package:
`query_ref_source_material_authorized_docs_control_curation_advancement_package_v0_docs_control`.

That future package is future explicit classification only,
docs/control-only, curation-only, not auto-started, and not approval.

## Downstream Allowance

On B03 PASS, B01/A01/00 may use this route-decision surface to classify or
dispatch a future explicit docs/control curation package task. PASS must not
auto-start actual query instance creation, query execution, concrete runtime
query-ref resolution, concrete refs, source-basis selection, evidence bundle
creation, actual packet work, sample JSON, story content,
runtime/CLI/schema/validator work, 04/05/06/07/08 route,
renderer/ffmpeg/TTS execution, provider/API work, media generation, approval,
canon, registry, release, final approval, or production lock.

## Boundary Summary

- docs/control only
- authorized docs/control concrete curation package review-status
  route-decision surface only
- exactly three query-ref route-decision rows
- route can advance only to docs/control curation
- concrete remains limited to package structure and review surfaces
- concrete query refs remain unresolved/absent
- no actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
  instances
- no query execution
- no queryer implementation
- no concrete runtime query-ref resolution
- no concrete refs
- no source/story/query invention
- no Qingli promotion
- no source-basis selection for actual packet work
- no evidence bundle creation
- no actual `ScriptReadyScenePacket`
- no actual `ScriptReadyScenePacketCandidate`
- no sample JSON
- no runtime/CLI/schema/validator work
- no provider/API
- no web/server
- no direct SQLite writes
- no renderer/ffmpeg/TTS execution
- no media generation
- no 04/05/06/07/08 restart
- no Phase 20/21 restart
- no approval
- no canon
- no registry approval
- no release
- no final approval
- no production lock
