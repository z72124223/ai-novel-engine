# NPC-ACTOR-04A 內部評審封存報告

日期：`2026-07-14`

狀態：`INTERNAL_REVIEW_COMPLETE`

## 結果

原本要求 Product Owner（產品負責人）反覆完成的四組匿名 A/B 評分已取消。三個彼此
獨立、在評分時看不到答案表的內部 Agent（代理）分別從下列角度審查同一份完整場景包：

1. 環境與物理；
2. 角色與對白；
3. 對抗性安全與知識邊界。

三份結果完成後，只進行一次格式修訂，使它們符合相同的嚴格 JSON 契約；選擇、分數、
證據與理由沒有因此改變。三份結果通過完整性、角色唯一性、匿名來源洩漏與 SHA-256
檢查後，彙整器才讀取封存答案。

確定性多數決結果：

| 場景 | 多數選擇 | 演員版位置 | 結果 |
|---|---|---|---|
| Mira — kitchen fire | A | B | `CONTROL_PREFERRED` |
| Yan — storm gate | `BOTH_FAIL` | A | `BOTH_FAIL` |
| Lian — market collapse | A | A | `ACTOR_PREFERRED` |
| Ren — dock surge | B | B | `ACTOR_PREFERRED` |

總計：演員版 2 勝、控制版 1 勝、兩邊都失敗 1、平手 0、無共識 0。

這個結果關閉「還要不要繼續讓使用者做 A/B」的流程問題，但不是全面品質通過。尤其
Yan 暴風城門場景暴露了未固定繩索、突然出現受力繩結、濕滑斜坡人力制動與跨位置操作
等問題；未來若實際產生這類行動，仍須由既有的物理與正式回合驗證層拒絕。

## 實作

- 新增內部評審嚴格資料模型與 JSON Schema（資料格式規範）。
- 新增三角色、四場完整性、分數範圍、證據、問題標籤與匿名邊界驗證。
- 新增確定性多數決，答案表只能在三份結果有效後參與 Actor／Control 對照。
- 新增 CLI（命令列介面）：
  - `validate-npc-actor-embodied-internal-reviews`
  - `aggregate-npc-actor-embodied-internal-review`
- 原 `PRODUCT_OWNER` 結果契約沒有被冒用或弱化。
- 原 NPC-ACTOR-04 匿名包、答案、suite、技術報告與 Schema 全部保持原 Hash。

## 證據

- 工作包：`docs/work-packages/NPC-ACTOR-04A_INTERNAL_REVIEW_CLOSURE.md`
- Schema：`schemas/npc_actor/npc_actor_internal_review_v1.schema.json`
- 環境／物理評審：`samples/npc_actor04/internal_review/environment_and_physics_review.json`
- 角色／對白評審：`samples/npc_actor04/internal_review/character_and_dialogue_review.json`
- 對抗性評審：`samples/npc_actor04/internal_review/adversarial_boundary_review.json`
- 彙整結果：`samples/npc_actor04/internal_review/npc_actor04_internal_review_aggregate.json`
- 新證據 Hash：`samples/npc_actor04/internal_review/npc_actor04_internal_review_evidence_hashes.json`
- 凍結歷史 Hash：`samples/npc_actor04/npc_actor04_evidence_hashes.json`

彙整檔中的 `reviewContentSha256` 是 Kotlin 嚴格序列化後的內容 Hash；新證據 Hash 檔則記錄
Repository（程式庫）實際檔案位元組。兩者用途不同，因此數值不必相同。

## 驗證

- 新 focused tests（聚焦測試）：`4/4 PASS`。
- NPC Actor 相關測試：`42/42 PASS`。
- 完整 Gradle 測試：`944/944 PASS`，失敗 0、錯誤 0、略過 0。
- 三份評審的正式 CLI 預揭答驗證：`PASS`。
- JSON Schema：三份評審與一份彙整，共 `4/4 PASS`。
- 原 NPC-ACTOR-04 凍結證據：`6/6` 路徑、位元組與 SHA-256 完全相同。
- `git diff --check`：`PASS`。

## 限制

- 這是三個內部 Agent 的工程評審，不是人類偏好證明。
- 被評軌跡是人工編寫的離線測試素材，不是 live model（即時模型）輸出。
- 演員版只在四場中取得 2 勝，不能宣稱一般性自然度或全面優勢。
- 這不授權 API、付費 Provider（供應商）、正式上線、Canon（正式世界設定）升格、
  圖片、影片、音訊或下一章工作。
- 使用者不需要再替 NPC-ACTOR-04 完成更多 A/B 測試。
