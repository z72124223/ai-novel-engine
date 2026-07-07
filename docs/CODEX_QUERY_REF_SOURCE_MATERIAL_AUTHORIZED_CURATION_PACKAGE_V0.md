# Query Ref Source Material Authorized Curation Package V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`QueryRefSourceMaterialAuthorizedCurationPackage`

This document is docs/control only. It defines the authorized curation package
surface for future query-ref source material and preserves unresolved
concrete-ref status. It does not create actual `QueryRequest`,
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

## Upstream Route Decision

Reviewed route-decision outcome:
`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_REVIEW_STATUS_ROUTE_DECISION_CAN_NAME_FUTURE_CURATION_PACKAGE`.

This authorized curation package follows:
`docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_REVIEW_STATUS_ROUTE_DECISION_V0.md`.

The reviewed route-decision surface preserves exactly one route-decision row
each for `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport`. It
keeps concrete refs unresolved and preserves `REVIEW_PASS_NOT_APPROVAL`,
visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic
`boundaryCategory`, citation, and review-gate rules.

B03 PASS for the route-decision surface is technical review evidence only.
`REVIEW_PASS_NOT_APPROVAL` is not user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

## Referenced Reviewed Inputs

This authorized curation package cites these reviewed docs/control inputs:

- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_REVIEW_STATUS_ROUTE_DECISION_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_REVIEW_STATUS_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKET_REVIEW_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKET_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PLAN_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORITY_CLASSIFICATION_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_ROUTE_DECISION_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PROTOCOL_V0.md`
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

Citation rule: every authorized curation package row must cite an exact
reviewed docs/control source path, the reviewed route-decision outcome it
relies on, and the query-ref slot it preserves. If concrete source material is
not backed by future reviewed docs/control citations, the row must keep
concrete-ref status unresolved.

## Authorized Curation Package Rows

Each query-ref authorized curation package row appears exactly once below. The
rows define allowed source-material surface, citation requirements, visibility,
`allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`,
review gate, and non-approval status without resolving concrete refs.

| Authorized curation package row | Query-ref slot | Allowed source-material surface | Citation requirements | Visibility and prompt-surface rules | Protected-boundary rules | Review gate and non-approval status | Concrete-ref status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `AUTHORIZED_CURATION_PACKAGE_ROW_QueryRequest` | `QueryRequest` | Reviewed docs/control source-material surface for future request intent, scope, and allowed input basis only. | Must cite reviewed docs/control paths and the upstream route-decision outcome before any future request ref can be promoted. | Must preserve visibility classification and list any prompt-eligible field only through `allowedPromptSurface`; fields outside it cannot enter prompts, screen descriptions, negative prompts, prompt-safe asset notes, voice/audio planning notes, or media planning notes. | Must use `protectedBoundaryRef` plus generic `boundaryCategory` when request notes touch hidden causality, culprit identity, sealed motive, director goal, future reveal, or unavailable actor knowledge. | `DRAFT_FOR_REVIEW`; `REVIEW_PASS_NOT_APPROVAL`; future review gate required before any curation row can feed later package work. | Unresolved: actual request content, runtime query parameters, concrete source basis, concrete refs, and actual `QueryRequest` instance remain absent unless future reviewed docs explicitly support resolution. |
| `AUTHORIZED_CURATION_PACKAGE_ROW_QueryResponse` | `QueryResponse` | Reviewed docs/control source-material surface for future response evidence categories, source paths, and allowed citation basis only. | Must cite reviewed docs/control paths and the upstream route-decision outcome before any future response ref can be promoted. | Must preserve visibility classification and list any prompt-eligible field only through `allowedPromptSurface`; fields outside it cannot enter prompts, screen descriptions, negative prompts, prompt-safe asset notes, voice/audio planning notes, or media planning notes. | Must use `protectedBoundaryRef` plus generic `boundaryCategory` when response notes touch hidden causality, culprit identity, sealed motive, director goal, future reveal, or unavailable actor knowledge. | `DRAFT_FOR_REVIEW`; `REVIEW_PASS_NOT_APPROVAL`; future review gate required before any curation row can feed later package work. | Unresolved: actual query output, retrieved fact content, provider/API output, concrete refs, and actual `QueryResponse` instance remain absent unless future reviewed docs explicitly support resolution. |
| `AUTHORIZED_CURATION_PACKAGE_ROW_QueryScopeReviewReport` | `QueryScopeReviewReport` | Reviewed docs/control source-material surface for future scope-review criteria, slot coverage, and allowed review evidence only. | Must cite reviewed docs/control paths and the upstream route-decision outcome before any future scope-review ref can be promoted. | Must preserve visibility classification and list any prompt-eligible field only through `allowedPromptSurface`; fields outside it cannot enter prompts, screen descriptions, negative prompts, prompt-safe asset notes, voice/audio planning notes, or media planning notes. | Must use `protectedBoundaryRef` plus generic `boundaryCategory` when scope-review notes touch hidden causality, culprit identity, sealed motive, director goal, future reveal, or unavailable actor knowledge. | `DRAFT_FOR_REVIEW`; `REVIEW_PASS_NOT_APPROVAL`; future review gate required before any curation row can feed later package work. | Unresolved: actual scope-review verdict, concrete query linkage, evidence bundle creation, source-basis selection, concrete refs, and actual `QueryScopeReviewReport` instance remain absent unless future reviewed docs explicitly support resolution. |

## Package Rules

- All rows are docs/control-only authorized curation package rows.
- Concrete-ref status stays unresolved unless future reviewed docs explicitly
  support resolution.
- Allowed source-material surfaces are reviewed docs/control source-material
  surfaces only; they are not actual query instances, not runtime query refs,
  and not source/story/query content.
- Citation requirements must point to reviewed docs/control inputs, not
  placeholders, unreviewed samples, hidden author-only material, runtime
  database inspection, direct SQLite writes, provider/API output,
  renderer/ffmpeg/TTS output, media artifacts, or Qingli candidates that are
  not explicitly reviewed/promoted.
- Review gates must preserve `DRAFT_FOR_REVIEW` and
  `REVIEW_PASS_NOT_APPROVAL`.

## Prompt Surface And Visibility Guardrails

This package follows `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`.

Fields not explicitly listed in `allowedPromptSurface` must not enter prompts,
screen descriptions, negative prompts, prompt-safe asset notes,
voice/audio planning notes, or media planning notes. Negative prompts are also
prompt surface.

Future curation review work must distinguish review-only, author-only,
production-only, and future prompt-facing material. Review-only or author-only
material must not be copied, paraphrased, inverted, or hinted into
prompt-facing fields.

When future source-material notes must avoid leaking hidden causality, culprit
identity, sealed motive, director goal, future reveal, or unavailable actor
knowledge, the authorized curation package row must use `protectedBoundaryRef`
plus generic `boundaryCategory` instead of exposing protected content.

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

## Selected Package Outcome

Selected package outcome:

`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKAGE_READY_FOR_REVIEW`

Rationale: the reviewed route-decision outcome authorizes a future docs/control
curation package surface. This package preserves all three query-ref rows,
keeps concrete-ref status unresolved, and retains non-approval, visibility,
`allowedPromptSurface`, `protectedBoundaryRef`, `boundaryCategory`, citation,
and review-gate rules.

Future bounded docs/control review package candidate:
`query_ref_source_material_authorized_curation_package_review_v0_docs_control`.

That future candidate is future explicit classification only,
docs/control-only, classification/curation/review/status/package/
classification-only, not auto-started, and not approval.

## Downstream Allowance

On B03 PASS, B01/A01/00 may use this authorized curation package to classify
or dispatch a future explicit docs/control package. PASS must not auto-start
actual query instance creation, query execution, concrete runtime query-ref
resolution, concrete refs, source-basis selection, evidence bundle creation,
actual packet work, sample JSON, story content, runtime/CLI/schema/validator
work, 04/05/06/07/08 route, renderer/ffmpeg/TTS execution, provider/API work,
media generation, approval, canon, registry, release, final approval, or
production lock.

## Boundary Summary

- docs/control only
- authorized curation package surface only
- exactly three query-ref authorized curation package rows
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
