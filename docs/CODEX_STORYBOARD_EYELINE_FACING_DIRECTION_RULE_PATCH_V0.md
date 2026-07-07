# Codex Storyboard Eyeline Facing Direction Rule Patch V0

Task: `storyboard_eyeline_facing_direction_rule_patch_v0_docs_control`

Status: `DRAFT_FOR_REVIEW`

Selected outcome:
`STORYBOARD_EYELINE_FACING_DIRECTION_RULE_PATCH_ALIGNED`

This docs/control rule patch makes character eyeline and facing direction a
required future storyboard/image-facing planning surface. It is not a Kotlin/JVM
model change, schema change, runtime validator, sample JSON, generated prompt
payload, storyboard instance, image generation, approval, release, or
production lock.

## Eyeline / Facing Direction Rule

Every future storyboard or image-facing frame that includes dialogue, reaction,
attention, avoidance, threat monitoring, companion interaction, or
interpersonal blocking must carry explicit frame-level eyeline and facing
direction fields:

| Field | Required when | Purpose |
| --- | --- | --- |
| `gazeTarget` | a character looks at, tracks, avoids, or monitors someone or something | states who or what the character is looking at |
| `facingTarget` | a character's face or body orientation matters to the beat | states where the face/body is oriented |
| `eyelineReason` | any gaze or facing choice is declared | explains why that gaze/facing choice is correct for the beat |
| `nonFacingReason` | a character speaks or reacts while not facing the interaction target | records the intentional non-facing reason |
| `interactionTarget` | dialogue or directed action appears | states the person, creature, object, threat, sound, scent, or zone being addressed |
| `continuityCheck` | every such frame | confirms gaze/facing matches prior blocking unless intentionally changed |

The fields are docs/control requirements for future packet planning. They do
not create actual `StoryboardPacketFrameDraft` or `ImageFacingDeliveryFrameCard`
runtime fields in this slice.

## Rule Examples

- If 陸知行 speaks to 愛醬, default `gazeTarget`, `facingTarget`, and
  `interactionTarget` should target 愛醬.
- If 陸知行 does not look at 愛醬 while speaking, `nonFacingReason` is required.
  Acceptable reason categories include fear, concealment, watching danger,
  shame, deliberate coldness, tactical attention, or another beat-specific
  intentional non-facing explanation.
- If 愛醬 reacts to 陸知行, the frame must state whether she looks at him, turns
  an ear/body toward him, ignores him deliberately, watches a threat, or tracks
  scent/sound.
- If a character watches a threat while speaking to a companion, the frame must
  separate `interactionTarget` from `gazeTarget` and explain the split in
  `eyelineReason`.

Chenxing guards for examples:

- Current-facing protagonist name is only `陸知行`; `衛昭` is superseded in
  derived/current-facing materials.
- 愛醬 has her own story line and must not be reduced to pet, tool, clue
  machine, mascot, combat asset, or mount-only growth.
- Living world first; final-product chains serve the world later.
- Old opening/mainline material remains discussion evidence only, not locked
  plot, canon, or final-product content.

## Future Review Checks

Future B03/05/08-style validation should check:

- dialogue frames without `gazeTarget`, `facingTarget`, and
  `interactionTarget` are incomplete;
- reaction, attention, avoidance, threat-monitoring, companion-interaction, or
  interpersonal-blocking frames without `eyelineReason` are incomplete;
- a frame where speech or reaction happens while not facing the target requires
  `nonFacingReason`;
- contradictory gaze/facing versus `visibleAction` is a drift risk;
- accidental back-facing is a fail unless the non-facing choice is explicitly
  intentional;
- `continuityCheck` must confirm the gaze/facing matches prior blocking unless
  intentionally changed;
- gaze/facing must not reveal author-only information or
  character-inaccessible knowledge;
- gaze/facing notes must not smuggle hidden reasoning into prompt-ready fields
  or user-facing story text.

## Final-Product Chain Links

This rule reduces uncertainty in these final-product chains:

- storyboard/camera: frame blocking becomes reviewable instead of implicit;
- prompt/asset cards: future prompts can request direction only after reviewed
  fields exist and no prompt leakage is present;
- image-facing delivery proof: image review can detect whether gaze/facing
  matches the packet;
- renderer/package QA: downstream QA can reject accidental facing drift;
- human approval gates: reviewers can approve or reject visible interpersonal
  staging with clear evidence.

## Boundaries

- no provider/API.
- no SQLite.
- no image generation.
- no browser, web/server, renderer, ffmpeg, TTS, or media generation.
- no direct SQLite writes.
- no sample JSON or generated prompt payload.
- no actual `ScriptReadyScenePacket`, `StoryboardPacket`, `ImageCandidate`,
  storyboard frame, image-facing delivery card, or runtime object.
- no Kotlin/JVM model, schema, service, validator, migration, or test change.
- no canon, registry, release, final approval, or production lock.

This patch is non-approval. It records a future storyboard/image-facing
requirement only.
