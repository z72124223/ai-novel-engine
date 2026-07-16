# AI Novel Engine 工具化範圍凍結與封存交接

日期：2026-07-16

狀態：`PRODUCT_SCOPE_FROZEN / THREAD_ARCHIVE_READY / V1_RELEASE_NOT_STARTED`

## 1. 本窗口最終決策

`ai-novel-engine` 不再以「把小說、NPC、圖片、影片、聲音、介面與模型全部做在一起」為產品方向。

正式定位收斂為：

> **可靠的本地故事世界、上下文組裝與 AI 結果驗證工具。**

它讓外部 AI 可以提出故事或行動候選，但不能直接改寫正式世界。只有通過既有規則、Hash、
狀態版本與 transaction（交易式寫入）邊界的結果，才能進入 SQLite。

## 2. v1 工具應保留的核心

- Kotlin/JVM 命令列介面（CLI）。
- SQLite 正式狀態與 additive Migration。
- JSON 輸入／輸出檔案協議。
- 世界、角色、場景、時間、物品與能力資料。
- 角色 Memory／Knowledge 邊界。
- Context 組裝、預算、Hash 與狀態版本。
- `AiTurnResultValidator`、其他既有 Validator 與 `StateReducer`。
- 場景連續性、物理可行性、能力成本與秘密洩漏檢查。
- 合法結果的單一 SQLite transaction。
- Git 來源重建、Canon 驗證、證據匯出與可追溯報告。

## 3. 不再列為本 Repository v1 完成門檻

- 圖片生成與角色／場景視覺產線。
- 影片、配音、聲音、字幕與剪輯。
- Reader UI、網站、Android App 或公開服務。
- OpenAI API、本機模型或其他 Provider 的正式接入。
- 大型 NPC 社會模擬與無限自主世界。
- 《塵行》或其他特定小說的完整章節生產。
- 新的 MVP 編號與未經凍結的功能擴張。

以上既有內容不刪除，保留為歷史、實驗與未來獨立專案的證據；但不得再阻止本工具封版。

## 4. 未來獨立專案建議

| Repository | 職責 |
|---|---|
| `ai-novel-engine` | 世界狀態、Context、Validator、SQLite、證據 |
| `chenxing-story` | 《塵行》世界觀、角色、故事與劇本內容 |
| `ai-story-model-gateway` | GPT API、本機模型與候選文字產生 |
| `ai-visual-production` | 角色／場景資產、圖片、分鏡與影片 |
| `ai-story-reader` | 一般讀者或玩家使用介面 |

名稱只是後續建議，不代表本窗口已建立這些 Repository。

## 5. 已確認的最新技術證據

2026-07-15 驗證快照：

- fresh Git-input rebuild：PASS。
- Canon validation：PASS，findings 0。
- 聚焦測試：48／48 PASS。
- 完整測試：976／976 PASS。
- Production Kotlin、Test、Migration、Schema、正式 SQLite 與 Canon 變更：0。

第一話 V2：

- Branch：`codex/episode01-engine-screenplay`
- Commit：`6a0e2c2357e2046751b7cd37ec9a744e7ce7f5c2`
- 劇本：`samples/episode01/episode01_screenplay_engine_v2_zh-Hant-TW.md`
- 狀態：`READY_FOR_PRODUCT_OWNER_REVIEW / NON_CANON_DRAFT`

這些是當時快照，不等於 `v1.0.0` 已發布。

## 6. 目前 Git 狀態

- 遠端預設分支：`codex/mvp6-checkpoint-branching-spec`
- 預設分支 SHA：`54ed365514b228dc20cb3b3a48a21d774705249a`
- 收尾工作分支：`codex/episode01-engine-screenplay`
- 收尾前 SHA：`6a0e2c2357e2046751b7cd37ec9a744e7ce7f5c2`
- 收尾分支相對預設分支：ahead 8／behind 0。
- 原髒工作樹：Branch `codex/mvp6-checkpoint-branching-spec`、HEAD
  `89e522f18a946426232f5d56009fea0262329b47`、約 124 項修改；本窗口未整理或改動。

## 7. 封存後唯一建議工作包

名稱：

```text
TOOL-1.0 — Reliable Local Story Validation Tool Finalization
```

只做：

1. 列出並凍結正式支援的 CLI。
2. 把實驗命令標為 Experimental／Historical。
3. 建立全新電腦可理解的安裝與快速開始說明。
4. 建立一個最小世界、一個完整回合、一個失敗案例。
5. 建立一鍵 smoke test（冒煙測試 / 最小可用性檢查）。
6. 在乾淨環境驗證資料重建、範例與完整測試。
7. 建立可下載壓縮檔與 `v1.0.0` Release。

不做：

- 新故事功能；
- 新模型；
- 新媒體；
- 新介面；
- 新世界內容；
- 原髒工作樹大整理。

## 8. 真正停止點

本窗口到此收尾。沒有開始 `TOOL-1.0`，沒有合併預設分支，也沒有發布 Release。

下一個工作階段應先讀：

1. `PROJECT_STATE.md`
2. `PLANS.md` Current Plan Window V25
3. 本文件

然後只決定是否啟動 `TOOL-1.0`，不得自動恢復圖片、影片、NPC Actor、雲端 API 或第二話。
