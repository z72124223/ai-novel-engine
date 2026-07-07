# Codex Local Renderer Manifest Adapter Contract Draft V0

Status: `LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_DRAFT`

Date: 2026-07-02

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## Purpose

Phase 19 is a local file-protocol JSON draft layer. It reads the accepted Phase 17 adapter contract plan and the accepted Phase 18 adapter contract plan review report, then writes a draft adapter contract artifact for a future review gate.

This is a draft contract artifact only. It is not adapter implementation, not adapter execution, not renderer execution, not ffmpeg/TTS execution, not media generation, not final video, not real production `ComicVideoRenderHandoffManifest` generation or approval, not final media approval, not provider/media handoff, not canon, not registry, not release, not approval, and not production lock.

## Command

```text
draft-local-renderer-manifest-adapter-contract --plan <phase17-plan.json> --plan-review <phase18-review-report.json> [--draft <id>] --out <contract-draft.json>
```

## Input

- Phase 17 status: `LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN`
- Phase 17 downstream boundary: `local_renderer_manifest_adapter_contract_plan_only_not_adapter_implementation`
- Phase 17 next step: `local_renderer_manifest_adapter_contract_plan_review`
- Phase 18 status: `LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_REVIEW_PASS`
- Phase 18 downstream boundary: `local_renderer_manifest_adapter_contract_plan_review_only_not_adapter_implementation`
- Phase 18 next step: `local_renderer_manifest_adapter_contract_draft`
- Sample plan path: `samples/local_story_input/rain_gate_local_renderer_manifest_adapter_contract_plan_sample.json`
- Sample review path: `samples/local_story_input/rain_gate_local_renderer_manifest_adapter_contract_plan_review_report_sample.json`

## Output

- Output status: `LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_DRAFT` or FAIL
- Sample output path: `samples/local_story_input/rain_gate_local_renderer_manifest_adapter_contract_draft_sample.json`
- Downstream boundary: `local_renderer_manifest_adapter_contract_draft_only_not_adapter_execution`
- Next allowed step: `local_renderer_manifest_adapter_contract_draft_review`

The output includes:

- required inputs
- normalized field mapping
- adapter obligations
- fail-stop rules
- renderer invocation prohibition
- verification requirements
- known non-goals
- local-only/no-media/no-provider/no-production/no-readiness boundary fields

## Boundary

Phase 19 must remain local file-protocol JSON only:

- draft contract only
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

- `contractDraftGenerated=true`
- `mediaGenerated=false`
- `rendererInvocation=none`
- `providerInvocation=none`
- `ttsInvocation=none`
- `adapterImplementationExecuted=false`
- `adapterExecuted=false`
- `productionManifestGenerated=false`
- `productionReady=false`
- `productionApproved=false`
- `noExternalPaidProviderTools=true`
- `rendererReady=false`
- `mp4ReadinessClaimed=false`

## Verification Evidence

- Focused tests PASS with `-Pkotlin.daemon.jvmargs=-Xmx4096m`:
  - `com.localnovel.engine.LocalRendererManifestAdapterContractDraftServiceTest`
  - `com.localnovel.engine.LocalRendererManifestAdapterContractDraftCliTest`
- Sample CLI generation PASS.
- UTF-8 JSON parse PASS for status, downstream boundary, next allowed step, counts, findings, and required boundary fields.

## Next Step

PASS only allows `local_renderer_manifest_adapter_contract_draft_review` as a future explicitly assigned task. That future task still must not implement or execute an adapter, run renderer/ffmpeg/TTS, generate media, claim final video readiness, perform provider/media handoff, approve a real production `ComicVideoRenderHandoffManifest`, or create production lock unless a later explicit task changes the boundary.
