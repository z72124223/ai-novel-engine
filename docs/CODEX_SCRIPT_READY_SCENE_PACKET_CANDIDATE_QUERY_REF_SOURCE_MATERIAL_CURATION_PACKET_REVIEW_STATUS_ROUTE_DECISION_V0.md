# Script-Ready Scene Packet Candidate Query Ref Source Material Curation Packet Review Status Route Decision V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`ScriptReadyScenePacketCandidateQueryRefSourceMaterialCurationPacketReviewStatusRouteDecision`

This document is docs/control only. It records a route-decision surface for the
source-material curation packet review-status chain. It does not create actual
`QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport` instances, does
not execute queries, does not implement a queryer, does not resolve concrete
runtime refs, does not select source basis for actual packet work, does not
create evidence bundles, does not create an actual `ScriptReadyScenePacket`,
does not create an actual `ScriptReadyScenePacketCandidate`, does not generate
sample JSON, does not invent story/source/query content, does not promote
Qingli candidates, does not start runtime/CLI/schema/validator work, does not
call provider/API, does not run renderer/ffmpeg/TTS, does not generate media,
does not grant approval, and does not create production lock.

## Upstream Reviewed Status

This route-decision surface follows:
`docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_V0.md`.

Reviewed review-status outcome:
`QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_READY_FOR_REVIEW`.

Upstream review-surface outcome:
`QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_READY_FOR_REVIEW`.

Upstream packet outcome:
`QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_READY_FOR_REVIEW`.

Upstream protocol outcome:
`QUERY_REF_SOURCE_MATERIAL_CURATION_PROTOCOL_READY_FOR_REVIEW`.

Current blocker restated:
`BLOCKED_BY_UNRESOLVED_QUERY_REF_INPUTS_REQUIRING_NEW_SOURCE_MATERIAL_CLASSIFICATION`.

B03 PASS for the review-status surface is technical review evidence only. This
route-decision surface records that evidence as `REVIEW_PASS_NOT_APPROVAL`.
It is not user approval, asset approval, registry approval, canon, release,
final media approval, provider/media handoff, final approval, or production
lock.

## Referenced Reviewed Inputs

This route-decision surface cites these docs/control inputs:

- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_V0.md`
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

Citation rule: every route-decision row must cite exact docs/control source
paths and must preserve unresolved status when reviewed evidence does not
supply concrete query-ref input material.

## Aggregate Route-Decision Gates

This route-decision surface may classify whether the reviewed status chain can
continue by docs/control work or whether it needs a higher-level
classification before any next package is safe.

Allowed per-slot route-decision labels are:

- `ROUTE_DECISION_REQUIRES_A_GROUP_CLASSIFICATION`
- `ROUTE_DECISION_CAN_CONTINUE_DOCS_CONTROL_ONLY`
- `ROUTE_DECISION_REQUIRES_USER_WORLDBUILDING`
- `ROUTE_DECISION_BLOCKED_BY_BOUNDARY`

Gate meanings:

- `ROUTE_DECISION_REQUIRES_A_GROUP_CLASSIFICATION`: the route has technical
  reviewed status but unresolved concrete source-material inputs still require
  an explicit future A01/B01/00 classification before any next task is named.
- `ROUTE_DECISION_CAN_CONTINUE_DOCS_CONTROL_ONLY`: reviewed evidence supports
  a specific next docs/control-only task without resolving concrete inputs.
- `ROUTE_DECISION_REQUIRES_USER_WORLDBUILDING`: reviewed evidence proves the
  next step requires user/worldbuilding input.
- `ROUTE_DECISION_BLOCKED_BY_BOUNDARY`: continuing would cross actual query
  instance creation, query execution, concrete runtime ref resolution,
  source-basis selection, evidence bundle creation, actual packet generation,
  sample JSON, story content, runtime/schema, provider/API,
  renderer/ffmpeg/TTS, media generation, approval, canon, registry, release,
  final approval, or production lock.

## Route-Decision Rows

Each future query-ref source-material slot appears exactly once below. Each
row preserves unresolved concrete input status, prior curation row meaning,
prior review row meaning, prior status row meaning, `REVIEW_PASS_NOT_APPROVAL`,
and `REVIEW_STATUS_UNRESOLVED_REVIEW_PASS_NOT_APPROVAL`.

| Route-decision row | Future query-ref source-material slot | Prior curation row meaning | Prior review row meaning | Prior status row meaning | Preserved review status | Preserved review-status classification | Route-decision label | Rationale |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `ROUTE_DECISION_ROW_QueryRequest` | `QueryRequest` | `CURATION_ROW_QueryRequest` remains an unresolved query request source-material surface. | `REVIEW_ROW_QueryRequest` preserves unresolved request content, runtime query parameters, source basis, and actual query instance as forbidden. | `STATUS_ROW_QueryRequest` preserves unresolved request status and non-approval status. | `REVIEW_PASS_NOT_APPROVAL` | `REVIEW_STATUS_UNRESOLVED_REVIEW_PASS_NOT_APPROVAL` | `ROUTE_DECISION_REQUIRES_A_GROUP_CLASSIFICATION` | Reviewed status exists, but concrete request content, runtime query parameters, source basis, actual query instance, and concrete runtime refs remain unresolved. |
| `ROUTE_DECISION_ROW_QueryResponse` | `QueryResponse` | `CURATION_ROW_QueryResponse` remains an unresolved query response source-material surface. | `REVIEW_ROW_QueryResponse` preserves unresolved query output, retrieved fact content, response facts, and actual query instance as forbidden. | `STATUS_ROW_QueryResponse` preserves unresolved response status and non-approval status. | `REVIEW_PASS_NOT_APPROVAL` | `REVIEW_STATUS_UNRESOLVED_REVIEW_PASS_NOT_APPROVAL` | `ROUTE_DECISION_REQUIRES_A_GROUP_CLASSIFICATION` | Reviewed status exists, but actual query output, retrieved fact content, response facts, actual query instance, and concrete runtime refs remain unresolved. |
| `ROUTE_DECISION_ROW_QueryScopeReviewReport` | `QueryScopeReviewReport` | `CURATION_ROW_QueryScopeReviewReport` remains an unresolved query scope-review source-material surface. | `REVIEW_ROW_QueryScopeReviewReport` preserves unresolved actual scope-review verdict, concrete query linkage, evidence bundle, and actual report instance as forbidden. | `STATUS_ROW_QueryScopeReviewReport` preserves unresolved scope-review status and non-approval status. | `REVIEW_PASS_NOT_APPROVAL` | `REVIEW_STATUS_UNRESOLVED_REVIEW_PASS_NOT_APPROVAL` | `ROUTE_DECISION_REQUIRES_A_GROUP_CLASSIFICATION` | Reviewed status exists, but actual scope-review verdict, concrete query linkage, evidence bundle, actual report instance, and concrete runtime refs remain unresolved. |

No route-decision row resolves concrete query-ref inputs. No route-decision row
creates an actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
instance. No route-decision row upgrades unresolved curation, review, or status
material to user/worldbuilding required unless future reviewed evidence proves
that classification.

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

This route-decision surface follows
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
knowledge, the route-decision row must require `protectedBoundaryRef` plus
generic `boundaryCategory` instead of exposing protected content.

## Aggregate Route-Decision Outcome

Allowed aggregate route-decision outcome labels are:

- `QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_ROUTE_DECISION_REQUIRES_A_GROUP_CLASSIFICATION`
- `QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_ROUTE_DECISION_CAN_CONTINUE_DOCS_CONTROL_ONLY`
- `QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_ROUTE_DECISION_REQUIRES_USER_WORLDBUILDING`
- `QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_ROUTE_DECISION_BLOCKED_BY_BOUNDARY`

Selected aggregate route-decision outcome:

`QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_ROUTE_DECISION_REQUIRES_A_GROUP_CLASSIFICATION`

Rationale: the review-status chain is technically reviewed, but all three
query-ref source-material slots still preserve unresolved concrete inputs. The
current reviewed evidence supports a route-decision record, not an automatic
next package. A future continuation should therefore require A-group
classification or another explicit B01/A01/00 route decision before any new
docs/control package is named or dispatched.

No future bounded package candidate is named by this document. The selected
aggregate route-decision outcome requires A-group classification, so naming a
speculative next package would exceed this route-decision surface.

This is not a user/worldbuilding blocker and not a boundary block. It is a
classification requirement caused by unresolved concrete query-ref
source-material inputs and by the instruction not to invent a safe path.

## Technical PASS Is Not Approval

`B03 PASS`, `03 PASS`, `QA_PASS_NOT_APPROVAL`, and
`REVIEW_PASS_NOT_APPROVAL` do not equal user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

A technical PASS on this route-decision surface does not create query
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

On B03 PASS, B01/A01/00 may use this route-decision surface for status
awareness or to classify a future explicit docs/control task. PASS must not
auto-start actual query instance creation, query execution, concrete runtime
query-ref resolution, source-basis selection, evidence bundle creation, actual
packet work, sample JSON, story content, runtime/CLI/schema/validator work,
04/05/06/07/08 route, renderer/ffmpeg/TTS execution, provider/API work, media
generation, approval, canon, registry, release, final approval, or production
lock.

## Boundary Summary

- docs/control only
- source-material curation packet review-status route-decision only
- route-decision rows only
- no future bounded package candidate named by this document
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
