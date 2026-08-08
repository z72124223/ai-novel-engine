# Codex Chenxing 09 Scene Packet Shape Readiness V0

Task: `chenxing_09_scene_packet_shape_readiness_v0_docs_control`

Status: `DRAFT_FOR_REVIEW`

Control artifact: `Chenxing09ScenePacketShapeReadiness`

Selected shape-readiness outcome:

`CHENXING_09_SCENE_PACKET_SHAPE_READINESS_READY_FOR_REVIEW`

This docs/control surface defines Chenxing scene packet shape readiness from
the reviewed alignment outcome
`CHENXING_09_TO_SCENE_PACKET_ALIGNMENT_READY_FOR_REVIEW`. The alignment outcome
is consumed as review-only evidence, not as source-basis selection, not as
packet approval, not as canon approval, not as setting-pack apply, not as asset
approval, not as registry approval, not as release, not as final approval, and
not as production lock.

This file creates no actual ScriptReadyScenePacket, no
ScriptReadyScenePacketCandidate, no QueryRequest, no QueryResponse, no
QueryScopeReviewReport, no concrete refs, no evidence bundle, no scene prose,
no story/source/query invention, no storyboard, no prompt cards, no asset
cards, no voice/subtitle/SFX timeline, no renderer package, no sample JSON, no
provider/API, no SQLite, and no media generation.

## Product-Distance Target

The product-distance target is limited to reducing uncertainty for the scene
packet chain. Secondary chains are named only as downstream consumers:

- storyboard/camera bridge;
- prompt/asset card boundaries;
- voice/subtitle/SFX planning surface;
- renderer/package QA readiness;
- human approval gates.

Those secondary chains must not become implementation surfaces in this slice.

## Section Contract

The future `ScriptReadyScenePacket` section contract is shape-only. Each
section describes expected planning fields, source boundaries, visibility
rules, unresolved-gap markers, downstream consumers, and review gates.

| Future section | Required fields | Source boundaries | Visibility rules | Unresolved-gap markers | Downstream consumers | Review gates |
| --- | --- | --- | --- | --- | --- | --- |
| packet identity | candidate id shape, active world label, draft status, non-approval status | cite reviewed Chenxing alignment and 09 sample only | review-only; not prompt-ready | missing reviewed package identity | scene packet, human approval gates | B01 dispatch plus B03 review only |
| source scene basis | source scene need, location need, zone need, beat need | cite reviewed docs/control paths, sample card ids, manifest source ids, raw paths | source-boundary notes remain planning-only | source scene id unresolved, zone id unresolved, beat id unresolved | scene packet, scoped query/source scope | source-basis review required before any packet |
| character foundation | character foundation need, relationship scope, character-boundary notes | cite reviewed 09 character foundation cards and source authorities | no character-facing knowledge unless future reviewed evidence allows it | character refs unresolved | scene packet, prompt/asset card boundaries, human approval gates | character visibility review required |
| constraints and visibility | protected boundary, allowed prompt surface, boundary category, leakage flags | cite alignment visibility/leakage guard and 09 constraint card | author_only, review_only, draft-only, hidden reasoning, and source-boundary notes stay out of prompts | unclear boundary category, missing protected-boundary ref | scene packet, prompt/asset cards, voice/subtitle/SFX planning | boundary review required |
| asset seed surface | asset seed need, asset need refs shape, asset exclusion notes | cite reviewed 09 asset seed card only as candidate seed | no asset card body, no asset registry claim | asset need refs unresolved | prompt/asset card boundaries, storyboard/camera bridge | asset boundary review required |
| situation and beat pressure | situation seed need, beat pressure shape, scene question shape | cite reviewed 09 situation seed card and source authority | no scene prose, no user-facing story content | beat id unresolved, scene question unresolved | scene packet, storyboard/camera bridge, voice/subtitle/SFX planning | situation review required |
| scoped query dependency | future QueryRequest shape, future QueryResponse shape, future QueryScopeReviewReport shape | cite alignment scoped-query dependency only | no query prompt, no query response, no verdict text | all query shapes unresolved | scoped query/source scope, scene packet | query-shape review required |
| downstream handoff notes | consumer list, blocked surfaces, approval firewall | cite alignment downstream consumer matrix and review-only firewall | planning-control wording only | consumer boundary unresolved | all secondary consumers | convergence review required |

