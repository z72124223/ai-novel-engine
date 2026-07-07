# Codex Local Asset / Prompt Cards V0

Status: `LOCAL_ASSET_PROMPT_CARDS_V0_IMPLEMENTED`

Date: 2026-07-01

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## 用途

Local asset / prompt cards v0（本地素材卡與提示詞卡 v0）是 validated story input pack（已驗證故事輸入包）之後的第二個本地 JSON 層。

它把使用者的故事輸入整理成可審查的角色卡、場景卡、beat cards（節拍卡）、visual prompt cards（視覺提示詞卡）、audio cue cards（聲音提示卡）與 render intent（渲染意圖）。這一步只做本地資料整理，不呼叫外部工具，也不生成媒體。

## 新命令

```powershell
.\gradlew.bat :app:run --args="draft-local-asset-prompt-cards --story-input samples/local_story_input/rain_gate_story_input_pack_sample.json --story-validation samples/local_story_input/rain_gate_story_input_pack_validation_sample.json --out samples/local_story_input/rain_gate_asset_prompt_cards_draft_sample.json"
```

這個命令只做 file-protocol JSON（檔案協議 JSON）轉換：

- 讀取 story input pack JSON。
- 讀取 story validation report JSON。
- 要求 validation report 的 `verdict` 是 `PASS`。
- 要求 validation report 的 `packId` 與 story input pack 的 `packId` 一致。
- 輸出 asset/prompt cards draft JSON。
- 不寫 SQLite。
- 不呼叫 provider/API。
- 不開 web/server。
- 不產生圖片、音訊或影片。

## V0 輸出內容

輸出的 draft（草稿）至少包含：

- `status=LOCAL_ASSET_PROMPT_CARDS_DRAFT`。
- `sourceStoryInputPackId` 與 `sourceStoryValidationId`。
- `localOnlyConstraints`，保留 local-only（只在本地處理）、no external paid/provider tools（不使用外部付費或供應商工具）、file-protocol JSON only（只使用檔案協議 JSON）。
- `characterCards`，由故事角色轉出，保留 visual cue（視覺提示）與 voice cue（聲音提示）。
- `sceneCards`，由故事場景轉出，保留地點、氣氛與可見情況。
- `beatCards`，由故事節拍轉出，連回 scene（場景）與 character cards（角色卡）。
- `visualPromptCards`，只包含本地 text prompt seeds（文字提示詞種子）與 negative boundary notes（負向邊界註記），`providerInvocation=none`。
- `audioCueCards`，只包含聲音意圖與 voice cue 摘要，`ttsInvocation=none`。
- `renderIntent`，只作為後續本地渲染包規劃的摘要。
- `boundaries`，明確記錄 no provider/API、no media generation、no final media approval、no release、no canon/registry、no production lock。

## 目前範例

故事輸入：

```text
samples/local_story_input/rain_gate_story_input_pack_sample.json
```

故事驗證報告：

```text
samples/local_story_input/rain_gate_story_input_pack_validation_sample.json
```

卡片草稿輸出：

```text
samples/local_story_input/rain_gate_asset_prompt_cards_draft_sample.json
```

Rain Gate 範例會產出 2 張角色卡、1 張場景卡、2 張節拍卡、2 張視覺提示詞卡、2 張聲音提示卡，以及 9000 ms 的本地渲染意圖摘要。

## 對使用者的意義

這一步讓 Codex 可以把使用者的世界觀與故事輸入，轉成下一階段更容易審查、修改與組裝的本地卡片。也就是說，現在不只是「有一份故事 JSON」，而是能拆成角色、場景、節拍、畫面提示、聲音提示與渲染規劃種子。

它仍然遵守 no external paid/provider tools（不使用外部付費或供應商工具）的路線。外部工具可以等到變現或資金後再加入；目前不需要它們來把本地 demo 做強。

## 邊界

- 不是媒體生成。
- 不是最終媒體批准。
- 不是外部 provider/API handoff（供應商或 API 交接）。
- 不是 TTS（文字轉語音）執行。
- 不是 approved asset（已批准素材）。
- 不是 release（正式釋出）。
- 不是 canon（正典）。
- 不是 registry（登錄）。
- 不是 production lock（生產鎖定）。
- 不允許直接寫 SQLite。
- 不允許 web/server。
- 不允許 staging、commit 或 push，除非 01 或使用者另外明確指派。
