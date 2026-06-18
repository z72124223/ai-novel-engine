# Architecture

v1 是本地單進程 CLI。它沒有 server、沒有背景 worker、沒有網路 API。

## Flow

```text
CLI
→ StoryRepository(SQLite)
→ ContextCompiler
→ context_packet.json
→ ai_turn_result.json
→ AiTurnResultValidator
→ StateReducer
→ SQLite state + narrative output
```

## Components

- `Main.kt`：解析 CLI 命令並呼叫服務。
- `StoryRepository`：定義故事狀態存取介面。
- `SqliteStoryRepository`：SQLite 實作，負責 migration、seed、查詢、寫入。
- `ContextCompiler`：從 SQLite 讀取當前 project 狀態並輸出 `ContextPacket`。
- `AiTurnResultValidator`：驗證 result 是否符合 turn、project、scene contract 與 forbidden reveal 規則。
- `StateReducer`：套用合法 `StateDelta`，寫入 facts、events、secret reveal、character state、task queue、turn logs。
- `StallDetector`：偵測連續兩回合沒有有效狀態變化。
- `MultiTurnRuntime`：MVP-2 session runtime，負責 `start-session`、`prepare-turn`、`resolve-turn`、pending context、context hash、冪等 resultId 與 transaction rollback。
- `MemoryRetriever`：用 deterministic scoring 從角色可存取記憶中選出本回合 context 記憶。
- `KnowledgeBoundaryValidator`：阻擋角色 action 使用未授權 fact、memory、knowledge、item 或 technique。
- `MemoryLifecycleService`：驗證 AI 提出的 memory candidate；正式寫入由 repository 在 successful `resolve-turn` transaction 內完成。
- `EmotionReducer`：驗證 emotion delta 需要 trigger/source/reason，並限制單回合變化。
- `ContextBudgeter`：限制 context 大小，優先保留協議身分、安全欄位、scene contract 與世界硬規則。

## Persistence Boundary

SQLite 是唯一狀態來源。JSON 檔案是 turn protocol 的交換格式，不是長期狀態來源。

AI/Codex 只能產生 `AiTurnResult` JSON；只有 `resolve-turn` 可以在 validator 通過後寫入 SQLite。

## MVP-3 Read/Write Boundary

`prepare-turn` 讀取 session、scene、角色狀態、可見記憶與 knowledge claims，產生 `retrievedMemories`、`knowledgeClaims` 與 `contextBudget`。它不增加 `recall_count`，也不寫入 `memory_recall_logs`。

`resolve-turn` 重新載入 pending context，驗證 result 身分、hash、知識邊界、forbidden reveal、memory candidate、knowledge update 與 emotion delta。通過後才在同一 transaction 內寫入 state deltas、facts、events、memory candidates、knowledge updates、recall logs、recall counts、session state 與 turn log。