No section may become an actual packet section until a future explicit
source-basis/evidence planning task and review gate authorizes that separate
work.

## Mapping From Reviewed Alignment Inputs

This mapping uses only reviewed alignment inputs and keeps them as planning
shape evidence.

- 09 foundation card to scene-packet section mapping: `world_premise`,
  `location_foundation`, `character_foundation`, `system_foundation`,
  `constraint`, `asset_seed`, and `situation_seed` inform future section needs
  only.
- Chenxing source boundary: cite reviewed docs/control paths, sample card ids,
  manifest source ids, and raw paths, but do not copy raw archive prose as
  normalized scene text.
- Visibility/leakage guard: preserve review-only, author_only, draft-only,
  protected-boundary, and source-boundary separation.
- Downstream consumer matrix: secondary chains are consumers only, not
  implementation surfaces.
- Scoped-query dependency: list future query shapes only, not actual
  QueryRequest, QueryResponse, or QueryScopeReviewReport instances.
- Approval firewall: B03 PASS, alignment PASS, this shape readiness, and
  future review PASS are not approval.
- Non-goals and review gates: carry non-goals forward as blocking gates, not as
  permissions.

## Prompt-Surface Firewall

The prompt-surface firewall prevents future shape fields from leaking protected
planning material into creative output.

- `author_only` material must not become prompt-ready fields.
- `review_only` material must not become scene prose.
- draft-only wording must not become character-facing knowledge.
- source-boundary notes must not become user-facing story content.
- protected-boundary refs must stay generic until future reviewed evidence
  selection.
- hidden reasoning must not appear in prompts, packet prose, or user-facing
  story text.
- raw paths may be cited as authority but must not be copied into normalized
  scene text.

Any future task that needs prompt-ready language must pass a separate review
gate and must still avoid source-basis assignment unless that future task is
explicitly authorized.

## Chenxing Isolation

Chenxing isolation remains mandatory:

- no Rain Gate import;
- no Pilot import;
- no local renderer demo import;
- no old preview defaults;
- no camera defaults;
- no renderer defaults;
- no unrelated old story content;
- no unrelated source assumptions;
- no asset assumptions imported from old demo material.

Rain Gate, Pilot, local renderer demo, preview defaults, camera defaults,
renderer defaults, and old story material may only appear as exclusionary
test/demo or non-active-world boundaries.

## Scoped-Query Dependency

This shape readiness may identify future scoped-query input shapes only:

- future `QueryRequest` shape for missing source scene, location, zone, beat,
  character, asset seed, and boundary questions;
- future `QueryResponse` shape for reviewed answer surfaces;
- future `QueryScopeReviewReport` shape for visibility, citation, and boundary
  checks.

It creates no QueryRequest, no QueryResponse, no QueryScopeReviewReport, no
query execution, no queryer implementation, no concrete refs, no runtime
query-ref resolution, no source-basis selection, and no evidence bundle.

## Approval Separation

Approval separation is strict:

- B03 PASS is review evidence, not approval.
- Alignment PASS is review evidence, not packet approval.
- Shape readiness is section-contract planning, not approval.
- Future review PASS is not canon approval.
- No setting-pack apply is implied.
- No asset approval, registry approval, release, final approval, or production
  lock is implied.
- No provider/API, no SQLite, no renderer/ffmpeg/TTS, no web/server, and no
  media generation are authorized.

## Review Gates And Blockers

Future scene-packet work must stop if any of these blockers appear:

- a required section lacks reviewed source authority;
- source scene id, zone id, beat id, character refs, or asset need refs are
  treated as concrete refs;
- review-only or author_only material is promoted into prompt-ready text;
- raw archive prose is copied into scene prose;
- Rain Gate, Pilot, renderer demo, preview defaults, camera defaults, renderer
  defaults, unrelated old story content, or unrelated source assumptions are
  imported into Chenxing;
- B03 PASS, alignment PASS, shape readiness, or future review PASS is phrased
  as approval, canon, registry, release, final approval, or production lock;
- a future task attempts actual ScriptReadyScenePacket creation before a
  separately reviewed source-basis/evidence planning gate.

## Boundary Summary

This file is docs/control only. It does not mutate `world/chenxing/raw/*`, does
not create sample JSON, does not create actual packet/query/media/runtime/schema
work, does not create concrete refs, does not select source basis, does not
create evidence bundles, does not stage, commit, push, archive, create windows,
contact B03, or start a next task.
