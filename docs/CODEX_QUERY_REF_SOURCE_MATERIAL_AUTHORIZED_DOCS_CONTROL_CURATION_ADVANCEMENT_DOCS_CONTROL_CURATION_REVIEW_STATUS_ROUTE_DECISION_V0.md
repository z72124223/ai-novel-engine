# Query Ref Source Material Authorized Docs Control Curation Advancement Docs Control Curation Review Status Route Decision V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`QueryRefSourceMaterialAuthorizedDocsControlCurationAdvancementDocsControlCurationReviewStatusRouteDecision`

This document is docs/control only. It records a route-decision surface for the
docs/control curation review-status rows across `QueryRequest`,
`QueryResponse`, and `QueryScopeReviewReport`. The route decision is limited to
future docs/control classification. It does not create actual query instances,
does not execute queries, does not create concrete refs, does not invent
source/story/query content, does not promote Qingli candidates, does not select
source basis, does not create an evidence bundle, does not create a
packet/candidate, does not generate sample JSON, does not start runtime/schema/
provider/API/media work, has no provider/API work, does not start a web/server,
does not write SQLite directly, does not run renderer/ffmpeg/TTS, does not
generate media, does not grant approval/canon/registry/release, and does not
create production lock.

## Upstream Review-Status Outcome

Reviewed curation review-status outcome:
`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_REVIEW_STATUS_READY_FOR_ROUTE_DECISION`.

This docs/control curation review-status route-decision surface follows:
`docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_REVIEW_STATUS_V0.md`.

The reviewed curation review-status surface records exactly one review-status
row each for `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport`.
Each row follows upstream `CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_REVIEW_ROW_*`,
preserves `DOCS_CONTROL_CURATION_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_STATUS`,
uses `DOCS_CONTROL_CURATION_REVIEW_STATUS_PASS_NOT_APPROVAL_FOR_FUTURE_ROUTE_DECISION`,
keeps concrete refs unresolved/absent, and preserves citation/path check
status, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic
`boundaryCategory`, review gates, `DRAFT_FOR_REVIEW`, and
`REVIEW_PASS_NOT_APPROVAL`.

B03 PASS for the upstream curation review-status surface is technical review
evidence only. `REVIEW_PASS_NOT_APPROVAL` is not user approval, asset approval,
registry approval, canon, release, final media approval, provider/media
handoff, final approval, or production lock.

## Referenced Reviewed Inputs

This docs/control curation review-status route-decision surface cites these
reviewed docs/control inputs:

- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_REVIEW_STATUS_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_REVIEW_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_ROUTE_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_ROUTE_PACKAGE_REVIEW_STATUS_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_ROUTE_PACKAGE_REVIEW_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_ROUTE_PACKAGE_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_PACKAGE_REVIEW_STATUS_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_PACKAGE_REVIEW_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_PACKAGE_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CONCRETE_CURATION_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_V0.md`
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
docs/control source path, the reviewed curation review-status outcome it relies
on, and the query-ref slot it decides. The upstream citation/path check found
30 referenced docs/control paths and 0 missing. Concrete refs must stay
unresolved/absent unless future reviewed docs/control citations explicitly
support resolution.

## Docs/Control Curation Review-Status Route-Decision Rows

Each query-ref source-material route-decision row appears exactly once below.
The rows decide route state only; they do not resolve concrete refs.

| Route-decision row | Query-ref slot | Upstream review-status row | Prior review classification | Review-status classification | Route decision | Preserved guardrails | Concrete-ref status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_REVIEW_STATUS_ROUTE_DECISION_ROW_QueryRequest` | `QueryRequest` | `CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_REVIEW_STATUS_ROW_QueryRequest` | `DOCS_CONTROL_CURATION_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_STATUS` | `DOCS_CONTROL_CURATION_REVIEW_STATUS_PASS_NOT_APPROVAL_FOR_FUTURE_ROUTE_DECISION` | `ROUTE_DECISION_CAN_ADVANCE_TO_DOCS_CONTROL_ROUTE_PACKAGE_ONLY` because the row remains docs/control-only, non-approval, and does not create request content or runtime parameters. | Preserves exact citation/path check status, visibility classification, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gates, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved/absent: no actual request content, no runtime query parameters, no concrete source basis, no concrete refs, and no actual `QueryRequest` instance. |
| `CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_REVIEW_STATUS_ROUTE_DECISION_ROW_QueryResponse` | `QueryResponse` | `CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_REVIEW_STATUS_ROW_QueryResponse` | `DOCS_CONTROL_CURATION_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_STATUS` | `DOCS_CONTROL_CURATION_REVIEW_STATUS_PASS_NOT_APPROVAL_FOR_FUTURE_ROUTE_DECISION` | `ROUTE_DECISION_CAN_ADVANCE_TO_DOCS_CONTROL_ROUTE_PACKAGE_ONLY` because the row remains docs/control-only, non-approval, and does not create query output or provider results. | Preserves exact citation/path check status, visibility classification, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gates, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved/absent: no actual query output, no provider/API output, no retrieved fact content, no concrete refs, and no actual `QueryResponse` instance. |
| `CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_REVIEW_STATUS_ROUTE_DECISION_ROW_QueryScopeReviewReport` | `QueryScopeReviewReport` | `CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_REVIEW_STATUS_ROW_QueryScopeReviewReport` | `DOCS_CONTROL_CURATION_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_STATUS` | `DOCS_CONTROL_CURATION_REVIEW_STATUS_PASS_NOT_APPROVAL_FOR_FUTURE_ROUTE_DECISION` | `ROUTE_DECISION_CAN_ADVANCE_TO_DOCS_CONTROL_ROUTE_PACKAGE_ONLY` because the row remains docs/control-only, non-approval, and does not create scope-review evidence or verdicts. | Preserves exact citation/path check status, visibility classification, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gates, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved/absent: no actual scope-review verdict, no concrete query linkage, no evidence bundle, no source-basis selection, no concrete refs, and no actual `QueryScopeReviewReport` instance. |

