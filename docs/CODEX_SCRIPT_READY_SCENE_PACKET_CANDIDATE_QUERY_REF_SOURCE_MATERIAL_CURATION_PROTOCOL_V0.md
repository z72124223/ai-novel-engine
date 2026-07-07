# Script-Ready Scene Packet Candidate Query Ref Source Material Curation Protocol V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`ScriptReadyScenePacketCandidateQueryRefSourceMaterialCurationProtocol`

This document is docs/control only. It defines protocol rules for future
reviewed query-ref input source-material curation. It does not create actual
`QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport` instances, does
not execute queries, does not implement a queryer, does not resolve concrete
runtime refs, does not select source basis for actual packet work, does not
create evidence bundles, does not create an actual `ScriptReadyScenePacket`,
does not create an actual `ScriptReadyScenePacketCandidate`, does not generate
sample JSON, does not invent story/source/query content, does not promote
Qingli candidates, does not start runtime/CLI/schema/validator work, does not
call provider/API, does not run renderer/ffmpeg/TTS, does not generate media,
does not grant approval, and does not create production lock.

## Current Blocker

The current route blocker is:

`BLOCKED_BY_UNRESOLVED_QUERY_REF_INPUTS_REQUIRING_NEW_SOURCE_MATERIAL_CLASSIFICATION`

This protocol follows the reviewed status route-decision surface:
`docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_STATUS_ROUTE_DECISION_V0.md`.

That upstream route-decision selected:
`QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_STATUS_ROUTE_DECISION_REQUIRES_A_GROUP_CLASSIFICATION`.

A01 has now classified this protocol task as `AI_TECHNICAL_APPROVED` and
`READY_FOR_B01_DISPATCH`. That classification authorizes only this
docs/control protocol surface. It is not approval, not canon, not registry,
not release, not final approval, and not production lock.

## Referenced Reviewed Inputs

This protocol cites these reviewed docs/control inputs:

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

Citation rule: future curation claims must cite exact docs/control paths and
the specific reviewed status they rely on. A curation note that cannot cite a
reviewed docs/control source remains unresolved.

## Upstream Context Preserved

Concrete reviewed query-ref inputs remain unresolved until a later explicitly
dispatched and reviewed task resolves or reclassifies them. This protocol
preserves the following route context:

- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_STATUS_ROUTE_DECISION_REQUIRES_A_GROUP_CLASSIFICATION`
- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PACKET_REVIEW_STATUS_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PACKET_REVIEW_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PACKET_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PLAN_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_MATRIX_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_PLACEHOLDER_PACKET_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_DEFINITION_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_GAP_CLOSURE_ROUTE_DEFINED_FOR_FUTURE_SOURCE_DOCS`
- `QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_BLOCKED_BY_MISSING_REVIEWED_REFS`
- `NO_REVIEWED_SOURCE_FOUND_AI_PLANNING_GAP`
- `REVIEWED_INPUTS_MISSING_AI_PLANNING_GAP`
- `CLOSURE_PLAN_DEFINED_FOR_AI_RESOLVABLE_GAP`
- `REVIEW_PASS_NOT_APPROVAL`

## Allowed Source-Material Surfaces

Allowed source-material surfaces are reference-only docs/control surfaces that
can later support reviewed query-ref input curation without becoming actual
query instances.

| Future query-ref slot | Allowed source-material surface | What the surface may carry | What remains unresolved |
| --- | --- | --- | --- |
| `QueryRequest` | Query request source-material surface | Intended future query purpose, source-material scope, required citation placeholder, visibility classification, `allowedPromptSurface` eligibility placeholder, `protectedBoundaryRef`, generic `boundaryCategory`, and review gate. | Concrete request content, selected source basis, runtime query parameters, and actual `QueryRequest` instance. |
| `QueryResponse` | Query response source-material surface | Candidate reviewed source-material references, response eligibility notes, unresolved evidence notes, visibility classification, `allowedPromptSurface` eligibility placeholder, `protectedBoundaryRef`, generic `boundaryCategory`, and review gate. | Concrete response content, retrieved facts, runtime query output, and actual `QueryResponse` instance. |
| `QueryScopeReviewReport` | Query scope-review source-material surface | Scope-review criteria, source-material sufficiency checks, rejection reasons, visibility classification, `allowedPromptSurface` eligibility placeholder, `protectedBoundaryRef`, generic `boundaryCategory`, and review gate. | Actual scope-review verdict, concrete query linkage, reviewed evidence bundle, and actual `QueryScopeReviewReport` instance. |

These surfaces may identify what a future task must cite or review. They must
not provide concrete story/source/query content merely to make a future packet
look complete.

## AI-Curated Versus User-Required Material

AI-curated material may include only:

- reviewed docs/control route labels, blocker labels, and prior outcomes;
- reviewed source paths already present in the project;
- reviewed visibility and prompt-surface rules;
- reviewed protected-boundary categories;
- procedural source-material curation requirements;
- unresolved-input classifications carried forward from reviewed docs.

AI-curated material must remain reference-only until a later explicit task
reviews the concrete source material. It may not create or infer new scene
facts, zone facts, beat facts, character facts, asset facts, query content, or
story causality.

