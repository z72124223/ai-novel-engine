# Codex Chenxing 09 Scene Packet Source Requirement Matrix V0

Task: `chenxing_09_scene_packet_source_requirement_matrix_v0_docs_control`

Status: `DRAFT_FOR_REVIEW`

Control artifact: `Chenxing09ScenePacketSourceRequirementMatrix`

Selected source-requirement-matrix outcome:

`CHENXING_09_SCENE_PACKET_SOURCE_REQUIREMENT_MATRIX_READY_FOR_REVIEW`

This docs/control source requirement matrix is requirement-type-only planning
for Chenxing future `ScriptReadyScenePacket` sections. It consumes the
B03-PASSed skeleton outcome
`CHENXING_09_SCENE_PACKET_CANDIDATE_SKELETON_READY_FOR_REVIEW` as review-only
skeleton-contract evidence, not as packet approval, not as source-basis
selection, not as evidence approval, not as canon approval, not as setting-pack
apply, not as asset approval, not as registry approval, not as release, not as
final approval, and not as production lock.

This file creates no source-basis selection, no evidence bundle, no concrete
refs, no source/story/query text, no Chenxing content filling, no actual
ScriptReadyScenePacket, no ScriptReadyScenePacketCandidate, no actual packet
instances, no actual candidate instances, no QueryRequest, no QueryResponse,
no QueryScopeReviewReport, no actual query instances, no sample JSON, no query
execution, no queryer implementation, no retrieval runtime, no schema, no
provider/API, no SQLite, no external lookup, no storyboard/camera artifact, no
prompt cards, no asset cards, no voice/subtitle/SFX timeline, no renderer
package, no generated media, no runtime object, and no media object.

## Product-Distance Rule

The product-distance rule is to reduce uncertainty about what each future
Chenxing scene-packet skeleton section will require from reviewed source
material. This matrix is not another status/review/route-decision-only layer.
It is a requirement-type-only bridge from skeleton shape to future source
planning review.

Primary chain:

- scene packet.

Secondary readiness consumers:

- scoped query/source scope;
- future storyboard/camera readiness;
- human approval gates.

Secondary consumers receive impact notes only. They do not receive storyboard,
camera, prompt, asset, voice, subtitle, SFX, renderer, package, runtime, media,
or approval surfaces in this slice.

## Requirement Type Vocabulary

The matrix may use these requirement types only. They are labels for future
review needs, not selected sources and not evidence selections.

- reviewed docs/control citation need;
- Chenxing 09 card-family citation need;
- manifest source-id need;
- raw path citation need;
- source scene-id need;
- zone-id need;
- beat-id need;
- character-ref need;
- asset-need-ref need;
- protected-boundary review need;
- visibility review need;
- human approval gate evidence need.

Every requirement type remains unresolved until a future reviewed task
explicitly authorizes source-basis planning. No requirement type may become a
concrete ref, source-basis selection, evidence bundle, raw archive prose copy,
scene prose, or prompt-ready text.

## Visibility Tag Vocabulary

Allowed visibility tags are limited to review and boundary tags:

- `review_only`;
- `draft_only`;
- `source_boundary_note`;
- `author_only_guard`;
- `protected_boundary_note`;
- `allowedPromptSurface`;
- `protectedBoundaryRef`;
- `boundaryCategory`;
- `non_approval_status`;
- `unresolved_gap_status`;
- `human_review_needed`.

Forbidden visibility tags are any tag or wording that promotes a planning slot
into content, approval, or runtime authority:

- `prompt_ready`;
- `character_facing_knowledge`;
- `scene_prose`;
- `user_facing_story_content`;
- `source_basis_selected`;
- `evidence_selected`;
- `concrete_ref`;
- `canon_approved`;
- `asset_approved`;
- `registry_approved`;
- `release_ready`;
- `production_lock`.

Allowed visibility tags and forbidden visibility tags must be evaluated on
every matrix row.

## Missing-Info Markers And Future Review Questions

