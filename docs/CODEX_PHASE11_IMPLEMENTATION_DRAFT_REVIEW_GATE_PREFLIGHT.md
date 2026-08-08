# Codex Phase 11 Implementation Draft Review Gate Preflight

Status: `PHASE11_PREFLIGHT_DRAFT_FOR_01`

Date: 2026-07-01

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## Purpose

這份文件是 Phase 11 的 preflight packet（預檢包）。它只替下一個 code slice（程式碼切片）定義 review gate（審查關卡）的形狀，不實作 Kotlin code（Kotlin 程式碼），不跑測試，不產生 sample，不執行 renderer/ffmpeg/TTS，也不產生媒體。

Phase 10 已產出 `LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT`。Phase 11 的唯一目標應該是審查這份 draft JSON，確認它仍是本地 JSON 草稿，且沒有跳到 adapter execution（轉接器執行）、manifest generation（渲染清單產生）或影片產出。

## Proposed Command

```text
review-local-renderer-adapter-implementation-draft --draft <phase10-draft.json> [--review <id>] --out <review-report.json>
```

## Required Input

- Required input status: `LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT`
- Required Phase 10 boundary: `local_renderer_adapter_implementation_draft_only_not_manifest_generation`
- Required next step: `local_renderer_adapter_implementation_draft_review`

## Proposed Output

- Expected output status proposal: `LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_PASS` or FAIL
- Expected downstream boundary proposal: `local_renderer_adapter_implementation_draft_review_only_not_manifest_generation`
- Expected next step proposal after PASS: `local_renderer_manifest_draft_plan`

## Future Fail-Stop Checks

Phase 11 implementation should fail-stop if any required evidence is missing or unsafe:

- checks list is empty, or any required check is not PASS
- implementation tasks are empty
- mapping draft is empty
- verification plan is empty
- boundary flags are missing or unsafe
- `rendererReady=false` is not preserved
- `mp4ReadinessClaimed=false` is not preserved
- `manifestGenerated=false` is not preserved
- `adapterImplementationExecuted=false` is not preserved
- `mediaGenerated=false` is not preserved
- findings are present
- `providerInvocation=none` is not preserved
- `ttsInvocation=none` is not preserved
- `rendererInvocation=none` is not preserved
- `noExternalPaidProviderTools=true` is not preserved

## Explicit Non-Goals

Phase 11 preflight and the next Phase 11 review gate must not claim or perform:

- no adapter execution
- no real `ComicVideoRenderHandoffManifest`
- no renderer/ffmpeg/TTS
- no media/final video
- no provider/API
- no direct SQLite writes
- no final media approval
- no provider/media handoff
- no canon/registry/release/approval/production lock
- no staging/commit/push

## Recommended Next Code Slice

Recommended next task:

Implement Phase 11 local renderer adapter implementation draft review gate v0 with model/service/CLI/tests/sample/docs, using the command proposal above, and keep it file-protocol JSON only. The next slice should not generate `ComicVideoRenderHandoffManifest`; it should only review whether a future `local_renderer_manifest_draft_plan` may be considered after PASS.
