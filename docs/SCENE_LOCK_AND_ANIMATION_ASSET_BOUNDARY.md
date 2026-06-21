# Scene Lock And Animation Asset Boundary

Status: `DOCS_ONLY_GUIDANCE`

Date: 2026-06-22

This document separates runtime scene continuity from production scene / animation asset planning. It does not change Kotlin source, schema, migrations, Validator, StateReducer, `resolve-turn`, `stateVersion`, `contextHash`, or setting-pack fields.

## 1. VERDICT

The project has runtime scene definitions, scene instances, and scene transitions from MVP-4 Scene Thread Runtime. Those are necessary for CLI / Validator / StateReducer / context continuity.

They are not the same as animation-ready scene planning or production asset lock. Future Oiioii / animation generation needs a smaller, stable visual scene registry so every turn does not freely create new visual asset requirements.

## 2. Layer Definitions

Runtime scene:

- formal scene definition and scene instance used by CLI, Validator, StateReducer, context, and transition logic;
- answers continuity questions such as where characters are, whether a transition is legal, and which scene requirements apply;
- may be sufficient for story-state validation but not for production asset stability.

Production scene / animation asset:

- stable visual asset node intended for future Oiioii or animation generation;
- answers production questions such as what must be drawn, animated, lit, reused, blocked, and kept visually consistent;
- needs a stricter approved scene set than ordinary prose description.

The two layers are related but not equivalent. A runtime transition can be legal without authorizing the writer to invent unlimited new animation-ready scenes.

## 3. Scene Registry Rule

Each chapter, episode, or act should have a finite approved scene set before animation-oriented drafting.

Each approved production scene should define at least:

- stable scene id;
- public visual description;
- visual anchors: fixed objects, light, weather, materials, landmarks;
- camera zones / blocking zones;
- allowed NPCs;
- allowed objects / materials;
- interaction zones;
- transition conditions;
- forbidden additions;
- author-only boundary.

Public visual descriptions must not include author-only secrets, sealed-truth answers, culprit identity, director-only goals, or hidden plot explanations.

## 4. Authoring Rule

04 / AI authoring should not freely create formal scenes.

If a draft needs a new space, label it as `sceneCandidate` and report it to 01. Do not treat it as an approved scene until 01 explicitly approves it.

Within one approved scene, many local positions should usually be zones or blocking, not new scenes:

- powder-line edge;
- awning underside;
- behind the crowd;
- mud before the gate;
- ledger table corner;
- drainage edge;
- cart-side position;
- doorway threshold.

Scene movement is allowed only through approved transition or explicit 01 decision. Camera movement, character blocking, or a new angle does not automatically create a new production scene.

For 04 / 05 / 06 handoff, scene decisions should be captured in a script-ready scene packet before display or animation-oriented production use. See `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md` for the packet fields, `sceneCandidate` flow, 05 review checklist, 06 display contract, and explicit human approval gate.

## 5. Review Gate Checklist

The authoring review gate should ask:

- Is the scene in the approved scene set?
- Are visual anchors stable from previous outputs?
- Did the draft add an unapproved new location, building, room, interior, road segment, or hidden space?
- Did the draft mistake a camera corner, blocking zone, or local position for a new scene?
- Are allowed NPCs and objects still within the scene registry?
- Are materials and lighting consistent with the approved visual description?
- If a `sceneCandidate` appears, was it handed to 01 instead of being treated as approved output for 06 / animation?
- Does the public visual description avoid author-only secrets?

Review result should be concise:

```text
Scene lock:
approved scene id: <id or NONE>
sceneCandidate: YES / NO - <candidate id or reason>
unapproved additions: PASS / RISK / FAIL - <one sentence>
visual anchors stable: PASS / RISK / FAIL - <one sentence>
handoff needed: YES / NO - <01 decision needed?>
```

## 6. Pilot-2 Current Evidence

Known Pilot-2 runtime scene ids from existing docs / setting pack evidence:

- `scene-qingli-herb-warehouse-perimeter`
- `scene-qingli-herb-awning`
- `scene-qingli-runoff-channel`
- `scene-qingli-outer-waiting-line`

These ids must not be translated or casually renamed.

The mountain path / dense fog prologue bridge currently remains a playtest prologue bridge / `sceneCandidate`, not a formal runtime scene and not an approved production asset scene, unless 01 later approves it.

## 7. Boundary Examples

Within one production scene:

- moving from the powder-line edge to the awning underside may be a zone / blocking change if the approved scene covers both visual areas;
- looking past the crowd may be a camera zone;
- stepping from mud to a gate threshold may be blocking;
- placing a character beside a cart may be an interaction zone.

Potential new scene candidates:

- a new interior room;
- a hidden cellar;
- a mountain trail outside the approved episode set;
- a new village street not in the registry;
- an otherworld landmark not yet approved;
- a dream / memory location needing its own visual assets.

## 8. Future Work Boundaries

This milestone is docs-only.

Not approved:

- runtime quality gate;
- Validator changes;
- schema or migration changes;
- StateReducer changes;
- setting-pack field additions;
- context protocol changes;
- automatic scene registry enforcement;
- direct SQLite writes;
- committing generated assets.

Future engineering may separately design fields such as `productionSceneId`, `visualAnchors`, `blockingZones`, `cameraZones`, `allowedObjects`, `forbiddenAdditions`, `sceneCandidate`, or `assetLockStatus`. That requires a separate 01 / user-approved milestone.

Script-ready packet fields such as `packetStatus`, `reviewVerdict`, `displaySafeFields`, or `animationAssetNotes` are also future implementation candidates only. They are not approved by this docs-only boundary note.
