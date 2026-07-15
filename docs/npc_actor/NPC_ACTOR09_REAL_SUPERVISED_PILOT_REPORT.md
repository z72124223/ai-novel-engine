# NPC-ACTOR-09 真實監督 Pilot 報告

## 結論

本次結果為 `SAFE_REJECTION`（安全拒絕），沒有產生可供使用者審查的 NPC 候選，也沒有套用
正式回合。這不是 Qwen3-8B 品質失敗的有效測量；真正原因是測試 Session 啟動後，玩家與
沈霜被放到同一地點，已不符合「玩家不在場」的基本條件。

## 執行範圍

- 基線 Commit：`54ed365514b228dc20cb3b3a48a21d774705249a`
- 分支：`codex/npc-actor09-real-supervised-pilot`
- 模型：`Qwen3-8B-Q5_K_M.gguf`
- 模型 SHA-256：`068bae163faa96ad48032daf4e071a6a28fe67d8dcc95367609c2ff165e52738`
- 執行器：`llama.cpp-b10002-vulkan/llama-cli.exe`
- 執行器 SHA-256：`f0741939ea8adecc58db40cfeabd8526d156878fdaa1ab2c8c1b6de08bdd8d9e`
- 模型呼叫：本機直接程序；沒有 server（伺服器）、port（連接埠）、雲端 API 或付費 Provider。

## 發生了什麼

非 Canon fixture 原先把沈霜放在 `loc-npc09-old-corridor`，把玩家放在
`loc-npc09-rear-hall`。但 opening scene 同時宣告了
`locationId=loc-npc09-old-corridor`。

既有 `SqliteStoryRepository.startSession` 在 opening scene 有 `locationId` 時，會對該專案的
全部 `character_states` 更新位置。正式 Session 建立後，匯出的證據因此顯示：

- 玩家：`loc-npc09-old-corridor`
- 沈霜：`loc-npc09-old-corridor`

這使 offscreen（玩家不在場）條件失效。`prepare-local-npc-actor-formal-turn` 仍依原安全規則
執行，最多兩次模型嘗試後回傳 `REJECTED_AFTER_MAX_ATTEMPTS`。系統沒有建立候選檔，並中止
pending Context；沒有降低角色、知識、物理或世界合法性規則。

## 狀態安全證據

Session：`session-f6471aae-7ead-4ec8-8fa4-8156e479a637`

| 項目 | prepare 前 | 拒絕後 |
|---|---:|---:|
| `stateVersion` | 0 | 0 |
| `nextTurnNumber` | 1 | 1 |
| active scene instance | 1 | 1 |
| 玩家位置 | 舊廊 | 舊廊 |
| 沈霜位置 | 舊廊 | 舊廊 |
| 正式回合套用 | 否 | 否 |

前後匯出證據的 SHA-256 完全相同：
`aeeaf3a909c438902a509750db40c784388d2a664ca9f88b58a78ea357792d2d`。

臨時 SQLite 檔案本身因 pending Context 建立與中止而有位元差異，但語意狀態沒有改變；臨時
資料庫不提交 GitHub。

## 驗證結果

- `LocalNpcActorFormalTurnServiceTest`：8/8 PASS（通過）。
- 全部 `*NpcActor*` 相關測試：73/73 PASS。
- `samples/npc_actor09` JSON：5/5 可解析。
- Manifest 內檔案 SHA-256：4/4 重算一致。
- prepare 前後 Session 證據：逐位元相同。
- 候選檔：0。
- Production Kotlin／Test／Migration 變更：0。
- `git diff --check`：PASS。
- 新增內容的金鑰、秘密與本機絕對路徑檢查：PASS。

## 為何停止而不重跑

NPC-ACTOR-09 的計畫明定：安全鏈拒絕後要保存證據並停止，不能反覆生成到碰巧通過。這次還
發現原本的場景假設不成立，所以依 strata-agent-loop（長任務防漂移工作流）先停止，而不是
在同一工作包暗中改 fixture 後重試。

## 建議後續工作包

建議另開 `NPC-ACTOR-09A_SESSION_SETUP_REPAIR`：

1. 建立全新的臨時資料庫與 Session，不重用本次 Session。
2. opening scene 不宣告全員位置，或建立明確、可測試的官方 Session 設定流程。
3. 在任何模型呼叫前，先以匯出證據硬性確認玩家與 NPC 位於不同地點。
4. 只有前置條件通過才允許再執行一次 Qwen3-8B prepare。

現有直接測試 fixture 的 `Scene` 不帶 `locationId`，可作為下一包的已知安全起點；本報告不
授權修改 Production Kotlin、直接 SQL、重跑模型、resolve、雲端 API 或 Canon 寫入。
