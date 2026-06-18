# Pilot-1 Context Metadata Normalization

## 0. 問題摘要

正式 Pilot：

- DB：`data/pilot1-rerun.db`
- SESSION_ID：`session-75e1b190-542c-49c0-9a5a-6b242bed8e97`
- stateVersion：7
- nextTurnNumber：8
- 已完成真人回合：7

Turn 8 在 `explain-context-budget` 階段失敗：

- before trimming：65854 bytes
- after trimming：24621 bytes
- max：24576 bytes
- over：45 bytes
- mandatoryBytes：6370
- optionalBytesRemaining：18251
- `otherMetadata` after trimming：16997 bytes
- `contextBudgetMetadata`：1460 bytes

失敗原因：

`No optional or compactable context sections remain`

這表示問題不是玩家輸入太長，而是最終 ContextPacket 內仍存在大量未分類、被錯誤視為不可裁切，或與正式欄位重複的 metadata。

本修補只處理 context schema 正規化與 metadata 分離，不執行正式 Turn 8。

## 1. 禁止事項

- 不提高 `maxContextBytes=24576`。
- 不縮短或改寫 Turn 8 玩家輸入來規避問題。
- 不修改正式 Pilot DB 的故事狀態。
- 不執行正式 Turn 8 的 prepare/resolve。
- 不修改 migration 001 至 005。
- 不新增 migration。
- 不刪除正式 memories、facts、settings、turn logs 或 secrets。
- 不新增 Android、網路、API、server、port、Rust、Docker、embedding 或向量資料庫。
- 不為特定 session、entity ID 或中文句子硬編碼。
- 不把 audit/forensics 詳細資料塞回正式 ContextPacket。

## 2. Git 基線

先封存已通過的 Context Budget V2：

```bat
git status
gradlew.bat clean test --no-daemon
git diff --check
git add .
git commit -m "fix: improve deterministic context fitting and budget forensics"
git tag -a pilot-1-budget-v2 -m "Deterministic context fitting and budget forensics"
```

建立分支：

```bat
git switch -c fix/pilot1-context-metadata-normalization
```

保留：

- `data/pilot1-rerun.db`
- 正式 SESSION_ID
- stateVersion=7
- nextTurnNumber=8

所有可修改重現使用 DB 複本：

`data/pilot1-metadata-normalization-e2e.db`

## 3. 先做精確鑑識

使用完全相同的 Turn 8 玩家輸入，對正式 DB 只執行 read-only explain。

報告必須把 `otherMetadata=16997` 展開成確切 JSON paths，至少列出：

- path
- UTF-8 bytes
- value type
- semantic owner
- mandatory / optional
- duplicatedWith
- canCompact
- canMoveToAuditSidecar
- inclusion reason

不得只回報 `otherMetadata` 聚合值。

特別檢查：

- legacy top-level aliases
- `characters` 與 `activeCharacters`
- `characterStates` 與 active character runtime view
- `facts` / `recentEvents` / `recentTurnSummaries`
- `worldRules` 與 relevant setting constraints
- `sceneContract` 與 scene requirements
- dependency paths / provenance / revision summaries
- budget forensics fields
- retainedItems / removedItems / largest paths / duplicate groups
-完整 schema 或 output example 是否被重複嵌入
- null、empty array、empty object、default values
- 同一 entity 的 full profile 與 compact view

## 4. 最終 ContextPacket 使用明確 allowlist

建立單一、版本化的 final ContextPacket top-level allowlist。

只有正式協議欄位可以寫入 final packet。任何未列入欄位：

1. 必須明確分類並加入 schema；或
2. 移至 audit sidecar；或
3. 在 build 階段拒絕。

禁止將未知欄位統一歸入 `otherMetadata` 並視為 mandatory。

建議正式 top-level sections 只包括既有必要協議，例如：

- protocol identity
- player input
- project/session summary
- scene lifecycle / scene contract / requirements
- active characters compact runtime views
- retrieved memories
- character knowledge claims
- relevant settings compact views
- world hard rules
- active story threads
- available transitions
- pending world events
- world clock
- continuity constraints
- forbidden reveals safety representation
- recent turn summaries
- output requirements
- minimal context budget summary
- minimal canon revision summary

實際名稱可沿用現有 model，但不得同時輸出 legacy 與 canonical 兩套欄位。

## 5. Diagnostics 必須移出正式 ContextPacket

下列詳細資料只能存在於：

- `explain-context-budget` 報告
- `audit-context` sidecar
- prepare failure report

不得放進送給 AI 的正式 ContextPacket：

- perSectionBytesBefore / After
- topLargestJsonPaths
- duplicateContentGroups
- full removedItems
- full retainedItems
- trimming simulation history
- detailed provenance
- complete dependency traversal
- validation debug traces
- full canonical snapshots
-完整 schema 描述

正式 `contextBudget` 只保留最小欄位：

- maxBytes
- actualBytes
- includedMemoryCount
- omittedMemoryCount
- includedKnowledgeCount
- omittedKnowledgeCount
- includedSettingCount
- omittedSettingCount
- truncationApplied
- concise truncationReasonCodes

不得保存長篇自然語言裁切說明。

## 6. Compact 與去重規則

### 6.1 Active Character

每個 active character 的正式 runtime view 只保留本回合必要資料：

