# Qingli Script Packet Dry-Run Review

Status: `DOCS_ONLY_DRY_RUN_REVIEW`

Date: 2026-06-22

This document manually applies `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md` and `docs/QINGLI_SCENE_REGISTRY_SEED_BRIEF.md` to one conservative sample packet. It is not 04 generation, not a 05 formal review gate, not 06 display, not a formal Turn, not an AI result JSON, not a setting-pack patch, not a runtime scene definition, not an approved animation asset lock, and not Oiioii integration.

04 / 05 / 06 remain paused. This dry-run exists only to test whether the existing docs are usable for a future manual handoff.

## 1. VERDICT

`PASS`

The current script-ready scene packet spec and Qingli scene registry seed are sufficient to support one conservative, user-facing-safe sample packet inside `scene-qingli-herb-warehouse-perimeter`.

No blocker was found. The review suggests future packet authors should keep zone ids explicit and keep `oiioiiNotes` production-only, but these are authoring-process recommendations, not schema or runtime changes.

## 2. Positioning

This dry-run validates documentation usability only:

- packet shape from `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`;
- approved scene and zone boundaries from `docs/QINGLI_SCENE_REGISTRY_SEED_BRIEF.md`;
- scene-lock and animation-asset boundaries from `docs/SCENE_LOCK_AND_ANIMATION_ASSET_BOUNDARY.md`;
- authoring / review cautions from `docs/AUTHORING_QUALITY_GUIDELINES.md`.

It does not create canon, does not approve new setting-pack data, and does not authorize 04 / 05 / 06 to resume.

## 3. Sample Packet

The sample below is intentionally short and conservative. It is a dry-run artifact, not a story deliverable.

```yaml
packetTitle: "Qingli Perimeter Boundary Check - Dry-Run Sample"
packetStatus: historical_sample
sourceContext:
  sourceType: docs_only_dry_run
  sourceDocs:
    - docs/SCRIPT_READY_SCENE_PACKET_SPEC.md
    - docs/QINGLI_SCENE_REGISTRY_SEED_BRIEF.md
  note: "No DB dump, no formal turn, no player-approved content."
sceneReference:
  type: approvedScene
  sceneId: scene-qingli-herb-warehouse-perimeter
scenePurpose: "Show the sealed warehouse perimeter as a controlled public boundary where the player can orient, ask limited procedural questions, or choose whether to wait, observe, or request witnessed movement."
playerObjective: "Stay outside the powder line, understand the immediate boundary, and choose a safe next action without forcing warehouse access."
locationZone:
  sceneId: scene-qingli-herb-warehouse-perimeter
  zoneId: powder-line-edge
  blocking: "Player and companion remain outside the line; the sealed door stays visible but untouchable."
visualSetup:
  publicDescription: "Rain beads along a wet powder line in front of the sealed herb warehouse. The sealed door, cart-side mud, crowd edge, and registry-facing position remain visible from outside the boundary."
  visualAnchors:
    - wet powder / chalk line
    - sealed wooden warehouse door
    - rainwater and cart wheel ruts
    - crowd edge
charactersPresent:
  - characterId: char-player
    visiblePosition: "outside the powder line"
    visibleConstraint: "may observe and speak, but not cross or touch sealed objects"
  - characterId: char-ai-chan
    visiblePosition: "kept beside the player"
    visibleConstraint: "must not cross the powder line or approach seal strips"
  - characterId: char-patrol-he
    visiblePosition: "near the line, supervising movement"
    visibleConstraint: "must preserve boundary and public order"
  - characterId: char-clerk-luo
    visiblePosition: "near the registry / ledger-facing position"
    visibleConstraint: "records procedure, not hidden truth"
characterState:
  - characterId: char-player
    immediatePressure: "needs orientation and safe procedural options"
    knowledgeBoundary: "knows visible rain, crowd pressure, line, door, carts; does not know sealed-truth answers"
  - characterId: char-patrol-he
    immediatePressure: "must prevent boundary crossing"
    knowledgeBoundary: "knows public procedure and visible movement only"
approvedProps:
  - powder / chalk line
  - sealed door as visible protected object
  - rainwater
  - mud
  - wooden or iron-rimmed cart wheel marks
  - ledger / seal book if carried by clerk
forbiddenProps:
  - warehouse interior
  - hidden room, cellar, tunnel, or back door
  - modern rubber tires
  - plastic containers
  - electric lamps
  - firearms
  - visible proof of sealed-truth answers
beatList:
  - beatId: beat-01-orient-at-line
    purpose: "establish boundary and immediate player objective"
    visibleAction: "The player stops outside the wet powder line and checks where the line, crowd, carts, and sealed door sit in relation to each other."
    causalLink: "The line and patrol presence make orientation necessary before action."
  - beatId: beat-02-request-procedure
    purpose: "turn pressure into a safe interaction"
    visibleAction: "The player asks what can be observed without crossing or touching sealed objects."
    causalLink: "The player needs an allowed next step, not a forced mystery investigation."
  - beatId: beat-03-options-remain-local
    purpose: "leave usable choices within approved scene / transition boundaries"
    visibleAction: "The perimeter remains the current scene; waiting line, runoff edge, or witnessed awning movement stay possible only as later explicit choices."
    causalLink: "No transition happens inside this sample."
actionLines:
  - actor: char-player
    line: "Stops just outside the powder line and looks from the cart-side mud to the sealed door without reaching past the boundary."
    blockingZone: powder-line-edge
  - actor: char-patrol-he
    line: "Keeps the boundary visible and answers in procedural terms instead of granting access."
    blockingZone: powder-line-edge
dialogueLines:
  - speaker: char-player
    line: "I will stay outside the line. What can be observed or recorded from here without touching the seal?"
    knowledgeBoundary: "player asks only about visible procedure"
  - speaker: char-patrol-he
    line: "Outside the line, you can ask and look. Crossing, touching, or opening is a separate matter."
    knowledgeBoundary: "patrol speaks from public boundary rules only"
interactionChoices:
  - choiceId: choice-wait-outside
    label: "wait outside the control line"
    allowedBecause: "matches `scene-qingli-outer-waiting-line` transition reference if the player later chooses it"
  - choiceId: choice-observe-runoff
    label: "ask to observe the runoff edge"
    allowedBecause: "matches `scene-qingli-runoff-channel` transition reference if explicit intent is supplied later"
  - choiceId: choice-awning-witness
    label: "request witnessed movement to the awning"
    allowedBecause: "matches `scene-qingli-herb-awning` transition reference if explicit witnessed movement intent is supplied later"
stateConstraints:
  - "No formal turn is run."
  - "No scene transition is applied."
  - "No warehouse entry, seal touch, goods touch, or hidden truth reveal."
  - "No setting-pack or runtime state changes."
continuityChecks:
  - "Scene id remains `scene-qingli-herb-warehouse-perimeter`."
  - "Powder line, crowd edge, cart side, and doorfront mud are zones / blocking positions, not new scenes."
  - "Props match approved perimeter seed entry."
  - "Material culture uses wooden / iron-rimmed cart traces, not modern tire tracks."
oiioiiNotes:
  cameraZones:
    - "wide shot: powder line, crowd edge, sealed door"
    - "medium shot: player and companion outside the line"
    - "insert: wet line and cart-side mud"
  assetReuse:
    - "perimeter rain / sealed door / cart-side mud assets"
  animationRisks:
    - "Do not introduce warehouse interior, modern props, or new street assets."
secrecyKnowledgeNotes:
  authorOnlyExcluded: true
  notes:
    - "This sample intentionally omits sealed-truth answers, culprit motive, director goals, and character-unknown knowledge."
reviewSummary:
  verdict: PASS
  reviewer: docs_only_dry_run
  requiredFixes: []
```

