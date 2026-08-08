# Codex Local Render Package / QA Draft V0

Status: `LOCAL_RENDER_PACKAGE_V0_IMPLEMENTED`

Date: 2026-07-01

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## 用途

Local render package / QA draft v0（本地渲染包與品質檢查草稿 v0）是 local story input pack（本地故事輸入包）與 local asset / prompt cards（本地素材卡與提示詞卡）之後的第三個本地 JSON 層。

它把已整理的故事與卡片轉成 reviewable local render package（可審查的本地渲染包草稿）：包含 timeline plan（時間軸計畫）、scene plan（場景計畫）、panel plan（面板計畫）、asset prompt references（素材與提示詞引用）與 QA checklist（品質檢查清單）。

這一步不呼叫 renderer（渲染器）、不產生圖片、音訊或影片，也不接外部 paid/provider tools（付費或供應商工具）。

## 新命令

```powershell
.\gradlew.bat :app:run --args="draft-local-render-package --story-input samples/local_story_input/rain_gate_story_input_pack_sample.json --asset-prompt-cards samples/local_story_input/rain_gate_asset_prompt_cards_draft_sample.json --out samples/local_story_input/rain_gate_local_render_package_draft_sample.json"
```

這個命令只做 file-protocol JSON（檔案協議 JSON）轉換：

- 讀取 story input pack JSON。
- 讀取 asset/prompt cards draft JSON。
- 要求 cards 的 `status` 是 `LOCAL_ASSET_PROMPT_CARDS_DRAFT`。
- 要求 cards 的 `sourceStoryInputPackId` 與 story input pack 的 `packId` 一致。
- 輸出 local render package draft JSON。
- 不寫 SQLite。
- 不呼叫 provider/API。
- 不開 web/server。
- 不產生圖片、音訊或影片。

## V0 輸出內容

輸出的 draft（草稿）至少包含：

- `status=LOCAL_RENDER_PACKAGE_DRAFT`。
- `sourceStoryInputPackId`。
- `sourceAssetPromptCardsStatus=LOCAL_ASSET_PROMPT_CARDS_DRAFT`。
- `timelinePlan`，由 story beats（故事節拍）與 `durationTargetMillis` deterministic（可重現）切分。
- `scenePlan`，整理場景與面板關係。
- `panelPlan`，整理每個 beat 對應的 panel、duration、visual prompt card 與 audio cue card。
- `assetPromptReferences`，連到角色卡、場景卡、節拍卡、視覺提示詞卡與聲音提示卡。
- `qaChecklist`，檢查 story pack match、card status、duration positive、no provider/API、no media generation、no final approval/release/production lock。
- `renderBoundary=local_render_package_draft_only_not_media_generation`。
- `downstreamBoundary=local_render_package_draft_only`。

## 目前範例

故事輸入：

```text
samples/local_story_input/rain_gate_story_input_pack_sample.json
```

素材與提示詞卡：

```text
samples/local_story_input/rain_gate_asset_prompt_cards_draft_sample.json
```

本地渲染包草稿：

```text
samples/local_story_input/rain_gate_local_render_package_draft_sample.json
```

Rain Gate 範例會產出 2 個 timeline items（時間軸項目）、1 個 scene plan、2 個 panel plans、2 組 asset prompt references、7 個 QA checks，總長度 9000 ms，`qaFailCount=0`。

## 對使用者的意義

這一步讓 Codex 不只是整理故事與提示詞，而是能把它們包成一份「之後可以餵給本地渲染流程或未來 adapter（轉接器）」的本地計畫。它把畫面、聲音、時間、場景與 QA 邊界放在同一份 JSON 裡，方便下一輪審查或下一階段轉換。

它仍然遵守 no external paid/provider tools（不使用外部付費或供應商工具）的路線。外部工具可以等到變現或資金後再加入；目前不需要它們來把本地 demo 做強。

## 邊界

- 不是媒體生成。
- 不是最終影片。
- 不是最終媒體批准。
- 不是外部 provider/API handoff（供應商或 API 交接）。
- 不是 TTS（文字轉語音）執行。
- 不是 local preview MP4。
- 不是 release（正式釋出）。
- 不是 canon（正典）。
- 不是 registry（登錄）。
- 不是 production lock（生產鎖定）。
- 不允許直接寫 SQLite。
- 不允許 web/server。
- 不允許 staging、commit 或 push，除非 01 或使用者另外明確指派。
