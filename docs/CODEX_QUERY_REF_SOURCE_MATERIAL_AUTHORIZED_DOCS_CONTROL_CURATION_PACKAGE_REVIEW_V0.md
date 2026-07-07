# Query Ref Source Material Authorized Docs Control Curation Package Review V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`QueryRefSourceMaterialAuthorizedDocsControlCurationPackageReview`

This document is docs/control only. It reviews and classifies the authorized
docs/control curation package surface for future query-ref source-material
curation across `QueryRequest`, `QueryResponse`, and
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

## Upstream Curation-Package Outcome

Reviewed curation-package outcome:
`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_PACKAGE_READY_FOR_REVIEW`.

This package-review surface follows:
`docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_PACKAGE_V0.md`.

The reviewed curation package records exactly one curation-package row each
for `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport`. Each row
preserves `DOCS_CONTROL_CURATION_LABEL_READY_FOR_FUTURE_REVIEW`,
`DRAFT_FOR_REVIEW`, `REVIEW_PASS_NOT_APPROVAL`, allowed input surfaces,
citation requirements, visibility, `allowedPromptSurface`,
`protectedBoundaryRef`, generic `boundaryCategory`, and review gate rules while
keeping concrete-ref status unresolved.

B03 PASS for the curation package is technical review evidence only.
`REVIEW_PASS_NOT_APPROVAL` is not user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

## Referenced Reviewed Inputs

This package-review surface cites these reviewed docs/control inputs:

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

Citation rule: every package-review row must cite an exact reviewed
docs/control source path, the reviewed curation-package outcome it relies on,
and the query-ref slot it reviews. Concrete refs must stay unresolved unless
future reviewed docs/control citations explicitly support resolution.

## Query-Ref Package-Review Rows

Each query-ref source-material package-review row appears exactly once below.
The rows confirm whether the curation package remains safe as docs/control
curation only, without resolving concrete refs.

| Package-review row | Query-ref slot | Prior curation-package row | Review label | Review classification | Preserved safety conditions | Concrete-ref status |
| --- | --- | --- | --- | --- | --- | --- |
| `PACKAGE_REVIEW_ROW_QueryRequest` | `QueryRequest` | `CURATION_PACKAGE_ROW_QueryRequest` | `PACKAGE_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_STATUS` | The request row remains safe as docs/control curation only. | Preserves allowed input surfaces, citation requirements, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gate, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved: no actual request content, no runtime query parameters, no concrete source basis, no concrete refs, and no actual `QueryRequest` instance. |
| `PACKAGE_REVIEW_ROW_QueryResponse` | `QueryResponse` | `CURATION_PACKAGE_ROW_QueryResponse` | `PACKAGE_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_STATUS` | The response row remains safe as docs/control curation only. | Preserves allowed input surfaces, citation requirements, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gate, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved: no actual query output, no provider/API output, no retrieved fact content, no concrete refs, and no actual `QueryResponse` instance. |
| `PACKAGE_REVIEW_ROW_QueryScopeReviewReport` | `QueryScopeReviewReport` | `CURATION_PACKAGE_ROW_QueryScopeReviewReport` | `PACKAGE_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_STATUS` | The scope-review row remains safe as docs/control curation only. | Preserves allowed input surfaces, citation requirements, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gate, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL`. | Unresolved: no actual scope-review verdict, no concrete query linkage, no evidence bundle, no source-basis selection, no concrete refs, and no actual `QueryScopeReviewReport` instance. |

## Package-Review Decision

The package remains safe as docs/control curation only because all three
package-review rows:

- preserve the upstream curation-package outcome
  `QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_PACKAGE_READY_FOR_REVIEW`;
- preserve `DOCS_CONTROL_CURATION_LABEL_READY_FOR_FUTURE_REVIEW`;
- preserve `PACKAGE_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_STATUS`;
- preserve `DRAFT_FOR_REVIEW` and `REVIEW_PASS_NOT_APPROVAL`;
- preserve allowed input surfaces, citation requirements, visibility,
  `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`,
  and review gate rules;
- keep concrete refs unresolved unless future reviewed docs/control citations
  explicitly support resolution;
- reject forbidden claims, including actual query instances, query execution,
  queryer implementation, concrete runtime query-ref resolution, concrete refs,
  source/story/query invention, provider/API output, media output, approval,
  canon, release, final approval, and production lock.

This review is non-approval. It is not source-basis selection, concrete
query-ref resolution, evidence bundle creation, actual packet generation,
canon, release, final approval, or production lock.

## Prompt Surface And Visibility Guardrails

This package-review surface follows
`docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`.

Fields not explicitly listed in `allowedPromptSurface` must not enter prompts,
screen descriptions, negative prompts, prompt-safe asset notes,
voice/audio planning notes, or media planning notes. Negative prompts are also
prompt surface.

Future status or route-decision work must distinguish review-only,
author-only, production-only, and future prompt-facing material. Review-only
or author-only material must not be copied, paraphrased, inverted, or hinted
into prompt-facing fields.

When future source-material notes must avoid leaking hidden causality, culprit
identity, sealed motive, director goal, future reveal, or unavailable actor
knowledge, the status item must use `protectedBoundaryRef` plus generic
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

## Selected Package-Review Outcome

Selected package-review outcome:

`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_PACKAGE_REVIEW_READY_FOR_STATUS`

Rationale: all three query-ref rows remain safe as docs/control curation only.
The review preserves unresolved concrete-ref status, citation rules,
visibility, `allowedPromptSurface`, `protectedBoundaryRef`, `boundaryCategory`,
review gates, and `REVIEW_PASS_NOT_APPROVAL`. It can name one future
status/route-decision candidate without auto-starting it or implying approval.

Future bounded docs/control status/route-decision candidate:
`query_ref_source_material_authorized_docs_control_curation_package_review_status_v0_docs_control`.

That future candidate is future explicit classification only,
docs/control-only, status/route-decision-only, not auto-started, and not
approval.

## Downstream Allowance

On B03 PASS, B01/A01/00 may use this package-review surface to classify or
dispatch a future explicit docs/control status or route-decision task. PASS
must not auto-start actual query instance creation, query execution, concrete
runtime query-ref resolution, concrete refs, source-basis selection, evidence
bundle creation, actual packet work, sample JSON, story content,
runtime/CLI/schema/validator work, 04/05/06/07/08 route,
renderer/ffmpeg/TTS execution, provider/API work, media generation, approval,
canon, registry, release, final approval, or production lock.

## Boundary Summary

- docs/control only
- authorized docs/control curation package review surface only
- exactly three query-ref package-review rows
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
