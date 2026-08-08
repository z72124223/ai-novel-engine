# Query Ref Source Material Authorized Curation Review Status Route Decision V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`QueryRefSourceMaterialAuthorizedCurationReviewStatusRouteDecision`

This document is docs/control only. It records a route-decision surface for the
authorized curation review-status chain and decides whether the reviewed status
can authorize exactly one future docs/control curation package candidate. It
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

## Upstream Reviewed Status

Reviewed status label:
`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_REVIEW_STATUS_READY_FOR_REVIEW`.

This route-decision surface follows:
`docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_REVIEW_STATUS_V0.md`.

The reviewed status surface records exactly one review-status row each for
`QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport`. Each row
preserves `PACKET_ROW_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_CURATION` and
`REVIEW_PASS_NOT_APPROVAL`, and each keeps concrete input status unresolved.

B03 PASS for the reviewed status surface is technical review evidence only.
`REVIEW_PASS_NOT_APPROVAL` is not user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

## Referenced Reviewed Inputs

This route decision cites these reviewed docs/control inputs:

- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_REVIEW_STATUS_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKET_REVIEW_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKET_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PLAN_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORITY_CLASSIFICATION_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_ROUTE_DECISION_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PROTOCOL_V0.md`
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

Citation rule: every route-decision row must cite an exact reviewed
docs/control source path, the reviewed status label it relies on, and the
query-ref slot it preserves. If concrete source material is not backed by
reviewed docs/control citations, the row must keep concrete input status
unresolved.

## Route-Decision Rows

Each query-ref source-material route-decision row appears exactly once below.
The route decision preserves prior status and decides whether the row can
support one future docs/control curation package without resolving concrete
refs.

| Route-decision row | Query-ref slot | Prior reviewed status | Preserved rules | Concrete input status | Route-decision status |
| --- | --- | --- | --- | --- | --- |
| `ROUTE_DECISION_ROW_QueryRequest` | `QueryRequest` | `PACKET_ROW_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_CURATION`; `REVIEW_PASS_NOT_APPROVAL` | Preserves citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gate, and `DRAFT_FOR_REVIEW`. | Unresolved: actual request content, runtime query parameters, concrete source basis, concrete refs, and actual `QueryRequest` instance remain absent. | `ROUTE_DECISION_SUPPORTS_FUTURE_CURATION_PACKAGE_NOT_APPROVAL` |
| `ROUTE_DECISION_ROW_QueryResponse` | `QueryResponse` | `PACKET_ROW_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_CURATION`; `REVIEW_PASS_NOT_APPROVAL` | Preserves citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gate, and `DRAFT_FOR_REVIEW`. | Unresolved: actual query output, retrieved fact content, provider/API output, concrete refs, and actual `QueryResponse` instance remain absent. | `ROUTE_DECISION_SUPPORTS_FUTURE_CURATION_PACKAGE_NOT_APPROVAL` |
| `ROUTE_DECISION_ROW_QueryScopeReviewReport` | `QueryScopeReviewReport` | `PACKET_ROW_REVIEW_PASS_NOT_APPROVAL_FOR_FUTURE_CURATION`; `REVIEW_PASS_NOT_APPROVAL` | Preserves citation, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`, review gate, and `DRAFT_FOR_REVIEW`. | Unresolved: actual scope-review verdict, concrete query linkage, evidence bundle creation, source-basis selection, concrete refs, and actual `QueryScopeReviewReport` instance remain absent. | `ROUTE_DECISION_SUPPORTS_FUTURE_CURATION_PACKAGE_NOT_APPROVAL` |

## Route-Decision Summary

The reviewed status authorizes exactly one future docs/control curation package
candidate because all three rows preserve technical review status, non-approval
language, citation requirements, visibility requirements, `allowedPromptSurface`
rules, `protectedBoundaryRef` plus generic `boundaryCategory` rules, and review
gate rules while keeping concrete refs unresolved.

This authorization is route-decision/classification-only. It does not create
actual query instances, does not resolve concrete refs, does not approve source
basis, does not create an evidence bundle, does not approve actual packet work,
and does not create canon, release, final approval, or production lock.

## Prompt Surface And Visibility Guardrails

This route-decision surface follows
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
knowledge, the route-decision-derived curation item must use
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

## Selected Route-Decision Outcome

Selected route-decision outcome:

`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_REVIEW_STATUS_ROUTE_DECISION_CAN_NAME_FUTURE_CURATION_PACKAGE`

Rationale: the reviewed status outcome
`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_REVIEW_STATUS_READY_FOR_REVIEW`
preserves all three query-ref rows, keeps concrete inputs unresolved, and
retains non-approval, visibility, `allowedPromptSurface`,
`protectedBoundaryRef`, `boundaryCategory`, citation, and review-gate rules.
Therefore, it can name one future docs/control curation package candidate
without auto-starting it or implying approval.

Future bounded docs/control curation package candidate:
`query_ref_source_material_authorized_curation_package_v0_docs_control`.

That future candidate is future explicit classification only,
docs/control-only, classification/curation/review/status/route-decision/
classification-only, not auto-started, and not approval.

## Downstream Allowance

On B03 PASS, B01/A01/00 may use this route-decision surface to classify or
dispatch a future explicit docs/control package. PASS must not auto-start
actual query instance creation, query execution, concrete runtime query-ref
resolution, concrete refs, source-basis selection, evidence bundle creation,
actual packet work, sample JSON, story content, runtime/CLI/schema/validator
work, 04/05/06/07/08 route, renderer/ffmpeg/TTS execution, provider/API work,
media generation, approval, canon, registry, release, final approval, or
production lock.

## Boundary Summary

- docs/control only
- authorized curation review-status route decision only
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
