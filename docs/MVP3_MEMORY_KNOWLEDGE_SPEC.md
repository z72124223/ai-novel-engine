# MVP-3：角色記憶、知識邊界與情緒因果

## 0. 前置條件

MVP-2 reliable multi-turn runtime 已通過：

- 多回合 session
- contextId / resultId / stateVersion / contextHash
- stale 與 duplicate 防護
- transaction rollback
- forbidden reveal 驗證
- recentTurnSummaries 上限
- 五回合端到端測試

本階段的目的不是增加更多劇情內容，而是讓角色能：

1. 只記得自己有權知道的事。
2. 在適當情境召回相關過去。
3. 讓記憶影響情緒、判斷與行動。
4. 區分世界真相與角色相信的版本。
5. 在有限 context 預算內保持人物深度。
6. 不因記憶數量增長而把整個資料庫送給 AI。

## 1. 禁止事項

- 不做 Android、Web UI、HTTP server、port。
- 不接外部 AI API。
- 不加入 Rust、Docker、向量資料庫、embedding 或網路服務。
- 不修改 `001_init.sql` 或 `002_multi_turn_runtime.sql`。
- 不把全部記憶、全部 facts 或完整歷史放進 context packet。
- 不允許 AI 直接新增、覆寫或刪除 SQLite 記憶。
- 不允許角色使用自己不知道的 fact、secret、memory、item 或 technique。
- 不允許新資訊靜默覆蓋舊信念。
- 不為測試輸入硬編碼結果。

## 2. Git 與 migration

1. 確認 MVP-2 已提交。
2. 建立分支：
   `feat/character-memory-knowledge-runtime`
3. 新增：
   `app/src/main/resources/migrations/003_memory_knowledge_runtime.sql`
4. 所有 schema 變更必須 additive，保留既有資料。
5. SQLite foreign keys 必須維持啟用。

## 3. 記憶與知識的概念分離

### 3.1 Canonical fact

世界層真相或已登記事件。角色不會因為世界資料庫知道，就自動知道。

### 3.2 Character knowledge claim

角色對某個命題的認知，可為：

- `KNOWN`
- `BELIEVED`
- `SUSPECTED`
- `DOUBTED`
- `MISREMEMBERED`
- `UNKNOWN`

每項具有 confidence，且可以與 canonical fact 不一致。

### 3.3 Character memory

角色親歷、聽聞或形成的記憶。類型至少包含：

- `CANON_PAST`
- `EPISODIC`
- `EMOTIONAL`
- `RELATIONAL`
- `SEMANTIC`
- `TRAUMA`
- `PROMISE`
- `RUMOR`

記憶不是世界真相；它可以不完整、偏誤或錯誤。

## 4. Schema

先檢查既有 `character_memories` 欄位，再以 migration 增加缺少欄位，不重建或遺失資料。

記憶至少需支援：

- id
- project_id
- owner_character_id
- memory_type
- summary
- details_json
- source_type
- source_id
- subject_type
- subject_id
- tags_json
- trigger_tags_json
- emotional_signature_json
- salience（0..100）
- confidence（0..100）
- reliability（0..100）
- created_turn_number
- last_recalled_turn_number
- recall_count
- decay_rate
- visibility
- status
- is_canon
- supersedes_memory_id
- created_at
- updated_at

新增 `character_knowledge`，至少包含：

- id
- project_id
- character_id
- proposition_key
- statement
- epistemic_state
- confidence
- source_fact_id
- source_memory_id
- acquired_turn_number
- updated_turn_number
- visibility
- status
- created_at
- updated_at

新增 `memory_recall_logs`，至少包含：

- id
- session_id
- turn_number
- context_id
- character_id
- memory_id
- retrieval_score
- retrieval_reasons_json
- included_in_context
- created_at

建立合理 foreign keys、索引及必要唯一約束。避免同一角色、同一來源、同一摘要重複建立相同 runtime memory。

## 5. 新增核心服務

至少實作：

- `MemoryRetriever`
- `KnowledgeBoundaryValidator`
- `MemoryLifecycleService`
- `EmotionReducer`
- `ContextBudgeter`

責任：

### MemoryRetriever

根據當前：

- 玩家輸入
- 場景
- active thread
- 角色 currentGoal
- 角色 dominant emotions
- 相關人物、物件、地點
- tags
- dependency edges
- recency
- salience

產生可重現、可解釋的記憶排名。

相同 DB state 與相同輸入，排名必須 deterministic。

不得使用 embedding、外部模型或向量資料庫。

### KnowledgeBoundaryValidator

驗證：

- 角色是否知道該 fact / secret / memory。
- 角色 action 是否使用未授權資訊。
- characterAction 中引用的 usedMemoryIds、usedItemIds、usedTechniqueIds 是否對該角色可用。
- revealedFacts 是否真的可在本回合揭露。
- 世界 canonical fact 不得自動注入角色知識。

