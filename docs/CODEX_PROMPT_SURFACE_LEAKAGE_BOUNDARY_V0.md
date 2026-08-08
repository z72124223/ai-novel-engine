# Codex Prompt Surface Leakage Boundary V0

Status: `CURRENT_CONTROL_PACKET`

This document defines a docs/control boundary for prompt surface（提示詞表面）
leakage. It is not runtime enforcement, not a schema change, not sample data,
not provider/API integration, not media generation, and not approval of canon,
registry, release, final media, or production lock.

## Core Rule

Prompt surface（提示詞表面）is deny-by-default（預設拒絕）.

Fields not explicitly listed in `allowedPromptSurface` must not enter:

- prompts（提示詞）
- screen descriptions（畫面描述）
- negative prompts（負面提示詞）
- prompt-safe asset notes（提示詞安全素材註記）

Negative prompts are also prompt surface. They are not a safe place to copy,
summarize, hint, invert, or paraphrase hidden material.

## Protected Boundary Rule

`author_only` / `review_only` material must not be copied or paraphrased into
`NegativeBoundaryCard`.

When a prompt-facing packet needs to avoid hidden material, use
`protectedBoundaryRef` plus a generic `boundaryCategory` instead of exposing the
hidden fact. This protects:

- hidden causality（隱藏因果）
- culprit identity（犯人身分）
- sealed motive（封存動機）
- director goal（導演目標）
- future reveal（未來揭露）
- unavailable actor knowledge（角色不可取得知識）

Allowed generic examples include `hidden_causality`, `culprit_identity`,
`sealed_motive`, `director_goal`, `future_reveal`, and
`unavailable_actor_knowledge`. The generic category may tell a reviewer what
kind of leak is being blocked, but it must not reveal the protected content.

## Card Surface Requirements

Every future card or recipe that can influence prompts must define visibility
（可見性）, allowed/forbidden prompt surface（允許/禁止提示詞表面）, and
`reviewStatus` rules.

### `ReferenceCard`

- Must declare `visibility`.
- Must declare `allowedPromptSurface`.
- Must declare `forbiddenPromptSurface`.
- Must declare `reviewStatus`.
- Must not let `author_only` or `review_only` evidence become prompt-facing
  text unless the exact field is explicitly allowed and reviewed for prompt use.

### `PromptIntentCard`

- Must declare intended prompt-facing purpose.
- Must declare the exact source fields allowed to become prompt text.
- Must keep hidden causality, future reveal, director goal, and unavailable
  actor knowledge outside prompt-facing text.
- Must fail review if it relies on unlisted fields.

### `NegativeBoundaryCard`

- Must declare `protectedBoundaryRef`.
- Must declare generic `boundaryCategory`.
- Must not copy or paraphrase `author_only` / `review_only` content.
- Must describe only what category of boundary is protected, not the protected
  fact itself.
- Must treat negative prompts as prompt surface and apply the same deny rule.

### `CompositionRecipe`

- Must declare which `ReferenceCard`, `PromptIntentCard`, and
  `NegativeBoundaryCard` inputs are used.
- Must declare the composed `allowedPromptSurface`.
- Must declare all forbidden source surfaces.
- Must keep prompt-safe asset notes separate from protected author/review
  causality.
- Must not claim final media approval, asset approval, canon, registry, release,
  or production lock.

## Review Status Rules

`reviewStatus` must remain explicit:

- `draft`: may be inspected only as a candidate and must not be used for live
  prompt generation.
- `review_pass`: technically safe for the declared prompt surface only.
- `needs_revision`: blocked until the leaking field or unsafe surface is fixed.
- `rejected`: must not feed prompts, negative prompts, screen descriptions, or
  prompt-safe asset notes.

Review PASS is technical only. It is not human approval, canon approval, asset
approval, registry approval, final media approval, release, or production lock.

## Stop Conditions

Stop and return to B組01 / the current control lane if any future task requires:

- Kotlin/CLI/runtime/schema implementation.
- Sample generation that could be mistaken for canon, setting-pack data, or
  approved assets.
- Direct SQLite writes.
- Provider/API calls.
- 04/05/06/07/08 restart.
- Phase 20 / Phase 21 restart.
- Renderer/ffmpeg/TTS/media generation.
- External paid tools.
- Canon, registry, release, final approval, final media approval, provider/media
  handoff, or production lock.

## Future Implementation Note

A future implementation task may add validators for `allowedPromptSurface`,
`forbiddenPromptSurface`, `protectedBoundaryRef`, `boundaryCategory`, and
`reviewStatus`. This document does not authorize that implementation by itself.
