# NPC-ACTOR-02 硬式多輪匿名盲測指引

狀態：`HUMAN_BLIND_REVIEW_COMPLETE / NO_ACTOR_ADVANTAGE_OBSERVED`

## 評分前只看這個檔案

開啟：

`samples/npc_actor02/npc_actor02_hard_blind_review_packet_v1.json`

評分完成前不要開啟：

`samples/npc_actor02/npc_actor02_hard_blind_answer_key_v1.json`

## 每題如何判斷

先閱讀角色簡介、場景與共同開場，再比較 A、B 兩段完整對話。選擇：

- `A`：A 整體較自然且更像該角色；
- `B`：B 整體較自然且更像該角色；
- `TIE`：兩者同樣可用，無法合理區分；
- `BOTH_FAIL`：兩者都不像人或都不符合角色。

同時留意六個面向：

1. 自然度：是不是當下會說的話；
2. 角色聲音：同一角色跨場是否仍像同一個人；
3. 動機：嘴上說法、動作與私人目的是否連得起來；
4. 知識邊界：有沒有說出角色不該知道的事；
5. 連續性：前後說法、動作與已知事實是否一致；
6. 多輪修復：被追問、質疑或拒絕後，是否仍自然延續。

不要猜哪邊是 Actor。若兩邊都好或都差，請直接使用 `TIE`／`BOTH_FAIL`，不要硬選。

## 回覆格式

可以分批回覆，但請保留題號，例如：

```text
1 A
2 TIE
3 B
4 BOTH_FAIL
```

若某題有明顯問題，可在選擇後加問題標籤：`自然度`、`角色聲音`、`動機`、`知識`、`連續性`、`接話修復`，並可補一句理由。沒有問題標籤就表示本題沒有額外觀察到該類缺陷。所有 12 題完成後才可揭露答案表。

完成後，Codex 會把逐題選擇與問題標籤寫入 strict result（嚴格結果）JSON，再用 `validate-npc-actor-hard-blind-review` 驗證題號完整、沒有重複、答案表未提前開啟。真人結果不得由 Codex 猜測或補造。

## Claim Boundary（主張邊界）

這 12 題的兩側都是人工離線 fixtures（固定評估素材），不是兩個真實模型輸出。因此結果只能測試「目前 Actor 設計偏好的寫法是否更符合角色」，不能直接證明 API 模型會產生相同品質。

完成本盲測也不會自動授權 live API（即時 API）、API Key、付費 Provider、正式 Runtime rollout（上線）、Canon 升格或狀態寫入。

## 完成結果

- Product Owner 在揭答前完成 12／12 題。
- Actor 4、Control 6、TIE 2、BOTH_FAIL 0。
- 知識與安全疑慮皆為 0。
- 結論：`NO_ACTOR_ADVANTAGE_OBSERVED`；目前 Actor 設計不升格、不進入 live API。
