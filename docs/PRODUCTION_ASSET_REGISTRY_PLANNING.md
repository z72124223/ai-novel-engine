# Production Asset Registry Planning

Status: `FUTURE_PLANNING_NOT_IMPLEMENTED`

Date: 2026-06-22

This document records future planning for a production asset registry that serves storyboard consistency first. It does not change Kotlin source, tests, schema, migrations, Validator, StateReducer, `resolve-turn`, `stateVersion`, `contextHash`, setting-pack fields, runtime gates, GPT image generation tooling, file storage, Seedance integration, or Oiioii integration.

## 1. Purpose

The project needs a stable production asset registry before storyboard delivery can scale.

Scene lock alone is not enough. If GPT or any later video tool generates a character, weapon, prop, costume, or scene reference once and later regenerates it differently, the story may remain logically valid while the storyboard deliverable becomes visually unstable.

Future production needs a rule:

- once a visual asset is adopted, it must be registered;
- later packets should reference the registered asset id;
- later generation should not redesign it unless the story creates an approved variant;
- storyboard pickup should use reviewed references, not exploratory descriptions;
- optional downstream video generation should use approved storyboard / asset references, not exploratory descriptions.

This is a future planning document only. It does not create an asset database or approve any production asset.

## 2. Definitions

Production asset:

- any reusable visual object needed for storyboard-first production and possible optional downstream video;
- includes characters, animals, costumes, weapons, props, locations, vehicles, signs, seals, documents, and recurring visual effects.

Asset registry:

- a future list of approved or candidate assets;
- stores stable ids, public descriptions, reference requirements, variants, allowed use, forbidden drift, and review status.

Reference sheet:

- visual reference used to keep repeated generation consistent;
- may be a three-view sheet, expression sheet, prop sheet, panorama, zone map, material swatch sheet, or variant sheet.

Variant:

- a story-caused alternate state of an existing asset;
- examples: broken sword, wet coat, burned seal, bloodied sleeve, cracked cart wheel;
- should be registered as a new variant id instead of overwriting the original asset.

## 3. Planning Verdict

The project should separate story-state objects from production assets.

A story may say "the sword breaks." Production should not silently replace the original sword design. It should create:

- original weapon asset: `weapon-x-v1`;
- broken variant: `weapon-x-broken-v1`;
- cause: the story beat that broke it;
- allowed later use: only after that beat.

The same rule applies to scenes, characters, clothes, documents, wounds, weathered props, and important marks.

## 4. What Must Be Registered

Register an asset when it is likely to be reused, seen clearly, animated, referenced in later beats, or used as a production anchor.

Must register:

- protagonist design;
- recurring companion / animal design;
- named NPC design;
- recurring costume or uniform;
- weapons;
- important props;
- fixed scenes;
- scene zones and visual anchors;
- recurring vehicles or carts;
- important documents, signs, seals, tokens, symbols;
- injury / damage / weathering variants that affect continuity.

Usually do not register:

- one-off background crowd silhouettes;
- generic mud, rain, smoke, or darkness unless it has a special visual rule;
- disposable unnamed objects that never recur;
- temporary camera framing that does not create a new visual asset.

## 5. Reference Requirement Rules

Different asset types need different reference views.

### 5.1 Characters

Requirement: three-view sheet by default.

Recommended references:

- front view;
- side view;
- back view;
- neutral expression;
- 2 to 4 expression / pose notes if the character is recurring;
- costume notes;
- scale reference against another registered asset when useful.

Why:

- characters turn, walk, speak, and appear in closeups;
- one image is too easy to drift across GPT or optional video generation;
- clothing, hair, body shape, scars, and accessories must remain stable.

### 5.2 Animals

Requirement: three-view sheet by default.

Recommended references:

- front view;
- side view;
- back view;
- size relative to protagonist;
- fur length / markings;
- ear, tail, gait, and posture notes;
- behavior-safe notes that do not humanize the animal beyond setting rules.

Why:

- animal silhouettes drift easily;
- side profile matters for motion;
- markings, fur length, and size are continuity anchors.

### 5.3 Weapons

Requirement: three-view or prop sheet by default.

Recommended references:

- full weapon front / profile;
- grip / handle detail;
- blade or head detail;
- scale reference;
- sheath / carrying state when relevant;
- damage variants as separate ids.

Why:

