# NPC-ACTOR-05 實作報告

日期：2026-07-14
狀態：`TECHNICAL_PASS / MODEL_INTEGRATION_AUTHORIZATION_REQUIRED`

## 1. 做了什麼

本包新增一個完全離線、每個提案最多兩次的 NPC 行動安全閉環。它沒有新增第二套世界規則，而是依序組合既有邊界：

1. NPC actor contract（NPC 演員契約）；
2. physical binding（物理綁定）；
3. physical affordance（物理可行性）；
4. Game Master world legality（遊戲主持人世界合法性）；
5. 十分鐘行動預算。

第一次失敗時，系統只產生清理過的修訂要求。第二次仍失敗就停止。通過結果也只會成為正式 `resolve-turn` 的候選，不會直接寫入 SQLite 或 Canon。

## 2. 安全與秘密邊界

- 修訂要求只包含角色可見的實體 ID、允許目標、允許位置、拒絕碼與固定修訂指示。
- 不回傳引擎內部重量、力量、距離、時間計算、作者秘密或隱藏知識。
- 每個輸入檔都綁定 Repository 相對路徑、檔案大小與 SHA-256。
- `formalStateWritePerformed` 固定為 `false`。
- 沒有 API、Provider、模型下載、網路呼叫、Migration 或資料庫寫入。

## 3. 四個固定案例

| Case | 第一次 | 第二次 | 最終結果 |
|---|---|---|---|
| Mira stable first pass | 通過 | 不需要 | 正式解決候選 |
| Lian knowledge repair | 使用別人的知識，拒絕 | 改為自己的可見證據，通過 | 正式解決候選 |
| Ren physical repair | 推動超過能力的鐵砧，拒絕 | 改推可負擔的重箱，通過 | 正式解決候選 |
| Yan storm gate | 推動超限，拒絕 | 路徑仍被障礙封鎖，拒絕 | 兩次後正式拒絕 |

結果：4/4 符合預期；3 個候選可進下一層正式驗證，1 個安全拒絕。

## 4. 已知限制

本包能驗證既有資料中明確表示的承重、接觸、容納、路徑、障礙、位置與行動預算，但仍未精細模擬：

- 繩索張力；
- 摩擦係數；
- 濕滑坡面；
- 軟體變形；
- 更完整的多物體動力學。

這些限制是誠實的模型邊界，不應靠提示詞假裝已解決。

## 5. 驗證證據

- focused tests（聚焦測試）：9/9 PASS。
- related tests（相關回歸測試）：43/43 PASS。
- full tests（完整測試）：953/953 PASS，0 failure、0 error、0 skipped。
- 固定案例：4/4 符合預期。
- Evidence manifest（證據清單）：11 個來源檔案的大小與 SHA-256 可重算。
- JSON、Schema、Hash、秘密、私人路徑、Git scope 與 whitespace（空白字元）檢查均納入完成 Gate。

主要證據：

- `samples/npc_actor05/npc_actor05_safety_loop_suite_v1.json`
- `samples/npc_actor05/npc_actor05_safety_loop_report_v1.json`
- `samples/npc_actor05/npc_actor05_evidence_hashes.json`
- `schemas/npc_actor/npc_actor_safety_loop_v1.schema.json`

## 6. 真正停止點

NPC-ACTOR-05 只證明現有 Validator（驗證器）可以形成可重現、有限次數的離線修訂與拒絕閉環。它不證明 live model（即時模型）已能自然扮演 NPC，也不證明所有物理環境都被理解。

下一步若要接入本地模型或雲端模型，必須另外建立計畫、定義資料外洩邊界、選定模型與成本，並由 Product Owner 明確授權。現在停止在這個 Gate（授權關卡）。
