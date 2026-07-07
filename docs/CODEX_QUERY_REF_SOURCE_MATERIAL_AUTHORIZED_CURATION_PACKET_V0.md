# Query Ref Source Material Authorized Curation Packet V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`QueryRefSourceMaterialAuthorizedCurationPacket`

This document is docs/control only. It creates a reviewable curation packet
surface for future query-ref source-material input rows after the authorized
curation plan was reviewed. It does not create actual `QueryRequest`,
`QueryResponse`, or `QueryScopeReviewReport` instances, does not execute
queries, does not implement a queryer, does not resolve concrete runtime query
refs, does not invent source/story/query content, does not promote Qingli
candidates, does not select source basis for actual packet work, does not
create evidence bundles, does not create an actual `ScriptReadyScenePacket`,
does not create an actual `ScriptReadyScenePacketCandidate`, does not generate
sample JSON, does not start runtime/CLI/schema/validator work, does not call
provider/API, does not run renderer/ffmpeg/TTS, does not generate media, does
not grant approval, and does not create production lock.

## Upstream Plan

Reviewed plan outcome:
`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PLAN_READY_FOR_REVIEW`.

This packet follows:
`docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PLAN_V0.md`.

The reviewed plan defines authorized curation rows and allows a future
docs/control curation packet surface. It does not resolve concrete query-ref
runtime inputs and does not approve actual packet work.

B03 PASS for the authorized curation plan is technical review evidence only.
This packet records that status as `REVIEW_PASS_NOT_APPROVAL`; it is not user
approval, asset approval, registry approval, canon, release, final media
approval, provider/media handoff, final approval, or production lock.

## Referenced Reviewed Inputs

This authorized curation packet cites these reviewed docs/control inputs:

- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PLAN_V0.md`
- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORITY_CLASSIFICATION_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_ROUTE_DECISION_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PROTOCOL_V0.md`
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

The citation requirements for this packet are strict: every curation packet
row must cite an exact reviewed
docs/control source path, the reviewed plan or authority outcome it relies on,
and the query-ref slot it serves. If concrete source material is not backed by
reviewed docs/control citations, the row must keep concrete input status
unresolved.

## Curation Packet Rows

Each future query-ref source-material row appears exactly once. Rows are
docs/control curation surfaces only, not concrete query-ref runtime inputs.

| Curation packet row | Future query-ref slot | Allowed source-material surface | Citation requirements | Visibility / prompt / protected-boundary fields | Review gate and non-approval status | Concrete input status |
| --- | --- | --- | --- | --- | --- | --- |
| `CURATION_PACKET_ROW_QueryRequest` | `QueryRequest` | Query request source-material surface; reviewed docs/control citation surface; unresolved source-basis note surface; review gate surface. | Cite `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PLAN_V0.md`, the reviewed plan outcome, intended future use, source-material scope, and unresolved source-basis reason. | Declare visibility classification; deny prompt use unless explicitly included in `allowedPromptSurface`; require `protectedBoundaryRef` plus generic `boundaryCategory` for hidden causality, culprit identity, sealed motive, director goal, future reveal, or unavailable actor knowledge. | `DRAFT_FOR_REVIEW` until technical review; `REVIEW_PASS_NOT_APPROVAL` after technical PASS only; use `BLOCKED_BY_HUMAN_DECISION` or `BLOCKED_BY_BOUNDARY` only when reviewed evidence supports it. | Unresolved: actual request content, runtime query parameters, concrete source basis, and actual `QueryRequest` instance are not supplied. |
| `CURATION_PACKET_ROW_QueryResponse` | `QueryResponse` | Query response source-material surface; reviewed source-material citation surface; response sufficiency/rejection note surface; review gate surface. | Cite `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PLAN_V0.md`, the reviewed plan outcome, response eligibility or insufficiency note, and unresolved evidence reason. | Declare visibility classification; deny prompt use unless explicitly included in `allowedPromptSurface`; require `protectedBoundaryRef` plus generic `boundaryCategory` when response notes could expose hidden or unavailable information. | `DRAFT_FOR_REVIEW` until technical review; `REVIEW_PASS_NOT_APPROVAL` after technical PASS only; use `BLOCKED_BY_HUMAN_DECISION` or `BLOCKED_BY_BOUNDARY` only when reviewed evidence supports it. | Unresolved: actual query output, retrieved fact content, concrete response facts, provider/API output, and actual `QueryResponse` instance are not supplied. |
| `CURATION_PACKET_ROW_QueryScopeReviewReport` | `QueryScopeReviewReport` | Query scope-review source-material surface; scope criteria surface; sufficiency checklist surface; rejection reason surface; review gate surface. | Cite `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PLAN_V0.md`, the reviewed plan outcome, scope criteria, sufficiency/rejection rule, and unresolved concrete linkage reason. | Declare visibility classification; deny prompt use unless explicitly included in `allowedPromptSurface`; require `protectedBoundaryRef` plus generic `boundaryCategory` for protected hidden, causal, reveal, or actor-knowledge boundaries. | `DRAFT_FOR_REVIEW` until technical review; `REVIEW_PASS_NOT_APPROVAL` after technical PASS only; use `BLOCKED_BY_HUMAN_DECISION` or `BLOCKED_BY_BOUNDARY` only when reviewed evidence supports it. | Unresolved: actual scope-review verdict, concrete query linkage, evidence bundle creation, source-basis selection, and actual `QueryScopeReviewReport` instance are not supplied. |

