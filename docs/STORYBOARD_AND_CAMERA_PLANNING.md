# Storyboard And Camera Planning

Status: `FUTURE_PLANNING_NOT_IMPLEMENTED`

Date: 2026-06-22

This document records future planning for storyboard frames, camera language, GPT-assisted storyboard pickup, and reviewable storyboard delivery. The current product target is storyboard images / storyboard packets, not high-quality animation. Seedance, Oiioii, and other video-generation tools are optional downstream work only. This document does not change Kotlin source, tests, schema, migrations, Validator, StateReducer, `resolve-turn`, `stateVersion`, `contextHash`, setting-pack fields, runtime gates, GPT image generation tooling, Seedance integration, or Oiioii integration.

## 1. Purpose

The project is moving from raw interactive prose toward script-ready scene packets that can produce reviewable storyboard images and storyboard packets. For that workflow, a text-only script is not enough. The project needs storyboard planning and camera intent so that 08 can retrieve / review storyboard frames and the user can audit what will be seen before any optional downstream video work:

- which approved scene is being used;
- where characters and important objects are positioned;
- what each frame should communicate;
- what is outside the current viewpoint, evidence path, or actor knowledge;
- what can be shown clearly without inventing new locations or assets.

This file is a planning note only. It is not a working storyboard generator, image generator, Seedance adapter, Oiioii adapter, schema, or runtime validator.

## 2. Definitions

Storyboard / story board:

- a sequence of frame-level visual plans;
- may be text-only at first;
- may later be rendered into rough image storyboard frames;
- should support scene continuity, character blocking, camera intent, and review.

Camera language:

- camera purpose, framing, angle, movement, and viewer guidance;
- used to clarify information, hide information, or guide temporary viewer interpretation;
- should not become decoration or a fixed "make this person look evil, expose a twist later" template.

Scene lock:

- the approved production scene remains stable across storyboard frames;
- camera angle and blocking may change inside the scene;
- a new room, street, interior, road, or landmark requires `sceneCandidate` handling.

## 3. Planning Verdict

Storyboard frames should be scene-locked and asset-aware.

The story can advance and the camera can move, but the production scene and spatial relationships must remain stable unless an approved transition occurs.

Why this matters:

- visual generation can drift if every frame reinvents the background;
- character positions may jump without a stable blocking map;
- props can appear or disappear if the scene asset boundary is not fixed;
- 08 storyboard pickup needs repeatable visual anchors;
- user review should happen before a storyboard frame becomes production guidance.

Current product verdict:

- storyboard image / frame packet delivery is the current final deliverable;
- Seedance / Oiioii / high-quality video generation is optional future downstream work;
- no storyboard frame becomes deliverable until the packet and required assets have passed the manual review / approval path.

## 4. Recommended Layering

### 4.1 Script-ready scene packet

The scene packet remains the parent object. It defines:

- scene id or prologue bridge;
- scene purpose;
- player objective;
- approved location / zone;
- visual setup;
- characters present;
- beat list;
- action lines;
- dialogue lines;
- interaction choices;
- continuity checks;
- optional downstream video notes.

See `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`.

### 4.2 Storyboard frames

Storyboard frames are a future child layer under an approved or candidate scene packet.

Suggested fields:

```yaml
storyboardFrames:
  - frameId: ""
    sceneId: ""
    zoneId: ""
    beatId: ""
    framePurpose: ""
    shotType: establishing | wide | medium | closeup | insert | overShoulder | pointOfView
    cameraPurpose: clarify_space | show_risk | guide_attention | hide_information | show_evidence_detail | neutral_coverage
    viewerEffect: ""
    subject: ""
    blocking:
      foreground: []
      midground: []
      background: []
    visibleAction: ""
    dialogueOrAudio: ""
    gazeTarget: ""
    facingTarget: ""
    eyelineReason: ""
    nonFacingReason: ""
    interactionTarget: ""
    continuityCheck: ""
    doNotShow: []
    continuityAnchor: []
    oiioiiPromptNotes: []
    reviewNotes: []
```

These eyeline/facing fields are future documentation requirements only, not an
implemented schema.

### 4.2.1 Eyeline / Facing Direction Rule

Every future storyboard or image-facing frame that includes dialogue, reaction,
attention, avoidance, threat monitoring, companion interaction, or
interpersonal blocking must carry explicit eyeline and facing direction fields:

- `gazeTarget`: who or what the character is looking at;
- `facingTarget`: where the face/body is oriented;
- `eyelineReason`: why that gaze/facing choice is correct for the beat;
- `nonFacingReason`: required when a character speaks or reacts while not
  facing the interaction target, and must state the intentional non-facing
  reason;
- `interactionTarget`: required for dialogue or directed action;
- `continuityCheck`: confirms gaze/facing matches prior blocking unless it is
  intentionally changed.

If 陸知行 speaks to 愛醬, default `gazeTarget`, `facingTarget`, and
`interactionTarget` should target 愛醬. If he does not look at 愛醬 while
speaking, the frame must state why: fear, concealment, watching danger, shame,
deliberate coldness, tactical attention, or another beat-specific intentional
non-facing reason. If 愛醬 reacts to 陸知行, the frame must state whether she
looks at him, turns an ear/body toward him, ignores him deliberately, watches a
threat, or tracks scent/sound.

