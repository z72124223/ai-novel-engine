# AI Novel Engine 專案狀態

> 這是本地專案狀態摘要（Project State）。正式規範以 `AGENTS.md`、正式規格文件與經驗證的程式碼為準；本檔案不取代它們。

## Current Work Package

- ID: `WP-001 Project Governance Reset`
- Status: `COMPLETED`
- Scope: 治理文件與 Codex 工作流程入口
- Current step: 等待下一個 Work Package 指示

## Confirmed Repository Facts

- 專案是本地單進程 Kotlin/JVM CLI 互動小說引擎。
- 狀態變更必須經過 `AiTurnResultValidator`、`StateReducer` 與既有交易流程。
- SQLite migration 只能依既有專案規則以 additive migration（加法式 migration）處理。
- `docs/HANDOFF.md` 定義 `01 -> 02 -> 03 -> 01` 的工作交接循環。
- 本地工作樹在建立本檔案前已有大量未提交修改；那些修改不屬於本 Work Package，除非另有明確證據。

## Active Boundaries

- 目前只做一個 Work Package，完成驗證後再決定下一包。
- 不直接修改 SQLite，不繞過 Validator、`StateReducer`、`contextHash`、`stateVersion` 或交易邊界。
- 不新增 web server、port、外部 AI API、Docker、React/Next、Rust 或其他既有禁止項目。
- 不把規劃文件、候選資料或聊天內容直接視為正式 Canon（正式世界設定）。

## Source Of Truth Order

1. 使用者當前明確指示。
2. Repository 內適用範圍的 `AGENTS.md`。
3. 已驗證的程式碼、測試與 CLI 輸出。
4. `docs/` 中與目前 milestone（里程碑）相符的規格與交接文件。
5. 本檔案的摘要與工作狀態。

## Known Unknowns

- GitHub 遠端目前是否與本地工作樹同步，尚未在本 Work Package 中驗證。
- 後續產品方向與下一個 Work Package，須在本包完成並檢查後再決定。

## Update Rule

每個 Work Package 完成後，更新本檔案的工作包狀態、已驗證事實、未知事項與下一步；不得把未驗證推測寫成事實。
