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

## MVP-3 Memory And Knowledge Protocol

`ContextPacket` 新增：

- `retrievedMemories`：本回合可供角色使用的記憶摘要、owner、score、reasons 與 allowed uses。
- `knowledgeClaims`：角色可見的命題認知，標示 epistemic state 與 confidence。
- `contextBudget`：max bytes、actual bytes、memory/knowledge included/omitted counts、truncation 狀態與原因。

`AiTurnResult` 新增：

- `memoryCandidates`：AI 只能提出候選記憶，不能直接寫入 SQLite。
- `knowledgeUpdates`：AI 只能提出認知狀態更新，engine 會檢查 evidence 與 conflict transition。

`prepare-turn` 仍是 read-only。`resolve-turn` 成功後才正式寫入 memory、knowledge、recall logs 與 recall count。

## MVP-4 Scene Thread Protocol

`ContextPacket` adds:

- `sceneLifecycle`
- `sceneRequirements`
- `activeStoryThreads`
- `availableTransitions`
- `pendingWorldEvents`
- `worldClock`
- `continuityConstraints`

`AiTurnResult` adds:

- `sceneRequirementUpdates`
- `threadProgressUpdates`
- `transitionProposal`
- `worldEventAcknowledgements`
- `timeAdvanceProposal`

AI/Codex must only read the current context JSON when drafting a result. It must not write SQLite directly. `resolve-turn` reloads the pending context and validates `contextId`, `sessionId`, `turnNumber`, `baseStateVersion`, `contextHash`, and `resultId` before accepting these fields.

## MVP-5 Setting Context Protocol

`ContextPacket` adds:

- `relevantSettings`: bounded excerpts selected from imported canon revisions and dependency paths.
- `settingContextBudget`: byte budget and truncation accounting for the setting section.
- `canonRevisionSummary`: imported packs and relevant revision IDs used by this context.

The setting section is read-only context. It must not contain the full setting pack, the full dependency graph, or author-only secret content. AI/Codex still writes only `AiTurnResult` JSON for turns; setting pack JSON is handled by the separate setting-pack import commands.

## Pilot-1 Stabilization Protocol

`prepare-turn` now finalizes the complete `ContextPacket` against the hard UTF-8 byte budget:

- `contextBudget.maxBytes` remains `24576`.
- `contextBudget.actualBytes` is the exact number of UTF-8 bytes written for the final JSON payload.
- `contextHash` is computed after byte-budget reduction and accounting converge.
- If mandatory context cannot fit, `prepare-turn` fails with `CONTEXT_BUDGET_UNSATISFIABLE` and does not create a pending turn.

Before drafting an `AiTurnResult`, run:

```powershell
./gradlew.bat run --args="audit-context --file out/context.json --out out/context_audit.json"
```

`audit-context` verifies exact bytes, declared bytes, hash, and mandatory fields. A failed audit is a hard stop.

Pilot runs should also use:

```powershell
./gradlew.bat run --args="pilot-preflight --db data/pilot1-rerun.db --project <projectId> --session <sessionId> --out out/pilot1/preflight.json"
./gradlew.bat run --args="export-session-evidence --db data/pilot1-rerun.db --session <sessionId> --out out/pilot1/evidence.json"
```

Both commands are read-only. `pilot-preflight` performs a dry-run context build without saving a pending turn.