Missing-info markers are empty marker labels only. They must not contain
Chenxing content, copied raw prose, source/story/query text, scene prose,
prompt-ready text, source refs, evidence refs, or answers.

Marker shape:

- `MISSING_INFO_<SECTION>_<FIELD_FAMILY>_UNRESOLVED`

Future review questions are question-shape labels only. They identify what a
future reviewer would need to ask; they do not create QueryRequest,
QueryResponse, QueryScopeReviewReport, query text, query output, or verdicts.

Question shape:

- `REVIEW_QUESTION_<SECTION>_<REQUIREMENT_TYPE>_NEEDED`

Missing-info markers and future review questions remain empty markers and do
not fill Chenxing content. Every marker row also preserves no concrete refs.

## Source Requirement Matrix

The source requirement matrix maps B03-PASSed skeleton sections and field
families to requirement types, allowed visibility tags, forbidden visibility
tags, missing-info markers, future review questions, downstream consumer
impact, and stop conditions.

| Skeleton section / field family | Requirement types only | Allowed visibility tags | Forbidden visibility tags | Missing-info marker shape | Future review question shape | Downstream consumer impact | Stop conditions |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `candidate_identity` / candidate label, draft status, non-approval status | reviewed docs/control citation need; human approval gate evidence need | `review_only`, `draft_only`, `non_approval_status`, `unresolved_gap_status` | `canon_approved`, `registry_approved`, `release_ready`, `production_lock` | `MISSING_INFO_candidate_identity_status_UNRESOLVED` | `REVIEW_QUESTION_candidate_identity_review_status_NEEDED` | scene packet review traceability; human approval gate readiness | stop if status is treated as approval or packet content |
| `chenxing_source_boundary` / allowed and excluded source families | reviewed docs/control citation need; manifest source-id need; raw path citation need | `source_boundary_note`, `review_only`, `protected_boundary_note`, `unresolved_gap_status` | `source_basis_selected`, `evidence_selected`, `concrete_ref`, `prompt_ready` | `MISSING_INFO_chenxing_source_boundary_authority_UNRESOLVED` | `REVIEW_QUESTION_chenxing_source_boundary_source_authority_NEEDED` | scoped query/source scope can identify authority class only | stop if Rain Gate, Pilot, renderer demo, preview defaults, camera defaults, renderer defaults, or old story assumptions enter Chenxing |
| `scene_basis_need` / source scene, location, zone, beat | source scene-id need; zone-id need; beat-id need; reviewed docs/control citation need; raw path citation need | `review_only`, `draft_only`, `source_boundary_note`, `unresolved_gap_status` | `concrete_ref`, `source_basis_selected`, `scene_prose`, `user_facing_story_content` | `MISSING_INFO_scene_basis_need_scene_zone_beat_UNRESOLVED` | `REVIEW_QUESTION_scene_basis_need_source_scene_zone_beat_NEEDED` | future scene packet can see which basis classes are missing | stop if a row fills scene prose or concrete refs |
| `character_foundation_need` / character refs and relationship scope | character-ref need; Chenxing 09 card-family citation need; visibility review need | `review_only`, `draft_only`, `author_only_guard`, `unresolved_gap_status` | `character_facing_knowledge`, `prompt_ready`, `concrete_ref`, `canon_approved` | `MISSING_INFO_character_foundation_need_character_refs_UNRESOLVED` | `REVIEW_QUESTION_character_foundation_need_character_ref_visibility_NEEDED` | future scene packet and human review can assess character evidence class | stop if character knowledge or source/story content is invented |
| `constraint_visibility_need` / visibility tag, allowedPromptSurface, protectedBoundaryRef, boundaryCategory | protected-boundary review need; visibility review need; reviewed docs/control citation need | `review_only`, `author_only_guard`, `protected_boundary_note`, `allowedPromptSurface`, `protectedBoundaryRef`, `boundaryCategory` | `prompt_ready`, `scene_prose`, `user_facing_story_content`, `concrete_ref` | `MISSING_INFO_constraint_visibility_need_boundary_UNRESOLVED` | `REVIEW_QUESTION_constraint_visibility_need_boundary_visibility_NEEDED` | future prompt-surface checks and human approval gates can identify risk class | stop if author_only, review_only, hidden reasoning, or protected boundary notes leak |
| `asset_seed_need` / asset seed category and asset need refs shape | asset-need-ref need; Chenxing 09 card-family citation need; reviewed docs/control citation need | `review_only`, `draft_only`, `unresolved_gap_status`, `source_boundary_note` | `asset_approved`, `registry_approved`, `prompt_ready`, `concrete_ref` | `MISSING_INFO_asset_seed_need_asset_refs_UNRESOLVED` | `REVIEW_QUESTION_asset_seed_need_asset_requirement_NEEDED` | future prompt/asset card readiness can identify missing evidence class only | stop if asset card body, registry claim, or concrete asset ref appears |
| `situation_seed_need` / situation seed and beat pressure | beat-id need; Chenxing 09 card-family citation need; raw path citation need | `review_only`, `draft_only`, `source_boundary_note`, `unresolved_gap_status` | `scene_prose`, `prompt_ready`, `user_facing_story_content`, `source_basis_selected` | `MISSING_INFO_situation_seed_need_beat_pressure_UNRESOLVED` | `REVIEW_QUESTION_situation_seed_need_beat_pressure_NEEDED` | future scene packet can identify situation evidence class without prose | stop if source/story/query text or copied raw prose fills the gap |
| `scoped_query_dependency` / allowed query surface category and query-shape need | reviewed docs/control citation need; visibility review need; protected-boundary review need | `review_only`, `draft_only`, `unresolved_gap_status`, `allowedPromptSurface` | `prompt_ready`, `concrete_ref`, `source_basis_selected`, `evidence_selected` | `MISSING_INFO_scoped_query_dependency_query_shape_UNRESOLVED` | `REVIEW_QUESTION_scoped_query_dependency_query_surface_NEEDED` | scoped query/source scope can see which category is needed later | stop if QueryRequest, QueryResponse, QueryScopeReviewReport, query execution, or queryer implementation appears |
| `downstream_consumer_notes` / consumer family and blocked output | human approval gate evidence need; reviewed docs/control citation need | `review_only`, `draft_only`, `human_review_needed`, `non_approval_status` | `release_ready`, `production_lock`, `asset_approved`, `prompt_ready` | `MISSING_INFO_downstream_consumer_notes_impact_UNRESOLVED` | `REVIEW_QUESTION_downstream_consumer_notes_consumer_impact_NEEDED` | storyboard/camera and human approval gates can see impact without artifacts | stop if storyboard, prompt/asset card body, voice/subtitle/SFX timeline, renderer/package artifact, runtime object, or media object appears |
| `review_and_stop_conditions` / checklist, blocker, approval firewall | reviewed docs/control citation need; human approval gate evidence need; visibility review need | `review_only`, `non_approval_status`, `human_review_needed`, `unresolved_gap_status` | `canon_approved`, `registry_approved`, `release_ready`, `production_lock` | `MISSING_INFO_review_and_stop_conditions_blocker_UNRESOLVED` | `REVIEW_QUESTION_review_and_stop_conditions_blocker_NEEDED` | reviewers can decide whether future work is safe or blocked | stop if B03 PASS, skeleton readiness, source requirement matrix, or future review PASS is approval-inflated |

