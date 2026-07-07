# Codex Voice Timeline Subtitle SFX Contract V0

Status: `DRAFT_FOR_REVIEW`

Route label: `voice_timeline_subtitle_sfx_contract_v0_docs_control`

This document defines a reviewable `VoiceTimelineSubtitleSfxContract`
docs/control surface. It is not an implemented Kotlin/JVM source artifact, not
a CLI command, not runtime or schema, not migration, not sample JSON, not
provider/API, not renderer/ffmpeg/TTS execution, not image/audio/video media
generation, not final media readiness, not canon, not asset approval, not
registry approval, not release, not final approval, and not production lock.

## Purpose

`VoiceTimelineSubtitleSfxContract` describes how a future local JSON packet may
represent dialogue, narration, subtitle, SFX, ambient, silence, and timing
intent so later planning can inspect voice/audio needs without running audio
tools or claiming renderer readiness.

The intended bridge is:

```text
ScriptReadyScenePacket evidence
-> StoryboardCameraPacket planning
-> VoiceTimelineSubtitleSfxContract draft
-> technical review only
-> future B組01/A組01/00 route decision
```

This contract does not auto-start any downstream route. It does not restart
`04/05/06/07/08`, create story content, run TTS, run renderer/ffmpeg, call
provider/API, generate media, approve final media, or create production lock.

## Reviewable Contract Shape

A future voice timeline / subtitle / SFX packet should be reviewable using
these sections.

### Packet Metadata

- `packetId`: stable candidate id.
- `status`: one of `DRAFT_FOR_REVIEW`, `REVIEW_PASS_NOT_APPROVAL`,
  `NEEDS_REVISION`, or `BLOCKED_BY_HUMAN_DECISION`.
- `sourceScenePacketRefs`: scoped `ScriptReadyScenePacket` or script-ready
  alignment references.
- `sourceStoryboardPacketRefs`: scoped `StoryboardCameraPacket` references when
  storyboard/camera planning exists.
- `sourceBeatRefs`: scene/beat evidence needed for dialogue and timing.
- `reviewMetadata`: reviewer id, checklist version, unresolved findings,
  human-decision blockers, and downstream note.

### Dialogue And Narration Cues

Each dialogue or narration cue should include:

- `cueId`
- `sourceBeatRef`
- `speakerRef` or `narratorRef`
- `dialogueTextPlaceholder` or `narrationTextPlaceholder`
- `intentNote`
- `timingIntent`
- `visibility`
- `allowedPromptSurface`
- `forbiddenPromptSurface`
- `protectedBoundaryRef`
- `boundaryCategory`
- `reviewStatus`

Text placeholders are planning fields. They are not final dialogue approval,
not dubbing approval, not TTS input, not voice actor direction, and not media
generation permission.

### Subtitle Placeholders

Subtitle entries may include:

- `subtitleId`
- `sourceCueRef`
- `subtitleTextPlaceholder`
- `languageOrLocale`
- `readingPaceIntent`
- `lineBreakIntent`
- `accessibilityNote`
- `visibility`
- `allowedPromptSurface`
- `forbiddenPromptSurface`
- `reviewStatus`

Subtitle placeholders must remain reviewable candidates. They must not claim
final subtitle approval, final media approval, release, or production lock.

### SFX And Ambient Bed Cues

SFX and ambient entries may include:

- `audioCueId`
- `sourceBeatRef`
- `cueType`: `SFX`, `ambient_bed`, `silence`, or `beat_marker`
- `assetNeedRef` when a sound asset may later be needed
- `sfxIntent`
- `ambientBedNote`
- `silenceOrBeatMarkerIntent`
- `timingIntent`
- `visibility`
- `allowedPromptSurface`
- `forbiddenPromptSurface`
- `protectedBoundaryRef`
- `boundaryCategory`
- `reviewStatus`

SFX and ambient notes are not approved assets, not registry records, not audio
generation prompts, and not renderer input.

### Timing Intent

Timing fields should remain approximate planning notes:

- target scene or beat offset;
- cue ordering;
- expected overlap with panel, dialogue, subtitle, or silence;
- optional duration target;
- blockers if source timing evidence is missing.

Timing intent is not a locked timeline, not final edit timing, not renderer
readiness, and not MP4 readiness.

### Localization Notes

Localization notes may be included when useful:

- language or locale target;
- line-length or readability constraint;
- term consistency note;
- subtitle placement note;
- unresolved human decision.

Localization notes must not rewrite story content or bypass human approval.

## Relationships To Other Route Surfaces

- `ScriptReadyScenePacket`: provides scoped scene/beat/source evidence. This
  contract must not import a full world bible by default or expose author-only
  causality.
- `StoryboardCameraPacket`: may provide panel ordering, camera intent, and
  asset need references that help align audio cue timing.
- Asset need references: remain candidate-only and do not become approved
  assets, registry entries, or production locks.
- Local render package planning: may later inspect the reviewed contract, but
  this document does not authorize renderer execution or media generation.
- Later renderer readiness: requires a separate explicit B組01/A組01/00
  decision and must not be inferred from `REVIEW_PASS_NOT_APPROVAL`.

## Visibility And Prompt Surface Rules

