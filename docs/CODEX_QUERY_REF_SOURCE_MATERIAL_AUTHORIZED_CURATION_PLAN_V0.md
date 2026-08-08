# Query Ref Source Material Authorized Curation Plan V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`QueryRefSourceMaterialAuthorizedCurationPlan`

This document is docs/control only. It defines an authorized curation plan for
future reviewed query-ref source-material input surfaces after authority was
defined for docs/control curation. It does not create actual `QueryRequest`,
`QueryResponse`, or `QueryScopeReviewReport` instances, does not execute
queries, does not implement a queryer, does not resolve concrete runtime query
refs, does not invent source/story/query content, does not promote Qingli
candidates, does not select source basis for actual packet work, does not
create evidence bundles, does not create an actual `ScriptReadyScenePacket`,
does not create an actual `ScriptReadyScenePacketCandidate`, does not generate
sample JSON, does not start runtime/CLI/schema/validator work, does not call
provider/API, does not run renderer/ffmpeg/TTS, does not generate media, does
not grant approval, and does not create production lock.

## Upstream Authority

Reviewed authority outcome:
`QUERY_REF_SOURCE_MATERIAL_AUTHORITY_DEFINED_FOR_DOCS_CONTROL_CURATION`.

This plan follows:
`docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORITY_CLASSIFICATION_V0.md`.

The reviewed authority classification resolved
`A_GROUP_ROUTE_AUTHORITY_GAP_FOR_QUERY_REF_SOURCE_MATERIAL` as docs/control
curation authority only. It did not resolve concrete query-ref runtime inputs
and did not approve actual packet work.

B03 PASS for the authority classification is technical review evidence only.
This plan records that status as `REVIEW_PASS_NOT_APPROVAL`; it is not user
approval, asset approval, registry approval, canon, release, final media
approval, provider/media handoff, final approval, or production lock.

## Referenced Reviewed Inputs

This authorized curation plan cites these reviewed docs/control inputs:

- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORITY_CLASSIFICATION_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_ROUTE_DECISION_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PROTOCOL_V0.md`
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

Citation rule: every future curation item must cite an exact reviewed
docs/control source path, the route or review status it relies on, the
query-ref slot it serves, and the specific allowed source surface being used.
If a future item cannot cite reviewed docs/control evidence, it remains
unresolved and must not be promoted into concrete query-ref runtime input.

## Authorized Curation Plan Rows

Each future reviewed input surface appears exactly once. Each row is a
procedural curation plan, not a concrete query-ref input.

| Plan row | Future reviewed input surface | Allowed source surfaces | Required citation and review evidence | Visibility / prompt / protected-boundary requirements | Non-approval and unresolved status |
| --- | --- | --- | --- | --- | --- |
| `AUTHORIZED_CURATION_PLAN_ROW_QueryRequest` | `QueryRequest` | Query request source-material surface; reviewed docs/control citation surface; unresolved source-basis note surface; review gate surface. | Cite reviewed docs/control source path, reviewed authority outcome, intended future use, source-material scope, and any unresolved source-basis reason. | Declare source visibility classification; deny prompt use unless explicitly included in `allowedPromptSurface`; require `protectedBoundaryRef` plus generic `boundaryCategory` for hidden causality, culprit identity, sealed motive, director goal, future reveal, or unavailable actor knowledge. | Keep actual request content, runtime query parameters, concrete source basis, and actual `QueryRequest` instance unresolved; status remains `DRAFT_FOR_REVIEW` until technical review and `REVIEW_PASS_NOT_APPROVAL` after technical PASS only. |
| `AUTHORIZED_CURATION_PLAN_ROW_QueryResponse` | `QueryResponse` | Query response source-material surface; reviewed source-material citation surface; response sufficiency/rejection note surface; review gate surface. | Cite reviewed docs/control source path, reviewed authority outcome, response eligibility or insufficiency note, and unresolved evidence reason. | Declare source visibility classification; deny prompt use unless explicitly included in `allowedPromptSurface`; require `protectedBoundaryRef` plus generic `boundaryCategory` when response notes could expose hidden or unavailable information. | Keep actual query output, retrieved fact content, concrete response facts, provider/API output, and actual `QueryResponse` instance unresolved; status remains `DRAFT_FOR_REVIEW` until technical review and `REVIEW_PASS_NOT_APPROVAL` after technical PASS only. |
| `AUTHORIZED_CURATION_PLAN_ROW_QueryScopeReviewReport` | `QueryScopeReviewReport` | Query scope-review source-material surface; scope criteria surface; sufficiency checklist surface; rejection reason surface; review gate surface. | Cite reviewed docs/control source path, reviewed authority outcome, scope criteria, sufficiency/rejection rule, and unresolved concrete linkage reason. | Declare source visibility classification; deny prompt use unless explicitly included in `allowedPromptSurface`; require `protectedBoundaryRef` plus generic `boundaryCategory` for protected hidden, causal, reveal, or actor-knowledge boundaries. | Keep actual scope-review verdict, concrete query linkage, evidence bundle creation, source-basis selection, and actual `QueryScopeReviewReport` instance unresolved; status remains `DRAFT_FOR_REVIEW` until technical review and `REVIEW_PASS_NOT_APPROVAL` after technical PASS only. |

## Allowed Source Surfaces

Allowed source surfaces for this plan are limited to:

- reviewed docs/control source paths and route labels;
- reviewed query-ref source-material authority classification;
- reviewed source-material curation protocol, packet, review, review-status,
  and route-decision surfaces;
- current route card pointer text;
- `allowedPromptSurface` and forbidden prompt-surface rules from
  `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`;
- `protectedBoundaryRef` plus generic `boundaryCategory` guardrails;
- future explicit reviewed docs/control evidence produced by a bounded task.

These allowed source surfaces may support future classification or curation.
They must not become concrete runtime query-ref inputs by themselves.

## Forbidden Surfaces/Claims

The forbidden surfaces/claims below must be rejected or kept unresolved.

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
- placeholders not explicitly reviewed and promoted;
- samples that are not reviewed docs/control evidence;
- hidden author-only or review-only material copied, paraphrased, inverted, or
  hinted into prompt surface;
- 04/05/06/07/08 restart or Phase 20/21 restart;
- approval, canon, registry, release, final approval, or production lock.

## Review Gates

Future curation work under this plan must use explicit review gates:

- `DRAFT_FOR_REVIEW`: inspectable docs/control material only.
- `REVIEW_PASS_NOT_APPROVAL`: technical review pass only; no approval, canon,
  registry, release, final approval, or production lock.
- `BLOCKED_BY_HUMAN_DECISION`: use only when reviewed evidence proves a human
  decision or user/worldbuilding input is required.
- `BLOCKED_BY_BOUNDARY`: use when continuing would require actual query
  instance creation, query execution, concrete runtime query-ref resolution,
  source-basis selection, evidence bundle creation, actual packet generation,
  sample JSON, story content, runtime/schema, provider/API,
  renderer/ffmpeg/TTS, media generation, approval, canon, registry, release,
  final approval, or production lock.

## Prompt Surface And Visibility Guardrails

This plan follows `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`.

Fields not explicitly listed in `allowedPromptSurface` must not enter prompts,
screen descriptions, negative prompts, prompt-safe asset notes,
voice/audio planning notes, or media planning notes. Negative prompts are also
prompt surface.

Future curation must distinguish review-only, author-only, production-only,
and future prompt-facing material. Review-only or author-only material must not
be copied, paraphrased, inverted, or hinted into prompt-facing fields.

When future source-material notes must avoid leaking hidden causality, culprit
identity, sealed motive, director goal, future reveal, or unavailable actor
knowledge, the curation item must use `protectedBoundaryRef` plus generic
`boundaryCategory` instead of exposing protected content.

## Selected Plan Outcome

Allowed plan outcome labels are:

- `QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PLAN_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PLAN_BLOCKED_BY_MISSING_AUTHORITY`
- `QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PLAN_BLOCKED_BY_BOUNDARY`
- `QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PLAN_REQUIRES_TRUE_STOP_CONDITION`

Selected plan outcome:

`QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_CURATION_PLAN_READY_FOR_REVIEW`

Rationale: the reviewed authority outcome defines docs/control curation
authority, and this plan can safely define authorized curation rows, allowed
source surfaces, citation requirements, visibility classification,
`allowedPromptSurface`, `protectedBoundaryRef`, generic `boundaryCategory`,
review gates, and non-approval status without creating concrete query-ref
runtime inputs.

Because the plan is safe, exactly one future bounded docs/control curation
package candidate is named for future explicit classification only:
`query_ref_source_material_authorized_curation_packet_v0_docs_control`.

That future candidate is docs/control-only, classification/curation-only, not
auto-started, and not approval.

## Downstream Allowance

On B03 PASS, B01/A01/00 may use this authorized curation plan to classify or
dispatch a future explicit docs/control task. PASS must not auto-start actual
query instance creation, query execution, concrete runtime query-ref
resolution, source-basis selection, evidence bundle creation, actual packet
work, sample JSON, story content, runtime/CLI/schema/validator work,
04/05/06/07/08 route, renderer/ffmpeg/TTS execution, provider/API work, media
generation, approval, canon, registry, release, final approval, or production
lock.

## Boundary Summary

- docs/control only
- authorized curation plan only
- allowed source surfaces only
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
