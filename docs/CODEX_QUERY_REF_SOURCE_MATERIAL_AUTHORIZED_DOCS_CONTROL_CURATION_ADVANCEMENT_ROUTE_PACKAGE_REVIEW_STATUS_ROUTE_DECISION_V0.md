# Query Ref Source Material Authorized Docs Control Curation Advancement Route Package Review Status Route Decision V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`QueryRefSourceMaterialAuthorizedDocsControlCurationAdvancementRoutePackageReviewStatusRouteDecision`

This document is docs/control only. It records a route-decision surface for the
curation advancement route-package review-status rows across `QueryRequest`,
`QueryResponse`, and `QueryScopeReviewReport`. The route-decision surface is
limited to deciding whether a future explicit docs/control curation step may be
classified. It does not create actual `QueryRequest`, `QueryResponse`, or
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

## Upstream Review-Status Outcome

Reviewed route-package review-status outcome:
`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_ROUTE_PACKAGE_REVIEW_STATUS_READY_FOR_ROUTE_DECISION`.

This route-decision surface follows:
`docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_ROUTE_PACKAGE_REVIEW_STATUS_V0.md`.

The reviewed route-package review-status surface records exactly one
review-status row each for `QueryRequest`, `QueryResponse`, and
`QueryScopeReviewReport`. Each row follows its upstream
`CURATION_ADVANCEMENT_ROUTE_PACKAGE_REVIEW_ROW_*`, preserves
`ROUTE_PACKAGE_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_STATUS`, uses
`ROUTE_PACKAGE_REVIEW_STATUS_PASS_NOT_APPROVAL_FOR_FUTURE_ROUTE_DECISION`, keeps
concrete refs unresolved/absent, and preserves citation, visibility,
`allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`,
review gates, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`.

B03 PASS for the upstream route-package review-status surface is technical
review evidence only. `REVIEW_PASS_NOT_APPROVAL` is not user approval, asset
approval, registry approval, canon, release, final media approval,
provider/media handoff, final approval, or production lock.

## Referenced Reviewed Inputs

This curation advancement route-package review-status route-decision surface
cites these reviewed docs/control inputs:

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
docs/control source path, the reviewed route-package review-status outcome it
relies on, and the query-ref slot it decides. Concrete refs must stay
unresolved/absent unless future reviewed docs/control citations explicitly
support resolution.

## Curation Advancement Route-Package Review-Status Route-Decision Rows

Each query-ref source-material route-decision row appears exactly once below.
The rows decide route state only; they do not resolve concrete refs.

| Route-decision row | Query-ref slot | Upstream review-status row | Prior review classification | Review-status classification | Route decision | Preserved guardrails | Concrete-ref status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `CURATION_ADVANCEMENT_ROUTE_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_ROW_QueryRequest` | `QueryRequest` | `CURATION_ADVANCEMENT_ROUTE_PACKAGE_REVIEW_STATUS_ROW_QueryRequest` | `ROUTE_PACKAGE_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_STATUS` | `ROUTE_PACKAGE_REVIEW_STATUS_PASS_NOT_APPROVAL_FOR_FUTURE_ROUTE_DECISION` | `ROUTE_DECISION_CAN_ADVANCE_TO_DOCS_CONTROL_CURATION_ONLY` because the row remains docs/control-only, non-approval, and does not resolve concrete request inputs. | Preserves exact citation expectations, visibility classification, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gates, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved/absent: no actual request content, no runtime query parameters, no concrete source basis, no concrete refs, and no actual `QueryRequest` instance. |
| `CURATION_ADVANCEMENT_ROUTE_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_ROW_QueryResponse` | `QueryResponse` | `CURATION_ADVANCEMENT_ROUTE_PACKAGE_REVIEW_STATUS_ROW_QueryResponse` | `ROUTE_PACKAGE_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_STATUS` | `ROUTE_PACKAGE_REVIEW_STATUS_PASS_NOT_APPROVAL_FOR_FUTURE_ROUTE_DECISION` | `ROUTE_DECISION_CAN_ADVANCE_TO_DOCS_CONTROL_CURATION_ONLY` because the row remains docs/control-only, non-approval, and does not resolve concrete query output. | Preserves exact citation expectations, visibility classification, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gates, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved/absent: no actual query output, no provider/API output, no retrieved fact content, no concrete refs, and no actual `QueryResponse` instance. |
| `CURATION_ADVANCEMENT_ROUTE_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_ROW_QueryScopeReviewReport` | `QueryScopeReviewReport` | `CURATION_ADVANCEMENT_ROUTE_PACKAGE_REVIEW_STATUS_ROW_QueryScopeReviewReport` | `ROUTE_PACKAGE_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_STATUS` | `ROUTE_PACKAGE_REVIEW_STATUS_PASS_NOT_APPROVAL_FOR_FUTURE_ROUTE_DECISION` | `ROUTE_DECISION_CAN_ADVANCE_TO_DOCS_CONTROL_CURATION_ONLY` because the row remains docs/control-only, non-approval, and does not resolve concrete scope-review evidence or verdicts. | Preserves exact citation expectations, visibility classification, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gates, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved/absent: no actual scope-review verdict, no concrete query linkage, no evidence bundle, no source-basis selection, no concrete refs, and no actual `QueryScopeReviewReport` instance. |

## Aggregate Route Decision

The route can advance only to a future explicit docs/control curation
classification surface because every row:

- preserves the reviewed route-package review-status outcome;
- preserves the prior route-package review and review-status classifications;
- preserves all three query-ref slots as non-approval route-decision surfaces;
- keeps advancement limited to docs/control curation/review/status/
  route-decision surfaces;
- keeps concrete refs and actual query content absent/unresolved;
- requires exact reviewed docs/control citations before any future resolution;
- preserves visibility, `allowedPromptSurface`, `protectedBoundaryRef`,
  generic `boundaryCategory`, review gates, `DRAFT_FOR_REVIEW`, and
  `REVIEW_PASS_NOT_APPROVAL`;
- rejects forbidden claims, including actual query instances, query execution,
  queryer implementation, concrete runtime query-ref resolution, concrete refs,
  source/story/query invention, provider/API output, media output, approval,
  canon, release, final approval, and production lock.

Absent concrete refs continue to block actual query-ref resolution, actual
query instances, evidence bundles, source-basis selection, and actual packet
work. They do not block a future docs/control-only curation surface that
continues to preserve absent refs and keeps the next step classification-only.
No true stop condition is selected because the reviewed facts support only
bounded docs/control advancement and still reject actual content or runtime
work.

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

`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_ROUTE_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_CAN_ADVANCE_TO_DOCS_CONTROL_CURATION`

Rationale: all three query-ref route-decision rows preserve the reviewed
route-package review-status state, keep concrete refs unresolved/absent, keep
route advancement limited to future docs/control curation classification, and
preserve citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`,
`boundaryCategory`, review gates, and `REVIEW_PASS_NOT_APPROVAL`.