Every field that may later feed prompts, screen descriptions, negative prompts,
prompt-safe asset notes, or voice/audio planning notes must declare
`visibility`, `allowedPromptSurface`, and `forbiddenPromptSurface`.

Prompt surface follows
`docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`:

- fields not explicitly listed in `allowedPromptSurface` must not enter
  prompts, screen descriptions, negative prompts, prompt-safe asset notes, or
  voice/audio planning notes;
- negative prompts are also prompt surface;
- `author_only` / `review_only` material must not be copied, hinted,
  inverted, or paraphrased into dialogue, subtitle, SFX, ambient, silence,
  prompt-facing, or voice/audio planning fields;
- prompt-safe asset notes and audio notes must not leak hidden causality.

Use `forbiddenPromptSurface` to name field families that must remain out of
prompt-facing and voice/audio planning text.

## Protected Boundary Rule

When voice, subtitle, SFX, or ambient notes must avoid leaking protected
information, use `protectedBoundaryRef` plus a generic `boundaryCategory`.

This is required for:

- hidden causality（隱藏因果）;
- culprit identity（犯人身分）;
- sealed motive（封存動機）;
- director goal（導演目標）;
- future reveal（未來揭露）;
- unavailable actor knowledge（角色不可取得知識）.

The generic `boundaryCategory` may say what kind of boundary is protected, but
it must not reveal the protected fact. For example, an SFX cue may say
`boundaryCategory=hidden_causality`, but must not describe the hidden cause.

## Review Checklist

A `VoiceTimelineSubtitleSfxContract` draft should receive
`REVIEW_PASS_NOT_APPROVAL` only when all checklist areas pass.

### Source Evidence

- Does every dialogue, subtitle, SFX, ambient, silence, or beat marker cue cite
  scoped source evidence?
- Are missing source ids recorded as blockers instead of invented?
- Is the packet aligned with `ScriptReadyScenePacket` and
  `StoryboardCameraPacket` evidence where available?

### Timing Feasibility

- Is cue ordering understandable?
- Are timing targets approximate and non-final?
- Are impossible or missing timing assumptions marked as findings?

### Continuity

- Do speaker/reference ids match source evidence?
- Do SFX and ambient notes preserve scene continuity?
- Are silence and beat markers used for story pacing rather than hidden reveal?

### Speaker And Asset References

- Are speaker refs, narrator refs, asset need refs, and panel refs scoped?
- Are asset need refs candidate-only?
- Does the draft avoid asset approval, registry approval, approved reference,
  provider handoff, and production lock claims?

### Prompt-Surface Leakage

- Are `visibility`, `allowedPromptSurface`, and `forbiddenPromptSurface`
  declared for prompt-relevant and voice/audio planning fields?
- Are negative prompts treated as prompt surface?
- Are `protectedBoundaryRef` and generic `boundaryCategory` used instead of
  leaking hidden facts?
- Are `author_only` / `review_only` materials kept out of prompts, screen
  descriptions, negative prompts, prompt-safe asset notes, and voice/audio
  planning notes?

### Subtitle Readability

- Are subtitle placeholders clearly marked as placeholders?
- Are line-break, language, and reading-pace notes reviewable?
- Does the draft avoid final subtitle approval or release claims?

### SFX And Ambient Boundaries

- Are SFX and ambient cues candidate-only?
- Does the draft avoid audio generation, sound-library selection, approved
  asset claims, and registry claims?
- Are silence markers represented as pacing notes, not renderer commands?

### Human Approval Boundaries

- Does the draft state that technical PASS is not user approval?
- Does it avoid final media approval, provider/media handoff, canon, registry,
  release, approval, and production lock claims?

### No Media / Provider / Renderer Claims

- no provider/API
- no renderer/ffmpeg/TTS
- no image/audio/video media generation
- no final media readiness
- no final video readiness
- no renderer readiness
- no MP4 readiness

## Review Statuses

- `DRAFT_FOR_REVIEW`: inspectable candidate only.
- `REVIEW_PASS_NOT_APPROVAL`: technically coherent for later route selection,
  but not human approval, not media approval, and not downstream execution.
- `NEEDS_REVISION`: blocked by missing evidence, unsafe prompt surface,
  continuity issue, readability issue, or boundary drift.
- `BLOCKED_BY_HUMAN_DECISION`: requires B組01/A組01/00/user decision before the
  packet can be reviewed further.

## Narrow Downstream Allowance

On `REVIEW_PASS_NOT_APPROVAL`, B組01/A組01/00 may consider another
route-selected docs/control task or a future explicit decision about local
renderer readiness.

PASS must not auto-start:

- TTS;
- audio generation;
- renderer/ffmpeg execution;
- provider/API;
- schema/runtime implementation;
- direct SQLite writes;
- media generation;
- final media approval;
- provider/media handoff;
- release or production lock.

## Stop Conditions

Stop and return to B組01 / the current control lane if a future task requires:

- Kotlin/JVM source, CLI, runtime, schema, migration, or sample JSON;
- direct SQLite writes;
- provider/API calls;
- web/server/open ports;
- 04/05/06/07/08 restart;
- Phase 20 / Phase 21 restart;
- renderer/ffmpeg/TTS/media generation;
- voice synthesis, sound-library selection, or audio export;
- story content generation;
- canon, asset approval, registry approval, release, final approval, final
  media approval, provider/media handoff, or production lock.
