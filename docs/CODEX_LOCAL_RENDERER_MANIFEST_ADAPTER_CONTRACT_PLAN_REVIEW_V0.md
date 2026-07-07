# Codex Local Renderer Manifest Adapter Contract Plan Review V0

Status: `LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_REVIEW_PASS`

Date: 2026-07-02

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## Purpose

Phase 18 is a local file-protocol JSON review gate. It reads a PASS Phase 17 adapter contract plan and validates whether that plan is complete and safe enough to allow a future adapter contract draft step.

This is review only. It is not adapter implementation, not adapter execution, not renderer execution, not ffmpeg/TTS execution, not media generation, not final video, not real production `ComicVideoRenderHandoffManifest` generation or approval, not final media approval, not provider/media handoff, not canon, not registry, not release, not approval, and not production lock.

## Command

```text
review-local-renderer-manifest-adapter-contract-plan --plan <phase17-plan.json> [--review <id>] --out <review-report.json>
```

## Input

- Phase 17 status: `LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN`
- Phase 17 downstream boundary: `local_renderer_manifest_adapter_contract_plan_only_not_adapter_implementation`
- Phase 17 next step: `local_renderer_manifest_adapter_contract_plan_review`
- Sample input path: `samples/local_story_input/rain_gate_local_renderer_manifest_adapter_contract_plan_sample.json`

## Output

- Output status: `LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_REVIEW_PASS` or FAIL
- Sample output path: `samples/local_story_input/rain_gate_local_renderer_manifest_adapter_contract_plan_review_report_sample.json`
- Downstream boundary: `local_renderer_manifest_adapter_contract_plan_review_only_not_adapter_implementation`
- Next allowed step: `local_renderer_manifest_adapter_contract_draft`

## Review Checks

The review gate validates:

- Phase 17 plan verdict/status/downstream step.
- Source ids are present.
- Phase 17 checks are non-empty and PASS.
- Phase 17 findings are absent.
- Adapter contract requirements are present.
- Adapter input mapping entries are present and complete.
- Required adapter contract work items are present.
- Verification plan is present and does not require adapter execution, renderer execution, media generation, or production approval.
- Boundary fields preserve local-only, no media generation, no provider/API, no adapter execution, no production approval, no renderer readiness, and no MP4 readiness.

## Boundary

Phase 18 must remain local file-protocol JSON only:

- review only
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

PASS only allows `local_renderer_manifest_adapter_contract_draft` as a future explicitly assigned task. That future task still must not implement or execute an adapter, run renderer/ffmpeg/TTS, generate media, claim final video readiness, perform provider/media handoff, approve a real production `ComicVideoRenderHandoffManifest`, or create production lock unless a later explicit task changes the boundary.