### MemoryLifecycleService

只在成功 `resolve-turn` 的 transaction 內：

- 新增合法 runtime memory。
- reinforcement：提高 salience / confidence。
- weakening：降低 confidence。
- reframe：建立新版本並以 supersedes 關聯舊版本。
- archive：只改狀態，不物理刪除。
- 更新 recall count 與 last recalled turn。

失敗或 rejected turn 不得留下任何記憶變化。

### EmotionReducer

- 情緒值維持 0..100。
- 每個 delta 必須有 trigger/source/reason。
- 一般單回合單一情緒絕對變化不得超過 25。
- 超過 25 必須引用已登記的高嚴重度事件。
- 記憶觸發造成的 emotion delta 必須引用 memoryId。
- 不允許 AI 無理由直接把情緒設成任意絕對值。

### ContextBudgeter

預設限制可配置，初始建議：

- `maxContextBytes = 24576`
- `maxMemoriesPerCharacter = 5`
- `maxKnowledgeClaimsPerCharacter = 8`
- `maxRecentTurnSummaries = 3`

優先級：

1. 協議身分與安全欄位
2. forbidden reveals
3. scene contract 與世界硬規則
4. 角色最新狀態與合法行動
5. 高相關記憶
6. 高相關 knowledge claims
7. recent summaries
8. 次要描述

不得因預算裁切掉安全欄位、forbidden reveals 或硬規則。

Context packet 新增 `contextBudget`：

- maxBytes
- actualBytes
- includedMemoryCount
- omittedMemoryCount
- includedKnowledgeCount
- omittedKnowledgeCount
- truncationApplied
- truncationReasons

## 6. 記憶檢索評分

請實作並記錄一個明確、可測試的 deterministic scoring 公式。

至少考慮：

- salience
- tag overlap
- direct entity match
- current goal match
- emotional congruence
- dependency relation
- recency
- unresolved promise / trauma / conflict
- decay penalty

要求：

1. 高度相關的舊記憶可以勝過近期但無關的記憶。
2. author-only 或角色未知記憶在 ranking 前就排除。
3. inaccessible / archived 記憶不得送入 context。
4. 每個被選中記憶需帶 `retrievalReasons`，例如：
   - matched player input tag
   - related to current goal
   - related item present
   - emotional trigger matched
5. 分數與理由寫入 `memory_recall_logs`。
6. `prepare-turn` 仍必須是 read-only，不得增加 recall_count；只有成功 resolve 後才正式計入 recall。

## 7. 協議擴充

### ContextPacket 新增

- `retrievedMemories`
- `knowledgeClaims`
- `contextBudget`

每個 retrieved memory 至少包含：

- memoryId
- ownerCharacterId
- type
- summary
- emotionalSignature
- confidence
- salience
- retrievalScore
- retrievalReasons
- allowedUses

每個 knowledge claim 至少包含：

- claimId
- characterId
- propositionKey
- statement
- epistemicState
- confidence
- sourceType

禁止把 canonical hidden truth 放進 character-facing knowledgeClaims。

### AiTurnResult 新增

- `memoryCandidates`
- `knowledgeUpdates`

每個 memory candidate 至少包含：

- candidateId
- ownerCharacterId
- memoryType
- summary
- sourceEventIds
- sourceFactIds
- tags
- emotionalSignature
- salience
- confidence
- reason

每個 knowledge update 至少包含：

- characterId
- propositionKey
- requestedState
- confidenceDelta
- sourceEventIds
- sourceFactIds
- sourceMemoryIds
- reason

## 8. Runtime memory 建立規則

AI 只能提出 candidate；引擎決定是否接受。

接受條件：

1. owner 在本回合有感知該事件的合理條件。
2. sourceEventIds / sourceFactIds 必須存在且屬於當前已接受回合或既有可知資料。
3. candidate 不含 forbidden reveal。
4. candidate 不把角色未知的真相寫成已知。
5. summary 不得只是 narrative 全文複製。
6. candidate 必須具備 story/character value，避免每回合產生大量瑣碎記憶。
7. 重複 candidate 應去重或 reinforcement，不建立重複 row。

Canon memory：

- 不可被 AI 覆寫。
- reframe 不刪除原記憶。
- 角色可以重新理解 canon 過去，但原始紀錄保留。

## 9. 知識衝突

當新資料與既有 belief 衝突：

- 不得直接把 belief 改成 KNOWN。
- 產生 conflict result：
  - old claim
  - new evidence
  - confidence delta
  - recommended epistemic transition
- 可從 BELIEVED 轉為 DOUBTED 或 SUSPECTED。
- 只有達到明確證據條件才能轉為 KNOWN。
- canonical world fact 不得因角色 belief 改變。

