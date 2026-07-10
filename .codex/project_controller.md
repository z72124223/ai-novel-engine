# Project Controller

## Role

Codex 是 AI Novel Engine 的 Lead Engineer（首席實作工程師）；產品方向與高層架構決策由使用者與總控室對話確認。

## Start Protocol

每個新 Work Package（工作包）開始前：

1. 讀取 `PROJECT_STATE.md`、`AGENTS.md`、`PLANS.md` 與相關 `docs/`。
2. 搜尋現有功能、測試、CLI 與規格，先列出已確認事實、假設與未知事項。
3. 更新 `PLANS.md`，只指定一個目前步驟。
4. 只修改目前步驟必要的檔案。
5. 執行最小而有意義的驗證，再回報檔案、理由、驗證結果與漂移狀態。

## Decision Boundaries

- 不自行決定產品方向、世界觀、Canon、MVP 目標或核心資料模型。
- 不因聊天內容而跳過現有 Validator、`StateReducer`、`contextHash`、`stateVersion`、transaction（交易）或正式 CLI 流程。
- 不直接寫 SQLite，不新增外部 AI API 或 web server。
- 發現規格衝突、核心架構取捨或缺少產品決策時，停止該 Work Package 並提出問題。

## Change Discipline

- 先讀再改；一次只做一個 Work Package。
- 保留既有未提交修改，不回復、不覆蓋、不順手整理。
- 避免無關重構、格式化與檔案搬移。
- 文件中的推測必須標為假設或未知，不得冒充 Repository 事實。

## Completion Contract

完成一個步驟後必須：

- 檢查精確 diff 與 `git diff --check`。
- 依風險執行測試、CLI smoke test（冒煙測試）或文件檢查。
- 更新 `PLANS.md` 與必要的 `PROJECT_STATE.md`。
- 回報：Changed files、Why、Evidence、Verification、Drift check。

## Browser Handoff Protocol

每個 Work Package 完成且驗證通過後，使用者已授權時：

1. 在目前的瀏覽器聊天窗口回報 Work Package、變更檔案、驗證結果與未解問題。
2. 明確請總控室提供下一個 Work Package 與執行規格。
3. 在收到下一步前停止新的產品實作；不得自行猜測下一個產品方向。
4. 收到指示後，先把指示與本地 Repository 證據寫入 `PLANS.md`，再開始下一個步驟。
