# Codex Chenxing Scene Packet Visual Continuity Requirement Bridge V0

Task: `chenxing_scene_packet_visual_continuity_requirement_bridge_v0_docs_control`

Status: `DRAFT_FOR_REVIEW`

Selected visual-continuity bridge outcome:
`CHENXING_SCENE_PACKET_VISUAL_CONTINUITY_REQUIREMENT_BRIDGE_READY_FOR_REVIEW`

This docs/control bridge consumes the B03-PASSed storyboard eyeline/facing rule
as reviewed rule input for future Chenxing scene-packet and storyboard
planning. It is a requirement bridge only. It creates no actual
ScriptReadyScenePacket, no storyboard frames, no prompt cards, no image
generation, no provider/API, no renderer, no ffmpeg, no TTS, no media, no
source/story invention, no concrete refs, no sample JSON, no query instances,
no evidence bundle, no Kotlin/schema/runtime change, no canon, no registry, no
release, no final approval, and no production lock.

## Reviewed Input

Reviewed input:

- `docs/CODEX_STORYBOARD_EYELINE_FACING_DIRECTION_RULE_PATCH_V0.md`
- selected reviewed outcome:
  `STORYBOARD_EYELINE_FACING_DIRECTION_RULE_PATCH_ALIGNED`
- downstream Chenxing planning surfaces:
  `docs/CODEX_CHENXING_09_SCENE_PACKET_CANDIDATE_SKELETON_V0.md` and
  `docs/CODEX_CHENXING_09_SCENE_PACKET_SOURCE_REQUIREMENT_MATRIX_V0.md`

The reviewed rule is not approval, not canon, not registry, not release, not
runtime schema, and not production lock.

## Final-Product Chain Tag

Primary final-product chain:

- storyboard/camera.

Secondary final-product chain:

- prompt/asset cards.

This bridge reduces uncertainty about future Chenxing visual continuity. It
does not create another status layer, actual storyboard, prompt payload, image
artifact, or packet instance.

## Carried-Forward Eyeline/Facing Fields

Future Chenxing `ScriptReadyScenePacket` planning and future storyboard planning
must carry these six fields forward whenever a beat or frame includes dialogue,
reaction, attention, avoidance, threat monitoring, companion interaction, or
interpersonal blocking:

| Field | Carry-forward requirement | Chenxing planning impact |
| --- | --- | --- |
| `gazeTarget` | name who or what the character is looking at | prevents missing eye direction in dialogue or reaction beats |
| `facingTarget` | name where the face/body is oriented | prevents accidental back-facing or unclear body orientation |
| `eyelineReason` | state why the gaze/facing choice fits the beat | links visual continuity to beat logic instead of decoration |
| `nonFacingReason` | required when speech or reaction happens without facing the interaction target | distinguishes intentional non-facing from drift |
| `interactionTarget` | required for dialogue, directed action, companion interaction, or threat monitoring | keeps the addressed subject separate from camera subject |
| `continuityCheck` | confirm match with prior blocking or state the intentional change | preserves visual continuity across future scene-packet and storyboard beats |

These are requirement fields only. They do not create actual scene-packet
fields, storyboard frame instances, prompt fields, image-facing delivery cards,
or runtime data in this slice.

## 陸知行 / 愛醬 Interaction Defaults

Future Chenxing scene-packet/storyboard planning must use these defaults:

- If 陸知行 speaks to 愛醬, `gazeTarget`, `facingTarget`, and
  `interactionTarget` default to 愛醬.
- If 愛醬 responds or reacts to 陸知行, the future planning surface must state
  whether she looks at him, turns ear/body toward him, deliberately ignores
  him, watches a threat, or tracks scent/sound.
- If either character speaks or reacts while not facing the other,
  `nonFacingReason` is required. Acceptable reason categories include fear,
  concealment, watching danger, shame, deliberate coldness, tactical attention,
  scent/sound tracking, or another beat-specific intentional non-facing reason.
- If a threat or environmental cue splits attention, `interactionTarget` and
  `gazeTarget` must be separate and the split must be explained in
  `eyelineReason`.

## 愛醬 Independence Preservation

愛醬 is an independent subject in Chenxing visual continuity planning. Future
scene-packet or storyboard planning must not reduce her to pet, tool, clue
machine, mascot, combat asset, mount-only growth, or a visual prop that only
supports 陸知行.

When 愛醬 appears in visual continuity requirements:

- her gaze, ear/body orientation, reaction, scent/sound tracking, refusal,
  curiosity, fear, or attention may be recorded as her own beat logic;
- `interactionTarget` may point from 愛醬 to 陸知行, a threat, scent/sound,
  space, object, or social pressure;
- `eyelineReason` must explain her own visible attention, not only how she helps
  another character;
- `continuityCheck` must preserve her prior blocking and agency unless a future
  reviewed beat intentionally changes it.

## Review Checks

Future review should treat these as incomplete or risky:

- missing eye direction when dialogue, reaction, attention, avoidance, threat
  monitoring, companion interaction, or interpersonal blocking is present;
- missing `gazeTarget` or `facingTarget` in a dialogue frame;
- missing `interactionTarget` for dialogue or directed action;
- missing `eyelineReason` when gaze/facing affects beat meaning;
- missing `nonFacingReason` when speech or reaction happens without facing the
  interaction target;
- accidental back-facing unless explicitly intentional;
- contradictory visible action versus `gazeTarget`, `facingTarget`, or
  `interactionTarget`;
- `continuityCheck` that ignores prior blocking;
- gaze/facing notes that reveal author-only information or
  character-inaccessible knowledge;
- prompt/asset card wording that turns review-only gaze/facing notes into
  prompt-ready hidden reasoning.

## Chenxing Guards

- Current-facing protagonist name is only `陸知行`; `衛昭` is superseded in
  derived/current-facing materials.
- 愛醬 has her own story line and must not be reduced to pet, tool, clue
  machine, mascot, combat asset, or mount-only growth.
- Living world first; final-product chains serve the world later.
- Old opening/mainline material remains discussion evidence only, not locked
  plot, canon, or final-product content.

## Boundary Preservation

- no actual ScriptReadyScenePacket.
- no storyboard frames.
- no prompt cards.
- no image generation.
- no provider/API.
- no renderer, ffmpeg, TTS, or media generation.
- no source/story invention.
- no concrete refs.
- no sample JSON.
- no query instances.
- no evidence bundle.
- no Kotlin/schema/runtime change.
- no canon, registry, release, final approval, or production lock.

This bridge is non-approval and docs/control only. A future implementation
package would be required before any schema, model, runtime, packet, storyboard,
prompt, image, provider, renderer, or media work.