## Scoped-Query Dependency Preservation

The matrix preserves scoped-query dependency from the B03-PASSed scoped-query
surface. It may reference scoped-query categories as requirement context only.
It creates no QueryRequest, no QueryResponse, no QueryScopeReviewReport, no
query execution, no queryer implementation, no retrieval runtime, no provider/API,
no SQLite, no external lookup, no query text, no query output, and no verdict.

## Chenxing Isolation

Chenxing isolation is mandatory:

- no Rain Gate import;
- no Pilot import;
- no local renderer demo import;
- no old preview defaults;
- no camera defaults;
- no renderer defaults;
- no unrelated old story assumptions;
- no unrelated source assumptions.

Rain Gate, Pilot, local renderer demo, preview defaults, camera defaults,
renderer defaults, and old story material may only appear as exclusionary
test/demo or non-active-world boundary wording.

## Prompt-Surface Firewall

The prompt-surface leakage firewall preserves visibility and protected-boundary
separation:

- `author_only` material must remain outside prompt-ready fields;
- `review_only` notes must not become scene prose;
- draft-only labels must not become character-facing knowledge;
- source-boundary notes must not become user-facing story content;
- protected-boundary notes must not become concrete refs;
- hidden reasoning must not enter a packet, prompt, query, or user-facing field;
- raw archive prose must not be copied, summarized into scene prose, or
  normalized as content.

