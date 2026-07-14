# NPC-ACTOR-05 — Offline NPC Action Safety Loop

狀態：`ACTIVE`

日期：2026-07-14

## 目的

建立一條完全離線、最多兩次嘗試的 NPC 行動安全閉環。第一份 NPC 回覆必須依序通過：

1. 角色身分、知識、記憶、語氣與輸出契約。
2. 角色可見目標與引擎私有物理上下文的綁定契約。
3. 實體可行性。
4. 位置、玩家是否在場、世界合法性。
5. 離線回合時間上限。

任一階段失敗時，只產生安全的結構化修正單；若有第二份回覆，再重新走完整檢查。第二次
仍失敗就停止。此流程只輸出正式回合的候選，不能寫 SQLite、Canon 或任何正式世界狀態。

## 必須重用

- `NpcActorResponseValidator`
- `NpcActorPhysicalBindingValidator`
- `PhysicalAffordanceValidator`
- `GameMasterResolutionService`

不得複製或弱化上述規則。

## 允許範圍

- `app/src/main/kotlin/**/model/NpcActorSafetyLoopModels.kt`
- `app/src/main/kotlin/**/service/NpcActorSafetyLoopService.kt`
- `app/src/main/kotlin/**/service/NpcActorSafetyLoopFixtureFactory.kt`
- `app/src/main/kotlin/**/cli/NpcActorCli.kt`
- `app/src/main/kotlin/**/Main.kt`
- 直接相關 tests、Schema、samples、docs 與治理文件

## 禁止範圍

- 不修改 Migration、Repository、`MultiTurnRuntime`、`StateReducer` 或正式 SQLite。
- 不修改 Chenxing Canon、Setting Pack、Knowledge、Memory、故事章節或媒體資產。
- 不接 API、不下載／啟動模型、不生成圖片／影片／音訊。
- 不建立無上限迴圈，不使用 force push，不碰原髒工作樹。

## 固定狀態

最終狀態只能是：

- `REPAIR_RESPONSE_REQUIRED`
- `ACCEPTED_FOR_FORMAL_RESOLVE_CANDIDATE`
- `REJECTED_AFTER_MAX_ATTEMPTS`

修正單階段只能是：

- `ACTOR_CONTRACT`
- `PHYSICAL_BINDING_CONTRACT`
- `PHYSICAL_AFFORDANCE`
- `WORLD_LEGALITY`
- `TIME_BUDGET`

## 修正單安全邊界

允許包含：request id、attempt number、stage、錯誤代碼、允許行動、可見目標、必須保持的
角色與位置識別、一般化修正指示。

禁止包含：完整 engine context、physical archetype、snapshot、質量、尺寸、承重、重力值、
障礙座標、未揭露知識、作者秘密或 Canon 內部資料。

## 四個固定情境

- 一個首次即通過的穩定情境。
- 一個角色／知識契約第一次失敗、第二次修正通過的情境。
- 一個物理或位置第一次失敗、第二次修正通過的情境。
- 一個暴風雨閘門高風險情境；若兩次都不安全，必須誠實保留拒絕。

全部標記 `NON_CANON_EVALUATION`，不得升格正式世界設定。

## 驗證與完成條件

- focused tests 覆蓋五階段、最多兩次、停止條件、修正單秘密邊界與 CLI 可達性。
- 四情境 suite、technical report 與 evidence manifest 可由 Git 重建。
- committed JSON 通過嚴格解析與 Schema；SHA-256 可重算。
- NPC actor、physical、formal-turn related tests 與完整 Gradle tests 通過。
- `git diff --check`、私人路徑、秘密、禁止檔案與 Git scope 檢查通過。
- Commit 並普通 Push 功能分支；不自動整合模型、不開始下一包。

## 真正停止點

完成並 Push 後，狀態停在：

`MODEL_INTEGRATION_AUTHORIZATION_REQUIRED`

後續若要使用本地或雲端生成模型提供 `NpcActorPhysicalResponse`，必須另行取得 Product Owner
授權；本工作包不暗示該授權。
