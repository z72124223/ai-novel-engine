# Query Ref Source Material Authorized Docs Control Curation Package V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`QueryRefSourceMaterialAuthorizedDocsControlCurationPackage`

This document is docs/control only. It defines an authorized docs/control
curation package surface for future query-ref source-material curation across
`QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport`. It does not
create actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
instances, does not execute queries, does not implement a queryer, does not
resolve concrete runtime query refs, does not create concrete refs, does not
invent source/story/query content, does not promote Qingli candidates, does
not select source basis for actual packet work, does not create evidence
bundles, does not create an actual `ScriptReadyScenePacket`, does not create
an actual `ScriptReadyScenePacketCandidate`, does not generate sample JSON,
does not start runtime/CLI/schema/validator work, has no provider/API work,
does not run renderer/ffmpeg/TTS, does not generate media, does not grant
approval, and does not create production lock.

## Upstream Route-Advance Outcome

Reviewed route-advance outcome:
`QUERY_REF_SOURCE_MATERIAL_ROUTE_CAN_ADVANCE_TO_DOCS_CONTROL_CURATION`.

This curation package follows:
`docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_ROUTE_PACKAGE_REVIEW_STATUS_ROUTE_DECISION_V0.md`.

The reviewed route-decision surface records exactly one route-decision row each
for `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport`. Each row
preserves `ROUTE_DECISION_CAN_ADVANCE_DOCS_CONTROL_CURATION_WITH_UNRESOLVED_REFS`,
`DRAFT_FOR_REVIEW`, `REVIEW_PASS_NOT_APPROVAL`, citation, visibility,
`allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`,
and review gate rules while keeping concrete-ref status unresolved.

B03 PASS for the route-decision surface is technical review evidence only.
`REVIEW_PASS_NOT_APPROVAL` is not user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

## Referenced Reviewed Inputs

This curation package cites these reviewed docs/control inputs:

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

Citation rule: every curation-package row must cite an exact reviewed
docs/control source path, the reviewed route-advance outcome it relies on, and
the query-ref slot it curates. Concrete refs must stay unresolved unless
future reviewed docs/control citations explicitly support resolution.

## Authorized Curation Package Rows

Each query-ref source-material curation-package row appears exactly once below.
The rows define allowed docs/control curation surfaces only; they do not create
actual query instances and do not resolve concrete refs.

| Curation-package row | Query-ref slot | Prior route-decision row | Package label | Allowed input surfaces | Citation requirements | Visibility and prompt boundary | Concrete-ref status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `CURATION_PACKAGE_ROW_QueryRequest` | `QueryRequest` | `ROUTE_DECISION_ROW_QueryRequest` | `DOCS_CONTROL_CURATION_LABEL_READY_FOR_FUTURE_REVIEW` | Reviewed docs/control route facts, reviewed curation authority, reviewed source-material criteria, request-scope placeholders, intended-use placeholders, and source-path placeholders only. | Must cite a reviewed docs/control path, the source-material curation rule, the query-ref slot, and the review gate before any future request material can advance. | Must preserve visibility classification, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved: no actual request content, no runtime query parameters, no concrete source basis, no concrete refs, and no actual `QueryRequest` instance. |
| `CURATION_PACKAGE_ROW_QueryResponse` | `QueryResponse` | `ROUTE_DECISION_ROW_QueryResponse` | `DOCS_CONTROL_CURATION_LABEL_READY_FOR_FUTURE_REVIEW` | Reviewed docs/control route facts, reviewed curation authority, reviewed source-material criteria, response-evidence placeholders, reviewed-source placeholders, and rejection-reason placeholders only. | Must cite a reviewed docs/control path, the source-material curation rule, the query-ref slot, and the review gate before any future response material can advance. | Must preserve visibility classification, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved: no actual query output, no provider/API output, no retrieved fact content, no concrete refs, and no actual `QueryResponse` instance. |
| `CURATION_PACKAGE_ROW_QueryScopeReviewReport` | `QueryScopeReviewReport` | `ROUTE_DECISION_ROW_QueryScopeReviewReport` | `DOCS_CONTROL_CURATION_LABEL_READY_FOR_FUTURE_REVIEW` | Reviewed docs/control route facts, reviewed curation authority, reviewed source-material criteria, scope-coverage placeholders, review-evidence placeholders, and boundary-check placeholders only. | Must cite a reviewed docs/control path, the source-material curation rule, the query-ref slot, and the review gate before any future scope-review material can advance. | Must preserve visibility classification, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved: no actual scope-review verdict, no concrete query linkage, no evidence bundle, no source-basis selection, no concrete refs, and no actual `QueryScopeReviewReport` instance. |

## Package-Level Review Gates

The curation package can name one future review/status candidate because all
three curation-package rows:

- preserve the upstream route-advance outcome
  `QUERY_REF_SOURCE_MATERIAL_ROUTE_CAN_ADVANCE_TO_DOCS_CONTROL_CURATION`;
- preserve `ROUTE_DECISION_CAN_ADVANCE_DOCS_CONTROL_CURATION_WITH_UNRESOLVED_REFS`;
- preserve `DOCS_CONTROL_CURATION_LABEL_READY_FOR_FUTURE_REVIEW`;
- preserve `DRAFT_FOR_REVIEW` and `REVIEW_PASS_NOT_APPROVAL`;
- preserve citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`,
  generic `boundaryCategory`, and review gate rules;
- keep concrete refs unresolved unless future reviewed docs/control citations
  explicitly support resolution;
- reject forbidden claims, including actual query instances, query execution,
  queryer implementation, concrete runtime query-ref resolution, concrete refs,
  source/story/query invention, provider/API output, media output, approval,
  canon, release, final approval, and production lock.

This package is curation-surface only. It is not source-basis selection,
concrete query-ref resolution, evidence bundle creation, actual packet
generation, canon, release, final approval, or production lock.

## Prompt Surface And Visibility Guardrails

This curation package follows
`docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`.

Fields not explicitly listed in `allowedPromptSurface` must not enter prompts,
screen descriptions, negative prompts, prompt-safe asset notes,
voice/audio planning notes, or media planning notes. Negative prompts are also
prompt surface.

Future review/status work must distinguish review-only, author-only,
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

## Selected Curation-Package Outcome

Selected curation-package outcome:

`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_PACKAGE_READY_FOR_REVIEW`

Rationale: all three query-ref rows can advance to future review/status only
because this package remains docs/control curation surface and does not require
concrete query refs. Concrete refs, actual query instances, source basis,
evidence bundles, runtime work, approval, canon, release, final approval, and
production lock remain forbidden.

Future bounded docs/control review/status candidate:
`query_ref_source_material_authorized_docs_control_curation_package_review_v0_docs_control`.

That future candidate is future explicit classification only,
docs/control-only, review/status-only, not auto-started, and not approval.

## Downstream Allowance

On B03 PASS, B01/A01/00 may use this curation package to classify or dispatch
a future explicit docs/control review/status task. PASS must not auto-start
actual query instance creation, query execution, concrete runtime query-ref
resolution, concrete refs, source-basis selection, evidence bundle creation,
actual packet work, sample JSON, story content, runtime/CLI/schema/validator
work, 04/05/06/07/08 route, renderer/ffmpeg/TTS execution, provider/API work,
media generation, approval, canon, registry, release, final approval, or
production lock.

## Boundary Summary

- docs/control only
- authorized docs/control curation package surface only
- exactly three query-ref curation-package rows
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
