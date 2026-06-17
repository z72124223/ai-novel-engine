# Turn Protocol

每回合透過兩個 JSON 檔交換：

- `context_packet.json`
- `ai_turn_result.json`

## Context Packet

`ContextPacket` 由 `compile-context` 產生，包含：

- `project`
- `turnId`
- `turnNumber`
- `playerInput`
- `activeScene`
- `sceneContract`
- `characters`
- `characterMemories`
- `characterStates`
- `worldRules`
- `facts`
- `revealedSecrets`
- `items`
- `techniques`
- `recentEvents`
- `openTasks`

未揭露 secrets 的 content 不放入 context packet；validator 會從 SQLite 讀取它們以檢查 forbidden reveal。

## AI Turn Result

`AiTurnResult` 由人工或 Codex 填寫：

- `projectId`
- `turnId`
- `narrative`
- `stateDelta`
- `notes`

`stateDelta.operations` 支援：

- `update_character_state`
- `add_fact`
- `reveal_secret`
- `add_event`
- `upsert_task`
- `noop`

`apply-result` 永遠先驗證 result，再套用 delta。

## MVP-2 Session Protocol

`ContextPacket` 新增：

- `contextId`
- `sessionId`
- `baseStateVersion`
- `contextHash`
- `generatedAt`
- `recentTurnSummaries`
- `sceneProgress`
- `directorPressure`
- `outputRequirements`

`AiTurnResult` 新增：

- `resultId`
- `contextId`
- `sessionId`
- `turnNumber`
- `baseStateVersion`
- `contextHash`
- `turnSummary`
- `characterActions`
- `stateDeltas`
- `revealedFacts`
- `memoryEffects`
- `sceneProgressUpdates`
- `newTasks`
- `validationNotes`

Hash rule: the engine serializes `ContextPacket` with `contextHash = ""` using the repository JSON serializer, then computes SHA-256 over UTF-8 bytes. This uses Kotlin data-class property order and does not depend on map insertion order for protocol identity fields.

`prepare-turn` stores pending context metadata in `turn_logs` with `validation_status = pending`. `resolve-turn` loads that pending context and refuses stale state versions, turn mismatches, hash mismatches, duplicate result IDs, unsupported protocol versions, and forbidden reveals.
