# Script-Ready Scene Packet Candidate Query Ref Source Material Curation Packet Review Status V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`ScriptReadyScenePacketCandidateQueryRefSourceMaterialCurationPacketReviewStatus`

This document is docs/control only. It records a review-status surface for the
source-material curation packet review. It does not create actual
`QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport` instances, does
not execute queries, does not implement a queryer, does not resolve concrete
runtime refs, does not select source basis for actual packet work, does not
create evidence bundles, does not create an actual `ScriptReadyScenePacket`,
does not create an actual `ScriptReadyScenePacketCandidate`, does not generate
sample JSON, does not invent story/source/query content, does not promote
Qingli candidates, does not start runtime/CLI/schema/validator work, does not
call provider/API, does not run renderer/ffmpeg/TTS, does not generate media,
does not grant approval, and does not create production lock.

## Upstream Reviewed Review Surface

This review-status surface follows:
`docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_V0.md`.

Reviewed review-surface outcome:
`QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_READY_FOR_REVIEW`.

Upstream packet outcome:
`QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_READY_FOR_REVIEW`.

Upstream protocol outcome:
`QUERY_REF_SOURCE_MATERIAL_CURATION_PROTOCOL_READY_FOR_REVIEW`.

Current blocker restated:
`BLOCKED_BY_UNRESOLVED_QUERY_REF_INPUTS_REQUIRING_NEW_SOURCE_MATERIAL_CLASSIFICATION`.

B03 PASS for the curation packet review is technical review evidence only.
This review-status surface records that evidence as `REVIEW_PASS_NOT_APPROVAL`.
It is not user approval, asset approval, registry approval, canon, release,
final media approval, provider/media handoff, final approval, or production
lock.

## Referenced Reviewed Inputs

This review-status surface cites these docs/control inputs:

- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PROTOCOL_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_STATUS_ROUTE_DECISION_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PACKET_REVIEW_STATUS_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PACKET_REVIEW_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PACKET_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PLAN_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_MATRIX_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_PLACEHOLDER_PACKET_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_DEFINITION_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_GAP_CLOSURE_ROUTE_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_REVIEWED_REF_DISCOVERY_MATRIX_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_V0.md`
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

Citation rule: every status row must cite exact docs/control source paths and
must preserve unresolved status when reviewed evidence does not supply
concrete query-ref input material.

## Packet-Level Review-Status Gates

This status surface may record only the technical review status of the
curation packet review. Review-status gates are:

- `DRAFT_FOR_REVIEW`: status rows are inspectable as docs/control material
  only.
- `REVIEW_PASS_NOT_APPROVAL`: technical review can pass the status surface
  without approving query instances, source basis, evidence bundles, canon,
  registry, release, final approval, or production lock.
- `BLOCKED_BY_HUMAN_DECISION`: use when a future step needs human/user
  decision or user/worldbuilding input.
- `BLOCKED_BY_BOUNDARY`: use when a future step would cross actual query
  instance creation, query execution, concrete runtime ref resolution,
  source-basis selection, evidence bundle creation, actual packet generation,
  sample JSON, story content, runtime/schema, provider/API,
  renderer/ffmpeg/TTS, media generation, approval, canon, registry, release,
  final approval, or production lock.

## Status Rows

Each future query-ref source-material slot appears exactly once below. Each
row preserves unresolved concrete input status, prior curation row meaning,
prior review row meaning, and review status `REVIEW_PASS_NOT_APPROVAL`, while
recording review-status classification, citation/review evidence expectations,
visibility, `allowedPromptSurface`, `protectedBoundaryRef`, `boundaryCategory`,
rejection/blocked conditions, and non-approval status.

| Status row | Future query-ref source-material slot | Prior curation row meaning | Prior review row meaning | Review status | Review-status classification | Citation and review evidence expectations | Visibility / prompt / protected-boundary guardrails | Rejection / blocked conditions | Non-approval status |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `STATUS_ROW_QueryRequest` | `QueryRequest` | `CURATION_ROW_QueryRequest` remains an unresolved query request source-material surface. | `REVIEW_ROW_QueryRequest` preserves unresolved request content, runtime query parameters, source basis, and actual query instance as forbidden. | `REVIEW_PASS_NOT_APPROVAL` | `REVIEW_STATUS_UNRESOLVED_REVIEW_PASS_NOT_APPROVAL` | Cite reviewed docs/control paths, reviewed route labels, and unresolved-input status; preserve that no actual request content or runtime query parameters are supplied. | Preserve visibility classification; `allowedPromptSurface` must explicitly list any prompt-facing field; use `protectedBoundaryRef` and generic `boundaryCategory` for hidden or unavailable information. | Reject placeholders, samples not reviewed/promoted, hidden author-only/review-only material in prompt surface, runtime database inspection, direct SQLite writes, provider/API output, renderer/ffmpeg/TTS output, media artifacts, Qingli promotion, actual query instances, and concrete runtime refs. | Technical status only; not user approval, canon, registry, release, final approval, or production lock. |
| `STATUS_ROW_QueryResponse` | `QueryResponse` | `CURATION_ROW_QueryResponse` remains an unresolved query response source-material surface. | `REVIEW_ROW_QueryResponse` preserves unresolved query output, retrieved fact content, response facts, and actual query instance as forbidden. | `REVIEW_PASS_NOT_APPROVAL` | `REVIEW_STATUS_UNRESOLVED_REVIEW_PASS_NOT_APPROVAL` | Cite reviewed docs/control paths and preserve whether response material remains unresolved; do not provide actual query output or retrieved fact content. | Preserve visibility classification; `allowedPromptSurface` is deny-by-default; use `protectedBoundaryRef` and generic `boundaryCategory` when response notes could expose hidden causality or unavailable actor knowledge. | Reject placeholders, inferred story/source/query content, unreviewed source material, hidden review-only material copied into prompt surface, provider/API output, renderer/ffmpeg/TTS output, media artifacts, evidence bundle creation, and approval claims. | Technical status only; not user approval, canon, registry, release, final approval, or production lock. |
| `STATUS_ROW_QueryScopeReviewReport` | `QueryScopeReviewReport` | `CURATION_ROW_QueryScopeReviewReport` remains an unresolved query scope-review source-material surface. | `REVIEW_ROW_QueryScopeReviewReport` preserves unresolved actual scope-review verdict, concrete query linkage, evidence bundle, and actual report instance as forbidden. | `REVIEW_PASS_NOT_APPROVAL` | `REVIEW_STATUS_UNRESOLVED_REVIEW_PASS_NOT_APPROVAL` | Cite reviewed docs/control paths and required review gates; do not create actual scope-review verdicts, concrete query linkage, or evidence bundles. | Preserve visibility classification; `allowedPromptSurface` applies to any future prompt-facing summary; use `protectedBoundaryRef` and generic `boundaryCategory` for hidden causality, culprit identity, sealed motive, director goal, future reveal, or unavailable actor knowledge. | Reject placeholders treated as concrete evidence, actual query execution, queryer implementation, runtime database inspection, direct SQLite writes, provider/API output, renderer/ffmpeg/TTS output, media artifacts, source-basis selection, and production lock claims. | Technical status only; not user approval, canon, registry, release, final approval, or production lock. |

No status row resolves concrete query-ref inputs. No status row creates an
actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport` instance.
No status row upgrades unresolved curation or review material to
user/worldbuilding required unless future reviewed evidence proves that
classification.

