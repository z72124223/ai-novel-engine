# NPC-ACTOR-09A Session 設定修復報告

## 結論

Session 設定修復成功，但 Qwen3-8B 在有效的離場 NPC 場景中仍未通過既有安全鏈。本次結果為
`SAFE_REJECTION`（安全拒絕）：沒有候選、沒有 resolve、沒有正式世界狀態變更。

這次與 NPC-ACTOR-09 不同。上一包因玩家被錯誤搬到 NPC 所在地而無法有效測量模型；本包已先
證明玩家和沈霜分處兩地，因此這次拒絕是有效的模型／安全鏈測試結果。

## 設定修復證據

- 基線 Commit：`679ec9afbff3706ea76eccdd17e062203d6c1758`
- 分支：`codex/npc-actor09a-session-setup-repair`
- 新 Session：`session-17df474b-0cd7-4872-96b3-7a1f7702dd8c`
- 玩家位置：`loc-npc09a-rear-hall`
- 沈霜位置：`loc-npc09a-old-corridor`
- 位置分離：PASS
- `stateVersion=0`、`nextTurnNumber=1`、pending Context 0

修正版 opening scene 不含 `locationId`，所以既有 `start-session` 沒有批次覆蓋角色位置。全部 DB
動作都使用官方 CLI，沒有直接 SQL。

## 模型執行

- 模型：`Qwen3-8B-Q5_K_M.gguf`
- 模型 SHA-256：`068bae163faa96ad48032daf4e071a6a28fe67d8dcc95367609c2ff165e52738`
- 執行器：`llama.cpp-b10002-vulkan/llama-cli.exe`
- 執行器 SHA-256：`f0741939ea8adecc58db40cfeabd8526d156878fdaa1ab2c8c1b6de08bdd8d9e`
- 執行方式：本機直接程序，沒有 server、port、雲端 API 或付費 Provider
- prepare 呼叫：1 次
- 模型內部嘗試上限：2
- 結果：`REJECTED_AFTER_MAX_ATTEMPTS`
- 候選檔：未建立

## 狀態安全證據

| 項目 | prepare 前 | 拒絕後 |
|---|---:|---:|
| `stateVersion` | 0 | 0 |
| `nextTurnNumber` | 1 | 1 |
| active scene instance | 1 | 1 |
| 玩家位置 | 後殿 | 後殿 |
| 沈霜位置 | 舊廊 | 舊廊 |
| 正式回合套用 | 否 | 否 |

前後 Session 證據逐位元相同，SHA-256 都是：
`1708c41408e34b252b337e6b4ad871e0fa167f033266e927c3e20176a8956b9c`。

臨時 SQLite 檔案因 pending Context 建立與中止而有位元差異，但匯出的語意狀態完全相同；DB
不提交 GitHub。

## 為何不再重跑

工作包明定最多一次 prepare，安全拒絕後停止。反覆抽樣只會讓測試變成「抽到通過為止」，無法
回答模型為什麼失敗，也會浪費使用量。

目前正式準備服務在拒絕時只留下錯誤狀態，沒有將兩次嘗試的安全檢查原因輸出成可審查 sidecar。
因此下一個合理工作包是 `NPC-ACTOR-09B_REJECTION_OBSERVABILITY`：保存經過秘密清理的失敗階段、
規則代號與修正提示，不保存作者秘密，也不寫正式狀態。取得原因後，才能判斷應調整 Prompt、輸入
契約或未來模型路由；本包不授權該實作。

## 驗證結果

- `LocalNpcActorFormalTurnServiceTest`：8/8 PASS（通過）。
- 全部 `*NpcActor*` 相關測試：73/73 PASS。
- `samples/npc_actor09a` JSON：5/5 可解析。
- Manifest 內檔案 SHA-256：4/4 重算一致。
- 位置分離前置條件：PASS。
- prepare 前後 Session 證據：逐位元相同。
- 候選檔：0。
- Production Kotlin／Test／Migration／舊 NPC-ACTOR-09 證據變更：0。
- 秘密、本機絕對路徑、Git scope 與 `git diff --check`：PASS。
- 完整測試套件未重跑；本包沒有修改 Production 或 Test，已執行全部 NPC Actor 相關回歸測試。
