# AI 劇本：Claude 本機工作入口

請以繁體中文回答。這個資料夾是 AI Novel Engine 的 Kotlin/JVM 原始碼專案。
本檔是本機閱讀入口；它不代表 Claude 桌面版已連接此資料夾，也不授權自動開發。

## 開始前

1. 讀取適用的 `AGENTS.md`，保留現有工作樹與未追蹤檔案。
2. 核對 `git status --short`、`git branch --show-current`、`git rev-parse HEAD`。
3. 依 `AGENTS.md` 次序讀取現行文件，再以實際程式碼核對文件宣稱。
4. 本次整理的閱讀指南與來源摘錄在 `out/claude-onboarding-20260927/`；它們是 2026-09-27 的快照，不會自動更新。

## 引擎核心

- 入口：`app/src/main/kotlin/com/localnovel/engine/Main.kt`。
- 多回合：`service/MultiTurnRuntime.kt` 準備 ContextPacket、檢查 AiTurnResult，再呼叫 `repository/SqliteStoryRepository.kt` 的 `resolvePendingTurn`，透過單一交易套用正式狀態。
- 舊單回合：`service/TurnRunner.kt` 呼叫 `AiTurnResultValidator` 與 `StateReducer`。不要把兩條路徑混成同一條。
- `prepare-turn` 建立 pending context；`resolve-turn` 驗證後才套用。AI 只產生結構化候選，不直接操作 SQLite。
- 版本、contextHash、resultId、角色知識、場景、能力、物理、敘事與人工批准共同限制可接受的結果。

上述 `service/` 和 `repository/` 路徑均位於 `app/src/main/kotlin/com/localnovel/engine/`。

## 本次已知文件差異

`PROJECT_STATE.md` 仍寫 WP-001，`PLANS.md` 沒有 AGENTS 所指的 Current Plan Window V15；另有 NPC07/08 原始碼。NPC handler 存在，但本次讀取的 `Main.kt` 未接入 `NpcActorCli`。請重新核對，不要把舊報告、服務存在或模型 Pilot 通過當成當前可執行性與授權。

## 工作範圍

目前先理解與分析引擎。使用者另行提出工作時，依其範圍執行；不要從歷史工作包自行啟動下一包。
不得繞過驗證、交易、版本、雜湊、角色知識或人工批准；不要直接寫 SQLite。
不得因閱讀本檔自行生成圖片／影片、使用付費 API、啟動服務、下載模型、升格 Canon、Push 或發布。
來源存在、測試通過、執行成功與正式交付須分別報告，沒有當次證據就明說未驗證。