## Forbidden Surfaces/Claims

Forbidden surfaces/claims that must be rejected or kept unresolved include:

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
- 04/05/06/07/08 restart or Phase 20/21 restart;
- approval, canon, registry, release, final approval, or production lock.

## Prompt Surface And Visibility Guardrails

This review-status surface follows
`docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`.

Fields not explicitly listed in `allowedPromptSurface` must not enter prompts,
screen descriptions, negative prompts, prompt-safe asset notes,
voice/audio planning notes, or media planning notes. Negative prompts are also
prompt surface.

Visibility rules must distinguish review-only, author-only, production-only,
and future prompt-facing material. Review-only or author-only material must not
be copied, paraphrased, inverted, or hinted into prompt-facing fields.

When source-material notes must avoid leaking hidden causality, culprit
identity, sealed motive, director goal, future reveal, or unavailable actor
knowledge, the status row must require `protectedBoundaryRef` plus generic
`boundaryCategory` instead of exposing protected content.

## Current Review-Status Outcome

Allowed current review-status outcome labels are:

- `QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_BLOCKED_BY_MISSING_REVIEWED_INPUTS`
- `QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_REQUIRES_USER_WORLDBUILDING`
- `QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_BLOCKED_BY_BOUNDARY`

Selected current review-status outcome:

`QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_READY_FOR_REVIEW`

Rationale: the review-status surface is reviewable because it records B03 PASS
as `REVIEW_PASS_NOT_APPROVAL`, includes exactly one unresolved status row for
each query-ref source-material slot, cites the reviewed curation packet review,
packet, and protocol, preserves concrete inputs as unresolved, and states
review-status gates, citation rules, visibility rules,
prompt-surface/protected-boundary guardrails, forbidden surfaces/claims, and
non-approval boundaries.

The selected outcome safely permits exactly one future bounded docs/control
candidate:
`script_ready_scene_packet_candidate_query_ref_source_material_curation_packet_review_status_route_decision_v0_docs_control`.

That future candidate remains docs/control-only, classification/curation/review/status/route-decision-only,
not auto-start, and not approval.

## Technical PASS Is Not Approval

`B03 PASS`, `03 PASS`, `QA_PASS_NOT_APPROVAL`, and
`REVIEW_PASS_NOT_APPROVAL` do not equal user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

A technical PASS on this review-status surface does not create query
instances, does not execute queries, does not implement a queryer, does not
resolve concrete refs, does not select source basis, does not create an
evidence bundle, does not create an actual `ScriptReadyScenePacket`, does not
create an actual `ScriptReadyScenePacketCandidate`, does not create sample
JSON, does not start story generation, does not start
runtime/CLI/schema/validator work, does not resume 04/05/06/07/08, does not
run renderer/ffmpeg/TTS, does not call provider/API, does not generate media,
does not grant approval, does not create canon, does not create registry
approval, does not release, and does not create production lock.

## Downstream Allowance

On B03 PASS, B01/A01/00 may use this review-status surface to classify or
dispatch a future explicit docs/control task. PASS must not auto-start actual
query instance creation, query execution, concrete runtime query-ref
resolution, source-basis selection, evidence bundle creation, actual packet
work, sample JSON, story content, runtime/CLI/schema/validator work,
04/05/06/07/08 route, renderer/ffmpeg/TTS execution, provider/API work, media
generation, approval, canon, registry, release, final approval, or production
lock.

## Boundary Summary

- docs/control only
- source-material curation packet review-status only
- status rows only
- no actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
  instances
- no query execution or queryer implementation
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
