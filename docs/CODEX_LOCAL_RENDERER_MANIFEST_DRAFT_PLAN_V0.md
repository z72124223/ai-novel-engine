# Codex Local Renderer Manifest Draft Plan V0

Status: `LOCAL_RENDERER_MANIFEST_DRAFT_PLAN`

Date: 2026-07-01

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## Purpose

Phase 12 is a local file-protocol JSON planning layer. It reads a PASS Phase 10 implementation draft and a PASS Phase 11 implementation-draft review report, then writes a manifest draft plan for a future reviewed manifest-draft step.

This is planning only. It does not create a real `ComicVideoRenderHandoffManifest`, does not execute the adapter, does not run renderer/ffmpeg/TTS, does not generate media or final video, and does not claim renderer readiness or MP4 readiness.

## Command

```text
plan-local-renderer-manifest-draft --implementation-draft <phase10-draft.json> --draft-review <phase11-review.json> [--plan <id>] --out <plan.json>
```

## Inputs

- Phase 10 implementation draft status: `LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT`
- Phase 10 downstream boundary: `local_renderer_adapter_implementation_draft_only_not_manifest_generation`
- Phase 10 next step: `local_renderer_adapter_implementation_draft_review`
- Phase 11 review status: `LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_PASS`
- Phase 11 downstream boundary: `local_renderer_adapter_implementation_draft_review_only_not_manifest_generation`
- Phase 11 next step: `local_renderer_manifest_draft_plan`

## Output

- Output status: `LOCAL_RENDERER_MANIFEST_DRAFT_PLAN`
- Sample output path: `samples/local_story_input/rain_gate_local_renderer_manifest_draft_plan_sample.json`
- Downstream boundary: `local_renderer_manifest_draft_plan_only_not_manifest_generation`
- Next allowed step: `local_renderer_manifest_draft_plan_review`

## What The Plan Contains

- source id alignment checks between the Phase 10 draft and Phase 11 review
- required manifest draft work items derived from the reviewed implementation draft
- `manifestFieldPlan` planning notes for future `ComicVideoRenderHandoffManifest` fields
- verification plan for the future manifest-draft review step
- boundary summary and encoded local-only/no-media/no-manifest flags

## Boundary

Phase 12 must remain local file-protocol JSON only:

- no media generation
- no real `ComicVideoRenderHandoffManifest` generation
- no adapter execution
- no renderer/ffmpeg/TTS
- no provider/API
- no direct SQLite writes
- no web/server
- no final media approval
- no provider/media handoff
- no canon/registry/release/approval/production lock
- no renderer readiness claim
- no MP4 readiness claim

## Required Boundary Fields

- `mediaGenerated=false`
- `rendererInvocation=none`
- `providerInvocation=none`
- `ttsInvocation=none`
- `manifestGenerated=false`
- `adapterImplementationExecuted=false`
- `noExternalPaidProviderTools=true`
- `rendererReady=false`
- `mp4ReadinessClaimed=false`

## Next Step

PASS only allows `local_renderer_manifest_draft_plan_review` as a future review-gate task. That future task still must not generate a real `ComicVideoRenderHandoffManifest`, run renderer/ffmpeg/TTS, generate media, claim final media approval, perform provider/media handoff, or create production lock unless a later explicit task changes the boundary.
