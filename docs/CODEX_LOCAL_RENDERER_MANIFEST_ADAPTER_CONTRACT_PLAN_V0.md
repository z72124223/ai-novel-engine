# Codex Local Renderer Manifest Adapter Contract Plan V0

Status: `LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN`

Date: 2026-07-02

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## Purpose

Phase 17 is a local file-protocol JSON planning layer. It reads the PASS Phase 16 adapter dry-run report and writes an adapter contract plan for a future review gate.

This is planning only. It is not adapter implementation, not adapter execution, not renderer execution, not ffmpeg/TTS execution, not media generation, not final video, not real production `ComicVideoRenderHandoffManifest` generation or approval, not final media approval, not provider/media handoff, not canon, not registry, not release, not approval, and not production lock.

## Command

```text
plan-local-renderer-manifest-adapter-contract --dry-run-report <phase16-dry-run-report.json> [--plan <id>] --out <plan.json>
```

`--dry-run-report` is used instead of `--dry-run` because this CLI already uses `--dry-run` as a boolean execution flag in renderer-related commands.

## Input

- Phase 16 status: `LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_PASS`
- Phase 16 downstream boundary: `local_renderer_manifest_draft_json_adapter_dry_run_only_not_renderer_execution`
- Phase 16 next step: `local_renderer_manifest_adapter_contract_plan`
- Sample input path: `samples/local_story_input/rain_gate_local_renderer_manifest_draft_json_adapter_dry_run_report_sample.json`

## Output

- Output status: `LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN` or FAIL
- Sample output path: `samples/local_story_input/rain_gate_local_renderer_manifest_adapter_contract_plan_sample.json`
- Downstream boundary: `local_renderer_manifest_adapter_contract_plan_only_not_adapter_implementation`
- Next allowed step: `local_renderer_manifest_adapter_contract_plan_review`

## Planned Contract Content

The plan records:

- source dry-run, draft, review, plan, handoff, and story ids;
- adapter contract requirements;
- adapter input mapping plan from Phase 16 mapped draft panel fields;
- required adapter contract work items;
- verification plan for a future review gate;
- local-only and no-media boundary fields.

The adapter contract is a future planning target only. It does not create a real adapter, does not execute an adapter, and does not create a production manifest.

## Boundary

Phase 17 must remain local file-protocol JSON only:

- adapter contract planning only
- no adapter implementation
- no adapter execution
- no renderer/ffmpeg/TTS
- no media generation
- no real production `ComicVideoRenderHandoffManifest` generation or approval
- no provider/API
- no direct SQLite writes
- no web/server
- no final video readiness
- no renderer readiness
- no MP4 readiness
- no final media approval
- no provider/media handoff
- no canon/registry/release/approval/production lock

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

PASS only allows `local_renderer_manifest_adapter_contract_plan_review` as a future explicitly assigned review gate. That future task still must not implement or execute an adapter, run renderer/ffmpeg/TTS, generate media, claim final video readiness, perform provider/media handoff, approve a real production `ComicVideoRenderHandoffManifest`, or create production lock unless a later explicit task changes the boundary.
