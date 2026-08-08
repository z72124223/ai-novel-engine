# Query Ref Source Material Authority Classification V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`QueryRefSourceMaterialAuthorityClassification`

This document is docs/control only. It resolves
`A_GROUP_ROUTE_AUTHORITY_GAP_FOR_QUERY_REF_SOURCE_MATERIAL` for future
query-ref source-material curation authority. It does not create actual
`QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport` instances, does
not execute queries, does not implement a queryer, does not resolve concrete
runtime query refs, does not invent source/story/query content, does not
promote Qingli candidates, does not select source basis for actual packet
work, does not create evidence bundles, does not create an actual
`ScriptReadyScenePacket`, does not create an actual
`ScriptReadyScenePacketCandidate`, does not generate sample JSON, does not
start runtime/CLI/schema/validator work, does not call provider/API, does not
run renderer/ffmpeg/TTS, does not generate media, does not grant approval, and
does not create production lock.

## Authority Basis

A01 returned an `IMPLEMENTATION_PACKAGE` with `AI_TECHNICAL_APPROVED` and
`READY_FOR_B01_DISPATCH` for this exact bounded docs/control task. That A01
classification resolves the prior route authority gap for query-ref
source-material curation as a docs/control authority surface only.

Prior reviewed route-decision surface:
`docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_ROUTE_DECISION_V0.md`.

Prior reviewed aggregate route-decision outcome:
`QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_ROUTE_DECISION_REQUIRES_A_GROUP_CLASSIFICATION`.

The prior route-decision surface named no future package candidate because the
route needed A-group classification. This document supplies that classification
without upgrading unresolved source-material inputs into concrete query refs or
actual packet work.

## Referenced Reviewed Inputs

This authority classification cites these reviewed docs/control inputs:

- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_ROUTE_DECISION_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_STATUS_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_REVIEW_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PACKET_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_MATERIAL_CURATION_PROTOCOL_V0.md`
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

Citation rule: every future curation claim must cite an exact reviewed
docs/control source path, the route label or review gate it relies on, and the
slot it serves. A source-material claim that cannot cite reviewed docs/control
evidence remains unresolved and must not be promoted into a concrete query ref.

## Allowed Source-Material Surfaces

Allowed source-material surfaces are docs/control reference or procedural
surfaces for future reviewed inputs. They may define what a future task must
cite, review, hide, or reject. They are not actual query instances.

| Future reviewed input slot | Allowed source-material surface | Authority granted | Concrete material still forbidden |
| --- | --- | --- | --- |
| `QueryRequest` | Query request source-material surface | May carry reviewed source path placeholders, intended future use, unresolved source-basis notes, visibility classification, `allowedPromptSurface` eligibility, `protectedBoundaryRef`, generic `boundaryCategory`, and review gates. | Actual request content, runtime query parameters, concrete source basis, source/story/query invention, and actual `QueryRequest` instance. |
| `QueryResponse` | Query response source-material surface | May carry reviewed source-material citation notes, response eligibility or insufficiency notes, unresolved evidence notes, visibility classification, `allowedPromptSurface` eligibility, `protectedBoundaryRef`, generic `boundaryCategory`, and review gates. | Actual query output, retrieved fact content, concrete response facts, provider/API output, and actual `QueryResponse` instance. |
| `QueryScopeReviewReport` | Query scope-review source-material surface | May carry scope-review criteria, sufficiency checks, rejection reasons, unresolved status, visibility classification, `allowedPromptSurface` eligibility, `protectedBoundaryRef`, generic `boundaryCategory`, and review gates. | Actual scope-review verdict, concrete query linkage, evidence bundle creation, source-basis selection for actual packet work, and actual `QueryScopeReviewReport` instance. |

## Allowed Sources

Future curation may use only these source classes:

- existing reviewed docs/control source paths and route labels;
- the reviewed source-material curation protocol, curation packet, review,
  review-status, and route-decision docs;
- the current route card as a pointer surface;
- prompt-surface and protected-boundary rules from
  `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`;
- future explicit reviewed docs/control evidence produced by a bounded task.

Future curation must treat these as forbidden sources or forbidden claims:

- placeholders not explicitly reviewed and promoted;
- samples that are not reviewed docs/control evidence;
- inferred source/story/query content;
- hidden author-only or review-only material copied, paraphrased, inverted, or
  hinted into prompt surface;
- Qingli candidates not explicitly reviewed and promoted for this route;
- runtime database inspection or direct SQLite writes;
- provider/API output;
- renderer/ffmpeg/TTS output or media artifacts;
- actual query instance creation, query execution, or queryer implementation;
- concrete runtime query-ref resolution;
- source-basis selection for actual packet work;
- evidence bundle creation;
- actual `ScriptReadyScenePacket` or `ScriptReadyScenePacketCandidate`
  content;
- sample JSON;
- runtime/CLI/schema/validator work;
- approval, canon, registry, release, final approval, or production lock.

## Review Gates And Non-Approval Status

Allowed review gates and status terms for future curation include:

- `DRAFT_FOR_REVIEW`: the curation authority or future curation material is
  inspectable as docs/control only.
- `REVIEW_PASS_NOT_APPROVAL`: technical review may pass a docs/control surface
  without approving query instances, source basis, evidence bundles, canon,
  registry, release, final approval, or production lock.
- `BLOCKED_BY_HUMAN_DECISION`: use when reviewed evidence proves a human
  decision or user/worldbuilding input is required.
- `BLOCKED_BY_BOUNDARY`: use when continuing would cross actual query
  instance creation, query execution, concrete runtime query-ref resolution,
  source-basis selection, evidence bundle creation, actual packet generation,
  sample JSON, story content, runtime/schema, provider/API,
  renderer/ffmpeg/TTS, media generation, approval, canon, registry, release,
  final approval, or production lock.

`B03 PASS`, `03 PASS`, `QA_PASS_NOT_APPROVAL`, and
`REVIEW_PASS_NOT_APPROVAL` do not equal user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

## Visibility And Protected-Boundary Rules

This authority classification follows
`docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`.

Fields not explicitly listed in `allowedPromptSurface` must not enter prompts,
screen descriptions, negative prompts, prompt-safe asset notes,
voice/audio planning notes, or media planning notes. Negative prompts are also
prompt surface.

Future curation must distinguish review-only, author-only, production-only,
and future prompt-facing material. Review-only or author-only material must not
be copied, paraphrased, inverted, or hinted into prompt-facing fields.

When future source-material notes must avoid leaking hidden causality, culprit
identity, sealed motive, director goal, future reveal, or unavailable actor
knowledge, the curation surface must use `protectedBoundaryRef` plus generic
`boundaryCategory` instead of exposing protected content.

## Selected Authority Outcome

Selected authority outcome:

`QUERY_REF_SOURCE_MATERIAL_AUTHORITY_DEFINED_FOR_DOCS_CONTROL_CURATION`

Rationale: A01 has resolved the prior authority gap as a bounded docs/control
implementation package. Existing reviewed route evidence supports defining
authority, allowed source-material surfaces, allowed sources, citation rules,
review gates, visibility/protected-boundary rules, and non-approval status.
It does not support actual query instances, concrete runtime refs,
source/story/query invention, source-basis selection, evidence bundle creation,
actual packet work, approval, canon, release, or production lock.

Because authority is defined, exactly one future bounded docs/control curation
package candidate is named for future explicit classification only:
`query_ref_source_material_authorized_curation_plan_v0_docs_control`.

That future candidate is docs/control-only, classification/curation-only, not
auto-started, and not approval.

## Downstream Allowance

On B03 PASS, B01/A01/00 may use this authority classification to classify or
dispatch a future explicit docs/control task. PASS must not auto-start actual
query instance creation, query execution, concrete runtime query-ref
resolution, source-basis selection, evidence bundle creation, actual packet
work, sample JSON, story content, runtime/CLI/schema/validator work,
04/05/06/07/08 route, renderer/ffmpeg/TTS execution, provider/API work, media
generation, approval, canon, registry, release, final approval, or production
lock.

## Boundary Summary

- docs/control only
- authority classification only
- allowed source-material surfaces only
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
