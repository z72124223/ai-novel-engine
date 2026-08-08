# Query Ref Source Material Authorized Curation Route Package V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`QueryRefSourceMaterialAuthorizedCurationRoutePackage`

This document is docs/control only. It defines the route package surface for
future query-ref source-material curation across `QueryRequest`,
`QueryResponse`, and `QueryScopeReviewReport`. It does not create actual
`QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport` instances, does
not execute queries, does not implement a queryer, does not resolve concrete
runtime query refs, does not create concrete refs, does not invent
source/story/query content, does not promote Qingli candidates, does not select
source basis for actual packet work, does not create evidence bundles, does
not create an actual `ScriptReadyScenePacket`, does not create an actual
`ScriptReadyScenePacketCandidate`, does not generate sample JSON, does not
start runtime/CLI/schema/validator work, has no provider/API work, does not run
renderer/ffmpeg/TTS, does not generate media, does not grant approval, and does
not create production lock.

## Upstream Curation-Route Outcome

Reviewed curation-route outcome:
`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_ROUTE_READY_FOR_FUTURE_PACKAGE`.

This route package follows:
`docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_ROUTE_V0.md`.

The reviewed curation route records exactly one curation-route row each for
`QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport`. Each row
preserves citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`,
generic `boundaryCategory`, review gate, `DRAFT_FOR_REVIEW`, and
`REVIEW_PASS_NOT_APPROVAL`, while keeping concrete-ref status unresolved.

B03 PASS for the curation route is technical review evidence only.
`REVIEW_PASS_NOT_APPROVAL` is not user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

## Referenced Reviewed Inputs

This route package cites these reviewed docs/control inputs:

- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_ROUTE_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKAGE_REVIEW_STATUS_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKAGE_REVIEW_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKAGE_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_REVIEW_STATUS_ROUTE_DECISION_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_REVIEW_STATUS_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKET_REVIEW_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKET_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PLAN_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORITY_CLASSIFICATION_V0.md`
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

Citation rule: every route-package row must cite an exact reviewed
docs/control source path, the reviewed curation-route outcome it relies on,
and the query-ref slot it preserves. Concrete refs must stay unresolved unless
future reviewed docs/control citations explicitly support resolution.

## Authorized Curation Route-Package Rows

Each query-ref source-material route-package row appears exactly once below.
The rows define docs/control package surfaces for later review/status work
without resolving concrete refs.

| Route-package row | Query-ref slot | Prior curation-route row | Package label | Route-package surface | Preserved rules | Concrete-ref status |
| --- | --- | --- | --- | --- | --- | --- |
| `ROUTE_PACKAGE_ROW_QueryRequest` | `QueryRequest` | `CURATION_ROUTE_ROW_QueryRequest` | `ROUTE_PACKAGE_NON_APPROVAL_READY_FOR_FUTURE_REVIEW_STATUS` | Future docs/control curation package surface for request intent, request scope, allowed input basis, and reviewed citation placeholders only. | Preserves citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gate, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved: actual request content, runtime query parameters, concrete source basis, concrete refs, and actual `QueryRequest` instance remain absent unless future reviewed docs/control citations support resolution. |
| `ROUTE_PACKAGE_ROW_QueryResponse` | `QueryResponse` | `CURATION_ROUTE_ROW_QueryResponse` | `ROUTE_PACKAGE_NON_APPROVAL_READY_FOR_FUTURE_REVIEW_STATUS` | Future docs/control curation package surface for response evidence categories, source paths, allowed citation basis, and reviewed citation placeholders only. | Preserves citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gate, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved: actual query output, retrieved fact content, provider/API output, concrete refs, and actual `QueryResponse` instance remain absent unless future reviewed docs/control citations support resolution. |
| `ROUTE_PACKAGE_ROW_QueryScopeReviewReport` | `QueryScopeReviewReport` | `CURATION_ROUTE_ROW_QueryScopeReviewReport` | `ROUTE_PACKAGE_NON_APPROVAL_READY_FOR_FUTURE_REVIEW_STATUS` | Future docs/control curation package surface for scope-review criteria, slot coverage, allowed review evidence, and reviewed citation placeholders only. | Preserves citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gate, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved: actual scope-review verdict, concrete query linkage, evidence bundle creation, source-basis selection, concrete refs, and actual `QueryScopeReviewReport` instance remain absent unless future reviewed docs/control citations support resolution. |