Any row that cannot preserve this firewall must stop before future source or
query planning.

## Review Checklist

The review checklist for requirement-type-only safety and downstream usefulness
must pass before future work may use this matrix:

- The row maps an allowed skeleton section and field family.
- The row uses only allowed requirement types from this matrix.
- The row states allowed visibility tags and forbidden visibility tags.
- The row keeps missing-info markers empty.
- The row keeps future review questions as question-shape labels only.
- The row states downstream consumer impact without creating downstream
  artifacts.
- The row preserves scoped-query dependency without query instances or query
  execution.
- The row preserves Chenxing isolation with no Rain Gate, no Pilot, no local
  renderer demo, no old preview defaults, no camera defaults, no renderer
  defaults, and no old story assumptions.
- The row preserves the prompt-surface leakage firewall.
- The row states no source-basis selection, no evidence bundle, no concrete
  refs, no actual ScriptReadyScenePacket, no ScriptReadyScenePacketCandidate,
  no QueryRequest, no QueryResponse, no QueryScopeReviewReport, no sample JSON,
  no query execution, no queryer implementation, no provider/API, no SQLite,
  and no production lock.
- The row reduces scene-packet uncertainty and does not create another
  status/review/route-decision-only layer.

## Stop Conditions

The future route must stop before using this matrix if any stop condition
appears:

- a row selects a source, evidence, source basis, or concrete ref;
- a row fills source/story/query text or Chenxing content;
- a row creates actual packet/candidate instances or query instances;
- a row creates QueryRequest, QueryResponse, or QueryScopeReviewReport content;
- a row requires query execution, queryer implementation, retrieval runtime,
  schema, provider/API, SQLite, external lookup, or runtime work;
- a row creates sample JSON, fenced JSON examples, storyboard/camera artifact,
  prompt cards, asset cards, voice/subtitle/SFX timeline, renderer package,
  generated media, runtime object, or media object;
- a row mutates `world/chenxing/raw/*`;
- a row imports Rain Gate, Pilot, renderer demo, preview defaults, camera
  defaults, renderer defaults, or old story assumptions into Chenxing;
- a row leaks author_only, review_only, hidden reasoning, protected-boundary
  notes, source-boundary notes, or raw archive prose into prompt-ready fields;
- a row treats B03 PASS, skeleton readiness, source requirement matrix, or
  future review PASS as canon approval, setting-pack apply, asset approval,
  registry approval, release, final approval, or production lock.

## Approval Separation

Approval separation is strict:

- B03 PASS is review evidence, not approval.
- Skeleton readiness is review-only skeleton-contract evidence, not packet
  approval.
- The source requirement matrix is requirement-type-only planning, not evidence
  approval.
- Future review PASS is not canon approval, setting-pack apply, asset approval,
  registry approval, release, final approval, or production lock.
- No provider/API, no SQLite, no renderer/ffmpeg/TTS, no web/server, no
  retrieval runtime, no schema work, no direct SQLite writes, and no media
  generation are authorized.

## Boundary Summary

This file is docs/control only. It does not mutate `world/chenxing/raw/*`, does
not create sample JSON, does not create actual packet/query/media/runtime/schema
work, does not create concrete refs, does not select source basis, does not
create evidence bundles, does not stage, commit, push, archive, create windows,
contact B03, or start a next task.
