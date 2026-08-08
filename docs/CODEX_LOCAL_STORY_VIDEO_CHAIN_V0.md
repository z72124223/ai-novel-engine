# Codex Local Story Video Chain V0

Status: `LOCAL_STORY_VIDEO_CHAIN_V0_IMPLEMENTED`

Date: 2026-07-01

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## 用途

Local story video chain v0（本地故事到影片鏈式驗證 v0）是 Phase 1、Phase 2、Phase 3 之後的本地自動串接層。

它用一個命令讀取 story input pack（故事輸入包）JSON，依序產出：

- story validation report（故事驗證報告）。
- local asset / prompt cards draft（本地素材卡與提示詞卡草稿）。
- local render package / QA draft（本地渲染包與品質檢查草稿）。
- chain report（鏈式驗證報告）。

這一步只證明「本地 JSON 工作流可以從故事輸入穩定走到渲染包草稿」。它不是影片生成，也不是外部 provider workflow（供應商工作流）。

## 新命令

```powershell
.\gradlew.bat :app:run --args="verify-local-story-video-chain --story-input samples/local_story_input/rain_gate_story_input_pack_sample.json --output-dir samples/local_story_input/rain_gate_story_video_chain_outputs --out samples/local_story_input/rain_gate_story_video_chain_report_sample.json"
```

這個命令會：

- 只讀取 `--story-input` 指定的 story input JSON。
- 在記憶體中執行 local story input pack validation（本地故事輸入包驗證）。
- 若驗證為 `PASS`，繼續產出 local asset / prompt cards draft。
- 若 cards 狀態為 `LOCAL_ASSET_PROMPT_CARDS_DRAFT`，繼續產出 local render package draft。
- 將中間 JSON 輸出到 `--output-dir`。
- 將 chain report 寫到 `--out`。
- 在 stdout（終端輸出）印出簡短摘要。
- 任一階段失敗時停止鏈路並回傳 nonzero exit code（非零結束碼）。

## Rain Gate 範例輸出

鏈式報告：

```text
samples/local_story_input/rain_gate_story_video_chain_report_sample.json
```

中間輸出目錄：

```text
samples/local_story_input/rain_gate_story_video_chain_outputs/
```

目前 Rain Gate chain sample（鏈式樣本）結果：

- `status=LOCAL_STORY_VIDEO_CHAIN_VERIFIED`。
- `storyValidation=PASS`。
- `assetPromptCards=LOCAL_ASSET_PROMPT_CARDS_DRAFT`。
- `localRenderPackage=LOCAL_RENDER_PACKAGE_DRAFT`。
- `characters=2`、`scenes=1`、`beats=2`。
- `visualPromptCards=2`、`audioCueCards=2`、`panelPlans=2`。
- `qaChecks=7`、`qaPass=7`、`qaFail=0`。
- `targetDurationMillis=9000`。
- `nextAllowedDownstreamStep=local_chain_review`。

## Chain Report 邊界

chain report 會明確保留這些 boundary（邊界）欄位：

- `fileProtocolJsonOnly=true`。
- `noExternalPaidProviderTools=true`。
- `providerInvocation=none`。
- `ttsInvocation=none`。
- `mediaGenerated=false`。
- `rendererInvocation=none`。
- `directSqliteWrites=false`。
- `providerApiCalls=false`。
- `finalMediaApproval=false`。
- `release=false`。
- `canonRegistryOrProductionLock=false`。
- `downstreamBoundary=local_story_video_chain_verified_only_not_media_generation`。

## 對使用者的意義

這一步讓 Codex 可以把「使用者世界觀 / 故事輸入」轉成一條可重跑、可審查的本地 JSON 生產線。使用者不用手動分別跑三個命令，也可以清楚看到每個階段產出了什麼、是否通過、下一步允許做什麼。

它讓專案更接近可重複的 local workbench（本地工作台）：先在本地把故事、卡片、渲染草稿與 QA 報告整理好，再由 01 或使用者決定是否進入下一個審查或渲染相關任務。

## 邊界

- 不是媒體生成。
- 不是最終影片。
- 不執行 renderer（渲染器）。
- 不執行 ffmpeg。
- 不執行 TTS（文字轉語音）。
- 不呼叫 provider/API。
- 不使用外部 paid/provider tools（付費或供應商工具）。
- 不寫 SQLite。
- 不開 web/server。
- 不是 provider/media handoff（供應商或媒體交接）。
- 不是 final media approval（最終媒體批准）。
- 不是 release（正式釋出）。
- 不是 canon（正典）。
- 不是 registry（登錄）。
- 不是 production lock（生產鎖定）。
- 不允許 staging、commit 或 push，除非 01 或使用者另外明確指派。
