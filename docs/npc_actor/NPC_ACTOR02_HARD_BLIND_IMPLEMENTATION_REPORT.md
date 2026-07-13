# NPC-ACTOR-02 硬式多輪盲測實作報告

狀態：`TECHNICAL_PASS / HUMAN_BLIND_REVIEW_PENDING / LIVE_API_BLOCKED`

日期：2026-07-13

分支：`codex/npc-actor02-hard-blind-multiturn`

## 完成內容

本包建立 12 個全新、非 Canon 的多輪對話情境，涵蓋 4 名重複角色與 12 種壓力類型。每題包含共同開場及兩條完整候選對話；每條路徑合計 4 個 conversational turns（對話輪次）。

兩個候選都由人工離線編寫，目的不是宣稱模型品質，而是避免 NPC-ACTOR-01 使用明顯客服腔對照所造成的容易猜題。兩邊都必須自然、簡潔、合理，且角色發言長度差不得超過 25%。

## 技術結果

- 情境：12／12。
- 重複角色：4 名，每名 3 場。
- 分類：12 種。
- 對話結構違規：0。
- 明顯 AI 稻草人句型：0。
- 作者秘密引用：0。
- 長度平衡違規：0；最大比率 1.2173913043478262。
- A/B 順序：依固定 seed（種子）決定，Actor 候選 A／B 各 6 題。
- 答案表與匿名題目包分離。
- 技術決策：`TECHNICAL_PASS_HUMAN_REVIEW_PENDING`。
- 針對性測試：6／6 PASS。
- 完整測試：892／892 PASS；failure 0、error 0、skipped 0。
- JSON：6／6 可解析；SHA-256：5／5 相符。
- 匿名性、秘密、私人絕對路徑、網路呼叫與 Git scope（變更範圍）檢查：PASS。

## 能證明與不能證明的事

本包目前只證明：評估資料可重建、A/B 平衡、題型是多輪、兩側長度相近，且沒有已知秘密越界或故意寫壞的 AI 對照。

尚未證明：NPC 已普遍像真人、已達 ChatGPT 視窗自然度、真實 API 模型一定選中 Actor 路徑，或可直接投入正式 Runtime。

Product Owner 完成盲測後，可作為專案方向證據。若要形成一般化的「像真人」主張，仍需至少一名額外獨立評審或另一個經批准的證據來源。

## 安全邊界

- 未呼叫任何外部模型或 Provider（供應商）。
- 未使用 API Key、未產生成本、未傳送隱私資料。
- 未寫 SQLite、Canon、Knowledge 或 Memory。
- 未修改 `AiTurnResultValidator`、`StateReducer` 或 transaction（交易）邊界。
- 未生成圖片、影片或音訊。
- 所有情境皆為 `NON_CANON_EVALUATION`。

## 證據位置

- 盲測原始套件：`samples/npc_actor02/npc_actor02_hard_blind_suite_v1.json`
- 真人評分用匿名包：`samples/npc_actor02/npc_actor02_hard_blind_review_packet_v1.json`
- 分離答案表：`samples/npc_actor02/npc_actor02_hard_blind_answer_key_v1.json`
- 技術報告：`samples/npc_actor02/npc_actor02_hard_blind_technical_report_v1.json`
- SHA-256：`samples/npc_actor02/npc_actor02_evidence_hashes.json`
- 嚴格 Schema（包含真人評分結果契約）：`schemas/npc_actor/npc_actor_hard_blind_v1.schema.json`
- 評分指引：`docs/npc_actor/NPC_ACTOR02_HARD_BLIND_REVIEW_GUIDE.md`
