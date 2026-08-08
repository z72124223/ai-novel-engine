# Codex Chenxing 09 Scoped Query Surface V0

Task: `chenxing_09_scoped_query_surface_v0_docs_control`

Status: `DRAFT_FOR_REVIEW`

Control artifact: `Chenxing09ScopedQuerySurface`

Selected scoped-query-surface outcome:

`CHENXING_09_SCOPED_QUERY_SURFACE_READY_FOR_REVIEW`

This docs/control surface defines Chenxing-only scoped query surface rules for
future scene packet gap planning after the reviewed shape-readiness outcome
`CHENXING_09_SCENE_PACKET_SHAPE_READINESS_READY_FOR_REVIEW`. The shape-readiness
outcome is consumed as review-only shape evidence, not as source-basis
selection, not as query approval, not as packet approval, not as canon approval,
not as setting-pack apply, not as asset approval, not as registry approval, not
as release, not as final approval, and not as production lock.

This file creates no QueryRequest, no QueryResponse, no QueryScopeReviewReport,
no actual query instances, no query execution, no queryer implementation, no
retrieval runtime, no schema, no provider/API, no SQLite, no external lookup,
no concrete refs, no source-basis selection, no evidence bundle, no
source/story/query invention, no scene prose, no copied raw prose, no
prompt-ready text, no actual ScriptReadyScenePacket, no actual packet
instances, no ScriptReadyScenePacketCandidate, no storyboard, no prompt cards,
no asset cards, no voice/subtitle/SFX timeline, no renderer package, no
generated media, and no sample JSON.

## Product-Distance Rule

The product-distance rule is to reduce uncertainty about which future scoped
query categories can safely support future `ScriptReadyScenePacket` gap filling
for Chenxing.

Primary chain:

- scene packet.

Secondary readiness consumers:

- storyboard/camera;
- voice/subtitle/SFX;
- human approval gates.

This is not another status-only layer. It converts the reviewed shape-readiness
section contract into bounded query-category rules, stop conditions, leakage
guards, and review checks. The secondary readiness consumers are named only for
impact analysis and must not become implementation surfaces in this slice.

## Reviewed Shape-Readiness Input

The scoped query surface may only consume the reviewed Chenxing 09 shape
readiness as planning evidence. It follows these reviewed inputs:

- future `ScriptReadyScenePacket` section contract;
- 09 foundation card to scene-packet section mapping;
- Chenxing source boundary;
- prompt-surface firewall;
- Chenxing isolation;
- scoped-query dependency;
- downstream consumer matrix;
- approval separation;
- non-goals and review gates.

The reviewed shape evidence remains non-approval and unresolved. It does not
resolve source scene id, zone id, beat id, character refs, asset need refs,
source-basis selection, query content, query output, or packet content.

## Allowed Future Query Surface Categories

Allowed future query surface categories are shape-only categories. They may be
used by a future reviewed task to decide what kind of `QueryRequest` shape is
needed, but they do not create that `QueryRequest`, do not execute a query, and
do not resolve the answer.

| Future query surface category | Tied shape-readiness section | Allowed question surface | Required boundary |
| --- | --- | --- | --- |
| source scene gap | source scene basis | ask which reviewed Chenxing source authority could support a source scene id need | no concrete refs and no source-basis selection |
| location, zone, and beat gap | source scene basis; situation and beat pressure | ask what reviewed category of location, zone, or beat evidence is missing | no copied raw prose and no scene prose |
| character foundation gap | character foundation | ask which character foundation evidence type is required for review | no character-facing knowledge and no prompt-ready text |
| constraint and protected-boundary gap | constraints and visibility | ask which protected-boundary rule or boundaryCategory requires review | no author_only, review_only, draft-only, or hidden reasoning leakage |
| asset seed and asset need gap | asset seed surface | ask what asset seed category or asset need refs shape would be needed later | no prompt cards, no asset cards, and no registry claim |
| situation seed and pressure gap | situation and beat pressure | ask what situation seed category is missing for future packet planning | no story/source/query invention and no normalized scene text |
| citation granularity gap | source scene basis; constraints and visibility | ask which reviewed docs/control citation, manifest source id, or raw path granularity is required | raw archive prose may be cited by path only and must not be copied |
| downstream consumer impact gap | downstream handoff notes | ask which future consumer would need the gap closed | no storyboard, no voice/subtitle/SFX timeline, no renderer/package artifact |
| review gate gap | scoped query dependency; downstream handoff notes | ask which review gate must pass before a future query shape can be used | review gate only, not approval |

Each allowed category must keep visibility, allowedPromptSurface,
protectedBoundaryRef, boundaryCategory, citation requirement, unresolved-gap
status, and review gate explicit.

## Forbidden Query Categories And Stop Conditions

Forbidden query categories must stop the future route until a separate reviewed
planning/control package resolves the blocker.

- Query categories that request actual QueryRequest, QueryResponse, or
  QueryScopeReviewReport instances are forbidden.
- Query categories that require no query execution in this slice but attempt to
  schedule execution anyway are forbidden.
- Query categories that imply queryer implementation, retrieval runtime,
  provider/API, SQLite, schema, external lookup, or media work are forbidden.
- Query categories that select concrete refs, source basis, or evidence bundle
  entries are forbidden.
- Query categories that invent source/story/query content, scene prose, copied
  raw prose, or prompt-ready text are forbidden.
