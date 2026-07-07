# Codex Local Story Video Preview Handoff Review V0

Status: `LOCAL_STORY_VIDEO_PREVIEW_HANDOFF_REVIEW_V0_IMPLEMENTED`

Date: 2026-07-01

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## 用途

Local story video preview handoff review v0（本地故事影片預覽交接審查 v0）是 Phase 5 之後的本地 JSON review gate（審查閘門）。它只檢查 Phase 5 preview handoff draft（預覽交接草稿）是否安全、完整、仍然停在本地 file-protocol JSON（檔案協議 JSON）邊界內。

這一層不是 adapter execution（轉接器執行），不是 renderer（渲染器）執行，也不是 video generation（影片生成）。它的責任是決定 Phase 5 handoff 是否可以進入下一個受控步驟：`local_renderer_adapter_dry_run`。

## 指令

```powershell
.\gradlew.bat :app:run --args="review-local-story-video-preview-handoff --handoff samples/local_story_input/rain_gate_story_video_preview_handoff_draft_sample.json --review rain-gate-story-video-preview-handoff-review-v0-001 --out samples/local_story_input/rain_gate_story_video_preview_handoff_review_report_sample.json"
```

這個指令只會：

- 讀取 `--handoff` 指定的 Phase 5 JSON。
- 驗證 `status=LOCAL_STORY_VIDEO_PREVIEW_HANDOFF_DRAFT`。
- 驗證 `downstreamBoundary=local_story_video_preview_handoff_draft_only_not_media_generation`。
- 驗證 `nextAllowedDownstreamStep=local_preview_handoff_review`。
- 驗證所有 `requiredForDraft=true` 的 readiness checks（就緒檢查）都是 `PASS`。
- 驗證 preview plan（預覽計畫）有至少一個 panel（分鏡格）、正數 duration（時長）與零 QA failure（品質檢查失敗）。
- 驗證 local-only boundaries（本地-only 邊界）：no provider/API、no renderer invocation、no TTS invocation、no media generation、no final media approval、no release、no canon/registry/production lock。
- 保留 `localRendererCompatibility.ready=false`，把它視為誠實的「尚未能直接渲染」訊號，而不是失敗。
- 寫出 review report JSON。

它不會：

- 寫 SQLite。
- 呼叫 provider/API。
- 開 web/server。
- 執行 renderer/ffmpeg/TTS。
- 產生影片或 MP4。
- 產生 `ComicVideoRenderHandoffManifest`。
- 做 final media approval（最終媒體批准）。
- 做 provider/media handoff（供應商或媒體交接）。
- 做 canon、registry、release、approval、production lock（正典、登錄、發布、批准、製作鎖定）。

## Rain Gate 樣本

輸入 handoff draft：

```text
samples/local_story_input/rain_gate_story_video_preview_handoff_draft_sample.json
```

輸出 review report：

```text
samples/local_story_input/rain_gate_story_video_preview_handoff_review_report_sample.json
```

Rain Gate Phase 6 sample 的結果：

- `verdict=PASS`
- `handoffStatus=LOCAL_STORY_VIDEO_PREVIEW_HANDOFF_DRAFT`
- `checks=10`
- `checksPass=10`
- `checksFail=0`
- `requiredReadinessPass=9`
- `requiredReadinessFail=0`
- `previewPanels=2`
- `targetDurationMillis=9000`
- `qaFail=0`
- `localRendererReady=false`
- `mediaGenerated=false`
- `rendererInvocation=none`
- `providerInvocation=none`
- `ttsInvocation=none`
- `noExternalPaidProviderTools=true`
- `downstreamBoundary=local_story_video_preview_handoff_review_only_not_media_generation`
- `nextAllowedDownstreamStep=local_renderer_adapter_dry_run`

## 為什麼 ready=false 仍然可以 PASS

Phase 6 的審查重點不是「可以直接產 MP4」，而是「Phase 5 handoff 是否足夠乾淨，可以交給下一個本地 adapter dry-run（轉接器乾跑）任務」。因此 `localRendererCompatibility.ready=false` 是預期狀態。

PASS 只代表：

- Phase 5 JSON 來源完整。
- 目前仍是 local JSON only（只用本地 JSON）。
- 沒有 provider/API、renderer、TTS 或 media generation。
- preview plan 和 QA summary 可以被下一步審查。
- 下一步只能是 `local_renderer_adapter_dry_run`。

PASS 不代表：

- final video（最終影片）已完成。
- media generation（媒體生成）已發生。
- final media approval 已完成。
- provider/media handoff 已完成。
- canon、registry、release、approval 或 production lock 已完成。

## 邊界

Phase 6 必須維持：

- file-protocol JSON only。
- no external paid/provider tools。
- no provider/API。
- no web/server。
- no renderer/ffmpeg/TTS execution。
- no media generation。
- no adapter execution。
- no `ComicVideoRenderHandoffManifest` generation。
- no final media approval。
- no provider/media handoff。
- no canon、registry、release、approval、production lock。
- no staging、commit、push，除非 01 或使用者另外明確批准。
