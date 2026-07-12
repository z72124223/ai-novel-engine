# Demand-Driven Query Layer Blueprint

Status: `DOCS_ONLY_FUTURE_PRODUCT_BLUEPRINT`

Date: 2026-06-22

Local implementation research addendum (2026-07-13):
`docs/LOCAL_HYBRID_RETRIEVAL_KNOWLEDGE_BLUEPRINT_V1.md` refines a possible
local-first implementation route using deterministic parsing, optional local
extraction, SQL/FTS/BM25, Embedding recall, Reranker ordering, visibility
filtering, and bounded evidence handoff. This document remains the product
contract; the addendum is a docs-only future candidate and does not authorize
model download, vector storage, schema changes, runtime work, API calls, MCP,
or implementation.

This document defines a future product-layer query blueprint for the new-world / storyboard production line. It does not implement Kotlin source, tests, schema, migrations, Validator behavior, StateReducer behavior, `resolve-turn`, `stateVersion`, `contextHash`, CLI commands, SQLite tables, OpenAI API calls, GPT browser automation, Seedance integration, Oiioii integration, or runtime enforcement.

## 1. VERDICT

The project should plan three demand-driven queryers before restarting 04 / 05 / 06 / 07 / 08:

- World Card Queryer: retrieves only the world data needed for the current packet or review task.
- Asset Queryer: retrieves only the approved or candidate asset data needed for the current storyboard packet or frame.
- Story Card Queryer, better understood as Situation / Continuity / Pressure Queryer: retrieves only the situation, continuity, causal pressure, actor objective, available evidence, local misconception, and known constraints needed for the current packet or review task.

These queryers are future implementation paths, not current capabilities. They are product-layer contracts for worldbuilding, script packet, asset, and storyboard workflows. They do not replace the existing runtime ContextPacket, `relevantSettings`, `retrievedMemories`, or `knowledgeClaims` surfaces.

## 2. Why This Layer Exists

New-world production will eventually contain more data than 04, 05, 06, 07, or 08 should read at once. Full-world prompts create predictable risks:

- 04 may overfit to author-only world causality or future arcs.
- 04 may invent scenes, assets, materials, or institutions because the needed card was not supplied.
- 05 cannot tell whether a draft used too much information or missed a required boundary.
- 08 may redesign characters, scenes, props, costumes, or weapons instead of using stable references.
- Author-only causal notes may leak into player-facing text, storyboard notes, or image prompts.

The query layer gives future windows a smaller, auditable retrieval surface. Each request should say why data is needed, what visibility level is allowed, and what must remain outside the current actor / user-facing view.

## 3. Pipeline Position

Future storyboard-first flow:

```text
09 world foundation / world data organization
        |
        v
World Card Queryer + Situation / Continuity / Pressure Queryer
        |
        v
04 draft script-ready scene packet
        |
        v
05 packet review and query-scope audit
        |
        v
06 display / intake with explicit user approval
        |
        v
07 asset candidate packet
        |
        v
Asset Queryer
        |
        v
08 GPT storyboard pickup / verification
        |
        v
user-reviewable storyboard images / packets
```

Important boundaries:

- 09 data organization does not make a world canon by itself.
- 04 should not read the full world bible. It should request scoped World Cards and Situation / Continuity / Pressure Cards for the current packet.
- 07 may prepare asset candidate packets only after explicit handoff and approval rules are satisfied.
- 08 must cite or carry the asset ids, scene ids, zone ids, variants, and storyboard frame data returned by the query layer. 08 must not freely redesign characters, scenes, props, clothes, weapons, materials, or author-only causal facts.
- 05 must review whether each query was necessary, too broad, too narrow, or secrecy-unsafe.

## 4. World Card Queryer

### 4.1 Responsibility

World Card Queryer retrieves scoped world data, such as:

- core premise fragments;
- era and material culture constraints;
- geography and scene-region context;
- social structure and institutions;
- law, custom, taboo, cost, and survival pressure;
- allowed public setting details;
- review-only constraints needed to prevent drift.

It should answer "what world facts are needed now?" rather than "what is the whole world?"

### 4.2 Inputs

Potential future request fields:

```yaml
requestId: ""
requestingWindow: 04 | 05 | 06 | 07 | 08 | 01
packetId: ""
sceneId: ""
zoneId: ""
playerObjective: ""
informationNeed: []
visibilityTarget: author_only | review_only | production_only | user_facing
maxCards: 0
forbiddenTopics: []
reason: ""
```

### 4.3 Outputs

Potential future response fields:

```yaml
worldCards:
  - cardId: ""
    title: ""
    visibility: author_only | review_only | production_only | user_facing
    summary: ""
    constraints: []
    forbiddenImplications: []
    sourceRefs: []
omittedBecauseHidden: []
omittedBecauseOutOfScope: []
reviewWarnings: []
```