User/worldbuilding input is required when future curation would need new
world facts, new story decisions, new character/location/asset facts, a human
choice between competing source meanings, or any material that reviewed
project evidence cannot safely derive. That situation should use
`BLOCKED_BY_HUMAN_DECISION` or a user/worldbuilding blocker instead of AI
curation.

Boundary blocking applies when future curation would require actual query
instance creation, query execution, concrete runtime ref resolution,
source-basis selection for actual packet work, evidence bundle creation,
sample JSON, story/source invention, runtime/schema/validator work,
provider/API output, direct SQLite writes, renderer/ffmpeg/TTS output, media
artifacts, approval, canon, registry, release, final approval, or production
lock.

## Review Gates And Citation Requirements

Every future source-material curation item must include:

- exact reviewed docs/control source path;
- reviewed status or route gate such as `DRAFT_FOR_REVIEW` or
  `REVIEW_PASS_NOT_APPROVAL`;
- slot name: `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`;
- intended future use, without creating the query instance;
- source visibility classification;
- prompt-surface eligibility through `allowedPromptSurface`, if any;
- forbidden prompt-surface notes for material that must remain outside prompts;
- `protectedBoundaryRef` when hidden or unavailable information must be
  protected;
- generic `boundaryCategory`, not protected fact text;
- unresolved reason if the curation item lacks a reviewed citation;
- non-approval status.

Forbidden surfaces/claims that must be rejected or kept unresolved include:

- placeholders not explicitly reviewed and promoted;
- samples that are not reviewed docs/control evidence;
- inferred story/source/query content;
- hidden author-only or review-only material copied into prompt surface;
- Qingli candidates not explicitly reviewed and promoted for this route;
- runtime database inspection;
- direct SQLite writes;
- provider/API output;
- renderer/ffmpeg/TTS output;
- media artifacts;
- any claim of approval, canon, registry, release, final approval, or
  production lock.

## Prompt Surface And Protected Boundary Rules

This protocol follows
`docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`.

Fields not explicitly listed in `allowedPromptSurface` must not enter prompts,
screen descriptions, negative prompts, prompt-safe asset notes,
voice/audio planning notes, or media planning notes. Negative prompts are also
prompt surface.

When source-material notes must avoid leaking hidden causality, culprit
identity, sealed motive, director goal, future reveal, or unavailable actor
knowledge, the future curation item must use `protectedBoundaryRef` plus a
generic `boundaryCategory`. It must not copy, paraphrase, invert, or hint at
the protected content in prompt-facing fields.

## Current Protocol Outcome

Allowed current protocol outcome labels are:

- `QUERY_REF_SOURCE_MATERIAL_CURATION_PROTOCOL_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_MATERIAL_CURATION_PROTOCOL_BLOCKED_BY_MISSING_RULES`
- `QUERY_REF_SOURCE_MATERIAL_CURATION_PROTOCOL_BLOCKED_BY_BOUNDARY`
- `QUERY_REF_SOURCE_MATERIAL_CURATION_PROTOCOL_REQUIRES_USER_WORLDBUILDING`

Selected current protocol outcome:

`QUERY_REF_SOURCE_MATERIAL_CURATION_PROTOCOL_READY_FOR_REVIEW`

Rationale: the protocol is reviewable because it defines allowed
source-material surfaces, citation rules, visibility rules, prompt-surface and
protected-boundary rules, review gates, rejection rules, and non-approval
status without curating concrete source material or creating actual query
instances.

The selected outcome safely permits exactly one future bounded docs/control
candidate:
`script_ready_scene_packet_candidate_query_ref_source_material_curation_packet_v0_docs_control`.

That future candidate remains classification/curation-only. It must not
auto-start, must not be treated as approval, and must not create actual
`QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport` instances.

## Technical PASS Is Not Approval

`B03 PASS`, `03 PASS`, `QA_PASS_NOT_APPROVAL`, and
`REVIEW_PASS_NOT_APPROVAL` do not equal user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

A technical PASS on this protocol does not create query instances, does not
execute queries, does not implement a queryer, does not resolve concrete refs,
does not select source basis, does not create an evidence bundle, does not
create an actual `ScriptReadyScenePacket`, does not create an actual
`ScriptReadyScenePacketCandidate`, does not create sample JSON, does not start
story generation, does not start runtime/CLI/schema/validator work, does not
resume 04/05/06/07/08, does not run renderer/ffmpeg/TTS, does not call
provider/API, does not generate media, does not grant approval, does not
create canon, does not create registry approval, does not release, and does
not create production lock.

## Downstream Allowance

On B03 PASS, B01/A01/00 may use this protocol to classify or dispatch a future
explicit docs/control task. PASS must not auto-start actual query instance
creation, query execution, concrete runtime query-ref resolution,
source-basis selection, evidence bundle creation, actual packet work, sample
JSON, story content, runtime/CLI/schema/validator work, 04/05/06/07/08 route,
renderer/ffmpeg/TTS execution, provider/API work, media generation, approval,
canon, registry, release, final approval, or production lock.

## Boundary Summary

- docs/control only
- source-material curation protocol only
- allowed source-material surfaces only
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