- ID / name
- current location
- current goal
- dominant emotion /必要 emotion values
- knowledge/belief subset
- available / forbidden actions
- relevant relationship summary
- currently held/used item and technique references
- character-facing constraints

完整 canon profile、完整 history、完整 relationship map 不得重複進入。

### 6.2 Facts / Events / Summaries

同一事件不得同時以完整形式出現在：

- recent events
- facts
- recent turn summaries
- memory details

若多處需要，只保留一份 canonical compact record，其他處使用 short reference。

### 6.3 Setting / Rule

當前使用 item/technique 的 requirements、costs、limitations 必須保留。

但 public description、origin、provenance、relation path 不得在多個 section 重複全文。

### 6.4 Empty / Default

最終序列化前移除非必要：

- null
- empty array
- empty object
- false/default debug flags
- 空字串
- 未使用 optional fields

前提是 protocol parser 能正確採用明確預設值，且不影響 validator。

## 7. `otherMetadata` 驗收標準

修補後：

- final packet 不應存在語意不明的巨大 `otherMetadata`。
- 若保留此 section，必須有 schema、內容 allowlist，且 Turn 8 中 <= 1024 bytes。
- 更推薦 final packet 中完全不輸出 `otherMetadata`。
- audit sidecar 可以保留完整 diagnostics，不計入 AI context。

## 8. 測試

保留全部 128 tests，至少新增：

1. final ContextPacket 僅含 allowlist 欄位。
2. 未知 top-level field 會在 build/test 階段失敗，而非歸入 otherMetadata。
3. diagnostics 不出現在 final packet。
4. audit sidecar 仍含完整 forensics。
5. legacy alias 與 canonical field 不會同時輸出。
6. active character profile/runtime 不重複全文。
7. fact/event/summary 重複內容正規化為一份 + references。
8. empty/default optional fields 不輸出。
9. minimal contextBudget metadata 小於合理固定上限。
10. Turn 8 相同輸入 final bytes <=24576。
11. Turn 8 `otherMetadata <=1024` 或不存在。
12. mandatory safety fields 全部存在。
13. forbidden secret 不洩漏。
14. current item/technique requirements/costs/limitations 保留。
15. contextHash 以 final normalized packet 計算。
16. actualBytes 等於實際 UTF-8 檔案 bytes。
17. normalization deterministic。
18. no dangling references。
19. explain/audit read-only。
20. failed preview 不改 stateVersion、nextTurnNumber 或 pending context。
21. Turn 1 至 Turn 7 evidence 不被修改。
22. 中文 UTF-8 round-trip 正確。

## 9. 複本 E2E

複製：

`data/pilot1-rerun.db`
→
`data/pilot1-metadata-normalization-e2e.db`

使用完全相同 Turn 8 玩家輸入：

```text
我摸了摸愛醬的頸側，讓牠知道我記得牠剛才忍住低吼守著我。

「趙無咎，既然安靜時它沒有追過來，我們能不能不碰它，慢慢退到驛亭門外？」

我仍留在原地，等他回答。
```

在複本執行：

1. explain-context-budget
2. prepare-turn
3. audit-context
4. 不生成 result
5. abort-pending-turn

驗收：

- explain PASS
- prepare PASS
- audit PASS
- context <=24576
- `otherMetadata <=1024` 或不存在
- contextBudget metadata 為精簡版本
- hash valid
- safety fields 完整
- pending 可 abort
- stateVersion 仍 7
- nextTurnNumber 仍 8
- 沒有新增正式 turn log / state delta

## 10. 正式 DB 只讀驗證

對：

`data/pilot1-rerun.db`

只執行：

- explain-context-budget
- pilot-preflight

回報：

- predicted final bytes
- section breakdown
- otherMetadata bytes
- contextBudgetMetadata bytes
- omitted counts
- safety fields
- stateVersion=7
- nextTurnNumber=8
- pendingContextCount=0

不得在正式 DB 執行 prepare 或 resolve。

## 11. 文件

更新：

- README.md
- AGENTS.md
- docs/ARCHITECTURE.md
- docs/TURN_PROTOCOL.md
- docs/VALIDATION_RULES.md
- docs/CONTEXT_BUDGET_FORENSICS.md
- docs/PILOT1_FINDINGS.md

新增：

- `docs/CONTEXT_PACKET_NORMALIZATION.md`

說明：

- final packet allowlist
- diagnostics sidecar
- legacy alias removal
- canonical compact records
- reference-based deduplication
- empty/default omission
- otherMetadata policy
- hash / byte accounting order

## 12. 最終回報

執行：

```bat
gradlew.bat clean test --no-daemon
git diff --check
```

回報：

1. VERDICT
2. branch / status
3. tests 結果
4. Turn 8 原始 otherMetadata path breakdown
5. 最大重複欄位
6. 移出正式 packet 的 diagnostics
7. 移除的 legacy aliases
8. normalization / compact 策略
9. before / after bytes
10. otherMetadata before / after
11. contextBudgetMetadata before / after
12. safety field 驗證
13. hash / actual bytes
14. 複本 E2E
15. 正式 DB read-only preflight
16. stateVersion / nextTurnNumber / pending count
17. 明確確認未正式執行 Turn 8
18. 尚未解決問題
