# Query Ref Source Material Authorized Curation Route Package Review Status Route Decision V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`QueryRefSourceMaterialAuthorizedCurationRoutePackageReviewStatusRouteDecision`

This document is docs/control only. It decides whether the reviewed
route-package review-status surface can advance toward future docs/control
source-material curation for `QueryRequest`, `QueryResponse`, and
`QueryScopeReviewReport`. It does not create actual `QueryRequest`,
`QueryResponse`, or `QueryScopeReviewReport` instances, does not execute
queries, does not implement a queryer, does not resolve concrete runtime query
refs, does not create concrete refs, does not invent source/story/query
content, does not promote Qingli candidates, does not select source basis for
actual packet work, does not create evidence bundles, does not create an
actual `ScriptReadyScenePacket`, does not create an actual
`ScriptReadyScenePacketCandidate`, does not generate sample JSON, does not
start runtime/CLI/schema/validator work, has no provider/API work, does not run
renderer/ffmpeg/TTS, does not generate media, does not grant approval, and does
not create production lock.

## Upstream Review-Status Outcome

Reviewed review-status outcome:
`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_ROUTE_PACKAGE_REVIEW_STATUS_READY_FOR_ROUTE_DECISION`.

This route-decision surface follows:
`docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_ROUTE_PACKAGE_REVIEW_STATUS_V0.md`.

The reviewed review-status surface records exactly one non-approval row each
for `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport`. Each row
preserves `ROUTE_PACKAGE_NON_APPROVAL_READY_FOR_FUTURE_REVIEW_STATUS`,
`ROUTE_PACKAGE_REVIEW_STATUS_NON_APPROVAL_READY_FOR_ROUTE_DECISION`,
`DRAFT_FOR_REVIEW`, `REVIEW_PASS_NOT_APPROVAL`, citation, visibility,
`allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`,
and review gate rules while keeping concrete-ref status unresolved.

B03 PASS for the review-status surface is technical review evidence only.
`REVIEW_PASS_NOT_APPROVAL` is not user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

## Referenced Reviewed Inputs

This route-decision surface cites these reviewed docs/control inputs:

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
docs/control source path, the reviewed review-status outcome it relies on, and
the query-ref slot it classifies. Concrete refs must stay unresolved unless
future reviewed docs/control citations explicitly support resolution.

## Query-Ref Route-Decision Rows

Each query-ref source-material route-decision row appears exactly once below.
The rows decide whether the route can advance or must block, without resolving
concrete refs.

| Route-decision row | Query-ref slot | Prior review-status row | Preserved status label | Slot decision | Why this does not resolve refs | Preserved guardrails |
| --- | --- | --- | --- | --- | --- | --- |
| `ROUTE_DECISION_ROW_QueryRequest` | `QueryRequest` | `REVIEW_STATUS_ROW_QueryRequest` | `ROUTE_PACKAGE_REVIEW_STATUS_NON_APPROVAL_READY_FOR_ROUTE_DECISION` | `ROUTE_DECISION_CAN_ADVANCE_DOCS_CONTROL_CURATION_WITH_UNRESOLVED_REFS` | The missing request content, runtime query parameters, concrete source basis, and concrete refs remain unresolved. They do not block a future docs/control curation package because that future package must still preserve citation and review gates before any concrete ref can be promoted. | Preserves citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gate, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. |
| `ROUTE_DECISION_ROW_QueryResponse` | `QueryResponse` | `REVIEW_STATUS_ROW_QueryResponse` | `ROUTE_PACKAGE_REVIEW_STATUS_NON_APPROVAL_READY_FOR_ROUTE_DECISION` | `ROUTE_DECISION_CAN_ADVANCE_DOCS_CONTROL_CURATION_WITH_UNRESOLVED_REFS` | The missing query output, retrieved fact content, provider/API output, concrete refs, and actual response instance remain unresolved. They do not block a future docs/control curation package because no runtime output or provider/API material is accepted as evidence here. | Preserves citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gate, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. |
| `ROUTE_DECISION_ROW_QueryScopeReviewReport` | `QueryScopeReviewReport` | `REVIEW_STATUS_ROW_QueryScopeReviewReport` | `ROUTE_PACKAGE_REVIEW_STATUS_NON_APPROVAL_READY_FOR_ROUTE_DECISION` | `ROUTE_DECISION_CAN_ADVANCE_DOCS_CONTROL_CURATION_WITH_UNRESOLVED_REFS` | The missing scope-review verdict, concrete query linkage, evidence bundle, source-basis selection, and concrete refs remain unresolved. They do not block a future docs/control curation package because future work must still classify and cite reviewed source material before any review report can exist. | Preserves citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gate, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. |

## Aggregate Route Decision

The aggregate route can advance to one future docs/control curation package
because all three route-decision rows:

- preserve the upstream review-status outcome
  `QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_ROUTE_PACKAGE_REVIEW_STATUS_READY_FOR_ROUTE_DECISION`;
- preserve `ROUTE_PACKAGE_REVIEW_STATUS_NON_APPROVAL_READY_FOR_ROUTE_DECISION`;
- preserve `DRAFT_FOR_REVIEW` and `REVIEW_PASS_NOT_APPROVAL`;
- preserve citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`,
  generic `boundaryCategory`, and review gate rules;
- keep concrete refs unresolved unless future reviewed docs/control citations
  explicitly support resolution;
- reject forbidden claims, including actual query instances, query execution,
  queryer implementation, concrete runtime query-ref resolution, concrete refs,
  source/story/query invention, provider/API output, media output, approval,
  canon, release, final approval, and production lock.

Absent concrete refs do not block this route because the next allowed step is
still docs/control curation only. The route would block only if the next step
required actual query instances, concrete runtime refs, source-basis selection,
or approval. No reviewed evidence supports a true-stop condition for this
slice.

## Prompt Surface And Visibility Guardrails

This route-decision surface follows
`docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`.

Fields not explicitly listed in `allowedPromptSurface` must not enter prompts,
screen descriptions, negative prompts, prompt-safe asset notes,
voice/audio planning notes, or media planning notes. Negative prompts are also
prompt surface.

Future docs/control curation must distinguish review-only, author-only,
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

## Selected Aggregate Outcome

Selected aggregate outcome:

`QUERY_REF_SOURCE_MATERIAL_ROUTE_CAN_ADVANCE_TO_DOCS_CONTROL_CURATION`

Rationale: all three query-ref route-decision rows can advance only because the
next step remains docs/control curation and does not require concrete query
refs. The route is not blocked by absent concrete refs at this layer, and no
reviewed evidence establishes a true-stop condition. Concrete refs, actual
query instances, source basis, evidence bundles, runtime work, approval,
canon, release, final approval, and production lock remain forbidden.

Future bounded docs/control curation package candidate:
`query_ref_source_material_authorized_docs_control_curation_package_v0_docs_control`.

That future candidate is future explicit classification only,
docs/control-only, classification/curation-only, not auto-started, and not
approval.

## Downstream Allowance

On B03 PASS, B01/A01/00 may use this route-decision surface to classify or
dispatch a future explicit docs/control curation task. PASS must not
auto-start actual query instance creation, query execution, concrete runtime
query-ref resolution, concrete refs, source-basis selection, evidence bundle
creation, actual packet work, sample JSON, story content,
runtime/CLI/schema/validator work, 04/05/06/07/08 route,
renderer/ffmpeg/TTS execution, provider/API work, media generation, approval,
canon, registry, release, final approval, or production lock.

## Boundary Summary

- docs/control only
- route-package review-status route-decision surface only
- exactly three query-ref route-decision rows
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
