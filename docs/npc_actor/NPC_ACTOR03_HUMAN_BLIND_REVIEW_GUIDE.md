# NPC-ACTOR-03 自適應表現匿名盲測指引

狀態：`HUMAN_BLIND_REVIEW_REQUIRED / ANSWER_KEY_SEALED`

## 評分前只看這個檔案

開啟：

`samples/npc_actor03/npc_actor03_blind_review_packet_v1.json`

8 題完成前不要開啟：

`samples/npc_actor03/npc_actor03_blind_review_answer_key_v1.json`

## 每題如何判斷

先讀角色簡介、場景與共同開場，再比較 A、B 的完整對話。請選：

- `A`：A 整體較自然、像真人且符合角色；
- `B`：B 整體較自然、像真人且符合角色；
- `TIE`：兩者同樣可用，無法合理區分；
- `BOTH_FAIL`：兩者都不像人或都不符合角色。

不要猜哪一側是校準版。兩側皆為人工離線素材，不是真實 API 模型輸出。若同樣好或同樣差，請直接選 `TIE`／`BOTH_FAIL`。

可額外標記：`自然度`、`角色聲音`、`動機`、`知識`、`連續性`、`接話修復`。也請指出任何 knowledge boundary（知識邊界）或 safety boundary（安全邊界）疑慮。

## 回覆格式

只需依題號回覆，例如：

```text
1 A
2 B
3 TIE
4 BOTH_FAIL
5 A
6 B
7 A
8 B
```

有問題時可在答案後加短註解。8 題完成後，Codex 會先保存並驗證真人結果，之後才可打開答案表。

## 完成後的決策規則

- 校準版勝過自然對照：只代表此人工設計方向值得下一階段驗證；不等於真實 API 已通過。
- 沒有整體優勢：停止 NPC-ACTOR-03，不做第二次校準來追求勝出。
- 出現知識或安全疑慮：停止並保留 Gate，不得啟用 live API。

任何結果都不會自動授權 live API、API Key、付費 Provider、正式 Runtime、Canon 升格或 SQLite 狀態寫入。