- weapons are often held, dropped, broken, repaired, or shown in inserts;
- shape and damage state must not change randomly.

### 5.4 Important Props

Requirement:

- important handheld props: front / side / back or prop sheet;
- flat documents / signs / seals: front view plus material and size notes;
- background props: one reference image may be enough if not animated or close-up.

Examples:

- ledger, seal strip, token, medicine packet, letter, cart wheel, bottle, charm.

### 5.5 Costumes

Requirement: front / side / back when recurring.

Costume assets should be separate from character body design when:

- characters can change clothes;
- uniforms recur across many NPCs;
- damage / wet / stained states matter;
- social rank or institution is communicated by clothing.

### 5.6 Fixed Scenes

Requirement: panorama / establishing view plus zone map.

Recommended references:

- establishing panorama or wide scene reference;
- approved zones;
- camera-safe angles;
- visual anchors;
- lighting / weather / material palette;
- allowed props;
- forbidden additions;
- transition edges.

Why:

- fixed spaces are not solved by a three-view character sheet;
- animation needs stable layout, not just a pretty background;
- a zone map prevents every camera angle from becoming a new scene.

### 5.7 Scene Zones

Scene zones should be registered when they affect blocking or repeated choices.

Examples:

- powder-line edge;
- awning underside;
- crowd edge;
- cart side;
- drainage channel edge;
- warehouse threshold.

Zones are not full scenes unless they require a separate production background or transition.

## 6. Asset Lifecycle

Suggested future statuses:

| Status | Meaning | Can downstream use it? |
| --- | --- | --- |
| `candidate` | Proposed by 04 / 07 / user, not reviewed. | No |
| `review_ready` | Complete enough for 05 / human review. | No |
| `approved_reference` | Approved as visual reference for storyboard work. | Yes, for reviewed storyboard drafts |
| `production_locked` | Approved for final storyboard packet use and possible optional downstream video handoff. | Yes, for approved production |
| `variant_candidate` | Proposed altered state of an existing asset. | No |
| `retired` | No longer used except historical reference. | No new use |

07 output remains `candidate` or `review_ready` unless 01 / 05 / 06 / user approval explicitly advances it. No asset should jump from generated image to `approved_reference` or `production_locked` without review. A storyboard made from unlocked assets is a draft, not a final deliverable.

## 7. Suggested Registry Fields

This is a future documentation shape, not an implemented schema.

```yaml
assetId: ""
assetType: character | animal | costume | weapon | prop | scene | zone | vehicle | document | visualEffect | variant
assetStatus: candidate
source:
  createdFrom: user | 04 | 07 | GPT | Oiioii | existingSetting | other
  sourcePacketId: ""
  sourceFrameId: ""
  sourcePromptSummary: ""
publicDescription: ""
referenceRequirements:
  requiredViews: []
  optionalViews: []
  scaleReference: ""
  materialNotes: []
approvedReferences:
  textReference: ""
  imagePaths: []
  externalLinks: []
continuityRules:
  mustRemainStable: []
  allowedChanges: []
  forbiddenDrift: []
variantRules:
  parentAssetId: ""
  variantCause: ""
  validAfterBeatId: ""
  replacesParentVisually: false
usage:
  allowedScenes: []
  allowedCharacters: []
  productionUse: false
review:
  reviewer: ""
  verdict: unreviewed
  notes: []
```

## 8. GPT Storyboard And Optional Video Cost Boundary

GPT / lower-cost image generation should be used for:

- rough reference exploration;
- first-pass three-view sheets;
- prop and weapon concept sheets;
- scene panorama drafts;
- storyboard frame drafts;
- drift testing before any optional downstream video generation.

Seedance / Oiioii should be used only as optional future downstream tools for:

- video generation after storyboard packets, assets, and scenes are approved;
- production tests using locked references;
- final or near-final motion output if that future milestone is explicitly approved.

Do not spend video-generation credits to discover what a character, scene, weapon, or prop looks like. Discover and review cheaply first, deliver storyboards first, then consider optional video later.

## 9. Drift Review Checklist

Before a generated image becomes a storyboard reference:

- Does it match the locked scene / asset id?
- Did it invent new clothes, props, symbols, materials, or modern objects?
- Did it change body type, fur pattern, weapon shape, or scene layout?
- Did it expose author-only secrets or future reveals?
- Does it match the required reference view?
- Is the asset usable across multiple angles?
- Does it need a variant id instead of replacing the original?
- Is it cheap draft material or expensive production output?