## Required Boundary Fields

Every future review or classification derived from this packet must preserve:

- reviewed docs/control citation path;
- query-ref slot name;
- allowed source-material surface;
- visibility classification;
- `allowedPromptSurface`;
- `protectedBoundaryRef`;
- generic `boundaryCategory`;
- review gate;
- `DRAFT_FOR_REVIEW`;
- `REVIEW_PASS_NOT_APPROVAL`;
- unresolved concrete input status;
- rejection or blocker status when applicable.

## Forbidden Claims

The forbidden claims below must be rejected or kept unresolved.

Forbidden claims that must be rejected or kept unresolved include:

- actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
  instances;
- query execution or queryer implementation;
- concrete runtime query-ref resolution;
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
- placeholders not explicitly reviewed and promoted;
- samples that are not reviewed docs/control evidence;
- hidden author-only or review-only material copied, paraphrased, inverted, or
  hinted into prompt surface;
- 04/05/06/07/08 restart or Phase 20/21 restart;
- approval, canon, registry, release, final approval, or production lock.

## Prompt Surface And Visibility Guardrails

This packet follows `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`.

Fields not explicitly listed in `allowedPromptSurface` must not enter prompts,
screen descriptions, negative prompts, prompt-safe asset notes,
voice/audio planning notes, or media planning notes. Negative prompts are also
prompt surface.

Future curation packet reviews must distinguish review-only, author-only,
production-only, and future prompt-facing material. Review-only or author-only
material must not be copied, paraphrased, inverted, or hinted into
prompt-facing fields.

When future source-material notes must avoid leaking hidden causality, culprit
identity, sealed motive, director goal, future reveal, or unavailable actor
knowledge, the curation packet row must use `protectedBoundaryRef` plus
generic `boundaryCategory` instead of exposing protected content.

## Selected Packet Outcome

Allowed packet outcome labels are:

- `QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKET_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKET_BLOCKED_BY_MISSING_REVIEWED_CITATIONS`
- `QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKET_BLOCKED_BY_BOUNDARY`
- `QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKET_REQUIRES_TRUE_STOP_CONDITION`

Selected packet outcome:

`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PACKET_READY_FOR_REVIEW`

Rationale: the reviewed plan outcome supports creating a docs/control curation
packet surface. This packet defines exactly one row each for `QueryRequest`,
`QueryResponse`, and `QueryScopeReviewReport`, includes required citation,
visibility, `allowedPromptSurface`, `protectedBoundaryRef`, generic
`boundaryCategory`, review gate, and non-approval fields, and keeps concrete
input status unresolved where reviewed docs/control citations do not supply
concrete input material.

Because the packet is safe, exactly one future bounded docs/control
review/classification package candidate is named for future explicit
classification only:
`query_ref_source_material_authorized_curation_packet_review_v0_docs_control`.

That future candidate is docs/control-only, classification/curation/review/
classification-only, not auto-started, and not approval.

## Downstream Allowance

On B03 PASS, B01/A01/00 may use this authorized curation packet to classify or
dispatch a future explicit docs/control review or classification task. PASS
must not auto-start actual query instance creation, query execution, concrete
runtime query-ref resolution, source-basis selection, evidence bundle
creation, actual packet work, sample JSON, story content,
runtime/CLI/schema/validator work, 04/05/06/07/08 route,
renderer/ffmpeg/TTS execution, provider/API work, media generation, approval,
canon, registry, release, final approval, or production lock.

## Boundary Summary

- docs/control only
- authorized curation packet surface only
- exactly three query-ref curation packet rows
- no actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
  instances
- no query execution
- no queryer implementation
- no concrete runtime query-ref resolution
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