For Chenxing examples, current-facing protagonist name is only 陸知行; 衛昭 is
superseded in derived/current-facing materials. 愛醬 has her own story line and
must not be reduced to pet, tool, clue machine, mascot, combat asset, or
mount-only growth. Living world first; final-product chains serve the world
later. Old opening/mainline material remains discussion evidence only, not
locked plot, canon, or final-product content.

Review checks:

- dialogue frames without eyeline/facing target are incomplete;
- contradictory gaze/facing versus visible action is a drift risk;
- accidental back-facing is a fail unless explicitly intentional;
- gaze/facing must not reveal author-only information or
  character-inaccessible knowledge;
- this rule supports storyboard/camera, prompt/asset cards, image-facing
  delivery proof, renderer/package QA, and human approval gates.

See `docs/CODEX_STORYBOARD_EYELINE_FACING_DIRECTION_RULE_PATCH_V0.md`.

### 4.3 Storyboard images

GPT or a lower-cost image model should be the preferred future pickup tool for rough storyboard images. Those images are the current target deliverable only after they pass 08 verification and user-facing approval rules; before that, they are visual drafts.

Seedance / Oiioii should not be used for early storyboard exploration by default. Their role should be reserved for later optional video generation after the script packet, storyboard frames, scene lock, storyboard images, and visual references have passed human review. This avoids spending video-generation budget on unclear scenes, unstable blocking, or unapproved camera ideas.

Recommended order:

1. produce text storyboard frames;
2. review scene lock, blocking, and secrecy boundaries;
3. 07 prepares needed asset candidates and reference requirements;
4. 08 retrieves rough storyboard images with GPT / image-model quota only after the text frames and asset candidates are ready;
5. review images for drift;
6. deliver only approved storyboard images / packets to the user;
7. use approved storyboard packets as optional downstream video references only after a later decision.

Do not let image generation invent new rooms, costumes, props, weather, lighting, character designs, or author-only causal disclosures without approval.

Cost rule:

- GPT / image-model quota may be used for draft storyboard exploration when the user approves that workflow.
- Seedance / Oiioii should be treated as optional downstream video generation, not an exploratory storyboard sketch tool.
- If a storyboard image is not yet scene-locked, asset-aware, and human-approved, it should not be sent to any video pipeline as production input.

## 5. Camera Use Principles

Camera planning should be restrained.

Use camera notes when they solve a story or production problem:

- clarify where characters are;
- show a boundary or risk;
- guide viewer attention to a relevant object;
- preserve an author-only boundary by not showing inaccessible causal facts;
- plant a visible detail that can be reinterpreted later;
- support an approved evidence-discovery beat.

Do not use camera notes as decoration:

- do not give every NPC a suspicious angle;
- do not make every authority figure look evil by default;
- do not force every character into a "mislead now, redeem later" pattern;
- do not use shadows, low angles, or closeups as generic drama;
- do not use camera tricks to replace missing causality.

Most frames should be clear coverage. Biased or withholding camera choices should appear only when the beat needs them.

## 6. Camera Categories

Ordinary coverage:

- shows space, position, action, and readable continuity;
- default choice for most frames.

Position / blocking frame:

- establishes where the protagonist, companion, NPCs, boundaries, and props are;
- useful for player choices and animation continuity.

Stance / viewer-bias frame:

- gently pushes the viewer to trust, doubt, fear, or focus on someone;
- should be used sparingly and only when the scene has a real information imbalance.

Reveal frame:

- shows a detail that changes the viewer's interpretation;
- should be tied to a beat, not added for style.

Do-not-show frame:

- explicitly blocks the camera from showing author-only secrets, sealed causal answers, hidden culprit evidence, director goals, or character-inaccessible knowledge.

## 7. Scene Lock Rules For Storyboard Frames

Each storyboard frame should reference one of:

- an approved `sceneId`;
- an approved prologue bridge;
- a clearly labeled `sceneCandidate`.

Inside one approved scene, the following can be zones or blocking, not new scenes:

- powder-line edge;
- awning underside;
- crowd edge;
- cart side;
- drainage edge;
- gate threshold;
- foreground / midground / background positions.

The following should usually become `sceneCandidate` unless already approved:

- a new interior room;
- a hidden cellar;
- a new street;
- a mountain path outside the approved prologue bridge;
- a dream / memory space;
- an unseen warehouse interior;
- any location requiring new visual assets.

## 8. GPT Storyboard Pickup And Optional Video Downstream

Future GPT storyboard pickup may need:

- text storyboard frame data;
- stable scene registry entries;
- registered production asset ids and approved reference requirements;
- scoped Asset Queryer / Situation-Continuity-Pressure Queryer responses when that future product layer is approved;
- approved character design references;
- approved prop and material lists;
- blocking notes;
- camera purpose;
- do-not-show boundaries;
- low-cost image draft prompts;
- image drift review notes.

Future optional Seedance / Oiioii handoff may need:

- text storyboard frame data;
- approved rough storyboard images;
- stable scene registry entries;
- registered production asset ids and approved reference requirements;
- approved character design references;
- approved prop and material lists;
- blocking notes;
- camera purpose;
- prompt-safe visual notes;
- do-not-show boundaries.

This document does not implement GPT image generation, Seedance handoff, Oiioii handoff, or any external API.

Future work should decide whether GPT storyboard notes and optional video notes live in:

- scene packets;
- storyboard frame packets;
- a separate storyboard prompt sheet;
- a separate optional video prompt sheet;
- a production asset registry.

For future planning of locked characters, animals, props, weapons, costumes, scene references, reference view requirements, and variants, see `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md`.

For future planning of scoped world, situation / continuity / pressure, and asset retrieval before storyboard pickup, see `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md` and `docs/SIMULATION_FIRST_KNOWLEDGE_TRUTH_RULES.md`. The query layer is a future product contract only; it does not authorize 08 to generate images, redesign assets, leak author-only causality, or call external services.

## 9. 04 / 05 / 06 / 07 / 08 Future Workflow Impact

If this planning becomes an approved workflow later:

- 04 should include only the minimum useful camera / storyboard fields in a script-ready scene packet.
- 04 should not write a full shot list for every beat unless explicitly requested.
- 05 should review whether camera notes are necessary, restrained, producible, scene-locked, and secrecy-safe.
- 06 should show storyboard / camera fields only when 01 decides they are safe and useful for human approval.
- After 06 receives explicit user approval, 01 may assign 07 to prepare asset candidates needed for the storyboard.
- 08 should retrieve / assemble storyboard images and run storyboard-specific drift checks before anything is shown as deliverable.
- 08 should reference query-returned asset ids, scene ids, zone ids, variants, and storyboard frame data where the future query-layer workflow applies.
- Optional video generation may be considered only after storyboard delivery is approved and 01/user explicitly opens a new downstream milestone.

05 should fail or return for review when:

- storyboard frames create unapproved scenes;
- camera notes leak inaccessible or author-only information;
- camera choices over-direct every NPC;
- storyboard images or prompts change approved scene assets;
- a frame cannot be produced from the locked scene and approved visual anchors.

## 10. Historical Sample Only: Conservative Qingli Opening Frame

This is a historical planning example, not current new-world base, not approved canon, not a storyboard deliverable, and not a Seedance / Oiioii prompt. Qingli material is deprecated / paused as a new opening foundation by `docs/WORLD_BIBLE_REBUILD_DECISION_BRIEF.md`; keep this only as a format and drift-review sample.

```yaml
frameId: "frame-qingli-perimeter-001"
sceneId: "scene-qingli-herb-warehouse-perimeter"
zoneId: "powder-line-edge"
beatId: "beat-first-boundary-pressure"
framePurpose: "Show that the protagonist and Ai-chan have reached a boundary they do not understand."
shotType: "medium"
cameraPurpose: "clarify_space"
viewerEffect: "The viewer understands the powder line, the dog, and the patrol pressure without learning sealed causal answers."
subject: "Ai-chan stopped near the powder line; protagonist slightly behind; patrol presence at the edge of frame."
blocking:
  foreground:
    - "powder line across wet ground"
  midground:
    - "Ai-chan outside the line"
    - "protagonist behind Ai-chan"
  background:
    - "sealed warehouse door and crowd shapes, without interior view"
visibleAction: "Ai-chan's nose angles toward a scent; one front paw stops short of the line."
dialogueOrAudio: "Patrol voice off to one side: 'Call the dog back. Do not let it cross.'"
doNotShow:
  - "warehouse interior"
  - "sealed causal evidence"
  - "author-only cause of contamination"
continuityAnchor:
  - "rain-wet ground"
  - "powder line"
  - "sealed door"
  - "no leash"
oiioiiPromptNotes:
  - "Historical only. Do not use as current downstream video input."
reviewNotes:
  - "Frame is for spatial clarity, not for making the patrolman look evil. Do not use Qingli as the new-world base."
```

## 11. Review Questions

Before a storyboard / camera plan is shown or used:

- Is the frame tied to an approved scene or candidate?
- Does it preserve scene lock?
- Does the camera note solve a real story or production need?
- Is the camera over-directing or creating melodrama?
- Are character positions consistent with prior frames?
- Are props and materials approved for the scene?
- Does the frame avoid author-only secrets?
- Could a simpler ordinary coverage frame do the job?
- If an image is generated, does it match the text frame instead of inventing details?

## 12. Future Work Boundaries

Not implemented by this document:

- automatic storyboard image generation;
- Oiioii API or browser integration;
- Seedance integration;
- GPT image-generation integration;
- runtime storyboard fields;
- schema / migration changes;
- Validator or StateReducer rules;
- new setting-pack fields;
- approved production asset locks;
- formal 04 / 05 / 06 restart;
- generated image storage or review tooling.

Future approved milestones may separately design:

- storyboard frame templates;
- optional downstream video prompt sheets;
- GPT storyboard image prompt templates;
- image review checklists;
- scene / character reference asset registries;
- export formats for animation production.