Before a packet uses an asset:

- Is the asset registered?
- Is the status high enough for the intended use?
- Is the intended use a draft storyboard, reviewed storyboard deliverable, or optional downstream video reference?
- Are the scene and asset allowed together?
- Is the packet changing the asset without a story cause?
- If damaged, wet, broken, bloodied, repaired, or aged, does a variant exist?

## 10. User-Identified Core Rule

After a generated character, scene, weapon, prop, or costume is accepted into the project, it should not be regenerated freely.

Only three kinds of change should be allowed:

1. Reference refinement before approval.
2. Story-caused variant after an explicit beat.
3. User / 01 approved redesign with migration notes.

This prevents GPT, Seedance, Oiioii, or any future visual tool from turning one asset into many accidental versions.

## 11. Additional Overlooked Points

These are risks that should be decided before implementation, because they can quietly break the pipeline.

### 11.1 Scale And Proportion

Assets need scale anchors. A dog, cart, door, weapon, and human must not change size between frames.

Future registry entries should include relative scale where useful:

- Ai-chan shoulder height versus protagonist knee / hip;
- patrol staff length versus human height;
- warehouse door height versus cart;
- cart wheel diameter versus boot.

### 11.2 Color And Material Palette

Consistency is not only shape. Materials and colors need boundaries:

- wet wood versus polished wood;
- iron hoop versus rubber tire;
- oil paper versus plastic sheet;
- cloth uniform versus synthetic fabric;
- mud, powder line, seal paper, medicine stains.

Without material locks, GPT may produce a visually coherent but era-breaking design.

### 11.3 Negative Prompt / Forbidden Detail Lists

Every production asset should include forbidden drift notes:

- no modern zippers;
- no rubber tires;
- no plastic buckets;
- no electric lights;
- no modern police uniform;
- no fantasy armor unless approved;
- no warehouse interior reveal if the scene forbids it.

Forbidden lists are as important as positive descriptions.

### 11.4 Versioning And Supersession

If an asset is redesigned, the registry needs a history:

- old asset id;
- new asset id;
- reason;
- whether prior storyboard frames must be regenerated;
- whether previous optional video outputs are invalidated.

Otherwise the project may silently mix old and new designs.

### 11.5 Ownership And Review Authority

The project should define who can approve an asset:

- 04 may propose;
- 05 may review;
- 06 may show to the user;
- user may approve;
- 01 may decide whether it becomes project-ready;
- 02 / 03 should only implement or verify repo changes when assigned.

No generated image should approve itself.

### 11.6 Storage And Naming

Future implementation must decide where references live:

- repo docs only;
- local ignored asset folder;
- tracked low-resolution reference sheets;
- external links;
- Seedance / video project library;
- GPT conversation artifacts.

Naming should be stable before many images exist. Otherwise references become impossible to audit.

### 11.7 Prompt Hygiene

Asset prompts should avoid leaking:

- author-only secrets;
- culprit identity;
- director goals;
- future reveal logic;
- hidden scene truth.

If a visual asset needs secrecy, the public prompt should describe only what the camera may show.

## 12. Relationship To Existing Documents

This planning document complements:

- `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`: packets should reference asset ids rather than redesigning assets in prose.
- `docs/SCENE_LOCK_AND_ANIMATION_ASSET_BOUNDARY.md`: scene lock controls spaces; asset registry controls reusable visual objects inside those spaces.
- `docs/STORYBOARD_AND_CAMERA_PLANNING.md`: storyboard frames should use registered assets and reference requirements before GPT storyboard pickup or any optional video generation.

## 13. Future Work Boundaries

Not implemented by this document:

- asset registry schema;
- asset database;
- image storage;
- reference image generation;
- Oiioii integration;
- Seedance integration;
- GPT browser automation;
- Validator checks;
- StateReducer changes;
- setting-pack fields;
- runtime scene / asset enforcement;
- formal production locks;
- generated image commits.

Future approved milestones may separately design:

- `docs/ASSET_REGISTRY_SPEC.md`;
- asset id naming convention;
- asset folder structure;
- reference image review checklist;
- GPT prompt templates for three-view / panorama generation;
- optional downstream video handoff prompt format;
- low-resolution tracked reference policy;
- ignored high-resolution output policy;
- read-only audit for asset drift.
