# MVP-4：場景生命週期、劇情線與世界事件

## 0. 目標

MVP-3 已具備：

- 多回合 session 與版本控制
- 記憶檢索、知識邊界、belief conflict
- 情緒因果與 context budget
- validator-gated transaction

本階段要解決：

1. 故事不能永遠停留在同一場景。
2. 場景必須能開始、推進、完成、失敗、離開及再次造訪。
3. 主線、角色線、謎團線與勢力線必須分開追蹤。
4. 角色不可因作者的劇情目標而變成工具人。
5. 玩家不接受轉場時，系統不得任意把玩家搬走。
6. 世界事件與時間壓力必須能在角色不配合時繼續運作。
7. 已關閉場景只保留摘要與必要後果，不把全文歷史塞回 context。

## 1. 禁止事項

- 不做 Android、Web UI、HTTP server 或任何 port。
- 不接外部 AI API。
- 不加入 Rust、Docker、embedding、向量資料庫或多代理框架。
- 不修改 001、002、003 migration。
- AI/Codex 不得直接寫 SQLite。
- 不得繞過 Validator 或 transaction。
- 不得把 story thread 的作者目的當成角色內在目標。
- 不得讓 AI 任意建立或跳轉至不存在的場景。
- 不得無視地點連接、時間、移動條件或同行角色。
- 不得因 scene requirement 已完成就強迫玩家離場。
- 不得將完整 closed-scene narrative 放入後續 context。
- 不得為 E2E 玩家句子硬編碼行為或結果。

## 2. Git 與 migration

1. 先確認 MVP-3 已提交並標記 tag `mvp-3`。
2. 建立分支：
   `feat/scene-thread-world-event-runtime`
3. 新增：
   `app/src/main/resources/migrations/004_scene_thread_runtime.sql`
4. 所有 schema 變更 additive，保留現有資料。
5. SQLite foreign keys 維持啟用。

## 3. 核心概念

### 3.1 Scene Definition

場景的固定定義，例如地點、戲劇問題、可進入條件、相關 threads。

### 3.2 Scene Instance

某個 session 中的一次實際造訪。同一 scene 可重訪，但每次有不同 instance、開始與結束摘要。

狀態至少包含：

- `PENDING`
- `ACTIVE`
- `READY_TO_CLOSE`
- `CLOSED`
- `FAILED`
- `ABORTED`

### 3.3 Scene Requirement

場景希望或必須產生的狀態變化，但不是角色命令。

類型至少包含：

- `REQUIRED`
- `OPTIONAL`
- `FAILURE_GUARD`
- `EXIT_GATE`

Requirement 必須以已接受的 event、fact、state delta 或 world event 作為證據。

### 3.4 Story Thread

獨立追蹤：

- `MAIN`
- `CHARACTER`
- `MYSTERY`
- `FACTION`
- `RELATIONSHIP`
- `WORLD`

狀態至少包含：

- `OPEN`
- `DORMANT`
- `BLOCKED`
- `RESOLVED`
- `FAILED`
- `ABANDONED`

Thread 是導演層資料。角色只能看到自己合法知道的 facts、beliefs、memories，不得看到 thread 的作者目標。

### 3.5 Scene Transition

AI 只能提出 transition proposal。引擎驗證：

- session 與 scene instance
- player action 是否支持移動
- 地點是否可達
- entry prerequisites
- exit gates
- required participants
- travel time
- world rules
- forced-transition 依據

引擎決定是否接受並原子套用。

### 3.6 World Event

時間或條件觸發的世界事件，例如追兵抵達、雨勢加劇、門外火把接近。

世界事件可施加壓力，但不能直接命令角色坦白。

## 4. Schema

新增或擴充下列結構。

### 4.1 story_threads

至少包含：

- id
- project_id
- thread_type
- title
- director_goal
- public_summary
- priority
- visibility
- parent_thread_id
- created_at
- updated_at

`director_goal` 不得放入角色知識區段。

