# Query Ref Source Material Authorized Docs Control Curation Advancement Docs Control Curation V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`QueryRefSourceMaterialAuthorizedDocsControlCurationAdvancementDocsControlCuration`

This document is docs/control only. It defines a planning/control curation
surface for future query-ref source material across `QueryRequest`,
`QueryResponse`, and `QueryScopeReviewReport`. The curation surface preserves
the reviewed route-decision state and keeps concrete refs unresolved/absent.
It does not create actual `QueryRequest`, `QueryResponse`, or
`QueryScopeReviewReport` instances, does not execute queries, does not
implement a queryer, does not resolve concrete runtime query refs, does not
create concrete refs, does not invent source/story/query content, does not
promote Qingli candidates, does not select source basis, does not create an
evidence bundle, does not create an actual packet/candidate, does not generate
sample JSON, does not start runtime/schema/provider/API/web/server/SQLite/media
work, does not write SQLite directly, does not run renderer/ffmpeg/TTS, does
not grant approval, and does not create production lock.

## Upstream Route-Decision Outcome

Reviewed aggregate route-decision outcome:
`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_ROUTE_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_CAN_ADVANCE_TO_DOCS_CONTROL_CURATION`.

This docs/control curation surface follows:
`docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_ROUTE_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_V0.md`.

The reviewed route-decision surface records exactly one route-decision row each
for `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport`. Each row
preserves its upstream `CURATION_ADVANCEMENT_ROUTE_PACKAGE_REVIEW_STATUS_ROW_*`,
prior `ROUTE_PACKAGE_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_STATUS`,
review-status `ROUTE_PACKAGE_REVIEW_STATUS_PASS_NOT_APPROVAL_FOR_FUTURE_ROUTE_DECISION`,
and route decision `ROUTE_DECISION_CAN_ADVANCE_TO_DOCS_CONTROL_CURATION_ONLY`.
Those facts authorize only this docs/control curation surface; they do not
authorize concrete refs, actual query instances, query content, evidence
bundles, source-basis selection, actual packet work, approval, canon, release,
or production lock.

B03 PASS for the upstream route-decision surface is technical review evidence
only. `REVIEW_PASS_NOT_APPROVAL` is not user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

## Referenced Reviewed Inputs

This docs/control curation surface cites these reviewed docs/control inputs:

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

Citation rule: every curation row must cite an exact reviewed docs/control
source path, the reviewed aggregate route-decision outcome it relies on, and
the query-ref slot it curates. Concrete refs must stay unresolved/absent unless
future reviewed docs/control citations explicitly support resolution.

## Docs/Control Curation Rows

Each query-ref source-material docs/control curation row appears exactly once
below. The rows curate planning/control surfaces only; they do not resolve
concrete refs.

| Curation row | Query-ref slot | Upstream route-decision row | Curation status | Allowed curation surface | Preserved guardrails | Concrete-ref status |
| --- | --- | --- | --- | --- | --- | --- |
| `CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_ROW_QueryRequest` | `QueryRequest` | `CURATION_ADVANCEMENT_ROUTE_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_ROW_QueryRequest` | `DOCS_CONTROL_CURATION_READY_FOR_REVIEW_STATUS` because the row remains docs/control-only, non-approval, and does not create request content or runtime parameters. | Curation may organize reviewed docs/control citation requirements, visibility classification, prompt-surface eligibility, protected-boundary references, and review gates for a future request source-material surface. | Preserves exact citation expectations, visibility classification, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gates, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved/absent: no actual request content, no runtime query parameters, no concrete source basis, no concrete refs, and no actual `QueryRequest` instance. |
| `CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_ROW_QueryResponse` | `QueryResponse` | `CURATION_ADVANCEMENT_ROUTE_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_ROW_QueryResponse` | `DOCS_CONTROL_CURATION_READY_FOR_REVIEW_STATUS` because the row remains docs/control-only, non-approval, and does not create query output or provider results. | Curation may organize reviewed docs/control citation requirements, visibility classification, prompt-surface eligibility, protected-boundary references, and review gates for a future response source-material surface. | Preserves exact citation expectations, visibility classification, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gates, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved/absent: no actual query output, no provider/API output, no retrieved fact content, no concrete refs, and no actual `QueryResponse` instance. |
| `CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_ROW_QueryScopeReviewReport` | `QueryScopeReviewReport` | `CURATION_ADVANCEMENT_ROUTE_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_ROW_QueryScopeReviewReport` | `DOCS_CONTROL_CURATION_READY_FOR_REVIEW_STATUS` because the row remains docs/control-only, non-approval, and does not create scope-review evidence or verdicts. | Curation may organize reviewed docs/control citation requirements, visibility classification, prompt-surface eligibility, protected-boundary references, and review gates for a future scope-review source-material surface. | Preserves exact citation expectations, visibility classification, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gates, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved/absent: no actual scope-review verdict, no concrete query linkage, no evidence bundle, no source-basis selection, no concrete refs, and no actual `QueryScopeReviewReport` instance. |

