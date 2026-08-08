# NPC-ACTOR-07 — Formal Runtime Shadow Integration

狀態：`TECHNICAL_PASS / NO_FORMAL_STATE_WRITE / NPC_ACTOR08_AUTHORIZATION_REQUIRED`

日期：2026-07-15

## 目標

讓已驗證的本機 Qwen3-8B 從真正的 `MultiTurnRuntime` Session 取得一份只屬於指定 NPC 的
唯讀資料包，產生對白與行動候選，再經過既有安全閉環。結果只保存為 Git 證據，不進入
`resolve-turn`，也不改變正式世界。

## 執行邊界

```text
正式 SQLite Session
→ previewTurnContext（唯讀）
→ actor visibility filter（角色可見性過濾）
→ Qwen3-8B 本機候選
→ actor / knowledge / physical / world / time validators
→ Shadow report（影子報告）
→ STOP；不得自動 resolve-turn
```

## 必須成立

- 使用正式 Session 與正式 Context 編譯器，但不得保存 pending context。
- 模型只能看到指定 NPC 自己可使用的資料與白名單。
- engine-owned 幾何、質量、承重、障礙座標只供 Validator 使用，不得進 Prompt。
- 模型最多兩次嘗試，第二次仍錯就拒絕。
- 接受狀態只能是 `ACCEPTED_FOR_FORMAL_RESOLVE_CANDIDATE`，不代表已套用。
- 執行前後 Session、turn log、event、pending context、物理狀態與 DB Hash 不變。

## 允許修改

- `PLANS.md`
- `PROJECT_STATE.md`
- `.codex/project_controller.md`
- `AGENTS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/cli/NpcActorCli.kt`
- 新增直接相關 `model`／`service` Kotlin 檔
- 新增直接相關 Test
- `schemas/npc_actor/`
- `samples/npc_actor07/`
- `docs/npc_actor/`
- 本工作包

## 禁止修改

- `app/src/main/resources/migrations/**`
- Repository transaction、`StateReducer`、正式 resolve 流程
- Chenxing Canon、世界設定、Memory、Knowledge、SCREEN-01、圖片、影片、音訊
- 模型權重與 llama.cpp 執行器

## 真正停止條件

- 唯讀資料無法建立，除非修改 Migration／Repository transaction 才能繼續。
- 模型必須看到 author-only 或其他角色私有資料才可能運作。
- 必須弱化現有 Validator 才能讓候選通過。
- 測試或真實 Pilot 發現正式狀態有任何變動。
- 需要付費 API、server、port、Canon 決策或 Product Owner 創作判斷。

## 完成條件

- 一個正式 Session 的真實本機模型影子 Pilot 可重現。
- 至少一個接受或安全拒絕結果，且零正式寫入。
- 負面測試證明 director goal、秘密、他人私有記憶與完整物理數值不進 Prompt。
- focused、related、full tests 與所有證據檢查通過。
- 獨立 Commit 並普通 Push 到 `codex/npc-actor07-formal-runtime-shadow`。
- 停止在 NPC-ACTOR-08 人工批准後執行 Gate，不自行開始。