### 4.4 Prohibitions

World Card Queryer must not:

- dump the full world bible into 04;
- turn questionnaire answers into canon without 01/user approval;
- expose author-only causal notes to user-facing fields;
- supply world causality as character knowledge without an evidence path;
- invent material culture, institutions, maps, factions, or power-system rules;
- approve setting-pack patches or runtime data.

## 5. Story Card Queryer / Situation-Continuity-Pressure Queryer

### 5.1 Responsibility

The historical label `Story Card Queryer` is retained for continuity, but its safer product meaning is Situation / Continuity / Pressure Queryer.

It retrieves scoped situation data, such as:

- current act / chapter objective;
- local causal pressure;
- unresolved questions;
- prior beat continuity;
- protagonist objective handoff;
- known blockers;
- available evidence paths;
- local misconceptions;
- what currently exists only as world causality rather than actor knowledge;
- what player-facing choices are currently natural.

It should answer "what situation, continuity, pressure, evidence, and constraint are relevant now?" rather than "what is the next plot?"

### 5.2 Inputs

Potential future request fields:

```yaml
requestId: ""
requestingWindow: 04 | 05 | 06 | 07 | 08 | 01
packetId: ""
currentSituationId: ""
sceneId: ""
protagonistObjective: ""
desiredHorizon: current_situation | next_choice | scene_end | chapter_end
visibilityTarget: author_only | review_only | production_only | user_facing
reason: ""
```

### 5.3 Outputs

Potential future response fields:

```yaml
situationCards:
  - cardId: ""
    nodeType: situation | continuity | pressure | objective | constraint | evidence | misconception | choice
    visibility: author_only | review_only | production_only | user_facing
    summary: ""
    causalPressures: []
    availableEvidence: []
    actorKnowledgeLimits: []
    localMisconceptions: []
    continuityRefs: []
missingContext: []
reviewWarnings: []
```

### 5.4 Prohibitions

Story Card Queryer / Situation-Continuity-Pressure Queryer must not:

- decide the next plot by itself;
- write final prose, AI result JSON, or storyboard frames;
- turn world causality into a forced reveal path;
- leak author-only causal answers into user-facing packet fields;
- force a player objective that violates the protagonist's knowledge boundary;
- let 04 skip protagonist reaction beats or objective handoff;
- bypass 01/user approval for canon story changes.

## 6. Asset Queryer

### 6.1 Responsibility

Asset Queryer retrieves scoped production asset data for storyboard consistency, such as:

- character asset ids;
- animal / companion asset ids;
- scene ids and zone ids;
- prop, weapon, costume, document, sign, vehicle, and material ids;
- variants such as wet, damaged, repaired, dirty, ceremonial, or disguised;
- asset status and review state;
- prompt-safe notes for storyboard pickup;
- missing asset warnings.

It should answer "which approved or candidate assets are needed for this packet / frame?" rather than "create new assets."

### 6.2 Inputs

Potential future request fields:

```yaml
requestId: ""
requestingWindow: 04 | 05 | 07 | 08 | 01
packetId: ""
frameId: ""
sceneId: ""
zoneId: ""
neededCharacters: []
neededObjects: []
variantNeeds: []
minimumStatus: candidate | review_ready | approved_reference | production_locked
promptSafeOnly: true
reason: ""
```

### 6.3 Outputs

Potential future response fields:

```yaml
assets:
  - assetId: ""
    assetType: character | animal | scene | zone | prop | weapon | costume | document | sign | vehicle | material | variant
    status: candidate | review_ready | approved_reference | production_locked
    variantOf: ""
    promptSafeSummary: ""
    requiredReferences: []
    forbiddenDrift: []
sceneRefs: []
zoneRefs: []
missingAssets: []
reviewWarnings: []
```

### 6.4 Prohibitions

Asset Queryer must not:

- approve candidate assets;
- generate new images;
- redesign characters, scenes, props, weapons, costumes, or materials;
- treat a candidate asset as `production_locked`;
- leak author-only causal notes into prompt-facing fields;
- invent scene ids, zone ids, or variants;
- start GPT storyboard pickup, Seedance, Oiioii, or external API work.

## 7. Relationship To Runtime Context

The existing interactive fiction runtime already has turn-level context surfaces:

- `ContextPacket`;
- `relevantSettings`;
- `retrievedMemories`;
- `knowledgeClaims`;
- context budget trimming;
- Validator and StateReducer guarded turn resolution.

The three queryers in this document are different. They are future product-layer retrieval contracts for new-world planning, script-ready packets, production assets, and storyboard delivery. They do not change:

- runtime ContextPacket generation;
- context budget rules;
- `contextHash`;
- `stateVersion`;
- `AiTurnResultValidator`;
- `TransitionValidator`;
- `NarrativeContinuityValidator`;
- `StateReducer`;
- SQLite schema or migrations.

If a later milestone wants to connect product queryers to runtime context, that requires a separate decision brief, implementation plan, tests, and 01/user approval.

## 8. Visibility And Secrecy Rules

Every future query response should carry a visibility boundary:

- `author_only`: never enters 04 prose, 06 display, 08 prompt, or user-facing packet fields.
- `review_only`: visible to 01 / 02 / 03 / 05 for review, not to user-facing output.
- `production_only`: visible to 07 / 08 for asset or storyboard preparation, not casually shown to the user.
- `user_facing`: safe for 06 display after 05 PASS and explicit 01 handoff.

Author-only world causality, future answers, director goals, sealed motives, or knowledge unavailable to present characters must not enter player-facing packet fields or image prompts. This boundary prevents perspective leaks; it is not a fixed reveal schedule.

## 9. 05 Query-Scope Review Checklist

05 should check:

- Was every query tied to a packet, frame, scene, objective, or review need?
- Did 04 request the minimum necessary world / story data?
- Did the response omit needed background, causing 04 to invent?
- Did the response include author-only causal material or future-spoiler material in a lower-visibility field?
- Did 04 create unregistered assets, scene ids, zone ids, materials, institutions, or story facts?
- Did 04 / 08 use an asset id, scene id, zone id, or variant not returned by the queryer?
- Did 08 treat a candidate asset as approved or production-locked?
- Did the packet or storyboard frame contradict query-returned material culture, scene locks, or story constraints?
- Did 06 receive only user-facing material after 05 PASS and explicit 01 handoff?

Verdict options:

- `PASS`: query scope was necessary, limited, and safe.
- `NEEDS_REVIEW`: missing cards, overbroad retrieval, unclear visibility, or unstable asset status.
- `FAIL`: author-only leak, invented formal ids, unauthorized asset redesign, wrong scene, forced reveal, forced concealment, or premature actor knowledge.

## 10. 08 Storyboard Pickup Rules

08 must:

- use query-returned asset ids, scene ids, zone ids, variants, and storyboard frame data;
- preserve asset status and never upgrade candidate assets;
- report missing assets instead of inventing them;
- preserve scene lock and zone/blocking rules;
- keep prompt notes free of author-only causal notes;
- flag visual drift when GPT storyboard images diverge from registered references;
- return evidence to 01 / 03 rather than directly presenting unreviewed images as final.

08 must not:

- redesign protagonists, companions, NPCs, weapons, costumes, props, or scenes;
- use Oiioii / Seedance / high-quality video generation as part of this query layer;
- treat storyboard pickup as a formal turn, setting-pack patch, or runtime state change.

## 11. Future Data Shapes Are Not Schema

The YAML snippets above are review templates only. They are not approved schema, database tables, JSON protocol, CLI arguments, or migration plans.

Future implementation would need separate decisions about:

- file templates versus SQLite;
- same DB versus sidecar DB;
- query audit logs;
- card id naming;
- source provenance;
- visibility filtering;
- 05 review report shape;
- 08 storyboard evidence shape.

## 12. Recommended Next Steps

Safe next steps remain decision-gated:

1. User / 00 answers `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md`.
2. 01 may assign a docs-only `NEW_WORLD_FOUNDATION_BRIEF`.
3. 01 may later assign a docs-only 09 world-card / story-card organization brief.
4. 01 may later assign query request / response templates.
5. Only after separate approval should 02 implement any files, CLI commands, schema, or runtime integration.

This blueprint does not authorize restarting 04 / 05 / 06 / 07 / 08, creating a setting pack, building a production asset registry, or generating storyboard images.

Related planning register:

- `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md` records non-canon blueprint candidates that future query templates may reference only after 01 explicitly selects them.
- Queryers must not treat the blueprint pool as canon, setting-pack data, runtime data, approved assets, or a full-world source to dump into 04.

## 13. 03 Review Focus

03 should verify:

- the three queryers are described as future product-layer specifications, not implemented capabilities;
- the 09 -> 04 -> 05 -> 06 -> 07 -> 08 positions are clear;
- 05 over-query / under-query / leak / invented-id checks are included;
- 08 must use returned asset ids / scene ids / zone ids / frame data and cannot redesign assets;
- runtime ContextPacket boundaries are preserved;
- author-only world causality cannot enter player-facing fields;
- no Kotlin, tests, schema, migrations, Validator, StateReducer, SQLite, external API, Oiioii, Seedance, or 04/05/06/07/08 activation is implied.