## 4. Field Coverage Check

| Field | Filled in sample? | Notes |
| --- | --- | --- |
| `packetTitle` | Yes | Human-readable dry-run title. |
| `packetStatus` | Yes | Uses `historical_sample`, not `formal_candidate`. |
| `sourceContext` | Yes | References docs only; no DB dump. |
| `sceneReference` | Yes | Approved scene id only. |
| `scenePurpose` | Yes | Describes perimeter orientation and safe procedural pressure. |
| `playerObjective` | Yes | Objective stays within knowledge boundary and immediate need. |
| `locationZone` | Yes | Uses `powder-line-edge` as a zone / blocking position. |
| `visualSetup` | Yes | Uses public-safe visual anchors. |
| `charactersPresent` | Yes | Uses conservative existing / scenario-supported characters. |
| `characterState` | Yes | Review-only pressure and knowledge boundary; no secrets. |
| `approvedProps` | Yes | Matches seed brief material culture. |
| `forbiddenProps` | Yes | Repeats public-safe forbidden additions, no hidden reveal. |
| `beatList` | Yes | Three short causal beats. |
| `actionLines` | Yes | Animation-friendly blocking, no final prose claim. |
| `dialogueLines` | Yes | Usable and knowledge-limited. |
| `interactionChoices` | Yes | Choices refer to allowed future transition surfaces but do not apply them. |
| `stateConstraints` | Yes | Explicitly blocks formal turn / transition / setting changes. |
| `continuityChecks` | Yes | Covers scene, zone, props, material culture. |
| `oiioiiNotes` | Yes | Production-only notes, no integration implementation. |
| `secrecyKnowledgeNotes` | Yes | Explicit author-only exclusion. |
| `reviewSummary` | Yes | Dry-run PASS only; not formal 05 PASS. |

Coverage result: `PASS`.

The field list is usable for a small packet without schema changes. The only process recommendation is to keep `zoneId` names stable inside future packets, even though this document does not approve new schema fields.

