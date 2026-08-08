# Codex Blueprint Future Route Gap Audit

Status: CONTROL_NOTE_FOR_NEW_WINDOWS
Date: 2026-07-01
Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`
Branch: `codex/mvp6-checkpoint-branching-spec`

## Purpose

This note preserves the future route gaps discussed with the user so a new Codex window does not confuse the active local renderer / manifest phases with the whole product blueprint.

The current local product-delivery baseline may display `Project completion progress: 100%`, but that is not true final project completion. It is not final video readiness, formal registry, canon, release, provider/media handoff, approval, or production lock.

Ordered route view: `docs/CODEX_BLUEPRINT_FUTURE_ROUTE_INTEGRATION_MAP_V0.md`. That integration map is the current ordered route view across user / 00 input, 09 foundation, demand-driven query, script-ready packets, 04/05/06/07/08, storyboard/camera, asset registry, voice timeline, local renderer, human approval, and external provider gates. This gap audit remains the preservation note that explains why those routes must stay visible.

Whole-project blueprint entry: `docs/CODEX_WHOLE_PROJECT_BLUEPRINT_V0.md`.
Use that file as the top-level route control document after the user redirected
the work away from the old renderer-manifest micro-phase chain.

Execution control entry: `docs/CODEX_BLUEPRINT_EXECUTION_CONTROL_INDEX_V0.md`.
Use that file plus `docs/CODEX_CURRENT_ROUTE_CARD_V0.md` for current task
selection. This gap audit preserves missing route families and stop conditions;
it is not the active task selector.

Current redirect note:

- Phase 19 completed PASS and 03 review PASS.
- Phase 20 was assigned briefly, then stopped by 01 after the user requested the
  whole project plan be added to the blueprint instead of continuing old work.
- 02 reported `STOPPED_BEFORE_EDITS`; no Phase 20 file edits were made.
- Do not resume Phase 20 / Phase 21 until the whole-project blueprint has been
  reviewed and 01/user explicitly selects the next route.

## Current Coordination State

- 01 control thread id: `019f0dd1-1543-7f22-b646-5c12d07d168d`.
- 02 worker thread id: `019f1882-d5c6-72a3-893c-6d43cd097835`.
- 03 reviewer thread id: `019f1ad1-2926-70c3-b85c-267df6fe331e`.
- Phase 19 completed PASS and 03 review PASS.
- Phase 20 was stopped before edits after the user redirected the project toward
  whole-project blueprint alignment.
- 02 is idle for the old renderer-manifest route and must not resume Phase 20
  or Phase 21 without explicit 01/user route selection.
- 03 should stay read-only idle until 01 sends a new review request for a
  blueprint or docs/control slice.
- Event-driven handoff is primary: 02 reports completion to 01, 01 sends to 03, 03 reports review to 01, then 01 assigns exactly one next bounded task.
- The watchdog automation is only backup. Do not use frequent wakeups as the main work loop.

## Model And Usage Guardrail

- 01 lightweight status checks may use the current/default model.
- 01 assignment decisions, 02 implementation/verification, and 03 read-only reviews should use `gpt-5.5` with `thinking=xhigh` when supported.
- Check quota before every high-cost transition:
  - before 01 sends a PASS result to 03 for read-only review;
  - before 01 assigns a new 02 implementation/verification task;
  - after two completed medium slices if work continues;
  - when the last quota observation is older than three hours;
  - whenever the watchdog wakes and must decide whether to advance work.
- Do not wake only to check quota more often than the hourly watchdog unless a 02/03 completion event needs a decision.
- Weekly quota is a hard gate. If weekly quota is zero, stale, not visible, or too low, do not start work even if the 5-hour quota is high.
- Preserve enough quota for at least one final status / handoff message.
- If quota is near exhaustion, stop with status instead of starting a new phase.

## Future Routes Not Yet Fully Integrated Into The Active Blueprint

These routes are already mentioned across planning docs, but they are not yet a single executable mainline. New windows must preserve them as product direction rather than letting the current renderer-manifest phases crowd them out.

1. Demand-driven query layer
   - Evidence: `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md`.
   - Needed route: World Card Queryer, Asset Queryer, and Story / Situation / Continuity / Pressure Queryer.
   - Missing active mainline: query request / response JSON templates, query-scope review gate, query audit evidence, and clear data-source rules.
   - Boundary: not implemented CLI, SQLite schema, runtime enforcement, provider/API, GPT automation, Seedance, or Oiioii.

2. New-world foundation and 09 card organization
   - Evidence: `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md` and `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md`.
   - Needed route: user / 00 input -> new-world foundation brief -> 09 world / character / location / premise cards -> scoped downstream retrieval.
   - Missing active mainline: conversion from planning pool to reviewable non-canon foundation cards before script/storyboard production.

3. Script-ready scene packet and 04 / 05 / 06 / 07 / 08 flow
   - Evidence: `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md` and `docs/HANDOFF.md`.
   - Needed route: 04 generation -> 05 review -> 06 display / user approval -> 07 asset candidates -> 08 GPT storyboard pickup / verification.
   - Missing active mainline: bridge from current local story/render package artifacts into script-ready scene packet review and manual approval.
   - Boundary: do not resume 04 / 05 / 06 / 07 / 08 without explicit 01/user approval.

4. Storyboard and camera planning
   - Evidence: `docs/STORYBOARD_AND_CAMERA_PLANNING.md`.
   - Needed route: script-ready packet -> storyboard packet -> camera / panel intent -> user-reviewable storyboard image or packet evidence.
   - Missing active mainline: deterministic storyboard packet draft/review commands and evidence shape.

5. Production asset registry and asset lifecycle
   - Evidence: `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md` and `docs/ASSET_REGISTRY_IMPLEMENTATION_BLUEPRINT.md`.
   - Needed route: asset candidates -> review -> approved references -> registry or sidecar registry decision -> reuse by storyboard / render packets.
   - Missing active mainline: candidate-to-approved asset lifecycle and decision on SQLite runtime DB, separate production DB, or exportable sidecar DB.
   - Boundary: current asset / prompt cards are not approved assets and not registry entries.

6. Voice timeline, subtitle, and audio planning
   - Evidence: `docs/CODEX_NO_EXTERNAL_TOOLS_LOCAL_VIDEO_AUTOMATION_ROADMAP.md`.
   - Needed route: dialogue / narration / SFX / subtitle timing -> local JSON voice timeline -> review gate -> later renderer input.
   - Missing active mainline: voice timeline command sequence integrated with the new local story-to-render chain.

7. Local renderer and manifest path after Phase 16
   - Evidence: Phase 7 through Phase 16 local renderer / manifest JSON chain.
   - Needed route: adapter dry-run -> adapter contract plan -> implementation / dry-run review -> eventual local renderer execution.
   - Missing active mainline: actual renderer execution and MP4 generation from the new local story pipeline.
   - Boundary: do not run renderer/ffmpeg/TTS or generate media unless explicitly assigned.

8. Human approval, canon, registry, release, and production lock
   - Needed route: define exactly what the user reviews, what PASS means, and what is still blocked before canon/registry/release/production-lock decisions.
   - Missing active mainline: positive approval checklist that is separate from the repeated "not approval" boundary text.
   - Boundary: silence is never approval.

9. External provider / paid tool route
   - Needed route: keep as optional future after funding or explicit user decision.
   - Missing active mainline: provider/media handoff decision packet and budget gate.
   - Boundary: no provider/API calls and no external paid/provider tools in the current no-funding local path.

## Recommended Control Work After Whole-Project Redirect

After the user redirected the route away from the old renderer-manifest
micro-phase chain, 01 should treat the whole-project blueprint as the top-level
control surface.

Current control package:

- `docs/CODEX_WHOLE_PROJECT_BLUEPRINT_V0.md`
- `docs/CODEX_WHOLE_PROJECT_WORK_PACKAGE_V0.md`
- `docs/CODEX_TRUE_PROJECT_COMPLETION_PROGRESS_MODEL_V0.md`
- `docs/CODEX_POSITIVE_APPROVAL_MATRIX_V0.md`
- `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md`
- `docs/CODEX_09_FOUNDATION_CARD_ORGANIZATION_V0.md`

Do not resume Phase 20, Phase 21, or the old renderer-manifest route until this
control package is reviewed and 01/user explicitly selects a route.

## Stop Conditions

Stop and return to the user / 01 if any next step requires:

- product direction decision;
- user review of visual/story/audio output;
- final media approval;
- canon, registry, release, or production-lock decision;
- external paid/provider tool or funding decision;
- unclear quota state or near-exhausted weekly quota;
- failed verification or missing 03 review.