Future bounded docs/control candidate:
`query_ref_source_material_authorized_docs_control_curation_advancement_docs_control_curation_v0_docs_control`.

That future candidate is future explicit classification only,
docs/control-only, curation-only, not auto-started, and not approval.

## Downstream Allowance

On B03 PASS, B01/A01/00 may use this route-decision surface to classify or
dispatch a future explicit docs/control curation task. PASS must not auto-start
actual query instance creation, query execution, concrete runtime query-ref
resolution, concrete refs, source-basis selection, evidence bundle creation,
actual packet work, sample JSON, story content, runtime/CLI/schema/validator
work, 04/05/06/07/08 route, renderer/ffmpeg/TTS execution, provider/API work,
media generation, approval, canon, registry, release, final approval, or
production lock.

## Boundary Summary

- docs/control only
- authorized docs/control curation advancement route-package review-status
  route-decision only
- exactly three query-ref route-decision rows
- route can advance only to a future docs/control curation surface
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
- no actual `ScriptReadyScenePacket` or `ScriptReadyScenePacketCandidate`
- no sample JSON
- no runtime/CLI/schema/validator work
- no provider/API, web/server, direct SQLite writes, renderer/ffmpeg/TTS, or
  media generation
- no 04/05/06/07/08 restart
- no Phase 20/21 restart
- no approval, canon, registry, release, final approval, or production lock
