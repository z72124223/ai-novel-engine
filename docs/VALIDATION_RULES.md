# Validation Rules

`AiTurnResultValidator` 在套用狀態前執行。

## Required Checks

- `result.projectId` 必須等於 context project id。
- `result.turnId` 必須等於 context turn id。
- `narrative` 不可空白。
- `update_character_state` 的 target character state 必須存在。
- `add_fact` 必須提供 `payload.content`。
- `add_event` 必須提供 `payload.summary`。
- `upsert_task` 建立新 task 時必須提供 `payload.title`。
- `reveal_secret` 的 target secret 必須存在，且必須列在 active scene contract 的 `allowedSecretIds`。

## Forbidden Reveal

validator 會從 SQLite 載入未揭露 secrets。

`narrative` 不得包含任何未揭露 secret 的完整 content，除非同一個 result 內存在合法的 `reveal_secret` operation 且該 secret 被 scene contract 允許。

active scene contract 的 `forbiddenReveals` 片語也不得出現在 narrative。

## MVP-2 Rules

- `protocolVersion` must be `2`.
- `contextId`、`sessionId`、`turnNumber`、`baseStateVersion`、`contextHash` must match the pending context.
- `resultId` is required and cannot have been applied before.
- `characterActions` are required; every action must use actors and memory/item/technique IDs present in the context.
- `characterFitScore` must be at least `70`.
- Forbidden reveals are scanned across narrative、turnSummary、revealedFacts、stateDeltas payloads、memoryEffects and newTasks.
- `resolve-turn` applies all writes in one SQLite transaction; any failed write rolls back state deltas, facts, events, turn log update, scene progress and session state.

## MVP-3 Rules

- Forbidden reveals are also scanned across `memoryCandidates` and `knowledgeUpdates`.
- `characterActions` may only reference memories in `retrievedMemories` owned by that actor, public facts or actor knowledge claims, and context-visible items/techniques.
- `memoryCandidates` must have a valid owner, valid source event/fact evidence, character value, and must not copy the narrative wholesale.
- Reframe candidates must set `supersedesMemoryId`; the old memory is retained.
- `knowledgeUpdates` cannot use hidden canonical facts as automatic character knowledge.
- If a requested update tries to jump a conflicted belief straight to `KNOWN` without strong evidence, the runtime keeps the conflict visible and transitions toward `DOUBTED` instead.
- Emotion deltas require `emotion`、`delta`、`trigger`、`sourceType/sourceId` and `reason`; normal single-turn absolute change is capped at 25.
- `prepare-turn` cannot write recall logs or increase recall counts. Successful `resolve-turn` records recall logs and increments each included memory once.
