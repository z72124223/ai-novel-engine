# Query Ref Source Material Authorized Curation Route V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`QueryRefSourceMaterialAuthorizedCurationRoute`

This document is docs/control only. It defines the authorized docs/control
curation route for future query-ref source material and decides whether the
route can name exactly one future docs/control curation package candidate. It
does not create actual `QueryRequest`, `QueryResponse`, or
`QueryScopeReviewReport` instances, does not execute queries, does not
implement a queryer, does not resolve concrete runtime query refs, does not
create concrete refs, does not invent source/story/query content, does not
promote Qingli candidates, does not select source basis for actual packet work,
does not create evidence bundles, does not create an actual
`ScriptReadyScenePacket`, does not create an actual
`ScriptReadyScenePacketCandidate`, does not generate sample JSON, does not
start runtime/CLI/schema/validator work, has no provider/API work, does not run
renderer/ffmpeg/TTS, does not generate media, does not grant approval, and does
not create production lock.

## Upstream Route-Decision Outcome

Reviewed route-decision outcome:
`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_CAN_SUPPORT_FUTURE_DOCS_CONTROL_CURATION_ROUTE`.

This authorized curation route follows:
`docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_V0.md`.

The reviewed route-decision surface records exactly one route-decision row each
for `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport`. Each row
preserves `PACKAGE_ROW_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_STATUS`,
`REVIEW_PASS_NOT_APPROVAL`, citation, visibility, `allowedPromptSurface`,
`protectedBoundaryRef`, generic `boundaryCategory`, and review-gate rules while
keeping concrete-ref status unresolved.

B03 PASS for the route-decision surface is technical review evidence only.
`REVIEW_PASS_NOT_APPROVAL` is not user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

## Referenced Reviewed Inputs

This authorized curation route cites these reviewed docs/control inputs:

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

Citation rule: every curation-route row must cite an exact reviewed
docs/control source path, the reviewed route-decision outcome it relies on,
and the query-ref slot it preserves. If concrete source material is not backed
by future reviewed docs/control citations, the row must keep concrete-ref
status unresolved.

## Authorized Curation-Route Rows

Each query-ref source-material curation-route row appears exactly once below.
The route defines non-approval docs/control curation steps for each query-ref
slot without resolving concrete refs.

| Curation-route row | Query-ref slot | Prior route-decision row | Curation-route label | Required docs/control curation surface | Preserved rules | Concrete-ref status |
| --- | --- | --- | --- | --- | --- | --- |
| `CURATION_ROUTE_ROW_QueryRequest` | `QueryRequest` | `ROUTE_DECISION_ROW_QueryRequest` | `CURATION_ROUTE_NON_APPROVAL_READY_FOR_FUTURE_PACKAGE` | Future reviewed docs/control curation surface for request intent, scope, and allowed input basis only. | Preserves citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gate, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved: actual request content, runtime query parameters, concrete source basis, concrete refs, and actual `QueryRequest` instance remain absent. |
| `CURATION_ROUTE_ROW_QueryResponse` | `QueryResponse` | `ROUTE_DECISION_ROW_QueryResponse` | `CURATION_ROUTE_NON_APPROVAL_READY_FOR_FUTURE_PACKAGE` | Future reviewed docs/control curation surface for response evidence categories, source paths, and allowed citation basis only. | Preserves citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gate, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved: actual query output, retrieved fact content, provider/API output, concrete refs, and actual `QueryResponse` instance remain absent. |
| `CURATION_ROUTE_ROW_QueryScopeReviewReport` | `QueryScopeReviewReport` | `ROUTE_DECISION_ROW_QueryScopeReviewReport` | `CURATION_ROUTE_NON_APPROVAL_READY_FOR_FUTURE_PACKAGE` | Future reviewed docs/control curation surface for scope-review criteria, slot coverage, and allowed review evidence only. | Preserves citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gate, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved: actual scope-review verdict, concrete query linkage, evidence bundle creation, source-basis selection, concrete refs, and actual `QueryScopeReviewReport` instance remain absent. |

## Curation Route Summary

The authorized docs/control curation route can name one future curation package
candidate because all three curation-route rows:

- preserve the reviewed route-decision outcome
  `QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_CAN_SUPPORT_FUTURE_DOCS_CONTROL_CURATION_ROUTE`;
- preserve `PACKAGE_ROW_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_STATUS`;
- preserve `REVIEW_PASS_NOT_APPROVAL`;
- preserve citation requirements;
- preserve visibility requirements;
- preserve `allowedPromptSurface` rules;
- preserve `protectedBoundaryRef` plus generic `boundaryCategory` rules;
- preserve review gate rules;
- keep concrete refs unresolved;
- reject actual query instances, query execution, queryer implementation,
  concrete runtime query-ref resolution, source/story/query invention,
  provider/API output, media output, approval, canon, release, and production
  lock.

This curation route is route/classification-only. It does not create actual
query instances, does not resolve concrete refs, does not approve source basis,
does not create an evidence bundle, does not approve actual packet work, and
does not create canon, release, final approval, or production lock.

## Prompt Surface And Visibility Guardrails

This authorized curation route follows
`docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`.

Fields not explicitly listed in `allowedPromptSurface` must not enter prompts,
screen descriptions, negative prompts, prompt-safe asset notes,
voice/audio planning notes, or media planning notes. Negative prompts are also
prompt surface.

Future curation package work must distinguish review-only, author-only,
production-only, and future prompt-facing material. Review-only or author-only
material must not be copied, paraphrased, inverted, or hinted into
prompt-facing fields.

When future source-material notes must avoid leaking hidden causality, culprit
identity, sealed motive, director goal, future reveal, or unavailable actor
knowledge, the curation-route-derived package item must use
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

## Selected Curation-Route Outcome

Selected curation-route outcome:

`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_ROUTE_READY_FOR_FUTURE_PACKAGE`

Rationale: the reviewed route-decision outcome supports an authorized
docs/control curation route. This route preserves all three query-ref rows as
non-approval, keeps concrete-ref status unresolved, and retains citation,
visibility, `allowedPromptSurface`, `protectedBoundaryRef`,
`boundaryCategory`, review gates, and `REVIEW_PASS_NOT_APPROVAL`. Therefore,
it can name one future docs/control curation package candidate without
auto-starting it or implying approval.

Future bounded docs/control curation package candidate:
`query_ref_source_material_authorized_curation_route_package_v0_docs_control`.

That future candidate is future explicit classification only,
docs/control-only, classification/curation/review/status/package-review/status/
route-decision/route/classification-only, not auto-started, and not approval.

## Downstream Allowance

On B03 PASS, B01/A01/00 may use this authorized curation route to classify or
dispatch a future explicit docs/control package. PASS must not auto-start
actual query instance creation, query execution, concrete runtime query-ref
resolution, concrete refs, source-basis selection, evidence bundle creation,
actual packet work, sample JSON, story content, runtime/CLI/schema/validator
work, 04/05/06/07/08 route, renderer/ffmpeg/TTS execution, provider/API work,
media generation, approval, canon, registry, release, final approval, or
production lock.

## Boundary Summary

- docs/control only
- authorized curation route only
- exactly three query-ref curation-route rows
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