- Query categories that create actual query instances, actual packet instances,
  actual ScriptReadyScenePacket,
  ScriptReadyScenePacketCandidate, storyboard, prompt cards, asset cards,
  voice/subtitle/SFX timeline, renderer package, generated media, or sample JSON
  are forbidden.
- Query categories that import Rain Gate, Pilot, local renderer demo, old
  preview defaults, camera defaults, renderer defaults, unrelated old story
  assumptions, or unrelated source assumptions into Chenxing are forbidden.
- Query categories that promote B03 PASS, shape readiness, scoped query surface,
  or future review PASS into approval, canon, registry, release, final approval,
  or production lock are forbidden.

Stop conditions:

- missing reviewed Chenxing source authority;
- unclear visibility or boundaryCategory;
- missing allowedPromptSurface or protectedBoundaryRef;
- attempted prompt-surface leakage from protected notes;
- attempted concrete refs, source-basis selection, or evidence bundle creation;
- attempted non-Chenxing import;
- user/worldbuilding need that cannot be resolved from reviewed docs/control
  evidence;
- AI-resolvable planning gap that needs a future bounded docs/control package.

## Chenxing-Only Source Boundary

Chenxing isolation is mandatory. Future scoped query planning may use only
reviewed Chenxing docs/control surfaces, reviewed Chenxing 09 sample metadata,
manifest source ids, and raw paths as citations.

Boundary exclusions:

- no Rain Gate import;
- no Pilot import;
- no local renderer demo import;
- no old preview defaults;
- no camera defaults;
- no renderer defaults;
- no unrelated old story assumptions;
- no unrelated source assumptions;
- no test/demo source authority replacing Chenxing source authority.

Rain Gate, Pilot, local renderer demo, preview defaults, camera defaults,
renderer defaults, and old story material may only appear as exclusionary
test/demo or non-active-world boundary wording.

## Visibility And Prompt-Surface Leakage Firewall

The prompt-surface leakage firewall prevents future query surfaces from turning
planning evidence into creative output or hidden knowledge.

- `author_only` material must remain outside query prompts, packet prose,
  character-facing knowledge, scene prose, and user-facing story content.
- `review_only` material may support review checks only.
- draft-only labels must not become prompt-ready text.
- source-boundary notes may be cited as boundary evidence but must not become
  normalized scene text.
- protected-boundary notes must stay generic until a future reviewed package
  authorizes a concrete boundary decision.
- hidden reasoning must not be copied into any future prompt surface.
- raw archive prose may not be copied or normalized as scene prose; only raw
  paths or source ids may be cited as source authority.

Any future category that cannot maintain this firewall must block before query
shape creation.

## Downstream Consumer Impact

The scoped query surface describes future consumer impact only. It does not
create downstream artifacts.

| Downstream consumer | Allowed impact statement | Blocked output |
| --- | --- | --- |
| future scene packet | identify which scene packet section may need a future query shape | no actual ScriptReadyScenePacket and no packet prose |
| storyboard/camera | identify whether a future storyboard/camera question depends on a reviewed gap | no storyboard, no shot list, no camera defaults |
| voice/subtitle/SFX | identify whether a future voice/subtitle/SFX planning surface depends on a reviewed gap | no voice timeline, no subtitle file, no SFX timeline, no TTS |
| human approval gates | identify what future human review would need to see after reviewed evidence exists | no approval, no final approval, no release, no production lock |

Prompt/asset cards and renderer/package QA remain indirect consumers only for
this slice and receive no implementation surface.

## Review Checklist For Query Surface Safety

A future query-surface proposal must pass all checklist items before it can be
sent forward for review:

- The query surface category is one of the allowed categories in this document.
- The category is tied to a reviewed shape-readiness section.
- The category cites reviewed docs/control evidence, Chenxing source ids, or
  Chenxing raw paths only as authority references.
- The proposal states visibility, allowedPromptSurface, protectedBoundaryRef,
  boundaryCategory, citation requirement, unresolved-gap status, and review
  gate.
- The proposal states no QueryRequest, no QueryResponse, no
  QueryScopeReviewReport, no query execution, and no queryer implementation.
- The proposal states no concrete refs, no source-basis selection, and no
  evidence bundle.
- The proposal preserves Chenxing isolation and includes no Rain Gate, no
  Pilot, and no demo/default import except exclusionary wording.
- The proposal blocks if prompt-surface leakage, raw prose copy, source/story
  invention, scene prose, prompt-ready text, or downstream artifact creation is
  required.
- The proposal states product-distance toward the scene packet chain and does
  not become a status-only layer.
- The proposal states that review is not approval.

## Approval Separation

Approval separation is strict:

- B03 PASS is review evidence, not approval.
- Shape readiness is review-only section-contract evidence, not approval.
- The scoped query surface is planning/control evidence, not approval.
- Future review PASS is not canon approval, setting-pack apply, asset approval,
  registry approval, release, final approval, or production lock.
- No source-basis selection, query approval, packet approval, source authority
  approval, or runtime authorization is implied.
- No provider/API, no SQLite, no renderer/ffmpeg/TTS, no web/server, no
  retrieval runtime, and no media generation are authorized.

## Boundary Summary

This file is docs/control only. It does not mutate `world/chenxing/raw/*`, does
not create sample JSON, does not create actual query/packet/media/runtime/schema
work, does not create concrete refs, does not select source basis, does not
create evidence bundles, does not stage, commit, push, archive, create windows,
contact B03, or start a next task.
