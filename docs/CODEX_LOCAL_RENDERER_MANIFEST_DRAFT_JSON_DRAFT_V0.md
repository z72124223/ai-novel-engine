# Codex Local Renderer Manifest Draft JSON Draft V0

Status: `LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT`

Date: 2026-07-01

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## Purpose

Phase 14 is a draft-only local file-protocol JSON layer. It reads a PASS Phase 12 manifest draft plan and a PASS Phase 13 manifest draft plan review report, then writes a draft-only renderer manifest JSON packet for later review.

This is not a real production `ComicVideoRenderHandoffManifest`. It does not execute the adapter, does not run renderer/ffmpeg/TTS, does not generate media or final video, and does not claim renderer readiness, MP4 readiness, final media approval, provider/media handoff, canon, registry, release, approval, or production lock.

## Command

```text
draft-local-renderer-manifest-json --plan <phase12-plan.json> --plan-review <phase13-review.json> [--draft <id>] --out <draft-json.json>
```

## Inputs

- Phase 12 plan status: `LOCAL_RENDERER_MANIFEST_DRAFT_PLAN`
- Phase 12 downstream boundary: `local_renderer_manifest_draft_plan_only_not_manifest_generation`
- Phase 12 next step: `local_renderer_manifest_draft_plan_review`
- Phase 13 review status: `LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_PASS`
- Phase 13 downstream boundary: `local_renderer_manifest_draft_plan_review_only_not_manifest_generation`
- Phase 13 next step: `local_renderer_manifest_draft_json_draft`

## Output

- Output status: `LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT` or FAIL
- Sample output path: `samples/local_story_input/rain_gate_local_renderer_manifest_draft_json_draft_sample.json`
- Downstream boundary: `local_renderer_manifest_draft_json_draft_only_not_production_manifest`
- Next allowed step: `local_renderer_manifest_draft_json_review`

## What The Draft Contains

- source id alignment between the Phase 12 plan and Phase 13 review
- readiness checks for source state, source checks, findings, boundaries, draft field coverage, and no readiness overclaim
- `draftPanelManifestEntries` derived from Phase 12 `manifestFieldPlan` panel plans
- draft-only field hints for future renderer manifest review work
- boundary summary and encoded draft-only/no-media/no-production-manifest flags

## Boundary

Phase 14 must remain local file-protocol JSON only:

- draft-only
- no media generation
- no real production `ComicVideoRenderHandoffManifest` generation
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
- `adapterImplementationExecuted=false`
- `draftManifestGenerated=true`
- `productionManifestGenerated=false`
- `productionReady=false`
- `noExternalPaidProviderTools=true`
- `rendererReady=false`
- `mp4ReadinessClaimed=false`

## Next Step

PASS only allows `local_renderer_manifest_draft_json_review` as a future review-gate task. That future task still must not run renderer/ffmpeg/TTS, generate media, claim final media approval, perform provider/media handoff, create a real production `ComicVideoRenderHandoffManifest`, or create production lock unless a later explicit task changes the boundary.
