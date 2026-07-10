# Codex / GPT Image Production Blueprint V1 — Grok Video Addendum

Status: `BINDING_FUTURE_ROUTE_ADDENDUM_NOT_ACTIVE_IMPLEMENTATION`

Date: 2026-07-10

Parent blueprint:

- `docs/CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1.md`
- `docs/CODEX_GPT_IMAGE_PRODUCTION_EXECUTION_PLAN_V1.md`

Current-entry gate:

- `PROJECT_STATE.md`
- `docs/CURRENT_ROUTE_GATE_V1.md`

## 1. Purpose

This addendum reserves a future path in which approved comic/storyboard keyframes become controlled inputs for Grok video generation.

It does not start video production now.

Current priority remains:

```text
WP-003 visual Bible inventory
→ WP-004 real GPT image handoff
→ WP-005 candidate image generation and user review
→ WP-006 Codex intake, SHA-256, review, and Candidate Reference validation
```

Only after approved visual references and keyframes exist may the controller activate the short-video route.

## 2. Why The Comic / Storyboard Is Useful

The comic/storyboard provides:

- reviewed story order;
- one stable shot identity per frame;
- approved character, costume, prop, and location bindings;
- camera and composition intent;
- continuity between adjacent shots;
- a user-reviewable visual target before motion is added.

The production video input is not the final printed comic page.

A formal video source frame should be:

- one clean shot;
- no speech bubble;
- no subtitle burn-in;
- no page number;
- no panel border;
- no multi-panel layout;
- final target aspect ratio already selected;
- exact approved image file and SHA-256 recorded.

Dialogue, narration, subtitles, sound effects, and timing remain separate data.

## 3. Provider-Neutral First

Do not make the core story or shot data depend on Grok-specific fields.

The future chain should be:

```text
Storyboard / Shot Packet
→ Approved Keyframe
→ provider-neutral VideoShotHandoff
→ provider profile
→ manual provider generation
→ result intake and review
```

A different video provider should be replaceable without rewriting the story, visual Bible, or Shot Packet.

## 4. Current Grok Capability Snapshot

At the time of this addendum, official xAI documentation describes these video workflows:

- text-to-video;
- image-to-video;
- reference-to-video;
- video editing;
- video extension.

These provider capabilities, model names, limits, resolutions, reference counts, prices, and API shapes are time-sensitive. Before any real Grok Pilot, Codex or GPT must re-check the current official xAI documentation and record a dated provider profile.

Do not hardcode a changing provider limit into the provider-neutral core schema.

## 5. Mode Selection Policy

### 5.1 `IMAGE_TO_VIDEO` — formal default

Use when an approved clean keyframe exists.

The source keyframe controls:

- first-frame composition;
- character appearance;
- costume;
- visible props;
- location geometry;
- weather and lighting;
- scale and placement.

The motion request should add only the approved movement. It must not redesign the image.

### 5.2 `REFERENCE_TO_VIDEO` — exploration or bounded fallback

Use when multiple approved references are needed but no approved first frame exists.

It is suitable for:

- controlled motion exploration;
- checking whether a provider can preserve a character/reference set;
- low-risk visual experiments.

It is not the default formal-shot path because the provider is freer to choose the opening composition.

### 5.3 `EDIT_VIDEO` — bounded repair

Use only when the existing motion is acceptable and one bounded visual issue may be repairable.

Examples:

- rain intensity;
- lighting tone;
- a removable extra object;
- a minor costume-color error.

Do not repeatedly edit a clip that has major face, body, animal-marking, geometry, or continuity drift. Regenerate the single shot from the approved keyframe instead.

### 5.4 `EXTEND_VIDEO` — one bounded continuation

Use only when continuation from the existing last frame is narratively and visually justified.

Default project policy:

- at most one bounded extension before a new approved keyframe is required;
- do not create a long scene by repeatedly extending a drifting clip;
- the extension must preserve the same shot identity or explicitly create a new shot.

### 5.5 `TEXT_TO_VIDEO` — rough concept only

Text-only video may be used for atmosphere or motion research when no locked character or recurring location must remain exact.

It must not be treated as a formal character-consistent Chenxing shot.

## 6. Future `VideoShotHandoff` Minimum Contract

A future provider-neutral contract should support:

```text
schemaVersion
videoHandoffId
status
projectId
worldId
sourceCommitSha
sourceShotPacket
shotId
sceneId
locationId
storyPhase
sourceKeyframePath
sourceKeyframeSha256
sourceKeyframeApprovalEvidence
mode
referenceImages
referenceImageHashes
durationIntentSec
aspectRatio
resolutionIntent
motionPrompt
cameraMotion
allowedCharacterMotion
allowedAnimalMotion
allowedEnvironmentMotion
forbiddenDrift
continuityFrom
continuityTo
firstFrameContract
lastFrameContract
audioTimelineRef
subtitleTimelineRef
expectedOutputFileName
providerProfileRef
humanReviewRequired
returnToCodexContract
```

The final schema must be created only when the route is activated and after the real image Pilot exposes practical requirements.

## 7. Motion Budget