### 4.2 session_thread_states

至少包含：

- session_id
- thread_id
- status
- progress
- urgency
- blocked_reason
- last_advanced_turn
- evidence_json
- created_at
- updated_at

唯一鍵：`session_id + thread_id`。

### 4.3 scene_requirements

至少包含：

- id
- scene_id
- requirement_type
- description
- completion_condition_json
- failure_condition_json
- priority
- visibility
- created_at
- updated_at

### 4.4 session_scene_requirement_states

至少包含：

- session_id
- scene_instance_id
- requirement_id
- status
- progress
- evidence_json
- completed_turn_number
- created_at
- updated_at

### 4.5 scene_instances

至少包含：

- id
- session_id
- scene_id
- visit_number
- status
- started_turn_number
- ended_turn_number
- entry_reason
- exit_reason
- summary
- created_at
- updated_at

同一 session 同時只能有一個 ACTIVE scene instance。

### 4.6 location_connections

至少包含：

- id
- project_id
- from_location_id
- to_location_id
- connection_type
- bidirectional
- travel_minutes
- prerequisites_json
- restrictions_json
- visibility
- created_at
- updated_at

### 4.7 scene_transitions

場景定義間允許的轉場：

- id
- project_id
- from_scene_id
- to_scene_id
- transition_type
- prerequisites_json
- costs_json
- required_player_intent
- forced_transition_allowed
- visibility
- created_at
- updated_at

### 4.8 pending_scene_transitions

至少包含：

- id
- session_id
- result_id
- from_scene_instance_id
- to_scene_id
- proposal_type
- reason
- evidence_json
- status
- rejection_reason
- created_turn_number
- resolved_turn_number
- created_at
- updated_at

### 4.9 scheduled_world_events

至少包含：

- id
- project_id
- session_id
- event_type
- due_turn_number
- due_world_time
- trigger_conditions_json
- payload_json
- visibility
- status
- triggered_turn_number
- consumed_turn_number
- created_at
- updated_at

### 4.10 story_sessions 擴充

新增或保存：

- current_scene_instance_id
- world_time
- elapsed_minutes

禁止破壞現有 session。

## 5. 新增服務

至少實作：

- `SceneLifecycleService`
- `SceneRequirementEvaluator`
- `StoryThreadManager`
- `TransitionPlanner`
- `TransitionValidator`
- `WorldClockService`
- `ScheduledWorldEventService`
- `NarrativeContinuityValidator`

### SceneLifecycleService

負責：

- 建立第一個 scene instance
- ACTIVE → READY_TO_CLOSE
- READY_TO_CLOSE → CLOSED
- FAILED / ABORTED
- 重訪時 visit_number 增加
- close 時產生短 scene summary
- 只能在成功 resolve transaction 內改變正式狀態

### SceneRequirementEvaluator

- 根據本回合已接受的 events/facts/deltas 評估 requirement。
- AI 只能提出 update；引擎驗證證據。
- 不允許只靠文字宣稱 requirement 完成。
- REQUIRED 未完成時，普通 transition 可被拒絕。
- OPTIONAL 未完成不一定阻止離場。
- FAILURE_GUARD 被觸發時可將 scene 標記 FAILED。

### StoryThreadManager

- 只依已接受證據更新 thread progress。
- progress 限定 0..100。
- 一回合單 thread 變化應有合理上限。
- thread status 轉換需驗證。
- thread director goal 不進 character knowledge。
- dormant/irrelevant threads 不進當前 context。

### TransitionPlanner

根據：

- scene lifecycle
- player intent
- 可達地點
- scene transitions
- thread state
- world events
- required participants
- entry prerequisites

產生 `availableTransitions`，但不得直接套用。

### TransitionValidator

至少驗證：

