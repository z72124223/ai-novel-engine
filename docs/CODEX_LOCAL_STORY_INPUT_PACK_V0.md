# Codex Local Story Input Pack V0

Status: `LOCAL_STORY_INPUT_PACK_V0_IMPLEMENTED`

Date: 2026-07-01

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## 用途

Local story input pack v0（本地故事輸入包 v0）是使用者世界觀與故事想法進入本地 comic-video workbench（有聲漫畫工作台）的第一個 JSON 閘門。

它解決的是「先把想法整理成可驗證的本地資料」，不是直接產生影片。這讓後續階段可以再把通過驗證的故事輸入轉成素材卡、提示詞卡、分鏡、聲音時間線與本地渲染包。

## 新命令

```powershell
.\gradlew.bat :app:run --args="validate-local-story-input-pack --input samples/local_story_input/rain_gate_story_input_pack_sample.json --out samples/local_story_input/rain_gate_story_input_pack_validation_sample.json"
```

這個命令只做 file-protocol JSON（檔案協議 JSON）驗證：

- 讀取 story input pack JSON。
- 在記憶體中驗證必要欄位與邊界。
- 輸出 validation report JSON。
- 不寫 SQLite。
- 不呼叫 provider/API。
- 不開 web/server。
- 不產生媒體。

## V0 驗證範圍

目前 validator（驗證器）至少檢查：

- `packId`、`title`、`locale` 必須存在。
- 至少一個角色，且每個角色要有 `id`、`name`、`role`、`visualCue`、`voiceCue`。
- 至少一個場景，且每個場景要有 `id`、`title`、`location`、`mood`。
- 至少一個 beat（節拍），且 beat 要連到既有場景並包含 `narrativeIntent` 與 `dialogueIntent`。
- `durationTargetMillis` 必須大於 0，且不超過 60000 ms。
- `constraints` 必須明確宣告 local-only（只在本地處理）、no external paid/provider tools（不使用外部付費或供應商工具）、file-protocol JSON only（只使用檔案協議 JSON）。
- pack 文字不得宣稱 final media approval（最終媒體批准）、release（正式釋出）、canon（正典）、registry（登錄）、production lock（生產鎖定）或已執行 provider/API。

## 目前範例

範例輸入：

```text
samples/local_story_input/rain_gate_story_input_pack_sample.json
```

驗證報告：

```text
samples/local_story_input/rain_gate_story_input_pack_validation_sample.json
```

Rain Gate 範例描述一個 9 秒本地 demo 片段所需的世界前提、角色、場景、兩個節拍、視覺風格、聲音風格與本地限制。

## 對使用者的意義

這一步讓使用者之後可以把自己的世界觀、角色、場景與劇情意圖交給 Codex，先整理成可驗證的本地 JSON。通過後，下一階段才適合把它轉成 local asset / prompt cards（本地素材卡與提示詞卡），再往 local render package and QA（本地渲染包與品質檢查）前進。

這仍然是 no external paid/provider tools（不使用外部付費或供應商工具）的路線。外部工具可以等到變現或資金後再加入；目前不需要它們來把本地 demo 變強。

## 邊界

- 不是影片生成。
- 不是最終媒體批准。
- 不是外部製作線。
- 不是 release（正式釋出）。
- 不是 canon（正典）。
- 不是 registry（登錄）。
- 不是 production lock（生產鎖定）。
- 不允許直接寫 SQLite。
- 不允許 provider/API calls。
- 不允許 web/server。
- 不允許 staging、commit 或 push，除非 01 或使用者另外明確指派。
