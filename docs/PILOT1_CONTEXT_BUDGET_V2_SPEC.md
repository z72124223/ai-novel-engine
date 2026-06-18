# Pilot-1 Context Budget V2：失敗鑑識與針對性修補

## 0. 問題

Pilot-1 正式 session：

- DB：`data/pilot1-rerun.db`
- SESSION_ID：`session-75e1b190-542c-49c0-9a5a-6b242bed8e97`
- stateVersion：3
- nextTurnNumber：4

Turn 4 在 `prepare-turn` 階段失敗：

- final ContextPacket：24957 bytes
- max：24576 bytes
- 超出：381 bytes
- 沒有建立 pending context
- 沒有 result / resolve / state change

本階段只做：

1. 失敗 payload 鑑識。
2. 找出不可裁切與重複內容。
3. 針對性縮減。
4. 證明同一個 Turn 4 輸入可以成功 prepare。
5. 不執行正式 Turn 4 resolve。

不得提高 context 上限。

## 1. 禁止事項

- 不新增 Android、Web UI、HTTP server、port、外部 API。
- 不加入 Rust、Docker、embedding、向量資料庫或多代理框架。
- 不修改 001 至 005 migration。
- 預設不得新增 migration。
- 不得修改正式 Pilot DB 的故事狀態。
- 不得自行執行或 resolve Turn 4。
- 不得刪除正式記憶、facts、secrets、settings 或 turn logs。
- 不得把 `maxContextBytes` 提高到 24576 以上。
- 不得裁掉 forbidden reveals、hard world rules、continuity constraints、當前角色狀態、當前使用 item/technique 的 requirements/costs/limitations。
- 不得針對特定中文句子、entity ID 或 session 寫硬編碼特例。
- 不得用任意截斷字串產生不完整語意或無效 JSON。

## 2. Git 與資料安全

目前應在 Pilot 工作分支上。

1. 執行：
   - `git status`
   - `gradlew.bat clean test --no-daemon`
2. 確認 106 tests 全數通過。
3. 建立分支：
   `fix/pilot1-context-budget-v2`
4. 保留正式：
   - `data/pilot1-rerun.db`
   - 原 SESSION_ID
5. 所有重現與測試使用 DB 複本：
   `data/pilot1-budget-v2-e2e.db`
6. 不得用測試流程寫入正式 Pilot DB。

## 3. 新增失敗鑑識報告

當 `prepare-turn` 因 `CONTEXT_BUDGET_UNSATISFIABLE` 失敗時，仍需產生一份只讀診斷 JSON，但不得建立可 resolve 的 pending context。

建議檔名：

`out/pilot1/rerun/turns/turn_004_budget_failure.json`

至少包含：

- sessionId
- turnNumber
- baseStateVersion
- maxContextBytes
- candidateBytesBeforeTrimming
- candidateBytesAfterTrimming
- bytesOverLimit
- mandatoryBytes
- optionalBytesRemaining
- perSectionBytesBefore
- perSectionBytesAfter
- mandatorySections
- optionalSections
- removedItems
- retainedItems
- topLargestJsonPaths
- duplicateContentGroups
- danglingReferenceCheck
- safetyFieldPresence
- unsatisfiedReason

### Section 至少分開統計

- protocolIdentity
- playerInput
- sceneLifecycle
- sceneContract
- activeCharacters
- characterKnowledge
- retrievedMemories
- relevantSettings
- storyThreads
- recentTurnSummaries
- worldRules
- worldEvents
- continuityConstraints
- forbiddenReveals
- outputRequirements
- contextBudgetMetadata
- canonRevisionSummary
- otherMetadata

### topLargestJsonPaths

列出至少前 25 個最占 bytes 的 JSON path、bytes、是否 mandatory、是否可去重。

### duplicateContentGroups

偵測同一內容或高度相同 canonical fragment 是否重複出現在：

- active character profile 與 relevant settings
- world rules 與 setting constraints
- scene contract 與 scene requirements
- recent summaries 與 facts/events
- item/technique description 與 constraints
- public summary 與 character-accessible details

不得只用人工猜測，必須用穩定 hash 或 canonical fragment 比較。

## 4. 修補策略

先以鑑識結果決定，不得預先假設單一原因。

允許的修補方式：

### 4.1 去除重複資料

若同一語意已在 mandatory section 完整存在，其他 section 只保留：

- entityId
- relation
- short reference
- inclusion reason

不要重複整段 description、constraints、costs。

### 4.2 Mandatory Compact View

不可裁切不等於必須保存冗長原文。

為 mandatory entities 建立 compact view，至少保留：

- stable ID
- current state
- hard constraints
- requirements
- costs / limitations
- knowledge visibility
- action legality data

可移除：

- 重複 prose
- 重複 public description
- provenance 詳情
- 非本回合必要 metadata
- 已由 ID 可查回的長欄位

Compact view 必須有單元測試證明語意安全資料仍在。

### 4.3 Dynamic Budget Allocation

`maxSettingBytes=8192` 是上限，不是保留配額。

實際 setting 額度：

`min(maxSettingBytes, globalRemainingAfterMandatoryAndHigherPrioritySections)`

recent summaries、memories、knowledge、threads、settings 必須共享剩餘全域預算，而不是各自先吃滿再做最終失敗。

### 4.4 Incremental Fitting

每次移除一項後重新 canonical serialize 並計算 UTF-8 bytes。

排序必須 deterministic。

