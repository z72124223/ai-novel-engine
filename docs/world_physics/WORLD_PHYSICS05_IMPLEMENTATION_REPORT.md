# WORLD-PHYSICS-05 實作報告

日期：2026-07-14

狀態：`WORLD_PHYSICS05_EMBODIED_SCENE_PILOT_PASS`

邊界：`NON_CANON_EVALUATION`，故事來源維持 `NON_CANON_PILOT`

## 簡單結論

本包把既有物理規則放進 SCREEN-01〈潮線之外〉第三場「大坪海岸凹壁救援」，用正式
`MultiTurnRuntime`、Validator（驗證器）、`PhysicalStateReducer` 與 SQLite transaction
（資料庫交易）連續跑完五回合。

結果證明：

- 成人把愛醬的肩側當起身承重支點會被拒絕；
- 拒絕後，故事版本、物理版本與 transition（狀態轉換紀錄）都沒有前進；
- 同一 pending turn（待處理回合）改用穩定岩面後可以安全完成；
- 短撬桿被拿起、帶過入口、放上穩定撬點、再次拿起並安全放下；
- 五回合都由正式 Runtime 寫入同一套交易邊界，沒有直接寫 SQLite；
- 所有來源、報告、Schema 與證據都有 SHA-256（檔案指紋）。

這只證明一條固定、非 Canon 的故事軌跡，不代表 AI 已理解完整現實世界或自由文字中的
所有物理規則。

## 真實發現與修正

試演先建立失敗測試，重現一個既有問題：角色手持物件穿過入口時，角色位置會改變，
但持有物仍留在舊位置。持有關係雖存在，空間位置已矛盾。

修正採用通用 attached-dependent translation（附著物連動平移）：移動根實體時，遞迴找出
由 `heldByEntityId`、`supportedByEntityId` 或 `containedByEntityId` 連接的實體，保留相對位移
一起移動。修正套用於既有六種狀態動作中需要改變位置的路徑，沒有新增物理類型、資料表
或 Migration。

回歸測試證明：

- 持有物穿過入口會跟隨持有者；
- 舊 WORLD-PHYSICS-01～04 的結果仍相容；
- 正式交易、rollback（交易失敗回復）與 Hash 綁定仍通過。

## 五回合結果

| 回合 | 動作 | 結果 | Story version | Physical version | Transition count |
|---|---|---|---:|---:|---:|
| 1 | 愛醬肩側支撐 → 穩定岩面支撐 | 前者拒絕，修正版完成 | 0 → 1 | 0 → 0 | 0 → 0 |
| 2 | `LIFT` 短撬桿 | 完成，建立持有關係 | 1 → 2 | 0 → 1 | 0 → 1 |
| 3 | 持物 `PASS_THROUGH` | 完成，工具與持有者一起移動 | 2 → 3 | 1 → 2 | 1 → 2 |
| 4 | `PLACE_ON` 穩定撬點 | 完成，支撐關係保存 | 3 → 4 | 2 → 3 | 2 → 3 |
| 5 | `LIFT` → `DROP` | 完成，持有／支撐／容納關係清除 | 4 → 5 | 3 → 5 | 3 → 5 |

總計：5 個正式完成回合、1 個安全拒絕嘗試、5 筆正式物理 transition。

## 固定證據

- Pilot 報告：`samples/world_physics05/screen01_scene03_embodied_physical_pilot_v1.json`
- Hash manifest：`samples/world_physics05/world_physics05_evidence_hashes.json`
- Strict Schema：`schemas/world_physics/embodied_scene_physical_pilot_v1.schema.json`
- 故事主檔：`samples/screen01/screen01_story_production_master.json`
- 場景表：`samples/screen01/screen01_scene_list.json`

報告 SHA-256：`899f342e87ee5e1209a721f19266bddbe9683700172d66fc0512a38e126708e7`。

## 驗證結果

- Kotlin production/test compile（正式程式／測試編譯）：PASS。
- WORLD-PHYSICS-05 focused tests（直接測試）：2／2 PASS。
- WORLD-PHYSICS-01～05 與 NPC embodied related tests（相關測試）：42／42 PASS。
- 完整 `clean test`：940／940 PASS，0 failure、0 error、0 skipped。
- JSON parse：3／3 PASS。
- strict Schema contract：PASS。
- SHA-256／bytes：4／4 PASS。
- SCREEN-01 來源路徑與內容邊界：PASS。
- 私人路徑與秘密掃描（只檢查本包新增內容）：PASS。
- `git diff --check`：PASS。
- 圖片、影片、音訊、Provider、API、模型下載、Canon、下一章：0 項。

## 跨平台 Hash 修正

驗證期間發現 WORLD-PHYSICS-04 JSON 與 Migration 016 未完整鎖定 LF（跨平台換行格式），
Windows checkout 會改變實體 bytes，造成內容相同但 Hash 測試失敗。本包只在
`.gitattributes` 補上 WORLD-PHYSICS-04／05 JSON 與 Migration 016 的 `eol=lf`，沒有修改
其邏輯內容。補正後舊證據與完整測試全部通過。

## 停止條件

WORLD-PHYSICS-05 已完成。藍圖的下一階段屬於可選的 richer physics（更細物理）研究，只有
新的故事失敗證據能證明現有近似不足、且 Product Owner 重新授權時才啟動。目前不得自動
開始 API、Provider、媒體、Canon 或新的物理工作包。