## Route-Package Summary

The route package can name one future review/status package candidate because
all three route-package rows:

- preserve the reviewed curation-route outcome
  `QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_ROUTE_READY_FOR_FUTURE_PACKAGE`;
- preserve `ROUTE_PACKAGE_NON_APPROVAL_READY_FOR_FUTURE_REVIEW_STATUS`;
- preserve `REVIEW_PASS_NOT_APPROVAL`;
- preserve `DRAFT_FOR_REVIEW`;
- preserve citation requirements;
- preserve visibility requirements;
- preserve `allowedPromptSurface` rules;
- preserve `protectedBoundaryRef` plus generic `boundaryCategory` rules;
- preserve review gate rules;
- keep concrete refs unresolved unless future reviewed docs/control citations
  support resolution;
- reject actual query instances, query execution, queryer implementation,
  concrete runtime query-ref resolution, source/story/query invention,
  provider/API output, media output, approval, canon, release, and production
  lock.

This route package is package/classification-only. It does not create actual
query instances, does not resolve concrete refs, does not approve source basis,
does not create an evidence bundle, does not approve actual packet work, and
does not create canon, release, final approval, or production lock.

## Prompt Surface And Visibility Guardrails

This route package follows `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`.

Fields not explicitly listed in `allowedPromptSurface` must not enter prompts,
screen descriptions, negative prompts, prompt-safe asset notes,
voice/audio planning notes, or media planning notes. Negative prompts are also
prompt surface.

Future review/status package work must distinguish review-only, author-only,
production-only, and future prompt-facing material. Review-only or author-only
material must not be copied, paraphrased, inverted, or hinted into
prompt-facing fields.

When future source-material notes must avoid leaking hidden causality, culprit
identity, sealed motive, director goal, future reveal, or unavailable actor
knowledge, the route-package-derived review/status item must use
`protectedBoundaryRef` plus generic `boundaryCategory` instead of exposing
protected content.

## Forbidden Claims

The forbidden claims below must be rejected or kept unresolved:

- actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
  instances;
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

## Selected Route-Package Outcome

Selected route-package outcome:

`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_ROUTE_PACKAGE_READY_FOR_REVIEW_STATUS`

Rationale: the reviewed curation-route outcome supports a docs/control route
package surface. This package preserves all three query-ref rows as
docs/control only, keeps concrete-ref status unresolved unless future reviewed
docs/control citations support resolution, and retains citation, visibility,
`allowedPromptSurface`, `protectedBoundaryRef`, `boundaryCategory`, review
gates, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. Therefore, it can
name one future docs/control review/status package candidate without
auto-starting it or implying approval.

Future bounded docs/control review/status package candidate:
`query_ref_source_material_authorized_curation_route_package_review_status_v0_docs_control`.

That future candidate is future explicit classification only,
docs/control-only, classification/curation/review/status/package-review/status/
route-decision/route/package/classification-only, not auto-started, and not
approval.

## Downstream Allowance

On B03 PASS, B01/A01/00 may use this route package to classify or dispatch a
future explicit docs/control package. PASS must not auto-start actual query
instance creation, query execution, concrete runtime query-ref resolution,
concrete refs, source-basis selection, evidence bundle creation, actual packet
work, sample JSON, story content, runtime/CLI/schema/validator work,
04/05/06/07/08 route, renderer/ffmpeg/TTS execution, provider/API work, media
generation, approval, canon, registry, release, final approval, or production
lock.

## Boundary Summary

- docs/control only
- authorized curation route package only
- exactly three query-ref route-package rows
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
