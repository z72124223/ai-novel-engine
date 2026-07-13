# NPC-ACTOR-01 真人匿名盲測指引

狀態：HUMAN_BLIND_REVIEW_COMPLETE / LIMITED EVIDENCE

## 評分時要開啟的檔案

只開啟：

samples/npc_actor01/npc_actor01_blind_review_packet.json

評分完成前不要開啟：

samples/npc_actor01/npc_actor01_blind_review_answer_key.json

完成結果：

```text
samples/npc_actor01/npc_actor01_human_blind_review_result.json
```

## 每題怎麼評

對 20 題各自記錄：

1. A 或 B 哪個更像真人在當下自然說話；
2. 是否出現過度解說、客服腔或規則朗讀；
3. 是否說出角色不該知道的資訊；
4. 沉默、拒絕、欺瞞或非語言反應是否符合處境。

若兩者都不好，可記錄 BOTH_FAIL；若無法分辨，可記錄 TIE。不要為了完成而硬選。

## 通過條件

只有在 NPC Actor 選項獲得可量測的偏好、沒有知識或 Canon 越界、且沒有新的安全退化時，才能把離線 Pilot 標為 PASS。

此評分不會自動授權付費 API、模型供應商、正式 Runtime rollout 或狀態寫入。

## 2026-07-13 完成結果

- Product Owner 在答案表揭露前完成全部 20 題。
- Actor 選項獲得 20／20 偏好；沒有標記知識洩漏或安全退化。
- 評審同時指出 baseline 的 AI 解說腔過於明顯，因此本結果只支持「減少明顯 AI 腔」，不能支持一般真人感、ChatGPT 對話自然度或多輪角色穩定性。
- 下一輪必須使用兩邊都自然、長度相近、隨機順序與未見多輪情境的 Hard Blind Test。
