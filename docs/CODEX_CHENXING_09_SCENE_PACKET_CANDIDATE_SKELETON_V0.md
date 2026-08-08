# Codex Chenxing 09 Scene Packet Candidate Skeleton V0

Task: `chenxing_09_scene_packet_candidate_skeleton_v0_docs_control`

Status: `DRAFT_FOR_REVIEW`

Control artifact: `Chenxing09ScenePacketCandidateSkeleton`

Selected skeleton outcome:

`CHENXING_09_SCENE_PACKET_CANDIDATE_SKELETON_READY_FOR_REVIEW`

This docs/control surface defines a future `ScriptReadyScenePacket` candidate
skeleton contract for Chenxing only. It consumes the B03-PASSed scoped-query
outcome `CHENXING_09_SCOPED_QUERY_SURFACE_READY_FOR_REVIEW` as review-only
scoped-query evidence, not as query approval, not as source-basis selection,
not as packet approval, not as canon approval, not as setting-pack apply, not
as registry approval, not as release, not as final approval, and not as
production lock.

This file creates no actual ScriptReadyScenePacket, no
ScriptReadyScenePacketCandidate, no actual packet instances, no actual
candidate instances, no sample JSON, no QueryRequest, no QueryResponse, no
QueryScopeReviewReport, no actual query instances, no query execution, no
queryer implementation, no retrieval runtime, no provider/API, no schema, no
SQLite, no external lookup, no concrete refs, no source-basis selection, no
evidence bundle, no source/story/query content, no copied raw prose, no scene
prose, no prompt-ready text, no storyboard/camera artifact, no prompt cards, no
asset cards, no voice/subtitle/SFX timeline, no renderer package, no generated
media, no runtime object, and no media object. For verification clarity, this
also means no ScriptReadyScenePacketCandidate and no queryer implementation in
any later skeleton row produced from this surface.

## Product-Distance Rule

The product-distance rule is to reduce uncertainty about future
`ScriptReadyScenePacket` candidate shape for the Chenxing scene packet chain.
This is not another status/review/route-decision layer. It converts the
reviewed scoped query surface into a product-chain skeleton contract that a
future reviewed task can use before any actual packet work is considered.

Primary chain:

- scene packet.

Secondary readiness consumers:

- scoped query/source scope;
- storyboard/camera readiness;
- human approval gates.

Secondary chains are consumer-readiness notes only. They do not authorize
storyboard, camera, prompt, asset, voice, subtitle, SFX, renderer, package, or
media artifacts.

## Candidate Skeleton Section Names

The future candidate skeleton uses section names only. These section names are
not packet content, not scene prose, and not source-basis selection.

| Section name | Purpose | Required/optional marker | Product-chain reason |
| --- | --- | --- | --- |
| `candidate_identity` | identify draft candidate shape and non-approval status | required | lets reviewers distinguish shape planning from actual packet content |
| `chenxing_source_boundary` | state Chenxing-only authority boundary | required | prevents Rain Gate, Pilot, demo, preview, camera, renderer, or old story contamination |
| `scene_basis_need` | state future source scene, location, zone, and beat needs | required | reduces scene packet uncertainty without concrete refs |
| `character_foundation_need` | state future character foundation and relationship need families | required | preserves character visibility and source-boundary review |
| `constraint_visibility_need` | state protected boundary, allowedPromptSurface, and boundaryCategory needs | required | prevents prompt-surface leakage |
| `asset_seed_need` | state future asset seed and asset need refs shape only | optional until future source evidence exists | keeps prompt/asset cards out of this slice |
| `situation_seed_need` | state future situation and beat pressure shape only | required | supports future packet planning without scene prose |
| `scoped_query_dependency` | map needed gaps to reviewed scoped-query surface categories | required | keeps query planning shape-only |
| `downstream_consumer_notes` | state consumer impact for scoped query/source scope, storyboard/camera, and human approval gates | optional for non-blocking consumers | avoids downstream artifact creation |
| `review_and_stop_conditions` | state review checklist and blockers | required | prevents approval inflation and same-family status-layer repetition |

## Field Families And Required/Optional Markers

Each future skeleton section must use field families and required/optional
markers. Field families are planning slots only, not values, not concrete refs,
and not content.

| Section | Field families | Marker rule |
| --- | --- | --- |
| `candidate_identity` | candidate label shape, draft status, review-only status, non-approval status, product-distance target | all required |
| `chenxing_source_boundary` | allowed source family, excluded source families, citation family, raw-path citation rule, no-import guard | all required |
| `scene_basis_need` | source scene need, location need, zone need, beat need, citation requirement, unresolved-gap status | all required |
| `character_foundation_need` | character refs shape, relationship scope, character-facing visibility guard, unresolved-gap status | all required |
| `constraint_visibility_need` | visibility tag, allowedPromptSurface, protectedBoundaryRef, boundaryCategory, leakage guard | all required |
| `asset_seed_need` | asset seed category, asset need refs shape, asset exclusion note, future review gate | optional unless future packet planning names an asset dependency |
| `situation_seed_need` | situation seed category, beat pressure shape, source-boundary note, unresolved-gap status | all required |
| `scoped_query_dependency` | allowed query surface category, blocked query category, query-shape need, review gate | all required |
| `downstream_consumer_notes` | consumer family, impact note, blocked output, review dependency | optional unless the consumer blocks scene packet review |
| `review_and_stop_conditions` | checklist result shape, blocker category, approval firewall, status-layer convergence guard | all required |

Required means the future skeleton candidate must include the family as a
planning slot. Optional means the family may be omitted only if the future
review explains why the omitted family is not needed for scene packet
uncertainty reduction.

## Future Source Requirement Types

The future source requirement types below are labels for later review needs.

