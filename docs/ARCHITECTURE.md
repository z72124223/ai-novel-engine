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
## MVP-4 Scene Thread Runtime

MVP-4 keeps the same local single-process boundary and adds only additive SQLite schema in `004_scene_thread_runtime.sql`.

- `SceneLifecycleService` exposes the current scene instance and enforces one current `ACTIVE` or `READY_TO_CLOSE` instance per session.
- `SceneRequirementEvaluator` validates requirement progress and evidence.
- `StoryThreadManager` validates thread progress while keeping `director_goal` out of character-facing actions.
- `TransitionPlanner` emits location-aware `availableTransitions`.
- `TransitionValidator` accepts only context-listed transition proposals and rejects unmet exit gates, missing world-event evidence, player refusals and stale scene instances.
- `WorldClockService` validates time advance proposals; travel time is applied with accepted transitions.
- `ScheduledWorldEventService` exposes due events and consumes acknowledged events only after a successful resolve.
- `NarrativeContinuityValidator` checks actor location, item reachability and world-event knowledge boundaries.

Scene/thread/time/world-event writes happen inside the existing `resolve-turn` transaction after protocol, stateVersion and contextHash validation.

## MVP-5 Setting Pack Runtime

MVP-5 keeps the local single-process boundary and adds only additive SQLite schema in `005_setting_pack_runtime.sql`.

- `SettingPackParser` reads UTF-8 JSON setting packs.
- `SettingPackSchemaValidator` checks required pack metadata, IDs, versions, modes, visibility and dependencies.
- `CanonSemanticValidator` checks references, cycles, author-only knowledge leaks, technique cost/limitation balance and active-session patch blockers.
- `SettingPackDiffService` compares incoming entities with `entity_revisions`.
- `ImpactAnalyzer` traverses `dependency_edges` and active sessions before apply.
- `SettingPackImportService` enforces parse -> validate -> diff -> impact preview -> transaction apply.
- `DependencyGraphService` provides bounded graph display.
- `SettingContextResolver` adds only relevant setting excerpts to `ContextPacket`.

Codex may draft setting pack JSON, but formal canon writes must go through `apply-setting-pack`; no direct SQL, `sqlite3`, scripts or network APIs are part of this runtime.