## Aggregate Route Decision

The route can advance only to a future explicit docs/control route-package
classification surface because every row:

- preserves the reviewed curation review-status outcome;
- preserves all three query-ref review-status rows as non-approval
  route-decision surfaces;
- preserves `DOCS_CONTROL_CURATION_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_STATUS`
  and `DOCS_CONTROL_CURATION_REVIEW_STATUS_PASS_NOT_APPROVAL_FOR_FUTURE_ROUTE_DECISION`;
- keeps route decision limited to docs/control route classification;
- keeps concrete refs and actual query content absent/unresolved;
- preserves citation/path check status, visibility, `allowedPromptSurface`,
  `protectedBoundaryRef`, generic `boundaryCategory`, review gates,
  `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`;
- rejects forbidden claims, including actual query instances, query execution,
  concrete refs, source/story/query invention, provider/API output, direct
  SQLite writes, media generation, approval, canon, registry, release, and
  production lock.

Absent concrete refs continue to block actual query-ref resolution, actual
query instances, evidence bundles, source-basis selection, and actual packet
work. They do not block a future docs/control-only route-package surface that
continues to preserve absent refs and keeps the next step classification-only.
No true stop condition is selected because the reviewed facts support only
bounded docs/control advancement and still reject actual content or runtime
work.

This route decision is non-approval. It is not source-basis selection, concrete
query-ref resolution, evidence bundle creation, actual packet generation,
canon, release, final approval, or production lock.

## Prompt Surface And Visibility Guardrails

This docs/control curation review-status route-decision surface follows
`docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`.

Fields not explicitly listed in `allowedPromptSurface` must not enter prompts,
screen descriptions, negative prompts, prompt-safe asset notes,
voice/audio planning notes, or media planning notes. Negative prompts are also
prompt surface.

Future docs/control route-package work must distinguish review-only,
author-only, production-only, and future prompt-facing material. Review-only or
author-only material must not be copied, paraphrased, inverted, or hinted into
prompt-facing fields.

When future source-material notes must avoid leaking hidden causality, culprit
identity, sealed motive, director goal, future reveal, or unavailable actor
knowledge, the route-decision item must use `protectedBoundaryRef` plus
generic `boundaryCategory` instead of exposing protected content.

## Forbidden Claims

The forbidden claims below must be rejected or kept unresolved:

- actual query instances;
- actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
  instances;
- plain forbidden-claim aliases for verification: actual QueryRequest,
  actual QueryResponse, actual QueryScopeReviewReport, no provider/API,
  renderer/ffmpeg/TTS, production lock;
- query execution;
- concrete refs;
- source/story/query invention;
- Qingli promotion;
- source-basis selection;
- evidence bundle creation;
- packet/candidate content;
- sample JSON;
- runtime/schema/provider/API/media work;
- provider/API, web/server, direct SQLite writes, renderer/ffmpeg/TTS, or media
  generation;
- hidden author-only or review-only material copied, paraphrased, inverted, or
  hinted into prompt surface;
- 04/05/06/07/08 restart or Phase 20/21 restart;
- approval, canon, registry, release, or production lock.

## Selected Aggregate Outcome

Selected aggregate outcome:

`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_REVIEW_STATUS_ROUTE_DECISION_CAN_ADVANCE_TO_DOCS_CONTROL_ROUTE_PACKAGE`

Rationale: all three query-ref route-decision rows preserve the upstream
review-status rows, keep concrete refs unresolved/absent, keep route decision
limited to future docs/control route-package classification, and preserve
citation/path check status, visibility, `allowedPromptSurface`,
`protectedBoundaryRef`, `boundaryCategory`, review gates, `DRAFT_FOR_REVIEW`,
and `REVIEW_PASS_NOT_APPROVAL`.

Future bounded docs/control candidate:
`query_ref_source_material_authorized_docs_control_curation_advancement_docs_control_curation_route_package_v0_docs_control`.

That future candidate is future explicit classification only,
docs/control-only, route-package-only, not auto-started, and not approval.

## Downstream Allowance

On B03 PASS, B01/A01/00 may use this docs/control curation review-status
route-decision surface to classify or dispatch a future explicit docs/control
route-package task. PASS must not auto-start actual query instance creation,
query execution, concrete refs, source-basis selection, evidence bundle
creation, actual packet work, sample JSON, story content,
runtime/schema/provider/API/media work, provider/API work, web/server work,
direct SQLite writes, 04/05/06/07/08 route, renderer/ffmpeg/TTS execution,
approval, canon, registry, release, final approval, or production lock.

## Boundary Summary

- docs/control only
- authorized docs/control curation advancement curation review-status
  route-decision only
- exactly three query-ref route-decision rows
- route can advance only to a future docs/control route-package surface
- concrete query refs remain unresolved/absent
- no actual query instances
- no query execution
- no concrete refs
- no source/story/query invention
- no Qingli promotion
- no source-basis selection
- no evidence bundle
- no packet/candidate
- no sample JSON
- no runtime/schema/provider/API/media
- no provider/API, web/server, direct SQLite writes, renderer/ffmpeg/TTS, or
  media generation
- no 04/05/06/07/08 restart
- no Phase 20/21 restart
- no approval, canon, registry, release, or production lock
