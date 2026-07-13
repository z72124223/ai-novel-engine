# NPC-ACTOR-01 真人匿名盲測指引

狀態：HUMAN_BLIND_REVIEW_PENDING

## 評分時要開啟的檔案

只開啟：

samples/npc_actor01/npc_actor01_blind_review_packet.json

評分完成前不要開啟：

samples/npc_actor01/npc_actor01_blind_review_answer_key.json

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
