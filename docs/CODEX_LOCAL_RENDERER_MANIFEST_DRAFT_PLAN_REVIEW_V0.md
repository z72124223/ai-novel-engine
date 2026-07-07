# Codex Local Renderer Manifest Draft Plan Review V0

Status: `LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_PASS`

Date: 2026-07-01

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## Purpose

Phase 13 is a local file-protocol JSON review gate. It reads a PASS Phase 12 manifest draft plan and validates whether that plan can be accepted for a future manifest JSON draft task.

This is review gate only. It does not create a real `ComicVideoRenderHandoffManifest`, does not execute the adapter, does not run renderer/ffmpeg/TTS, does not generate media or final video, and does not claim renderer readiness or MP4 readiness.

## Command

```text
review-local-renderer-manifest-draft-plan --plan <phase12-plan.json> [--review <id>] --out <review-report.json>
```

## Input

- Phase 12 status: `LOCAL_RENDERER_MANIFEST_DRAFT_PLAN`
- Phase 12 downstream boundary: `local_renderer_manifest_draft_plan_only_not_manifest_generation`
- Phase 12 next step: `local_renderer_manifest_draft_plan_review`

## Output

- Output status: `LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_PASS` or FAIL
- Sample output path: `samples/local_story_input/rain_gate_local_renderer_manifest_draft_plan_review_report_sample.json`
- Downstream boundary: `local_renderer_manifest_draft_plan_review_only_not_manifest_generation`
- Next allowed step: `local_renderer_manifest_draft_json_draft`

## Review Checks

- Phase 12 plan verdict/status/downstream step are PASS and expected.
- Phase 12 plan checks are present and all required checks PASS.
- Phase 12 findings are absent.
- Manifest field plan, panel field mappings, required manifest draft work items, verification plan, and target duration are present.
- Boundary flags preserve local-only/no-media/no-manifest/no-readiness constraints.
- The `ComicVideoRenderHandoffManifest` name appears only as a future target type or planning note.

## Boundary

Phase 13 must remain local file-protocol JSON only:

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

PASS only allows `local_renderer_manifest_draft_json_draft` as a future explicitly assigned task. That future task still must not run renderer/ffmpeg/TTS, generate media, claim final media approval, perform provider/media handoff, or create production lock unless a later explicit task changes the boundary.
