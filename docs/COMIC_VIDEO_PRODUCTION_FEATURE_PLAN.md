# Comic Video Production Feature Plan

Status: `DOCS_ONLY_PRODUCT_PLAN`

Date: 2026-06-25

This document defines the feature layer needed for the project to produce finished comic-style videos with audio, independent character voices, comic panels, storyboard timing, and strong visual continuity. It does not implement image generation, audio generation, video rendering, web servers, external AI APIs, SQLite migrations, or automation against any external tool.

Current local JSON workbench workflow: `docs/COMIC_VIDEO_WORKBENCH_CLI_WORKFLOW.md`.

## 1. Product Target

The target product is not just a novel and not just static manga pages.

The target is:

```text
accepted novel chapter
  -> script packet
  -> storyboard packet
  -> locked character / scene / prop references
  -> comic panel generation plan
  -> character voice casting plan
  -> timed comic video timeline
  -> rendered video with audio, subtitles, comic panels, and storyboard rhythm
```

Required final output:

- video file;
- full narration / dialogue audio;
- independent voice line per recurring character;
- comic-style panels on screen;
- storyboard timing and panel transitions;
- stable character identity across long sequences;
- stable scene, costume, prop, injury, and object states;
- review reports that explain what passed and what must be regenerated.

## 2. What "Guarantee" Means

The project cannot honestly guarantee that a generative image model will never drift by itself.

The project can guarantee production acceptance:

```text
Generate candidates
  -> compare against locked references
  -> reject drift
  -> regenerate or patch
  -> accept only if visual continuity passes
```

So the guarantee is an acceptance gate, not blind trust in the image model.

## 3. Required Feature Modules

### 3.1 Visual Asset Lock

Purpose: make characters, costumes, scenes, props, and variants stable.

Required draft files:

```text
assetId
assetType
assetStatus
canonicalDescription
requiredReferenceViews
approvedReferenceImages
mustRemainStable
allowedChanges
forbiddenDrift
variantRules
usageRules
reviewStatus
```

Character requirements:

- front / side / back reference;
- expression sheet;
- scale reference;
- costume lock;
- hair / eye / body / accessory lock;
- forbidden drift list.

Animal requirements:

- front / side / back reference;
- size relative to protagonist;
- markings / fur / ear / tail / posture lock;
- behavior notes.

Scene requirements:

- establishing panorama;
- zone map;
- camera-safe angles;
- lighting / material palette;
- allowed props;
- forbidden additions.

Prop / weapon / costume requirements:

- reference sheet;
- scale;
- material;
- damage variants;
- valid-after beat id.

### 3.2 Comic Panel Packet

Purpose: turn storyboard frames into comic panels that can be generated, reviewed, and sequenced.

Suggested fields:

```yaml
panelId: ""
pageId: ""
sceneId: ""
zoneId: ""
beatId: ""
panelPurpose: ""
panelSize: wide | medium | tall | insert | splash
camera:
  shotType: wide | medium | closeup | insert | overShoulder | pointOfView
  cameraPurpose: clarify_space | show_risk | guide_attention | show_emotion | show_evidence_detail
characters:
  - assetId: ""
    variantId: ""
    pose: ""
    expression: ""
    blocking: foreground | midground | background
    facing: left | right | camera | away
dialogue:
  speakerId: ""
  text: ""
  voiceId: ""
narration: ""
mustShow: []
mustNotShow: []
continuityAnchors: []
assetRefs: []
promptContract:
  positivePrompt: ""
  negativePrompt: ""
  referenceImages: []
review:
  status: draft
  driftFindings: []
```

The panel packet is the bridge between script/storyboard and image generation.

### 3.3 Long-Form Comic Layout Engine

Purpose: avoid random panel composition and make long chapters readable.

Supported layout targets should be explicit:

- 16:9 comic video frames;
- vertical webtoon scroll;
- manga page;
- storyboard board.

For the user's current target, first priority should be 16:9 comic video frames.

Layout rules:

- one beat may become one or more panels;
- action beats use wider panels;
- emotion beats may use medium or closeup panels;
- evidence/detail beats use insert panels;
- dialogue-heavy scenes need alternating speaker panels and reaction panels;
- avoid more than 2 major text balloons per panel;
- every panel should map to a timed audio segment.

### 3.4 Character Voice Casting

Purpose: each recurring character has an independent voice line.

The standalone `小說朗讀` sidecar already has useful evidence:

- `speakerVoices`;
- `speakerProsody`;
- `voicePool`;
- subtitle settings;
- MP4 rendering.

The core project should output a voice casting plan:

```yaml
voiceCast:
  - characterId: ""
    displayName: ""
    voiceId: ""
    rate: 1.0
    pitch: "+0Hz"
    volume: 100
    role: narrator | protagonist | recurringNpc | minorNpc | companion
    continuityNotes: []
```

Rules:

- narrator has a stable voice;
- protagonist has a stable voice if direct dialogue is voiced;
- recurring NPCs keep their assigned voice;
- minor NPCs may use a controlled pool;
- voice changes must be story-caused and recorded as variants.

### 3.5 Timed Comic Video Timeline

