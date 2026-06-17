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

## Persistence Boundary

SQLite 是唯一狀態來源。JSON 檔案是 turn protocol 的交換格式，不是長期狀態來源。

AI/Codex 只能產生 `AiTurnResult` JSON；只有 `resolve-turn` 可以在 validator 通過後寫入 SQLite。