- from scene instance 為當前 ACTIVE/READY_TO_CLOSE
- to scene 存在
- location connection 可達
- player action 支持該 transition，或存在合法 forced reason
- entry prerequisites 成立
- exit gates 成立
- 不使用角色未知資訊
- required participants 狀態合理
- result/context/stateVersion 仍有效
- 不得瞬移或跳過 travel cost
- 不得因作者 thread goal 強制轉場

### WorldClockService

- 每個成功回合可推進可配置分鐘數。
- time advance 必須有 reason。
- 非法負值、過大值或無理由跳時拒絕。
- transition travel time 在同一 transaction 加入。
- rejected turn 不改 world time。

### ScheduledWorldEventService

- due 前不出現。
- due 後在 context 中成為 pending world event。
- AI 可以敘事化，但不能改寫 event canonical payload。
- 成功 resolve 後只 consume 一次。
- duplicate result 不重複 consume。
- event 可增加 director pressure，不直接控制角色。

### NarrativeContinuityValidator

驗證：

- 出場角色目前位置
- 場景 location
- 時間連續性
- 已離場角色不應無理由出現
- 道具 owner/location
- 移動與 travel time
- closed scene 不可繼續被當成 current scene
- narrative 不得宣稱未被接受的 transition 已完成

## 6. ContextPacket 擴充

新增：

- `sceneLifecycle`
- `sceneRequirements`
- `activeStoryThreads`
- `availableTransitions`
- `pendingWorldEvents`
- `worldClock`
- `continuityConstraints`

### sceneLifecycle

至少包含：

- sceneInstanceId
- sceneId
- visitNumber
- status
- startedTurnNumber
- readyToClose
- closureReasons

### sceneRequirements

每項至少包含：

- requirementId
- type
- description
- status
- progress
- remainingCondition
- directorOnly

角色不得把 `directorOnly` requirement 當成自身目標。

### activeStoryThreads

每項至少包含：

- threadId
- type
- publicSummary
- status
- progress
- urgency
- directorGuidance

`directorGoal` 不可出現在 character-facing knowledge。

最多載入與當前 scene 直接相關的 3 條 thread。

### availableTransitions

每項至少包含：

- transitionId
- toSceneId
- targetLocation
- playerIntentRequired
- prerequisitesSatisfied
- unmetPrerequisites
- travelMinutes
- forcedAllowed
- visibleReason

### pendingWorldEvents

每項至少包含：

- eventId
- type
- publicEffect
- dueReason
- allowedNarrativeUses
- forbiddenCharacterUses

### worldClock

至少包含：

- worldTime
- elapsedMinutes
- turnNumber

### Context Budget

沿用 MVP-3 預算。新增資料不能裁掉：

- forbidden reveals
- scene contract
- world hard rules
- continuity constraints
- pending forced world event 的安全摘要

Closed scene 只能以 `scene summary` 或必要 consequence 進 context，不得帶完整 narrative。

## 7. AiTurnResult 擴充

新增：

- `sceneRequirementUpdates`
- `threadProgressUpdates`
- `transitionProposal`
- `worldEventAcknowledgements`
- `timeAdvanceProposal`

### sceneRequirementUpdates

每項：

- requirementId
- requestedStatus
- progressDelta
- evidenceEventIds
- evidenceFactIds
- evidenceStateDeltaIds
- reason

### threadProgressUpdates

每項：

- threadId
- progressDelta
- requestedStatus
- evidenceEventIds
- evidenceFactIds
- reason

### transitionProposal

可為 null；若存在至少包含：

- fromSceneInstanceId
- transitionId
- toSceneId
- proposalType
- playerIntentEvidence
- worldEventEvidence
- participatingCharacterIds
- reason

`proposalType` 至少：

- `PLAYER_INITIATED`
- `CHARACTER_INITIATED`
- `WORLD_FORCED`

CHARACTER_INITIATED 必須符合角色自身目標。
WORLD_FORCED 必須引用合法 world event 或世界規則。

### worldEventAcknowledgements

每項：

- eventId
- narrativeUse
- perceivedByCharacterIds
- resultingEventIds
- reason

