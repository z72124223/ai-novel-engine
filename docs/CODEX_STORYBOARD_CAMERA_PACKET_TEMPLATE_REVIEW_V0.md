# Codex Storyboard Camera Packet Template Review V0

Status: `DRAFT_FOR_REVIEW`

Route label: `storyboard_camera_packet_template_review_v0_docs_control`

This document defines a reviewable `StoryboardCameraPacket` docs/control
template and review checklist. It is not an implemented Kotlin/CLI/runtime
artifact, not a schema or migration, not sample JSON, not queryer
implementation, not script packet generation, not 04/05/06/07/08 restart, not
provider/API, not renderer/ffmpeg/TTS, not image/audio/video media generation,
not canon, not registry, not release, not final approval, and not production
lock.

## Purpose

`StoryboardCameraPacket` is a future docs-only bridge from
`ScriptReadyScenePacket` / script-ready scene packet alignment evidence into
storyboard/camera planning.

The bridge is:

```text
ScriptReadyScenePacket or script-ready alignment evidence
-> StoryboardCameraPacket draft
-> Storyboard/camera technical review
-> future B組01/A組01/00 route decision
```

This route does not restart `04/05/06/07/08`. It only defines what a future
storyboard/camera packet draft should contain and how a reviewer should check
it before any later route decision.

## Reviewable Template

A future `StoryboardCameraPacket` draft should include these sections.

### Packet Metadata

- `packetId`: stable candidate id for this storyboard/camera packet.
- `status`: one of `DRAFT_FOR_REVIEW`, `REVIEW_PASS_NOT_APPROVAL`, or
  `NEEDS_REVISION`.
- `sourceScenePacketRef`: reference to a `ScriptReadyScenePacket`, script-ready
  alignment evidence, or accepted route doc.
- `sourceSceneId`, `sourceBeatIds`, `sourceQueryRefs`: scoped source evidence.
- `reviewMetadata`: reviewer id, review date, checklist version, unresolved
  findings, and downstream note.

### Source Scene / Beat References

- Cite only the scene, beat, and query evidence needed for the storyboard/camera
  packet.
- Do not import a full world bible by default.
- Mark missing evidence as missing; do not invent scene ids, beat ids, assets,
  dialogue, or causality.

### Panel List

Each panel entry should include:

- `panelId`
- `sourceBeatRef`
- `panelPurpose`
- `shotType`
- `cameraIntent`
- `compositionRecipeRefs`
- `assetNeedRefs`
- `continuityNotes`
- `dialoguePlaceholder` or `subtitlePlaceholder` when useful
- `audioCuePlaceholder` when useful
- `visibility`
- `allowedPromptSurface`
- `forbiddenPromptSurface`
- `protectedBoundaryRef`
- `boundaryCategory`
- `reviewStatus`

Panel entries are text planning only. They are not storyboard images, not image
prompts ready for a provider, not final visual approval, and not renderer input.

### Camera Intent

Camera intent should solve a story or production need:

- clarify space;
- show blocking;
- guide attention;
- preserve secrecy by not showing unavailable knowledge;
- show evidence detail that is visible to the intended audience;
- keep neutral coverage when no special camera intent is needed.

Camera intent must not over-direct suspicion, reveal sealed motives, imply
culprit identity, or replace missing causality with style.

### Composition Recipe References

`compositionRecipeRefs` may point to future `CompositionRecipe` candidates, but
the packet must not treat them as approved assets, approved prompts, registry
entries, or production locks.

### Asset Need References

`assetNeedRefs` may describe candidate needs such as character reference,
location reference, prop reference, costume reference, or SFX reference. They
must preserve asset status:

- candidate only;
- not asset approval;
- not registry approval;
- not approved reference;
- not production lock.

### Continuity Notes

Continuity notes should record:

- scene lock or scene candidate status;
- blocking continuity;
- prop and material continuity;
- character position continuity;
- what cannot be shown because evidence is missing or protected.

### Dialogue / Subtitle Placeholders

Dialogue and subtitle placeholders may cite reviewed source lines or mark a
slot as pending. They must not generate new story content, rewrite dialogue, or
claim user-facing approval.

## Visibility And Prompt Surface Rules

Every field that may later feed prompts, screen descriptions, negative prompts,
or prompt-safe asset notes must declare `visibility` and `allowedPromptSurface`.

