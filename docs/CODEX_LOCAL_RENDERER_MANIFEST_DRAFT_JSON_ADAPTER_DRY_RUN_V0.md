# Codex Local Renderer Manifest Draft JSON Adapter Dry-Run V0

Status: `LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_PASS`

Date: 2026-07-01

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## Purpose

Phase 16 is a local file-protocol JSON adapter dry-run. It reads a PASS Phase 14 draft-only renderer manifest JSON draft and a PASS Phase 15 review report, then checks whether every draft panel can be mapped toward future local renderer adapter requirements.

This is adapter dry-run only. It does not execute an adapter, does not run renderer/ffmpeg/TTS, does not generate media or final video, does not generate or approve a real production `ComicVideoRenderHandoffManifest`, and does not claim final video readiness, renderer readiness, MP4 readiness, final media approval, provider/media handoff, canon, registry, release, approval, or production lock.

## Command

```text
dry-run-local-renderer-manifest-draft-json-adapter --draft <phase14-draft-json.json> --review <phase15-review-report.json> [--dry-run-id <id>] --out <dry-run-report.json>
```

`--dry-run-id` is used because this CLI already treats `--dry-run` as a boolean dry-run flag in other commands.

## Inputs

- Phase 14 draft status: `LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT`
- Phase 14 downstream boundary: `local_renderer_manifest_draft_json_draft_only_not_production_manifest`
- Phase 14 next step: `local_renderer_manifest_draft_json_review`
- Phase 15 review status: `LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_PASS`
- Phase 15 downstream boundary: `local_renderer_manifest_draft_json_review_only_not_production_approval`
- Phase 15 next step: `local_renderer_manifest_draft_json_adapter_dry_run`

## Output

- Output status: `LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_PASS` or FAIL
- Sample output path: `samples/local_story_input/rain_gate_local_renderer_manifest_draft_json_adapter_dry_run_report_sample.json`
- Downstream boundary: `local_renderer_manifest_draft_json_adapter_dry_run_only_not_renderer_execution`
- Next allowed step: `local_renderer_manifest_adapter_contract_plan`

## Dry-Run Checks

- Phase 14 draft and Phase 15 review verdict/status/downstream step are PASS and expected.
- Phase 14 and Phase 15 source ids align.
- Phase 14 and Phase 15 checks are present and all required checks PASS.
- Phase 14 and Phase 15 findings are absent.
- Draft panel entries and draft field coverage are present.
- Every draft panel has one adapter dry-run mapping entry.
- No draft panel is unmapped.
- Boundary flags preserve local-only/no-media/no-production-approval constraints.
- `draftManifestGenerated=true` means only that a draft artifact exists.
- `productionManifestGenerated=false`, `productionReady=false`, and `productionApproved=false` remain required.
- The `ComicVideoRenderHandoffManifest` name appears only as a future target or boundary reference, not as production generation or approval.

## Boundary

Phase 16 must remain local file-protocol JSON only:

- adapter dry-run
- draft-only
- no media generation
- no adapter execution
- no renderer/ffmpeg/TTS
- no real production `ComicVideoRenderHandoffManifest` generation or approval
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
- `adapterExecuted=false`
- `draftManifestGenerated=true`
- `productionManifestGenerated=false`
- `productionReady=false`
- `productionApproved=false`
- `noExternalPaidProviderTools=true`
- `rendererReady=false`
- `mp4ReadinessClaimed=false`

## Next Step

PASS only allows `local_renderer_manifest_adapter_contract_plan` as a future explicitly assigned task. That future task still must not run renderer/ffmpeg/TTS, generate media, claim final video readiness, claim final media approval, perform provider/media handoff, approve a real production `ComicVideoRenderHandoffManifest`, or create production lock unless a later explicit task changes the boundary.