### timeAdvanceProposal

至少：

- minutes
- sourceType
- sourceId
- reason

## 8. 場景關閉與玩家自主性

1. Requirements 完成後，scene 只能進入 READY_TO_CLOSE。
2. READY_TO_CLOSE 不等於自動離場。
3. 玩家未選擇移動，且無合法 forced event 時，仍可留在場景。
4. 若玩家明確拒絕某 transition，該 transition 不得同回合套用。
5. Forced transition 只允許：
   - 世界規則不可避免
   - 已觸發且可驗證的 world event
   - 生存性緊急事件
6. Forced transition 必須留下：
   - force reason
   - evidence
   - affected actors
   - consequences
7. 不能以「劇情需要」作為 forced reason。

## 9. Thread 與角色自主性

- Thread 進度可以由角色阻礙、誤判、逃避或失敗推進。
- 角色不需要配合 thread 目標。
- character action 的 `intent` 不得直接複製 thread director goal。
- 若 characterFitScore 合格但 thread progress 為負或零，仍可接受。
- Story progress 不得凌駕 character knowledge boundary。
- Thread update 必須由事件證據支持，不得僅因 narrative 看似有戲劇性就更新。

## 10. CLI

新增：

### show-scene

```bat
gradlew.bat run --args="show-scene --session SESSION_ID"
```

顯示：

- current scene instance
- status
- requirements
- available transitions
- active threads
- world time
- pending world events

### show-threads

```bat
gradlew.bat run --args="show-threads --session SESSION_ID"
```

顯示 thread 狀態、進度、證據摘要；不得顯示角色不應知道的 director goal 給 character-facing output。CLI 管理模式可明確標記 `DIRECTOR_ONLY`。

### preview-transitions

```bat
gradlew.bat run --args="preview-transitions --session SESSION_ID --input \"玩家輸入\""
```

只讀，顯示候選 transition、滿足/未滿足條件、travel time、是否可能 forced。

### show-world-events

```bat
gradlew.bat run --args="show-world-events --session SESSION_ID"
```

顯示 pending/triggered/consumed events。

### explain-scene-state

顯示 scene 為何 ACTIVE、READY_TO_CLOSE、CLOSED 或 FAILED，以及 requirement 證據。

## 11. 測試

保留 MVP-1 至 MVP-3 全部測試，並至少新增：

1. 同一 session 只能有一個 ACTIVE scene instance。
2. scene requirements 有有效 evidence 才能完成。
3. OPTIONAL 未完成不阻止合法離場。
4. REQUIRED 未完成時普通 transition 被拒絕。
5. READY_TO_CLOSE 不會自動轉場。
6. 玩家明確拒絕 transition 時不套用。
7. unreachable location transition 被拒絕。
8. entry prerequisite 不成立被拒絕。
9. WORLD_FORCED 沒有 event evidence 被拒絕。
10. 合法 world event 可觸發 forced transition。
11. transition 成功後舊 scene CLOSED、新 scene ACTIVE。
12. transition 與 travel time 在同一 transaction。
13. transition 任一寫入失敗時全部 rollback。
14. stale/duplicate result 不重複轉場或 consume event。
15. due world event 不提前出現。
16. world event 只 consume 一次。
17. thread progress 無 evidence 被拒絕。
18. thread director goal 不進 character knowledge。
19. 角色可阻礙 thread 但 action 仍被接受。
20. closed scene 的完整 narrative 不進後續 context。
21. scene summary 長度受限且能供下一場 continuity 使用。
22. 無理由跨地點或角色瞬移被 continuity validator 拒絕。
23. 道具 location/owner 與 narrative 衝突被拒絕。
24. preview CLI 不修改 DB。
25. context budget 在跨場景後仍受控。
26. 訪問同一 scene 第二次會建立 visit_number=2，而非覆寫第一次 instance。
27. rejected result 不更新 scene/thread/time/world event。
28. transition 後舊 context/result 被 stateVersion/context 驗證拒絕。

