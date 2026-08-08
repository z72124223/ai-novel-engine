# Codex Local Renderer Manifest Draft JSON Review V0

Status: `LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_PASS`

Date: 2026-07-01

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## Purpose

Phase 15 is a local file-protocol JSON review gate. It reads a PASS Phase 14 draft-only renderer manifest JSON draft and validates whether that draft can be accepted for a future adapter dry-run planning step.

This is review gate only. It is a draft-only local JSON review, not a real production `ComicVideoRenderHandoffManifest` approval. It does not execute the adapter, does not run renderer/ffmpeg/TTS, does not generate media or final video, and does not claim final video readiness, renderer readiness, MP4 readiness, final media approval, provider/media handoff, canon, registry, release, approval, or production lock.

## Command

```text
review-local-renderer-manifest-draft-json --draft <phase14-draft-json.json> [--review <id>] --out <review-report.json>
```

## Input

- Phase 14 draft status: `LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT`
- Phase 14 downstream boundary: `local_renderer_manifest_draft_json_draft_only_not_production_manifest`
- Phase 14 next step: `local_renderer_manifest_draft_json_review`

## Output

- Output status: `LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_PASS` or FAIL
- Sample output path: `samples/local_story_input/rain_gate_local_renderer_manifest_draft_json_review_report_sample.json`
- Downstream boundary: `local_renderer_manifest_draft_json_review_only_not_production_approval`
- Next allowed step: `local_renderer_manifest_draft_json_adapter_dry_run`

## Review Checks

- Phase 14 draft verdict/status/downstream step are PASS and expected.
- Phase 14 checks are present and all required checks PASS.
- Phase 14 findings are absent.
- Draft panel entries and draft field coverage are present.
- Boundary flags preserve local-only/no-media/no-production-approval constraints.
- `draftManifestGenerated=true` means only that a draft artifact exists.
- `productionManifestGenerated=false`, `productionReady=false`, and `productionApproved=false` remain required.
- The `ComicVideoRenderHandoffManifest` name appears only as a future target or boundary reference, not as production approval.

## Boundary

Phase 15 must remain local file-protocol JSON only:

- draft-only
- no media generation
- no real production `ComicVideoRenderHandoffManifest` generation or approval
- no adapter execution
- no renderer/ffmpeg/TTS
- no provider/API
- no direct SQLite writes
- no web/server
- no final video readiness
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
- `productionApproved=false`
- `noExternalPaidProviderTools=true`
- `rendererReady=false`
- `mp4ReadinessClaimed=false`

## Next Step

PASS only allows `local_renderer_manifest_draft_json_adapter_dry_run` as a future explicitly assigned task. That future task still must not run renderer/ffmpeg/TTS, generate media, claim final video readiness, claim final media approval, perform provider/media handoff, approve a real production `ComicVideoRenderHandoffManifest`, or create production lock unless a later explicit task changes the boundary.