The first Pilot should prefer 3–8 second shots.

Early allowed motion:

- breathing;
- blinking;
- small head turns;
- ear movement for Aijiang;
- subtle hand or finger movement;
- fabric and fur response to wind;
- rain, fog, spray, firelight, or water movement;
- slow camera push, pan, tilt, or restrained handheld motion.

Early forbidden or high-risk motion:

- long continuous fight choreography;
- full-body transformation;
- rapid costume changes;
- complex multi-character crossing;
- unrestricted camera orbit through unbuilt geometry;
- large unseen-room reveals;
- motion that requires the model to invent the back side of an unapproved asset.

## 8. Character And Location Locks

A Grok video request must bind the same identifiers used by the image route:

- character ID;
- appearance version;
- costume version;
- Aijiang appearance version;
- scene/location version;
- spatial-anchor version;
- prop version;
- source keyframe identity;
- continuity predecessor.

The prompt may describe motion. It must not restate or reinvent the full design when approved files already exist.

## 9. One Mode Per Provider Request

The provider profile must validate one request mode at a time.

Do not assume a provider request can simultaneously use every available input mechanism. For example, a provider may require choosing between a source image and a separate reference-image set.

The handoff compiler should select a supported mode rather than silently dropping references.

## 10. Review States

Every generated clip remains a candidate.

Allowed user states:

```text
PENDING_USER_REVIEW
APPROVE_VIDEO_CANDIDATE
REJECT_VIDEO_DRIFT
REQUEST_VIDEO_VARIATION
REQUEST_SHOT_REBUILD
CHANGE_MOTION_DIRECTION
```

Minimum drift codes:

```text
FACE_TEMPORAL_DRIFT
BODY_TEMPORAL_DRIFT
AGE_TEMPORAL_DRIFT
GLASSES_OR_HAIR_TEMPORAL_DRIFT
COSTUME_TEMPORAL_DRIFT
AIJIANG_MARKING_TEMPORAL_DRIFT
SCALE_TEMPORAL_DRIFT
SCENE_GEOMETRY_TEMPORAL_DRIFT
PROP_APPEAR_DISAPPEAR_DRIFT
WEATHER_OR_LIGHTING_TEMPORAL_DRIFT
CAMERA_PATH_DRIFT
MOTION_INTENT_DRIFT
SHOT_CONTINUITY_DRIFT
EXTRA_CHARACTER_OR_OBJECT
FRAME_WARP_OR_ANATOMY_FAILURE
OTHER
```

Technical completion, a downloadable URL, or provider success never equals user approval.

## 11. Return To Codex

For every retained clip, Codex should later record:

- exact local file path;
- SHA-256;
- byte size;
- media metadata;
- source keyframe SHA-256;
- provider profile and dated capability snapshot;
- prompt and mode;
- user decision;
- drift findings;
- continuity decision;
- whether regeneration, edit, extension, or acceptance is next.

Large video binaries remain outside ordinary Git by default. Git stores the manifest, Hash, review evidence, and controlled storage pointer unless Git LFS or another storage policy is separately approved.

## 12. Activation Gates

The Grok video route must not activate until all are true:

1. WP-003 identifies the formal character/location reference set and gaps.
2. WP-004 creates a real image handoff.
3. WP-005 produces candidate images with required references attached.
4. The user explicitly approves at least one clean shot keyframe.
5. WP-006 verifies the exact file and Candidate Reference lineage.
6. Adjacent-shot continuity is defined.
7. A provider-neutral VideoShotHandoff package is approved.
8. The user explicitly authorizes a manual video Pilot and any provider cost.

## 13. Future Work Package Split

When activated, prefer this bounded sequence:

### WP-007A — Provider-Neutral Video Handoff Contract

- define schema and one example-only package;
- no provider call;
- no API key;
- no generated clip.

### WP-007B — Manual Grok Short-Video Pilot

- one approved clean keyframe;
- one 3–8 second shot;
- manual provider action;
- candidate-only result;
- user review.

### WP-007C — Video Intake And Continuity Evidence

- Codex records Hash, metadata, decision, and continuity;
- no automatic final-media or release claim.

### WP-007D — Optional Automation Decision

Only after the manual Pilot succeeds may the project decide whether a provider adapter is worth implementing.

## 14. Non-Goals

This addendum does not authorize:

- xAI API integration now;
- storing an API key in Git;
- provider spending without explicit user approval;
- background generation;
- whole-episode generation;
- long-video generation from raw prose;
- automatic image or video approval;
- video-driven Canon changes;
- changing the active WP-003 → WP-004 → WP-005 → WP-006 image route;
- another large family of status-only planning documents.

## 15. Definition Of Future Success

The future Grok path succeeds when:

- one approved clean keyframe becomes one reviewable short clip;
- the same character, costume, Aijiang markings, props, and scene geometry persist through the clip;
- the requested motion and camera move are recognizable;
- a failed clip can be rejected and regenerated without rebuilding the chapter;
- Codex can identify the exact source, prompt, provider profile, output file, Hash, and user decision;
- the provider can later be changed without rewriting the story or visual Bible.