## 5. Registry Alignment Check

| Check | Result | Evidence |
| --- | --- | --- |
| Approved scene id | PASS | Uses `scene-qingli-herb-warehouse-perimeter`. |
| Zone / blocking only | PASS | `powder-line-edge`, crowd edge, cart side, and doorfront mud remain zones. |
| Props | PASS | Uses powder line, sealed door, rainwater, mud, wooden / iron-rimmed cart marks, ledger. |
| Forbidden additions avoided | PASS | No warehouse interior, hidden room, modern tools, rubber tires, firearms, or hidden proof. |
| NPCs | PASS | Uses player, Ai-chan, Patrolman He, and Clerk Luo, all supported by Pilot-2 scenario / seed context. |
| Transitions | PASS | Mentions waiting line, runoff edge, and awning only as future explicit choices; no transition is applied. |
| Author-only boundary | PASS | Records boundary existence without copying secret contents. |

Registry alignment result: `PASS`.

No sample field requires upgrading a local position into a new scene. The perimeter seed entry is sufficient for this small dry-run.

## 6. 05-Style Review Dry-Run

This is not a formal 05 review gate. It is a manual application of the checklist to test the checklist.

| Review item | Dry-run verdict | Reason |
| --- | --- | --- |
| Scene registry alignment | PASS | Packet stays inside one approved scene and one zone. |
| Animation producibility | PASS | Visual anchors and camera zones are stable and reusable. |
| Information completeness | PASS | All required packet fields have minimal content. |
| Player objective clarity | PASS | Objective is orientation and safe procedure, not forced local mystery solving. |
| Character motivation and knowledge boundary | PASS | Patrol and clerk act from public procedure only. |
| Material culture consistency | PASS | Uses cart wheel marks and rain / mud, not modern tire or petrochemical language. |
| Beat causality | PASS | Boundary -> request procedure -> local choices. |
| Dialogue usability | PASS | Lines are short, speakable, and not exposition dumps. |
| Interaction readiness | PASS | Choices are actionable but still require later explicit player intent. |
| Secrecy safety | PASS | No author-only secret or sealed-truth answer appears. |
| Manual approval safety | PASS | Packet is marked dry-run / historical sample and cannot go directly to 06. |

05-style dry-run result: `PASS`.

## 7. 06 Display Boundary Dry-Run

This dry-run must not be displayed by 06 unless 01 later explicitly hands it off, 05 formally passes it, and 06 receives an approved display scope.

Potential user-facing fields after a future 05 PASS:

- `packetTitle`;
- `packetStatus`, if clearly labeled as sample / not final;
- `scenePurpose`;
- `playerObjective`;
- `locationZone`;
- `visualSetup.publicDescription`;
- `charactersPresent.visiblePosition` / `visibleConstraint`;
- reviewed `beatList`;
- reviewed `actionLines`;
- reviewed `dialogueLines`;
- `interactionChoices`.

Review-only fields:

- `sourceContext`;
- `characterState`;
- `stateConstraints`;
- `continuityChecks`;
- `secrecyKnowledgeNotes`;
- `reviewSummary`.

Production-only fields:

- `approvedProps`;
- `forbiddenProps`;
- `oiioiiNotes`;
- camera / asset reuse / animation risk notes.

Display boundary result: `PASS`.

The split is clear enough for a future 06 display contract, but this document itself is not a 06-ready packet.

## 8. Findings

Dry-run result: `PASS`.

The current spec + seed brief can support a conservative scene packet sample when the packet:

- uses an approved scene id;
- treats local positions as zones / blocking;
- marks the packet as historical / dry-run sample;
- keeps secrets and production-only notes out of user-facing sections;
- lists transitions as possible future choices, not applied state changes;
- keeps material culture conservative.

Recommendations for future docs-only work:

1. If 01 wants another sample, use `scene-qingli-herb-awning` or `scene-qingli-outer-waiting-line` in a separate dry-run.
2. If repeated samples reveal naming drift, consider a docs-only `zoneId` naming note before any schema design.
3. If 04 / 05 / 06 resume later, start from a new 01 handoff and do not treat this dry-run as 04 draft, 05 PASS, or 06-approved display content.

Not recommended:

- Do not convert this sample into setting-pack canon.
- Do not treat the sample as a formal player-approved scene.
- Do not add runtime Validator, StateReducer, schema, or context protocol enforcement from this dry-run.
- Do not start Oiioii integration from this document.

## 9. 03 Review Focus

03 should verify:

- this file is docs-only and does not imply formal content approval;
- the sample uses `scene-qingli-herb-warehouse-perimeter` and does not create a new scene;
- required packet fields are covered;
- 05 / 06 sections are explicitly dry-run only;
- no author-only secret or hidden truth is exposed;
- no runtime, schema, Validator, StateReducer, `resolve-turn`, `stateVersion`, or `contextHash` change is implied.
