# Codex Local Renderer Adapter Implementation Draft V0

Status: `LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_V0_IMPLEMENTED`

Date: 2026-07-01

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## 目的

Phase 10 是本地 JSON 實作草稿層。它讀取三個已通過前置關卡的檔案：

- Phase 5 preview handoff（預覽交接草稿）
- Phase 8 implementation plan（實作計畫）
- Phase 9 plan review report（計畫審查報告）

輸出是 `LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT` JSON。這份 JSON 只把未來 adapter implementation（轉接器實作）的工作、欄位對應草稿、驗證計畫和邊界整理成可審查的檔案。

這一步不是 adapter execution（轉接器執行），不是 real manifest generation（真實渲染清單產生），不是 renderer/ffmpeg/TTS 執行，不是影片產生，也不是 final video（最終影片）。

## 指令

```powershell
.\gradlew.bat :app:run --args="draft-local-renderer-adapter-implementation --handoff samples/local_story_input/rain_gate_story_video_preview_handoff_draft_sample.json --plan samples/local_story_input/rain_gate_local_renderer_adapter_implementation_plan_sample.json --plan-review samples/local_story_input/rain_gate_local_renderer_adapter_implementation_plan_review_report_sample.json --draft rain-gate-local-renderer-adapter-implementation-draft-v0-001 --out samples/local_story_input/rain_gate_local_renderer_adapter_implementation_draft_sample.json"
```

輸入：
- Phase 5 handoff JSON
- Phase 8 plan JSON
- Phase 9 plan review JSON

輸出：
- Phase 10 implementation draft JSON

## PASS 條件

Phase 10 PASS 需要：

- Phase 5 handoff `status=LOCAL_STORY_VIDEO_PREVIEW_HANDOFF_DRAFT`
- Phase 8 plan `verdict=PASS`
- Phase 8 plan `status=LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_DRAFT`
- Phase 8 downstream boundary `local_renderer_adapter_implementation_plan_only_not_implementation`
- Phase 8 next step `local_renderer_adapter_implementation_review`
- Phase 9 review `verdict=PASS`
- Phase 9 review `status=LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_REVIEW_PASS`
- Phase 9 downstream boundary `local_renderer_adapter_implementation_plan_review_only_not_implementation`
- Phase 9 next step `local_renderer_adapter_implementation_draft`
- story input pack id、handoff id、handoff review id、dry-run id、plan id 對齊
- local-only / no-media boundaries（本地限定、不產媒體邊界）保留
- `mediaGenerated=false`
- `rendererInvocation=none`
- `providerInvocation=none`
- `ttsInvocation=none`
- `manifestGenerated=false`
- `adapterImplementationExecuted=false`
- `rendererReady=false`
- `mp4ReadinessClaimed=false`
- Phase 8 plan 與 Phase 9 review 沒有 findings（問題項）
- preview panels、required adapter work items、implementation steps、verification plan、mapping notes 皆非空

PASS 只允許下一步：

- `local_renderer_adapter_implementation_draft_review`

## Rain Gate Sample

輸出檔：

```text
samples/local_story_input/rain_gate_local_renderer_adapter_implementation_draft_sample.json
```

樣本重點：

- `verdict=PASS`
- `status=LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT`
- `downstreamBoundary=local_renderer_adapter_implementation_draft_only_not_manifest_generation`
- `nextAllowedDownstreamStep=local_renderer_adapter_implementation_draft_review`
- `mediaGenerated=false`
- `rendererInvocation=none`
- `providerInvocation=none`
- `ttsInvocation=none`
- `manifestGenerated=false`
- `adapterImplementationExecuted=false`
- `noExternalPaidProviderTools=true`
- `rendererReady=false`
- `mp4ReadinessClaimed=false`

## 明確邊界

Phase 10 僅是 JSON draft（草稿）：

- no media generation
- no adapter execution
- no `ComicVideoRenderHandoffManifest` generation
- no renderer/ffmpeg/TTS execution
- no provider/API calls
- no web/server
- no direct SQLite writes
- no final media approval
- no provider/media handoff
- no canon / registry / release / approval
- no production lock
- no staging / commit / push

`ComicVideoRenderHandoffManifest` 在這一步只能出現在欄位對應草稿與禁止產生的邊界說明中。Phase 10 不會建立、序列化或輸出真正的 `ComicVideoRenderHandoffManifest`。
