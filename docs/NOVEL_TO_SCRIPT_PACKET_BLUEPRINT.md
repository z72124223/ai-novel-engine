# Novel To Script Packet Blueprint

Status: `DOCS_ONLY_TRANSFORMATION_BLUEPRINT`

Date: 2026-06-24

This document defines how prose, novel chapters, or turn narratives can be transformed into script-ready scene packets and later storyboard frame plans. It does not implement Kotlin source, tests, schema, migrations, Validator behavior, StateReducer behavior, `resolve-turn`, `stateVersion`, `contextHash`, CLI commands, SQLite data, setting-pack conversion, queryers, GPT automation, image generation, Seedance, Oiioii, video generation, storyboard generation, or 04 / 05 / 06 / 07 / 08 work.

## 1. Verdict

The project should add a transformation layer between prose and production packets:

```text
novel prose / chapter / turn narrative
  -> prose analysis notes
  -> scene segmentation
  -> beat extraction
  -> script-ready scene packet
  -> storyboard frame plan
  -> reviewable storyboard packet
```

This layer should make prose usable for review, scene locking, asset planning, and future storyboard images without turning every sentence into a camera shot or leaking author-only causality.

The transformation result is a draft packet only. It is not canon, not a setting pack, not runtime data, not an AI result JSON, not a final script, and not a storyboard deliverable.

## 2. Existing Anchors

This blueprint sits between existing docs:

- `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`: target packet shape for 05 review, 06 display, 07 asset planning, and 08 storyboard pickup.
- `docs/STORYBOARD_AND_CAMERA_PLANNING.md`: future storyboard frame child layer under approved or candidate scene packets.
- `docs/SIMULATION_FIRST_KNOWLEDGE_TRUTH_RULES.md`: world causality and actor knowledge guardrails.
- `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md`: asset-lock rules for reusable characters, animals, scenes, props, costumes, weapons, and variants.
- `docs/BLUEPRINT_REDESIGN_DECISION_BRIEF.md`: product spine from low-friction input to reviewable storyboard packet.

## 3. Input Types

Supported input types for future conversion planning:

| Input | Meaning | Risk |
| --- | --- | --- |
| `novelChapter` | A prose chapter or chapter slice. | May contain interiority, summary, time jumps, and author-only hints. |
| `turnNarrative` | Narrative output from a validated turn. | May be too small or too context-dependent for a full scene packet. |
| `authorOutline` | Human-written outline or rough scene notes. | May omit visible action and knowledge boundaries. |
| `mixedDraft` | Prose plus notes, dialogue, or partial beats. | May mix user-facing and author-only material. |

Unsupported as direct input:

- full database dumps;
- full setting packs;
- full world bible dumps;
- author-only causality tables without scene context;
- unreviewed blueprint pool entries as if they were canon.

## 4. Output Target

The target is a `script-ready scene packet` compatible with `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`.

Minimum output fields:

- `packetTitle`
- `packetStatus`
- `sourceContext`
- `sceneReference`
- `scenePurpose`
- `playerObjective`
- `locationZone`
- `visualSetup`
- `charactersPresent`
- `characterState`
- `approvedProps`
- `forbiddenProps`
- `beatList`
- `actionLines`
- `dialogueLines`
- `interactionChoices`
- `stateConstraints`
- `continuityChecks`
- `storyboardNotes`
- `secrecyKnowledgeNotes`
- `reviewSummary`

Default status:

```yaml
packetStatus: draft
reviewSummary:
  verdict: unreviewed
```

## 5. Transformation Stages

### 5.1 Intake Classification

First classify the input:

```yaml
inputType: novelChapter | turnNarrative | authorOutline | mixedDraft
sourceId: ""
sourceRange: ""
knownCanonStatus: canon | non_canon | draft | unknown
intendedUse: scene_packet | storyboard_plan | review_only
```

Rules:

- If canon status is unknown, keep the output non-canon.
- If input contains author-only notes, they must be separated before user-facing fields are drafted.
- If the input depends on unapproved scenes or assets, label those as candidates.

### 5.2 Scene Segmentation

Split prose by production scene, not by paragraph count.

A new segment begins when one of these changes:

- location changes;
- scene zone changes enough to require new blocking;
- time jumps;
- active cast changes materially;
- player objective changes;
- causal pressure changes;
- a new visual asset or location would be required.

Do not split only because:

- a paragraph break appears;
- dialogue starts;
- the camera could theoretically change;
- the prose shifts from action to interior thought.

Segmentation output:

```yaml
segments:
  - segmentId: ""
    sourceRange: ""
    sceneCandidateReason: ""
    likelySceneId: ""
    locationZone: ""
    activeCast: []
    objective: ""
    pressure: ""
    needsSeparatePacket: true
```

### 5.3 Perspective And Knowledge Pass

For each segment, identify:

- viewpoint actor;
- what the actor observes;
- what the actor infers;
- what the actor is told;
- what remains author-only;
- what the prose implies but the actor cannot know.

Output:

```yaml
knowledgePass:
  viewpointActor: ""
  observableFacts: []
  actorInferences: []
  toldInformation: []
  authorOnlyExtracted: []
  unsafeUserFacingLines: []
  evidencePaths: []
```

Rule: prose narration may know more than the character. The packet must not carry that extra knowledge into `dialogueLines`, `playerObjective`, `interactionChoices`, or user-facing `beatList`.

### 5.4 Beat Extraction

Extract beats by action / pressure / choice, not by sentence.

A beat should answer:

- what changes visibly;
- who acts;
- why the action follows from the previous beat;
- what pressure or option it creates;
- what can be reviewed later.

Beat shape:

```yaml
beatList:
  - beatId: ""
    purpose: ""
    visibleAction: ""
    causalLink: ""
    actorKnowledgeLimit: ""
    productionRisk: ""
```

Avoid:

- summary-only beats with no visible action;
- beats that exist only to reveal a secret;
- beats that convert internal thought directly into camera action;
- beats that require unapproved assets or scenes without marking candidates.

### 5.5 Action Line Conversion

Convert prose into concise, producible action lines.

Good action line:

```yaml
actor: "protagonist"
line: "Stops at the threshold and checks the floor before stepping in."
blockingZone: "threshold"
```

Bad action line:

```yaml
actor: "protagonist"
line: "Realizes destiny is pulling him toward the hidden truth."
blockingZone: ""
```

Action lines should:

- be visible or audibly playable;
- name actor and blocking zone;
- avoid hidden motivation as if it were visible motion;
- avoid camera-only invention;
- preserve material culture and scene lock.

### 5.6 Dialogue Line Conversion

Dialogue lines should retain speakable lines, remove exposition dumps, and preserve knowledge boundaries.

Shape:

```yaml
dialogueLines:
  - speaker: ""
    line: ""
    knowledgeBoundary: ""
    sourceRange: ""
```

Rules:

- If a line explains world causality the speaker cannot know, move it to review-only notes.
- If a line is interior thought, convert only if the future format explicitly supports voiceover or inner monologue.
- If the line is only prose summary, do not invent dialogue unless the future task asks for adaptation.

### 5.7 Interiority Handling

Novel prose often contains interiority. Do not map all interiority to dialogue.

Allowed destinations:

| Prose interiority | Destination |
| --- | --- |
| Immediate fear, hesitation, decision | `characterState` and sometimes visible `actionLines`. |
| Tactical reasoning based on observed facts | `characterState`, `beatList`, or `interactionChoices`. |
| Author-only explanation | `secrecyKnowledgeNotes.authorOnlyExtracted`. |
| Long reflective summary | Usually omit or summarize as review-only context. |
| Emotional state visible through behavior | Convert to visible action, posture, pause, or choice pressure. |

Do not convert hidden thoughts into storyboard-visible facts unless the future format explicitly includes narration.

### 5.8 Asset And Scene Candidate Pass

Identify every reusable visual requirement:

- characters;
- animals / companions;
- scenes;
- zones;
- props;
- weapons;
- costumes;
- documents;
- signs;
- vehicles;
- material variants;
- damage / wet / dirty / injured states.

Output:

```yaml
assetNeeds:
  - assetType: ""
    label: ""
    existingAssetId: ""
    candidateReason: ""
    requiredReferenceViews: []
    forbiddenDrift: []
```

Rules:

- If no approved asset exists, do not invent an approved id.
- If the prose changes an asset state, propose a variant need.
- If a new room, road, interior, or landmark appears, mark `sceneCandidate`.

### 5.9 Storyboard Preparation Pass

After scene packet extraction, create optional storyboard notes only when useful.

Do not produce a shot list for every beat by default. Instead identify frame candidates:

```yaml
storyboardNotes:
  frameCandidates:
    - beatId: ""
      framePurpose: ""
      shotType: establishing | wide | medium | closeup | insert | overShoulder | pointOfView
      cameraPurpose: clarify_space | show_risk | guide_attention | hide_information | show_evidence_detail | neutral_coverage
      visibleSubject: ""
      doNotShow: []
      assetReferencesNeeded: []
      reviewRisk: ""
```

Rules:

- Most beats do not need unique frames.
- Camera notes should solve clarity, risk, evidence, blocking, or secrecy problems.
- Avoid generic dramatic camera language.
- Never put author-only causality into image prompts.

## 6. Field Mapping Guide

| Prose Source | Packet Destination |
| --- | --- |
| Scene title or chapter heading | `packetTitle` |
| Location description | `sceneReference`, `locationZone`, `visualSetup` |
| Viewpoint character | `charactersPresent`, `characterState` |
| Immediate goal | `playerObjective` |
| External pressure | `scenePurpose`, `beatList`, `stateConstraints` |
| Visible actions | `actionLines` |
| Spoken dialogue | `dialogueLines` |
| Meaningful options | `interactionChoices` |
| Props / materials | `approvedProps`, `assetNeeds`, `forbiddenProps` |
| Internal thought | `characterState`, review-only notes, or visible behavior |
| Author-only explanation | `secrecyKnowledgeNotes` |
| Continuity references | `continuityChecks` |
| Camera-worthy visual beat | `storyboardNotes.frameCandidates` |

