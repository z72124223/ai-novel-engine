# Codex Local Renderer Adapter Dry-Run V0

Status: `LOCAL_RENDERER_ADAPTER_DRY_RUN_V0_IMPLEMENTED`

Date: 2026-07-01

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## 用途

Local renderer adapter dry-run v0（本地渲染器轉接器乾跑 v0）是 Phase 6 之後的本地 JSON dry-run gate（乾跑閘門）。它檢查 Phase 5 preview handoff draft（預覽交接草稿）和 Phase 6 review report（審查報告）是否能安全地進入未來 adapter implementation plan（轉接器實作計畫）。

這一層只做 mappability check（可映射性檢查）。它不實作 adapter（轉接器），不執行 renderer（渲染器），不產生 `ComicVideoRenderHandoffManifest`，不產生影片，也不宣稱 renderer readiness（渲染器就緒）或 MP4 readiness（MP4 就緒）。

## 指令

```powershell
.\gradlew.bat :app:run --args="dry-run-local-renderer-adapter --handoff samples/local_story_input/rain_gate_story_video_preview_handoff_draft_sample.json --review samples/local_story_input/rain_gate_story_video_preview_handoff_review_report_sample.json --dry-run-id rain-gate-local-renderer-adapter-dry-run-v0-001 --out samples/local_story_input/rain_gate_local_renderer_adapter_dry_run_report_sample.json"
```

這個指令只會：

- 讀取 Phase 5 handoff JSON。
- 讀取 Phase 6 review report JSON。
- 驗證 Phase 5 狀態仍是 `LOCAL_STORY_VIDEO_PREVIEW_HANDOFF_DRAFT`。
- 驗證 Phase 6 `verdict=PASS`。
- 驗證兩份 JSON 的 handoff/story id 一致。
- 驗證 no provider/API、no renderer invocation、no TTS invocation、no media generation、no final media approval、no release、no canon/registry/production lock。
- 檢查 preview plan（預覽計畫）是否有正數 duration（時長）、非空 panel（分鏡格）、panel id、scene/beat id、caption/dialogue cue、visual prompt reference、audio cue reference、QA zero fail。
- 寫出 dry-run report JSON。

它不會：

- 寫 SQLite。
- 呼叫 provider/API。
- 開 web/server。
- 執行 renderer/ffmpeg/TTS。
- 產生 media generation。
- 產生影片或 MP4。
- 產生 `ComicVideoRenderHandoffManifest`。
- 實作 adapter。
- 做 final media approval。
- 做 provider/media handoff。
- 做 canon、registry、release、approval、production lock。

## Rain Gate 樣本

輸入 Phase 5 handoff draft：

```text
samples/local_story_input/rain_gate_story_video_preview_handoff_draft_sample.json
```

輸入 Phase 6 review report：

```text
samples/local_story_input/rain_gate_story_video_preview_handoff_review_report_sample.json
```

輸出 Phase 7 dry-run report：

```text
samples/local_story_input/rain_gate_local_renderer_adapter_dry_run_report_sample.json
```

Rain Gate Phase 7 sample 的結果：

- `verdict=PASS`
- `status=LOCAL_RENDERER_ADAPTER_DRY_RUN_PASS`
- `checks=9`
- `checksPass=9`
- `checksFail=0`
- `sceneCount=1`
- `panelCount=2`
- `mappedPanelCount=2`
- `unmappedPanelCount=0`
- `missingRequiredFieldCount=0`
- `targetDurationMillis=9000`
- `qaFail=0`
- `localRendererReady=false`
- `manifestGenerated=false`
- `mediaGenerated=false`
- `rendererInvocation=none`
- `providerInvocation=none`
- `ttsInvocation=none`
- `noExternalPaidProviderTools=true`
- `downstreamBoundary=local_renderer_adapter_dry_run_only_not_media_generation`
- `nextAllowedDownstreamStep=local_renderer_adapter_implementation_plan`

## PASS 代表什麼

PASS 只代表：

- Phase 5/6 JSON 來源互相一致。
- 目前仍然是 local file-protocol JSON only（只用本地檔案協議 JSON）。
- 沒有 provider/API、renderer、TTS 或 media generation。
- panel/cue/prompt/audio/QA 資料足以支撐下一步 adapter implementation plan。
- 下一步只能是 `local_renderer_adapter_implementation_plan`。

PASS 不代表：

- adapter 已經實作。
- `ComicVideoRenderHandoffManifest` 已經生成。
- renderer 可以執行。
- MP4 可以直接生成。
- final video（最終影片）已完成。
- final media approval 已完成。
- provider/media handoff 已完成。
- canon、registry、release、approval 或 production lock 已完成。

## 邊界

Phase 7 必須維持：

- file-protocol JSON only。
- no external paid/provider tools。
- no provider/API。
- no web/server。
- no renderer/ffmpeg/TTS execution。
- no media generation。
- no adapter implementation execution。
- no `ComicVideoRenderHandoffManifest` generation。
- no final media approval。
- no provider/media handoff。
- no canon、registry、release、approval、production lock。
- no staging、commit、push，除非 01 或使用者另外明確批准。