允許角色帶著錯誤 belief 合理行動，只要 context 清楚標示它是角色 belief，不是世界真相。

## 10. CLI

新增：

### show-character-mind

```bat
gradlew.bat run --args="show-character-mind --session SESSION_ID --character CHARACTER_ID"
```

顯示：

- current goal
- dominant emotions
- top active memories
- knowledge claims
- suspected / doubted beliefs
- relationship state
- inaccessible memory 數量，但不得顯示 author-only 內容

### preview-recall

```bat
gradlew.bat run --args="preview-recall --session SESSION_ID --character CHARACTER_ID --input \"玩家輸入\""
```

顯示：

- 候選 memory IDs
- score
- reasons
- 是否會放入 context
- budget 影響

必須 read-only。

### explain-context

```bat
gradlew.bat run --args="explain-context --file out\turn_context.json"
```

顯示：

- 為什麼每項記憶或 knowledge 被選入
- 哪些被 budget 排除
- context 大小
- 安全欄位是否完整

## 11. 測試要求

至少新增以下測試：

1. 角色未知 secret memory 不會被 retrieval。
2. author-only memory 不會進 character context。
3. 相同輸入與 state 的 ranking deterministic。
4. 高相關舊記憶勝過無關近期記憶。
5. context budget 生效且安全欄位不被裁切。
6. 角色 action 引用未知 fact 時被拒絕。
7. 角色可依錯誤 belief 行動，但 canonical fact 不變。
8. 成功 turn 的 memory candidate 在同一 transaction 內寫入。
9. rejected turn 不留下 memory、knowledge、recall log 或 emotion 變更。
10. emotion delta 超界被拒絕或按規格處理。
11. memory reframe 建立 supersedes 關係，不刪除舊 row。
12. preview-recall 不改 DB。
13. prepare-turn 不增加正式 recall_count。
14. 成功 resolve 後 recall_count 只增加一次。
15. duplicate result 不會重複增加 recall_count 或建立 memory。
16. knowledge conflict 不會靜默覆蓋原 belief。
17. runtime memory 去重或轉為 reinforcement。
18. forbidden reveal 出現在 memoryCandidates 或 knowledgeUpdates 時被拒絕。

既有 MVP-1、MVP-2 測試必須全部保留並通過。

## 12. 端到端驗收

使用全新 DB：

`data\memory-e2e.db`

使用 demo project 建立 session，執行至少四回合。測試內容需自然覆蓋：

1. 玩家展示沈家銅牌，觸發一條與過去誓言有關的舊記憶。
2. 沈霜因既有 belief 懷疑玩家可能是外人安排的誘餌。
3. 新 evidence 與該 belief 衝突，使 belief 從 BELIEVED 或 SUSPECTED 轉為 DOUBTED，而非瞬間變成完全信任。
4. 成功回合形成一條新的 episodic 或 relational memory。
5. 後續回合能召回該 runtime memory，並合理影響情緒或行動。
6. 未揭露 secret 始終不進沈霜 knowledge。
7. Context packet 保持在預算內。
8. 每回合只載入相關記憶，不載入全部記憶。
9. 沈霜的情緒變化都有 trigger / source。
10. 沈霜仍然依自己的目標與認知行動，不成為劇情工具人。

不得硬編碼 E2E 玩家句子或結果。

## 13. 文件

更新：

- README.md
- AGENTS.md
- docs/ARCHITECTURE.md
- docs/DATA_MODEL.md
- docs/TURN_PROTOCOL.md
- docs/VALIDATION_RULES.md
- docs/ROADMAP.md

新增：

- `docs/MEMORY_AND_KNOWLEDGE.md`

文件說明：

- world truth、character knowledge、memory 的差異
- 記憶類型
- visibility 與知識邊界
- retrieval scoring
- context budget
- runtime memory candidate 驗證
- belief conflict
- emotion delta 因果
- recall logging
- reframe / supersedes
- Codex 如何依 retrieved memories 生成角色行動

AGENTS.md 只加入長期規則並引用本規格，不複製全文。

## 14. 驗收與回報

執行：

```bat
gradlew.bat clean test --no-daemon
```

最終回報：

1. VERDICT：PASS / FAIL
2. branch 與 commit 狀態
3. migration 摘要
4. 新增及修改檔案
5. 全部測試數與結果
6. retrieval scoring 公式
7. 四回合 E2E 摘要
8. 每回合選入的 memory IDs、scores、reasons
9. knowledge claims 狀態變化
10. emotion delta 與 trigger/source
11. 新 runtime memories
12. belief conflict 處理結果
13. context bytes 與 budget 狀況
14. 未揭露 secrets 狀態
15. rejected / rollback / duplicate 測試結果
16. 尚未解決問題

若失敗，先修到通過，不擴充本規格以外的功能。