## 12. E2E 驗收

使用全新 DB：

`data\scene-thread-e2e.db`

Demo 至少包含：

- Scene A：舊祠堂後殿
- Scene B：外廊
- Scene C：祠堂外石階
- 可達 location connections
- 一條 MAIN thread
- 一條 CHARACTER thread
- 一條 MYSTERY thread
- 至少一個 scheduled world event：追兵接近
- Scene A 至少一個 REQUIRED 與一個 OPTIONAL requirement

執行至少 7 回合，自然覆蓋：

1. 在 Scene A 取得足夠 evidence 完成 REQUIRED requirement。
2. OPTIONAL requirement 尚未完成，但 scene 可 READY_TO_CLOSE。
3. 玩家有一回合明確選擇留下，確認不自動轉場。
4. scheduled world event 到期並提高壓力。
5. 玩家或角色依自身目標提出合法 transition。
6. 成功移至 Scene B，world time 加入 travel cost。
7. Scene A CLOSED 且留下摘要。
8. Scene B context 只帶 Scene A 摘要與必要 consequence。
9. 至少一條 thread 合理前進。
10. 至少一條 thread 因角色阻礙或玩家選擇未前進，仍不強迫角色。
11. 再從 Scene B 移至 Scene C，或合法重訪 Scene A。
12. forbidden secret 始終未提前揭露。
13. 角色 location、item location、world time 連續。
14. context bytes 保持在 MVP-3 budget 內。
15. recentTurnSummaries 與 memory retrieval 規則仍有效。

不得硬編碼 E2E 玩家輸入或結果。

另執行至少 20 回合的 deterministic soak test：

- 可使用測試 fixture result，不需要外部 AI。
- 至少經歷 3 個 scene instances。
- context 不超過 maxContextBytes。
- closed scene narrative 不累積。
- world event 不重複 consume。
- stateVersion 每成功回合只增 1。
- 不發生多個 ACTIVE scene instance。
- memory/recent summaries/thread context 維持上限。

## 13. 文件

更新：

- README.md
- AGENTS.md
- docs/ARCHITECTURE.md
- docs/DATA_MODEL.md
- docs/TURN_PROTOCOL.md
- docs/VALIDATION_RULES.md
- docs/ROADMAP.md
- docs/MEMORY_AND_KNOWLEDGE.md

新增：

- `docs/SCENE_THREAD_RUNTIME.md`

必須說明：

- scene definition 與 scene instance
- scene lifecycle
- requirements 與 evidence
- READY_TO_CLOSE 與玩家自主性
- transition proposal / validation / apply
- location reachability
- story thread 與 character goal 的隔離
- world clock
- scheduled world events
- scene summary 與 context 壓縮
- re-visit semantics
- transaction / rollback
- Codex 如何生成 transition proposal 而不替玩家做決定

AGENTS.md 僅加入長期規則並引用本規格，不複製全文。

## 14. 驗收命令與回報

執行：

```bat
gradlew.bat clean test --no-daemon
```

最終回報：

1. VERDICT：PASS / FAIL
2. branch 與 commit status
3. migration 摘要
4. 新增與修改檔案
5. 全部測試數與結果
6. 七回合 E2E 逐回合摘要
7. scene instance IDs、status 與 visit numbers
8. requirement 狀態與 evidence
9. transition proposals、接受/拒絕理由
10. world time 與 travel time
11. world events 觸發/consume 狀態
12. thread 狀態、progress 與 evidence
13. 玩家拒絕轉場時的結果
14. 角色自主性驗證
15. closed scene summary 與後續 context 內容
16. context bytes 與 budget
17. 20 回合 soak test 結果
18. rollback、duplicate、stale 與 continuity 測試
19. forbidden secrets 狀態
20. 尚未解決問題

若失敗，先修到全部測試與 E2E 通過，不擴充規格外功能。