Purpose: bind panel images, dialogue/narration audio, subtitles, and transitions into a renderable timeline.

Suggested fields:

```yaml
timelineId: ""
video:
  width: 1920
  height: 1080
  fps: 30
  style: comic_video
tracks:
  panels:
    - panelId: ""
      imagePath: ""
      startSec: 0.0
      endSec: 0.0
      transitionIn: cut | fade | pan | zoom
      motion: none | slow_push | slow_pan | shake
  audio:
    - segmentId: ""
      speakerId: ""
      voiceId: ""
      audioPath: ""
      startSec: 0.0
      endSec: 0.0
  subtitles:
    - segmentId: ""
      text: ""
      startSec: 0.0
      endSec: 0.0
      speakerLabel: ""
  soundEffects:
    - effectId: ""
      startSec: 0.0
      audioPath: ""
review:
  syncStatus: draft
  notes: []
```

The current `小說朗讀` sidecar can render audio/subtitles/video, but it currently uses a solid-color background. A later sidecar milestone should teach it to consume panel images and timeline tracks.

## 4. Consistency Gates

### Gate 1: Asset Reference Completeness

Fail if:

- recurring character has no approved reference sheet;
- recurring scene has no establishing view or zone map;
- important prop has no reference;
- costume variant lacks cause and valid-after beat.

### Gate 2: Panel Asset Compatibility

Fail if:

- panel references an unknown asset id;
- panel uses candidate-only asset in production context;
- character variant appears before its valid-after beat;
- scene panel adds unapproved room, costume, prop, weather, or lighting.

### Gate 3: Cross-Panel Continuity

Fail if:

- character hair/clothing/accessories change without variant;
- wound/damage state disappears;
- prop changes hand/location without beat;
- camera jumps to impossible space;
- scene geography contradicts zone map.

### Gate 4: Voice Continuity

Fail if:

- recurring character changes voice without recorded variant;
- narrator voice changes mid-chapter;
- dialogue speaker cannot be mapped to voice;
- subtitles and audio segment timing are mismatched.

### Gate 5: Motion Continuity

For animation-ready output, fail if:

- action direction reverses without beat;
- pose sequence skips required contact / impact / recovery state;
- moving prop appears/disappears between panels;
- panel timing cannot support the audio duration.

## 5. Action Continuity For Animation

Do not jump directly from static comic panels to full animation.

Use a staged ladder:

```text
static panel
  -> panel with camera push/pan
  -> pose sequence
  -> keyframe strip
  -> short animated shot
  -> optional downstream video generation
```

For each action beat, record:

```yaml
actionContinuity:
  actionId: ""
  actorId: ""
  startPose: ""
  contactPose: ""
  endPose: ""
  propStateBefore: []
  propStateAfter: []
  directionOfMotion: ""
  cameraMotion: ""
```

This gives animation tools a structure to follow and gives reviewers something concrete to inspect.

## 6. End-To-End Pipeline

Recommended pipeline:

```text
accepted chapter
  -> chapter ledger
  -> writer brief
  -> script packet
  -> storyboard packet
  -> asset need extraction
  -> asset reference generation / review
  -> comic panel packet
  -> panel image candidates
  -> visual continuity audit
  -> voice cast plan
  -> audio segments
  -> timed comic video timeline
  -> video render sidecar
  -> human review
```

No stage should skip review into final output.

## 7. First Implementation Order

Do not start with image generation.

Start with data contracts and validators:

1. `create-comic-video-packet-template`
   - creates empty packet with panel, asset, voice, and timeline sections.
2. `validate-comic-video-packet`
   - checks required fields and cross references.
3. `validate-visual-asset-lock`
   - checks reference requirements and variants.
4. `audit-panel-continuity`
   - checks panel-to-panel metadata continuity.
5. Sidecar milestone in `小說朗讀`
   - consume panel image timeline instead of solid background.

Only after this should Codex generate actual comic panel images.

## 8. Minimum Viable Finished Product

First MVP target:

```text
one accepted chapter
  -> 12 to 20 comic panels
  -> narrator voice
  -> 2 to 3 recurring character voices
  -> subtitles
  -> 16:9 MP4
  -> simple transitions and camera push/pan
```

This is not full animation yet, but it is a real comic video product.

Pass criteria:

- no unknown character voice;
- no missing panel image;
- no unregistered recurring character design;
- no unapproved scene drift;
- no broken subtitle/audio sync;
- no panel sequence that contradicts the script packet;
- exported MP4 exists and can be replayed.

## 9. What This Adds Beyond Existing Blueprints

Existing docs already cover:

- scene-locked storyboard planning;
- asset registry planning;
- script packet structure;
- optional narrated video sidecar.

This plan adds:

- comic panel packet layer;
- 16:9 comic video layout target;
- voice cast continuity;
- timed panel/audio/subtitle timeline;
- cross-panel visual continuity gates;
- staged path from still panels to animation-ready keyframes.

## 10. Product Promise

The project should promise this:

> We do not trust a single generated image or voice pass. We build a locked production packet, generate candidates, audit drift, and accept only what stays consistent.

That is how the project can produce comic videos with reliable characters, voices, panels, and story continuity.
