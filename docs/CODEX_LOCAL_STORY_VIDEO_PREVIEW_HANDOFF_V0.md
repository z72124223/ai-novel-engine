# Codex Local Story Video Preview Handoff V0

Status: `LOCAL_STORY_VIDEO_PREVIEW_HANDOFF_V0_IMPLEMENTED`

Date: 2026-07-01

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## 用途

Local story video preview handoff v0（本地故事影片預覽交接草稿 v0）是 Phase 4 之後的本地 JSON 交接層。

Phase 4 已證明 story input pack（故事輸入包）可以一鍵產出 validation report（驗證報告）、asset / prompt cards（素材卡與提示詞卡）、local render package（本地渲染包草稿）與 chain report（鏈式報告）。Phase 5 則把這些已驗證 JSON 整理成一份 review-only preview handoff（僅供審查的預覽交接草稿），讓下一個任務可以檢查是否要轉成真正 local preview renderer（本地預覽渲染器）可用的輸入。

這一步不是影片生成，也不是 local MP4 generation（本地 MP4 生成）。

## 新命令

```powershell
.\gradlew.bat :app:run --args="draft-local-story-video-preview-handoff --chain-report samples/local_story_input/rain_gate_story_video_chain_report_sample.json --render-package samples/local_story_input/rain_gate_story_video_chain_outputs/local_render_package_draft.json --asset-prompt-cards samples/local_story_input/rain_gate_story_video_chain_outputs/local_asset_prompt_cards_draft.json --story-input samples/local_story_input/rain_gate_story_input_pack_sample.json --handoff rain-gate-story-video-preview-handoff-v0-001 --out samples/local_story_input/rain_gate_story_video_preview_handoff_draft_sample.json"
```

這個命令只做 file-protocol JSON（檔案協議 JSON）整理：

- 讀取明確指定的 chain report JSON。
- 讀取明確指定的 render package JSON。
- 讀取明確指定的 asset/prompt cards JSON。
- 讀取明確指定的 story input JSON。
- 要求 chain report status 是 `LOCAL_STORY_VIDEO_CHAIN_VERIFIED`。
- 要求 render package status 是 `LOCAL_RENDER_PACKAGE_DRAFT`。
- 要求 asset/prompt cards status 是 `LOCAL_ASSET_PROMPT_CARDS_DRAFT`。
- 要求來源 story/source ids 一致。
- 輸出 preview handoff draft JSON。
- 不寫 SQLite。
- 不呼叫 provider/API。
- 不開 web/server。
- 不執行 renderer/ffmpeg/TTS。
- 不產生圖片、音訊或影片。

## Rain Gate 範例輸出

Preview handoff draft：

```text
samples/local_story_input/rain_gate_story_video_preview_handoff_draft_sample.json
```

目前 Rain Gate Phase 5 sample（樣本）結果：

- `status=LOCAL_STORY_VIDEO_PREVIEW_HANDOFF_DRAFT`。
- `sourceChainStatus=LOCAL_STORY_VIDEO_CHAIN_VERIFIED`。
- `sourceRenderPackageStatus=LOCAL_RENDER_PACKAGE_DRAFT`。
- `sourceAssetPromptCardsStatus=LOCAL_ASSET_PROMPT_CARDS_DRAFT`。
- `readinessChecks=9`。
- `readinessPass=9`。
- `readinessFail=0`。
- `sceneCount=1`。
- `panelCount=2`。
- `targetDurationMillis=9000`。
- `localRendererCompatibility.ready=false`。
- `nextAllowedDownstreamStep=local_preview_handoff_review`。

## V0 輸出內容

輸出的 handoff draft（交接草稿）包含：

- `readinessChecks`：檢查明確來源檔、Phase 4 chain status、Phase 3 render package status、Phase 2 cards status、來源 id 一致、render plan 存在與本地邊界。
- `previewPlan`：整理 scene ordering（場景順序）、panel ordering（面板順序）、target durations（目標時長）、caption/dialogue cues（字幕/對白提示）、asset prompt references（素材與提示詞引用）、audio cue references（聲音提示引用）與 QA summary（品質檢查摘要）。
- `localRendererCompatibility`：明確說明哪些欄位已準備好、哪些欄位仍缺、實際產生 MP4 前必須做哪些檢查。
- `boundaries`：明確保留 no provider/API、no renderer invocation、no TTS invocation、no media generation、no final media approval、no release、no canon/registry/production lock。

## 為什麼 localRendererCompatibility.ready 是 false

目前既有 local renderer（本地渲染器）吃的是 `ComicVideoRenderHandoffManifest`。Phase 5 輸出的則是 `LocalStoryVideoPreviewHandoffDraft`。

因此 Phase 5 不假裝它已經可以直接丟進 renderer。它只把 story-video chain 的資料整理到足夠明確，讓下一個任務可以選擇：

- 做 read-only review（唯讀審查）。
- 建立 adapter（轉接器）把 handoff draft 轉成 `ComicVideoRenderHandoffManifest`。
- 先跑 dry-run renderer/preflight（乾跑渲染器或預檢），再考慮真正產生 MP4。

## 對使用者的意義

這一步把「離影片更近」這件事做成可審查、可停止、可重跑的本地 JSON 關卡。它不直接產生影片，但它減少了下一輪產生本地預覽 MP4 前的人工整理工作。

也就是說，現在路線是：

```text
故事輸入 JSON
  -> 本地驗證
  -> 素材/提示詞卡
  -> 本地渲染包草稿
  -> 本地鏈式驗證
  -> 本地預覽交接草稿
  -> 未來審查/轉接/乾跑
  -> 未來明確批准後才可能產生本地 MP4
```

## 邊界

- 不是媒體生成。
- 不是最終影片。
- 不是 final media approval（最終媒體批准）。
- 不是 provider/media handoff（供應商或媒體交接）。
- 不執行 renderer（渲染器）。
- 不執行 ffmpeg。
- 不執行 TTS（文字轉語音）。
- 不呼叫 provider/API。
- 不使用外部 paid/provider tools（付費或供應商工具）。
- 不寫 SQLite。
- 不開 web/server。
- 不是 release（正式釋出）。
- 不是 approval（批准）。
- 不是 canon（正典）。
- 不是 registry（登錄）。
- 不是 production lock（生產鎖定）。
- 不允許 staging、commit 或 push，除非 01 或使用者另外明確指派。
