# MVP-2：可靠的多回合互動運行規格

## 0. 範圍與禁止事項

目前單回合 MVP 已通過端到端驗收。本階段只實作可靠的多回合 runtime。

禁止：

- Android、外部 AI API、HTTP server、port
- Rust、Docker、Web UI、多代理框架
- AI 或 Codex 直接寫 SQLite
- 繞過 Validator
- 修改既有 `001_init.sql`
- 硬編碼 Windows 使用者絕對路徑
- 將全量歷史放入 context packet
- 將 author-only 秘密當成角色已知資訊

Codex 仍透過 JSON 檔案充當暫時的 AI 生成窗口。

## 1. Git 安全基線

1. 執行 `git status`。
2. 確認沒有追蹤：
   - `data/*.db*`
   - `out/*.json`
   - `.gradle/`
   - `**/build/`
   - 暫存 JDK
3. 若目前版本尚未提交，提交：
   `chore: establish tested single-turn story engine MVP`
4. 建立分支：
   `feat/reliable-multi-turn-runtime`

## 2. 核心目標

支援至少五回合：

```text
start-session
→ prepare-turn
→ Codex 產生 ai_turn_result.json
→ resolve-turn
→ 更新 SQLite
→ 下一回合
```

每回合必須：

1. 使用最新資料庫狀態。
2. 防止舊 context packet 被套用。
3. 防止同一 result 被套用兩次。
4. 所有狀態變更在單一 SQLite transaction 中完成。
5. 任一 delta 不合法時整回合回滾。
6. 不得揭露未解鎖秘密。
7. 不把完整歷史塞入 context packet。
8. 保存最近必要的回合摘要與狀態變化。

## 3. 協議強化

### ContextPacket 新增欄位

- `protocolVersion`
- `contextId`
- `sessionId`
- `turnNumber`
- `baseStateVersion`
- `contextHash`
- `generatedAt`
- `recentTurnSummaries`
- `sceneProgress`
- `directorPressure`
- `outputRequirements`

### AiTurnResult 新增欄位

- `protocolVersion`
- `resultId`
- `contextId`
- `sessionId`
- `turnNumber`
- `baseStateVersion`
- `contextHash`
- `narrative`
- `turnSummary`
- `characterActions`
- `stateDeltas`
- `revealedFacts`
- `memoryEffects`
- `sceneProgressUpdates`
- `newTasks`
- `validationNotes`

### 驗證要求

1. `contextId`、`sessionId`、`turnNumber`、`baseStateVersion`、`contextHash` 必須吻合。
2. 不吻合即拒絕。
3. 已套用過的 `resultId` 不得再次套用。
4. 不支援的 `protocolVersion` 必須拒絕。
5. `contextHash` 使用 canonical JSON 或明確的穩定序列化方式。
6. 文件說明 hash 生成規則，不能依賴不穩定 key 順序。

## 4. Migration

不得修改 `001_init.sql`。新增：

`app/src/main/resources/migrations/002_multi_turn_runtime.sql`

新增 `story_sessions`：

- `id`
- `project_id`
- `current_scene_id`
- `next_turn_number`
- `state_version`
- `status`
- `created_at`
- `updated_at`

為 `turn_logs` 增加或保存：

- `session_id`
- `turn_number`
- `context_id`
- `result_id`
- `base_state_version`
- `context_hash`
- `turn_summary`
- `validation_status`

必要時為 `scenes` 增加：

- `progress_json`
- `completed_requirements_json`

建立唯一索引：

- `result_id` 不可重複
- `session_id + turn_number` 不可有兩個正式成功回合

使用 foreign key，連線初始化必須執行：

`PRAGMA foreign_keys = ON`

## 5. CLI

### start-session

```bat
gradlew.bat run --args="start-session --project demo-project --scene demo-scene"
```

輸出：

- sessionId
- currentSceneId
- nextTurnNumber
- stateVersion

### prepare-turn

```bat
gradlew.bat run --args="prepare-turn --session SESSION_ID --input \"玩家輸入\" --out out\turn_001_context.json"
```

要求：

- 從最新狀態編譯 context packet
- 產生 contextId 與穩定 contextHash
- 不改變正式劇情狀態
- 保存 pending context metadata

### resolve-turn

```bat
gradlew.bat run --args="resolve-turn --session SESSION_ID --result out\turn_001_result.json"
```

流程：

- 驗證協議與 context 身分
- 驗證角色、秘密、規則、scene contract、delta
- 單一 transaction 套用
- 寫入 turn_logs、state_deltas、facts、events
- `stateVersion + 1`
- `nextTurnNumber + 1`
- 輸出 narrative 與狀態摘要

### show-session

至少顯示：

- sessionId
- currentScene
- nextTurnNumber
- stateVersion
- 最近回合摘要
- 沈霜目前情緒與目標
- 已揭露 facts
- 未揭露 secrets 數量
- scene progress
- idleTurnCount

### abort-pending-turn

放棄尚未 resolve 的 context，不改正式故事狀態。

