# NPC Actor Cloud API Blueprint V1

Status: `ROUTE_SELECTED / DOCUMENTATION_ONLY / NO_LIVE_API_CALL`

Date: `2026-07-15`

## 1. 白話決策

Qwen3-8B 不再負責未來正式 NPC（非玩家角色）演出。它沒有被刪除：模型檔、llama.cpp、本機測試與
NPC-ACTOR-06～09C 證據都保留，方便日後查證；但正式路線與備援路線都不得再偷偷呼叫它。

替代方案是 OpenAI Responses API（雲端回應介面）：

- 一般重要 NPC：`gpt-5.6-terra`，`reasoning.effort=medium`；
- 關鍵人物／高風險場景：只有預先標記或評測證明 Terra 不足時，才升級
  `gpt-5.6-sol`，`reasoning.effort=high`；
- 不把 `max`、Pro mode 或每回合 Sol 當預設；
- 背景人物優先使用確定性規則；若需要自然語句，再另行評測較便宜模型。

選雲端模型是為了提高角色表演、對白和複雜情境品質，不代表模型可以接管世界。模型仍只提出候選，
引擎才是裁判。

## 2. 不變的正式安全流程

```text
SQLite 正式狀態
→ 角色可見資料篩選
→ 雲端 NPC Actor 產生嚴格 JSON 候選
→ 本機 response validator（回應驗證）
→ 物理與世界規則檢查
→ safety loop（安全迴路，最多一次消毒後修正）
→ Game Master（世界裁判）
→ 正式 Runtime Validator（執行期驗證）
→ 人類批准 Gate（關卡）
→ StateReducer（狀態套用器）與 SQLite transaction（交易）
```

雲端 API 不得：

- 直接寫 SQLite、Canon、Memory 或 Knowledge；
- 繞過 `AiTurnResultValidator`、物理檢查、Game Master 或人工批准；
- 看到作者秘密、未來劇情、其他角色私密記憶或完整資料庫；
- 因為輸出好看就自動 resolve（正式套用）。

## 3. 第一版請求契約

第一版實作應使用以下固定策略：

```text
endpoint: POST /v1/responses
default model: gpt-5.6-terra
default reasoning.effort: medium
store: false
text.format.type: json_schema
text.format.strict: true
tools: none
max_output_tokens: 依 NpcActorResponse 上限固定
API key source: OPENAI_API_KEY 環境變數
```

回傳內容必須直接符合既有 `NpcActorResponse` 與物理提案契約。禁止保存 hidden chain-of-thought
（隱藏思考過程）；只保存輸入／輸出 Hash、模型 ID、推理強度、token 用量、延遲、驗證結果與必要的
消毒後錯誤代碼。

## 4. 傳給雲端的資料邊界

只允許送出當下 NPC 真正可見且完成篩選的最小資料：

- 穩定身分、口吻與關係；
- 當下地點、可感知物件、可達行動與物理限制；
- 該角色自己的記憶與知識主張；
- 當下目標、情緒、疲勞、恐懼、債務與可隱瞞資訊；
- 必要的近期互動摘要；
- 嚴格輸出 Schema 與禁止事項。

不得送出完整 SQLite、整個世界設定包、作者專用秘密、其他人物私密記憶、未公開結局、API Key、
本機路徑或無關素材。`store=false` 是額外保護，不是放寬資料最小化的理由。

## 5. 模型路由與升級規則

| 情況 | 模型 | 推理強度 | 規則 |
| --- | --- | --- | --- |
| 一般重要 NPC 回合 | `gpt-5.6-terra` | `medium` | 預設路線 |
| 預先標記的重大劇情或代表性評測失敗 | `gpt-5.6-sol` | `high` | 記錄原因後一次升級 |
| 背景排程／可確定行動 | 不呼叫模型 | 無 | 規則優先 |
| 格式或安全失敗 | 原模型 | 原強度 | 最多一次消毒後修正，不自動升級 |

升級不能是重抽卡。相同輸入不得無上限重試，也不能在使用者看不到的情況下從 Terra 自動升到 Sol。
模型、推理強度、嘗試次數與估算成本都必須進入稽核紀錄。

## 6. 成本控制

正式 live Pilot（連線試跑）前必須設定：

- 每次請求最大輸入與輸出 token；
- 單次成本上限；
- 每日成本上限；
- 一個場景最多呼叫次數；
- Terra 升級 Sol 的明確條件；
- 超過上限立即停止，不以更便宜模型偷偷續跑。

2026-07-15 官方標準短上下文價格快照（每百萬 token）為：Terra 輸入 US$2.50、輸出 US$15；
Sol 輸入 US$5、輸出 US$30。價格與模型識別可能變動，live Pilot 前必須重新查官方價格，不能把本表
當永久報價。ChatGPT 訂閱使用量與 API 額度是分開的。

## 7. 秘密與錯誤處理

- API Key 只能從 `OPENAI_API_KEY` 讀取，不可寫入 Git、JSON、測試 fixture、日誌或錯誤訊息。
- 缺 Key、預算、網路、逾時、限流、Schema 不符或服務端錯誤時，預設安全失敗，不寫正式狀態。
- 錯誤報告只保存必要代碼、Hash 與供應者 request id；不保存作者秘密或未消毒模型原文。
- 不啟動 Web Server，不開 Port；Kotlin CLI 只作為 API client（客戶端）送出單次 HTTPS 請求。
- Provider conversation id（供應者對話識別）不能成為唯一記憶；正式狀態仍在 SQLite。

## 8. 分階段實作

### NPC-ACTOR-10A：治理與離線 Adapter 基礎

先修訂 Repository `AGENTS.md` 的舊 no-API 邊界，再建立 provider-neutral 的
`OpenAiResponsesNpcActorGateway`。只用 Fake HTTP 測試：請求映射、strict Schema、逾時、限流、拒絕、
秘密消毒、成本估算與零正式寫入。這一階段網路請求數必須是 0。

### NPC-ACTOR-10B：一次非 Canon 雲端 Pilot

必須同時具備：使用者在本機設定的 Key、單次／每日預算、已核准的角色可見資料包與明確 live-call
授權。只跑一個既有 hard-blind（高難度盲測）或物理場景，不 resolve、不升格 Canon。

### NPC-ACTOR-10C：評測與路由定案

比較 Terra／medium 與必要時 Sol／high 的角色一致性、自然對白、物理錯誤、秘密洩漏、拒絕率、延遲與
成本。沒有通過知識與正式狀態安全門檻時，不得以「比較自然」為理由上線。

## 9. 真正停止條件

- 需要付費請求但 Key、預算或 live 授權缺少；
- 要送出的資料包含作者秘密或不屬於該 NPC 的私密資料；
- 必須降低 Validator、物理、安全或人工批准門檻才能通過；
- 需要 API 直接寫正式狀態；
- 同一品質問題經一次提示修正與一次受控模型比較後仍存在。

## 10. 官方依據

- 模型與推理指引：`https://developers.openai.com/api/docs/guides/latest-model`
- Responses API：`https://developers.openai.com/api/docs/guides/migrate-to-responses`
- Structured Outputs：`https://developers.openai.com/api/docs/guides/structured-outputs`
- 價格：`https://developers.openai.com/api/docs/pricing`
- API 資料控制：`https://developers.openai.com/api/docs/guides/your-data`

模型名稱、價格與 request shape（請求格式）屬於會變動的外部資料，每次 live Pilot 前都要重新核對。
