# NPC-ACTOR-03 自適應角色表現校準實作報告

狀態：`TECHNICAL_PASS / HUMAN_BLIND_REVIEW_PENDING / LIVE_API_BLOCKED`

日期：2026-07-13

分支：`codex/npc-actor03-adaptive-profile-calibration`

## 為什麼只校準一次

NPC-ACTOR-02 的真人結果為 Actor 4、Natural Control 6、TIE 2，未觀察到整體 Actor 優勢。因為評審沒有提供逐題理由，「角色特徵表現過量」只能當成待測假設，不能當成已知原因。

本包只建立 revision 1（第一版）的 evaluation-only policy（僅評估規則）。若新保留測試仍沒有優勢，必須停止，不得重寫題目直到結果變好。

## 單次校準規則

決策順序固定為：眼前需求 → 知識與安全邊界 → 關係與在場壓力 → 私人動機的表達方式 → 語氣強度。

規則要求角色先處理實際問題；私人動機可以藏在行動、省略或時機，不必說破；依關係、急迫度、觀眾與風險調整風格；允許中性句、沉默、遲疑、拒絕與有依據的欺瞞；不在每輪強塞招牌挖苦、威脅或比喻。

## 新保留測試

- 8 個全新 `NON_CANON_EVALUATION` 情境。
- 米拉、嚴朔、連霜、任七各 2 題。
- 每條候選路徑合計 4 個對話輪次。
- 兩側都是人工離線編寫的可信版本；不是模型輸出。
- NPC-ACTOR-02 情境 ID 重複：0。
- NPC-ACTOR-02 內容指紋重複：0。
- A/B 順序以固定 seed（種子）決定，Adaptive Actor 在 A／B 各 4 題。
- 結構、稻草人句型、作者秘密與長度違規均為 0。
- 最大角色發言長度比：1.25。

## 技術證據

- 校準專用測試：6／6 PASS。
- 完整測試：898／898 PASS；failure 0、error 0、skipped 0。
- JSON：7／7 可解析；SHA-256：6／6 相符。
- Schema、匿名性、秘密、私人絕對路徑、網路呼叫與 Git scope（變更範圍）檢查：PASS。
- 匿名題目不含 `policyId`、校準 signals（訊號）、來源身分、Evidence（證據）、seed 或答案。
- 答案表與匿名題目分離；尚未建立真人結果，也未揭露答案。
- 所有素材皆可由正式 CLI（命令列介面）重建與嚴格解碼。

## 能證明與不能證明的事

目前只證明：單次校準規則、8 題新保留資料、匿名化、長度平衡與安全邊界可重建且通過技術 Gate（關卡）。

尚未證明：校準版比自然對照更好、NPC 已像真人、ChatGPT 視窗自然度已重現、或真實 API 模型能產生同樣品質。真人盲測結果也只有一名 Product Owner（產品負責人）的方向證據。

## 安全邊界

- 未呼叫外部模型、Provider（供應商）或網路 API。
- 未使用 API Key、未產生成本、未傳送隱私資料。
- 未寫 SQLite、Canon、Knowledge 或 Memory。
- 未修改 Validator、StateReducer 或 transaction（交易）邊界。
- 未生成圖片、影片或音訊。
- 未修改 NPC-ACTOR-01／02 已封存結果。

## 證據位置

- 校準規則：`samples/npc_actor03/npc_actor03_adaptive_expression_policy_v1.json`
- 新保留套件：`samples/npc_actor03/npc_actor03_holdout_suite_v1.json`
- 技術報告：`samples/npc_actor03/npc_actor03_technical_report_v1.json`
- 匿名真人題目：`samples/npc_actor03/npc_actor03_blind_review_packet_v1.json`
- 分離答案表：`samples/npc_actor03/npc_actor03_blind_review_answer_key_v1.json`
- SHA-256：`samples/npc_actor03/npc_actor03_evidence_hashes.json`
- 嚴格 Schema：`schemas/npc_actor/npc_actor_calibration_v1.schema.json`
- 真人評分指引：`docs/npc_actor/NPC_ACTOR03_HUMAN_BLIND_REVIEW_GUIDE.md`