Prompt surface follows
`docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`:

- fields not explicitly listed in `allowedPromptSurface` must not enter prompts,
  screen descriptions, negative prompts, or prompt-safe asset notes;
- negative prompts are also prompt surface;
- `author_only` / `review_only` material must not be copied or paraphrased into
  prompt-facing fields or `NegativeBoundaryCard`;
- prompt-safe asset notes are not allowed to leak hidden causality.

Use `forbiddenPromptSurface` to name field families that must remain out of
prompt-facing text.

## Protected Boundary Rule

When a storyboard/camera packet needs to avoid leaking protected information,
use `protectedBoundaryRef` plus a generic `boundaryCategory`.

This is required for:

- hidden causality（隱藏因果）
- culprit identity（犯人身分）
- sealed motive（封存動機）
- director goal（導演目標）
- future reveal（未來揭露）
- unavailable actor knowledge（角色不可取得知識）

The generic `boundaryCategory` may say that a category is protected, but it must
not reveal the protected fact. For example, a panel may say
`boundaryCategory=sealed_motive`, but it must not describe the sealed motive.

## Review Checklist

A storyboard/camera packet draft should receive `REVIEW_PASS_NOT_APPROVAL` only
when all checklist areas pass.

### Scope

- Is the packet limited to the assigned scene / beat scope?
- Does it avoid restarting `04/05/06/07/08`?
- Does it avoid story content generation and script packet generation?

### Source Evidence

- Does every panel cite source scene / beat / query evidence?
- Are missing sources named as blockers instead of invented?
- Is the packet aligned with the script-ready scene packet route?

### Continuity

- Does the packet preserve scene lock or mark scene candidates?
- Are blocking, prop, material, and character-position notes consistent?
- Are do-not-show boundaries explicit where needed?

### Prompt-Surface Safety

- Are `visibility`, `allowedPromptSurface`, and `forbiddenPromptSurface`
  declared for prompt-relevant fields?
- Are negative prompts treated as prompt surface?
- Are `protectedBoundaryRef` and generic `boundaryCategory` used instead of
  leaking hidden facts?
- Are `author_only` / `review_only` materials kept out of prompts, screen
  descriptions, negative prompts, and prompt-safe asset notes?

### Asset Status

- Are `assetNeedRefs` candidate-only?
- Does the packet avoid asset approval, registry approval, approved reference,
  provider handoff, and production lock claims?

### Human Approval Boundaries

- Does the packet state that technical PASS is not user approval?
- Does it avoid final visual approval, final media approval, canon, registry,
  release, approval, and production lock claims?

### No Media / Provider / Renderer Claims

- no provider/API
- no renderer/ffmpeg/TTS
- no image/audio/video media generation
- no final video readiness
- no production manifest or provider/media handoff

## Review Statuses

- `DRAFT_FOR_REVIEW`: packet is inspectable as a candidate only.
- `REVIEW_PASS_NOT_APPROVAL`: packet passed technical review for the declared
  scope, but this is not human approval, not final visual approval, not final
  media approval, not canon, not registry, not release, and not production lock.
- `NEEDS_REVISION`: packet has missing evidence, continuity risk,
  prompt-surface leakage risk, asset-status overclaim, or approval-boundary
  drift.

## Narrow Downstream Allowance

On `REVIEW_PASS_NOT_APPROVAL`, `B組01/A組01/00` may consider a future bounded
docs/control decision for `asset_lifecycle_registry_decision_packet` or another
route-selected next task.

PASS must not auto-start that downstream task. PASS must not resume
`04/05/06/07/08`, create assets, approve registry data, call provider/API,
run renderer/ffmpeg/TTS, generate media, or claim production lock.

## Stop Conditions

Stop and return to the control lane if a future task needs:

- Kotlin/JVM source or CLI command changes;
- runtime/schema/migration work;
- sample JSON generation;
- queryer implementation;
- story content generation;
- script packet generation;
- 04/05/06/07/08 restart;
- Phase 20 / Phase 21 restart;
- direct SQLite writes;
- provider/API calls;
- web/server/open ports;
- renderer/ffmpeg/TTS;
- image/audio/video media generation;
- canon, registry, release, final approval, final media approval, provider/media
  handoff, approval, or production lock.