## 6. ContextCompiler 限制

只載入：

- 當前場景與 scene contract
- 出場角色及最新狀態
- 相關記憶、道具、功法、世界規則
- forbidden reveals
- 尚未完成的 scene requirements
- 最近最多三回合 `turnSummary`
- 最近必要的 state delta 摘要

禁止：

- 所有 turn_logs narrative
- 完整專案歷史
- 無關角色、記憶、道具、規則

`turnSummary` 必須是短摘要，不能複製整段 narrative。

## 7. 停滯處理

有意義的狀態變化包括：

- 新事實
- 顯著情緒或關係變化
- 威脅、位置或資源變化
- 線索進度
- 角色重要決定
- scene requirement 完成

若連續兩回合沒有有意義變化：

1. 下一個 context packet 啟用 `directorPressure`。
2. 只允許：
   - 時間壓力
   - 環境變化
   - 外部勢力行動
   - 道具反應
   - 危險接近
3. 不得直接命令角色坦白或服務劇情。
4. 設定 `mustProduceMeaningfulStateChange=true`。
5. 引擎不得自行編寫 narrative。

## 8. 角色自主性

角色只根據：

- currentGoal
- fear
- desire
- emotion
- memory
- knownFacts
- relationship
- availableActions

場景目的不得成為角色內在任務。

每個 `characterAction` 必須包含：

- characterId
- intent
- visibleAction
- hiddenMotive
- triggeringConditions
- usedMemoryIds
- usedItemIds
- usedTechniqueIds
- characterFitScore
- storyProgressScore

`characterFitScore < 70` 時拒絕，除非有已建立的心理轉折依據。

角色不得使用自己未知的 fact、secret、memory、item 或 technique。

## 9. 原子性與冪等測試

必須證明：

1. 同一 `resultId` 第二次套用被拒絕，DB 不變。
2. 舊 `baseStateVersion` 被拒絕。
3. `contextHash` 不符被拒絕。
4. `turnNumber` 不符被拒絕。
5. 多個 delta 中任一違法時全部不寫入。
6. turn_logs 寫入失敗時 delta 全部回滾。
7. forbidden reveal 出現在 narrative、revealedFacts、stateDeltas、memoryEffects 或 newTasks 任一處皆拒絕。
8. 成功 resolve 後 stateVersion 只增加一次。
9. 成功 result 不可重複套用。
10. prepare-turn 不改正式故事狀態。

## 10. 五回合 E2E

使用全新 DB：

`data\multi-turn-e2e.db`

玩家輸入：

1. `我質問沈霜：「你是不是早就知道祠堂下面有東西？」`
2. `我沒有向前，只把沈家銅牌放到她能看見的位置：「那你為什麼擋著後殿？」`
3. `我收起銅牌，改問：「昨夜門閂被動過時，你人在什麼地方？」`
4. `我注意她袖口裡的手勢，但不揭穿，只說：「外面的腳步聲不是我的人。」`
5. `我退到柱影旁，讓出門口：「你可以不相信我，但追兵不會等你想清楚。」`

每回合：

1. prepare-turn
2. Codex 只讀本回合 context
3. 產生對應 result JSON
4. resolve-turn
5. show-session

五回合至少驗證：

- 一次記憶觸發造成情緒變化
- 一次道具影響角色選擇
- 一條世界規則限制可用行動
- 一次角色目標合理變化
- 一個 scene requirement 完成
- forbidden secret 未提前揭露
- 沈霜沒有無理由坦白
- 行動不是單純替劇情服務
- 每回合使用上一回合最新狀態
- recentTurnSummaries 最多三回合
- context packet 沒有無限制膨脹

不得硬編碼上述玩家輸入或結果。

## 11. 文件

更新：

- `README.md`
- `docs/ARCHITECTURE.md`
- `docs/DATA_MODEL.md`
- `docs/TURN_PROTOCOL.md`
- `docs/VALIDATION_RULES.md`
- `docs/ROADMAP.md`
- `AGENTS.md`

說明：

- 單回合與多回合差異
- pending context 生命週期
- stateVersion
- contextHash
- resultId 冪等性
- transaction rollback
- recent turn summary
- stall pressure
- Codex 作為人工 AI 窗口的流程

`AGENTS.md` 只保留長期規則，並引用本文件；不要把本文件全文複製進 AGENTS.md。

## 12. 驗收

執行：

```bat
gradlew.bat clean test
```

最終回報：

1. VERDICT：PASS / FAIL
2. Git 分支與提交狀態
3. migration 摘要
4. 新增及修改檔案
5. 測試數與結果
6. 五回合逐回合摘要
7. 每回合 stateVersion
8. 每回合 contextId/resultId
9. context packet 大小變化
10. 沈霜情緒、目標、行動變化
11. 記憶、道具、功法、世界規則使用位置
12. scene requirements 狀況
13. 未揭露秘密狀態
14. stale、duplicate、hash mismatch、rollback 測試結果
15. 尚未解決問題

失敗時先修到測試通過，不擴充其他功能。