## Curation Findings

The docs/control curation surface can safely name one future review/status
candidate because it:

- preserves the reviewed aggregate route-decision outcome;
- preserves all three route-decision rows as curation rows without changing
  their unresolved concrete-ref state;
- keeps curation limited to docs/control planning/control organization;
- does not create actual `QueryRequest`, `QueryResponse`, or
  `QueryScopeReviewReport` instances;
- does not execute queries, implement queryers, or resolve concrete runtime
  query refs;
- requires exact reviewed docs/control citations before any future resolution;
- preserves visibility, `allowedPromptSurface`, `protectedBoundaryRef`,
  generic `boundaryCategory`, review gates, `DRAFT_FOR_REVIEW`, and
  `REVIEW_PASS_NOT_APPROVAL`;
- rejects forbidden claims, including actual query instances, source/story/query
  invention, provider/API output, direct SQLite writes, media output, approval,
  canon, release, final approval, and production lock.

Absent concrete refs continue to block actual query-ref resolution, actual
query instances, evidence bundles, source-basis selection, and actual packet
work. They do not block a future docs/control-only review/status surface that
continues to preserve absent refs and keeps the next step classification-only.

This curation is non-approval. It is not source-basis selection, concrete
query-ref resolution, evidence bundle creation, actual packet generation,
canon, release, final approval, or production lock.

## Prompt Surface And Visibility Guardrails

This docs/control curation surface follows
`docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`.

Fields not explicitly listed in `allowedPromptSurface` must not enter prompts,
screen descriptions, negative prompts, prompt-safe asset notes,
voice/audio planning notes, or media planning notes. Negative prompts are also
prompt surface.

Future docs/control review/status work must distinguish review-only,
author-only, production-only, and future prompt-facing material. Review-only or
author-only material must not be copied, paraphrased, inverted, or hinted into
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
- source-basis selection;
- evidence bundle creation;
- actual packet/candidate content;
- sample JSON;
- runtime/schema/provider/API/web/server/SQLite/media work;
- direct SQLite writes;
- renderer/ffmpeg/TTS;
- hidden author-only or review-only material copied, paraphrased, inverted, or
  hinted into prompt surface;
- 04/05/06/07/08 restart or Phase 20/21 restart;
- approval, canon, registry, release, final approval, or production lock.

## Selected Docs/Control Curation Outcome

Selected docs/control curation outcome:

`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_READY_FOR_REVIEW`

Rationale: all three query-ref curation rows preserve the reviewed
route-decision state, keep concrete refs unresolved/absent, keep curation
limited to future docs/control review/status classification, and preserve
citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`,
`boundaryCategory`, review gates, and `REVIEW_PASS_NOT_APPROVAL`.

Future bounded docs/control review/status candidate:
`query_ref_source_material_authorized_docs_control_curation_advancement_docs_control_curation_review_v0_docs_control`.

That future candidate is future explicit classification only,
docs/control-only, review/status-only, not auto-started, and not approval.

## Downstream Allowance

On B03 PASS, B01/A01/00 may use this docs/control curation surface to classify
or dispatch a future explicit docs/control review/status task. PASS must not
auto-start actual query instance creation, query execution, concrete runtime
query-ref resolution, concrete refs, source-basis selection, evidence bundle
creation, actual packet work, sample JSON, story content,
runtime/schema/provider/API/web/server/SQLite/media work, direct SQLite writes,
04/05/06/07/08 route, renderer/ffmpeg/TTS execution, approval, canon, registry,
release, final approval, or production lock.

## Boundary Summary

- docs/control only
- authorized docs/control curation advancement docs/control curation only
- exactly three query-ref curation rows
- curation can advance only to a future docs/control review/status surface
- concrete query refs remain unresolved/absent
- no actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
  instances
- no query execution
- no queryer implementation
- no concrete runtime query-ref resolution
- no concrete refs
- no source/story/query invention
- no Qingli promotion
- no source-basis selection
- no evidence bundle
- no actual packet/candidate
- no sample JSON
- no runtime/schema/provider/API/web/server/SQLite/media
- no direct SQLite writes
- no renderer/ffmpeg/TTS
- no 04/05/06/07/08 restart
- no Phase 20/21 restart
- no approval, canon, registry, release, final approval, or production lock