## 7. Review Checklist

Before a transformed packet can go to 05:

- Does every packet segment have one scene or a labeled `sceneCandidate`?
- Are user-facing fields free of author-only causality?
- Are actor knowledge limits explicit?
- Are action lines visible, playable, and attached to blocking?
- Are dialogue lines speakable and role-bound?
- Are interior thoughts handled without forced voiceover?
- Are new assets marked as candidates?
- Are forbidden materials, props, and modern objects named when relevant?
- Are storyboard notes restrained and useful?
- Is the packet still marked `draft` and `unreviewed`?

## 8. Minimal Transformation Template

```yaml
source:
  inputType: novelChapter
  sourceId: ""
  sourceRange: ""
  canonStatus: draft
  transformationStatus: draft
segments:
  - segmentId: ""
    sourceRange: ""
    sceneReference:
      type: approvedScene | prologueBridge | sceneCandidate
      sceneId: ""
      candidateId: ""
    scenePurpose: ""
    playerObjective: ""
    locationZone:
      sceneId: ""
      zoneId: ""
      blocking: ""
    visualSetup:
      publicDescription: ""
      visualAnchors: []
      lightingWeatherMaterials: []
    knowledgePass:
      viewpointActor: ""
      observableFacts: []
      actorInferences: []
      toldInformation: []
      authorOnlyExtracted: []
      evidencePaths: []
    beatList:
      - beatId: ""
        purpose: ""
        visibleAction: ""
        causalLink: ""
        actorKnowledgeLimit: ""
        productionRisk: ""
    actionLines:
      - actor: ""
        line: ""
        blockingZone: ""
    dialogueLines:
      - speaker: ""
        line: ""
        knowledgeBoundary: ""
        sourceRange: ""
    interactionChoices:
      - choiceId: ""
        label: ""
        allowedBecause: ""
    assetNeeds: []
    continuityChecks: []
    stateConstraints: []
    storyboardNotes:
      frameCandidates: []
    secrecyKnowledgeNotes:
      authorOnlyExcluded: true
      notes: []
    reviewSummary:
      verdict: unreviewed
      requiredFixes: []
```

## 9. Future CLI Shape

If later approved, a future CLI could be planned as read-only first:

```text
draft-script-packet --source <prose.json|txt> --out <packet-draft.json>
validate-script-packet-draft --packet <packet-draft.json> --out <review.json>
```

But this document does not approve those commands.

Future implementation would need:

- explicit input file format;
- source range tracking;
- JSON schema or Kotlin model;
- tests for author-only separation;
- tests for sceneCandidate handling;
- tests for action/dialogue extraction;
- tests for storyboard note safety;
- no SQLite writes unless a later milestone explicitly approves a write path.

## 10. What This Enables Later

This blueprint enables a future workflow:

```text
author writes messy prose
  -> 02 or future CLI drafts packet structure
  -> 05 reviews packet safety and producibility
  -> 06 displays user-facing parts for approval
  -> 07 prepares asset candidates
  -> 08 creates / verifies storyboard image drafts
  -> user reviews storyboard packet
  -> optional future animation handoff only after separate approval
```

This is the local, auditable version of low-friction AI-assisted story creation. It favors traceable transformation over opaque generation.

## 11. Boundaries

This blueprint does not:

- convert any current novel text;
- create canon story output;
- approve a setting pack;
- create or modify SQLite data;
- implement a converter;
- create a CLI command;
- call any AI API;
- generate storyboard images;
- approve 04 / 05 / 06 / 07 / 08 restart;
- approve Seedance, Oiioii, or video generation;
- approve Candidate B or runtime quality gates.

## 12. Next Safe Step

Recommended next docs-only step, if selected later:

```text
SCRIPT_PACKET_DRAFT_TEMPLATE_SPEC
```

Scope:

- decide whether the transformation template should be YAML, JSON, or Markdown;
- define source range notation;
- define required review warnings;
- include one neutral invented sample that cannot be mistaken for project canon.

Alternative implementation-oriented step, only after explicit approval:

```text
READ_ONLY_SCRIPT_PACKET_DRAFT_VALIDATOR
```

Scope:

- validate a draft packet file;
- report missing fields and unsafe author-only leakage;
- write a report JSON only;
- no SQLite writes.

## 13. 03 Review Focus

03 should verify:

- the document fills the gap between prose and `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`;
- output stays draft / non-canon / unreviewed by default;
- author-only causality is separated from user-facing fields;
- action, dialogue, interiority, assets, and storyboard notes are handled separately;
- no runtime, schema, Validator, StateReducer, SQLite, setting-pack, queryer implementation, asset registry implementation, GPT automation, image generation, Seedance, Oiioii, video generation, Candidate B, MVP-8, or 04 / 05 / 06 / 07 / 08 activation is implied.