Future source requirement types describe what evidence class may be needed
later. They do not select sources, create concrete refs, or create an evidence
bundle.

- reviewed docs/control citation requirement;
- reviewed Chenxing 09 card-family citation requirement;
- Chenxing manifest source id citation requirement;
- Chenxing raw path citation requirement;
- source scene id requirement type;
- zone id requirement type;
- beat id requirement type;
- character refs requirement type;
- asset need refs requirement type;
- protected-boundary review requirement type;
- visibility review requirement type;
- human approval gate evidence requirement type.

Every source requirement type must remain unresolved until a future reviewed
source-basis/evidence task explicitly authorizes source selection. No source
requirement type may copy raw archive prose, invent source content, or become
prompt-ready text.

## Visibility Tag Requirements

Future skeleton fields must carry visibility tag requirements before any packet
candidate can be reviewed.

Required visibility tag families:

- `review_only`;
- `draft_only`;
- `author_only_guard`;
- `source_boundary_note`;
- `protected_boundary_note`;
- `allowedPromptSurface`;
- `protectedBoundaryRef`;
- `boundaryCategory`;
- `non_approval_status`;
- `unresolved_gap_status`.

The prompt-surface leakage firewall is mandatory:

- author_only material must not become prompt-ready text;
- review_only notes must not become scene prose;
- draft-only labels must not become character-facing knowledge;
- source-boundary notes must not become user-facing story content;
- protected-boundary notes must not become concrete refs;
- hidden reasoning must not enter any packet, prompt, query, or user-facing
  field;
- raw archive prose must not be copied or normalized.

## Query-Surface Dependency

This query-surface dependency section preserves reviewed category links only.

The skeleton depends on the B03-PASSed scoped-query surface categories only as
planning dependencies. The dependency is not query approval and does not create
query artifacts.

Allowed dependency mappings:

| Skeleton section | Scoped-query dependency |
| --- | --- |
| `scene_basis_need` | source scene gap; location, zone, and beat gap |
| `character_foundation_need` | character foundation gap |
| `constraint_visibility_need` | constraint and protected-boundary gap; citation granularity gap |
| `asset_seed_need` | asset seed and asset need gap |
| `situation_seed_need` | situation seed and pressure gap |
| `downstream_consumer_notes` | downstream consumer impact gap |
| `review_and_stop_conditions` | review gate gap |

No dependency creates a QueryRequest, QueryResponse, QueryScopeReviewReport,
query execution, queryer implementation, retrieval runtime, concrete refs,
source-basis selection, evidence bundle, source/story/query content, or packet
content.

## Review Checklist

The review checklist for skeleton safety and downstream usefulness must pass
before any future skeleton candidate can move to the next docs/control review.

- The skeleton names the final-product chain as scene packet.
- Every section name is one of the allowed skeleton section names.
- Every section has required/optional markers.
- Every required section includes field families, future source requirement
  types, visibility tag requirements, unresolved-gap status, and review gate.
- Query-surface dependency points only to reviewed Chenxing scoped-query surface
  categories.
- Chenxing isolation is preserved with no Rain Gate, no Pilot, no renderer demo,
  no preview defaults, no camera defaults, no renderer defaults, and no old
  story assumptions imported into Chenxing.
- The skeleton states no actual ScriptReadyScenePacket, no
  ScriptReadyScenePacketCandidate, no sample JSON, no QueryRequest, no
  QueryResponse, no QueryScopeReviewReport, no query execution, no queryer
  implementation, no evidence bundle, no source-basis selection, and no concrete
  refs.
- The skeleton contains no copied raw prose, no scene prose, no prompt/asset
  card body, no storyboard, no voice/subtitle/SFX timeline, no renderer/package
  artifact, no runtime object, and no media object.
- The skeleton states that B03 PASS, docs PASS, scoped-query surface, skeleton
  readiness, and future review PASS are not approval.
- The skeleton reduces scene packet uncertainty and does not repeat a
  same-family status-layer loop.

## Stop Conditions

The stop conditions below are blockers, not permissions.

The future route must stop before skeleton use if any stop condition appears:

- source/story invention is needed to fill a section;
- actual packet or actual candidate instances are requested;
- actual QueryRequest, QueryResponse, or QueryScopeReviewReport instances are
  requested;
- query execution, queryer implementation, retrieval runtime, provider/API,
  schema, SQLite, or external lookup work is requested;
- concrete refs, source-basis selection, source-basis assignment, or evidence
  bundle creation is requested;
- prompt-surface leakage would expose author_only, review_only, hidden
  reasoning, protected boundary notes, or source-boundary notes;
- Rain Gate, Pilot, renderer demo, preview defaults, camera defaults, renderer
  defaults, or old story assumptions are imported into Chenxing;
- B03 PASS, docs PASS, scoped-query surface, skeleton readiness, or future
  review PASS is treated as approval, canon, registry, release, final approval,
  or production lock;
- a future proposal adds another same-family status/review/route-decision layer
  instead of a product-chain skeleton or explicit blocker.

## Approval Separation

Approval separation is strict:

- B03 PASS is review evidence, not approval.
- The scoped-query surface is review-only planning evidence, not query approval.
- The skeleton is a section-contract planning surface, not packet approval.
- Future review PASS is not canon approval, setting-pack apply, asset approval,
  registry approval, release, final approval, or production lock.
- No provider/API, no SQLite, no renderer/ffmpeg/TTS, no web/server, no
  retrieval runtime, no schema work, and no media generation are authorized.

## Boundary Summary

This file is docs/control only. It does not mutate `world/chenxing/raw/*`, does
not create sample JSON, does not create actual packet/query/media/runtime/schema
work, does not create concrete refs, does not select source basis, does not
create evidence bundles, does not stage, commit, push, archive, create windows,
contact B03, or start a next task.