建議 trimming 順序：

1. duplicate fragments
2. verbose metadata / provenance
3. low-relevance unrelated settings
4. secondary threads
5. low-confidence knowledge
6. low-score memories
7. oldest recent summary
8. optional public prose

安全資料不可進 trimming queue。

### 4.5 Reference Integrity

裁切後：

- usedMemoryIds 必須仍可驗證。
- active item/technique 限制必須仍存在。
- relationPaths 不得引用完全不存在且無 short reference 的 entity。
- 不得產生 dangling IDs。
- contextHash 以最終 payload 計算。
- actualBytes 必須等於真正寫出檔案的 UTF-8 bytes。

## 5. 新增只讀 CLI

新增或擴充：

### explain-context-budget

```bat
gradlew.bat run --args="explain-context-budget --db data\pilot1-rerun.db --session SESSION_ID --input-file out\turn4_input.txt --out out\turn4_budget_explain.json"
```

要求：

- read-only
- 不建立 pending context
- 不修改 session / stateVersion / turn logs
- 產生完整鑑識報告
- 可顯示 trimming simulation
- 明確顯示是否可在上限內 fit

若不新增 CLI，可讓既有 `pilot-preflight` 或 preview mode 提供等價能力，但正式 `prepare-turn` 失敗時仍必須輸出 failure report。

## 6. 回歸測試

保留全部 106 tests，至少新增：

1. 24957-byte 類型 candidate 可被裁到 <=24576。
2. 不得提高 maxContextBytes。
3. mandatory safety fields 全部保留。
4. active item/technique requirements/costs/limitations 保留。
5. duplicate content 被壓成 short reference。
6. dynamic setting budget 不會預占 8192。
7. final actualBytes 等於檔案 bytes。
8. contextHash 以最終裁切 payload 生成。
9. trimming deterministic。
10. 沒有 dangling entity/memory/relation IDs。
11. prepare failure report read-only。
12. explain-context-budget read-only。
13. mandatory payload 真正無法 fit 時仍正確 fail，且產生診斷。
14. failed prepare 不建立 pending context。
15. failed prepare 不改 stateVersion / nextTurnNumber。
16. UTF-8 中文 player input 計算正確。
17. 既有 Pilot Turn 1-3 evidence 不被修改。
18. forbidden secret 不因 compact view 洩漏。
19. low-relevance setting 先被裁切，高優先記憶仍可保留。
20. current character runtime state 不被 compact 掉必要欄位。

## 7. E2E 重現

複製正式 DB：

`data/pilot1-rerun.db`
→
`data/pilot1-budget-v2-e2e.db`

在複本上使用完全相同 Turn 4 玩家輸入：

```text
我仍留在愛醬身後，一手輕按著牠的肩背，沒有靠近那片影子。

我對趙無咎說：

「你若也想知道它在追什麼，就站到驛亭另一側說一句話。我和愛醬都不出聲。」

我盯著影子的邊緣，等趙無咎自己決定是否配合。
```

流程：

1. `explain-context-budget`
2. `prepare-turn`
3. `audit-context`
4. 不生成 result
5. `abort-pending-turn`

驗收：

- explain report 產生
- prepare 成功
- audit PASS
- final bytes <=24576
- setting bytes <=8192
- mandatory safety fields 完整
- hash valid
- pending 可正常 abort
- DB 複本 formal stateVersion 仍為 3
- 沒有新增 turn log / state delta

另用正式 DB 只執行：

- explain-context-budget
- pilot-preflight

兩者都必須 read-only。

不得在正式 DB 上執行 prepare，直到全部測試與複本 E2E 通過。

## 8. 正式 Pilot 恢復條件

全部通過後，對正式 DB：

- 執行 `pilot-preflight`
- 回報 Turn 4 相同輸入的預估 final bytes
- 回報 mandatoryBytes
- 回報各 section bytes
- 回報 trimmingApplied 與 omitted counts
- 明確確認 stateVersion 仍為 3、nextTurnNumber 仍為 4
- 明確確認沒有 pending context

然後停止，等待使用者再次下達 Turn 4。

不得自行完成 Turn 4。

## 9. 文件

更新：

- README.md
- AGENTS.md
- docs/ARCHITECTURE.md
- docs/TURN_PROTOCOL.md
- docs/VALIDATION_RULES.md
- docs/PILOT1_STABILIZATION.md
- docs/PILOT1_FINDINGS.md

新增：

- `docs/CONTEXT_BUDGET_FORENSICS.md`

說明：

- section accounting
- mandatory vs optional bytes
- duplicate fragment detection
- compact mandatory view
- dynamic allocation
- deterministic trimming
- failure report
- explain-context-budget
- 為何不能提高上限
- 為何失敗 prepare 不得留下 pending context

## 10. 驗收回報

執行：

```bat
gradlew.bat clean test --no-daemon
git diff --check
```

回報：

1. VERDICT：PASS / FAIL
2. branch / status
3. tests 總數
4. 原失敗 candidate 的 section breakdown
5. mandatoryBytes / optionalBytes
6. largest JSON paths
7. duplicate groups
8. 實際採用的修補
9. 裁切前後 bytes
10. omitted items
11. safety fields 驗證
12. hash / actualBytes 驗證
13. DB 複本 E2E
14. 正式 DB read-only preflight
15. 正式 DB stateVersion / nextTurnNumber
16. 明確確認未執行正式 Turn 4
17. 尚未解決問題
